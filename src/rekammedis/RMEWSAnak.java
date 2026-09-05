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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
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

/**
 * Early Warning Scoring System (Anak), RM 9.1 REV001-03/23. Isinya (kriteria/skor/warna/tabel
 * referensi usia) IDENTIK dgn RMEWSBayi -- dokumen resmi RS utk form Anak memang sama persis dgn
 * form Bayi (termasuk tabel referensi usia Neonatus/Bayi yg sama). Sengaja dibuat class TERPISAH
 * (bukan parameterisasi 1 class) krn form Dewasa (menyusul) kemungkinan kriterianya beda, dan pola
 * "1 dokumen kertas = 1 class+tabel sendiri" ini konsisten dgn form2 lain di proyek (spt
 * RMPenilaianAwalKeperawatanRanapBayi vs RMAsesmenKeperawatanAnak vs ...Dewasa).
 */
public final class RMEWSAnak extends JDialog {

    private static final int N_SLOT = 6;
    private static final int LEBAR_KOLOM_KATEGORI = 140;
    private static final int LEBAR_KOLOM_KRITERIA = 780;
    private static final int LEBAR_KOLOM_SKOR = 50;
    private static final int LEBAR_KOLOM_SEBELUM_SLOT = LEBAR_KOLOM_KATEGORI + LEBAR_KOLOM_KRITERIA + LEBAR_KOLOM_SKOR;
    private static final int LEBAR_KOLOM_SLOT = 95;
    private static final int LEBAR_TOTAL = LEBAR_KOLOM_SEBELUM_SLOT + LEBAR_KOLOM_SLOT * N_SLOT;

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();

    private static final class Opsi {
        final String teks; final int skor; final Color warna;
        Opsi(String teks, int skor, Color warna) { this.teks = teks; this.skor = skor; this.warna = warna; }
    }

    private static final Color C_PUTIH = Color.WHITE;
    private static final Color C_BIRU = new Color(144, 202, 249);
    private static final Color C_KUNING = new Color(255, 245, 157);
    private static final Color C_SALEM = new Color(230, 184, 175);
    private static final Color C_ORANYE = new Color(255, 183, 77);
    private static final Color C_HIJAU = new Color(165, 214, 167);
    private static final Color C_MERAH = new Color(239, 154, 154);

    private static final String[] KATEGORI_LABEL = {"Keadaan Umum", "Kardiovaskular", "Respirasi"};
    private static final String[] KATEGORI_KEY = {"keadaan_umum", "kardiovaskular", "respirasi"};

    // Wording & skor VERBATIM kertas asli (RM 9.1 Anak) -- termasuk typo "Intraksi","Latergi","sianonis","Tanpak","derik".
    private static final Opsi[][] OPSI_KATEGORI = {
        { // KEADAAN UMUM
            new Opsi("Intraksi biasa", 0, C_PUTIH),
            new Opsi("Somnolen atau rewel tetapi dapat ditenangkan", 1, C_KUNING),
            new Opsi("Iritabel, tidak dapat ditenangkan", 2, C_SALEM),
            new Opsi("Latergi, Gelisah, penurunan respon terhadap nyeri", 3, C_ORANYE),
        },
        { // KARDIOVASKULAR
            new Opsi("Tidak sianonis ATAU pengisian kapiler <2 detik", 0, C_BIRU),
            new Opsi("Tanpak pucat ATAU pengisian kapiler 2 detik", 1, C_KUNING),
            new Opsi("Tampak sianotik ATAU pengisian kapiler >20x di atas parameter RR sesuai usia/mt", 2, C_HIJAU),
            new Opsi("Sianotik dan ATAU pengisian kapiler >5 derik ATAU Takikardi >30x di atas parameter RR sesuai usia/mt ATAU Bradikardi (sesuai usia)", 3, C_MERAH),
        },
        { // RESPIRASI
            new Opsi("Respirasi dalam parameter normal tidak terdapat retraksi", 0, C_BIRU),
            new Opsi("Respirasi > 10x di atas parameter RR sesuai usia per menit ATAU menggunakan otot alat bantu napas ATAU menggunakan FiO2 lebih 30%", 1, C_HIJAU),
            new Opsi("Respirasi > 20x di atas parameter RR sesuai usia/menit, ATAU ada retraksi, ATAU menggunakan FiO2 lebih dari 40%", 2, C_KUNING),
            new Opsi("Respirasi > 30x di atas parameter normal, atau ≥ 5x dibawah RR sesuai usia permenit dengan retraksi berat ATAU Merintih, ATAU menggunakan FiO2 lebih dari 50%", 3, C_MERAH),
        },
    };

