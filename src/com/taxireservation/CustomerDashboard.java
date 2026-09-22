package com.taxireservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Customer-facing dashboard with role-appropriate sidebar navigation.
 */
public class CustomerDashboard extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final JPanel homePanel = new JPanel(new BorderLayout());
    private final NewReservationPanel bookingPanel = new NewReservationPanel(null);
    private final CustomerReservationsPanel reservationsPanel = new CustomerReservationsPanel();

    public CustomerDashboard() {
        if (!Session.hasRole("CUSTOMER")) {
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
            dispose();
            return;
        }

        setTitle("Taxi Reservation - Customer");
        setSize(1050, 660);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        buildHome();
        contentPanel.add(homePanel, "HOME");
        contentPanel.add(bookingPanel, "BOOKING");
        contentPanel.add(reservationsPanel, "RESERVATIONS");

        cardLayout.show(contentPanel, "HOME");
    }

    private JPanel buildSidebar() {
        UserAccount user = Session.getCurrentUser();

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(0x1F2937));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(20, 0, 20, 0));

        JLabel logo = new JLabel("  \uD83D\uDE95 TaxiRes");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.setBorder(new EmptyBorder(0, 10, 8, 0));

        JLabel userLabel = new JLabel("  " + user.getDisplayName());
        userLabel.setForeground(new Color(0xD1D5DB));
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        userLabel.setBorder(new EmptyBorder(0, 10, 24, 0));

        sidebar.add(logo);
        sidebar.add(userLabel);
        sidebar.add(navButton("\uD83C\uDFE0  Dashboard", () -> cardLayout.show(contentPanel, "HOME")));
        sidebar.add(navButton("\u2795  New Booking", () ->
                cardLayout.show(contentPanel, "BOOKING")));
        sidebar.add(navButton("\uD83D\uDCCB  My Reservations", () -> {
            reservationsPanel.refresh();
            cardLayout.show(contentPanel, "RESERVATIONS");
        }));

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(navButton("\u21A9  Logout", this::logout));

        return sidebar;
    }

    private void buildHome() {
        homePanel.setBackground(Color.WHITE);
        homePanel.setBorder(new EmptyBorder(32, 32, 32, 32));

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        UserAccount user = Session.getCurrentUser();

        JLabel title = new JLabel("Welcome, " + user.getDisplayName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Book a taxi or check your reservations from the menu.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel cards = new JPanel(new GridLayout(1, 2, 18, 18));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(680, 150));
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.add(infoCard("New Booking", "Create a new taxi reservation."));
        cards.add(infoCard("My Reservations", "Review your current and previous bookings."));

        wrapper.add(title);
        wrapper.add(Box.createRigidArea(new Dimension(0, 8)));
        wrapper.add(subtitle);
        wrapper.add(Box.createRigidArea(new Dimension(0, 30)));
        wrapper.add(cards);

        homePanel.add(wrapper, BorderLayout.NORTH);
    }

    private JPanel infoCard(String title, String text) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(0xF9FAFB));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 17));

        JLabel body = new JLabel("<html>" + text + "</html>");
        body.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        body.setForeground(new Color(0x4B5563));

        card.add(heading);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(body);
        return card;
    }

    private JButton navButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(220, 44));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0x1F2937));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 10));
        button.addActionListener(e -> action.run());
        button.addChangeListener(e -> {
            if (button.getModel().isRollover()) button.setBackground(new Color(0x374151));
            else button.setBackground(new Color(0x1F2937));
        });
        return button;
    }

    private void logout() {
        Session.logout();
        dispose();
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
