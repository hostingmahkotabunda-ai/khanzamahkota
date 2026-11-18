package simrskhanza;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.SplashScreen;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

/**
 * Indikator loading saat SIMRS Khanza dibuka.
 *
 * - Splash grafis: gambar latar dari manifest (SplashScreen-Image: splash/splash-khanza.png, muncul
 *   seketika saat JVM start), lalu kelas ini menggambar animasi di atasnya (status, spinner, bar,
 *   persen, nama RS, versi) dari thread sendiri, jadi tetap bergerak walau EDT sibuk membangun form.
 * - Konsol: status per tahap. Berwarna (ANSI + UTF-8) bila konsol Windows bisa dinyalakan mode VT-nya;
 *   kalau tidak (NetBeans, output dialihkan) jatuh ke teks polos.
 *
 * Persen dihitung per tahap. Bobot tiap tahap diambil dari durasi nyata pembukaan sebelumnya di PC ini
 * (cache/startup-profile.properties), jadi makin lama makin akurat.
 *
 * Semua method aman dipanggil walau mulai() tidak pernah dipanggil (jadi no-op).
 * Geometri overlay harus sama dengan tools/splash/SplashArtGenerator.java.
 */
public final class StartupProgress {

    public static final int KONFIGURASI = 0, DATABASE = 1, MODUL = 2, TAMPILAN = 3, FITUR = 4, PROFIL = 5;
    private static final String[] KUNCI = {"konfigurasi", "database", "modul", "tampilan", "fitur", "profil"};
    private static final String[] LABEL = {
        "Membaca konfigurasi",
        "Menghubungkan ke database",
        "Memuat modul layanan",
        "Menyusun tampilan menu",
        "Menyiapkan fitur & hak akses",
        "Memuat profil rumah sakit"
    };
    /** Durasi bawaan (ms) bila belum ada riwayat di PC ini. */
    private static final long[] DEFAULT_MS = {300, 900, 6000, 4500, 2500, 600};

    // ---- geometri (logis, skala 1x) - sama dengan SplashArtGenerator ----
    private static final int IMG_W = 660, OFF_X = 24, OFF_Y = 18;
    private static final int CARD_W = 612, CARD_H = 352, PAD = 44, BAR_Y = 280, BAR_H = 6;
    private static final int TEXT_X = PAD + 196 + 30;
    private static final Color GREEN = new Color(0x7CC142), PINK = new Color(0xE91E78);

    private static final File CACHE = new File("cache", "startup-profile.properties");

    private static volatile boolean aktif = false, selesai = false;
    private static volatile int tahap = -1;
    private static long mulaiNs, tahapMulaiNs;
    private static final long[] durasiMs = new long[KUNCI.length];
    private static final long[] bobotMs = DEFAULT_MS.clone();
    private static String namaRs = "", versi = "";
    private static volatile double tampil = 0;    // persen yang sedang ditampilkan (dihaluskan)
    private static Thread animator;
    private static boolean warna = false;           // konsol ANSI + UTF-8 aktif

    private StartupProgress() {
    }

    // =====================================================================
    //  API
    // =====================================================================

    public static synchronized void mulai() {
        if (aktif) {
            return;
        }
        aktif = true;
        mulaiNs = System.nanoTime();
        bacaProfil();
        versi = bacaVersi();
        warna = KonsolWindows.nyalakan();
        cetakHeader();
        masukTahap(KONFIGURASI);

        SplashScreen splash = null;
        try {
            splash = SplashScreen.getSplashScreen();
        } catch (Throwable ignored) {
            // headless / tidak didukung
        }
        if (splash != null) {
            final SplashScreen s = splash;
            animator = new Thread(() -> animasi(s), "startup-splash");
            animator.setDaemon(true);
            animator.start();
        }

        Thread t = new Thread(LauncherUpgrade::periksa, "launcher-upgrade");
        t.setDaemon(true);
        t.start();

        // Pengaman: bila konsol disembunyikan launcher tapi loading macet/terlalu lama
        // (mis. dialog error tertutup splash), munculkan konsol supaya log terbaca.
        Thread jaga = new Thread(() -> {
            try {
                Thread.sleep(90_000L);
            } catch (InterruptedException e) {
                return;
            }
            if (!selesai) {
                tampilkanKonsol();
            }
        }, "startup-jaga-konsol");
        jaga.setDaemon(true);
        jaga.start();
    }

