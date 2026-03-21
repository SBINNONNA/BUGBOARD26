package com.bugboard.bugboard26.ui;

import com.fasterxml.jackson.databind.JsonNode;
import javax.swing.*;
import java.awt.*;

public class NotificheDialog extends JDialog {

    public NotificheDialog(DashboardFrame parent) {
        super(parent, "Notifiche", true);
        setSize(500, 450);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(new Color(140, 80, 200));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("  🔔 Notifiche", SwingConstants.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setOpaque(true);
        title.setBackground(new Color(90, 0, 160));
        title.setBorder(BorderFactory.createEmptyBorder(14, 15, 14, 15));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(150, 95, 205));
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Long projectId = ApiClient.getCurrentProjectId();

        try {
            String resp = ApiClient.get("/projects/" + projectId + "/issues");
            JsonNode allIssues = ApiClient.mapper.readTree(resp);

            java.util.List<JsonNode> mine = new java.util.ArrayList<>();
            for (JsonNode iss : allIssues) {
                long assignedId = iss.path("assignedTo").path("id").asLong(-1);
                if (assignedId == parent.currentUserId) mine.add(iss);
            }

            if (!mine.isEmpty()) {
                addNotifCard(listPanel, "📌 Issue assegnate a te", new Color(120, 40, 200));
                for (JsonNode iss : mine) {
                    addNotifCard(listPanel,
                            "  #" + iss.get("id").asText() + " — "
                                    + iss.get("title").asText()
                                    + "  [" + iss.get("status").asText() + "]",
                            new Color(155, 100, 215));
                }
            } else {
                addNotifCard(listPanel, "Nessuna issue assegnata a te",
                        new Color(130, 70, 195));
            }

            addNotifCard(listPanel, "💬 Commenti recenti su tue issue",
                    new Color(120, 40, 200));
            boolean anyComment = false;
            for (JsonNode iss : mine) {
                try {
                    String cResp = ApiClient.get("/projects/" + projectId
                            + "/issues/" + iss.get("id").asText() + "/comments");
                    JsonNode comments = ApiClient.mapper.readTree(cResp);
                    for (JsonNode c : comments) {
                        addDismissibleCard(listPanel,
                                "  [#" + iss.get("id").asText() + "] "
                                        + c.path("author").path("email").asText()
                                        + ": " + c.get("text").asText(),
                                new Color(155, 100, 215));
                        anyComment = true;
                    }
                } catch (Exception ignored) {}
            }
            if (!anyComment)
                addNotifCard(listPanel, "Nessun commento recente",
                        new Color(130, 70, 195));

        } catch (Exception ex) {
            addNotifCard(listPanel, "Errore: " + ex.getMessage(),
                    new Color(200, 80, 80));
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.getViewport().setBackground(new Color(150, 95, 205));

        JButton close = new JButton("Chiudi");
        close.setBackground(new Color(85, 0, 155));
        close.setForeground(Color.WHITE);
        close.setFocusPainted(false);
        close.setBorderPainted(false);
        close.addActionListener(e -> dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(new Color(90, 0, 160));
        footer.add(close);

        add(title,  BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    // ── card semplice (solo testo) ─────────────────────────
    private void addNotifCard(JPanel p, String text, Color bg) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(10, new Color(130, 80, 190)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.add(lbl);
        p.add(Box.createVerticalStrut(6));
    }

    // ── card con ✕ che rimuove solo la notifica visivamente ─
    private void addDismissibleCard(JPanel p, String text, Color bg) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(true);
        row.setBackground(bg);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(10, new Color(130, 80, 190)),
                BorderFactory.createEmptyBorder(6, 12, 6, 6)));

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(false);

        JButton del = new JButton("\uD83D\uDDD1");
        del.setFont(new Font("SansSerif", Font.BOLD, 10));
        del.setBackground(new Color(180, 40, 40));
        del.setForeground(Color.WHITE);
        del.setFocusPainted(false);
        del.setBorderPainted(false);
        del.setPreferredSize(new Dimension(28, 24));
        del.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        del.addActionListener(e -> {
            // ← rimuove solo la card visivamente, commento intatto nel DB
            Component[] comps = p.getComponents();
            for (int i = 0; i < comps.length; i++) {
                if (comps[i] == row) {
                    p.remove(row);
                    if (i < p.getComponentCount())
                        p.remove(p.getComponent(i)); // rimuove lo strut
                    break;
                }
            }
            p.revalidate();
            p.repaint();
        });

        row.add(lbl, BorderLayout.CENTER);
        row.add(del, BorderLayout.EAST);
        p.add(row);
        p.add(Box.createVerticalStrut(6));
    }
}
