package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import kepegawaian.DlgCariDokter;
import net.sf.jasperreports.engine.JasperCompileManager;
import widget.Button;
import widget.Table;
import widget.Tanggal;
import widget.TextArea;
import widget.TextBox;

public final class RMResumeMedisRanapV2 extends JDialog {
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

    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private final DlgCariDokter dokter = new DlgCariDokter(null, false);
    private final SimpleDateFormat formatJam = new SimpleDateFormat("HH:mm");
    private final DefaultTableModel tabMode;

    private final TextBox TNoRw = buatTextBox(15);
    private final TextBox TNoRM = buatTextBox(15);
    private final TextBox TPasien = buatTextBox(36);
    private final TextBox TRuang = buatTextBox(24);
    private final TextBox TPenjab = buatTextBox(24);
    private final TextBox TTglMasuk = buatTextBox(12);
    private final TextBox TJamMasuk = buatTextBox(8);
    private final TextBox TTglKeluar = buatTextBox(12);
    private final TextBox TJamKeluar = buatTextBox(8);

    private final TextBox TKodeDokter = buatTextBox(12);
    private final TextBox TNamaDokter = buatTextBox(28);
    private final Button BtnDokter = buatButtonMini("Pilih");
    private final Button BtnAmbilAlasanSoap = buatButtonMini("Ambil");
    private final Button BtnAmbilPemeriksaanSoap = buatButtonMini("Ambil");
    private final Button BtnAmbilTerapiObatSoap = buatButtonMini("Ambil");

    private final TextArea AreaAlasanRawat = buatTextArea(3);
    private final TextBox TDiagnosaMasuk = buatTextBox(26);
    private final TextBox TICD10Masuk = buatTextBox(12);
    private final TextBox TDiagnosaKeluar = buatTextBox(26);
    private final TextBox TICD10Keluar = buatTextBox(12);
    private final TextBox TDiagnosaSekunder1 = buatTextBox(26);
    private final TextBox TICD10Sekunder1 = buatTextBox(12);
    private final TextBox TDiagnosaSekunder2 = buatTextBox(26);
    private final TextBox TICD10Sekunder2 = buatTextBox(12);
    private final TextBox TDiagnosaSekunder3 = buatTextBox(26);
    private final TextBox TICD10Sekunder3 = buatTextBox(12);

    private final TextArea AreaTerapiTindakan = buatTextArea(4);
    private final TextBox TICD9CM = buatTextBox(16);
    private final TextArea AreaPenyebabKematian = buatTextArea(3);
    private final TextArea AreaPemeriksaanFisik = buatTextArea(4);
    private final TextArea AreaLaboratorium = buatTextArea(4);
    private final TextArea AreaRadiologi = buatTextArea(4);
    private final TextArea AreaPenunjangLain = buatTextArea(4);
    private final TextArea AreaTerapiObat = buatTextArea(4);
    private final TextArea AreaInstruksi = buatTextArea(4);

    private final javax.swing.JComboBox<String> CmbCaraKeluar = buatCombo(
        "Diijinkan Pulang",
        "Melarikan Diri",
        "Pindah Rumah Sakit",
        "Pulang Atas Permintaan Sendiri",
        "Dirujuk Ke"
    );
    private final TextBox TDirujukKe = buatTextBox(24);
    private final javax.swing.JComboBox<String> CmbKeadaanKeluar = buatCombo("Sembuh", "Membaik", "Belum Sembuh", "Meninggal");
    private final Tanggal DTPTanggalResume = new Tanggal();
    private final TextBox TJamResume = buatTextBox(8);

    private final JLabel LblTtdDokter = buatLabelSignature();
    private final Button BtnInputTtd = buatButtonMini("Input TTD");
    private final Button BtnUploadTtd = buatButtonMini("Scan TTD");
    private final Button BtnHapusTtd = buatButtonMini("Hapus TTD");
    private byte[] ttdDokter;

    private final Tanggal DTPCari1 = new Tanggal();
    private final Tanggal DTPCari2 = new Tanggal();
    private final TextBox TCari = buatTextBox(28);
    private final Table tbData = new Table();
    private final JTabbedPane TabUtama = new JTabbedPane();
    private JScrollPane ScrollFormUtama;
    private final JPopupMenu PopupData = new JPopupMenu();
    private final JMenuItem MnBukaForm = new JMenuItem("Buka/Edit Form");
    private final JMenuItem MnPreviewJasper = new JMenuItem("Preview Jasper");
    private final JMenuItem MnCetakPdf = new JMenuItem("Cetak PDF");

    private final Button BtnBaru = buatButton("Baru");
    private final Button BtnSimpan = buatButton("Simpan");
    private final Button BtnEdit = buatButton("Ubah");
    private final Button BtnHapus = buatButton("Hapus");
    private final Button BtnPrint = buatButton("Preview Jasper");
    private final Button BtnCari = buatButton("Cari");
    private final Button BtnTutup = buatButton("Tutup");

    public RMResumeMedisRanapV2(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("Resume Medis Ranap V2");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initDokterListener();
        initComponents();
        tabMode = new DefaultTableModel(null, new Object[]{
            "No.Rawat", "No.RM", "Nama Pasien", "Dokter", "Tanggal Resume", "Keadaan Keluar", "Cara Keluar"
        }) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tbData.setModel(tabMode);
        tbData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbData.setPreferredScrollableViewportSize(new Dimension(920, 260));
        tbData.setAutoResizeMode(Table.AUTO_RESIZE_OFF);
        tbData.getColumnModel().getColumn(0).setPreferredWidth(110);
        tbData.getColumnModel().getColumn(1).setPreferredWidth(80);
        tbData.getColumnModel().getColumn(2).setPreferredWidth(220);
        tbData.getColumnModel().getColumn(3).setPreferredWidth(180);
        tbData.getColumnModel().getColumn(4).setPreferredWidth(110);
        tbData.getColumnModel().getColumn(5).setPreferredWidth(140);
        tbData.getColumnModel().getColumn(6).setPreferredWidth(170);
        aturTemaVisual();
        initPopupData();
        aktifkanEvent();
        aturFieldReadonly();
        setTanggalAwal();
        ensureTable();
        ensureAksesResumeRanapV2();
        tampilData();
        isiDefaultDokterLogin();
        isCek();
        pack();
        if (getWidth() < 1220 || getHeight() < 860) {
            setSize(Math.max(1220, getWidth()), Math.max(860, getHeight()));
        }
        setLocationRelativeTo(parent);
    }

