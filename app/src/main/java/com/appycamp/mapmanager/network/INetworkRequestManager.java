package com.appycamp.mapmanager.network;

import com.android.volley.Response;
import com.appycamp.mapmanager.polylines.PolylineGenerator;

/**
 * Created by Zach on 6/13/2015.
 */
public interface INetworkRequestManager {

    void fetchPolylines(String requestUrl, Response.Listener<PolylineGenerator.DirectionsResult> responseListern, Response.ErrorListener errorListener);
}
