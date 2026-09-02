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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import kepegawaian.DlgCariDokter;

/**
 * Form Asesmen Kebidanan (rawat inap, RM 5a). Programatik mengikuti pola
 * RMAsesmenKeperawatanAnak/Dewasa. Disimpan ke asesmen_kebidanan
 * (+ _persalinan untuk riwayat persalinan lalu). REPLACE INTO per no_rawat.
 */
public final class RMAsesmenKebidanan extends JDialog {

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
    private final widget.TextArea taKeluhan = ta();

    // Alergi
    private final Grup grpLateks = new Grup("Sarung tangan", "Balon", "Plester", "NGT");
    private final widget.TextBox tAlergiMakananObat = tf();
    private final widget.TextBox tAlergiReaksi = tf();
    private final JCheckBox cekSnapAlert = new JCheckBox("Bila ada alergi, pakaikan Snap Alert Merah");

    // Nyeri
    private final widget.TextBox tNyeriSkala = tf();
    private final widget.TextBox tNyeriLokasi = tf();
    private final widget.TextBox tNyeriOnset = tf();
    private final widget.TextBox tNyeriVariasi = tf();
    private final Grup grpNyeriKualitas = new Grup("Nyeri", "Terbakar", "Menusuk", "Kram", "Tajam", "Tertekan", "Tumpul", "Nyeri tembak");
    private final Grup grpNyeriPemberat = new Grup("Cahaya", "Gelap", "Gerakan", "Berbaring", "Panas");
    private final Grup grpNyeriPencetus = new Grup("Makan", "Sunyi", "Dingin", "Panas");
    private final widget.TextBox tNyeriObat = tf();
    private final Grup grpNyeriEfek = new Grup("Mual/muntah", "Nafsu makan menurun", "Emosi labil", "Gangguan Tidur", "Aktivitas");

    // Psikososial
    private final widget.ComboBox cmbStatusNikah = cmb("Menikah", "Belum menikah", "Duda/janda");
    private final widget.ComboBox cmbKeluargaTinggal = cmb("Tinggal serumah", "Tinggal sendiri");
    private final widget.ComboBox cmbTempatTinggal = cmb("Rumah", "Panti asuhan", "Lainnya");
    private final widget.ComboBox cmbPekerjaan = cmb("Purna waktu", "Paruh waktu", "Pensiun", "Lainnya");
    private final widget.TextBox tAgama = tf();

    // Riwayat menstruasi
    private final widget.TextBox tMenarche = tf();
    private final widget.TextBox tLamaHaid = tf();
    private final widget.TextBox tJumlahDarah = tf();
    private final widget.TextBox tHaidTerakhir = tf();
    private final widget.TextBox tTafsiranPersalinan = tf();
    private final Grup grpGangguanHaid = new Grup("Disminore", "Spoting", "Menorrhagia", "Pre Menstruasi Syndrom");

    // Riwayat perkawinan & kehamilan
    private final widget.TextBox tKawinKe = tf();
    private final widget.TextBox tGpaG = tf();
    private final widget.TextBox tGpaP = tf();
    private final widget.TextBox tGpaA = tf();
    private final widget.TextBox tGpaHidup = tf();
    private final widget.Table tbPersalinan = new widget.Table();
    private final DefaultTableModel modePersalinan = new DefaultTableModel(null, new Object[]{
        "Tgl/Tahun Partus", "Tempat Partus", "Umur Hamil", "Jenis Persalinan", "Penolong", "Penyulit", "Anak Kel/BB", "Keadaan Anak"
    });
    private final Grup grpHamilMuda = new Grup("Mual", "Muntah", "Perdarahan", "Lain-lain (TT I)");
    private final Grup grpHamilTua = new Grup("Pusing", "Sakit kepala", "Perdarahan", "Lain-lain (TT II)");
    private final widget.TextArea taRiwayatOperasi = ta();
    private final Grup grpPenyakitKeluarga = new Grup("Kanker", "Hepatitis", "Hipertensi", "DM", "Penyakit Ginjal",
            "Penyakit jiwa", "Kelainan bawaan", "Hamil kembar", "TBC", "Epilepsi", "Alergi");
    private final Grup grpGynekologi = new Grup("Infertilitas", "Infeksi Virus", "Cervisitis kronis", "Endometriosis",
            "Myoma", "Polip Cervix", "Kanker kandungan", "Operasi kandungan", "Perkosaan", "PMS");
    private final widget.TextBox tKbMetode = tf();
    private final widget.TextBox tKbLama = tf();
    private final Grup grpKbKomplikasi = new Grup("Perdarahan", "PID/Radang Panggul");

    // Pola makan minum & gizi
    private final widget.TextBox tPolaMakan = tf();
    private final widget.TextBox tPolaMinum = tf();
    private final widget.TextBox tPolaKonsumsi = tf();
    private final widget.ComboBox cmbGiziAsupan = cmb("Tidak (0)", "Ya (1)");
    private final widget.ComboBox cmbGiziBb = cmb("Tidak (0)", "Ya (1)");
    private final widget.ComboBox cmbGiziHb = cmb("Tidak (0)", "Ya (1)");
    private final widget.ComboBox cmbGiziMetabolisme = cmb("Tidak (0)", "Ya (1)");
    private final widget.TextBox tGiziTotal = tf();

    // Pola eliminasi & istirahat
    private final widget.TextBox tBakJumlah = tf();
    private final widget.TextBox tBakWarna = tf();
    private final widget.TextBox tBakTerakhir = tf();
    private final widget.TextBox tBabJumlah = tf();
    private final widget.TextBox tBabKarakteristik = tf();
    private final widget.TextBox tBabTerakhir = tf();
    private final widget.TextBox tTidurJam = tf();
    private final widget.TextBox tTidurTerakhir = tf();
    private final widget.TextBox tNilaiKeyakinan = tf();
    private final widget.TextBox tPenerimaanKehamilan = tf();
    private final Grup grpSosialSupport = new Grup("Suami", "Orang Tua", "Mertua", "Keluarga lain");

    // Data obyektif - pemeriksaan fisik
    private final Grup grpFisikMata = new Grup("Pandangan Kabur", "Diplopia", "Sklera ikterik", "Conjungtiva Pucat");
    private final Grup grpFisikDada = new Grup("Mamae simetris", "Mamae asimetris", "Areola Hiperpigmentasi",
            "Putting susu menonjol", "Tumor", "Kolostrum (+)");
    private final Grup grpFisikEkstrimitas = new Grup("Tungkai Symetris", "Edema (+)", "Edema (-)", "Reflek (+)", "Reflek (-)");
    private final Grup grpFisikKardio = new Grup("Dyspneu", "Orthopneu", "Takipneu", "Wheezing", "Batuk", "Sputum",
            "Batuk darah", "Nyeri dada", "Keringat malam");

    // Morse
    private final widget.ComboBox cmbMorseJatuh = cmb("Tidak (0)", "Ya (25)");
    private final widget.ComboBox cmbMorseDiagnosis = cmb("Tidak (0)", "Ya (15)");
    private final widget.ComboBox cmbMorseAlat = cmb("Tidak ada/tirah baring (0)", "Tongkat/penopang (15)", "Perabot (30)");
    private final widget.ComboBox cmbMorseInfus = cmb("Tidak (0)", "Ya (20)");
    private final widget.ComboBox cmbMorseJalan = cmb("Normal/imobilisasi (0)", "Lemah (10)", "Terganggu (20)");
    private final widget.ComboBox cmbMorseMental = cmb("Orientasi baik (0)", "Sering lupa keterbatasan (15)");
    private final widget.TextBox tMorseTotal = tf();
    private final widget.TextBox tMorseResiko = tf();

