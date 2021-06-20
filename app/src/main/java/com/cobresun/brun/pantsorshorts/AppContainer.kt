package com.cobresun.brun.pantsorshorts

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer (context: Context) {

    private val apiService = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://api.darksky.net/")
        .build()
        .create(WeatherAPIService::class.java)

    private val weatherRepository = WeatherRepository(BuildConfig.DarkSkyAPIKey, apiService)

    private val sharedPrefsUserDataRepository by lazy {
        SharedPrefsUserDataRepository(
            context.getSharedPreferences("userPrefs", AppCompatActivity.MODE_PRIVATE)
        )
    }

    val mainViewModelFactory = MainViewModelFactory(sharedPrefsUserDataRepository, weatherRepository)
}
