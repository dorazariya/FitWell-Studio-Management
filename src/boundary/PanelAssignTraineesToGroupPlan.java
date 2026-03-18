package boundary;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import control.FitnessPlanControl;
import control.TraineeRegisterControl;
import entity.GroupPlan;
import entity.Trainee;

// PanelAssignTraineesToGroupPlan allows the system manager to select an active Group Plan and assign eligible trainees to it. It includes real-time filtering and business-rule validation.
 
public class PanelAssignTraineesToGroupPlan extends JPanel {
    private static final long serialVersionUID = 1L;

    // UI Styling Constants 
    private static final Font TITLE_FONT      = new Font("Arial", Font.BOLD, 36);
    private static final Font LABEL_FONT      = new Font("Arial", Font.BOLD, 18);
    private static final Font INPUT_FONT      = new Font("Arial", Font.PLAIN, 18);
    private static final Font TABLE_FONT      = new Font("Arial", Font.PLAIN, 16);
    private static final Font HEADER_FONT     = new Font("Arial", Font.BOLD, 16);
    private static final Font BUTTON_FONT     = new Font("Arial", Font.BOLD, 20);
    private static final Font DETAIL_LABEL_FONT = new Font("Arial", Font.BOLD, 16);
    private static final Font DETAIL_VALUE_FONT = new Font("Arial", Font.BOLD, 16);
    private static final Color HIGHLIGHT_COLOR  = new Color(0, 102, 204);

    private static final javax.swing.border.LineBorder BORDER =
            new javax.swing.border.LineBorder(DesignUtils.GRID, 1);

    // UI Components 
    private JComboBox<String> cmbGroupPlan;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter; // Handles real time search filtering
    private JTextField txtSearch;
    private JLabel lblPlanIdValue, lblStatusValue, lblStartDateValue, lblDurationValue, lblAgeRangeValue;
    private ArrayList<GroupPlan> activeGroupPlans;

    public PanelAssignTraineesToGroupPlan() {
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(18, 33, 18, 33));
        DesignUtils.styleMainPanel(this);

        add(buildTop(), BorderLayout.NORTH);

        JPanel tableAndButtonPanel = new JPanel(new BorderLayout(0, 10));
        tableAndButtonPanel.setOpaque(false);
        tableAndButtonPanel.add(buildCenter(), BorderLayout.NORTH);
        tableAndButtonPanel.add(buildBottom(), BorderLayout.CENTER);

        add(tableAndButtonPanel, BorderLayout.CENTER);

