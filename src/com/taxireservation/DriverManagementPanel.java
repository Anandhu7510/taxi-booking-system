package com.taxireservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Driver / vehicle management: list, add, remove, change status.
 *
 * NetBeans GUI Builder equivalent:
 *  - JPanel, BorderLayout
 *  - NORTH: JPanel (FlowLayout) with JTextFields for name/phone/license/vehicle no,
 *    a JComboBox for vehicle type, and a JButton "Add Driver"
 *  - CENTER: JScrollPane containing a JTable bound to a DefaultTableModel
 *  - SOUTH: JButtons "Set Available" / "Set Offline" / "Remove Driver" acting on
 *    the selected table row (table.getSelectedRow())
 */
public class DriverManagementPanel extends JPanel {

    private DefaultTableModel model;
    private JTable table;
    private JTextField txtName, txtPhone, txtLicense, txtVehicleNo;
    private JComboBox<String> cboType;

    public DriverManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel header = new JLabel("Driver Management");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(header, BorderLayout.NORTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(20, 0, 0, 0));
        wrapper.add(buildAddForm(), BorderLayout.NORTH);
        wrapper.add(buildTable(), BorderLayout.CENTER);
        wrapper.add(buildActions(), BorderLayout.SOUTH);
        add(wrapper, BorderLayout.CENTER);

        loadDrivers();
    }

    private JPanel buildAddForm() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        p.setBorder(BorderFactory.createTitledBorder("Add New Driver"));

        txtName = new JTextField(10);
        txtPhone = new JTextField(10);
        txtLicense = new JTextField(8);
        txtVehicleNo = new JTextField(8);
        cboType = new JComboBox<>(new String[]{"Mini", "Sedan", "SUV", "Luxury"});

        p.add(new JLabel("Name:")); p.add(txtName);
        p.add(new JLabel("Phone:")); p.add(txtPhone);
        p.add(new JLabel("License#:")); p.add(txtLicense);
        p.add(new JLabel("Vehicle#:")); p.add(txtVehicleNo);
        p.add(new JLabel("Type:")); p.add(cboType);

        JButton btnAdd = new JButton("Add Driver");
        btnAdd.setBackground(new Color(0x3B82F6));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.addActionListener(e -> addDriver());
        p.add(btnAdd);

        return p;
    }

    private JScrollPane buildTable() {
        String[] cols = {"ID", "Name", "Phone", "License No.", "Vehicle No.", "Type", "Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        return new JScrollPane(table);
    }

    private JPanel buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));

        JButton btnAvailable = new JButton("Set Available");
        btnAvailable.addActionListener(e -> updateSelectedStatus("Available"));

        JButton btnOffline = new JButton("Set Offline");
        btnOffline.addActionListener(e -> updateSelectedStatus("Offline"));

        JButton btnRemove = new JButton("Remove Driver");
        btnRemove.setForeground(new Color(0xB91C1C));
        btnRemove.addActionListener(e -> removeSelected());

        p.add(btnAvailable);
        p.add(btnOffline);
        p.add(btnRemove);
        return p;
    }

    private void loadDrivers() {
        model.setRowCount(0);
        List<Driver> drivers = DataStore.get().getDrivers();
        for (Driver d : drivers) {
            model.addRow(new Object[]{d.getId(), d.getName(), d.getPhone(), d.getLicenseNo(),
                    d.getVehicleNumber(), d.getVehicleType(), d.getStatus()});
        }
    }

    private void addDriver() {
        if (txtName.getText().isBlank() || txtPhone.getText().isBlank()
                || txtVehicleNo.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Name, phone and vehicle number are required.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DataStore.get().addDriver(txtName.getText(), txtPhone.getText(), txtLicense.getText(),
                txtVehicleNo.getText(), (String) cboType.getSelectedItem(), "Available");
        txtName.setText(""); txtPhone.setText(""); txtLicense.setText(""); txtVehicleNo.setText("");
        loadDrivers();
    }

    private void updateSelectedStatus(String status) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a driver first.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String name = (String) model.getValueAt(row, 1);
        DataStore.get().setDriverStatus(name, status);
        loadDrivers();
    }

    private void removeSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a driver first.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        Driver toRemove = null;
        for (Driver d : DataStore.get().getDrivers()) if (d.getId() == id) toRemove = d;
        if (toRemove != null) DataStore.get().removeDriver(toRemove);
        loadDrivers();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) loadDrivers();
        super.setVisible(visible);
    }
}
