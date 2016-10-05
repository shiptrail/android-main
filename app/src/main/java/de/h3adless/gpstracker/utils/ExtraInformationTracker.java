package de.h3adless.gpstracker.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.h3adless.gpstracker.AppSettings;
import de.h3adless.gpstracker.R;

public class ExtraInformationTracker {

	private static final String DIRECTORY = "gpstracker_extra_information";
	private static FileWriter fileWriter = null;
	private static BufferedWriter writer = null;

	private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SSS z", Locale.US);

	/**
	 * Battery: no parameters needed.
	 * Network: [0]: Url
	 * 			[1]: parameter
	 * 			[2]: responsecode / error
	 * 			[3]: time needed for request / "" if failed
	 * ToggleTracking: [0]: enabled / disabled
	 */
	public enum ExtraInformationType {
		Battery, Network, ToggleTracking, Other
	}

	public static void init () throws Exception {
		File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), DIRECTORY);
		if (!dir.exists() && !dir.mkdir()) {
			throw new Exception("ExtraInformationTracker can not create directory for extra Information.");
		}
		File file = new File(dir, "information.txt");

		Log.d("ExtraInformationTracker","init. file: " + file.getAbsolutePath());

		//returns false if file already exists
		boolean append = !file.createNewFile();
		fileWriter = new FileWriter(file, append);
		writer = new BufferedWriter(fileWriter);
	}


	public static void track(Context context, ExtraInformationType type, String... params) {
		Log.d("ExtraInformationTracker","track type : " + type + " params : " + params);
		if (!AppSettings.getTrackExtraInformation()) {
			Log.d("ExtraInformationTracker","cancelling track");
			return;
		}

		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.extra_information_tracking_failed_title);
			builder.setMessage(e.getMessage());
			builder.setNeutralButton(R.string.ok, null);
			builder.show();
		}


		Calendar c = Calendar.getInstance();
		String prefix = sdf.format(c.getTime()) + ": ";

		try {

			switch (type) {
				case Battery:
					IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
					Intent batteryStatus = context.registerReceiver(null, ifilter);
					int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
					int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
					float batteryPct = level / (float) scale;
					Log.d("ExtraInformationTracker","writing battery");
					writer.write(prefix + "Battery Percent: " + batteryPct + "\n");
					break;
				case Network:
					writer.write(prefix + "Network: Url: " + params[0] + "\n");
					writer.write(prefix + "Network: Parameter: " + params[1] + "\n");
					writer.write(prefix + "Network: Parameter Size: " + params[1].getBytes(Charset.forName("UTF-8")).length + "\n");
					writer.write(prefix + "Network: Responsecode / Error: " + params[2] + "\n");
					writer.write(prefix + "Network: time needed (empty if failed): " + params[3] + "\n");
					break;
				case ToggleTracking:
					writer.write(prefix + "Tracking is now " + params[0] + "\n");
					break;
				case Other:
					for (String s : params) {
						writer.write(prefix + s + "\n");
					}
					break;
			}

			writer.close();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.extra_information_tracking_failed_title);
			builder.setMessage(e.getMessage());
			builder.setNeutralButton(R.string.ok, null);
			builder.show();
		}
	}

}