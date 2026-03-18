package boundary;

import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import control.EquipmentControl;
import entity.EquipmentType;

// PanelReviewFlagged provides a pagination-based UI for reviewing and approving equipment types flagged by the system.
public class PanelReviewFlagged extends JPanel {
	 // UI Components 
	private static final long serialVersionUID = 1L;
	private ArrayList<EquipmentType> flaggedList;
    private int currentIndex = 0; 
    private JTextField txtItemName;
    private JTextField txtCategory;
    private JTextArea txtDescription;
    private JButton btnNext;
    private JButton btnPrev;
    private JButton btnApprove;
    private JLabel lblImage;
    private JLabel lblStatus;

    public PanelReviewFlagged() {
        setLayout(null); // Absolute positioning layout
        setBounds(0, 0, 1000, 700);
        DesignUtils.styleMainPanel(this);

        initComponents();
        loadData();
    }

     // Fetches the queue of flagged equipment types from the Control layer.
     // Resets the pagination index and updates the UI accordingly.
   
    public void loadData() {
        flaggedList = EquipmentControl.getInstance().getFlaggedEquipmentTypes();
        if (flaggedList != null && !flaggedList.isEmpty()) {
            currentIndex = 0;
            updateDisplay();
        } else {
            showEmptyState();
        }
    }

    private void updateDisplay() {
        EquipmentType currentItem = flaggedList.get(currentIndex);

        txtItemName.setText(currentItem.getTypeName());
        txtCategory.setText(currentItem.getCategory());
        txtDescription.setText(currentItem.getDescription());

        int confidence = currentItem.getExtractionConfidence();
        lblStatus.setText("Item " + (currentIndex + 1) + " of " + flaggedList.size() + "  (Confidence: " + confidence + "%)");

        // Resolve and load associated image dynamically
        String path = getEquipmentImagePath(currentItem.getTypeName());
        Image img = (path != null) ? new ImageIcon(path).getImage() : null;
        setImageToLabel(img);

        // Toggle pagination button states to prevent OutOfBounds
        btnPrev.setEnabled(currentIndex > 0);
        btnNext.setEnabled(currentIndex < flaggedList.size() - 1);
        btnApprove.setEnabled(true);
    }

    // Scales and sets the provided image to the label, or displays a fallback text if null.
     
    private void setImageToLabel(Image img) {
        if (img != null) {
            Image scaled = img.getScaledInstance(lblImage.getWidth(), lblImage.getHeight(), Image.SCALE_SMOOTH);
            lblImage.setIcon(new ImageIcon(scaled));
            lblImage.setText("");
        } else {
            lblImage.setIcon(null);
            lblImage.setText("Image Missing");
        }
    }

    // Clears all fields and disables actions when the review queue is empty.
     
    private void showEmptyState() {
        txtItemName.setText("");
        txtCategory.setText("");
        txtDescription.setText("");

        lblImage.setIcon(null);
        lblImage.setText("No flagged items found!");
        lblStatus.setText("Queue Empty");

        btnNext.setEnabled(false);
        btnPrev.setEnabled(false);
        btnApprove.setEnabled(false);
    }

