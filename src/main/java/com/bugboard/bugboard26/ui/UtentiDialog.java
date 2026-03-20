package com.bugboard.bugboard26.ui;

import com.fasterxml.jackson.databind.JsonNode;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class UtentiDialog extends JDialog {

    public UtentiDialog(Frame parent) {
        super(parent, "Tutti gli Utenti", true);
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

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Email", "Ruolo"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);

        try {
            String resp = ApiClient.get("/users");
            JsonNode arr = ApiClient.mapper.readTree(resp);
            for (JsonNode u : arr) {
                model.addRow(new Object[]{
                        u.get("id").asText(),
                        u.get("email").asText(),
                        u.path("role").asText("USER")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scroll.getViewport().setBackground(new Color(160, 110, 210));

        JButton close = new JButton("Chiudi");
        close.setBackground(new Color(85, 0, 155));
        close.setForeground(Color.WHITE);
        close.setFocusPainted(false);
        close.setBorderPainted(false);
        close.addActionListener(e -> dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(new Color(90, 0, 160));
        footer.add(close);

        add(title,  BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
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
