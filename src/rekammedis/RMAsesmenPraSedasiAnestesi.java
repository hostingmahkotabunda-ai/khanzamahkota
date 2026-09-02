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
import java.awt.BasicStroke;
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
import kepegawaian.DlgCariDokter;
import kepegawaian.DlgCariPetugas;

/**
 * Evaluasi Pra-Sedasi/Pra-Anestesi + Asesmen Pra Induksi (RM 25, blok 4 &amp; 5
 * RM Operasi -- 1 lembar kertas bolak-balik jadi 1 form digital dengan 2 tab).
 * Semua item ceklis (kotak centang) disimpan generik lewat {@link Item} supaya
 * puluhan checkbox tidak perlu field terpisah satu-satu; field teks bebas
 * disimpan generik lewat {@code semuaTeks}.
 */
public final class RMAsesmenPraSedasiAnestesi extends JDialog {

    private static final Font FONT_FORM = new Font("Times New Roman", Font.PLAIN, 13);
    private static final Font FONT_FORM_BOLD = new Font("Times New Roman", Font.BOLD, 13);

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final Map<String, ImageIcon> cacheFotoTtd = new HashMap<>();
    // Picker modal diperlukan karena form ini sendiri dibuka secara modal dari
    // Dokumentasi RM Operasi; picker nonmodal tanpa owner akan terblokir di belakang.
    private final DlgCariDokter pickerDokter1 = new DlgCariDokter(null, true);
    private final DlgCariDokter pickerDokter2 = new DlgCariDokter(null, true);
    private final DlgCariPetugas pickerPetugas = new DlgCariPetugas(null, true);

    // Header identitas (readonly, sama pola RM22)
    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TJK = ro();
    private final widget.TextBox TTglLahir = ro();
    private final widget.Tanggal dtpTanggal = dt();

    // Vital sign awal (ditarik otomatis dari pemeriksaan_ranap, sama pola RM22)
    private final widget.TextBox tTd = tf();
    private final widget.TextBox tNadi1 = tf();
    private final widget.TextBox tRr1 = tf();
    private final widget.TextBox tSuhu1 = tf();
    private final widget.TextBox tBeratBadan = tf();
    private final widget.TextBox tTinggiBadan = tf();

    // Registry generik utk semua item ceklis & field teks bebas
    private final List<Item> semuaItem = new ArrayList<>();
    private final LinkedHashMap<String, widget.TextBox> semuaTeks = new LinkedHashMap<>();
    private final List<TanggalField> semuaTanggal = new ArrayList<>();

    // Field tanggal sungguhan (bukan free text) -- Tanggal Operasi & Tanggal/Jam Premedikasi
    private final widget.Tanggal dtpTanggalOperasi = tglSaja();
    private final widget.Tanggal dtpTglJamPremedikasi = dt();

    // Tanda tangan
    private final widget.TextBox tDokter1 = ro();
    private final widget.Button btnPilihDokter1 = new widget.Button();
    private final JLabel lblFotoDokter1 = new JLabel();
    private String kdDokter1 = "";

    private final widget.TextBox tPetugasPremedikasi = ro();
    private final widget.Button btnPilihPetugasPremedikasi = new widget.Button();
    private final JLabel lblFotoPetugasPremedikasi = new JLabel();
    private String kdPetugasPremedikasi = "";

    private final widget.TextBox tDokter2 = ro();
    private final widget.Button btnPilihDokter2 = new widget.Button();
    private final JLabel lblFotoDokter2 = new JLabel();
    private String kdDokter2 = "";

    private final widget.Button BtnBaru = new widget.Button();
    private final widget.Button BtnSimpan = new widget.Button();
    private final widget.Button BtnHapus = new widget.Button();
    private final widget.Button BtnCetak = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();
    private JTabbedPane tabHalaman;

    public RMAsesmenPraSedasiAnestesi(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Evaluasi Pra-Sedasi - Pra-Anestesi & Pra Induksi (RM 25) ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
        ensureTable();
        siapkanPicker();
        setSize(1150, 820);
        setMinimumSize(new Dimension(950, 640));
        setLocationRelativeTo(parent);
    }

    // ====================== Item ceklis generik ======================
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
                catatan.setPreferredSize(new Dimension(130, 22));
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

    /** Field tanggal sungguhan (widget.Tanggal) yg tetap ikut disimpan/dimuat generik lewat semuaTanggal. */
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

    private void daftarTanggal(String kolom, widget.Tanggal komponen, String format) {
        new TanggalField(kolom, komponen, format);
    }

    private static String slug(String opsi) {
        return opsi.toLowerCase().replace(" ", "").replace("/", "");
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
        JLabel judulUtama = new JLabel("Evaluasi Pra-Sedasi - Pra-Anestesi & Pra Induksi");
        judulUtama.setFont(new Font("Times New Roman", Font.BOLD, 20));
        judulUtama.setForeground(teks);
        JLabel subjudul = new JLabel("Form RM 25  •  Diisi tim anestesi sebelum tindakan operasi");
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

        tabHalaman = new JTabbedPane();
        tabHalaman.setFont(FONT_FORM_BOLD);
        tabHalaman.addTab("Pra-Sedasi - Pra-Anestesi", bungkusScroll(buatTab1(teks, garis)));
        tabHalaman.addTab("Pra Induksi", bungkusScroll(buatTab2(teks, garis)));
        getContentPane().add(tabHalaman, BorderLayout.CENTER);

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

    private JPanel buatTab1(Color teks, Color garis) {
        JPanel isi = new JPanel();
        isi.setBackground(new Color(246, 249, 251));
        isi.setBorder(new EmptyBorder(6, 8, 10, 8));
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));

