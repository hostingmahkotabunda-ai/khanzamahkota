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
 * Early Warning Scoring System (Dewasa), RM 9.1 REV001-03/23. Beda struktur dari EWS Bayi/Anak --
 * ini 7 parameter INDEPENDEN (bukan 3 kategori deskriptif), tiap parameter berbasis rentang angka
 * vital sign, semuanya dijumlah (bukan pilih-1-dari-semua spt Bayi/Anak, di sini pilih 1 PER
 * PARAMETER lalu ke-7 nilai dijumlah). Ditambah "Parameter Tambahan" (Gula Darah + Inisial, TIDAK
 * ikut skor, cuma catatan) dan aturan eskalasi khusus: SATU parameter manapun bernilai 3 otomatis
 * naik ke level "5-6 / 1 parameter skor 3" walau totalnya di bawah 5 (lihat hitungLevel()).
 *
 * CATATAN PENTING soal pembacaan kertas asli: beberapa sel di kertas (Tekanan Darah Sistolik,
 * Nadi, Temperatur) memakai sel gabungan (beberapa rentang berbagi 1 kotak skor) -- dibaca &
 * dikonfirmasi ke user sbg 5/7/5 pilihan (bukan baris per baris), lihat OPSI_KATEGORI di bawah.
 * Jumlah slot waktu penilaian (N_SLOT=12) dipilih sendiri krn user belum menjawab pasti berapa
 * kolom di kertas aslinya (kertasnya jauh lebih lebar dari Bayi/Anak) -- gampang diubah kalau perlu.
 */
public final class RMEWSDewasa extends JDialog {

    // N_SLOT=6 (sama spt Bayi/Anak) -- awalnya dicoba 12 tapi MySQL menolak CREATE TABLE ("Row
    // size too large"): 7 parameter x lebar kolom opsi bikin 1 baris kepanjangan kalau slotnya
    // sebanyak itu. 6 slot sudah teruji aman jauh di bawah batas (lihat catatan di ensureTable()).
    private static final int N_SLOT = 6;
    private static final int LEBAR_KOLOM_KATEGORI = 185;
    private static final int LEBAR_KOLOM_KRITERIA = 260;
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

    // Warna berbasis SKOR (bukan per-kategori spt Bayi/Anak) -- konsisten di semua parameter,
    // sesuai pola pewarnaan kertas Dewasa (putih=0, kuning=1, oranye=2, merah=3).
    private static final Color C_SKOR0 = Color.WHITE;
    private static final Color C_SKOR1 = new Color(255, 245, 157);
    private static final Color C_SKOR2 = new Color(255, 204, 128);
    private static final Color C_SKOR3 = new Color(239, 154, 154);
    private static Color warnaSkor(int skor) {
        switch (skor) {
            case 1: return C_SKOR1;
            case 2: return C_SKOR2;
            case 3: return C_SKOR3;
            default: return C_SKOR0;
        }
    }

    private static final String[] KATEGORI_LABEL = {
        "Laju Pernapasan", "Saturasi Oksigen (%)", "Alat Oksigen", "Tekanan Darah Sistolik (mmHg)",
        "Nadi (kali/menit)", "Level Kesadaran", "Temperatur (celcius)"
    };
    private static final String[] KATEGORI_KEY = {
        "laju_pernapasan", "saturasi_oksigen", "alat_oksigen", "tekanan_darah_sistolik",
        "nadi", "level_kesadaran", "temperatur"
    };

