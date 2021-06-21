package com.cobresun.brun.pantsorshorts

import kotlin.math.roundToInt

data class Temperature(
    val value: Int,
    val unit: TemperatureUnit
)

fun Temperature.toFahrenheit(): Temperature {
    return if (this.unit == TemperatureUnit.CELSIUS) {
        this.copy(value = (value.toDouble() * (9.0/5.0) + 32.0).roundToInt(), unit = TemperatureUnit.FAHRENHEIT)
    } else {
        this
    }
}

fun Temperature.toCelsius(): Temperature {
    return if (this.unit == TemperatureUnit.FAHRENHEIT) {
        this.copy(value = ((value - 32.0) * (5.0/9.0)).roundToInt(), unit = TemperatureUnit.CELSIUS)
    } else {
        this
    }
}

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT
}
