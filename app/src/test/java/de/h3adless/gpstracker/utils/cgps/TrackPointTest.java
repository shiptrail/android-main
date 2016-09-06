package de.h3adless.gpstracker.utils.cgps;

import android.location.Location;

import com.google.gson.Gson;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;

import de.h3adless.gpstracker.AppSettings;
import de.h3adless.gpstracker.database.TrackingLocation;

/**
 * Created by phiros on 05.09.16.
 */
public class TrackPointTest {
    final static int SAT_COUNT = 1;
    final static float LAT = 10.0f;
    final static float LNG = 20.0f;
    final static float ACCURACY = 1.0f;
    final static float ELE = 30.0f;
    final static float HEADING = 40.0f;
    final static float SPEED = 50.0f;
    final static int TIMESTAMP = 123456;

    String JSON_FOR_TRACKING_LOCATION = "{\"lat\":10.0,\"lng\":20.0,\"timestamp\":123456,\"ele\":30.0," +
            "\"gpsMeta\":[{\"accuracy\":1.0,\"satCount\":1,\"toffset\":0}]," +
            "\"compass\":[],\"accelerometer\":[],\"orientation\":[]}";

    static TrackingLocation trackingLocation;

    private boolean trackPointsAreEqual(TrackPoint one, TrackPoint two) {
        return one.lat == two.lat &&
                one.lng == two.lng &&
                one.timestamp == two.timestamp &&
                one.ele == two.ele &&
                one.gpsMeta.size() == two.gpsMeta.size() &&
                one.accelerometer.size() == two.accelerometer.size() &&
                one.compass.size() == two.compass.size() &&
                one.orientation.size() == two.orientation.size();
    }

    @BeforeClass
    public static void setUp() {
        trackingLocation = new TrackingLocation();
        trackingLocation.satCount = SAT_COUNT;
        trackingLocation.lat = LAT;
        trackingLocation.lng = LNG;
        trackingLocation.accuracy = ACCURACY;
        trackingLocation.ele = ELE;
        trackingLocation.heading = HEADING;
        trackingLocation.speed = SPEED;
        trackingLocation.timestamp = TIMESTAMP;
    }

    @Test
    public void TransformTrackingLocationsToTrackPoints() throws Exception {
        TrackPoint expectedTrackPoint = new TrackPoint(LAT, LNG, TIMESTAMP, ELE);
        expectedTrackPoint.gpsMeta.add(expectedTrackPoint.createGpsMeta(ACCURACY, SAT_COUNT, 0));

        TrackPoint transformedTrackPoint = new TrackPoint(trackingLocation);
        assertTrue(trackPointsAreEqual(transformedTrackPoint, expectedTrackPoint));
    }

    @Test
    public void shouldBeAbleToTransformTrackPointsToJson() throws Exception {
        TrackPoint trackPoint = new TrackPoint(trackingLocation);

        Gson gson = new Gson();
        String json = gson.toJson(trackPoint);
        assertTrue(json.equals(JSON_FOR_TRACKING_LOCATION));
    }

    @Test
    public void shouldBeAbleToTransformJsonToTrackPoints() throws Exception {
        TrackPoint expectedTrackPoint = new TrackPoint(trackingLocation);

        Gson gson = new Gson();
        TrackPoint parsedTrackPoint = gson.fromJson(JSON_FOR_TRACKING_LOCATION, TrackPoint.class);
        assertTrue(trackPointsAreEqual(parsedTrackPoint, expectedTrackPoint));
    }
}
