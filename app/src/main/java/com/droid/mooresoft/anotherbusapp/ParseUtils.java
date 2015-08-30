package com.droid.mooresoft.anotherbusapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ed on 8/24/15.
 */
public class ParseUtils {

    public static Map<String, ArrayList<Departure>> parseGetDeparturesByStopJson(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray jsonArray = jsonObject.getJSONArray("departures");
        // map from stop point ID to departure list
        Map<String, ArrayList<Departure>> map = new HashMap<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonDeparture = jsonArray.getJSONObject(i);
            try {
                Departure departure = new Departure(jsonDeparture);
                // the stop point ID for this departure may or may not already be in the map
                ArrayList<Departure> departures = null;
                if (map.containsKey(departure.stopId)) {
                    departures = map.get(departure.stopId);
                } else {
                    departures = new ArrayList<>();
                    map.put(departure.stopId, departures);
                }
                departures.add(departure);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    public static Map<String, Stop> parseGetStopsJson(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray jsonArray = jsonObject.getJSONArray("stops");
        Map<String, Stop> map = new HashMap<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonStop = jsonArray.getJSONObject(i);
                Stop stop = new Stop(jsonStop);
                map.put(stop.stopName, stop);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    public static ArrayList<String> parseAutocompleteJson(String jsonString) throws JSONException {
        JSONArray jsonArray = new JSONArray(jsonString);
        ArrayList<String> stopNames = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            String stopName = jsonArray.getJSONObject(i).getString("n");
            stopNames.add(stopName);
        }
        return stopNames;
    }
}
