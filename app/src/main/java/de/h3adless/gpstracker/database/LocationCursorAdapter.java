package de.h3adless.gpstracker.database;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.h3adless.gpstracker.R;

/**
 * Created by H3ADLESS on 28.07.2016.
 */
public class LocationCursorAdapter extends CursorAdapter {

    public LocationCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_location, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvEle = (TextView) view.findViewById(R.id.list_item_location_ele_textView);
        TextView tvLat = (TextView) view.findViewById(R.id.list_item_location_lat_textView);
        TextView tvLng = (TextView) view.findViewById(R.id.list_item_location_lng_textView);
        TextView tvTime = (TextView) view.findViewById(R.id.list_item_location_time_textView);

        Float lat = cursor.getFloat(cursor.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_LAT));
        Float lng = cursor.getFloat(cursor.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_LNG));
        Long time = cursor.getLong(cursor.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_TIMESTAMP));
        Float ele = cursor.getFloat(cursor.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_ELE));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy \n HH:mm:ss:SSS", Locale.getDefault());
        Date date = new Date(time);
        String dateAsString = dateFormat.format(date);

        tvEle.setText(String.valueOf(ele));
        tvLat.setText(String.valueOf(lat));
        tvLng.setText(String.valueOf(lng));
        tvTime.setText(dateAsString);
    }
}
