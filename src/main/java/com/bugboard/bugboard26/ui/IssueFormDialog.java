package com.bugboard.bugboard26.ui;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import java.awt.*;

public class IssueFormDialog extends JDialog {



    private final Long issueId;
    private final JTextField titleField = new JTextField(25);
    private final JTextArea descArea = new JTextArea(4, 25);
    private final JComboBox<String> typeBox = new JComboBox<>(
            new String[]{"BUG", "QUESTION", "FEATURE", "DOCUMENTATION"}
    );
    private final JComboBox<String> priorityBox = new JComboBox<>(
            new String[]{"P1", "P2", "P3", "P4", "P5"}  // ← AGGIORNATO
    );
    private final JComboBox<String> statusBox = new JComboBox<>(
            new String[]{"TODO", "IN_PROGRESS", "DONE"}
    );
    private final JTextArea commentsArea = new JTextArea(4, 25);
    private final JTextField commentField = new JTextField(20);

    private String issueBase() {
        return "/projects/" + ApiClient.getCurrentProjectId() + "/issues";
    }

    public IssueFormDialog(Frame parent, Long issueId) {
        super(parent, issueId == null ? "Nuova Issue" : "Issue #" + issueId, true);
        this.issueId = issueId;
        setSize(500, 560);
        setLocationRelativeTo(parent);
        buildUI();
        if (issueId != null) loadIssue();
    }

    private void buildUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill = GridBagConstraints.HORIZONTAL;

        addRow(panel, g, 0, "Titolo:", titleField);
        addRow(panel, g, 1, "Tipo:", typeBox);
        addRow(panel, g, 2, "Priorità (1-5):", priorityBox);  // ← label aggiornata
        if (issueId != null) addRow(panel, g, 3, "Stato:", statusBox);

        g.gridx = 0; g.gridy = 4;
        panel.add(new JLabel("Descrizione:"), g);
        g.gridx = 1;
        panel.add(new JScrollPane(descArea), g);

        if (issueId != null) {
            g.gridx = 0; g.gridy = 5;
            panel.add(new JLabel("Commenti:"), g);
            commentsArea.setEditable(false);
            commentsArea.setBackground(new Color(245, 245, 245));
            g.gridx = 1;
            panel.add(new JScrollPane(commentsArea), g);

            g.gridx = 0; g.gridy = 6;
            panel.add(new JLabel("Aggiungi:"), g);
            g.gridx = 1;
            JPanel commentPanel = new JPanel(new BorderLayout(4, 0));
            JButton sendBtn = new JButton("Invia");
            commentPanel.add(commentField, BorderLayout.CENTER);
            commentPanel.add(sendBtn, BorderLayout.EAST);
            panel.add(commentPanel, g);
            sendBtn.addActionListener(e -> sendComment());
        }

        JButton saveBtn = new JButton(issueId == null ? "Crea Issue" : "Salva Modifiche");
        g.gridx = 0; g.gridy = 7; g.gridwidth = 2;
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

    private void loadIssue() {
        try {
            String resp = ApiClient.get(issueBase() + "/" + issueId);
            JsonNode n = ApiClient.mapper.readTree(resp);
            titleField.setText(n.get("title").asText());
            descArea.setText(n.path("description").asText());
            typeBox.setSelectedItem(n.get("type").asText());
            priorityBox.setSelectedItem(n.get("priority").asText()); // es. "P3"
            statusBox.setSelectedItem(n.get("status").asText());
            loadComments();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
        }
    }

    private void loadComments() {
        try {
            String resp = ApiClient.get(issueBase() + "/" + issueId + "/comments");
            JsonNode arr = ApiClient.mapper.readTree(resp);
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

    private void save() {
        String title = titleField.getText().trim();
        String desc  = descArea.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Il titolo è obbligatorio");
            return;
        }
        try {
            if (issueId == null) {
                String json = String.format(
                        "{\"title\":\"%s\",\"description\":\"%s\",\"type\":\"%s\",\"priority\":\"%s\"}",
                        title, desc, typeBox.getSelectedItem(), priorityBox.getSelectedItem()
                );
                ApiClient.postAuth(issueBase(), json);
                JOptionPane.showMessageDialog(this, "Issue creata!");
            } else {
                String json = String.format(
                        "{\"title\":\"%s\",\"description\":\"%s\",\"status\":\"%s\"}",
                        title, desc, statusBox.getSelectedItem()
                );
                ApiClient.put(issueBase() + "/" + issueId, json);
                JOptionPane.showMessageDialog(this, "Issue aggiornata!");
            }
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
        }
    }
}
