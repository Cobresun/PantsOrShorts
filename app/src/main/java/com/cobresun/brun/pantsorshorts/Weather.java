package com.cobresun.brun.pantsorshorts;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Weather extends AsyncTask<Void, Void, Void> {
    private static final double ABS_ZERO = -273.15;

    JSONObject data = null;
    private static String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?";
    private static String CITYID = "id=292223"; // TODO: Don't hardcode city
    private static String APPID = "&APPID=ef157718f460aa11e33cfabcea9f6d01";
    public double temp;

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL url = new URL(BASE_URL + CITYID + APPID);
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
            JSONArray listArray = object.getJSONArray("list");
            JSONObject list0Obejct = listArray.getJSONObject(0);
            JSONObject mainObject = list0Obejct.getJSONObject("main");
            temp = mainObject.getDouble("temp");
            temp = temp + ABS_ZERO; // Kelvin to celsius

            System.out.println("BNOR: temp today is: " + temp);

        } catch (java.io.IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
