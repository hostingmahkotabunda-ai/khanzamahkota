import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Generator gambar latar splash screen & jendela update SIMRS Khanza.
 *
 * Jalankan dari root proyek:
 *   javac -d tools/splash/out tools/splash/SplashArtGenerator.java
 *   java -cp tools/splash/out SplashArtGenerator
 *
 * Output:
 *   src/splash/splash-khanza.png (+ @125pct/@150pct/@175pct/@200pct)  -> splash Java (manifest)
 *   tools/launcher/update-bg.png (+ update-bg@2x.png)                    -> jendela update launcher.ps1
 *
 * Bagian dinamis (status, persen, bar, nama RS, versi) TIDAK ada di gambar ini;
 * digambar di atasnya oleh simrskhanza.StartupProgress (Java) dan launcher.ps1.
 * Koordinat di sini harus sama dengan konstanta di kedua file itu.
 */
public class SplashArtGenerator {

    // ---- Geometri logis (skala 1x) ----
    static final int CARD_W = 612, CARD_H = 352, RADIUS = 26;
    static final int SPLASH_MARGIN_X = 24, SPLASH_MARGIN_TOP = 18, SPLASH_MARGIN_BOTTOM = 30;
    static final int PAD = 44;            // padding kiri/kanan isi kartu
    static final int BAR_Y = 280, BAR_H = 6;

    static final Color BG_A = new Color(0x0A1216), BG_B = new Color(0x112027);
    static final Color GREEN = new Color(0x7CC142), PINK = new Color(0xE91E78);

    static BufferedImage logo;

    public static void main(String[] args) throws Exception {
        File root = new File(args.length > 0 ? args[0] : ".");
        logo = cropToContent(ImageIO.read(new File(root, "logo_rs.png")));

        File splashDir = new File(root, "src/splash");
        splashDir.mkdirs();
        int[] pcts = {100, 125, 150, 175, 200};
        for (int p : pcts) {
            String name = p == 100 ? "splash-khanza.png" : "splash-khanza@" + p + "pct.png";
            ImageIO.write(render(p / 100.0, true, false), "png", new File(splashDir, name));
        }

        File launcherDir = new File(root, "tools/launcher");
        launcherDir.mkdirs();
        ImageIO.write(render(1.0, false, true), "png", new File(launcherDir, "update-bg.png"));
        ImageIO.write(render(2.0, false, true), "png", new File(launcherDir, "update-bg@2x.png"));
        System.out.println("Selesai. Logo crop: " + logo.getWidth() + "x" + logo.getHeight());
    }

    static BufferedImage render(double s, boolean withShadow, boolean updateMode) {
        int offX = withShadow ? SPLASH_MARGIN_X : 0;
        int offY = withShadow ? SPLASH_MARGIN_TOP : 0;
        int W = (int) Math.round((CARD_W + (withShadow ? SPLASH_MARGIN_X * 2 : 0)) * s);
        int H = (int) Math.round((CARD_H + (withShadow ? SPLASH_MARGIN_TOP + SPLASH_MARGIN_BOTTOM : 0)) * s);

        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        hints(g);
        g.scale(s, s);

        RoundRectangle2D card = new RoundRectangle2D.Double(offX, offY, CARD_W, CARD_H, RADIUS * 2, RADIUS * 2);

        // ---- Bayangan lembut ----
        if (withShadow) {
            BufferedImage sh = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gs = sh.createGraphics();
            hints(gs);
            gs.scale(s, s);
            gs.setColor(new Color(0, 0, 0, 120));
            gs.fill(new RoundRectangle2D.Double(offX + 4, offY + 10, CARD_W - 8, CARD_H - 4, RADIUS * 2, RADIUS * 2));
            gs.dispose();
            sh = blur(sh, (int) Math.round(12 * s));
            g.drawImage(sh, AffineTransform.getScaleInstance(1 / s, 1 / s), null);
        }

        // ---- Kartu: gradasi gelap + cahaya hijau & pink ----
        Shape oldClip = g.getClip();
        g.clip(card);
        g.setPaint(new GradientPaint(offX, offY, BG_A, offX + CARD_W, offY + CARD_H, BG_B));
        g.fill(card);
        glow(g, offX + 70, offY + 40, 300, GREEN, 0.30f);
        glow(g, offX + CARD_W - 40, offY + CARD_H + 10, 330, PINK, 0.28f);
        glow(g, offX + CARD_W * 0.55, offY - 60, 220, new Color(0x2BB3A3), 0.10f);

        // garis-garis halus diagonal (tekstur)
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(255, 255, 255, 6));
        for (int x = -CARD_H; x < CARD_W; x += 14) {
            g.draw(new Line2D.Double(offX + x, offY + CARD_H, offX + x + CARD_H, offY));
        }

