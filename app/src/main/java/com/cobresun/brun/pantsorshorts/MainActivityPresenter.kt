package com.cobresun.brun.pantsorshorts

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.cobresun.brun.pantsorshorts.SharedPrefsUserDataRepository.Companion.COLD
import com.cobresun.brun.pantsorshorts.SharedPrefsUserDataRepository.Companion.HOT
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

class MainActivityPresenter(private val view: MainActivityView, private val userDataRepository: UserDataRepository, private val mContext: Context) {

    private var currentTemp: Int = 0
    private var highTemp: Int = 0
    private var lowTemp: Int = 0

    private var clothingSuggestion: Clothing = Clothing.UNKNOWN

    private var weatherCallInProgress: Boolean = false

    private var hourlyTemps = IntArray(24)

    private val hour: Int
        get() {
            val c = Calendar.getInstance()
            return c.get(Calendar.HOUR_OF_DAY)
        }

    private fun updateUserThreshold(howTheyFelt: Int) {
        if (howTheyFelt == COLD) {
            var curPreference = userDataRepository.readUserThreshold()
            while (pantsOrShorts(curPreference) == Clothing.SHORTS) {
                curPreference++
            }
            userDataRepository.writeUserThreshold(curPreference)
        } else if (howTheyFelt == HOT) {
            var curPreference = userDataRepository.readUserThreshold()
            while (pantsOrShorts(curPreference) == Clothing.PANTS) {
                curPreference--
            }
            userDataRepository.writeUserThreshold(curPreference)
        }
    }

    private fun pantsOrShorts(preference: Int): Clothing {
        val curTime = hour
        var average = 0

        val hoursToInclude = max(HOURS_SPENT_OUT, AVERAGE_HOME_TIME - curTime)

        for (i in 0 until hoursToInclude) {
            if (hourlyTemps[i] >= preference) {
                average++
            } else {
                average--
            }
        }

        return if (average >= 0) {
            Clothing.SHORTS
        } else {
            Clothing.PANTS
        }
    }

    fun calibrateThreshold() {
        if (clothingSuggestion == Clothing.SHORTS) {
            updateUserThreshold(COLD)
        } else {
            updateUserThreshold(HOT)
        }
        updateClothing()
    }

    private fun updateClothing() {
        val clothing = pantsOrShorts(userDataRepository.readUserThreshold())
        clothingSuggestion = clothing
        view.displayClothingImage(clothing)
        view.displayButton(clothing)
        view.displayYouShouldWearText(clothing)
    }

