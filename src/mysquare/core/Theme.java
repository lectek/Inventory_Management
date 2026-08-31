package mysquare.core;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;

final class Theme {

    static final Color BACKGROUND = new Color(0xF4, 0xF2, 0xEC);
    static final Color SURFACE = new Color(0xFF, 0xFF, 0xFF);
    static final Color BORDER = new Color(0xD8, 0xD6, 0xC9);
    static final Color ACCENT = new Color(0x2F, 0x6F, 0x62);
    static final Color ACCENT_DARK = new Color(0x21, 0x50, 0x46);
    static final Color TEXT = new Color(0x1C, 0x24, 0x30);
    static final Color TEXT_MUTED = new Color(0x55, 0x5F, 0x6B);
    static final Color TABLE_HEADER_BG = new Color(0xEA, 0xE8, 0xE0);
    static final Color TABLE_STRIPE = new Color(0xF7, 0xF6, 0xF1);

    static final Font FONT_LABEL = new Font("SansSerif", Font.PLAIN, 12);
    static final Font FONT_FIELD = new Font("SansSerif", Font.PLAIN, 13);
    static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD, 13);
    static final Font FONT_TABLE = new Font("SansSerif", Font.PLAIN, 13);
    static final Font FONT_TABLE_HEADER = new Font("SansSerif", Font.BOLD, 12);
    static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 14);

    private Theme() {
    }

    /** Applies Nimbus (bundled with the JDK) if available; silently keeps the platform default otherwise. */
    static void applyLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (Exception ignored) {
            // Keep whatever look and feel Swing already picked.
        }
    }

    /** A label stacked above a field, used to give the add/dispatch forms a clearer caption per input. */
    static JPanel labeledField(String label, JComponent field) {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setOpaque(false);
        JLabel caption = new JLabel(label);
        caption.setFont(FONT_LABEL);
        caption.setForeground(TEXT_MUTED);
        caption.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        field.setFont(FONT_FIELD);
        field.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        wrap.add(caption);
        wrap.add(field);
        wrap.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        return wrap;
    }

    /** Titled section border used to group a form inside a panel. */
    static Border sectionBorder(String title) {
        Border line = BorderFactory.createLineBorder(BORDER);
        Border titled = BorderFactory.createTitledBorder(line, title);
        ((javax.swing.border.TitledBorder) titled).setTitleFont(FONT_TITLE);
        ((javax.swing.border.TitledBorder) titled).setTitleColor(ACCENT_DARK);
        return BorderFactory.createCompoundBorder(titled, BorderFactory.createEmptyBorder(4, 8, 8, 8));
    }
}
