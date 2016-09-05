package de.h3adless.gpstracker.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import de.h3adless.gpstracker.activities.MainActivity;
import de.h3adless.gpstracker.database.TrackDatabase;
import de.h3adless.gpstracker.database.TrackDatabaseHelper;
import de.h3adless.gpstracker.database.TrackingLocation;

/**
 * Created by H3ADLESS on 10.07.2016.
 */
public class LocationService extends Service {

    public static final String BROADCAST_ACTION = "GPS_BROADCAST";
    public static final String BROADCAST_LOCATION = "GPS_LOCATION";
    public static final String BROADCAST_SENSOR_BEARING = "sensor_bearing";
    public static final String BROADCAST_SENSOR_PITCH = "sensor_pitch";
    public static final String BROADCAST_SENSOR_ROLL = "sensor_roll";

    public static final String TRACK_ID = "TRACK_ID";

    private static final int INTERVAL = 10000;
    private TrackDatabaseHelper trackDatabaseHelper;

    private LocationManager locationManager;
    private MyLocationListener listener;
    private static long trackID = 0;

    private NotificationManager notificationManager;
    private SensorManager sensorManager;
    private Sensor accelerometer, magnetometer;
    private MySensorEventListener sensorListener = new MySensorEventListener();
    private float bearing, pitch, roll = 0.0f;

    private Intent intent;

    private static long currentTrack = -1;

    private ArrayList<TrackingLocation> signalsNotSent = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("LocationService","onCreate");
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.rounded_button_active)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setOngoing(true);

        Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(resultPendingIntent);

        Notification trackingNotification = notificationBuilder.build();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, trackingNotification);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        intent = new Intent(BROADCAST_ACTION);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d("LocationService","onStartCommand");
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

        //registering for sensor data

        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorListener, magnetometer, SensorManager.SENSOR_DELAY_UI);

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppSettings.setTrackingEnabled(false);
        notificationManager.cancel(0);
        Log.v("STOP_SERVICE", "DONE");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider Permission Request
            return;
        }
        locationManager.removeUpdates(listener);

        //unregister sensor data
        sensorManager.unregisterListener(sensorListener);
    }

    public class MySensorEventListener implements SensorEventListener {

        float[] mGravity = null;
        float[] mGeomagnetic = null;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                mGravity = event.values.clone();
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mGeomagnetic = event.values.clone();
            if (mGravity != null && mGeomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    bearing = (float) Math.toDegrees(orientation[0]);
                    pitch = (float) Math.toDegrees(orientation[1]);
                    roll = (float) Math.toDegrees(orientation[2]);
                    intent.putExtra(BROADCAST_SENSOR_BEARING, bearing);
                    intent.putExtra(BROADCAST_SENSOR_PITCH, pitch);
                    intent.putExtra(BROADCAST_SENSOR_ROLL, roll);
                    sendBroadcast(intent);
                    mGravity = null;
                    mGeomagnetic = null;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
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

            //send positions to server. add the location always to the list, but only send it
            //if we have the correct settings.
            signalsNotSent.add(new TrackingLocation(loc));
            if (AppSettings.getSendTracksToServer()) {
                if (signalsNotSent.size() >= AppSettings.getSendTogether()) {
                    HttpRequest httpRequest = new HttpRequest(LocationService.this);
                    TrackingLocation[] parameters = new TrackingLocation[signalsNotSent.size()];
                    signalsNotSent.toArray(parameters);
                    httpRequest.execute(parameters);
                    signalsNotSent.clear();
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
