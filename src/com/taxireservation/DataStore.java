package com.taxireservation;

import java.sql.SQLException;
import java.util.List;

/** Existing UI-facing facade. MySQL is the sole persistent source of truth. */
public final class DataStore {
    private static final DataStore INSTANCE = new DataStore();
    private DataStore() {}
    public static DataStore get() { return INSTANCE; }

    public List<Driver> getDrivers() {
        try { return DriverDAO.list(); }
        catch (SQLException ex) { throw failure(ex); }
    }
    public List<Driver> getAvailableDrivers(String vehicleType) {
        try { return DriverDAO.available(vehicleType); }
        catch (SQLException ex) { throw failure(ex); }
    }
    public void addDriver(String name, String phone, String licenseNo, String vehicleNumber,
                          String vehicleType, String status) {
        if (!"Available".equals(status)) throw new IllegalArgumentException("Invalid initial status.");
        try { DriverDAO.add(name, phone, licenseNo, vehicleNumber, vehicleType); }
        catch (SQLException ex) { throw failure(ex); }
    }
    public void removeDriver(int id) {
        try { DriverDAO.remove(id); }
        catch (SQLException ex) { throw failure(ex); }
    }
    public void setDriverStatus(int id, String status) {
        try { DriverDAO.setManualStatus(id, status); }
        catch (SQLException ex) { throw failure(ex); }
    }
    public List<Reservation> getReservations() {
        try { return ReservationDAO.list(null); }
        catch (SQLException ex) { throw failure(ex); }
    }
    public List<Reservation> getReservationsForCustomer(int customerId) {
        try { return ReservationDAO.list(customerId); }
        catch (SQLException ex) { throw failure(ex); }
    }
    public void bookReservation(Integer customerId, String customerName, String customerPhone,
                                String pickup, String drop, String dateTime, String serviceType,
                                String cabType, String customerVehicle, double estimatedKm,
                                int driverId, double fare) {
        try {
            ReservationDAO.book(customerId, customerName, customerPhone, pickup, drop, dateTime,
                    serviceType, cabType, customerVehicle, estimatedKm, driverId, fare);
        } catch (SQLException ex) { throw failure(ex); }
    }
    public void updateReservationStatus(int reservationId, String status) {
        try { ReservationDAO.changeStatus(reservationId, status); }
        catch (SQLException ex) { throw failure(ex); }
    }
    public int countByStatus(String status) {
        int count = 0;
        for (Reservation reservation : getReservations())
            if (status.equals(reservation.getStatus())) count++;
        return count;
    }
    public double totalRevenue() {
        double total = 0;
        for (Reservation reservation : getReservations())
            if ("Completed".equals(reservation.getStatus())) total += reservation.getFare();
        return total;
    }
    private static IllegalStateException failure(SQLException ex) {
        return new IllegalStateException("MySQL operation failed: " + ex.getMessage(), ex);
    }
}
