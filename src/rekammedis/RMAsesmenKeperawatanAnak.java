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
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import kepegawaian.DlgCariDokter;

/**
 * Form Assesment Keperawatan Anak (rawat inap). Dibangun programatik mengikuti
 * pola RMPenilaianAwalKeperawatanRanapBayi. Data disimpan ke tabel baru
 * asesmen_keperawatan_anak (REPLACE INTO, satu baris per no_rawat).
 * Dibuka dari tab "Penilaian Awal" di DlgRawatInap.
 */
public final class RMAsesmenKeperawatanAnak extends JDialog {

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

    // ===== Data umum =====
    private final widget.Tanggal dtpTanggal = dt();
    private final widget.TextBox tRuang = tf();
    private final Grup grpKondisiMasuk = new Grup("Mandiri", "Brankar", "Kursi Roda");
    private final Grup grpVia = new Grup("Praktek", "UGD", "Kamar Operasi");
    private final widget.TextBox tNadi = tf();
    private final widget.TextBox tRespirasi = tf();
    private final widget.TextBox tSuhu = tf();
    private final widget.TextBox tSpo2 = tf();
    private final widget.TextBox tTD = tf();
    private final Grup grpTdPosisi = new Grup("Berdiri", "Tidur", "Duduk");
    private final widget.TextBox tTB = tf();
    private final widget.TextBox tBB = tf();
    private final widget.TextBox tGcsM = tf();
    private final widget.TextBox tGcsV = tf();
    private final widget.TextBox tGcsE = tf();
    private final widget.TextBox tGcsTotal = tf();
    private final widget.TextArea taDiagnosaMasuk = ta();
    private final widget.TextArea taKeluhan = ta();

    // ===== Riwayat tumbuh kembang =====
    private final widget.TextBox tGigiPertama = tf();
    private final widget.TextBox tTengkurap = tf();
    private final widget.TextBox tDuduk = tf();
    private final widget.TextBox tBerdiri = tf();
    private final widget.TextBox tBerjalan = tf();
    private final widget.TextBox tBicara = tf();
    private final widget.TextBox tBacaTulis = tf();
    private final widget.TextBox tRambutPubis = tf();
    private final widget.TextBox tMammae = tf();
    private final widget.TextBox tHaidPertama = tf();
    private final Grup grpMental = new Grup("Normal", "Kelainan / Gangguan Perkembangan");
    private final widget.TextBox tJelaskanKelainan = tf();
    private final Grup grpMakanan = new Grup("ASI", "PASI", "Lainnya");
    private final widget.ComboBox cmbAlergi = cmb("Tidak Ada", "Ada");
    private final Grup grpLateks = new Grup("Balon", "Plester", "Makanan", "Sarung tangan", "NGT");
    private final widget.TextBox tAlergiObat = tf();
    private final JCheckBox cekSnapAlert = new JCheckBox("Bila ada alergi, pakaikan Snap Alert Merah");

    // ===== Barang berharga =====
    private final Grup grpBarang = new Grup("Kacamata", "Lensa Kontak", "Gigi Palsu", "Alat Bantu Dengar");
    private final Grup grpBarangTindakan = new Grup("Dikumpulkan dan disimpan keluarga", "Menolak");

    // ===== Riwayat pasien =====
    private final Grup grpRiwayatPasien = new Grup("Kanker", "Kejang", "Gangguan Jiwa", "Anestesi", "Tidak ada",
            "Hepatitis", "Hipertensi", "PPOK", "Penyakit paru lainnya", "Ulkus", "Jantung", "Asma",
            "Penyakit Ginjal", "TB", "Lainnya");
    private final JCheckBox cekMasalahAnastesi = new JCheckBox("Panggil dokter");
    private final widget.TextArea taDeskripsiPenyakit = ta();
    private final Grup grpVaksinasi = new Grup("Hepatitis", "BCG", "DPT", "Campak", "Polio");
    private final widget.ComboBox cmbInfluenza = cmb("Tidak", "Ya");
    private final widget.ComboBox cmbPneumonia = cmb("Tidak", "Ya");
    private final widget.TextBox tVaksinasiLain = tf();
    private final Grup grpRiwayatKeluarga = new Grup("Penyakit Jantung", "Hipertensi", "Gangguan Jiwa", "Asma", "TB",
            "Diabetes", "Ginjal", "Anestesi", "Kanker", "Kejang", "Gangguan Hematologi", "Tidak ada", "Lainnya");

    // ===== Psikososial / ekonomi / spiritual =====
    private final Grup grpTempatTinggal = new Grup("Rumah", "Panti Asuhan", "Lainnya");
    private final Grup grpAktivitasPsiko = new Grup("Mandiri", "Tongkat", "Kursi Roda", "Tirah Baring");
    private final Grup grpAniaya = new Grup("Ya", "Tidak", "Depresi");
    private final Grup grpEmosional = new Grup("Kooperatif", "Cemas", "Ingin mengakhiri hidup");
    private final widget.TextBox tKeluargaTerdekat = tf();
    private final widget.TextBox tHubungan = tf();
    private final widget.TextBox tTelepon = tf();
    private final Grup grpInformasi = new Grup("Pasien", "Keluarga", "Lainnya");
    private final widget.TextBox tAgama = tf();

    // ===== Pemeriksaan fisik =====
    private final Grup grpFisikMata = new Grup("Gangguan Visus", "Sulit Mendengar", "Gusi", "Kemerahan", "Drainase",
            "Buta", "Tuli", "Gigi", "Rasa Terbakar", "Luka", "Glukoma");
    private final widget.TextBox tFisikMataKet = tf();
    private final Grup grpFisikParu = new Grup("Normal", "Asimetris", "Takipnea", "Ronki", "Barrel Chest", "Bradipnea",
            "Mengi/Wheezing", "Sesak", "Dangkal", "Vesikuler menghilang", "Batuk", "Vesikuler berkurang");
    private final widget.TextBox tFisikParuKet = tf();
    private final Grup grpFisikKardio = new Grup("Takikardi", "Ireguler", "Fatique", "Edema", "Normal", "Bradikardi",
            "Murmur", "Baal", "Tingling (Kesemutan)", "Denyut Nadi lemah", "Denyut nadi tidak ada", "S3 dan S4");
    private final widget.TextBox tFisikKardioKet = tf();
    private final Grup grpFisikGastro = new Grup("Distensi", "Bising usus menurun", "Disfagia", "Terpasang Tube Feeding",
            "Kaku", "Bising usus meningkat", "Konstipasi", "Diet Khusus", "Intoleransi diet", "Anoreksia",
            "Terpasang ostomy", "Diare", "Nyeri tekan", "Diabetes", "Inkontinensia");
    private final widget.TextBox tFisikGastroKet = tf();

    // ===== Status nutrisi (skrining) =====
    private final widget.ComboBox cmbNutrisiKurus = cmb("Tidak", "Ya");
    private final widget.ComboBox cmbNutrisiPenurunan = cmb("Tidak", "Ya");
    private final widget.ComboBox cmbNutrisiKondisi1 = cmb("Tidak", "Ya");
    private final widget.ComboBox cmbNutrisiKondisi2 = cmb("Tidak", "Ya");
    private final widget.ComboBox cmbNutrisiPenyakit = cmb("Tidak", "Ya");
    private final JCheckBox cekNutrisiRujuk = new JCheckBox("Bag Gizi (4-5 Rujuk)");
    private final widget.TextBox tNutrisiTotal = tf();

    private final Grup grpFisikGenito = new Grup("Disturia", "Inkontinensia", "Foley", "Menstruasi akhir", "Frekuensi",
            "Nokturia", "Urostomi", "Sekret abnormal", "Hamil");
    private final widget.TextBox tFisikGenitoKet = tf();
    private final Grup grpFisikNeuro = new Grup("Normal", "Delirium", "Letargi", "Bicara tidak jelas", "Kejang",
            "Tingling", "Koma", "Pupil tidak reaktif", "Vertigo", "Tremor", "Tidak Stabil", "Hesitansi", "Afasia",
            "Sakit Kepala", "Baal", "Paralisis", "Dalam Sedasi", "Genggaman Lemah");
    private final widget.TextBox tFisikNeuroKet = tf();
    private final Grup grpFisikMusculo = new Grup("Alat bantu", "Turgor buruk", "Panas", "Deformitas / atrofi",
            "Diaforesis", "Dingin", "Bengkak", "Lembab", "Kontraktur", "Pucat", "Kemerahan");
    private final widget.TextBox tFisikMusculoKet = tf();

