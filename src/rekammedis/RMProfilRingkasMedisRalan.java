package rekammedis;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JasperCompileManager;

/**
 * Profil Ringkas Medis Rawat Jalan (RM.01) -- diisi manual per baris oleh petugas (bukan
 * rangkuman otomatis dari tab lain), sesuai kertas aslinya: No, Tgl/Jam, Diagnosa (ICD X),
 * Uraian Klinis Penting, Penunjang, Riwayat Rawat Inap/Operasi, Obat-Obatan/Rencana Tindak
 * Lanjut, Paraf DPJP/Petugas. 1 pasien (no_rkm_medis) bisa punya banyak baris, ditambah dari
 * kunjungan manapun. Paraf ditarik otomatis dari foto TTD petugas yg sedang login
 * (pegawai.photo, pola sama dgn RMTransferPasienInternal/RMRingkasanRiwayatMasuk).
 */
public final class RMProfilRingkasMedisRalan extends javax.swing.JDialog {

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final Map<String, ImageIcon> cacheFotoTtd = new HashMap<>();

    private String noRM = "";
    private String noRawat = "";
    private Integer idTerpilih = null;

    private final widget.TextBox TNama = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.Tanggal dtpTglJam = dt();
    private final widget.TextArea taDiagnosa = ta();
    private final widget.TextArea taKlinis = ta();
    private final widget.TextArea taPenunjang = ta();
    private final widget.TextArea taRiwayatInap = ta();
    private final widget.TextArea taObatRtl = ta();
    private final JLabel lblPetugas = new JLabel("-");
    private final JLabel lblFotoPetugas = new JLabel();

    private final DefaultTableModel tabMode = new DefaultTableModel(null,
            new Object[]{"ID", "Tgl / Jam", "Diagnosa (ICD X)", "Uraian Klinis Penting", "Petugas"}) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final widget.Table tbBaris = new widget.Table();

    private final widget.Button BtnBaru = new widget.Button();
    private final widget.Button BtnSimpan = new widget.Button();
    private final widget.Button BtnHapus = new widget.Button();
    private final widget.Button BtnCetak = new widget.Button();
    private final widget.Button BtnTutup = new widget.Button();

    public RMProfilRingkasMedisRalan(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("Profil Ringkas Medis Rawat Jalan");
        pastikanTabel();
        bangunTampilan();
        setSize(1180, 720);
    }

    public void setNoRM(String noRM, String namaPasien) {
        setNoRM(noRM, namaPasien, "");
    }

    /** noRawat = kunjungan yg sedang dibuka di DlgRawatJalan -- dipakai utk tarik otomatis
     *  Diagnosa & Plan (lihat autoTarikDiagnosaPlan()). Boleh kosong (mis. dipanggil dari
     *  tempat lain yg belum tahu no_rawat spesifik); kalau kosong, tidak ada yg ditarik. */
    public void setNoRM(String noRM, String namaPasien, String noRawat) {
        this.noRM = noRM == null ? "" : noRM.trim();
        this.noRawat = noRawat == null ? "" : noRawat.trim();
        TNoRM.setText(this.noRM);
        TNama.setText(namaPasien == null ? "" : namaPasien);
        kosongkanForm();
        muatDaftar();
        setPetugasSekarang();
    }

    private void pastikanTabel() {
        try (Statement st = koneksi.createStatement()) {
            st.executeUpdate(
                "create table if not exists profil_ringkas_medis_ralan ("
                + "id int not null auto_increment,"
                + "no_rkm_medis varchar(15) not null,"
                + "tanggal date not null,"
                + "jam time not null,"
                + "diagnosa text,"
                + "uraian_klinis text,"
                + "penunjang text,"
                + "riwayat_inap_operasi text,"
                + "obat_rtl text,"
                + "kd_petugas varchar(20) not null default '',"
                + "nama_petugas varchar(50) not null default '',"
                + "created_by varchar(20) not null default '',"
                + "created_at datetime not null,"
                + "updated_by varchar(20) not null default '',"
                + "updated_at datetime null,"
                + "primary key (id),"
                + "key no_rkm_medis (no_rkm_medis)"
                + ") engine=InnoDB default charset=latin1");
        } catch (Exception e) {
            System.out.println("Notif pastikan tabel profil ringkas medis ralan : " + e);
        }
    }

