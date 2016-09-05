package de.h3adless.gpstracker;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import de.h3adless.gpstracker.activities.MainActivity;
import de.h3adless.gpstracker.database.TrackingLocation;
import de.h3adless.gpstracker.services.HttpRequest;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by Sebu on 05.09.2016.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */

@RunWith(AndroidJUnit4.class)
public class HttpsTest {

	@Rule
	public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<MainActivity>(MainActivity.class);

	@Test
	public void httpsTest() throws Exception {
		//delete custom certificates and do other settings
		AppSettings.deleteCustomAcceptedCertificates();
		AppSettings.setSendTracksToServer(true);
		AppSettings.setRandomDeviceUuid(UUID.randomUUID().toString());
		AppSettings.setUseHttps(true);
		AppSettings.setUseCustomServer(true);
		AppSettings.setCustomServerUrl(mActivityRule.getActivity().getString(R.string.activity_settings_custom_server_url_standard));
		AppSettings.setCustomServerPort(mActivityRule.getActivity().getString(R.string.activity_settings_custom_server_port_standard));

		HttpRequest httpRequest = new HttpRequest(mActivityRule.getActivity());
		httpRequest.execute(new TrackingLocation());

		//wait some seconds to let the Request fail
		Thread.sleep(10000);

		//check if dialog is shown
		onView(withText(R.string.certificate_failed_title)).check(matches(isDisplayed()));

		//click on yes button to accept certificate
		onView(withText(R.string.yes)).perform(click());

		//check if dialog is still shown
		onView(withText(R.string.certificate_failed_title)).check(doesNotExist());

		//check if we have a certificate saved now
		if (AppSettings.getCustomAcceptedCertificates().isEmpty()) {
			throw new Exception("No Certificate saved.");
		}
	}

}
