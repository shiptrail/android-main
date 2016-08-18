package de.h3adless.gpstracker;

import android.content.Context;
import android.os.Parcelable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Sebu on 16.08.2016.
 *
 * Simple Handler that saves and loads special storage data. Only use one of the Storage Types defined here.
 *
 */
public class StorageHandler {

	/**
	 * Type for Storage. Used for the boolean AppSettings.TRACKING_ENABLED
	 */
	public static final String STORAGE_TRACKING_BOOLEAN = "tracking_boolean";
	/**
	 * Type for Storage. Used for the Intent LOCATION_SERVICE_INTENT
	 */
	public static final String STORAGE_LOCATION_SERVICE_INTENT = "location_service_intent";
	/**
	 * Type for Storage. Used for the Long AppSettings.TRACKING_INTERVAL
	 */
	public static final String STORAGE_TRACKING_INTERVAL = "tracking_interval";
	/**
	 * Type for Storage. Used for the int AppSettings.SEND_TOGETHER
	 */
	public static final String STORAGE_SEND_TOGETHER = "send_together";
	/**
	 * Type for Storage. Used for the String AppSettings.SERVER_URL
	 */
	public static final String STORAGE_SERVER_URL = "server_url";
	/**
	 * Type for Storage. Used for the String AppSettings.SERVER_PORT
	 */
	public static final String STORAGE_SERVER_PORT = "server_port";

	/**
	 * @param context the context to use.
	 * @param type one of the Types for Storage (see at top)
	 * @param data the data to save
	 */
	public static void save(Context context, String type, Serializable data) {
		try {
			FileOutputStream fos = context.openFileOutput(type, Context.MODE_PRIVATE);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			ObjectOutputStream output = new ObjectOutputStream(bos);

			output.writeObject(data);

			output.close();
			bos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param context the context to use.
	 * @param type one of the Types for Stoarge (see at top)
	 * @return the data to load or null if exception
	 */
	public static Serializable load(Context context, String type) {
		try {
			FileInputStream fis = context.openFileInput(type);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ObjectInputStream input = new ObjectInputStream(bis);

			Serializable result = (Serializable) input.readObject();

			input.close();
			bis.close();
			fis.close();
			return result;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

}
