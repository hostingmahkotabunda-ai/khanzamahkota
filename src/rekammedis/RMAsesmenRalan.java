package rekammedis;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/**
 * Panel "Penilaian Awal Rawat Jalan" (Asesmen Rawat Jalan) yang di-embed sebagai
 * tab di DlgRawatJalan. Mengacu tata letak PDF Asesmen Rawat Jalan TrustMedis:
 * identitas + riwayat (auto tarik) + asesmen (input) + TTD pelaksana.
 *
 * Data disimpan REPLACE INTO tabel asesmen_ralan (1 baris per no_rawat, semua
 * kolom TEXT untuk menghindari #1118 Row size too large). Cetak digabung dengan
 * ringkasan riwayat kunjungan lewat CetakAsesmen (kop RS + QR TTD).
 *
 * Riwayat asesmen: dropdown berisi asesmen kunjungan SEBELUMNYA milik pasien yang sama
 * (no_rkm_medis sama, no_rawat lain). Memilih satu item menyalin isinya ke form; Simpan
 * menyimpannya sebagai data kunjungan yang sedang dibuka (no_rawat sekarang). Data
 * kunjungan lama tidak pernah diubah.
 *
 * Tampilan mengikuti standar modern proyek (Catatan ADIME Gizi / Instruksi Diet
 * NutritionOrder): kartu Surface rounded, palet BACK/INK/TEAL/MUTED, font Segoe UI.
 */
public final class RMAsesmenRalan extends JPanel {

    // ---- palet & font (sama dengan AdimeGiziView / SatuSehatAturNutritionOrder) ----
    private static final Color BACK = new Color(243, 247, 250);
    private static final Color INK = new Color(24, 44, 62);
    private static final Color TEAL = new Color(12, 132, 124);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color GARIS = new Color(215, 226, 233);
    private static final Color AMBER = new Color(180, 105, 0);
    private static final String FONT = "Segoe UI";

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();

    private String noRawat = "";
    private boolean adaTersimpan = false;
    private String waktuTersimpan = "";

    // Identitas (read-only, auto tarik)
    private final JTextField TNoRw = ro();
    private final JTextField TNoRM = ro();
    private final JTextField TPasien = ro();
    private final JTextField TNIK = ro();
    private final JTextField TJK = ro();
    private final JTextField TTglLahir = ro();
    private final JTextField TAlamat = ro();
    private final JTextField TUnit = ro();
    private final JTextField TKelas = ro();
    private final JTextField TCaraBayar = ro();

    // Riwayat (auto tarik, boleh diubah manual)
    private final JTextArea taRiwayatAlergi = ta();
    private final JTextArea taRiwayatBedah = ta();

    // Asesmen (input)
    private final widget.Tanggal dtpTanggal = dt();
    private final Grup grpRujukan = new Grup("Puskesmas", "RS", "Dokter");
    private final Grup grpDatang = new Grup("Datang sendiri", "Diantar", "Lainnya");
    private final Grup grpInformasi = new Grup("Pasien", "Keluarga", "Teman", "Lainnya");
    private final JTextArea taPenyakitSekarang = ta();
    private final JTextArea taPenyakitDahulu = ta();

    private final JTextField tTD = tf();
    private final JTextField tSuhu = tf();
    private final JTextField tNadi = tf();
    private final JTextField tRR = tf();
    private final JComboBox<String> cmbNyeri = cmb("Tidak nyeri", "Nyeri");
    private final JComboBox<String> cmbSkalaNyeri = cmb("0 - Tidak nyeri", "1-3 Nyeri ringan",
            "4-6 Nyeri sedang", "7-10 Nyeri berat");

    private final JTextField tBB = tf();
    private final JTextField tTB = tf();
    private final JTextField tIMT = tf();
    private final JTextField tLingkarKepala = tf();

    private final JTextField tAlatBantu = tf();
    private final JTextField tProthesa = tf();
    private final JTextField tCacatTubuh = tf();
    private final JTextField tAdl = tf();
    private final JComboBox<String> cmbMandiri = cmb("Mandiri", "Dibantu");

    private final JComboBox<String> cmbNikah = cmb("Belum menikah", "Menikah",
            "Duda/Janda (meninggal)", "Duda/Janda (bercerai)");
    private final JComboBox<String> cmbSaudara = cmb("Tidak ada", "Ada");
    private final JTextField tJumlahSaudara = tf();
    private final JComboBox<String> cmbNegara = cmb("WNI", "WNA");
    private final JTextField tWnaAsal = tf();
    private final JComboBox<String> cmbPekerjaan = cmb("PNS", "Swasta", "TNI/POLRI", "Tidak bekerja", "Lainnya");
    private final JComboBox<String> cmbTinggal = cmb("Suami", "Anak", "Orang tua", "Sendiri", "Lainnya");
    private final JTextField tNamaKeluarga = tf();
    private final JTextField tTelepon = tf();
    private final JComboBox<String> cmbAgama = cmb("Islam", "Kristen", "Katolik", "Hindu", "Budha", "Konghucu");

    // TTD
    private final widget.Tanggal dtpTtd = dt();
    private final JTextField KdPetugas = ro();
    private final JTextField NmPetugas = ro();

    // Riwayat asesmen sebelumnya
    private final JComboBox<Object> cmbHistori = new JComboBox<>();
    private final JLabel lblHistoriInfo = new JLabel();
    private boolean sedangIsiHistori = false;

    // Status & tombol
    private final Pill lblStatus = new Pill();
    private final JLabel lblBantuan = new JLabel();
    private final JButton BtnBaru = tombol("Baru", new Color(37, 99, 205));
    private final JButton BtnSimpan = tombol("Simpan", new Color(8, 121, 102));
    private final JButton BtnHapus = tombol("Hapus", new Color(187, 43, 65));
    private final JButton BtnCetak = tombol("Cetak", new Color(71, 85, 105));

    public RMAsesmenRalan() {
        pastikanTabel();
        initComponents();
        isCek();
        perbaruiStatus();
    }

    // =====================================================================
    //  Tata letak
    // =====================================================================

    private void initComponents() {
        setLayout(new BorderLayout(0, 10));
        setBackground(BACK);
        setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        // ---- header + riwayat asesmen ----
        JPanel atas = new JPanel();
        atas.setOpaque(false);
        atas.setLayout(new BoxLayout(atas, BoxLayout.Y_AXIS));

        Surface hero = new Surface(new BorderLayout(16, 0), INK);
        hero.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        JPanel judul = new JPanel(new GridLayout(2, 1, 0, 2));
        judul.setOpaque(false);
        JLabel title = new JLabel("Asesmen Rawat Jalan");
        title.setFont(new Font(FONT, Font.BOLD, 21));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Penilaian awal pasien rawat jalan  ·  tersimpan per kunjungan");
        subtitle.setFont(new Font(FONT, Font.PLAIN, 13));
        subtitle.setForeground(new Color(194, 218, 228));
        judul.add(title);
        judul.add(subtitle);
        hero.add(judul, BorderLayout.CENTER);
        JPanel kanan = new JPanel(new GridBagLayout());
        kanan.setOpaque(false);
        kanan.add(lblStatus);
        hero.add(kanan, BorderLayout.EAST);
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);
        atas.add(hero);
        atas.add(Box.createVerticalStrut(10));

        Surface histori = new Surface(new BorderLayout(16, 6), Color.WHITE);
        histori.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        JPanel histJudul = new JPanel(new GridLayout(2, 1, 0, 2));
        histJudul.setOpaque(false);
        JLabel hj = new JLabel("Riwayat asesmen");
        hj.setFont(new Font(FONT, Font.BOLD, 16));
        hj.setForeground(INK);
        JLabel hs = new JLabel("Salin isi asesmen kunjungan sebelumnya ke form ini");
        hs.setFont(new Font(FONT, Font.PLAIN, 12));
        hs.setForeground(MUTED);
        histJudul.add(hj);
        histJudul.add(hs);
        histJudul.setPreferredSize(new Dimension(330, 44));
        histori.add(histJudul, BorderLayout.WEST);
        gayaCombo(cmbHistori);
        cmbHistori.setPreferredSize(new Dimension(520, 36));
        cmbHistori.setMaximumRowCount(12);
        cmbHistori.setRenderer(new RendererHistori());
        cmbHistori.addActionListener(e -> {
            if (!sedangIsiHistori) {
                pilihHistori();
            }
        });
        histori.add(cmbHistori, BorderLayout.CENTER);
        lblHistoriInfo.setFont(new Font(FONT, Font.PLAIN, 12));
        lblHistoriInfo.setForeground(MUTED);
        histori.add(lblHistoriInfo, BorderLayout.SOUTH);
        histori.setAlignmentX(Component.LEFT_ALIGNMENT);
        atas.add(histori);
        add(atas, BorderLayout.NORTH);

