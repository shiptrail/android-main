package de.h3adless.gpstracker.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import java.security.cert.Certificate;
import java.util.Arrays;

import de.h3adless.gpstracker.AppSettings;
import de.h3adless.gpstracker.R;
import de.h3adless.gpstracker.services.HttpRequest;
import de.h3adless.gpstracker.utils.cgps.TrackPoint;

/**
 * Created by Sebu on 02.09.2016.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */
public class MainActivityType extends AppCompatActivity {

	@Override
	public void onResume() {
		super.onResume();
		Intent intent = getIntent();
		if (intent.getBooleanExtra(AppSettings.INTENT_START_HTTPS_DIALOG, false)) {
			final TrackPoint[] locations = (TrackPoint[]) intent.getSerializableExtra(AppSettings.INTENT_START_DIALOG_PARAMS);
			showHttpsDialog(locations);
		} else if (intent.getBooleanExtra(AppSettings.INTENT_START_CERTIFICATE_DIALOG, false)) {
			final TrackPoint[] locations = (TrackPoint[]) intent.getSerializableExtra(AppSettings.INTENT_START_DIALOG_PARAMS);
			final Certificate[] certificates = (Certificate[]) intent.getSerializableExtra(AppSettings.INTENT_START_CERTIFICATE_DIALOG_CERTIFICATES);
			showCertificateDialog(certificates, locations);
		} else if (intent.getBooleanExtra(AppSettings.INTENT_START_RETRY_DIALOG, false)) {
			final TrackPoint[] locations = (TrackPoint[]) intent.getSerializableExtra(AppSettings.INTENT_START_DIALOG_PARAMS);
			final String errorMsg = intent.getStringExtra(AppSettings.INTENT_START_RETRY_DIALOG_PARAMS);
			showRetryDialog(errorMsg, locations);
		}

		//so we dont show dialog again
		intent.putExtra(AppSettings.INTENT_START_HTTPS_DIALOG, false);
		intent.putExtra(AppSettings.INTENT_START_CERTIFICATE_DIALOG, false);
		intent.putExtra(AppSettings.INTENT_START_RETRY_DIALOG, false);
		setIntent(intent);
	}

	public void showRetryDialog(final String errorMsg, final TrackPoint... locations) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.http_request_failed_title);
		builder.setMessage(getString(R.string.http_request_failed, errorMsg));
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				HttpRequest httpRequest = new HttpRequest(MainActivityType.this);
				httpRequest.execute(locations);
				dialogInterface.dismiss();
			}
		});
		builder.setNegativeButton(R.string.no, null);
		builder.show();
	}

	public void showCertificateDialog(final Certificate[] certificates, final TrackPoint... locations) {
		//dont try to send any new Requests right now unless user clicks on yes
		AppSettings.setSendTracksToServer(false);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.certificate_failed_title);
		builder.setMessage(getString(R.string.certificate_failed, Arrays.toString(certificates)));
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				AppSettings.setSendTracksToServer(true);
				AppSettings.addCustomAcceptedCertificate(AppSettings.getCustomServerUrl(), certificates);
				HttpRequest httpRequest = new HttpRequest(MainActivityType.this);
				httpRequest.execute(locations);
				dialogInterface.dismiss();
			}
		});
		builder.setNegativeButton(R.string.no, null);
		builder.show();
	}

	public void showHttpsDialog(final TrackPoint... locations) {
		//dont try to send any new Requests right now unless user clicks on yes
		AppSettings.setSendTracksToServer(false);

		//create Dialog. If user clicks yes, we retry. If he clicks no, no more requests to server are done
		android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
		builder.setMessage(R.string.https_failed);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				AppSettings.setSendTracksToServer(true);
				AppSettings.setUseHttps(false);
				final HttpRequest httpRequest = new HttpRequest(MainActivityType.this);
				httpRequest.execute(locations);
				dialogInterface.dismiss();
			}
		});
		builder.setNegativeButton(R.string.no, null);
		builder.show();
	}
}
