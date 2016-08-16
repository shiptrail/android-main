package de.h3adless.gpstracker;

import android.app.Application;
import android.content.Intent;

/**
 * Created by timo_ on 09.08.2016.
 */
public class AppSettings extends Application {

    private AppSettings mainContext = null;


    public static boolean TRACKING_ENABLED = false;
    public static Intent LOCATION_SERVICE_INTENT = null;

    public static String SERVER_URL = null;
    public static String SERVER_PORT = null;

    public static long TRACKING_INTERVALL = 10000;
    public static int SEND_EVERY_XTH_LOC = 1;

    public static String RANDOM_DEVICE_UUID = null;

    public void onCreate() {
        super.onCreate();
        mainContext = this;
    }

    public AppSettings getAppSettings() {
        return mainContext;
    }

}
