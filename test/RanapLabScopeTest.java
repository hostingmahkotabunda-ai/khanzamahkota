import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import javax.swing.SwingUtilities;
import sun.misc.Unsafe;

/** Memeriksa konteks kunjungan lab tanpa membuka dialog atau database SIMRS. */
public class RanapLabScopeTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                Field field = Unsafe.class.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                Unsafe unsafe = (Unsafe) field.get(null);
                Object dialog = unsafe.allocateInstance(simrskhanza.DlgRawatInap.class);
                widget.TextBox noRawat = new widget.TextBox();
                widget.TextBox noRm = new widget.TextBox();
                widget.TextBox cariPasien = new widget.TextBox();
                widget.editorpane hasil = new widget.editorpane();
                hasil.setContentType("text/html");
                noRm.setText("RM-SAMA");
                cariPasien.setText("RM-SAMA");
                set(dialog, "TNoRw", noRawat);
                set(dialog, "TNoRM", noRm);
                set(dialog, "TCariPasien", cariPasien);
                set(dialog, "LoadHTMLLaborat", hasil);
                List<String> kunjungan = new ArrayList<>();
                List<String> kategori = new ArrayList<>();
                Connection connection = proxy(Connection.class, (obj, method, values) -> {
                    if (!"prepareStatement".equals(method.getName())) throw new AssertionError(method.getName());
                    String sql = (String) values[0];
                    check(sql.contains("periksa_lab.no_rawat=?"), "Lab wajib dibatasi nomor rawat");
                    check(!sql.contains("no_rkm_medis"), "Tidak mengambil semua kunjungan pasien");
                    kategori.add(sql.contains("kategori='PA'") ? "PA" : "PK/MB");
                    String[] parameter = {null};
                    return proxy(PreparedStatement.class, (stmt, action, params) -> {
                        if ("setString".equals(action.getName())) { parameter[0] = (String) params[1]; return null; }
                        if ("close".equals(action.getName())) return null;
                        if ("executeQuery".equals(action.getName())) {
                            kunjungan.add(parameter[0]);
                            return proxy(ResultSet.class, (rs, read, ignored) -> {
                                if ("next".equals(read.getName())) return false;
                                if ("close".equals(read.getName())) return null;
                                throw new AssertionError(read.getName());
                            });
                        }
                        throw new AssertionError(action.getName());
                    });
                });
                set(dialog, "koneksi", connection);
                Method tampil = dialog.getClass().getDeclaredMethod("tampilLaboratPasien");
                tampil.setAccessible(true);
                for (String rawat : new String[]{"2026/10/05/000001", "2026/09/01/000001"}) {
                    kunjungan.clear();
                    kategori.clear();
                    noRawat.setText(rawat);
                    tampil.invoke(dialog);
                    check(kunjungan.equals(Arrays.asList(rawat, rawat)), "Konteks PK/MB dan PA mengikuti kunjungan aktif");
                    check(kategori.equals(Arrays.asList("PK/MB", "PA")), "Kedua kategori tetap dibaca");
                    check(hasil.getText().contains(rawat), "Header mencantumkan nomor rawat aktif");
                    check(hasil.getText().contains("Data laboratorium tidak ditemukan"), "Tidak kembali ke riwayat lama saat kunjungan kosong");
                }
                kunjungan.clear();
                noRawat.setText("");
                tampil.invoke(dialog);
                check(kunjungan.isEmpty(), "RM saja tidak boleh memuat riwayat lab");
                check(hasil.getText().contains("Pilih nomor rawat"), "Hasil lama dibersihkan saat nomor rawat kosong");
                System.out.println("PASS: konteks dua kunjungan RM sama, PK/MB + PA, hasil kosong, dan nomor rawat kosong.");
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }
}
