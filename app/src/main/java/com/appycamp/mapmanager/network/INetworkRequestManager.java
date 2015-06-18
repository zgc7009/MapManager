package com.appycamp.mapmanager.network;

import com.android.volley.Response;
import com.appycamp.mapmanager.network.models.MarkerModel;

/**
 * Created by Zach on 6/13/2015.
 */
public interface INetworkRequestManager {

    void sendMarkerRequest(String ip, Response.Listener<MarkerModel> listener, Response.ErrorListener errorListener);

}
