package com.cobresun.brun.pantsorshorts

import android.Manifest
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobresun.brun.pantsorshorts.weather.Clothing
import com.cobresun.brun.pantsorshorts.weather.Temperature
import com.cobresun.brun.pantsorshorts.weather.TemperatureUnit
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.valentinilk.shimmer.shimmer

private val lowBlue = Color(0xff80cee1)
private val highRed = Color(0xffff6961)

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
    mainButtonCallback: () -> Unit,
    toggleTemperatureUnitCallback: () -> Unit
) {
    MaterialTheme(
        colors = if (isSystemInDarkTheme()) darkColors else lightColors
    ) {
        val locationPermissionState = rememberPermissionState(
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        when (locationPermissionState.status) {
            is PermissionStatus.Denied -> {
                LocationPermissionDialog(
                    shouldShowRationale = locationPermissionState.status.shouldShowRationale,
                    launchPermissionRequest = { locationPermissionState.launchPermissionRequest() }
                )
            }
            is PermissionStatus.Granted -> {
                if (isLoading.value) {
//                if (true) {
                    LoadingView()
                } else {
                    MainView(
                        city = cityName.value ?: stringResource(R.string.no_city_found),
                        currentTemp = currentTemp.value ?: Temperature(0, TemperatureUnit.CELSIUS),
                        highTemp = highTemp.value ?: Temperature(0, TemperatureUnit.CELSIUS),
                        lowTemp = lowTemp.value ?: Temperature(0, TemperatureUnit.CELSIUS),
                        clothing = clothing.value ?: Clothing.PANTS,
                        mainButtonCallback = { mainButtonCallback() },
                        toggleTemperatureUnitCallback = { toggleTemperatureUnitCallback() }
                    )
                }
            }
        }
    }
}

@Composable
fun LocationPermissionDialog(shouldShowRationale: Boolean, launchPermissionRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* Can't be dismissed */ },
        confirmButton = {
            if (!shouldShowRationale) {
                Button(onClick = { launchPermissionRequest() }) {
                    Text(
                        text = stringResource(id = R.string.accept),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        title = {
            Text(
                text = stringResource(R.string.location_request),
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.need_permission),
                fontWeight = FontWeight.Medium
            )
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun LoadingView() {
    val grayRoundedBoxModifier = Modifier.background(Color.Gray, RoundedCornerShape(16.dp))

    Column(
        Modifier
            .padding(64.dp)
            .shimmer()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Box(modifier = grayRoundedBoxModifier.size(width = 256.dp, height = 36.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = grayRoundedBoxModifier.size(width = 128.dp, height = 36.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = grayRoundedBoxModifier.size(width = 200.dp, height = 36.dp))
            Spacer(modifier = Modifier.height(64.dp))
            Box(modifier = grayRoundedBoxModifier.size(256.dp))
        }
        Box(modifier = grayRoundedBoxModifier.size(width = 256.dp, height = 64.dp))
    }
}

@Composable
fun MainView(
    city: String,
    currentTemp: Temperature,
    highTemp: Temperature,
    lowTemp: Temperature,
    clothing: Clothing,
    mainButtonCallback: () -> Unit,
    toggleTemperatureUnitCallback: () -> Unit
) {
    Column(
        Modifier.padding(64.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            City(city)
            CurrentTemp(currentTemp) { toggleTemperatureUnitCallback() }
            Spacer(modifier = Modifier.height(16.dp))
            HighLowTemp(highTemp, lowTemp) { toggleTemperatureUnitCallback() }
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
    toggleTemperatureUnitCallback: () -> Unit
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
        modifier = Modifier.clickable { toggleTemperatureUnitCallback() },
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colors.primary
    )
}

@Composable
fun HighLowTemp(
    highTemp: Temperature,
    lowTemp: Temperature,
    toggleTemperatureUnitCallback: () -> Unit
) {
    Row(
        modifier = Modifier.clickable { toggleTemperatureUnitCallback() }
    ) {
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ClothingSuggestion(
    clothing: Clothing
) {
    AnimatedContent(targetState = clothing) { targetClothing ->
        Text(
            text = stringResource(
                if (targetClothing == Clothing.PANTS) R.string.feels_like_pants else R.string.feels_like_shorts
            ),
            color = MaterialTheme.colors.primary
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ClothingImage(
    clothing: Clothing
) {
    AnimatedContent(targetState = clothing) { targetClothing ->
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(
                    if (targetClothing == Clothing.PANTS) R.drawable.pants else R.drawable.shorts
                ),
                contentDescription = stringResource(R.string.image_content_desc),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainButton(
    clothing: Clothing,
    mainButtonCallback: () -> Unit
) {
    AnimatedContent(targetState = clothing) { targetClothing ->
        Button(
            onClick = {
                mainButtonCallback()
                // TODO: Move this text to be a popup message that doesn't cover up content
//                Toast.makeText(
//                    context,
//                    R.string.remember_that,
//                    Toast.LENGTH_SHORT
//                ).show()
            },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (targetClothing == Clothing.PANTS) highRed else lowBlue,
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(
                    if (targetClothing == Clothing.PANTS) {
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
                    if (targetClothing == Clothing.PANTS) {
                        R.string.too_hot
                    } else {
                        R.string.too_cold
                    }
                )
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun PermissionDialogShouldShowRationale() {
    MaterialTheme(
        colors = if (isSystemInDarkTheme()) darkColors else lightColors
    ) {
        LocationPermissionDialog(
            shouldShowRationale = true
        ) { }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionDialogShouldNotShowRationale() {
    MaterialTheme(
        colors = if (isSystemInDarkTheme()) darkColors else lightColors
    ) {
        LocationPermissionDialog(
            shouldShowRationale = false
        ) { }
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
            clothing = Clothing.SHORTS,
            mainButtonCallback = { },
            toggleTemperatureUnitCallback = {}
        )
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
            clothing = Clothing.SHORTS,
            mainButtonCallback = { },
            toggleTemperatureUnitCallback = {}
        )
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
            clothing = Clothing.PANTS,
            mainButtonCallback = { },
            toggleTemperatureUnitCallback = {}
        )
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
            clothing = Clothing.PANTS,
            mainButtonCallback = { },
            toggleTemperatureUnitCallback = {}
        )
    }
}
