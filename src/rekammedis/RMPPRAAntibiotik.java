package rekammedis;

import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
 * "Lama Rawat Inap" tidak ada di form ini -- nanti ditarik dari menu Hitung ALOS
 * setelah menu itu direvisi (lihat memory terkait), jadi field itu DITUNDA dulu.
 */
public final class RMPPRAAntibiotik extends JDialog {

    private final sekuel Sequel = new sekuel();
    private final Connection koneksi = koneksiDB.condb();
    private final DefaultTableModel tabModeAntibiotik;
    private final DefaultTableModel tabModeTanggal;
    private final Map<String, String> petaKodeAtc = new LinkedHashMap<>();
    private final Map<String, Double> petaStandarDdd = new LinkedHashMap<>();

    private Integer antibiotikIdTerpilih = null;

    // Header identitas (readonly)
    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TDiagnosa = ro();

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
        final Color utama = new Color(0, 133, 143);
        final Color latar = new Color(246, 249, 251);
        final Color garis = new Color(215, 224, 230);
        final Color teks = new Color(32, 49, 66);

        getContentPane().setBackground(latar);
        getContentPane().setLayout(new BorderLayout(0, 10));

        // ===== Header =====
        JPanel atas = new JPanel(new BorderLayout(12, 10));
        atas.setBackground(latar);
        atas.setBorder(new EmptyBorder(14, 18, 0, 18));

        JLabel judulUtama = new JLabel("Input Antibiotik - PPRA / DDD");
        judulUtama.setFont(new Font("Tahoma", Font.BOLD, 20));
        judulUtama.setForeground(teks);
        atas.add(judulUtama, BorderLayout.NORTH);

