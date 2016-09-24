package de.h3adless.gpstracker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Date;
import java.util.Map;

import de.h3adless.gpstracker.activities.LocationListActivity;
import de.h3adless.gpstracker.activities.MainActivity;
import de.h3adless.gpstracker.activities.SettingsActivity;
import de.h3adless.gpstracker.activities.TrackListActivity;
import de.h3adless.gpstracker.database.Queries;
import de.h3adless.gpstracker.database.TrackDatabase;
import de.h3adless.gpstracker.database.TrackDatabaseHelper;

import static android.app.PendingIntent.getActivity;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasType;
import static android.support.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static android.support.test.espresso.matcher.CursorMatchers.withRowString;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringStartsWith.startsWith;

/**
 * Created by timo_ on 20.09.2016.
 */

public class TrackViewsTest {

    private TrackListActivity trackListActivity;

    // TEST DATA
    private static String TEST_TRACK_NAME;
    private final static long TIMESTAMP = new Date().getTime();
    private final static float LAT = 1.0f;
    private final static float LNG = 2.0f;
    private final static float ELE = 3.0f;

    private long TRACK_ID;
    private long locationId;

    @Rule
    public IntentsTestRule<TrackListActivity> mActivityRule = new IntentsTestRule<TrackListActivity>(TrackListActivity.class, true, false);

    @Before
    public void pre() throws Exception {
        TEST_TRACK_NAME = "TEST TRACK " + new Date().getTime();

        TRACK_ID = Queries.insertTrack(getTargetContext(), TEST_TRACK_NAME);
        if (TRACK_ID == -1) {
            throw new Exception("insert Track did not work.");
        }

        locationId = Queries.insertLocation(getTargetContext(), TRACK_ID, LAT, LNG, TIMESTAMP, ELE);

    }

    @After
    public void post() {
        SQLiteDatabase db = TrackDatabaseHelper.getInstance(getTargetContext()).getWritableDatabase();

        //delete stuff from db.
        db.delete(TrackDatabase.TrackEntry.TABLE_NAME, TrackDatabase.TrackEntry._ID + "=" + TRACK_ID, null);
        db.delete(TrackDatabase.LocationEntry.TABLE_NAME, TrackDatabase.LocationEntry._ID + "=" + locationId, null);
    }

    @Test
    public void testTrackList() throws InterruptedException {
        // Start activity manually, else the entry isn't in the list, because it was created before.
        mActivityRule.launchActivity(new Intent());

        ListView tracks = (ListView)mActivityRule.getActivity().findViewById(R.id.listView_tracks);
        int itemIndex = clickItemWithName(tracks, TEST_TRACK_NAME);
        Assert.assertNotEquals(itemIndex, -1);

        onData(hasToString(startsWith(TEST_TRACK_NAME)))
                    .inAdapterView(withId(R.id.listView_tracks))
                    .atPosition(0)
                    .perform(click());


        Thread.sleep(500);

        // Check if new Activity was started
        intended(hasComponent(new ComponentName(getTargetContext(), LocationListActivity.class)));

        Thread.sleep(500);

        onView(withId(R.id.list)).check(matches(new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View view) {
                ListView listView = (ListView) view;
                View v = listView.getChildAt(0);
                TextView tvLat = (TextView) v.findViewById(R.id.list_item_location_lat_textView);
                TextView tvLng = (TextView) v.findViewById(R.id.list_item_location_lng_textView);
                TextView tvEle = (TextView) v.findViewById(R.id.list_item_location_ele_textView);

                Assert.assertEquals(String.valueOf(LAT), tvLat.getText());
                Assert.assertEquals(String.valueOf(LNG), tvLng.getText());
                Assert.assertEquals(String.valueOf(ELE), tvEle.getText());

                return true;
            }

            @Override
            public void describeTo(Description description) {
            }

        }));

        onView(withId(R.id.share_button)).perform(click());

        // Checks for the correct intent
        intended(allOf (hasAction (Intent.ACTION_CHOOSER), hasExtra(is(Intent.EXTRA_INTENT), allOf( hasAction(Intent.ACTION_SEND)))));

    }

    private int clickItemWithName(ListView list, String name) throws InterruptedException {
        for (int i = 0; i < list.getCount(); i++) {
            String item = (String)list.getItemAtPosition(i);
            if (item.matches(name)) {
                return i;
            }
        }
        return -1;
    }


}
