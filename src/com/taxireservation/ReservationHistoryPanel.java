package com.taxireservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import java.awt.*;
import java.util.List;

/**
 * All reservations: search, filtering, and status-aware administrative actions.
 *
 * NetBeans GUI Builder equivalent:
 *  - JPanel, BorderLayout
 *  - NORTH: JPanel (FlowLayout) with JTextField txtSearch + JComboBox cboStatusFilter
 *    {All, Pending, Ongoing, Completed, Cancelled} + JButton "Search"
 *  - CENTER: JScrollPane + JTable bound to DefaultTableModel
 *  - SOUTH: status-aware Start / Complete / Cancel buttons for active bookings
 */
public class ReservationHistoryPanel extends JPanel {

    private DefaultTableModel model;
    private JTable table;
    private JTextField txtSearch;
    private JComboBox<String> cboStatusFilter;
    private final DashboardHomePanel homePanel;
    private JButton btnStart, btnComplete, btnCancel, btnDelete;
    private JLabel lblActionHint;

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
        p.setBackground(Color.WHITE);

        txtSearch = new JTextField(18);
        cboStatusFilter = new JComboBox<>(new String[]{"All", "Pending", "Ongoing", "Completed", "Cancelled"});

        JButton btnSearch = actionButton("Search", new Color(0x374151));
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
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) updateActionButtons();
        });
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        return new JScrollPane(table);
    }

    private JPanel buildActions() {
        JPanel footer = new JPanel(new BorderLayout(0, 4));
        footer.setBackground(Color.WHITE);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        actions.setBackground(Color.WHITE);

        btnStart = actionButton("Start Trip", new Color(0x1D4ED8));
        btnStart.addActionListener(e -> updateSelectedStatus("Ongoing"));

        btnComplete = actionButton("Mark Completed", new Color(0x047857));
        btnComplete.addActionListener(e -> updateSelectedStatus("Completed"));

        btnCancel = actionButton("Cancel Booking", new Color(0xB91C1C));
        btnCancel.addActionListener(e -> updateSelectedStatus("Cancelled"));

        btnDelete = actionButton("Delete Record", new Color(0x7F1D1D));
        btnDelete.addActionListener(e -> deleteSelected());

        JButton btnRefresh = actionButton("Refresh", new Color(0x374151));
        btnRefresh.addActionListener(e -> loadReservations());

        actions.add(btnStart);
        actions.add(btnComplete);
        actions.add(btnCancel);
        actions.add(btnDelete);
        actions.add(btnRefresh);

        lblActionHint = new JLabel("Select a reservation to manage it.");
        lblActionHint.setForeground(new Color(0x374151));
        lblActionHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblActionHint.setBorder(new EmptyBorder(0, 10, 7, 0));

        footer.add(actions, BorderLayout.NORTH);
        footer.add(lblActionHint, BorderLayout.SOUTH);
        updateActionButtons();
        return footer;
    }

    /** Use consistent, legible button colors across Windows look-and-feels. */
    private static JButton actionButton(String label, Color background) {
        JButton button = new JButton(label);
        button.setUI(new BasicButtonUI() {
            @Override
            protected void paintText(Graphics graphics, AbstractButton action,
                                     Rectangle textRect, String text) {
                graphics.setColor(action.getForeground());
                FontMetrics metrics = graphics.getFontMetrics(action.getFont());
                BasicGraphicsUtils.drawStringUnderlineCharAt(graphics, text,
                        action.getDisplayedMnemonicIndex(),
                        textRect.x, textRect.y + metrics.getAscent());
            }
        });
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        return button;
    }

    /** Keep actions responsive: invalid transitions explain why instead of silently doing nothing. */
    private void updateActionButtons() {
        if (lblActionHint == null || table == null) return;
        String status = selectedStatus();
        String hint;
        if ("Pending".equals(status)) {
            hint = "Pending: Start, complete, or cancel this booking.";
        } else if ("Ongoing".equals(status)) {
            hint = "Ongoing: Complete or cancel this trip.";
        } else if ("Completed".equals(status)) {
            hint = "Completed is final. Delete Record permanently removes this history and its revenue.";
        } else if ("Cancelled".equals(status)) {
            hint = "Cancelled is final. Delete Record permanently removes this booking.";
        } else {
            hint = "Select a reservation. Actions will explain if a status change is not allowed.";
        }
        lblActionHint.setText(hint);
        btnStart.setToolTipText("Start is only available for Pending reservations.");
        btnComplete.setToolTipText("Complete is only available for Pending or Ongoing reservations.");
        btnCancel.setToolTipText("Cancel is only available for Pending or Ongoing reservations.");
        btnDelete.setToolTipText("Permanently delete Completed or Cancelled reservations only.");
    }

    private String selectedStatus() {
        int row = table.getSelectedRow();
        return row < 0 ? null : String.valueOf(
                model.getValueAt(table.convertRowIndexToModel(row), 11));
    }

    private void loadReservations() {
        Integer selectedId = null;
        if (table.getSelectedRow() >= 0) {
            int row = table.convertRowIndexToModel(table.getSelectedRow());
            selectedId = (Integer) model.getValueAt(row, 0);
        }
        model.setRowCount(0);
        String query = txtSearch.getText().trim().toLowerCase();
        String statusFilter = (String) cboStatusFilter.getSelectedItem();
        try {
            for (Reservation r : DataStore.get().getReservations()) {
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
            if (selectedId != null) {
                for (int i = 0; i < model.getRowCount(); i++) {
                    if (selectedId.equals(model.getValueAt(i, 0))) {
                        int viewRow = table.convertRowIndexToView(i);
                        if (viewRow >= 0) table.setRowSelectionInterval(viewRow, viewRow);
                        break;
                    }
                }
            }
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        updateActionButtons();
    }

    private void updateSelectedStatus(String status) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a reservation first.",
                    "No Reservation Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
        int id = (int) model.getValueAt(row, 0);
        String current = selectedStatus();
        boolean active = "Pending".equals(current) || "Ongoing".equals(current);
        if (!active || ("Ongoing".equals(status) && !"Pending".equals(current))) {
            String reason = !active
                    ? "Booking #" + id + " is already " + current + ". This is a final status."
                    : "Booking #" + id + " is already Ongoing.";
            JOptionPane.showMessageDialog(this,
                    reason + "\nOnly Pending or Ongoing reservations can be completed or cancelled.",
                    "Action Not Available", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            DataStore.get().updateReservationStatus(id, status);
            loadReservations();
            if (homePanel != null) homePanel.refresh();
        } catch (IllegalStateException ex) {
            loadReservations();
            if (ex.getMessage() != null && ex.getMessage().contains("cannot transition")) {
                JOptionPane.showMessageDialog(this,
                        "This booking was already updated elsewhere. The list has been refreshed.",
                        "Booking Already Updated", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Unable to Update Booking",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a reservation first.",
                    "No Reservation Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
        int id = (int) model.getValueAt(row, 0);
        String current = selectedStatus();
        if (!"Completed".equals(current) && !"Cancelled".equals(current)) {
            JOptionPane.showMessageDialog(this,
                    "Booking #" + id + " is still active. Complete or cancel it before deleting.",
                    "Active Booking", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Permanently delete booking #" + id + " (" + current + ")?\n"
                + "This removes it from history and reports, including completed revenue.\n"
                + "This cannot be undone.",
                "Confirm Permanent Deletion", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) return;
        try {
            DataStore.get().deleteFinishedReservation(id);
            loadReservations();
            if (homePanel != null) homePanel.refresh();
        } catch (IllegalStateException ex) {
            loadReservations();
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Unable to Delete Booking", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) loadReservations();
        super.setVisible(visible);
    }
}
