package com.junior.dwan.runtracker;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Created by Might on 11.01.2017.
 */

public class RunManager {
    public static final String TAG = RunActivity.TAG_PREFIX + "RunManager";

    public static final String ACTION_LOCATION = "com.junior.dwan.runtracker.ACTION_LOCATION";
    public static final String TEST_PROVIDER = "TEST_PROVIDER";

    public static final String PREFS_FILES = "runs";
    public static final String PREF_CURRENT_RUN_ID = "RunManager.currentRunId";

    private static RunManager sRunManager;
    private Context mAppContext;
    private LocationManager mLocationManager;

    private long mCurrentRunId;
    private RunDatabaseHelper mDatabaseHelper;
    private SharedPreferences mSharedPreferences;

    // Закрытый конструктор заставляет использовать
    // RunManager.get(Context)
    private RunManager(Context context) {
        mAppContext = context;
        mLocationManager = (LocationManager) mAppContext.getSystemService(Service.LOCATION_SERVICE);

        mDatabaseHelper = new RunDatabaseHelper(mAppContext);
        mSharedPreferences = mAppContext.getSharedPreferences(PREFS_FILES, Context.MODE_PRIVATE);
        mCurrentRunId = mSharedPreferences.getLong(PREF_CURRENT_RUN_ID, -1);
    }

    public static RunManager get(Context context) {
        if (sRunManager == null) {
            // Использование контекста приложения для предотвращения
            // утечки активностей
            sRunManager = new RunManager(context.getApplicationContext());
        }

        return sRunManager;
    }

    public Run startNewRun() {
        // Вставка объекта Run в базу данных
        Run run = insertRun();
        // Запуск отслеживания серии
        startTrackingRun(run);
        return run;
    }

    public void startTrackingRun(Run run) {
        // Получение идентификатора
        mCurrentRunId = run.getId();
        // Сохранение его в общих настройках
        mSharedPreferences.edit().putLong(PREF_CURRENT_RUN_ID, mCurrentRunId).apply();
        // Запуск обновления данных местоположения
        startLocationUpdates();
    }

    public void stopRun() {
        stopLocationUpdates();
        mCurrentRunId = -1;
        mSharedPreferences.edit().remove(PREF_CURRENT_RUN_ID).apply();
    }

    private Run insertRun() {
        Run run = new Run();
        run.setId(mDatabaseHelper.insertRun(run));
        return run;
    }

    public void insertLocation(Location location) {
        if (mCurrentRunId != -1) {
            mDatabaseHelper.insertLocation(mCurrentRunId, location);
            Log.i(TAG, "" + "RECEIVED FROM TRACKING");
        } else {
            Log.e(TAG, "Location received with no tracking run; ignoring.");
        }

    }

    public RunDatabaseHelper.RunCursor queryRuns() {
        return mDatabaseHelper.queryRuns();
    }


    private PendingIntent getLocationPendingIntent(boolean shouldCreated) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        int flags = shouldCreated ? 0 : PendingIntent.FLAG_NO_CREATE;
        return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);
    }

    public void startLocationUpdates() {
        String provider = LocationManager.GPS_PROVIDER;

        // Если имеется поставщик тестовых данных и он активен,
        // использовать его.
        if (mLocationManager.getProvider(TEST_PROVIDER) != null
                && mLocationManager.isProviderEnabled(TEST_PROVIDER)) {
            provider = TEST_PROVIDER;
            Log.i(TAG, "Using provider " + provider);
        }

        // Получение последнего известного местоположения
        // и его рассылка (если данные доступны).
        Location lastKnown = mLocationManager.getLastKnownLocation(provider);

        if (lastKnown != null) {
            // Время инициализируется текущим значением
            lastKnown.setTime(System.currentTimeMillis());
            broadcastLocation(lastKnown);
        }


        // Запуск обновлений из LocationManager
        if (ActivityCompat.checkSelfPermission(mAppContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mAppContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        PendingIntent pendingIntent = getLocationPendingIntent(true);
        mLocationManager.requestLocationUpdates(provider, 0, 0, pendingIntent);
    }

    private void broadcastLocation(Location location) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
        mAppContext.sendBroadcast(broadcast);

    }

    public void stopLocationUpdates() {
        PendingIntent pendingIntent = getLocationPendingIntent(false);

        if (mLocationManager != null) {
            mLocationManager.removeUpdates(pendingIntent);
            pendingIntent.cancel();
        }
    }

    public boolean isTrackingRun() {
        return getLocationPendingIntent(false) != null;
    }

    public Run getRun(long runId) {
        Run run = null;
        RunDatabaseHelper.RunCursor cursor = mDatabaseHelper.queryRun(runId);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            run = cursor.getRun();
        }
        cursor.close();
        return run;
    }

}
