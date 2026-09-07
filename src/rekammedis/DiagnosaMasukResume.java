package rekammedis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Sumber diagnosis masuk kedua resume V2: episode rawat yang sama di Kamar Inap. */
final class DiagnosaMasukResume {
    private DiagnosaMasukResume() {}

    static String isiJikaKosong(Connection koneksi, String noRawat, String isiResume) throws SQLException {
        if (isiResume != null && !isiResume.trim().isEmpty()) return isiResume;
        if (noRawat == null || noRawat.trim().isEmpty()) return "";
        // Jika pindah kamar, ambil diagnosis terisi dari awal episode masuk.
        // Jangan memakai riwayat kunjungan lain walaupun nomor RM sama.
        try (PreparedStatement stmt = koneksi.prepareStatement(
                "select diagnosa_awal from kamar_inap where no_rawat=? " +
                "and diagnosa_awal is not null and trim(diagnosa_awal)<>'' " +
                "order by tgl_masuk asc,jam_masuk asc,kd_kamar asc limit 1")) {
            stmt.setString(1, noRawat.trim());
            try (ResultSet hasil = stmt.executeQuery()) {
                return hasil.next() ? hasil.getString(1) : "";
            }
        }
    }
}
