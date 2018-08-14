package com.cobresun.brun.pantsorshorts;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final int PANTS = 1;
    private static final int SHORTS = 2;

    private static final int COLD = 3;
    private static final int HOT = 4;
    private Weather weather;
    private float defaultThreshold = 21f;
    private float userThreshold = defaultThreshold;
    private static final String PREFS_NAME = "userPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        changeStatus();
        System.out.println("BNOR: " + "At the beginning of program file says threshold is: " + getUserThreshold());
    }


    /**
     *
     * @return float of current temperature
     */
    private float getTemp() {
        try {
            weather = new Weather();    // TODO: hard-code it here for now
            weather.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return (float) weather.temp;
    }


    /**
     *
     * @return static final int SHORTS or PANTS
     */
    private int pantsOrShorts() {
        System.out.println("BNOR: temp in pantsOrShorts: " + getTemp());
        float currentTemp = getTemp();
        if (currentTemp > getUserThreshold()){
            return SHORTS;
        }
        else {
            return PANTS;
        }
    }

    /**
     *
     * @param howTheyFelt static final int of COLD or HOT which configures their user preferences
     */
    private void updateUserPref(int howTheyFelt) {
        float currentTemp = getTemp();
        if (howTheyFelt == COLD && currentTemp > getUserThreshold()){
            userThreshold = currentTemp;
        }
        else if (howTheyFelt == HOT && currentTemp < getUserThreshold()){
            userThreshold = currentTemp;
        }
        updateUserPrefFile(userThreshold);
    }

    /**
     *
     * @param userThres
     */
    private void updateUserPrefFile(float userThres) {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putFloat("userThreshold", userThres);
        editor.apply();
        
        System.out.println("BNOR: " + "Writing: " + userThres);
    }


    /**
     *
     * @return float current user threshold
     */
    private float getUserThreshold() {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        System.out.println("BNOR: " + "Reading: " + settings.getFloat("userThreshold", defaultThreshold));
        return settings.getFloat("userThreshold", defaultThreshold);
    }

    /**
     * Updates the image to either pants or shorts
     */
    private void changeStatus(){
        ImageView img = findViewById(R.id.imageView);

        if (pantsOrShorts() == PANTS){
            img.setTag("pants");
            img.setImageResource(R.drawable.pants);
        }
        else if (pantsOrShorts() == SHORTS){
            img.setTag("shorts");
            img.setImageResource(R.drawable.shorts);
        }
    }

    /**
     * Sets userThreshold based on Hot or Cold user feedback
     */
    public void calibrateThreshold(View view){
        int id = view.getId();
        
        if(id == R.id.buttonCold){
            updateUserPref(COLD);
        }
        else if(id == R.id.buttonHot){
            updateUserPref(HOT);
        }
    }
}
