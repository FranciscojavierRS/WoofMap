package com.francisco.woofmapapi.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public final class GeometryUtils {

    public static final int SRID_WGS84 = 4326;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), SRID_WGS84);
    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private GeometryUtils() {
    }

    /**
     * Crea un Point WGS84 (SRID 4326) a partir de latitud y longitud.
     * En JTS/GIS, la coordenada X corresponde a la longitud y la Y a la latitud.
     */
    public static Point createPoint(double lat, double lon) {
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(lon, lat));
        point.setSRID(SRID_WGS84);
        return point;
    }

    public static double getLat(Point point) {
        if (point == null) {
            return 0.0;
        }
        return point.getY();
    }

    public static double getLon(Point point) {
        if (point == null) {
            return 0.0;
        }
        return point.getX();
    }

    /**
     * Calcula la distancia en metros entre dos coordenadas geograficas usando la formula del semiverseno (Haversine).
     */
    public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round((EARTH_RADIUS_METERS * c) * 10.0) / 10.0; // redondeo a 1 decimal
    }
}
