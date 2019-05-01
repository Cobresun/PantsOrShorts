package com.cobresun.brun.pantsorshorts.presenter;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface WeatherAPIService {
    @GET("forecast/{appid}/{lat},{lon}?exclude=minutely,hourly,alerts,flags&units=ca")
    Call<ForecastResponse> getTemp(@Path("appid") String appid, @Path("lat") double lat, @Path("lon") double lon);
}
