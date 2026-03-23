package com.bugboard.bugboard26.ui;

import com.fasterxml.jackson.databind.JsonNode;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.*;

public class DashboardFrame extends JFrame {

    static final Color SIDEBAR_BG  = new Color(100, 0, 170);
    static final Color SIDEBAR_BTN = new Color(125, 30, 205);
    static final Color MAIN_BG     = new Color(185, 150, 215);
    static final Color COL_BG      = new Color(148, 98, 192);
    private JCheckBox onlyMineBox;

    private JPanel todoPanel, inProgressPanel, donePanel;
    private final JTextField searchField = new JTextField(12);
    private final JComboBox<String> priFilter = new JComboBox<>(
            new String[]{"Tutti", "P1", "P2", "P3", "P4", "P5"});
    private final JComboBox<String> typeFilter = new JComboBox<>(
            new String[]{"Tutti", "BUG", "QUESTION", "FEATURE", "DOCUMENTATION"});

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

        javax.swing.Timer t = new javax.swing.Timer(300, e -> loadIssues());
        t.setRepeats(false);
        t.start();
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
                    String rawRole = "";
                    if (n.has("role"))             rawRole = n.get("role").asText("");
                    else if (n.has("roles"))       rawRole = n.get("roles").toString();
                    else if (n.has("authorities")) rawRole = n.get("authorities").toString();
                    currentUserRole = rawRole.toUpperCase().contains("ADMIN") ? "ADMIN" : "USER";
                    userLabel.setText(currentUserEmail.split("@")[0].toUpperCase());
                    roleLabel.setText(currentUserRole);

                    if (onlyMineBox != null)
                        onlyMineBox.setVisible(!"ADMIN".equals(currentUserRole));

