package de.h3adless.gpstracker.utils.cgps;

import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * Created by phiros on 05.09.16.
 */
public class CgpsWriter extends CgpsCommon {
    String FILE_NAME_PREFIX = "track";
    TrackPoint[] trackPoints;
    String logName = "";

    public CgpsWriter(TrackPoint... trackPoints) {
        this.trackPoints = trackPoints;
    }

    public boolean writeToFile() {
        boolean writeWasSuccessful = false;
        File dir = getStorageDirectory();

        //make sure directory exists
        if (!dir.exists() && !dir.mkdir()) {
            return false;
        }
        Log.d("CgpsWriter","writing to: " + dir.getAbsolutePath());
        try {
            Log.d("CgpsWriter","writing to: " + dir.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String lineTerminator = "\n";

        this.logName = buildFileName();
        File file = new File(dir, this.logName);
        Gson gson = new Gson();

        try {
            FileOutputStream fs = new FileOutputStream(file, true);
            String line;
            for(TrackPoint tp: this.trackPoints) {
                line = String.format("%s%s", gson.toJson(tp), lineTerminator);
                fs.write(line.getBytes());
            }
            fs.close();
            writeWasSuccessful = true;
        } catch (Exception e) {
            Log.d("CgpsWriter", e.getMessage());
            writeWasSuccessful = false;
        }
        return writeWasSuccessful;
    }

    private String buildFileName() {
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd_hhmmss", Locale.US);
        dateFormatter.setLenient(false);
        Date today = new Date();
        String formatedDateTime = dateFormatter.format(today);
        return String.format("%s-%s.%s", this.FILE_NAME_PREFIX, formatedDateTime, this.FILE_EXT);
    }
}
