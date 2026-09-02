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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Ringkasan Riwayat Masuk dan Keluar Rumah Sakit (RM 2a).
 * Dibuka dari tab "Penilaian Awal" di DlgRawatInap. Sebagian besar isian
 * ditarik otomatis dari data pasien/registrasi saat form dibuka, tapi tetap
 * bisa diedit manual sebelum disimpan (mis. hasil tarik tidak selalu pas
 * dengan kategori checkbox di kertas RM 2a).
 */
public final class RMRingkasanRiwayatMasuk extends JDialog {

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();

    // Header identitas (readonly, dari pasien/reg_periksa)
    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TJK = ro();
    private final widget.TextBox TTglLahir = ro();

    // Isian RM 2a
    private final widget.TextBox tNoKtp = tf();
    private final widget.TextBox tNoAsuransi = tf();
    private final widget.ComboBox cmbPerkawinan = cmb("-", "Kawin", "Belum Kawin", "Janda/Duda");
    private final widget.TextBox tPetugasTPP = ro();
    private final JLabel lblTtdPetugasTPP = new JLabel();
    private String petugasTppNip = "";
    private final Map<String, ImageIcon> cacheFotoTtd = new HashMap<>();
    private final widget.ComboBox cmbSuku = cmb("-", "Banjar", "Jawa", "Dayak", "Lainnya");
    private final widget.TextBox tSukuLainnya = tf();
    private final widget.ComboBox cmbCaraMasuk = cmb("-", "Dokter Luar", "Paramedis", "RS Pemerintah",
            "Puskesmas", "Datang Sendiri", "RS Lain/Klinik", "Rujukan");
    private final widget.ComboBox cmbAgama = cmb("-", "Islam", "Kristen", "Katolik", "Hindu", "Budha", "Kong Hu Cu");
    private final widget.ComboBox cmbGolDarah = cmb("-", "A", "B", "O", "AB");
    private final widget.ComboBox cmbPendidikan = cmb("-", "Belum Sekolah", "Tidak Sekolah", "SD", "SMP", "SMA",
            "DIII", "S1", "S2");
    private final widget.TextArea taAlamat = ta();
    private final widget.TextBox tTelepon = tf();
    private final widget.ComboBox cmbPekerjaan = cmb("-", "PNS", "TNI/POLRI", "Swasta", "Honorer", "Pensiunan",
            "Wiraswasta", "IRT", "Buruh", "Lainnya");
    private final widget.TextBox tPekerjaanAsli = ro();
    private final widget.TextBox tVerifikasi = tf();
    private final widget.TextBox tRiwayatKe = ro();
    private final widget.TextBox tTglMasuk = ro();
    private final widget.TextBox tJamMasuk = ro();
    private final widget.TextBox tRuanganUnit = ro();
    private final widget.TextBox tKelas = ro();
    private final widget.TextArea taDiagnosaMasuk = ta();
    private final widget.TextBox tKodeDiagnosa = tf();
    private final JPopupMenu popupDiagnosa = new JPopupMenu();
    private final DefaultListModel<String> modelSaranDiagnosa = new DefaultListModel<>();
    private final JList<String> listSaranDiagnosa = new JList<>(modelSaranDiagnosa);
    private final List<String[]> dataSaranDiagnosa = new ArrayList<>();
    private final Timer timerCariDiagnosa = new Timer(300, e -> cariSaranDiagnosa());
    private boolean sedangPilihDiagnosa = false;
    private final widget.TextBox tPerawatRuangan = tf();
    private final widget.TextBox tDokterMerawat = tf();

    private final widget.Button BtnBaru = new widget.Button();
    private final widget.Button BtnSimpan = new widget.Button();
    private final widget.Button BtnHapus = new widget.Button();
    private final widget.Button BtnCetak = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();

