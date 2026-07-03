package rekammedis;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * Master pengelola Template Asuhan Gizi (tambah / ubah / hapus). Satu
 * template berisi teks untuk field Fisik/Klinis, Intervensi Gizi, dan
 * Monitoring & Evaluasi. Pola mengikuti MasterTemplateLaporanOperasi.
 */
public class MasterTemplateAsuhanGizi extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private Connection koneksi=koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i;

    private widget.InternalFrame internalFrame1;
    private javax.swing.JTabbedPane TabRawat;
    private widget.InternalFrame internalFrame2;
    private widget.ScrollPane scrollInput;
    private widget.PanelBiasa FormInput;
    private widget.Label labelKd;
    private widget.TextBox Kd;
    private widget.Label labelNama;
    private widget.TextBox NamaTemplate;
    private widget.Label labelFisik;
    private widget.ScrollPane scrollFisik;
    private widget.TextArea FisikKlinis;
    private widget.Label labelIntervensi;
    private widget.ScrollPane scrollIntervensi;
    private widget.TextArea IntervensiGizi;
    private widget.Label labelMonitoring;
    private widget.ScrollPane scrollMonitoring;
    private widget.TextArea Monitoring;
    private widget.InternalFrame internalFrame3;
    private widget.ScrollPane Scroll;
    private widget.Table tbDokter;
    private widget.panelisi panelGlass9;
    private widget.Label label9;
    private widget.TextBox TCari;
    private widget.Button BtnCari;
    private widget.Button BtnAll;
    private widget.panelisi panelGlass8;
    private widget.Button BtnSimpan;
    private widget.Button BtnBatal;
    private widget.Button BtnHapus;
    private widget.Button BtnEdit;
    private widget.Label label10;
    private widget.Label LCount;
    private widget.Button BtnKeluar;

    public MasterTemplateAsuhanGizi(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        Object[] row={"No.Template","Nama Template","Fisik / Klinis","Intervensi Gizi","Monitoring & Evaluasi"};
        tabMode=new DefaultTableModel(null,row){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbDokter.setModel(tabMode);
        tbDokter.setPreferredScrollableViewportSize(new Dimension(800,800));
        tbDokter.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (i = 0; i < 5; i++) {
            TableColumn column = tbDokter.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(80);
            }else if(i==1){
                column.setPreferredWidth(200);
            }else{
                column.setPreferredWidth(300);
            }
        }
        tbDokter.setDefaultRenderer(Object.class, new WarnaTable());

        Kd.setDocument(new batasInput((byte)15).getKata(Kd));
        NamaTemplate.setDocument(new batasInput((byte)100).getKata(NamaTemplate));
        FisikKlinis.setDocument(new batasInput((int)1000).getKata(FisikKlinis));
        IntervensiGizi.setDocument(new batasInput((int)1000).getKata(IntervensiGizi));
        Monitoring.setDocument(new batasInput((int)1000).getKata(Monitoring));
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override public void insertUpdate(DocumentEvent e) { if(TCari.getText().length()>2){ tampil(); } }
                @Override public void removeUpdate(DocumentEvent e) { if(TCari.getText().length()>2){ tampil(); } }
                @Override public void changedUpdate(DocumentEvent e) { if(TCari.getText().length()>2){ tampil(); } }
            });
        }
    }

    private void initComponents() {
        internalFrame1 = new widget.InternalFrame();
        TabRawat = new javax.swing.JTabbedPane();
        internalFrame2 = new widget.InternalFrame();
        scrollInput = new widget.ScrollPane();
        FormInput = new widget.PanelBiasa();
        labelKd = new widget.Label();
        Kd = new widget.TextBox();
        labelNama = new widget.Label();
        NamaTemplate = new widget.TextBox();
        labelFisik = new widget.Label();
        scrollFisik = new widget.ScrollPane();
        FisikKlinis = new widget.TextArea();
        labelIntervensi = new widget.Label();
        scrollIntervensi = new widget.ScrollPane();
        IntervensiGizi = new widget.TextArea();
        labelMonitoring = new widget.Label();
        scrollMonitoring = new widget.ScrollPane();
        Monitoring = new widget.TextArea();
        internalFrame3 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbDokter = new widget.Table();
        panelGlass9 = new widget.panelisi();
        label9 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        label10 = new widget.Label();
        LCount = new widget.Label();
        BtnKeluar = new widget.Button();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Master Template Asuhan Gizi ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50)));
        internalFrame1.setName("internalFrame1");
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        TabRawat.setBackground(new java.awt.Color(254, 255, 254));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setFont(new java.awt.Font("Tahoma", 0, 11));
        TabRawat.setName("TabRawat");
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if(TabRawat.getSelectedIndex()==1){ tampil(); }
            }
        });

        internalFrame2.setBorder(null);
        internalFrame2.setName("internalFrame2");
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        scrollInput.setName("scrollInput");
        scrollInput.setPreferredSize(new java.awt.Dimension(102, 557));

        FormInput.setBackground(new java.awt.Color(255, 255, 255));
        FormInput.setBorder(null);
        FormInput.setName("FormInput");
        FormInput.setPreferredSize(new java.awt.Dimension(700, 520));
        FormInput.setLayout(null);

        labelKd.setText("No.Template :");
        labelKd.setName("labelKd");
        FormInput.add(labelKd);
        labelKd.setBounds(0, 12, 90, 23);

        Kd.setEditable(false);
        Kd.setName("Kd");
        FormInput.add(Kd);
        Kd.setBounds(95, 12, 120, 23);

        labelNama.setText("Nama Template :");
        labelNama.setName("labelNama");
        FormInput.add(labelNama);
        labelNama.setBounds(230, 12, 100, 23);

        NamaTemplate.setName("NamaTemplate");
        NamaTemplate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Valid.pindah(evt,Kd,FisikKlinis);
            }
        });
        FormInput.add(NamaTemplate);
        NamaTemplate.setBounds(335, 12, 350, 23);

        labelFisik.setText("Fisik / Klinis :");
        labelFisik.setName("labelFisik");
        FormInput.add(labelFisik);
        labelFisik.setBounds(16, 45, 200, 20);

        scrollFisik.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollFisik.setName("scrollFisik");
        FisikKlinis.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        FisikKlinis.setColumns(20);
        FisikKlinis.setRows(5);
        FisikKlinis.setLineWrap(true);
        FisikKlinis.setWrapStyleWord(true);
        FisikKlinis.setName("FisikKlinis");
        scrollFisik.setViewportView(FisikKlinis);
        FormInput.add(scrollFisik);
        scrollFisik.setBounds(16, 68, 670, 110);

        labelIntervensi.setText("Intervensi Gizi :");
        labelIntervensi.setName("labelIntervensi");
        FormInput.add(labelIntervensi);
        labelIntervensi.setBounds(16, 185, 200, 20);

        scrollIntervensi.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollIntervensi.setName("scrollIntervensi");
        IntervensiGizi.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        IntervensiGizi.setColumns(20);
        IntervensiGizi.setRows(5);
        IntervensiGizi.setLineWrap(true);
        IntervensiGizi.setWrapStyleWord(true);
        IntervensiGizi.setName("IntervensiGizi");
        scrollIntervensi.setViewportView(IntervensiGizi);
        FormInput.add(scrollIntervensi);
        scrollIntervensi.setBounds(16, 208, 670, 110);

        labelMonitoring.setText("Monitoring & Evaluasi :");
        labelMonitoring.setName("labelMonitoring");
        FormInput.add(labelMonitoring);
        labelMonitoring.setBounds(16, 325, 200, 20);

        scrollMonitoring.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollMonitoring.setName("scrollMonitoring");
        Monitoring.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Monitoring.setColumns(20);
        Monitoring.setRows(5);
        Monitoring.setLineWrap(true);
        Monitoring.setWrapStyleWord(true);
        Monitoring.setName("Monitoring");
        scrollMonitoring.setViewportView(Monitoring);
        FormInput.add(scrollMonitoring);
        scrollMonitoring.setBounds(16, 348, 670, 110);

        scrollInput.setViewportView(FormInput);
        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);
        TabRawat.addTab("Input Template", internalFrame2);

        internalFrame3.setBorder(null);
        internalFrame3.setName("internalFrame3");
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll");
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));

        tbDokter.setAutoCreateRowSorter(true);
        tbDokter.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbDokter.setName("tbDokter");
        tbDokter.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbDokterMouseClicked(evt);
            }
        });
        tbDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbDokterKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbDokter);
        internalFrame3.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9");
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        label9.setText("Key Word :");
        label9.setName("label9");
        label9.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(label9);

        TCari.setName("TCari");
        TCari.setPreferredSize(new java.awt.Dimension(530, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if(evt.getKeyCode()==KeyEvent.VK_ENTER){ tampil(); }
                else if(evt.getKeyCode()==KeyEvent.VK_UP){ tbDokter.requestFocus(); }
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setMnemonic('1');
        BtnCari.setToolTipText("Alt+1");
        BtnCari.setName("BtnCari");
        BtnCari.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tampil();
            }
        });
        panelGlass9.add(BtnCari);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnAll.setMnemonic('M');
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll");
        BtnAll.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TCari.setText("");
                tampil();
            }
        });
        panelGlass9.add(BtnAll);

        internalFrame3.add(panelGlass9, java.awt.BorderLayout.PAGE_END);
        TabRawat.addTab("Data Template", internalFrame3);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        panelGlass8.setName("panelGlass8");
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 54));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16i.png")));
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setName("BtnSimpan");
        BtnSimpan.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpanActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnSimpan);

        BtnBatal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png")));
        BtnBatal.setMnemonic('B');
        BtnBatal.setText("Baru");
        BtnBatal.setToolTipText("Alt+B");
        BtnBatal.setName("BtnBatal");
        BtnBatal.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emptTeks();
            }
        });
        panelGlass8.add(BtnBatal);

        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png")));
        BtnHapus.setMnemonic('H');
        BtnHapus.setText("Hapus");
        BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setName("BtnHapus");
        BtnHapus.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnHapusActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnHapus);

        BtnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png")));
        BtnEdit.setMnemonic('G');
        BtnEdit.setText("Ganti");
        BtnEdit.setToolTipText("Alt+G");
        BtnEdit.setName("BtnEdit");
        BtnEdit.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnEditActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnEdit);

        label10.setText("Record :");
        label10.setName("label10");
        label10.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass8.add(label10);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount");
        LCount.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass8.add(LCount);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setName("BtnKeluar");
        BtnKeluar.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dispose();
            }
        });
        panelGlass8.add(BtnKeluar);

        internalFrame1.add(panelGlass8, java.awt.BorderLayout.PAGE_END);
        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);
        pack();
    }

    private void tbDokterMouseClicked(java.awt.event.MouseEvent evt) {
        if(tabMode.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
            if((evt.getClickCount()==2)&&(tbDokter.getSelectedColumn()==0)){
                TabRawat.setSelectedIndex(0);
            }
        }
    }

    private void tbDokterKeyPressed(java.awt.event.KeyEvent evt) {
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try { getData(); } catch (java.lang.NullPointerException e) { }
            }else if(evt.getKeyCode()==KeyEvent.VK_SPACE){
                try {
                    getData();
                    TabRawat.setSelectedIndex(0);
                } catch (java.lang.NullPointerException e) { }
            }
        }
    }

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {
        if(Kd.getText().trim().equals("")){
            Valid.textKosong(Kd,"No.Template");
        }else if(NamaTemplate.getText().trim().equals("")){
            Valid.textKosong(NamaTemplate,"Nama Template");
        }else if(FisikKlinis.getText().trim().equals("")){
            Valid.textKosong(FisikKlinis,"Fisik / Klinis");
        }else if(IntervensiGizi.getText().trim().equals("")){
            Valid.textKosong(IntervensiGizi,"Intervensi Gizi");
        }else if(Monitoring.getText().trim().equals("")){
            Valid.textKosong(Monitoring,"Monitoring & Evaluasi");
        }else{
            if(Sequel.menyimpantf("template_asuhan_gizi","?,?,?,?,?","No.Template",5,new String[]{
                Kd.getText(),NamaTemplate.getText(),FisikKlinis.getText(),IntervensiGizi.getText(),Monitoring.getText()
            })==true){
                emptTeks();
            }
        }
    }

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {
        if(Kd.getText().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Maaf, Pilih dulu data yang akan Anda hapus dengan menklik data pada tabel...!!!");
            tbDokter.requestFocus();
        }else{
            if(Valid.hapusTabletf(tabMode,Kd,"template_asuhan_gizi","no_template")==true){
                if(tbDokter.getSelectedRow()!= -1){
                    tabMode.removeRow(tbDokter.getSelectedRow());
                    LCount.setText(""+tabMode.getRowCount());
                    emptTeks();
                }
            }
        }
    }

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {
        if(Kd.getText().trim().equals("")){
            Valid.textKosong(Kd,"No.Template");
        }else if(NamaTemplate.getText().trim().equals("")){
            Valid.textKosong(NamaTemplate,"Nama Template");
        }else if(FisikKlinis.getText().trim().equals("")){
            Valid.textKosong(FisikKlinis,"Fisik / Klinis");
        }else if(IntervensiGizi.getText().trim().equals("")){
            Valid.textKosong(IntervensiGizi,"Intervensi Gizi");
        }else if(Monitoring.getText().trim().equals("")){
            Valid.textKosong(Monitoring,"Monitoring & Evaluasi");
        }else if(tbDokter.getSelectedRow()== -1){
            JOptionPane.showMessageDialog(null,"Maaf, Pilih dulu data yang akan Anda ubah dengan menklik data pada tabel...!!!");
        }else{
            if(Valid.editTabletf(tabMode,"template_asuhan_gizi","no_template","?","no_template=?,nama_template=?,fisik_klinis=?,intervensi_gizi=?,monitoring_evaluasi=?",6,new String[]{
                Kd.getText(),NamaTemplate.getText(),FisikKlinis.getText(),IntervensiGizi.getText(),Monitoring.getText(),tbDokter.getValueAt(tbDokter.getSelectedRow(),0).toString()
            })==true){
                tbDokter.setValueAt(Kd.getText(),tbDokter.getSelectedRow(),0);
                tbDokter.setValueAt(NamaTemplate.getText(),tbDokter.getSelectedRow(),1);
                tbDokter.setValueAt(FisikKlinis.getText(),tbDokter.getSelectedRow(),2);
                tbDokter.setValueAt(IntervensiGizi.getText(),tbDokter.getSelectedRow(),3);
                tbDokter.setValueAt(Monitoring.getText(),tbDokter.getSelectedRow(),4);
                emptTeks();
                TabRawat.setSelectedIndex(1);
            }
        }
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            ps=koneksi.prepareStatement(
                    "select no_template,nama_template,fisik_klinis,intervensi_gizi,monitoring_evaluasi from template_asuhan_gizi "+
                    (TCari.getText().trim().equals("")?"":"where no_template like ? or nama_template like ? or fisik_klinis like ? or intervensi_gizi like ? or monitoring_evaluasi like ? ")+
                    "order by no_template");
            try {
                if(!TCari.getText().trim().equals("")){
                    ps.setString(1,"%"+TCari.getText().trim()+"%");
                    ps.setString(2,"%"+TCari.getText().trim()+"%");
                    ps.setString(3,"%"+TCari.getText().trim()+"%");
                    ps.setString(4,"%"+TCari.getText().trim()+"%");
                    ps.setString(5,"%"+TCari.getText().trim()+"%");
                }
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new Object[]{
                        rs.getString("no_template"),rs.getString("nama_template"),rs.getString("fisik_klinis"),
                        rs.getString("intervensi_gizi"),rs.getString("monitoring_evaluasi")
                    });
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally{
                if(rs!=null){ rs.close(); }
                if(ps!=null){ ps.close(); }
            }
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        LCount.setText(""+tabMode.getRowCount());
    }

    public void emptTeks() {
        Kd.setText("");
        NamaTemplate.setText("");
        FisikKlinis.setText("");
        IntervensiGizi.setText("");
        Monitoring.setText("");
        Valid.autoNomer("template_asuhan_gizi","G",4,Kd);
        TabRawat.setSelectedIndex(0);
        NamaTemplate.requestFocus();
    }

    private void getData() {
        if(tbDokter.getSelectedRow()!= -1){
            Kd.setText(tabMode.getValueAt(tbDokter.getSelectedRow(),0).toString());
            NamaTemplate.setText(tabMode.getValueAt(tbDokter.getSelectedRow(),1).toString());
            FisikKlinis.setText(tabMode.getValueAt(tbDokter.getSelectedRow(),2).toString());
            IntervensiGizi.setText(tabMode.getValueAt(tbDokter.getSelectedRow(),3).toString());
            Monitoring.setText(tabMode.getValueAt(tbDokter.getSelectedRow(),4).toString());
        }
    }

    public JTable getTable(){
        return tbDokter;
    }

    public void isCek(){
        BtnSimpan.setEnabled(akses.getasuhan_gizi());
        BtnHapus.setEnabled(akses.getasuhan_gizi());
        BtnEdit.setEnabled(akses.getasuhan_gizi());
    }

    public void setTampil(){
        TabRawat.setSelectedIndex(1);
    }
}
