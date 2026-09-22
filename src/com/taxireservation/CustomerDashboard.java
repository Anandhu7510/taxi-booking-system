package com.taxireservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Customer-facing shell. For now it exposes booking only; administrative driver
 * and global reservation controls remain behind the ADMIN role.
 */
public class CustomerDashboard extends JFrame {

    public CustomerDashboard() {
        if (!Session.hasRole("CUSTOMER")) {
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
            dispose();
            return;
        }

        UserAccount user = Session.getCurrentUser();
        setTitle("Taxi Reservation - Customer");
        setSize(980, 650);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(0x1F2937));
        top.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel welcome = new JLabel("Welcome, " + user.getDisplayName());
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> logout());

        top.add(welcome, BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(new NewReservationPanel(null), BorderLayout.CENTER);
    }

    private void logout() {
        Session.logout();
        dispose();
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
