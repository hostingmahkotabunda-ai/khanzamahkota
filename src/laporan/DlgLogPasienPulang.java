package laporan;

import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Log siapa & kapan memulangkan pasien rawat inap -- sebelumnya tidak tercatat sama sekali
 * (masalah: pasien dipulangkan tapi petugasnya tidak diketahui, tidak bisa di-track). Dibuka
 * dari tombol di DlgKamarInap. Murni baca (read-only), tidak ada simpan/hapus.
 */
public final class DlgLogPasienPulang extends JDialog {

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();

    private final javax.swing.table.DefaultTableModel tabMode = new javax.swing.table.DefaultTableModel(null,
            new Object[]{"No. Rawat", "No. RM", "Nama Pasien", "Status Pulang", "Tanggal Pulang", "Jam Pulang", "Petugas", "Dicatat Pada"}) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final widget.Tanggal dtpDari = dtTanggal();
    private final widget.Tanggal dtpSampai = dtTanggal();
    private final widget.TextBox TCari = new widget.TextBox();
    private final widget.Button BtnTampilkan = new widget.Button();
    private final widget.Table tbLog = new widget.Table();
    private final widget.Button BtnKeluar = new widget.Button();

    public DlgLogPasienPulang(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Log Pasien Pulang ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
        setSize(1100, 650);
        setMinimumSize(new Dimension(850, 400));
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        final Color latar = new Color(246, 249, 251);
        final Color garis = new Color(215, 224, 230);
        final Color teks = new Color(32, 49, 66);

        getContentPane().setBackground(latar);
        getContentPane().setLayout(new BorderLayout(0, 10));

        JPanel atas = new JPanel();
        atas.setBackground(latar);
        atas.setLayout(new BoxLayout(atas, BoxLayout.Y_AXIS));
        atas.setBorder(new javax.swing.border.EmptyBorder(14, 18, 0, 18));

        JLabel judulUtama = new JLabel("Log Pasien Pulang");
        judulUtama.setFont(new Font("Tahoma", Font.BOLD, 20));
        judulUtama.setForeground(teks);
        judulUtama.setAlignmentX(Component.LEFT_ALIGNMENT);
        atas.add(judulUtama);
        JLabel subJudul = new JLabel("Riwayat pemulangan pasien rawat inap -- tanggal, jam, dan petugas yang memulangkan");
        subJudul.setFont(new Font("Tahoma", Font.PLAIN, 11));
        subJudul.setForeground(new Color(110, 125, 138));
        subJudul.setAlignmentX(Component.LEFT_ALIGNMENT);
        atas.add(subJudul);
        atas.add(Box.createVerticalStrut(10));

        JPanel panelFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelFilter.setOpaque(false);
        panelFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel labelDari = new JLabel("Tanggal Dari");
        labelDari.setFont(new Font("Tahoma", Font.PLAIN, 11));
        dtpDari.setPreferredSize(new Dimension(120, 25));
        JLabel labelSampai = new JLabel("Sampai");
        labelSampai.setFont(new Font("Tahoma", Font.PLAIN, 11));
        dtpSampai.setPreferredSize(new Dimension(120, 25));
        JLabel labelCari = new JLabel("Cari");
        labelCari.setFont(new Font("Tahoma", Font.PLAIN, 11));
        TCari.setPreferredSize(new Dimension(220, 25));
        TCari.setToolTipText("No.Rawat / No.RM / Nama Pasien / Petugas");
        BtnTampilkan.setText("Tampilkan");
        panelFilter.add(labelDari);
        panelFilter.add(dtpDari);
        panelFilter.add(labelSampai);
        panelFilter.add(dtpSampai);
        panelFilter.add(labelCari);
        panelFilter.add(TCari);
        panelFilter.add(BtnTampilkan);
        atas.add(panelFilter);

        getContentPane().add(atas, BorderLayout.NORTH);

        JPanel tengah = new JPanel(new BorderLayout());
        tengah.setBackground(latar);
        tengah.setBorder(new javax.swing.border.EmptyBorder(10, 18, 10, 18));

        tbLog.setModel(tabMode);
        tbLog.setAutoResizeMode(widget.Table.AUTO_RESIZE_OFF);
        tbLog.setRowHeight(24);
        JScrollPane scrollLog = new JScrollPane(tbLog);
        tengah.add(scrollLog, BorderLayout.CENTER);

        getContentPane().add(tengah, BorderLayout.CENTER);

        BtnTampilkan.addActionListener(e -> muatData());
        BtnKeluar.setText("Keluar");
        BtnKeluar.addActionListener(e -> dispose());
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 9));
        bawah.setBackground(Color.WHITE);
        bawah.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, garis));
        bawah.add(BtnKeluar);
        getContentPane().add(bawah, BorderLayout.SOUTH);
    }

    public void isCek() {
    }

    /** Dipanggil sekali saat dialog dibuka -- default tampilkan hari ini saja. */
    public void tampil() {
        Date hariIni = new Date();
        dtpDari.setDate(hariIni);
        dtpSampai.setDate(hariIni);
        muatData();
    }

    private void muatData() {
        String dari = ambilTanggal(dtpDari);
        String sampai = ambilTanggal(dtpSampai);
        if (dari.isEmpty() || sampai.isEmpty() || dari.compareTo(sampai) > 0) {
            JOptionPane.showMessageDialog(this, "Rentang tanggal tidak valid.");
            return;
        }
        String kw = "%" + TCari.getText().trim() + "%";
        Vector<Vector<Object>> data = new Vector<>();
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select no_rawat,no_rkm_medis,nm_pasien,status_pulang,tgl_pulang,jam_pulang,petugas_nama,petugas_nip,dicatat_pada "
                + "from log_pasien_pulang where tgl_pulang between ? and ? "
                + "and (no_rawat like ? or no_rkm_medis like ? or nm_pasien like ? or petugas_nama like ? or petugas_nip like ?) "
                + "order by dicatat_pada desc")) {
            ps.setString(1, dari);
            ps.setString(2, sampai);
            ps.setString(3, kw);
            ps.setString(4, kw);
            ps.setString(5, kw);
            ps.setString(6, kw);
            ps.setString(7, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> baris = new Vector<>();
                    baris.add(nvl(rs.getString("no_rawat")));
                    baris.add(nvl(rs.getString("no_rkm_medis")));
                    baris.add(nvl(rs.getString("nm_pasien")));
                    baris.add(nvl(rs.getString("status_pulang")));
                    baris.add(fmtTgl(rs.getString("tgl_pulang")));
                    baris.add(nvl(rs.getString("jam_pulang")));
                    String petugas = nvl(rs.getString("petugas_nama"));
                    baris.add(petugas.isEmpty() ? nvl(rs.getString("petugas_nip")) : petugas);
                    baris.add(rs.getTimestamp("dicatat_pada") == null ? "" : rs.getTimestamp("dicatat_pada").toString());
                    data.add(baris);
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat log pasien pulang : " + e);
        }
        tabMode.setDataVector(data, new Vector<>(java.util.Arrays.asList(
                "No. Rawat", "No. RM", "Nama Pasien", "Status Pulang", "Tanggal Pulang", "Jam Pulang", "Petugas", "Dicatat Pada")));
        aturLebarKolom();
    }

    private void aturLebarKolom() {
        int n = tbLog.getColumnModel().getColumnCount();
        if (n < 8) { return; }
        tbLog.getColumnModel().getColumn(0).setPreferredWidth(140);
        tbLog.getColumnModel().getColumn(1).setPreferredWidth(90);
        tbLog.getColumnModel().getColumn(2).setPreferredWidth(220);
        tbLog.getColumnModel().getColumn(3).setPreferredWidth(110);
        tbLog.getColumnModel().getColumn(4).setPreferredWidth(110);
        tbLog.getColumnModel().getColumn(5).setPreferredWidth(80);
        tbLog.getColumnModel().getColumn(6).setPreferredWidth(180);
        tbLog.getColumnModel().getColumn(7).setPreferredWidth(160);
    }

    private String ambilTanggal(widget.Tanggal d) {
        Object v = d.getSelectedItem();
        if (v == null) { return ""; }
        String s = v.toString();
        return s.length() >= 10 ? Valid.SetTgl(s.substring(0, 10)) : "";
    }

    private static String fmtTgl(String iso) {
        if (iso == null || iso.length() < 10) { return ""; }
        return iso.substring(8, 10) + "-" + iso.substring(5, 7) + "-" + iso.substring(0, 4);
    }

    private static widget.Tanggal dtTanggal() {
        widget.Tanggal d = new widget.Tanggal();
        d.setDisplayFormat("dd-MM-yyyy");
        return d;
    }

    private static String nvl(String v) {
        return v == null ? "" : v;
    }

}
