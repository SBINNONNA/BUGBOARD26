package com.bugboard.bugboard26.ui;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CalendarioDialog extends JDialog {

    private static final DateTimeFormatter FMT_IN  =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FMT_SHOW =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public CalendarioDialog(DashboardFrame parent) {
        super(parent, "📅 Calendario Scadenze", true);
        setSize(560, 520);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(140, 80, 200));

        // ── Header ────────────────────────────────────────
        JLabel title = new JLabel("  📅 Scadenze Issue", SwingConstants.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setOpaque(true);
        title.setBackground(new Color(90, 0, 160));
        title.setBorder(new EmptyBorder(14, 15, 14, 15));
        add(title, BorderLayout.NORTH);

        // ── Lista card ────────────────────────────────────
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(150, 95, 205));
        listPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        loadIssues(parent, listPanel);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.getViewport().setBackground(new Color(150, 95, 205));
        add(scroll, BorderLayout.CENTER);
    }

    // ─── Carica e ordina le issue ──────────────────────────
    private void loadIssues(DashboardFrame parent, JPanel listPanel) {
        try {
            Long projectId = ApiClient.getCurrentProjectId();
            String resp    = ApiClient.get("/projects/" + projectId + "/issues");
            JsonNode all   = ApiClient.mapper.readTree(resp);
            LocalDate today = LocalDate.now();

            List<JsonNode> withDeadline = new ArrayList<>();
            for (JsonNode iss : all) {
                JsonNode dl = iss.path("deadline");
                if (!dl.isNull() && !dl.isMissingNode())
                    withDeadline.add(iss);
            }

            if (withDeadline.isEmpty()) {
                JLabel empty = new JLabel("Nessuna issue con scadenza impostata");
                empty.setForeground(Color.WHITE);
                empty.setFont(new Font("SansSerif", Font.ITALIC, 13));
                empty.setBorder(new EmptyBorder(20, 10, 0, 0));
                listPanel.add(empty);
                return;
            }

            withDeadline.sort(Comparator.comparing(n -> parseDeadline(n.get("deadline"))));

            for (JsonNode iss : withDeadline) {
                LocalDate dl  = parseDeadline(iss.get("deadline"));
                long daysLeft = ChronoUnit.DAYS.between(today, dl);
                listPanel.add(buildCard(iss, dl, daysLeft));
                listPanel.add(Box.createVerticalStrut(8));
            }

        } catch (Exception ex) {
            JLabel err = new JLabel("Errore caricamento: " + ex.getMessage());
            err.setForeground(new Color(255, 100, 100));
            listPanel.add(err);
        }
    }

    // ── Parsing robusto: gestisce array E stringa ──────────
    private LocalDate parseDeadline(JsonNode node) {
        if (node == null || node.isNull()) return LocalDate.MAX;
        if (node.isArray()) {
            return LocalDate.of(
                    node.get(0).asInt(),
                    node.get(1).asInt(),
                    node.get(2).asInt());
        }
        return LocalDate.parse(node.asText().split("T")[0], FMT_IN);
    }

    // ─── Card singola issue ────────────────────────────────
    private JPanel buildCard(JsonNode iss, LocalDate deadline, long daysLeft) {
        Color bg;
        String badge;
        if (daysLeft < 0) {
            bg    = new Color(160, 30, 30);
            badge = "⛔ Scaduta";
        } else if (daysLeft == 0) {
            bg    = new Color(200, 80, 0);
            badge = "🔥 Scade oggi!";
        } else if (daysLeft <= 3) {
            bg    = new Color(190, 100, 0);
            badge = "⚠ " + daysLeft + " giorni";
        } else if (daysLeft <= 7) {
            bg    = new Color(100, 60, 190);
            badge = "📌 " + daysLeft + " giorni";
        } else {
            bg    = new Color(120, 70, 200);
            badge = "📅 " + daysLeft + " giorni";
        }

        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(bg);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(10, bg.darker()),
                new EmptyBorder(8, 14, 8, 14)));

        // ── Info issue (sinistra) ─────────────────────────
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel titleLbl = new JLabel(
                "#" + iss.get("id").asText() + " — " + iss.get("title").asText());
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        titleLbl.setForeground(Color.WHITE);

        String assignee = iss.path("assignedTo").path("email").asText("Non assegnata");
        JLabel subLbl = new JLabel(
                iss.get("status").asText()
                        + "  •  " + iss.get("priority").asText()
                        + "  •  " + assignee);
        subLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subLbl.setForeground(new Color(230, 210, 255));

        info.add(titleLbl);
        info.add(Box.createVerticalStrut(3));
        info.add(subLbl);

        // ── Badge scadenza (destra) ───────────────────────
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);

        JLabel dateLbl = new JLabel(deadline.format(FMT_SHOW), SwingConstants.RIGHT);
        dateLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        dateLbl.setForeground(Color.WHITE);
        dateLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel badgeLbl = new JLabel(badge, SwingConstants.RIGHT);
        badgeLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        badgeLbl.setForeground(new Color(255, 230, 180));
        badgeLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        right.add(dateLbl);
        right.add(Box.createVerticalStrut(3));
        right.add(badgeLbl);

        card.add(info,  BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }
}
