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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
 * Form Asesmen Keperawatan Dewasa (rawat inap, RM 5d). Programatik mengikuti
 * pola RMAsesmenKeperawatanAnak. Disimpan ke tabel asesmen_keperawatan_dewasa
 * (REPLACE INTO, satu baris per no_rawat). Dibuka dari tab "Penilaian Awal".
 */
public final class RMAsesmenKeperawatanDewasa extends JDialog {

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final DlgCariDokter dokter = new DlgCariDokter(null, true);

    // Header
    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TJK = ro();
    private final widget.TextBox TTglLahir = ro();
    private final widget.TextBox TAlamat = ro();
    private final widget.TextBox TUnit = ro();
    private final widget.TextBox TCaraBayar = ro();
    private final widget.TextBox tRuang = tf();
    private final widget.TextBox tLantai = tf();
    private final widget.TextBox tKelas = tf();
    private final widget.TextBox tDiagnosaMedis = tf();
    private final widget.ComboBox cmbGelang = cmb("Ya", "Menolak");

    // Data umum
    private final widget.Tanggal dtpTanggal = dt();
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
    private final widget.TextBox tGcsE = tf();
    private final widget.TextBox tGcsM = tf();
    private final widget.TextBox tGcsV = tf();
    private final widget.TextArea taDiagnosaMasuk = ta();
    private final widget.TextArea taKeluhan = ta();
    private final widget.ComboBox cmbAlergi = cmb("Tidak ada", "Ada");
    private final Grup grpLateks = new Grup("Balon", "Plester", "Makanan", "Sarung tangan", "NGT");
    private final widget.TextBox tAlergiObat = tf();
    private final JCheckBox cekSnapAlert = new JCheckBox("Bila ada alergi, pakaikan Snap Alert Merah");
    private final Grup grpBarang = new Grup("Kacamata", "Lensa Kontak", "Gigi palsu", "Alat bantu dengar");
    private final Grup grpBarangTindakan = new Grup("Dikumpulkan dan disimpan keluarga", "Menolak");

    // Riwayat pasien
    private final Grup grpRiwayatPasien = new Grup("Hipertensi", "Kanker", "Diabetes", "PPOK", "Infark miokard",
            "Kejang", "Hepatitis", "Asma", "Stroke", "Gangguan jiwa", "Ulkus", "Penyakit paru lainnya",
            "Jantung lainnya", "Anestesi", "Penyakit ginjal", "Lainnya", "Tidak ada");
    private final JCheckBox cekMasalahAnastesi = new JCheckBox("Panggil dokter");
    private final widget.TextArea taDeskripsiPenyakit = ta();
    private final widget.ComboBox cmbAlkohol = cmb("Tidak", "Ya", "Berhenti");
    private final widget.TextBox tAlkoholJumlah = tf();
    private final widget.TextBox tAlkoholJenis = tf();
    private final widget.ComboBox cmbMerokok = cmb("Tidak", "Ya", "Berhenti");
    private final widget.TextBox tMerokokJumlah = tf();
    private final widget.TextBox tMerokokJenis = tf();
    private final widget.ComboBox cmbInfluenza = cmb("Tidak", "Ya");
    private final widget.ComboBox cmbPneumonia = cmb("Tidak", "Ya");
    private final widget.TextBox tVaksinasiLain = tf();
    private final Grup grpRiwayatKeluarga = new Grup("Penyakit Jantung", "Hipertensi", "Gangguan jiwa", "Asma", "TB",
            "Diabetes", "Ginjal", "Anestesi", "Kanker", "Kejang", "Gangguan Hematologi", "Tidak ada", "Lainnya");

    // Psikososial
    private final widget.ComboBox cmbStatusNikah = cmb("Menikah", "Belum menikah", "Duda/janda");
    private final widget.ComboBox cmbKeluargaTinggal = cmb("Tinggal serumah", "Tinggal sendiri");
    private final widget.ComboBox cmbTempatTinggal = cmb("Rumah", "Panti asuhan", "Lainnya");
    private final widget.ComboBox cmbPekerjaan = cmb("Purna waktu", "Paruh waktu", "Pensiun", "Lainnya");
    private final widget.TextBox tAgama = tf();
    private final Grup grpAktivitasPsiko = new Grup("Mandiri", "Tongkat", "Kursi Roda", "Tirah baring");
    private final Grup grpAniaya = new Grup("Ya", "Tidak", "Depresi");
    private final Grup grpEmosional = new Grup("Kooperatif", "Cemas", "Ingin mengakhiri hidup");
    private final widget.TextBox tKeluargaTerdekat = tf();
    private final widget.TextBox tHubungan = tf();
    private final widget.TextBox tTelepon = tf();
    private final Grup grpInformasi = new Grup("Pasien", "Keluarga", "Lainnya");

    // Pemeriksaan fisik
    private final Grup grpFisikMata = new Grup("Normal", "Gangguan Visus", "Sulit mendengar", "Gusi", "Kemerahan",
            "Drainase", "Buta", "Tuli", "Gigi", "Rasa terbakar", "Luka", "Glukoma");
    private final widget.TextBox tFisikMataKet = tf();
    private final Grup grpFisikKardio = new Grup("Normal", "Takikardi", "Ireguler", "Fatique", "Edema", "Bradikardi",
            "Murmur", "Baal", "Tingling (kesemutan)", "Denyut nadi lemah", "Denyut nadi tidak ada", "S3 dan atau S4");
    private final widget.TextBox tFisikKardioKet = tf();
    private final Grup grpFisikGastro = new Grup("Normal", "Distensi", "Bising usus menurun", "Disfagia",
            "Terpasang Tube Feeding", "Kaku", "Bising usus meningkat", "Konstipasi", "Diet khusus", "Intoleransi diet",
            "Anoreksia", "Terpasang ostomy", "Diare", "Nyeri tekan", "Diabetes", "Inkontinensia", "BAB terakhir");
    private final widget.TextBox tFisikGastroKet = tf();

    private final widget.ComboBox cmbMst1 = cmb("Tidak (0)", "Tidak yakin/baju longgar (2)", "Ya 1-5 kg (1)",
            "Ya 6-10 kg (2)", "Ya 11-15 kg (3)", "Ya >15 kg (4)", "Tidak tahu (2)");
    private final widget.ComboBox cmbMst2 = cmb("Tidak (0)", "Ya (1)");
    private final widget.TextBox tMstTotal = tf();

