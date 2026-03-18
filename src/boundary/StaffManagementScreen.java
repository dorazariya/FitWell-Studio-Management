package boundary;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

// Main navigation screen for staff users (Consultant and Studio Manager).
// The menu bar changes based on the role — managers get additional Trainee Management options.
public class StaffManagementScreen extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final int BAR_HEIGHT = 60;
    private static final int FRAME_WIDTH = 1400;
    private static final int FRAME_HEIGHT = 800;

    // Command strings used to identify which panel to load
    private static final String CMD_REGISTER_TRAINEE = "Register New Trainee";
    private static final String CMD_MANAGE_TRAINEES = "Manage Trainees";

    private static final String CMD_MANAGE_STOCK = "Manage Stock & Status";
    private static final String CMD_REVIEW_FLAGGED = "Review Flagged Items";
    private static final String CMD_VIEW_INVENTORY = "View Equipment Inventory";

    private static final String CMD_CREATE_PLAN = "Create New Plan";
    private static final String CMD_MANAGE_PLAN = "Manage Fitness Plan";
    private static final String CMD_ASSIGN_TRAINEES = "Assign Trainees To Group Plan";

    private static final String CMD_UNREGISTERED_REPORT = "Unregistered Class Report";
    private static final String CMD_INVENTORY_REPORT = "Equipment Inventory Report";

    private static final String CMD_MANAGE_CLASSES = "Manage Classes";
    private static final String CMD_EMERGENCY = "Emergency Mode";

    private final JPanel mainPanel = new JPanel(new BorderLayout());

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            StaffManagementScreen frame = new StaffManagementScreen(true);
            frame.setVisible(true);
        });
    }

    // isManager=true adds Trainee Management menu and sets manager-specific title and default screen
    public StaffManagementScreen(boolean isManager) {
        super(isManager ? "FitWell Studio Manager System" : "FitWell Consultant System");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        menuBar.setPreferredSize(new Dimension(FRAME_WIDTH, BAR_HEIGHT));
        menuBar.setBorder(new EmptyBorder(0, 0, 0, 0));
        DesignUtils.styleMenuBar(menuBar);
        setJMenuBar(menuBar);

        // Trainee Management menu is exclusive to Studio Manager
        JMenuItem itemRegisterTrainee = new JMenuItem(CMD_REGISTER_TRAINEE);
        styleSubMenuItem(itemRegisterTrainee);

        JMenuItem itemManageTrainees = new JMenuItem(CMD_MANAGE_TRAINEES);
        styleSubMenuItem(itemManageTrainees);

        if (isManager) {
            JMenu menuTrainees = new JMenu("Trainee Management");
            styleTopMenu(menuTrainees, false);
            menuTrainees.add(itemRegisterTrainee);
            menuTrainees.add(itemManageTrainees);
            menuBar.add(menuTrainees);
        }

        // Equipment Management
        JMenu menuEquipment = new JMenu("Equipment Management");
        styleTopMenu(menuEquipment, false);

        JMenuItem itemManageStock = new JMenuItem(CMD_MANAGE_STOCK);
        styleSubMenuItem(itemManageStock);

        JMenuItem itemReviewFlagged = new JMenuItem(CMD_REVIEW_FLAGGED);
        styleSubMenuItem(itemReviewFlagged);

        JMenuItem itemInventoryView = new JMenuItem(CMD_VIEW_INVENTORY);
        styleSubMenuItem(itemInventoryView);

        menuEquipment.add(itemManageStock);
        menuEquipment.add(itemReviewFlagged);
        menuEquipment.add(itemInventoryView);
        menuBar.add(menuEquipment);

        // Plans Management
        JMenu menuPlans = new JMenu("Plans Management");
        styleTopMenu(menuPlans, false);

        JMenuItem itemCreatePlan = new JMenuItem(CMD_CREATE_PLAN);
        styleSubMenuItem(itemCreatePlan);

        JMenuItem itemManagePlan = new JMenuItem(CMD_MANAGE_PLAN);
        styleSubMenuItem(itemManagePlan);

        JMenuItem itemAssignTrainees = new JMenuItem(CMD_ASSIGN_TRAINEES);
        styleSubMenuItem(itemAssignTrainees);

        menuPlans.add(itemCreatePlan);
        menuPlans.add(itemManagePlan);
        menuPlans.add(itemAssignTrainees);
        menuBar.add(menuPlans);

        // Reports
        JMenu menuReports = new JMenu("Reports");
        styleTopMenu(menuReports, false);

        JMenuItem itemUnregisteredReport = new JMenuItem(CMD_UNREGISTERED_REPORT);
        styleSubMenuItem(itemUnregisteredReport);

        JMenuItem itemInventoryReport = new JMenuItem(CMD_INVENTORY_REPORT);
        styleSubMenuItem(itemInventoryReport);

        menuReports.add(itemUnregisteredReport);
        menuReports.add(itemInventoryReport);
        menuBar.add(menuReports);

        // Classes — top-level item with no sub menu
        JMenuItem itemClasses = new JMenuItem(CMD_MANAGE_CLASSES);
        styleTopMenu(itemClasses, false);
        menuBar.add(itemClasses);

        // Emergency — highlighted in orange to draw attention
        JMenuItem itemEmergency = new JMenuItem(CMD_EMERGENCY);
        styleTopMenu(itemEmergency, true);
        menuBar.add(itemEmergency);

        DesignUtils.styleMainPanel(mainPanel);
        setContentPane(mainPanel);

        // Single listener handles all menu navigation by replacing the main panel content
        ActionListener menuListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                mainPanel.removeAll();

                switch (command) {
                    case CMD_REGISTER_TRAINEE:
                        mainPanel.add(new PanelStudioManagerRegisterTrainee(), BorderLayout.CENTER);
                        break;
                    case CMD_MANAGE_TRAINEES:
                        mainPanel.add(new PanelStudioManagerManageTrainees(), BorderLayout.CENTER);
                        break;
                    case CMD_MANAGE_STOCK:
                        mainPanel.add(new PanelManageEquipment(), BorderLayout.CENTER);
                        break;
                    case CMD_REVIEW_FLAGGED:
                        mainPanel.add(new PanelReviewFlagged(), BorderLayout.CENTER);
                        break;
                    case CMD_VIEW_INVENTORY:
                        mainPanel.add(new PanelEquipmentInventory(), BorderLayout.CENTER);
                        break;
                    case CMD_CREATE_PLAN:
                        mainPanel.add(new PanelCreatePlan(), BorderLayout.CENTER);
                        break;
                    case CMD_MANAGE_PLAN:
                        mainPanel.add(new PanelManagePlans(), BorderLayout.CENTER);
                        break;
                    case CMD_ASSIGN_TRAINEES:
                        mainPanel.add(new PanelAssignTraineesToGroupPlan(), BorderLayout.CENTER);
                        break;
                    case CMD_MANAGE_CLASSES:
                        mainPanel.add(new PanelClassManagement(), BorderLayout.CENTER);
                        break;
                    case CMD_UNREGISTERED_REPORT:
                        mainPanel.add(new PanelUnregisteredClassReport(), BorderLayout.CENTER);
                        break;
                    case CMD_INVENTORY_REPORT:
                        mainPanel.add(new PanelInventoryReport(), BorderLayout.CENTER);
                        break;
                    case CMD_EMERGENCY:
                        mainPanel.add(new EmergencyMode(), BorderLayout.CENTER);
                        break;
                }

                mainPanel.revalidate();
                mainPanel.repaint();
            }
        };

        // Attach listener to all menu items
        if (isManager) {
            itemRegisterTrainee.addActionListener(menuListener);
            itemManageTrainees.addActionListener(menuListener);
        }

        itemManageStock.addActionListener(menuListener);
        itemInventoryView.addActionListener(menuListener);
        itemReviewFlagged.addActionListener(menuListener);
        itemCreatePlan.addActionListener(menuListener);
        itemManagePlan.addActionListener(menuListener);
        itemAssignTrainees.addActionListener(menuListener);
        itemClasses.addActionListener(menuListener);
        itemUnregisteredReport.addActionListener(menuListener);
        itemInventoryReport.addActionListener(menuListener);
        itemEmergency.addActionListener(menuListener);

        // Default screen on open differs by role
        if (isManager) {
            mainPanel.add(new PanelStudioManagerRegisterTrainee(), BorderLayout.CENTER);
        } else {
            mainPanel.add(new PanelClassManagement(), BorderLayout.CENTER);
        }
    }

    // Styles top-level menu items — emergency items use orange to stand out
    private void styleTopMenu(JComponent component, boolean isEmergency) {
        component.setFont(DesignUtils.FONT_HEADER);
        component.setOpaque(true);
        component.setBackground(isEmergency ? DesignUtils.BUTTON_BG : DesignUtils.HEADER_BG);
        component.setForeground(Color.WHITE);

        component.setPreferredSize(null);
        component.setMinimumSize(new Dimension(100, BAR_HEIGHT));
        component.setMaximumSize(new Dimension(400, BAR_HEIGHT));

        component.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 0, 1, new Color(255, 255, 255, 50)),
                new EmptyBorder(0, 20, 0, 20)
        ));
    }

    // Styles
    private void styleSubMenuItem(JMenuItem item) {
        item.setFont(DesignUtils.FONT_TEXT);
        item.setBackground(DesignUtils.SURFACE);
        item.setForeground(DesignUtils.TEXT_MAIN);
        item.setOpaque(true);
        item.setBorder(new EmptyBorder(8, 15, 8, 15));
    }
}