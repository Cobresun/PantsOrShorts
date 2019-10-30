package com.cobresun.brun.pantsorshorts

data class ForecastResponse (
    var currently: CurrentlyResponse? = null,
    var daily: DailyResponse? = null,
    var hourly: HourlyResponse? = null
)

data class CurrentlyResponse (
    var temperature: Double? = null,
    var apparentTemperature: Double? = null
)

data class DailyResponse (
    var data: List<DayResponse>? = null
)

data class HourlyResponse (
    var data: List<HourResponse>? = null
)

data class HourResponse (
    var apparentTemperature: Double? = null
)

data class DayResponse (
    var temperatureMax: Double? = null,
    var temperatureMin: Double? = null,
    var apparentTemperatureMax: Double? = null,
    var apparentTemperatureMin: Double? = null
)
