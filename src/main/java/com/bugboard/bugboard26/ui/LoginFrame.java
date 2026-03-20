package com.bugboard.bugboard26.ui;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    // Palette viola
    static final Color PRIMARY      = new Color(110, 0, 200);
    static final Color PRIMARY_DARK = new Color(70, 0, 140);
    static final Color PRIMARY_LIGHT= new Color(180, 100, 255);
    static final Color BG           = new Color(245, 240, 255);
    static final Color BG_CARD      = new Color(255, 255, 255);

    private final JTextField emailField    = new JTextField(20);
    private final JPasswordField passField = new JPasswordField(20);
    private final JButton loginBtn         = makeButton("Accedi");
    private final JLabel statusLabel       = new JLabel(" ");

    public LoginFrame() {
        setTitle("BugBoard26 — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 520);
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(BG);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Logo grande centrato
        JPanel logoWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoWrapper.setOpaque(false);
        logoWrapper.add(new LogoPanel(true));
        gbc.gridy = 0;
        add(logoWrapper, gbc);

        // Titolo
        JLabel title = new JLabel("BugBoard26", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(PRIMARY);
        gbc.gridy = 1;
        add(title, gbc);

        JLabel subtitle = new JLabel("Issue Tracking Platform", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(140, 100, 180));
        gbc.gridy = 2;
        add(subtitle, gbc);

        // Card form
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 170, 240), 1, true),
                BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 4, 6, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Email
        c.gridx = 0; c.gridy = 0; c.gridwidth = 1;
        JLabel emailLbl = new JLabel("Email");
        emailLbl.setForeground(PRIMARY_DARK);
        emailLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        card.add(emailLbl, c);
        c.gridy = 1; c.gridwidth = 2;
        styleField(emailField);
        card.add(emailField, c);

        // Password
        c.gridy = 2; c.gridwidth = 1;
        JLabel passLbl = new JLabel("Password");
        passLbl.setForeground(PRIMARY_DARK);
        passLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        card.add(passLbl, c);
        c.gridy = 3; c.gridwidth = 2;
        styleField(passField);
        card.add(passField, c);

        // Bottone
        c.gridy = 4; c.gridwidth = 2;
        c.insets = new Insets(16, 4, 4, 4);
        card.add(loginBtn, c);

        // Status
        c.gridy = 5;
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);
        card.add(statusLabel, c);

        gbc.gridy = 3;
        gbc.insets = new Insets(10, 30, 10, 30);
        add(card, gbc);

        // Prefill
        emailField.setText("admin@bugboard.com");
        passField.setText("admin123");

        loginBtn.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(loginBtn);
    }

    private void styleField(JTextField f) {
        f.setPreferredSize(new Dimension(260, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(190, 160, 230), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
    }

    private static JButton makeButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(260, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String password = new String(passField.getPassword());
        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Inserisci email e password");
            return;
        }
        loginBtn.setEnabled(false);
        statusLabel.setForeground(new Color(110, 0, 200));
        statusLabel.setText("Accesso in corso...");

        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                String json = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
                return ApiClient.post("/auth/login", json);
            }
            @Override protected void done() {
                try {
                    JsonNode node = ApiClient.mapper.readTree(get());
                    if (node.has("token")) {
                        ApiClient.setToken(node.get("token").asText());
                        dispose();
                        new ProjectSelectionFrame().setVisible(true);
                    } else {
                        statusLabel.setForeground(Color.RED);
                        statusLabel.setText("Credenziali non valide");
                        loginBtn.setEnabled(true);
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Backend non raggiungibile");
                    loginBtn.setEnabled(true);
                }
            }
        }.execute();
    }
}
