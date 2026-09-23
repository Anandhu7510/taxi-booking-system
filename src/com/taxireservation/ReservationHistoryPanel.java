package com.taxireservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * All reservations: search box, status filter, and Complete/Cancel actions.
 *
 * NetBeans GUI Builder equivalent:
 *  - JPanel, BorderLayout
 *  - NORTH: JPanel (FlowLayout) with JTextField txtSearch + JComboBox cboStatusFilter
 *    {All, Pending, Ongoing, Completed, Cancelled} + JButton "Search"
 *  - CENTER: JScrollPane + JTable bound to DefaultTableModel
 *  - SOUTH: JButtons "Mark Completed" / "Cancel Booking" acting on selected row
 */
public class ReservationHistoryPanel extends JPanel {

    private DefaultTableModel model;
    private JTable table;
    private JTextField txtSearch;
    private JComboBox<String> cboStatusFilter;
    private final DashboardHomePanel homePanel;

    public ReservationHistoryPanel(DashboardHomePanel homePanel) {
        this.homePanel = homePanel;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel header = new JLabel("Reservations");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(header, BorderLayout.NORTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(20, 0, 0, 0));
        wrapper.add(buildFilterBar(), BorderLayout.NORTH);
        wrapper.add(buildTable(), BorderLayout.CENTER);
        wrapper.add(buildActions(), BorderLayout.SOUTH);
        add(wrapper, BorderLayout.CENTER);

        loadReservations();
    }

    private JPanel buildFilterBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

        txtSearch = new JTextField(18);
        cboStatusFilter = new JComboBox<>(new String[]{"All", "Pending", "Ongoing", "Completed", "Cancelled"});

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> loadReservations());
        cboStatusFilter.addActionListener(e -> loadReservations());

        p.add(new JLabel("Search customer/location:"));
        p.add(txtSearch);
        p.add(new JLabel("Status:"));
        p.add(cboStatusFilter);
        p.add(btnSearch);
        return p;
    }

    private JScrollPane buildTable() {
        String[] cols = {"ID", "Customer", "Phone", "Pickup", "Drop", "Date/Time",
                "Service", "Vehicle / Cab", "Distance", "Driver", "Fare", "Status"};
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

        JButton btnComplete = new JButton("Mark Completed");
        btnComplete.setBackground(new Color(0x10B981));
        btnComplete.setForeground(Color.WHITE);
        btnComplete.setFocusPainted(false);
        btnComplete.addActionListener(e -> updateSelectedStatus("Completed"));

        JButton btnCancel = new JButton("Cancel Booking");
        btnCancel.setBackground(new Color(0xEF4444));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> updateSelectedStatus("Cancelled"));

        p.add(btnComplete);
        p.add(btnCancel);
        return p;
    }

    private void loadReservations() {
        model.setRowCount(0);
        String query = txtSearch.getText().trim().toLowerCase();
        String statusFilter = (String) cboStatusFilter.getSelectedItem();

        List<Reservation> reservations = DataStore.get().getReservations();
        for (Reservation r : reservations) {
            boolean matchesQuery = query.isEmpty()
                    || r.getCustomerName().toLowerCase().contains(query)
                    || r.getPickupLocation().toLowerCase().contains(query)
                    || r.getDropLocation().toLowerCase().contains(query);
            boolean matchesStatus = "All".equals(statusFilter) || r.getStatus().equals(statusFilter);
            if (matchesQuery && matchesStatus) {
                model.addRow(new Object[]{r.getId(), r.getCustomerName(), r.getCustomerPhone(),
                        r.getPickupLocation(), r.getDropLocation(), r.getDateTime(), r.getServiceType(),
                        r.getVehicleDisplay(), r.getDistanceDisplay(), r.getDriverName(),
                        String.format("\u20B9%.0f", r.getFare()), r.getStatus()});
            }
        }
    }

    private void updateSelectedStatus(String status) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a reservation first.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        String driverName = (String) model.getValueAt(row, 9);
        for (Reservation r : DataStore.get().getReservations()) {
            if (r.getId() == id) r.setStatus(status);
        }
        if (!status.equals("Ongoing")) {
            DataStore.get().setDriverStatus(driverName, "Available");
        }
        loadReservations();
        if (homePanel != null) homePanel.refresh();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) loadReservations();
        super.setVisible(visible);
    }
}
