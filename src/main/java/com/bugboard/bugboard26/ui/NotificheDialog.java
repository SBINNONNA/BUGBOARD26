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

        // Carica issue assegnate all'utente corrente
        try {
            String resp = ApiClient.get("/issues?assignedTo=" + parent.currentUserId);
            JsonNode issues = ApiClient.mapper.readTree(resp);

            if (!issues.isEmpty()) {
                addNotifCard(listPanel, "📌 Issue assegnate a te", new Color(120, 40, 200));
                for (JsonNode iss : issues) {
                    addNotifCard(listPanel,
                            "  #" + iss.get("id").asText() + " — " + iss.get("title").asText()
                                    + "  [" + iss.get("status").asText() + "]",
                            new Color(155, 100, 215));
                }
            } else {
                addNotifCard(listPanel, "Nessuna issue assegnata a te", new Color(130, 70, 195));
            }

            // Commenti sulle issue assegnate
            addNotifCard(listPanel, "💬 Commenti recenti su tue issue", new Color(120, 40, 200));
            for (JsonNode iss : issues) {
                try {
                    String cResp = ApiClient.get("/issues/" + iss.get("id").asText() + "/comments");
                    JsonNode comments = ApiClient.mapper.readTree(cResp);
                    for (JsonNode c : comments) {
                        addNotifCard(listPanel,
                                "  [#" + iss.get("id").asText() + "] "
                                        + c.path("author").path("email").asText()
                                        + ": " + c.get("text").asText(),
                                new Color(155, 100, 215));
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ex) {
            addNotifCard(listPanel, "Errore: " + ex.getMessage(), new Color(200, 80, 80));
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

    private void addNotifCard(JPanel p, String text, Color bg) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(10, new Color(130, 80, 190)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.add(lbl);
        p.add(Box.createVerticalStrut(6));
    }
}