        JPanel kartuInfo = kartu("Informasi Operasi", teks, garis);
        int r0 = 1;
        daftarTanggal("tanggal_operasi", dtpTanggalOperasi, "dd-MM-yyyy");
        r0 = tambahBaris(kartuInfo, r0, barisField(
                fieldKecil("Ruangan Poli", teks("ruangan_poli")),
                fieldKecil("Tanggal Operasi", dtpTanggalOperasi),
                fieldKecil("Tanggal / Jam Pemeriksaan", dtpTanggal)));
        r0 = tambahBaris(kartuInfo, r0, barisField(
                fieldKecilLebar("Diagnose Pra-Anestesi", bungkusKurung(teks("diagnosa_pra_anestesi"))),
                fieldKecilLebar("Rencana Tindakan", bungkusKurung(teks("rencana_operasi")))));
        isi.add(kartuInfo);
        isi.add(Box.createVerticalStrut(4));

        JPanel kartuRiwayat = kartu("Riwayat", teks, garis);
        int r1 = 1;
        String[][] riwayatList = {
            {"riwayat_operasi", "Riwayat Operasi / Anestesi"},
            {"riwayat_komplikasi", "Riwayat Komplikasi"},
            {"riwayat_anestesi", "Riwayat Anestesi"},
            {"riwayat_obat", "Obat Yang Dikonsumsi"},
            {"riwayat_alergi", "Riwayat Alergi"},
            {"riwayat_asma", "Riwayat Asma"},
            {"riwayat_rokok", "Riwayat Merokok / Alkohol"}
        };
        for (String[] rrow : riwayatList) {
            r1 = tambahBaris(kartuRiwayat, r1, barisLabel(rrow[1],
                    itemCatatan(rrow[0] + "_ada", "Ada"), item(rrow[0] + "_tidakada", "Tidak Ada")));
        }
        r1 = tambahBaris(kartuRiwayat, r1, barisLabel("Anamnesa Dari",
                item("anamnesa_pasien", "Pasien"), item("anamnesa_keluarga", "Keluarga"),
                itemCatatan("anamnesa_lainnya", "Lainnya")));
        r1 = tambahBaris(kartuRiwayat, r1, barisField(
                fieldKecil("TD (mmHg)", tTd), fieldKecil("Nadi (x/mnt)", tNadi1),
                fieldKecil("RR (x/mnt)", tRr1), fieldKecil("Suhu (°C)", tSuhu1)));
        r1 = tambahBaris(kartuRiwayat, r1, barisField(
                fieldKecil("Berat Badan (kg)", tBeratBadan), fieldKecil("Tinggi Badan (cm)", tTinggiBadan)));
        semuaTeks.put("td", tTd);
        semuaTeks.put("nadi", tNadi1);
        semuaTeks.put("rr", tRr1);
        semuaTeks.put("suhu", tSuhu1);
        semuaTeks.put("berat_badan", tBeratBadan);
        semuaTeks.put("tinggi_badan", tTinggiBadan);
        JPanel kartuNafas = kartu("Evaluasi Jalan Nafas", teks, garis);
        int r2 = 1;
        String[][] nafasList = {
            {"jalan_nafas_bebas", "Bebas", "Ya", "Tidak"},
            {"jalan_nafas_bukamulut", "Mampu Membuka Mulut", "Ya", "Tidak"},
            {"jalan_nafas_leher", "Leher", "DBN", "Pendek"},
            {"jalan_nafas_gerakleher", "Gerak Leher", "Bebas", "Terbatas"},
            {"jalan_nafas_protusi", "Protusi Mandibula", "Tidak", "Ya"},
            {"jalan_nafas_obesitas", "Obesitas", "Tidak", "Ya"},
            {"jalan_nafas_massa", "Massa", "Tidak", "Ya"},
            {"jalan_nafas_gigi", "Masalah Gigi Geligi", "Tidak", "Ya"},
            {"jalan_nafas_sulitventilasi", "Sulit Ventilasi", "Tidak", "Ya"}
        };
        for (String[] nrow : nafasList) {
            r2 = tambahBaris(kartuNafas, r2, barisLabel(nrow[1],
                    item(nrow[0] + "_" + slug(nrow[2]), nrow[2]), item(nrow[0] + "_" + slug(nrow[3]), nrow[3])));
        }
        r2 = tambahBaris(kartuNafas, r2, barisLabel("Malampathy",
                item("jalan_nafas_malampathy_1", "I"), item("jalan_nafas_malampathy_2", "II"),
                item("jalan_nafas_malampathy_3", "III"), item("jalan_nafas_malampathy_4", "IV")));
        isi.add(pasanganKartu(kartuRiwayat, kartuNafas, 3, 2));
        isi.add(Box.createVerticalStrut(4));

