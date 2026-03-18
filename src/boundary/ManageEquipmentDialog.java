package boundary;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import javax.swing.border.EmptyBorder;

import javax.swing.table.DefaultTableModel;

import control.EquipmentControl;
import control.TrainingClassControl;
import entity.EquipmentType;

// Dialog for managing equipment requirements for a specific training class.
// The consultant can add, update, or remove equipment type requirements.
// On save, the system enforces a hard constraint checking for availability conflicts 
// with overlapping classes to prevent overbooking of physical equipment.
public class ManageEquipmentDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final int classID;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    
    // Callback executed after a successful save to refresh the parent UI panel
    private final Runnable onSuccessCallback;

    private JComboBox<String> cmbEquipmentType;
    private JTextField txtQuantity;
    private JTable tblEquipment;
    private DefaultTableModel modelEquipment;
    
    // Holds the actual data objects to be sent to the database, kept in sync with the table model
    private ArrayList<TrainingClassControl.EquipmentReqInput> requirementsList = new ArrayList<>();

    public ManageEquipmentDialog(Frame parent,int classID,LocalDateTime startTime,LocalDateTime endTime,Runnable onSuccessCallback) {
        super(parent, "Manage Equipment — Class #" + classID, true);
        this.classID = classID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.onSuccessCallback = onSuccessCallback;

        setSize(900, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        getContentPane().setBackground(DesignUtils.BG_MAIN);

        add(buildContent(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        // Populate the table and bring a list with data already saved in the database
        loadExistingRequirements();
    }

    // Builds the main content area combining the input form at the top and the data table below it
    private JPanel buildContent() {
        JPanel pnl = new JPanel(new BorderLayout(8, 8));
        pnl.setBackground(DesignUtils.SURFACE);
        pnl.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(DesignUtils.GRID, 1, true),
                "Equipment Requirements",
                0, 0,
                DesignUtils.FONT_HEADER
        ));

        JPanel pnlSelect = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlSelect.setOpaque(false);

        cmbEquipmentType = new JComboBox<>();
        cmbEquipmentType.setPreferredSize(new Dimension(260, 35));
        loadEquipmentTypes();
        DesignUtils.styleFilterComboBox(cmbEquipmentType);

        txtQuantity = new JTextField(6);
        DesignUtils.styleFilterTextField(txtQuantity);

        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        DesignUtils.styleButton(btnAdd);
        DesignUtils.styleButton(btnUpdate);

        JLabel lblType = new JLabel("Type:");
        DesignUtils.styleFilterLabel(lblType);

        JLabel lblQty = new JLabel("Qty:");
        DesignUtils.styleFilterLabel(lblQty);

        pnlSelect.add(lblType);
        pnlSelect.add(cmbEquipmentType);
        pnlSelect.add(lblQty);
        pnlSelect.add(txtQuantity);
        pnlSelect.add(btnAdd);
        pnlSelect.add(btnUpdate);

        pnl.add(pnlSelect, BorderLayout.NORTH);

        String[] cols = {"Equipment Type", "Required Quantity"};
        modelEquipment = new DefaultTableModel(cols, 0) {
            private static final long serialVersionUID = 1L;
            // Prevent direct cell editing to force users to use the update form above
            @Override 
            public boolean isCellEditable(int r, int c) { 
            	return false;
            }
        };

        tblEquipment = new JTable(modelEquipment);
        DesignUtils.styleTableCentered(tblEquipment);

        tblEquipment.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblEquipment.getSelectedRow();
                if (row != -1) {
                    String type = String.valueOf(modelEquipment.getValueAt(row, 0));
                    int qty = (int) modelEquipment.getValueAt(row, 1);
                    cmbEquipmentType.setSelectedItem(type);
                    txtQuantity.setText(String.valueOf(qty));
                }
            }
        });

        JScrollPane sp = new JScrollPane(tblEquipment);
        DesignUtils.styleScrollPane(sp);
        pnl.add(sp, BorderLayout.CENTER);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlBottom.setOpaque(false);

        JButton btnRemove = new JButton("Remove Selected");
        DesignUtils.styleButton(btnRemove);

        pnlBottom.add(btnRemove);
        pnl.add(pnlBottom, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> addItem());
        btnUpdate.addActionListener(e -> updateItem());
        btnRemove.addActionListener(e -> removeSelected());

        return pnl;
    }

    private JPanel buildButtons() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        pnl.setBackground(DesignUtils.BG_MAIN);
        pnl.setBorder(new EmptyBorder(0, 0, 10, 0));

        JButton btnSave = new JButton("Save Changes");
        JButton btnCancel = new JButton("Cancel");

        btnSave.setPreferredSize(new Dimension(180, 45));
        btnCancel.setPreferredSize(new Dimension(140, 45));

        DesignUtils.styleButton(btnSave);
        DesignUtils.styleButton(btnCancel);

        btnSave.addActionListener(e -> saveChanges());
        btnCancel.addActionListener(e -> dispose());

        pnl.add(btnSave);
        pnl.add(btnCancel);
        return pnl;
    }

    // Validates inputs and adds a new equipment requirement
    // It blocks duplicate equipment types to maintain database integrity
    private void addItem() {
        String type = (String) cmbEquipmentType.getSelectedItem();
        String qtyStr = txtQuantity.getText().trim();
        if (type == null) return;

        if (findIndexByType(type) != -1) {
            JOptionPane.showMessageDialog(this, "Item exists! Use 'Update'.");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) throw new NumberFormatException();
            
            // Add to both the list and the visual table model to keep them synchronized
            requirementsList.add(new TrainingClassControl.EquipmentReqInput(type, qty));
            modelEquipment.addRow(new Object[]{type, qty});
            txtQuantity.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity.");
        }
    }

    // Modifies an existing requirement based on the selected type
    // Ensures the type exists before attempting to update its quantity
    private void updateItem() {
        String type = (String) cmbEquipmentType.getSelectedItem();
        String qtyStr = txtQuantity.getText().trim();
        if (type == null) return;

        int idx = findIndexByType(type);
        if (idx == -1) {
            JOptionPane.showMessageDialog(this, "Item not found! Use 'Add'.");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) throw new NumberFormatException();
            
            // Update both the data object and the visual table cell
            requirementsList.set(idx, new TrainingClassControl.EquipmentReqInput(type, qty));
            modelEquipment.setValueAt(qty, idx, 1);
            
            txtQuantity.setText("");
            tblEquipment.clearSelection();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity.");
        }
    }

    // Removes the currently selected row from both the UI table and the array list
    private void removeSelected() {
        int row = tblEquipment.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a row.");
            return;
        }
        modelEquipment.removeRow(row);
        requirementsList.remove(row);
    }

    // Fetches the saved requirements for this specific class from the database
    private void loadExistingRequirements() {
        requirementsList = TrainingClassControl.getInstance().getEquipmentInputsForClass(classID);
        modelEquipment.setRowCount(0);
        for (TrainingClassControl.EquipmentReqInput req : requirementsList) {
            modelEquipment.addRow(new Object[]{req.getTypeName(), req.getQuantity()});
        }
    }

    // Fetches all available equipment categories to populate the drop-down menu
    private void loadEquipmentTypes() {
        cmbEquipmentType.removeAllItems();
        ArrayList<EquipmentType> types = EquipmentControl.getInstance().getAllEquipmentTypes();
        if (types == null) return;

        for (EquipmentType t : types) {
            if (t != null && t.getTypeName() != null) {
                cmbEquipmentType.addItem(t.getTypeName());
            }
        }
        if (cmbEquipmentType.getItemCount() > 0) cmbEquipmentType.setSelectedIndex(0);
    }

    // Helper method to locate an equipment requirement in the list by its type name
    private int findIndexByType(String typeName) {
        for (int i = 0; i < requirementsList.size(); i++) {
            if (requirementsList.get(i).getTypeName().equalsIgnoreCase(typeName)) return i;
        }
        return -1;
    }

    // Saves the modified equipment list to the database
    private void saveChanges() {
        // Fetch the original list again to perform a dirty check
    	ArrayList<TrainingClassControl.EquipmentReqInput> original = TrainingClassControl.getInstance().getEquipmentInputsForClass(classID);

        // Dirty Check optimization compares the current UI list against the database original
        // If nothing was modified it prevents an unnecessary database transaction
        boolean changed = requirementsList.size() != original.size();
        if (!changed) {
            for (int i = 0; i < requirementsList.size(); i++) {
                if (!requirementsList.get(i).getTypeName().equals(original.get(i).getTypeName()) ||
                		requirementsList.get(i).getQuantity() != original.get(i).getQuantity()) {
                    changed = true;
                    break;
                }
            }
        }

        if (!changed) {
            JOptionPane.showMessageDialog(this, "No changes were made.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Save changes?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            // Attempts to update requirements and perform an overlapping inventory check
        	TrainingClassControl.getInstance().updateEquipmentRequirementsFromUI(classID, requirementsList, startTime, endTime);
            JOptionPane.showMessageDialog(this, "Saved successfully!");
            if (onSuccessCallback != null) onSuccessCallback.run();
            dispose();

        } catch (TrainingClassControl.EquipmentConflictException conflict) {
            // The database transaction was rolled back by the controller due to insufficient inventory
            // We alert the user with the specific conflicts and purposely keep the dialog open
            // This forces the consultant to resolve the issue before the class is updated
            StringBuilder msg = new StringBuilder("Cannot save! Conflicts detected:\n\n");
            for (String c : conflict.getConflicts()) msg.append("  ").append(c).append("\n");
            msg.append("\nPlease adjust the equipment quantities");
            
            JOptionPane.showMessageDialog(this, msg.toString(), "Conflict Detected", JOptionPane.ERROR_MESSAGE);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}