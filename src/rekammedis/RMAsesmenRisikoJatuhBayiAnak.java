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
import java.awt.GridLayout;
import java.awt.Image;
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
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import kepegawaian.DlgCariPetugas;

/**
 * Asesmen Risiko Jatuh Pasien Bayi dan Anak (Humpty Dumpty Scale), Rawat Inap.
 * Layout mengikuti PERSIS kertas asli: tabel skoring 7 faktor x 5 kolom ASES
 * (ASES 1 Saat Masuk .. ASES 5) dgn kotak centang per baris skala, lalu tabel
 * intervensi (Standar Risiko Rendah 10 item + Risiko Tinggi 12 item) x 5 kolom
 * INTERV, semuanya utk SATU pasien (bukan lagi catatan berulang tak terbatas --
 * kertas aslinya memang cuma sediakan 5 slot penilaian per pasien).
 */
public final class RMAsesmenRisikoJatuhBayiAnak extends JDialog {

    private static final int N_SLOT = 5;
    /** Lebar kolom "Faktor Risiko"+"Skala"+"Skor" (130+640+46) -- dipakai jg utk lebar spacerKiri
     *  baris tanggal ASES 1-5, spy tanggalnya sejajar persis dgn kolom centang ASES di tabel. */
    private static final int LEBAR_KOLOM_SEBELUM_ASES = 130 + 640 + 46;
    /** Lebar 1 kolom ASES (checkbox) di tabel Pengkajian -- dipakai jg utk lebar 1 slot tanggal ASES. */
    private static final int LEBAR_KOLOM_ASES = 100;

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();

    private static final class Opsi {
        final String teks; final int skor;
        Opsi(String teks, int skor) { this.teks = teks; this.skor = skor; }
    }

    private static final String[] FAKTOR_LABEL = {"Umur (otomatis)", "Jenis Kelamin (otomatis)", "Diagnosa", "Gangguan Kognitif",
        "Faktor Lingkungan", "Respon Operasi/Obat Penenang/Anastesi", "Penggunaan Obat"};
    private static final String[] FAKTOR_KEY = {"umur", "jk", "diagnosa", "kognitif", "lingkungan", "respon_obat", "obat"};

    private static final Opsi[][] OPSI_FAKTOR = {
        { new Opsi("Di bawah 3 tahun", 4), new Opsi("3 - 7 tahun", 3), new Opsi("7 - 13 tahun", 2), new Opsi("Di atas 13 tahun", 1) },
        { new Opsi("Laki-laki", 2), new Opsi("Perempuan", 1) },
        { new Opsi("Diagnosis neurologis", 4),
          new Opsi("Perubahan dalam oksigenasi (masalah saluran nafas, dehidrasi, anemia, anoreksia, sinkop/sakit kepala, dll)", 3),
          new Opsi("Kelainan psikis/perilaku", 2), new Opsi("Diagnosis lain", 1) },
        { new Opsi("Tidak sadar akan keterbatasan dirinya", 3), new Opsi("Lupa akan adanya keterbatasan", 2),
          new Opsi("Mengetahui kemampuan diri sendiri", 1) },
        { new Opsi("Riwayat jatuh dari tempat tidur saat bayi-anak", 4), new Opsi("Pasien menggunakan alat bantu atau box atau mebel", 3),
          new Opsi("Pasien berada di tempat tidur", 2), new Opsi("Di luar ruang rawat", 1) },
        { new Opsi("Dalam waktu 24 jam", 3), new Opsi("Dalam waktu 48 jam", 2), new Opsi("Lebih dari 48 jam", 1) },
        { new Opsi("Menggunakan bermacam-macam obat: sedatif (kecuali pasien ICU yang menggunakan sedasi dan paralisis), hipnotik, barbiturat, fenotiazin, antidepresan, laksatif/diuretik, narkotika", 3),
          new Opsi("Menggunakan salah satu dari pengobatan di atas", 2), new Opsi("Pengobatan lain", 1) },
    };

    private static final String[] TEKS_RR = {
        "Orientasi ruangan",
        "Posisi tempat tidur rendah dan ada remnya",
        "Ada pengaman samping tempat tidur dengan 2 atau 4 sisi pengaman. Mempunyai luas tempat tidur yang cukup untuk mencegah tangan dan kaki atau bagian tubuh lain terjepit",
        "Tidak menggunakan alas kaki yang licin untuk pasien yang bisa berjalan",
        "Nilai kebutuhan untuk ke kamar mandi dan bantu pasien bila dibutuhkan",
        "Akses untuk menghubungi petugas kesehatan mudah dijangkau. Terangkan kepada pasien mengenai fungsi alat tersebut",
        "Lingkungan harus bebas dari peralatan yang mengandung resiko",
        "Lampu penerangan harus cukup",
        "Leaflet penatalaksanaan jatuh untuk pasien atau keluarga harus tersedia",
        "Dokumen pemantauan pencegahan pasien jatuh harus ada di status pasien",
    };
    private static final String[] TEKS_RT = {
        "Ada tanda peringatan pasien resiko jatuh (ditempat tidur, brankar, kursi roda)",
        "Penjelasan pada pasien atau orang tuanya tentang protokol pencegahan pasien jatuh",
        "Cek pasien minimal setiap satu jam",
        "Temani pasien pada saat mobilisasi",
        "Tempat tidur pasien harus disesuaikan dengan perkembangan tubuh pasien",
        "Pertimbangkan penempatan pasien yang perlu perhatian diletakkan dekat nurse station",
        "Perbandingan pasien dengan perawat 1:3 apabila belum terpenuhi libatkan keluarga pasien",
        "Evaluasi terapi yang sesuai. Pindahkan semua peralatan yang tidak dibutuhkan ke luar ruangan",
        "Pencegahan jatuh dengan pengamanan yang cukup, naikkan hand rail, batasi gerakan pasien di tempat tidur",
        "Biarkan pintu terbuka setiap saat kecuali pada pasien yang membutuhkan ruang isolasi",
        "Tempatkan pasien pada posisi tempat tidur yang rendah kecuali pada pasien yang ditunggu keluarga",
        "Semua kegiatan yang dilakukan pada pasien harus didokumentasikan",
    };

