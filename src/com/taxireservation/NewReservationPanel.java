package com.taxireservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * New booking form supporting both Taxi + Driver and Driver Only (Own Vehicle).
 */
public class NewReservationPanel extends JPanel {

    private final JTextField txtName = new JTextField();
    private final JTextField txtPhone = new JTextField();
    private final JComboBox<String> cboPickup = new JComboBox<>(RouteCatalog.cityOptions());
    private final JComboBox<String> cboDrop = new JComboBox<>(RouteCatalog.cityOptions());
    private final JTextField txtDateTime = new JTextField(currentDateTime());
    private final JTextField txtEstimatedKm = new JTextField("-");

    private final JComboBox<String> cboServiceType = new JComboBox<>(new String[]{
            "Taxi + Driver", "Driver Only (Own Vehicle)"
    });
    private final JComboBox<String> cboCabType = new JComboBox<>(new String[]{
            DriverAvailability.ALL_TYPES, "Mini", "Sedan", "SUV", "Luxury"
    });
    private final JTextField txtOwnVehicleType = new JTextField();
    private final JTextField txtOwnVehicleNumber = new JTextField();
    private final JComboBox<Driver> cboDriver = new JComboBox<>();
    private final JLabel lblDriverHint = new JLabel();
    private final JPanel driverPicker = new JPanel(new BorderLayout(0, 4));
    private boolean loadingDrivers = false;
    private final JLabel lblFareValue = new JLabel("\u20B9 0");
    private final JLabel lblFareHint = new JLabel();

    private final CardLayout vehicleCardLayout = new CardLayout();
    private final JPanel vehicleDetailsPanel = new JPanel(vehicleCardLayout);

    private final DashboardHomePanel homePanel;

