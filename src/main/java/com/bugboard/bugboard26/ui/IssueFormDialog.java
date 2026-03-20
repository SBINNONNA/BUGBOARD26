package com.bugboard.bugboard26.ui;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class IssueFormDialog extends JDialog {

    private final Long issueId;
    private final boolean isAdmin;

    private final JTextField    titleField   = new JTextField(25);
    private final JTextArea     descArea     = new JTextArea(4, 25);
    private final JComboBox<String> typeBox  = new JComboBox<>(
            new String[]{"BUG", "QUESTION", "FEATURE", "DOCUMENTATION"});
    private final JComboBox<String> priorityBox = new JComboBox<>(
            new String[]{"P1", "P2", "P3", "P4", "P5"});
    private final JComboBox<String> statusBox = new JComboBox<>(
            new String[]{"TODO", "IN_PROGRESS", "DONE"});
    private final JTextArea   commentsArea = new JTextArea(4, 25);
    private final JTextField  commentField = new JTextField(20);

    // ✅ Dropdown assegnatari — visibile solo agli admin
    private final JComboBox<UserEntry> assigneeBox = new JComboBox<>();
    private final List<UserEntry> userList = new ArrayList<>();

    // Modello interno per utenti nel combo
    private static class UserEntry {
        final long   id;
        final String email;
        UserEntry(long id, String email) { this.id = id; this.email = email; }
        @Override public String toString() { return email; }
    }

    private String issueBase() {
        return "/projects/" + ApiClient.getCurrentProjectId() + "/issues";
    }

    public IssueFormDialog(DashboardFrame parent, Long issueId) {
        super(parent, issueId == null ? "Nuova Issue" : "Issue #" + issueId, true);
        this.issueId = issueId;
        this.isAdmin = "ADMIN".equals(parent.currentUserRole);
        setSize(520, issueId == null ? 480 : 620);
        setLocationRelativeTo(parent);
        if (isAdmin) loadUsers();   // carica utenti prima di buildUI
        buildUI();
        if (issueId != null) loadIssue();
    }

    // ─── Carica lista utenti dal backend ───────────────────
    private void loadUsers() {
        try {
            String resp = ApiClient.get("/users");
            JsonNode arr = ApiClient.mapper.readTree(resp);
            // Opzione "nessuno" (rimuove assegnazione)
            assigneeBox.addItem(new UserEntry(-1L, "— Nessuno —"));
            for (JsonNode u : arr) {
                UserEntry entry = new UserEntry(
                        u.get("id").asLong(),
                        u.get("email").asText());
                userList.add(entry);
                assigneeBox.addItem(entry);
            }
        } catch (Exception ex) {
            assigneeBox.addItem(new UserEntry(-1L, "Errore caricamento utenti"));
        }
    }

    // ─── Costruzione UI ────────────────────────────────────
    private void buildUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill   = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(panel, g, row++, "Titolo:",    titleField);
        addRow(panel, g, row++, "Tipo:",      typeBox);
        addRow(panel, g, row++, "Priorità (1-5):", priorityBox);

        if (issueId != null) {
            // In modifica: mostra stato
            addRow(panel, g, row++, "Stato:", statusBox);
        }

        // ✅ Riga assegnazione — visibile solo agli admin
        if (isAdmin) {
            addRow(panel, g, row++, "Assegna a:", assigneeBox);
        }

        g.gridx = 0; g.gridy = row;
        panel.add(new JLabel("Descrizione:"), g);
        g.gridx = 1;
        panel.add(new JScrollPane(descArea), g);
        row++;

        if (issueId != null) {
            // Sezione commenti (solo in modifica)
            g.gridx = 0; g.gridy = row;
            panel.add(new JLabel("Commenti:"), g);
            commentsArea.setEditable(false);
            commentsArea.setBackground(new Color(245, 245, 245));
            g.gridx = 1;
            panel.add(new JScrollPane(commentsArea), g);
            row++;

            g.gridx = 0; g.gridy = row;
            panel.add(new JLabel("Aggiungi:"), g);
            g.gridx = 1;
            JPanel commentPanel = new JPanel(new BorderLayout(4, 0));
            JButton sendBtn = new JButton("Invia");
            commentPanel.add(commentField, BorderLayout.CENTER);
            commentPanel.add(sendBtn,      BorderLayout.EAST);
            panel.add(commentPanel, g);
            sendBtn.addActionListener(e -> sendComment());
            row++;
        }

        JButton saveBtn = new JButton(issueId == null ? "Crea Issue" : "Salva Modifiche");
        saveBtn.setBackground(new Color(85, 0, 155));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.gridx = 0; g.gridy = row; g.gridwidth = 2;
        panel.add(saveBtn, g);
        saveBtn.addActionListener(e -> save());

        add(new JScrollPane(panel));
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent comp) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1;
        p.add(new JLabel(label), g);
        g.gridx = 1;
        p.add(comp, g);
    }

    // ─── Carica dati issue esistente ───────────────────────
    private void loadIssue() {
        try {
            String   resp = ApiClient.get(issueBase() + "/" + issueId);
            JsonNode n    = ApiClient.mapper.readTree(resp);
            titleField.setText(n.get("title").asText());
            descArea.setText(n.path("description").asText());
            typeBox.setSelectedItem(n.get("type").asText());
            priorityBox.setSelectedItem(n.get("priority").asText());
            statusBox.setSelectedItem(n.get("status").asText());

            // ✅ Pre-seleziona l'assegnatario attuale se admin
            if (isAdmin && n.has("assignedTo") && !n.get("assignedTo").isNull()) {
                long assignedId = n.get("assignedTo").get("id").asLong();
                for (int i = 0; i < assigneeBox.getItemCount(); i++) {
                    if (assigneeBox.getItemAt(i).id == assignedId) {
                        assigneeBox.setSelectedIndex(i);
                        break;
                    }
                }
            }

            loadComments();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore caricamento: " + ex.getMessage());
        }
    }

    // ─── Commenti ──────────────────────────────────────────
    private void loadComments() {
        try {
            String   resp = ApiClient.get(issueBase() + "/" + issueId + "/comments");
            JsonNode arr  = ApiClient.mapper.readTree(resp);
            StringBuilder sb = new StringBuilder();
            for (JsonNode c : arr) {
                sb.append("[").append(c.path("author").path("email").asText()).append("] ");
                sb.append(c.get("text").asText()).append("\n");
            }
            commentsArea.setText(sb.toString());
        } catch (Exception ex) {
            commentsArea.setText("Errore caricamento commenti");
        }
    }

    private void sendComment() {
        String text = commentField.getText().trim();
        if (text.isEmpty()) return;
        try {
            String json = String.format("{\"text\":\"%s\"}", text);
            ApiClient.postAuth(issueBase() + "/" + issueId + "/comments", json);
            commentField.setText("");
            loadComments();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
        }
    }

    // ─── Salvataggio ───────────────────────────────────────
    private void save() {
        String title = titleField.getText().trim();
        String desc  = descArea.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Il titolo è obbligatorio");
            return;
        }

        try {
            if (issueId == null) {
                // ── CREAZIONE ──
                // Costruisce il JSON base
                StringBuilder json = new StringBuilder();
                json.append(String.format(
                        "{\"title\":\"%s\",\"description\":\"%s\",\"type\":\"%s\",\"priority\":\"%s\"",
                        title, desc,
                        typeBox.getSelectedItem(),
                        priorityBox.getSelectedItem()));

                // ✅ Admin: aggiunge assignedToId se selezionato
                if (isAdmin) {
                    UserEntry selected = (UserEntry) assigneeBox.getSelectedItem();
                    if (selected != null && selected.id > 0) {
                        json.append(",\"assignedToId\":\"").append(selected.id).append("\"");
                    }
                }
                json.append("}");

                ApiClient.postAuth(issueBase(), json.toString());
                JOptionPane.showMessageDialog(this, "Issue creata!");

            } else {
                // ── MODIFICA ──
                // 1. Aggiorna titolo, descrizione e stato
                String jsonUpdate = String.format(
                        "{\"title\":\"%s\",\"description\":\"%s\",\"status\":\"%s\"}",
                        title, desc, statusBox.getSelectedItem());
                ApiClient.put(issueBase() + "/" + issueId, jsonUpdate);

                // ✅ 2. Admin: aggiorna assegnazione se cambiata
                if (isAdmin) {
                    UserEntry selected = (UserEntry) assigneeBox.getSelectedItem();
                    if (selected != null && selected.id > 0) {
                        String jsonAssign = String.format("{\"userId\":\"%d\"}", selected.id);
                        ApiClient.patch(issueBase() + "/" + issueId + "/assign", jsonAssign);
                    }
                }

                JOptionPane.showMessageDialog(this, "Issue aggiornata!");
            }
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
        }
    }
}
