package boundary;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import javax.swing.border.EmptyBorder;

import javax.swing.table.DefaultTableModel;

import control.FitnessPlanControl;

// PanelManagePlans serves as the dashboard for managing existing Fitness Plans.

public class PanelManagePlans extends JPanel {
    //  UI Components 
	private static final long serialVersionUID = 1L;
    private static final String ALL_STATUSES = "All Statuses";
    private static final String ALL_TYPES = "All Types";
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");
    private DefaultTableModel model;
    private JTable table;
    private JComboBox<String> cmbFilter;     // Status filter
    private JComboBox<String> cmbTypeFilter; // Type filter

    public PanelManagePlans() {
        setLayout(new BorderLayout(0, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 80, 40));
        DesignUtils.styleMainPanel(this);

        // Title and Dual Filters 
        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        DesignUtils.styleCardPanel(topPanel);

        JLabel lblTitle = new JLabel("Manage Training Plans", SwingConstants.CENTER);
        DesignUtils.styleTitle(lblTitle);
        topPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        filterPanel.setOpaque(false);

        // Status Filter
        JLabel lblFilter = new JLabel("Status:");
        DesignUtils.styleFilterLabel(lblFilter);

        String[] statuses = {ALL_STATUSES, "Active", "Paused", "Completed", "Cancelled"};
        cmbFilter = new JComboBox<>(statuses);
        cmbFilter.setPreferredSize(new Dimension(160, 32));
        DesignUtils.styleFilterComboBox(cmbFilter);
        cmbFilter.addActionListener(e -> loadPlansToTable());

        // Type Filter
        JLabel lblTypeFilter = new JLabel("Type:");
        DesignUtils.styleFilterLabel(lblTypeFilter);

        String[] types = {ALL_TYPES, "Personal", "Group"};
        cmbTypeFilter = new JComboBox<>(types);
        cmbTypeFilter.setPreferredSize(new Dimension(160, 32));
        DesignUtils.styleFilterComboBox(cmbTypeFilter);
        cmbTypeFilter.addActionListener(e -> loadPlansToTable());

        filterPanel.add(lblFilter);
        filterPanel.add(cmbFilter);
        filterPanel.add(lblTypeFilter);
        filterPanel.add(cmbTypeFilter);

        topPanel.add(filterPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Data Table 
        String[] columns = {"Select", "Plan ID", "Type", "Start Date", "Duration", "End Date", "Status"};

        // Custom model enforces strict checkbox-based single selection
        model = new DefaultTableModel(new Object[][]{}, columns) {
            private static final long serialVersionUID = 1L;

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; 
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (column == 0) {
                    boolean newVal = Boolean.TRUE.equals(aValue);

                    if (newVal) {
                        for (int i = 0; i < getRowCount(); i++) {
                            if (i != row) super.setValueAt(false, i, 0);
                        }
                        super.setValueAt(true, row, 0);
                        if (table != null) table.getSelectionModel().setSelectionInterval(row, row);
                    } else {
                        super.setValueAt(false, row, 0);
                        if (table != null) table.clearSelection();
                    }
                    return;
                }
                super.setValueAt(aValue, row, column);
            }
        };

        table = new JTable(model);
        DesignUtils.styleTableCentered(table);
        table.setFocusable(false); 

        JScrollPane sp = new JScrollPane(table);
        DesignUtils.styleScrollPane(sp);

        JPanel tableCard = new JPanel(new BorderLayout());
        DesignUtils.styleCardPanel(tableCard);
        tableCard.add(sp, BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);

        // Action Controls 
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        DesignUtils.styleCardPanel(bottomPanel);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btnEdit = new JButton("Edit Selected Plan");
        DesignUtils.styleButton(btnEdit);
        btnEdit.setPreferredSize(new Dimension(250, 50));
        btnEdit.addActionListener(e -> openEditDialog());

        bottomPanel.add(btnEdit);
        add(bottomPanel, BorderLayout.SOUTH);

        // Initialize table
        loadPlansToTable();
    }

    // Validates selection state and enforces business rules before delegating the edit process to a dedicated Dialog window.
     
    private void openEditDialog() {
        int selectedRow = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (Boolean.TRUE.equals(model.getValueAt(i, 0))) {
                selectedRow = i;
                break;
            }
        }

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a plan to edit.");
            return;
        }

        // Business Logic Validation 
        // Immutable states Completed or Cancelled plans cannot be altered.
        String status = String.valueOf(model.getValueAt(selectedRow, 6));
        if ("Completed".equalsIgnoreCase(status) || "Cancelled".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Cannot edit plans that are already Completed or Cancelled.",
                    "Access Denied",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Extract required plan meta-data for the Edit Dialog
        String id = String.valueOf(model.getValueAt(selectedRow, 1));
        String type = String.valueOf(model.getValueAt(selectedRow, 2));

        Date date;
        try {
            date = DATE_FMT.parse(String.valueOf(model.getValueAt(selectedRow, 3)));
        } catch (ParseException ex) {
            date = new Date(); 
        }

        int duration;
        try {
            duration = Integer.parseInt(String.valueOf(model.getValueAt(selectedRow, 4)));
        } catch (NumberFormatException ex) {
            duration = 0;
        }

        // Deploy dialog component
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        EditPlanDialog dialog = new EditPlanDialog(parentFrame, id, type, date, duration, status);
        dialog.setVisible(true); // Thread pauses here until dialog is closed

        // Refresh UI to reflect dialog updates
        loadPlansToTable();
    }

    private void loadPlansToTable() {
        model.setRowCount(0); 

        ArrayList<Object[]> plans = FitnessPlanControl.getInstance().getAllPlansWithType();

        String selectedStatus = (String) cmbFilter.getSelectedItem();
        String selectedType = (String) cmbTypeFilter.getSelectedItem();

        if (selectedStatus == null) selectedStatus = ALL_STATUSES;
        if (selectedType == null) selectedType = ALL_TYPES;

        for (Object[] row : plans) {
            String rowType = String.valueOf(row[2]).trim();
            String rowStatus = String.valueOf(row[5]).trim();

            boolean statusMatch = ALL_STATUSES.equals(selectedStatus) || rowStatus.equalsIgnoreCase(selectedStatus);
            boolean typeMatch = ALL_TYPES.equals(selectedType) || rowType.equalsIgnoreCase(selectedType);

            if (statusMatch && typeMatch) {
                Date startDate = (Date) row[3];
                int duration = (int) row[4];

                //  Dynamic End Date Calculation 
                Calendar cal = Calendar.getInstance();
                cal.setTime(startDate);
                cal.add(Calendar.DAY_OF_MONTH, duration);

                String endDate = DATE_FMT.format(cal.getTime());
                String startDateStr = DATE_FMT.format(startDate);

                Object[] newRow = {false, row[1], row[2], startDateStr, row[4], endDate, row[5]};
                model.addRow(newRow);
            }
        }

        table.clearSelection();
    }
}