package rekammedis;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;
import javax.imageio.ImageIO;
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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Menggantikan submenu "RM Operasi" (yg sebelumnya bercabang ke ~11 dialog
 * form digital terpisah) jadi 1 halaman berisi blok-blok. SEBAGIAN BESAR blok
 * cuma upload FOTO dokumen fisik (form nya TETAP di kertas, sistem cuma
 * menyimpan hasil foto/scan-nya per kategori, bisa lebih dari 1 foto/blok);
 * SEBAGIAN lain (lihat {@link #INDEX_BLOK_FORM}) buka form digital
 * sungguhan (dikerjakan satu-satu sesuai referensi kertas asli yg dikirim
 * user). Blok terakhir "Laporan Operasi" BEDA lagi -- itu data digital yg
 * SUDAH ADA di Khanza (DlgLaporanOperasiPemeriksaan), jadi cuma
 * ditarik/ditampilkan, bukan upload/form baru.
 *
 * Upload foto masih LOKAL (JFileChooser + thumbnail in-memory), BELUM
 * disimpan ke database -- menunggu konfirmasi struktur data sebelum
 * dikerjakan penuh (beda track dgn form digital yg sudah simpan ke DB).
 */
public final class RMDokumentasiOperasi extends JDialog {

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private String norawat = "";
    private final JLabel lblPasien = new JLabel(" ");

    private static final String[] JUDUL_BLOK = {
        "Asesmen Awal Medis Bedah / Pra Bedah",
        "Informed Consent Tindakan Kedokteran (Persetujuan)",
        "Informed Consent Tindakan Anastesi - Sedasi",
        "Asesmen Pra-Sedasi - Pra-Anastesi",
        "Asesmen Pra Induksi",
        "Monitoring Intra Anastesi - Sedasi",
        "Monitoring Pasca Anastesi - Sedasi",
        "Askep Perioperatif",
        "Ceklis Keselamatan Pembedahan",
        "Formulir Penandaan Lokasi Operasi (Wanita)",
        "Laporan Operasi"
    };
    private static final int INDEX_LAPORAN_OPERASI = 10;
    /** Blok yg buka FORM DIGITAL (bukan upload foto) -- ditambah satu-satu begitu form-nya jadi. */
    private static final Set<Integer> INDEX_BLOK_FORM = new HashSet<>(Arrays.asList(0, 3, 4, 7, 8, 9));
    /** Tabel DB yg jadi acuan status "sudah diisi" per blok form -- blok 3 & 4 berbagi 1 tabel krn 1 lembar bolak-balik yg sama. */
    private static final Map<Integer, String> TABEL_STATUS_BLOK = new HashMap<>();
    static {
        TABEL_STATUS_BLOK.put(0, "asesmen_awal_medis_bedah");
        TABEL_STATUS_BLOK.put(3, "asesmen_pra_sedasi_anestesi");
        TABEL_STATUS_BLOK.put(4, "asesmen_pra_sedasi_anestesi");
        TABEL_STATUS_BLOK.put(7, "askep_perioperatif");
        TABEL_STATUS_BLOK.put(8, "ceklis_keselamatan_pembedahan");
        TABEL_STATUS_BLOK.put(9, "penandaan_lokasi_operasi");
    }
    private final Map<Integer, JLabel> lencanaStatusBlok = new HashMap<>();
    private final Map<Integer, JPanel> kartuStatusBlok = new HashMap<>();
    private final JLabel lblRingkasanStatus = new JLabel("0 dari 6 form digital sudah diisi");
    /** Blok upload foto (bukan form digital) -- fotonya disimpan ke tabel dokumentasi_blok_operasi. */
    private final Map<Integer, StripThumbnail> stripUploadBlok = new HashMap<>();
    private final Map<Integer, JLabel> captionUploadBlok = new HashMap<>();
    private final Map<Integer, JPanel> kartuUploadBlok = new HashMap<>();

    public RMDokumentasiOperasi(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Dokumentasi RM Operasi ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
        ensureTableUploadBlok();
        setSize(1080, 760);
        setMinimumSize(new Dimension(860, 600));
        setLocationRelativeTo(parent);
    }

    private void ensureTableUploadBlok() {
        Sequel.queryu2(
                "create table if not exists dokumentasi_blok_operasi ("
                + "id int not null auto_increment primary key,"
                + "no_rawat varchar(17) not null,"
                + "indeks_blok int not null,"
                + "nama_file varchar(255) null,"
                + "photo longblob null,"
                + "created_by varchar(50) null,"
                + "created_at datetime null,"
                + "key idx_no_rawat_blok (no_rawat, indeks_blok)"
                + ") ROW_FORMAT=DYNAMIC");
    }

    /** Dipanggil pemanggil (DlgKamarInap) SEBELUM/SESUDAH setVisible(true). */
    public void setNoRm(String norawatParam) {
        this.norawat = norawatParam == null ? "" : norawatParam;
        if (this.norawat.trim().isEmpty()) {
            lblPasien.setText(" ");
            perbaruiStatusSemuaBlok();
            muatSemuaFotoUpload();
            return;
        }
        String nama = Sequel.cariIsi(
                "select pasien.nm_pasien from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "where reg_periksa.no_rawat=?", this.norawat);
        lblPasien.setText(this.norawat + "  •  " + (nama == null ? "" : nama));
        perbaruiStatusSemuaBlok();
        muatSemuaFotoUpload();
    }

    /** Muat ulang thumbnail semua blok upload dari DB begitu pasien dipilih/ganti. */
    private void muatSemuaFotoUpload() {
        for (Map.Entry<Integer, StripThumbnail> e : stripUploadBlok.entrySet()) {
            e.getValue().muatDariDb();
            perbaruiTampilanUpload(e.getKey());
        }
    }

    /** Update caption + gaya kartu blok upload sesuai jumlah foto tersimpan saat ini. */
    private void perbaruiTampilanUpload(int indeksBlok) {
        StripThumbnail strip = stripUploadBlok.get(indeksBlok);
        JLabel caption = captionUploadBlok.get(indeksBlok);
        JPanel kartu = kartuUploadBlok.get(indeksBlok);
        if (strip == null || caption == null || kartu == null) {
            return;
        }
        int jml = strip.jumlahFoto();
        if (jml > 0) {
            caption.setText("✓  " + jml + " FOTO TERSIMPAN — klik untuk menambah");
            caption.setFont(new Font("Tahoma", Font.BOLD, 10));
            caption.setForeground(new Color(0, 108, 70));
            caption.setOpaque(true);
            caption.setBackground(new Color(214, 245, 229));
            caption.setBorder(new EmptyBorder(3, 6, 3, 6));
            kartu.setBackground(new Color(249, 255, 252));
            kartu.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 5, 1, 1, new Color(31, 157, 105)),
                    new EmptyBorder(9, 8, 9, 11)));
        } else {
            caption.setText("Belum ada foto -- klik area di atas untuk upload");
            caption.setFont(new Font("Tahoma", Font.ITALIC, 10));
            caption.setForeground(new Color(120, 133, 143));
            caption.setOpaque(false);
            caption.setBorder(null);
            kartu.setBackground(Color.WHITE);
            kartu.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(215, 224, 230)),
                    new EmptyBorder(10, 12, 10, 12)));
        }
    }

    /** Update lencana "Sudah Diisi"/"Belum Diisi" di tiap blok form begitu pasien dipilih/ganti. */
    private void perbaruiStatusSemuaBlok() {
        int jumlahLengkap = 0;
        for (Map.Entry<Integer, JLabel> e : lencanaStatusBlok.entrySet()) {
            boolean adaData = false;
            if (!norawat.trim().isEmpty()) {
                String tabel = TABEL_STATUS_BLOK.get(e.getKey());
                if (tabel != null) {
                    adaData = Sequel.cariInteger("select count(*) from " + tabel + " where no_rawat=?", norawat) > 0;
                }
            }
            JLabel lencana = e.getValue();
            JPanel kartu = kartuStatusBlok.get(e.getKey());
            if (adaData) {
                jumlahLengkap++;
                lencana.setText("✓  SUDAH DIISI");
                lencana.setForeground(new Color(0, 108, 70));
                lencana.setOpaque(true);
                lencana.setBackground(new Color(214, 245, 229));
                if (kartu != null) {
                    kartu.setBackground(new Color(249, 255, 252));
                    kartu.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(1, 5, 1, 1, new Color(31, 157, 105)),
                            new EmptyBorder(9, 8, 9, 11)));
                }
            } else {
                lencana.setText("○  BELUM DIISI");
                lencana.setForeground(new Color(166, 91, 0));
                lencana.setOpaque(true);
                lencana.setBackground(new Color(255, 239, 204));
                if (kartu != null) {
                    kartu.setBackground(new Color(255, 252, 246));
                    kartu.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(1, 5, 1, 1, new Color(239, 165, 42)),
                            new EmptyBorder(9, 8, 9, 11)));
                }
            }
        }
        int total = lencanaStatusBlok.size();
        lblRingkasanStatus.setText(jumlahLengkap + " dari " + total + " form digital sudah diisi");
        lblRingkasanStatus.setForeground(jumlahLengkap == total && total > 0
                ? new Color(0, 108, 70) : new Color(166, 91, 0));
    }

    private void initComponents() {
        final Color utama = new Color(0, 133, 143);
        final Color latar = new Color(246, 249, 251);
        final Color garis = new Color(215, 224, 230);
        final Color teks = new Color(32, 49, 66);

        getContentPane().setBackground(latar);
        getContentPane().setLayout(new BorderLayout());

        JPanel atas = new JPanel(new BorderLayout(12, 4));
        atas.setBackground(latar);
        atas.setBorder(new EmptyBorder(14, 18, 10, 18));
        JPanel blokJudul = new JPanel();
        blokJudul.setOpaque(false);
        blokJudul.setLayout(new BoxLayout(blokJudul, BoxLayout.Y_AXIS));
        JLabel judulUtama = new JLabel("Dokumentasi RM Operasi");
        judulUtama.setFont(new Font("Tahoma", Font.BOLD, 20));
        judulUtama.setForeground(teks);
        JLabel subjudul = new JLabel("Klik tiap blok untuk upload foto dokumen fisik (bisa lebih dari 1 foto per blok)");
        subjudul.setFont(new Font("Tahoma", Font.PLAIN, 12));
        subjudul.setForeground(new Color(92, 107, 119));
        blokJudul.add(judulUtama);
        blokJudul.add(Box.createVerticalStrut(3));
        blokJudul.add(subjudul);
        atas.add(blokJudul, BorderLayout.WEST);
        lblPasien.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblPasien.setForeground(teks);
        atas.add(lblPasien, BorderLayout.EAST);
        JPanel panelProgres = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panelProgres.setOpaque(false);
        JLabel ikonProgres = new JLabel("STATUS KELENGKAPAN");
        ikonProgres.setFont(new Font("Tahoma", Font.BOLD, 10));
        ikonProgres.setForeground(Color.WHITE);
        ikonProgres.setOpaque(true);
        ikonProgres.setBackground(utama);
        ikonProgres.setBorder(new EmptyBorder(4, 8, 4, 8));
        lblRingkasanStatus.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelProgres.add(ikonProgres);
        panelProgres.add(lblRingkasanStatus);
        atas.add(panelProgres, BorderLayout.SOUTH);
        getContentPane().add(atas, BorderLayout.NORTH);

        JPanel gridBlok = new JPanel(new GridLayout(0, 3, 14, 14));
        gridBlok.setBackground(latar);
        for (int i = 0; i < JUDUL_BLOK.length; i++) {
            int indeksBlok = i;
            if (i == INDEX_LAPORAN_OPERASI) {
                gridBlok.add(buatBlokLaporanOperasi(i + 1, JUDUL_BLOK[i], teks, garis));
            } else if (INDEX_BLOK_FORM.contains(i)) {
                gridBlok.add(buatBlokForm(i + 1, JUDUL_BLOK[i], indeksBlok, utama, teks, garis));
            } else {
                gridBlok.add(buatBlokUpload(i + 1, JUDUL_BLOK[i], indeksBlok, utama, teks, garis));
            }
        }
        JPanel pembungkusGrid = new JPanel(new BorderLayout());
        pembungkusGrid.setBackground(latar);
        pembungkusGrid.setBorder(new EmptyBorder(4, 18, 18, 18));
        pembungkusGrid.add(gridBlok, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(pembungkusGrid);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.getViewport().setBackground(latar);
        getContentPane().add(scroll, BorderLayout.CENTER);

        widget.Button btnKeluar = new widget.Button();
        btnKeluar.setText("Keluar");
        btnKeluar.addActionListener(e -> dispose());
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 9));
        bawah.setBackground(Color.WHITE);
        bawah.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, garis));
        bawah.add(btnKeluar);
        getContentPane().add(bawah, BorderLayout.SOUTH);
    }

    /** Kerangka kartu blok yg dipakai bersama (nomor+judul di header) -- isi tengahnya beda2 per jenis blok. */
    private JPanel kerangkaKartu(int nomor, String judul, Color aksen, Color teks, Color garis) {
        JPanel kartu = new JPanel(new BorderLayout(0, 8));
        kartu.setBackground(Color.WHITE);
        kartu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis),
                new EmptyBorder(10, 12, 10, 12)));
        kartu.setPreferredSize(new Dimension(260, 190));

        JPanel kepala = new JPanel(new BorderLayout(8, 0));
        kepala.setOpaque(false);
        JLabel lblNomor = new JLabel(String.valueOf(nomor));
        lblNomor.setOpaque(true);
        lblNomor.setBackground(aksen);
        lblNomor.setForeground(Color.WHITE);
        lblNomor.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblNomor.setHorizontalAlignment(SwingConstants.CENTER);
        lblNomor.setPreferredSize(new Dimension(22, 22));
        kepala.add(lblNomor, BorderLayout.WEST);
        JLabel lblJudul = new JLabel("<html>" + judul + "</html>");
        lblJudul.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblJudul.setForeground(teks);
        kepala.add(lblJudul, BorderLayout.CENTER);
        kartu.add(kepala, BorderLayout.NORTH);
        return kartu;
    }

    /** Blok upload foto biasa (bisa lebih dari 1 foto) -- tersimpan ke dokumentasi_blok_operasi. */
    private JPanel buatBlokUpload(int nomor, String judul, int indeksBlok, Color utama, Color teks, Color garis) {
        JPanel kartu = kerangkaKartu(nomor, judul, utama, teks, garis);

        StripThumbnail strip = new StripThumbnail(utama, indeksBlok);
        JScrollPane scrollStrip = new JScrollPane(strip,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollStrip.setBorder(BorderFactory.createLineBorder(new Color(228, 233, 237)));
        scrollStrip.setPreferredSize(new Dimension(230, 80));
        kartu.add(scrollStrip, BorderLayout.CENTER);

        JLabel caption = new JLabel("Belum ada foto -- klik area di atas untuk upload");
        caption.setFont(new Font("Tahoma", Font.ITALIC, 10));
        caption.setForeground(new Color(120, 133, 143));
        kartu.add(caption, BorderLayout.SOUTH);

        stripUploadBlok.put(indeksBlok, strip);
        captionUploadBlok.put(indeksBlok, caption);
        kartuUploadBlok.put(indeksBlok, kartu);

        strip.setOnUpload(() -> pilihFoto(strip, indeksBlok));
        strip.setOnLihat(id -> lihatFotoUpload(strip, indeksBlok, id));
        return kartu;
    }

    /** Blok yg buka FORM DIGITAL sungguhan (RMAsesmenAwalMedisBedah, dst -- lihat {@link #bukaForm}). */
    private JPanel buatBlokForm(int nomor, String judul, int indeksBlok, Color utama, Color teks, Color garis) {
        Color aksen = new Color(0, 105, 92);
        JPanel kartu = kerangkaKartu(nomor, judul, aksen, teks, garis);
        JPanel isi = new JPanel();
        isi.setOpaque(false);
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));
        isi.setBorder(new EmptyBorder(10, 0, 0, 0));
        JLabel info = new JLabel("<html>Form digital -- klik untuk mengisi/lihat data.</html>");
        info.setFont(new Font("Tahoma", Font.PLAIN, 11));
        info.setForeground(new Color(92, 107, 119));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        isi.add(info);
        isi.add(Box.createVerticalStrut(6));
        JLabel lencana = new JLabel("Belum Diisi");
        lencana.setFont(new Font("Tahoma", Font.BOLD, 11));
        lencana.setForeground(new Color(150, 96, 0));
        lencana.setBackground(new Color(255, 244, 224));
        lencana.setOpaque(true);
        lencana.setBorder(new EmptyBorder(5, 9, 5, 9));
        lencana.setAlignmentX(Component.LEFT_ALIGNMENT);
        lencanaStatusBlok.put(indeksBlok, lencana);
        kartuStatusBlok.put(indeksBlok, kartu);
        isi.add(lencana);
        isi.add(Box.createVerticalGlue());
        widget.Button btnBuka = new widget.Button();
        btnBuka.setText("Buka Form");
        btnBuka.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBuka.addActionListener(e -> bukaForm(indeksBlok));
        isi.add(btnBuka);
        kartu.add(isi, BorderLayout.CENTER);
        return kartu;
    }

    /** Blok khusus Laporan Operasi -- data digital yg SUDAH ADA di Khanza, cuma ditarik/ditampilkan. */
    private JPanel buatBlokLaporanOperasi(int nomor, String judul, Color teks, Color garis) {
        Color aksen = new Color(112, 61, 170);
        JPanel kartu = kerangkaKartu(nomor, judul, aksen, teks, garis);
        JPanel isi = new JPanel();
        isi.setOpaque(false);
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));
        isi.setBorder(new EmptyBorder(10, 0, 0, 0));
        JLabel info = new JLabel("<html>Data laporan operasi sudah ada di sistem.<br>Klik untuk langsung cetak preview.</html>");
        info.setFont(new Font("Tahoma", Font.PLAIN, 11));
        info.setForeground(new Color(92, 107, 119));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        isi.add(info);
        isi.add(Box.createVerticalGlue());
        widget.Button btnBuka = new widget.Button();
        btnBuka.setText("Cetak Laporan Operasi");
        btnBuka.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBuka.addActionListener(e -> cetakLaporanOperasi());
        isi.add(btnBuka);
        kartu.add(isi, BorderLayout.CENTER);
        return kartu;
    }

    /**
     * Preview Jasper Laporan Operasi (rptLaporanOperasi.jasper) langsung dari pasien yg lagi
     * dibuka di sini -- data laporan_operasi sendiri sudah diisi lewat jalur lama
     * (DlgLaporanOperasiPemeriksaan/DlgCariTagihanOperasi), blok ini cuma menarik & mencetak.
     * Query & param dibangun ULANG (bukan panggil method lama di DlgCariTagihanOperasi) krn
     * method lama itu baca dari baris JTable terpilih, bukan dari no_rawat langsung -- pola
     * SQL & isi param disalin persis dari MnLaporanOperasiActionPerformed di
     * DlgCariTagihanOperasi.java (verified match thd rptLaporanOperasi.jrxml).
     */
    public void cetakLaporanOperasi() {
        if (norawat.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pasien belum dipilih.");
            return;
        }
        String tglOperasi = "";
        String kodeOperator = "";
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select tgl_operasi, operator1 from operasi where no_rawat=? order by tgl_operasi desc limit 1")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tglOperasi = nvl(rs.getString("tgl_operasi"));
                    kodeOperator = nvl(rs.getString("operator1"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif ambil data operasi (cetak laporan operasi) : " + e);
        }
        if (tglOperasi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Data operasi untuk pasien ini belum ditemukan.");
            return;
        }
        if (Sequel.cariInteger("select count(*) from laporan_operasi where no_rawat=? and tanggal=?", norawat, tglOperasi) == 0) {
            JOptionPane.showMessageDialog(this, "Laporan Operasi tanggal " + tglOperasi + " belum diisi.");
            return;
        }
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            param.put("norawat", norawat);
            param.put("tanggaloperasi", tglOperasi);
            param.put("url_penggajian", "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/"
                    + koneksiDB.HYBRIDWEB() + "/penggajian/");

            String namaOperator = nvl(Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", kodeOperator));
            String finger = nvl(Sequel.cariIsi(
                    "select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",
                    kodeOperator));
            param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs()
                    + "\nDitandatangani secara elektronik oleh " + namaOperator
                    + "\nID " + (finger.isEmpty() ? kodeOperator : finger)
                    + "\n" + Valid.SetTgl3(tglOperasi));

            String statusLanjut = nvl(Sequel.cariIsi("select status_lanjut from reg_periksa where no_rawat=?", norawat));
            boolean ralan = "Ralan".equals(statusLanjut);
            String tabel = ralan ? "pemeriksaan_ralan" : "pemeriksaan_ranap";
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "select " + tabel + ".no_rawat," + tabel + ".tgl_perawatan," + tabel + ".jam_rawat,"
                    + tabel + ".suhu_tubuh," + tabel + ".tensi," + tabel + ".nadi," + tabel + ".respirasi,"
                    + tabel + ".tinggi," + tabel + ".berat," + tabel + ".gcs," + tabel + ".keluhan,"
                    + tabel + ".pemeriksaan," + tabel + ".alergi," + tabel + ".rtl," + tabel + ".penilaian "
                    + "from " + tabel + " where " + tabel + ".no_rawat=? "
                    + "and concat(" + tabel + ".tgl_perawatan,' '," + tabel + ".jam_rawat) <= ? "
                    + "order by " + tabel + ".tgl_perawatan desc," + tabel + ".jam_rawat desc limit 1")) {
                ps.setString(1, norawat);
                ps.setString(2, tglOperasi);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        param.put("tgl_perawatan", rs.getDate("tgl_perawatan"));
                        param.put("jam_rawat", rs.getString("jam_rawat"));
                        param.put("alergi", rs.getString("alergi"));
                        param.put("keluhan", rs.getString("keluhan"));
                        param.put("pemeriksaan", rs.getString("pemeriksaan"));
                        param.put("penilaian", rs.getString("penilaian"));
                        param.put("rtl", rs.getString("rtl"));
                        param.put("ruang", ralan
                                ? Sequel.cariIsi("select poliklinik.nm_poli from poliklinik inner join reg_periksa on reg_periksa.kd_poli=poliklinik.kd_poli where reg_periksa.no_rawat=?", rs.getString("no_rawat"))
                                : Sequel.cariIsi("select nm_bangsal from bangsal inner join kamar inner join kamar_inap on bangsal.kd_bangsal=kamar.kd_bangsal and kamar_inap.kd_kamar=kamar.kd_kamar where no_rawat=? order by tgl_masuk desc limit 1", rs.getString("no_rawat")));
                        param.put("suhu_tubuh", rs.getString("suhu_tubuh"));
                        param.put("tensi", rs.getString("tensi"));
                        param.put("tinggi", rs.getString("tinggi"));
                        param.put("berat", rs.getString("berat"));
                        param.put("nadi", rs.getString("nadi"));
                        param.put("respirasi", rs.getString("respirasi"));
                        param.put("gcs", rs.getString("gcs"));
                    }
                }
            }
            Valid.MyReport("rptLaporanOperasi.jasper", "report", "::[ Laporan Operasi ]::", param);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak laporan operasi.\n" + e.getMessage());
        }
    }

    private static String nvl(String v) {
        return v == null ? "" : v;
    }

    /** Dispatch klik blok "form digital" ke dialog form yg sesuai. Tambah case baru begitu form lain jadi. */
    private void bukaForm(int indeksBlok) {
        if (norawat.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pasien belum dipilih.");
            return;
        }
        switch (indeksBlok) {
            case 0:
                RMAsesmenAwalMedisBedah formBedah = new RMAsesmenAwalMedisBedah(null, true);
                formBedah.setSize(getWidth() - 40, getHeight() - 40);
                formBedah.setLocationRelativeTo(this);
                formBedah.isCek();
                formBedah.setNoRm(norawat);
                formBedah.setVisible(true);
                break;
            case 3:
            case 4:
                RMAsesmenPraSedasiAnestesi formSedasi = new RMAsesmenPraSedasiAnestesi(null, true);
                formSedasi.setSize(getWidth() - 40, getHeight() - 40);
                formSedasi.setLocationRelativeTo(this);
                formSedasi.isCek();
                formSedasi.setNoRm(norawat);
                formSedasi.pilihTab(indeksBlok == 4 ? 1 : 0);
                formSedasi.setVisible(true);
                break;
            case 7:
                RMAskepPerioperatif formAskep = new RMAskepPerioperatif(null, true);
                formAskep.setSize(getWidth() - 40, getHeight() - 40);
                formAskep.setLocationRelativeTo(this);
                formAskep.isCek();
                formAskep.setNoRm(norawat);
                formAskep.setVisible(true);
                break;
            case 8:
                RMCeklisKeselamatanPembedahan formCeklis = new RMCeklisKeselamatanPembedahan(null, true);
                formCeklis.setSize(getWidth() - 40, getHeight() - 40);
                formCeklis.setLocationRelativeTo(this);
                formCeklis.isCek();
                formCeklis.setNoRm(norawat);
                formCeklis.setVisible(true);
                break;
            case 9:
                RMPenandaanLokasiOperasi formLokasi = new RMPenandaanLokasiOperasi(null, true);
                formLokasi.setSize(getWidth() - 40, getHeight() - 40);
                formLokasi.setLocationRelativeTo(this);
                formLokasi.isCek();
                formLokasi.setNoRm(norawat);
                formLokasi.setVisible(true);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Form untuk blok ini belum dibuat.");
        }
        perbaruiStatusSemuaBlok();
    }

    private void pilihFoto(StripThumbnail strip, int indeksBlok) {
        if (norawat.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pasien belum dipilih.");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Upload Foto -- " + JUDUL_BLOK[indeksBlok]);
        fc.setMultiSelectionEnabled(true);
        fc.setFileFilter(new FileNameExtensionFilter("Gambar (JPG/PNG)", "jpg", "jpeg", "png"));
        int hasil = fc.showOpenDialog(this);
        if (hasil == JFileChooser.APPROVE_OPTION) {
            for (File f : fc.getSelectedFiles()) {
                strip.tambahFotoBaru(f);
            }
            perbaruiTampilanUpload(indeksBlok);
        }
    }

    /** Lihat 1 foto upload ukuran penuh, dgn tombol Hapus (dipanggil dari klik thumbnail). */
    private void lihatFotoUpload(StripThumbnail strip, int indeksBlok, int id) {
        byte[] foto = strip.ambilFotoAsli(id);
        if (foto == null) {
            return;
        }
        try {
            Image img = ImageIO.read(new ByteArrayInputStream(foto));
            if (img == null) {
                return;
            }
            int w = img.getWidth(null);
            int h = img.getHeight(null);
            double skala = Math.min(1.0, Math.min(700.0 / w, 600.0 / h));
            JDialog dlg = new JDialog(this, JUDUL_BLOK[indeksBlok], true);
            JLabel lbl = new JLabel(new ImageIcon(
                    img.getScaledInstance((int) (w * skala), (int) (h * skala), Image.SCALE_SMOOTH)));
            widget.Button btnHapus = new widget.Button();
            btnHapus.setText("Hapus Foto Ini");
            btnHapus.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(dlg, "Hapus foto ini?", "Konfirmasi",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    strip.hapusFoto(id);
                    perbaruiTampilanUpload(indeksBlok);
                    dlg.dispose();
                }
            });
            JPanel panelBawah = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            panelBawah.add(btnHapus);
            dlg.getContentPane().setLayout(new BorderLayout());
            dlg.getContentPane().add(new JScrollPane(lbl), BorderLayout.CENTER);
            dlg.getContentPane().add(panelBawah, BorderLayout.SOUTH);
            dlg.pack();
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
        } catch (Exception e) {
            System.out.println("Notif lihatFotoUpload : " + e);
        }
    }

    /**
     * Strip horizontal thumbnail di dalam 1 blok upload, plus tombol "+" di
     * ujung. Foto SELALU disimpan ke tabel dokumentasi_blok_operasi begitu
     * dipilih (bukan cuma in-memory) supaya bisa ditarik ulang di halaman
     * Riwayat Perawatan. Klik thumbnail = lihat ukuran penuh (+ hapus), klik
     * "+" = upload.
     */
    private final class StripThumbnail extends JPanel {
        private final List<Integer> idFoto = new ArrayList<>();
        private final List<byte[]> dataAsli = new ArrayList<>();
        private final List<ImageIcon> thumbAsli = new ArrayList<>();
        private final int indeksBlok;
        private final Color aksen;
        private Runnable onUpload;
        private IntConsumer onLihat;

        StripThumbnail(Color aksen, int indeksBlok) {
            this.aksen = aksen;
            this.indeksBlok = indeksBlok;
            setLayout(new FlowLayout(FlowLayout.LEFT, 6, 6));
            setBackground(new Color(250, 251, 252));
            tampilkanPlaceholder();
        }

        void setOnUpload(Runnable r) { this.onUpload = r; }

        void setOnLihat(IntConsumer c) { this.onLihat = c; }

        private void tampilkanPlaceholder() {
            removeAll();
            JLabel plus = new JLabel("+  Upload Foto");
            plus.setFont(new Font("Tahoma", Font.BOLD, 11));
            plus.setForeground(aksen);
            plus.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            plus.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (onUpload != null) { onUpload.run(); }
                }
            });
            add(plus);
            revalidate();
            repaint();
        }

        void muatDariDb() {
            idFoto.clear();
            dataAsli.clear();
            thumbAsli.clear();
            if (!norawat.trim().isEmpty()) {
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "select id, photo from dokumentasi_blok_operasi where no_rawat=? and indeks_blok=? order by id")) {
                    ps.setString(1, norawat);
                    ps.setInt(2, indeksBlok);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            byte[] data = rs.getBytes("photo");
                            if (data != null) {
                                idFoto.add(rs.getInt("id"));
                                dataAsli.add(data);
                                thumbAsli.add(new ImageIcon(data));
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notif muat foto upload blok " + indeksBlok + " : " + e);
                }
            }
            muatUlangTampilan();
        }

        void tambahFotoBaru(File f) {
            try {
                byte[] data = Files.readAllBytes(f.toPath());
                int id = -1;
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "insert into dokumentasi_blok_operasi (no_rawat,indeks_blok,nama_file,photo,created_by,created_at) "
                        + "values (?,?,?,?,?,now())", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, norawat);
                    ps.setInt(2, indeksBlok);
                    ps.setString(3, f.getName());
                    ps.setBytes(4, data);
                    ps.setString(5, akses.getkode());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) { id = keys.getInt(1); }
                    }
                }
                idFoto.add(id);
                dataAsli.add(data);
                thumbAsli.add(new ImageIcon(data));
                muatUlangTampilan();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(RMDokumentasiOperasi.this, "Gagal menyimpan foto.\n" + e.getMessage());
            }
        }

        byte[] ambilFotoAsli(int id) {
            int idx = idFoto.indexOf(id);
            return idx < 0 ? null : dataAsli.get(idx);
        }

        void hapusFoto(int id) {
            try (PreparedStatement ps = koneksi.prepareStatement("delete from dokumentasi_blok_operasi where id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            } catch (Exception e) {
                System.out.println("Notif hapus foto upload blok : " + e);
            }
            int idx = idFoto.indexOf(id);
            if (idx >= 0) {
                idFoto.remove(idx);
                dataAsli.remove(idx);
                thumbAsli.remove(idx);
            }
            muatUlangTampilan();
        }

        int jumlahFoto() { return idFoto.size(); }

        private void muatUlangTampilan() {
            removeAll();
            if (thumbAsli.isEmpty()) {
                tampilkanPlaceholder();
                return;
            }
            for (int i = 0; i < thumbAsli.size(); i++) {
                final int id = idFoto.get(i);
                Image scaled = thumbAsli.get(i).getImage().getScaledInstance(-1, 60, Image.SCALE_SMOOTH);
                JLabel thumb = new JLabel(new ImageIcon(scaled));
                thumb.setBorder(BorderFactory.createLineBorder(new Color(210, 218, 224)));
                thumb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                thumb.addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        if (onLihat != null) { onLihat.accept(id); }
                    }
                });
                add(thumb);
            }
            JLabel plus = new JLabel("+");
            plus.setFont(new Font("Tahoma", Font.BOLD, 16));
            plus.setForeground(new Color(120, 133, 143));
            plus.setPreferredSize(new Dimension(30, 60));
            plus.setHorizontalAlignment(SwingConstants.CENTER);
            plus.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            plus.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (onUpload != null) { onUpload.run(); }
                }
            });
            add(plus);
            revalidate();
            repaint();
        }
    }

    /** @param args */
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            RMDokumentasiOperasi dialog = new RMDokumentasiOperasi(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) { System.exit(0); }
            });
            dialog.setVisible(true);
        });
    }
}
