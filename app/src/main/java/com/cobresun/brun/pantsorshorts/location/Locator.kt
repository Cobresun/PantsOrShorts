package com.cobresun.brun.pantsorshorts.location

import android.location.Geocoder
import android.location.Location
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Locator @Inject constructor(
    private val geocoder: Geocoder
) {
    fun getCityName(location: Location): String? {
        return try {
            val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)[0]
            address?.locality ?: address?.subLocality
        } catch (e: Exception) {
            Log.e(this@Locator.toString(), e.toString())
            null
        }
    }
}
