package com.ecoquest.util;

import com.ecoquest.entity.embedded.GeoLocation;

public class GeoUtils {

    private static final double EARTH_RADIUS_METERS = 6_371_000;

    private GeoUtils() {
    }

    public static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    public static boolean isWithinRadius(GeoLocation target, double lat, double lon, int radiusMeters) {
        if (target == null || target.getLatitude() == null || target.getLongitude() == null) {
            return true; // no target to compare against - nothing to flag
        }
        return distanceMeters(target.getLatitude(), target.getLongitude(), lat, lon) <= radiusMeters;
    }
}
