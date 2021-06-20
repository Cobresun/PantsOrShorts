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
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.cobresun.brun.pantsorshorts.databinding.ActivityMainBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import splitties.toast.toast
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    @Inject lateinit var locator: Locator

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        Objects.requireNonNull<ActionBar>(supportActionBar).hide()

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                binding.loadingGroup.visibility = GONE
                binding.requestPermissionGroup.visibility = GONE
                binding.mainGroup.visibility = VISIBLE
            } else {
                binding.requestPermissionGroup.visibility = VISIBLE
                binding.mainGroup.visibility = GONE
                binding.loadingGroup.visibility = GONE
            }
        }

        connectivityManager = getSystemService(ConnectivityManager::class.java)
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                createLocationRequest()
            }

            override fun onLost(network: Network) {
                toast("The application no longer has access to the internet.")
            }

            override fun onUnavailable() {
                toast(R.string.internet_unavailable)
            }
        }

        binding.mainButton.setOnClickListener {
            viewModel.calibrateThreshold()
            toast(R.string.remember_that)
        }

        binding.permissionButton.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        setupViews()
    }

    override fun onResume() {
        super.onResume()
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onPause() {
        super.onPause()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun setupViews() {
        viewModel.clothingSuggestion.observe(this, {
            it?.let {
                when (it) {
                    Clothing.PANTS -> {
                        binding.clothingImageView.tag = getString(R.string.pants)
                        binding.clothingImageView.setImageResource(R.drawable.pants)

                        binding.mainButton.text = getString(R.string.too_hot)
                        binding.mainButton.setBackgroundResource(R.drawable.my_button_red)
                        val sun = ResourcesCompat.getDrawable(resources, R.drawable.ic_wb_sunny, null)
                        binding.mainButton.setCompoundDrawablesWithIntrinsicBounds(sun, null, null, null)

                        binding.shouldWearTextView.text = getString(R.string.feels_like_pants)
                    }
                    Clothing.SHORTS -> {
                        binding.clothingImageView.tag = getString(R.string.shorts)
                        binding.clothingImageView.setImageResource(R.drawable.shorts)

                        binding.mainButton.text = getString(R.string.too_cold)
                        val snow = ResourcesCompat.getDrawable(resources ,R.drawable.ic_ac_unit, null)
                        binding.mainButton.setCompoundDrawablesWithIntrinsicBounds(snow, null, null, null)
                        binding.mainButton.setBackgroundResource(R.drawable.my_button_blue)

                        binding.shouldWearTextView.text = getString(R.string.feels_like_shorts)
                    }
                }
                binding.clothingImageView.invalidate()
                binding.mainButton.invalidate()
                binding.shouldWearTextView.invalidate()
                binding.mainGroup.visibility = VISIBLE
            }
        })

        viewModel.cityName.observe(this, {
            when (it) {
                null -> createLocationRequest()
                else -> {
                    binding.cityName.text = it
                    binding.cityName.invalidate()
                }
            }
        })

        viewModel.currentTemp.observe(this, {
            it?.let {
                binding.temperatureTextView.text = getString(R.string.celsius, it)
            }
            binding.temperatureTextView.invalidate()
        })

        viewModel.highTemp.observe(this, {
            it?.let {
                binding.temperatureHighTextView.text = getString(R.string.celsius, it)
            }
            binding.temperatureHighTextView.invalidate()
        })

        viewModel.lowTemp.observe(this, {
            it?.let {
                binding.temperatureLowTextView.text = getString(R.string.celsius, it)
            }
            binding.temperatureLowTextView.invalidate()
        })
    }

    fun createLocationRequest() {
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                LocationServices
                    .getFusedLocationProviderClient(applicationContext)
                    .removeLocationUpdates(this)

                val city = locator.getCityName(locationResult.lastLocation)
                city?.let { viewModel.setCityName(city) }

                when (viewModel.shouldFetchWeather()) {
                    true -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            val latitude = locationResult.lastLocation.latitude
                            val longitude = locationResult.lastLocation.longitude
                            viewModel.fetchWeather(latitude, longitude)
                            viewModel.writeAndDisplayNewData()
                        }
                    }
                    false -> viewModel.loadAndDisplayPreviousData()
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
            .getSettingsClient(applicationContext)
            .checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                when {
                    ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        binding.requestPermissionGroup.visibility = GONE
                        binding.loadingGroup.visibility = GONE
                        LocationServices
                            .getFusedLocationProviderClient(applicationContext)
                            .requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                Looper.getMainLooper()
                            )
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                        binding.mainGroup.visibility = GONE
                        binding.loadingGroup.visibility = GONE
                        binding.requestPermissionGroup.visibility = VISIBLE
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            }
            .addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    try {
                        e.startResolutionForResult(this, 8888)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Log.e(this.toString(), sendEx.toString())
                    }
                }
            }
    }
}
