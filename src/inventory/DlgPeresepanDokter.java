/*
  Dilarang keras menggandakan/mengcopy/menyebarkan/membajak/mendecompile 
  Software ini dalam bentuk apapun tanpa seijin pembuat software
  (Khanza.Soft Media). Bagi yang sengaja membajak softaware ini ta
  npa ijin, kami sumpahi sial 1000 turunan, miskin sampai 500 turu
  nan. Selalu mendapat kecelakaan sampai 400 turunan. Anak pertama
  nya cacat tidak punya kaki sampai 300 turunan. Susah cari jodoh
  sampai umur 50 tahun sampai 200 turunan. Ya Alloh maafkan kami 
  karena telah berdoa buruk, semua ini kami lakukan karena kami ti
  dak pernah rela karya kami dibajak tanpa ijin.
 */

package inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.WarnaTable2;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;
import widget.Button;

/**
 *
 * @author dosen
 */
public final class DlgPeresepanDokter extends javax.swing.JDialog {
    private final DefaultTableModel tabModeResep,tabModeDetailResepRacikan,tabModeResepRacikan;
    private DefaultTableModel tabModeResepRacikanV2,tabModeDetailResepRacikanV2;
    private DefaultTableModel tabModeTindakan;
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private Connection koneksi=koneksiDB.condb();
    private PreparedStatement psresep,pscarikapasitas,psresepasuransi,ps2;
    private ResultSet rsobat,carikapasitas,rs2;
    private double y=0,kenaikan=0,ttl=0,ppnobat=0,jumlahracik=0,persenracik=0,kapasitasracik=0;
    private int i=0,z=0,row2=0,r=0;
    private boolean ubah=false,copy=false,sukses=true;
    private WarnaTable2 warna=new WarnaTable2();
    private WarnaTable2 warna2=new WarnaTable2();
    private WarnaTable2 warna3=new WarnaTable2();
    private DlgCariDokter dokter=new DlgCariDokter(null,false);
    private String noracik="",aktifkanbatch="no",STOKKOSONGRESEP="no",qrystokkosong="",tampilkan_ppnobat_ralan="",status="",bangsal="",resep="",DEPOAKTIFOBAT="",
            kamar="",norawatibu="",kelas,bangsaldefault=Sequel.cariIsi("select set_lokasi.kd_bangsal from set_lokasi limit 1"),RESEPRAJALKEPLAN="no";
    private File file;
    private FileWriter fileWriter;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private JsonNode response;
    private FileReader myObj;
    private String TANGGALMUNDUR="yes";
    private String draftTerapiSOAP="";
    private String planSOAPDokter="";
    private String catatanResepDokter="";
    private List<String[]> pilihanMetodeRacikV2=new ArrayList<>();
    private List<String> pilihanAturanPakaiV2=new ArrayList<>();
    private boolean draftResepSOAPTableChecked=false;
    private boolean draftTerapiSOAPSudahDiterapkan=false;
    private boolean resepBerhasilDisimpan=false;
    private boolean sedangSinkronCatatanDokter=false;
    private String noResepTersimpan="";
    private javax.swing.JPanel panelUmumResep;
    private javax.swing.JPanel panelRacikanResep;
    private javax.swing.JPanel panelDraftSOAP;
    private javax.swing.JPanel panelDraftSOAPRacikan;
    private javax.swing.JPanel panelDraftSOAPRacikanV2;
    private javax.swing.JScrollPane scrollDraftSOAP;
    private javax.swing.JScrollPane scrollDraftSOAPRacikan;
    private javax.swing.JScrollPane scrollDraftSOAPRacikanV2;
    private javax.swing.JTextArea areaDraftSOAP;
    private javax.swing.JTextArea areaDraftSOAPRacikan;
    private javax.swing.JTextArea areaDraftSOAPRacikanV2;
    private javax.swing.JTextArea areaCatatanDokter;
    private javax.swing.JTextArea areaCatatanDokterRacikanV2;
    private widget.ScrollPane scrollCatatanDokter;
    private widget.ScrollPane scrollCatatanDokterRacikanV2;
    private widget.Label labelCatatanDokter;
    private widget.Label labelCatatanDokterRacikanV2;
    private javax.swing.JPanel panelTindakanPasien;
    private javax.swing.JPanel panelTindakanPasienRacikan;
    private javax.swing.JPanel panelTindakanPasienRacikanV2;
    private widget.ScrollPane scrollTindakanPasien;
    private widget.ScrollPane scrollTindakanPasienRacikan;
    private widget.ScrollPane scrollTindakanPasienRacikanV2;
    private widget.Table tbTindakanPasien;
    private widget.Table tbTindakanPasienRacikan;
    private widget.Table tbTindakanPasienRacikanV2;
    private widget.Label labelTindakanPasien;
    private widget.Label labelTindakanPasienRacikan;
    private widget.Label labelTindakanPasienRacikanV2;
    private widget.Label labelDraftSOAP;
    private widget.Label labelDraftSOAPRacikan;
    private widget.Label labelDraftSOAPRacikanV2;
    private javax.swing.JPanel panelRacikanV2;
    private widget.Table tbRacikanV2;
    private widget.Table tbDetailRacikanV2;
    private widget.Label labelRacikanAktifV2;
    private boolean modeEmbedded=false;
    private final widget.Button BtnPaketResep = new widget.Button();
    private static final Pattern POLA_JUMLAH_RESEP=Pattern.compile("(?i)(?:\\b(?:qty|jumlah|jml|jlh|no\\.?|#)\\s*[:=]?\\s*)(\\d+(?:[\\.,]\\d+)?)");
    private static final Pattern POLA_AWAL_LIST=Pattern.compile("^\\s*(?:[-*]+|\\d+[\\.)])\\s*");

    public static DlgPeresepanDokter buatDari(java.awt.Component parent) {
        java.awt.Window owner = parent instanceof java.awt.Window ? (java.awt.Window)parent : javax.swing.SwingUtilities.getWindowAncestor(parent);
        if(owner instanceof java.awt.Dialog){
            return new DlgPeresepanDokter((java.awt.Dialog)owner,false);
        }else if(owner instanceof java.awt.Frame){
            return new DlgPeresepanDokter((java.awt.Frame)owner,false);
        }
        return new DlgPeresepanDokter((java.awt.Frame)null,false);
    }
    /** Creates new form DlgPenyakit
     * @param parent
     * @param modal */
    public DlgPeresepanDokter(java.awt.Frame parent, boolean modal) {
        this((java.awt.Window)parent,modal);
    }

    public DlgPeresepanDokter(java.awt.Dialog parent, boolean modal) {
        this((java.awt.Window)parent,modal);
    }

    private DlgPeresepanDokter(java.awt.Window parent, boolean modal) {
        super(parent);
        setModalityType(modal ? java.awt.Dialog.ModalityType.APPLICATION_MODAL : java.awt.Dialog.ModalityType.MODELESS);
        initComponents();
        inisialisasiPanelTindakanPasien();
        inisialisasiPanelDraftSOAP();
        pasangTombolPaketResep();
        this.setLocation(10,2);
        setSize(656,250);
        tabModeResep=new DefaultTableModel(null,new Object[]{
                "K","Jumlah","Aturan Pakai","Kode Barang","Nama Barang","Satuan",
                "Komposisi","Harga(Rp)","Jenis Obat","I.F.","H.Beli","Stok"
            }){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if ((colIndex==0)||(colIndex==1)||(colIndex==2)) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                java.lang.Boolean.class, java.lang.Object.class,java.lang.Object.class, java.lang.Object.class, 
                java.lang.Object.class,java.lang.Object.class, java.lang.Object.class, java.lang.Double.class, 
                java.lang.Object.class,java.lang.Object.class,java.lang.Double.class,java.lang.Double.class
             };
             /*Class[] types = new Class[] {
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
             };*/
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbResep.setModel(tabModeResep);
        //tbPenyakit.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbPenyakit.getBackground()));
        tbResep.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbResep.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (i = 0; i < 12; i++) {
            TableColumn column = tbResep.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(20);
            }else if(i==1){
                column.setPreferredWidth(45);
            }else if(i==2){
                column.setPreferredWidth(130);
            }else if(i==3){
                column.setPreferredWidth(70);
            }else if(i==4){
                column.setPreferredWidth(240);
            }else if(i==5){
                column.setPreferredWidth(75);
            }else if(i==6){
                column.setPreferredWidth(110);
            }else if(i==7){
                column.setPreferredWidth(85);
            }else if(i==8){
                column.setPreferredWidth(110);
            }else if(i==9){
                column.setPreferredWidth(100);
            }else if(i==10){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==11){
                column.setPreferredWidth(50);
            }                 
        }
        warna.kolom=1;
        tbResep.setDefaultRenderer(Object.class,warna);
        
        tabModeResepRacikan=new DefaultTableModel(null,new Object[]{
                "No","Nama Racikan","Kode Racik","Metode Racik","Jml.Racik",
                "Aturan Pakai","Keterangan"
            }){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = true;
                if ((colIndex==0)||(colIndex==2)||(colIndex==3)) {
                    a=false;
                }
                return a;
             }
             Class[] types = new Class[] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, 
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
             };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };

        tbObatResepRacikan.setModel(tabModeResepRacikan);
        tbObatResepRacikan.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObatResepRacikan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);        
        
        for (i = 0; i < 7; i++) {
            TableColumn column = tbObatResepRacikan.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(25);
            }else if(i==1){
                column.setPreferredWidth(250);
            }else if(i==2){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==3){
                column.setPreferredWidth(100);
            }else if(i==4){
                column.setPreferredWidth(60);
            }else if(i==5){
                column.setPreferredWidth(200);
            }else if(i==6){
                column.setPreferredWidth(250);
            }
        }

        warna2.kolom=4;
        tbObatResepRacikan.setDefaultRenderer(Object.class,warna2);
        
        tabModeDetailResepRacikan=new DefaultTableModel(null,new Object[]{
                "No","Kode Barang","Nama Barang","Satuan","Harga(Rp)","H.Beli",
                "Jenis Obat","Stok","Kps","P1","/","P2","Kandungan","Jml","I.F.",
                "Komposisi"
            }){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if ((colIndex==9)||(colIndex==11)||(colIndex==12)||(colIndex==13)) {
                    a=true;
                }
                return a;
             }             
             Class[] types = new Class[] {
                java.lang.Object.class,java.lang.Object.class,java.lang.Object.class,
                java.lang.Object.class,java.lang.Double.class,java.lang.Double.class,
                java.lang.Object.class,java.lang.Double.class,java.lang.Double.class,
                java.lang.Double.class,java.lang.Object.class,java.lang.Double.class,
                java.lang.Object.class,java.lang.Double.class,java.lang.Object.class,
                java.lang.Object.class
             };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };

        tbDetailResepObatRacikan.setModel(tabModeDetailResepRacikan);
        tbDetailResepObatRacikan.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbDetailResepObatRacikan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);        
        
        for (i = 0; i < 16; i++) {
            TableColumn column = tbDetailResepObatRacikan.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(25);
            }else if(i==1){
                column.setPreferredWidth(75);
            }else if(i==2){
                column.setPreferredWidth(240);
            }else if(i==3){
                column.setPreferredWidth(45);
            }else if(i==4){
                column.setPreferredWidth(85);
            }else if(i==5){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==6){
                column.setPreferredWidth(110);
            }else if(i==7){
                column.setPreferredWidth(50);
            }else if(i==8){
                column.setPreferredWidth(40);
            }else if(i==9){
                column.setPreferredWidth(25);
            }else if(i==10){
                column.setMinWidth(11);
                column.setMaxWidth(11);
            }else if(i==11){
                column.setPreferredWidth(25);
            }else if(i==12){
                column.setPreferredWidth(60);
            }else if(i==13){
                column.setPreferredWidth(40);
            }else if(i==14){
                column.setPreferredWidth(100);
            }else if(i==15){
                column.setPreferredWidth(150);
            }
        }

        warna3.kolom=9;
        tbDetailResepObatRacikan.setDefaultRenderer(Object.class,warna3);
        inisialisasiRacikanV2();
        sembunyikanTabRacikanLama();
        
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        BtnCariActionPerformed(null);
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        BtnCariActionPerformed(null);
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        BtnCariActionPerformed(null);
                    }
                }
            });
        }
        
        dokter.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(dokter.getTable().getSelectedRow()!= -1){        
                     KdDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                     NmDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());
                }  
                KdDokter.requestFocus();
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
        
        jam();
        
        tampilkan_ppnobat_ralan=Sequel.cariIsi("select set_nota.tampilkan_ppnobat_ralan from set_nota"); 
        
        try {
            aktifkanbatch = koneksiDB.AKTIFKANBATCHOBAT();
            STOKKOSONGRESEP = koneksiDB.STOKKOSONGRESEP();
        } catch (Exception e) {
            System.out.println("E : "+e);
            aktifkanbatch = "no";
            STOKKOSONGRESEP="no";
        }
        
        try {
            DEPOAKTIFOBAT = koneksiDB.DEPOAKTIFOBAT();
        } catch (Exception e) {
            System.out.println("E : "+e);
            DEPOAKTIFOBAT = "";
        }
        
        try {
            RESEPRAJALKEPLAN=koneksiDB.RESEPRAJALKEPLAN();
        } catch (Exception e) {
            RESEPRAJALKEPLAN="no";
        }
        
//        try {
//            TANGGALMUNDUR=koneksiDB.TANGGALMUNDUR();
//        } catch (Exception e) {
//            TANGGALMUNDUR="yes";
//        }
    }    
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Popup = new javax.swing.JPopupMenu();
        ppBersihkan = new javax.swing.JMenuItem();
        ppStok1 = new javax.swing.JMenuItem();
        KdPj = new widget.TextBox();
        LPpn = new widget.Label();
        jLabel6 = new widget.Label();
        internalFrame1 = new widget.InternalFrame();
        panelisi3 = new widget.panelisi();
        label9 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        label12 = new widget.Label();
        Jeniskelas = new widget.ComboBox();
        BtnTambah = new widget.Button();
        BtnSeek5 = new widget.Button();
        BtnSimpan = new widget.Button();
        BtnTambah1 = new widget.Button();
        BtnHapus = new widget.Button();
        BtnKeluar = new widget.Button();
        FormInput = new widget.PanelBiasa();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        KdDokter = new widget.TextBox();
        NmDokter = new widget.TextBox();
        jLabel3 = new widget.Label();
        jLabel13 = new widget.Label();
        btnDokter = new widget.Button();
        jLabel11 = new widget.Label();
        NoResep = new widget.TextBox();
        jLabel8 = new widget.Label();
        DTPBeri = new widget.Tanggal();
        cmbJam = new widget.ComboBox();
        cmbMnt = new widget.ComboBox();
        cmbDtk = new widget.ComboBox();
        ChkRM = new widget.CekBox();
        ChkJln = new widget.CekBox();
        jLabel5 = new widget.Label();
        LTotal = new widget.Label();
        jLabel7 = new widget.Label();
        LTotalTagihan = new widget.Label();
        jButton1 = new javax.swing.JButton();
        TabRawat = new javax.swing.JTabbedPane();
        Scroll = new widget.ScrollPane();
        tbResep = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        Scroll1 = new widget.ScrollPane();
        tbObatResepRacikan = new widget.Table();
        Scroll2 = new widget.ScrollPane();
        tbDetailResepObatRacikan = new widget.Table();

        Popup.setName("Popup"); // NOI18N

        ppBersihkan.setBackground(new java.awt.Color(255, 255, 254));
        ppBersihkan.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppBersihkan.setForeground(new java.awt.Color(50, 50, 50));
        ppBersihkan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppBersihkan.setText("Bersihkan Jumlah");
        ppBersihkan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppBersihkan.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppBersihkan.setName("ppBersihkan"); // NOI18N
        ppBersihkan.setPreferredSize(new java.awt.Dimension(180, 25));
        ppBersihkan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppBersihkanActionPerformed(evt);
            }
        });
        Popup.add(ppBersihkan);

        ppStok1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppStok1.setForeground(new java.awt.Color(50, 50, 50));
        ppStok1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppStok1.setText("Cek Stok Lokasi");
        ppStok1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppStok1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppStok1.setName("ppStok1"); // NOI18N
        ppStok1.setPreferredSize(new java.awt.Dimension(180, 25));
        ppStok1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppStok1ActionPerformed(evt);
            }
        });
        Popup.add(ppStok1);

        KdPj.setHighlighter(null);
        KdPj.setName("KdPj"); // NOI18N

        LPpn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LPpn.setText("0");
        LPpn.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        LPpn.setName("LPpn"); // NOI18N
        LPpn.setPreferredSize(new java.awt.Dimension(65, 23));

        jLabel6.setText("PPN :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(35, 23));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Peresepan Obat Oleh Dokter ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelisi3.setName("panelisi3"); // NOI18N
        panelisi3.setPreferredSize(new java.awt.Dimension(100, 72));
        panelisi3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 5));

        label9.setText("Key Word :");
        label9.setName("label9"); // NOI18N
        label9.setPreferredSize(new java.awt.Dimension(68, 23));
        panelisi3.add(label9);

        TCari.setToolTipText("Alt+C");
        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(245, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelisi3.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('1');
        BtnCari.setText("Cari");
        BtnCari.setToolTipText("Cari (Alt+1)");
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(70, 23));
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
        panelisi3.add(BtnCari);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('2');
        BtnAll.setText("Semua");
        BtnAll.setToolTipText("Tampilkan Semua (Alt+2)");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(82, 23));
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
        panelisi3.add(BtnAll);

        label12.setText("Tarif :");
        label12.setName("label12"); // NOI18N
        label12.setPreferredSize(new java.awt.Dimension(50, 23));
        panelisi3.add(label12);

        Jeniskelas.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Rawat Jalan", "Beli Luar", "Karyawan", "Utama/BPJS", "Kelas 1", "Kelas 2", "Kelas 3", "VIP", "VVIP" }));
        Jeniskelas.setName("Jeniskelas"); // NOI18N
        Jeniskelas.setPreferredSize(new java.awt.Dimension(120, 23));
        Jeniskelas.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                JeniskelasItemStateChanged(evt);
            }
        });
        Jeniskelas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JeniskelasKeyPressed(evt);
            }
        });
        panelisi3.add(Jeniskelas);

        BtnTambah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        BtnTambah.setMnemonic('3');
        BtnTambah.setText("Tambah Obat");
        BtnTambah.setToolTipText("Tambah Obat (Alt+3)");
        BtnTambah.setName("BtnTambah"); // NOI18N
        BtnTambah.setPreferredSize(new java.awt.Dimension(112, 23));
        BtnTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTambahActionPerformed(evt);
            }
        });
        panelisi3.add(BtnTambah);

        BtnSeek5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/011.png"))); // NOI18N
        BtnSeek5.setMnemonic('4');
        BtnSeek5.setText("Konversi");
        BtnSeek5.setToolTipText("Konversi Satuan (Alt+4)");
        BtnSeek5.setName("BtnSeek5"); // NOI18N
        BtnSeek5.setPreferredSize(new java.awt.Dimension(92, 23));
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
        panelisi3.add(BtnSeek5);

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Simpan (Alt+S)");
        BtnSimpan.setName("BtnSimpan"); // NOI18N
        BtnSimpan.setPreferredSize(new java.awt.Dimension(88, 23));
        BtnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpanActionPerformed(evt);
            }
        });
        panelisi3.add(BtnSimpan);

        BtnTambah1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        BtnTambah1.setMnemonic('3');
        BtnTambah1.setText("Tambah Racikan");
        BtnTambah1.setToolTipText("Tambah Racikan (Alt+3)");
        BtnTambah1.setName("BtnTambah1"); // NOI18N
        BtnTambah1.setPreferredSize(new java.awt.Dimension(128, 23));
        BtnTambah1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTambah1ActionPerformed(evt);
            }
        });
        panelisi3.add(BtnTambah1);

        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        BtnHapus.setMnemonic('H');
        BtnHapus.setText("Hapus");
        BtnHapus.setToolTipText("Hapus Racikan (Alt+H)");
        BtnHapus.setName("BtnHapus"); // NOI18N
        BtnHapus.setPreferredSize(new java.awt.Dimension(82, 23));
        BtnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnHapusActionPerformed(evt);
            }
        });
        panelisi3.add(BtnHapus);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('5');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Keluar (Alt+5)");
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(82, 23));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluarActionPerformed(evt);
            }
        });
        panelisi3.add(BtnKeluar);

        internalFrame1.add(panelisi3, java.awt.BorderLayout.PAGE_END);

        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(440, 107));
        FormInput.setLayout(null);

        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(75, 12, 120, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        FormInput.add(TPasien);
        TPasien.setBounds(196, 12, 487, 23);

        KdDokter.setEditable(false);
        KdDokter.setHighlighter(null);
        KdDokter.setName("KdDokter"); // NOI18N
        KdDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdDokterKeyPressed(evt);
            }
        });
        FormInput.add(KdDokter);
        KdDokter.setBounds(75, 72, 120, 23);

        NmDokter.setEditable(false);
        NmDokter.setHighlighter(null);
        NmDokter.setName("NmDokter"); // NOI18N
        FormInput.add(NmDokter);
        NmDokter.setBounds(196, 72, 230, 23);

        jLabel3.setText("No.Rawat :");
        jLabel3.setName("jLabel3"); // NOI18N
        FormInput.add(jLabel3);
        jLabel3.setBounds(0, 12, 72, 23);

        jLabel13.setText("Peresep :");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(0, 72, 72, 23);

        btnDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnDokter.setMnemonic('3');
        btnDokter.setToolTipText("Alt+3");
        btnDokter.setName("btnDokter"); // NOI18N
        btnDokter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDokterActionPerformed(evt);
            }
        });
        btnDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnDokterKeyPressed(evt);
            }
        });
        FormInput.add(btnDokter);
        btnDokter.setBounds(428, 72, 28, 23);

        jLabel11.setText("No.Resep :");
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(455, 72, 70, 23);

        NoResep.setHighlighter(null);
        NoResep.setName("NoResep"); // NOI18N
        NoResep.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NoResepKeyPressed(evt);
            }
        });
        FormInput.add(NoResep);
        NoResep.setBounds(528, 72, 130, 23);

        jLabel8.setText("Tgl.Resep :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(0, 42, 72, 23);

        DTPBeri.setForeground(new java.awt.Color(50, 70, 50));
        DTPBeri.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "28-11-2025" }));
        DTPBeri.setDisplayFormat("dd-MM-yyyy");
        DTPBeri.setName("DTPBeri"); // NOI18N
        DTPBeri.setOpaque(false);
        DTPBeri.setPreferredSize(new java.awt.Dimension(100, 23));
        DTPBeri.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                DTPBeriItemStateChanged(evt);
            }
        });
        DTPBeri.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DTPBeriKeyPressed(evt);
            }
        });
        FormInput.add(DTPBeri);
        DTPBeri.setBounds(75, 42, 90, 23);

        cmbJam.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cmbJam.setName("cmbJam"); // NOI18N
        cmbJam.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cmbJamKeyPressed(evt);
            }
        });
        FormInput.add(cmbJam);
        cmbJam.setBounds(168, 42, 62, 23);

        cmbMnt.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        cmbMnt.setName("cmbMnt"); // NOI18N
        cmbMnt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cmbMntKeyPressed(evt);
            }
        });
        FormInput.add(cmbMnt);
        cmbMnt.setBounds(233, 42, 62, 23);

        cmbDtk.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        cmbDtk.setName("cmbDtk"); // NOI18N
        cmbDtk.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cmbDtkKeyPressed(evt);
            }
        });
        FormInput.add(cmbDtk);
        cmbDtk.setBounds(298, 42, 62, 23);

        ChkRM.setBorder(null);
        ChkRM.setSelected(true);
        ChkRM.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        ChkRM.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ChkRM.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ChkRM.setName("ChkRM"); // NOI18N
        ChkRM.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ChkRMItemStateChanged(evt);
            }
        });
        FormInput.add(ChkRM);
        ChkRM.setBounds(660, 72, 23, 23);

        ChkJln.setBorder(null);
        ChkJln.setSelected(true);
        ChkJln.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        ChkJln.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ChkJln.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ChkJln.setName("ChkJln"); // NOI18N
        ChkJln.setPreferredSize(new java.awt.Dimension(22, 23));
        ChkJln.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkJlnActionPerformed(evt);
            }
        });
        FormInput.add(ChkJln);
        ChkJln.setBounds(363, 42, 23, 23);

        jLabel5.setText("Total :");
        jLabel5.setName("jLabel5"); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(45, 23));
        FormInput.add(jLabel5);
        jLabel5.setBounds(385, 42, 45, 23);

        LTotal.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LTotal.setText("0");
        LTotal.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        LTotal.setName("LTotal"); // NOI18N
        LTotal.setPreferredSize(new java.awt.Dimension(80, 23));
        FormInput.add(LTotal);
        LTotal.setBounds(433, 42, 85, 23);

        jLabel7.setText("Total+PPN :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(65, 23));
        FormInput.add(jLabel7);
        jLabel7.setBounds(520, 42, 65, 23);

        LTotalTagihan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LTotalTagihan.setText("0");
        LTotalTagihan.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        LTotalTagihan.setName("LTotalTagihan"); // NOI18N
        LTotalTagihan.setPreferredSize(new java.awt.Dimension(80, 23));
        FormInput.add(LTotalTagihan);
        LTotalTagihan.setBounds(588, 42, 95, 23);

        jButton1.setText("Detail Resep");
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        FormInput.add(jButton1);
        jButton1.setBounds(700, 70, 100, 23);

        internalFrame1.add(FormInput, java.awt.BorderLayout.PAGE_START);

        TabRawat.setBackground(new java.awt.Color(255, 255, 253));
        TabRawat.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 246, 236)));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setName("TabRawat"); // NOI18N
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });

        Scroll.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll.setComponentPopupMenu(Popup);
        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbResep.setComponentPopupMenu(Popup);
        tbResep.setName("tbResep"); // NOI18N
        tbResep.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbResepMouseClicked(evt);
            }
        });
        tbResep.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                tbResepPropertyChange(evt);
            }
        });
        tbResep.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbResepKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbResep);

        TabRawat.addTab("Umum", Scroll);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(300, 102));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll1.setName("Scroll1"); // NOI18N
        Scroll1.setOpaque(true);
        Scroll1.setPreferredSize(new java.awt.Dimension(454, 90));

        tbObatResepRacikan.setName("tbObatResepRacikan"); // NOI18N
        tbObatResepRacikan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbObatResepRacikanKeyPressed(evt);
            }
        });
        Scroll1.setViewportView(tbObatResepRacikan);

        jPanel3.add(Scroll1, java.awt.BorderLayout.PAGE_START);

        Scroll2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll2.setComponentPopupMenu(Popup);
        Scroll2.setName("Scroll2"); // NOI18N
        Scroll2.setOpaque(true);

        tbDetailResepObatRacikan.setAutoCreateRowSorter(true);
        tbDetailResepObatRacikan.setComponentPopupMenu(Popup);
        tbDetailResepObatRacikan.setName("tbDetailResepObatRacikan"); // NOI18N
        tbDetailResepObatRacikan.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                tbDetailResepObatRacikanPropertyChange(evt);
            }
        });
        tbDetailResepObatRacikan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbDetailResepObatRacikanKeyPressed(evt);
            }
        });
        Scroll2.setViewportView(tbDetailResepObatRacikan);

        jPanel3.add(Scroll2, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Racikan", jPanel3);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            tbResep.requestFocus();
        }
}//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        if(tabUmumAktif()){
            tampilcacheresep();
        }else if(tabRacikanLamaAktif()){
            if(tbObatResepRacikan.getRowCount()!=0){
                if(tbObatResepRacikan.getSelectedRow()!= -1){
                    if(tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),0).toString().equals("")||
                            tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),1).toString().equals("")||
                            tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),2).toString().equals("")||
                            tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),3).toString().equals("")||
                            tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),4).toString().equals("")||
                            tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),5).toString().equals("")||
                            tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),6).toString().equals("")){
                        JOptionPane.showMessageDialog(null,"Silahkan lengkapi data racikan..!!");
                    }else{
                        tampildetailracikanresep();
                    }
                }else{
                    JOptionPane.showMessageDialog(null,"Silahkan pilih racikan..!!");
                }
            }else{
                JOptionPane.showMessageDialog(null,"Silahkan masukkan racikan..!!");
            }
        }else if(tabRacikanV2Aktif()){
            if(validasiRacikanAktifV2()){
                tampilCacheResepRacikanV2();
            }
        }  
}//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnCariActionPerformed(null);
        }else{
            Valid.pindah(evt, TCari, BtnAll);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        buatcacheresep();
        BtnCariActionPerformed(evt);
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnAllActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnCari, TCari);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void tbResepMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbResepMouseClicked
        if(tbResep.getRowCount()!=0){
            try {
                getCekStok();
            } catch (java.lang.NullPointerException e) {
            }
        }
}//GEN-LAST:event_tbResepMouseClicked

    private void tbResepKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbResepKeyPressed
        if(tbResep.getRowCount()!=0){
            try {
                if(evt.getKeyCode()==KeyEvent.VK_DELETE){
                    i=tbResep.getSelectedColumn();
                    if((i==1)||(i==2)){
                        if(tbResep.getSelectedRow()!= -1){
                            tbResep.setValueAt("",tbResep.getSelectedRow(),i);
                        }
                    }   
                }else if(evt.getKeyCode()==KeyEvent.VK_SHIFT){
                    i=tbResep.getSelectedColumn();
                    if(i!=11){
                        TCari.requestFocus();
                    }                
                }else if(evt.getKeyCode()==KeyEvent.VK_RIGHT){
                    getCekStok();
                    i=tbResep.getSelectedColumn();
                    if(i==2){
                        akses.setform("DlgCariObat");
                        DlgCariAturanPakai aturanpakai=new DlgCariAturanPakai(null,false);
                        aturanpakai.addWindowListener(new WindowListener() {
                            @Override
                            public void windowOpened(WindowEvent e) {}
                            @Override
                            public void windowClosing(WindowEvent e) {}
                            @Override
                            public void windowClosed(WindowEvent e) {
                                if(aturanpakai.getTable().getSelectedRow()!= -1){  
                                    if(tabUmumAktif()){
                                        tbResep.setValueAt(aturanpakai.getTable().getValueAt(aturanpakai.getTable().getSelectedRow(),0).toString(),tbResep.getSelectedRow(),2);
                                        tbResep.requestFocus();
                                    }else if(tabRacikanLamaAktif()){
                                        tbObatResepRacikan.setValueAt(aturanpakai.getTable().getValueAt(aturanpakai.getTable().getSelectedRow(),0).toString(),tbObatResepRacikan.getSelectedRow(),5);
                                        tbObatResepRacikan.requestFocus();
                                    }   
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
                        aturanpakai.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                        aturanpakai.setLocationRelativeTo(internalFrame1);
                        aturanpakai.setVisible(true);
                    }else if(i==2){
                        hitungResep();
                    }
                }else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
                    getCekStok();
                    i=tbResep.getSelectedColumn();
                    if((i==2)||(i==3)){
                        hitungResep();
                        TCari.requestFocus();
                    } 
                }   
            } catch (Exception e) {
            }
        }
}//GEN-LAST:event_tbResepKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        if(modeEmbedded){
            return;
        }
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTambahActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        DlgBarang barang=new DlgBarang(null,false);
        barang.emptTeks();
        barang.isCek();
        barang.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        barang.setLocationRelativeTo(internalFrame1);
        barang.setAlwaysOnTop(false);
        barang.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnTambahActionPerformed

    // ===== Paket Resep (paket obat) =====
    private void pasangTombolPaketResep(){
        BtnPaketResep.setText("Tambah Paket");
        try{
            BtnPaketResep.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png")));
        }catch(Exception e){
            System.out.println("Notif ikon paket : "+e);
        }
        BtnPaketResep.setToolTipText("Tambah obat dari paket resep");
        BtnPaketResep.setPreferredSize(new java.awt.Dimension(124,23));
        BtnPaketResep.addActionListener(new java.awt.event.ActionListener(){
            @Override public void actionPerformed(java.awt.event.ActionEvent evt){ BtnPaketResepActionPerformed(evt); }
        });
        // sisipkan tepat setelah tombol "Tambah Racikan" (BtnTambah1) bila ada
        int idx=-1;
        java.awt.Component[] comps=panelisi3.getComponents();
        for(int k=0;k<comps.length;k++){
            if(comps[k]==BtnTambah1){ idx=k+1; break; }
        }
        if(idx>=0){
            panelisi3.add(BtnPaketResep, idx);
        }else{
            panelisi3.add(BtnPaketResep);
        }
        panelisi3.revalidate();
        panelisi3.repaint();
    }

    private void BtnPaketResepActionPerformed(java.awt.event.ActionEvent evt){
        if(!tabUmumAktif()){
            JOptionPane.showMessageDialog(null,"Paket resep hanya untuk tab obat umum (bukan racikan).");
            return;
        }
        if(TNoRw.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"pasien");
            return;
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        DlgPaketResep paket=new DlgPaketResep(null,true);
        paket.setSelectionMode(true);
        paket.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        paket.setLocationRelativeTo(internalFrame1);
        paket.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
        String kd=paket.getSelectedPackageCode();
        if(kd!=null && !kd.trim().equals("")){
            terapkanPaketResep(kd.trim());
        }
    }

    private void terapkanPaketResep(String kdPaket){
        java.util.List<String[]> items=new java.util.ArrayList<String[]>();
        try(java.sql.PreparedStatement ps=koneksi.prepareStatement(
                "select kode_brng,jml from paket_resep_detail where kd_paket=? order by urut")){
            ps.setString(1,kdPaket);
            try(java.sql.ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    double j=rs.getDouble("jml");
                    String js=(j==Math.floor(j))?String.valueOf((long)j):String.valueOf(j);
                    items.add(new String[]{rs.getString("kode_brng"),js});
                }
            }
        }catch(Exception e){
            System.out.println("Notif Paket Resep : "+e);
        }
        if(items.isEmpty()){
            JOptionPane.showMessageDialog(null,"Paket kosong / tidak ada obat di paket tersebut.");
            return;
        }
        // muat katalog penuh agar obat paket pasti tersedia (baris yang sudah diisi jumlah tetap dipertahankan)
        TCari.setText("");
        tampilcacheresep();
        int terpasang=0;
        StringBuilder gagal=new StringBuilder();
        for(String[] it:items){
            String kode=it[0];
            boolean found=false;
            for(int r=0;r<tbResep.getRowCount();r++){
                if(tbResep.getValueAt(r,3)!=null && tbResep.getValueAt(r,3).toString().equals(kode)){
                    tbResep.setValueAt(Boolean.TRUE,r,0);
                    tbResep.setValueAt(it[1],r,1);
                    found=true;
                    terpasang++;
                    break;
                }
            }
            if(!found){
                gagal.append("- ").append(kode).append("\n");
            }
        }
        hitungResep();
        String pesan="Paket diterapkan: "+terpasang+" obat masuk ke daftar resep.";
        if(gagal.length()>0){
            pesan=pesan+"\n\nObat berikut tidak ditemukan di katalog (mungkin nonaktif / stok kosong) dan dilewati:\n"+gagal.toString();
        }
        JOptionPane.showMessageDialog(null,pesan);
        TCari.requestFocus();
    }

