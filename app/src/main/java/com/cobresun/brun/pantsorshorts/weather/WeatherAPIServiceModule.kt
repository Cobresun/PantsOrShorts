package com.cobresun.brun.pantsorshorts.weather

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object WeatherAPIServiceModule {

    @Provides
    fun provideWeatherAPIService() : WeatherAPIService {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .baseUrl("https://api.darksky.net/")
            .build()
            .create(WeatherAPIService::class.java)
    }
}
