package boundary;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;

import javax.swing.border.LineBorder;

import control.FitnessPlanControl;
import control.TraineeRegisterControl;
import control.TrainingClassControl;
import control.UsersControl;
import entity.ClassType;
import entity.Trainee;

	// PanelCreatePlan allows the Consultant to create new Fitness Plans.
  	// The panel dynamically switches its layout and required inputs 
  	// based on the selected plan type (Personal,Group).
 
public class PanelCreatePlan extends JPanel {
    private static final long serialVersionUID = 1L;

    // UI Components
    private JLabel lblTitle, lblChooseType, lblStartDate, lblDuration;
    private JRadioButton rbIndividual, rbGroup;
    private ButtonGroup typeGroup;
    private JSpinner spStartDate;
    private JTextField txtDuration;
    private JButton btnSave, btnClear;

    // Personal Plan Specific UI 
    private JLabel lblTrainee;
    private JComboBox<Trainee> cmbTrainees;
    private JLabel lblGoals;
    private JTextArea txtGoals;
    private JScrollPane scrollGoals;

    //  Group Plan Specific UI 
    private JLabel lblMinAge, lblMaxAge, lblPreferredClasses, lblGuide;
    private JTextField txtMinAge, txtMaxAge;
    private JTextArea txtGuide;
    private JScrollPane scrollGuide;
    private JPanel pnlCheckboxes;
    private ArrayList<JCheckBox> classTypeBoxes;

    public PanelCreatePlan() {
        setLayout(null);
        setBounds(0, 0, 1000, 700);
        DesignUtils.styleMainPanel(this);

        initComponents();
        setupDynamicLogic();
        setupActionListeners();

        // Initialize to default state 
        showIndividualFields(true);
        showGroupFields(false);
    }

    //  Initializes all UI components for both plan types.
     
