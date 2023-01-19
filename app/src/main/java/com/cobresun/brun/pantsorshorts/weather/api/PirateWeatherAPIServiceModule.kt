package com.cobresun.brun.pantsorshorts.weather.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object PirateWeatherAPIServiceModule {

    @Provides
    fun providePirateWeatherAPIService() : PirateWeatherAPIService {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .baseUrl("https://dev.pirateweather.net")
            .build()
            .create(PirateWeatherAPIService::class.java)
    }
}
