package bridging;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public final class SatuSehatBuatPermintaanLabPKDariKunjungan extends JDialog {
    private final Connection koneksi = koneksiDB.condb();
    private final validasi Valid = new validasi();
    private final DefaultTableModel tabMode;
    private final JTable tbData = new JTable();
    private final widget.Tanggal DTPCari1 = new widget.Tanggal();
    private final widget.Tanggal DTPCari2 = new widget.Tanggal();
    private final JTextField TCari = new JTextField(30);
    private final JLabel LCount = new JLabel("0");
    private final JButton BtnTampil = new JButton("Tampil");
    private final JButton BtnSemua = new JButton("Semua");
    private final JButton BtnPilihSemua = new JButton("Pilih Semua");
    private final JButton BtnBersihkan = new JButton("Bersihkan");
    private final JButton BtnBuat = new JButton("Buat Request");
    private final JButton BtnKeluar = new JButton("Keluar");

    public SatuSehatBuatPermintaanLabPKDariKunjungan(Frame parent, boolean modal) {
        super(parent, modal);
        tabMode = new DefaultTableModel(null, new Object[]{
            "P", "Tgl Periksa", "Jam", "No.Rawat", "No.RM", "Nama Pasien",
            "Kode Dokter", "Dokter Perujuk", "Diagnosa", "Tindakan", "Detail Pemeriksaan"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex == 0;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
        };
        initComponents();
        tampil();
    }

    private void initComponents() {
        setTitle("::[ Buat Request Lab PK Dari Kunjungan Langsung ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(6, 6));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        top.add(new JLabel("Tgl.Periksa :"));
        top.add(DTPCari1);
        top.add(new JLabel("s.d."));
        top.add(DTPCari2);
        top.add(new JLabel("Keyword :"));
        top.add(TCari);
        top.add(BtnTampil);
        add(top, BorderLayout.NORTH);

        tbData.setModel(tabMode);
        tbData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbData.setRowHeight(20);
        setColumnWidths();
        add(new JScrollPane(tbData), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        bottom.add(new JLabel("Record :"));
        LCount.setPreferredSize(new Dimension(60, 23));
        bottom.add(LCount);
        bottom.add(BtnSemua);
        bottom.add(BtnPilihSemua);
        bottom.add(BtnBersihkan);
        bottom.add(BtnBuat);
        bottom.add(BtnKeluar);
        add(bottom, BorderLayout.SOUTH);

        BtnTampil.addActionListener(evt -> tampil());
        BtnSemua.addActionListener(evt -> {
            TCari.setText("");
            tampil();
        });
        BtnPilihSemua.addActionListener(evt -> setSemuaPilihan(true));
        BtnBersihkan.addActionListener(evt -> setSemuaPilihan(false));
        BtnBuat.addActionListener(evt -> prosesBuatRequest());
        BtnKeluar.addActionListener(evt -> dispose());

        TCari.addActionListener(evt -> tampil());
        if (koneksiDB.CARICEPAT().equals("aktif")) {
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { if (TCari.getText().length() > 2) tampil(); }
                @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { if (TCari.getText().length() > 2 || TCari.getText().trim().isEmpty()) tampil(); }
                @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { if (TCari.getText().length() > 2) tampil(); }
            });
        }

        setPreferredSize(new Dimension(1220, 650));
        pack();
    }

    private void setColumnWidths() {
        int[] widths = new int[]{20, 90, 75, 110, 80, 180, 0, 180, 180, 200, 340};
        for (int i = 0; i < widths.length; i++) {
            TableColumn column = tbData.getColumnModel().getColumn(i);
            if (widths[i] == 0) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
                column.setPreferredWidth(0);
            } else {
                column.setPreferredWidth(widths[i]);
            }
        }
    }

    public void isCek() {
        BtnBuat.setEnabled(akses.getpermintaan_lab());
    }

    private void tampil() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            while (tabMode.getRowCount() > 0) {
                tabMode.removeRow(0);
            }

            String sql = "select periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien," +
                    "periksa_lab.dokter_perujuk,ifnull(dokter.nm_dokter,'') as nm_dokter," +
                    "ifnull((select penyakit.nm_penyakit from diagnosa_pasien inner join penyakit on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit " +
                    "where diagnosa_pasien.no_rawat=periksa_lab.no_rawat order by diagnosa_pasien.prioritas asc limit 1),'') as diagnosa_klinis," +
                    "group_concat(distinct jns_perawatan_lab.nm_perawatan order by jns_perawatan_lab.nm_perawatan separator ', ') as tindakan," +
                    "group_concat(distinct template_laboratorium.Pemeriksaan order by template_laboratorium.Pemeriksaan separator ', ') as detail_pemeriksaan " +
                    "from periksa_lab " +
                    "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat " +
                    "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                    "left join dokter on periksa_lab.dokter_perujuk=dokter.kd_dokter " +
                    "inner join jns_perawatan_lab on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw " +
                    "inner join detail_periksa_lab on detail_periksa_lab.no_rawat=periksa_lab.no_rawat and detail_periksa_lab.kd_jenis_prw=periksa_lab.kd_jenis_prw " +
                    "and detail_periksa_lab.tgl_periksa=periksa_lab.tgl_periksa and detail_periksa_lab.jam=periksa_lab.jam " +
                    "inner join template_laboratorium on template_laboratorium.id_template=detail_periksa_lab.id_template " +
                    "where periksa_lab.kategori='PK' and periksa_lab.status='Ralan' and periksa_lab.tgl_periksa between ? and ? " +
                    "and not exists(select 1 from permintaan_lab where permintaan_lab.no_rawat=periksa_lab.no_rawat and permintaan_lab.tgl_hasil=periksa_lab.tgl_periksa and permintaan_lab.jam_hasil=periksa_lab.jam) " +
                    "and (periksa_lab.no_rawat like ? or reg_periksa.no_rkm_medis like ? or pasien.nm_pasien like ? or ifnull(dokter.nm_dokter,'') like ? or jns_perawatan_lab.nm_perawatan like ? or template_laboratorium.Pemeriksaan like ?) " +
                    "group by periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,periksa_lab.dokter_perujuk,dokter.nm_dokter " +
                    "order by periksa_lab.tgl_periksa desc,periksa_lab.jam desc";
            ps = koneksi.prepareStatement(sql);
            ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + ""));
            ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + ""));
            for (int i = 3; i <= 8; i++) {
                ps.setString(i, "%" + TCari.getText().trim() + "%");
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                tabMode.addRow(new Object[]{
                    false,
                    rs.getString("tgl_periksa"),
                    rs.getString("jam"),
                    rs.getString("no_rawat"),
                    rs.getString("no_rkm_medis"),
                    rs.getString("nm_pasien"),
                    rs.getString("dokter_perujuk"),
                    rs.getString("nm_dokter"),
                    rs.getString("diagnosa_klinis"),
                    rs.getString("tindakan"),
                    rs.getString("detail_pemeriksaan")
                });
            }
            LCount.setText(String.valueOf(tabMode.getRowCount()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menampilkan data kunjungan laboratorium langsung.\n" + e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ex) {
                System.out.println("Notifikasi : " + ex);
            }
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void setSemuaPilihan(boolean pilih) {
        for (int i = 0; i < tabMode.getRowCount(); i++) {
            tabMode.setValueAt(pilih, i, 0);
        }
    }

    private void prosesBuatRequest() {
        if (!akses.getpermintaan_lab()) {
            JOptionPane.showMessageDialog(this, "Hak akses permintaan lab belum tersedia.");
            return;
        }
        int dibuat = 0;
        int dilewati = 0;
        StringBuilder gagal = new StringBuilder();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            for (int i = 0; i < tabMode.getRowCount(); i++) {
                if (Boolean.TRUE.equals(tabMode.getValueAt(i, 0))) {
                    try {
                        if (buatRequest(i)) {
                            dibuat++;
                        } else {
                            dilewati++;
                        }
                    } catch (Exception e) {
                        if (gagal.length() < 1200) {
                            gagal.append(tabMode.getValueAt(i, 3)).append(" / ")
                                 .append(tabMode.getValueAt(i, 5)).append(" : ")
                                 .append(e.getMessage()).append("\n");
                        }
                    }
                }
            }
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
        tampil();
        JOptionPane.showMessageDialog(this,
                "Pembuatan request selesai.\n" +
                "Berhasil : " + dibuat + "\n" +
                "Dilewati : " + dilewati + "\n" +
                (gagal.length() > 0 ? "\nGagal :\n" + gagal : ""));
    }

    private boolean buatRequest(int row) throws Exception {
        String tglPeriksa = valueAt(row, 1);
        String jamPeriksa = valueAt(row, 2);
        String noRawat = valueAt(row, 3);
        String kdDokterPerujuk = valueAt(row, 6);
        String diagnosa = potong(valueAt(row, 8), 80, "-");
        String status = "ralan";

        if (sudahAdaPermintaan(noRawat, tglPeriksa, jamPeriksa)) {
            return false;
        }

        boolean autoCommit = koneksi.getAutoCommit();
        koneksi.setAutoCommit(false);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String noorder = generateNoOrder(tglPeriksa);

            ps = koneksi.prepareStatement(
                    "insert into permintaan_lab(noorder,no_rawat,tgl_permintaan,jam_permintaan,tgl_sampel,jam_sampel,tgl_hasil,jam_hasil,dokter_perujuk,status,informasi_tambahan,diagnosa_klinis) " +
                    "values (?,?,?,?,?,?,?,?,?,?,?,?)"
            );
            ps.setString(1, noorder);
            ps.setString(2, noRawat);
            ps.setString(3, tglPeriksa);
            ps.setString(4, jamPeriksa);
            ps.setString(5, tglPeriksa);
            ps.setString(6, jamPeriksa);
            ps.setString(7, tglPeriksa);
            ps.setString(8, jamPeriksa);
            ps.setString(9, kdDokterPerujuk);
            ps.setString(10, status);
            ps.setString(11, "Dari kunjungan lab");
            ps.setString(12, diagnosa);
            ps.executeUpdate();
            ps.close();

            int jumlahPemeriksaan = 0;
            ps = koneksi.prepareStatement(
                    "select distinct kd_jenis_prw from periksa_lab where kategori='PK' and status='Ralan' and no_rawat=? and tgl_periksa=? and jam=? order by kd_jenis_prw"
            );
            ps.setString(1, noRawat);
            ps.setString(2, tglPeriksa);
            ps.setString(3, jamPeriksa);
            rs = ps.executeQuery();
            while (rs.next()) {
                jumlahPemeriksaan++;
                PreparedStatement simpan = koneksi.prepareStatement(
                        "insert into permintaan_pemeriksaan_lab(noorder,kd_jenis_prw,stts_bayar) values (?,?,?)"
                );
                simpan.setString(1, noorder);
                simpan.setString(2, rs.getString("kd_jenis_prw"));
                simpan.setString(3, "Sudah");
                simpan.executeUpdate();
                simpan.close();
            }
            rs.close();
            ps.close();

            int jumlahDetail = 0;
            ps = koneksi.prepareStatement(
                    "select distinct kd_jenis_prw,id_template from detail_periksa_lab where no_rawat=? and tgl_periksa=? and jam=? order by kd_jenis_prw,id_template"
            );
            ps.setString(1, noRawat);
            ps.setString(2, tglPeriksa);
            ps.setString(3, jamPeriksa);
            rs = ps.executeQuery();
            while (rs.next()) {
                jumlahDetail++;
                PreparedStatement simpan = koneksi.prepareStatement(
                        "insert into permintaan_detail_permintaan_lab(noorder,kd_jenis_prw,id_template,stts_bayar) values (?,?,?,?)"
                );
                simpan.setString(1, noorder);
                simpan.setString(2, rs.getString("kd_jenis_prw"));
                simpan.setString(3, rs.getString("id_template"));
                simpan.setString(4, "Sudah");
                simpan.executeUpdate();
                simpan.close();
            }
            rs.close();
            ps.close();

            if (jumlahPemeriksaan == 0 || jumlahDetail == 0) {
                throw new Exception("Detail pemeriksaan laboratorium belum lengkap.");
            }

            koneksi.commit();
            return true;
        } catch (Exception e) {
            koneksi.rollback();
            throw e;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ex) {
                System.out.println("Notifikasi : " + ex);
            }
            koneksi.setAutoCommit(autoCommit);
        }
    }

    private boolean sudahAdaPermintaan(String noRawat, String tglPeriksa, String jamPeriksa) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement(
                    "select noorder from permintaan_lab where no_rawat=? and tgl_hasil=? and jam_hasil=? limit 1"
            );
            ps.setString(1, noRawat);
            ps.setString(2, tglPeriksa);
            ps.setString(3, jamPeriksa);
            rs = ps.executeQuery();
            return rs.next();
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        }
    }

    private String generateNoOrder(String tglPeriksa) {
        return Valid.autoNomer3(
                "select ifnull(MAX(CONVERT(RIGHT(noorder,4),signed)),0) from permintaan_lab where tgl_permintaan='" + tglPeriksa + "' ",
                "PL" + tglPeriksa.replaceAll("-", ""),
                4
        );
    }

    private String valueAt(int row, int col) {
        Object value = tabMode.getValueAt(row, col);
        return value == null ? "" : value.toString();
    }

    private String potong(String nilai, int panjang, String fallback) {
        String hasil = nilai == null ? "" : nilai.trim();
        if (hasil.isEmpty()) {
            hasil = fallback;
        }
        if (hasil.length() > panjang) {
            hasil = hasil.substring(0, panjang);
        }
        return hasil;
    }
}
