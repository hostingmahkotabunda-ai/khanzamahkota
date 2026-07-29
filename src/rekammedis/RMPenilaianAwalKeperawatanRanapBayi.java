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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import kepegawaian.DlgCariDokter;

/**
 * Form Asesmen Keperawatan Bayi (rawat inap). Dibuka dari menu "Keperawatan Bayi"
 * di DlgKamarInap (pola sama dengan Keperawatan Anak). Dibangun programatik.
 * Data disimpan ke asesmen_keperawatan_bayi (+ _persalinan, _gambar).
 */
public final class RMPenilaianAwalKeperawatanRanapBayi extends JDialog {

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final DlgCariDokter dokter = new DlgCariDokter(null, true);

    // ===== Header (read-only) =====
    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TJK = ro();
    private final widget.TextBox TTglLahir = ro();
    private final widget.TextBox TAlamat = ro();
    private final widget.TextBox TUnit = ro();
    private final widget.TextBox TCaraBayar = ro();

    // ===== Data umum / asesmen =====
    private final widget.TextBox tRuang = tf();
    private final widget.TextBox tLantai = tf();
    private final widget.TextBox tKelas = tf();
    private final widget.ComboBox cmbGelang = cmb("Ya", "Tidak");
    private final widget.Tanggal dtpTanggal = dt();

    // A. Penanggung jawab
    private final widget.TextBox pjNama = tf();
    private final widget.TextBox pjAlamat = tf();
    private final widget.TextBox pjUmur = tf();
    private final widget.TextBox pjPendidikan = tf();
    private final widget.TextBox pjHubungan = tf();

    // B. Riwayat ibu
    private final widget.TextBox ibuNama = tf();
    private final widget.TextBox ibuUmur = tf();
    private final widget.TextBox ibuPendidikan = tf();
    private final widget.TextBox ibuSuku = tf();
    private final widget.TextBox ibuAgama = tf();
    private final widget.TextBox ibuPenyakit = tf();
    private final widget.TextBox ibuPerkawinan = tf();

    // C. Riwayat penyakit
    private final widget.TextArea taKeluhan = ta();
    private final widget.TextArea taRiwSekarang = ta();
    private final widget.TextArea taRiwDahulu = ta();

    // D. Riwayat kehamilan
    private final Grup grpKomplikasi = new Grup("Diabetes", "Hipertensi", "Toksemi", "Eklampsi", "Jantung");
    private final widget.TextArea taKomplikasiLain = ta();

    // Riwayat persalinan lalu
    private final widget.Table tbPersalinan = new widget.Table();
    private final DefaultTableModel modePersalinan = new DefaultTableModel(null, new Object[]{
        "BB Lahir", "Jenis Kelamin", "Jenis Persalinan", "Komplikasi", "Kondisi Saat Ini", "Riwayat Imunisasi", "Tahun Lahir"
    });

    // F. Psikososial
    private final widget.TextArea taPsiko = ta();
    private final Grup grpPengasuh = new Grup("Ayah", "Ibu", "Nenek", "Orang Lain", "Negara/Pemerintahan");
    private final widget.ComboBox cmbSibling = cmb("Ada", "Tidak Ada");
    private final widget.ComboBox cmbDukKeluarga = cmb("Ada", "Tidak Ada");
    private final widget.TextBox tDukKeluargaSebut = tf();
    private final widget.ComboBox cmbBudaya = cmb("Ada", "Tidak Ada");
    private final widget.TextBox tBudayaSebut = tf();

    // G. Pemeriksaan fisik
    private final widget.TextBox tApgar = tf();
    private final widget.TextBox tDown = tf();
    private final widget.TextBox tSuhu = tf();
    private final widget.TextBox tRR = tf();
    private final widget.TextBox tNadi = tf();
    private final widget.TextBox tKesadaran = tf();
    private final widget.TextBox tBB = tf();
    private final widget.TextBox tPB = tf();
    private final Grup grpTangisan = new Grup("Menangis Kuat", "Merintih");
    private final widget.ComboBox cmbCrt = cmb("Kurang dari 2 detik", "Lebih dari 2 detik");
    private final Grup grpKulit = new Grup("Kemerahan", "Lanugo", "Sianosis", "Vernic Casiosa", "Anemis", "Luka/Lecet", "Ikterik", "Tipis/Lemak(-)", "Mengelupas/Serotinus");
    private final widget.TextBox tLingkarKepala = tf();
    private final Grup grpUbun = new Grup("Caput Succadaneum", "Cepal Haematon", "An Encepalus");
    private final Grup grpMata = new Grup("Sekret Banyak", "Sklera Ikterik");
    private final Grup grpHidung = new Grup("Nafas Spontan Tanpa O2", "O2 Head Box", "Pernafasan Cuping Hidung", "Keluar Darah", "O2 Nasal", "Lendir");
    private final Grup grpTelinga = new Grup("Bentuk Simetris", "Bentuk Asimetris", "Lanugo");
    private final Grup grpMulut = new Grup("Reflek Isap Kuat", "Reflek Isap Lemah", "Muntah", "Mulut Mencucu", "Sianosis Bibir", "Pipa OGT", "Labio Schizis", "Labio Palato S");
    private final widget.TextBox tFreqNafas = tf();
    private final Grup grpDada = new Grup("Retraksi", "Ronchi", "Wheezing", "Grunting", "Apnoe", "Batuk");
    private final Grup grpAbdomen = new Grup("Tali Pusar Segar", "Tali Pusat Layu", "Kembung", "Distensi", "Turgor Elastis", "Turgor Jelek", "Bising Usus");
    private final widget.ComboBox cmbJK = cmb("Laki-Laki", "Perempuan");
    private final widget.ComboBox cmbTestis = cmb("-", "Turun", "Tidak");
    private final widget.ComboBox cmbLabia = cmb("-", "Menutup", "Tidak");
    private final widget.ComboBox cmbAnus = cmb("Ada", "Tidak");
    private final widget.TextBox tBabFreq = tf();
    private final Grup grpBab = new Grup("Mekonium", "Kuning Cair", "Kuning Berampas", "Stetorhoe", "Melana");
    private final widget.TextBox tBak = tf();
    private final Grup grpEkstrAtas = new Grup("Tonus Otot (+)", "Tonus Otot (-)", "Fraktur", "Oedema", "Syanosis", "Ikterik", "Opsitotonus", "Reflek Moro");
    private final Grup grpEkstrBawah = new Grup("Tonus Otot (+)", "Tonus Otot (-)", "Fraktur", "Oedema", "Syanosis", "Ikterik", "Kejang");
    private final widget.ComboBox cmbReflekMoro = cmb("-", "Kuat", "Lemah");
    private final widget.ComboBox cmbReflekMengisap = cmb("-", "Kuat", "Lemah");
    private final widget.TextBox tBabinski = tf();
    private final widget.ComboBox cmbReflekRooting = cmb("-", "Kuat", "Lemah");
    private final Grup grpAktifitas = new Grup("Bayi Aktif", "Bayi Hipoaktif", "Rewel");
    private final Grup grpHygiene = new Grup("Bersih", "Kotor", "Mandi", "Tali Pusat Dirawat");
    private final Grup grpNutrisi = new Grup("ASI", "PASI", "Menggunakan OGT", "Puasa");
    private final widget.ComboBox cmbKontakIbu = cmb("Ya", "Tidak");
    private final widget.ComboBox cmbBayiDiharapkan = cmb("Ya", "Tidak");
    private final widget.TextArea taObat = ta();

    // Cap kaki
    private byte[] capKaki1 = null;
    private byte[] capKaki2 = null;
    private final JLabel lblCap1 = new JLabel("(belum ada)");
    private final JLabel lblCap2 = new JLabel("(belum ada)");

