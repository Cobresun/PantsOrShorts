package com.cobresun.brun.pantsorshorts

import android.content.Context
import android.content.Context.MODE_PRIVATE

class SharedPrefsUserDataRepository(private val context: Context) : UserDataRepository {

    override val isFirstTimeLaunching: Boolean
        get() {
            val settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            val isFirstTime = settings.getBoolean("isFirstTime", true)

            if (isFirstTime) {
                val editor = settings.edit()
                editor.putBoolean("isFirstTime", false)
                editor.apply()
            }

            return isFirstTime
        }

    override val isNightMode: Boolean
        get() {
            val settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            return settings.getBoolean("isNightMode", false)
        }

    override val isCelsius: Boolean
        get() {
            val settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            return settings.getBoolean("isCelsius", false)
        }

    override fun readUserThreshold(): Int {
        val defaultThreshold = 21
        val settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return settings.getInt("userThreshold", defaultThreshold)
    }

    override fun writeUserThreshold(threshold: Int) {
        val settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = settings.edit()
        editor.putInt("userThreshold", threshold)
        editor.apply()
    }

    override fun readLastTimeFetchedWeather(): Long {
        val defaultTime = System.currentTimeMillis()
        val settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return settings.getLong("timeLastFetched", defaultTime)
    }

    override fun writeLastTimeFetchedWeather(time: Long) {
        val settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = settings.edit()
        editor.putLong("timeLastFetched", time)
        editor.apply()
    }

    override fun readLastFetchedTemp(): Int {
        val defaultTemp = 1000
        val settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return settings.getInt("tempLastFetched", defaultTemp)
    }

    override fun writeLastFetchedTemp(temp: Int) {
        val settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = settings.edit()
        editor.putInt("tempLastFetched", temp)
        editor.apply()
    }

    override fun readLastFetchedTempHigh(): Int {
        val defaultTemp = 1000
        val settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return settings.getInt("tempHighLastFetched", defaultTemp)
    }

    override fun writeLastFetchedTempHigh(temp: Int) {
        val settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = settings.edit()
        editor.putInt("tempHighLastFetched", temp)
        editor.apply()
    }

    override fun readLastFetchedTempLow(): Int {
        val defaultTemp = 1000
        val settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return settings.getInt("tempLowLastFetched", defaultTemp)
    }

    override fun writeLastFetchedTempLow(temp: Int) {
        val settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = settings.edit()
        editor.putInt("tempLowLastFetched", temp)
        editor.apply()
    }

    override fun readLastFetchedHourlyTemps(): IntArray {
        val defaultTemp = 10000
        val temps = IntArray(24)
        val settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        for (i in temps.indices) {
            temps[i] = settings.getInt("tempHourlyLastFetched$i", defaultTemp)
        }
        return temps
    }

    override fun writeLastFetchedHourlyTemps(temps: IntArray) {
        val settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = settings.edit()
        for (i in temps.indices) {
            editor.putInt("tempHourlyLastFetched$i", temps[i])
        }
        editor.apply()
    }

    override fun writeNightMode(nightMode: Boolean) {
        val settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = settings.edit()
        editor.putBoolean("isNightMode", nightMode)
        editor.apply()
    }

    override fun writeIsCelsius(isCelsius: Boolean) {
        val settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = settings.edit()
        editor.putBoolean("isCelsius", isCelsius)
        editor.apply()
    }

    companion object {

        // int HowTheyFelt types
        const val COLD = 1
        const val HOT = 2
        private const val PREFS_NAME = "userPrefs"
    }

}
