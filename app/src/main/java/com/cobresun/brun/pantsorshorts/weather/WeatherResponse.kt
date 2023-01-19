package com.cobresun.brun.pantsorshorts.weather

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    val currently: CurrentlyResponse,
    val daily: DailyResponse,
    val hourly: HourlyResponse
)

@JsonClass(generateAdapter = true)
data class CurrentlyResponse(
    val temperature: Double,
    val apparentTemperature: Double
)

@JsonClass(generateAdapter = true)
data class DailyResponse(
    val data: List<DayResponse>
)

@JsonClass(generateAdapter = true)
data class HourlyResponse(
    val data: List<HourResponse>
)

@JsonClass(generateAdapter = true)
data class HourResponse(
    val apparentTemperature: Double
)

@JsonClass(generateAdapter = true)
data class DayResponse(
    val temperatureMax: Double,
    val temperatureMin: Double,
    val apparentTemperatureMax: Double,
    val apparentTemperatureMin: Double
)
