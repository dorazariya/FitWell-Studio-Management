package boundary;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import control.TrainingClassControl;

/* Emergency mode screen displayed when the system enters an emergency state.
** All classes are suspended automatically. The consultant can release the emergency
** to restore classes to their previous statuses.
*/
public class EmergencyMode extends JPanel {
    private static final long serialVersionUID = 1L;

    private JLabel lblEmergencyTitle;
    private JLabel lblExplain;
    private JButton btnRelease;

    public EmergencyMode() {
        setLayout(new GridBagLayout());
        DesignUtils.styleMainPanel(this);
        initComponents();
        registerListeners();
    }

    // Builds the UI: title, explanation label, and release button centered on screen
    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 20, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        lblEmergencyTitle = new JLabel("EMERGENCY MODE", SwingConstants.CENTER);
        lblEmergencyTitle.setFont(DesignUtils.FONT_TITLE.deriveFont(Font.BOLD, 48f));
        lblEmergencyTitle.setForeground(DesignUtils.HEADER_BG);
        lblEmergencyTitle.setOpaque(false);
        add(lblEmergencyTitle, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 10, 40, 10);

        lblExplain = new JLabel("All classes have been suspended. Release to restore them.", SwingConstants.CENTER);
        lblExplain.setFont(DesignUtils.FONT_TEXT.deriveFont(Font.PLAIN, 24f));
        lblExplain.setForeground(DesignUtils.TEXT_MAIN);
        lblExplain.setOpaque(false);
        add(lblExplain, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 10, 10, 10);

        btnRelease = new JButton("RELEASE EMERGENCY");
        DesignUtils.styleButton(btnRelease);
        btnRelease.setFont(DesignUtils.FONT_BUTTON.deriveFont(Font.BOLD, 20f));
        btnRelease.setPreferredSize(new Dimension(300, 60));
        add(btnRelease, gbc);
    }

    /* Release button asks for confirmation before restoring suspended classes.
    ** On success, classes are restored to SCHEDULED, ACTIVE, or COMPLETED based on their times.
    ** If no suspended classes exist, an informational message is shown instead.
     */
    private void registerListeners() {
        btnRelease.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to release from emergency mode?",
                    "Confirm Release",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (response == JOptionPane.YES_OPTION) {
                boolean success = TrainingClassControl.getInstance().releaseEmergencyMode();

                if (success) {
                    JOptionPane.showMessageDialog(
                            this,
                            "All suspended classes were released successfully",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    // No suspended classes found, nothing to release
                    JOptionPane.showMessageDialog(
                            this,
                            "There are no suspended classes to release",
                            "Info",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        });
    }
}