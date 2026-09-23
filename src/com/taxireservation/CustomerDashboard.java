package com.taxireservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
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

    private final JLabel lblTotalTrips = new JLabel("0");
    private final JLabel lblActiveTrips = new JLabel("0");
    private final JLabel lblCompletedTrips = new JLabel("0");
    private final JLabel lblTotalSpent = new JLabel("\u20B9 0");
    private final JPanel currentRidePanel = new JPanel(new BorderLayout());
    private final DefaultTableModel recentModel = new DefaultTableModel(
            new String[]{"Route", "Date / Time", "Service", "Distance", "Fare", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public CustomerDashboard() {
        if (!Session.hasRole("CUSTOMER")) {
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
            dispose();
            return;
        }

        setTitle("Taxi Reservation - Customer");
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        buildHome();
        contentPanel.add(homePanel, "HOME");
        contentPanel.add(bookingPanel, "BOOKING");
        contentPanel.add(reservationsPanel, "RESERVATIONS");

        showHome();
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
        sidebar.add(navButton("\uD83C\uDFE0  Dashboard", this::showHome));
        sidebar.add(navButton("\u2795  New Booking", this::showBooking));
        sidebar.add(navButton("\uD83D\uDCCB  My Reservations", this::showReservations));

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(navButton("\u21A9  Logout", this::logout));

        return sidebar;
    }

    private void buildHome() {
        homePanel.setBackground(Color.WHITE);
        homePanel.setBorder(new EmptyBorder(28, 30, 28, 30));

        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setOpaque(false);

        JPanel headingText = new JPanel();
        headingText.setOpaque(false);
        headingText.setLayout(new BoxLayout(headingText, BoxLayout.Y_AXIS));

        UserAccount user = Session.getCurrentUser();

        JLabel title = new JLabel("Welcome, " + user.getDisplayName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Here is what is happening with your rides.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(0x6B7280));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        headingText.add(title);
        headingText.add(Box.createRigidArea(new Dimension(0, 5)));
        headingText.add(subtitle);

        JButton bookButton = actionButton("Book a Ride", new Color(0x2563EB));
        bookButton.addActionListener(e -> showBooking());

        header.add(headingText, BorderLayout.WEST);
        header.add(bookButton, BorderLayout.EAST);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));
        stats.setOpaque(false);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        stats.add(statCard("Total Trips", lblTotalTrips));
        stats.add(statCard("Active", lblActiveTrips));
        stats.add(statCard("Completed", lblCompletedTrips));
        stats.add(statCard("Total Spent", lblTotalSpent));

        JPanel currentSection = new JPanel(new BorderLayout());
        currentSection.setOpaque(false);

        JLabel currentTitle = new JLabel("Current / Upcoming Ride");
        currentTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        currentTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        currentRidePanel.setBackground(new Color(0xF9FAFB));
        currentRidePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                new EmptyBorder(16, 18, 16, 18)));
        currentRidePanel.setPreferredSize(new Dimension(0, 145));
        currentRidePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 145));

        currentSection.add(currentTitle, BorderLayout.NORTH);
        currentSection.add(currentRidePanel, BorderLayout.CENTER);

        JPanel recentSection = new JPanel(new BorderLayout(0, 10));
        recentSection.setOpaque(false);

        JPanel recentHeader = new JPanel(new BorderLayout());
        recentHeader.setOpaque(false);

        JLabel recentTitle = new JLabel("Recent Reservations");
        recentTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));

        JButton viewAll = smallActionButton("View All");
        viewAll.addActionListener(e -> showReservations());

        recentHeader.add(recentTitle, BorderLayout.WEST);
        recentHeader.add(viewAll, BorderLayout.EAST);

        JTable recentTable = new JTable(recentModel);
        recentTable.setRowHeight(27);
        recentTable.setFillsViewportHeight(true);
        recentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane recentScroll = new JScrollPane(recentTable);
        recentScroll.setPreferredSize(new Dimension(0, 135));

        recentSection.add(recentHeader, BorderLayout.NORTH);
        recentSection.add(recentScroll, BorderLayout.CENTER);

        body.add(stats);
        body.add(Box.createRigidArea(new Dimension(0, 20)));
        body.add(currentSection);
        body.add(Box.createRigidArea(new Dimension(0, 20)));
        body.add(recentSection);

        homePanel.add(header, BorderLayout.NORTH);
        homePanel.add(body, BorderLayout.CENTER);
    }

    private JPanel statCard(String caption, JLabel valueLabel) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(0xF9FAFB));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                new EmptyBorder(15, 16, 15, 16)));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 23));
        valueLabel.setForeground(new Color(0x111827));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        captionLabel.setForeground(new Color(0x6B7280));
        captionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(valueLabel);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(captionLabel);
        return card;
    }

    private void refreshHome() {
        UserAccount user = Session.getCurrentUser();
        if (user == null) return;

        String customerName = user.getDisplayName();
        int total = 0;
        int active = 0;
        int completed = 0;
        double spent = 0;
        Reservation currentRide = null;

        for (Reservation reservation : DataStore.get().getReservations()) {
            if (!reservation.getCustomerName().equalsIgnoreCase(customerName)) continue;

            total++;
            if (isActive(reservation)) {
                active++;
                currentRide = reservation;
            }
            if ("Completed".equalsIgnoreCase(reservation.getStatus())) {
                completed++;
                spent += reservation.getFare();
            }
        }

        lblTotalTrips.setText(String.valueOf(total));
        lblActiveTrips.setText(String.valueOf(active));
        lblCompletedTrips.setText(String.valueOf(completed));
        lblTotalSpent.setText(String.format("\u20B9 %.0f", spent));

        refreshCurrentRide(currentRide);
        refreshRecentReservations(customerName);
    }

    private boolean isActive(Reservation reservation) {
        String status = reservation.getStatus();
        return "Pending".equalsIgnoreCase(status) || "Ongoing".equalsIgnoreCase(status);
    }

    private void refreshCurrentRide(Reservation ride) {
        currentRidePanel.removeAll();

        if (ride == null) {
            JPanel empty = new JPanel(new BorderLayout());
            empty.setOpaque(false);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

            JLabel title = new JLabel("No active ride");
            title.setFont(new Font("Segoe UI", Font.BOLD, 16));

            JLabel subtitle = new JLabel("Your next booking will appear here.");
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            subtitle.setForeground(new Color(0x6B7280));

            text.add(title);
            text.add(Box.createRigidArea(new Dimension(0, 5)));
            text.add(subtitle);

            JButton book = smallActionButton("Book Now");
            book.addActionListener(e -> showBooking());

            empty.add(text, BorderLayout.CENTER);
            empty.add(book, BorderLayout.EAST);
            currentRidePanel.add(empty, BorderLayout.CENTER);
        } else {
            JPanel wrapper = new JPanel(new BorderLayout(16, 0));
            wrapper.setOpaque(false);

            JPanel details = new JPanel();
            details.setOpaque(false);
            details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

            JLabel route = new JLabel(ride.getPickupLocation() + "  \u2192  " + ride.getDropLocation());
            route.setFont(new Font("Segoe UI", Font.BOLD, 18));

            JLabel meta1 = new JLabel("Driver: " + ride.getDriverName()
                    + "    |    " + ride.getServiceType()
                    + "    |    " + ride.getVehicleDisplay());
            meta1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            meta1.setForeground(new Color(0x4B5563));

            JLabel meta2 = new JLabel(ride.getDateTime()
                    + "    |    Distance: " + ride.getDistanceDisplay()
                    + "    |    Fare: " + String.format("\u20B9 %.0f", ride.getFare()));
            meta2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            meta2.setForeground(new Color(0x4B5563));

            details.add(route);
            details.add(Box.createRigidArea(new Dimension(0, 8)));
            details.add(meta1);
            details.add(Box.createRigidArea(new Dimension(0, 5)));
            details.add(meta2);

            JLabel status = new JLabel(ride.getStatus());
            status.setOpaque(true);
            status.setForeground(Color.WHITE);
            status.setBackground(statusColor(ride.getStatus()));
            status.setFont(new Font("Segoe UI", Font.BOLD, 12));
            status.setBorder(new EmptyBorder(7, 12, 7, 12));

            wrapper.add(details, BorderLayout.CENTER);
            wrapper.add(status, BorderLayout.EAST);
            currentRidePanel.add(wrapper, BorderLayout.CENTER);
        }

        currentRidePanel.revalidate();
        currentRidePanel.repaint();
    }

    private void refreshRecentReservations(String customerName) {
        recentModel.setRowCount(0);
        int added = 0;

        for (int i = DataStore.get().getReservations().size() - 1; i >= 0 && added < 3; i--) {
            Reservation reservation = DataStore.get().getReservations().get(i);
            if (!reservation.getCustomerName().equalsIgnoreCase(customerName)) continue;

            recentModel.addRow(new Object[]{
                    reservation.getPickupLocation() + " \u2192 " + reservation.getDropLocation(),
                    reservation.getDateTime(),
                    reservation.getServiceType(),
                    reservation.getDistanceDisplay(),
                    String.format("\u20B9 %.0f", reservation.getFare()),
                    reservation.getStatus()
            });
            added++;
        }
    }

    private Color statusColor(String status) {
        if ("Completed".equalsIgnoreCase(status)) return new Color(0x10B981);
        if ("Cancelled".equalsIgnoreCase(status)) return new Color(0xEF4444);
        if ("Ongoing".equalsIgnoreCase(status)) return new Color(0x2563EB);
        return new Color(0xF59E0B);
    }

    private JButton actionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setPreferredSize(new Dimension(120, 38));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(9, 14, 9, 14));
        return button;
    }

    private JButton smallActionButton(String text) {
        JButton button = new JButton(text);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setBackground(new Color(0xE5E7EB));
        button.setForeground(new Color(0x111827));
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(7, 12, 7, 12));
        return button;
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

    private void showHome() {
        refreshHome();
        cardLayout.show(contentPanel, "HOME");
    }

    private void showBooking() {
        cardLayout.show(contentPanel, "BOOKING");
    }

    private void showReservations() {
        reservationsPanel.refresh();
        cardLayout.show(contentPanel, "RESERVATIONS");
    }

    private void logout() {
        Session.logout();
        dispose();
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
