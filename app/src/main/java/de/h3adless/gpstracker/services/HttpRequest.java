package de.h3adless.gpstracker.services;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.appdatasearch.GetRecentContextCall;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import de.h3adless.gpstracker.database.TrackingLocation;

/**
 * Created by Sebu on 09.07.2016.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */

public class HttpRequest extends AsyncTask<TrackingLocation, Integer, Void> {

    private static String URL_ROUTE = "/v1/send";
    private static String URL;

    private SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss:SSS", Locale.GERMAN);

    public HttpRequest(String url, String port) {
        URL = "http://" + url + ":" + port + URL_ROUTE;
    }

    @Override
    protected Void doInBackground(TrackingLocation... locations) {
        try {
            URL url = new URL(URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            Gson gson = new Gson();
            String json = gson.toJson(locations);

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                    connection.getOutputStream()));

            out.write(json);
            out.flush();
            out.close();

            // TODO RETRY?
            int responseCode = connection.getResponseCode();

            connection.disconnect();

            return null;
        } catch (java.io.IOException e) {

            e.printStackTrace();
            return null;
        }
    }

}