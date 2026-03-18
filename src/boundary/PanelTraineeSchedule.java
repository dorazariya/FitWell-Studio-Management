package boundary;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import control.TraineeRegisterControl;
import control.TrainingClassControl;
import control.UsersControl;
import entity.ClassType;
import entity.TrainingClass;

// PanelTraineeSchedule acts as a personal dashboard for the logged in Trainee.
 
public class PanelTraineeSchedule extends JPanel {
    private static final long serialVersionUID = 1L;

    // Constants for filter default states
    private static final String ALL_TYPES = "All Types";
    private static final String ALL_STATUSES = "All Statuses";
    private static final String STATUS_SCHEDULED = "SCHEDULED";

    // UI Components
    private DefaultTableModel model;
    private JTable table;
    private JSpinner dateSpinner;
    private JCheckBox chkEnableDateFilter;
    private JComboBox<String> cmbFilter;
    private JComboBox<String> cmbStatusFilter;

    public PanelTraineeSchedule() {
        setLayout(new BorderLayout(0, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 80, 40));

        // Apply consistent system design
        DesignUtils.styleMainPanel(this);

        // Title & Filters 
        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        DesignUtils.styleCardPanel(topPanel);

        JLabel lblTitle = new JLabel("My Schedule & History", SwingConstants.CENTER);
        DesignUtils.styleTitle(lblTitle);
        topPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        filterPanel.setOpaque(false);

        // Date Filter Setup
        chkEnableDateFilter = new JCheckBox("Filter by Date:");
        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy"));
        dateSpinner.setPreferredSize(new Dimension(140, 32));
        dateSpinner.setEnabled(false); // Disabled until checkbox is toggled

        // Auto refresh table when date filters change
        chkEnableDateFilter.addActionListener(e -> {
            dateSpinner.setEnabled(chkEnableDateFilter.isSelected());
            loadSchedule();
        });
        dateSpinner.addChangeListener(e -> loadSchedule());

        //  Type & Status Filter Setup
        JLabel lblFilter = new JLabel("Type:");
        JLabel lblStatusFilter = new JLabel("Status:");

        cmbFilter = new JComboBox<>();
        cmbFilter.setPreferredSize(new Dimension(190, 32));
        populateFilterCombo();
        cmbFilter.addActionListener(e -> loadSchedule());

        cmbStatusFilter = new JComboBox<>(new String[]{ALL_STATUSES, STATUS_SCHEDULED, "COMPLETED"});
        cmbStatusFilter.setPreferredSize(new Dimension(170, 32));
        cmbStatusFilter.addActionListener(e -> loadSchedule());

        // Apply styling to all filter components
        DesignUtils.styleFilterCheckBox(chkEnableDateFilter);
        DesignUtils.styleFilterSpinner(dateSpinner);
        DesignUtils.styleFilterLabel(lblFilter);
        DesignUtils.styleFilterComboBox(cmbFilter);
        DesignUtils.styleFilterLabel(lblStatusFilter);
        DesignUtils.styleFilterComboBox(cmbStatusFilter);

        // Assemble filter panel
        filterPanel.add(chkEnableDateFilter);
        filterPanel.add(dateSpinner);
        filterPanel.add(Box.createHorizontalStrut(15));
        filterPanel.add(lblFilter);
        filterPanel.add(cmbFilter);
        filterPanel.add(Box.createHorizontalStrut(15));
        filterPanel.add(lblStatusFilter);
        filterPanel.add(cmbStatusFilter);

        topPanel.add(filterPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Schedule Data Table 
        String[] columns = {"Select", "ID", "Class Name", "Type", "Date & Time", "Status"};
        
        // Custom TableModel for Checkbox rendering and single selection enforcement
        model = new DefaultTableModel(new Object[][]{}, columns) {
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
                // Ensure only one row can be checked at a time for cancellation
                if (column == 0) {
                    boolean newVal = Boolean.TRUE.equals(aValue);

                    if (newVal) {
                        for (int i = 0; i < getRowCount(); i++) {
                            if (i != row) super.setValueAt(false, i, 0);
                        }
                        super.setValueAt(true, row, 0);
                        if (table != null) {
                            table.getSelectionModel().setSelectionInterval(row, row);
                        }
                    } else {
                        super.setValueAt(false, row, 0);
                        if (table != null) {
                            table.clearSelection();
                        }
                    }
                    return;
                }
                super.setValueAt(aValue, row, column);
            }
        };

        table = new JTable(model);
        DesignUtils.styleTableCentered(table);

        JScrollPane sp = new JScrollPane(table);
        DesignUtils.styleScrollPane(sp);

        JPanel tableCard = new JPanel(new BorderLayout());
        DesignUtils.styleCardPanel(tableCard);
        tableCard.add(sp, BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);

        // Action Buttons 
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        DesignUtils.styleCardPanel(bottomPanel);

        JButton btnCancel = new JButton("Cancel Registration");
        DesignUtils.styleButton(btnCancel);
        btnCancel.setPreferredSize(new Dimension(250, 46));
        btnCancel.addActionListener(e -> cancelRegistration()); // Triggers cancellation process

        bottomPanel.add(btnCancel);
        add(bottomPanel, BorderLayout.SOUTH);

        // Fetch initial data
        loadSchedule();

        // Ensure table doesn't steal focus immediately upon loading
        SwingUtilities.invokeLater(() -> table.requestFocusInWindow());
    }

