package com.taxireservation;

import javax.swing.*;
import java.awt.GridLayout;
import java.sql.SQLException;

/** Start the existing Swing application with automatic MySQL initialization. */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseBootstrap.initialize();
                if (!UserDAO.hasAdministrator() && !createFirstAdministrator()) return;
                new LoginForm().setVisible(true);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null,
                        "Unable to start Taxi Reservation.\n" + ex.getMessage()
                        + "\n\nInstall/start MySQL Server, then launch using run.bat "
                        + "(see README).",
                        "Database Setup Required", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static boolean createFirstAdministrator() throws SQLException {
        String username = System.getenv("TAXI_ADMIN_USERNAME");
        String password = System.getenv("TAXI_ADMIN_PASSWORD");
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            JTextField usernameInput = new JTextField();
            JPasswordField passwordInput = new JPasswordField();
            JPanel fields = new JPanel(new GridLayout(2, 2, 8, 8));
            fields.add(new JLabel("Admin username / email:"));
            fields.add(usernameInput);
            fields.add(new JLabel("Admin password:"));
            fields.add(passwordInput);
            int selected = JOptionPane.showConfirmDialog(null, fields,
                    "First Run: Create Administrator", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (selected != JOptionPane.OK_OPTION) return false;
            username = usernameInput.getText().trim();
            password = new String(passwordInput.getPassword());
            passwordInput.setText("");
        }
        if (username.isBlank() || password.length() < 8) {
            JOptionPane.showMessageDialog(null,
                    "Enter an administrator username and password of at least 8 characters.",
                    "Invalid Administrator Credentials", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        UserDAO.createAdministrator(username, password);
        return true;
    }
}
