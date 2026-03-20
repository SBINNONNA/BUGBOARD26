package com.bugboard.bugboard26.ui;

import com.fasterxml.jackson.databind.JsonNode;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MembriDialog extends JDialog {

    private final DashboardFrame parent;

    public MembriDialog(DashboardFrame parent) {
        super(parent, "Membri", true);
        this.parent = parent;
        setSize(560, 420);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(new Color(140, 80, 200));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("  👥 Membri del team", SwingConstants.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setOpaque(true);
        title.setBackground(new Color(90, 0, 160));
        title.setBorder(BorderFactory.createEmptyBorder(14, 15, 14, 15));

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Email", "Ruolo"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        // ← SOSTITUISCI le due righe dell'header con questo blocco
        table.getTableHeader().setDefaultRenderer((tbl, val, sel, foc, row, col) -> {
            JLabel lbl = new JLabel(val == null ? "" : val.toString(), SwingConstants.CENTER);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            lbl.setForeground(Color.WHITE);
            lbl.setBackground(new Color(100, 0, 170));
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(130, 50, 200)),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)
            ));
            return lbl;
        });

        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setBackground(new Color(100, 0, 170));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setRowHeight(28);
        table.setGridColor(new Color(130, 80, 190));

        try {
            Long projectId = ApiClient.getCurrentProjectId();
            String resp = ApiClient.get("/projects/" + projectId + "/members");
            JsonNode arr = ApiClient.mapper.readTree(resp);
            for (JsonNode u : arr) {
                model.addRow(new Object[]{
                        u.get("id").asText(),
                        u.get("email").asText(),
                        u.path("role").asText("USER")
                });
            }
        } catch (Exception ex) {
            model.addRow(new Object[]{"Err", ex.getMessage(), ""});
        }


        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scroll.setBackground(new Color(140, 80, 200));
        scroll.getViewport().setBackground(new Color(160, 110, 210));

        // Se admin → doppio click per modificare utente
        if ("ADMIN".equals(parent.currentUserRole)) {
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = table.getSelectedRow();
                        if (row >= 0) {
                            Long userId = Long.parseLong(model.getValueAt(row, 0).toString());
                            String email = model.getValueAt(row, 1).toString();
                            String role  = model.getValueAt(row, 2).toString();
                            showEditUser(userId, email, role, model, row);
                        }
                    }
                }
            });
            JLabel hint = new JLabel("  ✏️ Doppio click su un utente per modificarlo", SwingConstants.LEFT);
            hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
            hint.setForeground(new Color(220, 200, 255));
            hint.setOpaque(true);
            hint.setBackground(new Color(110, 20, 180));
            hint.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            add(hint, BorderLayout.SOUTH);
        } else {
            JButton close = new JButton("Chiudi");
            close.setBackground(new Color(85, 0, 155));
            close.setForeground(Color.WHITE);
            close.setFocusPainted(false);
            close.setBorderPainted(false);
            close.addActionListener(ev -> dispose());
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
            footer.setBackground(new Color(90, 0, 160));
            footer.add(close);
            add(footer, BorderLayout.SOUTH);
        }

        add(title,  BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void showEditUser(Long id, String email, String role, DefaultTableModel model, int row) {
        JDialog edit = new JDialog(this, "Modifica utente", true);
        edit.setSize(340, 250);
        edit.setLocationRelativeTo(this);
        edit.getContentPane().setBackground(new Color(120, 60, 190));
        edit.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField emailField = new JTextField(email, 18);
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"USER", "ADMIN"});
        roleBox.setSelectedItem(role);

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
                String json = String.format("{\"email\":\"%s\",\"role\":\"%s\"}",
                        emailField.getText().trim(), roleBox.getSelectedItem());
                ApiClient.put("/users/" + id, json);
                model.setValueAt(emailField.getText().trim(), row, 1);
                model.setValueAt(roleBox.getSelectedItem(), row, 2);
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
}
