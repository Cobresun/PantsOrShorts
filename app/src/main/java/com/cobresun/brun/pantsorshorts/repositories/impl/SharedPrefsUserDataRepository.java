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
}
