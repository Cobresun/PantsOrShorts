package com.cobresun.brun.pantsorshorts.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cobresun.brun.pantsorshorts.Clothing
import com.cobresun.brun.pantsorshorts.Clothing.PANTS
import com.cobresun.brun.pantsorshorts.Clothing.SHORTS
import com.cobresun.brun.pantsorshorts.Feeling
import com.cobresun.brun.pantsorshorts.Feeling.COLD
import com.cobresun.brun.pantsorshorts.Feeling.HOT
import com.cobresun.brun.pantsorshorts.UserPreferencesDataStore
import com.cobresun.brun.pantsorshorts.weather.Temperature
import com.cobresun.brun.pantsorshorts.weather.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val weatherRepository: WeatherRepository,
) : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _cityName: MutableStateFlow<String?> = MutableStateFlow(null)

    fun initializeUiState(cityName: String?, latitude: Double, longitude: Double) {
        _cityName.update { cityName }

        viewModelScope.launch {
            weatherRepository.fetchWeather(latitude, longitude)
        }

        combine(
            weatherRepository.temperatureDataFlow,
            userPreferencesDataStore.temperatureUnitFlow,
            userPreferencesDataStore.userThresholdFlow,
            _cityName
        ) { temperatureData, temperatureUnit, userThreshold, cityName ->
            _uiState.update {
                UiState.Loaded(
                    cityName = cityName,
                    temperatures = UiState.Temperatures(
                        current = Temperature(temperatureData.current, temperatureUnit),
                        high = Temperature(temperatureData.high, temperatureUnit),
                        low = Temperature(temperatureData.low, temperatureUnit)
                    ),
                    clothing = pantsOrShorts(userThreshold, temperatureData.hourly),
                )
            }
        }.launchIn(viewModelScope)
    }

    fun calibrateThreshold(currentClothingRecommendation: Clothing) {
        when (currentClothingRecommendation) {
            PANTS -> updateUserThreshold(HOT)
            SHORTS -> updateUserThreshold(COLD)
        }
    }

    fun toggleTemperatureUnit() {
        viewModelScope.launch {
            userPreferencesDataStore.toggleTemperatureUnit()
        }
    }

    private fun updateUserThreshold(howTheyFelt: Feeling) {
        viewModelScope.launch {
            var currentPreference = userPreferencesDataStore.userThresholdFlow.first()
            val hourlyTemps = weatherRepository.temperatureDataFlow.first().hourly

            when (howTheyFelt) {
                COLD -> {
                    while (pantsOrShorts(currentPreference, hourlyTemps) == SHORTS) {
                        currentPreference += 1
                    }
                }

                HOT -> {
                    while (pantsOrShorts(currentPreference, hourlyTemps) == PANTS) {
                        currentPreference -= 1
                    }
                }
            }

            userPreferencesDataStore.setUserThreshold(currentPreference)
        }
    }

    private fun pantsOrShorts(thresholdTemp: Int, hourlyTemps: List<Int>): Clothing {
        val hoursSpentOut = 4
        val averageHomeTime = 18

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        var average = 0

        val hoursToInclude = max(hoursSpentOut, averageHomeTime - currentHour)

        for (i in 0 until hoursToInclude) {
            when {
                hourlyTemps[i] >= thresholdTemp -> average++
                else -> average--
            }
        }

        return when {
            average >= 0 -> SHORTS
            else -> PANTS
        }
    }
}
