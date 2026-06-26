package surat;

import fungsi.akses;
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import kepegawaian.DlgCariDokter;

/**
 * Form Surat Keterangan Kontrol (versi baru). Dibuka dari tombol "Surat Kontrol"
 * (BtnSKDP) di DlgRawatJalan. Data disimpan ke tabel surat_kontrol (1 baris per
 * no_rawat, REPLACE INTO). Diagnosa/Terapi di-prefill otomatis dari data pasien
 * tapi bisa diedit. Cetak: rptSuratKontrol (kop RS + barcode TTD dokter).
 */
public final class SuratKontrolV2 extends JDialog {

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final DlgCariDokter dokter = new DlgCariDokter(null, true);

    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TTglLahir = ro();
    private final widget.TextBox TAlamat = ro();
    private final widget.TextBox TPoli = ro();

    private final widget.TextArea taDiagnosa = ta();
    private final widget.TextArea taTerapi = ta();
    private final widget.Tanggal dtpJadwal = dt();
    private final widget.TextArea taRencana = ta();
    private final widget.Tanggal dtpTanggalSurat = dt();
    private final widget.TextBox KdDokter = tf();
    private final widget.TextBox NmDokter = ro();
    private final widget.Button BtnDokter = new widget.Button();
    private final widget.Button BtnAmbilPlan = new widget.Button();

    private final widget.TextBox KdPetugas = ro();

    private final widget.Button BtnSimpan = new widget.Button();
    private final widget.Button BtnBaru = new widget.Button();
    private final widget.Button BtnHapus = new widget.Button();
    private final widget.Button BtnCetak = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();

