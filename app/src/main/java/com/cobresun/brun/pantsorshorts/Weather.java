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
    public String city;
    private static String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?";
    private static String CITYID = "id=292223"; // TODO: currently some random hardcoded city in case we can't find user's city
    private static String APPID = "&APPID=ef157718f460aa11e33cfabcea9f6d01";
    public double temp;

    @Override
    protected Void doInBackground(Void... voids) {
        try {
//            CITYID = updateCITYID(city);
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

    private String updateCITYID(String city){
        System.out.println("BNOR: city(0): " + getCities(MainActivity.activity).size());
        String cityID = "292223";   // TODO: need to not hard code this...
        return "id=" + cityID;
    }


    public Set<City> getCities(final Activity activity) {

        Set<City> usersList = new HashSet<>();
        String json = readFromAsset(activity, "city.list.json");
        Type listType = new TypeToken<HashSet<City>>() {}.getType();
        // convert json into a list of Users
        try {
            usersList = new Gson().fromJson(json, listType);
        }
        catch (Exception e) {
            // we never know :)
            Log.e("error parsing", e.toString());
        }
        return usersList;
    }

    /**
     * Read file from asset directory
     * @param act current activity
     * @param fileName file to read
     * @return content of the file, string format
     */
    private  String readFromAsset(final Activity act, final String fileName)
    {
        String text = "";
        try {
            InputStream is = act.getAssets().open(fileName);

            int size = is.available();

            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            text = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }
}
