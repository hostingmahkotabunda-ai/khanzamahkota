package rekammedis;

import fungsi.akses;
import fungsi.koneksiDB;
import kepegawaian.DlgCariDokter;
import kepegawaian.DlgCariPegawai;
import widget.TabRoundedRawatInapUI;
import widget.Tanggal;
import widget.Toast;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Form CP (Clinical Pathway) Anak - Bayi Berat Lahir Rendah (BBLR). Arsitektur & mekanisme PERSIS
 * mengikuti {@link RMCPBronkopneumonia} (form CP pertama/pola acuan), termasuk dukungan sel teks
 * bebas utk baris Nutrisi yg diperkenalkan di {@link RMCPKejangDemam}.
 *
 * - Identitas pasien & ruang rawat ditarik otomatis dari reg_periksa/pasien/kamar_inap.
 * - Grid harian: default 3 kolom "Hari Rawat", bisa ditambah lewat tombol "+ Tambah Hari".
 * - Tombol "Lihat SOAP" membuka {@link RMCariSOAPRawatIni} (non-modal) supaya petugas tidak
 *   perlu keluar dari form ini saat mengisi, krn isian CP memang diambil dari SOAP.
 * - Data tersimpan di cp_bblr (header) + cp_bblr_detail (grid, per
 *   baris = 1 sel hari supaya jumlah hari bisa berapa saja tanpa ubah struktur tabel).
 */
public final class RMCPBBLR extends javax.swing.JDialog {

    // ---- palet modern (disamakan dgn widget.TabRoundedRawatInapUI & widget.Toast spy 1 bahasa visual) ----
    private static final Color BIRU = new Color(24, 112, 220);
    private static final Color BIRU_GELAP = new Color(14, 78, 158);
    private static final Color TEKS = new Color(58, 80, 109);
    private static final Color GARIS = new Color(207, 220, 235);
    private static final Color BG_SOFT = new Color(245, 248, 252);
    private static final Color HIJAU = new Color(0x38, 0x8E, 0x3C);
    private static final Color MERAH = new Color(0xC6, 0x28, 0x28);
    private static final String NAMA_FONT = "Segoe UI";
    private static final Font FONT_LABEL = new Font(NAMA_FONT, Font.PLAIN, 13);
    private static final Font FONT_BOLD = new Font(NAMA_FONT, Font.BOLD, 13);
    private static final int TINGGI_BARIS = 32;
    private static final int LEBAR_HARI = 90;
    private static final int LEBAR_LABEL = 290;
    private static final int MAKS_HARI = 14;

    // ---- katalog baris grid: seksi -> daftar {item_kode, label[, "T"=sel teks bebas]} ----
    private static final LinkedHashMap<String, String[][]> KATALOG = new LinkedHashMap<>();
    static {
        KATALOG.put("Penyakit Penyerta", new String[][]{
            {"penyerta_aspirasi_pneumoni", "Aspirasi Pneumoni"},
            {"penyerta_rds", "RDS"}
        });
        KATALOG.put("Komplikasi", new String[][]{
            {"komplikasi_gagal_napas", "Gagal Napas"},
            {"komplikasi_sepsis", "Sepsis"},
            {"komplikasi_hipoglikemi", "Hipoglikemi"}
        });
        KATALOG.put("Assesmen Klinis", new String[][]{
            {"assesmen_dr_igd_poli_ruangan", "Pemeriksaan dr di IGD / Poli / Ruangan"},
            {"assesmen_dr_sp_anak", "Pemeriksaan Dokter Sp. Anak"}
        });
        KATALOG.put("Pemeriksaan Penunjang", new String[][]{
            {"penunjang_darah_lengkap", "Darah Lengkap"},
            {"penunjang_rontgen_thorax", "Rontgen Thorax"},
            {"penunjang_gds", "GDS"},
            {"penunjang_agd", "AGD"}
        });
        KATALOG.put("Tindakan", new String[][]{
            {"tindakan_pasang_ivfd", "Pasang IVFD"},
            {"tindakan_pasang_oksigen", "Pasang Oksigen"},
            {"tindakan_pasang_cpap", "Pasang CPAP"},
            {"tindakan_inkubator", "Inkubator"}
        });
        KATALOG.put("Obat-obatan", new String[][]{
            {"obat_cefotaxim", "Cefotaxim (Antibiotik)"},
            {"obat_ceftriaxon", "Ceftriaxon (Antibiotik)"},
            {"obat_amikasin", "Amikasin (Antibiotik)"},
            {"obat_gentamisin", "Gentamisin (Antibiotik)"},
            {"obat_meropenem", "Meropenem (Antibiotik)"},
            {"obat_dexametason", "Dexametason"},
            {"obat_aminofilin", "Aminofilin Inj."},
            {"obat_dextrose_40", "Dextrose 40%"},
            {"obat_meylon", "Meylon Inj."},
            {"obat_aminofusin_paed", "Aminofusin Paed"}
        });
        KATALOG.put("Alkes", new String[][]{
            {"alkes_iv_set", "IV Set"},
            {"alkes_microburet", "Microburet"},
            {"alkes_spuit_10cc", "Spuit 10 cc"},
            {"alkes_spuit_3cc", "Spuit 3 cc"},
            {"alkes_needle_26", "Needle 26"},
            {"alkes_selang_oksigen", "Selang Oksigen"},
            {"alkes_cpap_set", "CPAP Set"}
        });
        KATALOG.put("Nutrisi", new String[][]{
            {"nutrisi_kkal", "Kebutuhan Kalori (kkal/hari)", "T"},
            {"nutrisi_protein", "Kebutuhan Protein (mg)", "T"}
        });
        KATALOG.put("Hasil (Outcome)", new String[][]{
            {"hasil_pernafasan", "Pernafasan"},
            {"hasil_kesadaran", "Kesadaran"}
        });
        KATALOG.put("Pendidikan / Rencana Pemulangan", new String[][]{
            {"edukasi_perawatan_rumah", "Perawatan di Rumah"},
            {"edukasi_diet_rumah", "Pemberian Diet di Rumah"}
        });
    }

