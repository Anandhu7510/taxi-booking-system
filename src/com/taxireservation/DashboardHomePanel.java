package com.taxireservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Landing screen: quick-glance stat cards.
 *
 * NetBeans GUI Builder equivalent:
 *  - JPanel with GridLayout(1,4,15,15) row of "card" JPanels
 *  - Each card = JPanel with a big JLabel (number) + small JLabel (caption)
 */
public class DashboardHomePanel extends JPanel {

    private JLabel lblTotal, lblOngoing, lblCompleted, lblRevenue;

    public DashboardHomePanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(25, 25, 25, 25));
        setBackground(Color.WHITE);

        JLabel header = new JLabel("Dashboard Overview");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(header, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 15, 15));
        cards.setBorder(new EmptyBorder(20, 0, 0, 0));

        lblTotal = new JLabel();
        lblOngoing = new JLabel();
        lblCompleted = new JLabel();
        lblRevenue = new JLabel();

        cards.add(statCard("Total Bookings", lblTotal, new Color(0x3B82F6)));
        cards.add(statCard("Ongoing Trips", lblOngoing, new Color(0xF59E0B)));
        cards.add(statCard("Completed", lblCompleted, new Color(0x10B981)));
        cards.add(statCard("Revenue (\u20B9)", lblRevenue, new Color(0x8B5CF6)));

        add(cards, BorderLayout.CENTER);
        refresh();
    }

    private JPanel statCard(String caption, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB), 1, true),
                new EmptyBorder(18, 18, 18, 18)));

        JPanel accentBar = new JPanel();
        accentBar.setBackground(accent);
        accentBar.setMaximumSize(new Dimension(40, 5));
        accentBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel captionLbl = new JLabel(caption);
        captionLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        captionLbl.setForeground(Color.GRAY);
        captionLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(accentBar);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(valueLabel);
        card.add(captionLbl);
        return card;
    }

    public void refresh() {
        DataStore ds = DataStore.get();
        lblTotal.setText(String.valueOf(ds.getReservations().size()));
        lblOngoing.setText(String.valueOf(ds.countByStatus("Ongoing")));
        lblCompleted.setText(String.valueOf(ds.countByStatus("Completed")));
        lblRevenue.setText(String.format("%.0f", ds.totalRevenue()));
    }
}
