package com.cobresun.brun.pantsorshorts;

import java.util.List;

class ForecastResponse {
    CurrentlyResponse currently;
    DailyResponse daily;
    HourlyResponse hourly;
}

class CurrentlyResponse {
    Double temperature;
    Double apparentTemperature;
}

class DailyResponse {
    List<DayResponse> data;
}

class HourlyResponse {
    List<HourResponse> data;
}

class HourResponse {
    Double apparentTemperature;
}

class DayResponse {
    Double temperatureMax;
    Double temperatureMin;
    Double apparentTemperatureMax;
    Double apparentTemperatureMin;
}