    // Discharge / perencanaan pulang
    private final JCheckBox cekHambatan = new JCheckBox("Memiliki Hambatan Mobilisasi");
    private final widget.ComboBox cmbTinggal = cmb("Orang Tua", "Lain-lain");
    private final widget.TextBox tTinggalSebut = tf();
    private final widget.ComboBox cmbPerokok = cmb("Tidak", "Ya");
    private final widget.TextBox tPerokokSebut = tf();
    private final Grup grpKondisiRumah = new Grup("Sumber Air Bersih", "Sumber Air Kotor", "Lingkungan Polusi", "Lingkungan Tidak Polusi");

    private final widget.TextArea taMasalah = ta();
    private final widget.TextArea taRencana = ta();

    // TTD
    private final widget.Tanggal dtpTtd = dt();
    private final widget.TextBox KdPetugas = ro();
    private final widget.TextBox NmPetugas = ro();
    private final widget.TextBox KdDokter = tf();
    private final widget.TextBox NmDokter = ro();
    private final widget.Button BtnDokter = new widget.Button();

    private final widget.Button BtnBaru = new widget.Button();
    private final widget.Button BtnSimpan = new widget.Button();
    private final widget.Button BtnHapus = new widget.Button();
    private final widget.Button BtnCetak = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();

    public RMPenilaianAwalKeperawatanRanapBayi(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Asesmen Keperawatan Bayi ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        cekHambatan.setOpaque(false);
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
        setSize(1200, 800);
        setMinimumSize(new Dimension(1050, 700));
        setLocationRelativeTo(parent);
    }

    // ====================== UI ======================
    private void initComponents() {
        final Color utama = new Color(0, 133, 143);
        final Color utamaMuda = new Color(230, 247, 248);
        final Color latar = new Color(246, 249, 251);
        final Color garis = new Color(214, 224, 230);
        final Color teks = new Color(31, 47, 62);
        getContentPane().setBackground(latar);
        getContentPane().setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout(10, 8));
        header.setBackground(latar);
        header.setBorder(new EmptyBorder(12, 16, 9, 16));
        JPanel blokJudul = new JPanel();
        blokJudul.setOpaque(false);
        blokJudul.setLayout(new BoxLayout(blokJudul, BoxLayout.Y_AXIS));
        JLabel judulUtama = new JLabel("Asesmen Keperawatan Bayi");
        judulUtama.setFont(new Font("Tahoma", Font.BOLD, 20));
        judulUtama.setForeground(teks);
        JLabel subJudul = new JLabel("Penilaian awal pasien bayi rawat inap");
        subJudul.setFont(new Font("Tahoma", Font.PLAIN, 12));
        subJudul.setForeground(new Color(89, 104, 115));
        blokJudul.add(judulUtama);
        blokJudul.add(Box.createVerticalStrut(2));
        blokJudul.add(subJudul);
        header.add(blokJudul, BorderLayout.NORTH);

