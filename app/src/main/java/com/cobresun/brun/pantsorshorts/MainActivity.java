/**
 * PANTS OR SHORTS
 *
 * App that informs the user whether or not they should wear pants because it is cold,
 * or shorts if it is hot, depending on the weather of the user's city, based on user preference.
 *
 * Produced by Brian Norman and Sunny Nagam
 * Cobresun - August 2018
 */

package com.cobresun.brun.pantsorshorts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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

/**
 * Main class by which all app functions are run.
 *
 *
 */
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
    private boolean wearingPants = false;

    private static final String[] INITIAL_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION,
    };
    private static final int INITIAL_REQUEST = 1337;

    public static boolean cityFetched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        activity = this;

        if(!isNetworkStatusAvialable (context)) {
            Toast.makeText(getApplicationContext(), "Internet unavialable, please restart.", Toast.LENGTH_SHORT).show();
            TextView textView = findViewById(R.id.textView);
            textView.setText("Internet unavailable, please reconnect and try again.");
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
            }
        }
        else {
            getLocation();
        }
    }

    public static boolean isNetworkStatusAvialable (Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null)
        {
            NetworkInfo netInfos = connectivityManager.getActiveNetworkInfo();
            if(netInfos != null)
                if(netInfos.isConnected())
                    return true;
        }
        return false;
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == INITIAL_REQUEST) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted.
                getLocation();
            }
            else {
                Toast.makeText(context, "This app won't even work if you don't enable permission...", Toast.LENGTH_LONG).show();
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);

            }
        }
    }

    @SuppressLint("MissingPermission")
    public void getLocation(){
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
                            cityFetched = true;
                            textView.invalidate();
                            try {
                                weather = new Weather();
                                weather.lat = (int) location.getLatitude();
                                weather.lon = (int) location.getLongitude();
                                weather.execute().get();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                            changeStatus();
                        }
                    }
                });
    }

    /**
     * Calls the instance of weather and casts the temperature to a float.
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
        System.out.println("BNOR: temp: " + currentTemp);
        if (currentTemp > getUserThreshold())
            return SHORTS;
        else
            return PANTS;
    }

    /**
     *
     * @param howTheyFelt static final int of COLD or HOT which configures their user preferences
     */
    private void updateUserPref(int howTheyFelt) {
        float currentTemp = getTemp();
        if (howTheyFelt == COLD && currentTemp > getUserThreshold()){
            userThreshold = currentTemp + 1;
        }
        else if (howTheyFelt == HOT && currentTemp < getUserThreshold()){
            userThreshold = currentTemp - 1;
        }
        Toast.makeText(getApplicationContext(), "Pants or Shorts will remember that.", Toast.LENGTH_SHORT).show();
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
        Button button = findViewById(R.id.button);

        if (pantsOrShorts() == PANTS){
            img.setTag("pants");
            img.setImageResource(R.drawable.sunnyspants);
            button.setText("It's too hot for pants");
            button.setBackgroundResource(R.drawable.my_button_red);
            Drawable sun = context.getResources().getDrawable(R.drawable.ic_wb_sunny);
            button.setCompoundDrawablesWithIntrinsicBounds(sun, null, null, null);
            wearingPants = true;
        }
        else if (pantsOrShorts() == SHORTS){
            img.setTag("shorts");
            img.setImageResource(R.drawable.sunnysshorts);
            button.setText("It's too cold for shorts");
            Drawable snow = context.getResources().getDrawable(R.drawable.ic_ac_unit);
            button.setCompoundDrawablesWithIntrinsicBounds(snow, null, null, null);
            button.setBackgroundResource(R.drawable.my_button_blue);
            wearingPants = false;
        }
    }

    /**
     * Sets userThreshold based on Hot or Cold user feedback
     */
    public void calibrateThreshold(View view){
        if (!wearingPants){
            updateUserPref(COLD);
        }
        else {
            updateUserPref(HOT);
        }
        changeStatus();
    }

    public String getAddress(double lats, double lons) {
        Geocoder geocoder;
        geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(lats, lons, 1);
        } catch (IOException e) {

            e.printStackTrace();
        }
        if (addresses != null) {
            String city = addresses.get(0).getLocality();
            return city;
        } else {
            return "failed";
        }
    }
}