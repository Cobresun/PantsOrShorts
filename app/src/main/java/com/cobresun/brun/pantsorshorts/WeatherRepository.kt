package com.cobresun.brun.pantsorshorts

class WeatherRepository(
    private val weatherApiKey: String,
    private val apiService: WeatherAPIService
) {
    suspend fun getWeather(latitude: Double, longitude: Double): ForecastResponse {
        return apiService.getForecastResponse(weatherApiKey, latitude, longitude)
    }
}