    // ===== Resiko kulit =====
    private final widget.ComboBox cmbKulitFisik = cmb("Baik", "Sedang", "Buruk", "Sangat Buruk");
    private final widget.ComboBox cmbKulitMental = cmb("Kompos Mentis", "Apatis", "Bingung", "Stupor");
    private final widget.ComboBox cmbKulitAktivitas = cmb("Jalan Sendiri", "Jalan Dengan Bantuan", "Kursi Roda", "Tirah Baring");
    private final widget.ComboBox cmbKulitMobilitas = cmb("Bebas Bergerak", "Agak Terbatas", "Sangat Terbatas", "Tidak Bisa Bergerak");
    private final widget.ComboBox cmbKulitInkontinensia = cmb("Tidak", "Terkadang", "Sering", "Inkontinen Urin & Alvi");
    private final widget.TextBox tKulitSkor = tf();
    private final widget.TextBox tKulitCatatan = tf();

    // ===== Aktivitas & harian dasar =====
    private final widget.ComboBox cmbAdlKode = cmb("Mandiri", "25% Dibantu", "50% Dibantu", "75% Dibantu", "Dibantu Penuh");
    private final widget.TextBox tAdlAktivitas = tf();
    private final widget.TextBox tAdlSkor = tf();
    private final JCheckBox cekAdlRehab = new JCheckBox("Rujuk Bag Rehab Medik");

    // ===== Faktor resiko jatuh (Humpty Dumpty) =====
    private final widget.ComboBox cmbJatuhUsia = cmb("Dibawah 3 Tahun", "3 - 7 Tahun", "7 - 13 Tahun", "Diatas 13 Tahun");
    private final widget.ComboBox cmbJatuhJk = cmb("Laki-laki", "Perempuan");
    private final widget.ComboBox cmbJatuhDiagnosis = cmb("Diagnosis terkait neurologis", "Perubahan oksigenasi",
            "Gangguan perilaku / psikiatri", "Diagnosis lainnya");
    private final widget.ComboBox cmbJatuhKognitif = cmb("Tidak menyadari keterbatasan", "Lupa keterbatasan",
            "Mengetahui kemampuan diri", "Orientasi baik");
    private final widget.ComboBox cmbJatuhLingkungan = cmb("Riwayat jatuh", "Pasien menggunakan alat bantu",
            "Pasien berada di tempat tidur", "Area pasien");
    private final widget.ComboBox cmbJatuhRespon = cmb("Dalam 24 Jam", "Dalam 48 Jam", "Lebih dari 48 Jam", "Tidak ada");
    private final widget.ComboBox cmbJatuhObat = cmb("Penggunaan multiple", "Pengobatan lain", "Tidak ada");
    private final widget.TextBox tJatuhTotal = tf();
    private final widget.TextBox tJatuhResiko = tf();

    // ===== Nyeri =====
    private final widget.ComboBox cmbNyeriSkala = cmb("Tidak Sakit", "Sedikit Sakit", "Agak Mengganggu",
            "Mengganggu Aktivitas", "Sangat Mengganggu", "Tak Tertahankan");
    private final widget.TextBox tNyeriLokasi = tf();
    private final widget.TextBox tNyeriOnset = tf();
    private final widget.TextBox tNyeriVariasi = tf();
    private final Grup grpNyeriKualitas = new Grup("Nyeri", "Terbakar", "Menusuk", "Kram", "Tajam", "Tertekan", "Tumpul", "Nyeri tembak");
    private final Grup grpNyeriPemberat = new Grup("Cahaya", "Gelap", "Gerakan", "Berbaring", "Panas");
    private final Grup grpNyeriPencetus = new Grup("Makan", "Sunyi", "Dingin");
    private final widget.TextBox tNyeriObat = tf();
    private final Grup grpNyeriEfek = new Grup("Mual/muntah", "Nafsu makan menurun", "Emosi", "Tidur", "Hubungan dengan orang lain");

    // ===== Restraint =====
    private final widget.ComboBox cmbRestraintPernah = cmb("Tidak", "Ya");
    private final widget.ComboBox cmbRestraintPerlu = cmb("Tidak", "Ya");

    // ===== Komunikasi & edukasi =====
    private final Grup grpKomunikasi = new Grup("Normal", "Inkoheren", "Pelo", "Afasia", "Membaca gerak bibir",
            "Bahasa isyarat", "Tidak dapat membaca", "Tidak dapat mendengar");
    private final Grup grpBahasa = new Grup("Indonesia", "Daerah", "Asing");
    private final Grup grpHambatanBelajar = new Grup("Bahasa", "Pendengaran", "Penglihatan", "Hilang memori", "Kognitif");
    private final Grup grpCaraBelajar = new Grup("Membaca", "Audio Visual", "Diskusi", "Demonstrasi");
    private final Grup grpPendidikan = new Grup("Perguruan Tinggi", "SLTA", "Lainnya");
    private final Grup grpEdukasi = new Grup("Pengetahuan tentang penyakit & rencana pengobatan", "Rencana tempat rujukan",
            "Pemberian Obat", "Manajemen Nyeri", "Pengajaran Diet/Nutrisi", "Aktivitas / Mobilisasi");

    // ===== Discharge planning / perencanaan pulang =====
    private final Grup grpKriteria = new Grup("Memiliki hambatan Mobilisasi",
            "Membutuhkan pelayanan medis dan keperawatan berkelanjutan", "Tergantung dengan orang lain dalam ADL");
    private final widget.ComboBox cmbTinggal = cmb("Orang Tua", "Lain-lain");
    private final widget.TextBox tTinggalSebut = tf();
    private final widget.ComboBox cmbPerokok = cmb("Tidak", "Ya");
    private final widget.TextBox tPerokokSebut = tf();
    private final Grup grpKondisiRumah = new Grup("Sumber air bersih", "Sumber air kotor", "Lingkungan Polusi", "Lingkungan Tidak Polusi");
    private final widget.ComboBox cmbAlatBantu = cmb("Tidak", "Ya");
    private final widget.TextBox tAlatBantuSebut = tf();
    private final widget.ComboBox cmbRujukKomunitas = cmb("Tidak", "Ya");
    private final widget.TextBox tRujukSebut = tf();

    private final widget.TextArea taMasalah = ta();
    private final widget.TextArea taRencana = ta();

    // ===== TTD =====
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

    public RMAsesmenKeperawatanAnak(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Assesment Keperawatan Anak ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        cekSnapAlert.setOpaque(false);
        cekMasalahAnastesi.setOpaque(false);
        cekNutrisiRujuk.setOpaque(false);
        cekAdlRehab.setOpaque(false);
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
        setLocationRelativeTo(parent);
    }

