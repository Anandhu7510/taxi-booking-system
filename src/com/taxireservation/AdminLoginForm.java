package com.taxireservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

/** Administrator-only login screen. */
public class AdminLoginForm extends JFrame {
    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();

    public AdminLoginForm() {
        setTitle("Admin Login - Taxi Reservation");
        setSize(430, 430);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        buildUi();
    }

    private void buildUi() {
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(new Color(0x111827));
        setContentPane(background);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(30, 30, 30, 30));
        card.setPreferredSize(new Dimension(340, 320));

        JLabel title = new JLabel("Administrator Login");
        title.setFont(new Font("Segoe UI", Font.BOLD, 21));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        styleField(txtUsername);
        styleField(txtPassword);

        JButton login = new JButton("Login as Admin");
        login.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        login.setAlignmentX(Component.CENTER_ALIGNMENT);
        login.setMaximumSize(new Dimension(260, 38));
        login.setBackground(new Color(0x374151));
        login.setForeground(Color.WHITE);
        login.setFont(new Font("Segoe UI", Font.BOLD, 12));
        login.setOpaque(true);
        login.setContentAreaFilled(true);
        login.setBorderPainted(false);
        login.setFocusPainted(false);
        login.addActionListener(e -> doLogin());

        JButton back = new JButton("Back");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.setMaximumSize(new Dimension(260, 34));
        back.setForeground(new Color(0x1F2937));
        back.addActionListener(e -> open(new LoginForm()));

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 26)));
        card.add(labeled("Admin Username", txtUsername));
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(labeled("Password", txtPassword));
        card.add(Box.createRigidArea(new Dimension(0, 24)));
        card.add(login);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(back);

        background.add(card);
        getRootPane().setDefaultButton(login);
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        if (username.length() == 0 || password.length() == 0) {
            showError("Enter both admin username and password.");
            return;
        }

        try {
            UserAccount account = AuthStore.authenticate(username, password, "ADMIN");
            if (account == null) {
                showError("Invalid administrator credentials.");
                return;
            }
            Session.login(account);
            dispose();
            SwingUtilities.invokeLater(() -> new MainDashboard().setVisible(true));
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

    private void open(JFrame frame) {
        dispose();
        frame.setVisible(true);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Login Failed", JOptionPane.ERROR_MESSAGE);
    }
}
