package com.cobresun.brun.pantsorshorts;

import android.view.View;

public interface MainActivityView {

    void displayCity(String city);

    void displayTemperature(int temperature, boolean isCelsius);

    void displayHighTemperature(int temperature, boolean isCelsius);

    void displayLowTemperature(int temperature, boolean isCelsius);

    void displayYouShouldWearText(int clothing);

    void displayClothingImage(int clothing);

    void displayButton(int clothing);

    void displayNoInternet();

    void requestPermissions();

    void displayNoPermissionsEnabled();

    void updateView();

    void changeTempMode(View view);

    void displayNightMode(boolean isNightMode);
}