    private final Grup grpFisikGenito = new Grup("Normal", "Disturia", "Inkontinensia", "Foley", "Menstruasi akhir",
            "Frekuensi", "Nokturia", "Urostomi", "Sekret abnormal", "Hesitansi", "Hematuria", "Menopause", "Hamil");
    private final widget.TextBox tFisikGenitoKet = tf();
    private final Grup grpFisikNeuro = new Grup("Normal", "Delirium", "Letargi", "Bicara tidak jelas", "Kejang",
            "Tingling", "Koma", "Pupil tidak reaktif", "Vertigo", "Tremor", "Tidak Stabil", "Hesitansi", "Afasia",
            "Sakit kepala", "Baal", "Paralisis", "Dalam sedasi", "Genggaman lemah", "Skala otot");
    private final widget.TextBox tFisikNeuroKet = tf();
    private final Grup grpFisikMusculo = new Grup("Normal", "Alat bantu", "Turgor buruk", "Panas", "Deformitas/atrofi",
            "Diaforesis", "Dingin", "Bengkak", "Lembab", "Gangren", "Pucat", "Kemerahan", "Luka");
    private final widget.TextBox tFisikMusculoKet = tf();
    private final JCheckBox cekLukaRujuk = new JCheckBox("Rujuk Bag. Penanganan Luka");

    // Norton scale
    private final widget.ComboBox cmbNortonFisik = cmb("4. Baik", "3. Cukup", "2. Buruk", "1. Sangat buruk");
    private final widget.ComboBox cmbNortonMental = cmb("4. Kompos mentis", "3. Apatis", "2. Delirium", "1. Stupor");
    private final widget.ComboBox cmbNortonAktivitas = cmb("4. Mandiri", "3. Dipapah", "2. Kursi roda", "1. Tirah baring");
    private final widget.ComboBox cmbNortonMobilitas = cmb("4. Baik", "3. Agak terbatas", "2. Sangat terbatas", "1. Imobilisasi");
    private final widget.ComboBox cmbNortonInkontinensia = cmb("4. Tidak", "3. Terkadang", "2. Sering", "1. Selalu");
    private final widget.TextBox tNortonTotal = tf();
    private final widget.TextBox tNortonCatatan = tf();

    // ADL Barthel
    private final widget.ComboBox cmbAdlMakan = cmb("2 - Mandiri", "1 - Butuh bantuan", "0 - Tidak mampu");
    private final widget.ComboBox cmbAdlMandi = cmb("1 - Mandiri", "0 - Tergantung orang lain");
    private final widget.ComboBox cmbAdlGrooming = cmb("1 - Mandiri", "0 - Membutuhkan bantuan");
    private final widget.ComboBox cmbAdlBerpakaian = cmb("2 - Mandiri", "1 - Sebagian dibantu", "0 - Tergantung orang lain");
    private final widget.ComboBox cmbAdlBak = cmb("2 - Kontinensia", "1 - Kadang inkontinensia", "0 - Inkontinensia/kateter");
    private final widget.ComboBox cmbAdlBab = cmb("2 - Kontinensia", "1 - Kadang inkontinensia", "0 - Inkontinensia");
    private final widget.ComboBox cmbAdlToilet = cmb("2 - Mandiri", "1 - Membutuhkan bantuan", "0 - Tergantung orang lain");
    private final widget.ComboBox cmbAdlTransfer = cmb("3 - Mandiri", "2 - Bantuan kecil (1 org)", "1 - Bantuan (2 org)", "0 - Tidak mampu");
    private final widget.ComboBox cmbAdlMobilitas = cmb("3 - Mandiri", "2 - Berjalan dibantu 1 org", "1 - Kursi roda", "0 - Immobile");
    private final widget.ComboBox cmbAdlTangga = cmb("2 - Mandiri", "1 - Membutuhkan bantuan", "0 - Tidak mampu");
    private final widget.TextBox tAdlTotal = tf();
    private final widget.TextBox tAdlInterpretasi = tf();

    // Morse
    private final widget.ComboBox cmbMorseJatuh = cmb("Tidak (0)", "Ya (25)");
    private final widget.ComboBox cmbMorseDiagnosis = cmb("Tidak (0)", "Ya (15)");
    private final widget.ComboBox cmbMorseAlat = cmb("Tidak ada/tirah baring (0)", "Tongkat/penopang (15)", "Perabot (30)");
    private final widget.ComboBox cmbMorseInfus = cmb("Tidak (0)", "Ya (20)");
    private final widget.ComboBox cmbMorseJalan = cmb("Normal/imobilisasi (0)", "Lemah (10)", "Terganggu (20)");
    private final widget.ComboBox cmbMorseMental = cmb("Orientasi baik (0)", "Sering lupa keterbatasan (15)");
    private final widget.TextBox tMorseTotal = tf();
    private final widget.TextBox tMorseResiko = tf();

    // Nyeri
    private final widget.TextBox tNyeriSkala = tf();
    private final widget.TextBox tNyeriLokasi = tf();
    private final widget.TextBox tNyeriOnset = tf();
    private final widget.TextBox tNyeriVariasi = tf();
    private final Grup grpNyeriKualitas = new Grup("Nyeri", "Terbakar", "Menusuk", "Kram", "Tajam", "Tertekan", "Tumpul", "Nyeri tembak");
    private final Grup grpNyeriPemberat = new Grup("Cahaya", "Gelap", "Gerakan", "Berbaring", "Panas");
    private final Grup grpNyeriPencetus = new Grup("Makan", "Sunyi", "Dingin", "Panas");
    private final widget.TextBox tNyeriObat = tf();
    private final Grup grpNyeriEfek = new Grup("Mual/muntah", "Nafsu makan", "Emosi", "Aktivitas", "Tidur", "Hubungan dengan orang lain");

    // Restraint
    private final widget.ComboBox cmbRestraintPernah = cmb("Tidak", "Ya");
    private final widget.ComboBox cmbRestraintPerlu = cmb("Tidak", "Ya");

