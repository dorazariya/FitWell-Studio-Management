package boundary;

import java.awt.Color;
import java.awt.Font;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import control.TraineeRegisterControl;
import control.UsersControl;
import entity.Trainee;
import entity.UpdateMethod;

// PanelTraineeProfile represents the dashboard where a logged in Trainee 
public class PanelTraineeProfile extends JPanel {
    private static final long serialVersionUID = 1L;

    // UI Layout Constants 
    private static final int TITLE_X = 33;
    private static final int TITLE_Y = 30;
    private static final int TITLE_W = 400;
    private static final int TITLE_H = 40;

    private static final int START_X = 83;
    private static final int START_Y = 100;
    private static final int GAP_Y   = 50;

    private static final int LABEL_W = 150;
    private static final int FIELD_W = 300;
    private static final int ROW_H   = 30;

    // UI Components 
    private JTextField txtID, txtFirstName, txtLastName, txtEmail, txtPhone;
    private JSpinner spinBirthDate;
    private JRadioButton radioEmail, radioSMS;
    private JButton btnEdit, btnCancel;

    // State flag to track whether the user is currently editing their profile
    private boolean isEditing = false;

    public PanelTraineeProfile() {
        setLayout(null); 
        DesignUtils.styleMainPanel(this);

        JLabel lblTitle = new JLabel("My Profile", SwingConstants.CENTER);
        DesignUtils.styleTitle(lblTitle);
        lblTitle.setBounds(TITLE_X, TITLE_Y, TITLE_W, TITLE_H);
        lblTitle.setOpaque(false);
        add(lblTitle);

        // Trainee ID Strictly Read Only
        add(formLabel("ID:", START_X, START_Y));
        txtID = formField(START_X + LABEL_W, START_Y, false);
        applyReadOnlyIdStyle(txtID); // Applies a visual "locked" style
        add(txtID);

        // Visual indicator that the ID cannot be changed
        JLabel lockIcon = new JLabel("🔒");
        lockIcon.setBounds(START_X + LABEL_W + FIELD_W + 10, START_Y, 30, ROW_H);
        lockIcon.setOpaque(false);
        add(lockIcon);

        JLabel roNote = new JLabel("Read-only");
        roNote.setFont(DesignUtils.FONT_TEXT.deriveFont(Font.PLAIN, 14f));
        roNote.setForeground(new Color(150, 150, 150));
        roNote.setBounds(START_X + LABEL_W + FIELD_W + 40, START_Y, 100, ROW_H);
        roNote.setOpaque(false);
        add(roNote);

        // First Name 
        add(formLabel("First Name:", START_X, START_Y + GAP_Y));
        txtFirstName = formField(START_X + LABEL_W, START_Y + GAP_Y, false);
        add(txtFirstName);

        //  Last Name 
        add(formLabel("Last Name:", START_X, START_Y + GAP_Y * 2));
        txtLastName = formField(START_X + LABEL_W, START_Y + GAP_Y * 2, false);
        add(txtLastName);

        // Birth Date 
        add(formLabel("Birth Date:", START_X, START_Y + GAP_Y * 3));
        spinBirthDate = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinBirthDate, "dd/MM/yyyy");
        spinBirthDate.setEditor(dateEditor);
        spinBirthDate.setBounds(START_X + LABEL_W, START_Y + GAP_Y * 3, FIELD_W, ROW_H);
        spinBirthDate.setEnabled(false);
        DesignUtils.styleFilterSpinner(spinBirthDate);
        add(spinBirthDate);

        // Email 
        add(formLabel("Email:", START_X, START_Y + GAP_Y * 4));
        txtEmail = formField(START_X + LABEL_W, START_Y + GAP_Y * 4, false);
        add(txtEmail);

        // Phone 
        add(formLabel("Phone:", START_X, START_Y + GAP_Y * 5));
        txtPhone = formField(START_X + LABEL_W, START_Y + GAP_Y * 5, false);
        add(txtPhone);

        // Preferred Update Method 
        add(formLabel("Update Method:", START_X, START_Y + GAP_Y * 6));

        radioEmail = createRadio("Email", START_X + LABEL_W, START_Y + GAP_Y * 6);
        radioSMS   = createRadio("SMS",   START_X + LABEL_W + 110, START_Y + GAP_Y * 6);

        ButtonGroup bg = new ButtonGroup();
        bg.add(radioEmail);
        bg.add(radioSMS);

        add(radioEmail);
        add(radioSMS);

        // Action Buttons 
        btnEdit = new JButton("Edit Profile");
        btnEdit.setBounds(START_X, START_Y + GAP_Y * 8, 200, 50);
        DesignUtils.styleButton(btnEdit);
        btnEdit.addActionListener(e -> toggleEditMode()); // Handles switching states
        add(btnEdit);

