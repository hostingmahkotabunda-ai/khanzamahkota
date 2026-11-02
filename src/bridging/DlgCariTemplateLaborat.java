package bridging;

import fungsi.koneksiDB;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Pemilih template untuk mapping SatuSehat, tanpa mengubah data laboratorium. */
public final class DlgCariTemplateLaborat extends JDialog {
    private final JTextField pencarian = new JTextField(30);
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Kode Perawatan", "Jenis Pemeriksaan", "ID Template", "Pemeriksaan", "Satuan"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tabel = new JTable(model);
    private String[] pilihan;

    public DlgCariTemplateLaborat(Dialog owner, boolean modal) {
        super(owner, "Pilih Template Laboratorium", modal);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(850, 500);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.setAutoCreateRowSorter(true);
        JPanel atas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton cari = new JButton("Cari");
        atas.add(pencarian);
        atas.add(cari);
        add(atas, BorderLayout.NORTH);
        add(new JScrollPane(tabel), BorderLayout.CENTER);
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton pilih = new JButton("Pilih");
        JButton batal = new JButton("Batal");
        bawah.add(pilih);
        bawah.add(batal);
        add(bawah, BorderLayout.SOUTH);
        cari.addActionListener(e -> cari());
        pencarian.addActionListener(e -> cari());
        batal.addActionListener(e -> setVisible(false));
        pilih.addActionListener(e -> {
            int row = tabel.getSelectedRow();
            if (row < 0) return;
            pilihan = new String[5];
            for (int i = 0; i < pilihan.length; i++) {
                Object value = tabel.getValueAt(row, i);
                pilihan[i] = value == null ? "" : value.toString();
            }
            setVisible(false);
        });
    }

    public String[] pilihTemplate() {
        pilihan = null;
        tabel.clearSelection();
        cari();
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return pilihan;
    }

    private void cari() {
        model.setRowCount(0);
        String sql = "select t.kd_jenis_prw,j.nm_perawatan,t.id_template,t.Pemeriksaan,t.satuan "
                + "from template_laboratorium t inner join jns_perawatan_lab j on j.kd_jenis_prw=t.kd_jenis_prw "
                + "where t.kd_jenis_prw like ? or j.nm_perawatan like ? or t.Pemeriksaan like ? "
                + "order by j.nm_perawatan,t.urut limit 500";
        try (PreparedStatement ps = koneksiDB.condb().prepareStatement(sql)) {
            for (int i = 1; i <= 3; i++) ps.setString(i, "%" + pencarian.getText().trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) model.addRow(new Object[]{rs.getString(1), rs.getString(2),
                    rs.getString(3), rs.getString(4), rs.getString(5)});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal membaca template laboratorium: " + e.getMessage());
        }
    }
}
