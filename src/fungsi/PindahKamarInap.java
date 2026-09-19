package fungsi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/** Menyimpan seluruh perubahan pindah kamar dalam satu transaksi. */
public final class PindahKamarInap {
    private static final String SUMBER_AKTIF =
            " where no_rawat=? and kd_kamar=? and tgl_masuk=? and jam_masuk=? and stts_pulang='-'";

    private PindahKamarInap() {
    }

    /**
     * Koneksi khusus operasi ini disediakan dan ditutup oleh pemanggil.
     * Identitas asal berasal dari snapshot sebelum perhitungan biaya dilakukan.
     */
    public static void simpan(Connection koneksi, int mode, String noRawat, String[] asal,
            String kamarTujuan, String tarifBaru, String diagnosaAwal, String diagnosaAkhir,
            String tglPindah, String jamPindah, String lamaBaru, String biayaBaru,
            String tarifLama, String lamaLama, String biayaLama) throws SQLException {
        if (mode < 1 || mode > 4) {
            throw validasi("Pilih cara pemindahan kamar terlebih dahulu.");
        }
        if (noRawat == null || noRawat.trim().isEmpty() || asal == null || asal.length != 3
                || asal[0] == null || asal[1] == null || asal[2] == null) {
            throw validasi("Data kamar asal tidak lengkap. Muat ulang data pasien.");
        }
        if (kamarTujuan == null || kamarTujuan.trim().isEmpty()) {
            throw validasi("Pilih kamar tujuan terlebih dahulu.");
        }
        if (kamarTujuan.equals(asal[0])) {
            throw validasi("Kamar tujuan harus berbeda dengan kamar asal.");
        }
        if (!koneksi.getAutoCommit()) {
            throw validasi("Koneksi pemindahan kamar sedang digunakan oleh transaksi lain.");
        }

        koneksi.setAutoCommit(false);
        try {
            periksaKamarAsal(koneksi, noRawat, asal);
            if (mode != 2) {
                periksaWaktuPindah(asal, tglPindah, jamPindah);
            }
            periksaKamarTujuan(koneksi, kamarTujuan);

            if (mode == 2) {
                ubahSatuBaris(koneksi,
                        "update kamar_inap set kd_kamar=?,trf_kamar=?,lama=?,ttl_biaya=?" + SUMBER_AKTIF,
                        kamarTujuan, tarifBaru, lamaBaru, biayaBaru,
                        noRawat, asal[0], asal[1], asal[2]);
            } else {
                // Jika insert gagal, segmen asal belum ditutup/dihapus.
                ubahSatuBaris(koneksi,
                        "insert into kamar_inap (no_rawat,kd_kamar,trf_kamar,diagnosa_awal,diagnosa_akhir,"
                        + "tgl_masuk,jam_masuk,tgl_keluar,jam_keluar,lama,ttl_biaya,stts_pulang) "
                        + "values (?,?,?,?,?,?,?,?,?,?,?,?)",
                        noRawat, kamarTujuan, tarifBaru, diagnosaAwal, diagnosaAkhir,
                        tglPindah, jamPindah, "0000-00-00", "00:00:00", lamaBaru, biayaBaru, "-");
                if (mode == 1) {
                    ubahSatuBaris(koneksi, "delete from kamar_inap" + SUMBER_AKTIF,
                            noRawat, asal[0], asal[1], asal[2]);
                } else {
                    ubahSatuBaris(koneksi,
                            "update kamar_inap set trf_kamar=?,tgl_keluar=?,jam_keluar=?,lama=?,ttl_biaya=?,"
                            + "stts_pulang='Pindah Kamar'" + SUMBER_AKTIF,
                            tarifLama, tglPindah, jamPindah, lamaLama, biayaLama,
                            noRawat, asal[0], asal[1], asal[2]);
                }
            }

            ubahSatuBaris(koneksi, "update kamar set status='ISI' where kd_kamar=? and status='KOSONG'",
                    kamarTujuan);
            ubahSatuBaris(koneksi, "update kamar set status='KOSONG' where kd_kamar=?", asal[0]);
            koneksi.commit();
        } catch (SQLException gagal) {
            batalkan(koneksi, gagal);
            throw gagal;
        } catch (RuntimeException gagal) {
            batalkan(koneksi, gagal);
            throw gagal;
        }

        // Commit sudah berhasil; kegagalan pemulihan koneksi tidak boleh disebut rollback.
        try {
            koneksi.setAutoCommit(true);
        } catch (SQLException gagal) {
            throw new SQLException("Pemindahan kamar sudah tersimpan, tetapi koneksi tidak dapat dipulihkan. "
                    + "Muat ulang daftar pasien sebelum mengulangi.", "01000", gagal);
        }
    }

    private static void periksaKamarAsal(Connection koneksi, String noRawat, String[] asal) throws SQLException {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select kd_kamar,tgl_masuk,jam_masuk from kamar_inap "
                + "where no_rawat=? and stts_pulang='-' for update")) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !asal[0].equals(rs.getString(1))
                        || !asal[1].equals(rs.getString(2)) || !asal[2].equals(rs.getString(3))) {
                    throw validasi("Kamar aktif pasien sudah berubah. Muat ulang data pasien sebelum memindahkan kamar.");
                }
                if (rs.next()) {
                    throw validasi("Pasien mempunyai lebih dari satu kamar aktif. Periksa data kamar pasien terlebih dahulu.");
                }
            }
        }
    }

    private static void periksaKamarTujuan(Connection koneksi, String kamarTujuan) throws SQLException {
        try (PreparedStatement ps = koneksi.prepareStatement("select status from kamar where kd_kamar=? for update")) {
            ps.setString(1, kamarTujuan);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !"KOSONG".equals(rs.getString(1))) {
                    throw validasi("Kamar tujuan sudah tidak tersedia. Pilih kamar kosong dan coba lagi.");
                }
            }
        }
    }

    private static void periksaWaktuPindah(String[] asal, String tanggal, String jam) throws SQLException {
        try {
            Timestamp masuk = Timestamp.valueOf(asal[1] + " " + asal[2]);
            Timestamp pindah = Timestamp.valueOf(tanggal + " " + jam);
            if (!pindah.after(masuk)) {
                throw validasi("Tanggal dan jam pindah harus setelah tanggal dan jam masuk kamar asal.");
            }
        } catch (IllegalArgumentException gagal) {
            throw validasi("Tanggal atau jam pemindahan kamar tidak valid.");
        }
    }

    private static void ubahSatuBaris(Connection koneksi, String sql, String... parameter) throws SQLException {
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            for (int i = 0; i < parameter.length; i++) {
                ps.setString(i + 1, parameter[i]);
            }
            if (ps.executeUpdate() != 1) {
                throw validasi("Data kamar sudah berubah. Pemindahan dibatalkan; muat ulang data pasien.");
            }
        }
    }

    private static SQLException validasi(String pesan) {
        return new SQLException(pesan, "45000");
    }

    private static void batalkan(Connection koneksi, Exception gagal) {
        try {
            koneksi.rollback();
        } catch (SQLException gagalRollback) {
            gagal.addSuppressed(gagalRollback);
            // setAutoCommit(true) dapat melakukan commit; jangan panggil jika rollback gagal.
            return;
        }
        try {
            koneksi.setAutoCommit(true);
        } catch (SQLException gagalPemulihan) {
            gagal.addSuppressed(gagalPemulihan);
        }
    }
}
