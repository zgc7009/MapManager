package com.appycamp.mapmanager.polylines;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * Created by Zach on 6/13/2015.
 */
public class PolylineManager {

    public interface OnPolylineReadyListener{
        void onPolylineReady(int polylinePosition, PolylineOptions options);
    }

    public static final int NUM_ROUTES = 25;
    public static final int NUM_POLYLINES = 3000;

    private static PolylineManager instance;

    private PolylineOptions[] mPolylineOptions;

    private PolylineManager(final OnPolylineReadyListener listener){
        mPolylineOptions = new PolylineOptions[NUM_ROUTES];
        for(int i = 0; i < NUM_ROUTES; i++) {
            final int polyLinePos = i;
            PolylineGenerator.generatePolyline(new PolylineGenerator.OnPolylineCreatedListener() {
                @Override
                public void onPolylineCreated(PolylineOptions polylineOptions) {
                    mPolylineOptions[polyLinePos] = polylineOptions;
                    if(listener != null)
                        listener.onPolylineReady(polyLinePos, polylineOptions);
                }
            });
        }
    }

    public static PolylineManager init(OnPolylineReadyListener listener){
        instance = new PolylineManager(listener);
        return instance;
    }

    public static PolylineManager getInstance(){
        return instance;
    }

    public PolylineOptions getPolylineOptions(int position){
        return mPolylineOptions[position];
    }

    public PolylineOptions[] getAllPolylineOptions(){
        return mPolylineOptions;
    }

    public ArrayList<ArrayList<LatLng>> getPolylineLatLngs(){
        ArrayList<ArrayList<LatLng>> returnList = new ArrayList<>();
        for(PolylineOptions options : getAllPolylineOptions()) {
            if(options != null && options.getPoints() != null)
                returnList.add(new ArrayList(options.getPoints()));
        }
        return returnList;
    }
}
