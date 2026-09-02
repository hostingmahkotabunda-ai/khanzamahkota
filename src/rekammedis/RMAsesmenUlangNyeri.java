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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import kepegawaian.DlgCariPetugas;

/**
 * Asesmen Ulang Nyeri (RM 7.1). Berbeda dari RM 2a/RM 3a: ini bukan snapshot
 * 1 baris per no_rawat, tapi CATATAN BERULANG (setiap kali dilakukan kaji
 * ulang nyeri dicatat sebagai baris baru). Dibuka dari tab "Penilaian Awal"
 * di DlgRawatInap. Identitas pasien ditarik otomatis; setiap baris kaji ulang
 * diisi manual (data observasi baru, bukan riwayat yang bisa ditarik).
 */
public final class RMAsesmenUlangNyeri extends JDialog {

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final DefaultTableModel tabMode;

    // Header identitas (readonly)
    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TJK = ro();
    private final widget.TextBox TTglLahir = ro();

    // Baris kaji ulang: tanda vital & skor
    private final widget.Tanggal dtpTglVital = dt();
    private final widget.ComboBox cmbSkorNyeri = cmb("-", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
    private final widget.ComboBox cmbSkorSedasi = cmb("-", "S", "1", "2", "3", "4");
    private final widget.TextBox tTD = tf();
    private final widget.TextBox tNadi = tf();
    private final widget.TextBox tSuhu = tf();
    private final widget.TextBox tPernafasan = tf();
    private final widget.TextBox tPerawatVitalNama = ro();
    private final widget.TextBox kdPerawatVital = ro();
    private final widget.Button btnPerawatVital = new widget.Button();
    private final DlgCariPetugas pilihPerawatVital = new DlgCariPetugas(null, true);

    // Baris kaji ulang: intervensi
    private final widget.Tanggal dtpTglIntervensi = dt();
    private final widget.TextBox tNamaObat = tf();
    private final widget.TextBox tDosisFrekuensi = tf();
    private final widget.TextBox tRute = tf();
    private final widget.TextBox tIntervensiNonFarmakologi = tf();
    private final widget.TextBox tPerawatIntervensiNama = ro();
    private final widget.TextBox kdPerawatIntervensi = ro();
    private final widget.Button btnPerawatIntervensi = new widget.Button();
    private final DlgCariPetugas pilihPerawatIntervensi = new DlgCariPetugas(null, true);
    private final widget.TextBox tWaktuKajiUlang = tf();

    private final widget.Table tbRiwayat = new widget.Table();
    private final Map<String, ImageIcon> cacheParaf = new HashMap<>();

    private final widget.Button BtnTambah = new widget.Button();
    private final widget.Button BtnUpdate = new widget.Button();
    private final widget.Button BtnHapusBaris = new widget.Button();
    private final widget.Button BtnBersihkan = new widget.Button();
    private final widget.Button BtnCetak = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();

    private Integer idSedangDiedit = null;

    public RMAsesmenUlangNyeri(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Asesmen Ulang Nyeri (RM 7.1) ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        ensureTable();
        tabMode = new DefaultTableModel(null, new Object[]{
            "ID", "Tgl/Jam Vital", "Nyeri", "Sedasi", "TD", "Nadi", "Suhu", "RR", "Perawat Vital",
            "Tgl/Jam Obat", "Nama Obat", "Dosis/Frek", "Rute", "Non-Farmakologi", "Perawat Obat", "Kaji Ulang",
            "Paraf Vital", "Paraf Obat"
        }) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        initComponents();
        pilihPerawatVital.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) {
                JTable t = pilihPerawatVital.getTable();
                if (t.getSelectedRow() != -1) {
                    int r = t.getSelectedRow();
                    kdPerawatVital.setText(t.getValueAt(r, 0).toString());
                    tPerawatVitalNama.setText(t.getValueAt(r, 1).toString());
                }
            }
        });
        pilihPerawatIntervensi.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) {
                JTable t = pilihPerawatIntervensi.getTable();
                if (t.getSelectedRow() != -1) {
                    int r = t.getSelectedRow();
                    kdPerawatIntervensi.setText(t.getValueAt(r, 0).toString());
                    tPerawatIntervensiNama.setText(t.getValueAt(r, 1).toString());
                }
            }
        });
        setSize(1250, 820);
        setMinimumSize(new Dimension(1050, 700));
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        final Color utama = new Color(0, 133, 143);
        final Color utamaMuda = new Color(230, 247, 248);
        final Color latar = new Color(246, 249, 251);
        final Color garis = new Color(215, 224, 230);
        final Color teks = new Color(32, 49, 66);

        getContentPane().setBackground(latar);
        getContentPane().setLayout(new BorderLayout(0, 10));

        JPanel atas = new JPanel(new BorderLayout(12, 10));
        atas.setBackground(latar);
        atas.setBorder(new EmptyBorder(14, 18, 0, 18));

        JLabel judulUtama = new JLabel("Asesmen Ulang Nyeri");
        judulUtama.setFont(new Font("Tahoma", Font.BOLD, 20));
        judulUtama.setForeground(teks);
        atas.add(judulUtama, BorderLayout.NORTH);

        JPanel ringkasanPasien = new JPanel(new GridLayout(1, 5, 0, 0));
        ringkasanPasien.setBackground(Color.WHITE);
        ringkasanPasien.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis),
                new EmptyBorder(10, 12, 10, 12)));
        ringkasanPasien.add(fieldRingkasan("No. Rawat *", TNoRw));
        ringkasanPasien.add(fieldRingkasan("No. RM", TNoRM));
        ringkasanPasien.add(fieldRingkasan("Nama Pasien", TPasien));
        ringkasanPasien.add(fieldRingkasan("Jenis Kelamin", TJK));
        ringkasanPasien.add(fieldRingkasan("Tanggal Lahir", TTglLahir));
        atas.add(ringkasanPasien, BorderLayout.CENTER);
        getContentPane().add(atas, BorderLayout.NORTH);

        JPanel tengah = new JPanel();
        tengah.setBackground(latar);
        tengah.setLayout(new BoxLayout(tengah, BoxLayout.Y_AXIS));
        tengah.setBorder(new EmptyBorder(10, 18, 10, 18));

        JLabel petunjuk = new JLabel("<html>PETUNJUK &nbsp;&nbsp;•&nbsp;&nbsp; Isilah dengan tulisan yang jelas dan rapi "
                + "&nbsp;&nbsp;•&nbsp;&nbsp; Beri tanda ( √ ) pada kolom checklist "
                + "&nbsp;&nbsp;•&nbsp;&nbsp; Gunakan pulpen biru = Dokter &amp; hitam = Tenaga Medis Lainnya</html>");
        petunjuk.setFont(new Font("Tahoma", Font.PLAIN, 10));
        petunjuk.setForeground(new Color(91, 105, 115));
        petunjuk.setAlignmentX(Component.LEFT_ALIGNMENT);
        petunjuk.setBorder(new EmptyBorder(0, 0, 10, 0));
        tengah.add(petunjuk);

        JPanel panelLegenda = new JPanel(new GridBagLayout());
        panelLegenda.setOpaque(false);
        panelLegenda.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gLegenda = new GridBagConstraints();
        gLegenda.gridy = 0;
        gLegenda.weightx = 0.25;
        gLegenda.fill = GridBagConstraints.HORIZONTAL;
        gLegenda.anchor = GridBagConstraints.NORTHWEST;
        gLegenda.insets = new Insets(0, 0, 0, 10);
        gLegenda.gridx = 0;
        panelLegenda.add(kartuLegenda("Skor Nyeri", teks, garis,
                "0 : Tidak ada nyeri",
                "1 - 3 : Nyeri ringan",
                "4 - 7 : Nyeri sedang",
                "8 - 10 : Nyeri berat"), gLegenda);
        gLegenda.gridx = 1;
        panelLegenda.add(kartuLegenda("Skor Sedasi — Pasero-Mc Caffery (POSS)", teks, garis,
                "S : Tidur, mudah dibangunkan",
                "1 : Bangun dan sadar",
                "2 : Agak mengantuk, mudah dibangunkan",
                "3 : Sering mengantuk, bisa dibangunkan, mudah tertidur saat bicara",
                "4 : Somnolent, minimal/tidak respons terhadap rangsangan fisik"), gLegenda);
        gLegenda.gridx = 2;
        panelLegenda.add(kartuLegenda("Intervensi Non-Farmakologi", teks, garis,
                "1 Dingin        5 Musik",
                "2 Panas         6 TENS",
                "3 Posisi         7 Relaksasi dan Pernafasan",
                "4 Pijat"), gLegenda);
        gLegenda.gridx = 3;
        gLegenda.insets = new Insets(0, 0, 0, 0);
        panelLegenda.add(kartuLegenda("Waktu Kaji Ulang", teks, garis,
                "1. 15 menit setelah intervensi obat injeksi",
                "2. 1 jam setelah intervensi obat oral/lainnya",
                "3. 1x/shift bila skor nyeri 1 - 3",
                "4. Setiap 3 jam bila skor nyeri 4 - 7",
                "5. Setiap 1 jam bila skor nyeri 8 - 10",
                "6. Dihentikan bila skor nyeri 0"), gLegenda);
        tengah.add(panelLegenda);
        tengah.add(Box.createVerticalStrut(10));

        JPanel baris2Kartu = new JPanel(new GridLayout(1, 2, 12, 0));
        baris2Kartu.setOpaque(false);
        baris2Kartu.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel kartuVital = kartu("Tanda Vital & Skor (per kaji ulang)", teks, garis);
        int row = 0;
        row = pasanganVertikal(kartuVital, row, "Tanggal / Jam", dtpTglVital, "Skor Nyeri (0-10)", cmbSkorNyeri);
        row = pasanganVertikal(kartuVital, row, "Skor Sedasi (POSS)", cmbSkorSedasi, "Tekanan Darah", tTD);
        row = pasanganVertikal(kartuVital, row, "Nadi", tNadi, "Suhu", tSuhu);
        row = pasanganVertikal(kartuVital, row, "Pernafasan", tPernafasan,
                "Perawat/Bidan (Nama)", gabungPetugas(tPerawatVitalNama, btnPerawatVital));
        baris2Kartu.add(bungkusScrollVertikal(kartuVital));

        JPanel kartuIntervensi = kartu("Intervensi & Kaji Ulang", teks, garis);
        row = 0;
        row = pasanganVertikal(kartuIntervensi, row, "Tanggal / Jam", dtpTglIntervensi, "Nama Obat", tNamaObat);
        row = pasanganVertikal(kartuIntervensi, row, "Dosis & Frekuensi", tDosisFrekuensi, "Rute", tRute);
        row = tunggalVertikal(kartuIntervensi, row, "Intervensi Non-Farmakologi (mis. Posisi, Musik, Pijat)", tIntervensiNonFarmakologi);
        row = tunggalVertikal(kartuIntervensi, row, "Perawat/Bidan (Nama)", gabungPetugas(tPerawatIntervensiNama, btnPerawatIntervensi));
        row = tunggalVertikal(kartuIntervensi, row, "Waktu Kaji Ulang Berikutnya", tWaktuKajiUlang);
        baris2Kartu.add(bungkusScrollVertikal(kartuIntervensi));

        tengah.add(baris2Kartu);
        tengah.add(Box.createVerticalStrut(10));

        JPanel panelEntriTombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelEntriTombol.setOpaque(false);
        panelEntriTombol.setAlignmentX(Component.LEFT_ALIGNMENT);
        BtnTambah.setText("+ Tambah Baris");
        BtnUpdate.setText("Update Baris Terpilih");
        BtnHapusBaris.setText("Hapus Baris Terpilih");
        BtnBersihkan.setText("Bersihkan Form Entri");
        panelEntriTombol.add(BtnTambah);
        panelEntriTombol.add(BtnUpdate);
        panelEntriTombol.add(BtnHapusBaris);
        panelEntriTombol.add(BtnBersihkan);
        tengah.add(panelEntriTombol);
        tengah.add(Box.createVerticalStrut(10));

        JLabel judulRiwayat = new JLabel("Riwayat Kaji Ulang Nyeri");
        judulRiwayat.setFont(new Font("Tahoma", Font.BOLD, 13));
        judulRiwayat.setForeground(teks);
        judulRiwayat.setAlignmentX(Component.LEFT_ALIGNMENT);
        judulRiwayat.setBorder(new EmptyBorder(0, 0, 6, 0));
        tengah.add(judulRiwayat);

        tbRiwayat.setModel(tabMode);
        tbRiwayat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbRiwayat.setAutoResizeMode(widget.Table.AUTO_RESIZE_OFF);
        tbRiwayat.setRowHeight(36);
        tbRiwayat.getColumnModel().getColumn(16).setCellRenderer(new ParafRenderer());
        tbRiwayat.getColumnModel().getColumn(17).setCellRenderer(new ParafRenderer());
        tbRiwayat.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) { muatBarisTerpilihKeForm(); }
            }
        });
        JScrollPane scrollTabel = new JScrollPane(tbRiwayat);
        scrollTabel.setPreferredSize(new Dimension(1100, 220));
        scrollTabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        tengah.add(scrollTabel);

        getContentPane().add(tengah, BorderLayout.CENTER);

        btnPerawatVital.setText("...");
        btnPerawatVital.setPreferredSize(new Dimension(28, 23));
        btnPerawatVital.addActionListener(e -> {
            pilihPerawatVital.emptTeks();
            pilihPerawatVital.isCek();
            pilihPerawatVital.setSize(900, 540);
            pilihPerawatVital.setLocationRelativeTo(this);
            pilihPerawatVital.setVisible(true);
        });
        btnPerawatIntervensi.setText("...");
        btnPerawatIntervensi.setPreferredSize(new Dimension(28, 23));
        btnPerawatIntervensi.addActionListener(e -> {
            pilihPerawatIntervensi.emptTeks();
            pilihPerawatIntervensi.isCek();
            pilihPerawatIntervensi.setSize(900, 540);
            pilihPerawatIntervensi.setLocationRelativeTo(this);
            pilihPerawatIntervensi.setVisible(true);
        });
        BtnTambah.addActionListener(e -> tambahBaris());
        BtnUpdate.addActionListener(e -> updateBaris());
        BtnHapusBaris.addActionListener(e -> hapusBaris());
        BtnBersihkan.addActionListener(e -> bersihkanEntri());
        BtnKeluar.setText("Keluar");
        BtnKeluar.addActionListener(e -> dispose());
        BtnCetak.setText("Cetak");
        BtnCetak.addActionListener(e -> cetak());
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 9));
        bawah.setBackground(Color.WHITE);
        bawah.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, garis));
        bawah.add(BtnKeluar);
        bawah.add(BtnCetak);
        getContentPane().add(bawah, BorderLayout.SOUTH);

        dtpTglVital.setDate(new Date());
        dtpTglIntervensi.setDate(new Date());
    }

    public void isCek() {
        // Boleh Simpan kalau punya izin perawat (penilaian_awal_keperawatan_ranap) ATAU izin
        // dokter (booking_operasi) -- dokter juga perlu bisa mengisi asesmen ini di lapangan.
        boolean bisa = akses.getpenilaian_awal_keperawatan_ranap() || akses.getbooking_operasi();
        BtnTambah.setEnabled(bisa);
        BtnUpdate.setEnabled(bisa);
        BtnHapusBaris.setEnabled(bisa);
    }

    /** Dipanggil dari DlgRawatInap tab Penilaian Awal. */
    public void setNoRm(String norawat) {
        bersihkanEntri();
        if (norawat == null || norawat.trim().equals("")) {
            TNoRw.setText(""); TNoRM.setText(""); TPasien.setText(""); TJK.setText(""); TTglLahir.setText("");
            tabMode.setRowCount(0);
            return;
        }
        TNoRw.setText(norawat);
        tarikIdentitasPasien(norawat);
        muatRiwayat(norawat);
    }

    private void tarikIdentitasPasien(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select p.no_rkm_medis,p.nm_pasien,p.jk,ifnull(date_format(p.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir "
                + "from reg_periksa rp inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis "
                + "where rp.no_rawat=?")) {
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
            System.out.println("Notif tarik identitas asesmen ulang nyeri : " + e);
        }
        String namaPetugas = Sequel.cariIsi("select nama from petugas where nip=?", akses.getkode());
        kdPerawatVital.setText(akses.getkode());
        kdPerawatIntervensi.setText(akses.getkode());
        tPerawatVitalNama.setText(namaPetugas);
        tPerawatIntervensiNama.setText(namaPetugas);
    }

    private void muatRiwayat(String norawat) {
        tabMode.setRowCount(0);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select * from asesmen_ulang_nyeri where no_rawat=? order by tgl_jam_vital")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tabMode.addRow(new Object[]{
                        rs.getInt("id"),
                        fmtDatetime(rs.getString("tgl_jam_vital")),
                        nvl(rs.getString("skor_nyeri")),
                        nvl(rs.getString("skor_sedasi")),
                        nvl(rs.getString("tekanan_darah")),
                        nvl(rs.getString("nadi")),
                        nvl(rs.getString("suhu")),
                        nvl(rs.getString("pernafasan")),
                        nvl(rs.getString("perawat_vital_nama")),
                        fmtDatetime(rs.getString("tgl_jam_intervensi")),
                        nvl(rs.getString("nama_obat")),
                        nvl(rs.getString("dosis_frekuensi")),
                        nvl(rs.getString("rute")),
                        nvl(rs.getString("intervensi_non_farmakologi")),
                        nvl(rs.getString("perawat_intervensi_nama")),
                        nvl(rs.getString("waktu_kaji_ulang")),
                        nvl(rs.getString("perawat_vital_nip")),
                        nvl(rs.getString("perawat_intervensi_nip"))
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat riwayat asesmen ulang nyeri : " + e);
        }
        aturLebarKolom();
    }

    private void aturLebarKolom() {
        if (tbRiwayat.getColumnModel().getColumnCount() < 18) { return; }
        int[] lebar = {0, 110, 50, 55, 70, 60, 55, 50, 110, 110, 110, 90, 60, 130, 110, 110, 60, 60};
        for (int i = 0; i < lebar.length; i++) {
            tbRiwayat.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
        tbRiwayat.getColumnModel().getColumn(0).setMinWidth(0);
        tbRiwayat.getColumnModel().getColumn(0).setMaxWidth(0);
    }

    private void muatBarisTerpilihKeForm() {
        int r = tbRiwayat.getSelectedRow();
        if (r < 0) { return; }
        idSedangDiedit = (Integer) tabMode.getValueAt(r, 0);
        try {
            setTglJam(dtpTglVital, tabMode.getValueAt(r, 1) + "");
        } catch (Exception ignore) { }
        cmbSkorNyeri.setSelectedItem(cocokkanOpsi(cmbSkorNyeri, tabMode.getValueAt(r, 2) + ""));
        cmbSkorSedasi.setSelectedItem(cocokkanOpsi(cmbSkorSedasi, tabMode.getValueAt(r, 3) + ""));
        tTD.setText(tabMode.getValueAt(r, 4) + "");
        tNadi.setText(tabMode.getValueAt(r, 5) + "");
        tSuhu.setText(tabMode.getValueAt(r, 6) + "");
        tPernafasan.setText(tabMode.getValueAt(r, 7) + "");
        tPerawatVitalNama.setText(tabMode.getValueAt(r, 8) + "");
        try {
            setTglJam(dtpTglIntervensi, tabMode.getValueAt(r, 9) + "");
        } catch (Exception ignore) { }
        tNamaObat.setText(tabMode.getValueAt(r, 10) + "");
        tDosisFrekuensi.setText(tabMode.getValueAt(r, 11) + "");
        tRute.setText(tabMode.getValueAt(r, 12) + "");
        tIntervensiNonFarmakologi.setText(tabMode.getValueAt(r, 13) + "");
        tPerawatIntervensiNama.setText(tabMode.getValueAt(r, 14) + "");
        tWaktuKajiUlang.setText(tabMode.getValueAt(r, 15) + "");
        kdPerawatVital.setText(tabMode.getValueAt(r, 16) + "");
        kdPerawatIntervensi.setText(tabMode.getValueAt(r, 17) + "");
    }

    private void tambahBaris() {
        if (ambil(TNoRw).equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into asesmen_ulang_nyeri (no_rawat,tgl_jam_vital,skor_nyeri,skor_sedasi,tekanan_darah,"
                + "nadi,suhu,pernafasan,perawat_vital_nama,perawat_vital_nip,tgl_jam_intervensi,nama_obat,"
                + "dosis_frekuensi,rute,intervensi_non_farmakologi,perawat_intervensi_nama,perawat_intervensi_nip,"
                + "waktu_kaji_ulang,created_by,created_at) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,now())")) {
            isiStatementBaris(ps, false);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Baris kaji ulang nyeri ditambahkan.");
            bersihkanEntri();
            muatRiwayat(ambil(TNoRw));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menambah baris.\n" + e.getMessage());
        }
    }

    private void updateBaris() {
        if (idSedangDiedit == null) {
            JOptionPane.showMessageDialog(this, "Pilih baris di tabel riwayat terlebih dahulu (klik dua kali).");
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "update asesmen_ulang_nyeri set tgl_jam_vital=?,skor_nyeri=?,skor_sedasi=?,tekanan_darah=?,"
                + "nadi=?,suhu=?,pernafasan=?,perawat_vital_nama=?,perawat_vital_nip=?,tgl_jam_intervensi=?,"
                + "nama_obat=?,dosis_frekuensi=?,rute=?,intervensi_non_farmakologi=?,perawat_intervensi_nama=?,"
                + "perawat_intervensi_nip=?,waktu_kaji_ulang=? where id=?")) {
            isiStatementBaris(ps, true);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Baris kaji ulang nyeri diperbarui.");
            bersihkanEntri();
            muatRiwayat(ambil(TNoRw));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui baris.\n" + e.getMessage());
        }
    }

    private void isiStatementBaris(PreparedStatement ps, boolean modeUpdate) throws java.sql.SQLException {
        int i = 1;
        if (!modeUpdate) { ps.setString(i++, ambil(TNoRw)); }
        ps.setString(i++, ambilTglJam(dtpTglVital));
        ps.setString(i++, s(cmbSkorNyeri));
        ps.setString(i++, s(cmbSkorSedasi));
        ps.setString(i++, ambil(tTD));
        ps.setString(i++, ambil(tNadi));
        ps.setString(i++, ambil(tSuhu));
        ps.setString(i++, ambil(tPernafasan));
        ps.setString(i++, ambil(tPerawatVitalNama));
        ps.setString(i++, ambil(kdPerawatVital));
        ps.setString(i++, ambilTglJam(dtpTglIntervensi));
        ps.setString(i++, ambil(tNamaObat));
        ps.setString(i++, ambil(tDosisFrekuensi));
        ps.setString(i++, ambil(tRute));
        ps.setString(i++, ambil(tIntervensiNonFarmakologi));
        ps.setString(i++, ambil(tPerawatIntervensiNama));
        ps.setString(i++, ambil(kdPerawatIntervensi));
        ps.setString(i++, ambil(tWaktuKajiUlang));
        if (!modeUpdate) {
            ps.setString(i++, akses.getkode());
        }
        if (modeUpdate) {
            ps.setInt(i++, idSedangDiedit);
        }
    }

    private void hapusBaris() {
        if (idSedangDiedit == null) {
            JOptionPane.showMessageDialog(this, "Pilih baris di tabel riwayat terlebih dahulu (klik dua kali).");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus baris kaji ulang nyeri terpilih ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement("delete from asesmen_ulang_nyeri where id=?")) {
            ps.setInt(1, idSedangDiedit);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Baris dihapus.");
            bersihkanEntri();
            muatRiwayat(ambil(TNoRw));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus baris.\n" + e.getMessage());
        }
    }

    /** Cetak langsung dari no_rawat tanpa membuka dialog (dipakai dari klik-kanan di layar Riwayat). */
    public static void cetak(String noRawat) {
        if (noRawat == null || noRawat.trim().isEmpty()) {
            return;
        }
        RMAsesmenUlangNyeri f = new RMAsesmenUlangNyeri(null, false);
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
        if (Sequel.cariInteger("select count(*) from asesmen_ulang_nyeri where no_rawat=?", ambil(TNoRw)) == 0) {
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
            param.put("no_rkm_medis", ambil(TNoRM));
            param.put("nm_pasien", ambil(TPasien));
            param.put("jk", ambil(TJK));
            param.put("tgl_lahir", ambil(TTglLahir));
            String sql = "select ifnull(date_format(tgl_jam_vital,'%d-%m-%Y %H:%i'),'') as tgl_jam_vital,"
                    + "ifnull(skor_nyeri,'') as skor_nyeri,ifnull(skor_sedasi,'') as skor_sedasi,"
                    + "ifnull(tekanan_darah,'') as tekanan_darah,ifnull(nadi,'') as nadi,ifnull(suhu,'') as suhu,"
                    + "ifnull(pernafasan,'') as pernafasan,ifnull(perawat_vital_nama,'') as perawat_vital_nama,"
                    + "ifnull(date_format(tgl_jam_intervensi,'%d-%m-%Y %H:%i'),'') as tgl_jam_intervensi,"
                    + "ifnull(nama_obat,'') as nama_obat,ifnull(dosis_frekuensi,'') as dosis_frekuensi,ifnull(rute,'') as rute,"
                    + "ifnull(intervensi_non_farmakologi,'') as intervensi_non_farmakologi,"
                    + "ifnull(perawat_intervensi_nama,'') as perawat_intervensi_nama,ifnull(waktu_kaji_ulang,'') as waktu_kaji_ulang "
                    + "from asesmen_ulang_nyeri where no_rawat='" + ambil(TNoRw) + "' order by tgl_jam_vital";
            Valid.MyReportqry("rptAsesmenUlangNyeri.jasper", "report",
                    "::[ Asesmen Ulang Nyeri (RM 7.1) ]::", sql, param);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak.\n" + e.getMessage());
        }
    }

    private void bersihkanEntri() {
        idSedangDiedit = null;
        for (widget.TextBox t : new widget.TextBox[]{tTD, tNadi, tSuhu, tPernafasan,
            tNamaObat, tDosisFrekuensi, tRute, tIntervensiNonFarmakologi, tWaktuKajiUlang}) {
            t.setText("");
        }
        for (widget.ComboBox c : new widget.ComboBox[]{cmbSkorNyeri, cmbSkorSedasi}) {
            c.setSelectedIndex(0);
        }
        dtpTglVital.setDate(new Date());
        dtpTglIntervensi.setDate(new Date());
        String namaPetugas = Sequel.cariIsi("select nama from petugas where nip=?", akses.getkode());
        kdPerawatVital.setText(akses.getkode());
        kdPerawatIntervensi.setText(akses.getkode());
        tPerawatVitalNama.setText(namaPetugas);
        tPerawatIntervensiNama.setText(namaPetugas);
    }

    private void ensureTable() {
        Sequel.queryu2(
                "create table if not exists asesmen_ulang_nyeri ("
                + "id int not null auto_increment primary key,"
                + "no_rawat varchar(17) not null,"
                + "tgl_jam_vital datetime null,"
                + "skor_nyeri varchar(5) null,"
                + "skor_sedasi varchar(5) null,"
                + "tekanan_darah varchar(20) null,"
                + "nadi varchar(20) null,"
                + "suhu varchar(10) null,"
                + "pernafasan varchar(20) null,"
                + "perawat_vital_nama varchar(60) null,"
                + "perawat_vital_paraf varchar(30) null,"
                + "tgl_jam_intervensi datetime null,"
                + "nama_obat varchar(100) null,"
                + "dosis_frekuensi varchar(60) null,"
                + "rute varchar(40) null,"
                + "intervensi_non_farmakologi varchar(150) null,"
                + "perawat_intervensi_nama varchar(60) null,"
                + "perawat_intervensi_paraf varchar(30) null,"
                + "waktu_kaji_ulang varchar(60) null,"
                + "created_by varchar(50) null,"
                + "created_at datetime null,"
                + "index idx_no_rawat (no_rawat)"
                + ")");
        ensureKolomNip();
    }

    /** Kolom paraf lama diganti pola NIP (dipakai utk tarik foto TTD dari pegawai) -- ALTER manual krn table sudah ada di instalasi lama. */
    private void ensureKolomNip() {
        try {
            if (Sequel.cariInteger("select count(*) from information_schema.columns where table_schema=database() "
                    + "and table_name='asesmen_ulang_nyeri' and column_name='perawat_vital_nip'") == 0) {
                Sequel.queryu2("alter table asesmen_ulang_nyeri add column perawat_vital_nip varchar(20) null after perawat_vital_paraf");
            }
            if (Sequel.cariInteger("select count(*) from information_schema.columns where table_schema=database() "
                    + "and table_name='asesmen_ulang_nyeri' and column_name='perawat_intervensi_nip'") == 0) {
                Sequel.queryu2("alter table asesmen_ulang_nyeri add column perawat_intervensi_nip varchar(20) null after perawat_intervensi_paraf");
            }
        } catch (Exception e) {
            System.out.println("Notif kolom nip asesmen ulang nyeri : " + e);
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

    private static String fmtDatetime(String v) {
        if (v == null || v.trim().equals("") || v.equals("null")) { return ""; }
        return v.length() >= 16 ? v.substring(0, 16) : v;
    }

    private static void setTglJam(widget.Tanggal d, String v) {
        if (v == null || v.trim().equals("") || v.equals("null")) { d.setDate(new Date()); return; }
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
            d.setDate(sdf.parse(v.length() >= 16 ? v.substring(0, 16) : v));
        } catch (Exception e) {
            d.setDate(new Date());
        }
    }

    private String ambilTglJam(widget.Tanggal d) {
        Object v = d.getSelectedItem();
        if (v == null) { return null; }
        String s = v.toString();
        return s.length() >= 19 ? Valid.SetTglJam(s.substring(0, 19)) : null;
    }

    // ====================== Helpers UI ======================
    private static widget.TextBox tf() { return new widget.TextBox(); }

    private static widget.TextBox ro() {
        widget.TextBox t = new widget.TextBox();
        t.setEditable(false);
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

    /** Nama petugas (readonly, terisi otomatis dari user login) + tombol "..." utk pilih petugas lain. */
    private JPanel gabungPetugas(Component nama, Component tombol) {
        JPanel pnl = new JPanel(new BorderLayout(3, 0));
        pnl.setOpaque(false);
        tombol.setPreferredSize(new Dimension(28, 23));
        pnl.add(nama, BorderLayout.CENTER);
        pnl.add(tombol, BorderLayout.EAST);
        return pnl;
    }

    private JPanel fieldRingkasan(String label, Component komponen) {
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
        komponen.setBackground(new Color(248, 250, 251));
        p.add(l);
        p.add(Box.createVerticalStrut(3));
        p.add(komponen);
        return p;
    }

    /** Kartu referensi/legenda (mis. skala skor nyeri) -- teks saja, bukan isian, dicetak persis seperti di kertas RM 7.1. */
    private JPanel kartuLegenda(String judul, Color teks, Color garis, String... baris) {
        JPanel p = new JPanel();
        p.setBackground(new Color(252, 253, 253));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis),
                new EmptyBorder(8, 10, 8, 10)));
        JLabel judulL = new JLabel("<html>" + judul + "</html>");
        judulL.setFont(new Font("Tahoma", Font.BOLD, 10));
        judulL.setForeground(teks);
        judulL.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(judulL);
        p.add(Box.createVerticalStrut(4));
        for (String b : baris) {
            JLabel l = new JLabel("<html><div style='width:230px'>" + b + "</div></html>");
            l.setFont(new Font("Tahoma", Font.PLAIN, 10));
            l.setForeground(new Color(74, 91, 104));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            l.setBorder(new EmptyBorder(0, 0, 2, 0));
            p.add(l);
        }
        return p;
    }

    private JPanel kartu(String judul, Color teks, Color garis) {
        JPanel luar = new JPanel(new GridBagLayout());
        luar.setBackground(Color.WHITE);
        luar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis),
                new EmptyBorder(8, 12, 12, 12)));
        JLabel l = new JLabel(judul);
        l.setFont(new Font("Tahoma", Font.BOLD, 13));
        l.setForeground(teks);
        GridBagConstraints g = gc(0, 0, 4, 1.0);
        g.insets = new Insets(2, 4, 10, 4);
        luar.add(l, g);
        return luar;
    }

    private JScrollPane bungkusScrollVertikal(JPanel p) {
        JScrollPane s = new JScrollPane(p);
        s.setBorder(null);
        s.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        s.getVerticalScrollBar().setUnitIncrement(20);
        s.setPreferredSize(new Dimension(500, 260));
        return s;
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

    private JLabel labelAtas(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font("Tahoma", Font.PLAIN, 11));
        l.setForeground(new Color(49, 64, 75));
        return l;
    }

    private void siapkanInput(Component komponen) {
        komponen.setPreferredSize(new Dimension(220, 28));
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

    private static String nvl(String v) {
        return v == null ? "" : v;
    }

    /** Foto TTD petugas berdasarkan NIP, ditarik dari pegawai.photo -- pola sama seperti ttd_petugas_photo di CetakAsesmen. Di-cache per NIP. */
    private ImageIcon ambilParafIcon(String nip) {
        if (nip == null || nip.trim().isEmpty()) { return null; }
        String key = nip.trim();
        if (cacheParaf.containsKey(key)) { return cacheParaf.get(key); }
        ImageIcon ic = null;
        try {
            String photo = bersihkanPathFoto(Sequel.cariIsi("select photo from pegawai where nik=?", key));
            if (!photo.isEmpty()) {
                String urlPenggajian = "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/"
                        + koneksiDB.HYBRIDWEB() + "/penggajian/";
                Image gambar = CetakCPPT.ambilGambarServer(urlPenggajian + photo);
                if (gambar != null) {
                    ic = new ImageIcon(gambar.getScaledInstance(-1, 32, Image.SCALE_SMOOTH));
                }
            }
        } catch (Exception ignore) { }
        cacheParaf.put(key, ic);
        return ic;
    }

    private static String bersihkanPathFoto(String photo) {
        if (photo == null) { return ""; }
        String p = photo.trim();
        if (p.equals("") || p.equals("-") || p.equals("pages/pegawai/photo/")) { return ""; }
        return p.replace("\\", "/");
    }

    /** Render kolom "Paraf" di tabel riwayat sbg thumbnail TTD (bukan teks paraf manual lagi). */
    private final class ParafRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
            l.setHorizontalAlignment(JLabel.CENTER);
            l.setIcon(ambilParafIcon(value == null ? null : value.toString()));
            return l;
        }
    }
}
