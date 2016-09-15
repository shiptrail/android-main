package de.h3adless.gpstracker.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import de.h3adless.gpstracker.R;
import de.h3adless.gpstracker.database.LocationCursorAdapter;
import de.h3adless.gpstracker.database.Queries;
import de.h3adless.gpstracker.database.TrackDatabase;
import de.h3adless.gpstracker.database.TrackDatabaseHelper;
import de.h3adless.gpstracker.utils.cgps.CgpsWriter;
import de.h3adless.gpstracker.utils.cgps.TrackPoint;

/**
 * Created by H3ADLESS (TH) on 28.07.2016.
 */
public class LocationListFragment extends Fragment {

    private final static int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    private long trackID;

    public LocationListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_location_list, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.list);

        Intent intent = getActivity().getIntent();
        this.trackID = (long) intent.getExtras().get("ID");

        Cursor c = getCursor(trackID);

        LocationCursorAdapter cursorAdapter = new LocationCursorAdapter(getContext(), c, 0);
        listView.setAdapter(cursorAdapter);

        Button sendButton = (Button) rootView.findViewById(R.id.send_button);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<TrackPoint> trackPointList = Queries.getLocationsByTrackID(getContext(), trackID);
                TrackPoint[] trackPoints = new TrackPoint[trackPointList.size()];
                trackPoints = trackPointList.toArray(trackPoints);
                CgpsWriter cgpsWriter = new CgpsWriter(trackPoints);
                boolean result = cgpsWriter.writeToFile();
                Toast.makeText(getContext(),
                        result ? R.string.location_list_fragment_saving_success : R.string.location_list_fragment_saving_failure,
                        Toast.LENGTH_LONG)
                        .show();
/*
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                } else {
                    writeTmpFileAndShare();
                }*/
            }
        });

        return rootView;
    }

    private void writeTmpFileAndShare(){
        File f = null;

        try {
            f = File.createTempFile("tmpTrack", ".json", new File(Environment.getExternalStorageDirectory().getAbsolutePath()));

            if (!f.exists() || !f.canRead()) {
                Toast.makeText(getContext(), "Attachment Error", Toast.LENGTH_SHORT).show();
            }

            writeTmpFile(f);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(f != null && f.exists() && f.canRead()) {
            Uri uri = Uri.parse("file://" + f.getPath());

            // SHARE FILE
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType("application/json");
            startActivity(Intent.createChooser(shareIntent, "Send Track To"));
        }
    }

    private void writeTmpFile(File f){
        List<TrackPoint> locs = Queries.getLocationsByTrackID(getContext(), trackID);
        Gson gson = new Gson();

        try {
            Writer writer = new FileWriter(f);
            gson.toJson(locs.toArray(), writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    writeTmpFileAndShare();
                } else {
                    Toast.makeText(getContext(), "Attachment Error", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private Cursor getCursor(long trackID){
        String[] entriesProjection = {
                TrackDatabase.LocationEntry._ID,
                TrackDatabase.LocationEntry.COLUMN_NAME_LAT,
                TrackDatabase.LocationEntry.COLUMN_NAME_LNG,
                TrackDatabase.LocationEntry.COLUMN_NAME_TIMESTAMP,
                TrackDatabase.LocationEntry.COLUMN_NAME_ELE
        };

        String sortOrder = TrackDatabase.LocationEntry.COLUMN_NAME_TIMESTAMP + " ASC";

        String whereClause = TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID + " = ?";

        SQLiteDatabase db = TrackDatabaseHelper.getInstance(getActivity()).getReadableDatabase();

        return db.query(
                TrackDatabase.LocationEntry.TABLE_NAME,
                entriesProjection,
                whereClause,
                new String[] {String.valueOf(trackID)},
                null,
                null,
                sortOrder
        );
    }

}
