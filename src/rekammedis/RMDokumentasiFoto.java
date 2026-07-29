package rekammedis;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/**
 * Dokumentasi Foto Rawat Inap. Diakses klik-kanan pasien di DlgKamarInap.
 * Upload banyak foto sekaligus (JFileChooser multi-select) beserta
 * keterangan per foto, disimpan sbg BLOB (pola sama seperti "Cap Kaki" di
 * RMPenilaianAwalKeperawatanRanapBayi) di tabel log dokumentasi_foto_ranap
 * (1 baris = 1 foto, banyak baris per no_rawat).
 */
public final class RMDokumentasiFoto extends JDialog {

    private final sekuel Sequel = new sekuel();
    private final Connection koneksi = koneksiDB.condb();

    private final DefaultTableModel tabMode = new DefaultTableModel(null,
            new Object[]{"ID", "Foto", "Nama File", "Keterangan", "Tgl Upload", "Oleh"}) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
        @Override public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) { return Long.class; }
            if (columnIndex == 1) { return ImageIcon.class; }
            return String.class;
        }
    };

    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TRuangan = ro();

    private final widget.Table tbFoto = new widget.Table();
    private final widget.Button BtnUpload = new widget.Button();
    private final widget.Button BtnLihat = new widget.Button();
    private final widget.Button BtnHapus = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();

    public RMDokumentasiFoto(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Dokumentasi Foto Rawat Inap ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        ensureTable();
        initComponents();
        setSize(900, 560);
        setMinimumSize(new Dimension(700, 420));
        setLocationRelativeTo(parent);
    }

    private void ensureTable() {
        Sequel.queryu2(
                "create table if not exists dokumentasi_foto_ranap ("
                + "id int not null auto_increment primary key,"
                + "no_rawat varchar(17) not null,"
                + "keterangan varchar(255) null,"
                + "nama_file varchar(150) null,"
                + "photo longblob null,"
                + "tgl_upload datetime null,"
                + "created_by varchar(50) null,"
                + "index idx_no_rawat (no_rawat)"
                + ")");
    }

    private void initComponents() {
        final Color latar = new Color(246, 249, 251);
        final Color garis = new Color(215, 224, 230);
        final Color teks = new Color(32, 49, 66);

        getContentPane().setBackground(latar);
        getContentPane().setLayout(new BorderLayout(0, 10));

        JPanel atas = new JPanel();
        atas.setBackground(latar);
        atas.setLayout(new BoxLayout(atas, BoxLayout.Y_AXIS));
        atas.setBorder(new EmptyBorder(14, 18, 0, 18));

        JLabel judulUtama = new JLabel("Dokumentasi Foto Rawat Inap");
        judulUtama.setFont(new Font("Tahoma", Font.BOLD, 20));
        judulUtama.setForeground(teks);
        judulUtama.setAlignmentX(Component.LEFT_ALIGNMENT);
        atas.add(judulUtama);
        atas.add(Box.createVerticalStrut(8));

        JPanel ringkasanPasien = new JPanel(new java.awt.GridLayout(1, 4, 0, 0));
        ringkasanPasien.setBackground(Color.WHITE);
        ringkasanPasien.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis),
                new EmptyBorder(10, 12, 10, 12)));
        ringkasanPasien.setAlignmentX(Component.LEFT_ALIGNMENT);
        ringkasanPasien.add(fieldRingkasan("No. Rawat", TNoRw));
        ringkasanPasien.add(fieldRingkasan("No. RM", TNoRM));
        ringkasanPasien.add(fieldRingkasan("Nama Pasien", TPasien));
        ringkasanPasien.add(fieldRingkasan("Ruangan", TRuangan));
        atas.add(ringkasanPasien);

        getContentPane().add(atas, BorderLayout.NORTH);

        JPanel tengah = new JPanel(new BorderLayout());
        tengah.setBackground(latar);
        tengah.setBorder(new EmptyBorder(10, 18, 10, 18));

        tbFoto.setModel(tabMode);
        tbFoto.setRowHeight(56);
        tbFoto.getColumnModel().removeColumn(tbFoto.getColumnModel().getColumn(0));
        tbFoto.getColumnModel().getColumn(0).setPreferredWidth(70);
        tbFoto.getColumnModel().getColumn(1).setPreferredWidth(160);
        tbFoto.getColumnModel().getColumn(2).setPreferredWidth(320);
        tbFoto.getColumnModel().getColumn(3).setPreferredWidth(120);
        tbFoto.getColumnModel().getColumn(4).setPreferredWidth(120);
        tbFoto.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { lihatFoto(); }
            }
        });
        JScrollPane scrollFoto = new JScrollPane(tbFoto);
        tengah.add(scrollFoto, BorderLayout.CENTER);

        getContentPane().add(tengah, BorderLayout.CENTER);

        BtnUpload.setText("Upload Foto...");
        BtnUpload.addActionListener(e -> uploadFoto());
        BtnLihat.setText("Lihat");
        BtnLihat.addActionListener(e -> lihatFoto());
        BtnHapus.setText("Hapus");
        BtnHapus.addActionListener(e -> hapusFoto());
        BtnKeluar.setText("Keluar");
        BtnKeluar.addActionListener(e -> dispose());

        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 9));
        bawah.setBackground(Color.WHITE);
        bawah.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, garis));
        bawah.add(BtnUpload);
        bawah.add(BtnLihat);
        bawah.add(BtnHapus);
        bawah.add(BtnKeluar);
        getContentPane().add(bawah, BorderLayout.SOUTH);
    }

    public void isCek() {
    }

    /** Dipanggil dari menu klik-kanan pasien di DlgKamarInap. */
    public void setNoRm(String norawat) {
        TNoRw.setText(nvl(norawat));
        TNoRM.setText(""); TPasien.setText(""); TRuangan.setText("");
        if (norawat == null || norawat.trim().isEmpty()) {
            tabMode.setRowCount(0);
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select p.no_rkm_medis,p.nm_pasien,"
                + "ifnull(bangsal.nm_bangsal,'') as ruang,ifnull(kamar.kelas,'') as kelas "
                + "from reg_periksa rp inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis "
                + "left join kamar_inap on kamar_inap.no_rawat=rp.no_rawat "
                + "left join kamar on kamar.kd_kamar=kamar_inap.kd_kamar "
                + "left join bangsal on bangsal.kd_bangsal=kamar.kd_bangsal "
                + "where rp.no_rawat=? order by kamar_inap.tgl_masuk desc limit 1")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    String ruang = nvl(rs.getString("ruang"));
                    String kelas = nvl(rs.getString("kelas"));
                    TRuangan.setText(ruang + (kelas.isEmpty() ? "" : " / " + kelas));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif identitas dokumentasi foto : " + e);
        }
        muatData(norawat);
    }

    private void muatData(String norawat) {
        Vector<Vector<Object>> data = new Vector<>();
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select d.id,d.nama_file,d.keterangan,d.tgl_upload,d.photo,"
                + "ifnull(pg.nama,d.created_by) as oleh "
                + "from dokumentasi_foto_ranap d left join petugas pg on pg.nip=d.created_by "
                + "where d.no_rawat=? order by d.tgl_upload desc, d.id desc")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> baris = new Vector<>();
                    baris.add(rs.getLong("id"));
                    baris.add(buatThumbnail(rs.getBytes("photo"), 48));
                    baris.add(nvl(rs.getString("nama_file")));
                    baris.add(nvl(rs.getString("keterangan")));
                    baris.add(rs.getTimestamp("tgl_upload") == null ? "" : fmtTgl(rs.getTimestamp("tgl_upload")));
                    baris.add(nvl(rs.getString("oleh")));
                    data.add(baris);
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat dokumentasi foto : " + e);
        }
        tabMode.setDataVector(data, new Vector<>(java.util.Arrays.asList(
                "ID", "Foto", "Nama File", "Keterangan", "Tgl Upload", "Oleh")));
        tbFoto.getColumnModel().removeColumn(tbFoto.getColumnModel().getColumn(0));
        tbFoto.getColumnModel().getColumn(0).setPreferredWidth(70);
        tbFoto.getColumnModel().getColumn(1).setPreferredWidth(160);
        tbFoto.getColumnModel().getColumn(2).setPreferredWidth(320);
        tbFoto.getColumnModel().getColumn(3).setPreferredWidth(120);
        tbFoto.getColumnModel().getColumn(4).setPreferredWidth(120);
    }

    private void uploadFoto() {
        if (TNoRw.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        fc.setFileFilter(new FileNameExtensionFilter(
                "Gambar (jpg, jpeg, png, bmp, gif)", "jpg", "jpeg", "png", "bmp", "gif"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) { return; }
        File[] files = fc.getSelectedFiles();
        if (files == null || files.length == 0) { return; }
        int sukses = 0;
        for (File f : files) {
            String ket = JOptionPane.showInputDialog(this, "Keterangan untuk \"" + f.getName() + "\" :", "");
            if (ket == null) { continue; }
            try {
                byte[] data = Files.readAllBytes(f.toPath());
                data = kompresGambar(data);
                simpanFoto(f.getName(), ket, data);
                sukses++;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal upload \"" + f.getName() + "\".\n" + e.getMessage());
            }
        }
        if (sukses > 0) {
            muatData(TNoRw.getText());
            JOptionPane.showMessageDialog(this, sukses + " foto berhasil diupload.");
        }
    }

    private void simpanFoto(String namaFile, String keterangan, byte[] data) throws Exception {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into dokumentasi_foto_ranap (no_rawat,keterangan,nama_file,photo,tgl_upload,created_by) "
                + "values (?,?,?,?,now(),?)")) {
            ps.setString(1, TNoRw.getText());
            ps.setString(2, keterangan);
            ps.setString(3, namaFile);
            ps.setBytes(4, data);
            ps.setString(5, akses.getkode());
            ps.executeUpdate();
        }
    }

    private void lihatFoto() {
        int row = tbFoto.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih foto yang ingin dilihat.");
            return;
        }
        long id = (Long) tabMode.getValueAt(row, 0);
        byte[] data = null;
        try (PreparedStatement ps = koneksi.prepareStatement("select photo from dokumentasi_foto_ranap where id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { data = rs.getBytes("photo"); }
            }
        } catch (Exception e) {
            System.out.println("Notif lihat foto : " + e);
        }
        if (data == null) {
            JOptionPane.showMessageDialog(this, "Foto tidak ditemukan.");
            return;
        }
        ImageIcon icon = buatThumbnail(data, -1);
        if (icon == null) {
            JOptionPane.showMessageDialog(this, "Gagal membuka gambar.");
            return;
        }
        JDialog dlg = new JDialog(this, "Lihat Foto - " + tabMode.getValueAt(row, 2), true);
        JLabel label = new JLabel(icon);
        JScrollPane scroll = new JScrollPane(label);
        dlg.getContentPane().add(scroll);
        dlg.setSize(Math.min(icon.getIconWidth() + 40, 900), Math.min(icon.getIconHeight() + 60, 700));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void hapusFoto() {
        int row = tbFoto.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih foto yang ingin dihapus.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus foto \"" + tabMode.getValueAt(row, 2) + "\" ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        long id = (Long) tabMode.getValueAt(row, 0);
        try (PreparedStatement ps = koneksi.prepareStatement("delete from dokumentasi_foto_ranap where id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
            muatData(TNoRw.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus.\n" + e.getMessage());
        }
    }

    private static final int DIMENSI_MAKS = 1600;
    private static final int TARGET_BYTE = 900_000;

    /**
     * Kompres foto sebelum diupload -- server MySQL di sini max_allowed_packet
     * cuma 1 MB, jadi foto kamera HP (yg sering >1MB) selalu ditolak walau
     * "kecil" menurut mata. Diperkecil dimensi (maks 1600px sisi terpanjang)
     * + di-encode ulang jadi JPEG, kualitas diturunkan bertahap kalau masih
     * kebesaran. Kalau gagal didekode (format aneh), kirim apa adanya.
     */
    private static byte[] kompresGambar(byte[] asli) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(asli));
            if (img == null) { return asli; }
            int w = img.getWidth();
            int h = img.getHeight();
            if (Math.max(w, h) > DIMENSI_MAKS) {
                double skala = DIMENSI_MAKS / (double) Math.max(w, h);
                int wBaru = Math.max(1, (int) Math.round(w * skala));
                int hBaru = Math.max(1, (int) Math.round(h * skala));
                BufferedImage diperkecil = new BufferedImage(wBaru, hBaru, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = diperkecil.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(img, 0, 0, wBaru, hBaru, java.awt.Color.WHITE, null);
                g2.dispose();
                img = diperkecil;
            }
            float kualitas = 0.85f;
            byte[] hasil = encodeJpeg(img, kualitas);
            while (hasil.length > TARGET_BYTE && kualitas > 0.3f) {
                kualitas -= 0.15f;
                hasil = encodeJpeg(img, kualitas);
            }
            return hasil;
        } catch (Exception e) {
            return asli;
        }
    }

    private static byte[] encodeJpeg(BufferedImage img, float kualitas) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = it.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(kualitas);
        writer.setOutput(ImageIO.createImageOutputStream(baos));
        writer.write(null, new IIOImage(img, null, null), param);
        writer.dispose();
        return baos.toByteArray();
    }

    /** ukuran<0 = jangan discale (ukuran asli); ukuran>=0 = tinggi thumbnail dalam px. */
    private static ImageIcon buatThumbnail(byte[] data, int ukuran) {
        if (data == null || data.length == 0) { return null; }
        try {
            Image gambar = ImageIO.read(new ByteArrayInputStream(data));
            if (gambar == null) { return null; }
            if (ukuran < 0) { return new ImageIcon(gambar); }
            return new ImageIcon(gambar.getScaledInstance(-1, ukuran, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return null;
        }
    }

    private static String fmtTgl(java.sql.Timestamp t) {
        return new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date(t.getTime()));
    }

    private static widget.TextBox ro() {
        widget.TextBox t = new widget.TextBox();
        t.setEditable(false);
        return t;
    }

    private JPanel fieldRingkasan(String label, Component komponen) {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(0, 10, 0, 10));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Tahoma", Font.PLAIN, 10));
        l.setForeground(new Color(86, 101, 112));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        komponen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        komponen.setPreferredSize(new Dimension(150, 25));
        komponen.setBackground(new Color(248, 250, 251));
        p.add(l);
        p.add(Box.createVerticalStrut(3));
        p.add(komponen);
        return p;
    }

    private static String nvl(String v) {
        return v == null ? "" : v;
    }
}
