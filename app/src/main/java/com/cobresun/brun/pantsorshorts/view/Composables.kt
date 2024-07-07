package com.cobresun.brun.pantsorshorts.view

import android.Manifest
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobresun.brun.pantsorshorts.Clothing
import com.cobresun.brun.pantsorshorts.R
import com.cobresun.brun.pantsorshorts.weather.Temperature
import com.cobresun.brun.pantsorshorts.weather.TemperatureUnit
import com.cobresun.brun.pantsorshorts.weather.toFahrenheit
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.valentinilk.shimmer.shimmer

private val lowBlue = Color(0xff80cee1)
private val highRed = Color(0xffff6961)

val AppLightColorScheme = lightColorScheme(
    primary = Color.Black
)

val AppDarkColorScheme = darkColorScheme(
    primary = Color.White
)

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun EntryView(
    darkTheme: Boolean = isSystemInDarkTheme(),
    uiState: UiState,
    calibrateThresholdCallback: (Clothing) -> Unit,
    toggleTemperatureUnitCallback: () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) AppDarkColorScheme else AppLightColorScheme
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
                when (uiState) {
                    is UiState.Loading -> LoadingView()

                    is UiState.Loaded -> MainView(
                        city = uiState.cityName ?: stringResource(R.string.no_city_found),
                        temperatures = uiState.temperatures,
                        clothing = uiState.clothing,
                        calibrateThresholdCallback = { calibrateThresholdCallback(uiState.clothing) },
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
    temperatures: UiState.Temperatures,
    clothing: Clothing,
    calibrateThresholdCallback: () -> Unit,
    toggleTemperatureUnitCallback: () -> Unit
) {
    Column(
        Modifier.padding(64.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            City(city)
            CurrentTemp(temperatures.current) { toggleTemperatureUnitCallback() }
            Spacer(modifier = Modifier.height(16.dp))
            HighLowTemp(temperatures.high, temperatures.low) { toggleTemperatureUnitCallback() }
            Spacer(modifier = Modifier.height(32.dp))
            ClothingSuggestion(clothing)
            Spacer(modifier = Modifier.height(32.dp))
            ClothingImage(clothing)
        }
        MainButton(
            clothing = clothing,
            calibrateThresholdCallback = { calibrateThresholdCallback() })
    }
}

@Composable
fun City(
    city: String
) {
    Text(
        city,
        fontSize = 30.sp,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun CurrentTemp(
    currentTemp: Temperature,
    toggleTemperatureUnitCallback: () -> Unit
) {
    AnimatedContent(targetState = currentTemp.unit, label = "Current temp") {
        Text(
            text = if (it == TemperatureUnit.CELSIUS) {
                stringResource(R.string.celsius, currentTemp.value)
            } else {
                stringResource(R.string.fahrenheit, currentTemp.value.toFahrenheit())
            },
            modifier = Modifier.clickable { toggleTemperatureUnitCallback() },
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
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
        AnimatedContent(targetState = lowTemp.unit, label = "Low temp") {
            Text(
                text = if (it == TemperatureUnit.CELSIUS) {
                    stringResource(R.string.celsius, lowTemp.value)
                } else {
                    stringResource(R.string.fahrenheit, lowTemp.value.toFahrenheit())
                },
                fontSize = 20.sp,
                color = lowBlue
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "/",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        AnimatedContent(targetState = highTemp.unit, label = "High temp") {
            Text(
                text = if (it == TemperatureUnit.CELSIUS) {
                    stringResource(R.string.celsius, highTemp.value)
                } else {
                    stringResource(R.string.fahrenheit, highTemp.value.toFahrenheit())
                },
                fontSize = 20.sp,
                color = highRed
            )
        }
    }
}

@Composable
fun ClothingSuggestion(
    clothing: Clothing
) {
    AnimatedContent(targetState = clothing, label = "Clothing suggestion") { targetClothing ->
        Text(
            text = stringResource(
                if (targetClothing == Clothing.PANTS) R.string.feels_like_pants else R.string.feels_like_shorts
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ClothingImage(
    clothing: Clothing
) {
    AnimatedContent(targetState = clothing, label = "Clothing image") { targetClothing ->
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

@Composable
fun MainButton(
    clothing: Clothing,
    calibrateThresholdCallback: () -> Unit
) {
    AnimatedContent(targetState = clothing, label = "Main button") { targetClothing ->
        Button(
            onClick = { calibrateThresholdCallback() },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (targetClothing == Clothing.PANTS) highRed else lowBlue,
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
        colorScheme = if (isSystemInDarkTheme()) AppDarkColorScheme else AppLightColorScheme
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
        colorScheme = if (isSystemInDarkTheme()) AppDarkColorScheme else AppLightColorScheme
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
        colorScheme = if (isSystemInDarkTheme()) AppDarkColorScheme else AppLightColorScheme
    ) {
        LoadingView()
    }
}

@Preview(showBackground = true)
@Composable
fun MainViewHot() {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) AppDarkColorScheme else AppLightColorScheme
    ) {
        MainView(
            city = "Calgary",
            temperatures = UiState.Temperatures(
                current = Temperature(8, TemperatureUnit.CELSIUS),
                high = Temperature(12, TemperatureUnit.CELSIUS),
                low = Temperature(-3, TemperatureUnit.CELSIUS)
            ),
            clothing = Clothing.SHORTS,
            calibrateThresholdCallback = { },
            toggleTemperatureUnitCallback = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainViewHotNight() {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) AppDarkColorScheme else AppLightColorScheme
    ) {
        MainView(
            city = "Calgary",
            temperatures = UiState.Temperatures(
                current = Temperature(8, TemperatureUnit.CELSIUS),
                high = Temperature(12, TemperatureUnit.CELSIUS),
                low = Temperature(-3, TemperatureUnit.CELSIUS)
            ),
            clothing = Clothing.SHORTS,
            calibrateThresholdCallback = { },
            toggleTemperatureUnitCallback = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainViewCold() {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) AppDarkColorScheme else AppLightColorScheme
    ) {
        MainView(
            city = "Calgary",
            temperatures = UiState.Temperatures(
                current = Temperature(8, TemperatureUnit.CELSIUS),
                high = Temperature(12, TemperatureUnit.CELSIUS),
                low = Temperature(-3, TemperatureUnit.CELSIUS)
            ),
            clothing = Clothing.PANTS,
            calibrateThresholdCallback = { },
            toggleTemperatureUnitCallback = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainViewColdNight() {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) AppDarkColorScheme else AppLightColorScheme
    ) {
        MainView(
            city = "Calgary",
            temperatures = UiState.Temperatures(
                current = Temperature(8, TemperatureUnit.CELSIUS),
                high = Temperature(12, TemperatureUnit.CELSIUS),
                low = Temperature(-3, TemperatureUnit.CELSIUS)
            ),
            clothing = Clothing.PANTS,
            calibrateThresholdCallback = { },
            toggleTemperatureUnitCallback = {}
        )
    }
}
