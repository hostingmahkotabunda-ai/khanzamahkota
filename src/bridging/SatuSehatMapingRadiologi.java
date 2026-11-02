package bridging;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import keuangan.DlgJnsPerawatanRadiologi;

public final class SatuSehatMapingRadiologi extends JDialog {
    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final DlgJnsPerawatanRadiologi dlgPemeriksaan = new DlgJnsPerawatanRadiologi(null, false);
    private final DefaultTableModel tabMode;
    private final JTable tbData = new JTable();
    private final JTextField TCari = new JTextField(35);
    private final JTextField TxtKodePemeriksaan = new JTextField(18);
    private final JTextField TxtNamaPemeriksaan = new JTextField(35);
    private final JTextField TxtCode = new JTextField(20);
    private final JTextField TxtSystem = new JTextField(35);
    private final JTextField TxtDisplay = new JTextField(35);
    private final JTextField TxtAreaCode = new JTextField(20);
    private final JTextField TxtAreaSystem = new JTextField(35);
    private final JTextField TxtAreaDisplay = new JTextField(35);
    private final JButton BtnCariPemeriksaan = new JButton("Cari Pemeriksaan");
    private final JButton BtnReferensiCsv = new JButton("Referensi CSV");
    private final JButton BtnAmbilSerupa = new JButton("Ambil Serupa");
    private final JButton BtnSaranArea = new JButton("Saran Area");
    private final JButton BtnSimpan = new JButton("Simpan");
    private final JButton BtnEdit = new JButton("Ganti");
    private final JButton BtnHapus = new JButton("Hapus");
    private final JButton BtnBatal = new JButton("Bersihkan");
    private final JButton BtnPrint = new JButton("Cetak");
    private final JButton BtnKeluar = new JButton("Keluar");
    private final File referensiCsvFile = new File("setting/referensi_satu_sehat/Terminologi_LOINC_Radiologi_v1_0_gid0.csv");
    private List<LocalRadiologyReference> referensiCsvCache = null;

