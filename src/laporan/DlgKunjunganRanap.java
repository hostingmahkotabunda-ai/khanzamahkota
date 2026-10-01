/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DlgLhtBiaya.java
 *
 * Created on 12 Jul 10, 16:21:34
 */

package laporan;

import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author perpustakaan
 */
public final class DlgKunjunganRanap extends javax.swing.JDialog {
    private final DefaultTableModel tabMode,tabMode2;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps,ps2;
    private ResultSet rs,rs2;
    private int i=0,lama=0,baru=0,laki=0,per=0;  
    private String setbaru="",setlama="",umurlk="",umurpr="",kddiagnosa="",diagnosa="",dokterdpjp="",status="";
    /** Creates new form DlgLhtBiaya
     * @param parent
     * @param modal */
    public DlgKunjunganRanap(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8,1);
        setSize(885,674);
        tabMode=new DefaultTableModel(null,new String[]{"No.","Lama","Baru","Nama Pasien","L","P","Alamat","Kode","Diagnosa","Ruang","Stts.Pulang","Tgl.Masuk","Tgl.Keluar","DPJP","Pilih","NoRawat"}){
              // Kolom 14 "Pilih" (checkbox hapus-dari-tampilan) satu2nya yg boleh diedit user;
              // kolom 15 "NoRawat" data tersembunyi (lebar 0) utk keperluan hapus.
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return colIndex==14;}
              @Override public Class<?> getColumnClass(int colIndex){return colIndex==14 ? Boolean.class : Object.class;}
        };
        tbBangsal.setModel(tabMode);
        //tbBangsal.setDefaultRenderer(Object.class, new WarnaTable(jPanel2.getBackground(),tbBangsal.getBackground()));
        tbBangsal.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbBangsal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 16; i++) {
            TableColumn column = tbBangsal.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(35);
            }else if(i==1){
                column.setPreferredWidth(70);
            }else if(i==2){
                column.setPreferredWidth(70);
            }else if(i==3){
                column.setPreferredWidth(200);
            }else if(i==4){
                column.setPreferredWidth(40);
            }else if(i==5){
                column.setPreferredWidth(40);
            }else if(i==6){
                column.setPreferredWidth(200);
            }else if(i==7){
                column.setPreferredWidth(40);
            }else if(i==8){
                column.setPreferredWidth(200);
            }else if(i==9){
                column.setPreferredWidth(200);
            }else if(i==10){
                column.setPreferredWidth(85);
            }else if(i==11){
                column.setPreferredWidth(75);
            }else if(i==12){
                column.setPreferredWidth(75);
            }else if(i==13){
                column.setPreferredWidth(250);
            }else if(i==14){
                column.setPreferredWidth(40);
                column.setMaxWidth(40);
            }else if(i==15){
                column.setMinWidth(0);
                column.setMaxWidth(0);
                column.setPreferredWidth(0);
            }
        }
        tbBangsal.getColumnModel().moveColumn(14,0);
        tbBangsal.setDefaultRenderer(Object.class, new WarnaTable());

        tabMode2=new DefaultTableModel(null,new String[]{"No.","Lama","Baru","Nama Pasien","L","P","Alamat","Kode","Diagnosa","Ruang","Stts.Pulang","Tgl.Pulang","DPJP","Pilih","NoRawat"}){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return colIndex==13;}
              @Override public Class<?> getColumnClass(int colIndex){return colIndex==13 ? Boolean.class : Object.class;}
        };
        tbBangsal2.setModel(tabMode2);
        //tbBangsal2.setDefaultRenderer(Object.class, new WarnaTable(jPanel2.getBackground(),tbBangsal2.getBackground()));
        tbBangsal2.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbBangsal2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 15; i++) {
            TableColumn column = tbBangsal2.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(35);
            }else if(i==1){
                column.setPreferredWidth(70);
            }else if(i==2){
                column.setPreferredWidth(70);
            }else if(i==3){
                column.setPreferredWidth(200);
            }else if(i==4){
                column.setPreferredWidth(40);
            }else if(i==5){
                column.setPreferredWidth(40);
            }else if(i==6){
                column.setPreferredWidth(200);
            }else if(i==7){
                column.setPreferredWidth(40);
            }else if(i==8){
                column.setPreferredWidth(200);
            }else if(i==9){
                column.setPreferredWidth(200);
            }else if(i==10){
                column.setPreferredWidth(85);
            }else if(i==11){
                column.setPreferredWidth(75);
            }else if(i==12){
                column.setPreferredWidth(200);
            }else if(i==13){
                column.setPreferredWidth(40);
                column.setMaxWidth(40);
            }else if(i==14){
                column.setMinWidth(0);
                column.setMaxWidth(0);
                column.setPreferredWidth(0);
            }
        }
        tbBangsal2.getColumnModel().moveColumn(13,0);
        tbBangsal2.setDefaultRenderer(Object.class, new WarnaTable());

        TCari.setDocument(new batasInput((int)90).getKata(TCari));
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        if(TabRawat.getSelectedIndex()==0){
                            tampil();
                        }else if(TabRawat.getSelectedIndex()==1){
                            tampil2();
                        }
                    }                        
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        if(TabRawat.getSelectedIndex()==0){
                            tampil();
                        }else if(TabRawat.getSelectedIndex()==1){
                            tampil2();
                        }
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        if(TabRawat.getSelectedIndex()==0){
                            tampil();
                        }else if(TabRawat.getSelectedIndex()==1){
                            tampil2();
                        }
                    }
                }
            });
        }
        
        // Jendela pencarian lama (DlgCariDokter/DlgCariBangsal/DlgCariCaraBayar/DlgKabupaten/
        // DlgKecamatan/DlgKelurahan) TIDAK dipakai lagi di sini -- diganti popupFilterMultiPilih()
        // (lihat BtnSeek2..BtnSeek7) biar filter bisa pilih lebih dari satu nilai sekaligus,
        // SAMA SEKALI TIDAK mengubah kelas2 itu sendiri krn dipakai jg di banyak halaman lain
        // (pola sama persis spt DlgKunjunganRalan.java).
        pastikanTabelDisembunyikanRanap();
        pasangTombolHapusTampilan();

        ChkInput.setSelected(false);
        isForm();       
    }    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        TKd = new widget.TextBox();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        ppTampilkanBaru = new javax.swing.JMenuItem();
        ppTampilkanLama = new javax.swing.JMenuItem();
        internalFrame1 = new widget.InternalFrame();
        TabRawat = new javax.swing.JTabbedPane();
        Scroll = new widget.ScrollPane();
        tbBangsal = new widget.Table();
        Scroll2 = new widget.ScrollPane();
        tbBangsal2 = new widget.Table();
        panelGlass5 = new widget.panelisi();
        label11 = new widget.Label();
        Tgl1 = new widget.Tanggal();
        label18 = new widget.Label();
        Tgl2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        jLabel7 = new widget.Label();
        BtnPrint = new widget.Button();
        BtnKeluar = new widget.Button();
        PanelInput = new javax.swing.JPanel();
        ChkInput = new widget.CekBox();
        FormInput = new widget.panelisi();
        label17 = new widget.Label();
        kdkamar = new widget.TextBox();
        nmkamar = new widget.TextBox();
        BtnSeek2 = new widget.Button();
        label19 = new widget.Label();
        kdpenjab = new widget.TextBox();
        nmpenjab = new widget.TextBox();
        BtnSeek3 = new widget.Button();
        label20 = new widget.Label();
        kddokter = new widget.TextBox();
        nmdokter = new widget.TextBox();
        BtnSeek4 = new widget.Button();
        label21 = new widget.Label();
        nmkabupaten = new widget.TextBox();
        BtnSeek5 = new widget.Button();
        label22 = new widget.Label();
        nmkecamatan = new widget.TextBox();
        BtnSeek6 = new widget.Button();
        BtnSeek7 = new widget.Button();
        nmkelurahan = new widget.TextBox();
        label23 = new widget.Label();

        TKd.setForeground(new java.awt.Color(255, 255, 255));
        TKd.setName("TKd"); // NOI18N

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        ppTampilkanBaru.setBackground(new java.awt.Color(255, 255, 254));
        ppTampilkanBaru.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppTampilkanBaru.setForeground(java.awt.Color.darkGray);
        ppTampilkanBaru.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppTampilkanBaru.setText("Tampilkan Pasien Baru");
        ppTampilkanBaru.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppTampilkanBaru.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppTampilkanBaru.setName("ppTampilkanBaru"); // NOI18N
        ppTampilkanBaru.setPreferredSize(new java.awt.Dimension(175, 25));
        ppTampilkanBaru.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppTampilkanBaruBtnPrintActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppTampilkanBaru);

        ppTampilkanLama.setBackground(new java.awt.Color(255, 255, 254));
        ppTampilkanLama.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppTampilkanLama.setForeground(java.awt.Color.darkGray);
        ppTampilkanLama.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppTampilkanLama.setText("Tampilkan Pasien Lama");
        ppTampilkanLama.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppTampilkanLama.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppTampilkanLama.setName("ppTampilkanLama"); // NOI18N
        ppTampilkanLama.setPreferredSize(new java.awt.Dimension(175, 25));
        ppTampilkanLama.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppTampilkanLamaBtnPrintActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppTampilkanLama);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Kunjungan Rawat Inap ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        TabRawat.setBackground(new java.awt.Color(255, 255, 254));
        TabRawat.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        TabRawat.setName("TabRawat"); // NOI18N
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });

        Scroll.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll.setComponentPopupMenu(jPopupMenu1);
        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbBangsal.setComponentPopupMenu(jPopupMenu1);
        tbBangsal.setName("tbBangsal"); // NOI18N
        tbBangsal.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbBangsalMouseClicked(evt);
            }
        });
        tbBangsal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbBangsalKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbBangsal);

        TabRawat.addTab("Berdasar Tanggal Masuk", Scroll);

        Scroll2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll2.setComponentPopupMenu(jPopupMenu1);
        Scroll2.setName("Scroll2"); // NOI18N
        Scroll2.setOpaque(true);

        tbBangsal2.setComponentPopupMenu(jPopupMenu1);
        tbBangsal2.setName("tbBangsal2"); // NOI18N
        tbBangsal2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbBangsal2MouseClicked(evt);
            }
        });
        tbBangsal2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbBangsal2KeyPressed(evt);
            }
        });
        Scroll2.setViewportView(tbBangsal2);

        TabRawat.addTab("Berdasar Tanggal Keluar", Scroll2);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        panelGlass5.setName("panelGlass5"); // NOI18N
        panelGlass5.setPreferredSize(new java.awt.Dimension(55, 55));
        panelGlass5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        label11.setText("Tanggal :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(55, 23));
        panelGlass5.add(label11);

        Tgl1.setDisplayFormat("dd-MM-yyyy");
        Tgl1.setName("Tgl1"); // NOI18N
        Tgl1.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass5.add(Tgl1);

        label18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label18.setText("s.d.");
        label18.setName("label18"); // NOI18N
        label18.setPreferredSize(new java.awt.Dimension(25, 23));
        panelGlass5.add(label18);

        Tgl2.setDisplayFormat("dd-MM-yyyy");
        Tgl2.setName("Tgl2"); // NOI18N
        Tgl2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass5.add(Tgl2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass5.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(155, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass5.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('2');
        BtnCari.setToolTipText("Alt+2");
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariActionPerformed(evt);
            }
        });
        BtnCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariKeyPressed(evt);
            }
        });
        panelGlass5.add(BtnCari);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('M');
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllActionPerformed(evt);
            }
        });
        BtnAll.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnAllKeyPressed(evt);
            }
        });
        panelGlass5.add(BtnAll);

        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(30, 23));
        panelGlass5.add(jLabel7);

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrint.setMnemonic('T');
        BtnPrint.setText("Cetak");
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setName("BtnPrint"); // NOI18N
        BtnPrint.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrintActionPerformed(evt);
            }
        });
        BtnPrint.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPrintKeyPressed(evt);
            }
        });
        panelGlass5.add(BtnPrint);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluarActionPerformed(evt);
            }
        });
        BtnKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnKeluarKeyPressed(evt);
            }
        });
        panelGlass5.add(BtnKeluar);

        internalFrame1.add(panelGlass5, java.awt.BorderLayout.PAGE_END);

        PanelInput.setBackground(new java.awt.Color(255, 255, 255));
        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setMnemonic('M');
        ChkInput.setText(".: Filter Data");
        ChkInput.setBorderPainted(true);
        ChkInput.setBorderPaintedFlat(true);
        ChkInput.setFocusable(false);
        ChkInput.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ChkInput.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ChkInput.setName("ChkInput"); // NOI18N
        ChkInput.setPreferredSize(new java.awt.Dimension(192, 20));
        ChkInput.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkInputActionPerformed(evt);
            }
        });
        PanelInput.add(ChkInput, java.awt.BorderLayout.PAGE_END);

        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(100, 104));
        FormInput.setLayout(null);

        label17.setText("Ruang :");
        label17.setName("label17"); // NOI18N
        label17.setPreferredSize(new java.awt.Dimension(35, 23));
        FormInput.add(label17);
        label17.setBounds(0, 10, 75, 23);

        kdkamar.setEditable(false);
        kdkamar.setName("kdkamar"); // NOI18N
        kdkamar.setPreferredSize(new java.awt.Dimension(75, 23));
        kdkamar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdkamarKeyPressed(evt);
            }
        });
        FormInput.add(kdkamar);
        kdkamar.setBounds(78, 10, 85, 23);

        nmkamar.setEditable(false);
        nmkamar.setName("nmkamar"); // NOI18N
        nmkamar.setPreferredSize(new java.awt.Dimension(215, 23));
        FormInput.add(nmkamar);
        nmkamar.setBounds(165, 10, 228, 23);

        BtnSeek2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek2.setMnemonic('3');
        BtnSeek2.setToolTipText("Alt+3");
        BtnSeek2.setName("BtnSeek2"); // NOI18N
        BtnSeek2.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnSeek2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeek2ActionPerformed(evt);
            }
        });
        BtnSeek2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSeek2KeyPressed(evt);
            }
        });
        FormInput.add(BtnSeek2);
        BtnSeek2.setBounds(396, 10, 28, 23);

        label19.setText("Cara Bayar :");
        label19.setName("label19"); // NOI18N
        label19.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(label19);
        label19.setBounds(0, 70, 75, 23);

        kdpenjab.setEditable(false);
        kdpenjab.setName("kdpenjab"); // NOI18N
        kdpenjab.setPreferredSize(new java.awt.Dimension(75, 23));
        kdpenjab.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdpenjabKeyPressed(evt);
            }
        });
        FormInput.add(kdpenjab);
        kdpenjab.setBounds(78, 70, 85, 23);

        nmpenjab.setEditable(false);
        nmpenjab.setName("nmpenjab"); // NOI18N
        nmpenjab.setPreferredSize(new java.awt.Dimension(215, 23));
        FormInput.add(nmpenjab);
        nmpenjab.setBounds(165, 70, 228, 23);

        BtnSeek3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek3.setMnemonic('3');
        BtnSeek3.setToolTipText("Alt+3");
        BtnSeek3.setName("BtnSeek3"); // NOI18N
        BtnSeek3.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnSeek3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeek3ActionPerformed(evt);
            }
        });
        BtnSeek3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSeek3KeyPressed(evt);
            }
        });
        FormInput.add(BtnSeek3);
        BtnSeek3.setBounds(396, 70, 28, 23);

        label20.setText("Dokter :");
        label20.setName("label20"); // NOI18N
        label20.setPreferredSize(new java.awt.Dimension(35, 23));
        FormInput.add(label20);
        label20.setBounds(0, 40, 75, 23);

        kddokter.setEditable(false);
        kddokter.setName("kddokter"); // NOI18N
        kddokter.setPreferredSize(new java.awt.Dimension(75, 23));
        kddokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kddokterKeyPressed(evt);
            }
        });
        FormInput.add(kddokter);
        kddokter.setBounds(78, 40, 85, 23);

        nmdokter.setEditable(false);
        nmdokter.setName("nmdokter"); // NOI18N
        nmdokter.setPreferredSize(new java.awt.Dimension(215, 23));
        FormInput.add(nmdokter);
        nmdokter.setBounds(165, 40, 228, 23);

        BtnSeek4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek4.setMnemonic('3');
        BtnSeek4.setToolTipText("Alt+3");
        BtnSeek4.setName("BtnSeek4"); // NOI18N
        BtnSeek4.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnSeek4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeek4ActionPerformed(evt);
            }
        });
        BtnSeek4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSeek4KeyPressed(evt);
            }
        });
        FormInput.add(BtnSeek4);
        BtnSeek4.setBounds(396, 40, 28, 23);

        label21.setText("Kab/Kota :");
        label21.setName("label21"); // NOI18N
        label21.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(label21);
        label21.setBounds(429, 10, 87, 23);

        nmkabupaten.setEditable(false);
        nmkabupaten.setName("nmkabupaten"); // NOI18N
        nmkabupaten.setPreferredSize(new java.awt.Dimension(215, 23));
        FormInput.add(nmkabupaten);
        nmkabupaten.setBounds(519, 10, 260, 23);

        BtnSeek5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek5.setMnemonic('3');
        BtnSeek5.setToolTipText("Alt+3");
        BtnSeek5.setName("BtnSeek5"); // NOI18N
        BtnSeek5.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnSeek5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeek5ActionPerformed(evt);
            }
        });
        BtnSeek5.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSeek5KeyPressed(evt);
            }
        });
        FormInput.add(BtnSeek5);
        BtnSeek5.setBounds(782, 10, 28, 23);

        label22.setText("Kecamatan :");
        label22.setName("label22"); // NOI18N
        label22.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(label22);
        label22.setBounds(429, 40, 87, 23);

        nmkecamatan.setEditable(false);
        nmkecamatan.setName("nmkecamatan"); // NOI18N
        nmkecamatan.setPreferredSize(new java.awt.Dimension(215, 23));
        FormInput.add(nmkecamatan);
        nmkecamatan.setBounds(519, 40, 260, 23);

        BtnSeek6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek6.setMnemonic('3');
        BtnSeek6.setToolTipText("Alt+3");
        BtnSeek6.setName("BtnSeek6"); // NOI18N
        BtnSeek6.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnSeek6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeek6ActionPerformed(evt);
            }
        });
        BtnSeek6.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSeek6KeyPressed(evt);
            }
        });
        FormInput.add(BtnSeek6);
        BtnSeek6.setBounds(782, 40, 28, 23);

        BtnSeek7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnSeek7.setMnemonic('3');
        BtnSeek7.setToolTipText("Alt+3");
        BtnSeek7.setName("BtnSeek7"); // NOI18N
        BtnSeek7.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnSeek7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSeek7ActionPerformed(evt);
            }
        });
        BtnSeek7.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSeek7KeyPressed(evt);
            }
        });
        FormInput.add(BtnSeek7);
        BtnSeek7.setBounds(782, 70, 28, 23);

        nmkelurahan.setEditable(false);
        nmkelurahan.setName("nmkelurahan"); // NOI18N
        nmkelurahan.setPreferredSize(new java.awt.Dimension(215, 23));
        FormInput.add(nmkelurahan);
        nmkelurahan.setBounds(519, 70, 260, 23);

        label23.setText("Kelurahan :");
        label23.setName("label23"); // NOI18N
        label23.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(label23);
        label23.setBounds(429, 70, 87, 23);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            DefaultTableModel model = modelKunjunganAktif();
            if(model.getRowCount()==0){
                JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
                return;
            }

            String pilihan = (String)JOptionPane.showInputDialog(null,"Silahkan pilih laporan..!","Pilihan Cetak",JOptionPane.QUESTION_MESSAGE,null,new Object[]{"Laporan 1 (HTML)","Laporan 2 (WPS)","Laporan 3 (CSV)","Laporan 4 (Jasper)"},"Laporan 1 (HTML)");
            if(pilihan==null){
                return;
            }

            switch (pilihan) {
                case "Laporan 1 (HTML)":
                    tulisKunjunganRanapHtml("KunjunganRanap.html",model);
                    break;
                case "Laporan 2 (WPS)":
                    tulisKunjunganRanapHtml("KunjunganRanap.wps",model);
                    break;
                case "Laporan 3 (CSV)":
                    tulisKunjunganRanapCsv("KunjunganRanap.csv",model);
                    break;
                case "Laporan 4 (Jasper)":
                    cetakKunjunganRanapJasper(model);
                    break;
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
}//GEN-LAST:event_BtnPrintActionPerformed

    private DefaultTableModel modelKunjunganAktif() {
        return TabRawat.getSelectedIndex()==0 ? tabMode : tabMode2;
    }

    /** Kolom "Pilih"/"NoRawat" di ujung (lihat konstruktor) cuma utk kebutuhan UI hapus-dari-tampilan,
     *  TIDAK boleh ikut ke Cetak/Export -- dua kolom asli terakhir sebelum itu. */
    private int jumlahKolomExport(DefaultTableModel model) {
        return model.getColumnCount() - 2;
    }

    private String csvValue(Object value) {
        if(value==null){
            return "";
        }
        return value.toString().replace("\"", "\"\"").replace("\r", " ").replace("\n", " ");
    }

    private String htmlValue(Object value) {
        if(value==null){
            return "";
        }
        return value.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String sqlValue(Object value) {
        if(value==null){
            return "";
        }
        return value.toString().replace("'", "''");
    }

    private void tulisKunjunganRanapHtml(String namaFile, DefaultTableModel model) throws Exception {
        File g = new File("file2.css");
        BufferedWriter bg = new BufferedWriter(new FileWriter(g));
        bg.write(
                ".isi td{border-right: 1px solid #e2e7dd;font: 11px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi2 td{font: 11px tahoma;height:12px;background: #ffffff;color:#323232;}"
        );
        bg.close();

        int kolomExport = jumlahKolomExport(model);
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<tr class='isi'>");
        for(int kolom=0;kolom<kolomExport;kolom++){
            htmlContent.append("<td valign='middle' bgcolor='#FFFAFA' align='center'><b>").append(model.getColumnName(kolom)).append("</b></td>");
        }
        htmlContent.append("</tr>");

        for(int baris=0;baris<model.getRowCount();baris++){
            htmlContent.append("<tr class='isi'>");
            for(int kolom=0;kolom<kolomExport;kolom++){
                htmlContent.append("<td valign='top'>").append(htmlValue(model.getValueAt(baris,kolom))).append("</td>");
            }
            htmlContent.append("</tr>");
        }

        File f = new File(namaFile);
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write("<html>"+
                    "<head><link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" /></head>"+
                    "<body>"+
                        "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                            "<tr class='isi2'>"+
                                "<td valign='top' align='center'>"+
                                    "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
                                    akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                    akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                    "<font size='2' face='Tahoma'>LAPORAN KUNJUNGAN RAWAT INAP PERIODE "+Tgl1.getSelectedItem()+" s.d. "+Tgl2.getSelectedItem()+"<br><br></font>"+
                                "</td>"+
                           "</tr>"+
                        "</table>"+
                        "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                            htmlContent.toString()+
                        "</table>"+
                    "</body>"+
                 "</html>"
        );
        bw.close();
        Desktop.getDesktop().browse(f.toURI());
    }

    private void tulisKunjunganRanapCsv(String namaFile, DefaultTableModel model) throws Exception {
        int kolomExport = jumlahKolomExport(model);
        StringBuilder csvContent = new StringBuilder("sep=;\n");
        for(int kolom=0;kolom<kolomExport;kolom++){
            if(kolom>0){
                csvContent.append(";");
            }
            csvContent.append("\"").append(csvValue(model.getColumnName(kolom))).append("\"");
        }
        csvContent.append("\n");

        for(int baris=0;baris<model.getRowCount();baris++){
            for(int kolom=0;kolom<kolomExport;kolom++){
                if(kolom>0){
                    csvContent.append(";");
                }
                csvContent.append("\"").append(csvValue(model.getValueAt(baris,kolom))).append("\"");
            }
            csvContent.append("\n");
        }

        File f = new File(namaFile);
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write(csvContent.toString());
        bw.close();
        Desktop.getDesktop().browse(f.toURI());
    }

    private void cetakKunjunganRanapJasper(DefaultTableModel model) {
        Map<String, Object> param = new HashMap<>();
        param.put("namars",akses.getnamars());
        param.put("alamatrs",akses.getalamatrs());
        param.put("kotars",akses.getkabupatenrs());
        param.put("propinsirs",akses.getpropinsirs());
        param.put("kontakrs",akses.getkontakrs());
        param.put("emailrs",akses.getemailrs());
        param.put("periode",Tgl1.getSelectedItem()+" s.d. "+Tgl2.getSelectedItem());
        param.put("lama",lama);
        param.put("baru",baru);
        param.put("total",(lama+baru));
        param.put("laki",laki);
        param.put("perempuan",per);
        param.put("tanggal",Tgl2.getDate());

        Sequel.queryu("delete from temporary where temp37='"+akses.getalamatip()+"'");
        for(int r=0;r<model.getRowCount();r++){
            if(!model.getValueAt(r,0).toString().contains(">>")){
                simpanTemporaryKunjunganRanap(model,r);
            }
        }
        Valid.MyReportqry("rptKunjunganRanap.jasper","report","::[ Laporan Kunjungan Rawat Inap ]::","select * from temporary where temporary.temp37='"+akses.getalamatip()+"' order by temporary.no",param);
    }

    private void simpanTemporaryKunjunganRanap(DefaultTableModel model, int row) {
        String tanggal = model.getValueAt(row,11).toString();
        String dpjp = model.getValueAt(row,12).toString();
        // Dulu dibedakan lewat getColumnCount()>13 (tabMode=14 kol dgn Tgl.Masuk+Tgl.Keluar,
        // tabMode2=13 kol cuma Tgl.Pulang) -- skrng kedua model sama2 nambah kolom Pilih+NoRawat
        // di belakang, jadi >13 selalu true utk keduanya. Bandingkan ke tabMode langsung spy tetap benar.
        if(model==tabMode){
            tanggal = model.getValueAt(row,11).toString()+" s.d. "+model.getValueAt(row,12).toString();
            dpjp = model.getValueAt(row,13).toString();
        }

        StringBuilder dataTemporary = new StringBuilder("'").append(row).append("'");
        for(int kolom=0;kolom<=10;kolom++){
            dataTemporary.append(",'").append(sqlValue(model.getValueAt(row,kolom))).append("'");
        }
        dataTemporary.append(",'").append(sqlValue(tanggal)).append("'");
        dataTemporary.append(",'").append(sqlValue(dpjp)).append("'");
        for(int kolom=14;kolom<=36;kolom++){
            dataTemporary.append(",''");
        }
        dataTemporary.append(",'").append(akses.getalamatip()).append("'");
        Sequel.menyimpan("temporary",dataTemporary.toString(),"Rekap Nota Pembayaran");
    }

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            //Valid.pindah(evt, BtnHapus, BtnAll);
        }
}//GEN-LAST:event_BtnPrintKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnKeluar,TKd);}
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void tbBangsalMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbBangsalMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
}//GEN-LAST:event_tbBangsalMouseClicked

    private void tbBangsalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbBangsalKeyPressed
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
}//GEN-LAST:event_tbBangsalKeyPressed

