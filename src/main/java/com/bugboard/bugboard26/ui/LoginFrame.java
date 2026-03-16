package com.bugboard.bugboard26.ui;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JButton loginButton = new JButton("Login");
    private final JLabel statusLabel = new JLabel(" ");

    public LoginFrame() {
        setTitle("BugBoard26 — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(380, 230);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("BugBoard26", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 4;
        panel.add(statusLabel, gbc);

        add(panel);

        emailField.setText("admin@bugboard.com");
        passwordField.setText("admin123");

        loginButton.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(loginButton);
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Inserisci email e password");
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setText("Accesso in corso...");
        statusLabel.setForeground(Color.BLUE);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                String json = String.format(
                        "{\"email\":\"%s\",\"password\":\"%s\"}", email, password
                );
                return ApiClient.post("/auth/login", json);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    JsonNode node = ApiClient.mapper.readTree(response);
                    if (node.has("token")) {
                        ApiClient.setToken(node.get("token").asText());
                        dispose();
                        new DashboardFrame().setVisible(true);
                    } else {
                        statusLabel.setForeground(Color.RED);
                        statusLabel.setText("Credenziali non valide");
                        loginButton.setEnabled(true);
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Errore: backend non raggiungibile");
                    loginButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
}