    public SatuSehatMapingRadiologi(Frame parent, boolean modal) {
        super(parent, modal);
        tabMode = new DefaultTableModel(null, new Object[]{
            "Kode Periksa", "Nama Pemeriksaan", "Periksa Code", "Periksa System",
            "Periksa Display", "Area Code", "Area System", "Area Display"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        ensureSupportTables();
        initDialogPemeriksaan();
        initComponents();
        tampil();
    }

    private void initDialogPemeriksaan() {
        dlgPemeriksaan.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                ambilPemeriksaanTerpilih();
            }
        });
        dlgPemeriksaan.getTable().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    dlgPemeriksaan.dispose();
                }
            }
        });
    }

    private void initComponents() {
        setTitle("::[ Data Mapping Tindakan Radiologi Satu Sehat ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(6, 6));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        JButton btnTampil = new JButton("Tampil");
        top.add(new JLabel("Keyword :"));
        top.add(TCari);
        top.add(btnTampil);
        add(top, BorderLayout.NORTH);

        tbData.setModel(tabMode);
        tbData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbData.getColumnModel().getColumn(0).setPreferredWidth(95);
        tbData.getColumnModel().getColumn(1).setPreferredWidth(260);
        tbData.getColumnModel().getColumn(2).setPreferredWidth(95);
        tbData.getColumnModel().getColumn(3).setPreferredWidth(220);
        tbData.getColumnModel().getColumn(4).setPreferredWidth(260);
        tbData.getColumnModel().getColumn(5).setPreferredWidth(95);
        tbData.getColumnModel().getColumn(6).setPreferredWidth(220);
        tbData.getColumnModel().getColumn(7).setPreferredWidth(260);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        TxtKodePemeriksaan.setEditable(false);
        TxtNamaPemeriksaan.setEditable(false);

        int row = 0;
        addFormRow(form, gbc, row++, "Kode Pemeriksaan", TxtKodePemeriksaan, BtnCariPemeriksaan);
        addFormRow(form, gbc, row++, "Nama Pemeriksaan", TxtNamaPemeriksaan, null);
        addFormRow(form, gbc, row++, "Periksa Code", TxtCode, BtnAmbilSerupa);
        addFormRow(form, gbc, row++, "Periksa System", TxtSystem, null);
        addFormRow(form, gbc, row++, "Periksa Display", TxtDisplay, null);
        addFormRow(form, gbc, row++, "Area Code", TxtAreaCode, BtnSaranArea);
        addFormRow(form, gbc, row++, "Area System", TxtAreaSystem, null);
        addFormRow(form, gbc, row++, "Area Display", TxtAreaDisplay, null);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        buttons.add(BtnReferensiCsv);
        buttons.add(BtnSimpan);
        buttons.add(BtnEdit);
        buttons.add(BtnHapus);
        buttons.add(BtnBatal);
        buttons.add(BtnPrint);
        buttons.add(BtnKeluar);

        JPanel right = new JPanel(new BorderLayout(6, 6));
        right.add(form, BorderLayout.CENTER);
        right.add(buttons, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tbData), right);
        split.setResizeWeight(0.58);
        add(split, BorderLayout.CENTER);

        btnTampil.addActionListener(evt -> tampil());
        BtnCariPemeriksaan.addActionListener(evt -> pilihPemeriksaan());
        BtnReferensiCsv.addActionListener(evt -> pilihReferensiCsv());
        BtnAmbilSerupa.addActionListener(evt -> ambilSerupa(true, true));
        BtnSaranArea.addActionListener(evt -> saranArea(true, true));
        BtnSimpan.addActionListener(evt -> simpan());
        BtnEdit.addActionListener(evt -> edit());
        BtnHapus.addActionListener(evt -> hapus());
        BtnBatal.addActionListener(evt -> bersih());
        BtnPrint.addActionListener(evt -> cetak());
        BtnKeluar.addActionListener(evt -> dispose());

        TCari.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                tampil();
            }
        });

        tbData.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    loadSelectedRow();
                }
            }
        });

        setPreferredSize(new Dimension(1220, 650));
        pack();
        refreshActionState();
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, JTextField field, JButton button) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        form.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(field, gbc);

        if (button != null) {
            gbc.gridx = 2;
            gbc.weightx = 0;
            form.add(button, gbc);
        }
    }

    public void isCek() {
        refreshActionState();
    }

    private void ensureSupportTables() {
        PreparedStatement ps = null;
        try {
            ps = koneksi.prepareStatement(
                "create table if not exists satu_sehat_mapping_radiologi (" +
                "kd_jenis_prw varchar(15) not null," +
                "code varchar(50) default null," +
                "system varchar(255) default null," +
                "display varchar(500) default null," +
                "sampel_code varchar(50) default null," +
                "sampel_system varchar(255) default null," +
                "sampel_display varchar(500) default null," +
                "primary key (kd_jenis_prw)" +
                ") engine=InnoDB default charset=latin1 collate=latin1_swedish_ci"
            );
            ps.executeUpdate();
            ps.close();

            ps = koneksi.prepareStatement("alter table satu_sehat_mapping_radiologi modify code varchar(50) null");
            ps.executeUpdate();
            ps.close();

            ps = koneksi.prepareStatement("alter table satu_sehat_mapping_radiologi modify system varchar(255) null");
            ps.executeUpdate();
            ps.close();

            ps = koneksi.prepareStatement("alter table satu_sehat_mapping_radiologi modify display varchar(500) null");
            ps.executeUpdate();
            ps.close();

            ps = koneksi.prepareStatement("alter table satu_sehat_mapping_radiologi modify sampel_code varchar(50) null");
            ps.executeUpdate();
            ps.close();

            ps = koneksi.prepareStatement("alter table satu_sehat_mapping_radiologi modify sampel_system varchar(255) null");
            ps.executeUpdate();
            ps.close();

            ps = koneksi.prepareStatement("alter table satu_sehat_mapping_radiologi modify sampel_display varchar(500) null");
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void tampil() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select jpr.kd_jenis_prw,jpr.nm_perawatan,ifnull(map.code,'') as code,ifnull(map.system,'') as system," +
                "ifnull(map.display,'') as display,ifnull(map.sampel_code,'') as sampel_code," +
                "ifnull(map.sampel_system,'') as sampel_system,ifnull(map.sampel_display,'') as sampel_display " +
                "from jns_perawatan_radiologi jpr " +
                "left join satu_sehat_mapping_radiologi map on map.kd_jenis_prw=jpr.kd_jenis_prw " +
                "where jpr.status='1' and (" +
                "jpr.kd_jenis_prw like ? or jpr.nm_perawatan like ? or ifnull(map.code,'') like ? or " +
                "ifnull(map.display,'') like ? or ifnull(map.sampel_display,'') like ?" +
                ") order by jpr.nm_perawatan")) {
            String cari = "%" + TCari.getText().trim() + "%";
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, cari);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (tabMode.getRowCount() > 0) {
                    tabMode.removeRow(0);
                }
                while (rs.next()) {
                    tabMode.addRow(new Object[]{
                        rs.getString("kd_jenis_prw"),
                        rs.getString("nm_perawatan"),
                        rs.getString("code"),
                        rs.getString("system"),
                        rs.getString("display"),
                        rs.getString("sampel_code"),
                        rs.getString("sampel_system"),
                        rs.getString("sampel_display")
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        } finally {
            setCursor(Cursor.getDefaultCursor());
            refreshActionState();
        }
    }

    private void pilihPemeriksaan() {
        dlgPemeriksaan.isCek();
        dlgPemeriksaan.setSize(getWidth() - 20, getHeight() - 20);
        dlgPemeriksaan.setLocationRelativeTo(this);
        dlgPemeriksaan.setVisible(true);
    }

    private void ambilPemeriksaanTerpilih() {
        int row = dlgPemeriksaan.getTable().getSelectedRow();
        if (row < 0) {
            refreshActionState();
            return;
        }
        TxtKodePemeriksaan.setText(dlgPemeriksaan.getTable().getValueAt(row, 1).toString());
        TxtNamaPemeriksaan.setText(dlgPemeriksaan.getTable().getValueAt(row, 2).toString());
        loadMappingByPemeriksaan(TxtKodePemeriksaan.getText().trim());
        terapkanSaranOtomatis();
        refreshActionState();
    }

    private void loadMappingByPemeriksaan(String kdJenisPrw) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select code,system,display,sampel_code,sampel_system,sampel_display " +
                "from satu_sehat_mapping_radiologi where kd_jenis_prw=?")) {
            ps.setString(1, kdJenisPrw);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TxtCode.setText(rs.getString("code"));
                    TxtSystem.setText(rs.getString("system"));
                    TxtDisplay.setText(rs.getString("display"));
                    TxtAreaCode.setText(rs.getString("sampel_code"));
                    TxtAreaSystem.setText(rs.getString("sampel_system"));
                    TxtAreaDisplay.setText(rs.getString("sampel_display"));
                } else {
                    TxtCode.setText("");
                    TxtSystem.setText("");
                    TxtDisplay.setText("");
                    TxtAreaCode.setText("");
                    TxtAreaSystem.setText("");
                    TxtAreaDisplay.setText("");
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void simpan() {
        if (!validInput()) {
            return;
        }
        if (mappingSudahAda(TxtKodePemeriksaan.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Mapping untuk pemeriksaan ini sudah ada. Gunakan tombol Ganti.");
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into satu_sehat_mapping_radiologi(kd_jenis_prw,code,system,display,sampel_code,sampel_system,sampel_display) values(?,?,?,?,?,?,?)")) {
            bindMapping(ps);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Mapping radiologi berhasil disimpan.");
            tampil();
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
            JOptionPane.showMessageDialog(this, "Gagal menyimpan mapping radiologi.");
        }
    }

    private void edit() {
        if (!validInput()) {
            return;
        }
        if (!mappingSudahAda(TxtKodePemeriksaan.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Mapping belum ada. Gunakan tombol Simpan.");
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "update satu_sehat_mapping_radiologi set code=?,system=?,display=?,sampel_code=?,sampel_system=?,sampel_display=? where kd_jenis_prw=?")) {
            ps.setString(1, TxtCode.getText().trim());
            ps.setString(2, TxtSystem.getText().trim());
            ps.setString(3, TxtDisplay.getText().trim());
            ps.setString(4, TxtAreaCode.getText().trim());
            ps.setString(5, TxtAreaSystem.getText().trim());
            ps.setString(6, TxtAreaDisplay.getText().trim());
            ps.setString(7, TxtKodePemeriksaan.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Mapping radiologi berhasil diperbarui.");
            tampil();
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
            JOptionPane.showMessageDialog(this, "Gagal memperbarui mapping radiologi.");
        }
    }

    private void hapus() {
        if (TxtKodePemeriksaan.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih pemeriksaan radiologi terlebih dahulu.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus mapping radiologi ini?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement("delete from satu_sehat_mapping_radiologi where kd_jenis_prw=?")) {
            ps.setString(1, TxtKodePemeriksaan.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Mapping radiologi berhasil dihapus.");
            bersih();
            tampil();
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
            JOptionPane.showMessageDialog(this, "Gagal menghapus mapping radiologi.");
        }
    }

    private void cetak() {
        if (tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Belum ada data yang bisa dicetak.");
            return;
        }
        File report = new File("report/rptMapingPemeriksaanRadiologiSatuSehat.jasper");
        if (!report.exists()) {
            JOptionPane.showMessageDialog(this, "Report mapping radiologi belum ada di repo ini.");
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
            param.put("parameter", "%" + TCari.getText().trim() + "%");
            Valid.MyReport(report.getName(), "report", "::[ Mapping Pemeriksaan Radiologi Satu Sehat ]::", param);
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void bersih() {
        TxtKodePemeriksaan.setText("");
        TxtNamaPemeriksaan.setText("");
        TxtCode.setText("");
        TxtSystem.setText("");
        TxtDisplay.setText("");
        TxtAreaCode.setText("");
        TxtAreaSystem.setText("");
        TxtAreaDisplay.setText("");
        tbData.clearSelection();
        refreshActionState();
    }

    private void loadSelectedRow() {
        int row = tbData.getSelectedRow();
        if (row < 0) {
            refreshActionState();
            return;
        }
        TxtKodePemeriksaan.setText(tabMode.getValueAt(row, 0).toString());
        TxtNamaPemeriksaan.setText(tabMode.getValueAt(row, 1).toString());
        TxtCode.setText(tabMode.getValueAt(row, 2).toString());
        TxtSystem.setText(tabMode.getValueAt(row, 3).toString());
        TxtDisplay.setText(tabMode.getValueAt(row, 4).toString());
        TxtAreaCode.setText(tabMode.getValueAt(row, 5).toString());
        TxtAreaSystem.setText(tabMode.getValueAt(row, 6).toString());
        TxtAreaDisplay.setText(tabMode.getValueAt(row, 7).toString());
        refreshActionState();
    }

    private boolean validInput() {
        if (TxtKodePemeriksaan.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih pemeriksaan radiologi terlebih dahulu.");
            BtnCariPemeriksaan.requestFocus();
            return false;
        }
        if (TxtCode.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Code SATUSEHAT belum diisi.");
            TxtCode.requestFocus();
            return false;
        }
        if (TxtSystem.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "System SATUSEHAT belum diisi.");
            TxtSystem.requestFocus();
            return false;
        }
        if (TxtDisplay.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Display SATUSEHAT belum diisi.");
            TxtDisplay.requestFocus();
            return false;
        }
        if (TxtAreaCode.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Area Code belum diisi.");
            TxtAreaCode.requestFocus();
            return false;
        }
        if (TxtAreaSystem.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Area System belum diisi.");
            TxtAreaSystem.requestFocus();
            return false;
        }
        if (TxtAreaDisplay.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Area Display belum diisi.");
            TxtAreaDisplay.requestFocus();
            return false;
        }
        return true;
    }

    private boolean mappingSudahAda(String kdJenisPrw) {
        try (PreparedStatement ps = koneksi.prepareStatement("select count(*) from satu_sehat_mapping_radiologi where kd_jenis_prw=?")) {
            ps.setString(1, kdJenisPrw);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
        return false;
    }

    private void bindMapping(PreparedStatement ps) throws Exception {
        ps.setString(1, TxtKodePemeriksaan.getText().trim());
        ps.setString(2, TxtCode.getText().trim());
        ps.setString(3, TxtSystem.getText().trim());
        ps.setString(4, TxtDisplay.getText().trim());
        ps.setString(5, TxtAreaCode.getText().trim());
        ps.setString(6, TxtAreaSystem.getText().trim());
        ps.setString(7, TxtAreaDisplay.getText().trim());
    }

    private void refreshActionState() {
        boolean canManage = akses.getsatu_sehat_mapping_obat();
        boolean hasSelection = !TxtKodePemeriksaan.getText().trim().isEmpty();
        boolean hasMapping = hasSelection && mappingSudahAda(TxtKodePemeriksaan.getText().trim());
        BtnReferensiCsv.setEnabled(hasSelection && referensiCsvFile.exists());
        BtnAmbilSerupa.setEnabled(hasSelection);
        BtnSaranArea.setEnabled(hasSelection);
        BtnSimpan.setEnabled(canManage && hasSelection && !hasMapping);
        BtnEdit.setEnabled(canManage && hasMapping);
        BtnHapus.setEnabled(canManage && hasMapping);
        BtnPrint.setEnabled(canManage && tabMode.getRowCount() > 0);
    }

    private void terapkanSaranOtomatis() {
        boolean csv = ambilReferensiCsv(false, false);
        if (!csv) {
            ambilSerupa(false, false);
        }
        saranArea(false, false);
    }

    private boolean ambilSerupa(boolean overwrite, boolean showMessage) {
        String kdJenisPrw = TxtKodePemeriksaan.getText().trim();
        String namaPeriksa = TxtNamaPemeriksaan.getText().trim();
        if (kdJenisPrw.isEmpty() || namaPeriksa.isEmpty()) {
            if (showMessage) {
                JOptionPane.showMessageDialog(this, "Silakan pilih pemeriksaan radiologi terlebih dahulu.");
            }
            return false;
        }

        String namaLower = namaPeriksa.toLowerCase(Locale.ENGLISH).trim();
        String likeNama = "%" + namaLower + "%";

        try (PreparedStatement ps = koneksi.prepareStatement(
                "select map.code,map.system,map.display,map.sampel_code,map.sampel_system,map.sampel_display,jpr.nm_perawatan," +
                "(case when lower(jpr.nm_perawatan)=? then 60 else 0 end +" +
                " case when lower(jpr.nm_perawatan) like ? then 25 else 0 end +" +
                " case when ? like concat('%', lower(jpr.nm_perawatan), '%') then 15 else 0 end) as skor " +
                "from satu_sehat_mapping_radiologi map " +
                "inner join jns_perawatan_radiologi jpr on jpr.kd_jenis_prw=map.kd_jenis_prw " +
                "where map.kd_jenis_prw<>? and jpr.status='1' and (" +
                "lower(jpr.nm_perawatan) like ? or ? like concat('%', lower(jpr.nm_perawatan), '%')" +
                ") order by skor desc,length(jpr.nm_perawatan),jpr.kd_jenis_prw limit 1")) {
            ps.setString(1, namaLower);
            ps.setString(2, likeNama);
            ps.setString(3, namaLower);
            ps.setString(4, kdJenisPrw);
            ps.setString(5, likeNama);
            ps.setString(6, namaLower);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    applyText(TxtCode, rs.getString("code"), overwrite);
                    applyText(TxtSystem, rs.getString("system"), overwrite);
                    applyText(TxtDisplay, rs.getString("display"), overwrite);
                    applyText(TxtAreaCode, rs.getString("sampel_code"), overwrite);
                    applyText(TxtAreaSystem, rs.getString("sampel_system"), overwrite);
                    applyText(TxtAreaDisplay, rs.getString("sampel_display"), overwrite);
                    if (showMessage) {
                        JOptionPane.showMessageDialog(this,
                                "Saran diambil dari mapping serupa: " + rs.getString("nm_perawatan"));
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }

        if (showMessage) {
            JOptionPane.showMessageDialog(this, "Belum ada mapping radiologi serupa yang bisa dijadikan referensi.");
        }
        return false;
    }

    private boolean ambilReferensiCsv(boolean overwrite, boolean showMessage) {
        String namaPeriksa = TxtNamaPemeriksaan.getText().trim();
        if (namaPeriksa.isEmpty()) {
            if (showMessage) {
                JOptionPane.showMessageDialog(this, "Silakan pilih pemeriksaan radiologi terlebih dahulu.");
            }
            return false;
        }
        if (!referensiCsvFile.exists()) {
            if (showMessage) {
                JOptionPane.showMessageDialog(this, "File referensi CSV belum ditemukan:\n" + referensiCsvFile.getPath());
            }
            return false;
        }

        LocalRadiologyReference best = cariReferensiCsvTerbaik();
        if (best == null) {
            if (showMessage) {
                JOptionPane.showMessageDialog(this, "Belum ada kandidat cocok dari referensi CSV lokal.");
            }
            return false;
        }

        AreaSuggestion area = applyReferenceChoice(best, overwrite);

        if (showMessage) {
            String areaInfo = area == null || area.display.isEmpty()
                    ? "Belum ada saran area yang cukup kuat."
                    : "Saran Area : " + area.display + (area.code.isEmpty() ? " (kode perlu dicek/manual)" : "");
            JOptionPane.showMessageDialog(this,
                    "Referensi CSV ditemukan:\n" +
                    "Nama Pemeriksaan : " + best.namaPemeriksaan + "\n" +
                    "Code : " + best.code + "\n" +
                    "Display : " + best.display + "\n" +
                    areaInfo);
        }
        return true;
    }

    private void pilihReferensiCsv() {
        String namaPeriksa = TxtNamaPemeriksaan.getText().trim();
        if (namaPeriksa.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih pemeriksaan radiologi terlebih dahulu.");
            return;
        }
        if (!referensiCsvFile.exists()) {
            JOptionPane.showMessageDialog(this, "File referensi CSV belum ditemukan:\n" + referensiCsvFile.getPath());
            return;
        }

        List<ReferenceCandidate> kandidat = cariKandidatReferensiCsv();
        if (kandidat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Referensi radiologi lokal belum berhasil dimuat.");
            return;
        }

        LocalRadiologyReference pilihan = tampilkanDialogReferensiCsv(kandidat);
        if (pilihan == null) {
            return;
        }

        AreaSuggestion area = applyReferenceChoice(pilihan, true);
        String areaInfo = area == null || area.display.isEmpty()
                ? "Belum ada saran area yang cukup kuat."
                : "Saran Area : " + area.display + (area.code.isEmpty() ? " (kode perlu dicek/manual)" : "");
        JOptionPane.showMessageDialog(this,
                "Referensi dipilih:\n" +
                "Nama Pemeriksaan : " + pilihan.namaPemeriksaan + "\n" +
                "Code : " + pilihan.code + "\n" +
                "Display : " + pilihan.display + "\n" +
                areaInfo);
    }

    private LocalRadiologyReference tampilkanDialogReferensiCsv(List<ReferenceCandidate> kandidat) {
        final JDialog dialog = new JDialog(this, "Pilih Referensi CSV Radiologi", true);
        final JTextField txtCari = new JTextField(TxtNamaPemeriksaan.getText().trim(), 35);
        final DefaultTableModel model = new DefaultTableModel(null, new Object[]{
            "Skor", "Nama Referensi", "Code", "Display", "Area", "System"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        final JTable table = new JTable(model);
        final List<ReferenceCandidate> tampil = new ArrayList<>();
        final LocalRadiologyReference[] hasil = new LocalRadiologyReference[1];

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(1).setPreferredWidth(320);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(260);
        table.getColumnModel().getColumn(4).setPreferredWidth(240);
        table.getColumnModel().getColumn(5).setPreferredWidth(220);

        JButton btnPilih = new JButton("Pilih");
        JButton btnBatal = new JButton("Batal");
        btnPilih.setEnabled(false);

        Runnable refresh = () -> {
            String keyword = normalizeText(txtCari.getText());
            while (model.getRowCount() > 0) {
                model.removeRow(0);
            }
            tampil.clear();

            for (ReferenceCandidate item : kandidat) {
                if (!keyword.isEmpty() && !item.searchable.contains(keyword)) {
                    continue;
                }
                tampil.add(item);
                model.addRow(new Object[]{
                    item.score,
                    item.reference.namaPemeriksaan,
                    item.reference.code,
                    item.reference.display,
                    item.areaDisplay,
                    item.reference.codeSystem
                });
                if (tampil.size() >= 300) {
                    break;
                }
            }

            if (!tampil.isEmpty()) {
                table.setRowSelectionInterval(0, 0);
            }
            btnPilih.setEnabled(!tampil.isEmpty());
        };

        txtCari.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                refresh.run();
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnPilih.setEnabled(table.getSelectedRow() >= 0);
            }
        });

        btnPilih.addActionListener(evt -> {
            int row = table.getSelectedRow();
            if (row >= 0 && row < tampil.size()) {
                hasil[0] = tampil.get(row).reference;
                dialog.dispose();
            }
        });
        btnBatal.addActionListener(evt -> dialog.dispose());

        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    btnPilih.doClick();
                }
            }
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        top.add(new JLabel("Cari Referensi :"));
        top.add(txtCari);
        top.add(new JLabel("Sumber: " + referensiCsvFile.getName()));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        bottom.add(btnPilih);
        bottom.add(btnBatal);

        dialog.setLayout(new BorderLayout(6, 6));
        dialog.add(top, BorderLayout.NORTH);
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setSize(1220, 560);
        dialog.setLocationRelativeTo(this);

        refresh.run();
        dialog.setVisible(true);
        return hasil[0];
    }

    private AreaSuggestion applyReferenceChoice(LocalRadiologyReference ref, boolean overwrite) {
        applyText(TxtCode, ref.code, overwrite);
        applyText(TxtSystem, ref.codeSystem, overwrite);
        applyText(TxtDisplay, ref.display, overwrite);
        AreaSuggestion area = inferAreaSuggestion(ref);
        if (area != null) {
            applyText(TxtAreaCode, area.code, overwrite);
            applyText(TxtAreaSystem, area.system, overwrite);
            applyText(TxtAreaDisplay, area.display, overwrite);
        }
        return area;
    }

    private boolean saranArea(boolean overwrite, boolean showMessage) {
        LocalRadiologyReference best = cariReferensiCsvTerbaik();
        AreaSuggestion area = inferAreaSuggestion(best);
        if (area != null && (!area.display.isEmpty() || !area.code.isEmpty() || !area.system.isEmpty())) {
            applyText(TxtAreaCode, area.code, overwrite);
            applyText(TxtAreaSystem, area.system, overwrite);
            applyText(TxtAreaDisplay, area.display, overwrite);
            if (showMessage) {
                String suffix = area.code.isEmpty() ? "\nKode area belum pasti, silakan cek sebelum simpan." : "";
                JOptionPane.showMessageDialog(this, "Saran area: " + area.display + suffix);
            }
            return true;
        }

        if (showMessage) {
            JOptionPane.showMessageDialog(this,
                    "Belum ada saran area otomatis untuk pemeriksaan ini. Silakan isi manual atau gunakan Referensi CSV.");
        }
        return false;
    }

    private AreaSuggestion inferAreaSuggestion(LocalRadiologyReference best) {
        if (best == null) {
            return null;
        }

        if (!best.bodySiteDisplay.trim().isEmpty() || !best.bodySiteCode.trim().isEmpty()) {
            return new AreaSuggestion(best.bodySiteCode, best.bodySiteCodeSystem, best.bodySiteDisplay);
        }

        AreaSuggestion inferred = inferAreaDariAnatomi(best);
        if (inferred != null && (!inferred.display.isEmpty() || !inferred.code.isEmpty())) {
            return inferred;
        }

        return cariAreaDariReferensi(best);
    }

    private AreaSuggestion cariAreaDariReferensi(LocalRadiologyReference target) {
        List<LocalRadiologyReference> refs = loadReferensiCsv();
        String source = normalizeText(
                target.namaPemeriksaan + " " + target.anatomyPath + " " + TxtKodePemeriksaan.getText() + " " + TxtNamaPemeriksaan.getText());
        LocalRadiologyReference best = null;
        int bestScore = Integer.MIN_VALUE;

        for (LocalRadiologyReference ref : refs) {
            if (ref.bodySiteCode.trim().isEmpty() && ref.bodySiteDisplay.trim().isEmpty()) {
                continue;
            }

            int score = 0;
            if (!target.anatomyNorm.isEmpty() && ref.anatomyNorm.equals(target.anatomyNorm)) {
                score += 220;
            }
            if (!target.anatomyNorm.isEmpty() && !ref.anatomyNorm.isEmpty()) {
                if (ref.anatomyNorm.contains(target.anatomyNorm) || target.anatomyNorm.contains(ref.anatomyNorm)) {
                    score += 80;
                }
                score += skorToken(target.anatomyNorm, ref.anatomyNorm) * 18;
            }
            if (!target.namaNorm.isEmpty()) {
                if (ref.namaNorm.equals(target.namaNorm)) {
                    score += 120;
                }
                if (ref.namaNorm.contains(target.namaNorm) || target.namaNorm.contains(ref.namaNorm)) {
                    score += 35;
                }
                score += skorToken(target.namaNorm, ref.namaNorm) * 10;
            }
            score += skorToken(source, ref.namaNorm) * 7;
            score += skorToken(source, ref.anatomyNorm) * 9;
            score += skorToken(source, normalizeText(ref.bodySiteDisplay)) * 6;

            if (ref.categoryNorm.contains("radiologi")) {
                score += 5;
            }

            if (best == null || score > bestScore) {
                best = ref;
                bestScore = score;
            }
        }

        if (best != null && bestScore >= 60) {
            return new AreaSuggestion(best.bodySiteCode, best.bodySiteCodeSystem, best.bodySiteDisplay);
        }
        return null;
    }

    private AreaSuggestion inferAreaDariAnatomi(LocalRadiologyReference best) {
        String source = normalizeText(best.anatomyPath + " " + best.namaPemeriksaan + " "
                + TxtKodePemeriksaan.getText() + " " + TxtNamaPemeriksaan.getText());
        if (source.isEmpty()) {
            return null;
        }

        if (containsAny(source, "lumbosacral", "lumbosakral") ||
                (source.contains("spine lumbar") && source.contains("sacrum"))) {
            return new AreaSuggestion("297173004", "http://snomed.info/sct", "Lumbo-sacral spine");
        }
        if (containsAny(source, "thoracolumbar", "thorakolumbal") ||
                (source.contains("spine thoracic") && source.contains("spine lumbar"))) {
            return new AreaSuggestion("297172009", "http://snomed.info/sct", "Thoraco-lumbar spine");
        }
        if (containsAny(source, "abdomen atas", "upper abdomen")) {
            return new AreaSuggestion("80581009", "http://snomed.info/sct", "Upper abdomen structure");
        }
        if (containsAny(source, "abdomen bawah", "lower abdomen")) {
            return new AreaSuggestion("27033000", "http://snomed.info/sct", "Lower abdomen structure");
        }
        if (containsAny(source, "mastoid", "stenvers")) {
            if (containsAny(source, " kanan ", " dextra ", " right ")) {
                return new AreaSuggestion("763872005", "http://snomed.info/sct", "Structure of right mastoid bone");
            }
            if (containsAny(source, " kiri ", " sinistra ", " left ")) {
                return new AreaSuggestion("763871003", "http://snomed.info/sct", "Structure of left mastoid bone");
            }
            return new AreaSuggestion("59066005", "http://snomed.info/sct", "Mastoid structure");
        }
        if (containsAny(source, "scapula", "skapula")) {
            if (containsAny(source, " kanan ", " dextra ", " right ")) {
                return new AreaSuggestion("719628000", "http://snomed.info/sct", "Bone structure of right scapula");
            }
            if (containsAny(source, " kiri ", " sinistra ", " left ")) {
                return new AreaSuggestion("719627005", "http://snomed.info/sct", "Bone structure of left scapula");
            }
            return new AreaSuggestion("302517007", "http://snomed.info/sct", "Entire scapula");
        }
        if (containsAny(source, "ankle", "mata kaki", "calcaneus", "pedis", "foot")) {
            if (containsAny(source, " bilateral ", " r l ", " r+l ", " kanan kiri ")) {
                return new AreaSuggestion("729790008", "http://snomed.info/sct", "Entire ankle joint cavity");
            }
            if (containsAny(source, "pedis", "foot", "calcaneus")) {
                return new AreaSuggestion("731298009", "http://snomed.info/sct", "Entire ankle and foot");
            }
            return new AreaSuggestion("344001", "http://snomed.info/sct", "Ankle region structure");
        }
        if (containsAny(source, "mammae", "breast", "payudara")) {
            return new AreaSuggestion("76752008", "http://snomed.info/sct", "Breast structure");
        }
        if (containsAny(source, "prostat", "prostate")) {
            return new AreaSuggestion("41216001", "http://snomed.info/sct", "Prostatic structure");
        }
        if (containsAny(source, "ginjal bilateral", "renal", "both kidneys")) {
            return new AreaSuggestion("17373004", "http://snomed.info/sct", "Both kidneys");
        }
        if (containsAny(source, "thorax", "thoraks", "chest", "pulmo")) {
            return new AreaSuggestion("302551006", "http://snomed.info/sct", "Entire thorax");
        }

        String display = buildAreaDisplayFromPath(best.anatomyPath, source);
        if (!display.isEmpty()) {
            return new AreaSuggestion("", "http://snomed.info/sct", display);
        }
        return null;
    }

    private String buildAreaDisplayFromPath(String anatomyPath, String fallbackSource) {
        String source = anatomyPath == null ? "" : anatomyPath.trim();
        if (source.isEmpty()) {
            source = fallbackSource == null ? "" : fallbackSource;
        }
        String lower = source.toLowerCase(Locale.ENGLISH);

        if (lower.contains("spine.cervical") || lower.contains("cervical")) {
            return "Cervical spine structure";
        }
        if (lower.contains("spine.thoracic") || lower.contains("thoracic") || lower.contains("thoracal")) {
            return "Thoracic spine structure";
        }
        if (lower.contains("spine.lumbar") || lower.contains("lumbar") || lower.contains("lumbal") || lower.contains("lumbosacral")) {
            return "Lumbar spine structure";
        }
        if (lower.contains("sacrum")) {
            return "Sacral structure";
        }
        if (lower.contains("elbow") || lower.contains("cubiti")) {
            return "Elbow structure";
        }
        if (lower.contains("clav")) {
            return "Clavicle structure";
        }
        if (lower.contains("humerus")) {
            return "Humerus structure";
        }
        if (lower.contains("wrist") || lower.contains("pergelangan tangan")) {
            return "Wrist structure";
        }
        if (lower.contains("hand") || lower.contains("manus") || lower.contains("tangan")) {
            return "Hand structure";
        }
        if (lower.contains("finger") || lower.contains("digiti") || lower.contains("jari")) {
            return "Finger structure";
        }
        if (lower.contains("knee") || lower.contains("genu")) {
            return "Knee structure";
        }
        if (lower.contains("hip") || lower.contains("coxae")) {
            return "Hip structure";
        }
        if (lower.contains("femur") || lower.contains("thigh")) {
            return "Thigh structure";
        }
        if (lower.contains("whole body") || lower.contains("skeletal system")) {
            return "Skeletal system structure";
        }

        String cleaned = source.replace('^', ' ')
                .replace('>', ' ')
                .replace('.', ' ')
                .replace('&', ' ')
                .replace('+', ' ')
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        return cleaned;
    }

    private boolean containsAny(String source, String... terms) {
        if (source == null || source.isEmpty()) {
            return false;
        }
        String padded = " " + source + " ";
        for (String term : terms) {
            if (term == null || term.isEmpty()) {
                continue;
            }
            String token = term.toLowerCase(Locale.ENGLISH);
            if (padded.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private LocalRadiologyReference cariReferensiCsvTerbaik() {
        List<ReferenceCandidate> kandidat = cariKandidatReferensiCsv();
        if (kandidat.isEmpty()) {
            return null;
        }
        return kandidat.get(0).score >= 35 ? kandidat.get(0).reference : null;
    }

    private List<ReferenceCandidate> cariKandidatReferensiCsv() {
        List<LocalRadiologyReference> refs = loadReferensiCsv();
        List<ReferenceCandidate> kandidat = new ArrayList<>();
        if (refs.isEmpty()) {
            return kandidat;
        }

        String namaPeriksa = normalizeText(TxtNamaPemeriksaan.getText());
        String gabung = normalizeText(TxtKodePemeriksaan.getText() + " " + TxtNamaPemeriksaan.getText());
        boolean thoraxApPa = containsAny(gabung, " thorax ap pa ", " xr thorax ap pa ", " chest ap pa ");
        String expectedModality = inferExpectedModality(namaPeriksa, gabung);

        for (LocalRadiologyReference ref : refs) {
            int score = hitungSkorReferensi(ref, namaPeriksa, gabung, thoraxApPa, expectedModality);
            AreaSuggestion area = inferAreaSuggestion(ref);
            String areaDisplay = area == null ? "" : area.display;
            String areaCode = area == null ? "" : area.code;
            String searchable = normalizeText(
                    ref.namaPemeriksaan + " " + ref.code + " " + ref.display + " " +
                    ref.anatomyPath + " " + areaDisplay + " " + areaCode);
            kandidat.add(new ReferenceCandidate(ref, score, areaDisplay, searchable));
        }

        Collections.sort(kandidat, Comparator
                .comparingInt((ReferenceCandidate item) -> item.score).reversed()
                .thenComparing(item -> item.reference.namaPemeriksaan));

        boolean adaCocok = !kandidat.isEmpty() && kandidat.get(0).score > 0;
        if (adaCocok) {
            List<ReferenceCandidate> filtered = new ArrayList<>();
            for (ReferenceCandidate item : kandidat) {
                if (item.score > 0) {
                    filtered.add(item);
                }
                if (filtered.size() >= 300) {
                    break;
                }
            }
            return filtered;
        }

        return kandidat;
    }

    private int hitungSkorReferensi(LocalRadiologyReference ref, String namaPeriksa, String gabung,
                                    boolean thoraxApPa, String expectedModality) {
        int score = 0;
        if (!namaPeriksa.isEmpty() && ref.namaNorm.equals(namaPeriksa)) {
            score += 180;
        }
        if (!namaPeriksa.isEmpty() && ref.namaNorm.contains(namaPeriksa)) {
            score += 95;
        }
        if (!namaPeriksa.isEmpty() && namaPeriksa.contains(ref.namaNorm)) {
            score += 90;
        }
        if (!gabung.isEmpty() && ref.namaNorm.contains(gabung)) {
            score += 45;
        }
        if (!gabung.isEmpty() && gabung.contains(ref.namaNorm)) {
            score += 35;
        }
        score += skorToken(namaPeriksa, ref.namaNorm) * 8;
        score += skorToken(gabung, ref.namaNorm) * 4;

        if (ref.requestResultNorm.contains("permintaan hasil")) {
            score += 30;
        } else if (ref.requestResultNorm.contains("permintaan")) {
            score += 25;
        } else if (ref.requestResultNorm.contains("hasil")) {
            score -= 10;
        }

        if (ref.categoryNorm.contains("radiologi")) {
            score += 10;
        }

        if (!expectedModality.isEmpty()) {
            String refModality = inferReferenceModality(ref);
            if (expectedModality.equals(refModality)) {
                score += 220;
            } else if (!refModality.isEmpty()) {
                score -= 160;
            }
        }

        if (containsAny(gabung, " ap lat ", " ap lateral ", " ap dan lateral ", " ap lat")) {
            if (containsAny(ref.namaNorm, " ap dan lateral ", " ap lateral ") ||
                    containsAny(normalizeText(ref.display), " ap and lateral ")) {
                score += 180;
            }
            if (containsAny(normalizeText(ref.display), " ge 2 views ", " 2 views ")) {
                score += 60;
            }
        }

        if (thoraxApPa) {
            if (ref.namaNorm.contains("xr thorax satu proyeksi") || ref.display.toLowerCase(Locale.ENGLISH).contains("single view")) {
                score += 260;
            }
            if (ref.namaNorm.contains("xr thorax pa") || ref.display.toLowerCase(Locale.ENGLISH).contains("chest pa")) {
                score += 180;
            }
            if (ref.namaNorm.contains("xr thorax ap") || ref.display.toLowerCase(Locale.ENGLISH).contains("chest ap")) {
                score += 180;
            }
            if (ref.anatomyNorm.contains("chest")) {
                score += 40;
            }
        }

        return score;
    }

    private String inferExpectedModality(String namaPeriksa, String gabung) {
        String source = (gabung + " " + namaPeriksa).trim();
        if (source.isEmpty()) {
            return "";
        }
        if (containsAny(source, " ct ", " ctscan ", " ct scan ", " cat scan ")) {
            return "CT";
        }
        if (containsAny(source, " mr ", " mri ", " magnetic resonance ")) {
            return "MR";
        }
        if (containsAny(source, " usg ", " us ", " ultrasound ", " doppler ")) {
            return "US";
        }
        if (containsAny(source, " rf ", " fluoroscopy ", " mielografi ", " arthrogram ")) {
            return "RF";
        }
        if (containsAny(source, " xr ", " xray ", " x ray ", " foto ", " ap ", " pa ", " lat ", " lateral ", " oblique ", " skyline ", " mortise ", " waters ", " rosenberg ")) {
            return "XR";
        }
        return "";
    }

    private String inferReferenceModality(LocalRadiologyReference ref) {
        String displayUpper = ref.display == null ? "" : ref.display.trim().toUpperCase(Locale.ENGLISH);
        if (displayUpper.startsWith("XR ")) {
            return "XR";
        }
        if (displayUpper.startsWith("MR ")) {
            return "MR";
        }
        if (displayUpper.startsWith("CT ")) {
            return "CT";
        }
        if (displayUpper.startsWith("US ")) {
            return "US";
        }
        if (displayUpper.startsWith("RF ")) {
            return "RF";
        }
        return "";
    }

    private List<LocalRadiologyReference> loadReferensiCsv() {
        if (referensiCsvCache != null) {
            return referensiCsvCache;
        }

        List<LocalRadiologyReference> refs = new ArrayList<>();
        if (!referensiCsvFile.exists()) {
            referensiCsvCache = refs;
            return refs;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(referensiCsvFile), StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                List<String> cols = parseCsvLine(line);
                if (cols.size() < 17) {
                    continue;
                }

                String nama = safeCsvValue(cols, 2);
                String code = safeCsvValue(cols, 4);
                String display = safeCsvValue(cols, 5);
                String codeSystem = safeCsvValue(cols, 13);
                if (nama.isEmpty() || code.isEmpty() || display.isEmpty() || codeSystem.isEmpty()) {
                    continue;
                }

                refs.add(new LocalRadiologyReference(
                        safeCsvValue(cols, 1),
                        nama,
                        safeCsvValue(cols, 3),
                        code,
                        display,
                        safeCsvValue(cols, 9),
                        codeSystem,
                        safeCsvValue(cols, 14),
                        safeCsvValue(cols, 15),
                        safeCsvValue(cols, 16)
                ));
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }

        referensiCsvCache = refs;
        return refs;
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());
        return result;
    }

    private String safeCsvValue(List<String> cols, int index) {
        if (index < 0 || index >= cols.size()) {
            return "";
        }
        String value = cols.get(index);
        if (value == null) {
            return "";
        }
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            value = value.substring(1, value.length() - 1);
        }
        return value.replace("\"\"", "\"").trim();
    }

    private void applyText(JTextField field, String value, boolean overwrite) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        if (overwrite || field.getText().trim().isEmpty()) {
            field.setText(value.trim());
        }
    }

    private int skorToken(String left, String right) {
        if (left.isEmpty() || right.isEmpty()) {
            return 0;
        }
        String[] parts = left.split(" ");
        int score = 0;
        for (String part : parts) {
            if (part.length() < 3) {
                continue;
            }
            if (right.contains(part)) {
                score++;
            }
        }
        return score;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public JTable getTable() {
        return tbData;
    }

    private static final class LocalRadiologyReference {
        private final String category;
        private final String namaPemeriksaan;
        private final String requestResult;
        private final String code;
        private final String display;
        private final String anatomyPath;
        private final String codeSystem;
        private final String bodySiteCode;
        private final String bodySiteDisplay;
        private final String bodySiteCodeSystem;
        private final String categoryNorm;
        private final String namaNorm;
        private final String requestResultNorm;
        private final String anatomyNorm;

        private LocalRadiologyReference(String category, String namaPemeriksaan, String requestResult,
                                        String code, String display, String anatomyPath, String codeSystem,
                                        String bodySiteCode, String bodySiteDisplay, String bodySiteCodeSystem) {
            this.category = category == null ? "" : category;
            this.namaPemeriksaan = namaPemeriksaan == null ? "" : namaPemeriksaan;
            this.requestResult = requestResult == null ? "" : requestResult;
            this.code = code == null ? "" : code;
            this.display = display == null ? "" : display;
            this.anatomyPath = anatomyPath == null ? "" : anatomyPath;
            this.codeSystem = codeSystem == null ? "" : codeSystem;
            this.bodySiteCode = bodySiteCode == null ? "" : bodySiteCode;
            this.bodySiteDisplay = bodySiteDisplay == null ? "" : bodySiteDisplay;
            this.bodySiteCodeSystem = bodySiteCodeSystem == null ? "" : bodySiteCodeSystem;
            this.categoryNorm = normalizeStatic(this.category);
            this.namaNorm = normalizeStatic(this.namaPemeriksaan);
            this.requestResultNorm = normalizeStatic(this.requestResult);
            this.anatomyNorm = normalizeStatic(this.anatomyPath);
        }

        private static String normalizeStatic(String value) {
            if (value == null) {
                return "";
            }
            return value.toLowerCase(Locale.ENGLISH)
                    .replaceAll("[^a-z0-9]+", " ")
                    .replaceAll("\\s+", " ")
                    .trim();
        }
    }

    private static final class ReferenceCandidate {
        private final LocalRadiologyReference reference;
        private final int score;
        private final String areaDisplay;
        private final String searchable;

        private ReferenceCandidate(LocalRadiologyReference reference, int score, String areaDisplay, String searchable) {
            this.reference = reference;
            this.score = score;
            this.areaDisplay = areaDisplay == null ? "" : areaDisplay;
            this.searchable = searchable == null ? "" : searchable;
        }
    }

    private static final class AreaSuggestion {
        private final String code;
        private final String system;
        private final String display;

        private AreaSuggestion(String code, String system, String display) {
            this.code = code == null ? "" : code.trim();
            this.system = system == null ? "" : system.trim();
            this.display = display == null ? "" : display.trim();
        }
    }
}
