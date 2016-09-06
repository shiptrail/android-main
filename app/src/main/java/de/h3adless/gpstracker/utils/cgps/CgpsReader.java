package de.h3adless.gpstracker.utils.cgps;

import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;

/**
 * Created by phiros on 05.09.16.
 */
public class CgpsReader extends CgpsCommon {
    public static File[] getFiles() {
        return getStorageDirectory().listFiles();
    }

    public static List<TrackPoint> getTrackPoints(File file) throws FileNotFoundException {
        return getTrackPoints(getReaderFor(file));
    }

    public static List<TrackPoint> getTrackPoints(BufferedReader bufferedReader) throws FileNotFoundException {
        List<TrackPoint> trackPoints = new Vector();
        String json;
        Gson gson = new Gson();
        try {
            while ((json = bufferedReader.readLine())!= null) {
                json = json.replace("\n", "");
                trackPoints.add(gson.fromJson(json, TrackPoint.class));
            }
        } catch (IOException e) {
            Log.d("CgpsReader", e.getMessage());
        }

        return trackPoints;
    }

    public static BufferedReader getReaderFor(File file) throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

        return bufferedReader;
    }
}
