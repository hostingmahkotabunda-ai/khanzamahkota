import fungsi.PindahKamarInap;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Uji pemindahan kamar dan rollback tanpa koneksi ke database aplikasi. */
public class RanapPindahKamarTest {
    private static final String RAWAT = "2026/09/18/001";
    private static final String[] ASAL = {"K1", "2026-09-18", "10:00:00"};
    private static final String DIAGNOSIS = "Observasi pasien O'Connor";

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static Map<String, String> row(String... fields) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i < fields.length; i += 2) result.put(fields[i], fields[i + 1]);
        return result;
    }

    private static Map<String, String> episode(String rawat, String kamar, String tanggal,
            String jam, String status) {
        return row("no_rawat", rawat, "kd_kamar", kamar, "trf_kamar", "100000",
                "diagnosa_awal", "Diagnosis awal", "diagnosa_akhir", "",
                "tgl_masuk", tanggal, "jam_masuk", jam, "tgl_keluar", "0000-00-00",
                "jam_keluar", "00:00:00", "lama", "1", "ttl_biaya", "100000",
                "stts_pulang", status);
    }

    /** Simulasi tabel dan transaksi; SQL dibaca untuk menentukan baris yang benar-benar berubah. */
    private static final class Database implements InvocationHandler {
        Map<String, List<Map<String, String>>> tables = new LinkedHashMap<>();
        Map<String, List<Map<String, String>>> snapshot;
        boolean autoCommit = true;
        boolean rollbackFailure;
        int commits, rollbacks, begins, mutations, locks, statementsOpen, resultsOpen;
        String failure;
        String zeroRows;

        Database() {
            tables.put("kamar_inap", new ArrayList<>(Arrays.asList(
                    episode(RAWAT, "K0", "2026-09-17", "09:00:00", "Pindah Kamar"),
                    episode(RAWAT, ASAL[0], ASAL[1], ASAL[2], "-"),
                    episode("PASIEN-LAIN", "K8", ASAL[1], ASAL[2], "-"))));
            tables.put("kamar", new ArrayList<>(Arrays.asList(
                    row("kd_kamar", "K0", "status", "KOSONG"),
                    row("kd_kamar", "K1", "status", "ISI"),
                    row("kd_kamar", "K2", "status", "KOSONG"),
                    row("kd_kamar", "K8", "status", "ISI"))));
        }

        Connection connection() { return proxy(Connection.class, this); }

        Map<String, List<Map<String, String>>> copy() {
            Map<String, List<Map<String, String>>> result = new LinkedHashMap<>();
            for (Map.Entry<String, List<Map<String, String>>> table : tables.entrySet()) {
                List<Map<String, String>> rows = new ArrayList<>();
                for (Map<String, String> value : table.getValue()) rows.add(new LinkedHashMap<>(value));
                result.put(table.getKey(), rows);
            }
            return result;
        }

        String state() { return tables.toString(); }

        List<Map<String, String>> active() {
            List<Map<String, String>> result = new ArrayList<>();
            for (Map<String, String> value : tables.get("kamar_inap")) {
                if (RAWAT.equals(value.get("no_rawat")) && "-".equals(value.get("stts_pulang"))) result.add(value);
            }
            return result;
        }

        String room(String kamar) {
            for (Map<String, String> value : tables.get("kamar")) {
                if (kamar.equals(value.get("kd_kamar"))) return value.get("status");
            }
            return null;
        }

        @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            switch (method.getName()) {
                case "getAutoCommit": return autoCommit;
                case "setAutoCommit":
                    boolean requested = (Boolean) args[0];
                    if (!requested && autoCommit) { snapshot = copy(); begins++; }
                    if (requested && !autoCommit && snapshot != null) {
                        throw new AssertionError("Auto-commit dipulihkan sebelum commit/rollback selesai");
                    }
                    autoCommit = requested;
                    return null;
                case "commit": check(!autoCommit, "Commit di luar transaksi"); commits++; snapshot = null; return null;
                case "rollback":
                    check(!autoCommit && snapshot != null, "Rollback tanpa transaksi milik pemindahan");
                    if (rollbackFailure) throw new SQLException("Simulated rollback failure");
                    tables = snapshot; snapshot = null; rollbacks++; return null;
                case "prepareStatement":
                    check(!autoCommit, "Akses pemindahan harus dalam satu transaksi");
                    String sql = ((String) args[0]).trim().replaceAll("\\s+", " ");
                    check(!sql.contains(DIAGNOSIS), "Diagnosis digabung langsung ke SQL");
                    statementsOpen++;
                    return proxy(PreparedStatement.class, new Statement(this, sql));
                default: throw new AssertionError("Unexpected Connection call: " + method.getName());
            }
        }

        void beforeMutation(String operation) throws SQLException {
            check(locks >= 2, "Data sumber dan tujuan harus dikunci sebelum perubahan");
            if (operation.equals(failure)) throw new SQLException("Simulated failure: " + operation);
            mutations++;
        }

        void assertResources() {
            check(autoCommit, "Auto-commit tidak dipulihkan");
            check(statementsOpen == 0 && resultsOpen == 0, "Resource JDBC bocor");
        }
    }

    private static final class Statement implements InvocationHandler {
        private final Database db;
        private final String sql;
        private final Map<Integer, String> parameters = new LinkedHashMap<>();
        private boolean closed;

        Statement(Database db, String sql) { this.db = db; this.sql = sql; }

        @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            switch (method.getName()) {
                case "setString": parameters.put((Integer) args[0], (String) args[1]); return null;
                case "executeQuery": return query();
                case "executeUpdate": return update();
                case "close": if (!closed) { closed = true; db.statementsOpen--; } return null;
                default: throw new AssertionError("Unexpected PreparedStatement call: " + method.getName());
            }
        }

        private Matcher match(String pattern) {
            Matcher result = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(sql);
            check(result.matches(), "SQL belum didukung simulator: " + sql);
            return result;
        }

        private Map<String, String> values(String text, String separator, int[] index) {
            Map<String, String> result = new LinkedHashMap<>();
            for (String part : text.split(separator)) {
                String[] pieces = part.trim().split("\\s*=\\s*", 2);
                check(pieces.length == 2, "Predikat SQL tidak dikenali: " + part);
                result.put(pieces[0].trim().toLowerCase(Locale.ROOT), value(pieces[1].trim(), index));
            }
            return result;
        }

        private String value(String token, int[] index) {
            if ("?".equals(token)) {
                check(parameters.containsKey(index[0]), "Parameter SQL belum diisi: " + index[0]);
                return parameters.get(index[0]++);
            }
            check(token.startsWith("'") && token.endsWith("'"), "Literal SQL tidak dikenali: " + token);
            return token.substring(1, token.length() - 1).replace("''", "'");
        }

        private boolean matches(Map<String, String> value, Map<String, String> criteria) {
            for (Map.Entry<String, String> entry : criteria.entrySet()) {
                if (!entry.getValue().equals(value.get(entry.getKey()))) return false;
            }
            return true;
        }

        private ResultSet query() {
            Matcher query = match("select (.+) from (\\w+) where (.+) for update");
            db.locks++;
            String[] columns = query.group(1).split("\\s*,\\s*");
            Map<String, String> criteria = values(query.group(3), "(?i)\\s+and\\s+", new int[]{1});
            List<Map<String, String>> selected = new ArrayList<>();
            for (Map<String, String> value : db.tables.get(query.group(2))) {
                if (matches(value, criteria)) selected.add(new LinkedHashMap<>(value));
            }
            db.resultsOpen++;
            return proxy(ResultSet.class, new InvocationHandler() {
                int position = -1;
                boolean closed;
                @Override public Object invoke(Object proxy, Method method, Object[] args) {
                    switch (method.getName()) {
                        case "next": return ++position < selected.size();
                        case "getString":
                            String column = args[0] instanceof Integer ? columns[(Integer) args[0] - 1] : (String) args[0];
                            return selected.get(position).get(column);
                        case "close": if (!closed) { closed = true; db.resultsOpen--; } return null;
                        default: throw new AssertionError("Unexpected ResultSet call: " + method.getName());
                    }
                }
            });
        }

        private int update() throws SQLException {
            String lower = sql.toLowerCase(Locale.ROOT);
            if (lower.startsWith("insert into ")) {
                Matcher insert = match("insert into (\\w+) \\((.+)\\) values \\((.+)\\)");
                String[] columns = insert.group(2).split("\\s*,\\s*");
                String[] tokens = insert.group(3).split("\\s*,\\s*");
                Map<String, String> added = new LinkedHashMap<>();
                int[] index = {1};
                for (int i = 0; i < columns.length; i++) added.put(columns[i], value(tokens[i], index));
                db.beforeMutation("insert");
                if ("insert".equals(db.zeroRows)) return 0;
                for (Map<String, String> existing : db.tables.get(insert.group(1))) {
                    boolean sameKey = true;
                    for (String key : new String[]{"no_rawat", "tgl_masuk", "jam_masuk"}) {
                        sameKey &= existing.get(key).equals(added.get(key));
                    }
                    if (sameKey) throw new SQLException("Duplicate kamar_inap key");
                }
                db.tables.get(insert.group(1)).add(added);
                return 1;
            }
            boolean deleting = lower.startsWith("delete from ");
            Matcher change = match(deleting ? "delete from (\\w+) where (.+)" : "update (\\w+) set (.+) where (.+)");
            String table = change.group(1);
            int[] index = {1};
            Map<String, String> assignments = deleting ? null : values(change.group(2), "\\s*,\\s*", index);
            Map<String, String> criteria = values(change.group(deleting ? 2 : 3), "(?i)\\s+and\\s+", index);
            String operation = deleting ? "delete" : "kamar".equals(table)
                    ? ("ISI".equals(assignments.get("status")) ? "occupy" : "release") : "source";
            db.beforeMutation(operation);
            if (operation.equals(db.zeroRows)) return 0;
            int affected = 0;
            List<Map<String, String>> selected = db.tables.get(table);
            for (int i = selected.size() - 1; i >= 0; i--) {
                Map<String, String> value = selected.get(i);
                if (!matches(value, criteria)) continue;
                if (deleting) selected.remove(i); else value.putAll(assignments);
                affected++;
            }
            return affected;
        }
    }

    private static void transfer(Database db, int mode, String[] asal, String tujuan, String tanggal, String jam)
            throws SQLException {
        PindahKamarInap.simpan(db.connection(), mode, RAWAT, asal, tujuan, "200000", DIAGNOSIS,
                "Diagnosis akhir O'Connor", tanggal, jam, "2", "400000", "100000", "3", "300000");
    }

    private static void transfer(Database db, int mode) throws SQLException {
        transfer(db, mode, ASAL, "K2", "2026-09-19", "11:00:00");
    }

    @FunctionalInterface private interface Action { void run() throws SQLException; }

    private static void rejects(Database db, Action action, String description) throws SQLException {
        String before = db.state();
        try { action.run(); throw new AssertionError("Pemindahan seharusnya ditolak: " + description); }
        catch (SQLException expected) { /* Kegagalan harus sampai ke pemanggil. */ }
        check(before.equals(db.state()), "Data berubah setelah penolakan: " + description);
        check(db.commits == 0, "Kegagalan di-commit: " + description);
        check(db.begins == db.rollbacks, "Transaksi gagal tidak di-rollback: " + description);
        db.assertResources();
    }

    public static void main(String[] args) throws Exception {
        for (int mode = 1; mode <= 4; mode++) {
            Database db = new Database();
            Map<String, String> history = new LinkedHashMap<>(db.tables.get("kamar_inap").get(0));
            Map<String, String> otherPatient = new LinkedHashMap<>(db.tables.get("kamar_inap").get(2));
            transfer(db, mode);
            check(db.commits == 1 && db.rollbacks == 0 && db.begins == 1, "Transaksi sukses mode " + mode);
            check(db.active().size() == 1, "Pasien harus tetap memiliki tepat satu kamar aktif: mode " + mode);
            Map<String, String> active = db.active().get(0);
            check("K2".equals(active.get("kd_kamar")), "Kamar aktif bukan tujuan: mode " + mode);
            check("KOSONG".equals(db.room("K1")) && "ISI".equals(db.room("K2")), "Status tempat tidur salah");
            check("200000".equals(active.get("trf_kamar")) && "2".equals(active.get("lama"))
                    && "400000".equals(active.get("ttl_biaya")), "Tarif/lama/biaya tujuan salah");
            check("0000-00-00".equals(active.get("tgl_keluar")) && "00:00:00".equals(active.get("jam_keluar")),
                    "Pasien tujuan memiliki waktu pulang");
            check((mode == 2 ? ASAL[1] : "2026-09-19").equals(active.get("tgl_masuk"))
                    && (mode == 2 ? ASAL[2] : "11:00:00").equals(active.get("jam_masuk")), "Waktu masuk salah");
            if (mode != 2) {
                check(DIAGNOSIS.equals(active.get("diagnosa_awal"))
                        && "Diagnosis akhir O'Connor".equals(active.get("diagnosa_akhir")), "Diagnosis bertanda petik berubah");
            } else {
                check("Diagnosis awal".equals(active.get("diagnosa_awal")), "Diagnosis lama berubah saat mengoreksi kamar");
            }
            check(db.tables.get("kamar_inap").contains(history), "Riwayat lama ikut diubah");
            check(db.tables.get("kamar_inap").contains(otherPatient), "Pasien lain ikut diubah");
            check(db.tables.get("kamar_inap").size() == (mode <= 2 ? 3 : 4), "Jumlah riwayat salah");
            if (mode >= 3) {
                Map<String, String> old = db.tables.get("kamar_inap").get(1);
                check("K1".equals(old.get("kd_kamar")) && "Pindah Kamar".equals(old.get("stts_pulang")),
                        "Riwayat kamar asal tidak ditutup sebagai pindah kamar");
                check("2026-09-19".equals(old.get("tgl_keluar")) && "11:00:00".equals(old.get("jam_keluar"))
                        && "3".equals(old.get("lama")) && "300000".equals(old.get("ttl_biaya")), "Biaya/waktu kamar asal salah");
            }
            db.assertResources();
        }

        for (int mode = 1; mode <= 4; mode++) {
            final int selectedMode = mode;
            String[] failures = mode == 2 ? new String[]{"source", "occupy", "release"}
                    : new String[]{"insert", mode == 1 ? "delete" : "source", "occupy", "release"};
            for (String point : failures) {
                Database db = new Database(); db.failure = point;
                rejects(db, () -> transfer(db, selectedMode), "mode " + mode + ", failure " + point);
            }
            Database zero = new Database(); zero.zeroRows = "release";
            rejects(zero, () -> transfer(zero, selectedMode), "baris kamar asal hilang pada mode " + mode);
        }

        for (String[] snapshot : new String[][]{{"K9", ASAL[1], ASAL[2]},
                {ASAL[0], "2026-09-17", ASAL[2]}, {ASAL[0], ASAL[1], "09:59:59"}}) {
            Database stale = new Database();
            rejects(stale, () -> transfer(stale, 3, snapshot, "K2", "2026-09-19", "11:00:00"), "snapshot sumber usang");
        }
        for (int mode : new int[]{1, 3, 4}) {
            final int selectedMode = mode;
            Database collision = new Database();
            collision.tables.get("kamar_inap").add(episode(RAWAT, "K9", "2026-09-19", "11:00:00", "Pindah Kamar"));
            rejects(collision, () -> transfer(collision, selectedMode), "primary key timestamp bertabrakan pada kamar berbeda, mode " + mode);
            check(collision.active().size() == 1 && "K1".equals(collision.active().get(0).get("kd_kamar")),
                    "Tabrakan primary key menghilangkan kamar aktif asal");
        }
        Database duplicate = new Database(); duplicate.tables.get("kamar_inap").add(episode(RAWAT, "K9", ASAL[1], "10:01:00", "-"));
        rejects(duplicate, () -> transfer(duplicate, 3), "dua kamar aktif");
        Database discharged = new Database(); discharged.active().get(0).put("stts_pulang", "Sehat");
        rejects(discharged, () -> transfer(discharged, 3), "pasien sudah pulang");
        Database occupied = new Database(); occupied.tables.get("kamar").get(2).put("status", "ISI");
        rejects(occupied, () -> transfer(occupied, 3), "tujuan terisi");
        Database missing = new Database(); missing.tables.get("kamar").remove(2);
        rejects(missing, () -> transfer(missing, 3), "tujuan tidak ditemukan");
        Database sameRoom = new Database();
        rejects(sameRoom, () -> transfer(sameRoom, 3, ASAL, "K1", "2026-09-19", "11:00:00"), "kamar sama");
        for (int mode : new int[]{1, 3, 4}) {
            final int selectedMode = mode;
            for (String jam : new String[]{ASAL[2], "09:59:59"}) {
                Database time = new Database();
                rejects(time, () -> transfer(time, selectedMode, ASAL, "K2", ASAL[1], jam), "waktu sama/sebelum masuk");
            }
        }
        Database owned = new Database(); owned.autoCommit = false;
        String before = owned.state();
        try { transfer(owned, 3); throw new AssertionError("Transaksi pemanggil diambil alih"); }
        catch (SQLException expected) { /* Koneksi sudah dimiliki transaksi lain. */ }
        check(!owned.autoCommit && owned.begins == 0 && owned.commits == 0 && owned.rollbacks == 0
                && owned.mutations == 0 && owned.statementsOpen == 0 && before.equals(owned.state()), "Transaksi pemanggil terganggu");

        Database brokenRollback = new Database();
        brokenRollback.failure = "release"; brokenRollback.rollbackFailure = true;
        try { transfer(brokenRollback, 3); throw new AssertionError("Kegagalan rollback tertelan"); }
        catch (SQLException expected) {
            check(expected.getMessage().contains("release") && expected.getSuppressed().length == 1
                    && expected.getSuppressed()[0].getMessage().contains("rollback"), "Penyebab gagal simpan/rollback hilang");
        }
        check(!brokenRollback.autoCommit && brokenRollback.snapshot != null && brokenRollback.commits == 0,
                "Rollback gagal malah mengaktifkan auto-commit pada perubahan yang masih tertunda");
        check(brokenRollback.statementsOpen == 0 && brokenRollback.resultsOpen == 0, "Resource bocor ketika rollback gagal");
        System.out.println("PASS: 4 mode pindah kamar, pasien tetap aktif, diagnosis petik, rollback seluruh perubahan, validasi sumber/tujuan/waktu, dan kepemilikan transaksi");
    }
}
