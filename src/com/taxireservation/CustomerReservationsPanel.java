package com.taxireservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Customer history with owner-only cancellation of active bookings. */
public class CustomerReservationsPanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel actionHint = new JLabel("Select a reservation to manage it.");
    private final Runnable onBookingChanged;

    public CustomerReservationsPanel(Runnable onBookingChanged) {
        this.onBookingChanged = onBookingChanged;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel header = new JLabel("My Reservations");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(header, BorderLayout.NORTH);

        String[] columns = {"ID", "Pickup", "Drop", "Date / Time", "Service",
                "Vehicle / Cab", "Distance", "Driver", "Fare", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateActionHint();
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(20, 0, 0, 0));
        add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(0, 4));
        footer.setBackground(Color.WHITE);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        actions.setBackground(Color.WHITE);

        JButton cancel = actionButton("Cancel Booking", new Color(0xB91C1C));
        cancel.addActionListener(e -> cancelSelectedBooking());
        cancel.setToolTipText("Cancel your Pending or Ongoing reservation.");
        JButton refresh = actionButton("Refresh", new Color(0x374151));
        refresh.addActionListener(e -> refresh());
        actions.add(cancel);
        actions.add(refresh);

        actionHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        actionHint.setForeground(new Color(0x374151));
        actionHint.setBorder(new EmptyBorder(0, 10, 7, 0));
        footer.add(actions, BorderLayout.NORTH);
        footer.add(actionHint, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);

        refresh();
    }

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

    private void updateActionHint() {
        int row = table.getSelectedRow();
        if (row < 0) {
            actionHint.setText("Select one of your reservations, then choose Cancel Booking or Refresh.");
            return;
        }
        String status = String.valueOf(model.getValueAt(table.convertRowIndexToModel(row), 9));
        if ("Pending".equals(status) || "Ongoing".equals(status)) {
            actionHint.setText("You can cancel this " + status + " reservation.");
        } else {
            actionHint.setText("This reservation is " + status + ". It cannot be cancelled again.");
        }
    }

    private void cancelSelectedBooking() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select your reservation first.",
                    "No Reservation Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int row = table.convertRowIndexToModel(selected);
        int id = (int) model.getValueAt(row, 0);
        String status = String.valueOf(model.getValueAt(row, 9));
        if (!"Pending".equals(status) && !"Ongoing".equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "Booking #" + id + " is already " + status + " and cannot be cancelled.",
                    "Cancellation Not Available", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Cancel booking #" + id + "?\nYour assigned driver will become available again.",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;

        try {
            DataStore.get().cancelOwnReservation(id);
            refresh();
            if (onBookingChanged != null) onBookingChanged.run();
            JOptionPane.showMessageDialog(this, "Booking #" + id + " was cancelled.",
                    "Reservation Cancelled", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalStateException ex) {
            refresh();
            JOptionPane.showMessageDialog(this,
                    "Unable to cancel booking #" + id + ".\n" + ex.getMessage(),
                    "Cancellation Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refresh() {
        Integer selectedId = null;
        int selected = table.getSelectedRow();
        if (selected >= 0) {
            selectedId = (Integer) model.getValueAt(table.convertRowIndexToModel(selected), 0);
        }
        UserAccount user = Session.getCurrentUser();
        if (user == null || !Session.hasRole("CUSTOMER")) {
            model.setRowCount(0);
            updateActionHint();
            return;
        }
        try {
            java.util.List<Reservation> reservations =
                    DataStore.get().getReservationsForCustomer(user.getId());
            model.setRowCount(0);
            for (Reservation r : reservations) {
                model.addRow(new Object[]{
                        r.getId(), r.getPickupLocation(), r.getDropLocation(), r.getDateTime(),
                        r.getServiceType(), r.getVehicleDisplay(), r.getDistanceDisplay(),
                        r.getDriverName(), String.format("\u20B9 %.0f", r.getFare()),
                        r.getStatus()
                });
            }
            if (selectedId != null) {
                for (int i = 0; i < model.getRowCount(); i++) {
                    if (selectedId.equals(model.getValueAt(i, 0))) {
                        table.setRowSelectionInterval(i, i);
                        break;
                    }
                }
            }
            updateActionHint();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) refresh();
        super.setVisible(visible);
    }
}
