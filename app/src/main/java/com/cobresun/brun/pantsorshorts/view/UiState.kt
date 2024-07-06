package com.cobresun.brun.pantsorshorts.view

import com.cobresun.brun.pantsorshorts.Clothing
import com.cobresun.brun.pantsorshorts.weather.Temperature

sealed class UiState {
    data object Loading : UiState()

    data class Loaded(
        val cityName: String?,
        val temperatures: Temperatures,
        val clothing: Clothing
    ) : UiState()

    data class Temperatures(
        val current: Temperature,
        val high: Temperature,
        val low: Temperature,
    )
}
