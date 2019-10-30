package com.cobresun.brun.pantsorshorts

class ForecastResponse {
    var currently: CurrentlyResponse? = null
    var daily: DailyResponse? = null
    var hourly: HourlyResponse? = null
}

class CurrentlyResponse {
    var temperature: Double? = null
    var apparentTemperature: Double? = null
}

class DailyResponse {
    var data: List<DayResponse>? = null
}

class HourlyResponse {
    var data: List<HourResponse>? = null
}

class HourResponse {
    var apparentTemperature: Double? = null
}

class DayResponse {
    var temperatureMax: Double? = null
    var temperatureMin: Double? = null
    var apparentTemperatureMax: Double? = null
    var apparentTemperatureMin: Double? = null
}