    /** Baris grid pengkajian: 1 baris per opsi-skala (23 baris total dari 7 faktor). */
    private static final class GridRow {
        final int faktorIdx; final int opsiIdx;
        GridRow(int f, int o) { faktorIdx = f; opsiIdx = o; }
    }
    /**
     * Daftar baris grid Pengkajian saat ini -- BUKAN static/tetap lagi. Baris Umur & Jenis
     * Kelamin dipangkas jadi cuma 1 baris (yang cocok dgn data pasien) begitu pasien diketahui,
     * lewat bangunUlangGridPengkajian(); sebelum ada pasien tampil default penuh (4 opsi Umur,
     * 2 opsi JK) lewat buatGridRowsPenuh().
     */
    private GridRow[] gridRows = buatGridRowsPenuh();
    private static GridRow[] buatGridRowsPenuh() {
        java.util.List<GridRow> list = new java.util.ArrayList<>();
        for (int f = 0; f < OPSI_FAKTOR.length; f++) {
            for (int o = 0; o < OPSI_FAKTOR[f].length; o++) {
                list.add(new GridRow(f, o));
            }
        }
        return list.toArray(new GridRow[0]);
    }

    /** Umur & JK dipangkas ke 1 baris yg cocok (kalau sudah tahu); faktor lain tetap penuh. */
    private GridRow[] buatGridRowsUntukPasien(int idxUmur, int idxJk) {
        java.util.List<GridRow> list = new java.util.ArrayList<>();
        for (int f = 0; f < OPSI_FAKTOR.length; f++) {
            for (int o = 0; o < OPSI_FAKTOR[f].length; o++) {
                if (f == 0 && idxUmur >= 0 && o != idxUmur) { continue; }
                if (f == 1 && idxJk >= 0 && o != idxJk) { continue; }
                list.add(new GridRow(f, o));
            }
        }
        return list.toArray(new GridRow[0]);
    }

    /** Baris ke-idx (di gridRows saat ini) adalah baris pertama kelompok faktornya -- dipakai utk garis pemisah.
     *  TIDAK bisa lagi pakai patokan opsiIdx==0, krn Umur/JK bisa dipangkas jadi cuma nyisa 1 baris yg opsiIdx-nya bisa berapa saja. */
    private boolean awalKelompok(int idx) {
        if (idx < 0 || idx >= gridRows.length) { return false; }
        return idx == 0 || gridRows[idx].faktorIdx != gridRows[idx - 1].faktorIdx;
    }

    /**
     * Baris ke-idx (di gridRows saat ini) adalah baris TENGAH kelompok faktornya -- dipakai utk taruh
     * label "Faktor Risiko", spy tampil sejajar/rata dgn tengah kelompoknya (kayak sel gabung di kertas
     * asli/PDF), bukan cuma nempel di baris paling atas.
     */
    private boolean tengahKelompok(int idx) {
        if (idx < 0 || idx >= gridRows.length) { return false; }
        int mulai = idx;
        while (mulai > 0 && gridRows[mulai - 1].faktorIdx == gridRows[idx].faktorIdx) { mulai--; }
        int akhir = idx;
        while (akhir < gridRows.length - 1 && gridRows[akhir + 1].faktorIdx == gridRows[idx].faktorIdx) { akhir++; }
        return idx == mulai + (akhir - mulai) / 2;
    }

    /** Isi ulang seluruh baris modelPengkajian dari gridRows saat ini (dipanggil di constructor & tiap kali gridRows berubah). */
    private void isiBarisModelPengkajian() {
        boolean lamaSedangMemuat = sedangMemuat;
        sedangMemuat = true;
        modelPengkajian.setRowCount(0);
        for (int i = 0; i < gridRows.length; i++) {
            GridRow gr = gridRows[i];
            Opsi o = OPSI_FAKTOR[gr.faktorIdx][gr.opsiIdx];
            String labelFaktor = tengahKelompok(i) ? FAKTOR_LABEL[gr.faktorIdx] : "";
            modelPengkajian.addRow(new Object[]{labelFaktor, o.teks, o.skor, false, false, false, false, false});
        }
        sedangMemuat = lamaSedangMemuat;
    }

    // Header identitas (readonly)
    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TJK = ro();
    private final widget.TextBox TTglLahir = ro();

    /** Tgl lahir & JK asli pasien (bukan versi teks terformat) -- dipakai utk hitung otomatis baris Umur/Jenis Kelamin. */
    private Date tglLahirPasien = null;
    private String jkPasien = "";

