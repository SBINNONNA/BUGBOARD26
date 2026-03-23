package com.bugboard.bugboard26.ui;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class IssueFormDialog extends JDialog {

    private static final Color BG           = new Color(140, 80, 200);
    private static final Color BG_PANEL     = new Color(150, 95, 205);
    private static final Color BG_CARD      = new Color(235, 225, 255);
    private static final Color HEADER_BG    = new Color(90, 0, 160);
    private static final Color BTN_PRIMARY  = new Color(85, 0, 155);
    private static final Color BTN_GREEN    = new Color(40, 160, 60);
    private static final Color BTN_RED      = new Color(180, 40, 40);
    private static final Color BTN_EDIT     = new Color(100, 60, 200);
    private static final Color FIELD_BG     = new Color(245, 240, 255);
    private static final Color FIELD_BORDER = new Color(160, 110, 220);
    private static final Color LABEL_FG     = Color.WHITE;
    private static final Color COMMENT_BG   = new Color(245, 242, 255);

    private final Long issueId;
    private final boolean isAdmin;
    private final DashboardFrame parent;
    private JButton doneBtn        = null;
    private JButton deleteIssueBtn = null;
    private String selectedImageUrl = null;
    private JLabel imagePreview;
    private JPanel commentsPanel;

    private final JTextField           titleField    = styledField(new JTextField(25));
    private final JTextArea            descArea      = styledArea(new JTextArea(4, 25));
    private final JComboBox<String>    typeBox       = styledCombo(new JComboBox<>(
            new String[]{"BUG", "QUESTION", "FEATURE", "DOCUMENTATION"}));
    private final JComboBox<String>    priorityBox   = styledCombo(new JComboBox<>(
            new String[]{"P1", "P2", "P3", "P4", "P5"}));
    private final JTextField           commentField  = styledField(new JTextField(20));
    private final JTextField           deadlineField = styledField(new JTextField("gg/mm/aaaa", 15));
    private final JComboBox<UserEntry> assigneeBox   = styledCombo(new JComboBox<>());
    private final List<UserEntry>      userList      = new ArrayList<>();

    private static class UserEntry {
        final long id; final String email;
        UserEntry(long id, String email) { this.id = id; this.email = email; }
        @Override public String toString() { return email; }
    }

    private String issueBase() {
        return "/projects/" + ApiClient.getCurrentProjectId() + "/issues";
    }

    private static JTextField styledField(JTextField f) {
        f.setBackground(FIELD_BG);
        f.setForeground(new Color(60, 0, 120));
        f.setFont(new Font("SansSerif", Font.PLAIN, 12));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return f;
    }

    private static JTextArea styledArea(JTextArea a) {
        a.setBackground(FIELD_BG);
        a.setForeground(new Color(60, 0, 120));
        a.setFont(new Font("SansSerif", Font.PLAIN, 12));
        a.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        return a;
    }

    private static <T> JComboBox<T> styledCombo(JComboBox<T> cb) {
        cb.setBackground(FIELD_BG);
        cb.setForeground(new Color(60, 0, 120));
        cb.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return cb;
    }

    private JButton styledBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Bottone modifica commento (penna disegnata) ────────
    private JButton editCommentBtn() {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BTN_EDIT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                int cx = getWidth() / 2, cy = getHeight() / 2;
                // corpo penna
                g2.drawLine(cx - 5, cy + 4, cx + 4, cy - 5);
                g2.drawLine(cx - 3, cy + 6, cx + 6, cy - 3);
                g2.drawLine(cx + 4, cy - 5, cx + 6, cy - 3);
                g2.drawLine(cx - 5, cy + 4, cx - 3, cy + 6);
                // punta
                g2.drawLine(cx - 3, cy + 6, cx - 5, cy + 7);
                g2.drawLine(cx - 5, cy + 4, cx - 5, cy + 7);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(28, 24));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Bottone elimina commento (X disegnata) ─────────────
    private JButton deleteCommentBtn() {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BTN_RED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                int m = 7;
                g2.drawLine(m, m, getWidth() - m, getHeight() - m);
                g2.drawLine(getWidth() - m, m, m, getHeight() - m);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(28, 24));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public IssueFormDialog(DashboardFrame parent, Long issueId) {
        super(parent, issueId == null ? "Nuova Issue" : "Issue #" + issueId, true);
        this.issueId = issueId;
        this.parent  = parent;
        this.isAdmin = "ADMIN".equals(parent.currentUserRole);
        setSize(540, issueId == null ? 560 : 800);
        setLocationRelativeTo(parent);
        if (isAdmin) loadUsers();
        buildUI();

        if (issueId != null && !isAdmin) {
            titleField.setEditable(false);
            titleField.setBackground(new Color(220, 205, 245));
            descArea.setEditable(false);
            descArea.setBackground(new Color(220, 205, 245));
            typeBox.setEnabled(false);
            priorityBox.setEnabled(false);
        }

        if (issueId != null) loadIssue();
    }

    private void loadUsers() {
        try {
            String resp = ApiClient.get("/users");
            JsonNode arr = ApiClient.mapper.readTree(resp);
            assigneeBox.addItem(new UserEntry(-1L, "— Nessuno —"));
            for (JsonNode u : arr) {
                UserEntry entry = new UserEntry(u.get("id").asLong(), u.get("email").asText());
                userList.add(entry);
                assigneeBox.addItem(entry);
            }
        } catch (Exception ex) {
            assigneeBox.addItem(new UserEntry(-1L, "Errore caricamento utenti"));
        }
    }

    private void buildUI() {
        JLabel header = new JLabel(
                "  " + (issueId == null ? "Nuova Issue" : "Issue #" + issueId),
                SwingConstants.LEFT);
        header.setFont(new Font("SansSerif", Font.BOLD, 15));
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill   = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(panel, g, row++, "Titolo:",         titleField);
        addRow(panel, g, row++, "Tipo:",            typeBox);
        addRow(panel, g, row++, "Priorita (1-5):", priorityBox);

        if (isAdmin) {
            addRow(panel, g, row++, "Assegna a:", assigneeBox);
            addRow(panel, g, row++, "Scadenza:",  deadlineField);
        }

        g.gridx = 0; g.gridy = row;
        panel.add(styledLabel("Descrizione:"), g);
        g.gridx = 1;
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createLineBorder(FIELD_BORDER));
        panel.add(descScroll, g);
        row++;

        // ── Immagine ──────────────────────────────────────
        imagePreview = new JLabel("Nessuna immagine", SwingConstants.CENTER);
        imagePreview.setPreferredSize(new Dimension(80, 60));
        imagePreview.setForeground(new Color(130, 80, 180));
        imagePreview.setFont(new Font("SansSerif", Font.ITALIC, 11));
        imagePreview.setBorder(BorderFactory.createLineBorder(FIELD_BORDER));
        imagePreview.setBackground(FIELD_BG);
        imagePreview.setOpaque(true);

        JButton pickImg = styledBtn("Allega immagine", BTN_PRIMARY);
        pickImg.setFont(new Font("SansSerif", Font.PLAIN, 12));
        pickImg.addActionListener(e -> pickImage());
        if (issueId != null && !isAdmin) pickImg.setVisible(false);

        JPanel imgPanel = new JPanel(new BorderLayout(8, 0));
        imgPanel.setOpaque(false);
        imgPanel.add(pickImg,      BorderLayout.WEST);
        imgPanel.add(imagePreview, BorderLayout.CENTER);

        g.gridx = 0; g.gridy = row;
        panel.add(styledLabel("Immagine:"), g);
        g.gridx = 1;
        panel.add(imgPanel, g);
        row++;

        if (issueId != null) {
            g.gridx = 0; g.gridy = row;
            panel.add(styledLabel("Commenti:"), g);

            commentsPanel = new JPanel();
            commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
            commentsPanel.setBackground(COMMENT_BG);

            JScrollPane commentsScroll = new JScrollPane(commentsPanel);
            commentsScroll.setPreferredSize(new Dimension(350, 130));
            commentsScroll.setBorder(BorderFactory.createLineBorder(FIELD_BORDER));
            g.gridx = 1;
            panel.add(commentsScroll, g);
            row++;

            g.gridx = 0; g.gridy = row;
            panel.add(styledLabel("Aggiungi:"), g);
            g.gridx = 1;
            JPanel commentRow = new JPanel(new BorderLayout(4, 0));
            commentRow.setOpaque(false);
            JButton sendBtn = styledBtn("Invia", BTN_PRIMARY);
            sendBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
            commentRow.add(commentField, BorderLayout.CENTER);
            commentRow.add(sendBtn,      BorderLayout.EAST);
            panel.add(commentRow, g);
            sendBtn.addActionListener(e -> sendComment());
            row++;

            g.gridx = 0; g.gridy = row;
            panel.add(styledLabel("Allegato:"), g);
            JLabel existingImg = new JLabel("Nessuna immagine allegata");
            existingImg.setForeground(new Color(200, 170, 255));
            existingImg.setFont(new Font("SansSerif", Font.ITALIC, 11));
            existingImg.setName("existingImg");
            g.gridx = 1;
            panel.add(existingImg, g);
            row++;

            doneBtn = styledBtn("Segna come Completata", BTN_GREEN);
            doneBtn.setVisible(false);
            doneBtn.addActionListener(e -> {
                try {
                    ApiClient.patch(issueBase() + "/" + issueId + "/complete", "{}");
                    JOptionPane.showMessageDialog(this, "Issue completata!");
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
                }
            });
            g.gridx = 0; g.gridy = row; g.gridwidth = 2;
            panel.add(doneBtn, g);
            row++;

            if (isAdmin) {
                deleteIssueBtn = styledBtn("Elimina Issue", new Color(160, 30, 30));
                deleteIssueBtn.setVisible(false);
                deleteIssueBtn.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Eliminare definitivamente questa issue?\nL'operazione non e' reversibile.",
                            "Conferma eliminazione",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (confirm != JOptionPane.YES_OPTION) return;
                    try {
                        ApiClient.delete(issueBase() + "/" + issueId);
                        JOptionPane.showMessageDialog(this, "Issue eliminata!");
                        dispose();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
                    }
                });
                g.gridx = 0; g.gridy = row; g.gridwidth = 2;
                panel.add(deleteIssueBtn, g);
                row++;
            }
        }

        if ((issueId == null) || isAdmin) {
            JButton saveBtn = styledBtn(
                    issueId == null ? "Crea Issue" : "Salva Modifiche", BTN_PRIMARY);
            g.gridx = 0; g.gridy = row; g.gridwidth = 2;
            panel.add(saveBtn, g);
            saveBtn.addActionListener(e -> save());
        }

        JScrollPane formScroll = new JScrollPane(panel);
        formScroll.setBorder(null);
        formScroll.getViewport().setBackground(BG_PANEL);
        formScroll.getVerticalScrollBar().setUnitIncrement(12);

        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);
        add(header,     BorderLayout.NORTH);
        add(formScroll, BorderLayout.CENTER);
    }

    private JLabel styledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(LABEL_FG);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        return lbl;
    }

    private void pickImage() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Immagini", "jpg", "jpeg", "png", "gif"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File f = fc.getSelectedFile();
        ImageIcon icon = new ImageIcon(new ImageIcon(f.getAbsolutePath())
                .getImage().getScaledInstance(80, 60, Image.SCALE_SMOOTH));
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
                            "Errore upload: " + ex.getMessage());
                    imagePreview.setIcon(null);
                    imagePreview.setText("Errore upload");
                }
            }
        }.execute();
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent comp) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1;
        p.add(styledLabel(label), g);
        g.gridx = 1;
        p.add(comp, g);
    }

    private void loadIssue() {
        try {
            String   resp = ApiClient.get(issueBase() + "/" + issueId);
            JsonNode n    = ApiClient.mapper.readTree(resp);
            titleField.setText(n.get("title").asText());
            descArea.setText(n.path("description").asText());
            typeBox.setSelectedItem(n.get("type").asText());
            priorityBox.setSelectedItem(n.get("priority").asText());

            if (!n.path("deadline").isNull() && !n.path("deadline").isMissingNode()) {
                JsonNode dlNode = n.get("deadline");
                if (dlNode.isArray()) {
                    deadlineField.setText(String.format("%02d/%02d/%04d",
                            dlNode.get(2).asInt(),
                            dlNode.get(1).asInt(),
                            dlNode.get(0).asInt()));
                } else {
                    String dl = dlNode.asText("");
                    if (!dl.isEmpty()) {
                        String[] parts = dl.split("T")[0].split("-");
                        if (parts.length == 3)
                            deadlineField.setText(parts[2] + "/" + parts[1] + "/" + parts[0]);
                    }
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
                                imagePreview.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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

            boolean isDone     = "DONE".equals(n.get("status").asText());
            boolean isAssignee = !n.path("assignedTo").isNull()
                    && n.path("assignedTo").path("id").asLong() == parent.currentUserId;

            if (doneBtn != null)
                doneBtn.setVisible(!isDone && (isAdmin || isAssignee));
            if (deleteIssueBtn != null)
                deleteIssueBtn.setVisible(isDone);

            loadComments();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore caricamento: " + ex.getMessage());
        }
    }

    private void loadComments() {
        try {
            String   resp = ApiClient.get(issueBase() + "/" + issueId + "/comments");
            JsonNode arr  = ApiClient.mapper.readTree(resp);
            commentsPanel.removeAll();

            if (!arr.isArray() || arr.size() == 0) {
                JLabel empty = new JLabel("  Nessun commento");
                empty.setFont(new Font("SansSerif", Font.ITALIC, 11));
                empty.setForeground(new Color(150, 100, 200));
                commentsPanel.add(empty);
            }

            for (JsonNode c : arr) {
                long   authorId    = c.path("author").path("id").asLong(-1);
                String authorEmail = c.path("author").path("email").asText("?");
                String commentText = c.get("text").asText();
                long   commentId   = c.get("id").asLong();

                boolean isOwner   = (authorId == parent.currentUserId);
                boolean canEdit   = isOwner;
                boolean canDelete = isOwner || isAdmin;

                JPanel card = new JPanel(new BorderLayout(6, 0));
                card.setBackground(BG_CARD);
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(180, 140, 220)),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)));
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

                JLabel textLbl = new JLabel(
                        "<html><b style='color:#5A0096'>" + authorEmail
                                + "</b><span style='color:#333'>: " + commentText + "</span></html>");
                textLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
                card.add(textLbl, BorderLayout.CENTER);

                JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
                actions.setOpaque(false);

                if (canEdit) {
                    JButton editBtn = editCommentBtn(); // ← penna disegnata
                    editBtn.setToolTipText("Modifica commento");
                    editBtn.addActionListener(e ->
                            editComment(commentId, commentText, textLbl, authorEmail));
                    actions.add(editBtn);
                }
                if (canDelete) {
                    JButton delBtn = deleteCommentBtn(); // ← X disegnata
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

    private void editComment(long commentId, String oldText,
                             JLabel textLbl, String authorEmail) {
        String newText = (String) JOptionPane.showInputDialog(
                this, "Modifica il commento:",
                "Modifica commento", JOptionPane.PLAIN_MESSAGE,
                null, null, oldText);
        if (newText == null || newText.trim().isEmpty()) return;
        newText = newText.trim();
        try {
            String json = ApiClient.mapper.writeValueAsString(
                    java.util.Map.of("text", newText));
            ApiClient.put(issueBase() + "/" + issueId + "/comments/" + commentId, json);
            final String ft = newText;
            textLbl.setText("<html><b style='color:#5A0096'>" + authorEmail
                    + "</b><span style='color:#333'>: " + ft + "</span></html>");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore modifica: " + ex.getMessage());
        }
    }

    private void deleteComment(long commentId, JPanel card) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Eliminare questo commento?", "Conferma eliminazione",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            ApiClient.delete(issueBase() + "/" + issueId + "/comments/" + commentId);
            Container p = card.getParent();
            Component[] comps = p.getComponents();
            for (int i = 0; i < comps.length; i++) {
                if (comps[i] == card) {
                    p.remove(card);
                    if (i < p.getComponentCount()) p.remove(p.getComponent(i));
                    break;
                }
            }
            commentsPanel.revalidate();
            commentsPanel.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore eliminazione: " + ex.getMessage());
        }
    }

    private void sendComment() {
        String text = commentField.getText().trim();
        if (text.isEmpty()) return;
        try {
            String json = ApiClient.mapper.writeValueAsString(
                    java.util.Map.of("text", text));
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
            JOptionPane.showMessageDialog(this, "Il titolo e' obbligatorio");
            return;
        }
        try {
            if (issueId == null) {
                StringBuilder json = new StringBuilder();
                json.append(String.format(
                        "{\"title\":\"%s\",\"description\":\"%s\",\"type\":\"%s\",\"priority\":\"%s\"",
                        title, desc, typeBox.getSelectedItem(), priorityBox.getSelectedItem()));
                if (selectedImageUrl != null && !selectedImageUrl.isEmpty())
                    json.append(",\"imageUrl\":\"").append(selectedImageUrl).append("\"");
                if (isAdmin) {
                    UserEntry sel = (UserEntry) assigneeBox.getSelectedItem();
                    if (sel != null && sel.id > 0)
                        json.append(",\"assignedToId\":\"").append(sel.id).append("\"");
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
                UserEntry sel = (UserEntry) assigneeBox.getSelectedItem();
                if (sel != null && sel.id > 0) {
                    ApiClient.patch(issueBase() + "/" + issueId + "/assign",
                            String.format("{\"userId\":\"%d\"}", sel.id));
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
            return p.length == 3 ? p[2] + "-" + p[1] + "-" + p[0] : null;
        } catch (Exception e) { return null; }
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
                    int maxW = 660, maxH = 440;
                    double scale = Math.min((double) maxW / img.getWidth(),
                            (double) maxH / img.getHeight());
                    ImageIcon icon = new ImageIcon(img.getScaledInstance(
                            (int)(img.getWidth()  * scale),
                            (int)(img.getHeight() * scale), Image.SCALE_SMOOTH));
                    SwingUtilities.invokeLater(() -> {
                        viewer.remove(loading);
                        viewer.add(new JLabel(icon, SwingConstants.CENTER), BorderLayout.CENTER);
                        JButton close = new JButton("Chiudi");
                        close.setBackground(BTN_PRIMARY);
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
                SwingUtilities.invokeLater(() ->
                        loading.setText("Errore caricamento immagine"));
            }
        }).start();

        viewer.setVisible(true);
    }
}