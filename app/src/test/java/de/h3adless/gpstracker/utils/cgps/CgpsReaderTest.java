package de.h3adless.gpstracker.utils.cgps;

import org.junit.Test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;

/**
 * Created by phiros on 05.09.16.
 */
public class CgpsReaderTest {
    String fileContents = "{\"lat\":52.499371748417616,\"lng\":13.199539985507727,\"timestamp\":1336204772,\"ele\":23.799999237060547,\"gpsMeta\":[],\"compass\":[],\"accelerometer\":[],\"orientation\":[]}\n" +
            "{\"lat\":52.4994726665318,\"lng\":13.199675856158137,\"timestamp\":1336204779,\"ele\":23.799999237060547,\"gpsMeta\":[],\"compass\":[],\"accelerometer\":[],\"orientation\":[]}\n" +
            "{\"lat\":52.49953796155751,\"lng\":13.19979035295546,\"timestamp\":1336204785,\"ele\":23.799999237060547,\"gpsMeta\":[],\"compass\":[],\"accelerometer\":[],\"orientation\":[]}";

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

    @Test
    public void shouldbeAbleToReadTheFormat() throws Exception {
        List<TrackPoint> expectedTrackPoints = new Vector();
        expectedTrackPoints.add(new TrackPoint(52.49937f, 13.19954f, 1336204772, 23.8f));
        expectedTrackPoints.add(new TrackPoint(52.499474f, 13.199676f, 1336204779, 23.8f));
        expectedTrackPoints.add(new TrackPoint(52.49954f, 13.19979f, 1336204785, 23.8f));

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileContents.getBytes());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(byteArrayInputStream));

        List<TrackPoint> parsedTrackPoints = CgpsReader.getTrackPoints(bufferedReader);
        for(int i = 0; i<expectedTrackPoints.size(); i++) {
            assertTrue(trackPointsAreEqual(parsedTrackPoints.get(i), expectedTrackPoints.get(i)));
        }
    }
}
