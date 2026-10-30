package com.taxireservation;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Booking and status changes use transactions so driver availability stays consistent. */
public final class ReservationDAO {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ReservationDAO() {}

    public static List<Reservation> list(Integer customerId) throws SQLException {
        String sql = "SELECT r.*, d.name AS driver_name FROM reservations r "
                + "JOIN drivers d ON d.id = r.driver_id"
                + (customerId == null ? "" : " WHERE r.customer_id = ?")
                + " ORDER BY r.id";
        List<Reservation> reservations = new ArrayList<>();
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (customerId != null) statement.setInt(1, customerId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    reservations.add(new Reservation(rows.getInt("id"),
                            rows.getString("customer_name"), rows.getString("customer_phone"),
                            rows.getString("pickup_location"), rows.getString("drop_location"),
                            rows.getTimestamp("scheduled_at").toLocalDateTime().format(FORMAT),
                            rows.getString("service_type"), rows.getString("cab_type"),
                            rows.getString("customer_vehicle"), rows.getDouble("estimated_km"),
                            rows.getString("driver_name"), rows.getDouble("fare"),
                            rows.getString("status")));
                }
            }
        }
        return reservations;
    }

    public static void book(Integer customerId, String customerName, String customerPhone,
                            String pickup, String drop, String dateTime, String serviceType,
                            String cabType, String customerVehicle, double estimatedKm,
                            int driverId, double fare) throws SQLException {
        if (!Double.isFinite(estimatedKm) || estimatedKm <= 0 || !Double.isFinite(fare) || fare < 0) {
            throw new SQLException("Invalid fare or distance.");
        }
        // Recheck the selected route and fare before MySQL persists the booking.
        // UI display values are never the authority for the trip price.
        int expectedDistance = RouteCatalog.distanceKm(pickup, drop);
        if (expectedDistance <= 0) {
            throw new SQLException("Select two different supported cities.");
        }
        final double expectedFare;
        try {
            expectedFare = RouteCatalog.fare(serviceType, cabType, expectedDistance);
        } catch (IllegalArgumentException ex) {
            throw new SQLException(ex.getMessage(), ex);
        }
        if (Math.abs(estimatedKm - expectedDistance) > 0.001
                || Math.abs(fare - expectedFare) > 0.01) {
            throw new SQLException("The route distance or fare is out of date. "
                    + "Select the route again before booking.");
        }
        LocalDateTime scheduled;
        try {
            scheduled = LocalDateTime.parse(dateTime.trim(), FORMAT);
        } catch (RuntimeException ex) {
            throw new SQLException("Date/time must use yyyy-MM-dd HH:mm.", ex);
        }

        try (Connection connection = DatabaseConnection.open()) {
            connection.setAutoCommit(false);
            try {
                String claimSql = "UPDATE drivers d SET d.status = 'On Trip' "
                        + "WHERE d.id = ? AND d.is_active = TRUE AND d.status = 'Available' "
                        + "AND NOT EXISTS (SELECT 1 FROM reservations r WHERE r.driver_id = d.id "
                        + "AND r.status IN ('Pending', 'Ongoing'))";
                if ("Taxi + Driver".equals(serviceType)) claimSql += " AND d.vehicle_type = ?";
                try (PreparedStatement claim = connection.prepareStatement(claimSql)) {
                    claim.setInt(1, driverId);
                    if ("Taxi + Driver".equals(serviceType)) claim.setString(2, cabType);
                    if (claim.executeUpdate() != 1) {
                        throw new SQLException("The selected driver is no longer available for this service.");
                    }
                }

                String insertSql = "INSERT INTO reservations (customer_id, driver_id, customer_name, "
                        + "customer_phone, pickup_location, drop_location, scheduled_at, service_type, "
                        + "cab_type, customer_vehicle, estimated_km, fare, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Pending')";
                try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                    if (customerId == null) insert.setNull(1, Types.INTEGER);
                    else insert.setInt(1, customerId);
                    insert.setInt(2, driverId);
                    insert.setString(3, customerName.trim());
                    insert.setString(4, customerPhone.trim());
                    insert.setString(5, pickup.trim());
                    insert.setString(6, drop.trim());
                    insert.setTimestamp(7, Timestamp.valueOf(scheduled));
                    insert.setString(8, serviceType);
                    insert.setString(9, cabType);
                    insert.setString(10, customerVehicle);
                    insert.setDouble(11, estimatedKm);
                    insert.setDouble(12, fare);
                    insert.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    /** Permanently remove only finished/cancelled records; never orphan active drivers. */
    public static void deleteFinished(int reservationId) throws SQLException {
        String sql = "DELETE FROM reservations WHERE id = ? AND status IN ('Completed', 'Cancelled')";
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reservationId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Only completed or cancelled reservations can be deleted. "
                        + "Refresh the list and check its current status.");
            }
        }
    }

    /**
     * Cancel an active booking only when it belongs to the authenticated customer.
     * Assignment release and reservation status change are committed together.
     */
    public static void cancelOwn(int reservationId, int customerId) throws SQLException {
        if (customerId <= 0 || reservationId <= 0) {
            throw new SQLException("Invalid reservation or customer.");
        }
        try (Connection connection = DatabaseConnection.open()) {
            connection.setAutoCommit(false);
            try {
                String currentStatus;
                int driverId;
                try (PreparedStatement select = connection.prepareStatement(
                        "SELECT status, driver_id FROM reservations "
                        + "WHERE id = ? AND customer_id = ? FOR UPDATE")) {
                    select.setInt(1, reservationId);
                    select.setInt(2, customerId);
                    try (ResultSet rows = select.executeQuery()) {
                        if (!rows.next()) {
                            throw new SQLException("Reservation not found for this customer account.");
                        }
                        currentStatus = rows.getString("status");
                        driverId = rows.getInt("driver_id");
                    }
                }
                if (!"Pending".equals(currentStatus) && !"Ongoing".equals(currentStatus)) {
                    throw new SQLException("This reservation is already " + currentStatus
                            + " and cannot be cancelled.");
                }
                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE reservations SET status = 'Cancelled' "
                        + "WHERE id = ? AND customer_id = ? AND status IN ('Pending', 'Ongoing')")) {
                    update.setInt(1, reservationId);
                    update.setInt(2, customerId);
                    if (update.executeUpdate() != 1) {
                        throw new SQLException("This reservation was updated by another session.");
                    }
                }
                try (PreparedStatement release = connection.prepareStatement(
                        "UPDATE drivers d SET d.status = 'Available' "
                        + "WHERE d.id = ? AND d.is_active = TRUE "
                        + "AND NOT EXISTS (SELECT 1 FROM reservations r WHERE r.driver_id = d.id "
                        + "AND r.status IN ('Pending', 'Ongoing'))")) {
                    release.setInt(1, driverId);
                    release.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    public static void changeStatus(int reservationId, String newStatus) throws SQLException {
        if (!"Ongoing".equals(newStatus) && !"Completed".equals(newStatus)
                && !"Cancelled".equals(newStatus)) {
            throw new SQLException("Invalid reservation status.");
        }
        try (Connection connection = DatabaseConnection.open()) {
            connection.setAutoCommit(false);
            try {
                String currentStatus;
                int driverId;
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT status, driver_id FROM reservations WHERE id = ? FOR UPDATE")) {
                    statement.setInt(1, reservationId);
                    try (ResultSet rows = statement.executeQuery()) {
                        if (!rows.next()) throw new SQLException("Reservation was not found.");
                        currentStatus = rows.getString("status");
                        driverId = rows.getInt("driver_id");
                    }
                }
                boolean active = "Pending".equals(currentStatus) || "Ongoing".equals(currentStatus);
                if (!active || ("Pending".equals(newStatus) && !"Pending".equals(currentStatus))
                        || ("Ongoing".equals(newStatus) && !"Pending".equals(currentStatus))) {
                    throw new SQLException("This reservation cannot transition from "
                            + currentStatus + " to " + newStatus + ".");
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE reservations SET status = ? WHERE id = ?")) {
                    statement.setString(1, newStatus);
                    statement.setInt(2, reservationId);
                    statement.executeUpdate();
                }
                if (!"Ongoing".equals(newStatus)) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE drivers d SET d.status = 'Available' "
                            + "WHERE d.id = ? AND d.is_active = TRUE "
                            + "AND NOT EXISTS (SELECT 1 FROM reservations r WHERE r.driver_id = d.id "
                            + "AND r.status IN ('Pending', 'Ongoing'))")) {
                        statement.setInt(1, driverId);
                        statement.executeUpdate();
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }
}
