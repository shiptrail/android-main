package de.h3adless.gpstracker.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Random;

import de.h3adless.gpstracker.AppSettings;
import de.h3adless.gpstracker.utils.cgps.TrackPoint;

/**
 * Created by Sebu on 23.08.2016.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */
public class TestLocationService extends Service {

	private boolean running = false;

	private TrackPoint loc = new TrackPoint(52.30f, 13.52f, System.currentTimeMillis(), 10);

	private IBinder binder = new LocalBinder();

	public class LocalBinder extends Binder {
		public TestLocationService getService() {
			// Return this instance of LocalService so clients can call public methods
			return TestLocationService.this;
		}
	}


	private Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			Log.d("TestLocationService", "Runnable run.");
			if (AppSettings.getSendTracksToServer()) {
				HttpRequest httpRequest = new HttpRequest(TestLocationService.this);
				httpRequest.execute(loc);
			}

			if (running) {
				TestLocationService.this.run();
			}
		}
	};

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
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

	public int getRandomInt() {
		Random r = new Random();
		return r.nextInt();
	}
}
