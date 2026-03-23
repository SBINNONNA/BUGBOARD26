package com.bugboard.bugboard26.ui;

import com.fasterxml.jackson.databind.JsonNode;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private Image bgImage;

    public LoginFrame() {
        setTitle("BugBoard26 — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(true);

        try {
            java.io.InputStream is = getClass().getResourceAsStream("/sfondo.png");
            if (is != null) bgImage = javax.imageio.ImageIO.read(is);
        } catch (Exception ignored) {}

        JPanel root = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(100, 30, 170));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        root.setOpaque(true);
        setContentPane(root);

        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40, 0, 90, 210));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(360, 490));
        card.setBorder(BorderFactory.createEmptyBorder(30, 36, 30, 36));

        buildCard(card);
        root.add(card);
    }

    private void buildCard(JPanel card) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill  = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);

        // Logo
        g.gridy = 0;
        JPanel logoWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoWrap.setOpaque(false);
        logoWrap.add(buildLogo(130, 98));
        card.add(logoWrap, g);

        // Titolo
        g.gridy = 1;
        JLabel title = new JLabel("BugBoard26", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        card.add(title, g);

        // Sottotitoli (nomi)
        g.gridy = 2;
        JPanel names = new JPanel();
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        names.setOpaque(false);
        for (String n : new String[]{
                "Antonio Soritto N86004962",
                "Carmine Onorato N86005342"}) {
            JLabel l = new JLabel(n, SwingConstants.CENTER);
            l.setFont(new Font("SansSerif", Font.PLAIN, 11));
            l.setForeground(new Color(200, 170, 255));
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
            names.add(l);
        }
        card.add(names, g);

        // Email
        g.gridy = 3;
        card.add(makeLabel("Email"), g);
        g.gridy = 4;
        JTextField emailField = makeTextField();
        emailField.setText("admin@bugboard.com");
        card.add(emailField, g);

        // Password
        g.gridy = 5;
        card.add(makeLabel("Password"), g);
        g.gridy = 6;
        JPasswordField passField = makePasswordField();
        passField.setText("admin123");
        card.add(passField, g);

        // Bottone
        g.gridy = 7;
        g.insets = new Insets(18, 0, 6, 0);
        JButton loginBtn = new JButton("Accedi") {
            @Override
            protected void paintComponent(Graphics gr) {
                Graphics2D g2 = (Graphics2D) gr.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                        ? new Color(110, 30, 200)
                        : new Color(85, 0, 155));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        loginBtn.setPreferredSize(new Dimension(280, 44));
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        loginBtn.setContentAreaFilled(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.add(loginBtn, g);

        // Status
        g.gridy = 8;
        g.insets = new Insets(4, 0, 0, 0);
        JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(new Color(255, 120, 120));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        card.add(statusLabel, g);

        // Azione login con SwingWorker
        ActionListener doLogin = e -> {
            String email = emailField.getText().trim();
            String pass  = new String(passField.getPassword());
            if (email.isEmpty() || pass.isEmpty()) {
                statusLabel.setText("Inserisci email e password.");
                return;
            }
            loginBtn.setEnabled(false);
            statusLabel.setForeground(new Color(180, 140, 255));
            statusLabel.setText("Accesso in corso...");

            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    String json = String.format(
                            "{\"email\":\"%s\",\"password\":\"%s\"}", email, pass);
                    return ApiClient.post("/auth/login", json); // ← metodo corretto
                }
                @Override
                protected void done() {
                    try {
                        JsonNode node = ApiClient.mapper.readTree(get());
                        if (node.has("token")) {
                            ApiClient.setToken(node.get("token").asText());
                            dispose();
                            new ProjectSelectionFrame().setVisible(true);
                        } else {
                            statusLabel.setForeground(new Color(255, 120, 120));
                            statusLabel.setText("Credenziali non valide.");
                            loginBtn.setEnabled(true);
                        }
                    } catch (Exception ex) {
                        statusLabel.setForeground(new Color(255, 120, 120));
                        statusLabel.setText("Backend non raggiungibile.");
                        loginBtn.setEnabled(true);
                    }
                }
            }.execute();
        };

        loginBtn.addActionListener(doLogin);
        passField.addActionListener(doLogin);
        emailField.addActionListener(doLogin);
        getRootPane().setDefaultButton(loginBtn);
    }

    private JLabel buildLogo(int width, int height) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/logo.png");
            if (is != null) {
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(is);
                ImageIcon icon = new ImageIcon(
                        img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
                JLabel lbl = new JLabel(icon);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                return lbl;
            }
        } catch (Exception ignored) {}
        JLabel lbl = new JLabel("BB", SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 36));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(new Color(210, 185, 255));
        return l;
    }

    private JTextField makeTextField() {
        JTextField f = new JTextField(20);
        styleInput(f);
        return f;
    }

    private JPasswordField makePasswordField() {
        JPasswordField f = new JPasswordField(20);
        styleInput(f);
        return f;
    }

    private void styleInput(JTextField f) {
        f.setBackground(new Color(80, 20, 140, 180));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setPreferredSize(new Dimension(280, 38));
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(160, 100, 230), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
    }
}
