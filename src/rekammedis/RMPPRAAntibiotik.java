package rekammedis;

import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Input pemakaian antibiotik per pasien rawat inap, untuk perhitungan PPRA/DDD
 * (Defined Daily Dose). Dibuka dari menu sidebar "FormMenu" di DlgRawatInap.
 *
 * Aturan kunci (sudah dikonfirmasi ke dokter, lihat memory migrasi-ppra-ddd-antimikroba):
 * tabel log tanggal pemberian itu SATU BARIS PER HARI, bukan per kali pemberian --
 * walau antibiotik diberikan 3x/hari (tiap 8 jam), hari itu tetap dihitung 1 hari.
 * "Lama Terapi" = COUNT baris tanggal yang terisi untuk antibiotik tsb (bukan rumus
 * selisih tanggal terakhir-pertama), supaya kalau terapi terputus/bolong, hari yang
 * kosong tidak ikut terhitung.
 *
 * "Dosis per-hari" (mg) dihitung otomatis dari dosis per sekali pemberian x (24/interval
 * jam) -- jadi field satuan dosis tidak perlu dipilih, selalu mg. Nama Antibiotik wajib
 * pilih ketat dari master ppra_referensi_ddd (sudah ada, dipakai ulang) supaya lookup
 * Kode ATC & Standar DDD selalu ketemu. Kolom identitas pasien SENGAJA tidak disimpan di
 * tabel baru ini -- cukup no_rawat, identitas ditarik live tiap form dibuka.
 *
 * KSM (Kelompok Staf Medis) dicatat SEKALI per rawatan (bukan per baris antibiotik),
 * disimpan di tabel ppra_rawatan_ksm (no_rawat sbg PRIMARY KEY), auto-tersimpan tiap
 * dropdownnya diganti.
 *
 * "Lama Rawat Inap" tidak ada di form ini -- nanti ditarik dari menu Hitung ALOS
 * setelah menu itu direvisi (lihat memory terkait), jadi field itu DITUNDA dulu.
 *
 * Tampilan mengikuti gaya "kartu putih melengkung di atas latar abu-abu" ala
 * AdimeGiziView (modernisasi form ADIME Gizi) -- lihat kelas Surface/colorButton/style
 * di bawah, sengaja disalin gaya & palet warnanya (BACK/INK/TEAL/MUTED) persis supaya
 * konsisten dgn form lain yg sudah dimodernisasi.
 */
public final class RMPPRAAntibiotik extends JDialog {

    private static final Color BACK = new Color(243, 247, 250);
    private static final Color INK = new Color(24, 44, 62);
    private static final Color TEAL = new Color(12, 132, 124);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color GARIS = new Color(222, 232, 238);
    private static final Color HIJAU = new Color(8, 121, 102);
    private static final Color MERAH = new Color(187, 43, 65);
    private static final Color BIRU = new Color(37, 99, 205);
    private static final Color ABU = new Color(71, 85, 105);

    private final sekuel Sequel = new sekuel();
    private final Connection koneksi = koneksiDB.condb();
    private final DefaultTableModel tabModeAntibiotik;
    private final DefaultTableModel tabModeTanggal;
    private final Map<String, String> petaKodeAtc = new LinkedHashMap<>();
    private final Map<String, Double> petaStandarDdd = new LinkedHashMap<>();
    private final List<String> daftarAntibiotikSemua = new ArrayList<>();

    private Integer antibiotikIdTerpilih = null;
    private JTextField editorAntibiotik;
    private boolean sedangMenyaringAntibiotik = false;

    // Header identitas (readonly)
    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TDiagnosa = ro();
    private final widget.ComboBox cmbKsm = cmb("-- pilih KSM --", "Anak", "Obgyn", "Penyakit Dalam");
    private boolean sedangMemuatKsm = false;

    // Entri antibiotik baru
    private final widget.ComboBox cmbAntibiotik = new widget.ComboBox();
    private final widget.TextBox tDosisPemberian = tf();
    private final widget.ComboBox cmbInterval = cmb("4 Jam", "6 Jam", "8 Jam", "12 Jam", "24 Jam");
    private final widget.ComboBox cmbRute = cmb("IV", "Oral");
    private final widget.TextBox tDosisPerHari = ro();
    private final widget.TextBox tKodeAtc = ro();
    private final widget.TextBox tStandarDdd = ro();
    private final widget.Button BtnTambahAntibiotik = new widget.Button();
    private final widget.Button BtnHapusAntibiotik = new widget.Button();

    private final widget.Table tbAntibiotik = new widget.Table();
    private final JLabel ringkasanJumlahAntibiotik = new JLabel("0");
    private final JLabel ringkasanTotalDdd = new JLabel("0.0000");

    // Log tanggal pemberian (utk antibiotik yang dipilih di tabel atas)
    private final JLabel lblAntibiotikTerpilih = new JLabel("Pilih dulu salah satu antibiotik di tabel atas.");
    private final widget.Tanggal dtpTanggal = dtTanggalSaja();
    private final widget.Button BtnTambahTanggal = new widget.Button();
    private final widget.Button BtnHapusTanggal = new widget.Button();
    private final widget.Table tbTanggal = new widget.Table();

    private final widget.Button BtnKeluar = new widget.Button();

