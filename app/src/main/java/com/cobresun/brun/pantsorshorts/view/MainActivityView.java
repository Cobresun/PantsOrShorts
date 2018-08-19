package com.cobresun.brun.pantsorshorts.view;

public interface MainActivityView {

    void displayUserThreshold(float userThreshold);

    void displayCity(String city);

    void displayTemperature(float temperature);

    void displayYouShouldWearText(int clothing);

    void displayClothingImage(int clothing);

    void displayButton(int clothing);

    void displayNoInternet();

    void requestPermissions();

    void displayNoPermissionsEnabled();
}