        JPanel kartuOrgan = kartu("Fungsi Sistem Organ", teks, garis);
        int r3 = 1;
        String[][] organList = {
            {"organ_pernafasan", "Pernafasan"}, {"organ_kardiovaskuler", "Kardio Vaskuler"},
            {"organ_neuromuskuloskeletal", "Neuro / Muskuloskeletal"},
            {"organ_hepatogastrointestinal", "Hepato / Gastrointestinal"},
            {"organ_renalendokrin", "Renal / Endokrin"}, {"organ_lain", "Lain-Lain"}
        };
        for (String[] orow : organList) {
            r3 = tambahBaris(kartuOrgan, r3, barisLabel(orow[1],
                    item(orow[0] + "_dbn", "DBN"), itemCatatan(orow[0] + "_masalah", "Masalah"),
                    item(orow[0] + "_tidakdiperiksa", "Tidak Diperiksa")));
        }
        isi.add(kartuOrgan);
        isi.add(Box.createVerticalStrut(4));

        JPanel kartuLab = kartu("Pemeriksaan Laboratorium & Penunjang", teks, garis);
        int r4 = 1;
        String[][] labList = {
            {"lab_hematologi", "Hematologi"}, {"lab_fungsiginjal", "Fungsi Ginjal"},
            {"lab_fungsihati", "Fungsi Hati"}, {"lab_ekg", "EKG"},
            {"lab_rontgen", "Rontgen"}, {"lab_lain", "Lain-Lain"}
        };
        for (String[] lrow : labList) {
            r4 = tambahBaris(kartuLab, r4, barisLabel(lrow[1],
                    item(lrow[0] + "_dbn", "DBN"), itemCatatan(lrow[0] + "_abnormal", "Abnormal"),
                    item(lrow[0] + "_tidakdiperiksa", "Tidak Diperiksa")));
        }
        isi.add(kartuLab);
        isi.add(Box.createVerticalStrut(4));

        JPanel kartuKesimpulan = kartu("Kesimpulan", teks, garis);
        int r5 = 1;
        r5 = tambahBaris(kartuKesimpulan, r5, barisField(fieldKecilLebar("Catatan", teks("catatan_kesimpulan"))));
        r5 = tambahBaris(kartuKesimpulan, r5, barisLabel("PS ASA",
                item("kesimpulan_psasa_1", "I"), item("kesimpulan_psasa_2", "II"),
                item("kesimpulan_psasa_3", "III"), item("kesimpulan_psasa_4", "IV"),
                item("kesimpulan_psasa_5", "V")));
        r5 = tambahBaris(kartuKesimpulan, r5, barisLabel("Penyulit",
                item("kesimpulan_penyulit_tidakada", "Tidak Ada"), itemCatatan("kesimpulan_penyulit_ada", "Ada")));
        r5 = tambahBaris(kartuKesimpulan, r5, barisLabel("Resiko Komplikasi",
                item("kesimpulan_resiko_tidakada", "Tidak Ada"), itemCatatan("kesimpulan_resiko_ada", "Ada")));
        isi.add(kartuKesimpulan);
        isi.add(Box.createVerticalStrut(4));

