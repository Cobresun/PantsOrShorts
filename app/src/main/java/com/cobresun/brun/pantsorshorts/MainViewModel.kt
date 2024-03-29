package com.cobresun.brun.pantsorshorts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cobresun.brun.pantsorshorts.weather.Clothing.PANTS
import com.cobresun.brun.pantsorshorts.weather.Clothing.SHORTS
import com.cobresun.brun.pantsorshorts.weather.Feeling.COLD
import com.cobresun.brun.pantsorshorts.weather.Feeling.HOT
import com.cobresun.brun.pantsorshorts.preferences.SharedPrefsUserDataRepository
import com.cobresun.brun.pantsorshorts.weather.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userDataRepository: SharedPrefsUserDataRepository,
    private val weatherRepository: WeatherRepository,
) : ViewModel() {

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _currentTemp: MutableLiveData<Temperature> = MutableLiveData()
    val currentTemp: LiveData<Temperature> = _currentTemp

    private val _highTemp: MutableLiveData<Temperature> = MutableLiveData()
    val highTemp: LiveData<Temperature> = _highTemp

    private val _lowTemp: MutableLiveData<Temperature> = MutableLiveData()
    val lowTemp: LiveData<Temperature> = _lowTemp

    private val _clothingSuggestion: MutableLiveData<Clothing> = MutableLiveData()
    val clothingSuggestion: LiveData<Clothing> = _clothingSuggestion

    private val _cityName: MutableLiveData<String> = MutableLiveData()
    val cityName: LiveData<String> = _cityName

    private var hourlyTemps = IntArray(24)

    private fun updateUserThreshold(howTheyFelt: Feeling) {
        var currentPreference = userDataRepository.userThreshold
        when (howTheyFelt) {
            COLD -> {
                while (pantsOrShorts(currentPreference) == SHORTS) {
                    currentPreference += 1
                }
            }
            HOT -> {
                while (pantsOrShorts(currentPreference) == PANTS) {
                    currentPreference -= 1
                }
            }
        }
        userDataRepository.userThreshold = currentPreference
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
            null -> TODO()
        }
        updateClothing()
    }

    fun toggleTemperatureUnit() {
        val newUnit = when (userDataRepository.temperatureUnit) {
            TemperatureUnit.CELSIUS -> TemperatureUnit.FAHRENHEIT
            TemperatureUnit.FAHRENHEIT -> TemperatureUnit.CELSIUS
        }
        userDataRepository.temperatureUnit = newUnit
        loadAndDisplayPreviousData()
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
        when (userDataRepository.temperatureUnit) {
            TemperatureUnit.CELSIUS -> {
                _currentTemp.value = Temperature(forecastResponse.currently.apparentTemperature.roundToInt(), TemperatureUnit.CELSIUS)
                _highTemp.value = Temperature(forecastResponse.daily.data[0].apparentTemperatureMax.roundToInt(), TemperatureUnit.CELSIUS)
                _lowTemp.value = Temperature(forecastResponse.daily.data[0].apparentTemperatureMin.roundToInt(), TemperatureUnit.CELSIUS)
            }
            TemperatureUnit.FAHRENHEIT -> {
                _currentTemp.value = Temperature(forecastResponse.currently.apparentTemperature.roundToInt(), TemperatureUnit.CELSIUS).toFahrenheit()
                _highTemp.value = Temperature(forecastResponse.daily.data[0].apparentTemperatureMax.roundToInt(), TemperatureUnit.CELSIUS).toFahrenheit()
                _lowTemp.value = Temperature(forecastResponse.daily.data[0].apparentTemperatureMin.roundToInt(), TemperatureUnit.CELSIUS).toFahrenheit()
            }
        }

        for (i in hourlyTemps.indices) {
            hourlyTemps[i] = forecastResponse.hourly.data[i].apparentTemperature.roundToInt()
        }
    }

    fun writeAndDisplayNewData() {
        userDataRepository.lastFetchedTemp = currentTemp.value!!.value
        userDataRepository.lastFetchedTempHigh = highTemp.value!!.value
        userDataRepository.lastFetchedTempLow = lowTemp.value!!.value
        userDataRepository.writeLastFetchedHourlyTemps(hourlyTemps)
        userDataRepository.lastTimeFetchedWeather = System.currentTimeMillis()
        updateClothing()
        _isLoading.value = false
    }

    fun loadAndDisplayPreviousData() {
        when (userDataRepository.temperatureUnit) {
            TemperatureUnit.CELSIUS -> {
                _currentTemp.value = Temperature(userDataRepository.lastFetchedTemp, TemperatureUnit.CELSIUS)
                _highTemp.value = Temperature(userDataRepository.lastFetchedTempHigh, TemperatureUnit.CELSIUS)
                _lowTemp.value = Temperature(userDataRepository.lastFetchedTempLow, TemperatureUnit.CELSIUS)
            }
            TemperatureUnit.FAHRENHEIT -> {
                _currentTemp.value = Temperature(userDataRepository.lastFetchedTemp, TemperatureUnit.CELSIUS).toFahrenheit()
                _highTemp.value = Temperature(userDataRepository.lastFetchedTempHigh, TemperatureUnit.CELSIUS).toFahrenheit()
                _lowTemp.value = Temperature(userDataRepository.lastFetchedTempLow, TemperatureUnit.CELSIUS).toFahrenheit()
            }
        }
        hourlyTemps = userDataRepository.readLastFetchedHourlyTemps()
        updateClothing()
        _isLoading.value = false
    }

    fun setCityName(city: String) {
        _cityName.value = city
    }
}
