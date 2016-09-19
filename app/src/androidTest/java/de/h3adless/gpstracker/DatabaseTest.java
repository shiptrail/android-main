package de.h3adless.gpstracker;

import android.database.sqlite.SQLiteDatabase;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;


import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;

import de.h3adless.gpstracker.activities.MainActivity;
import de.h3adless.gpstracker.database.Queries;
import de.h3adless.gpstracker.database.TrackDatabase;
import de.h3adless.gpstracker.database.TrackDatabaseHelper;
import de.h3adless.gpstracker.utils.cgps.TrackPoint;

/**
 * Created by Sebu on 13.09.2016.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

	private long TRACK_ID;
	private String TRACK_NAME = "Track vom 01.02.03 - 01:02:03";

	private float LAT = 1.0f;
	private float LNG = 2.0f;
	private long TIMESTAMP = 123;
	private float ELE = 3.0f;

	private int TOFFSET = 100;

	private float ACCURACY = 1.0f;
	private int SAT_COUNT = 5;

	private float X = -1.0f;
	private float Y = -2.0f;
	private float Z = -3.0f;

	private float DEG = 179f;

	private float AZIMUTH = 1.0f;
	private float PITCH = -1.0f;
	private float ROLL = 0.0f;

	private String TYPE = TrackPoint.Annotation.TYPE_START_JIBE;

	private TrackPoint trackPoint;
	private MainActivity mainActivity;
	private long locationId;

	private boolean trackPointsAreEqual(TrackPoint one, TrackPoint two) {
		return one.lat == two.lat &&
				one.lng == two.lng &&
				one.timestamp == two.timestamp &&
				one.ele == two.ele &&
				one.gpsMeta.size() == 1 && two.gpsMeta.size() == 1 &&
				gpsMetaAreEqual(one.gpsMeta.get(0), two.gpsMeta.get(0)) &&
				one.accelerometer.size() == 1 && two.accelerometer.size() == 1 &&
				accelerometerAreEqual(one.accelerometer.get(0), two.accelerometer.get(0)) &&
				one.annotation.size() == 1 && two.annotation.size() == 1 &&
				annotationAreEqual(one.annotation.get(0), two.annotation.get(0)) &&
				one.compass.size() == 1 && two.compass.size() == 1 &&
				compassAreEqual(one.compass.get(0), two.compass.get(0)) &&
				one.orientation.size() == 1 && two.orientation.size() == 1 &&
				orientationAreEqual(one.orientation.get(0), two.orientation.get(0));
	}

	private boolean gpsMetaAreEqual(TrackPoint.GpsMeta one, TrackPoint.GpsMeta two) {
		return one.toffset == two.toffset &&
				one.accuracy == two.accuracy &&
				one.satCount == two.satCount;
	}
	private boolean accelerometerAreEqual (TrackPoint.Accelerometer one, TrackPoint.Accelerometer two) {
		return one.toffset == two.toffset &&
				one.x == two.x &&
				one.y == two.y &&
				one.z == two.z;
	}
	private boolean annotationAreEqual (TrackPoint.Annotation one, TrackPoint.Annotation two) {
		return one.toffset == two.toffset &&
				one.type.equals(two.type);
	}
	private boolean compassAreEqual (TrackPoint.Compass one, TrackPoint.Compass two) {
		return one.toffset == two.toffset &&
				one.deg == two.deg;
	}
	private boolean orientationAreEqual (TrackPoint.Orientation one, TrackPoint.Orientation two) {
		return one.toffset == two.toffset &&
				one.azimuth == two.azimuth &&
				one.pitch == two.pitch &&
				one.roll == two.roll;
	}


	@Rule
	public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<MainActivity>(MainActivity.class);

	@Before
	public void pre() {
		mainActivity = mActivityRule.getActivity();

		trackPoint = new TrackPoint(LAT,LNG,TIMESTAMP,ELE);
		trackPoint.gpsMeta.add(new TrackPoint.GpsMeta(ACCURACY, SAT_COUNT, TOFFSET));
		trackPoint.accelerometer.add(new TrackPoint.Accelerometer(X, Y, Z, TOFFSET));
		trackPoint.annotation.add(new TrackPoint.Annotation(TYPE, TOFFSET));
		trackPoint.compass.add(new TrackPoint.Compass(DEG, TOFFSET));
		trackPoint.orientation.add(new TrackPoint.Orientation(AZIMUTH, PITCH, ROLL, TOFFSET));
	}

	@Test
	public void databaseTest() throws Exception{
		//insert Track
		TRACK_ID = Queries.insertTrack(mainActivity, TRACK_NAME);
		if (TRACK_ID == -1) {
			throw new Exception("insert Track did not work.");
		}

		//check TRACK_ID
		Map<Long, String> map = Queries.getTracks(mainActivity);
		if (!map.containsKey(TRACK_ID) || !map.get(TRACK_ID).equals(TRACK_NAME)) {
			throw new Exception("TRACK_ID or TRACK_NAME is wrong");
		}

		//insert our trackpoint
		locationId = Queries.insertLocation(mainActivity,
				TRACK_ID,
				trackPoint.lat,
				trackPoint.lng,
				trackPoint.timestamp,
				trackPoint.ele);

		//insert other stuff
		Queries.insertCompass(mainActivity, locationId, trackPoint.compass.get(0));
		Queries.insertAcceleration(mainActivity, locationId, trackPoint.accelerometer.get(0));
		Queries.insertAnnotation(mainActivity, locationId, trackPoint.annotation.get(0));
		Queries.insertGpsMeta(mainActivity, locationId, trackPoint.gpsMeta.get(0));
		Queries.insertOrientation(mainActivity, locationId, trackPoint.orientation.get(0));

		//check location
		List<TrackPoint> list = Queries.getLocationsByTrackID(mainActivity, TRACK_ID);
		if (list.size() != 1 || !trackPointsAreEqual(list.get(0), trackPoint)) {
			throw new Exception("TRACK_ID gets wrong location(s)!");
		}
	}

	@After
	public void post() {
		SQLiteDatabase db = TrackDatabaseHelper.getInstance(mainActivity).getWritableDatabase();

		//delete stuff from db.
		db.delete(TrackDatabase.TrackEntry.TABLE_NAME, TrackDatabase.TrackEntry._ID + "=" + TRACK_ID, null);
		db.delete(TrackDatabase.LocationEntry.TABLE_NAME, TrackDatabase.LocationEntry._ID + "=" + locationId, null);
		db.delete(TrackDatabase.GpsMetaEntry.TABLE_NAME, TrackDatabase.GpsMetaEntry.COLUMN_NAME_LOCATION_ID + "=" + locationId, null);
		db.delete(TrackDatabase.OrientationEntry.TABLE_NAME, TrackDatabase.OrientationEntry.COLUMN_NAME_LOCATION_ID + "=" + locationId, null);
		db.delete(TrackDatabase.AccelerometerEntry.TABLE_NAME, TrackDatabase.AccelerometerEntry.COLUMN_NAME_LOCATION_ID + "=" + locationId, null);
		db.delete(TrackDatabase.AnnotationEntry.TABLE_NAME, TrackDatabase.AnnotationEntry.COLUMN_NAME_LOCATION_ID + "=" + locationId, null);
		db.delete(TrackDatabase.CompassEntry.TABLE_NAME, TrackDatabase.CompassEntry.COLUMN_NAME_LOCATION_ID + "=" + locationId, null);
	}


}
