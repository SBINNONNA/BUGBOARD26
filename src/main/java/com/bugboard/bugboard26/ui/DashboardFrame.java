package com.bugboard.bugboard26.ui;

import com.fasterxml.jackson.databind.JsonNode;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DashboardFrame extends JFrame {

    static final Color SIDEBAR_BG  = new Color(100, 0, 170);
    static final Color SIDEBAR_BTN = new Color(125, 30, 205);
    static final Color MAIN_BG     = new Color(185, 150, 215);
    static final Color COL_BG      = new Color(148, 98, 192);

    private JPanel todoPanel, inProgressPanel, donePanel;
    private final JTextField searchField = new JTextField(12);
    private final JComboBox<String> priFilter = new JComboBox<>(
            new String[]{"Tutti", "LOW", "MEDIUM", "HIGH"});

    final JLabel userLabel = new JLabel("...", SwingConstants.CENTER);
    final JLabel roleLabel = new JLabel("",   SwingConstants.CENTER);
    String currentUserEmail = "";
    String currentUserRole  = "USER";
    Long   currentUserId    = null;

    public DashboardFrame() {
        setTitle("BugBoard26 — " + ApiClient.getCurrentProjectName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(MAIN_BG);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(),    BorderLayout.CENTER);
        fetchCurrentUser();
        loadIssues();
    }

    private void fetchCurrentUser() {
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                return ApiClient.get("/users/me");
            }
            @Override protected void done() {
                try {
                    JsonNode n = ApiClient.mapper.readTree(get());
                    currentUserId    = n.get("id").asLong();
                    currentUserEmail = n.get("email").asText();
// ❌ PRIMA
                    currentUserRole = n.path("role").asText("USER");

// ✅ DOPO
                    String rawRole = "";
                    if (n.has("role"))             rawRole = n.get("role").asText("");
                    else if (n.has("roles"))       rawRole = n.get("roles").toString();
                    else if (n.has("authorities")) rawRole = n.get("authorities").toString();
                    currentUserRole = rawRole.toUpperCase().contains("ADMIN") ? "ADMIN" : "USER";
                    userLabel.setText(currentUserEmail.split("@")[0].toUpperCase());
                    roleLabel.setText(currentUserRole);
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    // ─── SIDEBAR ───────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(SIDEBAR_BG);
        sb.setPreferredSize(new Dimension(200, 0));
        sb.setBorder(BorderFactory.createEmptyBorder(30, 15, 25, 15));

        JPanel av = new JPanel(new FlowLayout(FlowLayout.CENTER));
        av.setOpaque(false);
        av.add(new AvatarPanel(80));
        sb.add(av);
        sb.add(Box.createVerticalStrut(10));

        userLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        userLabel.setForeground(new Color(220, 195, 255));
        userLabel.setAlignmentX(CENTER_ALIGNMENT);
        userLabel.setMaximumSize(new Dimension(180, 20));
        sb.add(userLabel);

        roleLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        roleLabel.setForeground(new Color(190, 155, 230));
        roleLabel.setAlignmentX(CENTER_ALIGNMENT);
        roleLabel.setMaximumSize(new Dimension(180, 18));
        sb.add(roleLabel);
        sb.add(Box.createVerticalStrut(20));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(160, 90, 230));
        sep.setMaximumSize(new Dimension(170, 1));
        sb.add(sep);
        sb.add(Box.createVerticalStrut(20));

        sb.add(sideBtn("PROFILO",    e -> new ProfiloDialog(this).setVisible(true)));
        sb.add(Box.createVerticalStrut(8));
        sb.add(sideBtn("MEMBRI",     e -> new MembriDialog(this).setVisible(true)));
        sb.add(Box.createVerticalStrut(8));
        sb.add(sideBtn("NOTIFICHE",  e -> new NotificheDialog(this).setVisible(true)));
        sb.add(Box.createVerticalStrut(8));
        sb.add(sideBtn("CALENDARIO", e -> new CalendarioDialog(this).setVisible(true)));
        sb.add(Box.createVerticalGlue());

        sb.add(sideBtn("◀ Progetti", e -> {
            dispose();
            new ProjectSelectionFrame().setVisible(true);
        }));
        sb.add(Box.createVerticalStrut(8));

        JButton logout = sideBtn("⬅  Logout", e -> {
            ApiClient.setToken(null);
            dispose();
            new LoginFrame().setVisible(true);
        });
        logout.setBackground(new Color(75, 0, 130));
        sb.add(logout);
        return sb;
    }

    private JButton sideBtn(String text, ActionListener a) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(172, 38));
        btn.setBackground(SIDEBAR_BTN);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(a);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(150, 60, 230)); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(SIDEBAR_BTN); }
        });
        return btn;
    }

    // ─── MAIN ──────────────────────────────────────────────
    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(MAIN_BG);
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        titleRow.setOpaque(false);
        JLabel b1 = new JLabel("Board:");
        b1.setFont(new Font("SansSerif", Font.BOLD, 30));
        b1.setForeground(new Color(55, 0, 100));
        JLabel b2 = new JLabel(ApiClient.getCurrentProjectName());
        b2.setFont(new Font("SansSerif", Font.BOLD, 30));
        b2.setForeground(new Color(210, 185, 240));
        titleRow.add(b1); titleRow.add(b2);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterRow.setOpaque(false);
        JLabel flt = new JLabel("▼ filtra");
        flt.setFont(new Font("SansSerif", Font.BOLD, 13));
        flt.setForeground(new Color(70, 0, 130));
        filterRow.add(flt);
        filterRow.add(searchField);
        JLabel priLbl = new JLabel("Priorità:");
        priLbl.setForeground(new Color(55, 0, 100));
        priLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        filterRow.add(priLbl);
        filterRow.add(priFilter);
        JButton cerca    = topBtn("🔍 Cerca");
        JButton nuova    = topBtn("＋ Issue");
        JButton aggiorna = topBtn("↻");
        cerca.addActionListener(e -> loadIssues());
        aggiorna.addActionListener(e -> loadIssues());
        nuova.addActionListener(e -> { new IssueFormDialog(this, null).setVisible(true); loadIssues(); });
        filterRow.add(cerca); filterRow.add(nuova); filterRow.add(aggiorna);

        JPanel titleBlock = new JPanel(new BorderLayout());
        titleBlock.setOpaque(false);
        titleBlock.add(titleRow,  BorderLayout.NORTH);
        titleBlock.add(filterRow, BorderLayout.SOUTH);

        JPanel logoP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoP.setOpaque(false);
        logoP.add(new LogoPanel(false));

        top.add(titleBlock, BorderLayout.WEST);
        top.add(logoP,      BorderLayout.EAST);
        main.add(top,           BorderLayout.NORTH);
        main.add(buildKanban(), BorderLayout.CENTER);
        return main;
    }

    private JButton topBtn(String t) {
        JButton b = new JButton(t);
        b.setBackground(new Color(85, 0, 155));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ─── KANBAN ────────────────────────────────────────────
    private JPanel buildKanban() {
        JPanel board = new JPanel(new GridLayout(1, 3, 14, 0));
        board.setOpaque(false);
        todoPanel       = colContent();
        inProgressPanel = colContent();
        donePanel       = colContent();
        board.add(colWrapper("DA RISOLVERE", new Color(235, 85, 85),  todoPanel));
        board.add(colWrapper("IN CORSO...",  new Color(235, 155, 0),  inProgressPanel));
        board.add(colWrapper("RISOLTE",      new Color(85, 210, 105), donePanel));
        return board;
    }

    private JPanel colContent() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(COL_BG);
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return p;
    }

    private JScrollPane colWrapper(String title, Color tc, JPanel content) {
        JLabel hdr = new JLabel(title, SwingConstants.CENTER);
        hdr.setFont(new Font("SansSerif", Font.BOLD, 14));
        hdr.setForeground(tc);
        hdr.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        hdr.setOpaque(true);
        hdr.setBackground(COL_BG);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(new RoundedBorder(18, new Color(130, 80, 200)));
        scroll.setColumnHeaderView(hdr);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.setBackground(COL_BG);
        scroll.getViewport().setBackground(COL_BG);
        return scroll;
    }

    // ─── CARICAMENTO ISSUE ─────────────────────────────────
    void loadIssues() {
        todoPanel.removeAll();
        inProgressPanel.removeAll();
        donePanel.removeAll();

        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                StringBuilder url = new StringBuilder("/projects/" + ApiClient.getCurrentProjectId() + "/issues?");
                String kw = searchField.getText().trim();
                if (!kw.isEmpty()) url.append("keyword=").append(kw).append("&");
                String pr = (String) priFilter.getSelectedItem();
                if (!"Tutti".equals(pr)) url.append("priority=").append(pr).append("&");
                return ApiClient.get(url.toString());
            }
            @Override protected void done() {
                try {
                    JsonNode arr = ApiClient.mapper.readTree(get());
                    for (JsonNode issue : arr) {
                        JPanel card = buildCard(issue);
                        switch (issue.get("status").asText()) {
                            case "TODO"        -> { todoPanel.add(card);       todoPanel.add(Box.createVerticalStrut(8)); }
                            case "IN_PROGRESS" -> { inProgressPanel.add(card); inProgressPanel.add(Box.createVerticalStrut(8)); }
                            case "DONE"        -> { donePanel.add(card);       donePanel.add(Box.createVerticalStrut(8)); }
                        }
                    }
                    todoPanel.revalidate(); inProgressPanel.revalidate(); donePanel.revalidate();
                    repaint();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DashboardFrame.this, "Errore: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private JPanel buildCard(JsonNode issue) {
        Long   id       = issue.get("id").asLong();
        String title    = issue.get("title").asText();
        String type     = issue.get("type").asText();
        String priority = issue.get("priority").asText();
        String creator  = issue.path("createdBy").path("email").asText();

        JPanel card = new JPanel(new BorderLayout(6, 4));
        card.setBackground(new Color(160, 112, 205));
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(14, new Color(130, 80, 190)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Titolo + avatar
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel titleLbl = new JLabel("#" + id + "  " + title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLbl.setForeground(Color.WHITE);
        topRow.add(titleLbl, BorderLayout.WEST);
        topRow.add(new AvatarPanel(28), BorderLayout.EAST);

        // Tipo
        JLabel typeLbl = new JLabel(type);
        typeLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        typeLbl.setForeground(new Color(220, 200, 255));

        // Bottom row: commenti + priorità
        JPanel botRow = new JPanel(new BorderLayout());
        botRow.setOpaque(false);
        JLabel commLbl = new JLabel("💬");
        commLbl.setForeground(Color.WHITE);
        JLabel priLbl = new JLabel(priority);
        priLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        priLbl.setForeground(getPriorityColor(priority));
        botRow.add(commLbl, BorderLayout.WEST);
        botRow.add(priLbl,  BorderLayout.EAST);

        card.add(topRow,  BorderLayout.NORTH);
        card.add(typeLbl, BorderLayout.CENTER);
        card.add(botRow,  BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new IssueFormDialog(DashboardFrame.this, id).setVisible(true);
                loadIssues();
            }
            public void mouseEntered(MouseEvent e) { card.setBackground(new Color(175, 130, 220)); }
            public void mouseExited(MouseEvent e)  { card.setBackground(new Color(160, 112, 205)); }
        });
        return card;
    }

    private Color getPriorityColor(String p) {
        return switch (p) {
            case "HIGH"   -> new Color(255, 100, 100);
            case "MEDIUM" -> new Color(255, 200, 50);
            default       -> new Color(100, 255, 140);
        };
    }
}
