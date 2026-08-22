package rekammedis;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Diagram tubuh/kepala/tangan/kaki untuk formulir penandaan lokasi operasi --
 * gambar ASLI hasil crop dari kertas RM 30 (bukan gambar ulang vektor lagi;
 * percobaan gambar ulang sebelumnya dinilai user "aneh, tidak mirip"), disimpan
 * di gambar/penandaan1.png..penandaan6.png. Dipakai BERSAMA oleh
 * {@link PanelDiagramTanda} (interaktif, Swing) dan cetakan Jasper (statis)
 * lewat {@link PanelDiagramTanda#renderStatis} spy gambarnya konsisten di
 * layar & di cetakan.
 */
public final class DiagramTubuhWanita {

    private DiagramTubuhWanita() { }

    public enum Jenis {
        BADAN, KEPALA_PROFIL, KEPALA_DEPAN_BELAKANG, TANGAN_PALMAR, TANGAN_DORSAL, KAKI
    }

    private static final Map<Jenis, String> FILE_GAMBAR = new EnumMap<>(Jenis.class);
    static {
        FILE_GAMBAR.put(Jenis.BADAN, "penandaan1.png");
        FILE_GAMBAR.put(Jenis.KEPALA_PROFIL, "penandaan2.png");
        FILE_GAMBAR.put(Jenis.KEPALA_DEPAN_BELAKANG, "penandaan3.png");
        FILE_GAMBAR.put(Jenis.TANGAN_PALMAR, "penandaan4.png");
        FILE_GAMBAR.put(Jenis.TANGAN_DORSAL, "penandaan5.png");
        FILE_GAMBAR.put(Jenis.KAKI, "penandaan6.png");
    }

    /** Batas kotak tampil -- gambar sumber resolusi HD (bisa &gt;1000px, &gt;500KB/file), jadi
     * TIDAK PERNAH digambar dari ukuran HD aslinya langsung -- diskalakan turun SEKALI saja
     * lalu di-cache di sini; kalau tidak, tiap repaint (termasuk tiap event mouseDragged saat
     * menggambar tanda, yg bisa terpicu puluhan kali/detik) akan scale ulang gambar HD utuh
     * setiap kali & bikin form terasa berat/lag. */
    private static final int MAX_LEBAR = 340;
    private static final int MAX_TINGGI = 320;

    private static final Map<Jenis, BufferedImage> CACHE_GAMBAR = new EnumMap<>(Jenis.class);

    private static synchronized BufferedImage ambilGambar(Jenis jenis) {
        BufferedImage img = CACHE_GAMBAR.get(jenis);
        if (img != null) {
            return img;
        }
        try {
            BufferedImage asli = ImageIO.read(new File("./gambar/" + FILE_GAMBAR.get(jenis)));
            if (asli != null) {
                img = skalakanSekali(asli);
                CACHE_GAMBAR.put(jenis, img);
            }
        } catch (Exception e) {
            System.out.println("Notif muat gambar diagram " + jenis + " : " + e);
        }
        return img;
    }

    private static BufferedImage skalakanSekali(BufferedImage asli) {
        double skala = Math.min(MAX_LEBAR / (double) asli.getWidth(), MAX_TINGGI / (double) asli.getHeight());
        if (skala > 1) {
            skala = 1;
        }
        int w = Math.max(1, (int) Math.round(asli.getWidth() * skala));
        int h = Math.max(1, (int) Math.round(asli.getHeight() * skala));
        BufferedImage kecil = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = kecil.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(asli, 0, 0, w, h, null);
        g2.dispose();
        return kecil;
    }

    /** Ukuran tampil (sudah di-skala turun & di-cache, muat dalam kotak MAX_LEBAR x MAX_TINGGI). */
    public static Dimension ukuran(Jenis jenis) {
        BufferedImage img = ambilGambar(jenis);
        return img == null ? new Dimension(320, 160) : new Dimension(img.getWidth(), img.getHeight());
    }

    public static void gambar(Graphics2D g, Jenis jenis, int w, int h) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        BufferedImage img = ambilGambar(jenis);
        if (img != null) {
            g.drawImage(img, 0, 0, w, h, null);
        }
    }
}
