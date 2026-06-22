package rekammedis;

import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * Halaman "SBAR Perawatan Pasien" (preview daftar SBAR) yang muncul saat klik
 * "Cetak SBAR" di tab. Baris yang sudah divalidasi disorot hijau + status VERIFIED.
 * Tombol "Cetak" memunculkan laporan Jasper.
 */
public final class DlgSBARPerawatanPasien extends JDialog {
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final String status;

    private final widget.TextBox TCari = new widget.TextBox();
    private final widget.Button BtnCari = new widget.Button();
    private final widget.Button BtnCetak = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();
    private final widget.Table tbSBAR = new widget.Table();
    private final DefaultTableModel tabMode;

    private String noRawatFilter = "";

    private static final int COL_VALIDASI = 12;

    public DlgSBARPerawatanPasien(Frame parent, boolean modal, String status) {
        super(parent, modal);
        this.status = (status == null || status.trim().equals("")) ? "ralan" : status.trim();
        setTitle("::[ SBAR Perawatan Pasien ]::");
        setSize(1200, 640);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        tabMode = new DefaultTableModel(null, new Object[]{
            "Tgl.Reg", "No.Rawat", "No.R.M", "Nama Pasien", "Status", "Tanggal SBAR", "Nama Pegawai",
            "Subjek", "Objek", "Asesmen", "Recommendation", "Advis", "Validasi"
        }) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        initComponents();
        tampil();
    }

    private void initComponents() {
        setLayout(new BorderLayout(4, 4));

        BtnCari.setText("Cari");
        BtnCetak.setText("Cetak");
        BtnKeluar.setText("Keluar");
        BtnCari.addActionListener(e -> tampil());
        BtnCetak.addActionListener(e -> DlgValidasiSBAR.cetakSBAR(noRawatFilter, status));
        BtnKeluar.addActionListener(e -> dispose());

        tbSBAR.setModel(tabMode);
        tbSBAR.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbSBAR.setRowHeight(46);
        final DefaultTableModel model = tabMode;
        tbSBAR.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setVerticalAlignment(TOP);
                boolean verified = false;
                int mr = t.convertRowIndexToModel(row);
                Object val = model.getValueAt(mr, COL_VALIDASI);
                if (val != null && val.toString().contains("VERIFIED")) {
                    verified = true;
                }
                if (!sel) {
                    c.setBackground(verified ? new Color(206, 240, 206) : Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });
        int[] lebar = {80, 120, 90, 200, 60, 130, 170, 150, 150, 150, 160, 130, 170};
        for (int i = 0; i < lebar.length; i++) {
            TableColumn c = tbSBAR.getColumnModel().getColumn(i);
            c.setPreferredWidth(lebar[i]);
        }
        JScrollPane scroll = new JScrollPane(tbSBAR);
        scroll.setBorder(BorderFactory.createTitledBorder("::[ SBAR Perawatan Pasien ]::"));

        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        bawah.add(new JLabel("Key Word (No.Rawat / No.RM / Nama) :"));
        TCari.setPreferredSize(new Dimension(260, 23));
        bawah.add(TCari);
        bawah.add(BtnCari);
        bawah.add(new JLabel("    "));
        bawah.add(BtnCetak);
        bawah.add(BtnKeluar);

        add(scroll, BorderLayout.CENTER);
        add(bawah, BorderLayout.SOUTH);
    }

    /** Filter ke pasien tertentu (no_rawat dari tab SBAR). */
    public void setNoRawat(String noRawat) {
        this.noRawatFilter = noRawat == null ? "" : noRawat.trim();
        tampil();
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        String sql = "select reg.tgl_registrasi,s.no_rawat,p.no_rkm_medis,"
                + "concat(p.nm_pasien,' / ',p.jk,' / ',reg.umurdaftar,' ',reg.sttsumur) as pasien,"
                + "s.status,s.tgl_sbar,s.jam_sbar,"
                + "coalesce((select nm_dokter from dokter where kd_dokter=s.nip),(select nama from petugas where nip=s.nip),(select nama from pegawai where nik=s.nip),s.nip) as pegawai,"
                + "s.situation,s.background,s.assesmen,s.recommendation,s.advis,s.validasi,"
                + "ifnull((select nm_dokter from dokter where kd_dokter=s.validator),'') as validator,"
                + "s.tgl_validasi,s.jam_validasi "
                + "from sbar_pasien s inner join reg_periksa reg on s.no_rawat=reg.no_rawat "
                + "inner join pasien p on reg.no_rkm_medis=p.no_rkm_medis "
                + "where s.status=? "
                + (noRawatFilter.equals("") ? "" : "and s.no_rawat=? ")
                + "and (s.no_rawat like ? or p.no_rkm_medis like ? or p.nm_pasien like ?) "
                + "order by s.tgl_sbar,s.jam_sbar";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, status);
            if (!noRawatFilter.equals("")) {
                ps.setString(idx++, noRawatFilter);
            }
            String cari = "%" + TCari.getText().trim() + "%";
            ps.setString(idx++, cari);
            ps.setString(idx++, cari);
            ps.setString(idx++, cari);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String validasi = rs.getString("validasi");
                    String selValidasi;
                    if (validasi != null && validasi.equalsIgnoreCase("Sudah")) {
                        selValidasi = "<html><b style='color:#1a7a1a;'>&#10003; VERIFIED</b><br>"
                                + rs.getString("validator") + "<br>" + tampil(rs.getString("tgl_validasi"))
                                + " " + tampil(rs.getString("jam_validasi")) + "</html>";
                    } else {
                        selValidasi = "Belum Divalidasi";
                    }
                    tabMode.addRow(new Object[]{
                        rs.getString("tgl_registrasi"), rs.getString("no_rawat"), rs.getString("no_rkm_medis"),
                        rs.getString("pasien"), kapital(rs.getString("status")),
                        "<html>" + rs.getString("tgl_sbar") + "<br>" + rs.getString("jam_sbar") + "</html>",
                        rs.getString("pegawai"), rs.getString("situation"), rs.getString("background"),
                        rs.getString("assesmen"), rs.getString("recommendation"), rs.getString("advis"),
                        selValidasi
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tampil SBAR Perawatan : " + e);
        }
    }

    private String tampil(String t) {
        return (t == null || t.startsWith("0000") || t.equals("00:00:00")) ? "" : t;
    }

    private String kapital(String s) {
        if (s == null || s.trim().equals("")) {
            return "";
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
