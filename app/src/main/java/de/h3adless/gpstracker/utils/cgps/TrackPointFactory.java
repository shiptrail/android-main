package de.h3adless.gpstracker.utils.cgps;

import java.util.List;
import java.util.Vector;

import de.h3adless.gpstracker.database.TrackingLocation;

/**
 * Created by phiros on 05.09.16.
 */
public class TrackPointFactory {
    public static List<TrackPoint> buildFrom(TrackingLocation... trackingLocations) {
        List<TrackPoint> trackPoints = new Vector();
        for (TrackingLocation location: trackingLocations) {
            TrackPoint tp = new TrackPoint(location);
            trackPoints.add(tp);
        }

        return trackPoints;
    }
}
