package rekammedis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

/** Integrasi SQL pada database memori, tanpa koneksi ke database pasien. */
public class ResumeTindakanTest {
    private static void check(boolean kondisi, String pesan) {
        if (!kondisi) throw new AssertionError(pesan);
    }

    private static void tambah(Connection c, String tabel, String rawat, String jam, String dokter) throws Exception {
        try (PreparedStatement s = c.prepareStatement("insert into " + tabel + " values(?,?,'2026-10-31',?,?,'P1')")) {
            s.setString(1, rawat);
            s.setString(2, "T1");
            s.setString(3, jam);
            s.setString(4, dokter);
            s.executeUpdate();
        }
    }

    public static void main(String[] args) throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");
        try (Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:resume_tindakan", "sa", "")) {
            try (Statement s = c.createStatement()) {
                s.execute("create table dokter(kd_dokter varchar(20),nm_dokter varchar(100))");
                s.execute("create table petugas(nip varchar(20),nama varchar(100))");
                s.execute("insert into dokter values('D1','Dokter Uji')");
                s.execute("insert into petugas values('P1','Petugas Uji')");
                for (String master : new String[]{"jns_perawatan", "jns_perawatan_inap"}) {
                    s.execute("create table " + master + "(kd_jenis_prw varchar(20),nm_perawatan varchar(100))");
                    s.execute("insert into " + master + " values('T1','" + master + "')");
                }
                for (String awalan : new String[]{"rawat_jl_", "rawat_inap_"}) {
                    for (String jenis : new String[]{"dr", "pr", "drpr"}) {
                        s.execute("create table " + awalan + jenis + "(no_rawat varchar(100),kd_jenis_prw varchar(20)," +
                            "tgl_perawatan date,jam_rawat varchar(8),kd_dokter varchar(20),nip varchar(20))");
                    }
                }
            }
            for (String awalan : new String[]{"rawat_jl_", "rawat_inap_"}) {
                tambah(c, awalan + "dr", "A", "08:00:00", "D1");
                tambah(c, awalan + "pr", "A", "09:00:00", "D1");
                tambah(c, awalan + "drpr", "A", "10:00:00", "D1");
                tambah(c, awalan + "dr", "B", "11:00:00", "D1");
            }
            for (boolean ranap : new boolean[]{false, true}) {
                List<Object[]> rows = TindakanResume.baca(c, "A", ranap);
                check(rows.size() == 3, "Tiga sumber harus terbaca, kunjungan lain tidak boleh tercampur");
                check("Dokter Uji / Petugas Uji".equals(rows.get(0)[3]), "Pelaksana gabungan dan urutan terbaru");
                check("Petugas Uji".equals(rows.get(1)[3]), "Sumber petugas");
                check("Dokter Uji".equals(rows.get(2)[3]), "Sumber dokter");
                check((ranap ? "jns_perawatan_inap" : "jns_perawatan").equals(rows.get(0)[4]), "Master tindakan sesuai layanan");
                check(TindakanResume.baca(c, "TIDAK ADA", ranap).isEmpty(), "Data kosong");
                check(TindakanResume.baca(c, "A' OR '1'='1", ranap).isEmpty(), "Nomor rawat harus parameter");
            }
            tambah(c, "rawat_jl_dr", "A", "12:00:00", "D-HILANG");
            check("D-HILANG".equals(TindakanResume.baca(c, "A", false).get(0)[3]), "Tindakan tetap tampil saat master pelaksana tidak ada");
            check(TindakanResume.baca(c, "A", true).size() == 3, "Layanan tidak boleh tercampur");
            check(TindakanResume.baca(c, "  ", false).isEmpty(), "Nomor rawat kosong");
            check(TindakanResume.baca(c, null, true).isEmpty(), "Nomor rawat null");
        }
        System.out.println("ResumeTindakanTest: semua pengujian lulus.");
    }
}
