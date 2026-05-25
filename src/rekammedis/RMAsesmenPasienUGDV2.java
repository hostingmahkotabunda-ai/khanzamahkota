package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.AlphaComposite;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.imageio.ImageIO;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JasperCompileManager;
import widget.Button;
import widget.Table;
import widget.Tanggal;
import widget.TextArea;
import widget.TextBox;

public final class RMAsesmenPasienUGDV2 extends JDialog {
    private static final Color WARNA_BG = new Color(240, 244, 248);
    private static final Color WARNA_SURFACE = new Color(255, 255, 255);
    private static final Color WARNA_SURFACE_SOFT = new Color(248, 250, 252);
    private static final Color WARNA_BORDER = new Color(203, 213, 225);
    private static final Color WARNA_ACCENT = new Color(15, 118, 110);
    private static final Color WARNA_ACCENT_SOFT = new Color(204, 251, 241);
    private static final Color WARNA_TEXT = new Color(15, 23, 42);
    private static final Color WARNA_MUTED = new Color(100, 116, 139);
    private static final Color WARNA_FIELD = new Color(248, 250, 252);
    private static final Color WARNA_FIELD_READONLY = new Color(226, 232, 240);
    private static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    private static final int SIG_RESUSITASI = 1;
    private static final int SIG_EMERGENT = 2;
    private static final int SIG_URGENT = 3;
    private static final int SIG_NON_URGENT = 4;
    private static final int SIG_PERAWAT = 5;
    private static final int SIG_PENANGGUNG_JAWAB = 6;

    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private final SimpleDateFormat formatJam = new SimpleDateFormat("HH:mm");
    private final DefaultTableModel tabMode;

    private final TextBox TNoRw = buatTextBox(15);
    private final TextBox TNoRM = buatTextBox(15);
    private final TextBox TPasien = buatTextBox(35);
    private final TextBox TJk = buatTextBox(12);
    private final TextBox TTglLahir = buatTextBox(12);
    private final TextBox TNipPetugas = buatTextBox(12);
    private final TextBox TNamaPetugas = buatTextBox(28);

    private final Tanggal DTPTanggalMasuk = new Tanggal();
    private final TextBox TJamDatang = buatTextBox(8);
    private final TextBox TJamPeriksa = buatTextBox(8);

    private final javax.swing.JCheckBox ChkNonTrauma = buatCheckBox("NON TRAUMA");
    private final javax.swing.JCheckBox ChkKecelakaan = buatCheckBox("KECELAKAAN");
    private final javax.swing.JCheckBox ChkIntoksikasi = buatCheckBox("INTOKSIKASI");
    private final javax.swing.JCheckBox ChkKekerasan = buatCheckBox("KEKERASAN");
    private final javax.swing.JCheckBox ChkDoa = buatCheckBox("DOA");
    private final javax.swing.JCheckBox ChkTrauma = buatCheckBox("TRAUMA");
    private final javax.swing.JCheckBox ChkPenganiayaan = buatCheckBox("PENGANIAYAAN");
    private final javax.swing.JCheckBox ChkGigitan = buatCheckBox("GIGITAN");
    private final javax.swing.JCheckBox ChkSeksual = buatCheckBox("SEKSUAL");

    private final JComboBox<String> CmbRujukan = buatCombo("ADA", "TIDAK ADA");
    private final JComboBox<String> CmbRespons = buatCombo("ALERT", "PAIN", "VERBAL", "UNRESPONS");
    private final JComboBox<String> CmbStatusPernikahan = buatCombo("Belum Menikah", "Menikah", "Duda/Janda (Meninggal)", "Duda/Janda (Bercerai)");
    private final JComboBox<String> CmbAnak = buatCombo("Tidak Ada", "Ada");
    private final TextBox TJumlahAnak = buatTextBox(10);
    private final javax.swing.JCheckBox ChkMerokok = buatCheckBox("Merokok");
    private final javax.swing.JCheckBox ChkAlkohol = buatCheckBox("Alkohol");
    private final JComboBox<String> CmbWargaNegara = buatCombo("WNI", "WNA", "Lainnya");
    private final TextBox TWargaNegaraKet = buatTextBox(20);
    private final TextBox TPekerjaan = buatTextBox(20);
    private final JComboBox<String> CmbAgama = buatCombo("Islam", "Hindu", "Budha", "Kristen", "Katolik", "Lainnya");
    private final javax.swing.JCheckBox ChkTinggalSuamiIstri = buatCheckBox("Suami/Istri");
    private final javax.swing.JCheckBox ChkTinggalAnak = buatCheckBox("Anak");
    private final javax.swing.JCheckBox ChkTinggalOrangtua = buatCheckBox("Orangtua");
    private final javax.swing.JCheckBox ChkTinggalSendiri = buatCheckBox("Sendiri");
    private final javax.swing.JCheckBox ChkTinggalLainnya = buatCheckBox("Lainnya");
    private final TextBox TTinggalKet = buatTextBox(20);
    private final TextBox TNoTelp = buatTextBox(20);
    private final JComboBox<String> CmbMasalahBicara = buatCombo("Tidak", "Ya");
    private final TextBox TMasalahBicaraKet = buatTextBox(24);
    private final TextArea AreaKeluhanRps = buatTextArea(4);

    private final TextBox TTD = buatTextBox(14);
    private final TextBox TRR = buatTextBox(10);
    private final TextBox TNadi = buatTextBox(10);
    private final TextBox TSuhu = buatTextBox(10);
    private final TextBox TAlergiMakanan = buatTextBox(20);
    private final TextBox TAlergiObat = buatTextBox(20);
    private final TextBox TAlergiLain = buatTextBox(20);

    private final JComboBox<String> CmbKategoriTriase = buatCombo("RESUSITASI", "EMERGENT", "URGENT", "NON URGENT");
    private final JComboBox<String> CmbProfesiPetugasTriase = buatCombo("Perawat", "Dokter");
    private final TextBox TNamaPetugasTriase = buatTextBox(24);
    private final JComboBox<String> CmbJalanNafas = buatCombo("Sumbatan", "Bebas");
    private final JComboBox<String> CmbPernafasan = buatCombo("Henti Nafas / Frek Nafas < 10x/menit / Sianosis", "Frek Nafas > 32x/menit / Mengi", "Frek Nafas 24-32x/menit / Mengi", "Frek Nafas 16-29x/menit");
    private final JComboBox<String> CmbSirkulasi = buatCombo("Henti Jantung / Nadi tidak teraba / Pucat / Akral dingin", "Nadi lemah / Frek Nadi < 50 atau > 150 / CRT < 2 detik", "Frek Nadi 120-150 / TD sistol > 160 / TD diastol > 100", "Frek Nadi 80-120 / TD sistol 120-160 / TD diastol 80-100");
    private final JComboBox<String> CmbKesadaran = buatCombo("GCS < 8", "GCS 9 - 12", "GCS > 12", "GCS 15");

    private final TextBox TMorseSkor = buatTextBox(10);
    private final JComboBox<String> CmbMorseResiko = buatCombo("Resiko Rendah 0-24", "Resiko Sedang 25-50", "Resiko Tinggi >= 51");
    private final TextBox TNutrisiSkor = buatTextBox(10);
    private final JComboBox<String> CmbNutrisiResiko = buatCombo("Berisiko Rendah 0", "Berisiko Sedang 1-3", "Berisiko Tinggi 4-5");
    private final JComboBox<String> CmbNyeriDewasa = buatCombo("Tidak Nyeri", "Ringan (1-3)", "Sedang (4-6)", "Berat (7-10)");
    private final JComboBox<String> CmbNyeriAnak = buatCombo("Tidak Nyeri", "Ringan (1-3)", "Sedang (4-6)", "Berat (7-10)");
    private final TextBox TLokasiNyeri = buatTextBox(20);
    private final TextBox TNyeriHilang = buatTextBox(20);
    private final TextBox THumptySkor = buatTextBox(10);
    private final JComboBox<String> CmbHumptyResiko = buatCombo("Resiko Rendah 7-11", "Resiko Tinggi >= 12");
    private final TextArea AreaKeterangan = buatTextArea(3);

    private final TextBox TNamaPerawat = buatTextBox(24);
    private final TextBox TPenanggungJawab = buatTextBox(24);
    private final TextArea AreaPemeriksaanFisik = buatTextArea(4);
    private final TextArea AreaPemeriksaanPenunjang = buatTextArea(4);
    private final TextArea AreaDiagnosisMasalah = buatTextArea(4);
    private final TextArea AreaRencana = buatTextArea(4);
    private final TextArea AreaInstruksi = buatTextArea(4);
    private final TextBox TNamaDokter = buatTextBox(24);
    private final TextBox TNamaDokterInstruksi = buatTextBox(24);

    private final JComboBox<String> CmbStatusKeluar = buatCombo("OPNAME DI RUANGAN", "DIPULANGKAN", "DIRUJUK", "MENINGGAL");
    private final Tanggal DTPTanggalKeluar = new Tanggal();
    private final TextBox TJamKeluar = buatTextBox(8);
    private final TextBox TKeadaanUmumKeluar = buatTextBox(16);
    private final TextBox TTDKeluar = buatTextBox(10);
    private final TextBox TRRKeluar = buatTextBox(10);
    private final TextBox TSpO2Keluar = buatTextBox(10);
    private final TextBox THRKeluar = buatTextBox(10);
    private final TextBox TTempKeluar = buatTextBox(10);
    private final TextBox TGCSKeluar = buatTextBox(10);
    private final TextBox TOpnameRuangan = buatTextBox(22);
    private final TextArea AreaIndikasiMasuk = buatTextArea(3);
    private final TextBox TKontrolKe = buatTextBox(22);
    private final TextBox TDirujukKe = buatTextBox(22);
    private final TextArea AreaAlasanDirujuk = buatTextArea(3);
    private final TextBox TMeninggalJam = buatTextBox(8);
    private final TextArea AreaPenyebabMeninggal = buatTextArea(3);
    private byte[] ttdResusitasi;
    private byte[] ttdEmergent;
    private byte[] ttdUrgent;
    private byte[] ttdNonUrgent;
    private byte[] ttdPerawat;
    private byte[] ttdPenanggungJawab;
    private final JLabel LblTtdResusitasi = buatLabelSignature();
    private final JLabel LblTtdEmergent = buatLabelSignature();
    private final JLabel LblTtdUrgent = buatLabelSignature();
    private final JLabel LblTtdNonUrgent = buatLabelSignature();
    private final JLabel LblTtdPerawat = buatLabelSignature();
    private final JLabel LblTtdPenanggungJawab = buatLabelSignature();
    private JPanel PnlTtdResusitasi;
    private JPanel PnlTtdEmergent;
    private JPanel PnlTtdUrgent;
    private JPanel PnlTtdNonUrgent;
    private JPanel PnlTtdTriase;

    private final Tanggal DTPCari1 = new Tanggal();
    private final Tanggal DTPCari2 = new Tanggal();
    private final TextBox TCari = buatTextBox(30);
    private final Table tbData = new Table();

    private final Button BtnSimpan = buatButton("Simpan");
    private final Button BtnEdit = buatButton("Ubah");
    private final Button BtnHapus = buatButton("Hapus");
    private final Button BtnBaru = buatButton("Baru");
    private final Button BtnPrint = buatButton("Preview Jasper");
    private final Button BtnTutup = buatButton("Tutup");
    private final Button BtnCari = buatButton("Cari");
    private final JPopupMenu PopupData = new JPopupMenu();
    private final JMenuItem MnBukaForm = new JMenuItem("Buka/Edit Form");
    private final JMenuItem MnPreviewJasper = new JMenuItem("Preview Jasper");
    private final JMenuItem MnCetakPdf = new JMenuItem("Cetak PDF");

