package com.cobresun.brun.pantsorshorts

import com.cobresun.brun.pantsorshorts.weather.Temperature
import com.cobresun.brun.pantsorshorts.weather.TemperatureUnit
import com.cobresun.brun.pantsorshorts.weather.toCelsius
import com.cobresun.brun.pantsorshorts.weather.toFahrenheit
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TemperatureTest {
    @Test
    fun temperature_CorrectCelsiusToFahrenheit_ReturnsTrue() {
        val celsius = Temperature(8, TemperatureUnit.CELSIUS)
        val expectedFahrenheit = Temperature(46, TemperatureUnit.FAHRENHEIT)
        assertThat(celsius.toFahrenheit()).isEqualTo(expectedFahrenheit)
    }

    @Test
    fun temperature_CorrectFahrenheitToCelsius() {
        val fahrenheit = Temperature(8, TemperatureUnit.FAHRENHEIT)
        val expectedCelsius = Temperature(-13, TemperatureUnit.CELSIUS)
        assertThat(fahrenheit.toCelsius()).isEqualTo(expectedCelsius)
    }

    @Test
    fun temperature_CorrectFahrenheitToFahrenheit() {
        val fahrenheit = Temperature(8, TemperatureUnit.FAHRENHEIT)
        assertThat(fahrenheit.toFahrenheit()).isEqualTo(fahrenheit)
    }

    @Test
    fun temperature_CorrectCelsiusToCelsius() {
        val celsius = Temperature(8, TemperatureUnit.CELSIUS)
        assertThat(celsius.toCelsius()).isEqualTo(celsius)
    }
}
