import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

/** Jalankan tanpa database: menguji kontrak hasil SOAP dan penutupan resource JDBC. */
public class RanapLoadingReadTest {
    private static final class JdbcStub implements InvocationHandler {
        final Map<Integer, String> parameters = new TreeMap<>();
        final List<String[]> rows = new ArrayList<>();
        String sql;
        int row = -1;
        boolean statementClosed, resultClosed, fail;
        @SuppressWarnings("unchecked") <T> T proxy(Class<T> type) {
            return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, this);
        }
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            switch (method.getName()) {
                case "prepareStatement": sql = (String)args[0]; return proxy(PreparedStatement.class);
                case "setString": parameters.put((Integer)args[0], (String)args[1]); return null;
                case "setQueryTimeout": return null;
                case "executeQuery":
                    if (fail) throw new SQLException("Simulated database failure");
                    return proxy(ResultSet.class);
                case "next": return ++row < rows.size();
                case "getString": return rows.get(row)[(Integer)args[0]-1];
                case "getInt": return Integer.parseInt(rows.get(row)[(Integer)args[0]-1]);
                case "close":
                    if (proxy instanceof ResultSet) resultClosed = true;
                    if (proxy instanceof PreparedStatement) statementClosed = true;
                    return null;
                default: throw new AssertionError("Unexpected JDBC call: " + method.getName());
            }
        }
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    @SuppressWarnings("unchecked")
    private static List<Object[]> read(JdbcStub stub, String cari) throws Exception {
        Method method = simrskhanza.DlgRawatInap.class.getDeclaredMethod("bacaDataPemeriksaan",
                Connection.class, String.class, String.class, String.class, String.class);
        method.setAccessible(true);
        try {
            return (List<Object[]>)method.invoke(null, stub.proxy(Connection.class), "2026-09-01", "2026-09-07", "RM001", cari);
        } catch (InvocationTargetException e) {
            throw (Exception)e.getCause();
        }
    }
    public static void main(String[] args) throws Exception {
        JdbcStub stub = new JdbcStub();
        String[] ranap = new String[26], ralan = new String[26];
        for (int i=0; i<24; i++) { ranap[i]="ranap"+i; ralan[i]="ralan"+i; }
        ranap[24]="sUdAh"; ranap[25]="RANAP";
        ralan[24]=null; ralan[25]="RALAN";
        stub.rows.add(ranap); stub.rows.add(ralan);
        List<Object[]> result = read(stub, "RAWAT001");
        check(result.size()==2, "Kedua sumber SOAP harus dipertahankan");
        for (int i=1; i<=24; i++) check(result.get(0)[i].equals(ranap[i-1]), "Kolom SOAP bergeser: "+i);
        check(Boolean.FALSE.equals(result.get(0)[0]), "Pilihan awal harus false");
        check("Sudah".equals(result.get(0)[25]) && "Belum".equals(result.get(1)[25]), "Status validasi berubah");
        check("RALAN".equals(result.get(1)[26]), "Penanda sumber RALAN hilang");
        check(stub.parameters.size()==24, "Parameter UNION tidak lengkap");
        for (int offset : new int[]{0,12}) {
            check("2026-09-01".equals(stub.parameters.get(offset+1)), "Tanggal awal salah");
            check("2026-09-07".equals(stub.parameters.get(offset+2)), "Tanggal akhir salah");
            check("%RM001%".equals(stub.parameters.get(offset+3)), "Filter RM berubah");
            for (int i=4; i<=12; i++) check("%RAWAT001%".equals(stub.parameters.get(offset+i)), "Filter pencarian berubah");
        }
        check(stub.statementClosed && stub.resultClosed, "Resource tidak ditutup setelah berhasil");
        JdbcStub empty = new JdbcStub();
        check(read(empty, "").isEmpty() && empty.parameters.size()==6, "Pencarian tanpa kata kunci berubah");
        JdbcStub failed = new JdbcStub(); failed.fail=true;
        try { read(failed, "RAWAT001"); throw new AssertionError("Error DB tertelan"); }
        catch (SQLException expected) { check(failed.statementClosed, "Statement gagal tidak ditutup"); }
        JdbcStub cancelled = new JdbcStub(); cancelled.rows.add(ranap);
        Thread.currentThread().interrupt();
        try { read(cancelled, "RAWAT001"); throw new AssertionError("Pembatalan diabaikan"); }
        catch (java.util.concurrent.CancellationException expected) {
            check(cancelled.statementClosed && cancelled.resultClosed, "Resource pembatalan tidak ditutup");
        } finally { Thread.interrupted(); }
        JdbcStub first = new JdbcStub(); first.rows.add(new String[]{"DPJP1"}); first.rows.add(new String[]{"DPJP2"});
        Method scalar = simrskhanza.DlgRawatInap.class.getDeclaredMethod("bacaNilaiAwal", Connection.class, String.class, String.class);
        scalar.setAccessible(true);
        check("DPJP1".equals(scalar.invoke(null, first.proxy(Connection.class), "select kd_dokter from dpjp_ranap where no_rawat=?", "RAWAT001")), "Pemilihan DPJP pertama berubah");
        JdbcStub sbar = new JdbcStub(); sbar.rows.add(new String[]{"3"});
        check(rekammedis.DlgValidasiSBAR.countBelum(sbar.proxy(Connection.class), " RAWAT001 ", "ranap")==3, "Jumlah SBAR salah");
        check("RAWAT001".equals(sbar.parameters.get(1)) && "ranap".equals(sbar.parameters.get(2)), "Konteks SBAR salah");
        JdbcStub soap = new JdbcStub(); soap.rows.add(new String[]{"2"});
        check(rekammedis.DlgValidasiSOAP.countBelum(soap.proxy(Connection.class), "RAWAT001", "ranap")==2, "Jumlah SOAP salah");
        check(soap.sql.contains("dpjp_ranap") && soap.sql.contains("and not exists"), "Pengecualian penginput DPJP hilang");
        System.out.println("PASS: SOAP mapping/filter, RALAN marker, DPJP, validation, failure, cancellation and JDBC cleanup");
    }
}