    private final JTabbedPane TabUtama = new JTabbedPane();

    public RMAsesmenPasienUGDV2(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("Asesmen Pasien UGD V2");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
        tabMode = new DefaultTableModel(null, new Object[]{
            "No.Rawat", "No.RM", "Nama Pasien", "Tanggal", "Jam Datang", "Respons", "Triase", "Status Keluar", "Dokter", "Petugas"
        }) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tbData.setModel(tabMode);
        tbData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbData.setPreferredScrollableViewportSize(new Dimension(900, 260));
        tbData.setAutoResizeMode(Table.AUTO_RESIZE_OFF);
        tbData.getColumnModel().getColumn(0).setPreferredWidth(110);
        tbData.getColumnModel().getColumn(1).setPreferredWidth(80);
        tbData.getColumnModel().getColumn(2).setPreferredWidth(180);
        tbData.getColumnModel().getColumn(3).setPreferredWidth(90);
        tbData.getColumnModel().getColumn(4).setPreferredWidth(85);
        tbData.getColumnModel().getColumn(5).setPreferredWidth(90);
        tbData.getColumnModel().getColumn(6).setPreferredWidth(100);
        tbData.getColumnModel().getColumn(7).setPreferredWidth(130);
        tbData.getColumnModel().getColumn(8).setPreferredWidth(150);
        tbData.getColumnModel().getColumn(9).setPreferredWidth(150);
        aturTemaVisual();
        initPopupData();
        aktifkanEvent();
        aturFieldReadonly();
        setTanggalAwal();
        sinkronkanPanelTtdTriase();
        ensureTable();
        tampilData();
        isCek();
        pack();
        if (getWidth() < 1220 || getHeight() < 820) {
            setSize(Math.max(1220, getWidth()), Math.max(820, getHeight()));
        }
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        getContentPane().setLayout(new BorderLayout(0, 12));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(14, 14, 14, 14));
        ((JComponent) getContentPane()).setBackground(WARNA_BG);

        JPanel panelUtama = new JPanel(new BorderLayout(0, 12));
        panelUtama.setOpaque(false);
        panelUtama.add(buatPanelHero(), BorderLayout.NORTH);
        panelUtama.add(TabUtama, BorderLayout.CENTER);