    // Wording & skor VERBATIM kertas asli (RM 9.1 Dewasa). Sel yg di kertas aslinya digabung
    // (Tekanan Darah Sistolik, Nadi, Temperatur) DIBACA & DIKONFIRMASI ke user sbg 1 opsi per
    // kelompok rentang yg skornya sama (bukan 1 opsi per baris kertas).
    private static final Opsi[][] OPSI_KATEGORI = {
        { // LAJU PERNAPASAN
            new Opsi("≥25", 3, warnaSkor(3)),
            new Opsi("21-24", 2, warnaSkor(2)),
            new Opsi("12-20", 0, warnaSkor(0)),
            new Opsi("9-11", 1, warnaSkor(1)),
            new Opsi("≤8", 3, warnaSkor(3)),
        },
        { // SATURASI OKSIGEN (%)
            new Opsi("≥96", 0, warnaSkor(0)),
            new Opsi("94-95", 1, warnaSkor(1)),
            new Opsi("92-93", 2, warnaSkor(2)),
            new Opsi("≤91", 3, warnaSkor(3)),
        },
        { // ALAT OKSIGEN
            new Opsi("Aliran oksigen (L/menit)", 2, warnaSkor(2)),
            new Opsi("RM/NRM/NK/Tanpa Alat", 0, warnaSkor(0)),
        },
        { // TEKANAN DARAH SISTOLIK (mmHg) -- rentang 201-219 s/d 111-120 digabung (skor 0),
          // rentang 81-90/71-80/≤70 digabung (skor 3), sesuai sel gabungan kertas asli.
            new Opsi("≥220", 3, warnaSkor(3)),
            new Opsi("111-219", 0, warnaSkor(0)),
            new Opsi("101-110", 1, warnaSkor(1)),
            new Opsi("91-100", 2, warnaSkor(2)),
            new Opsi("≤90", 3, warnaSkor(3)),
        },
        { // NADI (kali per menit) -- rentang 91-100 s/d 51-60 digabung (skor 0), sesuai sel
          // gabungan kertas asli.
            new Opsi("≥131", 3, warnaSkor(3)),
            new Opsi("121-130", 2, warnaSkor(2)),
            new Opsi("111-120", 1, warnaSkor(1)),
            new Opsi("101-110", 1, warnaSkor(1)),
            new Opsi("51-100", 0, warnaSkor(0)),
            new Opsi("41-50", 1, warnaSkor(1)),
            new Opsi("≤40", 3, warnaSkor(3)),
        },
        { // LEVEL KESADARAN
            new Opsi("Sadar", 0, warnaSkor(0)),
            new Opsi("C/V/P/U", 3, warnaSkor(3)),
        },
        { // TEMPERATUR (celcius) -- rentang 37,1-38,0 & 36,1-37,0 digabung (skor 0), sesuai sel
          // gabungan kertas asli.
            new Opsi("≥ 39,1", 2, warnaSkor(2)),
            new Opsi("38,1 - 39,0", 1, warnaSkor(1)),
            new Opsi("36,1 - 38,0", 0, warnaSkor(0)),
            new Opsi("35,1 - 36,0", 1, warnaSkor(1)),
            new Opsi("≤ 35,0", 3, warnaSkor(3)),
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
    private final widget.TextBox TAlamat = ro();

    private final widget.Tanggal[] dtpTgl = new widget.Tanggal[N_SLOT];
    private final widget.TextBox[] tGulaDarah = new widget.TextBox[N_SLOT];
    private final widget.TextBox[] tInisial = new widget.TextBox[N_SLOT];
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

    public RMEWSDewasa(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Early Warning Scoring System (Dewasa) - RM 9.1 ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        ensureTable();

        Object[] kolom = new Object[3 + N_SLOT];
        kolom[0] = "Kategori"; kolom[1] = "Kriteria"; kolom[2] = "Skor";
        for (int s = 0; s < N_SLOT; s++) { kolom[3 + s] = String.valueOf(s + 1); }
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
        setSize(Math.min(1800, LEBAR_TOTAL + 60), 920);
        setMinimumSize(new Dimension(1300, 720));
        setLocationRelativeTo(parent);
    }

    private void isiBarisModelPengkajian() {
        modelPengkajian.setRowCount(0);
        for (int i = 0; i < GRID_ROWS.length; i++) {
            GridRow gr = GRID_ROWS[i];
            Opsi o = OPSI_KATEGORI[gr.katIdx][gr.opsiIdx];
            String labelKategori = tengahKelompok(i) ? KATEGORI_LABEL[gr.katIdx] : "";
            Object[] baris = new Object[3 + N_SLOT];
            baris[0] = labelKategori; baris[1] = o.teks; baris[2] = o.skor;
            for (int s = 0; s < N_SLOT; s++) { baris[3 + s] = false; }
            modelPengkajian.addRow(baris);
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
        JLabel judulUtama = new JLabel("Early Warning Scoring System (Dewasa)");
        judulUtama.setFont(new Font("Times New Roman", Font.BOLD, 20));
        judulUtama.setForeground(teks);
        JLabel subJudul = new JLabel("RM 9.1  •  Rawat Inap/Rawat Jalan  •  Penilaian 1 s/d " + N_SLOT);
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

        JPanel ringkasanPasien = new JPanel(new GridLayout(1, 5, 0, 0));
        ringkasanPasien.setBackground(Color.WHITE);
        ringkasanPasien.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis), new EmptyBorder(10, 12, 10, 12)));
        ringkasanPasien.add(fieldRingkasan("No. RM", TNoRM));
        ringkasanPasien.add(fieldRingkasan("Nama Pasien", TPasien));
        ringkasanPasien.add(fieldRingkasan("Jenis Kelamin", TJK));
        ringkasanPasien.add(fieldRingkasan("Tanggal Lahir", TTglLahir));
        ringkasanPasien.add(fieldRingkasan("Alamat", TAlamat));
        atas.add(ringkasanPasien, BorderLayout.SOUTH);
        getContentPane().add(atas, BorderLayout.NORTH);

        JPanel tengah = new JPanel();
        tengah.setBackground(latar);
        tengah.setLayout(new BoxLayout(tengah, BoxLayout.Y_AXIS));
        tengah.setBorder(new EmptyBorder(10, 18, 10, 18));

        // ----- Baris tanggal/jam Penilaian 1..N -----
        tengah.add(barisSlotWidget("Tanggal/Jam", dtpTgl, true));
        tengah.add(Box.createVerticalStrut(4));

        // ----- Tabel Pengkajian -----
        tblPengkajian.setModel(modelPengkajian);
        tblPengkajian.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        tblPengkajian.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 12));
        tblPengkajian.setRowHeight(30);
        tblPengkajian.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        aturLebarKolomPengkajian();
        tblPengkajian.getColumnModel().getColumn(0).setCellRenderer(new KategoriLabelRenderer());
        tblPengkajian.getColumnModel().getColumn(1).setCellRenderer(new KriteriaRenderer());
        tblPengkajian.getColumnModel().getColumn(2).setCellRenderer(new SkorRenderer());
        for (int c = 3; c < 3 + N_SLOT; c++) {
            tblPengkajian.getColumnModel().getColumn(c).setCellRenderer(new CheckboxRenderer());
        }
        aturTinggiBarisOtomatis();
        int tinggiTabel = 0;
        for (int r = 0; r < modelPengkajian.getRowCount(); r++) { tinggiTabel += tblPengkajian.getRowHeight(r); }
        tblPengkajian.setPreferredScrollableViewportSize(new Dimension(LEBAR_TOTAL, tinggiTabel));
        JScrollPane scrollPengkajian = new JScrollPane(tblPengkajian);
        scrollPengkajian.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPengkajian.setPreferredSize(new Dimension(LEBAR_TOTAL, Math.min(tinggiTabel + 20, 560)));
        scrollPengkajian.setMaximumSize(new Dimension(Integer.MAX_VALUE, Math.min(tinggiTabel + 20, 560)));
        tengah.add(scrollPengkajian);
        tengah.add(Box.createVerticalStrut(6));

        tengah.add(barisRingkasSlot("TOTAL SKOR", lblTotal));
        tengah.add(barisRingkasSlot("Level & Respon", lblLevel));
        tengah.add(Box.createVerticalStrut(10));

        // ----- Parameter Tambahan (Gula Darah + Inisial, TIDAK ikut skor) -----
        JPanel panelTambahan = new JPanel();
        panelTambahan.setLayout(new BoxLayout(panelTambahan, BoxLayout.Y_AXIS));
        panelTambahan.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelTambahan.setBorder(BorderFactory.createTitledBorder("Parameter Tambahan"));
        panelTambahan.add(barisSlotWidget("Gula Darah", tGulaDarah, false));
        panelTambahan.add(barisSlotWidget("Inisial", tInisial, false));
        tengah.add(panelTambahan);
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

    /** Baris widget per-slot generik -- dipakai utk Tanggal/Jam (widget.Tanggal) maupun Gula
     *  Darah/Inisial (widget.TextBox). */
    @SuppressWarnings("unchecked")
    private JPanel barisSlotWidget(String judul, Object[] arrTarget, boolean tanggal) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lJudul = new JLabel(judul);
        lJudul.setFont(new Font("Times New Roman", Font.BOLD, 11));
        lJudul.setPreferredSize(new Dimension(LEBAR_KOLOM_SEBELUM_SLOT, 26));
        row.add(lJudul, BorderLayout.WEST);
        JPanel grid = new JPanel(new GridLayout(1, N_SLOT, 2, 0));
        grid.setOpaque(false);
        grid.setPreferredSize(new Dimension(LEBAR_KOLOM_SLOT * N_SLOT, 26));
        for (int s = 0; s < N_SLOT; s++) {
            if (tanggal) {
                final int slot = s;
                widget.Tanggal d = dt();
                d.addPropertyChangeListener("date", evt -> perbaruiSkorSlot(slot));
                d.setPreferredSize(new Dimension(LEBAR_KOLOM_SLOT - 2, 24));
                ((widget.Tanggal[]) arrTarget)[s] = d;
                grid.add(d);
            } else {
                widget.TextBox t = new widget.TextBox();
                t.setPreferredSize(new Dimension(LEBAR_KOLOM_SLOT - 2, 24));
                ((widget.TextBox[]) arrTarget)[s] = t;
                grid.add(t);
            }
        }
        JPanel pembungkus = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pembungkus.setOpaque(false);
        pembungkus.add(grid);
        row.add(pembungkus, BorderLayout.CENTER);
        return row;
    }

    private JPanel panelLevelSkorRespon(Color teks, Color garis) {
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 1));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(BorderFactory.createTitledBorder("Level Skor & Respon"));
        p.add(barisLevel("SKOR 0", Color.WHITE,
                "Monitor rutin per 8 jam"));
        p.add(barisLevel("SKOR 1-4", C_SKOR1,
                "Assesment segera oleh perawat senior, eskalasi perawatan dan monitoring per 4-6 jam, jika diperlukan konsultasi ke dokter jaga"));
        p.add(barisLevel("SKOR 5-6 atau satu parameter SKOR 3", C_SKOR2,
                "Assesment segera oleh dokter jaga (respon segera, maks 5 menit), konsultasi DPJP dan spesialis terkait, eskalasi perawatan dan monitoring tiap jam, pertimbangkan perawatan HCU"));
        p.add(barisLevel("SKOR ≥ 7", C_SKOR3,
                "Resusitasi dan monitoring secara kontinyu oleh dokter jaga dan perawat senior, panggil tim medis reaksi cepat, konsultasikan DPJP"));
        p.add(barisLevel("HENTI NAPAS/JANTUNG", new Color(100, 149, 237),
                "Lakukan RJP, aktivasi henti jantung (118), respon tim henti jantung segera, maksimal 5 menit, resusitasi lanjutan oleh tim medis reaksi cepat (respon segera, maksimal 10 menit)"));
        return bungkusKartu(p);
    }

    private JPanel barisLevel(String judul, Color warna, String teksRespon) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(4, 4, 4, 4));
        JLabel lJudul = new JLabel("<html><div style='width:150px;text-align:center'>" + judul + "</div></html>", JLabel.CENTER);
        lJudul.setOpaque(true);
        lJudul.setBackground(warna);
        lJudul.setForeground(warna.equals(new Color(100, 149, 237)) ? Color.WHITE : new Color(40, 40, 40));
        lJudul.setFont(new Font("Times New Roman", Font.BOLD, 11));
        lJudul.setPreferredSize(new Dimension(170, 44));
        lJudul.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        row.add(lJudul, BorderLayout.WEST);
        JLabel lTeks = new JLabel("<html><div style='width:" + (LEBAR_TOTAL - 200) + "px'>" + teksRespon + "</div></html>");
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

    private JPanel barisRingkasSlot(String judul, JLabel[] target) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel lJudul = new JLabel(judul);
        lJudul.setFont(new Font("Times New Roman", Font.BOLD, 11));
        lJudul.setPreferredSize(new Dimension(LEBAR_KOLOM_SEBELUM_SLOT, 26));
        row.add(lJudul, BorderLayout.WEST);
        JPanel grid = new JPanel(new GridLayout(1, N_SLOT, 2, 0));
        grid.setOpaque(false);
        for (int s = 0; s < N_SLOT; s++) {
            JLabel l = new JLabel("-", JLabel.CENTER);
            l.setOpaque(true);
            l.setBackground(new Color(238, 240, 242));
            l.setFont(new Font("Times New Roman", Font.BOLD, 10));
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

    private void aturTinggiBarisOtomatis() {
        java.awt.FontMetrics fm = tblPengkajian.getFontMetrics(tblPengkajian.getFont());
        int lebarEfektif = LEBAR_KOLOM_KRITERIA - 16;
        for (int r = 0; r < modelPengkajian.getRowCount(); r++) {
            String teksKriteria = String.valueOf(modelPengkajian.getValueAt(r, 1));
            int jmlBaris = hitungJumlahBarisWrap(teksKriteria, fm, lebarEfektif);
            int tinggi = Math.max(26, jmlBaris * (fm.getHeight() + 3) + 8);
            tblPengkajian.setRowHeight(r, tinggi);
        }
    }

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

    /** Sekali salah satu opsi dicentang di suatu kategori+kolom, opsi lain di kategori & kolom yang sama otomatis lepas (radio). */
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

    /** Level & warna sesuai aturan kertas asli: SKOR>=7 paling parah, lalu SKOR 5-6 ATAU ada
     *  satu parameter manapun bernilai 3 (biarpun totalnya di bawah 5), baru SKOR 1-4, baru SKOR 0. */
    private static String[] hitungLevel(int total, boolean satuParameterSkor3) {
        if (total >= 7) { return new String[]{"Skor ≥ 7", "3"}; }
        if (total >= 5 || satuParameterSkor3) { return new String[]{"Skor 5-6 / 1 param=3", "2"}; }
        if (total >= 1) { return new String[]{"Skor 1-4", "1"}; }
        return new String[]{"Skor 0", "0"};
    }

    private void perbaruiSkorSlot(int slot) {
        int col = 3 + slot;
        int total = 0;
        int jmlKategoriTerisi = 0;
        boolean satuParameterSkor3 = false;
        for (int k = 0; k < KATEGORI_KEY.length; k++) {
            for (int o = 0; o < OPSI_KATEGORI[k].length; o++) {
                int r = indeksBaris(k, o);
                if (Boolean.TRUE.equals(modelPengkajian.getValueAt(r, col))) {
                    int skor = OPSI_KATEGORI[k][o].skor;
                    total += skor;
                    jmlKategoriTerisi++;
                    if (skor == 3) { satuParameterSkor3 = true; }
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
        String[] hasil = hitungLevel(total, satuParameterSkor3);
        lblLevel[slot].setText(hasil[0]);
        lblLevel[slot].setBackground(warnaSkor(Integer.parseInt(hasil[1])));
        lblLevel[slot].setForeground(new Color(40, 40, 40));
    }

    public void isCek() {
        boolean bisa = akses.getpenilaian_awal_keperawatan_ranap();
        BtnSimpan.setEnabled(bisa);
    }

    /** Dipanggil dari tab EWS (dropdown "EWS Dewasa") di DlgRawatInap/DlgRawatJalan. */
    public void setNoRm(String norawat) {
        noRawat = norawat == null ? "" : norawat.trim();
        kosongkanForm();
        if (noRawat.isEmpty()) {
            TNoRM.setText(""); TPasien.setText(""); TJK.setText(""); TTglLahir.setText(""); TAlamat.setText("");
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
            tGulaDarah[s].setText("");
            tInisial[s].setText("");
            perbaruiSkorSlot(s);
        }
    }

    private void tarikIdentitasPasien(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select p.no_rkm_medis,p.nm_pasien,p.jk,ifnull(date_format(p.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir_teks,"
                + "ifnull(p.alamat,'') as alamat "
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
                    TAlamat.setText(rs.getString("alamat"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik identitas EWS dewasa : " + e);
        }
    }

    private void muat(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement("select * from ews_dewasa where no_rawat=?")) {
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
                    tGulaDarah[slot].setText(nvl(rs.getString("a" + s + "_gula_darah")));
                    tInisial[slot].setText(nvl(rs.getString("a" + s + "_inisial")));
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
            System.out.println("Notif muat EWS dewasa : " + e);
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
            boolean satuParameterSkor3 = false;
            for (int k = 0; k < KATEGORI_KEY.length; k++) {
                for (int o = 0; o < OPSI_KATEGORI[k].length; o++) {
                    if (Boolean.TRUE.equals(modelPengkajian.getValueAt(indeksBaris(k, o), 3 + slot))) {
                        total += OPSI_KATEGORI[k][o].skor;
                        if (OPSI_KATEGORI[k][o].skor == 3) { satuParameterSkor3 = true; }
                    }
                }
            }
            boolean adaIsi = false;
            for (int k = 0; k < KATEGORI_KEY.length && !adaIsi; k++) {
                for (int o = 0; o < OPSI_KATEGORI[k].length; o++) {
                    if (Boolean.TRUE.equals(modelPengkajian.getValueAt(indeksBaris(k, o), 3 + slot))) { adaIsi = true; break; }
                }
            }
            boolean adaTambahan = !tGulaDarah[slot].getText().trim().isEmpty() || !tInisial[slot].getText().trim().isEmpty();

            kolom.append(",a").append(s).append("_tanggal");
            placeholder.append(",?");
            updateSet.append("a").append(s).append("_tanggal=values(a").append(s).append("_tanggal),");
            nilai.add((adaIsi || adaTambahan) ? ambilTglJam(dtpTgl[slot]) : null);

            kolom.append(",a").append(s).append("_gula_darah,a").append(s).append("_inisial");
            placeholder.append(",?,?");
            updateSet.append("a").append(s).append("_gula_darah=values(a").append(s).append("_gula_darah),");
            updateSet.append("a").append(s).append("_inisial=values(a").append(s).append("_inisial),");
            nilai.add(tGulaDarah[slot].getText().trim().isEmpty() ? null : tGulaDarah[slot].getText().trim());
            nilai.add(tInisial[slot].getText().trim().isEmpty() ? null : tInisial[slot].getText().trim());

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

            String level = !adaIsi ? null : hitungLevel(total, satuParameterSkor3)[0];
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

        String sql = "insert into ews_dewasa (" + kolom + ") values (" + placeholder + ") "
                + "on duplicate key update " + updateSet;
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            for (int i = 0; i < nilai.size(); i++) {
                Object v = nilai.get(i);
                if (v instanceof Integer) { ps.setInt(i + 1, (Integer) v); }
                else if (v == null) { ps.setNull(i + 1, java.sql.Types.VARCHAR); }
                else { ps.setString(i + 1, v.toString()); }
            }
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "EWS Dewasa tersimpan.");
            muat(noRawat);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan.\n" + e.getMessage());
        }
    }

    /** Kolom teks sengaja dibuat sekecil mungkin (opsi/level cuma berisi teks pendek spt "111-219",
     *  "C/V/P/U", "Skor 5-6 / 1 param=3") -- versi awal (varchar(60), N_SLOT=12) kena error MySQL
     *  "Row size too large" krn 7 parameter x banyak slot bikin 1 baris kepanjangan. */
    private void ensureTable() {
        StringBuilder sql = new StringBuilder("create table if not exists ews_dewasa (no_rawat varchar(17) not null primary key,");
        for (int s = 1; s <= N_SLOT; s++) {
            sql.append("a").append(s).append("_tanggal datetime null,");
            sql.append("a").append(s).append("_gula_darah varchar(15) null,");
            sql.append("a").append(s).append("_inisial varchar(15) null,");
            for (String kk : KATEGORI_KEY) {
                sql.append("a").append(s).append("_").append(kk).append("_opsi varchar(30) null,");
                sql.append("a").append(s).append("_").append(kk).append("_skor int null,");
            }
            sql.append("a").append(s).append("_total_skor int null,");
            sql.append("a").append(s).append("_level varchar(30) null,");
        }
        sql.append("created_by varchar(50) null,created_at datetime null,updated_by varchar(50) null,updated_at datetime null"
                + ") ROW_FORMAT=DYNAMIC");
        Sequel.queryu2(sql.toString());
    }

    /** Cetak langsung dari no_rawat tanpa membuka dialog (dipakai dari kartu blok di layar Riwayat
     *  Perawatan) -- pola sama persis RMAsesmenUlangNyeri.cetak(String). */
    public static void cetak(String noRawat) {
        if (noRawat == null || noRawat.trim().isEmpty()) { return; }
        RMEWSDewasa f = new RMEWSDewasa(null, false);
        f.isCek();
        f.setNoRm(noRawat.trim());
        f.cetak();
        f.dispose();
    }

    /** Cetak Jasper (rptEWSDewasa.jasper) -- pola sama dgn RMEWSBayi.cetak() (grid 1 baris = 1 kriteria,
     *  30 baris total krn 7 parameter), ditambah 2 baris "Parameter Tambahan" (Gula Darah/Inisial,
     *  non-skor) via parameter gulaN/inisialN. */
    private void cetak() {
        if (noRawat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        if (Sequel.cariInteger("select count(*) from ews_dewasa where no_rawat=?", noRawat) == 0) {
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
                param.put("tgl" + s, Sequel.cariIsi("select ifnull(date_format(a" + s + "_tanggal,'%d-%m-%Y %H:%i'),'-') from ews_dewasa where no_rawat='" + noRawatEsc + "'"));
                param.put("total" + s, Sequel.cariIsi("select ifnull(a" + s + "_total_skor,'-') from ews_dewasa where no_rawat='" + noRawatEsc + "'"));
                param.put("level" + s, printSafe(Sequel.cariIsi("select ifnull(a" + s + "_level,'-') from ews_dewasa where no_rawat='" + noRawatEsc + "'")));
                param.put("gula" + s, Sequel.cariIsi("select ifnull(a" + s + "_gula_darah,'') from ews_dewasa where no_rawat='" + noRawatEsc + "'"));
                param.put("inisial" + s, Sequel.cariIsi("select ifnull(a" + s + "_inisial,'') from ews_dewasa where no_rawat='" + noRawatEsc + "'"));
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
                    sql.append(" from ews_dewasa where no_rawat='").append(noRawatEsc).append("'");
                    urutan++;
                }
            }
            sql.append(" order by urutan");
            Valid.MyReportqry("rptEWSDewasa.jasper", "report", "::[ Early Warning Scoring System (Dewasa) ]::", sql.toString(), param);
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
        d.setDisplayFormat("dd-MM HH:mm");
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

    private static final class KriteriaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            String teksIsi = value == null ? "" : value.toString();
            JLabel l = (JLabel) super.getTableCellRendererComponent(table,
                    "<html><div style='width:" + (table.getColumnModel().getColumn(column).getWidth() - 10) + "px'>" + teksIsi + "</div></html>",
                    isSelected, hasFocus, row, column);
            l.setVerticalAlignment(JLabel.TOP);
            l.setOpaque(true);
            l.setBackground(OPSI_KATEGORI[GRID_ROWS[row].katIdx][GRID_ROWS[row].opsiIdx].warna);
            return l;
        }
    }

    private static final class SkorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            l.setHorizontalAlignment(JLabel.CENTER);
            l.setFont(l.getFont().deriveFont(Font.BOLD));
            l.setOpaque(true);
            l.setBackground(OPSI_KATEGORI[GRID_ROWS[row].katIdx][GRID_ROWS[row].opsiIdx].warna);
            return l;
        }
    }

    private static final class CheckboxRenderer implements javax.swing.table.TableCellRenderer {
        private final JCheckBox checkBoxRenderer = new JCheckBox();
        CheckboxRenderer() { checkBoxRenderer.setHorizontalAlignment(JLabel.CENTER); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            checkBoxRenderer.setSelected(Boolean.TRUE.equals(value));
            checkBoxRenderer.setBackground(OPSI_KATEGORI[GRID_ROWS[row].katIdx][GRID_ROWS[row].opsiIdx].warna);
            checkBoxRenderer.setOpaque(true);
            return checkBoxRenderer;
        }
    }
}
