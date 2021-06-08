package com.cobresun.brun.pantsorshorts

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepository(private val weatherApiKey: String) {

    // TODO: Should be a dependency injection
    private val apiService = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://api.darksky.net/")
        .build()
        .create(WeatherAPIService::class.java)

    suspend fun getWeather(latitude: Double, longitude: Double): ForecastResponse {
        return apiService.getForecastResponse(weatherApiKey, latitude, longitude)
    }
}
