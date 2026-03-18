package boundary;

import java.awt.BorderLayout;
import java.util.Date;

import javax.swing.BorderFactory;
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

import control.TraineeRegisterControl;
import entity.Trainee;
import entity.UpdateMethod;

// Panel for the Studio Manager to register new trainees into the system.
// Collects personal details and preferred update method, validates input, then saves to the database.
public class PanelStudioManagerRegisterTrainee extends JPanel {
    private static final long serialVersionUID = 1L;

    private JTextField txtFirst, txtLast, txtEmail, txtPhone;
    private JSpinner spinDob;
    private JRadioButton radioEmail, radioSms;

    public PanelStudioManagerRegisterTrainee() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(18, 0, 20, 0));
        DesignUtils.styleMainPanel(this);

        JPanel form = new JPanel(null);
        form.setOpaque(false);
        add(form, BorderLayout.CENTER);

        final int LABEL_X = 33;
        final int FIELD_X = 230;
        final int TITLE_Y = 30;
        final int LABEL_W = 190;
        final int FIELD_W = 220;
        final int FIELD_H = 30;
        final int ROW_H = 50;
        int y = 105;

        JLabel title = new JLabel("Register New Trainee", SwingConstants.LEFT);
        DesignUtils.styleTitle(title);
        title.setBounds(LABEL_X, TITLE_Y, 700, 40);
        form.add(title);

        JLabel lblFirst = new JLabel("First Name:");
        DesignUtils.styleFilterLabel(lblFirst);
        lblFirst.setBounds(LABEL_X, y, LABEL_W, FIELD_H);
        form.add(lblFirst);

        txtFirst = new JTextField();
        txtFirst.setBounds(FIELD_X, y, FIELD_W, FIELD_H);
        DesignUtils.styleFilterTextField(txtFirst);
        form.add(txtFirst);

        y += ROW_H;
        JLabel lblLast = new JLabel("Last Name:");
        DesignUtils.styleFilterLabel(lblLast);
        lblLast.setBounds(LABEL_X, y, LABEL_W, FIELD_H);
        form.add(lblLast);

        txtLast = new JTextField();
        txtLast.setBounds(FIELD_X, y, FIELD_W, FIELD_H);
        DesignUtils.styleFilterTextField(txtLast);
        form.add(txtLast);

        y += ROW_H;
        JLabel lblDob = new JLabel("Date of Birth:");
        DesignUtils.styleFilterLabel(lblDob);
        lblDob.setBounds(LABEL_X, y, LABEL_W, FIELD_H);
        form.add(lblDob);

        spinDob = new JSpinner(new SpinnerDateModel());
        spinDob.setEditor(new JSpinner.DateEditor(spinDob, "dd/MM/yyyy"));
        spinDob.setBounds(FIELD_X, y, FIELD_W, FIELD_H);
        DesignUtils.styleFilterSpinner(spinDob);
        form.add(spinDob);

        y += ROW_H;
        JLabel lblEmail = new JLabel("Email:");
        DesignUtils.styleFilterLabel(lblEmail);
        lblEmail.setBounds(LABEL_X, y, LABEL_W, FIELD_H);
        form.add(lblEmail);

        txtEmail = new JTextField();
        txtEmail.setBounds(FIELD_X, y, FIELD_W, FIELD_H);
        DesignUtils.styleFilterTextField(txtEmail);
        form.add(txtEmail);

        y += ROW_H;
        JLabel lblPhone = new JLabel("Phone Number:");
        DesignUtils.styleFilterLabel(lblPhone);
        lblPhone.setBounds(LABEL_X, y, LABEL_W, FIELD_H);
        form.add(lblPhone);

        txtPhone = new JTextField();
        txtPhone.setBounds(FIELD_X, y, FIELD_W, FIELD_H);
        DesignUtils.styleFilterTextField(txtPhone);
        form.add(txtPhone);

        y += ROW_H;
        JLabel lblMethod = new JLabel("Update Method:");
        DesignUtils.styleFilterLabel(lblMethod);
        lblMethod.setBounds(LABEL_X, y, LABEL_W, FIELD_H);
        form.add(lblMethod);

        radioEmail = new JRadioButton("Email");
        radioEmail.setFont(DesignUtils.FONT_TEXT);
        radioEmail.setForeground(DesignUtils.TEXT_MAIN);
        radioEmail.setOpaque(false);
        radioEmail.setFocusable(false);
        radioEmail.setBounds(FIELD_X, y, 110, FIELD_H);
        form.add(radioEmail);

        radioSms = new JRadioButton("SMS");
        radioSms.setFont(DesignUtils.FONT_TEXT);
        radioSms.setForeground(DesignUtils.TEXT_MAIN);
        radioSms.setOpaque(false);
        radioSms.setFocusable(false);
        radioSms.setBounds(FIELD_X + 120, y, 90, FIELD_H);
        form.add(radioSms);

        // Group ensures only one update method can be selected at a time
        ButtonGroup bg = new ButtonGroup();
        bg.add(radioEmail);
        bg.add(radioSms);
        radioEmail.setSelected(true);

        JButton btnSave = new JButton("Save Trainee");
        btnSave.setBounds(LABEL_X, y + 70, 190, 44);
        DesignUtils.styleButton(btnSave);
        btnSave.addActionListener(e -> saveTrainee());
        form.add(btnSave);

        JButton btnClear = new JButton("Clear");
        btnClear.setBounds(LABEL_X + 210, y + 70, 150, 44);
        DesignUtils.styleButton(btnClear);
        btnClear.addActionListener(e -> clearFields());
        form.add(btnClear);
    }

    // Validates input via control layer before creating and saving the new trainee
    private void saveTrainee() {
        String fName = txtFirst.getText().trim();
        String lName = txtLast.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        Date dob = (Date) spinDob.getValue();
        UpdateMethod method = radioEmail.isSelected() ? UpdateMethod.Email : UpdateMethod.SMS;

        String error = TraineeRegisterControl.getInstance().validateTraineeDetails(fName, lName, email, phone, dob);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
        	    this,
        	    "Are you sure you want to save the following trainee?\n\n" +
        	    "Name: " + fName + " " + lName + "\n" +
        	    "Date of Birth: " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(dob) + "\n" +
        	    "Email: " + email + "\n" +
        	    "Phone: " + phone + "\n" +
        	    "Update Method: " + method.name(),
        	    "Confirm Registration",
        	    JOptionPane.YES_NO_OPTION,
        	    JOptionPane.QUESTION_MESSAGE
        	);
        	if (confirm != JOptionPane.YES_OPTION) return;

        	try {
        	    if (TraineeRegisterControl.getInstance().addTrainee(fName, lName, dob, email, phone, method)) {
                JOptionPane.showMessageDialog(this, "Trainee added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add trainee. Please try again.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "System error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        txtFirst.setText("");
        txtLast.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        spinDob.setValue(new Date());
        radioEmail.setSelected(true);
    }
}