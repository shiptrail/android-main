package de.h3adless.gpstracker.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.h3adless.gpstracker.R;
import de.h3adless.gpstracker.AppSettings;
import de.h3adless.gpstracker.database.TrackDatabase;
import de.h3adless.gpstracker.database.TrackDatabaseHelper;
import de.h3adless.gpstracker.database.TrackingLocation;

/**
 * Created by H3ADLESS on 10.07.2016.
 */
public class LocationService extends Service {

    public static final String BROADCAST_ACTION = "GPS_BROADCAST";
    public static final String BROADCAST_LOCATION = "GPS_LOCATION";

    public static final String TRACK_ID = "TRACK_ID";

    private static final int INTERVAL = 10000;
    private TrackDatabaseHelper trackDatabaseHelper;

    private LocationManager locationManager;
    private MyLocationListener listener;
    private static long trackID = 0;

    private NotificationManager notificationManager;

    private Intent intent;

    private static long currentTrack = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.rounded_button_active)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setOngoing(true);

        Notification trackingNotification = notificationBuilder.build();
        this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, trackingNotification);

        intent = new Intent(BROADCAST_ACTION);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider permission request
            Log.d("GPS", "PERMISSION DENIED");
            return START_REDELIVER_INTENT;
        }

        if (intent.hasExtra(TRACK_ID)) {
            trackID = intent.getExtras().getLong(TRACK_ID);
        } else if (currentTrack > -1) {
            trackID = currentTrack;
        } else {
            throw new IllegalStateException("Missing parameter trackID on service creation.");
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, 0, listener);

//        return 0;
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(0);
        Log.v("STOP_SERVICE", "DONE");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider Permission Request
            return;
        }
        locationManager.removeUpdates(listener);
    }

    public class MyLocationListener implements LocationListener {

        private void saveToDb(Location location) {
            if(trackDatabaseHelper == null) {
                trackDatabaseHelper = TrackDatabaseHelper.getInstance(getApplicationContext());
            }

            SQLiteDatabase db = trackDatabaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID, trackID);
            values.put(TrackDatabase.LocationEntry.COLUMN_NAME_ACCURACY, location.getAccuracy());
            values.put(TrackDatabase.LocationEntry.COLUMN_NAME_ALTITUDE, location.getAltitude());
            values.put(TrackDatabase.LocationEntry.COLUMN_NAME_BEARING, location.getBearing());
            values.put(TrackDatabase.LocationEntry.COLUMN_NAME_LAT, location.getLatitude());
            values.put(TrackDatabase.LocationEntry.COLUMN_NAME_LNG, location.getLongitude());
            values.put(TrackDatabase.LocationEntry.COLUMN_NAME_SAT_COUNT, (Integer) location.getExtras().get("satellites"));
            values.put(TrackDatabase.LocationEntry.COLUMN_NAME_SPEED, location.getSpeed());
            values.put(TrackDatabase.LocationEntry.COLUMN_NAME_TIME, location.getTime());
            db.insert(TrackDatabase.LocationEntry.TABLE_NAME, null, values);
        }

        @Override
        public void onLocationChanged(final Location loc){
            Log.i("GPS", "Location changed");

                // save positions
                saveToDb(loc);

                // TODO send positions to server

                if(AppSettings.SERVER_URL != null && !AppSettings.SERVER_URL.equals("")) {
                    if(AppSettings.SERVER_PORT != null && !AppSettings.SERVER_PORT.equals("")) {
                        HttpRequest httpRequest = new HttpRequest(AppSettings.SERVER_URL, AppSettings.SERVER_PORT);
                        List<TrackingLocation> locs = new ArrayList<TrackingLocation>();
                        httpRequest.doInBackground(new TrackingLocation(loc));
                    }
                }

                // Send to activity
                intent.putExtra(BROADCAST_LOCATION, loc);
                sendBroadcast(intent);
        }

        @Override
        public void onProviderDisabled(String provider){
            Toast.makeText( getApplicationContext(), "GPS disabled", Toast.LENGTH_SHORT ).show();
        }

        @Override
        public void onProviderEnabled(String provider){
            Toast.makeText( getApplicationContext(), "GPS enabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras){}

    }
}
