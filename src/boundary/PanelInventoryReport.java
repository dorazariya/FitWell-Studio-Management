package boundary;

import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import control.ReportControl;

// Panel for viewing and exporting the Equipment Inventory Report.
// Displays equipment usage statistics for the current year.
// Also supports exporting the report as XML to the SwiftFit system.
public class PanelInventoryReport extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final int PANEL_W = 1000;
    private static final int PANEL_H = 700;
    private static final int X = 50;

    private static final Font FONT_TITLE  = new Font("Arial", Font.BOLD, 32);
    private static final Font FONT_DESC   = new Font("Arial", Font.PLAIN, 16);
    private static final Font FONT_BTN    = new Font("Arial", Font.PLAIN, 22);
    private static final Font FONT_STATUS = new Font("Arial", Font.ITALIC, 14);

    private JLabel lblStatus;

    public PanelInventoryReport() {
        setLayout(null);
        setBounds(0, 0, PANEL_W, PANEL_H);
        DesignUtils.styleMainPanel(this);

        JLabel lblTitle = new JLabel("Equipment Inventory Report");
        DesignUtils.styleTitle(lblTitle);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setHorizontalAlignment(SwingConstants.LEADING);
        lblTitle.setBounds(X, 50, 600, 40);
        add(lblTitle);

        JLabel lblDesc = new JLabel("View equipment usage statistics for the current year");
        lblDesc.setFont(FONT_DESC);
        lblDesc.setForeground(DesignUtils.TEXT_MAIN);
        lblDesc.setBounds(X, 100, 500, 25);
        lblDesc.setOpaque(false);
        add(lblDesc);

        JButton btnViewReport = new JButton("View Inventory Report");
        DesignUtils.styleButton(btnViewReport);
        btnViewReport.setFont(FONT_BTN);
        btnViewReport.setBounds(X, 180, 300, 50);
        add(btnViewReport);

        // Export button sends XML to SwiftFit at end of year
        JButton btnExportXML = new JButton("Export to XML (SwiftFit)");
        DesignUtils.styleButton(btnExportXML);
        btnExportXML.setFont(FONT_BTN);
        btnExportXML.setBounds(X, 260, 300, 50);
        add(btnExportXML);

        lblStatus = new JLabel("");
        lblStatus.setFont(FONT_STATUS);
        lblStatus.setForeground(DesignUtils.TEXT_MAIN);
        lblStatus.setBounds(X, 340, 500, 25);
        lblStatus.setOpaque(false);
        add(lblStatus);

        // Opens the inventory report in a JasperReports viewer window
        btnViewReport.addActionListener(e -> {
            try {
                JFrame reportFrame = ReportControl.getInstance().produceInventoryReport();
                if (reportFrame != null) reportFrame.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to generate report:\n" + ex.getMessage(),
                        "System Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Exports inventory data to XML and notifies the user of the file path
        btnExportXML.addActionListener(e -> {
            String path = System.getProperty("user.dir") + "/EquipmentInventoryExport.xml";
            boolean success = ReportControl.getInstance().exportInventoryToXML(path);

            if (success) {
                lblStatus.setText("Exported successfully to: " + path);
                JOptionPane.showMessageDialog(this,
                        "File sent to SwiftFit System!\nFile exported successfully!\n" + path,
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } else {
                lblStatus.setText("Export failed.");
                JOptionPane.showMessageDialog(this, "Failed to export XML file.",
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}