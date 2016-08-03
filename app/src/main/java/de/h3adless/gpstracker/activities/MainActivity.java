package de.h3adless.gpstracker.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.h3adless.gpstracker.R;
import de.h3adless.gpstracker.database.TrackDatabase;
import de.h3adless.gpstracker.database.TrackDatabaseHelper;
import de.h3adless.gpstracker.services.LocationService;

/**
 * Created by H3ADLESS on 28.07.2016.
 */
public class MainActivity extends AppCompatActivity {

    private final static String GPS = "GPS";
    private boolean trackingEnabled = false;
    private BroadcastReceiver broadcastReceiver;

    private Intent locationServiceIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button trackButton = (Button) findViewById(R.id.button_track_me);
        if (trackButton != null) {
            trackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    trackingEnabled = !trackingEnabled;
                    Log.d(GPS, "Track button was clicked and tracking is now: " + trackingEnabled);

                    if (trackingEnabled) {
                        enableTracking();
                        setButtonState();
                    } else {
                        disableTracking();
                        setButtonState();
                    }
                }
            });
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Location location = (Location) intent.getExtras().get(LocationService.BROADCAST_LOCATION);
                if (location != null) {
                    updateUI(location);
                }
            }
        };

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_main_menu);
        item.setVisible(false);
        invalidateOptionsMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
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
        if (trackingEnabled) {
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

        locationServiceIntent = new Intent(this, LocationService.class);
        registerReceiver(broadcastReceiver, new IntentFilter(LocationService.BROADCAST_ACTION));

        // Insert track id
        TrackDatabaseHelper trackDatabaseHelper = TrackDatabaseHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = trackDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy - HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        String dateAsString = dateFormat.format(date);

        values.put(TrackDatabase.TrackEntry.COLUMN_NAME_NAME, "Track vom " + dateAsString);
        long trackId = db.insert(TrackDatabase.TrackEntry.TABLE_NAME, null, values);
        locationServiceIntent.putExtra(LocationService.TRACK_ID, trackId);

        startService(locationServiceIntent);
    }

    private void disableTracking() {

        if(locationServiceIntent != null) {
            stopService(locationServiceIntent);
        }

        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }

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
                // User cancelled the dialog, so do nothing
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void resetUI(){
        TextView latText = (TextView) findViewById(R.id.textViewLat);
        TextView lngText = (TextView) findViewById(R.id.textViewLng);
        TextView accuracyText = (TextView) findViewById(R.id.textViewAccuracy);
        TextView speedText = (TextView) findViewById(R.id.textViewSpeed);
        TextView satellitesText = (TextView) findViewById(R.id.textViewSatellites);
        TextView bearingText = (TextView) findViewById(R.id.textViewBearing);

        if (latText != null) latText.setText(R.string.unknown_value);
        if (lngText != null) lngText.setText(R.string.unknown_value);
        if (accuracyText != null) accuracyText.setText(R.string.unknown_value);
        if (speedText != null) speedText.setText(R.string.unknown_value);
        if (satellitesText != null) satellitesText.setText(R.string.unknown_value);
        if (bearingText != null) bearingText.setText(R.string.unknown_value);
    }

    private void updateUI(Location location){
        TextView latText = (TextView) findViewById(R.id.textViewLat);
        TextView lngText = (TextView) findViewById(R.id.textViewLng);
        TextView accuracyText = (TextView) findViewById(R.id.textViewAccuracy);
        TextView speedText = (TextView) findViewById(R.id.textViewSpeed);
        TextView satellitesText = (TextView) findViewById(R.id.textViewSatellites);
        TextView bearingText = (TextView) findViewById(R.id.textViewBearing);

        if (latText != null) latText.setText(String.valueOf(location.getLatitude()));
        if (lngText != null) lngText.setText(String.valueOf(location.getLongitude()));
        if (accuracyText != null) accuracyText.setText(String.valueOf(location.getAccuracy()));
        if (speedText != null) speedText.setText(String.valueOf(location.getSpeed()));
        if (satellitesText != null) satellitesText.setText(String.valueOf(location.getExtras().getInt("satellites")));
        if (bearingText != null) bearingText.setText(String.valueOf(location.getBearing()));
    }

}
