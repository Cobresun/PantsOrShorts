package com.cobresun.brun.pantsorshorts.weather

import retrofit2.http.GET
import retrofit2.http.Path

/** TODO:
 *   Remove the exclude=flags and units=ca and take in default unit temperature.
 *   Read flags.units in response from: https://darksky.net/dev/docs#forecast-request
 *   to get the temperature being sent.
 *   If app's first launch: store the unit sent as the preference.
 *   User can tap on temperature to change their preference.
 *   If not the first launch: use preference like temperature.toCelsius / temperature.toFahrenheit
 **/
interface WeatherAPIService {
    @GET("forecast/{appid}/{lat},{lon}?exclude=minutely,alerts,flags&units=ca")
    suspend fun getDarkSkyResponse(
        @Path("appid") appid: String,
        @Path("lat") lat: Double,
        @Path("lon") lon: Double
    ): DarkSkyResponse
}
