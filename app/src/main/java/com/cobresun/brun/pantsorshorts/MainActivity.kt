/**
 * PANTS OR SHORTS
 *
 * App that informs the user whether or not they should wear pants because it is cold,
 * or shorts if it is hot, depending on the weather of the user's city, based on user preference.
 *
 * Produced by Brian Norman and Sunny Nagam
 * Cobresun - August 2018
 */

package com.cobresun.brun.pantsorshorts

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.content.ContextCompat
import com.cobresun.brun.pantsorshorts.location.Locator
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient

    @Inject
    lateinit var locator: Locator

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EntryView(
                isLoading = viewModel.isLoading.observeAsState(true),
                cityName = viewModel.cityName.observeAsState(),
                currentTemp = viewModel.currentTemp.observeAsState(),
                highTemp = viewModel.highTemp.observeAsState(),
                lowTemp = viewModel.lowTemp.observeAsState(),
                clothing = viewModel.clothingSuggestion.observeAsState(),
                mainButtonCallback = { viewModel.calibrateThreshold() },
                toggleTemperatureUnitCallback = { viewModel.toggleTemperatureUnit() }
            )
        }

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                createLocationRequest()
            }

            override fun onLost(network: Network) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.no_internet),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onUnavailable() {
                Toast.makeText(
                    applicationContext,
                    R.string.internet_unavailable,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onPause() {
        super.onPause()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    fun createLocationRequest() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            handleLocationSettingsResponse()
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleLocationSettingsResponse() {
        if (!isLocationPermissionGranted()) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    handleLocation(location)
                } ?: showLocationNotFoundMessage()
            }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun handleLocation(location: Location) {
        val city = locator.getCityName(location)
        city?.let { viewModel.setCityName(city) }

        when {
            viewModel.shouldFetchWeather() -> fetchWeather(location)
            else -> viewModel.loadAndDisplayPreviousData()
        }
    }

    private fun fetchWeather(location: Location) {
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.fetchWeather(location.latitude, location.longitude)
            viewModel.writeAndDisplayNewData()
        }
    }

    private fun showLocationNotFoundMessage() {
        Toast.makeText(
            applicationContext,
            getString(R.string.location_not_found),
            Toast.LENGTH_LONG
        ).show()
    }
}