        JScrollPane scrollForm = new JScrollPane(bangunFormPanel());
        aturScrollPane(scrollForm, true);
        getContentPane().add(panelAksi(), BorderLayout.SOUTH);
        getContentPane().add(panelUtama, BorderLayout.CENTER);
        TabUtama.addTab("Form Asesmen", scrollForm);
        TabUtama.addTab("Data Tersimpan", bangunPanelList());
    }

    private JPanel panelAksi() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panel.setOpaque(true);
        panel.setBackground(WARNA_SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARNA_BORDER),
            new EmptyBorder(10, 12, 10, 12)
        ));
        panel.add(BtnBaru);
        panel.add(BtnSimpan);
        panel.add(BtnEdit);
        panel.add(BtnHapus);
        panel.add(BtnPrint);
        panel.add(BtnTutup);
        return panel;
    }

    private JPanel bangunFormPanel() {
        JPanel pembungkus = new JPanel(new BorderLayout());
        pembungkus.setOpaque(false);
        pembungkus.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(WARNA_SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARNA_BORDER),
            new EmptyBorder(18, 18, 18, 18)
        ));
        GridBagConstraints gbc = dasarGbc();
        int row = 0;

        row = tambahJudul(panel, row, "Identitas Pasien");
        row = tambahDuaKolom(panel, row, "No. Rawat", TNoRw, "No. RM", TNoRM);
        row = tambahDuaKolom(panel, row, "Nama Pasien", TPasien, "Jenis Kelamin", TJk);
        row = tambahDuaKolom(panel, row, "Tgl. Lahir", TTglLahir, "Petugas", flowPanel(TNipPetugas, TNamaPetugas));

        row = tambahJudul(panel, row, "Asesmen Awal UGD");
        row = tambahDuaKolom(panel, row, "Tanggal Masuk", DTPTanggalMasuk, "Jam Datang", TJamDatang);
        row = tambahDuaKolom(panel, row, "Jam Periksa", TJamPeriksa, "Rujukan", CmbRujukan);
        row = tambahSatuKolom(panel, row, "Jenis Kasus", flowPanel(
            ChkNonTrauma, ChkKecelakaan, ChkIntoksikasi, ChkKekerasan, ChkDoa,
            ChkTrauma, ChkPenganiayaan, ChkGigitan, ChkSeksual
        ));
        row = tambahSatuKolom(panel, row, "Respons", CmbRespons);

        row = tambahJudul(panel, row, "Biopsikososial");
        row = tambahDuaKolom(panel, row, "Status Pernikahan", CmbStatusPernikahan, "Anak", flowPanel(CmbAnak, new JLabel("Jumlah"), TJumlahAnak));
        row = tambahSatuKolom(panel, row, "Kebiasaan", flowPanel(ChkMerokok, ChkAlkohol));
        row = tambahDuaKolom(panel, row, "Warga Negara", flowPanel(CmbWargaNegara, TWargaNegaraKet), "Pekerjaan", TPekerjaan);
        row = tambahDuaKolom(panel, row, "Agama", CmbAgama, "No. Telp", TNoTelp);
        row = tambahSatuKolom(panel, row, "Tinggal Bersama", flowPanel(ChkTinggalSuamiIstri, ChkTinggalAnak, ChkTinggalOrangtua, ChkTinggalSendiri, ChkTinggalLainnya, TTinggalKet));
        row = tambahSatuKolom(panel, row, "Masalah Dalam Berbicara", flowPanel(CmbMasalahBicara, TMasalahBicaraKet));
        row = tambahArea(panel, row, "Keluhan & RPS", AreaKeluhanRps, 90);

        row = tambahJudul(panel, row, "Tanda Vital & Triase");
        row = tambahDuaKolom(panel, row, "Tekanan Darah", TTD, "RR / Respirasi", TRR);
        row = tambahDuaKolom(panel, row, "Frekuensi Nadi", TNadi, "Suhu", TSuhu);
        row = tambahDuaKolom(panel, row, "Alergi Makanan", TAlergiMakanan, "Alergi Obat", TAlergiObat);
        row = tambahSatuKolom(panel, row, "Alergi Lain", TAlergiLain);
        row = tambahDuaKolom(panel, row, "Kategori Triase", CmbKategoriTriase, "Jalan Nafas", CmbJalanNafas);
        row = tambahDuaKolom(panel, row, "Pernafasan", CmbPernafasan, "Sirkulasi", CmbSirkulasi);
        row = tambahSatuKolom(panel, row, "Kesadaran", CmbKesadaran);
        row = tambahDuaKolom(panel, row, "Profesi Petugas Triase", CmbProfesiPetugasTriase, "Nama Petugas Triase", TNamaPetugasTriase);

        row = tambahJudul(panel, row, "Risiko & Skoring");
        row = tambahDuaKolom(panel, row, "Morse Skor", TMorseSkor, "Morse Resiko", CmbMorseResiko);
        row = tambahDuaKolom(panel, row, "Nutrisi Skor", TNutrisiSkor, "Nutrisi Resiko", CmbNutrisiResiko);
        row = tambahDuaKolom(panel, row, "Skala Nyeri Dewasa", CmbNyeriDewasa, "Skala Nyeri Anak", CmbNyeriAnak);
        row = tambahDuaKolom(panel, row, "Lokasi Nyeri", TLokasiNyeri, "Nyeri Hilang Bila", TNyeriHilang);
        row = tambahDuaKolom(panel, row, "Humpty Skor", THumptySkor, "Humpty Resiko", CmbHumptyResiko);
        row = tambahArea(panel, row, "Keterangan", AreaKeterangan, 80);
        row = tambahDuaKolom(panel, row, "Nama Perawat", TNamaPerawat, "Penanggung Jawab", TPenanggungJawab);
        row = tambahJudul(panel, row, "Tanda Tangan");
        row = tambahSatuKolom(panel, row, "Input TTD", panelTandaTangan());

        row = tambahJudul(panel, row, "Pemeriksaan & Rencana");
        row = tambahArea(panel, row, "Pemeriksaan Fisik", AreaPemeriksaanFisik, 90);
        row = tambahArea(panel, row, "Pemeriksaan Penunjang", AreaPemeriksaanPenunjang, 90);
        row = tambahArea(panel, row, "Diagnosis Sementara / Masalah", AreaDiagnosisMasalah, 90);
        row = tambahArea(panel, row, "Rencana", AreaRencana, 90);
        row = tambahArea(panel, row, "Instruksi", AreaInstruksi, 90);
        row = tambahDuaKolom(panel, row, "Nama Dokter", TNamaDokter, "Nama Dokter Instruksi", TNamaDokterInstruksi);

        row = tambahJudul(panel, row, "Pasien Keluar IGD");
        row = tambahDuaKolom(panel, row, "Status Keluar", CmbStatusKeluar, "Tanggal Keluar", DTPTanggalKeluar);
        row = tambahDuaKolom(panel, row, "Jam Keluar", TJamKeluar, "K/U", TKeadaanUmumKeluar);
        row = tambahDuaKolom(panel, row, "TD", TTDKeluar, "RR", TRRKeluar);
        row = tambahDuaKolom(panel, row, "SpO2", TSpO2Keluar, "HR", THRKeluar);
        row = tambahDuaKolom(panel, row, "Temp", TTempKeluar, "GCS", TGCSKeluar);
        row = tambahSatuKolom(panel, row, "Opname di Ruangan", TOpnameRuangan);
        row = tambahArea(panel, row, "Indikasi Masuk", AreaIndikasiMasuk, 75);
        row = tambahDuaKolom(panel, row, "Kontrol Ke", TKontrolKe, "Dirujuk Ke", TDirujukKe);
        row = tambahArea(panel, row, "Alasan Dirujuk", AreaAlasanDirujuk, 75);
        row = tambahDuaKolom(panel, row, "Meninggal Jam", TMeninggalJam, "Penyebab", scrollArea(AreaPenyebabMeninggal, 75));

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JLabel(""), gbc);
        pembungkus.add(panel, BorderLayout.NORTH);
        return pembungkus;
    }

    private JPanel bangunPanelList() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.add(bungkusKartu(panelFilter(), 12, 12), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(tbData);
        aturScrollPane(scroll, false);
        panel.add(bungkusKartu(scroll, 12, 12), BorderLayout.CENTER);
        return panel;
    }

    private JPanel panelFilter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        panel.add(labelFilter("Tanggal"));
        panel.add(DTPCari1);
        panel.add(labelFilter("s.d"));
        panel.add(DTPCari2);
        panel.add(labelFilter("Cari"));
        panel.add(TCari);
        panel.add(BtnCari);
        return panel;
    }

    private void initPopupData() {
        PopupData.setBorder(BorderFactory.createLineBorder(WARNA_BORDER));
        PopupData.setBackground(WARNA_SURFACE);
        aturMenuItem(MnBukaForm);
        aturMenuItem(MnPreviewJasper);
        aturMenuItem(MnCetakPdf);
        PopupData.add(MnBukaForm);
        PopupData.add(MnPreviewJasper);
        PopupData.add(MnCetakPdf);
        tbData.setComponentPopupMenu(PopupData);
    }

    private JPanel panelTandaTangan() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        PnlTtdResusitasi = panelKartuSignature("Triage Resusitasi", SIG_RESUSITASI);
        PnlTtdEmergent = panelKartuSignature("Triage Emergent", SIG_EMERGENT);
        PnlTtdUrgent = panelKartuSignature("Triage Urgent", SIG_URGENT);
        PnlTtdNonUrgent = panelKartuSignature("Triage Non Urgent", SIG_NON_URGENT);

        PnlTtdTriase = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        PnlTtdTriase.setOpaque(false);
        PnlTtdTriase.add(PnlTtdResusitasi);
        PnlTtdTriase.add(PnlTtdEmergent);
        PnlTtdTriase.add(PnlTtdUrgent);
        PnlTtdTriase.add(PnlTtdNonUrgent);

        JPanel panelPetugas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelPetugas.setOpaque(false);
        panelPetugas.add(panelKartuSignature("Nama Perawat", SIG_PERAWAT));
        panelPetugas.add(panelKartuSignature("Penanggung Jawab", SIG_PENANGGUNG_JAWAB));

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(PnlTtdTriase, gbc);

        gbc.gridy = 1;
        panel.add(panelPetugas, gbc);
        sinkronkanPanelTtdTriase();
        return panel;
    }

    private JPanel panelKartuSignature(final String judul, final int tipe) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(true);
        panel.setBackground(WARNA_SURFACE_SOFT);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARNA_BORDER),
            new EmptyBorder(8, 8, 8, 8)
        ));
        panel.setPreferredSize(new Dimension(190, 126));

        JLabel title = new JLabel(judul);
        title.setFont(FONT_LABEL);
        title.setForeground(WARNA_TEXT);

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        tombol.setOpaque(false);
        Button btnGambar = buatButtonMini("Gambar");
        btnGambar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bukaEditorTtd(judul, tipe);
            }
        });
        Button btnHapus = buatButtonMini("Hapus");
        btnHapus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSignatureData(tipe, null);
            }
        });
        tombol.add(btnGambar);
        tombol.add(btnHapus);

        panel.add(title, BorderLayout.NORTH);
        panel.add(getSignatureLabel(tipe), BorderLayout.CENTER);
        panel.add(tombol, BorderLayout.SOUTH);
        return panel;
    }

    private void aktifkanEvent() {
        BtnCari.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tampilData();
            }
        });
        BtnBaru.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                emptTeks();
            }
        });
        BtnSimpan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                simpanAtauPerbarui(false);
            }
        });
        BtnEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                simpanAtauPerbarui(true);
            }
        });
        BtnHapus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hapusData();
            }
        });
        BtnPrint.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                previewJasper();
            }
        });
        BtnTutup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        CmbKategoriTriase.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sinkronkanPanelTtdTriase();
            }
        });
        TCari.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    tampilData();
                }
            }
        });
        tbData.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2 && tbData.getSelectedRow() != -1) {
                    bukaDariTabel(tbData.getValueAt(tbData.getSelectedRow(), 0).toString());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                tampilkanPopupData(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                tampilkanPopupData(e);
            }
        });
        MnBukaForm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bukaBarisTerpilih();
            }
        });
        MnPreviewJasper.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bukaBarisTerpilih();
                previewJasper();
            }
        });
        MnCetakPdf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bukaBarisTerpilih();
                cetakPdf();
            }
        });
    }

    private void aturFieldReadonly() {
        aturTextReadonly(TNoRw);
        aturTextReadonly(TNoRM);
        aturTextReadonly(TPasien);
        aturTextReadonly(TJk);
        aturTextReadonly(TTglLahir);
        aturTextReadonly(TNipPetugas);
        aturTextReadonly(TNamaPetugas);
    }

    private void setTanggalAwal() {
        Date sekarang = new Date();
        DTPTanggalMasuk.setDate(sekarang);
        DTPTanggalKeluar.setDate(sekarang);
        DTPCari1.setDate(sekarang);
        DTPCari2.setDate(sekarang);
        String jam = formatJam.format(sekarang);
        TJamDatang.setText(jam);
        TJamPeriksa.setText(jam);
        TJamKeluar.setText(jam);
        TMeninggalJam.setText(jam);
    }

    private void ensureTable() {
        Sequel.queryu(
            "create table if not exists asesmen_pasien_ugd_v2 (" +
            "no_rawat varchar(17) not null," +
            "tanggal_asesmen date," +
            "jam_datang varchar(8)," +
            "jam_periksa varchar(8)," +
            "jenis_kasus text," +
            "rujukan varchar(30)," +
            "respons varchar(30)," +
            "status_pernikahan varchar(60)," +
            "anak_status varchar(30)," +
            "jumlah_anak varchar(20)," +
            "kebiasaan varchar(120)," +
            "warga_negara varchar(30)," +
            "warga_negara_keterangan varchar(120)," +
            "pekerjaan varchar(120)," +
            "agama varchar(30)," +
            "tinggal_bersama varchar(200)," +
            "tinggal_keterangan varchar(120)," +
            "no_telp varchar(50)," +
            "masalah_bicara varchar(30)," +
            "masalah_bicara_keterangan varchar(150)," +
            "keluhan_rps longtext," +
            "tekanan_darah varchar(30)," +
            "respirasi varchar(30)," +
            "frekuensi_nadi varchar(30)," +
            "suhu varchar(30)," +
            "alergi_makanan varchar(150)," +
            "alergi_obat varchar(150)," +
            "alergi_lain varchar(150)," +
            "kategori_triase varchar(30)," +
            "jalan_nafas varchar(150)," +
            "pernafasan varchar(150)," +
            "sirkulasi varchar(150)," +
            "kesadaran varchar(50)," +
            "morse_skor varchar(20)," +
            "morse_resiko varchar(100)," +
            "nutrisi_skor varchar(20)," +
            "nutrisi_resiko varchar(100)," +
            "nyeri_dewasa varchar(50)," +
            "nyeri_anak varchar(50)," +
            "lokasi_nyeri varchar(150)," +
            "nyeri_hilang_bila varchar(150)," +
            "humpty_skor varchar(20)," +
            "humpty_resiko varchar(100)," +
            "keterangan longtext," +
            "jenis_petugas_triase varchar(30)," +
            "nama_petugas_triase varchar(120)," +
            "nama_perawat varchar(120)," +
            "penanggung_jawab varchar(120)," +
            "ttd_resusitasi longblob," +
            "ttd_emergent longblob," +
            "ttd_urgent longblob," +
            "ttd_non_urgent longblob," +
            "ttd_perawat longblob," +
            "ttd_penanggung_jawab longblob," +
            "pemeriksaan_fisik longtext," +
            "pemeriksaan_penunjang longtext," +
            "diagnosis_masalah longtext," +
            "rencana longtext," +
            "instruksi longtext," +
            "nama_dokter varchar(120)," +
            "nama_dokter_instruksi varchar(120)," +
            "status_keluar varchar(40)," +
            "tanggal_keluar date," +
            "jam_keluar varchar(8)," +
            "keadaan_umum_keluar varchar(100)," +
            "td_keluar varchar(30)," +
            "rr_keluar varchar(30)," +
            "spo2_keluar varchar(30)," +
            "hr_keluar varchar(30)," +
            "temp_keluar varchar(30)," +
            "gcs_keluar varchar(30)," +
            "opname_ruangan varchar(120)," +
            "indikasi_masuk longtext," +
            "kontrol_ke varchar(120)," +
            "dirujuk_ke varchar(120)," +
            "alasan_dirujuk longtext," +
            "meninggal_jam varchar(8)," +
            "penyebab_meninggal longtext," +
            "nip varchar(20)," +
            "nama_petugas varchar(120)," +
            "created_at datetime," +
            "updated_at datetime," +
            "primary key (no_rawat)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8"
        );
        pastikanKolomBlob("ttd_resusitasi");
        pastikanKolomBlob("ttd_emergent");
        pastikanKolomBlob("ttd_urgent");
        pastikanKolomBlob("ttd_non_urgent");
        pastikanKolomBlob("ttd_perawat");
        pastikanKolomBlob("ttd_penanggung_jawab");
        pastikanKolomVarchar("jenis_petugas_triase", 30);
        pastikanKolomVarchar("nama_petugas_triase", 120);
    }

    private void pastikanKolomBlob(String namaKolom) {
        PreparedStatement cek = null;
        ResultSet hasil = null;
        PreparedStatement alter = null;
        try {
            cek = koneksi.prepareStatement(
                "select count(*) from information_schema.columns where table_schema=? and table_name='asesmen_pasien_ugd_v2' and column_name=?"
            );
            cek.setString(1, koneksiDB.DATABASE());
            cek.setString(2, namaKolom);
            hasil = cek.executeQuery();
            if (hasil.next() && hasil.getInt(1) == 0) {
                alter = koneksi.prepareStatement("alter table asesmen_pasien_ugd_v2 add column " + namaKolom + " longblob null");
                alter.executeUpdate();
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        } finally {
            try {
                if (hasil != null) {
                    hasil.close();
                }
            } catch (Exception e) {
                System.out.println("Notifikasi : " + e);
            }
            try {
                if (cek != null) {
                    cek.close();
                }
            } catch (Exception e) {
                System.out.println("Notifikasi : " + e);
            }
            try {
                if (alter != null) {
                    alter.close();
                }
            } catch (Exception e) {
                System.out.println("Notifikasi : " + e);
            }
        }
    }

    private void pastikanKolomVarchar(String namaKolom, int panjang) {
        PreparedStatement cek = null;
        ResultSet hasil = null;
        PreparedStatement alter = null;
        try {
            cek = koneksi.prepareStatement(
                "select count(*) from information_schema.columns where table_schema=? and table_name='asesmen_pasien_ugd_v2' and column_name=?"
            );
            cek.setString(1, koneksiDB.DATABASE());
            cek.setString(2, namaKolom);
            hasil = cek.executeQuery();
            if (hasil.next() && hasil.getInt(1) == 0) {
                alter = koneksi.prepareStatement("alter table asesmen_pasien_ugd_v2 add column " + namaKolom + " varchar(" + panjang + ") null");
                alter.executeUpdate();
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        } finally {
            try {
                if (hasil != null) {
                    hasil.close();
                }
            } catch (Exception e) {
                System.out.println("Notifikasi : " + e);
            }
            try {
                if (cek != null) {
                    cek.close();
                }
            } catch (Exception e) {
                System.out.println("Notifikasi : " + e);
            }
            try {
                if (alter != null) {
                    alter.close();
                }
            } catch (Exception e) {
                System.out.println("Notifikasi : " + e);
            }
        }
    }

    private void tampilData() {
        tabMode.setRowCount(0);
        String sql =
            "select a.no_rawat,p.no_rkm_medis,p.nm_pasien,a.tanggal_asesmen,a.jam_datang,a.respons,a.kategori_triase,a.status_keluar,a.nama_dokter,a.nama_petugas " +
            "from asesmen_pasien_ugd_v2 a " +
            "inner join reg_periksa r on r.no_rawat=a.no_rawat " +
            "inner join pasien p on p.no_rkm_medis=r.no_rkm_medis " +
            "where a.tanggal_asesmen between ? and ? and " +
            "(a.no_rawat like ? or p.no_rkm_medis like ? or p.nm_pasien like ?) " +
            "order by a.tanggal_asesmen desc,a.updated_at desc";
        try {
            ps = koneksi.prepareStatement(sql);
            ps.setDate(1, new java.sql.Date(DTPCari1.getDate().getTime()));
            ps.setDate(2, new java.sql.Date(DTPCari2.getDate().getTime()));
            String cari = "%" + TCari.getText().trim() + "%";
            ps.setString(3, cari);
            ps.setString(4, cari);
            ps.setString(5, cari);
            rs = ps.executeQuery();
            while (rs.next()) {
                tabMode.addRow(new Object[]{
                    rs.getString("no_rawat"),
                    rs.getString("no_rkm_medis"),
                    rs.getString("nm_pasien"),
                    rs.getString("tanggal_asesmen"),
                    rs.getString("jam_datang"),
                    rs.getString("respons"),
                    rs.getString("kategori_triase"),
                    rs.getString("status_keluar"),
                    rs.getString("nama_dokter"),
                    rs.getString("nama_petugas")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menampilkan data asesmen V2 : " + e.getMessage());
        } finally {
            tutupStatement();
        }
    }

    private void bukaDariTabel(String noRawat) {
        TNoRw.setText(noRawat);
        TCari.setText(noRawat);
        isRawat();
        loadAsesmen(noRawat);
        TabUtama.setSelectedIndex(0);
    }

    private boolean loadAsesmen(String noRawat) {
        if (noRawat.trim().isEmpty()) {
            return false;
        }
        try {
            ps = koneksi.prepareStatement("select * from asesmen_pasien_ugd_v2 where no_rawat=?");
            ps.setString(1, noRawat);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getDate("tanggal_asesmen") != null) {
                    DTPTanggalMasuk.setDate(rs.getDate("tanggal_asesmen"));
                }
                if (rs.getDate("tanggal_keluar") != null) {
                    DTPTanggalKeluar.setDate(rs.getDate("tanggal_keluar"));
                }
                TJamDatang.setText(nvl(rs.getString("jam_datang")));
                TJamPeriksa.setText(nvl(rs.getString("jam_periksa")));
                setChecks(rs.getString("jenis_kasus"), ChkNonTrauma, ChkKecelakaan, ChkIntoksikasi, ChkKekerasan, ChkDoa, ChkTrauma, ChkPenganiayaan, ChkGigitan, ChkSeksual);
                CmbRujukan.setSelectedItem(nvlCombo(rs.getString("rujukan"), CmbRujukan));
                CmbRespons.setSelectedItem(nvlCombo(rs.getString("respons"), CmbRespons));
                CmbStatusPernikahan.setSelectedItem(nvlCombo(rs.getString("status_pernikahan"), CmbStatusPernikahan));
                CmbAnak.setSelectedItem(nvlCombo(rs.getString("anak_status"), CmbAnak));
                TJumlahAnak.setText(nvl(rs.getString("jumlah_anak")));
                setChecks(rs.getString("kebiasaan"), ChkMerokok, ChkAlkohol);
                CmbWargaNegara.setSelectedItem(nvlCombo(rs.getString("warga_negara"), CmbWargaNegara));
                TWargaNegaraKet.setText(nvl(rs.getString("warga_negara_keterangan")));
                TPekerjaan.setText(nvl(rs.getString("pekerjaan")));
                CmbAgama.setSelectedItem(nvlCombo(rs.getString("agama"), CmbAgama));
                setChecks(rs.getString("tinggal_bersama"), ChkTinggalSuamiIstri, ChkTinggalAnak, ChkTinggalOrangtua, ChkTinggalSendiri, ChkTinggalLainnya);
                TTinggalKet.setText(nvl(rs.getString("tinggal_keterangan")));
                TNoTelp.setText(nvl(rs.getString("no_telp")));
                CmbMasalahBicara.setSelectedItem(nvlCombo(rs.getString("masalah_bicara"), CmbMasalahBicara));
                TMasalahBicaraKet.setText(nvl(rs.getString("masalah_bicara_keterangan")));
                AreaKeluhanRps.setText(nvl(rs.getString("keluhan_rps")));
                TTD.setText(nvl(rs.getString("tekanan_darah")));
                TRR.setText(nvl(rs.getString("respirasi")));
                TNadi.setText(nvl(rs.getString("frekuensi_nadi")));
                TSuhu.setText(nvl(rs.getString("suhu")));
                TAlergiMakanan.setText(nvl(rs.getString("alergi_makanan")));
                TAlergiObat.setText(nvl(rs.getString("alergi_obat")));
                TAlergiLain.setText(nvl(rs.getString("alergi_lain")));
                CmbKategoriTriase.setSelectedItem(nvlCombo(rs.getString("kategori_triase"), CmbKategoriTriase));
                CmbProfesiPetugasTriase.setSelectedItem(nvlCombo(rs.getString("jenis_petugas_triase"), CmbProfesiPetugasTriase));
                TNamaPetugasTriase.setText(nvl(rs.getString("nama_petugas_triase")));
                CmbJalanNafas.setSelectedItem(nvlCombo(rs.getString("jalan_nafas"), CmbJalanNafas));
                CmbPernafasan.setSelectedItem(nvlCombo(rs.getString("pernafasan"), CmbPernafasan));
                CmbSirkulasi.setSelectedItem(nvlCombo(rs.getString("sirkulasi"), CmbSirkulasi));
                CmbKesadaran.setSelectedItem(nvlCombo(rs.getString("kesadaran"), CmbKesadaran));
                TMorseSkor.setText(nvl(rs.getString("morse_skor")));
                CmbMorseResiko.setSelectedItem(nvlCombo(rs.getString("morse_resiko"), CmbMorseResiko));
                TNutrisiSkor.setText(nvl(rs.getString("nutrisi_skor")));
                CmbNutrisiResiko.setSelectedItem(nvlCombo(rs.getString("nutrisi_resiko"), CmbNutrisiResiko));
                CmbNyeriDewasa.setSelectedItem(nvlCombo(rs.getString("nyeri_dewasa"), CmbNyeriDewasa));
                CmbNyeriAnak.setSelectedItem(nvlCombo(rs.getString("nyeri_anak"), CmbNyeriAnak));
                TLokasiNyeri.setText(nvl(rs.getString("lokasi_nyeri")));
                TNyeriHilang.setText(nvl(rs.getString("nyeri_hilang_bila")));
                THumptySkor.setText(nvl(rs.getString("humpty_skor")));
                CmbHumptyResiko.setSelectedItem(nvlCombo(rs.getString("humpty_resiko"), CmbHumptyResiko));
                AreaKeterangan.setText(nvl(rs.getString("keterangan")));
                TNamaPerawat.setText(nvl(rs.getString("nama_perawat")));
                TPenanggungJawab.setText(nvl(rs.getString("penanggung_jawab")));
                setSignatureData(SIG_RESUSITASI, rs.getBytes("ttd_resusitasi"));
                setSignatureData(SIG_EMERGENT, rs.getBytes("ttd_emergent"));
                setSignatureData(SIG_URGENT, rs.getBytes("ttd_urgent"));
                setSignatureData(SIG_NON_URGENT, rs.getBytes("ttd_non_urgent"));
                setSignatureData(SIG_PERAWAT, rs.getBytes("ttd_perawat"));
                setSignatureData(SIG_PENANGGUNG_JAWAB, rs.getBytes("ttd_penanggung_jawab"));
                sinkronkanPanelTtdTriase();
                AreaPemeriksaanFisik.setText(nvl(rs.getString("pemeriksaan_fisik")));
                AreaPemeriksaanPenunjang.setText(nvl(rs.getString("pemeriksaan_penunjang")));
                AreaDiagnosisMasalah.setText(nvl(rs.getString("diagnosis_masalah")));
                AreaRencana.setText(nvl(rs.getString("rencana")));
                AreaInstruksi.setText(nvl(rs.getString("instruksi")));
                TNamaDokter.setText(nvl(rs.getString("nama_dokter")));
                TNamaDokterInstruksi.setText(nvl(rs.getString("nama_dokter_instruksi")));
                CmbStatusKeluar.setSelectedItem(nvlCombo(rs.getString("status_keluar"), CmbStatusKeluar));
                TJamKeluar.setText(nvl(rs.getString("jam_keluar")));
                TKeadaanUmumKeluar.setText(nvl(rs.getString("keadaan_umum_keluar")));
                TTDKeluar.setText(nvl(rs.getString("td_keluar")));
                TRRKeluar.setText(nvl(rs.getString("rr_keluar")));
                TSpO2Keluar.setText(nvl(rs.getString("spo2_keluar")));
                THRKeluar.setText(nvl(rs.getString("hr_keluar")));
                TTempKeluar.setText(nvl(rs.getString("temp_keluar")));
                TGCSKeluar.setText(nvl(rs.getString("gcs_keluar")));
                TOpnameRuangan.setText(nvl(rs.getString("opname_ruangan")));
                AreaIndikasiMasuk.setText(nvl(rs.getString("indikasi_masuk")));
                TKontrolKe.setText(nvl(rs.getString("kontrol_ke")));
                TDirujukKe.setText(nvl(rs.getString("dirujuk_ke")));
                AreaAlasanDirujuk.setText(nvl(rs.getString("alasan_dirujuk")));
                TMeninggalJam.setText(nvl(rs.getString("meninggal_jam")));
                AreaPenyebabMeninggal.setText(nvl(rs.getString("penyebab_meninggal")));
                TNipPetugas.setText(nvl(rs.getString("nip")));
                TNamaPetugas.setText(nvl(rs.getString("nama_petugas")));
                return true;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal membuka data asesmen V2 : " + e.getMessage());
        } finally {
            tutupStatement();
        }
        return false;
    }

    private void isRawat() {
        if (TNoRw.getText().trim().isEmpty()) {
            return;
        }
        try {
            ps = koneksi.prepareStatement(
                "select reg_periksa.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,pasien.stts_nikah,pasien.pekerjaan,pasien.agama " +
                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where reg_periksa.no_rawat=?"
            );
            ps.setString(1, TNoRw.getText().trim());
            rs = ps.executeQuery();
            if (rs.next()) {
                TNoRM.setText(rs.getString("no_rkm_medis"));
                TPasien.setText(rs.getString("nm_pasien"));
                TJk.setText(rs.getString("jk"));
                TTglLahir.setText(rs.getString("tgl_lahir"));
                if (TPekerjaan.getText().trim().isEmpty()) {
                    TPekerjaan.setText(nvl(rs.getString("pekerjaan")));
                }
                if (pilihanAda(CmbStatusPernikahan, rs.getString("stts_nikah"))) {
                    CmbStatusPernikahan.setSelectedItem(rs.getString("stts_nikah"));
                }
                if (pilihanAda(CmbAgama, rs.getString("agama"))) {
                    CmbAgama.setSelectedItem(rs.getString("agama"));
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil data pasien : " + e.getMessage());
        } finally {
            tutupStatement();
        }
    }

    private void simpanAtauPerbarui(boolean modeEdit) {
        if (!validInput()) {
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            if (dataSudahAda()) {
                updateData();
                if (modeEdit) {
                    JOptionPane.showMessageDialog(this, "Asesmen Pasien UGD V2 berhasil diubah.");
                }
            } else {
                insertData();
                JOptionPane.showMessageDialog(this, "Asesmen Pasien UGD V2 berhasil disimpan.");
            }
            tampilData();
            loadAsesmen(TNoRw.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan asesmen V2 : " + e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private boolean validInput() {
        if (TNoRw.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No. Rawat masih kosong.");
            return false;
        }
        if (TPasien.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Data pasien belum terisi.");
            return false;
        }
        if (AreaKeluhanRps.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keluhan & RPS masih kosong.");
            AreaKeluhanRps.requestFocus();
            return false;
        }
        if (TNipPetugas.getText().trim().isEmpty() && TNamaPetugas.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Petugas penginput belum terisi.");
            return false;
        }
        return true;
    }

    private boolean dataSudahAda() throws SQLException {
        PreparedStatement cek = null;
        ResultSet hasil = null;
        try {
            cek = koneksi.prepareStatement("select no_rawat from asesmen_pasien_ugd_v2 where no_rawat=?");
            cek.setString(1, TNoRw.getText().trim());
            hasil = cek.executeQuery();
            return hasil.next();
        } finally {
            if (hasil != null) {
                hasil.close();
            }
            if (cek != null) {
                cek.close();
            }
        }
    }

    private void insertData() throws SQLException {
        String placeholders = String.join(",", Collections.nCopies(80, "?"));
        String sql =
            "insert into asesmen_pasien_ugd_v2 (" +
            "no_rawat,tanggal_asesmen,jam_datang,jam_periksa,jenis_kasus,rujukan,respons,status_pernikahan,anak_status,jumlah_anak,kebiasaan,warga_negara,warga_negara_keterangan,pekerjaan,agama," +
            "tinggal_bersama,tinggal_keterangan,no_telp,masalah_bicara,masalah_bicara_keterangan,keluhan_rps,tekanan_darah,respirasi,frekuensi_nadi,suhu,alergi_makanan,alergi_obat,alergi_lain," +
            "kategori_triase,jalan_nafas,pernafasan,sirkulasi,kesadaran,morse_skor,morse_resiko,nutrisi_skor,nutrisi_resiko,nyeri_dewasa,nyeri_anak,lokasi_nyeri,nyeri_hilang_bila,humpty_skor,humpty_resiko,keterangan," +
            "jenis_petugas_triase,nama_petugas_triase,nama_perawat,penanggung_jawab,ttd_resusitasi,ttd_emergent,ttd_urgent,ttd_non_urgent,ttd_perawat,ttd_penanggung_jawab,pemeriksaan_fisik,pemeriksaan_penunjang,diagnosis_masalah,rencana,instruksi,nama_dokter,nama_dokter_instruksi,status_keluar,tanggal_keluar,jam_keluar,keadaan_umum_keluar,td_keluar,rr_keluar,spo2_keluar,hr_keluar,temp_keluar,gcs_keluar,opname_ruangan,indikasi_masuk,kontrol_ke,dirujuk_ke,alasan_dirujuk,meninggal_jam,penyebab_meninggal,nip,nama_petugas,created_at,updated_at" +
            ") values (" +
            placeholders + ",now(),now()" +
            ")";
        ps = koneksi.prepareStatement(sql);
        int idx = 1;
        ps.setString(idx++, TNoRw.getText().trim());
        idx = isiParameter(ps, idx);
        ps.executeUpdate();
    }

    private void updateData() throws SQLException {
        String sql =
            "update asesmen_pasien_ugd_v2 set " +
            "tanggal_asesmen=?,jam_datang=?,jam_periksa=?,jenis_kasus=?,rujukan=?,respons=?,status_pernikahan=?,anak_status=?,jumlah_anak=?,kebiasaan=?,warga_negara=?,warga_negara_keterangan=?,pekerjaan=?,agama=?," +
            "tinggal_bersama=?,tinggal_keterangan=?,no_telp=?,masalah_bicara=?,masalah_bicara_keterangan=?,keluhan_rps=?,tekanan_darah=?,respirasi=?,frekuensi_nadi=?,suhu=?,alergi_makanan=?,alergi_obat=?,alergi_lain=?," +
            "kategori_triase=?,jalan_nafas=?,pernafasan=?,sirkulasi=?,kesadaran=?,morse_skor=?,morse_resiko=?,nutrisi_skor=?,nutrisi_resiko=?,nyeri_dewasa=?,nyeri_anak=?,lokasi_nyeri=?,nyeri_hilang_bila=?,humpty_skor=?,humpty_resiko=?,keterangan=?," +
            "jenis_petugas_triase=?,nama_petugas_triase=?,nama_perawat=?,penanggung_jawab=?,ttd_resusitasi=?,ttd_emergent=?,ttd_urgent=?,ttd_non_urgent=?,ttd_perawat=?,ttd_penanggung_jawab=?,pemeriksaan_fisik=?,pemeriksaan_penunjang=?,diagnosis_masalah=?,rencana=?,instruksi=?,nama_dokter=?,nama_dokter_instruksi=?,status_keluar=?,tanggal_keluar=?,jam_keluar=?,keadaan_umum_keluar=?,td_keluar=?,rr_keluar=?,spo2_keluar=?,hr_keluar=?,temp_keluar=?,gcs_keluar=?,opname_ruangan=?,indikasi_masuk=?,kontrol_ke=?,dirujuk_ke=?,alasan_dirujuk=?,meninggal_jam=?,penyebab_meninggal=?,nip=?,nama_petugas=?,updated_at=now() " +
            "where no_rawat=?";
        ps = koneksi.prepareStatement(sql);
        int idx = isiParameter(ps, 1);
        ps.setString(idx++, TNoRw.getText().trim());
        ps.executeUpdate();
    }

    private int isiParameter(PreparedStatement statement, int idx) throws SQLException {
        setDate(statement, idx++, DTPTanggalMasuk.getDate());
        statement.setString(idx++, ambil(TJamDatang));
        statement.setString(idx++, ambil(TJamPeriksa));
        statement.setString(idx++, gabungCheck(ChkNonTrauma, ChkKecelakaan, ChkIntoksikasi, ChkKekerasan, ChkDoa, ChkTrauma, ChkPenganiayaan, ChkGigitan, ChkSeksual));
        statement.setString(idx++, ambilCombo(CmbRujukan));
        statement.setString(idx++, ambilCombo(CmbRespons));
        statement.setString(idx++, ambilCombo(CmbStatusPernikahan));
        statement.setString(idx++, ambilCombo(CmbAnak));
        statement.setString(idx++, ambil(TJumlahAnak));
        statement.setString(idx++, gabungCheck(ChkMerokok, ChkAlkohol));
        statement.setString(idx++, ambilCombo(CmbWargaNegara));
        statement.setString(idx++, ambil(TWargaNegaraKet));
        statement.setString(idx++, ambil(TPekerjaan));
        statement.setString(idx++, ambilCombo(CmbAgama));
        statement.setString(idx++, gabungCheck(ChkTinggalSuamiIstri, ChkTinggalAnak, ChkTinggalOrangtua, ChkTinggalSendiri, ChkTinggalLainnya));
        statement.setString(idx++, ambil(TTinggalKet));
        statement.setString(idx++, ambil(TNoTelp));
        statement.setString(idx++, ambilCombo(CmbMasalahBicara));
        statement.setString(idx++, ambil(TMasalahBicaraKet));
        statement.setString(idx++, ambil(AreaKeluhanRps));
        statement.setString(idx++, ambil(TTD));
        statement.setString(idx++, ambil(TRR));
        statement.setString(idx++, ambil(TNadi));
        statement.setString(idx++, ambil(TSuhu));
        statement.setString(idx++, ambil(TAlergiMakanan));
        statement.setString(idx++, ambil(TAlergiObat));
        statement.setString(idx++, ambil(TAlergiLain));
        statement.setString(idx++, ambilCombo(CmbKategoriTriase));
        statement.setString(idx++, ambilCombo(CmbJalanNafas));
        statement.setString(idx++, ambilCombo(CmbPernafasan));
        statement.setString(idx++, ambilCombo(CmbSirkulasi));
        statement.setString(idx++, ambilCombo(CmbKesadaran));
        statement.setString(idx++, ambil(TMorseSkor));
        statement.setString(idx++, ambilCombo(CmbMorseResiko));
        statement.setString(idx++, ambil(TNutrisiSkor));
        statement.setString(idx++, ambilCombo(CmbNutrisiResiko));
        statement.setString(idx++, ambilCombo(CmbNyeriDewasa));
        statement.setString(idx++, ambilCombo(CmbNyeriAnak));
        statement.setString(idx++, ambil(TLokasiNyeri));
        statement.setString(idx++, ambil(TNyeriHilang));
        statement.setString(idx++, ambil(THumptySkor));
        statement.setString(idx++, ambilCombo(CmbHumptyResiko));
        statement.setString(idx++, ambil(AreaKeterangan));
        statement.setString(idx++, ambilCombo(CmbProfesiPetugasTriase));
        statement.setString(idx++, ambil(TNamaPetugasTriase));
        statement.setString(idx++, ambil(TNamaPerawat));
        statement.setString(idx++, ambil(TPenanggungJawab));
        setBlob(statement, idx++, getSignatureDataUntukSimpan(SIG_RESUSITASI));
        setBlob(statement, idx++, getSignatureDataUntukSimpan(SIG_EMERGENT));
        setBlob(statement, idx++, getSignatureDataUntukSimpan(SIG_URGENT));
        setBlob(statement, idx++, getSignatureDataUntukSimpan(SIG_NON_URGENT));
        setBlob(statement, idx++, ttdPerawat);
        setBlob(statement, idx++, ttdPenanggungJawab);
        statement.setString(idx++, ambil(AreaPemeriksaanFisik));
        statement.setString(idx++, ambil(AreaPemeriksaanPenunjang));
        statement.setString(idx++, ambil(AreaDiagnosisMasalah));
        statement.setString(idx++, ambil(AreaRencana));
        statement.setString(idx++, ambil(AreaInstruksi));
        statement.setString(idx++, ambil(TNamaDokter));
        statement.setString(idx++, ambil(TNamaDokterInstruksi));
        statement.setString(idx++, ambilCombo(CmbStatusKeluar));
        setDate(statement, idx++, DTPTanggalKeluar.getDate());
        statement.setString(idx++, ambil(TJamKeluar));
        statement.setString(idx++, ambil(TKeadaanUmumKeluar));
        statement.setString(idx++, ambil(TTDKeluar));
        statement.setString(idx++, ambil(TRRKeluar));
        statement.setString(idx++, ambil(TSpO2Keluar));
        statement.setString(idx++, ambil(THRKeluar));
        statement.setString(idx++, ambil(TTempKeluar));
        statement.setString(idx++, ambil(TGCSKeluar));
        statement.setString(idx++, ambil(TOpnameRuangan));
        statement.setString(idx++, ambil(AreaIndikasiMasuk));
        statement.setString(idx++, ambil(TKontrolKe));
        statement.setString(idx++, ambil(TDirujukKe));
        statement.setString(idx++, ambil(AreaAlasanDirujuk));
        statement.setString(idx++, ambil(TMeninggalJam));
        statement.setString(idx++, ambil(AreaPenyebabMeninggal));
        statement.setString(idx++, ambil(TNipPetugas));
        statement.setString(idx++, ambil(TNamaPetugas));
        return idx;
    }

    private void hapusData() {
        if (TNoRw.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus dulu.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus Asesmen Pasien UGD V2 untuk no. rawat " + TNoRw.getText() + " ?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                ps = koneksi.prepareStatement("delete from asesmen_pasien_ugd_v2 where no_rawat=?");
                ps.setString(1, TNoRw.getText().trim());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Data asesmen V2 berhasil dihapus.");
                tampilData();
                emptTeks();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus data asesmen V2 : " + e.getMessage());
            } finally {
                tutupStatement();
            }
        }
    }

    public void emptTeks() {
        Date sekarang = new Date();
        DTPTanggalMasuk.setDate(sekarang);
        TJamDatang.setText(formatJam.format(sekarang));
        TJamPeriksa.setText(formatJam.format(sekarang));
        CmbRujukan.setSelectedIndex(0);
        CmbRespons.setSelectedIndex(0);
        resetCheck(ChkNonTrauma, ChkKecelakaan, ChkIntoksikasi, ChkKekerasan, ChkDoa, ChkTrauma, ChkPenganiayaan, ChkGigitan, ChkSeksual);
        CmbStatusPernikahan.setSelectedIndex(0);
        CmbAnak.setSelectedIndex(0);
        TJumlahAnak.setText("");
        resetCheck(ChkMerokok, ChkAlkohol, ChkTinggalSuamiIstri, ChkTinggalAnak, ChkTinggalOrangtua, ChkTinggalSendiri, ChkTinggalLainnya);
        CmbWargaNegara.setSelectedIndex(0);
        TWargaNegaraKet.setText("");
        TTinggalKet.setText("");
        TNoTelp.setText("");
        CmbMasalahBicara.setSelectedIndex(0);
        TMasalahBicaraKet.setText("");
        AreaKeluhanRps.setText("");
        TTD.setText("");
        TRR.setText("");
        TNadi.setText("");
        TSuhu.setText("");
        TAlergiMakanan.setText("");
        TAlergiObat.setText("");
        TAlergiLain.setText("");
        CmbKategoriTriase.setSelectedIndex(0);
        CmbProfesiPetugasTriase.setSelectedIndex(0);
        TNamaPetugasTriase.setText("");
        CmbJalanNafas.setSelectedIndex(0);
        CmbPernafasan.setSelectedIndex(0);
        CmbSirkulasi.setSelectedIndex(0);
        CmbKesadaran.setSelectedIndex(0);
        TMorseSkor.setText("");
        CmbMorseResiko.setSelectedIndex(0);
        TNutrisiSkor.setText("");
        CmbNutrisiResiko.setSelectedIndex(0);
        CmbNyeriDewasa.setSelectedIndex(0);
        CmbNyeriAnak.setSelectedIndex(0);
        TLokasiNyeri.setText("");
        TNyeriHilang.setText("");
        THumptySkor.setText("");
        CmbHumptyResiko.setSelectedIndex(0);
        AreaKeterangan.setText("");
        TNamaPerawat.setText("");
        TPenanggungJawab.setText("");
        resetSemuaTtd();
        AreaPemeriksaanFisik.setText("");
        AreaPemeriksaanPenunjang.setText("");
        AreaDiagnosisMasalah.setText("");
        AreaRencana.setText("");
        AreaInstruksi.setText("");
        TNamaDokter.setText("");
        TNamaDokterInstruksi.setText("");
        CmbStatusKeluar.setSelectedIndex(0);
        DTPTanggalKeluar.setDate(sekarang);
        TJamKeluar.setText(formatJam.format(sekarang));
        TKeadaanUmumKeluar.setText("");
        TTDKeluar.setText("");
        TRRKeluar.setText("");
        TSpO2Keluar.setText("");
        THRKeluar.setText("");
        TTempKeluar.setText("");
        TGCSKeluar.setText("");
        TOpnameRuangan.setText("");
        AreaIndikasiMasuk.setText("");
        TKontrolKe.setText("");
        TDirujukKe.setText("");
        AreaAlasanDirujuk.setText("");
        TMeninggalJam.setText(formatJam.format(sekarang));
        AreaPenyebabMeninggal.setText("");
        if (!TNoRw.getText().trim().isEmpty()) {
            isRawat();
            if (!loadAsesmen(TNoRw.getText().trim())) {
                isiPetugasLogin();
            }
        } else {
            TNoRM.setText("");
            TPasien.setText("");
            TJk.setText("");
            TTglLahir.setText("");
            isiPetugasLogin();
        }
        TabUtama.setSelectedIndex(0);
    }

    public void setNoRm(String norwt, Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        if (tgl2 != null) {
            DTPCari1.setDate(tgl2);
            DTPCari2.setDate(tgl2);
            DTPTanggalMasuk.setDate(tgl2);
        }
        isRawat();
        isiPetugasLogin();
        loadAsesmen(norwt);
        tampilData();
    }

    public void isCek() {
        boolean bolehAkses = akses.getpenilaian_awal_keperawatan_igd() || akses.getpenilaian_awal_medis_igd();
        BtnSimpan.setEnabled(bolehAkses);
        BtnEdit.setEnabled(bolehAkses);
        BtnHapus.setEnabled(bolehAkses);
        BtnPrint.setEnabled(bolehAkses);
        MnPreviewJasper.setEnabled(bolehAkses);
        MnCetakPdf.setEnabled(bolehAkses);
        MnBukaForm.setEnabled(bolehAkses);
        isiPetugasLogin();
    }

    private void tampilkanPopupData(MouseEvent e) {
        if (e.isPopupTrigger()) {
            int row = tbData.rowAtPoint(e.getPoint());
            if (row >= 0) {
                tbData.setRowSelectionInterval(row, row);
            }
            PopupData.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private void bukaBarisTerpilih() {
        if (tbData.getSelectedRow() != -1) {
            bukaDariTabel(tbData.getValueAt(tbData.getSelectedRow(), 0).toString());
        }
    }

    private boolean siapCetak() {
        if (TNoRw.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih atau buka data asesmen yang akan dicetak dulu.");
            return false;
        }
        return true;
    }

    private void previewJasper() {
        if (!siapCetak()) {
            return;
        }
        try {
            siapkanReport("rptAsesmenPasienUGDV2");
            Valid.MyReportqry(
                "rptAsesmenPasienUGDV2.jasper",
                "report",
                "::[ Laporan Asesmen Pasien UGD V2 ]::",
                buatQueryCetak(),
                buatParameterCetak()
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal preview Jasper asesmen V2 : " + e.getMessage());
        }
    }

    private void cetakPdf() {
        if (!siapCetak()) {
            return;
        }
        try {
            siapkanReport("rptAsesmenPasienUGDV2");
            Valid.MyReportqrypdf(
                "rptAsesmenPasienUGDV2.jasper",
                "report",
                "::[ Laporan Asesmen Pasien UGD V2 ]::",
                buatQueryCetak(),
                buatParameterCetak()
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal cetak PDF asesmen V2 : " + e.getMessage());
        }
    }

    private Map<String, Object> buatParameterCetak() {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
        param.put("ttd_resusitasi", buatImageReport(getSignatureDataUntukSimpan(SIG_RESUSITASI)));
        param.put("ttd_emergent", buatImageReport(getSignatureDataUntukSimpan(SIG_EMERGENT)));
        param.put("ttd_urgent", buatImageReport(getSignatureDataUntukSimpan(SIG_URGENT)));
        param.put("ttd_non_urgent", buatImageReport(getSignatureDataUntukSimpan(SIG_NON_URGENT)));
        param.put("petugas_triase_resusitasi", getLabelPetugasTriaseUntukKategori("RESUSITASI"));
        param.put("petugas_triase_emergent", getLabelPetugasTriaseUntukKategori("EMERGENT"));
        param.put("petugas_triase_urgent", getLabelPetugasTriaseUntukKategori("URGENT"));
        param.put("petugas_triase_non_urgent", getLabelPetugasTriaseUntukKategori("NON URGENT"));
        param.put("ttd_perawat", buatImageReport(ttdPerawat));
        param.put("ttd_penanggung_jawab", buatImageReport(ttdPenanggungJawab));
        return param;
    }

    private String buatQueryCetak() {
        return "select " +
            "a.no_rawat," +
            "p.no_rkm_medis," +
            "p.nm_pasien," +
            "if(p.jk='L','Laki-Laki','Perempuan') as jk," +
            "p.tgl_lahir," +
            "concat(timestampdiff(year,p.tgl_lahir,curdate()),' th') as umur," +
            "a.tanggal_asesmen,a.jam_datang,a.jam_periksa," +
            "ifnull(a.jenis_kasus,'') as jenis_kasus,ifnull(a.rujukan,'') as rujukan,ifnull(a.respons,'') as respons," +
            "ifnull(a.status_pernikahan,'') as status_pernikahan,ifnull(a.anak_status,'') as anak_status,ifnull(a.jumlah_anak,'') as jumlah_anak," +
            "ifnull(a.kebiasaan,'') as kebiasaan,ifnull(a.warga_negara,'') as warga_negara,ifnull(a.warga_negara_keterangan,'') as warga_negara_keterangan," +
            "ifnull(a.pekerjaan,'') as pekerjaan,ifnull(a.agama,'') as agama,ifnull(a.tinggal_bersama,'') as tinggal_bersama,ifnull(a.tinggal_keterangan,'') as tinggal_keterangan," +
            "ifnull(a.no_telp,'') as no_telp,ifnull(a.masalah_bicara,'') as masalah_bicara,ifnull(a.masalah_bicara_keterangan,'') as masalah_bicara_keterangan," +
            "ifnull(a.keluhan_rps,'') as keluhan_rps,ifnull(a.tekanan_darah,'') as tekanan_darah,ifnull(a.respirasi,'') as respirasi," +
            "ifnull(a.frekuensi_nadi,'') as frekuensi_nadi,ifnull(a.suhu,'') as suhu,ifnull(a.alergi_makanan,'') as alergi_makanan," +
            "ifnull(a.alergi_obat,'') as alergi_obat,ifnull(a.alergi_lain,'') as alergi_lain,ifnull(a.kategori_triase,'') as kategori_triase," +
            "ifnull(a.jalan_nafas,'') as jalan_nafas,ifnull(a.pernafasan,'') as pernafasan,ifnull(a.sirkulasi,'') as sirkulasi,ifnull(a.kesadaran,'') as kesadaran," +
            "ifnull(a.morse_skor,'') as morse_skor,ifnull(a.morse_resiko,'') as morse_resiko,ifnull(a.nutrisi_skor,'') as nutrisi_skor,ifnull(a.nutrisi_resiko,'') as nutrisi_resiko," +
            "ifnull(a.nyeri_dewasa,'') as nyeri_dewasa,ifnull(a.nyeri_anak,'') as nyeri_anak,ifnull(a.lokasi_nyeri,'') as lokasi_nyeri,ifnull(a.nyeri_hilang_bila,'') as nyeri_hilang_bila," +
            "ifnull(a.humpty_skor,'') as humpty_skor,ifnull(a.humpty_resiko,'') as humpty_resiko,ifnull(a.keterangan,'') as keterangan," +
            "ifnull(a.nama_perawat,'') as nama_perawat,ifnull(a.penanggung_jawab,'') as penanggung_jawab," +
            "ifnull(a.pemeriksaan_fisik,'') as pemeriksaan_fisik,ifnull(a.pemeriksaan_penunjang,'') as pemeriksaan_penunjang," +
            "ifnull(a.diagnosis_masalah,'') as diagnosis_masalah,ifnull(a.rencana,'') as rencana,ifnull(a.instruksi,'') as instruksi," +
            "ifnull(a.nama_dokter,'') as nama_dokter,ifnull(a.nama_dokter_instruksi,'') as nama_dokter_instruksi," +
            "ifnull(a.status_keluar,'') as status_keluar,a.tanggal_keluar,ifnull(a.jam_keluar,'') as jam_keluar," +
            "ifnull(a.keadaan_umum_keluar,'') as keadaan_umum_keluar,ifnull(a.td_keluar,'') as td_keluar,ifnull(a.rr_keluar,'') as rr_keluar," +
            "ifnull(a.spo2_keluar,'') as spo2_keluar,ifnull(a.hr_keluar,'') as hr_keluar,ifnull(a.temp_keluar,'') as temp_keluar,ifnull(a.gcs_keluar,'') as gcs_keluar," +
            "ifnull(a.opname_ruangan,'') as opname_ruangan,ifnull(a.indikasi_masuk,'') as indikasi_masuk,ifnull(a.kontrol_ke,'') as kontrol_ke," +
            "ifnull(a.dirujuk_ke,'') as dirujuk_ke,ifnull(a.alasan_dirujuk,'') as alasan_dirujuk,ifnull(a.meninggal_jam,'') as meninggal_jam," +
            "ifnull(a.penyebab_meninggal,'') as penyebab_meninggal,ifnull(a.nama_petugas,'') as nama_petugas,ifnull(a.nip,'') as nip_petugas " +
            "from asesmen_pasien_ugd_v2 a " +
            "inner join reg_periksa r on r.no_rawat=a.no_rawat " +
            "inner join pasien p on p.no_rkm_medis=r.no_rkm_medis " +
            "where a.no_rawat='" + TNoRw.getText().trim() + "'";
    }

    private void siapkanReport(String baseName) throws Exception {
        File jrxml = new File("./report/" + baseName + ".jrxml");
        File jasper = new File("./report/" + baseName + ".jasper");
        if (!jrxml.exists()) {
            throw new Exception("File report " + baseName + " tidak ditemukan.");
        }
        if (!jasper.exists() || jrxml.lastModified() > jasper.lastModified()) {
            JasperCompileManager.compileReportToFile(jrxml.getPath(), jasper.getPath());
        }
    }

    public void setTampil() {
        TabUtama.setSelectedIndex(1);
    }

    private void isiPetugasLogin() {
        if (akses.getkode() != null && !akses.getkode().trim().isEmpty()) {
            TNipPetugas.setText(akses.getkode());
            String namaPetugas = Sequel.cariIsi("select nama from petugas where nip=?", akses.getkode());
            if (namaPetugas != null && !namaPetugas.trim().isEmpty()) {
                TNamaPetugas.setText(namaPetugas);
            }
            isiDefaultPetugasTriase(namaPetugas);
        }
    }

    private void isiDefaultPetugasTriase(String namaPetugasLogin) {
        if (!TNamaPetugasTriase.getText().trim().isEmpty()) {
            return;
        }
        String namaDokter = Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", akses.getkode());
        if (namaDokter != null && !namaDokter.trim().isEmpty()) {
            CmbProfesiPetugasTriase.setSelectedItem("Dokter");
            TNamaPetugasTriase.setText(namaDokter);
            return;
        }
        CmbProfesiPetugasTriase.setSelectedItem("Perawat");
        if (namaPetugasLogin != null && !namaPetugasLogin.trim().isEmpty()) {
            TNamaPetugasTriase.setText(namaPetugasLogin);
        } else if (!TNamaPetugas.getText().trim().isEmpty()) {
            TNamaPetugasTriase.setText(TNamaPetugas.getText().trim());
        }
    }

    private void setDate(PreparedStatement statement, int index, Date value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DATE);
        } else {
            statement.setDate(index, new java.sql.Date(value.getTime()));
        }
    }

    private void tutupStatement() {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
        rs = null;
        ps = null;
    }

    private GridBagConstraints dasarGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        return gbc;
    }

    private int tambahJudul(JPanel panel, int row, String judul) {
        JLabel label = new JLabel(judul);
        label.setOpaque(true);
        label.setBackground(WARNA_ACCENT_SOFT);
        label.setForeground(WARNA_ACCENT);
        label.setFont(FONT_LABEL.deriveFont(13f));
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, WARNA_ACCENT),
            new EmptyBorder(8, 10, 8, 8)
        ));
        GridBagConstraints gbc = dasarGbc();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        panel.add(label, gbc);
        return row + 1;
    }

    private int tambahDuaKolom(JPanel panel, int row, String label1, Component comp1, String label2, Component comp2) {
        GridBagConstraints gbc = dasarGbc();
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        panel.add(labelKiri(label1), gbc);

        gbc = dasarGbc();
        gbc.gridy = row;
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        panel.add(bungkusKomponen(comp1), gbc);

        gbc = dasarGbc();
        gbc.gridy = row;
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        panel.add(labelKiri(label2), gbc);

        gbc = dasarGbc();
        gbc.gridy = row;
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        panel.add(bungkusKomponen(comp2), gbc);
        return row + 1;
    }

    private int tambahSatuKolom(JPanel panel, int row, String label, Component comp) {
        GridBagConstraints gbc = dasarGbc();
        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(labelKiri(label), gbc);

        gbc = dasarGbc();
        gbc.gridy = row;
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        panel.add(bungkusKomponen(comp), gbc);
        return row + 1;
    }

    private int tambahArea(JPanel panel, int row, String label, JTextArea area, int tinggi) {
        return tambahSatuKolom(panel, row, label, scrollArea(area, tinggi));
    }

    private JLabel labelKiri(String text) {
        JLabel label = new JLabel(text);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setForeground(WARNA_MUTED);
        label.setFont(FONT_LABEL);
        return label;
    }

    private Component bungkusKomponen(Component comp) {
        if (comp instanceof JPanel || comp instanceof JScrollPane) {
            return comp;
        }
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(comp, BorderLayout.CENTER);
        return panel;
    }

    private JPanel flowPanel(Component... components) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false);
        for (Component component : components) {
            panel.add(component);
        }
        return panel;
    }

    private JScrollPane scrollArea(JTextArea area, int tinggi) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(100, tinggi));
        aturScrollPane(scroll, false);
        return scroll;
    }

    private JLabel buatLabelSignature() {
        JLabel label = new JLabel("Belum ada tanda tangan", SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(160, 60));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setForeground(WARNA_MUTED);
        label.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        label.setBorder(BorderFactory.createLineBorder(WARNA_BORDER));
        return label;
    }

    private TextBox buatTextBox(int columns) {
        TextBox text = new TextBox();
        text.setColumns(columns);
        text.setFont(FONT_BODY);
        text.setForeground(WARNA_TEXT);
        text.setBackground(WARNA_FIELD);
        text.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARNA_BORDER),
            new EmptyBorder(4, 8, 4, 8)
        ));
        text.setCaretColor(WARNA_ACCENT);
        return text;
    }

    private TextArea buatTextArea(int rows) {
        TextArea area = new TextArea();
        area.setRows(rows);
        area.setFont(FONT_BODY);
        area.setForeground(WARNA_TEXT);
        area.setBackground(WARNA_FIELD);
        area.setBorder(new EmptyBorder(6, 8, 6, 8));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setCaretColor(WARNA_ACCENT);
        return area;
    }

    private JComboBox<String> buatCombo(String... values) {
        JComboBox<String> combo = new JComboBox<String>(new DefaultComboBoxModel<String>(values));
        combo.setFont(FONT_BODY);
        combo.setBackground(WARNA_FIELD);
        combo.setForeground(WARNA_TEXT);
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARNA_BORDER),
            new EmptyBorder(2, 6, 2, 6)
        ));
        return combo;
    }

    private javax.swing.JCheckBox buatCheckBox(String label) {
        javax.swing.JCheckBox check = new javax.swing.JCheckBox(label);
        check.setOpaque(false);
        check.setFont(FONT_BODY);
        check.setForeground(WARNA_TEXT);
        return check;
    }

    private Button buatButton(String label) {
        Button button = new Button() {
            @Override
            protected void paintComponent(Graphics g) {
                Color warnaDasar = getBackground();
                if (!isEnabled()) {
                    warnaDasar = new Color(148, 163, 184);
                } else if (getModel().isPressed()) {
                    warnaDasar = warnaDasar.darker();
                }
                g.setColor(warnaDasar);
                g.fillRect(0, 0, getWidth(), getHeight());

                FontMetrics metrics = g.getFontMetrics(getFont());
                int textX = (getWidth() - metrics.stringWidth(getText())) / 2;
                int textY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
                g.setColor(isEnabled() ? getForeground() : new Color(226, 232, 240));
                g.setFont(getFont());
                g.drawString(getText(), textX, textY);
            }
        };
        button.setText(label);
        button.setFont(FONT_LABEL);
        button.setForeground(WARNA_TEXT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setRoundRect(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setMargin(new Insets(8, 14, 8, 14));
        return button;
    }

    private Button buatButtonMini(String label) {
        Button button = buatButton(label);
        button.setMargin(new Insets(4, 10, 4, 10));
        button.setPreferredSize(new Dimension(78, 28));
        return button;
    }

    private void bukaEditorTtd(String judul, int tipe) {
        SignaturePadDialog dialog = new SignaturePadDialog(this, judul, getSignatureData(tipe));
        byte[] hasil = dialog.showDialog();
        if (hasil != null) {
            setSignatureData(tipe, hasil);
        }
    }

    private void sinkronkanPanelTtdTriase() {
        String kategori = ambilCombo(CmbKategoriTriase);
        setPanelTtdVisible(PnlTtdResusitasi, "RESUSITASI".equalsIgnoreCase(kategori));
        setPanelTtdVisible(PnlTtdEmergent, "EMERGENT".equalsIgnoreCase(kategori));
        setPanelTtdVisible(PnlTtdUrgent, "URGENT".equalsIgnoreCase(kategori));
        setPanelTtdVisible(PnlTtdNonUrgent, "NON URGENT".equalsIgnoreCase(kategori));
        if (PnlTtdTriase != null) {
            PnlTtdTriase.revalidate();
            PnlTtdTriase.repaint();
        }
    }

    private void setPanelTtdVisible(JPanel panel, boolean visible) {
        if (panel != null) {
            panel.setVisible(visible);
        }
    }

    private byte[] getSignatureData(int tipe) {
        switch (tipe) {
            case SIG_RESUSITASI:
                return ttdResusitasi;
            case SIG_EMERGENT:
                return ttdEmergent;
            case SIG_URGENT:
                return ttdUrgent;
            case SIG_NON_URGENT:
                return ttdNonUrgent;
            case SIG_PERAWAT:
                return ttdPerawat;
            case SIG_PENANGGUNG_JAWAB:
                return ttdPenanggungJawab;
            default:
                return null;
        }
    }

    private JLabel getSignatureLabel(int tipe) {
        switch (tipe) {
            case SIG_RESUSITASI:
                return LblTtdResusitasi;
            case SIG_EMERGENT:
                return LblTtdEmergent;
            case SIG_URGENT:
                return LblTtdUrgent;
            case SIG_NON_URGENT:
                return LblTtdNonUrgent;
            case SIG_PERAWAT:
                return LblTtdPerawat;
            case SIG_PENANGGUNG_JAWAB:
                return LblTtdPenanggungJawab;
            default:
                return LblTtdResusitasi;
        }
    }

    private void setSignatureData(int tipe, byte[] data) {
        switch (tipe) {
            case SIG_RESUSITASI:
                ttdResusitasi = data;
                updateSignatureLabel(LblTtdResusitasi, data);
                break;
            case SIG_EMERGENT:
                ttdEmergent = data;
                updateSignatureLabel(LblTtdEmergent, data);
                break;
            case SIG_URGENT:
                ttdUrgent = data;
                updateSignatureLabel(LblTtdUrgent, data);
                break;
            case SIG_NON_URGENT:
                ttdNonUrgent = data;
                updateSignatureLabel(LblTtdNonUrgent, data);
                break;
            case SIG_PERAWAT:
                ttdPerawat = data;
                updateSignatureLabel(LblTtdPerawat, data);
                break;
            case SIG_PENANGGUNG_JAWAB:
                ttdPenanggungJawab = data;
                updateSignatureLabel(LblTtdPenanggungJawab, data);
                break;
            default:
                break;
        }
    }

    private byte[] getSignatureDataUntukSimpan(int tipe) {
        switch (tipe) {
            case SIG_RESUSITASI:
                return "RESUSITASI".equalsIgnoreCase(ambilCombo(CmbKategoriTriase)) ? ttdResusitasi : null;
            case SIG_EMERGENT:
                return "EMERGENT".equalsIgnoreCase(ambilCombo(CmbKategoriTriase)) ? ttdEmergent : null;
            case SIG_URGENT:
                return "URGENT".equalsIgnoreCase(ambilCombo(CmbKategoriTriase)) ? ttdUrgent : null;
            case SIG_NON_URGENT:
                return "NON URGENT".equalsIgnoreCase(ambilCombo(CmbKategoriTriase)) ? ttdNonUrgent : null;
            default:
                return getSignatureData(tipe);
        }
    }

    private String getLabelPetugasTriase() {
        String nama = ambil(TNamaPetugasTriase);
        if (nama.isEmpty()) {
            return "";
        }
        String profesi = ambilCombo(CmbProfesiPetugasTriase);
        if (profesi.isEmpty()) {
            return nama;
        }
        return profesi + " : " + nama;
    }

    private String getLabelPetugasTriaseUntukKategori(String kategori) {
        if (kategori.equalsIgnoreCase(ambilCombo(CmbKategoriTriase))) {
            return getLabelPetugasTriase();
        }
        return "";
    }

    private void resetSemuaTtd() {
        setSignatureData(SIG_RESUSITASI, null);
        setSignatureData(SIG_EMERGENT, null);
        setSignatureData(SIG_URGENT, null);
        setSignatureData(SIG_NON_URGENT, null);
        setSignatureData(SIG_PERAWAT, null);
        setSignatureData(SIG_PENANGGUNG_JAWAB, null);
    }

    private void updateSignatureLabel(JLabel label, byte[] data) {
        label.setIcon(null);
        if (data == null || data.length == 0) {
            label.setText("Belum ada tanda tangan");
            return;
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
            if (image == null) {
                label.setText("Tanda tangan tidak valid");
                return;
            }
            Image scaled = image.getScaledInstance(150, 54, Image.SCALE_SMOOTH);
            label.setIcon(new javax.swing.ImageIcon(scaled));
            label.setText("");
        } catch (Exception e) {
            label.setText("Tanda tangan tidak valid");
        }
    }

    private void setBlob(PreparedStatement statement, int index, byte[] data) throws SQLException {
        if (data == null || data.length == 0) {
            statement.setNull(index, Types.LONGVARBINARY);
        } else {
            statement.setBytes(index, data);
        }
    }

    private Image buatImageReport(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        try {
            return ImageIO.read(new ByteArrayInputStream(data));
        } catch (Exception e) {
            return null;
        }
    }

    private JPanel buatPanelHero() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(true);
        panel.setBackground(WARNA_SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARNA_BORDER),
            new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel judul = new JLabel("Asesmen Pasien UGD V2");
        judul.setFont(FONT_TITLE);
        judul.setForeground(WARNA_TEXT);

        JLabel subjudul = new JLabel("<html>Tampilan lebih bersih untuk asesmen cepat, review data lebih nyaman, dan alur input tetap kompatibel dengan sistem saat ini.</html>");
        subjudul.setFont(FONT_BODY);
        subjudul.setForeground(WARNA_MUTED);

        JPanel teks = new JPanel(new BorderLayout(0, 4));
        teks.setOpaque(false);
        teks.add(judul, BorderLayout.NORTH);
        teks.add(subjudul, BorderLayout.CENTER);

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        badgePanel.setOpaque(false);
        badgePanel.add(buatBadge("Java 8 Ready", WARNA_ACCENT_SOFT, WARNA_ACCENT));
        badgePanel.add(buatBadge("Jasper Report", new Color(219, 234, 254), new Color(30, 64, 175)));
        badgePanel.add(buatBadge("Workflow IGD", new Color(254, 242, 242), new Color(153, 27, 27)));

        panel.add(teks, BorderLayout.CENTER);
        panel.add(badgePanel, BorderLayout.EAST);
        return panel;
    }

    private JLabel buatBadge(String text, Color background, Color foreground) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(background);
        label.setForeground(foreground);
        label.setFont(FONT_LABEL);
        label.setBorder(new EmptyBorder(7, 10, 7, 10));
        return label;
    }

    private JComponent bungkusKartu(Component component, int h, int v) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(true);
        panel.setBackground(WARNA_SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARNA_BORDER),
            new EmptyBorder(v, h, v, h)
        ));
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private JLabel labelFilter(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(WARNA_MUTED);
        return label;
    }

    private void aturMenuItem(JMenuItem item) {
        item.setFont(FONT_BODY);
        item.setBackground(WARNA_SURFACE);
        item.setForeground(WARNA_TEXT);
    }

    private void aturTextReadonly(TextBox field) {
        field.setEditable(false);
        field.setBackground(WARNA_FIELD_READONLY);
        field.setForeground(new Color(71, 85, 105));
        field.setCaretColor(WARNA_ACCENT);
    }

    private void aturScrollPane(JScrollPane scroll, boolean gunakanLatarAplikasi) {
        scroll.setBorder(BorderFactory.createLineBorder(WARNA_BORDER));
        scroll.getViewport().setBackground(gunakanLatarAplikasi ? WARNA_BG : WARNA_SURFACE);
        scroll.setBackground(gunakanLatarAplikasi ? WARNA_BG : WARNA_SURFACE);
    }

    private void aturTemaVisual() {
        TabUtama.setFont(FONT_LABEL);
        TabUtama.setBackground(WARNA_SURFACE);
        TabUtama.setForeground(WARNA_TEXT);
        TabUtama.setBorder(BorderFactory.createLineBorder(WARNA_BORDER));

        aturTemaButton(BtnBaru, new Color(15, 23, 42), Color.WHITE);
        aturTemaButton(BtnSimpan, new Color(22, 163, 74), Color.WHITE);
        aturTemaButton(BtnEdit, new Color(21, 128, 61), Color.WHITE);
        aturTemaButton(BtnHapus, new Color(220, 38, 38), Color.WHITE);
        aturTemaButton(BtnPrint, new Color(0, 0, 0), Color.WHITE);
        aturTemaButton(BtnCari, new Color(15, 23, 42), Color.WHITE);
        aturTemaButton(BtnTutup, new Color(100, 116, 139), Color.WHITE);

        aturTemaTanggal(DTPTanggalMasuk);
        aturTemaTanggal(DTPTanggalKeluar);
        aturTemaTanggal(DTPCari1);
        aturTemaTanggal(DTPCari2);

        tbData.setRowHeight(30);
        tbData.setShowGrid(false);
        tbData.setIntercellSpacing(new Dimension(0, 0));
        tbData.setSelectionBackground(WARNA_ACCENT_SOFT);
        tbData.setSelectionForeground(WARNA_TEXT);
        tbData.setBackground(WARNA_SURFACE);
        tbData.setForeground(WARNA_TEXT);
        tbData.setFont(FONT_BODY);
        tbData.getTableHeader().setFont(FONT_LABEL);
        tbData.getTableHeader().setBackground(new Color(226, 232, 240));
        tbData.getTableHeader().setForeground(WARNA_TEXT);
        tbData.getTableHeader().setReorderingAllowed(false);
        tbData.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    comp.setBackground(WARNA_ACCENT_SOFT);
                    comp.setForeground(WARNA_TEXT);
                } else {
                    comp.setBackground(row % 2 == 0 ? WARNA_SURFACE : WARNA_SURFACE_SOFT);
                    comp.setForeground(WARNA_TEXT);
                }
                return comp;
            }
        });
    }

    private void aturTemaTanggal(Tanggal tanggal) {
        tanggal.setFont(FONT_BODY);
        tanggal.setBackground(WARNA_FIELD);
        tanggal.setForeground(WARNA_TEXT);
        tanggal.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARNA_BORDER),
            new EmptyBorder(2, 8, 2, 8)
        ));
    }

    private void aturTemaButton(Button button, Color warna, Color teks) {
        button.setForeground(teks);
        button.setBackground(warna);
        button.setGlassColor(warna);
    }

    private String ambil(javax.swing.text.JTextComponent component) {
        return component.getText().trim();
    }

    private String ambilCombo(JComboBox<String> combo) {
        Object selected = combo.getSelectedItem();
        return selected == null ? "" : selected.toString();
    }

    private String gabungCheck(javax.swing.JCheckBox... checks) {
        StringBuilder hasil = new StringBuilder();
        for (javax.swing.JCheckBox check : checks) {
            if (check.isSelected()) {
                if (hasil.length() > 0) {
                    hasil.append(", ");
                }
                hasil.append(check.getText());
            }
        }
        return hasil.toString();
    }

    private void setChecks(String nilai, javax.swing.JCheckBox... checks) {
        resetCheck(checks);
        if (nilai == null || nilai.trim().isEmpty()) {
            return;
        }
        Set<String> pilihan = new LinkedHashSet<String>();
        String[] bagian = nilai.split(",");
        for (String item : bagian) {
            pilihan.add(item.trim());
        }
        for (javax.swing.JCheckBox check : checks) {
            check.setSelected(pilihan.contains(check.getText()));
        }
    }

    private void resetCheck(javax.swing.JCheckBox... checks) {
        for (javax.swing.JCheckBox check : checks) {
            check.setSelected(false);
        }
    }

    private String nvl(String nilai) {
        return nilai == null ? "" : nilai;
    }

    private String nvlCombo(String nilai, JComboBox<String> combo) {
        if (nilai == null || nilai.trim().isEmpty()) {
            return combo.getItemAt(0);
        }
        return pilihanAda(combo, nilai) ? nilai : combo.getItemAt(0);
    }

    private boolean pilihanAda(JComboBox<String> combo, String nilai) {
        if (nilai == null) {
            return false;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (nilai.equals(combo.getItemAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static final class SignaturePadDialog extends JDialog {
        private final SignatureCanvas canvas = new SignatureCanvas(440, 180);
        private byte[] hasil;
        private boolean disimpan;

        SignaturePadDialog(JDialog owner, String judul, byte[] existing) {
            super(owner, true);
            setTitle("Tanda Tangan - " + judul);
            setLayout(new BorderLayout(10, 10));
            ((JComponent) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

            JLabel info = new JLabel("Gambar tanda tangan pada area putih di bawah ini.");
            info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            add(info, BorderLayout.NORTH);
            add(canvas, BorderLayout.CENTER);

            JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            javax.swing.JButton btnBersihkan = new javax.swing.JButton("Bersihkan");
            javax.swing.JButton btnBatal = new javax.swing.JButton("Batal");
            javax.swing.JButton btnSimpan = new javax.swing.JButton("Simpan");
            tombol.add(btnBersihkan);
            tombol.add(btnBatal);
            tombol.add(btnSimpan);
            add(tombol, BorderLayout.SOUTH);

            if (existing != null && existing.length > 0) {
                canvas.setImage(existing);
            }

            btnBersihkan.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    canvas.clear();
                }
            });
            btnBatal.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            btnSimpan.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    hasil = canvas.toPngBytes();
                    disimpan = true;
                    dispose();
                }
            });

            pack();
            setLocationRelativeTo(owner);
        }

        byte[] showDialog() {
            setVisible(true);
            return disimpan ? hasil : null;
        }
    }

    private static final class SignatureCanvas extends JPanel {
        private final BufferedImage image;
        private Point lastPoint;

        SignatureCanvas(int width, int height) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            setPreferredSize(new Dimension(width, height));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(180, 188, 201)));
            clear();

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    lastPoint = e.getPoint();
                    gambarTitik(lastPoint);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    lastPoint = null;
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    Point current = e.getPoint();
                    gambarGaris(lastPoint, current);
                    lastPoint = current;
                }
            });
        }

        void clear() {
            Graphics2D g2 = image.createGraphics();
            g2.setComposite(AlphaComposite.Clear);
            g2.fillRect(0, 0, image.getWidth(), image.getHeight());
            g2.dispose();
            repaint();
        }

        void setImage(byte[] data) {
            clear();
            try {
                BufferedImage loaded = ImageIO.read(new ByteArrayInputStream(data));
                if (loaded != null) {
                    Graphics2D g2 = image.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.drawImage(loaded, 0, 0, image.getWidth(), image.getHeight(), null);
                    g2.dispose();
                    repaint();
                }
            } catch (IOException e) {
                clear();
            }
        }

        byte[] toPngBytes() {
            try {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                ImageIO.write(image, "png", output);
                return output.toByteArray();
            } catch (IOException e) {
                return null;
            }
        }

        private void gambarTitik(Point titik) {
            if (titik == null) {
                return;
            }
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
            g2.fillOval(titik.x, titik.y, 3, 3);
            g2.dispose();
            repaint();
        }

        private void gambarGaris(Point awal, Point akhir) {
            if (awal == null || akhir == null) {
                return;
            }
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(awal.x, awal.y, akhir.x, akhir.y);
            g2.dispose();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.drawImage(image, 0, 0, null);
        }
    }
}
