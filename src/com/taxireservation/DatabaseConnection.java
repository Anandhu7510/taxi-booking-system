package com.taxireservation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Shared JDBC connection configuration; never stores credentials in source control. */
public final class DatabaseConnection {
    private DatabaseConnection() {}

    public static Connection open() throws SQLException {
        String url = System.getenv().getOrDefault(
                "TAXI_DB_URL", "jdbc:mysql://localhost:3306/taxi_booking");
        String username = System.getenv("TAXI_DB_USER");
        String password = System.getenv("TAXI_DB_PASSWORD");
        if (username == null || username.isBlank() || password == null) {
            throw new SQLException("Set TAXI_DB_USER and TAXI_DB_PASSWORD environment variables.");
        }
        return DriverManager.getConnection(url, username, password);
    }
}