    // Pemeriksaan khusus & nifas
    private final Grup grpObsInspeksi = new Grup("Membesar arah memanjang", "Melebar", "Pelebaran Vena", "Linea alba",
            "Linea nigra", "Striae livide", "Striae albican", "Luka bekas operasi", "Lain-lain");
    private final widget.TextBox tObsTfu = tf();
    private final widget.TextBox tObsLetakPunggung = tf();
    private final widget.TextBox tObsPresentasi = tf();
    private final Grup grpObsPalpasi = new Grup("Nyeri tekan", "Obsborn test", "Cekungan pada perut");
    private final widget.TextBox tObsTaksiranBb = tf();
    private final widget.TextBox tObsDjj = tf();
    private final widget.ComboBox cmbObsDjjIrama = cmb("-", "Teratur", "Tidak teratur");
    private final widget.TextBox tObsBagianTerendah = tf();
    private final widget.TextBox tObsHis = tf();
    private final widget.ComboBox cmbObsHisIrama = cmb("-", "Teratur", "Tidak Teratur");
    private final Grup grpGynInspeksi = new Grup("Darah", "Lendir", "Air Ketuban");
    private final widget.TextBox tGynVagina = tf();
    private final widget.TextBox tGynPortio = tf();
    private final widget.TextBox tGynVt = tf();
    private final widget.TextBox tGynKesanPanggul = tf();
    private final widget.TextBox tGynImbang = tf();
    private final widget.TextBox tNifasTfu = tf();
    private final widget.TextBox tNifasKontraksi = tf();
    private final widget.TextBox tNifasLochea = tf();
    private final widget.TextBox tNifasLuka = tf();

    // Discharge & perencanaan pulang
    private final Grup grpKriteria = new Grup("Usia lebih dari 60 tahun", "Memiliki hambatan mobilisasi",
            "Membutuhkan pelayanan medis dan keperawatan berkelanjutan", "Tergantung dengan orang lain dalam ADL");
    private final Grup grpPerencanaanPulang = new Grup("Perawatan diri", "Pemantauan pemberian obat", "Pemantauan diet",
            "Membutuhkan perawatan luka", "Latihan fisik lanjutan", "Membutuhkan pendamping tenaga khusus dirumah",
            "Membutuhkan bantuan medis/perawatan rumah", "Membutuhkan bantuan aktivitas fisik");

    // Penunjang
    private final widget.TextBox tPenunjangHb = tf();
    private final widget.TextBox tPenunjangHt = tf();
    private final widget.TextBox tPenunjangUrine = tf();
    private final widget.TextBox tPenunjangCtg = tf();
    private final widget.TextBox tPenunjangUsg = tf();

    private final widget.TextArea taDiagnosa = ta();
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

