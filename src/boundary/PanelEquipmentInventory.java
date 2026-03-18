package boundary;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import javax.swing.border.EmptyBorder;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import javax.swing.table.DefaultTableModel;

import control.EquipmentControl;
import control.ImportControl;

// PanelEquipmentInventory displays a high-level overview of all physical equipment in the studio, categorized by availability status.

public class PanelEquipmentInventory extends JPanel {
    private static final long serialVersionUID = 1L;

    // UI Components 
    private JTable table;
    private DefaultTableModel model;

    public PanelEquipmentInventory() {
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(30, 40, 30, 40));
        DesignUtils.styleMainPanel(this);

        initComponents();

        // Auto Refresh Logic 
        // Uses AncestorListener to automatically fetch fresh data from the DB 
        // every time this panel becomes visible on the screen.
        this.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                refreshTable(); 
            }
             public void ancestorRemoved(AncestorEvent event) {}
             public void ancestorMoved(AncestorEvent event) { }
        });
    }

    private void initComponents() {
        //  Title 
        JPanel topCard = new JPanel(new BorderLayout());
        DesignUtils.styleCardPanel(topCard);

        JLabel lblTitle = new JLabel("Equipment Stock Overview", SwingConstants.CENTER);
        DesignUtils.styleTitle(lblTitle);
        topCard.add(lblTitle, BorderLayout.CENTER);

        add(topCard, BorderLayout.NORTH);

        // Data Table 
        String[] columns = {"Equipment Type", "Category", "Total in Studio", "Available", "Out of Service"};
        
        model = new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        DesignUtils.styleTableCentered(table);

        JScrollPane scrollPane = new JScrollPane(table);
        DesignUtils.styleScrollPane(scrollPane);

        JPanel tableCard = new JPanel(new BorderLayout());
        DesignUtils.styleCardPanel(tableCard);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);

        // Action Buttons 
        JPanel bottomCard = new JPanel(new FlowLayout(FlowLayout.CENTER));
        DesignUtils.styleCardPanel(bottomCard);

        JButton btnImport = new JButton("Import JSON Data");
        DesignUtils.styleButton(btnImport);
        btnImport.setPreferredSize(new Dimension(220, 45));

        //  JSON Import Integration 
        btnImport.addActionListener(e -> {
            try {
                JOptionPane.showMessageDialog(this, "Reading JSON...", "Processing", JOptionPane.INFORMATION_MESSAGE);
                
                // Delegate parsing and DB insertion to Control layer
                ImportControl.getInstance().importDefaultJson();
                
                JOptionPane.showMessageDialog(this, "Import completed successfully!\nRecords updated.");
                refreshTable(); // Synchronize UI with new DB state
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error importing JSON: " + ex.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomCard.add(btnImport);
        add(bottomCard, BorderLayout.SOUTH);
    }

    // Clears the current table and re-populates it by requesting an aggregated stock summary from the EquipmentControl layer.
     
    public void refreshTable() {
        if (model == null) return;

        model.setRowCount(0); 
        try {
            // Fetch real time aggregated data
            ArrayList<Object[]> rows = EquipmentControl.getInstance().getEquipmentStockSummary();
            for (Object[] row : rows) {
                model.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}