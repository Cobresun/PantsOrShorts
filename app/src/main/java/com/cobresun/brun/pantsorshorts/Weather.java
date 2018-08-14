package com.cobresun.brun.pantsorshorts;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Weather {

    JSONObject data = null;
    private static String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?";
    private static String CITYID = "id=524901"; // TODO: Don't hardcode city
    private static String APPID = "&APPID=ef157718f460aa11e33cfabcea9f6d01";
    public double temp;

    @SuppressLint("StaticFieldLeak")
    public void getWeatherJSON() {
        new AsyncTask<Void, Void, Void>() {

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
                        return null;
                    }
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void Void) {
                if (data != null) {
                    try {
                        JSONObject object = new JSONObject(String.valueOf(data));
                        JSONArray listArray = object.getJSONArray("list");
                        JSONObject list0Obejct = listArray.getJSONObject(0);
                        JSONObject mainObject = list0Obejct.getJSONObject("main");
                        temp = mainObject.getDouble("temp");
                        temp = temp - 273.15;
                        System.out.println("BNOR: temp: " + temp);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }
}
