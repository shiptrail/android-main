package de.h3adless.gpstracker.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import de.h3adless.gpstracker.R;
import de.h3adless.gpstracker.AppSettings;
import de.h3adless.gpstracker.activities.MainActivity;
import de.h3adless.gpstracker.database.Queries;
import de.h3adless.gpstracker.utils.ExtraInformationTracker;
import de.h3adless.gpstracker.utils.cgps.TrackPoint;

/**
 * Created by H3ADLESS on 10.07.2016.
 */
public class LocationService extends Service {

    public static final String BROADCAST_ACTION = "GPS_BROADCAST";
    public static final String BROADCAST_LOCATION = "GPS_LOCATION";

    public static final String BROADCAST_ANNOTATION = "ANNOTATION_BROADCAST";
    public static final String BROADCAST_ANNOTATION_TYPE = "ANNOTATION_TYPE";
    public static final String BROADCAST_ANNOTATION_TIMESTAMP = "ANNOTATION_TIMPESTAMP";

    public static final String TRACK_ID = "TRACK_ID";

    private static final int INTERVAL = 10000;

    private LocationManager locationManager;
    private MyLocationListener listener;
    private static long trackID = 0;

    private NotificationManager notificationManager;
    private SensorManager sensorManager;
    private Sensor accelerometer, magnetometer;
    private MySensorEventListener sensorListener = new MySensorEventListener();

    private Intent intent;

    private static long currentTrack = -1;

    private ArrayList<TrackPoint> signalsNotSent = new ArrayList<>();
    private ArrayList<Long> locationIdsNotSent = new ArrayList<>();
    private long lastLocationId = -1;

    //stuff for knowing if we are online or not
    private ConnectionChangeReceiver connectionChangeReceiver;
    public boolean isOnline;

