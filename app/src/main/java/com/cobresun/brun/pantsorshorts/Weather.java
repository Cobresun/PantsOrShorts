package com.cobresun.brun.pantsorshorts;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class Weather extends AsyncTask<Void, Void, Void> {
    private static final double ABS_ZERO = -273.15;

    JSONObject data = null;
    private static String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?q=";
    public static String CITY = "Calgary"; // TODO: currently some random hardcoded city in case we can't find user's city
    private static String APPID = "&APPID=ef157718f460aa11e33cfabcea9f6d01";
    public double temp;

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL url = new URL(BASE_URL + CITY + APPID);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp = "";

            while ((tmp = reader.readLine()) != null) {
                json.append(tmp).append("\n");
            }
            reader.close();

            data = new JSONObject(json.toString());

            if (data.getInt("cod") != 200) {
                System.out.println("Cancelled");
            }

            JSONObject object = new JSONObject(String.valueOf(data));
            JSONObject mainObject = object.getJSONObject("main");
            temp = mainObject.getDouble("temp");
            temp = temp + ABS_ZERO; // Kelvin to celsius
            System.out.println("BNOR: the temp in " + CITY + " " + temp);

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
