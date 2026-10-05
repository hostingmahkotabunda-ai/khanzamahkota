import fungsi.ResepPenginput;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/** Uji JDBC terisolasi; tidak mengakses database SIMRS. */
public class ResepPenginputTest {
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    private static void check(boolean valid, String message) {
        if (!valid) throw new AssertionError(message);
    }

    private static class Database {
        boolean autoCommit = true;
        boolean missingTable;
        int prepared;
        final Map<String, String[]> rows = new HashMap<>();
        final Connection connection = proxy(Connection.class, (obj, method, args) -> {
            switch (method.getName()) {
                case "getAutoCommit": return autoCommit;
                case "prepareStatement":
                    prepared++;
                    String sql = (String) args[0];
                    if (missingTable && sql.contains("resep_penginput")) throw new SQLException("Missing table", "42S02");
                    return statement(sql);
                default:
                    throw new AssertionError("Helper tidak boleh mengubah transaksi/koneksi: " + method.getName());
            }
        });

        PreparedStatement statement(String sql) {
            Map<Integer, String> parameters = new HashMap<>();
            return proxy(PreparedStatement.class, (obj, method, args) -> {
                switch (method.getName()) {
                    case "close": return null;
                    case "setString": parameters.put((Integer) args[0], (String) args[1]); return null;
                    case "executeUpdate":
                        check(sql.startsWith("insert into resep_penginput "), "Hanya menulis tabel pendamping");
                        String nomor = parameters.get(1);
                        if (rows.containsKey(nomor)) throw new SQLException("Duplicate", "23000");
                        rows.put(nomor, new String[]{parameters.get(2), parameters.get(3), "2026-10-05 09:00:00"});
                        return 1;
                    case "executeQuery":
                        String[] row;
                        if (sql.contains("from resep_penginput")) {
                            row = rows.get(parameters.get(1));
                        } else if (sql.contains("from petugas") && "P01".equals(parameters.get(1))) {
                            row = new String[]{"Petugas Satu"};
                        } else {
                            row = null;
                        }
                        return result(row);
                    default: throw new AssertionError(method.getName());
                }
            });
        }

        ResultSet result(String[] row) {
            boolean[] read = {false};
            return proxy(ResultSet.class, (obj, method, args) -> {
                switch (method.getName()) {
                    case "close": return null;
                    case "next":
                        if (read[0] || row == null) return false;
                        read[0] = true;
                        return true;
                    case "getString":
                        if (args[0] instanceof Integer) return row[(Integer) args[0] - 1];
                        if ("id_penginput".equals(args[0])) return row[0];
                        if ("nama_penginput".equals(args[0])) return row[1];
                        if ("waktu_input".equals(args[0])) return row[2];
                    default: throw new AssertionError(method.getName());
                }
            });
        }
    }

    public static void main(String[] args) {
        Database db = new Database();
        db.autoCommit = false;
        check(!ResepPenginput.simpan(db.connection, "R1", "P01", "Nama"), "Tolak penulisan di transaksi utama");
        check(db.prepared == 0, "Tidak mengirim SQL saat transaksi utama aktif");
        db.autoCommit = true;
        check(!ResepPenginput.simpan(db.connection, "R1", "", "Nama"), "Akun kosong tidak boleh dicatat");
        check(ResepPenginput.simpan(db.connection, "R1", "P01", "Nama O'Neil"), "Catat resep baru");
        check(ResepPenginput.informasi(db.connection, "R1").equals("Nama O'Neil - 2026-10-05 09:00:00"), "Tampilkan nama dan waktu tanpa ID akun");
        check(ResepPenginput.namaTersimpan(db.connection, "R1").equals("Nama O'Neil"), "Kolom petugas hanya menampilkan nama");
        check(!ResepPenginput.simpan(db.connection, "R1", "P02", "Pengganti"), "Duplikasi ditolak");
        check(db.rows.get("R1")[0].equals("P01"), "Penginput awal tidak ditimpa");
        check(ResepPenginput.informasi(db.connection, "lama").equals("Penginput belum tercatat"), "Tidak menebak penginput resep lama");
        check(ResepPenginput.nama(db.connection, "P01").equals("Petugas Satu"), "Fallback nama ke petugas");
        check(ResepPenginput.nama(db.connection, "unknown").equals("Nama petugas belum tersedia"), "Nama tidak tersedia tidak menampilkan ID akun");
        check(ResepPenginput.nama(db.connection, "Admin Utama").equals("Admin Utama"), "Dukung akun admin");
        db.missingTable = true;
        check(!ResepPenginput.simpan(db.connection, "R2", "P01", "Nama"), "Kegagalan tabel dikembalikan tanpa mengubah transaksi utama");
        check(ResepPenginput.informasi(db.connection, "R1").equals("Informasi penginput tidak tersedia"), "Bedakan kegagalan baca dari data lama");
        System.out.println("PASS: ResepPenginputTest");
    }
}
