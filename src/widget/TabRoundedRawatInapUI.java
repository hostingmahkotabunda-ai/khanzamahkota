package widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/** Tab rounded ringkas khusus DlgRawatInap, tanpa komponen penangkap klik. */
public final class TabRoundedRawatInapUI extends BasicTabbedPaneUI {
    private static final Color BIRU = new Color(24, 112, 220);
    private static final Color TEKS = new Color(58, 80, 109);
    private static final Color GARIS = new Color(207, 220, 235);
    private static final Font FONT = new Font("Tahoma", Font.BOLD, 11);

    @Override protected void installDefaults() {
        super.installDefaults();
        tabInsets = new Insets(4, 9, 4, 9);
        selectedTabPadInsets = new Insets(0, 0, 0, 0);
        tabAreaInsets = new Insets(3, 2, 2, 2);
        tabRunOverlay = 0;
    }

    @Override protected int calculateTabHeight(int placement, int index, int fontHeight) {
        return 32;
    }

    @Override protected int calculateTabWidth(int placement, int index, FontMetrics metrics) {
        return Math.max(52, tabPane.getFontMetrics(FONT).stringWidth(tabPane.getTitleAt(index)) + 22);
    }

    @Override protected void paintTab(Graphics graphics, int placement, Rectangle[] bounds,
            int index, Rectangle iconRect, Rectangle textRect) {
        Rectangle r = bounds[index];
        boolean selected = tabPane.getSelectedIndex() == index;
        boolean enabled = tabPane.isEnabled() && tabPane.isEnabledAt(index);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int x = r.x + 2, y = r.y + 1, w = r.width - 5, h = r.height - 3;
            g.setColor(selected ? BIRU : (getRolloverTab() == index && enabled
                    ? new Color(229, 240, 254) : new Color(245, 248, 252)));
            g.fillRoundRect(x, y, w, h, 10, 10);
            g.setColor(selected ? BIRU.darker() : GARIS);
            g.drawRoundRect(x, y, w, h, 10, 10);
            Color foreground = !enabled ? new Color(148, 158, 170) : selected ? Color.WHITE : TEKS;
            g.setColor(foreground);
            g.setFont(FONT);
            FontMetrics fm = g.getFontMetrics();
            String title = tabPane.getTitleAt(index);
            g.drawString(title, x + (w - fm.stringWidth(title)) / 2,
                    y + (h - fm.getHeight()) / 2 + fm.getAscent());
            if (tabPane.hasFocus() && getFocusIndex() == index) {
                g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                        1f, new float[]{2f, 3f}, 0));
                g.drawRoundRect(x + 3, y + 3, w - 6, h - 6, 7, 7);
            }
        } finally {
            g.dispose();
        }
    }
}
