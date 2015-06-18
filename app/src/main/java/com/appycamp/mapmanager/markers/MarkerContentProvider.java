package com.appycamp.mapmanager.markers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.appycamp.mapmanager.MainApplication;

/**
 * Created by Zach on 6/16/2015.
 */
public class MarkerContentProvider extends ContentProvider {

    private static final String PACKAGE_IDENTIFIER = MainApplication.getAplicationPackageName();
    public static final String AUTHORITY = PACKAGE_IDENTIFIER + ".content_provider" + MarkerContentProvider.class.getSimpleName();
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + MarkerSQLHelper.TABLE_MARKERS);
    private MarkerSQLHelper mSQLHelper;

    @Override
    public boolean onCreate() {
        mSQLHelper = new MarkerSQLHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(MarkerSQLHelper.TABLE_MARKERS);
        Cursor cursor = builder.query(mSQLHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        //NOTE we only have a single type of Uri
        return uri.getAuthority();
        // return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mSQLHelper.getWritableDatabase();
        long id = db.insert(MarkerSQLHelper.TABLE_MARKERS, null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(MarkerSQLHelper.TABLE_MARKERS + "/" + id);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values){
        int insertCount = 0;
        try{
            SQLiteDatabase db = mSQLHelper.getWritableDatabase();
            try{
                db.beginTransaction();
                for(ContentValues value : values){
                    db.insert(MarkerSQLHelper.TABLE_MARKERS, null, value);
                    insertCount++;
                }
                db.setTransactionSuccessful();
            } catch(Exception e){
                e.printStackTrace();
            } finally{
                db.endTransaction();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return insertCount;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mSQLHelper.getWritableDatabase();
        int rowsDeleted = db.delete(MarkerSQLHelper.TABLE_MARKERS, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mSQLHelper.getWritableDatabase();
        db.update(MarkerSQLHelper.TABLE_MARKERS, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return 0;
    }
}