    /**
     * Munculkan jendela konsol bila sedang disembunyikan (launcher menjalankan java dengan konsol
     * tersembunyi supaya saat loading hanya splash yang tampil). Ditaruh di BELAKANG jendela lain
     * dan tidak merebut fokus. Aman dipanggil berkali-kali / dari thread mana saja.
     */
    public static void tampilkanKonsol() {
        KonsolWindows.tampilkan();
    }

    /**
     * Pastikan jendela utama tampil di depan dan tidak ter-minimize. Perlu karena shortcut desktop
     * di-set "Run: Minimized" (supaya terminal tidak berkedip) dan Windows meneruskan status
     * minimized itu ke jendela pertama aplikasi; selain itu proses yang dijalankan di latar
     * (oleh launcher) bisa ditolak Windows saat minta fokus. Diulang sekali setelah jeda karena
     * Windows kadang menerapkan status awal sesudah setVisible.
     */
    public static void munculkanJendela(final java.awt.Frame f) {
        if (f == null) {
            return;
        }
        final Runnable depan = () -> {
            try {
                int st = f.getExtendedState();
                if ((st & java.awt.Frame.ICONIFIED) != 0) {
                    f.setExtendedState((st & ~java.awt.Frame.ICONIFIED) | java.awt.Frame.MAXIMIZED_BOTH);
                }
                boolean atas = f.isAlwaysOnTop();
                f.setAlwaysOnTop(true);   // naikkan z-order walau fokus ditolak
                f.toFront();
                f.requestFocus();
                f.setAlwaysOnTop(atas);
                KonsolWindows.depankan(f);
            } catch (Throwable ignored) {
            }
        };
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            depan.run();
        } else {
            javax.swing.SwingUtilities.invokeLater(depan);
        }
        javax.swing.Timer ulang = new javax.swing.Timer(600, e -> depan.run());
        ulang.setRepeats(false);
        ulang.start();
    }

    /** Pindah ke tahap berikutnya. Panggilan mundur/berulang diabaikan. */
    public static synchronized void tahap(int idx) {
        if (!aktif || selesai || idx <= tahap) {
            return;
        }
        tutupTahap();
        masukTahap(idx);
    }

    public static void selesai() {
        synchronized (StartupProgress.class) {
            if (!aktif || selesai) {
                return;
            }
            tutupTahap();
            selesai = true;
        }
        long totalMs = (System.nanoTime() - mulaiNs) / 1_000_000L;
        try {
            String n = nz(fungsi.akses.getnamars());
            if (!n.isEmpty()) {
                namaRs = n;
            }
        } catch (Throwable ignored) {
        }
        simpanProfil(totalMs);
        cetakSelesai(totalMs);

        if (animator != null) {
            try {
                animator.join(900);   // biarkan bar mengisi ke 100% lalu splash ditutup
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        try {
            SplashScreen s = SplashScreen.getSplashScreen();
            if (s != null && s.isVisible()) {
                s.close();
            }
        } catch (Throwable ignored) {
        }
    }

    /** Dipakai koneksiDB untuk mencetak banner lisensi; berwarna bila konsol mendukung. */
    public static void cetakBanner(String teks) {
        if (!warna) {
            System.out.println(teks);
            return;
        }
        StringBuilder sb = new StringBuilder();
        String[] baris = teks.split("\n", -1);
        int art = 0;
        for (String b : baris) {
            String t = b.trim();
            if (t.startsWith("#")) {
                Color c = campur(GREEN, PINK, Math.min(1, art++ / 5.0));
                sb.append(rgb(c)).append(b).append(R);
            } else if (t.startsWith("Koneksi Berhasil")) {
                sb.append(rgb(GREEN)).append(TEBAL).append(b).append(R);
            } else if (t.startsWith("Licensi") || t.startsWith("Informasi") || t.startsWith("Bagi")) {
                sb.append(rgb(new Color(0x8FA3AD))).append(b).append(R);
            } else {
                sb.append(rgb(new Color(0xC7D2D8))).append(b).append(R);
            }
            sb.append('\n');
        }
        System.out.print(sb);
        System.out.flush();
    }

    // =====================================================================
    //  Tahap & persen
    // =====================================================================

    private static void masukTahap(int idx) {
        tahap = idx;
        tahapMulaiNs = System.nanoTime();
    }

    private static void tutupTahap() {
        if (tahap < 0) {
            return;
        }
        long ms = (System.nanoTime() - tahapMulaiNs) / 1_000_000L;
        durasiMs[tahap] += ms;
        cetakTahap(LABEL[tahap], ms);
    }

    /** Persen target 0..100 berdasarkan tahap aktif dan waktu yang sudah berjalan di tahap itu. */
    private static double targetPersen() {
        if (selesai) {
            return 100;
        }
        int t = Math.max(0, tahap);
        double total = 0, sebelum = 0;
        for (int i = 0; i < bobotMs.length; i++) {
            total += bobotMs[i];
            if (i < t) {
                sebelum += bobotMs[i];
            }
        }
        double e = Math.max(150, bobotMs[t]);
        double jalan = (System.nanoTime() - tahapMulaiNs) / 1e6;
        // linier sampai perkiraan durasi (90% jatah tahap), lalu melambat, tidak pernah melewati 99%
        double f = jalan < e ? 0.9 * jalan / e : 0.9 + 0.09 * (1 - Math.exp(-(jalan - e) / e));
        return Math.min(99.5, (sebelum + f * bobotMs[t]) / total * 100.0);
    }

    // =====================================================================
    //  Splash overlay
    // =====================================================================

    private static void animasi(SplashScreen splash) {
        Graphics2D g;
        double skala;
        try {
            g = splash.createGraphics();
            skala = splash.getSize().getWidth() / IMG_W;
        } catch (Throwable e) {
            return;
        }
        long t0 = System.nanoTime();
        long selesaiSejak = 0;
        while (true) {
            double target = targetPersen();
            tampil = Math.max(tampil, tampil + (target - tampil) * (selesai ? 0.35 : 0.12));
            if (selesai && selesaiSejak == 0) {
                selesaiSejak = System.nanoTime();
            }
            double detik = (System.nanoTime() - t0) / 1e9;
            try {
                if (!splash.isVisible()) {
                    return;
                }
                gambar(g, skala, detik);
                splash.update();
            } catch (IllegalStateException closed) {
                return;   // splash sudah ditutup (mis. ada dialog error muncul)
            } catch (Throwable e) {
                return;
            }
            if (selesai && (tampil > 99.6 || System.nanoTime() - selesaiSejak > 700_000_000L)) {
                return;
            }
            try {
                Thread.sleep(33);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private static void gambar(Graphics2D g0, double skala, double detik) {
        Graphics2D g = (Graphics2D) g0.create();
        try {
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0, 0, 4000, 4000);
            g.setComposite(AlphaComposite.SrcOver);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.scale(skala, skala);
            g.translate(OFF_X, OFF_Y);

            int barX = PAD, barW = CARD_W - PAD * 2;
            double p = Math.min(100, Math.max(0, tampil));

            // nama rumah sakit (dari pembukaan sebelumnya)
            if (!namaRs.isEmpty()) {
                g.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                g.setColor(new Color(0xB9C6CD));
                g.drawString(potong(g.getFontMetrics(), namaRs, CARD_W - TEXT_X - PAD), TEXT_X, 226);
            }

            // spinner
            int sx = barX, sy = 256, sd = 13;
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(255, 255, 255, 30));
            g.draw(new Ellipse2D.Double(sx, sy, sd, sd));
            if (!selesai) {
                g.setPaint(new GradientPaint(sx, sy, GREEN, sx + sd, sy + sd, PINK));
                g.draw(new Arc2D.Double(sx, sy, sd, sd, -detik * 400, 110, Arc2D.OPEN));
            } else {
                g.setColor(GREEN);
                g.fill(new Ellipse2D.Double(sx, sy, sd, sd));
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawPolyline(new int[]{sx + 3, sx + 6, sx + 10}, new int[]{sy + 7, sy + 10, sy + 4}, 3);
            }

            // status
            g.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            g.setColor(new Color(0xD3DDE2));
            String status = selesai ? "Siap digunakan" : LABEL[Math.max(0, tahap)] + titik(detik);
            g.drawString(status, sx + sd + 10, sy + 11);

            // persen
            g.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
            String ps = (int) Math.floor(p) + "%";
            FontMetrics fm = g.getFontMetrics();
            g.setColor(Color.WHITE);
            g.drawString(ps, barX + barW - fm.stringWidth(ps), sy + 11);

            // bar
            double fw = Math.max(BAR_H, barW * p / 100.0);
            Shape isi = new RoundRectangle2D.Double(barX, BAR_Y, fw, BAR_H, BAR_H, BAR_H);
            // pendar di bawah bar
            g.setPaint(new GradientPaint(barX, 0, new Color(124, 193, 66, 50), barX + barW, 0, new Color(233, 30, 120, 60)));
            g.fill(new RoundRectangle2D.Double(barX - 1, BAR_Y - 2, fw + 2, BAR_H + 4, BAR_H + 4, BAR_H + 4));
            g.setPaint(new GradientPaint(barX, 0, GREEN, barX + barW, 0, PINK));
            g.fill(isi);
            // kilau berjalan
            Shape klipLama = g.getClip();
            g.clip(isi);
            double kx = barX - 120 + ((detik * 260) % (barW + 240));
            g.setPaint(new GradientPaint((float) kx, 0, new Color(255, 255, 255, 0),
                    (float) (kx + 60), 0, new Color(255, 255, 255, 150), true));
            g.fillRect((int) kx, BAR_Y, 120, BAR_H);
            g.setClip(klipLama);
            // titik terang di ujung
            double hx = barX + fw - BAR_H / 2.0, hy = BAR_Y + BAR_H / 2.0;
            g.setPaint(new RadialGradientPaint(new Point2D.Double(hx, hy), 14f,
                    new float[]{0f, 1f}, new Color[]{new Color(255, 120, 180, 170), new Color(233, 30, 120, 0)}));
            g.fill(new Ellipse2D.Double(hx - 14, hy - 14, 28, 28));

            // versi
            if (!versi.isEmpty()) {
                g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g.setColor(new Color(0x7E8E97));
                g.drawString("Versi " + versi, barX, CARD_H - 20);
            }
        } finally {
            g.dispose();
        }
    }

    private static String titik(double detik) {
        int n = (int) (detik * 2.5) % 4;
        return n == 0 ? "" : (n == 1 ? "." : (n == 2 ? ".." : "..."));
    }

    private static String potong(FontMetrics fm, String s, int maks) {
        if (fm.stringWidth(s) <= maks) {
            return s;
        }
        while (s.length() > 1 && fm.stringWidth(s + "…") > maks) {
            s = s.substring(0, s.length() - 1);
        }
        return s + "…";
    }

    // =====================================================================
    //  Konsol
    // =====================================================================

    private static final String ESC = "\u001b[", R = ESC + "0m", TEBAL = ESC + "1m";
    /**
     * Windows Terminal punya fallback font lengkap; konsol klasik (Consolas) tidak, jadi pakai simbol WGL4 saja.
     * Centang sengaja U+2713 (bukan U+2714 yang dirender sebagai emoji lebar berwarna).
     */
    private static final boolean WT = System.getenv("WT_SESSION") != null;
    private static final String CENTANG = WT ? "✓" : "√";
    private static final char SUDUT_KA = WT ? '╭' : '┌', SUDUT_KN = WT ? '╮' : '┐', SUDUT_BKI = WT ? '╰' : '└', SUDUT_BKN = WT ? '╯' : '┘';

    private static String rgb(Color c) {
        return ESC + "38;2;" + c.getRed() + ";" + c.getGreen() + ";" + c.getBlue() + "m";
    }

    private static Color campur(Color a, Color b, double t) {
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    private static String gradasi(String s, Color a, Color b) {
        StringBuilder sb = new StringBuilder();
        int n = Math.max(1, s.length() - 1);
        for (int i = 0; i < s.length(); i++) {
            sb.append(rgb(campur(a, b, (double) i / n))).append(s.charAt(i));
        }
        return sb.append(R).toString();
    }

    private static void cetakHeader() {
        String waktu = new SimpleDateFormat("dd MMM yyyy  HH:mm", new Locale("id", "ID")).format(new Date());
        if (!warna) {
            System.out.println("  ==== SIMRS Khanza - memulai aplikasi"
                    + (versi.isEmpty() ? "" : " (versi " + versi + ")") + " - " + waktu + " ====");
            return;
        }
        int lebar = 58;
        String garis = repeat('─', lebar);
        String b1 = "  " + TEBAL + "SIMRS Khanza" + R + rgb(new Color(0x8FA3AD)) + "  ·  memulai aplikasi" + R;
        String b1polos = "  SIMRS Khanza  ·  memulai aplikasi";
        String b2polos = "  " + (versi.isEmpty() ? "" : "Versi " + versi + "  ·  ") + waktu;
        String b2 = rgb(new Color(0x8FA3AD)) + b2polos + R;
        StringBuilder sb = new StringBuilder("\n");
        sb.append("  ").append(gradasi(SUDUT_KA + garis + SUDUT_KN, GREEN, PINK)).append('\n');
        sb.append("  ").append(rgb(GREEN)).append("│").append(R).append(b1).append(repeat(' ', lebar - b1polos.length()))
                .append(rgb(PINK)).append("│").append(R).append('\n');
        sb.append("  ").append(rgb(GREEN)).append("│").append(R).append(b2).append(repeat(' ', Math.max(0, lebar - b2polos.length())))
                .append(rgb(PINK)).append("│").append(R).append('\n');
        sb.append("  ").append(gradasi(SUDUT_BKI + garis + SUDUT_BKN, GREEN, PINK)).append("\n");
        System.out.println(sb);
    }

    private static void cetakTahap(String label, long ms) {
        String dtk = String.format(Locale.US, "%.2f dtk", ms / 1000.0).replace('.', ',');
        if (!warna) {
            System.out.println("  [OK] " + label + " (" + dtk + ")");
            return;
        }
        String isi = label + " " + repeat('·', Math.max(2, 40 - label.length()));
        Color warnaWaktu = ms > 8000 ? new Color(0xF2B33D) : new Color(0x6F8590);
        System.out.println("  " + rgb(GREEN) + CENTANG + R + " " + rgb(new Color(0xD3DDE2)) + isi + R
                + " " + rgb(warnaWaktu) + dtk + R);
    }

    private static void cetakSelesai(long totalMs) {
        String dtk = String.format(Locale.US, "%.1f detik", totalMs / 1000.0).replace('.', ',');
        if (!warna) {
            System.out.println("  ==== Siap digunakan dalam " + dtk + " ====");
            return;
        }
        System.out.println("\n  " + gradasi("●", GREEN, PINK) + " " + TEBAL + "Siap digunakan" + R
                + rgb(new Color(0x8FA3AD)) + " dalam " + dtk + R
                + "   " + gradasi(repeat(WT ? '━' : '─', 30), GREEN, PINK) + "\n");
    }

    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    // =====================================================================
    //  Profil (riwayat durasi per PC) & versi
    // =====================================================================

    private static void bacaProfil() {
        if (!CACHE.isFile()) {
            return;
        }
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(CACHE)) {
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            for (int i = 0; i < KUNCI.length; i++) {
                String v = p.getProperty("durasi." + KUNCI[i]);
                if (v != null) {
                    bobotMs[i] = Math.max(50, Long.parseLong(v.trim()));
                }
            }
            namaRs = nz(p.getProperty("nama_rs"));
        } catch (Exception ignored) {
        }
    }

    private static void simpanProfil(long totalMs) {
        Properties p = new Properties();
        for (int i = 0; i < KUNCI.length; i++) {
            // dihaluskan dengan riwayat sebelumnya supaya satu pembukaan lambat tidak merusak perkiraan
            long baru = durasiMs[i] > 0 ? durasiMs[i] : bobotMs[i];
            long rata = (bobotMs[i] + baru * 2) / 3;
            p.setProperty("durasi." + KUNCI[i], String.valueOf(rata));
        }
        p.setProperty("total_terakhir", String.valueOf(totalMs));
        if (!namaRs.isEmpty()) {
            p.setProperty("nama_rs", namaRs);
        }
        try {
            File dir = CACHE.getParentFile();
            if (dir != null && !dir.isDirectory()) {
                dir.mkdirs();
            }
            try (OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(CACHE), StandardCharsets.UTF_8)) {
                p.store(w, "Riwayat durasi pembukaan SIMRS Khanza (untuk persen loading)");
            }
        } catch (Exception ignored) {
        }
    }

    private static String bacaVersi() {
        File f = new File("version.txt");
        if (!f.isFile()) {
            return "";
        }
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String s = r.readLine();
            return s == null ? "" : s.trim();
        } catch (Exception e) {
            return "";
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s.trim();
    }

    // =====================================================================
    //  Konsol Windows: nyalakan mode VT (warna ANSI) + UTF-8 lewat JNA
    // =====================================================================

    static final class KonsolWindows {

        interface Kernel32Konsol extends com.sun.jna.Library {
            com.sun.jna.Pointer GetStdHandle(int n);

            boolean GetConsoleMode(com.sun.jna.Pointer h, com.sun.jna.ptr.IntByReference mode);

            boolean SetConsoleMode(com.sun.jna.Pointer h, int mode);

            boolean SetConsoleOutputCP(int cp);

            com.sun.jna.Pointer GetConsoleWindow();

            boolean SetConsoleTitleW(com.sun.jna.WString judul);
        }

        interface User32Jendela extends com.sun.jna.Library {
            boolean ShowWindow(com.sun.jna.Pointer hwnd, int cmd);

            boolean IsWindowVisible(com.sun.jna.Pointer hwnd);

            boolean SetWindowPos(com.sun.jna.Pointer hwnd, com.sun.jna.Pointer after, int x, int y, int cx, int cy, int flags);

            com.sun.jna.Pointer GetForegroundWindow();

            boolean SetForegroundWindow(com.sun.jna.Pointer hwnd);

            void SwitchToThisWindow(com.sun.jna.Pointer hwnd, boolean altTab);

            void keybd_event(byte vk, byte scan, int flags, com.sun.jna.Pointer extra);
        }

        /**
         * Ambil alih fokus untuk jendela f. Windows menolak SetForegroundWindow dari proses yang
         * dijalankan di latar ("foreground lock"), mis. kalau user sempat klik jendela lain selama
         * loading. Trik baku: kirim satu input tombol netral (F24, tidak dipakai aplikasi apa pun)
         * supaya proses ini dianggap penerima input terakhir, lalu SetForegroundWindow;
         * cadangan terakhir SwitchToThisWindow.
         */
        static void depankan(java.awt.Window f) {
            if (!windows() || f == null || !f.isDisplayable()) {
                return;
            }
            try {
                com.sun.jna.Pointer h = com.sun.jna.Native.getWindowPointer(f);
                if (h == null || h.equals(user32().GetForegroundWindow())) {
                    return;
                }
                final byte VK_F24 = (byte) 0x87;
                user32().keybd_event(VK_F24, (byte) 0, 0, null);
                user32().keybd_event(VK_F24, (byte) 0, 0x0002 /* KEYUP */, null);
                user32().SetForegroundWindow(h);
                if (!h.equals(user32().GetForegroundWindow())) {
                    user32().SwitchToThisWindow(h, true);
                }
            } catch (Throwable ignored) {
            }
        }

        private static final int STD_OUTPUT_HANDLE = -11, ENABLE_VIRTUAL_TERMINAL_PROCESSING = 0x0004;
        private static final int SW_SHOWNOACTIVATE = 4;
        private static final int SWP_NOSIZE = 0x1, SWP_NOMOVE = 0x2, SWP_NOACTIVATE = 0x10;

        private static Kernel32Konsol k32;
        private static User32Jendela u32;

        private static boolean windows() {
            return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows");
        }

        private static synchronized Kernel32Konsol kernel32() {
            if (k32 == null) {
                k32 = (Kernel32Konsol) com.sun.jna.Native.loadLibrary("kernel32", Kernel32Konsol.class);
            }
            return k32;
        }

        private static synchronized User32Jendela user32() {
            if (u32 == null) {
                u32 = (User32Jendela) com.sun.jna.Native.loadLibrary("user32", User32Jendela.class);
            }
            return u32;
        }

        /** Munculkan konsol tersembunyi di belakang jendela lain, tanpa merebut fokus. */
        static void tampilkan() {
            if (!windows()) {
                return;
            }
            try {
                com.sun.jna.Pointer h = kernel32().GetConsoleWindow();
                if (h == null || user32().IsWindowVisible(h)) {
                    return;   // tidak ada konsol, atau memang sudah terlihat (mis. dibuka lewat Aplikasi.bat)
                }
                kernel32().SetConsoleTitleW(new com.sun.jna.WString("SIMRS Khanza - Log Aplikasi"));
                user32().ShowWindow(h, SW_SHOWNOACTIVATE);
                user32().SetWindowPos(h, com.sun.jna.Pointer.createConstant(1) /* HWND_BOTTOM */, 0, 0, 0, 0,
                        SWP_NOSIZE | SWP_NOMOVE | SWP_NOACTIVATE);
            } catch (Throwable ignored) {
            }
        }

        static boolean nyalakan() {
            if (!windows() || "polos".equalsIgnoreCase(System.getenv("KHANZA_KONSOL"))) {
                return false;
            }
            try {
                Kernel32Konsol k = kernel32();
                com.sun.jna.Pointer h = k.GetStdHandle(STD_OUTPUT_HANDLE);
                com.sun.jna.ptr.IntByReference mode = new com.sun.jna.ptr.IntByReference();
                if (h == null || !k.GetConsoleMode(h, mode)) {
                    return false;   // bukan konsol (output dialihkan / NetBeans)
                }
                if (!k.SetConsoleMode(h, mode.getValue() | ENABLE_VIRTUAL_TERMINAL_PROCESSING)) {
                    return false;   // konsol lama tanpa dukungan VT
                }
                k.SetConsoleOutputCP(65001);
                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
                System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, "UTF-8"));
                return true;
            } catch (Throwable e) {
                return false;
            }
        }
    }
}