        JPanel kartuRencana = kartu("Rencana Tindakan", teks, garis);
        int r6 = 1;
        r6 = tambahBaris(kartuRencana, r6, barisLabel("Premedikasi",
                item("premedikasi_ranitidine", "Ranitidine"), item("premedikasi_ondansentron", "Ondansentron"),
                item("premedikasi_metokloperamide", "Metokloperamide"), item("premedikasi_ketamine", "Ketamine"),
                item("premedikasi_midazolam", "Midazolam"), itemCatatan("premedikasi_lainnya", "Lainnya")));
        r6 = tambahBaris(kartuRencana, r6, barisLabel("Regional Anestesi",
                item("regional_sab", "SAB"), item("regional_epidural", "Epidural"), item("regional_pnb", "PNB")));
        r6 = tambahBaris(kartuRencana, r6, barisLabel("General Anestesi",
                item("generalanestesi_masker", "Masker"), item("generalanestesi_tiva", "TIVA"),
                item("generalanestesi_intubasi", "Intubasi"), item("generalanestesi_lma", "LMA")));
        r6 = tambahBaris(kartuRencana, r6, barisLabel("Lokal Anestesi",
                item("lokal_lidocaine", "Lidocaine"), item("lokal_bupivacaine", "Bupivacaine"),
                item("lokal_ropivacaine", "Ropivacaine")));
        r6 = tambahBaris(kartuRencana, r6, barisLabel("Induksi",
                item("induksi_intravena", "Intravena"), itemCatatan("induksi_insuflasi", "Insuflasi")));
        r6 = tambahBaris(kartuRencana, r6, barisLabel("Sedasi",
                item("sedasi_midazolam", "Midazolam"), item("sedasi_propofol", "Propofol"),
                item("sedasi_tiopental", "Tiopental"), item("sedasi_kentamin", "Kentamin")));
        r6 = tambahBaris(kartuRencana, r6, barisLabel("Analgesik",
                item("analgesik_pathidine", "Pathidine"), item("analgesik_fentanyl", "Fentanyl"),
                item("analgesik_kentamin", "Kentamin"), item("analgesik_tramadol", "Tramadol")));
        r6 = tambahBaris(kartuRencana, r6, barisLabel("Pelumpuh Otot",
                item("pelumpuh_atracurium", "Atracurium"), item("pelumpuh_rucoronium", "Rucoronium")));
        r6 = tambahBaris(kartuRencana, r6, barisLabel("Mantenance",
                item("maintenance_o2", "O2"), item("maintenance_n2o", "N2O"),
                item("maintenance_isoflurane", "Isoflurane"), item("maintenance_sevoflurane", "Sevoflurane"),
                itemCatatan("maintenance_lainnya", "Lainnya")));
        r6 = tambahBaris(kartuRencana, r6, barisLabel("Mantenance (Intravena)",
                item("maintenance_iv_propofol", "Propofol"), item("maintenance_iv_fentanyl", "Fentanyl"),
                item("maintenance_iv_atracurium", "Atracurium")));
        JPanel kartuTtd1 = kartu("Dokter Anestesi (Penandatangan Halaman 1)", teks, garis);
        int r7 = 0;
        r7 = tunggalVertikal(kartuTtd1, r7, "Dokter",
                bungkusPicker(bungkusFotoTtd(tDokter1, lblFotoDokter1), btnPilihDokter1));
        isi.add(pasanganKartu(kartuRencana, kartuTtd1, 4, 1));
        isi.add(Box.createVerticalGlue());
        return isi;
    }

    private JPanel buatTab2(Color teks, Color garis) {
        JPanel isi = new JPanel();
        isi.setBackground(new Color(246, 249, 251));
        isi.setBorder(new EmptyBorder(6, 8, 10, 8));
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));

        JPanel kartuGigi = kartu("Status Gigi, Puasa & Alergi", teks, garis);
        int r1 = 1;
        r1 = tambahBaris(kartuGigi, r1, barisLabel("Gigi",
                item("gigi_lengkap", "Lengkap"), item("gigi_ompong", "Ompong"), item("gigi_goyang", "Goyang"),
                item("gigi_palsu_lepas", "Palsu (Lepas)"), item("gigi_palsu_tidaklepas", "Palsu (Tidak Lepas)")));
        r1 = tambahBaris(kartuGigi, r1, barisLabel("",
                itemCatatan("catatan2_riwayat", "Riwayat"), item("catatan2_asma", "Asma"),
                itemCatatan("catatan2_alergi", "Riwayat Alergi")));
        r1 = tambahBaris(kartuGigi, r1, barisLabel("Puasa",
                item("puasa_puasa", "Puasa"), item("puasa_tidakpuasa", "Tidak Puasa")));
        r1 = tambahBaris(kartuGigi, r1, barisField(
                fieldKecil("Makan Terakhir", teks("makan_terakhir")), fieldKecil("Minum Terakhir", teks("minum_terakhir"))));
        JPanel kartuPremed2 = kartu("Premedikasi (Diberikan)", teks, garis);
        int r2 = 1;
        String[][] premed2List = {
            {"premedikasi2_rantitidin", "Rantitidin (mg)"}, {"premedikasi2_metoklopramide", "Metoklopramide (mg)"},
            {"premedikasi2_ondansentron", "Ondansentron (mg)"}, {"premedikasi2_midazolam", "Midazolam (mg)"},
            {"premedikasi2_ketamine", "Ketamine (mg)"}
        };
        Item[] premed2Items = new Item[premed2List.length];
        for (int i = 0; i < premed2List.length; i++) {
            premed2Items[i] = itemCatatan(premed2List[i][0], premed2List[i][1]);
        }
        r2 = tambahBaris(kartuPremed2, r2, barisLabel("Agen", premed2Items));
        daftarTanggal("tgl_jam_premedikasi", dtpTglJamPremedikasi, "dd-MM-yyyy HH:mm:ss");
        r2 = tambahBaris(kartuPremed2, r2, barisField(
                fieldKecil("Diberikan Oleh", teks("diberikan_oleh")), fieldKecil("Tanggal / Jam", dtpTglJamPremedikasi)));
        r2 = tunggalVertikal(kartuPremed2, r2, "Perawat Premedikasi",
                bungkusPicker(bungkusFotoTtd(tPetugasPremedikasi, lblFotoPetugasPremedikasi), btnPilihPetugasPremedikasi));
        JPanel kartuKondisi = kartu("Kondisi Sebelum Induksi", teks, garis);
        int r3 = 1;
        r3 = tambahBaris(kartuKondisi, r3, barisField(fieldKecilLebar("Cara Masuk Pra-Anestesi", teks("cara_masuk_pra_anestesi"))));
        r3 = tambahBaris(kartuKondisi, r3, barisField(
                fieldKecil("TD", teks("td2")), fieldKecil("HR (x/mnt)", teks("hr2")),
                fieldKecil("RR (x/mnt)", teks("rr2")), fieldKecil("Suhu (°C)", teks("suhu2")),
                fieldKecil("SpO2 (%)", teks("spo2"))));
        r3 = tambahBaris(kartuKondisi, r3, barisLabel("Masalah Saat Induksi",
                item("induksi2_masalah_tidakada", "Tidak Ada"), itemCatatan("induksi2_masalah_ada", "Ada")));
        JPanel kolomKiriAtas = tumpukKartu(kartuGigi, kartuKondisi);
        isi.add(pasanganKartu(kolomKiriAtas, kartuPremed2, 7, 3));
        isi.add(Box.createVerticalStrut(4));

        JPanel kartuPraInduksi = kartu("Asesmen Pra Induksi (Checklist)", teks, garis);
        JPanel gridPraInduksi = new JPanel(new GridLayout(0, 3, 10, 4));
        gridPraInduksi.setOpaque(false);
        String[][] praInduksiList = {
            {"praindukai_identifikasipasien", "Identifikasi Pasien"}, {"praindukai_izinoperasi", "Izin Operasi"},
            {"praindukai_puasaterpenuhi", "Puasa Terpenuhi"}, {"praindukai_mesinanestesi", "Mesin Anestesi"},
            {"praindukai_antibiotikprofilaksis", "Antibiotik Profilaksis"}, {"praindukai_obatobatan", "Obat-Obatan"},
            {"praindukai_urinkateter", "Urin Kateter"}, {"praindukai_nibp", "NIBP"},
            {"praindukai_pulseoximeter", "Pulse Oxmeter"}, {"praindukai_suction", "Suction"},
            {"praindukai_penghangatcairan", "Penghangat Cairan"}, {"praindukai_stetoskopprecordial", "Stetoskop Precordial"},
            {"praindukai_ekgmonitor", "EKG"}, {"praindukai_selimutpenghangat", "Selimut Penghangat"},
            {"praindukai_titiktekananbantalan", "Titik Tekanan Diberi Bantalan"},
            {"praindukai_sabukpengaman", "Sabuk Pengaman"}, {"praindukai_mataterlindung", "Mata Terlindung"}
        };
        for (String[] prow : praInduksiList) {
            Item it = item(prow[0], prow[1]);
            gridPraInduksi.add(it.check);
        }
        GridBagConstraints gPra = gc(0, 1, 4, 1.0);
        gPra.insets = new Insets(1, 4, 8, 4);
        kartuPraInduksi.add(gridPraInduksi, gPra);
        isi.add(kartuPraInduksi);
        isi.add(Box.createVerticalStrut(4));

        JPanel kartuPosisi = kartu("Posisi, Lokasi Infus, Waktu Tindakan & Tim Operasi", teks, garis);
        int r4 = 1;
        r4 = tambahBaris(kartuPosisi, r4, barisLabel("Posisi",
                item("posisi_supine", "Supine"), item("posisi_prone", "Prone"),
                item("posisi_trendelenburg", "Trendelenburg"), item("posisi_lithotomy", "Lithotomy"),
                item("posisi_lateral", "Lateral"), itemCatatan("posisi_lainnya", "Lainnya")));
        r4 = tambahBaris(kartuPosisi, r4, barisLabel("Lokasi Infus",
                item("lokasiinfus_tangankanan", "Tangan Kanan"), item("lokasiinfus_tangankiri", "Tangan Kiri"),
                item("lokasiinfus_kakikanan", "Kaki Kanan"), item("lokasiinfus_kakikiri", "Kaki Kiri"),
                itemCatatan("lokasiinfus_cvc", "CVC"), itemCatatan("lokasiinfus_lainnya", "Lainnya")));
        r4 = tambahBaris(kartuPosisi, r4, barisField(
                fieldKecil("Pasien Masuk", teks("waktu_pasienmasuk")), fieldKecil("Mulai Anestesi", teks("waktu_mulaianestesi")),
                fieldKecil("Mulai Operasi", teks("waktu_mulaioperasi"))));
        r4 = tambahBaris(kartuPosisi, r4, barisField(
                fieldKecil("Selesai Operasi", teks("waktu_selesaioperasi")), fieldKecil("Selesai Anestesi", teks("waktu_selesaianestesi")),
                fieldKecil("Pasien Keluar", teks("waktu_pasienkeluar"))));
        r4 = tambahBaris(kartuPosisi, r4, barisField(
                fieldKecil("Spesialis Anestesiologi", teks("tim_anestesiologi")), fieldKecil("Penata Anestesi", teks("tim_penataanestesi")),
                fieldKecil("Spesialis Bedah", teks("tim_spesialisbedah"))));
        r4 = tambahBaris(kartuPosisi, r4, barisField(
                fieldKecil("Asisten 1 Bedah", teks("tim_asisten1")), fieldKecil("Asisten 2 Bedah", teks("tim_asisten2")),
                fieldKecil("Perawat Instrumen", teks("tim_perawatinstrumen"))));
        isi.add(kartuPosisi);
        isi.add(Box.createVerticalStrut(4));

        JPanel kartuProsedur = kartu("Prosedur Induksi", teks, garis);
        int r5 = 1;
        r5 = tambahBaris(kartuProsedur, r5, barisLabel("General Anestesi",
                item("prosedur_general_masker", "Masker"), item("prosedur_general_lma", "LMA"),
                item("prosedur_general_tiva", "TIVA")));
        r5 = tambahBaris(kartuProsedur, r5, barisLabel("SAB Lumbal Space - Level",
                item("sab_level_l2l3", "L2 L3"), item("sab_level_l3l4", "L3 L4"), item("sab_level_l4l5", "L4 L5")));
        r5 = tambahBaris(kartuProsedur, r5, barisLabel("Pucurtre",
                item("sab_pucurtre_median", "Median"), item("sab_pucurtre_paramedian", "Paramedian")));
        r5 = tambahBaris(kartuProsedur, r5, barisLabel("LCS",
                item("sab_lcs_jernih", "Jernih"), item("sab_lcs_keruh", "Keruh")));
        r5 = tambahBaris(kartuProsedur, r5, barisLabel("No. Spinocan",
                item("sab_spinocan_25", "25"), item("sab_spinocan_26", "26"), item("sab_spinocan_27", "27")));
        r5 = tambahBaris(kartuProsedur, r5, barisLabel("Barbutasc",
                item("sab_barbutasc_positif", "Positif"), item("sab_barbutasc_negatif", "Negatif")));
        r5 = tambahBaris(kartuProsedur, r5, barisField(
                fieldKecilLebar("Obat SAB", teks("sab_obat"))));
        r5 = tambahBaris(kartuProsedur, r5, barisField(
                fieldKecil("Epidural", teks("epidural_ket")), fieldKecil("PNB", teks("pnb_ket"))));
        r5 = tambahBaris(kartuProsedur, r5, barisField(fieldKecilLebar("Obat / Dosis / Rute", teks("obat_dosis_rute_catatan"))));
        r5 = tambahBaris(kartuProsedur, r5, barisLabel("Konversi Anestesi",
                itemCatatan("konversi_regionalgagal", "Regional Gagal")));
        r5 = tambahBaris(kartuProsedur, r5, barisField(fieldKecilLebar("Durasi Obat Regional", teks("durasi_obat_regional_ket"))));
        r5 = tambahBaris(kartuProsedur, r5, barisLabel("Pilihan",
                item("konversi_convertgeneral", "SAB / Epidural / PNB Convert to General Anestesi")));
        isi.add(kartuProsedur);
        isi.add(Box.createVerticalStrut(4));

        JPanel kartuAlat = kartu("Alat Bantu Pernafasan, Ventilasi & Inhalasi", teks, garis);
        int r6 = 1;
        r6 = tambahBaris(kartuAlat, r6, barisField(
                fieldKecil("OPA No.1", teks("opa1_ket")), fieldKecil("OPA No.2", teks("opa2_ket"))));
        r6 = tambahBaris(kartuAlat, r6, barisField(
                fieldKecil("LMA No.", teks("lma_ket")), fieldKecil("Cuff (ml)", teks("lma_cuff_ket"))));
        r6 = tambahBaris(kartuAlat, r6, barisLabel("ETT",
                item("alat_ett", "ETT"), item("alat_ettoral", "Oral"), item("alat_ettnasal", "Nasal")));
        r6 = tambahBaris(kartuAlat, r6, barisLabel("Oksigen",
                item("alat_maskeroksigen", "Masker Oksigen"), item("alat_kanuloksigen", "Kanul Oksigen"),
                item("alat_sungkup", "Sungkup"), item("alat_trakeostomi", "Trakeostomi")));
        r6 = tambahBaris(kartuAlat, r6, barisField(fieldKecil("Level Oksigen", teks("masker_level_ket"))));
        r6 = tambahBaris(kartuAlat, r6, barisLabel("Ventilasi",
                item("ventilasi_circuit", "Circuit"), item("ventilasi_spontan", "Spontan"),
                item("ventilasi_assistedsimv", "Assisted / SIMV"), item("ventilasi_control", "Control"),
                item("ventilasi_jacksonress", "Jackson Ress")));
        r6 = tambahBaris(kartuAlat, r6, barisField(
                fieldKecil("Tidal Volume", teks("tidal_volume_ket")), fieldKecil("Rate", teks("rate_ket")),
                fieldKecil("PEEP", teks("peep_ket"))));
        String[][] inhalasiList = {
            {"inhalasi_o2", "O2"}, {"inhalasi_no2", "N2O"}, {"inhalasi_airbar", "Air Bar"},
            {"inhalasi_isoflurane", "Isoflurane"}, {"inhalasi_sevoflurane", "Sevoflurane"}
        };
        Item[] inhalasiItems = new Item[inhalasiList.length];
        for (int i = 0; i < inhalasiList.length; i++) {
            inhalasiItems[i] = itemCatatan(inhalasiList[i][0], inhalasiList[i][1]);
        }
        r6 = tambahBaris(kartuAlat, r6, barisLabel("Inhalasi", inhalasiItems));
        r6 = tambahBaris(kartuAlat, r6, barisField(fieldKecilLebar("Inhalasi Lainnya", teks("inhalasi_lainnya_ket"))));
        JPanel kartuTtd2 = kartu("Dokter Anestesi (Penandatangan Halaman 2)", teks, garis);
        int r7 = 0;
        r7 = tunggalVertikal(kartuTtd2, r7, "Dokter",
                bungkusPicker(bungkusFotoTtd(tDokter2, lblFotoDokter2), btnPilihDokter2));
        isi.add(pasanganKartu(kartuAlat, kartuTtd2, 4, 1));
        isi.add(Box.createVerticalGlue());
        return isi;
    }

    // ====================== Picker TTD ======================
    private void siapkanPicker() {
        siapkanSatuPicker(btnPilihDokter1, pickerDokter1, tDokter1, lblFotoDokter1, kd -> kdDokter1 = kd);
        siapkanSatuPicker(btnPilihDokter2, pickerDokter2, tDokter2, lblFotoDokter2, kd -> kdDokter2 = kd);
        btnPilihPetugasPremedikasi.setText("...");
        btnPilihPetugasPremedikasi.setPreferredSize(new Dimension(32, 25));
        btnPilihPetugasPremedikasi.addActionListener(e -> {
            pickerPetugas.isCek();
            pickerPetugas.setSize(650, 400);
            pickerPetugas.setLocationRelativeTo(this);
            pickerPetugas.setVisible(true);
        });
        pickerPetugas.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (pickerPetugas.getTable().getSelectedRow() != -1) {
                    kdPetugasPremedikasi = pickerPetugas.getTable().getValueAt(pickerPetugas.getTable().getSelectedRow(), 0).toString();
                    String nama = pickerPetugas.getTable().getValueAt(pickerPetugas.getTable().getSelectedRow(), 1).toString();
                    tPetugasPremedikasi.setText(kdPetugasPremedikasi + " - " + nama);
                    lblFotoPetugasPremedikasi.setIcon(ambilFotoTtd(nama));
                }
            }
        });
    }

    private interface PenerimaKode {
        void terima(String kode);
    }

    private void siapkanSatuPicker(widget.Button tombol, DlgCariDokter picker, widget.TextBox target,
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
        for (widget.TextBox t : new widget.TextBox[]{TNoRw, TNoRM, TPasien, TJK, TTglLahir, tDokter1, tDokter2, tPetugasPremedikasi}) {
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
        dtpTanggal.setDate(new Date());
        for (TanggalField tf : semuaTanggal) {
            tf.komponen.setDate(new Date());
        }
        lblFotoDokter1.setIcon(null);
        lblFotoDokter2.setIcon(null);
        lblFotoPetugasPremedikasi.setIcon(null);
        kdDokter1 = "";
        kdDokter2 = "";
        kdPetugasPremedikasi = "";
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

    /** Dipanggil dari RMDokumentasiOperasi supaya klik blok 4 vs blok 5 langsung buka tab yg relevan. */
    public void pilihTab(int indeks) {
        if (tabHalaman != null && indeks >= 0 && indeks < tabHalaman.getTabCount()) {
            tabHalaman.setSelectedIndex(indeks);
        }
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
            System.out.println("Notif tarik data pasien RM25 : " + e);
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select tensi,nadi,respirasi,suhu_tubuh,tinggi,berat from pemeriksaan_ranap "
                + "where no_rawat=? order by tgl_perawatan desc,jam_rawat desc limit 1")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tTd.setText(nvl(rs.getString("tensi")));
                    tNadi1.setText(nvl(rs.getString("nadi")));
                    tRr1.setText(nvl(rs.getString("respirasi")));
                    tSuhu1.setText(nvl(rs.getString("suhu_tubuh")));
                    tTinggiBadan.setText(nvl(rs.getString("tinggi")));
                    tBeratBadan.setText(nvl(rs.getString("berat")));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik vital RM25 : " + e);
        }
        dtpTanggal.setDate(new Date());
    }

    private void muatDataJikaAda(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement("select * from asesmen_pra_sedasi_anestesi where no_rawat=?")) {
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
                    isiTtdJikaAda(rs, "kd_dokter1", "nama_dokter1", tDokter1, lblFotoDokter1, kd -> kdDokter1 = kd);
                    isiTtdJikaAda(rs, "kd_dokter2", "nama_dokter2", tDokter2, lblFotoDokter2, kd -> kdDokter2 = kd);
                    isiTtdJikaAda(rs, "kd_petugas_premedikasi", "nama_petugas_premedikasi",
                            tPetugasPremedikasi, lblFotoPetugasPremedikasi, kd -> kdPetugasPremedikasi = kd);
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat RM25 : " + e);
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
        kolom.add("kd_dokter1"); nilai.add(kdDokter1);
        kolom.add("nama_dokter1"); nilai.add(ambilNamaDariTeks(tDokter1));
        kolom.add("kd_petugas_premedikasi"); nilai.add(kdPetugasPremedikasi);
        kolom.add("nama_petugas_premedikasi"); nilai.add(ambilNamaDariTeks(tPetugasPremedikasi));
        kolom.add("kd_dokter2"); nilai.add(kdDokter2);
        kolom.add("nama_dokter2"); nilai.add(ambilNamaDariTeks(tDokter2));
        kolom.add("updated_by"); nilai.add(akses.getkode());

        String placeholder = ulang("?", kolom.size());
        StringBuilder sb = new StringBuilder("insert into asesmen_pra_sedasi_anestesi (no_rawat,tanggal,jam,");
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
            JOptionPane.showMessageDialog(this, "Evaluasi Pra-Sedasi/Pra-Anestesi & Pra Induksi tersimpan.");
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
        if (JOptionPane.showConfirmDialog(this, "Hapus evaluasi pra-sedasi/pra-anestesi untuk No.Rawat " + ambil(TNoRw) + " ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement("delete from asesmen_pra_sedasi_anestesi where no_rawat=?")) {
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
        if (Sequel.cariInteger("select count(*) from asesmen_pra_sedasi_anestesi where no_rawat=?", ambil(TNoRw)) == 0) {
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
                    + fotoSql("a.nama_dokter1", "dokter1_photo") + ","
                    + fotoSql("a.nama_dokter2", "dokter2_photo") + ","
                    + fotoSql("a.nama_petugas_premedikasi", "petugas_photo") + " "
                    + "from asesmen_pra_sedasi_anestesi a "
                    + "inner join reg_periksa on a.no_rawat=reg_periksa.no_rawat "
                    + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "where a.no_rawat='" + ambil(TNoRw) + "'";
            Valid.MyReportqry("rptAsesmenPraSedasiAnestesi.jasper", "report",
                    "::[ Evaluasi Pra-Sedasi - Pra-Anestesi & Pra Induksi ]::", sql, param);
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
        StringBuilder sql = new StringBuilder("create table if not exists asesmen_pra_sedasi_anestesi ("
                + "no_rawat varchar(17) not null primary key,"
                + "tanggal date null,"
                + "jam varchar(8) null,"
                + "kd_dokter1 varchar(20) null,"
                + "nama_dokter1 varchar(60) null,"
                + "kd_petugas_premedikasi varchar(20) null,"
                + "nama_petugas_premedikasi varchar(60) null,"
                + "kd_dokter2 varchar(20) null,"
                + "nama_dokter2 varchar(60) null,"
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

    // ====================== Helpers UI ======================
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

    /** Tampilan isian mengikuti formulir kertas: tanpa balok, hanya garis titik-titik. */
    private static void gayaIsianCetak(widget.TextBox t) {
        t.setFont(FONT_FORM);
        t.setOpaque(false);
        t.setBackground(Color.WHITE);
        t.setBorder(new GarisTitikBawah());
    }

    /**
     * TextBoxGlass bawaan Khanza selalu melukis kapsul. UI dasar dipakai khusus
     * form ini supaya yang terlihat hanya teks dan border titik-titik RM 25.
     */
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

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(70, 70, 70));
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    1f, new float[]{1.5f, 2.5f}, 0f));
            g2.drawLine(x + 2, y + height - 2, x + width - 2, y + height - 2);
            g2.dispose();
        }
    }

    private static widget.Tanggal dt() {
        widget.Tanggal d = new widget.Tanggal();
        d.setFont(FONT_FORM);
        d.setBorder(new GarisTitikBawah());
        d.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        return d;
    }

    private static widget.Tanggal tglSaja() {
        widget.Tanggal d = new widget.Tanggal();
        d.setFont(FONT_FORM);
        d.setBorder(new GarisTitikBawah());
        d.setDisplayFormat("dd-MM-yyyy");
        return d;
    }

    private JPanel fieldRingkasan(String label, Component komponen, boolean bacaSaja) {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(0, 10, 0, 10));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Times New Roman", Font.PLAIN, 11));
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

    /** Dua blok formulir dalam satu baris, seperti pembagian kolom pada RM 25 cetak. */
    private JPanel pasanganKartu(JPanel kiri, JPanel kanan, int bobotKiri, int bobotKanan) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6000));
        GridBagConstraints g1 = gc(0, 0, 1, bobotKiri);
        g1.weighty = 1.0;
        g1.fill = GridBagConstraints.BOTH;
        g1.insets = new Insets(0, 0, 0, 2);
        p.add(kiri, g1);
        GridBagConstraints g2 = gc(1, 0, 1, bobotKanan);
        g2.weighty = 1.0;
        g2.fill = GridBagConstraints.BOTH;
        g2.insets = new Insets(0, 2, 0, 0);
        p.add(kanan, g2);
        return p;
    }

    /** Menumpuk dua blok tanpa jarak kartu besar agar tampak sebagai tabel sambung. */
    private JPanel tumpukKartu(JPanel atas, JPanel bawah) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(atas);
        p.add(Box.createVerticalStrut(4));
        p.add(bawah);
        return p;
    }

    /** Satu baris label + N item ceklis (opsional catatan) dlm satu panel FlowLayout. */
    private JPanel barisLabel(String label, Item... daftar) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 1));
        p.setOpaque(false);
        if (label != null && !label.isEmpty()) {
            JLabel l = new JLabel(label + " :");
            l.setFont(FONT_FORM);
            l.setForeground(new Color(49, 64, 75));
            l.setPreferredSize(new Dimension(185, 22));
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

    /** Panel label di atas + input teks di bawah, utk disusun berjajar lewat {@link #barisField}. */
    private JPanel fieldKecil(String label, Component input) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(labelAtas(label));
        p.add(Box.createVerticalStrut(2));
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

    /** Tambah satu baris penuh (hasil barisLabel/barisField) ke kartu, kembalikan nomor baris berikutnya. */
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
        komponen.setPreferredSize(new Dimension(320, 32));
        GridBagConstraints g = gc(0, barisInput, 4, 1.0);
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

    /** Catatan opsi pada RM 25 ditulis sebagai (........), bukan kotak input. */
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
        p.setPreferredSize(new Dimension(145, 22));
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
