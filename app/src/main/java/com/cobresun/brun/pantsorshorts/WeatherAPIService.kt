package com.cobresun.brun.pantsorshorts

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface WeatherAPIService {
    @GET("forecast/{appid}/{lat},{lon}?exclude=minutely,alerts,flags&units=ca")
    suspend fun getForecastResponse(
        @Path("appid") appid: String,
        @Path("lat") lat: Double,
        @Path("lon") lon: Double
    ): ForecastResponse
}

@Module
@InstallIn(SingletonComponent::class)
object WeatherAPIServiceModule {

    @Provides
    fun provideWeatherAPIService() : WeatherAPIService {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.darksky.net/")
            .build()
            .create(WeatherAPIService::class.java)
    }
}