    // Dynamically populates the class type dropdown filter from the database.
     
    private void populateFilterCombo() {
        cmbFilter.addItem(ALL_TYPES);
        ArrayList<ClassType> types = TrainingClassControl.getInstance().getAllClassTypes();
        for (ClassType type : types) {
            cmbFilter.addItem(type.getTypeName());
        }
    }

    // Fetches the specific schedule for the logged-in trainee from the Control layer,applies the active UI filters (Date, Type, Status), and populates the table.
     
    private void loadSchedule() {
        if (model == null) return;

        model.setRowCount(0); // Clear table before reloading
        int traineeID = UsersControl.getInstance().getLoggedInUserId();
        if (traineeID == -1) return; // Prevent execution if no user is logged in

        // Extract filter values
        Date selectedDate = (Date) dateSpinner.getValue();
        java.time.LocalDate filterDate = new java.sql.Date(selectedDate.getTime()).toLocalDate();

        String selectedType = (String) cmbFilter.getSelectedItem();
        if (selectedType == null) selectedType = ALL_TYPES;

        String selectedStatus = (String) cmbStatusFilter.getSelectedItem();
        if (selectedStatus == null) selectedStatus = ALL_STATUSES;

        // Fetch user's registered classes
        ArrayList<TrainingClass> classes = TrainingClassControl.getInstance().getTraineeSchedule(traineeID);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (TrainingClass tc : classes) {
            // Apply Type filter
            if (!selectedType.equals(ALL_TYPES) && !tc.getTypeName().equals(selectedType)) {
                continue;
            }

            // Apply Status filter
            if (!selectedStatus.equals(ALL_STATUSES) && !tc.getStatus().toString().equals(selectedStatus)) {
                continue;
            }

            // Apply Date filter
            if (chkEnableDateFilter.isSelected()) {
                if (!tc.getStartTime().toLocalDate().equals(filterDate)) {
                    continue;
                }
            }

            // Add valid row to table
            model.addRow(new Object[]{
                    false, 
                    tc.getClassID(),
                    tc.getName(),
                    tc.getTypeName(),
                    tc.getStartTime().format(fmt),
                    tc.getStatus()
            });
        }
    }

    /**
     * Handles the process of a trainee canceling their registration.
     * Enforces business logic: only upcoming (SCHEDULED) classes can be canceled.
     */
    private void cancelRegistration() {
        // Locate selected row
        int selectedRow = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if ((Boolean) model.getValueAt(i, 0)) {
                selectedRow = i;
                break;
            }
        }

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a class to cancel.");
            return;
        }

        // Business Logic Validation 
        // Trainees cannot cancel classes that are already COMPLETED, ACTIVE, or SUSPENDED.
        Object statusObj = model.getValueAt(selectedRow, 5);
        String statusStr = statusObj.toString();

        if (!STATUS_SCHEDULED.equals(statusStr)) {
            JOptionPane.showMessageDialog(this, "You can only cancel future SCHEDULED classes.");
            return;
        }

        int classID = (int) model.getValueAt(selectedRow, 1);
        int traineeID = UsersControl.getInstance().getLoggedInUserId();

        // Require user confirmation before dropping the registration
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel class " + classID + " ?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        // Delegate cancellation transaction to the Control layer
        try {
            boolean ok = TraineeRegisterControl.getInstance().cancelTraineeRegistration(classID, traineeID);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Cancellation successful.");
                loadSchedule(); // Refresh view to remove the canceled class
            } else {
                JOptionPane.showMessageDialog(this, "Cancellation failed.");
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cancellation Rule", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "System error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}