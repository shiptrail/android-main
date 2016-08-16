package de.h3adless.gpstracker.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import de.h3adless.gpstracker.AppSettings;
import de.h3adless.gpstracker.R;

/**
 * Created by Sebu on 16.08.2016.
 */
public class SettingsActivity extends AppCompatActivity {
	
	private EditText editTrackInterval;
	private EditText editSendTogether;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		editTrackInterval = (EditText) findViewById(R.id.activity_settings_edit_time_between_gps);
		editSendTogether = (EditText) findViewById(R.id.activity_settings_edit_amount_to_send_together);
		
		editTrackInterval.setText(String.valueOf(AppSettings.getTrackingInterval()));
		editSendTogether.setText(String.valueOf(AppSettings.getSendTogether()));
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
				String trackInterval = editTrackInterval.getText().toString();
				if (!trackInterval.equals("")) {
					AppSettings.setTrackingInterval(Long.parseLong(trackInterval));
				}
				String sendTogether = editSendTogether.getText().toString();
				if (!sendTogether.equals("")) {
					AppSettings.setSendTogether(Integer.parseInt(sendTogether));
				}
				finish();	
			case R.id.action_settings_cancel:
				finish();
		}
		
		return super.onOptionsItemSelected(item);
	}

}
