package com.cobresun.brun.pantsorshorts.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
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
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.Calendar;
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

    public static final int HOURS_SPENT_OUT = 4;
    public static final int AVERAGE_HOME_TIME = 18;

    public static final String[] INITIAL_PERMS = {Manifest.permission.ACCESS_FINE_LOCATION,};
    public static final int INITIAL_REQUEST = 1337;

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;

    private int currentTemp;
    private int highTemp;
    private int lowTemp;

    private int clothingSuggestion;

    private boolean weatherCallInProgress;

    private int[] hourlyTemps = new int[24];

    private Context mContext;
    private MainActivityView view;
    private UserDataRepository userDataRepository;
    static FusedLocationProviderClient fusedLocationProviderClient;

    public MainActivityPresenter(MainActivityView view, UserDataRepository userDataRepository, Context context) {
        this.view = view;
        this.userDataRepository = userDataRepository;
        this.mContext = context;
    }

    private void updateUserThreshold(int howTheyFelt) {
        if (howTheyFelt == COLD) {
            int curPreference = userDataRepository.readUserThreshold();
            while (pantsOrShorts(curPreference) == SHORTS){
                curPreference++;
            }
            userDataRepository.writeUserThreshold(curPreference);
        } else if (howTheyFelt == HOT) {
            int curPreference = userDataRepository.readUserThreshold();
            while (pantsOrShorts(curPreference) == PANTS){
                curPreference--;
            }
            userDataRepository.writeUserThreshold(curPreference);
        }
    }

    private int pantsOrShorts(int preference) {
        int curTime = getHour();
        int average = 0;

        int hoursToInclude = Math.max(HOURS_SPENT_OUT, AVERAGE_HOME_TIME - curTime);

        for (int i = 0; i < hoursToInclude; i++){
            if (hourlyTemps[i] >= preference) {
                average++;
            } else {
                average--;
            }
        }

        if (average >= 0){
            return SHORTS;
        } else {
            return PANTS;
        }
    }

    private int getHour() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.HOUR_OF_DAY);
    }

    public void calibrateThreshold() {
        if (clothingSuggestion == SHORTS) {
            updateUserThreshold(SharedPrefsUserDataRepository.COLD);
        } else {
            updateUserThreshold(SharedPrefsUserDataRepository.HOT);
        }
        updateClothing();
    }

    private void updateClothing(){
        int clothing = pantsOrShorts(userDataRepository.readUserThreshold());
        clothingSuggestion = clothing;
        view.displayClothingImage(clothing);
        view.displayButton(clothing);
        view.displayYouShouldWearText(clothing);
    }

    public void createLocationRequest(final Activity activity) {
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Log.d("BNORTAG", "Successfully got location");
                        String city = getCity(location.getLatitude(), location.getLongitude());
                        view.displayCity(city);
                        getWeather(location);
                    }
                    else {
                        Log.d("BNORTAG", "Location fetch failed!");
                    }
                }
            };
        };
        final int REQUEST_CHECK_SETTINGS = 8888;
        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(mContext);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        final LocationCallback finalLocationCallback = locationCallback;
        task.addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize location requests here.
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    view.requestPermissions();
                }
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, finalLocationCallback, null);
            }
        });
        task.addOnFailureListener(activity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
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

    private void getWeather(Location location) {
        if (weatherCallInProgress){
            return;
        }
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
            hourlyTemps = userDataRepository.readLastFetchedHourlyTemps();

            view.displayTemperature(currentTemp, isCelsius);
            view.displayHighTemperature(highTemp, isCelsius);
            view.displayLowTemperature(lowTemp, isCelsius);
            updateClothing();
        }
        else {
            weatherCallInProgress = true;
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://api.darksky.net/")
                    .build();

            WeatherAPIService service = retrofit.create(WeatherAPIService.class);
            service.getTemp(apiKey, location.getLatitude(), location.getLongitude()).enqueue(new Callback<ForecastResponse>() {
                @Override
                public void onResponse(@NonNull Call<ForecastResponse> call, @NonNull Response<ForecastResponse> response) {
                    assert response.body() != null;
                    currentTemp = round(response.body().currently.apparentTemperature);
                    highTemp = round(response.body().daily.data.get(0).apparentTemperatureMax);
                    lowTemp = round(response.body().daily.data.get(0).apparentTemperatureMin);

                    for (int i = 0; i < hourlyTemps.length; i++){
                        hourlyTemps[i] = round(response.body().hourly.data.get(i).apparentTemperature);
                    }

                    userDataRepository.writeLastFetchedTemp(currentTemp);
                    userDataRepository.writeLastFetchedTempHigh(highTemp);
                    userDataRepository.writeLastFetchedTempLow(lowTemp);
                    userDataRepository.writeLastFetchedHourlyTemps(hourlyTemps);
                    userDataRepository.writeLastTimeFetchedWeather(currentTime);
                    userDataRepository.writeIsCelsius(true);
                    view.displayTemperature(currentTemp, true);
                    view.displayHighTemperature(highTemp, true);
                    view.displayLowTemperature(lowTemp, true);
                    updateClothing();
                    weatherCallInProgress = false;
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
