package boundary;

import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javax.swing.border.TitledBorder;

import control.EquipmentControl;
import entity.ItemStatus;

public class PanelManageEquipment extends JPanel {
    private static final long serialVersionUID = 1L;

    // Search and Action Components 
    private JTextField txtSerialNum;
    private JButton btnSearch;
    private JButton btnClear;

    // Status Selection Components 
    private JRadioButton rdbtnRemove;
    private JRadioButton rdbtnDisable;
    private JRadioButton rdbtnAvailable;
    private ButtonGroup actionGroup;
    private JButton btnSubmit;

    // Dynamic Detail Displays 
    private JLabel lblNameVal;
    private JLabel lblCategoryVal;
    private JLabel lblStatusVal;

    // Tracks the currently loaded equipment item to prevent desynchronized updates
    private int currentLoadedSN = -1;

    public PanelManageEquipment() {
        setLayout(null); 
        setBounds(0, 0, 1000, 700);
        DesignUtils.styleMainPanel(this);
        initComponents();
    }

    private void initComponents() {
        JLabel lblTitle = new JLabel("Manage Stock & Status", SwingConstants.LEADING);
        DesignUtils.styleTitle(lblTitle);
        lblTitle.setBounds(50, 30, 700, 50);
        add(lblTitle);

        // Search Bar Area 
        JLabel lblSN = new JLabel("Enter Serial Number:");
        lblSN.setFont(DesignUtils.FONT_TEXT.deriveFont(Font.PLAIN, 24f));
        lblSN.setForeground(DesignUtils.TEXT_MAIN);
        lblSN.setBounds(50, 120, 260, 30);
        lblSN.setOpaque(false);
        add(lblSN);

        txtSerialNum = new JTextField();
        txtSerialNum.setBounds(50, 160, 200, 45);
        DesignUtils.styleFilterTextField(txtSerialNum);
        txtSerialNum.setFont(DesignUtils.FONT_TEXT.deriveFont(Font.BOLD, 22f));
        txtSerialNum.addActionListener(e -> performSearch()); // Enter key triggers search
        add(txtSerialNum);

        btnSearch = new JButton("Search");
        DesignUtils.styleButton(btnSearch);
        btnSearch.setBounds(260, 160, 120, 45);
        btnSearch.setFont(DesignUtils.FONT_BUTTON.deriveFont(Font.BOLD, 18f));
        add(btnSearch);

        btnClear = new JButton("Clear");
        DesignUtils.styleButton(btnClear);
        btnClear.setBounds(390, 160, 100, 45);
        btnClear.setFont(DesignUtils.FONT_BUTTON.deriveFont(Font.BOLD, 18f));
        add(btnClear);

        // Status Action Area 
        JLabel lblOption = new JLabel("Select New Status:");
        lblOption.setFont(DesignUtils.FONT_TEXT.deriveFont(Font.PLAIN, 24f));
        lblOption.setForeground(DesignUtils.TEXT_MAIN);
        lblOption.setBounds(50, 260, 260, 30);
        lblOption.setOpaque(false);
        add(lblOption);

        rdbtnAvailable = new JRadioButton("Available");
        styleRadio(rdbtnAvailable);
        rdbtnAvailable.setBounds(50, 300, 300, 35);
        
        rdbtnDisable = new JRadioButton("Out Of Service");
        styleRadio(rdbtnDisable);
        rdbtnDisable.setBounds(50, 340, 320, 35);

        rdbtnRemove = new JRadioButton("Remove");
        styleRadio(rdbtnRemove);
        rdbtnRemove.setBounds(50, 380, 300, 35);

        actionGroup = new ButtonGroup();
        actionGroup.add(rdbtnDisable);
        actionGroup.add(rdbtnAvailable);
        actionGroup.add(rdbtnRemove);

        add(rdbtnDisable);
        add(rdbtnAvailable);
        add(rdbtnRemove);

        enableControls(false);

        btnSubmit = new JButton("Update Status");
        DesignUtils.styleButton(btnSubmit);
        btnSubmit.setBounds(50, 460, 250, 60);
        btnSubmit.setFont(DesignUtils.FONT_BUTTON.deriveFont(Font.BOLD, 20f));
        btnSubmit.setEnabled(false);
        add(btnSubmit);

        // Info Display Panel 
        JPanel infoPanel = new JPanel(null);
        infoPanel.setBounds(530, 120, 450, 280);
        infoPanel.setBackground(DesignUtils.SURFACE);

        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(DesignUtils.GRID, 1),
                "Item Details"
        );
        border.setTitleFont(DesignUtils.FONT_HEADER.deriveFont(Font.BOLD, 18f));
        border.setTitleColor(DesignUtils.TEXT_MAIN);
        infoPanel.setBorder(border);

