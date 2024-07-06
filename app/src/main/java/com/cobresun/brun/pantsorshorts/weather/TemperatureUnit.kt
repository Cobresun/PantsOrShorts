package com.cobresun.brun.pantsorshorts.weather

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT
}

/**
 * Converts a celsius int to a fahrenheit int
 */
fun Int.toFahrenheit(): Any {
    return (this * 9 / 5) + 32
}
