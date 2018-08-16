package com.cobresun.brun.pantsorshorts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PANTS = 1;
    private static final int SHORTS = 2;
    private static final int COLD = 3;
    private static final int HOT = 4;

    private Weather weather;
    private float defaultThreshold = 21f;
    private float userThreshold = defaultThreshold;
    private static final String PREFS_NAME = "userPrefs";
    public static Context context;
    public static Activity activity;
    public static FusedLocationProviderClient mFusedLocationClient;
    private String city;

    private static final String[] INITIAL_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION,
    };
    private static final int INITIAL_REQUEST=1337;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        activity = this;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
            }
        }
        else {
            getLocation();
        }

        try {
            weather = new Weather();
            weather.city = city;
            weather.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        changeStatus();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == INITIAL_REQUEST) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted.
                getLocation();
            }
        }

    }

    @SuppressLint("MissingPermission")
    private void getLocation(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            city = getAddress(location.getLatitude(), location.getLongitude());
                            TextView textView = findViewById(R.id.textView);
                            textView.setText("Today in " + city + " you should wear");
                            textView.invalidate();
                        }
                    }
                });
    }


    /**
     *
     * @return float of current temperature
     */
    private float getTemp() {
        return (float) weather.temp;
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

    public String getAddress(double lats, double lons) {

        Geocoder geocoder;
        double lat = lats;
        double lon = lons;
        geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);
        } catch (IOException e) {

            e.printStackTrace();
        }

        if (addresses != null) {

            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();

            return city;
        } else {
            return "failed";
        }
    }
}
