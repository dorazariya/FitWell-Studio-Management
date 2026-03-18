package boundary;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import javax.swing.plaf.basic.BasicComboBoxUI;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public final class DesignUtils {

    private DesignUtils() {} 

    // THEME COLORS
    public static final Color BG_MAIN = Color.decode("#FBF6EE");
    public static final Color SURFACE = Color.WHITE;
    public static final Color ROW_ODD = Color.WHITE;
    public static final Color ROW_EVEN = Color.decode("#F3E9DA");
    public static final Color GRID = Color.decode("#D9C7B5");
    public static final Color HEADER_BG = Color.decode("#3B2B20");
    public static final Color HEADER_FG = Color.WHITE;
    public static final Color BUTTON_BG = Color.decode("#C07A4A");
    public static final Color BUTTON_FG = Color.WHITE;
    public static final Color TEXT_MAIN = Color.decode("#1F2937");

    public static final Color FILTER_BG = Color.decode("#F7EBDD");
    public static final Color FILTER_BORDER = Color.decode("#C9A98F");
    
    public static final Color BUTTON_DISABLED_BG = Color.decode("#DABCA8"); 
    public static final Color BUTTON_DISABLED_FG = Color.decode("#FBF6EE"); 
    
    // FONTS
    public static final Font FONT_TITLE = new Font("Arial", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Arial", Font.BOLD, 16);
    public static final Font FONT_TEXT = new Font("Arial", Font.PLAIN, 16);
    public static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 16);

    // PANEL STYLING
    public static void styleMainPanel(JPanel panel) {
        panel.setBackground(BG_MAIN);
        panel.setOpaque(true);
    }

    public static void styleCardPanel(JPanel panel) {
        panel.setBackground(SURFACE);
        panel.setOpaque(true);
        panel.setBorder(new CompoundBorder(new LineBorder(GRID, 1, true),new EmptyBorder(15, 15, 15, 15)));
    }

    // LABELS
    public static void styleTitle(JLabel lbl) {
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(TEXT_MAIN);
    }

    public static void styleFilterLabel(JLabel lbl) {
        lbl.setFont(FONT_TEXT);
        lbl.setForeground(Color.BLACK);
    }

    // BUTTONS
    public static void styleButton(JButton btn) {
        if (btn.isEnabled()) {
            btn.setBackground(BUTTON_BG);
            btn.setForeground(BUTTON_FG);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            btn.setBackground(BUTTON_DISABLED_BG);
            btn.setForeground(BUTTON_DISABLED_FG);
            btn.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        btn.setFont(FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setFocusable(false);

        btn.setBorder(new CompoundBorder(
                new LineBorder(BUTTON_BG.darker(), 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) {
                    btn.setBackground(BUTTON_BG.darker());
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) {
                    btn.setBackground(BUTTON_BG);
                }
            }
        });

        btn.addPropertyChangeListener("enabled", evt -> {
            if (btn.isEnabled()) {
                btn.setBackground(BUTTON_BG);
                btn.setForeground(BUTTON_FG);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
            } else {
                btn.setBackground(BUTTON_DISABLED_BG);
                btn.setForeground(BUTTON_DISABLED_FG);
                btn.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    // TABLE STYLING
    public static void styleTableBase(JTable table) {
        table.setFont(FONT_TEXT);
        table.setRowHeight(32);

        table.setShowGrid(true);
        table.setGridColor(GRID);
        table.setIntercellSpacing(new Dimension(1, 1));

        table.setSelectionBackground(BUTTON_BG);
        table.setSelectionForeground(Color.WHITE);

        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(HEADER_BG);
        header.setForeground(HEADER_FG);
        header.setFont(FONT_HEADER);
        header.setOpaque(true);
        header.setReorderingAllowed(false);
    }

    public static void styleTable(JTable table) {
        styleTableBase(table);
        installZebraRenderers(table, false);
    }

    public static void styleTableCentered(JTable table) {
        styleTableBase(table);
        installZebraRenderers(table, true);
        centerTableHeader(table);
    }

    private static void installZebraRenderers(JTable table, boolean centered) {
        table.setDefaultRenderer(Object.class, centered ? new ZebraCenteredRenderer() : new ZebraRenderer());
        table.setDefaultRenderer(Boolean.class, new ZebraBooleanRenderer());
    }

    private static void centerTableHeader(JTable table) {
        JTableHeader header = table.getTableHeader();
        if (header == null) return;

        final TableCellRenderer current = header.getDefaultRenderer();
        header.setDefaultRenderer((tbl, value, isSelected, hasFocus, row, column) -> {
            Component c = current.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
            if (c instanceof JLabel) {
                JLabel lbl = (JLabel) c;
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(FONT_HEADER);
                lbl.setOpaque(true);
                lbl.setBackground(HEADER_BG);
                lbl.setForeground(HEADER_FG);
                lbl.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            }
            return c;
        });
    }

    private static class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (c instanceof JComponent) {
                ((JComponent) c).setOpaque(true);
                ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            }

            setHorizontalAlignment(SwingConstants.LEADING);

            if (!isSelected) {
                c.setBackground((row % 2 == 0) ? ROW_ODD : ROW_EVEN);
                c.setForeground(TEXT_MAIN);
            }

            return c;
        }
    }

    private static class ZebraCenteredRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (c instanceof JComponent) {
                ((JComponent) c).setOpaque(true);
                ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            }

            setHorizontalAlignment(SwingConstants.CENTER);

            if (!isSelected) {
                c.setBackground((row % 2 == 0) ? ROW_ODD : ROW_EVEN);
                c.setForeground(TEXT_MAIN);
            }

            return c;
        }
    }

    private static class ZebraBooleanRenderer extends JCheckBox implements TableCellRenderer {
        public ZebraBooleanRenderer() {
            setHorizontalAlignment(CENTER);
            setBorder(BorderFactory.createEmptyBorder());
            setOpaque(true);
            setFocusable(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            boolean checked = (value instanceof Boolean) && (Boolean) value;
            setSelected(checked);

            if (checked || isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground((row % 2 == 0) ? DesignUtils.ROW_ODD : DesignUtils.ROW_EVEN);
            }

            return this;
        }
    }

    public static void styleScrollPane(JScrollPane sp) {
        sp.getViewport().setBackground(SURFACE);
        sp.setBorder(new LineBorder(GRID, 1, true));
        sp.setOpaque(true);

        if (sp.getVerticalScrollBar() != null) {
            sp.getVerticalScrollBar().setUnitIncrement(16);
        }
    }

    // TEXT AREA
    public static void styleTextArea(JTextArea ta) {
        ta.setFont(FONT_TEXT);
        ta.setForeground(TEXT_MAIN);
        ta.setBackground(FILTER_BG);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(new CompoundBorder(
                new LineBorder(FILTER_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    // MENU BAR
    public static void styleMenuBar(JMenuBar menuBar) {
        menuBar.setBackground(HEADER_BG);
        menuBar.setOpaque(true);
        menuBar.setBorder(new EmptyBorder(6, 10, 6, 10));

        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu m = menuBar.getMenu(i);
            if (m != null) {
                m.setForeground(Color.WHITE);
                m.setFont(FONT_HEADER);
            }
        }
    }

    // FILTER COMPONENTS STYLE
    private static void styleFilterBase(JComponent c) {
        c.setFont(FONT_TEXT);
        c.setBackground(FILTER_BG);
        c.setForeground(TEXT_MAIN);
        c.setOpaque(true);
        c.setBorder(new CompoundBorder(
                new LineBorder(FILTER_BORDER, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
    }

    public static void styleFilterComboBox(JComboBox<?> combo) {
        styleFilterBase(combo);

        combo.setFocusable(false);
        combo.setRequestFocusEnabled(false);

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, false);
                lbl.setFont(FONT_TEXT);
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

                if (isSelected && index >= 0) {
                    lbl.setBackground(BUTTON_BG);
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(FILTER_BG);
                    lbl.setForeground(TEXT_MAIN);
                }
                return lbl;
            }
        });

        combo.setUI(new BasicComboBoxUI() {
            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(FILTER_BG);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            @Override
            public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                super.paintCurrentValue(g, bounds, false);
            }
        });

        combo.revalidate();
        combo.repaint();
    }

    public static void styleFilterCheckBox(JCheckBox chk) {
        chk.setFont(FONT_TEXT);
        chk.setForeground(TEXT_MAIN);
        chk.setOpaque(false);
        chk.setFocusable(false);
    }

    public static void styleFilterSpinner(JSpinner spinner) {
        styleFilterBase(spinner);

        if (spinner.getEditor() instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
            tf.setFont(FONT_TEXT);
            tf.setBackground(FILTER_BG);
            tf.setForeground(TEXT_MAIN);
            tf.setBorder(BorderFactory.createEmptyBorder());
        }
    }

    public static void styleFilterTextField(JTextField tf) {
        styleFilterBase(tf);
    }
}