    private static final class GridRow {
        final int katIdx; final int opsiIdx;
        GridRow(int k, int o) { katIdx = k; opsiIdx = o; }
    }
    private static final GridRow[] GRID_ROWS = buatGridRows();
    private static GridRow[] buatGridRows() {
        java.util.List<GridRow> list = new java.util.ArrayList<>();
        for (int k = 0; k < OPSI_KATEGORI.length; k++) {
            for (int o = 0; o < OPSI_KATEGORI[k].length; o++) {
                list.add(new GridRow(k, o));
            }
        }
        return list.toArray(new GridRow[0]);
    }
    private static int indeksBaris(int katIdx, int opsiIdx) {
        for (int i = 0; i < GRID_ROWS.length; i++) {
            if (GRID_ROWS[i].katIdx == katIdx && GRID_ROWS[i].opsiIdx == opsiIdx) { return i; }
        }
        return -1;
    }
    private boolean awalKelompok(int idx) {
        if (idx < 0 || idx >= GRID_ROWS.length) { return false; }
        return idx == 0 || GRID_ROWS[idx].katIdx != GRID_ROWS[idx - 1].katIdx;
    }
    private boolean tengahKelompok(int idx) {
        if (idx < 0 || idx >= GRID_ROWS.length) { return false; }
        int mulai = idx;
        while (mulai > 0 && GRID_ROWS[mulai - 1].katIdx == GRID_ROWS[idx].katIdx) { mulai--; }
        int akhir = idx;
        while (akhir < GRID_ROWS.length - 1 && GRID_ROWS[akhir + 1].katIdx == GRID_ROWS[idx].katIdx) { akhir++; }
        return idx == mulai + (akhir - mulai) / 2;
    }

    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TJK = ro();
    private final widget.TextBox TTglLahir = ro();

