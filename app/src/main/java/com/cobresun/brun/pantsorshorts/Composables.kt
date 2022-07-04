package com.cobresun.brun.pantsorshorts

import android.Manifest
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.*

private val lowBlue = Color(0xff80cee1)
private val highRed = Color(0xffff6961)
private val pastelGreen = Color(0xff77dd77)

private val darkColors = darkColors(
    primary = Color.White,
)

private val lightColors = lightColors(
    primary = Color.Black,
)

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun EntryView(
    isLoading: State<Boolean>,
    cityName: State<String?>,
    currentTemp: State<Temperature?>,
    highTemp: State<Temperature?>,
    lowTemp: State<Temperature?>,
    clothing: State<Clothing?>,
    mainButtonCallback: () -> Unit
) {
    MaterialTheme(
        colors = if (isSystemInDarkTheme()) darkColors else lightColors
    ) {
        val locationPermissionState = rememberPermissionState(
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        when (locationPermissionState.status) {
            is PermissionStatus.Denied -> {
                if (!locationPermissionState.status.shouldShowRationale) {
                    Text(
                        stringResource(id = R.string.permission_explanation),
                        color = MaterialTheme.colors.primary
                    )
                } else {
                    LocationRationale(locationPermissionState)
                }
            }
            is PermissionStatus.Granted -> {
                if (isLoading.value) {
                    LoadingView()
                } else {
                    MainView(
                        city = cityName.value ?: stringResource(R.string.no_city_found),
                        currentTemp = currentTemp.value ?: Temperature(0, TemperatureUnit.CELSIUS),
                        highTemp = highTemp.value ?: Temperature(0, TemperatureUnit.CELSIUS),
                        lowTemp = lowTemp.value ?: Temperature(0, TemperatureUnit.CELSIUS),
                        clothing = clothing.value ?: Clothing.PANTS,
                        mainButtonCallback = { mainButtonCallback() }
                    )
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
    clothing: Clothing,
    mainButtonCallback: () -> Unit
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
        MainButton(clothing = clothing, mainButtonCallback = { mainButtonCallback() })
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
            color = lowBlue
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
            color = highRed
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
    clothing: Clothing,
    mainButtonCallback: () -> Unit
) {
    val context = LocalContext.current
    Button(
        onClick = {
            mainButtonCallback()
            Toast.makeText(
                context,
                R.string.remember_that,
                Toast.LENGTH_SHORT
            ).show()
        },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (clothing == Clothing.PANTS) highRed else lowBlue,
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
fun LocationRationale(
    locationPermissionState: PermissionState
) {
    Column(
        modifier = Modifier.padding(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.need_permission),
            color = MaterialTheme.colors.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { locationPermissionState.launchPermissionRequest() },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = pastelGreen,
                contentColor = Color.Black
            )
        ) {
            Text(
                stringResource(id = R.string.ok),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Loading() {
    MaterialTheme(
        colors = if (isSystemInDarkTheme()) darkColors else lightColors
    ) {
        LoadingView()
    }
}

@Preview(showBackground = true)
@Composable
fun MainViewHot() {
    MaterialTheme(
        colors = if (isSystemInDarkTheme()) darkColors else lightColors
    ) {
        MainView(
            city = "Calgary",
            currentTemp = Temperature(8, TemperatureUnit.CELSIUS),
            highTemp = Temperature(12, TemperatureUnit.CELSIUS),
            lowTemp = Temperature(-3, TemperatureUnit.CELSIUS),
            clothing = Clothing.SHORTS
        ) { }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainViewHotNight() {
    MaterialTheme(
        colors = if (isSystemInDarkTheme()) darkColors else lightColors
    ) {
        MainView(
            city = "Calgary",
            currentTemp = Temperature(8, TemperatureUnit.CELSIUS),
            highTemp = Temperature(12, TemperatureUnit.CELSIUS),
            lowTemp = Temperature(-3, TemperatureUnit.CELSIUS),
            clothing = Clothing.SHORTS
        ) { }
    }
}

@Preview(showBackground = true)
@Composable
fun MainViewCold() {
    MaterialTheme(
        colors = if (isSystemInDarkTheme()) darkColors else lightColors
    ) {
        MainView(
            city = "Calgary",
            currentTemp = Temperature(8, TemperatureUnit.CELSIUS),
            highTemp = Temperature(12, TemperatureUnit.CELSIUS),
            lowTemp = Temperature(-3, TemperatureUnit.CELSIUS),
            clothing = Clothing.PANTS
        ) { }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainViewColdNight() {
    MaterialTheme(
        colors = if (isSystemInDarkTheme()) darkColors else lightColors
    ) {
        MainView(
            city = "Calgary",
            currentTemp = Temperature(8, TemperatureUnit.CELSIUS),
            highTemp = Temperature(12, TemperatureUnit.CELSIUS),
            lowTemp = Temperature(-3, TemperatureUnit.CELSIUS),
            clothing = Clothing.PANTS
        ) { }
    }
}