    private final Connection koneksi = koneksiDB.condb();
    private final RMCariSOAPRawatIni dlgSoap;
    private String noRawat = "";
    private boolean kotor = false;
    private Date tglMasuk;

    // identitas (read-only, ditarik otomatis)
    private final JLabel lblJudulPasien = new JLabel("-");
    private final JLabel lblNoRM = labelNilai();
    private final JLabel lblUmurJk = labelNilai();
    private final JComboBox<String> cbRuang = new JComboBox<>();
    private final Tanggal tMasuk = new Tanggal();
    private final JTextField tHariRawatKe = kotakHeader(36);

    // isian header
    private final JTextField tBB = kotakKecil();
    private final JTextField tTB = kotakKecil();
    private final JTextField tDiagnosisAwal = kotakLebar();
    private final JTextField tKodeICD10 = kotakKecil();
    private final JTextField tRencanaRawat = kotakKecil();

    // grid harian
    private final List<Integer> daftarHari = new ArrayList<>();
    private final Map<String, Map<Integer, String>> nilaiGrid = new LinkedHashMap<>();
    private final JPanel panelRowHeader = new JPanel();
    private final JPanel panelKolomHeader = new JPanel();
    private final JPanel panelGrid = new JPanel();
    private final JLabel lblInfoHari = new JLabel();
    private final JScrollPane scrollGrid = new JScrollPane();

    // penutup
    private final JTextArea tVarians = new JTextArea(4, 20);
    private final JTextField tJumlahBiaya = kotakKecil();
    private final JTextField[] tDiagAkhir = {kotakLebar(), kotakLebar(), kotakLebar()};
    private final JTextField[] tIcd10Akhir = {kotakKecil(), kotakKecil(), kotakKecil()};
    private final JTextField[] tTindakanAkhir = {kotakLebar(), kotakLebar(), kotakLebar()};
    private final JTextField[] tIcd9Akhir = {kotakKecil(), kotakKecil(), kotakKecil()};
    private final JTextField tNamaPerawat = kotakLebar();
    private final JTextField tNamaDPJP = kotakLebar();
    private final JTextField tNamaVerifikator = kotakLebar();
    private String kodePerawatPPJP = "";
    private String kodeDPJP = "";
    private String kodeVerifikator = "";
    private final DlgCariDokter dlgCariDPJP;
    private final DlgCariPegawai dlgCariPerawat;
    private final DlgCariPegawai dlgCariVerifikator;

