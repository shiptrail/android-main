package de.h3adless.gpstracker.services;

import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import de.h3adless.gpstracker.AppSettings;
import de.h3adless.gpstracker.BuildConfig;
import de.h3adless.gpstracker.R;
import de.h3adless.gpstracker.activities.MainActivity;
import de.h3adless.gpstracker.database.Queries;
import de.h3adless.gpstracker.database.TrackDatabase;
import de.h3adless.gpstracker.utils.ExtraInformationTracker;
import de.h3adless.gpstracker.utils.cgps.TrackPoint;

/**
 * Created by Sebu on 09.07.2016.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */

public class HttpRequest extends AsyncTask<TrackPoint, Integer, Void> {

    private static String URL;
    private static final String BASE_URL = "shiptrail.lenucksi.eu";

    Context context;
    private Certificate[] certificates = null;

    public ArrayList<Long> locationIds = new ArrayList<>();
    public long trackId;

    private long startTime = 0;
    private long endTime = 0;

    public HttpRequest( Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        if (!AppSettings.getSendTracksToServer() ||
                AppSettings.getRandomDeviceUuid() == null ||
                AppSettings.getRandomDeviceUuid().equals("")) {
            cancel(true);
        }

        URL = (AppSettings.getUseHttps() ? "https://" : "http://")
                +
                (AppSettings.getUseCustomServer() ?
                        (AppSettings.getCustomServerUrl() + ":" + AppSettings.getCustomServerPort()) :
                        BASE_URL)
                +
                AppSettings.getMainContext().getString(R.string.server_route, AppSettings.getRandomDeviceUuid());
    }

    @Override
    protected Void doInBackground(TrackPoint... locations) {
        try {
            if (BuildConfig.DEBUG) {
                TrafficStats.setThreadStatsTag(0x1000);
            }
            startTime = System.currentTimeMillis();
            URL url = new URL(URL);
            Log.d("HttpRequest", "Url to send: " + URL);

            HttpURLConnection connection;
            if (AppSettings.getUseHttps()) {
                connection = (HttpsURLConnection) url.openConnection();

                HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        HostnameVerifier hv =
                                HttpsURLConnection.getDefaultHostnameVerifier();

                        try {
                            certificates = session.getPeerCertificates();
                        } catch (SSLPeerUnverifiedException e) {
                            e.printStackTrace();
                        }


                        //check for manually accepted certificates.
                        if (AppSettings.getCustomAcceptedCertificates().containsKey(AppSettings.getCustomServerUrl())
                                && certificates != null && certificates.length > 0) {
                            return Arrays.equals(certificates, AppSettings.getCustomAcceptedCertificates().get(AppSettings.getCustomServerUrl()));
                        }
                        return hv.verify(hostname, session);
                    }
                };
                ((HttpsURLConnection) connection).setHostnameVerifier(hostnameVerifier);
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }

            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            Gson gson = new Gson();
            String json = gson.toJson(locations);

            Log.d("HttpRequest", "Parameter to send: " + json);
            for (TrackPoint tp : locations) {
                Log.d("HttpRequest","sending trackpoint timestamp: " + tp.timestamp);
            }

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                    connection.getOutputStream()));

            out.write(json);
            out.flush();
            out.close();

            int responseCode = connection.getResponseCode();

            endTime = System.currentTimeMillis();

            Log.d("HttpRequest", "ResponseCode: " + responseCode);
            ExtraInformationTracker.track(context, ExtraInformationTracker.ExtraInformationType.Battery);
            ExtraInformationTracker.track(context, ExtraInformationTracker.ExtraInformationType.Network,
                    URL,
                    json,
                    String.valueOf(responseCode),
                    String.valueOf(endTime - startTime));
            connection.disconnect();
            return null;
        } catch (IOException e) {
            e.printStackTrace();

            ExtraInformationTracker.track(context, ExtraInformationTracker.ExtraInformationType.Battery);
            String time = "";
            if (endTime != 0) {
                time = String.valueOf(endTime - startTime);
            }
            ExtraInformationTracker.track(context,
                    ExtraInformationTracker.ExtraInformationType.Network,
                    URL,
                    new Gson().toJson(locations),
                    e.getMessage(),
                    time);

            //für Informationen siehe https://developer.android.com/training/articles/security-ssl.html
            //allgemeine HTTPS Probleme/manuelles Akzeptieren vom Zert. gescheitert: HTTP Probieren-Dialog.
            //bestimmter Fehler, in dem das Zertifikat nicht akzeptiert wurde: anderer Dialog
            if (AppSettings.getUseHttps()) {
                if ((e instanceof SSLException)
                            || (e.getMessage().contains("cannot be cast to javax.net.ssl.HttpsURLConnection"))
                            || (AppSettings.getCustomAcceptedCertificates().containsKey(AppSettings.getCustomServerUrl()))
                    ) {
                    makeHttpsDialog();
                    return null;
                } else if (e.getMessage().startsWith("Hostname")
                        && e.getMessage().contains("was not verified")) {
                    makeCertificateDialog();
                    return null;
                }
            }

            makeRetryDialog(e.getLocalizedMessage());

            return null;
        } finally {
            if (BuildConfig.DEBUG) {
                TrafficStats.clearThreadStatsTag();
            }
        }
    }

    private void makeRetryDialog(String errorMsg) {
        //insert into DB that request for these locations failed
        if (locationIds.size() > 0) {
            Queries.insertFailedRequests(context, locationIds, TrackDatabase.FailedRequestsEntry.TYPE_OTHER);
        }

        //tell mainactivity to show dialog
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(AppSettings.INTENT_START_RETRY_DIALOG, true);
        intent.putExtra(AppSettings.INTENT_START_DIALOG_PARAMS, errorMsg);
        intent.putExtra(AppSettings.INTENT_START_DIALOG_TRACKID, trackId);

        context.startActivity(intent);
    }

    private void makeCertificateDialog() {
        //insert into DB that request for these locations failed
        if (locationIds.size() > 0) {
            Queries.insertFailedRequests(context, locationIds, TrackDatabase.FailedRequestsEntry.TYPE_CERTIFICATE);
        }

        //tell mainactivity to show dialog
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(AppSettings.INTENT_START_CERTIFICATE_DIALOG, true);
        intent.putExtra(AppSettings.INTENT_START_DIALOG_PARAMS, certificates);
        intent.putExtra(AppSettings.INTENT_START_DIALOG_TRACKID, trackId);

        context.startActivity(intent);
    }

    private void makeHttpsDialog() {
        //insert into DB that request for these locations failed
        if (locationIds.size() > 0) {
            Queries.insertFailedRequests(context, locationIds, TrackDatabase.FailedRequestsEntry.TYPE_HTTPS);
        }

        //tell mainactivity to show dialog
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(AppSettings.INTENT_START_HTTPS_DIALOG, true);
        intent.putExtra(AppSettings.INTENT_START_DIALOG_TRACKID, trackId);

        context.startActivity(intent);
    }

}