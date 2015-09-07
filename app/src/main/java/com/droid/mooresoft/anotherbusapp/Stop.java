package com.droid.mooresoft.anotherbusapp;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Ed on 8/23/15.
 */
public class Stop {

    public Stop(JSONObject jsonStop) throws JSONException {
        stopId = jsonStop.getString("stop_id");
        stopName = jsonStop.getString("stop_name");
        // convert JSON stop point array to a list of stop points
        JSONArray jsonStopPoints = jsonStop.getJSONArray("stop_points");
        stopPoints = new ArrayList<>(jsonStopPoints.length());
        for (int i = 0; i < jsonStopPoints.length(); i++) {
            JSONObject jsonStopPoint = jsonStopPoints.getJSONObject(i);
            stopPoints.add(new StopPoint(jsonStopPoint));
        }
        jsonString = jsonStop.toString();
    }

    public LatLng getLocation() {
        double avgLat = 0, avgLon = 0;
        for (StopPoint sp : stopPoints) {
            avgLat += sp.stopLat;
            avgLon += sp.stopLon;
        }
        avgLat /= stopPoints.size();
        avgLon /= stopPoints.size();
        return new LatLng(avgLat, avgLon);
    }

    public String stopId;
    public String stopName;
    public ArrayList<StopPoint> stopPoints;
    public String jsonString;

    public class StopPoint {

        public StopPoint(JSONObject jsonStopPoint) throws JSONException {
            stopId = jsonStopPoint.getString("stop_id");
            stopLat = jsonStopPoint.getDouble("stop_lat");
            stopLon = jsonStopPoint.getDouble("stop_lon");
            stopName = jsonStopPoint.getString("stop_name");
        }

        public String stopId;
        public double stopLat, stopLon;
        public String stopName;
    }
}
