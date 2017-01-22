package com.junior.dwan.runtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * Created by Might on 11.01.2017.
 */

public class LocationReceiver extends BroadcastReceiver {
    public static final String TAG=RunActivity.TAG_PREFIX+"LocationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        // Если имеется дополнение Location, использовать его
        Location location=(Location)intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
        if(location!=null){
            onLocationReceived(context,location);
            return;
        }

        // Если мы попали в эту точку, произошло что-то другое
        if(intent.hasExtra(LocationManager.KEY_PROVIDER_ENABLED)){
            boolean enabled=intent.getBooleanExtra(LocationManager.KEY_PROVIDER_ENABLED,false);
            onProviderEnabledChanged(enabled);
        }

    }

    protected void onLocationReceived (Context context,Location location){
        Log.i(TAG, this + " Got location from " + location.getProvider() + ": "
                + location.getLatitude() + ", " + location.getLongitude());
    }

    protected void onProviderEnabledChanged(boolean enabled){
        Log.i(TAG, "Provider " + (enabled ? "enabled" : "disabled"));
    }
}
