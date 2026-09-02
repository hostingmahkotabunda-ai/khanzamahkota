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
import java.awt.Image;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicTextFieldUI;
import kepegawaian.DlgCariPetugas;

/**
 * Askep Perioperatif (RM 28, blok 8 RM Operasi) -- 3 tab (Pre/Intra/Post
 * Operatif), tiap tab punya asesmen ceklis + "kartu diagnosa keperawatan"
 * (etiologi/kriteria hasil/intervensi/evaluasi SOAP) sesuai bentuk asli
 * kertas. Pola generik Item/semuaTeks sama seperti RMAsesmenPraSedasiAnestesi
 * (RM25) -- lihat memory rm25-form-besar-teknik.
 */
public final class RMAskepPerioperatif extends JDialog {

    private static final Font FONT_FORM = new Font("Times New Roman", Font.PLAIN, 13);
    private static final Font FONT_FORM_BOLD = new Font("Times New Roman", Font.BOLD, 13);

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final Map<String, ImageIcon> cacheFotoTtd = new HashMap<>();
    // Picker modal diperlukan agar tampil aktif di atas form RM Operasi yang modal.
    private final DlgCariPetugas pickerPetugas1 = new DlgCariPetugas(null, true);
    private final DlgCariPetugas pickerPetugas2 = new DlgCariPetugas(null, true);
    private final DlgCariPetugas pickerPetugas3 = new DlgCariPetugas(null, true);

    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TJK = ro();
    private final widget.TextBox TTglLahir = ro();
    private final widget.Tanggal dtpTanggal = dt();
    private final widget.Tanggal dtpTanggalOperasi = tglSaja();

    private final List<Item> semuaItem = new ArrayList<>();
    private final LinkedHashMap<String, widget.TextBox> semuaTeks = new LinkedHashMap<>();

    private final widget.TextBox tPetugas1 = ro();
    private final widget.Button btnPilihPetugas1 = new widget.Button();
    private final JLabel lblFotoPetugas1 = new JLabel();
    private String kdPetugas1 = "";

    private final widget.TextBox tPetugas2 = ro();
    private final widget.Button btnPilihPetugas2 = new widget.Button();
    private final JLabel lblFotoPetugas2 = new JLabel();
    private String kdPetugas2 = "";

    private final widget.TextBox tPetugas3 = ro();
    private final widget.Button btnPilihPetugas3 = new widget.Button();
    private final JLabel lblFotoPetugas3 = new JLabel();
    private String kdPetugas3 = "";

    private final widget.Button BtnBaru = new widget.Button();
    private final widget.Button BtnSimpan = new widget.Button();
    private final widget.Button BtnHapus = new widget.Button();
    private final widget.Button BtnCetak = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();

    public RMAskepPerioperatif(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Askep Perioperatif (RM 28) ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
        ensureTable();
        siapkanPicker();
        setSize(1150, 820);
        setMinimumSize(new Dimension(950, 640));
        setLocationRelativeTo(parent);
    }

    // ====================== Item ceklis generik (pola sama RM25) ======================
    private final class Item {
        final String kolom;
        final JCheckBox check;
        final widget.TextBox catatan;

        Item(String kolom, String label, boolean adaCatatan) {
            this.kolom = kolom;
            this.check = new JCheckBox(label);
            check.setOpaque(false);
            check.setFont(FONT_FORM);
            this.catatan = adaCatatan ? tf() : null;
            if (catatan != null) {
                catatan.setPreferredSize(new Dimension(160, 22));
            }
            semuaItem.add(this);
        }
    }

    private Item item(String kolom, String label) {
        return new Item(kolom, label, false);
    }

    private Item itemCatatan(String kolom, String label) {
        return new Item(kolom, label, true);
    }

    private widget.TextBox teks(String kolom) {
        widget.TextBox t = tf();
        semuaTeks.put(kolom, t);
        return t;
    }

    /** Parse mini-DSL "kolom:Label" / "kolom:Label:C" jadi Item[] siap taruh ke barisLabel(). */
    private Item[] itemsDari(String... spek) {
        Item[] hasil = new Item[spek.length];
        for (int i = 0; i < spek.length; i++) {
            String[] bag = spek[i].split(":");
            hasil[i] = (bag.length > 2 && bag[2].equals("C")) ? itemCatatan(bag[0], bag[1]) : item(bag[0], bag[1]);
        }
        return hasil;
    }

    private final class TanggalField {
        final String kolom;
        final widget.Tanggal komponen;
        final String format;

        TanggalField(String kolom, widget.Tanggal komponen, String format) {
            this.kolom = kolom;
            this.komponen = komponen;
            this.format = format;
            semuaTanggal.add(this);
        }
    }

    private final List<TanggalField> semuaTanggal = new ArrayList<>();

    private void daftarTanggal(String kolom, widget.Tanggal komponen, String format) {
        new TanggalField(kolom, komponen, format);
    }

