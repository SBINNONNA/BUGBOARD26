package com.bugboard.bugboard26.ui;

import com.fasterxml.jackson.databind.JsonNode;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class ProfiloDialog extends JDialog {

    private static final Color BG        = new Color(130, 70, 195);
    private static final Color HEADER_BG = new Color(80, 0, 150);
    private static final Color CARD_BG   = new Color(155, 100, 210);
    private static final Color FOOTER_BG = new Color(80, 0, 150);

    public ProfiloDialog(DashboardFrame parent) {
        super(parent, "Il mio profilo", true);
        setSize(420, 420);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildHeader(parent), BorderLayout.NORTH);
        add(buildBody(),         BorderLayout.CENTER);
        add(buildFooter(),       BorderLayout.SOUTH);
    }

    // ─── HEADER ──────────────────────────────────────────
    private JPanel buildHeader(DashboardFrame parent) {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 18));
        header.setBackground(HEADER_BG);

        header.add(new AvatarPanel(65));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel nameL = new JLabel(parent.currentUserEmail.split("@")[0].toUpperCase());
        nameL.setFont(new Font("SansSerif", Font.BOLD, 17));
        nameL.setForeground(Color.WHITE);
        nameL.setAlignmentX(LEFT_ALIGNMENT);

        String role = parent.currentUserRole;
        JLabel roleL = new JLabel("  " + role + "  ");
        roleL.setFont(new Font("SansSerif", Font.BOLD, 11));
        roleL.setForeground(Color.WHITE);
        roleL.setOpaque(true);
        roleL.setBackground(getRoleColor(role));
        roleL.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
        roleL.setAlignmentX(LEFT_ALIGNMENT);

        info.add(nameL);
        info.add(Box.createVerticalStrut(6));
        info.add(roleL);

        header.add(info);
        return header;
    }

    // ─── BODY ─────────────────────────────────────────────
    private JScrollPane buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(CARD_BG);
        body.setBorder(BorderFactory.createEmptyBorder(18, 25, 18, 25));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 8, 7, 8);
        g.fill   = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        try {
            String   resp = ApiClient.get("/users/me");
            JsonNode n    = ApiClient.mapper.readTree(resp);

            addRow(body, g, 0, "📧  Email",  n.get("email").asText());
            addRow(body, g, 1, "🪪  ID",     "#" + n.get("id").asText());
            addRow(body, g, 2, "🔑  Ruolo",  n.path("role").asText("USER"));

            Long projectId = ApiClient.getCurrentProjectId();
            String issResp = ApiClient.get("/projects/" + projectId + "/issues");
            JsonNode issues = ApiClient.mapper.readTree(issResp);

            int count = 0;
            StringBuilder sb = new StringBuilder();
            for (JsonNode iss : issues) {
                String assignedTo = iss.path("assignedTo").path("id").asText("");
                if (assignedTo.equals(n.get("id").asText())) {
                    sb.append("• #").append(iss.get("id").asText())
                            .append("  ").append(iss.get("title").asText()).append("\n");
                    count++;
                }
            }

            addRow(body, g, 3, "📋  Assegnate", count + " issue");

            if (count > 0) {
                JTextArea issArea = new JTextArea(sb.toString().trim());
                issArea.setEditable(false);
                issArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
                issArea.setBackground(new Color(120, 70, 180));
                issArea.setForeground(new Color(220, 200, 255));
                issArea.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

                g.gridx = 0; g.gridy = 4; g.gridwidth = 2;
                JScrollPane sp = new JScrollPane(issArea);
                sp.setPreferredSize(new Dimension(340, 80));
                sp.setBorder(new LineBorder(new Color(160, 110, 220), 1, true));
                body.add(sp, g);
                g.gridwidth = 1;
            }

        } catch (Exception ex) {
            addRow(body, g, 0, "Errore", ex.getMessage());
        }

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CARD_BG);
        return scroll;
    }

    // ─── FOOTER ───────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        footer.setBackground(FOOTER_BG);

        // ── Cambia foto profilo ──
        JButton changePhoto = styledBtn("📷 Cambia foto", new Color(85, 0, 155));
        changePhoto.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Immagini", "jpg", "jpeg", "png"));
            if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
            java.io.File f = fc.getSelectedFile();
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() throws Exception {
                    return ApiClient.uploadFile(f);
                }
                @Override protected void done() {
                    try {
                        String raw = get();
                        JsonNode resp = ApiClient.mapper.readTree(raw);
                        String url = resp.path("url").asText("");

                        if (url.isEmpty()) {
                            JOptionPane.showMessageDialog(ProfiloDialog.this,
                                    "Upload fallito: risposta non valida\n" + raw);
                            return;
                        }

                        ApiClient.put("/users/me/picture",
                                "{\"profilePicture\":\"" + url + "\"}");

                        AvatarPanel.setAvatarUrl(url);

                        SwingUtilities.invokeLater(() -> {
                            getContentPane().removeAll();
                            setLayout(new BorderLayout());
                            add(buildHeader((DashboardFrame) getOwner()), BorderLayout.NORTH);
                            add(buildBody(),   BorderLayout.CENTER);
                            add(buildFooter(), BorderLayout.SOUTH);
                            revalidate();
                            repaint();
                            ((DashboardFrame) getOwner()).repaint();
                        });

                        JOptionPane.showMessageDialog(ProfiloDialog.this, "Foto aggiornata! ✅");

                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(ProfiloDialog.this,
                                        "Errore: " + ex.getMessage(),
                                        "Errore", JOptionPane.ERROR_MESSAGE));
                    }
                }
            }.execute();  // ← senza questo lo SwingWorker non parte mai!

        });

        JButton close = styledBtn("✖  Chiudi", new Color(120, 30, 180));
        close.addActionListener(e -> dispose());

        footer.add(changePhoto);
        footer.add(close);
        return footer;
    }

    // ─── HELPER ───────────────────────────────────────────
    private void addRow(JPanel p, GridBagConstraints g, int row, String key, String val) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1;
        JLabel k = new JLabel(key);
        k.setForeground(new Color(220, 200, 255));
        k.setFont(new Font("SansSerif", Font.BOLD, 12));
        p.add(k, g);

        g.gridx = 1;
        JLabel v = new JLabel(val);
        v.setForeground(Color.WHITE);
        v.setFont(new Font("SansSerif", Font.PLAIN, 12));
        p.add(v, g);
    }

    private JButton styledBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private Color getRoleColor(String role) {
        return switch (role) {
            case "ADMIN"         -> new Color(200, 50,  50);
            case "ASSIGNED_USER" -> new Color(40,  140, 200);
            default              -> new Color(80,  160, 80);
        };
    }
}
