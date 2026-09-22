package com.taxireservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Main application shell: left sidebar navigation + CardLayout content area.
 *
 * NetBeans GUI Builder equivalent:
 *  - JFrame Form, set root layout to BorderLayout
 *  - Add a JPanel "sidebar" to the WEST, layout BoxLayout (Y_AXIS), dark background
 *  - Add JButtons for each menu item (Dashboard, New Booking, Drivers, History, Logout)
 *  - Add a JPanel "contentPanel" to the CENTER with CardLayout
 *  - Drop each screen's panel (NewReservationPanel, DriverManagementPanel,
 *    ReservationHistoryPanel) into contentPanel as separate "cards"
 *  - Wire each sidebar button's actionPerformed to call cardLayout.show(...)
 */
public class MainDashboard extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private DashboardHomePanel homePanel;

    public MainDashboard() {
        setTitle("Taxi & Driver Reservation System");
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        homePanel = new DashboardHomePanel();
        contentPanel.add(homePanel, "HOME");
        contentPanel.add(new NewReservationPanel(homePanel), "BOOKING");
        contentPanel.add(new DriverManagementPanel(), "DRIVERS");
        contentPanel.add(new ReservationHistoryPanel(homePanel), "HISTORY");

        cardLayout.show(contentPanel, "HOME");
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(0x1F2937));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(20, 0, 20, 0));

        JLabel logo = new JLabel("  \uD83D\uDE95 TaxiRes");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.setBorder(new EmptyBorder(0, 10, 30, 0));
        sidebar.add(logo);

        sidebar.add(navButton("\uD83C\uDFE0  Dashboard", () -> {
            homePanel.refresh();
            cardLayout.show(contentPanel, "HOME");
        }));
        sidebar.add(navButton("\u2795  New Booking", () -> cardLayout.show(contentPanel, "BOOKING")));
        sidebar.add(navButton("\uD83D\uDE97  Drivers", () -> cardLayout.show(contentPanel, "DRIVERS")));
        sidebar.add(navButton("\uD83D\uDCCB  Reservations", () -> {
            cardLayout.show(contentPanel, "HISTORY");
        }));

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(navButton("\u21A9  Logout", () -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
        }));

        return sidebar;
    }

    private JButton navButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(220, 44));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0x1F2937));
        btn.setBorder(new EmptyBorder(10, 20, 10, 10));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.addActionListener(e -> action.run());
        btn.addChangeListener(e -> {
            if (btn.getModel().isRollover()) btn.setBackground(new Color(0x374151));
            else btn.setBackground(new Color(0x1F2937));
        });
        return btn;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new MainDashboard().setVisible(true));
    }
}
