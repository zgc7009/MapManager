package com.appycamp.mapmanager.markers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.AsyncTask;

/**
 * This class will asynchronously query our database and, once done, utilize our listener
 * to notify our adapter to update cursors. If the cursor is null or empty it will
 * return null which will result in an empty list.
 *
 * @author Zach
 */
public class MarkerQueryTask extends AsyncTask<MarkerQueryObject, Void, Cursor> {

    public interface QueryListener {
         void onQueryResult(Cursor cursor);
    }

    private MarkerSQLHelper mSQLHelper;
    private QueryListener mListener;

    public MarkerQueryTask(Context context, QueryListener listener){
        mSQLHelper = new MarkerSQLHelper(context);
        mListener = listener;
    }

    /**
     * Will get our generic (full) cursor for our database
     */
    public void getMarkersCursor(){
        query(mSQLHelper.ALL_COLUMNS, null, null);
    }

    /**
     * This method will query for all matches of the current string, and modify
     * our adapter to use the respective cursor as our list cursor.
     *
     * @param query - the query string
     */
    public void getMatches(String query) {
        String selection = mSQLHelper.COLUMN_MARKER_IP + " LIKE ?";				    // read args from description column
        String[] selectionArgs = new String[] {"%" + query + "%"};					// return all rows that the column matches the query + *
        query(mSQLHelper.ALL_COLUMNS, selection, selectionArgs);
    }

    /**
     * Will perform the query, asynchronously of course, based on the passed params.
     * If selection && selectionArgs == null it will return the full list.
     *
     * @param columns - columns we want to return with our cursor
     * @param selection - selection SQL string
     * @param selectionArgs - selection args (our query)
     */
    public void query(String[] columns, String selection, String[] selectionArgs) {
        MarkerQueryObject queryObject = new MarkerQueryObject(columns, selection, selectionArgs);
        execute(queryObject);
    }
    
    @Override
    protected Cursor doInBackground(MarkerQueryObject... params) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(MarkerSQLHelper.TABLE_MARKERS);

        Cursor cursor = builder.query(mSQLHelper.getReadableDatabase(), params[0].getColumns(), params[0].getSelection(),
                params[0].getSelectionArgs(), null, null, null);

        if (cursor == null)
            return null;
        else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    @Override
    public void onPostExecute(Cursor cursor){
        if(mListener != null)
            mListener.onQueryResult(cursor);
    }


}
