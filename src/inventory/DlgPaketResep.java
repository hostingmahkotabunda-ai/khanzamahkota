package inventory;

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

/**
 * Master / pemilih Paket Resep (paket obat untuk input resep dokter).
 * Paket bersifat umum (dipakai semua dokter). Pola mengikuti DlgPaketObatOperasi,
 * namun disederhanakan: tanpa dokter/operasi, obat dipilih dari master databarang.
 */
public class DlgPaketResep extends JDialog {
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();

    private final widget.TextBox TKodePaket = new widget.TextBox();
    private final widget.TextBox TNamaPaket = new widget.TextBox();
    private final widget.TextBox TKeterangan = new widget.TextBox();
    private final widget.TextBox TCariPaket = new widget.TextBox();
    private final widget.TextBox TCariObat = new widget.TextBox();
    private final widget.ComboBox CmbStatus = new widget.ComboBox();

    private final widget.Button BtnBaru = new widget.Button();
    private final widget.Button BtnSimpan = new widget.Button();
    private final widget.Button BtnHapus = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();
    private final widget.Button BtnPilih = new widget.Button();
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

    private boolean selectionMode = false;
    private String selectedPackageCode = "";

    public DlgPaketResep(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Paket Resep Obat ]::");
        setSize(1100, 700);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        ensureSupportTables();

