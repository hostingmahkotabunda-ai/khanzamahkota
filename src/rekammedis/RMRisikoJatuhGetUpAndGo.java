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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import kepegawaian.DlgCariPetugas;

/**
 * Panel "Pengkajian dan Intervensi Risiko Jatuh Pasien Poli Klinik - Get Up
 * and Go Test" (rawat jalan &amp; IGD), di-embed sebagai tab di DlgRawatJalan
 * dan DlgIGD (mengikuti pola RMAsesmenRalan). Hasil & tindakan DIHITUNG
 * OTOMATIS dari 2 kriteria pengkajian (bukan dipilih manual):
 * - 0 dari {cara jalan tidak normal, menopang saat duduk} -> Tidak Berisiko, tidak ada tindakan
 * - 1 dari 2 -> Risiko Rendah -> Edukasi
 * - 2 dari 2 -> Risiko Tinggi -> Pasang pita kuning + Edukasi
 * Kolom TTD/nama petugas otomatis terisi dari user yg sedang login (spt
 * RMAsesmenUlangNyeri), tapi bisa diganti lewat tombol "...".
 */
public final class RMRisikoJatuhGetUpAndGo extends JPanel {

    private static final Font FONT_FORM = new Font("Times New Roman", Font.PLAIN, 13);
    private static final Font FONT_FORM_BOLD = new Font("Times New Roman", Font.BOLD, 13);
    private static final Color GARIS_TABEL = new Color(153, 153, 153);
    private static final Color LATAR_HEADER_TABEL = new Color(238, 242, 245);

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();

    private String noRawat = "";

    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TJK = ro();
    private final widget.TextBox TTglLahir = ro();
    private final widget.TextBox TRuang = ro();
    private final widget.TextBox TDx = tf();
    private final widget.Tanggal dtpTanggal = dt();

    private final JCheckBox chkA1 = new JCheckBox("1. Tidak seimbang / sempoyongan / limbung");
    private final JCheckBox chkA2 = new JCheckBox("2. Jalan dengan menggunakan alat bantu ( kruk, tripot, kursi roda, orang lain )");
    private final JCheckBox chkB = new JCheckBox("Menopang saat akan duduk : tampak memegang pinggiran kursi atau meja / benda lain sebagai penopang saat akan duduk");
    /** Indikator Ya/Tdk per baris "a"/"b" -- BUKAN dicentang manual, cuma cerminan otomatis dari
     *  chkA1/chkA2 (baris a, salah satu = Ya) & chkB (baris b), spy tampil sbg kolom Ya/Tdk spt di
     *  kertas asli (lihat rptRisikoJatuhGetUpAndGo.jrxml bagian "1. PENGKAJIAN"). */
    private final JCheckBox chkYaA = new JCheckBox();
    private final JCheckBox chkTdkA = new JCheckBox();
    private final JCheckBox chkYaB = new JCheckBox();
    private final JCheckBox chkTdkB = new JCheckBox();

    private final JLabel lblHasil = new JLabel("-");
    /** Tabel "3. Tindakan" -- 4 baris tetap (persis kertas asli), kolom Ya/Tdk DICENTANG MANUAL
     *  oleh petugas (bukan otomatis) -- lihat pasangRadioTindakan(). Baris "2. Hasil" di atasnya
     *  tetap otomatis, jadi cukup jadi acuan petugas utk menentukan mana yg dicentang di sini. */
    private final DefaultTableModel modelTindakan = new DefaultTableModel(
            new Object[]{"No", "Hasil Kajian", "Tindakan", "Ya", "Tidak"}, 0) {
        @Override public Class<?> getColumnClass(int c) { return c >= 3 ? Boolean.class : String.class; }
        @Override public boolean isCellEditable(int row, int col) { return col >= 3; }
    };
    private final widget.Table tblTindakan = new widget.Table();
    private boolean sedangMemuatTindakan = false;

    private final DlgCariPetugas pickerPetugas = new DlgCariPetugas(null, true);
    private final widget.TextBox tPetugasNama = ro();
    private final widget.Button btnPilihPetugas = new widget.Button();
    private String kdPetugas = "";

    private final widget.Button BtnSimpan = new widget.Button();
    private final widget.Button BtnCetak = new widget.Button();