        btnCancel = new JButton("Cancel");
        btnCancel.setBounds(START_X + 220, START_Y + GAP_Y * 8, 150, 50);
        DesignUtils.styleButton(btnCancel);
        btnCancel.setVisible(false); 
        btnCancel.addActionListener(e -> cancelEdit());
        add(btnCancel);

        loadTraineeData();
    }

    //  UI Factory Methods 
    private JLabel formLabel(String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        DesignUtils.styleFilterLabel(lbl);
        lbl.setOpaque(false);
        lbl.setBounds(x, y, LABEL_W, ROW_H);
        return lbl;
    }

    private JTextField formField(int x, int y, boolean editable) {
        JTextField tf = new JTextField();
        tf.setBounds(x, y, FIELD_W, ROW_H);
        tf.setEditable(editable);
        DesignUtils.styleFilterTextField(tf);
        return tf;
    }

    private JRadioButton createRadio(String text, int x, int y) {
        JRadioButton rb = new JRadioButton(text);
        rb.setBounds(x, y, 100, ROW_H);
        rb.setEnabled(false);
        rb.setOpaque(false);
        rb.setFont(DesignUtils.FONT_TEXT);
        rb.setForeground(DesignUtils.TEXT_MAIN);
        rb.setFocusable(false);
        return rb;
    }

    // Applies a specific visual style to the ID field to clearly indicate it cannot be changed.
    private void applyReadOnlyIdStyle(JTextField tf) {
        tf.setEnabled(false);
        tf.setFocusable(false);
        tf.setDisabledTextColor(new Color(120, 120, 120));
        tf.setBackground(new Color(235, 235, 235));
        tf.setBorder(new CompoundBorder(
                new LineBorder(new Color(180, 180, 180), 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
    }

    // Retrieves the currently logged in user from the session and populates the form.
    private void loadTraineeData() {
        Object user = UsersControl.getInstance().getLoggedInUser();
        if (!(user instanceof Trainee)) return;

        Trainee t = (Trainee) user;

        txtID.setText(String.valueOf(t.getId()));
        txtFirstName.setText(t.getFirstName());
        txtLastName.setText(t.getLastName());
        txtEmail.setText(t.getEmail());
        txtPhone.setText(t.getPhone());

        if (t.getBirthDate() != null) {
            spinBirthDate.setValue(t.getBirthDate());
        }

        if (t.getUpdateMethod() == UpdateMethod.Email) {
            radioEmail.setSelected(true);
        } else {
            radioSMS.setSelected(true);
        }
    }

    private void toggleEditMode() {
        //  State Transition
        if (!isEditing) {
            isEditing = true;
            btnEdit.setText("Save Changes");
            btnCancel.setVisible(true); 

            // Unlock editable fields
            txtFirstName.setEditable(true);
            txtLastName.setEditable(true);
            txtEmail.setEditable(true);
            txtPhone.setEditable(true);
            spinBirthDate.setEnabled(true);
            radioEmail.setEnabled(true);
            radioSMS.setEnabled(true);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to save the changes to your profile?",
                "Confirm Update",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            spinBirthDate.commitEdit();
        } catch (java.text.ParseException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date format (dd/MM/yyyy).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(txtID.getText());
        String fName = normalizeName(txtFirstName.getText());
        String lName = normalizeName(txtLastName.getText());
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        Date dob = (Date) spinBirthDate.getValue();
        UpdateMethod method = radioEmail.isSelected() ? UpdateMethod.Email : UpdateMethod.SMS;

        String error = TraineeRegisterControl.getInstance().validateTraineeDetails(fName, lName, email, phone, dob);

        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Database Update
        boolean success = TraineeRegisterControl.getInstance()
                .updateTraineeProfile(id, fName, lName, dob, email, phone, method);

        if (!success) {
            JOptionPane.showMessageDialog(this, "Error updating profile in Database.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

        // Update Active Session Data
        UsersControl.getInstance().updateSessionTrainee(id, fName, lName, dob, email, phone, method);

        exitEditMode();
    }

    private String normalizeName(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ");
    }

    private void exitEditMode() {
        isEditing = false;
        btnEdit.setText("Edit Profile");
        btnCancel.setVisible(false);

        txtFirstName.setEditable(false);
        txtLastName.setEditable(false);
        txtEmail.setEditable(false);
        txtPhone.setEditable(false);
        spinBirthDate.setEnabled(false);
        radioEmail.setEnabled(false);
        radioSMS.setEnabled(false);
    }

    private void cancelEdit() {
        exitEditMode();
        loadTraineeData();
    }
}