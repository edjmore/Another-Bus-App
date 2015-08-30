package com.droid.mooresoft.anotherbusapp;

import android.content.Context;
import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Ed on 8/21/15.
 */
public class UrlFactory {

    public static URL buildGetDeparturesByStopUrl(String stopId) {
        URL url = null;
        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority("developer.cumtd.com")
                    .appendPath("api")
                    .appendPath("v2.2")
                    .appendPath("json")
                    .appendPath("GetDeparturesByStop")
                    .appendQueryParameter("key", CUMTD_API_KEY)
                    .appendQueryParameter("stop_id", stopId);
            url = new URL(builder.build().toString());
        } catch (MalformedURLException e) {
            // this method should never actually throw an exception
            e.printStackTrace();
        }
        return url;
    }

    public static URL buildAutocompleteUrl(String usrInput) {
        URL url = null;
        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("www.cumtd.com")
                    .appendPath("autocomplete")
                    .appendPath("Stops")
                    .appendPath("v1.0")
                    .appendPath("json")
                    .appendPath("search")
                    .appendQueryParameter("query", usrInput);
            url = new URL(builder.build().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static URL buildGetStopsUrl(Context context) {
        URL url = null;
        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority("developer.cumtd.com")
                    .appendPath("api")
                    .appendPath("v2.2")
                    .appendPath("json")
                    .appendPath("GetStops")
                    .appendQueryParameter("key", CUMTD_API_KEY);
            String changeset;
            if ((changeset = Data.getChangesetId(Data.REQUEST_ID_GET_STOPS, context)) != null) {
                builder.appendQueryParameter("changeset_id", changeset);
            }
            url = new URL(builder.build().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    private static final String CUMTD_API_KEY = "18c63ee8a7d442a6a891030a2cc07fc7";
}
