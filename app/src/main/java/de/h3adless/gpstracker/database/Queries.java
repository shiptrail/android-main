package de.h3adless.gpstracker.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

        while(c.moveToNext()) {
            Long id = c.getLong(c.getColumnIndex(TrackDatabase.TrackEntry._ID));
            String name =  c.getString(c.getColumnIndex(TrackDatabase.TrackEntry.COLUMN_NAME_NAME));
            result.put(id, name);
        }

        c.close();
        return result;
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
                new String[] {String.valueOf(trackID)},
                null,
                null,
                sortOrder
        );

        List<TrackPoint> locs = new ArrayList<>();

        while(c.moveToNext()) {

            float lat = c.getFloat(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_LAT));
            float lng = c.getFloat(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_LNG));
            long timestamp = c.getLong(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_TIMESTAMP));
            float ele = c.getFloat(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_ELE));

            TrackPoint loc = new TrackPoint(lat,lng,timestamp,ele);

            int locationID = c.getInt(c.getColumnIndex(TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID));
            loc.gpsMeta = getGpsMetaByLocationID(context, locationID);
            loc.compass = getCompassByLocationID(context, locationID);
            loc.accelerometer = getAccelerometerByLocationID(context, locationID);
            loc.orientation = getOrientationByLocationID(context, locationID);

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

        Cursor c = database.rawQuery(sql, new String[] {String.valueOf(locationID)});

        List<TrackPoint.GpsMeta> gpsMetas = new ArrayList<>();

        if (!c.moveToFirst()) {
            return gpsMetas;
        }
        do {
            float accuracy = c.getFloat(c.getColumnIndex(TrackDatabase.GpsMetaEntry.COLUMN_NAME_ACCURACY));
            int toffset = c.getInt(c.getColumnIndex(TrackDatabase.GpsMetaEntry.COLUMN_NAME_TOFFSET));
            int satcount = c.getInt(c.getColumnIndex(TrackDatabase.GpsMetaEntry.COLUMN_NAME_SATCOUNT));

            gpsMetas.add(new TrackPoint.GpsMeta(accuracy,toffset,satcount));
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

        Cursor c = database.rawQuery(sql, new String[] {String.valueOf(locationID)});

        List<TrackPoint.Compass> compasses = new ArrayList<>();

        if (!c.moveToFirst()) {
            return compasses;
        }
        do {
            float deg = c.getFloat(c.getColumnIndex(TrackDatabase.CompassEntry.COLUMN_NAME_DEG));
            int toffset = c.getInt(c.getColumnIndex(TrackDatabase.CompassEntry.COLUMN_NAME_TOFFSET));

            compasses.add(new TrackPoint.Compass(deg,toffset));
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

        Cursor c = database.rawQuery(sql, new String[] {String.valueOf(locationID)});

        List<TrackPoint.Accelerometer> accelerometers = new ArrayList<>();

        if (!c.moveToFirst()) {
            return accelerometers;
        }
        do {
            float x = c.getFloat(c.getColumnIndex(TrackDatabase.AccelerometerEntry.COLUMN_NAME_X));
            float y = c.getFloat(c.getColumnIndex(TrackDatabase.AccelerometerEntry.COLUMN_NAME_Y));
            float z = c.getFloat(c.getColumnIndex(TrackDatabase.AccelerometerEntry.COLUMN_NAME_Z));
            int toffset = c.getInt(c.getColumnIndex(TrackDatabase.CompassEntry.COLUMN_NAME_TOFFSET));

            accelerometers.add(new TrackPoint.Accelerometer(x,y,z,toffset));
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

        Cursor c = database.rawQuery(sql, new String[] {String.valueOf(locationID)});

        List<TrackPoint.Orientation> orientations = new ArrayList<>();

        if (!c.moveToFirst()) {
            return orientations;
        }
        do {
            float azimuth = c.getFloat(c.getColumnIndex(TrackDatabase.OrientationEntry.COLUMN_NAME_AZIMUTH));
            float pitch = c.getFloat(c.getColumnIndex(TrackDatabase.OrientationEntry.COLUMN_NAME_PITCH));
            float roll = c.getFloat(c.getColumnIndex(TrackDatabase.OrientationEntry.COLUMN_NAME_ROLL));
            int toffset = c.getInt(c.getColumnIndex(TrackDatabase.CompassEntry.COLUMN_NAME_TOFFSET));

            orientations.add(new TrackPoint.Orientation(azimuth,pitch,roll,toffset));
        } while (c.moveToNext());
        c.close();
        return orientations;
    }

    public static List<TrackPoint.Orientation> getAnnotationByLocationID(Context context, int locationID) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getReadableDatabase();

        String sql = "SELECT " +
                TrackDatabase.OrientationEntry.COLUMN_NAME_TOFFSET + "," +
                TrackDatabase.OrientationEntry.COLUMN_NAME_AZIMUTH + "," +
                TrackDatabase.OrientationEntry.COLUMN_NAME_PITCH + "," +
                TrackDatabase.OrientationEntry.COLUMN_NAME_ROLL +
                " FROM " + TrackDatabase.OrientationEntry.TABLE_NAME +
                " WHERE " + TrackDatabase.OrientationEntry.COLUMN_NAME_LOCATION_ID + "= ?";

        Cursor c = database.rawQuery(sql, new String[] {String.valueOf(locationID)});

        List<TrackPoint.Orientation> orientations = new ArrayList<>();

        if (!c.moveToFirst()) {
            return orientations;
        }
        do {
            float azimuth = c.getFloat(c.getColumnIndex(TrackDatabase.OrientationEntry.COLUMN_NAME_AZIMUTH));
            float pitch = c.getFloat(c.getColumnIndex(TrackDatabase.OrientationEntry.COLUMN_NAME_PITCH));
            float roll = c.getFloat(c.getColumnIndex(TrackDatabase.OrientationEntry.COLUMN_NAME_ROLL));
            int toffset = c.getInt(c.getColumnIndex(TrackDatabase.CompassEntry.COLUMN_NAME_TOFFSET));

            orientations.add(new TrackPoint.Orientation(azimuth,pitch,roll,toffset));
        } while (c.moveToNext());
        c.close();
        return orientations;
    }

    public static int insertLocation(Context context, long trackID, float lat, float lng, long timestamp, float ele) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();

        String sql =
                "INSERT INTO " + TrackDatabase.LocationEntry.TABLE_NAME +
                        "("  + TrackDatabase.LocationEntry.COLUMN_NAME_TRACK_ID +
                        ","  + TrackDatabase.LocationEntry.COLUMN_NAME_LAT +
                        ","  + TrackDatabase.LocationEntry.COLUMN_NAME_LNG +
                        ","  + TrackDatabase.LocationEntry.COLUMN_NAME_TIMESTAMP +
                        ","  + TrackDatabase.LocationEntry.COLUMN_NAME_ELE +
                        ") VALUES (?,?,?,?,?);" +
                        "SELECT last_insert_rowid() FROM " + TrackDatabase.LocationEntry.TABLE_NAME;

        String[] args = new String[] {
                String.valueOf(trackID),
                String.valueOf(lat),
                String.valueOf(lng),
                String.valueOf(timestamp),
                String.valueOf(ele)
        };
        Cursor c = database.rawQuery(sql, args);

        int id = -1;

        if (!c.moveToFirst()) {
            c.close();
            return id;
        }
        id = c.getInt(0);
        c.close();
        return id;
    }

    public static void insertGpsMeta(Context context, int locationID, List<TrackPoint.GpsMeta> gpsMetas) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();

        String sql =
                "INSERT INTO " + TrackDatabase.GpsMetaEntry.TABLE_NAME +
                        "(" + TrackDatabase.GpsMetaEntry.COLUMN_NAME_LOCATION_ID +
                        "," + TrackDatabase.GpsMetaEntry.COLUMN_NAME_TOFFSET +
                        "," + TrackDatabase.GpsMetaEntry.COLUMN_NAME_ACCURACY +
                        "," + TrackDatabase.GpsMetaEntry.COLUMN_NAME_SATCOUNT +
                        ") VALUES (?,?,?,?)";

        String[] args = new String[4*gpsMetas.size()];

        for (int i = 0; i < gpsMetas.size(); i++) {
            TrackPoint.GpsMeta gpsMeta = gpsMetas.get(i);
            args[i*4] = String.valueOf(locationID);
            args[i*4+1] = String.valueOf(gpsMeta.toffset);
            args[i*4+2] = String.valueOf(gpsMeta.accuracy);
            args[i*4+3] = String.valueOf(gpsMeta.satCount);

            if (i == 0) {
                continue;
            }
            sql += ",(?,?,?,?)";
        }

        database.rawQuery(sql, args);
    }

    public static void insertOrientation(Context context, int locationID, List<TrackPoint.Orientation> orientations) {
        SQLiteDatabase database = TrackDatabaseHelper.getInstance(context).getWritableDatabase();

        String sql =
                "INSERT INTO " + TrackDatabase.OrientationEntry.TABLE_NAME +
                        "(" + TrackDatabase.OrientationEntry.COLUMN_NAME_LOCATION_ID +
                        "," + TrackDatabase.OrientationEntry.COLUMN_NAME_TOFFSET +
                        "," + TrackDatabase.OrientationEntry.COLUMN_NAME_AZIMUTH +
                        "," + TrackDatabase.OrientationEntry.COLUMN_NAME_PITCH +
                        "," + TrackDatabase.OrientationEntry.COLUMN_NAME_ROLL +
                        ") VALUES (?,?,?,?,?)";

        String[] args = new String[5*orientations.size()];

        for (int i = 0; i < orientations.size(); i++) {
            TrackPoint.Orientation orientation = orientations.get(i);
            args[i*5] = String.valueOf(locationID);
            args[i*5+1] = String.valueOf(orientation.toffset);
            args[i*5+2] = String.valueOf(orientation.azimuth);
            args[i*5+3] = String.valueOf(orientation.pitch);
            args[i*5+4] = String.valueOf(orientation.roll);

            if (i == 0) {
                continue;
            }
            sql += ",(?,?,?,?,?)";
        }

        database.rawQuery(sql, args);
    }
}