    public NewReservationPanel(DashboardHomePanel homePanel) {
        this.homePanel = homePanel;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel header = new JLabel("New Reservation");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(25, 0, 0, 0));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(7, 8, 7, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        buildVehicleDetailsPanel();
        driverPicker.setOpaque(false);
        JPanel driverControls = new JPanel(new BorderLayout(8, 0));
        driverControls.setOpaque(false);
        driverControls.add(cboDriver, BorderLayout.CENTER);
        cboDriver.setMaximumRowCount(12);
        JButton btnRefreshDrivers = new JButton("Refresh Drivers");
        btnRefreshDrivers.setToolTipText("Reload available drivers from MySQL.");
        btnRefreshDrivers.addActionListener(e -> refreshDriverList());
        driverControls.add(btnRefreshDrivers, BorderLayout.EAST);
        driverPicker.add(driverControls, BorderLayout.NORTH);
        lblDriverHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDriverHint.setForeground(new Color(0x374151));
        driverPicker.add(lblDriverHint, BorderLayout.SOUTH);
        cboDriver.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof Driver) {
                    Driver driver = (Driver) value;
                    label.setText(driver.getName() + " (" + driver.getVehicleNumber()
                            + ") - " + driver.getVehicleType());
                }
                return label;
            }
        });
        // The distance is derived from the two cities; customers cannot edit it.
        txtEstimatedKm.setEditable(false);
        txtEstimatedKm.setFocusable(false);
        txtEstimatedKm.setBackground(new Color(0xF3F4F6));

        int row = 0;
        addField(form, c, row++, "Customer Name", txtName);
        addField(form, c, row++, "Phone Number", txtPhone);
        addField(form, c, row++, "Starting City", cboPickup);
        addField(form, c, row++, "Destination City", cboDrop);
        addField(form, c, row++, "Date / Time", txtDateTime);
        addField(form, c, row++, "Estimated Distance (km)", txtEstimatedKm);
        addField(form, c, row++, "Service Type", cboServiceType);
        addField(form, c, row++, "Vehicle Details", vehicleDetailsPanel);
        addField(form, c, row++, "Assign Driver", driverPicker);

        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 1;
        c.weightx = 0;

        JLabel fareCaption = new JLabel("Estimated Fare:");
        fareCaption.setFont(new Font("Segoe UI", Font.BOLD, 13));
        form.add(fareCaption, c);

        JPanel farePanel = new JPanel();
        farePanel.setLayout(new BoxLayout(farePanel, BoxLayout.Y_AXIS));
        farePanel.setOpaque(false);

        lblFareValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblFareValue.setForeground(new Color(0x10B981));
        lblFareValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblFareHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFareHint.setForeground(new Color(0x6B7280));
        lblFareHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        farePanel.add(lblFareValue);
        farePanel.add(lblFareHint);

        c.gridx = 1;
        c.weightx = 1;
        form.add(farePanel, c);
        row++;

        JButton btnConfirm = new JButton("Confirm Booking");
        btnConfirm.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnConfirm.setBackground(new Color(0xF59E0B));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnConfirm.setOpaque(true);
        btnConfirm.setContentAreaFilled(true);
        btnConfirm.setBorderPainted(false);
        btnConfirm.setFocusPainted(false);
        btnConfirm.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        btnConfirm.addActionListener(e -> confirmBooking());

        c.gridx = 1;
        c.gridy = row;
        c.gridwidth = 1;
        form.add(btnConfirm, c);

        cboServiceType.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                updateServiceMode();
            }
        });
        cboCabType.addActionListener(e -> refreshDriverList());
        cboDriver.addActionListener(e -> {
            if (!loadingDrivers) updateFareEstimate();
        });

        cboPickup.addActionListener(e -> updateFareEstimate());
        cboDrop.addActionListener(e -> updateFareEstimate());

        applyCustomerIdentity();
        updateServiceMode();
        add(form, BorderLayout.CENTER);
    }

    private void buildVehicleDetailsPanel() {
        vehicleDetailsPanel.setOpaque(false);
        vehicleDetailsPanel.setPreferredSize(new Dimension(360, 70));

        JPanel taxiPanel = new JPanel(new BorderLayout());
        taxiPanel.setOpaque(false);
        cboCabType.setPreferredSize(new Dimension(280, 30));
        taxiPanel.add(cboCabType, BorderLayout.NORTH);

        JPanel ownVehiclePanel = new JPanel(new GridBagLayout());
        ownVehiclePanel.setOpaque(false);

        GridBagConstraints v = new GridBagConstraints();
        v.insets = new Insets(2, 0, 4, 8);
        v.fill = GridBagConstraints.HORIZONTAL;

        JLabel typeLabel = new JLabel("Vehicle Type");
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel numberLabel = new JLabel("Registration Number");
        numberLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        txtOwnVehicleType.setPreferredSize(new Dimension(210, 28));
        txtOwnVehicleNumber.setPreferredSize(new Dimension(210, 28));

        txtOwnVehicleType.setEnabled(true);
        txtOwnVehicleNumber.setEnabled(true);
        txtOwnVehicleType.setEditable(true);
        txtOwnVehicleNumber.setEditable(true);
        txtOwnVehicleType.setFocusable(true);
        txtOwnVehicleNumber.setFocusable(true);

        v.gridx = 0;
        v.gridy = 0;
        v.weightx = 0;
        ownVehiclePanel.add(typeLabel, v);

        v.gridx = 1;
        v.weightx = 1;
        ownVehiclePanel.add(txtOwnVehicleType, v);

        v.gridx = 0;
        v.gridy = 1;
        v.weightx = 0;
        ownVehiclePanel.add(numberLabel, v);

        v.gridx = 1;
        v.weightx = 1;
        ownVehiclePanel.add(txtOwnVehicleNumber, v);

        vehicleDetailsPanel.add(taxiPanel, "TAXI");
        vehicleDetailsPanel.add(ownVehiclePanel, "OWN");
    }

    private void applyCustomerIdentity() {
        if (Session.hasRole("CUSTOMER") && Session.getCurrentUser() != null) {
            txtName.setText(Session.getCurrentUser().getDisplayName());
            txtName.setEditable(false);
            txtName.setBackground(new Color(0xF3F4F6));
        }
    }

    private void addField(JPanel form, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 1;
        c.weightx = 0;

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(l, c);

        c.gridx = 1;
        c.weightx = 1;
        field.setPreferredSize(field == vehicleDetailsPanel ? new Dimension(360, 70)
                : field == driverPicker ? new Dimension(360, 55)
                : new Dimension(280, 30));
        form.add(field, c);
    }

    private boolean isDriverOnlyMode() {
        return cboServiceType.getSelectedIndex() == 1;
    }

    private void updateServiceMode() {
        boolean driverOnly = isDriverOnlyMode();

        vehicleCardLayout.show(vehicleDetailsPanel, driverOnly ? "OWN" : "TAXI");

        if (!driverOnly) {
            txtOwnVehicleType.setText("");
            txtOwnVehicleNumber.setText("");
        }

        refreshDriverList();

        vehicleDetailsPanel.revalidate();
        vehicleDetailsPanel.repaint();

        if (driverOnly) {
            SwingUtilities.invokeLater(() -> txtOwnVehicleType.requestFocusInWindow());
        }
    }

    private double readEstimatedKm() {
        return RouteCatalog.distanceKm((String) cboPickup.getSelectedItem(),
                (String) cboDrop.getSelectedItem());
    }

    private void updateFareEstimate() {
        int km = RouteCatalog.distanceKm((String) cboPickup.getSelectedItem(),
                (String) cboDrop.getSelectedItem());
        txtEstimatedKm.setText(km > 0 ? String.format("%d km", km) : "-");
        if (km <= 0) {
            lblFareValue.setText("\u20B9 0");
            lblFareHint.setText("Choose two different cities to see the estimated fare.");
            return;
        }

        boolean driverOnly = isDriverOnlyMode();
        Driver driver = (Driver) cboDriver.getSelectedItem();
        if (!driverOnly && driver == null) {
            lblFareValue.setText("\u20B9 0");
            lblFareHint.setText("Choose a driver and vehicle to calculate your taxi fare.");
            return;
        }

        String type = driverOnly ? "Own Vehicle"
                : DriverAvailability.canonicalCabType(driver.getVehicleType());
        if (type == null) {
            lblFareValue.setText("\u20B9 0");
            lblFareHint.setText("The selected driver's vehicle type is unsupported.");
            return;
        }

        double fare = RouteCatalog.fare(
                (String) cboServiceType.getSelectedItem(), type, km);
        lblFareValue.setText(String.format("\u20B9 %.0f", fare));
        lblFareHint.setText(String.format("%s - %d km (fixed route estimate)",
                driverOnly ? "Driver only" : type, km));
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            applyCustomerIdentity();
            updateServiceMode();
        }
        super.setVisible(visible);
    }

    private void refreshDriverList() {
        Driver previouslySelected = (Driver) cboDriver.getSelectedItem();
        Integer previousId = previouslySelected == null ? null : previouslySelected.getId();
        try {
            // Fetch once from MySQL. Filtering stays deterministic and never changes
            // the cab-filter dropdown from inside its own event listener.
            java.util.List<Driver> available = DataStore.get().getAvailableDrivers(null);
            boolean driverOnly = isDriverOnlyMode();
            String filter = (String) cboCabType.getSelectedItem();
            java.util.List<Driver> matches = driverOnly
                    ? available : DriverAvailability.forCabType(available, filter);

            loadingDrivers = true;
            try {
                cboDriver.removeAllItems();
                int restoreIndex = -1;
                for (int i = 0; i < matches.size(); i++) {
                    Driver driver = matches.get(i);
                    cboDriver.addItem(driver);
                    if (previousId != null && driver.getId() == previousId) restoreIndex = i;
                }
                // Do not silently pick the first driver after a refresh/filter change.
                cboDriver.setSelectedIndex(restoreIndex);
                cboDriver.setEnabled(!matches.isEmpty());
            } finally {
                loadingDrivers = false;
            }

            if (matches.isEmpty()) {
                lblDriverHint.setText(available.isEmpty()
                        ? "No available drivers. Ask admin to add or enable one, then refresh."
                        : driverOnly
                          ? "No drivers available for your own vehicle."
                          : "No drivers for this cab filter. Available types: "
                            + DriverAvailability.availableTypes(available) + ".");
            } else if (driverOnly) {
                lblDriverHint.setText(matches.size()
                        + " available driver(s). Select one or press Refresh Drivers.");
            } else {
                lblDriverHint.setText(matches.size()
                        + " driver + vehicle option(s). Select one; fare uses its actual cab type.");
            }
            lblDriverHint.setForeground(matches.isEmpty()
                    ? new Color(0xB45309) : new Color(0x374151));
            updateFareEstimate();
        } catch (IllegalStateException ex) {
            loadingDrivers = true;
            try {
                cboDriver.removeAllItems();
                cboDriver.setEnabled(false);
            } finally {
                loadingDrivers = false;
            }
            lblDriverHint.setText("Driver data could not be loaded; try Refresh Drivers.");
            lblDriverHint.setForeground(new Color(0xB91C1C));
            updateFareEstimate();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void confirmBooking() {
        if (txtName.getText().trim().length() == 0
                || txtPhone.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double estimatedKm = readEstimatedKm();
        if (estimatedKm <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Select two different cities for the starting point and destination.",
                    "Invalid Route", JOptionPane.WARNING_MESSAGE);
            cboPickup.requestFocusInWindow();
            return;
        }

        boolean driverOnly = isDriverOnlyMode();

        if (driverOnly && (txtOwnVehicleType.getText().trim().length() == 0
                || txtOwnVehicleNumber.getText().trim().length() == 0)) {
            JOptionPane.showMessageDialog(this,
                    "Enter your vehicle type and registration number for Driver Only service.",
                    "Vehicle Information Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Driver driver = (Driver) cboDriver.getSelectedItem();
        if (driver == null) {
            JOptionPane.showMessageDialog(this, "No compatible driver can be selected.\n"
                    + lblDriverHint.getText(),
                    "No Driver Available", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String serviceType = (String) cboServiceType.getSelectedItem();
        String cabType = driverOnly ? "Own Vehicle"
                : DriverAvailability.canonicalCabType(driver.getVehicleType());
        if (cabType == null) {
            JOptionPane.showMessageDialog(this,
                    "Selected driver has an unsupported cab type. Ask admin to correct it.",
                    "Invalid Driver Vehicle", JOptionPane.WARNING_MESSAGE);
            refreshDriverList();
            return;
        }
        String customerVehicle = driverOnly
                ? txtOwnVehicleType.getText().trim() + " - " + txtOwnVehicleNumber.getText().trim()
                : "";

        double fare = RouteCatalog.fare(serviceType, cabType, estimatedKm);

        Integer customerId = Session.hasRole("CUSTOMER")
                ? Session.getCurrentUser().getId() : null;
        try {
            DataStore.get().bookReservation(
                    customerId, txtName.getText(), txtPhone.getText(),
                    (String) cboPickup.getSelectedItem(), (String) cboDrop.getSelectedItem(),
                    txtDateTime.getText(),
                    serviceType, cabType, customerVehicle, estimatedKm,
                    driver.getId(), fare);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Unable to Confirm Booking",
                    JOptionPane.ERROR_MESSAGE);
            refreshDriverList();
            return;
        }

        JOptionPane.showMessageDialog(this,
                String.format("Booking confirmed for %.1f km. Estimated fare: \u20B9%.0f",
                        estimatedKm, fare),
                "Success", JOptionPane.INFORMATION_MESSAGE);

        if (!Session.hasRole("CUSTOMER")) txtName.setText("");
        txtPhone.setText("");
        cboPickup.setSelectedIndex(0);
        cboDrop.setSelectedIndex(0);
        txtDateTime.setText(currentDateTime());
        txtOwnVehicleType.setText("");
        txtOwnVehicleNumber.setText("");
        cboServiceType.setSelectedIndex(0);

        refreshDriverList();
        updateServiceMode();

        if (homePanel != null) homePanel.refresh();
    }

    private static String currentDateTime() {
        return java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
