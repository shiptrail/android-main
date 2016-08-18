package de.h3adless.gpstracker.database;

import android.location.Location;

import com.google.gson.annotations.Expose;

import de.h3adless.gpstracker.AppSettings;

/**
 * Created by H3ADLESS on 30.07.2016.
 */
public class TrackingLocation {

    public TrackingLocation() {}

    public TrackingLocation(Location loc) {
        Integer satCount = (Integer)loc.getExtras().get("satellites");
        //if (satCount != null) this.satCount = satCount;

        this.lat = (float) loc.getLatitude();
        this.lng = (float) loc.getLongitude();
        //this.accuracy = (float) loc.getAccuracy();
        this.ele = (float) loc.getAltitude();
        this.heading = loc.getBearing();
        //this.speed = loc.getSpeed();
        //this.time = loc.getTime();
        //this.deviceId = AppSettings.RANDOM_DEVICE_UUID;
    }

    //public long trackId;
    //public long id;

    //String deviceId;

    public float lat;
    public float lng;

    //public float accuracy;
    public float ele;
    public float heading;

    //public float speed;
    public long timestamp;

    //public int satCount;

}