    fun createLocationRequest(activity: Activity) {
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    if (location != null) {
                        Log.d(this@MainActivityPresenter.toString(), "Successfully got location")
                        val city = getCity(location.latitude, location.longitude)
                        view.displayCity(city)
                        getWeather(location)
                    } else {
                        Log.e(this@MainActivityPresenter.toString(), "Location fetch failed!")
                    }
                }
            }
        }

        val locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(mContext)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener(activity) {
            // All location settings are satisfied. The client can initialize location requests here.
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                view.requestPermissions()
            }

             LocationServices
                 .getFusedLocationProviderClient(mContext)
                 .requestLocationUpdates(locationRequest, locationCallback, null)
        }
        task.addOnFailureListener(activity) { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.e(this@MainActivityPresenter.toString(), sendEx.toString())
                }

            }
        }
    }

    private fun getCity(lats: Double, lons: Double): String {
        val geocoder = Geocoder(mContext, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(lats, lons, 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return if (addresses != null) {
            addresses[0].locality
        } else {
            "failed" // TODO: This is so weird...
        }
    }

    private fun isNetworkStatusAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.activeNetworkInfo?.let { return it.isConnected }
        return false
    }

    // TODO: The problem with doing this is: if they connect after opening the app, we don't respond!
    //  They stay disconnected until they restart the app
    fun checkInternet() {
        if (!isNetworkStatusAvailable(mContext)) {
            view.displayNoInternet()
        }
    }

    // TODO: This should really be abstracted away into some reusable reactive utility
    private fun getWeather(location: Location) {
        if (weatherCallInProgress) {
            return
        }
        val lastFetched = userDataRepository.readLastTimeFetchedWeather()
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - lastFetched

        val isFirstTime = userDataRepository.isFirstTimeLaunching
        val apiKey = mContext.resources.getString(R.string.dark_sky)

        if (diff < MINUTE_MILLIS && !isFirstTime) {
            val isCelsius = userDataRepository.isCelsius

            currentTemp = userDataRepository.readLastFetchedTemp()
            highTemp = userDataRepository.readLastFetchedTempHigh()
            lowTemp = userDataRepository.readLastFetchedTempLow()
            hourlyTemps = userDataRepository.readLastFetchedHourlyTemps()

            view.displayTemperature(currentTemp, isCelsius)
            view.displayHighTemperature(highTemp, isCelsius)
            view.displayLowTemperature(lowTemp, isCelsius)
            updateClothing()
        } else {
            weatherCallInProgress = true
            val retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://api.darksky.net/")
                    .build()

            val service = retrofit.create(WeatherAPIService::class.java)
            service.getTemp(apiKey, location.latitude, location.longitude).enqueue(object : Callback<ForecastResponse> {
                override fun onResponse(call: Call<ForecastResponse>, response: Response<ForecastResponse>) {
                    currentTemp = response.body()?.currently?.apparentTemperature?.roundToInt()!!
                    highTemp = response.body()?.daily?.data?.get(0)?.apparentTemperatureMax?.roundToInt()!!
                    lowTemp = response.body()?.daily?.data?.get(0)?.apparentTemperatureMin?.roundToInt()!!

                    for (i in hourlyTemps.indices) {
                        hourlyTemps[i] = response.body()?.hourly?.data?.get(i)?.apparentTemperature?.roundToInt()!!
                    }

                    userDataRepository.writeLastFetchedTemp(currentTemp)
                    userDataRepository.writeLastFetchedTempHigh(highTemp)
                    userDataRepository.writeLastFetchedTempLow(lowTemp)
                    userDataRepository.writeLastFetchedHourlyTemps(hourlyTemps)
                    userDataRepository.writeLastTimeFetchedWeather(currentTime)
                    userDataRepository.writeIsCelsius(true)
                    view.displayTemperature(currentTemp, true)
                    view.displayHighTemperature(highTemp, true)
                    view.displayLowTemperature(lowTemp, true)
                    updateClothing()
                    weatherCallInProgress = false
                }

                override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                    Log.e(this@MainActivityPresenter.toString(), t.toString())
                }
            })
        }
    }

    fun updateTempMode() {
        val isCelsius = userDataRepository.isCelsius
        userDataRepository.writeIsCelsius(!isCelsius)
        view.displayTemperature(currentTemp, !isCelsius)
        view.displayHighTemperature(highTemp, !isCelsius)
        view.displayLowTemperature(lowTemp, !isCelsius)
    }

    fun setupNightMode() {
        val isNightMode = userDataRepository.isNightMode
        userDataRepository.writeNightMode(isNightMode)
        view.displayNightMode(isNightMode)
    }

    fun toggleNightMode() {
        val isNightMode = userDataRepository.isNightMode
        userDataRepository.writeNightMode(!isNightMode)
        view.displayNightMode(!isNightMode)
    }

    companion object {
        const val HOURS_SPENT_OUT = 4
        const val AVERAGE_HOME_TIME = 18

        val INITIAL_PERMS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        const val INITIAL_REQUEST = 1337
        const val REQUEST_CHECK_SETTINGS = 8888

        private const val SECOND_MILLIS = 1000
        private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    }
}

enum class Clothing { PANTS, SHORTS, UNKNOWN }
