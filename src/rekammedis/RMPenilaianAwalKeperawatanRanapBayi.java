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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

        row = judul(form, row, "Asesmen Keperawatan Bayi");
        row = baris2(form, row, "Ruang", tRuang, "Lantai", tLantai);
        row = baris2(form, row, "Kelas", tKelas, "Gelang Identitas", cmbGelang);
        row = baris1(form, row, "Tanggal / Jam *", dtpTanggal);

        row = judul(form, row, "A. Identitas Penanggung Jawab");
        row = baris2(form, row, "Nama", pjNama, "Alamat", pjAlamat);
        row = baris2(form, row, "Umur", pjUmur, "Pendidikan", pjPendidikan);
        row = baris1(form, row, "Hubungan", pjHubungan);

        row = judul(form, row, "B. Riwayat Ibu");
        row = baris2(form, row, "Nama", ibuNama, "Umur", ibuUmur);
        row = baris2(form, row, "Pendidikan", ibuPendidikan, "Suku", ibuSuku);
        row = baris2(form, row, "Agama", ibuAgama, "Penyakit", ibuPenyakit);
        row = baris1(form, row, "Perkawinan Ke", ibuPerkawinan);

        row = judul(form, row, "C. Riwayat Penyakit (Untuk Bayi Sakit)");
        row = area(form, row, "Keluhan Utama", taKeluhan);
        row = area(form, row, "Riwayat Kesehatan Sekarang", taRiwSekarang);
        row = area(form, row, "Riwayat Kesehatan Dahulu", taRiwDahulu);

        row = judul(form, row, "D. Riwayat Kehamilan");
        row = grup(form, row, "Komplikasi Kehamilan", grpKomplikasi.panel);
        row = area(form, row, "Lain-lain", taKomplikasiLain);

        row = judul(form, row, "Riwayat Persalinan Yang Lalu");
        row = tabelPersalinan(form, row);

        row = judul(form, row, "F. Riwayat Psikososial Orang Tua");
        row = area(form, row, "Perkembangan Interpersonal", taPsiko);
        row = grup(form, row, "Pengasuh", grpPengasuh.panel);
        row = baris2(form, row, "Dukungan Sibling", cmbSibling, "Dukungan Keluarga Lain", cmbDukKeluarga);
        row = baris2(form, row, "Sebutkan (Keluarga)", tDukKeluargaSebut, "Budaya Dianut", cmbBudaya);
        row = baris1(form, row, "Sebutkan (Budaya)", tBudayaSebut);

        row = judul(form, row, "G. Pemeriksaan Fisik");
        row = baris2(form, row, "APGAR Score *", tApgar, "Score Down", tDown);
        row = baris2(form, row, "Suhu *", tSuhu, "RR *", tRR);
        row = baris2(form, row, "Nadi *", tNadi, "Tingkat Kesadaran", tKesadaran);
        row = baris2(form, row, "BB (gr) *", tBB, "PB (cm) *", tPB);
        row = grup(form, row, "Tangisan", grpTangisan.panel);
        row = baris1(form, row, "CRT", cmbCrt);
        row = grup(form, row, "Kulit", grpKulit.panel);
        row = baris1(form, row, "Lingkar Kepala", tLingkarKepala);
        row = grup(form, row, "Ubun-ubun", grpUbun.panel);
        row = grup(form, row, "Mata & Penglihatan", grpMata.panel);
        row = grup(form, row, "Hidung & Penciuman", grpHidung.panel);
        row = grup(form, row, "Telinga & Pendengaran", grpTelinga.panel);
        row = grup(form, row, "Mulut", grpMulut.panel);
        row = baris1(form, row, "Frekuensi Nafas (x/m)", tFreqNafas);
        row = grup(form, row, "Dada, Pernafasan & Sirkulasi", grpDada.panel);
        row = grup(form, row, "Abdomen", grpAbdomen.panel);
        row = baris2(form, row, "Jenis Kelamin", cmbJK, "Testis", cmbTestis);
        row = baris2(form, row, "Labia Mayora", cmbLabia, "Anus", cmbAnus);
        row = baris2(form, row, "BAB (Frekuensi)", tBabFreq, "BAK (Warna/Frekuensi)", tBak);
        row = grup(form, row, "BAB", grpBab.panel);
        row = grup(form, row, "Ekstremitas Atas", grpEkstrAtas.panel);
        row = grup(form, row, "Ekstremitas Bawah", grpEkstrBawah.panel);
        row = baris2(form, row, "Reflek Moro", cmbReflekMoro, "Reflek Mengisap", cmbReflekMengisap);
        row = baris2(form, row, "Reflek Babinski", tBabinski, "Reflek Rooting", cmbReflekRooting);
        row = grup(form, row, "Aktifitas & Istirahat", grpAktifitas.panel);
        row = grup(form, row, "Personal Hygiene", grpHygiene.panel);
        row = grup(form, row, "Nutrisi", grpNutrisi.panel);
        row = baris2(form, row, "Kontak Ibu dengan Bayi", cmbKontakIbu, "Bayi Diharapkan", cmbBayiDiharapkan);
        row = area(form, row, "Obat / Therapi", taObat);

        row = judul(form, row, "Cap Telapak Kaki Bayi");
        row = capKaki(form, row);

        row = judul(form, row, "Kriteria Discharge Planning & Perencanaan Pulang");
        row = grup(form, row, "Kriteria", panelHambatan());
        row = baris2(form, row, "Pasien Tinggal Dengan", cmbTinggal, "Sebutkan", tTinggalSebut);
        row = baris2(form, row, "Keluarga Perokok", cmbPerokok, "Sebutkan", tPerokokSebut);
        row = grup(form, row, "Kondisi Rumah", grpKondisiRumah.panel);
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
        BtnBaru.addActionListener(e -> emptTeks());
        BtnSimpan.addActionListener(e -> simpan());
        BtnHapus.addActionListener(e -> hapus());
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