    private final widget.Tanggal[] dtpTgl = new widget.Tanggal[N_SLOT];
    private final DefaultTableModel modelPengkajian;
    private final widget.Table tblPengkajian = new widget.Table() {
        @Override
        public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);
            if (c instanceof javax.swing.JComponent) {
                boolean perluGarisPemisah = row > 0 && awalKelompok(row);
                ((javax.swing.JComponent) c).setBorder(perluGarisPemisah
                        ? BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(90, 100, 110))
                        : BorderFactory.createEmptyBorder());
            }
            return c;
        }
    };
    private final JLabel[] lblTotal = new JLabel[N_SLOT];
    private final JLabel[] lblLevel = new JLabel[N_SLOT];

    private final widget.Button BtnSimpan = new widget.Button();
    private final widget.Button BtnCetak = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();

    private String noRawat = "";
    private boolean sedangMemuat = false;

    public RMEWSAnak(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Early Warning Scoring System (Anak) - RM 9.1 ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        ensureTable();

        Object[] kolom = {"Kategori", "Kriteria", "Skor", "1", "2", "3", "4", "5", "6"};
        modelPengkajian = new DefaultTableModel(kolom, 0) {
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

        initComponents();
        pasangPemicuPengkajian();
        setSize(Math.min(1800, LEBAR_TOTAL + 60), 900);
        setMinimumSize(new Dimension(1300, 720));
        setLocationRelativeTo(parent);
    }

    private void isiBarisModelPengkajian() {
        modelPengkajian.setRowCount(0);
        for (int i = 0; i < GRID_ROWS.length; i++) {
            GridRow gr = GRID_ROWS[i];
            Opsi o = OPSI_KATEGORI[gr.katIdx][gr.opsiIdx];
            String labelKategori = tengahKelompok(i) ? KATEGORI_LABEL[gr.katIdx] : "";
            modelPengkajian.addRow(new Object[]{labelKategori, (gr.opsiIdx + 1) + ". " + o.teks,
                o.skor, false, false, false, false, false, false});
        }
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
        JLabel judulUtama = new JLabel("Early Warning Scoring System (Anak)");
        judulUtama.setFont(new Font("Times New Roman", Font.BOLD, 20));
        judulUtama.setForeground(teks);
        JLabel subJudul = new JLabel("RM 9.1  •  Rawat Inap/Rawat Jalan  •  Penilaian 1 s/d 6");
        subJudul.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        subJudul.setForeground(new Color(91, 105, 115));
        JPanel judulBox = new JPanel();
        judulBox.setOpaque(false);
        judulBox.setLayout(new BoxLayout(judulBox, BoxLayout.Y_AXIS));
        judulUtama.setAlignmentX(Component.LEFT_ALIGNMENT);
        subJudul.setAlignmentX(Component.LEFT_ALIGNMENT);
        judulBox.add(judulUtama);
        judulBox.add(subJudul);
        atas.add(judulBox, BorderLayout.NORTH);

        JPanel ringkasanPasien = new JPanel(new GridLayout(1, 4, 0, 0));
        ringkasanPasien.setBackground(Color.WHITE);
        ringkasanPasien.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis), new EmptyBorder(10, 12, 10, 12)));
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

        JPanel barisTanggal = new JPanel(new BorderLayout());
        barisTanggal.setOpaque(false);
        barisTanggal.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel spacerKiri = new JPanel();
        spacerKiri.setOpaque(false);
        spacerKiri.setPreferredSize(new Dimension(LEBAR_KOLOM_SEBELUM_SLOT, 34));
        barisTanggal.add(spacerKiri, BorderLayout.WEST);
        JPanel tglGrid = new JPanel(new GridLayout(1, N_SLOT, 0, 0));
        tglGrid.setOpaque(false);
        tglGrid.setPreferredSize(new Dimension(LEBAR_KOLOM_SLOT * N_SLOT, 34));
        for (int s = 0; s < N_SLOT; s++) {
            final int slot = s;
            dtpTgl[s] = dt();
            dtpTgl[s].addPropertyChangeListener("date", evt -> perbaruiSkorSlot(slot));
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            JLabel l = new JLabel("Penilaian " + (s + 1), JLabel.CENTER);
            l.setFont(new Font("Times New Roman", Font.BOLD, 10));
            l.setForeground(teks);
            p.add(l, BorderLayout.NORTH);
            dtpTgl[s].setPreferredSize(new Dimension(LEBAR_KOLOM_SLOT - 4, 24));
            p.add(dtpTgl[s], BorderLayout.CENTER);
            tglGrid.add(p);
        }
        JPanel pembungkusTglGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pembungkusTglGrid.setOpaque(false);
        pembungkusTglGrid.add(tglGrid);
        barisTanggal.add(pembungkusTglGrid, BorderLayout.CENTER);
        tengah.add(barisTanggal);
        tengah.add(Box.createVerticalStrut(4));

        tblPengkajian.setModel(modelPengkajian);
        tblPengkajian.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        tblPengkajian.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 13));
        tblPengkajian.setRowHeight(34);
        tblPengkajian.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        aturLebarKolomPengkajian();
        tblPengkajian.getColumnModel().getColumn(0).setCellRenderer(new KategoriLabelRenderer());
        tblPengkajian.getColumnModel().getColumn(1).setCellRenderer(new KriteriaRenderer());
        tblPengkajian.getColumnModel().getColumn(2).setCellRenderer(new KategoriRenderer(false));
        for (int c = 3; c < 3 + N_SLOT; c++) {
            tblPengkajian.getColumnModel().getColumn(c).setCellRenderer(new KategoriRenderer(true));
        }
        aturTinggiBarisOtomatis();
        int tinggiTabel = 0;
        for (int r = 0; r < modelPengkajian.getRowCount(); r++) { tinggiTabel += tblPengkajian.getRowHeight(r); }
        tblPengkajian.setPreferredScrollableViewportSize(new Dimension(LEBAR_TOTAL, tinggiTabel));
        JScrollPane scrollPengkajian = new JScrollPane(tblPengkajian);
        scrollPengkajian.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPengkajian.setPreferredSize(new Dimension(LEBAR_TOTAL, Math.min(tinggiTabel + 20, 520)));
        scrollPengkajian.setMaximumSize(new Dimension(Integer.MAX_VALUE, Math.min(tinggiTabel + 20, 520)));
        tengah.add(scrollPengkajian);
        tengah.add(Box.createVerticalStrut(6));

        tengah.add(barisRingkasSlot("TOTAL SKOR", lblTotal, false));
        tengah.add(barisRingkasSlot("Level & Respon", lblLevel, true));
        tengah.add(Box.createVerticalStrut(10));

        tengah.add(panelReferensiUsia(teks, garis));
        tengah.add(Box.createVerticalStrut(8));
        tengah.add(panelLevelSkorRespon(teks, garis));

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

    private JPanel panelReferensiUsia(Color teks, Color garis) {
        String[][] data = {
            {"Neonatus", "0-1 bulan", "100-180", "40-60"},
            {"Bayi", "1-12 bulan", "100-180", "35-40"},
        };
        DefaultTableModel m = new DefaultTableModel(new Object[]{"", "Usia", "Nadi Saat Istirahat (kali/menit)", "Nafas Saat Istirahat (nafas/menit)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (String[] baris : data) { m.addRow(baris); }
        widget.Table t = new widget.Table();
        t.setModel(m);
        t.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        t.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 13));
        t.setRowHeight(24);
        t.setEnabled(false);
        JScrollPane sc = new JScrollPane(t);
        sc.setAlignmentX(Component.LEFT_ALIGNMENT);
        sc.setPreferredSize(new Dimension(LEBAR_TOTAL, 90));
        sc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        sc.setBorder(BorderFactory.createTitledBorder("Parameter Normal Sesuai Usia"));
        return bungkusKartu(sc);
    }

    private JPanel panelLevelSkorRespon(Color teks, Color garis) {
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 1));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(BorderFactory.createTitledBorder("Level Skor & Respon"));
        p.add(barisLevel("SKOR 0-2", C_HIJAU,
                "Pasien dalam keadaan stabil, jika skor 0 lakukan evaluasi secara rutin tiap 8 jam, jika skor naik 1 atau lebih, lakukan evaluasi setiap 4 jam, jika diperlukan assessment oleh dokter jaga bangsal"));
        p.add(barisLevel("SKOR 3-4", C_KUNING,
                "Ada penurunan kondisi pasien, assessment oleh dokter jaga bangsal, lakukan evaluasi ulang setiap 2 jam atau lebih cepat, konsultasi ke DPJP, lakukan sesuai instruksi, jika diperlukan dipindahkan ke area dengan monitoring yang sesuai"));
        p.add(barisLevel("SKOR 5 atau lebih", C_MERAH,
                "Ada perubahan yang signifikan, lakukan resusitasi, monitoring secara kontinyu, aktifitas kode blue kegawatan medis (1124) respon tim medis emergency (TME) segera, maksimal 10 menit, informasikan dan konsulkan ke DPJP"));
        p.add(barisLevel("HENTI JANTUNG", new Color(183, 28, 28),
                "Lakukan RJP oleh petugas/tim primer, aktivasi code blue henti jantung (1124), respon tim medis emergency, maksimal 5 menit, informasikan dan konsultasikan ke DPJP"));
        return bungkusKartu(p);
    }

    private JPanel barisLevel(String judul, Color warna, String teksRespon) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(4, 4, 4, 4));
        JLabel lJudul = new JLabel("<html><div style='width:110px;text-align:center'>" + judul + "</div></html>", JLabel.CENTER);
        lJudul.setOpaque(true);
        lJudul.setBackground(warna);
        lJudul.setForeground(warna.equals(new Color(183, 28, 28)) ? Color.WHITE : new Color(40, 40, 40));
        lJudul.setFont(new Font("Times New Roman", Font.BOLD, 11));
        lJudul.setPreferredSize(new Dimension(130, 40));
        lJudul.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        row.add(lJudul, BorderLayout.WEST);
        JLabel lTeks = new JLabel("<html><div style='width:" + (LEBAR_TOTAL - 180) + "px'>" + teksRespon + "</div></html>");
        lTeks.setFont(new Font("Times New Roman", Font.PLAIN, 11));
        row.add(lTeks, BorderLayout.CENTER);
        return row;
    }

    private JPanel bungkusKartu(java.awt.Component isi) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(isi, BorderLayout.CENTER);
        return p;
    }

    private JPanel barisRingkasSlot(String judul, JLabel[] target, boolean isLevel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel lJudul = new JLabel(judul);
        lJudul.setFont(new Font("Times New Roman", Font.BOLD, 11));
        lJudul.setPreferredSize(new Dimension(LEBAR_KOLOM_SEBELUM_SLOT, 26));
        row.add(lJudul, BorderLayout.WEST);
        JPanel grid = new JPanel(new GridLayout(1, N_SLOT, 4, 0));
        grid.setOpaque(false);
        for (int s = 0; s < N_SLOT; s++) {
            JLabel l = new JLabel("-", JLabel.CENTER);
            l.setOpaque(true);
            l.setBackground(new Color(238, 240, 242));
            l.setFont(new Font("Times New Roman", Font.BOLD, 11));
            l.setBorder(BorderFactory.createLineBorder(new Color(215, 224, 230)));
            target[s] = l;
            grid.add(l);
        }
        row.add(grid, BorderLayout.CENTER);
        return row;
    }

    private void aturLebarKolomPengkajian() {
        int[] lebar = new int[3 + N_SLOT];
        lebar[0] = LEBAR_KOLOM_KATEGORI;
        lebar[1] = LEBAR_KOLOM_KRITERIA;
        lebar[2] = LEBAR_KOLOM_SKOR;
        for (int i = 0; i < N_SLOT; i++) { lebar[3 + i] = LEBAR_KOLOM_SLOT; }
        for (int i = 0; i < lebar.length; i++) {
            tblPengkajian.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
    }

    /** Tinggi tiap baris dihitung otomatis sesuai panjang teks kolom Kriteria (word-wrap) supaya
     *  tidak tumpang tindih (dulu tinggi baris tetap 34px utk semua baris, padahal ada baris dgn
     *  teks panjang yg butuh 2-3 baris tampilan). */
    private void aturTinggiBarisOtomatis() {
        java.awt.FontMetrics fm = tblPengkajian.getFontMetrics(tblPengkajian.getFont());
        int lebarEfektif = LEBAR_KOLOM_KRITERIA - 16;
        for (int r = 0; r < modelPengkajian.getRowCount(); r++) {
            String teksKriteria = String.valueOf(modelPengkajian.getValueAt(r, 1));
            int jmlBaris = hitungJumlahBarisWrap(teksKriteria, fm, lebarEfektif);
            int tinggi = Math.max(30, jmlBaris * (fm.getHeight() + 3) + 8);
            tblPengkajian.setRowHeight(r, tinggi);
        }
    }

    /** Simulasi word-wrap greedy (per kata) utk estimasi jumlah baris teks pada lebar tertentu. */
    private static int hitungJumlahBarisWrap(String teks, java.awt.FontMetrics fm, int lebarMax) {
        if (teks == null || teks.trim().isEmpty()) { return 1; }
        String[] kata = teks.split(" ");
        int jmlBaris = 1;
        int lebarBaris = 0;
        for (String k : kata) {
            int lebarKata = fm.stringWidth(k + " ");
            if (lebarBaris > 0 && lebarBaris + lebarKata > lebarMax) {
                jmlBaris++;
                lebarBaris = lebarKata;
            } else {
                lebarBaris += lebarKata;
            }
        }
        return jmlBaris;
    }

    private void pasangPemicuPengkajian() {
        modelPengkajian.addTableModelListener(e -> {
            if (sedangMemuat) { return; }
            if (e.getType() != TableModelEvent.UPDATE || e.getColumn() < 3) { return; }
            int row = e.getFirstRow();
            int col = e.getColumn();
            Object val = modelPengkajian.getValueAt(row, col);
            if (Boolean.TRUE.equals(val)) {
                int katIdx = GRID_ROWS[row].katIdx;
                sedangMemuat = true;
                for (int r = 0; r < GRID_ROWS.length; r++) {
                    if (r != row && GRID_ROWS[r].katIdx == katIdx) {
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
        int jmlKategoriTerisi = 0;
        for (int k = 0; k < KATEGORI_KEY.length; k++) {
            for (int o = 0; o < OPSI_KATEGORI[k].length; o++) {
                int r = indeksBaris(k, o);
                if (Boolean.TRUE.equals(modelPengkajian.getValueAt(r, col))) {
                    total += OPSI_KATEGORI[k][o].skor;
                    jmlKategoriTerisi++;
                }
            }
        }
        if (jmlKategoriTerisi == 0) {
            lblTotal[slot].setText("-");
            lblLevel[slot].setText("-");
            lblLevel[slot].setBackground(new Color(238, 240, 242));
            lblLevel[slot].setForeground(Color.BLACK);
            return;
        }
        lblTotal[slot].setText(String.valueOf(total));
        String level; Color bg; Color fg = new Color(40, 40, 40);
        if (total <= 2) { level = "Skor 0-2"; bg = C_HIJAU; }
        else if (total <= 4) { level = "Skor 3-4"; bg = C_KUNING; }
        else { level = "Skor ≥ 5"; bg = C_MERAH; }
        lblLevel[slot].setText(level);
        lblLevel[slot].setBackground(bg);
        lblLevel[slot].setForeground(fg);
    }

    public void isCek() {
        boolean bisa = akses.getpenilaian_awal_keperawatan_ranap();
        BtnSimpan.setEnabled(bisa);
    }

    /** Dipanggil dari tab EWS (dropdown "EWS Anak") di DlgRawatInap/DlgRawatJalan. */
    public void setNoRm(String norawat) {
        noRawat = norawat == null ? "" : norawat.trim();
        kosongkanForm();
        if (noRawat.isEmpty()) {
            TNoRM.setText(""); TPasien.setText(""); TJK.setText(""); TTglLahir.setText("");
            return;
        }
        tarikIdentitasPasien(noRawat);
        muat(noRawat);
    }

    private void kosongkanForm() {
        sedangMemuat = true;
        for (int r = 0; r < modelPengkajian.getRowCount(); r++) {
            for (int c = 3; c < 3 + N_SLOT; c++) { modelPengkajian.setValueAt(false, r, c); }
        }
        sedangMemuat = false;
        for (int s = 0; s < N_SLOT; s++) {
            dtpTgl[s].setDate(new Date());
            perbaruiSkorSlot(s);
        }
    }

    private void tarikIdentitasPasien(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select p.no_rkm_medis,p.nm_pasien,p.jk,ifnull(date_format(p.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir_teks "
                + "from reg_periksa rp inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis "
                + "where rp.no_rawat=?")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    String jk = nvl(rs.getString("jk"));
                    TJK.setText("L".equalsIgnoreCase(jk) ? "Laki-Laki" : "Perempuan");
                    TTglLahir.setText(rs.getString("tgl_lahir_teks"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik identitas EWS anak : " + e);
        }
    }

    private void muat(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement("select * from ews_anak where no_rawat=?")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    for (int s = 0; s < N_SLOT; s++) { perbaruiSkorSlot(s); }
                    return;
                }
                sedangMemuat = true;
                for (int s = 1; s <= N_SLOT; s++) {
                    int slot = s - 1;
                    String tgl = rs.getString("a" + s + "_tanggal");
                    if (tgl != null) { setTglJam(dtpTgl[slot], tgl); } else { dtpTgl[slot].setDate(new Date()); }
                    for (int k = 0; k < KATEGORI_KEY.length; k++) {
                        String opsiTerpilih = rs.getString("a" + s + "_" + KATEGORI_KEY[k] + "_opsi");
                        if (opsiTerpilih == null) { continue; }
                        for (int o = 0; o < OPSI_KATEGORI[k].length; o++) {
                            if (OPSI_KATEGORI[k][o].teks.equals(opsiTerpilih)) {
                                modelPengkajian.setValueAt(true, indeksBaris(k, o), 3 + slot);
                            }
                        }
                    }
                }
                sedangMemuat = false;
                for (int s = 0; s < N_SLOT; s++) { perbaruiSkorSlot(s); }
            }
        } catch (Exception e) {
            sedangMemuat = false;
            System.out.println("Notif muat EWS anak : " + e);
        }
    }

    private void simpan() {
        if (noRawat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        StringBuilder kolom = new StringBuilder("no_rawat");
        StringBuilder placeholder = new StringBuilder("?");
        StringBuilder updateSet = new StringBuilder();
        java.util.List<Object> nilai = new java.util.ArrayList<>();
        nilai.add(noRawat);

        for (int s = 1; s <= N_SLOT; s++) {
            int slot = s - 1;
            int total = 0;
            for (int k = 0; k < KATEGORI_KEY.length; k++) {
                for (int o = 0; o < OPSI_KATEGORI[k].length; o++) {
                    if (Boolean.TRUE.equals(modelPengkajian.getValueAt(indeksBaris(k, o), 3 + slot))) {
                        total += OPSI_KATEGORI[k][o].skor;
                    }
                }
            }
            boolean adaIsi = false;
            for (int k = 0; k < KATEGORI_KEY.length && !adaIsi; k++) {
                for (int o = 0; o < OPSI_KATEGORI[k].length; o++) {
                    if (Boolean.TRUE.equals(modelPengkajian.getValueAt(indeksBaris(k, o), 3 + slot))) { adaIsi = true; break; }
                }
            }

            kolom.append(",a").append(s).append("_tanggal");
            placeholder.append(",?");
            updateSet.append("a").append(s).append("_tanggal=values(a").append(s).append("_tanggal),");
            nilai.add(adaIsi ? ambilTglJam(dtpTgl[slot]) : null);

            for (int k = 0; k < KATEGORI_KEY.length; k++) {
                String opsiTeks = null; Integer skor = null;
                for (int o = 0; o < OPSI_KATEGORI[k].length; o++) {
                    if (Boolean.TRUE.equals(modelPengkajian.getValueAt(indeksBaris(k, o), 3 + slot))) {
                        opsiTeks = OPSI_KATEGORI[k][o].teks;
                        skor = OPSI_KATEGORI[k][o].skor;
                    }
                }
                kolom.append(",a").append(s).append("_").append(KATEGORI_KEY[k]).append("_opsi,a").append(s).append("_").append(KATEGORI_KEY[k]).append("_skor");
                placeholder.append(",?,?");
                updateSet.append("a").append(s).append("_").append(KATEGORI_KEY[k]).append("_opsi=values(a").append(s).append("_").append(KATEGORI_KEY[k]).append("_opsi),");
                updateSet.append("a").append(s).append("_").append(KATEGORI_KEY[k]).append("_skor=values(a").append(s).append("_").append(KATEGORI_KEY[k]).append("_skor),");
                nilai.add(opsiTeks);
                nilai.add(skor);
            }

            String level = !adaIsi ? null : (total <= 2 ? "Skor 0-2" : (total <= 4 ? "Skor 3-4" : "Skor >= 5"));
            kolom.append(",a").append(s).append("_total_skor,a").append(s).append("_level");
            placeholder.append(",?,?");
            updateSet.append("a").append(s).append("_total_skor=values(a").append(s).append("_total_skor),");
            updateSet.append("a").append(s).append("_level=values(a").append(s).append("_level),");
            nilai.add(!adaIsi ? null : total);
            nilai.add(level);
        }

        kolom.append(",created_by,created_at,updated_by,updated_at");
        placeholder.append(",?,now(),?,now()");
        updateSet.append("updated_by=values(updated_by),updated_at=now()");
        nilai.add(akses.getkode());
        nilai.add(akses.getkode());

        String sql = "insert into ews_anak (" + kolom + ") values (" + placeholder + ") "
                + "on duplicate key update " + updateSet;
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            for (int i = 0; i < nilai.size(); i++) {
                Object v = nilai.get(i);
                if (v instanceof Integer) { ps.setInt(i + 1, (Integer) v); }
                else if (v == null) { ps.setNull(i + 1, java.sql.Types.VARCHAR); }
                else { ps.setString(i + 1, v.toString()); }
            }
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "EWS Anak tersimpan.");
            muat(noRawat);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan.\n" + e.getMessage());
        }
    }

    private void ensureTable() {
        StringBuilder sql = new StringBuilder("create table if not exists ews_anak (no_rawat varchar(17) not null primary key,");
        for (int s = 1; s <= N_SLOT; s++) {
            sql.append("a").append(s).append("_tanggal datetime null,");
            for (String kk : KATEGORI_KEY) {
                sql.append("a").append(s).append("_").append(kk).append("_opsi varchar(300) null,");
                sql.append("a").append(s).append("_").append(kk).append("_skor int null,");
            }
            sql.append("a").append(s).append("_total_skor int null,");
            sql.append("a").append(s).append("_level varchar(20) null,");
        }
        sql.append("created_by varchar(50) null,created_at datetime null,updated_by varchar(50) null,updated_at datetime null"
                + ") ROW_FORMAT=DYNAMIC");
        Sequel.queryu2(sql.toString());
    }

    /** Cetak langsung dari no_rawat tanpa membuka dialog (dipakai dari kartu blok di layar Riwayat
     *  Perawatan) -- pola sama persis RMAsesmenUlangNyeri.cetak(String). */
    public static void cetak(String noRawat) {
        if (noRawat == null || noRawat.trim().isEmpty()) { return; }
        RMEWSAnak f = new RMEWSAnak(null, false);
        f.isCek();
        f.setNoRm(noRawat.trim());
        f.cetak();
        f.dispose();
    }

    /** Cetak Jasper (rptEWSAnak.jasper) -- sama persis pola RMEWSBayi.cetak(), lihat komentar di sana. */
    private void cetak() {
        if (noRawat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        if (Sequel.cariInteger("select count(*) from ews_anak where no_rawat=?", noRawat) == 0) {
            JOptionPane.showMessageDialog(this, "Simpan data terlebih dahulu sebelum mencetak.");
            return;
        }
        try {
            String noRawatEsc = sqlEsc(noRawat);
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            param.put("no_rkm_medis", TNoRM.getText());
            param.put("nm_pasien", TPasien.getText());
            param.put("jk", TJK.getText());
            param.put("tgl_lahir", TTglLahir.getText());
            for (int s = 1; s <= N_SLOT; s++) {
                param.put("tgl" + s, Sequel.cariIsi("select ifnull(date_format(a" + s + "_tanggal,'%d-%m-%Y %H:%i'),'-') from ews_anak where no_rawat='" + noRawatEsc + "'"));
                param.put("total" + s, Sequel.cariIsi("select ifnull(a" + s + "_total_skor,'-') from ews_anak where no_rawat='" + noRawatEsc + "'"));
                param.put("level" + s, printSafe(Sequel.cariIsi("select ifnull(a" + s + "_level,'-') from ews_anak where no_rawat='" + noRawatEsc + "'")));
            }
            StringBuilder sql = new StringBuilder();
            int urutan = 0;
            for (int k = 0; k < KATEGORI_KEY.length; k++) {
                for (int o = 0; o < OPSI_KATEGORI[k].length; o++) {
                    int idx = indeksBaris(k, o);
                    if (urutan > 0) { sql.append(" union all "); }
                    String labelKategori = tengahKelompok(idx) ? sqlEsc(KATEGORI_LABEL[k]) : "";
                    String kriteriaTeks = sqlEsc((o + 1) + ". " + printSafe(OPSI_KATEGORI[k][o].teks));
                    boolean awal = awalKelompok(idx);
                    sql.append("select ").append(urutan).append(" as urutan,'").append(labelKategori).append("' as kategori,'")
                       .append(kriteriaTeks).append("' as kriteria,").append(OPSI_KATEGORI[k][o].skor).append(" as skor,'")
                       .append(awal ? "Y" : "").append("' as awal");
                    for (int s = 1; s <= N_SLOT; s++) {
                        sql.append(",case when a").append(s).append("_").append(KATEGORI_KEY[k]).append("_opsi='")
                           .append(sqlEsc(OPSI_KATEGORI[k][o].teks)).append("' then 'Y' else '' end as c").append(s);
                    }
                    sql.append(" from ews_anak where no_rawat='").append(noRawatEsc).append("'");
                    urutan++;
                }
            }
            sql.append(" order by urutan");
            Valid.MyReportqry("rptEWSAnak.jasper", "report", "::[ Early Warning Scoring System (Anak) ]::", sql.toString(), param);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak.\n" + e.getMessage());
        }
    }

    private static String printSafe(String s) {
        return s == null ? "" : s.replace("≥", ">=").replace("≤", "<=");
    }

    private static String sqlEsc(String s) {
        return s == null ? "" : s.replace("'", "''");
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

    private JPanel fieldRingkasan(String label, Component komponen) {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(0, 10, 0, 10));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Times New Roman", Font.PLAIN, 10));
        l.setForeground(new Color(86, 101, 112));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        komponen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        komponen.setPreferredSize(new Dimension(150, 25));
        komponen.setBackground(new Color(248, 250, 251));
        p.add(l);
        p.add(Box.createVerticalStrut(3));
        p.add(komponen);
        return p;
    }

    private static String nvl(String v) {
        return v == null ? "" : v;
    }

    /** Kolom Kategori: label kategori cuma tampil di baris tengah kelompoknya (meniru sel gabungan di kertas). */
    private static final class KategoriLabelRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            l.setFont(l.getFont().deriveFont(Font.BOLD));
            l.setVerticalAlignment(JLabel.TOP);
            l.setOpaque(true);
            l.setBackground(OPSI_KATEGORI[GRID_ROWS[row].katIdx][GRID_ROWS[row].opsiIdx].warna);
            return l;
        }
    }

    /** Kolom Kriteria: teks opsi -- dibungkus HTML supaya word-wrap rapi mengikuti lebar kolom
     *  sebenarnya (dipakai jg oleh aturTinggiBarisOtomatis()). */
    private static final class KriteriaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            String teks = value == null ? "" : value.toString();
            JLabel l = (JLabel) super.getTableCellRendererComponent(table,
                    "<html><div style='width:" + (table.getColumnModel().getColumn(column).getWidth() - 10) + "px'>" + teks + "</div></html>",
                    isSelected, hasFocus, row, column);
            l.setVerticalAlignment(JLabel.TOP);
            l.setOpaque(true);
            l.setBackground(OPSI_KATEGORI[GRID_ROWS[row].katIdx][GRID_ROWS[row].opsiIdx].warna);
            return l;
        }
    }

    private static final class KategoriRenderer implements javax.swing.table.TableCellRenderer {
        private final boolean checkbox;
        private final DefaultTableCellRenderer teksRenderer = new DefaultTableCellRenderer();
        private final JCheckBox checkBoxRenderer = new JCheckBox();
        KategoriRenderer(boolean checkbox) { this.checkbox = checkbox; checkBoxRenderer.setHorizontalAlignment(JLabel.CENTER); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Color warna = OPSI_KATEGORI[GRID_ROWS[row].katIdx][GRID_ROWS[row].opsiIdx].warna;
            if (checkbox) {
                checkBoxRenderer.setSelected(Boolean.TRUE.equals(value));
                checkBoxRenderer.setBackground(warna);
                checkBoxRenderer.setOpaque(true);
                return checkBoxRenderer;
            } else {
                JLabel l = (JLabel) teksRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(JLabel.CENTER);
                l.setFont(l.getFont().deriveFont(Font.BOLD));
                l.setOpaque(true);
                l.setBackground(warna);
                return l;
            }
        }
    }
}
