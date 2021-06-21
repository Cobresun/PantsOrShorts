package com.cobresun.brun.pantsorshorts.weather

data class DarkSkyResponse(
    val currently: CurrentlyResponse,
    val daily: DailyResponse,
    val hourly: HourlyResponse
)

data class CurrentlyResponse(
    val temperature: Double,
    val apparentTemperature: Double
)

data class DailyResponse(
    val data: List<DayResponse>
)

data class HourlyResponse(
    val data: List<HourResponse>
)

data class HourResponse(
    val apparentTemperature: Double
)

data class DayResponse(
    val temperatureMax: Double,
    val temperatureMin: Double,
    val apparentTemperatureMax: Double,
    val apparentTemperatureMin: Double
)
