package de.h3adless.gpstracker;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import java.io.Serializable;
import java.net.URISyntaxException;

/**
 * Created by timo_ on 09.08.2016.
 *
 * kind of a global static singleton.
 * gets instantiated first before any other classes of this app.
 * needs android:name=".AppSettings" in manifest.
 *
 * when being created, it checks for already saved storage data with loadDataFromFile().
 *
 * every setter automatically saves the data in storage with the storagehandler.
 *
 */

public class AppSettings extends Application {

    private static AppSettings mainContext = null;


    private static boolean TRACKING_ENABLED = false;
    private static Intent LOCATION_SERVICE_INTENT = null;

    public static String SERVER_URL = null;
    public static String SERVER_PORT = null;

    public static long TRACKING_INTERVAL = 10000;
    public static int SEND_TOGETHER = 1;

    public static String RANDOM_DEVICE_UUID = null;

    public void onCreate() {
        super.onCreate();
        mainContext = this;
        Log.d("AppSettings", "ONCREATE. maincontext: " + mainContext);
        loadDataFromFile();
    }

    public static boolean getTrackingEnabled() {
        return TRACKING_ENABLED;
    }

    public static void setTrackingEnabled(boolean trackingEnabled) {
        TRACKING_ENABLED = trackingEnabled;
        StorageHandler.save(mainContext, StorageHandler.STORAGE_TRACKING_BOOLEAN, trackingEnabled);
    }

    public static Intent getLocationServiceIntent() {
        return LOCATION_SERVICE_INTENT;
    }

    public static void setLocationServiceIntent(Intent locationServiceIntent) {
        LOCATION_SERVICE_INTENT = locationServiceIntent;
        StorageHandler.save(mainContext, StorageHandler.STORAGE_LOCATION_SERVICE_INTENT, locationServiceIntent.toUri(0));
    }

    public static long getTrackingInterval() {
        return TRACKING_INTERVAL;
    }

    public static void setTrackingInterval(long trackingInterval) {
        TRACKING_INTERVAL = trackingInterval;
        StorageHandler.save(mainContext, StorageHandler.STORAGE_TRACKING_INTERVAL, trackingInterval);
    }

    public static int getSendTogether() {
        return SEND_TOGETHER;
    }

    public static void setSendTogether(int sendTogether) {
        SEND_TOGETHER = sendTogether;
        StorageHandler.save(mainContext, StorageHandler.STORAGE_SEND_TOGETHER, sendTogether);
    }

    private static void loadDataFromFile() {
        Serializable trackingEnabled = StorageHandler.load(mainContext, StorageHandler.STORAGE_TRACKING_BOOLEAN);
        if (trackingEnabled != null) {
            TRACKING_ENABLED = (Boolean) trackingEnabled;
        }

        try {
            Serializable locationServiceIntent = StorageHandler.load(mainContext, StorageHandler.STORAGE_LOCATION_SERVICE_INTENT);
            if (locationServiceIntent != null) {
                LOCATION_SERVICE_INTENT = Intent.parseUri((String) locationServiceIntent, 0);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Serializable trackingInterval = StorageHandler.load(mainContext, StorageHandler.STORAGE_TRACKING_INTERVAL);
        if (trackingInterval != null) {
            TRACKING_INTERVAL = (long) trackingInterval;
        }

        Serializable sendTogether = StorageHandler.load(mainContext, StorageHandler.STORAGE_SEND_TOGETHER);
        if (sendTogether != null) {
            SEND_TOGETHER = (int) sendTogether;
        }
    }

}
