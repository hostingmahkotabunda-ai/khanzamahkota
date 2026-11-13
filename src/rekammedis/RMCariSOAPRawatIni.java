package rekammedis;

import fungsi.koneksiDB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Jendela ringan (non-modal) utk menampilkan SEMUA catatan SOAP (dokter maupun perawat,
 * tidak difilter per-nip) milik SATU no_rawat saja -- dipakai dari dalam form CP (Clinical
 * Pathway) spy petugas bisa lihat SOAP tanpa harus keluar dari form CP yg sedang diisi.
 * Beda dgn RMCari5SOAPTerakhir (itu lintas kunjungan/per no_rkm_medis, dibatasi 1 nip & 5 baris).
 */
public final class RMCariSOAPRawatIni extends javax.swing.JDialog {

    private static final Color BIRU = new Color(24, 112, 220);
    private static final Color TEKS = new Color(58, 80, 109);
    private static final Color GARIS = new Color(207, 220, 235);
    private static final Color BG_SOFT = new Color(245, 248, 252);
    private static final String NAMA_FONT = "Segoe UI";
    private static final Font FONT_LABEL = new Font(NAMA_FONT, Font.PLAIN, 13);

    private final Connection koneksi = koneksiDB.condb();
    private final DefaultTableModel model;
    private final JTable tabel;
    private final JLabel lblSub;
    private String noRawat = "";

    public RMCariSOAPRawatIni(Frame parent) {
        super(parent, "Riwayat SOAP", false);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BIRU);
        header.setBorder(new EmptyBorder(10, 16, 10, 16));
        JLabel judul = new JLabel("Riwayat SOAP Rawatan Ini");
        judul.setFont(new Font(NAMA_FONT, Font.BOLD, 17));
        judul.setForeground(Color.WHITE);
        lblSub = new JLabel(" ");
        lblSub.setFont(FONT_LABEL);
        lblSub.setForeground(new Color(222, 234, 252));
        JPanel teksHeader = new JPanel(new GridLayout(2, 1));
        teksHeader.setOpaque(false);
        teksHeader.add(judul);
        teksHeader.add(lblSub);
        header.add(teksHeader, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        Object[] kolom = {"Tanggal", "Jam", "Petugas", "Subjek", "Objek", "Asesmen", "Plan", "Instruksi", "Evaluasi"};
        model = new DefaultTableModel(null, kolom) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabel = new JTable(model);
        tabel.setRowHeight(80);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.setGridColor(GARIS);
        tabel.setShowGrid(true);
        tabel.getTableHeader().setBackground(BG_SOFT);
        tabel.getTableHeader().setForeground(TEKS);
        tabel.getTableHeader().setFont(new Font(NAMA_FONT, Font.BOLD, 13));
        tabel.setFont(FONT_LABEL);
        tabel.getColumnModel().getColumn(0).setPreferredWidth(80);
        tabel.getColumnModel().getColumn(1).setPreferredWidth(60);
        tabel.getColumnModel().getColumn(2).setPreferredWidth(140);
        for (int i = 3; i <= 8; i++) {
            tabel.getColumnModel().getColumn(i).setPreferredWidth(220);
            tabel.getColumnModel().getColumn(i).setCellRenderer(new BungkusTeksRenderer());
        }

        JScrollPane scroll = new JScrollPane(tabel);
        scroll.setBorder(BorderFactory.createLineBorder(GARIS));
        add(scroll, BorderLayout.CENTER);

        setSize(1180, 480);
    }

    /** Muat ulang & tampilkan utk no_rawat tertentu (dipanggil ulang tiap diklik dari form CP). */
    public void tampilkan(String noRawat, Component posisiRelatif) {
        this.noRawat = noRawat;
        lblSub.setText("No. Rawat: " + noRawat);
        muat();
        // digeser dikit dari posisi form pemanggil (bukan persis menimpa) spy form CP di
        // belakangnya tetap keliatan sebagian -- petugas bisa lirik2 tanpa harus geser manual.
        if (posisiRelatif != null) {
            java.awt.Point p = posisiRelatif.getLocationOnScreen();
            setLocation(p.x + 50, p.y + 50);
        } else {
            setLocationRelativeTo(null);
        }
        setVisible(true);
        toFront();
    }

    private void muat() {
        model.setRowCount(0);
        String sql =
            "select tgl_perawatan,jam_rawat,coalesce(peg.nama,pr.nip) as petugas,pr.keluhan,pr.pemeriksaan,pr.penilaian,pr.rtl,pr.instruksi,pr.evaluasi " +
            "from pemeriksaan_ralan pr left join pegawai peg on pr.nip=peg.nik where pr.no_rawat=? " +
            "union all " +
            "select tgl_perawatan,jam_rawat,coalesce(peg.nama,pn.nip) as petugas,pn.keluhan,pn.pemeriksaan,pn.penilaian,pn.rtl,pn.instruksi,pn.evaluasi " +
            "from pemeriksaan_ranap pn left join pegawai peg on pn.nip=peg.nik where pn.no_rawat=? " +
            "order by 1,2";
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noRawat);
            ps.setString(2, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getDate(1) == null ? "" : df.format(rs.getDate(1)),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getString(6),
                        rs.getString(7), rs.getString(8), rs.getString(9)
                    });
                }
            }
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Belum ada catatan SOAP utk rawatan ini.", "Riwayat SOAP", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat SOAP: " + e.getMessage());
        }
    }

    /** Renderer teks panjang (Subjek/Objek/dst) biar wrap, tidak kepotong satu baris. */
    private static final class BungkusTeksRenderer extends JTextArea
            implements javax.swing.table.TableCellRenderer {
        BungkusTeksRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setFont(FONT_LABEL);
            setBorder(new EmptyBorder(4, 6, 4, 6));
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            setBackground(isSelected ? new Color(229, 240, 254) : (row % 2 == 0 ? Color.WHITE : BG_SOFT));
            return this;
        }
    }
}
