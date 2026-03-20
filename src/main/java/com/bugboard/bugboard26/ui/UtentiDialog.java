package com.bugboard.bugboard26.ui;

import com.bugboard.bugboard26.service.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class UtentiDialog extends JDialog {

    private final ApiClient apiClient;
    private DefaultTableModel tableModel;

    public UtentiDialog(Frame parent, ApiClient apiClient) {
        super(parent, "Utenti", true);
        this.apiClient = apiClient;
        setSize(550, 380);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"ID", "Email", "Ruolo"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);

        JButton btnClose = new JButton("Chiudi");
        btnClose.addActionListener(e -> dispose());
        JPanel footer = new JPanel();
        footer.add(btnClose);
        add(footer, BorderLayout.SOUTH);

        loadUsers();
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        try {
            JSONArray users = new JSONArray(apiClient.get("/api/users"));
            for (int i = 0; i < users.length(); i++) {
                JSONObject u = users.getJSONObject(i);
                tableModel.addRow(new Object[]{
                        u.getLong("id"),
                        u.getString("email"),
                        u.getString("role")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
        }
    }
}
