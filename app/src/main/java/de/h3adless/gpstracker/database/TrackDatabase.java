package de.h3adless.gpstracker.database;

import android.provider.BaseColumns;

/**
 * Created by H3ADLESS on 24.07.2016.
 */
public class TrackDatabase {

    private static final String INT_TYPE = " INTEGER";
    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";

    // ##### LOCATION - TABLE #####

    public static abstract class LocationEntry implements BaseColumns {
        public static final String TABLE_NAME = "location";
        public static final String COLUMN_NAME_TRACK_ID = "track_id";
        public static final String COLUMN_NAME_LAT = "lat";
        public static final String COLUMN_NAME_LNG = "lng";
        public static final String COLUMN_NAME_ACCURACY = "accuracy";
        public static final String COLUMN_NAME_BEARING = "bearing";
        public static final String COLUMN_NAME_SPEED = "speed";
        public static final String COLUMN_NAME_ALTITUDE = "altitude";
        public static final String COLUMN_NAME_SAT_COUNT = "sat_count";
        public static final String COLUMN_NAME_TIME = "time";
    }

    public static final String SQL_CREATE_LOCATION_TABLE =
            "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                    LocationEntry._ID + " INTEGER PRIMARY KEY," +
                    LocationEntry.COLUMN_NAME_TRACK_ID + INT_TYPE + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_LAT + REAL_TYPE + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_LNG + REAL_TYPE + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_ACCURACY + REAL_TYPE + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_BEARING + REAL_TYPE + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_SPEED + REAL_TYPE + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_ALTITUDE + REAL_TYPE + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_SAT_COUNT + INT_TYPE + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_TIME + INT_TYPE +
            " )";

    public static final String SQL_DELETE_LOCATION_TABLE =
            "DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME;



    // ##### TRACK - TABLE #####

    public static abstract class TrackEntry implements BaseColumns {
        public static final String TABLE_NAME = "track";
        public static final String COLUMN_NAME_NAME = "name";
    }

    public static final String SQL_CREATE_TRACK_TABLE =
            "CREATE TABLE " + TrackEntry.TABLE_NAME + " (" +
                    TrackEntry._ID + " INTEGER PRIMARY KEY," +
                    TrackEntry.COLUMN_NAME_NAME + TEXT_TYPE +
                    " )";

    public static final String SQL_DELETE_TRACK_TABLE =
            "DROP TABLE IF EXISTS " + TrackEntry.TABLE_NAME;

}
