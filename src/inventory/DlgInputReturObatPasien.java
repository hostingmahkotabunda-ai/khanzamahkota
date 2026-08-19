package inventory;

import fungsi.WarnaTable;
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import simrskhanza.DlgCariBangsal;

/**
 * Transaksi Retur Obat, Alkes &amp; BHP Medis dari Pasien -- halaman BARU
 * (2026-08-15), khusus utk retur yg terikat pasien/no_rawat tertentu.
 * Dibuka SELALU dgn {@link #tampilkan(String, String)} sebelum
 * {@code setVisible(true)}, dari DlgBilingRanap.java / DlgKamarInap.java.
 *
 * PENTING (2026-08-15): nama class ini SEBELUMNYA "DlgReturObatPasien" --
 * TERNYATA nama itu SUDAH DIPAKAI class LAIN yg SUDAH ADA sejak awal project
 * (halaman laporan retur obat ranap dari tabel returpasien, dipanggil dari
 * menu utama/frmUtama.java "Retur Obat Ranap"). Class ini DIGANTI NAMA jadi
 * DlgInputReturObatPasien supaya tidak lagi bentrok/menimpa file itu.
 *
 * Beda dgn DlgReturJual.java (halaman lama, TETAP DIPAKAI utk retur
 * non-pasien/supplier dari menu utama -- lihat frmUtama.java, TIDAK disentuh):
 * di sini seluruh obat yg pernah diberikan ke pasien LANGSUNG tampil di
 * grid, dikelompokkan per ruangan fisik (bukan per lokasi apotek -- lihat
 * catatan di {@link #ambilRiwayatKamar}), tinggal centang + isi jumlah,
 * tidak ada lagi picker+Tambah satu-satu.
 *
 * Alur simpan (insert returjual/detreturjual, update data_batch.sisa &amp;
 * gudangbarang.stok, catat riwayatobat) di-port APA ADANYA dari
 * DlgReturJual.java lama yg sudah terbukti benar -- lihat method simpan().
 */
public final class DlgInputReturObatPasien extends JDialog {

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final riwayatobat Trackobat = new riwayatobat();
    private final DlgCariBangsal bangsalPicker = new DlgCariBangsal(null, false);

    private String norm = "";
    private String norawat = "";
    private String kdgudang = "";

    private final widget.TextBox TNoRetur = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TPetugas = ro();
    private final widget.TextBox TLokasi = ro();
    private final widget.Tanggal dtpTanggal = dt();
    private final widget.ComboBox cmbJenis = cmb(
            "Rawat Jalan", "Kelas 1", "Kelas 2", "Kelas 3", "Utama/BPJS", "VIP", "VVIP",
            "Beli Luar", "Jual Bebas", "Karyawan", "Harga Beli");

    private final DefaultTableModel tabMode;
    private final widget.Table tbObat = new widget.Table();
    private final JLabel lblTotal = new JLabel("Total Retur : 0");
    private final widget.Button BtnSimpan = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();

    private static final String RUANGAN_MARKER = " RUANGAN_HEADER";
    private static final String RESEP_MARKER = " RESEP_HEADER";
    private static final int KOLOM_CATATAN = 21;
    private String aktifkanbatch = "no";

