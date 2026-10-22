package com.taxireservation;

import java.sql.*;

/** MySQL authentication and registration queries. */
public final class UserDAO {
    private UserDAO() {}

    public static UserAccount authenticate(String username, String password, String role)
            throws SQLException {
        String sql = "SELECT id, username, password_hash, role, display_name "
                + "FROM users WHERE username = ? AND role = ?";
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());
            statement.setString(2, role);
            try (ResultSet rows = statement.executeQuery()) {
                if (rows.next() && PasswordUtil.verify(password, rows.getString("password_hash"))) {
                    return new UserAccount(rows.getInt("id"), rows.getString("username"),
                            rows.getString("role"), rows.getString("display_name"));
                }
            }
        }
        return null;
    }

    public static boolean usernameExists(String username) throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM users WHERE username = ?")) {
            statement.setString(1, username.trim());
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        }
    }

    public static UserAccount register(String displayName, String username, String password)
            throws SQLException {
        String sql = "INSERT INTO users(username, password_hash, role, display_name) "
                + "VALUES (?, ?, 'CUSTOMER', ?)";
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, username.trim());
            statement.setString(2, PasswordUtil.hash(password));
            statement.setString(3, displayName.trim());
            try {
                statement.executeUpdate();
            } catch (SQLIntegrityConstraintViolationException duplicate) {
                return null;
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new UserAccount(keys.getInt(1), username.trim(), "CUSTOMER",
                            displayName.trim());
                }
            }
            throw new SQLException("Database did not return a new account ID.");
        }
    }

    public static boolean hasAdministrator() throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM users WHERE role = 'ADMIN' LIMIT 1");
             ResultSet rows = statement.executeQuery()) {
            return rows.next();
        }
    }

    public static void createAdministrator(String username, String password) throws SQLException {
        if (username == null || username.isBlank() || password == null || password.length() < 8) {
            throw new SQLException("Administrator username and password (minimum 8 characters) are required.");
        }
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO users(username, password_hash, role, display_name) "
                     + "VALUES (?, ?, 'ADMIN', 'System Administrator')")) {
            statement.setString(1, username.trim());
            statement.setString(2, PasswordUtil.hash(password));
            statement.executeUpdate();
        }
    }

    /** Provision the first administrator using local environment variables only. */
    public static void ensureAdministrator() throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM users WHERE role = 'ADMIN' LIMIT 1");
             ResultSet rows = statement.executeQuery()) {
            if (rows.next()) return;
            String username = System.getenv("TAXI_ADMIN_USERNAME");
            String password = System.getenv("TAXI_ADMIN_PASSWORD");
            if (username == null || username.isBlank() || password == null || password.length() < 8) {
                throw new SQLException("No administrator account exists. Set TAXI_ADMIN_USERNAME "
                        + "and TAXI_ADMIN_PASSWORD (at least 8 characters) for first launch.");
            }
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO users(username, password_hash, role, display_name) "
                    + "VALUES (?, ?, 'ADMIN', ?)")) {
                insert.setString(1, username.trim());
                insert.setString(2, PasswordUtil.hash(password));
                insert.setString(3, "System Administrator");
                insert.executeUpdate();
            }
        }
    }
}
