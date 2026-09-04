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
import java.awt.Image;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import kepegawaian.DlgCariPetugas;
import simrskhanza.DlgCariBangsal;

/**
 * Lembar Transfer Pasien Internal (RM 38). Menggantikan "Pengantar Pasien
 * Rawat Inap (RM 3a)" (rekammedis.RMPengantarPasienRanap, TIDAK dihapus,
 * hanya tidak dipakai lagi) di menu "Penilaian Awal" DlgRawatInap/DlgIGD/
 * DlgRawatJalan. Identitas, ruangan asal, tanggal masuk RS, indikasi dirawat,
 * dan kondisi vital SEBELUM transfer ditarik otomatis dari data yang sudah
 * ada; sisanya (tujuan, kategori transfer, checklist barang, kondisi
 * SETELAH transfer, dsb) diisi manual saat itu juga.
 */
public final class RMTransferPasienInternal extends JDialog {

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final Map<String, ImageIcon> cacheFotoTtd = new HashMap<>();

    // Header identitas (readonly)
    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TJK = ro();
    private final widget.TextBox TTglLahir = ro();

    // 1. Info Transfer
    private final widget.TextBox tRuanganAsal = ro();
    private final widget.TextBox tRuanganTujuan = tf();
    private final widget.TextBox tPetugasTujuanDihubungi = tf();
    private final widget.Button btnPilihRuanganTujuan = new widget.Button();
    private final widget.Button btnPilihPetugasTujuan = new widget.Button();
    private final DlgCariBangsal pickerRuanganTujuan = new DlgCariBangsal(null, false);
    private final DlgCariPetugas pickerPetugasTujuan = new DlgCariPetugas(null, false);
    private final widget.Tanggal dtpDihubungi = dt();
    private final widget.Tanggal dtpTransfer = dt();
    private final widget.ComboBox cmbKategori = cmb("-", "Level 0", "Level 1", "Level 2", "Level 3");
    private final widget.ComboBox cmbPendamping = cmb("-", "Perawat", "Bidan");
    private final JCheckBox ckBtcls = new JCheckBox("BT-CLS");
    private final JCheckBox ckPpgd = new JCheckBox("PPGD");
    private final JCheckBox ckApn = new JCheckBox("APN");
    private final widget.Tanggal dtpMasukRs = dt();

    // 2. Kondisi Klinis
    private final widget.TextArea taAnamnesa = ta();
    private final widget.TextArea taIndikasiDirawat = ta();
    private final widget.TextArea taTindakan = ta();
    private final widget.TextArea taTerapi = ta();
    private final widget.ComboBox cmbTransportasi = cmb("-", "Kursi Roda", "Brankar");

    // 3. Dokumen & Barang yang Disertakan
    private final JCheckBox ckRmPasien = new JCheckBox("RM Pasien");
    private final JCheckBox ckObatOral = new JCheckBox("Obat oral yang diminum");
    private final JCheckBox ckObatInjeksi = new JCheckBox("Obat injeksi");
    private final JCheckBox ckObatDibawa = new JCheckBox("Obat pasien yang dibawa");
    private final JCheckBox ckHasilLab = new JCheckBox("Hasil laboratorium");
    private final JCheckBox ckHasilUsg = new JCheckBox("Hasil USG");
    private final JCheckBox ckHasilRontgen = new JCheckBox("Hasil Rontgen");
    private final JCheckBox ckDompet = new JCheckBox("Dompet");
    private final JCheckBox ckHp = new JCheckBox("HP");
    private final JCheckBox ckBarangLainnya = new JCheckBox("Barang lainnya");
    private final widget.TextBox tBarangLainnyaKet = tf();

    // 4. Ringkasan Kondisi & TTD
    private final widget.TextBox tKuSebelum = tf();
    private final widget.TextBox tTdSebelum = tf();
    private final widget.TextBox tNadiSebelum = tf();
    private final widget.TextBox tRrSebelum = tf();
    private final widget.TextBox tSuhuSebelum = tf();
    private final widget.TextBox tSpo2Sebelum = tf();
    private final widget.TextBox tPemFisikSebelum = tf();
    private final widget.TextBox tCatatanSebelum = tf();
    private final widget.TextBox tKuSetelah = tf();
    private final widget.TextBox tTdSetelah = tf();
    private final widget.TextBox tNadiSetelah = tf();
    private final widget.TextBox tRrSetelah = tf();
    private final widget.TextBox tSuhuSetelah = tf();
    private final widget.TextBox tSpo2Setelah = tf();
    private final widget.TextBox tPemFisikSetelah = tf();
    private final widget.TextBox tCatatanSetelah = tf();
    private final widget.TextBox tNamaMenyerahkan = ro();
    private final JLabel lblFotoMenyerahkan = new JLabel();
    private final widget.TextBox tNamaMenerima = tf();
    private String nipMenyerahkan = "";

    private final widget.Button BtnBaru = new widget.Button();
    private final widget.Button BtnSimpan = new widget.Button();
    private final widget.Button BtnHapus = new widget.Button();
    private final widget.Button BtnCetak = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();

    public RMTransferPasienInternal(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Lembar Transfer Pasien Internal (RM 38) ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        ensureTable();
        initComponents();
        siapkanPicker();
        setSize(1180, 780);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(parent);
    }

