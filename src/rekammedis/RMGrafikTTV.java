package rekammedis;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;
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
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Grafik Nadi, Suhu & TTV (RM 9). Tampilan dibuat mengikuti bentuk kertas RM
 * 9 asli: sumbu-X berupa GRID kolom per tanggal, tiap tanggal dibagi 4 slot
 * jam checkpoint (06/12/18/24) -- bukan sumbu waktu kontinu.
 *
 * Nadi/Suhu/Respirasi/Tensi/Tinggi/Berat OTOMATIS ditarik dari riwayat
 * SOAP/CPPT (tabel pemeriksaan_ranap, diisi tiap kali DlgSOAPPerawatan
 * disimpan) -- jam aktualnya di-"snap" ke checkpoint terdekat (06/12/18/24).
 * Bisa difilter rentang tanggal (default: tanggal masuk s.d. hari ini).
 *
 * Baris Intake/Output (Per Oral, Parenteral, Transfusi, D.L.L, Kemih,
 * Muntah, Defekasi, Berkemih) tidak ada sumbernya di sistem manapun, jadi
 * tetap diisi manual per tanggal (snapshot 1 baris/hari, tabel terpisah
 * grafik_intake_output).
 */
public final class RMGrafikTTV extends JDialog {

    private static final String[] SLOTS = {"06", "12", "18", "24"};
    private static final int[] SLOT_JAM = {6, 12, 18, 24};

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final DefaultTableModel tabModeVital;
    private final DefaultTableModel tabModeIO;
    private final DefaultTableModel tabModeGridBawah = new DefaultTableModel() {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    // Header identitas (readonly)
    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TJK = ro();
    private final widget.TextBox TTglLahir = ro();
    private final widget.TextBox TRuangan = ro();

    private final JPanel panelChart = new JPanel(new BorderLayout());
    private final widget.Table tbGridBawah = new widget.Table();
    private final widget.Table tbVital = new widget.Table();
    private final widget.Table tbIO = new widget.Table();

    // Filter rentang tanggal grafik
    private final widget.Tanggal dtpDari = dtTanggal();
    private final widget.Tanggal dtpSampai = dtTanggal();
    private final widget.Button BtnTampilkanGrafik = new widget.Button();

    // Entri Intake/Output harian
    private final widget.Tanggal dtpTanggalIO = dtTanggal();
    private final widget.TextBox tPerOral = tf();
    private final widget.TextBox tParenteral = tf();
    private final widget.TextBox tTransfusi = tf();
    private final widget.TextBox tDll = tf();
    private final widget.TextBox tKemih = tf();
    private final widget.TextBox tMuntah = tf();
    private final widget.TextBox tDefekasi = tf();
    private final widget.TextBox tBerkemih = tf();
    private final widget.TextArea taCatatan = ta();

    private final widget.Button BtnSimpanIO = new widget.Button();
    private final widget.Button BtnHapusIO = new widget.Button();
    private final widget.Button BtnBersihkanIO = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();

    public RMGrafikTTV(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Grafik Nadi, Suhu & TTV (RM 9) ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        ensureTable();
        tabModeVital = new DefaultTableModel(null, new Object[]{
            "Tanggal", "Jam", "Nadi", "Suhu", "Respirasi", "Tekanan Darah", "Tinggi", "Berat"
        }) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tabModeIO = new DefaultTableModel(null, new Object[]{
            "TglRaw", "Tanggal", "Per Oral", "Parenteral", "Transfusi", "D.L.L",
            "Kemih", "Muntah", "Defekasi", "Berkemih", "Catatan"
        }) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        initComponents();
        setSize(1200, 950);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        final Color latar = new Color(246, 249, 251);
        final Color garis = new Color(215, 224, 230);
        final Color teks = new Color(32, 49, 66);

        getContentPane().setBackground(latar);
        getContentPane().setLayout(new BorderLayout(0, 10));

        JPanel atas = new JPanel(new BorderLayout(12, 10));
        atas.setBackground(latar);
        atas.setBorder(new EmptyBorder(14, 18, 0, 18));

        JLabel judulUtama = new JLabel("Grafik Nadi, Suhu & TTV");
        judulUtama.setFont(new Font("Tahoma", Font.BOLD, 20));
        judulUtama.setForeground(teks);
        atas.add(judulUtama, BorderLayout.NORTH);

        JPanel ringkasanPasien = new JPanel(new java.awt.GridLayout(1, 6, 0, 0));
        ringkasanPasien.setBackground(Color.WHITE);
        ringkasanPasien.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis),
                new EmptyBorder(10, 12, 10, 12)));
        ringkasanPasien.add(fieldRingkasan("No. Rawat *", TNoRw));
        ringkasanPasien.add(fieldRingkasan("No. RM", TNoRM));
        ringkasanPasien.add(fieldRingkasan("Nama Pasien", TPasien));
        ringkasanPasien.add(fieldRingkasan("Jenis Kelamin", TJK));
        ringkasanPasien.add(fieldRingkasan("Tanggal Lahir", TTglLahir));
        ringkasanPasien.add(fieldRingkasan("Ruangan / Kamar", TRuangan));
        atas.add(ringkasanPasien, BorderLayout.CENTER);
        getContentPane().add(atas, BorderLayout.NORTH);

        JPanel tengah = new JPanel();
        tengah.setBackground(latar);
        tengah.setLayout(new BoxLayout(tengah, BoxLayout.Y_AXIS));
        tengah.setBorder(new EmptyBorder(10, 18, 10, 18));

        // --- Grafik grid per tanggal (spt kertas RM 9) ---
        JPanel panelFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelFilter.setOpaque(false);
        panelFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel labelDari = new JLabel("Tanggal Dari");
        labelDari.setFont(new Font("Tahoma", Font.PLAIN, 11));
        dtpDari.setPreferredSize(new Dimension(120, 25));
        JLabel labelSampai = new JLabel("Sampai");
        labelSampai.setFont(new Font("Tahoma", Font.PLAIN, 11));
        dtpSampai.setPreferredSize(new Dimension(120, 25));
        BtnTampilkanGrafik.setText("Tampilkan");
        panelFilter.add(labelDari);
        panelFilter.add(dtpDari);
        panelFilter.add(labelSampai);
        panelFilter.add(dtpSampai);
        panelFilter.add(BtnTampilkanGrafik);
        tengah.add(panelFilter);
        tengah.add(Box.createVerticalStrut(8));

        JPanel kartuGrafik = kartu("Grafik Nadi & Suhu -- grid per tanggal (otomatis dari SOAP/CPPT)", teks, garis);
        GridBagConstraints gGrafik = gc(0, 1, 4, 1.0);
        gGrafik.fill = GridBagConstraints.BOTH;
        gGrafik.weighty = 1.0;
        panelChart.setOpaque(false);
        panelChart.setPreferredSize(new Dimension(100, 320));
        kartuGrafik.add(panelChart, gGrafik);
        kartuGrafik.setAlignmentX(Component.LEFT_ALIGNMENT);
        tengah.add(kartuGrafik);
        tengah.add(Box.createVerticalStrut(4));

        tbGridBawah.setModel(tabModeGridBawah);
        tbGridBawah.setAutoResizeMode(widget.Table.AUTO_RESIZE_OFF);
        tbGridBawah.setRowHeight(24);
        tbGridBawah.setEnabled(false);
        JScrollPane scrollGridBawah = new JScrollPane(tbGridBawah);
        scrollGridBawah.setPreferredSize(new Dimension(1100, 130));
        scrollGridBawah.setAlignmentX(Component.LEFT_ALIGNMENT);
        tengah.add(scrollGridBawah);
        tengah.add(Box.createVerticalStrut(14));

        // --- Tabel mentah riwayat CPPT (audit, sebelum di-snap ke slot) ---
        JLabel judulVital = new JLabel("Riwayat Tanda Vital dari SOAP/CPPT (data mentah, sebelum digabung ke grid)");
        judulVital.setFont(new Font("Tahoma", Font.BOLD, 13));
        judulVital.setForeground(teks);
        judulVital.setAlignmentX(Component.LEFT_ALIGNMENT);
        judulVital.setBorder(new EmptyBorder(0, 0, 6, 0));
        tengah.add(judulVital);

        tbVital.setModel(tabModeVital);
        tbVital.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbVital.setAutoResizeMode(widget.Table.AUTO_RESIZE_OFF);
        tbVital.setRowHeight(24);
        JScrollPane scrollVital = new JScrollPane(tbVital);
        scrollVital.setPreferredSize(new Dimension(1100, 130));
        scrollVital.setAlignmentX(Component.LEFT_ALIGNMENT);
        tengah.add(scrollVital);
        tengah.add(Box.createVerticalStrut(14));

        // --- Intake / Output harian ---
        JPanel kartuIO = kartu("Intake / Output Harian (manual -- tidak ada sumbernya di SOAP/CPPT)", teks, garis);
        int row = 0;
        row = pasanganVertikal(kartuIO, row, "Tanggal", dtpTanggalIO, "Per Oral", tPerOral);
        row = pasanganVertikal(kartuIO, row, "Parenteral", tParenteral, "Transfusi", tTransfusi);
        row = pasanganVertikal(kartuIO, row, "D.L.L", tDll, "Kemih", tKemih);
        row = pasanganVertikal(kartuIO, row, "Muntah", tMuntah, "Defekasi", tDefekasi);
        row = pasanganVertikal(kartuIO, row, "Berkemih", tBerkemih, "", new JLabel());
        row = tunggalVertikal(kartuIO, row, "Catatan", taCatatan);
        kartuIO.setAlignmentX(Component.LEFT_ALIGNMENT);
        tengah.add(kartuIO);
        tengah.add(Box.createVerticalStrut(8));

        JPanel panelTombolIO = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelTombolIO.setOpaque(false);
        panelTombolIO.setAlignmentX(Component.LEFT_ALIGNMENT);
        BtnSimpanIO.setText("Simpan Intake/Output");
        BtnHapusIO.setText("Hapus Tanggal Ini");
        BtnBersihkanIO.setText("Bersihkan Form Entri");
        panelTombolIO.add(BtnSimpanIO);
        panelTombolIO.add(BtnHapusIO);
        panelTombolIO.add(BtnBersihkanIO);
        tengah.add(panelTombolIO);
        tengah.add(Box.createVerticalStrut(10));

        JLabel judulIO = new JLabel("Riwayat Intake / Output");
        judulIO.setFont(new Font("Tahoma", Font.BOLD, 13));
        judulIO.setForeground(teks);
        judulIO.setAlignmentX(Component.LEFT_ALIGNMENT);
        judulIO.setBorder(new EmptyBorder(0, 0, 6, 0));
        tengah.add(judulIO);

        tbIO.setModel(tabModeIO);
        tbIO.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbIO.setAutoResizeMode(widget.Table.AUTO_RESIZE_OFF);
        tbIO.setRowHeight(24);
        tbIO.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) { muatBarisIOTerpilihKeForm(); }
            }
        });
        JScrollPane scrollIO = new JScrollPane(tbIO);
        scrollIO.setPreferredSize(new Dimension(1100, 160));
        scrollIO.setAlignmentX(Component.LEFT_ALIGNMENT);
        tengah.add(scrollIO);

        JScrollPane scrollTengah = new JScrollPane(tengah);
        scrollTengah.setBorder(null);
        scrollTengah.getVerticalScrollBar().setUnitIncrement(20);
        getContentPane().add(scrollTengah, BorderLayout.CENTER);

        BtnTampilkanGrafik.addActionListener(e -> {
            if (!ambil(TNoRw).equals("")) { muatGrafikDanVital(ambil(TNoRw)); }
        });
        BtnSimpanIO.addActionListener(e -> simpanIO());
        BtnHapusIO.addActionListener(e -> hapusIO());
        BtnBersihkanIO.addActionListener(e -> bersihkanEntriIO());
        BtnKeluar.setText("Keluar");
        BtnKeluar.addActionListener(e -> dispose());
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 9));
        bawah.setBackground(Color.WHITE);
        bawah.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, garis));
        bawah.add(BtnKeluar);
        getContentPane().add(bawah, BorderLayout.SOUTH);

        dtpTanggalIO.setDate(new Date());
    }

    public void isCek() {
        boolean bisa = akses.getpenilaian_awal_keperawatan_ranap();
        BtnSimpanIO.setEnabled(bisa);
        BtnHapusIO.setEnabled(bisa);
    }

    /** Dipanggil dari DlgRawatInap tab Penilaian Awal. */
    public void setNoRm(String norawat) {
        bersihkanEntriIO();
        if (norawat == null || norawat.trim().equals("")) {
            TNoRw.setText(""); TNoRM.setText(""); TPasien.setText(""); TJK.setText("");
            TTglLahir.setText(""); TRuangan.setText("");
            tabModeVital.setRowCount(0);
            tabModeIO.setRowCount(0);
            List<Kolom> kosong = new ArrayList<>();
            tampilkanGrafik(kosong);
            tampilkanGridBawah(kosong);
            return;
        }
        TNoRw.setText(norawat);
        tarikIdentitasPasien(norawat);
        muatGrafikDanVital(norawat);
        muatRiwayatIO(norawat);
    }

    private void tarikIdentitasPasien(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select p.no_rkm_medis,p.nm_pasien,p.jk,ifnull(date_format(p.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir "
                + "from reg_periksa rp inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis "
                + "where rp.no_rawat=?")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    TJK.setText("L".equalsIgnoreCase(rs.getString("jk")) ? "Laki-Laki" : "Perempuan");
                    TTglLahir.setText(rs.getString("tgl_lahir"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik identitas grafik ttv : " + e);
        }
        dtpSampai.setDate(new Date());
        dtpDari.setDate(new Date());
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select ifnull(bangsal.nm_bangsal,'') as ruang,ifnull(kamar.kelas,'') as kelas,kamar_inap.tgl_masuk "
                + "from kamar_inap inner join kamar on kamar.kd_kamar=kamar_inap.kd_kamar "
                + "inner join bangsal on bangsal.kd_bangsal=kamar.kd_bangsal "
                + "where kamar_inap.no_rawat=? order by kamar_inap.tgl_masuk desc limit 1")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String ruang = rs.getString("ruang");
                    String kelas = rs.getString("kelas");
                    TRuangan.setText((ruang == null ? "" : ruang) + (kelas != null && !kelas.isEmpty() ? " / " + kelas : ""));
                    if (rs.getDate("tgl_masuk") != null) {
                        setTanggal(dtpDari, rs.getString("tgl_masuk"));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik ruangan grafik ttv : " + e);
        }
    }

    /** Isi tabel mentah audit dari CPPT, lalu bangun grid (CPPT snap ke checkpoint) & gambar grafik + tabel bawah, sesuai filter rentang tanggal. */
    private void muatGrafikDanVital(String norawat) {
        String dari = ambilTanggal(dtpDari);
        String sampai = ambilTanggal(dtpSampai);
        if (dari.isEmpty() || sampai.isEmpty() || dari.compareTo(sampai) > 0) {
            JOptionPane.showMessageDialog(this, "Rentang tanggal tidak valid.");
            return;
        }

        tabModeVital.setRowCount(0);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select tgl_perawatan,jam_rawat,nadi,suhu_tubuh,respirasi,tensi,tinggi,berat "
                + "from pemeriksaan_ranap where no_rawat=? and tgl_perawatan between ? and ? "
                + "order by tgl_perawatan,jam_rawat")) {
            ps.setString(1, norawat);
            ps.setString(2, dari);
            ps.setString(3, sampai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tabModeVital.addRow(new Object[]{
                        fmtTanggal(rs.getString("tgl_perawatan")), nvl(rs.getString("jam_rawat")),
                        nvl(rs.getString("nadi")), nvl(rs.getString("suhu_tubuh")),
                        nvl(rs.getString("respirasi")), nvl(rs.getString("tensi")),
                        nvl(rs.getString("tinggi")), nvl(rs.getString("berat"))
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat riwayat cppt grafik ttv : " + e);
        }
        aturLebarKolomVital();

        List<Kolom> grid = bangunGrid(norawat, dari, sampai);
        tampilkanGrafik(grid);
        tampilkanGridBawah(grid);
    }

    /** Satu kolom grid (1 tanggal + 1 slot checkpoint 06/12/18/24), nilai dari CPPT (snap ke checkpoint terdekat). */
    private static final class Kolom {
        String tanggal;
        String slot;
        Double nadi;
        Double suhu;
        String respirasi = "";
        String tensi = "";
        String tinggi = "";
        String berat = "";

        String label() { return fmtTanggal(tanggal) + " " + slot; }
    }

    private static int slotTerdekat(int jamAktual) {
        int best = 0;
        int bestDiff = Integer.MAX_VALUE;
        for (int i = 0; i < SLOT_JAM.length; i++) {
            int diff = Math.abs(jamAktual - SLOT_JAM[i]);
            if (diff > 12) { diff = 24 - diff; }
            if (diff < bestDiff) { bestDiff = diff; best = i; }
        }
        return best;
    }

    /** Susun grid kronologis (tiap tanggal dalam rentang [dari,sampai] x 4 slot checkpoint) dari data CPPT (snap ke checkpoint terdekat). */
    private List<Kolom> bangunGrid(String norawat, String dari, String sampai) {
        Map<String, String[]> cppt = new HashMap<>();
        TreeSet<String> tanggalSet = new TreeSet<>();

        try (PreparedStatement ps = koneksi.prepareStatement(
                "select tgl_perawatan,jam_rawat,nadi,suhu_tubuh,respirasi,tensi,tinggi,berat "
                + "from pemeriksaan_ranap where no_rawat=? and tgl_perawatan between ? and ? "
                + "order by tgl_perawatan,jam_rawat")) {
            ps.setString(1, norawat);
            ps.setString(2, dari);
            ps.setString(3, sampai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tgl = rs.getString("tgl_perawatan");
                    String jam = rs.getString("jam_rawat");
                    if (tgl == null || jam == null || jam.length() < 2) { continue; }
                    int jamAktual;
                    try {
                        jamAktual = Integer.parseInt(jam.substring(0, 2));
                    } catch (Exception e) {
                        continue;
                    }
                    tanggalSet.add(tgl);
                    int idx = slotTerdekat(jamAktual);
                    cppt.put(tgl + "|" + idx, new String[]{
                        nvl(rs.getString("nadi")), nvl(rs.getString("suhu_tubuh")),
                        nvl(rs.getString("respirasi")), nvl(rs.getString("tensi")),
                        nvl(rs.getString("tinggi")), nvl(rs.getString("berat"))
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notif bangun grid cppt : " + e);
        }

        List<Kolom> hasil = new ArrayList<>();
        for (String tgl : tanggalSet) {
            for (int idx = 0; idx < SLOTS.length; idx++) {
                String key = tgl + "|" + idx;
                String[] v = cppt.get(key);
                Kolom k = new Kolom();
                k.tanggal = tgl;
                k.slot = SLOTS[idx];
                if (v != null) {
                    k.nadi = parseAngka(v[0]);
                    k.suhu = parseAngka(v[1]);
                    k.respirasi = v[2];
                    k.tensi = v[3];
                    k.tinggi = v[4];
                    k.berat = v[5];
                }
                hasil.add(k);
            }
        }
        return hasil;
    }

    private void aturLebarKolomVital() {
        if (tbVital.getColumnModel().getColumnCount() < 8) { return; }
        int[] lebar = {90, 70, 60, 60, 80, 100, 60, 60};
        for (int i = 0; i < lebar.length; i++) {
            tbVital.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
    }

    private void tampilkanGrafik(List<Kolom> grid) {
        DefaultCategoryDataset datasetNadi = new DefaultCategoryDataset();
        DefaultCategoryDataset datasetSuhu = new DefaultCategoryDataset();
        for (Kolom k : grid) {
            // semua kolom (termasuk yg kosong) tetap ditambahkan (nilai null diperbolehkan) supaya
            // sumbu-X grafik selalu sama persis dengan kolom tabel Respirasi/TD/BB di bawahnya.
            datasetNadi.addValue(k.nadi, "Nadi", k.label());
            datasetSuhu.addValue(k.suhu, "Suhu", k.label());
        }

        JFreeChart chart = ChartFactory.createLineChart(
                null, "Tanggal / Jam Checkpoint", "Nadi", datasetNadi, PlotOrientation.VERTICAL, true, true, false);
        CategoryPlot plot = chart.getCategoryPlot();

        LineAndShapeRenderer rendererNadi = new LineAndShapeRenderer(true, true);
        rendererNadi.setSeriesPaint(0, new Color(30, 70, 200));
        plot.setRenderer(0, rendererNadi);

        NumberAxis sumbuSuhu = new NumberAxis("Suhu (°C)");
        plot.setRangeAxis(1, sumbuSuhu);
        plot.setDataset(1, datasetSuhu);
        plot.mapDatasetToRangeAxis(1, 1);
        LineAndShapeRenderer rendererSuhu = new LineAndShapeRenderer(true, true);
        rendererSuhu.setSeriesPaint(0, new Color(200, 30, 30));
        plot.setRenderer(1, rendererSuhu);

        CategoryAxis sumbuTanggal = plot.getDomainAxis();
        sumbuTanggal.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        chart.setBackgroundPaint(Color.WHITE);
        plot.setBackgroundPaint(new Color(250, 250, 250));
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(new Color(210, 216, 222));
        plot.setRangeGridlinePaint(new Color(225, 230, 234));

        // set skala tetap TERAKHIR (setelah semua dataset/axis lain terpasang) --
        // kalau dipanggil lebih awal, penambahan dataset kedua bikin JFreeChart
        // re-autorange dan nilai setRange() sebelumnya ke-override.
        NumberAxis sumbuNadi = (NumberAxis) plot.getRangeAxis();
        sumbuNadi.setRange(40, 180);
        sumbuSuhu.setRange(35, 42);

        panelChart.removeAll();
        ChartPanel cp = new ChartPanel(chart);
        cp.setPreferredSize(new Dimension(100, 300));
        panelChart.add(cp, BorderLayout.CENTER);
        panelChart.revalidate();
        panelChart.repaint();
    }

    /** Tabel transposisi persis pola kertas: baris = Respirasi/Tekanan Darah/Tinggi/Berat, kolom = tanggal+slot. */
    private void tampilkanGridBawah(List<Kolom> grid) {
        Vector<String> headers = new Vector<>();
        headers.add("");
        for (Kolom k : grid) { headers.add(k.label()); }
        Vector<Vector<Object>> data = new Vector<>();
        data.add(barisGrid("Respirasi", grid, 0));
        data.add(barisGrid("Tekanan Darah", grid, 1));
        data.add(barisGrid("Tinggi", grid, 2));
        data.add(barisGrid("Berat", grid, 3));
        tabModeGridBawah.setDataVector(data, headers);
        if (tbGridBawah.getColumnModel().getColumnCount() > 0) {
            tbGridBawah.getColumnModel().getColumn(0).setPreferredWidth(110);
            for (int i = 1; i < tbGridBawah.getColumnModel().getColumnCount(); i++) {
                tbGridBawah.getColumnModel().getColumn(i).setPreferredWidth(75);
            }
        }
    }

    private Vector<Object> barisGrid(String label, List<Kolom> grid, int field) {
        Vector<Object> v = new Vector<>();
        v.add(label);
        for (Kolom k : grid) {
            switch (field) {
                case 0: v.add(k.respirasi); break;
                case 1: v.add(k.tensi); break;
                case 2: v.add(k.tinggi); break;
                default: v.add(k.berat); break;
            }
        }
        return v;
    }

    private void muatRiwayatIO(String norawat) {
        tabModeIO.setRowCount(0);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select * from grafik_intake_output where no_rawat=? order by tanggal")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tglIso = rs.getString("tanggal");
                    tabModeIO.addRow(new Object[]{
                        tglIso, fmtTanggal(tglIso),
                        nvl(rs.getString("per_oral")), nvl(rs.getString("parenteral")),
                        nvl(rs.getString("transfusi")), nvl(rs.getString("dll")),
                        nvl(rs.getString("kemih")), nvl(rs.getString("muntah")),
                        nvl(rs.getString("defekasi")), nvl(rs.getString("berkemih")),
                        nvl(rs.getString("catatan"))
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat riwayat intake/output : " + e);
        }
        aturLebarKolomIO();
    }

    private void aturLebarKolomIO() {
        if (tbIO.getColumnModel().getColumnCount() < 11) { return; }
        int[] lebar = {0, 90, 70, 80, 70, 60, 60, 60, 70, 70, 220};
        for (int i = 0; i < lebar.length; i++) {
            tbIO.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
        tbIO.getColumnModel().getColumn(0).setMinWidth(0);
        tbIO.getColumnModel().getColumn(0).setMaxWidth(0);
    }

    private void muatBarisIOTerpilihKeForm() {
        int r = tbIO.getSelectedRow();
        if (r < 0) { return; }
        setTanggal(dtpTanggalIO, tabModeIO.getValueAt(r, 0) + "");
        tPerOral.setText(tabModeIO.getValueAt(r, 2) + "");
        tParenteral.setText(tabModeIO.getValueAt(r, 3) + "");
        tTransfusi.setText(tabModeIO.getValueAt(r, 4) + "");
        tDll.setText(tabModeIO.getValueAt(r, 5) + "");
        tKemih.setText(tabModeIO.getValueAt(r, 6) + "");
        tMuntah.setText(tabModeIO.getValueAt(r, 7) + "");
        tDefekasi.setText(tabModeIO.getValueAt(r, 8) + "");
        tBerkemih.setText(tabModeIO.getValueAt(r, 9) + "");
        taCatatan.setText(tabModeIO.getValueAt(r, 10) + "");
    }

    private void simpanIO() {
        if (ambil(TNoRw).equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        String tgl = ambilTanggal(dtpTanggalIO);
        if (tgl.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi tanggal terlebih dahulu.");
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "replace into grafik_intake_output (no_rawat,tanggal,per_oral,parenteral,transfusi,dll,"
                + "kemih,muntah,defekasi,berkemih,catatan,created_by,created_at) "
                + "values (?,?,?,?,?,?,?,?,?,?,?,?,now())")) {
            ps.setString(1, ambil(TNoRw));
            ps.setString(2, tgl);
            ps.setString(3, ambil(tPerOral));
            ps.setString(4, ambil(tParenteral));
            ps.setString(5, ambil(tTransfusi));
            ps.setString(6, ambil(tDll));
            ps.setString(7, ambil(tKemih));
            ps.setString(8, ambil(tMuntah));
            ps.setString(9, ambil(tDefekasi));
            ps.setString(10, ambil(tBerkemih));
            ps.setString(11, taCatatan.getText() == null ? "" : taCatatan.getText().trim());
            ps.setString(12, akses.getkode());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data intake/output tersimpan.");
            bersihkanEntriIO();
            muatRiwayatIO(ambil(TNoRw));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan.\n" + e.getMessage());
        }
    }

    private void hapusIO() {
        String tgl = ambilTanggal(dtpTanggalIO);
        if (ambil(TNoRw).equals("") || tgl.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih baris di tabel riwayat terlebih dahulu (klik dua kali).");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus data intake/output tanggal " + Valid.SetTgl3(tgl) + " ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "delete from grafik_intake_output where no_rawat=? and tanggal=?")) {
            ps.setString(1, ambil(TNoRw));
            ps.setString(2, tgl);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data dihapus.");
            bersihkanEntriIO();
            muatRiwayatIO(ambil(TNoRw));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus.\n" + e.getMessage());
        }
    }

    private void bersihkanEntriIO() {
        for (widget.TextBox t : new widget.TextBox[]{tPerOral, tParenteral, tTransfusi, tDll,
            tKemih, tMuntah, tDefekasi, tBerkemih}) {
            t.setText("");
        }
        taCatatan.setText("");
        dtpTanggalIO.setDate(new Date());
    }

    private void ensureTable() {
        Sequel.queryu2(
                "create table if not exists grafik_intake_output ("
                + "no_rawat varchar(17) not null,"
                + "tanggal date not null,"
                + "per_oral varchar(20) null,"
                + "parenteral varchar(20) null,"
                + "transfusi varchar(20) null,"
                + "dll varchar(20) null,"
                + "kemih varchar(20) null,"
                + "muntah varchar(20) null,"
                + "defekasi varchar(20) null,"
                + "berkemih varchar(20) null,"
                + "catatan varchar(500) null,"
                + "created_by varchar(50) null,"
                + "created_at datetime null,"
                + "primary key (no_rawat,tanggal)"
                + ")");
    }

    // ====================== Helpers data ======================
    private static Double parseAngka(String v) {
        if (v == null) { return null; }
        String s = v.trim().replace(",", ".");
        if (s.isEmpty()) { return null; }
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static String fmtTanggal(String iso) {
        if (iso == null || iso.length() < 10) { return ""; }
        return iso.substring(8, 10) + "-" + iso.substring(5, 7) + "-" + iso.substring(0, 4);
    }

    private String ambilTanggal(widget.Tanggal d) {
        Object v = d.getSelectedItem();
        if (v == null) { return ""; }
        String s = v.toString();
        return s.length() >= 10 ? Valid.SetTgl(s.substring(0, 10)) : "";
    }

    private static void setTanggal(widget.Tanggal d, String iso) {
        if (iso == null || iso.trim().isEmpty() || iso.equals("null")) { d.setDate(new Date()); return; }
        try {
            d.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(iso.trim()));
        } catch (Exception e) {
            d.setDate(new Date());
        }
    }

    // ====================== Helpers UI ======================
    private static widget.TextBox tf() { return new widget.TextBox(); }

    private static widget.TextBox ro() {
        widget.TextBox t = new widget.TextBox();
        t.setEditable(false);
        return t;
    }

    private static widget.TextArea ta() {
        widget.TextArea t = new widget.TextArea();
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        t.setRows(3);
        return t;
    }

    private static widget.ComboBox cmb(String... items) {
        widget.ComboBox c = new widget.ComboBox();
        for (String it : items) { c.addItem(it); }
        return c;
    }

    private static widget.Tanggal dtTanggal() {
        widget.Tanggal d = new widget.Tanggal();
        d.setDisplayFormat("dd-MM-yyyy");
        return d;
    }

    private JPanel fieldRingkasan(String label, Component komponen) {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(0, 10, 0, 10));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Tahoma", Font.PLAIN, 10));
        l.setForeground(label.contains("*") ? new Color(190, 35, 35) : new Color(86, 101, 112));
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
                BorderFactory.createLineBorder(garis),
                new EmptyBorder(8, 12, 12, 12)));
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

    private static String ambil(widget.TextBox t) {
        return t.getText() == null ? "" : t.getText().trim();
    }

    private static String nvl(String v) {
        return v == null ? "" : v;
    }
}