        // lingkaran dekoratif besar di kanan atas
        g.setColor(new Color(255, 255, 255, 10));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new Ellipse2D.Double(offX + CARD_W - 170, offY - 120, 300, 300));
        g.draw(new Ellipse2D.Double(offX + CARD_W - 120, offY - 70, 200, 200));
        g.setClip(oldClip);

        // tepi kartu: sorot tipis
        g.setStroke(new BasicStroke(1f));
        g.setPaint(new GradientPaint(offX, offY, new Color(255, 255, 255, 46), offX, offY + CARD_H, new Color(255, 255, 255, 8)));
        g.draw(new RoundRectangle2D.Double(offX + 0.5, offY + 0.5, CARD_W - 1, CARD_H - 1, RADIUS * 2, RADIUS * 2));

        // ---- Logo dalam "lencana" kaca ----
        int badgeX = offX + PAD, badgeY = offY + 52, badgeW = 196, badgeH = 150;
        RoundRectangle2D badge = new RoundRectangle2D.Double(badgeX, badgeY, badgeW, badgeH, 36, 36);
        g.setPaint(new GradientPaint(badgeX, badgeY, Color.WHITE, badgeX, badgeY + badgeH, new Color(0xF1F5F7)));
        g.fill(badge);
        g.setColor(new Color(255, 255, 255, 60));
        g.setStroke(new BasicStroke(4f));
        g.draw(new RoundRectangle2D.Double(badgeX - 3, badgeY - 3, badgeW + 6, badgeH + 6, 42, 42));
        double lh = 112, lw = lh * logo.getWidth() / logo.getHeight();
        if (lw > badgeW - 28) { lw = badgeW - 28; lh = lw * logo.getHeight() / logo.getWidth(); }
        drawScaled(g, logo, badgeX + (badgeW - lw) / 2, badgeY + (badgeH - lh) / 2, lw, lh, s);

        // ---- Judul ----
        int tx = badgeX + badgeW + 30;
        g.setColor(GREEN.brighter());
        g.setFont(tracked(new Font("Segoe UI Semibold", Font.PLAIN, 11), 0.18f));
        g.drawString(updateMode ? "PEMBARUAN OTOMATIS" : "SISTEM INFORMASI RUMAH SAKIT", tx, offY + 84);

        g.setFont(new Font("Segoe UI", Font.BOLD, 44));
        g.setPaint(new GradientPaint(tx, 0, Color.WHITE, tx + 260, 0, new Color(0xDDE7EC)));
        g.drawString("SIMRS", tx - 2, offY + 130);
        g.setFont(new Font("Segoe UI Light", Font.PLAIN, 44));
        FontMetrics fm = g.getFontMetrics(new Font("Segoe UI", Font.BOLD, 44));
        g.setColor(Color.WHITE);
        g.drawString("Khanza", tx - 2, offY + 176);

        // aksen garis gradasi di bawah judul
        g.setPaint(new GradientPaint(tx, 0, GREEN, tx + 64, 0, PINK));
        g.fill(new RoundRectangle2D.Double(tx, offY + 192, 64, 4, 4, 4));

        // ---- Track progress bar ----
        int barX = offX + PAD, barW = CARD_W - PAD * 2;
        g.setColor(new Color(255, 255, 255, 22));
        g.fill(new RoundRectangle2D.Double(barX, offY + BAR_Y, barW, BAR_H, BAR_H, BAR_H));

        // ---- Footer statis ----
        g.setColor(new Color(255, 255, 255, 18));
        g.fill(new Rectangle2D.Double(barX, offY + CARD_H - 44, barW, 1));
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g.setColor(new Color(0x7E8E97));
        String credit = "Khanza.Soft Media";
        int cw = g.getFontMetrics().stringWidth(credit);
        g.drawString(credit, barX + barW - cw, offY + CARD_H - 20);

        g.dispose();
        return img;
    }

    static void glow(Graphics2D g, double cx, double cy, double r, Color c, float alpha) {
        Color c0 = new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.round(alpha * 255));
        Color c1 = new Color(c.getRed(), c.getGreen(), c.getBlue(), 0);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(cx, cy), (float) r,
                new float[]{0f, 1f}, new Color[]{c0, c1}));
        g.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
    }

    static void drawScaled(Graphics2D g, BufferedImage src, double x, double y, double w, double h, double s) {
        // turunkan resolusi bertahap supaya logo tetap halus
        int tw = (int) Math.round(w * s), th = (int) Math.round(h * s);
        BufferedImage cur = src;
        while (cur.getWidth() / 2 >= tw) {
            cur = resize(cur, cur.getWidth() / 2, cur.getHeight() / 2);
        }
        cur = resize(cur, tw, th);
        AffineTransform t = new AffineTransform();
        t.translate(x, y);
        t.scale(1 / s, 1 / s);
        g.drawImage(cur, t, null);
    }

    static BufferedImage resize(BufferedImage src, int w, int h) {
        BufferedImage out = new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        hints(g);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    static BufferedImage cropToContent(BufferedImage src) {
        int minX = src.getWidth(), minY = src.getHeight(), maxX = -1, maxY = -1;
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);
                int a = argb >>> 24, r = (argb >> 16) & 255, gg = (argb >> 8) & 255, b = argb & 255;
                boolean isi = a > 16 && !(r > 240 && gg > 240 && b > 240);
                if (isi) {
                    minX = Math.min(minX, x); minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x); maxY = Math.max(maxY, y);
                }
            }
        }
        if (maxX < 0) return src;
        int m = 4;
        minX = Math.max(0, minX - m); minY = Math.max(0, minY - m);
        maxX = Math.min(src.getWidth() - 1, maxX + m); maxY = Math.min(src.getHeight() - 1, maxY + m);
        return src.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    static Font tracked(Font f, float tracking) {
        Map<TextAttribute, Object> a = new HashMap<>();
        a.put(TextAttribute.TRACKING, tracking);
        return f.deriveFont(a);
    }

    static void hints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    /** Box blur 3 lintasan (mendekati gaussian), cukup untuk bayangan. */
    static BufferedImage blur(BufferedImage src, int r) {
        BufferedImage a = src;
        for (int i = 0; i < 3; i++) {
            a = boxBlur(a, r, true);
            a = boxBlur(a, r, false);
        }
        return a;
    }

    static BufferedImage boxBlur(BufferedImage src, int r, boolean horizontal) {
        int w = src.getWidth(), h = src.getHeight();
        int[] in = src.getRGB(0, 0, w, h, null, 0, w), out = new int[in.length];
        int len = horizontal ? w : h, lines = horizontal ? h : w;
        for (int l = 0; l < lines; l++) {
            long sa = 0, sr = 0, sg = 0, sb = 0;
            int win = r * 2 + 1;
            for (int i = -r; i <= r; i++) {
                int p = in[idx(l, clamp(i, len), horizontal, w)];
                sa += p >>> 24; sr += (p >> 16) & 255; sg += (p >> 8) & 255; sb += p & 255;
            }
            for (int i = 0; i < len; i++) {
                out[idx(l, i, horizontal, w)] = (int) (sa / win) << 24 | (int) (sr / win) << 16 | (int) (sg / win) << 8 | (int) (sb / win);
                int pOut = in[idx(l, clamp(i - r, len), horizontal, w)];
                int pIn = in[idx(l, clamp(i + r + 1, len), horizontal, w)];
                sa += (pIn >>> 24) - (pOut >>> 24);
                sr += ((pIn >> 16) & 255) - ((pOut >> 16) & 255);
                sg += ((pIn >> 8) & 255) - ((pOut >> 8) & 255);
                sb += (pIn & 255) - (pOut & 255);
            }
        }
        BufferedImage o = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        o.setRGB(0, 0, w, h, out, 0, w);
        return o;
    }

    static int clamp(int i, int len) { return i < 0 ? 0 : (i >= len ? len - 1 : i); }

    static int idx(int line, int i, boolean horizontal, int w) {
        return horizontal ? line * w + i : i * w + line;
    }
}
