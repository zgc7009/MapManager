package com.appycamp.mapmanager.tiles;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Zach on 6/14/2015.
 */
public class PolylineOverlaySaver extends AsyncTask<Bitmap, Void, Boolean> {

    public interface OnOverlaySavedListener{
        void onOverlaySaved(boolean success);
    }

    private OnOverlaySavedListener listener;
    private int x, y , zoom;

    public PolylineOverlaySaver(int x, int y, int zoom, OnOverlaySavedListener listener){
        this.x = x;
        this.y = y;
        this.zoom = zoom;
        this.listener = listener;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if(listener  != null)
            listener.onOverlaySaved(success);
    }

    @Override
    protected Boolean doInBackground(Bitmap... overlayBmps) {
        FileOutputStream out = null;
        boolean success = false;
        try {
            File overlayFile = new File(TileOverlayManager.getOverlayFileString(x, y, zoom));
            if(!overlayFile.exists())
                new File(TileOverlayManager.getOverlayFileDir()).mkdirs();
            out = new FileOutputStream(overlayFile);
            overlayBmps[0].compress(Bitmap.CompressFormat.PNG, 100, out);
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return success;
    }
}