    public DlgInputReturObatPasien(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Retur Obat, Alkes & BHP Medis dari Pasien ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        try {
            aktifkanbatch = koneksiDB.AKTIFKANBATCHOBAT();
        } catch (Exception e) {
            aktifkanbatch = "no";
        }
        ensureKolomCatatan();

        tabMode = new DefaultTableModel(null, new Object[]{
                "Retur?", "Kode Barang", "Nama Barang", "Satuan", "Jml Dimiliki", "Hrg.Retur(Rp)",
                "Jml.Retur", "Total Retur(Rp)", "No.Batch", "No.Faktur",
                "Rawat Jalan", "Kelas 1", "Kelas 2", "Kelas 3", "Utama/BPJS", "VIP", "VVIP",
                "Beli Luar", "Jual Bebas", "Karyawan", "Harga Beli", "Catatan"
            }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                Object marker = getValueAt(rowIndex, 1);
                if (RUANGAN_MARKER.equals(marker) || RESEP_MARKER.equals(marker)) { return false; }
                return colIndex == 0 || colIndex == 5 || colIndex == 6 || colIndex == KOLOM_CATATAN;
            }
            Class[] types = new Class[]{
                java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class,
                java.lang.String.class, java.lang.String.class,
                java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class,
                java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class,
                java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.String.class
            };
            @Override
            public Class getColumnClass(int columnIndex) { return types[columnIndex]; }
        };
        final boolean[] sedangHitungUlang = {false};
        tabMode.addTableModelListener(evt -> {
            if (sedangHitungUlang[0]) { return; }
            int col = evt.getColumn();
            if ((col == 5 || col == 6) && evt.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = evt.getFirstRow();
                if (row < 0 || row >= tabMode.getRowCount()) { return; }
                Object marker = tabMode.getValueAt(row, 1);
                if (RUANGAN_MARKER.equals(marker) || RESEP_MARKER.equals(marker)) { return; }
                double hrg = ambilDouble(tabMode.getValueAt(row, 5));
                double jml = ambilDouble(tabMode.getValueAt(row, 6));
                sedangHitungUlang[0] = true;
                try {
                    tabMode.setValueAt(hrg * jml, row, 7);
                } finally {
                    sedangHitungUlang[0] = false;
                }
                hitungTotal();
            }
        });

