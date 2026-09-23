package com.taxireservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * New booking form supporting both Taxi + Driver and Driver Only (Own Vehicle).
 */
public class NewReservationPanel extends JPanel {

    private final JTextField txtName = new JTextField();
    private final JTextField txtPhone = new JTextField();
    private final JTextField txtPickup = new JTextField();
    private final JTextField txtDrop = new JTextField();
    private final JTextField txtDateTime = new JTextField(currentDateTime());
    private final JTextField txtEstimatedKm = new JTextField("10");

    private final JComboBox<String> cboServiceType = new JComboBox<>(new String[]{
            "Taxi + Driver", "Driver Only (Own Vehicle)"
    });
    private final JComboBox<String> cboCabType = new JComboBox<>(new String[]{
            "Mini", "Sedan", "SUV", "Luxury"
    });
    private final JTextField txtOwnVehicleType = new JTextField();
    private final JTextField txtOwnVehicleNumber = new JTextField();
    private final JComboBox<Driver> cboDriver = new JComboBox<>();
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

        int row = 0;
        addField(form, c, row++, "Customer Name", txtName);
        addField(form, c, row++, "Phone Number", txtPhone);
        addField(form, c, row++, "Pickup Location", txtPickup);
        addField(form, c, row++, "Drop Location", txtDrop);
        addField(form, c, row++, "Date / Time", txtDateTime);
        addField(form, c, row++, "Estimated Distance (km)", txtEstimatedKm);
        addField(form, c, row++, "Service Type", cboServiceType);
        addField(form, c, row++, "Vehicle Details", vehicleDetailsPanel);
        addField(form, c, row++, "Assign Driver", cboDriver);

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
        cboCabType.addActionListener(e -> updateFareEstimate());

        txtEstimatedKm.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateFareEstimate(); }

            @Override
            public void removeUpdate(DocumentEvent e) { updateFareEstimate(); }

            @Override
            public void changedUpdate(DocumentEvent e) { updateFareEstimate(); }
        });

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
        field.setPreferredSize(field == vehicleDetailsPanel
                ? new Dimension(360, 70)
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

        updateFareEstimate();

        vehicleDetailsPanel.revalidate();
        vehicleDetailsPanel.repaint();

        if (driverOnly) {
            SwingUtilities.invokeLater(() -> txtOwnVehicleType.requestFocusInWindow());
        }
    }

    private double readEstimatedKm() {
        try {
            double km = Double.parseDouble(txtEstimatedKm.getText().trim());
            return km > 0 ? km : -1;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private void updateFareEstimate() {
        double km = readEstimatedKm();

        if (km <= 0) {
            lblFareValue.setText("\u20B9 0");
            lblFareHint.setText("Enter a valid distance greater than 0 km.");
            return;
        }

        double baseFare;
        double perKm;
        String pricingLabel;

        if (isDriverOnlyMode()) {
            baseFare = 50;
            perKm = 10;
            pricingLabel = "Driver only";
        } else {
            String type = (String) cboCabType.getSelectedItem();

            if ("Mini".equals(type)) {
                baseFare = 80;
                perKm = 14;
            } else if ("Sedan".equals(type)) {
                baseFare = 120;
                perKm = 20;
            } else if ("SUV".equals(type)) {
                baseFare = 180;
                perKm = 30;
            } else if ("Luxury".equals(type)) {
                baseFare = 250;
                perKm = 50;
            } else {
                baseFare = 80;
                perKm = 14;
            }
            pricingLabel = type == null ? "Taxi + Driver" : type;
        }

        double fare = baseFare + (perKm * km);
        lblFareValue.setText(String.format("\u20B9 %.0f", fare));
        lblFareHint.setText(String.format(
                "%s: \u20B9%.0f base + \u20B9%.0f/km x %.1f km",
                pricingLabel, baseFare, perKm, km));
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            applyCustomerIdentity();
            refreshDriverList();
            updateServiceMode();
        }
        super.setVisible(visible);
    }

    private void refreshDriverList() {
        Driver selected = (Driver) cboDriver.getSelectedItem();
        cboDriver.removeAllItems();

        for (Driver driver : DataStore.get().getAvailableDrivers()) {
            cboDriver.addItem(driver);
        }

        if (selected != null) cboDriver.setSelectedItem(selected);
    }

    private void confirmBooking() {
        if (txtName.getText().trim().length() == 0
                || txtPhone.getText().trim().length() == 0
                || txtPickup.getText().trim().length() == 0
                || txtDrop.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double estimatedKm = readEstimatedKm();
        if (estimatedKm <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Enter a valid estimated distance greater than 0 km.",
                    "Invalid Distance", JOptionPane.WARNING_MESSAGE);
            txtEstimatedKm.requestFocusInWindow();
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
            JOptionPane.showMessageDialog(this, "No available driver to assign. Try again shortly.",
                    "No Driver Available", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String serviceType = (String) cboServiceType.getSelectedItem();
        String cabType = driverOnly ? "Own Vehicle" : (String) cboCabType.getSelectedItem();
        String customerVehicle = driverOnly
                ? txtOwnVehicleType.getText().trim() + " - " + txtOwnVehicleNumber.getText().trim()
                : "";

        double fare = Double.parseDouble(lblFareValue.getText().replace("\u20B9", "").trim());

        DataStore.get().addReservation(
                txtName.getText(),
                txtPhone.getText(),
                txtPickup.getText(),
                txtDrop.getText(),
                txtDateTime.getText(),
                serviceType,
                cabType,
                customerVehicle,
                estimatedKm,
                driver.getName(),
                fare,
                "Pending"
        );

        DataStore.get().setDriverStatus(driver.getName(), "On Trip");

        JOptionPane.showMessageDialog(this,
                String.format("Booking confirmed for %.1f km. Estimated fare: \u20B9%.0f",
                        estimatedKm, fare),
                "Success", JOptionPane.INFORMATION_MESSAGE);

        if (!Session.hasRole("CUSTOMER")) txtName.setText("");
        txtPhone.setText("");
        txtPickup.setText("");
        txtDrop.setText("");
        txtDateTime.setText(currentDateTime());
        txtEstimatedKm.setText("10");
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
