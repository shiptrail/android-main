package de.h3adless.gpstracker.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import de.h3adless.gpstracker.AppSettings;
import de.h3adless.gpstracker.R;

/**
 * Created by Sebu on 16.08.2016.
 */
public class SettingsActivity extends AppCompatActivity {

	private EditText editTrackInterval;
	private EditText editSendTogether;
	private Switch switchSendTracksToServer;
	private Switch switchUseHttps;
	private Switch switchUseCustomServer;
	private LinearLayout layoutCustomServer;
	private EditText editCustomServerUrl;
	private EditText editCustomServerPort;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		editTrackInterval = (EditText) findViewById(R.id.activity_settings_edit_time_between_gps);
		editSendTogether = (EditText) findViewById(R.id.activity_settings_edit_amount_to_send_together);
		switchSendTracksToServer = (Switch) findViewById(R.id.activity_settings_switch_send_to_server);
		switchUseHttps = (Switch) findViewById(R.id.activity_settings_switch_use_https);
		switchUseCustomServer = (Switch) findViewById(R.id.activity_settings_switch_use_custom_server);
		layoutCustomServer = (LinearLayout) findViewById(R.id.activity_settings_custom_server_layout);
		editCustomServerUrl = (EditText) findViewById(R.id.activity_settings_edit_custom_server_ip);
		editCustomServerPort = (EditText) findViewById(R.id.activity_settings_edit_custom_server_port);

		editTrackInterval.setText(String.valueOf(AppSettings.getTrackingInterval()));
		editSendTogether.setText(String.valueOf(AppSettings.getSendTogether()));
		switchSendTracksToServer.setChecked(AppSettings.getSendTracksToServer());
		switchUseHttps.setChecked(AppSettings.getUseHttps());
		boolean useCustomServer = AppSettings.getUseCustomServer();
		switchUseCustomServer.setChecked(useCustomServer);
		if (useCustomServer) {
			layoutCustomServer.setVisibility(View.VISIBLE);
		} else {
			layoutCustomServer.setVisibility(View.INVISIBLE);
		}
		String customServerUrl = AppSettings.getCustomServerUrl();
		String customServerPort = AppSettings.getCustomServerPort();
		if (!customServerPort.equals("") && !customServerUrl.equals("")) {
			editCustomServerUrl.setText(AppSettings.getCustomServerUrl());
			editCustomServerPort.setText(AppSettings.getCustomServerPort());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_settings, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		int id = item.getItemId();
		
		switch (id) {
			case R.id.action_settings_confirm:
				//we dont want trackInterval or sendTogether to be empty. So we check it.
				String trackInterval = editTrackInterval.getText().toString();
				if (!trackInterval.equals("")) {
					AppSettings.setTrackingInterval(Long.parseLong(trackInterval));
				}
				String sendTogether = editSendTogether.getText().toString();
				if (!sendTogether.equals("")) {
					AppSettings.setSendTogether(Integer.parseInt(sendTogether));
				}

				AppSettings.setSendTracksToServer(switchSendTracksToServer.isChecked());
				AppSettings.setUseHttps(switchUseHttps.isChecked());

				boolean useCustomServer = switchUseCustomServer.isChecked();
				if (useCustomServer) {
					String customServerUrl = editCustomServerUrl.getText().toString();
					String customServerPort = editCustomServerPort.getText().toString();
					if (customServerPort.equals("") || customServerUrl.equals("")) {
						useCustomServer = false;
					} else {
						AppSettings.setCustomServerUrl(customServerUrl);
						AppSettings.setCustomServerPort(customServerPort);
					}
				}
				AppSettings.setUseCustomServer(useCustomServer);

				finish();
			case R.id.action_settings_cancel:
				finish();
		}

		return super.onOptionsItemSelected(item);
	}

	public void toggleCustomServerLayout(View view) {
		if (switchUseCustomServer.isChecked()) {
			layoutCustomServer.setVisibility(View.VISIBLE);
		} else {
			layoutCustomServer.setVisibility(View.INVISIBLE);
		}
	}

	public void deleteCustomCertificates(View view) {
		AppSettings.deleteCustomAcceptedCertificates();
		Toast.makeText(this, R.string.activity_settings_toast_deleted_successfully, Toast.LENGTH_SHORT).show();
	}
}