        initComponents();
        setSize(1000, 650);
        setMinimumSize(new Dimension(820, 480));
    }

    private void initComponents() {
        final Color latar = new Color(246, 249, 251);
        final Color garis = new Color(215, 224, 230);
        final Color teks = new Color(32, 49, 66);
        getContentPane().setBackground(latar);
        getContentPane().setLayout(new BorderLayout(0, 8));

        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));
        int row = 0;
        row = baris2(header, row, "No. Retur", TNoRetur, "Pasien", TPasien);
        row = baris2(header, row, "Petugas", TPetugas, "Lokasi", TLokasi);
        row = barisKomboTanggal(header, row, "Jenis", cmbJenis, "Tanggal", dtpTanggal);
        getContentPane().add(header, BorderLayout.NORTH);

        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(900, 420));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbObat.setRowHeight(22);
        int[] lebar = {45, 90, 220, 60, 80, 90, 70, 90, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 200};
        RuanganHeaderRenderer renderer = new RuanganHeaderRenderer();
        for (int i = 0; i < tbObat.getColumnModel().getColumnCount(); i++) {
            TableColumn kolom = tbObat.getColumnModel().getColumn(i);
            if (lebar[i] == 0) {
                kolom.setMinWidth(0);
                kolom.setMaxWidth(0);
                kolom.setPreferredWidth(0);
            } else {
                kolom.setPreferredWidth(lebar[i]);
            }
            kolom.setCellRenderer(renderer);
        }
        JScrollPane scroll = new JScrollPane(tbObat);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        getContentPane().add(scroll, BorderLayout.CENTER);

        lblTotal.setFont(new Font("Tahoma", Font.BOLD, 12));
        BtnSimpan.setText("Simpan");
        BtnSimpan.addActionListener(e -> simpan());
        BtnKeluar.setText("Keluar");
        BtnKeluar.addActionListener(e -> dispose());
        JPanel bawah = new JPanel(new BorderLayout());
        bawah.setOpaque(false);
        bawah.setBorder(BorderFactory.createEmptyBorder(6, 12, 10, 12));
        bawah.add(lblTotal, BorderLayout.WEST);
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tombol.setOpaque(false);
        tombol.add(BtnSimpan);
        tombol.add(BtnKeluar);
        bawah.add(tombol, BorderLayout.EAST);
        getContentPane().add(bawah, BorderLayout.SOUTH);

        cmbJenis.setSelectedIndex(10);
        cmbJenis.addActionListener(e -> refreshHarga());
    }

    /** Dipanggil pemanggil (DlgBilingRanap/DlgKamarInap) SEBELUM setVisible(true). */
    public void tampilkan(String normParam, String norawatParam) {
        this.norm = normParam;
        this.norawat = norawatParam;
        Valid.autoNomer3("select ifnull(MAX(CONVERT(RIGHT(no_retur_jual,2),signed)),0) from returjual where no_retur_jual like '%"+norawat+"%' ", norawat, 2, TNoRetur);
        Sequel.cariIsi("select pasien.nm_pasien from pasien where pasien.no_rkm_medis=?", TPasien, norm);
        TPetugas.setText(akses.getkode() + " - " + Sequel.cariIsi("select nama from petugas where nip='"+akses.getkode()+"'"));
        kdgudang = akses.getkdbangsal();
        TLokasi.setText(kdgudang + " - " + bangsalPicker.tampil3(kdgudang));
        dtpTanggal.setDate(new java.util.Date());
        tampilkanObat();
    }

    // ====================== Grid obat per ruangan ======================

    /** Satu periode pasien menempati 1 kamar (dari kamar_inap), batas waktu "yyyy-MM-dd HH:mm:ss" (string ISO, bisa dibandingkan langsung). */
    private static final class Okupansi {
        String ruangan;
        String mulai;
        String selesai;
    }

    /** Baris obat hasil akumulasi per (ruangan,waktu pemberian,kode_brng,no_batch,no_faktur) sebelum ditulis ke grid. */
    private static final class BarisObatRuangan {
        int idxRuangan;
        String namaRuangan;
        String waktu;
        String kodeBrng, namaBrng, satuan, noBatch, noFaktur;
        double jmlDimiliki = 0;
        double[] hargaTier;
    }

    /** Render baris header ruangan/resep sbg 1 baris highlight, kolom lain dikosongkan; kolom angka ditulis bulat (tanpa desimal). */
    private static final class RuanganHeaderRenderer extends WarnaTable {
        private static final java.text.DecimalFormat FORMAT_ANGKA = new java.text.DecimalFormat("###,###,###,###,###,###,###");

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Object marker = null;
            try {
                marker = table.getModel().getValueAt(row, 1);
            } catch (Exception ignore) { }
            if (RUANGAN_MARKER.equals(marker)) {
                Object tampil = column == 2 ? value : "";
                Component c = super.getTableCellRendererComponent(table, tampil, false, false, row, column);
                c.setBackground(new Color(222, 234, 246));
                c.setForeground(new Color(30, 60, 90));
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                return c;
            }
            if (RESEP_MARKER.equals(marker)) {
                Object tampil = column == 2 ? value : "";
                Component c = super.getTableCellRendererComponent(table, tampil, false, false, row, column);
                c.setBackground(new Color(238, 243, 247));
                c.setForeground(new Color(70, 90, 105));
                c.setFont(c.getFont().deriveFont(Font.ITALIC));
                return c;
            }
            if (value instanceof Double && (column == 4 || column == 5 || column == 6 || column == 7)) {
                Component c = super.getTableCellRendererComponent(table, FORMAT_ANGKA.format((Double) value), isSelected, hasFocus, row, column);
                ((JLabel) c).setHorizontalAlignment(JLabel.RIGHT);
                return c;
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    /**
     * Riwayat kamar pasien (no_rawat), urut kronologis -- dipakai utk
     * menentukan ruangan FISIK pasien saat suatu obat diberikan.
     * PENTING: detail_pemberian_obat.kd_bangsal TERNYATA cuma nyatet lokasi
     * apotek/pengeluaran obat (SELALU "Apotek"), BUKAN kamar pasien -- jadi
     * TIDAK dipakai di sini. Ruangan di-resolve dari kamar_inap dicocokkan ke
     * tgl_perawatan+jam tiap obat. Pola query sama dgn RMRiwayatKamarPasien.java.
     */
    private List<Okupansi> ambilRiwayatKamar(String norawatParam) {
        List<Okupansi> hasil = new ArrayList<>();
        try (PreparedStatement pst = koneksi.prepareStatement(
                "select bangsal.nm_bangsal,kamar_inap.tgl_masuk,kamar_inap.jam_masuk,"
                + "kamar_inap.tgl_keluar,kamar_inap.jam_keluar,kamar_inap.stts_pulang "
                + "from kamar_inap inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar "
                + "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal "
                + "where kamar_inap.no_rawat=? order by kamar_inap.tgl_masuk,kamar_inap.jam_masuk")) {
            pst.setString(1, norawatParam);
            try (ResultSet r = pst.executeQuery()) {
                while (r.next()) {
                    Okupansi o = new Okupansi();
                    o.ruangan = r.getString("nm_bangsal");
                    o.mulai = r.getDate("tgl_masuk") + " " + r.getTime("jam_masuk");
                    java.sql.Date tglKeluar = r.getDate("tgl_keluar");
                    java.sql.Time jamKeluar = r.getTime("jam_keluar");
                    if (tglKeluar == null || "-".equals(r.getString("stts_pulang"))) {
                        o.selesai = "9999-12-31 23:59:59";
                    } else {
                        o.selesai = tglKeluar + " " + (jamKeluar == null ? "23:59:59" : jamKeluar.toString());
                    }
                    hasil.add(o);
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi ambilRiwayatKamar : " + e);
        }
        return hasil;
    }

    /** Cari index okupansi kamar yg mencakup "waktu" ("yyyy-MM-dd HH:mm:ss"); -1 kalau tidak ketemu (mis. obat rawat jalan / sebelum masuk kamar). */
    private int cariIndexRuangan(List<Okupansi> riwayat, String waktu) {
        for (int i = 0; i < riwayat.size(); i++) {
            Okupansi o = riwayat.get(i);
            if (waktu.compareTo(o.mulai) >= 0 && waktu.compareTo(o.selesai) <= 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Label sumber utk obat yg diberikan SEBELUM/TANPA kamar_inap (mis. di IGD sblm
     * keputusan rawat inap) -- pola sama persis {@code sumberResep()} di DlgCopyResep.java:
     * baca poliklinik asal dari reg_periksa.kd_poli, kalau namanya mengandung IGD/UGD/
     * GAWAT DARURAT dianggap "IGD", selain itu "Rawat Jalan".
     */
    private String sumberTanpaKamar(String norawatParam) {
        String poli = Sequel.cariIsi(
                "select ifnull(poliklinik.nm_poli,'') from reg_periksa "
                + "left join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "
                + "where reg_periksa.no_rawat=?", norawatParam);
        String nama = poli == null ? "" : poli.toUpperCase();
        if (nama.contains("IGD") || nama.contains("UGD") || nama.contains("GAWAT DARURAT")) {
            return "IGD";
        }
        return "Rawat Jalan";
    }

    private void tampilkanObat() {
        Valid.tabelKosong(tabMode);
        if (norawat.trim().equals("")) { return; }
        List<Okupansi> riwayatKamar = ambilRiwayatKamar(norawat);
        String labelTanpaKamar = sumberTanpaKamar(norawat);
        LinkedHashMap<String, BarisObatRuangan> akumulasi = new LinkedHashMap<>();
        try {
            PreparedStatement ps;
            if (aktifkanbatch.equals("yes")) {
                ps = koneksi.prepareStatement(
                        "select databarang.kode_brng,databarang.nama_brng,kodesatuan.satuan,detail_pemberian_obat.jml,"
                        + "detail_pemberian_obat.tgl_perawatan,detail_pemberian_obat.jam,detail_pemberian_obat.status as status_obat,"
                        + "detail_pemberian_obat.no_batch,detail_pemberian_obat.no_faktur,"
                        + "data_batch.ralan,data_batch.kelas1,data_batch.kelas2,data_batch.kelas3,data_batch.utama,"
                        + "databarang.vip,data_batch.vvip,data_batch.beliluar,data_batch.jualbebas,data_batch.karyawan,data_batch.h_beli "
                        + "from data_batch inner join databarang on data_batch.kode_brng=databarang.kode_brng "
                        + "inner join kodesatuan on databarang.kode_sat=kodesatuan.kode_sat "
                        + "inner join detail_pemberian_obat on detail_pemberian_obat.kode_brng=data_batch.kode_brng "
                        + "and detail_pemberian_obat.no_batch=data_batch.no_batch and detail_pemberian_obat.no_faktur=data_batch.no_faktur "
                        + "where detail_pemberian_obat.no_rawat=? "
                        + "order by detail_pemberian_obat.tgl_perawatan,detail_pemberian_obat.jam");
            } else {
                ps = koneksi.prepareStatement(
                        "select databarang.kode_brng,databarang.nama_brng,kodesatuan.satuan,detail_pemberian_obat.jml,"
                        + "detail_pemberian_obat.tgl_perawatan,detail_pemberian_obat.jam,detail_pemberian_obat.status as status_obat,"
                        + "'' as no_batch,'' as no_faktur,"
                        + "databarang.ralan,databarang.kelas1,databarang.kelas2,databarang.kelas3,databarang.utama,"
                        + "databarang.vip,databarang.vvip,databarang.beliluar,databarang.jualbebas,databarang.karyawan,databarang.h_beli "
                        + "from databarang inner join kodesatuan on databarang.kode_sat=kodesatuan.kode_sat "
                        + "inner join detail_pemberian_obat on detail_pemberian_obat.kode_brng=databarang.kode_brng "
                        + "where detail_pemberian_obat.no_rawat=? "
                        + "order by detail_pemberian_obat.tgl_perawatan,detail_pemberian_obat.jam");
            }
            try (ResultSet rs = withParam(ps, norawat)) {
                while (rs.next()) {
                    String waktu = rs.getDate("tgl_perawatan") + " " + rs.getTime("jam");
                    String statusObat = rs.getString("status_obat");
                    // status='Ralan' (spt di resep_obat, dipakai Copy Resep) -- obat blm terkait rawat inap,
                    // gak dianggap masuk kamar fisik meski waktu-nya kebetulan sdh sesudah tgl masuk kamar_inap.
                    boolean ralan = "Ralan".equalsIgnoreCase(statusObat);
                    int idxRuangan = ralan ? -1 : cariIndexRuangan(riwayatKamar, waktu);
                    String namaRuangan = idxRuangan < 0 ? labelTanpaKamar : riwayatKamar.get(idxRuangan).ruangan;
                    String noBatch = rs.getString("no_batch");
                    String noFaktur = rs.getString("no_faktur");
                    String kunci = idxRuangan + "" + waktu + "" + rs.getString("kode_brng") + "" + noBatch + "" + noFaktur;
                    BarisObatRuangan b = akumulasi.get(kunci);
                    if (b == null) {
                        b = new BarisObatRuangan();
                        b.idxRuangan = idxRuangan;
                        b.namaRuangan = namaRuangan;
                        b.waktu = waktu;
                        b.kodeBrng = rs.getString("kode_brng");
                        b.namaBrng = rs.getString("nama_brng");
                        b.satuan = rs.getString("satuan");
                        b.noBatch = noBatch;
                        b.noFaktur = noFaktur;
                        b.hargaTier = new double[]{
                            rs.getDouble("ralan"), rs.getDouble("kelas1"), rs.getDouble("kelas2"), rs.getDouble("kelas3"),
                            rs.getDouble("utama"), rs.getDouble("vip"), rs.getDouble("vvip"), rs.getDouble("beliluar"),
                            rs.getDouble("jualbebas"), rs.getDouble("karyawan"), rs.getDouble("h_beli")
                        };
                        akumulasi.put(kunci, b);
                    }
                    b.jmlDimiliki += rs.getDouble("jml");
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi tampilkanObat : " + e);
            return;
        }

        List<BarisObatRuangan> daftar = new ArrayList<>(akumulasi.values());
        daftar.sort((a, b) -> {
            int c = Integer.compare(a.idxRuangan, b.idxRuangan);
            if (c != 0) { return c; }
            c = a.waktu.compareTo(b.waktu);
            return c != 0 ? c : a.namaBrng.compareToIgnoreCase(b.namaBrng);
        });

        int hiddenCol = 10 + cmbJenis.getSelectedIndex();
        String ruanganSblm = null;
        String waktuSblm = null;
        for (BarisObatRuangan b : daftar) {
            if (!Objects.equals(b.namaRuangan, ruanganSblm)) {
                tabMode.addRow(new Object[]{
                    false, RUANGAN_MARKER, "Ruang : " + b.namaRuangan, "", 0.0, 0.0, 0.0, 0.0, "", "",
                    0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ""
                });
                ruanganSblm = b.namaRuangan;
                waktuSblm = null;
            }
            if (!Objects.equals(b.waktu, waktuSblm)) {
                tabMode.addRow(new Object[]{
                    false, RESEP_MARKER, "     Pemberian Obat : " + formatWaktu(b.waktu), "", 0.0, 0.0, 0.0, 0.0, "", "",
                    0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ""
                });
                waktuSblm = b.waktu;
            }
            Object[] baris = new Object[]{
                false, b.kodeBrng, b.namaBrng, b.satuan, b.jmlDimiliki, 0.0, 0.0, 0.0, b.noBatch, b.noFaktur,
                b.hargaTier[0], b.hargaTier[1], b.hargaTier[2], b.hargaTier[3], b.hargaTier[4],
                b.hargaTier[5], b.hargaTier[6], b.hargaTier[7], b.hargaTier[8], b.hargaTier[9], b.hargaTier[10], ""
            };
            baris[5] = ambilDouble(baris[hiddenCol]);
            tabMode.addRow(baris);
        }
        hitungTotal();
    }

    /** "yyyy-MM-dd HH:mm:ss" -> "dd-MM-yyyy HH:mm:ss" utk label header per-resep. */
    private static String formatWaktu(String waktuIso) {
        try {
            String[] bagian = waktuIso.split(" ");
            String[] tgl = bagian[0].split("-");
            return tgl[2] + "-" + tgl[1] + "-" + tgl[0] + " " + (bagian.length > 1 ? bagian[1] : "");
        } catch (Exception e) {
            return waktuIso;
        }
    }

    private static ResultSet withParam(PreparedStatement ps, String norawat) throws Exception {
        ps.setString(1, norawat);
        return ps.executeQuery();
    }

    private boolean isHeaderRow(int row) {
        Object marker = tabMode.getValueAt(row, 1);
        return RUANGAN_MARKER.equals(marker) || RESEP_MARKER.equals(marker);
    }

    private void refreshHarga() {
        int hiddenCol = 10 + cmbJenis.getSelectedIndex();
        for (int row = 0; row < tabMode.getRowCount(); row++) {
            if (isHeaderRow(row)) { continue; }
            tabMode.setValueAt(ambilDouble(tabMode.getValueAt(row, hiddenCol)), row, 5);
        }
    }

    private void hitungTotal() {
        double total = 0;
        for (int row = 0; row < tabMode.getRowCount(); row++) {
            if (isHeaderRow(row)) { continue; }
            if (Boolean.TRUE.equals(tabMode.getValueAt(row, 0))) {
                total += ambilDouble(tabMode.getValueAt(row, 7));
            }
        }
        lblTotal.setText("Total Retur : " + Valid.SetAngka(total));
    }

    // ====================== Simpan ======================

    /**
     * Validasi semua baris tercentang, lalu simpan LANGSUNG ke returjual/detreturjual
     * (bukan lewat tabel staging tampreturjual spt halaman lama -- grid ITU SENDIRI
     * sudah representasi final yg mau disimpan, jadi staging tidak perlu lagi).
     * Alur & tabel yg ditulis identik dgn DlgReturJual.java lama (BtnSimpanActionPerformed+simpan())
     * yg sudah terbukti benar -- TIDAK posting tampjurnal krn itu cuma utk retur non-pasien.
     */
    private void simpan() {
        if (norawat.trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Pasien belum dipilih.");
            return;
        }
        List<Integer> barisValid = new ArrayList<>();
        for (int i = 0; i < tabMode.getRowCount(); i++) {
            if (isHeaderRow(i)) { continue; }
            if (!Boolean.TRUE.equals(tabMode.getValueAt(i, 0))) { continue; }
            String nama = String.valueOf(tabMode.getValueAt(i, 2));
            double jml = ambilDouble(tabMode.getValueAt(i, 6));
            double dimiliki = ambilDouble(tabMode.getValueAt(i, 4));
            if (jml <= 0) {
                JOptionPane.showMessageDialog(this, "Jumlah retur \""+nama+"\" harus diisi lebih dari 0.");
                return;
            }
            if (jml > dimiliki) {
                JOptionPane.showMessageDialog(this, "Jumlah retur \""+nama+"\" ("+Valid.SetAngka(jml)+") melebihi jumlah yang dimiliki pasien ("+Valid.SetAngka(dimiliki)+").");
                return;
            }
            barisValid.add(i);
        }
        if (barisValid.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih minimal 1 obat yang mau diretur (centang \"Retur?\" & isi jumlahnya).");
            return;
        }
        if (TNoRetur.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "No. Retur belum terisi.");
            return;
        }
        int reply = JOptionPane.showConfirmDialog(this, "Yakin ingin menyimpan retur obat ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (reply != JOptionPane.YES_OPTION) { return; }

        boolean sukses = true;
        Sequel.AutoComitFalse();
        try {
            sukses = Sequel.menyimpantf2("returjual", "'"+TNoRetur.getText()+"','"+ambilTgl(dtpTanggal)+"','"+akses.getkode()+"','"+norm+"','"+kdgudang+"',''", "data");
            if (sukses) {
                for (int i : barisValid) {
                    String kodeBrng = String.valueOf(tabMode.getValueAt(i, 1));
                    String satuan = String.valueOf(tabMode.getValueAt(i, 3));
                    double hrgRetur = ambilDouble(tabMode.getValueAt(i, 5));
                    double jmlRetur = ambilDouble(tabMode.getValueAt(i, 6));
                    double subtotal = ambilDouble(tabMode.getValueAt(i, 7));
                    String noBatch = String.valueOf(tabMode.getValueAt(i, 8));
                    String noFaktur = String.valueOf(tabMode.getValueAt(i, 9));
                    String catatan = ambilCatatanBaris(i);

                    boolean okDetail = Sequel.menyimpantf2("detreturjual", "?,?,?,?,?,?,?,?,?,?,?,?", "data barang sama", 12, new String[]{
                        TNoRetur.getText(), "", kodeBrng, satuan, "0", "0",
                        String.valueOf(jmlRetur), String.valueOf(hrgRetur), String.valueOf(subtotal), noBatch, noFaktur, catatan
                    });
                    if (!okDetail) { sukses = false; break; }

                    if (aktifkanbatch.equals("yes")) {
                        Sequel.mengedit("data_batch", "no_batch=? and kode_brng=? and no_faktur=?", "sisa=sisa+?", 4, new String[]{
                            String.valueOf(jmlRetur), noBatch, kodeBrng, noFaktur
                        });
                        Trackobat.catatRiwayat(kodeBrng, jmlRetur, 0, "Retur Jual", akses.getkode(), kdgudang, "Simpan", noBatch, noFaktur, TNoRetur.getText()+" "+norm+" "+TPasien.getText());
                        Sequel.menyimpan("gudangbarang", "'"+kodeBrng+"','"+kdgudang+"','"+jmlRetur+"','"+noBatch+"','"+noFaktur+"'",
                                "stok=stok+'"+jmlRetur+"'", "kode_brng='"+kodeBrng+"' and kd_bangsal='"+kdgudang+"' and no_batch='"+noBatch+"' and no_faktur='"+noFaktur+"'");
                    } else {
                        Trackobat.catatRiwayat(kodeBrng, jmlRetur, 0, "Retur Jual", akses.getkode(), kdgudang, "Simpan", "", "", TNoRetur.getText()+" "+norm+" "+TPasien.getText());
                        Sequel.menyimpan("gudangbarang", "'"+kodeBrng+"','"+kdgudang+"','"+jmlRetur+"','',''",
                                "stok=stok+'"+jmlRetur+"'", "kode_brng='"+kodeBrng+"' and kd_bangsal='"+kdgudang+"' and no_batch='' and no_faktur=''");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi simpan retur obat pasien : " + e);
            sukses = false;
        }
        if (sukses) {
            Sequel.Commit();
        } else {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat pemrosesan data, transaksi dibatalkan.\nPeriksa kembali data sebelum melanjutkan menyimpan..!!");
            Sequel.RollBack();
        }
        Sequel.AutoComitTrue();
        if (sukses) {
            JOptionPane.showMessageDialog(this, "Retur obat berhasil disimpan.");
            Valid.autoNomer3("select ifnull(MAX(CONVERT(RIGHT(no_retur_jual,2),signed)),0) from returjual where no_retur_jual like '%"+norawat+"%' ", norawat, 2, TNoRetur);
            tampilkanObat();
        }
    }

    // ====================== Helpers ======================

    private static double ambilDouble(Object v) {
        if (v == null) { return 0; }
        try {
            return Double.parseDouble(v.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private String ambilTgl(widget.Tanggal d) {
        Date tgl = d.getDate();
        return tgl == null ? "" : new java.text.SimpleDateFormat("yyyy-MM-dd").format(tgl);
    }

    /** Catatan per baris obat (kolom KOLOM_CATATAN), diketik langsung di grid. */
    private String ambilCatatanBaris(int row) {
        Object v = tabMode.getValueAt(row, KOLOM_CATATAN);
        return v == null ? "" : v.toString().trim();
    }

    private static widget.TextBox ro() {
        widget.TextBox t = new widget.TextBox();
        t.setEditable(false);
        return t;
    }

    private static widget.Tanggal dt() {
        widget.Tanggal d = new widget.Tanggal();
        d.setDisplayFormat("dd-MM-yyyy");
        return d;
    }

    private static widget.ComboBox cmb(String... opsi) {
        widget.ComboBox c = new widget.ComboBox();
        for (String o : opsi) { c.addItem(o); }
        return c;
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t + " :");
        l.setFont(new Font("Tahoma", Font.PLAIN, 11));
        return l;
    }

    private GridBagConstraints gc(int x, int y, int w, double weightx) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x; g.gridy = y; g.gridwidth = w;
        g.weightx = weightx;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(3, 3, 3, 3);
        g.anchor = GridBagConstraints.WEST;
        return g;
    }

    private void siz(Component c) {
        c.setPreferredSize(new Dimension(220, 23));
    }

    private int baris2(JPanel p, int row, String label1, Component f1, String label2, Component f2) {
        p.add(lbl(label1), gc(0, row, 1, 0.0));
        siz(f1);
        p.add(f1, gc(1, row, 1, 1.0));
        p.add(lbl(label2), gc(2, row, 1, 0.0));
        siz(f2);
        p.add(f2, gc(3, row, 1, 1.0));
        return row + 1;
    }

    private int barisKomboTanggal(JPanel p, int row, String label1, widget.ComboBox combo, String label2, widget.Tanggal tgl) {
        p.add(lbl(label1), gc(0, row, 1, 0.0));
        combo.setPreferredSize(new Dimension(220, 23));
        p.add(combo, gc(1, row, 1, 1.0));
        p.add(lbl(label2), gc(2, row, 1, 0.0));
        tgl.setPreferredSize(new Dimension(150, 23));
        p.add(tgl, gc(3, row, 1, 1.0));
        return row + 1;
    }

    /**
     * Kolom catatan per-item ditambah ke detreturjual (BUKAN returjual lagi --
     * lihat revisi 2026-08-19, catatan dipindah dari level header/transaksi
     * ke level per-baris obat). ALTER manual krn table detreturjual sudah ada
     * di instalasi lama. Kolom returjual.catatan (dari revisi sebelumnya)
     * dibiarkan menganggur di skema, tidak dipakai lagi & tidak di-DROP.
     */
    private void ensureKolomCatatan() {
        try {
            if (Sequel.cariInteger("select count(*) from information_schema.columns where table_schema=database() "
                    + "and table_name='detreturjual' and column_name='catatan'") == 0) {
                Sequel.queryu2("alter table detreturjual add column catatan text null after no_faktur");
            }
        } catch (Exception e) {
            System.out.println("Notif kolom catatan detreturjual : " + e);
        }
    }
}