        JLabel lblName = new JLabel("Item Name:");
        lblName.setFont(DesignUtils.FONT_TEXT.deriveFont(Font.PLAIN, 18f));
        lblName.setForeground(DesignUtils.TEXT_MAIN);
        lblName.setBounds(30, 60, 150, 30);
        lblName.setOpaque(false);
        infoPanel.add(lblName);

        lblNameVal = new JLabel("-");
        lblNameVal.setFont(DesignUtils.FONT_HEADER.deriveFont(Font.BOLD, 20f));
        lblNameVal.setForeground(DesignUtils.HEADER_BG);
        lblNameVal.setBounds(150, 60, 300, 30);
        lblNameVal.setOpaque(false);
        infoPanel.add(lblNameVal);

        JLabel lblCat = new JLabel("Category:");
        lblCat.setFont(DesignUtils.FONT_TEXT.deriveFont(Font.PLAIN, 18f));
        lblCat.setForeground(DesignUtils.TEXT_MAIN);
        lblCat.setBounds(30, 130, 150, 30);
        lblCat.setOpaque(false);
        infoPanel.add(lblCat);

        lblCategoryVal = new JLabel("-");
        lblCategoryVal.setFont(DesignUtils.FONT_HEADER.deriveFont(Font.BOLD, 20f));
        lblCategoryVal.setForeground(DesignUtils.TEXT_MAIN);
        lblCategoryVal.setBounds(150, 130, 300, 30);
        lblCategoryVal.setOpaque(false);
        infoPanel.add(lblCategoryVal);

        JLabel lblStat = new JLabel("Current Status:");
        lblStat.setFont(DesignUtils.FONT_TEXT.deriveFont(Font.PLAIN, 18f));
        lblStat.setForeground(DesignUtils.TEXT_MAIN);
        lblStat.setBounds(30, 200, 150, 30);
        lblStat.setOpaque(false);
        infoPanel.add(lblStat);

        lblStatusVal = new JLabel("-");
        lblStatusVal.setFont(DesignUtils.FONT_HEADER.deriveFont(Font.BOLD, 20f));
        lblStatusVal.setForeground(DesignUtils.TEXT_MAIN);
        lblStatusVal.setBounds(180, 200, 250, 30);
        lblStatusVal.setOpaque(false);
        infoPanel.add(lblStatusVal);

        add(infoPanel);