    // Komunikasi & edukasi
    private final Grup grpKomunikasi = new Grup("Normal", "Inkoheren", "Pelo", "Afasia", "Membaca gerak bibir",
            "Bahasa Isyarat", "Tidak dapat membaca", "Tidak dapat mendengar");
    private final Grup grpBahasa = new Grup("Indonesia", "Daerah", "Asing");
    private final Grup grpHambatanBelajar = new Grup("Bahasa", "Pendengaran", "Penglihatan", "Hilang memori", "Kognitif");
    private final Grup grpCaraBelajar = new Grup("Membaca", "Audio Visual", "Diskusi", "Demonstrasi");
    private final Grup grpPendidikan = new Grup("Perguruan Tinggi", "SLTA", "Lainnya");
    private final Grup grpEdukasi = new Grup("Pengetahuan tentang penyakit & rencana pengobatan", "Rencana tempat rujukan",
            "Pemberian Obat", "Aktivitas/Mobilisasi", "Manajemen Nyeri", "Pengajaran Diet/Nutrisi");

    // Discharge
    private final Grup grpKriteria = new Grup("Usia lebih dari 60 tahun", "Memiliki hambatan mobilisasi",
            "Membutuhkan pelayanan medis dan keperawatan berkelanjutan", "Tergantung dengan orang lain dalam ADL");

    // Resume
    private final Grup grpResumeUmum = new Grup("Branker lateks", "Barang Berharga");
    private final Grup grpResumeRujuk = new Grup("Keuangan", "Gizi", "Penanganan Luka", "Rehab Medik");
    private final Grup grpRencana = new Grup("Ansietas", "Kardiovaskular", "Genitourinaria/ginekologi", "Nyeri",
            "Depresi", "Gastrointestinal", "Kulit", "Infeksi", "Spiritual", "Endokrin", "ADL", "Respirasi", "Nutrisi", "Jatuh/Cedera");
    private final Grup grpMasalah = new Grup("Manajemen Nyeri", "Perawatan Diri", "Manajemen Nutrisi", "Manajemen Jalan Napas",
            "Manajemen Neurologi", "Terapi Oksigen", "Cardiac Care", "Proteksi Terhadap Infeksi", "Circulation Status",
            "Kontrol Infeksi", "Neurologic Status", "Penurunan Kecemasan", "Monitor Vital Sign");

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