    private final widget.Tanggal[] dtpTgl = new widget.Tanggal[N_SLOT];
    private final DefaultTableModel modelPengkajian;
    /**
     * Garis pemisah lebih tebal di baris pertama tiap kelompok faktor (spy kelihatan batas antar
     * faktor). Baris Umur/Jenis Kelamin sudah dipangkas jadi cuma 1 baris yg cocok dgn data pasien
     * (lihat buatGridRowsUntukPasien()), tapi kotak centangnya kotak centang biasa spt faktor lain --
     * petugas tetap yg mencentang manual per kolom ASES, skor otomatis dihitung kalau dicentang.
     */
    private final widget.Table tblPengkajian = new widget.Table() {
        @Override
        public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);
            if (c instanceof javax.swing.JComponent) {
                boolean perluGarisPemisah = row > 0 && awalKelompok(row);
                ((javax.swing.JComponent) c).setBorder(perluGarisPemisah
                        ? BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(120, 130, 140))
                        : BorderFactory.createEmptyBorder());
            }
            return c;
        }
    };
    private final JLabel[] lblTotal = new JLabel[N_SLOT];
    private final JLabel[] lblKategori = new JLabel[N_SLOT];

    private final widget.TextBox[] tPetugasPenilaiNama = new widget.TextBox[N_SLOT];
    private final widget.TextBox[] kdPetugasPenilai = new widget.TextBox[N_SLOT];
    private final widget.Button[] btnPetugasPenilai = new widget.Button[N_SLOT];

    private final DefaultTableModel modelRR;
    private final widget.Table tblRR = new widget.Table();
    private final DefaultTableModel modelRT;
    private final widget.Table tblRT = new widget.Table();

    private final widget.TextBox[] tPetugasIntervensiNama = new widget.TextBox[N_SLOT];
    private final widget.TextBox[] kdPetugasIntervensi = new widget.TextBox[N_SLOT];
    private final widget.Button[] btnPetugasIntervensi = new widget.Button[N_SLOT];

    private final DlgCariPetugas pilihPetugas = new DlgCariPetugas(null, true);
    private int slotPetugasDipilih = -1;
    private boolean modePetugasIntervensi = false;

    private final widget.Button BtnSimpan = new widget.Button();
    private final widget.Button BtnCetak = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();

    private String noRawat = "";
    private boolean sedangMemuat = false;

    public RMAsesmenRisikoJatuhBayiAnak(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Asesmen Risiko Jatuh Pasien Bayi dan Anak (Humpty Dumpty Scale) ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        ensureTable();

        Object[] kolomPengkajian = {"Faktor Risiko", "Skala", "Skor", "ASES 1", "ASES 2", "ASES 3", "ASES 4", "ASES 5"};
        modelPengkajian = new DefaultTableModel(kolomPengkajian, 0) {
            @Override public Class<?> getColumnClass(int c) {
                if (c == 2) { return Integer.class; }
                if (c >= 3) { return Boolean.class; }
                return String.class;
            }
            @Override public boolean isCellEditable(int row, int col) {
                return col >= 3;
            }
        };
        isiBarisModelPengkajian();

        Object[] kolomInterv = {"No", "Standar Risiko Rendah dan Tidak Berisiko (Skor 7 - 11)", "I1", "I2", "I3", "I4", "I5"};
        modelRR = buatModelInterv(kolomInterv[1].toString(), TEKS_RR);
        Object[] kolomIntervTinggi = {"No", "Standar Risiko Tinggi (Skor >= 12)", "I1", "I2", "I3", "I4", "I5"};
        modelRT = buatModelInterv(kolomIntervTinggi[1].toString(), TEKS_RT);

        initComponents();
        pasangPemicuPengkajian();
        pasangPetugasPicker();
        setSize(1480, 960);
        setMinimumSize(new Dimension(1250, 780));
        setLocationRelativeTo(parent);
    }

    private DefaultTableModel buatModelInterv(String judulKolom, String[] teksItem) {
        DefaultTableModel m = new DefaultTableModel(new Object[]{"No", judulKolom, "I1", "I2", "I3", "I4", "I5"}, 0) {
            @Override public Class<?> getColumnClass(int c) { return c >= 2 ? Boolean.class : (c == 0 ? Integer.class : String.class); }
            @Override public boolean isCellEditable(int row, int col) { return col >= 2; }
        };
        for (int i = 0; i < teksItem.length; i++) {
            m.addRow(new Object[]{i + 1, teksItem[i], false, false, false, false, false});
        }
        return m;
    }

    private void initComponents() {
        final Color latar = new Color(246, 249, 251);
        final Color garis = new Color(215, 224, 230);
        final Color teks = new Color(32, 49, 66);

        getContentPane().setBackground(latar);
        getContentPane().setLayout(new BorderLayout(0, 10));

        JPanel atas = new JPanel(new BorderLayout(12, 10));
        atas.setBackground(latar);
        atas.setBorder(new EmptyBorder(14, 18, 0, 18));
        JLabel judulUtama = new JLabel("Asesmen Risiko Jatuh Pasien Bayi dan Anak");
        judulUtama.setFont(new Font("Tahoma", Font.BOLD, 20));
        judulUtama.setForeground(teks);
        JLabel subJudul = new JLabel("Humpty Dumpty Scale  •  Rawat Inap  •  ASES 1 (Saat Masuk) s/d ASES 5");
        subJudul.setFont(new Font("Tahoma", Font.PLAIN, 12));
        subJudul.setForeground(new Color(91, 105, 115));
        JPanel judulBox = new JPanel();
        judulBox.setOpaque(false);
        judulBox.setLayout(new BoxLayout(judulBox, BoxLayout.Y_AXIS));
        judulUtama.setAlignmentX(Component.LEFT_ALIGNMENT);
        subJudul.setAlignmentX(Component.LEFT_ALIGNMENT);
        judulBox.add(judulUtama);
        judulBox.add(subJudul);
        atas.add(judulBox, BorderLayout.NORTH);

        JPanel ringkasanPasien = new JPanel(new GridLayout(1, 5, 0, 0));
        ringkasanPasien.setBackground(Color.WHITE);
        ringkasanPasien.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis), new EmptyBorder(10, 12, 10, 12)));
        ringkasanPasien.add(fieldRingkasan("No. Rawat *", TNoRw));
        ringkasanPasien.add(fieldRingkasan("No. RM", TNoRM));
        ringkasanPasien.add(fieldRingkasan("Nama Pasien", TPasien));
        ringkasanPasien.add(fieldRingkasan("Jenis Kelamin", TJK));
        ringkasanPasien.add(fieldRingkasan("Tanggal Lahir", TTglLahir));
        atas.add(ringkasanPasien, BorderLayout.SOUTH);
        getContentPane().add(atas, BorderLayout.NORTH);

        JPanel tengah = new JPanel();
        tengah.setBackground(latar);
        tengah.setLayout(new BoxLayout(tengah, BoxLayout.Y_AXIS));
        tengah.setBorder(new EmptyBorder(10, 18, 10, 18));

        JPanel legenda = kartuLegenda("Kategori Skor", teks, garis,
                "7 - 11 : Risiko Rendah (RR)      |      >= 12 : Risiko Tinggi (RT)",
                "Tiap kolom ASES = satu kali waktu penilaian. Centang salah satu skala per faktor untuk kolom ASES itu.");
        tengah.add(legenda);
        tengah.add(Box.createVerticalStrut(8));

        // ----- Baris tanggal ASES 1-5 -----
        // Lebar spacerKiri & tglGrid HARUS PERSIS sama dgn lebar kolom tabel di bawahnya (lihat
        // aturLebarKolomPengkajian()) spy tiap tanggal ASES N sejajar/rata persis di atas kolom
        // centang ASES N -- makanya tglGrid dibungkus panel FlowLayout(LEFT) yg TIDAK melar
        // ikut lebar dialog (beda dgn BorderLayout.CENTER biasa yg akan melar & jadi geser).
        JPanel barisTanggal = new JPanel(new BorderLayout());
        barisTanggal.setOpaque(false);
        barisTanggal.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel spacerKiri = new JPanel();
        spacerKiri.setOpaque(false);
        spacerKiri.setPreferredSize(new Dimension(LEBAR_KOLOM_SEBELUM_ASES, 30));
        barisTanggal.add(spacerKiri, BorderLayout.WEST);
        JPanel tglGrid = new JPanel(new GridLayout(1, N_SLOT, 0, 0));
        tglGrid.setOpaque(false);
        tglGrid.setPreferredSize(new Dimension(LEBAR_KOLOM_ASES * N_SLOT, 30));
        for (int s = 0; s < N_SLOT; s++) {
            final int slot = s;
            dtpTgl[s] = dt();
            dtpTgl[s].addPropertyChangeListener("date", evt -> perbaruiSkorSlot(slot));
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            JLabel l = new JLabel((s == 0 ? "ASES 1 (Saat Masuk)" : "ASES " + (s + 1)), JLabel.CENTER);
            l.setFont(new Font("Tahoma", Font.BOLD, 10));
            l.setForeground(teks);
            p.add(l, BorderLayout.NORTH);
            dtpTgl[s].setPreferredSize(new Dimension(LEBAR_KOLOM_ASES - 4, 24));
            p.add(dtpTgl[s], BorderLayout.CENTER);
            tglGrid.add(p);
        }
        JPanel pembungkusTglGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pembungkusTglGrid.setOpaque(false);
        pembungkusTglGrid.add(tglGrid);
        barisTanggal.add(pembungkusTglGrid, BorderLayout.CENTER);
        tengah.add(barisTanggal);
        tengah.add(Box.createVerticalStrut(4));

        // ----- Tabel Pengkajian -----
        tblPengkajian.setModel(modelPengkajian);
        tblPengkajian.setRowHeight(20);
        // AUTO_RESIZE_OFF -- lebar kolom HARUS tetap persis spt aturLebarKolomPengkajian(), tdk boleh
        // melar ikut viewport (kalau melar, sejajarnya dgn baris tanggal ASES 1-5 di atas jadi rusak lagi).
        tblPengkajian.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        aturLebarKolomPengkajian();
        tblPengkajian.getColumnModel().getColumn(0).setCellRenderer(new FaktorRenderer());
        tblPengkajian.getColumnModel().getColumn(1).setCellRenderer(new WrapRenderer());
        tblPengkajian.setPreferredScrollableViewportSize(
                new Dimension(LEBAR_KOLOM_SEBELUM_ASES + LEBAR_KOLOM_ASES * N_SLOT, 480));
        JScrollPane scrollPengkajian = new JScrollPane(tblPengkajian);
        scrollPengkajian.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPengkajian.setPreferredSize(new Dimension(1440, 480));
        scrollPengkajian.setMaximumSize(new Dimension(Integer.MAX_VALUE, 480));
        tengah.add(scrollPengkajian);
        tengah.add(Box.createVerticalStrut(6));

        // ----- Baris Total / Kategori -----
        tengah.add(barisRingkasSlot("Total Skor", lblTotal, false));
        tengah.add(barisRingkasSlot("Kategori", lblKategori, true));
        tengah.add(Box.createVerticalStrut(8));

        // ----- Baris Petugas Penilai -----
        tengah.add(barisPetugasSlot("Paraf dan Nama Petugas yang Menilai", tPetugasPenilaiNama, kdPetugasPenilai, btnPetugasPenilai));
        tengah.add(Box.createVerticalStrut(14));

        // ----- Intervensi -----
        JLabel judulInterv = new JLabel("Intervensi Pencegahan Risiko Jatuh (INTERV 1 - 5, sejalan dengan ASES 1 - 5)");
        judulInterv.setFont(new Font("Tahoma", Font.BOLD, 13));
        judulInterv.setForeground(teks);
        judulInterv.setAlignmentX(Component.LEFT_ALIGNMENT);
        judulInterv.setBorder(new EmptyBorder(0, 0, 6, 0));
        tengah.add(judulInterv);

        tblRR.setModel(modelRR);
        tblRT.setModel(modelRT);
        for (widget.Table t : new widget.Table[]{tblRR, tblRT}) {
            t.setRowHeight(30);
            aturLebarKolomInterv(t);
            t.getColumnModel().getColumn(1).setCellRenderer(new WrapRenderer());
        }
        JScrollPane scrollRR = new JScrollPane(tblRR);
        scrollRR.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollRR.setPreferredSize(new Dimension(1440, 260));
        scrollRR.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        tengah.add(scrollRR);
        tengah.add(Box.createVerticalStrut(6));
        JScrollPane scrollRT = new JScrollPane(tblRT);
        scrollRT.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollRT.setPreferredSize(new Dimension(1440, 300));
        scrollRT.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        tengah.add(scrollRT);
        tengah.add(Box.createVerticalStrut(8));

        tengah.add(barisPetugasSlot("Nama dan Paraf Petugas yang Melakukan Intervensi", tPetugasIntervensiNama, kdPetugasIntervensi, btnPetugasIntervensi));

        JScrollPane scrollLuar = new JScrollPane(tengah);
        scrollLuar.setBorder(null);
        scrollLuar.getVerticalScrollBar().setUnitIncrement(20);
        getContentPane().add(scrollLuar, BorderLayout.CENTER);

        BtnSimpan.setText("Simpan");
        BtnSimpan.addActionListener(e -> simpan());
        BtnCetak.setText("Cetak");
        BtnCetak.addActionListener(e -> cetak());
        BtnKeluar.setText("Keluar");
        BtnKeluar.addActionListener(e -> dispose());
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 9));
        bawah.setBackground(Color.WHITE);
        bawah.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, garis));
        bawah.add(BtnSimpan);
        bawah.add(BtnCetak);
        bawah.add(BtnKeluar);
        getContentPane().add(bawah, BorderLayout.SOUTH);

        for (int s = 0; s < N_SLOT; s++) { dtpTgl[s].setDate(new Date()); }
    }

    private JPanel barisRingkasSlot(String judul, JLabel[] target, boolean warnaKategori) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel lJudul = new JLabel(judul);
        lJudul.setFont(new Font("Tahoma", Font.BOLD, 11));
        lJudul.setPreferredSize(new Dimension(466, 26));
        row.add(lJudul, BorderLayout.WEST);
        JPanel grid = new JPanel(new GridLayout(1, N_SLOT, 4, 0));
        grid.setOpaque(false);
        for (int s = 0; s < N_SLOT; s++) {
            JLabel l = new JLabel("-", JLabel.CENTER);
            l.setOpaque(true);
            l.setBackground(new Color(238, 240, 242));
            l.setFont(new Font("Tahoma", Font.BOLD, 11));
            l.setBorder(BorderFactory.createLineBorder(new Color(215, 224, 230)));
            target[s] = l;
            grid.add(l);
        }
        row.add(grid, BorderLayout.CENTER);
        return row;
    }

    private JPanel barisPetugasSlot(String judul, widget.TextBox[] namaArr, widget.TextBox[] kdArr, widget.Button[] btnArr) {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lJudul = new JLabel(judul);
        lJudul.setFont(new Font("Tahoma", Font.BOLD, 11));
        lJudul.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.add(lJudul);
        wrap.add(Box.createVerticalStrut(3));
        JPanel grid = new JPanel(new GridLayout(1, N_SLOT, 4, 0));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int s = 0; s < N_SLOT; s++) {
            namaArr[s] = ro();
            kdArr[s] = ro();
            btnArr[s] = new widget.Button();
            btnArr[s].setText("...");
            JPanel p = new JPanel(new BorderLayout(2, 0));
            p.setOpaque(false);
            namaArr[s].setPreferredSize(new Dimension(80, 24));
            btnArr[s].setPreferredSize(new Dimension(24, 24));
            p.add(namaArr[s], BorderLayout.CENTER);
            p.add(btnArr[s], BorderLayout.EAST);
            grid.add(p);
        }
        wrap.add(grid);
        return wrap;
    }

    private void aturLebarKolomPengkajian() {
        int[] lebar = {130, 640, 46, LEBAR_KOLOM_ASES, LEBAR_KOLOM_ASES, LEBAR_KOLOM_ASES, LEBAR_KOLOM_ASES, LEBAR_KOLOM_ASES};
        for (int i = 0; i < lebar.length; i++) {
            tblPengkajian.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
    }

    private void aturLebarKolomInterv(widget.Table t) {
        int[] lebar = {30, 850, 90, 90, 90, 90, 90};
        for (int i = 0; i < lebar.length; i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
    }

    /** Sekali salah satu ASES dicentang di suatu faktor, opsi lain di faktor & kolom yang sama otomatis lepas (perilaku radio). */
    private void pasangPemicuPengkajian() {
        modelPengkajian.addTableModelListener(e -> {
            if (sedangMemuat) { return; }
            if (e.getType() != TableModelEvent.UPDATE || e.getColumn() < 3) { return; }
            int row = e.getFirstRow();
            int col = e.getColumn();
            Object val = modelPengkajian.getValueAt(row, col);
            if (Boolean.TRUE.equals(val)) {
                int faktorIdx = gridRows[row].faktorIdx;
                sedangMemuat = true;
                for (int r = 0; r < gridRows.length; r++) {
                    if (r != row && gridRows[r].faktorIdx == faktorIdx) {
                        modelPengkajian.setValueAt(false, r, col);
                    }
                }
                sedangMemuat = false;
            }
            perbaruiSkorSlot(col - 3);
        });
    }

    private void perbaruiSkorSlot(int slot) {
        int col = 3 + slot;
        int total = 0;
        for (GridRow gr : gridRows) {
            int r = indeksBaris(gr);
            if (Boolean.TRUE.equals(modelPengkajian.getValueAt(r, col))) {
                total += OPSI_FAKTOR[gr.faktorIdx][gr.opsiIdx].skor;
            }
        }
        boolean adaIsi = total > 0;
        if (!adaIsi) {
            lblTotal[slot].setText("-");
            lblKategori[slot].setText("-");
            lblKategori[slot].setBackground(new Color(238, 240, 242));
            lblKategori[slot].setForeground(Color.BLACK);
            return;
        }
        lblTotal[slot].setText(String.valueOf(total));
        String kategori = total >= 12 ? "Risiko Tinggi" : (total >= 7 ? "Risiko Rendah" : "-");
        lblKategori[slot].setText(kategori);
        if ("Risiko Tinggi".equals(kategori)) {
            lblKategori[slot].setBackground(new Color(253, 231, 231));
            lblKategori[slot].setForeground(new Color(163, 29, 29));
        } else if ("Risiko Rendah".equals(kategori)) {
            lblKategori[slot].setBackground(new Color(224, 247, 234));
            lblKategori[slot].setForeground(new Color(21, 128, 61));
        } else {
            lblKategori[slot].setBackground(new Color(238, 240, 242));
            lblKategori[slot].setForeground(Color.BLACK);
        }
    }

    private int indeksBaris(GridRow gr) {
        for (int i = 0; i < gridRows.length; i++) { if (gridRows[i] == gr) { return i; } }
        return -1;
    }

    private void pasangPetugasPicker() {
        for (int s = 0; s < N_SLOT; s++) {
            final int slot = s;
            btnPetugasPenilai[s].addActionListener(e -> bukaPickerPetugas(slot, false));
            btnPetugasIntervensi[s].addActionListener(e -> bukaPickerPetugas(slot, true));
        }
        pilihPetugas.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) {
                if (slotPetugasDipilih < 0) { return; }
                JTable t = pilihPetugas.getTable();
                if (t.getSelectedRow() != -1) {
                    int r = t.getSelectedRow();
                    String kd = t.getValueAt(r, 0).toString();
                    String nm = t.getValueAt(r, 1).toString();
                    if (modePetugasIntervensi) {
                        kdPetugasIntervensi[slotPetugasDipilih].setText(kd);
                        tPetugasIntervensiNama[slotPetugasDipilih].setText(nm);
                    } else {
                        kdPetugasPenilai[slotPetugasDipilih].setText(kd);
                        tPetugasPenilaiNama[slotPetugasDipilih].setText(nm);
                    }
                }
                slotPetugasDipilih = -1;
            }
        });
    }

    private void bukaPickerPetugas(int slot, boolean intervensi) {
        slotPetugasDipilih = slot;
        modePetugasIntervensi = intervensi;
        pilihPetugas.emptTeks();
        pilihPetugas.isCek();
        pilihPetugas.setSize(900, 540);
        pilihPetugas.setLocationRelativeTo(this);
        pilihPetugas.setVisible(true);
    }

    public void isCek() {
        boolean bisa = akses.getpenilaian_awal_keperawatan_ranap();
        BtnSimpan.setEnabled(bisa);
    }

    /** Dipanggil dari DlgRawatInap tab Risiko Jatuh. */
    public void setNoRm(String norawat) {
        noRawat = norawat == null ? "" : norawat.trim();
        kosongkanForm();
        if (noRawat.isEmpty()) {
            TNoRw.setText(""); TNoRM.setText(""); TPasien.setText(""); TJK.setText(""); TTglLahir.setText("");
            tglLahirPasien = null;
            jkPasien = "";
            gridRows = buatGridRowsPenuh();
            isiBarisModelPengkajian();
            return;
        }
        TNoRw.setText(noRawat);
        tarikIdentitasPasien(noRawat);
        muat(noRawat);
    }

    private void kosongkanForm() {
        sedangMemuat = true;
        for (int r = 0; r < modelPengkajian.getRowCount(); r++) {
            for (int c = 3; c < 8; c++) { modelPengkajian.setValueAt(false, r, c); }
        }
        for (int r = 0; r < modelRR.getRowCount(); r++) { for (int c = 2; c < 7; c++) { modelRR.setValueAt(false, r, c); } }
        for (int r = 0; r < modelRT.getRowCount(); r++) { for (int c = 2; c < 7; c++) { modelRT.setValueAt(false, r, c); } }
        sedangMemuat = false;
        for (int s = 0; s < N_SLOT; s++) {
            dtpTgl[s].setDate(new Date());
            tPetugasPenilaiNama[s].setText("");
            kdPetugasPenilai[s].setText("");
            tPetugasIntervensiNama[s].setText("");
            kdPetugasIntervensi[s].setText("");
            perbaruiSkorSlot(s);
        }
    }

    private void tarikIdentitasPasien(String norawat) {
        tglLahirPasien = null;
        jkPasien = "";
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select p.no_rkm_medis,p.nm_pasien,p.jk,p.tgl_lahir,ifnull(date_format(p.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir_teks "
                + "from reg_periksa rp inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis "
                + "where rp.no_rawat=?")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    jkPasien = nvl(rs.getString("jk"));
                    TJK.setText("L".equalsIgnoreCase(jkPasien) ? "Laki-Laki" : "Perempuan");
                    TTglLahir.setText(rs.getString("tgl_lahir_teks"));
                    tglLahirPasien = rs.getDate("tgl_lahir");
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik identitas risiko jatuh bayi anak : " + e);
        }
        int idxUmur = hitungIndexUmur(new Date());
        int idxJk = hitungIndexJk();
        gridRows = (idxUmur >= 0 || idxJk >= 0) ? buatGridRowsUntukPasien(idxUmur, idxJk) : buatGridRowsPenuh();
        isiBarisModelPengkajian();
    }

    /** Umur dihitung dari tgl lahir pasien pada tanggal pengkajian slot itu. */
    private int hitungIndexUmur(Date tanggalKaji) {
        if (tglLahirPasien == null || tanggalKaji == null) { return -1; }
        // pakai getTime()+Instant.ofEpochMilli, BUKAN date.toInstant() -- java.sql.Date (dari JDBC) melempar
        // UnsupportedOperationException kalau toInstant() dipanggil langsung (batasan spek JDBC utk tipe DATE-only).
        java.time.LocalDate lahir = java.time.Instant.ofEpochMilli(tglLahirPasien.getTime()).atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        java.time.LocalDate kaji = java.time.Instant.ofEpochMilli(tanggalKaji.getTime()).atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        if (kaji.isBefore(lahir)) { return -1; }
        int umurTahun = java.time.Period.between(lahir, kaji).getYears();
        if (umurTahun < 3) { return 0; }
        if (umurTahun < 7) { return 1; }
        if (umurTahun < 13) { return 2; }
        return 3;
    }

    private int hitungIndexJk() {
        if (jkPasien.isEmpty()) { return -1; }
        return "L".equalsIgnoreCase(jkPasien) ? 0 : 1;
    }

    private void muat(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement("select * from risiko_jatuh_bayi_anak where no_rawat=?")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    isiPetugasDefaultSemuaSlot();
                    for (int s = 0; s < N_SLOT; s++) { perbaruiSkorSlot(s); }
                    return;
                }
                sedangMemuat = true;
                for (int s = 1; s <= N_SLOT; s++) {
                    int slot = s - 1;
                    String tgl = rs.getString("a" + s + "_tanggal");
                    if (tgl != null) { setTglJam(dtpTgl[slot], tgl); } else { dtpTgl[slot].setDate(new Date()); }
                    for (int f = 0; f < FAKTOR_KEY.length; f++) {
                        String opsiTerpilih = rs.getString("a" + s + "_" + FAKTOR_KEY[f] + "_opsi");
                        if (opsiTerpilih == null) { continue; }
                        for (GridRow gr : gridRows) {
                            if (gr.faktorIdx == f && OPSI_FAKTOR[f][gr.opsiIdx].teks.equals(opsiTerpilih)) {
                                modelPengkajian.setValueAt(true, indeksBaris(gr), 3 + slot);
                            }
                        }
                    }
                    for (int i = 0; i < TEKS_RR.length; i++) {
                        modelRR.setValueAt("Y".equals(rs.getString("a" + s + "_rr" + (i + 1))), i, 2 + slot);
                    }
                    for (int i = 0; i < TEKS_RT.length; i++) {
                        modelRT.setValueAt("Y".equals(rs.getString("a" + s + "_rt" + (i + 1))), i, 2 + slot);
                    }
                    String penNip = nvl(rs.getString("a" + s + "_petugas_penilai_nip"));
                    String penNama = nvl(rs.getString("a" + s + "_petugas_penilai_nama"));
                    String intNip = nvl(rs.getString("a" + s + "_petugas_intervensi_nip"));
                    String intNama = nvl(rs.getString("a" + s + "_petugas_intervensi_nama"));
                    if (penNip.isEmpty() && penNama.isEmpty()) {
                        isiPetugasDefault(kdPetugasPenilai[slot], tPetugasPenilaiNama[slot]);
                    } else {
                        kdPetugasPenilai[slot].setText(penNip);
                        tPetugasPenilaiNama[slot].setText(penNama);
                    }
                    if (intNip.isEmpty() && intNama.isEmpty()) {
                        isiPetugasDefault(kdPetugasIntervensi[slot], tPetugasIntervensiNama[slot]);
                    } else {
                        kdPetugasIntervensi[slot].setText(intNip);
                        tPetugasIntervensiNama[slot].setText(intNama);
                    }
                }
                sedangMemuat = false;
                for (int s = 0; s < N_SLOT; s++) { perbaruiSkorSlot(s); }
            }
        } catch (Exception e) {
            sedangMemuat = false;
            System.out.println("Notif muat risiko jatuh bayi anak : " + e);
        }
    }

    private void isiPetugasDefaultSemuaSlot() {
        for (int s = 0; s < N_SLOT; s++) {
            isiPetugasDefault(kdPetugasPenilai[s], tPetugasPenilaiNama[s]);
            isiPetugasDefault(kdPetugasIntervensi[s], tPetugasIntervensiNama[s]);
        }
    }

    private void isiPetugasDefault(widget.TextBox kd, widget.TextBox nama) {
        kd.setText(akses.getkode());
        nama.setText(Sequel.cariIsi("select nama from petugas where nip=?", akses.getkode()));
    }

    private void simpan() {
        if (ambil(TNoRw).isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        StringBuilder kolom = new StringBuilder("no_rawat");
        StringBuilder placeholder = new StringBuilder("?");
        StringBuilder updateSet = new StringBuilder();
        java.util.List<Object> nilai = new java.util.ArrayList<>();
        nilai.add(ambil(TNoRw));

        for (int s = 1; s <= N_SLOT; s++) {
            int slot = s - 1;

            int total = 0;
            for (int f = 0; f < FAKTOR_KEY.length; f++) {
                for (GridRow gr : gridRows) {
                    if (gr.faktorIdx == f && Boolean.TRUE.equals(modelPengkajian.getValueAt(indeksBaris(gr), 3 + slot))) {
                        total += OPSI_FAKTOR[f][gr.opsiIdx].skor;
                    }
                }
            }
            boolean slotDipakai = total > 0;

            kolom.append(",a").append(s).append("_tanggal");
            placeholder.append(",?");
            updateSet.append("a").append(s).append("_tanggal=values(a").append(s).append("_tanggal),");
            nilai.add(slotDipakai ? ambilTglJam(dtpTgl[slot]) : null);

            for (int f = 0; f < FAKTOR_KEY.length; f++) {
                String opsiTeks = null; Integer skor = null;
                for (GridRow gr : gridRows) {
                    if (gr.faktorIdx == f && Boolean.TRUE.equals(modelPengkajian.getValueAt(indeksBaris(gr), 3 + slot))) {
                        opsiTeks = OPSI_FAKTOR[f][gr.opsiIdx].teks;
                        skor = OPSI_FAKTOR[f][gr.opsiIdx].skor;
                    }
                }
                kolom.append(",a").append(s).append("_").append(FAKTOR_KEY[f]).append("_opsi,a").append(s).append("_").append(FAKTOR_KEY[f]).append("_skor");
                placeholder.append(",?,?");
                updateSet.append("a").append(s).append("_").append(FAKTOR_KEY[f]).append("_opsi=values(a").append(s).append("_").append(FAKTOR_KEY[f]).append("_opsi),");
                updateSet.append("a").append(s).append("_").append(FAKTOR_KEY[f]).append("_skor=values(a").append(s).append("_").append(FAKTOR_KEY[f]).append("_skor),");
                nilai.add(opsiTeks);
                nilai.add(skor);
            }

            String kategori = total == 0 ? null : (total >= 12 ? "Risiko Tinggi" : (total >= 7 ? "Risiko Rendah" : null));
            kolom.append(",a").append(s).append("_total_skor,a").append(s).append("_kategori");
            placeholder.append(",?,?");
            updateSet.append("a").append(s).append("_total_skor=values(a").append(s).append("_total_skor),");
            updateSet.append("a").append(s).append("_kategori=values(a").append(s).append("_kategori),");
            nilai.add(total == 0 ? null : total);
            nilai.add(kategori);

            kolom.append(",a").append(s).append("_petugas_penilai_nip,a").append(s).append("_petugas_penilai_nama");
            kolom.append(",a").append(s).append("_petugas_intervensi_nip,a").append(s).append("_petugas_intervensi_nama");
            placeholder.append(",?,?,?,?");
            updateSet.append("a").append(s).append("_petugas_penilai_nip=values(a").append(s).append("_petugas_penilai_nip),");
            updateSet.append("a").append(s).append("_petugas_penilai_nama=values(a").append(s).append("_petugas_penilai_nama),");
            updateSet.append("a").append(s).append("_petugas_intervensi_nip=values(a").append(s).append("_petugas_intervensi_nip),");
            updateSet.append("a").append(s).append("_petugas_intervensi_nama=values(a").append(s).append("_petugas_intervensi_nama),");
            nilai.add(ambil(kdPetugasPenilai[slot]));
            nilai.add(ambil(tPetugasPenilaiNama[slot]));
            nilai.add(ambil(kdPetugasIntervensi[slot]));
            nilai.add(ambil(tPetugasIntervensiNama[slot]));

            for (int i = 1; i <= TEKS_RR.length; i++) {
                kolom.append(",a").append(s).append("_rr").append(i);
                placeholder.append(",?");
                updateSet.append("a").append(s).append("_rr").append(i).append("=values(a").append(s).append("_rr").append(i).append("),");
                nilai.add(Boolean.TRUE.equals(modelRR.getValueAt(i - 1, 2 + slot)) ? "Y" : null);
            }
            for (int i = 1; i <= TEKS_RT.length; i++) {
                kolom.append(",a").append(s).append("_rt").append(i);
                placeholder.append(",?");
                updateSet.append("a").append(s).append("_rt").append(i).append("=values(a").append(s).append("_rt").append(i).append("),");
                nilai.add(Boolean.TRUE.equals(modelRT.getValueAt(i - 1, 2 + slot)) ? "Y" : null);
            }
        }

        kolom.append(",created_by,created_at,updated_by,updated_at");
        placeholder.append(",?,now(),?,now()");
        updateSet.append("updated_by=values(updated_by),updated_at=now()");
        nilai.add(akses.getkode());
        nilai.add(akses.getkode());

        String sql = "insert into risiko_jatuh_bayi_anak (" + kolom + ") values (" + placeholder + ") "
                + "on duplicate key update " + updateSet;
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            for (int i = 0; i < nilai.size(); i++) {
                Object v = nilai.get(i);
                if (v instanceof Integer) { ps.setInt(i + 1, (Integer) v); }
                else if (v == null) { ps.setNull(i + 1, java.sql.Types.VARCHAR); }
                else { ps.setString(i + 1, v.toString()); }
            }
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Asesmen risiko jatuh tersimpan.");
            muat(ambil(TNoRw));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan.\n" + e.getMessage());
        }
    }

    private void ensureTable() {
        StringBuilder sql = new StringBuilder("create table if not exists risiko_jatuh_bayi_anak (no_rawat varchar(17) not null primary key,");
        for (int s = 1; s <= N_SLOT; s++) {
            sql.append("a").append(s).append("_tanggal datetime null,");
            for (String fk : FAKTOR_KEY) {
                sql.append("a").append(s).append("_").append(fk).append("_opsi varchar(300) null,");
                sql.append("a").append(s).append("_").append(fk).append("_skor int null,");
            }
            sql.append("a").append(s).append("_total_skor int null,");
            sql.append("a").append(s).append("_kategori varchar(20) null,");
            sql.append("a").append(s).append("_petugas_penilai_nip varchar(20) null,");
            sql.append("a").append(s).append("_petugas_penilai_nama varchar(60) null,");
            sql.append("a").append(s).append("_petugas_intervensi_nip varchar(20) null,");
            sql.append("a").append(s).append("_petugas_intervensi_nama varchar(60) null,");
            for (int i = 1; i <= TEKS_RR.length; i++) { sql.append("a").append(s).append("_rr").append(i).append(" varchar(1) null,"); }
            for (int i = 1; i <= TEKS_RT.length; i++) { sql.append("a").append(s).append("_rt").append(i).append(" varchar(1) null,"); }
        }
        sql.append("created_by varchar(50) null,created_at datetime null,updated_by varchar(50) null,updated_at datetime null"
                + ") ROW_FORMAT=DYNAMIC");
        Sequel.queryu2(sql.toString());
    }

    private void cetak() {
        if (ambil(TNoRw).isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        if (Sequel.cariInteger("select count(*) from risiko_jatuh_bayi_anak where no_rawat=?", ambil(TNoRw)) == 0) {
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

            StringBuilder sql = new StringBuilder();
            for (int s = 1; s <= N_SLOT; s++) {
                if (s > 1) { sql.append(" union all "); }
                sql.append("select a.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,")
                        .append("if(pasien.jk='L','Laki-laki','Perempuan') as jk,")
                        .append("ifnull(date_format(pasien.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir,")
                        .append("ifnull((select bangsal.nm_bangsal from kamar_inap inner join kamar on kamar.kd_kamar=kamar_inap.kd_kamar ")
                        .append("inner join bangsal on bangsal.kd_bangsal=kamar.kd_bangsal where kamar_inap.no_rawat=a.no_rawat ")
                        .append("order by kamar_inap.tgl_masuk desc limit 1),'') as ruang,")
                        .append("ifnull(date_format(a.a").append(s).append("_tanggal,'%d-%m-%Y'),'') as tanggal_kaji,")
                        .append("ifnull(date_format(a.a").append(s).append("_tanggal,'%H:%i'),'') as jam_kaji,");
                for (String fk : FAKTOR_KEY) {
                    sql.append("a.a").append(s).append("_").append(fk).append("_opsi as opsi_").append(fk).append(",");
                    sql.append("a.a").append(s).append("_").append(fk).append("_skor as skor_").append(fk).append(",");
                }
                sql.append("a.a").append(s).append("_total_skor as total_skor,")
                        .append("a.a").append(s).append("_kategori as kategori,");
                for (int i = 1; i <= TEKS_RR.length; i++) { sql.append("a.a").append(s).append("_rr").append(i).append(" as rr").append(i).append(","); }
                for (int i = 1; i <= TEKS_RT.length; i++) { sql.append("a.a").append(s).append("_rt").append(i).append(" as rt").append(i).append(","); }
                sql.append("a.a").append(s).append("_petugas_penilai_nama as petugas_penilai_nama,")
                        .append(fotoSqlByNip("a.a" + s + "_petugas_penilai_nip", "petugas_penilai_photo")).append(",")
                        .append("a.a").append(s).append("_petugas_intervensi_nama as petugas_intervensi_nama,")
                        .append(fotoSqlByNip("a.a" + s + "_petugas_intervensi_nip", "petugas_intervensi_photo"))
                        .append(" from risiko_jatuh_bayi_anak a ")
                        .append("inner join reg_periksa on a.no_rawat=reg_periksa.no_rawat ")
                        .append("inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis ")
                        .append("where a.no_rawat='").append(ambil(TNoRw)).append("' and a.a").append(s).append("_tanggal is not null");
            }
            Valid.MyReportqry("rptAsesmenRisikoJatuhBayiAnak.jasper", "report",
                    "::[ Asesmen Risiko Jatuh Pasien Bayi dan Anak ]::", sql.toString(), param);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak.\n" + e.getMessage());
        }
    }

    private String fotoSqlByNip(String kolomNip, String alias) {
        String sub = "(select p2.photo from pegawai p2 where p2.nik=" + kolomNip + " limit 1)";
        return "if(coalesce(nullif(" + sub + ",''),'')='' or coalesce(nullif(" + sub + ",''),'')='-' "
                + "or coalesce(nullif(" + sub + ",''),'')='pages/pegawai/photo/','',"
                + "replace(coalesce(" + sub + ",''),'\\\\\\\\','/')) as " + alias;
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
    private static widget.TextBox ro() {
        widget.TextBox t = new widget.TextBox();
        t.setEditable(false);
        return t;
    }

    private static widget.Tanggal dt() {
        widget.Tanggal d = new widget.Tanggal();
        d.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        return d;
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

    private JPanel kartuLegenda(String judul, Color teks, Color garis, String... baris) {
        JPanel p = new JPanel();
        p.setBackground(new Color(252, 253, 253));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis), new EmptyBorder(8, 10, 8, 10)));
        JLabel judulL = new JLabel(judul);
        judulL.setFont(new Font("Tahoma", Font.BOLD, 11));
        judulL.setForeground(teks);
        judulL.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(judulL);
        p.add(Box.createVerticalStrut(4));
        for (String b : baris) {
            JLabel l = new JLabel(b);
            l.setFont(new Font("Tahoma", Font.PLAIN, 11));
            l.setForeground(new Color(74, 91, 104));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(l);
        }
        return p;
    }

    private static String ambil(widget.TextBox t) {
        return t.getText() == null ? "" : t.getText().trim();
    }

    private static String nvl(String v) {
        return v == null ? "" : v;
    }

    /** Kolom 0 tabel Pengkajian: label faktor cuma tampil di baris pertama kelompoknya (meniru sel gabungan di kertas). */
    private static final class FaktorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            l.setFont(l.getFont().deriveFont(Font.BOLD));
            l.setVerticalAlignment(JLabel.TOP);
            return l;
        }
    }

    /** Render teks panjang (kolom Skala / item intervensi) dgn HTML supaya kebaca meski sel tak terlalu lebar. */
    private static final class WrapRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            String teks = value == null ? "" : value.toString();
            JLabel l = (JLabel) super.getTableCellRendererComponent(table,
                    "<html><div style='width:" + (table.getColumnModel().getColumn(column).getWidth() - 10) + "px'>" + teks + "</div></html>",
                    isSelected, hasFocus, row, column);
            l.setVerticalAlignment(JLabel.TOP);
            return l;
        }
    }
}
