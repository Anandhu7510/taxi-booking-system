package com.taxireservation;

import javax.swing.*;

/**
 * Entry point. In NetBeans, right-click this file and "Set as Main Class"
 * (Project Properties > Run > Main Class = com.taxireservation.Main).
 */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