        // ---- isi form: kartu-kartu ----
        Konten isi = new Konten(new GridBagLayout());
        isi.setBackground(BACK);
        int y = 0;
        y = baris(isi, y, kartuIdentitas(), null);
        y = baris(isi, y, kartuKedatangan(), kartuRiwayat());
        y = baris(isi, y, kartuTandaVital(), kartuAntropometri());
        y = baris(isi, y, kartuFungsional(), kartuSosial());
        y = baris(isi, y, kartuPelaksana(), null);
        GridBagConstraints sisa = new GridBagConstraints();
        sisa.gridy = y;
        sisa.weighty = 1;
        isi.add(Box.createGlue(), sisa);

        JScrollPane sc = new JScrollPane(isi);
        sc.setBorder(BorderFactory.createEmptyBorder());
        sc.getViewport().setBackground(BACK);
        sc.getVerticalScrollBar().setUnitIncrement(18);
        sc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(sc, BorderLayout.CENTER);

        // ---- footer tombol (selalu terlihat) ----
        Surface footer = new Surface(new BorderLayout(14, 0), Color.WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 14));
        lblBantuan.setFont(new Font(FONT, Font.PLAIN, 12));
        lblBantuan.setForeground(MUTED);
        footer.add(lblBantuan, BorderLayout.CENTER);
        JPanel tombol = new JPanel(new GridLayout(1, 4, 10, 0));
        tombol.setOpaque(false);
        tombol.add(BtnBaru);
        tombol.add(BtnCetak);
        tombol.add(BtnHapus);
        tombol.add(BtnSimpan);
        footer.add(tombol, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        BtnBaru.addActionListener(e -> kosongkanForm());
        BtnSimpan.addActionListener(e -> simpan());
        BtnHapus.addActionListener(e -> hapus());
        BtnCetak.addActionListener(e -> cetak());

        java.awt.event.FocusAdapter imt = new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                hitungIMT();
            }
        };
        tBB.addFocusListener(imt);
        tTB.addFocusListener(imt);
        tIMT.setToolTipText("Dihitung otomatis dari BB dan TB; boleh diubah manual.");
        taRiwayatAlergi.setToolTipText("Terisi otomatis dari riwayat pasien; boleh diketik/diubah manual.");
        taRiwayatBedah.setToolTipText("Terisi otomatis dari riwayat rawat inap; boleh diketik/diubah manual.");
    }

    private JPanel kartuIdentitas() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        TPasien.setFont(new Font(FONT, Font.BOLD, 17));
        GridBagConstraints c = gc(0, 0);
        c.gridwidth = 8;
        c.insets = new Insets(0, 0, 8, 0);
        body.add(TPasien, c);
        String[] cap = {"No. Rawat", "No. RM", "NIK", "Jenis Kelamin", "Tanggal Lahir", "Poli / Unit", "Kelas", "Cara Bayar"};
        JTextField[] val = {TNoRw, TNoRM, TNIK, TJK, TTglLahir, TUnit, TKelas, TCaraBayar};
        for (int i = 0; i < cap.length; i++) {
            JPanel sel = new JPanel(new GridLayout(2, 1, 0, 0));
            sel.setOpaque(false);
            sel.add(caption(cap[i]));
            sel.add(val[i]);
            GridBagConstraints g = gc(i % 4, 1 + i / 4);
            g.weightx = 1;
            g.insets = new Insets(2, 0, 6, 16);
            body.add(sel, g);
        }
        JPanel alamat = new JPanel(new GridLayout(2, 1, 0, 0));
        alamat.setOpaque(false);
        alamat.add(caption("Alamat"));
        alamat.add(TAlamat);
        GridBagConstraints g = gc(0, 3);
        g.gridwidth = 4;
        g.weightx = 1;
        g.insets = new Insets(2, 0, 0, 16);
        body.add(alamat, g);
        return kartu("Identitas pasien", "Terisi otomatis dari data registrasi kunjungan.", body);
    }

    private JPanel kartuKedatangan() {
        Form f = new Form();
        f.baris("Tanggal / Pukul", dtpTanggal);
        f.baris("Rujukan", grpRujukan.panel);
        f.baris("Cara datang", grpDatang.panel);
        f.baris("Informasi dari", grpInformasi.panel);
        f.baris("Penyakit sekarang", area(taPenyakitSekarang, 3));
        f.baris("Penyakit dahulu", area(taPenyakitDahulu, 3));
        return kartu("Kedatangan & anamnesis", "Rujukan, cara datang, sumber informasi, dan keluhan pasien.", f.panel);
    }

    private JPanel kartuRiwayat() {
        Form f = new Form();
        f.baris("Riwayat alergi", area(taRiwayatAlergi, 4));
        f.baris("Pembedahan / rawat inap", area(taRiwayatBedah, 6));
        return kartu("Riwayat pasien", "Terisi otomatis dari riwayat; bisa diketik manual bila kosong.", f.panel);
    }

    private JPanel kartuTandaVital() {
        Form f = new Form();
        f.baris2("Tekanan darah", satuan(tTD, "mmHg"), "Suhu", satuan(tSuhu, "°C"));
        f.baris2("Nadi", satuan(tNadi, "x/mnt"), "Pernapasan", satuan(tRR, "x/mnt"));
        f.baris2("Nyeri", cmbNyeri, "Skala nyeri", cmbSkalaNyeri);
        return kartu("Tanda vital", "Hasil pemeriksaan saat kunjungan ini.", f.panel);
    }

    private JPanel kartuAntropometri() {
        Form f = new Form();
        f.baris2("Berat badan", satuan(tBB, "kg"), "Tinggi badan", satuan(tTB, "cm"));
        f.baris2("IMT", satuan(tIMT, "kg/m²"), "Lingkar kepala", satuan(tLingkarKepala, "cm"));
        return kartu("Antropometri", "IMT dihitung otomatis dari berat dan tinggi badan.", f.panel);
    }

    private JPanel kartuFungsional() {
        Form f = new Form();
        f.baris2("Alat bantu", tAlatBantu, "Prothesa", tProthesa);
        f.baris2("Cacat tubuh", tCacatTubuh, "ADL", tAdl);
        f.baris("Kemandirian", cmbMandiri);
        return kartu("Status fungsional", "Alat bantu, prothesa, kecacatan, dan aktivitas harian.", f.panel);
    }

    private JPanel kartuSosial() {
        Form f = new Form();
        f.baris2("Pernikahan", cmbNikah, "Agama", cmbAgama);
        f.baris2("Saudara", cmbSaudara, "Jumlah saudara", satuan(tJumlahSaudara, "orang"));
        f.baris2("Kewarganegaraan", cmbNegara, "Asal (WNA)", tWnaAsal);
        f.baris2("Pekerjaan", cmbPekerjaan, "Tinggal bersama", cmbTinggal);
        f.baris2("Nama keluarga", tNamaKeluarga, "No. telepon", tTelepon);
        return kartu("Sosial, ekonomi & spiritual", "Sebagian terisi otomatis dari data pasien.", f.panel);
    }

    private JPanel kartuPelaksana() {
        Form f = new Form();
        f.baris2("Tanggal TTD", dtpTtd, "NIP", KdPetugas);
        f.baris("Nama pelaksana", NmPetugas);
        KdPetugas.setFont(new Font(FONT, Font.BOLD, 14));
        NmPetugas.setFont(new Font(FONT, Font.BOLD, 14));
        return kartu("Pelaksana asesmen", "Petugas yang sedang login; tercetak sebagai tanda tangan.", f.panel);
    }

    // =====================================================================
    //  Entry point
    // =====================================================================

    public void isCek() {
        BtnSimpan.setEnabled(true);
        BtnHapus.setEnabled(true);
        KdPetugas.setText(akses.getkode());
        NmPetugas.setText(Sequel.cariIsi("select nama from petugas where nip=?", akses.getkode()));
    }

    public void setKonteks(String norwt) {
        this.noRawat = norwt == null ? "" : norwt.trim();
        baru();
        TNoRw.setText(noRawat);
        if (!noRawat.equals("")) {
            tarikIdentitas();
            muat();                    // ambil nilai tersimpan (termasuk riwayat freetext bila ada)
            lengkapiRiwayatOtomatis(); // isi otomatis hanya bila area riwayat masih kosong
        }
        isiDaftarHistori();
        perbaruiStatus();
    }

    /** Tombol Baru: kosongkan isian, tapi identitas & riwayat otomatis pasien tetap ditarik ulang. */
    private void kosongkanForm() {
        baru();
        TNoRw.setText(noRawat);
        if (!noRawat.isEmpty()) {
            tarikIdentitas();
            lengkapiRiwayatOtomatis();
            waktuTersimpan = Sequel.cariIsi("select concat(date_format(tanggal,'%d-%m-%Y'),' ',left(ifnull(jam,''),5)) "
                    + "from asesmen_ralan where no_rawat=?", noRawat);
            adaTersimpan = !waktuTersimpan.trim().isEmpty();
        }
        perbaruiStatus();
    }

    private void baru() {
        for (JTextField t : new JTextField[]{tTD, tSuhu, tNadi, tRR, tBB, tTB, tIMT, tLingkarKepala,
            tAlatBantu, tProthesa, tCacatTubuh, tAdl, tJumlahSaudara, tWnaAsal, tNamaKeluarga, tTelepon}) {
            t.setText("");
        }
        for (JTextArea a : new JTextArea[]{taPenyakitSekarang, taPenyakitDahulu, taRiwayatAlergi, taRiwayatBedah}) {
            a.setText("");
        }
        for (Grup g : new Grup[]{grpRujukan, grpDatang, grpInformasi}) {
            g.clear();
        }
        for (JComboBox<String> c : semuaCombo()) {
            c.setSelectedIndex(0);
        }
        dtpTanggal.setDate(new Date());
        dtpTtd.setDate(new Date());
        KdPetugas.setText(akses.getkode());
        NmPetugas.setText(Sequel.cariIsi("select nama from petugas where nip=?", akses.getkode()));
        adaTersimpan = false;
        waktuTersimpan = "";
        lblHistoriInfo.setText(" ");
        lblHistoriInfo.setForeground(MUTED);
        resetPilihanHistori();
    }

    @SuppressWarnings("unchecked")
    private JComboBox<String>[] semuaCombo() {
        return new JComboBox[]{cmbNyeri, cmbSkalaNyeri, cmbMandiri, cmbNikah,
            cmbSaudara, cmbNegara, cmbPekerjaan, cmbTinggal, cmbAgama};
    }

    private void tarikIdentitas() {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select p.nm_pasien,p.no_rkm_medis,p.no_ktp,p.jk,p.tgl_lahir,p.alamat,p.no_tlp,"
                + "ifnull(p.agama,'') as agama,ifnull(p.stts_nikah,'') as stts_nikah,ifnull(p.pekerjaan,'') as pekerjaan,"
                + "ifnull(p.namakeluarga,'') as namakeluarga,ifnull(poliklinik.nm_poli,'') as unit,"
                + "ifnull(pj.png_jawab,'') as carabayar "
                + "from reg_periksa rp inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis "
                + "left join poliklinik on rp.kd_poli=poliklinik.kd_poli "
                + "left join penjab pj on rp.kd_pj=pj.kd_pj where rp.no_rawat=?")) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    TNIK.setText(rs.getString("no_ktp"));
                    TJK.setText("L".equalsIgnoreCase(rs.getString("jk")) ? "Laki-Laki" : "Perempuan");
                    TTglLahir.setText(rs.getString("tgl_lahir"));
                    TAlamat.setText(rs.getString("alamat"));
                    TUnit.setText(rs.getString("unit"));
                    TCaraBayar.setText(rs.getString("carabayar"));
                    tTelepon.setText(rs.getString("no_tlp"));
                    tNamaKeluarga.setText(rs.getString("namakeluarga"));
                    setCombo(cmbAgama, rs.getString("agama"));
                    setNikahFromMaster(rs.getString("stts_nikah"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif identitas asesmen ralan : " + e);
        }
        TKelas.setText(Sequel.cariIsi("select ifnull(kamar.kelas,'') from kamar_inap "
                + "inner join kamar on kamar.kd_kamar=kamar_inap.kd_kamar where kamar_inap.no_rawat=? "
                + "order by kamar_inap.tgl_masuk desc limit 1", noRawat));
        for (JTextField t : new JTextField[]{TPasien, TNoRw, TNoRM, TNIK, TJK, TTglLahir, TUnit, TKelas, TCaraBayar, TAlamat}) {
            t.setCaretPosition(0);
            t.setToolTipText(t.getText());
        }
    }

    /**
     * Isi area riwayat dari data pasien HANYA bila area masih kosong (mis. belum
     * ada nilai tersimpan). Bila data historis juga tidak ada, area dibiarkan
     * kosong agar petugas bisa mengetik manual (freetext).
     */
    private void lengkapiRiwayatOtomatis() {
        if (taRiwayatAlergi.getText().trim().isEmpty()) {
            StringBuilder alergi = new StringBuilder();
            tambahRiwayat(alergi, "select group_concat(distinct nullif(trim(pr.alergi),'') separator ', ') "
                    + "from pemeriksaan_ralan pr inner join reg_periksa rp on pr.no_rawat=rp.no_rawat "
                    + "where rp.no_rkm_medis=? and trim(ifnull(pr.alergi,'')) not in ('','-','--','---')");
            tambahRiwayat(alergi, "select group_concat(distinct nullif(trim(pr.alergi),'') separator ', ') "
                    + "from pemeriksaan_ranap pr inner join reg_periksa rp on pr.no_rawat=rp.no_rawat "
                    + "where rp.no_rkm_medis=? and trim(ifnull(pr.alergi,'')) not in ('','-','--','---')");
            taRiwayatAlergi.setText(alergi.toString());
        }
        if (taRiwayatBedah.getText().trim().isEmpty()) {
            StringBuilder bedah = new StringBuilder();
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "select concat(date_format(ki.tgl_masuk,'%d-%m-%Y'),' - ',ifnull(bangsal.nm_bangsal,''),"
                    + "' (',ifnull(kamar.kelas,''),')') as ket "
                    + "from kamar_inap ki inner join reg_periksa rp on ki.no_rawat=rp.no_rawat "
                    + "inner join kamar on kamar.kd_kamar=ki.kd_kamar "
                    + "inner join bangsal on bangsal.kd_bangsal=kamar.kd_bangsal "
                    + "where rp.no_rkm_medis=? order by ki.tgl_masuk desc")) {
                ps.setString(1, TNoRM.getText());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (bedah.length() > 0) {
                            bedah.append("\n");
                        }
                        bedah.append("- ").append(rs.getString("ket"));
                    }
                }
            } catch (Exception e) {
                System.out.println("Notif riwayat ranap asesmen ralan : " + e);
            }
            taRiwayatBedah.setText(bedah.toString());
        }
        taRiwayatAlergi.setCaretPosition(0);
        taRiwayatBedah.setCaretPosition(0);
    }

    private void tambahRiwayat(StringBuilder sb, String sql) {
        String v = Sequel.cariIsi(sql, TNoRM.getText());
        if (v != null && !v.trim().equals("")) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(v.trim());
        }
    }

    private void hitungIMT() {
        try {
            double bb = Double.parseDouble(tBB.getText().trim().replace(",", "."));
            double tb = Double.parseDouble(tTB.getText().trim().replace(",", ".")) / 100.0;
            if (bb > 0 && tb > 0) {
                tIMT.setText(new java.text.DecimalFormat("0.0").format(bb / (tb * tb)));
            }
        } catch (Exception e) {
            // biarkan manual bila input belum lengkap/valid
        }
    }

    // =====================================================================
    //  Riwayat asesmen sebelumnya (salin ke form)
    // =====================================================================

    /** Item dropdown: satu asesmen tersimpan dari kunjungan lain milik pasien yang sama. */
    private static final class ItemHistori {
        final String noRawat, tanggal, poli, petugas;

        ItemHistori(String noRawat, String tanggal, String poli, String petugas) {
            this.noRawat = noRawat;
            this.tanggal = tanggal;
            this.poli = poli;
            this.petugas = petugas;
        }

        @Override
        public String toString() {
            return tanggal + "   ·   " + (poli.isEmpty() ? "-" : poli) + "   ·   No. Rawat " + noRawat;
        }
    }

    private static final String PLACEHOLDER_HISTORI = "Pilih asesmen kunjungan sebelumnya untuk disalin...";

    private void isiDaftarHistori() {
        sedangIsiHistori = true;
        try {
            DefaultComboBoxModel<Object> m = new DefaultComboBoxModel<>();
            int n = 0;
            if (!TNoRM.getText().trim().isEmpty()) {
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "select a.no_rawat,concat(date_format(a.tanggal,'%d-%m-%Y'),' ',left(ifnull(a.jam,''),5)) as tgl,"
                        + "ifnull(poliklinik.nm_poli,'') as poli,ifnull(petugas.nama,ifnull(a.nik,'')) as petugas "
                        + "from asesmen_ralan a inner join reg_periksa rp on a.no_rawat=rp.no_rawat "
                        + "left join poliklinik on rp.kd_poli=poliklinik.kd_poli "
                        + "left join petugas on petugas.nip=a.nik "
                        + "where rp.no_rkm_medis=? and a.no_rawat<>? "
                        + "order by a.tanggal desc,a.jam desc limit 60")) {
                    ps.setString(1, TNoRM.getText());
                    ps.setString(2, noRawat);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            if (n == 0) {
                                m.addElement(PLACEHOLDER_HISTORI);
                            }
                            m.addElement(new ItemHistori(rs.getString("no_rawat"), rs.getString("tgl"),
                                    nz(rs.getString("poli"), ""), nz(rs.getString("petugas"), "")));
                            n++;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notif riwayat asesmen ralan : " + e);
                }
            }
            if (n == 0) {
                m.addElement(TNoRM.getText().trim().isEmpty() ? "Pilih pasien terlebih dahulu"
                        : "Belum ada asesmen dari kunjungan sebelumnya");
            }
            cmbHistori.setModel(m);
            cmbHistori.setEnabled(n > 0);
            cmbHistori.setSelectedIndex(0);
            if (n > 0) {
                lblHistoriInfo.setForeground(MUTED);
                lblHistoriInfo.setText(n + " asesmen sebelumnya tersedia. Data kunjungan lama tidak akan berubah.");
            }
        } finally {
            sedangIsiHistori = false;
        }
    }

    private void resetPilihanHistori() {
        if (cmbHistori.getItemCount() > 0) {
            sedangIsiHistori = true;
            cmbHistori.setSelectedIndex(0);
            sedangIsiHistori = false;
        }
    }

    private void pilihHistori() {
        Object o = cmbHistori.getSelectedItem();
        if (!(o instanceof ItemHistori)) {
            return;
        }
        ItemHistori h = (ItemHistori) o;
        if (adaTersimpan || isiFormAda()) {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Isi form saat ini akan diganti dengan asesmen tanggal " + h.tanggal
                    + (h.poli.isEmpty() ? "" : " (" + h.poli + ")") + ".\n"
                    + "Perubahan baru tersimpan setelah Anda klik Simpan. Lanjutkan?",
                    "Salin asesmen sebelumnya", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (ok != JOptionPane.YES_OPTION) {
                resetPilihanHistori();
                return;
            }
        }
        try (PreparedStatement ps = koneksi.prepareStatement("select * from asesmen_ralan where no_rawat=?")) {
            ps.setString(1, h.noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Data asesmen tersebut sudah tidak ditemukan.");
                    isiDaftarHistori();
                    return;
                }
                isiDari(rs, true);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyalin asesmen.\n" + e.getMessage());
            return;
        }
        lblHistoriInfo.setForeground(AMBER);
        lblHistoriInfo.setText("Disalin dari kunjungan " + h.tanggal + " (No. Rawat " + h.noRawat
                + "). Periksa kembali tanda vital & antropometri, lalu klik Simpan untuk kunjungan ini.");
        lblStatus.set("Disalin · belum disimpan", new Color(245, 158, 11), INK);
        SwingUtilities.invokeLater(() -> taPenyakitSekarang.requestFocusInWindow());
    }

    /** Ada isian klinis di form (selain yang terisi otomatis dari master pasien)? */
    private boolean isiFormAda() {
        for (JTextField t : new JTextField[]{tTD, tSuhu, tNadi, tRR, tBB, tTB, tLingkarKepala,
            tAlatBantu, tProthesa, tCacatTubuh, tAdl, tJumlahSaudara, tWnaAsal}) {
            if (!t.getText().trim().isEmpty()) {
                return true;
            }
        }
        if (!taPenyakitSekarang.getText().trim().isEmpty() || !taPenyakitDahulu.getText().trim().isEmpty()) {
            return true;
        }
        return !grpRujukan.get().isEmpty() || !grpDatang.get().isEmpty() || !grpInformasi.get().isEmpty();
    }

    // =====================================================================
    //  Simpan / muat / hapus
    // =====================================================================

    private static final String[] KOLOM = {
        "no_rawat", "tanggal", "jam", "rujukan", "cara_datang", "informasi_dari",
        "penyakit_sekarang", "penyakit_dahulu", "td", "suhu", "nadi", "rr", "nyeri", "skala_nyeri",
        "bb", "tb", "imt", "lingkar_kepala", "alat_bantu", "prothesa", "cacat_tubuh", "adl", "mandiri",
        "status_nikah", "saudara", "jumlah_saudara", "negara", "wna_asal", "pekerjaan", "tinggal_bersama",
        "nama_keluarga", "telepon", "agama", "riwayat_alergi", "riwayat_bedah", "tgl_ttd", "jam_ttd", "nik"
    };

    private void simpan() {
        if (TNoRw.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        if (KdPetugas.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Pelaksana asesmen belum terisi.");
            return;
        }
        hitungIMT();
        String tgl = Valid.SetTgl(dtpTanggal.getSelectedItem() + "");
        String jam = dtpTanggal.getSelectedItem().toString().substring(11, 19);
        String tglTtd = Valid.SetTgl(dtpTtd.getSelectedItem() + "");
        String jamTtd = dtpTtd.getSelectedItem().toString().substring(11, 19);
        String[] nilai = {
            TNoRw.getText(), tgl, jam, grpRujukan.get(), grpDatang.get(), grpInformasi.get(),
            taPenyakitSekarang.getText(), taPenyakitDahulu.getText(), tTD.getText(), tSuhu.getText(), tNadi.getText(), tRR.getText(),
            s(cmbNyeri), s(cmbSkalaNyeri), tBB.getText(), tTB.getText(), tIMT.getText(), tLingkarKepala.getText(),
            tAlatBantu.getText(), tProthesa.getText(), tCacatTubuh.getText(), tAdl.getText(), s(cmbMandiri),
            s(cmbNikah), s(cmbSaudara), tJumlahSaudara.getText(), s(cmbNegara), tWnaAsal.getText(), s(cmbPekerjaan), s(cmbTinggal),
            tNamaKeluarga.getText(), tTelepon.getText(), s(cmbAgama),
            taRiwayatAlergi.getText(), taRiwayatBedah.getText(), tglTtd, jamTtd, KdPetugas.getText()
        };
        if (KOLOM.length != nilai.length) {
            JOptionPane.showMessageDialog(this, "Kesalahan internal: jumlah kolom (" + KOLOM.length
                    + ") != jumlah nilai (" + nilai.length + ").");
            return;
        }
        StringBuilder cols = new StringBuilder();
        StringBuilder qm = new StringBuilder();
        for (String k : KOLOM) {
            if (cols.length() > 0) {
                cols.append(",");
                qm.append(",");
            }
            cols.append(k);
            qm.append("?");
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "replace into asesmen_ralan (" + cols + ") values (" + qm + ")")) {
            for (int i = 0; i < nilai.length; i++) {
                ps.setString(i + 1, nilai[i]);
            }
            ps.executeUpdate();
            adaTersimpan = true;
            waktuTersimpan = waktuDari(tgl, jam);
            resetPilihanHistori();
            lblHistoriInfo.setForeground(MUTED);
            lblHistoriInfo.setText("Tersimpan untuk kunjungan ini (No. Rawat " + TNoRw.getText() + ").");
            perbaruiStatus();
            JOptionPane.showMessageDialog(this, "Asesmen rawat jalan tersimpan.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan.\n" + e.getMessage());
        }
    }

    private void muat() {
        try (PreparedStatement ps = koneksi.prepareStatement("select * from asesmen_ralan where no_rawat=?")) {
            ps.setString(1, TNoRw.getText());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    isiDari(rs, false);
                    adaTersimpan = true;
                    waktuTersimpan = waktuDari(g(rs, "tanggal"), g(rs, "jam"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat asesmen ralan : " + e);
        }
    }

    /**
     * Isi form dari satu baris asesmen_ralan.
     *
     * @param salinan true = menyalin dari kunjungan lain: tanggal asesmen, tanggal TTD dan
     *                pelaksana TIDAK ikut disalin (tetap waktu sekarang & petugas yang login),
     *                karena itu milik kunjungan ini.
     */
    private void isiDari(ResultSet rs, boolean salinan) {
        if (!salinan) {
            setTgl(dtpTanggal, g(rs, "tanggal"), g(rs, "jam"));
        }
        grpRujukan.set(g(rs, "rujukan"));
        grpDatang.set(g(rs, "cara_datang"));
        grpInformasi.set(g(rs, "informasi_dari"));
        taPenyakitSekarang.setText(g(rs, "penyakit_sekarang"));
        taPenyakitDahulu.setText(g(rs, "penyakit_dahulu"));
        tTD.setText(g(rs, "td"));
        tSuhu.setText(g(rs, "suhu"));
        tNadi.setText(g(rs, "nadi"));
        tRR.setText(g(rs, "rr"));
        setCombo(cmbNyeri, g(rs, "nyeri"));
        setCombo(cmbSkalaNyeri, g(rs, "skala_nyeri"));
        tBB.setText(g(rs, "bb"));
        tTB.setText(g(rs, "tb"));
        tIMT.setText(g(rs, "imt"));
        tLingkarKepala.setText(g(rs, "lingkar_kepala"));
        tAlatBantu.setText(g(rs, "alat_bantu"));
        tProthesa.setText(g(rs, "prothesa"));
        tCacatTubuh.setText(g(rs, "cacat_tubuh"));
        tAdl.setText(g(rs, "adl"));
        setCombo(cmbMandiri, g(rs, "mandiri"));
        setCombo(cmbNikah, g(rs, "status_nikah"));
        setCombo(cmbSaudara, g(rs, "saudara"));
        tJumlahSaudara.setText(g(rs, "jumlah_saudara"));
        setCombo(cmbNegara, g(rs, "negara"));
        tWnaAsal.setText(g(rs, "wna_asal"));
        setCombo(cmbPekerjaan, g(rs, "pekerjaan"));
        setCombo(cmbTinggal, g(rs, "tinggal_bersama"));
        setTeksBilaAda(tNamaKeluarga, g(rs, "nama_keluarga"));
        setTeksBilaAda(tTelepon, g(rs, "telepon"));
        setCombo(cmbAgama, g(rs, "agama"));
        taRiwayatAlergi.setText(g(rs, "riwayat_alergi"));
        taRiwayatBedah.setText(g(rs, "riwayat_bedah"));
        if (salinan) {
            dtpTanggal.setDate(new Date());
            dtpTtd.setDate(new Date());
            KdPetugas.setText(akses.getkode());
            NmPetugas.setText(Sequel.cariIsi("select nama from petugas where nip=?", akses.getkode()));
            lengkapiRiwayatOtomatis();
        } else {
            setTgl(dtpTtd, g(rs, "tgl_ttd"), g(rs, "jam_ttd"));
            if (!g(rs, "nik").trim().equals("")) {
                KdPetugas.setText(g(rs, "nik"));
                NmPetugas.setText(Sequel.cariIsi("select nama from petugas where nip=?", g(rs, "nik")));
            }
        }
        for (JTextArea a : new JTextArea[]{taPenyakitSekarang, taPenyakitDahulu, taRiwayatAlergi, taRiwayatBedah}) {
            a.setCaretPosition(0);
        }
    }

    private void hapus() {
        if (TNoRw.getText().trim().equals("")) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus asesmen rawat jalan untuk No.Rawat " + TNoRw.getText() + " ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement("delete from asesmen_ralan where no_rawat=?")) {
            ps.setString(1, TNoRw.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data dihapus.");
            kosongkanForm();
            isiDaftarHistori();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus.\n" + e.getMessage());
        }
    }

    private void perbaruiStatus() {
        if (TNoRw.getText().trim().isEmpty()) {
            lblStatus.set("Belum ada pasien dipilih", new Color(255, 255, 255, 40), Color.WHITE);
            lblBantuan.setText("Pilih pasien di daftar rawat jalan untuk mengisi asesmen.");
            return;
        }
        if (adaTersimpan) {
            lblStatus.set("Tersimpan" + (waktuTersimpan.trim().isEmpty() ? "" : "  ·  " + waktuTersimpan),
                    new Color(16, 185, 129), Color.WHITE);
        } else {
            lblStatus.set("Belum diisi untuk kunjungan ini", new Color(255, 255, 255, 40), Color.WHITE);
        }
        lblBantuan.setText("Simpan menyimpan asesmen untuk kunjungan No. Rawat " + TNoRw.getText() + ".");
    }

    // =====================================================================
    //  Cetak gabungan
    // =====================================================================

    private void cetak() {
        if (TNoRw.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        List<Map<String, ?>> rows = CetakAsesmen.mulai();
        CetakAsesmen.h(rows, "Asesmen Rawat Jalan");
        CetakAsesmen.r(rows, "Tanggal / Pukul", dtpTanggal.getSelectedItem() + "");
        CetakAsesmen.r(rows, "Rujukan", grpRujukan);
        CetakAsesmen.r(rows, "Cara Datang", grpDatang);
        CetakAsesmen.r(rows, "Informasi Dari", grpInformasi);
        CetakAsesmen.r(rows, "Penyakit Sekarang", taPenyakitSekarang.getText());
        CetakAsesmen.r(rows, "Penyakit Dahulu", taPenyakitDahulu.getText());

        CetakAsesmen.h(rows, "Tanda Vital");
        CetakAsesmen.r2(rows, "TD", tTD.getText() + " mmHg", "Suhu", tSuhu.getText() + " C");
        CetakAsesmen.r2(rows, "Nadi", tNadi.getText() + " x/mnt", "RR", tRR.getText() + " x/mnt");
        CetakAsesmen.r(rows, "Nyeri / Skala", s(cmbNyeri) + " (" + s(cmbSkalaNyeri) + ")");

        CetakAsesmen.h(rows, "Antropometri");
        CetakAsesmen.r2(rows, "BB", tBB.getText() + " kg", "TB", tTB.getText() + " cm");
        CetakAsesmen.r2(rows, "IMT", tIMT.getText() + " kg/m2", "Lingkar Kepala", tLingkarKepala.getText() + " cm");

        CetakAsesmen.h(rows, "Fungsional");
        CetakAsesmen.r2(rows, "Alat Bantu", tAlatBantu.getText(), "Prothesa", tProthesa.getText());
        CetakAsesmen.r2(rows, "Cacat Tubuh", tCacatTubuh.getText(), "ADL", tAdl.getText());
        CetakAsesmen.r(rows, "Kemandirian", s(cmbMandiri));

        CetakAsesmen.h(rows, "Sosial / Ekonomi / Spiritual");
        CetakAsesmen.r(rows, "Pernikahan", s(cmbNikah));
        CetakAsesmen.r(rows, "Saudara", s(cmbSaudara) + (tJumlahSaudara.getText().trim().isEmpty() ? "" : " (" + tJumlahSaudara.getText() + " orang)"));
        CetakAsesmen.r(rows, "Negara", s(cmbNegara) + (tWnaAsal.getText().trim().isEmpty() ? "" : " - " + tWnaAsal.getText()));
        CetakAsesmen.r(rows, "Pekerjaan", s(cmbPekerjaan));
        CetakAsesmen.r(rows, "Tinggal Bersama", s(cmbTinggal));
        CetakAsesmen.r2(rows, "Nama Keluarga", tNamaKeluarga.getText(), "Telpon", tTelepon.getText());
        CetakAsesmen.r(rows, "Agama", s(cmbAgama));

        CetakAsesmen.h(rows, "Riwayat Alergi & Pembedahan");
        CetakAsesmen.r(rows, "Riwayat Alergi", nz(taRiwayatAlergi.getText(), ""));
        CetakAsesmen.r(rows, "Riwayat Pembedahan / Rawat Inap", nz(taRiwayatBedah.getText(), ""));

        CetakAsesmen.h(rows, "Ringkasan Kunjungan Rawat Jalan");
        tambahRingkasanKunjungan(rows);

        CetakAsesmen.Identitas id = new CetakAsesmen.Identitas();
        id.nama = TPasien.getText();
        id.noRawat = TNoRw.getText();
        id.kelas = TKelas.getText();
        id.nik = TNIK.getText();
        id.tglMasuk = Sequel.cariIsi("select concat(date_format(tgl_registrasi,'%d-%m-%Y'),' ',jam_reg) "
                + "from reg_periksa where no_rawat=?", TNoRw.getText());
        id.pembayaran = TCaraBayar.getText();
        id.jk = TJK.getText();
        id.noRM = TNoRM.getText();
        id.unit = TUnit.getText();
        id.tglLahir = TTglLahir.getText();
        id.alamat = TAlamat.getText();

        CetakAsesmen.cetak("ASESMEN & RINGKASAN RAWAT JALAN", "RM RJ", rows, id,
                dtpTtd.getSelectedItem() + "", "Pelaksana Asesmen", KdPetugas.getText(), NmPetugas.getText(),
                "", "");
    }

    /** Cetak langsung dari no_rawat tanpa membuka form (dipakai dari klik-kanan di layar Riwayat). */
    public static void cetak(String noRawat) {
        if (noRawat == null || noRawat.trim().isEmpty()) {
            return;
        }
        RMAsesmenRalan f = new RMAsesmenRalan();
        f.setKonteks(noRawat.trim());
        f.cetak();
    }

    /** Ringkasan kunjungan: 1 baris per kunjungan (tanggal, poli, diagnosa, terapi, dokter). */
    private void tambahRingkasanKunjungan(List<Map<String, ?>> rows) {
        int n = 0;
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select concat(date_format(pr.tgl_perawatan,'%d-%m-%Y'),' ',left(pr.jam_rawat,5)) as tanggal,"
                + "ifnull(poliklinik.nm_poli,'') as nm_poli,ifnull(dokter.nm_dokter,'') as nm_dokter,"
                + "ifnull((select group_concat(distinct penyakit.nm_penyakit separator ', ') from diagnosa_pasien dp "
                + "inner join penyakit on penyakit.kd_penyakit=dp.kd_penyakit where dp.no_rawat=pr.no_rawat),'') as diagnosa,"
                + "ifnull((select group_concat(concat(databarang.nama_brng,' (',resep_dokter.jml,' ',resep_dokter.aturan_pakai,')') separator ', ') "
                + "from resep_obat inner join resep_dokter on resep_dokter.no_resep=resep_obat.no_resep "
                + "inner join databarang on databarang.kode_brng=resep_dokter.kode_brng where resep_obat.no_rawat=pr.no_rawat),'') as terapi "
                + "from pemeriksaan_ralan pr "
                + "inner join reg_periksa on pr.no_rawat=reg_periksa.no_rawat "
                + "left join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "
                + "left join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "
                + "where reg_periksa.no_rkm_medis=? order by pr.tgl_perawatan desc,pr.jam_rawat desc limit 50")) {
            ps.setString(1, TNoRM.getText());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    n++;
                    String ket = nz(rs.getString("nm_poli"), "-") + " | Diagnosa: " + nz(rs.getString("diagnosa"), "-")
                            + " | Terapi: " + nz(rs.getString("terapi"), "-") + " | Dokter: " + nz(rs.getString("nm_dokter"), "-");
                    CetakAsesmen.r(rows, rs.getString("tanggal"), ket);
                }
            }
        } catch (Exception e) {
            System.out.println("Notif ringkasan kunjungan asesmen ralan : " + e);
        }
        if (n == 0) {
            CetakAsesmen.r(rows, "Ringkasan Kunjungan", "Data tidak ditemukan");
        }
    }

    // =====================================================================
    //  util data
    // =====================================================================

    private static String nz(String v, String def) {
        return (v == null || v.trim().isEmpty()) ? def : v;
    }

    private String s(JComboBox<String> c) {
        Object o = c.getSelectedItem();
        return o == null ? "" : o.toString();
    }

    private String g(ResultSet rs, String kolom) {
        try {
            String v = rs.getString(kolom);
            return v == null ? "" : v;
        } catch (Exception e) {
            return "";
        }
    }

    private void setCombo(JComboBox<String> c, String v) {
        if (v == null || v.trim().equals("")) {
            return;
        }
        c.setSelectedItem(v.trim());
    }

    /** "2026-11-18" + "15:05:58" -> "18-11-2026 15:05". */
    private static String waktuDari(String tgl, String jam) {
        if (tgl == null || tgl.length() < 10) {
            return "";
        }
        String j = jam == null ? "" : (jam.length() >= 5 ? jam.substring(0, 5) : jam);
        return tgl.substring(8, 10) + "-" + tgl.substring(5, 7) + "-" + tgl.substring(0, 4) + (j.isEmpty() ? "" : " " + j);
    }

    private void setTeksBilaAda(JTextField t, String v) {
        if (v != null && !v.trim().isEmpty()) {
            t.setText(v);
        }
    }

    /** Mapping status nikah dari master pasien ke pilihan combo. */
    private void setNikahFromMaster(String v) {
        if (v == null) {
            return;
        }
        String low = v.toLowerCase();
        if (low.contains("belum")) {
            cmbNikah.setSelectedItem("Belum menikah");
        } else if (low.contains("cerai")) {
            cmbNikah.setSelectedItem("Duda/Janda (bercerai)");
        } else if (low.contains("janda") || low.contains("duda")) {
            cmbNikah.setSelectedItem("Duda/Janda (meninggal)");
        } else if (low.contains("nikah") || low.contains("kawin")) {
            cmbNikah.setSelectedItem("Menikah");
        }
    }

    private void setTgl(widget.Tanggal picker, String tgl, String jam) {
        if (tgl == null || tgl.startsWith("0000") || tgl.trim().equals("")) {
            picker.setDate(new Date());
            return;
        }
        String jm = (jam == null || jam.trim().equals("")) ? "00:00:00" : jam;
        if (jm.length() > 8) {
            jm = jm.substring(0, 8);
        }
        try {
            picker.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(tgl.substring(0, 10) + " " + jm));
        } catch (Exception e) {
            picker.setDate(new Date());
        }
    }

    private void pastikanTabel() {
        try {
            koneksi.prepareStatement(
                    "create table if not exists asesmen_ralan ("
                    + "no_rawat varchar(17) not null primary key,"
                    + "tanggal date,jam time,rujukan text,cara_datang text,informasi_dari text,"
                    + "penyakit_sekarang text,penyakit_dahulu text,td text,suhu text,nadi text,rr text,nyeri text,skala_nyeri text,"
                    + "bb text,tb text,imt text,lingkar_kepala text,alat_bantu text,prothesa text,cacat_tubuh text,adl text,mandiri text,"
                    + "status_nikah text,saudara text,jumlah_saudara text,negara text,wna_asal text,pekerjaan text,tinggal_bersama text,"
                    + "nama_keluarga text,telepon text,agama text,riwayat_alergi text,riwayat_bedah text,tgl_ttd date,jam_ttd time,nik varchar(20)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=latin1").execute();
        } catch (Exception e) {
            System.out.println("Notif pastikan tabel asesmen_ralan : " + e);
        }
        // Migrasi: tambah kolom riwayat bila tabel sudah ada dari versi sebelumnya
        tambahKolomBilaHilang("riwayat_alergi", "text");
        tambahKolomBilaHilang("riwayat_bedah", "text");
    }

    private void tambahKolomBilaHilang(String kolom, String tipe) {
        try {
            if (Sequel.cariInteger("select count(*) from information_schema.columns where table_schema=database() "
                    + "and table_name='asesmen_ralan' and column_name='" + kolom + "'") == 0) {
                koneksi.prepareStatement("alter table asesmen_ralan add column " + kolom + " " + tipe).execute();
            }
        } catch (Exception e) {
            System.out.println("Notif migrasi kolom asesmen_ralan (" + kolom + ") : " + e);
        }
    }

    // =====================================================================
    //  Komponen bergaya (Segoe UI, flat, border tipis, fokus TEAL)
    // =====================================================================

    private static Border borderField(Color warna) {
        return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(warna),
                BorderFactory.createEmptyBorder(4, 10, 4, 10));
    }

    private static void fokusTeal(JComponent c, final JComponent pemilikBorder, final Border normal, final Border fokus) {
        c.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                pemilikBorder.setBorder(fokus);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                pemilikBorder.setBorder(normal);
            }
        });
    }

    private static JTextField tf() {
        JTextField t = new JTextField();
        t.setFont(new Font(FONT, Font.PLAIN, 13));
        t.setForeground(INK);
        t.setBackground(Color.WHITE);
        t.setCaretColor(TEAL);
        Border normal = borderField(GARIS), fokus = borderField(TEAL);
        t.setBorder(normal);
        fokusTeal(t, t, normal, fokus);
        t.setPreferredSize(new Dimension(120, 34));
        return t;
    }

    /** Nilai read-only: tampil seperti teks (tanpa kotak), tetap bisa diseleksi/disalin. */
    private static JTextField ro() {
        JTextField t = new JTextField();
        t.setEditable(false);
        t.setOpaque(false);
        t.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
        t.setFont(new Font(FONT, Font.BOLD, 14));
        t.setForeground(INK);
        return t;
    }

    private static JTextArea ta() {
        JTextArea t = new JTextArea();
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        t.setFont(new Font(FONT, Font.PLAIN, 13));
        t.setForeground(INK);
        t.setCaretColor(TEAL);
        t.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
        return t;
    }

    private static JComboBox<String> cmb(String... items) {
        JComboBox<String> c = new JComboBox<>(items);
        gayaCombo(c);
        return c;
    }

    private static void gayaCombo(JComboBox<?> c) {
        c.setFont(new Font(FONT, Font.PLAIN, 14));
        c.setForeground(INK);
        c.setBackground(Color.WHITE);
        c.setPreferredSize(new Dimension(140, 34));
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        c.setBorder(BorderFactory.createLineBorder(GARIS));
        if (!(c.getRenderer() instanceof RendererHistori)) {
            c.setRenderer(new RendererPolos());
        }
    }

    /**
     * Basis renderer: untuk area tampilan combo (index -1) warna dikunci. BasicComboBoxUI menimpa
     * background/foreground dengan warna seleksi (biru-abu) SETELAH renderer dipanggil saat combo fokus.
     */
    private static class RendererTerkunci extends DefaultListCellRenderer {
        private boolean kunci = false;

        Component siapkan(JList<?> list, Object value, int index, boolean sel, boolean focus) {
            kunci = false;
            return super.getListCellRendererComponent(list, value, index, sel, focus);
        }

        void kunciBila(int index) {
            kunci = index == -1;
        }

        @Override
        public void setBackground(Color c) {
            if (!kunci) {
                super.setBackground(c);
            }
        }

        @Override
        public void setForeground(Color c) {
            if (!kunci) {
                super.setForeground(c);
            }
        }
    }

    /** Renderer combo biasa: area tampilan selalu putih (bukan biru-abu bawaan Metal saat fokus). */
    private static class RendererPolos extends RendererTerkunci {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean focus) {
            siapkan(list, value, index, sel, focus);
            setFont(new Font(FONT, Font.PLAIN, 14));
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            if (index == -1) {
                setBackground(Color.WHITE);
                setForeground(INK);
            } else if (sel) {
                setBackground(TEAL);
                setForeground(Color.WHITE);
            } else {
                setBackground(Color.WHITE);
                setForeground(INK);
            }
            kunciBila(index);
            return this;
        }
    }

    private static widget.Tanggal dt() {
        widget.Tanggal d = new widget.Tanggal();
        d.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        d.setFont(new Font(FONT, Font.PLAIN, 14));
        d.setForeground(INK);
        d.setPreferredSize(new Dimension(200, 34));
        d.setBorder(BorderFactory.createLineBorder(GARIS));
        return d;
    }

    private static JScrollPane area(JTextArea a, int baris) {
        a.setRows(baris);
        JScrollPane sc = new JScrollPane(a);
        Border normal = BorderFactory.createLineBorder(GARIS), fokus = BorderFactory.createLineBorder(TEAL);
        sc.setBorder(normal);
        sc.getViewport().setBackground(Color.WHITE);
        sc.setWheelScrollingEnabled(false);   // scroll halaman, bukan area kecil
        fokusTeal(a, sc, normal, fokus);
        FontMetrics fm = a.getFontMetrics(a.getFont());
        sc.setPreferredSize(new Dimension(200, baris * fm.getHeight() + 18));
        return sc;
    }

    /** Field dengan satuan di sisi kanan (mmHg, kg, ...). */
    private static JPanel satuan(JTextField t, String unit) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.add(t, BorderLayout.CENTER);
        JLabel u = new JLabel(unit);
        u.setFont(new Font(FONT, Font.PLAIN, 12));
        u.setForeground(MUTED);
        u.setPreferredSize(new Dimension(48, 34));
        p.add(u, BorderLayout.EAST);
        return p;
    }

    private static JLabel caption(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font(FONT, Font.PLAIN, 13));
        l.setForeground(MUTED);
        return l;
    }

    private static JButton tombol(String teks, final Color warna) {
        JButton b = new JButton(teks) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(!isEnabled() ? new Color(218, 226, 232)
                        : getModel().isPressed() ? warna.darker() : getModel().isRollover() ? warna.brighter() : warna);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (hasFocus()) {
                    g2.setColor(new Color(166, 215, 232));
                    g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setForeground(Color.WHITE);
        b.setFont(new Font(FONT, Font.BOLD, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(108, 40));
        return b;
    }

    /** Kartu putih rounded dengan judul 16 bold + subjudul 12 MUTED. */
    private static JPanel kartu(String judul, String detail, Component isi) {
        Surface card = new Surface(new BorderLayout(0, 12), Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 16, 18));
        JPanel kepala = new JPanel(new GridLayout(2, 1, 0, 2));
        kepala.setOpaque(false);
        JLabel h = new JLabel(judul);
        h.setFont(new Font(FONT, Font.BOLD, 16));
        h.setForeground(INK);
        JLabel d = new JLabel(detail);
        d.setFont(new Font(FONT, Font.PLAIN, 12));
        d.setForeground(MUTED);
        kepala.add(h);
        kepala.add(d);
        card.add(kepala, BorderLayout.NORTH);
        // isi menempel ke atas walau kartu ditinggikan menyamai kartu di sebelahnya
        JPanel atas = new JPanel(new BorderLayout());
        atas.setOpaque(false);
        atas.add(isi, BorderLayout.NORTH);
        card.add(atas, BorderLayout.CENTER);
        return card;
    }

    /** Satu baris kartu: dua kartu berdampingan (tinggi disamakan) atau satu kartu penuh. */
    private static int baris(JPanel p, int y, JPanel kiri, JPanel kanan) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = y;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 10, kanan == null ? 0 : 5);
        c.gridx = 0;
        c.gridwidth = kanan == null ? 2 : 1;
        p.add(kiri, c);
        if (kanan != null) {
            c.gridx = 1;
            c.insets = new Insets(0, 5, 10, 0);
            p.add(kanan, c);
        }
        return y + 1;
    }

    private static GridBagConstraints gc(int x, int y) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x;
        g.gridy = y;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        return g;
    }

    /** Pembangun form label-kiri di dalam kartu (1 atau 2 pasang per baris). */
    private static final class Form {
        final JPanel panel = new JPanel(new GridBagLayout());
        private int y = 0;

        Form() {
            panel.setOpaque(false);
        }

        void baris(String label, Component comp) {
            GridBagConstraints c = gc(0, y);
            c.insets = new Insets(4, 0, 4, 12);
            c.anchor = GridBagConstraints.NORTHWEST;
            panel.add(labelBaris(label), c);
            c = gc(1, y);
            c.gridwidth = 3;
            c.weightx = 1;
            c.insets = new Insets(4, 0, 4, 0);
            panel.add(comp, c);
            y++;
        }

        void baris2(String l1, Component c1, String l2, Component c2) {
            GridBagConstraints c = gc(0, y);
            c.insets = new Insets(4, 0, 4, 12);
            panel.add(labelBaris(l1), c);
            c = gc(1, y);
            c.weightx = 1;
            c.insets = new Insets(4, 0, 4, 18);
            panel.add(c1, c);
            c = gc(2, y);
            c.insets = new Insets(4, 0, 4, 12);
            panel.add(labelBaris(l2), c);
            c = gc(3, y);
            c.weightx = 1;
            c.insets = new Insets(4, 0, 4, 0);
            panel.add(c2, c);
            y++;
        }

        private static JLabel labelBaris(String teks) {
            JLabel l = caption(teks);
            l.setPreferredSize(new Dimension(Math.max(l.getPreferredSize().width, 112), 34));
            l.setVerticalAlignment(SwingConstants.CENTER);
            return l;
        }
    }

    /** Grup pilihan berbentuk chip; get/set sebagai string gabungan dipisah koma. */
    private static final class Grup implements CetakAsesmen.OpsiCheckbox {
        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        final List<JCheckBox> boxes = new ArrayList<>();

        Grup(String... items) {
            panel.setOpaque(false);
            for (String it : items) {
                Chip c = new Chip(it);
                boxes.add(c);
                panel.add(c);
            }
        }

        @Override
        public List<String> semuaOpsi() {
            List<String> hasil = new ArrayList<>();
            for (JCheckBox c : boxes) {
                hasil.add(c.getText());
            }
            return hasil;
        }

        @Override
        public String get() {
            StringBuilder sb = new StringBuilder();
            for (JCheckBox c : boxes) {
                if (c.isSelected()) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(c.getText());
                }
            }
            return sb.toString();
        }

        void set(String v) {
            Set<String> sel = new HashSet<>();
            if (v != null) {
                for (String x : v.split(",")) {
                    sel.add(x.trim());
                }
            }
            for (JCheckBox c : boxes) {
                c.setSelected(sel.contains(c.getText()));
            }
        }

        void clear() {
            for (JCheckBox c : boxes) {
                c.setSelected(false);
            }
        }
    }

    /** Checkbox yang digambar sebagai chip/pill: terpilih = TEAL penuh, tidak = abu muda. */
    private static final class Chip extends JCheckBox {
        Chip(String teks) {
            super(teks);
            setOpaque(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFont(new Font(FONT, Font.PLAIN, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setRolloverEnabled(true);
            FontMetrics fm = getFontMetrics(new Font(FONT, Font.BOLD, 13));
            setPreferredSize(new Dimension(fm.stringWidth(teks) + 44, 32));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth() - 1, h = getHeight() - 1;
            boolean on = isSelected(), hover = getModel().isRollover();
            g2.setColor(on ? TEAL : hover ? new Color(226, 240, 238) : new Color(240, 245, 248));
            g2.fillRoundRect(0, 0, w, h, h, h);
            g2.setColor(on ? TEAL : hover ? new Color(150, 200, 195) : GARIS);
            g2.drawRoundRect(0, 0, w, h, h, h);
            if (hasFocus()) {
                g2.setColor(new Color(166, 215, 232));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(2, 2, w - 4, h - 4, h - 4, h - 4);
            }
            // ikon kecil: centang saat terpilih, lingkaran kosong saat tidak
            int cx = 13, cy = h / 2;
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (on) {
                g2.setColor(Color.WHITE);
                g2.drawPolyline(new int[]{cx - 4, cx - 1, cx + 4}, new int[]{cy, cy + 3, cy - 3}, 3);
            } else {
                g2.setColor(new Color(160, 174, 188));
                g2.drawOval(cx - 4, cy - 4, 8, 8);
            }
            g2.setFont(on ? getFont().deriveFont(Font.BOLD) : getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(on ? Color.WHITE : INK);
            g2.drawString(getText(), 25, (h - fm.getHeight()) / 2 + fm.getAscent() + 1);
            g2.dispose();
        }
    }

    /** Label status berbentuk pill (di header gelap). */
    private static final class Pill extends JLabel {
        private Color isi = new Color(255, 255, 255, 40);

        Pill() {
            setFont(new Font(FONT, Font.BOLD, 12));
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        void set(String teks, Color bg, Color fg) {
            setText(teks);
            isi = bg;
            setForeground(fg);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isi);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Renderer dropdown riwayat: tanggal tebal + poli & no. rawat abu. */
    private static final class RendererHistori extends RendererTerkunci {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean focus) {
            siapkan(list, value, index, sel, focus);
            setFont(new Font(FONT, Font.PLAIN, 14));
            setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            if (value instanceof ItemHistori) {
                ItemHistori h = (ItemHistori) value;
                String warnaAbu = (sel && index != -1) ? "#e2f3f1" : "#64748b";
                setText("<html><b>" + esc(h.tanggal) + "</b>&nbsp;&nbsp;<span style='color:" + warnaAbu + "'>"
                        + esc(h.poli.isEmpty() ? "-" : h.poli) + "&nbsp;&nbsp;·&nbsp;&nbsp;No. Rawat " + esc(h.noRawat)
                        + (h.petugas.isEmpty() ? "" : "&nbsp;&nbsp;·&nbsp;&nbsp;" + esc(h.petugas)) + "</span></html>");
            } else {
                setForeground(MUTED);
            }
            if (index == -1) {
                setBackground(Color.WHITE);      // area tampilan combo: selalu putih
                if (!(value instanceof ItemHistori)) {
                    setForeground(MUTED);
                }
            } else if (sel) {
                setBackground(TEAL);
                setForeground(Color.WHITE);
            } else {
                setBackground(Color.WHITE);
            }
            kunciBila(index);
            return this;
        }

        private static String esc(String s) {
            return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }

    /** Panel isi yang selalu mengikuti lebar viewport (tanpa scroll horizontal). */
    private static final class Konten extends JPanel implements Scrollable {
        Konten(LayoutManager l) {
            super(l);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
            return Math.max(r.height - 40, 40);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /** Panel rounded-corner (gaya AdimeGiziView). */
    private static final class Surface extends JPanel {
        private final Color fill;

        Surface(LayoutManager layout, Color fill) {
            super(layout);
            this.fill = fill;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D p = (Graphics2D) g.create();
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            p.setColor(fill);
            p.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            p.dispose();
        }
    }
}