    public RMAsesmenKeperawatanDewasa(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Asesmen Keperawatan Dewasa ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        cekSnapAlert.setOpaque(false);
        cekMasalahAnastesi.setOpaque(false);
        cekLukaRujuk.setOpaque(false);
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
        row = baris2(form, row, "Ruang", tRuang, "Lantai", tLantai);
        row = baris2(form, row, "Kelas", tKelas, "Diagnosa Medis", tDiagnosaMedis);
        row = baris1(form, row, "Gelang Identitas", cmbGelang);

        row = judul(form, row, "Bagian 1 : Data Umum");
        row = baris1(form, row, "Tanggal / Jam *", dtpTanggal);
        row = grup(form, row, "Kondisi Saat Masuk", grpKondisiMasuk.panel);
        row = grup(form, row, "Via", grpVia.panel);
        row = baris2(form, row, "Nadi (x/menit) *", tNadi, "Respirasi (x/menit) *", tRespirasi);
        row = baris2(form, row, "Suhu (C) *", tSuhu, "SpO2 (%)", tSpo2);
        row = baris2(form, row, "Tekanan Darah", tTD, "Tinggi Badan (cm)", tTB);
        row = grup(form, row, "Posisi TD", grpTdPosisi.panel);
        row = baris2(form, row, "Berat Badan (kg) *", tBB, "GCS - E", tGcsE);
        row = baris2(form, row, "GCS - M", tGcsM, "GCS - V", tGcsV);
        row = area(form, row, "Diagnosis Masuk", taDiagnosaMasuk);
        row = area(form, row, "Keluhan Utama", taKeluhan);
        row = baris1(form, row, "Alergi", cmbAlergi);
        row = grup(form, row, "Jenis Alergi (Lateks)", grpLateks.panel);
        row = baris1(form, row, "Obat / Jenis / Reaksi", tAlergiObat);
        row = grup(form, row, "Snap Alert", panelCek(cekSnapAlert));
        row = grup(form, row, "Barang Berharga", grpBarang.panel);
        row = grup(form, row, "Tindakan Barang", grpBarangTindakan.panel);

        row = judul(form, row, "Bagian 2 : Riwayat Pasien");
        row = grup(form, row, "Riwayat Penyakit / Operasi / Cedera Mayor", grpRiwayatPasien.panel);
        row = grup(form, row, "Masalah Anastesi", panelCek(cekMasalahAnastesi));
        row = area(form, row, "Deskripsi Penyakit & Operasi", taDeskripsiPenyakit);
        row = baris2(form, row, "Alkohol/Obat", cmbAlkohol, "Jumlah/Hari (Alkohol)", tAlkoholJumlah);
        row = baris1(form, row, "Jenis (Alkohol)", tAlkoholJenis);
        row = baris2(form, row, "Merokok", cmbMerokok, "Jumlah/Hari (Rokok)", tMerokokJumlah);
        row = baris1(form, row, "Jenis (Rokok)", tMerokokJenis);
        row = baris2(form, row, "Influenza dalam 12 bulan", cmbInfluenza, "Pneumonia dalam 5 tahun", cmbPneumonia);
        row = baris1(form, row, "Vaksinasi Lainnya", tVaksinasiLain);
        row = grup(form, row, "Riwayat Keluarga", grpRiwayatKeluarga.panel);

        row = judul(form, row, "Psikososial / Ekonomi / Spiritual");
        row = baris2(form, row, "Status Pernikahan", cmbStatusNikah, "Keluarga", cmbKeluargaTinggal);
        row = baris2(form, row, "Tempat Tinggal", cmbTempatTinggal, "Pekerjaan", cmbPekerjaan);
        row = baris1(form, row, "Agama / Nilai Keyakinan", tAgama);
        row = grup(form, row, "Aktivitas", grpAktivitasPsiko.panel);
        row = grup(form, row, "Curiga Penganiayaan / Penelantaran", grpAniaya.panel);
        row = grup(form, row, "Status Emosional", grpEmosional.panel);
        row = baris2(form, row, "Keluarga Terdekat", tKeluargaTerdekat, "Hubungan", tHubungan);
        row = baris1(form, row, "Telepon", tTelepon);
        row = grup(form, row, "Informasi Didapat Dari", grpInformasi.panel);

        row = judul(form, row, "Bagian 3 : Pemeriksaan Fisik");
        row = grup(form, row, "Mata, Telinga, Hidung, Tenggorokan", grpFisikMata.panel);
        row = baris1(form, row, "Catatan (THT)", tFisikMataKet);
        row = grup(form, row, "Kardiovaskular", grpFisikKardio.panel);
        row = baris1(form, row, "Catatan (Kardio)", tFisikKardioKet);
        row = grup(form, row, "Gastrointestinal", grpFisikGastro.panel);
        row = baris1(form, row, "Catatan (Gastro)", tFisikGastroKet);

        row = judul(form, row, "Skrining Gizi (MST)");
        row = baris1(form, row, "Penurunan BB 6 bulan terakhir", cmbMst1);
        row = baris1(form, row, "Asupan makan berkurang", cmbMst2);
        row = baris1(form, row, "Skor Total MST", tMstTotal);

        row = grup(form, row, "Genitourinaria & Ginekologi", grpFisikGenito.panel);
        row = baris1(form, row, "Catatan (Genito)", tFisikGenitoKet);
        row = grup(form, row, "Neurologi", grpFisikNeuro.panel);
        row = baris1(form, row, "Catatan (Neuro)", tFisikNeuroKet);
        row = grup(form, row, "Muskuloskeletal & Kulit", grpFisikMusculo.panel);
        row = baris2(form, row, "Catatan (Muskulo)", tFisikMusculoKet, "Penanganan Luka", panelCek(cekLukaRujuk));

        row = judul(form, row, "Norton Scale (Risiko Kulit)");
        row = baris2(form, row, "Kondisi Fisik", cmbNortonFisik, "Kondisi Mental", cmbNortonMental);
        row = baris2(form, row, "Aktivitas", cmbNortonAktivitas, "Mobilitas", cmbNortonMobilitas);
        row = baris2(form, row, "Inkontinensia", cmbNortonInkontinensia, "Total Skor", tNortonTotal);
        row = baris1(form, row, "Catatan (Norton)", tNortonCatatan);

        row = judul(form, row, "ADL / Indeks Barthel");
        row = baris2(form, row, "Makan", cmbAdlMakan, "Mandi", cmbAdlMandi);
        row = baris2(form, row, "Perawatan Diri", cmbAdlGrooming, "Berpakaian", cmbAdlBerpakaian);
        row = baris2(form, row, "Buang Air Kecil", cmbAdlBak, "Buang Air Besar", cmbAdlBab);
        row = baris2(form, row, "Penggunaan Toilet", cmbAdlToilet, "Transfer", cmbAdlTransfer);
        row = baris2(form, row, "Mobilitas", cmbAdlMobilitas, "Naik Turun Tangga", cmbAdlTangga);
        row = baris2(form, row, "Total Skor ADL", tAdlTotal, "Interpretasi", tAdlInterpretasi);

        row = judul(form, row, "Faktor Resiko Jatuh (Morse)");
        row = baris2(form, row, "Riwayat Jatuh", cmbMorseJatuh, "Diagnosis Sekunder", cmbMorseDiagnosis);
        row = baris2(form, row, "Alat Bantu", cmbMorseAlat, "Terpasang Infus", cmbMorseInfus);
        row = baris2(form, row, "Gaya Berjalan", cmbMorseJalan, "Status Mental", cmbMorseMental);
        row = baris2(form, row, "Total Skor Morse", tMorseTotal, "Tingkat Resiko", tMorseResiko);

        row = judul(form, row, "Pemeriksaan Nyeri (NRS)");
        row = baris2(form, row, "Skala Nyeri (0-10)", tNyeriSkala, "Lokasi", tNyeriLokasi);
        row = baris2(form, row, "Onset", tNyeriOnset, "Variasi", tNyeriVariasi);
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

        row = judul(form, row, "Kebutuhan Pasien Kritis / Discharge Planning");
        row = grup(form, row, "Kriteria", grpKriteria.panel);

        row = judul(form, row, "Resume");
        row = grup(form, row, "Umum", grpResumeUmum.panel);
        row = grup(form, row, "Rujuk", grpResumeRujuk.panel);
        row = grup(form, row, "Rencana Keperawatan (pilih 3 prioritas)", grpRencana.panel);
        row = grup(form, row, "Masalah Keperawatan", grpMasalah.panel);

        row = judul(form, row, "Tanda Tangan");
        row = baris1(form, row, "Tanggal / Jam Selesai Asesmen", dtpTtd);
        row = baris2(form, row, "Pelaksana Asesmen *", gabungBtn(KdPetugas, NmPetugas, null), "Dokter PJ", gabungBtn(KdDokter, NmDokter, BtnDokter));

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
        for (widget.TextBox t : new widget.TextBox[]{tRuang, tLantai, tKelas, tDiagnosaMedis, tNadi, tRespirasi, tSuhu, tSpo2,
            tTD, tTB, tBB, tGcsE, tGcsM, tGcsV, tAlergiObat, tAlkoholJumlah, tAlkoholJenis, tMerokokJumlah, tMerokokJenis,
            tVaksinasiLain, tAgama, tKeluargaTerdekat, tHubungan, tTelepon, tFisikMataKet, tFisikKardioKet, tFisikGastroKet,
            tMstTotal, tFisikGenitoKet, tFisikNeuroKet, tFisikMusculoKet, tNortonTotal, tNortonCatatan, tAdlTotal, tAdlInterpretasi,
            tMorseTotal, tMorseResiko, tNyeriSkala, tNyeriLokasi, tNyeriOnset, tNyeriVariasi, tNyeriObat, KdDokter, NmDokter}) {
            t.setText("");
        }
        for (widget.TextArea a : new widget.TextArea[]{taDiagnosaMasuk, taKeluhan, taDeskripsiPenyakit}) {
            a.setText("");
        }
        for (Grup g : new Grup[]{grpKondisiMasuk, grpVia, grpTdPosisi, grpLateks, grpBarang, grpBarangTindakan,
            grpRiwayatPasien, grpRiwayatKeluarga, grpAktivitasPsiko, grpAniaya, grpEmosional, grpInformasi,
            grpFisikMata, grpFisikKardio, grpFisikGastro, grpFisikGenito, grpFisikNeuro, grpFisikMusculo,
            grpNyeriKualitas, grpNyeriPemberat, grpNyeriPencetus, grpNyeriEfek, grpKomunikasi, grpBahasa,
            grpHambatanBelajar, grpCaraBelajar, grpPendidikan, grpEdukasi, grpKriteria, grpResumeUmum, grpResumeRujuk,
            grpRencana, grpMasalah}) {
            g.clear();
        }
        for (widget.ComboBox c : new widget.ComboBox[]{cmbGelang, cmbAlergi, cmbAlkohol, cmbMerokok, cmbInfluenza, cmbPneumonia,
            cmbStatusNikah, cmbKeluargaTinggal, cmbTempatTinggal, cmbPekerjaan, cmbMst1, cmbMst2, cmbNortonFisik, cmbNortonMental,
            cmbNortonAktivitas, cmbNortonMobilitas, cmbNortonInkontinensia, cmbAdlMakan, cmbAdlMandi, cmbAdlGrooming,
            cmbAdlBerpakaian, cmbAdlBak, cmbAdlBab, cmbAdlToilet, cmbAdlTransfer, cmbAdlMobilitas, cmbAdlTangga,
            cmbMorseJatuh, cmbMorseDiagnosis, cmbMorseAlat, cmbMorseInfus, cmbMorseJalan, cmbMorseMental,
            cmbRestraintPernah, cmbRestraintPerlu}) {
            c.setSelectedIndex(0);
        }
        cekSnapAlert.setSelected(false);
        cekMasalahAnastesi.setSelected(false);
        cekLukaRujuk.setSelected(false);
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
                + "ifnull(poliklinik.nm_poli,'') as unit,ifnull(rp.p_jawab,'') as p_jawab,ifnull(rp.hubunganpj,'') as hubunganpj,"
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
            System.out.println("Notif identitas dewasa : " + e);
        }
        String[] rk = ruangLantaiKelas(norwt);
        tRuang.setText(rk[0]); tLantai.setText(rk[1]); tKelas.setText(rk[2]);
        muat();
    }

    private String[] ruangLantaiKelas(String norwt) {
        String[] hasil = {"", "", ""};
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select ifnull(bangsal.nm_bangsal,'') as ruang,ifnull(kamar.kelas,'') as kelas "
                + "from kamar_inap inner join kamar on kamar.kd_kamar=kamar_inap.kd_kamar "
                + "inner join bangsal on bangsal.kd_bangsal=kamar.kd_bangsal "
                + "where kamar_inap.no_rawat=? order by kamar_inap.tgl_masuk desc limit 1")) {
            ps.setString(1, norwt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { hasil[0] = rs.getString("ruang"); hasil[2] = rs.getString("kelas"); }
            }
        } catch (Exception e) {
            System.out.println("Notif ruang dewasa : " + e);
        }
        return hasil;
    }

    // ====================== Kolom DB ======================
    private static final String[] KOLOM = {
        "no_rawat", "tanggal", "jam", "ruang", "lantai", "kelas", "diagnosa_medis", "gelang",
        "kondisi_masuk", "via", "nadi", "respirasi", "suhu", "spo2", "td", "td_posisi", "tb", "bb",
        "gcs_e", "gcs_m", "gcs_v", "diagnosa_masuk", "keluhan_utama",
        "alergi", "alergi_lateks", "alergi_obat", "snap_alert", "barang_jenis", "barang_tindakan",
        "riwayat_pasien", "masalah_anastesi", "deskripsi_penyakit", "alkohol", "alkohol_jumlah", "alkohol_jenis",
        "merokok", "merokok_jumlah", "merokok_jenis", "influenza_12bln", "pneumonia_5thn", "vaksinasi_lainnya", "riwayat_keluarga",
        "status_nikah", "keluarga_tinggal", "tempat_tinggal", "pekerjaan", "agama_keyakinan", "aktivitas_psiko",
        "curiga_aniaya", "status_emosional", "keluarga_terdekat", "hubungan_keluarga", "telepon", "informasi_dari",
        "fisik_mata", "fisik_mata_ket", "fisik_kardio", "fisik_kardio_ket", "fisik_gastro", "fisik_gastro_ket",
        "mst_bb", "mst_asupan", "mst_total",
        "fisik_genito", "fisik_genito_ket", "fisik_neuro", "fisik_neuro_ket", "fisik_musculo", "fisik_musculo_ket", "luka_rujuk",
        "norton_fisik", "norton_mental", "norton_aktivitas", "norton_mobilitas", "norton_inkontinensia", "norton_total", "norton_catatan",
        "adl_makan", "adl_mandi", "adl_grooming", "adl_berpakaian", "adl_bak", "adl_bab", "adl_toilet", "adl_transfer",
        "adl_mobilitas", "adl_tangga", "adl_total", "adl_interpretasi",
        "morse_jatuh", "morse_diagnosis", "morse_alat", "morse_infus", "morse_jalan", "morse_mental", "morse_total", "morse_resiko",
        "nyeri_skala", "nyeri_lokasi", "nyeri_onset", "nyeri_variasi", "nyeri_kualitas", "nyeri_pemberat", "nyeri_pencetus", "nyeri_obat", "nyeri_efek",
        "restraint_pernah", "restraint_perlu",
        "komunikasi", "bahasa_harian", "hambatan_belajar", "cara_belajar", "tingkat_pendidikan", "kebutuhan_edukasi",
        "kriteria_discharge", "resume_umum", "resume_rujuk", "rencana_keperawatan", "masalah_keperawatan",
        "tgl_ttd", "jam_ttd", "nik", "kd_dokter"
    };

    private void simpan() {
        if (TNoRw.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        String[][] wajib = {
            {tNadi.getText(), "Nadi"}, {tRespirasi.getText(), "Respirasi"}, {tSuhu.getText(), "Suhu"},
            {tBB.getText(), "Berat Badan"}, {KdPetugas.getText(), "Pelaksana Asesmen"}
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
            TNoRw.getText(), tgl, jam, tRuang.getText(), tLantai.getText(), tKelas.getText(), tDiagnosaMedis.getText(), s(cmbGelang),
            grpKondisiMasuk.get(), grpVia.get(), tNadi.getText(), tRespirasi.getText(), tSuhu.getText(), tSpo2.getText(),
            tTD.getText(), grpTdPosisi.get(), tTB.getText(), tBB.getText(),
            tGcsE.getText(), tGcsM.getText(), tGcsV.getText(), taDiagnosaMasuk.getText(), taKeluhan.getText(),
            s(cmbAlergi), grpLateks.get(), tAlergiObat.getText(), (cekSnapAlert.isSelected() ? "Ya" : ""), grpBarang.get(), grpBarangTindakan.get(),
            grpRiwayatPasien.get(), (cekMasalahAnastesi.isSelected() ? "Ya" : ""), taDeskripsiPenyakit.getText(),
            s(cmbAlkohol), tAlkoholJumlah.getText(), tAlkoholJenis.getText(), s(cmbMerokok), tMerokokJumlah.getText(), tMerokokJenis.getText(),
            s(cmbInfluenza), s(cmbPneumonia), tVaksinasiLain.getText(), grpRiwayatKeluarga.get(),
            s(cmbStatusNikah), s(cmbKeluargaTinggal), s(cmbTempatTinggal), s(cmbPekerjaan), tAgama.getText(), grpAktivitasPsiko.get(),
            grpAniaya.get(), grpEmosional.get(), tKeluargaTerdekat.getText(), tHubungan.getText(), tTelepon.getText(), grpInformasi.get(),
            grpFisikMata.get(), tFisikMataKet.getText(), grpFisikKardio.get(), tFisikKardioKet.getText(), grpFisikGastro.get(), tFisikGastroKet.getText(),
            s(cmbMst1), s(cmbMst2), tMstTotal.getText(),
            grpFisikGenito.get(), tFisikGenitoKet.getText(), grpFisikNeuro.get(), tFisikNeuroKet.getText(), grpFisikMusculo.get(), tFisikMusculoKet.getText(),
            (cekLukaRujuk.isSelected() ? "Ya" : ""),
            s(cmbNortonFisik), s(cmbNortonMental), s(cmbNortonAktivitas), s(cmbNortonMobilitas), s(cmbNortonInkontinensia), tNortonTotal.getText(), tNortonCatatan.getText(),
            s(cmbAdlMakan), s(cmbAdlMandi), s(cmbAdlGrooming), s(cmbAdlBerpakaian), s(cmbAdlBak), s(cmbAdlBab), s(cmbAdlToilet), s(cmbAdlTransfer),
            s(cmbAdlMobilitas), s(cmbAdlTangga), tAdlTotal.getText(), tAdlInterpretasi.getText(),
            s(cmbMorseJatuh), s(cmbMorseDiagnosis), s(cmbMorseAlat), s(cmbMorseInfus), s(cmbMorseJalan), s(cmbMorseMental), tMorseTotal.getText(), tMorseResiko.getText(),
            tNyeriSkala.getText(), tNyeriLokasi.getText(), tNyeriOnset.getText(), tNyeriVariasi.getText(), grpNyeriKualitas.get(), grpNyeriPemberat.get(), grpNyeriPencetus.get(), tNyeriObat.getText(), grpNyeriEfek.get(),
            s(cmbRestraintPernah), s(cmbRestraintPerlu),
            grpKomunikasi.get(), grpBahasa.get(), grpHambatanBelajar.get(), grpCaraBelajar.get(), grpPendidikan.get(), grpEdukasi.get(),
            grpKriteria.get(), grpResumeUmum.get(), grpResumeRujuk.get(), grpRencana.get(), grpMasalah.get(),
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
                "replace into asesmen_keperawatan_dewasa (" + cols + ") values (" + qm + ")")) {
            for (int i = 0; i < nilai.length; i++) {
                ps.setString(i + 1, nilai[i]);
            }
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Asesmen keperawatan dewasa tersimpan.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan.\n" + e.getMessage());
        }
    }

    private void muat() {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select * from asesmen_keperawatan_dewasa where no_rawat=?")) {
            ps.setString(1, TNoRw.getText());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    setTgl(dtpTanggal, rs.getString("tanggal"), rs.getString("jam"));
                    tRuang.setText(g(rs, "ruang")); tLantai.setText(g(rs, "lantai")); tKelas.setText(g(rs, "kelas"));
                    tDiagnosaMedis.setText(g(rs, "diagnosa_medis")); cmbGelang.setSelectedItem(g(rs, "gelang"));
                    grpKondisiMasuk.set(g(rs, "kondisi_masuk")); grpVia.set(g(rs, "via"));
                    tNadi.setText(g(rs, "nadi")); tRespirasi.setText(g(rs, "respirasi")); tSuhu.setText(g(rs, "suhu")); tSpo2.setText(g(rs, "spo2"));
                    tTD.setText(g(rs, "td")); grpTdPosisi.set(g(rs, "td_posisi")); tTB.setText(g(rs, "tb")); tBB.setText(g(rs, "bb"));
                    tGcsE.setText(g(rs, "gcs_e")); tGcsM.setText(g(rs, "gcs_m")); tGcsV.setText(g(rs, "gcs_v"));
                    taDiagnosaMasuk.setText(g(rs, "diagnosa_masuk")); taKeluhan.setText(g(rs, "keluhan_utama"));
                    cmbAlergi.setSelectedItem(g(rs, "alergi")); grpLateks.set(g(rs, "alergi_lateks")); tAlergiObat.setText(g(rs, "alergi_obat"));
                    cekSnapAlert.setSelected("Ya".equalsIgnoreCase(g(rs, "snap_alert"))); grpBarang.set(g(rs, "barang_jenis")); grpBarangTindakan.set(g(rs, "barang_tindakan"));
                    grpRiwayatPasien.set(g(rs, "riwayat_pasien")); cekMasalahAnastesi.setSelected("Ya".equalsIgnoreCase(g(rs, "masalah_anastesi"))); taDeskripsiPenyakit.setText(g(rs, "deskripsi_penyakit"));
                    cmbAlkohol.setSelectedItem(g(rs, "alkohol")); tAlkoholJumlah.setText(g(rs, "alkohol_jumlah")); tAlkoholJenis.setText(g(rs, "alkohol_jenis"));
                    cmbMerokok.setSelectedItem(g(rs, "merokok")); tMerokokJumlah.setText(g(rs, "merokok_jumlah")); tMerokokJenis.setText(g(rs, "merokok_jenis"));
                    cmbInfluenza.setSelectedItem(g(rs, "influenza_12bln")); cmbPneumonia.setSelectedItem(g(rs, "pneumonia_5thn")); tVaksinasiLain.setText(g(rs, "vaksinasi_lainnya")); grpRiwayatKeluarga.set(g(rs, "riwayat_keluarga"));
                    cmbStatusNikah.setSelectedItem(g(rs, "status_nikah")); cmbKeluargaTinggal.setSelectedItem(g(rs, "keluarga_tinggal")); cmbTempatTinggal.setSelectedItem(g(rs, "tempat_tinggal")); cmbPekerjaan.setSelectedItem(g(rs, "pekerjaan"));
                    tAgama.setText(g(rs, "agama_keyakinan")); grpAktivitasPsiko.set(g(rs, "aktivitas_psiko")); grpAniaya.set(g(rs, "curiga_aniaya")); grpEmosional.set(g(rs, "status_emosional"));
                    tKeluargaTerdekat.setText(g(rs, "keluarga_terdekat")); tHubungan.setText(g(rs, "hubungan_keluarga")); tTelepon.setText(g(rs, "telepon")); grpInformasi.set(g(rs, "informasi_dari"));
                    grpFisikMata.set(g(rs, "fisik_mata")); tFisikMataKet.setText(g(rs, "fisik_mata_ket"));
                    grpFisikKardio.set(g(rs, "fisik_kardio")); tFisikKardioKet.setText(g(rs, "fisik_kardio_ket"));
                    grpFisikGastro.set(g(rs, "fisik_gastro")); tFisikGastroKet.setText(g(rs, "fisik_gastro_ket"));
                    cmbMst1.setSelectedItem(g(rs, "mst_bb")); cmbMst2.setSelectedItem(g(rs, "mst_asupan")); tMstTotal.setText(g(rs, "mst_total"));
                    grpFisikGenito.set(g(rs, "fisik_genito")); tFisikGenitoKet.setText(g(rs, "fisik_genito_ket"));
                    grpFisikNeuro.set(g(rs, "fisik_neuro")); tFisikNeuroKet.setText(g(rs, "fisik_neuro_ket"));
                    grpFisikMusculo.set(g(rs, "fisik_musculo")); tFisikMusculoKet.setText(g(rs, "fisik_musculo_ket")); cekLukaRujuk.setSelected("Ya".equalsIgnoreCase(g(rs, "luka_rujuk")));
                    cmbNortonFisik.setSelectedItem(g(rs, "norton_fisik")); cmbNortonMental.setSelectedItem(g(rs, "norton_mental")); cmbNortonAktivitas.setSelectedItem(g(rs, "norton_aktivitas"));
                    cmbNortonMobilitas.setSelectedItem(g(rs, "norton_mobilitas")); cmbNortonInkontinensia.setSelectedItem(g(rs, "norton_inkontinensia")); tNortonTotal.setText(g(rs, "norton_total")); tNortonCatatan.setText(g(rs, "norton_catatan"));
                    cmbAdlMakan.setSelectedItem(g(rs, "adl_makan")); cmbAdlMandi.setSelectedItem(g(rs, "adl_mandi")); cmbAdlGrooming.setSelectedItem(g(rs, "adl_grooming")); cmbAdlBerpakaian.setSelectedItem(g(rs, "adl_berpakaian"));
                    cmbAdlBak.setSelectedItem(g(rs, "adl_bak")); cmbAdlBab.setSelectedItem(g(rs, "adl_bab")); cmbAdlToilet.setSelectedItem(g(rs, "adl_toilet")); cmbAdlTransfer.setSelectedItem(g(rs, "adl_transfer"));
                    cmbAdlMobilitas.setSelectedItem(g(rs, "adl_mobilitas")); cmbAdlTangga.setSelectedItem(g(rs, "adl_tangga")); tAdlTotal.setText(g(rs, "adl_total")); tAdlInterpretasi.setText(g(rs, "adl_interpretasi"));
                    cmbMorseJatuh.setSelectedItem(g(rs, "morse_jatuh")); cmbMorseDiagnosis.setSelectedItem(g(rs, "morse_diagnosis")); cmbMorseAlat.setSelectedItem(g(rs, "morse_alat")); cmbMorseInfus.setSelectedItem(g(rs, "morse_infus"));
                    cmbMorseJalan.setSelectedItem(g(rs, "morse_jalan")); cmbMorseMental.setSelectedItem(g(rs, "morse_mental")); tMorseTotal.setText(g(rs, "morse_total")); tMorseResiko.setText(g(rs, "morse_resiko"));
                    tNyeriSkala.setText(g(rs, "nyeri_skala")); tNyeriLokasi.setText(g(rs, "nyeri_lokasi")); tNyeriOnset.setText(g(rs, "nyeri_onset")); tNyeriVariasi.setText(g(rs, "nyeri_variasi"));
                    grpNyeriKualitas.set(g(rs, "nyeri_kualitas")); grpNyeriPemberat.set(g(rs, "nyeri_pemberat")); grpNyeriPencetus.set(g(rs, "nyeri_pencetus")); tNyeriObat.setText(g(rs, "nyeri_obat")); grpNyeriEfek.set(g(rs, "nyeri_efek"));
                    cmbRestraintPernah.setSelectedItem(g(rs, "restraint_pernah")); cmbRestraintPerlu.setSelectedItem(g(rs, "restraint_perlu"));
                    grpKomunikasi.set(g(rs, "komunikasi")); grpBahasa.set(g(rs, "bahasa_harian")); grpHambatanBelajar.set(g(rs, "hambatan_belajar"));
                    grpCaraBelajar.set(g(rs, "cara_belajar")); grpPendidikan.set(g(rs, "tingkat_pendidikan")); grpEdukasi.set(g(rs, "kebutuhan_edukasi"));
                    grpKriteria.set(g(rs, "kriteria_discharge")); grpResumeUmum.set(g(rs, "resume_umum")); grpResumeRujuk.set(g(rs, "resume_rujuk")); grpRencana.set(g(rs, "rencana_keperawatan")); grpMasalah.set(g(rs, "masalah_keperawatan"));
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
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat dewasa : " + e);
        }
    }

    private void cetak() {
        if (TNoRw.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            param.put("url_penggajian", "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/"
                    + koneksiDB.HYBRIDWEB() + "/penggajian/");

            String sql = "select p.no_rkm_medis,ak.ruang,ak.lantai,p.nm_pasien,ak.kelas,"
                    + "ifnull(date_format(p.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir,"
                    + "if(p.jk='L','Laki-laki','Perempuan') as jk,"
                    + "ak.diagnosa_medis,ak.gelang,"
                    + "ifnull(date_format(ak.tanggal,'%d-%m-%Y'),'') as tanggal,ak.jam,"
                    + "ak.kondisi_masuk,ak.via,ak.nadi,ak.respirasi,ak.suhu,ak.spo2,ak.td,ak.td_posisi,ak.tb,ak.bb,"
                    + "ak.gcs_e,ak.gcs_m,ak.gcs_v,ak.diagnosa_masuk,ak.keluhan_utama,"
                    + "ak.alergi,ak.snap_alert,ak.alergi_lateks,ak.alergi_obat,ak.barang_jenis,ak.barang_tindakan,"
                    + "ak.riwayat_pasien,ak.masalah_anastesi,ak.deskripsi_penyakit,"
                    + "ak.alkohol,ak.alkohol_jumlah,ak.alkohol_jenis,ak.merokok,ak.merokok_jumlah,ak.merokok_jenis,"
                    + "ak.influenza_12bln,ak.pneumonia_5thn,ak.vaksinasi_lainnya,ak.riwayat_keluarga,"
                    + "ak.status_nikah,ak.keluarga_tinggal,ak.tempat_tinggal,ak.pekerjaan,ak.agama_keyakinan,"
                    + "ak.aktivitas_psiko,ak.curiga_aniaya,ak.status_emosional,ak.keluarga_terdekat,ak.hubungan_keluarga,"
                    + "ak.telepon,ak.informasi_dari,"
                    + "ak.fisik_mata,ak.fisik_mata_ket,ak.fisik_kardio,ak.fisik_kardio_ket,ak.fisik_gastro,ak.fisik_gastro_ket,"
                    + "ak.mst_bb,ak.mst_asupan,ak.mst_total,"
                    + "ak.fisik_genito,ak.fisik_genito_ket,ak.fisik_neuro,ak.fisik_neuro_ket,ak.fisik_musculo,ak.fisik_musculo_ket,ak.luka_rujuk,"
                    + "ak.norton_fisik,ak.norton_mental,ak.norton_aktivitas,ak.norton_mobilitas,ak.norton_inkontinensia,ak.norton_total,ak.norton_catatan,"
                    + "ak.adl_makan,ak.adl_mandi,ak.adl_grooming,ak.adl_berpakaian,ak.adl_bak,ak.adl_bab,ak.adl_toilet,ak.adl_transfer,ak.adl_mobilitas,ak.adl_tangga,ak.adl_total,ak.adl_interpretasi,"
                    + "ak.morse_jatuh,ak.morse_diagnosis,ak.morse_alat,ak.morse_infus,ak.morse_jalan,ak.morse_mental,ak.morse_total,ak.morse_resiko,"
                    + "ak.nyeri_skala,ak.nyeri_lokasi,ak.nyeri_onset,ak.nyeri_variasi,ak.nyeri_kualitas,ak.nyeri_pemberat,ak.nyeri_pencetus,ak.nyeri_obat,ak.nyeri_efek,"
                    + "ak.restraint_pernah,ak.restraint_perlu,"
                    + "ak.komunikasi,ak.bahasa_harian,ak.hambatan_belajar,ak.cara_belajar,ak.tingkat_pendidikan,ak.kebutuhan_edukasi,"
                    + "ak.kriteria_discharge,ak.resume_umum,ak.resume_rujuk,ak.rencana_keperawatan,ak.masalah_keperawatan,"
                    + fotoSqlByNip("ak.nik", "perawat_photo") + ","
                    + "ifnull((select nama from petugas where nip=ak.nik),'') as nama_perawat,"
                    + "ifnull((select nm_dokter from dokter where kd_dokter=ak.kd_dokter),'') as nama_dokter,"
                    + "ifnull(date_format(ak.tgl_ttd,'%d-%m-%Y'),'') as tgl_ttd "
                    + "from asesmen_keperawatan_dewasa ak "
                    + "inner join reg_periksa rp on rp.no_rawat=ak.no_rawat "
                    + "inner join pasien p on p.no_rkm_medis=rp.no_rkm_medis "
                    + "where ak.no_rawat='" + TNoRw.getText().trim() + "'";
            Valid.MyReportqry("rptAsesmenKeperawatanDewasa.jasper", "report", "::[ Asesmen Keperawatan Dewasa (RM 5d) ]::", sql, param);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak.\n" + e.getMessage());
        }
    }

    private String fotoSqlByNip(String kolomNip, String alias) {
        String sub = "(select photo from pegawai where nik=" + kolomNip + " limit 1)";
        return "if(coalesce(nullif(" + sub + ",''),'')='' or coalesce(nullif(" + sub + ",''),'')='-' "
                + "or coalesce(nullif(" + sub + ",''),'')='pages/pegawai/photo/','',"
                + "replace(coalesce(" + sub + ",''),'\\\\\\\\','/')) as " + alias;
    }

    /** Cetak langsung dari no_rawat tanpa membuka dialog (dipakai dari klik-kanan di layar Riwayat). */
    public static void cetak(String noRawat) {
        if (noRawat == null || noRawat.trim().isEmpty()) {
            return;
        }
        RMAsesmenKeperawatanDewasa f = new RMAsesmenKeperawatanDewasa(null, false);
        f.isCek();
        f.setNoRm(noRawat.trim(), new Date(), "", null);
        f.cetak();
        f.dispose();
    }

    private void hapus() {
        if (TNoRw.getText().trim().equals("")) { return; }
        if (JOptionPane.showConfirmDialog(this, "Hapus asesmen keperawatan dewasa untuk No.Rawat " + TNoRw.getText() + " ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement("delete from asesmen_keperawatan_dewasa where no_rawat=?")) {
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

        @Override
        public List<String> semuaOpsi() {
            List<String> hasil = new ArrayList<>();
            for (JCheckBox c : boxes) { hasil.add(c.getText()); }
            return hasil;
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
