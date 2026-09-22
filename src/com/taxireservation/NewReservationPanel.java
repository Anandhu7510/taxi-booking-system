package com.taxireservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * New booking form: customer details, pickup/drop, cab type, driver assignment,
 * live fare estimate.
 *
 * NetBeans GUI Builder equivalent:
 *  - JPanel, layout GroupLayout (default "Free Design")
 *  - Row of JLabel + JTextField pairs for Customer Name / Phone
 *  - JLabel + JTextField for Pickup Location, Drop Location
 *  - JLabel + JFormattedTextField or JSpinner (date model) for Date/Time
 *  - JLabel + JComboBox<String> cboCabType {Mini, Sedan, SUV, Luxury}
 *  - JLabel + JComboBox<Driver> cboDriver, populated from DataStore.getAvailableDrivers()
 *  - JLabel lblFare showing live estimate (update in cboCabType's itemStateChanged)
 *  - JButton "Confirm Booking" -> actionPerformed calls DataStore.addReservation(...)
 */
public class NewReservationPanel extends JPanel {

    private final JTextField txtName = new JTextField();
    private final JTextField txtPhone = new JTextField();
    private final JTextField txtPickup = new JTextField();
    private final JTextField txtDrop = new JTextField();
    private final JTextField txtDateTime = new JTextField(currentDateTime());
    private final JComboBox<String> cboCabType = new JComboBox<>(new String[]{"Mini", "Sedan", "SUV", "Luxury"});
    private final JComboBox<Driver> cboDriver = new JComboBox<>();
    private final JLabel lblFareValue = new JLabel("\u20B9 0");
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
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addField(form, c, row++, "Customer Name", txtName);
        addField(form, c, row++, "Phone Number", txtPhone);
        addField(form, c, row++, "Pickup Location", txtPickup);
        addField(form, c, row++, "Drop Location", txtDrop);
        addField(form, c, row++, "Date / Time", txtDateTime);
        addField(form, c, row++, "Cab Type", cboCabType);
        addField(form, c, row++, "Assign Driver", cboDriver);

        c.gridx = 0; c.gridy = row; c.gridwidth = 1;
        JLabel fareCaption = new JLabel("Estimated Fare:");
        fareCaption.setFont(new Font("Segoe UI", Font.BOLD, 13));
        form.add(fareCaption, c);
        c.gridx = 1;
        lblFareValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblFareValue.setForeground(new Color(0x10B981));
        form.add(lblFareValue, c);
        row++;

        JButton btnConfirm = new JButton("Confirm Booking");
        btnConfirm.setBackground(new Color(0xF59E0B));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFocusPainted(false);
        btnConfirm.addActionListener(e -> confirmBooking());
        c.gridx = 1; c.gridy = row; c.gridwidth = 1;
        form.add(btnConfirm, c);

        cboCabType.addActionListener(e -> updateFareEstimate());
        updateFareEstimate();

        add(form, BorderLayout.CENTER);
    }

    private void addField(JPanel form, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0; c.gridy = row; c.gridwidth = 1; c.weightx = 0;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(l, c);
        c.gridx = 1; c.weightx = 1;
        field.setPreferredSize(new Dimension(260, 30));
        form.add(field, c);
    }

    private void updateFareEstimate() {
        String type = (String) cboCabType.getSelectedItem();
        double base = switch (type == null ? "" : type) {
            case "Mini" -> 220;
            case "Sedan" -> 320;
            case "SUV" -> 480;
            case "Luxury" -> 750;
            default -> 250;
        };
        lblFareValue.setText(String.format("\u20B9 %.0f", base));
    }

    /** Call this whenever the panel becomes visible so the driver list is fresh. */
    @Override
    public void setVisible(boolean visible) {
        if (visible) refreshDriverList();
        super.setVisible(visible);
    }

    private void refreshDriverList() {
        Driver selected = (Driver) cboDriver.getSelectedItem();
        cboDriver.removeAllItems();
        for (Driver d : DataStore.get().getAvailableDrivers()) cboDriver.addItem(d);
        if (selected != null) cboDriver.setSelectedItem(selected);
    }

    private void confirmBooking() {
        if (txtName.getText().isBlank() || txtPhone.getText().isBlank()
                || txtPickup.getText().isBlank() || txtDrop.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Driver driver = (Driver) cboDriver.getSelectedItem();
        if (driver == null) {
            JOptionPane.showMessageDialog(this, "No available driver to assign. Try again shortly.",
                    "No Driver Available", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double fare = Double.parseDouble(lblFareValue.getText().replace("\u20B9", "").trim());

        DataStore.get().addReservation(txtName.getText(), txtPhone.getText(), txtPickup.getText(),
                txtDrop.getText(), txtDateTime.getText(), (String) cboCabType.getSelectedItem(),
                driver.getName(), fare, "Pending");
        DataStore.get().setDriverStatus(driver.getName(), "On Trip");

        JOptionPane.showMessageDialog(this, "Booking confirmed for " + txtName.getText() + "!",
                "Success", JOptionPane.INFORMATION_MESSAGE);

        txtName.setText(""); txtPhone.setText(""); txtPickup.setText(""); txtDrop.setText("");
        txtDateTime.setText(currentDateTime());
        refreshDriverList();
        if (homePanel != null) homePanel.refresh();
    }

    private static String currentDateTime() {
        return java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