    public SuratKontrolV2(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Surat Keterangan Kontrol ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
        dokter.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) {
                if (dokter.getTable().getSelectedRow() != -1) {
                    int r = dokter.getTable().getSelectedRow();
                    KdDokter.setText(dokter.getTable().getValueAt(r, 0).toString());
                    NmDokter.setText(dokter.getTable().getValueAt(r, 1).toString());
                }
            }
        });
        setSize(720, 560);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        getContentPane().setLayout(new BorderLayout(6, 6));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        int row = 0;
        row = judul(form, row, "Identitas Pasien");
        row = baris2(form, row, "No.Rawat", TNoRw, "No.RM", TNoRM);
        row = baris2(form, row, "Nama Pasien", TPasien, "Tanggal Lahir", TTglLahir);
        row = baris2(form, row, "Alamat", TAlamat, "Poliklinik", TPoli);

        row = judul(form, row, "Surat Keterangan Kontrol");
        row = area(form, row, "Diagnosa", taDiagnosa);
        row = areaDenganTombol(form, row, "Terapi", taTerapi, BtnAmbilPlan);
        row = baris1(form, row, "Jadwal Kembali Kontrol", dtpJadwal);
        row = area(form, row, "Rencana Tindak Lanjut", taRencana);
        row = baris1(form, row, "Tanggal Surat", dtpTanggalSurat);
        row = baris1(form, row, "Dokter", gabungBtn(KdDokter, NmDokter, BtnDokter));

        JScrollPane scroll = new JScrollPane(form);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        getContentPane().add(scroll, BorderLayout.CENTER);

        BtnSimpan.setText("Simpan");
        BtnBaru.setText("Baru");
        BtnHapus.setText("Hapus");
        BtnCetak.setText("Cetak");
        BtnKeluar.setText("Keluar");
        BtnDokter.setText("...");
        BtnDokter.setPreferredSize(new Dimension(34, 23));
        BtnSimpan.addActionListener(e -> simpan());
        BtnBaru.addActionListener(e -> emptTeks());
        BtnHapus.addActionListener(e -> hapus());
        BtnCetak.addActionListener(e -> cetak());
        BtnKeluar.addActionListener(e -> dispose());
        BtnAmbilPlan.addActionListener(e -> ambilPlan());
        BtnDokter.addActionListener(e -> {
            dokter.emptTeks();
            dokter.isCek();
            dokter.setSize(900, 540);
            dokter.setLocationRelativeTo(this);
            dokter.setVisible(true);
        });
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        bawah.add(BtnSimpan);
        bawah.add(BtnBaru);
        bawah.add(BtnHapus);
        bawah.add(BtnCetak);
        bawah.add(BtnKeluar);
        getContentPane().add(bawah, BorderLayout.SOUTH);

        dtpTanggalSurat.setDate(new Date());
        dtpJadwal.setDate(new Date());
    }

    // ===== helpers UI =====
    private static widget.TextBox tf() { return new widget.TextBox(); }

    private static widget.TextBox ro() {
        widget.TextBox t = new widget.TextBox();
        t.setEditable(false);
        return t;
    }

    private static widget.TextArea ta() {
        widget.TextArea t = new widget.TextArea();
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        return t;
    }

    private static widget.Tanggal dt() {
        widget.Tanggal d = new widget.Tanggal();
        d.setDisplayFormat("dd-MM-yyyy");
        return d;
    }

    private GridBagConstraints gc(int x, int y, int w, double wx) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x; g.gridy = y; g.gridwidth = w; g.weightx = wx;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(2, 4, 2, 4);
        return g;
    }

    private int judul(JPanel p, int row, String teks) {
        JLabel l = new JLabel(teks);
        l.setOpaque(true);
        l.setBackground(new Color(225, 240, 225));
        l.setForeground(new Color(30, 90, 30));
        l.setFont(new Font("Tahoma", Font.BOLD, 12));
        l.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 6, 5, 6));
        GridBagConstraints g = gc(0, row, 4, 1.0);
        g.insets = new Insets(10, 4, 2, 4);
        p.add(l, g);
        return row + 1;
    }

    private int baris1(JPanel p, int row, String label, Component comp) {
        p.add(lbl(label), gc(0, row, 1, 0.0));
        siz(comp);
        p.add(comp, gc(1, row, 3, 1.0));
        return row + 1;
    }

    private int baris2(JPanel p, int row, String l1, Component c1, String l2, Component c2) {
        p.add(lbl(l1), gc(0, row, 1, 0.0));
        siz(c1);
        p.add(c1, gc(1, row, 1, 0.5));
        p.add(lbl(l2), gc(2, row, 1, 0.0));
        siz(c2);
        p.add(c2, gc(3, row, 1, 0.5));
        return row + 1;
    }

    private int area(JPanel p, int row, String label, widget.TextArea a) {
        p.add(lbl(label), gc(0, row, 1, 0.0));
        JScrollPane sc = new JScrollPane(a);
        sc.setPreferredSize(new Dimension(420, 50));
        sc.setWheelScrollingEnabled(false);
        p.add(sc, gc(1, row, 3, 1.0));
        return row + 1;
    }

    private int areaDenganTombol(JPanel p, int row, String label, widget.TextArea a, widget.Button btn) {
        p.add(lbl(label), gc(0, row, 1, 0.0));
        JScrollPane sc = new JScrollPane(a);
        sc.setPreferredSize(new Dimension(360, 50));
        sc.setWheelScrollingEnabled(false);
        p.add(sc, gc(1, row, 2, 1.0));
        btn.setText("Ambil");
        btn.setPreferredSize(new Dimension(72, 24));
        GridBagConstraints g = gc(3, row, 1, 0.0);
        g.fill = GridBagConstraints.NONE;
        p.add(btn, g);
        return row + 1;
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t + " :");
        l.setFont(new Font("Tahoma", Font.PLAIN, 11));
        return l;
    }

    private void siz(Component c) {
        if (c instanceof widget.TextBox || c instanceof widget.Tanggal) {
            c.setPreferredSize(new Dimension(220, 23));
        }
    }

    private JPanel gabungBtn(Component a, Component b, Component btn) {
        JPanel pnl = new JPanel(new BorderLayout(3, 0));
        pnl.setOpaque(false);
        JPanel kiri = new JPanel(new GridLayout(1, 2, 3, 0));
        kiri.setOpaque(false);
        a.setPreferredSize(new Dimension(80, 23));
        kiri.add(a);
        kiri.add(b);
        pnl.add(kiri, BorderLayout.CENTER);
        if (btn != null) { pnl.add(btn, BorderLayout.EAST); }
        return pnl;
    }

    // ===== entry points =====
    public void isCek() {
        boolean bisa = akses.getskdp_bpjs();
        BtnSimpan.setEnabled(bisa);
        BtnHapus.setEnabled(bisa);
        KdPetugas.setText(akses.getkode());
    }

    public void emptTeks() {
        TNoRw.setText("");
        TNoRM.setText("");
        TPasien.setText("");
        TTglLahir.setText("");
        TAlamat.setText("");
        TPoli.setText("");
        taDiagnosa.setText("");
        taTerapi.setText("");
        taRencana.setText("");
        KdDokter.setText("");
        NmDokter.setText("");
        dtpTanggalSurat.setDate(new Date());
        dtpJadwal.setDate(new Date());
    }

    /** Dipanggil dari DlgRawatJalan saat tombol Surat Kontrol ditekan. */
    public void setData(String norawat, String norm, String nama, String kodepoli,
            String namapoli, String kodedokter, String namadokter) {
        emptTeks();
        TNoRw.setText(norawat);
        TNoRM.setText(norm);
        TPasien.setText(nama);
        TPoli.setText(namapoli);
        KdDokter.setText(kodedokter);
        NmDokter.setText(namadokter);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select ifnull(date_format(tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir,ifnull(alamat,'') as alamat "
                + "from pasien where no_rkm_medis=?")) {
            ps.setString(1, norm);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TTglLahir.setText(rs.getString("tgl_lahir"));
                    TAlamat.setText(rs.getString("alamat"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif identitas surat kontrol : " + e);
        }
        muat();
    }

    private void prefillDiagnosaTerapi() {
        String diag = Sequel.cariIsi(
                "select group_concat(distinct penyakit.nm_penyakit separator ', ') "
                + "from diagnosa_pasien inner join penyakit on penyakit.kd_penyakit=diagnosa_pasien.kd_penyakit "
                + "where diagnosa_pasien.no_rawat=?", TNoRw.getText());
        if (diag != null && !diag.trim().isEmpty()) {
            taDiagnosa.setText(diag);
        }
        String terapi = Sequel.cariIsi(
                "select group_concat(distinct databarang.nama_brng separator ', ') "
                + "from resep_obat inner join resep_dokter on resep_dokter.no_resep=resep_obat.no_resep "
                + "inner join databarang on databarang.kode_brng=resep_dokter.kode_brng "
                + "where resep_obat.no_rawat=?", TNoRw.getText());
        if (terapi != null && !terapi.trim().isEmpty()) {
            taTerapi.setText(terapi);
        }
    }

    // ===== simpan / muat / hapus =====
    private void simpan() {
        if (TNoRw.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "replace into surat_kontrol "
                + "(no_rawat,tanggal_surat,diagnosa,terapi,jadwal_kontrol,rencana_tindak_lanjut,kd_dokter,nik) "
                + "values (?,?,?,?,?,?,?,?)")) {
            ps.setString(1, TNoRw.getText());
            ps.setString(2, Valid.SetTgl(dtpTanggalSurat.getSelectedItem() + ""));
            ps.setString(3, taDiagnosa.getText());
            ps.setString(4, taTerapi.getText());
            ps.setString(5, Valid.SetTgl(dtpJadwal.getSelectedItem() + ""));
            ps.setString(6, taRencana.getText());
            ps.setString(7, KdDokter.getText());
            ps.setString(8, KdPetugas.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Surat keterangan kontrol tersimpan.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan.\n" + e.getMessage());
        }
    }

    private void muat() {
        boolean ada = false;
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select * from surat_kontrol where no_rawat=?")) {
            ps.setString(1, TNoRw.getText());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ada = true;
                    taDiagnosa.setText(g(rs, "diagnosa"));
                    taTerapi.setText(g(rs, "terapi"));
                    taRencana.setText(g(rs, "rencana_tindak_lanjut"));
                    setTgl(dtpTanggalSurat, rs.getString("tanggal_surat"));
                    setTgl(dtpJadwal, rs.getString("jadwal_kontrol"));
                    String kd = g(rs, "kd_dokter");
                    if (!kd.isEmpty()) {
                        KdDokter.setText(kd);
                        NmDokter.setText(Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", kd));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat surat kontrol : " + e);
        }
        if (!ada) {
            prefillDiagnosaTerapi();
        }
    }

    private void hapus() {
        if (TNoRw.getText().trim().equals("")) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus surat kontrol untuk No.Rawat " + TNoRw.getText() + " ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement("delete from surat_kontrol where no_rawat=?")) {
            ps.setString(1, TNoRw.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data dihapus.");
            taDiagnosa.setText("");
            taTerapi.setText("");
            taRencana.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus.\n" + e.getMessage());
        }
    }

    /** Ambil kolom Plan (rtl) dari SOAP dokter (pemeriksaan_ralan) ke field Terapi.
     *  Logika sama dengan Resume Medis Ranap V2 (di sini sumbernya pemeriksaan_ralan). */
    private void ambilPlan() {
        if (TNoRw.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        final DefaultTableModel model = new DefaultTableModel(null, new Object[]{"Tanggal", "Jam", "Dokter", "Plan"}) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select pemeriksaan_ralan.tgl_perawatan,pemeriksaan_ralan.jam_rawat,"
                + "ifnull(pemeriksaan_ralan.rtl,'') as plan_soap,pegawai.nama "
                + "from pemeriksaan_ralan inner join pegawai on pemeriksaan_ralan.nip=pegawai.nik "
                + "where pemeriksaan_ralan.no_rawat=? "
                + "and (pegawai.jbtn like '%Dokter%' or pegawai.nama like 'dr.%' or pegawai.nama like 'dr %' or pegawai.nama like 'drg%') "
                + "and ifnull(pemeriksaan_ralan.rtl,'')<>'' "
                + "order by pemeriksaan_ralan.tgl_perawatan desc,pemeriksaan_ralan.jam_rawat desc")) {
            ps.setString(1, TNoRw.getText());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getString("tgl_perawatan"), rs.getString("jam_rawat"),
                        rs.getString("nama"), rs.getString("plan_soap")});
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat Plan SOAP : " + e.getMessage());
            return;
        }
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Data Plan (SOAP dokter) untuk pasien ini belum ditemukan.");
            return;
        }
        final JDialog dlg = new JDialog(this, "Ambil Plan Dokter", true);
        final JTable tabel = new JTable(model);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.setRowHeight(44);
        tabel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] w = {90, 60, 170, 520};
        for (int i = 0; i < w.length; i++) {
            tabel.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        JScrollPane sc = new JScrollPane(tabel);
        widget.Button bPilih = new widget.Button();
        bPilih.setText("Pilih");
        widget.Button bBatal = new widget.Button();
        bBatal.setText("Batal");
        final java.awt.event.ActionListener pilih = ev -> {
            int row = tabel.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dlg, "Pilih satu baris terlebih dahulu.");
                return;
            }
            Object v = model.getValueAt(tabel.convertRowIndexToModel(row), 3);
            taTerapi.setText(v == null ? "" : v.toString());
            dlg.dispose();
        };
        bPilih.addActionListener(pilih);
        bBatal.addActionListener(ev -> dlg.dispose());
        tabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) { pilih.actionPerformed(null); }
            }
        });
        JPanel aksi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        aksi.add(bPilih);
        aksi.add(bBatal);
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.add(new JLabel(" Pilih SOAP dokter — kolom Plan akan dimasukkan ke Terapi."), BorderLayout.NORTH);
        pnl.add(sc, BorderLayout.CENTER);
        pnl.add(aksi, BorderLayout.SOUTH);
        dlg.setContentPane(pnl);
        dlg.setSize(900, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ===== cetak =====
    private void cetak() {
        if (TNoRw.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        if (Sequel.cariInteger("select count(no_rawat) from surat_kontrol where no_rawat=?", TNoRw.getText()) <= 0) {
            JOptionPane.showMessageDialog(this, "Simpan surat kontrol terlebih dahulu sebelum mencetak.");
            return;
        }
        try {
            siapkanReport("rptSuratKontrol");
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            param.put("kota_ttd", akses.getkabupatenrs());
            param.put("finger", buatQrDokter());
            Valid.MyReportqry(
                    "rptSuratKontrol.jasper",
                    "report",
                    "::[ Surat Keterangan Kontrol ]::",
                    "select sk.no_rawat,p.no_rkm_medis,p.nm_pasien,"
                    + "ifnull(date_format(p.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir,ifnull(p.alamat,'') as alamat,"
                    + "ifnull(sk.diagnosa,'') as diagnosa,ifnull(sk.terapi,'') as terapi,"
                    + "ifnull(date_format(sk.jadwal_kontrol,'%d-%m-%Y'),'') as jadwal_kontrol,"
                    + "ifnull(sk.rencana_tindak_lanjut,'') as rencana,"
                    + "ifnull(date_format(sk.tanggal_surat,'%d-%m-%Y'),'') as tanggal_surat,"
                    + "ifnull(d.nm_dokter,'') as nm_dokter,ifnull(sk.kd_dokter,'') as kd_dokter "
                    + "from surat_kontrol sk inner join reg_periksa rp on rp.no_rawat=sk.no_rawat "
                    + "inner join pasien p on p.no_rkm_medis=rp.no_rkm_medis "
                    + "left join dokter d on d.kd_dokter=sk.kd_dokter "
                    + "where sk.no_rawat='" + TNoRw.getText() + "'",
                    param);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal cetak surat kontrol : " + e.getMessage());
        }
    }

    /** Teks QR tanda tangan dokter, pola sama dengan Resume Medis Ranap V2. */
    private String buatQrDokter() {
        String kd = KdDokter.getText();
        String finger = Sequel.cariIsi(
                "select sha1(sidikjari.sidikjari) from sidikjari "
                + "inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", kd);
        finger = finger == null ? "" : finger;
        String tgl = dtpTanggalSurat.getSelectedItem() == null ? "" : dtpTanggalSurat.getSelectedItem().toString();
        return "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs()
                + "\nDitandatangani secara elektronik oleh " + NmDokter.getText()
                + "\nID " + (finger.equals("") ? kd : finger)
                + "\n" + tgl;
    }

    private void siapkanReport(String baseName) throws Exception {
        java.io.File jrxml = new java.io.File("./report/" + baseName + ".jrxml");
        java.io.File jasper = new java.io.File("./report/" + baseName + ".jasper");
        if (!jrxml.exists()) {
            throw new Exception("File report " + baseName + ".jrxml tidak ditemukan.");
        }
        if (!jasper.exists() || jrxml.lastModified() > jasper.lastModified()) {
            net.sf.jasperreports.engine.JasperCompileManager.compileReportToFile(jrxml.getPath(), jasper.getPath());
        }
    }

    // ===== util =====
    private String g(ResultSet rs, String kolom) {
        try {
            String v = rs.getString(kolom);
            return v == null ? "" : v;
        } catch (Exception e) {
            return "";
        }
    }

    private void setTgl(widget.Tanggal picker, String tgl) {
        if (tgl == null || tgl.startsWith("0000") || tgl.trim().equals("")) {
            picker.setDate(new Date());
            return;
        }
        try {
            picker.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(tgl.substring(0, 10)));
        } catch (Exception e) {
            picker.setDate(new Date());
        }
    }
}
