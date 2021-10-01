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
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var connectivityManager: ConnectivityManager
    @Inject lateinit var fusedLocationClient: FusedLocationProviderClient
    @Inject lateinit var locator: Locator

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    private val viewModel: MainViewModel by viewModels()

    private val darkColors = darkColors(
        primary = Color.White,
    )

    private val lightColors = lightColors(
        primary = Color.Black,
    )

    @ExperimentalPermissionsApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colors = if (isSystemInDarkTheme()) darkColors else lightColors
            ) {
                val isLoading by viewModel.isLoading.observeAsState(initial = true)

                val doNotShowRationale by rememberSaveable { mutableStateOf(false) }
                val locationPermissionState = rememberPermissionState(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                PermissionRequired(
                    permissionState = locationPermissionState,
                    permissionNotGrantedContent = {
                        if (doNotShowRationale) {
                            Text(
                                getString(R.string.permission_explanation),
                                color = MaterialTheme.colors.primary
                            )
                        } else {
                            LocationRationale(locationPermissionState)
                        }
                    },
                    permissionNotAvailableContent = {
                        Column (Modifier.padding(64.dp)) {
                            Text(
                                getString(R.string.location_denied),
                                color = MaterialTheme.colors.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                ) {
                    if (isLoading) {
                        LoadingView()
                    } else {
                        val city: String by viewModel.cityName.observeAsState(
                            stringResource(R.string.no_city_found)
                        )
                        val currentTemp: Temperature by viewModel.currentTemp.observeAsState(
                            Temperature(0, TemperatureUnit.CELSIUS)
                        )
                        val highTemp: Temperature by viewModel.highTemp.observeAsState(
                            Temperature(0, TemperatureUnit.CELSIUS)
                        )
                        val lowTemp: Temperature by viewModel.lowTemp.observeAsState(
                            Temperature(0, TemperatureUnit.CELSIUS)
                        )
                        val clothing: Clothing by viewModel.clothingSuggestion.observeAsState(
                            Clothing.PANTS
                        )

                        MainView(city, currentTemp, highTemp, lowTemp, clothing)
                    }
                }
            }
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
            priority = LocationRequest.PRIORITY_LOW_POWER
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            if (
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location : Location? ->
                        // Got last known location. In some rare situations this can be null.
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
                    }
            }
        }
    }

    @Composable
    fun LoadingView() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    @Composable
    fun MainView(
        city: String,
        currentTemp: Temperature,
        highTemp: Temperature,
        lowTemp: Temperature,
        clothing: Clothing
    ) {
        Column(
            Modifier.padding(64.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                City(city)
                CurrentTemp(currentTemp)
                Spacer(modifier = Modifier.height(16.dp))
                HighLowTemp(highTemp, lowTemp)
                Spacer(modifier = Modifier.height(32.dp))
                ClothingSuggestion(clothing)
                Spacer(modifier = Modifier.height(32.dp))
                ClothingImage(clothing)
            }
            MainButton(clothing)
        }
    }

    @Composable
    fun City(
        city: String
    ) {
        Text(
            city,
            fontSize = 30.sp,
            color = MaterialTheme.colors.primary
        )
    }

    @Composable
    fun CurrentTemp(
        currentTemp: Temperature,
    ) {
        Text(
            text = stringResource(
                if (currentTemp.unit == TemperatureUnit.CELSIUS) {
                    R.string.celsius
                } else {
                    R.string.fahrenheit
                },
                currentTemp.value
            ),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary
        )
    }

    @Composable
    fun HighLowTemp(
        highTemp: Temperature,
        lowTemp: Temperature
    ) {
        Row {
            Text(
                text = stringResource(
                    if (lowTemp.unit == TemperatureUnit.CELSIUS) {
                        R.string.celsius
                    } else {
                        R.string.fahrenheit
                    },
                    lowTemp.value
                ),
                fontSize = 20.sp,
                color = Color.Blue
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "/",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(
                    if (highTemp.unit == TemperatureUnit.CELSIUS) {
                        R.string.celsius
                    } else {
                        R.string.fahrenheit
                    },
                    highTemp.value
                ),
                fontSize = 20.sp,
                color = Color.Red
            )
        }
    }

    @Composable
    fun ClothingSuggestion(
        clothing: Clothing
    ) {
        Text(
            text = stringResource(
                if (clothing == Clothing.PANTS) {
                    R.string.feels_like_pants
                } else {
                    R.string.feels_like_shorts
                }
            ),
            color = MaterialTheme.colors.primary
        )
    }

    @Composable
    fun ClothingImage(
        clothing: Clothing
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(
                    if (clothing == Clothing.PANTS) {
                        R.drawable.pants
                    } else {
                        R.drawable.shorts
                    }
                ),
                contentDescription = stringResource(R.string.image_content_desc),
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    @Composable
    fun MainButton(
        clothing: Clothing
    ) {
        Button(
            onClick = {
                viewModel.calibrateThreshold()
                Toast.makeText(
                    applicationContext,
                    R.string.remember_that,
                    Toast.LENGTH_SHORT
                ).show()
            },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (clothing == Clothing.PANTS) Color.Red else Color.Blue,
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(
                    if (clothing == Clothing.PANTS) {
                        R.drawable.ic_wb_sunny
                    } else {
                        R.drawable.ic_ac_unit
                    }
                ),
                contentDescription = stringResource(R.string.button_icon_desc)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(
                    if (clothing == Clothing.PANTS) {
                        R.string.too_hot
                    } else {
                        R.string.too_cold
                    }
                )
            )
        }
    }

    @ExperimentalPermissionsApi
    @Composable
    private fun LocationRationale(
        locationPermissionState: PermissionState
    ) {
        Column(
            modifier = Modifier.padding(64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                getString(R.string.need_permission),
                color = MaterialTheme.colors.primary
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { locationPermissionState.launchPermissionRequest() },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Green,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    getString(R.string.ok),
                )
            }
        }
    }

    @Preview(name = "Main View Light Mode Shorts", showSystemUi = true)
    @Composable
    fun MainViewLightShortsPreview() {
        MaterialTheme(
            colors = lightColors
        ) {
            MainView(
                city = "Calgary",
                currentTemp = Temperature(8, TemperatureUnit.CELSIUS),
                highTemp = Temperature(88, TemperatureUnit.CELSIUS),
                lowTemp = Temperature(-8, TemperatureUnit.CELSIUS),
                clothing = Clothing.SHORTS
            )
        }
    }

    @Preview(name = "Main View Light Mode Pants", showSystemUi = true)
    @Composable
    fun MainViewLightPantsPreview() {
        MaterialTheme(
            colors = lightColors
        ) {
            MainView(
                city = "Calgary",
                currentTemp = Temperature(8, TemperatureUnit.CELSIUS),
                highTemp = Temperature(88, TemperatureUnit.CELSIUS),
                lowTemp = Temperature(-8, TemperatureUnit.CELSIUS),
                clothing = Clothing.PANTS
            )
        }
    }
}
