package rekammedis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Membaca tindakan penanganan pada satu kunjungan, tanpa mengubah data sumber. */
final class TindakanResume {
    private TindakanResume() { }

    static List<Object[]> baca(Connection koneksi, String noRawat, boolean ranap) throws SQLException {
        List<Object[]> hasil = new ArrayList<Object[]>();
        if (noRawat == null || noRawat.trim().isEmpty()) {
            return hasil;
        }
        // Nama tabel hanya berasal dari pilihan internal, nomor rawat selalu parameter JDBC.
        String tabel = ranap ? "rawat_inap_" : "rawat_jl_";
        String master = ranap ? "jns_perawatan_inap" : "jns_perawatan";
        String sql =
            "select r.tgl_perawatan,r.jam_rawat,coalesce(d.nm_dokter,r.kd_dokter) as pelaksana,j.nm_perawatan " +
            "from " + tabel + "dr r inner join " + master + " j on j.kd_jenis_prw=r.kd_jenis_prw " +
            "left join dokter d on d.kd_dokter=r.kd_dokter where r.no_rawat=? " +
            "union all " +
            "select r.tgl_perawatan,r.jam_rawat,coalesce(p.nama,r.nip) as pelaksana,j.nm_perawatan " +
            "from " + tabel + "pr r inner join " + master + " j on j.kd_jenis_prw=r.kd_jenis_prw " +
            "left join petugas p on p.nip=r.nip where r.no_rawat=? " +
            "union all " +
            "select r.tgl_perawatan,r.jam_rawat," +
            "concat(concat(coalesce(d.nm_dokter,r.kd_dokter),' / '),coalesce(p.nama,r.nip)) as pelaksana,j.nm_perawatan " +
            "from " + tabel + "drpr r inner join " + master + " j on j.kd_jenis_prw=r.kd_jenis_prw " +
            "left join dokter d on d.kd_dokter=r.kd_dokter left join petugas p on p.nip=r.nip where r.no_rawat=? " +
            "order by tgl_perawatan desc,jam_rawat desc,nm_perawatan,pelaksana";
        try (PreparedStatement stmt = koneksi.prepareStatement(sql)) {
            for (int i = 1; i <= 3; i++) {
                stmt.setString(i, noRawat);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new Object[]{Boolean.FALSE, rs.getString("tgl_perawatan"),
                        rs.getString("jam_rawat"), rs.getString("pelaksana"), rs.getString("nm_perawatan")});
                }
            }
        }
        return hasil;
    }
}
