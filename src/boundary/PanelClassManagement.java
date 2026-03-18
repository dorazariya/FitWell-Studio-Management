package boundary;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import control.TraineeRegisterControl;
import control.TrainingClassControl;

import entity.Trainee;
import entity.TrainingClass;

// Main panel for managing training classes.
// Supports filtering by status, type, and date.
// Actions available: Add, Reschedule, Register/Unregister Trainee, Cancel, Manage Equipment.
// Action buttons are enabled or disabled based on the selected class status.
public class PanelClassManagement extends JPanel {
    private static final long serialVersionUID = 1L;
    private javax.swing.Timer autoRefreshTimer; //Field for auto refresh
    
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cmbStatusFilter;
    private JComboBox<String> cmbTypeFilter;
    private JSpinner dateSpinner;
    private JCheckBox chkEnableDateFilter;

    private JButton btnAdd, btnReschedule, btnRegister, btnUnregister, btnCancel, btnManageEquipment;

    public PanelClassManagement() {
        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        DesignUtils.styleMainPanel(this);

        JPanel pnlTop = new JPanel(new GridLayout(2, 1, 0, 10));
        DesignUtils.styleCardPanel(pnlTop);

        JLabel lblTitle = new JLabel("Training Classes Management", SwingConstants.CENTER);
        DesignUtils.styleTitle(lblTitle);
        pnlTop.add(lblTitle);

        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        pnlFilter.setOpaque(false);

        chkEnableDateFilter = new JCheckBox("Filter by Date:");
        DesignUtils.styleFilterCheckBox(chkEnableDateFilter);

        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy"));
        dateSpinner.setPreferredSize(new Dimension(120, 30));
        dateSpinner.setEnabled(false);
        DesignUtils.styleFilterSpinner(dateSpinner);

        // Date filter is only active when the checkbox is checked
        chkEnableDateFilter.addActionListener(e -> {
            dateSpinner.setEnabled(chkEnableDateFilter.isSelected());
            refreshTable();
        });
        dateSpinner.addChangeListener(e -> refreshTable());

        JLabel lblStatus = new JLabel("Status:");
        DesignUtils.styleFilterLabel(lblStatus);

        cmbStatusFilter = new JComboBox<>();
        cmbStatusFilter.setPreferredSize(new Dimension(130, 30));
        cmbStatusFilter.addItem("All");
        for (String s : TrainingClassControl.getInstance().getAllStatusNames()) cmbStatusFilter.addItem(s);
        DesignUtils.styleFilterComboBox(cmbStatusFilter);
        cmbStatusFilter.addActionListener(e -> refreshTable());

        JLabel lblType = new JLabel("Type:");
        DesignUtils.styleFilterLabel(lblType);

        cmbTypeFilter = new JComboBox<>();
        cmbTypeFilter.setPreferredSize(new Dimension(150, 30));
        populateTypeCombo();
        DesignUtils.styleFilterComboBox(cmbTypeFilter);
        cmbTypeFilter.addActionListener(e -> refreshTable());

        pnlFilter.add(chkEnableDateFilter);
        pnlFilter.add(dateSpinner);
        pnlFilter.add(Box.createHorizontalStrut(15));
        pnlFilter.add(lblType);
        pnlFilter.add(cmbTypeFilter);
        pnlFilter.add(Box.createHorizontalStrut(15));
        pnlFilter.add(lblStatus);
        pnlFilter.add(cmbStatusFilter);

        pnlTop.add(pnlFilter);
        add(pnlTop, BorderLayout.NORTH);

        String[] columns = {"Select", "ID", "Name", "Type", "Start Time", "End Time", "Participants (Reg/Max)", "Status"};
        model = new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 0) ? Boolean.class : super.getColumnClass(columnIndex);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }

            // Enforces single selection — checking a row unchecks all others
            @Override
            public void setValueAt(Object aValue, int row, int column) {
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
                        if (table != null) table.clearSelection();
                    }
                    return;
                }
                super.setValueAt(aValue, row, column);
            }
        };

        table = new JTable(model);
        DesignUtils.styleTableCentered(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Clicking a non-checkbox column still toggles the checkbox for that row
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0 || col < 0) return;

                if (col == 0) {
                    boolean current = Boolean.TRUE.equals(model.getValueAt(row, 0));
                    model.setValueAt(!current, row, 0);
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        DesignUtils.styleScrollPane(sp);

        JPanel tableCard = new JPanel(new BorderLayout());
        DesignUtils.styleCardPanel(tableCard);
        tableCard.add(sp, BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        DesignUtils.styleCardPanel(pnlButtons);

        btnAdd = new JButton("Add New Class");
        btnReschedule = new JButton("Reschedule");
        btnRegister = new JButton("Register Trainee");
        btnUnregister = new JButton("Cancel Registration");
        btnCancel = new JButton("Cancel Class");
        btnManageEquipment = new JButton("Manage Equipment");

        Dimension btnSize = new Dimension(200, 45);
        for (JButton btn : new JButton[]{btnAdd, btnReschedule, btnRegister, btnUnregister, btnCancel, btnManageEquipment}) {
            DesignUtils.styleButton(btn);
            btn.setPreferredSize(btnSize);
        }

        pnlButtons.add(btnAdd);
        pnlButtons.add(btnReschedule);
        pnlButtons.add(btnRegister);
        pnlButtons.add(btnUnregister);
        pnlButtons.add(btnManageEquipment);
        pnlButtons.add(btnCancel);

        add(pnlButtons, BorderLayout.SOUTH);

        disableAllActionButtons();
        refreshTable();
        
        autoRefreshTimer = new javax.swing.Timer(2 * 60 * 1000, e -> {
            if (isShowing()) refreshTable();
        });
        autoRefreshTimer.start();

        // Re-evaluate button states whenever selection changes
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) checkSelection();
        });
        model.addTableModelListener(e -> checkSelection());

        btnAdd.addActionListener(e ->
                new AddClassDialog((Frame) SwingUtilities.getWindowAncestor(this), this::refreshTable).setVisible(true));

        btnRegister.addActionListener(e -> showRegisterTraineeDialog());
        btnUnregister.addActionListener(e -> showUnregisterDialog());
        btnReschedule.addActionListener(e -> showRescheduleDialog());
        btnCancel.addActionListener(e -> handleCancelClass());
        btnManageEquipment.addActionListener(e -> showManageEquipmentDialog());
    }

    private void populateTypeCombo() {
        cmbTypeFilter.addItem("All Types");
        for (String name : TrainingClassControl.getInstance().getAllClassTypeNames()) cmbTypeFilter.addItem(name);
    }

    private void disableAllActionButtons() {
        if (btnRegister != null) btnRegister.setEnabled(false);
        if (btnUnregister != null) btnUnregister.setEnabled(false);
        if (btnReschedule != null) btnReschedule.setEnabled(false);
        if (btnCancel != null) btnCancel.setEnabled(false);
        if (btnManageEquipment != null) btnManageEquipment.setEnabled(false);
    }

    // Enables or disables action buttons based on the selected class status.
    // Only SCHEDULED classes allow full actions. SUSPENDED allows cancel only.
    private void checkSelection() {
        int selectedRow = getSelectedRowIndex();
        if (selectedRow != -1) {
            String status = getStatusFromRow(selectedRow);
            if ("SCHEDULED".equals(status)) {
                btnRegister.setEnabled(true);
                btnUnregister.setEnabled(true);
                btnReschedule.setEnabled(true);
                btnCancel.setEnabled(true);
                btnManageEquipment.setEnabled(true);
            } else if ("SUSPENDED".equals(status)) {
                disableAllActionButtons();
                btnCancel.setEnabled(true);
            } else {
                disableAllActionButtons();
            }
        } else {
            disableAllActionButtons();
        }
    }

    private int getSelectedRowIndex() {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (Boolean.TRUE.equals(model.getValueAt(i, 0))) return i;
        }
        return -1;
    }

    private String getStatusFromRow(int row) {
        Object s = model.getValueAt(row, 7);
        return (s != null) ? s.toString().trim().toUpperCase() : "";
    }

    // Reloads table data applying current filter selections.
    // Auto-status updates (SCHEDULED to ACTIVE/COMPLETED)
    private void refreshTable() {
        disableAllActionButtons();
        model.setRowCount(0);

        String selStatus = (String) cmbStatusFilter.getSelectedItem();
        String selType = (String) cmbTypeFilter.getSelectedItem();

        Date sDate = (Date) dateSpinner.getValue();
        java.time.LocalDate filterDate = new java.sql.Date(sDate.getTime()).toLocalDate();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (TrainingClass tc : TrainingClassControl.getInstance().getAllClasses()) {
            String statusStr = tc.getStatus().name();
            String typeStr = tc.getTypeName();

            if (selStatus != null && !selStatus.equals("All") && !statusStr.equals(selStatus)) continue;
            if (selType != null && !selType.equals("All Types") && !typeStr.equals(selType)) continue;
            if (chkEnableDateFilter.isSelected() && !tc.getStartTime().toLocalDate().equals(filterDate)) continue;

            int current = TraineeRegisterControl.getInstance().getParticipantCount(tc.getClassID());

            model.addRow(new Object[]{
                    false,
                    tc.getClassID(),
                    tc.getName(),
                    typeStr,
                    tc.getStartTime().format(formatter),
                    tc.getEndTime().format(formatter),
                    current + " / " + tc.getMaxParticipants(),
                    statusStr
            });
        }
        table.clearSelection();
    }

    // Opens ManageEquipmentDialog for the selected class to add or update equipment requirements
    private void showManageEquipmentDialog() {
        int selectedRow = getSelectedRowIndex();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a class first.");
            return;
        }
        try {
            int classID = (int) model.getValueAt(selectedRow, 1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime lStart = LocalDateTime.parse((String) model.getValueAt(selectedRow, 4), formatter);
            LocalDateTime lEnd = LocalDateTime.parse((String) model.getValueAt(selectedRow, 5), formatter);

            new ManageEquipmentDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    classID, lStart, lEnd,
                    this::refreshTable
            ).setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Shows a dialog to register an eligible trainee to the selected class.
    // Eligible trainees are those assigned to the class plan with no schedule conflicts.
    private void showRegisterTraineeDialog() {
        int selectedRow = getSelectedRowIndex();
        if (selectedRow == -1) return;

        int classID = (int) model.getValueAt(selectedRow, 1);
        ArrayList<Trainee> eligible = TraineeRegisterControl.getInstance().getEligibleTraineesForClass(classID);

        if (eligible.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No eligible trainees found.");
            return;
        }

        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register Trainee", true);
        d.setSize(400, 250);
        d.setLocationRelativeTo(this);
        d.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
        d.getContentPane().setBackground(DesignUtils.BG_MAIN);

        JLabel lbl = new JLabel("Select Trainee:");
        DesignUtils.styleFilterLabel(lbl);

        JComboBox<Trainee> cmbTrainees = new JComboBox<>(eligible.toArray(new Trainee[0]));
        cmbTrainees.setPreferredSize(new Dimension(300, 35));
        cmbTrainees.setSelectedIndex(-1);
        DesignUtils.styleFilterComboBox(cmbTrainees);

        JButton btnConfirm = new JButton("Register");
        DesignUtils.styleButton(btnConfirm);
        btnConfirm.setPreferredSize(new Dimension(150, 40));

        btnConfirm.addActionListener(e -> {
            Trainee selected = (Trainee) cmbTrainees.getSelectedItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(d, "Please select a trainee.");
                return;
            }

            if (TraineeRegisterControl.getInstance().registerTraineeToClass(classID, selected.getId())) {
                JOptionPane.showMessageDialog(d, "Successfully registered!");
                refreshTable();
                d.dispose();
            } else {
                JOptionPane.showMessageDialog(d, "Registration failed.");
            }
        });

        d.add(lbl);
        d.add(cmbTrainees);
        d.add(btnConfirm);
        d.setVisible(true);
    }

    // Shows a dialog to remove a registered trainee from the selected class
    private void showUnregisterDialog() {
        int selectedRow = getSelectedRowIndex();
        if (selectedRow == -1) return;

        int classID = (int) model.getValueAt(selectedRow, 1);
        ArrayList<Trainee> registered = TraineeRegisterControl.getInstance().getTraineesInClass(classID);

        if (registered.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No trainees registered.");
            return;
        }

        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Cancel Registration", true);
        d.setSize(400, 250);
        d.setLocationRelativeTo(this);
        d.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
        d.getContentPane().setBackground(DesignUtils.BG_MAIN);

        JLabel lbl = new JLabel("Select Trainee:");
        DesignUtils.styleFilterLabel(lbl);

        JComboBox<Trainee> cmbTrainees = new JComboBox<>(registered.toArray(new Trainee[0]));
        cmbTrainees.setPreferredSize(new Dimension(300, 35));
        cmbTrainees.setSelectedIndex(-1);
        DesignUtils.styleFilterComboBox(cmbTrainees);

        JButton btnConfirm = new JButton("Remove Trainee");
        DesignUtils.styleButton(btnConfirm);
        btnConfirm.setPreferredSize(new Dimension(180, 40));

        btnConfirm.addActionListener(e -> {
            Trainee selected = (Trainee) cmbTrainees.getSelectedItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(d, "Please select a trainee.");
                return;
            }

            if (TraineeRegisterControl.getInstance().cancelTraineeRegistration(classID, selected.getId())) {
                JOptionPane.showMessageDialog(d, "Trainee removed.");
                refreshTable();
                d.dispose();
            }
        });

        d.add(lbl);
        d.add(cmbTrainees);
        d.add(btnConfirm);
        d.setVisible(true);
    }

    private void handleCancelClass() {
        int selectedRow = getSelectedRowIndex();
        if (selectedRow == -1) return;

        int classID = (int) model.getValueAt(selectedRow, 1);
        int confirm = JOptionPane.showConfirmDialog(this, "Cancel class " + classID + "?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (TrainingClassControl.getInstance().cancelClass(classID)) {
                JOptionPane.showMessageDialog(this, "Class cancelled.");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Error cancelling class.");
            }
        }
    }

    // Opens a dialog with date/time spinners pre-filled with the current class schedule
    private void showRescheduleDialog() {
        int selectedRow = getSelectedRowIndex();
        if (selectedRow == -1) return;

        int classID = (int) model.getValueAt(selectedRow, 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        try {
            LocalDateTime lStart = LocalDateTime.parse((String) model.getValueAt(selectedRow, 4), formatter);
            LocalDateTime lEnd = LocalDateTime.parse((String) model.getValueAt(selectedRow, 5), formatter);

            Date dStart = Date.from(lStart.atZone(ZoneId.systemDefault()).toInstant());
            Date dEnd = Date.from(lEnd.atZone(ZoneId.systemDefault()).toInstant());

            JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Reschedule Class", true);
            d.setSize(400, 300);
            d.setLocationRelativeTo(this);
            d.setLayout(new GridLayout(0, 1, 10, 10));

            JPanel content = (JPanel) d.getContentPane();
            content.setBackground(DesignUtils.BG_MAIN);
            content.setBorder(new EmptyBorder(20, 20, 20, 20));

            JSpinner spStart = createDateSpinner(dStart);
            JSpinner spEnd = createDateSpinner(dEnd);
            DesignUtils.styleFilterSpinner(spStart);
            DesignUtils.styleFilterSpinner(spEnd);

            JLabel lblStart = new JLabel("New Start Time:");
            DesignUtils.styleFilterLabel(lblStart);

            JLabel lblEnd = new JLabel("New End Time:");
            DesignUtils.styleFilterLabel(lblEnd);

            d.add(lblStart);
            d.add(spStart);
            d.add(lblEnd);
            d.add(spEnd);

            JButton btnUpdate = new JButton("Update");
            DesignUtils.styleButton(btnUpdate);

            btnUpdate.addActionListener(ev -> {
                try {
                    LocalDateTime newStart = ((Date) spStart.getValue()).toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDateTime();

                    LocalDateTime newEnd = ((Date) spEnd.getValue()).toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDateTime();

                    if (TrainingClassControl.getInstance().rescheduleClass(classID, newStart, newEnd)) {
                        JOptionPane.showMessageDialog(d, "Class rescheduled!");
                        refreshTable();
                        d.dispose();
                    } else {
                        JOptionPane.showMessageDialog(d, "Update failed.");
                    }

                } catch (TrainingClassControl.EquipmentConflictException conflict) {
                    StringBuilder msg = new StringBuilder("Cannot reschedule! Conflicts detected:\n\n");
                    for (String c : conflict.getConflicts()) msg.append("  ").append(c).append("\n");
                    msg.append("\nPlease adjust the schedule or equipment requirements.");
                    JOptionPane.showMessageDialog(d, msg.toString(), "Conflict Detected", JOptionPane.ERROR_MESSAGE);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage());
                }
            });

            d.add(btnUpdate);
            d.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Creates a date/time spinner pre-initialized with the given date
    private JSpinner createDateSpinner(Date initDate) {
        SpinnerDateModel m = new SpinnerDateModel();
        m.setValue(initDate);

        JSpinner s = new JSpinner(m);
        JSpinner.DateEditor e = new JSpinner.DateEditor(s, "dd/MM/yyyy HH:mm");
        s.setEditor(e);

        e.getTextField().setFont(DesignUtils.FONT_TEXT);
        return s;
    }
    
    // Turning of the timer 
    @Override
    public void removeNotify() {
        if (autoRefreshTimer != null) autoRefreshTimer.stop();
        super.removeNotify();
    }
}