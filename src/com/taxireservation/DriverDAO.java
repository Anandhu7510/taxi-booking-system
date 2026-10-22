package com.taxireservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Driver records remain in the database after removal to preserve trip history. */
public final class DriverDAO {
    private DriverDAO() {}

    public static List<Driver> list() throws SQLException {
        return read("SELECT * FROM drivers WHERE is_active = TRUE ORDER BY id", null);
    }

    public static List<Driver> available(String vehicleType) throws SQLException {
        String sql = "SELECT d.* FROM drivers d WHERE d.is_active = TRUE "
                + "AND d.status = 'Available' "
                + "AND NOT EXISTS (SELECT 1 FROM reservations r WHERE r.driver_id = d.id "
                + "AND r.status IN ('Pending', 'Ongoing'))";
        if (vehicleType != null) sql += " AND d.vehicle_type = ?";
        return read(sql + " ORDER BY d.id", vehicleType);
    }

    private static List<Driver> read(String sql, String vehicleType) throws SQLException {
        List<Driver> result = new ArrayList<>();
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (vehicleType != null) statement.setString(1, vehicleType);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.add(new Driver(rows.getInt("id"), rows.getString("name"),
                            rows.getString("phone"), rows.getString("license_no"),
                            rows.getString("vehicle_number"), rows.getString("vehicle_type"),
                            rows.getString("status")));
                }
            }
        }
        return result;
    }

    public static void add(String name, String phone, String licenseNo, String vehicleNumber,
                           String vehicleType) throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO drivers(name, phone, license_no, vehicle_number, vehicle_type, status) "
                     + "VALUES (?, ?, ?, ?, ?, 'Available')")) {
            statement.setString(1, name.trim());
            statement.setString(2, phone.trim());
            statement.setString(3, licenseNo.isBlank() ? null : licenseNo.trim());
            statement.setString(4, vehicleNumber.trim());
            statement.setString(5, vehicleType);
            statement.executeUpdate();
        }
    }

    public static void setManualStatus(int id, String status) throws SQLException {
        if (!"Available".equals(status) && !"Offline".equals(status)) {
            throw new SQLException("Unsupported manual driver status.");
        }
        String sql = "UPDATE drivers d SET d.status = ? WHERE d.id = ? AND d.is_active = TRUE "
                + "AND NOT EXISTS (SELECT 1 FROM reservations r WHERE r.driver_id = d.id "
                + "AND r.status IN ('Pending', 'Ongoing'))";
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, id);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Driver not found or assigned to an active reservation.");
            }
        }
    }

    public static void remove(int id) throws SQLException {
        String sql = "UPDATE drivers d SET d.is_active = FALSE, d.status = 'Offline' "
                + "WHERE d.id = ? AND d.is_active = TRUE "
                + "AND NOT EXISTS (SELECT 1 FROM reservations r WHERE r.driver_id = d.id "
                + "AND r.status IN ('Pending', 'Ongoing'))";
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Cannot remove a driver assigned to an active reservation.");
            }
        }
    }
}
