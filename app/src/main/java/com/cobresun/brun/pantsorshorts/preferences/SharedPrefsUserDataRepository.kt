package com.cobresun.brun.pantsorshorts.preferences

import android.content.SharedPreferences
import androidx.core.content.edit
import com.cobresun.brun.pantsorshorts.weather.TemperatureUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefsUserDataRepository @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    var temperatureUnit: TemperatureUnit
        get() = TemperatureUnit.valueOf(sharedPreferences.getString("temperatureUnit", TemperatureUnit.CELSIUS.name)!!)
        set(value) = sharedPreferences.edit { putString("temperatureUnit", value.name) }

    var userThreshold: Int
        get() = sharedPreferences.getInt("userThreshold", 21)
        set(value) = sharedPreferences.edit { putInt("userThreshold", value) }

    var lastTimeFetchedWeather: Long
        get() {
            return sharedPreferences.getLong(
                "timeLastFetched",
                System.currentTimeMillis() - (1 * 60 * 60 * 1000)  // Default is 1 hour ago
            )
        }
        set(value) = sharedPreferences.edit { putLong("timeLastFetched", value) }

    var lastFetchedTemp: Int
        get() = sharedPreferences.getInt("tempLastFetched", 1000)
        set(value) = sharedPreferences.edit { putInt("tempLastFetched", value) }

    var lastFetchedTempHigh: Int
        get() = sharedPreferences.getInt("tempHighLastFetched", 1000)
        set(value) = sharedPreferences.edit { putInt("tempHighLastFetched", value) }

    var lastFetchedTempLow: Int
        get() = sharedPreferences.getInt("tempLowLastFetched", 1000)
        set(value) = sharedPreferences.edit { putInt("tempLowLastFetched", value) }

    fun readLastFetchedHourlyTemps(): IntArray {
        val defaultTemp = 10000
        val temps = IntArray(24)
        for (i in temps.indices) {
            temps[i] = sharedPreferences.getInt("tempHourlyLastFetched$i", defaultTemp)
        }
        return temps
    }

    fun writeLastFetchedHourlyTemps(temps: IntArray) {
        val editor = sharedPreferences.edit()
        for (i in temps.indices) {
            editor.putInt("tempHourlyLastFetched$i", temps[i])
        }
        editor.apply()
    }
}
