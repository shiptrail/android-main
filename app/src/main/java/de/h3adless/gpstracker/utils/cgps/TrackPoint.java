package de.h3adless.gpstracker.utils.cgps;

import java.util.List;
import java.util.Vector;

import de.h3adless.gpstracker.database.TrackingLocation;

/**
 * Created by phiros on 05.09.16.
 */
public class TrackPoint {
    public float lat;
    public float lng;
    public long timestamp;
    public float ele; // Optional<Float> geht leider nicht weil API level >= 24 benötigt
    public List<GpsMeta> gpsMeta = new Vector();
    public List<Compass> compass = new Vector();
    public List<Accelerometer> accelerometer = new Vector();
    public List<Orientation> orientation = new Vector();

    public TrackPoint(TrackingLocation tl) {
        this(tl.lat, tl.lng, tl.timestamp, tl.ele);
        this.gpsMeta.add(new GpsMeta(tl));
    }

    public TrackPoint(float lat, float lng, long timestamp, float ele) {
        this.lat = lat;
        this.lng = lng;
        this.timestamp = timestamp;
        this.ele = ele;
    }

    public GpsMeta createGpsMeta(TrackingLocation tl) {
        return new GpsMeta(tl);
    }

    public GpsMeta createGpsMeta(float accuracy, int satCount, int toffset) {
        return new GpsMeta(accuracy, satCount, toffset);
    }

    public class GpsMeta extends MetaInfos {
        public float accuracy;
        public int satCount;

        public GpsMeta(TrackingLocation tl) {
            this.accuracy = tl.accuracy;
            this.satCount = tl.satCount;
            this.toffset = 0;
        }

        public GpsMeta(float accuracy, int satCount, int toffset) {
            this.accuracy = accuracy;
            this.satCount = satCount;
            this.toffset = toffset;
        }
    }

    public class Compass extends MetaInfos {
        public float deg;

        public Compass(float deg, int toffset) {
            this.deg = deg;
            this.toffset = toffset;
        }

        public Compass(TrackingLocation tl) {
            this.deg = tl.heading;
            this.toffset = 0;
        }
    }

    public class Accelerometer extends MetaInfos {
        public float x;
        public float y;
        public float z;
    }

    public class Orientation extends MetaInfos {
        public float azimuth;
        public float pitch;
        public float roll;
    }

    public class MetaInfos {
        public int toffset;
    }
}
