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
        String key = getKey(departure);
        return mSharedPrefs.contains(key);
    }

    public boolean setAlarm(Departure departure, int minutesBefore) {
        String key = getKey(departure);
        if (minutesBefore >= departure.getExpectedMins()) {
            return false;
        }
        long alarmTimeMillis = System.currentTimeMillis() + ((departure.getExpectedMins() - minutesBefore) * 60 * 1000);
        mSharedPrefs.edit().putLong(key, alarmTimeMillis).commit();
        return true;
    }

    public void removeAlarm(Departure departure) {
        String key = getKey(departure);
        mSharedPrefs.edit().remove(key).commit();
    }

    private String getKey(Departure departure) {
        return "alarm_" + departure.vehicleId + "_" + departure.stopId;
    }

    private SharedPreferences mSharedPrefs;
}
