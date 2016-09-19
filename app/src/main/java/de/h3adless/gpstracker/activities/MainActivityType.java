package de.h3adless.gpstracker.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.h3adless.gpstracker.AppSettings;
import de.h3adless.gpstracker.R;
import de.h3adless.gpstracker.database.Queries;
import de.h3adless.gpstracker.database.TrackDatabase;
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
			final long trackID = intent.getLongExtra(AppSettings.INTENT_START_DIALOG_TRACKID, -1);
			showHttpsDialog(trackID);
		} else if (intent.getBooleanExtra(AppSettings.INTENT_START_CERTIFICATE_DIALOG, false)) {
			final long trackID = intent.getLongExtra(AppSettings.INTENT_START_DIALOG_TRACKID, -1);
			final Certificate[] certificates = (Certificate[]) intent.getSerializableExtra(AppSettings.INTENT_START_DIALOG_PARAMS);
			showCertificateDialog(certificates, trackID);
		} else if (intent.getBooleanExtra(AppSettings.INTENT_START_RETRY_DIALOG, false)) {
			final long trackID = intent.getLongExtra(AppSettings.INTENT_START_DIALOG_TRACKID, -1);
			final String errorMsg = intent.getStringExtra(AppSettings.INTENT_START_DIALOG_PARAMS);
			showRetryDialog(errorMsg, trackID);
		}

		//so we dont show dialog again
		intent.putExtra(AppSettings.INTENT_START_HTTPS_DIALOG, false);
		intent.putExtra(AppSettings.INTENT_START_CERTIFICATE_DIALOG, false);
		intent.putExtra(AppSettings.INTENT_START_RETRY_DIALOG, false);
		setIntent(intent);
	}

	public void showRetryDialog(final String errorMsg, final long trackID) {
		//dont try to send any new Requests right now unless user ends dialog
		AppSettings.setSendTracksToServer(false);

		final ArrayList<Long> retryLocationIds =
				Queries.getFailedRequestLocationIdsByTrackIdAndType(this, trackID, TrackDatabase.FailedRequestsEntry.TYPE_OTHER);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.http_request_failed_title);
		builder.setMessage(getString(R.string.http_request_failed, retryLocationIds.size(), errorMsg));
		builder.setCancelable(false);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				AppSettings.setSendTracksToServer(true);
				HttpRequest httpRequest = new HttpRequest(MainActivityType.this);
				httpRequest.trackId = trackID;
				httpRequest.locationIds = (ArrayList<Long>) retryLocationIds.clone();

				TrackPoint[] params = new TrackPoint[retryLocationIds.size()];
				for (int j = 0; j < retryLocationIds.size(); j++) {
					params[j] = Queries.getLocationByLocationId(MainActivityType.this, retryLocationIds.get(j));
				}
				httpRequest.execute(params);

				retryLocationIds.clear();
				Queries.deleteFailedRequestsByTrackIdAndType(MainActivityType.this, trackID, TrackDatabase.FailedRequestsEntry.TYPE_OTHER);
				dialogInterface.dismiss();
			}
		});
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				AppSettings.setSendTracksToServer(true);
				dialogInterface.dismiss();
			}
		});
		builder.show();
	}

	public void showCertificateDialog(final Certificate[] certificates, final long trackID) {
		//dont try to send any new Requests right now unless user clicks on yes
		AppSettings.setSendTracksToServer(false);

		final ArrayList<Long> retryLocationIds =
				Queries.getFailedRequestLocationIdsByTrackIdAndType(this, trackID, TrackDatabase.FailedRequestsEntry.TYPE_CERTIFICATE);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.certificate_failed_title);
		builder.setMessage(getString(R.string.certificate_failed, Arrays.toString(certificates)));
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				AppSettings.setSendTracksToServer(true);
				AppSettings.addCustomAcceptedCertificate(AppSettings.getCustomServerUrl(), certificates);

				HttpRequest httpRequest = new HttpRequest(MainActivityType.this);
				httpRequest.trackId = trackID;
				httpRequest.locationIds = retryLocationIds;

				TrackPoint[] params = new TrackPoint[retryLocationIds.size()];
				for (int j = 0; j < retryLocationIds.size(); j++) {
					params[j] = Queries.getLocationByLocationId(MainActivityType.this, retryLocationIds.get(j));
				}
				httpRequest.execute(params);

				retryLocationIds.clear();
				Queries.deleteFailedRequestsByTrackIdAndType(MainActivityType.this, trackID, TrackDatabase.FailedRequestsEntry.TYPE_CERTIFICATE);
				dialogInterface.dismiss();
			}
		});
		builder.setNegativeButton(R.string.no, null);
		builder.show();
	}

	public void showHttpsDialog(final long trackID) {
		//dont try to send any new Requests right now unless user clicks on yes
		AppSettings.setSendTracksToServer(false);

		final ArrayList<Long> retryLocationIds =
				Queries.getFailedRequestLocationIdsByTrackIdAndType(this, trackID, TrackDatabase.FailedRequestsEntry.TYPE_HTTPS);
		//create Dialog. If user clicks yes, we retry. If he clicks no, no more requests to server are done
		android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
		builder.setMessage(R.string.https_failed);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				AppSettings.setSendTracksToServer(true);
				AppSettings.setUseHttps(false);

				final HttpRequest httpRequest = new HttpRequest(MainActivityType.this);
				httpRequest.trackId = trackID;
				httpRequest.locationIds = retryLocationIds;

				TrackPoint[] params = new TrackPoint[retryLocationIds.size()];
				for (int j = 0; j < retryLocationIds.size(); j++) {
					params[j] = Queries.getLocationByLocationId(MainActivityType.this, retryLocationIds.get(j));
				}
				httpRequest.execute(params);

				retryLocationIds.clear();
				Queries.deleteFailedRequestsByTrackIdAndType(MainActivityType.this, trackID, TrackDatabase.FailedRequestsEntry.TYPE_HTTPS);
				dialogInterface.dismiss();
			}
		});
		builder.setNegativeButton(R.string.no, null);
		builder.show();
	}
}
