package de.h3adless.gpstracker.services;

import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import de.h3adless.gpstracker.AppSettings;
import de.h3adless.gpstracker.BuildConfig;
import de.h3adless.gpstracker.R;
import de.h3adless.gpstracker.database.TrackingLocation;

/**
 * Created by Sebu on 09.07.2016.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */

public class HttpRequest extends AsyncTask<TrackingLocation, Integer, Void> {

    private static String URL;
    private static final String BASE_URL = "shiptrail.lenucksi.eu/";

    @Override
    protected void onPreExecute() {
        if (!AppSettings.getSendTracksToServer() ||
                AppSettings.getRandomDeviceUuid() == null ||
                AppSettings.getRandomDeviceUuid().equals("")) {
            cancel(true);
        }

        URL = "https://" +
                BASE_URL +
                AppSettings.getMainContext().getString(R.string.server_route, AppSettings.getRandomDeviceUuid());
    }

    @Override
    protected Void doInBackground(TrackingLocation... locations) {
        try {
            if (BuildConfig.DEBUG) {
                TrafficStats.setThreadStatsTag(0x1000);
            }
            URL url = new URL(URL);
            Log.d("HttpRequest","Url to send: " + URL);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            Gson gson = new Gson();
            String json = gson.toJson(locations);

            Log.d("HttpRequest","Parameter to send: " + json);

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                    connection.getOutputStream()));

            out.write(json);
            out.flush();
            out.close();

            // TODO retry?
            int responseCode = connection.getResponseCode();

            Log.d("HttpRequest", "ResponseCode: " + responseCode);

            //TODO read input?
            /*
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

            String answer = "";
            String read;
            while ((read = in.readLine()) != null) {
                answer += read;
            }
            in.close();
            //do stuff with answer
            */

            connection.disconnect();

            return null;
        } catch (java.io.IOException e) {

            e.printStackTrace();
            return null;
        } finally {
            if (BuildConfig.DEBUG) {
                TrafficStats.clearThreadStatsTag();
            }
        }
    }

}