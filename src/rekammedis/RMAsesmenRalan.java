package rekammedis;

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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Panel "Penilaian Awal Rawat Jalan" (Asesmen Rawat Jalan) yang di-embed sebagai
 * tab di DlgRawatJalan. Mengacu tata letak PDF Asesmen Rawat Jalan TrustMedis:
 * identitas + riwayat (auto tarik) + asesmen (input) + TTD pelaksana.
 *
 * Data disimpan REPLACE INTO tabel asesmen_ralan (1 baris per no_rawat, semua
 * kolom TEXT untuk menghindari #1118 Row size too large). Cetak digabung dengan
 * ringkasan riwayat kunjungan lewat CetakAsesmen (kop RS + QR TTD).
 */
public final class RMAsesmenRalan extends JPanel {

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();

    private String noRawat = "";

    // Identitas (read-only, auto tarik)
    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TNIK = ro();
    private final widget.TextBox TJK = ro();
    private final widget.TextBox TTglLahir = ro();
    private final widget.TextBox TAlamat = ro();
    private final widget.TextBox TUnit = ro();
    private final widget.TextBox TKelas = ro();
    private final widget.TextBox TCaraBayar = ro();

    // Riwayat (read-only, auto tarik)
    private final widget.TextArea taRiwayatAlergi = ta();
    private final widget.TextArea taRiwayatBedah = ta();

    // Asesmen (input)
    private final widget.Tanggal dtpTanggal = dt();
    private final Grup grpRujukan = new Grup("Puskesmas", "RS", "Dokter");
    private final Grup grpDatang = new Grup("Datang sendiri", "Diantar", "Lainnya");
    private final Grup grpInformasi = new Grup("Pasien", "Keluarga", "Teman", "Lainnya");
    private final widget.TextArea taPenyakitSekarang = ta();
    private final widget.TextArea taPenyakitDahulu = ta();

    private final widget.TextBox tTD = tf();
    private final widget.TextBox tSuhu = tf();
    private final widget.TextBox tNadi = tf();
    private final widget.TextBox tRR = tf();
    private final widget.ComboBox cmbNyeri = cmb("Tidak nyeri", "Nyeri");
    private final widget.ComboBox cmbSkalaNyeri = cmb("0 - Tidak nyeri", "1-3 Nyeri ringan",
            "4-6 Nyeri sedang", "7-10 Nyeri berat");

    private final widget.TextBox tBB = tf();
    private final widget.TextBox tTB = tf();
    private final widget.TextBox tIMT = tf();
    private final widget.TextBox tLingkarKepala = tf();

    private final widget.TextBox tAlatBantu = tf();
    private final widget.TextBox tProthesa = tf();
    private final widget.TextBox tCacatTubuh = tf();
    private final widget.TextBox tAdl = tf();
    private final widget.ComboBox cmbMandiri = cmb("Mandiri", "Dibantu");

    private final widget.ComboBox cmbNikah = cmb("Belum menikah", "Menikah",
            "Duda/Janda (meninggal)", "Duda/Janda (bercerai)");
    private final widget.ComboBox cmbSaudara = cmb("Tidak ada", "Ada");
    private final widget.TextBox tJumlahSaudara = tf();
    private final widget.ComboBox cmbNegara = cmb("WNI", "WNA");
    private final widget.TextBox tWnaAsal = tf();
    private final widget.ComboBox cmbPekerjaan = cmb("PNS", "Swasta", "TNI/POLRI", "Tidak bekerja", "Lainnya");
    private final widget.ComboBox cmbTinggal = cmb("Suami", "Anak", "Orang tua", "Sendiri", "Lainnya");
    private final widget.TextBox tNamaKeluarga = tf();
    private final widget.TextBox tTelepon = tf();
    private final widget.ComboBox cmbAgama = cmb("Islam", "Kristen", "Katolik", "Hindu", "Budha", "Konghucu");

    // TTD
    private final widget.Tanggal dtpTtd = dt();
    private final widget.TextBox KdPetugas = ro();
    private final widget.TextBox NmPetugas = ro();

    private final widget.Button BtnBaru = new widget.Button();
    private final widget.Button BtnSimpan = new widget.Button();
    private final widget.Button BtnHapus = new widget.Button();
    private final widget.Button BtnCetak = new widget.Button();

    public RMAsesmenRalan() {
        pastikanTabel();
        initComponents();
        isCek();
    }

    private void initComponents() {
        setLayout(new BorderLayout(6, 6));
        setBackground(Color.WHITE);

        // ---- toolbar ----
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        toolbar.setBackground(new Color(238, 243, 238));
        siapkanTombol(BtnBaru, "Baru");
        siapkanTombol(BtnSimpan, "Simpan");
        siapkanTombol(BtnHapus, "Hapus");
        siapkanTombol(BtnCetak, "Cetak");
        BtnBaru.addActionListener(e -> baru());
        BtnSimpan.addActionListener(e -> simpan());
        BtnHapus.addActionListener(e -> hapus());
        BtnCetak.addActionListener(e -> cetak());
        toolbar.add(BtnBaru);
        toolbar.add(BtnSimpan);
        toolbar.add(BtnHapus);
        toolbar.add(BtnCetak);
        add(toolbar, BorderLayout.NORTH);

        // ---- form ----
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        int row = 0;

        row = judul(form, row, "Identitas Pasien");
        row = baris2(form, row, "No. Rawat", TNoRw, "No. RM", TNoRM);
        row = baris2(form, row, "Nama Pasien", TPasien, "NIK", TNIK);
        row = baris2(form, row, "Jenis Kelamin", TJK, "Tanggal Lahir", TTglLahir);
        row = baris1(form, row, "Alamat", TAlamat);
        row = baris2(form, row, "Poli / Unit", TUnit, "Kelas", TKelas);
        row = baris1(form, row, "Cara Bayar", TCaraBayar);

        row = judul(form, row, "Riwayat (otomatis; bisa diisi manual bila kosong)");
        row = area(form, row, "Riwayat Alergi", taRiwayatAlergi);
        row = area(form, row, "Riwayat Pembedahan / Rawat Inap", taRiwayatBedah);
        taRiwayatAlergi.setToolTipText("Terisi otomatis dari riwayat pasien; boleh diketik/diubah manual.");
        taRiwayatBedah.setToolTipText("Terisi otomatis dari riwayat rawat inap; boleh diketik/diubah manual.");

        row = judul(form, row, "Asesmen Rawat Jalan");
        row = baris1(form, row, "Tanggal / Pukul", dtpTanggal);
        row = grup(form, row, "Rujukan", grpRujukan.panel);
        row = grup(form, row, "Cara Datang", grpDatang.panel);
        row = grup(form, row, "Informasi Dari", grpInformasi.panel);
        row = area(form, row, "Penyakit Sekarang", taPenyakitSekarang);
        row = area(form, row, "Penyakit Dahulu", taPenyakitDahulu);

        row = judul(form, row, "Tanda Vital");
        row = baris2(form, row, "TD (mmHg)", tTD, "Suhu (C)", tSuhu);
        row = baris2(form, row, "Nadi (x/mnt)", tNadi, "RR (x/mnt)", tRR);
        row = baris2(form, row, "Nyeri", cmbNyeri, "Skala Nyeri", cmbSkalaNyeri);

        row = judul(form, row, "Antropometri");
        row = baris2(form, row, "BB (kg)", tBB, "TB (cm)", tTB);
        row = baris2(form, row, "IMT (kg/m2)", tIMT, "Lingkar Kepala (cm)", tLingkarKepala);
        tBB.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { hitungIMT(); }
        });
        tTB.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { hitungIMT(); }
        });

        row = judul(form, row, "Fungsional");
        row = baris2(form, row, "Alat Bantu", tAlatBantu, "Prothesa", tProthesa);
        row = baris2(form, row, "Cacat Tubuh", tCacatTubuh, "ADL", tAdl);
        row = baris1(form, row, "Kemandirian", cmbMandiri);

        row = judul(form, row, "Sosial / Ekonomi / Spiritual");
        row = baris2(form, row, "Pernikahan", cmbNikah, "Saudara", cmbSaudara);
        row = baris2(form, row, "Jumlah Saudara (orang)", tJumlahSaudara, "Negara", cmbNegara);
        row = baris2(form, row, "WNA Asal", tWnaAsal, "Pekerjaan", cmbPekerjaan);
        row = baris2(form, row, "Tinggal Bersama", cmbTinggal, "Agama", cmbAgama);
        row = baris2(form, row, "Nama Keluarga", tNamaKeluarga, "No. Telpon", tTelepon);

        row = judul(form, row, "Pelaksana Asesmen");
        row = baris2(form, row, "Tanggal TTD", dtpTtd, "NIP", KdPetugas);
        row = baris1(form, row, "Nama Pelaksana", NmPetugas);

        JScrollPane sc = new JScrollPane(form);
        sc.getVerticalScrollBar().setUnitIncrement(16);
        sc.setBorder(BorderFactory.createEmptyBorder());
        add(sc, BorderLayout.CENTER);
    }

    private void siapkanTombol(widget.Button b, String teks) {
        b.setText(teks);
        b.setFont(new Font("Tahoma", Font.BOLD, 11));
        b.setPreferredSize(new Dimension(95, 27));
    }

    // ====================== Entry point ======================
    public void isCek() {
        boolean bisa = akses.getpenilaian_awal_keperawatan_ralan();
        BtnSimpan.setEnabled(bisa);
        BtnHapus.setEnabled(bisa);
        KdPetugas.setText(akses.getkode());
        NmPetugas.setText(Sequel.cariIsi("select nama from petugas where nip=?", akses.getkode()));
    }

    public void setKonteks(String norwt) {
        this.noRawat = norwt == null ? "" : norwt.trim();
        baru();
        TNoRw.setText(noRawat);
        if (!noRawat.equals("")) {
            tarikIdentitas();
            muat();                    // ambil nilai tersimpan (termasuk riwayat freetext bila ada)
            lengkapiRiwayatOtomatis(); // isi otomatis hanya bila area riwayat masih kosong
        }
    }

    private void baru() {
        for (widget.TextBox t : new widget.TextBox[]{tTD, tSuhu, tNadi, tRR, tBB, tTB, tIMT, tLingkarKepala,
            tAlatBantu, tProthesa, tCacatTubuh, tAdl, tJumlahSaudara, tWnaAsal, tNamaKeluarga, tTelepon}) {
            t.setText("");
        }
        for (widget.TextArea a : new widget.TextArea[]{taPenyakitSekarang, taPenyakitDahulu, taRiwayatAlergi, taRiwayatBedah}) {
            a.setText("");
        }
        for (Grup g : new Grup[]{grpRujukan, grpDatang, grpInformasi}) {
            g.clear();
        }
        for (widget.ComboBox c : new widget.ComboBox[]{cmbNyeri, cmbSkalaNyeri, cmbMandiri, cmbNikah,
            cmbSaudara, cmbNegara, cmbPekerjaan, cmbTinggal, cmbAgama}) {
            c.setSelectedIndex(0);
        }
        dtpTanggal.setDate(new Date());
        dtpTtd.setDate(new Date());
        KdPetugas.setText(akses.getkode());
        NmPetugas.setText(Sequel.cariIsi("select nama from petugas where nip=?", akses.getkode()));
    }

    private void tarikIdentitas() {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select p.nm_pasien,p.no_rkm_medis,p.no_ktp,p.jk,p.tgl_lahir,p.alamat,p.no_tlp,"
                + "ifnull(p.agama,'') as agama,ifnull(p.stts_nikah,'') as stts_nikah,ifnull(p.pekerjaan,'') as pekerjaan,"
                + "ifnull(p.namakeluarga,'') as namakeluarga,ifnull(poliklinik.nm_poli,'') as unit,"
                + "ifnull(pj.png_jawab,'') as carabayar "
                + "from reg_periksa rp inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis "
                + "left join poliklinik on rp.kd_poli=poliklinik.kd_poli "
                + "left join penjab pj on rp.kd_pj=pj.kd_pj where rp.no_rawat=?")) {
            ps.setString(1, noRawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    TNIK.setText(rs.getString("no_ktp"));
                    TJK.setText("L".equalsIgnoreCase(rs.getString("jk")) ? "Laki-Laki" : "Perempuan");
                    TTglLahir.setText(rs.getString("tgl_lahir"));
                    TAlamat.setText(rs.getString("alamat"));
                    TUnit.setText(rs.getString("unit"));
                    TCaraBayar.setText(rs.getString("carabayar"));
                    tTelepon.setText(rs.getString("no_tlp"));
                    tNamaKeluarga.setText(rs.getString("namakeluarga"));
                    setCombo(cmbAgama, rs.getString("agama"));
                    setNikahFromMaster(rs.getString("stts_nikah"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif identitas asesmen ralan : " + e);
        }
        TKelas.setText(Sequel.cariIsi("select ifnull(kamar.kelas,'') from kamar_inap "
                + "inner join kamar on kamar.kd_kamar=kamar_inap.kd_kamar where kamar_inap.no_rawat=? "
                + "order by kamar_inap.tgl_masuk desc limit 1", noRawat));
    }

    /**
     * Isi area riwayat dari data pasien HANYA bila area masih kosong (mis. belum
     * ada nilai tersimpan). Bila data historis juga tidak ada, area dibiarkan
     * kosong agar petugas bisa mengetik manual (freetext).
     */
    private void lengkapiRiwayatOtomatis() {
        if (taRiwayatAlergi.getText().trim().isEmpty()) {
            StringBuilder alergi = new StringBuilder();
            tambahRiwayat(alergi, "select group_concat(distinct nullif(trim(pr.alergi),'') separator ', ') "
                    + "from pemeriksaan_ralan pr inner join reg_periksa rp on pr.no_rawat=rp.no_rawat "
                    + "where rp.no_rkm_medis=? and trim(ifnull(pr.alergi,''))<>''");
            tambahRiwayat(alergi, "select group_concat(distinct nullif(trim(pr.alergi),'') separator ', ') "
                    + "from pemeriksaan_ranap pr inner join reg_periksa rp on pr.no_rawat=rp.no_rawat "
                    + "where rp.no_rkm_medis=? and trim(ifnull(pr.alergi,''))<>''");
            taRiwayatAlergi.setText(alergi.toString());
        }
        if (taRiwayatBedah.getText().trim().isEmpty()) {
            StringBuilder bedah = new StringBuilder();
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "select concat(date_format(ki.tgl_masuk,'%d-%m-%Y'),' - ',ifnull(bangsal.nm_bangsal,''),"
                    + "' (',ifnull(kamar.kelas,''),')') as ket "
                    + "from kamar_inap ki inner join reg_periksa rp on ki.no_rawat=rp.no_rawat "
                    + "inner join kamar on kamar.kd_kamar=ki.kd_kamar "
                    + "inner join bangsal on bangsal.kd_bangsal=kamar.kd_bangsal "
                    + "where rp.no_rkm_medis=? order by ki.tgl_masuk desc")) {
                ps.setString(1, TNoRM.getText());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (bedah.length() > 0) { bedah.append("\n"); }
                        bedah.append("- ").append(rs.getString("ket"));
                    }
                }
            } catch (Exception e) {
                System.out.println("Notif riwayat ranap asesmen ralan : " + e);
            }
            taRiwayatBedah.setText(bedah.toString());
        }
        taRiwayatAlergi.setCaretPosition(0);
        taRiwayatBedah.setCaretPosition(0);
    }

    private void tambahRiwayat(StringBuilder sb, String sql) {
        String v = Sequel.cariIsi(sql, TNoRM.getText());
        if (v != null && !v.trim().equals("")) {
            if (sb.length() > 0) { sb.append(", "); }
            sb.append(v.trim());
        }
    }

    private void hitungIMT() {
        try {
            double bb = Double.parseDouble(tBB.getText().trim().replace(",", "."));
            double tb = Double.parseDouble(tTB.getText().trim().replace(",", ".")) / 100.0;
            if (bb > 0 && tb > 0) {
                tIMT.setText(new java.text.DecimalFormat("0.0").format(bb / (tb * tb)));
            }
        } catch (Exception e) {
            // biarkan manual bila input belum lengkap/valid
        }
    }

    // ====================== Kolom DB ======================
    private static final String[] KOLOM = {
        "no_rawat", "tanggal", "jam", "rujukan", "cara_datang", "informasi_dari",
        "penyakit_sekarang", "penyakit_dahulu", "td", "suhu", "nadi", "rr", "nyeri", "skala_nyeri",
        "bb", "tb", "imt", "lingkar_kepala", "alat_bantu", "prothesa", "cacat_tubuh", "adl", "mandiri",
        "status_nikah", "saudara", "jumlah_saudara", "negara", "wna_asal", "pekerjaan", "tinggal_bersama",
        "nama_keluarga", "telepon", "agama", "riwayat_alergi", "riwayat_bedah", "tgl_ttd", "jam_ttd", "nik"
    };

    private void simpan() {
        if (TNoRw.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        if (KdPetugas.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Pelaksana asesmen belum terisi.");
            return;
        }
        hitungIMT();
        String tgl = Valid.SetTgl(dtpTanggal.getSelectedItem() + "");
        String jam = dtpTanggal.getSelectedItem().toString().substring(11, 19);
        String tglTtd = Valid.SetTgl(dtpTtd.getSelectedItem() + "");
        String jamTtd = dtpTtd.getSelectedItem().toString().substring(11, 19);
        String[] nilai = {
            TNoRw.getText(), tgl, jam, grpRujukan.get(), grpDatang.get(), grpInformasi.get(),
            taPenyakitSekarang.getText(), taPenyakitDahulu.getText(), tTD.getText(), tSuhu.getText(), tNadi.getText(), tRR.getText(),
            s(cmbNyeri), s(cmbSkalaNyeri), tBB.getText(), tTB.getText(), tIMT.getText(), tLingkarKepala.getText(),
            tAlatBantu.getText(), tProthesa.getText(), tCacatTubuh.getText(), tAdl.getText(), s(cmbMandiri),
            s(cmbNikah), s(cmbSaudara), tJumlahSaudara.getText(), s(cmbNegara), tWnaAsal.getText(), s(cmbPekerjaan), s(cmbTinggal),
            tNamaKeluarga.getText(), tTelepon.getText(), s(cmbAgama),
            taRiwayatAlergi.getText(), taRiwayatBedah.getText(), tglTtd, jamTtd, KdPetugas.getText()
        };
        if (KOLOM.length != nilai.length) {
            JOptionPane.showMessageDialog(this, "Kesalahan internal: jumlah kolom (" + KOLOM.length
                    + ") != jumlah nilai (" + nilai.length + ").");
            return;
        }
        StringBuilder cols = new StringBuilder();
        StringBuilder qm = new StringBuilder();
        for (String k : KOLOM) {
            if (cols.length() > 0) { cols.append(","); qm.append(","); }
            cols.append(k);
            qm.append("?");
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "replace into asesmen_ralan (" + cols + ") values (" + qm + ")")) {
            for (int i = 0; i < nilai.length; i++) {
                ps.setString(i + 1, nilai[i]);
            }
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Asesmen rawat jalan tersimpan.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan.\n" + e.getMessage());
        }
    }

    private void muat() {
        try (PreparedStatement ps = koneksi.prepareStatement("select * from asesmen_ralan where no_rawat=?")) {
            ps.setString(1, TNoRw.getText());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    setTgl(dtpTanggal, g(rs, "tanggal"), g(rs, "jam"));
                    grpRujukan.set(g(rs, "rujukan"));
                    grpDatang.set(g(rs, "cara_datang"));
                    grpInformasi.set(g(rs, "informasi_dari"));
                    taPenyakitSekarang.setText(g(rs, "penyakit_sekarang"));
                    taPenyakitDahulu.setText(g(rs, "penyakit_dahulu"));
                    tTD.setText(g(rs, "td")); tSuhu.setText(g(rs, "suhu")); tNadi.setText(g(rs, "nadi")); tRR.setText(g(rs, "rr"));
                    setCombo(cmbNyeri, g(rs, "nyeri")); setCombo(cmbSkalaNyeri, g(rs, "skala_nyeri"));
                    tBB.setText(g(rs, "bb")); tTB.setText(g(rs, "tb")); tIMT.setText(g(rs, "imt")); tLingkarKepala.setText(g(rs, "lingkar_kepala"));
                    tAlatBantu.setText(g(rs, "alat_bantu")); tProthesa.setText(g(rs, "prothesa"));
                    tCacatTubuh.setText(g(rs, "cacat_tubuh")); tAdl.setText(g(rs, "adl")); setCombo(cmbMandiri, g(rs, "mandiri"));
                    setCombo(cmbNikah, g(rs, "status_nikah")); setCombo(cmbSaudara, g(rs, "saudara"));
                    tJumlahSaudara.setText(g(rs, "jumlah_saudara")); setCombo(cmbNegara, g(rs, "negara")); tWnaAsal.setText(g(rs, "wna_asal"));
                    setCombo(cmbPekerjaan, g(rs, "pekerjaan")); setCombo(cmbTinggal, g(rs, "tinggal_bersama"));
                    tNamaKeluarga.setText(g(rs, "nama_keluarga")); tTelepon.setText(g(rs, "telepon")); setCombo(cmbAgama, g(rs, "agama"));
                    taRiwayatAlergi.setText(g(rs, "riwayat_alergi")); taRiwayatBedah.setText(g(rs, "riwayat_bedah"));
                    setTgl(dtpTtd, g(rs, "tgl_ttd"), g(rs, "jam_ttd"));
                    if (!g(rs, "nik").trim().equals("")) {
                        KdPetugas.setText(g(rs, "nik"));
                        NmPetugas.setText(Sequel.cariIsi("select nama from petugas where nip=?", g(rs, "nik")));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat asesmen ralan : " + e);
        }
    }

    private void hapus() {
        if (TNoRw.getText().trim().equals("")) { return; }
        if (JOptionPane.showConfirmDialog(this, "Hapus asesmen rawat jalan untuk No.Rawat " + TNoRw.getText() + " ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement("delete from asesmen_ralan where no_rawat=?")) {
            ps.setString(1, TNoRw.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data dihapus.");
            baru();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus.\n" + e.getMessage());
        }
    }

    // ====================== Cetak gabungan ======================
    private void cetak() {
        if (TNoRw.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        List<Map<String, ?>> rows = CetakAsesmen.mulai();
        CetakAsesmen.h(rows, "Asesmen Rawat Jalan");
        CetakAsesmen.r(rows, "Tanggal / Pukul", dtpTanggal.getSelectedItem() + "");
        CetakAsesmen.r(rows, "Rujukan", grpRujukan);
        CetakAsesmen.r(rows, "Cara Datang", grpDatang);
        CetakAsesmen.r(rows, "Informasi Dari", grpInformasi);
        CetakAsesmen.r(rows, "Penyakit Sekarang", taPenyakitSekarang.getText());
        CetakAsesmen.r(rows, "Penyakit Dahulu", taPenyakitDahulu.getText());

        CetakAsesmen.h(rows, "Tanda Vital");
        CetakAsesmen.r2(rows, "TD", tTD.getText() + " mmHg", "Suhu", tSuhu.getText() + " C");
        CetakAsesmen.r2(rows, "Nadi", tNadi.getText() + " x/mnt", "RR", tRR.getText() + " x/mnt");
        CetakAsesmen.r(rows, "Nyeri / Skala", s(cmbNyeri) + " (" + s(cmbSkalaNyeri) + ")");

        CetakAsesmen.h(rows, "Antropometri");
        CetakAsesmen.r2(rows, "BB", tBB.getText() + " kg", "TB", tTB.getText() + " cm");
        CetakAsesmen.r2(rows, "IMT", tIMT.getText() + " kg/m2", "Lingkar Kepala", tLingkarKepala.getText() + " cm");

        CetakAsesmen.h(rows, "Fungsional");
        CetakAsesmen.r2(rows, "Alat Bantu", tAlatBantu.getText(), "Prothesa", tProthesa.getText());
        CetakAsesmen.r2(rows, "Cacat Tubuh", tCacatTubuh.getText(), "ADL", tAdl.getText());
        CetakAsesmen.r(rows, "Kemandirian", s(cmbMandiri));

        CetakAsesmen.h(rows, "Sosial / Ekonomi / Spiritual");
        CetakAsesmen.r(rows, "Pernikahan", s(cmbNikah));
        CetakAsesmen.r(rows, "Saudara", s(cmbSaudara) + (tJumlahSaudara.getText().trim().isEmpty() ? "" : " (" + tJumlahSaudara.getText() + " orang)"));
        CetakAsesmen.r(rows, "Negara", s(cmbNegara) + (tWnaAsal.getText().trim().isEmpty() ? "" : " - " + tWnaAsal.getText()));
        CetakAsesmen.r(rows, "Pekerjaan", s(cmbPekerjaan));
        CetakAsesmen.r(rows, "Tinggal Bersama", s(cmbTinggal));
        CetakAsesmen.r2(rows, "Nama Keluarga", tNamaKeluarga.getText(), "Telpon", tTelepon.getText());
        CetakAsesmen.r(rows, "Agama", s(cmbAgama));

        CetakAsesmen.h(rows, "Riwayat Alergi & Pembedahan");
        CetakAsesmen.r(rows, "Riwayat Alergi", nz(taRiwayatAlergi.getText(), ""));
        CetakAsesmen.r(rows, "Riwayat Pembedahan / Rawat Inap", nz(taRiwayatBedah.getText(), ""));

        CetakAsesmen.h(rows, "Ringkasan Kunjungan Rawat Jalan");
        tambahRingkasanKunjungan(rows);

        CetakAsesmen.Identitas id = new CetakAsesmen.Identitas();
        id.nama = TPasien.getText();
        id.noRawat = TNoRw.getText();
        id.kelas = TKelas.getText();
        id.nik = TNIK.getText();
        id.tglMasuk = Sequel.cariIsi("select concat(date_format(tgl_registrasi,'%d-%m-%Y'),' ',jam_reg) "
                + "from reg_periksa where no_rawat=?", TNoRw.getText());
        id.pembayaran = TCaraBayar.getText();
        id.jk = TJK.getText();
        id.noRM = TNoRM.getText();
        id.unit = TUnit.getText();
        id.tglLahir = TTglLahir.getText();
        id.alamat = TAlamat.getText();

        CetakAsesmen.cetak("ASESMEN & RINGKASAN RAWAT JALAN", "RM RJ", rows, id,
                dtpTtd.getSelectedItem() + "", "Pelaksana Asesmen", KdPetugas.getText(), NmPetugas.getText(),
                "", "");
    }

    /** Cetak langsung dari no_rawat tanpa membuka form (dipakai dari klik-kanan di layar Riwayat). */
    public static void cetak(String noRawat) {
        if (noRawat == null || noRawat.trim().isEmpty()) {
            return;
        }
        RMAsesmenRalan f = new RMAsesmenRalan();
        f.setKonteks(noRawat.trim());
        f.cetak();
    }

    /** Ringkasan kunjungan: 1 baris per kunjungan (tanggal, poli, diagnosa, terapi, dokter). */
    private void tambahRingkasanKunjungan(List<Map<String, ?>> rows) {
        int n = 0;
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select concat(date_format(pr.tgl_perawatan,'%d-%m-%Y'),' ',left(pr.jam_rawat,5)) as tanggal,"
                + "ifnull(poliklinik.nm_poli,'') as nm_poli,ifnull(dokter.nm_dokter,'') as nm_dokter,"
                + "ifnull((select group_concat(distinct penyakit.nm_penyakit separator ', ') from diagnosa_pasien dp "
                + "inner join penyakit on penyakit.kd_penyakit=dp.kd_penyakit where dp.no_rawat=pr.no_rawat),'') as diagnosa,"
                + "ifnull((select group_concat(concat(databarang.nama_brng,' (',resep_dokter.jml,' ',resep_dokter.aturan_pakai,')') separator ', ') "
                + "from resep_obat inner join resep_dokter on resep_dokter.no_resep=resep_obat.no_resep "
                + "inner join databarang on databarang.kode_brng=resep_dokter.kode_brng where resep_obat.no_rawat=pr.no_rawat),'') as terapi "
                + "from pemeriksaan_ralan pr "
                + "inner join reg_periksa on pr.no_rawat=reg_periksa.no_rawat "
                + "left join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "
                + "left join dokter on reg_periksa.kd_dokter=dokter.kd_dokter "
                + "where reg_periksa.no_rkm_medis=? order by pr.tgl_perawatan desc,pr.jam_rawat desc limit 50")) {
            ps.setString(1, TNoRM.getText());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    n++;
                    String ket = nz(rs.getString("nm_poli"), "-") + " | Diagnosa: " + nz(rs.getString("diagnosa"), "-")
                            + " | Terapi: " + nz(rs.getString("terapi"), "-") + " | Dokter: " + nz(rs.getString("nm_dokter"), "-");
                    CetakAsesmen.r(rows, rs.getString("tanggal"), ket);
                }
            }
        } catch (Exception e) {
            System.out.println("Notif ringkasan kunjungan asesmen ralan : " + e);
        }
        if (n == 0) {
            CetakAsesmen.r(rows, "Ringkasan Kunjungan", "Data tidak ditemukan");
        }
    }

    // ====================== util ======================
    private static String nz(String v, String def) {
        return (v == null || v.trim().isEmpty()) ? def : v;
    }

    private String s(widget.ComboBox c) {
        Object o = c.getSelectedItem();
        return o == null ? "" : o.toString();
    }

    private String g(ResultSet rs, String kolom) {
        try {
            String v = rs.getString(kolom);
            return v == null ? "" : v;
        } catch (Exception e) {
            return "";
        }
    }

    private void setCombo(widget.ComboBox c, String v) {
        if (v == null || v.trim().equals("")) { return; }
        c.setSelectedItem(v.trim());
    }

    /** Mapping status nikah dari master pasien ke pilihan combo. */
    private void setNikahFromMaster(String v) {
        if (v == null) { return; }
        String low = v.toLowerCase();
        if (low.contains("belum")) { cmbNikah.setSelectedItem("Belum menikah"); }
        else if (low.contains("cerai")) { cmbNikah.setSelectedItem("Duda/Janda (bercerai)"); }
        else if (low.contains("janda") || low.contains("duda")) { cmbNikah.setSelectedItem("Duda/Janda (meninggal)"); }
        else if (low.contains("nikah") || low.contains("kawin")) { cmbNikah.setSelectedItem("Menikah"); }
    }

    private void setTgl(widget.Tanggal picker, String tgl, String jam) {
        if (tgl == null || tgl.startsWith("0000") || tgl.trim().equals("")) {
            picker.setDate(new Date());
            return;
        }
        String jm = (jam == null || jam.trim().equals("")) ? "00:00:00" : jam;
        if (jm.length() > 8) { jm = jm.substring(0, 8); }
        try {
            picker.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(tgl.substring(0, 10) + " " + jm));
        } catch (Exception e) {
            picker.setDate(new Date());
        }
    }

    private void pastikanTabel() {
        try {
            koneksi.prepareStatement(
                    "create table if not exists asesmen_ralan ("
                    + "no_rawat varchar(17) not null primary key,"
                    + "tanggal date,jam time,rujukan text,cara_datang text,informasi_dari text,"
                    + "penyakit_sekarang text,penyakit_dahulu text,td text,suhu text,nadi text,rr text,nyeri text,skala_nyeri text,"
                    + "bb text,tb text,imt text,lingkar_kepala text,alat_bantu text,prothesa text,cacat_tubuh text,adl text,mandiri text,"
                    + "status_nikah text,saudara text,jumlah_saudara text,negara text,wna_asal text,pekerjaan text,tinggal_bersama text,"
                    + "nama_keluarga text,telepon text,agama text,riwayat_alergi text,riwayat_bedah text,tgl_ttd date,jam_ttd time,nik varchar(20)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=latin1").execute();
        } catch (Exception e) {
            System.out.println("Notif pastikan tabel asesmen_ralan : " + e);
        }
        // Migrasi: tambah kolom riwayat bila tabel sudah ada dari versi sebelumnya
        tambahKolomBilaHilang("riwayat_alergi", "text");
        tambahKolomBilaHilang("riwayat_bedah", "text");
    }

    private void tambahKolomBilaHilang(String kolom, String tipe) {
        try {
            if (Sequel.cariInteger("select count(*) from information_schema.columns where table_schema=database() "
                    + "and table_name='asesmen_ralan' and column_name='" + kolom + "'") == 0) {
                koneksi.prepareStatement("alter table asesmen_ralan add column " + kolom + " " + tipe).execute();
            }
        } catch (Exception e) {
            System.out.println("Notif migrasi kolom asesmen_ralan (" + kolom + ") : " + e);
        }
    }

    // ====================== helper widget ======================
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

    private static widget.ComboBox cmb(String... items) {
        widget.ComboBox c = new widget.ComboBox();
        for (String it : items) { c.addItem(it); }
        return c;
    }

    private static widget.Tanggal dt() {
        widget.Tanggal d = new widget.Tanggal();
        d.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
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
        l.setBorder(BorderFactory.createEmptyBorder(5, 6, 5, 6));
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
        sc.setPreferredSize(new Dimension(400, 52));
        sc.setWheelScrollingEnabled(false);
        p.add(sc, gc(1, row, 3, 1.0));
        return row + 1;
    }

    private int grup(JPanel p, int row, String label, JPanel grupPanel) {
        p.add(lbl(label), gc(0, row, 1, 0.0));
        p.add(grupPanel, gc(1, row, 3, 1.0));
        return row + 1;
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t + " :");
        l.setFont(new Font("Tahoma", Font.PLAIN, 11));
        return l;
    }

    private void siz(Component c) {
        if (c instanceof widget.TextBox || c instanceof widget.ComboBox || c instanceof widget.Tanggal) {
            c.setPreferredSize(new Dimension(220, 23));
        }
    }

    /** Grup checkbox: get/set sebagai string gabungan dipisah koma. */
    private static final class Grup implements CetakAsesmen.OpsiCheckbox {
        final JPanel panel = new JPanel(new GridLayout(0, 4, 4, 0));
        final List<JCheckBox> boxes = new ArrayList<>();

        Grup(String... items) {
            panel.setOpaque(false);
            for (String it : items) {
                JCheckBox c = new JCheckBox(it);
                c.setOpaque(false);
                c.setFont(new Font("Tahoma", Font.PLAIN, 11));
                boxes.add(c);
                panel.add(c);
            }
        }

        @Override
        public List<String> semuaOpsi() {
            List<String> hasil = new ArrayList<>();
            for (JCheckBox c : boxes) { hasil.add(c.getText()); }
            return hasil;
        }

        @Override
        public String get() {
            StringBuilder sb = new StringBuilder();
            for (JCheckBox c : boxes) {
                if (c.isSelected()) {
                    if (sb.length() > 0) { sb.append(", "); }
                    sb.append(c.getText());
                }
            }
            return sb.toString();
        }

        void set(String v) {
            Set<String> sel = new HashSet<>();
            if (v != null) {
                for (String x : v.split(",")) { sel.add(x.trim()); }
            }
            for (JCheckBox c : boxes) { c.setSelected(sel.contains(c.getText())); }
        }

        void clear() {
            for (JCheckBox c : boxes) { c.setSelected(false); }
        }
    }
}
