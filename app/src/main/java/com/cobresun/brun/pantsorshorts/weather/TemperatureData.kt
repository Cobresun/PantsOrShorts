package com.cobresun.brun.pantsorshorts.weather

import kotlinx.serialization.Serializable

@Serializable
data class TemperatureData(
    val timeFetched: Long = (System.currentTimeMillis() - (1 * 60 * 60 * 1000)),  // Default is 1 hour ago
    val current: Int = 0,
    val high: Int = Int.MAX_VALUE,
    val low: Int = Int.MIN_VALUE,
    val hourly: List<Int> = List(24) { 0 }
)
