package boundary;

import java.awt.Font;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;

import javax.swing.JSpinner.DateEditor;

import control.ReportControl;

// Panel for generating the Unregistered Class Report.
// The consultant selects a date range and exports a report showing trainees
public class PanelUnregisteredClassReport extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final int PANEL_W = 600;
    private static final int PANEL_H = 500;

    private static final int TITLE_X = 24;
    private static final int TITLE_Y = 65;
    private static final int TITLE_W = 413;
    private static final int TITLE_H = 32;

    private static final int LABEL_X = 57;
    private static final int CHOOSE_Y = 127;
    private static final int START_LABEL_Y = 188;
    private static final int END_LABEL_Y = 242;

    private static final int SPINNER_X = 157;
    private static final int SPINNER_W = 160;
    private static final int SPINNER_H = 32;
    private static final int START_SPINNER_Y = 181;
    private static final int END_SPINNER_Y = 235;

    private static final int BTN_Y = 306;
    private static final int BTN_W = 260;
    private static final int BTN_H = 45;

    private static final Font FONT_TITLE  = new Font("Arial", Font.BOLD, 28);
    private static final Font FONT_LABEL  = new Font("Arial", Font.PLAIN, 20);
    private static final Font FONT_INPUT  = new Font("Arial", Font.PLAIN, 18);
    private static final Font FONT_BUTTON = new Font("Arial", Font.PLAIN, 26);

    private final JSpinner spStartDate;
    private final JSpinner spEndDate;
    private final JButton btnExport = new JButton("Export Report");

    public PanelUnregisteredClassReport() {
        setLayout(null);
        setBounds(0, 0, PANEL_W, PANEL_H);
        DesignUtils.styleMainPanel(this);

        JLabel lblTitle = new JLabel("Unregistered Class Report", SwingConstants.CENTER);
        DesignUtils.styleTitle(lblTitle);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setBounds(TITLE_X, TITLE_Y, TITLE_W, TITLE_H);
        add(lblTitle);

        JLabel lblChoose = new JLabel("Choose Dates:");
        lblChoose.setFont(FONT_LABEL);
        lblChoose.setForeground(DesignUtils.TEXT_MAIN);
        lblChoose.setBounds(LABEL_X, CHOOSE_Y, 200, 24);
        lblChoose.setOpaque(false);
        add(lblChoose);

        JLabel lblStartDate = new JLabel("Start Date:");
        lblStartDate.setFont(FONT_LABEL);
        lblStartDate.setForeground(DesignUtils.TEXT_MAIN);
        lblStartDate.setBounds(LABEL_X, START_LABEL_Y, 140, 24);
        lblStartDate.setOpaque(false);
        add(lblStartDate);

        JLabel lblEndDate = new JLabel("End Date:");
        lblEndDate.setFont(FONT_LABEL);
        lblEndDate.setForeground(DesignUtils.TEXT_MAIN);
        lblEndDate.setBounds(LABEL_X, END_LABEL_Y, 140, 24);
        lblEndDate.setOpaque(false);
        add(lblEndDate);

        // Spinners are normalized before use to avoid time-of-day issues
        spStartDate = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        spStartDate.setBounds(SPINNER_X, START_SPINNER_Y, SPINNER_W, SPINNER_H);
        spStartDate.setEditor(new DateEditor(spStartDate, "dd/MM/yyyy"));
        DesignUtils.styleFilterSpinner(spStartDate);

        if (spStartDate.getEditor() instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) spStartDate.getEditor()).getTextField().setFont(FONT_INPUT);
        }
        add(spStartDate);

        spEndDate = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        spEndDate.setBounds(SPINNER_X, END_SPINNER_Y, SPINNER_W, SPINNER_H);
        spEndDate.setEditor(new DateEditor(spEndDate, "dd/MM/yyyy"));
        DesignUtils.styleFilterSpinner(spEndDate);

        if (spEndDate.getEditor() instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) spEndDate.getEditor()).getTextField().setFont(FONT_INPUT);
        }
        add(spEndDate);

        DesignUtils.styleButton(btnExport);
        btnExport.setFont(FONT_BUTTON);
        btnExport.setBounds(LABEL_X, BTN_Y, BTN_W, BTN_H);
        add(btnExport);

        btnExport.addActionListener(e -> exportReport());
    }

    // Validates the date range, normalizes start to 00:00 and end to 23:59:59,
    // then delegates report generation to ReportControl.
    private void exportReport() {
        Date start = (Date) spStartDate.getValue();
        Date end = (Date) spEndDate.getValue();

        // Convert to LocalDate to ignore time-of-day differences
        LocalDate s = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate e = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if (e.isBefore(s)) {
            JOptionPane.showMessageDialog(this, "End date cannot be before start date.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Start of day and end of day to cover full date range
        Date normalizedStart = Date.from(s.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date normalizedEnd = Date.from(e.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        try {
            JFrame reportFrame = ReportControl.getInstance().produceUnregisteredClassReport(normalizedStart, normalizedEnd);
            if (reportFrame != null) reportFrame.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to generate report:\n" + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public JButton getBtnImport() {
        return btnExport;
    }

    public Date getStartDate() {
        return (Date) spStartDate.getValue();
    }

    public Date getEndDate() {
        return (Date) spEndDate.getValue();
    }
}