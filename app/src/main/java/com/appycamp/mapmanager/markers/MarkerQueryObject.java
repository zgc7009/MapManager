package com.appycamp.mapmanager.markers;

/**
 * Created by Zach on 6/16/2015.
 */
public class MarkerQueryObject {

    private final String[] columns, selectionArgs;
    private final String selection;

    public MarkerQueryObject(String[] columns, String selection, String[] selectionArgs){
        this.columns = columns;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
    }

    public String[] getColumns() {
        return columns;
    }

    public String getSelection() {
        return selection;
    }

    public String[] getSelectionArgs() {
        return selectionArgs;
    }
}

