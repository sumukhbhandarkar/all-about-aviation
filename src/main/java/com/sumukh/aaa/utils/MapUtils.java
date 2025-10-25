package com.sumukh.aaa.utils;

import com.sumukh.aaa.model.Runway;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MapUtils {
    // --- Put these near your controller (or in a small util class) ---
    public record MapLine(double lat1, double lon1, double lat2, double lon2, String label) {}
    public record MapModel(double lat, double lon, String iata, String city, List<MapLine> runways) {}

    private static final double EARTH_R = 6_371_000; // meters

    private static double toRad(double deg) { return Math.toRadians(deg); }
    private static double toDeg(double rad) { return Math.toDegrees(rad); }

    // Destination point given start (lat,lon), bearing (deg), distance (m)
    private static double[] destPoint(double latDeg, double lonDeg, double bearingDeg, double distMeters) {
        double δ = distMeters / EARTH_R;
        double θ = toRad(bearingDeg);
        double φ1 = toRad(latDeg);
        double λ1 = toRad(lonDeg);

        double sinφ1 = Math.sin(φ1), cosφ1 = Math.cos(φ1);
        double sinδ = Math.sin(δ), cosδ = Math.cos(δ);
        double sinφ2 = sinφ1 * cosδ + cosφ1 * sinδ * Math.cos(θ);
        double φ2 = Math.asin(sinφ2);
        double y = Math.sin(θ) * sinδ * cosφ1;
        double x = cosδ - sinφ1 * sinφ2;
        double λ2 = λ1 + Math.atan2(y, x);

        return new double[]{ toDeg(φ2), ((toDeg(λ2) + 540) % 360) - 180 }; // normalize lon
    }

    // Parse a heading from a runway ident like "09", "09L/27R", "18", etc.
    private static Double headingFromIdent(String ident) {
        if (ident == null) return null;
        // Take the first numeric part (09L -> 09)
        var m = java.util.regex.Pattern.compile("^(\\d{2})").matcher(ident.trim());
        if (!m.find()) return null;
        int tens = Integer.parseInt(m.group(1));
        return tens * 10.0; // 09 -> 90 degrees
    }

    // Build one center line per runway (if we know length and heading)
    public List<MapLine> buildRunwayLines(double apLat, double apLon, List<Runway> runways) {
        List<MapLine> lines = new java.util.ArrayList<>();
        for (Runway r : runways) {
            Double hdg = headingFromIdent(r.getIdentifier());
            if (hdg == null) continue;

            // Use actual length if present; otherwise show a short 1km visual
            double length = (r.getLengthMeters() != null && r.getLengthMeters() > 0) ? r.getLengthMeters() : 1000.0;
            double half = length / 2.0;

            // Endpoints along the heading and opposite heading, from airport center
            double[] a = destPoint(apLat, apLon, hdg, half);
            double[] b = destPoint(apLat, apLon, (hdg + 180.0) % 360.0, half);

            String label = (r.getIdentifier() != null ? r.getIdentifier() : "RWY") +
                    (r.getSurface() != null ? (" • " + r.getSurface()) : "");
            lines.add(new MapLine(a[0], a[1], b[0], b[1], label));
        }
        return lines;
    }

}
