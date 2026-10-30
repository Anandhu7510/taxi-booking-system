package com.taxireservation;

import java.util.Arrays;
import java.util.List;

/** No database needed: verify the taxi-category filtering used by the booking UI. */
public final class DriverAvailabilityTest {
    private static final String[] TYPES = {"Mini", "Sedan", "SUV", "Luxury"};

    private static Driver driver(int id, String type) {
        return new Driver(id, "Driver " + id, "9999999999", "LIC" + id, "KL" + id,
                type, "Available");
    }

    private static void expect(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        Driver mini = driver(1, "Mini");
        Driver sedan = driver(2, "Sedan ");
        Driver suv = driver(3, "SUV");
        List<Driver> available = Arrays.asList(mini, sedan, suv);

        expect(DriverAvailability.forCabType(available, "Sedan").size() == 1,
                "A Sedan should be selectable when registered.");
        expect(DriverAvailability.forCabType(available, "Sedan").get(0).getId() == 2,
                "Only the matching Sedan should be returned.");
        expect(DriverAvailability.forCabType(available, "Luxury").isEmpty(),
                "An unavailable category must not show the wrong vehicle.");
        expect(DriverAvailability.forCabType(available, "suv").get(0).getId() == 3,
                "Matching should tolerate case differences.");
        expect("Mini".equals(DriverAvailability.firstAvailableType(available, TYPES)),
                "First available type should be selected at initial booking load.");
        expect("Mini, Sedan, SUV".equals(DriverAvailability.availableTypes(available, TYPES)),
                "Available category hint should list actual driver types.");
        expect(DriverAvailability.firstAvailableType(List.of(), TYPES) == null,
                "An empty database must not choose an invalid cab type.");
        expect("none".equals(DriverAvailability.availableTypes(List.of(), TYPES)),
                "Empty database should be explained.");
        expect(DriverAvailability.forCabType(List.of(), "Sedan").isEmpty(),
                "An empty driver list must not throw.");
        System.out.println("PASS: cab-type filtering, initial selection, and no-match hints.");
    }
}