private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        selesaiEditRacikanV2();
        hitungResep();
        if(TNoRw.getText().trim().equals("")||TPasien.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"pasien");
        }else if(KdDokter.getText().trim().equals("")||NmDokter.getText().trim().equals("")){
            Valid.textKosong(KdDokter,"Dokter");
        }else if(NoResep.getText().trim().equals("")){
            Valid.textKosong(NoResep,"No.Resep");
        }else if(ttl<=0){
            JOptionPane.showMessageDialog(null,"Maaf, silahkan masukkan terlebih dahulu obat yang mau diberikan...!!!");
            TCari.requestFocus();
        }else{
            int reply = JOptionPane.showConfirmDialog(rootPane,"Eeiiiiiits, udah bener belum data yang mau disimpan..??","Konfirmasi",JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.YES_OPTION) {
                pastikanTabelCatatanResepDokter();
                ChkJln.setSelected(false);
                if(ubah==false){
                    // ambil nomor resep final secara atomik tepat saat simpan (anti-duplikat), sebelum transaksi dibuka
                    NoResep.setText(Valid.nomorResepAtomik(Valid.SetTgl(DTPBeri.getSelectedItem()+""),
                        DTPBeri.getSelectedItem().toString().substring(6,10)+DTPBeri.getSelectedItem().toString().substring(3,5)+DTPBeri.getSelectedItem().toString().substring(0,2),4));
                }
                Sequel.AutoComitFalse();
                sukses=true;
                if(ubah==false){
                    if(Sequel.menyimpantf2("resep_obat","?,?,?,?,?,?,?,?,?,?","Nomer Resep",10,new String[]{
                        NoResep.getText(),"0000-00-00","00:00:00",TNoRw.getText(),KdDokter.getText(),Valid.SetTgl(DTPBeri.getSelectedItem()+""),
                        cmbJam.getSelectedItem()+":"+cmbMnt.getSelectedItem()+":"+cmbDtk.getSelectedItem(),status,"0000-00-00","00:00:00"
                        })==true){
                            simpandata();
                    }else{
                        emptTeksobat();
                        if(Sequel.menyimpantf2("resep_obat","?,?,?,?,?,?,?,?,?,?","Nomer Resep",10,new String[]{
                            NoResep.getText(),"0000-00-00","00:00:00",TNoRw.getText(),KdDokter.getText(),Valid.SetTgl(DTPBeri.getSelectedItem()+""),
                            cmbJam.getSelectedItem()+":"+cmbMnt.getSelectedItem()+":"+cmbDtk.getSelectedItem(),status,"0000-00-00","00:00:00"
                            })==true){
                                simpandata();
                        }else{
                            emptTeksobat();
                            if(Sequel.menyimpantf2("resep_obat","?,?,?,?,?,?,?,?,?,?","Nomer Resep",10,new String[]{
                                NoResep.getText(),"0000-00-00","00:00:00",TNoRw.getText(),KdDokter.getText(),Valid.SetTgl(DTPBeri.getSelectedItem()+""),
                                cmbJam.getSelectedItem()+":"+cmbMnt.getSelectedItem()+":"+cmbDtk.getSelectedItem(),status,"0000-00-00","00:00:00"
                                })==true){
                                    simpandata();
                            }else{
                                emptTeksobat();
                                sukses=false;
                            }
                        }
                    }
                }else if(ubah==true){
                    Sequel.meghapus("resep_dokter","no_resep",NoResep.getText());
                    Sequel.meghapus("resep_dokter_racikan","no_resep",NoResep.getText());
                    Sequel.meghapus("resep_dokter_racikan_detail","no_resep",NoResep.getText());
                    ubah=false;
                    simpandata();
                }                                                      
                
                if(sukses==true){
                    if(RESEPRAJALKEPLAN.equals("yes")&&status.equals("ralan")&&(ubah==false)){
                        try {
                            ps2=koneksi.prepareStatement(
                                "select pemeriksaan_ralan.tgl_perawatan,pemeriksaan_ralan.jam_rawat from pemeriksaan_ralan where pemeriksaan_ralan.no_rawat=? and pemeriksaan_ralan.nip=? order by pemeriksaan_ralan.tgl_perawatan desc,pemeriksaan_ralan.jam_rawat desc limit 1");
                            try {
                                ps2.setString(1,TNoRw.getText());
                                ps2.setString(2,KdDokter.getText());
                                rs2=ps2.executeQuery();
                                if(rs2.next()){
                                    resep="Resep : \n";
                                    psresep=koneksi.prepareStatement(
                                           "select databarang.nama_brng,resep_dokter.jml,resep_dokter.aturan_pakai from databarang inner join resep_dokter on databarang.kode_brng=resep_dokter.kode_brng where resep_dokter.no_resep=?");
                                    try {
                                        psresep.setString(1,NoResep.getText());
                                        rsobat=psresep.executeQuery();
                                        while(rsobat.next()){
                                            resep=resep+rsobat.getString("nama_brng")+" Jumlah "+rsobat.getString("jml")+" Aturan Pakai "+rsobat.getString("aturan_pakai")+"\n";
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Notif : "+e);
                                    } finally{
                                        if(rsobat != null){
                                            rsobat.close();
                                        }

                                        if(psresep != null){
                                            psresep.close();
                                        }
                                    }
                                    
                                    psresep=koneksi.prepareStatement(
                                            "select resep_dokter_racikan.no_racik,resep_dokter_racikan.nama_racik,metode_racik.nm_racik as metode,resep_dokter_racikan.jml_dr,resep_dokter_racikan.aturan_pakai "+
                                            "from resep_dokter_racikan inner join metode_racik on resep_dokter_racikan.kd_racik=metode_racik.kd_racik where resep_dokter_racikan.no_resep=?");
                                    try {
                                        psresep.setString(1,NoResep.getText());
                                        rsobat=psresep.executeQuery();
                                        while(rsobat.next()){
                                            resep=resep+rsobat.getString("no_racik")+". "+rsobat.getString("nama_racik")+" Jumlah "+rsobat.getString("jml_dr")+" "+rsobat.getString("metode")+" Aturan Pakai "+rsobat.getString("aturan_pakai")+"\n";
                                            pscarikapasitas=koneksi.prepareStatement(
                                                    "select databarang.nama_brng,resep_dokter_racikan_detail.jml from resep_dokter_racikan_detail inner join databarang "+
                                                    "on resep_dokter_racikan_detail.kode_brng=databarang.kode_brng where resep_dokter_racikan_detail.no_resep=? and "+
                                                    "resep_dokter_racikan_detail.no_racik=?");
                                            try {
                                                pscarikapasitas.setString(1,NoResep.getText());
                                                pscarikapasitas.setString(2,rsobat.getString("no_racik"));
                                                carikapasitas=pscarikapasitas.executeQuery();
                                                while(carikapasitas.next()){
                                                    resep=resep+"-- "+carikapasitas.getString("nama_brng")+" "+carikapasitas.getString("jml")+"\n";
                                                }
                                            } catch (Exception e) {
                                                System.out.println("Notif : "+e);
                                            } finally{
                                                if(carikapasitas != null){
                                                    carikapasitas.close();
                                                }

                                                if(pscarikapasitas != null){
                                                    pscarikapasitas.close();
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Notif : "+e);
                                    } finally{
                                        if(rsobat != null){
                                            rsobat.close();
                                        }

                                        if(psresep != null){
                                            psresep.close();
                                        }
                                    }
                                    
                                    Sequel.queryu2("update pemeriksaan_ralan set rtl=concat(rtl,' ',?) where no_rawat=? and tgl_perawatan=? and jam_rawat=? and nip=?",5,new String[]{
                                        resep,TNoRw.getText(),rs2.getString("tgl_perawatan"),rs2.getString("jam_rawat"),KdDokter.getText()
                                    });
                                }
                            } catch (Exception e) {
                                System.out.println("Notif : "+e);
                            } finally{
                                if(rs2 != null){
                                    rs2.close();
                                }

                                if(ps2 != null){
                                    ps2.close();
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notif : "+e);
                        }
                    }
                    
                    Sequel.Commit();
                    resepBerhasilDisimpan=true;
                    noResepTersimpan=NoResep.getText();
                    tampilDetailResep(NoResep.getText());
                    for(i=0;i<tbResep.getRowCount();i++){
                        tbResep.setValueAt("",i,1);
                        tbResep.setValueAt("",i,2);
                    }
                    Valid.tabelKosong(tabModeResepRacikan);
                    Valid.tabelKosong(tabModeDetailResepRacikan);
                    Valid.tabelKosong(tabModeResepRacikanV2);
                    Valid.tabelKosong(tabModeDetailResepRacikanV2);
                    kosongkanCatatanDokter();
                    tampilkanLabelRacikanAktifV2();
                    tampilkanDetailRacikanV2Aktif();
                    if(!modeEmbedded){
                        dispose();
                    }
                }else{
                    JOptionPane.showMessageDialog(null,"Terjadi kesalahan saat pemrosesan data, transaksi dibatalkan.\nPeriksa kembali data sebelum melanjutkan menyimpan..!!");
                    Sequel.RollBack();
                }
                Sequel.AutoComitTrue();
                ChkJln.setSelected(true);
            }                
        }
}//GEN-LAST:event_BtnSimpanActionPerformed

private void BtnSeek5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek5ActionPerformed
    DlgCariKonversi carikonversi=new DlgCariKonversi(null,false);
    carikonversi.setLocationRelativeTo(internalFrame1);
    carikonversi.setAlwaysOnTop(false);
    carikonversi.setVisible(true);
}//GEN-LAST:event_BtnSeek5ActionPerformed

private void BtnSeek5KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeek5KeyPressed
// TODO add your handling code here:
}//GEN-LAST:event_BtnSeek5KeyPressed

private void ppBersihkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppBersihkanActionPerformed
    if(tabUmumAktif()){
        for(i=0;i<tbResep.getRowCount();i++){ 
            tbResep.setValueAt(false,i,0);
            tbResep.setValueAt("",i,1);
            tbResep.setValueAt("",i,2);
        }
    }else if(tabRacikanLamaAktif()){
        for(i=0;i<tbDetailResepObatRacikan.getRowCount();i++){ 
            tbDetailResepObatRacikan.setValueAt(1,i,9);
            tbDetailResepObatRacikan.setValueAt(1,i,11);
            tbDetailResepObatRacikan.setValueAt("",i,12);
            tbDetailResepObatRacikan.setValueAt(0,i,13);
        }
    }else if(tabRacikanV2Aktif()){
        String racikanAktif=ambilNomorRacikanAktifV2();
        for(i=0;i<tbDetailRacikanV2.getRowCount();i++){
            if(tbDetailRacikanV2.getValueAt(i,0).toString().startsWith(racikanAktif+" - ")){
                tbDetailRacikanV2.setValueAt(false,i,1);
                tbDetailRacikanV2.setValueAt("",i,2);
            }
        }
    }  
    hitungResep();
}//GEN-LAST:event_ppBersihkanActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        TabRawatMouseClicked(null);
        if(ubah==false){
            emptTeksobat();
        }
            
    }//GEN-LAST:event_formWindowActivated

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            Sequel.cariIsi("select concat(pasien.no_rkm_medis,' ',pasien.nm_pasien) from reg_periksa inner join pasien "+
                " on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where reg_periksa.no_rawat=? ",TPasien,TNoRw.getText());
        }else if(evt.getKeyCode()==KeyEvent.VK_DOWN){
            TCari.requestFocus();
        }else{
            Valid.pindah(evt,KdDokter,DTPBeri);
        }
    }//GEN-LAST:event_TNoRwKeyPressed

    private void KdDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdDokterKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_UP){
            btnDokterActionPerformed(null);
        }else{
            Valid.pindah(evt,NoResep,BtnSimpan);
        }
    }//GEN-LAST:event_KdDokterKeyPressed

    private void btnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDokterActionPerformed
        dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter.isCek();
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setVisible(true);
    }//GEN-LAST:event_btnDokterActionPerformed

    private void btnDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnDokterKeyPressed
        Valid.pindah(evt,KdDokter,BtnSimpan);
    }//GEN-LAST:event_btnDokterKeyPressed

    private void NoResepKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NoResepKeyPressed
        Valid.pindah(evt,cmbDtk,KdDokter);
    }//GEN-LAST:event_NoResepKeyPressed

    private void DTPBeriKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DTPBeriKeyPressed
        try {
            emptTeksobat();
        } catch (Exception e) {
        }
        Valid.pindah(evt,TNoRw,cmbJam);
    }//GEN-LAST:event_DTPBeriKeyPressed

    private void cmbJamKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cmbJamKeyPressed
        Valid.pindah(evt,DTPBeri,cmbMnt);
    }//GEN-LAST:event_cmbJamKeyPressed

    private void cmbMntKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cmbMntKeyPressed
        Valid.pindah(evt,cmbJam,cmbDtk);
    }//GEN-LAST:event_cmbMntKeyPressed

    private void cmbDtkKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cmbDtkKeyPressed
        Valid.pindah(evt,cmbMnt,NoResep);
    }//GEN-LAST:event_cmbDtkKeyPressed

    private void ChkRMItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_ChkRMItemStateChanged
        if(ChkRM.isSelected()==true){
            NoResep.setEditable(false);
            NoResep.setBackground(new Color(245,250,240));
            try {
                emptTeksobat();
            } catch (Exception e) {
            }
        }else if(ChkRM.isSelected()==false){
            NoResep.setEditable(true);
            NoResep.setBackground(new Color(250,255,245));
            NoResep.setText("");
        }
    }//GEN-LAST:event_ChkRMItemStateChanged

    private void ChkJlnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkJlnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ChkJlnActionPerformed

    private void JeniskelasItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_JeniskelasItemStateChanged
        tampilcacheresep();
    }//GEN-LAST:event_JeniskelasItemStateChanged

    private void JeniskelasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JeniskelasKeyPressed
        Valid.pindah(evt, TCari,BtnKeluar);
    }//GEN-LAST:event_JeniskelasKeyPressed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        if(tabUmumAktif()){
            BtnTambah1.setVisible(false);
            BtnHapus.setVisible(false);
            TCari.setPreferredSize(new Dimension(245, 23));
        }else if(tabRacikanV2Aktif()){
            BtnTambah1.setVisible(true);
            BtnHapus.setVisible(true);
            TCari.setPreferredSize(new Dimension(181, 23));
            tampilkanLabelRacikanAktifV2();
        }else if(tabRacikanLamaAktif()){
            BtnTambah1.setVisible(true);
            BtnHapus.setVisible(true);
            TCari.setPreferredSize(new Dimension(181, 23));
        }
    }//GEN-LAST:event_TabRawatMouseClicked

    private void tbObatResepRacikanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatResepRacikanKeyPressed
        if(tbObatResepRacikan.getRowCount()!=0){
            try {
                i=tbObatResepRacikan.getSelectedColumn();
                if(evt.getKeyCode()==KeyEvent.VK_RIGHT){
                    if(i==5){
                        akses.setform("DlgCariObat");
                        DlgCariAturanPakai aturanpakai=new DlgCariAturanPakai(null,false);
                        aturanpakai.addWindowListener(new WindowListener() {
                            @Override
                            public void windowOpened(WindowEvent e) {}
                            @Override
                            public void windowClosing(WindowEvent e) {}
                            @Override
                            public void windowClosed(WindowEvent e) {
                                if(aturanpakai.getTable().getSelectedRow()!= -1){  
                                    if(tabUmumAktif()){
                                        tbResep.setValueAt(aturanpakai.getTable().getValueAt(aturanpakai.getTable().getSelectedRow(),0).toString(),tbResep.getSelectedRow(),2);
                                        tbResep.requestFocus();
                                    }else if(tabRacikanLamaAktif()){
                                        tbObatResepRacikan.setValueAt(aturanpakai.getTable().getValueAt(aturanpakai.getTable().getSelectedRow(),0).toString(),tbObatResepRacikan.getSelectedRow(),5);
                                        tbObatResepRacikan.requestFocus();
                                    }   
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
                        aturanpakai.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                        aturanpakai.setLocationRelativeTo(internalFrame1);
                        aturanpakai.setVisible(true);
                    }else if(i==3){
                        if(tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),1).equals("")){
                            JOptionPane.showMessageDialog(null,"Silahkan masukkan nama racikan..!!");
                            tbObatResepRacikan.requestFocus();
                        }else{
                            DlgCariMetodeRacik metoderacik=new DlgCariMetodeRacik(null,false);
        
                            metoderacik.addWindowListener(new WindowListener() {
                                @Override
                                public void windowOpened(WindowEvent e) {}
                                @Override
                                public void windowClosing(WindowEvent e) {}
                                @Override
                                public void windowClosed(WindowEvent e) {
                                    if(metoderacik.getTable().getSelectedRow()!= -1){  
                                        tbObatResepRacikan.setValueAt(metoderacik.getTable().getValueAt(metoderacik.getTable().getSelectedRow(),1).toString(),tbObatResepRacikan.getSelectedRow(),2);
                                        tbObatResepRacikan.setValueAt(metoderacik.getTable().getValueAt(metoderacik.getTable().getSelectedRow(),2).toString(),tbObatResepRacikan.getSelectedRow(),3);
                                        tbObatResepRacikan.requestFocus();
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

                            metoderacik.getTable().addKeyListener(new KeyListener() {
                                @Override
                                public void keyTyped(KeyEvent e) {}
                                @Override
                                public void keyPressed(KeyEvent e) {
                                    if(e.getKeyCode()==KeyEvent.VK_SPACE){
                                        metoderacik.dispose();
                                    }
                                }
                                @Override
                                public void keyReleased(KeyEvent e) {}
                            }); 
                            metoderacik.isCek();
                            metoderacik.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                            metoderacik.setLocationRelativeTo(internalFrame1);
                            metoderacik.setVisible(true);
                        }
                    }
                }else if(evt.getKeyCode()==KeyEvent.VK_SHIFT){
                    if(i==6){
                        TCari.requestFocus();
                    }
                }else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
                    if(i==6){
                        tampildetailracikanresep();
                    }
                }
            } catch (Exception e) {
            }
        }
    }//GEN-LAST:event_tbObatResepRacikanKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        if(tabRacikanV2Aktif()){
            hapusRacikanV2();
        }else if(tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),1).equals("")&&tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),4).equals("")&&tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),5).equals("")&&tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),6).equals("")){
            tabModeResepRacikan.removeRow(tbObatResepRacikan.getSelectedRow());
        }else{
            JOptionPane.showMessageDialog(null,"Maaf sudah terisi, gak boleh dihapus..!!");
        }
    }//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnTambah1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTambah1ActionPerformed
        if(tabRacikanV2Aktif()){
            tambahRacikanV2();
        }else{
            i=tabModeResepRacikan.getRowCount()+1;
            if(i==99){
                JOptionPane.showMessageDialog(null,"Maksimal 98 Racikan..!!");
            }else{
                tabModeResepRacikan.addRow(new Object[]{""+i,"","","","","",""});
            }
        }
    }//GEN-LAST:event_BtnTambah1ActionPerformed

    private void tbDetailResepObatRacikanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbDetailResepObatRacikanKeyPressed
        if(tbDetailResepObatRacikan.getRowCount()!=0){
            try {
                if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_RIGHT)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                    i=tbDetailResepObatRacikan.getSelectedColumn();
                    if((i==11)||(i==9)||(i==13)||(i==14)){
                        try {
                            if(!tbDetailResepObatRacikan.getValueAt(tbDetailResepObatRacikan.getSelectedRow(),11).toString().equals(tbDetailResepObatRacikan.getValueAt(tbDetailResepObatRacikan.getSelectedRow(),9).toString())){
                                if(Valid.SetAngka(tbDetailResepObatRacikan.getValueAt(tbDetailResepObatRacikan.getSelectedRow(),8).toString())==0){
                                    JOptionPane.showMessageDialog(null,"Kapasitas obat masih kosong..!!!");
                                }else{
                                    tbDetailResepObatRacikan.setValueAt(Valid.SetAngka8(Valid.SetAngka(tbDetailResepObatRacikan.getValueAt(tbDetailResepObatRacikan.getSelectedRow(),8).toString())*
                                        (Valid.SetAngka(tbDetailResepObatRacikan.getValueAt(tbDetailResepObatRacikan.getSelectedRow(),9).toString())/Valid.SetAngka(tbDetailResepObatRacikan.getValueAt(tbDetailResepObatRacikan.getSelectedRow(),11).toString())),1),
                                            tbDetailResepObatRacikan.getSelectedRow(),12);
                                }                                
                            }
                        } catch (Exception e) {
                            tbDetailResepObatRacikan.setValueAt(0,tbDetailResepObatRacikan.getSelectedRow(),12);
                        }      
                        getCekStokRacikan();
                    }else if(i==12){
                        if(tbDetailResepObatRacikan.getValueAt(tbDetailResepObatRacikan.getSelectedRow(),12).toString().contains("%")){
                            getDatadetailresepracikan2();
                        }else{
                            getDatadetailresepracikan();
                        }  
                        getCekStokRacikan();
                    }
                    hitungResep();
                }
            } catch (Exception e) {
            }
        }
    }//GEN-LAST:event_tbDetailResepObatRacikanKeyPressed

    private void tbDetailResepObatRacikanPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_tbDetailResepObatRacikanPropertyChange
        if(this.isVisible()==true || modeEmbedded){
            try {
                if(tbDetailResepObatRacikan.getSelectedRow()!= -1){
                    if(tbDetailResepObatRacikan.getValueAt(tbDetailResepObatRacikan.getSelectedRow(),12).toString().contains("%")){
                        getDatadetailresepracikan2();
                    }
                }else{
                    getDatadetailresepracikan();
                }  
                getCekStokRacikan();
                hitungResep();
            } catch (Exception e) {
            }   
        }
    }//GEN-LAST:event_tbDetailResepObatRacikanPropertyChange

    private void tbResepPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_tbResepPropertyChange
        if(this.isVisible()==true || modeEmbedded){
            try {
                getCekStok();
                hitungResep();
            } catch (Exception e) {
            }
        }
    }//GEN-LAST:event_tbResepPropertyChange

    private void ppStok1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppStok1ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        DlgCekStok ceksetok=new DlgCekStok(null,false);
        ceksetok.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        ceksetok.setLocationRelativeTo(internalFrame1);
        ceksetok.setAlwaysOnTop(false);
        ceksetok.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_ppStok1ActionPerformed

    private void DTPBeriItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_DTPBeriItemStateChanged
        try {
            emptTeksobat();
        } catch (Exception e) {
        }
            
    }//GEN-LAST:event_DTPBeriItemStateChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       if (tbResep.getSelectedRow() != -1) {
        String noResep = tbResep.getValueAt(tbResep.getSelectedRow(), 0).toString();
        tampilDetailResep(noResep);
    } else {
        JOptionPane.showMessageDialog(null, "Silakan pilih resep dulu...");
    }
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgPeresepanDokter dialog = new DlgPeresepanDokter(new javax.swing.JFrame(), true);
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
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnSeek5;
    private widget.Button BtnSimpan;
    private widget.Button BtnTambah;
    private widget.Button BtnTambah1;
    private widget.CekBox ChkJln;
    private widget.CekBox ChkRM;
    private widget.Tanggal DTPBeri;
    private widget.PanelBiasa FormInput;
    private widget.ComboBox Jeniskelas;
    private widget.TextBox KdDokter;
    private widget.TextBox KdPj;
    private widget.Label LPpn;
    private widget.Label LTotal;
    private widget.Label LTotalTagihan;
    private widget.TextBox NmDokter;
    private widget.TextBox NoResep;
    private javax.swing.JPopupMenu Popup;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll1;
    private widget.ScrollPane Scroll2;
    private widget.TextBox TCari;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private javax.swing.JTabbedPane TabRawat;
    private widget.Button btnDokter;
    private widget.ComboBox cmbDtk;
    private widget.ComboBox cmbJam;
    private widget.ComboBox cmbMnt;
    private widget.InternalFrame internalFrame1;
    private javax.swing.JButton jButton1;
    private widget.Label jLabel11;
    private widget.Label jLabel13;
    private widget.Label jLabel3;
    private widget.Label jLabel5;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private javax.swing.JPanel jPanel3;
    private widget.Label label12;
    private widget.Label label9;
    private widget.panelisi panelisi3;
    private javax.swing.JMenuItem ppBersihkan;
    private javax.swing.JMenuItem ppStok1;
    private widget.Table tbDetailResepObatRacikan;
    private widget.Table tbObatResepRacikan;
    private widget.Table tbResep;
    // End of variables declaration//GEN-END:variables

    public void tampilobat() {        
        buatcacheresep();
        tampilcacheresep();
        terapkanDraftTerapiSOAPJikaPerlu();
    }
    
    private void buatcacheresep(){
        try{
            file=new File("./cache/peresepandokter.iyem");
            file.createNewFile();
            fileWriter = new FileWriter(file);
            StringBuilder iyembuilder = new StringBuilder();
            if(kenaikan>0){
                if(aktifkanbatch.equals("yes")){
                    qrystokkosong="";
                    if(STOKKOSONGRESEP.equals("no")){
                        qrystokkosong=" and gudangbarang.stok>0 ";
                    }
                    psresepasuransi=koneksi.prepareStatement("select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,(databarang.h_beli+(databarang.h_beli*?)) as harga,"+
                        " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,sum(gudangbarang.stok) as stok,databarang.kapasitas "+
                        " from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                        " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                        " inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng "+
                        " where databarang.status='1' "+qrystokkosong+" and gudangbarang.no_batch<>'' and gudangbarang.no_faktur<>'' and gudangbarang.kd_bangsal=? "+
                        " group by gudangbarang.kode_brng order by databarang.nama_brng");
                }else{
                    qrystokkosong="";
                    if(STOKKOSONGRESEP.equals("no")){
                        qrystokkosong=" and gudangbarang.stok>0 ";
                    }
                    psresepasuransi=koneksi.prepareStatement("select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,(databarang.h_beli+(databarang.h_beli*?)) as harga,"+
                        " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,gudangbarang.stok,databarang.kapasitas "+
                        " from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                        " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                        " inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng "+
                        " where databarang.status='1' "+qrystokkosong+" and gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.kd_bangsal=?  "+
                        " order by databarang.nama_brng");
                }
                    
                try{
                    psresepasuransi.setDouble(1,kenaikan);
                    psresepasuransi.setString(2,bangsal);
                    rsobat=psresepasuransi.executeQuery();
                    while(rsobat.next()){
                        iyembuilder.append("{\"KodeBarang\":\"").append(rsobat.getString("kode_brng")).append("\",\"NamaBarang\":\"").append(rsobat.getString("nama_brng").replaceAll("\"","")).append("\",\"Satuan\":\"").append(rsobat.getString("kode_sat")).append("\",\"Kandungan\":\"").append(rsobat.getString("letak_barang")).append("\",\"HargaKaryawan\":\"").append(Valid.roundUp(rsobat.getDouble("harga"),100)).append("\",\"HargaRalan\":\"").append(Valid.roundUp(rsobat.getDouble("harga"),100)).append("\",\"HargaBeliLuar\":\"").append(Valid.roundUp(rsobat.getDouble("harga"),100)).append("\",\"HargaKelas1\":\"").append(Valid.roundUp(rsobat.getDouble("harga"),100)).append("\",\"HargaKelas2\":\"").append(Valid.roundUp(rsobat.getDouble("harga"),100)).append("\",\"HargaKelas3\":\"").append(Valid.roundUp(rsobat.getDouble("harga"),100)).append("\",\"HargaVIP\":\"").append(Valid.roundUp(rsobat.getDouble("harga"),100)).append("\",\"HargaVVIP\":\"").append(Valid.roundUp(rsobat.getDouble("harga"),100)).append("\",\"HargaUtama\":\"").append(Valid.roundUp(rsobat.getDouble("harga"),100)).append("\",\"Jenis\":\"").append(rsobat.getString("nama")).append("\",\"IndustriFarmasi\":\"").append(rsobat.getString("nama_industri")).append("\",\"HargaBeli\":\"").append(rsobat.getDouble("h_beli")).append("\",\"Stok\":\"").append(rsobat.getDouble("stok")).append("\",\"Kapasitas\":\"").append(rsobat.getDouble("kapasitas")).append("\"},");
                    }  
                }catch(Exception e){
                    System.out.println("Notifikasi : "+e);
                }finally{
                    if(rsobat != null){
                        rsobat.close();
                    }

                    if(psresepasuransi != null){
                        psresepasuransi.close();
                    }
                }                                   
            }else{    
                if(aktifkanbatch.equals("yes")){
                    qrystokkosong="";
                    if(STOKKOSONGRESEP.equals("no")){
                        qrystokkosong=" and gudangbarang.stok>0 ";
                    }
                    psresep=koneksi.prepareStatement(
                        "select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,"+
                        " databarang.karyawan,databarang.ralan,databarang.beliluar,databarang.kelas1," +
                        " databarang.kelas2,databarang.kelas3,databarang.vip,databarang.vvip,"+
                        " databarang.letak_barang,databarang.utama,industrifarmasi.nama_industri,databarang.h_beli,sum(gudangbarang.stok) as stok,databarang.kapasitas "+
                        " from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                        " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                        " inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng "+
                        " where  databarang.status='1' "+qrystokkosong+" and gudangbarang.no_batch<>'' and gudangbarang.no_faktur<>'' and gudangbarang.kd_bangsal=? "+
                        " group by gudangbarang.kode_brng order by databarang.nama_brng");
                }else{
                    qrystokkosong="";
                    if(STOKKOSONGRESEP.equals("no")){
                        qrystokkosong=" and gudangbarang.stok>0 ";
                    }
                    psresep=koneksi.prepareStatement(
                        "select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,"+
                        " databarang.karyawan,databarang.ralan,databarang.beliluar,databarang.kelas1," +
                        " databarang.kelas2,databarang.kelas3,databarang.vip,databarang.vvip,"+
                        " databarang.letak_barang,databarang.utama,industrifarmasi.nama_industri,databarang.h_beli,gudangbarang.stok,databarang.kapasitas "+
                        " from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                        " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                        " inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng "+
                        " where  databarang.status='1' "+qrystokkosong+" and gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.kd_bangsal=? "+
                        " order by databarang.nama_brng");
                }
                    
                try{
                    psresep.setString(1,bangsal);
                    rsobat=psresep.executeQuery();
                    while(rsobat.next()){
                        iyembuilder.append("{\"KodeBarang\":\"").append(rsobat.getString("kode_brng")).append("\",\"NamaBarang\":\"").append(rsobat.getString("nama_brng").replaceAll("\"","")).append("\",\"Satuan\":\"").append(rsobat.getString("kode_sat")).append("\",\"Kandungan\":\"").append(rsobat.getString("letak_barang")).append("\",\"HargaKaryawan\":\"").append(Valid.roundUp(rsobat.getDouble("karyawan"),100)).append("\",\"HargaRalan\":\"").append(Valid.roundUp(rsobat.getDouble("ralan"),100)).append("\",\"HargaBeliLuar\":\"").append(Valid.roundUp(rsobat.getDouble("beliluar"),100)).append("\",\"HargaKelas1\":\"").append(Valid.roundUp(rsobat.getDouble("kelas1"),100)).append("\",\"HargaKelas2\":\"").append(Valid.roundUp(rsobat.getDouble("kelas2"),100)).append("\",\"HargaKelas3\":\"").append(Valid.roundUp(rsobat.getDouble("kelas3"),100)).append("\",\"HargaVIP\":\"").append(Valid.roundUp(rsobat.getDouble("vip"),100)).append("\",\"HargaVVIP\":\"").append(Valid.roundUp(rsobat.getDouble("vvip"),100)).append("\",\"HargaUtama\":\"").append(Valid.roundUp(rsobat.getDouble("utama"),100)).append("\",\"Jenis\":\"").append(rsobat.getString("nama")).append("\",\"IndustriFarmasi\":\"").append(rsobat.getString("nama_industri")).append("\",\"HargaBeli\":\"").append(rsobat.getDouble("h_beli")).append("\",\"Stok\":\"").append(rsobat.getDouble("stok")).append("\",\"Kapasitas\":\"").append(rsobat.getDouble("kapasitas")).append("\"},");
                    }  
                }catch(Exception e){
                    System.out.println("Notifikasi : "+e);
                }finally{
                    if(rsobat != null){
                        rsobat.close();
                    }

                    if(psresep != null){
                        psresep.close();
                    }
                }
            }  
            if (iyembuilder.length() > 0) {
                iyembuilder.setLength(iyembuilder.length() - 1);
                fileWriter.write("{\"peresepandokter\":["+iyembuilder+"]}");
                fileWriter.flush();
            }else{
                fileWriter.write("{\"peresepandokter\":[]}");
                fileWriter.flush();
            }
            
            fileWriter.close();
            iyembuilder=null;
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }  
    }
    
    private void tampilcacheresep() {  
        try{
            boolean[] pilih; 
            double[] jumlah,harga,beli,stok;
            String[] kodebarang,namabarang,kodesatuan,letakbarang,namajenis,aturan,industri;
            z=0;
            for(i=0;i<tbResep.getRowCount();i++){
                if(!tbResep.getValueAt(i,1).toString().equals("")){
                    z++;
                }
            }    

            pilih=new boolean[z]; 
            jumlah=new double[z];
            harga=new double[z];
            kodebarang=new String[z];
            namabarang=new String[z];
            kodesatuan=new String[z];
            letakbarang=new String[z];
            namajenis=new String[z];                   
            aturan=new String[z];           
            industri=new String[z];         
            beli=new double[z];
            stok=new double[z]; 
            z=0;        
            for(i=0;i<tbResep.getRowCount();i++){
                if(!tbResep.getValueAt(i,1).toString().equals("")){
                    pilih[z]=Boolean.parseBoolean(tbResep.getValueAt(i,0).toString());                
                    try {
                        jumlah[z]=Double.parseDouble(tbResep.getValueAt(i,1).toString());
                    } catch (Exception e) {
                        jumlah[z]=0;
                    }  
                    aturan[z]=tbResep.getValueAt(i,2).toString();
                    kodebarang[z]=tbResep.getValueAt(i,3).toString();
                    namabarang[z]=tbResep.getValueAt(i,4).toString();
                    kodesatuan[z]=tbResep.getValueAt(i,5).toString();
                    try {
                        letakbarang[z]=tbResep.getValueAt(i,6).toString();
                    } catch (Exception e) {
                        letakbarang[z]="";
                    }

                    try {
                        harga[z]=Double.parseDouble(tbResep.getValueAt(i,7).toString());
                    } catch (Exception e) {
                        harga[z]=0;
                    }                  
                    namajenis[z]=tbResep.getValueAt(i,8).toString();
                    industri[z]=tbResep.getValueAt(i,9).toString();
                    try {
                        beli[z]=Double.parseDouble(tbResep.getValueAt(i,10).toString());
                    } catch (Exception e) {
                        beli[z]=0;
                    } 

                    try {
                        stok[z]=Double.parseDouble(tbResep.getValueAt(i,11).toString());
                    } catch (Exception e) {
                        stok[z]=0;
                    } 
                    z++;
                }
            }

            Valid.tabelKosong(tabModeResep);             

            for(i=0;i<z;i++){
                tabModeResep.addRow(new Object[] {
                    pilih[i],jumlah[i],aturan[i],kodebarang[i],namabarang[i],kodesatuan[i],letakbarang[i],harga[i],namajenis[i],industri[i],beli[i],stok[i]
                });
            }
            
            pilih=null; 
            jumlah=null;
            harga=null;
            kodebarang=null;
            namabarang=null;
            kodesatuan=null;
            letakbarang=null;
            namajenis=null;                   
            aturan=null;          
            industri=null;        
            beli=null;
            stok=null; 

            myObj = new FileReader("./cache/peresepandokter.iyem");
            root = mapper.readTree(myObj);
            response = root.path("peresepandokter");
            if(response.isArray()){
                if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                    if(TCari.getText().trim().equals("")){
                        for(JsonNode list:response){
                            tabModeResep.addRow(new Object[] {
                                false,"","",list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),list.path("Satuan").asText(),list.path("Kandungan").asText(),
                                Double.parseDouble(list.path("HargaKaryawan").asText()),list.path("Jenis").asText(),list.path("IndustriFarmasi").asText(),
                                Double.parseDouble(list.path("HargaBeli").asText()),Double.parseDouble(list.path("Stok").asText())
                            });
                        }
                    }else{
                        for(JsonNode list:response){
                            if(list.path("KodeBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("NamaBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Jenis").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Kandungan").asText().toLowerCase().contains(TCari.getText().toLowerCase())){
                                tabModeResep.addRow(new Object[] {
                                    false,"","",list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),list.path("Satuan").asText(),list.path("Kandungan").asText(),
                                    Double.parseDouble(list.path("HargaKaryawan").asText()),list.path("Jenis").asText(),list.path("IndustriFarmasi").asText(),
                                    Double.parseDouble(list.path("HargaBeli").asText()),Double.parseDouble(list.path("Stok").asText())
                                });
                            }
                        }
                    }
                }else if(Jeniskelas.getSelectedItem().equals("Rawat Jalan")){
                    if(TCari.getText().trim().equals("")){
                        for(JsonNode list:response){
                            tabModeResep.addRow(new Object[] {
                                false,"","",list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),list.path("Satuan").asText(),list.path("Kandungan").asText(),
                                Double.parseDouble(list.path("HargaRalan").asText()),list.path("Jenis").asText(),list.path("IndustriFarmasi").asText(),
                                Double.parseDouble(list.path("HargaBeli").asText()),Double.parseDouble(list.path("Stok").asText())
                            });
                        }
                    }else{
                        for(JsonNode list:response){
                            if(list.path("KodeBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("NamaBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Jenis").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Kandungan").asText().toLowerCase().contains(TCari.getText().toLowerCase())){
                                tabModeResep.addRow(new Object[] {
                                    false,"","",list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),list.path("Satuan").asText(),list.path("Kandungan").asText(),
                                    Double.parseDouble(list.path("HargaRalan").asText()),list.path("Jenis").asText(),list.path("IndustriFarmasi").asText(),
                                    Double.parseDouble(list.path("HargaBeli").asText()),Double.parseDouble(list.path("Stok").asText())
                                });
                            }
                        }
                    }
                }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                    if(TCari.getText().trim().equals("")){
                        for(JsonNode list:response){
                            tabModeResep.addRow(new Object[] {
                                false,"","",list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),list.path("Satuan").asText(),list.path("Kandungan").asText(),
                                Double.parseDouble(list.path("HargaBeliLuar").asText()),list.path("Jenis").asText(),list.path("IndustriFarmasi").asText(),
                                Double.parseDouble(list.path("HargaBeli").asText()),Double.parseDouble(list.path("Stok").asText())
                            });
                        }
                    }else{
                        for(JsonNode list:response){
                            if(list.path("KodeBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("NamaBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Jenis").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Kandungan").asText().toLowerCase().contains(TCari.getText().toLowerCase())){
                                tabModeResep.addRow(new Object[] {
                                    false,"","",list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),list.path("Satuan").asText(),list.path("Kandungan").asText(),
                                    Double.parseDouble(list.path("HargaBeliLuar").asText()),list.path("Jenis").asText(),list.path("IndustriFarmasi").asText(),
                                    Double.parseDouble(list.path("HargaBeli").asText()),Double.parseDouble(list.path("Stok").asText())
                                });
                            }
                        }
                    }
                }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                    if(TCari.getText().trim().equals("")){
                        for(JsonNode list:response){
                            tabModeResep.addRow(new Object[] {
                                false,"","",list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),list.path("Satuan").asText(),list.path("Kandungan").asText(),
                                Double.parseDouble(list.path("HargaUtama").asText()),list.path("Jenis").asText(),list.path("IndustriFarmasi").asText(),
                                Double.parseDouble(list.path("HargaBeli").asText()),Double.parseDouble(list.path("Stok").asText())
                            });
                        }
                    }else{
                        for(JsonNode list:response){
                            if(list.path("KodeBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("NamaBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Jenis").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Kandungan").asText().toLowerCase().contains(TCari.getText().toLowerCase())){
                                tabModeResep.addRow(new Object[] {
                                    false,"","",list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),list.path("Satuan").asText(),list.path("Kandungan").asText(),
                                    Double.parseDouble(list.path("HargaUtama").asText()),list.path("Jenis").asText(),list.path("IndustriFarmasi").asText(),
                                    Double.parseDouble(list.path("HargaBeli").asText()),Double.parseDouble(list.path("Stok").asText())
                                });
                            }
                        }
                    }
                }else if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                    if(TCari.getText().trim().equals("")){
                        for(JsonNode list:response){
                            tabModeResep.addRow(new Object[] {
                                false,"","",list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),list.path("Satuan").asText(),list.path("Kandungan").asText(),
                                Double.parseDouble(list.path("HargaKelas1").asText()),list.path("Jenis").asText(),list.path("IndustriFarmasi").asText(),
                                Double.parseDouble(list.path("HargaBeli").asText()),Double.parseDouble(list.path("Stok").asText())
                            });
                        }
                    }else{
                        for(JsonNode list:response){
                            if(list.path("KodeBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("NamaBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Jenis").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Kandungan").asText().toLowerCase().contains(TCari.getText().toLowerCase())){
                                tabModeResep.addRow(new Object[] {
                                    false,"","",list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),list.path("Satuan").asText(),list.path("Kandungan").asText(),
                                    Double.parseDouble(list.path("HargaKelas1").asText()),list.path("Jenis").asText(),list.path("IndustriFarmasi").asText(),
                                    Double.parseDouble(list.path("HargaBeli").asText()),Double.parseDouble(list.path("Stok").asText())
                                });
                            }
                        }
                    }
                }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                    if(TCari.getText().trim().equals("")){
                        for(JsonNode list:response){
                            tabModeResep.addRow(new Object[] {
                                false,"","",list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),list.path("Satuan").asText(),list.path("Kandungan").asText(),
                                Double.parseDouble(list.path("HargaKelas2").asText()),list.path("Jenis").asText(),list.path("IndustriFarmasi").asText(),
                                Double.parseDouble(list.path("HargaBeli").asText()),Double.parseDouble(list.path("Stok").asText())
                            });
                        }
                    }else{
                        for(JsonNode list:response){
                            if(list.path("KodeBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("NamaBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Jenis").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Kandungan").asText().toLowerCase().contains(TCari.getText().toLowerCase())){
                                tabModeResep.addRow(new Object[] {
                                    false,"","",list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),list.path("Satuan").asText(),list.path("Kandungan").asText(),
                                    Double.parseDouble(list.path("HargaKelas2").asText()),list.path("Jenis").asText(),list.path("IndustriFarmasi").asText(),
                                    Double.parseDouble(list.path("HargaBeli").asText()),Double.parseDouble(list.path("Stok").asText())
                                });
                            }
                        }
                    }
                }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                    if(TCari.getText().trim().equals("")){
                        for(JsonNode list:response){
                            tabModeResep.addRow(new Object[] {
                                false,"","",list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),list.path("Satuan").asText(),list.path("Kandungan").asText(),
                                Double.parseDouble(list.path("HargaKelas3").asText()),list.path("Jenis").asText(),list.path("IndustriFarmasi").asText(),
                                Double.parseDouble(list.path("HargaBeli").asText()),Double.parseDouble(list.path("Stok").asText())
                            });
                        }
                    }else{
                        for(JsonNode list:response){
                            if(list.path("KodeBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("NamaBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Jenis").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Kandungan").asText().toLowerCase().contains(TCari.getText().toLowerCase())){
                                tabModeResep.addRow(new Object[] {
                                    false,"","",list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),list.path("Satuan").asText(),list.path("Kandungan").asText(),
                                    Double.parseDouble(list.path("HargaKelas3").asText()),list.path("Jenis").asText(),list.path("IndustriFarmasi").asText(),
                                    Double.parseDouble(list.path("HargaBeli").asText()),Double.parseDouble(list.path("Stok").asText())
                                });
                            }
                        }
                    }
                }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                    if(TCari.getText().trim().equals("")){
                        for(JsonNode list:response){
                            tabModeResep.addRow(new Object[] {
                                false,"","",list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),list.path("Satuan").asText(),list.path("Kandungan").asText(),
                                Double.parseDouble(list.path("HargaVIP").asText()),list.path("Jenis").asText(),list.path("IndustriFarmasi").asText(),
                                Double.parseDouble(list.path("HargaBeli").asText()),Double.parseDouble(list.path("Stok").asText())
                            });
                        }
                    }else{
                        for(JsonNode list:response){
                            if(list.path("KodeBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("NamaBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Jenis").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Kandungan").asText().toLowerCase().contains(TCari.getText().toLowerCase())){
                                tabModeResep.addRow(new Object[] {
                                    false,"","",list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),list.path("Satuan").asText(),list.path("Kandungan").asText(),
                                    Double.parseDouble(list.path("HargaVIP").asText()),list.path("Jenis").asText(),list.path("IndustriFarmasi").asText(),
                                    Double.parseDouble(list.path("HargaBeli").asText()),Double.parseDouble(list.path("Stok").asText())
                                });
                            }
                        }
                    }
                }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                    if(TCari.getText().trim().equals("")){
                        for(JsonNode list:response){
                            tabModeResep.addRow(new Object[] {
                                false,"","",list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),list.path("Satuan").asText(),list.path("Kandungan").asText(),
                                Double.parseDouble(list.path("HargaVVIP").asText()),list.path("Jenis").asText(),list.path("IndustriFarmasi").asText(),
                                Double.parseDouble(list.path("HargaBeli").asText()),Double.parseDouble(list.path("Stok").asText())
                            });
                        }
                    }else{
                        for(JsonNode list:response){
                            if(list.path("KodeBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("NamaBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Jenis").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Kandungan").asText().toLowerCase().contains(TCari.getText().toLowerCase())){
                                tabModeResep.addRow(new Object[] {
                                    false,"","",list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),list.path("Satuan").asText(),list.path("Kandungan").asText(),
                                    Double.parseDouble(list.path("HargaVVIP").asText()),list.path("Jenis").asText(),list.path("IndustriFarmasi").asText(),
                                    Double.parseDouble(list.path("HargaBeli").asText()),Double.parseDouble(list.path("Stok").asText())
                                });
                            }
                        }
                    }
                } 
            }
            myObj.close();
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }            
    }

    public void emptTeksobat() {
        if(ChkRM.isSelected()==true){
            Valid.autoNomer3("select ifnull(MAX(CONVERT(RIGHT(resep_obat.no_resep,4),signed)),0) from resep_obat where resep_obat.tgl_peresepan='"+Valid.SetTgl(DTPBeri.getSelectedItem()+"")+"'",
                DTPBeri.getSelectedItem().toString().substring(6,10)+DTPBeri.getSelectedItem().toString().substring(3,5)+DTPBeri.getSelectedItem().toString().substring(0,2),4,NoResep);        
        } 
    }

    public JTable getTable(){
        return tbResep;
    }

    public java.awt.Component ambilKontenUntukEmbed() {
        java.awt.Container parent=internalFrame1.getParent();
        if(parent!=null){
            parent.remove(internalFrame1);
        }
        return internalFrame1;
    }

    public void aktifkanModeEmbedded() {
        modeEmbedded=true;
        BtnKeluar.setVisible(false);
        internalFrame1.setBorder(null);
        sembunyikanTabRacikanLama();
        TabRawatMouseClicked(null);
    }
    
    public Button getButton(){
        return BtnSimpan;
    }
    
    public void isCek(){   
        BtnTambah.setEnabled(akses.getresep_dokter());
        TCari.requestFocus();
        tentukanBangsalResep();
        
        if(TANGGALMUNDUR.equals("no")){
            if(!akses.getkode().equals("Admin Utama")){
                DTPBeri.setEditable(false);
                DTPBeri.setEnabled(false);
                ChkJln.setEnabled(false);
                cmbJam.setEnabled(false);
                cmbMnt.setEnabled(false);
                cmbDtk.setEnabled(false);
                ChkRM.setEnabled(false);
                NoResep.setEnabled(false);
            }
        }
    }
    
    private void tentukanBangsalResep(){
        if(!DEPOAKTIFOBAT.equals("")){
            bangsal=DEPOAKTIFOBAT;
        }else{
            if(status.equals("ralan")){
                bangsal=Sequel.cariIsi("select set_depo_ralan.kd_bangsal from set_depo_ralan where set_depo_ralan.kd_poli=?",Sequel.cariIsi("select reg_periksa.kd_poli from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText()));
                if(bangsal.equals("")){
                    bangsal=bangsaldefault;
                }
            }else if(status.equals("ranap")){
                bangsal=akses.getkdbangsal();
                if(bangsal.equals("")){
                    bangsal=bangsaldefault;
                }
            }else if(bangsal.equals("")){
                bangsal=bangsaldefault;
            }
        }
    }

    public void setNoRm(String norwt,Date tanggal, String jam,String menit,String detik,String KodeDokter,String NamaDokter,String status) {        
        TNoRw.setText(norwt);
        Sequel.cariIsi("select concat(pasien.no_rkm_medis,' ',pasien.nm_pasien,' (',pasien.umur,')') from reg_periksa inner join pasien "+
                    " on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where no_rawat=? ",TPasien,TNoRw.getText());
        
        DTPBeri.setDate(tanggal);
        cmbJam.setSelectedItem(jam);
        cmbMnt.setSelectedItem(menit);
        cmbDtk.setSelectedItem(detik); 
        KdDokter.setText(KodeDokter);
        NmDokter.setText(NamaDokter);
        KdPj.setText(Sequel.cariIsi("select reg_periksa.kd_pj from reg_periksa where reg_periksa.no_rawat=?",norwt));
        TCari.requestFocus();
        this.status=status;
        tentukanBangsalResep();
        SetHarga();
        tampilkanPlanSOAPDokter(norwt);
        tampilkanTindakanPasien(norwt);
        ubah=false;
        copy=false;
        draftTerapiSOAPSudahDiterapkan=false;
        resepBerhasilDisimpan=false;
        noResepTersimpan="";
        kosongkanCatatanDokter();
    }
    
    public void setNoRm(String norwt,String KodeDokter,String NamaDokter,String Pasien,String kodepj,String status) {        
        TNoRw.setText(norwt);
        TPasien.setText(Pasien);
        KdDokter.setText(KodeDokter);
        NmDokter.setText(NamaDokter);
        KdPj.setText(kodepj);
        TCari.requestFocus();
        this.status=status;
        tentukanBangsalResep();
        SetHarga();
        tampilkanPlanSOAPDokter(norwt);
        tampilkanTindakanPasien(norwt);
        ubah=false;
        copy=false;
        draftTerapiSOAPSudahDiterapkan=false;
        resepBerhasilDisimpan=false;
        noResepTersimpan="";
        kosongkanCatatanDokter();
    }
    
    public void setNoRm(String norwt,Date tanggal,String status) {        
        TNoRw.setText(norwt);
        Sequel.cariIsi("select concat(pasien.no_rkm_medis,' ',pasien.nm_pasien) from reg_periksa inner join pasien "+
                    " on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where no_rawat=? ",TPasien,TNoRw.getText());
        
        DTPBeri.setDate(tanggal);
        KdDokter.setText(Sequel.cariIsi("select dpjp_ranap.kd_dokter from dpjp_ranap where dpjp_ranap.no_rawat=?",norwt));
        if(KdDokter.getText().equals("")){
            KdDokter.setText(Sequel.cariIsi("select reg_periksa.kd_dokter from reg_periksa where reg_periksa.no_rawat=?",norwt));
        }
        NmDokter.setText(dokter.tampil3(KdDokter.getText()));
        
        KdPj.setText(Sequel.cariIsi("select reg_periksa.kd_pj from reg_periksa where reg_periksa.no_rawat=?",norwt));
        TCari.requestFocus();
        this.status=status;
        tentukanBangsalResep();
        SetHarga();
        tampilkanPlanSOAPDokter(norwt);
        tampilkanTindakanPasien(norwt);
        ubah=false;
        copy=false;
        draftTerapiSOAPSudahDiterapkan=false;
        resepBerhasilDisimpan=false;
        noResepTersimpan="";
        kosongkanCatatanDokter();
    }

    public void setDraftTerapiSOAP(String draftTerapiSOAP) {
        this.draftTerapiSOAP = draftTerapiSOAP==null ? "" : draftTerapiSOAP.trim();
        this.draftTerapiSOAPSudahDiterapkan=false;
        tampilkanDraftTerapiSOAP();
    }

    public boolean isResepBerhasilDisimpan() {
        return resepBerhasilDisimpan;
    }

    public String getNoResepTersimpan() {
        return noResepTersimpan;
    }

    private void inisialisasiPanelDraftSOAP() {
        panelUmumResep = new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelUmumResep.setName("panelUmumResep");
        panelUmumResep.setOpaque(false);
        panelRacikanResep = new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelRacikanResep.setName("panelRacikanResep");
        panelRacikanResep.setOpaque(false);

        panelDraftSOAP = new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelDraftSOAP.setName("panelDraftSOAP");
        panelDraftSOAP.setOpaque(false);
        panelDraftSOAP.setPreferredSize(new java.awt.Dimension(460,100));
        panelDraftSOAPRacikan = new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelDraftSOAPRacikan.setName("panelDraftSOAPRacikan");
        panelDraftSOAPRacikan.setOpaque(false);
        panelDraftSOAPRacikan.setPreferredSize(new java.awt.Dimension(460,100));
        panelDraftSOAPRacikanV2 = new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelDraftSOAPRacikanV2.setName("panelDraftSOAPRacikanV2");
        panelDraftSOAPRacikanV2.setOpaque(false);
        panelDraftSOAPRacikanV2.setPreferredSize(new java.awt.Dimension(460,100));

        labelDraftSOAP = new widget.Label();
        labelDraftSOAP.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelDraftSOAP.setText("Plan SOAP Dokter :");
        labelDraftSOAP.setPreferredSize(new java.awt.Dimension(100,23));
        labelDraftSOAPRacikan = new widget.Label();
        labelDraftSOAPRacikan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelDraftSOAPRacikan.setText("Plan SOAP Dokter :");
        labelDraftSOAPRacikan.setPreferredSize(new java.awt.Dimension(100,23));
        labelDraftSOAPRacikanV2 = new widget.Label();
        labelDraftSOAPRacikanV2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelDraftSOAPRacikanV2.setText("Plan SOAP Dokter :");
        labelDraftSOAPRacikanV2.setPreferredSize(new java.awt.Dimension(100,23));
        labelCatatanDokter = new widget.Label();
        labelCatatanDokter.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelCatatanDokter.setText("Catatan Dokter :");
        labelCatatanDokter.setPreferredSize(new java.awt.Dimension(100,23));
        labelCatatanDokterRacikanV2 = new widget.Label();
        labelCatatanDokterRacikanV2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelCatatanDokterRacikanV2.setText("Catatan Dokter :");
        labelCatatanDokterRacikanV2.setPreferredSize(new java.awt.Dimension(100,23));

        areaDraftSOAP = new javax.swing.JTextArea();
        areaDraftSOAP.setEditable(false);
        areaDraftSOAP.setLineWrap(true);
        areaDraftSOAP.setWrapStyleWord(true);
        areaDraftSOAP.setMargin(new java.awt.Insets(6,6,6,6));
        areaDraftSOAP.setName("areaDraftSOAP");
        areaDraftSOAPRacikan = new javax.swing.JTextArea();
        areaDraftSOAPRacikan.setEditable(false);
        areaDraftSOAPRacikan.setLineWrap(true);
        areaDraftSOAPRacikan.setWrapStyleWord(true);
        areaDraftSOAPRacikan.setMargin(new java.awt.Insets(6,6,6,6));
        areaDraftSOAPRacikan.setName("areaDraftSOAPRacikan");
        areaDraftSOAPRacikanV2 = new javax.swing.JTextArea();
        areaDraftSOAPRacikanV2.setEditable(false);
        areaDraftSOAPRacikanV2.setLineWrap(true);
        areaDraftSOAPRacikanV2.setWrapStyleWord(true);
        areaDraftSOAPRacikanV2.setMargin(new java.awt.Insets(6,6,6,6));
        areaDraftSOAPRacikanV2.setName("areaDraftSOAPRacikanV2");
        areaCatatanDokter = new javax.swing.JTextArea();
        areaCatatanDokter.setLineWrap(true);
        areaCatatanDokter.setWrapStyleWord(true);
        areaCatatanDokter.setMargin(new java.awt.Insets(6,6,6,6));
        areaCatatanDokter.setName("areaCatatanDokter");
        areaCatatanDokterRacikanV2 = new javax.swing.JTextArea();
        areaCatatanDokterRacikanV2.setLineWrap(true);
        areaCatatanDokterRacikanV2.setWrapStyleWord(true);
        areaCatatanDokterRacikanV2.setMargin(new java.awt.Insets(6,6,6,6));
        areaCatatanDokterRacikanV2.setName("areaCatatanDokterRacikanV2");
        pasangSinkronCatatanDokter(areaCatatanDokter);
        pasangSinkronCatatanDokter(areaCatatanDokterRacikanV2);

        scrollDraftSOAP = new widget.ScrollPane();
        scrollDraftSOAP.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255,255,255)));
        scrollDraftSOAP.setName("scrollDraftSOAP");
        scrollDraftSOAP.setViewportView(areaDraftSOAP);
        scrollDraftSOAPRacikan = new widget.ScrollPane();
        scrollDraftSOAPRacikan.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255,255,255)));
        scrollDraftSOAPRacikan.setName("scrollDraftSOAPRacikan");
        scrollDraftSOAPRacikan.setViewportView(areaDraftSOAPRacikan);
        scrollDraftSOAPRacikanV2 = new widget.ScrollPane();
        scrollDraftSOAPRacikanV2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255,255,255)));
        scrollDraftSOAPRacikanV2.setName("scrollDraftSOAPRacikanV2");
        scrollDraftSOAPRacikanV2.setViewportView(areaDraftSOAPRacikanV2);
        scrollCatatanDokter = new widget.ScrollPane();
        scrollCatatanDokter.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255,255,255)));
        scrollCatatanDokter.setName("scrollCatatanDokter");
        scrollCatatanDokter.setPreferredSize(new java.awt.Dimension(230,185));
        scrollCatatanDokter.setViewportView(areaCatatanDokter);
        scrollCatatanDokterRacikanV2 = new widget.ScrollPane();
        scrollCatatanDokterRacikanV2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255,255,255)));
        scrollCatatanDokterRacikanV2.setName("scrollCatatanDokterRacikanV2");
        scrollCatatanDokterRacikanV2.setPreferredSize(new java.awt.Dimension(230,185));
        scrollCatatanDokterRacikanV2.setViewportView(areaCatatanDokterRacikanV2);

        javax.swing.JPanel panelPlanSOAP=new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelPlanSOAP.setName("panelPlanSOAP");
        panelPlanSOAP.setOpaque(false);
        panelPlanSOAP.add(labelDraftSOAP, java.awt.BorderLayout.PAGE_START);
        panelPlanSOAP.add(scrollDraftSOAP, java.awt.BorderLayout.CENTER);
        javax.swing.JSplitPane splitInfoSOAP=new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT,panelPlanSOAP,panelTindakanPasien);
        splitInfoSOAP.setName("splitInfoSOAP");
        splitInfoSOAP.setBorder(null);
        splitInfoSOAP.setResizeWeight(0.68);
        splitInfoSOAP.setDividerLocation(225);
        javax.swing.JPanel panelCatatanSOAP=new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelCatatanSOAP.setName("panelCatatanSOAP");
        panelCatatanSOAP.setOpaque(false);
        panelCatatanSOAP.setPreferredSize(new java.awt.Dimension(230,210));
        panelCatatanSOAP.add(labelCatatanDokter, java.awt.BorderLayout.PAGE_START);
        panelCatatanSOAP.add(scrollCatatanDokter, java.awt.BorderLayout.CENTER);
        panelDraftSOAP.add(splitInfoSOAP, java.awt.BorderLayout.CENTER);
        panelDraftSOAP.add(panelCatatanSOAP, java.awt.BorderLayout.PAGE_END);

        javax.swing.JPanel panelPlanSOAPRacikan=new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelPlanSOAPRacikan.setName("panelPlanSOAPRacikan");
        panelPlanSOAPRacikan.setOpaque(false);
        panelPlanSOAPRacikan.add(labelDraftSOAPRacikan, java.awt.BorderLayout.PAGE_START);
        panelPlanSOAPRacikan.add(scrollDraftSOAPRacikan, java.awt.BorderLayout.CENTER);
        javax.swing.JSplitPane splitInfoSOAPRacikan=new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT,panelPlanSOAPRacikan,panelTindakanPasienRacikan);
        splitInfoSOAPRacikan.setName("splitInfoSOAPRacikan");
        splitInfoSOAPRacikan.setBorder(null);
        splitInfoSOAPRacikan.setResizeWeight(0.35);
        splitInfoSOAPRacikan.setDividerLocation(210);
        panelDraftSOAPRacikan.add(splitInfoSOAPRacikan, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel panelPlanSOAPRacikanV2=new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelPlanSOAPRacikanV2.setName("panelPlanSOAPRacikanV2");
        panelPlanSOAPRacikanV2.setOpaque(false);
        panelPlanSOAPRacikanV2.add(labelDraftSOAPRacikanV2, java.awt.BorderLayout.PAGE_START);
        panelPlanSOAPRacikanV2.add(scrollDraftSOAPRacikanV2, java.awt.BorderLayout.CENTER);
        javax.swing.JSplitPane splitInfoSOAPRacikanV2=new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT,panelPlanSOAPRacikanV2,panelTindakanPasienRacikanV2);
        splitInfoSOAPRacikanV2.setName("splitInfoSOAPRacikanV2");
        splitInfoSOAPRacikanV2.setBorder(null);
        splitInfoSOAPRacikanV2.setResizeWeight(0.68);
        splitInfoSOAPRacikanV2.setDividerLocation(225);
        javax.swing.JPanel panelCatatanSOAPRacikanV2=new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelCatatanSOAPRacikanV2.setName("panelCatatanSOAPRacikanV2");
        panelCatatanSOAPRacikanV2.setOpaque(false);
        panelCatatanSOAPRacikanV2.setPreferredSize(new java.awt.Dimension(230,210));
        panelCatatanSOAPRacikanV2.add(labelCatatanDokterRacikanV2, java.awt.BorderLayout.PAGE_START);
        panelCatatanSOAPRacikanV2.add(scrollCatatanDokterRacikanV2, java.awt.BorderLayout.CENTER);
        panelDraftSOAPRacikanV2.add(splitInfoSOAPRacikanV2, java.awt.BorderLayout.CENTER);
        panelDraftSOAPRacikanV2.add(panelCatatanSOAPRacikanV2, java.awt.BorderLayout.PAGE_END);

        int indexUmum = TabRawat.indexOfComponent(Scroll);
        if(indexUmum>-1) {
            TabRawat.remove(indexUmum);
            panelUmumResep.add(Scroll, java.awt.BorderLayout.CENTER);
            panelUmumResep.add(panelDraftSOAP, java.awt.BorderLayout.EAST);
            TabRawat.insertTab("Umum", null, panelUmumResep, null, indexUmum);
        }

        int indexRacikan = TabRawat.indexOfComponent(jPanel3);
        if(indexRacikan>-1) {
            TabRawat.remove(indexRacikan);
            panelRacikanResep.add(jPanel3, java.awt.BorderLayout.CENTER);
            panelRacikanResep.add(panelDraftSOAPRacikan, java.awt.BorderLayout.EAST);
        }

        TabRawat.setSelectedIndex(0);
        tampilkanDraftTerapiSOAP();
    }

    private void inisialisasiRacikanV2() {
        tabModeResepRacikanV2=new DefaultTableModel(null,new Object[]{
            "No","Nama Racikan","Kode Racik","Metode Racik","Jml.Racik","Aturan Pakai","Keterangan"
        }){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){
                return !((colIndex==0)||(colIndex==2));
            }
            Class[] types = new Class[] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            @Override
            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        };

        tabModeDetailResepRacikanV2=new DefaultTableModel(null,new Object[]{
            "Racikan","K","Jumlah","Aturan Pakai","Kode Barang","Nama Barang","Satuan",
            "Komposisi","Harga(Rp)","Jenis Obat","I.F.","H.Beli","Stok"
        }){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){
                return colIndex==2;
            }
            Class[] types = new Class[] {
                java.lang.Object.class, java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Double.class, java.lang.Object.class, java.lang.Object.class, java.lang.Double.class,
                java.lang.Double.class
            };
            @Override
            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        };

        tbRacikanV2=new widget.Table();
        tbRacikanV2.setName("tbRacikanV2");
        tbRacikanV2.setModel(tabModeResepRacikanV2);
        tbRacikanV2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        aturKolomRacikanV2();
        pasangEditorRacikanV2();
        tbRacikanV2.setDefaultRenderer(Object.class,warna2);
        tabModeResepRacikanV2.addTableModelListener((javax.swing.event.TableModelEvent e) -> {
            if(e.getType()==javax.swing.event.TableModelEvent.UPDATE && e.getFirstRow()>=0){
                if(e.getColumn()==1 || e.getColumn()==3){
                    sinkronLabelDetailRacikanV2(e.getFirstRow());
                    tampilkanLabelRacikanAktifV2();
                    tampilkanDetailRacikanV2Aktif();
                }
                if(e.getColumn()==3){
                    isiKodeMetodeRacikV2(e.getFirstRow());
                }
            }
        });
        tbRacikanV2.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if(!e.getValueIsAdjusting()){
                tampilkanLabelRacikanAktifV2();
                tampilkanDetailRacikanV2Aktif();
            }
        });
        tbRacikanV2.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                tbRacikanV2KeyPressed(evt);
            }
        });

        tbDetailRacikanV2=new widget.Table();
        tbDetailRacikanV2.setName("tbDetailRacikanV2");
        tbDetailRacikanV2.setModel(tabModeDetailResepRacikanV2);
        tbDetailRacikanV2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbDetailRacikanV2.setComponentPopupMenu(Popup);
        tbDetailRacikanV2.setRowSorter(new javax.swing.table.TableRowSorter<DefaultTableModel>(tabModeDetailResepRacikanV2));
        aturKolomDetailRacikanV2();
        tbDetailRacikanV2.setDefaultRenderer(Object.class,warna);
        tbDetailRacikanV2.addPropertyChangeListener(evt -> {
            if(this.isVisible()==true || modeEmbedded){
                hitungResep();
            }
        });

        labelRacikanAktifV2=new widget.Label();
        labelRacikanAktifV2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelRacikanAktifV2.setText("Obat untuk racikan: pilih/isi racikan terlebih dahulu");
        labelRacikanAktifV2.setPreferredSize(new java.awt.Dimension(100,23));

        widget.ScrollPane scrollHeader=new widget.ScrollPane();
        scrollHeader.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255,255,255)));
        scrollHeader.setName("scrollRacikanV2");
        scrollHeader.setOpaque(true);
        scrollHeader.setPreferredSize(new java.awt.Dimension(454, 92));
        scrollHeader.setViewportView(tbRacikanV2);

        widget.ScrollPane scrollDetail=new widget.ScrollPane();
        scrollDetail.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255,255,255)));
        scrollDetail.setName("scrollDetailRacikanV2");
        scrollDetail.setOpaque(true);
        scrollDetail.setViewportView(tbDetailRacikanV2);

        javax.swing.JPanel panelDetail=new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelDetail.setName("panelDetailRacikanV2");
        panelDetail.setOpaque(false);
        panelDetail.add(labelRacikanAktifV2, java.awt.BorderLayout.PAGE_START);
        panelDetail.add(scrollDetail, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel panelTabelRacikanV2=new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelTabelRacikanV2.setName("panelTabelRacikanV2");
        panelTabelRacikanV2.setOpaque(false);
        panelTabelRacikanV2.add(scrollHeader, java.awt.BorderLayout.PAGE_START);
        panelTabelRacikanV2.add(panelDetail, java.awt.BorderLayout.CENTER);

        panelRacikanV2=new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelRacikanV2.setName("panelRacikanV2");
        panelRacikanV2.setOpaque(false);
        panelRacikanV2.add(panelTabelRacikanV2, java.awt.BorderLayout.CENTER);
        panelRacikanV2.add(panelDraftSOAPRacikanV2, java.awt.BorderLayout.EAST);
        TabRawat.addTab("Racikan V2", panelRacikanV2);
        sembunyikanTabRacikanLama();
        tampilkanDetailRacikanV2Aktif();
    }

    private void sembunyikanTabRacikanLama() {
        int index=TabRawat.indexOfComponent(jPanel3);
        if(index>-1) {
            TabRawat.remove(index);
        }

        index=TabRawat.indexOfComponent(panelRacikanResep);
        if(index>-1) {
            TabRawat.remove(index);
        }

        if(TabRawat.getSelectedIndex()<0 && TabRawat.getTabCount()>0) {
            TabRawat.setSelectedIndex(0);
        }
    }

    private boolean tabUmumAktif() {
        return TabRawat.getSelectedComponent()==panelUmumResep || TabRawat.getSelectedComponent()==Scroll;
    }

    private boolean tabRacikanLamaAktif() {
        return TabRawat.getSelectedComponent()==panelRacikanResep || TabRawat.getSelectedComponent()==jPanel3;
    }

    private boolean tabRacikanV2Aktif() {
        return TabRawat.getSelectedComponent()==panelRacikanV2;
    }

    private void pasangSinkronCatatanDokter(javax.swing.JTextArea area) {
        area.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                sinkronCatatanDokter(area);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                sinkronCatatanDokter(area);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                sinkronCatatanDokter(area);
            }
        });
    }

    private void sinkronCatatanDokter(javax.swing.JTextArea sumber) {
        if(sedangSinkronCatatanDokter){
            return;
        }
        sedangSinkronCatatanDokter=true;
        try {
            catatanResepDokter=sumber.getText();
            if(areaCatatanDokter!=null && sumber!=areaCatatanDokter && !areaCatatanDokter.getText().equals(catatanResepDokter)){
                areaCatatanDokter.setText(catatanResepDokter);
            }
            if(areaCatatanDokterRacikanV2!=null && sumber!=areaCatatanDokterRacikanV2 && !areaCatatanDokterRacikanV2.getText().equals(catatanResepDokter)){
                areaCatatanDokterRacikanV2.setText(catatanResepDokter);
            }
        } finally {
            sedangSinkronCatatanDokter=false;
        }
    }

    private void kosongkanCatatanDokter() {
        catatanResepDokter="";
        sedangSinkronCatatanDokter=true;
        try {
            if(areaCatatanDokter!=null){
                areaCatatanDokter.setText("");
            }
            if(areaCatatanDokterRacikanV2!=null){
                areaCatatanDokterRacikanV2.setText("");
            }
        } finally {
            sedangSinkronCatatanDokter=false;
        }
    }

    private void muatCatatanResepDokter(String noResep) {
        pastikanTabelCatatanResepDokter();
        catatanResepDokter=Sequel.cariIsi("select catatan from catatan_resep_dokter where no_resep=?",noResep);
        sedangSinkronCatatanDokter=true;
        try {
            if(areaCatatanDokter!=null){
                areaCatatanDokter.setText(catatanResepDokter);
            }
            if(areaCatatanDokterRacikanV2!=null){
                areaCatatanDokterRacikanV2.setText(catatanResepDokter);
            }
        } finally {
            sedangSinkronCatatanDokter=false;
        }
    }

    private void aturKolomRacikanV2() {
        for (int kolom = 0; kolom < 7; kolom++) {
            TableColumn column = tbRacikanV2.getColumnModel().getColumn(kolom);
            if(kolom==0){
                column.setPreferredWidth(25);
            }else if(kolom==1){
                column.setPreferredWidth(250);
            }else if(kolom==2){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(kolom==3){
                column.setPreferredWidth(100);
            }else if(kolom==4){
                column.setPreferredWidth(60);
            }else if(kolom==5){
                column.setPreferredWidth(200);
            }else if(kolom==6){
                column.setPreferredWidth(250);
            }
        }
    }

    private void pasangEditorRacikanV2() {
        muatPilihanMetodeRacikV2();
        muatPilihanAturanPakaiV2();

        javax.swing.JComboBox<String> cmbMetode=new javax.swing.JComboBox<>();
        cmbMetode.addItem("");
        for(String[] metode:pilihanMetodeRacikV2){
            cmbMetode.addItem(metode[1]);
        }
        tbRacikanV2.getColumnModel().getColumn(3).setCellEditor(new javax.swing.DefaultCellEditor(cmbMetode));

        javax.swing.JComboBox<String> cmbAturan=new javax.swing.JComboBox<>();
        cmbAturan.setEditable(true);
        cmbAturan.addItem("");
        for(String aturan:pilihanAturanPakaiV2){
            cmbAturan.addItem(aturan);
        }
        tbRacikanV2.getColumnModel().getColumn(5).setCellEditor(new javax.swing.DefaultCellEditor(cmbAturan));
    }

    private void muatPilihanMetodeRacikV2() {
        pilihanMetodeRacikV2.clear();
        try(PreparedStatement psMetode=koneksi.prepareStatement("select kd_racik,nm_racik from metode_racik order by nm_racik");
            ResultSet rsMetode=psMetode.executeQuery()){
            while(rsMetode.next()){
                pilihanMetodeRacikV2.add(new String[]{rsMetode.getString("kd_racik"),rsMetode.getString("nm_racik")});
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Metode Racik V2 : "+e);
        }
    }

    private void muatPilihanAturanPakaiV2() {
        pilihanAturanPakaiV2.clear();
        try(PreparedStatement psAturan=koneksi.prepareStatement("select aturan from master_aturan_pakai order by aturan");
            ResultSet rsAturan=psAturan.executeQuery()){
            while(rsAturan.next()){
                pilihanAturanPakaiV2.add(rsAturan.getString("aturan"));
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Aturan Pakai V2 : "+e);
        }
    }

    private void isiKodeMetodeRacikV2(int row) {
        if(row<0 || row>=tbRacikanV2.getRowCount()){
            return;
        }
        String metode=tbRacikanV2.getValueAt(row,3).toString();
        for(String[] pilihan:pilihanMetodeRacikV2){
            if(pilihan[1].equals(metode)){
                tbRacikanV2.setValueAt(pilihan[0],row,2);
                return;
            }
        }
        tbRacikanV2.setValueAt("",row,2);
    }

    private void aturKolomDetailRacikanV2() {
        for (int kolom = 0; kolom < 13; kolom++) {
            TableColumn column = tbDetailRacikanV2.getColumnModel().getColumn(kolom);
            if(kolom==0){
                column.setPreferredWidth(155);
            }else if(kolom==1){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(kolom==2){
                column.setPreferredWidth(55);
            }else if(kolom==3){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(kolom==4){
                column.setPreferredWidth(75);
            }else if(kolom==5){
                column.setPreferredWidth(240);
            }else if(kolom==6){
                column.setPreferredWidth(70);
            }else if(kolom==7){
                column.setPreferredWidth(110);
            }else if(kolom==8){
                column.setPreferredWidth(85);
            }else if(kolom==9){
                column.setPreferredWidth(110);
            }else if(kolom==10){
                column.setPreferredWidth(100);
            }else if(kolom==11){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(kolom==12){
                column.setPreferredWidth(50);
            }
        }
    }

    private void tambahRacikanV2() {
        int nomor=tabModeResepRacikan.getRowCount()+tabModeResepRacikanV2.getRowCount()+1;
        if(nomor==99){
            JOptionPane.showMessageDialog(null,"Maksimal 98 Racikan..!!");
        }else{
            tabModeResepRacikanV2.addRow(new Object[]{""+nomor,"Racikan "+nomor,"","",1,"",""});
            tbRacikanV2.setRowSelectionInterval(tabModeResepRacikanV2.getRowCount()-1,tabModeResepRacikanV2.getRowCount()-1);
            fokusKolomRacikanV2(tabModeResepRacikanV2.getRowCount()-1,3);
            tampilkanLabelRacikanAktifV2();
            tampilkanDetailRacikanV2Aktif();
        }
    }

    private void hapusRacikanV2() {
        selesaiEditRacikanV2();
        if(tbRacikanV2.getSelectedRow()==-1){
            JOptionPane.showMessageDialog(null,"Silahkan pilih racikan V2 yang mau dihapus..!!");
            return;
        }
        String noRacik=tbRacikanV2.getValueAt(tbRacikanV2.getSelectedRow(),0).toString();
        boolean adaObat=false;
        for(int baris=0;baris<tabModeDetailResepRacikanV2.getRowCount();baris++){
            if(tabModeDetailResepRacikanV2.getValueAt(baris,0).toString().startsWith(noRacik+" - ")&&Valid.SetAngka(tabModeDetailResepRacikanV2.getValueAt(baris,2).toString())>0){
                adaObat=true;
            }
        }
        if(adaObat){
            JOptionPane.showMessageDialog(null,"Maaf racikan sudah memiliki obat, bersihkan jumlah obatnya dulu..!!");
            return;
        }
        for(int baris=tabModeDetailResepRacikanV2.getRowCount()-1;baris>=0;baris--){
            if(tabModeDetailResepRacikanV2.getValueAt(baris,0).toString().startsWith(noRacik+" - ")){
                tabModeDetailResepRacikanV2.removeRow(baris);
            }
        }
        tabModeResepRacikanV2.removeRow(tbRacikanV2.getSelectedRow());
        tampilkanLabelRacikanAktifV2();
        tampilkanDetailRacikanV2Aktif();
    }

    private boolean validasiRacikanAktifV2() {
        selesaiEditRacikanV2();
        if(tbRacikanV2.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Silahkan tambah racikan V2 dulu..!!");
            return false;
        }
        if(tbRacikanV2.getSelectedRow()==-1){
            JOptionPane.showMessageDialog(null,"Silahkan pilih racikan V2 yang akan diisi obat..!!");
            return false;
        }
        if(tbRacikanV2.getValueAt(tbRacikanV2.getSelectedRow(),1).toString().trim().equals("")||
                tbRacikanV2.getValueAt(tbRacikanV2.getSelectedRow(),2).toString().trim().equals("")||
                tbRacikanV2.getValueAt(tbRacikanV2.getSelectedRow(),3).toString().trim().equals("")||
                tbRacikanV2.getValueAt(tbRacikanV2.getSelectedRow(),4).toString().trim().equals("")||
                tbRacikanV2.getValueAt(tbRacikanV2.getSelectedRow(),5).toString().trim().equals("")){
            JOptionPane.showMessageDialog(null,"Lengkapi Nama Racikan, Metode Racik, Jml.Racik, dan Aturan Pakai dulu..!!");
            fokusKolomRacikanV2(tbRacikanV2.getSelectedRow(),kolomRacikanV2BelumLengkap(tbRacikanV2.getSelectedRow()));
            return false;
        }
        return true;
    }

    private int kolomRacikanV2BelumLengkap(int row) {
        int[] kolomCek=new int[]{1,3,4,5};
        for(int kolom:kolomCek){
            if(tbRacikanV2.getValueAt(row,kolom).toString().trim().equals("")){
                return kolom;
            }
        }
        if(tbRacikanV2.getValueAt(row,2).toString().trim().equals("")){
            return 3;
        }
        return 1;
    }

    private void fokusKolomRacikanV2(int row,int kolom) {
        if(row<0 || row>=tbRacikanV2.getRowCount()){
            return;
        }
        tbRacikanV2.requestFocus();
        tbRacikanV2.setRowSelectionInterval(row,row);
        tbRacikanV2.setColumnSelectionInterval(kolom,kolom);
        tbRacikanV2.editCellAt(row,kolom);
        if(tbRacikanV2.getEditorComponent()!=null){
            tbRacikanV2.getEditorComponent().requestFocus();
            if(tbRacikanV2.getEditorComponent() instanceof javax.swing.JComboBox){
                ((javax.swing.JComboBox)tbRacikanV2.getEditorComponent()).showPopup();
            }
        }
    }

    private void selesaiEditRacikanV2() {
        try {
            if(tbRacikanV2!=null && tbRacikanV2.isEditing()){
                tbRacikanV2.getCellEditor().stopCellEditing();
            }
            if(tbDetailRacikanV2!=null && tbDetailRacikanV2.isEditing()){
                tbDetailRacikanV2.getCellEditor().stopCellEditing();
            }
        } catch (Exception e) {
        }
    }

    private void sinkronLabelDetailRacikanV2(int row) {
        if(row<0 || row>=tbRacikanV2.getRowCount() || tabModeDetailResepRacikanV2==null){
            return;
        }
        String noRacik=tbRacikanV2.getValueAt(row,0).toString();
        String labelBaru=noRacik+" - "+tbRacikanV2.getValueAt(row,1).toString();
        for(int baris=0;baris<tabModeDetailResepRacikanV2.getRowCount();baris++){
            if(tabModeDetailResepRacikanV2.getValueAt(baris,0).toString().startsWith(noRacik+" - ")){
                tabModeDetailResepRacikanV2.setValueAt(labelBaru,baris,0);
            }
        }
    }

    private void tampilkanDetailRacikanV2Aktif() {
        if(tbDetailRacikanV2==null || tbDetailRacikanV2.getRowSorter()==null){
            return;
        }
        final String noRacik=ambilNomorRacikanAktifV2();
        javax.swing.table.TableRowSorter sorter=(javax.swing.table.TableRowSorter)tbDetailRacikanV2.getRowSorter();
        if(noRacik.equals("")){
            sorter.setRowFilter(new javax.swing.RowFilter<DefaultTableModel,Integer>() {
                @Override
                public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    return false;
                }
            });
        }else{
            sorter.setRowFilter(new javax.swing.RowFilter<DefaultTableModel,Integer>() {
                @Override
                public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    return entry.getStringValue(0).startsWith(noRacik+" - ");
                }
            });
        }
    }

    private void tampilkanLabelRacikanAktifV2() {
        if(labelRacikanAktifV2==null){
            return;
        }
        if(tbRacikanV2.getSelectedRow()==-1){
            labelRacikanAktifV2.setText("Obat untuk racikan: pilih/isi racikan terlebih dahulu");
        }else{
            labelRacikanAktifV2.setText("Obat untuk Racikan "+tbRacikanV2.getValueAt(tbRacikanV2.getSelectedRow(),0)+" - "+tbRacikanV2.getValueAt(tbRacikanV2.getSelectedRow(),1));
        }
    }

    private String ambilNomorRacikanAktifV2() {
        if(tbRacikanV2.getSelectedRow()==-1){
            return "";
        }
        return tbRacikanV2.getValueAt(tbRacikanV2.getSelectedRow(),0).toString();
    }

    private String labelRacikanV2Aktif() {
        return ambilNomorRacikanAktifV2()+" - "+tbRacikanV2.getValueAt(tbRacikanV2.getSelectedRow(),1).toString();
    }

    private String ambilNoRacikDariLabelV2(String label) {
        if(label==null){
            return "";
        }
        int batas=label.indexOf(" - ");
        return batas>-1 ? label.substring(0,batas).trim() : label.trim();
    }

    private void tbRacikanV2KeyPressed(KeyEvent evt) {
        if(tbRacikanV2.getRowCount()!=0){
            try {
                i=tbRacikanV2.getSelectedColumn();
                if(evt.getKeyCode()==KeyEvent.VK_ENTER){
                    if(tbRacikanV2.isEditing()){
                        tbRacikanV2.getCellEditor().stopCellEditing();
                    }
                    if(i<5){
                        fokusKolomRacikanV2(tbRacikanV2.getSelectedRow(),kolomRacikanV2Berikutnya(i));
                    }else if(validasiRacikanAktifV2()){
                        tampilCacheResepRacikanV2();
                    }
                    evt.consume();
                }else if(evt.getKeyCode()==KeyEvent.VK_RIGHT){
                    if(i<5){
                        fokusKolomRacikanV2(tbRacikanV2.getSelectedRow(),kolomRacikanV2Berikutnya(i));
                    }
                    evt.consume();
                }
            } catch (Exception e) {
            }
        }
    }

    private int kolomRacikanV2Berikutnya(int kolomSaatIni) {
        if(kolomSaatIni<3){
            return 3;
        }else if(kolomSaatIni<4){
            return 4;
        }else if(kolomSaatIni<5){
            return 5;
        }
        return 5;
    }

    private void tampilCacheResepRacikanV2() {
        selesaiEditRacikanV2();
        String labelRacikan=labelRacikanV2Aktif();
        String noRacik=ambilNomorRacikanAktifV2();
        List<Object[]> pilihanLama=new ArrayList<>();
        for(int baris=tabModeDetailResepRacikanV2.getRowCount()-1;baris>=0;baris--){
            if(tabModeDetailResepRacikanV2.getValueAt(baris,0).toString().startsWith(noRacik+" - ")){
                if(Valid.SetAngka(tabModeDetailResepRacikanV2.getValueAt(baris,2).toString())>0){
                    pilihanLama.add(new Object[]{
                        tabModeDetailResepRacikanV2.getValueAt(baris,4).toString(),
                        tabModeDetailResepRacikanV2.getValueAt(baris,1),
                        tabModeDetailResepRacikanV2.getValueAt(baris,2)
                    });
                }
                tabModeDetailResepRacikanV2.removeRow(baris);
            }
        }
        try{
            File cache=new File("./cache/peresepandokter.iyem");
            if(!cache.exists()){
                buatcacheresep();
            }
            myObj = new FileReader("./cache/peresepandokter.iyem");
            root = mapper.readTree(myObj);
            response = root.path("peresepandokter");
            if(response.isArray()){
                for(JsonNode list:response){
                    if(TCari.getText().trim().equals("")||list.path("KodeBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||
                            list.path("NamaBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||
                            list.path("Jenis").asText().toLowerCase().contains(TCari.getText().toLowerCase())||
                            list.path("Kandungan").asText().toLowerCase().contains(TCari.getText().toLowerCase())){
                        Object[] nilaiPilihan=pilihanRacikanV2(pilihanLama,list.path("KodeBarang").asText());
                        tabModeDetailResepRacikanV2.addRow(new Object[] {
                            labelRacikan,nilaiPilihan[0],nilaiPilihan[1],"",list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),
                            list.path("Satuan").asText(),list.path("Kandungan").asText(),ambilHargaCacheResep(list),
                            list.path("Jenis").asText(),list.path("IndustriFarmasi").asText(),Double.parseDouble(list.path("HargaBeli").asText()),
                            Double.parseDouble(list.path("Stok").asText())
                        });
                    }
                }
            }
            myObj.close();
        }catch(Exception e){
            System.out.println("Notifikasi Racikan V2 : "+e);
        }
        tampilkanDetailRacikanV2Aktif();
        hitungResep();
    }

    private Object[] pilihanRacikanV2(List<Object[]> pilihanLama, String kodeBarang) {
        for(Object[] pilihanData:pilihanLama){
            if(pilihanData[0].toString().equals(kodeBarang)){
                return new Object[]{pilihanData[1],pilihanData[2]};
            }
        }
        return new Object[]{false,""};
    }

    private double ambilHargaCacheResep(JsonNode list) {
        try {
            if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                return Double.parseDouble(list.path("HargaKaryawan").asText());
            }else if(Jeniskelas.getSelectedItem().equals("Rawat Jalan")){
                return Double.parseDouble(list.path("HargaRalan").asText());
            }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                return Double.parseDouble(list.path("HargaBeliLuar").asText());
            }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                return Double.parseDouble(list.path("HargaUtama").asText());
            }else if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                return Double.parseDouble(list.path("HargaKelas1").asText());
            }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                return Double.parseDouble(list.path("HargaKelas2").asText());
            }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                return Double.parseDouble(list.path("HargaKelas3").asText());
            }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                return Double.parseDouble(list.path("HargaVIP").asText());
            }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                return Double.parseDouble(list.path("HargaVVIP").asText());
            }
        } catch (Exception e) {
        }
        return 0;
    }

    private void inisialisasiPanelTindakanPasien() {
        tabModeTindakan=new DefaultTableModel(null,new Object[]{
            "Tgl.Perawatan","Jam","Jenis","Kode","Nama Tindakan","Dokter/Petugas","Biaya"
        }){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        labelTindakanPasien = new widget.Label();
        labelTindakanPasien.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelTindakanPasien.setText("Tindakan yang sudah diinput :");
        labelTindakanPasien.setPreferredSize(new java.awt.Dimension(100,23));
        labelTindakanPasienRacikan = new widget.Label();
        labelTindakanPasienRacikan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelTindakanPasienRacikan.setText("Tindakan yang sudah diinput :");
        labelTindakanPasienRacikan.setPreferredSize(new java.awt.Dimension(100,23));

        tbTindakanPasien=new widget.Table();
        tbTindakanPasien.setName("tbTindakanPasien");
        tbTindakanPasien.setModel(tabModeTindakan);
        tbTindakanPasien.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbTindakanPasien.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbTindakanPasienRacikan=new widget.Table();
        tbTindakanPasienRacikan.setName("tbTindakanPasienRacikan");
        tbTindakanPasienRacikan.setModel(tabModeTindakan);
        tbTindakanPasienRacikan.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbTindakanPasienRacikan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        labelTindakanPasienRacikanV2 = new widget.Label();
        labelTindakanPasienRacikanV2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelTindakanPasienRacikanV2.setText("Tindakan yang sudah diinput :");
        labelTindakanPasienRacikanV2.setPreferredSize(new java.awt.Dimension(100,23));
        tbTindakanPasienRacikanV2=new widget.Table();
        tbTindakanPasienRacikanV2.setName("tbTindakanPasienRacikanV2");
        tbTindakanPasienRacikanV2.setModel(tabModeTindakan);
        tbTindakanPasienRacikanV2.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbTindakanPasienRacikanV2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        aturKolomTindakanPasien(tbTindakanPasien);
        aturKolomTindakanPasien(tbTindakanPasienRacikan);
        aturKolomTindakanPasien(tbTindakanPasienRacikanV2);

        scrollTindakanPasien=new widget.ScrollPane();
        scrollTindakanPasien.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255,255,255)));
        scrollTindakanPasien.setName("scrollTindakanPasien");
        scrollTindakanPasien.setOpaque(true);
        scrollTindakanPasien.setViewportView(tbTindakanPasien);
        scrollTindakanPasienRacikan=new widget.ScrollPane();
        scrollTindakanPasienRacikan.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255,255,255)));
        scrollTindakanPasienRacikan.setName("scrollTindakanPasienRacikan");
        scrollTindakanPasienRacikan.setOpaque(true);
        scrollTindakanPasienRacikan.setViewportView(tbTindakanPasienRacikan);
        scrollTindakanPasienRacikanV2=new widget.ScrollPane();
        scrollTindakanPasienRacikanV2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255,255,255)));
        scrollTindakanPasienRacikanV2.setName("scrollTindakanPasienRacikanV2");
        scrollTindakanPasienRacikanV2.setOpaque(true);
        scrollTindakanPasienRacikanV2.setViewportView(tbTindakanPasienRacikanV2);

        panelTindakanPasien=new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelTindakanPasien.setName("panelTindakanPasien");
        panelTindakanPasien.setOpaque(false);
        panelTindakanPasien.add(labelTindakanPasien, java.awt.BorderLayout.PAGE_START);
        panelTindakanPasien.add(scrollTindakanPasien, java.awt.BorderLayout.CENTER);
        panelTindakanPasienRacikan=new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelTindakanPasienRacikan.setName("panelTindakanPasienRacikan");
        panelTindakanPasienRacikan.setOpaque(false);
        panelTindakanPasienRacikan.add(labelTindakanPasienRacikan, java.awt.BorderLayout.PAGE_START);
        panelTindakanPasienRacikan.add(scrollTindakanPasienRacikan, java.awt.BorderLayout.CENTER);
        panelTindakanPasienRacikanV2=new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelTindakanPasienRacikanV2.setName("panelTindakanPasienRacikanV2");
        panelTindakanPasienRacikanV2.setOpaque(false);
        panelTindakanPasienRacikanV2.add(labelTindakanPasienRacikanV2, java.awt.BorderLayout.PAGE_START);
        panelTindakanPasienRacikanV2.add(scrollTindakanPasienRacikanV2, java.awt.BorderLayout.CENTER);
    }

    private void aturKolomTindakanPasien(JTable table){
        for (int kolom = 0; kolom < 7; kolom++) {
            TableColumn column = table.getColumnModel().getColumn(kolom);
            if(kolom==0){
                column.setPreferredWidth(85);
            }else if(kolom==1){
                column.setPreferredWidth(70);
            }else if(kolom==2){
                column.setPreferredWidth(120);
            }else if(kolom==3){
                column.setPreferredWidth(90);
            }else if(kolom==4){
                column.setPreferredWidth(260);
            }else if(kolom==5){
                column.setPreferredWidth(240);
            }else if(kolom==6){
                column.setPreferredWidth(90);
            }
        }
    }

    private void tampilkanTindakanPasien(String noRawat) {
        if(tabModeTindakan==null){
            return;
        }
        Valid.tabelKosong(tabModeTindakan);
        if(noRawat==null || noRawat.trim().equals("")){
            return;
        }

        String sql="";
        if(status.equals("ralan")){
            sql=
                "select rawat_jl_dr.tgl_perawatan,rawat_jl_dr.jam_rawat,'Dokter' as jenis,rawat_jl_dr.kd_jenis_prw,jns_perawatan.nm_perawatan,dokter.nm_dokter as pelaksana,rawat_jl_dr.biaya_rawat "+
                "from rawat_jl_dr inner join jns_perawatan on rawat_jl_dr.kd_jenis_prw=jns_perawatan.kd_jenis_prw inner join dokter on rawat_jl_dr.kd_dokter=dokter.kd_dokter where rawat_jl_dr.no_rawat=? "+
                "union all "+
                "select rawat_jl_pr.tgl_perawatan,rawat_jl_pr.jam_rawat,'Petugas' as jenis,rawat_jl_pr.kd_jenis_prw,jns_perawatan.nm_perawatan,petugas.nama as pelaksana,rawat_jl_pr.biaya_rawat "+
                "from rawat_jl_pr inner join jns_perawatan on rawat_jl_pr.kd_jenis_prw=jns_perawatan.kd_jenis_prw inner join petugas on rawat_jl_pr.nip=petugas.nip where rawat_jl_pr.no_rawat=? "+
                "union all "+
                "select rawat_jl_drpr.tgl_perawatan,rawat_jl_drpr.jam_rawat,'Dokter & Petugas' as jenis,rawat_jl_drpr.kd_jenis_prw,jns_perawatan.nm_perawatan,concat(dokter.nm_dokter,' / ',petugas.nama) as pelaksana,rawat_jl_drpr.biaya_rawat "+
                "from rawat_jl_drpr inner join jns_perawatan on rawat_jl_drpr.kd_jenis_prw=jns_perawatan.kd_jenis_prw inner join dokter on rawat_jl_drpr.kd_dokter=dokter.kd_dokter inner join petugas on rawat_jl_drpr.nip=petugas.nip where rawat_jl_drpr.no_rawat=? "+
                "order by tgl_perawatan,jam_rawat,jenis,nm_perawatan";
        }else if(status.equals("ranap")){
            sql=
                "select rawat_inap_dr.tgl_perawatan,rawat_inap_dr.jam_rawat,'Dokter' as jenis,rawat_inap_dr.kd_jenis_prw,jns_perawatan_inap.nm_perawatan,dokter.nm_dokter as pelaksana,rawat_inap_dr.biaya_rawat "+
                "from rawat_inap_dr inner join jns_perawatan_inap on rawat_inap_dr.kd_jenis_prw=jns_perawatan_inap.kd_jenis_prw inner join dokter on rawat_inap_dr.kd_dokter=dokter.kd_dokter where rawat_inap_dr.no_rawat=? "+
                "union all "+
                "select rawat_inap_pr.tgl_perawatan,rawat_inap_pr.jam_rawat,'Petugas' as jenis,rawat_inap_pr.kd_jenis_prw,jns_perawatan_inap.nm_perawatan,petugas.nama as pelaksana,rawat_inap_pr.biaya_rawat "+
                "from rawat_inap_pr inner join jns_perawatan_inap on rawat_inap_pr.kd_jenis_prw=jns_perawatan_inap.kd_jenis_prw inner join petugas on rawat_inap_pr.nip=petugas.nip where rawat_inap_pr.no_rawat=? "+
                "union all "+
                "select rawat_inap_drpr.tgl_perawatan,rawat_inap_drpr.jam_rawat,'Dokter & Petugas' as jenis,rawat_inap_drpr.kd_jenis_prw,jns_perawatan_inap.nm_perawatan,concat(dokter.nm_dokter,' / ',petugas.nama) as pelaksana,rawat_inap_drpr.biaya_rawat "+
                "from rawat_inap_drpr inner join jns_perawatan_inap on rawat_inap_drpr.kd_jenis_prw=jns_perawatan_inap.kd_jenis_prw inner join dokter on rawat_inap_drpr.kd_dokter=dokter.kd_dokter inner join petugas on rawat_inap_drpr.nip=petugas.nip where rawat_inap_drpr.no_rawat=? "+
                "order by tgl_perawatan,jam_rawat,jenis,nm_perawatan";
        }else{
            return;
        }

        try(PreparedStatement psTindakan=koneksi.prepareStatement(sql)){
            psTindakan.setString(1,noRawat);
            psTindakan.setString(2,noRawat);
            psTindakan.setString(3,noRawat);
            try(ResultSet rsTindakan=psTindakan.executeQuery()){
                while(rsTindakan.next()){
                    tabModeTindakan.addRow(new Object[]{
                        rsTindakan.getString("tgl_perawatan"),
                        rsTindakan.getString("jam_rawat"),
                        rsTindakan.getString("jenis"),
                        rsTindakan.getString("kd_jenis_prw"),
                        rsTindakan.getString("nm_perawatan"),
                        rsTindakan.getString("pelaksana"),
                        Valid.SetAngka(rsTindakan.getDouble("biaya_rawat"))
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Tindakan Pasien DlgPeresepanDokter : "+e);
        }
    }

    private void tampilkanPlanSOAPDokter(String noRawat) {
        planSOAPDokter="";
        try {
            if(noRawat!=null && !noRawat.trim().equals("")) {
                pastikanTabelDraftResepSOAP();
                bersihkanDraftSOAPNonDokter(noRawat);
                planSOAPDokter=Sequel.cariIsi(
                    "select ifnull(permintaan_resep_soap.resep_teks,'') from permintaan_resep_soap " +
                    "inner join dokter on permintaan_resep_soap.kd_dokter=dokter.kd_dokter " +
                    "where permintaan_resep_soap.no_rawat=? and permintaan_resep_soap.status='Belum Terlayani' " +
                    "order by permintaan_resep_soap.tgl_permintaan desc,permintaan_resep_soap.jam_permintaan desc limit 1",
                    noRawat
                );
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Plan SOAP DlgPeresepanDokter : "+e);
        }
        tampilkanDraftTerapiSOAP();
    }

    private void tampilkanDraftTerapiSOAP() {
        String tampilan = !planSOAPDokter.trim().equals("") ? planSOAPDokter : draftTerapiSOAP;
        String isi = tampilan.trim().equals("") ? "Belum ada Plan SOAP dokter untuk kunjungan ini." : tampilan;
        if(areaDraftSOAP!=null) {
            areaDraftSOAP.setText(isi);
            areaDraftSOAP.setCaretPosition(0);
        }
        if(areaDraftSOAPRacikan!=null) {
            areaDraftSOAPRacikan.setText(isi);
            areaDraftSOAPRacikan.setCaretPosition(0);
        }
        if(areaDraftSOAPRacikanV2!=null) {
            areaDraftSOAPRacikanV2.setText(isi);
            areaDraftSOAPRacikanV2.setCaretPosition(0);
        }
    }

    private void pastikanTabelDraftResepSOAP() {
        if(draftResepSOAPTableChecked){
            return;
        }
        try {
            Sequel.queryu("create table if not exists permintaan_resep_soap ("+
                    "id bigint not null auto_increment,"+
                    "no_rawat varchar(17) not null,"+
                    "tgl_soap date not null,"+
                    "jam_soap time not null,"+
                    "kd_dokter varchar(20) not null,"+
                    "tgl_permintaan date not null,"+
                    "jam_permintaan time not null,"+
                    "resep_teks text not null,"+
                    "status varchar(20) not null default 'Belum Terlayani',"+
                    "no_resep varchar(15) not null default '',"+
                    "primary key (id),"+
                    "key idx_prs_soap_1 (no_rawat,tgl_soap,jam_soap),"+
                    "key idx_prs_soap_2 (status,tgl_permintaan,jam_permintaan))");
            draftResepSOAPTableChecked=true;
        } catch (Exception e) {
            System.out.println("Notifikasi Plan SOAP DlgPeresepanDokter : "+e);
        }
    }

    private void bersihkanDraftSOAPNonDokter(String noRawat) {
        try {
            Sequel.queryu2(
                "delete from permintaan_resep_soap where no_rawat=? and status='Belum Terlayani' " +
                "and kd_dokter not in (select kd_dokter from dokter)",
                1,
                new String[]{noRawat}
            );
        } catch (Exception e) {
            System.out.println("Notifikasi Plan SOAP DlgPeresepanDokter : "+e);
        }
    }

    private void jam(){
        ActionListener taskPerformer = new ActionListener(){
            private int nilai_jam;
            private int nilai_menit;
            private int nilai_detik;
            @Override
            public void actionPerformed(ActionEvent e) {
                String nol_jam = "";
                String nol_menit = "";
                String nol_detik = "";
                // Membuat Date
                //Date dt = new Date();
                Date now = Calendar.getInstance().getTime();

                // Mengambil nilaj JAM, MENIT, dan DETIK Sekarang
                if(ChkJln.isSelected()==true){
                    nilai_jam = now.getHours();
                    nilai_menit = now.getMinutes();
                    nilai_detik = now.getSeconds();
                }else if(ChkJln.isSelected()==false){
                    nilai_jam =cmbJam.getSelectedIndex();
                    nilai_menit =cmbMnt.getSelectedIndex();
                    nilai_detik =cmbDtk.getSelectedIndex();
                }

                // Jika nilai JAM lebih kecil dari 10 (hanya 1 digit)
                if (nilai_jam <= 9) {
                    // Tambahkan "0" didepannya
                    nol_jam = "0";
                }
                // Jika nilai MENIT lebih kecil dari 10 (hanya 1 digit)
                if (nilai_menit <= 9) {
                    // Tambahkan "0" didepannya
                    nol_menit = "0";
                }
                // Jika nilai DETIK lebih kecil dari 10 (hanya 1 digit)
                if (nilai_detik <= 9) {
                    // Tambahkan "0" didepannya
                    nol_detik = "0";
                }
                // Membuat String JAM, MENIT, DETIK
                String jam = nol_jam + Integer.toString(nilai_jam);
                String menit = nol_menit + Integer.toString(nilai_menit);
                String detik = nol_detik + Integer.toString(nilai_detik);
                // Menampilkan pada Layar
                //tampil_jam.setText("  " + jam + " : " + menit + " : " + detik + "  ");
                cmbJam.setSelectedItem(jam);
                cmbMnt.setSelectedItem(menit);
                cmbDtk.setSelectedItem(detik);
            }
        };
        // Timer
        new Timer(1000, taskPerformer).start();
    }
    
    public void tampildetailracikanresep() {   
        try {
            double[] jumlah,harga,beli,stok,kapasitas,p1,p2;
            String[] no,kodebarang,namabarang,kodesatuan,kandungan,namajenis,industri,komposisi;
            z=0;
            for(i=0;i<tbDetailResepObatRacikan.getRowCount();i++){
                if(Valid.SetAngka(tbDetailResepObatRacikan.getValueAt(i,13).toString())>0){
                    z++;
                }
            }    

            jumlah=new double[z];
            harga=new double[z];
            stok=new double[z];
            p1=new double[z];
            p2=new double[z];
            kodebarang=new String[z];
            namabarang=new String[z];
            kodesatuan=new String[z];
            no=new String[z];
            namajenis=new String[z];        
            industri=new String[z];          
            komposisi=new String[z];        
            beli=new double[z];     
            kapasitas=new double[z];   
            kandungan=new String[z];
            z=0;        
            for(i=0;i<tbDetailResepObatRacikan.getRowCount();i++){
                if(Valid.SetAngka(tbDetailResepObatRacikan.getValueAt(i,13).toString())>0){
                    no[z]=tbDetailResepObatRacikan.getValueAt(i,0).toString();
                    kodebarang[z]=tbDetailResepObatRacikan.getValueAt(i,1).toString();
                    namabarang[z]=tbDetailResepObatRacikan.getValueAt(i,2).toString();
                    kodesatuan[z]=tbDetailResepObatRacikan.getValueAt(i,3).toString();
                    try {
                        harga[z]=Double.parseDouble(tbDetailResepObatRacikan.getValueAt(i,4).toString());
                    } catch (Exception e) {
                        harga[z]=0;
                    }
                    try {
                        beli[z]=Double.parseDouble(tbDetailResepObatRacikan.getValueAt(i,5).toString());
                    } catch (Exception e) {
                        beli[z]=0;
                    }
                    namajenis[z]=tbDetailResepObatRacikan.getValueAt(i,6).toString();
                    try {
                        stok[z]=Double.parseDouble(tbDetailResepObatRacikan.getValueAt(i,7).toString());
                    } catch (Exception e) {
                        stok[z]=0;
                    }                
                    try {
                        kapasitas[z]=Double.parseDouble(tbDetailResepObatRacikan.getValueAt(i,8).toString());
                    } catch (Exception e) {
                        kapasitas[z]=0;
                    }          
                    try {
                        p1[z]=Double.parseDouble(tbDetailResepObatRacikan.getValueAt(i,9).toString());
                    } catch (Exception e) {
                        p1[z]=0;
                    } 
                    try {
                        p2[z]=Double.parseDouble(tbDetailResepObatRacikan.getValueAt(i,11).toString());
                    } catch (Exception e) {
                        p2[z]=0;
                    } 
                    kandungan[z]=tbDetailResepObatRacikan.getValueAt(i,12).toString();
                    try {
                        jumlah[z]=Double.parseDouble(tbDetailResepObatRacikan.getValueAt(i,13).toString());
                    } catch (Exception e) {
                        jumlah[z]=0;
                    }                 
                    industri[z]=tbDetailResepObatRacikan.getValueAt(i,14).toString();
                    komposisi[z]=tbDetailResepObatRacikan.getValueAt(i,15).toString();
                    z++;
                }
            }

            Valid.tabelKosong(tabModeDetailResepRacikan);             

            for(i=0;i<z;i++){
                tabModeDetailResepRacikan.addRow(new Object[] {
                    no[i],kodebarang[i],namabarang[i],kodesatuan[i],harga[i],beli[i],
                    namajenis[i],stok[i],kapasitas[i],p1[i],"/",p2[i],kandungan[i],
                    jumlah[i],industri[i],komposisi[i]
                });
            }
            
            jumlah=null;
            harga=null;
            stok=null;
            p1=null;
            p2=null;
            kodebarang=null;
            namabarang=null;
            kodesatuan=null;
            no=null;
            namajenis=null;        
            industri=null;          
            komposisi=null;        
            beli=null;     
            kapasitas=null;   
            kandungan=null;
            
            myObj = new FileReader("./cache/peresepandokter.iyem");
            root = mapper.readTree(myObj);
            response = root.path("peresepandokter");
            if(response.isArray()){
                if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                    if(TCari.getText().trim().equals("")){
                        for(JsonNode list:response){
                            tabModeDetailResepRacikan.addRow(new Object[] {
                                tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),0).toString(),list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),
                                list.path("Satuan").asText(),Double.parseDouble(list.path("HargaKaryawan").asText()),Double.parseDouble(list.path("HargaBeli").asText()),
                                list.path("Jenis").asText(),Double.parseDouble(list.path("Stok").asText()),Double.parseDouble(list.path("Kapasitas").asText()),1,"/",1,"",0,
                                list.path("IndustriFarmasi").asText(),list.path("Kandungan").asText()
                            }); 
                        }
                    }else{
                        for(JsonNode list:response){
                            if(list.path("KodeBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("NamaBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Jenis").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Kandungan").asText().toLowerCase().contains(TCari.getText().toLowerCase())){
                                tabModeDetailResepRacikan.addRow(new Object[] {
                                    tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),0).toString(),list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),
                                    list.path("Satuan").asText(),Double.parseDouble(list.path("HargaKaryawan").asText()),Double.parseDouble(list.path("HargaBeli").asText()),
                                    list.path("Jenis").asText(),Double.parseDouble(list.path("Stok").asText()),Double.parseDouble(list.path("Kapasitas").asText()),1,"/",1,"",0,
                                    list.path("IndustriFarmasi").asText(),list.path("Kandungan").asText()
                                }); 
                            }
                        }
                    }
                }else if(Jeniskelas.getSelectedItem().equals("Rawat Jalan")){
                    if(TCari.getText().trim().equals("")){
                        for(JsonNode list:response){
                            tabModeDetailResepRacikan.addRow(new Object[] {
                                tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),0).toString(),list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),
                                list.path("Satuan").asText(),Double.parseDouble(list.path("HargaRalan").asText()),Double.parseDouble(list.path("HargaBeli").asText()),
                                list.path("Jenis").asText(),Double.parseDouble(list.path("Stok").asText()),Double.parseDouble(list.path("Kapasitas").asText()),1,"/",1,"",0,
                                list.path("IndustriFarmasi").asText(),list.path("Kandungan").asText()
                            }); 
                        }
                    }else{
                        for(JsonNode list:response){
                            if(list.path("KodeBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("NamaBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Jenis").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Kandungan").asText().toLowerCase().contains(TCari.getText().toLowerCase())){
                                tabModeDetailResepRacikan.addRow(new Object[] {
                                    tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),0).toString(),list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),
                                    list.path("Satuan").asText(),Double.parseDouble(list.path("HargaRalan").asText()),Double.parseDouble(list.path("HargaBeli").asText()),
                                    list.path("Jenis").asText(),Double.parseDouble(list.path("Stok").asText()),Double.parseDouble(list.path("Kapasitas").asText()),1,"/",1,"",0,
                                    list.path("IndustriFarmasi").asText(),list.path("Kandungan").asText()
                                }); 
                            }
                        }
                    }
                }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                    if(TCari.getText().trim().equals("")){
                        for(JsonNode list:response){
                            tabModeDetailResepRacikan.addRow(new Object[] {
                                tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),0).toString(),list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),
                                list.path("Satuan").asText(),Double.parseDouble(list.path("HargaBeliLuar").asText()),Double.parseDouble(list.path("HargaBeli").asText()),
                                list.path("Jenis").asText(),Double.parseDouble(list.path("Stok").asText()),Double.parseDouble(list.path("Kapasitas").asText()),1,"/",1,"",0,
                                list.path("IndustriFarmasi").asText(),list.path("Kandungan").asText()
                            }); 
                        }
                    }else{
                        for(JsonNode list:response){
                            if(list.path("KodeBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("NamaBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Jenis").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Kandungan").asText().toLowerCase().contains(TCari.getText().toLowerCase())){
                                tabModeDetailResepRacikan.addRow(new Object[] {
                                    tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),0).toString(),list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),
                                    list.path("Satuan").asText(),Double.parseDouble(list.path("HargaBeliLuar").asText()),Double.parseDouble(list.path("HargaBeli").asText()),
                                    list.path("Jenis").asText(),Double.parseDouble(list.path("Stok").asText()),Double.parseDouble(list.path("Kapasitas").asText()),1,"/",1,"",0,
                                    list.path("IndustriFarmasi").asText(),list.path("Kandungan").asText()
                                }); 
                            }
                        }
                    }
                }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                    if(TCari.getText().trim().equals("")){
                        for(JsonNode list:response){
                            tabModeDetailResepRacikan.addRow(new Object[] {
                                tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),0).toString(),list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),
                                list.path("Satuan").asText(),Double.parseDouble(list.path("HargaUtama").asText()),Double.parseDouble(list.path("HargaBeli").asText()),
                                list.path("Jenis").asText(),Double.parseDouble(list.path("Stok").asText()),Double.parseDouble(list.path("Kapasitas").asText()),1,"/",1,"",0,
                                list.path("IndustriFarmasi").asText(),list.path("Kandungan").asText()
                            }); 
                        }
                    }else{
                        for(JsonNode list:response){
                            if(list.path("KodeBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("NamaBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Jenis").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Kandungan").asText().toLowerCase().contains(TCari.getText().toLowerCase())){
                                tabModeDetailResepRacikan.addRow(new Object[] {
                                    tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),0).toString(),list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),
                                    list.path("Satuan").asText(),Double.parseDouble(list.path("HargaUtama").asText()),Double.parseDouble(list.path("HargaBeli").asText()),
                                    list.path("Jenis").asText(),Double.parseDouble(list.path("Stok").asText()),Double.parseDouble(list.path("Kapasitas").asText()),1,"/",1,"",0,
                                    list.path("IndustriFarmasi").asText(),list.path("Kandungan").asText()
                                }); 
                            }
                        }
                    }
                }else if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                    if(TCari.getText().trim().equals("")){
                        for(JsonNode list:response){
                            tabModeDetailResepRacikan.addRow(new Object[] {
                                tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),0).toString(),list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),
                                list.path("Satuan").asText(),Double.parseDouble(list.path("HargaKelas1").asText()),Double.parseDouble(list.path("HargaBeli").asText()),
                                list.path("Jenis").asText(),Double.parseDouble(list.path("Stok").asText()),Double.parseDouble(list.path("Kapasitas").asText()),1,"/",1,"",0,
                                list.path("IndustriFarmasi").asText(),list.path("Kandungan").asText()
                            }); 
                        }
                    }else{
                        for(JsonNode list:response){
                            if(list.path("KodeBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("NamaBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Jenis").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Kandungan").asText().toLowerCase().contains(TCari.getText().toLowerCase())){
                                tabModeDetailResepRacikan.addRow(new Object[] {
                                    tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),0).toString(),list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),
                                    list.path("Satuan").asText(),Double.parseDouble(list.path("HargaKelas1").asText()),Double.parseDouble(list.path("HargaBeli").asText()),
                                    list.path("Jenis").asText(),Double.parseDouble(list.path("Stok").asText()),Double.parseDouble(list.path("Kapasitas").asText()),1,"/",1,"",0,
                                    list.path("IndustriFarmasi").asText(),list.path("Kandungan").asText()
                                }); 
                            }
                        }
                    }
                }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                    if(TCari.getText().trim().equals("")){
                        for(JsonNode list:response){
                            tabModeDetailResepRacikan.addRow(new Object[] {
                                tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),0).toString(),list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),
                                list.path("Satuan").asText(),Double.parseDouble(list.path("HargaKelas2").asText()),Double.parseDouble(list.path("HargaBeli").asText()),
                                list.path("Jenis").asText(),Double.parseDouble(list.path("Stok").asText()),Double.parseDouble(list.path("Kapasitas").asText()),1,"/",1,"",0,
                                list.path("IndustriFarmasi").asText(),list.path("Kandungan").asText()
                            }); 
                        }
                    }else{
                        for(JsonNode list:response){
                            if(list.path("KodeBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("NamaBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Jenis").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Kandungan").asText().toLowerCase().contains(TCari.getText().toLowerCase())){
                                tabModeDetailResepRacikan.addRow(new Object[] {
                                    tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),0).toString(),list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),
                                    list.path("Satuan").asText(),Double.parseDouble(list.path("HargaKelas2").asText()),Double.parseDouble(list.path("HargaBeli").asText()),
                                    list.path("Jenis").asText(),Double.parseDouble(list.path("Stok").asText()),Double.parseDouble(list.path("Kapasitas").asText()),1,"/",1,"",0,
                                    list.path("IndustriFarmasi").asText(),list.path("Kandungan").asText()
                                }); 
                            }
                        }
                    }
                }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                    if(TCari.getText().trim().equals("")){
                        for(JsonNode list:response){
                            tabModeDetailResepRacikan.addRow(new Object[] {
                                tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),0).toString(),list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),
                                list.path("Satuan").asText(),Double.parseDouble(list.path("HargaKelas3").asText()),Double.parseDouble(list.path("HargaBeli").asText()),
                                list.path("Jenis").asText(),Double.parseDouble(list.path("Stok").asText()),Double.parseDouble(list.path("Kapasitas").asText()),1,"/",1,"",0,
                                list.path("IndustriFarmasi").asText(),list.path("Kandungan").asText()
                            }); 
                        }
                    }else{
                        for(JsonNode list:response){
                            if(list.path("KodeBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("NamaBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Jenis").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Kandungan").asText().toLowerCase().contains(TCari.getText().toLowerCase())){
                                tabModeDetailResepRacikan.addRow(new Object[] {
                                    tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),0).toString(),list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),
                                    list.path("Satuan").asText(),Double.parseDouble(list.path("HargaKelas3").asText()),Double.parseDouble(list.path("HargaBeli").asText()),
                                    list.path("Jenis").asText(),Double.parseDouble(list.path("Stok").asText()),Double.parseDouble(list.path("Kapasitas").asText()),1,"/",1,"",0,
                                    list.path("IndustriFarmasi").asText(),list.path("Kandungan").asText()
                                }); 
                            }
                        }
                    }
                }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                    if(TCari.getText().trim().equals("")){
                        for(JsonNode list:response){
                            tabModeDetailResepRacikan.addRow(new Object[] {
                                tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),0).toString(),list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),
                                list.path("Satuan").asText(),Double.parseDouble(list.path("HargaVIP").asText()),Double.parseDouble(list.path("HargaBeli").asText()),
                                list.path("Jenis").asText(),Double.parseDouble(list.path("Stok").asText()),Double.parseDouble(list.path("Kapasitas").asText()),1,"/",1,"",0,
                                list.path("IndustriFarmasi").asText(),list.path("Kandungan").asText()
                            }); 
                        }
                    }else{
                        for(JsonNode list:response){
                            if(list.path("KodeBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("NamaBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Jenis").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Kandungan").asText().toLowerCase().contains(TCari.getText().toLowerCase())){
                                tabModeDetailResepRacikan.addRow(new Object[] {
                                    tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),0).toString(),list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),
                                    list.path("Satuan").asText(),Double.parseDouble(list.path("HargaVIP").asText()),Double.parseDouble(list.path("HargaBeli").asText()),
                                    list.path("Jenis").asText(),Double.parseDouble(list.path("Stok").asText()),Double.parseDouble(list.path("Kapasitas").asText()),1,"/",1,"",0,
                                    list.path("IndustriFarmasi").asText(),list.path("Kandungan").asText()
                                }); 
                            }
                        }
                    }
                }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                    if(TCari.getText().trim().equals("")){
                        for(JsonNode list:response){
                            tabModeDetailResepRacikan.addRow(new Object[] {
                                tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),0).toString(),list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),
                                list.path("Satuan").asText(),Double.parseDouble(list.path("HargaVVIP").asText()),Double.parseDouble(list.path("HargaBeli").asText()),
                                list.path("Jenis").asText(),Double.parseDouble(list.path("Stok").asText()),Double.parseDouble(list.path("Kapasitas").asText()),1,"/",1,"",0,
                                list.path("IndustriFarmasi").asText(),list.path("Kandungan").asText()
                            }); 
                        }
                    }else{
                        for(JsonNode list:response){
                            if(list.path("KodeBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("NamaBarang").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Jenis").asText().toLowerCase().contains(TCari.getText().toLowerCase())||list.path("Kandungan").asText().toLowerCase().contains(TCari.getText().toLowerCase())){
                                tabModeDetailResepRacikan.addRow(new Object[] {
                                    tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),0).toString(),list.path("KodeBarang").asText(),list.path("NamaBarang").asText(),
                                    list.path("Satuan").asText(),Double.parseDouble(list.path("HargaVVIP").asText()),Double.parseDouble(list.path("HargaBeli").asText()),
                                    list.path("Jenis").asText(),Double.parseDouble(list.path("Stok").asText()),Double.parseDouble(list.path("Kapasitas").asText()),1,"/",1,"",0,
                                    list.path("IndustriFarmasi").asText(),list.path("Kandungan").asText()
                                }); 
                            }
                        }
                    }
                } 
            }
            myObj.close();
        } catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }           
    }

    private void getDatadetailresepracikan() {
        if(tbDetailResepObatRacikan.getSelectedRow()!= -1){
            try {
                tbDetailResepObatRacikan.setValueAt(Valid.SetAngka8((Double.parseDouble(tbObatResepRacikan.getValueAt(tbObatResepRacikan.getSelectedRow(),4).toString())
                                *Double.parseDouble(tbDetailResepObatRacikan.getValueAt(tbDetailResepObatRacikan.getSelectedRow(),12).toString()))
                                /Double.parseDouble(tbDetailResepObatRacikan.getValueAt(tbDetailResepObatRacikan.getSelectedRow(),8).toString()),1)
                                ,tbDetailResepObatRacikan.getSelectedRow(),13);
            } catch (Exception e) {
                tbDetailResepObatRacikan.setValueAt(0,tbDetailResepObatRacikan.getSelectedRow(),13);
            }
        }
    }
    
    private void getDatadetailresepracikan2() {
        if(tbDetailResepObatRacikan.getSelectedRow()!= -1){
            try {
                r=tbDetailResepObatRacikan.getSelectedRow();
                noracik=tbDetailResepObatRacikan.getValueAt(r,0).toString();
                jumlahracik=0;
                persenracik=Double.parseDouble(tbDetailResepObatRacikan.getValueAt(r,12).toString().replaceAll("%",""));
                kapasitasracik=Double.parseDouble(tbDetailResepObatRacikan.getValueAt(r,8).toString());
                for(i=0;i<tbDetailResepObatRacikan.getRowCount();i++){ 
                    if(noracik.equals(tbDetailResepObatRacikan.getValueAt(i,0).toString())){
                        if(!tbDetailResepObatRacikan.getValueAt(i,12).toString().contains("%")){
                            jumlahracik=jumlahracik+(Double.parseDouble(tbDetailResepObatRacikan.getValueAt(i,8).toString())*
                                    Double.parseDouble(tbDetailResepObatRacikan.getValueAt(i,13).toString()));
                        }
                    }
                }
                tbDetailResepObatRacikan.setValueAt(Valid.SetAngka8((jumlahracik*(persenracik/100))/kapasitasracik,1),r,13);
            } catch (Exception e) {
                tbDetailResepObatRacikan.setValueAt(0,r,13);
            }
        }
    }
    
    public void tampilobat(String no_resep) {
        NoResep.setText(no_resep);
        ubah=true;
        muatCatatanResepDokter(no_resep);
        try {
            Valid.tabelKosong(tabModeResep);
            Valid.tabelKosong(tabModeResepRacikan);
            Valid.tabelKosong(tabModeDetailResepRacikan);
            if(kenaikan>0){
                if(aktifkanbatch.equals("yes")){
                    qrystokkosong="";
                    if(STOKKOSONGRESEP.equals("no")){
                        qrystokkosong=" and gudangbarang.stok>0 ";
                    }
                    psresepasuransi=koneksi.prepareStatement("select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,(databarang.h_beli+(databarang.h_beli*?)) as harga,"+
                        " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,sum(gudangbarang.stok) as stok,resep_dokter.jml, resep_dokter.aturan_pakai "+
                        " from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                        " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                        " inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng "+
                        " inner join resep_dokter on resep_dokter.kode_brng=databarang.kode_brng "+
                        " where databarang.status='1' "+qrystokkosong+" and gudangbarang.no_batch<>'' and gudangbarang.no_faktur<>'' and gudangbarang.kd_bangsal=? and "+
                        " resep_dokter.no_resep=? group by gudangbarang.kode_brng order by databarang.nama_brng");
                }else{
                    qrystokkosong="";
                    if(STOKKOSONGRESEP.equals("no")){
                        qrystokkosong=" and gudangbarang.stok>0 ";
                    }
                    psresepasuransi=koneksi.prepareStatement("select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,(databarang.h_beli+(databarang.h_beli*?)) as harga,"+
                        " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,gudangbarang.stok,resep_dokter.jml, resep_dokter.aturan_pakai "+
                        " from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                        " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                        " inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng "+
                        " inner join resep_dokter on resep_dokter.kode_brng=databarang.kode_brng "+
                        " where databarang.status='1' "+qrystokkosong+" and gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.kd_bangsal=? and "+
                        " resep_dokter.no_resep=? order by databarang.nama_brng");
                }
                try{
                    psresepasuransi.setDouble(1,kenaikan);
                    psresepasuransi.setString(2,bangsal);
                    psresepasuransi.setString(3,no_resep);
                    rsobat=psresepasuransi.executeQuery();
                    if(STOKKOSONGRESEP.equals("no")){
                        while(rsobat.next()){
                            if(rsobat.getDouble("jml")>rsobat.getDouble("stok")){
                                JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                tabModeResep.addRow(new Object[] {
                                   false,"",rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("harga"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                }); 
                            }else{
                                tabModeResep.addRow(new Object[] {
                                    false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                    rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("harga"),100),
                                    rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                }); 
                            }         
                        }     
                    }else{
                        while(rsobat.next()){
                            tabModeResep.addRow(new Object[] {
                                false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("harga"),100),
                                rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                            }); 
                        } 
                    }    
                }catch(Exception e){
                    System.out.println("Notifikasi : "+e);
                }finally{
                    if(rsobat != null){
                        rsobat.close();
                    }

                    if(psresepasuransi != null){
                        psresepasuransi.close();
                    }
                }                                   
            }else{    
                if(aktifkanbatch.equals("yes")){
                    qrystokkosong="";
                    if(STOKKOSONGRESEP.equals("no")){
                        qrystokkosong=" and gudangbarang.stok>0 ";
                    }
                    psresep=koneksi.prepareStatement(
                        "select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,"+
                        " databarang.karyawan,databarang.ralan,databarang.beliluar,databarang.kelas1," +
                        " databarang.kelas2,databarang.kelas3,databarang.vip,databarang.vvip,"+
                        " databarang.letak_barang,databarang.utama,industrifarmasi.nama_industri,databarang.h_beli,sum(gudangbarang.stok) as stok,resep_dokter.jml, resep_dokter.aturan_pakai "+
                        " from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                        " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                        " inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng "+
                        " inner join resep_dokter on resep_dokter.kode_brng=databarang.kode_brng "+
                        " where  databarang.status='1' "+qrystokkosong+" and gudangbarang.no_batch<>'' and gudangbarang.no_faktur<>'' and gudangbarang.kd_bangsal=? and "+
                        " resep_dokter.no_resep=? group by gudangbarang.kode_brng order by databarang.nama_brng");
                }else{
                    qrystokkosong="";
                    if(STOKKOSONGRESEP.equals("no")){
                        qrystokkosong=" and gudangbarang.stok>0 ";
                    }
                    psresep=koneksi.prepareStatement(
                        "select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,"+
                        " databarang.karyawan,databarang.ralan,databarang.beliluar,databarang.kelas1," +
                        " databarang.kelas2,databarang.kelas3,databarang.vip,databarang.vvip,"+
                        " databarang.letak_barang,databarang.utama,industrifarmasi.nama_industri,databarang.h_beli,gudangbarang.stok,resep_dokter.jml, resep_dokter.aturan_pakai "+
                        " from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                        " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                        " inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng "+
                        " inner join resep_dokter on resep_dokter.kode_brng=databarang.kode_brng "+
                        " where  databarang.status='1' "+qrystokkosong+" and gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.kd_bangsal=? and "+
                        " resep_dokter.no_resep=? order by databarang.nama_brng");
                }
                try{
                    psresep.setString(1,bangsal);
                    psresep.setString(2,no_resep);
                    rsobat=psresep.executeQuery();
                    if(STOKKOSONGRESEP.equals("no")){
                        if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                            while(rsobat.next()){
                                if(rsobat.getDouble("jml")>rsobat.getDouble("stok")){
                                    JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                    tabModeResep.addRow(new Object[] {
                                       false,"",rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("karyawan"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else{
                                    tabModeResep.addRow(new Object[] {
                                       false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("karyawan"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }                   
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Rawat Jalan")){
                            while(rsobat.next()){
                                if(rsobat.getDouble("jml")>rsobat.getDouble("stok")){
                                    JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                    tabModeResep.addRow(new Object[] {
                                       false,"",rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("ralan"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else{
                                    tabModeResep.addRow(new Object[] {
                                       false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("ralan"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }                   
                            } 
                        }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                            while(rsobat.next()){
                                if(rsobat.getDouble("jml")>rsobat.getDouble("stok")){
                                    JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                    tabModeResep.addRow(new Object[] {
                                       false,"",rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("beliluar"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),
                                       rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else{
                                    tabModeResep.addRow(new Object[] {
                                       false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("beliluar"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }                   
                            } 
                        }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                            while(rsobat.next()){
                                if(rsobat.getDouble("jml")>rsobat.getDouble("stok")){
                                    JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                    tabModeResep.addRow(new Object[] {
                                       false,"",rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("utama"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else{
                                    tabModeResep.addRow(new Object[] {
                                       false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("utama"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }                   
                            }    
                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                            while(rsobat.next()){
                                if(rsobat.getDouble("jml")>rsobat.getDouble("stok")){
                                    JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                    tabModeResep.addRow(new Object[] {
                                       false,"",rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas1"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else{
                                    tabModeResep.addRow(new Object[] {
                                       false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas1"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }                   
                            } 
                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                            while(rsobat.next()){
                                if(rsobat.getDouble("jml")>rsobat.getDouble("stok")){
                                    JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                    tabModeResep.addRow(new Object[] {
                                       false,"",rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas2"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else{
                                    tabModeResep.addRow(new Object[] {
                                       false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas2"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }                   
                            } 
                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                            while(rsobat.next()){
                                if(rsobat.getDouble("jml")>rsobat.getDouble("stok")){
                                    JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                    tabModeResep.addRow(new Object[] {
                                       false,"",rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas3"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else{
                                    tabModeResep.addRow(new Object[] {
                                       false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas3"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }                   
                            } 
                        }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                            while(rsobat.next()){
                                if(rsobat.getDouble("jml")>rsobat.getDouble("stok")){
                                    JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                    tabModeResep.addRow(new Object[] {
                                       false,"",rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("vip"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else{
                                    tabModeResep.addRow(new Object[] {
                                       false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("vip"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }                   
                            } 
                        }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                            while(rsobat.next()){
                                if(rsobat.getDouble("jml")>rsobat.getDouble("stok")){
                                    JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                    tabModeResep.addRow(new Object[] {
                                       false,"",rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("vvip"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else{
                                    tabModeResep.addRow(new Object[] {
                                       false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("vvip"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }                   
                            } 
                        }    
                    }else{
                        if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                            while(rsobat.next()){
                                tabModeResep.addRow(new Object[] {
                                   false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("karyawan"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                });        
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Rawat Jalan")){
                            while(rsobat.next()){
                                tabModeResep.addRow(new Object[] {
                                   false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("ralan"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                });          
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                            while(rsobat.next()){
                                tabModeResep.addRow(new Object[] {
                                   false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("beliluar"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                });            
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                            while(rsobat.next()){
                                tabModeResep.addRow(new Object[] {
                                   false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("utama"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                });              
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                            while(rsobat.next()){
                                tabModeResep.addRow(new Object[] {
                                   false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas1"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                });            
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                            while(rsobat.next()){
                                tabModeResep.addRow(new Object[] {
                                   false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas2"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                });              
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                            while(rsobat.next()){
                                tabModeResep.addRow(new Object[] {
                                   false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas3"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                });            
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                            while(rsobat.next()){
                                tabModeResep.addRow(new Object[] {
                                   false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("vip"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                });              
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                            while(rsobat.next()){
                                tabModeResep.addRow(new Object[] {
                                   false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("vvip"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                });       
                            }
                        }  
                    }
                }catch(Exception e){
                    System.out.println("Notifikasi : "+e);
                }finally{
                    if(rsobat != null){
                        rsobat.close();
                    }

                    if(psresep != null){
                        psresep.close();
                    }
                }
            } 
            psresep=koneksi.prepareStatement(
                    "select resep_dokter_racikan.no_racik,resep_dokter_racikan.nama_racik,"+
                    "resep_dokter_racikan.kd_racik,metode_racik.nm_racik as metode,"+
                    "resep_dokter_racikan.jml_dr,resep_dokter_racikan.aturan_pakai,"+
                    "resep_dokter_racikan.keterangan from resep_dokter_racikan inner join metode_racik "+
                    "on resep_dokter_racikan.kd_racik=metode_racik.kd_racik where "+
                    "resep_dokter_racikan.no_resep=? ");
            try {
                psresep.setString(1,no_resep);
                rsobat=psresep.executeQuery();
                while(rsobat.next()){
                    tabModeResepRacikan.addRow(new Object[]{
                        rsobat.getString("no_racik"),rsobat.getString("nama_racik"),rsobat.getString("kd_racik"),
                        rsobat.getString("metode"),rsobat.getString("jml_dr"),rsobat.getString("aturan_pakai"),
                        rsobat.getString("keterangan")
                    });   
                    if(kenaikan>0){
                        if(aktifkanbatch.equals("yes")){
                            qrystokkosong="";
                            if(STOKKOSONGRESEP.equals("no")){
                                qrystokkosong=" and gudangbarang.stok>0 ";
                            }
                            ps2=koneksi.prepareStatement(
                                "select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,(databarang.h_beli+(databarang.h_beli*?)) as harga,"+
                                "databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,sum(gudangbarang.stok) as stok,databarang.kapasitas,resep_dokter_racikan_detail.p1,"+
                                "resep_dokter_racikan_detail.p2,resep_dokter_racikan_detail.kandungan,resep_dokter_racikan_detail.jml "+
                                "from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                                "inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                                "inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng "+
                                "inner join resep_dokter_racikan_detail on resep_dokter_racikan_detail.kode_brng=databarang.kode_brng "+
                                "where databarang.status='1' "+qrystokkosong+" and gudangbarang.no_batch<>'' and gudangbarang.no_faktur<>'' and gudangbarang.kd_bangsal=? and "+
                                "resep_dokter_racikan_detail.no_resep=? and resep_dokter_racikan_detail.no_racik=? group by gudangbarang.kode_brng order by databarang.nama_brng");
                        }else{
                            qrystokkosong="";
                            if(STOKKOSONGRESEP.equals("no")){
                                qrystokkosong=" and gudangbarang.stok>0 ";
                            }
                            ps2=koneksi.prepareStatement(
                                "select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,(databarang.h_beli+(databarang.h_beli*?)) as harga,"+
                                "databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,gudangbarang.stok,databarang.kapasitas,resep_dokter_racikan_detail.p1,"+
                                "resep_dokter_racikan_detail.p2,resep_dokter_racikan_detail.kandungan,resep_dokter_racikan_detail.jml "+
                                "from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                                "inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                                "inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng "+
                                "inner join resep_dokter_racikan_detail on resep_dokter_racikan_detail.kode_brng=databarang.kode_brng "+
                                "where databarang.status='1' "+qrystokkosong+" and gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.kd_bangsal=? and "+
                                "resep_dokter_racikan_detail.no_resep=? and resep_dokter_racikan_detail.no_racik=? order by databarang.nama_brng");
                        }
                        try{ 
                            ps2.setDouble(1,kenaikan);
                            ps2.setString(2,bangsal);
                            ps2.setString(3,no_resep);
                            ps2.setString(4,rsobat.getString("no_racik"));
                            rs2=ps2.executeQuery();
                            if(STOKKOSONGRESEP.equals("no")){
                                while(rs2.next()){
                                    if(rs2.getDouble("jml")>rs2.getDouble("stok")){
                                        JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("harga"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),0,rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        }); 
                                    }else{
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("harga"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        });  
                                    }        
                                } 
                            }else{
                                while(rs2.next()){
                                    tabModeDetailResepRacikan.addRow(new Object[] {
                                        rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                        rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("harga"),100),
                                        rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                        rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                        rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                    });  
                                } 
                            }
                                 
                        }catch(Exception e){
                            System.out.println("Notifikasi : "+e);
                        }finally{
                            if(rs2 != null){
                                rs2.close();
                            }
                            if(ps2 != null){
                                ps2.close();
                            }
                        }               
                    }else{
                        if(aktifkanbatch.equals("yes")){
                            qrystokkosong="";
                            if(STOKKOSONGRESEP.equals("no")){
                                qrystokkosong=" and gudangbarang.stok>0 ";
                            }
                            ps2=koneksi.prepareStatement(
                                "select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,"+
                                "databarang.karyawan,databarang.ralan,databarang.beliluar,databarang.kelas1," +
                                "databarang.kelas2,databarang.kelas3,databarang.vip,databarang.vvip,"+
                                "databarang.letak_barang,databarang.utama,industrifarmasi.nama_industri,databarang.h_beli,sum(gudangbarang.stok) as stok,databarang.kapasitas,resep_dokter_racikan_detail.p1,"+
                                "resep_dokter_racikan_detail.p2,resep_dokter_racikan_detail.kandungan,resep_dokter_racikan_detail.jml "+
                                "from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                                "inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                                "inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng "+
                                "inner join resep_dokter_racikan_detail on resep_dokter_racikan_detail.kode_brng=databarang.kode_brng "+
                                "where databarang.status='1' "+qrystokkosong+" and gudangbarang.no_batch<>'' and gudangbarang.no_faktur<>'' and gudangbarang.kd_bangsal=? and "+
                                "resep_dokter_racikan_detail.no_resep=? and resep_dokter_racikan_detail.no_racik=? group by gudangbarang.kode_brng order by databarang.nama_brng");
                        }else{
                            qrystokkosong="";
                            if(STOKKOSONGRESEP.equals("no")){
                                qrystokkosong=" and gudangbarang.stok>0 ";
                            }
                            ps2=koneksi.prepareStatement(
                                "select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,"+
                                "databarang.karyawan,databarang.ralan,databarang.beliluar,databarang.kelas1," +
                                "databarang.kelas2,databarang.kelas3,databarang.vip,databarang.vvip,"+
                                "databarang.letak_barang,databarang.utama,industrifarmasi.nama_industri,databarang.h_beli,gudangbarang.stok,databarang.kapasitas,resep_dokter_racikan_detail.p1,"+
                                "resep_dokter_racikan_detail.p2,resep_dokter_racikan_detail.kandungan,resep_dokter_racikan_detail.jml "+
                                "from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                                "inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                                "inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng "+
                                "inner join resep_dokter_racikan_detail on resep_dokter_racikan_detail.kode_brng=databarang.kode_brng "+
                                "where databarang.status='1' "+qrystokkosong+" and gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.kd_bangsal=? and "+
                                "resep_dokter_racikan_detail.no_resep=? and resep_dokter_racikan_detail.no_racik=? order by databarang.nama_brng");
                        }
                        try{ 
                            ps2.setString(1,bangsal);
                            ps2.setString(2,no_resep);
                            ps2.setString(3,rsobat.getString("no_racik"));
                            rs2=ps2.executeQuery();
                            if(STOKKOSONGRESEP.equals("no")){
                                if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                                    while(rs2.next()){
                                        if(rs2.getDouble("jml")>rs2.getDouble("stok")){
                                            JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("karyawan"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),0,rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            }); 
                                        }else{
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("karyawan"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            });  
                                        }                
                                    }
                                }else if(Jeniskelas.getSelectedItem().equals("Rawat Jalan")){
                                    while(rs2.next()){
                                        if(rs2.getDouble("jml")>rs2.getDouble("stok")){
                                            JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("ralan"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),0,rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            }); 
                                        }else{
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("ralan"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            }); 
                                        }                
                                    }
                                }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                                     while(rs2.next()){
                                        if(rs2.getDouble("jml")>rs2.getDouble("stok")){
                                            JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("beliluar"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),0,rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            });  
                                        }else{
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("beliluar"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            });   
                                        }                
                                    }
                                }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                                    while(rs2.next()){
                                        if(rs2.getDouble("jml")>rs2.getDouble("stok")){
                                            JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("utama"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),0,rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            }); 
                                        }else{
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("utama"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            });  
                                        }                
                                    } 
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                                    while(rs2.next()){
                                        if(rs2.getDouble("jml")>rs2.getDouble("stok")){
                                            JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("kelas1"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),0,rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            }); 
                                        }else{
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("kelas1"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            }); 
                                        }                
                                    }
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                                    while(rs2.next()){
                                        if(rs2.getDouble("jml")>rs2.getDouble("stok")){
                                            JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("kelas2"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),0,rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            });  
                                        }else{
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("kelas2"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            }); 
                                        }                
                                    } 
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                                    while(rs2.next()){
                                        if(rs2.getDouble("jml")>rs2.getDouble("stok")){
                                            JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("kelas3"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),0,rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            }); 
                                        }else{
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("kelas3"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            });  
                                        }                
                                    }
                                }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                                    while(rs2.next()){
                                        if(rs2.getDouble("jml")>rs2.getDouble("stok")){
                                            JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("vip"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),0,rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            }); 
                                        }else{
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("vip"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            }); 
                                        }                
                                    }
                                }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                                    while(rs2.next()){
                                        if(rs2.getDouble("jml")>rs2.getDouble("stok")){
                                            JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("vvip"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),0,rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            });
                                        }else{
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("vvip"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            }); 
                                        }                
                                    }
                                } 
                            }else{
                                if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                                    while(rs2.next()){
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("karyawan"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        });
                                    } 
                                }else if(Jeniskelas.getSelectedItem().equals("Rawat Jalan")){
                                    while(rs2.next()){
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("ralan"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        }); 
                                    }
                                }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                                    while(rs2.next()){
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("beliluar"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        }); 
                                    } 
                                }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                                    while(rs2.next()){
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("utama"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        });
                                    } 
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                                    while(rs2.next()){
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("kelas1"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        }); 
                                    }
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                                    while(rs2.next()){
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("kelas2"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        }); 
                                    }
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                                    while(rs2.next()){
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("kelas3"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        }); 
                                    }
                                }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                                    while(rs2.next()){
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("vip"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        }); 
                                    }
                                }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                                    while(rs2.next()){
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("vvip"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        }); 
                                    }
                                } 
                            }
                        }catch(Exception e){
                            System.out.println("Notifikasi : "+e);
                        }finally{
                            if(rs2 != null){
                                rs2.close();
                            }
                            if(ps2 != null){
                                ps2.close();
                            }
                        }
                    }  
                }
            } catch (Exception e) {
                System.out.println("Notifikasi 2 : "+e);
            } finally{
                if(rsobat!=null){
                    rsobat.close();
                }
                if(psresep!=null){
                    psresep.close();
                }
            }
            hitungResep();
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        } 
    }
    
    public void tampilobat2(String no_resep) {
        kosongkanCatatanDokter();
        try {
            Valid.tabelKosong(tabModeResep);
            Valid.tabelKosong(tabModeResepRacikan);
            Valid.tabelKosong(tabModeDetailResepRacikan);
            copy=true;
            if(kenaikan>0){
                if(aktifkanbatch.equals("yes")){
                    qrystokkosong="";
                    if(STOKKOSONGRESEP.equals("no")){
                        qrystokkosong=" and gudangbarang.stok>0 ";
                    }
                    psresepasuransi=koneksi.prepareStatement("select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,(databarang.h_beli+(databarang.h_beli*?)) as harga,"+
                        " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,sum(gudangbarang.stok) as stok,resep_dokter.jml, resep_dokter.aturan_pakai "+
                        " from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                        " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                        " inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng "+
                        " inner join resep_dokter on resep_dokter.kode_brng=databarang.kode_brng "+
                        " where databarang.status='1' "+qrystokkosong+" and gudangbarang.no_batch<>'' and gudangbarang.no_faktur<>'' and gudangbarang.kd_bangsal=? and "+
                        " resep_dokter.no_resep=? group by gudangbarang.kode_brng order by databarang.nama_brng");
                }else{
                    qrystokkosong="";
                    if(STOKKOSONGRESEP.equals("no")){
                        qrystokkosong=" and gudangbarang.stok>0 ";
                    }
                    psresepasuransi=koneksi.prepareStatement("select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,(databarang.h_beli+(databarang.h_beli*?)) as harga,"+
                        " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,gudangbarang.stok,resep_dokter.jml, resep_dokter.aturan_pakai "+
                        " from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                        " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                        " inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng "+
                        " inner join resep_dokter on resep_dokter.kode_brng=databarang.kode_brng "+
                        " where databarang.status='1' "+qrystokkosong+" and gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.kd_bangsal=? and "+
                        " resep_dokter.no_resep=? order by databarang.nama_brng");
                }
                
                try{
                    psresepasuransi.setDouble(1,kenaikan);
                    psresepasuransi.setString(2,bangsal);
                    psresepasuransi.setString(3,no_resep);
                    rsobat=psresepasuransi.executeQuery();
                    if(STOKKOSONGRESEP.equals("no")){
                        while(rsobat.next()){
                            if(rsobat.getDouble("jml")>rsobat.getDouble("stok")){
                                JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                tabModeResep.addRow(new Object[] {
                                   false,"",rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("harga"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                });  
                            }else{
                                tabModeResep.addRow(new Object[] {
                                   false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("harga"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                });  
                            }        
                        }
                    }else{
                        while(rsobat.next()){
                            tabModeResep.addRow(new Object[] {
                               false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                               rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("harga"),100),
                               rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                            });  
                        }
                    }     
                }catch(Exception e){
                    System.out.println("Notifikasi : "+e);
                }finally{
                    if(rsobat != null){
                        rsobat.close();
                    }

                    if(psresepasuransi != null){
                        psresepasuransi.close();
                    }
                }                                   
            }else{    
                if(aktifkanbatch.equals("yes")){
                    qrystokkosong="";
                    if(STOKKOSONGRESEP.equals("no")){
                        qrystokkosong=" and gudangbarang.stok>0 ";
                    }
                    psresep=koneksi.prepareStatement(
                        "select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,"+
                        " databarang.karyawan,databarang.ralan,databarang.beliluar,databarang.kelas1," +
                        " databarang.kelas2,databarang.kelas3,databarang.vip,databarang.vvip,"+
                        " databarang.letak_barang,databarang.utama,industrifarmasi.nama_industri,databarang.h_beli,sum(gudangbarang.stok) as stok,resep_dokter.jml, resep_dokter.aturan_pakai "+
                        " from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                        " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                        " inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng "+
                        " inner join resep_dokter on resep_dokter.kode_brng=databarang.kode_brng "+
                        " where databarang.status='1' "+qrystokkosong+" and gudangbarang.no_batch<>'' and gudangbarang.no_faktur<>'' and gudangbarang.kd_bangsal=? and "+
                        " resep_dokter.no_resep=? group by gudangbarang.kode_brng order by databarang.nama_brng");
                }else{
                    qrystokkosong="";
                    if(STOKKOSONGRESEP.equals("no")){
                        qrystokkosong=" and gudangbarang.stok>0 ";
                    }
                    psresep=koneksi.prepareStatement(
                        "select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,"+
                        " databarang.karyawan,databarang.ralan,databarang.beliluar,databarang.kelas1," +
                        " databarang.kelas2,databarang.kelas3,databarang.vip,databarang.vvip,"+
                        " databarang.letak_barang,databarang.utama,industrifarmasi.nama_industri,databarang.h_beli,gudangbarang.stok,resep_dokter.jml, resep_dokter.aturan_pakai "+
                        " from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                        " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                        " inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng "+
                        " inner join resep_dokter on resep_dokter.kode_brng=databarang.kode_brng "+
                        " where databarang.status='1' "+qrystokkosong+" and gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.kd_bangsal=? and "+
                        " resep_dokter.no_resep=? order by databarang.nama_brng");
                }
                
                try{
                    psresep.setString(1,bangsal);
                    psresep.setString(2,no_resep);
                    rsobat=psresep.executeQuery();
                    if(STOKKOSONGRESEP.equals("no")){
                        while(rsobat.next()){
                            if(rsobat.getDouble("jml")>rsobat.getDouble("stok")){
                                JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                                    tabModeResep.addRow(new Object[] {
                                       false,"",rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("karyawan"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Rawat Jalan")){
                                    tabModeResep.addRow(new Object[] {
                                       false,"",rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("ralan"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                                    tabModeResep.addRow(new Object[] {
                                       false,"",rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("beliluar"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                                    tabModeResep.addRow(new Object[] {
                                       false,"",rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("utama"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                                    tabModeResep.addRow(new Object[] {
                                       false,"",rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas1"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                                    tabModeResep.addRow(new Object[] {
                                       false,"",rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas2"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                                    tabModeResep.addRow(new Object[] {
                                       false,"",rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas3"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                                    tabModeResep.addRow(new Object[] {
                                       false,"",rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("vip"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                                    tabModeResep.addRow(new Object[] {
                                       false,"",rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("vvip"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                } 
                            }else{
                                if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                                    tabModeResep.addRow(new Object[] {
                                       false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("karyawan"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Rawat Jalan")){
                                    tabModeResep.addRow(new Object[] {
                                       false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("ralan"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                                    tabModeResep.addRow(new Object[] {
                                       false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("beliluar"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                                    tabModeResep.addRow(new Object[] {
                                       false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("utama"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                                    tabModeResep.addRow(new Object[] {
                                       false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas1"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                                    tabModeResep.addRow(new Object[] {
                                       false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas2"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                                    tabModeResep.addRow(new Object[] {
                                       false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas3"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                                    tabModeResep.addRow(new Object[] {
                                       false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("vip"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                                    tabModeResep.addRow(new Object[] {
                                       false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                       rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("vvip"),100),
                                       rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                    });
                                } 
                            }                     
                        }
                    }else{
                        while(rsobat.next()){
                            if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                                tabModeResep.addRow(new Object[] {
                                   false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("karyawan"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                });
                            }else if(Jeniskelas.getSelectedItem().equals("Rawat Jalan")){
                                tabModeResep.addRow(new Object[] {
                                   false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("ralan"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                });
                            }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                                tabModeResep.addRow(new Object[] {
                                   false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("beliluar"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                });
                            }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                                tabModeResep.addRow(new Object[] {
                                   false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("utama"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                });
                            }else if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                                tabModeResep.addRow(new Object[] {
                                   false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas1"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                });
                            }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                                tabModeResep.addRow(new Object[] {
                                   false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas2"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                });
                            }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                                tabModeResep.addRow(new Object[] {
                                   false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas3"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                });
                            }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                                tabModeResep.addRow(new Object[] {
                                   false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("vip"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                });
                            }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                                tabModeResep.addRow(new Object[] {
                                   false,rsobat.getDouble("jml"),rsobat.getString("aturan_pakai"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("vvip"),100),
                                   rsobat.getString("nama"),rsobat.getString("nama_industri"),rsobat.getDouble("h_beli"),rsobat.getDouble("stok")
                                });
                            }                 
                        }
                    }
                }catch(Exception e){
                    System.out.println("Notifikasi : "+e);
                }finally{
                    if(rsobat != null){
                        rsobat.close();
                    }

                    if(psresep != null){
                        psresep.close();
                    }
                }
            } 
            psresep=koneksi.prepareStatement(
                    "select resep_dokter_racikan.no_racik,resep_dokter_racikan.nama_racik,"+
                    "resep_dokter_racikan.kd_racik,metode_racik.nm_racik as metode,"+
                    "resep_dokter_racikan.jml_dr,resep_dokter_racikan.aturan_pakai,"+
                    "resep_dokter_racikan.keterangan from resep_dokter_racikan inner join metode_racik "+
                    "on resep_dokter_racikan.kd_racik=metode_racik.kd_racik where "+
                    "resep_dokter_racikan.no_resep=? ");
            try {
                psresep.setString(1,no_resep);
                rsobat=psresep.executeQuery();
                while(rsobat.next()){
                    tabModeResepRacikan.addRow(new Object[]{
                        rsobat.getString("no_racik"),rsobat.getString("nama_racik"),rsobat.getString("kd_racik"),
                        rsobat.getString("metode"),rsobat.getString("jml_dr"),rsobat.getString("aturan_pakai"),
                        rsobat.getString("keterangan")
                    });   
                    if(kenaikan>0){
                        if(aktifkanbatch.equals("yes")){
                            qrystokkosong="";
                            if(STOKKOSONGRESEP.equals("no")){
                                qrystokkosong=" and gudangbarang.stok>0 ";
                            }
                            ps2=koneksi.prepareStatement(
                                "select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,(databarang.h_beli+(databarang.h_beli*?)) as harga,"+
                                "databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,sum(gudangbarang.stok) as stok,databarang.kapasitas,resep_dokter_racikan_detail.p1,"+
                                "resep_dokter_racikan_detail.p2,resep_dokter_racikan_detail.kandungan,resep_dokter_racikan_detail.jml "+
                                "from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                                "inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                                "inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng "+
                                "inner join resep_dokter_racikan_detail on resep_dokter_racikan_detail.kode_brng=databarang.kode_brng "+
                                "where databarang.status='1' "+qrystokkosong+" and gudangbarang.no_batch<>'' and gudangbarang.no_faktur<>'' and gudangbarang.kd_bangsal=? and "+
                                "resep_dokter_racikan_detail.no_resep=? and resep_dokter_racikan_detail.no_racik=? group by gudangbarang.kode_brng order by databarang.nama_brng");
                        }else{
                            qrystokkosong="";
                            if(STOKKOSONGRESEP.equals("no")){
                                qrystokkosong=" and gudangbarang.stok>0 ";
                            }
                            ps2=koneksi.prepareStatement(
                                "select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,(databarang.h_beli+(databarang.h_beli*?)) as harga,"+
                                "databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,gudangbarang.stok,databarang.kapasitas,resep_dokter_racikan_detail.p1,"+
                                "resep_dokter_racikan_detail.p2,resep_dokter_racikan_detail.kandungan,resep_dokter_racikan_detail.jml "+
                                "from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                                "inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                                "inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng "+
                                "inner join resep_dokter_racikan_detail on resep_dokter_racikan_detail.kode_brng=databarang.kode_brng "+
                                "where databarang.status='1' "+qrystokkosong+" and gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.kd_bangsal=? and "+
                                "resep_dokter_racikan_detail.no_resep=? and resep_dokter_racikan_detail.no_racik=? order by databarang.nama_brng");
                        }
                        
                        try{ 
                            ps2.setDouble(1,kenaikan);
                            ps2.setString(2,bangsal);
                            ps2.setString(3,no_resep);
                            ps2.setString(4,rsobat.getString("no_racik"));
                            rs2=ps2.executeQuery();
                            if(STOKKOSONGRESEP.equals("no")){
                                while(rs2.next()){
                                    if(rs2.getDouble("jml")>rs2.getDouble("stok")){
                                        JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("harga"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),0,rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        }); 
                                    }else{
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("harga"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        }); 
                                    }         
                                }  
                            }else{
                                while(rs2.next()){
                                    tabModeDetailResepRacikan.addRow(new Object[] {
                                        rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                        rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("harga"),100),
                                        rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                        rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                        rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                    });   
                                }  
                            }
                        }catch(Exception e){
                            System.out.println("Notifikasi : "+e);
                        }finally{
                            if(rs2 != null){
                                rs2.close();
                            }
                            if(ps2 != null){
                                ps2.close();
                            }
                        }               
                    }else{
                        if(aktifkanbatch.equals("yes")){
                            qrystokkosong="";
                            if(STOKKOSONGRESEP.equals("no")){
                                qrystokkosong=" and gudangbarang.stok>0 ";
                            }
                            ps2=koneksi.prepareStatement(
                                "select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,"+
                                "databarang.karyawan,databarang.ralan,databarang.beliluar,databarang.kelas1," +
                                "databarang.kelas2,databarang.kelas3,databarang.vip,databarang.vvip,"+
                                "databarang.letak_barang,databarang.utama,industrifarmasi.nama_industri,databarang.h_beli,sum(gudangbarang.stok) as stok,databarang.kapasitas,resep_dokter_racikan_detail.p1,"+
                                "resep_dokter_racikan_detail.p2,resep_dokter_racikan_detail.kandungan,resep_dokter_racikan_detail.jml "+
                                "from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                                "inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                                "inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng "+
                                "inner join resep_dokter_racikan_detail on resep_dokter_racikan_detail.kode_brng=databarang.kode_brng "+
                                "where databarang.status='1' "+qrystokkosong+" and gudangbarang.no_batch<>'' and gudangbarang.no_faktur<>'' and gudangbarang.kd_bangsal=? and "+
                                "resep_dokter_racikan_detail.no_resep=? and resep_dokter_racikan_detail.no_racik=? group by gudangbarang.kode_brng order by databarang.nama_brng");
                        }else{
                            qrystokkosong="";
                            if(STOKKOSONGRESEP.equals("no")){
                                qrystokkosong=" and gudangbarang.stok>0 ";
                            }
                            ps2=koneksi.prepareStatement(
                                "select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,"+
                                "databarang.karyawan,databarang.ralan,databarang.beliluar,databarang.kelas1," +
                                "databarang.kelas2,databarang.kelas3,databarang.vip,databarang.vvip,"+
                                "databarang.letak_barang,databarang.utama,industrifarmasi.nama_industri,databarang.h_beli,gudangbarang.stok,databarang.kapasitas,resep_dokter_racikan_detail.p1,"+
                                "resep_dokter_racikan_detail.p2,resep_dokter_racikan_detail.kandungan,resep_dokter_racikan_detail.jml "+
                                "from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                                "inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                                "inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng "+
                                "inner join resep_dokter_racikan_detail on resep_dokter_racikan_detail.kode_brng=databarang.kode_brng "+
                                "where databarang.status='1' "+qrystokkosong+" and gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.kd_bangsal=? and "+
                                "resep_dokter_racikan_detail.no_resep=? and resep_dokter_racikan_detail.no_racik=? order by databarang.nama_brng");
                        }
                        try{ 
                            ps2.setString(1,bangsal);
                            ps2.setString(2,no_resep);
                            ps2.setString(3,rsobat.getString("no_racik"));
                            rs2=ps2.executeQuery();
                            if(STOKKOSONGRESEP.equals("no")){
                                while(rs2.next()){
                                    if(rs2.getDouble("jml")>rs2.getDouble("stok")){
                                        JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                        if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("karyawan"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),0,rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            }); 
                                        }else if(Jeniskelas.getSelectedItem().equals("Rawat Jalan")){
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("ralan"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),0,rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            }); 
                                        }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("beliluar"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),0,rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            });  
                                        }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("utama"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),0,rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            }); 
                                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("kelas1"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),0,rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("kelas2"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),0,rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("kelas3"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),0,rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("vip"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),0,rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("vvip"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),0,rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            });
                                        } 
                                    }else{
                                        if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("karyawan"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            }); 
                                        }else if(Jeniskelas.getSelectedItem().equals("Rawat Jalan")){
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("ralan"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            }); 
                                        }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("beliluar"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            });  
                                        }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("utama"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            }); 
                                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("kelas1"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("kelas2"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("kelas3"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("vip"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                                            tabModeDetailResepRacikan.addRow(new Object[] {
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("vvip"),100),
                                                rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                                rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                                rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                            });
                                        } 
                                    }                  
                                }
                            }else{
                                while(rs2.next()){
                                    if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("karyawan"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        }); 
                                    }else if(Jeniskelas.getSelectedItem().equals("Rawat Jalan")){
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("ralan"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        }); 
                                    }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("beliluar"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        });  
                                    }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("utama"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        }); 
                                    }else if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("kelas1"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        });
                                    }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("kelas2"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        });
                                    }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("kelas3"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        });
                                    }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("vip"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        });
                                    }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                                        tabModeDetailResepRacikan.addRow(new Object[] {
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),Valid.roundUp(rs2.getDouble("vvip"),100),
                                            rs2.getDouble("h_beli"),rs2.getString("nama"),rs2.getDouble("stok"),
                                            rs2.getDouble("kapasitas"),rs2.getDouble("p1"),"/",rs2.getDouble("p2"),
                                            rs2.getString("kandungan"),rs2.getDouble("jml"),rs2.getString("nama_industri"),rs2.getString("letak_barang")
                                        });
                                    } 
                                }    
                            }
                        }catch(Exception e){
                            System.out.println("Notifikasi : "+e);
                        }finally{
                            if(rs2 != null){
                                rs2.close();
                            }
                            if(ps2 != null){
                                ps2.close();
                            }
                        }
                    }  
                }
            } catch (Exception e) {
                System.out.println("Notifikasi 2 : "+e);
            } finally{
                if(rsobat!=null){
                    rsobat.close();
                }
                if(psresep!=null){
                    psresep.close();
                }
            }
            hitungResep();
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        } 
    }

    private void simpandata() {
        try {
            for(i=0;i<tbResep.getRowCount();i++){ 
                if(Valid.SetAngka(tbResep.getValueAt(i,1).toString())>0){                        
                    if(tbResep.getValueAt(i,0).toString().equals("true")){
                        pscarikapasitas= koneksi.prepareStatement("select IFNULL(databarang.kapasitas,1) from databarang where databarang.kode_brng=?");                                      
                        try {
                            pscarikapasitas.setString(1,tbResep.getValueAt(i,2).toString());
                            carikapasitas=pscarikapasitas.executeQuery();
                            if(carikapasitas.next()){ 
                                if(Sequel.menyimpantf2("resep_dokter","?,?,?,?","data",4,new String[]{
                                    NoResep.getText(),tbResep.getValueAt(i,3).toString(),
                                    ""+(Double.parseDouble(tbResep.getValueAt(i,1).toString())/carikapasitas.getDouble(1)),
                                    tbResep.getValueAt(i,2).toString()
                                })==false){
                                    sukses=false;
                                }
                            }else{
                                if(Sequel.menyimpantf2("resep_dokter","?,?,?,?","data",4,new String[]{
                                    NoResep.getText(),tbResep.getValueAt(i,3).toString(),
                                    ""+(Double.parseDouble(tbResep.getValueAt(i,1).toString())),
                                    tbResep.getValueAt(i,2).toString()
                                })==false){
                                    sukses=false;
                                }                               
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi Kapasitas : "+e);
                        } finally{
                            if(carikapasitas!=null){
                                carikapasitas.close();
                            }
                            if(pscarikapasitas!=null){
                                pscarikapasitas.close();
                            }
                        }
                    }else{
                        if(Sequel.menyimpantf2("resep_dokter","?,?,?,?","data",4,new String[]{
                            NoResep.getText(),tbResep.getValueAt(i,3).toString(),
                            ""+(Double.parseDouble(tbResep.getValueAt(i,1).toString())),
                            tbResep.getValueAt(i,2).toString()
                        })==false){
                            sukses=false;
                        }                                   
                    }                      
                }
            } 

            for(i=0;i<tbObatResepRacikan.getRowCount();i++){ 
                if(Valid.SetAngka(tbObatResepRacikan.getValueAt(i,4).toString())>0){ 
                    if(Sequel.menyimpantf2("resep_dokter_racikan","?,?,?,?,?,?,?","resep obat racikan",7,new String[]{
                       NoResep.getText(),tbObatResepRacikan.getValueAt(i,0).toString(),tbObatResepRacikan.getValueAt(i,1).toString(),
                       tbObatResepRacikan.getValueAt(i,2).toString(),tbObatResepRacikan.getValueAt(i,4).toString(),
                       tbObatResepRacikan.getValueAt(i,5).toString(),tbObatResepRacikan.getValueAt(i,6).toString()
                    })==false){
                        sukses=false;
                    } 
                }
            }

            for(i=0;i<tbRacikanV2.getRowCount();i++){
                if(Valid.SetAngka(tbRacikanV2.getValueAt(i,4).toString())>0){
                    if(Sequel.menyimpantf2("resep_dokter_racikan","?,?,?,?,?,?,?","resep obat racikan v2",7,new String[]{
                       NoResep.getText(),tbRacikanV2.getValueAt(i,0).toString(),tbRacikanV2.getValueAt(i,1).toString(),
                       tbRacikanV2.getValueAt(i,2).toString(),tbRacikanV2.getValueAt(i,4).toString(),
                       tbRacikanV2.getValueAt(i,5).toString(),tbRacikanV2.getValueAt(i,6).toString()
                    })==false){
                        sukses=false;
                    }
                }
            }

            for(i=0;i<tbDetailResepObatRacikan.getRowCount();i++){ 
                if(Valid.SetAngka(tbDetailResepObatRacikan.getValueAt(i,13).toString())>0){
                    if(Sequel.menyimpantf2("resep_dokter_racikan_detail","?,?,?,?,?,?,?","resep dokter racikan detail",7,new String[]{
                        NoResep.getText(),tbDetailResepObatRacikan.getValueAt(i,0).toString(),tbDetailResepObatRacikan.getValueAt(i,1).toString(),
                        tbDetailResepObatRacikan.getValueAt(i,9).toString(),tbDetailResepObatRacikan.getValueAt(i,11).toString(),
                        tbDetailResepObatRacikan.getValueAt(i,12).toString(),tbDetailResepObatRacikan.getValueAt(i,13).toString()
                    })==false){
                        sukses=false;
                    } 
                }
            }

            for(i=0;i<tabModeDetailResepRacikanV2.getRowCount();i++){
                if(Valid.SetAngka(tabModeDetailResepRacikanV2.getValueAt(i,2).toString())>0){
                    if(Sequel.menyimpantf2("resep_dokter_racikan_detail","?,?,?,?,?,?,?","resep dokter racikan v2 detail",7,new String[]{
                        NoResep.getText(),ambilNoRacikDariLabelV2(tabModeDetailResepRacikanV2.getValueAt(i,0).toString()),tabModeDetailResepRacikanV2.getValueAt(i,4).toString(),
                        "1","1","",tabModeDetailResepRacikanV2.getValueAt(i,2).toString()
                    })==false){
                        sukses=false;
                    }
                }
            }
            simpanCatatanResepDokter();
        } catch (Exception e) {
            System.out.println("Notif : "+e);
        } 
    }

    private void pastikanTabelCatatanResepDokter() {
        try {
            Sequel.queryu2(
                "create table if not exists catatan_resep_dokter ("+
                "no_resep varchar(14) not null,"+
                "catatan text,"+
                "tanggal_update datetime not null,"+
                "primary key (no_resep)) engine=InnoDB default charset=latin1"
            );
        } catch (Exception e) {
            System.out.println("Notifikasi Catatan Resep Dokter : "+e);
        }
    }

    private void simpanCatatanResepDokter() {
        try {
            Sequel.queryu2("delete from catatan_resep_dokter where no_resep=?",1,new String[]{NoResep.getText()});
            if(catatanResepDokter.trim().equals("")){
                return;
            }
            if(Sequel.menyimpantf2("catatan_resep_dokter","?,?,now()","Catatan Resep Dokter",2,new String[]{
                NoResep.getText(),catatanResepDokter.trim()
            })==false){
                sukses=false;
            }
        } catch (Exception e) {
            sukses=false;
            System.out.println("Notifikasi Catatan Resep Dokter : "+e);
        }
    }
    
    public void MatikanJam(){
        ChkJln.setSelected(false);
    }

    private void SetHarga() {
        if(status.equals("ranap")){
            norawatibu=Sequel.cariIsi("select ranap_gabung.no_rawat from ranap_gabung where ranap_gabung.no_rawat2=?",TNoRw.getText());
            if(!norawatibu.equals("")){
                kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat=? order by kamar_inap.tgl_masuk desc limit 1",norawatibu);
            }else{
                kamar=Sequel.cariIsi("select ifnull(kamar_inap.kd_kamar,'') from kamar_inap where kamar_inap.no_rawat=? order by kamar_inap.tgl_masuk desc limit 1",TNoRw.getText());
            }
            if(!norawatibu.equals("")){
                kelas=Sequel.cariIsi(
                    "select kamar.kelas from kamar inner join kamar_inap on kamar.kd_kamar=kamar_inap.kd_kamar where kamar_inap.no_rawat=? and kamar_inap.stts_pulang='-' order by STR_TO_DATE(concat(kamar_inap.tgl_masuk,' ',kamar_inap.jam_masuk),'%Y-%m-%d %H:%i:%s') desc limit 1",norawatibu);
            }else{
                kelas=Sequel.cariIsi(
                    "select kamar.kelas from kamar inner join kamar_inap on kamar.kd_kamar=kamar_inap.kd_kamar where kamar_inap.no_rawat=? and kamar_inap.stts_pulang='-' order by STR_TO_DATE(concat(kamar_inap.tgl_masuk,' ',kamar_inap.jam_masuk),'%Y-%m-%d %H:%i:%s') desc limit 1",TNoRw.getText());
            }                
            if(kelas.equals("Kelas 1")){
                Jeniskelas.setSelectedItem("Kelas 1");
            }else if(kelas.equals("Kelas 2")){
                Jeniskelas.setSelectedItem("Kelas 2");
            }else if(kelas.equals("Kelas 3")){
                Jeniskelas.setSelectedItem("Kelas 3");
            }else if(kelas.equals("Kelas Utama")){
                Jeniskelas.setSelectedItem("Utama/BPJS");
            }else if(kelas.equals("Kelas VIP")){
                Jeniskelas.setSelectedItem("VIP");
            }else if(kelas.equals("Kelas VVIP")){
                Jeniskelas.setSelectedItem("VVIP");
            } 
            kenaikan=Sequel.cariIsiAngka2("select (set_harga_obat_ranap.hargajual/100) from set_harga_obat_ranap where set_harga_obat_ranap.kd_pj=? and set_harga_obat_ranap.kelas=?",KdPj.getText(),kelas);
        }else if(status.equals("ralan")){
            kelas="Rawat Jalan";
            kenaikan=Sequel.cariIsiAngka("select (set_harga_obat_ralan.hargajual/100) from set_harga_obat_ralan where set_harga_obat_ralan.kd_pj=?",KdPj.getText());
        }
    }
    
    private void hitungResep() {
        ttl=0;
        y=0;
        row2=tabModeResep.getRowCount();
        for(r=0;r<row2;r++){ 
            try {
                if(Double.parseDouble(tabModeResep.getValueAt(r,1).toString())>0){
                    try {                
                        y=Math.round(Double.parseDouble(tabModeResep.getValueAt(r,1).toString())*
                          Double.parseDouble(tabModeResep.getValueAt(r,7).toString()));                                                
                    } catch (Exception e) {
                        y=0;
                    }
                    ttl=ttl+y;
                }  
            } catch (Exception e) {
            }                           
        }
        row2=tabModeDetailResepRacikan.getRowCount();
        for(r=0;r<row2;r++){ 
            if(Valid.SetAngka(tbDetailResepObatRacikan.getValueAt(r,13).toString())>0){
                try {
                    y=Math.round(Double.parseDouble(tabModeDetailResepRacikan.getValueAt(r,13).toString())*
                      Double.parseDouble(tabModeDetailResepRacikan.getValueAt(r,4).toString()));
                } catch (Exception e) {
                    y=0;
                }
                ttl=ttl+y;
            }
        }
        if(tabModeDetailResepRacikanV2!=null){
            row2=tabModeDetailResepRacikanV2.getRowCount();
            for(r=0;r<row2;r++){
                if(Valid.SetAngka(tabModeDetailResepRacikanV2.getValueAt(r,2).toString())>0){
                    try {
                        y=Math.round(Double.parseDouble(tabModeDetailResepRacikanV2.getValueAt(r,2).toString())*
                          Double.parseDouble(tabModeDetailResepRacikanV2.getValueAt(r,8).toString()));
                    } catch (Exception e) {
                        y=0;
                    }
                    ttl=ttl+y;
                }
            }
        }
        LTotal.setText(Valid.SetAngka(ttl));
        ppnobat=0;
        if(tampilkan_ppnobat_ralan.equals("Yes")){
            ppnobat=Math.round(ttl*0.11);
            ttl=ttl+ppnobat;
            LPpn.setText(Valid.SetAngka(ppnobat));
            LTotalTagihan.setText(Valid.SetAngka(ttl));
        }
    }

    private void terapkanDraftTerapiSOAPJikaPerlu() {
        if(draftTerapiSOAPSudahDiterapkan || draftTerapiSOAP.trim().equals("") || ubah || copy){
            return;
        }

        List<String> barisTerapi=ambilBarisDraftTerapi(draftTerapiSOAP);
        if(barisTerapi.isEmpty()){
            draftTerapiSOAPSudahDiterapkan=true;
            return;
        }

        List<String> tidakDikenali=new ArrayList<>();
        int cocok=0;
        int belumAdaJumlah=0;

        for(String baris:barisTerapi){
            List<ItemDraftTerapi> itemObat=pecahDraftPerObat(baris);
            if(itemObat.isEmpty()){
                tidakDikenali.add(baris);
            }else{
                for(ItemDraftTerapi item:itemObat){
                    String jumlah=ekstrakJumlahDraft(item.segmen,item.namaObat);
                    String aturan=ekstrakAturanDraft(item.segmen,item.namaObat);

                    if(tbResep.getValueAt(item.row,1).toString().trim().equals("") && !jumlah.equals("")){
                        tbResep.setValueAt(jumlah,item.row,1);
                    }

                    if(tbResep.getValueAt(item.row,2).toString().trim().equals("") && !aturan.equals("")){
                        tbResep.setValueAt(aturan,item.row,2);
                    }

                    if(tbResep.getValueAt(item.row,1).toString().trim().equals("")){
                        belumAdaJumlah++;
                    }
                    cocok++;
                }
            }
        }

        draftTerapiSOAPSudahDiterapkan=true;
        hitungResep();

        if(cocok>0 || !tidakDikenali.isEmpty()){
            StringBuilder pesan=new StringBuilder();
            pesan.append("Draft terapi dari Plan SOAP sudah dicoba dimasukkan ke daftar resep.");
            pesan.append("\nCocok : ").append(cocok).append(" item");
            if(belumAdaJumlah>0){
                pesan.append("\nJumlah belum terbaca : ").append(belumAdaJumlah).append(" item");
            }
            if(!tidakDikenali.isEmpty()){
                pesan.append("\nBelum dikenali :");
                for(i=0;i<tidakDikenali.size() && i<5;i++){
                    pesan.append("\n- ").append(tidakDikenali.get(i));
                }
                if(tidakDikenali.size()>5){
                    pesan.append("\n- dan ").append(tidakDikenali.size()-5).append(" baris lainnya");
                }
            }
            JOptionPane.showMessageDialog(this,pesan.toString(),"Draft Resep Dari Plan SOAP",JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private List<String> ambilBarisDraftTerapi(String teks) {
        List<String> hasil=new ArrayList<>();
        String[] baris=teks.replace("\r","\n").split("\n");
        for(String item:baris){
            String[] pecahan=item.split("[;]");
            for(String pecah:pecahan){
                String bersih=bersihkanBarisDraft(pecah);
                if(!bersih.equals("")){
                    hasil.add(bersih);
                }
            }
        }
        return hasil;
    }

    private String bersihkanBarisDraft(String baris) {
        if(baris==null){
            return "";
        }
        String hasil=POLA_AWAL_LIST.matcher(baris.trim()).replaceFirst("").trim();
        hasil=hasil.replaceAll("\\s{2,}"," ");
        return hasil;
    }

    private List<ItemDraftTerapi> pecahDraftPerObat(String barisDraft) {
        List<ItemDraftTerapi> hasil=new ArrayList<>();
        String target=normalisasiDraft(barisDraft);
        if(target.equals("")){
            return hasil;
        }

        List<KandidatDraftObat> kandidat=new ArrayList<>();
        for(int x=0;x<tbResep.getRowCount();x++){
            String namaObat=normalisasiDraft(tbResep.getValueAt(x,4).toString());
            if(namaObat.equals("") || namaObat.length()<4){
                continue;
            }
            int posisi=cariPosisiKecocokanDraft(target,namaObat);
            if(posisi>-1){
                int akhir=Math.min(target.length(),posisi+namaObat.length());
                kandidat.add(new KandidatDraftObat(x,namaObat,posisi,akhir));
            }
        }

        if(kandidat.isEmpty()){
            return hasil;
        }

        for(int a=0;a<kandidat.size()-1;a++){
            for(int b=a+1;b<kandidat.size();b++){
                KandidatDraftObat kiri=kandidat.get(a);
                KandidatDraftObat kanan=kandidat.get(b);
                boolean tukar=(kiri.posisi>kanan.posisi) ||
                        (kiri.posisi==kanan.posisi && kiri.namaObat.length()<kanan.namaObat.length());
                if(tukar){
                    kandidat.set(a,kanan);
                    kandidat.set(b,kiri);
                }
            }
        }

        List<KandidatDraftObat> terpilih=new ArrayList<>();
        int batasAkhir=-1;
        for(KandidatDraftObat item:kandidat){
            if(item.posisi>=batasAkhir){
                terpilih.add(item);
                batasAkhir=item.akhir;
            }
        }

        for(int x=0;x<terpilih.size();x++){
            KandidatDraftObat item=terpilih.get(x);
            int akhirSegmen=(x<terpilih.size()-1) ? terpilih.get(x+1).posisi : target.length();
            String segmen=target.substring(item.posisi,akhirSegmen).trim();
            hasil.add(new ItemDraftTerapi(item.row,item.namaObat,segmen));
        }
        return hasil;
    }

    private int cariPosisiKecocokanDraft(String target,String namaObat) {
        int posisi=target.indexOf(namaObat);
        if(posisi>-1){
            return posisi;
        }

        String[] tokenNama=namaObat.split(" ");
        for(String token:tokenNama){
            if(token.length()>=4){
                posisi=cariPosisiTokenDraft(target,token);
                if(posisi>-1){
                    return posisi;
                }
                if(token.length()>=5){
                    posisi=cariPosisiTokenDraft(target,token.substring(0,5));
                    if(posisi>-1){
                        return posisi;
                    }
                }
            }
        }
        return -1;
    }

    private int cariPosisiTokenDraft(String target,String token) {
        Matcher matcher=Pattern.compile("(^|\\s)"+Pattern.quote(token)+"(?=\\s|$)",Pattern.CASE_INSENSITIVE).matcher(target);
        if(matcher.find()){
            String hasil=matcher.group();
            return matcher.start() + (hasil.startsWith(" ") ? 1 : 0);
        }
        return -1;
    }

    private String normalisasiDraft(String teks) {
        return teks==null ? "" : teks.toLowerCase().replaceAll("[^a-z0-9/x]+"," ").trim().replaceAll("\\s{2,}"," ");
    }

    private String hapusNamaObatDariDraft(String barisDraft,String namaObat) {
        String sisa=normalisasiDraft(barisDraft);
        String nama=normalisasiDraft(namaObat);
        if(!nama.equals("")){
            String hasilExact=Pattern.compile("(?i)\\b"+Pattern.quote(nama)+"\\b").matcher(sisa).replaceFirst("").trim();
            if(!hasilExact.equals(sisa)){
                sisa=hasilExact;
            }else{
                String[] tokenNama=nama.split(" ");
                for(int x=0;x<tokenNama.length;x++){
                    for(int y=x+1;y<=tokenNama.length;y++){
                        String kandidat=String.join(" ",java.util.Arrays.copyOfRange(tokenNama,x,y)).trim();
                        if(kandidat.length()>=4){
                            String hasilToken=Pattern.compile("(?i)\\b"+Pattern.quote(kandidat)+"\\b").matcher(sisa).replaceFirst("").trim();
                            if(!hasilToken.equals(sisa)){
                                sisa=hasilToken;
                                x=tokenNama.length;
                                break;
                            }
                        }
                    }
                }
            }
        }
        sisa=sisa.replaceAll("\\s{2,}"," ");
        return sisa;
    }

    private String ekstrakJumlahDraft(String barisDraft,String namaObat) {
        String sisa=hapusNamaObatDariDraft(barisDraft,namaObat);

        Matcher matcher=POLA_JUMLAH_RESEP.matcher(sisa);
        if(matcher.find()){
            return matcher.group(1).replace(",",".");
        }

        Matcher jumlahAwal=Pattern.compile("^\\s*(\\d+(?:[\\.,]\\d+)?)\\b(?=\\s+(?:\\d+\\s*x\\s*\\d+|\\d+x\\d+|pagi|siang|malam|prn|bila|sesudah|sebelum|sesudahmakan|sebelummakan))",Pattern.CASE_INSENSITIVE).matcher(sisa);
        if(jumlahAwal.find()){
            return jumlahAwal.group(1).replace(",",".");
        }

        Matcher jumlahSederhana=Pattern.compile("^\\s*(\\d+(?:[\\.,]\\d+)?)(?:\\s*(?:tab|tablet|kapsul|kaps|kap|butir|pcs|botol|sachet|ampul|amp|supp|vial))?\\s*$",Pattern.CASE_INSENSITIVE).matcher(sisa);
        if(jumlahSederhana.find()){
            return jumlahSederhana.group(1).replace(",",".");
        }
        return "";
    }

    private String ekstrakAturanDraft(String barisDraft,String namaObat) {
        String aturan=hapusNamaObatDariDraft(barisDraft,namaObat);
        aturan=POLA_JUMLAH_RESEP.matcher(aturan).replaceAll("").trim();
        aturan=aturan.replaceFirst("^\\s*\\d+(?:[\\.,]\\d+)?\\b(?=\\s+(?:\\d+\\s*x\\s*\\d+|\\d+x\\d+|pagi|siang|malam|prn|bila|sesudah|sebelum|sesudahmakan|sebelummakan))","").trim();
        aturan=aturan.replaceFirst("^\\s*\\d+(?:[\\.,]\\d+)?(?:\\s*(?:tab|tablet|kapsul|kaps|kap|butir|pcs|botol|sachet|ampul|amp|supp|vial))?\\s*$","").trim();
        aturan=aturan.replaceAll("^[,:;\\-]+","").trim();
        aturan=aturan.replaceAll("\\s{2,}"," ");
        return aturan;
    }

    private static class KandidatDraftObat {
        private final int row;
        private final String namaObat;
        private final int posisi;
        private final int akhir;

        private KandidatDraftObat(int row,String namaObat,int posisi,int akhir) {
            this.row=row;
            this.namaObat=namaObat;
            this.posisi=posisi;
            this.akhir=akhir;
        }
    }

    private static class ItemDraftTerapi {
        private final int row;
        private final String namaObat;
        private final String segmen;

        private ItemDraftTerapi(int row,String namaObat,String segmen) {
            this.row=row;
            this.namaObat=namaObat;
            this.segmen=segmen;
        }
    }
    
    private void getCekStok() {
        if(tbResep.getSelectedRow()!= -1){
            if(STOKKOSONGRESEP.equals("no")){
                try {
                    if(Double.parseDouble(tbResep.getValueAt(tbResep.getSelectedRow(),1).toString())>0){
                        if(Valid.SetAngka(tbResep.getValueAt(tbResep.getSelectedRow(),1).toString())>Valid.SetAngka(tbResep.getValueAt(tbResep.getSelectedRow(),11).toString())){
                            JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                            tbResep.setValueAt("",tbResep.getSelectedRow(),1);
                        }
                    }
                } catch (Exception e) {
                    tbResep.setValueAt("",tbResep.getSelectedRow(),1);
                } 
            }  
        }               
    }
    
    private void getCekStokRacikan() {
        if(tbDetailResepObatRacikan.getSelectedRow()!= -1){
            if(STOKKOSONGRESEP.equals("no")){
                try {
                    if(Double.parseDouble(tbDetailResepObatRacikan.getValueAt(tbDetailResepObatRacikan.getSelectedRow(),13).toString())>0){
                        if(Valid.SetAngka(tbDetailResepObatRacikan.getValueAt(tbDetailResepObatRacikan.getSelectedRow(),13).toString())>Valid.SetAngka(tbDetailResepObatRacikan.getValueAt(tbDetailResepObatRacikan.getSelectedRow(),7).toString())){
                            JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                            tbDetailResepObatRacikan.setValueAt(0,tbDetailResepObatRacikan.getSelectedRow(),13);
                        }
                    }
                } catch (Exception e) {
                    tbDetailResepObatRacikan.setValueAt(0,tbDetailResepObatRacikan.getSelectedRow(),13);
                }
            }
        }               
    }
    private void tampilDetailResep(String noResep) {
    StringBuilder sb = new StringBuilder();
    PreparedStatement psHdr = null, psNon = null, psRacik = null, psRacikDet = null;
    ResultSet rsHdr = null, rsNon = null, rsRacik = null, rsRacikDet = null;

    try {
        // --- HEADER RESEP ---
        psHdr = koneksi.prepareStatement(
            "SELECT resep_obat.no_resep, resep_obat.tgl_peresepan, resep_obat.jam_peresepan, " +
            "       pasien.no_rkm_medis, pasien.nm_pasien, dokter.nm_dokter, resep_obat.status " +
            "FROM resep_obat " +
            "INNER JOIN reg_periksa ON reg_periksa.no_rawat = resep_obat.no_rawat " +
            "INNER JOIN pasien ON pasien.no_rkm_medis = reg_periksa.no_rkm_medis " +
            "INNER JOIN dokter ON dokter.kd_dokter = resep_obat.kd_dokter " +
            "WHERE resep_obat.no_resep = ?"
        );
        psHdr.setString(1, noResep);
        rsHdr = psHdr.executeQuery();
        if (rsHdr.next()) {
            sb.append("DETAIL RESEP OBAT\n\n");
            sb.append("No Resep  : ").append(rsHdr.getString("no_resep")).append("\n");
            sb.append("Tanggal   : ").append(rsHdr.getString("tgl_peresepan"))
              .append(" ").append(rsHdr.getString("jam_peresepan")).append("\n");
            sb.append("Pasien    : ").append(rsHdr.getString("no_rkm_medis"))
              .append(" - ").append(rsHdr.getString("nm_pasien")).append("\n");
            sb.append("Dokter    : ").append(rsHdr.getString("nm_dokter")).append("\n");
            sb.append("Status    : ").append(rsHdr.getString("status")).append("\n\n");
        }

        // --- OBAT NON RACIKAN ---
        sb.append("OBAT NON RACIKAN\n\n");
        psNon = koneksi.prepareStatement(
            "SELECT databarang.nama_brng, resep_dokter.jml, databarang.kode_sat, resep_dokter.aturan_pakai " +
            "FROM resep_dokter INNER JOIN databarang ON resep_dokter.kode_brng = databarang.kode_brng " +
            "WHERE resep_dokter.no_resep = ? ORDER BY databarang.nama_brng"
        );
        psNon.setString(1, noResep);
        rsNon = psNon.executeQuery();
        while (rsNon.next()) {
            sb.append("- ")
              .append(rsNon.getString("nama_brng")).append(" ")
              .append(rsNon.getString("jml")).append(" ")
              .append(rsNon.getString("kode_sat"))
              .append(" | Aturan: ").append(rsNon.getString("aturan_pakai"))
              .append("\n");
        }

        // --- OBAT RACIKAN (opsional kalau mau mirip lengkap) ---
        psRacik = koneksi.prepareStatement(
            "SELECT no_racik, nama_racik, jml_dr, aturan_pakai, metode_racik.nm_racik AS metode " +
            "FROM resep_dokter_racikan INNER JOIN metode_racik " +
            "ON resep_dokter_racikan.kd_racik = metode_racik.kd_racik " +
            "WHERE no_resep = ?"
        );
        psRacik.setString(1, noResep);
        rsRacik = psRacik.executeQuery();
        while (rsRacik.next()) {
            sb.append("\nOBAT RACIKAN : ")
              .append(rsRacik.getString("nama_racik"))
              .append(" (No.Racik ").append(rsRacik.getString("no_racik")).append(")")
              .append(" | ").append(rsRacik.getString("metode"))
              .append(" | Aturan: ").append(rsRacik.getString("aturan_pakai"))
              .append("\n");

            psRacikDet = koneksi.prepareStatement(
                "SELECT databarang.nama_brng, resep_dokter_racikan_detail.jml, databarang.kode_sat " +
                "FROM resep_dokter_racikan_detail " +
                "INNER JOIN databarang ON resep_dokter_racikan_detail.kode_brng = databarang.kode_brng " +
                "WHERE resep_dokter_racikan_detail.no_resep = ? AND resep_dokter_racikan_detail.no_racik = ?"
            );
            psRacikDet.setString(1, noResep);
            psRacikDet.setString(2, rsRacik.getString("no_racik"));
            rsRacikDet = psRacikDet.executeQuery();
            while (rsRacikDet.next()) {
                sb.append("   • ")
                  .append(rsRacikDet.getString("nama_brng")).append(" ")
                  .append(rsRacikDet.getString("jml")).append(" ")
                  .append(rsRacikDet.getString("kode_sat"))
                  .append("\n");
            }
            if (rsRacikDet != null) rsRacikDet.close();
            if (psRacikDet != null) psRacikDet.close();
        }

        // --- tampilkan di dialog ---
        JTextArea area = new JTextArea(sb.toString(), 20, 60);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(null, sp, "Detail Resep", JOptionPane.INFORMATION_MESSAGE);

    } catch (Exception e) {
        System.out.println("Notifikasi Detail Resep : " + e);
    } finally {
        try {
            if (rsHdr != null) rsHdr.close();
            if (rsNon != null) rsNon.close();
            if (rsRacik != null) rsRacik.close();
            if (psHdr != null) psHdr.close();
            if (psNon != null) psNon.close();
            if (psRacik != null) psRacik.close();
        } catch (Exception ex) {}
    }
}

}
