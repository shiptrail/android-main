package de.h3adless.gpstracker;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;

import de.h3adless.gpstracker.activities.MainActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by timo_ on 16.08.2016.
 */
@RunWith(AndroidJUnit4.class)
public class TrackingTest { //extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mainActivity;
    private LocationManager lm;

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
    public void trackTest() throws InterruptedException {

        onView(withId(R.id.textViewAccuracy)).check(matches(withText("?")));
        onView(withId(R.id.textViewBearing)).check(matches(withText("?")));
        onView(withId(R.id.textViewLat)).check(matches(withText("?")));
        onView(withId(R.id.textViewLng)).check(matches(withText("?")));
        onView(withId(R.id.textViewSatellites)).check(matches(withText("?")));
        onView(withId(R.id.textViewSpeed)).check(matches(withText("?")));

        onView(withId(R.id.button_track_me)).perform(click());

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            location.setElapsedRealtimeNanos(51235);
        }
        location.setTime(System.currentTimeMillis());
        lm.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);

        Thread.sleep(100);

        onView(withId(R.id.textViewAccuracy)).check(matches(withText("3.0")));
        onView(withId(R.id.textViewBearing)).check(matches(withText("5.0")));
        onView(withId(R.id.textViewLat)).check(matches(withText("1.0")));
        onView(withId(R.id.textViewLng)).check(matches(withText("2.0")));
        onView(withId(R.id.textViewSatellites)).check(matches(withText("7")));
        onView(withId(R.id.textViewSpeed)).check(matches(withText("6.0")));
    }

}
