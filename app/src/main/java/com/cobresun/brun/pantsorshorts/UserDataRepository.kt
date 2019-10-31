package com.cobresun.brun.pantsorshorts

interface UserDataRepository {

    val isFirstTimeLaunching: Boolean

    val isNightMode: Boolean

    val isCelsius: Boolean

    fun hasUserUpdated(): Boolean

    fun clearUserThreshold()

    fun readUserThreshold(): Int

    fun writeUserThreshold(threshold: Int)

    fun readLastTimeFetchedWeather(): Long

    fun writeLastTimeFetchedWeather(time: Long)

    fun readLastFetchedTemp(): Int

    fun writeLastFetchedTemp(temp: Int)

    fun readLastFetchedTempHigh(): Int

    fun writeLastFetchedTempHigh(temp: Int)

    fun readLastFetchedTempLow(): Int

    fun writeLastFetchedTempLow(temp: Int)

    fun readLastFetchedHourlyTemps(): IntArray

    fun writeLastFetchedHourlyTemps(temps: IntArray)

    fun writeNightMode(nightMode: Boolean)

    fun writeIsCelsius(isCelsius: Boolean)
}
