package com.bugboard.bugboard26.ui;

import com.fasterxml.jackson.databind.JsonNode;
import javax.swing.*;
import java.awt.*;

public class NotificheDialog extends JDialog {

    static final java.util.Set<String> dismissedComments = new java.util.HashSet<>();

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
                        String commentId = c.get("id").asText();
                        if (dismissedComments.contains(commentId)) continue;
                        addDismissibleCard(listPanel,
                                "[#" + iss.get("id").asText() + "] "
                                        + c.path("author").path("email").asText()
                                        + ": " + c.get("text").asText(),
                                new Color(155, 100, 215),
                                commentId);
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

        add(title,  BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
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
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT); // ← allineamento sinistro
        p.add(lbl);
        p.add(Box.createVerticalStrut(6));
    }

    // ── card dismissibile con testo wrappato ───────────────
    private void addDismissibleCard(JPanel p, String text, Color bg, String commentId) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(true);
        row.setBackground(bg);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        row.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(10, new Color(130, 80, 190)),
                BorderFactory.createEmptyBorder(8, 12, 8, 8)));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea lbl = new JTextArea(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(Color.WHITE);
        lbl.setBackground(bg);
        lbl.setOpaque(false);
        lbl.setEditable(false);
        lbl.setFocusable(false);
        lbl.setLineWrap(true);
        lbl.setWrapStyleWord(true);
        lbl.setBorder(null);

        JButton del = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(180, 40, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                int m = 7;
                g2.drawLine(m, m, getWidth() - m, getHeight() - m);
                g2.drawLine(getWidth() - m, m, m, getHeight() - m);
                g2.dispose();
            }
        };
        del.setContentAreaFilled(false);
        del.setBorderPainted(false);
        del.setFocusPainted(false);
        del.setPreferredSize(new Dimension(28, 24));
        del.setMinimumSize(new Dimension(28, 24));
        del.setMaximumSize(new Dimension(28, 24));
        del.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // ← pannello wrapper che ancora il bottone in alto a destra
        JPanel delWrapper = new JPanel(new BorderLayout());
        delWrapper.setOpaque(false);
        delWrapper.add(del, BorderLayout.NORTH); // ← NORTH: bottone resta piccolo in alto

        del.addActionListener(e -> {
            dismissedComments.add(commentId);
            Component[] comps = p.getComponents();
            for (int i = 0; i < comps.length; i++) {
                if (comps[i] == row) {
                    p.remove(row);
                    if (i < p.getComponentCount())
                        p.remove(p.getComponent(i));
                    break;
                }
            }
            p.revalidate();
            p.repaint();
        });

        row.add(lbl,        BorderLayout.CENTER);
        row.add(delWrapper, BorderLayout.EAST); // ← wrapper invece del bottone diretto
        p.add(row);
        p.add(Box.createVerticalStrut(6));
    }

}
