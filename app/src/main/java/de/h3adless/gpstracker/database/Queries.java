package de.h3adless.gpstracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import de.h3adless.gpstracker.utils.cgps.TrackPoint;

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

        while (c.moveToNext()) {
            Long id = c.getLong(c.getColumnIndex(TrackDatabase.TrackEntry._ID));
            String name = c.getString(c.getColumnIndex(TrackDatabase.TrackEntry.COLUMN_NAME_NAME));
            result.put(id, name);
        }

        c.close();
        return result;
    }

    public static int insertTrack(Context context, String name) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrackDatabase.TrackEntry.COLUMN_NAME_NAME, name);
        long id = database.insert(TrackDatabase.TrackEntry.TABLE_NAME, null, values);
        if (id > Integer.MAX_VALUE) {
            return -1;
        } else {
            return (int) id;
        }
    }

    public static List<TrackPoint> getLocationsByTrackID(Context context, long trackID) {
        String[] entriesProjection = {
                TrackDatabase.LocationEntry._ID,
                TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID,
                TrackDatabase.LocationEntry.COLUMN_NAME_LAT,
                TrackDatabase.LocationEntry.COLUMN_NAME_LNG,
                TrackDatabase.LocationEntry.COLUMN_NAME_TIMESTAMP,
                TrackDatabase.LocationEntry.COLUMN_NAME_ELE
        };

        String sortOrder = TrackDatabase.LocationEntry.COLUMN_NAME_TIMESTAMP + " ASC";
        String whereClause = TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID + " = ?";

        SQLiteDatabase db = TrackDatabaseHelper.getInstance(context).getReadableDatabase();
        Cursor c = db.query(
                TrackDatabase.LocationEntry.TABLE_NAME,
                entriesProjection,
                whereClause,
                new String[]{String.valueOf(trackID)},
                null,
                null,
                sortOrder
        );

        List<TrackPoint> locs = new ArrayList<>();

        while (c.moveToNext()) {

            float lat = c.getFloat(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_LAT));
            float lng = c.getFloat(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_LNG));
            long timestamp = c.getLong(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_TIMESTAMP));
            float ele = c.getFloat(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_ELE));

            TrackPoint loc = new TrackPoint(lat, lng, timestamp, ele);

            int locationID = c.getInt(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID));
            loc.gpsMeta = getGpsMetaByLocationID(context, locationID);
            loc.compass = getCompassByLocationID(context, locationID);
            loc.accelerometer = getAccelerometerByLocationID(context, locationID);
            loc.orientation = getOrientationByLocationID(context, locationID);
            loc.annotation = getAnnotationByLocationID(context, locationID);

            locs.add(loc);
        }

        c.close();
        return locs;
    }

    public static List<TrackPoint.GpsMeta> getGpsMetaByLocationID(Context context, int locationID) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getReadableDatabase();

        String sql = "SELECT " +
                TrackDatabase.GpsMetaEntry.COLUMN_NAME_TOFFSET + "," +
                TrackDatabase.GpsMetaEntry.COLUMN_NAME_ACCURACY + "," +
                TrackDatabase.GpsMetaEntry.COLUMN_NAME_SATCOUNT +
                " FROM " + TrackDatabase.GpsMetaEntry.TABLE_NAME +
                " WHERE " + TrackDatabase.GpsMetaEntry.COLUMN_NAME_LOCATION_ID + "= ?";

        Cursor c = database.rawQuery(sql, new String[]{String.valueOf(locationID)});

        List<TrackPoint.GpsMeta> gpsMetas = new ArrayList<>();

        if (!c.moveToFirst()) {
            c.close();
            return gpsMetas;
        }
        do {
            float accuracy = c.getFloat(c.getColumnIndex(TrackDatabase.GpsMetaEntry.COLUMN_NAME_ACCURACY));
            int toffset = c.getInt(c.getColumnIndex(TrackDatabase.GpsMetaEntry.COLUMN_NAME_TOFFSET));
            int satcount = c.getInt(c.getColumnIndex(TrackDatabase.GpsMetaEntry.COLUMN_NAME_SATCOUNT));

            gpsMetas.add(new TrackPoint.GpsMeta(accuracy, satcount, toffset));
        } while (c.moveToNext());
        c.close();
        return gpsMetas;
    }

    public static List<TrackPoint.Compass> getCompassByLocationID(Context context, int locationID) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getReadableDatabase();

        String sql = "SELECT " +
                TrackDatabase.CompassEntry.COLUMN_NAME_TOFFSET + "," +
                TrackDatabase.CompassEntry.COLUMN_NAME_DEG +
                " FROM " + TrackDatabase.CompassEntry.TABLE_NAME +
                " WHERE " + TrackDatabase.CompassEntry.COLUMN_NAME_LOCATION_ID + "= ?";

        Cursor c = database.rawQuery(sql, new String[]{String.valueOf(locationID)});

        List<TrackPoint.Compass> compasses = new ArrayList<>();

        if (!c.moveToFirst()) {
            c.close();
            return compasses;
        }
        do {
            float deg = c.getFloat(c.getColumnIndex(TrackDatabase.CompassEntry.COLUMN_NAME_DEG));
            int toffset = c.getInt(c.getColumnIndex(TrackDatabase.CompassEntry.COLUMN_NAME_TOFFSET));

            compasses.add(new TrackPoint.Compass(deg, toffset));
        } while (c.moveToNext());
        c.close();
        return compasses;
    }

    public static List<TrackPoint.Accelerometer> getAccelerometerByLocationID(Context context, int locationID) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getReadableDatabase();

        String sql = "SELECT " +
                TrackDatabase.AccelerometerEntry.COLUMN_NAME_TOFFSET + "," +
                TrackDatabase.AccelerometerEntry.COLUMN_NAME_X + "," +
                TrackDatabase.AccelerometerEntry.COLUMN_NAME_Y + "," +
                TrackDatabase.AccelerometerEntry.COLUMN_NAME_Z +
                " FROM " + TrackDatabase.AccelerometerEntry.TABLE_NAME +
                " WHERE " + TrackDatabase.AccelerometerEntry.COLUMN_NAME_LOCATION_ID + "= ?";

        Cursor c = database.rawQuery(sql, new String[]{String.valueOf(locationID)});

        List<TrackPoint.Accelerometer> accelerometers = new ArrayList<>();

        if (!c.moveToFirst()) {
            c.close();
            return accelerometers;
        }
        do {
            float x = c.getFloat(c.getColumnIndex(TrackDatabase.AccelerometerEntry.COLUMN_NAME_X));
            float y = c.getFloat(c.getColumnIndex(TrackDatabase.AccelerometerEntry.COLUMN_NAME_Y));
            float z = c.getFloat(c.getColumnIndex(TrackDatabase.AccelerometerEntry.COLUMN_NAME_Z));
            int toffset = c.getInt(c.getColumnIndex(TrackDatabase.AccelerometerEntry.COLUMN_NAME_TOFFSET));

            accelerometers.add(new TrackPoint.Accelerometer(x, y, z, toffset));
        } while (c.moveToNext());
        c.close();
        return accelerometers;
    }

    public static List<TrackPoint.Orientation> getOrientationByLocationID(Context context, int locationID) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getReadableDatabase();

        String sql = "SELECT " +
                TrackDatabase.OrientationEntry.COLUMN_NAME_TOFFSET + "," +
                TrackDatabase.OrientationEntry.COLUMN_NAME_AZIMUTH + "," +
                TrackDatabase.OrientationEntry.COLUMN_NAME_PITCH + "," +
                TrackDatabase.OrientationEntry.COLUMN_NAME_ROLL +
                " FROM " + TrackDatabase.OrientationEntry.TABLE_NAME +
                " WHERE " + TrackDatabase.OrientationEntry.COLUMN_NAME_LOCATION_ID + "= ?";

        Cursor c = database.rawQuery(sql, new String[]{String.valueOf(locationID)});

        List<TrackPoint.Orientation> orientations = new ArrayList<>();

        if (!c.moveToFirst()) {
            c.close();
            return orientations;
        }
        do {
            float azimuth = c.getFloat(c.getColumnIndex(TrackDatabase.OrientationEntry.COLUMN_NAME_AZIMUTH));
            float pitch = c.getFloat(c.getColumnIndex(TrackDatabase.OrientationEntry.COLUMN_NAME_PITCH));
            float roll = c.getFloat(c.getColumnIndex(TrackDatabase.OrientationEntry.COLUMN_NAME_ROLL));
            int toffset = c.getInt(c.getColumnIndex(TrackDatabase.OrientationEntry.COLUMN_NAME_TOFFSET));

            orientations.add(new TrackPoint.Orientation(azimuth, pitch, roll, toffset));
        } while (c.moveToNext());
        c.close();
        return orientations;
    }

    public static List<TrackPoint.Annotation> getAnnotationByLocationID(Context context, int locationID) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getReadableDatabase();

        String sql = "SELECT " +
                TrackDatabase.AnnotationEntry.COLUMN_NAME_TOFFSET + "," +
                TrackDatabase.AnnotationEntry.COLUMN_NAME_TYPE +
                " FROM " + TrackDatabase.AnnotationEntry.TABLE_NAME +
                " WHERE " + TrackDatabase.AnnotationEntry.COLUMN_NAME_LOCATION_ID + "= ?";

        Cursor c = database.rawQuery(sql, new String[]{String.valueOf(locationID)});

        List<TrackPoint.Annotation> annotations = new ArrayList<>();

        if (!c.moveToFirst()) {
            c.close();
            return annotations;
        }
        do {
            String type = c.getString(c.getColumnIndex(TrackDatabase.AnnotationEntry.COLUMN_NAME_TYPE));
            int toffset = c.getInt(c.getColumnIndex(TrackDatabase.AnnotationEntry.COLUMN_NAME_TOFFSET));

            annotations.add(new TrackPoint.Annotation(type, toffset));
        } while (c.moveToNext());
        c.close();
        return annotations;
    }

    public static int insertLocation(Context context, long trackID, float lat, float lng, long timestamp, float ele) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID, trackID);
        values.put(TrackDatabase.LocationEntry.COLUMN_NAME_LAT, lat);
        values.put(TrackDatabase.LocationEntry.COLUMN_NAME_LNG, lng);
        values.put(TrackDatabase.LocationEntry.COLUMN_NAME_TIMESTAMP, timestamp);
        values.put(TrackDatabase.LocationEntry.COLUMN_NAME_ELE, ele);
        long id = database.insert(TrackDatabase.LocationEntry.TABLE_NAME, null, values);
        if (id > Integer.MAX_VALUE) {
            return -1;
        } else {
            return (int) id;
        }
    }

    public static void insertGpsMeta(Context context, int locationID, TrackPoint.GpsMeta... gpsMetas) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        for (TrackPoint.GpsMeta gpsMeta : gpsMetas) {
            values.put(TrackDatabase.GpsMetaEntry.COLUMN_NAME_LOCATION_ID, locationID);
            values.put(TrackDatabase.GpsMetaEntry.COLUMN_NAME_TOFFSET, gpsMeta.toffset);
            values.put(TrackDatabase.GpsMetaEntry.COLUMN_NAME_ACCURACY, gpsMeta.accuracy);
            values.put(TrackDatabase.GpsMetaEntry.COLUMN_NAME_SATCOUNT, gpsMeta.satCount);
        }
        database.insert(TrackDatabase.GpsMetaEntry.TABLE_NAME, null, values);
    }

    public static void insertOrientation(Context context, int locationID, TrackPoint.Orientation... orientations) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        for (TrackPoint.Orientation orientation : orientations) {
            values.put(TrackDatabase.OrientationEntry.COLUMN_NAME_LOCATION_ID, locationID);
            values.put(TrackDatabase.OrientationEntry.COLUMN_NAME_TOFFSET, orientation.toffset);
            values.put(TrackDatabase.OrientationEntry.COLUMN_NAME_AZIMUTH, orientation.azimuth);
            values.put(TrackDatabase.OrientationEntry.COLUMN_NAME_PITCH, orientation.pitch);
            values.put(TrackDatabase.OrientationEntry.COLUMN_NAME_ROLL, orientation.roll);
        }
        database.insert(TrackDatabase.OrientationEntry.TABLE_NAME, null, values);
    }

    public static void insertAnnotation(Context context, int locationID, TrackPoint.Annotation... annotations) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        for (TrackPoint.Annotation annotation : annotations) {
            values.put(TrackDatabase.AnnotationEntry.COLUMN_NAME_LOCATION_ID, locationID);
            values.put(TrackDatabase.AnnotationEntry.COLUMN_NAME_TOFFSET, annotation.toffset);
            values.put(TrackDatabase.AnnotationEntry.COLUMN_NAME_TYPE, annotation.type);
        }
        database.insert(TrackDatabase.AnnotationEntry.TABLE_NAME, null, values);
    }

    public static void insertAcceleration(Context context, int locationID, TrackPoint.Accelerometer... accelerometers) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        for (TrackPoint.Accelerometer accelerometer : accelerometers) {
            values.put(TrackDatabase.AccelerometerEntry.COLUMN_NAME_LOCATION_ID, locationID);
            values.put(TrackDatabase.AccelerometerEntry.COLUMN_NAME_TOFFSET, accelerometer.toffset);
            values.put(TrackDatabase.AccelerometerEntry.COLUMN_NAME_X, accelerometer.x);
            values.put(TrackDatabase.AccelerometerEntry.COLUMN_NAME_Y, accelerometer.y);
            values.put(TrackDatabase.AccelerometerEntry.COLUMN_NAME_Z, accelerometer.z);
        }
        database.insert(TrackDatabase.AccelerometerEntry.TABLE_NAME, null, values);
    }

    public static void insertCompass(Context context, int locationID, TrackPoint.Compass... compasses) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        for (TrackPoint.Compass compass : compasses) {
            values.put(TrackDatabase.CompassEntry.COLUMN_NAME_LOCATION_ID, locationID);
            values.put(TrackDatabase.CompassEntry.COLUMN_NAME_TOFFSET, compass.toffset);
            values.put(TrackDatabase.CompassEntry.COLUMN_NAME_DEG, compass.deg);
        }
        database.insert(TrackDatabase.CompassEntry.TABLE_NAME, null, values);
    }
}