    // ====================== UI ======================
    private void initComponents() {
        getContentPane().setLayout(new BorderLayout(6, 6));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        int row = 0;
        row = judul(form, row, "Identitas Pasien");
        row = baris2(form, row, "No.Rawat", TNoRw, "No.RM", TNoRM);
        row = baris2(form, row, "Nama Pasien", TPasien, "Jenis Kelamin", TJK);
        row = baris2(form, row, "Tanggal Lahir", TTglLahir, "Unit", TUnit);
        row = baris2(form, row, "Cara Bayar", TCaraBayar, "Alamat", TAlamat);

        row = judul(form, row, "Data Umum");
        row = baris2(form, row, "Tanggal / Jam *", dtpTanggal, "Ruang", tRuang);
        row = grup(form, row, "Kondisi Saat Masuk", grpKondisiMasuk.panel);
        row = grup(form, row, "Via", grpVia.panel);
        row = baris2(form, row, "Nadi (x/menit) *", tNadi, "Respirasi (x/menit) *", tRespirasi);
        row = baris2(form, row, "Suhu (C) *", tSuhu, "SpO2 (%)", tSpo2);
        row = baris2(form, row, "Tekanan Darah", tTD, "Tinggi Badan (cm)", tTB);
        row = grup(form, row, "Posisi TD", grpTdPosisi.panel);
        row = baris1(form, row, "Berat Badan (kg) *", tBB);
        row = baris2(form, row, "GCS - E", tGcsE, "GCS - V", tGcsV);
        row = baris2(form, row, "GCS - M", tGcsM, "Angka GCS", tGcsTotal);
        row = area(form, row, "Diagnosa Masuk", taDiagnosaMasuk);
        row = area(form, row, "Keluhan Utama", taKeluhan);

        row = judul(form, row, "Riwayat Tumbuh Kembang");
        row = baris2(form, row, "Gigi Pertama (Bln)", tGigiPertama, "Tengkurap (Bln)", tTengkurap);
        row = baris2(form, row, "Duduk (Bln)", tDuduk, "Berdiri (Bln)", tBerdiri);
        row = baris2(form, row, "Berjalan (Bln)", tBerjalan, "Bicara (Bln)", tBicara);
        row = baris1(form, row, "Membaca & Menulis", tBacaTulis);
        row = baris2(form, row, "Rambut Pubis (Bln)", tRambutPubis, "Mammae (Bln)", tMammae);
        row = baris1(form, row, "Haid Pertama (Bln)", tHaidPertama);
        row = grup(form, row, "Perkembangan Mental / Emosi", grpMental.panel);
        row = baris1(form, row, "Jelaskan Kelainan", tJelaskanKelainan);
        row = grup(form, row, "Riwayat Makanan", grpMakanan.panel);
        row = baris1(form, row, "Alergi", cmbAlergi);
        row = grup(form, row, "Jenis Alergi (Lateks/Lainnya)", grpLateks.panel);
        row = baris1(form, row, "Obat / Jenis / Reaksi", tAlergiObat);
        row = grup(form, row, "Snap Alert", panelCek(cekSnapAlert));

        row = judul(form, row, "Barang Berharga");
        row = grup(form, row, "Jenis Barang", grpBarang.panel);
        row = grup(form, row, "Tindakan", grpBarangTindakan.panel);

        row = judul(form, row, "Riwayat Pasien");
        row = grup(form, row, "Riwayat Penyakit / Operasi / Cidera Mayor", grpRiwayatPasien.panel);
        row = grup(form, row, "Masalah Anastesi", panelCek(cekMasalahAnastesi));
        row = area(form, row, "Deskripsi Penyakit & Operasi", taDeskripsiPenyakit);
        row = grup(form, row, "Riwayat Vaksinasi", grpVaksinasi.panel);
        row = baris2(form, row, "Influenza dalam 12 bulan", cmbInfluenza, "Pneumonia dalam 5 tahun", cmbPneumonia);
        row = baris1(form, row, "Vaksinasi Lainnya", tVaksinasiLain);
        row = grup(form, row, "Riwayat Keluarga", grpRiwayatKeluarga.panel);

        row = judul(form, row, "Psikososial / Ekonomi / Spiritual");
        row = grup(form, row, "Tempat Tinggal", grpTempatTinggal.panel);
        row = grup(form, row, "Aktivitas", grpAktivitasPsiko.panel);
        row = grup(form, row, "Curiga Penganiayaan / Penelantaran", grpAniaya.panel);
        row = grup(form, row, "Status Emosional", grpEmosional.panel);
        row = baris2(form, row, "Keluarga Terdekat", tKeluargaTerdekat, "Hubungan", tHubungan);
        row = baris1(form, row, "Telepon", tTelepon);
        row = grup(form, row, "Informasi Didapat Dari", grpInformasi.panel);
        row = baris1(form, row, "Agama / Nilai Keyakinan", tAgama);

        row = judul(form, row, "Pemeriksaan Fisik");
        row = grup(form, row, "Mata, Telinga, Hidung, Tenggorokan", grpFisikMata.panel);
        row = baris1(form, row, "Catatan (THT)", tFisikMataKet);
        row = grup(form, row, "Paru", grpFisikParu.panel);
        row = baris1(form, row, "Catatan (Paru)", tFisikParuKet);
        row = grup(form, row, "Kardiovaskular", grpFisikKardio.panel);
        row = baris1(form, row, "Catatan (Kardio)", tFisikKardioKet);
        row = grup(form, row, "Gastrointestinal", grpFisikGastro.panel);
        row = baris1(form, row, "Catatan (Gastro)", tFisikGastroKet);

        row = judul(form, row, "Pemeriksaan Status Nutrisi");
        row = baris1(form, row, "Pasien tampak kurus", cmbNutrisiKurus);
        row = baris1(form, row, "Penurunan BB 1 bulan terakhir", cmbNutrisiPenurunan);
        row = baris1(form, row, "Diare/muntah/asupan berkurang (1)", cmbNutrisiKondisi1);
        row = baris1(form, row, "Diare/muntah/asupan berkurang (2)", cmbNutrisiKondisi2);
        row = baris1(form, row, "Penyakit beresiko malnutrisi", cmbNutrisiPenyakit);
        row = baris2(form, row, "Total Skor Nutrisi", tNutrisiTotal, "Rujuk Gizi", panelCek(cekNutrisiRujuk));

        row = grup(form, row, "Genitourinaria & Ginekologi", grpFisikGenito.panel);
        row = baris1(form, row, "Catatan (Genito)", tFisikGenitoKet);
        row = grup(form, row, "Neurologi", grpFisikNeuro.panel);
        row = baris1(form, row, "Catatan (Neuro)", tFisikNeuroKet);
        row = grup(form, row, "Muskuloskeletal & Kulit", grpFisikMusculo.panel);
        row = baris1(form, row, "Catatan (Muskulo)", tFisikMusculoKet);

        row = judul(form, row, "Pemeriksaan Resiko Kulit");
        row = baris2(form, row, "Kondisi Fisik", cmbKulitFisik, "Kondisi Mental", cmbKulitMental);
        row = baris2(form, row, "Aktivitas", cmbKulitAktivitas, "Mobilitas", cmbKulitMobilitas);
        row = baris2(form, row, "Inkontinensia", cmbKulitInkontinensia, "Skor", tKulitSkor);
        row = baris1(form, row, "Catatan", tKulitCatatan);

        row = judul(form, row, "Aktivitas & Harian Dasar");
        row = baris2(form, row, "Kemandirian", cmbAdlKode, "Aktivitas", tAdlAktivitas);
        row = baris2(form, row, "Skor", tAdlSkor, "Rehab Medik", panelCek(cekAdlRehab));

        row = judul(form, row, "Faktor Resiko Jatuh");
        row = baris2(form, row, "Usia", cmbJatuhUsia, "Jenis Kelamin", cmbJatuhJk);
        row = baris2(form, row, "Diagnosis", cmbJatuhDiagnosis, "Gangguan Kognitif", cmbJatuhKognitif);
        row = baris2(form, row, "Faktor Lingkungan", cmbJatuhLingkungan, "Respon thd Sedasi/Bedah", cmbJatuhRespon);
        row = baris2(form, row, "Penggunaan Obat", cmbJatuhObat, "Total Skor", tJatuhTotal);
        row = baris1(form, row, "Tingkat Resiko", tJatuhResiko);

        row = judul(form, row, "Pemeriksaan Nyeri");
        row = baris1(form, row, "Skala Nyeri", cmbNyeriSkala);
        row = baris2(form, row, "Lokasi", tNyeriLokasi, "Onset", tNyeriOnset);
        row = baris1(form, row, "Variasi", tNyeriVariasi);
        row = grup(form, row, "Kualitas", grpNyeriKualitas.panel);
        row = grup(form, row, "Faktor Pemberat", grpNyeriPemberat.panel);
        row = grup(form, row, "Faktor Pencetus", grpNyeriPencetus.panel);
        row = baris1(form, row, "Obat-obatan", tNyeriObat);
        row = grup(form, row, "Efek Nyeri", grpNyeriEfek.panel);

        row = judul(form, row, "Restraint");
        row = baris2(form, row, "Pernah menggunakan Restraint", cmbRestraintPernah, "Perlu Restraint", cmbRestraintPerlu);

        row = judul(form, row, "Komunikasi & Edukasi");
        row = grup(form, row, "Komunikasi", grpKomunikasi.panel);
        row = grup(form, row, "Bahasa Sehari-hari", grpBahasa.panel);
        row = grup(form, row, "Hambatan Belajar", grpHambatanBelajar.panel);
        row = grup(form, row, "Cara Belajar", grpCaraBelajar.panel);
        row = grup(form, row, "Tingkat Pendidikan", grpPendidikan.panel);
        row = grup(form, row, "Kebutuhan Edukasi", grpEdukasi.panel);

        row = judul(form, row, "Discharge Planning & Perencanaan Pulang");
        row = grup(form, row, "Kriteria Discharge Planning", grpKriteria.panel);
        row = baris2(form, row, "Pasien Tinggal Dengan", cmbTinggal, "Sebutkan", tTinggalSebut);
        row = baris2(form, row, "Keluarga Perokok", cmbPerokok, "Sebutkan", tPerokokSebut);
        row = grup(form, row, "Kondisi Rumah", grpKondisiRumah.panel);
        row = baris2(form, row, "Perlu Alat Bantu Khusus", cmbAlatBantu, "Sebutkan", tAlatBantuSebut);
        row = baris2(form, row, "Dirujuk ke Komunitas", cmbRujukKomunitas, "Sebutkan", tRujukSebut);

        row = judul(form, row, "Resume");
        row = area(form, row, "Masalah Keperawatan", taMasalah);
        row = area(form, row, "Rencana Keperawatan", taRencana);

        row = judul(form, row, "Tanda Tangan");
        row = baris1(form, row, "Tanggal / Jam", dtpTtd);
        row = baris2(form, row, "Perawat Pengkaji *", gabungBtn(KdPetugas, NmPetugas, null), "Dokter PJ", gabungBtn(KdDokter, NmDokter, BtnDokter));

        JScrollPane scroll = new JScrollPane(form);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(28);
        scroll.getVerticalScrollBar().setBlockIncrement(120);
        getContentPane().add(scroll, BorderLayout.CENTER);

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
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        bawah.add(BtnSimpan);
        bawah.add(BtnBaru);
        bawah.add(BtnHapus);
        bawah.add(BtnCetak);
        bawah.add(BtnKeluar);
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
        JLabel l = new JLabel(t + " :");
        l.setFont(new Font("Tahoma", Font.PLAIN, 11));
        return l;
    }

