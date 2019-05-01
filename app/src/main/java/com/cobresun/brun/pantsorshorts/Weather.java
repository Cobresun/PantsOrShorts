package com.cobresun.brun.pantsorshorts;

import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Weather extends AsyncTask<Void, Void, Void> {
    private static final double ABS_ZERO = -273.15;

    private static String APPID;

    public double lat;
    public double lon;

    public int temp;
    public int tempHigh;
    public int tempLow;

    public Weather(String apiKey) {
        APPID = apiKey;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            String BASE_URL = "https://api.darksky.net/forecast/";
            String OPTIONS = "?exclude=minutely,hourly,alerts,flags&units=ca";
            URL url = new URL(BASE_URL + APPID + "/" + lat + "," + lon + OPTIONS);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder json = new StringBuilder(1024);
            String tmp;

            while ((tmp = reader.readLine()) != null) {
                json.append(tmp).append("\n");
            }
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            JSONObject object = new JSONObject(String.valueOf(data));
            JSONObject currentlyObject = object.getJSONObject("currently");
            JSONObject daily = object.getJSONObject("daily");

            temp = round(currentlyObject.getDouble("temperature")); // + 0.5 for rounding purposes, since (int) cast drops it

            JSONArray dailyData = daily.getJSONArray("data");
            JSONObject today = dailyData.getJSONObject(0);

            tempHigh = round(today.getDouble("temperatureMax"));

            tempLow = round(today.getDouble("temperatureMin"));
        }
        catch (java.io.IOException e) {
            e.printStackTrace();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
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
