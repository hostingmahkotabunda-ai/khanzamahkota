package simrskhanza;

import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import widget.Button;
import widget.ComboBox;
import widget.Label;
import widget.TextArea;
import widget.TextBox;
import rekammedis.MasterCariTemplateLaporanOperasi;

public class DlgLaporanOperasiPemeriksaan extends javax.swing.JDialog {
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final TextBox TNoRw = new TextBox();
    private final TextBox TPasien = new TextBox();
    private final TextBox PreOp = new TextBox();
    private final TextBox PostOp = new TextBox();
    private final TextBox Jaringan = new TextBox();
    private final ComboBox DikirimPA = new ComboBox();
    private final TextArea Laporan = new TextArea();
    private final Button BtnSimpan = new Button();
    private final Button BtnTemplate = new Button();
    private final Button BtnKeluar = new Button();
    private final MasterCariTemplateLaporanOperasi template = new MasterCariTemplateLaporanOperasi(null, false);

    public DlgLaporanOperasiPemeriksaan(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initForm();
        initTemplate();
    }

    private void initForm() {
        setTitle("Laporan Operasi");
        setMinimumSize(new Dimension(620, 420));
        setPreferredSize(new Dimension(720, 480));
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        TNoRw.setEditable(false);
        TPasien.setEditable(false);
        PreOp.setDocument(new batasInput((int)100).getKata(PreOp));
        PostOp.setDocument(new batasInput((int)100).getKata(PostOp));
        Jaringan.setDocument(new batasInput((int)100).getKata(Jaringan));
        Laporan.setDocument(new batasInput((int)8000).getKata(Laporan));
        Laporan.setLineWrap(true);
        Laporan.setWrapStyleWord(true);
        DikirimPA.setModel(new DefaultComboBoxModel(new String[] {"Ya", "Tidak"}));

        JPanel panelInput = new JPanel(new GridBagLayout());
        panelInput.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        panelInput.setBackground(new Color(255, 255, 255));

        addField(panelInput, 0, "No.Rawat :", TNoRw);
        addField(panelInput, 1, "Pasien :", TPasien);
        addField(panelInput, 2, "Diagnosa Pre-operatif :", PreOp);
        addField(panelInput, 3, "Diagnosa Post-operatif :", PostOp);
        addField(panelInput, 4, "Jaringan di-Eksisi / -Insisi :", Jaringan);
        addField(panelInput, 5, "Dikirim Pemeriksaan PA :", DikirimPA);
        addTextArea(panelInput, 6, "Laporan Operasi :");

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png")));
        BtnSimpan.setText("Simpan");
        BtnSimpan.setPreferredSize(new Dimension(100, 30));
        BtnSimpan.addActionListener(evt -> simpan());

        BtnTemplate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
        BtnTemplate.setText("Template");
        BtnTemplate.setPreferredSize(new Dimension(110, 30));
        BtnTemplate.addActionListener(evt -> tampilkanTemplate());

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/cross.png")));
        BtnKeluar.setText("Keluar");
        BtnKeluar.setPreferredSize(new Dimension(100, 30));
        BtnKeluar.addActionListener(evt -> dispose());

        JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelButton.setBackground(new Color(250, 250, 250));
        panelButton.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(235, 235, 235)));
        panelButton.add(BtnSimpan);
        panelButton.add(BtnTemplate);
        panelButton.add(BtnKeluar);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panelInput, BorderLayout.CENTER);
        getContentPane().add(panelButton, BorderLayout.SOUTH);
        pack();
    }

    private void initTemplate() {
        template.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(template.getTable().getSelectedRow()!= -1){
                    PreOp.setText(template.getTable().getValueAt(template.getTable().getSelectedRow(),2).toString());
                    PostOp.setText(template.getTable().getValueAt(template.getTable().getSelectedRow(),3).toString());
                    Jaringan.setText(template.getTable().getValueAt(template.getTable().getSelectedRow(),4).toString());
                    DikirimPA.setSelectedItem(template.getTable().getValueAt(template.getTable().getSelectedRow(),5).toString());
                    Laporan.setText(template.getTable().getValueAt(template.getTable().getSelectedRow(),6).toString());
                    Laporan.requestFocus();
                }
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
    }

    private void tampilkanTemplate() {
        template.emptTeks();
        template.isCek();
        template.setSize(getWidth()-20, getHeight()-20);
        template.setLocationRelativeTo(this);
        template.setVisible(true);
    }

    private void addField(JPanel panel, int row, String label, javax.swing.JComponent input) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbcLabel.gridy = row;
        gbcLabel.anchor = GridBagConstraints.EAST;
        gbcLabel.insets = new Insets(4, 4, 4, 8);
        Label lbl = new Label();
        lbl.setText(label);
        panel.add(lbl, gbcLabel);

        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.gridx = 1;
        gbcInput.gridy = row;
        gbcInput.weightx = 1;
        gbcInput.fill = GridBagConstraints.HORIZONTAL;
        gbcInput.insets = new Insets(4, 0, 4, 4);
        panel.add(input, gbcInput);
    }

    private void addTextArea(JPanel panel, int row, String label) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbcLabel.gridy = row;
        gbcLabel.anchor = GridBagConstraints.NORTHEAST;
        gbcLabel.insets = new Insets(4, 4, 4, 8);
        Label lbl = new Label();
        lbl.setText(label);
        panel.add(lbl, gbcLabel);

        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.gridx = 1;
        gbcInput.gridy = row;
        gbcInput.weightx = 1;
        gbcInput.weighty = 1;
        gbcInput.fill = GridBagConstraints.BOTH;
        gbcInput.insets = new Insets(4, 0, 4, 4);
        panel.add(new JScrollPane(Laporan), gbcInput);
    }

    public void setNoRm(String noRawat, String pasien) {
        TNoRw.setText(noRawat);
        TPasien.setText(pasien);
        kosongkan();
        muat();
    }

    private void kosongkan() {
        PreOp.setText("");
        PostOp.setText("");
        Jaringan.setText("");
        DikirimPA.setSelectedItem("Ya");
        Laporan.setText("");
    }

    private void pastikanTabel() {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "create table if not exists draft_laporan_operasi ("+
                "no_rawat varchar(17) not null,"+
                "diagnosa_preop varchar(100) default '',"+
                "diagnosa_postop varchar(100) default '',"+
                "jaringan_dieksekusi varchar(100) default '',"+
                "permintaan_pa varchar(5) default 'Ya',"+
                "laporan_operasi text,"+
                "tanggal_update datetime not null,"+
                "primary key (no_rawat)) engine=InnoDB default charset=latin1")) {
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
    }

    private void muat() {
        pastikanTabel();
        if (muatDraft()) {
            return;
        }
        muatLaporanOperasiTerakhir();
    }

    private boolean muatDraft() {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select diagnosa_preop,diagnosa_postop,jaringan_dieksekusi,permintaan_pa,laporan_operasi "+
                "from draft_laporan_operasi where no_rawat=?")) {
            ps.setString(1, TNoRw.getText());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PreOp.setText(rs.getString("diagnosa_preop"));
                    PostOp.setText(rs.getString("diagnosa_postop"));
                    Jaringan.setText(rs.getString("jaringan_dieksekusi"));
                    DikirimPA.setSelectedItem(rs.getString("permintaan_pa"));
                    Laporan.setText(rs.getString("laporan_operasi"));
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
        return false;
    }

    private void muatLaporanOperasiTerakhir() {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select diagnosa_preop,diagnosa_postop,jaringan_dieksekusi,permintaan_pa,laporan_operasi "+
                "from laporan_operasi where no_rawat=? order by tanggal desc limit 1")) {
            ps.setString(1, TNoRw.getText());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PreOp.setText(rs.getString("diagnosa_preop"));
                    PostOp.setText(rs.getString("diagnosa_postop"));
                    Jaringan.setText(rs.getString("jaringan_dieksekusi"));
                    DikirimPA.setSelectedItem(rs.getString("permintaan_pa"));
                    Laporan.setText(rs.getString("laporan_operasi"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
    }

    private void simpan() {
        if (TNoRw.getText().trim().equals("")) {
            Valid.textKosong(TNoRw, "No.Rawat");
            return;
        }

        pastikanTabel();
        String sql;
        boolean ada = Sequel.cariInteger("select count(no_rawat) from draft_laporan_operasi where no_rawat=?", TNoRw.getText()) > 0;
        if (ada) {
            sql = "update draft_laporan_operasi set diagnosa_preop=?,diagnosa_postop=?,jaringan_dieksekusi=?,"+
                  "permintaan_pa=?,laporan_operasi=?,tanggal_update=now() where no_rawat=?";
        } else {
            sql = "insert into draft_laporan_operasi(diagnosa_preop,diagnosa_postop,jaringan_dieksekusi,"+
                  "permintaan_pa,laporan_operasi,tanggal_update,no_rawat) values(?,?,?,?,?,now(),?)";
        }

        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, PreOp.getText());
            ps.setString(2, PostOp.getText());
            ps.setString(3, Jaringan.getText());
            ps.setString(4, DikirimPA.getSelectedItem().toString());
            ps.setString(5, Laporan.getText());
            ps.setString(6, TNoRw.getText());
            ps.executeUpdate();
            updateLaporanOperasiTerakhir();
            JOptionPane.showMessageDialog(null, "Laporan operasi tersimpan.");
            dispose();
        } catch (Exception e) {
            System.out.println("Notif : " + e);
            JOptionPane.showMessageDialog(null, "Gagal menyimpan laporan operasi : " + e.getMessage());
        }
    }

    private void updateLaporanOperasiTerakhir() {
        String tanggal = "";
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select tanggal from laporan_operasi where no_rawat=? order by tanggal desc limit 1")) {
            ps.setString(1, TNoRw.getText());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tanggal = rs.getString("tanggal");
                }
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }

        if (tanggal.equals("")) {
            return;
        }

        try (PreparedStatement ps = koneksi.prepareStatement(
                "update laporan_operasi set diagnosa_preop=?,diagnosa_postop=?,jaringan_dieksekusi=?,"+
                "permintaan_pa=?,laporan_operasi=? where no_rawat=? and tanggal=?")) {
            ps.setString(1, PreOp.getText());
            ps.setString(2, PostOp.getText());
            ps.setString(3, Jaringan.getText());
            ps.setString(4, DikirimPA.getSelectedItem().toString());
            ps.setString(5, Laporan.getText());
            ps.setString(6, TNoRw.getText());
            ps.setString(7, tanggal);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
    }
}
