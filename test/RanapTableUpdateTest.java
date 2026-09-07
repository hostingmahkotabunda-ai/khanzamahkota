import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;

/** Uji model SOAP tanpa membuat dialog atau membuka koneksi database. */
public class RanapTableUpdateTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static Object snapshot(List<Object[]> rows) throws Exception {
        Method prepare = simrskhanza.DlgRawatInap.class.getDeclaredMethod("siapkanDataPemeriksaan", List.class);
        prepare.setAccessible(true);
        return prepare.invoke(null, rows);
    }

    private static Object field(Object owner, String name) throws Exception {
        Field field = owner.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(owner);
    }

    public static void main(String[] args) throws Exception {
        List<Object[]> rows = new ArrayList<>();
        for (int i=0; i<1000; i++) {
            Object[] row = new Object[27];
            row[0]=false;
            for (int j=1; j<=24; j++) row[j]="value"+i+"-"+j;
            row[1]=String.format("RAWAT%04d", 999-i);
            row[4]="2026-09-07"; row[5]="10:00:00";
            row[25]=i%2==0 ? "Sudah" : "Belum";
            row[26]=i%2==0 ? "RALAN" : "RANAP";
            rows.add(row);
        }
        // Persiapan sama seperti worker: tidak memerlukan EDT.
        Object prepared = snapshot(rows);
        Object preparedRows = field(prepared, "baris");
        Set<?> keys = (Set<?>)field(prepared, "kunciRalan");
        check(keys.size()==500 && keys.contains("RAWAT0999|2026-09-07|10:00:00"), "Penanda RALAN berubah");
        Object emptyRows = field(snapshot(Collections.<Object[]>emptyList()), "baris");
        Class<?> type = Class.forName("simrskhanza.DlgRawatInap$ModelPemeriksaan");
        Constructor<?> constructor = type.getDeclaredConstructor(Object[].class);
        constructor.setAccessible(true);
        Object[] columns = new Object[26];
        for (int i=0; i<26; i++) columns[i]="Kolom"+i;
        DefaultTableModel model = (DefaultTableModel)constructor.newInstance(new Object[]{columns});
        Method replace = type.getDeclaredMethod("gantiBaris", Vector.class);
        replace.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try {
                JTable table = new JTable(model);
                table.setAutoCreateRowSorter(true);
                table.getRowSorter().setSortKeys(Collections.singletonList(new RowSorter.SortKey(1, SortOrder.ASCENDING)));
                TableColumn first = table.getColumnModel().getColumn(0);
                first.setPreferredWidth(137);
                TableCellRenderer renderer = new DefaultTableCellRenderer();
                first.setCellRenderer(renderer);
                table.moveColumn(0, 2);
                int[] events = {0};
                model.addTableModelListener(e -> {
                    check(SwingUtilities.isEventDispatchThread(), "Notifikasi tabel di luar EDT");
                    check(e.getFirstRow()!=TableModelEvent.HEADER_ROW, "Struktur kolom direset");
                    events[0]++;
                });
                replace.invoke(model, preparedRows);
                check(events[0]==1, "Harus satu notifikasi untuk 1000 baris");
                check(model.getRowCount()==1000 && table.getRowCount()==1000, "Jumlah hasil berubah");
                for (int i=0; i<rows.size(); i++) {
                    for (int j=0; j<26; j++) check(Objects.equals(model.getValueAt(i,j),rows.get(i)[j]), "Isi/urutan SOAP berubah");
                }
                check("RAWAT0000".equals(table.getValueAt(0,table.convertColumnIndexToView(1))), "Sorting tidak bekerja setelah penggantian");
                check(table.getColumnModel().getColumn(2)==first && first.getPreferredWidth()==137 && first.getCellRenderer()==renderer,
                        "Urutan/lebar/renderer kolom berubah");
                model.setValueAt(true, 0, 0);
                check(Boolean.TRUE.equals(model.getValueAt(0,0)), "Checkbox tidak dapat diubah");
                events[0]=0;
                table.setRowSelectionInterval(0,0);
                replace.invoke(model, emptyRows);
                check(events[0]==1 && model.getRowCount()==0 && table.getSelectedRow()==-1, "Pengosongan tabel tidak konsisten");
                check(table.getRowSorter().getSortKeys().size()==1, "Sort key hilang setelah tabel dikosongkan");
            } catch (Exception e) { throw new RuntimeException(e); }
        });
        try {
            replace.invoke(model, emptyRows);
            throw new AssertionError("Pembaruan di luar EDT tidak ditolak");
        } catch (InvocationTargetException expected) {
            check(expected.getCause() instanceof IllegalStateException, "Jenis penolakan thread salah");
        }
        Thread.currentThread().interrupt();
        try {
            snapshot(rows);
            throw new AssertionError("Persiapan mengabaikan pembatalan");
        } catch (InvocationTargetException expected) {
            check(expected.getCause() instanceof java.util.concurrent.CancellationException, "Jenis pembatalan salah");
        } finally { Thread.interrupted(); }
        System.out.println("PASS: 1000 SOAP rows -> 1 model event; data, RALAN keys, sorting, columns, checkbox, clearing, EDT and cancellation preserved");
    }
}
