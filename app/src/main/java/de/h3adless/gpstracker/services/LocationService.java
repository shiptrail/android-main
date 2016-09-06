package de.h3adless.gpstracker.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import de.h3adless.gpstracker.R;
import de.h3adless.gpstracker.AppSettings;
import de.h3adless.gpstracker.activities.MainActivity;
import de.h3adless.gpstracker.database.Queries;
import de.h3adless.gpstracker.database.TrackDatabaseHelper;
import de.h3adless.gpstracker.utils.cgps.TrackPoint;

/**
 * Created by H3ADLESS on 10.07.2016.
 */
public class LocationService extends Service {

    public static final String BROADCAST_ACTION = "GPS_BROADCAST";
    public static final String BROADCAST_LOCATION = "GPS_LOCATION";
    public static final String BROADCAST_SENSOR_AZIMUTH = "sensor_azimuth";
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
    private float azimuth, pitch, roll = 0.0f;

    private Intent intent;

    private static long currentTrack = -1;

    private ArrayList<TrackPoint> signalsNotSent = new ArrayList<>();

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
                    azimuth = (float) Math.toDegrees(orientation[0]);
                    pitch = (float) Math.toDegrees(orientation[1]);
                    roll = (float) Math.toDegrees(orientation[2]);
                    intent.putExtra(BROADCAST_SENSOR_AZIMUTH, azimuth);
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

        private void saveToDb(TrackPoint trackPoint) {

            int id = Queries.insertLocation(getApplicationContext(),
                    trackID,
                    trackPoint.lat,
                    trackPoint.lng,
                    trackPoint.timestamp,
                    trackPoint.ele);

            Queries.insertGpsMeta(getApplicationContext(),
                    id,
                    trackPoint.gpsMeta);

            Queries.insertOrientaion(getApplicationContext(),
                    id,
                    trackPoint.orientation);
        }

        @Override
        public void onLocationChanged(final Location loc){
            Log.i("GPS", "Location changed");

            float lat = (float) loc.getLatitude();
            float lng = (float) loc.getLongitude();
            long timestamp = loc.getTime();
            float ele = (float) loc.getAltitude();
            TrackPoint trackPoint = new TrackPoint(lat,lng,timestamp,ele);

            float accuracy = loc.getAccuracy();
            int satcount = (int) loc.getExtras().get("satellites");
            int toffset = 0;
            trackPoint.gpsMeta.add(new TrackPoint.GpsMeta(accuracy, satcount, toffset));

            trackPoint.orientation.add(new TrackPoint.Orientation(azimuth, pitch, roll, 0));

            // save positions
            saveToDb(trackPoint);

            //send positions to server. add the location always to the list, but only send it
            //if we have the correct settings.
            signalsNotSent.add(trackPoint);
            if (AppSettings.getSendTracksToServer()) {
                if (signalsNotSent.size() >= AppSettings.getSendTogether()) {
                    HttpRequest httpRequest = new HttpRequest(LocationService.this);
                    TrackPoint[] parameters = new TrackPoint[signalsNotSent.size()];
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
