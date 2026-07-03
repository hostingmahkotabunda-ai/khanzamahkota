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
 * Dialog pencari Template Catatan ADIME Gizi. Logika mengikuti pola
 * MasterCariTemplateLaporanOperasi: daftar template di kiri, preview di
 * kanan, dan tombol Tambah untuk membuka master pengelola template.
 * Template hanya berisi field Asesmen, Monitoring, dan Evaluasi (Diagnosis,
 * Intervensi, Instruksi sengaja tidak ikut template agar data tarikan
 * tidak tertimpa). Template yang dipilih dibaca form pemanggil lewat
 * getTable().
 */
public final class MasterCariTemplateADIMEGizi extends javax.swing.JDialog {
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

    public MasterCariTemplateADIMEGizi(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        pastikanTabelTemplate();
        initComponents();
        this.setLocation(10,2);
        setSize(656,250);

        Object[] row={"No.Template","Nama Template","Asesmen","Monitoring","Evaluasi","Instruksi"};
        tabMode=new DefaultTableModel(null,row){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbKamar.setModel(tabMode);
        tbKamar.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbKamar.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < 6; i++) {
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

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Cari Template Catatan ADIME Gizi ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50)));
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
        MasterTemplateADIMEGizi form=new MasterTemplateADIMEGizi(null,false);
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
                        "ASESMEN :\n"+nilai(r,2)+"\n\n"+
                        "MONITORING :\n"+nilai(r,3)+"\n\n"+
                        "EVALUASI :\n"+nilai(r,4)+"\n\n"+
                        "INSTRUKSI :\n"+nilai(r,5));
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
                    "select no_template,nama_template,asesmen,monitoring,evaluasi,instruksi from template_adime_gizi "+
                    (TCari.getText().trim().equals("")?"":"where no_template like ? or nama_template like ? or asesmen like ? or monitoring like ? or evaluasi like ? or instruksi like ? ")+
                    "order by no_template");
            try {
                if(!TCari.getText().trim().equals("")){
                    for(int p=1;p<=6;p++){
                        ps.setString(p,"%"+TCari.getText().trim()+"%");
                    }
                }
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new Object[]{
                        rs.getString("no_template"),rs.getString("nama_template"),rs.getString("asesmen"),
                        rs.getString("monitoring"),rs.getString("evaluasi"),rs.getString("instruksi")
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

    /** Membuat tabel template_adime_gizi (skema Asesmen/Monitoring/Evaluasi/
     * Instruksi) bila belum ada, dan mengisi data default saat masih kosong.
     * Migrasi: bila tabel skema lama 6-field (punya kolom diagnosis) → drop
     * & buat ulang; bila skema 3-field (belum punya kolom instruksi) →
     * tambah kolom instruksi tanpa menghapus data. */
    private void pastikanTabelTemplate(){
        try{
            boolean tabelAda=false, adaDiagnosis=false, adaInstruksi=false;
            ps=koneksi.prepareStatement(
                    "select count(*) as jml from information_schema.tables where table_schema=database() and table_name='template_adime_gizi'");
            rs=ps.executeQuery();
            if(rs.next()){ tabelAda=rs.getInt("jml")>0; }
            rs.close(); ps.close();
            if(tabelAda){
                ps=koneksi.prepareStatement(
                        "select count(*) as jml from information_schema.columns where table_schema=database() and table_name='template_adime_gizi' and column_name='diagnosis'");
                rs=ps.executeQuery();
                if(rs.next()){ adaDiagnosis=rs.getInt("jml")>0; }
                rs.close(); ps.close();
                ps=koneksi.prepareStatement(
                        "select count(*) as jml from information_schema.columns where table_schema=database() and table_name='template_adime_gizi' and column_name='instruksi'");
                rs=ps.executeQuery();
                if(rs.next()){ adaInstruksi=rs.getInt("jml")>0; }
                rs.close(); ps.close();
                if(adaDiagnosis){
                    ps=koneksi.prepareStatement("drop table template_adime_gizi");
                    ps.executeUpdate();
                    ps.close();
                }else if(!adaInstruksi){
                    ps=koneksi.prepareStatement("alter table template_adime_gizi add column instruksi text");
                    ps.executeUpdate();
                    ps.close();
                }
            }
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        try{
            ps=koneksi.prepareStatement(
                    "create table if not exists template_adime_gizi("+
                    "no_template varchar(15) not null,"+
                    "nama_template varchar(150),"+
                    "asesmen text,"+
                    "monitoring text,"+
                    "evaluasi text,"+
                    "instruksi text,"+
                    "primary key(no_template)) engine=InnoDB default charset=utf8");
            ps.executeUpdate();
            ps.close();
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        try{
            int jml=0;
            ps=koneksi.prepareStatement("select count(*) as jml from template_adime_gizi");
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
                "Asupan makan baik. Keadaan umum baik, kesadaran composmentis. Antropometri dalam batas normal. Hasil laboratorium dalam batas normal.",
                "Monitoring asupan makan harian, berat badan, dan hasil laboratorium.",
                "Asupan makan tercapai minimal 80% dari kebutuhan. Status gizi dipertahankan baik.",
                "Lanjutkan diet. Kolaborasi dengan DPJP dan perawat untuk pemantauan asupan."},
            {"G0002","Asupan Kurang / TKTP",
                "Asupan makan kurang dari kebutuhan (<80%). Keadaan umum lemah. Nafsu makan menurun. Penurunan berat badan.",
                "Monitoring asupan makan setiap hari, berat badan, dan keluhan saluran cerna.",
                "Asupan makan meningkat bertahap menuju target kebutuhan. Berat badan dipertahankan/meningkat.",
                "Lanjutkan diet TKTP, evaluasi asupan tiap hari. Kolaborasi DPJP bila asupan tetap rendah."},
            {"G0003","Pasien Diabetes Mellitus",
                "Riwayat Diabetes Mellitus. Kadar gula darah tinggi. Keadaan umum baik, nafsu makan baik.",
                "Monitoring asupan makan, kadar gula darah (GDS), dan berat badan.",
                "Kadar gula darah terkontrol. Asupan sesuai diet DM. Status gizi dipertahankan.",
                "Lanjutkan diet DM. Kolaborasi DPJP untuk pengaturan terapi dan pemantauan gula darah."}
        };
        try{
            ps=koneksi.prepareStatement("insert into template_adime_gizi(no_template,nama_template,asesmen,monitoring,evaluasi,instruksi) values(?,?,?,?,?,?)");
            for(String[] d:data){
                for(int c=0;c<6;c++){
                    ps.setString(c+1,d[c]);
                }
                ps.executeUpdate();
            }
            ps.close();
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }
}
