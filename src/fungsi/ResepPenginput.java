package fungsi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Metadata pembuat resep; tidak mengubah transaksi maupun data resep utama. */
public final class ResepPenginput {
    private ResepPenginput() { }

    public static String nama(Connection koneksi, String id) {
        if ("Admin Utama".equals(id)) {
            return id;
        }
        String[] queries = {
            "select nama from pegawai where nik=?",
            "select nama from petugas where nip=?",
            "select nm_dokter from dokter where kd_dokter=?"
        };
        for (String query : queries) {
            try (PreparedStatement ps = koneksi.prepareStatement(query)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String nama = rs.getString(1);
                        if (nama != null && !nama.trim().isEmpty()) {
                            return nama;
                        }
                    }
                }
            } catch (SQLException e) {
                System.out.println("Lookup nama penginput resep: " + e.getSQLState());
            }
        }
        return "Nama petugas belum tersedia";
    }

    /** Hanya dipanggil untuk resep baru, setelah transaksi resep utama selesai. */
    public static boolean simpan(Connection koneksi, String noResep, String id, String nama) {
        try {
            if (!koneksi.getAutoCommit() || id == null || id.trim().isEmpty()) {
                return false;
            }
            // INSERT biasa: pemilik awal tidak boleh ditimpa, termasuk ketika terjadi duplikasi.
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "insert into resep_penginput (no_resep,id_penginput,nama_penginput,waktu_input) "
                    + "values (?,?,?,now())")) {
                ps.setString(1, noResep);
                ps.setString(2, id);
                ps.setString(3, nama);
                return ps.executeUpdate() == 1;
            }
        } catch (SQLException e) {
            System.out.println("Pencatatan penginput resep gagal: " + e.getSQLState());
            return false;
        }
    }

    public static String informasi(Connection koneksi, String noResep) {
        return informasi(koneksi, noResep, true);
    }

    public static String namaTersimpan(Connection koneksi, String noResep) {
        return informasi(koneksi, noResep, false);
    }

    private static String informasi(Connection koneksi, String noResep, boolean denganWaktu) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select id_penginput,nama_penginput,waktu_input from resep_penginput where no_resep=?")) {
            ps.setString(1, noResep);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nama = rs.getString("nama_penginput");
                    if (nama == null || nama.trim().isEmpty()
                            || (nama.equals(rs.getString("id_penginput")) && !"Admin Utama".equals(nama))) {
                        nama = "Nama petugas belum tersedia";
                    }
                    return denganWaktu ? nama + " - " + rs.getString("waktu_input") : nama;
                }
                return "Penginput belum tercatat";
            }
        } catch (SQLException e) {
            System.out.println("Informasi penginput resep tidak tersedia: " + e.getSQLState());
            return "Informasi penginput tidak tersedia";
        }
    }
}
