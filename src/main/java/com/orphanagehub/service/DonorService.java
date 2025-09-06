package com.orphanagehub.service;

import com.orphanagehub.dao.DonationDAO;
import com.orphanagehub.dao.OrphanageDAO;
import com.orphanagehub.model.Donation;
import com.orphanagehub.model.Orphanage;
import io.vavr.control.Try;
import org.locationtech.jts.geom.Point;

/**
 * Service for donor operations.
 * Handles donations and orphanage search.
 */
public class DonorService {

    private final DonationDAO donationDAO = new DonationDAO();
    private final OrphanageDAO orphanageDAO = new OrphanageDAO();

    /**
     * Makes a donation to an orphanage.
     * @param donation The Donation to record.
     * @return Try<Void> - success on record, failure on error.
     */
    public Try<Void> makeDonation(Donation donation) {
        return donationDAO.create(donation);
    }

    /**
     * Searches orphanages by location (using JTS for geo).
     * @param location The search location (e.g., Point).
     * @param radius The search radius in km.
     * @return Try<List<Orphanage>> - list of matching orphanages.
     */
    public Try<io.vavr.collection.List<Orphanage>> searchByLocation(Point location, double radius) {
        // For now, return all orphanages as location-based filtering requires actual geocoding API
        return orphanageDAO.findAll()
                .map(io.vavr.collection.List::ofAll)
                .map(all -> {
                    // TODO: Implement actual geocoding when API is available
                    // For demonstration, we'll return all orphanages within the "radius"
                    // In production, this would filter based on actual coordinates
                    return all.filter(o -> {
                        Point orphanageLocation = geocode(o.address());
                        if (orphanageLocation == null || location == null) {
                            // If we can't geocode, include the orphanage by default
                            return true;
                        }
                        return distanceTo(location, orphanageLocation) <= radius;
                    });
                });
    }

    // Helper: Geocode address (stub for future implementation)
    private Point geocode(String address) {
        // TODO: Integrate with actual geocoding service (Google Maps, OpenStreetMap, etc.)
        // For now, return null to indicate geocoding not available
        // In production, this would call an API to convert address to lat/lon coordinates
        return null;
    }

    // Helper: Calculate distance (efficient Haversine formula)
    private double distanceTo(Point p1, Point p2) {
        if (p1 == null || p2 == null) return Double.MAX_VALUE;
        double lat1 = p1.getY(), lon1 = p1.getX(), lat2 = p2.getY(), lon2 = p2.getX();
        double dLat = Math.toRadians(lat2 - lat1), dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)); // km
    }
}
