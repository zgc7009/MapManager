package com.appycamp.mapmanager.tiles;

import android.os.Environment;
import android.util.SparseArray;

import com.google.android.gms.maps.model.TileOverlayOptions;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zach on 6/14/2015.
 */
public class TileOverlayManager {

    private static final String POLYLINE_OVERLAY_LOC = Environment.getExternalStorageDirectory()
            + File.separator + "polyline_overlays/";

    private static TileOverlayManager instance;

    private SparseArray<TileOverlayOptions> mPolylineOptionsMap;

    private TileOverlayManager(){
        mPolylineOptionsMap = new SparseArray<>();
    }

    public static TileOverlayManager getInstance(){
        if(instance == null)
            instance = new TileOverlayManager();
        return instance;
    }

    public void initializePolylineOverlayOptions(){
        TileOverlayOptions overlayOptions = new TileOverlayOptions();
        overlayOptions.tileProvider(createTileProvider());
        mPolylineOptionsMap.put(0, overlayOptions);
    }

    public TileOverlayOptions getPolylineOverlayOptions(){
        return mPolylineOptionsMap.get(0);
    }

    public static CustomTileProvider createTileProvider(){
        return new CustomTileProvider();
    }

    public static String getOverlayFileString(int x, int y, int zoom){
        return getOverlayFileDir() + "_" + x + "_" + y + "_" + zoom + ".png";
    }

    public static String getOverlayFileDir(){
        return TileOverlayManager.POLYLINE_OVERLAY_LOC;
    }

}
