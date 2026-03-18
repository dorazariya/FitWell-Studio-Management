package boundary;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import control.FitnessPlanControl;
import control.UsersControl;

//DietitianScreen represents the main dashboard for the Dietitian actor.
//It displays a list of trainees with Personal Plans and allows the dietitian to securely update their specific dietary restrictions.
 
 
public class DietitianScreen extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final int FRAME_WIDTH = 1280;
    private static final int FRAME_HEIGHT = 850;

    private DefaultTableModel model;
    private JTable table;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                DietitianScreen frame = new DietitianScreen();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public DietitianScreen() {
        super("FitWell - Dietitian Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);

        // Main Layout Setup 
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 80, 40));
        DesignUtils.styleMainPanel(mainPanel);
        setContentPane(mainPanel);

        // Title 
        JPanel topCard = new JPanel(new BorderLayout());
        DesignUtils.styleCardPanel(topCard);

        JLabel lblTitle = new JLabel("Trainee Dietary Restrictions", SwingConstants.CENTER);
        DesignUtils.styleTitle(lblTitle);
        lblTitle.setOpaque(false);

        topCard.add(lblTitle, BorderLayout.CENTER);
        mainPanel.add(topCard, BorderLayout.NORTH);

        // Center Card Data Table 
        String[] columns = { "Select", "Full Name", "Trainee ID", "Plan ID", "Restrictions" };
        
        // Custom TableModel to handle Checkbox logic
        model = new DefaultTableModel(new Object[][] {}, columns) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Renders the first column as a Checkbox
                return columnIndex == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                // Only the checkbox column is editable directly in the table
                return column == 0;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                // Enforce single selection logic Checking one row unchecks the rest
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

        JScrollPane scrollPane = new JScrollPane(table);
        DesignUtils.styleScrollPane(scrollPane);

        JPanel tableCard = new JPanel(new BorderLayout());
        DesignUtils.styleCardPanel(tableCard);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(tableCard, BorderLayout.CENTER);

        // Bottom Card Action Buttons
        JPanel bottomCard = new JPanel(new FlowLayout(FlowLayout.CENTER));
        DesignUtils.styleCardPanel(bottomCard);

        JButton btnUpdate = new JButton("Update Trainee Restrictions");
        DesignUtils.styleButton(btnUpdate);
        btnUpdate.setPreferredSize(new Dimension(320, 46));
        btnUpdate.addActionListener(e -> openUpdateDialogAndSave());

        bottomCard.add(btnUpdate);
        mainPanel.add(bottomCard, BorderLayout.SOUTH);

        // Populate table with initial data
        loadTableData();
    }

    // Fetches trainees linked to Personal Plans via the Control layer and populates the table.
     
    private void loadTableData() {
        model.setRowCount(0); // Clear table
        for (String[] r : FitnessPlanControl.getInstance().getTraineesForDietitian()) {
            model.addRow(new Object[] { false, r[0], r[1], r[2], r[3] });
        }
    }

    // Helper method to locate the row that the user checked.
    
    private int getSelectedRowByCheckbox() {
        for (int i = 0; i < model.getRowCount(); i++) {
            Boolean isSelected = (Boolean) model.getValueAt(i, 0);
            if (Boolean.TRUE.equals(isSelected)) return i;
        }
        return -1;
    }

    // Opens a custom-styled dialog to edit dietary restrictions, validates input, and triggers the database update.
   
    private void openUpdateDialogAndSave() {
        // Prevent data extraction issues if the cell is currently being clicked/edited
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        int row = getSelectedRowByCheckbox();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select exactly ONE trainee checkbox to update.");
            return;
        }

        // Extract required data from the selected row
        String fullName = String.valueOf(model.getValueAt(row, 1));
        String planId = String.valueOf(model.getValueAt(row, 3));
        String currentRestrictions = String.valueOf(model.getValueAt(row, 4));

        if (planId == null || planId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selected row has no Plan ID. Cannot update.");
            return;
        }

        // Build Custom Edit Dialog UI
        JDialog dialog = new JDialog(this, "Edit Dietary Restrictions", true);
        dialog.setSize(580, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(DesignUtils.BG_MAIN);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(DesignUtils.BG_MAIN);
        contentPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel info = new JLabel("Update restrictions for: " + fullName + "   (Plan ID: " + planId + ")");
        info.setFont(DesignUtils.FONT_HEADER);
        info.setForeground(DesignUtils.TEXT_MAIN);
        contentPanel.add(info, BorderLayout.NORTH);

        JTextArea area = new JTextArea(8, 35);
        DesignUtils.styleTextArea(area);
        // Prevent printing "null" string to the user interface
        area.setText("null".equalsIgnoreCase(currentRestrictions) ? "" : currentRestrictions);

        JScrollPane areaScroll = new JScrollPane(area);
        areaScroll.setPreferredSize(new Dimension(520, 180));
        DesignUtils.styleScrollPane(areaScroll);
        contentPanel.add(areaScroll, BorderLayout.CENTER);

        dialog.add(contentPanel, BorderLayout.CENTER);

        // Dialog Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(DesignUtils.BG_MAIN);

        JButton btnOk = new JButton("OK");
        JButton btnCancel = new JButton("Cancel");
        DesignUtils.styleButton(btnOk);
        DesignUtils.styleButton(btnCancel);
        btnOk.setPreferredSize(new Dimension(120, 40));
        btnCancel.setPreferredSize(new Dimension(120, 40));

        final boolean[] confirmed = {false}; // Track if user pressed OK
        btnOk.addActionListener(ev -> { confirmed[0] = true; dialog.dispose(); });
        btnCancel.addActionListener(ev -> dialog.dispose());

        btnPanel.add(btnOk);
        btnPanel.add(btnCancel);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        // Blocks execution until the dialog is closed
        dialog.setVisible(true);
        // User canceled the dialog
        if (!confirmed[0]) return; 

        // Input Validations 
        String newRestrictions = area.getText().replaceAll("\\s+", " ").trim();

        // Check if the input contains only numbers invalid for textual restrictions
        if (newRestrictions.matches("^[0-9\\s]+$")) {
            JOptionPane.showMessageDialog(this, "Restrictions must contain actual text, not just numbers.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Prevent unnecessary DB updates if the text hasn't actually changed
        String oldNormalized = (currentRestrictions == null || "null".equalsIgnoreCase(currentRestrictions) ? "" : currentRestrictions)
                .replaceAll("\\s+", " ").trim();

        if (newRestrictions.equals(oldNormalized)) {
            JOptionPane.showMessageDialog(this, "No changes were made.");
            return;
        }

        // Final Confirmation & Database Update 
        int confirm = JOptionPane.showConfirmDialog(this, "Save changes for " + fullName + "?",
                "Confirm Update", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Ensure a Dietitian is actively logged in to track the update source
        int loggedInId = UsersControl.getInstance().getLoggedInUserId();
        if (loggedInId == -1) {
            JOptionPane.showMessageDialog(this, "Error: No dietitian is currently logged in!",
                    "Session Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Pass to Control layer to perform DB transaction
        boolean ok = FitnessPlanControl.getInstance().updateRestrictions(planId, newRestrictions, loggedInId);

        if (ok) {
            JOptionPane.showMessageDialog(this, "Restrictions updated successfully!");
            loadTableData(); 
        } else {
            JOptionPane.showMessageDialog(this, "Update failed. Verify Plan ID exists.");
        }
    }
}