    public RMRingkasanRiwayatMasuk(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Ringkasan Riwayat Masuk dan Keluar Rumah Sakit (RM 2a) ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        ensureTable();
        ensureKolomNip();
        initComponents();
        setSize(1180, 780);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(parent);
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
        JLabel judulUtama = new JLabel("Ringkasan Riwayat Masuk Rumah Sakit");
        judulUtama.setFont(new Font("Tahoma", Font.BOLD, 20));
        judulUtama.setForeground(teks);
        JLabel subjudul = new JLabel("Form RM 2a  •  Data masuk pasien rawat inap");
        subjudul.setFont(new Font("Tahoma", Font.PLAIN, 12));
        subjudul.setForeground(new Color(92, 107, 119));
        blokJudul.add(judulUtama);
        blokJudul.add(Box.createVerticalStrut(3));
        blokJudul.add(subjudul);
        barisJudul.add(blokJudul, BorderLayout.WEST);

        JLabel statusOtomatis = new JLabel("  Data ditarik otomatis  ");
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

        JPanel kependudukan = halaman("1. Data Kependudukan & Sosial", utama, latar);
        JPanel identitas = kartu("Identitas & Kependudukan", teks, garis);
        int row = 0;
        row = pasanganVertikal(identitas, row, "No. Kartu Identitas", tNoKtp,
                "No. Keanggotaan Asuransi", tNoAsuransi);
        row = pasanganVertikal(identitas, row, "Status Perkawinan", cmbPerkawinan,
                "Golongan Darah", cmbGolDarah);
        row = pasanganVertikal(identitas, row, "Suku/Bangsa", cmbSuku,
                "Suku Lainnya (bila pilih Lainnya)", tSukuLainnya);
        kependudukan.add(identitas);
        kependudukan.add(Box.createVerticalStrut(10));

        JPanel sosial = kartu("Informasi Sosial & Kontak", teks, garis);
        row = 0;
        row = pasanganVertikal(sosial, row, "Agama", cmbAgama,
                "Pendidikan Pasien", cmbPendidikan);
        row = pasanganVertikal(sosial, row, "Pekerjaan Pasien/Ortu/PJ", cmbPekerjaan,
                "Data Pekerjaan dari Master Pasien", tPekerjaanAsli);
        row = areaVertikal(sosial, row, "Alamat Lengkap", taAlamat);
        row = tunggalVertikal(sosial, row, "No. Telepon/Handphone", tTelepon);
        kependudukan.add(sosial);
        kependudukan.add(Box.createVerticalGlue());

        JPanel masuk = halaman("2. Informasi Masuk Rumah Sakit", utama, latar);
        JPanel detailMasuk = kartu("Detail Penerimaan Pasien", teks, garis);
        row = 0;
        row = pasanganVertikal(detailMasuk, row, "Cara Masuk RS/Kiriman Dari", cmbCaraMasuk,
                "Riwayat Rawat Inap yang ke", tRiwayatKe);
        row = pasanganVertikal(detailMasuk, row, "Tanggal Masuk", tTglMasuk,
                "Jam Masuk", tJamMasuk);
        row = pasanganVertikal(detailMasuk, row, "Ruangan/Unit", tRuanganUnit,
                "Kelas", tKelas);
        masuk.add(detailMasuk);
        masuk.add(Box.createVerticalStrut(10));

        JPanel klinis = kartu("Diagnosa & Tenaga yang Menerima", teks, garis);
        row = 0;
        row = areaVertikal(klinis, row, "Diagnosa Masuk", taDiagnosaMasuk);
        row = tunggalVertikal(klinis, row, "Kode Diagnosa (ICD-10)", tKodeDiagnosa);
        row = pasanganVertikal(klinis, row, "Nama Dokter yang Merawat", tDokterMerawat,
                "Nama Perawat Ruangan yang Menerima", tPerawatRuangan);
        masuk.add(klinis);
        masuk.add(Box.createVerticalGlue());

        JPanel verifikasi = halaman("3. Verifikasi", utama, latar);
        JPanel kartuVerifikasi = kartu("Persetujuan & Verifikasi", teks, garis);
        row = 0;
        row = pasanganVertikal(kartuVerifikasi, row, "Nama & TTD Petugas TPP 24 Jam", bungkusFotoTtd(tPetugasTPP, lblTtdPetugasTPP),
                "Verifikasi oleh Pasien/Penanggung Jawab", tVerifikasi);
        JLabel infoVerifikasi = new JLabel(
                "<html>Pastikan nama petugas dan pihak yang melakukan verifikasi telah sesuai sebelum menyimpan.</html>");
        infoVerifikasi.setForeground(new Color(74, 91, 104));
        infoVerifikasi.setBorder(new EmptyBorder(10, 8, 8, 8));
        GridBagConstraints gi = gc(0, (row * 2) + 1, 4, 1.0);
        kartuVerifikasi.add(infoVerifikasi, gi);
        verifikasi.add(kartuVerifikasi);
        verifikasi.add(Box.createVerticalGlue());

        isiHalaman.add(bungkusScroll(kependudukan), "KEPENDUDUKAN");
        isiHalaman.add(bungkusScroll(masuk), "MASUK");
        isiHalaman.add(bungkusScroll(verifikasi), "VERIFIKASI");

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

        String[] namaMenu = {"1  Data Kependudukan", "2  Informasi Masuk RS", "3  Verifikasi"};
        String[] kunciMenu = {"KEPENDUDUKAN", "MASUK", "VERIFIKASI"};
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
        JLabel infoNav = new JLabel("<html><span style='color:#00858F'>●</span> Field abu-abu berasal dari registrasi</html>");
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

        initAutocompleteDiagnosa();
    }

