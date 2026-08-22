package rekammedis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Panel diagram tubuh/kepala/tangan/kaki yg bisa ditandai bebas (freehand)
 * pakai mouse -- tekan+seret utk menggambar garis merah di atas diagram.
 * Tanda disimpan sbg data VEKTOR (list titik per goresan, bukan bitmap
 * langsung) spy bisa di-undo/dimuat ulang & tetap bisa digambar ulang jernih
 * di cetakan Jasper lewat {@link #renderStatis}.
 */
public final class PanelDiagramTanda extends JPanel {

    private final DiagramTubuhWanita.Jenis jenis;
    /** Ukuran kanvas ACUAN (sama dgn yg dipakai {@link PanelDiagramTanda#renderStatis}) -- semua titik
     * goresan disimpan dlm koordinat kanvas acuan ini, BUKAN piksel layar mentah, krn panel ini sering
     * ditaruh dlm GridLayout yg meregangkan ukuran tampil sebenarnya jadi beda dari ukuran acuan
     * (dulu ini bikin tanda "geser"/tidak sinkron antara layar & cetakan Jasper). */
    private final Dimension ukuranKanvas;
    private final List<List<Point>> strokes = new ArrayList<>();
    private List<Point> strokeAktif;
    private Runnable onBerubah;

    public PanelDiagramTanda(DiagramTubuhWanita.Jenis jenis) {
        this.jenis = jenis;
        this.ukuranKanvas = DiagramTubuhWanita.ukuran(jenis);
        setPreferredSize(ukuranKanvas);
        setMinimumSize(ukuranKanvas);
        setMaximumSize(ukuranKanvas);
        setBackground(java.awt.Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(200, 206, 212)));
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                strokeAktif = new ArrayList<>();
                strokeAktif.add(keKanvas(e.getPoint()));
                strokes.add(strokeAktif);
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (strokeAktif != null) {
                    strokeAktif.add(keKanvas(e.getPoint()));
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                strokeAktif = null;
                if (onBerubah != null) {
                    onBerubah.run();
                }
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    /** Piksel layar (ukuran tampil aktual, bisa beda dari ukuranKanvas kalau diregangkan layout) -> koordinat kanvas acuan. */
    private Point keKanvas(Point layar) {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return layar;
        }
        double sx = ukuranKanvas.width / (double) getWidth();
        double sy = ukuranKanvas.height / (double) getHeight();
        return new Point((int) Math.round(layar.x * sx), (int) Math.round(layar.y * sy));
    }

    /** Koordinat kanvas acuan -> piksel layar (ukuran tampil aktual saat ini), kebalikan dari keKanvas(). */
    private Point keLayar(Point kanvas) {
        double sx = getWidth() / (double) ukuranKanvas.width;
        double sy = getHeight() / (double) ukuranKanvas.height;
        return new Point((int) Math.round(kanvas.x * sx), (int) Math.round(kanvas.y * sy));
    }

    public void setOnBerubah(Runnable r) {
        this.onBerubah = r;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        DiagramTubuhWanita.gambar(g, jenis, getWidth(), getHeight());
        gambarStrokesKe(g, strokesKeLayar());
    }

    /** Goresan (koordinat kanvas acuan) diskalakan ke ukuran tampil AKTUAL panel ini sebelum digambar di layar. */
    private List<List<Point>> strokesKeLayar() {
        List<List<Point>> hasil = new ArrayList<>();
        for (List<Point> s : strokes) {
            List<Point> t = new ArrayList<>();
            for (Point p : s) {
                t.add(keLayar(p));
            }
            hasil.add(t);
        }
        return hasil;
    }

    private static void gambarStrokesKe(Graphics2D g, List<List<Point>> daftarStrokes) {
        g.setColor(new Color(214, 39, 39));
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (List<Point> s : daftarStrokes) {
            if (s.size() < 2) {
                continue;
            }
            GeneralPath p = new GeneralPath();
            Point first = s.get(0);
            p.moveTo(first.x, first.y);
            for (int i = 1; i < s.size(); i++) {
                p.lineTo(s.get(i).x, s.get(i).y);
            }
            g.draw(p);
        }
    }

    public void hapusTerakhir() {
        if (!strokes.isEmpty()) {
            strokes.remove(strokes.size() - 1);
            repaint();
            if (onBerubah != null) {
                onBerubah.run();
            }
        }
    }

    public void bersihkan() {
        strokes.clear();
        repaint();
        if (onBerubah != null) {
            onBerubah.run();
        }
    }

    public boolean adaTanda() {
        return !strokes.isEmpty();
    }

    /** Serialize: tiap goresan "x,y x,y x,y", antar goresan dipisah ";". */
    public String simpanKeTeks() {
        return serialize(strokes);
    }

    private static String serialize(List<List<Point>> daftarStrokes) {
        StringBuilder sb = new StringBuilder();
        for (List<Point> s : daftarStrokes) {
            if (sb.length() > 0) {
                sb.append(";");
            }
            for (int i = 0; i < s.size(); i++) {
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(s.get(i).x).append(",").append(s.get(i).y);
            }
        }
        return sb.toString();
    }

    public void muatDariTeks(String teks) {
        strokes.clear();
        strokes.addAll(parse(teks));
        repaint();
    }

    private static List<List<Point>> parse(String teks) {
        List<List<Point>> hasil = new ArrayList<>();
        if (teks == null || teks.trim().isEmpty()) {
            return hasil;
        }
        for (String strokeStr : teks.split(";")) {
            if (strokeStr.trim().isEmpty()) {
                continue;
            }
            List<Point> s = new ArrayList<>();
            for (String ptStr : strokeStr.trim().split(" ")) {
                if (ptStr.trim().isEmpty()) {
                    continue;
                }
                String[] xy = ptStr.split(",");
                try {
                    s.add(new Point(Integer.parseInt(xy[0].trim()), Integer.parseInt(xy[1].trim())));
                } catch (Exception ignore) { }
            }
            if (!s.isEmpty()) {
                hasil.add(s);
            }
        }
        return hasil;
    }

    /** Dipakai dari ekspresi gambar Jasper (statis, tanpa komponen Swing) -- gambar diagram + tanda merah jadi 1 bitmap. */
    public static BufferedImage renderStatis(DiagramTubuhWanita.Jenis jenis, String strokeTeks) {
        Dimension d = DiagramTubuhWanita.ukuran(jenis);
        BufferedImage img = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, d.width, d.height);
        DiagramTubuhWanita.gambar(g, jenis, d.width, d.height);
        gambarStrokesKe(g, parse(strokeTeks));
        g.dispose();
        return img;
    }
}
