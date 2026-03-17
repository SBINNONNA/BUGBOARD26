package com.bugboard.bugboard26.ui;

import javax.swing.*;

public class SwingApp {

    public static void main(String[] args) {
        launch();
    }

    public static void launch() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
