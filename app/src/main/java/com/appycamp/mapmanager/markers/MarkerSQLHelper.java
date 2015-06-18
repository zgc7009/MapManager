package com.appycamp.mapmanager.markers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.appycamp.mapmanager.network.models.MarkerModel;

/**
 * Created by Zach on 6/16/2015.
 */
public class MarkerSQLHelper extends SQLiteOpenHelper{


    private final String TAG = getClass().getSimpleName();
    
    // TABLE IDENTIFIERS
    protected static final String TABLE_MARKERS = "markers";
    protected static final String COLUMN_ID = "_id";
    protected static final String COLUMN_MARKER_IP = "marker_id";
    protected static final String COLUMN_LAT = "marker_lat";
    protected static final String COLUMN_LNG = "marker_lng";
    protected static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_MARKER_IP, COLUMN_LAT, COLUMN_LNG};

    private static final String DB_NAME = "comments.db";
    private static final int DB_VERSION = 1;

    private static final String DB_CREATE = "create table " + TABLE_MARKERS + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_MARKER_IP + " text not null, " +
            COLUMN_LAT + " text not null, " +
            COLUMN_LNG + " text not null;";

    private ContentResolver mContentResolver;

    public MarkerSQLHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_MARKERS);
        onCreate(db);
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    // 						FOR USING CONTENT RESOLVER/PROVIDER							//
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    public void addMarker(MarkerModel markerModel){
        ContentValues values = new ContentValues();
        //values.put(COLUMN_ID, markerModel.getId());
        values.put(COLUMN_MARKER_IP, markerModel.getIpAddress());
        values.put(COLUMN_LAT, markerModel.getLatitude());
        values.put(COLUMN_LNG, markerModel.getLongitude());

        mContentResolver.insert(MarkerContentProvider.CONTENT_URI, values);
    }

    public Cursor findMarker(String query){
        String[] projection = {COLUMN_MARKER_IP}; //ALL_COLUMNS;
        String selection = "%" + query + "%";
        String[] selectionArgs = {COLUMN_LAT, COLUMN_LNG};

        return mContentResolver.query(MarkerContentProvider.CONTENT_URI, projection, selection, selectionArgs, null);
    }
}