        // Attach action listeners
        btnSearch.addActionListener(e -> performSearch());
        btnSubmit.addActionListener(e -> performUpdate());
        btnClear.addActionListener(e -> {
            clearInfo();
            txtSerialNum.requestFocus();
        });
    }

    private void styleRadio(JRadioButton rb) {
        rb.setFont(DesignUtils.FONT_TEXT.deriveFont(Font.PLAIN, 20f));
        rb.setForeground(DesignUtils.TEXT_MAIN);
        rb.setOpaque(false);
        rb.setFocusable(false);
    }

    // Toggles interaction state for the status modification tools.
     
    private void enableControls(boolean enable) {
        rdbtnDisable.setEnabled(enable);
        rdbtnAvailable.setEnabled(enable);
        rdbtnRemove.setEnabled(enable);
        if (!enable) actionGroup.clearSelection();
    }

      // Queries the DB for the entered Serial Number and populates the Info Panel.
     // Implements logic to lock radio buttons based on current status.
     
    private void performSearch() {
        String input = txtSerialNum.getText().trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Serial Number.");
            return;
        }

        try {
            int sn = Integer.parseInt(input);
            String[] details = EquipmentControl.getInstance().getItemDisplayDetails(sn);

            if (details != null) {
                currentLoadedSN = sn; 
                lblNameVal.setText(details[0]);
                lblCategoryVal.setText(details[1]);

                String rawStatus = details[2];
                String displayStatus = rawStatus;
                if (rawStatus.equalsIgnoreCase("OUTOFSERVICE")) displayStatus = "Out Of Service";
                else if (rawStatus.equalsIgnoreCase("AVAILABLE")) displayStatus = "Available";
                else if (rawStatus.equalsIgnoreCase("REMOVED")) displayStatus = "Removed";

                lblStatusVal.setText(displayStatus);
                lblStatusVal.setForeground(DesignUtils.TEXT_MAIN);

                enableControls(true);
                btnSubmit.setEnabled(true);

                // Business Rule Prevent Redundant Status Selection 
                if (rawStatus.equalsIgnoreCase("AVAILABLE")) {
                    rdbtnAvailable.setEnabled(false);
                    rdbtnRemove.setEnabled(false); // Must be out of service before removal
                }
                else if (rawStatus.equalsIgnoreCase("OUTOFSERVICE")) rdbtnDisable.setEnabled(false);
                else if (rawStatus.equalsIgnoreCase("REMOVED")) {
                    rdbtnRemove.setEnabled(false);
                    actionGroup.clearSelection();
                }

            } else {
                JOptionPane.showMessageDialog(this, "Item SN " + sn + " not found!");
                clearInfo();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Serial Number must be a number!");
        }
    }

    // Evaluates selected action, applies final business validations, and sends the update command to the Control layer.
    
    private void performUpdate() {
        if (currentLoadedSN == -1) return;

        String actionName = "";
        ItemStatus targetStatus = null;

        if (rdbtnDisable.isSelected()) {
            actionName = "Out Of Service";
            targetStatus = ItemStatus.OUTOFSERVICE;
        } else if (rdbtnAvailable.isSelected()) {
            actionName = "Available";
            targetStatus = ItemStatus.AVAILABLE;
        } else if (rdbtnRemove.isSelected()) {
            actionName = "Remove";
            targetStatus = ItemStatus.REMOVED;
        } else {
            JOptionPane.showMessageDialog(this, "Please select an action.");
            return;
        }

        // Business Logic Validations 
        String currentRaw = lblStatusVal.getText();
        if (currentRaw.equalsIgnoreCase("Available") && targetStatus == ItemStatus.REMOVED) {
            JOptionPane.showMessageDialog(this, "Cannot remove an available item. Mark as Out Of Service first.");
            return;
        }
        if (currentRaw.equalsIgnoreCase("Removed")) {
            JOptionPane.showMessageDialog(this, "Cannot change status of a removed item.");
            return;
        }

        // Require final confirmation
        int response = JOptionPane.showConfirmDialog(
                this,
                "Change status of item " + currentLoadedSN + " to " + actionName + "?",
                "Confirm Update",
                JOptionPane.YES_NO_OPTION
        );

        if (response == JOptionPane.YES_OPTION) {
            try {
                boolean success = EquipmentControl.getInstance().manageItemStatus(currentLoadedSN, targetStatus);
                if (success) {
                    
                    // Trigger dynamic notification system based on system requirements story
                    if (targetStatus == ItemStatus.OUTOFSERVICE) {
                        JOptionPane.showMessageDialog(this,
                            "Status updated successfully.\nNotifications have been sent to all consultants of upcoming affected classes.",
                            "Update Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Updated successfully!");
                    }
                    
                    // Refresh view
                    performSearch();
                    txtSerialNum.setText("");
                    txtSerialNum.requestFocus();
                } else {
                    JOptionPane.showMessageDialog(this, "Update failed.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearInfo() {
        txtSerialNum.setText("");
        lblNameVal.setText("-");
        lblCategoryVal.setText("-");
        lblStatusVal.setText("-");
        lblStatusVal.setForeground(DesignUtils.TEXT_MAIN);
        currentLoadedSN = -1;
        enableControls(false);
        btnSubmit.setEnabled(false);
    }
}