    private void initComponents() {
        lblTitle = new JLabel("New Fitness Plan", SwingConstants.LEADING);
        DesignUtils.styleTitle(lblTitle);
        lblTitle.setBounds(33, 30, 520, 40);
        add(lblTitle);

        // Common Plan Configurations 
        lblChooseType = new JLabel("Choose Plan:");
        DesignUtils.styleFilterLabel(lblChooseType);
        lblChooseType.setFont(DesignUtils.FONT_HEADER);
        lblChooseType.setBounds(33, 100, 170, 30);
        add(lblChooseType);

        rbIndividual = new JRadioButton("Personal");
        styleRadio(rbIndividual);
        rbIndividual.setBounds(180, 100, 120, 30);
        rbIndividual.setSelected(true);

        rbGroup = new JRadioButton("Group");
        styleRadio(rbGroup);
        rbGroup.setBounds(310, 100, 120, 30);

        typeGroup = new ButtonGroup();
        typeGroup.add(rbIndividual);
        typeGroup.add(rbGroup);
        add(rbIndividual);
        add(rbGroup);

        lblStartDate = new JLabel("Start Date:");
        DesignUtils.styleFilterLabel(lblStartDate);
        lblStartDate.setBounds(33, 160, 150, 30);
        add(lblStartDate);

        spStartDate = new JSpinner(new SpinnerDateModel());
        spStartDate.setEditor(new JSpinner.DateEditor(spStartDate, "dd/MM/yyyy"));
        spStartDate.setBounds(190, 160, 150, 30);
        DesignUtils.styleFilterSpinner(spStartDate);
        add(spStartDate);

        lblDuration = new JLabel("Duration (days):");
        DesignUtils.styleFilterLabel(lblDuration);
        lblDuration.setBounds(33, 210, 170, 30);
        add(lblDuration);

        txtDuration = new JTextField();
        txtDuration.setBounds(190, 210, 150, 30);
        DesignUtils.styleFilterTextField(txtDuration);
        add(txtDuration);

        // Personal Plan Fields Initialization 
        lblTrainee = new JLabel("Select Trainee:");
        DesignUtils.styleFilterLabel(lblTrainee);
        lblTrainee.setBounds(33, 270, 150, 30);
        add(lblTrainee);

        cmbTrainees = new JComboBox<>();
        ArrayList<Trainee> allTrainees = TraineeRegisterControl.getInstance().getAllTrainees();
        for (Trainee t : allTrainees) cmbTrainees.addItem(t);
        cmbTrainees.setSelectedIndex(-1);

        // Custom renderer to format the Trainee object display text in the ComboBox
        cmbTrainees.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Trainee) {
                    Trainee t = (Trainee) value;
                    setText(t.getId() + " - " + t.getFirstName() + " " + t.getLastName());
                }
                return this;
            }
        });

        cmbTrainees.setBounds(190, 270, 250, 30);
        DesignUtils.styleFilterComboBox(cmbTrainees);
        add(cmbTrainees);

        lblGoals = new JLabel("Trainee Goals:");
        DesignUtils.styleFilterLabel(lblGoals);
        lblGoals.setBounds(33, 320, 170, 30);
        add(lblGoals);

        txtGoals = new JTextArea();
        DesignUtils.styleTextArea(txtGoals);

        scrollGoals = new JScrollPane(txtGoals);
        scrollGoals.setBounds(190, 320, 350, 100);
        DesignUtils.styleScrollPane(scrollGoals);
        add(scrollGoals);

        // Group Plan Fields Initialization 
        lblMinAge = new JLabel("Min Age:");
        DesignUtils.styleFilterLabel(lblMinAge);
        lblMinAge.setBounds(33, 270, 90, 30);
        add(lblMinAge);

        txtMinAge = new JTextField();
        txtMinAge.setBounds(150, 270, 60, 30);
        DesignUtils.styleFilterTextField(txtMinAge);
        add(txtMinAge);

        lblMaxAge = new JLabel("Max Age:");
        DesignUtils.styleFilterLabel(lblMaxAge);
        lblMaxAge.setBounds(240, 270, 90, 30);
        add(lblMaxAge);

        txtMaxAge = new JTextField();
        txtMaxAge.setBounds(357, 270, 60, 30);
        DesignUtils.styleFilterTextField(txtMaxAge);
        add(txtMaxAge);

        lblPreferredClasses = new JLabel("Preferred Classes:");
        DesignUtils.styleFilterLabel(lblPreferredClasses);
        lblPreferredClasses.setBounds(33, 320, 200, 30);
        add(lblPreferredClasses);

        pnlCheckboxes = new JPanel(new GridLayout(0, 2, 5, 5));
        pnlCheckboxes.setBounds(190, 320, 350, 80);
        pnlCheckboxes.setBackground(DesignUtils.SURFACE);
        pnlCheckboxes.setBorder(new LineBorder(DesignUtils.GRID, 1, true));
        add(pnlCheckboxes);

        // Dynamically populate available class types for Group Plan configuration
        classTypeBoxes = new ArrayList<>();
        ArrayList<ClassType> typesFromDB = TrainingClassControl.getInstance().getAllClassTypes();
        for (ClassType typeObj : typesFromDB) {
            JCheckBox cb = new JCheckBox(typeObj.getTypeName());
            cb.setFont(DesignUtils.FONT_TEXT.deriveFont(14f));
            cb.setForeground(DesignUtils.TEXT_MAIN);
            cb.setBackground(DesignUtils.SURFACE);
            cb.setFocusable(false);
            classTypeBoxes.add(cb);
            pnlCheckboxes.add(cb);
        }

        lblGuide = new JLabel("Guidelines:");
        DesignUtils.styleFilterLabel(lblGuide);
        lblGuide.setBounds(33, 420, 150, 30);
        add(lblGuide);

        txtGuide = new JTextArea();
        DesignUtils.styleTextArea(txtGuide);

        scrollGuide = new JScrollPane(txtGuide);
        scrollGuide.setBounds(190, 420, 350, 100);
        DesignUtils.styleScrollPane(scrollGuide);
        add(scrollGuide);

        // Action Buttons 
        btnSave = new JButton("Submit Plan");
        DesignUtils.styleButton(btnSave);
        btnSave.setBounds(33, 580, 250, 50);
        add(btnSave);

        btnClear = new JButton("Clear Form");
        DesignUtils.styleButton(btnClear);
        btnClear.setBounds(300, 580, 150, 50);
        add(btnClear);
    }

    private void styleRadio(JRadioButton rb) {
        rb.setFont(DesignUtils.FONT_TEXT);
        rb.setForeground(DesignUtils.TEXT_MAIN);
        rb.setOpaque(false);
        rb.setFocusable(false);
    }
    //Attaches listeners to the radio buttons to swap the UI context dynamically.
     
    private void setupDynamicLogic() {
        ActionListener toggleListener = e -> {
            boolean isIndividual = rbIndividual.isSelected();
            showIndividualFields(isIndividual);
            showGroupFields(!isIndividual);
            clearForm(); // Reset fields to avoid submitting hidden state data
            revalidate();
            repaint();
        };
        rbIndividual.addActionListener(toggleListener);
        rbGroup.addActionListener(toggleListener);
    }

    private void setupActionListeners() {
        btnSave.addActionListener(e -> saveTrainingPlan());
        btnClear.addActionListener(e -> clearForm());
    }

     // Extracts, validates, and dispatches the input data to the Control layer for processing.
     
    private void saveTrainingPlan() {
        Date startDate = (Date) spStartDate.getValue();

        // Boundary Level Type Validations 
        String durationText = txtDuration.getText().trim();
        if (durationText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a duration.");
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durationText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Duration must be a valid number.");
            return;
        }

        int currentConsultantID = UsersControl.getInstance().getLoggedInUserId();
        if (currentConsultantID == -1) {
            JOptionPane.showMessageDialog(this, "Error: No consultant is logged in!", "Session Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean isIndividual = rbIndividual.isSelected();
        String goals = txtGoals.getText();
        String notes = txtGuide.getText();

        int minAge = 0, maxAge = 0, traineeID = 0;
        ArrayList<String> selectedClasses = new ArrayList<>();

        if (isIndividual) {
            // Validate Personal Plan specific requirements
            Trainee selectedTrainee = (Trainee) cmbTrainees.getSelectedItem();
            if (selectedTrainee == null) {
                JOptionPane.showMessageDialog(this, "Please select a Trainee!");
                return;
            }
            traineeID = selectedTrainee.getId();
        } else {
            // Validate Group Plan specific requirements
            String minText = txtMinAge.getText().trim();
            String maxText = txtMaxAge.getText().trim();
            if (minText.isEmpty() || maxText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter Min Age and Max Age.");
                return;
            }
            try {
                minAge = Integer.parseInt(minText);
                maxAge = Integer.parseInt(maxText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Age must be a valid number.");
                return;
            }
            for (JCheckBox cb : classTypeBoxes) {
                if (cb.isSelected()) selectedClasses.add(cb.getText());
            }
        }

        String validationError = FitnessPlanControl.getInstance().validatePlanInput(
                duration, isIndividual, goals, minAge, maxAge, notes, selectedClasses);

        if (validationError != null) {
            JOptionPane.showMessageDialog(this, validationError);
            return; // Abort operation if logical rules fail 
        }

        int response = JOptionPane.showConfirmDialog(this, "Create this plan?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (response != JOptionPane.YES_OPTION) return;

        boolean success = FitnessPlanControl.getInstance().addNewTrainingPlan(
                startDate, duration, isIndividual,
                goals, minAge, maxAge, notes, selectedClasses,
                traineeID, currentConsultantID
        );

        if (success) {
            JOptionPane.showMessageDialog(this, "Plan created successfully!");
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Error creating plan.");
        }
    }

    // Resets all UI components to their default empty states.
     
    private void clearForm() {
        txtDuration.setText("");
        txtGoals.setText("");
        txtMinAge.setText("");
        txtMaxAge.setText("");
        txtGuide.setText("");
        spStartDate.setValue(new Date());
        if (cmbTrainees.getItemCount() > 0) cmbTrainees.setSelectedIndex(0);
        for (JCheckBox cb : classTypeBoxes) cb.setSelected(false);
    }

    // Toggles visibility of the Personal Plan components.
     
    private void showIndividualFields(boolean visible) {
        lblTrainee.setVisible(visible);
        cmbTrainees.setVisible(visible);
        lblGoals.setVisible(visible);
        scrollGoals.setVisible(visible);
    }

    // Toggles visibility of the Group Plan components.
     
    private void showGroupFields(boolean visible) {
        lblMinAge.setVisible(visible);
        txtMinAge.setVisible(visible);
        lblMaxAge.setVisible(visible);
        txtMaxAge.setVisible(visible);
        lblPreferredClasses.setVisible(visible);
        pnlCheckboxes.setVisible(visible);
        lblGuide.setVisible(visible);
        scrollGuide.setVisible(visible);
    }
}