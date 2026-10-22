package com.taxireservation;

import java.sql.SQLException;

/** Used by run.ps1 to test MySQL credentials before remembering them locally. */
public final class DatabaseCheck {
    private DatabaseCheck() { }

    public static void main(String[] args) {
        try {
            DatabaseBootstrap.initialize();
            System.out.println("MySQL connection and database schema verified.");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            System.exit(2);
        }
    }
}
