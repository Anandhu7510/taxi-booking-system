package com.taxireservation;

import java.util.ArrayList;
import java.util.List;

/** Vehicle matching rules shared by the customer booking form and its tests. */
public final class DriverAvailability {
    public static final String ALL_TYPES = "All available cab types";
    public static final String[] CAB_TYPES = {"Mini", "Sedan", "SUV", "Luxury"};

    private DriverAvailability() { }

    /** Returns a canonical cab category, or null for unknown database values. */
    public static String canonicalCabType(String value) {
        if (value == null) return null;
        for (String type : CAB_TYPES) {
            if (type.equalsIgnoreCase(value.trim())) return type;
        }
        return null;
    }

    /** Taxi + Driver may offer any available driver with a supported vehicle. */
    public static List<Driver> forCabType(List<Driver> available, String filter) {
        List<Driver> matches = new ArrayList<>();
        if (available == null) return matches;
        boolean all = ALL_TYPES.equals(filter);
        String type = canonicalCabType(filter);
        if (!all && type == null) return matches;
        for (Driver driver : available) {
            if (driver == null) continue;
            String registered = canonicalCabType(driver.getVehicleType());
            if (registered != null && (all || registered.equals(type))) {
                matches.add(driver);
            }
        }
        return matches;
    }

    public static String availableTypes(List<Driver> available) {
        List<String> types = new ArrayList<>();
        for (String type : CAB_TYPES) {
            if (!forCabType(available, type).isEmpty()) types.add(type);
        }
        return types.isEmpty() ? "none" : String.join(", ", types);
    }
}
