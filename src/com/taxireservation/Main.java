package com.taxireservation;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

/** Start Swing only after verifying that MySQL and the administrator are ready. */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        SwingUtilities.invokeLater(() -> {
            try (Connection ignored = DatabaseConnection.open()) {
                AuthStore.ensureAdministrator();
                new LoginForm().setVisible(true);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null,
                        "Unable to start Taxi Reservation.\n" + ex.getMessage()
                        + "\n\nInitialize db/schema.sql, configure the JDBC driver and "
                        + "environment variables (see README).",
                        "Database Setup Required", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