    private BroadcastReceiver annotationsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(BROADCAST_ANNOTATION_TYPE);
            long timestamp = intent.getLongExtra(BROADCAST_ANNOTATION_TIMESTAMP, System.currentTimeMillis());
            if (type != null && !type.equals("")) {
                addAnnotation(type, timestamp);
            }
        }
    };

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

        connectionChangeReceiver = new ConnectionChangeReceiver();
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

        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

        //register the annotations receiver
        registerReceiver(annotationsReceiver, new IntentFilter(BROADCAST_ANNOTATION));

        //register for network change status to know if we are online
        connectionChangeReceiver.checkOnline();
        registerReceiver(connectionChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

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

        //unregister annotations receiver
        unregisterReceiver(annotationsReceiver);

        //unregister connectivity receiver
        unregisterReceiver(connectionChangeReceiver);

        //send all data we didnt send yet!
        if (signalsNotSent.size() > 0) {
            HttpRequest httpRequest = new HttpRequest(LocationService.this);
            httpRequest.locationIds = (ArrayList<Long>) locationIdsNotSent.clone();
            httpRequest.trackId = trackID;
            TrackPoint[] parameters = new TrackPoint[signalsNotSent.size()];
            signalsNotSent.toArray(parameters);
            httpRequest.execute(parameters);
            locationIdsNotSent.clear();
            signalsNotSent.clear();
        }
    }

	/**
	 *
     * @param annotation has to be one of de.h3adless.gpstracker.utils.cgps.trackpoint.annotations public
     *                   static final Strings, like TYPE_START_JIBE
     * @param timestamp
     */
    private void addAnnotation(String annotation, long timestamp) {
        if (signalsNotSent.size() == 0) {
            return;
        }
        //save annotation
        int toffsetInt = -1;
        long toffset = timestamp - signalsNotSent.get(signalsNotSent.size()-1).timestamp;
        if (toffset < Integer.MAX_VALUE && toffset > Integer.MIN_VALUE) {
            toffsetInt = (int) toffset;
        }
        TrackPoint.Annotation annotationData = new TrackPoint.Annotation(annotation, toffsetInt);
        signalsNotSent.get(signalsNotSent.size()-1).annotation.add(annotationData);
        //save annotation to DB
        Queries.insertAnnotation(getApplicationContext(), lastLocationId, annotationData);
        ExtraInformationTracker.track(this,
                ExtraInformationTracker.ExtraInformationType.Other,
                "Annotation created: " + annotation);
    }


    /**
     * source:
     * https://developer.android.com/training/basics/network-ops/managing.html
     * and http://stackoverflow.com/questions/1783117/network-listener-android
     *
     * see also http://stackoverflow.com/questions/2802472/detect-network-connection-type-on-android
     * for information about different network types
     */
    public class ConnectionChangeReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive( Context context, Intent intent )
        {
            checkOnline();
        }

        public void checkOnline() {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            isOnline = (networkInfo != null && networkInfo.isConnected());
            Toast.makeText(LocationService.this,
                    getString(R.string.connection_changed, isOnline ? getString(R.string.online) : getString(R.string.offline)),
                    Toast.LENGTH_SHORT).show();

            String networkType = "";
            if (networkInfo == null) {
                networkType = "Unknown";
            } else {
                int type = networkInfo.getType();
                if (type == ConnectivityManager.TYPE_WIFI) {
                    networkType = "WIFI";
                } else if (type == ConnectivityManager.TYPE_MOBILE) {
                    networkType = "MOBILE / ";
                    int subtype = networkInfo.getSubtype();
                    switch (subtype) {
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                            networkType += "1xRTT 50-100Kbps";
                            break;
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                            networkType += "CDMA 14-64Kbps";
                            break;
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                            networkType += "EDGE 50-100Kbps";
                            break;
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                            networkType += "EVDO_0 400-1000Kbps";
                            break;
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                            networkType += "EVDO_A 600-1400Kbps";
                            break;
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                            networkType += "GPRS 100Kbps";
                            break;
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                            networkType += "HSDPA 2-14Mbps";
                            break;
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                            networkType += "HSPA 700-1700Kbps";
                            break;
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                            networkType += "HSUPA 1-23Mbps";
                            break;
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                            networkType += "UMTS 400-7000Kbps";
                            break;
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                            networkType += "EHRPD 1-2Mbps";
                            break;
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                            networkType += "EVDO_B 5Mbps";
                            break;
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            networkType += "HSPAP 10-20Mbps";
                            break;
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            networkType += "IDEN 25kbps";
                            break;
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            networkType += "LTE 10+Mbps";
                            break;
                        case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                        default:
                            networkType += "Unknown";
                    }
                }
            }

            ExtraInformationTracker.track(getApplicationContext(),
                    ExtraInformationTracker.ExtraInformationType.Other,
                    "Network status: " + (isOnline ? getString(R.string.online) : getString(R.string.offline)),
                    "Network Type Information: " + networkType);
        }
    }


    public class MySensorEventListener implements SensorEventListener {

        float[] mGravity = null;
        float[] mGeomagnetic = null;

        long nextTimestampToSave = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            //check if we want to get sensor data already
            long current = System.currentTimeMillis();
            if (current >= nextTimestampToSave && signalsNotSent.size() > 0) {
                nextTimestampToSave = current;

                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                    mGravity = event.values.clone();
                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                    mGeomagnetic = event.values.clone();
                //TODO Compass data here. event.values should have x,y and z data in [0],[1] and [2]
                if (mGravity != null && mGeomagnetic != null) {
                    float R[] = new float[9];
                    float I[] = new float[9];
                    boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                    if (success) {
                        float orientation[] = new float[3];
                        SensorManager.getOrientation(R, orientation);

                        float azimuth = orientation[0] * (180 / (float) Math.PI);
                        float compass = azimuth;
                        if (azimuth < 0){
                            compass = 360+azimuth;
                        } else if (azimuth >= 360) {
                            compass = azimuth-360;
                        }

                        float pitch = orientation[1]* (180 / (float) Math.PI);
                        float roll = orientation[2]* (180 / (float) Math.PI);

                        //save ORIENTATION data
                        long toffset = current - signalsNotSent.get(signalsNotSent.size()-1).timestamp;
                        int toffsetInteger = -1;
                        if (toffset < Integer.MAX_VALUE && toffset > Integer.MIN_VALUE) {
                            toffsetInteger = (int) toffset;
                        }
                        TrackPoint.Orientation orientationData = new TrackPoint.Orientation(compass,pitch,roll,toffsetInteger);
                        signalsNotSent.get(signalsNotSent.size()-1).orientation.add(orientationData);
                        //save ORIENTATION data to DB
                        Queries.insertOrientation(getApplicationContext(), lastLocationId, orientationData);

                        //acceleration
                        float x = mGravity[0];
                        float y = mGravity[1];
                        float z = mGravity[2];

                        //save acceleration
                        TrackPoint.Accelerometer accelerometerData = new TrackPoint.Accelerometer(x,y,z,toffsetInteger);
                        signalsNotSent.get(signalsNotSent.size()-1).accelerometer.add(accelerometerData);
                        //save accelerometer to DB
                        Queries.insertAcceleration(getApplicationContext(), lastLocationId, accelerometerData);

                        //save compass
                        TrackPoint.Compass compassData = new TrackPoint.Compass(compass, toffsetInteger);
                        signalsNotSent.get(signalsNotSent.size()-1).compass.add(compassData);
                        //save compass to DB
                        Queries.insertCompass(getApplicationContext(), lastLocationId, compassData);


                        //determine when the next sensor data shall be monitored
                        nextTimestampToSave += AppSettings.getTrackingInterval()/10;

                        mGravity = null;
                        mGeomagnetic = null;
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

    public class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(final Location loc){
            Log.i("GPS", "Location changed");

            //send the data we collected until now to server. add the location always
            //to the list, but only send it if we have the correct settings.
            if (AppSettings.getSendTracksToServer()) {
                if (signalsNotSent.size() >= AppSettings.getSendTogether()) {
                    //check if we are online to send data
                    if (isOnline) {
                        HttpRequest httpRequest = new HttpRequest(LocationService.this);
                        httpRequest.locationIds = (ArrayList<Long>) locationIdsNotSent.clone();
                        httpRequest.trackId = trackID;
                        TrackPoint[] parameters = new TrackPoint[signalsNotSent.size()];
                        signalsNotSent.toArray(parameters);
                        httpRequest.execute(parameters);
                        locationIdsNotSent.clear();
                        signalsNotSent.clear();
                    }
                }
            }

            //save the data we just got

            //saving basics
            float lat = (float) loc.getLatitude();
            float lng = (float) loc.getLongitude();
            long timestamp = loc.getTime();
            float ele = (float) loc.getAltitude();
            TrackPoint trackPoint = new TrackPoint(lat,lng,timestamp,ele);
            //saving basics in DB
            lastLocationId = Queries.insertLocation(getApplicationContext(),
                    trackID,
                    trackPoint.lat,
                    trackPoint.lng,
                    trackPoint.timestamp,
                    trackPoint.ele);

            //saving GPS META
            float accuracy = loc.getAccuracy();
            Bundle extras = loc.getExtras();
            int satcount = 0;
            if (extras != null) {
                satcount = extras.getInt("satellites", 0);
            }
            int toffset = 0;
            TrackPoint.GpsMeta gpsMeta = new TrackPoint.GpsMeta(accuracy, satcount, toffset);
            trackPoint.gpsMeta.add(gpsMeta);
            //saving GPS META in DB
            Queries.insertGpsMeta(getApplicationContext(),
                    lastLocationId,
                    gpsMeta);

            // save positions in queue that gets sent to server at some point
            signalsNotSent.add(trackPoint);
            locationIdsNotSent.add(lastLocationId);

            // Send to activity to update UI
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