private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        if(TabRawat.getSelectedIndex()==0){
            tampil();
        }else if(TabRawat.getSelectedIndex()==1){
            tampil2();
        }
}//GEN-LAST:event_BtnCariActionPerformed

private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); 
            tampil();
            this.setCursor(Cursor.getDefaultCursor());
        }else{
            Valid.pindah(evt, TKd, BtnPrint);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }
    }//GEN-LAST:event_TCariKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
           TCari.setText("");
           kdkamar.setText("");
           nmkamar.setText("");
           kddokter.setText("");
           nmdokter.setText("");
           kdpenjab.setText("");
           nmpenjab.setText("");
           nmkabupaten.setText("");
           nmkecamatan.setText("");
           nmkelurahan.setText("");
           status="";
           if(TabRawat.getSelectedIndex()==0){
               tampil();
           }else if(TabRawat.getSelectedIndex()==1){
               tampil2();
           }
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnAllActionPerformed(null);
        }else{
            
        }
    }//GEN-LAST:event_BtnAllKeyPressed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        if(TabRawat.getSelectedIndex()==0){
            tampil();
        }else if(TabRawat.getSelectedIndex()==1){
            tampil2();
        }
    }//GEN-LAST:event_TabRawatMouseClicked

    private void tbBangsal2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbBangsal2MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tbBangsal2MouseClicked

    private void tbBangsal2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbBangsal2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbBangsal2KeyPressed

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void kdkamarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdkamarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            Sequel.cariIsi("select bangsal.nm_bangsal from bangsal where bangsal.kd_bangsal=?", nmkamar,kdkamar.getText());
        }else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            Sequel.cariIsi("select bangsal.nm_bangsal from bangsal where bangsal.kd_bangsal=?", nmkamar,kdkamar.getText());
            BtnAll.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            Sequel.cariIsi("select bangsal.nm_bangsal from bangsal where bangsal.kd_bangsal=?", nmkamar,kdkamar.getText());
            Tgl2.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            BtnSeek2ActionPerformed(null);
        }
    }//GEN-LAST:event_kdkamarKeyPressed

    private void BtnSeek2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek2ActionPerformed
        popupFilterMultiPilih("Pilih Bangsal/Ruang", "select nm_bangsal from bangsal where status='1' order by nm_bangsal", nmkamar);
    }//GEN-LAST:event_BtnSeek2ActionPerformed

    private void BtnSeek2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeek2KeyPressed
        //Valid.pindah(evt,DTPCari2,TCari);
    }//GEN-LAST:event_BtnSeek2KeyPressed

    private void kdpenjabKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdpenjabKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnAll.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            Tgl2.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            BtnSeek2ActionPerformed(null);
        }
    }//GEN-LAST:event_kdpenjabKeyPressed

    private void BtnSeek3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek3ActionPerformed
        popupFilterMultiPilih("Pilih Cara Bayar", "select png_jawab from penjab where status='1' order by png_jawab", nmpenjab);
    }//GEN-LAST:event_BtnSeek3ActionPerformed

    private void BtnSeek3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeek3KeyPressed
        //Valid.pindah(evt,DTPCari2,TCari);
    }//GEN-LAST:event_BtnSeek3KeyPressed

    private void kddokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kddokterKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_kddokterKeyPressed

    private void BtnSeek4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek4ActionPerformed
        popupFilterMultiPilih("Pilih Dokter", "select nm_dokter from dokter where status='1' order by nm_dokter", nmdokter);
    }//GEN-LAST:event_BtnSeek4ActionPerformed

    private void BtnSeek4KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeek4KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnSeek4KeyPressed

    private void BtnSeek5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek5ActionPerformed
        popupFilterMultiPilih("Pilih Kab/Kota", "select nm_kab from kabupaten order by nm_kab", nmkabupaten);
    }//GEN-LAST:event_BtnSeek5ActionPerformed

    private void BtnSeek5KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeek5KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnSeek5KeyPressed

    private void BtnSeek6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek6ActionPerformed
        popupFilterMultiPilih("Pilih Kecamatan", "select nm_kec from kecamatan order by nm_kec", nmkecamatan);
    }//GEN-LAST:event_BtnSeek6ActionPerformed

    private void BtnSeek6KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeek6KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnSeek6KeyPressed

    private void BtnSeek7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek7ActionPerformed
        popupFilterMultiPilih("Pilih Kelurahan", "select nm_kel from kelurahan order by nm_kel", nmkelurahan);
    }//GEN-LAST:event_BtnSeek7ActionPerformed

    private void BtnSeek7KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeek7KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnSeek7KeyPressed

    private void ppTampilkanBaruBtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppTampilkanBaruBtnPrintActionPerformed
        status="Baru";
        BtnCariActionPerformed(null);
    }//GEN-LAST:event_ppTampilkanBaruBtnPrintActionPerformed

    private void ppTampilkanLamaBtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppTampilkanLamaBtnPrintActionPerformed
        status="Lama";
        BtnCariActionPerformed(null);
    }//GEN-LAST:event_ppTampilkanLamaBtnPrintActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgKunjunganRanap dialog = new DlgKunjunganRanap(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button BtnAll;
    private widget.Button BtnCari;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnSeek2;
    private widget.Button BtnSeek3;
    private widget.Button BtnSeek4;
    private widget.Button BtnSeek5;
    private widget.Button BtnSeek6;
    private widget.Button BtnSeek7;
    private widget.CekBox ChkInput;
    private widget.panelisi FormInput;
    private javax.swing.JPanel PanelInput;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll2;
    private widget.TextBox TCari;
    private widget.TextBox TKd;
    private javax.swing.JTabbedPane TabRawat;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.TextBox kddokter;
    private widget.TextBox kdkamar;
    private widget.TextBox kdpenjab;
    private widget.Label label11;
    private widget.Label label17;
    private widget.Label label18;
    private widget.Label label19;
    private widget.Label label20;
    private widget.Label label21;
    private widget.Label label22;
    private widget.Label label23;
    private widget.TextBox nmdokter;
    private widget.TextBox nmkabupaten;
    private widget.TextBox nmkamar;
    private widget.TextBox nmkecamatan;
    private widget.TextBox nmkelurahan;
    private widget.TextBox nmpenjab;
    private widget.panelisi panelGlass5;
    private javax.swing.JMenuItem ppTampilkanBaru;
    private javax.swing.JMenuItem ppTampilkanLama;
    private widget.Table tbBangsal;
    private widget.Table tbBangsal2;
    // End of variables declaration//GEN-END:variables

    /** Filter Bangsal/Cara Bayar/Dokter/Kab/Kec/Kel skrng boleh pilih LEBIH DARI SATU nilai
     *  sekaligus lewat popupFilterMultiPilih() di bawah -- pola & kode sama persis dgn
     *  DlgKunjunganRalan.java, SAMA SEKALI TIDAK memakai DlgCariBangsal/DlgCariDokter/
     *  DlgCariCaraBayar/DlgKabupaten/DlgKecamatan/DlgKelurahan (kelas2 itu dipakai jg di banyak
     *  halaman lain, sengaja tidak disentuh sedikit pun). */
    private String klausaMultiLike(String kolomSql, String nilaiFilter, java.util.List<String> bindKe) {
        String[] potongan = nilaiFilter.split(";");
        StringBuilder sb = new StringBuilder("(");
        boolean adaIsi = false;
        for (String p : potongan) {
            String t = p.trim();
            if (t.isEmpty()) { continue; }
            if (adaIsi) { sb.append(" or "); }
            sb.append(kolomSql).append(" like ?");
            bindKe.add("%" + t + "%");
            adaIsi = true;
        }
        sb.append(")");
        return adaIsi ? sb.toString() : "1=1";
    }

    /** DPJP (dokterdpjp) hasil gabungan dokter penanggung jawab + dokter reg_periksa jadi 1 string,
     *  tidak praktis difilter lewat SQL (beda2 sumber per baris) -- tetap dicek di Java spt semula,
     *  tapi skrng OR lintas beberapa nama dokter (dipisah "; ") bukan cuma 1 nama. Kosong = lolos
     *  semua (sama spt perilaku lama saat filter dokter belum diisi). */
    private boolean cocokDokterDpjp(String dokterDpjp, String nilaiFilter) {
        if (nilaiFilter == null || nilaiFilter.trim().isEmpty()) { return true; }
        String dpjpLower = dokterDpjp == null ? "" : dokterDpjp.toLowerCase();
        for (String p : nilaiFilter.split(";")) {
            String t = p.trim();
            if (!t.isEmpty() && dpjpLower.contains(t.toLowerCase())) { return true; }
        }
        return false;
    }

    /** Popup centang multi-pilih (sama persis dgn DlgKunjunganRalan.java) -- centang beberapa baris
     *  sekaligus, ketik di kotak Cari utk saring daftar, checkbox "Centang Semua" centang/lepas
     *  SEMUA baris yg sedang tampil (menghormati saringan Cari), klik "Pilih" utk konfirmasi. Hasil
     *  gabungan (dipisah "; ") langsung ditulis ke `target`. */
    private void popupFilterMultiPilih(String judul, String sqlDaftar, widget.TextBox target) {
        final DefaultTableModel model = new DefaultTableModel(null, new Object[]{"Pilih","Nama"}) {
            @Override public Class<?> getColumnClass(int c) { return c==0 ? Boolean.class : String.class; }
            @Override public boolean isCellEditable(int r, int c) { return c==0; }
        };
        try (PreparedStatement st = koneksi.prepareStatement(sqlDaftar); ResultSet rsX = st.executeQuery()) {
            while (rsX.next()) { model.addRow(new Object[]{Boolean.FALSE, rsX.getString(1)}); }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat daftar: " + ex.getMessage());
            return;
        }
        String isiSekarang = target.getText();
        if (!isiSekarang.trim().isEmpty()) {
            java.util.Set<String> terpilihLama = new java.util.HashSet<>();
            for (String p : isiSekarang.split(";")) {
                String t = p.trim();
                if (!t.isEmpty()) { terpilihLama.add(t); }
            }
            for (int r = 0; r < model.getRowCount(); r++) {
                if (terpilihLama.contains(model.getValueAt(r,1).toString())) { model.setValueAt(Boolean.TRUE, r, 0); }
            }
        }

        final javax.swing.JDialog dlgPilih = new javax.swing.JDialog(this, judul, true);
        final JTable tabelPilih = new JTable(model);
        tabelPilih.setRowHeight(22);
        tabelPilih.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabelPilih.getColumnModel().getColumn(0).setMaxWidth(60);
        tabelPilih.getColumnModel().getColumn(1).setPreferredWidth(360);
        final javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(model);
        tabelPilih.setRowSorter(sorter);
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(tabelPilih);

        widget.TextBox cari = new widget.TextBox();
        cari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void terapkan() {
                String teks = cari.getText().trim();
                sorter.setRowFilter(teks.isEmpty() ? null
                        : javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(teks), 1));
            }
            @Override public void insertUpdate(DocumentEvent e) { terapkan(); }
            @Override public void removeUpdate(DocumentEvent e) { terapkan(); }
            @Override public void changedUpdate(DocumentEvent e) { terapkan(); }
        });

        javax.swing.JCheckBox chkSemua = new javax.swing.JCheckBox("Centang Semua");
        chkSemua.setOpaque(false);
        chkSemua.addActionListener(e -> {
            boolean centang = chkSemua.isSelected();
            for (int rViewIdx = 0; rViewIdx < tabelPilih.getRowCount(); rViewIdx++) {
                model.setValueAt(centang, tabelPilih.convertRowIndexToModel(rViewIdx), 0);
            }
        });

        widget.Button btnPilih = new widget.Button();
        btnPilih.setText("Pilih");
        widget.Button btnBatal = new widget.Button();
        btnBatal.setText("Batal");
        btnPilih.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            for (int r = 0; r < model.getRowCount(); r++) {
                if (Boolean.TRUE.equals(model.getValueAt(r,0))) {
                    if (sb.length() > 0) { sb.append("; "); }
                    sb.append(model.getValueAt(r,1).toString());
                }
            }
            target.setText(sb.toString());
            dlgPilih.dispose();
        });
        btnBatal.addActionListener(e -> dlgPilih.dispose());

        javax.swing.JPanel panelCari = new javax.swing.JPanel(new java.awt.BorderLayout(6,0));
        panelCari.add(new javax.swing.JLabel("Cari :"), java.awt.BorderLayout.WEST);
        panelCari.add(cari, java.awt.BorderLayout.CENTER);
        panelCari.add(chkSemua, java.awt.BorderLayout.EAST);

        javax.swing.JPanel panelTombol = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 6));
        panelTombol.add(btnPilih);
        panelTombol.add(btnBatal);

        javax.swing.JPanel panelUtama = new javax.swing.JPanel(new java.awt.BorderLayout(0,8));
        panelUtama.setBorder(new javax.swing.border.EmptyBorder(10,10,10,10));
        panelUtama.add(panelCari, java.awt.BorderLayout.NORTH);
        panelUtama.add(scroll, java.awt.BorderLayout.CENTER);
        panelUtama.add(panelTombol, java.awt.BorderLayout.SOUTH);

        dlgPilih.setContentPane(panelUtama);
        dlgPilih.setSize(460,520);
        dlgPilih.setLocationRelativeTo(this);
        dlgPilih.setVisible(true);
    }

    /** Kunjungan Ranap yg "dihapus" (disembunyikan) lewat tombol Hapus -- cuma dikecualikan dari
     *  tampilan & Cetak/Export halaman ini, data aslinya SAMA SEKALI tidak disentuh. Tabel
     *  TERPISAH dari punya Kunjungan Ralan (kunjungan_ralan_disembunyikan) krn dua halaman ini
     *  laporan yg beda konteks. */
    private boolean tabelDisembunyikanRanapSiap = false;

    private void pastikanTabelDisembunyikanRanap() {
        if (tabelDisembunyikanRanapSiap) { return; }
        try (java.sql.Statement st = koneksi.createStatement()) {
            st.executeUpdate(
                "create table if not exists kunjungan_ranap_disembunyikan ("
                + "no_rawat varchar(17) not null,"
                + "disembunyikan_oleh varchar(20) not null default '',"
                + "disembunyikan_pada datetime not null,"
                + "primary key (no_rawat)"
                + ") engine=InnoDB default charset=latin1");
            tabelDisembunyikanRanapSiap = true;
        } catch (Exception e) {
            System.out.println("Notif pastikan tabel kunjungan_ranap_disembunyikan : " + e);
        }
    }

    private static final String KLAUSA_TIDAK_DISEMBUNYIKAN_RANAP =
        "not exists (select 1 from kunjungan_ranap_disembunyikan krd where krd.no_rawat=reg_periksa.no_rawat)";

    /** Tombol "Hapus" ditaruh di panelGlass5 (baris Tanggal/Cetak/Keluar), sebelah Cetak -- sama
     *  persis pola dgn DlgKunjunganRalan.java. */
    private void pasangTombolHapusTampilan() {
        widget.Button btnHapus = new widget.Button();
        btnHapus.setText("Hapus");
        btnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png")));
        btnHapus.setPreferredSize(new java.awt.Dimension(100, 30));
        btnHapus.setToolTipText("<html>Sembunyikan pasien yg dicentang dari halaman ini (termasuk dari<br/>"
                + "Cetak &amp; Export). Data pasien di database TIDAK dihapus/berubah.</html>");
        btnHapus.addActionListener(e -> {
            if (TabRawat.getSelectedIndex() == 1) {
                hapusTerpilih(tbBangsal2, tabMode2, 13, 14);
            } else {
                hapusTerpilih(tbBangsal, tabMode, 14, 15);
            }
        });
        int posisiKeluar = panelGlass5.getComponentZOrder(BtnKeluar);
        panelGlass5.add(btnHapus, posisiKeluar < 0 ? panelGlass5.getComponentCount() : posisiKeluar);
        panelGlass5.revalidate();
        panelGlass5.repaint();
    }

    /** Centang di kolom "Pilih" -> disembunyikan permanen dari halaman ini (lewat
     *  kunjungan_ranap_disembunyikan), baris langsung dibuang dari tabel yg sedang tampil. Baris
     *  ringkasan ("&gt;&gt;") tidak punya no_rawat (kolom NoRawat kosong) jadi otomatis dilewati.
     *  Data asli pasien SAMA SEKALI tidak disentuh. */
    private void hapusTerpilih(JTable table, DefaultTableModel model, int kolomPilih, int kolomNoRawat) {
        java.util.List<Integer> baris = new java.util.ArrayList<>();
        java.util.List<String> noRawatTerpilih = new java.util.ArrayList<>();
        for (int r = 0; r < model.getRowCount(); r++) {
            if (Boolean.TRUE.equals(model.getValueAt(r, kolomPilih))) {
                Object nr = model.getValueAt(r, kolomNoRawat);
                if (nr != null && !nr.toString().trim().isEmpty()) {
                    baris.add(r);
                    noRawatTerpilih.add(nr.toString());
                }
            }
        }
        if (baris.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Centang dulu pasien yang mau disembunyikan dari daftar ini.");
            return;
        }
        int jawab = JOptionPane.showConfirmDialog(this,
                "Sembunyikan " + baris.size() + " pasien terpilih dari halaman Kunjungan Ranap ini?\n"
                + "Data pasien di database TIDAK dihapus/berubah -- cuma tidak ikut tampil, Cetak, dan Export\n"
                + "di halaman ini (akan tetap tersembunyi walau halaman dibuka ulang).",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (jawab != JOptionPane.YES_OPTION) { return; }
        pastikanTabelDisembunyikanRanap();
        for (String nr : noRawatTerpilih) {
            try (PreparedStatement ps3 = koneksi.prepareStatement(
                    "insert ignore into kunjungan_ranap_disembunyikan(no_rawat,disembunyikan_oleh,disembunyikan_pada) values(?,?,now())")) {
                ps3.setString(1, nr);
                ps3.setString(2, akses.getkode());
                ps3.executeUpdate();
            } catch (Exception e) {
                System.out.println("Notif sembunyikan kunjungan ranap : " + e);
            }
        }
        for (int idx = baris.size() - 1; idx >= 0; idx--) {
            model.removeRow(baris.get(idx));
        }
    }

    public void tampil(){
        try{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Valid.tabelKosong(tabMode);
            pastikanTabelDisembunyikanRanap();
            // Bangsal/Cara Bayar/Kab/Kec/Kel skrng boleh multi-nilai (klausaMultiLike, dipisah ";"
            // dari popupFilterMultiPilih) -- fragmen dihitung SEKALI (bindUnit), dipakai ulang di
            // tiap 5 blok OR TCari di bawah (sama pola spt DlgKunjunganRalan, cuma di sini perlu
            // diulang krn kueri lama memang berbentuk 5 blok OR per target TCari yg berbeda).
            java.util.List<String> bindUnit = new java.util.ArrayList<>();
            String klBangsal = klausaMultiLike("bangsal.nm_bangsal", nmkamar.getText(), bindUnit);
            String klPenjab = klausaMultiLike("penjab.png_jawab", nmpenjab.getText(), bindUnit);
            String klKab = klausaMultiLike("kabupaten.nm_kab", nmkabupaten.getText(), bindUnit);
            String klKec = klausaMultiLike("kecamatan.nm_kec", nmkecamatan.getText(), bindUnit);
            String klKel = klausaMultiLike("kelurahan.nm_kel", nmkelurahan.getText(), bindUnit);
            String tglAwal = Valid.SetTgl(Tgl1.getSelectedItem()+"");
            String tglAkhir = Valid.SetTgl(Tgl2.getSelectedItem()+"");
            String tcariNilai = "%"+TCari.getText().trim()+"%";
            String[] targetTCari = {"pasien.alamat","pasien.nm_pasien","dokter.nm_dokter","reg_periksa.no_rkm_medis","kamar_inap.kd_kamar"};
            java.util.List<String> bind = new java.util.ArrayList<>();
            StringBuilder sqlOr = new StringBuilder();
            for (int t = 0; t < targetTCari.length; t++) {
                if (t > 0) { sqlOr.append(" or "); }
                sqlOr.append("reg_periksa.stts_daftar like '%").append(status).append("%' and reg_periksa.status_lanjut='Ranap' and reg_periksa.stts<>'Batal' and kamar_inap.stts_pulang<>'Pindah Kamar' and ")
                        .append(KLAUSA_TIDAK_DISEMBUNYIKAN_RANAP)
                        .append(" and reg_periksa.tgl_registrasi between ? and ? and ").append(klBangsal)
                        .append(" and ").append(klPenjab).append(" and ").append(klKab).append(" and ").append(klKec)
                        .append(" and ").append(klKel).append(" and ").append(targetTCari[t]).append(" like ?");
                bind.add(tglAwal); bind.add(tglAkhir); bind.addAll(bindUnit); bind.add(tcariNilai);
            }
            ps=koneksi.prepareStatement(
                    "select reg_periksa.no_rawat,reg_periksa.tgl_registrasi,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.alamat,pasien.jk,concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,pasien.tgl_daftar,reg_periksa.stts_daftar,"+
                    "kamar_inap.kd_kamar,bangsal.nm_bangsal,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab)as almt_pj,kamar_inap.stts_pulang,kamar_inap.tgl_masuk,kamar_inap.tgl_keluar,dokter.nm_dokter "+
                    "from reg_periksa inner join pasien inner join kamar_inap inner join kamar inner join bangsal inner join dokter inner join penjab " +
                    "inner join kabupaten inner join kecamatan inner join kelurahan on reg_periksa.no_rkm_medis=pasien.no_rkm_medis and reg_periksa.no_rawat=kamar_inap.no_rawat "+
                    "and reg_periksa.kd_pj=penjab.kd_pj and pasien.kd_kab=kabupaten.kd_kab and kamar_inap.kd_kamar=kamar.kd_kamar and kamar.kd_bangsal=bangsal.kd_bangsal "+
                    "and reg_periksa.kd_dokter=dokter.kd_dokter and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kel=kelurahan.kd_kel where ("+
                    sqlOr+") "+
                    "group by reg_periksa.no_rawat order by reg_periksa.tgl_registrasi");
            try {
                for (int b = 0; b < bind.size(); b++) { ps.setString(b+1, bind.get(b)); }
                rs=ps.executeQuery();
                i=1;   
                lama=0;baru=0;laki=0;per=0;
                while(rs.next()){
                    dokterdpjp=rs.getString("nm_dokter");
                    try{
                        ps2=koneksi.prepareStatement("select dokter.nm_dokter from dpjp_ranap inner join dokter "+
                            "on dpjp_ranap.kd_dokter=dokter.kd_dokter where dpjp_ranap.no_rawat=? ");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            rs2=ps2.executeQuery();                    
                            while(rs2.next()){
                                dokterdpjp=rs2.getString("nm_dokter")+", "+dokterdpjp;
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }
                    } catch(Exception e){
                        System.out.println("Notifikasi : "+e);
                    }
                    
                    if(cocokDokterDpjp(dokterdpjp, nmdokter.getText())){
                        setbaru="";
                        setlama="";
                        if(rs.getString("stts_daftar").equals("Baru")){
                            setbaru=rs.getString("no_rkm_medis");
                            baru++;
                        }else if(rs.getString("stts_daftar").equals("Lama")){
                            setlama=rs.getString("no_rkm_medis");
                            lama++;
                        }
                        umurlk="";
                        umurpr="";
                        switch (rs.getString("jk")) {
                            case "L":
                                umurlk=rs.getString("umur");
                                laki++;
                                break;
                            case "P":
                                umurpr=rs.getString("umur");
                                per++;
                                break;
                        }
                        diagnosa="";
                        kddiagnosa="";
                        ps2=koneksi.prepareStatement(
                                "select penyakit.kd_penyakit,penyakit.nm_penyakit from penyakit inner join diagnosa_pasien " +
                                "on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit " +
                                "where diagnosa_pasien.no_rawat=? order by prioritas asc limit 1");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            rs2=ps2.executeQuery();
                            if(rs2.next()){
                                kddiagnosa=rs2.getString(1);
                                diagnosa=rs2.getString(2);
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }

                        tabMode.addRow(new Object[]{
                            i,setlama,setbaru,rs.getString("nm_pasien"),umurlk,umurpr,rs.getString("almt_pj"),kddiagnosa,diagnosa,rs.getString("kd_kamar")+" "+rs.getString("nm_bangsal"),rs.getString("stts_pulang"),rs.getString("tgl_masuk"),rs.getString("tgl_keluar"),dokterdpjp,
                            Boolean.FALSE,rs.getString("no_rawat")
                        });
                        i++;
                    }

                }
                if(i>=2){
                    tabMode.addRow(new Object[]{
                        ">>",lama,baru,"",laki,per,"","","","","","","","",Boolean.FALSE,""
                    });
                }
            } catch (Exception e) {
                System.out.println("Notifikasi : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }    
            this.setCursor(Cursor.getDefaultCursor());
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }

    public void tampil2(){
        try{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Valid.tabelKosong(tabMode2);
            pastikanTabelDisembunyikanRanap();
            java.util.List<String> bindUnit2 = new java.util.ArrayList<>();
            String klBangsal2 = klausaMultiLike("bangsal.nm_bangsal", nmkamar.getText(), bindUnit2);
            String klPenjab2 = klausaMultiLike("penjab.png_jawab", nmpenjab.getText(), bindUnit2);
            String klKab2 = klausaMultiLike("kabupaten.nm_kab", nmkabupaten.getText(), bindUnit2);
            String klKec2 = klausaMultiLike("kecamatan.nm_kec", nmkecamatan.getText(), bindUnit2);
            String klKel2 = klausaMultiLike("kelurahan.nm_kel", nmkelurahan.getText(), bindUnit2);
            String tglAwal2 = Valid.SetTgl(Tgl1.getSelectedItem()+"");
            String tglAkhir2 = Valid.SetTgl(Tgl2.getSelectedItem()+"");
            String tcariNilai2 = "%"+TCari.getText().trim()+"%";
            String[] targetTCari2 = {"dokter.nm_dokter","pasien.alamat","pasien.nm_pasien","reg_periksa.no_rkm_medis","kamar_inap.kd_kamar"};
            java.util.List<String> bind2 = new java.util.ArrayList<>();
            StringBuilder sqlOr2 = new StringBuilder();
            for (int t = 0; t < targetTCari2.length; t++) {
                if (t > 0) { sqlOr2.append(" or "); }
                sqlOr2.append("reg_periksa.stts_daftar like '%").append(status).append("%' and kamar_inap.stts_pulang<>'Pindah Kamar' and ")
                        .append(KLAUSA_TIDAK_DISEMBUNYIKAN_RANAP)
                        .append(" and kamar_inap.tgl_keluar between ? and ? and ").append(klBangsal2)
                        .append(" and ").append(klPenjab2).append(" and ").append(klKab2).append(" and ").append(klKec2)
                        .append(" and ").append(klKel2).append(" and ").append(targetTCari2[t]).append(" like ?");
                bind2.add(tglAwal2); bind2.add(tglAkhir2); bind2.addAll(bindUnit2); bind2.add(tcariNilai2);
            }
            ps=koneksi.prepareStatement(
                    "select reg_periksa.no_rawat,reg_periksa.tgl_registrasi,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.alamat,pasien.jk,concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,pasien.tgl_daftar,reg_periksa.stts_daftar,"+
                    "kamar_inap.kd_kamar,bangsal.nm_bangsal,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab)as almt_pj,kamar_inap.stts_pulang,kamar_inap.tgl_keluar,dokter.nm_dokter "+
                    "from reg_periksa inner join pasien inner join kamar_inap inner join kamar inner join bangsal inner join dokter inner join penjab " +
                    "inner join kabupaten inner join kecamatan inner join kelurahan on reg_periksa.no_rkm_medis=pasien.no_rkm_medis and reg_periksa.no_rawat=kamar_inap.no_rawat and "+
                    "kamar_inap.kd_kamar=kamar.kd_kamar and kamar.kd_bangsal=bangsal.kd_bangsal and reg_periksa.kd_pj=penjab.kd_pj and pasien.kd_kab=kabupaten.kd_kab "+
                    "and reg_periksa.kd_dokter=dokter.kd_dokter and pasien.kd_kec=kecamatan.kd_kec and pasien.kd_kel=kelurahan.kd_kel where ("+
                    sqlOr2+") "+
                    "group by reg_periksa.no_rawat order by kamar_inap.tgl_keluar");
            try {
                for (int b = 0; b < bind2.size(); b++) { ps.setString(b+1, bind2.get(b)); }
                rs=ps.executeQuery();
                i=1;   
                lama=0;baru=0;laki=0;per=0;
                while(rs.next()){
                    dokterdpjp=rs.getString("nm_dokter");
                    try{
                        ps2=koneksi.prepareStatement("select dokter.nm_dokter from dpjp_ranap inner join dokter "+
                            "on dpjp_ranap.kd_dokter=dokter.kd_dokter where dpjp_ranap.no_rawat=? ");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            rs2=ps2.executeQuery();                    
                            while(rs2.next()){
                                dokterdpjp=rs2.getString("nm_dokter")+", "+dokterdpjp;
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : "+e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }
                    } catch(Exception e){
                        System.out.println("Notifikasi : "+e);
                    }
                    if(cocokDokterDpjp(dokterdpjp, nmdokter.getText())){
                        setbaru="";
                        setlama="";
                        if(rs.getString("stts_daftar").equals("Baru")){
                            setbaru=rs.getString("no_rkm_medis");
                            baru++;
                        }else if(rs.getString("stts_daftar").equals("Lama")){
                            setlama=rs.getString("no_rkm_medis");
                            lama++;
                        }
                        umurlk="";
                        umurpr="";
                        switch (rs.getString("jk")) {
                            case "L":
                                umurlk=rs.getString("umur");
                                laki++;
                                break;
                            case "P":
                                umurpr=rs.getString("umur");
                                per++;
                                break;
                        }
                        diagnosa="";
                        kddiagnosa="";
                        ps2=koneksi.prepareStatement(
                                "select penyakit.kd_penyakit,penyakit.nm_penyakit from penyakit inner join diagnosa_pasien " +
                                "on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit " +
                                "where diagnosa_pasien.no_rawat=? order by prioritas asc limit 1");
                        try {
                            ps2.setString(1,rs.getString("no_rawat"));
                            rs2=ps2.executeQuery();
                            if(rs2.next()){
                                kddiagnosa=rs2.getString(1);
                                diagnosa=rs2.getString(2);
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                        } finally{
                            if(rs2!=null){
                                rs2.close();
                            }
                            if(ps2!=null){
                                ps2.close();
                            }
                        }
                        tabMode2.addRow(new Object[]{
                            i,setlama,setbaru,rs.getString("nm_pasien"),umurlk,umurpr,rs.getString("almt_pj"),kddiagnosa,diagnosa,rs.getString("kd_kamar")+" "+rs.getString("nm_bangsal"),rs.getString("stts_pulang"),rs.getString("tgl_keluar"),dokterdpjp,
                            Boolean.FALSE,rs.getString("no_rawat")
                        });
                        i++;
                    }
                }
                if(i>=2){
                    tabMode2.addRow(new Object[]{
                        ">>",lama,baru,"",laki,per,"","","","","","","",Boolean.FALSE,""
                    });
                }
            } catch (Exception e) {
                System.out.println("Notifikasi : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }    
            this.setCursor(Cursor.getDefaultCursor());
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }
    
    private void getData() {
        int row=tbBangsal.getSelectedRow();
        if(row!= -1){
            TKd.setText(tabMode.getValueAt(row,0).toString());
        }
    }
    
    private void isForm(){
        if(ChkInput.isSelected()==true){
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH,126));
            FormInput.setVisible(true);      
            ChkInput.setVisible(true);
        }else if(ChkInput.isSelected()==false){           
            ChkInput.setVisible(false);            
            PanelInput.setPreferredSize(new Dimension(WIDTH,20));
            FormInput.setVisible(false);      
            ChkInput.setVisible(true);
        }
    }

}
