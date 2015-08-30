package com.droid.mooresoft.anotherbusapp;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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
}
