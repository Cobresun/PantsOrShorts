package com.cobresun.brun.pantsorshorts

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val apiService: WeatherAPIService
) {
    suspend fun getWeather(latitude: Double, longitude: Double): ForecastResponse {
        return apiService.getForecastResponse(BuildConfig.DarkSkyAPIKey, latitude, longitude)
    }
}
