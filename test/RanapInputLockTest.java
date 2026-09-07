import fungsi.PengunciInputSementara;
import java.awt.Component;
import java.awt.event.KeyEvent;
import javax.swing.*;

/** Uji penguncian keyboard tanpa database, termasuk mnemonic lintas komponen. */
public class RanapInputLockTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static boolean blocked(PengunciInputSementara lock, Component source,
            int id, int code, int modifiers) {
        KeyEvent event = new KeyEvent(source, id, System.currentTimeMillis(), modifiers,
                code, id == KeyEvent.KEY_TYPED ? 's' : KeyEvent.CHAR_UNDEFINED);
        boolean result = lock.dispatchKeyEvent(event);
        check(result == event.isConsumed(), "Hasil dispatcher dan konsumsi event berbeda");
        return result;
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel form = new JPanel();
            JTextField input = new JTextField("isi semula");
            JButton forbidden = new JButton("Hapus");
            forbidden.setEnabled(false);
            form.add(input); form.add(forbidden);
            JPanel loading = new JPanel();
            JButton close = new JButton("Tutup");
            loading.add(close);
            JButton otherWindow = new JButton("Jendela lain");
            int[] clicks = {0}, enabledChanges = {0};
            close.addActionListener(e -> clicks[0]++);
            input.addPropertyChangeListener("enabled", e -> enabledChanges[0]++);
            forbidden.addPropertyChangeListener("enabled", e -> enabledChanges[0]++);
            PengunciInputSementara lock = new PengunciInputSementara(form, loading);
            try {
                lock.kunci(); lock.kunci();
                for (int id : new int[]{KeyEvent.KEY_PRESSED, KeyEvent.KEY_RELEASED, KeyEvent.KEY_TYPED}) {
                    check(blocked(lock, input, id, id == KeyEvent.KEY_TYPED ? 0 : KeyEvent.VK_S, 0),
                            "Input pasien lolos saat memuat");
                }
                check(blocked(lock, close, KeyEvent.KEY_PRESSED, KeyEvent.VK_S, KeyEvent.ALT_DOWN_MASK),
                        "Mnemonic Simpan lolos dari panel pemuatan");
                check(blocked(lock, close, KeyEvent.KEY_RELEASED, KeyEvent.VK_S, KeyEvent.ALT_DOWN_MASK),
                        "Pelepasan mnemonic lolos");
                check(!blocked(lock, close, KeyEvent.KEY_PRESSED, KeyEvent.VK_SPACE, 0), "Space tombol terblokir");
                check(!blocked(lock, close, KeyEvent.KEY_PRESSED, KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK), "Navigasi terblokir");
                check(blocked(lock, close, KeyEvent.KEY_PRESSED, KeyEvent.VK_ENTER, 0) && clicks[0] == 1,
                        "Enter tidak menjalankan kontrol pemuatan");
                check(!blocked(lock, otherWindow, KeyEvent.KEY_PRESSED, KeyEvent.VK_S, 0), "Jendela lain terblokir");
                check(!blocked(lock, input, KeyEvent.KEY_PRESSED, KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK), "Alt+F4 terblokir");
                check(input.isEnabled() && input.isEditable() && !forbidden.isEnabled()
                        && input.getText().equals("isi semula") && enabledChanges[0] == 0, "Status/isi form berubah");
                lock.buka(); lock.buka();
                check(!blocked(lock, input, KeyEvent.KEY_PRESSED, KeyEvent.VK_S, 0), "Keyboard tetap terkunci setelah selesai");
                lock.kunci();
                check(blocked(lock, input, KeyEvent.KEY_PRESSED, KeyEvent.VK_S, 0), "Penguncian ulang gagal");
            } finally {
                lock.buka();
            }
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                JFrame window = new JFrame();
                JFrame unrelated = new JFrame();
                window.add(form);
                window.setGlassPane(loading);
                unrelated.add(otherWindow);
                try {
                    lock.kunci();
                    check(blocked(lock, window.getRootPane(), KeyEvent.KEY_PRESSED,
                            KeyEvent.VK_S, KeyEvent.ALT_DOWN_MASK), "Pintasan root jendela lolos");
                    check(blocked(lock, window, KeyEvent.KEY_PRESSED, KeyEvent.VK_S, 0), "Event jendela lolos");
                    check(!blocked(lock, otherWindow, KeyEvent.KEY_PRESSED, KeyEvent.VK_S, 0),
                            "Jendela lain ikut dikunci");
                } finally {
                    lock.buka(); window.dispose(); unrelated.dispose();
                }
            }
        });
        System.out.println("PASS RanapInputLockTest");
    }
}
