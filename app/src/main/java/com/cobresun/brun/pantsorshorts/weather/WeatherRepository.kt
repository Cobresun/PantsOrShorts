package com.cobresun.brun.pantsorshorts.weather

import android.util.Log
import com.cobresun.brun.pantsorshorts.weather.api.WeatherAPIService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class WeatherRepository @Inject constructor(
    private val apiService: WeatherAPIService,
    private val apiKey: String,
    private val weatherDataStore: WeatherDataStore
) {
    val temperatureDataFlow: Flow<TemperatureData> = weatherDataStore.temperatureDataFlow

    suspend fun fetchWeather(latitude: Double, longitude: Double) {
        if (needFreshWeatherData()) {
            val weatherResponse = apiService.getWeatherResponse(apiKey, latitude, longitude)
            weatherDataStore.setTemperatureData(
                TemperatureData(
                    timeFetched = System.currentTimeMillis(),
                    current = weatherResponse.currently.apparentTemperature.roundToInt(),
                    high = weatherResponse.daily.data[0].apparentTemperatureMax.roundToInt(),
                    low = weatherResponse.daily.data[0].apparentTemperatureMin.roundToInt(),
                    hourly = weatherResponse.hourly.data.map { it.apparentTemperature.roundToInt() }
                )
            )
        } else {
            Log.d("WeatherRepository", "Not fetching new weather data")
        }
    }

    /**
     * Returns true if it is time to fetch weather data.
     * We're rate limiting the fetching to 10 minutes.
     */
    private suspend fun needFreshWeatherData(): Boolean {
        val millisecondsInASecond = 1000
        val millisecondsInAMinute = 60 * millisecondsInASecond

        val lastFetched = temperatureDataFlow.first().timeFetched
        val timeSinceFetched = System.currentTimeMillis() - lastFetched

        return (timeSinceFetched > millisecondsInAMinute * 10)
    }
}
