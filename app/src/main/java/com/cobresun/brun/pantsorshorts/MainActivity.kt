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
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import com.cobresun.brun.pantsorshorts.Clothing.*
import com.cobresun.brun.pantsorshorts.databinding.ActivityMainBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import splitties.toast.longToast
import splitties.toast.toast
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by lazy {
        MainViewModel(
                SharedPrefsUserDataRepository(applicationContext),
                WeatherRepository(applicationContext.resources.getString(R.string.dark_sky)),
                Geocoder(applicationContext, Locale.getDefault())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        Objects.requireNonNull<ActionBar>(supportActionBar).hide()

        binding.nightModeSwitch.setOnCheckedChangeListener { buttonView, _ ->
            if (buttonView.isPressed) {
                mainViewModel.toggleNightMode()
            }
        }

        binding.mainButton.setOnClickListener {
            mainViewModel.calibrateThreshold()
            toast(R.string.remember_that)
        }

        mainViewModel.clothingSuggestion.observe(this, {
            it?.let {
                when (it) {
                    PANTS -> {
                        binding.clothingImageView.tag = getString(R.string.pants)
                        binding.clothingImageView.setImageResource(R.drawable.pants)

                        binding.mainButton.text = getString(R.string.too_hot)
                        binding.mainButton.setBackgroundResource(R.drawable.my_button_red)
                        val sun = ResourcesCompat.getDrawable(resources, R.drawable.ic_wb_sunny, null)
                        binding.mainButton.setCompoundDrawablesWithIntrinsicBounds(sun, null, null, null)

                        binding.shouldWearTextView.text = getString(R.string.feels_like_pants)
                    }
                    SHORTS -> {
                        binding.clothingImageView.tag = getString(R.string.shorts)
                        binding.clothingImageView.setImageResource(R.drawable.shorts)

                        binding.mainButton.text = getString(R.string.too_cold)
                        val snow = ResourcesCompat.getDrawable(resources ,R.drawable.ic_ac_unit, null)
                        binding.mainButton.setCompoundDrawablesWithIntrinsicBounds(snow, null, null, null)
                        binding.mainButton.setBackgroundResource(R.drawable.my_button_blue)

                        binding.shouldWearTextView.text = getString(R.string.feels_like_shorts)
                    }
                    UNKNOWN -> toast("Unidentified state...")
                }
                binding.clothingImageView.invalidate()
                binding.mainButton.invalidate()
                binding.shouldWearTextView.invalidate()
            }
        })

        mainViewModel.cityName.observe(this, {
            when (it) {
                null -> createLocationRequest()
                else -> {
                    binding.cityName.text = it
                    binding.cityName.invalidate()
                }
            }
        })

        mainViewModel.currentTemp.observe(this, {
            it?.let {
                binding.temperatureTextView.text = getString(R.string.celsius, it)
            }
            binding.temperatureTextView.invalidate()
        })

        mainViewModel.highTemp.observe(this, {
            it?.let {
                binding.temperatureHighTextView.text = getString(R.string.celsius, it)
            }
            binding.temperatureHighTextView.invalidate()
        })

        mainViewModel.lowTemp.observe(this, {
            it?.let {
                binding.temperatureLowTextView.text = getString(R.string.celsius, it)
            }
            binding.temperatureLowTextView.invalidate()
        })

        mainViewModel.isNightMode.observe(this, {
            it?.let {
                displayNightMode(it)
            }
        })

        checkInternet(applicationContext)
        createLocationRequest()
        mainViewModel.setupNightMode()
    }

    private fun displayNoInternet() {
        toast(R.string.internet_unavailable)
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, MainViewModel.INITIAL_PERMS, MainViewModel.INITIAL_REQUEST)
    }

    private fun displayNoPermissionsEnabled() {
        longToast(R.string.enable_permission)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MainViewModel.INITIAL_REQUEST) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted.
                checkInternet(applicationContext)
                createLocationRequest()
            } else {
                displayNoPermissionsEnabled()
                requestPermissions()
            }
        }
    }

    private fun isNetworkStatusAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.activeNetworkInfo?.let { return it.isConnected }
        return false
    }

    // TODO: BUG - If user connects after opening the app, we don't respond! They stay disconnected until they restart the app
    private fun checkInternet(context: Context) {
        if (!isNetworkStatusAvailable(context)) {
            displayNoInternet()
        }
    }

    private fun createLocationRequest() {
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                LocationServices
                        .getFusedLocationProviderClient(applicationContext)
                        .removeLocationUpdates(this)

                val city = mainViewModel.getCity(locationResult.lastLocation)
                city?.let { mainViewModel.setCityName(city) }

                when (mainViewModel.shouldFetchWeather()) {
                    true -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            mainViewModel.fetchWeather(locationResult.lastLocation)
                            mainViewModel.writeAndDisplayNewData()
                        }
                    }
                    false -> mainViewModel.loadAndDisplayPreviousData()
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
                    // All location settings are satisfied. The client can initialize location requests here.
                    if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions()
                        }
                    } else {
                        LocationServices
                                .getFusedLocationProviderClient(applicationContext)
                                .requestLocationUpdates(locationRequest, locationCallback, null)
                    }
                }
                .addOnFailureListener { e ->
                    if (e is ResolvableApiException) {
                        try {
                            e.startResolutionForResult(this, MainViewModel.REQUEST_CHECK_SETTINGS)
                        } catch (sendEx: IntentSender.SendIntentException) {
                            Log.e(this.toString(), sendEx.toString())
                        }
                    }
                }
    }


    // TODO: Replace with material theme-ing
    private fun displayNightMode(isNightMode: Boolean) {
        val darkColor = Color.parseColor("#212121")
        val lightColor = Color.parseColor("#FAFAFA")
        when {
            isNightMode -> {
                binding.rootLayout.setBackgroundColor(darkColor)
                binding.cityName.setTextColor(lightColor)
                binding.shouldWearTextView.setTextColor(lightColor)
                binding.nightModeImage.setColorFilter(lightColor)
            }
            else -> {
                binding.rootLayout.setBackgroundColor(lightColor)
                binding.cityName.setTextColor(darkColor)
                binding.shouldWearTextView.setTextColor(darkColor)
                binding.nightModeImage.setColorFilter(darkColor)
            }
        }
        binding.nightModeSwitch.isChecked = isNightMode
    }
}
