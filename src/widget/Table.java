package widget;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 *
 * @author usu
 */
public class Table extends JTable {

    /*
     * Serial version UID
     */
    private static final long serialVersionUID = 1L;

    public Table() {
        super();
        //setBackground(new Color(255,235,255));
        //setGridColor(new Color(245,170,245));
        //setForeground(new Color(90,90,90));
        setBackground(new Color(255,255,255));
        setGridColor(new Color(226,231,221));
        setForeground(new Color(50,50,50));
        setFont(new java.awt.Font("Tahoma", 0, 11));
        setRowHeight(22);
        setSelectionBackground(new Color(255,255,255));
        setSelectionForeground(new Color(255,0,0));
        getTableHeader().setForeground(new Color(50,50,50));
        getTableHeader().setBackground(new Color(255,250,250));
        getTableHeader().setBorder(javax.swing.BorderFactory.createLineBorder(new Color(255,250,250)));
        getTableHeader().setFont(new java.awt.Font("Tahoma", 0, 11));
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component component = super.prepareRenderer(renderer, row, column);

        Object aktif = getClientProperty("colorSoapRows");
        if (!(aktif instanceof Boolean) || !((Boolean) aktif)) {
            return component;
        }

        int modelRow = convertRowIndexToModel(row);
        TableModel model = getModel();
        int namaColumn = ambilIndexClientProperty("dokterNameColumn", -1);
        int profesiColumn = ambilIndexClientProperty("dokterProfesiColumn", -1);

        String nama = ambilNilaiModel(model, modelRow, namaColumn).toLowerCase();
        String profesi = ambilNilaiModel(model, modelRow, profesiColumn).toLowerCase();

        boolean dokter = profesi.contains("dokter")
                || nama.startsWith("dr.")
                || nama.startsWith("dr ")
                || nama.startsWith("drg.")
                || nama.startsWith("drg ");

        if (isRowSelected(row)) {
            component.setBackground(dokter ? new Color(102,181,255) : new Color(120,210,132));
            component.setForeground(Color.BLACK);
        } else {
            component.setBackground(dokter ? new Color(196,226,255) : new Color(202,237,206));
            component.setForeground(new Color(50,50,50));
        }

        return component;
    }

    private int ambilIndexClientProperty(String key, int defaultValue) {
        Object value = getClientProperty(key);
        return value instanceof Integer ? ((Integer) value) : defaultValue;
    }

    private String ambilNilaiModel(TableModel model, int row, int column) {
        if (model == null || row < 0 || column < 0 || column >= model.getColumnCount()) {
            return "";
        }
        Object value = model.getValueAt(row, column);
        return value == null ? "" : value.toString().trim();
    }
}
