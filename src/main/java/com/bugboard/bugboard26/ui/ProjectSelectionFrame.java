package com.bugboard.bugboard26.ui;

import com.fasterxml.jackson.databind.JsonNode;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

public class ProjectSelectionFrame extends JFrame {

    private static final Color BG       = new Color(155, 100, 215);
    private static final Color CARD_BG  = new Color(140, 80, 200);
    private static final Color CARD_HOV = new Color(160, 105, 225);
    private static final Color PURPLE   = new Color(85, 0, 155);

    private JPanel cardsPanel;
    private String currentRole = "USER";

    private final JButton newBtn;
    private final JButton newUserBtn;
    private final JButton allUsersBtn;

    public ProjectSelectionFrame() {
        setTitle("BugBoard26 — Scegli progetto");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        newBtn = new JButton("＋ Nuovo Progetto");
        styleTopBtn(newBtn);
        newBtn.setVisible(false);
        newBtn.addActionListener(e -> showCreateDialog());

        newUserBtn = new JButton("＋ Nuovo Utente");
        styleTopBtn(newUserBtn);
        newUserBtn.setVisible(false);
        newUserBtn.addActionListener(e -> showCreateUserDialog());

        allUsersBtn = new JButton("👥 Tutti gli Utenti");
        styleTopBtn(allUsersBtn);
        allUsersBtn.setVisible(false);
        allUsersBtn.addActionListener(e ->
                new UtentiDialog(this, "ADMIN".equals(currentRole)).setVisible(true));

        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        fetchRoleAndProjects();
    }

    // ── Logo da /logo.png ──────────────────────────────────
    private JLabel buildLogo(int width, int height) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/logo.png");
            if (is != null) {
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(is);
                ImageIcon icon = new ImageIcon(
                        img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
                JLabel lbl = new JLabel(icon);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                return lbl;
            }
        } catch (Exception ignored) {}
        JLabel lbl = new JLabel("BugBoard26", SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    // ─── TOP BAR ──────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(70, 0, 130));
        bar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(buildLogo(50, 38));
        JLabel title = new JLabel("BugBoard26");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        left.add(title);

        JButton logout = new JButton("⬅ Logout");
        logout.setBackground(new Color(75, 0, 130));
        logout.setForeground(Color.WHITE);
        logout.setFocusPainted(false);
        logout.setBorderPainted(false);
        logout.setFont(new Font("SansSerif", Font.BOLD, 12));
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> {
            ApiClient.setToken(null);
            dispose();
            new LoginFrame().setVisible(true);
        });

