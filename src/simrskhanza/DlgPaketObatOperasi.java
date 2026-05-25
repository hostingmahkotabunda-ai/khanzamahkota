package simrskhanza;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;
import keuangan.DlgJnsPerawatanOperasi;

public class DlgPaketObatOperasi extends JDialog {
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();

    private final widget.TextBox TKodePaket = new widget.TextBox();
    private final widget.TextBox TNamaPaket = new widget.TextBox();
    private final widget.TextBox TKdDokter = new widget.TextBox();
    private final widget.TextBox TNmDokter = new widget.TextBox();
    private final widget.TextBox TKodeOperasi = new widget.TextBox();
    private final widget.TextBox TNamaOperasi = new widget.TextBox();
    private final widget.TextBox TKeterangan = new widget.TextBox();
    private final widget.TextBox TCariPaket = new widget.TextBox();
    private final widget.TextBox TCariObat = new widget.TextBox();
    private final widget.ComboBox CmbStatus = new widget.ComboBox();

    private final widget.Button BtnBaru = new widget.Button();
    private final widget.Button BtnSimpan = new widget.Button();
    private final widget.Button BtnHapus = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();
    private final widget.Button BtnPilih = new widget.Button();
    private final widget.Button BtnCariDokter = new widget.Button();
    private final widget.Button BtnCariOperasi = new widget.Button();
    private final widget.Button BtnTambahItem = new widget.Button();
    private final widget.Button BtnHapusItem = new widget.Button();
    private final widget.Button BtnCariPaket = new widget.Button();
    private final widget.Button BtnCariObat = new widget.Button();

    private final widget.Table tbPaket = new widget.Table();
    private final widget.Table tbDetail = new widget.Table();
    private final widget.Table tbObat = new widget.Table();

    private final DefaultTableModel tabModePaket;
    private final DefaultTableModel tabModeDetail;
    private final DefaultTableModel tabModeObat;

    private final DlgCariDokter dokter = new DlgCariDokter(null, false);
    private final DlgJnsPerawatanOperasi operasi = new DlgJnsPerawatanOperasi(null, false);

    private boolean selectionMode = false;
    private String kdDokterFilter = "";
    private String kodeOperasiFilter = "";
    private String selectedPackageCode = "";

    public DlgPaketObatOperasi(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Paket Obat & BHP Operasi ]::");
        setSize(1180, 720);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        ensureSupportTables();

