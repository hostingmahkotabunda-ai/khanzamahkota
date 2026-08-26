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
 * Asesmen Risiko Jatuh Pasien Dewasa (Morse Fall Scale / MFS), Rawat Inap.
 * Layout mengikuti PERSIS kertas asli: tabel skoring 6 faktor x 3 kolom
 * Skoring (Skoring 1 Saat Masuk .. Skoring 3) dgn kotak centang per baris
 * skala (pola sama dgn RMAsesmenRisikoJatuhBayiAnak), lalu di bawahnya
 * ditambahkan tabel Intervensi Pencegahan Jatuh Dewasa (7 item Standar +
 * 9 item Risiko Tinggi, sama persis dgn RMPelaksanaanPencegahanJatuhDewasa)
 * x 3 kolom INTERV, supaya satu form ini lengkap (asesmen + tindak lanjut).
 */
public final class RMAsesmenRisikoJatuhMorse extends JDialog {

    private static final int N_SLOT = 3;
    /** Lebar kolom "Faktor Risiko"+"Skala"+"Skor" (150+560+46) -- dipakai jg utk lebar spacerKiri
     *  baris tanggal Skoring 1-3, spy tanggalnya sejajar persis dgn kolom centang Skoring di tabel. */
    private static final int LEBAR_KOLOM_SEBELUM_SKORING = 150 + 560 + 46;
    /** Lebar 1 kolom Skoring (checkbox) di tabel Pengkajian -- dipakai jg utk lebar 1 slot tanggal. */
    private static final int LEBAR_KOLOM_SKORING = 130;

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();

    private static final class Opsi {
        final String teks; final int skor;
        Opsi(String teks, int skor) { this.teks = teks; this.skor = skor; }
    }

    private static final String[] FAKTOR_LABEL = {"Riwayat Jatuh (3 bulan terakhir)", "Diagnosa Sekunder (>1 penyakit)",
        "Alat Bantu Jalan", "Terapi Intravena (infus)", "Gaya Berjalan / Cara Berpindah", "Status Mental"};
    private static final String[] FAKTOR_KEY = {"riwayat_jatuh", "diagnosa_sekunder", "alat_bantu", "terapi_iv", "gaya_berjalan", "status_mental"};

    private static final Opsi[][] OPSI_FAKTOR = {
        { new Opsi("Tidak", 0), new Opsi("Ya", 25) },
        { new Opsi("Tidak", 0), new Opsi("Ya", 15) },
        { new Opsi("Bed rest / dibantu perawat", 0), new Opsi("Kruk / tongkat / walker", 15),
          new Opsi("Berpegangan pada benda-benda di sekitar (kursi, lemari, meja)", 30) },
        { new Opsi("Tidak", 0), new Opsi("Ya", 20) },
        { new Opsi("Normal / bed rest / immobile (tidak dapat bergerak sendiri)", 0), new Opsi("Lemah (tidak bertenaga)", 10),
          new Opsi("Gangguan / tidak normal (pincang / diseret)", 20) },
        { new Opsi("Pasien menyadari kondisi dirinya", 0), new Opsi("Pasien mengalami keterbatasan daya ingat", 15) },
    };

