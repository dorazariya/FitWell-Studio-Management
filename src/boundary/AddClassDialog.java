package boundary;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.time.LocalDateTime;
import java.time.ZoneId;

import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import control.EquipmentControl;
import control.FitnessPlanControl;
import control.TrainingClassControl;
import control.UsersControl;

import entity.ClassType;
import entity.EquipmentType;
import entity.FitnessPlan;


// Dialog responsible for collecting all data required to schedule a new training class
// It gathers class details tips and initial equipment requirements before sending them to the database
public class AddClassDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final Runnable onSuccessCallback;

    private JTextField txtName;
    private JComboBox<ClassType> cmbType;
    private JSpinner spStart;
    private JSpinner spEnd;
    private JTextField txtMax;
    private JComboBox<FitnessPlan> cmbPlan;

    private final JTextField[] txtTipsContent = new JTextField[5];
    private final JTextField[] txtTipsUrl = new JTextField[5];

    private JComboBox<String> cmbEquipmentType;
    private JTextField txtQuantity;
    private JTable tblEquipment;
    private DefaultTableModel modelEquipment;
    
    // Stores the equipment requirements added locally by the user before the final save
    private final ArrayList<TrainingClassControl.EquipmentReqInput> requirementsList = new ArrayList<>();
    
    public AddClassDialog(Frame parent, Runnable onSuccessCallback) {
        super(parent, "Add New Class", true);
        this.onSuccessCallback = onSuccessCallback;

        setSize(700, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane = new JScrollPane(buildMainContent());
        scrollPane.setBorder(null);
        DesignUtils.styleScrollPane(scrollPane);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(DesignUtils.BG_MAIN);

        add(scrollPane, BorderLayout.CENTER);
        add(buildBottomButtons(), BorderLayout.SOUTH);
    }

    // Constructs the vertical flow of the form splitting it into three logical sections
    // General Details Optional Tips and Equipment Requirements
    private JPanel buildMainContent() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(new EmptyBorder(15, 20, 15, 20));
        container.setBackground(DesignUtils.BG_MAIN);

        // Section one Basic Class details
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 15));
        formPanel.setBackground(DesignUtils.SURFACE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(DesignUtils.GRID),
                "Class Details",
                0, 0,
                DesignUtils.FONT_HEADER
        ));

        txtName = createTextField();
        txtMax = createTextField();

        ArrayList<ClassType> types = TrainingClassControl.getInstance().getAllClassTypes();
        cmbType = new JComboBox<>(types.toArray(new ClassType[0]));
        cmbType.setSelectedIndex(-1);
        DesignUtils.styleFilterComboBox(cmbType);

        ArrayList<FitnessPlan> plans = FitnessPlanControl.getInstance().getAllFitnessPlans();
        cmbPlan = new JComboBox<>(plans.toArray(new FitnessPlan[0]));
        cmbPlan.setSelectedIndex(-1);
        DesignUtils.styleFilterComboBox(cmbPlan);

        // Initialize default times one hour from now to save user input time
        Date now = new Date();
        spStart = createDateSpinner(new Date(now.getTime() + 3600000));
        spEnd = createDateSpinner(new Date(now.getTime() + 7200000));
        DesignUtils.styleFilterSpinner(spStart);
        DesignUtils.styleFilterSpinner(spEnd);

        addLabeledField(formPanel, "Class Name:", txtName);
        addLabeledField(formPanel, "Class Type:", cmbType);
        addLabeledField(formPanel, "Start Time:", spStart);
        addLabeledField(formPanel, "End Time:", spEnd);
        addLabeledField(formPanel, "Max Participants:", txtMax);
        addLabeledField(formPanel, "Fitness Plan:", cmbPlan);

        // Section two Optional external tips and links
        JPanel tipsPanel = new JPanel(new GridLayout(0, 2, 5, 10));
        tipsPanel.setBackground(DesignUtils.SURFACE);
        tipsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(DesignUtils.GRID),
                "Optional Tips (Max 5)",
                0, 0,
                DesignUtils.FONT_HEADER
        ));

        for (int i = 0; i < 5; i++) {
            txtTipsContent[i] = createTextField();
            txtTipsUrl[i] = createTextField();
            addLabeledField(tipsPanel, "Tip " + (i + 1) + " Content:", txtTipsContent[i]);
            addLabeledField(tipsPanel, "Tip " + (i + 1) + " URL:", txtTipsUrl[i]);
        }

        JPanel equipPanel = buildEquipmentPanel();

        container.add(formPanel);
        container.add(Box.createVerticalStrut(20));
        container.add(tipsPanel);
        container.add(Box.createVerticalStrut(20));
        container.add(equipPanel);

        return container;
    }

    private void addLabeledField(JPanel p, String text, JComponent field) {
        JLabel l = new JLabel(text);
        DesignUtils.styleFilterLabel(l);
        p.add(l);
        p.add(field);
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        DesignUtils.styleFilterTextField(tf);
        tf.setMargin(new Insets(4, 6, 4, 6));
        return tf;
    }

    // Creates the embedded equipment management UI block
    // Operates independently holding data in local arrays until the main Save button is clicked
    private JPanel buildEquipmentPanel() {
        JPanel pnlEquipment = new JPanel(new BorderLayout(5, 10));
        pnlEquipment.setBackground(DesignUtils.SURFACE);
        pnlEquipment.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(DesignUtils.GRID),
                "Equipment Requirements",
                0, 0,
                DesignUtils.FONT_HEADER
        ));

        JPanel pnlSelect = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlSelect.setOpaque(false);

        cmbEquipmentType = new JComboBox<>();
        cmbEquipmentType.setPreferredSize(new Dimension(220, 35));
        loadEquipmentTypes();
        DesignUtils.styleFilterComboBox(cmbEquipmentType);

        txtQuantity = createTextField();
        txtQuantity.setColumns(4);

        JButton btnAddEquip = new JButton("Add");
        JButton btnUpdateEquip = new JButton("Update");
        DesignUtils.styleButton(btnAddEquip);
        DesignUtils.styleButton(btnUpdateEquip);

        JLabel lblType = new JLabel("Type:");
        DesignUtils.styleFilterLabel(lblType);
        JLabel lblQty = new JLabel("Qty:");
        DesignUtils.styleFilterLabel(lblQty);

        pnlSelect.add(lblType);
        pnlSelect.add(cmbEquipmentType);
        pnlSelect.add(lblQty);
        pnlSelect.add(txtQuantity);
        pnlSelect.add(btnAddEquip);
        pnlSelect.add(btnUpdateEquip);
        pnlEquipment.add(pnlSelect, BorderLayout.NORTH);

        String[] cols = {"Equipment Type", "Required Quantity"};
        modelEquipment = new DefaultTableModel(cols, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblEquipment = new JTable(modelEquipment);
        DesignUtils.styleTableCentered(tblEquipment);

        tblEquipment.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = tblEquipment.getSelectedRow();
                if (row != -1) {
                    cmbEquipmentType.setSelectedItem(modelEquipment.getValueAt(row, 0));
                    txtQuantity.setText(String.valueOf(modelEquipment.getValueAt(row, 1)));
                }
            }
        });

        JScrollPane scrollTable = new JScrollPane(tblEquipment);
        DesignUtils.styleScrollPane(scrollTable);
        scrollTable.setPreferredSize(new Dimension(500, 150));
        pnlEquipment.add(scrollTable, BorderLayout.CENTER);

        JButton btnRemove = new JButton("Remove Selected");
        DesignUtils.styleButton(btnRemove);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBottom.setOpaque(false);
        pnlBottom.add(btnRemove);
        pnlEquipment.add(pnlBottom, BorderLayout.SOUTH);

        btnAddEquip.addActionListener(e -> addEquipmentItem());
        btnUpdateEquip.addActionListener(e -> updateEquipmentItem());
        btnRemove.addActionListener(e -> removeSelectedEquipment());

        return pnlEquipment;
    }

    private JPanel buildBottomButtons() {
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        pnlButtons.setBackground(DesignUtils.BG_MAIN);

        JButton btnSave = new JButton("Create Class");
        JButton btnCancel = new JButton("Cancel");

        btnSave.setPreferredSize(new Dimension(180, 50));
        btnCancel.setPreferredSize(new Dimension(140, 50));

        DesignUtils.styleButton(btnSave);
        DesignUtils.styleButton(btnCancel);

        btnSave.addActionListener(e -> saveClass());
        btnCancel.addActionListener(e -> dispose());

        pnlButtons.add(btnSave);
        pnlButtons.add(btnCancel);
        return pnlButtons;
    }

    // Handles adding a new requirement to the pending list
    // Checks for duplicate entries before adding
    private void addEquipmentItem() {
        String type = (String) cmbEquipmentType.getSelectedItem();
        String qtyStr = txtQuantity.getText().trim();
        if (type == null) return;

        for (TrainingClassControl.EquipmentReqInput r : requirementsList) {
            if (r.getTypeName().equals(type)) {
                JOptionPane.showMessageDialog(this, "Item already exists! Use 'Update' to change quantity.");
                return;
            }
        }

        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) throw new NumberFormatException();

            // Store with a temporary class ID of 0 the real ID is assigned by the database on save
            requirementsList.add(new TrainingClassControl.EquipmentReqInput(type, qty));
            modelEquipment.addRow(new Object[]{type, qty});
            txtQuantity.setText("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity");
        }
    }

    private void updateEquipmentItem() {
        String type = (String) cmbEquipmentType.getSelectedItem();
        String qtyStr = txtQuantity.getText().trim();
        if (type == null) return;

        int idx = -1;
        for (int i = 0; i < requirementsList.size(); i++) {
            if (requirementsList.get(i).getTypeName().equals(type)) {
                idx = i;
                break;
            }
        }

        if (idx == -1) {
            JOptionPane.showMessageDialog(this, "Item not found! Use 'Add' first.");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) throw new NumberFormatException();

            requirementsList.set(idx, new TrainingClassControl.EquipmentReqInput(type, qty));
            modelEquipment.setValueAt(qty, idx, 1);

            txtQuantity.setText("");
            tblEquipment.clearSelection();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity");
        }
    }

    private void removeSelectedEquipment() {
        int row = tblEquipment.getSelectedRow();
        if (row != -1) {
            modelEquipment.removeRow(row);
            requirementsList.remove(row);
        } else {
            JOptionPane.showMessageDialog(this, "Select a row to remove.");
        }
    }

    private void loadEquipmentTypes() {
        cmbEquipmentType.removeAllItems();
        for (EquipmentType t : EquipmentControl.getInstance().getAllEquipmentTypes()) {
            cmbEquipmentType.addItem(t.getTypeName());
        }
    }

    // The core function executing the save operation and handling all validations
    private void saveClass() {
        try {
            String name = txtName.getText().trim();
            if (name.isEmpty()) throw new Exception("Name required.");

            int max = Integer.parseInt(txtMax.getText().trim());
            ClassType type = (ClassType) cmbType.getSelectedItem();
            FitnessPlan plan = (FitnessPlan) cmbPlan.getSelectedItem();

            if (type == null || plan == null) throw new Exception("Select Type/Plan.");

            LocalDateTime start = ((Date) spStart.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();

            LocalDateTime end = ((Date) spEnd.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();

            int consultantId = UsersControl.getInstance().getLoggedInUserId();

            // Process and validate optional tips
            ArrayList<TrainingClassControl.TipInput> tips = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                String content = txtTipsContent[i].getText().trim();
                String url = txtTipsUrl[i].getText().trim();

                if (!url.isEmpty()) {
                    // Regex ensures the URL format is structurally sound before saving
                    if (!url.matches("^(https?://.+)$"))
                        throw new Exception("Tip " + (i + 1) + " URL must start with http:// or https://");
                    // System logic dictates a URL is useless without context description
                    if (content.isEmpty())
                        throw new Exception("Tip " + (i + 1) + " has URL but missing description.");
                }

                if (!content.isEmpty()) tips.add(new TrainingClassControl.TipInput(content, url));
            }
            
            TrainingClassControl.getInstance().createClassFromUI(
                    name,
                    start,
                    end,
                    type.getTypeName(),
                    max,
                    plan.getPlanID(),
                    consultantId,
                    tips,
                    requirementsList
            );
            
            JOptionPane.showMessageDialog(this, "Class created!");
            if (onSuccessCallback != null) onSuccessCallback.run();
            dispose();

        } catch (TrainingClassControl.EquipmentConflictException ce) {
            // The database successfully rolled back the transaction to prevent invalid states
            // We notify the user of the specific shortages and keep the dialog open for correction
            StringBuilder sb = new StringBuilder("Cannot create class due to equipment conflicts:\n");
            for (String s : ce.getConflicts()) sb.append("- ").append(s).append("\n");
            sb.append("\nPlease adjust the class schedule or equipment quantities.");
      
            JOptionPane.showMessageDialog(this, sb.toString(), "Conflict Detected", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Configures the date spinner component using SpinnerDateModel and defines its visual format
    private JSpinner createDateSpinner(Date date) {
        SpinnerDateModel m = new SpinnerDateModel();
        m.setValue(date);

        JSpinner s = new JSpinner(m);
        JSpinner.DateEditor e = new JSpinner.DateEditor(s, "dd/MM/yyyy HH:mm");
        s.setEditor(e);

        e.getTextField().setFont(DesignUtils.FONT_TEXT);
        e.getTextField().setMargin(new Insets(4, 6, 4, 6));

        return s;
    }
}