    public RMRisikoJatuhGetUpAndGo() {
        setLayout(new BorderLayout());
        setOpaque(false);
        ensureTable();
        isiBarisTindakanTetap();
        pasangRadioTindakan();

        JPanel isi = new JPanel();
        isi.setOpaque(false);
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));
        isi.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        isi.add(panelJudul());
        isi.add(Box.createVerticalStrut(10));
        isi.add(panelIdentitas());
        isi.add(Box.createVerticalStrut(10));
        isi.add(panelPengkajian());
        isi.add(Box.createVerticalStrut(10));
        isi.add(panelHasil());
        isi.add(Box.createVerticalStrut(10));
        isi.add(panelTindakan());

        JScrollPane scroll = new JScrollPane(isi);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        BtnSimpan.setText("Simpan");
        BtnSimpan.addActionListener(e -> simpan());
        BtnCetak.setText("Cetak");
        BtnCetak.addActionListener(e -> cetak());
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        bawah.setOpaque(false);
        bawah.add(BtnCetak);
        bawah.add(BtnSimpan);
        add(bawah, BorderLayout.SOUTH);

        siapkanPickerPetugas();
        java.awt.event.ItemListener perbarui = e -> perbaruiHasilDanTindakan();
        chkA1.addItemListener(perbarui);
        chkA2.addItemListener(perbarui);
        chkB.addItemListener(perbarui);
        perbaruiHasilDanTindakan();
    }

    // ====================== UI ======================
    private JPanel panelJudul() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel judul = new JLabel("Pengkajian dan Intervensi Risiko Jatuh Pasien Poli Klinik");
        judul.setFont(new Font("Times New Roman", Font.BOLD, 18));
        judul.setForeground(new Color(32, 49, 66));
        JLabel sub = new JLabel("Get Up and Go Test  •  Rawat Jalan & IGD");
        sub.setFont(FONT_FORM);
        sub.setForeground(new Color(92, 107, 119));
        p.add(judul);
        p.add(Box.createVerticalStrut(2));
        p.add(sub);
        return p;
    }

    private JPanel panelIdentitas() {
        JPanel kartu = kartu();
        kartu.setLayout(new java.awt.GridLayout(2, 1, 0, 6));
        JPanel baris1 = new JPanel(new java.awt.GridLayout(1, 5, 8, 0));
        baris1.setOpaque(false);
        baris1.add(fieldRingkasan("No. Rawat", TNoRw));
        baris1.add(fieldRingkasan("No. RM", TNoRM));
        baris1.add(fieldRingkasan("Nama Pasien", TPasien));
        baris1.add(fieldRingkasan("Jenis Kelamin", TJK));
        baris1.add(fieldRingkasan("Tanggal Lahir", TTglLahir));
        JPanel baris2 = new JPanel(new java.awt.GridLayout(1, 3, 8, 0));
        baris2.setOpaque(false);
        baris2.add(fieldRingkasan("DX", TDx));
        baris2.add(fieldRingkasan("Ruang", TRuang));
        JPanel tglWrap = new JPanel(new BorderLayout());
        tglWrap.setOpaque(false);
        JLabel lbl = new JLabel("Tanggal");
        lbl.setFont(new Font("Tahoma", Font.PLAIN, 10));
        lbl.setForeground(new Color(120, 133, 143));
        tglWrap.add(lbl, BorderLayout.NORTH);
        tglWrap.add(dtpTanggal, BorderLayout.CENTER);
        baris2.add(tglWrap);
        kartu.add(baris1);
        kartu.add(baris2);
        return kartu;
    }

    /** Kolom "No."/"Penilaian & Pengkajian"/"Ya"/"Tdk" -- persis susunan tabel "1. PENGKAJIAN" di kertas asli. */
    private JPanel panelPengkajian() {
        JPanel kartu = kartu();
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));
        kartu.add(judulSeksi("1. Pengkajian"));

        JPanel tabel = new JPanel(new java.awt.GridBagLayout());
        tabel.setOpaque(false);
        tabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        tabel.setBorder(BorderFactory.createLineBorder(GARIS_TABEL));
        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.fill = java.awt.GridBagConstraints.BOTH;

        g.gridy = 0;
        tabel.add(selHeaderTabel("No.", JLabel.CENTER), kolTabel(g, 0, 0.06));
        tabel.add(selHeaderTabel("Penilaian / Pengkajian", JLabel.LEFT), kolTabel(g, 1, 0.72));
        tabel.add(selHeaderTabel("Ya", JLabel.CENTER), kolTabel(g, 2, 0.11));
        tabel.add(selHeaderTabel("Tdk", JLabel.CENTER), kolTabel(g, 3, 0.11));

        JPanel isiA = new JPanel();
        isiA.setOpaque(false);
        isiA.setLayout(new BoxLayout(isiA, BoxLayout.Y_AXIS));
        isiA.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 1, GARIS_TABEL), BorderFactory.createEmptyBorder(4, 6, 4, 4)));
        JLabel ketA = new JLabel("Cara berjalan pasien ( salah satu atau lebih ) :");
        ketA.setFont(FONT_FORM);
        ketA.setAlignmentX(Component.LEFT_ALIGNMENT);
        isiA.add(ketA);
        isiA.add(baris(chkA1, 12));
        isiA.add(baris(chkA2, 12));

        g.gridy = 1;
        tabel.add(selNoTabel("a"), kolTabel(g, 0, 0.06));
        tabel.add(isiA, kolTabel(g, 1, 0.72));
        tabel.add(selIndikator(chkYaA), kolTabel(g, 2, 0.11));
        tabel.add(selIndikator(chkTdkA), kolTabel(g, 3, 0.11));

        JPanel isiB = new JPanel(new BorderLayout());
        isiB.setOpaque(false);
        isiB.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 4));
        isiB.add(baris(chkB, 0), BorderLayout.CENTER);

        g.gridy = 2;
        tabel.add(selNoTabel("b"), kolTabel(g, 0, 0.06));
        tabel.add(isiB, kolTabel(g, 1, 0.72));
        tabel.add(selIndikator(chkYaB), kolTabel(g, 2, 0.11));
        tabel.add(selIndikator(chkTdkB), kolTabel(g, 3, 0.11));

        kartu.add(tabel);
        return kartu;
    }

    private java.awt.GridBagConstraints kolTabel(java.awt.GridBagConstraints base, int gridx, double weightx) {
        java.awt.GridBagConstraints g = (java.awt.GridBagConstraints) base.clone();
        g.gridx = gridx;
        g.weightx = weightx;
        return g;
    }

    private JLabel selHeaderTabel(String teks, int align) {
        JLabel l = new JLabel(teks, align);
        l.setFont(new Font("Tahoma", Font.BOLD, 11));
        l.setForeground(new Color(32, 49, 66));
        l.setOpaque(true);
        l.setBackground(LATAR_HEADER_TABEL);
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 1, GARIS_TABEL), BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        return l;
    }

    private JLabel selNoTabel(String teks) {
        JLabel l = new JLabel(teks, JLabel.CENTER);
        l.setFont(FONT_FORM);
        l.setVerticalAlignment(JLabel.TOP);
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 1, GARIS_TABEL), BorderFactory.createEmptyBorder(6, 4, 4, 4)));
        return l;
    }

    /** Kotak centang Ya/Tdk di kolom kanan tabel Pengkajian -- BUKAN diklik manual, cuma cermin
     *  otomatis (lihat perbaruiHasilDanTindakan()), makanya di-nonaktifkan (disabled). */
    private JPanel selIndikator(JCheckBox indikator) {
        indikator.setEnabled(false);
        indikator.setOpaque(false);
        JPanel wrap = new JPanel(new java.awt.GridBagLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, GARIS_TABEL));
        wrap.add(indikator);
        return wrap;
    }

    private JPanel panelHasil() {
        JPanel kartu = kartu();
        kartu.setLayout(new BorderLayout(10, 0));
        kartu.add(judulSeksi("2. Hasil"), BorderLayout.NORTH);
        lblHasil.setFont(new Font("Tahoma", Font.BOLD, 16));
        lblHasil.setOpaque(true);
        lblHasil.setHorizontalAlignment(JLabel.CENTER);
        lblHasil.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        wrap.add(lblHasil, BorderLayout.CENTER);
        kartu.add(wrap, BorderLayout.CENTER);
        return kartu;
    }

    /** Tabel "No. / Hasil Kajian / Tindakan / Ya / Tdk" -- persis kertas asli, 4 baris tetap,
     *  kolom Ya/Tdk terisi otomatis dari hasil skor (lihat perbaruiHasilDanTindakan()). */
    private JPanel panelTindakan() {
        JPanel kartu = kartu();
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));
        kartu.add(judulSeksi("3. Tindakan"));

        tblTindakan.setModel(modelTindakan);
        tblTindakan.setFont(FONT_FORM);
        tblTindakan.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 11));
        tblTindakan.setRowHeight(24);
        tblTindakan.setRowSelectionAllowed(false);
        tblTindakan.setFocusable(false);
        int[] lebarTindakan = {40, 150, 280, 60, 60};
        for (int i = 0; i < lebarTindakan.length; i++) {
            tblTindakan.getColumnModel().getColumn(i).setPreferredWidth(lebarTindakan[i]);
        }
        tblTindakan.getColumnModel().getColumn(0).setCellRenderer(rendererTengah());
        tblTindakan.setPreferredScrollableViewportSize(new Dimension(590, 24 * 4));
        JScrollPane scrollTindakan = new JScrollPane(tblTindakan);
        scrollTindakan.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollTindakan.setBorder(BorderFactory.createLineBorder(GARIS_TABEL));
        scrollTindakan.setPreferredSize(new Dimension(590, 24 * 4 + 26));
        scrollTindakan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24 * 4 + 26));
        kartu.add(scrollTindakan);
        kartu.add(Box.createVerticalStrut(10));

        JPanel baris = new JPanel(new java.awt.GridBagLayout());
        baris.setOpaque(false);
        baris.setAlignmentX(Component.LEFT_ALIGNMENT);
        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.insets = new java.awt.Insets(3, 3, 3, 3);
        g.fill = java.awt.GridBagConstraints.HORIZONTAL;
        g.gridy = 0;
        g.gridx = 0; g.weightx = 0;
        JLabel lblPetugas = new JLabel("TTD / Nama Petugas :");
        lblPetugas.setFont(new Font("Tahoma", Font.PLAIN, 11));
        baris.add(lblPetugas, g);
        g.gridx = 1; g.weightx = 1;
        tPetugasNama.setPreferredSize(new Dimension(240, 24));
        JPanel wrapPetugas = new JPanel(new BorderLayout(3, 0));
        wrapPetugas.setOpaque(false);
        wrapPetugas.add(tPetugasNama, BorderLayout.CENTER);
        btnPilihPetugas.setText("...");
        btnPilihPetugas.setPreferredSize(new Dimension(28, 23));
        wrapPetugas.add(btnPilihPetugas, BorderLayout.EAST);
        baris.add(wrapPetugas, g);
        kartu.add(baris);
        return kartu;
    }

    private JPanel baris(JCheckBox chk, int indentKiri) {
        chk.setOpaque(false);
        chk.setFont(FONT_FORM);
        chk.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, indentKiri, 0, 0));
        p.add(chk, BorderLayout.CENTER);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private JLabel judulSeksi(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font("Tahoma", Font.BOLD, 13));
        l.setForeground(new Color(32, 49, 66));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return l;
    }

    private JPanel kartu() {
        JPanel p = new JPanel();
        p.setBackground(java.awt.Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 224, 230)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private JPanel fieldRingkasan(String label, widget.TextBox field) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Tahoma", Font.PLAIN, 10));
        l.setForeground(new Color(120, 133, 143));
        p.add(l, BorderLayout.NORTH);
        field.setFont(FONT_FORM_BOLD);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private javax.swing.table.DefaultTableCellRenderer rendererTengah() {
        javax.swing.table.DefaultTableCellRenderer r = new javax.swing.table.DefaultTableCellRenderer();
        r.setHorizontalAlignment(JLabel.CENTER);
        return r;
    }

    /** 4 baris tetap tabel "3. Tindakan" persis kertas asli (No / Hasil Kajian / Tindakan) --
     *  kolom Ya/Tdk-nya DICENTANG MANUAL oleh petugas, mulai kosong (lihat pasangRadioTindakan()). */
    private void isiBarisTindakanTetap() {
        modelTindakan.addRow(new Object[]{"1", "Tidak Berisiko", "Tidak ada tindakan", false, false});
        modelTindakan.addRow(new Object[]{"2", "Risiko Rendah", "Edukasi", false, false});
        modelTindakan.addRow(new Object[]{"3", "Risiko Tinggi", "Pasang pita kuning", false, false});
        modelTindakan.addRow(new Object[]{"", "", "Edukasi", false, false});
    }

    /** Sekali kolom "Ya" atau "Tidak" dicentang di suatu baris, pasangannya di baris yg sama
     *  otomatis lepas (perilaku radio) -- spy tdk bisa Ya & Tidak sama2 tercentang di 1 baris. */
    private void pasangRadioTindakan() {
        modelTindakan.addTableModelListener(e -> {
            if (sedangMemuatTindakan) { return; }
            if (e.getType() != TableModelEvent.UPDATE || e.getColumn() < 3) { return; }
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (Boolean.TRUE.equals(modelTindakan.getValueAt(row, col))) {
                int lawan = col == 3 ? 4 : 3;
                sedangMemuatTindakan = true;
                modelTindakan.setValueAt(false, row, lawan);
                sedangMemuatTindakan = false;
            }
        });
    }

    // ====================== Hasil & Tindakan otomatis ======================
    private void perbaruiHasilDanTindakan() {
        boolean a = chkA1.isSelected() || chkA2.isSelected();
        boolean b = chkB.isSelected();
        chkYaA.setSelected(a);
        chkTdkA.setSelected(!a);
        chkYaB.setSelected(b);
        chkTdkB.setSelected(!b);

        int skor = hitungSkor();
        String hasil = teksHasil(skor);
        lblHasil.setText(hasil.toUpperCase());
        if (skor == 0) {
            lblHasil.setForeground(new Color(0, 128, 68));
            lblHasil.setBackground(new Color(224, 246, 234));
        } else if (skor == 1) {
            lblHasil.setForeground(new Color(150, 96, 0));
            lblHasil.setBackground(new Color(255, 244, 224));
        } else {
            lblHasil.setForeground(new Color(178, 30, 30));
            lblHasil.setBackground(new Color(253, 226, 226));
        }
    }

    private int hitungSkor() {
        boolean a = chkA1.isSelected() || chkA2.isSelected();
        boolean b = chkB.isSelected();
        return (a ? 1 : 0) + (b ? 1 : 0);
    }

    private static String teksHasil(int skor) {
        if (skor == 0) { return "Tidak Berisiko"; }
        if (skor == 1) { return "Risiko Rendah"; }
        return "Risiko Tinggi";
    }

    // ====================== Petugas (auto login + bisa ganti) ======================
    private void siapkanPickerPetugas() {
        btnPilihPetugas.addActionListener(e -> {
            pickerPetugas.emptTeks();
            pickerPetugas.isCek();
            pickerPetugas.setSize(650, 400);
            pickerPetugas.setLocationRelativeTo(this);
            pickerPetugas.setVisible(true);
        });
        pickerPetugas.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (pickerPetugas.getTable().getSelectedRow() != -1) {
                    int r = pickerPetugas.getTable().getSelectedRow();
                    kdPetugas = pickerPetugas.getTable().getValueAt(r, 0).toString();
                    tPetugasNama.setText(pickerPetugas.getTable().getValueAt(r, 1).toString());
                }
            }
        });
    }

    private void isiPetugasDenganLoginSaatIni() {
        kdPetugas = akses.getkode();
        String nama = Sequel.cariIsi("select nama from petugas where nip=?", kdPetugas);
        tPetugasNama.setText(nama == null ? "" : nama);
    }

    // ====================== Muat / Simpan ======================
    /** Dipanggil dari host (DlgRawatJalan/DlgIGD) tiap tab ini jadi aktif -- sama pola dgn RMAsesmenRalan.setKonteks. */
    public void setKonteks(String norwt) {
        String baru = norwt == null ? "" : norwt.trim();
        if (baru.equals(noRawat)) {
            return;
        }
        noRawat = baru;
        kosongkan();
        if (noRawat.isEmpty()) {
            return;
        }
        TNoRw.setText(noRawat);
        tarikDataPasien(noRawat);
        if (!muatTersimpan(noRawat)) {
            isiPetugasDenganLoginSaatIni();
        }
        perbaruiHasilDanTindakan();
    }

    private void kosongkan() {
        TNoRw.setText(""); TNoRM.setText(""); TPasien.setText(""); TJK.setText(""); TTglLahir.setText("");
        TRuang.setText(""); TDx.setText("");
        dtpTanggal.setDate(new Date());
        chkA1.setSelected(false);
        chkA2.setSelected(false);
        chkB.setSelected(false);
        tPetugasNama.setText("");
        kdPetugas = "";
        sedangMemuatTindakan = true;
        for (int r = 0; r < modelTindakan.getRowCount(); r++) {
            modelTindakan.setValueAt(false, r, 3);
            modelTindakan.setValueAt(false, r, 4);
        }
        sedangMemuatTindakan = false;
    }

    private void tarikDataPasien(String norwt) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select pasien.no_rkm_medis,pasien.nm_pasien,pasien.jk,"
                + "ifnull(date_format(pasien.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir,"
                + "ifnull((select poliklinik.nm_poli from poliklinik where poliklinik.kd_poli=reg_periksa.kd_poli),'') as ruang "
                + "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "where reg_periksa.no_rawat=?")) {
            ps.setString(1, norwt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TNoRM.setText(nvl(rs.getString("no_rkm_medis")));
                    TPasien.setText(nvl(rs.getString("nm_pasien")));
                    TJK.setText("L".equalsIgnoreCase(nvl(rs.getString("jk"))) ? "Laki-Laki" : "Perempuan");
                    TTglLahir.setText(nvl(rs.getString("tgl_lahir")));
                    TRuang.setText(nvl(rs.getString("ruang")));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik data pasien risiko jatuh : " + e);
        }
        dtpTanggal.setDate(new Date());
    }

    private boolean muatTersimpan(String norwt) {
        try (PreparedStatement ps = koneksi.prepareStatement("select * from risiko_jatuh_getupandgo where no_rawat=?")) {
            ps.setString(1, norwt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TDx.setText(nvl(rs.getString("dx")));
                    if (rs.getDate("tanggal") != null) {
                        String jam = nvl(rs.getString("jam"));
                        try {
                            dtpTanggal.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                    .parse(rs.getDate("tanggal") + " " + (jam.isEmpty() ? "00:00:00" : jam)));
                        } catch (Exception ignore) { }
                    }
                    chkA1.setSelected("1".equals(rs.getString("a1_tidak_seimbang")));
                    chkA2.setSelected("1".equals(rs.getString("a2_alat_bantu")));
                    chkB.setSelected("1".equals(rs.getString("b_menopang_duduk")));
                    sedangMemuatTindakan = true;
                    for (int r = 1; r <= 4; r++) {
                        modelTindakan.setValueAt("1".equals(rs.getString("t" + r + "_ya")), r - 1, 3);
                        modelTindakan.setValueAt("1".equals(rs.getString("t" + r + "_tidak")), r - 1, 4);
                    }
                    sedangMemuatTindakan = false;
                    kdPetugas = nvl(rs.getString("kd_petugas"));
                    tPetugasNama.setText(nvl(rs.getString("nama_petugas")));
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat risiko jatuh : " + e);
        }
        return false;
    }

    private void simpan() {
        if (noRawat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        int skor = hitungSkor();
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into risiko_jatuh_getupandgo "
                + "(no_rawat,no_rkm_medis,dx,tanggal,jam,a1_tidak_seimbang,a2_alat_bantu,b_menopang_duduk,hasil,"
                + "t1_ya,t1_tidak,t2_ya,t2_tidak,t3_ya,t3_tidak,t4_ya,t4_tidak,"
                + "kd_petugas,nama_petugas,created_by,created_at) "
                + "values (?,(select no_rkm_medis from reg_periksa where no_rawat=?),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,now()) "
                + "on duplicate key update no_rkm_medis=values(no_rkm_medis),dx=values(dx),tanggal=values(tanggal),"
                + "jam=values(jam),a1_tidak_seimbang=values(a1_tidak_seimbang),a2_alat_bantu=values(a2_alat_bantu),"
                + "b_menopang_duduk=values(b_menopang_duduk),hasil=values(hasil),"
                + "t1_ya=values(t1_ya),t1_tidak=values(t1_tidak),t2_ya=values(t2_ya),t2_tidak=values(t2_tidak),"
                + "t3_ya=values(t3_ya),t3_tidak=values(t3_tidak),t4_ya=values(t4_ya),t4_tidak=values(t4_tidak),"
                + "kd_petugas=values(kd_petugas),"
                + "nama_petugas=values(nama_petugas),updated_by=values(created_by),updated_at=now()")) {
            Date d = dtpTanggal.getDate();
            ps.setString(1, noRawat);
            ps.setString(2, noRawat);
            ps.setString(3, ambil(TDx));
            ps.setString(4, d == null ? null : new SimpleDateFormat("yyyy-MM-dd").format(d));
            ps.setString(5, d == null ? "" : new SimpleDateFormat("HH:mm:ss").format(d));
            ps.setString(6, chkA1.isSelected() ? "1" : "0");
            ps.setString(7, chkA2.isSelected() ? "1" : "0");
            ps.setString(8, chkB.isSelected() ? "1" : "0");
            ps.setString(9, teksHasil(skor));
            int idx = 10;
            for (int r = 0; r < 4; r++) {
                ps.setString(idx++, Boolean.TRUE.equals(modelTindakan.getValueAt(r, 3)) ? "1" : "0");
                ps.setString(idx++, Boolean.TRUE.equals(modelTindakan.getValueAt(r, 4)) ? "1" : "0");
            }
            ps.setString(idx++, kdPetugas);
            ps.setString(idx++, ambil(tPetugasNama));
            ps.setString(idx, akses.getkode());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Pengkajian Risiko Jatuh (Get Up and Go Test) tersimpan.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan.\n" + e.getMessage());
        }
    }

    private void cetak() {
        if (noRawat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        if (Sequel.cariInteger("select count(*) from risiko_jatuh_getupandgo where no_rawat=?", noRawat) == 0) {
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
                    + "ifnull((select poliklinik.nm_poli from poliklinik where poliklinik.kd_poli=reg_periksa.kd_poli),'') as ruang,"
                    + "ifnull(date_format(a.tanggal,'%d-%m-%Y'),'') as tanggal_cetak,ifnull(a.jam,'') as jam_cetak,"
                    + fotoSql("a.nama_petugas", "petugas_photo") + " "
                    + "from risiko_jatuh_getupandgo a "
                    + "inner join reg_periksa on a.no_rawat=reg_periksa.no_rawat "
                    + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "where a.no_rawat='" + noRawat + "'";
            Valid.MyReportqry("rptRisikoJatuhGetUpAndGo.jasper", "report",
                    "::[ Risiko Jatuh - Get Up and Go Test ]::", sql, param);
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
        Sequel.queryu2(
                "create table if not exists risiko_jatuh_getupandgo ("
                + "no_rawat varchar(17) not null primary key,"
                + "no_rkm_medis varchar(15) null,"
                + "dx varchar(200) null,"
                + "tanggal date null,"
                + "jam varchar(8) null,"
                + "a1_tidak_seimbang varchar(1) null,"
                + "a2_alat_bantu varchar(1) null,"
                + "b_menopang_duduk varchar(1) null,"
                + "hasil varchar(20) null,"
                + "kd_petugas varchar(20) null,"
                + "nama_petugas varchar(60) null,"
                + "created_by varchar(50) null,"
                + "updated_by varchar(50) null,"
                + "created_at datetime null,"
                + "updated_at datetime null"
                + ") ROW_FORMAT=DYNAMIC");
        ensureKolomTindakan();
    }

    /** Kolom Ya/Tidak tabel "3. Tindakan" (dicentang manual oleh petugas) -- ALTER manual krn table
     *  risiko_jatuh_getupandgo sudah ada di instalasi lama sblm kolom ini ditambahkan. */
    private void ensureKolomTindakan() {
        try {
            for (int r = 1; r <= 4; r++) {
                for (String suf : new String[]{"_ya", "_tidak"}) {
                    String kolom = "t" + r + suf;
                    if (Sequel.cariInteger("select count(*) from information_schema.columns where table_schema=database() "
                            + "and table_name='risiko_jatuh_getupandgo' and column_name='" + kolom + "'") == 0) {
                        Sequel.queryu2("alter table risiko_jatuh_getupandgo add column " + kolom + " varchar(1) null");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif kolom tindakan get up and go : " + e);
        }
    }

    // ====================== Helpers UI ======================
    private static widget.TextBox tf() {
        return new widget.TextBox();
    }

    private static widget.TextBox ro() {
        widget.TextBox t = new widget.TextBox();
        t.setEditable(false);
        return t;
    }

    private static widget.Tanggal dt() {
        widget.Tanggal d = new widget.Tanggal();
        d.setDisplayFormat("dd-MM-yyyy HH:mm");
        return d;
    }

    private static String ambil(widget.TextBox t) {
        String s = t.getText();
        return s == null ? "" : s.trim();
    }

    private static String nvl(String v) {
        return v == null ? "" : v;
    }
}
