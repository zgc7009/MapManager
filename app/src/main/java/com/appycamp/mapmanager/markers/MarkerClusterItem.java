package com.appycamp.mapmanager.markers;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Zach on 6/17/2015.
 */
public class MarkerClusterItem implements ClusterItem {
    private final String mIpTitle;
    private final LatLng mPosition;

    public MarkerClusterItem(String ip, double lat, double lng) {
        mIpTitle = ip;
        mPosition = new LatLng(lat, lng);
    }

    public String getTitle(){
        return mIpTitle;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}
