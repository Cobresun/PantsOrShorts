package com.cobresun.brun.pantsorshorts.weather

import com.cobresun.brun.pantsorshorts.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val apiService: DarkSkyAPIService
) {
    suspend fun getWeather(latitude: Double, longitude: Double): DarkSkyResponse {
        return apiService.getDarkSkyResponse(BuildConfig.DarkSkyAPIKey, latitude, longitude)
    }
}
