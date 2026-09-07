package widget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/**
 * Notifikasi banner ringan ("toast") yang muncul lalu HILANG SENDIRI (fade in -&gt; tahan
 * sebentar -&gt; fade out) tanpa perlu diklik OK/tutup -- pengganti JOptionPane.showMessageDialog()
 * utk notifikasi non-kritis (spt "berhasil disimpan"/"berhasil diedit") yg tidak boleh menghentikan
 * alur kerja pengguna. Dipakai pertama kali dari notifikasi SOAP di DlgRawatInap.java/DlgRawatJalan.java.
 */
public final class Toast {

    private static final Color HIJAU = new Color(0x38, 0x8E, 0x3C);
    private static final Color GARIS_HIJAU = new Color(0x2E, 0x7D, 0x32);

    /** 1 toast aktif per window pemilik -- kalau dipanggil lagi sblm yg lama hilang (mis. simpan
     *  dobel klik cepat), yg lama langsung ditutup dulu spy tidak menumpuk. */
    private static final Map<Window, JWindow> TOAST_AKTIF = new WeakHashMap<>();

    private Toast() {
    }

    /** Toast sukses (hijau, ada tanda centang) -- dipakai utk notifikasi "berhasil disimpan/diedit" dsb. */
    public static void sukses(Component pemilik, String pesan) {
        tampilkan(pemilik, "✓  " + pesan, HIJAU, GARIS_HIJAU, Color.WHITE);
    }

    /** Versi umum, warna bebas -- disediakan kalau nanti perlu toast error/warning dsb. */
    public static void tampilkan(Component pemilik, String pesan, Color latar, Color garis, Color teks) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> tampilkan(pemilik, pesan, latar, garis, teks));
            return;
        }

        final Window ownerWindow = (pemilik instanceof Window) ? (Window) pemilik
                : (pemilik == null ? null : SwingUtilities.getWindowAncestor(pemilik));

        JWindow lama = TOAST_AKTIF.get(ownerWindow);
        if (lama != null) {
            lama.dispose();
            TOAST_AKTIF.remove(ownerWindow);
        }

        final JWindow toast = new JWindow(ownerWindow);
        toast.setFocusableWindowState(false);

        JLabel label = new JLabel("<html>" + esc(pesan) + "</html>");
        label.setFont(new Font("Tahoma", Font.BOLD, 12));
        label.setForeground(teks);
        label.setBorder(new EmptyBorder(11, 20, 11, 20));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(latar);
        panel.setBorder(BorderFactory.createLineBorder(garis, 1));
        panel.add(label, BorderLayout.CENTER);

        toast.getContentPane().add(panel);
        toast.pack();

        Rectangle basis = (ownerWindow != null && ownerWindow.isShowing())
                ? ownerWindow.getBounds()
                : GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        toast.setLocation(basis.x + (basis.width - toast.getWidth()) / 2, basis.y + 36);

        boolean pakaiOpacityAwal;
        try {
            toast.setOpacity(0f);
            pakaiOpacityAwal = true;
        } catch (Exception e) {
            pakaiOpacityAwal = false;
        }
        final boolean pakaiOpacity = pakaiOpacityAwal;

        toast.setVisible(true);
        TOAST_AKTIF.put(ownerWindow, toast);

        final float[] op = {0f};
        final int[] tahap = {0}; // 0=fade-in, 1=tahan, 2=fade-out
        final int[] tick = {0};
        final Timer timer = new Timer(25, null);
        timer.addActionListener(ev -> {
            if (!toast.isDisplayable()) {
                timer.stop();
                return;
            }
            switch (tahap[0]) {
                case 0:
                    if (pakaiOpacity) {
                        op[0] = Math.min(1f, op[0] + 0.12f);
                        toast.setOpacity(op[0]);
                        if (op[0] >= 1f) { tahap[0] = 1; }
                    } else {
                        tahap[0] = 1;
                    }
                    break;
                case 1:
                    tick[0]++;
                    if (tick[0] > 70) { tahap[0] = 2; } // +-70*25ms = 1.75 detik tahan
                    break;
                case 2:
                    if (pakaiOpacity) {
                        op[0] = Math.max(0f, op[0] - 0.08f);
                        toast.setOpacity(op[0]);
                        if (op[0] <= 0f) {
                            timer.stop();
                            toast.dispose();
                            TOAST_AKTIF.remove(ownerWindow);
                        }
                    } else {
                        timer.stop();
                        toast.dispose();
                        TOAST_AKTIF.remove(ownerWindow);
                    }
                    break;
                default:
                    break;
            }
        });
        timer.start();
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }
}
