package com.cobresun.brun.pantsorshorts;

import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Weather extends AsyncTask<Void, Void, Void> {
    private static final double ABS_ZERO = -273.15;

    private static String LATITUDE_STRING = "&lat=";
    private static String LONGITUDE_STRING = "&lon=";
    private static String APPID;

    public int lat;
    public int lon;
    public int temp;

    public Weather(String apiKey) {
        APPID = "appid=" + apiKey;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            LATITUDE_STRING = LATITUDE_STRING + lat;
            LONGITUDE_STRING = LONGITUDE_STRING + lon;

            String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?";
            URL url = new URL(BASE_URL + APPID + LONGITUDE_STRING + LATITUDE_STRING);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder json = new StringBuilder(1024);
            String tmp;

            while ((tmp = reader.readLine()) != null) {
                json.append(tmp).append("\n");
            }
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            if (data.getInt("cod") != 200) {
                System.out.println("Cancelled");
            }

            JSONObject object = new JSONObject(String.valueOf(data));
            JSONObject mainObject = object.getJSONObject("main");
            double tempKelvin = mainObject.getDouble("temp") + 0.5; // + 0.5 for rounding purposes, since (int) cast drops it
            temp = (int) (tempKelvin + ABS_ZERO); // Kelvin to celsius
        }
        catch (java.io.IOException e) {
            e.printStackTrace();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
