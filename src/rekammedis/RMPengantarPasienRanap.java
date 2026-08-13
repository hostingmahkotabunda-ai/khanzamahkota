package rekammedis;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Pengantar Pasien Rawat Inap (RM 3a). Dibuka dari tab "Penilaian Awal" di
 * DlgRawatInap. Identitas pasien ditarik otomatis; tanda vital, antropometri,
 * status fungsional, dan catatan klinis diisi manual saat itu juga (bukan
 * data historis yang bisa ditarik dari tabel lain).
 */
public final class RMPengantarPasienRanap extends JDialog {

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();

    // Header identitas (readonly, dari pasien/reg_periksa)
    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TJK = ro();
    private final widget.TextBox TTglLahir = ro();
    private final widget.TextArea TAlamat = ta();

    // Informasi rujukan
    private final widget.TextBox tRuanganTujuan = tf();
    private final widget.Tanggal dtpTanggal = dt();
    private final widget.TextBox tWali = tf();

    // Tanda vital
    private final widget.TextBox tTD = tf();
    private final widget.TextBox tNadi = tf();
    private final widget.TextBox tSuhu = tf();
    private final widget.TextBox tNafas = tf();
    private final widget.ComboBox cmbSkorNyeriAda = cmb("-", "Ya", "Tidak");
    private final widget.TextBox tSkalaNyeri = tf();

    // Antropometri
    private final widget.TextBox tBB = tf();
    private final widget.TextBox tTB = tf();
    private final widget.TextBox tLingkarKepala = tf();

    // Status fungsional
    private final widget.TextBox tAlatBantu = tf();
    private final widget.TextBox tProthesa = tf();
    private final widget.TextBox tCacatTubuh = tf();
    private final widget.ComboBox cmbADL = cmb("-", "Mandiri", "Dibantu");
    private final widget.ComboBox cmbResikoJatuh = cmb("-", "Ringan", "Sedang", "Berat");
    private final widget.TextBox tScoreJatuh = tf();
    private final widget.TextBox tPerawatPenulis = tf();

    // Catatan klinis dokter
    private final widget.TextArea taRiwayatPenyakit = ta();
    private final widget.TextArea taPemeriksaanJasmani = ta();
    private final widget.TextArea taLaboratorium = ta();
    private final widget.TextArea taDiagnosa = ta();
    private final widget.TextArea taUsulPengobatan = ta();
    private final widget.ComboBox cmbAsalPengobatan = cmb("-", "Poliklinik", "Emergency", "Kamar Bersalin");
    private final widget.TextArea taPengobatanDiberikan = ta();
    private final widget.TextBox tDokterPenulis = tf();
    private String dokterIgdNama = "";

    private final widget.Button BtnBaru = new widget.Button();
    private final widget.Button BtnSimpan = new widget.Button();
    private final widget.Button BtnHapus = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();

