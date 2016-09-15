package de.h3adless.gpstracker.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import de.h3adless.gpstracker.AppSettings;
import de.h3adless.gpstracker.R;
import de.h3adless.gpstracker.database.Queries;
import de.h3adless.gpstracker.database.TrackDatabase;
import de.h3adless.gpstracker.database.TrackDatabaseHelper;
import de.h3adless.gpstracker.services.LocationService;
import de.h3adless.gpstracker.utils.cgps.TrackPoint;

/**
 * Created by H3ADLESS on 28.07.2016.
 */
public class MainActivity extends MainActivityType {

    private final static String GPS = "GPS";
    private BroadcastReceiver broadcastReceiver;

    private LocationService locationService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button trackButton = (Button) findViewById(R.id.button_track_me);
        if (trackButton != null) {
            trackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleTracking();
                    Log.d(GPS, "Track button was clicked and tracking is now: " + AppSettings.getTrackingEnabled());
                }
            });
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Location location = (Location) intent.getExtras().get(LocationService.BROADCAST_LOCATION);
                if (location != null) {
                    updateUI((float) location.getLatitude(), (float)location.getLongitude());
                }
            }
        };
        AppSettings.setLocationServiceIntent(new Intent(this, LocationService.class));

        //If we are tracking, register the broadcastreceiver
        if (AppSettings.getTrackingEnabled()) {
            registerReceiver(broadcastReceiver, new IntentFilter(LocationService.BROADCAST_ACTION));
            setButtonState();
            makeAnnotationButtonsEnabled(true);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void toggleTracking() {
        if (AppSettings.getTrackingEnabled()) {
            disableTracking();
        } else {
            enableTracking();
        }
        setButtonState();
    }

    @Override
    public void onDestroy() {
        if(AppSettings.getTrackingEnabled()) {
            unregisterReceiver(broadcastReceiver);
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else  if (id == R.id.action_list) {
            Intent intent = new Intent(this, TrackListActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode != 0) return;
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(GPS, "Permission granted.");
        }
    }

    private void setButtonState() {
        final Button trackButton = (Button) findViewById(R.id.button_track_me);
        Drawable drawable;
        if (AppSettings.getTrackingEnabled()) {
            drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.rounded_button_active, null);
            trackButton.setText(R.string.now_tracking);
        } else {
            drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.rounded_button, null);
            trackButton.setText(R.string.track_me);
        }
        setBackground(trackButton, drawable);
    }

    private void setBackground(Button b, Drawable d) {
        int sdk = Build.VERSION.SDK_INT;
        if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
            b.setBackgroundDrawable(d);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            b.setBackground(d);
        }
    }

    // Check permission, enable gps, request location updates
    private void enableTracking() {
        // Check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return;
        }

        // Check if gps is enabled
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            showGpsAlert();
        }

        AppSettings.setTrackingEnabled(true);

        registerReceiver(broadcastReceiver, new IntentFilter(LocationService.BROADCAST_ACTION));

        // Insert track id
        if(AppSettings.getRandomDeviceUuid() == null) {
            AppSettings.setRandomDeviceUuid(UUID.randomUUID().toString());
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy - HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        String dateAsString = dateFormat.format(date);

        long trackId = Queries.insertTrack(getApplicationContext(), "Track vom " + dateAsString);

        Intent intent = AppSettings.getLocationServiceIntent();
        intent.putExtra(LocationService.TRACK_ID, trackId);
        AppSettings.setLocationServiceIntent(intent);

        startService(AppSettings.getLocationServiceIntent());
    }

    private void disableTracking() {

        stopService(AppSettings.getLocationServiceIntent());

        unregisterReceiver(broadcastReceiver);

        AppSettings.setTrackingEnabled(false);

        resetUI();
    }

    private void showGpsAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.enable_gps_message);
        // Add the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button - show settings:
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog, cancel the tracking
                toggleTracking();
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void makeAnnotationButtonsEnabled(boolean enabled) {
        findViewById(R.id.activity_main_button_jibe_end).setEnabled(enabled);
        findViewById(R.id.activity_main_button_jibe_middle).setEnabled(enabled);
        findViewById(R.id.activity_main_button_jibe_start).setEnabled(enabled);
        findViewById(R.id.activity_main_button_tacking_end).setEnabled(enabled);
        findViewById(R.id.activity_main_button_tacking_middle).setEnabled(enabled);
        findViewById(R.id.activity_main_button_tacking_start).setEnabled(enabled);
    }

    private void resetUI(){
        ((TextView) findViewById(R.id.activity_main_textview_latlng)).setText("");
        makeAnnotationButtonsEnabled(false);
    }

    private void updateUI(float lat, float lng){
        lat = Math.round(lat*100000.0f)/100000.0f;
        lng = Math.round(lng*100000.0f)/100000.0f;
        String txt = String.valueOf(lat) + " / " + String.valueOf(lng);
        ((TextView) findViewById(R.id.activity_main_textview_latlng)).setText(txt);
        findViewById(R.id.activity_main_layout_annotations).setOnTouchListener(null);
        makeAnnotationButtonsEnabled(true);
    }

    public void onAnnotationClick(View v) {
        Log.d("mainActivity","onAnnotationClick");
        if (!AppSettings.getTrackingEnabled()) {
            return;
        }
        String type = "";
        switch (v.getId()) {
            case R.id.activity_main_button_jibe_start:
                type = TrackPoint.Annotation.TYPE_START_JIBE;
                break;
            case R.id.activity_main_button_jibe_middle:
                type = TrackPoint.Annotation.TYPE_MID_JIBE;
                break;
            case R.id.activity_main_button_jibe_end:
                type = TrackPoint.Annotation.TYPE_END_JIBE;
                break;
            case R.id.activity_main_button_tacking_start:
                type = TrackPoint.Annotation.TYPE_START_TACKING;
                break;
            case R.id.activity_main_button_tacking_middle:
                type = TrackPoint.Annotation.TYPE_MID_TACKING;
                break;
            case R.id.activity_main_button_tacking_end:
                type = TrackPoint.Annotation.TYPE_END_TACKING;
                break;
            default:
                return;
        }

        Intent intent = new Intent(LocationService.BROADCAST_ANNOTATION);
        intent.putExtra(LocationService.BROADCAST_ANNOTATION_TYPE, type);
        intent.putExtra(LocationService.BROADCAST_ANNOTATION_TIMESTAMP, System.currentTimeMillis());
        sendBroadcast(intent);

        Toast.makeText(MainActivity.this, getString(R.string.activity_main_toast_annotation_clicked, type), Toast.LENGTH_SHORT).show();
    }


}