    private static final String[] TEKS_STANDAR = {
        "Meningkatkan observasi bantuan yang sesuai saat ambulasi",
        "Keselamatan lingkungan: menghindari ruangan yang kacau balau, dekatkan bel dan telepon, biarkan pintu terbuka, gunakan lampu malam hari serta pagar tempat tidur",
        "Monitor kebutuhan pasien. Keluarga menemani pasien yang beresiko jatuh. Bila tidak ada keluarga, pasien diminta untuk menekan bel bila membutuhkan bantuan",
        "Edukasi perilaku untuk mencegah jatuh kepada pasien dan keluarga dengan menempatkan standing akrilik dan edukasi jatuh di meja samping tempat tidur pasien",
        "Gunakan alat bantu jalan (walker, handrail)",
        "Anjurkan pasien menggunakan alas kaki yang tidak licin",
        "Lakukan penilaian ulang risiko jatuh bila ada perubahan kondisi atau pengobatan",
    };
    private static final String[] TEKS_TINGGI = {
        "Pakaikan gelang risiko jatuh berwarna kuning. Pasang tanda risiko jatuh warna kuning pada bed pasien",
        "Melakukan intervensi jatuh standar",
        "Melakukan pencegahan jatuh dengan penilaian jatuh yang lebih detail seperti analisa cara berjalan dengan intervensi spesifik",
        "Menempatkan pasien di dekat nurse station",
        "Memasang handrail yang kokoh dan mudah dijangkau pasien",
        "Menyiapkan komod dan alat bantu jalan",
        "Menjaga lantai kamar mandi dengan karpet anti slip/tidak licin, serta anjurkan menggunakan tempat duduk di kamar mandi saat pasien mandi",
        "Dampingi pasien bila ke kamar mandi, jangan tinggalkan sendiri di toilet, informasikan cara menggunakan bel di toilet untuk memanggil perawat, pintu kamar mandi jangan dikunci",
        "Lakukan penilaian ulang risiko jatuh tiap shift",
    };

    /** Baris grid pengkajian: 1 baris per opsi-skala (14 baris total dari 6 faktor). */
    private static final class GridRow {
        final int faktorIdx; final int opsiIdx;
        GridRow(int f, int o) { faktorIdx = f; opsiIdx = o; }
    }
    private static final GridRow[] GRID_ROWS = buatGridRows();
    private static GridRow[] buatGridRows() {
        java.util.List<GridRow> list = new java.util.ArrayList<>();
        for (int f = 0; f < OPSI_FAKTOR.length; f++) {
            for (int o = 0; o < OPSI_FAKTOR[f].length; o++) {
                list.add(new GridRow(f, o));
            }
        }
        return list.toArray(new GridRow[0]);
    }

    // Header identitas (readonly)
    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TJK = ro();
    private final widget.TextBox TTglLahir = ro();

    private final widget.Tanggal[] dtpTgl = new widget.Tanggal[N_SLOT];
    private final DefaultTableModel modelPengkajian;
    private final widget.Table tblPengkajian;
    private final JLabel[] lblTotal = new JLabel[N_SLOT];
    private final JLabel[] lblKategori = new JLabel[N_SLOT];

    private final widget.TextBox[] tPetugasPenilaiNama = new widget.TextBox[N_SLOT];
    private final widget.TextBox[] kdPetugasPenilai = new widget.TextBox[N_SLOT];
    private final widget.Button[] btnPetugasPenilai = new widget.Button[N_SLOT];

    private final DefaultTableModel modelStandar;
    private final widget.Table tblStandar = new widget.Table();
    private final DefaultTableModel modelTinggi;
    private final widget.Table tblTinggi = new widget.Table();

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

