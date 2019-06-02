package com.cobresun.brun.pantsorshorts.repositories.impl;

import android.content.Context;
import android.content.SharedPreferences;

import com.cobresun.brun.pantsorshorts.repositories.UserDataRepository;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefsUserDataRepository implements UserDataRepository {

    // int HowTheyFelt types
    public static final int COLD = 1;
    public static final int HOT = 2;

    private Context context;
    private static final String PREFS_NAME = "userPrefs";


    public SharedPrefsUserDataRepository(Context context) {
        this.context = context;
    }

    // ***** These functions exist purely to escape a current crash ***** \\

    @Override
    public boolean hasUserUpdated() {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return settings.getBoolean("hasUserUpdated", true);
    }

    @Override
    public void writeHasUserUpdated(boolean userUpdated) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("hasUserUpdated", userUpdated);
        editor.apply();
    }

    @Override
    public void clearUserThreshold() {
        int defaultThreshold = 21;
        SharedPreferences pref = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("userThreshold", defaultThreshold);
        editor.apply();
    }

    // ***** These functions exist purely to escape a current crash ***** \\

    @Override
    public int readUserThreshold() {
        int defaultThreshold = 21;
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return settings.getInt("userThreshold", defaultThreshold);
    }

    @Override
    public void writeUserThreshold(int threshold) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("userThreshold", threshold);
        editor.apply();
    }

    @Override
    public long readLastTimeFetchedWeather() {
        long defaultTime = System.currentTimeMillis();
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return settings.getLong("timeLastFetched", defaultTime);
    }

    @Override
    public void writeLastTimeFetchedWeather(long time) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("timeLastFetched", time);
        editor.apply();
    }

    @Override
    public int readLastFetchedTemp() {
        int defaultTemp = 1000;
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return settings.getInt("tempLastFetched", defaultTemp);
    }

    @Override
    public void writeLastFetchedTemp(int temp) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("tempLastFetched", temp);
        editor.apply();
    }

    @Override
    public int readLastFetchedTempHigh() {
        int defaultTemp = 1000;
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return settings.getInt("tempHighLastFetched", defaultTemp);
    }

    @Override
    public void writeLastFetchedTempHigh(int temp) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("tempHighLastFetched", temp);
        editor.apply();
    }

    @Override
    public int readLastFetchedTempLow() {
        int defaultTemp = 1000;
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return settings.getInt("tempLowLastFetched", defaultTemp);
    }

    @Override
    public void writeLastFetchedTempLow(int temp) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("tempLowLastFetched", temp);
        editor.apply();
    }

    @Override
    public int[] readLastFetchedHourlyTemps() {
        int defaultTemp = 10000;
        int[] temps = new int[24];
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        for (int i = 0; i < temps.length; i++){
            temps[i] = settings.getInt("tempHourlyLastFetched" + i, defaultTemp);
        }
        return temps;
    }

    @Override
    public void writeLastFetchedHourlyTemps(int[] temps) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        for (int i = 0; i < temps.length; i++){
            editor.putInt("tempHourlyLastFetched" + i, temps[i]);
        }
        editor.apply();
    }

    @Override
    public boolean isFirstTimeLaunching() {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
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
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return settings.getBoolean("isNightMode", false);
    }

    @Override
    public void writeNightMode(boolean nightMode) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isNightMode", nightMode);
        editor.apply();
    }

    @Override
    public boolean isCelsius() {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return settings.getBoolean("isCelsius", false);
    }

    @Override
    public void writeIsCelsius(boolean isCelsius) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isCelsius", isCelsius);
        editor.apply();
    }

}
