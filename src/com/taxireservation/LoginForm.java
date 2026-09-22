package com.taxireservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Authentication landing screen. Users explicitly choose the customer,
 * registration, or administrator flow instead of sharing one hard-coded login.
 */
public class LoginForm extends JFrame {

    public LoginForm() {
        setTitle("Taxi & Driver Reservation System");
        setSize(440, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(new Color(0x1F2937));
        setContentPane(background);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(32, 34, 32, 34));
        card.setPreferredSize(new Dimension(350, 390));

        JLabel title = new JLabel("\uD83D\uDE95 Taxi Reservation");
        title.setFont(new Font("Segoe UI", Font.BOLD, 21));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Choose how you want to continue");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton customerLogin = actionButton("Customer Login", new Color(0x2563EB));
        customerLogin.addActionListener(e -> open(new CustomerLoginForm()));

        JButton register = actionButton("Create Customer Account", new Color(0x10B981));
        register.addActionListener(e -> open(new RegisterForm()));

        JButton adminLogin = actionButton("Admin Login", new Color(0x374151));
        adminLogin.addActionListener(e -> open(new AdminLoginForm()));

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(subtitle);
        card.add(Box.createRigidArea(new Dimension(0, 38)));
        card.add(customerLogin);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(register);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(adminLogin);

        background.add(card);
    }

    private JButton actionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(270, 42));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    private void open(JFrame frame) {
        dispose();
        frame.setVisible(true);
    }
}