    /** Tombol "..." di samping Ruangan Tujuan & Petugas Ruangan Tujuan -- buka picker non-modal, ambil hasil pilihan saat ditutup. */
    private void siapkanPicker() {
        btnPilihRuanganTujuan.setText("...");
        btnPilihRuanganTujuan.setPreferredSize(new Dimension(32, 25));
        btnPilihRuanganTujuan.addActionListener(e -> {
            pickerRuanganTujuan.isCek();
            pickerRuanganTujuan.setSize(650, 400);
            pickerRuanganTujuan.setLocationRelativeTo(this);
            pickerRuanganTujuan.setVisible(true);
        });
        pickerRuanganTujuan.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (pickerRuanganTujuan.getTable().getSelectedRow() != -1) {
                    tRuanganTujuan.setText(pickerRuanganTujuan.getTable()
                            .getValueAt(pickerRuanganTujuan.getTable().getSelectedRow(), 1).toString());
                }
            }
        });

        btnPilihPetugasTujuan.setText("...");
        btnPilihPetugasTujuan.setPreferredSize(new Dimension(32, 25));
        btnPilihPetugasTujuan.addActionListener(e -> {
            pickerPetugasTujuan.isCek();
            pickerPetugasTujuan.setSize(650, 400);
            pickerPetugasTujuan.setLocationRelativeTo(this);
            pickerPetugasTujuan.setVisible(true);
        });
        pickerPetugasTujuan.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (pickerPetugasTujuan.getTable().getSelectedRow() != -1) {
                    tPetugasTujuanDihubungi.setText(pickerPetugasTujuan.getTable()
                            .getValueAt(pickerPetugasTujuan.getTable().getSelectedRow(), 1).toString());
                }
            }
        });
    }

    /** Bungkus field teks + tombol picker "..." di sebelah kanan. */
    private JPanel bungkusPicker(Component field, Component tombol) {
        JPanel p = new JPanel(new BorderLayout(4, 0));
        p.setOpaque(false);
        p.add(field, BorderLayout.CENTER);
        p.add(tombol, BorderLayout.EAST);
        return p;
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
        JLabel judulUtama = new JLabel("Lembar Transfer Pasien Internal");
        judulUtama.setFont(new Font("Tahoma", Font.BOLD, 20));
        judulUtama.setForeground(teks);
        JLabel subjudul = new JLabel("Form RM 38  •  Diisi petugas saat pasien dipindahkan antar ruangan");
        subjudul.setFont(new Font("Tahoma", Font.PLAIN, 12));
        subjudul.setForeground(new Color(92, 107, 119));
        blokJudul.add(judulUtama);
        blokJudul.add(Box.createVerticalStrut(3));
        blokJudul.add(subjudul);
        barisJudul.add(blokJudul, BorderLayout.WEST);

        JLabel statusOtomatis = new JLabel("  Sebagian data ditarik otomatis  ");
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

        // ---------- 1. Info Transfer ----------
        JPanel infoTransfer = halaman("1. Info Transfer", utama, latar);
        JPanel kartuRuangan = kartu("Ruangan & Petugas", teks, garis);
        int row = 0;
        row = pasanganVertikal(kartuRuangan, row, "Ruangan Asal", tRuanganAsal,
                "Ruangan Tujuan", bungkusPicker(tRuanganTujuan, btnPilihRuanganTujuan));
        row = pasanganVertikal(kartuRuangan, row, "Petugas Ruangan Tujuan Yang Dihubungi",
                bungkusPicker(tPetugasTujuanDihubungi, btnPilihPetugasTujuan),
                "Tanggal / Jam Dihubungi", dtpDihubungi);
        row = pasanganVertikal(kartuRuangan, row, "Tanggal / Jam Transfer", dtpTransfer,
                "Kategori Pasien Transfer", cmbKategori);
        row = pasanganVertikal(kartuRuangan, row, "Petugas Pendamping", cmbPendamping,
                "Tanggal Masuk RS / Jam", dtpMasukRs);
        infoTransfer.add(kartuRuangan);
        infoTransfer.add(Box.createVerticalStrut(10));

        JPanel kartuKualifikasi = kartu("Kualifikasi Petugas Pendamping", teks, garis);
        JPanel panelKualifikasi = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        panelKualifikasi.setOpaque(false);
        for (JCheckBox ck : new JCheckBox[]{ckBtcls, ckPpgd, ckApn}) {
            ck.setOpaque(false);
            ck.setFont(new Font("Tahoma", Font.PLAIN, 11));
            panelKualifikasi.add(ck);
        }
        GridBagConstraints gKual = gc(0, 1, 4, 1.0);
        gKual.insets = new Insets(1, 4, 8, 4);
        kartuKualifikasi.add(panelKualifikasi, gKual);
        infoTransfer.add(kartuKualifikasi);
        infoTransfer.add(Box.createVerticalGlue());

        // ---------- 2. Kondisi Klinis ----------
        JPanel klinisPage = halaman("2. Kondisi Klinis", utama, latar);
        JPanel kartuKlinis2 = kartu("Anamnesa & Tindakan", teks, garis);
        row = 0;
        row = areaVertikal(kartuKlinis2, row, "Anamnesa", taAnamnesa);
        row = areaVertikal(kartuKlinis2, row, "Indikasi Dirawat", taIndikasiDirawat);
        row = areaVertikal(kartuKlinis2, row, "Tindakan Yang Telah Dilakukan", taTindakan);
        row = areaVertikal(kartuKlinis2, row, "Terapi Yang Telah Diberikan", taTerapi);
        row = tunggalVertikal(kartuKlinis2, row, "Transportasi Yang Digunakan", cmbTransportasi);
        klinisPage.add(kartuKlinis2);
        klinisPage.add(Box.createVerticalGlue());

        // ---------- 3. Dokumen & Barang ----------
        JPanel dokumenPage = halaman("3. Dokumen & Barang Yang Disertakan", utama, latar);
        JPanel kartuDokumen = kartu("Beri Tanda Yang Disertakan", teks, garis);
        JPanel panelChecklist = new JPanel(new GridLayout(4, 3, 10, 6));
        panelChecklist.setOpaque(false);
        for (JCheckBox ck : new JCheckBox[]{ckRmPasien, ckObatOral, ckObatInjeksi, ckObatDibawa, ckHasilLab,
            ckHasilUsg, ckHasilRontgen, ckDompet, ckHp, ckBarangLainnya}) {
            ck.setOpaque(false);
            ck.setFont(new Font("Tahoma", Font.PLAIN, 11));
            panelChecklist.add(ck);
        }
        panelChecklist.add(new JLabel());
        GridBagConstraints gChk = gc(0, 1, 4, 1.0);
        gChk.insets = new Insets(1, 4, 8, 4);
        kartuDokumen.add(panelChecklist, gChk);
        row = 1;
        row = tunggalVertikal(kartuDokumen, row, "Keterangan Barang Lainnya", tBarangLainnyaKet);
        dokumenPage.add(kartuDokumen);
        dokumenPage.add(Box.createVerticalGlue());

        // ---------- 4. Ringkasan Kondisi & TTD ----------
        JPanel ringkasanPage = halaman("4. Ringkasan Kondisi & Tanda Tangan", utama, latar);
        JPanel kartuRingkasan = kartu("Ringkasan Kondisi Pasien", teks, garis);
        GridBagConstraints gRingkasan = gc(0, 1, 4, 1.0);
        gRingkasan.insets = new Insets(1, 4, 10, 4);
        kartuRingkasan.add(panelRingkasanKondisi(teks, garis), gRingkasan);
        ringkasanPage.add(kartuRingkasan);
        ringkasanPage.add(Box.createVerticalStrut(10));

        JPanel kartuTtd = kartu("Nama Dan Tanda Tangan Petugas", teks, garis);
        row = 0;
        row = pasanganVertikal(kartuTtd, row, "Yang Menyerahkan",
                bungkusFotoTtd(tNamaMenyerahkan, lblFotoMenyerahkan), "Yang Menerima", tNamaMenerima);
        ringkasanPage.add(kartuTtd);
        ringkasanPage.add(Box.createVerticalGlue());

        isiHalaman.add(bungkusScroll(infoTransfer), "INFO");
        isiHalaman.add(bungkusScroll(klinisPage), "KLINIS");
        isiHalaman.add(bungkusScroll(dokumenPage), "DOKUMEN");
        isiHalaman.add(bungkusScroll(ringkasanPage), "RINGKASAN");

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

        String[] namaMenu = {"1  Info Transfer", "2  Kondisi Klinis", "3  Dokumen & Barang", "4  Ringkasan & TTD"};
        String[] kunciMenu = {"INFO", "KLINIS", "DOKUMEN", "RINGKASAN"};
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
        JLabel infoNav = new JLabel("<html>Field abu-abu berasal dari data yang sudah ada</html>");
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
        bawah.add(BtnKeluar);
        bawah.add(BtnCetak);
        bawah.add(BtnSimpan);
        getContentPane().add(bawah, BorderLayout.SOUTH);
    }

    /** Grid kecil "Ringkasan Kondisi Pasien" -- 2 baris (Sebelum/Setelah Transfer) x 8 kolom. */
    private JPanel panelRingkasanKondisi(Color teks, Color garis) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        String[] kolom = {"", "KU", "T/D (mmHg)", "Nadi (x/mnt)", "RR (x/mnt)", "Suhu (°C)", "Spo2", "Pem.Fisik", "Catatan"};
        for (int c = 0; c < kolom.length; c++) {
            JLabel l = new JLabel(kolom[c]);
            l.setFont(new Font("Tahoma", Font.BOLD, 10));
            l.setForeground(teks);
            GridBagConstraints g = gc(c, 0, 1, c == 0 ? 0.0 : 1.0);
            p.add(l, g);
        }
        widget.TextBox[] baris1 = {tKuSebelum, tTdSebelum, tNadiSebelum, tRrSebelum, tSuhuSebelum, tSpo2Sebelum, tPemFisikSebelum, tCatatanSebelum};
        widget.TextBox[] baris2 = {tKuSetelah, tTdSetelah, tNadiSetelah, tRrSetelah, tSuhuSetelah, tSpo2Setelah, tPemFisikSetelah, tCatatanSetelah};
        p.add(labelSamping("Sebelum Transfer", teks), gc(0, 1, 1, 0.0));
        for (int c = 0; c < baris1.length; c++) {
            baris1[c].setPreferredSize(new Dimension(90, 26));
            GridBagConstraints g = gc(c + 1, 1, 1, 1.0);
            g.insets = new Insets(2, 3, 2, 3);
            p.add(baris1[c], g);
        }
        p.add(labelSamping("Setelah Transfer", teks), gc(0, 2, 1, 0.0));
        for (int c = 0; c < baris2.length; c++) {
            baris2[c].setPreferredSize(new Dimension(90, 26));
            GridBagConstraints g = gc(c + 1, 2, 1, 1.0);
            g.insets = new Insets(2, 3, 2, 3);
            p.add(baris2[c], g);
        }
        return p;
    }

    private JLabel labelSamping(String teksLabel, Color teks) {
        JLabel l = new JLabel(teksLabel);
        l.setFont(new Font("Tahoma", Font.BOLD, 10));
        l.setForeground(teks);
        return l;
    }

    public void isCek() {
        // Boleh Simpan kalau punya izin perawat (penilaian_awal_keperawatan_ranap) ATAU izin
        // dokter (booking_operasi) -- dokter juga perlu bisa mengisi asesmen ini di lapangan.
        boolean bisa = akses.getpenilaian_awal_keperawatan_ranap() || akses.getbooking_operasi();
        BtnSimpan.setEnabled(bisa);
        BtnHapus.setEnabled(bisa);
    }

    public void emptTeks() {
        for (widget.TextBox t : new widget.TextBox[]{TNoRw, TNoRM, TPasien, TJK, TTglLahir, tRuanganAsal, tRuanganTujuan,
            tPetugasTujuanDihubungi, tBarangLainnyaKet, tKuSebelum, tTdSebelum, tNadiSebelum, tRrSebelum, tSuhuSebelum,
            tSpo2Sebelum, tPemFisikSebelum, tCatatanSebelum, tKuSetelah, tTdSetelah, tNadiSetelah, tRrSetelah, tSuhuSetelah,
            tSpo2Setelah, tPemFisikSetelah, tCatatanSetelah, tNamaMenyerahkan, tNamaMenerima}) {
            t.setText("");
        }
        for (widget.TextArea a : new widget.TextArea[]{taAnamnesa, taIndikasiDirawat, taTindakan, taTerapi}) {
            a.setText("");
        }
        for (widget.ComboBox c : new widget.ComboBox[]{cmbKategori, cmbPendamping, cmbTransportasi}) {
            c.setSelectedIndex(0);
        }
        for (JCheckBox ck : new JCheckBox[]{ckBtcls, ckPpgd, ckApn, ckRmPasien, ckObatOral, ckObatInjeksi, ckObatDibawa,
            ckHasilLab, ckHasilUsg, ckHasilRontgen, ckDompet, ckHp, ckBarangLainnya}) {
            ck.setSelected(false);
        }
        dtpDihubungi.setDate(new Date());
        dtpTransfer.setDate(new Date());
        dtpMasukRs.setDate(new Date());
        lblFotoMenyerahkan.setIcon(null);
        nipMenyerahkan = "";
    }

    /** Dipanggil dari menu "Penilaian Awal". Tarik data yang sudah ada lalu timpa dengan data tersimpan bila ada. */
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
                + "ifnull(date_format(rp.tgl_registrasi,'%d-%m-%Y'),'') as tgl_masuk,ifnull(rp.jam_reg,'') as jam_masuk "
                + "from reg_periksa rp inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis "
                + "where rp.no_rawat=?")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    TJK.setText("L".equalsIgnoreCase(rs.getString("jk")) ? "Laki-Laki" : "Perempuan");
                    TTglLahir.setText(rs.getString("tgl_lahir"));
                    String tglMasuk = rs.getString("tgl_masuk");
                    String jamMasuk = rs.getString("jam_masuk");
                    if (!tglMasuk.equals("")) {
                        try {
                            dtpMasukRs.setDate(new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                                    .parse(tglMasuk + " " + (jamMasuk.equals("") ? "00:00:00" : jamMasuk)));
                        } catch (Exception ignore) { }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik data pasien transfer internal : " + e);
        }

        try (PreparedStatement ps = koneksi.prepareStatement(
                "select ifnull(bangsal.nm_bangsal,'') as ruang,ifnull(kamar_inap.diagnosa_awal,'') as diagnosa_awal "
                + "from kamar_inap inner join kamar on kamar.kd_kamar=kamar_inap.kd_kamar "
                + "inner join bangsal on bangsal.kd_bangsal=kamar.kd_bangsal "
                + "where kamar_inap.no_rawat=? and kamar_inap.stts_pulang='-' "
                + "order by kamar_inap.tgl_masuk desc,kamar_inap.jam_masuk desc limit 1")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tRuanganAsal.setText(rs.getString("ruang"));
                    taIndikasiDirawat.setText(rs.getString("diagnosa_awal"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik kamar transfer internal : " + e);
        }

        try (PreparedStatement ps = koneksi.prepareStatement(
                "select kesadaran,tensi,nadi,respirasi,suhu_tubuh,spo2,pemeriksaan,keluhan "
                + "from pemeriksaan_ranap where no_rawat=? order by tgl_perawatan desc,jam_rawat desc limit 1")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tKuSebelum.setText(nvl(rs.getString("kesadaran")));
                    tTdSebelum.setText(nvl(rs.getString("tensi")));
                    tNadiSebelum.setText(nvl(rs.getString("nadi")));
                    tRrSebelum.setText(nvl(rs.getString("respirasi")));
                    tSuhuSebelum.setText(nvl(rs.getString("suhu_tubuh")));
                    tSpo2Sebelum.setText(nvl(rs.getString("spo2")));
                    tPemFisikSebelum.setText(nvl(rs.getString("pemeriksaan")));
                    tCatatanSebelum.setText(nvl(rs.getString("keluhan")));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik vital transfer internal : " + e);
        }

        dtpDihubungi.setDate(new Date());
        dtpTransfer.setDate(new Date());
        nipMenyerahkan = akses.getkode();
        tNamaMenyerahkan.setText(Sequel.cariIsi("select nama from petugas where nip=?", nipMenyerahkan));
        lblFotoMenyerahkan.setIcon(ambilFotoTtd(nipMenyerahkan));
    }

    private void muatDataJikaAda(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select * from lembar_transfer_pasien_internal where no_rawat=?")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (!nvl(rs.getString("ruangan_asal")).equals("")) {
                        tRuanganAsal.setText(rs.getString("ruangan_asal"));
                    }
                    tRuanganTujuan.setText(nvl(rs.getString("ruangan_tujuan")));
                    tPetugasTujuanDihubungi.setText(nvl(rs.getString("petugas_tujuan_dihubungi")));
                    isiTanggalJam(dtpDihubungi, rs.getDate("tanggal_dihubungi"), rs.getString("jam_dihubungi"));
                    isiTanggalJam(dtpTransfer, rs.getDate("tanggal_transfer"), rs.getString("jam_transfer"));
                    cmbKategori.setSelectedItem(cocokkanOpsi(cmbKategori, rs.getString("kategori_transfer")));
                    cmbPendamping.setSelectedItem(cocokkanOpsi(cmbPendamping, rs.getString("petugas_pendamping")));
                    ckBtcls.setSelected("1".equals(rs.getString("kualifikasi_btcls")));
                    ckPpgd.setSelected("1".equals(rs.getString("kualifikasi_ppgd")));
                    ckApn.setSelected("1".equals(rs.getString("kualifikasi_apn")));
                    if (rs.getDate("tanggal_masuk_rs") != null) {
                        isiTanggalJam(dtpMasukRs, rs.getDate("tanggal_masuk_rs"), rs.getString("jam_masuk_rs"));
                    }
                    taAnamnesa.setText(nvl(rs.getString("anamnesa")));
                    if (!nvl(rs.getString("indikasi_dirawat")).equals("")) {
                        taIndikasiDirawat.setText(rs.getString("indikasi_dirawat"));
                    }
                    taTindakan.setText(nvl(rs.getString("tindakan_dilakukan")));
                    taTerapi.setText(nvl(rs.getString("terapi_diberikan")));
                    cmbTransportasi.setSelectedItem(cocokkanOpsi(cmbTransportasi, rs.getString("transportasi")));
                    ckRmPasien.setSelected("1".equals(rs.getString("dok_rm_pasien")));
                    ckObatOral.setSelected("1".equals(rs.getString("obat_oral")));
                    ckObatInjeksi.setSelected("1".equals(rs.getString("obat_injeksi")));
                    ckObatDibawa.setSelected("1".equals(rs.getString("obat_dibawa")));
                    ckHasilLab.setSelected("1".equals(rs.getString("hasil_lab")));
                    ckHasilUsg.setSelected("1".equals(rs.getString("hasil_usg")));
                    ckHasilRontgen.setSelected("1".equals(rs.getString("hasil_rontgen")));
                    ckDompet.setSelected("1".equals(rs.getString("barang_dompet")));
                    ckHp.setSelected("1".equals(rs.getString("barang_hp")));
                    ckBarangLainnya.setSelected("1".equals(rs.getString("barang_lainnya")));
                    tBarangLainnyaKet.setText(nvl(rs.getString("barang_lainnya_ket")));
                    if (!nvl(rs.getString("ku_sebelum")).equals("")) {
                        tKuSebelum.setText(rs.getString("ku_sebelum"));
                        tTdSebelum.setText(nvl(rs.getString("td_sebelum")));
                        tNadiSebelum.setText(nvl(rs.getString("nadi_sebelum")));
                        tRrSebelum.setText(nvl(rs.getString("rr_sebelum")));
                        tSuhuSebelum.setText(nvl(rs.getString("suhu_sebelum")));
                        tSpo2Sebelum.setText(nvl(rs.getString("spo2_sebelum")));
                        tPemFisikSebelum.setText(nvl(rs.getString("pemfisik_sebelum")));
                        tCatatanSebelum.setText(nvl(rs.getString("catatan_sebelum")));
                    }
                    tKuSetelah.setText(nvl(rs.getString("ku_setelah")));
                    tTdSetelah.setText(nvl(rs.getString("td_setelah")));
                    tNadiSetelah.setText(nvl(rs.getString("nadi_setelah")));
                    tRrSetelah.setText(nvl(rs.getString("rr_setelah")));
                    tSuhuSetelah.setText(nvl(rs.getString("suhu_setelah")));
                    tSpo2Setelah.setText(nvl(rs.getString("spo2_setelah")));
                    tPemFisikSetelah.setText(nvl(rs.getString("pemfisik_setelah")));
                    tCatatanSetelah.setText(nvl(rs.getString("catatan_setelah")));
                    if (!nvl(rs.getString("nip_menyerahkan")).equals("")) {
                        nipMenyerahkan = rs.getString("nip_menyerahkan");
                        tNamaMenyerahkan.setText(nvl(rs.getString("nama_menyerahkan")));
                        lblFotoMenyerahkan.setIcon(ambilFotoTtd(nipMenyerahkan));
                    }
                    tNamaMenerima.setText(nvl(rs.getString("nama_menerima")));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat transfer internal : " + e);
        }
    }

    private void isiTanggalJam(widget.Tanggal komponen, java.sql.Date tgl, String jam) {
        if (tgl == null) { return; }
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
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into lembar_transfer_pasien_internal (no_rawat,ruangan_asal,ruangan_tujuan,petugas_tujuan_dihubungi,"
                + "tanggal_dihubungi,jam_dihubungi,tanggal_transfer,jam_transfer,kategori_transfer,petugas_pendamping,"
                + "kualifikasi_btcls,kualifikasi_ppgd,kualifikasi_apn,tanggal_masuk_rs,jam_masuk_rs,anamnesa,indikasi_dirawat,"
                + "tindakan_dilakukan,terapi_diberikan,transportasi,dok_rm_pasien,obat_oral,obat_injeksi,obat_dibawa,"
                + "hasil_lab,hasil_usg,hasil_rontgen,barang_dompet,barang_hp,barang_lainnya,barang_lainnya_ket,"
                + "ku_sebelum,td_sebelum,nadi_sebelum,rr_sebelum,suhu_sebelum,spo2_sebelum,pemfisik_sebelum,catatan_sebelum,"
                + "ku_setelah,td_setelah,nadi_setelah,rr_setelah,suhu_setelah,spo2_setelah,pemfisik_setelah,catatan_setelah,"
                + "nip_menyerahkan,nama_menyerahkan,nama_menerima,updated_by,updated_at,created_by,created_at) "
                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
                + "?,?,?,?,?,?,?,?,?,?,?,?,now(),?,now()) "
                + "on duplicate key update ruangan_asal=values(ruangan_asal),ruangan_tujuan=values(ruangan_tujuan),"
                + "petugas_tujuan_dihubungi=values(petugas_tujuan_dihubungi),tanggal_dihubungi=values(tanggal_dihubungi),"
                + "jam_dihubungi=values(jam_dihubungi),tanggal_transfer=values(tanggal_transfer),jam_transfer=values(jam_transfer),"
                + "kategori_transfer=values(kategori_transfer),petugas_pendamping=values(petugas_pendamping),"
                + "kualifikasi_btcls=values(kualifikasi_btcls),kualifikasi_ppgd=values(kualifikasi_ppgd),"
                + "kualifikasi_apn=values(kualifikasi_apn),tanggal_masuk_rs=values(tanggal_masuk_rs),"
                + "jam_masuk_rs=values(jam_masuk_rs),anamnesa=values(anamnesa),indikasi_dirawat=values(indikasi_dirawat),"
                + "tindakan_dilakukan=values(tindakan_dilakukan),terapi_diberikan=values(terapi_diberikan),"
                + "transportasi=values(transportasi),dok_rm_pasien=values(dok_rm_pasien),obat_oral=values(obat_oral),"
                + "obat_injeksi=values(obat_injeksi),obat_dibawa=values(obat_dibawa),hasil_lab=values(hasil_lab),"
                + "hasil_usg=values(hasil_usg),hasil_rontgen=values(hasil_rontgen),barang_dompet=values(barang_dompet),"
                + "barang_hp=values(barang_hp),barang_lainnya=values(barang_lainnya),"
                + "barang_lainnya_ket=values(barang_lainnya_ket),ku_sebelum=values(ku_sebelum),td_sebelum=values(td_sebelum),"
                + "nadi_sebelum=values(nadi_sebelum),rr_sebelum=values(rr_sebelum),suhu_sebelum=values(suhu_sebelum),"
                + "spo2_sebelum=values(spo2_sebelum),pemfisik_sebelum=values(pemfisik_sebelum),"
                + "catatan_sebelum=values(catatan_sebelum),ku_setelah=values(ku_setelah),td_setelah=values(td_setelah),"
                + "nadi_setelah=values(nadi_setelah),rr_setelah=values(rr_setelah),suhu_setelah=values(suhu_setelah),"
                + "spo2_setelah=values(spo2_setelah),pemfisik_setelah=values(pemfisik_setelah),"
                + "catatan_setelah=values(catatan_setelah),nip_menyerahkan=values(nip_menyerahkan),"
                + "nama_menyerahkan=values(nama_menyerahkan),nama_menerima=values(nama_menerima),"
                + "updated_by=values(updated_by),updated_at=now()")) {
            int i = 1;
            ps.setString(i++, ambil(TNoRw));
            ps.setString(i++, ambil(tRuanganAsal));
            ps.setString(i++, ambil(tRuanganTujuan));
            ps.setString(i++, ambil(tPetugasTujuanDihubungi));
            setTglJam(ps, i, dtpDihubungi); i += 2;
            setTglJam(ps, i, dtpTransfer); i += 2;
            ps.setString(i++, s(cmbKategori));
            ps.setString(i++, s(cmbPendamping));
            ps.setString(i++, ckBtcls.isSelected() ? "1" : "0");
            ps.setString(i++, ckPpgd.isSelected() ? "1" : "0");
            ps.setString(i++, ckApn.isSelected() ? "1" : "0");
            setTglJam(ps, i, dtpMasukRs); i += 2;
            ps.setString(i++, ambil(taAnamnesa));
            ps.setString(i++, ambil(taIndikasiDirawat));
            ps.setString(i++, ambil(taTindakan));
            ps.setString(i++, ambil(taTerapi));
            ps.setString(i++, s(cmbTransportasi));
            ps.setString(i++, ckRmPasien.isSelected() ? "1" : "0");
            ps.setString(i++, ckObatOral.isSelected() ? "1" : "0");
            ps.setString(i++, ckObatInjeksi.isSelected() ? "1" : "0");
            ps.setString(i++, ckObatDibawa.isSelected() ? "1" : "0");
            ps.setString(i++, ckHasilLab.isSelected() ? "1" : "0");
            ps.setString(i++, ckHasilUsg.isSelected() ? "1" : "0");
            ps.setString(i++, ckHasilRontgen.isSelected() ? "1" : "0");
            ps.setString(i++, ckDompet.isSelected() ? "1" : "0");
            ps.setString(i++, ckHp.isSelected() ? "1" : "0");
            ps.setString(i++, ckBarangLainnya.isSelected() ? "1" : "0");
            ps.setString(i++, ambil(tBarangLainnyaKet));
            ps.setString(i++, ambil(tKuSebelum));
            ps.setString(i++, ambil(tTdSebelum));
            ps.setString(i++, ambil(tNadiSebelum));
            ps.setString(i++, ambil(tRrSebelum));
            ps.setString(i++, ambil(tSuhuSebelum));
            ps.setString(i++, ambil(tSpo2Sebelum));
            ps.setString(i++, ambil(tPemFisikSebelum));
            ps.setString(i++, ambil(tCatatanSebelum));
            ps.setString(i++, ambil(tKuSetelah));
            ps.setString(i++, ambil(tTdSetelah));
            ps.setString(i++, ambil(tNadiSetelah));
            ps.setString(i++, ambil(tRrSetelah));
            ps.setString(i++, ambil(tSuhuSetelah));
            ps.setString(i++, ambil(tSpo2Setelah));
            ps.setString(i++, ambil(tPemFisikSetelah));
            ps.setString(i++, ambil(tCatatanSetelah));
            ps.setString(i++, nipMenyerahkan);
            ps.setString(i++, ambil(tNamaMenyerahkan));
            ps.setString(i++, ambil(tNamaMenerima));
            ps.setString(i++, akses.getkode());
            ps.setString(i++, akses.getkode());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Lembar transfer pasien internal tersimpan.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan.\n" + e.getMessage());
        }
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
        if (JOptionPane.showConfirmDialog(this, "Hapus lembar transfer pasien internal untuk No.Rawat " + ambil(TNoRw) + " ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement("delete from lembar_transfer_pasien_internal where no_rawat=?")) {
            ps.setString(1, ambil(TNoRw));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data dihapus.");
            String norw = ambil(TNoRw);
            setNoRm(norw);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus.\n" + e.getMessage());
        }
    }

    /** Cetak langsung dari no_rawat tanpa membuka dialog (dipakai dari klik-kanan di layar Riwayat). */
    public static void cetak(String noRawat) {
        if (noRawat == null || noRawat.trim().isEmpty()) {
            return;
        }
        RMTransferPasienInternal f = new RMTransferPasienInternal(null, false);
        f.isCek();
        f.setNoRm(noRawat.trim());
        f.cetak();
        f.dispose();
    }

    private void cetak() {
        if (ambil(TNoRw).equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        if (Sequel.cariInteger("select count(*) from lembar_transfer_pasien_internal where no_rawat=?", ambil(TNoRw)) == 0) {
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
            String sql = "select t.ruangan_asal,t.ruangan_tujuan,t.petugas_tujuan_dihubungi,"
                    + "ifnull(date_format(t.tanggal_dihubungi,'%d-%m-%Y'),'') as tanggal_dihubungi,ifnull(t.jam_dihubungi,'') as jam_dihubungi,"
                    + "ifnull(date_format(t.tanggal_transfer,'%d-%m-%Y'),'') as tanggal_transfer,ifnull(t.jam_transfer,'') as jam_transfer,"
                    + "t.kategori_transfer,t.petugas_pendamping,t.kualifikasi_btcls,t.kualifikasi_ppgd,t.kualifikasi_apn,"
                    + "ifnull(date_format(t.tanggal_masuk_rs,'%d-%m-%Y'),'') as tanggal_masuk_rs,ifnull(t.jam_masuk_rs,'') as jam_masuk_rs,"
                    + "t.anamnesa,t.indikasi_dirawat,t.tindakan_dilakukan,t.terapi_diberikan,t.transportasi,"
                    + "t.dok_rm_pasien,t.obat_oral,t.obat_injeksi,t.obat_dibawa,t.hasil_lab,t.hasil_usg,t.hasil_rontgen,"
                    + "t.barang_dompet,t.barang_hp,t.barang_lainnya,t.barang_lainnya_ket,"
                    + "t.ku_sebelum,t.td_sebelum,t.nadi_sebelum,t.rr_sebelum,t.suhu_sebelum,t.spo2_sebelum,t.pemfisik_sebelum,t.catatan_sebelum,"
                    + "t.ku_setelah,t.td_setelah,t.nadi_setelah,t.rr_setelah,t.suhu_setelah,t.spo2_setelah,t.pemfisik_setelah,t.catatan_setelah,"
                    + "t.nama_menyerahkan,t.nama_menerima,"
                    + "p.no_rkm_medis,p.nm_pasien,if(p.jk='L','Laki-laki','Perempuan') as jk,"
                    + "ifnull(date_format(p.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir,"
                    + fotoSqlByNip("t.nip_menyerahkan", "menyerahkan_photo") + " "
                    + "from lembar_transfer_pasien_internal t "
                    + "inner join reg_periksa rp on t.no_rawat=rp.no_rawat "
                    + "inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis "
                    + "where t.no_rawat='" + ambil(TNoRw) + "'";
            Valid.MyReportqry("rptLembarTransferPasienInternal.jasper", "report",
                    "::[ Lembar Transfer Pasien Internal (RM 38) ]::", sql, param);
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

    private void ensureTable() {
        Sequel.queryu2(
                "create table if not exists lembar_transfer_pasien_internal ("
                + "no_rawat varchar(17) not null primary key,"
                + "ruangan_asal varchar(60) null,"
                + "ruangan_tujuan varchar(60) null,"
                + "petugas_tujuan_dihubungi varchar(60) null,"
                + "tanggal_dihubungi date null,"
                + "jam_dihubungi varchar(8) null,"
                + "tanggal_transfer date null,"
                + "jam_transfer varchar(8) null,"
                + "kategori_transfer varchar(10) null,"
                + "petugas_pendamping varchar(20) null,"
                + "kualifikasi_btcls varchar(1) null,"
                + "kualifikasi_ppgd varchar(1) null,"
                + "kualifikasi_apn varchar(1) null,"
                + "tanggal_masuk_rs date null,"
                + "jam_masuk_rs varchar(8) null,"
                + "anamnesa text null,"
                + "indikasi_dirawat text null,"
                + "tindakan_dilakukan text null,"
                + "terapi_diberikan text null,"
                + "transportasi varchar(20) null,"
                + "dok_rm_pasien varchar(1) null,"
                + "obat_oral varchar(1) null,"
                + "obat_injeksi varchar(1) null,"
                + "obat_dibawa varchar(1) null,"
                + "hasil_lab varchar(1) null,"
                + "hasil_usg varchar(1) null,"
                + "hasil_rontgen varchar(1) null,"
                + "barang_dompet varchar(1) null,"
                + "barang_hp varchar(1) null,"
                + "barang_lainnya varchar(1) null,"
                + "barang_lainnya_ket varchar(100) null,"
                + "ku_sebelum varchar(30) null,"
                + "td_sebelum varchar(20) null,"
                + "nadi_sebelum varchar(10) null,"
                + "rr_sebelum varchar(10) null,"
                + "suhu_sebelum varchar(10) null,"
                + "spo2_sebelum varchar(10) null,"
                + "pemfisik_sebelum text null,"
                + "catatan_sebelum text null,"
                + "ku_setelah varchar(30) null,"
                + "td_setelah varchar(20) null,"
                + "nadi_setelah varchar(10) null,"
                + "rr_setelah varchar(10) null,"
                + "suhu_setelah varchar(10) null,"
                + "spo2_setelah varchar(10) null,"
                + "pemfisik_setelah text null,"
                + "catatan_setelah text null,"
                + "nip_menyerahkan varchar(20) null,"
                + "nama_menyerahkan varchar(60) null,"
                + "nama_menerima varchar(60) null,"
                + "created_by varchar(50) null,"
                + "updated_by varchar(50) null,"
                + "created_at datetime null,"
                + "updated_at datetime null"
                + ")");
        // Perbaikan utk tabel yg SUDAH terlanjur dibuat (create table if not exists di atas tidak
        // mengubah tabel yg sudah ada): pemfisik_sebelum/catatan_sebelum/pemfisik_setelah/
        // catatan_setelah awalnya varchar(150), kekecilan utk catatan pemeriksaan dokter yg
        // ditarik otomatis dari pemeriksaan_ranap (bisa jauh lebih panjang) -> error "Data too
        // long". Diperlebar jadi TEXT. queryu2() aman dipanggil berulang (exception ditelan,
        // MODIFY COLUMN ke tipe yg sama juga tidak merusak data), jadi cukup jalan tiap dialog dibuka.
        for (String kolom : new String[]{"pemfisik_sebelum", "catatan_sebelum", "pemfisik_setelah", "catatan_setelah"}) {
            Sequel.queryu2("alter table lembar_transfer_pasien_internal modify column " + kolom + " text null");
        }
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

    /** Bungkus field readonly + label foto TTD kecil di sebelah kanan. */
    private JPanel bungkusFotoTtd(Component field, JLabel lblFoto) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        p.add(field, BorderLayout.CENTER);
        lblFoto.setPreferredSize(new Dimension(60, 28));
        p.add(lblFoto, BorderLayout.EAST);
        return p;
    }

    /** Foto TTD petugas berdasarkan NIP, ditarik dari pegawai.photo -- pola sama seperti RMRingkasanRiwayatMasuk. Di-cache per NIP. */
    private ImageIcon ambilFotoTtd(String nip) {
        if (nip == null || nip.trim().isEmpty()) { return null; }
        String key = nip.trim();
        if (cacheFotoTtd.containsKey(key)) { return cacheFotoTtd.get(key); }
        ImageIcon ic = null;
        try {
            String photo = bersihkanPathFotoTtd(Sequel.cariIsi("select photo from pegawai where nik=?", key));
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
        if (photo == null) { return ""; }
        String p = photo.trim();
        if (p.equals("") || p.equals("-") || p.equals("pages/pegawai/photo/")) { return ""; }
        return p.replace("\\", "/");
    }

    // ====================== Helpers UI (pola sama dengan RMPengantarPasienRanap) ======================
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
