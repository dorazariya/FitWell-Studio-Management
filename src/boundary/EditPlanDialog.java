package boundary;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;

import javax.swing.border.LineBorder;

import control.FitnessPlanControl;
import control.TrainingClassControl;
import entity.ClassType;

// EditPlanDialog handles the UI and user input for modifying existing Fitness Plans.
 
public class EditPlanDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    // UI Components
    private JSpinner spStartDate;
    private JTextField txtDuration;
    private JComboBox<String> cmbStatus;
    private JButton btnSave;
    private JTextArea txtGoals;
    private JLabel lblGoals;
    private JTextField txtMinAge, txtMaxAge;
    private JTextArea txtGuide;
    private JLabel lblMinAge, lblMaxAge, lblGuide, lblClasses;
    private JPanel pnlCheckboxes;
    private ArrayList<JCheckBox> classTypeBoxes;
    private String planID;
    private String type;
    private int currentTraineeID = 0;

    public EditPlanDialog(JFrame parent, String planID, String type, Date startDate, int duration, String status) {
        super(parent, "Edit Plan: " + planID, true);
        this.planID = planID;
        this.type = type;

        setSize(600, 750);
        setLocationRelativeTo(parent);
        setLayout(null); 

        getContentPane().setBackground(DesignUtils.BG_MAIN);

        // Build UI based on plan type and current data
        initComponents(startDate, duration, status);
        loadSpecificDetails();
        applyBusinessRestrictions(status);
    }

    // Initializes the common fields and dynamically generates the specific fields depending on whether it is a Personal or Group plan.
     
    private void initComponents(Date startDate, int duration, String currentStatus) {
        int inputX = 180;

        JLabel lblInfo = new JLabel("Edit Plan: " + planID, SwingConstants.LEADING);
        DesignUtils.styleTitle(lblInfo);
        lblInfo.setBounds(30, 20, 520, 35);
        add(lblInfo);
        
        JLabel lblDate = new JLabel("Start Date:");
        DesignUtils.styleFilterLabel(lblDate);
        lblDate.setBounds(30, 80, 120, 30);
        add(lblDate);

        spStartDate = new JSpinner(new SpinnerDateModel());
        spStartDate.setValue(startDate);
        spStartDate.setEditor(new JSpinner.DateEditor(spStartDate, "dd/MM/yyyy"));
        spStartDate.setBounds(inputX, 80, 150, 30);
        DesignUtils.styleFilterSpinner(spStartDate);
        add(spStartDate);

        JLabel lblDur = new JLabel("Duration (days):");
        DesignUtils.styleFilterLabel(lblDur);
        lblDur.setBounds(30, 130, 140, 30);
        add(lblDur);

        txtDuration = new JTextField(String.valueOf(duration));
        txtDuration.setBounds(inputX, 130, 150, 30);
        DesignUtils.styleFilterTextField(txtDuration);
        add(txtDuration);

        JLabel lblStat = new JLabel("Status:");
        DesignUtils.styleFilterLabel(lblStat);
        lblStat.setBounds(30, 180, 120, 30);
        add(lblStat);

        String[] statuses = {"Active", "Paused", "Completed", "Cancelled"};
        cmbStatus = new JComboBox<>(statuses);
        cmbStatus.setSelectedItem(currentStatus);
        cmbStatus.setBounds(inputX, 180, 150, 30);
        DesignUtils.styleFilterComboBox(cmbStatus);
        add(cmbStatus);

        int yPos = 240;

        // Dynamic Fields Generation 
        if (type.equalsIgnoreCase("Personal")) {
            lblGoals = new JLabel("Goals:");
            DesignUtils.styleFilterLabel(lblGoals);
            lblGoals.setBounds(30, yPos, 100, 30);
            add(lblGoals);

            txtGoals = new JTextArea();
            DesignUtils.styleTextArea(txtGoals);

            JScrollPane scrollGoals = new JScrollPane(txtGoals);
            scrollGoals.setBounds(30, yPos + 35, 520, 120);
            DesignUtils.styleScrollPane(scrollGoals);
            add(scrollGoals);

        } else {
            // Group Plan UI 
            lblMinAge = new JLabel("Min Age:");
            DesignUtils.styleFilterLabel(lblMinAge);
            lblMinAge.setBounds(30, yPos, 80, 30);
            add(lblMinAge);

            txtMinAge = new JTextField();
            txtMinAge.setBounds(inputX, yPos, 60, 30);
            DesignUtils.styleFilterTextField(txtMinAge);
            add(txtMinAge);

            lblMaxAge = new JLabel("Max Age:");
            DesignUtils.styleFilterLabel(lblMaxAge);
            lblMaxAge.setBounds(inputX + 80, yPos, 80, 30);
            add(lblMaxAge);

            txtMaxAge = new JTextField();
            txtMaxAge.setBounds(inputX + 160, yPos, 60, 30);
            DesignUtils.styleFilterTextField(txtMaxAge);
            add(txtMaxAge);

            yPos += 55;

            lblClasses = new JLabel("Preferred Classes:");
            DesignUtils.styleFilterLabel(lblClasses);
            lblClasses.setBounds(30, yPos, 150, 30);
            add(lblClasses);

            pnlCheckboxes = new JPanel(new java.awt.GridLayout(0, 3));
            pnlCheckboxes.setBackground(DesignUtils.SURFACE);
            pnlCheckboxes.setBorder(new LineBorder(DesignUtils.GRID, 1, true));

            JScrollPane scrollCheck = new JScrollPane(pnlCheckboxes);
            scrollCheck.setBounds(30, yPos + 35, 520, 90);
            DesignUtils.styleScrollPane(scrollCheck);
            add(scrollCheck);

            // Populate available class types dynamically from DB
            classTypeBoxes = new ArrayList<>();
            ArrayList<ClassType> allTypes = TrainingClassControl.getInstance().getAllClassTypes();
            for (ClassType t : allTypes) {
                JCheckBox cb = new JCheckBox(t.getTypeName());
                cb.setBackground(DesignUtils.SURFACE);
                cb.setForeground(DesignUtils.TEXT_MAIN);
                cb.setFont(DesignUtils.FONT_TEXT);
                cb.setFocusable(false);
                classTypeBoxes.add(cb);
                pnlCheckboxes.add(cb);
            }

            yPos += 140;

            lblGuide = new JLabel("Guidelines:");
            DesignUtils.styleFilterLabel(lblGuide);
            lblGuide.setBounds(30, yPos, 120, 30);
            add(lblGuide);

            txtGuide = new JTextArea();
            DesignUtils.styleTextArea(txtGuide);

            JScrollPane scrollGuide = new JScrollPane(txtGuide);
            scrollGuide.setBounds(30, yPos + 35, 520, 100);
            DesignUtils.styleScrollPane(scrollGuide);
            add(scrollGuide);
        }

        btnSave = new JButton("Update Plan");
        DesignUtils.styleButton(btnSave);
        btnSave.setBounds(190, 640, 220, 45);
        btnSave.addActionListener(e -> saveChanges());
        add(btnSave);
    }

    // Enforces logical state rules.
    private void applyBusinessRestrictions(String status) {
        if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Cancelled")) {
            setAllFieldsEnabled(false);
            btnSave.setVisible(false); 
        } else if (status.equalsIgnoreCase("Active") || status.equalsIgnoreCase("Paused")) {
            spStartDate.setEnabled(false); 
        }
    }

    // Toggles the interactive state of all input components.
     
    private void setAllFieldsEnabled(boolean enabled) {
        spStartDate.setEnabled(enabled);
        txtDuration.setEnabled(enabled);
        cmbStatus.setEnabled(enabled);

        if (type.equalsIgnoreCase("Personal")) {
            if (txtGoals != null) txtGoals.setEnabled(enabled);
        } else {
            if (txtMinAge != null) txtMinAge.setEnabled(enabled);
            if (txtMaxAge != null) txtMaxAge.setEnabled(enabled);
            if (txtGuide != null) txtGuide.setEnabled(enabled);
            if (classTypeBoxes != null) {
                for (JCheckBox cb : classTypeBoxes) cb.setEnabled(enabled);
            }
        }
    }

    private void loadSpecificDetails() {
        Map<String, Object> details = FitnessPlanControl.getInstance().getPlanDetails(planID, type);

        if (type.equalsIgnoreCase("Personal")) {
            if (details.get("goals") != null && txtGoals != null) txtGoals.setText((String) details.get("goals"));
            if (details.get("traineeID") != null) currentTraineeID = (int) details.get("traineeID");
        } else {
            if (details.get("minAge") != null && txtMinAge != null) txtMinAge.setText(String.valueOf(details.get("minAge")));
            if (details.get("maxAge") != null && txtMaxAge != null) txtMaxAge.setText(String.valueOf(details.get("maxAge")));
            if (details.get("guidelines") != null && txtGuide != null) txtGuide.setText((String) details.get("guidelines"));

            // Check the appropriate boxes based on previously saved preferences
            ArrayList<String> selectedTypes = (ArrayList<String>) details.get("preferredClasses");
            if (selectedTypes != null && classTypeBoxes != null) {
                for (JCheckBox cb : classTypeBoxes) {
                    if (selectedTypes.contains(cb.getText())) cb.setSelected(true);
                }
            }
        }
    }

    private void saveChanges() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to save these changes?",
                "Confirm Update",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        // Boundary Level Validation
        String durationText = txtDuration.getText().trim();
        if (durationText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a duration.");
            return;
        }

        int newDuration;
        try {
            newDuration = Integer.parseInt(durationText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Duration must be a valid number.");
            return;
        }

        Date newDate = (Date) spStartDate.getValue();
        String newStatus = (String) cmbStatus.getSelectedItem();

        String goals = type.equalsIgnoreCase("Personal") && txtGoals != null ? txtGoals.getText() : "";
        String guide = type.equalsIgnoreCase("Group") && txtGuide != null ? txtGuide.getText() : "";

        int min = 0, max = 0;
        ArrayList<String> prefs = new ArrayList<>();

        if (type.equalsIgnoreCase("Group")) {
            String minText = txtMinAge.getText().trim();
            String maxText = txtMaxAge.getText().trim();

            if (minText.isEmpty() || maxText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter Min Age and Max Age.");
                return;
            }

            try {
                min = Integer.parseInt(minText);
                max = Integer.parseInt(maxText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Age must be a valid number.");
                return;
            }

            if (classTypeBoxes != null) {
                for (JCheckBox cb : classTypeBoxes) {
                    if (cb.isSelected()) prefs.add(cb.getText());
                }
            }
        }

        // Business Logic
        String validationError = FitnessPlanControl.getInstance().validatePlanInput(
                newDuration, type.equalsIgnoreCase("Personal"), goals, min, max, guide, prefs);

        if (validationError != null) {
            JOptionPane.showMessageDialog(this, validationError);
            return; // Stops execution if rules are violated (e.g., negative duration)
        }

        // Execution 
        boolean success = FitnessPlanControl.getInstance().updateTrainingPlan(
                Integer.parseInt(planID),
                newDate,
                newDuration,
                newStatus,
                type.equalsIgnoreCase("Personal"),
                goals,
                currentTraineeID,
                min,
                max,
                guide,
                prefs
        );

        if (success) {
            JOptionPane.showMessageDialog(this, "Plan updated successfully!");
            dispose(); // Close dialog on success
        } else {
            JOptionPane.showMessageDialog(this, "Update failed.");
        }
    }
}