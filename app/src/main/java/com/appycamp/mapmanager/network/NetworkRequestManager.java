package com.appycamp.mapmanager.network;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.appycamp.mapmanager.markers.MyMarkerManager;
import com.appycamp.mapmanager.network.models.MarkerModel;
import com.appycamp.mapmanager.polylines.PolylineGenerator;

/**
 * Created by Zach on 6/13/2015.
 */
public class NetworkRequestManager implements INetworkRequestManager {

    private static final int TIME_OUT_POLICY_DURATION = 5000;

    /**
     * Singleton instance of the request manager
     */
    private static NetworkRequestManager singleInstance = new NetworkRequestManager();

    /**
     * Single Network Request Queue for the Application.
     */
    public static RequestQueue requestQueue;

    /**
     * Private constructor for singleton pattern.
     */
    private NetworkRequestManager() {
    }

    public static NetworkRequestManager getInstance() {
        return singleInstance;
    }

    public static void initQueue(Context context) {
        //requestQueue = Volley.newRequestQueue(context, new SslHttpStack(new SslHttpClient(context, 44400)));
        requestQueue = Volley.newRequestQueue(context);
    }

    public static RequestQueue getRequestQueue() {
        if (requestQueue != null) {
            return requestQueue;
        } else {
            throw new IllegalStateException("Queue Not Initialized");
        }
    }

    private <T> void addToRequestQueue(BaseRequest<T> request){
        request.setRetryPolicy(new DefaultRetryPolicy(TIME_OUT_POLICY_DURATION,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(request);
    }

    @Override
    public void sendMarkerRequest(String ip, Response.Listener<MarkerModel> listener, Response.ErrorListener errorListener) {
        BaseRequest<MarkerModel> request = new BaseRequest<>(Request.Method.GET,
                UrlGenerator.getIpSpecificUrl(UrlGenerator.Precision.CITY, ip)
                , errorListener, listener, new BaseRequest.ClassParseStrategy(MarkerModel.class));
        addToRequestQueue(request);
    }

}