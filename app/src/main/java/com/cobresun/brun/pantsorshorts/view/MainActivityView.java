package com.cobresun.brun.pantsorshorts.view;

import android.view.View;

public interface MainActivityView {

    void displayCity(String city);

    void displayTemperature(float temperature);

    void displayYouShouldWearText(int clothing);

    void displayClothingImage(int clothing);

    void displayButton(int clothing);

    void displayNoInternet();

    void requestPermissions();

    void displayNoPermissionsEnabled();

    void updateView();

    void changeTempMode(View view);
}