        Object[] headerPaket = {"Kode Paket", "Nama Paket", "Dokter", "Operasi", "Status", "Keterangan"};
        tabModePaket = new DefaultTableModel(null, headerPaket) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        Object[] headerDetail = {"Jumlah", "Kode", "Nama", "Satuan", "Harga", "Total"};
        tabModeDetail = new DefaultTableModel(null, headerDetail) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex == 0;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 4 || columnIndex == 5) {
                    return Double.class;
                }
                return Object.class;
            }
        };
        Object[] headerObat = {"Kode", "Nama", "Satuan", "Harga"};
        tabModeObat = new DefaultTableModel(null, headerObat) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };

        initComponents();
        initDokterChooser();
        initOperasiChooser();
        emptTeks();
        tampilPaket();
        tampilObat();
    }

    private void initComponents() {
        TKodePaket.setEditable(false);
        TKdDokter.setEditable(false);
        TNmDokter.setEditable(false);
        TKodeOperasi.setEditable(false);
        TNamaOperasi.setEditable(false);
        TKodePaket.setDocument(new batasInput((byte)20).getKata(TKodePaket));
        TNamaPaket.setDocument(new batasInput((byte)100).getKata(TNamaPaket));
        TKeterangan.setDocument(new batasInput((byte)200).getKata(TKeterangan));
        TCariPaket.setDocument(new batasInput((byte)100).getKata(TCariPaket));
        TCariObat.setDocument(new batasInput((byte)100).getKata(TCariObat));
        CmbStatus.addItem("AKTIF");
        CmbStatus.addItem("NONAKTIF");

        BtnBaru.setText("Baru");
        BtnSimpan.setText("Simpan");
        BtnHapus.setText("Hapus");
        BtnKeluar.setText("Keluar");
        BtnPilih.setText("Pilih");
        BtnCariDokter.setText("Cari Dokter");
        BtnCariOperasi.setText("Cari Operasi");
        BtnTambahItem.setText("Tambah Item");
        BtnHapusItem.setText("Hapus Item");
        BtnCariPaket.setText("Tampil");
        BtnCariObat.setText("Cari Obat");
        BtnPilih.setVisible(false);

        BtnBaru.addActionListener(evt -> emptTeks());
        BtnSimpan.addActionListener(evt -> simpanPaket());
        BtnHapus.addActionListener(evt -> hapusPaket());
        BtnKeluar.addActionListener(evt -> dispose());
        BtnPilih.addActionListener(evt -> pilihPaket());
        BtnCariDokter.addActionListener(evt -> bukaCariDokter());
        BtnCariOperasi.addActionListener(evt -> bukaCariOperasi());
        BtnTambahItem.addActionListener(evt -> tambahItemTerpilih());
        BtnHapusItem.addActionListener(evt -> hapusItemDetail());
        BtnCariPaket.addActionListener(evt -> tampilPaket());
        BtnCariObat.addActionListener(evt -> tampilObat());

        TCariPaket.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    tampilPaket();
                }
            }
        });
        TCariObat.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    tampilObat();
                }
            }
        });

        if (koneksiDB.CARICEPAT().equals("aktif")) {
            TCariPaket.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { if (TCariPaket.getText().length() > 2 || TCariPaket.getText().isEmpty()) tampilPaket(); }
                @Override public void removeUpdate(DocumentEvent e) { if (TCariPaket.getText().length() > 2 || TCariPaket.getText().isEmpty()) tampilPaket(); }
                @Override public void changedUpdate(DocumentEvent e) { if (TCariPaket.getText().length() > 2 || TCariPaket.getText().isEmpty()) tampilPaket(); }
            });
            TCariObat.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { if (TCariObat.getText().length() > 2 || TCariObat.getText().isEmpty()) tampilObat(); }
                @Override public void removeUpdate(DocumentEvent e) { if (TCariObat.getText().length() > 2 || TCariObat.getText().isEmpty()) tampilObat(); }
                @Override public void changedUpdate(DocumentEvent e) { if (TCariObat.getText().length() > 2 || TCariObat.getText().isEmpty()) tampilObat(); }
            });
        }

        tbPaket.setModel(tabModePaket);
        tbPaket.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbPaket.setDefaultRenderer(Object.class, new WarnaTable());
        tbPaket.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (tbPaket.getSelectedRow() != -1) {
                    tampilDataPaket();
                    if (selectionMode) {
                        selectedPackageCode = tbPaket.getValueAt(tbPaket.getSelectedRow(), 0).toString();
                    }
                    if (selectionMode && evt.getClickCount() == 2) {
                        pilihPaket();
                    }
                }
            }
        });
        tbPaket.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (selectionMode && tbPaket.getSelectedRow() != -1) {
                    selectedPackageCode = tbPaket.getValueAt(tbPaket.getSelectedRow(), 0).toString();
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        evt.consume();
                        pilihPaket();
                    }
                }
            }
        });

        tbDetail.setModel(tabModeDetail);
        tbDetail.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbDetail.setDefaultRenderer(Object.class, new WarnaTable());
        tbDetail.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                if (tbDetail.getSelectedRow() != -1) {
                    hitungBarisDetail(tbDetail.getSelectedRow());
                }
            }
        });

        tbObat.setModel(tabModeObat);
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        tbObat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    tambahItemTerpilih();
                }
            }
        });

        setLebarKolom(tbPaket, new int[]{110, 220, 180, 220, 70, 250});
        setLebarKolom(tbDetail, new int[]{70, 110, 260, 90, 90, 90});
        setLebarKolom(tbObat, new int[]{110, 260, 90, 90});

        JPanel root = new JPanel(new BorderLayout(5, 5));
        root.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        JPanel form = new JPanel(new GridLayout(4, 1, 4, 4));
        form.setBorder(BorderFactory.createTitledBorder("Header Paket"));
        form.add(barisForm("Kode Paket", TKodePaket, "Nama Paket", TNamaPaket));
        form.add(barisForm("Dokter", gabung(TKdDokter, TNmDokter, BtnCariDokter), "Operasi", gabung(TKodeOperasi, TNamaOperasi, BtnCariOperasi)));
        form.add(barisForm("Status", CmbStatus, "Keterangan", TKeterangan));
        form.add(barisFilterPaket());

        JPanel panelPaket = new JPanel(new BorderLayout(4, 4));
        panelPaket.setBorder(BorderFactory.createTitledBorder("Daftar Paket"));
        widget.ScrollPane scrollPaket = new widget.ScrollPane();
        scrollPaket.setViewportView(tbPaket);
        panelPaket.add(scrollPaket, BorderLayout.CENTER);

        JPanel panelDetail = new JPanel(new BorderLayout(4, 4));
        panelDetail.setBorder(BorderFactory.createTitledBorder("Detail Paket"));
        widget.ScrollPane scrollDetail = new widget.ScrollPane();
        scrollDetail.setViewportView(tbDetail);
        panelDetail.add(scrollDetail, BorderLayout.CENTER);
        JPanel detailButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        detailButtons.add(BtnTambahItem);
        detailButtons.add(BtnHapusItem);
        panelDetail.add(detailButtons, BorderLayout.SOUTH);

        JPanel panelObat = new JPanel(new BorderLayout(4, 4));
        panelObat.setBorder(BorderFactory.createTitledBorder("Referensi Obat & BHP"));
        JPanel filterObat = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        widget.Label labelCariObat = new widget.Label();
        labelCariObat.setText("Key Word");
        labelCariObat.setPreferredSize(new Dimension(70, 23));
        filterObat.add(labelCariObat);
        TCariObat.setPreferredSize(new Dimension(280, 23));
        filterObat.add(TCariObat);
        filterObat.add(BtnCariObat);
        panelObat.add(filterObat, BorderLayout.NORTH);
        widget.ScrollPane scrollObat = new widget.ScrollPane();
        scrollObat.setViewportView(tbObat);
        panelObat.add(scrollObat, BorderLayout.CENTER);

        JSplitPane bawah = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelDetail, panelObat);
        bawah.setResizeWeight(0.52);
        bawah.setDividerLocation(560);
        JSplitPane tengah = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelPaket, bawah);
        tengah.setResizeWeight(0.48);
        tengah.setDividerLocation(250);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        actions.add(BtnBaru);
        actions.add(BtnSimpan);
        actions.add(BtnHapus);
        actions.add(BtnPilih);
        actions.add(BtnKeluar);

        root.add(form, BorderLayout.NORTH);
        root.add(tengah, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel barisForm(String labelKiri, java.awt.Component compKiri, String labelKanan, java.awt.Component compKanan) {
        JPanel p = new JPanel(new GridLayout(1, 4, 4, 4));
        widget.Label lk = new widget.Label();
        lk.setText(labelKiri);
        lk.setHorizontalAlignment(SwingConstants.LEFT);
        widget.Label ln = new widget.Label();
        ln.setText(labelKanan);
        ln.setHorizontalAlignment(SwingConstants.LEFT);
        p.add(lk);
        p.add(compKiri);
        p.add(ln);
        p.add(compKanan);
        return p;
    }

    private JPanel barisFilterPaket() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        widget.Label lCari = new widget.Label();
        lCari.setText("Cari Paket");
        lCari.setPreferredSize(new Dimension(70, 23));
        p.add(lCari);
        TCariPaket.setPreferredSize(new Dimension(280, 23));
        p.add(TCariPaket);
        p.add(BtnCariPaket);
        return p;
    }

    private JPanel gabung(java.awt.Component first, java.awt.Component second, java.awt.Component button) {
        JPanel p = new JPanel(new BorderLayout(4, 0));
        JPanel center = new JPanel(new GridLayout(1, 2, 4, 0));
        center.add(first);
        center.add(second);
        p.add(center, BorderLayout.CENTER);
        p.add(button, BorderLayout.EAST);
        return p;
    }

    private void initDokterChooser() {
        dokter.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent evt) {
                if (dokter.getTable().getSelectedRow() != -1) {
                    TKdDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 0).toString());
                    TNmDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 1).toString());
                }
            }
        });
    }

    private void initOperasiChooser() {
        operasi.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent evt) {
                if (operasi.getTable().getSelectedRow() != -1) {
                    TKodeOperasi.setText(operasi.getTable().getValueAt(operasi.getTable().getSelectedRow(), 1).toString());
                    TNamaOperasi.setText(operasi.getTable().getValueAt(operasi.getTable().getSelectedRow(), 2).toString());
                }
            }
        });
    }

    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
        this.selectedPackageCode = "";
        BtnPilih.setVisible(selectionMode);
        if (selectionMode) {
            setTitle("::[ Pilih Paket Obat & BHP Operasi ]::");
            BtnBaru.setEnabled(false);
            BtnSimpan.setEnabled(false);
            BtnHapus.setEnabled(false);
            BtnCariDokter.setEnabled(false);
            BtnCariOperasi.setEnabled(false);
            BtnTambahItem.setEnabled(false);
            BtnHapusItem.setEnabled(false);
            TNamaPaket.setEditable(false);
            TKeterangan.setEditable(false);
            CmbStatus.setEnabled(false);
        } else {
            setTitle("::[ Paket Obat & BHP Operasi ]::");
            BtnBaru.setEnabled(true);
            BtnSimpan.setEnabled(true);
            BtnHapus.setEnabled(true);
            BtnCariDokter.setEnabled(true);
            BtnCariOperasi.setEnabled(true);
            BtnTambahItem.setEnabled(true);
            BtnHapusItem.setEnabled(true);
            TNamaPaket.setEditable(true);
            TKeterangan.setEditable(true);
            CmbStatus.setEnabled(true);
        }
    }

    public void setFilterContext(String kdDokter, String namaDokter, String kodeOperasi, String namaOperasi) {
        this.kdDokterFilter = kdDokter == null ? "" : kdDokter.trim();
        this.kodeOperasiFilter = kodeOperasi == null ? "" : kodeOperasi.trim();
        this.selectedPackageCode = "";
        TKdDokter.setText(this.kdDokterFilter);
        TNmDokter.setText(namaDokter == null ? "" : namaDokter.trim());
        TKodeOperasi.setText(this.kodeOperasiFilter);
        TNamaOperasi.setText(namaOperasi == null ? "" : namaOperasi.trim());
        tampilPaket();
    }

    public String getSelectedPackageCode() {
        return selectedPackageCode;
    }

    public void setDraftPaketDariTagihan(String kdDokter, String namaDokter, String kodeOperasi, String namaOperasi,
                                         String namaPaketUsulan, String keteranganUsulan, Object[][] items) {
        setSelectionMode(false);
        emptTeks();
        TKdDokter.setText((kdDokter == null || kdDokter.trim().isEmpty()) ? "-" : kdDokter.trim());
        TNmDokter.setText(namaDokter == null ? "" : namaDokter.trim());
        TKodeOperasi.setText((kodeOperasi == null || kodeOperasi.trim().isEmpty()) ? "-" : kodeOperasi.trim());
        TNamaOperasi.setText(namaOperasi == null ? "" : namaOperasi.trim());
        TNamaPaket.setText(namaPaketUsulan == null ? "" : namaPaketUsulan.trim());
        TKeterangan.setText(keteranganUsulan == null ? "" : keteranganUsulan.trim());
        Valid.tabelKosong(tabModeDetail);

        if (items != null) {
            for (Object[] item : items) {
                if (item == null || item.length < 5) {
                    continue;
                }
                tambahAtauGabungDetail(
                        String.valueOf(item[0]),
                        String.valueOf(item[1]),
                        String.valueOf(item[2]),
                        Valid.SetAngka(String.valueOf(item[3])),
                        Valid.SetAngka(String.valueOf(item[4]))
                );
            }
        }
    }

    public void emptTeks() {
        TKodePaket.setText(generateKodePaket());
        TNamaPaket.setText("");
        TKdDokter.setText(selectionMode ? kdDokterFilter : "-");
        TNmDokter.setText("");
        TKodeOperasi.setText(selectionMode ? kodeOperasiFilter : "-");
        TNamaOperasi.setText("");
        TKeterangan.setText("");
        CmbStatus.setSelectedItem("AKTIF");
        Valid.tabelKosong(tabModeDetail);
        selectedPackageCode = "";
    }

    private String generateKodePaket() {
        int next = Sequel.cariInteger("select ifnull(max(convert(right(kd_paket_obat,6),signed)),0)+1 from paket_obat_operasi where kd_paket_obat like 'PKTOB%'");
        return String.format(Locale.ENGLISH, "PKTOB%06d", next);
    }

    private void ensureSupportTables() {
        try {
            Sequel.queryu("create table if not exists paket_obat_operasi (" +
                    "kd_paket_obat varchar(20) not null," +
                    "nama_paket varchar(100) not null," +
                    "kd_dokter varchar(20) not null default '-'," +
                    "kode_paket varchar(15) not null default '-'," +
                    "keterangan varchar(200) not null default ''," +
                    "status enum('AKTIF','NONAKTIF') not null default 'AKTIF'," +
                    "primary key (kd_paket_obat)," +
                    "key idx_paket_obat_operasi_dokter (kd_dokter)," +
                    "key idx_paket_obat_operasi_operasi (kode_paket)" +
                    ")");
            Sequel.queryu("create table if not exists paket_obat_operasi_detail (" +
                    "kd_paket_obat varchar(20) not null," +
                    "kd_obat varchar(15) not null," +
                    "jumlah double not null default 0," +
                    "urut int not null default 0," +
                    "primary key (kd_paket_obat,kd_obat)," +
                    "key idx_paket_obat_operasi_detail_obat (kd_obat)" +
                    ")");
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void tampilPaket() {
        Valid.tabelKosong(tabModePaket);
        String sql = "select p.kd_paket_obat,p.nama_paket,concat(p.kd_dokter,' ',ifnull(d.nm_dokter,'')) as dokter," +
                "concat(p.kode_paket,' ',ifnull(o.nm_perawatan,'')) as operasi,p.status,p.keterangan " +
                "from paket_obat_operasi p " +
                "left join dokter d on p.kd_dokter=d.kd_dokter " +
                "left join paket_operasi o on p.kode_paket=o.kode_paket " +
                "where (p.kd_paket_obat like ? or p.nama_paket like ? or p.keterangan like ? or ifnull(d.nm_dokter,'') like ? or ifnull(o.nm_perawatan,'') like ?) ";
        if (selectionMode) {
            sql = sql + "and p.status='AKTIF' and p.kd_dokter in (?, '-') and p.kode_paket in (?, '-') " +
                    "order by case " +
                    "when p.kd_dokter=? and p.kode_paket=? then 0 " +
                    "when p.kd_dokter=? and p.kode_paket='-' then 1 " +
                    "when p.kd_dokter='-' and p.kode_paket=? then 2 " +
                    "when p.kd_dokter='-' and p.kode_paket='-' then 3 else 9 end,p.nama_paket";
        } else {
            sql = sql + "order by p.nama_paket";
        }

        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            String cari = "%" + TCariPaket.getText().trim() + "%";
            ps.setString(1, cari);
            ps.setString(2, cari);
            ps.setString(3, cari);
            ps.setString(4, cari);
            ps.setString(5, cari);
            if (selectionMode) {
                String kdDokter = kdDokterFilter.isEmpty() ? "-" : kdDokterFilter;
                String kdOperasi = kodeOperasiFilter.isEmpty() ? "-" : kodeOperasiFilter;
                ps.setString(6, kdDokter);
                ps.setString(7, kdOperasi);
                ps.setString(8, kdDokter);
                ps.setString(9, kdOperasi);
                ps.setString(10, kdDokter);
                ps.setString(11, kdOperasi);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tabModePaket.addRow(new Object[]{
                        rs.getString("kd_paket_obat"),
                        rs.getString("nama_paket"),
                        rs.getString("dokter"),
                        rs.getString("operasi"),
                        rs.getString("status"),
                        rs.getString("keterangan")
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
        if (tabModePaket.getRowCount() > 0) {
            tbPaket.setRowSelectionInterval(0, 0);
            tampilDataPaket();
        } else if (!selectionMode) {
            emptTeks();
        }
    }

    private void tampilObat() {
        Valid.tabelKosong(tabModeObat);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select obatbhp_ok.kd_obat,obatbhp_ok.nm_obat,kodesatuan.satuan,obatbhp_ok.hargasatuan " +
                "from obatbhp_ok inner join kodesatuan on obatbhp_ok.kode_sat=kodesatuan.kode_sat " +
                "where obatbhp_ok.kd_obat like ? or obatbhp_ok.nm_obat like ? or kodesatuan.satuan like ? order by obatbhp_ok.nm_obat")) {
            String cari = "%" + TCariObat.getText().trim() + "%";
            ps.setString(1, cari);
            ps.setString(2, cari);
            ps.setString(3, cari);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tabModeObat.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getDouble(4)});
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void tampilDataPaket() {
        int row = tbPaket.getSelectedRow();
        if (row == -1) {
            return;
        }
        String kode = tbPaket.getValueAt(row, 0).toString();
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select p.kd_paket_obat,p.nama_paket,p.kd_dokter,ifnull(d.nm_dokter,'') as nm_dokter," +
                "p.kode_paket,ifnull(o.nm_perawatan,'') as nm_perawatan,p.keterangan,p.status " +
                "from paket_obat_operasi p left join dokter d on p.kd_dokter=d.kd_dokter " +
                "left join paket_operasi o on p.kode_paket=o.kode_paket where p.kd_paket_obat=?")) {
            ps.setString(1, kode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TKodePaket.setText(rs.getString("kd_paket_obat"));
                    TNamaPaket.setText(rs.getString("nama_paket"));
                    TKdDokter.setText(rs.getString("kd_dokter"));
                    TNmDokter.setText(rs.getString("nm_dokter"));
                    TKodeOperasi.setText(rs.getString("kode_paket"));
                    TNamaOperasi.setText(rs.getString("nm_perawatan"));
                    TKeterangan.setText(rs.getString("keterangan"));
                    CmbStatus.setSelectedItem(rs.getString("status"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
        tampilDetail(kode);
    }

    private void tampilDetail(String kdPaketObat) {
        Valid.tabelKosong(tabModeDetail);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select d.jumlah,d.kd_obat,o.nm_obat,k.satuan,o.hargasatuan,(d.jumlah*o.hargasatuan) as total " +
                "from paket_obat_operasi_detail d inner join obatbhp_ok o on d.kd_obat=o.kd_obat " +
                "inner join kodesatuan k on o.kode_sat=k.kode_sat where d.kd_paket_obat=? order by d.urut,o.nm_obat")) {
            ps.setString(1, kdPaketObat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tabModeDetail.addRow(new Object[]{
                        rs.getDouble("jumlah"),
                        rs.getString("kd_obat"),
                        rs.getString("nm_obat"),
                        rs.getString("satuan"),
                        rs.getDouble("hargasatuan"),
                        rs.getDouble("total")
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void bukaCariDokter() {
        dokter.setSize(900, 540);
        dokter.setLocationRelativeTo(this);
        dokter.setVisible(true);
    }

    private void bukaCariOperasi() {
        operasi.emptTeks();
        operasi.isCek();
        operasi.setSize(1000, 600);
        operasi.setLocationRelativeTo(this);
        operasi.setVisible(true);
    }

    private void tambahItemTerpilih() {
        int row = tbObat.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Silakan pilih item obat/BHP terlebih dahulu.");
            return;
        }
        tambahAtauGabungDetail(
                tbObat.getValueAt(row, 0).toString(),
                tbObat.getValueAt(row, 1).toString(),
                tbObat.getValueAt(row, 2).toString(),
                Valid.SetAngka(tbObat.getValueAt(row, 3).toString()),
                1d
        );
    }

    private void tambahAtauGabungDetail(String kdObat, String nama, String satuan, double harga, double jumlahTambah) {
        int existing = findDetailRow(kdObat);
        if (existing >= 0) {
            double jumlah = Valid.SetAngka(tabModeDetail.getValueAt(existing, 0).toString()) + jumlahTambah;
            tabModeDetail.setValueAt(jumlah, existing, 0);
            tabModeDetail.setValueAt(nama, existing, 2);
            tabModeDetail.setValueAt(satuan, existing, 3);
            tabModeDetail.setValueAt(harga, existing, 4);
            hitungBarisDetail(existing);
        } else {
            tabModeDetail.addRow(new Object[]{jumlahTambah, kdObat, nama, satuan, harga, harga * jumlahTambah});
        }
    }

    private int findDetailRow(String kdObat) {
        for (int i = 0; i < tabModeDetail.getRowCount(); i++) {
            if (tabModeDetail.getValueAt(i, 1).toString().equals(kdObat)) {
                return i;
            }
        }
        return -1;
    }

    private void hapusItemDetail() {
        int row = tbDetail.getSelectedRow();
        if (row != -1) {
            tabModeDetail.removeRow(row);
        }
    }

    private void hitungBarisDetail(int row) {
        if (row < 0 || row >= tabModeDetail.getRowCount()) {
            return;
        }
        double jumlah = Valid.SetAngka(tabModeDetail.getValueAt(row, 0).toString());
        double harga = Valid.SetAngka(tabModeDetail.getValueAt(row, 4).toString());
        tabModeDetail.setValueAt(Valid.SetAngka2(jumlah * harga), row, 5);
    }

    private void simpanPaket() {
        if (selectionMode) {
            return;
        }
        if (TKodePaket.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kode paket belum terisi.");
            return;
        }
        if (TNamaPaket.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama paket belum terisi.");
            return;
        }
        if (tabModeDetail.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Detail obat/BHP paket masih kosong.");
            return;
        }

        String kdDokter = TKdDokter.getText().trim().isEmpty() ? "-" : TKdDokter.getText().trim();
        String kdOperasi = TKodeOperasi.getText().trim().isEmpty() ? "-" : TKodeOperasi.getText().trim();

        try {
            koneksi.setAutoCommit(false);
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "insert into paket_obat_operasi(kd_paket_obat,nama_paket,kd_dokter,kode_paket,keterangan,status) " +
                    "values(?,?,?,?,?,?) on duplicate key update nama_paket=values(nama_paket),kd_dokter=values(kd_dokter)," +
                    "kode_paket=values(kode_paket),keterangan=values(keterangan),status=values(status)")) {
                ps.setString(1, TKodePaket.getText().trim());
                ps.setString(2, TNamaPaket.getText().trim());
                ps.setString(3, kdDokter);
                ps.setString(4, kdOperasi);
                ps.setString(5, TKeterangan.getText().trim());
                ps.setString(6, CmbStatus.getSelectedItem().toString());
                ps.executeUpdate();
            }

            try (PreparedStatement delete = koneksi.prepareStatement("delete from paket_obat_operasi_detail where kd_paket_obat=?")) {
                delete.setString(1, TKodePaket.getText().trim());
                delete.executeUpdate();
            }

            try (PreparedStatement insert = koneksi.prepareStatement(
                    "insert into paket_obat_operasi_detail(kd_paket_obat,kd_obat,jumlah,urut) values(?,?,?,?)")) {
                for (int i = 0; i < tabModeDetail.getRowCount(); i++) {
                    insert.setString(1, TKodePaket.getText().trim());
                    insert.setString(2, tabModeDetail.getValueAt(i, 1).toString());
                    insert.setDouble(3, Valid.SetAngka(tabModeDetail.getValueAt(i, 0).toString()));
                    insert.setInt(4, i + 1);
                    insert.addBatch();
                }
                insert.executeBatch();
            }

            koneksi.commit();
            JOptionPane.showMessageDialog(this, "Paket obat/BHP operasi berhasil disimpan.");
            tampilPaket();
            selectedPackageCode = TKodePaket.getText().trim();
        } catch (Exception e) {
            try {
                koneksi.rollback();
            } catch (Exception ignored) {
            }
            JOptionPane.showMessageDialog(this, "Gagal menyimpan paket obat/BHP operasi.\n" + e.getMessage());
        } finally {
            try {
                koneksi.setAutoCommit(true);
            } catch (Exception ignored) {
            }
        }
    }

    private void hapusPaket() {
        if (selectionMode) {
            return;
        }
        if (TKodePaket.getText().trim().isEmpty()) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus paket " + TNamaPaket.getText().trim() + " ?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            koneksi.setAutoCommit(false);
            try (PreparedStatement deleteDetail = koneksi.prepareStatement("delete from paket_obat_operasi_detail where kd_paket_obat=?");
                 PreparedStatement deleteHeader = koneksi.prepareStatement("delete from paket_obat_operasi where kd_paket_obat=?")) {
                deleteDetail.setString(1, TKodePaket.getText().trim());
                deleteDetail.executeUpdate();
                deleteHeader.setString(1, TKodePaket.getText().trim());
                deleteHeader.executeUpdate();
            }
            koneksi.commit();
            JOptionPane.showMessageDialog(this, "Paket berhasil dihapus.");
            emptTeks();
            tampilPaket();
        } catch (Exception e) {
            try {
                koneksi.rollback();
            } catch (Exception ignored) {
            }
            JOptionPane.showMessageDialog(this, "Gagal menghapus paket.\n" + e.getMessage());
        } finally {
            try {
                koneksi.setAutoCommit(true);
            } catch (Exception ignored) {
            }
        }
    }

    private void pilihPaket() {
        int row = tbPaket.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Silakan pilih paket terlebih dahulu.");
            return;
        }
        selectedPackageCode = tbPaket.getValueAt(row, 0).toString();
        dispose();
    }

    private void setLebarKolom(JTable table, int[] widths) {
        for (int i = 0; i < widths.length; i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(widths[i]);
        }
    }
}