    public RMAsesmenKebidanan(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Asesmen Kebidanan ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        cekSnapAlert.setOpaque(false);
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

        row = judul(form, row, "A. Data Subyektif - Data Umum");
        row = baris1(form, row, "Tanggal / Jam *", dtpTanggal);
        row = grup(form, row, "Kondisi Saat Masuk", grpKondisiMasuk.panel);
        row = grup(form, row, "Via", grpVia.panel);
        row = baris2(form, row, "Nadi (x/menit) *", tNadi, "Respirasi (x/menit) *", tRespirasi);
        row = baris2(form, row, "Suhu (C) *", tSuhu, "SpO2 (%)", tSpo2);
        row = baris2(form, row, "Tekanan Darah", tTD, "Tinggi Badan (cm)", tTB);
        row = grup(form, row, "Posisi TD", grpTdPosisi.panel);
        row = baris2(form, row, "Berat Badan (kg) *", tBB, "GCS - E", tGcsE);
        row = baris2(form, row, "GCS - M", tGcsM, "GCS - V", tGcsV);
        row = area(form, row, "Keluhan Utama", taKeluhan);

        row = judul(form, row, "Alergi");
        row = grup(form, row, "Lateks", grpLateks.panel);
        row = baris2(form, row, "Makanan / Obat", tAlergiMakananObat, "Jenis Reaksi", tAlergiReaksi);
        row = grup(form, row, "Snap Alert", panelCek(cekSnapAlert));

        row = judul(form, row, "Pemeriksaan Nyeri (NRS)");
        row = baris2(form, row, "Skala Nyeri (0-10)", tNyeriSkala, "Lokasi", tNyeriLokasi);
        row = baris2(form, row, "Onset", tNyeriOnset, "Variasi", tNyeriVariasi);
        row = grup(form, row, "Kualitas", grpNyeriKualitas.panel);
        row = grup(form, row, "Faktor Pemberat", grpNyeriPemberat.panel);
        row = grup(form, row, "Faktor Pencetus", grpNyeriPencetus.panel);
        row = baris1(form, row, "Obat-obatan", tNyeriObat);
        row = grup(form, row, "Efek Nyeri", grpNyeriEfek.panel);

        row = judul(form, row, "Psikososial / Ekonomi / Spiritual");
        row = baris2(form, row, "Status Pernikahan", cmbStatusNikah, "Keluarga", cmbKeluargaTinggal);
        row = baris2(form, row, "Tempat Tinggal", cmbTempatTinggal, "Pekerjaan", cmbPekerjaan);
        row = baris1(form, row, "Agama / Nilai Keyakinan", tAgama);

        row = judul(form, row, "Riwayat Menstruasi");
        row = baris2(form, row, "Umur Menarche", tMenarche, "Lama Haid (hr)", tLamaHaid);
        row = baris2(form, row, "Jumlah Darah (cc)", tJumlahDarah, "Haid Terakhir", tHaidTerakhir);
        row = baris1(form, row, "Tafsiran Persalinan", tTafsiranPersalinan);
        row = grup(form, row, "Gangguan Haid", grpGangguanHaid.panel);

        row = judul(form, row, "Riwayat Perkawinan & Kehamilan");
        row = baris1(form, row, "Kawin Ke", tKawinKe);
        row = baris2(form, row, "G (Gravida)", tGpaG, "P (Partus)", tGpaP);
        row = baris2(form, row, "A (Abortus)", tGpaA, "Hidup", tGpaHidup);
        row = judulKecil(form, row, "Riwayat Persalinan & Nifas Yang Lalu");
        row++;
        row = tabelPersalinan(form, row);
        row = grup(form, row, "Riwayat Hamil Muda", grpHamilMuda.panel);
        row = grup(form, row, "Riwayat Hamil Tua", grpHamilTua.panel);
        row = area(form, row, "Riwayat Penyakit Lalu / Operasi", taRiwayatOperasi);
        row = grup(form, row, "Riwayat Penyakit Keluarga", grpPenyakitKeluarga.panel);
        row = grup(form, row, "Riwayat Gynekologi", grpGynekologi.panel);
        row = baris2(form, row, "KB - Metode", tKbMetode, "KB - Lama", tKbLama);
        row = grup(form, row, "Komplikasi KB", grpKbKomplikasi.panel);

        row = judul(form, row, "Pola Makan, Minum & Skrining Gizi");
        row = baris2(form, row, "Pola Makan (kali/hari)", tPolaMakan, "Pola Minum (cc/hari)", tPolaMinum);
        row = baris1(form, row, "Alkohol / Obat / Jamu / Kopi", tPolaKonsumsi);
        row = baris2(form, row, "Asupan makan berkurang", cmbGiziAsupan, "Pertambahan BB kurang/lebih", cmbGiziBb);
        row = baris2(form, row, "Hb < 10 gr/dl atau HCT < 30%", cmbGiziHb, "Gangguan metabolisme/kondisi khusus", cmbGiziMetabolisme);
        row = baris1(form, row, "Skor Total Gizi", tGiziTotal);

        row = judul(form, row, "Pola Eliminasi & Istirahat");
        row = baris2(form, row, "BAK (cc/hari)", tBakJumlah, "BAK Warna", tBakWarna);
        row = baris2(form, row, "BAK Terakhir (jam)", tBakTerakhir, "BAB (kali/hari)", tBabJumlah);
        row = baris2(form, row, "BAB Karakteristik", tBabKarakteristik, "BAB Terakhir (jam)", tBabTerakhir);
        row = baris2(form, row, "Tidur (jam/hari)", tTidurJam, "Tidur Terakhir (jam)", tTidurTerakhir);
        row = baris1(form, row, "Nilai & Keyakinan", tNilaiKeyakinan);
        row = baris1(form, row, "Penerimaan Klien thd Kehamilan", tPenerimaanKehamilan);
        row = grup(form, row, "Sosial Support Dari", grpSosialSupport.panel);

        row = judul(form, row, "B. Data Obyektif - Pemeriksaan Fisik");
        row = grup(form, row, "Mata", grpFisikMata.panel);
        row = grup(form, row, "Dada & Axylla", grpFisikDada.panel);
        row = grup(form, row, "Ekstrimitas", grpFisikEkstrimitas.panel);
        row = grup(form, row, "Sistem Kardio", grpFisikKardio.panel);

        row = judul(form, row, "Faktor Resiko Jatuh (Morse)");
        row = baris2(form, row, "Riwayat Jatuh", cmbMorseJatuh, "Diagnosis Sekunder", cmbMorseDiagnosis);
        row = baris2(form, row, "Alat Bantu", cmbMorseAlat, "Terpasang Infus", cmbMorseInfus);
        row = baris2(form, row, "Gaya Berjalan", cmbMorseJalan, "Status Mental", cmbMorseMental);
        row = baris2(form, row, "Total Skor Morse", tMorseTotal, "Tingkat Resiko", tMorseResiko);

        row = judul(form, row, "Pemeriksaan Khusus & Nifas");
        row = grup(form, row, "Obstetri - Inspeksi Abdomen", grpObsInspeksi.panel);
        row = baris2(form, row, "TFU (cm)", tObsTfu, "Letak Punggung", tObsLetakPunggung);
        row = baris1(form, row, "Presentasi", tObsPresentasi);
        row = grup(form, row, "Palpasi", grpObsPalpasi.panel);
        row = baris2(form, row, "Taksiran Berat Janin (gr)", tObsTaksiranBb, "DJJ (x/m)", tObsDjj);
        row = baris2(form, row, "DJJ Irama", cmbObsDjjIrama, "Bagian Terendah", tObsBagianTerendah);
        row = baris2(form, row, "His / Kontraksi", tObsHis, "His Irama", cmbObsHisIrama);
        row = grup(form, row, "Gynekologi - Inspeksi Ano Genital", grpGynInspeksi.panel);
        row = baris2(form, row, "Inspekulo Vagina", tGynVagina, "Portio", tGynPortio);
        row = baris1(form, row, "Vagina Toucher", tGynVt);
        row = baris2(form, row, "Kesan Panggul", tGynKesanPanggul, "Imbang Feto Pelvic", tGynImbang);
        row = baris2(form, row, "Nifas - TFU", tNifasTfu, "Kontraksi Uteri", tNifasKontraksi);
        row = baris2(form, row, "Lochea", tNifasLochea, "Luka Jalan Lahir", tNifasLuka);

        row = judul(form, row, "Discharge Planning & Perencanaan Pulang");
        row = grup(form, row, "Kriteria Discharge Planning", grpKriteria.panel);
        row = grup(form, row, "Perencanaan Pulang", grpPerencanaanPulang.panel);

        row = judul(form, row, "Pemeriksaan Penunjang");
        row = baris2(form, row, "Darah HB (gr%)", tPenunjangHb, "Ht", tPenunjangHt);
        row = baris1(form, row, "Urine Protein", tPenunjangUrine);
        row = baris2(form, row, "CTG", tPenunjangCtg, "USG", tPenunjangUsg);

        row = judul(form, row, "Diagnosa & Rencana Kebidanan");
        row = area(form, row, "Diagnosa Kebidanan & Masalah", taDiagnosa);
        row = area(form, row, "Rencana Kebidanan", taRencana);

        row = judul(form, row, "Tanda Tangan");
        row = baris1(form, row, "Tanggal / Pukul", dtpTtd);
        row = baris2(form, row, "Bidan Pengkaji *", gabungBtn(KdPetugas, NmPetugas, null), "Dokter PJ", gabungBtn(KdDokter, NmDokter, BtnDokter));

        getContentPane().add(buatTampilanBertahap(form), BorderLayout.CENTER);

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
        bawah.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, new Color(214, 224, 230)));
        bawah.add(BtnHapus);
        bawah.add(BtnBaru);
        bawah.add(BtnCetak);
        bawah.add(BtnKeluar);
        bawah.add(BtnSimpan);
        getContentPane().add(bawah, BorderLayout.SOUTH);

        dtpTanggal.setDate(new Date());
        dtpTtd.setDate(new Date());
    }

    private JPanel buatTampilanBertahap(JPanel formLama) {
        final Color utama = new Color(0, 133, 143);
        final Color utamaMuda = new Color(230, 247, 248);
        final Color latar = new Color(246, 249, 251);
        final Color garis = new Color(214, 224, 230);

        GridBagLayout layoutLama = (GridBagLayout) formLama.getLayout();
        Component[] komponenAwal = formLama.getComponents();
        java.util.Map<Component, GridBagConstraints> posisi = new java.util.HashMap<>();
        for (Component komponen : komponenAwal) {
            posisi.put(komponen, layoutLama.getConstraints(komponen));
        }

        JPanel hasil = new JPanel(new BorderLayout());
        hasil.setBackground(latar);

        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setBackground(latar);
        header.setBorder(new EmptyBorder(12, 16, 9, 16));
        JPanel blokJudul = new JPanel();
        blokJudul.setOpaque(false);
        blokJudul.setLayout(new BoxLayout(blokJudul, BoxLayout.Y_AXIS));
        JLabel judulUtama = new JLabel("Asesmen Kebidanan");
        judulUtama.setFont(new Font("Tahoma", Font.BOLD, 20));
        judulUtama.setForeground(new Color(31, 47, 62));
        JLabel subJudul = new JLabel("Asesmen awal pasien kebidanan rawat inap");
        subJudul.setFont(new Font("Tahoma", Font.PLAIN, 12));
        subJudul.setForeground(new Color(87, 102, 113));
        blokJudul.add(judulUtama);
        blokJudul.add(Box.createVerticalStrut(2));
        blokJudul.add(subJudul);
        header.add(blokJudul, BorderLayout.NORTH);

        JPanel identitas = new JPanel(new GridLayout(2, 4, 0, 6));
        identitas.setBackground(Color.WHITE);
        identitas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis), new EmptyBorder(8, 8, 8, 8)));
        identitas.add(ringkasanPasien("No. Rawat *", TNoRw));
        identitas.add(ringkasanPasien("No. RM", TNoRM));
        identitas.add(ringkasanPasien("Nama Pasien", TPasien));
        identitas.add(ringkasanPasien("Jenis Kelamin", TJK));
        identitas.add(ringkasanPasien("Tanggal Lahir", TTglLahir));
        identitas.add(ringkasanPasien("Unit", TUnit));
        identitas.add(ringkasanPasien("Cara Bayar", TCaraBayar));
        identitas.add(ringkasanPasien("Alamat", TAlamat));
        header.add(identitas, BorderLayout.CENTER);
        hasil.add(header, BorderLayout.NORTH);

        // Buang label identitas lama; field identitasnya sudah dipindahkan ke header.
        for (Component komponen : komponenAwal) {
            GridBagConstraints g = posisi.get(komponen);
            if (g.gridy <= 3 && komponen.getParent() == formLama) {
                formLama.remove(komponen);
            }
        }

        int[] awal = {
            4,
            barisJudul(posisi, komponenAwal, "Pemeriksaan Nyeri (NRS)"),
            barisJudul(posisi, komponenAwal, "Riwayat Menstruasi"),
            barisJudul(posisi, komponenAwal, "Pola Makan, Minum & Skrining Gizi"),
            barisJudul(posisi, komponenAwal, "B. Data Obyektif - Pemeriksaan Fisik"),
            barisJudul(posisi, komponenAwal, "Pemeriksaan Khusus & Nifas"),
            barisJudul(posisi, komponenAwal, "Discharge Planning & Perencanaan Pulang"),
            barisJudul(posisi, komponenAwal, "Diagnosa & Rencana Kebidanan")
        };
        String[] nama = {
            "1  Informasi & Tanda Vital",
            "2  Nyeri & Psikososial",
            "3  Riwayat Reproduksi",
            "4  Nutrisi & Eliminasi",
            "5  Pemeriksaan Fisik",
            "6  Obstetri & Nifas",
            "7  Pulang & Penunjang",
            "8  Diagnosa & Verifikasi"
        };
        String[] kunci = {
            "UMUM", "NYERI", "REPRODUKSI", "NUTRISI",
            "FISIK", "OBSTETRI", "PULANG", "VERIFIKASI"
        };

        JPanel[] halaman = new JPanel[nama.length];
        for (int i = 0; i < halaman.length; i++) {
            halaman[i] = new JPanel(new GridBagLayout());
            halaman[i].setBackground(Color.WHITE);
            halaman[i].setBorder(new EmptyBorder(5, 8, 16, 8));
        }

        for (Component komponen : komponenAwal) {
            if (komponen.getParent() != formLama) {
                continue;
            }
            GridBagConstraints lama = posisi.get(komponen);
            int bagian = 0;
            for (int i = 1; i < awal.length; i++) {
                if (lama.gridy >= awal[i]) {
                    bagian = i;
                }
            }
            GridBagConstraints baru = (GridBagConstraints) lama.clone();
            baru.gridy = lama.gridy - awal[bagian];
            halaman[bagian].add(komponen, baru);
        }

        final CardLayout kartu = new CardLayout();
        final JPanel isi = new JPanel(kartu);
        isi.setBackground(latar);
        for (int i = 0; i < halaman.length; i++) {
            JScrollPane scroll = new JScrollPane(halaman[i]);
            scroll.setBorder(null);
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getVerticalScrollBar().setUnitIncrement(26);
            scroll.getVerticalScrollBar().setBlockIncrement(120);
            scroll.getViewport().setBackground(Color.WHITE);
            isi.add(scroll, kunci[i]);
        }

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

        JButton[] tombol = new JButton[nama.length];
        for (int i = 0; i < nama.length; i++) {
            final int indeks = i;
            tombol[i] = new JButton(nama[i]);
            tombol[i].setHorizontalAlignment(SwingConstants.LEFT);
            tombol[i].setFont(new Font("Tahoma", i == 0 ? Font.BOLD : Font.PLAIN, 11));
            tombol[i].setForeground(i == 0 ? utama : new Color(61, 76, 86));
            tombol[i].setBackground(i == 0 ? utamaMuda : Color.WHITE);
            tombol[i].setBorder(new EmptyBorder(9, 16, 9, 7));
            tombol[i].setFocusPainted(false);
            tombol[i].setMaximumSize(new Dimension(225, 38));
            tombol[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            tombol[i].addActionListener(e -> {
                kartu.show(isi, kunci[indeks]);
                for (int j = 0; j < tombol.length; j++) {
                    boolean aktif = j == indeks;
                    tombol[j].setBackground(aktif ? utamaMuda : Color.WHITE);
                    tombol[j].setForeground(aktif ? utama : new Color(61, 76, 86));
                    tombol[j].setFont(new Font("Tahoma", aktif ? Font.BOLD : Font.PLAIN, 11));
                }
            });
            navigasi.add(tombol[i]);
            navigasi.add(Box.createVerticalStrut(2));
        }
        navigasi.add(Box.createVerticalGlue());
        JLabel keterangan = new JLabel(
                "<html><span style='color:#D32F2F'>*</span> Wajib diisi</html>");
        keterangan.setFont(new Font("Tahoma", Font.PLAIN, 10));
        keterangan.setForeground(new Color(85, 99, 108));
        keterangan.setBorder(new EmptyBorder(8, 16, 14, 8));
        keterangan.setAlignmentX(Component.LEFT_ALIGNMENT);
        navigasi.add(keterangan);

        JPanel badan = new JPanel(new BorderLayout());
        badan.setBackground(latar);
        badan.add(navigasi, BorderLayout.WEST);
        badan.add(isi, BorderLayout.CENTER);
        hasil.add(badan, BorderLayout.CENTER);
        return hasil;
    }

    private int barisJudul(java.util.Map<Component, GridBagConstraints> posisi,
            Component[] komponen, String teks) {
        for (Component item : komponen) {
            if (item instanceof JLabel && teks.equals(((JLabel) item).getText())) {
                return posisi.get(item).gridy;
            }
        }
        throw new IllegalStateException("Bagian form tidak ditemukan: " + teks);
    }

    private JPanel ringkasanPasien(String label, Component komponen) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(0, 8, 0, 8));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel judul = new JLabel(label);
        judul.setFont(new Font("Tahoma", Font.PLAIN, 10));
        judul.setForeground(label.contains("*")
                ? new Color(198, 40, 40) : new Color(82, 97, 108));
        judul.setAlignmentX(Component.LEFT_ALIGNMENT);
        komponen.setPreferredSize(new Dimension(180, 25));
        komponen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        komponen.setBackground(new Color(247, 249, 250));
        panel.add(judul);
        panel.add(Box.createVerticalStrut(3));
        panel.add(komponen);
        return panel;
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
        l.setBackground(new Color(230, 247, 248));
        l.setForeground(new Color(0, 120, 130));
        l.setFont(new Font("Tahoma", Font.BOLD, 12));
        l.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));
        GridBagConstraints g = gc(0, row, 4, 1.0);
        g.insets = new Insets(10, 4, 2, 4);
        p.add(l, g);
        return row + 1;
    }

    private int judulKecil(JPanel p, int row, String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font("Tahoma", Font.BOLD, 11));
        p.add(l, gc(0, row, 4, 1.0));
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

    private int tabelPersalinan(JPanel p, int row) {
        tbPersalinan.setModel(modePersalinan);
        JScrollPane sc = new JScrollPane(tbPersalinan);
        sc.setPreferredSize(new Dimension(700, 90));
        sc.setWheelScrollingEnabled(false);
        p.add(sc, gc(0, row, 4, 1.0));
        row++;
        JButton bt = new JButton("Tambah Baris");
        JButton bh = new JButton("Hapus Baris");
        bt.addActionListener(e -> modePersalinan.addRow(new Object[]{"", "", "", "", "", "", "", ""}));
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

    // ====================== Entry points ======================
    public void isCek() {
        // Boleh Simpan kalau punya izin perawat (penilaian_awal_keperawatan_ranap) ATAU izin
        // dokter (booking_operasi) -- dokter juga perlu bisa mengisi asesmen ini di lapangan.
        boolean bisa = akses.getpenilaian_awal_keperawatan_ranap() || akses.getbooking_operasi();
        BtnSimpan.setEnabled(bisa);
        BtnHapus.setEnabled(bisa);
        KdPetugas.setText(akses.getkode());
        NmPetugas.setText(Sequel.cariIsi("select nama from petugas where nip=?", akses.getkode()));
    }

    public void emptTeks() {
        for (widget.TextBox t : new widget.TextBox[]{tRuang, tLantai, tKelas, tDiagnosaMedis, tNadi, tRespirasi, tSuhu, tSpo2,
            tTD, tTB, tBB, tGcsE, tGcsM, tGcsV, tAlergiMakananObat, tAlergiReaksi, tNyeriSkala, tNyeriLokasi, tNyeriOnset, tNyeriVariasi, tNyeriObat,
            tAgama, tMenarche, tLamaHaid, tJumlahDarah, tHaidTerakhir, tTafsiranPersalinan, tKawinKe, tGpaG, tGpaP, tGpaA, tGpaHidup,
            tKbMetode, tKbLama, tPolaMakan, tPolaMinum, tPolaKonsumsi, tGiziTotal, tBakJumlah, tBakWarna, tBakTerakhir, tBabJumlah,
            tBabKarakteristik, tBabTerakhir, tTidurJam, tTidurTerakhir, tNilaiKeyakinan, tPenerimaanKehamilan, tMorseTotal, tMorseResiko,
            tObsTfu, tObsLetakPunggung, tObsPresentasi, tObsTaksiranBb, tObsDjj, tObsBagianTerendah, tObsHis, tGynVagina, tGynPortio,
            tGynVt, tGynKesanPanggul, tGynImbang, tNifasTfu, tNifasKontraksi, tNifasLochea, tNifasLuka, tPenunjangHb, tPenunjangHt,
            tPenunjangUrine, tPenunjangCtg, tPenunjangUsg, KdDokter, NmDokter}) {
            t.setText("");
        }
        for (widget.TextArea a : new widget.TextArea[]{taKeluhan, taRiwayatOperasi, taDiagnosa, taRencana}) {
            a.setText("");
        }
        for (Grup g : new Grup[]{grpKondisiMasuk, grpVia, grpTdPosisi, grpLateks, grpNyeriKualitas, grpNyeriPemberat,
            grpNyeriPencetus, grpNyeriEfek, grpGangguanHaid, grpHamilMuda, grpHamilTua, grpPenyakitKeluarga, grpGynekologi,
            grpKbKomplikasi, grpSosialSupport, grpFisikMata, grpFisikDada, grpFisikEkstrimitas, grpFisikKardio, grpObsInspeksi,
            grpObsPalpasi, grpGynInspeksi, grpKriteria, grpPerencanaanPulang}) {
            g.clear();
        }
        for (widget.ComboBox c : new widget.ComboBox[]{cmbGelang, cmbStatusNikah, cmbKeluargaTinggal, cmbTempatTinggal, cmbPekerjaan,
            cmbGiziAsupan, cmbGiziBb, cmbGiziHb, cmbGiziMetabolisme, cmbMorseJatuh, cmbMorseDiagnosis, cmbMorseAlat, cmbMorseInfus,
            cmbMorseJalan, cmbMorseMental, cmbObsDjjIrama, cmbObsHisIrama}) {
            c.setSelectedIndex(0);
        }
        cekSnapAlert.setSelected(false);
        Valid.tabelKosong(modePersalinan);
        dtpTanggal.setDate(new Date());
        dtpTtd.setDate(new Date());
    }

    public void setNoRm(String norwt, Date tgl2, String carabayar, String norm) {
        emptTeks();
        TNoRw.setText(norwt);
        TNoRM.setText(norm);
        TCaraBayar.setText(carabayar);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select p.nm_pasien,p.no_rkm_medis,p.jk,p.tgl_lahir,p.alamat,ifnull(p.agama,'') as agama,"
                + "ifnull(poliklinik.nm_poli,'') as unit,ifnull(pj.png_jawab,'') as carabayar "
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
                    if (rs.getString("carabayar") != null && !rs.getString("carabayar").trim().equals("")) {
                        TCaraBayar.setText(rs.getString("carabayar"));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif identitas kebidanan : " + e);
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select ifnull(bangsal.nm_bangsal,'') as ruang,ifnull(kamar.kelas,'') as kelas "
                + "from kamar_inap inner join kamar on kamar.kd_kamar=kamar_inap.kd_kamar "
                + "inner join bangsal on bangsal.kd_bangsal=kamar.kd_bangsal "
                + "where kamar_inap.no_rawat=? order by kamar_inap.tgl_masuk desc limit 1")) {
            ps.setString(1, norwt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { tRuang.setText(rs.getString("ruang")); tKelas.setText(rs.getString("kelas")); }
            }
        } catch (Exception e) {
            System.out.println("Notif ruang kebidanan : " + e);
        }
        muat();
    }

    // ====================== Kolom DB ======================
    private static final String[] KOLOM = {
        "no_rawat", "tanggal", "jam", "ruang", "lantai", "kelas", "diagnosa_medis", "gelang",
        "kondisi_masuk", "via", "nadi", "respirasi", "suhu", "spo2", "td", "td_posisi", "tb", "bb",
        "gcs_e", "gcs_m", "gcs_v", "keluhan_utama",
        "alergi_lateks", "alergi_makanan_obat", "alergi_reaksi", "snap_alert",
        "nyeri_skala", "nyeri_lokasi", "nyeri_onset", "nyeri_variasi", "nyeri_kualitas", "nyeri_pemberat", "nyeri_pencetus", "nyeri_obat", "nyeri_efek",
        "status_nikah", "keluarga_tinggal", "tempat_tinggal", "pekerjaan", "agama_keyakinan",
        "menarche", "lama_haid", "jumlah_darah", "haid_terakhir", "tafsiran_persalinan", "gangguan_haid",
        "kawin_ke", "gpa_g", "gpa_p", "gpa_a", "gpa_hidup", "hamil_muda", "hamil_tua", "riwayat_operasi",
        "riwayat_penyakit_keluarga", "riwayat_gynekologi", "kb_metode", "kb_lama", "kb_komplikasi",
        "pola_makan", "pola_minum", "pola_konsumsi", "gizi_asupan", "gizi_bb", "gizi_hb", "gizi_metabolisme", "gizi_total",
        "bak_jumlah", "bak_warna", "bak_terakhir", "bab_jumlah", "bab_karakteristik", "bab_terakhir", "tidur_jam", "tidur_terakhir",
        "nilai_keyakinan", "penerimaan_kehamilan", "sosial_support",
        "fisik_mata", "fisik_dada", "fisik_ekstrimitas", "fisik_kardio",
        "morse_jatuh", "morse_diagnosis", "morse_alat", "morse_infus", "morse_jalan", "morse_mental", "morse_total", "morse_resiko",
        "obs_inspeksi", "obs_tfu", "obs_letak_punggung", "obs_presentasi", "obs_palpasi", "obs_taksiran_bb", "obs_djj", "obs_djj_irama",
        "obs_bagian_terendah", "obs_his", "obs_his_irama",
        "gyn_inspeksi", "gyn_vagina", "gyn_portio", "gyn_vt", "gyn_kesan_panggul", "gyn_imbang",
        "nifas_tfu", "nifas_kontraksi", "nifas_lochea", "nifas_luka",
        "kriteria_discharge", "perencanaan_pulang",
        "penunjang_hb", "penunjang_ht", "penunjang_urine", "penunjang_ctg", "penunjang_usg",
        "diagnosa_kebidanan", "rencana_kebidanan",
        "tgl_ttd", "jam_ttd", "nik", "kd_dokter"
    };

    private void simpan() {
        if (TNoRw.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        String[][] wajib = {
            {tNadi.getText(), "Nadi"}, {tRespirasi.getText(), "Respirasi"}, {tSuhu.getText(), "Suhu"},
            {tBB.getText(), "Berat Badan"}, {KdPetugas.getText(), "Bidan Pengkaji"}
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
            tGcsE.getText(), tGcsM.getText(), tGcsV.getText(), taKeluhan.getText(),
            grpLateks.get(), tAlergiMakananObat.getText(), tAlergiReaksi.getText(), (cekSnapAlert.isSelected() ? "Ya" : ""),
            tNyeriSkala.getText(), tNyeriLokasi.getText(), tNyeriOnset.getText(), tNyeriVariasi.getText(), grpNyeriKualitas.get(), grpNyeriPemberat.get(), grpNyeriPencetus.get(), tNyeriObat.getText(), grpNyeriEfek.get(),
            s(cmbStatusNikah), s(cmbKeluargaTinggal), s(cmbTempatTinggal), s(cmbPekerjaan), tAgama.getText(),
            tMenarche.getText(), tLamaHaid.getText(), tJumlahDarah.getText(), tHaidTerakhir.getText(), tTafsiranPersalinan.getText(), grpGangguanHaid.get(),
            tKawinKe.getText(), tGpaG.getText(), tGpaP.getText(), tGpaA.getText(), tGpaHidup.getText(), grpHamilMuda.get(), grpHamilTua.get(), taRiwayatOperasi.getText(),
            grpPenyakitKeluarga.get(), grpGynekologi.get(), tKbMetode.getText(), tKbLama.getText(), grpKbKomplikasi.get(),
            tPolaMakan.getText(), tPolaMinum.getText(), tPolaKonsumsi.getText(), s(cmbGiziAsupan), s(cmbGiziBb), s(cmbGiziHb), s(cmbGiziMetabolisme), tGiziTotal.getText(),
            tBakJumlah.getText(), tBakWarna.getText(), tBakTerakhir.getText(), tBabJumlah.getText(), tBabKarakteristik.getText(), tBabTerakhir.getText(), tTidurJam.getText(), tTidurTerakhir.getText(),
            tNilaiKeyakinan.getText(), tPenerimaanKehamilan.getText(), grpSosialSupport.get(),
            grpFisikMata.get(), grpFisikDada.get(), grpFisikEkstrimitas.get(), grpFisikKardio.get(),
            s(cmbMorseJatuh), s(cmbMorseDiagnosis), s(cmbMorseAlat), s(cmbMorseInfus), s(cmbMorseJalan), s(cmbMorseMental), tMorseTotal.getText(), tMorseResiko.getText(),
            grpObsInspeksi.get(), tObsTfu.getText(), tObsLetakPunggung.getText(), tObsPresentasi.getText(), grpObsPalpasi.get(), tObsTaksiranBb.getText(), tObsDjj.getText(), s(cmbObsDjjIrama),
            tObsBagianTerendah.getText(), tObsHis.getText(), s(cmbObsHisIrama),
            grpGynInspeksi.get(), tGynVagina.getText(), tGynPortio.getText(), tGynVt.getText(), tGynKesanPanggul.getText(), tGynImbang.getText(),
            tNifasTfu.getText(), tNifasKontraksi.getText(), tNifasLochea.getText(), tNifasLuka.getText(),
            grpKriteria.get(), grpPerencanaanPulang.get(),
            tPenunjangHb.getText(), tPenunjangHt.getText(), tPenunjangUrine.getText(), tPenunjangCtg.getText(), tPenunjangUsg.getText(),
            taDiagnosa.getText(), taRencana.getText(),
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
                "replace into asesmen_kebidanan (" + cols + ") values (" + qm + ")")) {
            for (int i = 0; i < nilai.length; i++) {
                ps.setString(i + 1, nilai[i]);
            }
            ps.executeUpdate();
            simpanPersalinan();
            JOptionPane.showMessageDialog(this, "Asesmen kebidanan tersimpan.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan.\n" + e.getMessage());
        }
    }

    private void simpanPersalinan() {
        try (PreparedStatement del = koneksi.prepareStatement("delete from asesmen_kebidanan_persalinan where no_rawat=?")) {
            del.setString(1, TNoRw.getText());
            del.executeUpdate();
        } catch (Exception e) {
            System.out.println("Notif del persalinan kebidanan : " + e);
        }
        for (int i = 0; i < modePersalinan.getRowCount(); i++) {
            try (PreparedStatement ins = koneksi.prepareStatement(
                    "insert into asesmen_kebidanan_persalinan "
                    + "(no_rawat,urut,tgl_partus,tempat_partus,umur_hamil,jenis_persalinan,penolong,penyulit,anak,keadaan_anak) "
                    + "values (?,?,?,?,?,?,?,?,?,?)")) {
                ins.setString(1, TNoRw.getText());
                ins.setInt(2, i + 1);
                for (int c = 0; c < 8; c++) {
                    Object o = modePersalinan.getValueAt(i, c);
                    ins.setString(c + 3, o == null ? "" : o.toString());
                }
                ins.executeUpdate();
            } catch (Exception e) {
                System.out.println("Notif ins persalinan kebidanan : " + e);
            }
        }
    }

    private void muat() {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select * from asesmen_kebidanan where no_rawat=?")) {
            ps.setString(1, TNoRw.getText());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    setTgl(dtpTanggal, rs.getString("tanggal"), rs.getString("jam"));
                    tRuang.setText(g(rs, "ruang")); tLantai.setText(g(rs, "lantai")); tKelas.setText(g(rs, "kelas"));
                    tDiagnosaMedis.setText(g(rs, "diagnosa_medis")); cmbGelang.setSelectedItem(g(rs, "gelang"));
                    grpKondisiMasuk.set(g(rs, "kondisi_masuk")); grpVia.set(g(rs, "via"));
                    tNadi.setText(g(rs, "nadi")); tRespirasi.setText(g(rs, "respirasi")); tSuhu.setText(g(rs, "suhu")); tSpo2.setText(g(rs, "spo2"));
                    tTD.setText(g(rs, "td")); grpTdPosisi.set(g(rs, "td_posisi")); tTB.setText(g(rs, "tb")); tBB.setText(g(rs, "bb"));
                    tGcsE.setText(g(rs, "gcs_e")); tGcsM.setText(g(rs, "gcs_m")); tGcsV.setText(g(rs, "gcs_v")); taKeluhan.setText(g(rs, "keluhan_utama"));
                    grpLateks.set(g(rs, "alergi_lateks")); tAlergiMakananObat.setText(g(rs, "alergi_makanan_obat")); tAlergiReaksi.setText(g(rs, "alergi_reaksi")); cekSnapAlert.setSelected("Ya".equalsIgnoreCase(g(rs, "snap_alert")));
                    tNyeriSkala.setText(g(rs, "nyeri_skala")); tNyeriLokasi.setText(g(rs, "nyeri_lokasi")); tNyeriOnset.setText(g(rs, "nyeri_onset")); tNyeriVariasi.setText(g(rs, "nyeri_variasi"));
                    grpNyeriKualitas.set(g(rs, "nyeri_kualitas")); grpNyeriPemberat.set(g(rs, "nyeri_pemberat")); grpNyeriPencetus.set(g(rs, "nyeri_pencetus")); tNyeriObat.setText(g(rs, "nyeri_obat")); grpNyeriEfek.set(g(rs, "nyeri_efek"));
                    cmbStatusNikah.setSelectedItem(g(rs, "status_nikah")); cmbKeluargaTinggal.setSelectedItem(g(rs, "keluarga_tinggal")); cmbTempatTinggal.setSelectedItem(g(rs, "tempat_tinggal")); cmbPekerjaan.setSelectedItem(g(rs, "pekerjaan")); tAgama.setText(g(rs, "agama_keyakinan"));
                    tMenarche.setText(g(rs, "menarche")); tLamaHaid.setText(g(rs, "lama_haid")); tJumlahDarah.setText(g(rs, "jumlah_darah")); tHaidTerakhir.setText(g(rs, "haid_terakhir")); tTafsiranPersalinan.setText(g(rs, "tafsiran_persalinan")); grpGangguanHaid.set(g(rs, "gangguan_haid"));
                    tKawinKe.setText(g(rs, "kawin_ke")); tGpaG.setText(g(rs, "gpa_g")); tGpaP.setText(g(rs, "gpa_p")); tGpaA.setText(g(rs, "gpa_a")); tGpaHidup.setText(g(rs, "gpa_hidup"));
                    grpHamilMuda.set(g(rs, "hamil_muda")); grpHamilTua.set(g(rs, "hamil_tua")); taRiwayatOperasi.setText(g(rs, "riwayat_operasi"));
                    grpPenyakitKeluarga.set(g(rs, "riwayat_penyakit_keluarga")); grpGynekologi.set(g(rs, "riwayat_gynekologi")); tKbMetode.setText(g(rs, "kb_metode")); tKbLama.setText(g(rs, "kb_lama")); grpKbKomplikasi.set(g(rs, "kb_komplikasi"));
                    tPolaMakan.setText(g(rs, "pola_makan")); tPolaMinum.setText(g(rs, "pola_minum")); tPolaKonsumsi.setText(g(rs, "pola_konsumsi"));
                    cmbGiziAsupan.setSelectedItem(g(rs, "gizi_asupan")); cmbGiziBb.setSelectedItem(g(rs, "gizi_bb")); cmbGiziHb.setSelectedItem(g(rs, "gizi_hb")); cmbGiziMetabolisme.setSelectedItem(g(rs, "gizi_metabolisme")); tGiziTotal.setText(g(rs, "gizi_total"));
                    tBakJumlah.setText(g(rs, "bak_jumlah")); tBakWarna.setText(g(rs, "bak_warna")); tBakTerakhir.setText(g(rs, "bak_terakhir")); tBabJumlah.setText(g(rs, "bab_jumlah")); tBabKarakteristik.setText(g(rs, "bab_karakteristik")); tBabTerakhir.setText(g(rs, "bab_terakhir")); tTidurJam.setText(g(rs, "tidur_jam")); tTidurTerakhir.setText(g(rs, "tidur_terakhir"));
                    tNilaiKeyakinan.setText(g(rs, "nilai_keyakinan")); tPenerimaanKehamilan.setText(g(rs, "penerimaan_kehamilan")); grpSosialSupport.set(g(rs, "sosial_support"));
                    grpFisikMata.set(g(rs, "fisik_mata")); grpFisikDada.set(g(rs, "fisik_dada")); grpFisikEkstrimitas.set(g(rs, "fisik_ekstrimitas")); grpFisikKardio.set(g(rs, "fisik_kardio"));
                    cmbMorseJatuh.setSelectedItem(g(rs, "morse_jatuh")); cmbMorseDiagnosis.setSelectedItem(g(rs, "morse_diagnosis")); cmbMorseAlat.setSelectedItem(g(rs, "morse_alat")); cmbMorseInfus.setSelectedItem(g(rs, "morse_infus"));
                    cmbMorseJalan.setSelectedItem(g(rs, "morse_jalan")); cmbMorseMental.setSelectedItem(g(rs, "morse_mental")); tMorseTotal.setText(g(rs, "morse_total")); tMorseResiko.setText(g(rs, "morse_resiko"));
                    grpObsInspeksi.set(g(rs, "obs_inspeksi")); tObsTfu.setText(g(rs, "obs_tfu")); tObsLetakPunggung.setText(g(rs, "obs_letak_punggung")); tObsPresentasi.setText(g(rs, "obs_presentasi")); grpObsPalpasi.set(g(rs, "obs_palpasi"));
                    tObsTaksiranBb.setText(g(rs, "obs_taksiran_bb")); tObsDjj.setText(g(rs, "obs_djj")); cmbObsDjjIrama.setSelectedItem(g(rs, "obs_djj_irama")); tObsBagianTerendah.setText(g(rs, "obs_bagian_terendah")); tObsHis.setText(g(rs, "obs_his")); cmbObsHisIrama.setSelectedItem(g(rs, "obs_his_irama"));
                    grpGynInspeksi.set(g(rs, "gyn_inspeksi")); tGynVagina.setText(g(rs, "gyn_vagina")); tGynPortio.setText(g(rs, "gyn_portio")); tGynVt.setText(g(rs, "gyn_vt")); tGynKesanPanggul.setText(g(rs, "gyn_kesan_panggul")); tGynImbang.setText(g(rs, "gyn_imbang"));
                    tNifasTfu.setText(g(rs, "nifas_tfu")); tNifasKontraksi.setText(g(rs, "nifas_kontraksi")); tNifasLochea.setText(g(rs, "nifas_lochea")); tNifasLuka.setText(g(rs, "nifas_luka"));
                    grpKriteria.set(g(rs, "kriteria_discharge")); grpPerencanaanPulang.set(g(rs, "perencanaan_pulang"));
                    tPenunjangHb.setText(g(rs, "penunjang_hb")); tPenunjangHt.setText(g(rs, "penunjang_ht")); tPenunjangUrine.setText(g(rs, "penunjang_urine")); tPenunjangCtg.setText(g(rs, "penunjang_ctg")); tPenunjangUsg.setText(g(rs, "penunjang_usg"));
                    taDiagnosa.setText(g(rs, "diagnosa_kebidanan")); taRencana.setText(g(rs, "rencana_kebidanan"));
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
            System.out.println("Notif muat kebidanan : " + e);
        }
        muatPersalinan();
    }

    private void muatPersalinan() {
        Valid.tabelKosong(modePersalinan);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select tgl_partus,tempat_partus,umur_hamil,jenis_persalinan,penolong,penyulit,anak,keadaan_anak "
                + "from asesmen_kebidanan_persalinan where no_rawat=? order by urut")) {
            ps.setString(1, TNoRw.getText());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modePersalinan.addRow(new Object[]{
                        rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8)
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat persalinan kebidanan : " + e);
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

            String[] kolPersalinan = {"tgl_partus", "tempat_partus", "umur_hamil", "jenis_persalinan", "penolong", "penyulit", "anak", "keadaan_anak"};
            String[] sufPersalinan = {"tgl", "tempat", "uk", "jenis", "penolong", "penyulit", "anak", "keadaan"};
            for (int r = 1; r <= 5; r++) {
                for (String suf : sufPersalinan) {
                    param.put("pers_r" + r + "_" + suf, "");
                }
            }
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "select tgl_partus,tempat_partus,umur_hamil,jenis_persalinan,penolong,penyulit,anak,keadaan_anak "
                    + "from asesmen_kebidanan_persalinan where no_rawat=? order by urut limit 5")) {
                ps.setString(1, TNoRw.getText().trim());
                try (ResultSet rs = ps.executeQuery()) {
                    int r = 1;
                    while (rs.next() && r <= 5) {
                        for (int c = 0; c < kolPersalinan.length; c++) {
                            param.put("pers_r" + r + "_" + sufPersalinan[c], g(rs, kolPersalinan[c]));
                        }
                        r++;
                    }
                }
            }

            String sql = "select p.no_rkm_medis,ak.ruang,ak.lantai,p.nm_pasien,ak.kelas,"
                    + "ifnull(date_format(p.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir,"
                    + "if(p.jk='L','Laki-laki','Perempuan') as jk,"
                    + "ak.diagnosa_medis,ak.gelang,"
                    + "ifnull(date_format(ak.tanggal,'%d-%m-%Y'),'') as tanggal,ak.jam,"
                    + "ak.kondisi_masuk,ak.via,ak.nadi,ak.respirasi,ak.suhu,ak.spo2,ak.td,ak.td_posisi,ak.tb,ak.bb,"
                    + "ak.gcs_e,ak.gcs_m,ak.gcs_v,ak.keluhan_utama,"
                    + "ak.alergi_lateks,ak.snap_alert,ak.alergi_makanan_obat,ak.alergi_reaksi,"
                    + "ak.nyeri_skala,ak.nyeri_lokasi,ak.nyeri_onset,ak.nyeri_variasi,ak.nyeri_kualitas,"
                    + "ak.nyeri_pemberat,ak.nyeri_pencetus,ak.nyeri_obat,ak.nyeri_efek,"
                    + "ak.status_nikah,ak.keluarga_tinggal,ak.tempat_tinggal,ak.pekerjaan,ak.agama_keyakinan,"
                    + "ak.menarche,ak.lama_haid,ak.jumlah_darah,ak.haid_terakhir,ak.tafsiran_persalinan,ak.gangguan_haid,"
                    + "ak.kawin_ke,ak.gpa_g,ak.gpa_p,ak.gpa_a,ak.gpa_hidup,ak.hamil_muda,ak.hamil_tua,"
                    + "ak.riwayat_operasi,ak.riwayat_penyakit_keluarga,ak.riwayat_gynekologi,"
                    + "ak.kb_metode,ak.kb_lama,ak.kb_komplikasi,"
                    + "ak.pola_makan,ak.pola_minum,ak.pola_konsumsi,ak.gizi_asupan,ak.gizi_bb,ak.gizi_hb,ak.gizi_metabolisme,ak.gizi_total,"
                    + "ak.bak_jumlah,ak.bak_warna,ak.bak_terakhir,ak.bab_jumlah,ak.bab_karakteristik,ak.bab_terakhir,"
                    + "ak.tidur_jam,ak.tidur_terakhir,ak.nilai_keyakinan,ak.penerimaan_kehamilan,ak.sosial_support,"
                    + "ak.fisik_mata,ak.fisik_dada,ak.fisik_ekstrimitas,ak.fisik_kardio,"
                    + "ak.morse_jatuh,ak.morse_diagnosis,ak.morse_alat,ak.morse_infus,ak.morse_jalan,ak.morse_mental,ak.morse_total,ak.morse_resiko,"
                    + "ak.obs_inspeksi,ak.obs_tfu,ak.obs_letak_punggung,ak.obs_presentasi,ak.obs_palpasi,ak.obs_taksiran_bb,ak.obs_djj,ak.obs_djj_irama,"
                    + "ak.obs_bagian_terendah,ak.obs_his,ak.obs_his_irama,"
                    + "ak.gyn_inspeksi,ak.gyn_vagina,ak.gyn_portio,ak.gyn_vt,ak.gyn_kesan_panggul,ak.gyn_imbang,"
                    + "ak.nifas_tfu,ak.nifas_kontraksi,ak.nifas_lochea,ak.nifas_luka,"
                    + "ak.kriteria_discharge,ak.perencanaan_pulang,"
                    + "ak.penunjang_hb,ak.penunjang_ht,ak.penunjang_urine,ak.penunjang_ctg,ak.penunjang_usg,"
                    + "ak.diagnosa_kebidanan,ak.rencana_kebidanan,"
                    + fotoSqlByNip("ak.nik", "bidan_photo") + ","
                    + "ifnull((select nama from petugas where nip=ak.nik),'') as nama_bidan,"
                    + "ifnull((select nm_dokter from dokter where kd_dokter=ak.kd_dokter),'') as nama_dokter,"
                    + "ifnull(date_format(ak.tgl_ttd,'%d-%m-%Y'),'') as tgl_ttd "
                    + "from asesmen_kebidanan ak "
                    + "inner join reg_periksa rp on rp.no_rawat=ak.no_rawat "
                    + "inner join pasien p on p.no_rkm_medis=rp.no_rkm_medis "
                    + "where ak.no_rawat='" + TNoRw.getText().trim() + "'";
            Valid.MyReportqry("rptAsesmenKebidanan.jasper", "report", "::[ Asesmen Kebidanan (RM 5a) ]::", sql, param);
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
        RMAsesmenKebidanan f = new RMAsesmenKebidanan(null, false);
        f.isCek();
        f.setNoRm(noRawat.trim(), new Date(), "", null);
        f.cetak();
        f.dispose();
    }

    private void hapus() {
        if (TNoRw.getText().trim().equals("")) { return; }
        if (JOptionPane.showConfirmDialog(this, "Hapus asesmen kebidanan untuk No.Rawat " + TNoRw.getText() + " ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            jalankan("delete from asesmen_kebidanan where no_rawat=?");
            jalankan("delete from asesmen_kebidanan_persalinan where no_rawat=?");
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
