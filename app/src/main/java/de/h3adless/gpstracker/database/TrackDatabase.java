package de.h3adless.gpstracker.database;

import android.provider.BaseColumns;
import android.provider.ContactsContract;

import de.h3adless.gpstracker.utils.cgps.TrackPoint;

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
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_ELE = "ele";
    }

    public static final String SQL_CREATE_LOCATION_TABLE =
            "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                    LocationEntry._ID + " INTEGER PRIMARY KEY," +
                    LocationEntry.COLUMN_NAME_TRACK_ID + INT_TYPE + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_LAT + REAL_TYPE + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_LNG + REAL_TYPE + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_TIMESTAMP + INT_TYPE + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_ELE + REAL_TYPE +
                    " )";

    public static final String SQL_DELETE_LOCATION_TABLE =
            "DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME;

    // ##### Abstract class for Meta-Tables #####

    public static abstract class MetaEntry implements BaseColumns {
        public static final String COLUMN_NAME_LOCATION_ID = "location_id";
        public static final String COLUMN_NAME_TOFFSET = "toffset";
    }

    // ##### GPS_META - TABLE #####

    public static abstract class GpsMetaEntry extends MetaEntry {
        public static final String TABLE_NAME = "gps_meta";
        public static final String COLUMN_NAME_ACCURACY = "accuracy";
        public static final String COLUMN_NAME_SATCOUNT = "satcount";

        public static final String SQL_CREATE_GPS_META_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                        COLUMN_NAME_LOCATION_ID + INT_TYPE + COMMA_SEP +
                        COLUMN_NAME_TOFFSET + INT_TYPE + COMMA_SEP +
                        COLUMN_NAME_ACCURACY + REAL_TYPE + COMMA_SEP +
                        COLUMN_NAME_SATCOUNT + INT_TYPE + COMMA_SEP +
                        "FOREIGN KEY(" + COLUMN_NAME_LOCATION_ID + ") REFERENCES " + LocationEntry.TABLE_NAME + "(" + LocationEntry._ID + ")" +
                        ")";

        public static final String SQL_DELETE_GPS_META_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    // ##### COMPASS - TABLE #####

    public static abstract class CompassEntry extends MetaEntry {
        public static final String TABLE_NAME = "compass";
        public static final String COLUMN_NAME_DEG = "deg";

        public static final String SQL_CREATE_COMPASS_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                        COLUMN_NAME_LOCATION_ID + INT_TYPE + COMMA_SEP +
                        COLUMN_NAME_TOFFSET + INT_TYPE + COMMA_SEP +
                        COLUMN_NAME_DEG + REAL_TYPE + COMMA_SEP +
                        "FOREIGN KEY(" + COLUMN_NAME_LOCATION_ID + ") REFERENCES " + LocationEntry.TABLE_NAME + "(" + LocationEntry._ID + ")" +
                        ")";

        public static final String SQL_DELETE_COMPASS_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    // ##### ACCELEROMETER - TABLE #####

    public static abstract class AccelerometerEntry extends MetaEntry {
        public static final String TABLE_NAME = "accelerometer";
        public static final String COLUMN_NAME_X = "x";
        public static final String COLUMN_NAME_Y = "y";
        public static final String COLUMN_NAME_Z = "z";

        public static final String SQL_CREATE_ACCELEROMETER_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                        COLUMN_NAME_LOCATION_ID + INT_TYPE + COMMA_SEP +
                        COLUMN_NAME_TOFFSET + INT_TYPE + COMMA_SEP +
                        COLUMN_NAME_X + REAL_TYPE + COMMA_SEP +
                        COLUMN_NAME_Y + REAL_TYPE + COMMA_SEP +
                        COLUMN_NAME_Z + REAL_TYPE + COMMA_SEP +
                        "FOREIGN KEY(" + COLUMN_NAME_LOCATION_ID + ") REFERENCES " + LocationEntry.TABLE_NAME + "(" + LocationEntry._ID + ")" +
                        ")";

        public static final String SQL_DELETE_ACCELEROMETER_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    // ##### ORIENTATION - TABLE #####

    public static abstract class OrientationEntry extends MetaEntry {
        public static final String TABLE_NAME = "orientation";
        public static final String COLUMN_NAME_AZIMUTH = "azimuth";
        public static final String COLUMN_NAME_PITCH = "pitch";
        public static final String COLUMN_NAME_ROLL = "roll";

        public static final String SQL_CREATE_ORIENTATION_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                        COLUMN_NAME_LOCATION_ID + INT_TYPE + COMMA_SEP +
                        COLUMN_NAME_TOFFSET + INT_TYPE + COMMA_SEP +
                        COLUMN_NAME_AZIMUTH + REAL_TYPE + COMMA_SEP +
                        COLUMN_NAME_PITCH + REAL_TYPE + COMMA_SEP +
                        COLUMN_NAME_ROLL + REAL_TYPE + COMMA_SEP +
                        "FOREIGN KEY(" + COLUMN_NAME_LOCATION_ID + ") REFERENCES " + LocationEntry.TABLE_NAME + "(" + LocationEntry._ID + ")" +
                        ")";

        public static final String SQL_DELETE_ORIENTATION_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    // ##### ANNOTATION-TABLE #####

    public static abstract class AnnotationEntry extends MetaEntry {
        public static final String TABLE_NAME = "annotation";
        public static final String COLUMN_NAME_TYPE = "type";

        public static final String SQL_CREATE_ANNOTATION_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                        COLUMN_NAME_LOCATION_ID + INT_TYPE + COMMA_SEP +
                        COLUMN_NAME_TOFFSET + INT_TYPE + COMMA_SEP +
                        COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
                        "FOREIGN KEY(" + COLUMN_NAME_LOCATION_ID + ") REFERENCES " + LocationEntry.TABLE_NAME + "(" + LocationEntry._ID + ")" +
                        ")";

        public static final String SQL_DELETE_ANNOTATION_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

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

    // ##### FAILED_REQUESTS - TABLE #####

    public static abstract class FailedRequestsEntry implements BaseColumns {
        public static final String TABLE_NAME = "failed_requests";
        public static final String COLUMN_NAME_LOCATION_ID = "location_id";
        public static final String COLUMN_NAME_TYPE = "type";

        public static final int TYPE_CERTIFICATE = 1;
        public static final int TYPE_HTTPS = 2;
        public static final int TYPE_OTHER = 3;

        public static final String SQL_CREATE_REQUESTS_FAILED_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                        COLUMN_NAME_LOCATION_ID + INT_TYPE + COMMA_SEP +
                        COLUMN_NAME_TYPE + INT_TYPE + COMMA_SEP +
                        "FOREIGN KEY(" + COLUMN_NAME_LOCATION_ID + ") REFERENCES " + LocationEntry.TABLE_NAME + "(" + LocationEntry._ID + ")" +
                        ")";

        public static final String SQL_DELETE_REQUESTS_FAILED_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

}
