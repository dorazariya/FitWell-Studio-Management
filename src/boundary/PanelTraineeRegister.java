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
import javax.swing.table.DefaultTableModel;

import control.TraineeRegisterControl;
import control.TrainingClassControl;
import control.UsersControl;
import entity.ClassType;
import entity.TrainingClass;

// PanelTraineeRegister provides the interface for a Trainee to browse available training classes and register for them.
 
public class PanelTraineeRegister extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final String ALL_TYPES = "All Types";

    // UI Components
    private DefaultTableModel model;
    private JTable table;
    private JSpinner dateSpinner;
    private JCheckBox chkEnableDateFilter;
    private JComboBox<String> cmbFilter;

    public PanelTraineeRegister() {
        setLayout(new BorderLayout(0, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 80, 40));

        // Apply consistent system background styling
        DesignUtils.styleMainPanel(this);

        // Title & Filtering Mechanism 
        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        DesignUtils.styleCardPanel(topPanel);

        JLabel lblTitle = new JLabel("Register for Class", SwingConstants.CENTER);
        DesignUtils.styleTitle(lblTitle);
        topPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        filterPanel.setOpaque(false);

        // Date Filter Setup
        chkEnableDateFilter = new JCheckBox("Filter by Date:");
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setPreferredSize(new Dimension(140, 32));
        dateSpinner.setEnabled(false); 

        // Listeners to refresh table dynamically when filters change
        chkEnableDateFilter.addActionListener(e -> {
            dateSpinner.setEnabled(chkEnableDateFilter.isSelected());
            loadAvailableClasses();
        });
        dateSpinner.addChangeListener(e -> loadAvailableClasses());

        // Class Type Filter Setup
        JLabel lblFilter = new JLabel("Filter by Type:");
        cmbFilter = new JComboBox<>();
        cmbFilter.setPreferredSize(new Dimension(210, 32));
        populateFilterCombo();
        cmbFilter.addActionListener(e -> loadAvailableClasses());

        // Apply styling to filter components
        DesignUtils.styleFilterCheckBox(chkEnableDateFilter);
        DesignUtils.styleFilterSpinner(dateSpinner);
        DesignUtils.styleFilterLabel(lblFilter);
        DesignUtils.styleFilterComboBox(cmbFilter);

        filterPanel.add(chkEnableDateFilter);
        filterPanel.add(dateSpinner);
        filterPanel.add(Box.createHorizontalStrut(15));
        filterPanel.add(lblFilter);
        filterPanel.add(cmbFilter);

        topPanel.add(filterPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Classes Data Table 
        String[] columns = {"Select", "ID", "Class Name", "Type", "Date & Time", "Participants (Reg/Max)"};
        
        // Custom TableModel to enforce Checkbox single selection
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
                // Ensure only one class can be selected at a time
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
        
        // Optimize column widths for better readability
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(40);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);
        
        JScrollPane sp = new JScrollPane(table);
        DesignUtils.styleScrollPane(sp);

        JPanel tableCard = new JPanel(new BorderLayout());
        DesignUtils.styleCardPanel(tableCard);
        tableCard.add(sp, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);

        // Action Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        DesignUtils.styleCardPanel(bottomPanel);

        JButton btnRegister = new JButton("Register Selected");
        DesignUtils.styleButton(btnRegister);
        btnRegister.setPreferredSize(new Dimension(250, 46));
        btnRegister.addActionListener(e -> registerSelected());

        bottomPanel.add(btnRegister);
        add(bottomPanel, BorderLayout.SOUTH);

        // Initial Data Load
        loadAvailableClasses();
    }

    	// Populates the Class Type filter dropdown from the database.
     
    private void populateFilterCombo() {
        cmbFilter.addItem(ALL_TYPES);
        ArrayList<ClassType> types = TrainingClassControl.getInstance().getAllClassTypes();
        for (ClassType type : types) {
            cmbFilter.addItem(type.getTypeName());
        }
    }

    //Fetches classes available specifically to the logged in trainee, and filters the view based on the UI selections (Date/Type).
     
    private void loadAvailableClasses() {
        model.setRowCount(0); 
        
        int traineeID = UsersControl.getInstance().getLoggedInUserId();
        if (traineeID == -1) return; 

        Date selectedDate = (Date) dateSpinner.getValue();
        java.time.LocalDate filterDate = new java.sql.Date(selectedDate.getTime()).toLocalDate();

        // Retrieve only classes this trainee is eligible for via Control layer
        ArrayList<TrainingClass> allClasses =
                TrainingClassControl.getInstance().getAvailableClassesForTrainee(traineeID);

        String selectedType = (String) cmbFilter.getSelectedItem();
        if (selectedType == null) selectedType = ALL_TYPES;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (TrainingClass tc : allClasses) {
            // Apply Class Type Filter
            if (!selectedType.equals(ALL_TYPES) && !tc.getTypeName().equals(selectedType)) {
                continue;
            }

            // Apply Date Filter
            if (chkEnableDateFilter.isSelected()) {
                if (!tc.getStartTime().toLocalDate().equals(filterDate)) {
                    continue;
                }
            }

            // Calculate current registration capacity dynamically
            int current = TraineeRegisterControl.getInstance().getParticipantCount(tc.getClassID());
            String participantsStr = current + " / " + tc.getMaxParticipants();

            model.addRow(new Object[]{
                    false, 
                    tc.getClassID(),
                    tc.getName(),
                    tc.getTypeName(),
                    tc.getStartTime().format(fmt) + " - " + tc.getEndTime().toLocalTime(),
                    participantsStr
            });
        }
    }

    // Handles the class registration sequence: validates selection, 
    //asks for confirmation, and delegates the transaction to the Control layer.
     
    private void registerSelected() {
        // Find which row is checked
        int selectedRow = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if ((Boolean) model.getValueAt(i, 0)) {
                selectedRow = i;
                break;
            }
        }

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a class to register.");
            return;
        }

        // Extract class details for the confirmation message
        int classID = (int) model.getValueAt(selectedRow, 1);
        String className = (String) model.getValueAt(selectedRow, 2);
        String dateTime = (String) model.getValueAt(selectedRow, 4);

        String message = String.format(
                "Are you sure you want to register for the following class?\n\n" +
                        "Class: %s\n" +
                        "Time: %s",
                className, dateTime
        );

        int confirm = JOptionPane.showConfirmDialog(
                this,
                message,
                "Confirm Registration",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            int traineeID = UsersControl.getInstance().getLoggedInUserId();

            try {
                boolean success = TraineeRegisterControl.getInstance()
                        .registerTraineeToClass(classID, traineeID);

                if (success) {
                    JOptionPane.showMessageDialog(this, "Successfully registered!");
                    loadAvailableClasses(); 
                } else {
                    JOptionPane.showMessageDialog(this, "Registration failed. Check rules.");
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "System error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}