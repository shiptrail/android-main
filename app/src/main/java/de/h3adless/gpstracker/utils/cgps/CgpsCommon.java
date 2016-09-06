package de.h3adless.gpstracker.utils.cgps;

import android.os.Environment;

import java.io.File;

/**
 * Created by phiros on 05.09.16.
 */
public class CgpsCommon {
    static final String STORAGE_DIRECTORY = "gpstracks";
    static final String FILE_EXT = "cgps";

    protected static File getStorageDirectory() {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), STORAGE_DIRECTORY);
        return file;
    }
}
