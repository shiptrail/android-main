package de.h3adless.gpstracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static long insertTrack(Context context, String name) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrackDatabase.TrackEntry.COLUMN_NAME_NAME, name);
        return database.insert(TrackDatabase.TrackEntry.TABLE_NAME, null, values);
    }

    public static List<TrackPoint> getLocationsByTrackID(Context context, long trackID) {
        String sql = "SELECT " +
                TrackDatabase.LocationEntry._ID + "," +
                TrackDatabase.LocationEntry.COLUMN_NAME_LAT + "," +
                TrackDatabase.LocationEntry.COLUMN_NAME_LNG + "," +
                TrackDatabase.LocationEntry.COLUMN_NAME_ELE + "," +
                TrackDatabase.LocationEntry.COLUMN_NAME_TIMESTAMP +
                " FROM " + TrackDatabase.LocationEntry.TABLE_NAME +
                " WHERE " + TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID + " =?" +
                " ORDER BY " + TrackDatabase.LocationEntry._ID;
        SQLiteDatabase db = TrackDatabaseHelper.getInstance(context).getWritableDatabase();
        Cursor c = db.rawQuery(sql, new String[] {String.valueOf(trackID)});
        List<TrackPoint> list = new ArrayList<>();
        if (!c.moveToFirst()) {
            c.close();
            return list;
        }
        do {
            float lat = c.getFloat(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_LAT));
            float lng = c.getFloat(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_LNG));
            long timestamp = c.getLong(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_TIMESTAMP));
            float ele = c.getFloat(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_ELE));
            TrackPoint tp = new TrackPoint(lat,lng,timestamp,ele);
            list.add(tp);
        } while (c.moveToNext());
        c.close();

        sql = "SELECT " +
                TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry._ID + "," +
                TrackDatabase.GpsMetaEntry.TABLE_NAME + "." + TrackDatabase.GpsMetaEntry.COLUMN_NAME_TOFFSET + "," +
                TrackDatabase.GpsMetaEntry.TABLE_NAME + "." + TrackDatabase.GpsMetaEntry.COLUMN_NAME_SATCOUNT + "," +
                TrackDatabase.GpsMetaEntry.TABLE_NAME + "." + TrackDatabase.GpsMetaEntry.COLUMN_NAME_ACCURACY +
                " FROM " + TrackDatabase.LocationEntry.TABLE_NAME +
                " JOIN " + TrackDatabase.GpsMetaEntry.TABLE_NAME +
                " ON " + TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry._ID +
                " = " + TrackDatabase.GpsMetaEntry.TABLE_NAME + "." + TrackDatabase.GpsMetaEntry.COLUMN_NAME_LOCATION_ID +
                " WHERE " + TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID + " = ?" +
                " ORDER BY " + TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry._ID;
        Cursor c1 = db.rawQuery(sql, new String[]{String.valueOf(trackID)});
        long lastLocID = -1;
        int locIndex = -1;
        if (!c1.moveToFirst()) {
            c1.close();
        } else {
            do {
                long locID = c1.getLong(c1.getColumnIndex(TrackDatabase.LocationEntry._ID));
                if (locID != lastLocID) {
                    locIndex++;
                    lastLocID = locID;
                }
                float accuracy = c1.getFloat(c1.getColumnIndex(TrackDatabase.GpsMetaEntry.COLUMN_NAME_ACCURACY));
                int satcount = c1.getInt(c1.getColumnIndex(TrackDatabase.GpsMetaEntry.COLUMN_NAME_SATCOUNT));
                int toffset = c1.getInt(c1.getColumnIndex(TrackDatabase.GpsMetaEntry.COLUMN_NAME_TOFFSET));
                TrackPoint.GpsMeta gpsMeta = new TrackPoint.GpsMeta(accuracy,satcount,toffset);
                list.get(locIndex).gpsMeta.add(gpsMeta);
            } while (c1.moveToNext());
        }
        c1.close();

        sql = "SELECT " +
                TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry._ID + "," +
                TrackDatabase.CompassEntry.TABLE_NAME + "." + TrackDatabase.CompassEntry.COLUMN_NAME_TOFFSET + "," +
                TrackDatabase.CompassEntry.TABLE_NAME + "." + TrackDatabase.CompassEntry.COLUMN_NAME_DEG +
                " FROM " + TrackDatabase.LocationEntry.TABLE_NAME +
                " JOIN " + TrackDatabase.CompassEntry.TABLE_NAME +
                " ON " + TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry._ID +
                " = " + TrackDatabase.CompassEntry.TABLE_NAME + "." + TrackDatabase.CompassEntry.COLUMN_NAME_LOCATION_ID +
                " WHERE " + TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID + " = ?" +
                " ORDER BY " + TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry._ID;
        Cursor c2 = db.rawQuery(sql, new String[]{String.valueOf(trackID)});
        lastLocID = -1;
        locIndex = -1;
        if (!c2.moveToFirst()) {
            c2.close();
        } else {
            do {
                long locID = c2.getLong(c2.getColumnIndex(TrackDatabase.LocationEntry._ID));
                if (locID != lastLocID) {
                    Log.d("Queries","insert locs fast. old locid: " + lastLocID + " new one: " + locID);
                    locIndex++;
                    lastLocID = locID;
                }
                float deg = c2.getFloat(c2.getColumnIndex(TrackDatabase.CompassEntry.COLUMN_NAME_DEG));
                int toffset = c2.getInt(c2.getColumnIndex(TrackDatabase.CompassEntry.COLUMN_NAME_TOFFSET));
                TrackPoint.Compass compass = new TrackPoint.Compass(deg,toffset);
                list.get(locIndex).compass.add(compass);
            } while (c2.moveToNext());
        }
        c2.close();

        sql = "SELECT " +
                TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry._ID + "," +
                TrackDatabase.AnnotationEntry.TABLE_NAME + "." + TrackDatabase.AnnotationEntry.COLUMN_NAME_TOFFSET + "," +
                TrackDatabase.AnnotationEntry.TABLE_NAME + "." + TrackDatabase.AnnotationEntry.COLUMN_NAME_TYPE +
                " FROM " + TrackDatabase.LocationEntry.TABLE_NAME +
                " JOIN " + TrackDatabase.AnnotationEntry.TABLE_NAME +
                " ON " + TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry._ID +
                " = " + TrackDatabase.AnnotationEntry.TABLE_NAME + "." + TrackDatabase.AnnotationEntry.COLUMN_NAME_LOCATION_ID +
                " WHERE " + TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID + " = ?" +
                " ORDER BY " + TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry._ID;
        Cursor c3 = db.rawQuery(sql, new String[]{String.valueOf(trackID)});
        lastLocID = -1;
        locIndex = -1;
        if (!c3.moveToFirst()) {
            c3.close();
        } else {
            do {
                long locID = c3.getLong(c3.getColumnIndex(TrackDatabase.LocationEntry._ID));
                if (locID != lastLocID) {
                    locIndex++;
                    lastLocID = locID;
                }
                String type = c3.getString(c3.getColumnIndex(TrackDatabase.AnnotationEntry.COLUMN_NAME_TYPE));
                int toffset = c3.getInt(c3.getColumnIndex(TrackDatabase.AnnotationEntry.COLUMN_NAME_TOFFSET));
                TrackPoint.Annotation annotation = new TrackPoint.Annotation(type,toffset);
                list.get(locIndex).annotation.add(annotation);
            } while (c3.moveToNext());
        }
        c3.close();

        sql = "SELECT " +
                TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry._ID + "," +
                TrackDatabase.AccelerometerEntry.TABLE_NAME + "." + TrackDatabase.AccelerometerEntry.COLUMN_NAME_TOFFSET + "," +
                TrackDatabase.AccelerometerEntry.TABLE_NAME + "." + TrackDatabase.AccelerometerEntry.COLUMN_NAME_X + "," +
                TrackDatabase.AccelerometerEntry.TABLE_NAME + "." + TrackDatabase.AccelerometerEntry.COLUMN_NAME_Y + "," +
                TrackDatabase.AccelerometerEntry.TABLE_NAME + "." + TrackDatabase.AccelerometerEntry.COLUMN_NAME_Z +
                " FROM " + TrackDatabase.LocationEntry.TABLE_NAME +
                " JOIN " + TrackDatabase.AccelerometerEntry.TABLE_NAME +
                " ON " + TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry._ID +
                " = " + TrackDatabase.AccelerometerEntry.TABLE_NAME + "." + TrackDatabase.AccelerometerEntry.COLUMN_NAME_LOCATION_ID +
                " WHERE " + TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID + " = ?" +
                " ORDER BY " + TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry._ID;
        Cursor c4 = db.rawQuery(sql, new String[]{String.valueOf(trackID)});
        lastLocID = -1;
        locIndex = -1;
        if (!c4.moveToFirst()) {
            c4.close();
        } else {
            do {
                long locID = c4.getLong(c4.getColumnIndex(TrackDatabase.LocationEntry._ID));
                if (locID != lastLocID) {
                    locIndex++;
                    lastLocID = locID;
                }
                float x = c4.getFloat(c4.getColumnIndex(TrackDatabase.AccelerometerEntry.COLUMN_NAME_X));
                float y = c4.getFloat(c4.getColumnIndex(TrackDatabase.AccelerometerEntry.COLUMN_NAME_Y));
                float z = c4.getFloat(c4.getColumnIndex(TrackDatabase.AccelerometerEntry.COLUMN_NAME_Z));
                int toffset = c4.getInt(c4.getColumnIndex(TrackDatabase.AccelerometerEntry.COLUMN_NAME_TOFFSET));
                TrackPoint.Accelerometer accelerometer = new TrackPoint.Accelerometer(x,y,z,toffset);
                list.get(locIndex).accelerometer.add(accelerometer);
            } while (c4.moveToNext());
        }
        c4.close();

        sql = "SELECT location._id,orientation.toffset,orientation.azimuth,orientation.pitch,orientation.roll FROM location JOIN orientation ON location._id = orientation.location_id WHERE location.track_id = ? ORDER BY location._id";
        Log.d("Queries","insert fast. sql : " + sql);
        sql = "SELECT " +
                TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry._ID + "," +
                TrackDatabase.OrientationEntry.TABLE_NAME + "." + TrackDatabase.OrientationEntry.COLUMN_NAME_TOFFSET + "," +
                TrackDatabase.OrientationEntry.TABLE_NAME + "." + TrackDatabase.OrientationEntry.COLUMN_NAME_AZIMUTH + "," +
                TrackDatabase.OrientationEntry.TABLE_NAME + "." + TrackDatabase.OrientationEntry.COLUMN_NAME_PITCH + "," +
                TrackDatabase.OrientationEntry.TABLE_NAME + "." + TrackDatabase.OrientationEntry.COLUMN_NAME_ROLL +
                " FROM " + TrackDatabase.LocationEntry.TABLE_NAME +
                " JOIN " + TrackDatabase.OrientationEntry.TABLE_NAME +
                " ON " + TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry._ID +
                " = " + TrackDatabase.OrientationEntry.TABLE_NAME + "." + TrackDatabase.OrientationEntry.COLUMN_NAME_LOCATION_ID +
                " WHERE " + TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID + " = ?" +
                " ORDER BY " + TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry._ID;
        Log.d("Queries","insert fast. sql2 : " + sql);
        Cursor c5 = db.rawQuery(sql, new String[]{String.valueOf(trackID)});
        lastLocID = -1;
        locIndex = -1;
        if (!c5.moveToFirst()) {
            c5.close();
        } else {
            do {
                long locID = c5.getLong(c5.getColumnIndex(TrackDatabase.LocationEntry._ID));
                if (locID != lastLocID) {
                    locIndex++;
                    lastLocID = locID;
                }
                float azimuth = c5.getFloat(c5.getColumnIndex(TrackDatabase.OrientationEntry.COLUMN_NAME_AZIMUTH));
                float pitch = c5.getFloat(c5.getColumnIndex(TrackDatabase.OrientationEntry.COLUMN_NAME_PITCH));
                float roll = c5.getFloat(c5.getColumnIndex(TrackDatabase.OrientationEntry.COLUMN_NAME_ROLL));
                int toffset = c5.getInt(c5.getColumnIndex(TrackDatabase.OrientationEntry.COLUMN_NAME_TOFFSET));
                TrackPoint.Orientation orientation = new TrackPoint.Orientation(azimuth,pitch,roll,toffset);
                list.get(locIndex).orientation.add(orientation);
            } while (c5.moveToNext());
        }
        c5.close();

        return list;
    }


    private static List<TrackPoint.GpsMeta> getGpsMetaByLocationID(Context context, long locationID) {
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

    private static List<TrackPoint.Compass> getCompassByLocationID(Context context, long locationID) {
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

    private static List<TrackPoint.Accelerometer> getAccelerometerByLocationID(Context context, long locationID) {
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

    private static List<TrackPoint.Orientation> getOrientationByLocationID(Context context, long locationID) {
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

    private static List<TrackPoint.Annotation> getAnnotationByLocationID(Context context, long locationID) {
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

    public static long insertLocation(Context context, long trackID, float lat, float lng, long timestamp, float ele) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID, trackID);
        values.put(TrackDatabase.LocationEntry.COLUMN_NAME_LAT, lat);
        values.put(TrackDatabase.LocationEntry.COLUMN_NAME_LNG, lng);
        values.put(TrackDatabase.LocationEntry.COLUMN_NAME_TIMESTAMP, timestamp);
        values.put(TrackDatabase.LocationEntry.COLUMN_NAME_ELE, ele);
        return database.insert(TrackDatabase.LocationEntry.TABLE_NAME, null, values);
    }

    public static void insertGpsMeta(Context context, long locationID, TrackPoint.GpsMeta... gpsMetas) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrackDatabase.GpsMetaEntry.COLUMN_NAME_LOCATION_ID, locationID);
        for (TrackPoint.GpsMeta gpsMeta : gpsMetas) {
            values.put(TrackDatabase.GpsMetaEntry.COLUMN_NAME_TOFFSET, gpsMeta.toffset);
            values.put(TrackDatabase.GpsMetaEntry.COLUMN_NAME_ACCURACY, gpsMeta.accuracy);
            values.put(TrackDatabase.GpsMetaEntry.COLUMN_NAME_SATCOUNT, gpsMeta.satCount);
            database.insert(TrackDatabase.GpsMetaEntry.TABLE_NAME, null, values);
        }
    }

    public static void insertOrientation(Context context, long locationID, TrackPoint.Orientation... orientations) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrackDatabase.OrientationEntry.COLUMN_NAME_LOCATION_ID, locationID);
        for (TrackPoint.Orientation orientation : orientations) {
            values.put(TrackDatabase.OrientationEntry.COLUMN_NAME_TOFFSET, orientation.toffset);
            values.put(TrackDatabase.OrientationEntry.COLUMN_NAME_AZIMUTH, orientation.azimuth);
            values.put(TrackDatabase.OrientationEntry.COLUMN_NAME_PITCH, orientation.pitch);
            values.put(TrackDatabase.OrientationEntry.COLUMN_NAME_ROLL, orientation.roll);
            database.insert(TrackDatabase.OrientationEntry.TABLE_NAME, null, values);
        }
    }

    public static void insertAnnotation(Context context, long locationID, TrackPoint.Annotation... annotations) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrackDatabase.AnnotationEntry.COLUMN_NAME_LOCATION_ID, locationID);
        for (TrackPoint.Annotation annotation : annotations) {
            values.put(TrackDatabase.AnnotationEntry.COLUMN_NAME_TOFFSET, annotation.toffset);
            values.put(TrackDatabase.AnnotationEntry.COLUMN_NAME_TYPE, annotation.type);
            database.insert(TrackDatabase.AnnotationEntry.TABLE_NAME, null, values);
        }
    }

    public static void insertAcceleration(Context context, long locationID, TrackPoint.Accelerometer... accelerometers) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrackDatabase.AccelerometerEntry.COLUMN_NAME_LOCATION_ID, locationID);
        for (TrackPoint.Accelerometer accelerometer : accelerometers) {
            values.put(TrackDatabase.AccelerometerEntry.COLUMN_NAME_TOFFSET, accelerometer.toffset);
            values.put(TrackDatabase.AccelerometerEntry.COLUMN_NAME_X, accelerometer.x);
            values.put(TrackDatabase.AccelerometerEntry.COLUMN_NAME_Y, accelerometer.y);
            values.put(TrackDatabase.AccelerometerEntry.COLUMN_NAME_Z, accelerometer.z);
            database.insert(TrackDatabase.AccelerometerEntry.TABLE_NAME, null, values);
        }
    }

    public static void insertCompass(Context context, long locationID, TrackPoint.Compass... compasses) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrackDatabase.CompassEntry.COLUMN_NAME_LOCATION_ID, locationID);
        for (TrackPoint.Compass compass : compasses) {
            values.put(TrackDatabase.CompassEntry.COLUMN_NAME_TOFFSET, compass.toffset);
            values.put(TrackDatabase.CompassEntry.COLUMN_NAME_DEG, compass.deg);
            database.insert(TrackDatabase.CompassEntry.TABLE_NAME, null, values);
        }
    }

    public static void insertFailedRequests(Context context, List<Long> locationIDs, int type) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrackDatabase.FailedRequestsEntry.COLUMN_NAME_TYPE, type);
        for (Long locationID : locationIDs) {
            values.put(TrackDatabase.FailedRequestsEntry.COLUMN_NAME_LOCATION_ID, locationID);
            database.insert(TrackDatabase.FailedRequestsEntry.TABLE_NAME, null, values);
        }
    }

    public static ArrayList<Long> getFailedRequestLocationIdsByTrackIdAndType(Context context, long trackID, int type) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();
        String sql = "SELECT " + TrackDatabase.FailedRequestsEntry.TABLE_NAME + "." + TrackDatabase.FailedRequestsEntry.COLUMN_NAME_LOCATION_ID
                + " FROM " + TrackDatabase.FailedRequestsEntry.TABLE_NAME
                + " INNER JOIN " + TrackDatabase.LocationEntry.TABLE_NAME
                + " ON " + TrackDatabase.FailedRequestsEntry.TABLE_NAME + "." + TrackDatabase.FailedRequestsEntry.COLUMN_NAME_LOCATION_ID
                + " = " + TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry._ID
                + " WHERE " + TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID
                + " = ?"
                + " AND " + TrackDatabase.FailedRequestsEntry.TABLE_NAME + "." + TrackDatabase.FailedRequestsEntry.COLUMN_NAME_TYPE
                + " = ?;";
        String[] params = new String[] {String.valueOf(trackID), String.valueOf(type)};
        Cursor c = database.rawQuery(sql, params);
        ArrayList<Long> locationIds = new ArrayList<>();
        if (!c.moveToFirst()) {
            c.close();
            return locationIds;
        }
        do {
            locationIds.add(c.getLong(c.getColumnIndex(TrackDatabase.FailedRequestsEntry.COLUMN_NAME_LOCATION_ID)));
        } while (c.moveToNext());
        c.close();
        return locationIds;
    }

    public static void deleteFailedRequestsByTrackIdAndType(Context context, long trackID, int type) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();
        String sql = "DELETE "
                + " FROM " + TrackDatabase.FailedRequestsEntry.TABLE_NAME
                + " WHERE " + TrackDatabase.FailedRequestsEntry.COLUMN_NAME_TYPE
                + " = ?"
                + " AND " + TrackDatabase.FailedRequestsEntry.COLUMN_NAME_LOCATION_ID
                + " IN ( SELECT " + TrackDatabase.FailedRequestsEntry.TABLE_NAME + "." + TrackDatabase.FailedRequestsEntry.COLUMN_NAME_LOCATION_ID
                + " FROM " + TrackDatabase.FailedRequestsEntry.TABLE_NAME
                + " INNER JOIN " + TrackDatabase.LocationEntry.TABLE_NAME
                + " ON " + TrackDatabase.FailedRequestsEntry.TABLE_NAME + "." + TrackDatabase.FailedRequestsEntry.COLUMN_NAME_LOCATION_ID
                + " = " + TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry._ID
                + " WHERE " + TrackDatabase.LocationEntry.TABLE_NAME + "." + TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID
                + " = ? );";
        String[] params = new String[] {String.valueOf(type), String.valueOf(trackID)};
        database.execSQL(sql, params);
    }

    public static TrackPoint getLocationByLocationId(Context context, long locationID) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();
        String sql = "SELECT * FROM " + TrackDatabase.LocationEntry.TABLE_NAME
                + " WHERE " + TrackDatabase.LocationEntry._ID
                + " = ?;";
        String[] params = new String[] {String.valueOf(locationID)};
        Cursor c = database.rawQuery(sql, params);
        TrackPoint trackPoint = null;
        if (c.moveToFirst()) {
            float lat = c.getFloat(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_LAT));
            float lng = c.getFloat(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_LNG));
            long timestamp = c.getLong(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_TIMESTAMP));
            float ele = c.getFloat(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_ELE));

            trackPoint = new TrackPoint(lat, lng, timestamp, ele);

            trackPoint.gpsMeta = getGpsMetaByLocationID(context, locationID);
            trackPoint.compass = getCompassByLocationID(context, locationID);
            trackPoint.accelerometer = getAccelerometerByLocationID(context, locationID);
            trackPoint.orientation = getOrientationByLocationID(context, locationID);
            trackPoint.annotation = getAnnotationByLocationID(context, locationID);
        }
        c.close();
        return trackPoint;
    }
}
