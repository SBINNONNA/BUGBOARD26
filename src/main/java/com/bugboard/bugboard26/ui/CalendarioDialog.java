package com.bugboard.bugboard26.ui;

import com.fasterxml.jackson.databind.JsonNode;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CalendarioDialog extends JDialog {

    public CalendarioDialog(DashboardFrame parent) {
        super(parent, "Calendario Issue", true);
        setSize(560, 480);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(new Color(140, 80, 200));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("  📅 Issue per scadenza", SwingConstants.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setOpaque(true);
        title.setBackground(new Color(90, 0, 160));
        title.setBorder(BorderFactory.createEmptyBorder(14, 15, 14, 15));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(150, 95, 205));
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        try {
            String resp = ApiClient.get("/issues");
            JsonNode arr = ApiClient.mapper.readTree(resp);

            // Ordina per deadline (prima le più vicine, poi quelle senza)
            List<JsonNode> issues = new ArrayList<>();
            arr.forEach(issues::add);
            issues.sort((a, b) -> {
                String da = a.path("deadline").asText("");
                String db = b.path("deadline").asText("");
                if (da.isEmpty() && db.isEmpty()) return 0;
                if (da.isEmpty()) return 1;
                if (db.isEmpty()) return -1;
                return da.compareTo(db);
            });

            if (issues.isEmpty()) {
                addCalCard(listPanel, "Nessuna issue trovata", "", "TODO", new Color(130, 70, 195));
            }

            for (JsonNode iss : issues) {
                String deadline = iss.path("deadline").asText("");
                String label    = deadline.isEmpty() ? "Nessuna scadenza" : "📅 " + deadline.substring(0, 10);
                String status   = iss.get("status").asText();
                Color  bg       = getStatusColor(status);
                addCalCard(listPanel,
                        "#" + iss.get("id").asText() + " — " + iss.get("title").asText(),
                        label, status, bg);
            }
        } catch (Exception ex) {
            addCalCard(listPanel, "Errore: " + ex.getMessage(), "", "", new Color(200, 80, 80));
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

    private void addCalCard(JPanel p, String text, String deadline, String status, Color bg) {
        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(12, new Color(130, 80, 190)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JLabel textLbl = new JLabel(text);
        textLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        textLbl.setForeground(Color.WHITE);

        JLabel dateLbl = new JLabel(deadline);
        dateLbl.setFont(new Font("SansSerif", Font.ITALIC, 11));
        dateLbl.setForeground(new Color(230, 210, 255));

        JLabel statusLbl = new JLabel(status);
        statusLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        statusLbl.setForeground(getStatusTextColor(status));

        JPanel right = new JPanel(new GridLayout(2, 1));
        right.setOpaque(false);
        right.add(dateLbl);
        right.add(statusLbl);

        card.add(textLbl, BorderLayout.CENTER);
        card.add(right,   BorderLayout.EAST);
        p.add(card);
        p.add(Box.createVerticalStrut(6));
    }

    private Color getStatusColor(String s) {
        return switch (s) {
            case "DONE"        -> new Color(50, 140, 80);
            case "IN_PROGRESS" -> new Color(160, 100, 20);
            default            -> new Color(155, 100, 215);
        };
    }

    private Color getStatusTextColor(String s) {
        return switch (s) {
            case "DONE"        -> new Color(150, 255, 170);
            case "IN_PROGRESS" -> new Color(255, 210, 100);
            default            -> new Color(200, 180, 255);
        };
    }
}
