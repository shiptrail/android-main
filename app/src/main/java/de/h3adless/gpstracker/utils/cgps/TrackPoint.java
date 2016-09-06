package de.h3adless.gpstracker.utils.cgps;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

/**
 * Created by phiros on 05.09.16.
 */
public class TrackPoint implements Serializable{
    public float lat;
    public float lng;
    public long timestamp;
    public float ele; // Optional<Float> geht leider nicht weil API level >= 24 benötigt
    public List<GpsMeta> gpsMeta = new Vector();
    public List<Compass> compass = new Vector();
    public List<Accelerometer> accelerometer = new Vector();
    public List<Orientation> orientation = new Vector();
    public List<Annotation> annotation = new Vector();

    public TrackPoint(float lat, float lng, long timestamp, float ele) {
        this.lat = lat;
        this.lng = lng;
        this.timestamp = timestamp;
        this.ele = ele;
    }

    public GpsMeta createGpsMeta(float accuracy, int satCount, int toffset) {
        return new GpsMeta(accuracy, satCount, toffset);
    }

    public static class GpsMeta extends MetaInfos {
        public float accuracy;
        public int satCount;

        public GpsMeta(float accuracy, int satCount, int toffset) {
            this.accuracy = accuracy;
            this.satCount = satCount;
            this.toffset = toffset;
        }
    }

    public static class Compass extends MetaInfos {
        public float deg;

        public Compass(float deg, int toffset) {
            this.deg = deg;
            this.toffset = toffset;
        }
    }

    public static class Accelerometer extends MetaInfos {
        public float x;
        public float y;
        public float z;

        public Accelerometer(float x, float y, float z, int toffset) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.toffset = toffset;
        }
    }

    public static class Orientation extends MetaInfos {
        public float azimuth;
        public float pitch;
        public float roll;

        public Orientation(float azimuth, float pitch, float roll, int toffset) {
            this.azimuth = azimuth;
            this.pitch = pitch;
            this.roll = roll;
            this.toffset = toffset;
        }
    }

    public static class Annotation extends MetaInfos {
        public static final String TYPE_START_JIBE = "START_JIBE";
        public static final String TYPE_MID_JIBE = "MID_JIBE";
        public static final String TYPE_END_JIBE = "END_JIBE";
        public static final String TYPE_START_TACKING = "START_TACKING";
        public static final String TYPE_MID_TACKING = "MID_TACKING";
        public static final String TYPE_END_TACKING = "END_TACKING";

        public String type;

        public Annotation (String type, int toffset) {
            this.type = type;
            this.toffset = toffset;
        }
    }

    public static class MetaInfos {
        public int toffset;
    }
}
