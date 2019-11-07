package com.cobresun.brun.pantsorshorts

interface MainActivityView {
    fun displayNoInternet()

    fun requestPermissions()

    fun displayNoPermissionsEnabled()

    fun updateView()

    fun displayNightMode(isNightMode: Boolean)
}
