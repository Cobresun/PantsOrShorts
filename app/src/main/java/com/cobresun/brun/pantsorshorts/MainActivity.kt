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
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.cobresun.brun.pantsorshorts.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var connectivityManager: ConnectivityManager
    @Inject lateinit var fusedLocationClient: FusedLocationProviderClient
    @Inject lateinit var locator: Locator

    private lateinit var binding: ActivityMainBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

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

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                createLocationRequest()
            }

            override fun onLost(network: Network) {
                Toast.makeText(applicationContext, "The application no longer has access to the internet.", Toast.LENGTH_SHORT).show()
            }

            override fun onUnavailable() {
                Toast.makeText(applicationContext, R.string.internet_unavailable, Toast.LENGTH_SHORT).show()
            }
        }

        binding.mainButton.setOnClickListener {
            viewModel.calibrateThreshold()
            Toast.makeText(applicationContext, R.string.remember_that, Toast.LENGTH_SHORT).show()
        }

        binding.permissionButton.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
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
                if (it.unit == TemperatureUnit.CELSIUS) {
                    binding.temperatureTextView.text = getString(R.string.celsius, it.value)
                } else {
                    binding.temperatureTextView.text = getString(R.string.fahrenheit, it.value)
                }

            }
            binding.temperatureTextView.invalidate()
        })

        viewModel.highTemp.observe(this, {
            it?.let {
                if (it.unit == TemperatureUnit.CELSIUS) {
                    binding.temperatureHighTextView.text = getString(R.string.celsius, it.value)
                } else {
                    binding.temperatureHighTextView.text = getString(R.string.fahrenheit, it.value)
                }
            }
            binding.temperatureHighTextView.invalidate()
        })

        viewModel.lowTemp.observe(this, {
            it?.let {
                if (it.unit == TemperatureUnit.CELSIUS) {
                    binding.temperatureLowTextView.text = getString(R.string.celsius, it.value)
                } else {
                    binding.temperatureLowTextView.text = getString(R.string.celsius, it.value)
                }
            }
            binding.temperatureLowTextView.invalidate()
        })
    }

    fun createLocationRequest() {
        when {
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location : Location? ->
                        location?.let {
                            val city = locator.getCityName(location)
                            city?.let { viewModel.setCityName(city) }

                            when (viewModel.shouldFetchWeather()) {
                                true -> {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        val latitude = location.latitude
                                        val longitude = location.longitude
                                        viewModel.fetchWeather(latitude, longitude)
                                        viewModel.writeAndDisplayNewData()
                                    }
                                }
                                false -> viewModel.loadAndDisplayPreviousData()
                            }
                        }
                        binding.requestPermissionGroup.visibility = GONE
                        binding.loadingGroup.visibility = GONE
                    }
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                binding.mainGroup.visibility = GONE
                binding.loadingGroup.visibility = GONE
                binding.requestPermissionGroup.visibility = VISIBLE
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
    }
}
