package com.cobresun.brun.pantsorshorts.widget

import android.annotation.SuppressLint
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import com.cobresun.brun.pantsorshorts.Locator
import com.cobresun.brun.pantsorshorts.R
import com.cobresun.brun.pantsorshorts.Temperature
import com.cobresun.brun.pantsorshorts.TemperatureUnit
import com.cobresun.brun.pantsorshorts.preferences.SharedPrefsUserDataRepository
import com.cobresun.brun.pantsorshorts.weather.WeatherRepository
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class UpdateWidgetService: Service() {
    @Inject lateinit var weatherRepository: WeatherRepository
    @Inject lateinit var userDataRepository: SharedPrefsUserDataRepository
    @Inject lateinit var fusedLocationClient: FusedLocationProviderClient
    @Inject lateinit var locator: Locator

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val updateViews = buildUpdate(this)
        val thisWidget = ComponentName(this, MyAppWidgetProvider::class.java)
        val manager = AppWidgetManager.getInstance(this)
        manager.updateAppWidget(thisWidget, updateViews)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // TODO: I'm sure suppressing this will screw me later...
    @SuppressLint("MissingPermission")
    fun buildUpdate(context: Context): RemoteViews {

        val views = RemoteViews(context.packageName, R.layout.appwidget)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    Log.d("bnor", "fetching current temp")
                    val currentTemp: Temperature = runBlocking {
                        fetchCurrentTemp(location.latitude, location.longitude)
                    }
                    Log.d("bnor", currentTemp.toString())
                    views.setTextViewText(R.id.current_temp, currentTemp.value.toString())
                }
            }
        return views
    }

    // TODO: Not great that this function has been duplicated here...
    private suspend fun fetchCurrentTemp(latitude: Double, longitude: Double): Temperature {
        val forecastResponse = withContext(Dispatchers.IO) {
            weatherRepository.getWeather(latitude, longitude)
        }
        return Temperature(forecastResponse.currently.apparentTemperature.roundToInt(), TemperatureUnit.CELSIUS)
    }
}
