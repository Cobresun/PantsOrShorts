package com.cobresun.brun.pantsorshorts

interface UserDataRepository {

    val isFirstTimeLaunching: Boolean

    var isNightMode: Boolean

    var userThreshold: Int

    var lastTimeFetchedWeather: Long

    var lastFetchedTemp: Int

    var lastFetchedTempHigh: Int

    var lastFetchedTempLow: Int

    fun readLastFetchedHourlyTemps(): IntArray

    fun writeLastFetchedHourlyTemps(temps: IntArray)
}
