package com.appycamp.mapmanager.markers;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appycamp.mapmanager.KeyGenerator;
import com.appycamp.mapmanager.R;
import com.appycamp.mapmanager.network.NetworkRequestManager;
import com.appycamp.mapmanager.network.UrlGenerator;
import com.appycamp.mapmanager.network.models.MarkerModel;

/**
 * Created by Zach on 6/16/2015.
 */
public class MarkerRequestService extends IntentService {

    public static final String START_IP_KEY = KeyGenerator.generateKey("START_IP");
    public static final String END_IP_KEY = KeyGenerator.generateKey(("END_IP"));
    public static long IP_TOTAL_COUNT = 0;
    private static long IP_ATTEMPT_COUNT = 0;

    private MyMarkerManager mMarkerManager;
    private String mStartIp, mEndIp, mCurrIp;

    public MarkerRequestService(){
        super(MarkerRequestService.class.getSimpleName());
        mMarkerManager = MyMarkerManager.getInstance();
    }

    public MarkerRequestService(String name) {
        super(name);
        mMarkerManager = MyMarkerManager.getInstance();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        IP_ATTEMPT_COUNT = 0;

        mStartIp = intent.getStringExtra(START_IP_KEY);
        mEndIp = intent.getStringExtra(END_IP_KEY);
        if(TextUtils.isEmpty(mStartIp) || TextUtils.isEmpty(mEndIp)) {
            Log.e(getClass().getSimpleName(), "Error passing IP addresses to service");
            return;
        }

        if(!IpManager.validateIpRangeOrder(mStartIp, mEndIp)){
            String tempMStartIp = mStartIp;
            mStartIp = mEndIp;
            mEndIp = tempMStartIp;
        }

        IP_TOTAL_COUNT = IP_TOTAL_COUNT + IpManager.getIpRangeSize(mStartIp, mEndIp) + 1;

        mCurrIp = mStartIp;
        makeIpCall(mCurrIp);
    }

    private void makeIpCall(final String ip){
        IP_ATTEMPT_COUNT++;

        NetworkRequestManager.getInstance().sendMarkerRequest(ip,
                new Response.Listener<MarkerModel>() {
                    @Override
                    public void onResponse(MarkerModel response) {
                        if (response != null && !(response.getLatitude() == 0 && response.getLongitude() == 0)) {
                            mMarkerManager.addMarker(response);
                            sendSuccessResponse(true);
                        } else {
                            Toast.makeText(MarkerRequestService.this,
                                    getString(R.string.toast_marker_pull_error_prefix) + mCurrIp, Toast.LENGTH_LONG).show();
                            sendSuccessResponse(false);
                        }

                        incrementIpNetworkCall();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MarkerRequestService.this,
                                getString(R.string.toast_generic_error_prefix) + error.getMessage(), Toast.LENGTH_LONG).show();
                        sendSuccessResponse(false);
                        incrementIpNetworkCall();
                    }
                });
    }

    private void sendSuccessResponse(boolean success){
        if(mMarkerManager.getListener() != null)
            mMarkerManager.getListener().onMarkerRequestComplete(success);
    }

    private void incrementIpNetworkCall(){
        mCurrIp = IpManager.getNextIpInRange(mCurrIp, mEndIp);
        if(mCurrIp == null)
            mMarkerManager.getListener().onAllMarkersRequested();
        else
            makeIpCall(mCurrIp);
    }

    public static int getCurrProgressStatus(){
        return (int) (((double) IP_ATTEMPT_COUNT / IP_TOTAL_COUNT) * 100);
    }

}
