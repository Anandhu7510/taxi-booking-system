package com.taxireservation;

import java.util.Arrays;
import java.util.List;

/** Standalone checks for taxi + driver selection; no MySQL connection required. */
public final class DriverAvailabilityTest {
    private static Driver driver(int id, String type) {
        return new Driver(id, "Driver " + id, "9999999999", "LIC" + id,
                "KL" + id, type, "Available");
    }

    private static void expect(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        Driver mini = driver(1, "Mini");
        Driver sedan = driver(2, " sedan ");
        Driver suv = driver(3, "SUV");
        Driver invalid = driver(4, "Unrecognized Type");
        List<Driver> available = Arrays.asList(mini, sedan, suv, invalid);

        // A fresh booking defaults to ALL supported drivers, not whichever
        // vehicle category happened to be selected on initial construction.
        List<Driver> everyone = DriverAvailability.forCabType(
                available, DriverAvailability.ALL_TYPES);
        expect(everyone.size() == 3, "All supported taxi + driver pairs should appear.");
        expect(everyone.get(0).getId() == 1 && everyone.get(2).getId() == 3,
                "Driver IDs and database ordering must not be lost.");

        expect(DriverAvailability.forCabType(available, "Sedan").size() == 1,
                "Sedan filter must show only the Sedan driver.");
        expect(DriverAvailability.forCabType(available, "SEDAN").get(0).getId() == 2,
                "Case and spaces in stored vehicle types must be tolerated.");
        expect(DriverAvailability.forCabType(available, "Luxury").isEmpty(),
                "Filtering an unavailable type must not show another type.");
        expect("Sedan".equals(DriverAvailability.canonicalCabType(" sedan ")),
                "Fare calculations need the canonical vehicle class.");
        expect(DriverAvailability.canonicalCabType("unknown") == null,
                "An unknown vehicle type must never reach taxi pricing.");
        expect("Mini, Sedan, SUV".equals(DriverAvailability.availableTypes(available)),
                "Available type hint should match records.");
        expect(DriverAvailability.forCabType(List.of(), DriverAvailability.ALL_TYPES).isEmpty(),
                "Zero available drivers should return an empty list.");
        expect("none".equals(DriverAvailability.availableTypes(List.of())),
                "Empty records must produce a useful hint.");

        // Refresh must reflect newly supplied DB rows and removals; the
        // helper intentionally caches nothing.
        List<Driver> afterAdminAddedDriver = Arrays.asList(mini, sedan, suv,
                driver(5, "Luxury"));
        expect(DriverAvailability.forCabType(afterAdminAddedDriver,
                DriverAvailability.ALL_TYPES).size() == 4,
                "Refreshing after admin adds a driver must show the new record.");
        expect(DriverAvailability.forCabType(List.of(sedan),
                DriverAvailability.ALL_TYPES).size() == 1,
                "Refreshing after a driver goes offline must remove it.");

        // Existing fare calculator must accept the canonical type from driver.
        double fare = RouteCatalog.fare("Taxi + Driver",
                DriverAvailability.canonicalCabType(sedan.getVehicleType()), 84);
        expect(Math.abs(fare - 1800.0) < 0.001, "Selected Sedan must price as Sedan.");
        System.out.println("PASS: all/filter views, DB refresh semantics, matching, and fare.");
    }
}
