package com.taxireservation;

import java.util.ArrayList;
import java.util.List;

/** Matching rules for the customer-facing driver selector. */
public final class DriverAvailability {
    private DriverAvailability() { }

    /** A taxi booking must match the driver's registered cab category. */
    public static List<Driver> forCabType(List<Driver> available, String cabType) {
        List<Driver> matches = new ArrayList<>();
        if (available == null || cabType == null) return matches;
        for (Driver driver : available) {
            if (driver != null && driver.getVehicleType() != null
                    && cabType.trim().equalsIgnoreCase(driver.getVehicleType().trim())) {
                matches.add(driver);
            }
        }
        return matches;
    }

    /** Returns the first selectable cab category with at least one available driver. */
    public static String firstAvailableType(List<Driver> available, String[] cabTypes) {
        for (String type : cabTypes) {
            if (!forCabType(available, type).isEmpty()) return type;
        }
        return null;
    }

    /** Concise category list for explaining an empty selection. */
    public static String availableTypes(List<Driver> available, String[] cabTypes) {
        List<String> types = new ArrayList<>();
        for (String type : cabTypes) {
            if (!forCabType(available, type).isEmpty()) types.add(type);
        }
        return types.isEmpty() ? "none" : String.join(", ", types);
    }
}
