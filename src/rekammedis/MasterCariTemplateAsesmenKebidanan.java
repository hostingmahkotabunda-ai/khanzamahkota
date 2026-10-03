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
 * Dialog pencari Template Asesmen Kebidanan. Pola persis MasterCariTemplateAsuhanGizi:
 * daftar template di kiri, preview di kanan, tombol Tambah membuka master pengelola
 * template. Template yang dipilih dibaca pemanggil lewat getTable().
 *
 * Mencakup SEMUA field narasi/teks bebas di form Asesmen Kebidanan (semua 8 bagian) --
 * field angka/pengukuran (TTV, GPA, skor Morse, hasil lab, dll) dan field checkbox/combo
 * SENGAJA tidak di-template karena nilainya spesifik per pasien saat itu juga, bukan
 * sesuatu yang masuk akal "dipakai ulang". Daftar & urutan FIELDS ini jadi acuan tunggal,
 * dipakai juga oleh MasterTemplateAsesmenKebidanan dan RMAsesmenKebidanan -- kalau
 * menambah/mengubah field di sini, sesuaikan juga array target di RMAsesmenKebidanan.
 */
public final class MasterCariTemplateAsesmenKebidanan extends javax.swing.JDialog {

    /** {nama_kolom_db, label tampilan} -- urutan ini SAMA dgn urutan field di form asli. */
    public static final String[][] FIELDS = {
        {"keluhan_utama", "Keluhan Utama"},
        {"alergi_makanan_obat", "Alergi - Makanan / Obat"},
        {"alergi_reaksi", "Alergi - Jenis Reaksi"},
        {"nyeri_lokasi", "Nyeri - Lokasi"},
        {"nyeri_onset", "Nyeri - Onset"},
        {"nyeri_variasi", "Nyeri - Variasi"},
        {"nyeri_obat", "Nyeri - Obat-obatan"},
        {"agama_nilai_keyakinan", "Agama / Nilai Keyakinan"},
        {"riwayat_operasi", "Riwayat Penyakit Lalu / Operasi"},
        {"kb_metode", "KB - Metode"},
        {"kb_lama", "KB - Lama"},
        {"pola_makan", "Pola Makan"},
        {"pola_minum", "Pola Minum"},
        {"pola_konsumsi", "Alkohol / Obat / Jamu / Kopi"},
        {"bak_warna", "BAK - Warna"},
        {"bab_karakteristik", "BAB - Karakteristik"},
        {"nilai_keyakinan", "Nilai & Keyakinan"},
        {"penerimaan_kehamilan", "Penerimaan Klien thd Kehamilan"},
        {"obs_letak_punggung", "Letak Punggung"},
        {"obs_presentasi", "Presentasi"},
        {"obs_bagian_terendah", "Bagian Terendah"},
        {"gyn_vagina", "Inspekulo Vagina"},
        {"gyn_portio", "Portio"},
        {"gyn_vt", "Vagina Toucher"},
        {"gyn_kesan_panggul", "Kesan Panggul"},
        {"gyn_imbang", "Imbang Feto Pelvic"},
        {"nifas_lochea", "Lochea"},
        {"nifas_luka", "Luka Jalan Lahir"},
        {"diagnosa_kebidanan", "Diagnosa Kebidanan & Masalah"},
        {"rencana_kebidanan", "Rencana Kebidanan"}
    };

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

    public MasterCariTemplateAsesmenKebidanan(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        pastikanTabelTemplate();
        initComponents();
        this.setLocation(10,2);
        setSize(656,250);

        Object[] row=new Object[2+FIELDS.length];
        row[0]="Kode"; row[1]="Nama Template";
        for(int i=0;i<FIELDS.length;i++){ row[2+i]=FIELDS[i][1]; }
        tabMode=new DefaultTableModel(row,0){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbKamar.setModel(tabMode);
        tbKamar.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbKamar.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < tabMode.getColumnCount(); i++) {
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

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Cari Template Asesmen Kebidanan ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50)));
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
        MasterTemplateAsesmenKebidanan form=new MasterTemplateAsesmenKebidanan(null,false);
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
                    StringBuilder sb=new StringBuilder();
                    for(int i=0;i<FIELDS.length;i++){
                        String isi=nilai(r,2+i);
                        if(isi.trim().equals("")){ continue; }
                        sb.append(FIELDS[i][1].toUpperCase()).append(" :\n").append(isi).append("\n\n");
                    }
                    Template.setText(sb.toString());
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
            StringBuilder kolom=new StringBuilder("kode,nama_template");
            for(String[] f:FIELDS){ kolom.append(',').append(f[0]); }
            String keyword=TCari.getText().trim();
            StringBuilder sql=new StringBuilder("select ").append(kolom).append(" from template_asesmen_kebidanan ");
            if(!keyword.equals("")){
                sql.append("where kode like ? or nama_template like ?");
                for(String[] f:FIELDS){ sql.append(" or ").append(f[0]).append(" like ?"); }
                sql.append(' ');
            }
            sql.append("order by kode");
            ps=koneksi.prepareStatement(sql.toString());
            try {
                if(!keyword.equals("")){
                    int total=2+FIELDS.length;
                    for(int i=1;i<=total;i++){ ps.setString(i,"%"+keyword+"%"); }
                }
                rs=ps.executeQuery();
                while(rs.next()){
                    Object[] baris=new Object[2+FIELDS.length];
                    baris[0]=rs.getString("kode");
                    baris[1]=rs.getString("nama_template");
                    for(int i=0;i<FIELDS.length;i++){ baris[2+i]=rs.getString(FIELDS[i][0]); }
                    tabMode.addRow(baris);
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
        BtnTambah.setEnabled(akses.getpenilaian_awal_keperawatan_ranap() || akses.getbooking_operasi());
    }

    /** Membuat tabel template_asesmen_kebidanan kalau belum ada, kolomnya dibangun dari FIELDS. */
    private void pastikanTabelTemplate(){
        try{
            StringBuilder sql=new StringBuilder("create table if not exists template_asesmen_kebidanan(");
            sql.append("kode varchar(15) not null,nama_template varchar(150),");
            for(String[] f:FIELDS){ sql.append(f[0]).append(" text,"); }
            sql.append("primary key(kode)) engine=InnoDB default charset=utf8");
            ps=koneksi.prepareStatement(sql.toString());
            ps.executeUpdate();
            ps.close();
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }
}
