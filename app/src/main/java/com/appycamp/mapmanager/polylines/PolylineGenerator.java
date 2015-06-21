package com.appycamp.mapmanager.polylines;

import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appycamp.mapmanager.MapsActivity;
import com.appycamp.mapmanager.network.NetworkRequestManager;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Zach on 6/13/2015.
 */
public class PolylineGenerator {

    private static final double MAX_POLYLINE_DISTANCE = .5;

    public interface OnPolylineCreatedListener{
        void onPolylineCreated(PolylineOptions polylineOptions);
    }

    public static void generatePolyline(final OnPolylineCreatedListener listener){
        new GeneratePolyline(listener).execute();
    }

    /**
     * Will generate a random destination point for us to polyline to. When we create a random polyline opposed
     * to an actual Google directions polyline this will create sequential destination points so that our map
     * can essentially zig-zag (no real pattern to this function as of yet)
     *
     * @return
     */
    private static LatLng generateDestinationLatLng(double startLat, double startLng) {
        Random coordGenerator = new Random();
        boolean xPosDir = coordGenerator.nextBoolean();
        boolean yPosDir = coordGenerator.nextBoolean();
        double endLat = startLat + (coordGenerator.nextDouble() * (xPosDir ? 1 : -1));
        double endLng = startLng + (coordGenerator.nextDouble() * (yPosDir ? 1 : -1));
        return DistanceConverter.shrinkCoordsToRange(startLat, startLng, endLat, endLng, MAX_POLYLINE_DISTANCE, DistanceConverter.Unit.MILES);
    }

    private static class GeneratePolyline extends AsyncTask<Void, Void, PolylineOptions> {

        private OnPolylineCreatedListener mListener;

        public GeneratePolyline(OnPolylineCreatedListener listener) {
            mListener = listener;
        }

        @Override
        protected void onPostExecute(PolylineOptions polylineOptions) {
            if (mListener != null)
                mListener.onPolylineCreated(polylineOptions);
        }

        @Override
        protected PolylineOptions doInBackground(Void... params) {
            List<LatLng> waypoints = new ArrayList<>();
            waypoints.add(MapsActivity.OFFICE_COORDS);
            double lat = MapsActivity.OFFICE_COORDS.latitude;
            double lng = MapsActivity.OFFICE_COORDS.longitude;
            for(int i = 0; i < PolylineManager.NUM_POLYLINES; i++) {
                LatLng nextWaypoint = generateDestinationLatLng(lat, lng);
                waypoints.add(nextWaypoint);
                lat = nextWaypoint.latitude;
                lng = nextWaypoint.longitude;
            }

            return new PolylineOptions()
                    .addAll(waypoints)
                    .width(3)
                    .color(0x7FFF0000);
        }
    }
}
