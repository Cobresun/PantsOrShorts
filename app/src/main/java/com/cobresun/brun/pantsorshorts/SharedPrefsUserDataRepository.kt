package com.cobresun.brun.pantsorshorts

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit

class SharedPrefsUserDataRepository(context: Context) : UserDataRepository {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

    override val isFirstTimeLaunching: Boolean
        get() {
            val isFirstTime = sharedPreferences.getBoolean("isFirstTime", true)

            if (isFirstTime) {
                val editor = sharedPreferences.edit()
                editor.putBoolean("isFirstTime", false)
                editor.apply()
            }

            return isFirstTime
        }

    override var isNightMode: Boolean
        get() { return sharedPreferences.getBoolean("isNightMode", false) }
        set(value) = sharedPreferences.edit { putBoolean("isNightMode", value) }

    override var userThreshold: Int
        get() = sharedPreferences.getInt("userThreshold", 21)
        set(value) = sharedPreferences.edit { putInt("userThreshold", value) }

    override var lastTimeFetchedWeather: Long
        get() = sharedPreferences.getLong("timeLastFetched", System.currentTimeMillis())
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

    companion object {
        private const val PREFS_NAME = "userPrefs"
    }
}
