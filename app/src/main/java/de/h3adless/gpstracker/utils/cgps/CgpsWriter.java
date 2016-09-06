package de.h3adless.gpstracker.utils.cgps;

import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        File dir = this.getStorageDirectory();
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
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd_hhmmss");
        dateFormatter.setLenient(false);
        Date today = new Date();
        String formatedDateTime = dateFormatter.format(today);
        return String.format("%s-%s.%s", this.FILE_NAME_PREFIX, formatedDateTime, this.FILE_EXT);
    }
}