    private void siz(Component c) {
        if (c instanceof widget.TextBox || c instanceof widget.ComboBox || c instanceof widget.Tanggal) {
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

    private JPanel panelCek(JCheckBox cek) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        pnl.setOpaque(false);
        pnl.add(cek);
        return pnl;
    }

    // ====================== Entry points ======================
    public void isCek() {
        boolean bisa = akses.getpenilaian_awal_keperawatan_ranap();
        BtnSimpan.setEnabled(bisa);
        BtnHapus.setEnabled(bisa);
        KdPetugas.setText(akses.getkode());
        NmPetugas.setText(Sequel.cariIsi("select nama from petugas where nip=?", akses.getkode()));
    }

    public void emptTeks() {
        for (widget.TextBox t : new widget.TextBox[]{tRuang, tNadi, tRespirasi, tSuhu, tSpo2, tTD, tTB, tBB,
            tGcsM, tGcsV, tGcsE, tGcsTotal, tGigiPertama, tTengkurap, tDuduk, tBerdiri, tBerjalan, tBicara, tBacaTulis,
            tRambutPubis, tMammae, tHaidPertama, tJelaskanKelainan, tAlergiObat, tVaksinasiLain,
            tKeluargaTerdekat, tHubungan, tTelepon, tAgama,
            tFisikMataKet, tFisikParuKet, tFisikKardioKet, tFisikGastroKet, tNutrisiTotal,
            tFisikGenitoKet, tFisikNeuroKet, tFisikMusculoKet, tKulitSkor, tKulitCatatan,
            tAdlAktivitas, tAdlSkor, tJatuhTotal, tJatuhResiko, tNyeriLokasi, tNyeriOnset, tNyeriVariasi, tNyeriObat,
            tTinggalSebut, tPerokokSebut, tAlatBantuSebut, tRujukSebut, KdDokter, NmDokter}) {
            t.setText("");
        }
        for (widget.TextArea a : new widget.TextArea[]{taDiagnosaMasuk, taKeluhan, taDeskripsiPenyakit, taMasalah, taRencana}) {
            a.setText("");
        }
        for (Grup g : new Grup[]{grpKondisiMasuk, grpVia, grpTdPosisi, grpMental, grpMakanan, grpLateks, grpBarang,
            grpBarangTindakan, grpRiwayatPasien, grpVaksinasi, grpRiwayatKeluarga, grpTempatTinggal, grpAktivitasPsiko,
            grpAniaya, grpEmosional, grpInformasi, grpFisikMata, grpFisikParu, grpFisikKardio, grpFisikGastro,
            grpFisikGenito, grpFisikNeuro, grpFisikMusculo, grpNyeriKualitas, grpNyeriPemberat, grpNyeriPencetus,
            grpNyeriEfek, grpKomunikasi, grpBahasa, grpHambatanBelajar, grpCaraBelajar, grpPendidikan, grpEdukasi,
            grpKriteria, grpKondisiRumah}) {
            g.clear();
        }
        for (widget.ComboBox c : new widget.ComboBox[]{cmbAlergi, cmbInfluenza, cmbPneumonia, cmbNutrisiKurus,
            cmbNutrisiPenurunan, cmbNutrisiKondisi1, cmbNutrisiKondisi2, cmbNutrisiPenyakit, cmbKulitFisik, cmbKulitMental,
            cmbKulitAktivitas, cmbKulitMobilitas, cmbKulitInkontinensia, cmbAdlKode, cmbJatuhUsia, cmbJatuhJk,
            cmbJatuhDiagnosis, cmbJatuhKognitif, cmbJatuhLingkungan, cmbJatuhRespon, cmbJatuhObat, cmbNyeriSkala,
            cmbRestraintPernah, cmbRestraintPerlu, cmbTinggal, cmbPerokok, cmbAlatBantu, cmbRujukKomunitas}) {
            c.setSelectedIndex(0);
        }
        cekSnapAlert.setSelected(false);
        cekMasalahAnastesi.setSelected(false);
        cekNutrisiRujuk.setSelected(false);
        cekAdlRehab.setSelected(false);
        dtpTanggal.setDate(new Date());
        dtpTtd.setDate(new Date());
    }

    public void setNoRm(String norwt, Date tgl2, String carabayar, String norm) {
        emptTeks();
        TNoRw.setText(norwt);
        TNoRM.setText(norm);
        TCaraBayar.setText(carabayar);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select p.nm_pasien,p.no_rkm_medis,p.jk,p.tgl_lahir,p.alamat,p.no_tlp,ifnull(p.agama,'') as agama,"
                + "ifnull(poliklinik.nm_poli,'') as unit,"
                + "ifnull(rp.p_jawab,'') as p_jawab,ifnull(rp.hubunganpj,'') as hubunganpj,ifnull(pj.png_jawab,'') as carabayar "
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
                    tAgama.setText(rs.getString("agama"));
                    tTelepon.setText(rs.getString("no_tlp"));
                    tKeluargaTerdekat.setText(rs.getString("p_jawab"));
                    tHubungan.setText(rs.getString("hubunganpj"));
                    if (rs.getString("carabayar") != null && !rs.getString("carabayar").trim().equals("")) {
                        TCaraBayar.setText(rs.getString("carabayar"));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif identitas anak : " + e);
        }
        String ruang = Sequel.cariIsi("select ifnull(bangsal.nm_bangsal,'') from kamar_inap "
                + "inner join kamar on kamar.kd_kamar=kamar_inap.kd_kamar "
                + "inner join bangsal on bangsal.kd_bangsal=kamar.kd_bangsal "
                + "where kamar_inap.no_rawat=? order by kamar_inap.tgl_masuk desc limit 1", norwt);
        tRuang.setText(ruang);
        muat();
    }

    // ====================== Kolom DB ======================
    private static final String[] KOLOM = {
        "no_rawat", "tanggal", "jam", "ruang",
        "kondisi_masuk", "via", "nadi", "respirasi", "suhu", "spo2", "td", "td_posisi", "tb", "bb",
        "gcs_m", "gcs_v", "gcs_e", "gcs_total", "diagnosa_masuk", "keluhan_utama",
        "gigi_pertama", "tengkurap", "duduk", "berdiri", "berjalan", "bicara", "baca_tulis",
        "rambut_pubis", "mammae", "haid_pertama", "mental_emosi", "jelaskan_kelainan", "riwayat_makanan",
        "alergi", "alergi_lateks", "alergi_obat", "snap_alert",
        "barang_jenis", "barang_tindakan",
        "riwayat_pasien", "masalah_anastesi", "deskripsi_penyakit", "riwayat_vaksinasi", "influenza_12bln",
        "pneumonia_5thn", "vaksinasi_lainnya", "riwayat_keluarga",
        "tempat_tinggal", "aktivitas_psiko", "curiga_aniaya", "status_emosional", "keluarga_terdekat",
        "hubungan_keluarga", "telepon", "informasi_dari", "agama_keyakinan",
        "fisik_mata", "fisik_mata_ket", "fisik_paru", "fisik_paru_ket", "fisik_kardio", "fisik_kardio_ket",
        "fisik_gastro", "fisik_gastro_ket",
        "nutrisi_kurus", "nutrisi_penurunan_bb", "nutrisi_kondisi1", "nutrisi_kondisi2", "nutrisi_penyakit",
        "nutrisi_rujuk", "nutrisi_total",
        "fisik_genito", "fisik_genito_ket", "fisik_neuro", "fisik_neuro_ket", "fisik_musculo", "fisik_musculo_ket",
        "kulit_fisik", "kulit_mental", "kulit_aktivitas", "kulit_mobilitas", "kulit_inkontinensia", "kulit_skor", "kulit_catatan",
        "adl_kode", "adl_aktivitas", "adl_skor", "adl_rehab",
        "jatuh_usia", "jatuh_jk", "jatuh_diagnosis", "jatuh_kognitif", "jatuh_lingkungan", "jatuh_respon", "jatuh_obat",
        "jatuh_total", "jatuh_resiko",
        "nyeri_skala", "nyeri_lokasi", "nyeri_onset", "nyeri_variasi", "nyeri_kualitas", "nyeri_pemberat",
        "nyeri_pencetus", "nyeri_obat", "nyeri_efek",
        "restraint_pernah", "restraint_perlu",
        "komunikasi", "bahasa_harian", "hambatan_belajar", "cara_belajar", "tingkat_pendidikan", "kebutuhan_edukasi",
        "kriteria_discharge", "tinggal_dengan", "keluarga_perokok", "kondisi_rumah", "alat_bantu_khusus", "rujuk_komunitas",
        "masalah_keperawatan", "rencana_keperawatan",
        "tgl_ttd", "jam_ttd", "nik", "kd_dokter"
    };

    private void simpan() {
        if (TNoRw.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        String[][] wajib = {
            {tNadi.getText(), "Nadi"}, {tRespirasi.getText(), "Respirasi"}, {tSuhu.getText(), "Suhu"},
            {tBB.getText(), "Berat Badan"}, {KdPetugas.getText(), "Perawat Pengkaji"}
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
            TNoRw.getText(), tgl, jam, tRuang.getText(),
            grpKondisiMasuk.get(), grpVia.get(), tNadi.getText(), tRespirasi.getText(), tSuhu.getText(), tSpo2.getText(),
            tTD.getText(), grpTdPosisi.get(), tTB.getText(), tBB.getText(),
            tGcsM.getText(), tGcsV.getText(), tGcsE.getText(), tGcsTotal.getText(), taDiagnosaMasuk.getText(), taKeluhan.getText(),
            tGigiPertama.getText(), tTengkurap.getText(), tDuduk.getText(), tBerdiri.getText(), tBerjalan.getText(), tBicara.getText(), tBacaTulis.getText(),
            tRambutPubis.getText(), tMammae.getText(), tHaidPertama.getText(), grpMental.get(), tJelaskanKelainan.getText(), grpMakanan.get(),
            s(cmbAlergi), grpLateks.get(), tAlergiObat.getText(), (cekSnapAlert.isSelected() ? "Ya" : ""),
            grpBarang.get(), grpBarangTindakan.get(),
            grpRiwayatPasien.get(), (cekMasalahAnastesi.isSelected() ? "Ya" : ""), taDeskripsiPenyakit.getText(), grpVaksinasi.get(), s(cmbInfluenza),
            s(cmbPneumonia), tVaksinasiLain.getText(), grpRiwayatKeluarga.get(),
            grpTempatTinggal.get(), grpAktivitasPsiko.get(), grpAniaya.get(), grpEmosional.get(), tKeluargaTerdekat.getText(),
            tHubungan.getText(), tTelepon.getText(), grpInformasi.get(), tAgama.getText(),
            grpFisikMata.get(), tFisikMataKet.getText(), grpFisikParu.get(), tFisikParuKet.getText(), grpFisikKardio.get(), tFisikKardioKet.getText(),
            grpFisikGastro.get(), tFisikGastroKet.getText(),
            s(cmbNutrisiKurus), s(cmbNutrisiPenurunan), s(cmbNutrisiKondisi1), s(cmbNutrisiKondisi2), s(cmbNutrisiPenyakit),
            (cekNutrisiRujuk.isSelected() ? "Ya" : ""), tNutrisiTotal.getText(),
            grpFisikGenito.get(), tFisikGenitoKet.getText(), grpFisikNeuro.get(), tFisikNeuroKet.getText(), grpFisikMusculo.get(), tFisikMusculoKet.getText(),
            s(cmbKulitFisik), s(cmbKulitMental), s(cmbKulitAktivitas), s(cmbKulitMobilitas), s(cmbKulitInkontinensia), tKulitSkor.getText(), tKulitCatatan.getText(),
            s(cmbAdlKode), tAdlAktivitas.getText(), tAdlSkor.getText(), (cekAdlRehab.isSelected() ? "Ya" : ""),
            s(cmbJatuhUsia), s(cmbJatuhJk), s(cmbJatuhDiagnosis), s(cmbJatuhKognitif), s(cmbJatuhLingkungan), s(cmbJatuhRespon), s(cmbJatuhObat),
            tJatuhTotal.getText(), tJatuhResiko.getText(),
            s(cmbNyeriSkala), tNyeriLokasi.getText(), tNyeriOnset.getText(), tNyeriVariasi.getText(), grpNyeriKualitas.get(), grpNyeriPemberat.get(),
            grpNyeriPencetus.get(), tNyeriObat.getText(), grpNyeriEfek.get(),
            s(cmbRestraintPernah), s(cmbRestraintPerlu),
            grpKomunikasi.get(), grpBahasa.get(), grpHambatanBelajar.get(), grpCaraBelajar.get(), grpPendidikan.get(), grpEdukasi.get(),
            grpKriteria.get(), gabung(cmbTinggal, tTinggalSebut), gabung(cmbPerokok, tPerokokSebut), grpKondisiRumah.get(),
            gabung(cmbAlatBantu, tAlatBantuSebut), gabung(cmbRujukKomunitas, tRujukSebut),
            taMasalah.getText(), taRencana.getText(),
            tglTtd, jamTtd, KdPetugas.getText(), KdDokter.getText()
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
                "replace into asesmen_keperawatan_anak (" + cols + ") values (" + qm + ")")) {
            for (int i = 0; i < nilai.length; i++) {
                ps.setString(i + 1, nilai[i]);
            }
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Assesment keperawatan anak tersimpan.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan.\n" + e.getMessage());
        }
    }

    private void muat() {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select * from asesmen_keperawatan_anak where no_rawat=?")) {
            ps.setString(1, TNoRw.getText());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    setTgl(dtpTanggal, rs.getString("tanggal"), rs.getString("jam"));
                    tRuang.setText(g(rs, "ruang"));
                    grpKondisiMasuk.set(g(rs, "kondisi_masuk")); grpVia.set(g(rs, "via"));
                    tNadi.setText(g(rs, "nadi")); tRespirasi.setText(g(rs, "respirasi")); tSuhu.setText(g(rs, "suhu")); tSpo2.setText(g(rs, "spo2"));
                    tTD.setText(g(rs, "td")); grpTdPosisi.set(g(rs, "td_posisi")); tTB.setText(g(rs, "tb")); tBB.setText(g(rs, "bb"));
                    tGcsM.setText(g(rs, "gcs_m")); tGcsV.setText(g(rs, "gcs_v")); tGcsE.setText(g(rs, "gcs_e")); tGcsTotal.setText(g(rs, "gcs_total"));
                    taDiagnosaMasuk.setText(g(rs, "diagnosa_masuk")); taKeluhan.setText(g(rs, "keluhan_utama"));
                    tGigiPertama.setText(g(rs, "gigi_pertama")); tTengkurap.setText(g(rs, "tengkurap")); tDuduk.setText(g(rs, "duduk"));
                    tBerdiri.setText(g(rs, "berdiri")); tBerjalan.setText(g(rs, "berjalan")); tBicara.setText(g(rs, "bicara")); tBacaTulis.setText(g(rs, "baca_tulis"));
                    tRambutPubis.setText(g(rs, "rambut_pubis")); tMammae.setText(g(rs, "mammae")); tHaidPertama.setText(g(rs, "haid_pertama"));
                    grpMental.set(g(rs, "mental_emosi")); tJelaskanKelainan.setText(g(rs, "jelaskan_kelainan")); grpMakanan.set(g(rs, "riwayat_makanan"));
                    cmbAlergi.setSelectedItem(g(rs, "alergi")); grpLateks.set(g(rs, "alergi_lateks")); tAlergiObat.setText(g(rs, "alergi_obat"));
                    cekSnapAlert.setSelected("Ya".equalsIgnoreCase(g(rs, "snap_alert")));
                    grpBarang.set(g(rs, "barang_jenis")); grpBarangTindakan.set(g(rs, "barang_tindakan"));
                    grpRiwayatPasien.set(g(rs, "riwayat_pasien")); cekMasalahAnastesi.setSelected("Ya".equalsIgnoreCase(g(rs, "masalah_anastesi")));
                    taDeskripsiPenyakit.setText(g(rs, "deskripsi_penyakit")); grpVaksinasi.set(g(rs, "riwayat_vaksinasi"));
                    cmbInfluenza.setSelectedItem(g(rs, "influenza_12bln")); cmbPneumonia.setSelectedItem(g(rs, "pneumonia_5thn"));
                    tVaksinasiLain.setText(g(rs, "vaksinasi_lainnya")); grpRiwayatKeluarga.set(g(rs, "riwayat_keluarga"));
                    grpTempatTinggal.set(g(rs, "tempat_tinggal")); grpAktivitasPsiko.set(g(rs, "aktivitas_psiko"));
                    grpAniaya.set(g(rs, "curiga_aniaya")); grpEmosional.set(g(rs, "status_emosional"));
                    tKeluargaTerdekat.setText(g(rs, "keluarga_terdekat")); tHubungan.setText(g(rs, "hubungan_keluarga")); tTelepon.setText(g(rs, "telepon"));
                    grpInformasi.set(g(rs, "informasi_dari")); tAgama.setText(g(rs, "agama_keyakinan"));
                    grpFisikMata.set(g(rs, "fisik_mata")); tFisikMataKet.setText(g(rs, "fisik_mata_ket"));
                    grpFisikParu.set(g(rs, "fisik_paru")); tFisikParuKet.setText(g(rs, "fisik_paru_ket"));
                    grpFisikKardio.set(g(rs, "fisik_kardio")); tFisikKardioKet.setText(g(rs, "fisik_kardio_ket"));
                    grpFisikGastro.set(g(rs, "fisik_gastro")); tFisikGastroKet.setText(g(rs, "fisik_gastro_ket"));
                    cmbNutrisiKurus.setSelectedItem(g(rs, "nutrisi_kurus")); cmbNutrisiPenurunan.setSelectedItem(g(rs, "nutrisi_penurunan_bb"));
                    cmbNutrisiKondisi1.setSelectedItem(g(rs, "nutrisi_kondisi1")); cmbNutrisiKondisi2.setSelectedItem(g(rs, "nutrisi_kondisi2"));
                    cmbNutrisiPenyakit.setSelectedItem(g(rs, "nutrisi_penyakit")); cekNutrisiRujuk.setSelected("Ya".equalsIgnoreCase(g(rs, "nutrisi_rujuk")));
                    tNutrisiTotal.setText(g(rs, "nutrisi_total"));
                    grpFisikGenito.set(g(rs, "fisik_genito")); tFisikGenitoKet.setText(g(rs, "fisik_genito_ket"));
                    grpFisikNeuro.set(g(rs, "fisik_neuro")); tFisikNeuroKet.setText(g(rs, "fisik_neuro_ket"));
                    grpFisikMusculo.set(g(rs, "fisik_musculo")); tFisikMusculoKet.setText(g(rs, "fisik_musculo_ket"));
                    cmbKulitFisik.setSelectedItem(g(rs, "kulit_fisik")); cmbKulitMental.setSelectedItem(g(rs, "kulit_mental"));
                    cmbKulitAktivitas.setSelectedItem(g(rs, "kulit_aktivitas")); cmbKulitMobilitas.setSelectedItem(g(rs, "kulit_mobilitas"));
                    cmbKulitInkontinensia.setSelectedItem(g(rs, "kulit_inkontinensia")); tKulitSkor.setText(g(rs, "kulit_skor")); tKulitCatatan.setText(g(rs, "kulit_catatan"));
                    cmbAdlKode.setSelectedItem(g(rs, "adl_kode")); tAdlAktivitas.setText(g(rs, "adl_aktivitas")); tAdlSkor.setText(g(rs, "adl_skor"));
                    cekAdlRehab.setSelected("Ya".equalsIgnoreCase(g(rs, "adl_rehab")));
                    cmbJatuhUsia.setSelectedItem(g(rs, "jatuh_usia")); cmbJatuhJk.setSelectedItem(g(rs, "jatuh_jk"));
                    cmbJatuhDiagnosis.setSelectedItem(g(rs, "jatuh_diagnosis")); cmbJatuhKognitif.setSelectedItem(g(rs, "jatuh_kognitif"));
                    cmbJatuhLingkungan.setSelectedItem(g(rs, "jatuh_lingkungan")); cmbJatuhRespon.setSelectedItem(g(rs, "jatuh_respon"));
                    cmbJatuhObat.setSelectedItem(g(rs, "jatuh_obat")); tJatuhTotal.setText(g(rs, "jatuh_total")); tJatuhResiko.setText(g(rs, "jatuh_resiko"));
                    cmbNyeriSkala.setSelectedItem(g(rs, "nyeri_skala")); tNyeriLokasi.setText(g(rs, "nyeri_lokasi")); tNyeriOnset.setText(g(rs, "nyeri_onset"));
                    tNyeriVariasi.setText(g(rs, "nyeri_variasi")); grpNyeriKualitas.set(g(rs, "nyeri_kualitas")); grpNyeriPemberat.set(g(rs, "nyeri_pemberat"));
                    grpNyeriPencetus.set(g(rs, "nyeri_pencetus")); tNyeriObat.setText(g(rs, "nyeri_obat")); grpNyeriEfek.set(g(rs, "nyeri_efek"));
                    cmbRestraintPernah.setSelectedItem(g(rs, "restraint_pernah")); cmbRestraintPerlu.setSelectedItem(g(rs, "restraint_perlu"));
                    grpKomunikasi.set(g(rs, "komunikasi")); grpBahasa.set(g(rs, "bahasa_harian")); grpHambatanBelajar.set(g(rs, "hambatan_belajar"));
                    grpCaraBelajar.set(g(rs, "cara_belajar")); grpPendidikan.set(g(rs, "tingkat_pendidikan")); grpEdukasi.set(g(rs, "kebutuhan_edukasi"));
                    grpKriteria.set(g(rs, "kriteria_discharge"));
                    pisah(g(rs, "tinggal_dengan"), cmbTinggal, tTinggalSebut);
                    pisah(g(rs, "keluarga_perokok"), cmbPerokok, tPerokokSebut);
                    grpKondisiRumah.set(g(rs, "kondisi_rumah"));
                    pisah(g(rs, "alat_bantu_khusus"), cmbAlatBantu, tAlatBantuSebut);
                    pisah(g(rs, "rujuk_komunitas"), cmbRujukKomunitas, tRujukSebut);
                    taMasalah.setText(g(rs, "masalah_keperawatan")); taRencana.setText(g(rs, "rencana_keperawatan"));
                    setTgl(dtpTtd, rs.getString("tgl_ttd"), rs.getString("jam_ttd"));
                    if (!g(rs, "kd_dokter").equals("")) {
                        KdDokter.setText(g(rs, "kd_dokter"));
                        NmDokter.setText(Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", g(rs, "kd_dokter")));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat anak : " + e);
        }
    }

    private void cetak() {
        if (TNoRw.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        StringBuilder b = new StringBuilder();
        CetakAsesmen.h(b, "Identitas Pasien");
        CetakAsesmen.r(b, "No. Rawat", TNoRw.getText());
        CetakAsesmen.r(b, "No. RM", TNoRM.getText());
        CetakAsesmen.r(b, "Nama Pasien", TPasien.getText());
        CetakAsesmen.r(b, "Jenis Kelamin", TJK.getText());
        CetakAsesmen.r(b, "Tanggal Lahir", TTglLahir.getText());
        CetakAsesmen.r(b, "Alamat", TAlamat.getText());
        CetakAsesmen.r(b, "Unit", TUnit.getText());
        CetakAsesmen.r(b, "Cara Bayar", TCaraBayar.getText());
        CetakAsesmen.sp(b);
        CetakAsesmen.h(b, "Data Umum");
        CetakAsesmen.r(b, "Tanggal / Jam", dtpTanggal.getSelectedItem() + "");
        CetakAsesmen.r(b, "Ruang", tRuang.getText());
        CetakAsesmen.r(b, "Kondisi Saat Masuk", grpKondisiMasuk.get());
        CetakAsesmen.r(b, "Via", grpVia.get());
        CetakAsesmen.r(b, "Nadi", tNadi.getText());
        CetakAsesmen.r(b, "Respirasi", tRespirasi.getText());
        CetakAsesmen.r(b, "Suhu", tSuhu.getText());
        CetakAsesmen.r(b, "SpO2", tSpo2.getText());
        CetakAsesmen.r(b, "Tekanan Darah", tTD.getText());
        CetakAsesmen.r(b, "Posisi TD", grpTdPosisi.get());
        CetakAsesmen.r(b, "Tinggi Badan", tTB.getText());
        CetakAsesmen.r(b, "Berat Badan", tBB.getText());
        CetakAsesmen.r(b, "GCS (E/V/M/Total)", tGcsE.getText() + " / " + tGcsV.getText() + " / " + tGcsM.getText() + " / " + tGcsTotal.getText());
        CetakAsesmen.r(b, "Diagnosa Masuk", taDiagnosaMasuk.getText());
        CetakAsesmen.r(b, "Keluhan Utama", taKeluhan.getText());
        CetakAsesmen.sp(b);
        CetakAsesmen.h(b, "Riwayat Tumbuh Kembang");
        CetakAsesmen.r(b, "Gigi Pertama", tGigiPertama.getText());
        CetakAsesmen.r(b, "Tengkurap / Duduk / Berdiri", tTengkurap.getText() + " / " + tDuduk.getText() + " / " + tBerdiri.getText());
        CetakAsesmen.r(b, "Berjalan / Bicara", tBerjalan.getText() + " / " + tBicara.getText());
        CetakAsesmen.r(b, "Membaca & Menulis", tBacaTulis.getText());
        CetakAsesmen.r(b, "Rambut Pubis / Mammae / Haid", tRambutPubis.getText() + " / " + tMammae.getText() + " / " + tHaidPertama.getText());
        CetakAsesmen.r(b, "Perkembangan Mental/Emosi", grpMental.get());
        CetakAsesmen.r(b, "Jelaskan Kelainan", tJelaskanKelainan.getText());
        CetakAsesmen.r(b, "Riwayat Makanan", grpMakanan.get());
        CetakAsesmen.r(b, "Alergi", s(cmbAlergi));
        CetakAsesmen.r(b, "Jenis Alergi (Lateks)", grpLateks.get());
        CetakAsesmen.r(b, "Obat / Jenis / Reaksi", tAlergiObat.getText());
        CetakAsesmen.r(b, "Snap Alert", cekSnapAlert.isSelected() ? "Ya" : "-");
        CetakAsesmen.sp(b);
        CetakAsesmen.h(b, "Barang Berharga");
        CetakAsesmen.r(b, "Jenis Barang", grpBarang.get());
        CetakAsesmen.r(b, "Tindakan", grpBarangTindakan.get());
        CetakAsesmen.sp(b);
        CetakAsesmen.h(b, "Riwayat Pasien");
        CetakAsesmen.r(b, "Riwayat Penyakit/Operasi/Cidera", grpRiwayatPasien.get());
        CetakAsesmen.r(b, "Masalah Anastesi", cekMasalahAnastesi.isSelected() ? "Panggil dokter" : "-");
        CetakAsesmen.r(b, "Deskripsi Penyakit & Operasi", taDeskripsiPenyakit.getText());
        CetakAsesmen.r(b, "Riwayat Vaksinasi", grpVaksinasi.get());
        CetakAsesmen.r(b, "Influenza 12 bln / Pneumonia 5 thn", s(cmbInfluenza) + " / " + s(cmbPneumonia));
        CetakAsesmen.r(b, "Vaksinasi Lainnya", tVaksinasiLain.getText());
        CetakAsesmen.r(b, "Riwayat Keluarga", grpRiwayatKeluarga.get());
        CetakAsesmen.sp(b);
        CetakAsesmen.h(b, "Psikososial / Ekonomi / Spiritual");
        CetakAsesmen.r(b, "Tempat Tinggal", grpTempatTinggal.get());
        CetakAsesmen.r(b, "Aktivitas", grpAktivitasPsiko.get());
        CetakAsesmen.r(b, "Curiga Penganiayaan", grpAniaya.get());
        CetakAsesmen.r(b, "Status Emosional", grpEmosional.get());
        CetakAsesmen.r(b, "Keluarga Terdekat / Hubungan", tKeluargaTerdekat.getText() + " / " + tHubungan.getText());
        CetakAsesmen.r(b, "Telepon", tTelepon.getText());
        CetakAsesmen.r(b, "Informasi Didapat Dari", grpInformasi.get());
        CetakAsesmen.r(b, "Agama / Nilai Keyakinan", tAgama.getText());
        CetakAsesmen.sp(b);
        CetakAsesmen.h(b, "Pemeriksaan Fisik");
        CetakAsesmen.r(b, "Mata/Telinga/Hidung/Tenggorokan", grpFisikMata.get());
        CetakAsesmen.r(b, "Catatan (THT)", tFisikMataKet.getText());
        CetakAsesmen.r(b, "Paru", grpFisikParu.get());
        CetakAsesmen.r(b, "Catatan (Paru)", tFisikParuKet.getText());
        CetakAsesmen.r(b, "Kardiovaskular", grpFisikKardio.get());
        CetakAsesmen.r(b, "Catatan (Kardio)", tFisikKardioKet.getText());
        CetakAsesmen.r(b, "Gastrointestinal", grpFisikGastro.get());
        CetakAsesmen.r(b, "Catatan (Gastro)", tFisikGastroKet.getText());
        CetakAsesmen.sp(b);
        CetakAsesmen.h(b, "Status Nutrisi");
        CetakAsesmen.r(b, "Tampak kurus", s(cmbNutrisiKurus));
        CetakAsesmen.r(b, "Penurunan BB 1 bln", s(cmbNutrisiPenurunan));
        CetakAsesmen.r(b, "Diare/muntah/asupan (1)", s(cmbNutrisiKondisi1));
        CetakAsesmen.r(b, "Diare/muntah/asupan (2)", s(cmbNutrisiKondisi2));
        CetakAsesmen.r(b, "Penyakit beresiko malnutrisi", s(cmbNutrisiPenyakit));
        CetakAsesmen.r(b, "Total Skor / Rujuk Gizi", tNutrisiTotal.getText() + (cekNutrisiRujuk.isSelected() ? " (Rujuk)" : ""));
        CetakAsesmen.sp(b);
        CetakAsesmen.h(b, "Pemeriksaan Fisik Lanjutan");
        CetakAsesmen.r(b, "Genitourinaria & Ginekologi", grpFisikGenito.get());
        CetakAsesmen.r(b, "Catatan (Genito)", tFisikGenitoKet.getText());
        CetakAsesmen.r(b, "Neurologi", grpFisikNeuro.get());
        CetakAsesmen.r(b, "Catatan (Neuro)", tFisikNeuroKet.getText());
        CetakAsesmen.r(b, "Muskuloskeletal & Kulit", grpFisikMusculo.get());
        CetakAsesmen.r(b, "Catatan (Muskulo)", tFisikMusculoKet.getText());
        CetakAsesmen.sp(b);
        CetakAsesmen.h(b, "Resiko Kulit");
        CetakAsesmen.r(b, "Kondisi Fisik / Mental", s(cmbKulitFisik) + " / " + s(cmbKulitMental));
        CetakAsesmen.r(b, "Aktivitas / Mobilitas", s(cmbKulitAktivitas) + " / " + s(cmbKulitMobilitas));
        CetakAsesmen.r(b, "Inkontinensia", s(cmbKulitInkontinensia));
        CetakAsesmen.r(b, "Skor / Catatan", tKulitSkor.getText() + " / " + tKulitCatatan.getText());
        CetakAsesmen.sp(b);
        CetakAsesmen.h(b, "Aktivitas & Harian Dasar");
        CetakAsesmen.r(b, "Kemandirian", s(cmbAdlKode));
        CetakAsesmen.r(b, "Aktivitas / Skor", tAdlAktivitas.getText() + " / " + tAdlSkor.getText());
        CetakAsesmen.r(b, "Rehab Medik", cekAdlRehab.isSelected() ? "Rujuk" : "-");
        CetakAsesmen.sp(b);
        CetakAsesmen.h(b, "Faktor Resiko Jatuh");
        CetakAsesmen.r(b, "Usia / Jenis Kelamin", s(cmbJatuhUsia) + " / " + s(cmbJatuhJk));
        CetakAsesmen.r(b, "Diagnosis / Kognitif", s(cmbJatuhDiagnosis) + " / " + s(cmbJatuhKognitif));
        CetakAsesmen.r(b, "Lingkungan / Respon", s(cmbJatuhLingkungan) + " / " + s(cmbJatuhRespon));
        CetakAsesmen.r(b, "Penggunaan Obat", s(cmbJatuhObat));
        CetakAsesmen.r(b, "Total Skor / Tingkat Resiko", tJatuhTotal.getText() + " / " + tJatuhResiko.getText());
        CetakAsesmen.sp(b);
        CetakAsesmen.h(b, "Pemeriksaan Nyeri");
        CetakAsesmen.r(b, "Skala Nyeri", s(cmbNyeriSkala));
        CetakAsesmen.r(b, "Lokasi / Onset", tNyeriLokasi.getText() + " / " + tNyeriOnset.getText());
        CetakAsesmen.r(b, "Variasi", tNyeriVariasi.getText());
        CetakAsesmen.r(b, "Kualitas", grpNyeriKualitas.get());
        CetakAsesmen.r(b, "Faktor Pemberat", grpNyeriPemberat.get());
        CetakAsesmen.r(b, "Faktor Pencetus", grpNyeriPencetus.get());
        CetakAsesmen.r(b, "Obat-obatan", tNyeriObat.getText());
        CetakAsesmen.r(b, "Efek Nyeri", grpNyeriEfek.get());
        CetakAsesmen.sp(b);
        CetakAsesmen.h(b, "Restraint");
        CetakAsesmen.r(b, "Pernah / Perlu Restraint", s(cmbRestraintPernah) + " / " + s(cmbRestraintPerlu));
        CetakAsesmen.sp(b);
        CetakAsesmen.h(b, "Komunikasi & Edukasi");
        CetakAsesmen.r(b, "Komunikasi", grpKomunikasi.get());
        CetakAsesmen.r(b, "Bahasa Sehari-hari", grpBahasa.get());
        CetakAsesmen.r(b, "Hambatan Belajar", grpHambatanBelajar.get());
        CetakAsesmen.r(b, "Cara Belajar", grpCaraBelajar.get());
        CetakAsesmen.r(b, "Tingkat Pendidikan", grpPendidikan.get());
        CetakAsesmen.r(b, "Kebutuhan Edukasi", grpEdukasi.get());
        CetakAsesmen.sp(b);
        CetakAsesmen.h(b, "Discharge Planning");
        CetakAsesmen.r(b, "Kriteria", grpKriteria.get());
        CetakAsesmen.r(b, "Tinggal Dengan", gabung(cmbTinggal, tTinggalSebut));
        CetakAsesmen.r(b, "Keluarga Perokok", gabung(cmbPerokok, tPerokokSebut));
        CetakAsesmen.r(b, "Kondisi Rumah", grpKondisiRumah.get());
        CetakAsesmen.r(b, "Perlu Alat Bantu Khusus", gabung(cmbAlatBantu, tAlatBantuSebut));
        CetakAsesmen.r(b, "Dirujuk ke Komunitas", gabung(cmbRujukKomunitas, tRujukSebut));
        CetakAsesmen.sp(b);
        CetakAsesmen.h(b, "Resume");
        CetakAsesmen.r(b, "Masalah Keperawatan", taMasalah.getText());
        CetakAsesmen.r(b, "Rencana Keperawatan", taRencana.getText());
        CetakAsesmen.cetak("ASSESMENT KEPERAWATAN ANAK", "RM 5d", b.toString(),
                dtpTtd.getSelectedItem() + "", "Perawat Pengkaji", KdPetugas.getText(), NmPetugas.getText());
    }

    private void hapus() {
        if (TNoRw.getText().trim().equals("")) { return; }
        if (JOptionPane.showConfirmDialog(this, "Hapus assesment keperawatan anak untuk No.Rawat " + TNoRw.getText() + " ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement("delete from asesmen_keperawatan_anak where no_rawat=?")) {
            ps.setString(1, TNoRw.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data dihapus.");
            String norw = TNoRw.getText(), norm = TNoRM.getText(), cb = TCaraBayar.getText();
            emptTeks();
            TNoRw.setText(norw); TNoRM.setText(norm); TCaraBayar.setText(cb);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus.\n" + e.getMessage());
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
    private static final class Grup {
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

        String get() {
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
