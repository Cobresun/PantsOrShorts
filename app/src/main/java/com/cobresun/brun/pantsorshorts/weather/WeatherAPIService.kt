package com.cobresun.brun.pantsorshorts.weather

import retrofit2.http.Path

interface WeatherAPIService {
    suspend fun getWeatherResponse(
        @Path(value = "appid") appid: String,
        @Path(value = "lat") lat: Double,
        @Path(value = "lon") lon: Double
    ): WeatherResponse
}
