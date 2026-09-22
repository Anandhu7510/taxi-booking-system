package com.taxireservation;

import java.util.ArrayList;
import java.util.List;

/**
 * Central in-memory data store (Singleton).
 * Swap the internals of this class for JDBC calls to MySQL/SQLite
 * later without touching any of the UI panels.
 */
public class DataStore {
    private static final DataStore INSTANCE = new DataStore();
    public static DataStore get() { return INSTANCE; }

    private final List<Driver> drivers = new ArrayList<>();
    private final List<Reservation> reservations = new ArrayList<>();
    private int nextDriverId = 1;
    private int nextReservationId = 1001;

    private DataStore() {
        // seed sample data so the app looks alive on first run
        addDriver("Ramesh Kumar", "9876500001", "DL-45-9981", "TN09-AB-1234", "Sedan", "Available");
        addDriver("Suresh Babu", "9876500002", "DL-45-3321", "TN09-CD-5678", "SUV", "Available");
        addDriver("Anita Rao", "9876500003", "DL-45-7765", "TN09-EF-9012", "Mini", "On Trip");
        addDriver("Vikram Singh", "9876500004", "DL-45-1190", "TN09-GH-3456", "Luxury", "Offline");

        addReservation("John Mathew", "9000011111", "Airport", "Downtown Mall",
                "2026-09-09 10:30", "Sedan", "Ramesh Kumar", 420.0, "Ongoing");
        addReservation("Priya Nair", "9000022222", "Railway Station", "Tech Park",
                "2026-09-09 08:15", "Mini", "Anita Rao", 260.0, "Completed");
        addReservation("Farhan Ali", "9000033333", "Hotel Taj", "City Center",
                "2026-09-08 19:00", "SUV", "Suresh Babu", 540.0, "Completed");
    }

    // ---------- Drivers ----------
    public List<Driver> getDrivers() { return drivers; }

    public List<Driver> getAvailableDrivers() {
        List<Driver> result = new ArrayList<>();
        for (Driver d : drivers) if (d.getStatus().equals("Available")) result.add(d);
        return result;
    }

    public Driver addDriver(String name, String phone, String licenseNo,
                             String vehicleNumber, String vehicleType, String status) {
        Driver d = new Driver(nextDriverId++, name, phone, licenseNo, vehicleNumber, vehicleType, status);
        drivers.add(d);
        return d;
    }

    public void removeDriver(Driver d) { drivers.remove(d); }

    public void setDriverStatus(String driverName, String status) {
        for (Driver d : drivers) if (d.getName().equals(driverName)) d.setStatus(status);
    }

    // ---------- Reservations ----------
    public List<Reservation> getReservations() { return reservations; }

    public Reservation addReservation(String customerName, String customerPhone, String pickup,
                                       String drop, String dateTime, String cabType,
                                       String driverName, double fare, String status) {
        Reservation r = new Reservation(nextReservationId++, customerName, customerPhone, pickup,
                drop, dateTime, cabType, driverName, fare, status);
        reservations.add(r);
        return r;
    }

    public int countByStatus(String status) {
        int c = 0;
        for (Reservation r : reservations) if (r.getStatus().equals(status)) c++;
        return c;
    }

    public double totalRevenue() {
        double t = 0;
        for (Reservation r : reservations) if (r.getStatus().equals("Completed")) t += r.getFare();
        return t;
    }
}
