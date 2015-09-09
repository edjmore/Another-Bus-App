package com.droid.mooresoft.anotherbusapp;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ed on 8/19/15.
 */
public class AndroidUtils {

    public static Drawable getTintedDrawable(int resDrawable, int tintColor, Context context) {
        Drawable d = context.getResources().getDrawable(resDrawable); // ToDo: use non-deprecated method
        d.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
        return d;
    }

    public static String sanitizeUserInput(String usrInput) {
        String lower = usrInput.toLowerCase();
        if (lower.replaceAll("[^a-z]", "").isEmpty()) {
            return "";
        }
        String alphabetic = lower.replaceAll("[^a-z ]", "");
        return alphabetic.replaceAll("[ ]", "+");
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static void requestCurrentLocation(LocationListener listener, Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // would like to use GPS if possible
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        lm.requestSingleUpdate(criteria, listener, null /* todo: should we use a 'Looper?' */);
    }

    public static void requestLocationUpdates(LocationListener listener,
                                              long minTime, float minDistance, Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // would like to use GPS if possible
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        lm.requestLocationUpdates(minTime, minDistance, criteria, listener, null /* todo: 'Looper?' */);
    }

    public static float getDistanceToStop(Location currentLocation, Stop stop) {
        Location stopLocation = new Location("dummy_provider");
        LatLng stopLatLng = stop.getLocation();
        stopLocation.setLatitude(stopLatLng.latitude);
        stopLocation.setLongitude(stopLatLng.longitude);
        return currentLocation.distanceTo(stopLocation) / ONE_MILE;
    }

    private static final float ONE_MILE = 1609.344f; // meters
}
