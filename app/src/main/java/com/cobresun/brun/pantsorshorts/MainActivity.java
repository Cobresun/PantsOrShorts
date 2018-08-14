package com.cobresun.brun.pantsorshorts;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private static final int PANTS = 1;
    private static final int SHORTS = 2;

    private static final int COLD = 3;
    private static final int HOT = 4;
    private float userThreshold = 21f;
    private Weather weather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weather = new Weather();
        weather.getWeatherJSON();
        changeStatus();
    }


    /**
     *
     * @return float of current temperature
     */
    private float getTemp() {
        float temp = (float) weather.temp;    // TODO: hard-code it here for now
        return temp;
    }


    /**
     *
     * @return static final int SHORTS or PANTS
     */
    private int pantsOrShorts() {
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
        // TODO: Eventually save this to phone's hard-drive
    }


    /**
     *
     * @return float current user threshold
     */
    private float getUserThreshold() {
        return userThreshold;
    }

    /**
     * Updates the image to either pants or shorts
     */
    private void changeStatus(){
        ImageView img = findViewById(R.id.imageView);

        if(pantsOrShorts() == PANTS){
            img.setTag("pants");
            img.setImageResource(R.drawable.pants);
        }
        else if(pantsOrShorts() == SHORTS){
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
