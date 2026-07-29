package inventory;
import fungsi.WarnaTable;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class DlgCopyResep extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private Connection koneksi=koneksiDB.condb();
    private PreparedStatement ps,ps2,ps3;
    private ResultSet rs,rs2,rs3;
    private String aktifkanparsial="no",norm="",kddokter="",kode_pj="",norawat="",status="";
    private final Properties prop = new Properties();
    private int jmlparsial=0;
    private final DefaultListModel<ResepRingkas> modelDaftarResep = new DefaultListModel<>();
    private final JList<ResepRingkas> daftarResep = new JList<>(modelDaftarResep);
    private final DefaultTableModel modelDetail = new DefaultTableModel(
            new Object[]{"Nama Obat / Racikan","Kode","Jumlah","Satuan","Aturan Pakai"},0){
        @Override public boolean isCellEditable(int row, int column){ return false; }
    };
    private final JTable tabelDetail = new JTable(modelDetail);
    private final JTextField txtCariModern = new JTextField();
    private final JComboBox<String> cmbSumberModern = new JComboBox<>(
            new String[]{"Semua Sumber","IGD","Rawat Jalan","Rawat Inap"});
    private final JLabel lblJudulDetail = new JLabel("Pilih resep untuk melihat detail");
    private final JLabel lblMetaDetail = new JLabel(" ");
    private final JLabel lblCatatanDetail = new JLabel("Tidak ada catatan tambahan.");
    private final JLabel lblPasienModern = new JLabel(" ");
    
    /** Creates new form 
     * @param parent
     * @param modal */
    public DlgCopyResep(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        Object[] row={"No.Resep","Tgl.Resep","Jam Resep","No.Rawat","No.RM","Pasien","Dokter Peresep","Kode Dokter","Status"};
        tabMode=new DefaultTableModel(null,row){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbPemisahan.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbPemisahan.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbPemisahan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < 9; i++) {
            TableColumn column = tbPemisahan.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(75);
            }else if(i==1){
                column.setPreferredWidth(65);
            }else if(i==2){
                column.setPreferredWidth(60);
            }else if(i==3){
                column.setPreferredWidth(170);
            }else if(i==4){
                column.setPreferredWidth(90);
            }else if(i==5){
                column.setPreferredWidth(300);
            }else if(i==6){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==7){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==8){
                column.setPreferredWidth(85);
            }
        }
        tbPemisahan.setDefaultRenderer(Object.class, new WarnaTable());
        modernisasiTampilan();
        
        try {
            prop.loadFromXML(new FileInputStream("setting/database.xml"));
            aktifkanparsial=prop.getProperty("AKTIFKANBILLINGPARSIAL");
        } catch (Exception ex) {
            aktifkanparsial="no";
        }

    }

    private void modernisasiTampilan(){
        Color utama=new Color(0,133,143);
        Color latar=new Color(246,249,251);
        Color garis=new Color(214,224,230);
        internalFrame1.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(garis),
                " Riwayat Resep Kunjungan Sebelumnya ",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Tahoma",Font.BOLD,13),utama));
        internalFrame1.remove(scrollPane1);
        internalFrame1.remove(panelisi1);

        JPanel atas=new JPanel(new BorderLayout(10,8));
        atas.setBackground(latar);
        atas.setBorder(new EmptyBorder(8,10,8,10));
        JPanel judul=new JPanel(new BorderLayout());
        judul.setOpaque(false);
        JLabel title=new JLabel("Riwayat Resep Kunjungan Sebelumnya");
        title.setFont(new Font("Tahoma",Font.BOLD,18));
        title.setForeground(new Color(31,47,62));
        JLabel sub=new JLabel("Pilih resep untuk disalin atau diperbarui");
        sub.setFont(new Font("Tahoma",Font.PLAIN,11));
        sub.setForeground(new Color(88,103,114));
        JPanel teksJudul=new JPanel(new GridLayout(2,1));
        teksJudul.setOpaque(false);
        teksJudul.add(title);
        teksJudul.add(sub);
        judul.add(teksJudul,BorderLayout.WEST);
        lblPasienModern.setHorizontalAlignment(SwingConstants.RIGHT);
        lblPasienModern.setFont(new Font("Tahoma",Font.BOLD,12));
        lblPasienModern.setForeground(new Color(31,47,62));
        judul.add(lblPasienModern,BorderLayout.EAST);
        atas.add(judul,BorderLayout.NORTH);

        JPanel filter=new JPanel(new FlowLayout(FlowLayout.LEFT,6,4));
        filter.setBackground(Color.WHITE);
        filter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis),new EmptyBorder(5,6,5,6)));
        ChkTanggal.setText("Tanggal Resep");
        ChkTanggal.setPreferredSize(new Dimension(105,26));
        DTPCari1.setPreferredSize(new Dimension(105,26));
        DTPCari2.setPreferredSize(new Dimension(105,26));
        BtnCari.setText("Terapkan");
        BtnCari.setPreferredSize(new Dimension(95,28));
        txtCariModern.setPreferredSize(new Dimension(260,28));
        txtCariModern.setToolTipText("Cari nomor resep, dokter, atau obat");
        cmbSumberModern.setPreferredSize(new Dimension(130,28));
        filter.add(ChkTanggal);
        filter.add(DTPCari1);
        filter.add(new JLabel("s.d"));
        filter.add(DTPCari2);
        filter.add(BtnCari);
        filter.add(new JLabel("  Cari :"));
        filter.add(txtCariModern);
        filter.add(cmbSumberModern);
        atas.add(filter,BorderLayout.CENTER);
        internalFrame1.add(atas,BorderLayout.NORTH);

        daftarResep.setCellRenderer(new RendererResep());
        daftarResep.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        daftarResep.setFixedCellHeight(112);
        daftarResep.setBackground(latar);
        daftarResep.addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()){
                tampilkanDetailTerpilih();
            }
        });

        JPanel kiri=new JPanel(new BorderLayout());
        kiri.setBackground(Color.WHITE);
        JLabel judulKiri=new JLabel("Daftar Resep");
        judulKiri.setFont(new Font("Tahoma",Font.BOLD,13));
        judulKiri.setBorder(new EmptyBorder(9,10,8,8));
        kiri.add(judulKiri,BorderLayout.NORTH);
        JScrollPane scrollDaftar=new JScrollPane(daftarResep);
        scrollDaftar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,garis));
        kiri.add(scrollDaftar,BorderLayout.CENTER);

        JPanel kanan=new JPanel(new BorderLayout(0,8));
        kanan.setBackground(Color.WHITE);
        kanan.setBorder(new EmptyBorder(8,10,8,10));
        JPanel kepalaDetail=new JPanel(new GridLayout(2,1,0,3));
        kepalaDetail.setOpaque(false);
        lblJudulDetail.setFont(new Font("Tahoma",Font.BOLD,14));
        lblJudulDetail.setForeground(new Color(31,47,62));
        lblMetaDetail.setFont(new Font("Tahoma",Font.PLAIN,11));
        lblMetaDetail.setForeground(new Color(72,88,99));
        kepalaDetail.add(lblJudulDetail);
        kepalaDetail.add(lblMetaDetail);
        kanan.add(kepalaDetail,BorderLayout.NORTH);
        tabelDetail.setRowHeight(27);
        tabelDetail.setFillsViewportHeight(true);
        tabelDetail.getTableHeader().setFont(new Font("Tahoma",Font.BOLD,11));
        kanan.add(new JScrollPane(tabelDetail),BorderLayout.CENTER);
        JPanel catatan=new JPanel(new BorderLayout());
        catatan.setBackground(new Color(244,248,255));
        catatan.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(160,190,230)),
                new EmptyBorder(7,9,7,9)));
        JLabel labelCatatan=new JLabel("Catatan Dokter");
        labelCatatan.setFont(new Font("Tahoma",Font.BOLD,11));
        catatan.add(labelCatatan,BorderLayout.NORTH);
        lblCatatanDetail.setFont(new Font("Tahoma",Font.ITALIC,11));
        catatan.add(lblCatatanDetail,BorderLayout.CENTER);
        kanan.add(catatan,BorderLayout.SOUTH);

        JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,kiri,kanan);
        split.setResizeWeight(0.36);
        split.setDividerLocation(390);
        split.setBorder(new EmptyBorder(0,8,0,8));
        internalFrame1.add(split,BorderLayout.CENTER);

        JPanel bawah=new JPanel(new FlowLayout(FlowLayout.RIGHT,7,8));
        bawah.setBackground(Color.WHITE);
        bawah.setBorder(BorderFactory.createMatteBorder(1,0,0,0,garis));
        BtnTambah.setText("Salin Resep Terpilih");
        BtnTambah.setPreferredSize(new Dimension(155,32));
        BtnEdit.setText("Ubah");
        BtnHapus.setText("Hapus");
        bawah.add(BtnHapus);
        bawah.add(BtnEdit);
        bawah.add(BtnKeluar);
        bawah.add(BtnTambah);
        internalFrame1.add(bawah,BorderLayout.SOUTH);

        txtCariModern.getDocument().addDocumentListener(new DocumentListener(){
            @Override public void insertUpdate(DocumentEvent e){ muatTampilanModern(); }
            @Override public void removeUpdate(DocumentEvent e){ muatTampilanModern(); }
            @Override public void changedUpdate(DocumentEvent e){ muatTampilanModern(); }
        });
        cmbSumberModern.addActionListener(e -> muatTampilanModern());
        internalFrame1.revalidate();
        internalFrame1.repaint();
    }

    private void muatTampilanModern(){
        String cari=txtCariModern.getText()==null ? "" : txtCariModern.getText().trim().toLowerCase();
        String filter=cmbSumberModern.getSelectedItem()==null ? "Semua Sumber" : cmbSumberModern.getSelectedItem().toString();
        modelDaftarResep.clear();
        for(int i=0;i<tabMode.getRowCount();i++){
            String no=nilaiTabel(i,0);
            if(no.equals("")){
                continue;
            }
            String sumber="Rawat Jalan";
            String catatan="";
            int item=0;
            StringBuilder obat=new StringBuilder();
            for(int j=i+1;j<tabMode.getRowCount() && nilaiTabel(j,0).equals("");j++){
                if(nilaiTabel(j,1).equals("Catatan")){
                    catatan=nilaiTabel(j,5);
                }else if(nilaiTabel(j,1).equals("Jumlah")){
                    if(!nilaiTabel(j,8).equals("")){
                        sumber=nilaiTabel(j,8);
                    }
                }else if(!nilaiTabel(j,5).trim().equals("")){
                    item++;
                    obat.append(' ').append(nilaiTabel(j,5));
                }
            }
            ResepRingkas r=new ResepRingkas(i,no,nilaiTabel(i,1),nilaiTabel(i,2),
                    nilaiTabel(i,3),nilaiTabel(i,4),nilaiTabel(i,5),nilaiTabel(i,6),
                    nilaiTabel(i,8),sumber,item,catatan);
            String gabung=(r.noResep+" "+r.dokter+" "+r.pasien+" "+obat).toLowerCase();
            if((filter.equals("Semua Sumber") || filter.equals(r.sumber)) &&
                    (cari.equals("") || gabung.contains(cari))){
                modelDaftarResep.addElement(r);
            }
        }
        if(!modelDaftarResep.isEmpty()){
            daftarResep.setSelectedIndex(0);
            ResepRingkas awal=modelDaftarResep.getElementAt(0);
            lblPasienModern.setText(awal.pasien+"  •  No. RM "+awal.noRm);
        }else{
            lblPasienModern.setText("Tidak ada resep");
            kosongkanDetail();
        }
    }

    private void tampilkanDetailTerpilih(){
        ResepRingkas resep=daftarResep.getSelectedValue();
        if(resep==null){
            kosongkanDetail();
            return;
        }
        tbPemisahan.setRowSelectionInterval(resep.barisAsli,resep.barisAsli);
        modelDetail.setRowCount(0);
        String catatan=resep.catatan;
        for(int i=resep.barisAsli+1;i<tabMode.getRowCount() && nilaiTabel(i,0).equals("");i++){
            if(nilaiTabel(i,1).equals("Catatan")){
                catatan=nilaiTabel(i,5);
            }else if(!nilaiTabel(i,1).equals("Jumlah") && !nilaiTabel(i,5).trim().equals("")){
                modelDetail.addRow(new Object[]{
                    nilaiTabel(i,5),nilaiTabel(i,4),nilaiTabel(i,1),
                    nilaiTabel(i,2),nilaiTabel(i,3)
                });
            }
        }
        lblJudulDetail.setText("Detail Resep "+resep.noResep+"  •  "+resep.sumber+"  •  "+resep.statusLayanan);
        lblMetaDetail.setText("Dokter Peresep: "+resep.dokter+"   |   Tanggal: "+
                resep.tanggal+" "+resep.jam+"   |   No. Rawat: "+resep.noRawat);
        lblCatatanDetail.setText(catatan.trim().equals("") ? "Tidak ada catatan tambahan." : catatan);
    }

    private void kosongkanDetail(){
        modelDetail.setRowCount(0);
        lblJudulDetail.setText("Pilih resep untuk melihat detail");
        lblMetaDetail.setText(" ");
        lblCatatanDetail.setText("Tidak ada catatan tambahan.");
    }

    private String nilaiTabel(int row,int col){
        Object nilai=tabMode.getValueAt(row,col);
        return nilai==null ? "" : nilai.toString();
    }

    private String sumberResep(String statusAsal,String noRawat){
        if("ranap".equalsIgnoreCase(statusAsal)){
            return "Rawat Inap";
        }
        String poli=Sequel.cariIsi(
                "select ifnull(poliklinik.nm_poli,'') from reg_periksa "+
                "left join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "+
                "where reg_periksa.no_rawat=?",noRawat);
        String nama=poli==null ? "" : poli.toUpperCase();
        if(nama.contains("IGD") || nama.contains("UGD") || nama.contains("GAWAT DARURAT")){
            return "IGD";
        }
        return "Rawat Jalan";
    }

    private static final class ResepRingkas{
        final int barisAsli,item;
        final String noResep,tanggal,jam,noRawat,noRm,pasien,dokter,statusLayanan,sumber,catatan;
        ResepRingkas(int barisAsli,String noResep,String tanggal,String jam,String noRawat,
                String noRm,String pasien,String dokter,String statusLayanan,String sumber,
                int item,String catatan){
            this.barisAsli=barisAsli;
            this.noResep=noResep;
            this.tanggal=tanggal;
            this.jam=jam;
            this.noRawat=noRawat;
            this.noRm=noRm;
            this.pasien=pasien;
            this.dokter=dokter;
            this.statusLayanan=statusLayanan;
            this.sumber=sumber;
            this.item=item;
            this.catatan=catatan;
        }
    }

    private static final class RendererResep extends JPanel implements ListCellRenderer<ResepRingkas>{
        private final JLabel badge=new JLabel();
        private final JLabel utama=new JLabel();
        private final JLabel dokter=new JLabel();
        private final JLabel meta=new JLabel();
        RendererResep(){
            setLayout(new BorderLayout(8,3));
            setBorder(new EmptyBorder(7,8,7,8));
            badge.setOpaque(true);
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setPreferredSize(new Dimension(82,78));
            badge.setFont(new Font("Tahoma",Font.BOLD,10));
            JPanel teks=new JPanel(new GridLayout(3,1));
            teks.setOpaque(false);
            utama.setFont(new Font("Tahoma",Font.BOLD,12));
            dokter.setFont(new Font("Tahoma",Font.BOLD,11));
            meta.setFont(new Font("Tahoma",Font.PLAIN,10));
            teks.add(utama);
            teks.add(dokter);
            teks.add(meta);
            add(badge,BorderLayout.WEST);
            add(teks,BorderLayout.CENTER);
        }
        @Override public Component getListCellRendererComponent(JList<? extends ResepRingkas> list,
                ResepRingkas value,int index,boolean isSelected,boolean cellHasFocus){
            Color aksen;
            if("IGD".equals(value.sumber)){
                aksen=new Color(239,108,0);
            }else if("Rawat Inap".equals(value.sumber)){
                aksen=new Color(112,61,170);
            }else{
                aksen=new Color(0,137,150);
            }
            badge.setText("<html><center>"+value.sumber.toUpperCase().replace(" ","<br>")+"</center></html>");
            badge.setBackground(aksen);
            badge.setForeground(Color.WHITE);
            utama.setText("Resep "+value.noResep+"  •  "+value.item+" item");
            dokter.setText(value.dokter);
            meta.setText(value.tanggal+" "+value.jam+"  •  "+value.statusLayanan);
            setBackground(isSelected ? campurPutih(aksen) : Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0,5,1,1,aksen),
                    new EmptyBorder(7,8,7,8)));
            return this;
        }
        private static Color campurPutih(Color c){
            return new Color((c.getRed()+255*5)/6,(c.getGreen()+255*5)/6,(c.getBlue()+255*5)/6);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        internalFrame1 = new widget.InternalFrame();
        panelisi1 = new widget.panelisi();
        ChkTanggal = new widget.CekBox();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        BtnCari = new widget.Button();
        jLabel7 = new widget.Label();
        BtnHapus = new widget.Button();
        BtnTambah = new widget.Button();
        BtnEdit = new widget.Button();
        BtnKeluar = new widget.Button();
        scrollPane1 = new widget.ScrollPane();
        tbPemisahan = new widget.Table();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Daftar Resep Dokter Di Kunjungan Sebelumnya ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50,50,50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelisi1.setName("panelisi1"); // NOI18N
        panelisi1.setPreferredSize(new java.awt.Dimension(55, 55));
        panelisi1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        ChkTanggal.setText("Tgl.Resep :");
        ChkTanggal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        ChkTanggal.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ChkTanggal.setName("ChkTanggal"); // NOI18N
        ChkTanggal.setOpaque(false);
        ChkTanggal.setPreferredSize(new java.awt.Dimension(90, 23));
        ChkTanggal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ChkTanggalItemStateChanged(evt);
            }
        });
        panelisi1.add(ChkTanggal);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "27-02-2019" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(90, 23));
        panelisi1.add(DTPCari1);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d");
        jLabel21.setName("jLabel21"); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(24, 23));
        panelisi1.add(jLabel21);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "27-02-2019" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelisi1.add(DTPCari2);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('1');
        BtnCari.setToolTipText("Alt+1");
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
        panelisi1.add(BtnCari);

        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(20, 23));
        panelisi1.add(jLabel7);

        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        BtnHapus.setMnemonic('H');
        BtnHapus.setText("Hapus");
        BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setName("BtnHapus"); // NOI18N
        BtnHapus.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnHapusActionPerformed(evt);
            }
        });
        BtnHapus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnHapusKeyPressed(evt);
            }
        });
        panelisi1.add(BtnHapus);

        BtnTambah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/editcopy.png"))); // NOI18N
        BtnTambah.setMnemonic('S');
        BtnTambah.setText("Copy");
        BtnTambah.setToolTipText("Alt+S");
        BtnTambah.setName("BtnTambah"); // NOI18N
        BtnTambah.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTambahActionPerformed(evt);
            }
        });
        BtnTambah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnTambahKeyPressed(evt);
            }
        });
        panelisi1.add(BtnTambah);

        BtnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        BtnEdit.setMnemonic('U');
        BtnEdit.setText("Ubah");
        BtnEdit.setToolTipText("Alt+U");
        BtnEdit.setName("BtnEdit"); // NOI18N
        BtnEdit.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnEditActionPerformed(evt);
            }
        });
        BtnEdit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnEditKeyPressed(evt);
            }
        });
        panelisi1.add(BtnEdit);

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
        panelisi1.add(BtnKeluar);

        internalFrame1.add(panelisi1, java.awt.BorderLayout.PAGE_END);

        scrollPane1.setName("scrollPane1"); // NOI18N
        scrollPane1.setOpaque(true);

        tbPemisahan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tbPemisahan.setName("tbPemisahan"); // NOI18N
        tbPemisahan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbPemisahanMouseClicked(evt);
            }
        });
        tbPemisahan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbPemisahanKeyPressed(evt);
            }
        });
        scrollPane1.setViewportView(tbPemisahan);

        internalFrame1.add(scrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        tampil();
}//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            tampil();
        }else{
            Valid.pindah(evt, DTPCari1,BtnKeluar);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    private void tbPemisahanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbPemisahanMouseClicked
        if(tabMode.getRowCount()!=0){
            if(evt.getClickCount()==2){
                if(akses.getberi_obat()==true){
                    BtnTambahActionPerformed(null);
                }
            }
        }
}//GEN-LAST:event_tbPemisahanMouseClicked

    private void tbPemisahanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbPemisahanKeyPressed
        if(tabMode.getRowCount()!=0){
            if(evt.getKeyCode()==KeyEvent.VK_SPACE){
                if(akses.getberi_obat()==true){
                    BtnTambahActionPerformed(null);
                }                    
            }
        }
}//GEN-LAST:event_tbPemisahanKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
            dispose();  
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){            
            dispose();              
        }else{Valid.pindah(evt,DTPCari1,BtnTambah);}
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTambahActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
        }else if(tbPemisahan.getSelectedRow()<= -1){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan pilih data resep dokter..!!");
        }else{
            if(tbPemisahan.getValueAt(tbPemisahan.getSelectedRow(),0).toString().equals("")){
                JOptionPane.showMessageDialog(rootPane,"Silahkan pilih No.Resep..!!");
            }else {
                jmlparsial=0;
                if(aktifkanparsial.equals("yes")){
                    jmlparsial=Sequel.cariInteger("select count(set_input_parsial.kd_pj) from set_input_parsial where set_input_parsial.kd_pj=?",kode_pj);
                }
                if(jmlparsial>0){
                    panggilform();
                }else{
                    if(Sequel.cariRegistrasi(norawat)>0){
                        JOptionPane.showMessageDialog(rootPane,"Data billing sudah terverifikasi ..!!");
                    }else{ 
                        panggilform();                             
                    }
                }                
            }
        }
}//GEN-LAST:event_BtnTambahActionPerformed

    private void BtnTambahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnTambahKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnTambahActionPerformed(null);
        }else{
           Valid.pindah(evt,DTPCari1,BtnKeluar);
        }
}//GEN-LAST:event_BtnTambahKeyPressed
/*
private void KdKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TKdKeyPressed
    Valid.pindah(evt,BtnCari,Nm);
}//GEN-LAST:event_TKdKeyPressed
*/

    private void ChkTanggalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_ChkTanggalItemStateChanged
        tampil();
    }//GEN-LAST:event_ChkTanggalItemStateChanged

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
        }else if(tbPemisahan.getSelectedRow()<= -1){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan pilih data resep dokter..!!");
        }else{
            if(tbPemisahan.getValueAt(tbPemisahan.getSelectedRow(),0).toString().equals("")){
                JOptionPane.showMessageDialog(rootPane,"Silahkan pilih No.Resep ..!!");
            }else {
                jmlparsial=0;
                if(aktifkanparsial.equals("yes")){
                    jmlparsial=Sequel.cariInteger("select count(set_input_parsial.kd_pj) from set_input_parsial where set_input_parsial.kd_pj=?",kode_pj);
                }
                if(jmlparsial>0){
                    panggilform2();
                }else{
                    if(Sequel.cariRegistrasi(tbPemisahan.getValueAt(tbPemisahan.getSelectedRow(),3).toString())>0){
                        JOptionPane.showMessageDialog(rootPane,"Data billing sudah terverifikasi ..!!");
                    }else{ 
                        panggilform2();                             
                    }
                }                
            }
        }
    }//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnEditActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnTambah, BtnKeluar);
        }
    }//GEN-LAST:event_BtnEditKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
        }else if(tbPemisahan.getSelectedRow()<= -1){
            JOptionPane.showMessageDialog(null,"Maaf, Silahkan pilih data resep dokter..!!");
        }else{
            if(tbPemisahan.getValueAt(tbPemisahan.getSelectedRow(),0).toString().equals("")){
                JOptionPane.showMessageDialog(rootPane,"Silahkan pilih No.Resep..!!");
            }else {
                Sequel.meghapus("resep_obat","no_resep",tbPemisahan.getValueAt(tbPemisahan.getSelectedRow(),0).toString()); 
                tampil();               
            }
        }
    }//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnHapusActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnHapus, BtnEdit);
        }
    }//GEN-LAST:event_BtnHapusKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgCopyResep dialog = new DlgCopyResep(new javax.swing.JFrame(), true);
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
    private widget.Button BtnCari;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnTambah;
    private widget.CekBox ChkTanggal;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel21;
    private widget.Label jLabel7;
    private widget.panelisi panelisi1;
    private widget.ScrollPane scrollPane1;
    private widget.Table tbPemisahan;
    // End of variables declaration//GEN-END:variables

    public void tampil() {
        Valid.tabelKosong(tabMode);
        try{  
            if(ChkTanggal.isSelected()==true){
                ps=koneksi.prepareStatement("select resep_obat.no_resep,resep_obat.tgl_peresepan,resep_obat.jam_peresepan,"+
                    " resep_obat.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,resep_obat.kd_dokter,dokter.nm_dokter, "+
                    " if(resep_obat.tgl_perawatan='0000-00-00','Belum Terlayani','Sudah Terlayani') as status,resep_obat.status as status_asal "+
                    " from resep_obat inner join reg_periksa inner join pasien inner join dokter on resep_obat.no_rawat=reg_periksa.no_rawat  "+
                    " and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and resep_obat.kd_dokter=dokter.kd_dokter where "+
                    " resep_obat.tgl_peresepan<>'0000-00-00' and resep_obat.tgl_peresepan between ? and ? and resep_obat.no_rawat=? order by resep_obat.tgl_perawatan,resep_obat.jam desc");
            }else{
                ps=koneksi.prepareStatement("select resep_obat.no_resep,resep_obat.tgl_peresepan,resep_obat.jam_peresepan,"+
                    " resep_obat.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,resep_obat.kd_dokter,dokter.nm_dokter, "+
                    " if(resep_obat.tgl_perawatan='0000-00-00','Belum Terlayani','Sudah Terlayani') as status,resep_obat.status as status_asal "+
                    " from resep_obat inner join reg_periksa inner join pasien inner join dokter on resep_obat.no_rawat=reg_periksa.no_rawat  "+
                    " and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and resep_obat.kd_dokter=dokter.kd_dokter where "+
                    " resep_obat.tgl_peresepan<>'0000-00-00' and resep_obat.no_rawat=? order by resep_obat.tgl_perawatan,resep_obat.jam desc");
            }
            try{
                if(ChkTanggal.isSelected()==true){
                    ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
                    ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
                    ps.setString(3,norawat);
                }else{
                    ps.setString(1,norawat);
                }
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new String[]{
                        rs.getString("no_resep"),rs.getString("tgl_peresepan"),rs.getString("jam_peresepan"),rs.getString("no_rawat"),
                        rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),rs.getString("nm_dokter"),rs.getString("kd_dokter"),
                        rs.getString("status")
                    });
                    String catatanResep=Sequel.cariIsi("select catatan from catatan_resep_dokter where no_resep=?",rs.getString("no_resep"));
                    if(catatanResep!=null && !catatanResep.trim().equals("")){
                        tabMode.addRow(new String[]{"","Catatan","","","",catatanResep,"","",""});
                    }  
                    tabMode.addRow(new String[]{"","Jumlah","Satuan","Aturan Pakai","Kode/No","Nama Obat/Racikan","","",
                        sumberResep(rs.getString("status_asal"),rs.getString("no_rawat"))});
                    ps2=koneksi.prepareStatement("select databarang.kode_brng,databarang.nama_brng,resep_dokter.jml,"+
                        "databarang.kode_sat,resep_dokter.aturan_pakai from resep_dokter inner join databarang on "+
                        "resep_dokter.kode_brng=databarang.kode_brng where resep_dokter.no_resep=? order by databarang.kode_brng");
                    try {
                        ps2.setString(1,rs.getString("no_resep"));
                        rs2=ps2.executeQuery();
                        while(rs2.next()){
                            tabMode.addRow(new String[]{
                                "",rs2.getString("jml"),rs2.getString("kode_sat"),rs2.getString("aturan_pakai"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),"","",""
                            });
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi 2 : "+e);
                    } finally{
                        if(rs2!=null){
                            rs2.close();
                        }
                        if(ps2!=null){
                            ps2.close();
                        }
                    }
                    ps2=koneksi.prepareStatement(
                            "select resep_dokter_racikan.no_racik,resep_dokter_racikan.nama_racik,"+
                            "resep_dokter_racikan.kd_racik,metode_racik.nm_racik as metode,"+
                            "resep_dokter_racikan.jml_dr,resep_dokter_racikan.aturan_pakai,"+
                            "resep_dokter_racikan.keterangan from resep_dokter_racikan inner join metode_racik "+
                            "on resep_dokter_racikan.kd_racik=metode_racik.kd_racik where "+
                            "resep_dokter_racikan.no_resep=? ");
                    try {
                        ps2.setString(1,rs.getString("no_resep"));
                        rs2=ps2.executeQuery();
                        while(rs2.next()){
                            tabMode.addRow(new String[]{
                                "",rs2.getString("jml_dr"),rs2.getString("metode"),rs2.getString("aturan_pakai"),"No.Racik : "+rs2.getString("no_racik"),rs2.getString("nama_racik"),"","",""
                            });
                            ps3=koneksi.prepareStatement("select databarang.kode_brng,databarang.nama_brng,resep_dokter_racikan_detail.jml,"+
                                "databarang.kode_sat from resep_dokter_racikan_detail inner join databarang on resep_dokter_racikan_detail.kode_brng=databarang.kode_brng "+
                                "where resep_dokter_racikan_detail.no_resep=? and resep_dokter_racikan_detail.no_racik=? order by databarang.kode_brng");
                            try {
                                ps3.setString(1,rs.getString("no_resep"));
                                ps3.setString(2,rs2.getString("no_racik"));
                                rs3=ps3.executeQuery();
                                while(rs3.next()){
                                    tabMode.addRow(new String[]{
                                        "","   "+rs3.getString("jml"),"   "+rs3.getString("kode_sat"),"","   "+rs3.getString("kode_brng"),"   "+rs3.getString("nama_brng"),"","","",""
                                    });
                                }
                            } catch (Exception e) {
                                System.out.println("Notifikasi 3 : "+e);
                            } finally{
                                if(rs3!=null){
                                    rs3.close();
                                }
                                if(ps3!=null){
                                    ps3.close();
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi 2 : "+e);
                    } finally{
                        if(rs2!=null){
                            rs2.close();
                        }
                        if(ps2!=null){
                            ps2.close();
                        }
                    }
                }                
            } catch(Exception ex){
                System.out.println("Notifikasi : "+ex);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }                
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        muatTampilanModern();
    }

    public void isCek(){
        BtnTambah.setEnabled(akses.getresep_dokter());
    }
    
    public void setRM(String norawat,String norm,String kodedokter,String kodepj,String status){
        this.norm=norm;
        this.status=status;
        this.norawat=norawat;
        this.kddokter=kodedokter;
        this.kode_pj=kodepj;
    }

    private void panggilform() {
        DlgPeresepanDokter resep=DlgPeresepanDokter.buatDari(this);
        resep.setSize(internalFrame1.getWidth(),internalFrame1.getHeight());
        resep.setLocationRelativeTo(internalFrame1);
        resep.setNoRm(norawat,tbPemisahan.getValueAt(tbPemisahan.getSelectedRow(),7).toString(),
                tbPemisahan.getValueAt(tbPemisahan.getSelectedRow(),6).toString(), 
                tbPemisahan.getValueAt(tbPemisahan.getSelectedRow(),4).toString()+" "+
                tbPemisahan.getValueAt(tbPemisahan.getSelectedRow(),5).toString(), 
                kode_pj,status);
        resep.isCek();
        resep.tampilobat2(tbPemisahan.getValueAt(tbPemisahan.getSelectedRow(),0).toString());
        resep.setVisible(true);
    }
    
    private void panggilform2() {
        DlgPeresepanDokter resep=DlgPeresepanDokter.buatDari(this);
        resep.setSize(internalFrame1.getWidth(),internalFrame1.getHeight());
        resep.setLocationRelativeTo(internalFrame1);
        resep.MatikanJam();
        resep.setNoRm(tbPemisahan.getValueAt(tbPemisahan.getSelectedRow(),3).toString(),
                Valid.SetTgl2(tbPemisahan.getValueAt(tbPemisahan.getSelectedRow(),1).toString()),
                tbPemisahan.getValueAt(tbPemisahan.getSelectedRow(),2).toString().substring(0,2),
                tbPemisahan.getValueAt(tbPemisahan.getSelectedRow(),2).toString().substring(3,5),
                tbPemisahan.getValueAt(tbPemisahan.getSelectedRow(),2).toString().substring(6,8),
                tbPemisahan.getValueAt(tbPemisahan.getSelectedRow(),7).toString(),
                tbPemisahan.getValueAt(tbPemisahan.getSelectedRow(),6).toString(),status);
        resep.isCek();
        resep.tampilobat(tbPemisahan.getValueAt(tbPemisahan.getSelectedRow(),0).toString());
        resep.setVisible(true);   
    }
}
