package com.junior.dwan.runtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import java.sql.Date;

/**
 * Created by Might on 12.01.2017.
 */

public class RunDatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "run.sqlite";
    public static final int DB_VERSION = 1;

    public static final String TABLE_RUN = "run";
    public static final String COLUMN_RUN_ID = "_id";
    public static final String COLUMN_RUN_START_DATE = "start_date";

    public static final String TABLE_LOCATION = "location";
    public static final String COLUMN_LOCATION_TIMESTAMP = "timestamp";
    public static final String COLUMN_LOCATION_LATITUDE = "latitude";
    public static final String COLUMN_LOCATION_LONGITUDE = "longitude";
    public static final String COLUMN_LOCATION_ALTITUDE = "altitude";
    public static final String COLUMN_LOCATION_PROVIDER = "provider";
    public static final String COLUMN_LOCATION_RUN_ID = "run_id";

    public RunDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создание таблицы "run
        db.execSQL("create table run("
                + "_id integer primary key autoincrement,"
                + " start_date integer" + ")");
        // Создание таблицы "location"
        db.execSQL("create table location("
                + "timestamp integer, " +
                "latitude real," +
                "longitude real, " +
                "altitude real, " +
                "provider varchar(100)," +
                "run_id integer references run(_id)"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Здесь реализуются изменения схемы и преобразования данных
        // при обновлении схемы
    }

    public long insertRun(Run run) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_RUN_START_DATE, run.getStartDate().getTime());
        return getWritableDatabase().insert(TABLE_RUN, null, cv);
    }

    public long insertLocation(long runID, Location location) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LOCATION_TIMESTAMP, location.getTime());
        cv.put(COLUMN_LOCATION_LATITUDE, location.getLatitude());
        cv.put(COLUMN_LOCATION_LONGITUDE, location.getLongitude());
        cv.put(COLUMN_LOCATION_ALTITUDE, location.getAltitude());
        cv.put(COLUMN_LOCATION_PROVIDER, location.getProvider());
        cv.put(COLUMN_LOCATION_PROVIDER, runID);
        return getWritableDatabase().insert(TABLE_LOCATION, null, cv);
    }

    public RunCursor queryRuns() {
        // Эквивалент "select * from run order by start_date asc"
        Cursor wrapped = getReadableDatabase().query(TABLE_RUN, null, null, null, null, null, COLUMN_RUN_START_DATE + " asc");
        return new RunCursor(wrapped);
    }

    public RunCursor queryRun(long runId) {
        Cursor wrapped = getReadableDatabase().query(TABLE_RUN,
                null,//все столбцы
                COLUMN_RUN_ID + " = ?",// Поиск по идентификатору серии
                new String[]{String.valueOf(runId)},// с этим значением
                null,//group by
                null,//order by
                null,//having
                "1");//1 строка
        return new RunCursor(wrapped);
    }


    /**
     * Вспомогательный класс с курсором, возвращающим строки таблицы "run".
     * Метод {@linkgetRun()} возвращает экземпляр Run, представляющий
     * текущую строку.s
     */
    public static class RunCursor extends CursorWrapper {

        public RunCursor(Cursor c) {
            super(c);
        }

        /**
         * Возвращает объект Run, представляющий текущую строку,
         * или null, если текущая строка недействительна.
         */

        public Run getRun() {
            if (isBeforeFirst() || isAfterLast()) {
                return null;
            }
            Run run = new Run();
            long runId = getLong(getColumnIndex(COLUMN_RUN_ID));
            run.setId(runId);
            long startDate = getLong(getColumnIndex(COLUMN_RUN_START_DATE));
            run.setStartDate(new Date(startDate));
            return run;
        }
    }
}