                    String pic = n.path("profilePicture").asText("");
                    if (!pic.isEmpty()) AvatarPanel.setAvatarUrl(pic);

                } catch (Exception ignored) {}
            }
        }.execute();
    }

    // ─── LOGO da resources ─────────────────────────────────
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
        lbl.setForeground(new Color(80, 0, 150));
        return lbl;
    }

    // ─── SIDEBAR ───────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(SIDEBAR_BG);
        sb.setPreferredSize(new Dimension(200, 0));
        sb.setBorder(BorderFactory.createEmptyBorder(20, 10, 25, 10));

        JPanel av = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        av.setOpaque(false);
        av.add(new AvatarPanel(120));
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

        sb.add(sideBtn("< Progetti", e -> {
            dispose();
            new ProjectSelectionFrame().setVisible(true);
        }));
        sb.add(Box.createVerticalStrut(8));

        JButton logout = sideBtn("Logout", e -> {
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
        main.setBorder(BorderFactory.createEmptyBorder(14, 20, 20, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        titleRow.setOpaque(false);
        JLabel b1 = new JLabel("Board:");
        b1.setFont(new Font("Trebuchet MS", Font.BOLD, 40));
        b1.setForeground(new Color(55, 0, 100));
        JLabel b2 = new JLabel(ApiClient.getCurrentProjectName());
        b2.setFont(new Font("Trebuchet MS", Font.BOLD, 40));
        b2.setForeground(new Color(210, 185, 240));
        titleRow.add(b1); titleRow.add(b2);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterRow.setOpaque(false);

        styleCombo(priFilter);
        styleCombo(typeFilter);

        JLabel titleLbl = new JLabel("Titolo:");
        titleLbl.setForeground(new Color(55, 0, 100));
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 12));

        searchField.setBackground(new Color(160, 110, 215));
        searchField.setForeground(Color.WHITE);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(190, 150, 240), 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        JLabel priLbl = new JLabel("Priorita:");
        priLbl.setForeground(new Color(55, 0, 100));
        priLbl.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel typeLbl = new JLabel("Tipo:");
        typeLbl.setForeground(new Color(55, 0, 100));
        typeLbl.setFont(new Font("SansSerif", Font.BOLD, 12));

        onlyMineBox = new JCheckBox("Solo le mie");
        onlyMineBox.setBackground(MAIN_BG);
        onlyMineBox.setForeground(new Color(55, 0, 100));
        onlyMineBox.setFont(new Font("SansSerif", Font.BOLD, 12));
        onlyMineBox.setFocusPainted(false);
        onlyMineBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton cerca = topBtn("Cerca");
        JButton nuova = topBtn("+ Issue");
        cerca.addActionListener(e -> loadIssues());
        nuova.addActionListener(e -> {
            new IssueFormDialog(this, null).setVisible(true);
            loadIssues();
        });

        filterRow.add(titleLbl);
        filterRow.add(searchField);
        filterRow.add(priLbl);
        filterRow.add(priFilter);
        filterRow.add(typeLbl);
        filterRow.add(typeFilter);
        filterRow.add(onlyMineBox);
        filterRow.add(cerca);
        filterRow.add(nuova);

        JPanel titleBlock = new JPanel(new BorderLayout());
        titleBlock.setOpaque(false);
        titleBlock.add(titleRow,  BorderLayout.NORTH);
        JPanel filterWrap = new JPanel(new BorderLayout());
        filterWrap.setOpaque(false);
        filterWrap.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        filterWrap.add(filterRow, BorderLayout.CENTER);
        titleBlock.add(filterWrap, BorderLayout.SOUTH);
        JPanel logoP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        logoP.setOpaque(false);
        logoP.add(buildLogo(90, 70));

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

    private void styleCombo(JComboBox<String> box) {
        Color bg      = new Color(110, 20, 180);
        Color bgHover = new Color(140, 50, 210);

        box.setBackground(bg);
        box.setForeground(Color.WHITE);
        box.setFont(new Font("SansSerif", Font.BOLD, 12));
        box.setOpaque(true);

        box.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                lbl.setBackground(isSelected ? bgHover : bg);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
                lbl.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                lbl.setOpaque(true);
                return lbl;
            }
        });

        box.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        g.setColor(new Color(140, 50, 210));
                        g.fillRect(0, 0, getWidth(), getHeight());
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(Color.WHITE);
                        int cx = getWidth()  / 2;
                        int cy = getHeight() / 2;
                        int[] xp = {cx - 5, cx + 5, cx};
                        int[] yp = {cy - 3, cy - 3, cy + 3};
                        g2.fillPolygon(xp, yp, 3);
                    }
                };
                btn.setBorder(BorderFactory.createEmptyBorder());
                btn.setFocusPainted(false);
                btn.setContentAreaFilled(false);
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return btn;
            }

            @Override
            public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                ListCellRenderer<Object> renderer = comboBox.getRenderer();
                Component c = renderer.getListCellRendererComponent(
                        listBox, comboBox.getSelectedItem(), -1, false, false);
                c.setFont(comboBox.getFont());
                if (c instanceof JLabel lbl) {
                    lbl.setForeground(Color.WHITE);
                    lbl.setBackground(bg);
                    lbl.setOpaque(true);
                }
                currentValuePane.paintComponent(g, c, comboBox,
                        bounds.x, bounds.y, bounds.width, bounds.height, false);
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds,
                                                    boolean hasFocus) {
                g.setColor(bg);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });
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
        hdr.setFont(new Font("Trebuchet MS", Font.BOLD, 14));
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

        final boolean onlyMine = onlyMineBox != null && onlyMineBox.isSelected();

        new SwingWorker<java.util.List<Object[]>, Void>() {
            @Override
            protected java.util.List<Object[]> doInBackground() throws Exception {
                StringBuilder url = new StringBuilder(
                        "/projects/" + ApiClient.getCurrentProjectId() + "/issues?");
                String kw = searchField.getText().trim();
                if (!kw.isEmpty()) url.append("keyword=").append(kw).append("&");
                String pr = (String) priFilter.getSelectedItem();
                if (!"Tutti".equals(pr)) url.append("priority=").append(pr).append("&");
                String tp = (String) typeFilter.getSelectedItem();
                if (!"Tutti".equals(tp)) url.append("type=").append(tp).append("&");

                String resp = ApiClient.get(url.toString());
                JsonNode arr = ApiClient.mapper.readTree(resp);

                java.util.List<Object[]> result = new java.util.ArrayList<>();
                for (JsonNode issue : arr) {
                    if (onlyMine) {
                        boolean assignedToMe = !issue.path("assignedTo").isNull()
                                && issue.path("assignedTo").path("id").asLong(-1)
                                == (currentUserId != null ? currentUserId : -2L);
                        if (!assignedToMe) continue;
                    }
                    long id = issue.get("id").asLong();
                    int commentCount = 0;
                    try {
                        String cr = ApiClient.get("/projects/" + ApiClient.getCurrentProjectId()
                                + "/issues/" + id + "/comments");
                        commentCount = ApiClient.mapper.readTree(cr).size();
                    } catch (Exception ignored) {}
                    result.add(new Object[]{issue, commentCount});
                }
                return result;
            }

            @Override
            protected void done() {
                try {
                    for (Object[] entry : get()) {
                        JsonNode issue        = (JsonNode) entry[0];
                        int      commentCount = (int)      entry[1];
                        JPanel   card         = buildCard(issue, commentCount);
                        String   status       = issue.get("status").asText();
                        if ("DONE".equals(status)) {
                            donePanel.add(card);
                            donePanel.add(Box.createVerticalStrut(8));
                        } else if ("IN_PROGRESS".equals(status)) {
                            inProgressPanel.add(card);
                            inProgressPanel.add(Box.createVerticalStrut(8));
                        } else {
                            todoPanel.add(card);
                            todoPanel.add(Box.createVerticalStrut(8));
                        }
                    }
                    todoPanel.revalidate();       todoPanel.repaint();
                    inProgressPanel.revalidate(); inProgressPanel.repaint();
                    donePanel.revalidate();       donePanel.repaint();
                    revalidate(); repaint();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DashboardFrame.this,
                            "Errore: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // ─── CARD ──────────────────────────────────────────────
    private JPanel buildCard(JsonNode issue, int commentCount) {
        Long   id       = issue.get("id").asLong();
        String title    = issue.get("title").asText();
        String type     = issue.get("type").asText();
        String priority = issue.get("priority").asText();

        boolean isAssignedToMe = !issue.path("assignedTo").isNull()
                && issue.path("assignedTo").path("id").asLong() == currentUserId;

        JPanel card = new JPanel(new BorderLayout(6, 4));
        card.setBackground(new Color(160, 112, 205));
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(14, isAssignedToMe
                        ? new Color(255, 200, 0)
                        : new Color(130, 80, 190)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // ── Riga titolo ──
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        titlePanel.setOpaque(false);
        if (isAssignedToMe) {
            JLabel flag = new JLabel("📌");
            flag.setFont(new Font("SansSerif", Font.PLAIN, 13));
            flag.setToolTipText("Questa issue e' assegnata a te");
            titlePanel.add(flag);
        }
        JLabel titleLbl = new JLabel("#" + id + "  " + title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLbl.setForeground(Color.WHITE);
        titlePanel.add(titleLbl);
        topRow.add(titlePanel, BorderLayout.WEST);

        // ── Tipo con icona ──
        String typeIcon = switch (type) {
            case "BUG"           -> "🐛 BUG";
            case "FEATURE"       -> "✨ FEATURE";
            case "QUESTION"      -> "❓ QUESTION";
            case "DOCUMENTATION" -> "📄 DOCUMENTATION";
            default              -> type;
        };
        JLabel typeLbl = new JLabel(typeIcon);
        typeLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        typeLbl.setForeground(new Color(220, 200, 255));

        // ── Riga bottom: commenti + priorità ──
        JPanel botRow = new JPanel(new BorderLayout());
        botRow.setOpaque(false);

        JLabel commentIcon = new JLabel("💬 " + commentCount);  // ← ripristinato + icona
        commentIcon.setForeground(Color.WHITE);
        commentIcon.setFont(new Font("SansSerif", Font.PLAIN, 12));

        String priorityNum = priority.replace("P", "");
        JLabel priLbl = new JLabel(priorityNum + "/5");
        priLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        priLbl.setForeground(getPriorityColor(priority));

        botRow.add(commentIcon, BorderLayout.WEST);
        botRow.add(priLbl,      BorderLayout.EAST);

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

    private Color getPriorityColor(String priority) {
        return switch (priority) {
            case "P1", "P2" -> new Color(127, 0, 255);
            case "P3"       -> new Color(128, 0, 255);
            case "P4", "P5" -> new Color(130, 0, 255);
            default         -> Color.WHITE;
        };
    }
}
