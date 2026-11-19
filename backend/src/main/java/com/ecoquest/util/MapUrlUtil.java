package com.ecoquest.util;

public class MapUrlUtil {

    private MapUrlUtil() {
    }

    public static String openStreetMapUrl(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        return String.format("https://www.openstreetmap.org/?mlat=%s&mlon=%s#map=17/%s/%s",
                latitude, longitude, latitude, longitude);
    }
}
