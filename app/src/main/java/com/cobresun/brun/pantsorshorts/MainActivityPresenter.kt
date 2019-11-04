package com.cobresun.brun.pantsorshorts

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.util.Log
import androidx.core.app.ActivityCompat.checkSelfPermission
import com.cobresun.brun.pantsorshorts.Clothing.*
import com.cobresun.brun.pantsorshorts.Feeling.COLD
import com.cobresun.brun.pantsorshorts.Feeling.HOT
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

class MainActivityPresenter(
        private val view: MainActivityView,
        private val userDataRepository: UserDataRepository,
        private val mContext: Context) {

    private var currentTemp: Int = 0
    private var highTemp: Int = 0
    private var lowTemp: Int = 0

    private var clothingSuggestion: Clothing = UNKNOWN

    private var hourlyTemps = IntArray(24)

    private fun updateUserThreshold(howTheyFelt: Feeling) {
        val currentPreference = userDataRepository.userThreshold
        when (howTheyFelt) {
            COLD -> userDataRepository.userThreshold = currentPreference + 1
            HOT -> userDataRepository.userThreshold = currentPreference - 1
        }
    }

    private fun pantsOrShorts(preference: Int): Clothing {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        var average = 0

        val hoursToInclude = max(HOURS_SPENT_OUT, AVERAGE_HOME_TIME - currentHour)

        for (i in 0 until hoursToInclude) {
            when {
                hourlyTemps[i] >= preference -> average++
                else -> average--
            }
        }

        return when {
            average >= 0 -> SHORTS
            else -> PANTS
        }
    }

    fun calibrateThreshold() {
        when (clothingSuggestion) {
            PANTS -> updateUserThreshold(HOT)
            SHORTS -> updateUserThreshold(COLD)
            UNKNOWN -> Log.e(this.toString(), "calibrateThreshold() but current suggestion unknown")
        }
        updateClothing()
    }

    private fun updateClothing() {
        val clothing = pantsOrShorts(userDataRepository.userThreshold)
        clothingSuggestion = clothing
        view.displayClothingImage(clothing)
        view.displayButton(clothing)
        view.displayYouShouldWearText(clothing)
    }

    fun createLocationRequest(activity: Activity) {
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                LocationServices
                        .getFusedLocationProviderClient(mContext)
                        .removeLocationUpdates(this)

                val city = getCity(locationResult.lastLocation)
                view.displayCity(city)

                when (shouldFetchWeather()) {
                    true -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            fetchWeather(locationResult.lastLocation)
                            writeAndDisplayNewData()
                        }
                    }
                    false -> loadAndDisplayPreviousData()
                }
            }
        }

        val locationRequest = LocationRequest
                .create()
                .setInterval(1000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        val locationSettingsRequest = LocationSettingsRequest
                .Builder()
                .addLocationRequest(locationRequest)
                .build()

        LocationServices
                .getSettingsClient(mContext)
                .checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener {
                    // All location settings are satisfied. The client can initialize location requests here.
                    if (checkSelfPermission(mContext, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
                        if (checkSelfPermission(mContext, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
                            view.requestPermissions()
                        }
                    }
                    else {
                        LocationServices
                                .getFusedLocationProviderClient(mContext)
                                .requestLocationUpdates(locationRequest, locationCallback, null)
                    }
                }
                .addOnFailureListener { e ->
                    if (e is ResolvableApiException) {
                        try {
                            e.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS)
                        } catch (sendEx: IntentSender.SendIntentException) {
                            Log.e(this@MainActivityPresenter.toString(), sendEx.toString())
                        }
                    }
                }
    }

    private fun getCity(location: Location): String? {
        val geocoder = Geocoder(mContext, Locale.getDefault())
        var addresses: List<Address> = emptyList()

        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (e: Exception) {
            Log.e(this@MainActivityPresenter.toString(), e.toString())
        }

        return if (addresses.isEmpty()) {
            Log.e(this@MainActivityPresenter.toString(), "No location found")
            null
        } else {
            addresses[0].locality
        }
    }

    private fun isNetworkStatusAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.activeNetworkInfo?.let { return it.isConnected }
        return false
    }

    // TODO: BUG - If user connects after opening the app, we don't respond! They stay disconnected until they restart the app
    fun checkInternet() {
        if (!isNetworkStatusAvailable(mContext)) {
            view.displayNoInternet()
        }
    }

    private fun shouldFetchWeather(): Boolean {
        val lastFetched = userDataRepository.lastTimeFetchedWeather
        val timeSinceFetched = System.currentTimeMillis() - lastFetched
        val isFirstTime = userDataRepository.isFirstTimeLaunching

        // Rate limiting to fetching only after 10 minutes
        return (timeSinceFetched > MINUTE_MILLIS * 10) || isFirstTime
    }

    private suspend fun fetchWeather(location: Location) {
        val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.darksky.net/")
                .build()

        val service = retrofit.create(WeatherAPIService::class.java)
        val apiKey = mContext.resources.getString(R.string.dark_sky)
        val forecastResponse = service.getForecastResponse(apiKey, location.latitude, location.longitude)

        currentTemp = forecastResponse.currently.apparentTemperature.roundToInt()
        highTemp = forecastResponse.daily.data[0].apparentTemperatureMax.roundToInt()
        lowTemp = forecastResponse.daily.data[0].apparentTemperatureMin.roundToInt()
        for (i in hourlyTemps.indices) {
            hourlyTemps[i] = forecastResponse.hourly.data[i].apparentTemperature.roundToInt()
        }
    }

    private fun writeAndDisplayNewData() {
        userDataRepository.lastFetchedTemp = currentTemp
        userDataRepository.lastFetchedTempHigh = highTemp
        userDataRepository.lastFetchedTempLow = lowTemp
        userDataRepository.writeLastFetchedHourlyTemps(hourlyTemps)
        userDataRepository.lastTimeFetchedWeather = System.currentTimeMillis()
        // BUG - So the fahrenheit setting only persists till next fetch, at which point we reset it back to celsius...
        // TODO: Map weather to correct degree by reading isCelsius from repo first!
        userDataRepository.isCelsius = true

        view.displayTemperature(currentTemp, true)
        view.displayHighTemperature(highTemp, true)
        view.displayLowTemperature(lowTemp, true)
        updateClothing()
    }

    private fun loadAndDisplayPreviousData() {
        val isCelsius = userDataRepository.isCelsius

        currentTemp = userDataRepository.lastFetchedTemp
        highTemp = userDataRepository.lastFetchedTempHigh
        lowTemp = userDataRepository.lastFetchedTempLow
        hourlyTemps = userDataRepository.readLastFetchedHourlyTemps()

        view.displayTemperature(currentTemp, isCelsius)
        view.displayHighTemperature(highTemp, isCelsius)
        view.displayLowTemperature(lowTemp, isCelsius)
        updateClothing()
    }

    fun updateTempMode() {
        val isCelsius = userDataRepository.isCelsius
        userDataRepository.isCelsius = !isCelsius
        view.displayTemperature(currentTemp, !isCelsius)
        view.displayHighTemperature(highTemp, !isCelsius)
        view.displayLowTemperature(lowTemp, !isCelsius)
    }

    fun setupNightMode() {
        val isNightMode = userDataRepository.isNightMode
        userDataRepository.isNightMode = isNightMode
        view.displayNightMode(isNightMode)
    }

    fun toggleNightMode() {
        val isNightMode = userDataRepository.isNightMode
        userDataRepository.isNightMode = !isNightMode
        view.displayNightMode(!isNightMode)
    }

    companion object {
        const val HOURS_SPENT_OUT = 4
        const val AVERAGE_HOME_TIME = 18

        val INITIAL_PERMS = arrayOf(ACCESS_FINE_LOCATION)
        const val INITIAL_REQUEST = 1337
        const val REQUEST_CHECK_SETTINGS = 8888

        private const val SECOND_MILLIS = 1000
        private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    }
}

enum class Clothing { PANTS, SHORTS, UNKNOWN }

enum class Feeling { COLD, HOT }
