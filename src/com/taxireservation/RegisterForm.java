package com.taxireservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

/** Customer self-registration screen. */
public class RegisterForm extends JFrame {
    private final JTextField txtName = new JTextField();
    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final JPasswordField txtConfirmPassword = new JPasswordField();

    public RegisterForm() {
        setTitle("Register Customer - Taxi Reservation");
        setSize(450, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        buildUi();
    }

    private void buildUi() {
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(new Color(0x1F2937));
        setContentPane(background);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(28, 30, 28, 30));
        card.setPreferredSize(new Dimension(350, 455));

        JLabel title = new JLabel("Create Customer Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        styleField(txtName);
        styleField(txtUsername);
        styleField(txtPassword);
        styleField(txtConfirmPassword);

        JButton register = new JButton("Register");
        register.setAlignmentX(Component.CENTER_ALIGNMENT);
        register.setMaximumSize(new Dimension(270, 40));
        register.setBackground(new Color(0x10B981));
        register.setForeground(Color.WHITE);
        register.setFocusPainted(false);
        register.addActionListener(e -> registerCustomer());

        JButton back = new JButton("Back to Login Options");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> open(new LoginForm()));

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 22)));
        card.add(labeled("Full Name", txtName));
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(labeled("Username / Email", txtUsername));
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(labeled("Password", txtPassword));
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(labeled("Confirm Password", txtConfirmPassword));
        card.add(Box.createRigidArea(new Dimension(0, 22)));
        card.add(register);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(back);

        background.add(card);
        getRootPane().setDefaultButton(register);
    }

    private void registerCustomer() {
        String name = txtName.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirmPassword.getPassword());

        if (name.length() == 0 || username.length() == 0 || password.length() == 0 || confirm.length() == 0) {
            showError("Please fill in every field.");
            return;
        }
        if (username.contains("|")) {
            showError("Username cannot contain the | character.");
            return;
        }
        if (password.length() < 4) {
            showError("Password must contain at least 4 characters.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }

        try {
            UserAccount account = AuthStore.registerCustomer(name, username, password);
            if (account == null) {
                showError("That username is already registered.");
                return;
            }
            JOptionPane.showMessageDialog(this,
                    "Account created successfully. You can now sign in as a customer.",
                    "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            SwingUtilities.invokeLater(() -> new CustomerLoginForm(username).setVisible(true));
        } catch (IOException ex) {
            showError("Unable to save the account: " + ex.getMessage());
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
        field.setMaximumSize(new Dimension(270, 32));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private void open(JFrame frame) {
        dispose();
        frame.setVisible(true);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Registration", JOptionPane.ERROR_MESSAGE);
    }
}
