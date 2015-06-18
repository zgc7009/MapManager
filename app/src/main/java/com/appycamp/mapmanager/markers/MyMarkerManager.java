package com.appycamp.mapmanager.markers;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appycamp.mapmanager.MainApplication;
import com.appycamp.mapmanager.network.NetworkRequestManager;
import com.appycamp.mapmanager.network.models.MarkerModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Zach on 6/16/2015.
 *
 * We can store local clusters here to avoid having to pull constantly from our db
 */
public class MyMarkerManager {

    private static final int HEAP_HOLD_LIMIT = 2500;

    public interface MarkerRequestCompleteListener{
        void onMarkerRequestComplete(boolean success);
        void onAllMarkersRequested();
    }

    private static MyMarkerManager instance;

    private List<MarkerModel> mMarkerModels;
    private MarkerRequestCompleteListener mListener;

    private MyMarkerManager(){
        mMarkerModels = new ArrayList<>();
    }

    public static MyMarkerManager getInstance(MarkerRequestCompleteListener listener){
        if(instance == null)
            instance = new MyMarkerManager();
        instance.mListener = listener;
        return instance;
    }

    public static MyMarkerManager getInstance(){
        if(instance == null)
            instance = new MyMarkerManager();
        return instance;
    }

    public MarkerRequestCompleteListener getListener(){
        return mListener;
    }

    public boolean addMarker(MarkerModel marker){
        if(marker == null)
            return false;

        if(mMarkerModels.size() >= HEAP_HOLD_LIMIT)
            offloadToDB();

        return mMarkerModels.add(marker);
    }

    public boolean addMarkers(MarkerModel[] markers){
        boolean markersAdded = false;

        if(mMarkerModels.size() + markers.length >= HEAP_HOLD_LIMIT)
            offloadToDB();

        for(int i = 0; i < markers.length; i++) {
            if(markers[i] == null)
                markersAdded = false;
            else
                markersAdded = addMarker(markers[i]) && (i == 0 || markersAdded);
        }
        return markersAdded;
    }

    public int getMarkerModelCount(){
        return mMarkerModels.size();
    }

    public MarkerModel[] getAllMarkers(){
        return (MarkerModel[]) mMarkerModels.toArray();
    }

    public MarkerModel getMarkerModel(int pos){
        return mMarkerModels.get(pos);
    }

    public MarkerModel getMostRecentMarker(){
        return mMarkerModels.get(mMarkerModels.size()-1);
    }

    /**
     * If we want to wait to build our cluster once we have populated our managers list we can utilize this method.
     * It would also work for bulk loading makers
     *
     * @param clusterManager
     */
    public void buildCluster(ClusterManager<MarkerClusterItem> clusterManager){
        if(mMarkerModels== null && mMarkerModels.size() == 0)
            return;

        List<MarkerClusterItem> clusterItems = new ArrayList<>();
        for(MarkerModel marker : mMarkerModels)
            clusterItems.add(new MarkerClusterItem(Double.valueOf(marker.getLatitude()), Double.valueOf(marker.getLongitude())));
        clusterManager.addItems(clusterItems);
    }

    /**
     * TODO will likely want to offload things to the db automatically, but for now I just want to utilize the heap (even though
     * TODO (cont.) our content provider is pretty well setup)
     */
    private void offloadToDB(){
        while(mMarkerModels.size() > 0){
            // TODO add marker(0) to our db and then remove it from our heap list, something like
            //new MarkerSQLHelper(MainApplication.getApplication()).addMarker(mMarkerModels.remove(0));

            return;         // dont forget to remove this so we actually loop
        }
    }

}