        bar.add(left,   BorderLayout.WEST);
        bar.add(logout, BorderLayout.EAST);
        return bar;
    }

    // ─── CONTENUTO ────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG);
        outer.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel heading = new JLabel("Seleziona un progetto");
        heading.setFont(new Font("Trebuchet MS", Font.BOLD, 26));
        heading.setForeground(new Color(50, 0, 90));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(allUsersBtn);
        btnPanel.add(newUserBtn);
        btnPanel.add(newBtn);

        headerRow.add(heading,  BorderLayout.WEST);
        headerRow.add(btnPanel, BorderLayout.EAST);

        cardsPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 18, 18));
        cardsPanel.setBackground(BG);

        JScrollPane scroll = new JScrollPane(cardsPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        outer.add(headerRow, BorderLayout.NORTH);
        outer.add(scroll,    BorderLayout.CENTER);
        return outer;
    }

    // ─── CARICAMENTO ──────────────────────────────────────────
    private void fetchRoleAndProjects() {
        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                String meResp       = ApiClient.get("/users/me");
                String projectsResp = ApiClient.get("/projects");
                JsonNode me       = ApiClient.mapper.readTree(meResp);
                JsonNode projects = ApiClient.mapper.readTree(projectsResp);

                long myId = me.get("id").asLong();

                Set<Long> projectsWithMyIssues = new HashSet<>();
                for (JsonNode p : projects) {
                    long projId = p.get("id").asLong();
                    try {
                        String issuesResp = ApiClient.get("/projects/" + projId + "/issues?");
                        JsonNode issues = ApiClient.mapper.readTree(issuesResp);
                        for (JsonNode issue : issues) {
                            if (!issue.path("assignedTo").isNull() &&
                                    issue.path("assignedTo").path("id").asLong() == myId &&
                                    !"DONE".equals(issue.path("status").asText())) {
                                projectsWithMyIssues.add(projId);
                                break;
                            }
                        }
                    } catch (Exception ignored) {}
                }
                return new Object[]{me, projects, projectsWithMyIssues};
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[]  results    = get();
                    JsonNode  me         = (JsonNode) results[0];
                    JsonNode  arr        = (JsonNode) results[1];
                    Set<Long> myProjects = (Set<Long>) results[2];

                    String rawRole = "";
                    if (me.has("role"))             rawRole = me.get("role").asText("");
                    else if (me.has("roles"))       rawRole = me.get("roles").toString();
                    else if (me.has("authorities")) rawRole = me.get("authorities").toString();
                    currentRole = rawRole.toUpperCase().contains("ADMIN") ? "ADMIN" : "USER";

                    if ("ADMIN".equals(currentRole)) {
                        newBtn.setVisible(true);
                        newUserBtn.setVisible(true);
                        allUsersBtn.setVisible(true);
                    }

                    cardsPanel.removeAll();
                    if (arr.isEmpty()) {
                        JLabel empty = new JLabel("Nessun progetto disponibile.");
                        empty.setForeground(new Color(230, 210, 255));
                        empty.setFont(new Font("SansSerif", Font.ITALIC, 15));
                        cardsPanel.add(empty);
                    } else {
                        for (JsonNode p : arr) {
                            boolean hasMyIssues = myProjects.contains(p.get("id").asLong());
                            addProjectCard(p, hasMyIssues);
                        }
                    }
                    cardsPanel.revalidate();
                    cardsPanel.repaint();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ProjectSelectionFrame.this,
                            "Errore: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // ─── CARD PROGETTO ────────────────────────────────────────
    private void addProjectCard(JsonNode project, boolean hasAssignedIssues) {
        Long   id   = project.get("id").asLong();
        String name = project.get("name").asText();
        String desc = project.path("description").asText("Nessuna descrizione");
        String by   = project.path("createdBy").path("email").asText("—");

        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(18, hasAssignedIssues
                        ? new Color(255, 200, 0)
                        : new Color(120, 60, 190)),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));
        card.setPreferredSize(new Dimension(240, 160));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // ── Riga titolo + flag ──
        JPanel nameRow = new JPanel(new BorderLayout(6, 0));
        nameRow.setOpaque(false);
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 17));
        nameLbl.setForeground(Color.WHITE);
        nameRow.add(nameLbl, BorderLayout.WEST);

        if (hasAssignedIssues) {
            JLabel flag = new JLabel("📌");
            flag.setFont(new Font("SansSerif", Font.PLAIN, 16));
            flag.setToolTipText("Hai issue assegnate in questo progetto");
            nameRow.add(flag, BorderLayout.EAST);
        }

        JLabel descLbl = new JLabel(
                "<html><body style='width:190px'>" + desc + "</body></html>");
        descLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        descLbl.setForeground(new Color(220, 195, 255));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JLabel byLbl = new JLabel("👤 " + by.split("@")[0]);
        byLbl.setFont(new Font("SansSerif", Font.ITALIC, 11));
        byLbl.setForeground(new Color(190, 155, 230));
        footer.add(byLbl, BorderLayout.WEST);

        if ("ADMIN".equals(currentRole)) {
            JButton del = new JButton("🗑");
            del.setBackground(new Color(160, 30, 60));
            del.setForeground(Color.WHITE);
            del.setFocusPainted(false);
            del.setBorderPainted(false);
            del.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            del.addActionListener(e -> {
                int ok = JOptionPane.showConfirmDialog(this,
                        "Eliminare \"" + name + "\"?",
                        "Conferma", JOptionPane.YES_NO_OPTION);
                if (ok == JOptionPane.YES_OPTION) deleteProject(id);
            });
            footer.add(del, BorderLayout.EAST);
        }

        card.add(nameRow, BorderLayout.NORTH);
        card.add(descLbl, BorderLayout.CENTER);
        card.add(footer,  BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { openProject(id, name); }
            public void mouseEntered(MouseEvent e) { card.setBackground(CARD_HOV); }
            public void mouseExited(MouseEvent e)  { card.setBackground(CARD_BG);  }
        });
        cardsPanel.add(card);
    }

    // ─── DIALOG NUOVO UTENTE ──────────────────────────────────
    private void showCreateUserDialog() {
        JDialog dlg = new JDialog(this, "Nuovo Utente", true);
        dlg.setSize(400, 280);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(new Color(120, 60, 190));
        dlg.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 12, 8, 12);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField     emailField = new JTextField(20);
        JPasswordField passField  = new JPasswordField(20);
        JComboBox<String> roleBox = new JComboBox<>(
                new String[]{"UNASSIGNED_USER", "ADMIN"});

        styleField(emailField);
        styleField(passField);
        roleBox.setBackground(new Color(160, 110, 215));
        roleBox.setForeground(Color.BLACK);
        roleBox.setFont(new Font("SansSerif", Font.PLAIN, 13));

        String[]     labels = {"Email:", "Password:", "Ruolo:"};
        JComponent[] fields = {emailField, passField, roleBox};

        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0; g.gridy = i; g.gridwidth = 1;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setForeground(new Color(220, 195, 255));
            lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
            dlg.add(lbl, g);
            g.gridx = 1;
            dlg.add(fields[i], g);
        }

        JButton save = new JButton("✔ Crea Utente");
        save.setBackground(PURPLE);
        save.setForeground(Color.WHITE);
        save.setFocusPainted(false);
        save.setBorderPainted(false);
        save.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2;
        dlg.add(save, g);

        save.addActionListener(e -> {
            String email = emailField.getText().trim();
            String pass  = new String(passField.getPassword()).trim();
            String role  = (String) roleBox.getSelectedItem();
            if (email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Email e password sono obbligatorie");
                return;
            }
            try {
                String json = String.format(
                        "{\"email\":\"%s\",\"password\":\"%s\",\"role\":\"%s\"}",
                        email, pass, role);
                ApiClient.postAuth("/users", json);
                JOptionPane.showMessageDialog(dlg, "Utente creato con successo! ✅");
                dlg.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Errore: " + ex.getMessage());
            }
        });

        dlg.setVisible(true);
    }

    // ─── DIALOG NUOVO PROGETTO ────────────────────────────────
    private void showCreateDialog() {
        JDialog dlg = new JDialog(this, "Nuovo Progetto", true);
        dlg.setSize(380, 260);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(new Color(120, 60, 190));
        dlg.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 12, 10, 12);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(20);
        JTextArea  descArea  = new JTextArea(3, 20);
        styleField(nameField);
        descArea.setBackground(new Color(160, 110, 215));
        descArea.setForeground(Color.WHITE);
        descArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descArea.setBorder(BorderFactory.createLineBorder(new Color(190, 150, 240)));

        g.gridx = 0; g.gridy = 0;
        JLabel nl = new JLabel("Nome:");
        nl.setForeground(new Color(220, 195, 255));
        nl.setFont(new Font("SansSerif", Font.BOLD, 12));
        dlg.add(nl, g);
        g.gridx = 1; dlg.add(nameField, g);

        g.gridx = 0; g.gridy = 1;
        JLabel dl = new JLabel("Descrizione:");
        dl.setForeground(new Color(220, 195, 255));
        dl.setFont(new Font("SansSerif", Font.BOLD, 12));
        dlg.add(dl, g);
        g.gridx = 1; dlg.add(new JScrollPane(descArea), g);

        JButton save = new JButton("✔ Crea Progetto");
        save.setBackground(PURPLE);
        save.setForeground(Color.WHITE);
        save.setFocusPainted(false);
        save.setBorderPainted(false);
        save.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.gridx = 0; g.gridy = 2; g.gridwidth = 2;
        dlg.add(save, g);

        save.addActionListener(e -> {
            String n = nameField.getText().trim();
            String d = descArea.getText().trim();
            if (n.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Il nome è obbligatorio");
                return;
            }
            try {
                String json = String.format(
                        "{\"name\":\"%s\",\"description\":\"%s\"}", n, d);
                ApiClient.postAuth("/projects", json);
                dlg.dispose();
                fetchRoleAndProjects();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Errore: " + ex.getMessage());
            }
        });

        dlg.setVisible(true);
    }

    // ─── AZIONI ───────────────────────────────────────────────
    private void openProject(Long id, String name) {
        ApiClient.setCurrentProject(id, name);
        dispose();
        new DashboardFrame().setVisible(true);
    }

    private void deleteProject(Long id) {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create("http://localhost:8081/api/projects/" + id))
                        .header("Authorization", "Bearer " + ApiClient.getToken())
                        .DELETE().build();
                client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
                return null;
            }
            @Override protected void done() { fetchRoleAndProjects(); }
        }.execute();
    }

    // ─── UTILITY ──────────────────────────────────────────────
    private void styleTopBtn(JButton btn) {
        btn.setBackground(PURPLE);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleField(JTextField f) {
        f.setBackground(new Color(160, 110, 215));
        f.setForeground(Color.WHITE);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(190, 150, 240), 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    }

    private void styleField(JPasswordField f) {
        f.setBackground(new Color(160, 110, 215));
        f.setForeground(Color.WHITE);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(190, 150, 240), 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    }
}
