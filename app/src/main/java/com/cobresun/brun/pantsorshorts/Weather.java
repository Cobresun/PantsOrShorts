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

    JSONObject data = null;
    private static String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?";
    private static String LATITUDE_STRING = "&lat=";
    private static String LONGITUDE_STRING = "&lon=";
    private static String APPID = "appid=ef157718f460aa11e33cfabcea9f6d01";

    public int lat;
    public int lon;

    public int temp;

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            LATITUDE_STRING = LATITUDE_STRING + lat;
            LONGITUDE_STRING = LONGITUDE_STRING + lon;

            URL url = new URL(BASE_URL + APPID + LONGITUDE_STRING + LATITUDE_STRING);
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
            temp = (int) mainObject.getDouble("temp");
            temp = (int) (temp + ABS_ZERO); // Kelvin to celsius
            System.out.println("BNOR: the temp is " + temp);
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
