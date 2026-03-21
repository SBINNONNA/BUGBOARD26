package com.bugboard.bugboard26.ui;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class IssueFormDialog extends JDialog {

    private final Long issueId;
    private final boolean isAdmin;
    private final DashboardFrame parent;
    private JButton doneBtn = null;
    private String selectedImageUrl = null;
    private JLabel imagePreview;

    // ← commentsPanel sostituisce la vecchia JTextArea
    private JPanel commentsPanel;

    private final JTextField        titleField    = new JTextField(25);
    private final JTextArea         descArea      = new JTextArea(4, 25);
    private final JComboBox<String> typeBox       = new JComboBox<>(
            new String[]{"BUG", "QUESTION", "FEATURE", "DOCUMENTATION"});
    private final JComboBox<String> priorityBox   = new JComboBox<>(
            new String[]{"P1", "P2", "P3", "P4", "P5"});
    private final JTextField        commentField  = new JTextField(20);
    private final JTextField        deadlineField = new JTextField("gg/mm/aaaa", 15);
    private final JComboBox<UserEntry> assigneeBox = new JComboBox<>();
    private final List<UserEntry>      userList    = new ArrayList<>();

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
        this.issueId  = issueId;
        this.parent   = parent;
        this.isAdmin  = "ADMIN".equals(parent.currentUserRole);
        setSize(520, issueId == null ? 540 : 750);
        setLocationRelativeTo(parent);
        if (isAdmin) loadUsers();
        buildUI();

        // ── Campi in sola lettura per utenti normali su issue esistenti ──
        if (issueId != null && !isAdmin) {
            titleField.setEditable(false);
            titleField.setBackground(new Color(235, 225, 250));
            descArea.setEditable(false);
            descArea.setBackground(new Color(235, 225, 250));
            typeBox.setEnabled(false);
            priorityBox.setEnabled(false);
        }

        if (issueId != null) loadIssue();
    }

    // ─── Carica lista utenti ───────────────────────────────
    private void loadUsers() {
        try {
            String resp = ApiClient.get("/users");
            JsonNode arr = ApiClient.mapper.readTree(resp);
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
        addRow(panel, g, row++, "Titolo:",         titleField);
        addRow(panel, g, row++, "Tipo:",            typeBox);
        addRow(panel, g, row++, "Priorità (1-5):", priorityBox);

        if (isAdmin) {
            addRow(panel, g, row++, "Assegna a:", assigneeBox);
            addRow(panel, g, row++, "Scadenza:",  deadlineField);
        }

        g.gridx = 0; g.gridy = row;
        panel.add(new JLabel("Descrizione:"), g);
        g.gridx = 1;
        panel.add(new JScrollPane(descArea), g);
        row++;

        // ── Sezione immagine ──────────────────────────────
        imagePreview = new JLabel("Nessuna immagine", SwingConstants.CENTER);
        imagePreview.setPreferredSize(new Dimension(80, 60));
        imagePreview.setForeground(new Color(130, 80, 180));
        imagePreview.setFont(new Font("SansSerif", Font.ITALIC, 11));
        imagePreview.setBorder(BorderFactory.createLineBorder(new Color(160, 110, 220)));

        JButton pickImg = new JButton("📎 Allega immagine");
        pickImg.setBackground(new Color(85, 0, 155));
        pickImg.setForeground(Color.WHITE);
        pickImg.setFocusPainted(false);
        pickImg.setBorderPainted(false);
        pickImg.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pickImg.addActionListener(e -> pickImage());

        if (issueId != null && !isAdmin) pickImg.setVisible(false);

        JPanel imgPanel = new JPanel(new BorderLayout(8, 0));
        imgPanel.setOpaque(false);
        imgPanel.add(pickImg,      BorderLayout.WEST);
        imgPanel.add(imagePreview, BorderLayout.CENTER);

        g.gridx = 0; g.gridy = row;
        panel.add(new JLabel("Immagine:"), g);
        g.gridx = 1;
        panel.add(imgPanel, g);
        row++;

        if (issueId != null) {

            // ── Sezione commenti come pannello card ────────
            g.gridx = 0; g.gridy = row;
            panel.add(new JLabel("Commenti:"), g);

            commentsPanel = new JPanel();
            commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
            commentsPanel.setBackground(new Color(245, 242, 255));

            JScrollPane commentsScroll = new JScrollPane(commentsPanel);
            commentsScroll.setPreferredSize(new Dimension(350, 130));
            commentsScroll.setBorder(BorderFactory.createLineBorder(new Color(160, 110, 220)));
            g.gridx = 1;
            panel.add(commentsScroll, g);
            row++;

            // ── Campo aggiungi commento ────────────────────
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

            // ── Allegato ──────────────────────────────────
            g.gridx = 0; g.gridy = row;
            panel.add(new JLabel("Allegato:"), g);
            JLabel existingImg = new JLabel("Nessuna immagine allegata");
            existingImg.setForeground(new Color(130, 80, 180));
            existingImg.setFont(new Font("SansSerif", Font.ITALIC, 11));
            existingImg.setName("existingImg");
            g.gridx = 1;
            panel.add(existingImg, g);
            row++;

            // ── Bottone "Segna come Completata" ──
            doneBtn = new JButton("✅ Segna come Completata");
            doneBtn.setBackground(new Color(40, 160, 60));
            doneBtn.setForeground(Color.WHITE);
            doneBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
            doneBtn.setFocusPainted(false);
            doneBtn.setBorderPainted(false);
            doneBtn.setVisible(false);
            doneBtn.addActionListener(e -> {
                try {
                    ApiClient.patch(issueBase() + "/" + issueId + "/complete", "{}");
                    JOptionPane.showMessageDialog(this, "Issue completata! ✅");
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
                }
            });
            g.gridx = 0; g.gridy = row; g.gridwidth = 2;
            panel.add(doneBtn, g);
            row++;
        }

        // ── "Crea Issue" a tutti, "Salva Modifiche" solo ADMIN ──
        if ((issueId == null) || isAdmin) {
            JButton saveBtn = new JButton(issueId == null ? "Crea Issue" : "Salva Modifiche");
            saveBtn.setBackground(new Color(85, 0, 155));
            saveBtn.setForeground(Color.WHITE);
            saveBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
            saveBtn.setFocusPainted(false);
            saveBtn.setBorderPainted(false);
            g.gridx = 0; g.gridy = row; g.gridwidth = 2;
            panel.add(saveBtn, g);
            saveBtn.addActionListener(e -> save());
        }

        add(new JScrollPane(panel));
    }

    // ─── Selezione e upload immagine ──────────────────────
    private void pickImage() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Immagini", "jpg", "jpeg", "png", "gif"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File f = fc.getSelectedFile();
        ImageIcon icon = new ImageIcon(
                new ImageIcon(f.getAbsolutePath()).getImage()
                        .getScaledInstance(80, 60, Image.SCALE_SMOOTH));
        imagePreview.setIcon(icon);
        imagePreview.setText("");

        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                return ApiClient.uploadFile(f);
            }
            @Override protected void done() {
                try {
                    JsonNode resp = ApiClient.mapper.readTree(get());
                    selectedImageUrl = resp.get("url").asText();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(IssueFormDialog.this,
                            "Errore upload immagine: " + ex.getMessage());
                    imagePreview.setIcon(null);
                    imagePreview.setText("Errore upload");
                }
            }
        }.execute();
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

            if (!n.path("deadline").isNull() && !n.path("deadline").isMissingNode()) {
                String dl = n.get("deadline").asText("");
                if (!dl.isEmpty()) {
                    String[] parts = dl.split("T")[0].split("-");
                    if (parts.length == 3)
                        deadlineField.setText(parts[2] + "/" + parts[1] + "/" + parts[0]);
                }
            }

            if (isAdmin && n.has("assignedTo") && !n.get("assignedTo").isNull()) {
                long assignedId = n.get("assignedTo").get("id").asLong();
                for (int i = 0; i < assigneeBox.getItemCount(); i++) {
                    if (assigneeBox.getItemAt(i).id == assignedId) {
                        assigneeBox.setSelectedIndex(i);
                        break;
                    }
                }
            }

            String imgUrl = n.path("imageUrl").asText("");
            if (!imgUrl.isEmpty()) {
                selectedImageUrl = imgUrl;
                new Thread(() -> {
                    try {
                        java.awt.image.BufferedImage img =
                                javax.imageio.ImageIO.read(new java.net.URL(imgUrl));
                        if (img != null) {
                            ImageIcon icon = new ImageIcon(
                                    img.getScaledInstance(80, 60, Image.SCALE_SMOOTH));
                            SwingUtilities.invokeLater(() -> {
                                imagePreview.setIcon(icon);
                                imagePreview.setText("");
                                imagePreview.setToolTipText(imgUrl);
                                imagePreview.setCursor(
                                        Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                imagePreview.addMouseListener(new java.awt.event.MouseAdapter() {
                                    public void mouseClicked(java.awt.event.MouseEvent e) {
                                        showFullImage(imgUrl);
                                    }
                                });
                            });
                        }
                    } catch (Exception ignored) {}
                }).start();
            }

            if (doneBtn != null) {
                boolean isDone     = "DONE".equals(n.get("status").asText());
                boolean isAssignee = !n.path("assignedTo").isNull()
                        && n.path("assignedTo").path("id").asLong() == parent.currentUserId;
                doneBtn.setVisible(!isDone && (isAdmin || isAssignee));
            }

            loadComments();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore caricamento: " + ex.getMessage());
        }
    }

    // ─── Carica commenti come card ─────────────────────────
    private void loadComments() {
        try {
            String   resp = ApiClient.get(issueBase() + "/" + issueId + "/comments");
            JsonNode arr  = ApiClient.mapper.readTree(resp);

            commentsPanel.removeAll();

            if (!arr.isArray() || arr.size() == 0) {
                JLabel empty = new JLabel("  Nessun commento");
                empty.setFont(new Font("SansSerif", Font.ITALIC, 11));
                empty.setForeground(Color.GRAY);
                commentsPanel.add(empty);
            }

            for (JsonNode c : arr) {
                long   authorId    = c.path("author").path("id").asLong(-1);
                String authorEmail = c.path("author").path("email").asText("?");
                String commentText = c.get("text").asText();
                long   commentId   = c.get("id").asLong();

                boolean isOwner = (authorId == parent.currentUserId);
                boolean canEdit = isOwner;                    // solo l'autore modifica
                boolean canDelete = isOwner || isAdmin;       // autore o admin cancella

                // ── card del commento ──────────────────────
                JPanel card = new JPanel(new BorderLayout(6, 0));
                card.setBackground(new Color(235, 225, 255));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(180, 140, 220)),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)));
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

                JLabel textLbl = new JLabel(
                        "<html><b>" + authorEmail + "</b>: " + commentText + "</html>");
                textLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
                card.add(textLbl, BorderLayout.CENTER);

                // ── bottoni azione ────────────────────────
                JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
                actions.setOpaque(false);

                if (canEdit) {
                    JButton editBtn = smallBtn("✏", new Color(100, 60, 200));
                    editBtn.setToolTipText("Modifica commento");
                    editBtn.addActionListener(e ->
                            editComment(commentId, commentText, textLbl, authorEmail));
                    actions.add(editBtn);
                }

                if (canDelete) {
                    JButton delBtn = smallBtn("🗑", new Color(180, 40, 40));
                    delBtn.setToolTipText("Elimina commento");
                    delBtn.addActionListener(e -> deleteComment(commentId, card));
                    actions.add(delBtn);
                }

                card.add(actions, BorderLayout.EAST);
                commentsPanel.add(card);
                commentsPanel.add(Box.createVerticalStrut(4));
            }

            commentsPanel.revalidate();
            commentsPanel.repaint();

        } catch (Exception ex) {
            JLabel err = new JLabel("  Errore caricamento commenti");
            err.setForeground(Color.RED);
            commentsPanel.add(err);
        }
    }

    // ─── Helper bottone piccolo ────────────────────────────
    private JButton smallBtn(String icon, Color bg) {
        JButton btn = new JButton(icon);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(30, 24));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ─── Modifica commento ─────────────────────────────────
    private void editComment(long commentId, String oldText,
                             JLabel textLbl, String authorEmail) {
        String newText = (String) JOptionPane.showInputDialog(
                this, "Modifica il commento:",
                "Modifica commento", JOptionPane.PLAIN_MESSAGE,
                null, null, oldText);

        if (newText == null || newText.trim().isEmpty()) return;
        newText = newText.trim();

        try {
            // ← usa il mapper invece di String.format: gestisce ", \n, \ ecc.
            String json = ApiClient.mapper.writeValueAsString(
                    java.util.Map.of("text", newText));

            ApiClient.put(issueBase() + "/" + issueId + "/comments/" + commentId, json);

            final String finalText = newText;
            textLbl.setText("<html><b>" + authorEmail + "</b>: " + finalText + "</html>");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore modifica: " + ex.getMessage());
        }
    }


    // ─── Elimina commento ──────────────────────────────────
    private void deleteComment(long commentId, JPanel card) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Eliminare questo commento?",
                "Conferma eliminazione",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            ApiClient.delete(issueBase() + "/" + issueId + "/comments/" + commentId);
            // rimuove la card + il relativo strut
            Container parent = card.getParent();
            Component[] comps = parent.getComponents();
            for (int i = 0; i < comps.length; i++) {
                if (comps[i] == card) {
                    parent.remove(card);
                    if (i < parent.getComponentCount())
                        parent.remove(parent.getComponent(i));
                    break;
                }
            }
            commentsPanel.revalidate();
            commentsPanel.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore eliminazione: " + ex.getMessage());
        }
    }

    // ─── Invia commento ────────────────────────────────────
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
                StringBuilder json = new StringBuilder();
                json.append(String.format(
                        "{\"title\":\"%s\",\"description\":\"%s\",\"type\":\"%s\",\"priority\":\"%s\"",
                        title, desc,
                        typeBox.getSelectedItem(),
                        priorityBox.getSelectedItem()));

                if (selectedImageUrl != null && !selectedImageUrl.isEmpty())
                    json.append(",\"imageUrl\":\"").append(selectedImageUrl).append("\"");

                if (isAdmin) {
                    UserEntry selected = (UserEntry) assigneeBox.getSelectedItem();
                    if (selected != null && selected.id > 0)
                        json.append(",\"assignedToId\":\"").append(selected.id).append("\"");
                    String dl = parseDeadline(deadlineField.getText().trim());
                    if (dl != null) json.append(",\"deadline\":\"").append(dl).append("\"");
                }
                json.append("}");
                ApiClient.postAuth(issueBase(), json.toString());
                JOptionPane.showMessageDialog(this, "Issue creata!");

            } else {
                StringBuilder jsonUpdate = new StringBuilder();
                jsonUpdate.append(String.format(
                        "{\"title\":\"%s\",\"description\":\"%s\"", title, desc));

                if (selectedImageUrl != null && !selectedImageUrl.isEmpty())
                    jsonUpdate.append(",\"imageUrl\":\"").append(selectedImageUrl).append("\"");

                String dl = parseDeadline(deadlineField.getText().trim());
                if (dl != null) jsonUpdate.append(",\"deadline\":\"").append(dl).append("\"");
                jsonUpdate.append("}");

                ApiClient.put(issueBase() + "/" + issueId, jsonUpdate.toString());

                UserEntry selected = (UserEntry) assigneeBox.getSelectedItem();
                if (selected != null && selected.id > 0) {
                    String jsonAssign = String.format("{\"userId\":\"%d\"}", selected.id);
                    ApiClient.patch(issueBase() + "/" + issueId + "/assign", jsonAssign);
                }
                JOptionPane.showMessageDialog(this, "Issue aggiornata!");
            }
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
        }
    }

    private String parseDeadline(String input) {
        if (input.isEmpty() || input.equals("gg/mm/aaaa")) return null;
        try {
            String[] p = input.split("/");
            if (p.length != 3) return null;
            return p[2] + "-" + p[1] + "-" + p[0];
        } catch (Exception e) { return null; }
    }

    // ─── Visualizzatore immagine a schermo intero ──────────
    private void showFullImage(String imgUrl) {
        JDialog viewer = new JDialog(this, "Immagine allegata", true);
        viewer.setSize(700, 500);
        viewer.setLocationRelativeTo(this);
        viewer.getContentPane().setBackground(new Color(30, 0, 60));
        viewer.setLayout(new BorderLayout());

        JLabel loading = new JLabel("Caricamento...", SwingConstants.CENTER);
        loading.setForeground(Color.WHITE);
        viewer.add(loading, BorderLayout.CENTER);

        new Thread(() -> {
            try {
                java.awt.image.BufferedImage img =
                        javax.imageio.ImageIO.read(new java.net.URL(imgUrl));
                if (img != null) {
                    int maxW = 660, maxH = 440;
                    double scale = Math.min((double) maxW / img.getWidth(),
                            (double) maxH / img.getHeight());
                    int w = (int)(img.getWidth()  * scale);
                    int h = (int)(img.getHeight() * scale);
                    ImageIcon icon = new ImageIcon(
                            img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
                    SwingUtilities.invokeLater(() -> {
                        viewer.remove(loading);
                        viewer.add(new JLabel(icon, SwingConstants.CENTER), BorderLayout.CENTER);
                        JButton close = new JButton("✖ Chiudi");
                        close.setBackground(new Color(85, 0, 155));
                        close.setForeground(Color.WHITE);
                        close.setFocusPainted(false);
                        close.setBorderPainted(false);
                        close.addActionListener(ev -> viewer.dispose());
                        JPanel footer = new JPanel();
                        footer.setBackground(new Color(50, 0, 90));
                        footer.add(close);
                        viewer.add(footer, BorderLayout.SOUTH);
                        viewer.revalidate();
                        viewer.repaint();
                    });
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> loading.setText("Errore caricamento immagine"));
            }
        }).start();

        viewer.setVisible(true);
    }
}
