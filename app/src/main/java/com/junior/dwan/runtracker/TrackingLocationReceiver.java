package com.junior.dwan.runtracker;

import android.content.Context;
import android.location.Location;
import android.util.Log;

/**
 * Created by Might on 12.01.2017.
 */
public class TrackingLocationReceiver extends LocationReceiver {
public static final String TAG=RunActivity.TAG_PREFIX+"TrackingLocationReceiver";
    @Override
    protected void onLocationReceived(Context context, Location location) {
        RunManager.get(context).insertLocation(location);

    }
}
