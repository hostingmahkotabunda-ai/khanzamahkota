package rekammedis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

/** Uji SQL dengan database sementara dalam memori; tidak mengakses database aplikasi. */
public class ResumeDiagnosaMasukTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void kamar(Connection koneksi, String noRawat, String diagnosis,
            String tanggal, String jam, String kamar) throws Exception {
        try (PreparedStatement stmt = koneksi.prepareStatement("insert into kamar_inap values(?,?,?,?,?)")) {
            stmt.setString(1, noRawat); stmt.setString(2, diagnosis);
            stmt.setDate(3, java.sql.Date.valueOf(tanggal));
            stmt.setString(4, jam); stmt.setString(5, kamar);
            stmt.executeUpdate();
        }
    }

    private static Connection dialekUji(Connection koneksi) {
        // HSQLDB 1.8 bawaan proyek menulis LIMIT di awal SELECT dan TRIM lengkap.
        // Hanya sintaks dialek yang disesuaikan; filter/parameter/urutan tetap.
        return (Connection)java.lang.reflect.Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class}, (proxy, method, args) -> {
                    if (method.getName().equals("prepareStatement")) {
                        String sql = (String)args[0];
                        check(sql.startsWith("select ") && sql.endsWith(" limit 1"), "Query harus dibatasi satu baris");
                        args[0] = ("select limit 0 1 " + sql.substring(7, sql.length() - " limit 1".length()))
                                .replace("trim(diagnosa_awal)", "trim(both from diagnosa_awal)");
                    }
                    try { return method.invoke(koneksi, args); }
                    catch (java.lang.reflect.InvocationTargetException e) { throw e.getCause(); }
                });
    }

    public static void main(String[] args) throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");
        try (Connection koneksi = DriverManager.getConnection("jdbc:hsqldb:mem:resume_diagnosa_test", "sa", "")) {
            Connection baca = dialekUji(koneksi);
            try (Statement stmt = koneksi.createStatement()) {
                stmt.execute("create table kamar_inap (no_rawat varchar(100), diagnosa_awal varchar(255), " +
                        "tgl_masuk date, jam_masuk varchar(8), kd_kamar varchar(20))");
            }
            // Urutan insert sengaja berbeda dari tanggal/jam masuk.
            kamar(koneksi, "RAWAT-A", "Diagnosis sesudah pindah", "2026-09-02", "08:00:00", "K2");
            kamar(koneksi, "RAWAT-B", "Diagnosis episode lain", "2026-09-01", "07:00:00", "K1");
            kamar(koneksi, "RAWAT-A", "Diagnosis awal", "2026-09-01", "09:00:00", "K1");
            kamar(koneksi, "RAWAT-A", "Diagnosis jam berikutnya", "2026-09-01", "10:00:00", "K3");
            kamar(koneksi, "RAWAT-A", "  ", "2026-09-01", "08:00:00", "K0");
            kamar(koneksi, "RAWAT-A", null, "2026-09-01", "07:00:00", "K0");
            kamar(koneksi, "KOSONG", null, "2026-09-01", "07:00:00", "K0");
            kamar(koneksi, "KOSONG", "  ", "2026-09-01", "08:00:00", "K0");
            check("Diagnosis awal".equals(DiagnosaMasukResume.isiJikaKosong(baca, "RAWAT-A", "")),
                    "Diagnosis awal hilang/terganti diagnosis pindah kamar");
            check("Diagnosis episode lain".equals(DiagnosaMasukResume.isiJikaKosong(baca, "RAWAT-B", null)),
                    "Diagnosis pasien sebelumnya terbawa");
            check("Diagnosis awal".equals(DiagnosaMasukResume.isiJikaKosong(baca, " RAWAT-A ", "  ")),
                    "Isian kosong tidak terisi");
            check("".equals(DiagnosaMasukResume.isiJikaKosong(baca, "RALAN-TANPA-RANAP", "")),
                    "Kunjungan tanpa Kamar Inap mengambil diagnosis kunjungan lain");
            check("".equals(DiagnosaMasukResume.isiJikaKosong(baca, "KOSONG", "")), "NULL/spasi tidak kosong");
            check("".equals(DiagnosaMasukResume.isiJikaKosong(baca, "' OR '1'='1", "")),
                    "Parameter nomor rawat tidak dibatasi secara tepat");
            check(" Diagnosis dokter ".equals(DiagnosaMasukResume.isiJikaKosong(null, "RAWAT-A", " Diagnosis dokter ")),
                    "Diagnosis resume tersimpan/hasil edit tertimpa atau masih mengakses DB");
            check("".equals(DiagnosaMasukResume.isiJikaKosong(null, " ", "")), "Nomor rawat kosong mengakses DB");
            try (Statement stmt = koneksi.createStatement();
                    java.sql.ResultSet hasil = stmt.executeQuery("select count(*) from kamar_inap")) {
                hasil.next(); check(hasil.getInt(1) == 8, "Data sumber berubah");
            }
        }
        System.out.println("PASS: diagnosis awal per nomor rawat, pindah kamar, resume terisi, data kosong, dan parameter SQL");
    }
}
