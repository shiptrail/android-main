package de.h3adless.gpstracker.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by H3ADLESS on 28.07.2016.
 */
public class Queries {

    public static Map<Long, String> getTracks(Context context) {
        String[] entriesProjection = {
                TrackDatabase.TrackEntry._ID,
                TrackDatabase.TrackEntry.COLUMN_NAME_NAME
        };

        String sortOrder = TrackDatabase.TrackEntry._ID + " ASC";

        SQLiteDatabase db = TrackDatabaseHelper.getInstance(context).getReadableDatabase();
        Cursor c = db.query(
                TrackDatabase.TrackEntry.TABLE_NAME,
                entriesProjection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        HashMap<Long, String> result = new HashMap<>();

        while(c.moveToNext()) {
            Long id = c.getLong(c.getColumnIndex(TrackDatabase.TrackEntry._ID));
            String name =  c.getString(c.getColumnIndex(TrackDatabase.TrackEntry.COLUMN_NAME_NAME));
            result.put(id, name);
        }

        c.close();
        return result;
    }

//    public static Map<Long, Map> getLocationsByTrackID(Context context, Long trackID) {
//        String[] entriesProjection = {
//                TrackDatabase.LocationEntry._ID,
//                TrackDatabase.LocationEntry.COLUMN_NAME_LAT,
//                TrackDatabase.LocationEntry.COLUMN_NAME_LNG,
//                TrackDatabase.LocationEntry.COLUMN_NAME_ACCURACY,
//                TrackDatabase.LocationEntry.COLUMN_NAME_ALTITUDE,
//                TrackDatabase.LocationEntry.COLUMN_NAME_BEARING,
//                TrackDatabase.LocationEntry.COLUMN_NAME_SPEED,
//                TrackDatabase.LocationEntry.COLUMN_NAME_TIME,
//                TrackDatabase.LocationEntry.COLUMN_NAME_SAT_COUNT
//        };
//
//        String sortOrder = TrackDatabase.LocationEntry.COLUMN_NAME_TIME + " ASC";
//
//        String whereClause = TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID + " = ?";
//
//        SQLiteDatabase db = TrackDatabaseHelper.getInstance(context).getReadableDatabase();
//        Cursor c = db.query(
//                TrackDatabase.LocationEntry.TABLE_NAME,
//                entriesProjection,
//                whereClause,
//                new String[] {String.valueOf(trackID)},
//                null,
//                null,
//                sortOrder
//        );
//
//        HashMap<Long, Map> result = new HashMap<>();
//
//
//        while(c.moveToNext()) {
//            Long id = c.getLong(c.getColumnIndex(TrackDatabase.LocationEntry._ID));
//            HashMap<String, Object> entry = new HashMap<>();
//            entry.put(TrackDatabase.LocationEntry.COLUMN_NAME_LAT, c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_LAT));
//            entry.put(TrackDatabase.LocationEntry.COLUMN_NAME_LNG, c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_LNG));
//            result.put(id, entry);
//        }
//
//        c.close();
//        return result;
//    }

    public static List<TrackingLocation> getLocationsByTrackID(Context context, long trackID) {
        String[] entriesProjection = {
                TrackDatabase.LocationEntry._ID,
                TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID,
                TrackDatabase.LocationEntry.COLUMN_NAME_LAT,
                TrackDatabase.LocationEntry.COLUMN_NAME_LNG,
                TrackDatabase.LocationEntry.COLUMN_NAME_ACCURACY,
                TrackDatabase.LocationEntry.COLUMN_NAME_ALTITUDE,
                TrackDatabase.LocationEntry.COLUMN_NAME_BEARING,
                TrackDatabase.LocationEntry.COLUMN_NAME_SPEED,
                TrackDatabase.LocationEntry.COLUMN_NAME_TIME,
                TrackDatabase.LocationEntry.COLUMN_NAME_SAT_COUNT
        };

        String sortOrder = TrackDatabase.LocationEntry.COLUMN_NAME_TIME + " ASC";
        String whereClause = TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID + " = ?";

        SQLiteDatabase db = TrackDatabaseHelper.getInstance(context).getReadableDatabase();
        Cursor c = db.query(
                TrackDatabase.LocationEntry.TABLE_NAME,
                entriesProjection,
                whereClause,
                new String[] {String.valueOf(trackID)},
                null,
                null,
                sortOrder
        );

        List<TrackingLocation> locs = new ArrayList<>();

        while(c.moveToNext()) {
            TrackingLocation loc = new TrackingLocation();
            loc.trackId = c.getLong(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID));
            loc.id = c.getLong(c.getColumnIndex(TrackDatabase.LocationEntry._ID));
            loc.lat = c.getFloat(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_LAT));
            loc.lng = c.getFloat(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_LNG));
            loc.accuracy = c.getFloat(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_ACCURACY));
            loc.ele = c.getFloat(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_ALTITUDE));
            loc.heading = c.getFloat(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_BEARING));
            loc.speed = c.getFloat(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_SPEED));
            loc.timestamp = c.getLong(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_TIME));
            loc.satCount = c.getInt(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_SAT_COUNT));

            locs.add(loc);
        }

        c.close();
        return locs;
    }


}
