package boundary;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import control.TraineeRegisterControl;
import entity.Trainee;
import entity.UpdateMethod;

// Panel for the Studio Manager to view and edit existing trainee profiles.
// Displays all trainees in a table. Selecting one and clicking Edit opens a dialog to modify their details.
public class PanelStudioManagerManageTrainees extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    private DefaultTableModel model;
    private JTable table;

    public PanelStudioManagerManageTrainees() {
        setLayout(new BorderLayout(0, 20));
        setBorder(BorderFactory.createEmptyBorder(25, 40, 40, 40));
        DesignUtils.styleMainPanel(this);

        JPanel topCard = new JPanel(new BorderLayout());
        DesignUtils.styleCardPanel(topCard);
        JLabel title = new JLabel("Manage Trainees", SwingConstants.CENTER);
        DesignUtils.styleTitle(title);
        topCard.add(title, BorderLayout.CENTER);
        add(topCard, BorderLayout.NORTH);

        String[] cols = {"Select", "ID", "First Name", "Last Name", "Birth Date", "Email", "Phone", "Update Method"};
        model = new DefaultTableModel(new Object[][]{}, cols) {
            private static final long serialVersionUID = 1L;

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 0;
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

        loadTraineesData();

        JPanel bottomCard = new JPanel(new FlowLayout(FlowLayout.CENTER));
        DesignUtils.styleCardPanel(bottomCard);

        JButton btnEdit = new JButton("Edit Selected");
        DesignUtils.styleButton(btnEdit);
        btnEdit.setPreferredSize(new Dimension(190, 50));
        btnEdit.addActionListener(e -> openEditDialog());

        bottomCard.add(btnEdit);
        add(bottomCard, BorderLayout.SOUTH);
    }

    // Loads all trainees from the database
    private void loadTraineesData() {
        model.setRowCount(0);
        ArrayList<Trainee> trainees = TraineeRegisterControl.getInstance().getAllTrainees();
        for (Trainee t : trainees) {
            model.addRow(new Object[]{
                    false,
                    String.valueOf(t.getId()),
                    t.getFirstName(),
                    t.getLastName(),
                    t.getBirthDate() != null ? DATE_FORMAT.format(t.getBirthDate()) : "",
                    t.getEmail(),
                    t.getPhone(),
                    t.getUpdateMethod().name()
            });
        }
    }

    // Opens an edit dialog pre-filled with the selected trainee's current details.
    // Validates input before saving and reloads the table on success.
    private void openEditDialog() {
        int selectedRow = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (Boolean.TRUE.equals(model.getValueAt(i, 0))) {
                selectedRow = i;
                break;
            }
        }

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a trainee to edit.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt((String) model.getValueAt(selectedRow, 1));
        String fName = (String) model.getValueAt(selectedRow, 2);
        String lName = (String) model.getValueAt(selectedRow, 3);
        String dobStr = (String) model.getValueAt(selectedRow, 4);
        String email = (String) model.getValueAt(selectedRow, 5);
        String phone = (String) model.getValueAt(selectedRow, 6);
        String methodStr = (String) model.getValueAt(selectedRow, 7);

        Date dobDate;
        try {
            dobDate = DATE_FORMAT.parse(dobStr);
        } catch (Exception e) {
            dobDate = new Date();
        }

        JDialog d = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), "Edit Trainee", true);
        d.setSize(500, 480);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 15, 20));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        formPanel.setBackground(DesignUtils.BG_MAIN);

        JTextField txtFirst = new JTextField(fName);
        JTextField txtLast = new JTextField(lName);
        JTextField txtEmail = new JTextField(email);
        JTextField txtPhone = new JTextField(phone);

        DesignUtils.styleFilterTextField(txtFirst);
        DesignUtils.styleFilterTextField(txtLast);
        DesignUtils.styleFilterTextField(txtEmail);
        DesignUtils.styleFilterTextField(txtPhone);

        JSpinner spinDob = new JSpinner(new SpinnerDateModel());
        spinDob.setEditor(new JSpinner.DateEditor(spinDob, "dd/MM/yyyy"));
        spinDob.setValue(dobDate);
        DesignUtils.styleFilterSpinner(spinDob);

        JComboBox<String> cmbMethod = new JComboBox<>(new String[]{"Email", "SMS"});
        cmbMethod.setSelectedItem(methodStr);
        DesignUtils.styleFilterComboBox(cmbMethod);

        JLabel lblFirst = new JLabel("First Name:");
        DesignUtils.styleFilterLabel(lblFirst);
        JLabel lblLast = new JLabel("Last Name:");
        DesignUtils.styleFilterLabel(lblLast);
        JLabel lblDob = new JLabel("Birth Date:");
        DesignUtils.styleFilterLabel(lblDob);
        JLabel lblEmail = new JLabel("Email:");
        DesignUtils.styleFilterLabel(lblEmail);
        JLabel lblPhone = new JLabel("Phone:");
        DesignUtils.styleFilterLabel(lblPhone);
        JLabel lblMethod = new JLabel("Update Method:");
        DesignUtils.styleFilterLabel(lblMethod);

        formPanel.add(lblFirst);
        formPanel.add(txtFirst);
        formPanel.add(lblLast);
        formPanel.add(txtLast);
        formPanel.add(lblDob); 
        formPanel.add(spinDob);
        formPanel.add(lblEmail);
        formPanel.add(txtEmail);
        formPanel.add(lblPhone);
        formPanel.add(txtPhone);
        formPanel.add(lblMethod);
        formPanel.add(cmbMethod);

        d.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(DesignUtils.BG_MAIN);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JButton btnSave = new JButton("Save Changes");
        DesignUtils.styleButton(btnSave);
        btnSave.setPreferredSize(new Dimension(180, 40));

        btnSave.addActionListener(ev -> {
            int confirm = JOptionPane.showConfirmDialog(d,
                    "Are you sure you want to save the changes to this trainee?",
                    "Confirm Update", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                spinDob.commitEdit();
            } catch (java.text.ParseException ex) {
                JOptionPane.showMessageDialog(d, "Please enter a valid date format (dd/MM/yyyy).", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String newFName = txtFirst.getText().trim();
            String newLName = txtLast.getText().trim();
            String newEmail = txtEmail.getText().trim();
            String newPhone = txtPhone.getText().trim();
            Date newDob = (Date) spinDob.getValue();
            UpdateMethod newMethod = "Email".equals(cmbMethod.getSelectedItem()) ? UpdateMethod.Email : UpdateMethod.SMS;

            // Delegate validation to control layer before attempting update
            String error = TraineeRegisterControl.getInstance().validateTraineeDetails(newFName, newLName, newEmail, newPhone, newDob);

            if (error != null) {
                JOptionPane.showMessageDialog(d, error, "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (TraineeRegisterControl.getInstance().updateTraineeProfile(id, newFName, newLName, newDob, newEmail, newPhone, newMethod)) {
                JOptionPane.showMessageDialog(d, "Trainee updated successfully!");
                loadTraineesData();
                d.dispose();
            } else {
                JOptionPane.showMessageDialog(d, "Error updating trainee.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(btnSave);
        d.add(buttonPanel, BorderLayout.SOUTH);
        d.setVisible(true);
    }
}