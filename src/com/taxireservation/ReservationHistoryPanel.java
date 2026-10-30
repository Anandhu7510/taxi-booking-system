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
    private JButton btnStart, btnComplete, btnCancel;

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
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        p.setBackground(Color.WHITE);

        btnStart = actionButton("Start Trip", new Color(0x1D4ED8));
        btnStart.addActionListener(e -> updateSelectedStatus("Ongoing"));

        btnComplete = actionButton("Mark Completed", new Color(0x047857));
        btnComplete.addActionListener(e -> updateSelectedStatus("Completed"));

        btnCancel = actionButton("Cancel Booking", new Color(0xB91C1C));
        btnCancel.addActionListener(e -> updateSelectedStatus("Cancelled"));

        p.add(btnStart);
        p.add(btnComplete);
        p.add(btnCancel);
        updateActionButtons();
        return p;
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

    /** A completed/cancelled reservation is final, so its actions stay disabled. */
    private void updateActionButtons() {
        if (btnStart == null || btnComplete == null || btnCancel == null || table == null) return;
        int viewRow = table.getSelectedRow();
        String status = viewRow < 0 ? "" :
                String.valueOf(model.getValueAt(table.convertRowIndexToModel(viewRow), 11));
        boolean active = "Pending".equals(status) || "Ongoing".equals(status);
        setActionState(btnStart, "Pending".equals(status), new Color(0x1D4ED8));
        setActionState(btnComplete, active, new Color(0x047857));
        setActionState(btnCancel, active, new Color(0xB91C1C));
    }

    private static void setActionState(JButton button, boolean enabled, Color activeColor) {
        button.setEnabled(enabled);
        button.setBackground(enabled ? activeColor : new Color(0xE5E7EB));
        button.setForeground(enabled ? Color.WHITE : new Color(0x374151));
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
        if (viewRow < 0) return;
        int row = table.convertRowIndexToModel(viewRow);
        int id = (int) model.getValueAt(row, 0);
        String current = String.valueOf(model.getValueAt(row, 11));
        boolean active = "Pending".equals(current) || "Ongoing".equals(current);
        if (!active || ("Ongoing".equals(status) && !"Pending".equals(current))) {
            updateActionButtons();
            return;
        }
        try {
            DataStore.get().updateReservationStatus(id, status);
            loadReservations();
            if (homePanel != null) homePanel.refresh();
        } catch (IllegalStateException ex) {
            // Another session may have completed/cancelled this booking since the last refresh.
            loadReservations();
            if (ex.getMessage() != null && ex.getMessage().contains("cannot transition")) {
                JOptionPane.showMessageDialog(this,
                        "This booking's status has already changed. The list has been refreshed.",
                        "Booking Already Updated", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Unable to Update Booking",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) loadReservations();
        super.setVisible(visible);
    }
}
