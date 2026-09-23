package com.taxireservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * New booking form supporting both Taxi + Driver and Driver Only (Own Vehicle).
 */
public class NewReservationPanel extends JPanel {

    private static final double DRIVER_ONLY_FARE = 150.0;

    private final JTextField txtName = new JTextField();
    private final JTextField txtPhone = new JTextField();
    private final JTextField txtPickup = new JTextField();
    private final JTextField txtDrop = new JTextField();
    private final JTextField txtDateTime = new JTextField(currentDateTime());

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

        int row = 0;
        addField(form, c, row++, "Customer Name", txtName);
        addField(form, c, row++, "Phone Number", txtPhone);
        addField(form, c, row++, "Pickup Location", txtPickup);
        addField(form, c, row++, "Drop Location", txtDrop);
        addField(form, c, row++, "Date / Time", txtDateTime);
        addField(form, c, row++, "Service Type", cboServiceType);
        addField(form, c, row++, "Cab Type", cboCabType);
        addField(form, c, row++, "Own Vehicle Type", txtOwnVehicleType);
        addField(form, c, row++, "Own Vehicle Number", txtOwnVehicleNumber);
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

        cboServiceType.addActionListener(e -> updateServiceMode());
        cboCabType.addActionListener(e -> updateFareEstimate());

        applyCustomerIdentity();
        updateServiceMode();
        add(form, BorderLayout.CENTER);
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
        field.setPreferredSize(new Dimension(280, 30));
        form.add(field, c);
    }

    private boolean isDriverOnlyMode() {
        return "Driver Only (Own Vehicle)".equals(cboServiceType.getSelectedItem());
    }

    private void updateServiceMode() {
        boolean driverOnly = isDriverOnlyMode();

        cboCabType.setEnabled(!driverOnly);
        txtOwnVehicleType.setEnabled(driverOnly);
        txtOwnVehicleNumber.setEnabled(driverOnly);

        cboCabType.setToolTipText(driverOnly
                ? "Not required when the customer provides the vehicle."
                : "Choose the taxi category.");

        txtOwnVehicleType.setBackground(driverOnly ? Color.WHITE : new Color(0xF3F4F6));
        txtOwnVehicleNumber.setBackground(driverOnly ? Color.WHITE : new Color(0xF3F4F6));

        updateFareEstimate();
    }

    private void updateFareEstimate() {
        double fare;

        if (isDriverOnlyMode()) {
            fare = DRIVER_ONLY_FARE;
            lblFareHint.setText("Driver service only - customer provides the vehicle.");
        } else {
            String type = (String) cboCabType.getSelectedItem();
            fare = switch (type == null ? "" : type) {
                case "Mini" -> 220;
                case "Sedan" -> 320;
                case "SUV" -> 480;
                case "Luxury" -> 750;
                default -> 250;
            };
            lblFareHint.setText("Includes taxi and driver.");
        }

        lblFareValue.setText(String.format("\u20B9 %.0f", fare));
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
                driver.getName(),
                fare,
                "Pending"
        );

        DataStore.get().setDriverStatus(driver.getName(), "On Trip");

        JOptionPane.showMessageDialog(this,
                driverOnly
                        ? "Driver booked successfully for your own vehicle!"
                        : "Taxi booking confirmed for " + txtName.getText() + "!",
                "Success", JOptionPane.INFORMATION_MESSAGE);

        if (!Session.hasRole("CUSTOMER")) txtName.setText("");
        txtPhone.setText("");
        txtPickup.setText("");
        txtDrop.setText("");
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
