package com.taxireservation;

/**
 * Fixed, offline route estimates supplied for this college microproject.
 * Distances are the same in either direction; these are not live road distances.
 */
public final class RouteCatalog {
    private static final String[] CITIES = {
            "Thrissur", "Kochi", "Palakkad", "Thiruvananthapuram"
    };

    // Rows/columns follow CITIES. Zero represents the same city (invalid booking).
    private static final int[][] DISTANCES_KM = {
            {0, 84, 65, 281},
            {84, 0, 141, 209},
            {65, 141, 0, 337},
            {281, 209, 337, 0}
    };

    private RouteCatalog() { }

    /** Placeholder first so a journey must be selected explicitly. */
    public static String[] cityOptions() {
        String[] options = new String[CITIES.length + 1];
        options[0] = "Select city...";
        System.arraycopy(CITIES, 0, options, 1, CITIES.length);
        return options;
    }

    /** Returns -1 for unselected, unknown, or identical cities. */
    public static int distanceKm(String from, String to) {
        int start = indexOf(from);
        int end = indexOf(to);
        return start < 0 || end < 0 || start == end ? -1 : DISTANCES_KM[start][end];
    }

    private static int indexOf(String city) {
        if (city == null) return -1;
        for (int i = 0; i < CITIES.length; i++) {
            if (CITIES[i].equals(city)) return i;
        }
        return -1;
    }

    /**
     * Uses the project's existing base + per-kilometre rates.
     * Unknown service/cab combinations are rejected instead of being silently priced.
     */
    public static double fare(String serviceType, String cabType, double distanceKm) {
        if (!Double.isFinite(distanceKm) || distanceKm <= 0) {
            throw new IllegalArgumentException("Select two different supported cities.");
        }
        double base;
        double perKm;
        if ("Driver Only (Own Vehicle)".equals(serviceType)) {
            base = 50;
            perKm = 10;
        } else if ("Taxi + Driver".equals(serviceType)) {
            if ("Mini".equals(cabType)) { base = 80; perKm = 14; }
            else if ("Sedan".equals(cabType)) { base = 120; perKm = 20; }
            else if ("SUV".equals(cabType)) { base = 180; perKm = 30; }
            else if ("Luxury".equals(cabType)) { base = 250; perKm = 50; }
            else throw new IllegalArgumentException("Unsupported cab type.");
        } else {
            throw new IllegalArgumentException("Unsupported service type.");
        }
        return base + perKm * distanceKm;
    }
}
