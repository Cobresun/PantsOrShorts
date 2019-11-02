package com.cobresun.brun.pantsorshorts

import android.view.View

interface MainActivityView {

    fun displayCity(city: String?)

    fun displayTemperature(temperature: Int, isCelsius: Boolean)

    fun displayHighTemperature(temperature: Int, isCelsius: Boolean)

    fun displayLowTemperature(temperature: Int, isCelsius: Boolean)

    fun displayYouShouldWearText(clothing: Clothing)

    fun displayClothingImage(clothing: Clothing)

    fun displayButton(clothing: Clothing)

    fun displayNoInternet()

    fun requestPermissions()

    fun displayNoPermissionsEnabled()

    fun updateView()

    fun changeTempMode(view: View)

    fun displayNightMode(isNightMode: Boolean)
}
