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
 * Ceklis Keselamatan Pembedahan (RM 29, blok 9 RM Operasi) -- adaptasi WHO
 * Surgical Safety Checklist, 3 fase (Sign In / Time Out / Sign Out). Pola
 * generik Item/semuaTeks sama seperti RM25/RM28 (lihat memory
 * rm25-form-besar-teknik). Wording indikator SENGAJA disalin verbatim dari
 * kertas asli (termasuk typo spt "impian"/"mmpunyai"/"dakam"/"selnjutnya")
 * krn user eksplisit minta tidak ada perbedaan dgn PDF asli.
 */
public final class RMCeklisKeselamatanPembedahan extends JDialog {

    private static final Font FONT_FORM = new Font("Times New Roman", Font.PLAIN, 13);
    private static final Font FONT_FORM_BOLD = new Font("Times New Roman", Font.BOLD, 13);

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final Map<String, ImageIcon> cacheFotoTtd = new HashMap<>();
    private final DlgCariPetugas pickerPetugas1 = new DlgCariPetugas(null, false);
    private final DlgCariPetugas pickerPetugas2 = new DlgCariPetugas(null, false);
    private final DlgCariPetugas pickerPetugas3 = new DlgCariPetugas(null, false);

    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TJK = ro();
    private final widget.TextBox TTglLahir = ro();
    private final widget.Tanggal dtpTanggal = dt();

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

    public RMCeklisKeselamatanPembedahan(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Ceklis Keselamatan Pembedahan (RM 29) ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
        ensureTable();
        siapkanPicker();
        setSize(1150, 820);
        setMinimumSize(new Dimension(950, 640));
        setLocationRelativeTo(parent);
    }

    // ====================== Item ceklis generik (pola sama RM25/RM28) ======================
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

    private Item[] itemsDari(String... spek) {
        Item[] hasil = new Item[spek.length];
        for (int i = 0; i < spek.length; i++) {
            String[] bag = spek[i].split(":");
            hasil[i] = (bag.length > 2 && bag[2].equals("C")) ? itemCatatan(bag[0], bag[1]) : item(bag[0], bag[1]);
        }
        return hasil;
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
        JLabel judulUtama = new JLabel("Ceklis Keselamatan Pembedahan");
        judulUtama.setFont(new Font("Times New Roman", Font.BOLD, 21));
        judulUtama.setForeground(teks);
        JLabel subjudul = new JLabel("Form RM 29  •  Sign In / Time Out / Sign Out");
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
        tab.addTab("Sign In", bungkusScroll(buatTabSignIn(teks, garis)));
        tab.addTab("Time Out", bungkusScroll(buatTabTimeOut(teks, garis)));
        tab.addTab("Sign Out", bungkusScroll(buatTabSignOut(teks, garis)));
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

    private JPanel buatTabSignIn(Color teks, Color garis) {
        JPanel isi = new JPanel();
        isi.setBackground(new Color(246, 249, 251));
        isi.setBorder(new EmptyBorder(6, 8, 10, 8));
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));

        JPanel kartuInfo = kartu("Informasi Tindakan", teks, garis);
        int r0 = 1;
        r0 = tambahBaris(kartuInfo, r0, barisField(
                fieldKecil("Nama Petugas", teks("nama_petugas")), fieldKecil("Tanggal / Jam", dtpTanggal)));
        r0 = tambahBaris(kartuInfo, r0, barisField(
                fieldKecilLebar("Diagnose Medis", teks("diagnose_medis")), fieldKecilLebar("Tindakan", teks("tindakan"))));
        r0 = tambahBaris(kartuInfo, r0, barisField(
                fieldKecilLebar("Operator / Assisten", teks("operator_assisten")), fieldKecilLebar("Anesthesi / Penata", teks("anesthesi_penata"))));
        isi.add(kartuInfo);
        isi.add(Box.createVerticalStrut(10));