        JPanel ringkasanPasien = new JPanel(new java.awt.GridLayout(1, 4, 0, 0));
        ringkasanPasien.setBackground(Color.WHITE);
        ringkasanPasien.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis), new EmptyBorder(10, 12, 10, 12)));
        ringkasanPasien.add(fieldRingkasan("No. Rawat", TNoRw));
        ringkasanPasien.add(fieldRingkasan("No. RM", TNoRM));
        ringkasanPasien.add(fieldRingkasan("Nama Pasien", TPasien));
        ringkasanPasien.add(fieldRingkasan("Diagnosa", TDiagnosa));
        atas.add(ringkasanPasien, BorderLayout.CENTER);
        getContentPane().add(atas, BorderLayout.NORTH);

        // ===== Tengah =====
        JPanel tengah = new JPanel();
        tengah.setBackground(latar);
        tengah.setLayout(new BoxLayout(tengah, BoxLayout.Y_AXIS));
        tengah.setBorder(new EmptyBorder(10, 18, 10, 18));

        JPanel kartuEntri = kartu("Tambah Antibiotik", teks, garis);
        int row = 0;
        row = pasanganVertikal(kartuEntri, row, "Nama Antibiotik", cmbAntibiotik, "Rute", cmbRute);
        row = pasanganVertikal(kartuEntri, row, "Dosis per Sekali Pemberian (mg)", tDosisPemberian,
                "Interval Pemberian", cmbInterval);
        row = pasanganVertikal(kartuEntri, row, "Dosis per Hari (mg) - otomatis", tDosisPerHari,
                "Kode ATC - otomatis", tKodeAtc);
        row = tunggalVertikal(kartuEntri, row, "Standar DDD (gram) - otomatis", tStandarDdd);
        kartuEntri.setAlignmentX(Component.LEFT_ALIGNMENT);
        tengah.add(kartuEntri);
        tengah.add(Box.createVerticalStrut(8));

        JPanel panelTombolEntri = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelTombolEntri.setOpaque(false);
        panelTombolEntri.setAlignmentX(Component.LEFT_ALIGNMENT);
        BtnTambahAntibiotik.setText("+ Tambah Antibiotik");
        BtnHapusAntibiotik.setText("Hapus Antibiotik Terpilih");
        panelTombolEntri.add(BtnTambahAntibiotik);
        panelTombolEntri.add(BtnHapusAntibiotik);
        tengah.add(panelTombolEntri);
        tengah.add(Box.createVerticalStrut(10));

        JLabel judulAntibiotik = new JLabel("Daftar Antibiotik Rawatan Ini");
        judulAntibiotik.setFont(new Font("Tahoma", Font.BOLD, 13));
        judulAntibiotik.setForeground(teks);
        judulAntibiotik.setAlignmentX(Component.LEFT_ALIGNMENT);
        judulAntibiotik.setBorder(new EmptyBorder(0, 0, 6, 0));
        tengah.add(judulAntibiotik);

        tbAntibiotik.setModel(tabModeAntibiotik);
        tbAntibiotik.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbAntibiotik.setAutoResizeMode(widget.Table.AUTO_RESIZE_OFF);
        tbAntibiotik.setRowHeight(24);
        tbAntibiotik.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { pilihAntibiotik(); }
        });
        JScrollPane scrollAntibiotik = new JScrollPane(tbAntibiotik);
        scrollAntibiotik.setPreferredSize(new Dimension(1120, 160));
        scrollAntibiotik.setAlignmentX(Component.LEFT_ALIGNMENT);
        tengah.add(scrollAntibiotik);
        tengah.add(Box.createVerticalStrut(12));

        JLabel judulTanggal = new JLabel("Log Tanggal Pemberian (untuk antibiotik terpilih di atas)");
        judulTanggal.setFont(new Font("Tahoma", Font.BOLD, 13));
        judulTanggal.setForeground(teks);
        judulTanggal.setAlignmentX(Component.LEFT_ALIGNMENT);
        judulTanggal.setBorder(new EmptyBorder(0, 0, 4, 0));
        tengah.add(judulTanggal);

        lblAntibiotikTerpilih.setFont(new Font("Tahoma", Font.ITALIC, 11));
        lblAntibiotikTerpilih.setForeground(new Color(91, 105, 115));
        lblAntibiotikTerpilih.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblAntibiotikTerpilih.setBorder(new EmptyBorder(0, 0, 6, 0));
        tengah.add(lblAntibiotikTerpilih);

        JPanel panelTanggalEntri = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelTanggalEntri.setOpaque(false);
        panelTanggalEntri.setAlignmentX(Component.LEFT_ALIGNMENT);
        dtpTanggal.setPreferredSize(new Dimension(130, 25));
        BtnTambahTanggal.setText("+ Tambah Tanggal Pemberian");
        BtnHapusTanggal.setText("Hapus Tanggal Terpilih");
        panelTanggalEntri.add(new JLabel("Tanggal : "));
        panelTanggalEntri.add(dtpTanggal);
        panelTanggalEntri.add(BtnTambahTanggal);
        panelTanggalEntri.add(BtnHapusTanggal);
        tengah.add(panelTanggalEntri);
        tengah.add(Box.createVerticalStrut(6));

        tbTanggal.setModel(tabModeTanggal);
        tbTanggal.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbTanggal.setAutoResizeMode(widget.Table.AUTO_RESIZE_OFF);
        tbTanggal.setRowHeight(22);
        JScrollPane scrollTanggal = new JScrollPane(tbTanggal);
        scrollTanggal.setPreferredSize(new Dimension(300, 120));
        scrollTanggal.setAlignmentX(Component.LEFT_ALIGNMENT);
        tengah.add(scrollTanggal);

        JScrollPane scrollTengah = new JScrollPane(tengah);
        scrollTengah.setBorder(null);
        scrollTengah.getVerticalScrollBar().setUnitIncrement(20);
        getContentPane().add(scrollTengah, BorderLayout.CENTER);

        // ===== Bawah =====
        BtnKeluar.setText("Keluar");
        BtnKeluar.addActionListener(e -> dispose());
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 9));
        bawah.setBackground(Color.WHITE);
        bawah.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, garis));
        bawah.add(BtnKeluar);
        getContentPane().add(bawah, BorderLayout.SOUTH);

        // ===== Listener & validasi input =====
        tDosisPemberian.setDocument(new batasInput((byte) 10).getKata(tDosisPemberian));
        DocumentListener listenerHitung = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { hitungDosisPerHari(); }
            @Override public void removeUpdate(DocumentEvent e) { hitungDosisPerHari(); }
            @Override public void changedUpdate(DocumentEvent e) { hitungDosisPerHari(); }
        };
        tDosisPemberian.getDocument().addDocumentListener(listenerHitung);
        cmbInterval.addItemListener(e -> hitungDosisPerHari());
        cmbAntibiotik.addItemListener(e -> perbaruiLookupAntibiotik());

        BtnTambahAntibiotik.addActionListener(e -> tambahAntibiotik());
        BtnHapusAntibiotik.addActionListener(e -> hapusAntibiotik());
        BtnTambahTanggal.addActionListener(e -> tambahTanggal());
        BtnHapusTanggal.addActionListener(e -> hapusTanggal());

        dtpTanggal.setDate(new Date());
        perbaruiLookupAntibiotik();
        hitungDosisPerHari();
    }

    /** Dipanggil dari DlgRawatInap (menu sidebar FormMenu). */
    public void setNoRawat(String norawat) {
        bersihkanEntri();
        antibiotikIdTerpilih = null;
        tabModeTanggal.setRowCount(0);
        lblAntibiotikTerpilih.setText("Pilih dulu salah satu antibiotik di tabel atas.");
        if (norawat == null || norawat.trim().equals("")) {
            TNoRw.setText(""); TNoRM.setText(""); TPasien.setText(""); TDiagnosa.setText("");
            tabModeAntibiotik.setRowCount(0);
            return;
        }
        TNoRw.setText(norawat.trim());
        tarikIdentitas(norawat.trim());
        muatAntibiotik(norawat.trim());
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
                    cmbAntibiotik.addItem(nama);
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat referensi PPRA DDD : " + e);
        }
    }

    private void perbaruiLookupAntibiotik() {
        String nama = s(cmbAntibiotik);
        if (nama.isEmpty() || !petaKodeAtc.containsKey(nama)) {
            tKodeAtc.setText(""); tStandarDdd.setText("");
            return;
        }
        tKodeAtc.setText(petaKodeAtc.get(nama));
        tStandarDdd.setText(String.format("%.4f", petaStandarDdd.get(nama)));
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
        String nama = s(cmbAntibiotik);
        if (nama.isEmpty() || !petaKodeAtc.containsKey(nama)) {
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
        cmbAntibiotik.setSelectedIndex(0);
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

    private JPanel kartu(String judul, Color teks, Color garis) {
        JPanel luar = new JPanel(new GridBagLayout());
        luar.setBackground(Color.WHITE);
        luar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis), new EmptyBorder(8, 12, 12, 12)));
        JLabel l = new JLabel(judul);
        l.setFont(new Font("Tahoma", Font.BOLD, 13));
        l.setForeground(teks);
        GridBagConstraints g = gc(0, 0, 4, 1.0);
        g.insets = new Insets(2, 4, 10, 4);
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
        l.setFont(new Font("Tahoma", Font.PLAIN, 11));
        l.setForeground(new Color(49, 64, 75));
        return l;
    }

    private void siapkanInput(Component komponen) {
        komponen.setPreferredSize(new Dimension(220, 28));
    }

    private GridBagConstraints gc(int x, int y, int w, double wx) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x; g.gridy = y; g.gridwidth = w; g.weightx = wx;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(2, 4, 2, 4);
        return g;
    }
}
