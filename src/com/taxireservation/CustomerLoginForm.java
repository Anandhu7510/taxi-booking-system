package com.taxireservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

/** Customer-only login screen. */
public class CustomerLoginForm extends JFrame {
    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();

    public CustomerLoginForm() {
        this("");
    }

    public CustomerLoginForm(String username) {
        setTitle("Customer Login - Taxi Reservation");
        setSize(430, 470);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        txtUsername.setText(username);
        buildUi();
    }

    private void buildUi() {
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(new Color(0x1F2937));
        setContentPane(background);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(30, 30, 30, 30));
        card.setPreferredSize(new Dimension(340, 360));

        JLabel title = new JLabel("Customer Login");
        title.setFont(new Font("Segoe UI", Font.BOLD, 21));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        styleField(txtUsername);
        styleField(txtPassword);

        JButton login = primaryButton("Login", new Color(0x2563EB));
        login.addActionListener(e -> doLogin());

        JButton register = primaryButton("Create Account", new Color(0x10B981));
        register.addActionListener(e -> open(new RegisterForm()));

        JButton back = secondaryButton("Back");
        back.addActionListener(e -> open(new LoginForm()));

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 26)));
        card.add(labeled("Username / Email", txtUsername));
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(labeled("Password", txtPassword));
        card.add(Box.createRigidArea(new Dimension(0, 24)));
        card.add(login);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(register);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(back);

        background.add(card);
        getRootPane().setDefaultButton(login);
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        if (username.length() == 0 || password.length() == 0) {
            showError("Enter both username and password.");
            return;
        }

        try {
            UserAccount account = AuthStore.authenticate(username, password, "CUSTOMER");
            if (account == null) {
                showError("Invalid customer credentials.");
                return;
            }
            Session.login(account);
            dispose();
            SwingUtilities.invokeLater(() -> new CustomerDashboard().setVisible(true));
        } catch (IOException ex) {
            showError("Unable to read user accounts: " + ex.getMessage());
        }
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(field);
        return p;
    }

    private void styleField(JComponent field) {
        field.setMaximumSize(new Dimension(260, 32));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private JButton primaryButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(260, 38));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        return button;
    }

    private JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(260, 34));
        button.setForeground(new Color(0x1F2937));
        return button;
    }

    private void open(JFrame frame) {
        dispose();
        frame.setVisible(true);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Login Failed", JOptionPane.ERROR_MESSAGE);
    }
}