        JPanel identitas = new JPanel(new GridLayout(1, 6, 0, 0));
        identitas.setBackground(Color.WHITE);
        identitas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis), new EmptyBorder(8, 8, 8, 8)));
        identitas.add(ringkasan("No. Rawat *", TNoRw));
        identitas.add(ringkasan("No. RM", TNoRM));
        identitas.add(ringkasan("Nama Pasien", TPasien));
        identitas.add(ringkasan("Jenis Kelamin", TJK));
        identitas.add(ringkasan("Tanggal Lahir", TTglLahir));
        identitas.add(ringkasan("Unit", TUnit));
        header.add(identitas, BorderLayout.CENTER);
        getContentPane().add(header, BorderLayout.NORTH);

        final CardLayout tataHalaman = new CardLayout();
        final JPanel kartuHalaman = new JPanel(tataHalaman);
        kartuHalaman.setBackground(latar);

        JPanel halamanInformasi = formHalaman("1. Informasi Asesmen", utama, latar);
        int row = 0;
        row = judul(halamanInformasi, row, "Informasi Ruang & Waktu");
        row = baris2(halamanInformasi, row, "Ruang", tRuang, "Lantai", tLantai);
        row = baris2(halamanInformasi, row, "Kelas", tKelas, "Gelang Identitas", cmbGelang);
        row = baris1(halamanInformasi, row, "Tanggal / Jam *", dtpTanggal);
        row = judul(halamanInformasi, row, "Informasi Registrasi");
        row = baris2(halamanInformasi, row, "Cara Bayar", TCaraBayar, "Alamat", TAlamat);
        kartuHalaman.add(scrollHalaman(halamanInformasi, latar), "INFORMASI");

        JPanel halamanKeluarga = formHalaman("2. Keluarga & Riwayat Ibu", utama, latar);
        row = 0;
        row = judul(halamanKeluarga, row, "A. Identitas Penanggung Jawab");
        row = baris2(halamanKeluarga, row, "Nama", pjNama, "Alamat", pjAlamat);
        row = baris2(halamanKeluarga, row, "Umur", pjUmur, "Pendidikan", pjPendidikan);
        row = baris1(halamanKeluarga, row, "Hubungan", pjHubungan);
        row = judul(halamanKeluarga, row, "B. Riwayat Ibu");
        row = baris2(halamanKeluarga, row, "Nama", ibuNama, "Umur", ibuUmur);
        row = baris2(halamanKeluarga, row, "Pendidikan", ibuPendidikan, "Suku", ibuSuku);
        row = baris2(halamanKeluarga, row, "Agama", ibuAgama, "Penyakit", ibuPenyakit);
        row = baris1(halamanKeluarga, row, "Perkawinan Ke", ibuPerkawinan);
        kartuHalaman.add(scrollHalaman(halamanKeluarga, latar), "KELUARGA");

        JPanel halamanKlinis = formHalaman("3. Riwayat Klinis", utama, latar);
        row = 0;
        row = judul(halamanKlinis, row, "C. Riwayat Penyakit (Untuk Bayi Sakit)");
        row = area(halamanKlinis, row, "Keluhan Utama", taKeluhan);
        row = area(halamanKlinis, row, "Riwayat Kesehatan Sekarang", taRiwSekarang);
        row = area(halamanKlinis, row, "Riwayat Kesehatan Dahulu", taRiwDahulu);
        kartuHalaman.add(scrollHalaman(halamanKlinis, latar), "KLINIS");

        JPanel halamanKehamilan = formHalaman("4. Kehamilan & Persalinan", utama, latar);
        row = 0;
        row = judul(halamanKehamilan, row, "D. Riwayat Kehamilan");
        row = grup(halamanKehamilan, row, "Komplikasi Kehamilan", grpKomplikasi.panel);
        row = area(halamanKehamilan, row, "Komplikasi Lain-lain", taKomplikasiLain);
        row = judul(halamanKehamilan, row, "Riwayat Persalinan Yang Lalu");
        row = tabelPersalinan(halamanKehamilan, row);
        kartuHalaman.add(scrollHalaman(halamanKehamilan, latar), "KEHAMILAN");

        JPanel halamanPsikososial = formHalaman("5. Psikososial Orang Tua", utama, latar);
        row = 0;
        row = judul(halamanPsikososial, row, "F. Riwayat Psikososial Orang Tua");
        row = area(halamanPsikososial, row, "Perkembangan Interpersonal", taPsiko);
        row = grup(halamanPsikososial, row, "Pengasuh", grpPengasuh.panel);
        row = baris2(halamanPsikososial, row, "Dukungan Sibling", cmbSibling, "Dukungan Keluarga Lain", cmbDukKeluarga);
        row = baris2(halamanPsikososial, row, "Sebutkan (Keluarga)", tDukKeluargaSebut, "Budaya Dianut", cmbBudaya);
        row = baris1(halamanPsikososial, row, "Sebutkan (Budaya)", tBudayaSebut);
        kartuHalaman.add(scrollHalaman(halamanPsikososial, latar), "PSIKOSOSIAL");

        JPanel halamanFisik = formHalaman("6. Pemeriksaan Fisik Bayi", utama, latar);
        row = 0;
        row = judul(halamanFisik, row, "Tanda Vital & Antropometri");
        row = baris2(halamanFisik, row, "APGAR Score *", tApgar, "Score Down", tDown);
        row = baris2(halamanFisik, row, "Suhu *", tSuhu, "RR *", tRR);
        row = baris2(halamanFisik, row, "Nadi *", tNadi, "Tingkat Kesadaran", tKesadaran);
        row = baris2(halamanFisik, row, "BB (gr) *", tBB, "PB (cm) *", tPB);
        row = judul(halamanFisik, row, "Observasi Umum");
        row = grup(halamanFisik, row, "Tangisan", grpTangisan.panel);
        row = baris2(halamanFisik, row, "CRT", cmbCrt, "Lingkar Kepala", tLingkarKepala);
        row = grup(halamanFisik, row, "Kulit", grpKulit.panel);
        row = judul(halamanFisik, row, "Kepala & Sistem Sensorik");
        row = grup(halamanFisik, row, "Ubun-ubun", grpUbun.panel);
        row = grup(halamanFisik, row, "Mata & Penglihatan", grpMata.panel);
        row = grup(halamanFisik, row, "Hidung & Penciuman", grpHidung.panel);
        row = grup(halamanFisik, row, "Telinga & Pendengaran", grpTelinga.panel);
        row = grup(halamanFisik, row, "Mulut", grpMulut.panel);
        row = judul(halamanFisik, row, "Pernafasan, Pencernaan & Eliminasi");
        row = baris1(halamanFisik, row, "Frekuensi Nafas (x/m)", tFreqNafas);
        row = grup(halamanFisik, row, "Dada, Pernafasan & Sirkulasi", grpDada.panel);
        row = grup(halamanFisik, row, "Abdomen", grpAbdomen.panel);
        row = baris2(halamanFisik, row, "Jenis Kelamin", cmbJK, "Testis", cmbTestis);
        row = baris2(halamanFisik, row, "Labia Mayora", cmbLabia, "Anus", cmbAnus);
        row = baris2(halamanFisik, row, "BAB (Frekuensi)", tBabFreq, "BAK (Warna/Frekuensi)", tBak);
        row = grup(halamanFisik, row, "BAB", grpBab.panel);
        row = judul(halamanFisik, row, "Motorik, Refleks & Kebutuhan Dasar");
        row = grup(halamanFisik, row, "Ekstremitas Atas", grpEkstrAtas.panel);
        row = grup(halamanFisik, row, "Ekstremitas Bawah", grpEkstrBawah.panel);
        row = baris2(halamanFisik, row, "Reflek Moro", cmbReflekMoro, "Reflek Mengisap", cmbReflekMengisap);
        row = baris2(halamanFisik, row, "Reflek Babinski", tBabinski, "Reflek Rooting", cmbReflekRooting);
        row = grup(halamanFisik, row, "Aktifitas & Istirahat", grpAktifitas.panel);
        row = grup(halamanFisik, row, "Personal Hygiene", grpHygiene.panel);
        row = grup(halamanFisik, row, "Nutrisi", grpNutrisi.panel);
        row = baris2(halamanFisik, row, "Kontak Ibu dengan Bayi", cmbKontakIbu, "Bayi Diharapkan", cmbBayiDiharapkan);
        row = area(halamanFisik, row, "Obat / Therapi", taObat);
        kartuHalaman.add(scrollHalaman(halamanFisik, latar), "FISIK");

        JPanel halamanPulang = formHalaman("7. Pulang & Cap Kaki", utama, latar);
        row = 0;
        row = judul(halamanPulang, row, "Cap Telapak Kaki Bayi");
        row = capKaki(halamanPulang, row);
        row = judul(halamanPulang, row, "Kriteria Discharge Planning & Perencanaan Pulang");
        row = grup(halamanPulang, row, "Kriteria", panelHambatan());
        row = baris2(halamanPulang, row, "Pasien Tinggal Dengan", cmbTinggal, "Sebutkan", tTinggalSebut);
        row = baris2(halamanPulang, row, "Keluarga Perokok", cmbPerokok, "Sebutkan", tPerokokSebut);
        row = grup(halamanPulang, row, "Kondisi Rumah", grpKondisiRumah.panel);
        row = area(halamanPulang, row, "Masalah Keperawatan", taMasalah);
        row = area(halamanPulang, row, "Rencana Keperawatan", taRencana);
        kartuHalaman.add(scrollHalaman(halamanPulang, latar), "PULANG");

        JPanel halamanVerifikasi = formHalaman("8. Verifikasi", utama, latar);
        row = 0;
        row = judul(halamanVerifikasi, row, "Tanda Tangan & Penanggung Jawab Asesmen");
        row = baris1(halamanVerifikasi, row, "Tanggal / Jam", dtpTtd);
        row = baris2(halamanVerifikasi, row, "Perawat Pengkaji *",
                gabungBtn(KdPetugas, NmPetugas, null), "Dokter PJ",
                gabungBtn(KdDokter, NmDokter, BtnDokter));
        kartuHalaman.add(scrollHalaman(halamanVerifikasi, latar), "VERIFIKASI");

        JPanel navigasi = new JPanel();
        navigasi.setBackground(Color.WHITE);
        navigasi.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, garis));
        navigasi.setPreferredSize(new Dimension(225, 100));
        navigasi.setLayout(new BoxLayout(navigasi, BoxLayout.Y_AXIS));
        JLabel judulNavigasi = new JLabel("BAGIAN ASESMEN");
        judulNavigasi.setFont(new Font("Tahoma", Font.BOLD, 11));
        judulNavigasi.setForeground(new Color(80, 95, 105));
        judulNavigasi.setBorder(new EmptyBorder(15, 16, 9, 8));
        judulNavigasi.setAlignmentX(Component.LEFT_ALIGNMENT);
        navigasi.add(judulNavigasi);

        String[] namaMenu = {
            "1  Informasi Asesmen", "2  Keluarga & Riwayat Ibu",
            "3  Riwayat Klinis", "4  Kehamilan & Persalinan",
            "5  Psikososial Orang Tua", "6  Pemeriksaan Fisik",
            "7  Pulang & Cap Kaki", "8  Verifikasi"
        };
        String[] kunciMenu = {
            "INFORMASI", "KELUARGA", "KLINIS", "KEHAMILAN",
            "PSIKOSOSIAL", "FISIK", "PULANG", "VERIFIKASI"
        };
        JButton[] tombolMenu = new JButton[namaMenu.length];
        for (int i = 0; i < namaMenu.length; i++) {
            final int indeks = i;
            JButton tombol = new JButton(namaMenu[i]);
            tombolMenu[i] = tombol;
            tombol.setHorizontalAlignment(SwingConstants.LEFT);
            tombol.setFont(new Font("Tahoma", i == 0 ? Font.BOLD : Font.PLAIN, 11));
            tombol.setForeground(i == 0 ? utama : new Color(61, 76, 86));
            tombol.setBackground(i == 0 ? utamaMuda : Color.WHITE);
            tombol.setBorder(new EmptyBorder(9, 16, 9, 7));
            tombol.setFocusPainted(false);
            tombol.setMaximumSize(new Dimension(225, 38));
            tombol.setAlignmentX(Component.LEFT_ALIGNMENT);
            tombol.addActionListener(e -> {
                tataHalaman.show(kartuHalaman, kunciMenu[indeks]);
                for (int m = 0; m < tombolMenu.length; m++) {
                    boolean aktif = m == indeks;
                    tombolMenu[m].setBackground(aktif ? utamaMuda : Color.WHITE);
                    tombolMenu[m].setForeground(aktif ? utama : new Color(61, 76, 86));
                    tombolMenu[m].setFont(new Font("Tahoma", aktif ? Font.BOLD : Font.PLAIN, 11));
                }
            });
            navigasi.add(tombol);
            navigasi.add(Box.createVerticalStrut(2));
        }
        navigasi.add(Box.createVerticalGlue());
        JLabel wajib = new JLabel("<html><span style='color:#D32F2F'>*</span> Wajib diisi</html>");
        wajib.setFont(new Font("Tahoma", Font.PLAIN, 10));
        wajib.setForeground(new Color(85, 99, 108));
        wajib.setBorder(new EmptyBorder(8, 16, 14, 8));
        wajib.setAlignmentX(Component.LEFT_ALIGNMENT);
        navigasi.add(wajib);

        JPanel tengah = new JPanel(new BorderLayout());
        tengah.setBackground(latar);
        tengah.add(navigasi, BorderLayout.WEST);
        tengah.add(kartuHalaman, BorderLayout.CENTER);
        getContentPane().add(tengah, BorderLayout.CENTER);

        BtnBaru.setText("Baru");
        BtnSimpan.setText("Simpan");
        BtnHapus.setText("Hapus");
        BtnKeluar.setText("Keluar");
        BtnDokter.setText("...");
        BtnDokter.setPreferredSize(new Dimension(34, 23));
        BtnCetak.setText("Cetak");
        BtnBaru.addActionListener(e -> emptTeks());
        BtnSimpan.addActionListener(e -> simpan());
        BtnHapus.addActionListener(e -> hapus());
        BtnCetak.addActionListener(e -> cetak());
        BtnKeluar.addActionListener(e -> dispose());
        BtnDokter.addActionListener(e -> {
            dokter.emptTeks();
            dokter.isCek();
            dokter.setSize(900, 540);
            dokter.setLocationRelativeTo(this);
            dokter.setVisible(true);
        });
        BtnSimpan.setText("Simpan Data");
        BtnHapus.setText("Hapus Data");
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 8));
        bawah.setBackground(Color.WHITE);
        bawah.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, garis));
        bawah.add(BtnHapus);
        bawah.add(BtnBaru);
        bawah.add(BtnCetak);
        bawah.add(BtnKeluar);
        bawah.add(BtnSimpan);
        getContentPane().add(bawah, BorderLayout.SOUTH);

        dtpTanggal.setDate(new Date());
        dtpTtd.setDate(new Date());
    }

    // ====================== Helpers UI ======================
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

    private JPanel ringkasan(String label, Component komponen) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(0, 8, 0, 8));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel judul = new JLabel(label);
        judul.setFont(new Font("Tahoma", Font.PLAIN, 10));
        judul.setForeground(label.contains("*")
                ? new Color(198, 40, 40) : new Color(82, 97, 108));
        judul.setAlignmentX(Component.LEFT_ALIGNMENT);
        komponen.setPreferredSize(new Dimension(145, 25));
        komponen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        komponen.setBackground(new Color(247, 249, 250));
        panel.add(judul);
        panel.add(Box.createVerticalStrut(3));
        panel.add(komponen);
        return panel;
    }

    private JPanel formHalaman(String nama, Color utama, Color latar) {
        JPanel pembungkus = new JPanel(new BorderLayout());
        pembungkus.setBackground(latar);
        pembungkus.setBorder(new EmptyBorder(13, 16, 16, 16));
        JLabel judulHalaman = new JLabel(nama);
        judulHalaman.setFont(new Font("Tahoma", Font.BOLD, 16));
        judulHalaman.setForeground(utama);
        judulHalaman.setBorder(new EmptyBorder(0, 3, 10, 0));
        pembungkus.add(judulHalaman, BorderLayout.NORTH);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createLineBorder(new Color(214, 224, 230)));
        pembungkus.add(form, BorderLayout.CENTER);
        return new HalamanPanel(pembungkus, form);
    }

    private JScrollPane scrollHalaman(JPanel halaman, Color latar) {
        JScrollPane scroll = new JScrollPane(halaman);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(25);
        scroll.getVerticalScrollBar().setBlockIncrement(120);
        scroll.getViewport().setBackground(latar);
        return scroll;
    }

    /**
     * Pembungkus transparan: pemanggil tetap menambahkan field dengan
     * GridBagLayout, sementara judul halaman berada di luar form.
     */
    private static final class HalamanPanel extends JPanel {
        private final JPanel form;

        HalamanPanel(JPanel pembungkus, JPanel form) {
            super(new BorderLayout());
            this.form = form;
            setOpaque(false);
            add(pembungkus, BorderLayout.CENTER);
        }

        @Override
        protected void addImpl(Component comp, Object constraints, int index) {
            if (form != null && comp != form && !(getLayout() instanceof BorderLayout
                    && BorderLayout.CENTER.equals(constraints))) {
                form.add(comp, constraints, index);
            } else {
                super.addImpl(comp, constraints, index);
            }
        }
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
        l.setBackground(new Color(230, 247, 248));
        l.setForeground(new Color(0, 120, 130));
        l.setFont(new Font("Tahoma", Font.BOLD, 12));
        l.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));
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
        sc.setPreferredSize(new Dimension(400, 56));
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
        String teks = t.contains("*")
                ? "<html>" + t.replace("*", "<font color='#D32F2F'>*</font>") + " :</html>"
                : t + " :";
        JLabel l = new JLabel(teks);
        l.setFont(new Font("Tahoma", Font.PLAIN, 11));
        l.setForeground(new Color(48, 63, 74));
        return l;
    }

    private void siz(Component c) {
        if (c instanceof widget.TextBox || c instanceof widget.ComboBox || c instanceof widget.Tanggal || c instanceof JPanel) {
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

    private JPanel panelHambatan() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        pnl.setOpaque(false);
        pnl.add(cekHambatan);
        return pnl;
    }

    private int tabelPersalinan(JPanel p, int row) {
        tbPersalinan.setModel(modePersalinan);
        JScrollPane sc = new JScrollPane(tbPersalinan);
        sc.setPreferredSize(new Dimension(600, 90));
        sc.setWheelScrollingEnabled(false);
        p.add(sc, gc(0, row, 4, 1.0));
        row++;
        JButton bt = new JButton("Tambah Baris");
        JButton bh = new JButton("Hapus Baris");
        bt.addActionListener(e -> modePersalinan.addRow(new Object[]{"", "", "", "", "", "", ""}));
        bh.addActionListener(e -> {
            int r = tbPersalinan.getSelectedRow();
            if (r != -1) { modePersalinan.removeRow(r); }
        });
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnl.setOpaque(false);
        pnl.add(bt);
        pnl.add(bh);
        p.add(pnl, gc(0, row, 4, 1.0));
        return row + 1;
    }

    private int capKaki(JPanel p, int row) {
        JButton b1 = new JButton("Upload Cap Kaki 1");
        JButton b2 = new JButton("Upload Cap Kaki 2");
        b1.addActionListener(e -> { capKaki1 = pilihGambar(); lblCap1.setText(capKaki1 == null ? "(belum ada)" : "(terisi " + capKaki1.length + " byte)"); });
        b2.addActionListener(e -> { capKaki2 = pilihGambar(); lblCap2.setText(capKaki2 == null ? "(belum ada)" : "(terisi " + capKaki2.length + " byte)"); });
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnl.setOpaque(false);
        pnl.add(b1); pnl.add(lblCap1);
        pnl.add(new JLabel("    "));
        pnl.add(b2); pnl.add(lblCap2);
        p.add(pnl, gc(0, row, 4, 1.0));
        return row + 1;
    }

    private byte[] pilihGambar() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                return java.nio.file.Files.readAllBytes(fc.getSelectedFile().toPath());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal membaca gambar.\n" + e.getMessage());
            }
        }
        return null;
    }

    // ====================== Entry points (dipanggil dari DlgKamarInap) ======================
    public void isCek() {
        boolean bisa = akses.getpenilaian_awal_keperawatan_ranap();
        BtnSimpan.setEnabled(bisa);
        BtnHapus.setEnabled(bisa);
        KdPetugas.setText(akses.getkode());
        NmPetugas.setText(Sequel.cariIsi("select nama from petugas where nip=?", akses.getkode()));
    }

    public void emptTeks() {
        for (widget.TextBox t : new widget.TextBox[]{tRuang, tLantai, tKelas, pjNama, pjAlamat, pjUmur, pjPendidikan, pjHubungan,
            ibuNama, ibuUmur, ibuPendidikan, ibuSuku, ibuAgama, ibuPenyakit, ibuPerkawinan, tDukKeluargaSebut, tBudayaSebut,
            tApgar, tDown, tSuhu, tRR, tNadi, tKesadaran, tBB, tPB, tLingkarKepala, tFreqNafas, tBabFreq, tBak, tBabinski,
            tTinggalSebut, tPerokokSebut, KdDokter, NmDokter}) {
            t.setText("");
        }
        for (widget.TextArea a : new widget.TextArea[]{taKeluhan, taRiwSekarang, taRiwDahulu, taKomplikasiLain, taPsiko, taObat, taMasalah, taRencana}) {
            a.setText("");
        }
        for (Grup g : new Grup[]{grpKomplikasi, grpPengasuh, grpKulit, grpUbun, grpMata, grpHidung, grpTelinga, grpMulut,
            grpDada, grpAbdomen, grpBab, grpEkstrAtas, grpEkstrBawah, grpAktifitas, grpHygiene, grpNutrisi, grpKondisiRumah, grpTangisan}) {
            g.clear();
        }
        cmbGelang.setSelectedIndex(0);
        cmbSibling.setSelectedIndex(0);
        cmbDukKeluarga.setSelectedIndex(0);
        cmbBudaya.setSelectedIndex(0);
        cmbCrt.setSelectedIndex(0);
        cmbJK.setSelectedIndex(0);
        cmbTestis.setSelectedIndex(0);
        cmbLabia.setSelectedIndex(0);
        cmbAnus.setSelectedIndex(0);
        cmbReflekMoro.setSelectedIndex(0);
        cmbReflekMengisap.setSelectedIndex(0);
        cmbReflekRooting.setSelectedIndex(0);
        cmbKontakIbu.setSelectedIndex(0);
        cmbBayiDiharapkan.setSelectedIndex(0);
        cmbTinggal.setSelectedIndex(0);
        cmbPerokok.setSelectedIndex(0);
        cekHambatan.setSelected(false);
        Valid.tabelKosong(modePersalinan);
        capKaki1 = null; capKaki2 = null;
        lblCap1.setText("(belum ada)"); lblCap2.setText("(belum ada)");
        dtpTanggal.setDate(new Date());
        dtpTtd.setDate(new Date());
    }

    public void setNoRm(String norwt, Date tgl2, String carabayar, String norm) {
        emptTeks();
        TNoRw.setText(norwt);
        TNoRM.setText(norm);
        TCaraBayar.setText(carabayar);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select p.nm_pasien,p.no_rkm_medis,p.jk,p.tgl_lahir,p.alamat,"
                + "ifnull(poliklinik.nm_poli,'') as unit,"
                + "ifnull(rp.p_jawab,'') as p_jawab,ifnull(rp.almt_pj,'') as almt_pj,ifnull(rp.hubunganpj,'') as hubunganpj,"
                + "ifnull(pj.png_jawab,'') as carabayar "
                + "from reg_periksa rp inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis "
                + "left join poliklinik on rp.kd_poli=poliklinik.kd_poli "
                + "left join penjab pj on rp.kd_pj=pj.kd_pj where rp.no_rawat=?")) {
            ps.setString(1, norwt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TPasien.setText(rs.getString("nm_pasien"));
                    if (norm == null || norm.trim().equals("")) { TNoRM.setText(rs.getString("no_rkm_medis")); }
                    TJK.setText("L".equalsIgnoreCase(rs.getString("jk")) ? "Laki-Laki" : "Perempuan");
                    TTglLahir.setText(rs.getString("tgl_lahir"));
                    TAlamat.setText(rs.getString("alamat"));
                    TUnit.setText(rs.getString("unit"));
                    if (rs.getString("carabayar") != null && !rs.getString("carabayar").trim().equals("")) {
                        TCaraBayar.setText(rs.getString("carabayar"));
                    }
                    // Identitas penanggung jawab dari data pasien (kosong bila tidak ada)
                    pjNama.setText(rs.getString("p_jawab"));
                    pjAlamat.setText(rs.getString("almt_pj"));
                    pjHubungan.setText(rs.getString("hubunganpj"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif identitas bayi : " + e);
        }
        // Ruang dari kamar_inap; Lantai & Kelas default
        String ruang = Sequel.cariIsi("select ifnull(bangsal.nm_bangsal,'') from kamar_inap "
                + "inner join kamar on kamar.kd_kamar=kamar_inap.kd_kamar "
                + "inner join bangsal on bangsal.kd_bangsal=kamar.kd_bangsal "
                + "where kamar_inap.no_rawat=? order by kamar_inap.tgl_masuk desc limit 1", norwt);
        tRuang.setText(ruang);
        tLantai.setText("4");
        tKelas.setText("Perinatologi");
        // Riwayat ibu otomatis bila RM bayi sudah digabung ke ranap ibu (ranap_gabung)
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select p.nm_pasien,p.umur,ifnull(p.agama,'') as agama,p.pnd as pnd,ifnull(s.nama_suku_bangsa,'') as suku "
                + "from ranap_gabung rg inner join reg_periksa reg on reg.no_rawat=rg.no_rawat "
                + "inner join pasien p on reg.no_rkm_medis=p.no_rkm_medis "
                + "left join suku_bangsa s on p.suku_bangsa=s.id where rg.no_rawat2=?")) {
            ps.setString(1, norwt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ibuNama.setText(rs.getString("nm_pasien"));
                    ibuUmur.setText(rs.getString("umur"));
                    ibuAgama.setText(rs.getString("agama"));
                    ibuPendidikan.setText(rs.getString("pnd"));
                    ibuSuku.setText(rs.getString("suku"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif riwayat ibu bayi : " + e);
        }
        muat();
    }

    // ====================== Simpan / Muat / Hapus ======================
    private static final String[] KOLOM = {
        "no_rawat", "tanggal", "jam", "ruang", "lantai", "kelas", "gelang_identitas",
        "pj_nama", "pj_alamat", "pj_umur", "pj_pendidikan", "pj_hubungan",
        "ibu_nama", "ibu_umur", "ibu_pendidikan", "ibu_suku", "ibu_agama", "ibu_penyakit", "ibu_perkawinan_ke",
        "keluhan_utama", "riwayat_sekarang", "riwayat_dahulu", "komplikasi_kehamilan", "komplikasi_lain",
        "psiko_perkembangan", "pengasuh", "dukungan_sibling", "dukungan_keluarga", "budaya",
        "apgar", "down_score", "suhu", "rr", "nadi", "kesadaran", "bb", "pb", "tangisan", "crt", "kulit",
        "lingkar_kepala", "ubun_ubun", "mata", "hidung", "telinga", "mulut",
        "frekuensi_nafas", "dada_sirkulasi", "abdomen", "jenis_kelamin", "testis", "labia_mayora", "anus",
        "bab", "bak", "ekstremitas_atas", "ekstremitas_bawah",
        "reflek_moro", "reflek_mengisap", "reflek_babinski", "reflek_rooting",
        "aktifitas", "hygiene", "nutrisi", "kontak_ibu", "bayi_diharapkan", "obat_therapi",
        "hambatan_mobilisasi", "pasien_tinggal", "keluarga_perokok", "kondisi_rumah",
        "masalah_keperawatan", "rencana_keperawatan", "tgl_ttd", "jam_ttd", "nik", "kd_dokter"
    };

    private void simpan() {
        if (TNoRw.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        String[][] wajib = {
            {tApgar.getText(), "APGAR Score"}, {tSuhu.getText(), "Suhu"}, {tRR.getText(), "RR"},
            {tNadi.getText(), "Nadi"}, {tBB.getText(), "BB"}, {tPB.getText(), "PB"},
            {KdPetugas.getText(), "Perawat Pengkaji"}
        };
        for (String[] w : wajib) {
            if (w[0] == null || w[0].trim().equals("")) {
                JOptionPane.showMessageDialog(this, "Field wajib (*) belum diisi : " + w[1]);
                return;
            }
        }
        String tgl = Valid.SetTgl(dtpTanggal.getSelectedItem() + "");
        String jam = dtpTanggal.getSelectedItem().toString().substring(11, 19);
        String tglTtd = Valid.SetTgl(dtpTtd.getSelectedItem() + "");
        String jamTtd = dtpTtd.getSelectedItem().toString().substring(11, 19);
        String[] nilai = {
            TNoRw.getText(), tgl, jam, tRuang.getText(), tLantai.getText(), tKelas.getText(), s(cmbGelang),
            pjNama.getText(), pjAlamat.getText(), pjUmur.getText(), pjPendidikan.getText(), pjHubungan.getText(),
            ibuNama.getText(), ibuUmur.getText(), ibuPendidikan.getText(), ibuSuku.getText(), ibuAgama.getText(), ibuPenyakit.getText(), ibuPerkawinan.getText(),
            taKeluhan.getText(), taRiwSekarang.getText(), taRiwDahulu.getText(), grpKomplikasi.get(), taKomplikasiLain.getText(),
            taPsiko.getText(), grpPengasuh.get(), s(cmbSibling), gabung(cmbDukKeluarga, tDukKeluargaSebut), gabung(cmbBudaya, tBudayaSebut),
            tApgar.getText(), tDown.getText(), tSuhu.getText(), tRR.getText(), tNadi.getText(), tKesadaran.getText(), tBB.getText(), tPB.getText(), grpTangisan.get(), s(cmbCrt), grpKulit.get(),
            tLingkarKepala.getText(), grpUbun.get(), grpMata.get(), grpHidung.get(), grpTelinga.get(), grpMulut.get(),
            tFreqNafas.getText(), grpDada.get(), grpAbdomen.get(), s(cmbJK), s(cmbTestis), s(cmbLabia), s(cmbAnus),
            (tBabFreq.getText() + (grpBab.get().equals("") ? "" : " - " + grpBab.get())), tBak.getText(), grpEkstrAtas.get(), grpEkstrBawah.get(),
            s(cmbReflekMoro), s(cmbReflekMengisap), tBabinski.getText(), s(cmbReflekRooting),
            grpAktifitas.get(), grpHygiene.get(), grpNutrisi.get(), s(cmbKontakIbu), s(cmbBayiDiharapkan), taObat.getText(),
            (cekHambatan.isSelected() ? "Ya" : ""), gabung(cmbTinggal, tTinggalSebut), gabung(cmbPerokok, tPerokokSebut), grpKondisiRumah.get(),
            taMasalah.getText(), taRencana.getText(), tglTtd, jamTtd, KdPetugas.getText(), KdDokter.getText()
        };
        StringBuilder cols = new StringBuilder();
        StringBuilder qm = new StringBuilder();
        for (String k : KOLOM) {
            if (cols.length() > 0) { cols.append(","); qm.append(","); }
            cols.append(k);
            qm.append("?");
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "replace into asesmen_keperawatan_bayi (" + cols + ") values (" + qm + ")")) {
            for (int i = 0; i < nilai.length; i++) {
                ps.setString(i + 1, nilai[i]);
            }
            ps.executeUpdate();
            simpanPersalinan();
            simpanGambar();
            JOptionPane.showMessageDialog(this, "Asesmen keperawatan bayi tersimpan.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan.\n" + e.getMessage());
        }
    }

    private void simpanPersalinan() {
        try (PreparedStatement del = koneksi.prepareStatement("delete from asesmen_keperawatan_bayi_persalinan where no_rawat=?")) {
            del.setString(1, TNoRw.getText());
            del.executeUpdate();
        } catch (Exception e) {
            System.out.println("Notif del persalinan : " + e);
        }
        for (int i = 0; i < modePersalinan.getRowCount(); i++) {
            try (PreparedStatement ins = koneksi.prepareStatement(
                    "insert into asesmen_keperawatan_bayi_persalinan "
                    + "(no_rawat,urut,bb_lahir,jenis_kelamin,jenis_persalinan,komplikasi,kondisi_saat_ini,riwayat_imunisasi,tahun_lahir) "
                    + "values (?,?,?,?,?,?,?,?,?)")) {
                ins.setString(1, TNoRw.getText());
                ins.setInt(2, i + 1);
                for (int c = 0; c < 7; c++) {
                    Object o = modePersalinan.getValueAt(i, c);
                    ins.setString(c + 3, o == null ? "" : o.toString());
                }
                ins.executeUpdate();
            } catch (Exception e) {
                System.out.println("Notif ins persalinan : " + e);
            }
        }
    }

    private void simpanGambar() {
        simpanSatuGambar("kaki1", capKaki1);
        simpanSatuGambar("kaki2", capKaki2);
    }

    private void simpanSatuGambar(String posisi, byte[] data) {
        if (data == null) { return; }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "replace into asesmen_keperawatan_bayi_gambar (no_rawat,posisi,photo) values (?,?,?)")) {
            ps.setString(1, TNoRw.getText());
            ps.setString(2, posisi);
            ps.setBytes(3, data);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Notif simpan gambar : " + e);
        }
    }

    private void muat() {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select * from asesmen_keperawatan_bayi where no_rawat=?")) {
            ps.setString(1, TNoRw.getText());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    setTgl(dtpTanggal, rs.getString("tanggal"), rs.getString("jam"));
                    tRuang.setText(g(rs, "ruang")); tLantai.setText(g(rs, "lantai")); tKelas.setText(g(rs, "kelas"));
                    cmbGelang.setSelectedItem(g(rs, "gelang_identitas"));
                    pjNama.setText(g(rs, "pj_nama")); pjAlamat.setText(g(rs, "pj_alamat")); pjUmur.setText(g(rs, "pj_umur"));
                    pjPendidikan.setText(g(rs, "pj_pendidikan")); pjHubungan.setText(g(rs, "pj_hubungan"));
                    ibuNama.setText(g(rs, "ibu_nama")); ibuUmur.setText(g(rs, "ibu_umur")); ibuPendidikan.setText(g(rs, "ibu_pendidikan"));
                    ibuSuku.setText(g(rs, "ibu_suku")); ibuAgama.setText(g(rs, "ibu_agama")); ibuPenyakit.setText(g(rs, "ibu_penyakit")); ibuPerkawinan.setText(g(rs, "ibu_perkawinan_ke"));
                    taKeluhan.setText(g(rs, "keluhan_utama")); taRiwSekarang.setText(g(rs, "riwayat_sekarang")); taRiwDahulu.setText(g(rs, "riwayat_dahulu"));
                    grpKomplikasi.set(g(rs, "komplikasi_kehamilan")); taKomplikasiLain.setText(g(rs, "komplikasi_lain"));
                    taPsiko.setText(g(rs, "psiko_perkembangan")); grpPengasuh.set(g(rs, "pengasuh")); cmbSibling.setSelectedItem(g(rs, "dukungan_sibling"));
                    pisah(g(rs, "dukungan_keluarga"), cmbDukKeluarga, tDukKeluargaSebut);
                    pisah(g(rs, "budaya"), cmbBudaya, tBudayaSebut);
                    tApgar.setText(g(rs, "apgar")); tDown.setText(g(rs, "down_score")); tSuhu.setText(g(rs, "suhu")); tRR.setText(g(rs, "rr")); tNadi.setText(g(rs, "nadi"));
                    tKesadaran.setText(g(rs, "kesadaran")); tBB.setText(g(rs, "bb")); tPB.setText(g(rs, "pb")); grpTangisan.set(g(rs, "tangisan")); cmbCrt.setSelectedItem(g(rs, "crt")); grpKulit.set(g(rs, "kulit"));
                    tLingkarKepala.setText(g(rs, "lingkar_kepala")); grpUbun.set(g(rs, "ubun_ubun")); grpMata.set(g(rs, "mata")); grpHidung.set(g(rs, "hidung")); grpTelinga.set(g(rs, "telinga")); grpMulut.set(g(rs, "mulut"));
                    tFreqNafas.setText(g(rs, "frekuensi_nafas")); grpDada.set(g(rs, "dada_sirkulasi")); grpAbdomen.set(g(rs, "abdomen"));
                    cmbJK.setSelectedItem(g(rs, "jenis_kelamin")); cmbTestis.setSelectedItem(g(rs, "testis")); cmbLabia.setSelectedItem(g(rs, "labia_mayora")); cmbAnus.setSelectedItem(g(rs, "anus"));
                    tBak.setText(g(rs, "bak")); grpEkstrAtas.set(g(rs, "ekstremitas_atas")); grpEkstrBawah.set(g(rs, "ekstremitas_bawah"));
                    cmbReflekMoro.setSelectedItem(g(rs, "reflek_moro")); cmbReflekMengisap.setSelectedItem(g(rs, "reflek_mengisap")); tBabinski.setText(g(rs, "reflek_babinski")); cmbReflekRooting.setSelectedItem(g(rs, "reflek_rooting"));
                    grpAktifitas.set(g(rs, "aktifitas")); grpHygiene.set(g(rs, "hygiene")); grpNutrisi.set(g(rs, "nutrisi"));
                    cmbKontakIbu.setSelectedItem(g(rs, "kontak_ibu")); cmbBayiDiharapkan.setSelectedItem(g(rs, "bayi_diharapkan")); taObat.setText(g(rs, "obat_therapi"));
                    cekHambatan.setSelected("Ya".equalsIgnoreCase(g(rs, "hambatan_mobilisasi")));
                    pisah(g(rs, "pasien_tinggal"), cmbTinggal, tTinggalSebut);
                    pisah(g(rs, "keluarga_perokok"), cmbPerokok, tPerokokSebut);
                    grpKondisiRumah.set(g(rs, "kondisi_rumah"));
                    taMasalah.setText(g(rs, "masalah_keperawatan")); taRencana.setText(g(rs, "rencana_keperawatan"));
                    setTgl(dtpTtd, rs.getString("tgl_ttd"), rs.getString("jam_ttd"));
                    if (!g(rs, "kd_dokter").equals("")) {
                        KdDokter.setText(g(rs, "kd_dokter"));
                        NmDokter.setText(Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", g(rs, "kd_dokter")));
                    }
                    // Pelaksana asesmen yang TERSIMPAN (bukan user yang sedang login) -- penting saat data dibuka/dicetak oleh user lain.
                    if (!g(rs, "nik").equals("")) {
                        KdPetugas.setText(g(rs, "nik"));
                        NmPetugas.setText(Sequel.cariIsi("select nama from petugas where nip=?", g(rs, "nik")));
                    }
                    String bab = g(rs, "bab");
                    if (bab.contains(" - ")) {
                        tBabFreq.setText(bab.substring(0, bab.indexOf(" - ")));
                        grpBab.set(bab.substring(bab.indexOf(" - ") + 3));
                    } else {
                        tBabFreq.setText(bab);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat bayi : " + e);
        }
        muatPersalinan();
        muatGambar();
    }

    private void muatPersalinan() {
        Valid.tabelKosong(modePersalinan);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select bb_lahir,jenis_kelamin,jenis_persalinan,komplikasi,kondisi_saat_ini,riwayat_imunisasi,tahun_lahir "
                + "from asesmen_keperawatan_bayi_persalinan where no_rawat=? order by urut")) {
            ps.setString(1, TNoRw.getText());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modePersalinan.addRow(new Object[]{
                        rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7)
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat persalinan : " + e);
        }
    }

    private void muatGambar() {
        capKaki1 = ambilGambar("kaki1");
        capKaki2 = ambilGambar("kaki2");
        lblCap1.setText(capKaki1 == null ? "(belum ada)" : "(tersimpan " + capKaki1.length + " byte)");
        lblCap2.setText(capKaki2 == null ? "(belum ada)" : "(tersimpan " + capKaki2.length + " byte)");
    }

    private byte[] ambilGambar(String posisi) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select photo from asesmen_keperawatan_bayi_gambar where no_rawat=? and posisi=?")) {
            ps.setString(1, TNoRw.getText());
            ps.setString(2, posisi);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { return rs.getBytes("photo"); }
            }
        } catch (Exception e) {
            System.out.println("Notif ambil gambar : " + e);
        }
        return null;
    }

    private void cetak() {
        if (TNoRw.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        List<Map<String, ?>> rows = CetakAsesmen.mulai();
        CetakAsesmen.h(rows, "Asesmen Keperawatan Bayi");
        CetakAsesmen.r2(rows, "Ruang / Lantai", tRuang.getText() + " / " + tLantai.getText(), "Kelas", tKelas.getText());
        CetakAsesmen.r(rows, "Gelang Identitas", s(cmbGelang));
        CetakAsesmen.r(rows, "Tanggal / Jam", dtpTanggal.getSelectedItem() + "");

        CetakAsesmen.h(rows, "A. Identitas Penanggung Jawab");
        CetakAsesmen.r2(rows, "Nama", pjNama.getText(), "Hubungan", pjHubungan.getText());
        CetakAsesmen.r(rows, "Alamat", pjAlamat.getText());
        CetakAsesmen.r2(rows, "Umur", pjUmur.getText(), "Pendidikan", pjPendidikan.getText());

        CetakAsesmen.h(rows, "B. Riwayat Ibu");
        CetakAsesmen.r2(rows, "Nama", ibuNama.getText(), "Umur", ibuUmur.getText());
        CetakAsesmen.r2(rows, "Pendidikan", ibuPendidikan.getText(), "Suku", ibuSuku.getText());
        CetakAsesmen.r2(rows, "Agama", ibuAgama.getText(), "Penyakit", ibuPenyakit.getText());
        CetakAsesmen.r(rows, "Perkawinan Ke", ibuPerkawinan.getText());

        CetakAsesmen.h(rows, "C. Riwayat Penyakit");
        CetakAsesmen.r(rows, "Keluhan Utama", taKeluhan.getText());
        CetakAsesmen.r(rows, "Riwayat Kesehatan Sekarang", taRiwSekarang.getText());
        CetakAsesmen.r(rows, "Riwayat Kesehatan Dahulu", taRiwDahulu.getText());

        CetakAsesmen.h(rows, "D. Riwayat Kehamilan & Persalinan Yang Lalu");
        CetakAsesmen.r(rows, "Komplikasi Kehamilan", grpKomplikasi);
        CetakAsesmen.r(rows, "Lain-lain", taKomplikasiLain.getText());
        if (modePersalinan.getRowCount() == 0) {
            CetakAsesmen.r(rows, "Riwayat Persalinan Yang Lalu", "");
        }
        for (int i = 0; i < modePersalinan.getRowCount(); i++) {
            StringBuilder rowTeks = new StringBuilder();
            for (int c = 0; c < 7; c++) {
                Object o = modePersalinan.getValueAt(i, c);
                if (c > 0) { rowTeks.append(" | "); }
                rowTeks.append(o == null ? "" : o.toString());
            }
            CetakAsesmen.r(rows, "Persalinan Lalu " + (i + 1), rowTeks.toString());
        }

        CetakAsesmen.h(rows, "F. Riwayat Psikososial Orang Tua");
        CetakAsesmen.r(rows, "Perkembangan Interpersonal", taPsiko.getText());
        CetakAsesmen.r(rows, "Pengasuh", grpPengasuh);
        CetakAsesmen.r2(rows, "Dukungan Sibling", s(cmbSibling), "Dukungan Keluarga", gabung(cmbDukKeluarga, tDukKeluargaSebut));
        CetakAsesmen.r(rows, "Budaya Dianut", gabung(cmbBudaya, tBudayaSebut));

        CetakAsesmen.h(rows, "G. Pemeriksaan Fisik");
        CetakAsesmen.r2(rows, "APGAR Score", tApgar.getText(), "Score Down", tDown.getText());
        CetakAsesmen.r(rows, "Suhu / RR / Nadi", tSuhu.getText() + " / " + tRR.getText() + " / " + tNadi.getText());
        CetakAsesmen.r(rows, "Tingkat Kesadaran", tKesadaran.getText());
        CetakAsesmen.r2(rows, "BB", tBB.getText(), "PB", tPB.getText());
        CetakAsesmen.r2(rows, "Tangisan", grpTangisan.get(), "CRT", s(cmbCrt));
        CetakAsesmen.r(rows, "Kulit", grpKulit);
        CetakAsesmen.r2(rows, "Lingkar Kepala", tLingkarKepala.getText(), "Ubun-ubun", grpUbun.get());
        CetakAsesmen.r(rows, "Mata", grpMata);
        CetakAsesmen.r(rows, "Hidung", grpHidung);
        CetakAsesmen.r(rows, "Telinga", grpTelinga);
        CetakAsesmen.r(rows, "Mulut", grpMulut);
        CetakAsesmen.r(rows, "Frekuensi Nafas", tFreqNafas.getText());
        CetakAsesmen.r(rows, "Dada & Sirkulasi", grpDada);
        CetakAsesmen.r(rows, "Abdomen", grpAbdomen);
        CetakAsesmen.r(rows, "Jenis Kelamin / Testis / Labia / Anus", s(cmbJK) + " / " + s(cmbTestis) + " / " + s(cmbLabia) + " / " + s(cmbAnus));
        CetakAsesmen.r(rows, "BAB", tBabFreq.getText() + " " + grpBab.get());
        CetakAsesmen.r(rows, "BAK", tBak.getText());
        CetakAsesmen.r(rows, "Ekstremitas Atas", grpEkstrAtas);
        CetakAsesmen.r(rows, "Ekstremitas Bawah", grpEkstrBawah);
        CetakAsesmen.r(rows, "Reflek Moro / Mengisap / Rooting", s(cmbReflekMoro) + " / " + s(cmbReflekMengisap) + " / " + s(cmbReflekRooting));
        CetakAsesmen.r(rows, "Reflek Babinski", tBabinski.getText());
        CetakAsesmen.r(rows, "Aktifitas & Istirahat", grpAktifitas);
        CetakAsesmen.r(rows, "Personal Hygiene", grpHygiene);
        CetakAsesmen.r(rows, "Nutrisi", grpNutrisi);
        CetakAsesmen.r2(rows, "Kontak Ibu", s(cmbKontakIbu), "Bayi Diharapkan", s(cmbBayiDiharapkan));
        CetakAsesmen.r(rows, "Obat / Therapi", taObat.getText());

        CetakAsesmen.h(rows, "Discharge Planning & Perencanaan Pulang");
        CetakAsesmen.r(rows, "Kriteria", cekHambatan.isSelected() ? "Memiliki Hambatan Mobilisasi" : "");
        CetakAsesmen.r(rows, "Pasien Tinggal Dengan", gabung(cmbTinggal, tTinggalSebut));
        CetakAsesmen.r(rows, "Keluarga Perokok", gabung(cmbPerokok, tPerokokSebut));
        CetakAsesmen.r(rows, "Kondisi Rumah", grpKondisiRumah);

        CetakAsesmen.h(rows, "Resume");
        CetakAsesmen.r(rows, "Masalah Keperawatan", taMasalah.getText());
        CetakAsesmen.r(rows, "Rencana Keperawatan", taRencana.getText());

        CetakAsesmen.Identitas id = new CetakAsesmen.Identitas();
        id.nama = TPasien.getText();
        id.noRawat = TNoRw.getText();
        id.kelas = tKelas.getText();
        id.nik = Sequel.cariIsi("select no_ktp from pasien where no_rkm_medis=?", TNoRM.getText());
        id.tglMasuk = Sequel.cariIsi("select concat(date_format(tgl_registrasi,'%d-%m-%Y'),' ',jam_reg) "
                + "from reg_periksa where no_rawat=?", TNoRw.getText());
        id.pembayaran = TCaraBayar.getText();
        id.jk = TJK.getText();
        id.noRM = TNoRM.getText();
        id.unit = TUnit.getText();
        id.tglLahir = TTglLahir.getText();
        id.alamat = TAlamat.getText();

        CetakAsesmen.cetak("ASESMEN KEPERAWATAN BAYI", "RM 5b", rows, id,
                dtpTtd.getSelectedItem() + "", "Perawat Pengkaji", KdPetugas.getText(), NmPetugas.getText(),
                "Dokter Penanggung Jawab", NmDokter.getText());
    }

    /** Cetak langsung dari no_rawat tanpa membuka dialog (dipakai dari klik-kanan di layar Riwayat). */
    public static void cetak(String noRawat) {
        if (noRawat == null || noRawat.trim().isEmpty()) {
            return;
        }
        RMPenilaianAwalKeperawatanRanapBayi f = new RMPenilaianAwalKeperawatanRanapBayi(null, false);
        f.isCek();
        f.setNoRm(noRawat.trim(), new Date(), "", null);
        f.cetak();
        f.dispose();
    }

    private void hapus() {
        if (TNoRw.getText().trim().equals("")) { return; }
        if (JOptionPane.showConfirmDialog(this, "Hapus asesmen keperawatan bayi untuk No.Rawat " + TNoRw.getText() + " ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            jalankan("delete from asesmen_keperawatan_bayi where no_rawat=?");
            jalankan("delete from asesmen_keperawatan_bayi_persalinan where no_rawat=?");
            jalankan("delete from asesmen_keperawatan_bayi_gambar where no_rawat=?");
            JOptionPane.showMessageDialog(this, "Data dihapus.");
            String norw = TNoRw.getText(), norm = TNoRM.getText(), cb = TCaraBayar.getText();
            emptTeks();
            TNoRw.setText(norw); TNoRM.setText(norm); TCaraBayar.setText(cb);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus.\n" + e.getMessage());
        }
    }

    private void jalankan(String sql) throws Exception {
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, TNoRw.getText());
            ps.executeUpdate();
        }
    }

    // ====================== util ======================
    private String s(widget.ComboBox c) {
        Object o = c.getSelectedItem();
        return o == null ? "" : o.toString();
    }

    private String gabung(widget.ComboBox c, widget.TextBox t) {
        String v = s(c);
        if (!t.getText().trim().equals("")) { v = v + ": " + t.getText().trim(); }
        return v;
    }

    private void pisah(String v, widget.ComboBox c, widget.TextBox t) {
        if (v == null) { v = ""; }
        if (v.contains(":")) {
            c.setSelectedItem(v.substring(0, v.indexOf(":")).trim());
            t.setText(v.substring(v.indexOf(":") + 1).trim());
        } else {
            c.setSelectedItem(v.trim());
            t.setText("");
        }
    }

    private String g(ResultSet rs, String kolom) {
        try {
            String v = rs.getString(kolom);
            return v == null ? "" : v;
        } catch (Exception e) {
            return "";
        }
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

    /** Grup checkbox: get/set sebagai string gabungan dipisah koma. */
    private static final class Grup implements CetakAsesmen.OpsiCheckbox {
        final JPanel panel = new JPanel(new GridLayout(0, 3, 4, 0));
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