    public RMPPRAAntibiotik(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Input Antibiotik - PPRA / DDD ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        tabModeAntibiotik = new DefaultTableModel(null, new Object[]{
            "ID", "Nama Antibiotik", "Dosis/Pemberian (mg)", "Interval", "Dosis/Hari (mg)",
            "Rute", "Kode ATC", "Standar DDD (g)", "Lama Terapi (hari)", "Total Dosis (g)", "DDD"
        }) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tabModeTanggal = new DefaultTableModel(null, new Object[]{"ID", "Tanggal"}) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        muatReferensi();
        initComponents();
        setSize(1180, 780);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel raiz = new JPanel(new BorderLayout(0, 10));
        raiz.setBackground(BACK);
        raiz.setBorder(new EmptyBorder(10, 14, 12, 14));
        setContentPane(raiz);

        // ===== Header (kartu navy melengkung) + identitas pasien (kartu putih melengkung) =====
        JPanel header = new Surface(new BorderLayout(14, 0), INK);
        header.setBorder(new EmptyBorder(10, 16, 10, 16));
        JPanel blokJudul = new JPanel();
        blokJudul.setOpaque(false);
        blokJudul.setLayout(new BoxLayout(blokJudul, BoxLayout.Y_AXIS));
        JLabel judulUtama = new JLabel("Input Antibiotik - PPRA / DDD");
        judulUtama.setFont(new Font("Segoe UI", Font.BOLD, 23));
        judulUtama.setForeground(Color.WHITE);
        JLabel subJudul = new JLabel("Catat pemakaian antibiotik & hitung DDD otomatis per pasien rawat inap");
        subJudul.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subJudul.setForeground(new Color(205, 221, 238));
        blokJudul.add(Box.createVerticalGlue());
        blokJudul.add(judulUtama);
        blokJudul.add(Box.createVerticalStrut(2));
        blokJudul.add(subJudul);
        blokJudul.add(Box.createVerticalGlue());
        header.add(blokJudul, BorderLayout.CENTER);

        JPanel kartuRingkasanHeader = new JPanel(new java.awt.GridLayout(1, 2, 9, 0));
        kartuRingkasanHeader.setOpaque(false);
        kartuRingkasanHeader.add(buatKartuRingkasanMini("ANTIBIOTIK", ringkasanJumlahAntibiotik, TEAL));
        kartuRingkasanHeader.add(buatKartuRingkasanMini("TOTAL DDD", ringkasanTotalDdd, HIJAU));
        kartuRingkasanHeader.setPreferredSize(new Dimension(300, 52));
        header.add(kartuRingkasanHeader, BorderLayout.EAST);

        JPanel identitas = new Surface(new java.awt.GridLayout(1, 5, 0, 0), Color.WHITE);
        identitas.setBorder(new EmptyBorder(10, 14, 10, 14));
        identitas.add(fieldRingkasan("No. Rawat", TNoRw));
        identitas.add(fieldRingkasan("No. RM", TNoRM));
        identitas.add(fieldRingkasan("Nama Pasien", TPasien));
        identitas.add(fieldRingkasan("Diagnosa", TDiagnosa));
        identitas.add(fieldRingkasan("KSM", cmbKsm));

        JPanel bagianAtas = new JPanel(new BorderLayout(0, 10));
        bagianAtas.setOpaque(false);
        bagianAtas.add(header, BorderLayout.NORTH);
        bagianAtas.add(identitas, BorderLayout.CENTER);
        raiz.add(bagianAtas, BorderLayout.NORTH);

        // ===== Tengah =====
        JPanel tengah = new JPanel();
        tengah.setOpaque(false);
        tengah.setLayout(new BoxLayout(tengah, BoxLayout.Y_AXIS));

        JPanel kartuEntri = kartu("Tambah Antibiotik");
        int row = 0;
        row = pasanganVertikal(kartuEntri, row, "Nama Antibiotik (ketik utk mencari)", cmbAntibiotik, "Rute", cmbRute);
        row = pasanganVertikal(kartuEntri, row, "Dosis per Sekali Pemberian (mg)", tDosisPemberian,
                "Interval Pemberian", cmbInterval);
        row = pasanganVertikal(kartuEntri, row, "Dosis per Hari (mg) - otomatis", tDosisPerHari,
                "Kode ATC - otomatis", tKodeAtc);
        row = tunggalVertikal(kartuEntri, row, "Standar DDD (gram) - otomatis", tStandarDdd);
        kartuEntri.setAlignmentX(Component.LEFT_ALIGNMENT);
        tengah.add(kartuEntri);
        tengah.add(Box.createVerticalStrut(10));

        JPanel panelTombolEntri = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelTombolEntri.setOpaque(false);
        panelTombolEntri.setAlignmentX(Component.LEFT_ALIGNMENT);
        BtnTambahAntibiotik.setText("+ Tambah Antibiotik");
        BtnHapusAntibiotik.setText("Hapus Antibiotik Terpilih");
        colorButton(BtnTambahAntibiotik, HIJAU);
        colorButton(BtnHapusAntibiotik, MERAH);
        panelTombolEntri.add(BtnTambahAntibiotik);
        panelTombolEntri.add(BtnHapusAntibiotik);
        tengah.add(panelTombolEntri);
        tengah.add(Box.createVerticalStrut(12));

        JPanel kartuTabelAntibiotik = new Surface(new BorderLayout(0, 8), Color.WHITE);
        kartuTabelAntibiotik.setBorder(new EmptyBorder(12, 14, 12, 14));
        kartuTabelAntibiotik.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel judulAntibiotik = new JLabel("Daftar Antibiotik Rawatan Ini");
        judulAntibiotik.setFont(new Font("Segoe UI", Font.BOLD, 16));
        judulAntibiotik.setForeground(INK);
        kartuTabelAntibiotik.add(judulAntibiotik, BorderLayout.NORTH);

        tbAntibiotik.setModel(tabModeAntibiotik);
        tbAntibiotik.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbAntibiotik.setAutoResizeMode(widget.Table.AUTO_RESIZE_ALL_COLUMNS);
        gayaTabelModern(tbAntibiotik);
        tbAntibiotik.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { pilihAntibiotik(); }
        });
        JScrollPane scrollAntibiotik = new JScrollPane(tbAntibiotik);
        scrollAntibiotik.setPreferredSize(new Dimension(1100, 160));
        scrollAntibiotik.setBorder(BorderFactory.createLineBorder(GARIS));
        scrollAntibiotik.getViewport().setBackground(Color.WHITE);
        kartuTabelAntibiotik.add(scrollAntibiotik, BorderLayout.CENTER);
        tengah.add(kartuTabelAntibiotik);
        tengah.add(Box.createVerticalStrut(12));

        JPanel kartuTanggal = new Surface(new BorderLayout(0, 8), Color.WHITE);
        kartuTanggal.setBorder(new EmptyBorder(12, 14, 12, 14));
        kartuTanggal.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel blokJudulTanggal = new JPanel();
        blokJudulTanggal.setOpaque(false);
        blokJudulTanggal.setLayout(new BoxLayout(blokJudulTanggal, BoxLayout.Y_AXIS));
        JLabel judulTanggal = new JLabel("Log Tanggal Pemberian (untuk antibiotik terpilih di atas)");
        judulTanggal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        judulTanggal.setForeground(INK);
        judulTanggal.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblAntibiotikTerpilih.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAntibiotikTerpilih.setForeground(MUTED);
        lblAntibiotikTerpilih.setAlignmentX(Component.LEFT_ALIGNMENT);
        blokJudulTanggal.add(judulTanggal);
        blokJudulTanggal.add(Box.createVerticalStrut(3));
        blokJudulTanggal.add(lblAntibiotikTerpilih);
        blokJudulTanggal.add(Box.createVerticalStrut(8));

        JPanel panelTanggalEntri = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelTanggalEntri.setOpaque(false);
        dtpTanggal.setPreferredSize(new Dimension(130, 28));
        BtnTambahTanggal.setText("+ Tambah Tanggal Pemberian");
        BtnHapusTanggal.setText("Hapus Tanggal Terpilih");
        colorButton(BtnTambahTanggal, BIRU);
        colorButton(BtnHapusTanggal, ABU);
        panelTanggalEntri.add(labelAtas("Tanggal :"));
        panelTanggalEntri.add(dtpTanggal);
        panelTanggalEntri.add(BtnTambahTanggal);
        panelTanggalEntri.add(BtnHapusTanggal);
        blokJudulTanggal.add(panelTanggalEntri);
        kartuTanggal.add(blokJudulTanggal, BorderLayout.NORTH);

        tbTanggal.setModel(tabModeTanggal);
        tbTanggal.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbTanggal.setAutoResizeMode(widget.Table.AUTO_RESIZE_ALL_COLUMNS);
        gayaTabelModern(tbTanggal);
        if (tbTanggal.getColumnModel().getColumnCount() >= 2) {
            tbTanggal.getColumnModel().getColumn(0).setMinWidth(0);
            tbTanggal.getColumnModel().getColumn(0).setMaxWidth(0);
        }
        JScrollPane scrollTanggal = new JScrollPane(tbTanggal);
        scrollTanggal.setPreferredSize(new Dimension(300, 120));
        scrollTanggal.setBorder(BorderFactory.createLineBorder(GARIS));
        scrollTanggal.getViewport().setBackground(Color.WHITE);
        kartuTanggal.add(scrollTanggal, BorderLayout.CENTER);
        tengah.add(kartuTanggal);

        JScrollPane scrollTengah = new JScrollPane(tengah);
        scrollTengah.setBorder(null);
        scrollTengah.getViewport().setOpaque(false);
        scrollTengah.setOpaque(false);
        scrollTengah.getVerticalScrollBar().setUnitIncrement(20);
        raiz.add(scrollTengah, BorderLayout.CENTER);

        // ===== Bawah =====
        BtnKeluar.setText("Keluar");
        colorButton(BtnKeluar, INK);
        BtnKeluar.addActionListener(e -> dispose());
        JPanel bawah = new Surface(new FlowLayout(FlowLayout.RIGHT, 8, 8), Color.WHITE);
        bawah.setBorder(new EmptyBorder(6, 14, 6, 14));
        bawah.add(BtnKeluar);
        raiz.add(bawah, BorderLayout.SOUTH);

        // ===== Listener & validasi input =====
        tDosisPemberian.setDocument(new batasInput((byte) 10).getKata(tDosisPemberian));
        DocumentListener listenerHitung = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { hitungDosisPerHari(); }
            @Override public void removeUpdate(DocumentEvent e) { hitungDosisPerHari(); }
            @Override public void changedUpdate(DocumentEvent e) { hitungDosisPerHari(); }
        };
        tDosisPemberian.getDocument().addDocumentListener(listenerHitung);
        cmbInterval.addItemListener(e -> hitungDosisPerHari());
        cmbKsm.addItemListener(e -> simpanKsm());
        aktifkanPencarianAntibiotik();

        BtnTambahAntibiotik.addActionListener(e -> tambahAntibiotik());
        BtnHapusAntibiotik.addActionListener(e -> hapusAntibiotik());
        BtnTambahTanggal.addActionListener(e -> tambahTanggal());
        BtnHapusTanggal.addActionListener(e -> hapusTanggal());

        tabModeAntibiotik.addTableModelListener(e -> perbaruiRingkasanHeader());

        dtpTanggal.setDate(new Date());
        perbaruiLookupAntibiotik();
        hitungDosisPerHari();
        perbaruiRingkasanHeader();
    }

    /** Dipanggil dari DlgRawatInap (menu sidebar FormMenu). */
    public void setNoRawat(String norawat) {
        bersihkanEntri();
        antibiotikIdTerpilih = null;
        tabModeTanggal.setRowCount(0);
        lblAntibiotikTerpilih.setText("Pilih dulu salah satu antibiotik di tabel atas.");
        if (norawat == null || norawat.trim().equals("")) {
            TNoRw.setText(""); TNoRM.setText(""); TPasien.setText(""); TDiagnosa.setText("");
            cmbKsm.setSelectedIndex(0);
            tabModeAntibiotik.setRowCount(0);
            return;
        }
        TNoRw.setText(norawat.trim());
        tarikIdentitas(norawat.trim());
        muatKsm(norawat.trim());
        muatAntibiotik(norawat.trim());
    }

    private void muatKsm(String norawat) {
        sedangMemuatKsm = true;
        try {
            cmbKsm.setSelectedIndex(0);
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "select ksm from ppra_rawatan_ksm where no_rawat=?")) {
                ps.setString(1, norawat);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String ksm = rs.getString("ksm");
                        for (int i = 0; i < cmbKsm.getItemCount(); i++) {
                            if (String.valueOf(cmbKsm.getItemAt(i)).equals(ksm)) {
                                cmbKsm.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat KSM PPRA : " + e);
        } finally {
            sedangMemuatKsm = false;
        }
    }

    private void simpanKsm() {
        if (sedangMemuatKsm) { return; }
        String ksm = s(cmbKsm);
        if (ksm.isEmpty() || ambil(TNoRw).isEmpty()) { return; }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "replace into ppra_rawatan_ksm (no_rawat,ksm,created_by,created_at) values (?,?,?,now())")) {
            ps.setString(1, ambil(TNoRw));
            ps.setString(2, ksm);
            ps.setString(3, akses.getkode());
            ps.executeUpdate();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan KSM.\n" + e.getMessage());
        }
    }

    private void tarikIdentitas(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select p.no_rkm_medis,p.nm_pasien from reg_periksa rp "
                + "inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis where rp.no_rawat=?")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik identitas PPRA antibiotik : " + e);
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select penyakit.nm_penyakit from diagnosa_pasien "
                + "inner join penyakit on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit "
                + "where diagnosa_pasien.no_rawat=? "
                + "order by field(diagnosa_pasien.status,'Ranap','Ralan'),diagnosa_pasien.prioritas")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                StringBuilder sb = new StringBuilder();
                while (rs.next()) {
                    if (sb.length() > 0) { sb.append("; "); }
                    sb.append(rs.getString("nm_penyakit"));
                }
                TDiagnosa.setText(sb.toString());
            }
        } catch (Exception e) {
            System.out.println("Notif tarik diagnosa PPRA antibiotik : " + e);
        }
    }

    private void muatReferensi() {
        cmbAntibiotik.removeAllItems();
        cmbAntibiotik.addItem("-- pilih antibiotik --");
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select nama_antimikroba,kode_atc,standar_ddd_gram from ppra_referensi_ddd order by nama_antimikroba")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nama = rs.getString("nama_antimikroba");
                    petaKodeAtc.put(nama, rs.getString("kode_atc"));
                    petaStandarDdd.put(nama, rs.getDouble("standar_ddd_gram"));
                    daftarAntibiotikSemua.add(nama);
                    cmbAntibiotik.addItem(nama);
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat referensi PPRA DDD : " + e);
        }
    }

    private void perbaruiLookupAntibiotik() {
        String nama = cariNamaTepat(teksAntibiotik());
        if (nama == null) {
            tKodeAtc.setText(""); tStandarDdd.setText("");
            return;
        }
        tKodeAtc.setText(petaKodeAtc.get(nama));
        tStandarDdd.setText(String.format("%.4f", petaStandarDdd.get(nama)));
    }

    /** Teks yg sedang diketik/terpilih di kolom cari-antibiotik. */
    private String teksAntibiotik() {
        if (editorAntibiotik != null) {
            return editorAntibiotik.getText() == null ? "" : editorAntibiotik.getText().trim();
        }
        return s(cmbAntibiotik);
    }

    /** Hanya cocok kalau teks SAMA PERSIS (tanpa besar/kecil huruf) dgn salah satu nama master --
     *  supaya ketikan sebagian/belum dipilih dari daftar saran tidak dianggap sudah valid. */
    private String cariNamaTepat(String teks) {
        if (teks == null || teks.trim().isEmpty()) { return null; }
        String k = teks.trim();
        for (String nama : daftarAntibiotikSemua) {
            if (nama.equalsIgnoreCase(k)) { return nama; }
        }
        return null;
    }

    /** Pasang pencarian "live" (search-as-you-type) di kombo Nama Antibiotik: tiap ketikan
     *  menyaring ulang daftar 282 master ke yg mengandung ketikan, lalu tampilkan sbg saran di
     *  popup combo (klik/Enter dari popup = bawaan JComboBox, tak perlu kode tambahan). Teks yg
     *  sedang diketik user SELALU dikembalikan apa adanya sesudah model disaring -- DefaultComboBoxModel
     *  otomatis memilih item pertama begitu ditambahkan (saat selectedItem lama kosong), yg kalau tidak
     *  dikoreksi akan "menimpa" ketikan user di kolom teks. */
    private void aktifkanPencarianAntibiotik() {
        cmbAntibiotik.setEditable(true);
        cmbAntibiotik.setMaximumRowCount(10);
        editorAntibiotik = (JTextField) cmbAntibiotik.getEditor().getEditorComponent();
        editorAntibiotik.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editorAntibiotik.setForeground(INK);
        editorAntibiotik.setBackground(Color.WHITE);
        editorAntibiotik.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { jadwalkanSaring(); }
            @Override public void removeUpdate(DocumentEvent e) { jadwalkanSaring(); }
            @Override public void changedUpdate(DocumentEvent e) { }
            private void jadwalkanSaring() {
                if (sedangMenyaringAntibiotik) { return; }
                String ketikan = editorAntibiotik.getText();
                SwingUtilities.invokeLater(() -> saringAntibiotik(ketikan));
            }
        });
    }

    private void saringAntibiotik(String ketikanAwal) {
        sedangMenyaringAntibiotik = true;
        try {
            String ketikan = ketikanAwal == null ? "" : ketikanAwal;
            String kunci = ketikan.trim().toLowerCase();
            cmbAntibiotik.removeAllItems();
            if (kunci.isEmpty()) { cmbAntibiotik.addItem("-- pilih antibiotik --"); }
            for (String nama : daftarAntibiotikSemua) {
                if (kunci.isEmpty() || nama.toLowerCase().contains(kunci)) {
                    cmbAntibiotik.addItem(nama);
                }
            }
            editorAntibiotik.setText(ketikan);
            editorAntibiotik.setCaretPosition(ketikan.length());
            if (!kunci.isEmpty() && editorAntibiotik.isFocusOwner() && cmbAntibiotik.getItemCount() > 0) {
                cmbAntibiotik.setPopupVisible(true);
            } else {
                cmbAntibiotik.setPopupVisible(false);
            }
        } finally {
            sedangMenyaringAntibiotik = false;
        }
        perbaruiLookupAntibiotik();
    }

    /** Kembalikan kolom cari-antibiotik ke kondisi awal (daftar penuh, teks kosong). */
    private void resetPilihanAntibiotik() {
        sedangMenyaringAntibiotik = true;
        try {
            cmbAntibiotik.removeAllItems();
            cmbAntibiotik.addItem("-- pilih antibiotik --");
            for (String nama : daftarAntibiotikSemua) { cmbAntibiotik.addItem(nama); }
            if (editorAntibiotik != null) { editorAntibiotik.setText(""); }
            cmbAntibiotik.setPopupVisible(false);
        } finally {
            sedangMenyaringAntibiotik = false;
        }
    }

    private void perbaruiRingkasanHeader() {
        ringkasanJumlahAntibiotik.setText(String.valueOf(tabModeAntibiotik.getRowCount()));
        double total = 0;
        for (int r = 0; r < tabModeAntibiotik.getRowCount(); r++) {
            total += parseDouble(String.valueOf(tabModeAntibiotik.getValueAt(r, 10)));
        }
        ringkasanTotalDdd.setText(fmt4(total));
    }

    private void hitungDosisPerHari() {
        double dosis = parseDouble(ambil(tDosisPemberian));
        int interval = parseIntervalJam(s(cmbInterval));
        double perHari = interval > 0 ? dosis * (24.0 / interval) : 0;
        tDosisPerHari.setText(String.format("%.2f", perHari));
    }

    private void muatAntibiotik(String norawat) {
        tabModeAntibiotik.setRowCount(0);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select * from ppra_antibiotik_ranap where no_rawat=? order by id")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tabModeAntibiotik.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nama_antimikroba"),
                        fmt2(rs.getDouble("dosis_per_pemberian_mg")),
                        rs.getInt("interval_jam") + " Jam",
                        fmt2(rs.getDouble("dosis_per_hari_mg")),
                        rs.getString("rute"),
                        rs.getString("kode_atc"),
                        fmt4(rs.getDouble("standar_ddd_gram")),
                        rs.getInt("lama_terapi_hari"),
                        fmt4(rs.getDouble("total_dosis_gram")),
                        fmt4(rs.getDouble("ddd_dihitung"))
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat antibiotik PPRA : " + e);
        }
        aturLebarKolomAntibiotik();
    }

    private void aturLebarKolomAntibiotik() {
        if (tbAntibiotik.getColumnModel().getColumnCount() < 11) { return; }
        int[] lebar = {0, 220, 110, 70, 100, 50, 90, 90, 90, 90, 70};
        for (int i = 0; i < lebar.length; i++) {
            tbAntibiotik.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
        tbAntibiotik.getColumnModel().getColumn(0).setMinWidth(0);
        tbAntibiotik.getColumnModel().getColumn(0).setMaxWidth(0);
    }

    private void pilihAntibiotik() {
        int r = tbAntibiotik.getSelectedRow();
        if (r < 0) {
            antibiotikIdTerpilih = null;
            tabModeTanggal.setRowCount(0);
            lblAntibiotikTerpilih.setText("Pilih dulu salah satu antibiotik di tabel atas.");
            return;
        }
        antibiotikIdTerpilih = (Integer) tabModeAntibiotik.getValueAt(r, 0);
        lblAntibiotikTerpilih.setText("Antibiotik terpilih : " + tabModeAntibiotik.getValueAt(r, 1)
                + " (" + tabModeAntibiotik.getValueAt(r, 5) + ")");
        muatTanggal(antibiotikIdTerpilih);
    }

    private void tambahAntibiotik() {
        if (ambil(TNoRw).isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        String nama = cariNamaTepat(teksAntibiotik());
        if (nama == null) {
            JOptionPane.showMessageDialog(this, "Pilih Nama Antibiotik dari daftar terlebih dahulu.");
            return;
        }
        double dosis = parseDouble(ambil(tDosisPemberian));
        if (dosis <= 0) {
            JOptionPane.showMessageDialog(this, "Isi Dosis per Sekali Pemberian dengan angka lebih dari 0.");
            return;
        }
        int interval = parseIntervalJam(s(cmbInterval));
        double dosisPerHari = dosis * (24.0 / interval);
        String rute = s(cmbRute);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into ppra_antibiotik_ranap (no_rawat,nama_antimikroba,kode_atc,standar_ddd_gram,"
                + "dosis_per_pemberian_mg,interval_jam,dosis_per_hari_mg,rute,lama_terapi_hari,total_dosis_gram,"
                + "ddd_dihitung,created_by,created_at) values (?,?,?,?,?,?,?,?,0,0,0,?,now())")) {
            ps.setString(1, ambil(TNoRw));
            ps.setString(2, nama);
            ps.setString(3, petaKodeAtc.get(nama));
            ps.setDouble(4, petaStandarDdd.get(nama));
            ps.setDouble(5, dosis);
            ps.setInt(6, interval);
            ps.setDouble(7, dosisPerHari);
            ps.setString(8, rute);
            ps.setString(9, akses.getkode());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Antibiotik ditambahkan. Lanjutkan isi tanggal pemberian di bawah.");
            bersihkanEntri();
            muatAntibiotik(ambil(TNoRw));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menambah antibiotik.\n" + e.getMessage());
        }
    }

    private void hapusAntibiotik() {
        if (antibiotikIdTerpilih == null) {
            JOptionPane.showMessageDialog(this, "Pilih antibiotik di tabel atas terlebih dahulu.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus antibiotik terpilih beserta seluruh log tanggalnya ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try (PreparedStatement ps1 = koneksi.prepareStatement(
                "delete from ppra_antibiotik_ranap_tanggal where antibiotik_id=?")) {
            ps1.setInt(1, antibiotikIdTerpilih);
            ps1.executeUpdate();
            try (PreparedStatement ps2 = koneksi.prepareStatement(
                    "delete from ppra_antibiotik_ranap where id=?")) {
                ps2.setInt(1, antibiotikIdTerpilih);
                ps2.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Antibiotik dihapus.");
            antibiotikIdTerpilih = null;
            tabModeTanggal.setRowCount(0);
            lblAntibiotikTerpilih.setText("Pilih dulu salah satu antibiotik di tabel atas.");
            muatAntibiotik(ambil(TNoRw));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus antibiotik.\n" + e.getMessage());
        }
    }

    private void muatTanggal(Integer antibiotikId) {
        tabModeTanggal.setRowCount(0);
        if (antibiotikId == null) { return; }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select id,date_format(tanggal,'%d-%m-%Y') as tgl from ppra_antibiotik_ranap_tanggal "
                + "where antibiotik_id=? order by tanggal")) {
            ps.setInt(1, antibiotikId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tabModeTanggal.addRow(new Object[]{rs.getInt("id"), rs.getString("tgl")});
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat tanggal PPRA : " + e);
        }
    }

    private void tambahTanggal() {
        if (antibiotikIdTerpilih == null) {
            JOptionPane.showMessageDialog(this, "Pilih antibiotik di tabel atas terlebih dahulu.");
            return;
        }
        String tanggal = ambilTanggalSaja(dtpTanggal);
        if (tanggal == null) {
            JOptionPane.showMessageDialog(this, "Tanggal tidak valid.");
            return;
        }
        try {
            int sudahAda = Sequel.cariInteger(
                    "select count(*) from ppra_antibiotik_ranap_tanggal where antibiotik_id=? and tanggal=?",
                    String.valueOf(antibiotikIdTerpilih), tanggal);
            if (sudahAda > 0) {
                JOptionPane.showMessageDialog(this, "Tanggal ini sudah tercatat untuk antibiotik ini.");
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memeriksa tanggal.\n" + e.getMessage());
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into ppra_antibiotik_ranap_tanggal (antibiotik_id,tanggal,created_by,created_at) "
                + "values (?,?,?,now())")) {
            ps.setInt(1, antibiotikIdTerpilih);
            ps.setString(2, tanggal);
            ps.setString(3, akses.getkode());
            ps.executeUpdate();
            hitungUlangAntibiotik(antibiotikIdTerpilih);
            muatTanggal(antibiotikIdTerpilih);
            muatAntibiotikJagaPilihan();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menambah tanggal.\n" + e.getMessage());
        }
    }

    private void hapusTanggal() {
        int r = tbTanggal.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Pilih tanggal di tabel log terlebih dahulu.");
            return;
        }
        Integer idTanggal = (Integer) tabModeTanggal.getValueAt(r, 0);
        if (JOptionPane.showConfirmDialog(this, "Hapus tanggal terpilih ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "delete from ppra_antibiotik_ranap_tanggal where id=?")) {
            ps.setInt(1, idTanggal);
            ps.executeUpdate();
            hitungUlangAntibiotik(antibiotikIdTerpilih);
            muatTanggal(antibiotikIdTerpilih);
            muatAntibiotikJagaPilihan();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus tanggal.\n" + e.getMessage());
        }
    }

    /** Reload tabel master antibiotik tapi tetap pilih baris antibiotik yang sama (dari id). */
    private void muatAntibiotikJagaPilihan() {
        Integer id = antibiotikIdTerpilih;
        muatAntibiotik(ambil(TNoRw));
        if (id == null) { return; }
        for (int r = 0; r < tabModeAntibiotik.getRowCount(); r++) {
            if (id.equals(tabModeAntibiotik.getValueAt(r, 0))) {
                tbAntibiotik.setRowSelectionInterval(r, r);
                break;
            }
        }
    }

    /** Lama Terapi = COUNT baris tanggal (bukan selisih tanggal awal-akhir) -- lihat catatan kelas. */
    private void hitungUlangAntibiotik(int antibiotikId) {
        try {
            int lamaTerapi = Sequel.cariInteger(
                    "select count(*) from ppra_antibiotik_ranap_tanggal where antibiotik_id=?",
                    String.valueOf(antibiotikId));
            double dosisPerHari = 0, standarDdd = 0;
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "select dosis_per_hari_mg,standar_ddd_gram from ppra_antibiotik_ranap where id=?")) {
                ps.setInt(1, antibiotikId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        dosisPerHari = rs.getDouble("dosis_per_hari_mg");
                        standarDdd = rs.getDouble("standar_ddd_gram");
                    }
                }
            }
            double totalDosisGram = (dosisPerHari / 1000.0) * lamaTerapi;
            double dddDihitung = standarDdd > 0 ? totalDosisGram / standarDdd : 0;
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "update ppra_antibiotik_ranap set lama_terapi_hari=?,total_dosis_gram=?,ddd_dihitung=? where id=?")) {
                ps.setInt(1, lamaTerapi);
                ps.setDouble(2, totalDosisGram);
                ps.setDouble(3, dddDihitung);
                ps.setInt(4, antibiotikId);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghitung ulang Lama Terapi/DDD.\n" + e.getMessage());
        }
    }

    private void bersihkanEntri() {
        resetPilihanAntibiotik();
        tDosisPemberian.setText("");
        cmbInterval.setSelectedIndex(0);
        cmbRute.setSelectedIndex(0);
        tKodeAtc.setText("");
        tStandarDdd.setText("");
        hitungDosisPerHari();
    }

    // ====================== Helpers ======================
    private static int parseIntervalJam(String teksCombo) {
        if (teksCombo == null) { return 24; }
        String digit = teksCombo.replaceAll("[^0-9]", "");
        if (digit.isEmpty()) { return 24; }
        try { return Integer.parseInt(digit); } catch (Exception e) { return 24; }
    }

    private static double parseDouble(String v) {
        if (v == null || v.trim().isEmpty()) { return 0; }
        try { return Double.parseDouble(v.trim().replace(",", ".")); } catch (Exception e) { return 0; }
    }

    private static String fmt2(double v) { return String.format("%.2f", v); }
    private static String fmt4(double v) { return String.format("%.4f", v); }

    private static String ambilTanggalSaja(widget.Tanggal d) {
        Object v = d.getSelectedItem();
        if (v == null) { return null; }
        try {
            SimpleDateFormat parse = new SimpleDateFormat("dd-MM-yyyy");
            Date tgl = parse.parse(v.toString().length() >= 10 ? v.toString().substring(0, 10) : v.toString());
            return new SimpleDateFormat("yyyy-MM-dd").format(tgl);
        } catch (Exception e) {
            return null;
        }
    }

    private static widget.Tanggal dtTanggalSaja() {
        widget.Tanggal d = new widget.Tanggal();
        d.setDisplayFormat("dd-MM-yyyy");
        return d;
    }

    private static widget.TextBox tf() { return new widget.TextBox(); }

    private static widget.TextBox ro() {
        widget.TextBox t = new widget.TextBox();
        t.setEditable(false);
        return t;
    }

    private static widget.ComboBox cmb(String... items) {
        widget.ComboBox c = new widget.ComboBox();
        for (String it : items) { c.addItem(it); }
        return c;
    }

    private static String s(widget.ComboBox c) {
        Object v = c.getSelectedItem();
        if (v == null) { return ""; }
        String r = v.toString();
        return r.startsWith("--") ? "" : r;
    }

    private static String ambil(widget.TextBox t) {
        return t.getText() == null ? "" : t.getText().trim();
    }

    /** Ratakan tampilan "glass" bawaan widget.TextBox/widget.ComboBox jadi flat modern, dgn
     *  skala font PERSIS spt SatuSehatAturNutritionOrder.java ("Instruksi Diet / NutritionOrder"):
     *  teks isian Segoe UI PLAIN 13, kombo/dropdown Segoe UI PLAIN 14. */
    private static void style(JComponent component) {
        component.setForeground(INK);
        if (component instanceof JTextField) {
            component.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            component.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(GARIS), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            component.setBackground(Color.WHITE);
        } else if (component instanceof javax.swing.JComboBox) {
            component.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            component.setBackground(Color.WHITE);
        }
    }

    /** Tombol flat rounded ala colorButton() di AdimeGiziView -- timpa gradient "glass"
     *  bawaan widget.Button tapi listener/komponen aslinya tetap dipakai. */
    private static void colorButton(javax.swing.AbstractButton button, final Color color) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setFocusPainted(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setUI(new BasicButtonUI() {
            @Override
            public void update(Graphics graphics, JComponent component) {
                javax.swing.AbstractButton b = (javax.swing.AbstractButton) component;
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, b.getWidth(), b.getHeight());
                g.setColor(!b.isEnabled() ? new Color(218, 226, 232)
                        : b.getModel().isPressed() ? color.darker()
                        : b.getModel().isRollover() ? color.brighter() : color);
                g.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 10, 10);
                if (b.hasFocus()) {
                    g.setColor(new Color(166, 215, 232));
                    g.drawRoundRect(2, 2, b.getWidth() - 5, b.getHeight() - 5, 8, 8);
                }
                g.dispose();
                paint(graphics, component);
            }
        });
    }

    /** Header tabel + grid flat, baris selang-seling -- gaya persis tabel riwayat di AdimeGiziView. */
    private void gayaTabelModern(widget.Table tabel) {
        tabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabel.setRowHeight(30);
        tabel.setShowGrid(false);
        tabel.setIntercellSpacing(new Dimension(0, 0));
        tabel.setFillsViewportHeight(true);
        tabel.setBackground(Color.WHITE);
        tabel.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabel.getTableHeader().setBackground(new Color(234, 243, 247));
        tabel.getTableHeader().setForeground(INK);
        tabel.getTableHeader().setPreferredSize(new Dimension(100, 32));
        tabel.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                    boolean focus, int r, int col) {
                super.getTableCellRendererComponent(t, value, selected, focus, r, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                setBackground(selected ? new Color(214, 239, 235) : r % 2 == 0 ? Color.WHITE : new Color(247, 250, 252));
                setForeground(INK);
                return this;
            }
        });
    }

    /** Kartu ringkasan kecil di header (mis. jumlah antibiotik/total DDD). */
    private JPanel buatKartuRingkasanMini(String judul, JLabel nilai, Color aksen) {
        JPanel panel = new Surface(new BorderLayout(6, 0), Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, aksen), new EmptyBorder(6, 10, 6, 10)));
        JLabel label = new JLabel(judul);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setForeground(MUTED);
        nilai.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nilai.setForeground(INK);
        nilai.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        panel.add(label, BorderLayout.CENTER);
        panel.add(nilai, BorderLayout.EAST);
        return panel;
    }

    private JPanel fieldRingkasan(String teksLabel, Component komponen) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 10, 0, 10));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = labelAtas(teksLabel);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (komponen instanceof JComponent) {
            style((JComponent) komponen);
            // Identitas pasien lbh menonjol dari field isian biasa -- spt "patient" (BOLD 14)
            // di SatuSehatAturNutritionOrder.java, bukan PLAIN spt field isian form Tambah Antibiotik.
            komponen.setFont(new Font("Segoe UI", Font.BOLD, 14));
        }
        komponen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        komponen.setPreferredSize(new Dimension(150, 28));
        p.add(l);
        p.add(Box.createVerticalStrut(3));
        p.add(komponen);
        return p;
    }

    private JPanel kartu(String judul) {
        JPanel luar = new Surface(new GridBagLayout(), Color.WHITE);
        luar.setBorder(new EmptyBorder(12, 14, 14, 14));
        JLabel l = new JLabel(judul);
        l.setFont(new Font("Segoe UI", Font.BOLD, 16));
        l.setForeground(INK);
        GridBagConstraints g = gc(0, 0, 4, 1.0);
        g.insets = new Insets(2, 4, 12, 4);
        luar.add(l, g);
        return luar;
    }

    private int pasanganVertikal(JPanel p, int row, String label1, Component komponen1,
            String label2, Component komponen2) {
        int barisLabel = (row * 2) + 1;
        int barisInput = barisLabel + 1;
        p.add(labelAtas(label1), gc(0, barisLabel, 2, 0.5));
        p.add(labelAtas(label2), gc(2, barisLabel, 2, 0.5));
        siapkanInput(komponen1);
        siapkanInput(komponen2);
        GridBagConstraints kiri = gc(0, barisInput, 2, 0.5);
        kiri.insets = new Insets(1, 4, 8, 10);
        GridBagConstraints kanan = gc(2, barisInput, 2, 0.5);
        kanan.insets = new Insets(1, 10, 8, 4);
        p.add(komponen1, kiri);
        p.add(komponen2, kanan);
        return row + 1;
    }

    private int tunggalVertikal(JPanel p, int row, String label, Component komponen) {
        int barisLabel = (row * 2) + 1;
        int barisInput = barisLabel + 1;
        p.add(labelAtas(label), gc(0, barisLabel, 4, 1.0));
        siapkanInput(komponen);
        GridBagConstraints g = gc(0, barisInput, 4, 1.0);
        g.insets = new Insets(1, 4, 8, 4);
        p.add(komponen, g);
        return row + 1;
    }

    private JLabel labelAtas(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(MUTED);
        return l;
    }

    private void siapkanInput(Component komponen) {
        komponen.setPreferredSize(new Dimension(220, 30));
        if (komponen instanceof JComponent) { style((JComponent) komponen); }
    }

    private GridBagConstraints gc(int x, int y, int w, double wx) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x; g.gridy = y; g.gridwidth = w; g.weightx = wx;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(2, 4, 2, 4);
        return g;
    }

    /** Panel putih/navy dgn sudut melengkung -- disalin persis dari kelas Surface di AdimeGiziView
     *  supaya semua "kartu" di form ini konsisten gaya dgn form ADIME Gizi yg sudah dimodernisasi. */
    private static final class Surface extends JPanel {
        private final Color fill;
        Surface(java.awt.LayoutManager layout, Color fill) {
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
