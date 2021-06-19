package com.cobresun.brun.pantsorshorts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cobresun.brun.pantsorshorts.Clothing.PANTS
import com.cobresun.brun.pantsorshorts.Clothing.SHORTS
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
        val hoursSpentOut = 4
        val averageHomeTime = 18

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        var average = 0

        val hoursToInclude = max(hoursSpentOut, averageHomeTime - currentHour)

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
        }
        updateClothing()
    }

    private fun updateClothing() {
        val clothing = pantsOrShorts(userDataRepository.userThreshold)
        _clothingSuggestion.value = clothing
    }

    fun shouldFetchWeather(): Boolean {
        val millisecondsInASecond = 1000
        val millisecondsInAMinute = 60 * millisecondsInASecond

        val lastFetched = userDataRepository.lastTimeFetchedWeather
        val timeSinceFetched = System.currentTimeMillis() - lastFetched

        // Rate limiting to fetching only after 10 minutes
        return (timeSinceFetched > millisecondsInAMinute * 10)
    }

    suspend fun fetchWeather(latitude: Double, longitude: Double) {
        val forecastResponse = withContext(Dispatchers.IO) {
            weatherRepository.getWeather(latitude, longitude)
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
}
