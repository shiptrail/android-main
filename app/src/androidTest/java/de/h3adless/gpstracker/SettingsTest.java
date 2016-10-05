package de.h3adless.gpstracker;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;
import android.widget.Switch;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import de.h3adless.gpstracker.activities.SettingsActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
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
	Switch switchUseHttps;
	Switch switchUseCustomServer;
	EditText editCustomIp;
	EditText editCustomPort;
	Switch switchTrackExtraInformation;

	@Rule
	public ActivityTestRule<SettingsActivity> mActivityRule = new ActivityTestRule<SettingsActivity>(SettingsActivity.class);

	@Before
	public void pre() {
		SettingsActivity activity = mActivityRule.getActivity();

		switchSendToServer = (Switch) activity.findViewById(R.id.activity_settings_switch_send_to_server);
		editTimeBetween = (EditText) activity.findViewById(R.id.activity_settings_edit_time_between_gps);
		editSendTogether = (EditText) activity.findViewById(R.id.activity_settings_edit_amount_to_send_together);
		switchUseHttps = (Switch) activity.findViewById(R.id.activity_settings_switch_use_https);
		switchUseCustomServer = (Switch) activity.findViewById(R.id.activity_settings_switch_use_custom_server);
		editCustomIp = (EditText) activity.findViewById(R.id.activity_settings_edit_custom_server_ip);
		editCustomPort = (EditText) activity.findViewById(R.id.activity_settings_edit_custom_server_port);
		switchTrackExtraInformation = (Switch) activity.findViewById(R.id.activity_settings_switch_track_extra_information);
	}

	@Test
	public void settingsTest() throws Throwable {
		//Create random content
		Random random = new Random();
		final boolean sendToServer = random.nextBoolean();
		final String timeBetween = String.valueOf((random.nextInt(5) + 1) * 1000);
		final String sendTogether = String.valueOf(random.nextInt(2) + 1);
		final boolean useHttps = random.nextBoolean();
		final String customIp = random.nextBoolean() ? "123.123.123.123" : "255.255.255.255";
		final String customPort = String.valueOf(random.nextInt(9000));
		final boolean trackExtraInformation = random.nextBoolean();

		//fill settings with it
		mActivityRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				switchSendToServer.setChecked(sendToServer);
				editTimeBetween.setText(timeBetween);
				editSendTogether.setText(sendTogether);
				switchUseHttps.setChecked(useHttps);
				switchUseCustomServer.setChecked(true);
				editCustomIp.setText(customIp);
				editCustomPort.setText(customPort);
				switchTrackExtraInformation.setChecked(trackExtraInformation);

			}
		});

		//save settings and destroy activity with that
		onView(withId(R.id.action_settings_confirm)).perform(click());

		//start new activity
		mActivityRule.launchActivity(new Intent(mActivityRule.getActivity(), SettingsActivity.class));

		//check settings
		onView(withId(R.id.activity_settings_switch_send_to_server)).check(matches(sendToServer ? isChecked() : isNotChecked()));
		onView(withId(R.id.activity_settings_edit_time_between_gps)).check(matches(withText(timeBetween)));
		onView(withId(R.id.activity_settings_edit_amount_to_send_together)).check(matches(withText(sendTogether)));
		onView(withId(R.id.activity_settings_switch_use_https)).check(matches(useHttps ? isChecked() : isNotChecked()));
		onView(withId(R.id.activity_settings_switch_use_custom_server)).check(matches(isChecked()));
		onView(withId(R.id.activity_settings_edit_custom_server_ip)).check(matches(withText(customIp)));
		onView(withId(R.id.activity_settings_edit_custom_server_port)).check(matches(withText(customPort)));
		onView(withId(R.id.activity_settings_switch_track_extra_information)).check(matches(trackExtraInformation ? isChecked() : isNotChecked()));
	}


}
