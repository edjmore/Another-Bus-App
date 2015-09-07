package com.droid.mooresoft.anotherbusapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Ed on 9/7/15.
 */
public class AlarmManager {

    public AlarmManager(Context context) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isAlarmed(Departure departure) {
        // todo:
        return false;
    }

    public void setAlarm(Departure departure, int minutes) {
        // todo
    }

    public void removeAlarm(Departure departure) {
        // todo
    }

    private SharedPreferences mSharedPrefs;
}
