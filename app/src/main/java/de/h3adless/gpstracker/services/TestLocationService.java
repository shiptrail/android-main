package de.h3adless.gpstracker.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

import de.h3adless.gpstracker.AppSettings;
import de.h3adless.gpstracker.database.TrackingLocation;

/**
 * Created by Sebu on 23.08.2016.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */
public class TestLocationService extends Service {

	private boolean running = false;

	private TrackingLocation loc = new TrackingLocation();

	private Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			Log.d("TestLocationService", "Runnable run.");
			HttpRequest httpRequest = new HttpRequest();
			httpRequest.execute(loc);

			if (running) {
				TestLocationService.this.run();
			}
		}
	};

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		loc.lat = 52.30f;
		loc.lng = 13.52f;
		loc.ele = 10;
		loc.heading = 10;
		loc.timestamp = 10;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		running = true;
		run();

		return START_STICKY;
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		running = false;
		handler.removeCallbacks(runnable);
	}

	private void run() {
		handler.postDelayed(runnable, 5000);
	}
}
