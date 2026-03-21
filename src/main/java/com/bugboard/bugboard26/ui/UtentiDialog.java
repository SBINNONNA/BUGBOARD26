package com.bugboard.bugboard26.ui;

import com.fasterxml.jackson.databind.JsonNode;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class UtentiDialog extends JDialog {

    private final boolean isAdmin;
    private DefaultTableModel model;

    public UtentiDialog(Frame parent, boolean isAdmin) {
        super(parent, "Tutti gli Utenti", true);
        this.isAdmin = isAdmin;
        setSize(600, 450);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(new Color(140, 80, 200));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("  👤 Tutti gli Utenti (globali)", SwingConstants.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setOpaque(true);
        title.setBackground(new Color(90, 0, 160));
        title.setBorder(BorderFactory.createEmptyBorder(14, 15, 14, 15));

        model = new DefaultTableModel(new String[]{"ID", "Email", "Ruolo"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);
        loadUsers();

        if (isAdmin) {
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = table.getSelectedRow();
                        if (row >= 0) editUser(
                                Long.parseLong(model.getValueAt(row, 0).toString()),
                                model.getValueAt(row, 1).toString(),
                                model.getValueAt(row, 2).toString(),
                                row
                        );
                    }
                }
            });
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scroll.getViewport().setBackground(new Color(160, 110, 210));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(new Color(90, 0, 160));

        if (isAdmin) {
            JButton btnDelete = new JButton("🗑 Elimina selezionato");
            btnDelete.setBackground(new Color(180, 30, 30));
            btnDelete.setForeground(Color.WHITE);
            btnDelete.setFocusPainted(false);
            btnDelete.setBorderPainted(false);
            btnDelete.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(this, "Seleziona un utente prima.");
                    return;
                }
                Long userId = Long.parseLong(model.getValueAt(row, 0).toString());
                String email = model.getValueAt(row, 1).toString();
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Eliminare l'utente " + email + "?", "Conferma",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        ApiClient.delete("/users/" + userId);
                        model.removeRow(row);
                        JOptionPane.showMessageDialog(this, "Utente eliminato.");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
                    }
                }
            });

            JLabel hint = new JLabel("  ✏️ Doppio click per modificare");
            hint.setForeground(new Color(220, 200, 255));
            hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
            footer.add(hint);
            footer.add(btnDelete);
        }

        JButton close = new JButton("Chiudi");
        close.setBackground(new Color(85, 0, 155));
        close.setForeground(Color.WHITE);
        close.setFocusPainted(false);
        close.setBorderPainted(false);
        close.addActionListener(e -> dispose());
        footer.add(close);

        add(title,  BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    // ── converte enum DB → etichetta UI ──────────────────────
    private String toDisplayRole(String raw) {
        return switch (raw.toUpperCase()) {
            case "ADMIN"           -> "ADMIN";
            case "ASSIGNED_USER"   -> "USER 🔧";
            case "UNASSIGNED_USER" -> "USER";
            default                -> raw;
        };
    }

    // ── converte etichetta UI → enum DB (per il salvataggio) ─
    private String toRawRole(String display) {
        return switch (display) {
            case "ADMIN"    -> "ADMIN";
            case "USER 🔧"  -> "ASSIGNED_USER";
            default         -> "UNASSIGNED_USER";
        };
    }

    private void loadUsers() {
        model.setRowCount(0);
        try {
            String resp = ApiClient.get("/users");
            JsonNode arr = ApiClient.mapper.readTree(resp);
            for (JsonNode u : arr) {
                String rawRole = u.path("role").asText("UNASSIGNED_USER");
                model.addRow(new Object[]{
                        u.get("id").asText(),
                        u.get("email").asText(),
                        toDisplayRole(rawRole)   // ← mostra etichetta leggibile
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
        }
    }

    private void editUser(Long id, String email, String displayRole, int row) {
        JDialog edit = new JDialog(this, "Modifica utente", true);
        edit.setSize(340, 220);
        edit.setLocationRelativeTo(this);
        edit.getContentPane().setBackground(new Color(120, 60, 190));
        edit.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField emailField = new JTextField(email, 18);

        // ── il combo mostra le etichette UI, non gli enum grezzi ──
        JComboBox<String> roleBox = new JComboBox<>(
                new String[]{"USER", "USER 🔧", "ADMIN"});
        roleBox.setSelectedItem(displayRole);

        addRow(edit, g, 0, "Email:", emailField);
        addRow(edit, g, 1, "Ruolo:", roleBox);

        JButton save = new JButton("Salva");
        save.setBackground(new Color(85, 0, 155));
        save.setForeground(Color.WHITE);
        save.setFocusPainted(false);
        save.setBorderPainted(false);
        g.gridx = 0; g.gridy = 2; g.gridwidth = 2;
        edit.add(save, g);

        save.addActionListener(ev -> {
            try {
                String selectedDisplay = (String) roleBox.getSelectedItem();
                String rawRole = toRawRole(selectedDisplay); // ← riconverti per il backend
                String json = String.format("{\"email\":\"%s\",\"role\":\"%s\"}",
                        emailField.getText().trim(), rawRole);
                ApiClient.put("/users/" + id, json);
                model.setValueAt(emailField.getText().trim(), row, 1);
                model.setValueAt(selectedDisplay, row, 2);  // ← mostra etichetta in tabella
                edit.dispose();
                JOptionPane.showMessageDialog(this, "Utente aggiornato!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
            }
        });
        edit.setVisible(true);
    }

    private void addRow(JDialog d, GridBagConstraints g, int row, String lbl, JComponent comp) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1;
        JLabel l = new JLabel(lbl);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        d.add(l, g);
        g.gridx = 1;
        d.add(comp, g);
    }

    private void styleTable(JTable table) {
        table.getTableHeader().setDefaultRenderer((tbl, val, sel, foc, row, col) -> {
            JLabel lbl = new JLabel(val == null ? "" : val.toString(), SwingConstants.CENTER);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            lbl.setForeground(Color.WHITE);
            lbl.setBackground(new Color(100, 0, 170));
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(130, 50, 200)),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            return lbl;
        });
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setGridColor(new Color(130, 80, 190));
    }
}
