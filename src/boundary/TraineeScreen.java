package boundary;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

public class TraineeScreen extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final int BAR_HEIGHT = 60;
    private static final int FRAME_WIDTH = 1280;
    private static final int FRAME_HEIGHT = 850;

    private static final String CMD_SCHEDULE = "My Schedule";
    private static final String CMD_REGISTER = "Register for Class";
    private static final String CMD_PROFILE  = "My Profile";

    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private String activeCommand = "";

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                TraineeScreen frame = new TraineeScreen();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public TraineeScreen() {
        super("FitWell Trainee System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);

        // Top navigation bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.setPreferredSize(new Dimension(FRAME_WIDTH, BAR_HEIGHT));
        menuBar.setLayout(new GridLayout(1, 3, 0, 0));
        DesignUtils.styleMenuBar(menuBar);

        JMenuItem itemSchedule = createTopNavItem(CMD_SCHEDULE, false);
        JMenuItem itemRegister = createTopNavItem(CMD_REGISTER, false);
        JMenuItem itemProfile  = createTopNavItem(CMD_PROFILE, true);

        menuBar.add(itemSchedule);
        menuBar.add(itemRegister);
        menuBar.add(itemProfile);

        setJMenuBar(menuBar);

        // Content
        DesignUtils.styleMainPanel(mainPanel);
        setContentPane(mainPanel);

        java.awt.event.ActionListener menuListener = e -> showPanel(menuBar, e.getActionCommand());
        itemSchedule.addActionListener(menuListener);
        itemRegister.addActionListener(menuListener);
        itemProfile.addActionListener(menuListener);

        // Default
        showPanel(menuBar, CMD_SCHEDULE);
    }

    private void showPanel(JMenuBar menuBar, String command) {
        mainPanel.removeAll();

        switch (command) {
            case CMD_SCHEDULE:
                mainPanel.add(new PanelTraineeSchedule(), BorderLayout.CENTER);
                break;
            case CMD_REGISTER:
                mainPanel.add(new PanelTraineeRegister(), BorderLayout.CENTER);
                break;
            case CMD_PROFILE:
                mainPanel.add(new PanelTraineeProfile(), BorderLayout.CENTER);
                break;
            default:
                mainPanel.add(new PanelTraineeSchedule(), BorderLayout.CENTER);
                command = CMD_SCHEDULE;
                break;
        }

        setActiveNavItem(menuBar, command);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Styles a single item in the top bar
    private JMenuItem createTopNavItem(String text, boolean isLast) {
        JMenuItem item = new JMenuItem(text);

        item.setFont(DesignUtils.FONT_HEADER);
        item.setForeground(DesignUtils.HEADER_FG);
        item.setBackground(DesignUtils.HEADER_BG);
        item.setOpaque(true);

        Color divider = new Color(255, 255, 255, 90);
        Border rightLine = isLast ? BorderFactory.createEmptyBorder()
                                  : new MatteBorder(0, 0, 0, 1, divider);

        item.setBorder(new CompoundBorder(
                rightLine,
                new EmptyBorder(10, 18, 10, 18)
        ));

        item.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!text.equals(activeCommand)) {
                    item.setBackground(DesignUtils.HEADER_BG.brighter());
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!text.equals(activeCommand)) {
                    item.setBackground(DesignUtils.HEADER_BG);
                }
            }
        });

        return item;
    }

    private void setActiveNavItem(JMenuBar menuBar, String command) {
        activeCommand = command;

        for (int i = 0; i < menuBar.getComponentCount(); i++) {
            Component c = menuBar.getComponent(i);
            if (!(c instanceof JMenuItem)) continue;

            JMenuItem item = (JMenuItem) c;

            if (command.equals(item.getText())) {
                item.setBackground(DesignUtils.BUTTON_BG.darker());
                item.setForeground(Color.WHITE);
            } else {
                item.setBackground(DesignUtils.HEADER_BG);
                item.setForeground(DesignUtils.HEADER_FG);
            }
            item.setOpaque(true);
        }
    }
}