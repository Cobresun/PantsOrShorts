package com.cobresun.brun.pantsorshorts.repositories.impl;

import android.content.Context;
import android.content.SharedPreferences;

import com.cobresun.brun.pantsorshorts.repositories.UserDataRepository;

public class SharedPrefsUserDataRepository implements UserDataRepository {

    // int HowTheyFelt types
    public static final int COLD = 1;
    public static final int HOT = 2;

    private Context context;
    private static final String PREFS_NAME = "userPrefs";


    public SharedPrefsUserDataRepository(Context context) {
        this.context = context;
    }

    @Override
    public float readUserThreshold() {
        float defaultThreshold = 21f;
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getFloat("userThreshold", defaultThreshold);
    }

    @Override
    public void writeUserThreshold(float threshold) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("userThreshold", threshold);
        editor.apply();
    }

    @Override
    public long readLastTimeFetchedWeather() {
        long defaultTime = System.currentTimeMillis();
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getLong("timeLastFetched", defaultTime);
    }

    @Override
    public void writeLastTimeFetchedWeather(long time) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("timeLastFetched", time);
        editor.apply();
    }

    @Override
    public float readLastFetchedTemp() {
        float defaultTemp = 1000;
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getFloat("tempLastFetched", defaultTemp);
    }

    @Override
    public void writeLastFetchedTemp(float temp) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("tempLastFetched", temp);
        editor.apply();
    }

    @Override
    public boolean isFirstTimeLaunching() {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isFirstTime =  settings.getBoolean("isFirstTime", true);

        if (isFirstTime) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("isFirstTime", false);
            editor.apply();
        }

        return isFirstTime;
    }

    @Override
    public boolean isNightMode() {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getBoolean("isNightMode", false);
    }

    @Override
    public void writeNightMode(boolean nightMode) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isNightMode", nightMode);
        editor.apply();
    }

    @Override
    public boolean isCelsius() {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getBoolean("isCelsius", false);
    }

    @Override
    public void writeIsCelsius(boolean isCelsius) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isCelsius", isCelsius);
        editor.apply();
    }

}