        Object[] headerPaket = {"Kode Paket", "Nama Paket", "Status", "Keterangan"};
        tabModePaket = new DefaultTableModel(null, headerPaket) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        Object[] headerDetail = {"Jumlah", "Kode Barang", "Nama Barang", "Satuan"};
        tabModeDetail = new DefaultTableModel(null, headerDetail) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex == 0;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Double.class : Object.class;
            }
        };
        Object[] headerObat = {"Kode Barang", "Nama Barang", "Satuan"};
        tabModeObat = new DefaultTableModel(null, headerObat) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };

        initComponents();
        emptTeks();
        tampilPaket();
        tampilObat();
    }

    private void initComponents() {
        TKodePaket.setEditable(false);
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
        BtnPilih.setText("Terapkan");
        BtnTambahItem.setText("Tambah Obat");
        BtnHapusItem.setText("Hapus Obat");
        BtnCariPaket.setText("Tampil");
        BtnCariObat.setText("Cari Obat");
        BtnPilih.setVisible(false);

        BtnBaru.addActionListener(evt -> emptTeks());
        BtnSimpan.addActionListener(evt -> simpanPaket());
        BtnHapus.addActionListener(evt -> hapusPaket());
        BtnKeluar.addActionListener(evt -> dispose());
        BtnPilih.addActionListener(evt -> pilihPaket());
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
                        if (evt.getClickCount() == 2) {
                            pilihPaket();
                        }
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

        setLebarKolom(tbPaket, new int[]{110, 260, 70, 250});
        setLebarKolom(tbDetail, new int[]{70, 120, 330, 110});
        setLebarKolom(tbObat, new int[]{110, 280, 90});

        JPanel root = new JPanel(new BorderLayout(5, 5));
        root.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        JPanel form = new JPanel(new GridLayout(3, 1, 4, 4));
        form.setBorder(BorderFactory.createTitledBorder("Data Paket  (klik 'Baru' untuk paket baru, lalu isi Nama Paket)"));
        form.add(barisForm("Kode Paket", TKodePaket, "Nama Paket", TNamaPaket));
        form.add(barisForm("Status", CmbStatus, "Keterangan", TKeterangan));
        form.add(barisFilterPaket());

        JPanel panelPaket = new JPanel(new BorderLayout(4, 4));
        panelPaket.setBorder(BorderFactory.createTitledBorder("Daftar Paket Tersimpan  (klik baris untuk memilih / mengubah)"));
        widget.ScrollPane scrollPaket = new widget.ScrollPane();
        scrollPaket.setViewportView(tbPaket);
        panelPaket.add(scrollPaket, BorderLayout.CENTER);

        JPanel panelDetail = new JPanel(new BorderLayout(4, 4));
        panelDetail.setBorder(BorderFactory.createTitledBorder("Isi Obat Paket  (atur Jumlah obat)"));
        widget.ScrollPane scrollDetail = new widget.ScrollPane();
        scrollDetail.setViewportView(tbDetail);
        panelDetail.add(scrollDetail, BorderLayout.CENTER);
        JPanel detailButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        detailButtons.add(BtnTambahItem);
        detailButtons.add(BtnHapusItem);
        panelDetail.add(detailButtons, BorderLayout.SOUTH);

        JPanel panelObat = new JPanel(new BorderLayout(4, 4));
        panelObat.setBorder(BorderFactory.createTitledBorder("Referensi Obat (master) - DOBEL-KLIK untuk menambah ke paket"));
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
        bawah.setResizeWeight(0.55);
        bawah.setDividerLocation(560);
        JSplitPane tengah = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelPaket, bawah);
        tengah.setResizeWeight(0.42);
        tengah.setDividerLocation(220);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        actions.add(BtnBaru);
        actions.add(BtnSimpan);
        actions.add(BtnHapus);
        actions.add(BtnPilih);
        actions.add(BtnKeluar);

        javax.swing.JLabel petunjuk = new javax.swing.JLabel("<html><b>Buat paket:</b> 1) klik <b>Baru</b> &amp; isi <b>Nama Paket</b> &nbsp; 2) <b>dobel-klik</b> obat di <b>Referensi Obat</b> (kanan bawah) untuk menambah, lalu atur <b>Jumlah</b> &nbsp; 3) <b>Simpan</b>. &nbsp;&nbsp; <b>Pakai di resep:</b> pilih paket di <b>Daftar Paket</b> lalu <b>Terapkan</b>.</html>");
        petunjuk.setBorder(BorderFactory.createEmptyBorder(2, 6, 4, 6));
        JPanel atas = new JPanel(new BorderLayout());
        atas.add(petunjuk, BorderLayout.NORTH);
        atas.add(form, BorderLayout.CENTER);

        root.add(atas, BorderLayout.NORTH);
        root.add(tengah, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel barisForm(String labelKiri, java.awt.Component compKiri, String labelKanan, java.awt.Component compKanan) {
        JPanel p = new JPanel(new GridLayout(1, 2, 16, 4));
        p.add(setengahForm(labelKiri, compKiri));
        p.add(setengahForm(labelKanan, compKanan));
        return p;
    }

    private JPanel setengahForm(String label, java.awt.Component comp) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        widget.Label l = new widget.Label();
        l.setText(label);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        l.setPreferredSize(new Dimension(90, 23));
        p.add(l, BorderLayout.WEST);
        p.add(comp, BorderLayout.CENTER);
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

    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
        this.selectedPackageCode = "";
        BtnPilih.setVisible(selectionMode);
        if (selectionMode) {
            setTitle("::[ Pilih Paket Resep Obat ]::");
        } else {
            setTitle("::[ Paket Resep Obat ]::");
        }
    }

    public String getSelectedPackageCode() {
        return selectedPackageCode;
    }

    public void emptTeks() {
        TKodePaket.setText(generateKodePaket());
        TNamaPaket.setText("");
        TKeterangan.setText("");
        CmbStatus.setSelectedItem("AKTIF");
        Valid.tabelKosong(tabModeDetail);
        selectedPackageCode = "";
    }

    private String generateKodePaket() {
        int next = Sequel.cariInteger("select ifnull(max(convert(right(kd_paket,6),signed)),0)+1 from paket_resep where kd_paket like 'PKTR%'");
        return String.format(Locale.ENGLISH, "PKTR%06d", next);
    }

    private void ensureSupportTables() {
        try {
            Sequel.queryu("create table if not exists paket_resep (" +
                    "kd_paket varchar(20) not null," +
                    "nama_paket varchar(100) not null," +
                    "keterangan varchar(200) not null default ''," +
                    "status enum('AKTIF','NONAKTIF') not null default 'AKTIF'," +
                    "primary key (kd_paket)" +
                    ")");
            Sequel.queryu("create table if not exists paket_resep_detail (" +
                    "kd_paket varchar(20) not null," +
                    "kode_brng varchar(15) not null," +
                    "jml double not null default 0," +
                    "urut int not null default 0," +
                    "primary key (kd_paket,kode_brng)," +
                    "key idx_paket_resep_detail_obat (kode_brng)" +
                    ")");
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void tampilPaket() {
        Valid.tabelKosong(tabModePaket);
        String sql = "select kd_paket,nama_paket,status,keterangan from paket_resep " +
                "where (kd_paket like ? or nama_paket like ? or keterangan like ?) ";
        if (selectionMode) {
            sql = sql + "and status='AKTIF' ";
        }
        sql = sql + "order by nama_paket";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            String cari = "%" + TCariPaket.getText().trim() + "%";
            ps.setString(1, cari);
            ps.setString(2, cari);
            ps.setString(3, cari);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tabModePaket.addRow(new Object[]{
                        rs.getString("kd_paket"),
                        rs.getString("nama_paket"),
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
                "select databarang.kode_brng,databarang.nama_brng,kodesatuan.satuan " +
                "from databarang inner join kodesatuan on databarang.kode_sat=kodesatuan.kode_sat " +
                "where databarang.status='1' and (databarang.kode_brng like ? or databarang.nama_brng like ?) " +
                "order by databarang.nama_brng limit 500")) {
            String cari = "%" + TCariObat.getText().trim() + "%";
            ps.setString(1, cari);
            ps.setString(2, cari);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tabModeObat.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3)});
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
        TKodePaket.setText(kode);
        TNamaPaket.setText(tbPaket.getValueAt(row, 1).toString());
        CmbStatus.setSelectedItem(tbPaket.getValueAt(row, 2).toString());
        TKeterangan.setText(tbPaket.getValueAt(row, 3).toString());
        tampilDetail(kode);
    }

    private void tampilDetail(String kdPaket) {
        Valid.tabelKosong(tabModeDetail);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select d.jml,d.kode_brng,databarang.nama_brng,kodesatuan.satuan " +
                "from paket_resep_detail d inner join databarang on d.kode_brng=databarang.kode_brng " +
                "inner join kodesatuan on databarang.kode_sat=kodesatuan.kode_sat " +
                "where d.kd_paket=? order by d.urut,databarang.nama_brng")) {
            ps.setString(1, kdPaket);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tabModeDetail.addRow(new Object[]{
                        rs.getDouble("jml"),
                        rs.getString("kode_brng"),
                        rs.getString("nama_brng"),
                        rs.getString("satuan")
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void tambahItemTerpilih() {
        int row = tbObat.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Silakan pilih obat dari referensi terlebih dahulu.");
            return;
        }
        String kode = tbObat.getValueAt(row, 0).toString();
        for (int i = 0; i < tabModeDetail.getRowCount(); i++) {
            if (tabModeDetail.getValueAt(i, 1).toString().equals(kode)) {
                JOptionPane.showMessageDialog(this, "Obat tersebut sudah ada di paket.");
                return;
            }
        }
        tabModeDetail.addRow(new Object[]{
            1d, kode, tbObat.getValueAt(row, 1).toString(), tbObat.getValueAt(row, 2).toString()
        });
    }

    private void hapusItemDetail() {
        int row = tbDetail.getSelectedRow();
        if (row != -1) {
            tabModeDetail.removeRow(row);
        }
    }

    private void simpanPaket() {
        if (TKodePaket.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kode paket belum terisi.");
            return;
        }
        if (TNamaPaket.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama paket belum terisi.");
            return;
        }
        if (tabModeDetail.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Isi obat paket masih kosong.");
            return;
        }

        try {
            koneksi.setAutoCommit(false);
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "insert into paket_resep(kd_paket,nama_paket,keterangan,status) values(?,?,?,?) " +
                    "on duplicate key update nama_paket=values(nama_paket),keterangan=values(keterangan),status=values(status)")) {
                ps.setString(1, TKodePaket.getText().trim());
                ps.setString(2, TNamaPaket.getText().trim());
                ps.setString(3, TKeterangan.getText().trim());
                ps.setString(4, CmbStatus.getSelectedItem().toString());
                ps.executeUpdate();
            }

            try (PreparedStatement delete = koneksi.prepareStatement("delete from paket_resep_detail where kd_paket=?")) {
                delete.setString(1, TKodePaket.getText().trim());
                delete.executeUpdate();
            }

            try (PreparedStatement insert = koneksi.prepareStatement(
                    "insert into paket_resep_detail(kd_paket,kode_brng,jml,urut) values(?,?,?,?)")) {
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
            JOptionPane.showMessageDialog(this, "Paket resep berhasil disimpan.");
            tampilPaket();
        } catch (Exception e) {
            try {
                koneksi.rollback();
            } catch (Exception ignored) {
            }
            JOptionPane.showMessageDialog(this, "Gagal menyimpan paket resep.\n" + e.getMessage());
        } finally {
            try {
                koneksi.setAutoCommit(true);
            } catch (Exception ignored) {
            }
        }
    }

    private void hapusPaket() {
        if (TKodePaket.getText().trim().isEmpty()) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus paket " + TNamaPaket.getText().trim() + " ?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            koneksi.setAutoCommit(false);
            try (PreparedStatement deleteDetail = koneksi.prepareStatement("delete from paket_resep_detail where kd_paket=?");
                 PreparedStatement deleteHeader = koneksi.prepareStatement("delete from paket_resep where kd_paket=?")) {
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
