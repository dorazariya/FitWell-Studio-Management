package boundary;

import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.LineBorder;

import control.EmergencySimulator;
import control.UsersControl;

// Main login portal for the FitWell system.
// Supports four roles: Trainee, Consultant, Dietitian, StudioManager.
// Also starts the EmergencySimulator which monitors for emergency signals in the background.
public class FitWellPortalScreen extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String BACKGROUND_IMAGE_BASE_NAME = "FitWellpic";
    private Image backgroundImage;
    private JComboBox<String> portalSelector;
    private JTextField idInputField;
    private JButton loginButton;

    public static void main(String[] args) {
        try {
            // Remove focus ring from all buttons system-wide for cleaner UI
            UIManager.put("Button.focus", new javax.swing.plaf.ColorUIResource(new java.awt.Color(0, 0, 0, 0)));
            UIManager.put("Button.focusPainted", Boolean.FALSE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        EventQueue.invokeLater(() -> {
            FitWellPortalScreen frame = new FitWellPortalScreen();
            frame.setVisible(true);
        });

        // Start background emergency simulator — triggers alert and suspends all classes if emergency is detected
        EmergencySimulator simulator = new control.EmergencySimulator(() -> {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                    "EMERGENCY SIGNAL RECEIVED!\n" +
                    "The security system has detected an incident.\n" +
                    "All active and upcoming classes are now SUSPENDED.",
                    "FitWell - System Alert",
                    JOptionPane.ERROR_MESSAGE);
            });
        });

        simulator.startSimulation();
    }

    public FitWellPortalScreen() {
        super("FitWell Studio - Login Portal");
        initializeFrame();
        backgroundImage = loadBackgroundImage(BACKGROUND_IMAGE_BASE_NAME);

        BackgroundPanel rootPanel = new BackgroundPanel();
        rootPanel.setLayout(new GridBagLayout());
        setContentPane(rootPanel);

        JPanel loginCard = createLoginCard();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        rootPanel.add(loginCard, gbc);

        // Auto-focus the ID field when the screen opens
        SwingUtilities.invokeLater(() -> idInputField.requestFocusInWindow());
    }

    private void initializeFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    // Builds the glass-style login card with portal selector, ID input, and login button
    private JPanel createLoginCard() {
        GlassCardPanel card = new GlassCardPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(480, 260));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblChoose = new JLabel("Choose Portal:");
        lblChoose.setFont(new Font("Arial", Font.BOLD, 20));
        lblChoose.setForeground(Color.BLACK);

        portalSelector = new JComboBox<>(new String[]{"Trainee", "Consultant", "Dietitian", "StudioManager"});
        portalSelector.setFont(new Font("Arial", Font.PLAIN, 18));
        portalSelector.setPreferredSize(new Dimension(200, 36));
        portalSelector.setBackground(Color.WHITE);

        JLabel lblId = new JLabel("Enter ID:");
        lblId.setFont(new Font("Arial", Font.BOLD, 20));
        lblId.setForeground(Color.BLACK);

        idInputField = new JTextField();
        idInputField.setFont(new Font("Arial", Font.PLAIN, 18));
        idInputField.setPreferredSize(new Dimension(200, 36));
        idInputField.setBorder(new LineBorder(new Color(255, 255, 255, 150), 1, true));

        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 22));
        loginButton.setPreferredSize(new Dimension(220, 46));
        loginButton.setFocusPainted(false);

        // Clear ID field when switching portals to avoid cross-role confusion
        portalSelector.addActionListener(e -> {
            idInputField.setText("");
            idInputField.requestFocusInWindow();
        });

        idInputField.addActionListener(e -> doLogin());
        loginButton.addActionListener(e -> doLogin());

        gbc.gridx = 0; gbc.gridy = 0; card.add(lblChoose, gbc);
        gbc.gridx = 1; card.add(portalSelector, gbc);
        gbc.gridx = 0; gbc.gridy = 1; card.add(lblId, gbc);
        gbc.gridx = 1; card.add(idInputField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(20, 0, 10, 0);
        card.add(loginButton, gbc);

        return card;
    }

    // Validates ID input and routes the user to the correct screen based on selected portal role
    private void doLogin() {
        String portalRole = (String) portalSelector.getSelectedItem();
        String idStr = idInputField.getText().trim();

        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your ID.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!idStr.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "ID must be numbers only. Please try again.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int userId = Integer.parseInt(idStr);
        boolean success = false;
        UsersControl control = UsersControl.getInstance();

        switch (portalRole) {
            case "Trainee":
                if (control.loginTrainee(userId)) {
                    success = true;                    
                    this.dispose();
                    JOptionPane.showMessageDialog(null, "Welcome, " + control.getLoggedInUserName() + "!");
                    openTraineeScreen();
                }
                break;

            case "Consultant":
                if (control.loginConsultant(userId)) {
                    success = true;
                    this.dispose();
                    JOptionPane.showMessageDialog(null, "Welcome, " + control.getLoggedInUserName() + "!");
                    new StaffManagementScreen(false).setVisible(true);
                }
                break;

            case "Dietitian":
                if (control.loginDietitian(userId)) {
                    success = true;
                    this.dispose();
                    JOptionPane.showMessageDialog(null, "Welcome, " + control.getLoggedInUserName() + "!");
                    openDietitianScreen();
                }
                break;

            case "StudioManager":
                if (control.loginManager(userId)) {
                    success = true;
                    this.dispose();
                    JOptionPane.showMessageDialog(null, "Welcome, " + control.getLoggedInUserName() + "!");
                    // Studio manager gets full access — isManager=true unlocks additional menu items
                    new StaffManagementScreen(true).setVisible(true);
                }
                break;
        }

        if (!success) {
            JOptionPane.showMessageDialog(this,
                "User ID " + userId + " was not found for the " + portalRole + " portal.\nPlease check your details and try again.",
                "Access Denied", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openTraineeScreen() {
        new TraineeScreen().setVisible(true);
    }

    private void openDietitianScreen() {
        new DietitianScreen().setVisible(true);
    }

    // Searches common image directories for the background image file
    private Image loadBackgroundImage(String baseNameNoExt) {
        String projectRoot = System.getProperty("user.dir");
        String[] exts = {"jpg", "jpeg", "png", "jfif"};
        String[] dirs = {"images", "src/images", "picture"};

        for (String dir : dirs) {
            for (String ext : exts) {
                File f = new File(projectRoot + File.separator + dir + File.separator + baseNameNoExt + "." + ext);
                if (f.exists()) return new ImageIcon(f.getAbsolutePath()).getImage();
            }
        }
        return null;
    }

    // Draws the background image
    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            } else {
                setBackground(Color.DARK_GRAY);
            }
        }
    }

    // Semi-transparent rounded card used as the login form container
    private static class GlassCardPanel extends JPanel {
        public GlassCardPanel() { setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = 28;
            g2.setColor(new Color(255, 255, 255, 160));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(new Color(255, 255, 255, 200));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc);
            g2.dispose();
        }
    }
}