package com.cobresun.brun.pantsorshorts

interface UserDataRepository {

    val isFirstTimeLaunching: Boolean

    var isNightMode: Boolean

    var userThreshold: Int

    var lastTimeFetchedWeather: Long    // TODO: Replace with type Duration from kotlin

    var lastFetchedTemp: Int

    var lastFetchedTempHigh: Int

    var lastFetchedTempLow: Int

    fun readLastFetchedHourlyTemps(): IntArray

    fun writeLastFetchedHourlyTemps(temps: IntArray)
}
