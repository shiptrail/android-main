package de.h3adless.gpstracker;

import android.app.ActivityManager;
import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.h3adless.gpstracker.activities.SettingsActivity;
import de.h3adless.gpstracker.activities.TestMainActivity;
import de.h3adless.gpstracker.services.TestLocationService;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;

/**
 * Created by Sebu on 02.09.2016.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */

@RunWith(AndroidJUnit4.class)
public class TestMainActivityTest {

	@Rule
	public ActivityTestRule<TestMainActivity> mActivityRule = new ActivityTestRule<TestMainActivity>(TestMainActivity.class);

	@Test
	public void testMainActivityTest() {
		//check if button text is correct
		onView(withId(R.id.button_track_me)).check(matches(withText(R.string.track_me)));

		//perform a click
		onView(withId(R.id.button_track_me)).perform(click());

		//check button text again
		onView(withId(R.id.button_track_me)).check(matches(withText(R.string.now_tracking)));

		//check if service is running
		boolean isRunning = false;
		ActivityManager manager = (ActivityManager) mActivityRule.getActivity().getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (TestLocationService.class.getName().equals(service.service.getClassName())) {
				isRunning = true;
			}
		}
		assertEquals("Service Running",true,isRunning);

		//perform another click
		onView(withId(R.id.button_track_me)).perform(click());

		//check button text again
		onView(withId(R.id.button_track_me)).check(matches(withText(R.string.track_me)));
	}

}
