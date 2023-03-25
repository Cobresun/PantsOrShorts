package com.cobresun.brun.pantsorshorts.weather.api

import com.cobresun.brun.pantsorshorts.BuildConfig
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
    fun provideWeatherAPIService(): WeatherAPIService {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .baseUrl("https://dev.pirateweather.net")
            .build()
            .create(PirateWeatherAPIService::class.java)
    }

    @Provides
    fun provideApiKey(): String {
        return BuildConfig.PirateWeatherAPIKey
    }
}
