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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
        private val userDataRepository: UserDataRepository,
        private val mContext: Context) {

    /** New Observables to replace interface! */

    private val _currentTemp: MutableLiveData<Int> = MutableLiveData()
    val currentTemp: LiveData<Int>
        get() = _currentTemp

    private val _highTemp: MutableLiveData<Int> = MutableLiveData()
    val highTemp: LiveData<Int>
        get() = _highTemp

    private val _lowTemp: MutableLiveData<Int> = MutableLiveData()
    val lowTemp: LiveData<Int>
        get() = _lowTemp

    private val _clothingSuggestion: MutableLiveData<Clothing> = MutableLiveData()
    val clothingSuggestion: LiveData<Clothing>
        get() = _clothingSuggestion

    private val _cityName: MutableLiveData<String> = MutableLiveData()
    val cityName: LiveData<String>
        get() = _cityName

    /** New Observables to replace interface! */

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
        when (clothingSuggestion.value) {
            PANTS -> updateUserThreshold(HOT)
            SHORTS -> updateUserThreshold(COLD)
            UNKNOWN -> Log.e(this.toString(), "calibrateThreshold() but current suggestion unknown")
        }
        updateClothing()
    }

    private fun updateClothing() {
        val clothing = pantsOrShorts(userDataRepository.userThreshold)
        _clothingSuggestion.value = clothing
    }

    fun createLocationRequest(activity: Activity) {
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                LocationServices
                        .getFusedLocationProviderClient(mContext)
                        .removeLocationUpdates(this)

                val city = getCity(locationResult.lastLocation)
                _cityName.value = city

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
//                            view.requestPermissions()
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
//            view.displayNoInternet()
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

        _currentTemp.value = forecastResponse.currently.apparentTemperature.roundToInt()
        _highTemp.value = forecastResponse.daily.data[0].apparentTemperatureMax.roundToInt()
        _lowTemp.value = forecastResponse.daily.data[0].apparentTemperatureMin.roundToInt()
        for (i in hourlyTemps.indices) {
            hourlyTemps[i] = forecastResponse.hourly.data[i].apparentTemperature.roundToInt()
        }
    }

    private fun writeAndDisplayNewData() {
        userDataRepository.lastFetchedTemp = currentTemp.value!!
        userDataRepository.lastFetchedTempHigh = highTemp.value!!
        userDataRepository.lastFetchedTempLow = lowTemp.value!!
        userDataRepository.writeLastFetchedHourlyTemps(hourlyTemps)
        userDataRepository.lastTimeFetchedWeather = System.currentTimeMillis()
        updateClothing()
    }

    private fun loadAndDisplayPreviousData() {
        _currentTemp.value = userDataRepository.lastFetchedTemp
        _highTemp.value = userDataRepository.lastFetchedTempHigh
        _lowTemp.value = userDataRepository.lastFetchedTempLow
        hourlyTemps = userDataRepository.readLastFetchedHourlyTemps()
        updateClothing()
    }

    fun setupNightMode() {
        val isNightMode = userDataRepository.isNightMode
        userDataRepository.isNightMode = isNightMode
//        view.displayNightMode(isNightMode)
    }

    fun toggleNightMode() {
        val isNightMode = userDataRepository.isNightMode
        userDataRepository.isNightMode = !isNightMode
//        view.displayNightMode(!isNightMode)
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