    public RMCPBBLR(Frame parent, boolean modal) {
        super(parent, "Clinical Pathway - Bayi Berat Lahir Rendah (BBLR)", modal);
        dlgSoap = new RMCariSOAPRawatIni(parent);
        dlgCariDPJP = new DlgCariDokter(parent, false);
        dlgCariPerawat = new DlgCariPegawai(parent, false);
        dlgCariVerifikator = new DlgCariPegawai(parent, false);
        dlgCariDPJP.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                if (dlgCariDPJP.getTable().getSelectedRow() != -1) {
                    kodeDPJP = dlgCariDPJP.getTable().getValueAt(dlgCariDPJP.getTable().getSelectedRow(), 0).toString();
                    tNamaDPJP.setText(dlgCariDPJP.getTable().getValueAt(dlgCariDPJP.getTable().getSelectedRow(), 1).toString());
                    kotor = true;
                }
            }
        });
        dlgCariPerawat.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                if (dlgCariPerawat.getTable().getSelectedRow() != -1) {
                    kodePerawatPPJP = dlgCariPerawat.getTable().getValueAt(dlgCariPerawat.getTable().getSelectedRow(), 0).toString();
                    tNamaPerawat.setText(dlgCariPerawat.getTable().getValueAt(dlgCariPerawat.getTable().getSelectedRow(), 1).toString());
                    kotor = true;
                }
            }
        });
        dlgCariVerifikator.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                if (dlgCariVerifikator.getTable().getSelectedRow() != -1) {
                    kodeVerifikator = dlgCariVerifikator.getTable().getValueAt(dlgCariVerifikator.getTable().getSelectedRow(), 0).toString();
                    tNamaVerifikator.setText(dlgCariVerifikator.getTable().getValueAt(dlgCariVerifikator.getTable().getSelectedRow(), 1).toString());
                    kotor = true;
                }
            }
        });
        tNamaPerawat.setEditable(false);
        tNamaDPJP.setEditable(false);
        tNamaVerifikator.setEditable(false);
        tNamaPerawat.setBackground(BG_SOFT);
        tNamaDPJP.setBackground(BG_SOFT);
        tNamaVerifikator.setBackground(BG_SOFT);
        for (String[][] daftar : KATALOG.values()) {
            for (String[] item : daftar) {
                nilaiGrid.put(item[0], new LinkedHashMap<>());
            }
        }
        muatDaftarKamar();
        inisialisasiUI();
        setSize(1280, 820);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (kotor) {
                    int pilih = JOptionPane.showConfirmDialog(RMCPBBLR.this,
                            "Ada perubahan yang belum disimpan. Tetap tutup?", "Konfirmasi",
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (pilih != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                dispose();
            }
        });
    }

    // ================= UI =================

    private void inisialisasiUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_SOFT);

        add(buatHeader(), BorderLayout.NORTH);

        JTabbedPane tab = new JTabbedPane();
        tab.setUI(new TabRoundedRawatInapUI());
        tab.setFont(FONT_BOLD);
        tab.setBackground(BG_SOFT);
        tab.addTab("Catatan Harian", buatTabHarian());
        tab.addTab("Penutup & Tanda Tangan", buatTabPenutup());
        JPanel bungkusTab = new JPanel(new BorderLayout());
        bungkusTab.setBackground(BG_SOFT);
        bungkusTab.setBorder(new EmptyBorder(8, 12, 0, 12));
        bungkusTab.add(tab, BorderLayout.CENTER);
        add(bungkusTab, BorderLayout.CENTER);

        add(buatToolbarBawah(), BorderLayout.SOUTH);
    }

    private JPanel buatHeader() {
        JPanel kartu = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new java.awt.GradientPaint(0, 0, BIRU, 0, getHeight(), BIRU_GELAP));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        kartu.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel label = new JLabel("CLINICAL PATHWAY");
        label.setFont(new Font(NAMA_FONT, Font.BOLD, 12));
        label.setForeground(new Color(200, 222, 252));
        JLabel judul = new JLabel("BBLR (Bayi Berat Lahir Rendah)");
        judul.setFont(new Font(NAMA_FONT, Font.BOLD, 23));
        judul.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Rumah Sakit Khusus Ibu dan Anak Mahkota Bunda");
        sub.setFont(FONT_LABEL);
        sub.setForeground(new Color(222, 234, 252));
        JPanel teks = new JPanel();
        teks.setOpaque(false);
        teks.setLayout(new GridLayout(3, 1, 0, 2));
        teks.add(label);
        teks.add(judul);
        teks.add(sub);
        kartu.add(teks, BorderLayout.WEST);

        JPanel identitas = new JPanel(new GridBagLayout());
        identitas.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(1, 10, 1, 2);
        c.anchor = GridBagConstraints.WEST;
        lblJudulPasien.setFont(new Font(NAMA_FONT, Font.BOLD, 16));
        lblJudulPasien.setForeground(Color.WHITE);
        putihkan(lblNoRM); putihkan(lblUmurJk);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; identitas.add(lblJudulPasien, c);
        c.gridwidth = 1;
        c.gridy = 1; c.gridx = 0; identitas.add(labelKecilPutih("No. RM"), c); c.gridx = 1; identitas.add(lblNoRM, c);
        c.gridy = 2; c.gridx = 0; identitas.add(labelKecilPutih("Umur / JK"), c); c.gridx = 1; identitas.add(lblUmurJk, c);
        cbRuang.setFont(FONT_LABEL);
        cbRuang.setEditable(true);
        cbRuang.setPreferredSize(new Dimension(230, 26));
        c.gridy = 3; c.gridx = 0; identitas.add(labelKecilPutih("Ruang"), c); c.gridx = 1; identitas.add(cbRuang, c);

        tMasuk.setFont(FONT_LABEL);
        tMasuk.setDisplayFormat("dd-MM-yyyy");
        tMasuk.setPreferredSize(new Dimension(110, 26));
        JPanel barisMasuk = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        barisMasuk.setOpaque(false);
        barisMasuk.add(tMasuk);
        barisMasuk.add(labelKecilPutih("Hari ke-"));
        barisMasuk.add(tHariRawatKe);
        c.gridy = 4; c.gridx = 0; identitas.add(labelKecilPutih("Masuk"), c); c.gridx = 1; identitas.add(barisMasuk, c);
        kartu.add(identitas, BorderLayout.CENTER);

        JButton btnSoap = tombolPutih("Lihat SOAP");
        btnSoap.addActionListener(e -> dlgSoap.tampilkan(noRawat, this));
        JPanel kananAtas = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        kananAtas.setOpaque(false);
        kananAtas.add(btnSoap);
        kartu.add(kananAtas, BorderLayout.EAST);
        return kartu;
    }

    private JPanel buatTabHarian() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel info = new JPanel(new BorderLayout());
        info.setOpaque(false);
        JPanel kartuRingkas = kartuBerLabel("Diagnosis Utama", "Bayi Berat Lahir Rendah (tetap)");
        info.add(kartuRingkas, BorderLayout.WEST);

        JButton btnTambahHari = tombolHijau("+ Tambah Hari");
        btnTambahHari.addActionListener(e -> tambahHari());
        lblInfoHari.setFont(FONT_LABEL);
        lblInfoHari.setForeground(TEKS);
        JPanel kananInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        kananInfo.setOpaque(false);
        kananInfo.add(lblInfoHari);
        kananInfo.add(btnTambahHari);
        info.add(kananInfo, BorderLayout.EAST);
        panel.add(info, BorderLayout.NORTH);

        panelRowHeader.setLayout(new GridLayout(0, 1));
        panelRowHeader.setBackground(Color.WHITE);
        panelKolomHeader.setBackground(BG_SOFT);
        panelGrid.setBackground(Color.WHITE);

        scrollGrid.setBorder(BorderFactory.createLineBorder(GARIS));
        scrollGrid.setViewportView(panelGrid);
        scrollGrid.setRowHeaderView(panelRowHeader);
        scrollGrid.setColumnHeaderView(panelKolomHeader);
        JLabel sudut = new JLabel("  Kegiatan");
        sudut.setFont(FONT_BOLD);
        sudut.setForeground(TEKS);
        sudut.setBackground(BG_SOFT);
        sudut.setOpaque(true);
        sudut.setPreferredSize(new Dimension(LEBAR_LABEL, TINGGI_BARIS));
        scrollGrid.setCorner(JScrollPane.UPPER_LEFT_CORNER, sudut);
        scrollGrid.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scrollGrid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buatTabPenutup() {
        JPanel panel = new JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        panel.add(subJudul("Varians & Biaya"));
        JPanel baris1 = new JPanel(new GridBagLayout());
        baris1.setOpaque(false);
        baris1.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints c = gbc();
        c.gridx = 0; c.gridy = 0; baris1.add(labelField("Varians Pelayanan"), c);
        c.gridx = 1; c.gridy = 0; c.gridwidth = 3;
        tVarians.setLineWrap(true);
        tVarians.setWrapStyleWord(true);
        tVarians.setFont(FONT_LABEL);
        JScrollPane scrollVarians = new JScrollPane(tVarians);
        scrollVarians.setPreferredSize(new Dimension(520, 70));
        scrollVarians.setBorder(BorderFactory.createLineBorder(GARIS));
        baris1.add(scrollVarians, c);
        c.gridwidth = 1;
        c.gridx = 0; c.gridy = 1; baris1.add(labelField("Jumlah Biaya (Rp)"), c);
        c.gridx = 1; c.gridy = 1; baris1.add(tJumlahBiaya, c);
        panel.add(baris1);

        panel.add(javax.swing.Box.createVerticalStrut(14));
        panel.add(subJudul("Diagnosis Akhir & Tindakan"));
        String[] baris = {"Utama", "Penyerta", "Komplikasi"};
        JPanel tabelAkhir = new JPanel(new GridBagLayout());
        tabelAkhir.setOpaque(false);
        tabelAkhir.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints c2 = gbc();
        c2.gridx = 1; c2.gridy = 0; tabelAkhir.add(labelField("Diagnosis Akhir"), c2);
        c2.gridx = 2; tabelAkhir.add(labelField("Kode ICD-10"), c2);
        c2.gridx = 3; tabelAkhir.add(labelField("Jenis Tindakan"), c2);
        c2.gridx = 4; tabelAkhir.add(labelField("Kode ICD-9-CM"), c2);
        for (int i = 0; i < 3; i++) {
            c2.gridy = i + 1;
            c2.gridx = 0; tabelAkhir.add(labelField(baris[i]), c2);
            c2.gridx = 1; tabelAkhir.add(tDiagAkhir[i], c2);
            c2.gridx = 2; tabelAkhir.add(tIcd10Akhir[i], c2);
            c2.gridx = 3; tabelAkhir.add(tTindakanAkhir[i], c2);
            c2.gridx = 4; tabelAkhir.add(tIcd9Akhir[i], c2);
        }
        panel.add(tabelAkhir);

        panel.add(javax.swing.Box.createVerticalStrut(14));
        panel.add(subJudul("Penanggung Jawab"));
        JPanel ttd = new JPanel(new GridBagLayout());
        ttd.setOpaque(false);
        ttd.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints c3 = gbc();
        JButton btnPilihPerawat = tombolBiruKecil("Pilih...");
        btnPilihPerawat.addActionListener(e -> bukaPickerPetugas(dlgCariPerawat));
        JButton btnPilihDPJP = tombolBiruKecil("Pilih...");
        btnPilihDPJP.addActionListener(e -> bukaPickerPetugas(dlgCariDPJP));
        JButton btnPilihVerifikator = tombolBiruKecil("Pilih...");
        btnPilihVerifikator.addActionListener(e -> bukaPickerPetugas(dlgCariVerifikator));

        c3.gridx = 0; c3.gridy = 0; ttd.add(labelField("Perawat (PPJP)"), c3);
        c3.gridx = 1; ttd.add(tNamaPerawat, c3);
        c3.gridx = 2; ttd.add(btnPilihPerawat, c3);
        c3.gridx = 0; c3.gridy = 1; ttd.add(labelField("DPJP"), c3);
        c3.gridx = 1; ttd.add(tNamaDPJP, c3);
        c3.gridx = 2; ttd.add(btnPilihDPJP, c3);
        c3.gridx = 0; c3.gridy = 2; ttd.add(labelField("Verifikator"), c3);
        c3.gridx = 1; ttd.add(tNamaVerifikator, c3);
        c3.gridx = 2; ttd.add(btnPilihVerifikator, c3);
        panel.add(ttd);

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        JPanel bungkus = new JPanel(new BorderLayout());
        bungkus.add(scroll, BorderLayout.CENTER);
        return bungkus;
    }

    private JPanel buatToolbarBawah() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bar.setBackground(BG_SOFT);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, GARIS));
        JButton btnTutup = tombolAbu("Tutup");
        btnTutup.addActionListener(e -> {
            WindowEvent we = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
            dispatchEvent(we);
        });
        JButton btnSimpan = tombolHijau("Simpan");
        btnSimpan.addActionListener(e -> simpan());
        bar.add(btnTutup);
        bar.add(btnSimpan);
        return bar;
    }

    // ================= grid harian =================

    private void tambahHari() {
        if (daftarHari.size() >= MAKS_HARI) {
            JOptionPane.showMessageDialog(this, "Maksimal " + MAKS_HARI + " kolom Hari Rawat.");
            return;
        }
        int hariBaru = daftarHari.isEmpty() ? 1 : daftarHari.get(daftarHari.size() - 1) + 1;
        daftarHari.add(hariBaru);
        kotor = true;
        susunUlangGrid();
    }

    private void susunUlangGrid() {
        lblInfoHari.setText("Menampilkan " + daftarHari.size() + " kolom Hari Rawat");

        panelRowHeader.removeAll();
        panelKolomHeader.removeAll();
        panelGrid.removeAll();

        panelKolomHeader.setLayout(new GridLayout(1, daftarHari.size()));
        for (int hari : daftarHari) {
            JLabel h = new JLabel("Hari ke-" + hari, SwingConstants.CENTER);
            h.setToolTipText("Hari Rawat ke-" + hari);
            h.setFont(FONT_BOLD);
            h.setForeground(TEKS);
            h.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, GARIS));
            h.setPreferredSize(new Dimension(LEBAR_HARI, TINGGI_BARIS));
            panelKolomHeader.add(h);
        }

        panelGrid.setLayout(new GridLayout(0, 1));

        for (Map.Entry<String, String[][]> seksi : KATALOG.entrySet()) {
            tambahBarisSeksi(seksi.getKey());
            for (String[] item : seksi.getValue()) {
                boolean teks = item.length > 2 && "T".equals(item[2]);
                tambahBarisItem(item[0], item[1], teks);
            }
        }

        panelRowHeader.setPreferredSize(new Dimension(LEBAR_LABEL, panelRowHeader.getComponentCount() * TINGGI_BARIS));
        panelGrid.setPreferredSize(new Dimension(LEBAR_HARI * daftarHari.size(), panelRowHeader.getComponentCount() * TINGGI_BARIS));
        panelKolomHeader.setPreferredSize(new Dimension(LEBAR_HARI * daftarHari.size(), TINGGI_BARIS));

        panelRowHeader.revalidate(); panelRowHeader.repaint();
        panelKolomHeader.revalidate(); panelKolomHeader.repaint();
        panelGrid.revalidate(); panelGrid.repaint();
        scrollGrid.revalidate(); scrollGrid.repaint();
    }

    private void tambahBarisSeksi(String namaSeksi) {
        JLabel label = new JLabel("  " + namaSeksi);
        label.setFont(FONT_BOLD);
        label.setForeground(BIRU_GELAP);
        label.setOpaque(true);
        label.setBackground(new Color(229, 240, 254));
        label.setPreferredSize(new Dimension(LEBAR_LABEL, TINGGI_BARIS));
        panelRowHeader.add(label);

        JPanel baris = new JPanel(new GridLayout(1, Math.max(1, daftarHari.size())));
        baris.setBackground(new Color(229, 240, 254));
        for (int i = 0; i < daftarHari.size(); i++) {
            baris.add(new JPanel() {{ setOpaque(false); }});
        }
        panelGrid.add(baris);
    }

    /** teks=true -> sel isian angka/teks bebas per hari (mis. Nutrisi); teks=false -> toggle pil +/-. */
    private void tambahBarisItem(String kode, String label, boolean teks) {
        JLabel l = new JLabel("  " + label);
        l.setFont(FONT_LABEL);
        l.setForeground(TEKS);
        l.setOpaque(true);
        l.setBackground(Color.WHITE);
        l.setPreferredSize(new Dimension(LEBAR_LABEL, TINGGI_BARIS));
        panelRowHeader.add(l);

        JPanel baris = new JPanel(new GridLayout(1, Math.max(1, daftarHari.size())));
        baris.setBackground(Color.WHITE);
        Map<Integer, String> nilaiItem = nilaiGrid.get(kode);
        for (int hari : daftarHari) {
            String awal = nilaiItem.get(hari);
            JComponent sel;
            if (teks) {
                JTextField tf = new JTextField(awal == null ? "" : awal);
                tf.setFont(FONT_LABEL);
                tf.setHorizontalAlignment(JTextField.CENTER);
                tf.setPreferredSize(new Dimension(LEBAR_HARI - 14, TINGGI_BARIS - 7));
                tf.setBorder(BorderFactory.createLineBorder(GARIS));
                tf.getDocument().addDocumentListener(new DocumentListener() {
                    private void simpanNilai() { nilaiItem.put(hari, tf.getText()); kotor = true; }
                    @Override public void insertUpdate(DocumentEvent e) { simpanNilai(); }
                    @Override public void removeUpdate(DocumentEvent e) { simpanNilai(); }
                    @Override public void changedUpdate(DocumentEvent e) { simpanNilai(); }
                });
                sel = tf;
            } else {
                sel = new TogglePlusMinus(awal, (nilaiBaru) -> {
                    nilaiItem.put(hari, nilaiBaru);
                    kotor = true;
                });
            }
            JPanel bungkus = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            bungkus.setOpaque(false);
            bungkus.add(sel);
            baris.add(bungkus);
        }
        panelGrid.add(baris);
    }

    // ================= data: muat =================

    public void setNoRawat(String noRawat) {
        this.noRawat = noRawat;
        muatIdentitas();
        muatCP();
        susunUlangGrid();
        kotor = false;
    }

    /** Isi dropdown Ruang dari master kamar+bangsal (dipanggil sekali saat form dibuat). */
    private void muatDaftarKamar() {
        String sql = "select concat(kamar.kd_kamar,' - ',bangsal.nm_bangsal,' (',kamar.kelas,')') as label "
                + "from kamar inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "
                + "order by bangsal.nm_bangsal, kamar.kd_kamar";
        try (PreparedStatement ps = koneksi.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cbRuang.addItem(rs.getString("label"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat daftar kamar: " + e.getMessage());
        }
    }

    private void muatIdentitas() {
        String sql = "select reg_periksa.no_rkm_medis,pasien.nm_pasien,"
                + "if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,"
                + "reg_periksa.tgl_registrasi,kamar.kd_kamar,bangsal.nm_bangsal,kamar.kelas,kamar_inap.diagnosa_awal,"
                + "kamar_inap.tgl_masuk,kamar_inap.jam_masuk "
                + "from reg_periksa "
                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "left join kamar_inap on kamar_inap.no_rawat=reg_periksa.no_rawat "
                + "left join kamar on kamar.kd_kamar=kamar_inap.kd_kamar "
                + "left join bangsal on bangsal.kd_bangsal=kamar.kd_bangsal "
                + "where reg_periksa.no_rawat=? order by kamar_inap.tgl_masuk desc,kamar_inap.jam_masuk desc limit 1";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblJudulPasien.setText(rs.getString("nm_pasien"));
                    lblNoRM.setText(rs.getString("no_rkm_medis"));
                    lblUmurJk.setText(hitungUmur(rs.getDate("tgl_lahir")) + " / " + rs.getString("jk"));
                    String kdKamar = rs.getString("kd_kamar");
                    String ruang = rs.getString("nm_bangsal");
                    String kelas = rs.getString("kelas");
                    if (ruang != null) {
                        cbRuang.setSelectedItem(kdKamar + " - " + ruang + " (" + kelas + ")");
                    }
                    Date masuk = rs.getDate("tgl_masuk");
                    tglMasuk = masuk;
                    tMasuk.setDate(masuk);
                    tHariRawatKe.setText(String.valueOf(hitungHariRawatSekarang()));
                    if (tDiagnosisAwal.getText().isEmpty() && rs.getString("diagnosa_awal") != null) {
                        tDiagnosisAwal.setText(rs.getString("diagnosa_awal"));
                    }
                } else {
                    lblJudulPasien.setText("(data rawatan tidak ditemukan)");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat identitas pasien: " + e.getMessage());
        }
    }

    private void muatCP() {
        for (Map<Integer, String> m : nilaiGrid.values()) {
            m.clear();
        }
        daftarHari.clear();
        int jumlahHariTampil = 3;
        tRencanaRawat.setText("3 hari");
        tNamaPerawat.setText("");
        tNamaDPJP.setText("");
        tNamaVerifikator.setText("");
        kodePerawatPPJP = "";
        kodeDPJP = "";
        kodeVerifikator = "";

        try (PreparedStatement ps = koneksi.prepareStatement("select * from cp_bblr where no_rawat=?")) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getString("diagnosis_awal") != null && !rs.getString("diagnosis_awal").isEmpty()) {
                        tDiagnosisAwal.setText(rs.getString("diagnosis_awal"));
                    }
                    if (rs.getString("ruang_rawat") != null && !rs.getString("ruang_rawat").isEmpty()) {
                        cbRuang.setSelectedItem(rs.getString("ruang_rawat"));
                    }
                    if (rs.getString("tgl_masuk_tampil") != null && !rs.getString("tgl_masuk_tampil").isEmpty()) {
                        try {
                            tMasuk.setDate(new SimpleDateFormat("dd-MM-yyyy").parse(rs.getString("tgl_masuk_tampil")));
                        } catch (java.text.ParseException ignored) {
                            // biarkan nilai hasil tarik otomatis dari muatIdentitas() kalau formatnya tak terbaca
                        }
                    }
                    if (rs.getString("hari_rawat_ke") != null && !rs.getString("hari_rawat_ke").isEmpty()) {
                        tHariRawatKe.setText(rs.getString("hari_rawat_ke"));
                    }
                    tKodeICD10.setText(ns(rs.getString("kode_icd10")));
                    tRencanaRawat.setText(ns(rs.getString("rencana_rawat")));
                    tBB.setText(ns(rs.getString("berat_badan")));
                    tTB.setText(ns(rs.getString("tinggi_badan")));
                    tVarians.setText(ns(rs.getString("varians")));
                    tJumlahBiaya.setText(ns(rs.getString("jumlah_biaya")));
                    tDiagAkhir[0].setText(ns(rs.getString("diag_akhir_utama")));
                    tIcd10Akhir[0].setText(ns(rs.getString("icd10_akhir_utama")));
                    tTindakanAkhir[0].setText(ns(rs.getString("tindakan_utama")));
                    tIcd9Akhir[0].setText(ns(rs.getString("icd9_utama")));
                    tDiagAkhir[1].setText(ns(rs.getString("diag_akhir_penyerta")));
                    tIcd10Akhir[1].setText(ns(rs.getString("icd10_akhir_penyerta")));
                    tTindakanAkhir[1].setText(ns(rs.getString("tindakan_penyerta")));
                    tIcd9Akhir[1].setText(ns(rs.getString("icd9_penyerta")));
                    tDiagAkhir[2].setText(ns(rs.getString("diag_akhir_komplikasi")));
                    tIcd10Akhir[2].setText(ns(rs.getString("icd10_akhir_komplikasi")));
                    tTindakanAkhir[2].setText(ns(rs.getString("tindakan_komplikasi")));
                    tIcd9Akhir[2].setText(ns(rs.getString("icd9_komplikasi")));
                    tNamaDPJP.setText(ns(rs.getString("nama_dpjp")));
                    tNamaVerifikator.setText(ns(rs.getString("nama_verifikator")));
                    tNamaPerawat.setText(ns(rs.getString("nama_perawat_ppjp")));
                    kodePerawatPPJP = ns(rs.getString("kode_perawat_ppjp"));
                    kodeDPJP = ns(rs.getString("kode_dpjp"));
                    kodeVerifikator = ns(rs.getString("kode_verifikator"));
                    jumlahHariTampil = Math.max(3, rs.getInt("jumlah_hari_tampil"));
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data CP: " + e.getMessage());
        }

        try (PreparedStatement ps = koneksi.prepareStatement(
                "select item_kode,hari_ke,nilai from cp_bblr_detail where no_rawat=?")) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String kode = rs.getString("item_kode");
                    int hari = rs.getInt("hari_ke");
                    String nilai = rs.getString("nilai");
                    Map<Integer, String> m = nilaiGrid.get(kode);
                    if (m != null && nilai != null && !nilai.isEmpty()) {
                        m.put(hari, nilai);
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat grid harian CP: " + e.getMessage());
        }

        int hariMaks = jumlahHariTampil;
        for (Map<Integer, String> m : nilaiGrid.values()) {
            for (Integer h : m.keySet()) {
                if (h > hariMaks) { hariMaks = h; }
            }
        }
        // ikut sesuaikan dgn "Hari ke-" di header (otomatis dari tgl_masuk, atau override manual
        // petugas di field-nya) spy kolomnya otomatis nambah tanpa harus klik "+ Tambah Hari" manual.
        int hariDariHeader;
        try {
            hariDariHeader = Integer.parseInt(tHariRawatKe.getText().trim());
        } catch (NumberFormatException e) {
            hariDariHeader = hitungHariRawatSekarang();
        }
        hariMaks = Math.max(hariMaks, Math.min(MAKS_HARI, hariDariHeader));
        for (int i = 1; i <= hariMaks; i++) {
            daftarHari.add(i);
        }
    }

    // ================= data: simpan =================

    private void simpan() {
        if (noRawat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No.Rawat belum ditentukan.");
            return;
        }
        String sqlHeader = "insert into cp_bblr "
                + "(no_rawat,diagnosis_awal,kode_icd10,rencana_rawat,berat_badan,tinggi_badan,"
                + "ruang_rawat,tgl_masuk_tampil,hari_rawat_ke,jumlah_hari_tampil,"
                + "varians,jumlah_biaya,diag_akhir_utama,icd10_akhir_utama,tindakan_utama,icd9_utama,"
                + "diag_akhir_penyerta,icd10_akhir_penyerta,tindakan_penyerta,icd9_penyerta,"
                + "diag_akhir_komplikasi,icd10_akhir_komplikasi,tindakan_komplikasi,icd9_komplikasi,"
                + "nama_perawat_ppjp,kode_perawat_ppjp,nama_dpjp,kode_dpjp,nama_verifikator,kode_verifikator,"
                + "created_by,created_at,updated_by,updated_at) "
                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,now(),?,now()) "
                + "on duplicate key update diagnosis_awal=values(diagnosis_awal),kode_icd10=values(kode_icd10),"
                + "rencana_rawat=values(rencana_rawat),berat_badan=values(berat_badan),tinggi_badan=values(tinggi_badan),"
                + "ruang_rawat=values(ruang_rawat),tgl_masuk_tampil=values(tgl_masuk_tampil),hari_rawat_ke=values(hari_rawat_ke),"
                + "jumlah_hari_tampil=values(jumlah_hari_tampil),varians=values(varians),jumlah_biaya=values(jumlah_biaya),"
                + "diag_akhir_utama=values(diag_akhir_utama),icd10_akhir_utama=values(icd10_akhir_utama),"
                + "tindakan_utama=values(tindakan_utama),icd9_utama=values(icd9_utama),"
                + "diag_akhir_penyerta=values(diag_akhir_penyerta),icd10_akhir_penyerta=values(icd10_akhir_penyerta),"
                + "tindakan_penyerta=values(tindakan_penyerta),icd9_penyerta=values(icd9_penyerta),"
                + "diag_akhir_komplikasi=values(diag_akhir_komplikasi),icd10_akhir_komplikasi=values(icd10_akhir_komplikasi),"
                + "tindakan_komplikasi=values(tindakan_komplikasi),icd9_komplikasi=values(icd9_komplikasi),"
                + "nama_perawat_ppjp=values(nama_perawat_ppjp),kode_perawat_ppjp=values(kode_perawat_ppjp),"
                + "nama_dpjp=values(nama_dpjp),kode_dpjp=values(kode_dpjp),"
                + "nama_verifikator=values(nama_verifikator),kode_verifikator=values(kode_verifikator),"
                + "updated_by=values(updated_by),updated_at=now()";
        try {
            koneksi.setAutoCommit(false);
            try (PreparedStatement ps = koneksi.prepareStatement(sqlHeader)) {
                int i = 1;
                ps.setString(i++, noRawat);
                ps.setString(i++, tDiagnosisAwal.getText());
                ps.setString(i++, tKodeICD10.getText());
                ps.setString(i++, tRencanaRawat.getText());
                ps.setString(i++, tBB.getText());
                ps.setString(i++, tTB.getText());
                ps.setString(i++, cbRuang.getSelectedItem() == null ? "" : cbRuang.getSelectedItem().toString());
                ps.setString(i++, tMasuk.getDate() == null ? "" : new SimpleDateFormat("dd-MM-yyyy").format(tMasuk.getDate()));
                ps.setString(i++, tHariRawatKe.getText());
                ps.setInt(i++, daftarHari.size());
                ps.setString(i++, tVarians.getText());
                ps.setString(i++, tJumlahBiaya.getText());
                ps.setString(i++, tDiagAkhir[0].getText());
                ps.setString(i++, tIcd10Akhir[0].getText());
                ps.setString(i++, tTindakanAkhir[0].getText());
                ps.setString(i++, tIcd9Akhir[0].getText());
                ps.setString(i++, tDiagAkhir[1].getText());
                ps.setString(i++, tIcd10Akhir[1].getText());
                ps.setString(i++, tTindakanAkhir[1].getText());
                ps.setString(i++, tIcd9Akhir[1].getText());
                ps.setString(i++, tDiagAkhir[2].getText());
                ps.setString(i++, tIcd10Akhir[2].getText());
                ps.setString(i++, tTindakanAkhir[2].getText());
                ps.setString(i++, tIcd9Akhir[2].getText());
                ps.setString(i++, tNamaPerawat.getText());
                ps.setString(i++, kodePerawatPPJP);
                ps.setString(i++, tNamaDPJP.getText());
                ps.setString(i++, kodeDPJP);
                ps.setString(i++, tNamaVerifikator.getText());
                ps.setString(i++, kodeVerifikator);
                ps.setString(i++, akses.getnamauser());
                ps.setString(i++, akses.getnamauser());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = koneksi.prepareStatement("delete from cp_bblr_detail where no_rawat=?")) {
                ps.setString(1, noRawat);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "insert into cp_bblr_detail (no_rawat,item_kode,hari_ke,nilai) values (?,?,?,?)")) {
                for (Map.Entry<String, Map<Integer, String>> item : nilaiGrid.entrySet()) {
                    for (Map.Entry<Integer, String> sel : item.getValue().entrySet()) {
                        if (sel.getValue() == null || sel.getValue().isEmpty()) { continue; }
                        ps.setString(1, noRawat);
                        ps.setString(2, item.getKey());
                        ps.setInt(3, sel.getKey());
                        ps.setString(4, sel.getValue());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }
            koneksi.commit();
            kotor = false;
            Toast.sukses(this, "Form CP BBLR berhasil disimpan.");
        } catch (Exception e) {
            try { koneksi.rollback(); } catch (Exception ignored) { }
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + e.getMessage());
        } finally {
            try { koneksi.setAutoCommit(true); } catch (Exception ignored) { }
        }
    }

    // ================= util kecil =================

    private static String ns(String s) { return s == null ? "" : s; }

    private static String hitungUmur(Date tglLahir) {
        if (tglLahir == null) { return "-"; }
        java.time.LocalDate lahir = tglLahir.toLocalDate();
        java.time.Period p = java.time.Period.between(lahir, java.time.LocalDate.now());
        if (p.getYears() > 0) { return p.getYears() + " th " + p.getMonths() + " bln"; }
        if (p.getMonths() > 0) { return p.getMonths() + " bln " + p.getDays() + " hr"; }
        return p.getDays() + " hari";
    }

    /** Hari rawat ke-berapa SEKARANG, dihitung dari tgl_masuk s/d hari ini (hari masuk = hari ke-1). */
    private int hitungHariRawatSekarang() {
        if (tglMasuk == null) { return 3; }
        long selisih = java.time.temporal.ChronoUnit.DAYS.between(tglMasuk.toLocalDate(), java.time.LocalDate.now());
        return Math.max(3, (int) selisih + 1);
    }

    private static JLabel labelNilai() {
        JLabel l = new JLabel("-");
        l.setFont(FONT_BOLD);
        return l;
    }

    private static void putihkan(JLabel l) { l.setForeground(Color.WHITE); }

    private static JLabel labelKecilPutih(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(FONT_LABEL);
        l.setForeground(new Color(210, 226, 252));
        return l;
    }

    /** Kotak isian di atas kartu header biru (Ruang/Masuk/Hari Rawat ke-) -- putih spy kontras & jelas bisa diedit. */
    private static JTextField kotakHeader(int lebar) {
        JTextField t = new JTextField();
        t.setFont(FONT_LABEL);
        t.setForeground(TEKS);
        t.setPreferredSize(new Dimension(lebar, 26));
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE), new EmptyBorder(2, 6, 2, 6)));
        return t;
    }

    private static JTextField kotakKecil() {
        JTextField t = new JTextField();
        t.setFont(FONT_LABEL);
        t.setPreferredSize(new Dimension(120, 28));
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GARIS), new EmptyBorder(3, 6, 3, 6)));
        return t;
    }

    private static JTextField kotakLebar() {
        JTextField t = kotakKecil();
        t.setPreferredSize(new Dimension(270, 28));
        return t;
    }

    private static JLabel labelField(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(FONT_LABEL);
        l.setForeground(TEKS);
        l.setPreferredSize(new Dimension(160, 26));
        return l;
    }

    private static JLabel subJudul(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font(NAMA_FONT, Font.BOLD, 15));
        l.setForeground(BIRU_GELAP);
        l.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BIRU));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static GridBagConstraints gbc() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 10);
        c.anchor = GridBagConstraints.WEST;
        return c;
    }

    private JPanel kartuBerLabel(String judul, String nilai) {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(2, 1));
        p.setBackground(new Color(229, 240, 254));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GARIS), new EmptyBorder(6, 12, 6, 12)));
        JLabel l1 = new JLabel(judul);
        l1.setFont(FONT_LABEL);
        l1.setForeground(TEKS);
        JLabel l2 = new JLabel(nilai);
        l2.setFont(new Font(NAMA_FONT, Font.BOLD, 15));
        l2.setForeground(BIRU_GELAP);
        p.add(l1);
        p.add(l2);
        return p;
    }

    private static JButton tombolPutih(String teks) {
        JButton b = tombolDasar(teks, Color.WHITE, BIRU_GELAP);
        return b;
    }
    private static JButton tombolHijau(String teks) { return tombolDasar(teks, HIJAU, Color.WHITE); }
    private static JButton tombolBiruKecil(String teks) {
        JButton b = tombolDasar(teks, BIRU, Color.WHITE);
        b.setBorder(new EmptyBorder(4, 12, 4, 12));
        return b;
    }

    /** Buka jendela pilih petugas (DlgCariDokter utk DPJP / DlgCariPegawai utk perawat & verifikator). */
    private void bukaPickerPetugas(java.awt.Dialog dlg) {
        dlg.setSize(700, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }
    private static JButton tombolAbu(String teks) { return tombolDasar(teks, new Color(220, 226, 233), TEKS); }

    private static JButton tombolDasar(String teks, Color latar, Color depan) {
        JButton b = new JButton(teks) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setBackground(latar);
        b.setForeground(depan);
        b.setFont(FONT_BOLD);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(7, 16, 7, 16));
        return b;
    }

    /** Toggle 3-state (belum diisi / + / -) ala pil bulat, dipakai di tiap sel grid harian (selain Nutrisi). */
    private static final class TogglePlusMinus extends JButton {
        private String nilai;
        TogglePlusMinus(String awal, java.util.function.Consumer<String> onUbah) {
            this.nilai = (awal == null) ? "" : awal;
            setPreferredSize(new Dimension(LEBAR_HARI - 14, TINGGI_BARIS - 7));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFont(FONT_BOLD);
            addActionListener((ActionListener) e -> {
                if (nilai.isEmpty()) { nilai = "+"; }
                else if ("+".equals(nilai)) { nilai = "-"; }
                else { nilai = ""; }
                onUbah.accept(nilai.isEmpty() ? null : nilai);
                repaint();
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            Color isi, tepi, teks;
            String s;
            if (nilai.isEmpty()) { isi = new Color(237, 241, 246); tepi = GARIS; teks = new Color(150, 162, 176); s = ""; }
            else if ("+".equals(nilai)) { isi = new Color(224, 242, 227); tepi = HIJAU; teks = HIJAU; s = "+"; }
            else { isi = new Color(252, 228, 228); tepi = MERAH; teks = MERAH; s = "-"; }
            g2.setColor(isi);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 9, 9);
            g2.setColor(tepi);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 9, 9);
            g2.setColor(teks);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(s, (w - fm.stringWidth(s)) / 2, (h + fm.getAscent()) / 2 - 2);
            g2.dispose();
        }
    }

}
