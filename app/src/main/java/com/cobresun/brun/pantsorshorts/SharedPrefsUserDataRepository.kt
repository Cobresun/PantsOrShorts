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

    override val isNightMode: Boolean
        get() { return sharedPreferences.getBoolean("isNightMode", false) }

    override val isCelsius: Boolean
        get() { return sharedPreferences.getBoolean("isCelsius", false) }

    override fun readUserThreshold() = sharedPreferences.getInt("userThreshold", 21)

    override fun writeUserThreshold(threshold: Int) = sharedPreferences.edit { putInt("userThreshold", threshold) }

    override fun readLastTimeFetchedWeather() = sharedPreferences.getLong("timeLastFetched", System.currentTimeMillis())

    override fun writeLastTimeFetchedWeather(time: Long) = sharedPreferences.edit { putLong("timeLastFetched", time) }

    override fun readLastFetchedTemp() = sharedPreferences.getInt("tempLastFetched", 1000)

    override fun writeLastFetchedTemp(temp: Int) = sharedPreferences.edit { putInt("tempLastFetched", temp) }

    override fun readLastFetchedTempHigh() = sharedPreferences.getInt("tempHighLastFetched", 1000)

    override fun writeLastFetchedTempHigh(temp: Int) = sharedPreferences.edit { putInt("tempHighLastFetched", temp) }

    override fun readLastFetchedTempLow() = sharedPreferences.getInt("tempLowLastFetched", 1000)

    override fun writeLastFetchedTempLow(temp: Int) = sharedPreferences.edit { putInt("tempLowLastFetched", temp) }

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

    override fun writeNightMode(nightMode: Boolean) = sharedPreferences.edit { putBoolean("isNightMode", nightMode) }

    override fun writeIsCelsius(isCelsius: Boolean) = sharedPreferences.edit { putBoolean("isCelsius", isCelsius) }

    companion object {
        private const val PREFS_NAME = "userPrefs"
    }
}
