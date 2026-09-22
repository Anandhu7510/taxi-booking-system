package com.taxireservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Login screen. Default credentials: admin / admin123
 *
 * NetBeans GUI Builder equivalent:
 *  - JFrame Form, set Layout to "Free Design" (GroupLayout, default)
 *  - Drop a JPanel (cardPanel) sized ~360x420, center it, give it a background color
 *  - Add JLabel "Taxi & Driver Reservation System" (bold, 20pt)
 *  - Add JLabel + JTextField for Username
 *  - Add JLabel + JPasswordField for Password
 *  - Add JButton "Login" -> in Events tab, double-click actionPerformed
 */
public class LoginForm extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginForm() {
        setTitle("Taxi & Driver Reservation System - Login");
        setSize(420, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(new Color(0x1F2937));
        setContentPane(background);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(30, 30, 30, 30));
        card.setPreferredSize(new Dimension(340, 380));

        JLabel title = new JLabel("\uD83D\uDE95 Taxi Reservation");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Driver & Booking Management");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtUsername = new JTextField("admin");
        txtPassword = new JPasswordField("admin123");
        styleField(txtUsername);
        styleField(txtPassword);

        JButton btnLogin = new JButton("Login");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setBackground(new Color(0xF59E0B));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setMaximumSize(new Dimension(260, 38));
        btnLogin.addActionListener(e -> doLogin());

        JLabel hint = new JLabel("hint: admin / admin123");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(Color.LIGHT_GRAY);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(subtitle);
        card.add(Box.createRigidArea(new Dimension(0, 24)));
        card.add(labeled("Username", txtUsername));
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(labeled("Password", txtPassword));
        card.add(Box.createRigidArea(new Dimension(0, 24)));
        card.add(btnLogin);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(hint);

        background.add(card);

        // Enter key submits
        getRootPane().setDefaultButton(btnLogin);
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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

    private void doLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());
        if (user.equals("admin") && pass.equals("admin123")) {
            dispose();
            SwingUtilities.invokeLater(() -> new MainDashboard().setVisible(true));
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
