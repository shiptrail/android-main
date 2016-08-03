package de.h3adless.gpstracker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by H3ADLESS on 24.07.2016.
 */
public class TrackDatabaseHelper extends SQLiteOpenHelper {

    private static TrackDatabaseHelper instance;

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Track.db";

    public static synchronized TrackDatabaseHelper getInstance(Context context) {
        if(instance == null) {
            instance = new TrackDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private TrackDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TrackDatabase.SQL_CREATE_TRACK_TABLE);
        db.execSQL(TrackDatabase.SQL_CREATE_LOCATION_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO implement on Upgrade
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(TrackDatabase.SQL_DELETE_LOCATION_TABLE);
        db.execSQL(TrackDatabase.SQL_DELETE_TRACK_TABLE);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO implement on Upgrade
        onUpgrade(db, oldVersion, newVersion);
    }

}