        JLabel ket = new JLabel("<html>Dilakukan sebelum induksi anesthesia, dihadiri minimal oleh perawat dan ahli anesthesi</html>");
        ket.setFont(new Font("Times New Roman", Font.ITALIC, 13));
        ket.setForeground(new Color(92, 107, 119));
        ket.setAlignmentX(Component.LEFT_ALIGNMENT);
        ket.setBorder(new EmptyBorder(0, 2, 8, 0));
        isi.add(ket);

        JPanel kartu1 = kartu("1. Pasien telah dikonfirmasi meliputi (Sudah / Belum)", teks, garis);
        int r1 = 1;
        r1 = tambahBaris(kartu1, r1, barisLabel("a). Identitas dan gelang pasien", itemsDari("signin_1a_sudah:Sudah", "signin_1a_belum:Belum")));
        r1 = tambahBaris(kartu1, r1, barisLabel("b). Lokasi operasi", itemsDari("signin_1b_sudah:Sudah", "signin_1b_belum:Belum")));
        r1 = tambahBaris(kartu1, r1, barisLabel("c). Prosedur", itemsDari("signin_1c_sudah:Sudah", "signin_1c_belum:Belum")));
        r1 = tambahBaris(kartu1, r1, barisLabel("d). persetujuan operasi", itemsDari("signin_1d_sudah:Sudah", "signin_1d_belum:Belum")));
        r1 = tambahBaris(kartu1, r1, barisLabel("2. Mesin dan obat-obatan anesthesia sudah di cek lengkap", itemsDari("signin_2_sudah:Sudah", "signin_2_belum:Belum")));
        r1 = tambahBaris(kartu1, r1, barisLabel("3. pulse oximeter sudah terpasang dan berfungsi", itemsDari("signin_3_sudah:Sudah", "signin_3_belum:Belum")));
        r1 = tambahBaris(kartu1, r1, barisLabel("4. Kelengkapan persiapan impian", itemsDari("signin_4_sudah:Sudah", "signin_4_belum:Belum")));
        isi.add(kartu1);
        isi.add(Box.createVerticalStrut(10));

        JPanel kartu2 = kartu("Indikator (Ya / Tidak)", teks, garis);
        int r2 = 1;
        r2 = tambahBaris(kartu2, r2, barisLabel("5. Apakah pasien mmpunyai riwayat alergi", itemsDari("signin_5_ya:Ya", "signin_5_tidak:Tidak")));
        r2 = tambahBaris(kartu2, r2, barisLabel("6. Kesulitan bernafas/ resiko aspirasi? Ketersediaan peralatan bantuan",
                itemsDari("signin_6_ya:Ya", "signin_6_tidak:Tidak")));
        r2 = tambahBaris(kartu2, r2, barisLabel("7. Resiko kehilangan darah >500 ml (7ml/kg BB pada anak)", itemsDari("signin_7_ya:Ya", "signin_7_tidak:Tidak")));
        r2 = tambahBaris(kartu2, r2, barisLabel("8. Dua akses intervena/akses sentral dan rencana terapi cairan", itemsDari("signin_8_ya:Ya", "signin_8_tidak:Tidak")));
        isi.add(kartu2);
        isi.add(Box.createVerticalStrut(10));

