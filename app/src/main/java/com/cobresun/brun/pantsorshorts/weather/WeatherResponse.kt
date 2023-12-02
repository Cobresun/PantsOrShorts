package com.cobresun.brun.pantsorshorts.weather

import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val currently: CurrentlyResponse,
    val daily: DailyResponse,
    val hourly: HourlyResponse
)

@Serializable
data class CurrentlyResponse(
    val temperature: Double,
    val apparentTemperature: Double
)

@Serializable
data class DailyResponse(
    val data: List<DayResponse>
)

@Serializable
data class HourlyResponse(
    val data: List<HourResponse>
)

@Serializable
data class HourResponse(
    val apparentTemperature: Double
)

@Serializable
data class DayResponse(
    val temperatureMax: Double,
    val temperatureMin: Double,
    val apparentTemperatureMax: Double,
    val apparentTemperatureMin: Double
)
