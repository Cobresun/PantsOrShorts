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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.cobresun.brun.pantsorshorts.R;
import com.cobresun.brun.pantsorshorts.repositories.UserDataRepository;
import com.cobresun.brun.pantsorshorts.repositories.impl.SharedPrefsUserDataRepository;
import com.cobresun.brun.pantsorshorts.view.MainActivityView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.cobresun.brun.pantsorshorts.repositories.impl.SharedPrefsUserDataRepository.COLD;
import static com.cobresun.brun.pantsorshorts.repositories.impl.SharedPrefsUserDataRepository.HOT;

public class MainActivityPresenter {

    public static final int PANTS = 1;
    public static final int SHORTS = 2;

    public static final String[] INITIAL_PERMS = {Manifest.permission.ACCESS_FINE_LOCATION,};
    public static final int INITIAL_REQUEST = 1337;

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;

    private int currentTemp;
    private int highTemp;
    private int lowTemp;

    private Context mContext;
    private MainActivityView view;
    private UserDataRepository userDataRepository;

    public MainActivityPresenter(MainActivityView view, UserDataRepository userDataRepository, Context context) {
        this.view = view;
        this.userDataRepository = userDataRepository;
        this.mContext = context;
    }

    private void updateUserThreshold(int howTheyFelt, float currentTemp) {
        if (howTheyFelt == COLD) {
            userDataRepository.writeUserThreshold(currentTemp + 1);
        } else if (howTheyFelt == HOT) {
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
        if (pantsOrShorts(currentTemp) == SHORTS) {
            updateUserThreshold(SharedPrefsUserDataRepository.COLD, currentTemp);
        } else {
            updateUserThreshold(SharedPrefsUserDataRepository.HOT, currentTemp);
        }
        updateClothing();
    }

    private void updateClothing(){
        int clothing = pantsOrShorts(currentTemp);
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
                            getWeather(location);
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

    private void getWeather(Location location) {
        long lastFetched = userDataRepository.readLastTimeFetchedWeather();
        final long currentTime = System.currentTimeMillis();
        long diff = currentTime - lastFetched;

        boolean isFirstTime = userDataRepository.isFirstTimeLaunching();
        String apiKey = mContext.getResources().getString(R.string.dark_sky);

        if (diff < MINUTE_MILLIS && !isFirstTime) {
            boolean isCelsius = userDataRepository.isCelsius();

            currentTemp = userDataRepository.readLastFetchedTemp();
            highTemp = userDataRepository.readLastFetchedTempHigh();
            lowTemp = userDataRepository.readLastFetchedTempLow();
            view.displayTemperature(currentTemp, isCelsius);
            view.displayHighTemperature(highTemp, isCelsius);
            view.displayLowTemperature(lowTemp, isCelsius);
            updateClothing();
        }
        else {
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://api.darksky.net/")
                    .build();

            WeatherAPIService service = retrofit.create(WeatherAPIService.class);
            service.getTemp(apiKey, location.getLatitude(), location.getLongitude()).enqueue(new Callback<ForecastResponse>() {
                @Override
                public void onResponse(@NonNull Call<ForecastResponse> call, @NonNull Response<ForecastResponse> response) {
                    assert response.body() != null;
                    currentTemp = round(response.body().currently.temperature);
                    highTemp = round(response.body().daily.data.get(0).temperatureMax);
                    lowTemp = round(response.body().daily.data.get(0).temperatureMin);

                    userDataRepository.writeLastFetchedTemp(currentTemp);
                    userDataRepository.writeLastFetchedTempHigh(highTemp);
                    userDataRepository.writeLastFetchedTempLow(lowTemp);
                    userDataRepository.writeLastTimeFetchedWeather(currentTime);
                    userDataRepository.writeIsCelsius(true);
                    view.displayTemperature(currentTemp, true);
                    view.displayHighTemperature(highTemp, true);
                    view.displayLowTemperature(lowTemp, true);
                    updateClothing();
                }

                @Override
                public void onFailure(@NonNull Call<ForecastResponse> call, @NonNull Throwable t) {
                    Log.d("BNORTAG", String.valueOf(t));
                }
            });
        }
    }

    public void updateTempMode(){
        boolean isCelsius = userDataRepository.isCelsius();
        userDataRepository.writeIsCelsius(!isCelsius);
        view.displayTemperature(currentTemp, !isCelsius);
        view.displayHighTemperature(highTemp, !isCelsius);
        view.displayLowTemperature(lowTemp, !isCelsius);
    }

    public void setupNightMode() {
        boolean isNightMode = userDataRepository.isNightMode();
        userDataRepository.writeNightMode(isNightMode);
        view.displayNightMode(isNightMode);
    }

    public void toggleNightMode() {
        boolean isNightMode = userDataRepository.isNightMode();
        userDataRepository.writeNightMode(!isNightMode);
        view.displayNightMode(!isNightMode);
    }

    private int round(double a) {
        if (a > 0) {
            return (int) (a + 0.5);
        } else if (a < 0) {
            return (int) (a - 0.5);
        }
        return (int) a;
    }
}
