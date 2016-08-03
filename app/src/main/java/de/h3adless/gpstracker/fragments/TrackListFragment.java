package de.h3adless.gpstracker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.h3adless.gpstracker.R;
import de.h3adless.gpstracker.activities.LocationListActivity;
import de.h3adless.gpstracker.database.Queries;
import de.h3adless.gpstracker.database.TrackingLocation;

/**
 * Created by H3ADLESS (TH) on  28.07.2016.
 */
public class TrackListFragment extends Fragment {

    public TrackListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_track_list, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listView_tracks);

        final Map<Long, String> tracksMap = Queries.getTracks(getActivity());
        ArrayList<String> tracks = new ArrayList<>(tracksMap.values());

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_tracks, R.id.list_item_track_textView, tracks);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object[] ids = tracksMap.keySet().toArray();
                if (i < ids.length) {

                    long id = (long) ids[i];

                    List<TrackingLocation> result = Queries.getLocationsByTrackID(getActivity(), id);
                    int size = result.size();

                    Toast t = Toast.makeText(getActivity(), "Entry count: " + size, Toast.LENGTH_SHORT);
                    t.show();

                    Intent intent = new Intent(getActivity(), LocationListActivity.class);
                    intent.putExtra("ID", (Long) tracksMap.keySet().toArray()[i]);
                    startActivity(intent);
                }

            }
        });

        return rootView;
    }
}
