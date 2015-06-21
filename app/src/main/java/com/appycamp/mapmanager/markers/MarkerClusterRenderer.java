package com.appycamp.mapmanager.markers;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by Zach on 6/21/2015.
 */
public class MarkerClusterRenderer extends DefaultClusterRenderer<MarkerClusterItem> {

    private static final int MIN_CLUSTER_SIZE = 20;

    public MarkerClusterRenderer(Context context, GoogleMap map, ClusterManager<MarkerClusterItem> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(MarkerClusterItem item, MarkerOptions markerOptions) {
        markerOptions.title(item.getTitle());
        super.onBeforeClusterItemRendered(item, markerOptions);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        return cluster.getSize() >= MIN_CLUSTER_SIZE; // if markers <= MIN_CLUSTER_SIZE then not clustering
    }
}
