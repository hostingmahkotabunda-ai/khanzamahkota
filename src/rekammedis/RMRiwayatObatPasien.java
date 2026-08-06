package rekammedis;

import fungsi.koneksiDB;
import fungsi.sekuel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.table.DefaultTableModel;

/**
 * Panel "Riwayat Obat" yang di-embed sebagai tab di DlgRawatJalan. Menampilkan
 * SEMUA riwayat resep pasien (lintas no_rawat -- IGD/Ralan/Ranap manapun,
 * selama no_rkm_medis sama) sbg daftar kunjungan (master, 1 baris per
 * no_rawat) di atas -- BUKAN langsung tampil semua obat sekaligus, biar tidak
 * kebanjiran data. Klik 1 baris kunjungan -> baru muncul detail obat/racikan
 * kunjungan itu di tabel bawah. Murni read-only (tidak ada simpan/hapus).
 */
public final class RMRiwayatObatPasien extends JPanel {

    private final sekuel Sequel = new sekuel();
    private final Connection koneksi = koneksiDB.condb();

    private String noRkmMedis = "";

    private final DefaultTableModel tabModeKunjungan = new DefaultTableModel(null,
            new Object[]{"No. Rawat", "Tanggal", "Unit/Poli", "Dokter"}) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final DefaultTableModel tabModeObat = new DefaultTableModel(null,
            new Object[]{"No. Resep", "Jam", "Nama Obat/Racikan", "Jumlah", "Satuan", "Aturan Pakai"}) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final widget.Table tbKunjungan = new widget.Table();
    private final widget.Table tbObat = new widget.Table();

    public RMRiwayatObatPasien() {
        setLayout(new BorderLayout());
        setOpaque(false);

        tbKunjungan.setModel(tabModeKunjungan);
        tbKunjungan.setRowHeight(24);
        tbKunjungan.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { tampilkanDetailKunjungan(); }
        });
        JScrollPane scrollKunjungan = new JScrollPane(tbKunjungan);
        scrollKunjungan.setBorder(javax.swing.BorderFactory.createTitledBorder("Daftar Kunjungan (klik untuk lihat obat)"));

        tbObat.setModel(tabModeObat);
        tbObat.setRowHeight(22);
        JScrollPane scrollObat = new JScrollPane(tbObat);
        scrollObat.setBorder(javax.swing.BorderFactory.createTitledBorder("Obat / Racikan pada Kunjungan Terpilih"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollKunjungan, scrollObat);
        split.setResizeWeight(0.45);
        split.setContinuousLayout(true);
        add(split, BorderLayout.CENTER);
    }

    /** Dipanggil setiap kali tab "Riwayat Obat" ini jadi aktif/dipilih. */
    public void setKonteks(String norm) {
        if (norm == null) { norm = ""; }
        if (norm.equals(noRkmMedis)) { return; }
        noRkmMedis = norm;
        tabModeObat.setRowCount(0);
        muatDaftarKunjungan();
    }

    private void muatDaftarKunjungan() {
        tabModeKunjungan.setRowCount(0);
        if (noRkmMedis.trim().isEmpty()) { return; }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select resep_obat.no_rawat,min(resep_obat.tgl_peresepan) as tgl,"
                + "min(ifnull(poliklinik.nm_poli,'')) as unit,min(ifnull(dokter.nm_dokter,'')) as dokter "
                + "from resep_obat inner join reg_periksa on resep_obat.no_rawat=reg_periksa.no_rawat "
                + "left join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "
                + "left join dokter on resep_obat.kd_dokter=dokter.kd_dokter "
                + "where reg_periksa.no_rkm_medis=? and resep_obat.tgl_peresepan<>'0000-00-00' "
                + "group by resep_obat.no_rawat order by tgl desc")) {
            ps.setString(1, noRkmMedis);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tabModeKunjungan.addRow(new Object[]{
                        rs.getString("no_rawat"), fmtTgl(rs.getString("tgl")),
                        rs.getString("unit"), rs.getString("dokter")
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat daftar kunjungan riwayat obat : " + e);
        }
    }

    private void tampilkanDetailKunjungan() {
        int row = tbKunjungan.getSelectedRow();
        tabModeObat.setRowCount(0);
        if (row == -1) { return; }
        String norawat = tbKunjungan.getValueAt(row, 0).toString();
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select no_resep,jam_peresepan from resep_obat "
                + "where no_rawat=? and tgl_peresepan<>'0000-00-00' order by tgl_peresepan,jam_peresepan")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String noResep = rs.getString("no_resep");
                    String jam = nvl(rs.getString("jam_peresepan"));
                    muatObatNonRacikan(noResep, jam);
                    muatObatRacikan(noResep, jam);
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat detail obat kunjungan : " + e);
        }
    }

    private void muatObatNonRacikan(String noResep, String jam) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select databarang.nama_brng,resep_dokter.jml,databarang.kode_sat,resep_dokter.aturan_pakai "
                + "from resep_dokter inner join databarang on resep_dokter.kode_brng=databarang.kode_brng "
                + "where resep_dokter.no_resep=? order by databarang.nama_brng")) {
            ps.setString(1, noResep);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tabModeObat.addRow(new Object[]{
                        noResep, jam, rs.getString("nama_brng"), rs.getString("jml"),
                        rs.getString("kode_sat"), nvl(rs.getString("aturan_pakai"))
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat obat non-racikan : " + e);
        }
    }

    private void muatObatRacikan(String noResep, String jam) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select resep_dokter_racikan.no_racik,resep_dokter_racikan.nama_racik,"
                + "resep_dokter_racikan.jml_dr,resep_dokter_racikan.aturan_pakai,metode_racik.nm_racik as metode "
                + "from resep_dokter_racikan left join metode_racik on resep_dokter_racikan.kd_racik=metode_racik.kd_racik "
                + "where resep_dokter_racikan.no_resep=? order by resep_dokter_racikan.no_racik")) {
            ps.setString(1, noResep);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String noRacik = rs.getString("no_racik");
                    tabModeObat.addRow(new Object[]{
                        noResep, jam, rs.getString("nama_racik"), rs.getString("jml_dr"),
                        nvl(rs.getString("metode")), nvl(rs.getString("aturan_pakai"))
                    });
                    muatIsiRacikan(noResep, noRacik);
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat obat racikan : " + e);
        }
    }

    private void muatIsiRacikan(String noResep, String noRacik) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select databarang.nama_brng,resep_dokter_racikan_detail.jml,databarang.kode_sat "
                + "from resep_dokter_racikan_detail inner join databarang on resep_dokter_racikan_detail.kode_brng=databarang.kode_brng "
                + "where resep_dokter_racikan_detail.no_resep=? and resep_dokter_racikan_detail.no_racik=? order by databarang.nama_brng")) {
            ps.setString(1, noResep);
            ps.setString(2, noRacik);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tabModeObat.addRow(new Object[]{
                        "", "", "    " + rs.getString("nama_brng"), rs.getString("jml"), rs.getString("kode_sat"), ""
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat isi racikan : " + e);
        }
    }

    private static String fmtTgl(String iso) {
        if (iso == null || iso.length() < 10) { return ""; }
        return iso.substring(8, 10) + "-" + iso.substring(5, 7) + "-" + iso.substring(0, 4);
    }

    private static String nvl(String v) {
        return v == null ? "" : v;
    }
}
