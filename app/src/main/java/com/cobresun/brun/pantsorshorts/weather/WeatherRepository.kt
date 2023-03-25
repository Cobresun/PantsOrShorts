package com.cobresun.brun.pantsorshorts.weather

import com.cobresun.brun.pantsorshorts.weather.api.WeatherAPIService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val apiService: WeatherAPIService,
    private val apiKey: String
) {
    suspend fun getWeather(latitude: Double, longitude: Double): WeatherResponse {
        return apiService.getWeatherResponse(apiKey, latitude, longitude)
    }
}
