package rekammedis;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * Dialog pencari Template Asuhan Gizi. Logika mengikuti pola
 * MasterCariTemplateLaporanOperasi: daftar template di kiri, preview di
 * kanan, dan tombol Tambah untuk membuka master pengelola template.
 * Template yang dipilih akan dibaca oleh form pemanggil lewat getTable().
 */
public final class MasterCariTemplateAsuhanGizi extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private validasi Valid=new validasi();
    private Connection koneksi=koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;

    private widget.InternalFrame internalFrame1;
    private widget.ScrollPane Scroll;
    private widget.Table tbKamar;
    private widget.panelisi panelisi3;
    private widget.Label label9;
    private widget.TextBox TCari;
    private widget.Button BtnCari;
    private widget.Button BtnAll;
    private widget.Button BtnTambah;
    private widget.Label label10;
    private widget.Label LCount;
    private widget.Button BtnKeluar;
    private widget.ScrollPane scrollPane2;
    private widget.TextArea Template;

    public MasterCariTemplateAsuhanGizi(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        pastikanTabelTemplate();
        initComponents();
        this.setLocation(10,2);
        setSize(656,250);

        Object[] row={"No.Template","Nama Template","Fisik / Klinis","Intervensi Gizi","Monitoring & Evaluasi"};
        tabMode=new DefaultTableModel(null,row){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbKamar.setModel(tabMode);
        tbKamar.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbKamar.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < 5; i++) {
            TableColumn column = tbKamar.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(80);
            }else if(i==1){
                column.setPreferredWidth(220);
            }else{
                column.setMinWidth(0);
                column.setMaxWidth(0);
                column.setPreferredWidth(0);
            }
        }
        tbKamar.setDefaultRenderer(Object.class, new WarnaTable());
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
        Scroll = new widget.ScrollPane();
        tbKamar = new widget.Table();
        panelisi3 = new widget.panelisi();
        label9 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        BtnTambah = new widget.Button();
        label10 = new widget.Label();
        LCount = new widget.Label();
        BtnKeluar = new widget.Button();
        scrollPane2 = new widget.ScrollPane();
        Template = new widget.TextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                emptTeks();
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Cari Template Asuhan Gizi ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50)));
        internalFrame1.setName("internalFrame1");
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll");
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(320, 402));

        tbKamar.setAutoCreateRowSorter(true);
        tbKamar.setName("tbKamar");
        tbKamar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbKamarMouseClicked(evt);
            }
        });
        tbKamar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbKamarKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbKamar);
        internalFrame1.add(Scroll, java.awt.BorderLayout.WEST);

        panelisi3.setName("panelisi3");
        panelisi3.setPreferredSize(new java.awt.Dimension(100, 43));
        panelisi3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 9));

        label9.setText("Key Word :");
        label9.setName("label9");
        label9.setPreferredSize(new java.awt.Dimension(68, 23));
        panelisi3.add(label9);

        TCari.setName("TCari");
        TCari.setPreferredSize(new java.awt.Dimension(312, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelisi3.add(TCari);

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
        BtnCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if(evt.getKeyCode()==KeyEvent.VK_SPACE){ tampil(); }
                else{ Valid.pindah(evt, TCari, BtnAll); }
            }
        });
        panelisi3.add(BtnCari);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnAll.setMnemonic('2');
        BtnAll.setToolTipText("Alt+2");
        BtnAll.setName("BtnAll");
        BtnAll.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TCari.setText("");
                tampil();
            }
        });
        panelisi3.add(BtnAll);

        BtnTambah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png")));
        BtnTambah.setMnemonic('3');
        BtnTambah.setToolTipText("Alt+3 : Tambah / Kelola Template");
        BtnTambah.setName("BtnTambah");
        BtnTambah.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTambahActionPerformed(evt);
            }
        });
        panelisi3.add(BtnTambah);

        label10.setText("Record :");
        label10.setName("label10");
        label10.setPreferredSize(new java.awt.Dimension(60, 23));
        panelisi3.add(label10);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount");
        LCount.setPreferredSize(new java.awt.Dimension(50, 23));
        panelisi3.add(LCount);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setMnemonic('4');
        BtnKeluar.setToolTipText("Alt+4");
        BtnKeluar.setName("BtnKeluar");
        BtnKeluar.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dispose();
            }
        });
        panelisi3.add(BtnKeluar);

        internalFrame1.add(panelisi3, java.awt.BorderLayout.PAGE_END);

        scrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)), "Isi Template :", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50)));
        scrollPane2.setName("scrollPane2");

        Template.setEditable(false);
        Template.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Template.setColumns(20);
        Template.setRows(40);
        Template.setLineWrap(true);
        Template.setWrapStyleWord(true);
        Template.setName("Template");
        scrollPane2.setViewportView(Template);

        internalFrame1.add(scrollPane2, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);
        pack();
    }

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            tampil();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            tbKamar.requestFocus();
        }
    }

    private void BtnTambahActionPerformed(java.awt.event.ActionEvent evt) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        MasterTemplateAsuhanGizi form=new MasterTemplateAsuhanGizi(null,false);
        form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        form.setLocationRelativeTo(internalFrame1);
        form.setAlwaysOnTop(false);
        form.emptTeks();
        form.isCek();
        form.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
    }

    private void tbKamarKeyPressed(java.awt.event.KeyEvent evt) {
        if(tabMode.getRowCount()!=0){
            if(evt.getKeyCode()==KeyEvent.VK_SPACE){
                dispose();
            }else if(evt.getKeyCode()==KeyEvent.VK_SHIFT){
                TCari.setText("");
                TCari.requestFocus();
            }else if((evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                tampilPreview();
            }
        }
    }

    private void tbKamarMouseClicked(java.awt.event.MouseEvent evt) {
        tampilPreview();
        if(evt.getClickCount()==2 && tbKamar.getSelectedRow()!=-1){
            dispose();
        }
    }

    private void tampilPreview(){
        if(tabMode.getRowCount()!=0){
            try {
                if(tbKamar.getSelectedRow()!= -1){
                    int r=tbKamar.getSelectedRow();
                    Template.setText(
                        "FISIK / KLINIS :\n"+nilai(r,2)+"\n\n"+
                        "INTERVENSI GIZI :\n"+nilai(r,3)+"\n\n"+
                        "MONITORING & EVALUASI :\n"+nilai(r,4));
                    Template.setCaretPosition(0);
                }
            } catch (java.lang.NullPointerException e) {
            }
        }
    }

    private String nilai(int row,int col){
        Object o=tabMode.getValueAt(row,col);
        return o==null?"":o.toString();
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
        Template.setText("");
        tampil();
        TCari.requestFocus();
    }

    public JTable getTable(){
        return tbKamar;
    }

    public void isCek(){
        BtnTambah.setEnabled(akses.getasuhan_gizi());
    }

    /** Membuat tabel template_asuhan_gizi bila belum ada (skema baru),
     * dan mengisi data default saat tabel masih kosong. Bila tabel lama
     * dengan skema berbeda terdeteksi, tabel di-drop lalu dibuat ulang. */
    private void pastikanTabelTemplate(){
        try{
            boolean tabelAda=false, kolomBaruAda=false;
            ps=koneksi.prepareStatement(
                    "select count(*) as jml from information_schema.tables where table_schema=database() and table_name='template_asuhan_gizi'");
            rs=ps.executeQuery();
            if(rs.next()){ tabelAda=rs.getInt("jml")>0; }
            rs.close(); ps.close();
            if(tabelAda){
                ps=koneksi.prepareStatement(
                        "select count(*) as jml from information_schema.columns where table_schema=database() and table_name='template_asuhan_gizi' and column_name='nama_template'");
                rs=ps.executeQuery();
                if(rs.next()){ kolomBaruAda=rs.getInt("jml")>0; }
                rs.close(); ps.close();
                if(!kolomBaruAda){
                    ps=koneksi.prepareStatement("drop table template_asuhan_gizi");
                    ps.executeUpdate();
                    ps.close();
                }
            }
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        try{
            ps=koneksi.prepareStatement(
                    "create table if not exists template_asuhan_gizi("+
                    "no_template varchar(15) not null,"+
                    "nama_template varchar(150),"+
                    "fisik_klinis text,"+
                    "intervensi_gizi text,"+
                    "monitoring_evaluasi text,"+
                    "primary key(no_template)) engine=InnoDB default charset=utf8");
            ps.executeUpdate();
            ps.close();
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        try{
            int jml=0;
            ps=koneksi.prepareStatement("select count(*) as jml from template_asuhan_gizi");
            rs=ps.executeQuery();
            if(rs.next()){ jml=rs.getInt("jml"); }
            rs.close(); ps.close();
            if(jml==0){ seedTemplateDefault(); }
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }

    private void seedTemplateDefault(){
        String[][] data={
            {"G0001","Pasien Umum / KU Baik",
                "Keadaan umum baik, kesadaran composmentis. Tanda-tanda vital dalam batas normal. Tidak ada mual, muntah, maupun diare. Nafsu makan baik.",
                "Pemberian diet biasa bentuk makanan biasa melalui rute oral. Energi dan protein sesuai kebutuhan. Edukasi gizi kepada pasien dan keluarga mengenai diet yang dianjurkan.",
                "Monitoring asupan makan harian (target minimal 80% dari kebutuhan), berat badan, dan hasil laboratorium. Evaluasi setiap hari dan sesuaikan intervensi sesuai perkembangan klinis."},
            {"G0002","Pasien Lemah / TKTP",
                "Keadaan umum lemah, kesadaran composmentis. Nafsu makan menurun. Asupan makan kurang dari kebutuhan.",
                "Pemberian diet Tinggi Energi Tinggi Protein (TKTP) bentuk makanan lunak, rute oral, porsi kecil tapi sering. Edukasi gizi seimbang kepada pasien dan keluarga.",
                "Monitoring asupan makan dan perubahan berat badan secara berkala. Evaluasi daya terima diet dan keluhan saluran cerna."},
            {"G0003","Gangguan Saluran Cerna",
                "Mual (+), muntah (+), kembung (+), nyeri ulu hati (+). Penurunan nafsu makan dan asupan oral.",
                "Pemberian diet lambung bentuk makanan lunak/saring, porsi kecil sering, rendah serat dan tidak merangsang. Edukasi gizi kepada pasien dan keluarga.",
                "Monitoring asupan makan, keluhan saluran cerna (mual/muntah), dan toleransi diet. Evaluasi setiap hari."},
            {"G0004","Pasien Diabetes Mellitus",
                "Keadaan umum baik, kesadaran composmentis. Riwayat Diabetes Mellitus. Nafsu makan baik.",
                "Pemberian diet Diabetes Mellitus sesuai kebutuhan energi, 3x makan utama dan 2-3x selingan, rendah gula sederhana. Edukasi diet DM kepada pasien dan keluarga.",
                "Monitoring asupan makan, kadar gula darah (GDS), dan berat badan. Evaluasi sesuai perkembangan klinis pasien."},
            {"G0005","Nutrisi via NGT",
                "Keadaan umum lemah, kesadaran menurun / terpasang NGT. Asupan oral tidak adekuat.",
                "Pemberian nutrisi enteral melalui NGT bentuk cair sesuai kebutuhan energi, diberikan bertahap sesuai toleransi. Edukasi keluarga mengenai pemberian sonde.",
                "Monitoring toleransi pemberian sonde (residu, distensi), asupan, dan berat badan. Evaluasi setiap hari."}
        };
        try{
            ps=koneksi.prepareStatement("insert into template_asuhan_gizi(no_template,nama_template,fisik_klinis,intervensi_gizi,monitoring_evaluasi) values(?,?,?,?,?)");
            for(String[] d:data){
                ps.setString(1,d[0]);
                ps.setString(2,d[1]);
                ps.setString(3,d[2]);
                ps.setString(4,d[3]);
                ps.setString(5,d[4]);
                ps.executeUpdate();
            }
            ps.close();
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }
}
