package de.h3adless.gpstracker.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.UUID;

import de.h3adless.gpstracker.AppSettings;
import de.h3adless.gpstracker.R;
import de.h3adless.gpstracker.services.TestLocationService;

/**
 * Created by H3ADLESS on 28.07.2016.
 */
public class TestMainActivity extends AppCompatActivity {

	private int startBatteryLvl = 0;
	private long startTimestamp = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final Button trackButton = (Button) findViewById(R.id.button_track_me);
		if (trackButton != null) {
			trackButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					toggleTracking();
					Log.d("TestMainActivity", "Track button was clicked and tracking is now: " + AppSettings.getTrackingEnabled());
				}
			});
		}
		AppSettings.setLocationServiceIntent(new Intent(this, TestLocationService.class));
		setButtonState();

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);


	}

	private void toggleTracking() {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = registerReceiver(null, ifilter);
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		float batteryPct = level / (float)scale;

		Log.d("TestMainActivity","Battery Level: " + level);
		Log.d("TestMainActivity","Battery Scale: " + scale);
		Log.d("TestMainActivity","Battery Pct: " + batteryPct);

		long time = System.currentTimeMillis();

		if (AppSettings.getTrackingEnabled()) {
			AppSettings.setTrackingEnabled(false);
			stopService(AppSettings.getLocationServiceIntent());
			Log.d("TestMainActivity", "Battery used while tracking for " + (time - startTimestamp) + " ms: " + (startBatteryLvl - level));
			Toast.makeText(this, "Battery used while tracking for "
					+ (time - startTimestamp)
					+ " ms: "
					+ (startBatteryLvl - level), Toast.LENGTH_LONG).show();
		} else {
			if (AppSettings.getRandomDeviceUuid() == null) {
				AppSettings.setRandomDeviceUuid(UUID.randomUUID().toString());
			}
			startBatteryLvl = level;
			startTimestamp = time;
			AppSettings.setTrackingEnabled(true);
			startService(AppSettings.getLocationServiceIntent());
		}
		setButtonState();
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


}
