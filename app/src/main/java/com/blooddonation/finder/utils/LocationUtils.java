package com.blooddonation.finder.utils;

public class LocationUtils {

    // Haversine formula to calculate distance between two GPS coordinates in km
    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // Check if donor is eligible to donate again (56-day cooldown)
    public static boolean isEligibleToDonate(long lastDonationDate) {
        if (lastDonationDate == 0) return true;
        long daysSince = (System.currentTimeMillis() - lastDonationDate) / (1000L * 60 * 60 * 24);
        return daysSince >= 56;
    }

    // Get days until next eligible donation
    public static long daysUntilNextDonation(long lastDonationDate) {
        if (lastDonationDate == 0) return 0;
        long daysSince = (System.currentTimeMillis() - lastDonationDate) / (1000L * 60 * 60 * 24);
        return Math.max(0, 56 - daysSince);
    }
}
