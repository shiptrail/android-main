package de.h3adless.gpstracker;

import android.app.Application;
import android.content.Intent;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.security.cert.Certificate;
import java.util.HashMap;

import javax.security.cert.X509Certificate;

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

    public static final String INTENT_START_HTTPS_DIALOG = "start_https_dialog";
    public static final String INTENT_START_CERTIFICATE_DIALOG = "start_certificate_dialog";
    public static final String INTENT_START_RETRY_DIALOG = "start_retry_dialog";

    public static final String INTENT_START_DIALOG_TRACKID = "start_retry_dialog_trackid";
    public static final String INTENT_START_DIALOG_PARAMS = "start_dialog_params";


    private static AppSettings mainContext = null;

    private static boolean TRACKING_ENABLED = false;
    private static Intent LOCATION_SERVICE_INTENT = null;
    private static boolean SEND_TRACKS_TO_SERVER = true;
    private static long TRACKING_INTERVAL = 10000;
    private static int SEND_TOGETHER = 1;
    private static String RANDOM_DEVICE_UUID = null;
    private static boolean USE_HTTPS = true;
    private static boolean USE_CUSTOM_SERVER = false;
    private static String CUSTOM_SERVER_URL = "";
    private static String CUSTOM_SERVER_PORT = "";
    private static boolean TRACK_EXTRA_INFORMATION = false;

    private static HashMap<String, Certificate[]> CUSTOM_ACCEPTED_CERTIFICATES = new HashMap<>();


    public void onCreate() {
        super.onCreate();
        mainContext = this;
        loadDataFromFile();
    }

    public static AppSettings getMainContext() {
        return mainContext;
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

    public static boolean getSendTracksToServer() {
        return SEND_TRACKS_TO_SERVER;
    }

    public static void setSendTracksToServer(boolean sendTracksToServer) {
        SEND_TRACKS_TO_SERVER = sendTracksToServer;
        StorageHandler.save(mainContext, StorageHandler.STORAGE_SEND_TRACKS_TO_SERVER, sendTracksToServer);
    }

    public static boolean getUseHttps() {
        return USE_HTTPS;
    }

    public static void setUseHttps(boolean useHttps) {
        USE_HTTPS = useHttps;
    }

    public static String getRandomDeviceUuid() {
        return RANDOM_DEVICE_UUID;
    }

    public static void setRandomDeviceUuid(String randomDeviceUuid) {
        RANDOM_DEVICE_UUID = randomDeviceUuid;
        StorageHandler.save(mainContext, StorageHandler.STORAGE_RANDOM_DEVICE_UUID, randomDeviceUuid);
    }

    public static boolean getUseCustomServer() {
        return USE_CUSTOM_SERVER;
    }

    public static void setUseCustomServer(boolean useCustomServer) {
        USE_CUSTOM_SERVER = useCustomServer;
        StorageHandler.save(mainContext, StorageHandler.STORAGE_USE_CUSTOM_SERVER, useCustomServer);
    }

    public static String getCustomServerUrl() {
        return CUSTOM_SERVER_URL;
    }

    public static void setCustomServerUrl(String customServerUrl) {
        CUSTOM_SERVER_URL = customServerUrl;
        StorageHandler.save(mainContext, StorageHandler.STORAGE_CUSTOM_SERVER_URL, customServerUrl);
    }

    public static String getCustomServerPort() {
        return CUSTOM_SERVER_PORT;
    }

    public static void setCustomServerPort(String customServerPort) {
        CUSTOM_SERVER_PORT = customServerPort;
        StorageHandler.save(mainContext, StorageHandler.STORAGE_CUSTOM_SERVER_PORT, customServerPort);
    }

    public static HashMap<String, Certificate[]> getCustomAcceptedCertificates() {
        return  CUSTOM_ACCEPTED_CERTIFICATES;
    }

    public static void addCustomAcceptedCertificate(String url, Certificate[] certificateChain) {
        CUSTOM_ACCEPTED_CERTIFICATES.put(url, certificateChain);
        StorageHandler.save(mainContext, StorageHandler.STORAGE_CUSTOM_ACCEPTED_CERTIFICATES, CUSTOM_ACCEPTED_CERTIFICATES);
    }

    public static void deleteCustomAcceptedCertificates() {
        CUSTOM_ACCEPTED_CERTIFICATES.clear();
        StorageHandler.save(mainContext, StorageHandler.STORAGE_CUSTOM_ACCEPTED_CERTIFICATES, CUSTOM_ACCEPTED_CERTIFICATES);
    }

    public static boolean getTrackExtraInformation() {
        return TRACK_EXTRA_INFORMATION;
    }

    public static void setTrackExtraInformation(boolean trackExtraInformation) {
        TRACK_EXTRA_INFORMATION = trackExtraInformation;
        StorageHandler.save(mainContext, StorageHandler.STORAGE_TRACK_EXTRA_INFORMATION, TRACK_EXTRA_INFORMATION);
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

        Serializable sendTracksToServer = StorageHandler.load(mainContext, StorageHandler.STORAGE_SEND_TRACKS_TO_SERVER);
        if (sendTracksToServer != null) {
            SEND_TRACKS_TO_SERVER = (boolean) sendTracksToServer;
        }

        Serializable randomDeviceUuid = StorageHandler.load(mainContext, StorageHandler.STORAGE_RANDOM_DEVICE_UUID);
        if (randomDeviceUuid != null) {
            RANDOM_DEVICE_UUID = (String) randomDeviceUuid;
        }

        Serializable useHttps = StorageHandler.load(mainContext, StorageHandler.STORAGE_USE_HTTPS);
        if (useHttps != null) {
            USE_HTTPS = (boolean) useHttps;
        }

        Serializable useDefaultServer = StorageHandler.load(mainContext, StorageHandler.STORAGE_USE_CUSTOM_SERVER);
        if (useDefaultServer != null) {
            USE_CUSTOM_SERVER = (boolean) useDefaultServer;
        }

        Serializable customServerUrl = StorageHandler.load(mainContext, StorageHandler.STORAGE_CUSTOM_SERVER_URL);
        if (customServerUrl != null) {
            CUSTOM_SERVER_URL = (String) customServerUrl;
        }

        Serializable customServerPort = StorageHandler.load(mainContext, StorageHandler.STORAGE_CUSTOM_SERVER_PORT);
        if (customServerPort != null) {
            CUSTOM_SERVER_PORT = (String) customServerPort;
        }

        Serializable customAcceptedCertificates = StorageHandler.load(mainContext, StorageHandler.STORAGE_CUSTOM_ACCEPTED_CERTIFICATES);
        if (customAcceptedCertificates != null) {
            CUSTOM_ACCEPTED_CERTIFICATES = (HashMap<String, Certificate[]>) customAcceptedCertificates;
        }

        Serializable trackExtraInformation = StorageHandler.load(mainContext, StorageHandler.STORAGE_TRACK_EXTRA_INFORMATION);
        if (trackExtraInformation != null) {
            TRACK_EXTRA_INFORMATION = (boolean) trackExtraInformation;
        }

    }

}
