package de.h3adless.gpstracker;

import android.content.Intent;
import android.location.Location;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.PreDestroy;

import de.h3adless.gpstracker.activities.MainActivity;
import de.h3adless.gpstracker.services.LocationService;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by Sebu on 11.09.2016.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */
@RunWith(AndroidJUnit4.class)
public class AnnotationsTest {

	@Rule
	public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<MainActivity>(MainActivity.class);

	@Before
	public void pre() {
		AppSettings.setTrackingEnabled(false);
	}

	@Test
	public void annotationsTest() throws InterruptedException {
		onView(withId(R.id.activity_main_button_jibe_end)).check(matches(not(isEnabled())));
		onView(withId(R.id.activity_main_button_jibe_middle)).check(matches(not(isEnabled())));
		onView(withId(R.id.activity_main_button_jibe_start)).check(matches(not(isEnabled())));
		onView(withId(R.id.activity_main_button_tacking_end)).check(matches(not(isEnabled())));
		onView(withId(R.id.activity_main_button_tacking_middle)).check(matches(not(isEnabled())));
		onView(withId(R.id.activity_main_button_tacking_start)).check(matches(not(isEnabled())));
		onView(withId(R.id.activity_main_textview_latlng)).check(matches(withText("")));

		onView(withId(R.id.button_track_me)).perform(click());

		Intent intent = new Intent(LocationService.BROADCAST_ACTION);
		Location location = new Location("");
		location.setLatitude(1.0);
		location.setLongitude(2.0);
		intent.putExtra(LocationService.BROADCAST_LOCATION, location);

		mActivityRule.getActivity().sendBroadcast(intent);

		Thread.sleep(1000);

		onView(withId(R.id.activity_main_button_jibe_end)).check(matches(isEnabled()));
		onView(withId(R.id.activity_main_button_jibe_middle)).check(matches(isEnabled()));
		onView(withId(R.id.activity_main_button_jibe_start)).check(matches(isEnabled()));
		onView(withId(R.id.activity_main_button_tacking_end)).check(matches(isEnabled()));
		onView(withId(R.id.activity_main_button_tacking_middle)).check(matches(isEnabled()));
		onView(withId(R.id.activity_main_button_tacking_start)).check(matches(isEnabled()));
		onView(withId(R.id.activity_main_textview_latlng)).check(matches(withText("1.0 / 2.0")));

		onView(withId(R.id.button_track_me)).perform(click());

		onView(withId(R.id.activity_main_button_jibe_end)).check(matches(not(isEnabled())));
		onView(withId(R.id.activity_main_button_jibe_middle)).check(matches(not(isEnabled())));
		onView(withId(R.id.activity_main_button_jibe_start)).check(matches(not(isEnabled())));
		onView(withId(R.id.activity_main_button_tacking_end)).check(matches(not(isEnabled())));
		onView(withId(R.id.activity_main_button_tacking_middle)).check(matches(not(isEnabled())));
		onView(withId(R.id.activity_main_button_tacking_start)).check(matches(not(isEnabled())));
		onView(withId(R.id.activity_main_textview_latlng)).check(matches(withText("")));
	}
}
