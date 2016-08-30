package de.h3adless.gpstracker;

import android.app.Activity;
import android.content.Intent;
import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleCallback;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitor;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.support.v7.view.menu.MenuView;
import android.util.Log;
import android.widget.EditText;
import android.widget.Switch;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import de.h3adless.gpstracker.activities.SettingsActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Sebu on 26.08.2016.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */

@RunWith(AndroidJUnit4.class)
public class SettingsTest {

	Switch switchSendToServer;
	EditText editTimeBetween;
	EditText editSendTogether;

	@Rule
	public ActivityTestRule<SettingsActivity> mActivityRule = new ActivityTestRule<SettingsActivity>(SettingsActivity.class);

	@Before
	public void pre() {
		SettingsActivity activity = mActivityRule.getActivity();

		switchSendToServer = (Switch) activity.findViewById(R.id.activity_settings_switch_send_to_server);
		editTimeBetween = (EditText) activity.findViewById(R.id.activity_settings_edit_time_between_gps);
		editSendTogether = (EditText) activity.findViewById(R.id.activity_settings_edit_amount_to_send_together);
	}

	@Test
	public void settingsTest() throws Throwable {
		//Create random content
		Random random = new Random();
		final boolean sendToServer = random.nextBoolean();
		final String timeBetween = String.valueOf((random.nextInt(5) + 1) * 1000);
		final String sendTogether = String.valueOf(random.nextInt(2) + 1);

		//fill settings with it
		mActivityRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				switchSendToServer.setChecked(sendToServer);
				editTimeBetween.setText(timeBetween);
				editSendTogether.setText(sendTogether);

			}
		});
		//save settings
		onView(withId(R.id.action_settings_confirm)).perform(click());

		//notify when activity is destroyed
		ActivityLifecycleMonitor monitor = ActivityLifecycleMonitorRegistry.getInstance();
		monitor.addLifecycleCallback(new ActivityLifecycleCallback() {
			@Override
			public void onActivityLifecycleChanged(Activity activity, Stage stage) {
				if (activity.getClass() == SettingsActivity.class && stage == Stage.DESTROYED) {
					synchronized (SettingsTest.this) {
						SettingsTest.this.notify();
						Log.d("SettingsTest","notified 1");
					}
				}
			}
		});

		synchronized (SettingsTest.this) {
			//wait until activity is destroyed
			SettingsTest.this.wait();

			//start new activity
			mActivityRule.launchActivity(new Intent(mActivityRule.getActivity(), SettingsActivity.class));

			//check settings
			onView(withId(R.id.activity_settings_switch_send_to_server)).check(matches(sendToServer ? isChecked() : isNotChecked()));
			onView(withId(R.id.activity_settings_edit_time_between_gps)).check(matches(withText(timeBetween)));
			onView(withId(R.id.activity_settings_edit_amount_to_send_together)).check(matches(withText(sendTogether)));
		}
	}


}
