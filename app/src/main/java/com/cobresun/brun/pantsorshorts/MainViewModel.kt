package com.cobresun.brun.pantsorshorts

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cobresun.brun.pantsorshorts.Clothing.*
import com.cobresun.brun.pantsorshorts.Feeling.COLD
import com.cobresun.brun.pantsorshorts.Feeling.HOT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

class MainViewModel(
    private val userDataRepository: UserDataRepository,
    private val weatherRepository: WeatherRepository,
    private val geocoder: Geocoder
) : ViewModel() {

    private val _currentTemp: MutableLiveData<Int> = MutableLiveData()
    val currentTemp: LiveData<Int> = _currentTemp

    private val _highTemp: MutableLiveData<Int> = MutableLiveData()
    val highTemp: LiveData<Int> = _highTemp

    private val _lowTemp: MutableLiveData<Int> = MutableLiveData()
    val lowTemp: LiveData<Int> = _lowTemp

    private val _clothingSuggestion: MutableLiveData<Clothing> = MutableLiveData()
    val clothingSuggestion: LiveData<Clothing> = _clothingSuggestion

    private val _cityName: MutableLiveData<String> = MutableLiveData()
    val cityName: LiveData<String> = _cityName

    private var hourlyTemps = IntArray(24)

    private fun updateUserThreshold(howTheyFelt: Feeling) {
        val currentPreference = userDataRepository.userThreshold
        when (howTheyFelt) {
            COLD -> userDataRepository.userThreshold = currentPreference + 1
            HOT -> userDataRepository.userThreshold = currentPreference - 1
        }
    }

    private fun pantsOrShorts(preference: Int): Clothing {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        var average = 0

        val hoursToInclude = max(HOURS_SPENT_OUT, AVERAGE_HOME_TIME - currentHour)

        for (i in 0 until hoursToInclude) {
            when {
                hourlyTemps[i] >= preference -> average++
                else -> average--
            }
        }

        return when {
            average >= 0 -> SHORTS
            else -> PANTS
        }
    }

    fun calibrateThreshold() {
        when (clothingSuggestion.value) {
            PANTS -> updateUserThreshold(HOT)
            SHORTS -> updateUserThreshold(COLD)
            UNKNOWN -> Log.e(this.toString(), "calibrateThreshold() but current suggestion unknown")
        }
        updateClothing()
    }

    private fun updateClothing() {
        val clothing = pantsOrShorts(userDataRepository.userThreshold)
        _clothingSuggestion.value = clothing
    }

    fun getCity(location: Location): String? {
        var addresses: List<Address> = emptyList()

        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (e: Exception) {
            Log.e(this@MainViewModel.toString(), e.toString())
        }

        return if (addresses.isEmpty()) {
            Log.e(this@MainViewModel.toString(), "No location found")
            null
        } else {
            addresses[0].locality
        }
    }

    fun shouldFetchWeather(): Boolean {
        val lastFetched = userDataRepository.lastTimeFetchedWeather
        val timeSinceFetched = System.currentTimeMillis() - lastFetched
        val isFirstTime = userDataRepository.isFirstTimeLaunching

        // Rate limiting to fetching only after 10 minutes
        return (timeSinceFetched > MINUTE_MILLIS * 10) || isFirstTime
    }

    // TODO: Leverage Room for built-in logic to refresh data after it gets stale
    suspend fun fetchWeather(location: Location) {
        val forecastResponse = withContext(Dispatchers.IO) {
            weatherRepository.getWeather(location.latitude, location.longitude)
        }
        _currentTemp.value = forecastResponse.currently.apparentTemperature.roundToInt()
        _highTemp.value = forecastResponse.daily.data[0].apparentTemperatureMax.roundToInt()
        _lowTemp.value = forecastResponse.daily.data[0].apparentTemperatureMin.roundToInt()
        for (i in hourlyTemps.indices) {
            hourlyTemps[i] = forecastResponse.hourly.data[i].apparentTemperature.roundToInt()
        }
    }

    fun writeAndDisplayNewData() {
        userDataRepository.lastFetchedTemp = currentTemp.value!!
        userDataRepository.lastFetchedTempHigh = highTemp.value!!
        userDataRepository.lastFetchedTempLow = lowTemp.value!!
        userDataRepository.writeLastFetchedHourlyTemps(hourlyTemps)
        userDataRepository.lastTimeFetchedWeather = System.currentTimeMillis()
        updateClothing()
    }

    fun loadAndDisplayPreviousData() {
        _currentTemp.value = userDataRepository.lastFetchedTemp
        _highTemp.value = userDataRepository.lastFetchedTempHigh
        _lowTemp.value = userDataRepository.lastFetchedTempLow
        hourlyTemps = userDataRepository.readLastFetchedHourlyTemps()
        updateClothing()
    }

    fun setCityName(city: String) {
        _cityName.value = city
    }

    companion object {
        const val HOURS_SPENT_OUT = 4
        const val AVERAGE_HOME_TIME = 18

        val INITIAL_PERMS = arrayOf(ACCESS_FINE_LOCATION)
        const val INITIAL_REQUEST = 1337
        const val REQUEST_CHECK_SETTINGS = 8888

        private const val SECOND_MILLIS = 1000
        private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    }
}

enum class Clothing { PANTS, SHORTS, UNKNOWN }

enum class Feeling { COLD, HOT }