        JPanel kartuTtd = kartu("Dokter Anesthesi/Penata Perawat Pre Med", teks, garis);
        int r9 = 1;
        r9 = tambahBaris(kartuTtd, r9, barisField(fieldKecil("Jam", teks("jam1"))));
        r9 = tunggalVertikal(kartuTtd, 0, "Petugas", bungkusPicker(bungkusFotoTtd(tPetugas1, lblFotoPetugas1), btnPilihPetugas1));
        isi.add(kartuTtd);
        isi.add(Box.createVerticalGlue());
        return isi;
    }

    private JPanel buatTabTimeOut(Color teks, Color garis) {
        JPanel isi = new JPanel();
        isi.setBackground(new Color(246, 249, 251));
        isi.setBorder(new EmptyBorder(6, 8, 10, 8));
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));

        JLabel ket = new JLabel("<html>Dilakukan sebelum insisi, dihadiri minimal oleh perawat, ahli anesthesia dan operator</html>");
        ket.setFont(new Font("Times New Roman", Font.ITALIC, 13));
        ket.setForeground(new Color(92, 107, 119));
        ket.setAlignmentX(Component.LEFT_ALIGNMENT);
        ket.setBorder(new EmptyBorder(0, 2, 8, 0));
        isi.add(ket);

        JPanel kartu1 = kartu("Indikator (Ya / Tidak)", teks, garis);
        int r1 = 1;
        r1 = tambahBaris(kartu1, r1, barisLabel("1. Konfirmasi meliputi :", itemsDari("timeout_1_ya:Ya", "timeout_1_tidak:Tidak")));
        r1 = tambahBaris(kartu1, r1, barisLabel("a). Nama pasien", itemsDari("timeout_1a_ya:Ya", "timeout_1a_tidak:Tidak")));
        r1 = tambahBaris(kartu1, r1, barisLabel("b). Prosedur", itemsDari("timeout_1b_ya:Ya", "timeout_1b_tidak:Tidak")));
        r1 = tambahBaris(kartu1, r1, barisLabel("c). Lokasi insisi", itemsDari("timeout_1c_ya:Ya", "timeout_1c_tidak:Tidak")));
        r1 = tambahBaris(kartu1, r1, barisLabel("2. Sebutkan nama dan peran masing-masing anggota tim", itemsDari("timeout_2_ya:Ya", "timeout_2_tidak:Tidak")));
        r1 = tambahBaris(kartu1, r1, barisLabel("3.a) sudahkan antibiotic diberikan 60 menit sebelumnya", itemsDari("timeout_3a_ya:Ya", "timeout_3a_tidak:Tidak")));
        r1 = tambahBaris(kartu1, r1, barisLabel("3.b) diberikan oleh", itemsDari("timeout_3b_ya:Ya", "timeout_3b_tidak:Tidak")));
        r1 = tambahBaris(kartu1, r1, barisField(fieldKecilLebar("(nama pemberi antibiotik)", teks("timeout_3b_diberikanoleh"))));
        r1 = tambahBaris(kartu1, r1, barisLabel("4. Implan yang akan dipasang sesuai rencana dan steril", itemsDari("timeout_4_ya:Ya", "timeout_4_tidak:Tidak")));
        r1 = tambahBaris(kartu1, r1, barisLabel("5. Pencegahan kejadian tidak diharapkan (KTD)", itemsDari("timeout_5_ya:Ya", "timeout_5_tidak:Tidak")));
        r1 = tambahBaris(kartu1, r1, barisLabel("6. Perlakukan hasil MRI,CT Scan, Foto dipasang?", itemsDari("timeout_6_ya:Ya", "timeout_6_tidak:Tidak")));
        isi.add(kartu1);
        isi.add(Box.createVerticalStrut(10));

        JPanel kartuOperator = kartu("Operator", teks, garis);
        int r2 = 1;
        r2 = tambahBaris(kartuOperator, r2, barisLabel("a). apakah memungkinkan timbul kesulitan dalam operasi apakah tidak untuk itu?",
                itemsDari("timeout_operator_a_ya:Ya", "timeout_operator_a_tidak:Tidak")));
        r2 = tambahBaris(kartuOperator, r2, barisLabel("b). berapa lama estimasi operasi? ...jam", itemsDari("timeout_operator_b_ya:Ya", "timeout_operator_b_tidak:Tidak")));
        r2 = tambahBaris(kartuOperator, r2, barisField(fieldKecil("Estimasi (jam)", teks("timeout_operator_b_estimasi"))));
        r2 = tambahBaris(kartuOperator, r2, barisLabel("c). perkiraan kehilangan darah ...Cc", itemsDari("timeout_operator_c_ya:Ya", "timeout_operator_c_tidak:Tidak")));
        r2 = tambahBaris(kartuOperator, r2, barisField(fieldKecil("Kehilangan Darah (Cc)", teks("timeout_operator_c_kehilangandarah"))));
        isi.add(kartuOperator);
        isi.add(Box.createVerticalStrut(10));

        JPanel kartuAnestesi = kartu("Ahli anesthesi", teks, garis);
        int r3 = 1;
        r3 = tambahBaris(kartuAnestesi, r3, barisLabel("Apakah masalah khusus pada pasien dan langkah antisipasi",
                itemsDari("timeout_anestesi_ya:Ya", "timeout_anestesi_tidak:Tidak")));
        r3 = tambahBaris(kartuAnestesi, r3, barisField(fieldKecilLebar("(catatan)", teks("timeout_anestesi_masalahkhusus"))));
        isi.add(kartuAnestesi);
        isi.add(Box.createVerticalStrut(10));

        JPanel kartuPerawat = kartu("perawat", teks, garis);
        int r4 = 1;
        r4 = tambahBaris(kartuPerawat, r4, barisField(fieldKecilLebar("(catatan)", teks("timeout_perawat_catatan"))));
        r4 = tambahBaris(kartuPerawat, r4, barisLabel("a). bukti sterilisasi alat", itemsDari("timeout_perawat_a_ya:Ya", "timeout_perawat_a_tidak:Tidak")));
        r4 = tambahBaris(kartuPerawat, r4, barisField(fieldKecilLebar("(keterangan)", teks("timeout_perawat_a_sterilisasi"))));
        r4 = tambahBaris(kartuPerawat, r4, barisLabel("b). adakah alat khusus", itemsDari("timeout_perawat_b_ya:Ya", "timeout_perawat_b_tidak:Tidak")));
        r4 = tambahBaris(kartuPerawat, r4, barisLabel("c). jumlah bighass", itemsDari("timeout_perawat_c_ya:Ya", "timeout_perawat_c_tidak:Tidak")));
        r4 = tambahBaris(kartuPerawat, r4, barisField(fieldKecil("Jumlah Bighass", teks("timeout_perawat_c_bighass"))));
        r4 = tambahBaris(kartuPerawat, r4, barisLabel("d). jumlah jarum", itemsDari("timeout_perawat_d_ya:Ya", "timeout_perawat_d_tidak:Tidak")));
        r4 = tambahBaris(kartuPerawat, r4, barisField(fieldKecil("Jumlah Jarum", teks("timeout_perawat_d_jarum"))));
        r4 = tambahBaris(kartuPerawat, r4, barisLabel("e). jumlah alat", itemsDari("timeout_perawat_e_ya:Ya", "timeout_perawat_e_tidak:Tidak")));
        r4 = tambahBaris(kartuPerawat, r4, barisField(fieldKecil("Jumlah Alat", teks("timeout_perawat_e_alat"))));
        isi.add(kartuPerawat);
        isi.add(Box.createVerticalStrut(10));

        JPanel kartuTtd = kartu("Perawat Sirkuler", teks, garis);
        int r9 = 1;
        r9 = tambahBaris(kartuTtd, r9, barisField(fieldKecil("Jam", teks("jam2"))));
        r9 = tunggalVertikal(kartuTtd, 0, "Petugas", bungkusPicker(bungkusFotoTtd(tPetugas2, lblFotoPetugas2), btnPilihPetugas2));
        isi.add(kartuTtd);
        isi.add(Box.createVerticalGlue());
        return isi;
    }

    private JPanel buatTabSignOut(Color teks, Color garis) {
        JPanel isi = new JPanel();
        isi.setBackground(new Color(246, 249, 251));
        isi.setBorder(new EmptyBorder(6, 8, 10, 8));
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));

        JLabel ket = new JLabel("<html>Dilakukan sebelum pasien meninggalkan OK, dihadiri oleh perawat, ahli anesthesi dan operator</html>");
        ket.setFont(new Font("Times New Roman", Font.ITALIC, 13));
        ket.setForeground(new Color(92, 107, 119));
        ket.setAlignmentX(Component.LEFT_ALIGNMENT);
        ket.setBorder(new EmptyBorder(0, 2, 8, 0));
        isi.add(ket);

        JPanel kartu1 = kartu("Indikator (Ya / Tidak)", teks, garis);
        int r1 = 1;
        r1 = tambahBaris(kartu1, r1, barisLabel("1. Konfirmasi secara verbal tentang nama prosedur/tindakan", itemsDari("signout_1_ya:Ya", "signout_1_tidak:Tidak")));
        r1 = tambahBaris(kartu1, r1, barisLabel("2. Jumlah instrument, kassa, bighass dan jarum", itemsDari("signout_2_ya:Ya", "signout_2_tidak:Tidak")));
        isi.add(kartu1);
        isi.add(Box.createVerticalStrut(10));

        JPanel kartuTabel = kartu("Tabel Jumlah Instrumen / Kassa / Bighass / Jarum", teks, garis);
        int r2 = 1;
        r2 = tambahBaris(kartuTabel, r2, barisField(
                fieldKecil("Instrumen", teks("signout_pra_instrumen")), fieldKecil("Kassa", teks("signout_pra_kassa")),
                fieldKecil("Bighass", teks("signout_pra_bighass")), fieldKecil("Jarum", teks("signout_pra_jarum"))), "Pra");
        r2 = tambahBarisLabelKiri(kartuTabel, r2, barisField(
                fieldKecil("Instrumen", teks("signout_intra_instrumen")), fieldKecil("Kassa", teks("signout_intra_kassa")),
                fieldKecil("Bighass", teks("signout_intra_bighass")), fieldKecil("Jarum", teks("signout_intra_jarum"))), "Intra");
        r2 = tambahBarisLabelKiri(kartuTabel, r2, barisField(
                fieldKecil("Instrumen", teks("signout_pasca_instrumen")), fieldKecil("Kassa", teks("signout_pasca_kassa")),
                fieldKecil("Bighass", teks("signout_pasca_bighass")), fieldKecil("Jarum", teks("signout_pasca_jarum"))), "Pasca");
        r2 = tambahBarisLabelKiri(kartuTabel, r2, barisField(
                fieldKecil("Instrumen", teks("signout_total_instrumen")), fieldKecil("Kassa", teks("signout_total_kassa")),
                fieldKecil("Bighass", teks("signout_total_bighass")), fieldKecil("Jarum", teks("signout_total_jarum"))), "Total");
        isi.add(kartuTabel);
        isi.add(Box.createVerticalStrut(10));

        JPanel kartu2 = kartu("Indikator (Ya / Tidak) lanjutan", teks, garis);
        int r3 = 1;
        r3 = tambahBaris(kartu2, r3, barisLabel("3. Specimen telah habis label (minimal nama, no.RM dan asal jaringan spesimen)",
                itemsDari("signout_3_ya:Ya", "signout_3_tidak:Tidak")));
        r3 = tambahBaris(kartu2, r3, barisLabel("4. Nomor register implan dimasukkan dakam rekam medis pasien", itemsDari("signout_4_ya:Ya", "signout_4_tidak:Tidak")));
        r3 = tambahBaris(kartu2, r3, barisLabel("5. Adakah masalah dengan peralatan selama operasi", itemsDari("signout_5_ya:Ya", "signout_5_tidak:Tidak")));
        r3 = tambahBaris(kartu2, r3, barisLabel("6. Pesan khusus dari operator, ahli anesthesi dan perawat untuk asuhan selnjutnya",
                itemsDari("signout_6_ya:Ya", "signout_6_tidak:Tidak")));
        r3 = tambahBaris(kartu2, r3, barisField(fieldKecilLebar("(pesan khusus)", teks("signout_6_pesan"))));
        isi.add(kartu2);
        isi.add(Box.createVerticalStrut(10));

        JPanel kartuTtd = kartu("Operator", teks, garis);
        int r9 = 1;
        r9 = tambahBaris(kartuTtd, r9, barisField(fieldKecil("Jam", teks("jam3"))));
        r9 = tunggalVertikal(kartuTtd, 0, "Petugas", bungkusPicker(bungkusFotoTtd(tPetugas3, lblFotoPetugas3), btnPilihPetugas3));
        isi.add(kartuTtd);
        isi.add(Box.createVerticalGlue());
        return isi;
    }

    // ====================== Picker TTD (3 petugas) ======================
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
        boolean bisa = akses.getpenilaian_awal_keperawatan_ranap();
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
            System.out.println("Notif tarik data pasien RM29 : " + e);
        }
        dtpTanggal.setDate(new Date());
    }

    private void muatDataJikaAda(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement("select * from ceklis_keselamatan_pembedahan where no_rawat=?")) {
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
                    isiTtdJikaAda(rs, "kd_petugas1", "nama_petugas1", tPetugas1, lblFotoPetugas1, kd -> kdPetugas1 = kd);
                    isiTtdJikaAda(rs, "kd_petugas2", "nama_petugas2", tPetugas2, lblFotoPetugas2, kd -> kdPetugas2 = kd);
                    isiTtdJikaAda(rs, "kd_petugas3", "nama_petugas3", tPetugas3, lblFotoPetugas3, kd -> kdPetugas3 = kd);
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat RM29 : " + e);
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
        kolom.add("kd_petugas1"); nilai.add(kdPetugas1);
        kolom.add("nama_petugas1"); nilai.add(ambilNamaDariTeks(tPetugas1));
        kolom.add("kd_petugas2"); nilai.add(kdPetugas2);
        kolom.add("nama_petugas2"); nilai.add(ambilNamaDariTeks(tPetugas2));
        kolom.add("kd_petugas3"); nilai.add(kdPetugas3);
        kolom.add("nama_petugas3"); nilai.add(ambilNamaDariTeks(tPetugas3));
        kolom.add("updated_by"); nilai.add(akses.getkode());

        String placeholder = ulang("?", kolom.size());
        StringBuilder sb = new StringBuilder("insert into ceklis_keselamatan_pembedahan (no_rawat,tanggal,jam,");
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
            JOptionPane.showMessageDialog(this, "Ceklis Keselamatan Pembedahan tersimpan.");
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
        if (JOptionPane.showConfirmDialog(this, "Hapus ceklis keselamatan pembedahan untuk No.Rawat " + ambil(TNoRw) + " ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement("delete from ceklis_keselamatan_pembedahan where no_rawat=?")) {
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
        if (Sequel.cariInteger("select count(*) from ceklis_keselamatan_pembedahan where no_rawat=?", ambil(TNoRw)) == 0) {
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
                    + "from ceklis_keselamatan_pembedahan a "
                    + "inner join reg_periksa on a.no_rawat=reg_periksa.no_rawat "
                    + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "where a.no_rawat='" + ambil(TNoRw) + "'";
            Valid.MyReportqry("rptCeklisKeselamatanPembedahan.jasper", "report", "::[ Ceklis Keselamatan Pembedahan ]::", sql, param);
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
        StringBuilder sql = new StringBuilder("create table if not exists ceklis_keselamatan_pembedahan ("
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

    // ====================== Helpers UI (pola sama RM25/RM28) ======================
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
        luar.setBorder(BorderFactory.createLineBorder(new Color(90, 100, 108)));
        luar.setAlignmentX(Component.LEFT_ALIGNMENT);
        luar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6000));
        JLabel l = new JLabel(judul);
        l.setFont(FONT_FORM_BOLD);
        l.setForeground(teks);
        l.setHorizontalAlignment(JLabel.CENTER);
        l.setOpaque(true);
        l.setBackground(new Color(239, 243, 246));
        l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(90, 100, 108)));
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
            l.setPreferredSize(new Dimension(430, 22));
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

    /** Baris tabel dgn label kecil di kiri (Pra/Intra/Pasca/Total) + isian di kanan. */
    private int tambahBaris(JPanel kartuPanel, int row, JPanel isiBaris, String labelKiri) {
        JPanel bungkus = new JPanel(new BorderLayout(8, 0));
        bungkus.setOpaque(false);
        JLabel l = labelAtas(labelKiri);
        l.setPreferredSize(new Dimension(50, 20));
        bungkus.add(l, BorderLayout.WEST);
        bungkus.add(isiBaris, BorderLayout.CENTER);
        return tambahBaris(kartuPanel, row, bungkus);
    }

    private int tambahBarisLabelKiri(JPanel kartuPanel, int row, JPanel isiBaris, String labelKiri) {
        return tambahBaris(kartuPanel, row, isiBaris, labelKiri);
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
