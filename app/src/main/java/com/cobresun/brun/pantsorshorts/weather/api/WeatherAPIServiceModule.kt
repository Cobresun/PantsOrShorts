package com.cobresun.brun.pantsorshorts.weather.api

import com.cobresun.brun.pantsorshorts.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object WeatherAPIServiceModule {

    @Provides
    fun provideWeatherAPIService(): WeatherAPIService {
        val json = Json {
            ignoreUnknownKeys = true
        }

        return Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory(contentType = "application/json".toMediaType()))
            .baseUrl("https://dev.pirateweather.net")
            .build()
            .create(PirateWeatherAPIService::class.java)
    }

    @Provides
    fun provideApiKey(): String {
        return BuildConfig.PirateWeatherAPIKey
    }
}