    private void bangunTampilan() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(Color.WHITE);

        JPanel header = new JPanel(new java.awt.GridBagLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(10, 12, 6, 12));
        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.insets = new Insets(3, 4, 3, 4);
        g.anchor = java.awt.GridBagConstraints.WEST;
        g.gridx = 0; g.gridy = 0; header.add(labelKecil("Nama Pasien"), g);
        g.gridx = 1; g.gridy = 0; TNama.setPreferredSize(new Dimension(300, 25)); header.add(TNama, g);
        g.gridx = 2; g.gridy = 0; header.add(labelKecil("No. RM"), g);
        g.gridx = 3; g.gridy = 0; TNoRM.setPreferredSize(new Dimension(120, 25)); header.add(TNoRM, g);
        add(header, BorderLayout.NORTH);

        tbBaris.setModel(tabMode);
        tbBaris.setRowHeight(24);
        tbBaris.getColumnModel().getColumn(0).setMinWidth(0);
        tbBaris.getColumnModel().getColumn(0).setMaxWidth(0);
        tbBaris.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { muatBarisTerpilih(); }
        });
        JScrollPane scrollTabel = new JScrollPane(tbBaris);
        scrollTabel.setBorder(BorderFactory.createTitledBorder("Daftar Baris Tersimpan (klik utk ubah)"));

        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createTitledBorder("Isi / Ubah Baris"));
        form.setLayout(new javax.swing.BoxLayout(form, javax.swing.BoxLayout.Y_AXIS));

        JPanel barisTglPetugas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        barisTglPetugas.setBackground(Color.WHITE);
        dtpTglJam.setDate(new Date());
        barisTglPetugas.add(labelKecil("Tgl / Jam"));
        barisTglPetugas.add(dtpTglJam);
        barisTglPetugas.add(javax.swing.Box.createHorizontalStrut(24));
        barisTglPetugas.add(labelKecil("Paraf DPJP/Petugas (otomatis dari login)"));
        lblFotoPetugas.setPreferredSize(new Dimension(70, 30));
        barisTglPetugas.add(lblFotoPetugas);
        lblPetugas.setFont(new Font("Tahoma", Font.BOLD, 11));
        barisTglPetugas.add(lblPetugas);
        form.add(barisTglPetugas);

        form.add(bungkusArea("Diagnosa (ICD X)", taDiagnosa));
        form.add(bungkusArea("Uraian Klinis Penting", taKlinis));
        form.add(bungkusArea("Penunjang", taPenunjang));
        form.add(bungkusArea("Riwayat Rawat Inap/Operasi", taRiwayatInap));
        form.add(bungkusArea("Obat-Obatan/Rencana Tindak Lanjut", taObatRtl));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTabel, new JScrollPane(form));
        split.setResizeWeight(0.42);
        add(split, BorderLayout.CENTER);

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        tombol.setBackground(Color.WHITE);
        BtnBaru.setText("Baris Baru");
        BtnSimpan.setText("Simpan");
        BtnHapus.setText("Hapus");
        BtnCetak.setText("Cetak");
        BtnTutup.setText("Tutup");
        BtnBaru.addActionListener(e -> kosongkanForm());
        BtnSimpan.addActionListener(e -> simpan());
        BtnHapus.addActionListener(e -> hapus());
        BtnCetak.addActionListener(e -> cetak());
        BtnTutup.addActionListener(e -> dispose());
        for (widget.Button b : new widget.Button[]{BtnBaru, BtnSimpan, BtnHapus, BtnCetak, BtnTutup}) {
            b.setPreferredSize(new Dimension(110, 28));
            tombol.add(b);
        }
        add(tombol, BorderLayout.SOUTH);
    }

    private void setPetugasSekarang() {
        String kode = akses.getkode();
        String nama = Sequel.cariIsi("select nama from petugas where nip=?", kode);
        if (nama == null || nama.trim().isEmpty()) {
            nama = Sequel.cariIsi("select nama from pegawai where nik=?", kode);
        }
        lblPetugas.setText((nama == null || nama.trim().isEmpty()) ? kode : nama.trim());
        ImageIcon ic = ambilFotoTtd(kode);
        lblFotoPetugas.setIcon(ic);
    }

    private void muatDaftar() {
        tabMode.setRowCount(0);
        if (noRM.isEmpty()) { return; }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select id,concat(date_format(tanggal,'%d-%m-%Y'),' ',left(jam,5)) tgljam,diagnosa,uraian_klinis,nama_petugas "
                + "from profil_ringkas_medis_ralan where no_rkm_medis=? order by tanggal,jam")) {
            ps.setString(1, noRM);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tabMode.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("tgljam"), nvl(rs.getString("diagnosa")),
                        ringkas(nvl(rs.getString("uraian_klinis"))), nvl(rs.getString("nama_petugas"))
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat daftar profil ringkas medis ralan : " + e);
        }
    }

    private void muatBarisTerpilih() {
        int row = tbBaris.getSelectedRow();
        if (row < 0) { return; }
        int id = (Integer) tabMode.getValueAt(row, 0);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select * from profil_ringkas_medis_ralan where id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    idTerpilih = id;
                    Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            .parse(rs.getString("tanggal") + " " + rs.getString("jam"));
                    dtpTglJam.setDate(d);
                    taDiagnosa.setText(nvl(rs.getString("diagnosa")));
                    taKlinis.setText(nvl(rs.getString("uraian_klinis")));
                    taPenunjang.setText(nvl(rs.getString("penunjang")));
                    taRiwayatInap.setText(nvl(rs.getString("riwayat_inap_operasi")));
                    taObatRtl.setText(nvl(rs.getString("obat_rtl")));
                    lblPetugas.setText(nvl(rs.getString("nama_petugas")));
                    lblFotoPetugas.setIcon(ambilFotoTtd(rs.getString("kd_petugas")));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat baris profil ringkas medis ralan : " + e);
        }
    }

    private void kosongkanForm() {
        idTerpilih = null;
        dtpTglJam.setDate(new Date());
        taDiagnosa.setText("");
        taKlinis.setText("");
        taPenunjang.setText("");
        taRiwayatInap.setText("");
        taObatRtl.setText("");
        tbBaris.clearSelection();
        setPetugasSekarang();
        autoTarikDiagnosaPlan();
    }

    /** Baris baru: Diagnosa ditarik otomatis dari tab Diagnosa (kalau sudah diisi utk kunjungan
     *  ini), dan Obat-Obatan/RTL ditarik dari Plan SOAP terakhir (pemeriksaan_ralan.rtl) kunjungan
     *  ini -- dua-duanya tetap boleh diedit manual sebelum disimpan. Kalau no_rawat tdk diketahui
     *  (dialog dibuka tanpa konteks kunjungan) atau datanya memang belum diisi, field dibiarkan
     *  kosong spt biasa. */
    private void autoTarikDiagnosaPlan() {
        if (noRawat.isEmpty()) { return; }
        try {
            String diagnosa = Sequel.cariIsi(
                    "select group_concat(distinct concat(dp.kd_penyakit,' ',p.nm_penyakit) "
                    + "order by dp.prioritas separator '\n') "
                    + "from diagnosa_pasien dp inner join penyakit p on p.kd_penyakit=dp.kd_penyakit "
                    + "where dp.no_rawat=? and dp.status='Ralan'", noRawat);
            if (diagnosa != null && !diagnosa.trim().isEmpty()) {
                taDiagnosa.setText(diagnosa.trim());
            }
            String plan = Sequel.cariIsi(
                    "select rtl from pemeriksaan_ralan where no_rawat=? and ifnull(rtl,'')<>'' "
                    + "order by tgl_perawatan desc,jam_rawat desc limit 1", noRawat);
            if (plan != null && !plan.trim().isEmpty()) {
                taObatRtl.setText(plan.trim());
            }
        } catch (Exception e) {
            System.out.println("Notif tarik otomatis diagnosa/plan profil ringkas medis ralan : " + e);
        }
    }

    private void simpan() {
        if (noRM.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        if (taDiagnosa.getText().trim().isEmpty() && taKlinis.getText().trim().isEmpty()
                && taPenunjang.getText().trim().isEmpty() && taRiwayatInap.getText().trim().isEmpty()
                && taObatRtl.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi minimal salah satu kolom sebelum menyimpan.");
            return;
        }
        Date d = dtpTglJam.getDate();
        if (d == null) {
            JOptionPane.showMessageDialog(this, "Tanggal/Jam belum diisi.");
            return;
        }
        String tanggal = new SimpleDateFormat("yyyy-MM-dd").format(d);
        String jam = new SimpleDateFormat("HH:mm:ss").format(d);
        String kode = akses.getkode();
        String namaPetugas = lblPetugas.getText();
        try {
            if (idTerpilih == null) {
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "insert into profil_ringkas_medis_ralan(no_rkm_medis,tanggal,jam,diagnosa,uraian_klinis,"
                        + "penunjang,riwayat_inap_operasi,obat_rtl,kd_petugas,nama_petugas,created_by,created_at) "
                        + "values(?,?,?,?,?,?,?,?,?,?,?,now())")) {
                    ps.setString(1, noRM);
                    ps.setString(2, tanggal);
                    ps.setString(3, jam);
                    ps.setString(4, taDiagnosa.getText().trim());
                    ps.setString(5, taKlinis.getText().trim());
                    ps.setString(6, taPenunjang.getText().trim());
                    ps.setString(7, taRiwayatInap.getText().trim());
                    ps.setString(8, taObatRtl.getText().trim());
                    ps.setString(9, kode);
                    ps.setString(10, namaPetugas);
                    ps.setString(11, kode);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "update profil_ringkas_medis_ralan set tanggal=?,jam=?,diagnosa=?,uraian_klinis=?,"
                        + "penunjang=?,riwayat_inap_operasi=?,obat_rtl=?,updated_by=?,updated_at=now() where id=?")) {
                    ps.setString(1, tanggal);
                    ps.setString(2, jam);
                    ps.setString(3, taDiagnosa.getText().trim());
                    ps.setString(4, taKlinis.getText().trim());
                    ps.setString(5, taPenunjang.getText().trim());
                    ps.setString(6, taRiwayatInap.getText().trim());
                    ps.setString(7, taObatRtl.getText().trim());
                    ps.setString(8, kode);
                    ps.setInt(9, idTerpilih);
                    ps.executeUpdate();
                }
            }
            muatDaftar();
            kosongkanForm();
            widget.Toast.sukses(this, "Baris Profil Ringkas Medis tersimpan.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan : " + e.getMessage());
            System.out.println("Notif simpan profil ringkas medis ralan : " + e);
        }
    }

    private void hapus() {
        if (idTerpilih == null) {
            JOptionPane.showMessageDialog(this, "Pilih dulu baris yang mau dihapus dari daftar.");
            return;
        }
        int jawab = JOptionPane.showConfirmDialog(this, "Hapus baris ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (jawab != JOptionPane.YES_OPTION) { return; }
        try (PreparedStatement ps = koneksi.prepareStatement("delete from profil_ringkas_medis_ralan where id=?")) {
            ps.setInt(1, idTerpilih);
            ps.executeUpdate();
            muatDaftar();
            kosongkanForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus : " + e.getMessage());
        }
    }

    private void cetak() {
        if (noRM.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            param.put("nama", TNama.getText());
            param.put("norm", noRM);
            String tgllahir = "", jk = "";
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "select date_format(tgl_lahir,'%d-%m-%Y') tgl_lahir,jk from pasien where no_rkm_medis=?")) {
                ps.setString(1, noRM);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) { tgllahir = nvl(rs.getString(1)); jk = nvl(rs.getString(2)); }
                }
            }
            param.put("tgllahir", tgllahir);
            param.put("jk", jk);
            param.put("url_penggajian", "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/"
                    + koneksiDB.HYBRIDWEB() + "/penggajian/");

            java.io.File jrxml = new java.io.File("./report/rptProfilRingkasMedisRalan.jrxml");
            java.io.File jasper = new java.io.File("./report/rptProfilRingkasMedisRalan.jasper");
            if (!jrxml.exists()) { throw new Exception("Template rptProfilRingkasMedisRalan.jrxml tidak ditemukan."); }
            if (!jasper.exists() || jrxml.lastModified() > jasper.lastModified()) {
                System.setProperty("net.sf.jasperreports.compiler.java",
                        "net.sf.jasperreports.engine.design.JRJavacCompiler");
                DefaultJasperReportsContext.getInstance().setProperty(
                        "net.sf.jasperreports.compiler.java",
                        "net.sf.jasperreports.engine.design.JRJavacCompiler");
                JasperCompileManager.compileReportToFile(jrxml.getPath(), jasper.getPath());
            }
            Valid.MyReport("rptProfilRingkasMedisRalan.jasper", "report",
                    "::[ Profil Ringkas Medis Rawat Jalan ]::", param);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak : " + e.getMessage());
            System.out.println("Notif cetak profil ringkas medis ralan : " + e);
        }
    }

    private ImageIcon ambilFotoTtd(String nip) {
        if (nip == null || nip.trim().isEmpty()) { return null; }
        String key = nip.trim();
        if (cacheFotoTtd.containsKey(key)) { return cacheFotoTtd.get(key); }
        ImageIcon ic = null;
        try {
            String photo = bersihkanPathFotoTtd(Sequel.cariIsi("select photo from pegawai where nik=?", key));
            if (!photo.isEmpty()) {
                String urlPenggajian = "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/"
                        + koneksiDB.HYBRIDWEB() + "/penggajian/";
                Image gambar = CetakCPPT.ambilGambarServer(urlPenggajian + photo);
                if (gambar != null) {
                    ic = new ImageIcon(gambar.getScaledInstance(-1, 28, Image.SCALE_SMOOTH));
                }
            }
        } catch (Exception ignore) { }
        cacheFotoTtd.put(key, ic);
        return ic;
    }

    private static String bersihkanPathFotoTtd(String photo) {
        if (photo == null) { return ""; }
        String p = photo.trim();
        if (p.equals("") || p.equals("-") || p.equals("pages/pegawai/photo/")) { return ""; }
        return p.replace("\\", "/");
    }

    private static String nvl(String s) { return s == null ? "" : s; }

    private static String ringkas(String s) {
        if (s == null) { return ""; }
        String satu = s.replaceAll("\\s+", " ").trim();
        return satu.length() > 60 ? satu.substring(0, 60) + "..." : satu;
    }

    private static widget.TextBox ro() {
        widget.TextBox t = new widget.TextBox();
        t.setEditable(false);
        t.setBackground(new Color(245, 248, 249));
        return t;
    }

    private static widget.TextArea ta() {
        widget.TextArea t = new widget.TextArea();
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        t.setRows(2);
        return t;
    }

    private static widget.Tanggal dt() {
        widget.Tanggal d = new widget.Tanggal();
        d.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        return d;
    }

    private JLabel labelKecil(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font("Tahoma", Font.PLAIN, 11));
        l.setForeground(new Color(49, 64, 75));
        return l;
    }

    private JPanel bungkusArea(String label, widget.TextArea area) {
        JPanel p = new JPanel(new BorderLayout(4, 2));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(4, 8, 4, 8));
        p.add(labelKecil(label), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(600, 50));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(190, 202, 210)));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }
}
