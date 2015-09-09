package com.droid.mooresoft.anotherbusapp;

import android.graphics.Color;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ed on 8/22/15.
 */
public class Departure implements Comparable<Departure> {

    public Departure(JSONObject jsonDeparture) throws JSONException {
        stopId = jsonDeparture.getString("stop_id");
        headsign = jsonDeparture.getString("headsign");
        String timeString = jsonDeparture.getString("expected");
        try {
            mExpectedDepartureTime = parseExpectedDepartureTime(timeString);
        } catch (ParseException e) {
            // throw the parse exception up
            throw new JSONException(e.toString());
        }
        vehicleId = jsonDeparture.getString("vehicle_id");
        // if we made it here without exception, we should be able to finish without exception
        if (jsonDeparture.getBoolean("is_scheduled")) {
            JSONObject jsonTrip = jsonDeparture.getJSONObject("trip");
            tripHeadsign = jsonTrip.getString("trip_headsign");
            JSONObject jsonRoute = jsonDeparture.getJSONObject("route");
            String colorString = jsonRoute.getString("route_color");
            // surrounding with try/catch just to be extra safe
            try {
                routeColor = Color.parseColor('#' + colorString);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                routeColor = DEFAULT_ROUTE_COLOR;
            }
        } else {
            // use default values
            tripHeadsign = "- unscheduled -";
            routeColor = DEFAULT_ROUTE_COLOR;
        }
    }

    @Override
    public int compareTo(Departure other) {
        return this.getExpectedMins() - other.getExpectedMins();
    }

    public int getExpectedMins() {
        // ToDo: get expected departure time relative to current time
        long diffMillis = mExpectedDepartureTime - System.currentTimeMillis();
        return (int) (diffMillis / ONE_MINUTE);
    }

    private long parseExpectedDepartureTime(String timeString) throws ParseException {
        int index = timeString.lastIndexOf(':');
        timeString = timeString.substring(0, index) + timeString.substring(index + 1);
        Date time = mDateFormat.parse(timeString);
        return time.getTime();
    }

    public String stopId;
    public String headsign, tripHeadsign;
    public int routeColor;
    public String vehicleId;

    private long mExpectedDepartureTime;

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    private static final int DEFAULT_ROUTE_COLOR = Color.WHITE;
    private static final int ONE_MINUTE = 1000 * 60; // millis
}
