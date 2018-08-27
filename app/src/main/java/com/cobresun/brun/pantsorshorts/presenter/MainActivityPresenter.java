package com.cobresun.brun.pantsorshorts.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;

import com.cobresun.brun.pantsorshorts.R;
import com.cobresun.brun.pantsorshorts.Weather;
import com.cobresun.brun.pantsorshorts.repositories.UserDataRepository;
import com.cobresun.brun.pantsorshorts.repositories.impl.SharedPrefsUserDataRepository;
import com.cobresun.brun.pantsorshorts.view.MainActivityView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static com.cobresun.brun.pantsorshorts.repositories.impl.SharedPrefsUserDataRepository.COLD;
import static com.cobresun.brun.pantsorshorts.repositories.impl.SharedPrefsUserDataRepository.HOT;

public class MainActivityPresenter {

    public static final int PANTS = 1;
    public static final int SHORTS = 2;

    public static final String[] INITIAL_PERMS = {Manifest.permission.ACCESS_FINE_LOCATION,};
    public static final int INITIAL_REQUEST = 1337;

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    private float currentTemp;
    private boolean wearingPants;
    private Context mContext;
    private MainActivityView view;
    private UserDataRepository userDataRepository;

    public MainActivityPresenter(MainActivityView view, UserDataRepository userDataRepository, Context context) {
        this.view = view;
        this.userDataRepository = userDataRepository;
        this.mContext = context;
    }

    public void loadUserThreshold() {
        float userThreshold = userDataRepository.readUserThreshold();
        view.displayUserThreshold(userThreshold);
    }

    private void updateUserThreshold(int howTheyFelt, float currentTemp) {
        if (howTheyFelt == COLD && currentTemp > userDataRepository.readUserThreshold()) {
            userDataRepository.writeUserThreshold(currentTemp + 1);
        } else if (howTheyFelt == HOT && currentTemp < userDataRepository.readUserThreshold()) {
            userDataRepository.writeUserThreshold(currentTemp - 1);
        }
    }

    private int pantsOrShorts(float currentTemp) {
        if (currentTemp > userDataRepository.readUserThreshold())
            return SHORTS;
        else
            return PANTS;
    }

    public void calibrateThreshold() {
        if (!wearingPants) {
            updateUserThreshold(SharedPrefsUserDataRepository.COLD, currentTemp);
        } else {
            updateUserThreshold(SharedPrefsUserDataRepository.HOT, currentTemp);
        }
        updateClothing();
    }

    public void updateClothing(){
        changeClothingInView(pantsOrShorts(currentTemp));
    }

    private void changeClothingInView(int clothing) {
        wearingPants = clothing == PANTS;
        view.displayClothingImage(clothing);
        view.displayButton(clothing);
        view.displayYouShouldWearText(clothing);
    }

    public void getLocation(Activity activity) {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            view.requestPermissions();
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            String city = getCity(location.getLatitude(), location.getLongitude());
                            view.displayCity(city);
                            try {
                                getWeather(location);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    private String getCity(double lats, double lons) {
        Geocoder geocoder;
        geocoder = new Geocoder(mContext, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(lats, lons, 1);
        } catch (IOException e) {

            e.printStackTrace();
        }
        if (addresses != null) {
            return addresses.get(0).getLocality();
        } else {
            return "failed";
        }
    }

    public void checkInternet() {
        if (!isNetworkStatusAvialable (mContext)) {
            view.displayNoInternet();
        }
    }

    private static boolean isNetworkStatusAvialable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfos = connectivityManager.getActiveNetworkInfo();
            if (netInfos != null)
                return netInfos.isConnected();
        }
        return false;
    }

    private void getWeather(Location location) throws ExecutionException, InterruptedException {
        long lastFetched = userDataRepository.readLastTimeFetchedWeather();
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - lastFetched;

        boolean isFirstTime = userDataRepository.isFirstTimeLaunching();

        if (diff < MINUTE_MILLIS && !isFirstTime) {
            currentTemp = userDataRepository.readLastFetchedTemp();
            view.displayTemperature(currentTemp);
        }
        else {
            String apiKey = mContext.getResources().getString(R.string.open_weather_map);
            Weather weather = new Weather(apiKey);
            weather.lat = (int) location.getLatitude();
            weather.lon = (int) location.getLongitude();
            weather.execute().get();
            currentTemp = weather.temp;
            userDataRepository.writeLastFetchedTemp(currentTemp);
            userDataRepository.writeLastTimeFetchedWeather(currentTime);
            view.displayTemperature(currentTemp);
        }

    }
}
