package com.droid.mooresoft.anotherbusapp;

import android.app.Application;
import android.content.Context;

import java.util.Map;

/**
 * Created by Ed on 8/16/15.
 */
public class GlobalVariables extends Application {

    public static GlobalVariables getInstance(Context context) {
        return (GlobalVariables) context.getApplicationContext();
    }

    public void setStopMap(Map<String, Stop> stopMap) {
        mStopMap = stopMap;
    }

    public Map<String, Stop> getStopMap() {
        return mStopMap;
    }

    private Map<String, Stop> mStopMap;
}
