package com.droid.mooresoft.anotherbusapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

/**
 * Created by Ed on 8/19/15.
 */
public class Data {

    public static void saveChangesetId(String requestId, String changesetId, Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(requestId + "_changeset_id", changesetId);
        editor.commit();
    }

    public static String getChangesetId(String requestId, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(requestId + "_changeset_id", null);
    }

    public static void saveRequestTime(String requestId, String extraId, long millis, Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putLong(requestId +
                (extraId != null ? "_" + extraId : "") +
                "_time", millis);
        editor.commit();
    }

    public static long getRequestTime(String requestId, String extraId, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(requestId +
                (extraId != null ? "_" + extraId : "") +
                "_time", -1);
    }

    public static void saveJsonResponse(String requestId, String extraId, String json, Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(requestId +
                (extraId != null ? "_" + extraId : "") +
                "_json", json);
        editor.commit();
    }

    public static String getJsonResponse(String requestId, String extraId, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(requestId +
                (extraId != null ? "_" + extraId : "") +
                "_json", null);
    }

    public static void saveRecentStop(String stopName, Context context) {
        ArrayList<String> recentStops = getRecentStops(context);
        if (recentStops.contains(stopName)) {
            // move to front of list
            recentStops.remove(stopName);
        } else if (recentStops.size() == MAX_RECENT_STOPS) {
            // remove oldest
            recentStops.remove(MAX_RECENT_STOPS - 1);
        }
        recentStops.add(0, stopName);
        saveRecentStops(recentStops, context);
    }

    public static ArrayList<String> getRecentStops(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        ArrayList<String> recentStops = new ArrayList<>();
        for (int i = 0; i < MAX_RECENT_STOPS; i++) {
            String key = "recent_" + i;
            String stopName = prefs.getString(key, null);
            if (stopName != null) recentStops.add(stopName);
        }
        return recentStops;
    }

    private static void saveRecentStops(ArrayList<String> recentStops, Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        for (int i = 0; i < recentStops.size(); i++) {
            String key = "recent_" + i;
            editor.putString(key, recentStops.get(i));
        }
        editor.commit();
    }

    public static final int MAX_RECENT_STOPS = 4;
    public static final String REQUEST_ID_GET_STOPS = "get_stops",
            REQUEST_ID_GET_DEPARTURES_BY_STOP = "get_departures_by_stop",
            REQUEST_ID_AUTOCOMPLETE = "autocomplete";
}