        // Fetch active plans to initialize the interface
        loadPlansData();
    }

    // Builds the top section Plan Selection ComboBox and the Summary Data grid.
     
    private JPanel buildTop() {
        JPanel top = new JPanel(new BorderLayout(0, 15));
        top.setOpaque(false);

        JLabel title = new JLabel("Add Trainees to Group Plan", SwingConstants.LEFT);
        title.setFont(TITLE_FONT);
        title.setForeground(DesignUtils.TEXT_MAIN);
        top.add(title, BorderLayout.NORTH);

        JPanel topContent = new JPanel(new BorderLayout());
        topContent.setOpaque(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);

        JLabel lblPlan = new JLabel("Choose Group Plan:  ");
        lblPlan.setFont(LABEL_FONT);
        lblPlan.setForeground(DesignUtils.TEXT_MAIN);

        cmbGroupPlan = new JComboBox<>();
        cmbGroupPlan.setPreferredSize(new Dimension(250, 30));
        DesignUtils.styleFilterComboBox(cmbGroupPlan);
        cmbGroupPlan.setFont(INPUT_FONT);
        cmbGroupPlan.setBorder(BORDER);

        leftPanel.add(lblPlan);
        leftPanel.add(cmbGroupPlan);
        topContent.add(leftPanel, BorderLayout.WEST);

        JPanel detailsWrapper = new JPanel(new BorderLayout());
        detailsWrapper.setOpaque(false);
        detailsWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 150));
        detailsWrapper.add(buildSimpleDetailsPanel(), BorderLayout.CENTER);
        topContent.add(detailsWrapper, BorderLayout.EAST);

        top.add(topContent, BorderLayout.CENTER);
        return top;
    }

    private JPanel buildSimpleDetailsPanel() {
        JPanel details = new JPanel(new GridLayout(3, 4, 15, 5));
        details.setOpaque(false);

        lblPlanIdValue    = new JLabel("-"); lblPlanIdValue.setFont(DETAIL_VALUE_FONT);    lblPlanIdValue.setForeground(HIGHLIGHT_COLOR);
        lblStatusValue    = new JLabel("-"); lblStatusValue.setFont(DETAIL_VALUE_FONT);    lblStatusValue.setForeground(HIGHLIGHT_COLOR);
        lblStartDateValue = new JLabel("-"); lblStartDateValue.setFont(DETAIL_VALUE_FONT); lblStartDateValue.setForeground(HIGHLIGHT_COLOR);
        lblDurationValue  = new JLabel("-"); lblDurationValue.setFont(DETAIL_VALUE_FONT);  lblDurationValue.setForeground(HIGHLIGHT_COLOR);
        lblAgeRangeValue  = new JLabel("-"); lblAgeRangeValue.setFont(DETAIL_VALUE_FONT);  lblAgeRangeValue.setForeground(HIGHLIGHT_COLOR);

        details.add(createBlueLabel("Plan ID:"));    details.add(lblPlanIdValue);
        details.add(createBlueLabel("Status:"));     details.add(lblStatusValue);
        details.add(createBlueLabel("Start Date:")); details.add(lblStartDateValue);
        details.add(createBlueLabel("Duration:"));   details.add(lblDurationValue);
        details.add(createBlueLabel("Age Range:"));  details.add(lblAgeRangeValue);
        details.add(new JLabel("")); details.add(new JLabel(""));
        return details;
    }

    private JLabel createBlueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(DETAIL_LABEL_FONT);
        label.setForeground(HIGHLIGHT_COLOR);
        return label;
    }

    // Builds the interactive table and the real time search bar mechanism.
     
    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setOpaque(false);

        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setOpaque(false);

        JLabel lblTable = new JLabel("Select Trainees to Add:");
        lblTable.setFont(LABEL_FONT);
        lblTable.setForeground(DesignUtils.TEXT_MAIN);
        tableHeaderPanel.add(lblTable, BorderLayout.WEST);

        //  Real time Search Panel 
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Search by First Name: ");
        lblSearch.setFont(new Font("Arial", Font.BOLD, 16));
        lblSearch.setForeground(DesignUtils.TEXT_MAIN);

        txtSearch = new JTextField(15);
        DesignUtils.styleFilterTextField(txtSearch);
        txtSearch.setFont(TABLE_FONT);

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        tableHeaderPanel.add(searchPanel, BorderLayout.EAST);
        center.add(tableHeaderPanel, BorderLayout.NORTH);
        
        // Data Table Setup
        String[] cols = {"Select", "Trainee ID", "First Name", "Last Name", "Email", "Phone", "Update Method"};
        model = new DefaultTableModel(new Object[][]{}, cols) {
            private static final long serialVersionUID = 1L;

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return Boolean.class; 
                    case 1: return Integer.class; 
                    default: return String.class;
                }
            }

            @Override
            public boolean isCellEditable(int row, int col) { return col == 0; }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                super.setValueAt(aValue, row, column);
                // Synchronize logic Keep internal model and UI selection highlighting aligned
                if (column == 0 && table != null) {
                    boolean isChecked = Boolean.TRUE.equals(aValue);
                    if (isChecked) {
                        table.getSelectionModel().addSelectionInterval(row, row);
                    } else {
                        table.getSelectionModel().removeSelectionInterval(row, row);
                    }
                }
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        DesignUtils.styleTableCentered(table);
        table.setFont(TABLE_FONT);
        table.setRowHeight(27);

        JTableHeader header = table.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setReorderingAllowed(false);
        header.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        table.setDefaultRenderer(Integer.class, table.getDefaultRenderer(Object.class));

        // Search Filter Logic 
        sorter = new TableRowSorter<>(model);
        for (int i = 0; i < cols.length; i++) sorter.setSortable(i, false); // Prevent column sorting to maintain stability
        table.setRowSorter(sorter);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText();
                sorter.setRowFilter(text.trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + text, 2));
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BORDER);
        sp.setPreferredSize(new Dimension(1100, 400));
        DesignUtils.styleScrollPane(sp);

        center.add(sp, BorderLayout.CENTER);
        return center;
    }

    private JPanel buildBottom() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));

        JButton btnAdd = new JButton("Add Selected Trainees");
        btnAdd.setPreferredSize(new Dimension(320, 42));
        DesignUtils.styleButton(btnAdd);
        btnAdd.setFont(BUTTON_FONT);
        btnAdd.addActionListener(e -> onAddSelected());

        bottom.add(btnAdd);
        return bottom;
    }

    // Extracts selected Trainee IDs and delegates batch assignment to the Control layer.
     
    private void onAddSelected() {
        int selectedPlanIndex = cmbGroupPlan.getSelectedIndex();
        if (selectedPlanIndex < 0) return;

        GroupPlan selectedPlan = activeGroupPlans.get(selectedPlanIndex);
        int planId = selectedPlan.getPlanID();

        // Scan the table model to find all checked rows
        ArrayList<Integer> selectedTraineeIds = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (Boolean.TRUE.equals(model.getValueAt(i, 0))) {
                selectedTraineeIds.add((Integer) model.getValueAt(i, 1)); 
            }
        }

        if (selectedTraineeIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one trainee.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Add " + selectedTraineeIds.size() + " trainees to plan #" + planId + "?",
                "Confirm Addition", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = FitnessPlanControl.getInstance().assignTraineesToPlan(planId, selectedTraineeIds);
            if (success) {
                JOptionPane.showMessageDialog(this, "Successfully added participants!", "Success", JOptionPane.INFORMATION_MESSAGE);
                // Refresh table to exclude trainees that were just assigned
                loadEligibleTraineesForPlan(selectedPlan);
                txtSearch.setText("");
            }
        }
    }

     // Fetches all Active Group Plans and initializes the ComboBox listener.
     // Triggers dynamic UI updates when a new plan is selected.
    
    public void loadPlansData() {
        activeGroupPlans = FitnessPlanControl.getInstance().getActiveGroupPlans();

        cmbGroupPlan.removeAllItems();
        for (GroupPlan plan : activeGroupPlans) {
            cmbGroupPlan.addItem("Plan #" + plan.getPlanID() + " (Ages " + plan.getAgeRangeString() + ")");
        }

        cmbGroupPlan.addActionListener(e -> {
            int idx = cmbGroupPlan.getSelectedIndex();
            if (idx >= 0) {
                GroupPlan selectedPlan = activeGroupPlans.get(idx);
                setSelectedPlanSummary(
                        String.valueOf(selectedPlan.getPlanID()),
                        selectedPlan.getStatus().name(),
                        selectedPlan.getStartDate().toLocalDate()
                                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        selectedPlan.getDuration() + " days",
                        selectedPlan.getAgeRangeString()
                );
                loadEligibleTraineesForPlan(selectedPlan);
            }
        });

        if (cmbGroupPlan.getItemCount() > 0) cmbGroupPlan.setSelectedIndex(0); // Trigger initial load
    }

    //Refreshes the table, pulling only trainees who meet the plan's age requirement and are not already assigned to it.

    private void loadEligibleTraineesForPlan(GroupPlan plan) {
        model.setRowCount(0);
        // Delegate age and assignment filtering to Control layer
        ArrayList<Trainee> eligibleTrainees =
                TraineeRegisterControl.getInstance().getEligibleTraineesForGroupPlan(
                        plan.getPlanID(), plan.getMinAge(), plan.getMaxAge());

        for (Trainee t : eligibleTrainees) {
            model.addRow(new Object[]{
                    false, t.getId(), t.getFirstName(), t.getLastName(),
                    t.getEmail(), t.getPhone(), t.getUpdateMethod().name()
            });
        }
    }

    // Utility method to update the labels displaying the plan details.
     
    public void setSelectedPlanSummary(String planId, String status, String startDate, String duration, String ageRange) {
        lblPlanIdValue.setText(safe(planId));
        lblStatusValue.setText(safe(status));
        lblStartDateValue.setText(safe(startDate));
        lblDurationValue.setText(safe(duration));
        lblAgeRangeValue.setText(safe(ageRange));
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }
}