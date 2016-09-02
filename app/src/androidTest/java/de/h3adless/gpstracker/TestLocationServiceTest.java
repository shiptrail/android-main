package de.h3adless.gpstracker;

import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

import de.h3adless.gpstracker.services.TestLocationService;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsInstanceOf.any;

/**
 * Created by Sebu on 02.09.2016.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */

@RunWith(AndroidJUnit4.class)
public class TestLocationServiceTest {

	@Rule
	public ServiceTestRule mServiceRule = new ServiceTestRule();

	@Test
	public void testLocationSerivceTest() throws TimeoutException {
		// Create the service Intent.
		Intent serviceIntent =
				new Intent(InstrumentationRegistry.getTargetContext(),
						TestLocationService.class);

		// Bind the service and grab a reference to the binder.
		IBinder binder = mServiceRule.bindService(serviceIntent);

		// Get the reference to the service, or you can call
		// public methods on the binder directly.
		TestLocationService service =
				((TestLocationService.LocalBinder) binder).getService();

		// Verify that the service is working correctly.
		assertThat(service.getRandomInt(), is(any(Integer.class)));
	}




}