    // ====================== UI ======================
    private void initComponents() {
        final Color latar = new Color(246, 249, 251);
        final Color garis = new Color(215, 224, 230);
        final Color teks = new Color(32, 49, 66);

        getContentPane().setBackground(latar);
        getContentPane().setLayout(new BorderLayout());

        JPanel atas = new JPanel(new BorderLayout(12, 10));
        atas.setBackground(latar);
        atas.setBorder(new EmptyBorder(14, 18, 10, 18));
        JPanel blokJudul = new JPanel();
        blokJudul.setOpaque(false);
        blokJudul.setLayout(new BoxLayout(blokJudul, BoxLayout.Y_AXIS));
        JLabel judulUtama = new JLabel("Askep Perioperatif");
        judulUtama.setFont(new Font("Times New Roman", Font.BOLD, 21));
        judulUtama.setForeground(teks);
        JLabel subjudul = new JLabel("Form RM 28  •  Asuhan keperawatan pre/intra/post operatif");
        subjudul.setFont(FONT_FORM);
        subjudul.setForeground(new Color(92, 107, 119));
        blokJudul.add(judulUtama);
        blokJudul.add(Box.createVerticalStrut(3));
        blokJudul.add(subjudul);
        atas.add(blokJudul, BorderLayout.NORTH);

        JPanel ringkasanPasien = new JPanel(new GridLayout(1, 5, 0, 0));
        ringkasanPasien.setBackground(Color.WHITE);
        ringkasanPasien.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis), new EmptyBorder(10, 12, 10, 12)));
        ringkasanPasien.add(fieldRingkasan("No. Rawat *", TNoRw, true));
        ringkasanPasien.add(fieldRingkasan("No. RM", TNoRM, true));
        ringkasanPasien.add(fieldRingkasan("Nama Pasien", TPasien, true));
        ringkasanPasien.add(fieldRingkasan("Jenis Kelamin", TJK, true));
        ringkasanPasien.add(fieldRingkasan("Tanggal Lahir", TTglLahir, true));
        atas.add(ringkasanPasien, BorderLayout.CENTER);
        getContentPane().add(atas, BorderLayout.NORTH);

        JTabbedPane tab = new JTabbedPane();
        tab.setFont(FONT_FORM_BOLD);
        tab.addTab("Pre Operatif", bungkusScroll(buatTabPre(teks, garis)));
        tab.addTab("Intra Operatif", bungkusScroll(buatTabIntra(teks, garis)));
        tab.addTab("Post Operatif", bungkusScroll(buatTabPost(teks, garis)));
        getContentPane().add(tab, BorderLayout.CENTER);

        BtnBaru.setText("Baru");
        BtnSimpan.setText("Simpan Data");
        BtnHapus.setText("Hapus Data");
        BtnCetak.setText("Cetak");
        BtnKeluar.setText("Keluar");
        BtnBaru.addActionListener(e -> setNoRm(ambil(TNoRw)));
        BtnSimpan.addActionListener(e -> simpan());
        BtnHapus.addActionListener(e -> hapus());
        BtnCetak.addActionListener(e -> cetak());
        BtnKeluar.addActionListener(e -> dispose());
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 9));
        bawah.setBackground(Color.WHITE);
        bawah.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, garis));
        bawah.add(BtnHapus);
        bawah.add(BtnBaru);
        bawah.add(BtnCetak);
        bawah.add(BtnKeluar);
        bawah.add(BtnSimpan);
        getContentPane().add(bawah, BorderLayout.SOUTH);
    }

    private JPanel buatTabPre(Color teks, Color garis) {
        JPanel isi = new JPanel();
        isi.setBackground(new Color(246, 249, 251));
        isi.setBorder(new EmptyBorder(6, 8, 10, 8));
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));

        JPanel kartuInfo = kartu("Informasi Operasi", teks, garis);
        daftarTanggal("tanggal_operasi", dtpTanggalOperasi, "dd-MM-yyyy");
        int r0 = 1;
        r0 = tambahBaris(kartuInfo, r0, barisField(
                fieldKecil("Ruangan Poli", teks("ruangan_poli")),
                fieldKecil("Tanggal Operasi", dtpTanggalOperasi),
                fieldKecil("Tanggal / Jam Pemeriksaan", dtpTanggal)));
        r0 = tambahBaris(kartuInfo, r0, barisField(
                fieldKecilLebar("Diagnose Pra-Bedah", teks("diagnosa_pra_bedah")),
                fieldKecilLebar("Rencana Tindakan", teks("rencana_operasi"))));
        isi.add(kartuInfo);
        isi.add(Box.createVerticalStrut(10));

        JPanel kartuAsesmen = kartu("Asesmen Pre Operatif", teks, garis);
        int r1 = 1;
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Pengetahuan tentang Penyakit",
                itemsDari("preop_pengetahuan_penyakit_baik:Baik", "preop_pengetahuan_penyakit_kurang:Kurang")));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Pengetahuan Prosedur Operasi & Anestesi",
                itemsDari("preop_pengetahuan_prosedur_baik:Baik", "preop_pengetahuan_prosedur_kurang:Kurang")));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Tingkat Kecemasan",
                itemsDari("preop_kecemasan_tinggi:Tinggi", "preop_kecemasan_rendah:Rendah")));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Resiko Jatuh",
                itemsDari("preop_resikojatuh_rendah:Rendah", "preop_resikojatuh_sedang:Sedang", "preop_resikojatuh_tinggi:Tinggi")));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Kesadaran",
                itemsDari("preop_kesadaran_cm:CM", "preop_kesadaran_delirium:Delirium",
                        "preop_kesadaran_samnolen:Samnolen", "preop_kesadaran_stupor:Stupor", "preop_kesadaran_koma:Koma")));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Skala Nyeri",
                itemsDari("preop_skalanyeri_ringan:Ringan", "preop_skalanyeri_sedang:Sedang", "preop_skalanyeri_berat:Berat")));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Data Premedikasi Tercantum",
                itemsDari("preop_premedikasi_tandavital:Tanda Vital", "preop_premedikasi_airway:Airway",
                        "preop_premedikasi_breathing:Breathing", "preop_premedikasi_circulation:Circulation",
                        "preop_premedikasi_puasa:Puasa", "preop_premedikasi_alergi:Alergi",
                        "preop_premedikasi_gigipalsu:Gigi Palsu", "preop_premedikasi_rencanaanestesialocal:Rencana Anesthesia (Local)",
                        "preop_premedikasi_rencanaanestesiagaregional:Rencana Anesthesia (GA/Regional)",
                        "preop_premedikasi_antibiotikprofilaksis:Antibiotic Profilaksis", "preop_premedikasi_kateterurin:Kateter Urin")));
        isi.add(kartuAsesmen);
        isi.add(Box.createVerticalStrut(10));

        isi.add(diagnosisCard("Diagnosa 1 : Ansietas / Cemas bd Prosedur Operasi / Kurang Informasi / Operasi Anestesi", teks, garis,
                new String[]{"cemas_etio_prosedur:Prosedur Operasi", "cemas_etio_kuranginformasi:Kurang Informasi ttg Prosedur",
                        "cemas_etio_operasianestesi:Operasi Anestesi"},
                new String[]{"cemas_kriteria_mengatakanmerasa:Klien Mengatakan Merasa Tenang", "cemas_kriteria_ttvdbn:TTV DBN",
                        "cemas_kriteria_tampaktenang:Klien Tampak Tenang"},
                new String[]{"cemas_int_diskusikanprosedur:Diskusikan Prosedur yang Akan Dijalankan",
                        "cemas_int_berikesempatan:Beri Kesempatan Bertanya", "cemas_int_ajakkeluarga:Ajak Keluarga Mendampingi",
                        "cemas_int_kenalkanlingkungan:Kenalkan Lingkungan Kamar Operasi", "cemas_int_anjurkanberdoa:Anjurkan Klien Berdoa",
                        "cemas_int_kolaborasiobatpenenang:Kolaborasi Pemberian Obat Penenang"},
                (kartuDx, row) -> {
                    row = tambahBaris(kartuDx, row, barisLabel("Evaluasi - S", itemsDari("cemas_eval_s:Klien Mengatakan Merasakan Tentang:C")));
                    row = tambahBaris(kartuDx, row, barisLabel("Evaluasi - O", itemsDari("cemas_eval_o_ttvdbn:TTV DBN", "cemas_eval_o_tampaktenang:Klien Tampak Tenang")));
                    row = tambahBaris(kartuDx, row, barisLabel("Evaluasi - A", itemsDari("cemas_eval_a_belumteratasi:Belum Teratasi",
                            "cemas_eval_a_teratasisebagian:Teratasi Sebagian", "cemas_eval_a_teratasi:Teratasi")));
                    row = tambahBaris(kartuDx, row, barisLabel("Evaluasi - P", itemsDari("cemas_eval_p_lanjutan:Lanjutan Intervensi")));
                    return row;
                }));
        isi.add(Box.createVerticalStrut(10));

        isi.add(diagnosisCard("Diagnosa 2 : Nyeri bd Agen Injuri Fisik / Kimia / Biologis / Psikologis", teks, garis,
                new String[]{"nyeri_etio_fisik:Fisik", "nyeri_etio_kimia:Kimia", "nyeri_etio_biologis:Biologis", "nyeri_etio_psikologis:Psikologis"},
                new String[]{"nyeri_kriteria_penurunanskala:Penurunan Skala Nyeri", "nyeri_kriteria_ttvdbn:TTV DBN"},
                new String[]{"nyeri_int_kajiobservasi:Kaji & Observasi Nyeri Kedua Reaksi Klien", "nyeri_int_kontrollingkungan:Kontrol Lingkungan",
                        "nyeri_int_ajarkanreduksi:Ajarkan Teknik Reduksi Nyeri Non Farmakologis", "nyeri_int_tingkatkanistirahat:Tingkatkan Istirahat",
                        "nyeri_int_immobilisasi:Immobilisasi Sumber Nyeri Fisik", "nyeri_int_kolaborasianalgetik:Kolaborasi Pemberian Analgetik & Sedasi",
                        "nyeri_int_monitorttv:Monitor Tanda Vital"},
                (kartuDx, row) -> {
                    row = tambahBaris(kartuDx, row, barisLabel("Evaluasi - S", itemsDari("nyeri_eval_s_keluhanberkurang:Keluhan Nyeri Berkurang")));
                    row = tambahBaris(kartuDx, row, barisLabel("Evaluasi - O", itemsDari("nyeri_eval_o_ttvdbn:TTV DBN", "nyeri_eval_o_skalamenurun:Skala Nyeri Menurun")));
                    row = tambahBaris(kartuDx, row, barisLabel("Evaluasi - A", itemsDari("nyeri_eval_a_teratasi:Teratasi",
                            "nyeri_eval_a_sebagian:Teratasi Sebagian", "nyeri_eval_a_belumteratasi:Belum Teratasi")));
                    row = tambahBaris(kartuDx, row, barisLabel("Evaluasi - P", itemsDari("nyeri_eval_p_lanjutkan:Lanjutkan Intervensi")));
                    return row;
                }));
        isi.add(Box.createVerticalStrut(10));

        JPanel kartuTtd1 = kartu("Perawat (Tertanda - Pre Operatif)", teks, garis);
        int r7 = 0;
        r7 = tunggalVertikal(kartuTtd1, r7, "Perawat", bungkusPicker(bungkusFotoTtd(tPetugas1, lblFotoPetugas1), btnPilihPetugas1));
        isi.add(kartuTtd1);
        isi.add(Box.createVerticalGlue());
        return isi;
    }

    private JPanel buatTabIntra(Color teks, Color garis) {
        JPanel isi = new JPanel();
        isi.setBackground(new Color(246, 249, 251));
        isi.setBorder(new EmptyBorder(6, 8, 10, 8));
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));

        JPanel kartuAsesmen = kartu("Pengkajian Intra Operatif", teks, garis);
        int r1 = 1;
        r1 = tambahBaris(kartuAsesmen, r1, barisField(
                fieldKecil("Jam Mulai (WITA)", teks("intraop_jammulai")), fieldKecil("Jam Selesai (WITA)", teks("intraop_jamselesai"))));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Tingkat Kesadaran",
                itemsDari("intraop_kesadaran_cm:CM", "intraop_kesadaran_delirium:Delirium", "intraop_kesadaran_samnolen:Samnolen", "intraop_kesadaran_koma:Koma")));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Jenis Operasi",
                itemsDari("intraop_jenisoperasi_bersih:Bersih", "intraop_jenisoperasi_bersihterkontaminasi:Bersih Terkontaminasi", "intraop_jenisoperasi_kotor:Kotor")));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Pemasangan Urin Kateter",
                itemsDari("intraop_kateter_tidak:Tidak", "intraop_kateter_ya:Ya")));
        r1 = tambahBaris(kartuAsesmen, r1, barisField(fieldKecil("Dipasang Oleh", teks("intraop_kateter_dipasangoleh"))));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Desinfeksi Kulit",
                itemsDari("intraop_desinfeksi_clorhexidin:Clorhexidin 2%", "intraop_desinfeksi_popidoniodine:Popidon Iodine 10%",
                        "intraop_desinfeksi_alkohol:Alcohol 70%", "intraop_desinfeksi_savlon:Savlon", "intraop_desinfeksi_lainlain:Lain-lain")));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Drepping", itemsDari("intraop_drepping_iya:Iya", "intraop_drepping_tidak:Tidak")));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Diatermi / Elektro Cauter",
                itemsDari("intraop_diatermi_iya:Iya", "intraop_diatermi_tidak:Tidak", "intraop_diatermi_monopolar:Monopolar", "intraop_diatermi_bipolar:Bipolar")));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Kulit Sebelum Dipasang Plat/Arde Cauter",
                itemsDari("intraop_kulitplat_utuh:Utuh", "intraop_kulitplat_melepuh:Melepuh")));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Tourniquet", itemsDari("intraop_tourniquet_iya:Iya", "intraop_tourniquet_tidak:Tidak")));
        r1 = tambahBaris(kartuAsesmen, r1, barisField(fieldKecil("Lamanya (menit)", teks("intraop_tourniquet_lamanya"))));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Implant", itemsDari("intraop_implant_iya:Iya", "intraop_implant_tidak:Tidak")));
        r1 = tambahBaris(kartuAsesmen, r1, barisField(
                fieldKecil("Jenis Implant", teks("intraop_implant_jenis")), fieldKecil("Lokasi Implant", teks("intraop_implant_lokasi"))));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Unit Pemanas", itemsDari("intraop_unitpemanas_iya:Iya", "intraop_unitpemanas_tidak:Tidak")));
        isi.add(kartuAsesmen);
        isi.add(Box.createVerticalStrut(10));

        isi.add(diagnosisCard("Diagnosa 1 : Resiko Jalan Napas Tidak Efektif", teks, garis,
                new String[]{"jalannafas_etio_obatsedasi:Penggunaan Obat Sedasi / Anestesi", "jalannafas_etio_alatbantu:Penggunaan Alat Bantu Jalan Nafas"},
                new String[]{"jalannafas_kriteria_tidaksuaratambahan:Tidak Ada Suara Nafas Tambahan", "jalannafas_kriteria_tidaksianosis:Tidak Ada Sianosis",
                        "jalannafas_kriteria_alatberfungsi:Alat Bantu Jalan Napas Berfungsi Baik"},
                new String[]{"jalannafas_int_pastikanalat:Pastikan Alat Bantu Jalan Napas Tersedia", "jalannafas_int_rencanakanpilihan:Rencanakan Pilihan Alat Bantu Jalan Nafas",
                        "jalannafas_int_aturposisikepala:Atur Posisi Kepala utk Kepentingan Jalan Napas", "jalannafas_int_gunakanalat:Gunakan Alat Bantu Jalan Napas",
                        "jalannafas_int_observasikepatenan:Observasi Kepatenan Alat Bantu Jalan Napas", "jalannafas_int_monitorttvspo2:Monitor TTV & SpO2",
                        "jalannafas_int_kajijaringan:Kaji Jaringan Perifer & Mukosa", "jalannafas_int_kolaborasibronchodilator:Kolaborasi Pemberian Bronchodilator"},
                (kartuDx, row) -> {
                    row = tambahBaris(kartuDx, row, catatanEvaluasiS());
                    row = tambahBaris(kartuDx, row, barisLabel("Evaluasi - O", itemsDari("jalannafas_eval_o_ttvdbn:TTV DBN",
                            "jalannafas_eval_o_tidaksuaratambahan:Tidak Ada Suara Napas Tambahan", "jalannafas_eval_o_tidaksianosis:Tidak Ada Sianosis",
                            "jalannafas_eval_o_alatberfungsi:Alat Bantu Jalan Nafas Berfungsi Baik")));
                    row = tambahBaris(kartuDx, row, barisLabel("Evaluasi - A", itemsDari("jalannafas_eval_a_efektif:Jalan Napas Efektif")));
                    row = tambahBaris(kartuDx, row, barisField(fieldKecilLebar("Evaluasi - P (catatan)", teks("jalannafas_eval_p_ket"))));
                    return row;
                }));
        isi.add(Box.createVerticalStrut(10));

        isi.add(diagnosisCard("Diagnosa 2 : Resiko Ketidakseimbangan Volume Cairan", teks, garis,
                new String[]{"cairan_etio_pendarahan:Pendarahan", "cairan_etio_vasodilatasi:Vasodilatasi Vaskuler"},
                new String[]{"cairan_kriteria_balanceseimbang:Balance Cairan Seimbang", "cairan_kriteria_ttvdbn:TTV DBN",
                        "cairan_kriteria_tidaktampakhipoperfusi:Tidak Tampak Tanda Hipoperfusi Jaringan"},
                new String[]{"cairan_int_pastikankepatenan:Pastikan Kepatenan Jalur Intra Vena",
                        "cairan_int_pasang2jalur:Pasang 2 Jalur IV bila Resiko Pendarahan >500ml (Dewasa)/7ml/kgBB (Anak)",
                        "cairan_int_pasangkateter:Pasang Kateter utk Evaluasi Output Cairan",
                        "cairan_int_pertahankankeseimbangan:Pertahankan Keseimbangan Cairan Masuk & Keluar",
                        "cairan_int_monitorttvspo2:Monitor TTV & SpO2",
                        "cairan_int_kolaborasicairan:Kolaborasi Pemberian Cairan Hipertonik, Darah & Vasokonstriktor Vaskuler"},
                (kartuDx, row) -> {
                    row = tambahBaris(kartuDx, row, catatanEvaluasiS());
                    row = tambahBaris(kartuDx, row, barisLabel("Evaluasi - O", itemsDari("cairan_eval_o_balanceseimbang:Balance Cairan Seimbang",
                            "cairan_eval_o_ttvdbn:TTV DBN", "cairan_eval_o_spo2:SpO2 90-100%")));
                    row = tambahBaris(kartuDx, row, barisLabel("Evaluasi - A", itemsDari("cairan_eval_a_tidakterjadi:Kekurangan Volume Cairan Tidak Terjadi")));
                    row = tambahBaris(kartuDx, row, barisField(fieldKecilLebar("Evaluasi - P (catatan)", teks("cairan_eval_p_ket"))));
                    return row;
                }));
        isi.add(Box.createVerticalStrut(10));

        isi.add(diagnosisCard("Diagnosa 3 : Resiko Cedera", teks, garis,
                new String[]{"cedera_etio_kejatuhan:Kejatuhan", "cedera_etio_bendaasing:Benda Asing Tertinggal",
                        "cedera_etio_lukabakar:Luka Bakar", "cedera_etio_kerusakanjaringan:Kerusakan Jaringan Perifer"},
                new String[]{"cedera_kriteria_tidakterjatuh:Klien Tidak Terjatuh", "cedera_kriteria_tidakbendaasing:Tidak Ada Benda Asing Tertinggal",
                        "cedera_kriteria_tidaklukabakar:Tidak Ada Luka Bakar Baru", "cedera_kriteria_tidakkerusakan:Tidak Ada Kerusakan Jaringan Perifer"},
                new String[]{"cedera_int_pastikanposisi:Pastikan Posisi Operasi", "cedera_int_pastikanpengamanan:Pastikan Pengamanan Posisi",
                        "cedera_int_cekpenekanan:Cek Daerah Penekanan Selama Operasi",
                        "cedera_int_hitungkasa:Hitung Kasa, Jarum, Bisturi & Instrument Bedah Sebelum & Sesudah Operasi",
                        "cedera_int_periksaplat:Periksa Tempat Pemasangan Plat/Arde Cauter Sebelum & Setelah Operasi",
                        "cedera_int_lepastourniquet:Lepaskan Tourniquet Setiap 1 Jam"},
                (kartuDx, row) -> {
                    row = tambahBaris(kartuDx, row, catatanEvaluasiS());
                    row = tambahBaris(kartuDx, row, barisLabel("Evaluasi - O", itemsDari("cedera_eval_o_tidakterjatuh:Klien Tidak Terjatuh",
                            "cedera_eval_o_tidakbendaasing:Tidak Ada Benda Asing Tertinggal", "cedera_eval_o_tidaklukabakar:Tidak Ada Luka Bakar Baru",
                            "cedera_eval_o_tidakkerusakan:Tidak Ada Kerusakan Jaringan Perifer")));
                    row = tambahBaris(kartuDx, row, barisLabel("Evaluasi - A", itemsDari("cedera_eval_a_tidakterjadi:Cedera Tidak Terjadi")));
                    row = tambahBaris(kartuDx, row, barisField(fieldKecilLebar("Evaluasi - P (catatan)", teks("cedera_eval_p_ket"))));
                    return row;
                }));
        isi.add(Box.createVerticalStrut(10));

        JPanel kartuTtd2 = kartu("Perawat (Tertanda - Intra Operatif)", teks, garis);
        int r7 = 0;
        r7 = tunggalVertikal(kartuTtd2, r7, "Perawat", bungkusPicker(bungkusFotoTtd(tPetugas2, lblFotoPetugas2), btnPilihPetugas2));
        isi.add(kartuTtd2);
        isi.add(Box.createVerticalGlue());
        return isi;
    }

    private JPanel buatTabPost(Color teks, Color garis) {
        JPanel isi = new JPanel();
        isi.setBackground(new Color(246, 249, 251));
        isi.setBorder(new EmptyBorder(6, 8, 10, 8));
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));

        JPanel kartuAsesmen = kartu("Pengkajian Post Operatif", teks, garis);
        int r1 = 1;
        r1 = tambahBaris(kartuAsesmen, r1, barisField(fieldKecil("Masuk RR Jam", teks("postop_masukrrjam"))));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Tingkat Kesadaran",
                itemsDari("postop_kesadaran_cm:CM", "postop_kesadaran_delirium:Delirium", "postop_kesadaran_samnolen:Samnolen",
                        "postop_kesadaran_stupor:Stupor", "postop_kesadaran_koma:Koma")));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("TTV", itemsDari("postop_ttv_td:TD (mmHg):C", "postop_ttv_suhu:Suhu (C):C",
                "postop_ttv_nadi:Nadi (x/mnt):C", "postop_ttv_pernapasan:Pernapasan (x/mnt):C")));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Posisi", itemsDari("postop_posisi_terlentang:Terlentang", "postop_posisi_miring:Miring",
                "postop_posisi_tengkurap:Tengkurap", "postop_posisi_semifowler:Semi/Fowler")));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Airway", itemsDari("postop_airway_bebas:Bebas", "postop_airway_alatbantu:Alat Bantu")));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Breathing", itemsDari("postop_breathing_spontan:Spontan", "postop_breathing_alatbantu:Alat Bantu",
                "postop_breathing_o2:O2 (lpm):C", "postop_breathing_masker:Masker", "postop_breathing_canulnasal:Canul Nasal")));
        r1 = tambahBaris(kartuAsesmen, r1, barisLabel("Sirkulasi (Kulit)", itemsDari("postop_sirkulasi_merahmuda:Merah Muda",
                "postop_sirkulasi_sianosis:Sianosis", "postop_sirkulasi_pucat:Pucat", "postop_sirkulasi_akraldingin:Akral Dingin")));
        isi.add(kartuAsesmen);
        isi.add(Box.createVerticalStrut(10));

        isi.add(diagnosisCard("Diagnosa : Hipotermia bd Efek Anestesi / Lingkungan", teks, garis,
                new String[]{"hipo_etio_efekanestesi:Efek Anestesi", "hipo_etio_lingkungan:Lingkungan"},
                new String[]{"hipo_kriteria_suhu:Suhu Tubuh 36-37,5", "hipo_kriteria_tidakdingin:Klien Mengatakan Tidak Kedinginan",
                        "hipo_kriteria_tidakmenggigil:Tidak Menggigil"},
                new String[]{"hipo_int_modifikasisuhu:Modifikasi Suhu Lingkungan", "hipo_int_beriselimut:Beri Selimut",
                        "hipo_int_pasangpemanas:Pasang Pemanas", "hipo_int_kolaborasiantagonis:Kolaborasi utk Pemberian Antagonis Obat Anestesi"},
                (kartuDx, row) -> {
                    row = tambahBaris(kartuDx, row, barisLabel("Evaluasi - S", itemsDari("hipo_eval_s_tidakdingin:Klien Mengatakan Tidak Kedinginan")));
                    row = tambahBaris(kartuDx, row, barisLabel("Evaluasi - O", itemsDari("hipo_eval_o_suhu:Suhu Tubuh 36-37,5C", "hipo_eval_o_tidakmenggigil:Tidak Menggigil")));
                    row = tambahBaris(kartuDx, row, barisLabel("Evaluasi - A", itemsDari("hipo_eval_a_belumteratasi:Belum Teratasi",
                            "hipo_eval_a_sebagian:Teratasi Sebagian", "hipo_eval_a_teratasi:Teratasi")));
                    row = tambahBaris(kartuDx, row, barisLabel("Evaluasi - P", itemsDari("hipo_eval_p_lanjutkan:Lanjutkan Intervensi:C")));
                    return row;
                }));
        isi.add(Box.createVerticalStrut(10));

        JPanel kartuTtd3 = kartu("Perawat (Tertanda - Post Operatif)", teks, garis);
        int r7 = 0;
        r7 = tunggalVertikal(kartuTtd3, r7, "Perawat", bungkusPicker(bungkusFotoTtd(tPetugas3, lblFotoPetugas3), btnPilihPetugas3));
        isi.add(kartuTtd3);
        isi.add(Box.createVerticalGlue());
        return isi;
    }

    private JPanel catatanEvaluasiS() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        p.setOpaque(false);
        JLabel l = new JLabel("Evaluasi - S : - (tidak berlaku, pasien dalam anestesi)");
        l.setFont(new Font("Times New Roman", Font.ITALIC, 13));
        l.setForeground(new Color(120, 133, 143));
        p.add(l);
        return p;
    }

    // ====================== Kartu diagnosa keperawatan (etiologi/kriteria/intervensi sama bentuknya di 6 diagnosa) ======================
    private interface IsiEvaluasi {
        int isi(JPanel kartu, int row);
    }

    private JPanel diagnosisCard(String judul, Color teks, Color garis,
            String[] etiologi, String[] kriteria, String[] intervensi, IsiEvaluasi evaluasi) {
        JPanel tabel = new JPanel(new GridBagLayout());
        tabel.setBackground(Color.WHITE);
        tabel.setBorder(BorderFactory.createLineBorder(new Color(90, 100, 108)));
        tabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        tabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6000));

        JPanel kolomDiagnosa = kolomDiagnosis("DIAGNOSA KEPERAWATAN");
        tambahBaris(kolomDiagnosa, 1, daftarVertikal(judul, itemsDari(etiologi)));

        JPanel kolomTujuan = kolomDiagnosis("TUJUAN");
        tambahBaris(kolomTujuan, 1, daftarVertikal("Kriteria Hasil", itemsDari(kriteria)));

        JPanel kolomIntervensi = kolomDiagnosis("INTERVENSI & IMPLEMENTASI");
        tambahBaris(kolomIntervensi, 1, daftarVertikal("", itemsDari(intervensi)));

        JPanel kolomEvaluasi = kolomDiagnosis("EVALUASI");
        evaluasi.isi(kolomEvaluasi, 1);

        tambahKolomDiagnosis(tabel, kolomDiagnosa, 0, 18);
        tambahKolomDiagnosis(tabel, kolomTujuan, 1, 25);
        tambahKolomDiagnosis(tabel, kolomIntervensi, 2, 34);
        tambahKolomDiagnosis(tabel, kolomEvaluasi, 3, 23);
        return tabel;
    }

    private JPanel kolomDiagnosis(String judul) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(90, 100, 108)));
        JLabel l = new JLabel(judul, JLabel.CENTER);
        l.setFont(FONT_FORM_BOLD);
        l.setOpaque(true);
        l.setBackground(new Color(239, 243, 246));
        l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(90, 100, 108)));
        GridBagConstraints g = gc(0, 0, 4, 1.0);
        g.insets = new Insets(0, 0, 3, 0);
        p.add(l, g);
        return p;
    }

    private void tambahKolomDiagnosis(JPanel tabel, JPanel kolom, int x, double bobot) {
        GridBagConstraints g = gc(x, 0, 1, bobot);
        g.weighty = 1.0;
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(0, 0, 0, 0);
        tabel.add(kolom, g);
    }

    private JPanel daftarVertikal(String pengantar, Item... daftar) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        if (pengantar != null && !pengantar.isEmpty()) {
            JLabel l = new JLabel("<html>" + pengantar + "</html>");
            l.setFont(FONT_FORM);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(l);
            p.add(Box.createVerticalStrut(3));
        }
        for (Item it : daftar) {
            JPanel baris = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
            baris.setOpaque(false);
            baris.setAlignmentX(Component.LEFT_ALIGNMENT);
            baris.add(it.check);
            if (it.catatan != null) {
                baris.add(bungkusKurung(it.catatan));
            }
            p.add(baris);
        }
        return p;
    }

    // ====================== Picker TTD (3 perawat) ======================
    private interface PenerimaKode {
        void terima(String kode);
    }

    private void siapkanPicker() {
        siapkanSatuPicker(btnPilihPetugas1, pickerPetugas1, tPetugas1, lblFotoPetugas1, kd -> kdPetugas1 = kd);
        siapkanSatuPicker(btnPilihPetugas2, pickerPetugas2, tPetugas2, lblFotoPetugas2, kd -> kdPetugas2 = kd);
        siapkanSatuPicker(btnPilihPetugas3, pickerPetugas3, tPetugas3, lblFotoPetugas3, kd -> kdPetugas3 = kd);
    }

    private void siapkanSatuPicker(widget.Button tombol, DlgCariPetugas picker, widget.TextBox target,
            JLabel lblFoto, PenerimaKode penerima) {
        tombol.setText("...");
        tombol.setPreferredSize(new Dimension(32, 25));
        tombol.addActionListener(e -> {
            picker.isCek();
            picker.setSize(650, 400);
            picker.setLocationRelativeTo(this);
            picker.setVisible(true);
        });
        picker.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (picker.getTable().getSelectedRow() != -1) {
                    String kode = picker.getTable().getValueAt(picker.getTable().getSelectedRow(), 0).toString();
                    String nama = picker.getTable().getValueAt(picker.getTable().getSelectedRow(), 1).toString();
                    penerima.terima(kode);
                    target.setText(kode + " - " + nama);
                    lblFoto.setIcon(ambilFotoTtd(nama));
                }
            }
        });
    }

    public void isCek() {
        // Form RM Operasi diisi dokter (bedah/anastesi) MAUPUN perawat -- boleh Simpan kalau
        // punya salah satu izin yg relevan (dokter selama ini sudah punya booking_operasi,
        // perawat sudah punya penilaian_awal_keperawatan_ranap; tidak perlu reset hak akses akun manapun).
        boolean bisa = akses.getpenilaian_awal_keperawatan_ranap() || akses.getbooking_operasi();
        BtnSimpan.setEnabled(bisa);
        BtnHapus.setEnabled(bisa);
    }

    public void emptTeks() {
        for (widget.TextBox t : new widget.TextBox[]{TNoRw, TNoRM, TPasien, TJK, TTglLahir, tPetugas1, tPetugas2, tPetugas3}) {
            t.setText("");
        }
        for (widget.TextBox t : semuaTeks.values()) {
            t.setText("");
        }
        for (Item it : semuaItem) {
            it.check.setSelected(false);
            if (it.catatan != null) {
                it.catatan.setText("");
            }
        }
        for (TanggalField tf : semuaTanggal) {
            tf.komponen.setDate(new Date());
        }
        dtpTanggal.setDate(new Date());
        lblFotoPetugas1.setIcon(null);
        lblFotoPetugas2.setIcon(null);
        lblFotoPetugas3.setIcon(null);
        kdPetugas1 = "";
        kdPetugas2 = "";
        kdPetugas3 = "";
    }

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
                "select p.no_rkm_medis,p.nm_pasien,p.jk,ifnull(date_format(p.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir "
                + "from reg_periksa rp inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis where rp.no_rawat=?")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    TJK.setText("L".equalsIgnoreCase(rs.getString("jk")) ? "Laki-Laki" : "Perempuan");
                    TTglLahir.setText(rs.getString("tgl_lahir"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik data pasien RM28 : " + e);
        }
        dtpTanggal.setDate(new Date());
    }

    private void muatDataJikaAda(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement("select * from askep_perioperatif where no_rawat=?")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getDate("tanggal") != null) {
                        isiTanggalJam(dtpTanggal, rs.getDate("tanggal"), rs.getString("jam"));
                    }
                    for (Item it : semuaItem) {
                        it.check.setSelected("1".equals(rs.getString(it.kolom)));
                        if (it.catatan != null) {
                            it.catatan.setText(nvl(rs.getString(it.kolom + "_ket")));
                        }
                    }
                    for (Map.Entry<String, widget.TextBox> e : semuaTeks.entrySet()) {
                        String v = nvl(rs.getString(e.getKey()));
                        if (!v.equals("")) {
                            e.getValue().setText(v);
                        }
                    }
                    for (TanggalField tf : semuaTanggal) {
                        String v = nvl(rs.getString(tf.kolom));
                        if (!v.equals("")) {
                            try {
                                tf.komponen.setDate(new java.text.SimpleDateFormat(tf.format).parse(v));
                            } catch (Exception ignore) { }
                        }
                    }
                    isiTtdJikaAda(rs, "kd_petugas1", "nama_petugas1", tPetugas1, lblFotoPetugas1, kd -> kdPetugas1 = kd);
                    isiTtdJikaAda(rs, "kd_petugas2", "nama_petugas2", tPetugas2, lblFotoPetugas2, kd -> kdPetugas2 = kd);
                    isiTtdJikaAda(rs, "kd_petugas3", "nama_petugas3", tPetugas3, lblFotoPetugas3, kd -> kdPetugas3 = kd);
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat RM28 : " + e);
        }
    }

    private void isiTtdJikaAda(ResultSet rs, String kolKode, String kolNama, widget.TextBox target,
            JLabel lblFoto, PenerimaKode penerima) throws Exception {
        String kode = nvl(rs.getString(kolKode));
        if (!kode.equals("")) {
            String nama = nvl(rs.getString(kolNama));
            penerima.terima(kode);
            target.setText(kode + " - " + nama);
            lblFoto.setIcon(ambilFotoTtd(nama));
        }
    }

    private void isiTanggalJam(widget.Tanggal komponen, java.sql.Date tgl, String jam) {
        if (tgl == null) {
            return;
        }
        String j = nvl(jam);
        try {
            komponen.setDate(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .parse(tgl + " " + (j.equals("") ? "00:00:00" : j)));
        } catch (Exception ignore) { }
    }

    private void simpan() {
        if (ambil(TNoRw).equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        List<String> kolom = new ArrayList<>();
        List<String> nilai = new ArrayList<>();
        for (Item it : semuaItem) {
            kolom.add(it.kolom);
            nilai.add(it.check.isSelected() ? "1" : "0");
            if (it.catatan != null) {
                kolom.add(it.kolom + "_ket");
                nilai.add(ambil(it.catatan));
            }
        }
        for (Map.Entry<String, widget.TextBox> e : semuaTeks.entrySet()) {
            kolom.add(e.getKey());
            nilai.add(ambil(e.getValue()));
        }
        for (TanggalField tf : semuaTanggal) {
            kolom.add(tf.kolom);
            Date d = tf.komponen.getDate();
            nilai.add(d == null ? "" : new java.text.SimpleDateFormat(tf.format).format(d));
        }
        kolom.add("kd_petugas1"); nilai.add(kdPetugas1);
        kolom.add("nama_petugas1"); nilai.add(ambilNamaDariTeks(tPetugas1));
        kolom.add("kd_petugas2"); nilai.add(kdPetugas2);
        kolom.add("nama_petugas2"); nilai.add(ambilNamaDariTeks(tPetugas2));
        kolom.add("kd_petugas3"); nilai.add(kdPetugas3);
        kolom.add("nama_petugas3"); nilai.add(ambilNamaDariTeks(tPetugas3));
        kolom.add("updated_by"); nilai.add(akses.getkode());

        String placeholder = ulang("?", kolom.size());
        StringBuilder sb = new StringBuilder("insert into askep_perioperatif (no_rawat,tanggal,jam,");
        sb.append(String.join(",", kolom));
        sb.append(",created_by,created_at) values (?,?,?,").append(placeholder).append(",?,now()) on duplicate key update tanggal=values(tanggal),jam=values(jam),");
        for (int i = 0; i < kolom.size(); i++) {
            sb.append(kolom.get(i)).append("=values(").append(kolom.get(i)).append(")");
            if (i < kolom.size() - 1) {
                sb.append(",");
            }
        }
        sb.append(",updated_at=now()");

        try (PreparedStatement ps = koneksi.prepareStatement(sb.toString())) {
            int idx = 1;
            ps.setString(idx++, ambil(TNoRw));
            setTglJam(ps, idx, dtpTanggal); idx += 2;
            for (String v : nilai) {
                ps.setString(idx++, v);
            }
            ps.setString(idx++, akses.getkode());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Askep Perioperatif tersimpan.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan.\n" + e.getMessage());
        }
    }

    private static String ulang(String token, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(i == 0 ? token : "," + token);
        }
        return sb.toString();
    }

    private String ambilNamaDariTeks(widget.TextBox t) {
        String s = ambil(t);
        int idx = s.indexOf(" - ");
        return idx < 0 ? s : s.substring(idx + 3).trim();
    }

    private void setTglJam(PreparedStatement ps, int idx, widget.Tanggal komponen) throws Exception {
        Date d = komponen.getDate();
        if (d == null) {
            ps.setNull(idx, java.sql.Types.DATE);
            ps.setString(idx + 1, "");
        } else {
            ps.setString(idx, new java.text.SimpleDateFormat("yyyy-MM-dd").format(d));
            ps.setString(idx + 1, new java.text.SimpleDateFormat("HH:mm:ss").format(d));
        }
    }

    private void hapus() {
        if (ambil(TNoRw).equals("")) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus askep perioperatif untuk No.Rawat " + ambil(TNoRw) + " ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement("delete from askep_perioperatif where no_rawat=?")) {
            ps.setString(1, ambil(TNoRw));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data dihapus.");
            setNoRm(ambil(TNoRw));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus.\n" + e.getMessage());
        }
    }

    public void cetak() {
        if (ambil(TNoRw).equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        if (Sequel.cariInteger("select count(*) from askep_perioperatif where no_rawat=?", ambil(TNoRw)) == 0) {
            JOptionPane.showMessageDialog(this, "Simpan data terlebih dahulu sebelum mencetak.");
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
            String sql = "select a.*,pasien.no_rkm_medis,pasien.nm_pasien,"
                    + "if(pasien.jk='L','Laki-laki','Perempuan') as jk,"
                    + "ifnull(date_format(pasien.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir,"
                    + "ifnull(date_format(a.tanggal,'%d-%m-%Y'),'') as tanggal_cetak,ifnull(a.jam,'') as jam_cetak,"
                    + fotoSql("a.nama_petugas1", "petugas1_photo") + ","
                    + fotoSql("a.nama_petugas2", "petugas2_photo") + ","
                    + fotoSql("a.nama_petugas3", "petugas3_photo") + " "
                    + "from askep_perioperatif a "
                    + "inner join reg_periksa on a.no_rawat=reg_periksa.no_rawat "
                    + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "where a.no_rawat='" + ambil(TNoRw) + "'";
            Valid.MyReportqry("rptAskepPerioperatif.jasper", "report", "::[ Askep Perioperatif ]::", sql, param);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak.\n" + e.getMessage());
        }
    }

    private String fotoSql(String kolomNama, String alias) {
        String sub = "(select p2.photo from pegawai p2 where lower(trim(p2.nama))=lower(trim(" + kolomNama + ")) limit 1)";
        return "if(coalesce(nullif(" + sub + ",''),'')='' or coalesce(nullif(" + sub + ",''),'')='-' "
                + "or coalesce(nullif(" + sub + ",''),'')='pages/pegawai/photo/','',"
                + "replace(coalesce(" + sub + ",''),'\\\\\\\\','/')) as " + alias;
    }

    private void ensureTable() {
        StringBuilder sql = new StringBuilder("create table if not exists askep_perioperatif ("
                + "no_rawat varchar(17) not null primary key,"
                + "tanggal date null,"
                + "jam varchar(8) null,"
                + "kd_petugas1 varchar(20) null,"
                + "nama_petugas1 varchar(60) null,"
                + "kd_petugas2 varchar(20) null,"
                + "nama_petugas2 varchar(60) null,"
                + "kd_petugas3 varchar(20) null,"
                + "nama_petugas3 varchar(60) null,"
                + "created_by varchar(50) null,"
                + "updated_by varchar(50) null,"
                + "created_at datetime null,"
                + "updated_at datetime null");
        for (Item it : semuaItem) {
            sql.append(",").append(it.kolom).append(" varchar(1) null");
            if (it.catatan != null) {
                sql.append(",").append(it.kolom).append("_ket varchar(150) null");
            }
        }
        for (String kolom : semuaTeks.keySet()) {
            sql.append(",").append(kolom).append(" varchar(150) null");
        }
        for (TanggalField tf : semuaTanggal) {
            sql.append(",").append(tf.kolom).append(" varchar(30) null");
        }
        sql.append(") ROW_FORMAT=DYNAMIC");
        Sequel.queryu2(sql.toString());
    }

    private ImageIcon ambilFotoTtd(String nama) {
        if (nama == null || nama.trim().isEmpty()) {
            return null;
        }
        String key = nama.trim().toLowerCase();
        if (cacheFotoTtd.containsKey(key)) {
            return cacheFotoTtd.get(key);
        }
        ImageIcon ic = null;
        try {
            String photo = bersihkanPathFotoTtd(Sequel.cariIsi(
                    "select photo from pegawai where lower(trim(nama))=lower(trim(?)) limit 1", nama));
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
        if (photo == null) {
            return "";
        }
        String p = photo.trim();
        if (p.equals("") || p.equals("-") || p.equals("pages/pegawai/photo/")) {
            return "";
        }
        return p.replace("\\", "/");
    }

    // ====================== Helpers UI (pola sama RM25) ======================
    private static widget.TextBox tf() {
        widget.TextBox t = new IsianDatar();
        gayaIsianCetak(t);
        return t;
    }

    private static widget.TextBox ro() {
        widget.TextBox t = new IsianDatar();
        gayaIsianCetak(t);
        t.setEditable(false);
        return t;
    }

    private static void gayaIsianCetak(widget.TextBox t) {
        t.setFont(FONT_FORM);
        t.setOpaque(false);
        t.setBackground(Color.WHITE);
        t.setBorder(new GarisTitikBawah());
    }

    /** TextBoxGlass tanpa kapsul, dengan titik-titik formulir yang selalu terlihat. */
    private static final class IsianDatar extends widget.TextBox {
        IsianDatar() {
            setUI(new BasicTextFieldUI());
        }

        @Override
        protected void paintComponent(Graphics g) {
            getUI().update(g, this);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(65, 65, 65));
            int y = getHeight() - 5;
            for (int x = 4; x < getWidth() - 3; x += 4) {
                g2.fillOval(x, y, 1, 1);
            }
            g2.dispose();
        }
    }

    private static final class GarisTitikBawah extends AbstractBorder {
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 3, 3, 3);
        }
    }

    private static widget.Tanggal dt() {
        widget.Tanggal d = new widget.Tanggal();
        d.setFont(FONT_FORM);
        d.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        return d;
    }

    private static widget.Tanggal tglSaja() {
        widget.Tanggal d = new widget.Tanggal();
        d.setFont(FONT_FORM);
        d.setDisplayFormat("dd-MM-yyyy");
        return d;
    }

    private JPanel fieldRingkasan(String label, Component komponen, boolean bacaSaja) {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(0, 10, 0, 10));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Times New Roman", Font.PLAIN, 12));
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

    private JPanel kartu(String judul, Color teks, Color garis) {
        JPanel luar = new JPanel(new GridBagLayout());
        luar.setBackground(Color.WHITE);
        luar.setBorder(BorderFactory.createLineBorder(new Color(116, 128, 138)));
        luar.setAlignmentX(Component.LEFT_ALIGNMENT);
        luar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6000));
        JLabel l = new JLabel(judul);
        l.setFont(FONT_FORM_BOLD);
        l.setForeground(teks);
        l.setHorizontalAlignment(JLabel.CENTER);
        l.setOpaque(true);
        l.setBackground(new Color(226, 237, 246));
        l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(116, 128, 138)));
        GridBagConstraints g = gc(0, 0, 4, 1.0);
        g.insets = new Insets(0, 0, 3, 0);
        luar.add(l, g);
        return luar;
    }

    private JPanel barisLabel(String label, Item... daftar) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 1));
        p.setOpaque(false);
        if (label != null && !label.isEmpty()) {
            JLabel l = new JLabel(label + " :");
            l.setFont(FONT_FORM);
            l.setForeground(new Color(49, 64, 75));
            l.setPreferredSize(new Dimension(240, 22));
            p.add(l);
        }
        for (Item it : daftar) {
            p.add(it.check);
            if (it.catatan != null) {
                p.add(bungkusKurung(it.catatan));
            }
        }
        return p;
    }

    private JPanel fieldKecil(String label, Component input) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(labelAtas(label));
        p.add(Box.createVerticalStrut(2));
        if (input instanceof widget.TextBox && ((widget.TextBox) input).isEditable()) {
            input = bungkusKurung(input);
        }
        input.setPreferredSize(new Dimension(150, 28));
        p.add(input);
        return p;
    }

    private JPanel fieldKecilLebar(String label, Component input) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(labelAtas(label));
        p.add(Box.createVerticalStrut(2));
        if (input instanceof widget.TextBox && ((widget.TextBox) input).isEditable()) {
            input = bungkusKurung(input);
        }
        input.setPreferredSize(new Dimension(480, 28));
        p.add(input);
        return p;
    }

    private JPanel barisField(JPanel... kolom) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        p.setOpaque(false);
        for (JPanel k : kolom) {
            p.add(k);
        }
        return p;
    }

    private int tambahBaris(JPanel kartuPanel, int row, JPanel isiBaris) {
        GridBagConstraints g = gc(0, row, 4, 1.0);
        g.insets = new Insets(1, 5, 2, 5);
        kartuPanel.add(isiBaris, g);
        return row + 1;
    }

    private int tunggalVertikal(JPanel p, int row, String label, Component komponen) {
        int barisLabel = (row * 2) + 1;
        int barisInput = barisLabel + 1;
        p.add(labelAtas(label), gc(0, barisLabel, 4, 1.0));
        // Jangan bentangkan baris picker sampai selebar kartu. Tombol "..." berada
        // di sisi EAST bungkusPicker, sehingga jika baris ikut melebar tombol akan
        // terdorong jauh ke pojok kanan pada layar lebar.
        komponen.setPreferredSize(new Dimension(430, 32));
        GridBagConstraints g = gc(0, barisInput, 4, 0.0);
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(1, 4, 8, 4);
        p.add(komponen, g);
        return row + 1;
    }

    private JLabel labelAtas(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(FONT_FORM);
        l.setForeground(new Color(49, 64, 75));
        return l;
    }

    private JPanel bungkusKurung(Component isian) {
        JPanel p = new JPanel(new BorderLayout(2, 0));
        p.setOpaque(false);
        JLabel buka = new JLabel("(");
        JLabel tutup = new JLabel(")");
        buka.setFont(FONT_FORM);
        tutup.setFont(FONT_FORM);
        p.add(buka, BorderLayout.WEST);
        p.add(isian, BorderLayout.CENTER);
        p.add(tutup, BorderLayout.EAST);
        p.setPreferredSize(new Dimension(175, 22));
        return p;
    }

    private JPanel bungkusFotoTtd(Component field, JLabel lblFoto) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        p.add(field, BorderLayout.CENTER);
        lblFoto.setPreferredSize(new Dimension(60, 28));
        p.add(lblFoto, BorderLayout.EAST);
        return p;
    }

    private JPanel bungkusPicker(Component field, Component tombol) {
        JPanel p = new JPanel(new BorderLayout(4, 0));
        p.setOpaque(false);
        p.add(field, BorderLayout.CENTER);
        p.add(tombol, BorderLayout.EAST);
        return p;
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

    private static String ambil(widget.TextBox t) {
        return t.getText() == null ? "" : t.getText().trim();
    }

    private static String nvl(String v) {
        return v == null ? "" : v;
    }
}