    private void initDokterListener() {
        dokter.addWindowListener(new WindowListener() {
            @Override public void windowOpened(WindowEvent e) {}
            @Override public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if (dokter.getTable().getSelectedRow() != -1) {
                    TKodeDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 0).toString());
                    TNamaDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 1).toString());
                }
            }
            @Override public void windowIconified(WindowEvent e) {}
            @Override public void windowDeiconified(WindowEvent e) {}
            @Override public void windowActivated(WindowEvent e) {}
            @Override public void windowDeactivated(WindowEvent e) {}
        });
    }

    private void initComponents() {
        getContentPane().setLayout(new BorderLayout(0, 12));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(14, 14, 14, 14));
        ((JComponent) getContentPane()).setBackground(WARNA_BG);

        JPanel panelUtama = new JPanel(new BorderLayout(0, 12));
        panelUtama.setOpaque(false);
        panelUtama.add(buatPanelHero(), BorderLayout.NORTH);
        panelUtama.add(TabUtama, BorderLayout.CENTER);

        ScrollFormUtama = new JScrollPane(bangunFormPanel());
        aturScrollPane(ScrollFormUtama, true);
        TabUtama.addTab("Form Resume", ScrollFormUtama);
        TabUtama.addTab("Data Tersimpan", bangunPanelList());

        getContentPane().add(panelUtama, BorderLayout.CENTER);
        getContentPane().add(panelAksi(), BorderLayout.SOUTH);
    }

    private JPanel bangunFormPanel() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setOpaque(false);
        GridBagConstraints gbc = dasarGbc();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        root.add(bungkusKartu(bangunPanelIdentitas(), 18, 18), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 0, 0);
        root.add(bungkusKartu(bangunPanelRingkasanMedis(), 18, 18), gbc);

        gbc.gridy++;
        root.add(bungkusKartu(bangunPanelPemeriksaan(), 18, 18), gbc);

        gbc.gridy++;
        root.add(bungkusKartu(bangunPanelKeluarTtd(), 18, 18), gbc);

        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        root.add(new JPanel(), gbc);
        return root;
    }

    private JPanel bangunPanelIdentitas() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        int row = 0;
        row = tambahJudul(panel, row, "Identitas Rawat Inap");
        row = tambahDuaKolom(panel, row, "No. Rawat *", TNoRw, "No. RM *", TNoRM);
        row = tambahDuaKolom(panel, row, "Nama Pasien *", TPasien, "Ruang/Bangsal", TRuang);
        row = tambahDuaKolom(panel, row, "Penjamin", TPenjab, "Tanggal Masuk", flowPanel(TTglMasuk, labelKiri("Jam"), TJamMasuk));
        row = tambahDuaKolom(panel, row, "Tanggal Keluar", flowPanel(TTglKeluar, labelKiri("Jam"), TJamKeluar), "Dokter *", flowPanel(TKodeDokter, TNamaDokter, BtnDokter));
        return panel;
    }

    private JPanel bangunPanelRingkasanMedis() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        int row = 0;
        row = tambahJudul(panel, row, "Resume Medis");
        row = tambahAreaDenganTombol(panel, row, "Alasan / Indikasi Dirawat", AreaAlasanRawat, 74, BtnAmbilAlasanSoap);
        row = tambahDuaKolom(panel, row, "Diagnosa Masuk", TDiagnosaMasuk, "Kode ICD 10", TICD10Masuk);
        row = tambahDuaKolom(panel, row, "Diagnosa Keluar *", TDiagnosaKeluar, "Kode ICD 10", TICD10Keluar);
        row = tambahDuaKolom(panel, row, "Diagnosa Sekunder 1", TDiagnosaSekunder1, "ICD 10 Sek. 1", TICD10Sekunder1);
        row = tambahDuaKolom(panel, row, "Diagnosa Sekunder 2", TDiagnosaSekunder2, "ICD 10 Sek. 2", TICD10Sekunder2);
        row = tambahDuaKolom(panel, row, "Diagnosa Sekunder 3", TDiagnosaSekunder3, "ICD 10 Sek. 3", TICD10Sekunder3);
        row = tambahDuaKolom(panel, row, "Kode ICD 9 CM", TICD9CM, "Penyebab Kematian", scrollArea(AreaPenyebabKematian, 74));
        return panel;
    }

    private JPanel bangunPanelPemeriksaan() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        int row = 0;
        row = tambahJudul(panel, row, "Pemeriksaan Dan Tindak Lanjut");
        row = tambahAreaDenganTombol(panel, row, "Pemeriksaan Fisik", AreaPemeriksaanFisik, 84, BtnAmbilPemeriksaanSoap);
        row = tambahArea(panel, row, "Laboratorium", AreaLaboratorium, 84);
        row = tambahArea(panel, row, "Radiologi", AreaRadiologi, 84);
        row = tambahArea(panel, row, "Penunjang Lainnya", AreaPenunjangLain, 84);
        row = tambahAreaDenganTombol(panel, row, "Terapi / Obat Yang Diberikan", AreaTerapiObat, 90, BtnAmbilTerapiObatSoap);
        row = tambahArea(panel, row, "Instruksi Untuk Tindak Lanjut", AreaInstruksi, 96);
        return panel;
    }

    private JPanel bangunPanelKeluarTtd() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        int row = 0;
        row = tambahJudul(panel, row, "Status Keluar Dan Validasi Dokter");
        row = tambahDuaKolom(panel, row, "Cara Keluar", CmbCaraKeluar, "Dirujuk Ke", TDirujukKe);
        row = tambahDuaKolom(panel, row, "Keadaan Keluar", CmbKeadaanKeluar, "Tanggal Resume *", flowPanel(DTPTanggalResume, labelKiri("Jam"), TJamResume));
        row = tambahSatuKolom(panel, row, "Tanda Tangan Dokter", panelSignatureDokter());
        return panel;
    }

    private JPanel panelSignatureDokter() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.add(LblTtdDokter, BorderLayout.WEST);
        panel.add(flowPanel(BtnInputTtd, BtnUploadTtd, BtnHapusTtd), BorderLayout.CENTER);
        return panel;
    }

    private JPanel bangunPanelList() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filter.setOpaque(false);
        filter.add(labelFilter("Tanggal"));
        filter.add(DTPCari1);
        filter.add(labelFilter("s.d."));
        filter.add(DTPCari2);
        filter.add(labelFilter("Cari"));
        filter.add(TCari);
        filter.add(BtnCari);

        JScrollPane scroll = new JScrollPane(tbData);
        aturScrollPane(scroll, false);
        panel.add(filter, BorderLayout.NORTH);
        panel.add(bungkusKartu(scroll, 12, 12), BorderLayout.CENTER);
        return panel;
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

    private void initPopupData() {
        aturMenuItem(MnBukaForm);
        aturMenuItem(MnPreviewJasper);
        aturMenuItem(MnCetakPdf);
        MnBukaForm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bukaDataTerpilih();
            }
        });
        MnPreviewJasper.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bukaDataTerpilih();
                previewJasper();
            }
        });
        MnCetakPdf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bukaDataTerpilih();
                cetakPdf();
            }
        });
        PopupData.add(MnBukaForm);
        PopupData.add(MnPreviewJasper);
        PopupData.add(MnCetakPdf);
        tbData.setComponentPopupMenu(PopupData);
    }

    private void aktifkanEvent() {
        BtnDokter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bukaCariDokter();
            }
        });
        BtnAmbilAlasanSoap.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bukaPemilihSoapDokter("keluhan");
            }
        });
        BtnAmbilPemeriksaanSoap.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bukaPemilihSoapDokter("pemeriksaan");
            }
        });
        BtnAmbilTerapiObatSoap.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bukaPemilihSoapDokter("rtl");
            }
        });
        BtnInputTtd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bukaEditorTtd();
            }
        });
        BtnUploadTtd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bukaFileTtd();
            }
        });
        BtnHapusTtd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSignatureData(null);
            }
        });
        BtnBaru.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bersihkanForm();
            }
        });
        BtnCari.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tampilData();
            }
        });
        BtnSimpan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                simpanBaru();
            }
        });
        BtnEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateData();
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
        CmbCaraKeluar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sinkronkanCaraKeluar();
            }
        });
        tbData.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tbData.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    tbData.setRowSelectionInterval(row, row);
                }
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2 && row >= 0) {
                    bukaDataTerpilih();
                }
            }
        });
    }

    private void aturFieldReadonly() {
        aturTextReadonly(TNoRw);
        aturTextReadonly(TNoRM);
        aturTextReadonly(TPasien);
        aturTextReadonly(TRuang);
        aturTextReadonly(TPenjab);
        aturTextReadonly(TKodeDokter);
        aturTextReadonly(TNamaDokter);
    }

    private void setTanggalAwal() {
        Date sekarang = new Date();
        DTPCari1.setDate(sekarang);
        DTPCari2.setDate(sekarang);
        DTPTanggalResume.setDate(sekarang);
        TJamResume.setText(formatJam.format(sekarang));
        sinkronkanCaraKeluar();
    }

    private void ensureTable() {
        String sql =
            "create table if not exists resume_medis_ranap_v2 (" +
            "no_rawat varchar(17) not null primary key," +
            "no_rkm_medis varchar(15) not null," +
            "nama_pasien varchar(120) not null," +
            "ruang_bangsal varchar(120) null," +
            "penjamin varchar(120) null," +
            "tgl_masuk date null," +
            "jam_masuk varchar(8) null," +
            "tgl_keluar date null," +
            "jam_keluar varchar(8) null," +
            "kd_dokter varchar(20) not null," +
            "nm_dokter varchar(120) not null," +
            "alasan_rawat text null," +
            "diagnosa_masuk varchar(255) null," +
            "icd10_masuk varchar(20) null," +
            "diagnosa_keluar varchar(255) null," +
            "icd10_keluar varchar(20) null," +
            "diagnosa_sekunder1 varchar(255) null," +
            "icd10_sekunder1 varchar(20) null," +
            "diagnosa_sekunder2 varchar(255) null," +
            "icd10_sekunder2 varchar(20) null," +
            "diagnosa_sekunder3 varchar(255) null," +
            "icd10_sekunder3 varchar(20) null," +
            "terapi_tindakan text null," +
            "icd9_cm varchar(50) null," +
            "penyebab_kematian text null," +
            "pemeriksaan_fisik text null," +
            "laboratorium text null," +
            "radiologi text null," +
            "penunjang_lain text null," +
            "terapi_obat text null," +
            "instruksi_tindak_lanjut text null," +
            "cara_keluar varchar(120) null," +
            "dirujuk_ke varchar(255) null," +
            "keadaan_keluar varchar(120) null," +
            "tanggal_resume date null," +
            "jam_resume varchar(8) null," +
            "ttd_dokter longblob null," +
            "created_by varchar(50) null," +
            "updated_by varchar(50) null," +
            "created_at datetime null," +
            "updated_at datetime null" +
            ")";
        Sequel.queryu2(sql);
    }

    private void ensureAksesResumeRanapV2() {
        try {
            if (Sequel.cariInteger("select count(*) from information_schema.columns where table_schema=database() and table_name='user' and column_name='data_resume_ranap_v2'") == 0) {
                Sequel.queryu2("alter table user add column data_resume_ranap_v2 enum('true','false') default 'false'");
            }
        } catch (Exception e) {
            System.out.println("Notif akses Resume Ranap V2 : " + e);
        }
    }

    public void tampilData() {
        tabMode.setRowCount(0);
        PreparedStatement stmt = null;
        ResultSet hasil = null;
        try {
            String sql =
                "select no_rawat,no_rkm_medis,nama_pasien,nm_dokter,tanggal_resume,keadaan_keluar,cara_keluar " +
                "from resume_medis_ranap_v2 where tanggal_resume between ? and ? " +
                (TCari.getText().trim().isEmpty() ? "" :
                "and (no_rawat like ? or no_rkm_medis like ? or nama_pasien like ? or nm_dokter like ? or diagnosa_keluar like ?) ") +
                "order by tanggal_resume desc, updated_at desc";
            stmt = koneksi.prepareStatement(sql);
            stmt.setDate(1, new java.sql.Date(DTPCari1.getDate().getTime()));
            stmt.setDate(2, new java.sql.Date(DTPCari2.getDate().getTime()));
            if (!TCari.getText().trim().isEmpty()) {
                String cari = "%" + TCari.getText().trim() + "%";
                stmt.setString(3, cari);
                stmt.setString(4, cari);
                stmt.setString(5, cari);
                stmt.setString(6, cari);
                stmt.setString(7, cari);
            }
            hasil = stmt.executeQuery();
            while (hasil.next()) {
                tabMode.addRow(new Object[]{
                    hasil.getString("no_rawat"),
                    hasil.getString("no_rkm_medis"),
                    hasil.getString("nama_pasien"),
                    hasil.getString("nm_dokter"),
                    hasil.getString("tanggal_resume"),
                    hasil.getString("keadaan_keluar"),
                    hasil.getString("cara_keluar")
                });
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        } finally {
            try {
                if (hasil != null) {
                    hasil.close();
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
        }
    }

    public void setNoRm(String norwt, Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        if (tgl2 != null) {
            DTPCari2.setDate(tgl2);
        }
        isRawat();
        muatDataJikaAda();
        TabUtama.setSelectedIndex(0);
    }

    public void isCek() {
        boolean boleh = akses.getdata_resume_ranap_v2();
        BtnSimpan.setEnabled(boleh);
        BtnEdit.setEnabled(boleh);
        BtnHapus.setEnabled(boleh);
        BtnPrint.setEnabled(boleh);
        BtnDokter.setEnabled(boleh);
        BtnAmbilAlasanSoap.setEnabled(boleh);
        BtnAmbilPemeriksaanSoap.setEnabled(boleh);
        BtnAmbilTerapiObatSoap.setEnabled(boleh);
        BtnInputTtd.setEnabled(boleh);
        BtnUploadTtd.setEnabled(boleh);
        BtnHapusTtd.setEnabled(boleh);
        MnBukaForm.setEnabled(boleh);
        MnPreviewJasper.setEnabled(boleh);
        MnCetakPdf.setEnabled(boleh);
    }

    private void isRawat() {
        tutupStatement();
        try {
            ps = koneksi.prepareStatement(
                "select reg_periksa.no_rkm_medis,pasien.nm_pasien,reg_periksa.tgl_registrasi,reg_periksa.jam_reg," +
                "reg_periksa.kd_pj,penjab.png_jawab,if(kamar_inap.tgl_keluar='0000-00-00',current_date(),kamar_inap.tgl_keluar) as tgl_keluar," +
                "if(kamar_inap.jam_keluar='00:00:00',current_time(),kamar_inap.jam_keluar) as jam_keluar," +
                "kamar_inap.diagnosa_awal,bangsal.nm_bangsal from reg_periksa " +
                "inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis " +
                "inner join penjab on penjab.kd_pj=reg_periksa.kd_pj " +
                "inner join kamar_inap on kamar_inap.no_rawat=reg_periksa.no_rawat " +
                "inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar " +
                "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal " +
                "where reg_periksa.no_rawat=? order by kamar_inap.tgl_keluar desc,kamar_inap.jam_keluar desc limit 1"
            );
            ps.setString(1, TNoRw.getText());
            rs = ps.executeQuery();
            if (rs.next()) {
                TNoRM.setText(nvl(rs.getString("no_rkm_medis")));
                TPasien.setText(nvl(rs.getString("nm_pasien")));
                TRuang.setText(nvl(rs.getString("nm_bangsal")));
                TPenjab.setText(nvl(rs.getString("png_jawab")));
                TTglMasuk.setText(nvl(rs.getString("tgl_registrasi")));
                TJamMasuk.setText(nvl(rs.getString("jam_reg")));
                TTglKeluar.setText(nvl(rs.getString("tgl_keluar")));
                TJamKeluar.setText(nvl(rs.getString("jam_keluar")));
                if (ambil(TDiagnosaMasuk).isEmpty()) {
                    TDiagnosaMasuk.setText(nvl(rs.getString("diagnosa_awal")));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        } finally {
            tutupStatement();
        }
    }

    private void muatDataJikaAda() {
        if (TNoRw.getText().trim().isEmpty()) {
            return;
        }
        PreparedStatement stmt = null;
        ResultSet hasil = null;
        try {
            stmt = koneksi.prepareStatement("select * from resume_medis_ranap_v2 where no_rawat=?");
            stmt.setString(1, TNoRw.getText());
            hasil = stmt.executeQuery();
            if (hasil.next()) {
                isiDariResultSet(hasil);
            } else {
                kosongkanResume();
                isiDefaultDokterLogin();
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        } finally {
            try {
                if (hasil != null) {
                    hasil.close();
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
        }
    }

    private void isiDariResultSet(ResultSet data) throws SQLException {
        TNoRw.setText(nvl(data.getString("no_rawat")));
        TNoRM.setText(nvl(data.getString("no_rkm_medis")));
        TPasien.setText(nvl(data.getString("nama_pasien")));
        TRuang.setText(nvl(data.getString("ruang_bangsal")));
        TPenjab.setText(nvl(data.getString("penjamin")));
        TTglMasuk.setText(nvl(data.getString("tgl_masuk")));
        TJamMasuk.setText(nvl(data.getString("jam_masuk")));
        TTglKeluar.setText(nvl(data.getString("tgl_keluar")));
        TJamKeluar.setText(nvl(data.getString("jam_keluar")));
        TKodeDokter.setText(nvl(data.getString("kd_dokter")));
        TNamaDokter.setText(nvl(data.getString("nm_dokter")));
        AreaAlasanRawat.setText(nvl(data.getString("alasan_rawat")));
        TDiagnosaMasuk.setText(nvl(data.getString("diagnosa_masuk")));
        TICD10Masuk.setText(nvl(data.getString("icd10_masuk")));
        TDiagnosaKeluar.setText(nvl(data.getString("diagnosa_keluar")));
        TICD10Keluar.setText(nvl(data.getString("icd10_keluar")));
        TDiagnosaSekunder1.setText(nvl(data.getString("diagnosa_sekunder1")));
        TICD10Sekunder1.setText(nvl(data.getString("icd10_sekunder1")));
        TDiagnosaSekunder2.setText(nvl(data.getString("diagnosa_sekunder2")));
        TICD10Sekunder2.setText(nvl(data.getString("icd10_sekunder2")));
        TDiagnosaSekunder3.setText(nvl(data.getString("diagnosa_sekunder3")));
        TICD10Sekunder3.setText(nvl(data.getString("icd10_sekunder3")));
        AreaTerapiTindakan.setText(nvl(data.getString("terapi_tindakan")));
        TICD9CM.setText(nvl(data.getString("icd9_cm")));
        AreaPenyebabKematian.setText(nvl(data.getString("penyebab_kematian")));
        AreaPemeriksaanFisik.setText(nvl(data.getString("pemeriksaan_fisik")));
        AreaLaboratorium.setText(nvl(data.getString("laboratorium")));
        AreaRadiologi.setText(nvl(data.getString("radiologi")));
        AreaPenunjangLain.setText(nvl(data.getString("penunjang_lain")));
        AreaTerapiObat.setText(nvl(data.getString("terapi_obat")));
        AreaInstruksi.setText(nvl(data.getString("instruksi_tindak_lanjut")));
        CmbCaraKeluar.setSelectedItem(nvlCombo(data.getString("cara_keluar"), CmbCaraKeluar));
        TDirujukKe.setText(nvl(data.getString("dirujuk_ke")));
        CmbKeadaanKeluar.setSelectedItem(nvlCombo(data.getString("keadaan_keluar"), CmbKeadaanKeluar));
        if (data.getDate("tanggal_resume") != null) {
            DTPTanggalResume.setDate(data.getDate("tanggal_resume"));
        }
        TJamResume.setText(nvl(data.getString("jam_resume")));
        setSignatureData(data.getBytes("ttd_dokter"));
        sinkronkanCaraKeluar();
    }

    private boolean validasiInput() {
        if (ambil(TNoRw).isEmpty() || ambil(TNoRM).isEmpty() || ambil(TPasien).isEmpty()) {
            Valid.textKosong(TNoRw, "Pasien Rawat Inap");
            return false;
        }
        if (ambil(TKodeDokter).isEmpty() || ambil(TNamaDokter).isEmpty()) {
            Valid.textKosong(BtnDokter, "Dokter");
            return false;
        }
        if (ambil(TDiagnosaKeluar).isEmpty()) {
            Valid.textKosong(TDiagnosaKeluar, "Diagnosa Keluar");
            return false;
        }
        if (!validasiTanggalJamMasuk()) {
            return false;
        }
        if (!validasiTanggalJamKeluar()) {
            return false;
        }
        if (DTPTanggalResume.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Tanggal resume belum diisi.");
            return false;
        }
        return true;
    }

    private void simpanBaru() {
        if (!validasiInput()) {
            return;
        }
        if (dataSudahAda()) {
            JOptionPane.showMessageDialog(this, "Data resume untuk no. rawat ini sudah ada. Gunakan tombol Ubah.");
            return;
        }
        PreparedStatement stmt = null;
        try {
            stmt = koneksi.prepareStatement(
                "insert into resume_medis_ranap_v2 (" +
                "no_rawat,no_rkm_medis,nama_pasien,ruang_bangsal,penjamin,tgl_masuk,jam_masuk,tgl_keluar,jam_keluar," +
                "kd_dokter,nm_dokter,alasan_rawat,diagnosa_masuk,icd10_masuk,diagnosa_keluar,icd10_keluar," +
                "diagnosa_sekunder1,icd10_sekunder1,diagnosa_sekunder2,icd10_sekunder2,diagnosa_sekunder3,icd10_sekunder3," +
                "terapi_tindakan,icd9_cm,penyebab_kematian,pemeriksaan_fisik,laboratorium,radiologi,penunjang_lain,terapi_obat," +
                "instruksi_tindak_lanjut,cara_keluar,dirujuk_ke,keadaan_keluar,tanggal_resume,jam_resume,ttd_dokter,created_by,updated_by,created_at,updated_at" +
                ") values (" +
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,now(),now()" +
                ")"
            );
            isiStatement(stmt, false);
            stmt.executeUpdate();
            tampilData();
            JOptionPane.showMessageDialog(this, "Resume medis ranap V2 berhasil disimpan.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data : " + e.getMessage());
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
        }
    }

    private void updateData() {
        if (!validasiInput()) {
            return;
        }
        if (!dataSudahAda()) {
            JOptionPane.showMessageDialog(this, "Data resume belum ada. Simpan dulu sebagai data baru.");
            return;
        }
        PreparedStatement stmt = null;
        try {
            stmt = koneksi.prepareStatement(
                "update resume_medis_ranap_v2 set " +
                "no_rkm_medis=?,nama_pasien=?,ruang_bangsal=?,penjamin=?,tgl_masuk=?,jam_masuk=?,tgl_keluar=?,jam_keluar=?," +
                "kd_dokter=?,nm_dokter=?,alasan_rawat=?,diagnosa_masuk=?,icd10_masuk=?,diagnosa_keluar=?,icd10_keluar=?," +
                "diagnosa_sekunder1=?,icd10_sekunder1=?,diagnosa_sekunder2=?,icd10_sekunder2=?,diagnosa_sekunder3=?,icd10_sekunder3=?," +
                "terapi_tindakan=?,icd9_cm=?,penyebab_kematian=?,pemeriksaan_fisik=?,laboratorium=?,radiologi=?,penunjang_lain=?,terapi_obat=?," +
                "instruksi_tindak_lanjut=?,cara_keluar=?,dirujuk_ke=?,keadaan_keluar=?,tanggal_resume=?,jam_resume=?,ttd_dokter=?,updated_by=?,updated_at=now() " +
                "where no_rawat=?"
            );
            isiStatement(stmt, true);
            stmt.executeUpdate();
            tampilData();
            JOptionPane.showMessageDialog(this, "Resume medis ranap V2 berhasil diperbarui.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui data : " + e.getMessage());
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
        }
    }

    private void isiStatement(PreparedStatement stmt, boolean updateMode) throws SQLException {
        int idx = 1;
        if (!updateMode) {
            stmt.setString(idx++, ambil(TNoRw));
        }
        stmt.setString(idx++, ambil(TNoRM));
        stmt.setString(idx++, ambil(TPasien));
        stmt.setString(idx++, ambil(TRuang));
        stmt.setString(idx++, ambil(TPenjab));
        setDateString(stmt, idx++, ambil(TTglMasuk));
        stmt.setString(idx++, ambil(TJamMasuk));
        setDateString(stmt, idx++, ambil(TTglKeluar));
        stmt.setString(idx++, ambil(TJamKeluar));
        stmt.setString(idx++, ambil(TKodeDokter));
        stmt.setString(idx++, ambil(TNamaDokter));
        stmt.setString(idx++, ambil(AreaAlasanRawat));
        stmt.setString(idx++, ambil(TDiagnosaMasuk));
        stmt.setString(idx++, ambil(TICD10Masuk));
        stmt.setString(idx++, ambil(TDiagnosaKeluar));
        stmt.setString(idx++, ambil(TICD10Keluar));
        stmt.setString(idx++, ambil(TDiagnosaSekunder1));
        stmt.setString(idx++, ambil(TICD10Sekunder1));
        stmt.setString(idx++, ambil(TDiagnosaSekunder2));
        stmt.setString(idx++, ambil(TICD10Sekunder2));
        stmt.setString(idx++, ambil(TDiagnosaSekunder3));
        stmt.setString(idx++, ambil(TICD10Sekunder3));
        stmt.setString(idx++, ambil(AreaTerapiTindakan));
        stmt.setString(idx++, ambil(TICD9CM));
        stmt.setString(idx++, ambil(AreaPenyebabKematian));
        stmt.setString(idx++, ambil(AreaPemeriksaanFisik));
        stmt.setString(idx++, ambil(AreaLaboratorium));
        stmt.setString(idx++, ambil(AreaRadiologi));
        stmt.setString(idx++, ambil(AreaPenunjangLain));
        stmt.setString(idx++, ambil(AreaTerapiObat));
        stmt.setString(idx++, ambil(AreaInstruksi));
        stmt.setString(idx++, ambilCombo(CmbCaraKeluar));
        stmt.setString(idx++, ambil(TDirujukKe));
        stmt.setString(idx++, ambilCombo(CmbKeadaanKeluar));
        setDate(stmt, idx++, DTPTanggalResume.getDate());
        stmt.setString(idx++, ambil(TJamResume));
        setBlob(stmt, idx++, ttdDokter);
        if (!updateMode) {
            stmt.setString(idx++, akses.getkode());
        }
        stmt.setString(idx++, akses.getkode());
        if (updateMode) {
            stmt.setString(idx++, ambil(TNoRw));
        }
    }

    private void hapusData() {
        String noRawat = ambil(TNoRw);
        if (noRawat.isEmpty() && tbData.getSelectedRow() >= 0) {
            noRawat = tbData.getValueAt(tbData.getSelectedRow(), 0).toString();
        }
        if (noRawat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih data resume yang ingin dihapus.");
            return;
        }
        if (!akses.getkode().equals("Admin Utama") && !akses.getkode().equals(ambil(TKodeDokter))) {
            JOptionPane.showMessageDialog(this, "Hanya dokter yang bersangkutan atau Admin Utama yang bisa menghapus.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus resume untuk no. rawat " + noRawat + " ?", "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        Sequel.meghapus("resume_medis_ranap_v2", "no_rawat", noRawat);
        tampilData();
        bersihkanForm();
    }

    private boolean dataSudahAda() {
        return Sequel.cariInteger("select count(*) from resume_medis_ranap_v2 where no_rawat='" + ambil(TNoRw) + "'") > 0;
    }

    private void bersihkanForm() {
        String noRawat = ambil(TNoRw);
        String noRm = ambil(TNoRM);
        String nama = ambil(TPasien);
        String ruang = ambil(TRuang);
        String penjab = ambil(TPenjab);
        String tglMasuk = ambil(TTglMasuk);
        String jamMasuk = ambil(TJamMasuk);
        String tglKeluar = ambil(TTglKeluar);
        String jamKeluar = ambil(TJamKeluar);

        kosongkanResume();
        TNoRw.setText(noRawat);
        TNoRM.setText(noRm);
        TPasien.setText(nama);
        TRuang.setText(ruang);
        TPenjab.setText(penjab);
        TTglMasuk.setText(tglMasuk);
        TJamMasuk.setText(jamMasuk);
        TTglKeluar.setText(tglKeluar);
        TJamKeluar.setText(jamKeluar);
        isiDefaultDokterLogin();
        TabUtama.setSelectedIndex(0);
    }

    private void kosongkanResume() {
        AreaAlasanRawat.setText("");
        TDiagnosaMasuk.setText("");
        TICD10Masuk.setText("");
        TDiagnosaKeluar.setText("");
        TICD10Keluar.setText("");
        TDiagnosaSekunder1.setText("");
        TICD10Sekunder1.setText("");
        TDiagnosaSekunder2.setText("");
        TICD10Sekunder2.setText("");
        TDiagnosaSekunder3.setText("");
        TICD10Sekunder3.setText("");
        AreaTerapiTindakan.setText("");
        TICD9CM.setText("");
        AreaPenyebabKematian.setText("");
        AreaPemeriksaanFisik.setText("");
        AreaLaboratorium.setText("");
        AreaRadiologi.setText("");
        AreaPenunjangLain.setText("");
        AreaTerapiObat.setText("");
        AreaInstruksi.setText("");
        CmbCaraKeluar.setSelectedIndex(0);
        TDirujukKe.setText("");
        CmbKeadaanKeluar.setSelectedIndex(0);
        DTPTanggalResume.setDate(new Date());
        TJamResume.setText(formatJam.format(new Date()));
        setSignatureData(null);
        sinkronkanCaraKeluar();
    }

    private void bukaCariDokter() {
        dokter.emptTeks();
        dokter.isCek();
        dokter.setSize(Math.max(850, getWidth() - 80), Math.max(550, getHeight() - 120));
        dokter.setLocationRelativeTo(this);
        dokter.setVisible(true);
    }

    private void isiDefaultDokterLogin() {
        if (!ambil(TKodeDokter).isEmpty() || !ambil(TNamaDokter).isEmpty()) {
            return;
        }
        String namaDokter = Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", akses.getkode());
        if (namaDokter != null && !namaDokter.trim().isEmpty()) {
            TKodeDokter.setText(akses.getkode());
            TNamaDokter.setText(namaDokter);
        }
    }

    private void sinkronkanCaraKeluar() {
        String cara = ambilCombo(CmbCaraKeluar);
        boolean butuhRujukan = "Dirujuk Ke".equalsIgnoreCase(cara) || "Pindah Rumah Sakit".equalsIgnoreCase(cara);
        TDirujukKe.setEditable(butuhRujukan);
        TDirujukKe.setBackground(butuhRujukan ? WARNA_FIELD : WARNA_FIELD_READONLY);
        if (!butuhRujukan) {
            TDirujukKe.setText("");
        }
    }

    private void bukaDataTerpilih() {
        if (tbData.getSelectedRow() < 0) {
            return;
        }
        TNoRw.setText(tbData.getValueAt(tbData.getSelectedRow(), 0).toString());
        muatDataJikaAda();
        TabUtama.setSelectedIndex(0);
    }

    private boolean siapCetak() {
        if (ambil(TNoRw).isEmpty() && tbData.getSelectedRow() >= 0) {
            bukaDataTerpilih();
        }
        if (ambil(TNoRw).isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih atau buka dulu data resume yang akan dicetak.");
            return false;
        }
        return true;
    }

    private void previewJasper() {
        if (!siapCetak()) {
            return;
        }
        try {
            siapkanReport("rptResumeMedisRanapV2");
            Valid.MyReportqry(
                "rptResumeMedisRanapV2.jasper",
                "report",
                "::[ Laporan Resume Medis Ranap V2 ]::",
                buatQueryCetak(),
                buatParameterCetak()
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal preview Jasper resume V2 : " + e.getMessage());
        }
    }

    private void cetakPdf() {
        if (!siapCetak()) {
            return;
        }
        try {
            siapkanReport("rptResumeMedisRanapV2");
            Valid.MyReportqrypdf(
                "rptResumeMedisRanapV2.jasper",
                "report",
                "::[ Laporan Resume Medis Ranap V2 ]::",
                buatQueryCetak(),
                buatParameterCetak()
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal cetak PDF resume V2 : " + e.getMessage());
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
        param.put("ttd_dokter", buatImageReport(ttdDokter));
        param.put("finger", buatBarcodeDokter());
        param.put("kota_ttd", akses.getkabupatenrs());
        return param;
    }

    private String buatBarcodeDokter() {
        String kodeDokter = ambil(TKodeDokter);
        String namaDokter = ambil(TNamaDokter);
        String tanggalResume = DTPTanggalResume.getSelectedItem() == null ? "" : DTPTanggalResume.getSelectedItem().toString();
        String finger = Sequel.cariIsi(
            "select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",
            kodeDokter
        );
        finger = finger == null ? "" : finger;
        return "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() +
            "\nDitandatangani secara elektronik oleh " + namaDokter +
            "\nID " + (finger.equals("") ? kodeDokter : finger) +
            "\n" + tanggalResume;
    }

    private String buatQueryCetak() {
        return "select " +
            "m.no_rawat," +
            "m.no_rkm_medis," +
            "m.nama_pasien," +
            "ifnull(date_format(p.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir_format," +
            "if(p.jk='L','L','P') as jk_singkat," +
            "concat(ifnull(p.alamat,'')," +
            "if(kelurahan.nm_kel is null or kelurahan.nm_kel='', '', concat(', ',kelurahan.nm_kel))," +
            "if(kecamatan.nm_kec is null or kecamatan.nm_kec='', '', concat(', ',kecamatan.nm_kec))," +
            "if(kabupaten.nm_kab is null or kabupaten.nm_kab='', '', concat(', ',kabupaten.nm_kab))) as alamat_lengkap," +
            "if(m.tgl_masuk is null,'',concat(date_format(m.tgl_masuk,'%d-%m-%Y'),' ',left(ifnull(m.jam_masuk,''),5))) as tgl_masuk_format," +
            "if(m.tgl_keluar is null,'',concat(date_format(m.tgl_keluar,'%d-%m-%Y'),' ',left(ifnull(m.jam_keluar,''),5))) as tgl_keluar_format," +
            "ifnull(m.alasan_rawat,'') as alasan_rawat," +
            "ifnull(m.diagnosa_masuk,'') as diagnosa_masuk," +
            "ifnull(m.icd10_masuk,'') as icd10_masuk," +
            "ifnull(m.diagnosa_keluar,'') as diagnosa_keluar," +
            "ifnull(m.icd10_keluar,'') as icd10_keluar," +
            "ifnull(m.diagnosa_sekunder1,'') as diagnosa_sekunder1," +
            "ifnull(m.icd10_sekunder1,'') as icd10_sekunder1," +
            "ifnull(m.diagnosa_sekunder2,'') as diagnosa_sekunder2," +
            "ifnull(m.icd10_sekunder2,'') as icd10_sekunder2," +
            "ifnull(m.diagnosa_sekunder3,'') as diagnosa_sekunder3," +
            "ifnull(m.icd10_sekunder3,'') as icd10_sekunder3," +
            "ifnull(m.terapi_tindakan,'') as terapi_tindakan," +
            "ifnull(m.icd9_cm,'') as icd9_cm," +
            "ifnull(m.penyebab_kematian,'') as penyebab_kematian," +
            "ifnull(m.pemeriksaan_fisik,'') as pemeriksaan_fisik," +
            "ifnull(m.laboratorium,'') as laboratorium," +
            "ifnull(m.radiologi,'') as radiologi," +
            "ifnull(m.penunjang_lain,'') as penunjang_lain," +
            "ifnull(m.terapi_obat,'') as terapi_obat," +
            "ifnull(m.instruksi_tindak_lanjut,'') as instruksi_tindak_lanjut," +
            "ifnull(m.cara_keluar,'') as cara_keluar," +
            "ifnull(m.keadaan_keluar,'') as keadaan_keluar," +
            "ifnull(m.dirujuk_ke,'') as dirujuk_ke," +
            "ifnull(date_format(m.tanggal_resume,'%d-%m-%Y'),'') as tanggal_resume_format," +
            "ifnull(date_format(m.tanggal_resume,'%Y'),'') as tahun_resume," +
            "ifnull(m.jam_resume,'') as jam_resume," +
            "ifnull(m.kd_dokter,'') as kd_dokter," +
            "ifnull(m.nm_dokter,'') as nm_dokter," +
            "ifnull((select d.nm_dokter from dpjp_ranap dr inner join dokter d on dr.kd_dokter=d.kd_dokter where dr.no_rawat=m.no_rawat order by dr.kd_dokter limit 0,1)," +
            "ifnull((select d2.nm_dokter from dokter d2 where d2.kd_dokter=m.kd_dokter),'') ) as dpjp," +
            "if((select count(*) from dpjp_ranap drx where drx.no_rawat=m.no_rawat)>0,'ya','tidak') as rawat_tim_dokter," +
            "ifnull((select concat('1. dr. ',d.nm_dokter) from dpjp_ranap dr inner join dokter d on dr.kd_dokter=d.kd_dokter where dr.no_rawat=m.no_rawat order by dr.kd_dokter limit 0,1)," +
            "concat('1. dr. ',ifnull(m.nm_dokter,''))) as tim_dokter1," +
            "ifnull((select concat('2. dr. ',d.nm_dokter) from dpjp_ranap dr inner join dokter d on dr.kd_dokter=d.kd_dokter where dr.no_rawat=m.no_rawat order by dr.kd_dokter limit 1,1),'') as tim_dokter2," +
            "ifnull((select concat('3. dr. ',d.nm_dokter) from dpjp_ranap dr inner join dokter d on dr.kd_dokter=d.kd_dokter where dr.no_rawat=m.no_rawat order by dr.kd_dokter limit 2,1),'') as tim_dokter3 " +
            "from resume_medis_ranap_v2 m " +
            "inner join reg_periksa r on r.no_rawat=m.no_rawat " +
            "inner join pasien p on p.no_rkm_medis=r.no_rkm_medis " +
            "left join kelurahan on p.kd_kel=kelurahan.kd_kel " +
            "left join kecamatan on p.kd_kec=kecamatan.kd_kec " +
            "left join kabupaten on p.kd_kab=kabupaten.kd_kab " +
            "where m.no_rawat='" + ambil(TNoRw) + "'";
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

    private void bukaEditorTtd() {
        SignaturePadDialog dialog = new SignaturePadDialog(this, "Dokter", ttdDokter);
        byte[] hasil = dialog.showDialog();
        if (hasil != null) {
            setSignatureData(hasil);
        }
    }

    private void bukaFileTtd() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Pilih file scan tanda tangan dokter");
        chooser.setFileFilter(new FileNameExtensionFilter("File gambar (*.png, *.jpg, *.jpeg, *.bmp)", "png", "jpg", "jpeg", "bmp"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                byte[] data = bacaFileGambarTtd(chooser.getSelectedFile());
                if (data == null || data.length == 0) {
                    JOptionPane.showMessageDialog(this, "File gambar tanda tangan tidak valid.");
                    return;
                }
                setSignatureData(data);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal membaca file tanda tangan : " + e.getMessage());
            }
        }
    }

    private void bukaPemilihSoapDokter(String kolomTarget) {
        if (ambil(TNoRw).isEmpty()) {
            Valid.textKosong(TNoRw, "Pasien Rawat Inap");
            return;
        }

        final boolean ambilSubjek = "keluhan".equals(kolomTarget);
        final boolean ambilObjek = "pemeriksaan".equals(kolomTarget);
        final String judulKolom = ambilSubjek ? "Subjek" : (ambilObjek ? "Objek" : "Plan");
        final DefaultTableModel modelSoap = new DefaultTableModel(null, new Object[]{
            "Tanggal", "Jam", "Dokter", judulKolom
        }) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        muatSoapDokter(modelSoap, kolomTarget);
        if (modelSoap.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Data SOAP dokter untuk pasien ini belum ditemukan.");
            return;
        }

        final JDialog dialog = new JDialog(this, "Ambil " + judulKolom + " Dokter", true);
        final JTable tabelSoap = new JTable(modelSoap);
        tabelSoap.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelSoap.setRowHeight(54);
        tabelSoap.setFont(FONT_BODY);
        tabelSoap.getTableHeader().setFont(FONT_LABEL);
        tabelSoap.getTableHeader().setBackground(WARNA_ACCENT_SOFT);
        tabelSoap.getTableHeader().setForeground(WARNA_ACCENT);
        tabelSoap.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] lebarKolom = {90, 70, 180, 620};
        for (int idx = 0; idx < lebarKolom.length; idx++) {
            tabelSoap.getColumnModel().getColumn(idx).setPreferredWidth(lebarKolom[idx]);
        }

        JScrollPane scroll = new JScrollPane(tabelSoap);
        aturScrollPane(scroll, false);

        Button btnPilih = buatButton("Pilih");
        Button btnBatal = buatButton("Batal");
        aturTemaButton(btnPilih, new Color(22, 163, 74), Color.WHITE);
        aturTemaButton(btnBatal, new Color(100, 116, 139), Color.WHITE);
        JPanel aksi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        aksi.setOpaque(false);
        aksi.setBorder(new EmptyBorder(10, 0, 0, 0));
        aksi.add(btnPilih);
        aksi.add(btnBatal);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(WARNA_SURFACE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));
        panel.add(labelInfoSoap(kolomTarget), BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(aksi, BorderLayout.SOUTH);

        ActionListener pilihData = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = tabelSoap.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(dialog, "Pilih data SOAP dokter terlebih dahulu.");
                    return;
                }
                int modelRow = tabelSoap.convertRowIndexToModel(row);
                Object nilaiTabel = modelSoap.getValueAt(modelRow, 3);
                String nilai = nilaiTabel == null ? "" : nilaiTabel.toString();
                if (ambilSubjek) {
                    AreaAlasanRawat.setText(nilai);
                } else if (ambilObjek) {
                    AreaPemeriksaanFisik.setText(nilai);
                } else {
                    AreaTerapiObat.setText(nilai);
                }
                dialog.dispose();
            }
        };
        btnPilih.addActionListener(pilihData);
        btnBatal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        tabelSoap.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    pilihData.actionPerformed(null);
                }
            }
        });

        dialog.setContentPane(panel);
        dialog.setSize(980, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JLabel labelInfoSoap(String kolomTarget) {
        String info = "Pilih SOAP dokter.";
        if ("keluhan".equals(kolomTarget)) {
            info = "Pilih SOAP dokter. Kolom Subjek akan dimasukkan ke Alasan / Indikasi Dirawat.";
        } else if ("pemeriksaan".equals(kolomTarget)) {
            info = "Pilih SOAP dokter. Kolom Objek akan dimasukkan ke Pemeriksaan Fisik.";
        } else if ("rtl".equals(kolomTarget)) {
            info = "Pilih SOAP dokter. Kolom Plan akan dimasukkan ke Terapi / Obat Yang Diberikan.";
        }
        JLabel label = new JLabel(info);
        label.setFont(FONT_LABEL);
        label.setForeground(WARNA_TEXT);
        return label;
    }

    private void muatSoapDokter(DefaultTableModel modelSoap, String kolomWajib) {
        PreparedStatement stmt = null;
        ResultSet hasil = null;
        try {
            String kolomData = "keluhan";
            if ("pemeriksaan".equals(kolomWajib)) {
                kolomData = "pemeriksaan";
            } else if ("rtl".equals(kolomWajib)) {
                kolomData = "rtl";
            }
            stmt = koneksi.prepareStatement(
                "select pemeriksaan_ranap.tgl_perawatan,pemeriksaan_ranap.jam_rawat," +
                "ifnull(pemeriksaan_ranap." + kolomData + ",'') as data_soap," +
                "pemeriksaan_ranap.nip,pegawai.nama,ifnull(pegawai.jbtn,'') as jbtn " +
                "from pemeriksaan_ranap inner join pegawai on pemeriksaan_ranap.nip=pegawai.nik " +
                "where pemeriksaan_ranap.no_rawat=? " +
                "and (pegawai.jbtn like ? or pegawai.nama like ? or pegawai.nama like ? or pegawai.nama like ?) " +
                "and ifnull(pemeriksaan_ranap." + kolomData + ",'')<>'' " +
                "order by pemeriksaan_ranap.tgl_perawatan desc,pemeriksaan_ranap.jam_rawat desc"
            );
            stmt.setString(1, ambil(TNoRw));
            stmt.setString(2, "%Dokter%");
            stmt.setString(3, "dr.%");
            stmt.setString(4, "dr %");
            stmt.setString(5, "drg%");
            hasil = stmt.executeQuery();
            while (hasil.next()) {
                modelSoap.addRow(new Object[]{
                    hasil.getString("tgl_perawatan"),
                    hasil.getString("jam_rawat"),
                    hasil.getString("nama"),
                    hasil.getString("data_soap")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat SOAP dokter : " + e.getMessage());
        } finally {
            try {
                if (hasil != null) {
                    hasil.close();
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
        }
    }

    private byte[] bacaFileGambarTtd(File file) throws IOException {
        BufferedImage sumber = ImageIO.read(file);
        if (sumber == null) {
            return null;
        }
        int lebarMaks = 600;
        int tinggiMaks = 240;
        double skala = Math.min((double) lebarMaks / sumber.getWidth(), (double) tinggiMaks / sumber.getHeight());
        skala = Math.min(1.0, skala);
        int lebar = Math.max(1, (int) Math.round(sumber.getWidth() * skala));
        int tinggi = Math.max(1, (int) Math.round(sumber.getHeight() * skala));
        BufferedImage hasil = new BufferedImage(lebar, tinggi, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = hasil.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(sumber, 0, 0, lebar, tinggi, null);
        g2.dispose();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(hasil, "png", output);
        return output.toByteArray();
    }

    private void setSignatureData(byte[] data) {
        ttdDokter = data;
        updateSignatureLabel(LblTtdDokter, data);
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

    private void setDate(PreparedStatement statement, int index, Date value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DATE);
        } else {
            statement.setDate(index, new java.sql.Date(value.getTime()));
        }
    }

    private void setDateString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            statement.setNull(index, Types.DATE);
        } else {
            statement.setDate(index, java.sql.Date.valueOf(value));
        }
    }

    private boolean validasiTanggalJamKeluar() {
        String tanggal = normalisasiTanggal(TTglKeluar.getText());
        if (tanggal.equals("")) {
            JOptionPane.showMessageDialog(this, "Format tanggal keluar tidak valid. Gunakan yyyy-MM-dd atau dd-MM-yyyy.");
            TTglKeluar.requestFocus();
            return false;
        }
        String jam = normalisasiJam(TJamKeluar.getText());
        if (jam.equals("")) {
            JOptionPane.showMessageDialog(this, "Format jam keluar tidak valid. Gunakan HH:mm atau HH:mm:ss.");
            TJamKeluar.requestFocus();
            return false;
        }
        TTglKeluar.setText(tanggal);
        TJamKeluar.setText(jam);
        return true;
    }

    private boolean validasiTanggalJamMasuk() {
        String tanggal = normalisasiTanggal(TTglMasuk.getText());
        if (tanggal.equals("")) {
            JOptionPane.showMessageDialog(this, "Format tanggal masuk tidak valid. Gunakan yyyy-MM-dd atau dd-MM-yyyy.");
            TTglMasuk.requestFocus();
            return false;
        }
        String jam = normalisasiJam(TJamMasuk.getText());
        if (jam.equals("")) {
            JOptionPane.showMessageDialog(this, "Format jam masuk tidak valid. Gunakan HH:mm atau HH:mm:ss.");
            TJamMasuk.requestFocus();
            return false;
        }
        TTglMasuk.setText(tanggal);
        TJamMasuk.setText(jam);
        return true;
    }

    private String normalisasiTanggal(String input) {
        if (input == null || input.trim().equals("")) {
            return "";
        }
        input = input.trim().replace("/", "-");
        try {
            if (input.matches("\\d{4}-\\d{2}-\\d{2}")) {
                java.sql.Date.valueOf(input);
                return input;
            }
            if (input.matches("\\d{2}-\\d{2}-\\d{4}")) {
                String tanggal = Valid.SetTgl(input);
                java.sql.Date.valueOf(tanggal);
                return tanggal;
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }

    private String normalisasiJam(String input) {
        if (input == null || input.trim().equals("")) {
            return "";
        }
        input = input.trim().replace(".", ":");
        try {
            if (input.matches("\\d{2}:\\d{2}")) {
                java.sql.Time.valueOf(input + ":00");
                return input + ":00";
            }
            if (input.matches("\\d{2}:\\d{2}:\\d{2}")) {
                java.sql.Time.valueOf(input);
                return input;
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }

    private void setBlob(PreparedStatement statement, int index, byte[] data) throws SQLException {
        if (data == null || data.length == 0) {
            statement.setNull(index, Types.LONGVARBINARY);
        } else {
            statement.setBytes(index, data);
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
        panel.add(labelKiri(label1), gbc);

        gbc = dasarGbc();
        gbc.gridy = row;
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        panel.add(bungkusKomponen(comp1), gbc);

        gbc = dasarGbc();
        gbc.gridy = row;
        gbc.gridx = 2;
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

    private int tambahAreaDenganTombol(JPanel panel, int row, String label, JTextArea area, int tinggi, Button button) {
        return tambahSatuKolom(panel, row, label, flowPanelPenuh(scrollArea(area, tinggi), button));
    }

    private JLabel labelKiri(String text) {
        JLabel label = new JLabel(formatLabelWajib(text));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setForeground(WARNA_MUTED);
        label.setFont(FONT_LABEL);
        return label;
    }

    private String formatLabelWajib(String text) {
        if (text != null && text.trim().endsWith("*")) {
            String label = text.substring(0, text.lastIndexOf("*")).trim();
            return "<html>" + label + " <font color='#dc2626'>*</font></html>";
        }
        return text;
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

    private JPanel flowPanelPenuh(Component utama, Component tombol) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        panel.add(utama, BorderLayout.CENTER);
        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelTombol.setOpaque(false);
        panelTombol.add(tombol);
        panel.add(panelTombol, BorderLayout.EAST);
        return panel;
    }

    private JScrollPane scrollArea(JTextArea area, int tinggi) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(100, tinggi));
        aturScrollPane(scroll, false);
        area.addMouseWheelListener(evt -> teruskanScrollKeFormJikaPerlu(scroll, evt));
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

    private javax.swing.JComboBox<String> buatCombo(String... values) {
        javax.swing.JComboBox<String> combo = new javax.swing.JComboBox<String>(new DefaultComboBoxModel<String>(values));
        combo.setFont(FONT_BODY);
        combo.setBackground(WARNA_FIELD);
        combo.setForeground(WARNA_TEXT);
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARNA_BORDER),
            new EmptyBorder(2, 6, 2, 6)
        ));
        return combo;
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
        button.setForeground(Color.WHITE);
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
        button.setPreferredSize(new Dimension(82, 28));
        return button;
    }

    private JPanel buatPanelHero() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(true);
        panel.setBackground(WARNA_SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARNA_BORDER),
            new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel judul = new JLabel("Resume Medis Ranap V2");
        judul.setFont(FONT_TITLE);
        judul.setForeground(WARNA_TEXT);

        JLabel subjudul = new JLabel("<html>Versi modern untuk resume rawat inap, dokter otomatis dari login aktif, dan validasi tanda tangan langsung dari canvas gambar.</html>");
        subjudul.setFont(FONT_BODY);
        subjudul.setForeground(WARNA_MUTED);

        JPanel teks = new JPanel(new BorderLayout(0, 4));
        teks.setOpaque(false);
        teks.add(judul, BorderLayout.NORTH);
        teks.add(subjudul, BorderLayout.CENTER);

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        badgePanel.setOpaque(false);
        badgePanel.add(buatBadge("Ranap Workflow", WARNA_ACCENT_SOFT, WARNA_ACCENT));
        badgePanel.add(buatBadge("Auto Dokter", new Color(219, 234, 254), new Color(30, 64, 175)));
        badgePanel.add(buatBadge("TTD Gambar", new Color(254, 242, 242), new Color(153, 27, 27)));

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
        scroll.getVerticalScrollBar().setUnitIncrement(gunakanLatarAplikasi ? 24 : 18);
        scroll.getVerticalScrollBar().setBlockIncrement(gunakanLatarAplikasi ? 180 : 120);
        scroll.getHorizontalScrollBar().setUnitIncrement(18);
        scroll.getHorizontalScrollBar().setBlockIncrement(120);
    }

    private void teruskanScrollKeFormJikaPerlu(JScrollPane sumber, MouseWheelEvent evt) {
        if (ScrollFormUtama == null || sumber == ScrollFormUtama) {
            return;
        }

        JScrollBar barSumber = sumber.getVerticalScrollBar();
        int arah = evt.getWheelRotation();
        boolean bisaScrollSendiri = barSumber.isVisible() && barSumber.getMaximum() > barSumber.getVisibleAmount();
        boolean mentokAtas = barSumber.getValue() <= barSumber.getMinimum() && arah < 0;
        boolean mentokBawah = barSumber.getValue() + barSumber.getVisibleAmount() >= barSumber.getMaximum() && arah > 0;

        if (!bisaScrollSendiri || mentokAtas || mentokBawah) {
            geserScrollFormUtama(evt);
            evt.consume();
        }
    }

    private void geserScrollFormUtama(MouseWheelEvent evt) {
        JScrollBar barUtama = ScrollFormUtama.getVerticalScrollBar();
        int langkah = Math.max(Math.abs(evt.getUnitsToScroll()) * barUtama.getUnitIncrement(), barUtama.getUnitIncrement());
        int nilaiBaru = barUtama.getValue() + (evt.getWheelRotation() < 0 ? -langkah : langkah);
        int nilaiMaksimum = barUtama.getMaximum() - barUtama.getVisibleAmount();
        barUtama.setValue(Math.max(barUtama.getMinimum(), Math.min(nilaiBaru, nilaiMaksimum)));
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
        aturTemaButton(BtnDokter, new Color(37, 99, 235), Color.WHITE);
        aturTemaButton(BtnAmbilAlasanSoap, new Color(37, 99, 235), Color.WHITE);
        aturTemaButton(BtnAmbilPemeriksaanSoap, new Color(37, 99, 235), Color.WHITE);
        aturTemaButton(BtnAmbilTerapiObatSoap, new Color(37, 99, 235), Color.WHITE);
        aturTemaButton(BtnInputTtd, new Color(124, 58, 237), Color.WHITE);
        aturTemaButton(BtnUploadTtd, new Color(14, 116, 144), Color.WHITE);
        aturTemaButton(BtnHapusTtd, new Color(100, 116, 139), Color.WHITE);

        aturTemaTanggal(DTPCari1);
        aturTemaTanggal(DTPCari2);
        aturTemaTanggal(DTPTanggalResume);

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

    private String ambil(JTextComponent component) {
        return component.getText().trim();
    }

    private String ambilCombo(javax.swing.JComboBox<String> combo) {
        Object selected = combo.getSelectedItem();
        return selected == null ? "" : selected.toString();
    }

    private String nvl(String nilai) {
        return nilai == null ? "" : nilai;
    }

    private String nvlCombo(String nilai, javax.swing.JComboBox<String> combo) {
        if (nilai == null || nilai.trim().isEmpty()) {
            return combo.getItemAt(0);
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (nilai.equals(combo.getItemAt(i))) {
                return nilai;
            }
        }
        return combo.getItemAt(0);
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
