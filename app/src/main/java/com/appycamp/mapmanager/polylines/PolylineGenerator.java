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

    private static final boolean VIA_NETWORKING = false;
    private static final double MAX_POLYLINE_DISTANCE = .5;

    public interface OnPolylineCreatedListener{
        void onPolylineCreated(PolylineOptions polylineOptions);
    }

    public static void generatePolyline(final OnPolylineCreatedListener listener){
        /*
        Attempt to use google directions to pull in actual polylines for directions via roads to
        randomly generated positions on the map. The downside, super low quota means that this
        isn't really a viable means of testing.
         */
        if(VIA_NETWORKING){
            LatLng endLatLng = generateDestinationLatLng(MapsActivity.TRANS_LOC_LAT, MapsActivity.TRANS_LOC_LNG);
            String url = getDestinationUrl(endLatLng.latitude, endLatLng.longitude);
            NetworkRequestManager.getInstance().fetchPolylines(url, new Response.Listener<DirectionsResult>() {
                @Override
                public void onResponse(DirectionsResult directionsResult) {
                    if (directionsResult != null && directionsResult.routes.size() != 0) {
                        String encodedPoints = directionsResult.routes.get(0).overview_polyLine.points;
                        List<LatLng> latLngs = PolyUtil.decode(encodedPoints);
                        if (listener != null)
                            listener.onPolylineCreated(new PolylineOptions()
                                    .addAll(latLngs)
                                    .width(3)
                                    .color(0x7F0000FF));
                    }
                    else
                        reportError(listener, new VolleyError("Null response"));
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    reportError(listener, error);
                }
            });

        }
        else
            new GeneratePolyline(listener).execute();
    }

    /**
     * If we get an error in our networking call (whether it be a bad response, or an actual error response from the server)
     * we can report it through this call which will log our response and generate a dummy polyline in its place
     *
     * @param listener
     * @param error
     */
    private static void reportError(OnPolylineCreatedListener listener, VolleyError error){
        Log.e(PolylineGenerator.class.getSimpleName(), "Error: " + error == null ? "Bad network call!" : error.getMessage());
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

    /**
     * Will generate our url for Google Directions from our generic starting point to our randomly generated destination point
     * to be utilized in network calls.
     *
     * @param latitude
     * @param longitude
     * @return
     */
    private static String getDestinationUrl(double latitude, double longitude) {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=" + MapsActivity.TRANS_LOC_LAT + "," +
                MapsActivity.TRANS_LOC_LAT + "&destination=" + latitude + "," + longitude; // + "&key=" + MapsActivity.API_KEY;
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
            waypoints.add(MapsActivity.officeCoords);
            double lat = MapsActivity.officeCoords.latitude;
            double lng = MapsActivity.officeCoords.longitude;
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

    /*
     * Classes to utilize for Volley parsing of Google Directions response
     */
    public static class DirectionsResult {
        public List<Route> routes;
    }

    public static class Route {
        public OverviewPolyLine overview_polyLine;
    }

    public static class OverviewPolyLine {
        public String points;
    }
}
