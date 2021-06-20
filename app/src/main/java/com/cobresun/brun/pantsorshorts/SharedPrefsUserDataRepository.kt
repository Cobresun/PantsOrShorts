package com.cobresun.brun.pantsorshorts

import android.content.SharedPreferences
import androidx.core.content.edit

class SharedPrefsUserDataRepository(
    private val sharedPreferences: SharedPreferences
) : UserDataRepository {

    override var userThreshold: Int
        get() = sharedPreferences.getInt("userThreshold", 21)
        set(value) = sharedPreferences.edit { putInt("userThreshold", value) }

    override var lastTimeFetchedWeather: Long
        get() {
            return sharedPreferences.getLong(
                "timeLastFetched",
                System.currentTimeMillis() - (1 * 60 * 60 * 1000)  // Default is 1 hour ago
            )
        }
        set(value) = sharedPreferences.edit { putLong("timeLastFetched", value) }

    override var lastFetchedTemp: Int
        get() = sharedPreferences.getInt("tempLastFetched", 1000)
        set(value) = sharedPreferences.edit { putInt("tempLastFetched", value) }

    override var lastFetchedTempHigh: Int
        get() = sharedPreferences.getInt("tempHighLastFetched", 1000)
        set(value) = sharedPreferences.edit { putInt("tempHighLastFetched", value) }

    override var lastFetchedTempLow: Int
        get() = sharedPreferences.getInt("tempLowLastFetched", 1000)
        set(value) = sharedPreferences.edit { putInt("tempLowLastFetched", value) }

    override fun readLastFetchedHourlyTemps(): IntArray {
        val defaultTemp = 10000
        val temps = IntArray(24)
        for (i in temps.indices) {
            temps[i] = sharedPreferences.getInt("tempHourlyLastFetched$i", defaultTemp)
        }
        return temps
    }

    override fun writeLastFetchedHourlyTemps(temps: IntArray) {
        val editor = sharedPreferences.edit()
        for (i in temps.indices) {
            editor.putInt("tempHourlyLastFetched$i", temps[i])
        }
        editor.apply()
    }
}
