package com.taxireservation;

/** Standalone regression checks; run with java -ea or invoke main directly. */
public final class RouteCatalogTest {
    private static void equal(double actual, double expected) {
        if (Math.abs(actual - expected) > 0.001) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }

    private static void route(String from, String to, int distance) {
        equal(RouteCatalog.distanceKm(from, to), distance);
        equal(RouteCatalog.distanceKm(to, from), distance);
    }

    private static void invalidRate(String service, String cab) {
        try {
            RouteCatalog.fare(service, cab, 84);
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Unexpected valid rate: " + service + ", " + cab);
    }

    public static void main(String[] args) {
        route("Thrissur", "Thiruvananthapuram", 281);
        route("Thrissur", "Kochi", 84);
        route("Thrissur", "Palakkad", 65);
        route("Kochi", "Palakkad", 141);
        route("Kochi", "Thiruvananthapuram", 209);
        route("Thiruvananthapuram", "Palakkad", 337);

        equal(RouteCatalog.distanceKm("Thrissur", "Thrissur"), -1);
        equal(RouteCatalog.distanceKm("Select city...", "Kochi"), -1);
        equal(RouteCatalog.distanceKm("Kochi", "Unsupported"), -1);

        equal(RouteCatalog.fare("Taxi + Driver", "Mini", 84), 1256);
        equal(RouteCatalog.fare("Taxi + Driver", "Sedan", 84), 1800);
        equal(RouteCatalog.fare("Taxi + Driver", "SUV", 84), 2700);
        equal(RouteCatalog.fare("Taxi + Driver", "Luxury", 84), 4450);
        equal(RouteCatalog.fare("Driver Only (Own Vehicle)", "Own Vehicle", 84), 890);

        invalidRate("Taxi + Driver", "Unknown");
        invalidRate("Unknown", "Mini");
        System.out.println("PASS: six routes both ways, invalid choices, and all five fare rates.");
    }
}
