package com.taxireservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Read-only reservation history for the currently signed-in customer. */
public class CustomerReservationsPanel extends JPanel {
    private final DefaultTableModel model;

    public CustomerReservationsPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel header = new JLabel("My Reservations");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(header, BorderLayout.NORTH);

        String[] columns = {"ID", "Pickup", "Drop", "Date / Time", "Service", "Vehicle / Cab", "Driver", "Fare", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(20, 0, 0, 0));
        add(scrollPane, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        UserAccount user = Session.getCurrentUser();
        if (user == null) return;

        String customerName = user.getDisplayName();
        for (Reservation r : DataStore.get().getReservations()) {
            if (!r.getCustomerName().equalsIgnoreCase(customerName)) continue;
            model.addRow(new Object[]{
                    r.getId(),
                    r.getPickupLocation(),
                    r.getDropLocation(),
                    r.getDateTime(),
                    r.getServiceType(),
                    r.getVehicleDisplay(),
                    r.getDriverName(),
                    String.format("\u20B9 %.0f", r.getFare()),
                    r.getStatus()
            });
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) refresh();
        super.setVisible(visible);
    }
}
