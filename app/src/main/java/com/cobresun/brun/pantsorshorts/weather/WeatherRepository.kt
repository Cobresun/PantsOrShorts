package com.cobresun.brun.pantsorshorts.weather

import com.cobresun.brun.pantsorshorts.BuildConfig
import com.cobresun.brun.pantsorshorts.weather.api.PirateWeatherAPIService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val apiService: PirateWeatherAPIService
) {
    suspend fun getWeather(latitude: Double, longitude: Double): WeatherResponse {
        return apiService.getWeatherResponse(BuildConfig.PirateWeatherAPIKey, latitude, longitude)
    }
}
