package com.cobresun.brun.pantsorshorts

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface WeatherAPIService {
    @GET("forecast/{appid}/{lat},{lon}?exclude=minutely,alerts,flags&units=ca")
    fun getTemp(@Path("appid") appid: String, @Path("lat") lat: Double, @Path("lon") lon: Double): Call<ForecastResponse>
}
