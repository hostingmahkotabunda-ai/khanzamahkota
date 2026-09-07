package fungsi;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.KeyEventDispatcher;
import java.awt.Window;
import java.awt.event.KeyEvent;
import javax.swing.SwingUtilities;
import javax.swing.JButton;

/** Pasangan glass pane: menahan keyboard tanpa mengubah enabled/hak akses komponen. */
public final class PengunciInputSementara implements KeyEventDispatcher {
    private final Component area;
    private final Component kontrolPemuatan;
    private boolean terkunci;

    public PengunciInputSementara(Component area, Component kontrolPemuatan) {
        this.area = area;
        this.kontrolPemuatan = kontrolPemuatan;
    }

    public void kunci() {
        if (!terkunci) {
            terkunci = true;
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
        }
    }

    public void buka() {
        if (terkunci) {
            terkunci = false;
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
        }
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (!terkunci || !(event.getSource() instanceof Component)) return false;
        Component sumber = (Component)event.getSource();
        boolean kontrol = diDalam(sumber, kontrolPemuatan);
        Window jendela = SwingUtilities.getWindowAncestor(area);
        Window asal = sumber instanceof Window ? (Window)sumber : SwingUtilities.getWindowAncestor(sumber);
        if (kontrol || diDalam(sumber, area) || (jendela != null && jendela == asal)) {
            // Tetap izinkan penutupan jendela standar dari keyboard.
            if (event.getKeyCode() == KeyEvent.VK_F4 && event.isAltDown()) return false;
            // Mnemonic tombol form (misalnya Alt+S) tetap aktif walaupun fokus
            // berada di glass pane. Hanya navigasi/kontrol pemuatan yang diizinkan.
            if (kontrol && !event.isAltDown() && !event.isControlDown() && !event.isMetaDown()) {
                int kode = event.getKeyCode();
                if (kode == KeyEvent.VK_TAB || kode == KeyEvent.VK_SPACE
                        || kode == KeyEvent.VK_LEFT || kode == KeyEvent.VK_RIGHT
                        || kode == KeyEvent.VK_UP || kode == KeyEvent.VK_DOWN) return false;
                if (kode == KeyEvent.VK_ENTER && event.getID() == KeyEvent.KEY_PRESSED
                        && sumber instanceof JButton) {
                    ((JButton)sumber).doClick(0);
                }
            }
            event.consume();
            return true;
        }
        return false;
    }

    private static boolean diDalam(Component sumber, Component induk) {
        return sumber == induk || SwingUtilities.isDescendingFrom(sumber, induk);
    }
}
