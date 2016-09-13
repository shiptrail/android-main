package de.h3adless.gpstracker;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.support.test.rule.ActivityTestRule;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

import de.h3adless.gpstracker.activities.MainActivity;
import de.h3adless.gpstracker.database.Queries;
import de.h3adless.gpstracker.database.TrackDatabase;
import de.h3adless.gpstracker.database.TrackDatabaseHelper;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by timo_ on 13.09.2016.
 */
public class LocationServiceTest {

    MainActivity mainActivity;
    LocationManager lm;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<MainActivity>(MainActivity.class);

    @Before
    public void pre() {
        mainActivity = mActivityRule.getActivity();
        lm = (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);
        lm.addTestProvider(LocationManager.GPS_PROVIDER, false, true, false, false, true, true, true, 0, 1);

        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            lm.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        }

        lm.setTestProviderStatus(LocationManager.GPS_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
    }

    @Test
    public void locationServiceRunsInBackgroundTests() throws Throwable {

        // Get current count of tracks from database
        final Map<Long, String> tracksMapBefore = Queries.getTracks(mainActivity.getApplicationContext());
        int trackCountBefore = tracksMapBefore.size();

        // Click tracking button
        onView(withId(R.id.button_track_me)).perform(click());
        onView(withId(R.id.button_track_me)).check(matches(withText(R.string.now_tracking)));

        // Kill activity
        mainActivity.finish();

        // Create new location
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(1.0);
        location.setLongitude(2.0);
        location.setAccuracy(3.0f);
        location.setAltitude(4.0f);
        location.setBearing(5.0f);
        location.setSpeed(6.0f);
        Bundle b = new Bundle();
        b.putInt("satellites", 7);
        location.setExtras(b);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) location.setElapsedRealtimeNanos(51235);
        location.setTime(System.currentTimeMillis());

        // Send new location
        lm.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);

        // Wait a moment and then reopen activity
        Thread.sleep(12*1000);
        mActivityRule.launchActivity(new Intent(mActivityRule.getActivity(), MainActivity.class));

        // Disable tracking
        onView(withId(R.id.button_track_me)).perform(click());
        onView(withId(R.id.button_track_me)).check(matches(withText(R.string.track_me)));

        // Get new count
        final Map<Long, String> tracksMapAfter = Queries.getTracks(mainActivity.getApplicationContext());
        int trackCountAfter = tracksMapAfter.size();

        // Check that a new Track was created
        Assert.assertEquals(trackCountAfter, trackCountBefore+1);

        newTrackContainsLocation(location.getLatitude(), location.getLongitude(), location.getAltitude());
    }

    private boolean newTrackContainsLocation(double lat, double lng, double ele) {
        // Get cursor for new Track
        Cursor cTrack = getNewestTrack();
        long lastId = cTrack.getLong(cTrack.getColumnIndex(TrackDatabase.LocationEntry._ID));

        Cursor cLocations = getLocationCursor(lastId);

        double dbLat;
        double dbLng;
        double dbEle;

        boolean found = false;

        while (cLocations.moveToNext()) {
            dbLat = cLocations.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_LAT);
            dbLng = cLocations.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_LNG);
            dbEle = cLocations.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_ELE);

            if (dbLat == lat && dbLng == lng && dbEle == ele) {
                cTrack.close();
                cLocations.close();
                return true;
            }
        }

        cTrack.close();
        cLocations.close();
        return false;
    }

    private Cursor getNewestTrack(){
        String[] entriesProjection = {
                TrackDatabase.TrackEntry._ID,
                TrackDatabase.TrackEntry.COLUMN_NAME_NAME
        };

        String sortOrder = TrackDatabase.TrackEntry._ID + " ASC";
        SQLiteDatabase db = TrackDatabaseHelper.getInstance(mainActivity).getReadableDatabase();

        Cursor c = db.query(
                TrackDatabase.TrackEntry.TABLE_NAME,
                entriesProjection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        c.moveToLast();
        return c;
    }

    private Cursor getLocationCursor(long trackID){
        String[] entriesProjection = {
                TrackDatabase.LocationEntry._ID,
                TrackDatabase.LocationEntry.COLUMN_NAME_LAT,
                TrackDatabase.LocationEntry.COLUMN_NAME_LNG,
                TrackDatabase.LocationEntry.COLUMN_NAME_TIMESTAMP,
                TrackDatabase.LocationEntry.COLUMN_NAME_ELE
        };

        String sortOrder = TrackDatabase.LocationEntry.COLUMN_NAME_TIMESTAMP + " ASC";

        String whereClause = TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID + " = ?";

        SQLiteDatabase db = TrackDatabaseHelper.getInstance(mainActivity).getReadableDatabase();

        return db.query(
                TrackDatabase.LocationEntry.TABLE_NAME,
                entriesProjection,
                whereClause,
                new String[] {String.valueOf(trackID)},
                null,
                null,
                sortOrder
        );
    }


}
