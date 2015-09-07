package com.droid.mooresoft.anotherbusapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Ed on 9/5/15.
 */
public class Favorites {

    public static void addFavorite(Stop favStop, Context context) {
        Set<String> favs = getFavoritesInternal(context);
        favs.add(favStop.jsonString);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putStringSet("favorites", favs);
        editor.commit();
    }

    public static void removeFavorite(Stop unfavStop, Context context) {
        Set<String> favStops = getFavoritesInternal(context);
        favStops.remove(unfavStop.jsonString);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putStringSet("favorites", favStops);
        editor.commit();
    }

    public static boolean isFavorite(Stop stop, Context context) {
        Set<String> favStops = getFavoritesInternal(context);
        return favStops.contains(stop.jsonString);
    }

    public static ArrayList<Stop> getFavorites(Context context) {
        Set<String> favs = getFavoritesInternal(context);
        Iterator<String> iter = favs.iterator();
        ArrayList<Stop> favStops = new ArrayList<>();
        while (iter.hasNext()) {
            try {
                JSONObject jsonStop = new JSONObject(iter.next());
                favStops.add(new Stop(jsonStop));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return favStops;
    }

    private static Set<String> getFavoritesInternal(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getStringSet("favorites", new HashSet<String>());
    }
}