    public RMPengantarPasienRanap(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Pengantar Pasien Rawat Inap (RM 3a) ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        ensureTable();
        initComponents();
        setSize(1180, 780);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        final Color utama = new Color(0, 133, 143);
        final Color utamaMuda = new Color(230, 247, 248);
        final Color latar = new Color(246, 249, 251);
        final Color garis = new Color(215, 224, 230);
        final Color teks = new Color(32, 49, 66);

        getContentPane().setBackground(latar);
        getContentPane().setLayout(new BorderLayout());

        JPanel atas = new JPanel(new BorderLayout(12, 10));
        atas.setBackground(latar);
        atas.setBorder(new EmptyBorder(14, 18, 10, 18));

        JPanel barisJudul = new JPanel(new BorderLayout());
        barisJudul.setOpaque(false);
        JPanel blokJudul = new JPanel();
        blokJudul.setOpaque(false);
        blokJudul.setLayout(new BoxLayout(blokJudul, BoxLayout.Y_AXIS));
        JLabel judulUtama = new JLabel("Pengantar Pasien Rawat Inap");
        judulUtama.setFont(new Font("Tahoma", Font.BOLD, 20));
        judulUtama.setForeground(teks);
        JLabel subjudul = new JLabel("Form RM 3a  •  Diisi dokter poliklinik/dokter jaga saat merujuk rawat inap");
        subjudul.setFont(new Font("Tahoma", Font.PLAIN, 12));
        subjudul.setForeground(new Color(92, 107, 119));
        blokJudul.add(judulUtama);
        blokJudul.add(Box.createVerticalStrut(3));
        blokJudul.add(subjudul);
        barisJudul.add(blokJudul, BorderLayout.WEST);

        JLabel statusOtomatis = new JLabel("  Identitas ditarik otomatis  ");
        statusOtomatis.setOpaque(true);
        statusOtomatis.setBackground(utamaMuda);
        statusOtomatis.setForeground(utama);
        statusOtomatis.setFont(new Font("Tahoma", Font.BOLD, 11));
        statusOtomatis.setBorder(BorderFactory.createLineBorder(new Color(142, 205, 210)));
        JPanel panelStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
        panelStatus.setOpaque(false);
        panelStatus.add(statusOtomatis);
        barisJudul.add(panelStatus, BorderLayout.EAST);
        atas.add(barisJudul, BorderLayout.NORTH);

        JPanel ringkasanPasien = new JPanel(new GridLayout(1, 5, 0, 0));
        ringkasanPasien.setBackground(Color.WHITE);
        ringkasanPasien.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis),
                new EmptyBorder(10, 12, 10, 12)));
        ringkasanPasien.add(fieldRingkasan("No. Rawat *", TNoRw, true));
        ringkasanPasien.add(fieldRingkasan("No. RM", TNoRM, true));
        ringkasanPasien.add(fieldRingkasan("Nama Pasien", TPasien, true));
        ringkasanPasien.add(fieldRingkasan("Jenis Kelamin", TJK, true));
        ringkasanPasien.add(fieldRingkasan("Tanggal Lahir", TTglLahir, true));
        atas.add(ringkasanPasien, BorderLayout.CENTER);

        JLabel wajib = new JLabel("* Pasien wajib dipilih");
        wajib.setForeground(new Color(198, 40, 40));
        wajib.setFont(new Font("Tahoma", Font.PLAIN, 10));
        atas.add(wajib, BorderLayout.SOUTH);
        getContentPane().add(atas, BorderLayout.NORTH);

        final CardLayout tataHalaman = new CardLayout();
        final JPanel isiHalaman = new JPanel(tataHalaman);
        isiHalaman.setBackground(latar);

        JPanel rujukan = halaman("1. Informasi Rujukan", utama, latar);
        JPanel kartuRujukan = kartu("Detail Rujukan Rawat Inap", teks, garis);
        int row = 0;
        row = pasanganVertikal(kartuRujukan, row, "Dikirim ke Ruangan", tRuanganTujuan,
                "Tanggal / Jam", dtpTanggal);
        row = tunggalVertikal(kartuRujukan, row, "Wali / Suami / Istri", tWali);
        JLabel infoRujukan = new JLabel(
                "<html>Sertakan surat pengantar dari dokter pengirim (jika ada).</html>");
        infoRujukan.setForeground(new Color(74, 91, 104));
        infoRujukan.setBorder(new EmptyBorder(6, 4, 8, 4));
        kartuRujukan.add(infoRujukan, gc(0, (row * 2) + 1, 4, 1.0));
        rujukan.add(kartuRujukan);
        rujukan.add(Box.createVerticalGlue());

        JPanel vital = halaman("2. Tanda Vital & Antropometri", utama, latar);
        JPanel kartuVital = kartu("Tanda Vital", teks, garis);
        row = 0;
        row = pasanganVertikal(kartuVital, row, "Tekanan Darah (mmHg)", tTD,
                "Frekuensi Nadi (x/menit)", tNadi);
        row = pasanganVertikal(kartuVital, row, "Suhu (°C)", tSuhu,
                "Frekuensi Nafas (x/menit)", tNafas);
        row = pasanganVertikal(kartuVital, row, "Skor Nyeri", cmbSkorNyeriAda,
                "Skala Nyeri (0-10)", tSkalaNyeri);
        vital.add(kartuVital);
        vital.add(Box.createVerticalStrut(10));

        JPanel kartuAntro = kartu("Antropometri", teks, garis);
        row = 0;
        row = pasanganVertikal(kartuAntro, row, "Berat Badan (kg)", tBB,
                "Tinggi Badan (cm)", tTB);
        row = tunggalVertikal(kartuAntro, row, "Lingkar Kepala - Khusus Pediatri (cm)", tLingkarKepala);
        vital.add(kartuAntro);
        vital.add(Box.createVerticalGlue());

        JPanel fungsional = halaman("3. Status Fungsional", utama, latar);
        JPanel kartuFungsional = kartu("Ditulis Perawat", teks, garis);
        row = 0;
        row = pasanganVertikal(kartuFungsional, row, "Alat Bantu", tAlatBantu,
                "Prothesa", tProthesa);
        row = pasanganVertikal(kartuFungsional, row, "Cacat Tubuh", tCacatTubuh,
                "ADL", cmbADL);
        row = pasanganVertikal(kartuFungsional, row, "Resiko Jatuh", cmbResikoJatuh,
                "Score Resiko Jatuh", tScoreJatuh);
        row = tunggalVertikal(kartuFungsional, row, "Nama Perawat Penulis", tPerawatPenulis);
        fungsional.add(kartuFungsional);
        fungsional.add(Box.createVerticalGlue());

        JPanel klinis = halaman("4. Catatan Klinis Dokter", utama, latar);
        JPanel kartuKlinis = kartu("Beri Catatan Singkat yang Positif", teks, garis);
        row = 0;
        row = areaVertikal(kartuKlinis, row, "1. Riwayat Penyakit yang Positif", taRiwayatPenyakit);
        row = areaVertikal(kartuKlinis, row, "2. Pemeriksaan Jasmani", taPemeriksaanJasmani);
        row = areaVertikal(kartuKlinis, row, "3. Laboratorium", taLaboratorium);
        row = areaVertikal(kartuKlinis, row, "4. Diagnosa", taDiagnosa);
        row = areaVertikal(kartuKlinis, row, "5. Usul Pengobatan Diperawatan", taUsulPengobatan);
        row = pasanganVertikal(kartuKlinis, row, "6. Pengobatan Sudah Diberikan Di", cmbAsalPengobatan,
                "Nama & TTD Dokter", tDokterPenulis);
        row = areaVertikal(kartuKlinis, row, "Detail Pengobatan yang Sudah Diberikan", taPengobatanDiberikan);
        klinis.add(kartuKlinis);
        klinis.add(Box.createVerticalGlue());

        isiHalaman.add(bungkusScroll(rujukan), "RUJUKAN");
        isiHalaman.add(bungkusScroll(vital), "VITAL");
        isiHalaman.add(bungkusScroll(fungsional), "FUNGSIONAL");
        isiHalaman.add(bungkusScroll(klinis), "KLINIS");

        JPanel navigasi = new JPanel();
        navigasi.setBackground(Color.WHITE);
        navigasi.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, garis));
        navigasi.setPreferredSize(new Dimension(220, 100));
        navigasi.setLayout(new BoxLayout(navigasi, BoxLayout.Y_AXIS));
        JLabel judulNav = new JLabel("BAGIAN FORM");
        judulNav.setFont(new Font("Tahoma", Font.BOLD, 11));
        judulNav.setForeground(new Color(83, 98, 108));
        judulNav.setBorder(new EmptyBorder(18, 18, 10, 10));
        judulNav.setAlignmentX(Component.LEFT_ALIGNMENT);
        navigasi.add(judulNav);

        String[] namaMenu = {"1  Informasi Rujukan", "2  Vital & Antropometri", "3  Status Fungsional", "4  Catatan Klinis"};
        String[] kunciMenu = {"RUJUKAN", "VITAL", "FUNGSIONAL", "KLINIS"};
        JButton[] tombolMenu = new JButton[namaMenu.length];
        for (int i = 0; i < namaMenu.length; i++) {
            final int indeks = i;
            JButton tombol = new JButton(namaMenu[i]);
            tombolMenu[i] = tombol;
            tombol.setHorizontalAlignment(SwingConstants.LEFT);
            tombol.setFont(new Font("Tahoma", i == 0 ? Font.BOLD : Font.PLAIN, 11));
            tombol.setForeground(i == 0 ? utama : new Color(63, 78, 88));
            tombol.setBackground(i == 0 ? utamaMuda : Color.WHITE);
            tombol.setBorder(new EmptyBorder(12, 18, 12, 8));
            tombol.setFocusPainted(false);
            tombol.setMaximumSize(new Dimension(220, 44));
            tombol.setAlignmentX(Component.LEFT_ALIGNMENT);
            tombol.addActionListener(e -> {
                tataHalaman.show(isiHalaman, kunciMenu[indeks]);
                for (int m = 0; m < tombolMenu.length; m++) {
                    boolean aktif = m == indeks;
                    tombolMenu[m].setBackground(aktif ? utamaMuda : Color.WHITE);
                    tombolMenu[m].setForeground(aktif ? utama : new Color(63, 78, 88));
                    tombolMenu[m].setFont(new Font("Tahoma", aktif ? Font.BOLD : Font.PLAIN, 11));
                }
            });
            navigasi.add(tombol);
            navigasi.add(Box.createVerticalStrut(4));
        }
        navigasi.add(Box.createVerticalGlue());
        JLabel infoNav = new JLabel("<html>Field abu-abu berasal dari registrasi</html>");
        infoNav.setFont(new Font("Tahoma", Font.PLAIN, 10));
        infoNav.setForeground(new Color(91, 105, 115));
        infoNav.setBorder(new EmptyBorder(10, 16, 16, 10));
        infoNav.setAlignmentX(Component.LEFT_ALIGNMENT);
        navigasi.add(infoNav);

        JPanel tengah = new JPanel(new BorderLayout());
        tengah.setBackground(latar);
        tengah.add(navigasi, BorderLayout.WEST);
        tengah.add(isiHalaman, BorderLayout.CENTER);
        getContentPane().add(tengah, BorderLayout.CENTER);

        BtnBaru.setText("Baru");
        BtnSimpan.setText("Simpan Data");
        BtnHapus.setText("Hapus Data");
        BtnKeluar.setText("Keluar");
        BtnBaru.addActionListener(e -> setNoRm(ambil(TNoRw)));
        BtnSimpan.addActionListener(e -> simpan());
        BtnHapus.addActionListener(e -> hapus());
        BtnKeluar.addActionListener(e -> dispose());
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 9));
        bawah.setBackground(Color.WHITE);
        bawah.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, garis));
        bawah.add(BtnHapus);
        bawah.add(BtnBaru);
        bawah.add(BtnKeluar);
        bawah.add(BtnSimpan);
        getContentPane().add(bawah, BorderLayout.SOUTH);
    }

    public void isCek() {
        boolean bisa = akses.getpenilaian_awal_keperawatan_ranap();
        BtnSimpan.setEnabled(bisa);
        BtnHapus.setEnabled(bisa);
    }

    public void emptTeks() {
        for (widget.TextBox t : new widget.TextBox[]{TNoRw, TNoRM, TPasien, TJK, TTglLahir, tRuanganTujuan, tWali,
            tTD, tNadi, tSuhu, tNafas, tSkalaNyeri, tBB, tTB, tLingkarKepala, tAlatBantu, tProthesa, tCacatTubuh,
            tScoreJatuh, tPerawatPenulis, tDokterPenulis}) {
            t.setText("");
        }
        for (widget.TextArea a : new widget.TextArea[]{TAlamat, taRiwayatPenyakit, taPemeriksaanJasmani,
            taLaboratorium, taDiagnosa, taUsulPengobatan, taPengobatanDiberikan}) {
            a.setText("");
        }
        for (widget.ComboBox c : new widget.ComboBox[]{cmbSkorNyeriAda, cmbADL, cmbResikoJatuh, cmbAsalPengobatan}) {
            c.setSelectedIndex(0);
        }
        dtpTanggal.setDate(new Date());
    }

    /** Dipanggil dari DlgRawatInap tab Penilaian Awal. Tarik identitas lalu timpa dengan data tersimpan bila ada. */
    public void setNoRm(String norawat) {
        emptTeks();
        if (norawat == null || norawat.trim().equals("")) {
            return;
        }
        TNoRw.setText(norawat);
        tarikDataPasien(norawat);
        muatDataJikaAda(norawat);
    }

    private void tarikDataPasien(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select p.no_rkm_medis,p.nm_pasien,p.jk,ifnull(date_format(p.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir,"
                + "concat(ifnull(p.alamat,''),"
                + "if(kelurahan.nm_kel is null or kelurahan.nm_kel='','',concat(', ',kelurahan.nm_kel)),"
                + "if(kecamatan.nm_kec is null or kecamatan.nm_kec='','',concat(', ',kecamatan.nm_kec)),"
                + "if(kabupaten.nm_kab is null or kabupaten.nm_kab='','',concat(', ',kabupaten.nm_kab))) as alamat_lengkap,"
                + "ifnull(rp.p_jawab,'') as p_jawab,ifnull(dokter.nm_dokter,'') as nm_dokter_igd "
                + "from reg_periksa rp inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis "
                + "left join kelurahan on p.kd_kel=kelurahan.kd_kel "
                + "left join kecamatan on p.kd_kec=kecamatan.kd_kec "
                + "left join kabupaten on p.kd_kab=kabupaten.kd_kab "
                + "left join dokter on rp.kd_dokter=dokter.kd_dokter "
                + "where rp.no_rawat=?")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    TJK.setText("L".equalsIgnoreCase(rs.getString("jk")) ? "Laki-Laki" : "Perempuan");
                    TTglLahir.setText(rs.getString("tgl_lahir"));
                    TAlamat.setText(rs.getString("alamat_lengkap"));
                    tWali.setText(rs.getString("p_jawab"));
                    dokterIgdNama = rs.getString("nm_dokter_igd");
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik data pasien pengantar ranap : " + e);
        }

        try (PreparedStatement ps = koneksi.prepareStatement(
                "select ifnull(bangsal.nm_bangsal,'') as ruang "
                + "from kamar_inap inner join kamar on kamar.kd_kamar=kamar_inap.kd_kamar "
                + "inner join bangsal on bangsal.kd_bangsal=kamar.kd_bangsal "
                + "where kamar_inap.no_rawat=? order by kamar_inap.tgl_masuk desc limit 1")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tRuanganTujuan.setText(rs.getString("ruang"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik kamar pengantar ranap : " + e);
        }

        dtpTanggal.setDate(new Date());
        String namaPetugas = Sequel.cariIsi("select nama from petugas where nip=?", akses.getkode());
        tPerawatPenulis.setText(namaPetugas);
        tDokterPenulis.setText(dokterIgdNama);
    }

    private void muatDataJikaAda(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select * from pengantar_pasien_ranap where no_rawat=?")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tRuanganTujuan.setText(nvl(rs.getString("ruangan_tujuan")));
                    if (rs.getDate("tanggal") != null) {
                        String jam = nvl(rs.getString("jam"));
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            dtpTanggal.setDate(sdf.parse(rs.getDate("tanggal") + " " + (jam.equals("") ? "00:00:00" : jam)));
                        } catch (Exception ignore) {
                        }
                    }
                    tWali.setText(nvl(rs.getString("wali")));
                    tTD.setText(nvl(rs.getString("td")));
                    tNadi.setText(nvl(rs.getString("nadi")));
                    tSuhu.setText(nvl(rs.getString("suhu")));
                    tNafas.setText(nvl(rs.getString("frekuensi_nafas")));
                    cmbSkorNyeriAda.setSelectedItem(cocokkanOpsi(cmbSkorNyeriAda, rs.getString("skor_nyeri_ada")));
                    tSkalaNyeri.setText(nvl(rs.getString("skala_nyeri")));
                    tBB.setText(nvl(rs.getString("bb")));
                    tTB.setText(nvl(rs.getString("tb")));
                    tLingkarKepala.setText(nvl(rs.getString("lingkar_kepala")));
                    tAlatBantu.setText(nvl(rs.getString("alat_bantu")));
                    tProthesa.setText(nvl(rs.getString("prothesa")));
                    tCacatTubuh.setText(nvl(rs.getString("cacat_tubuh")));
                    cmbADL.setSelectedItem(cocokkanOpsi(cmbADL, rs.getString("adl")));
                    cmbResikoJatuh.setSelectedItem(cocokkanOpsi(cmbResikoJatuh, rs.getString("resiko_jatuh")));
                    tScoreJatuh.setText(nvl(rs.getString("score_jatuh")));
                    if (!nvl(rs.getString("perawat_penulis")).equals("")) {
                        tPerawatPenulis.setText(rs.getString("perawat_penulis"));
                    }
                    taRiwayatPenyakit.setText(nvl(rs.getString("riwayat_penyakit")));
                    taPemeriksaanJasmani.setText(nvl(rs.getString("pemeriksaan_jasmani")));
                    taLaboratorium.setText(nvl(rs.getString("laboratorium")));
                    taDiagnosa.setText(nvl(rs.getString("diagnosa")));
                    taUsulPengobatan.setText(nvl(rs.getString("usul_pengobatan")));
                    cmbAsalPengobatan.setSelectedItem(cocokkanOpsi(cmbAsalPengobatan, rs.getString("asal_pengobatan")));
                    taPengobatanDiberikan.setText(nvl(rs.getString("pengobatan_diberikan")));
                    if (!nvl(rs.getString("dokter_penulis")).equals("")) {
                        tDokterPenulis.setText(rs.getString("dokter_penulis"));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat pengantar ranap : " + e);
        }
    }

    private void simpan() {
        if (ambil(TNoRw).equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        String tgl = Valid.SetTgl(dtpTanggal.getSelectedItem() + "");
        String jam = dtpTanggal.getSelectedItem().toString().length() >= 19
                ? dtpTanggal.getSelectedItem().toString().substring(11, 19) : "";
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into pengantar_pasien_ranap (no_rawat,ruangan_tujuan,tanggal,jam,wali,td,nadi,suhu,"
                + "frekuensi_nafas,skor_nyeri_ada,skala_nyeri,bb,tb,lingkar_kepala,alat_bantu,prothesa,cacat_tubuh,"
                + "adl,resiko_jatuh,score_jatuh,perawat_penulis,riwayat_penyakit,pemeriksaan_jasmani,laboratorium,"
                + "diagnosa,usul_pengobatan,asal_pengobatan,pengobatan_diberikan,dokter_penulis,updated_by,updated_at,"
                + "created_by,created_at) "
                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,now(),?,now()) "
                + "on duplicate key update ruangan_tujuan=values(ruangan_tujuan),tanggal=values(tanggal),jam=values(jam),"
                + "wali=values(wali),td=values(td),nadi=values(nadi),suhu=values(suhu),frekuensi_nafas=values(frekuensi_nafas),"
                + "skor_nyeri_ada=values(skor_nyeri_ada),skala_nyeri=values(skala_nyeri),bb=values(bb),tb=values(tb),"
                + "lingkar_kepala=values(lingkar_kepala),alat_bantu=values(alat_bantu),prothesa=values(prothesa),"
                + "cacat_tubuh=values(cacat_tubuh),adl=values(adl),resiko_jatuh=values(resiko_jatuh),"
                + "score_jatuh=values(score_jatuh),perawat_penulis=values(perawat_penulis),"
                + "riwayat_penyakit=values(riwayat_penyakit),pemeriksaan_jasmani=values(pemeriksaan_jasmani),"
                + "laboratorium=values(laboratorium),diagnosa=values(diagnosa),usul_pengobatan=values(usul_pengobatan),"
                + "asal_pengobatan=values(asal_pengobatan),pengobatan_diberikan=values(pengobatan_diberikan),"
                + "dokter_penulis=values(dokter_penulis),updated_by=values(updated_by),updated_at=now()")) {
            int i = 1;
            ps.setString(i++, ambil(TNoRw));
            ps.setString(i++, ambil(tRuanganTujuan));
            if (tgl == null || tgl.trim().equals("")) {
                ps.setNull(i++, java.sql.Types.DATE);
            } else {
                ps.setString(i++, tgl);
            }
            ps.setString(i++, jam);
            ps.setString(i++, ambil(tWali));
            ps.setString(i++, ambil(tTD));
            ps.setString(i++, ambil(tNadi));
            ps.setString(i++, ambil(tSuhu));
            ps.setString(i++, ambil(tNafas));
            ps.setString(i++, s(cmbSkorNyeriAda));
            ps.setString(i++, ambil(tSkalaNyeri));
            ps.setString(i++, ambil(tBB));
            ps.setString(i++, ambil(tTB));
            ps.setString(i++, ambil(tLingkarKepala));
            ps.setString(i++, ambil(tAlatBantu));
            ps.setString(i++, ambil(tProthesa));
            ps.setString(i++, ambil(tCacatTubuh));
            ps.setString(i++, s(cmbADL));
            ps.setString(i++, s(cmbResikoJatuh));
            ps.setString(i++, ambil(tScoreJatuh));
            ps.setString(i++, ambil(tPerawatPenulis));
            ps.setString(i++, ambil(taRiwayatPenyakit));
            ps.setString(i++, ambil(taPemeriksaanJasmani));
            ps.setString(i++, ambil(taLaboratorium));
            ps.setString(i++, ambil(taDiagnosa));
            ps.setString(i++, ambil(taUsulPengobatan));
            ps.setString(i++, s(cmbAsalPengobatan));
            ps.setString(i++, ambil(taPengobatanDiberikan));
            ps.setString(i++, ambil(tDokterPenulis));
            ps.setString(i++, akses.getkode());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Pengantar pasien rawat inap tersimpan.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan.\n" + e.getMessage());
        }
    }

    private void hapus() {
        if (ambil(TNoRw).equals("")) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus pengantar pasien rawat inap untuk No.Rawat " + ambil(TNoRw) + " ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement("delete from pengantar_pasien_ranap where no_rawat=?")) {
            ps.setString(1, ambil(TNoRw));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data dihapus.");
            String norw = ambil(TNoRw);
            setNoRm(norw);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus.\n" + e.getMessage());
        }
    }

    private void ensureTable() {
        Sequel.queryu2(
                "create table if not exists pengantar_pasien_ranap ("
                + "no_rawat varchar(17) not null primary key,"
                + "ruangan_tujuan varchar(60) null,"
                + "tanggal date null,"
                + "jam varchar(8) null,"
                + "wali varchar(60) null,"
                + "td varchar(20) null,"
                + "nadi varchar(20) null,"
                + "suhu varchar(10) null,"
                + "frekuensi_nafas varchar(20) null,"
                + "skor_nyeri_ada varchar(10) null,"
                + "skala_nyeri varchar(5) null,"
                + "bb varchar(20) null,"
                + "tb varchar(20) null,"
                + "lingkar_kepala varchar(20) null,"
                + "alat_bantu varchar(100) null,"
                + "prothesa varchar(100) null,"
                + "cacat_tubuh varchar(100) null,"
                + "adl varchar(20) null,"
                + "resiko_jatuh varchar(20) null,"
                + "score_jatuh varchar(20) null,"
                + "perawat_penulis varchar(60) null,"
                + "riwayat_penyakit text null,"
                + "pemeriksaan_jasmani text null,"
                + "laboratorium text null,"
                + "diagnosa text null,"
                + "usul_pengobatan text null,"
                + "asal_pengobatan varchar(20) null,"
                + "pengobatan_diberikan text null,"
                + "dokter_penulis varchar(60) null,"
                + "created_by varchar(50) null,"
                + "updated_by varchar(50) null,"
                + "created_at datetime null,"
                + "updated_at datetime null"
                + ")");
    }

    private static String cocokkanOpsi(widget.ComboBox combo, String nilai) {
        if (nilai == null || nilai.trim().equals("")) { return "-"; }
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object it = combo.getItemAt(i);
            if (it != null && it.toString().equalsIgnoreCase(nilai.trim())) {
                return it.toString();
            }
        }
        return "-";
    }

    // ====================== Helpers UI (pola sama dengan RMRingkasanRiwayatMasuk) ======================
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
        t.setRows(3);
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

    private JPanel fieldRingkasan(String label, Component komponen, boolean bacaSaja) {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(0, 10, 0, 10));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Tahoma", Font.PLAIN, 10));
        l.setForeground(label.contains("*") ? new Color(190, 35, 35) : new Color(86, 101, 112));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        komponen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        komponen.setPreferredSize(new Dimension(150, 25));
        if (bacaSaja) {
            komponen.setBackground(new Color(248, 250, 251));
        }
        p.add(l);
        p.add(Box.createVerticalStrut(3));
        p.add(komponen);
        return p;
    }

    private JPanel halaman(String judul, Color utama, Color latar) {
        JPanel p = new JPanel();
        p.setBackground(latar);
        p.setBorder(new EmptyBorder(14, 18, 18, 18));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(judul);
        l.setFont(new Font("Tahoma", Font.BOLD, 16));
        l.setForeground(utama);
        l.setBorder(new EmptyBorder(0, 2, 10, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        return p;
    }

    private JPanel kartu(String judul, Color teks, Color garis) {
        JPanel luar = new JPanel(new GridBagLayout());
        luar.setBackground(Color.WHITE);
        luar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis),
                new EmptyBorder(8, 12, 12, 12)));
        luar.setAlignmentX(Component.LEFT_ALIGNMENT);
        luar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2000));
        JLabel l = new JLabel(judul);
        l.setFont(new Font("Tahoma", Font.BOLD, 13));
        l.setForeground(teks);
        GridBagConstraints g = gc(0, 0, 4, 1.0);
        g.insets = new Insets(2, 4, 10, 4);
        luar.add(l, g);
        return luar;
    }

    private int pasanganVertikal(JPanel p, int row, String label1, Component komponen1,
            String label2, Component komponen2) {
        int barisLabel = (row * 2) + 1;
        int barisInput = barisLabel + 1;
        p.add(labelAtas(label1), gc(0, barisLabel, 2, 0.5));
        p.add(labelAtas(label2), gc(2, barisLabel, 2, 0.5));
        siapkanInput(komponen1);
        siapkanInput(komponen2);
        GridBagConstraints kiri = gc(0, barisInput, 2, 0.5);
        kiri.insets = new Insets(1, 4, 8, 10);
        GridBagConstraints kanan = gc(2, barisInput, 2, 0.5);
        kanan.insets = new Insets(1, 10, 8, 4);
        p.add(komponen1, kiri);
        p.add(komponen2, kanan);
        return row + 1;
    }

    private int tunggalVertikal(JPanel p, int row, String label, Component komponen) {
        int barisLabel = (row * 2) + 1;
        int barisInput = barisLabel + 1;
        p.add(labelAtas(label), gc(0, barisLabel, 4, 1.0));
        siapkanInput(komponen);
        GridBagConstraints g = gc(0, barisInput, 4, 1.0);
        g.insets = new Insets(1, 4, 8, 4);
        p.add(komponen, g);
        return row + 1;
    }

    private int areaVertikal(JPanel p, int row, String label, widget.TextArea area) {
        int barisLabel = (row * 2) + 1;
        int barisInput = barisLabel + 1;
        p.add(labelAtas(label), gc(0, barisLabel, 4, 1.0));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(600, 70));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(190, 202, 210)));
        GridBagConstraints g = gc(0, barisInput, 4, 1.0);
        g.insets = new Insets(1, 4, 8, 4);
        p.add(scroll, g);
        return row + 1;
    }

    private JLabel labelAtas(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font("Tahoma", Font.PLAIN, 11));
        l.setForeground(new Color(49, 64, 75));
        return l;
    }

    private void siapkanInput(Component komponen) {
        komponen.setPreferredSize(new Dimension(320, 30));
        if (komponen instanceof widget.TextBox && !((widget.TextBox) komponen).isEditable()) {
            komponen.setBackground(new Color(245, 248, 249));
        }
    }

    private JScrollPane bungkusScroll(JPanel halaman) {
        JScrollPane scroll = new JScrollPane(halaman);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.getViewport().setBackground(new Color(246, 249, 251));
        return scroll;
    }

    private GridBagConstraints gc(int x, int y, int w, double wx) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x; g.gridy = y; g.gridwidth = w; g.weightx = wx;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(2, 4, 2, 4);
        return g;
    }

    private static String s(widget.ComboBox c) {
        Object v = c.getSelectedItem();
        String r = v == null ? "" : v.toString();
        return "-".equals(r) ? "" : r;
    }

    private static String ambil(widget.TextBox t) {
        return t.getText() == null ? "" : t.getText().trim();
    }

    private static String ambil(widget.TextArea t) {
        return t.getText() == null ? "" : t.getText().trim();
    }

    private static String nvl(String v) {
        return v == null ? "" : v;
    }
}