    private void initComponents() {
        JLabel lblTitle = new JLabel("Flagged Equipment Review");
        DesignUtils.styleTitle(lblTitle);
        lblTitle.setBounds(33, 30, 520, 40);
        add(lblTitle);

        lblStatus = new JLabel();
        lblStatus.setFont(DesignUtils.FONT_TEXT.deriveFont(Font.BOLD, 16f));
        lblStatus.setForeground(DesignUtils.BUTTON_BG);
        lblStatus.setBounds(33, 470, 500, 20);
        lblStatus.setOpaque(false);
        add(lblStatus);

        JLabel lblTypeName = new JLabel("Type Name:");
        DesignUtils.styleFilterLabel(lblTypeName);
        lblTypeName.setFont(DesignUtils.FONT_TEXT.deriveFont(Font.PLAIN, 20f));
        lblTypeName.setBounds(33, 150, 150, 30);
        add(lblTypeName);

        JLabel lblDescription = new JLabel("Description:");
        DesignUtils.styleFilterLabel(lblDescription);
        lblDescription.setFont(DesignUtils.FONT_TEXT.deriveFont(Font.PLAIN, 20f));
        lblDescription.setBounds(33, 230, 150, 30);
        add(lblDescription);

        JLabel lblCategory = new JLabel("Category:");
        DesignUtils.styleFilterLabel(lblCategory);
        lblCategory.setFont(DesignUtils.FONT_TEXT.deriveFont(Font.PLAIN, 20f));
        lblCategory.setBounds(33, 350, 150, 30);
        add(lblCategory);

        txtItemName = new JTextField();
        txtItemName.setBounds(160, 150, 300, 30);
        DesignUtils.styleFilterTextField(txtItemName);
        txtItemName.setFont(DesignUtils.FONT_TEXT.deriveFont(Font.PLAIN, 18f));
        add(txtItemName);

        txtDescription = new JTextArea();
        txtDescription.setBounds(160, 230, 300, 100);
        DesignUtils.styleTextArea(txtDescription);
        txtDescription.setFont(DesignUtils.FONT_TEXT.deriveFont(Font.PLAIN, 18f));

        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setBounds(160, 230, 300, 100);
        DesignUtils.styleScrollPane(descScroll);
        add(descScroll);

        txtCategory = new JTextField();
        txtCategory.setBounds(160, 350, 300, 30);
        DesignUtils.styleFilterTextField(txtCategory);
        txtCategory.setFont(DesignUtils.FONT_TEXT.deriveFont(Font.PLAIN, 18f));
        add(txtCategory);

        // --- Image Display Area ---
        lblImage = new JLabel("Image Area", SwingConstants.CENTER);
        lblImage.setBounds(500, 150, 400, 300);
        lblImage.setBackground(DesignUtils.SURFACE);
        lblImage.setOpaque(true);
        lblImage.setBorder(BorderFactory.createLineBorder(DesignUtils.GRID, 1, true));
        add(lblImage);

        // Pagination and Action Buttons 
        btnPrev = new JButton("<< Prev");
        DesignUtils.styleButton(btnPrev);
        btnPrev.setBounds(33, 500, 120, 40);
        btnPrev.setFont(DesignUtils.FONT_BUTTON.deriveFont(Font.PLAIN, 16f));
        add(btnPrev);

        btnApprove = new JButton("Approve");
        DesignUtils.styleButton(btnApprove);
        btnApprove.setBounds(180, 500, 200, 40);
        btnApprove.setFont(DesignUtils.FONT_BUTTON.deriveFont(Font.BOLD, 16f));
        add(btnApprove);

        btnNext = new JButton("Next >>");
        DesignUtils.styleButton(btnNext);
        btnNext.setBounds(410, 500, 120, 40);
        btnNext.setFont(DesignUtils.FONT_BUTTON.deriveFont(Font.PLAIN, 16f));
        add(btnNext);

        // Navigation Listeners
        btnNext.addActionListener(e -> {
            if (flaggedList != null && currentIndex < flaggedList.size() - 1) {
                currentIndex++;
                updateDisplay();
            }
        });

        btnPrev.addActionListener(e -> {
            if (flaggedList != null && currentIndex > 0) {
                currentIndex--;
                updateDisplay();
            }
        });

        // Review Action Listener
        btnApprove.addActionListener(e -> {
            if (flaggedList == null || flaggedList.isEmpty()) return;

            // Extract potentially modified metadata from the UI fields
            String newName = txtItemName.getText();
            String newDesc = txtDescription.getText();
            String newCat = txtCategory.getText();
            String originalName = flaggedList.get(currentIndex).getTypeName();

            try {
                // Delegate the update transaction to the Control layer
                boolean success = EquipmentControl.getInstance().approveEquipmentType(originalName, newName, newDesc, newCat);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Item Approved Successfully!");
                    loadData(); // Re-fetch the queue 
                } else {
                    JOptionPane.showMessageDialog(this, "Operation could not be completed.");
                }
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "System error: " + ex.getMessage());
            }
        });
    }

    private String getEquipmentImagePath(String typeName) {
        String projectRoot = System.getProperty("user.dir");
        
        // Sanitize the filename by replacing special characters with underscores
        String safeFileName = typeName.replaceAll("[^a-zA-Z0-9_\\-]", "_");

        String specificPath = projectRoot + "/images/" + safeFileName + ".jpg";
        if (new File(specificPath).exists()) return specificPath;

        // Try raw name without sanitation as a secondary measure
        specificPath = projectRoot + "/images/" + typeName + ".jpg";
        if (new File(specificPath).exists()) return specificPath;

        String defaultPath = projectRoot + "/images/default.jpg";
        if (new File(defaultPath).exists()) return defaultPath;

        return null; 
    }
}