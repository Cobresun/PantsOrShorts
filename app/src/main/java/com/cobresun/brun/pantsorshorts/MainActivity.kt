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
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.cobresun.brun.pantsorshorts.Clothing.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    private val presenter: MainActivityPresenter by lazy {
        MainActivityPresenter(SharedPrefsUserDataRepository(applicationContext), applicationContext)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Objects.requireNonNull<ActionBar>(supportActionBar).hide()
        setContentView(R.layout.activity_main)

        nightModeSwitch.setOnCheckedChangeListener { buttonView, _ ->
            if (buttonView.isPressed) {
                presenter.toggleNightMode()
            }
        }

        mainButton.setOnClickListener {
            presenter.calibrateThreshold()
            Toast.makeText(applicationContext, getString(R.string.remember_that), Toast.LENGTH_SHORT).show()
        }

        presenter.clothingSuggestion.observe(this, androidx.lifecycle.Observer {
            it?.let {
                when (it) {
                    PANTS -> {
                        clothingImageView.tag = getString(R.string.pants)
                        clothingImageView.setImageResource(R.drawable.pants)

                        mainButton.text = getString(R.string.too_hot)
                        mainButton.setBackgroundResource(R.drawable.my_button_red)
                        val sun = applicationContext.resources.getDrawable(R.drawable.ic_wb_sunny, null)
                        mainButton.setCompoundDrawablesWithIntrinsicBounds(sun, null, null, null)

                        shouldWearTextView.text = getString(R.string.feels_like_pants)
                    }
                    SHORTS -> {
                        clothingImageView.tag = getString(R.string.shorts)
                        clothingImageView.setImageResource(R.drawable.shorts)

                        mainButton.text = getString(R.string.too_cold)
                        val snow = applicationContext.resources.getDrawable(R.drawable.ic_ac_unit, null)
                        mainButton.setCompoundDrawablesWithIntrinsicBounds(snow, null, null, null)
                        mainButton.setBackgroundResource(R.drawable.my_button_blue)

                        shouldWearTextView.text = getString(R.string.feels_like_shorts)
                    }
                    UNKNOWN -> TODO()
                }
                clothingImageView.invalidate()
                mainButton.invalidate()
                shouldWearTextView.invalidate()
            }
        })

        presenter.cityName.observe(this, androidx.lifecycle.Observer {
            when (it) {
                null -> createLocationRequest()
                else -> {
                    city_name.text = it
                    city_name.invalidate()
                }
            }
        })

        presenter.currentTemp.observe(this, androidx.lifecycle.Observer {
            it?.let {
                temperatureTextView.text = "$it\u00B0C"
            }
            temperatureTextView.invalidate()
        })

        presenter.highTemp.observe(this, androidx.lifecycle.Observer {
            it?.let {
                temperatureHighTextView.text = "$it\u00B0C"
            }
            temperatureHighTextView.invalidate()
        })

        presenter.lowTemp.observe(this, androidx.lifecycle.Observer {
            it?.let {
                temperatureLowTextView.text = "$it\u00B0C"
            }
            temperatureLowTextView.invalidate()
        })

        presenter.isNightMode.observe(this, androidx.lifecycle.Observer {
            it?.let {
                displayNightMode(it)
            }
        })

        checkInternet(applicationContext)
        createLocationRequest()
        presenter.setupNightMode()
    }

    private fun displayNoInternet() {
        Toast.makeText(applicationContext, getString(R.string.internet_unavailable), Toast.LENGTH_SHORT).show()
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, MainActivityPresenter.INITIAL_PERMS, MainActivityPresenter.INITIAL_REQUEST)
    }

    private fun displayNoPermissionsEnabled() {
        Toast.makeText(applicationContext, getString(R.string.enable_permission), Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == MainActivityPresenter.INITIAL_REQUEST) {
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

    // TODO: Where is the right place for this?
    private fun createLocationRequest() {
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                LocationServices
                        .getFusedLocationProviderClient(applicationContext)
                        .removeLocationUpdates(this)

                val city = presenter.getCity(locationResult.lastLocation)
                city?.let { presenter.setCityName(city) }


                when (presenter.shouldFetchWeather()) {
                    true -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            presenter.fetchWeather(locationResult.lastLocation)
                            presenter.writeAndDisplayNewData()
                        }
                    }
                    false -> presenter.loadAndDisplayPreviousData()
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
                    }
                    else {
                        LocationServices
                                .getFusedLocationProviderClient(applicationContext)
                                .requestLocationUpdates(locationRequest, locationCallback, null)
                    }
                }
                .addOnFailureListener { e ->
                    if (e is ResolvableApiException) {
                        try {
                            e.startResolutionForResult(this, MainActivityPresenter.REQUEST_CHECK_SETTINGS)
                        } catch (sendEx: IntentSender.SendIntentException) {
                            Log.e(this.toString(), sendEx.toString())
                        }
                    }
                }
    }


    // TODO: Replace with material theming
    private fun displayNightMode(isNightMode: Boolean) {
        val darkColor = Color.parseColor("#212121")
        val lightColor = Color.parseColor("#FAFAFA")
        when {
            isNightMode -> {
                rootLayout.setBackgroundColor(darkColor)
                city_name.setTextColor(lightColor)
                shouldWearTextView.setTextColor(lightColor)
                nightModeImage.setColorFilter(lightColor)
            }
            else -> {
                rootLayout.setBackgroundColor(lightColor)
                city_name.setTextColor(darkColor)
                shouldWearTextView.setTextColor(darkColor)
                nightModeImage.setColorFilter(darkColor)
            }
        }
        nightModeSwitch.isChecked = isNightMode
    }
}
