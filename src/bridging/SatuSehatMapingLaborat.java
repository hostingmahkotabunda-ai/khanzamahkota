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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.nio.charset.StandardCharsets;
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
import net.sf.jasperreports.engine.JasperCompileManager;

public final class SatuSehatMapingLaborat extends JDialog {
    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final DlgCariTemplateLaborat dlgTemplate;
    private final DefaultTableModel tabMode;
    private final JTable tbData = new JTable();
    private final JTextField TCari = new JTextField(35);
    private final JTextField TxtIdTemplate = new JTextField(18);
    private final JTextField TxtJenisPeriksa = new JTextField(35);
    private final JTextField TxtTemplate = new JTextField(35);
    private final JTextField TxtCode = new JTextField(20);
    private final JTextField TxtSystem = new JTextField(35);
    private final JTextField TxtDisplay = new JTextField(35);
    private final JTextField TxtSampelCode = new JTextField(20);
    private final JTextField TxtSampelSystem = new JTextField(35);
    private final JTextField TxtSampelDisplay = new JTextField(35);
    private final JButton BtnCariTemplate = new JButton("Cari Template");
    private final JButton BtnReferensiCsv = new JButton("Referensi CSV");
    private final JButton BtnAmbilSerupa = new JButton("Ambil Serupa");
    private final JButton BtnSaranSampel = new JButton("Saran Sampel");
    private final JButton BtnSimpan = new JButton("Simpan");
    private final JButton BtnEdit = new JButton("Ganti");
    private final JButton BtnHapus = new JButton("Hapus");
    private final JButton BtnBatal = new JButton("Bersihkan");
    private final JButton BtnPrint = new JButton("Cetak");
    private final JButton BtnKeluar = new JButton("Keluar");
    private final File reportJasperFile = new File("report/rptMapingPemeriksaanLaboratSatuSehat.jasper");
    private final File reportJrxmlFile = new File("report/rptMapingPemeriksaanLaboratSatuSehat.jrxml");
    private final File referensiCsvFile = new File("setting/referensi_satu_sehat/Terminologi_LOINC_Laboratorium_v1_1_gid1190079850.csv");
    private List<LocalLabReference> referensiCsvCache = null;
    private String selectedKdJenisPrw = "";