    /**
     * Autocomplete ICD-10 memakai master penyakit yang sama dengan tabel
     * Diagnosa pada DlgRawatJalan/DlgIGD.
     */
    private void initAutocompleteDiagnosa() {
        timerCariDiagnosa.setRepeats(false);
        popupDiagnosa.setFocusable(false);
        listSaranDiagnosa.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSaranDiagnosa.setVisibleRowCount(8);
        listSaranDiagnosa.setFont(new Font("Tahoma", Font.PLAIN, 12));
        listSaranDiagnosa.setFocusable(false);
        JScrollPane scrollSaran = new JScrollPane(listSaranDiagnosa);
        scrollSaran.setFocusable(false);
        scrollSaran.setPreferredSize(new Dimension(650, 190));
        popupDiagnosa.setBorder(BorderFactory.createLineBorder(new Color(142, 205, 210)));
        popupDiagnosa.add(scrollSaran);

        tKodeDiagnosa.getDocument().addDocumentListener(new DocumentListener() {
            private void berubah() {
                if(sedangPilihDiagnosa || !tKodeDiagnosa.isFocusOwner()){
                    return;
                }
                timerCariDiagnosa.restart();
            }
            @Override public void insertUpdate(DocumentEvent e) { berubah(); }
            @Override public void removeUpdate(DocumentEvent e) { berubah(); }
            @Override public void changedUpdate(DocumentEvent e) { berubah(); }
        });

        tKodeDiagnosa.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_DOWN && popupDiagnosa.isVisible()){
                    int berikutnya=Math.min(listSaranDiagnosa.getSelectedIndex()+1,modelSaranDiagnosa.size()-1);
                    listSaranDiagnosa.setSelectedIndex(Math.max(0,berikutnya));
                    listSaranDiagnosa.ensureIndexIsVisible(listSaranDiagnosa.getSelectedIndex());
                    e.consume();
                }else if(e.getKeyCode()==KeyEvent.VK_UP && popupDiagnosa.isVisible()){
                    int sebelumnya=Math.max(0,listSaranDiagnosa.getSelectedIndex()-1);
                    listSaranDiagnosa.setSelectedIndex(sebelumnya);
                    listSaranDiagnosa.ensureIndexIsVisible(sebelumnya);
                    e.consume();
                }else if(e.getKeyCode()==KeyEvent.VK_ENTER && popupDiagnosa.isVisible()){
                    pilihSaranDiagnosa();
                    e.consume();
                }else if(e.getKeyCode()==KeyEvent.VK_ESCAPE){
                    popupDiagnosa.setVisible(false);
                }
            }
        });

        listSaranDiagnosa.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if(e.getClickCount()>=1){
                    pilihSaranDiagnosa();
                }
            }
        });
    }

    private void cariSaranDiagnosa() {
        String keyword=tKodeDiagnosa.getText().trim();
        modelSaranDiagnosa.clear();
        dataSaranDiagnosa.clear();
        if(keyword.length()<1 || !tKodeDiagnosa.isShowing()){
            popupDiagnosa.setVisible(false);
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select kd_penyakit,nm_penyakit from penyakit "
                + "where kd_penyakit like ? or nm_penyakit like ? "
                + "order by case when kd_penyakit like ? then 0 else 1 end,kd_penyakit limit 25")) {
            ps.setString(1,"%"+keyword+"%");
            ps.setString(2,"%"+keyword+"%");
            ps.setString(3,keyword+"%");
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()){
                    String kode=rs.getString("kd_penyakit");
                    String nama=rs.getString("nm_penyakit");
                    dataSaranDiagnosa.add(new String[]{kode,nama});
                    modelSaranDiagnosa.addElement(kode+"  -  "+nama);
                }
            }
        } catch (Exception e) {
            System.out.println("Notif autocomplete ICD-10 RM 2a : "+e);
        }
        if(modelSaranDiagnosa.isEmpty()){
            popupDiagnosa.setVisible(false);
        }else{
            listSaranDiagnosa.setSelectedIndex(0);
            popupDiagnosa.show(tKodeDiagnosa,0,tKodeDiagnosa.getHeight());
            // JPopupMenu pada beberapa Look & Feel mengambil fokus ketika tampil.
            // Kembalikan fokus/caret supaya petugas dapat terus mengetik keyword.
            tKodeDiagnosa.requestFocusInWindow();
        }
    }

    private void pilihSaranDiagnosa() {
        int index=listSaranDiagnosa.getSelectedIndex();
        if(index<0 || index>=dataSaranDiagnosa.size()){
            return;
        }
        sedangPilihDiagnosa=true;
        try{
            String[] pilihan=dataSaranDiagnosa.get(index);
            tKodeDiagnosa.setText(pilihan[0]);
            taDiagnosaMasuk.setText(pilihan[1]);
            popupDiagnosa.setVisible(false);
            taDiagnosaMasuk.requestFocus();
        }finally{
            sedangPilihDiagnosa=false;
        }
    }

    public void isCek() {
        // Boleh Simpan kalau punya izin perawat (penilaian_awal_keperawatan_ranap) ATAU izin
        // dokter (booking_operasi) -- dokter juga perlu bisa mengisi asesmen ini di lapangan.
        boolean bisa = akses.getpenilaian_awal_keperawatan_ranap() || akses.getbooking_operasi();
        BtnSimpan.setEnabled(bisa);
        BtnHapus.setEnabled(bisa);
    }

    public void emptTeks() {
        for (widget.TextBox t : new widget.TextBox[]{TNoRw, TNoRM, TPasien, TJK, TTglLahir, tNoKtp, tNoAsuransi,
            tPetugasTPP, tSukuLainnya, tTelepon, tPekerjaanAsli, tVerifikasi, tRiwayatKe, tTglMasuk, tJamMasuk,
            tRuanganUnit, tKelas, tKodeDiagnosa, tPerawatRuangan, tDokterMerawat}) {
            t.setText("");
        }
        for (widget.TextArea a : new widget.TextArea[]{taAlamat, taDiagnosaMasuk}) {
            a.setText("");
        }
        for (widget.ComboBox c : new widget.ComboBox[]{cmbPerkawinan, cmbSuku, cmbCaraMasuk, cmbAgama, cmbGolDarah,
            cmbPendidikan, cmbPekerjaan}) {
            c.setSelectedIndex(0);
        }
    }

    /** Dipanggil dari DlgRawatInap tab Penilaian Awal. Tarik data pasien/reg lalu timpa dengan data tersimpan bila ada. */
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
                + "ifnull(p.no_ktp,'') as no_ktp,ifnull(p.no_peserta,'') as no_peserta,ifnull(p.stts_nikah,'') as stts_nikah,"
                + "ifnull(sb.nama_suku_bangsa,'') as suku_bangsa,ifnull(p.agama,'') as agama,ifnull(p.gol_darah,'') as gol_darah,"
                + "ifnull(p.pnd,'') as pnd,ifnull(p.pekerjaan,'') as pekerjaan,ifnull(p.no_tlp,'') as no_tlp,"
                + "concat(ifnull(p.alamat,''),"
                + "if(kelurahan.nm_kel is null or kelurahan.nm_kel='','',concat(', ',kelurahan.nm_kel)),"
                + "if(kecamatan.nm_kec is null or kecamatan.nm_kec='','',concat(', ',kecamatan.nm_kec)),"
                + "if(kabupaten.nm_kab is null or kabupaten.nm_kab='','',concat(', ',kabupaten.nm_kab))) as alamat_lengkap,"
                + "date_format(rp.tgl_registrasi,'%d-%m-%Y') as tgl_masuk,rp.jam_reg,"
                + "ifnull(dokter.nm_dokter,'') as nm_dokter "
                + "from reg_periksa rp inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis "
                + "left join kelurahan on p.kd_kel=kelurahan.kd_kel "
                + "left join kecamatan on p.kd_kec=kecamatan.kd_kec "
                + "left join kabupaten on p.kd_kab=kabupaten.kd_kab "
                + "left join suku_bangsa sb on p.suku_bangsa=sb.id "
                + "left join dokter on rp.kd_dokter=dokter.kd_dokter "
                + "where rp.no_rawat=?")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    TJK.setText("L".equalsIgnoreCase(rs.getString("jk")) ? "Laki-Laki" : "Perempuan");
                    TTglLahir.setText(rs.getString("tgl_lahir"));
                    tNoKtp.setText(rs.getString("no_ktp"));
                    tNoAsuransi.setText(rs.getString("no_peserta"));
                    cmbPerkawinan.setSelectedItem(petakanPerkawinan(rs.getString("stts_nikah")));
                    petakanSuku(rs.getString("suku_bangsa"));
                    cmbAgama.setSelectedItem(cocokkanOpsi(cmbAgama, rs.getString("agama")));
                    cmbGolDarah.setSelectedItem(cocokkanOpsi(cmbGolDarah, rs.getString("gol_darah")));
                    cmbPendidikan.setSelectedItem(petakanPendidikan(rs.getString("pnd")));
                    taAlamat.setText(rs.getString("alamat_lengkap"));
                    tTelepon.setText(rs.getString("no_tlp"));
                    tPekerjaanAsli.setText(rs.getString("pekerjaan"));
                    cmbPekerjaan.setSelectedItem(petakanPekerjaan(rs.getString("pekerjaan")));
                    tTglMasuk.setText(rs.getString("tgl_masuk"));
                    tJamMasuk.setText(rs.getString("jam_reg"));
                    tDokterMerawat.setText(rs.getString("nm_dokter"));
                    tPetugasTPP.setText(Sequel.cariIsi("select nama from petugas where nip=?", akses.getkode()));
                    petugasTppNip = akses.getkode();
                    lblTtdPetugasTPP.setIcon(ambilFotoTtd(petugasTppNip));
                    tPerawatRuangan.setText(Sequel.cariIsi("select nama from petugas where nip=?", akses.getkode()));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik data pasien ringkasan riwayat masuk : " + e);
        }

        try (PreparedStatement ps = koneksi.prepareStatement(
                "select ifnull(bangsal.nm_bangsal,'') as ruang,ifnull(kamar.kelas,'') as kelas,"
                + "ifnull(kamar_inap.diagnosa_awal,'') as diagnosa_awal "
                + "from kamar_inap inner join kamar on kamar.kd_kamar=kamar_inap.kd_kamar "
                + "inner join bangsal on bangsal.kd_bangsal=kamar.kd_bangsal "
                + "where kamar_inap.no_rawat=? order by kamar_inap.tgl_masuk desc limit 1")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tRuanganUnit.setText(rs.getString("ruang"));
                    tKelas.setText(rs.getString("kelas"));
                    taDiagnosaMasuk.setText(rs.getString("diagnosa_awal"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik kamar ringkasan riwayat masuk : " + e);
        }

        try (PreparedStatement ps = koneksi.prepareStatement(
                "select penyakit.nm_penyakit,penyakit.kd_penyakit from diagnosa_pasien "
                + "inner join penyakit on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit "
                + "where diagnosa_pasien.no_rawat=? and diagnosa_pasien.status='Ranap' "
                + "order by diagnosa_pasien.prioritas limit 1")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    taDiagnosaMasuk.setText(rs.getString("nm_penyakit"));
                    tKodeDiagnosa.setText(rs.getString("kd_penyakit"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik diagnosa ringkasan riwayat masuk : " + e);
        }

        String norm = TNoRM.getText();
        if (!norm.trim().equals("")) {
            int ke = Sequel.cariInteger(
                    "select count(*) from reg_periksa where no_rkm_medis='" + norm + "' "
                    + "and status_lanjut='Ranap' and no_rawat<='" + norawat + "'");
            tRiwayatKe.setText(String.valueOf(Math.max(ke, 1)));
        }
    }

    private void muatDataJikaAda(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select * from ringkasan_riwayat_masuk where no_rawat=?")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tNoKtp.setText(nvl(rs.getString("no_ktp")));
                    tNoAsuransi.setText(nvl(rs.getString("no_asuransi")));
                    cmbPerkawinan.setSelectedItem(cocokkanOpsi(cmbPerkawinan, rs.getString("perkawinan")));
                    tPetugasTPP.setText(nvl(rs.getString("petugas_tpp")));
                    if (!nvl(rs.getString("petugas_tpp_nip")).equals("")) {
                        petugasTppNip = rs.getString("petugas_tpp_nip");
                        lblTtdPetugasTPP.setIcon(ambilFotoTtd(petugasTppNip));
                    }
                    cmbSuku.setSelectedItem(cocokkanOpsi(cmbSuku, rs.getString("suku_bangsa")));
                    tSukuLainnya.setText(nvl(rs.getString("suku_lainnya")));
                    cmbCaraMasuk.setSelectedItem(cocokkanOpsi(cmbCaraMasuk, rs.getString("cara_masuk")));
                    cmbAgama.setSelectedItem(cocokkanOpsi(cmbAgama, rs.getString("agama")));
                    cmbGolDarah.setSelectedItem(cocokkanOpsi(cmbGolDarah, rs.getString("gol_darah")));
                    cmbPendidikan.setSelectedItem(cocokkanOpsi(cmbPendidikan, rs.getString("pendidikan")));
                    taAlamat.setText(nvl(rs.getString("alamat_lengkap")));
                    tTelepon.setText(nvl(rs.getString("no_telpon")));
                    cmbPekerjaan.setSelectedItem(cocokkanOpsi(cmbPekerjaan, rs.getString("pekerjaan")));
                    tVerifikasi.setText(nvl(rs.getString("verifikasi_oleh")));
                    if (rs.getObject("riwayat_ke") != null) {
                        tRiwayatKe.setText(String.valueOf(rs.getInt("riwayat_ke")));
                    }
                    tRuanganUnit.setText(nvl(rs.getString("ruangan_unit")));
                    tKelas.setText(nvl(rs.getString("kelas")));
                    taDiagnosaMasuk.setText(nvl(rs.getString("diagnosa_masuk")));
                    tKodeDiagnosa.setText(nvl(rs.getString("kode_diagnosa")));
                    tPerawatRuangan.setText(nvl(rs.getString("perawat_ruangan")));
                    if (!nvl(rs.getString("dokter_merawat")).equals("")) {
                        tDokterMerawat.setText(rs.getString("dokter_merawat"));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat ringkasan riwayat masuk : " + e);
        }
    }

    private void simpan() {
        if (ambil(TNoRw).equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        Integer riwayatKe = null;
        try {
            riwayatKe = Integer.parseInt(ambil(tRiwayatKe));
        } catch (Exception ignore) {
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into ringkasan_riwayat_masuk (no_rawat,no_ktp,no_asuransi,perkawinan,petugas_tpp,petugas_tpp_nip,"
                + "suku_bangsa,suku_lainnya,cara_masuk,agama,gol_darah,pendidikan,alamat_lengkap,no_telpon,"
                + "pekerjaan,pekerjaan_asli,verifikasi_oleh,riwayat_ke,ruangan_unit,kelas,diagnosa_masuk,"
                + "kode_diagnosa,perawat_ruangan,dokter_merawat,updated_by,updated_at,created_by,created_at) "
                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,now(),?,now()) "
                + "on duplicate key update no_ktp=values(no_ktp),no_asuransi=values(no_asuransi),"
                + "perkawinan=values(perkawinan),petugas_tpp=values(petugas_tpp),petugas_tpp_nip=values(petugas_tpp_nip),"
                + "suku_bangsa=values(suku_bangsa),suku_lainnya=values(suku_lainnya),cara_masuk=values(cara_masuk),"
                + "agama=values(agama),gol_darah=values(gol_darah),pendidikan=values(pendidikan),"
                + "alamat_lengkap=values(alamat_lengkap),no_telpon=values(no_telpon),pekerjaan=values(pekerjaan),"
                + "pekerjaan_asli=values(pekerjaan_asli),verifikasi_oleh=values(verifikasi_oleh),"
                + "riwayat_ke=values(riwayat_ke),ruangan_unit=values(ruangan_unit),kelas=values(kelas),"
                + "diagnosa_masuk=values(diagnosa_masuk),kode_diagnosa=values(kode_diagnosa),"
                + "perawat_ruangan=values(perawat_ruangan),dokter_merawat=values(dokter_merawat),"
                + "updated_by=values(updated_by),updated_at=now()")) {
            int i = 1;
            ps.setString(i++, ambil(TNoRw));
            ps.setString(i++, ambil(tNoKtp));
            ps.setString(i++, ambil(tNoAsuransi));
            ps.setString(i++, s(cmbPerkawinan));
            ps.setString(i++, ambil(tPetugasTPP));
            ps.setString(i++, petugasTppNip);
            ps.setString(i++, s(cmbSuku));
            ps.setString(i++, ambil(tSukuLainnya));
            ps.setString(i++, s(cmbCaraMasuk));
            ps.setString(i++, s(cmbAgama));
            ps.setString(i++, s(cmbGolDarah));
            ps.setString(i++, s(cmbPendidikan));
            ps.setString(i++, ambil(taAlamat));
            ps.setString(i++, ambil(tTelepon));
            ps.setString(i++, s(cmbPekerjaan));
            ps.setString(i++, ambil(tPekerjaanAsli));
            ps.setString(i++, ambil(tVerifikasi));
            if (riwayatKe == null) {
                ps.setNull(i++, java.sql.Types.INTEGER);
            } else {
                ps.setInt(i++, riwayatKe);
            }
            ps.setString(i++, ambil(tRuanganUnit));
            ps.setString(i++, ambil(tKelas));
            ps.setString(i++, ambil(taDiagnosaMasuk));
            ps.setString(i++, ambil(tKodeDiagnosa));
            ps.setString(i++, ambil(tPerawatRuangan));
            ps.setString(i++, ambil(tDokterMerawat));
            ps.setString(i++, akses.getkode());
            ps.setString(i++, akses.getkode());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Ringkasan riwayat masuk tersimpan.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan.\n" + e.getMessage());
        }
    }

    private void hapus() {
        if (ambil(TNoRw).equals("")) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus ringkasan riwayat masuk untuk No.Rawat " + ambil(TNoRw) + " ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement("delete from ringkasan_riwayat_masuk where no_rawat=?")) {
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
        RMRingkasanRiwayatMasuk f = new RMRingkasanRiwayatMasuk(null, false);
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
        if (Sequel.cariInteger("select count(*) from ringkasan_riwayat_masuk where no_rawat=?", ambil(TNoRw)) == 0) {
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
            String sql = "select rrm.no_ktp,rrm.no_asuransi,rrm.perkawinan,rrm.petugas_tpp,rrm.petugas_tpp_nip,"
                    + "rrm.suku_bangsa,rrm.suku_lainnya,rrm.cara_masuk,rrm.agama,rrm.gol_darah,rrm.pendidikan,"
                    + "rrm.alamat_lengkap,rrm.no_telpon,rrm.pekerjaan,rrm.verifikasi_oleh,rrm.riwayat_ke,"
                    + "rrm.ruangan_unit,rrm.kelas,rrm.diagnosa_masuk,rrm.kode_diagnosa,rrm.perawat_ruangan,rrm.dokter_merawat,"
                    + "p.no_rkm_medis,p.nm_pasien,if(p.jk='L','Laki-laki','Perempuan') as jk,"
                    + "ifnull(date_format(p.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir,"
                    + "ifnull(date_format(rp.tgl_registrasi,'%d-%m-%Y'),'') as tgl_masuk,ifnull(rp.jam_reg,'') as jam_masuk,"
                    + fotoSqlByNip("rrm.petugas_tpp_nip", "petugas_tpp_photo") + " "
                    + "from ringkasan_riwayat_masuk rrm "
                    + "inner join reg_periksa rp on rrm.no_rawat=rp.no_rawat "
                    + "inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis "
                    + "where rrm.no_rawat='" + ambil(TNoRw) + "'";
            Valid.MyReportqry("rptRingkasanRiwayatMasuk.jasper", "report",
                    "::[ Ringkasan Riwayat Masuk dan Keluar Rumah Sakit (RM 2a) ]::", sql, param);
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
                "create table if not exists ringkasan_riwayat_masuk ("
                + "no_rawat varchar(17) not null primary key,"
                + "no_ktp varchar(20) null,"
                + "no_asuransi varchar(25) null,"
                + "perkawinan varchar(20) null,"
                + "petugas_tpp varchar(60) null,"
                + "suku_bangsa varchar(30) null,"
                + "suku_lainnya varchar(60) null,"
                + "cara_masuk varchar(30) null,"
                + "agama varchar(20) null,"
                + "gol_darah varchar(5) null,"
                + "pendidikan varchar(30) null,"
                + "alamat_lengkap text null,"
                + "no_telpon varchar(40) null,"
                + "pekerjaan varchar(30) null,"
                + "pekerjaan_asli varchar(60) null,"
                + "petugas_tpp_nip varchar(20) null,"
                + "verifikasi_oleh varchar(60) null,"
                + "riwayat_ke int null,"
                + "ruangan_unit varchar(60) null,"
                + "kelas varchar(20) null,"
                + "diagnosa_masuk text null,"
                + "kode_diagnosa varchar(20) null,"
                + "perawat_ruangan varchar(60) null,"
                + "dokter_merawat varchar(60) null,"
                + "created_by varchar(50) null,"
                + "updated_by varchar(50) null,"
                + "created_at datetime null,"
                + "updated_at datetime null"
                + ")");
    }

    /** Kolom NIP ditambah belakangan (dipakai utk tarik foto TTD dari pegawai) -- ALTER manual krn table sudah ada di instalasi lama. */
    private void ensureKolomNip() {
        try {
            if (Sequel.cariInteger("select count(*) from information_schema.columns where table_schema=database() "
                    + "and table_name='ringkasan_riwayat_masuk' and column_name='petugas_tpp_nip'") == 0) {
                Sequel.queryu2("alter table ringkasan_riwayat_masuk add column petugas_tpp_nip varchar(20) null after pekerjaan_asli");
            }
        } catch (Exception e) {
            System.out.println("Notif kolom nip ringkasan riwayat masuk : " + e);
        }
    }

    /** Bungkus field readonly + label foto TTD kecil di sebelah kanan, dipakai oleh baris "Nama & TTD Petugas". */
    private JPanel bungkusFotoTtd(Component field, JLabel lblFoto) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        p.add(field, BorderLayout.CENTER);
        lblFoto.setPreferredSize(new Dimension(60, 28));
        p.add(lblFoto, BorderLayout.EAST);
        return p;
    }

    /** Foto TTD petugas berdasarkan NIP, ditarik dari pegawai.photo -- pola sama seperti ambilParafIcon di RMAsesmenUlangNyeri. Di-cache per NIP. */
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

    // ====================== Pemetaan enum data pasien -> kategori RM 2a ======================
    private static String petakanPerkawinan(String v) {
        if (v == null) { return "-"; }
        switch (v.trim().toUpperCase()) {
            case "MENIKAH": return "Kawin";
            case "BELUM MENIKAH": case "JOMBLO": return "Belum Kawin";
            case "JANDA": case "DUDHA": return "Janda/Duda";
            default: return "-";
        }
    }

    private static String petakanPendidikan(String v) {
        if (v == null) { return "-"; }
        switch (v.trim().toUpperCase()) {
            case "TS": return "Tidak Sekolah";
            case "TK": return "Belum Sekolah";
            case "SD": return "SD";
            case "SMP": return "SMP";
            case "SMA": case "SLTA/SEDERAJAT": return "SMA";
            case "D1": case "D2": case "D3": case "D4": return "DIII";
            case "S1": return "S1";
            case "S2": case "S3": return "S2";
            default: return "-";
        }
    }

    private void petakanSuku(String namaSuku) {
        if (namaSuku == null || namaSuku.trim().equals("") || namaSuku.trim().equals("-")) {
            cmbSuku.setSelectedItem("-");
            return;
        }
        String n = namaSuku.trim();
        if (n.equalsIgnoreCase("Banjar") || n.equalsIgnoreCase("Jawa") || n.equalsIgnoreCase("Dayak")) {
            cmbSuku.setSelectedItem(n.substring(0, 1).toUpperCase() + n.substring(1).toLowerCase());
        } else {
            cmbSuku.setSelectedItem("Lainnya");
            tSukuLainnya.setText(n);
        }
    }

    private static String petakanPekerjaan(String v) {
        if (v == null || v.trim().equals("")) { return "-"; }
        String n = v.trim().toUpperCase();
        if (n.contains("PNS")) { return "PNS"; }
        if (n.contains("TNI") || n.contains("POLRI")) { return "TNI/POLRI"; }
        if (n.contains("HONORER")) { return "Honorer"; }
        if (n.contains("PENSIUN")) { return "Pensiunan"; }
        if (n.contains("WIRASWASTA") || n.contains("WIRAUSAHA")) { return "Wiraswasta"; }
        if (n.contains("IRT") || n.contains("RUMAH TANGGA")) { return "IRT"; }
        if (n.contains("BURUH")) { return "Buruh"; }
        if (n.contains("SWASTA")) { return "Swasta"; }
        return "Lainnya";
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

    // ====================== Helpers UI (pola sama dengan RMAsesmenKeperawatanDewasa) ======================
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
        luar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1000));
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

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t + " :");
        l.setFont(new Font("Tahoma", Font.PLAIN, 11));
        return l;
    }

    private void siz(Component c) {
        if (c instanceof widget.TextBox || c instanceof widget.ComboBox) {
            c.setPreferredSize(new Dimension(220, 23));
        }
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
