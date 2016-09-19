package de.h3adless.gpstracker;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import de.h3adless.gpstracker.activities.MainActivity;
import de.h3adless.gpstracker.services.HttpRequest;
import de.h3adless.gpstracker.utils.cgps.TrackPoint;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Sebu on 05.09.2016.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */

@RunWith(AndroidJUnit4.class)
public class HttpTest {

	@Rule
	public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<MainActivity>(MainActivity.class);

	@Test
	public void httpTest() throws Exception {
		//set settings to let the request fail!
		AppSettings.setSendTracksToServer(true);
		AppSettings.setRandomDeviceUuid(UUID.randomUUID().toString());
		AppSettings.setUseHttps(true);
		AppSettings.setUseCustomServer(true);
		AppSettings.setCustomServerUrl("52.40.3");
		AppSettings.setCustomServerPort("25");

		HttpRequest httpRequest = new HttpRequest(mActivityRule.getActivity());
		httpRequest.execute(new TrackPoint(1,2,3,4));

		//wait some seconds to let the Request fail
		Thread.sleep(10000);

		//check if dialog is shown
		onView(withText(R.string.http_request_failed_title)).check(matches(isDisplayed()));

		//click on yes button to accept certificate
		onView(withText(R.string.no)).perform(click());

		//check if dialog is still shown
		onView(withText(R.string.http_request_failed_title)).check(doesNotExist());
	}

}
