package org.refugerestrooms.models;

/**
 * Created by Refuge Restrooms on 7/13/2015.
 */
//Haversine formula to compute shortest distance between two points on a sphere
public class Haversine {
    public static final double R = 6372.8; // Radius of Earth in Kilometers
    public static double formula(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLng / 2) * Math.sin(dLng / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }
}

