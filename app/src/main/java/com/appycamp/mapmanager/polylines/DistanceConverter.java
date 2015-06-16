package com.appycamp.mapmanager.polylines;

import com.google.android.gms.maps.model.LatLng;

import java.util.Random;

/**
 * Created by Zach on 6/13/2015.
 */
public class DistanceConverter {

    private static final double DISTANCE_RANDOMIZER = (double) 1/5;  // so we don't end up with a bunch of polys the same size
    private static final double LAT_LNG_TO_MILES = 60 * 1.1515;
    private static final double MILES_TO_KILOMETERS = 1.609344;
    private static final double MILES_TO_NAUTS = 0.8684;

    public enum Unit{
        MILES, KILOMETERS, NAUTICAL
    }

    public static LatLng shrinkCoordsToRange(double lat1, double lng1, double lat2, double lng2, double maxDistance, Unit unit){
        double currDistance = distance(lat1, lng1, lat2, lng2, unit);
        double multiplier = currDistance > maxDistance? maxDistance / currDistance: 1;
        multiplier = multiplier * DISTANCE_RANDOMIZER;
        double newLat2 = lat1 + ((lat2-lat1) * multiplier);
        double newLng2 = lng1 + ((lng2 - lng1) * multiplier);
        return new LatLng(newLat2, newLng2);
    }

    private static double distance(double lat1, double lng1, double lat2, double lng2, Unit unit) {
        double theta = lng1 - lng2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * LAT_LNG_TO_MILES;
        switch(unit){
            case KILOMETERS:
                dist = dist * MILES_TO_KILOMETERS;
                break;
            case NAUTICAL:
                dist = dist * MILES_TO_NAUTS;
                break;
        }
        return (dist);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

}