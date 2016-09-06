package de.h3adless.gpstracker.utils.cgps;

import android.os.Environment;

import java.io.File;

/**
 * Created by phiros on 05.09.16.
 */
public class CgpsCommon {
    static final String STORAGE_DIRECTORY = "gpstracks";
    static final String FILE_EXT = "cgps";

    //Because Environment.DIRECTORY_DOCUMENTS needs API 19, we use DIRECTORY_DOWNLOADS

    protected static File getStorageDirectory() {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), STORAGE_DIRECTORY);
        return file;
    }
}
