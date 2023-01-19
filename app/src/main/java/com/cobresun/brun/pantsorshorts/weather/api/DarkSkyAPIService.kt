package com.cobresun.brun.pantsorshorts.weather.api

import com.cobresun.brun.pantsorshorts.weather.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Being deprecated in March 31st 2023: https://blog.darksky.net/
 **/
interface DarkSkyAPIService: WeatherAPIService {
    @GET("forecast/{appid}/{lat},{lon}?exclude=minutely,alerts,flags&units=ca")
    override suspend fun getWeatherResponse(
        @Path("appid") appid: String,
        @Path("lat") lat: Double,
        @Path("lon") lon: Double
    ): WeatherResponse
}