    public RMAsesmenRisikoJatuhMorse(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Asesmen Risiko Jatuh Pasien Dewasa (Morse Fall Scale) ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        ensureTable();

        Object[] kolomPengkajian = {"Faktor Risiko", "Skala", "Skor", "Skoring 1", "Skoring 2", "Skoring 3"};
        modelPengkajian = new DefaultTableModel(kolomPengkajian, 0) {
            @Override public Class<?> getColumnClass(int c) {
                if (c == 2) { return Integer.class; }
                if (c >= 3) { return Boolean.class; }
                return String.class;
            }
            @Override public boolean isCellEditable(int row, int col) { return col >= 3; }
        };
        for (GridRow gr : GRID_ROWS) {
            Opsi o = OPSI_FAKTOR[gr.faktorIdx][gr.opsiIdx];
            String labelFaktor = gr.opsiIdx == 0 ? FAKTOR_LABEL[gr.faktorIdx] : "";
            modelPengkajian.addRow(new Object[]{labelFaktor, o.teks, o.skor, false, false, false});
        }

        tblPengkajian = new widget.Table() {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (c instanceof javax.swing.JComponent) {
                    boolean awalKelompok = row > 0 && row < GRID_ROWS.length && GRID_ROWS[row].opsiIdx == 0;
                    ((javax.swing.JComponent) c).setBorder(awalKelompok
                            ? BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(120, 130, 140))
                            : BorderFactory.createEmptyBorder());
                }
                return c;
            }
        };

        modelStandar = buatModelInterv(TEKS_STANDAR);
        modelTinggi = buatModelInterv(TEKS_TINGGI);

        initComponents();
        pasangPemicuPengkajian();
        pasangPetugasPicker();
        setSize(1250, 980);
        setMinimumSize(new Dimension(1050, 780));
        setLocationRelativeTo(parent);
    }

    private DefaultTableModel buatModelInterv(String[] teksItem) {
        DefaultTableModel m = new DefaultTableModel(new Object[]{"No", "Item", "I1", "I2", "I3"}, 0) {
            @Override public Class<?> getColumnClass(int c) { return c >= 2 ? Boolean.class : (c == 0 ? Integer.class : String.class); }
            @Override public boolean isCellEditable(int row, int col) { return col >= 2; }
        };
        for (int i = 0; i < teksItem.length; i++) {
            m.addRow(new Object[]{i + 1, teksItem[i], false, false, false});
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
        JLabel judulUtama = new JLabel("Asesmen Risiko Jatuh Pasien Dewasa");
        judulUtama.setFont(new Font("Tahoma", Font.BOLD, 20));
        judulUtama.setForeground(teks);
        JLabel subJudul = new JLabel("Morse Fall Scale (MFS)  •  Rawat Inap  •  Skoring 1 (Saat Masuk) s/d Skoring 3");
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

        JPanel legenda = kartuLegenda("Tingkatan Risiko (Nilai MFS)", teks, garis,
                "0 - 24 : Tidak Berisiko -> Perawatan dasar",
                "25 - 50 : Risiko Rendah -> Intervensi pencegahan jatuh standar",
                ">= 51 : Risiko Tinggi -> Intervensi pencegahan jatuh risiko tinggi",
                "Tiap kolom Skoring = satu kali waktu penilaian. Centang salah satu skala per faktor untuk kolom itu.");
        tengah.add(legenda);
        tengah.add(Box.createVerticalStrut(8));

        // ----- Baris tanggal Skoring 1-3 -----
        // Lebar spacerKiri & tglGrid HARUS PERSIS sama dgn lebar kolom tabel di bawahnya (lihat
        // aturLebarKolomPengkajian()) spy tiap tanggal Skoring N sejajar/rata persis di atas kolom
        // centang Skoring N -- makanya tglGrid dibungkus panel FlowLayout(LEFT) yg TIDAK melar
        // ikut lebar dialog (beda dgn BorderLayout.CENTER biasa yg akan melar & jadi geser).
        JPanel barisTanggal = new JPanel(new BorderLayout());
        barisTanggal.setOpaque(false);
        barisTanggal.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel spacerKiri = new JPanel();
        spacerKiri.setOpaque(false);
        spacerKiri.setPreferredSize(new Dimension(LEBAR_KOLOM_SEBELUM_SKORING, 30));
        barisTanggal.add(spacerKiri, BorderLayout.WEST);
        JPanel tglGrid = new JPanel(new GridLayout(1, N_SLOT, 0, 0));
        tglGrid.setOpaque(false);
        tglGrid.setPreferredSize(new Dimension(LEBAR_KOLOM_SKORING * N_SLOT, 30));
        for (int s = 0; s < N_SLOT; s++) {
            dtpTgl[s] = dt();
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            JLabel l = new JLabel((s == 0 ? "Skoring 1 (Saat Masuk)" : "Skoring " + (s + 1)), JLabel.CENTER);
            l.setFont(new Font("Tahoma", Font.BOLD, 10));
            l.setForeground(teks);
            p.add(l, BorderLayout.NORTH);
            dtpTgl[s].setPreferredSize(new Dimension(LEBAR_KOLOM_SKORING - 4, 24));
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
        // melar ikut viewport (kalau melar, sejajarnya dgn baris tanggal Skoring 1-3 di atas jadi rusak lagi).
        tblPengkajian.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        aturLebarKolomPengkajian();
        tblPengkajian.getColumnModel().getColumn(0).setCellRenderer(new FaktorRenderer());
        tblPengkajian.getColumnModel().getColumn(1).setCellRenderer(new WrapRenderer());
        tblPengkajian.setPreferredScrollableViewportSize(
                new Dimension(LEBAR_KOLOM_SEBELUM_SKORING + LEBAR_KOLOM_SKORING * N_SLOT, 320));
        JScrollPane scrollPengkajian = new JScrollPane(tblPengkajian);
        scrollPengkajian.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPengkajian.setPreferredSize(new Dimension(1210, 320));
        scrollPengkajian.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        tengah.add(scrollPengkajian);
        tengah.add(Box.createVerticalStrut(6));

        tengah.add(barisRingkasSlot("Total Nilai", lblTotal));
        tengah.add(barisRingkasSlot("Kategori", lblKategori));
        tengah.add(Box.createVerticalStrut(8));

        tengah.add(barisPetugasSlot("Paraf dan Nama Petugas yang Menilai", tPetugasPenilaiNama, kdPetugasPenilai, btnPetugasPenilai));
        tengah.add(Box.createVerticalStrut(14));

        JLabel judulInterv = new JLabel("Intervensi Pencegahan Jatuh Pasien Dewasa (INTERV 1 - 3, sejalan dengan Skoring 1 - 3)");
        judulInterv.setFont(new Font("Tahoma", Font.BOLD, 13));
        judulInterv.setForeground(teks);
        judulInterv.setAlignmentX(Component.LEFT_ALIGNMENT);
        judulInterv.setBorder(new EmptyBorder(0, 0, 6, 0));
        tengah.add(judulInterv);

        JLabel judulStandar = new JLabel("Intervensi Jatuh Standar Bagi Pasien Tidak Berisiko dan Risiko Rendah");
        judulStandar.setFont(new Font("Tahoma", Font.BOLD, 11));
        judulStandar.setForeground(teks);
        judulStandar.setAlignmentX(Component.LEFT_ALIGNMENT);
        tengah.add(judulStandar);
        tblStandar.setModel(modelStandar);
        tblTinggi.setModel(modelTinggi);
        for (widget.Table t : new widget.Table[]{tblStandar, tblTinggi}) {
            t.setRowHeight(28);
            aturLebarKolomInterv(t);
            t.getColumnModel().getColumn(1).setCellRenderer(new WrapRenderer());
        }
        JScrollPane scrollStandar = new JScrollPane(tblStandar);
        scrollStandar.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollStandar.setPreferredSize(new Dimension(1210, 190));
        scrollStandar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        tengah.add(scrollStandar);
        tengah.add(Box.createVerticalStrut(8));

        JLabel judulTinggi = new JLabel("Intervensi Jatuh Risiko Tinggi");
        judulTinggi.setFont(new Font("Tahoma", Font.BOLD, 11));
        judulTinggi.setForeground(teks);
        judulTinggi.setAlignmentX(Component.LEFT_ALIGNMENT);
        tengah.add(judulTinggi);
        JScrollPane scrollTinggi = new JScrollPane(tblTinggi);
        scrollTinggi.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollTinggi.setPreferredSize(new Dimension(1210, 230));
        scrollTinggi.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        tengah.add(scrollTinggi);
        tengah.add(Box.createVerticalStrut(8));

        tengah.add(barisPetugasSlot("Petugas yang Melaksanakan Pencegahan", tPetugasIntervensiNama, kdPetugasIntervensi, btnPetugasIntervensi));

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

    private JPanel barisRingkasSlot(String judul, JLabel[] target) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel lJudul = new JLabel(judul);
        lJudul.setFont(new Font("Tahoma", Font.BOLD, 11));
        lJudul.setPreferredSize(new Dimension(466, 26));
        row.add(lJudul, BorderLayout.WEST);
        JPanel grid = new JPanel(new GridLayout(1, N_SLOT, 6, 0));
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
        JPanel grid = new JPanel(new GridLayout(1, N_SLOT, 6, 0));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int s = 0; s < N_SLOT; s++) {
            namaArr[s] = ro();
            kdArr[s] = ro();
            btnArr[s] = new widget.Button();
            btnArr[s].setText("...");
            JPanel p = new JPanel(new BorderLayout(2, 0));
            p.setOpaque(false);
            namaArr[s].setPreferredSize(new Dimension(120, 24));
            btnArr[s].setPreferredSize(new Dimension(24, 24));
            p.add(namaArr[s], BorderLayout.CENTER);
            p.add(btnArr[s], BorderLayout.EAST);
            grid.add(p);
        }
        wrap.add(grid);
        return wrap;
    }

    private void aturLebarKolomPengkajian() {
        int[] lebar = {150, 560, 46, LEBAR_KOLOM_SKORING, LEBAR_KOLOM_SKORING, LEBAR_KOLOM_SKORING};
        for (int i = 0; i < lebar.length; i++) {
            tblPengkajian.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
    }

    private void aturLebarKolomInterv(widget.Table t) {
        int[] lebar = {30, 900, 90, 90, 90};
        for (int i = 0; i < lebar.length; i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
    }

    /** Sekali salah satu Skoring dicentang di suatu faktor, opsi lain di faktor & kolom yang sama otomatis lepas (perilaku radio). */
    private void pasangPemicuPengkajian() {
        modelPengkajian.addTableModelListener(e -> {
            if (sedangMemuat) { return; }
            if (e.getType() != TableModelEvent.UPDATE || e.getColumn() < 3) { return; }
            int row = e.getFirstRow();
            int col = e.getColumn();
            Object val = modelPengkajian.getValueAt(row, col);
            if (Boolean.TRUE.equals(val)) {
                int faktorIdx = GRID_ROWS[row].faktorIdx;
                sedangMemuat = true;
                for (int r = 0; r < GRID_ROWS.length; r++) {
                    if (r != row && GRID_ROWS[r].faktorIdx == faktorIdx) {
                        modelPengkajian.setValueAt(false, r, col);
                    }
                }
                sedangMemuat = false;
            }
            perbaruiSkorSlot(col - 3);
        });
    }

    private int indeksBaris(GridRow gr) {
        for (int i = 0; i < GRID_ROWS.length; i++) { if (GRID_ROWS[i] == gr) { return i; } }
        return -1;
    }

    private int totalSlot(int slot) {
        int col = 3 + slot;
        int total = 0;
        for (GridRow gr : GRID_ROWS) {
            if (Boolean.TRUE.equals(modelPengkajian.getValueAt(indeksBaris(gr), col))) {
                total += OPSI_FAKTOR[gr.faktorIdx][gr.opsiIdx].skor;
            }
        }
        return total;
    }

    /** Slot dianggap "lengkap" (bukan cuma total>0, krn 0 sendiri kategori sah / Tidak Berisiko) kalau ke-6 faktor sudah ada pilihan. */
    private boolean slotLengkap(int slot) {
        int col = 3 + slot;
        boolean[] adaPerFaktor = new boolean[FAKTOR_KEY.length];
        for (GridRow gr : GRID_ROWS) {
            if (Boolean.TRUE.equals(modelPengkajian.getValueAt(indeksBaris(gr), col))) {
                adaPerFaktor[gr.faktorIdx] = true;
            }
        }
        for (boolean b : adaPerFaktor) { if (!b) { return false; } }
        return true;
    }

    private void perbaruiSkorSlot(int slot) {
        boolean lengkap = slotLengkap(slot);
        int total = totalSlot(slot);
        if (!lengkap) {
            lblTotal[slot].setText(total > 0 ? String.valueOf(total) : "-");
            lblKategori[slot].setText("-");
            lblKategori[slot].setBackground(new Color(238, 240, 242));
            lblKategori[slot].setForeground(Color.BLACK);
            return;
        }
        lblTotal[slot].setText(String.valueOf(total));
        String kategori = kategoriDariSkor(total);
        lblKategori[slot].setText(kategori);
        if ("Risiko Tinggi".equals(kategori)) {
            lblKategori[slot].setBackground(new Color(253, 231, 231));
            lblKategori[slot].setForeground(new Color(163, 29, 29));
        } else if ("Risiko Rendah".equals(kategori)) {
            lblKategori[slot].setBackground(new Color(255, 244, 214));
            lblKategori[slot].setForeground(new Color(146, 100, 6));
        } else {
            lblKategori[slot].setBackground(new Color(224, 247, 234));
            lblKategori[slot].setForeground(new Color(21, 128, 61));
        }
    }

    private static String kategoriDariSkor(int total) {
        if (total >= 51) { return "Risiko Tinggi"; }
        if (total >= 25) { return "Risiko Rendah"; }
        return "Tidak Berisiko";
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
            return;
        }
        TNoRw.setText(noRawat);
        tarikIdentitasPasien(noRawat);
        muat(noRawat);
    }

    private void kosongkanForm() {
        sedangMemuat = true;
        for (int r = 0; r < modelPengkajian.getRowCount(); r++) {
            for (int c = 3; c < 3 + N_SLOT; c++) { modelPengkajian.setValueAt(false, r, c); }
        }
        for (int r = 0; r < modelStandar.getRowCount(); r++) { for (int c = 2; c < 2 + N_SLOT; c++) { modelStandar.setValueAt(false, r, c); } }
        for (int r = 0; r < modelTinggi.getRowCount(); r++) { for (int c = 2; c < 2 + N_SLOT; c++) { modelTinggi.setValueAt(false, r, c); } }
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
            System.out.println("Notif tarik identitas risiko jatuh morse : " + e);
        }
    }

    private void muat(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement("select * from risiko_jatuh_morse where no_rawat=?")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    isiPetugasDefaultSemuaSlot();
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
                        for (GridRow gr : GRID_ROWS) {
                            if (gr.faktorIdx == f && OPSI_FAKTOR[f][gr.opsiIdx].teks.equals(opsiTerpilih)) {
                                modelPengkajian.setValueAt(true, indeksBaris(gr), 3 + slot);
                            }
                        }
                    }
                    for (int i = 0; i < TEKS_STANDAR.length; i++) {
                        modelStandar.setValueAt("Y".equals(rs.getString("a" + s + "_std" + (i + 1))), i, 2 + slot);
                    }
                    for (int i = 0; i < TEKS_TINGGI.length; i++) {
                        modelTinggi.setValueAt("Y".equals(rs.getString("a" + s + "_tinggi" + (i + 1))), i, 2 + slot);
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
            System.out.println("Notif muat risiko jatuh morse : " + e);
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
            boolean lengkap = slotLengkap(slot);
            int total = totalSlot(slot);

            kolom.append(",a").append(s).append("_tanggal");
            placeholder.append(",?");
            updateSet.append("a").append(s).append("_tanggal=values(a").append(s).append("_tanggal),");
            nilai.add(lengkap ? ambilTglJam(dtpTgl[slot]) : null);

            for (int f = 0; f < FAKTOR_KEY.length; f++) {
                String opsiTeks = null; Integer skor = null;
                for (GridRow gr : GRID_ROWS) {
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

            String kategori = lengkap ? kategoriDariSkor(total) : null;
            kolom.append(",a").append(s).append("_total_skor,a").append(s).append("_kategori");
            placeholder.append(",?,?");
            updateSet.append("a").append(s).append("_total_skor=values(a").append(s).append("_total_skor),");
            updateSet.append("a").append(s).append("_kategori=values(a").append(s).append("_kategori),");
            nilai.add(lengkap ? total : null);
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

            for (int i = 1; i <= TEKS_STANDAR.length; i++) {
                kolom.append(",a").append(s).append("_std").append(i);
                placeholder.append(",?");
                updateSet.append("a").append(s).append("_std").append(i).append("=values(a").append(s).append("_std").append(i).append("),");
                nilai.add(Boolean.TRUE.equals(modelStandar.getValueAt(i - 1, 2 + slot)) ? "Y" : null);
            }
            for (int i = 1; i <= TEKS_TINGGI.length; i++) {
                kolom.append(",a").append(s).append("_tinggi").append(i);
                placeholder.append(",?");
                updateSet.append("a").append(s).append("_tinggi").append(i).append("=values(a").append(s).append("_tinggi").append(i).append("),");
                nilai.add(Boolean.TRUE.equals(modelTinggi.getValueAt(i - 1, 2 + slot)) ? "Y" : null);
            }
        }

        kolom.append(",created_by,created_at,updated_by,updated_at");
        placeholder.append(",?,now(),?,now()");
        updateSet.append("updated_by=values(updated_by),updated_at=now()");
        nilai.add(akses.getkode());
        nilai.add(akses.getkode());

        String sql = "insert into risiko_jatuh_morse (" + kolom + ") values (" + placeholder + ") "
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
        StringBuilder sql = new StringBuilder("create table if not exists risiko_jatuh_morse (no_rawat varchar(17) not null primary key,");
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
            for (int i = 1; i <= TEKS_STANDAR.length; i++) { sql.append("a").append(s).append("_std").append(i).append(" varchar(1) null,"); }
            for (int i = 1; i <= TEKS_TINGGI.length; i++) { sql.append("a").append(s).append("_tinggi").append(i).append(" varchar(1) null,"); }
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
        if (Sequel.cariInteger("select count(*) from risiko_jatuh_morse where no_rawat=?", ambil(TNoRw)) == 0) {
            JOptionPane.showMessageDialog(this, "Simpan minimal satu pengkajian terlebih dahulu sebelum mencetak.");
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
                        .append("a.a").append(s).append("_kategori as kategori,")
                        .append("(case when a.a").append(s).append("_kategori='Risiko Tinggi' then 'Pelaksanaan intervensi pencegahan jatuh risiko tinggi' ")
                        .append("when a.a").append(s).append("_kategori='Risiko Rendah' then 'Pelaksanaan intervensi pencegahan jatuh standar' ")
                        .append("else 'Perawatan dasar' end) as tindakan,")
                        .append("a.a").append(s).append("_petugas_penilai_nama as petugas_nama,")
                        .append(fotoSqlByNip("a.a" + s + "_petugas_penilai_nip", "petugas_photo"))
                        .append(" from risiko_jatuh_morse a ")
                        .append("inner join reg_periksa on a.no_rawat=reg_periksa.no_rawat ")
                        .append("inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis ")
                        .append("where a.no_rawat='").append(ambil(TNoRw)).append("' and a.a").append(s).append("_tanggal is not null");
            }
            Valid.MyReportqry("rptAsesmenRisikoJatuhMorse.jasper", "report",
                    "::[ Asesmen Risiko Jatuh Pasien Dewasa (Morse) ]::", sql.toString(), param);
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

    /** Foto TTD petugas berdasarkan NIP, ditarik dari pegawai.photo. Di-cache per NIP. */
    private final Map<String, ImageIcon> cacheParaf = new HashMap<>();
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
