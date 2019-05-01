package com.cobresun.brun.pantsorshorts.presenter;

import java.util.List;

class ForecastResponse {
    CurrentlyResponse currently;
    DailyResponse daily;
}

class CurrentlyResponse {
    Double temperature;
}

class DailyResponse {
    List<DayResponse> data;
}

class DayResponse {
    Double temperatureMax;
    Double temperatureMin;
}