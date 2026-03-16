package com.bugboard.bugboard26.ui;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DashboardFrame extends JFrame {

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Titolo", "Tipo", "Priorità", "Stato", "Creata da"}, 0
    ) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField(15);
    private final JComboBox<String> statusFilter = new JComboBox<>(
            new String[]{"Tutti", "TODO", "IN_PROGRESS", "DONE"}
    );
    private final JComboBox<String> priorityFilter = new JComboBox<>(
            new String[]{"Tutti", "LOW", "MEDIUM", "HIGH"}
    );

    public DashboardFrame() {
        setTitle("BugBoard26 — Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.add(new JLabel("Cerca:"));
        toolbar.add(searchField);
        toolbar.add(new JLabel("Stato:"));
        toolbar.add(statusFilter);
        toolbar.add(new JLabel("Priorità:"));
        toolbar.add(priorityFilter);

        JButton searchBtn = new JButton("Filtra");
        JButton newIssueBtn = new JButton("+ Nuova Issue");
        JButton refreshBtn = new JButton("Aggiorna");
        toolbar.add(searchBtn);
        toolbar.add(newIssueBtn);
        toolbar.add(refreshBtn);

        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(table);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton detailBtn = new JButton("Apri Issue");
        south.add(detailBtn);

        add(toolbar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> loadIssues());
        refreshBtn.addActionListener(e -> loadIssues());
        newIssueBtn.addActionListener(e -> {
            new IssueFormDialog(this, null).setVisible(true);
            loadIssues();
        });
        detailBtn.addActionListener(e -> openSelected());
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openSelected();
            }
        });

        loadIssues();
    }

    void loadIssues() {
        tableModel.setRowCount(0);
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                StringBuilder url = new StringBuilder("/issues?");
                String kw = searchField.getText().trim();
                if (!kw.isEmpty()) url.append("keyword=").append(kw).append("&");
                String st = (String) statusFilter.getSelectedItem();
                if (!"Tutti".equals(st)) url.append("status=").append(st).append("&");
                String pr = (String) priorityFilter.getSelectedItem();
                if (!"Tutti".equals(pr)) url.append("priority=").append(pr).append("&");
                return ApiClient.get(url.toString());
            }

            @Override
            protected void done() {
                try {
                    JsonNode arr = ApiClient.mapper.readTree(get());
                    for (JsonNode issue : arr) {
                        tableModel.addRow(new Object[]{
                                issue.get("id").asLong(),
                                issue.get("title").asText(),
                                issue.get("type").asText(),
                                issue.get("priority").asText(),
                                issue.get("status").asText(),
                                issue.path("createdBy").path("email").asText()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DashboardFrame.this,
                            "Errore nel caricamento: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void openSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona una issue prima");
            return;
        }
        Long id = (Long) tableModel.getValueAt(row, 0);
        new IssueFormDialog(this, id).setVisible(true);
        loadIssues();
    }
}
