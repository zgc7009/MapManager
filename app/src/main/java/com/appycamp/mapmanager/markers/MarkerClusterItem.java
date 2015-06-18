package com.appycamp.mapmanager.markers;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Zach on 6/17/2015.
 */
public class MarkerClusterItem implements ClusterItem {
    private final LatLng mPosition;

    public MarkerClusterItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}
