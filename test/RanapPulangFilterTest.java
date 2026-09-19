import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/** Uji SQL filter pulang dengan data sintetis HSQLDB in-memory, tanpa database SIMRS. */
public class RanapPulangFilterTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void add(Connection connection, String rawat, String rm, String kamar,
            String status, String keluar) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(
                "insert into kamar_inap values (?,?,?,?,?)")) {
            ps.setString(1, rawat);
            ps.setString(2, rm);
            ps.setString(3, kamar);
            ps.setString(4, status);
            ps.setString(5, keluar);
            ps.executeUpdate();
        }
    }

    private static Set<String> rows(Connection connection, String condition, String date) throws Exception {
        Set<String> rows = new LinkedHashSet<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "select no_rawat,kd_kamar from kamar_inap where " + condition
                + " and tgl_keluar between ? and ? order by no_rawat,kd_kamar")) {
            ps.setString(1, date);
            ps.setString(2, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(rs.getString(1) + "|" + rs.getString(2));
            }
        }
        return rows;
    }

    public static void main(String[] args) throws Exception {
        Method filter = Class.forName("simrskhanza.DlgKamarInap")
                .getDeclaredMethod("kondisiPasienPulangRanap");
        filter.setAccessible(true);
        String condition = (String) filter.invoke(null);
        Class.forName("org.hsqldb.jdbcDriver");
        try (Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:ranap_pulang_filter", "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("create table kamar_inap (no_rawat varchar(30),no_rkm_medis varchar(30),"
                    + "kd_kamar varchar(30),stts_pulang varchar(30),tgl_keluar varchar(10))");

            // Kamar asal mempunyai tanggal keluar meski pasien tetap dirawat di kamar tujuan.
            add(connection, "TRANSFER_DONE", "RM01", "OLD", "Pindah Kamar", "2026-09-19");
            add(connection, "TRANSFER_DONE", "RM01", "NEW", "-", null);
            // Data transfer lama yang sudah tidak mempunyai segmen aktif tetap bukan pulang.
            add(connection, "TRANSFER_MISSING", "RM02", "OLD", "Pindah Kamar", "2026-09-19");
            add(connection, "AFTER_TRANSFER", "RM03", "OLD", "Pindah Kamar", "2026-09-18");
            add(connection, "AFTER_TRANSFER", "RM03", "NEW", "Membaik", "2026-09-19");
            add(connection, "DISCHARGED", "RM04", "ROOM", "Sehat", "2026-09-19");
            // Segmen aktif mengalahkan status pulang sebelumnya untuk kunjungan yang sama.
            add(connection, "REOPENED", "RM05", "OLD", "Sehat", "2026-09-19");
            add(connection, "REOPENED", "RM05", "NEW", "-", "2026-09-19");
            // Kunjungan baru untuk RM yang sama tidak menghapus riwayat pulang kunjungan lama.
            add(connection, "PRIOR_VISIT", "RM06", "ROOM", "Rujuk", "2026-09-19");
            add(connection, "CURRENT_VISIT", "RM06", "ROOM", "-", null);
            add(connection, "OUTSIDE_DATE", "RM07", "ROOM", "APS", "2026-09-01");

            Set<String> expected = new LinkedHashSet<>(Arrays.asList(
                    "AFTER_TRANSFER|NEW", "DISCHARGED|ROOM", "PRIOR_VISIT|ROOM"));
            check(rows(connection, "1=1", "2026-09-19").size() > expected.size(),
                    "Fixture harus mereproduksi salah klasifikasi pada filter tanggal lama");
            check(rows(connection, condition, "2026-09-19").equals(expected),
                    "Transfer/kunjungan aktif masuk daftar pulang atau pulang sebenarnya hilang");
            check(rows(connection, condition, "2026-09-18").isEmpty(),
                    "Tanggal pindah kamar dianggap tanggal pemulangan");
            check(rows(connection, condition, "2026-09-01").equals(
                    new LinkedHashSet<>(Arrays.asList("OUTSIDE_DATE|ROOM"))),
                    "Filter tanggal pulang berubah");
            statement.execute("shutdown");
        }
        System.out.println("PASS RanapPulangFilterTest: transfer, active stays, discharge and visit/date boundaries");
    }
}
