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

    private final JTextField        titleField    = new JTextField(25);
    private final JTextArea         descArea      = new JTextArea(4, 25);
    private final JComboBox<String> typeBox       = new JComboBox<>(
            new String[]{"BUG", "QUESTION", "FEATURE", "DOCUMENTATION"});
    private final JComboBox<String> priorityBox   = new JComboBox<>(
            new String[]{"P1", "P2", "P3", "P4", "P5"});
    private final JTextArea         commentsArea  = new JTextArea(4, 25);
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
        this.issueId = issueId;
        this.parent  = parent;
        this.isAdmin = "ADMIN".equals(parent.currentUserRole);
        setSize(520, issueId == null ? 540 : 700);
        setLocationRelativeTo(parent);
        if (isAdmin) loadUsers();
        buildUI();
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

        JPanel imgPanel = new JPanel(new BorderLayout(8, 0));
        imgPanel.setOpaque(false);
        imgPanel.add(pickImg,      BorderLayout.WEST);
        imgPanel.add(imagePreview, BorderLayout.CENTER);

        g.gridx = 0; g.gridy = row;
        panel.add(new JLabel("Immagine:"), g);
        g.gridx = 1;
        panel.add(imgPanel, g);
        row++;
        // ─────────────────────────────────────────────────

        if (issueId != null) {
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

            // ── Immagine allegata (visualizza se esiste) ──
            g.gridx = 0; g.gridy = row;
            panel.add(new JLabel("Allegato:"), g);
            JLabel existingImg = new JLabel("Nessuna immagine allegata");
            existingImg.setForeground(new Color(130, 80, 180));
            existingImg.setFont(new Font("SansSerif", Font.ITALIC, 11));
            existingImg.setName("existingImg"); // per loadIssue
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

        JButton saveBtn = new JButton(issueId == null ? "Crea Issue" : "Salva Modifiche");
        saveBtn.setBackground(new Color(85, 0, 155));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        g.gridx = 0; g.gridy = row; g.gridwidth = 2;
        panel.add(saveBtn, g);
        saveBtn.addActionListener(e -> save());

        add(new JScrollPane(panel));
    }

    // ─── Selezione e upload immagine ──────────────────────
    private void pickImage() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Immagini", "jpg", "jpeg", "png", "gif"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File f = fc.getSelectedFile();

        // mostra preview subito
        ImageIcon icon = new ImageIcon(
                new ImageIcon(f.getAbsolutePath()).getImage()
                        .getScaledInstance(80, 60, Image.SCALE_SMOOTH));
        imagePreview.setIcon(icon);
        imagePreview.setText("");

        // carica in background
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

            // ── Mostra immagine esistente ──
            String imgUrl = n.path("imageUrl").asText("");
            if (!imgUrl.isEmpty()) {
                selectedImageUrl = imgUrl;

                // mostra preview nell'imagePreview già presente nel form
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
                                // click → apre immagine a schermo intero
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

    // cerca ricorsivamente la JLabel "existingImg" e aggiorna il testo
    private void findExistingImgLabel(Container container, String url) {
        for (Component c : container.getComponents()) {
            if (c instanceof JLabel lbl && "existingImg".equals(lbl.getName())) {
                lbl.setText("<html><a href=''>" + url + "</a></html>");
                lbl.setToolTipText(url);
                return;
            }
            if (c instanceof Container inner) findExistingImgLabel(inner, url);
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
                StringBuilder json = new StringBuilder();
                json.append(String.format(
                        "{\"title\":\"%s\",\"description\":\"%s\",\"type\":\"%s\",\"priority\":\"%s\"",
                        title, desc,
                        typeBox.getSelectedItem(),
                        priorityBox.getSelectedItem()));

                // immagine allegata
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
                // ── MODIFICA ──
                StringBuilder jsonUpdate = new StringBuilder();
                jsonUpdate.append(String.format(
                        "{\"title\":\"%s\",\"description\":\"%s\"", title, desc));

                if (selectedImageUrl != null && !selectedImageUrl.isEmpty())
                    jsonUpdate.append(",\"imageUrl\":\"").append(selectedImageUrl).append("\"");

                if (isAdmin) {
                    String dl = parseDeadline(deadlineField.getText().trim());
                    if (dl != null) jsonUpdate.append(",\"deadline\":\"").append(dl).append("\"");
                }
                jsonUpdate.append("}");

                ApiClient.put(issueBase() + "/" + issueId, jsonUpdate.toString());

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

    private String parseDeadline(String input) {
        if (input.isEmpty() || input.equals("gg/mm/aaaa")) return null;
        try {
            String[] p = input.split("/");
            if (p.length != 3) return null;
            return p[2] + "-" + p[1] + "-" + p[0];
        } catch (Exception e) {
            return null;
        }
    }
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
                    // scala mantenendo proporzioni
                    int maxW = 660, maxH = 440;
                    double scale = Math.min((double) maxW / img.getWidth(),
                            (double) maxH / img.getHeight());
                    int w = (int)(img.getWidth()  * scale);
                    int h = (int)(img.getHeight() * scale);
                    ImageIcon icon = new ImageIcon(
                            img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
                    SwingUtilities.invokeLater(() -> {
                        viewer.remove(loading);
                        JLabel imgLbl = new JLabel(icon, SwingConstants.CENTER);
                        viewer.add(imgLbl, BorderLayout.CENTER);

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
                SwingUtilities.invokeLater(() -> {
                    loading.setText("Errore caricamento immagine");
                });
            }
        }).start();

        viewer.setVisible(true);
    }

}
