package com.app.android.tensel.utility;

/**
 * Created by USER on 11/11/2016.
 */

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "androidhive-welcome";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public void setVideoRecordingDuration(int duration){
        editor.putInt(Utils.PREF_RECORDING_DURATION, duration)
                .commit();
    }

    public int getVideoRecordingDuration(){
        return pref.getInt(Utils.PREF_RECORDING_DURATION, 10);
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public boolean getShouldShowHints() {
        return pref.getBoolean(Utils.PREF_SHOW_HINTS, true);
    }

    public void setShouldShowHints(boolean val){
        editor.putBoolean(Utils.PREF_SHOW_HINTS, val)
                .commit();
    }

    public void setShowPvHints(boolean val){
        editor.putBoolean(Utils.PREF_SHOW_PV_HINTS, val)
                .commit();
    }

    public boolean getShowPvHints(){
        return pref.getBoolean(Utils.PREF_SHOW_PV_HINTS, true);
    }
}