    public SatuSehatMapingLaborat(Frame parent, boolean modal) {
        super(parent, modal);
        dlgTemplate = new DlgCariTemplateLaborat(this, true);
        tabMode = new DefaultTableModel(null, new Object[]{
            "ID Template", "Jenis Pemeriksaan", "Detail Pemeriksaan",
            "Code", "System", "Display", "Sampel Code", "Sampel System", "Sampel Display"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        ensureSupportTables();
        initComponents();
        tampil();
    }

    private void initComponents() {
        setTitle("::[ Data Mapping Pemeriksaan Laboratorium Satu Sehat ]::");
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
        tbData.getColumnModel().getColumn(0).setPreferredWidth(85);
        tbData.getColumnModel().getColumn(1).setPreferredWidth(260);
        tbData.getColumnModel().getColumn(2).setPreferredWidth(240);
        tbData.getColumnModel().getColumn(3).setPreferredWidth(90);
        tbData.getColumnModel().getColumn(4).setPreferredWidth(220);
        tbData.getColumnModel().getColumn(5).setPreferredWidth(220);
        tbData.getColumnModel().getColumn(6).setPreferredWidth(95);
        tbData.getColumnModel().getColumn(7).setPreferredWidth(220);
        tbData.getColumnModel().getColumn(8).setPreferredWidth(220);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        TxtIdTemplate.setEditable(false);
        TxtJenisPeriksa.setEditable(false);
        TxtTemplate.setEditable(false);

        int row = 0;
        addFormRow(form, gbc, row++, "ID Template", TxtIdTemplate, BtnCariTemplate);
        addFormRow(form, gbc, row++, "Jenis Pemeriksaan", TxtJenisPeriksa, null);
        addFormRow(form, gbc, row++, "Detail Pemeriksaan", TxtTemplate, null);
        addFormRow(form, gbc, row++, "Code", TxtCode, BtnAmbilSerupa);
        addFormRow(form, gbc, row++, "System", TxtSystem, null);
        addFormRow(form, gbc, row++, "Display", TxtDisplay, null);
        addFormRow(form, gbc, row++, "Sampel Code", TxtSampelCode, BtnSaranSampel);
        addFormRow(form, gbc, row++, "Sampel System", TxtSampelSystem, null);
        addFormRow(form, gbc, row++, "Sampel Display", TxtSampelDisplay, null);

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
        split.setResizeWeight(0.62);
        add(split, BorderLayout.CENTER);

        btnTampil.addActionListener(evt -> tampil());
        BtnCariTemplate.addActionListener(evt -> pilihTemplate());
        BtnReferensiCsv.addActionListener(evt -> ambilReferensiCsv(true, true));
        BtnAmbilSerupa.addActionListener(evt -> ambilSerupa(true, true));
        BtnSaranSampel.addActionListener(evt -> saranSampel(true, true));
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

        setPreferredSize(new Dimension(1200, 650));
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
                "create table if not exists satu_sehat_mapping_lab (" +
                "id_template int(11) not null," +
                "code varchar(50) default null," +
                "system varchar(255) default null," +
                "display varchar(500) default null," +
                "sampel_code varchar(50) default null," +
                "sampel_system varchar(255) default null," +
                "sampel_display varchar(500) default null," +
                "primary key (id_template)," +
                "constraint fk_satu_sehat_mapping_lab_template foreign key (id_template) " +
                "references template_laboratorium(id_template) on delete cascade on update cascade" +
                ") engine=InnoDB default charset=latin1 collate=latin1_swedish_ci"
            );
            ps.executeUpdate();
            ps.close();

            ps = koneksi.prepareStatement("alter table satu_sehat_mapping_lab modify id_template int(11) not null");
            ps.executeUpdate();
            ps.close();

            ps = koneksi.prepareStatement("alter table satu_sehat_mapping_lab modify code varchar(50) null");
            ps.executeUpdate();
            ps.close();

            ps = koneksi.prepareStatement("alter table satu_sehat_mapping_lab modify system varchar(255) null");
            ps.executeUpdate();
            ps.close();

            ps = koneksi.prepareStatement("alter table satu_sehat_mapping_lab modify display varchar(500) null");
            ps.executeUpdate();
            ps.close();

            ps = koneksi.prepareStatement("alter table satu_sehat_mapping_lab modify sampel_code varchar(50) null");
            ps.executeUpdate();
            ps.close();

            ps = koneksi.prepareStatement("alter table satu_sehat_mapping_lab modify sampel_system varchar(255) null");
            ps.executeUpdate();
            ps.close();

            ps = koneksi.prepareStatement("alter table satu_sehat_mapping_lab modify sampel_display varchar(500) null");
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
                "select tl.id_template,concat(tl.kd_jenis_prw,' - ',jpl.nm_perawatan) as jenis_pemeriksaan," +
                "tl.Pemeriksaan,ifnull(map.code,'') as code,ifnull(map.system,'') as system," +
                "ifnull(map.display,'') as display,ifnull(map.sampel_code,'') as sampel_code," +
                "ifnull(map.sampel_system,'') as sampel_system,ifnull(map.sampel_display,'') as sampel_display " +
                "from template_laboratorium tl " +
                "inner join jns_perawatan_lab jpl on jpl.kd_jenis_prw=tl.kd_jenis_prw " +
                "left join satu_sehat_mapping_lab map on map.id_template=tl.id_template " +
                "where (" +
                "tl.id_template like ? or tl.Pemeriksaan like ? or jpl.nm_perawatan like ? or " +
                "ifnull(map.code,'') like ? or ifnull(map.display,'') like ? or ifnull(map.sampel_display,'') like ?" +
                ") order by jpl.kategori,jpl.nm_perawatan,tl.urut,tl.id_template")) {
            String cari = "%" + TCari.getText().trim() + "%";
            for (int i = 1; i <= 6; i++) {
                ps.setString(i, cari);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (tabMode.getRowCount() > 0) {
                    tabMode.removeRow(0);
                }
                while (rs.next()) {
                    tabMode.addRow(new Object[]{
                        rs.getString("id_template"),
                        rs.getString("jenis_pemeriksaan"),
                        rs.getString("Pemeriksaan"),
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

    private void pilihTemplate() {
        String[] hasil = dlgTemplate.pilihTemplate();
        if (hasil != null) {
            selectedKdJenisPrw = hasil[0];
            TxtJenisPeriksa.setText(hasil[0] + " - " + hasil[1]);
            TxtIdTemplate.setText(hasil[2]);
            TxtTemplate.setText(hasil[3] + (hasil[4].trim().isEmpty() ? "" : " (" + hasil[4] + ")"));
            loadMappingById(hasil[2]);
            terapkanSaranOtomatis();
        }
        refreshActionState();
    }

    private void loadMappingById(String idTemplate) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select code,system,display,sampel_code,sampel_system,sampel_display from satu_sehat_mapping_lab where id_template=?")) {
            ps.setString(1, idTemplate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TxtCode.setText(rs.getString("code"));
                    TxtSystem.setText(rs.getString("system"));
                    TxtDisplay.setText(rs.getString("display"));
                    TxtSampelCode.setText(rs.getString("sampel_code"));
                    TxtSampelSystem.setText(rs.getString("sampel_system"));
                    TxtSampelDisplay.setText(rs.getString("sampel_display"));
                } else {
                    TxtCode.setText("");
                    TxtSystem.setText("");
                    TxtDisplay.setText("");
                    TxtSampelCode.setText("");
                    TxtSampelSystem.setText("");
                    TxtSampelDisplay.setText("");
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
        if (mappingSudahAda(TxtIdTemplate.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Mapping untuk template ini sudah ada. Gunakan tombol Ganti.");
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into satu_sehat_mapping_lab(id_template,code,system,display,sampel_code,sampel_system,sampel_display) values(?,?,?,?,?,?,?)")) {
            bindMapping(ps);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Mapping laboratorium berhasil disimpan.");
            tampil();
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
            JOptionPane.showMessageDialog(this, "Gagal menyimpan mapping laboratorium.");
        }
    }

    private void edit() {
        if (!validInput()) {
            return;
        }
        if (!mappingSudahAda(TxtIdTemplate.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Mapping belum ada. Gunakan tombol Simpan.");
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "update satu_sehat_mapping_lab set code=?,system=?,display=?,sampel_code=?,sampel_system=?,sampel_display=? where id_template=?")) {
            ps.setString(1, TxtCode.getText().trim());
            ps.setString(2, TxtSystem.getText().trim());
            ps.setString(3, TxtDisplay.getText().trim());
            ps.setString(4, TxtSampelCode.getText().trim());
            ps.setString(5, TxtSampelSystem.getText().trim());
            ps.setString(6, TxtSampelDisplay.getText().trim());
            ps.setString(7, TxtIdTemplate.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Mapping laboratorium berhasil diperbarui.");
            tampil();
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
            JOptionPane.showMessageDialog(this, "Gagal memperbarui mapping laboratorium.");
        }
    }

    private void hapus() {
        if (TxtIdTemplate.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih template laboratorium terlebih dahulu.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus mapping laboratorium ini?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement("delete from satu_sehat_mapping_lab where id_template=?")) {
            ps.setString(1, TxtIdTemplate.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Mapping laboratorium berhasil dihapus.");
            bersih();
            tampil();
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
            JOptionPane.showMessageDialog(this, "Gagal menghapus mapping laboratorium.");
        }
    }

    private void cetak() {
        if (tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Belum ada data yang bisa dicetak.");
            return;
        }
        if (!ensureReportFile()) {
            JOptionPane.showMessageDialog(this, "Report mapping laboratorium tidak tersedia atau gagal dikompilasi.");
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
            Valid.MyReport(reportJasperFile.getName(), "report", "::[ Mapping Pemeriksaan Laboratorium Satu Sehat ]::", param);
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void bersih() {
        selectedKdJenisPrw = "";
        TxtIdTemplate.setText("");
        TxtJenisPeriksa.setText("");
        TxtTemplate.setText("");
        TxtCode.setText("");
        TxtSystem.setText("");
        TxtDisplay.setText("");
        TxtSampelCode.setText("");
        TxtSampelSystem.setText("");
        TxtSampelDisplay.setText("");
        tbData.clearSelection();
        refreshActionState();
    }

    private void loadSelectedRow() {
        int row = tbData.getSelectedRow();
        if (row < 0) {
            refreshActionState();
            return;
        }
        TxtIdTemplate.setText(tabMode.getValueAt(row, 0).toString());
        TxtJenisPeriksa.setText(tabMode.getValueAt(row, 1).toString());
        TxtTemplate.setText(tabMode.getValueAt(row, 2).toString());
        TxtCode.setText(tabMode.getValueAt(row, 3).toString());
        TxtSystem.setText(tabMode.getValueAt(row, 4).toString());
        TxtDisplay.setText(tabMode.getValueAt(row, 5).toString());
        TxtSampelCode.setText(tabMode.getValueAt(row, 6).toString());
        TxtSampelSystem.setText(tabMode.getValueAt(row, 7).toString());
        TxtSampelDisplay.setText(tabMode.getValueAt(row, 8).toString());
        refreshActionState();
    }

    private boolean validInput() {
        if (TxtIdTemplate.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih template laboratorium terlebih dahulu.");
            BtnCariTemplate.requestFocus();
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
        if (TxtSampelCode.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sampel Code SATUSEHAT belum diisi.");
            TxtSampelCode.requestFocus();
            return false;
        }
        if (TxtSampelSystem.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sampel System SATUSEHAT belum diisi.");
            TxtSampelSystem.requestFocus();
            return false;
        }
        if (TxtSampelDisplay.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sampel Display SATUSEHAT belum diisi.");
            TxtSampelDisplay.requestFocus();
            return false;
        }
        return true;
    }

    private boolean mappingSudahAda(String idTemplate) {
        try (PreparedStatement ps = koneksi.prepareStatement("select count(*) from satu_sehat_mapping_lab where id_template=?")) {
            ps.setString(1, idTemplate);
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
        ps.setString(1, TxtIdTemplate.getText().trim());
        ps.setString(2, TxtCode.getText().trim());
        ps.setString(3, TxtSystem.getText().trim());
        ps.setString(4, TxtDisplay.getText().trim());
        ps.setString(5, TxtSampelCode.getText().trim());
        ps.setString(6, TxtSampelSystem.getText().trim());
        ps.setString(7, TxtSampelDisplay.getText().trim());
    }

    private void refreshActionState() {
        boolean canManage = akses.getsatu_sehat_mapping_lab();
        boolean hasSelection = !TxtIdTemplate.getText().trim().isEmpty();
        boolean hasMapping = hasSelection && mappingSudahAda(TxtIdTemplate.getText().trim());
        boolean reportReady = reportJasperFile.exists() || reportJrxmlFile.exists();
        BtnReferensiCsv.setEnabled(hasSelection && referensiCsvFile.exists());
        BtnAmbilSerupa.setEnabled(hasSelection);
        BtnSaranSampel.setEnabled(hasSelection);
        BtnSimpan.setEnabled(canManage && hasSelection && !hasMapping);
        BtnEdit.setEnabled(canManage && hasMapping);
        BtnHapus.setEnabled(canManage && hasMapping);
        BtnPrint.setEnabled(canManage && tabMode.getRowCount() > 0 && reportReady);
    }

    private boolean ensureReportFile() {
        if (reportJasperFile.exists()) {
            return true;
        }
        if (!reportJrxmlFile.exists()) {
            return false;
        }
        try {
            JasperCompileManager.compileReportToFile(reportJrxmlFile.getPath(), reportJasperFile.getPath());
            return reportJasperFile.exists();
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
            return false;
        }
    }

    private void terapkanSaranOtomatis() {
        boolean csv = ambilReferensiCsv(false, false);
        if (!csv) {
            ambilSerupa(false, false);
        }
        saranSampel(false, false);
    }

    private boolean ambilSerupa(boolean overwrite, boolean showMessage) {
        String idTemplate = TxtIdTemplate.getText().trim();
        String detail = TxtTemplate.getText().trim();
        if (idTemplate.isEmpty() || detail.isEmpty()) {
            if (showMessage) {
                JOptionPane.showMessageDialog(this, "Silakan pilih template laboratorium terlebih dahulu.");
            }
            return false;
        }

        String detailLower = detail.toLowerCase().trim();
        String likeDetail = "%" + detailLower + "%";

        try (PreparedStatement ps = koneksi.prepareStatement(
                "select map.code,map.system,map.display,map.sampel_code,map.sampel_system,map.sampel_display," +
                "tl.Pemeriksaan,tl.kd_jenis_prw," +
                "(case when tl.kd_jenis_prw=? then 60 else 0 end +" +
                " case when lower(tl.Pemeriksaan)=? then 40 else 0 end +" +
                " case when lower(tl.Pemeriksaan) like ? then 20 else 0 end +" +
                " case when ? like concat('%', lower(tl.Pemeriksaan), '%') then 10 else 0 end) as skor " +
                "from satu_sehat_mapping_lab map " +
                "inner join template_laboratorium tl on tl.id_template=map.id_template " +
                "where map.id_template<>? and (" +
                "tl.kd_jenis_prw=? or lower(tl.Pemeriksaan) like ? or ? like concat('%', lower(tl.Pemeriksaan), '%')" +
                ") order by skor desc, case when tl.kd_jenis_prw=? then 1 else 0 end desc, length(tl.Pemeriksaan), tl.id_template limit 1")) {
            ps.setString(1, selectedKdJenisPrw);
            ps.setString(2, detailLower);
            ps.setString(3, likeDetail);
            ps.setString(4, detailLower);
            ps.setString(5, idTemplate);
            ps.setString(6, selectedKdJenisPrw);
            ps.setString(7, likeDetail);
            ps.setString(8, detailLower);
            ps.setString(9, selectedKdJenisPrw);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    applyText(TxtCode, rs.getString("code"), overwrite);
                    applyText(TxtSystem, rs.getString("system"), overwrite);
                    applyText(TxtDisplay, rs.getString("display"), overwrite);
                    applyText(TxtSampelCode, rs.getString("sampel_code"), overwrite);
                    applyText(TxtSampelSystem, rs.getString("sampel_system"), overwrite);
                    applyText(TxtSampelDisplay, rs.getString("sampel_display"), overwrite);
                    if (showMessage) {
                        JOptionPane.showMessageDialog(this,
                                "Saran diambil dari mapping serupa: " + rs.getString("Pemeriksaan"));
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }

        if (showMessage) {
            JOptionPane.showMessageDialog(this, "Belum ada mapping serupa yang bisa dijadikan referensi.");
        }
        return false;
    }

    private boolean ambilReferensiCsv(boolean overwrite, boolean showMessage) {
        String detail = TxtTemplate.getText().trim();
        if (detail.isEmpty()) {
            if (showMessage) {
                JOptionPane.showMessageDialog(this, "Silakan pilih template laboratorium terlebih dahulu.");
            }
            return false;
        }
        if (!referensiCsvFile.exists()) {
            if (showMessage) {
                JOptionPane.showMessageDialog(this, "File referensi CSV belum ditemukan:\n" + referensiCsvFile.getPath());
            }
            return false;
        }

        LocalLabReference best = cariReferensiCsvTerbaik();
        if (best == null) {
            if (showMessage) {
                JOptionPane.showMessageDialog(this, "Belum ada kandidat cocok dari referensi CSV lokal.");
            }
            return false;
        }

        applyText(TxtCode, best.code, overwrite);
        applyText(TxtSystem, best.codeSystem, overwrite);
        applyText(TxtDisplay, best.display, overwrite);

        SpecimenSuggestion suggestion = inferSpecimenSuggestionFromText(best.specimen);
        if (suggestion != null) {
            applyText(TxtSampelCode, suggestion.code, overwrite);
            applyText(TxtSampelSystem, suggestion.system, overwrite);
            applyText(TxtSampelDisplay, suggestion.display, overwrite);
        }

        if (showMessage) {
            JOptionPane.showMessageDialog(this,
                    "Referensi CSV ditemukan:\n" +
                    "Nama Pemeriksaan : " + best.namaPemeriksaan + "\n" +
                    "Code : " + best.code + "\n" +
                    "Display : " + best.display);
        }
        return true;
    }

    private boolean saranSampel(boolean overwrite, boolean showMessage) {
        SpecimenSuggestion suggestion = inferSpecimenSuggestion();
        if (suggestion == null) {
            if (showMessage) {
                JOptionPane.showMessageDialog(this,
                        "Belum ada saran sampel otomatis untuk pemeriksaan ini. Silakan isi manual.");
            }
            return false;
        }

        applyText(TxtSampelCode, suggestion.code, overwrite);
        applyText(TxtSampelSystem, suggestion.system, overwrite);
        applyText(TxtSampelDisplay, suggestion.display, overwrite);

        if (showMessage) {
            JOptionPane.showMessageDialog(this, "Saran sampel: " + suggestion.display);
        }
        return true;
    }

    private LocalLabReference cariReferensiCsvTerbaik() {
        List<LocalLabReference> refs = loadReferensiCsv();
        if (refs.isEmpty()) {
            return null;
        }

        String detail = normalizeText(TxtTemplate.getText());
        String jenis = normalizeText(extractJenisNama());
        String gabung = normalizeText(TxtJenisPeriksa.getText() + " " + TxtTemplate.getText());
        SpecimenSuggestion currentSpecimen = inferSpecimenSuggestion();

        LocalLabReference best = null;
        int bestScore = Integer.MIN_VALUE;
        for (LocalLabReference ref : refs) {
            int score = 0;
            String nama = ref.namaNorm;
            String specimen = ref.specimenNorm;

            if (!detail.isEmpty() && nama.equals(detail)) {
                score += 140;
            }
            if (!jenis.isEmpty() && nama.equals(jenis)) {
                score += 120;
            }
            if (!detail.isEmpty() && nama.contains(detail)) {
                score += 70;
            }
            if (!detail.isEmpty() && detail.contains(nama)) {
                score += 60;
            }
            if (!jenis.isEmpty() && nama.contains(jenis)) {
                score += 55;
            }
            if (!jenis.isEmpty() && jenis.contains(nama)) {
                score += 45;
            }
            if (!gabung.isEmpty() && gabung.contains(nama)) {
                score += 20;
            }
            if (ref.requestResult.contains("&")) {
                score += 18;
            } else if (ref.requestResult.toLowerCase(Locale.ENGLISH).contains("hasil")) {
                score += 10;
            } else if (ref.requestResult.toLowerCase(Locale.ENGLISH).contains("permintaan")) {
                score += 8;
            }
            if (currentSpecimen != null && !specimen.isEmpty()) {
                if (specimen.contains(normalizeText(currentSpecimen.display))) {
                    score += 35;
                } else if (containsAny(specimen, "serum", "plasma") && containsAny(normalizeText(currentSpecimen.display), "serum", "plasma")) {
                    score += 30;
                } else if (containsAny(specimen, "darah", "blood") && containsAny(normalizeText(currentSpecimen.display), "blood", "darah")) {
                    score += 30;
                }
            }
            if (!ref.method.isEmpty()) {
                score -= 3;
            }

            if (score > bestScore) {
                bestScore = score;
                best = ref;
            }
        }

        return bestScore >= 55 ? best : null;
    }

    private List<LocalLabReference> loadReferensiCsv() {
        if (referensiCsvCache != null) {
            return referensiCsvCache;
        }

        List<LocalLabReference> refs = new ArrayList<>();
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
                if (cols.size() < 19) {
                    continue;
                }
                String nama = safeCsvValue(cols, 2);
                String specimen = safeCsvValue(cols, 4);
                String code = safeCsvValue(cols, 8);
                String display = safeCsvValue(cols, 9);
                String codeSystem = safeCsvValue(cols, 17);
                if (nama.isEmpty() || code.isEmpty() || display.isEmpty() || codeSystem.isEmpty()) {
                    continue;
                }
                refs.add(new LocalLabReference(
                        nama,
                        safeCsvValue(cols, 3),
                        specimen,
                        safeCsvValue(cols, 7),
                        code,
                        display,
                        codeSystem
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
        if (index < 0 || index >= cols.size() || cols.get(index) == null) {
            return "";
        }
        return cols.get(index).trim();
    }

    private void applyText(JTextField field, String value, boolean overwrite) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        if (overwrite || field.getText().trim().isEmpty()) {
            field.setText(value.trim());
        }
    }

    private String extractJenisNama() {
        String jenis = TxtJenisPeriksa.getText().trim();
        int idx = jenis.indexOf(" - ");
        if (idx >= 0 && idx + 3 < jenis.length()) {
            return jenis.substring(idx + 3).trim();
        }
        return jenis;
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private SpecimenSuggestion inferSpecimenSuggestion() {
        return inferSpecimenSuggestionFromText(TxtJenisPeriksa.getText() + " " + TxtTemplate.getText());
    }

    private SpecimenSuggestion inferSpecimenSuggestionFromText(String sourceText) {
        String text = normalizeText((sourceText == null ? "" : sourceText).replace("-", " "));

        if (containsAny(text, "urin", "urine")) {
            return new SpecimenSuggestion("122575003", "http://snomed.info/sct", "Urine specimen");
        }
        if (containsAny(text, "sputum", "dahak")) {
            return new SpecimenSuggestion("119334006", "http://snomed.info/sct", "Sputum specimen");
        }
        if (containsAny(text, "feses", "feces", "faeces", "stool")) {
            return new SpecimenSuggestion("119339001", "http://snomed.info/sct", "Stool specimen");
        }
        if (containsAny(text, "csf", "likuor", "liquor", "cerebrospinal")) {
            return new SpecimenSuggestion("258450006", "http://snomed.info/sct", "Cerebrospinal fluid sample (specimen)");
        }
        if (containsAny(text, "plasma")) {
            return new SpecimenSuggestion("119361006", "http://snomed.info/sct", "Plasma specimen");
        }
        if (containsAny(text, "serum", "albumin", "bilirubin", "kolesterol", "trigliserida", "sgot", "sgpt",
                "asam urat", "kreatinin", "ureum", "protein total", "glukosa", "profil lipid")) {
            return new SpecimenSuggestion("119364003", "http://snomed.info/sct", "Serum specimen");
        }
        if (containsAny(text, "darah rutin", "hematologi", "hemoglobin", "leukosit", "trombosit", "eritrosit",
                "hematokrit", "granulosit", "limfosit", "rdw", "mcv", "mch", "mchc", "aptt", "pt ", " inr",
                "d dimer", "d-dimer", "led", "golongan darah")) {
            return new SpecimenSuggestion("119297000", "http://snomed.info/sct", "Blood specimen");
        }
        return null;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static final class SpecimenSuggestion {
        private final String code;
        private final String system;
        private final String display;

        private SpecimenSuggestion(String code, String system, String display) {
            this.code = code;
            this.system = system;
            this.display = display;
        }
    }

    private static final class LocalLabReference {
        private final String namaPemeriksaan;
        private final String requestResult;
        private final String specimen;
        private final String method;
        private final String code;
        private final String display;
        private final String codeSystem;
        private final String namaNorm;
        private final String specimenNorm;

        private LocalLabReference(String namaPemeriksaan, String requestResult, String specimen, String method,
                                  String code, String display, String codeSystem) {
            this.namaPemeriksaan = namaPemeriksaan;
            this.requestResult = requestResult;
            this.specimen = specimen;
            this.method = method == null ? "" : method;
            this.code = code;
            this.display = display;
            this.codeSystem = codeSystem;
            this.namaNorm = namaPemeriksaan == null ? "" : namaPemeriksaan.toLowerCase(Locale.ENGLISH)
                    .replaceAll("[^a-z0-9]+", " ").replaceAll("\\s+", " ").trim();
            this.specimenNorm = specimen == null ? "" : specimen.toLowerCase(Locale.ENGLISH)
                    .replaceAll("[^a-z0-9]+", " ").replaceAll("\\s+", " ").trim();
        }
    }
}
