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

import bridging.ApiPcare;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.WarnaTable2;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import keuangan.Jurnal;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import simrskhanza.DlgCariBangsal;

/**
 *
 * @author dosen
 */
public final class DlgCariObat2 extends javax.swing.JDialog {
    private final DefaultTableModel tabMode,tabModeObatRacikan,tabModeDetailObatRacikan;;
    private DefaultTableModel tabModeTindakan;
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private Connection koneksi=koneksiDB.condb();
    private riwayatobat Trackobat=new riwayatobat();
    private PreparedStatement psobat,pscarikapasitas,psstok,psrekening,ps2,psbatch;
    private ResultSet rsobat,carikapasitas,rsstok,rsrekening,rs2,rsbatch;
    private Jurnal jur=new Jurnal();
    private double h_belicari=0, hargacari=0, sisacari=0,x=0,y=0,embalase=Sequel.cariIsiAngka("select set_embalase.embalase_per_obat from set_embalase"),
            tuslah=Sequel.cariIsiAngka("select set_embalase.tuslah_per_obat from set_embalase"),kenaikan,stokbarang,ttlhpp,ttljual;
    private int jml=0,i=0,z=0,row=0;
    private boolean[] pilih; 
    private double[] jumlah,harga,eb,ts,stok,beli,kapasitas,kandungan;
    private String[] no,kodebarang,namabarang,kodesatuan,letakbarang,namajenis,industri,aturan,kategori,golongan,nobatch,nofaktur,kadaluarsa;
    private String signa1="1",signa2="1",kdObatSK="",requestJson="",nokunjungan="",URL="",otorisasi,sql="",no_batchcari="", tgl_kadaluarsacari="", 
                   no_fakturcari="",aktifkanbatch="no",aktifpcare="no",noresep="",Suspen_Piutang_Obat_Ranap="",Obat_Ranap="",HPP_Obat_Rawat_Inap="",
                   Persediaan_Obat_Rawat_Inap="",hppfarmasi="",bangsaldefault=Sequel.cariIsi("select set_lokasi.kd_bangsal from set_lokasi limit 1"),
                   VALIDASIULANGBERIOBAT="",DEPOAKTIFOBAT="",utc="";
    private String noRacikAktif="";
    private WarnaTable2 warna=new WarnaTable2();
    private DlgCariBangsal caribangsal=new DlgCariBangsal(null,false);
    public DlgCariAturanPakai aturanpakai=new DlgCariAturanPakai(null,false);
    private DlgCariMetodeRacik metoderacik=new DlgCariMetodeRacik(null,false);
    private WarnaTable2 warna2=new WarnaTable2();
    private WarnaTable2 warna3=new WarnaTable2(){
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
            java.awt.Component component=super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if(column==kolom){
                try {
                    String nilai=table.getValueAt(row,kolom).toString().trim();
                    if(nilai.equals("0")||nilai.equals("0.0")||nilai.equals("0,0")){
                        component.setBackground(new java.awt.Color(215,215,255));
                        component.setForeground(new java.awt.Color(255,255,255));
                    }
                } catch (Exception e) {
                }
            }
            return component;
        }
    };
    private HttpHeaders headers;
    private HttpEntity requestEntity;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private JsonNode nameNode;
    private JsonNode response;
    private ApiPcare api=new ApiPcare();
    private String[] arrSplit;
    private boolean sukses=true;
    private String planSOAPTerakhir="";
    private boolean draftResepSOAPTableChecked=false;
    private String catatanResepDokter="";
    private boolean catatanResepDokterTableChecked=false;
    private java.util.List<String[]> pilihanMetodeRacikFarmasi=new java.util.ArrayList<String[]>();
    private java.util.List<String> pilihanAturanPakaiFarmasi=new java.util.ArrayList<String>();
    private javax.swing.JPanel panelUmumObat;
    private javax.swing.JPanel panelRacikanObat;
    private javax.swing.JPanel panelPlanSOAP;
    private javax.swing.JPanel panelPlanSOAPRacikan;
    private javax.swing.JPanel panelCatatanDokter;
    private javax.swing.JPanel panelCatatanDokterRacikan;
    private widget.ScrollPane scrollPlanSOAP;
    private widget.ScrollPane scrollPlanSOAPRacikan;
    private widget.ScrollPane scrollCatatanDokter;
    private widget.ScrollPane scrollCatatanDokterRacikan;
    private javax.swing.JTextArea areaPlanSOAP;
    private javax.swing.JTextArea areaPlanSOAPRacikan;
    private javax.swing.JTextArea areaCatatanDokter;
    private javax.swing.JTextArea areaCatatanDokterRacikan;
    private javax.swing.JPanel panelTindakanPasien;
    private javax.swing.JPanel panelTindakanPasienRacikan;
    private widget.ScrollPane scrollTindakanPasien;
    private widget.ScrollPane scrollTindakanPasienRacikan;
    private widget.Table tbTindakanPasien;
    private widget.Table tbTindakanPasienRacikan;
    private widget.Label labelTindakanPasien;
    private widget.Label labelTindakanPasienRacikan;
    private widget.Label labelPlanSOAP;
    private widget.Label labelPlanSOAPRacikan;
    private widget.Label labelCatatanDokter;
    private widget.Label labelCatatanDokterRacikan;
    private widget.Label labelDetailRacikanFarmasi;
    /** Buka dialog ini sbg anak resmi dari window pemanggil (owner tersambung
     * dgn benar) supaya tidak bisa ketiban/ketutup window lain saat frmUtama
     * menyusun ulang z-order semua JDialog yg terbuka (lihat formComponentMoved /
     * PanelWallMouseMoved) -- dulu selalu dibuka dgn owner=null shg dianggap
     * window mandiri yg tak terhubung ke layar pemanggilnya. */
    public static DlgCariObat2 buatDari(java.awt.Component parent) {
        java.awt.Window owner = parent instanceof java.awt.Window ? (java.awt.Window) parent : javax.swing.SwingUtilities.getWindowAncestor(parent);
        if (owner instanceof java.awt.Dialog) {
            return new DlgCariObat2((java.awt.Dialog) owner, false);
        } else if (owner instanceof java.awt.Frame) {
            return new DlgCariObat2((java.awt.Frame) owner, false);
        }
        return new DlgCariObat2((java.awt.Frame) null, false);
    }

    /** Creates new form DlgPenyakit
     * @param parent
     * @param modal */
    public DlgCariObat2(java.awt.Frame parent, boolean modal) {
        this((java.awt.Window) parent, modal);
    }

    public DlgCariObat2(java.awt.Dialog parent, boolean modal) {
        this((java.awt.Window) parent, modal);
    }

    private DlgCariObat2(java.awt.Window parent, boolean modal) {
        super(parent);
        setModalityType(modal ? java.awt.Dialog.ModalityType.APPLICATION_MODAL : java.awt.Dialog.ModalityType.MODELESS);
        initComponents();
        inisialisasiPanelTindakanPasien();
        this.setLocation(10,2);
        setSize(656,250);

        tabMode=new DefaultTableModel(null,new Object[]{
            "K","Jumlah","Kode","Nama Barang","Satuan","Kandungan",
            "Harga(Rp)","Jenis Obat","Emb","Tslh","Stok","I.F.","H.Beli",
            "Aturan Pakai","Kategori","Golongan","No.Batch","No.Faktur","Kadaluarsa"
        }){
            @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if ((colIndex==1)||(colIndex==8)||(colIndex==9)||(colIndex==13)||(colIndex==16)||(colIndex==17)) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, 
                java.lang.Object.class, java.lang.Object.class, java.lang.Double.class,  
                java.lang.Object.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class,
                java.lang.Object.class, java.lang.Double.class,java.lang.Object.class,
                java.lang.Object.class, java.lang.Object.class,java.lang.Object.class, 
                java.lang.Object.class, java.lang.Object.class
             };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbObat.setModel(tabMode);
        //tbPenyakit.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbPenyakit.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (i = 0; i < 19; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==1){
                column.setPreferredWidth(45);
            }else if(i==2){
                column.setPreferredWidth(70);
            }else if(i==3){
                column.setPreferredWidth(200);
            }else if(i==4){
                column.setPreferredWidth(70);
            }else if(i==5){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==6){
                column.setPreferredWidth(70);
            }else if(i==7){
                column.setPreferredWidth(85);
            }else if(i==8){
                column.setPreferredWidth(40);
            }else if(i==9){
                column.setPreferredWidth(40);
            }else if(i==10){
                column.setPreferredWidth(40);
            }else if(i==11){
                column.setPreferredWidth(80);
            }else if(i==12){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==13){
                column.setPreferredWidth(100);
            }else if(i==14){
                column.setPreferredWidth(80);
            }else if(i==15){
                column.setPreferredWidth(80);
            }else if(i==16){
                column.setPreferredWidth(70);
            }else if(i==17){
                column.setPreferredWidth(100);
            }else if(i==18){
                column.setPreferredWidth(65);
            }          
        }
        warna.kolom=1;
        tbObat.setDefaultRenderer(Object.class,warna);
        
        tabModeObatRacikan=new DefaultTableModel(null,new Object[]{
                "No","Nama Racikan","Kode Racik","Metode Racik","Jml.Racik",
                "Aturan Pakai","Keterangan"
            }){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = true;
                if ((colIndex==0)||(colIndex==2)) {
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

        tbObatRacikan.setModel(tabModeObatRacikan);
        tbObatRacikan.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObatRacikan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);        
        
        for (i = 0; i < 7; i++) {
            TableColumn column = tbObatRacikan.getColumnModel().getColumn(i);
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
        tbObatRacikan.setDefaultRenderer(Object.class,warna2);
        pasangEditorRacikanFarmasi();
        tabModeObatRacikan.addTableModelListener(new javax.swing.event.TableModelListener() {
            @Override
            public void tableChanged(javax.swing.event.TableModelEvent e) {
                if(e.getType()==javax.swing.event.TableModelEvent.UPDATE && e.getFirstRow()>=0){
                    if(e.getColumn()==3){
                        isiKodeMetodeRacikFarmasi(e.getFirstRow());
                    }
                    if(e.getColumn()==1 || e.getColumn()==3){
                        tampilkanLabelDetailRacikanFarmasi();
                    }
                }
            }
        });
        tbObatRacikan.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if(!e.getValueIsAdjusting()){
                    setNoRacikAktifDariPilihan();
                }
            }
        });
        
        tabModeDetailObatRacikan=new DefaultTableModel(null,new Object[]{
                "No","Kode Barang","Nama Barang","Satuan","Harga(Rp)","H.Beli",
                "Jenis Obat","Stok","Kps","Kandungan","Jumlah",
                "Embal","Tuslah","I.F.","Kategori","Golongan","No.Batch","No.Faktur","Kadaluarsa"
            }){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if ((colIndex==9)||(colIndex==10)||(colIndex==11)||(colIndex==12)||(colIndex==13)||(colIndex==16)||(colIndex==17)) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Double.class, java.lang.Double.class, java.lang.Object.class,
                java.lang.Double.class, java.lang.Double.class, java.lang.Object.class,
                java.lang.Double.class, java.lang.Double.class, java.lang.Double.class,
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
             };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };

        tbDetailObatRacikan.setModel(tabModeDetailObatRacikan);
        tbDetailObatRacikan.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbDetailObatRacikan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);        
        
        for (i = 0; i < 19; i++) {
            TableColumn column = tbDetailObatRacikan.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(25);
            }else if(i==1){
                column.setPreferredWidth(75);
            }else if(i==2){
                column.setPreferredWidth(200);
            }else if(i==3){
                column.setPreferredWidth(45);
            }else if(i==4){
                column.setPreferredWidth(85);
            }else if(i==5){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==6){
                column.setPreferredWidth(85);
            }else if(i==7){
                column.setPreferredWidth(40);
            }else if(i==8){
                column.setPreferredWidth(40);
            }else if(i==9){
                column.setPreferredWidth(70);
            }else if(i==10){
                column.setPreferredWidth(40);
            }else if(i==11){
                column.setPreferredWidth(40);
            }else if(i==12){
                column.setPreferredWidth(50);
            }else if(i==13){
                column.setPreferredWidth(80);
            }else if(i==14){
                column.setPreferredWidth(80);
            }else if(i==15){
                column.setPreferredWidth(80);
            }else if(i==16){
                column.setPreferredWidth(70);
            }else if(i==17){
                column.setPreferredWidth(100);
            }else if(i==18){
                column.setPreferredWidth(65);
            }
        }

        warna3.kolom=10;
        tbDetailObatRacikan.setDefaultRenderer(Object.class,warna3);
        aturTampilanDetailRacikanFarmasi(true);
        
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
        jam();
        
        caribangsal.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(caribangsal.getTable().getSelectedRow()!= -1){                   
                    kdgudang.setText(caribangsal.getTable().getValueAt(caribangsal.getTable().getSelectedRow(),0).toString());
                    nmgudang.setText(caribangsal.getTable().getValueAt(caribangsal.getTable().getSelectedRow(),1).toString());
                } 
                kdgudang.requestFocus();
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
        
        aturanpakai.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(aturanpakai.getTable().getSelectedRow()!= -1){  
                    if(TabRawat.getSelectedIndex()==0){
                        tbObat.setValueAt(aturanpakai.getTable().getValueAt(aturanpakai.getTable().getSelectedRow(),0).toString(),tbObat.getSelectedRow(),13);
                    }else if(TabRawat.getSelectedIndex()==1){
                        tbObatRacikan.setValueAt(aturanpakai.getTable().getValueAt(aturanpakai.getTable().getSelectedRow(),0).toString(),tbObatRacikan.getSelectedRow(),5);
                        tbObatRacikan.requestFocus();
                    }   
                }   
                tbObat.requestFocus();
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
        
        metoderacik.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(metoderacik.getTable().getSelectedRow()!= -1){  
                    tbObatRacikan.setValueAt(metoderacik.getTable().getValueAt(metoderacik.getTable().getSelectedRow(),1).toString(),tbObatRacikan.getSelectedRow(),2);
                    tbObatRacikan.setValueAt(metoderacik.getTable().getValueAt(metoderacik.getTable().getSelectedRow(),2).toString(),tbObatRacikan.getSelectedRow(),3);
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
        
        try {
            aktifkanbatch = koneksiDB.AKTIFKANBATCHOBAT();
            if(aktifkanbatch.equals("no")){
                ppStok.setVisible(true);
            }else{
                ppStok.setVisible(false);
            }
            otorisasi=koneksiDB.USERPCARE()+":"+koneksiDB.PASSPCARE()+":095";
            URL=koneksiDB.URLAPIPCARE();
        } catch (Exception e) {
            System.out.println("E : "+e);
            aktifkanbatch = "no";
            ppStok.setVisible(true);
        }
        
        try {
            hppfarmasi=koneksiDB.HPPFARMASI();
        } catch (Exception e) {
            hppfarmasi="dasar";
        }
        
        try {
            VALIDASIULANGBERIOBAT=koneksiDB.VALIDASIULANGBERIOBAT();
        } catch (Exception e) {
            VALIDASIULANGBERIOBAT="no";
        }
        
        try {
            DEPOAKTIFOBAT = koneksiDB.DEPOAKTIFOBAT();
        } catch (Exception e) {
            System.out.println("E : "+e);
            DEPOAKTIFOBAT = "";
        }
        
        try {
            psrekening=koneksi.prepareStatement("select * from set_akun_ranap");
            try {
                rsrekening=psrekening.executeQuery();
                while(rsrekening.next()){
                    Suspen_Piutang_Obat_Ranap=rsrekening.getString("Suspen_Piutang_Obat_Ranap");
                    Obat_Ranap=rsrekening.getString("Obat_Ranap");
                    HPP_Obat_Rawat_Inap=rsrekening.getString("HPP_Obat_Rawat_Inap");
                    Persediaan_Obat_Rawat_Inap=rsrekening.getString("Persediaan_Obat_Rawat_Inap");
                }
            } catch (Exception e) {
                System.out.println("Notif Rekening : "+e);
            } finally{
                if(rsrekening!=null){
                    rsrekening.close();
                }
                if(psrekening!=null){
                    psrekening.close();
                }
            }            
        } catch (Exception e) {
            System.out.println(e);
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

        Popup = new javax.swing.JPopupMenu();
        ppBersihkan = new javax.swing.JMenuItem();
        ppStok = new javax.swing.JMenuItem();
        ppStok1 = new javax.swing.JMenuItem();
        TNoRw = new widget.TextBox();
        KdPj = new widget.TextBox();
        kelas = new widget.TextBox();
        TNoRM = new widget.TextBox();
        TPasien = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        panelisi3 = new widget.panelisi();
        label9 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        BtnTambah = new widget.Button();
        BtnSeek5 = new widget.Button();
        BtnSimpan = new widget.Button();
        BtnTambah1 = new widget.Button();
        BtnHapus = new widget.Button();
        label13 = new widget.Label();
        BtnKeluar = new widget.Button();
        FormInput = new widget.PanelBiasa();
        jLabel5 = new widget.Label();
        DTPTgl = new widget.Tanggal();
        cmbJam = new widget.ComboBox();
        cmbMnt = new widget.ComboBox();
        cmbDtk = new widget.ComboBox();
        ChkJln = new widget.CekBox();
        label12 = new widget.Label();
        Jeniskelas = new widget.ComboBox();
        ChkNoResep = new widget.CekBox();
        label21 = new widget.Label();
        kdgudang = new widget.TextBox();
        nmgudang = new widget.TextBox();
        BtnGudang = new widget.Button();
        TabRawat = new javax.swing.JTabbedPane();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        Scroll2 = new widget.ScrollPane();
        tbObatRacikan = new widget.Table();
        Scroll3 = new widget.ScrollPane();
        tbDetailObatRacikan = new widget.Table();

        Popup.setName("Popup"); // NOI18N

        ppBersihkan.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppBersihkan.setForeground(new java.awt.Color(50, 50, 50));
        ppBersihkan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppBersihkan.setText("Bersihkan Jumlah");
        ppBersihkan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppBersihkan.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppBersihkan.setName("ppBersihkan"); // NOI18N
        ppBersihkan.setPreferredSize(new java.awt.Dimension(200, 25));
        ppBersihkan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppBersihkanActionPerformed(evt);
            }
        });
        Popup.add(ppBersihkan);

        ppStok.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppStok.setForeground(new java.awt.Color(50, 50, 50));
        ppStok.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppStok.setText("Tampilkan Semua Stok");
        ppStok.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppStok.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppStok.setName("ppStok"); // NOI18N
        ppStok.setPreferredSize(new java.awt.Dimension(200, 25));
        ppStok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppStokActionPerformed(evt);
            }
        });
        Popup.add(ppStok);

        ppStok1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppStok1.setForeground(new java.awt.Color(50, 50, 50));
        ppStok1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppStok1.setText("Tampilkan Stok Lokasi Lain");
        ppStok1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppStok1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppStok1.setName("ppStok1"); // NOI18N
        ppStok1.setPreferredSize(new java.awt.Dimension(200, 25));
        ppStok1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppStok1ActionPerformed(evt);
            }
        });
        Popup.add(ppStok1);

        TNoRw.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N

        KdPj.setHighlighter(null);
        KdPj.setName("KdPj"); // NOI18N
        KdPj.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdPjKeyPressed(evt);
            }
        });

        kelas.setHighlighter(null);
        kelas.setName("kelas"); // NOI18N
        kelas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kelasKeyPressed(evt);
            }
        });

        TNoRM.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N

        TPasien.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Obat, Alkes & BHP Medis ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelisi3.setName("panelisi3"); // NOI18N
        panelisi3.setPreferredSize(new java.awt.Dimension(100, 43));
        panelisi3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 9));

        label9.setText("Key Word :");
        label9.setName("label9"); // NOI18N
        label9.setPreferredSize(new java.awt.Dimension(68, 23));
        panelisi3.add(label9);

        TCari.setToolTipText("Alt+C");
        TCari.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(285, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelisi3.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('2');
        BtnCari.setToolTipText("Alt+2");
        BtnCari.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
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
        panelisi3.add(BtnCari);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('2');
        BtnAll.setToolTipText("Alt+2");
        BtnAll.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
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
        panelisi3.add(BtnAll);

        BtnTambah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        BtnTambah.setMnemonic('3');
        BtnTambah.setToolTipText("Alt+3");
        BtnTambah.setName("BtnTambah"); // NOI18N
        BtnTambah.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTambahActionPerformed(evt);
            }
        });
        panelisi3.add(BtnTambah);

        BtnSeek5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/011.png"))); // NOI18N
        BtnSeek5.setMnemonic('5');
        BtnSeek5.setToolTipText("Alt+5");
        BtnSeek5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
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
        panelisi3.add(BtnSeek5);

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        BtnSimpan.setName("BtnSimpan"); // NOI18N
        BtnSimpan.setPreferredSize(new java.awt.Dimension(28, 23));
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

        label13.setName("label13"); // NOI18N
        label13.setPreferredSize(new java.awt.Dimension(50, 23));
        panelisi3.add(label13);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('4');
        BtnKeluar.setToolTipText("Alt+4");
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluarActionPerformed(evt);
            }
        });
        panelisi3.add(BtnKeluar);

        internalFrame1.add(panelisi3, java.awt.BorderLayout.PAGE_END);

        FormInput.setBackground(new java.awt.Color(215, 225, 215));
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(100, 73));
        FormInput.setLayout(null);

        jLabel5.setText("Tanggal :");
        jLabel5.setName("jLabel5"); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(68, 23));
        FormInput.add(jLabel5);
        jLabel5.setBounds(4, 10, 68, 23);

        DTPTgl.setForeground(new java.awt.Color(50, 70, 50));
        DTPTgl.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "25-10-2021" }));
        DTPTgl.setDisplayFormat("dd-MM-yyyy");
        DTPTgl.setName("DTPTgl"); // NOI18N
        DTPTgl.setOpaque(false);
        DTPTgl.setPreferredSize(new java.awt.Dimension(100, 23));
        DTPTgl.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DTPTglKeyPressed(evt);
            }
        });
        FormInput.add(DTPTgl);
        DTPTgl.setBounds(75, 10, 90, 23);

        cmbJam.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cmbJam.setName("cmbJam"); // NOI18N
        cmbJam.setPreferredSize(new java.awt.Dimension(50, 23));
        cmbJam.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cmbJamKeyPressed(evt);
            }
        });
        FormInput.add(cmbJam);
        cmbJam.setBounds(169, 10, 62, 23);

        cmbMnt.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        cmbMnt.setName("cmbMnt"); // NOI18N
        cmbMnt.setPreferredSize(new java.awt.Dimension(50, 23));
        cmbMnt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cmbMntKeyPressed(evt);
            }
        });
        FormInput.add(cmbMnt);
        cmbMnt.setBounds(234, 10, 62, 23);

        cmbDtk.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        cmbDtk.setName("cmbDtk"); // NOI18N
        cmbDtk.setPreferredSize(new java.awt.Dimension(50, 23));
        cmbDtk.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cmbDtkKeyPressed(evt);
            }
        });
        FormInput.add(cmbDtk);
        cmbDtk.setBounds(299, 10, 62, 23);

        ChkJln.setBorder(null);
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
        ChkJln.setBounds(364, 10, 22, 23);

        label12.setText("Tarif :");
        label12.setName("label12"); // NOI18N
        label12.setPreferredSize(new java.awt.Dimension(50, 23));
        FormInput.add(label12);
        label12.setBounds(382, 10, 50, 23);

        Jeniskelas.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Kelas 1", "Kelas 2", "Kelas 3", "Utama/BPJS", "VIP", "VVIP", "Beli Luar", "Karyawan" }));
        Jeniskelas.setName("Jeniskelas"); // NOI18N
        Jeniskelas.setPreferredSize(new java.awt.Dimension(100, 23));
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
        FormInput.add(Jeniskelas);
        Jeniskelas.setBounds(435, 10, 110, 23);

        ChkNoResep.setSelected(true);
        ChkNoResep.setText("No.Resep   ");
        ChkNoResep.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ChkNoResep.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ChkNoResep.setName("ChkNoResep"); // NOI18N
        ChkNoResep.setOpaque(false);
        ChkNoResep.setPreferredSize(new java.awt.Dimension(85, 23));
        ChkNoResep.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ChkNoResepItemStateChanged(evt);
            }
        });
        FormInput.add(ChkNoResep);
        ChkNoResep.setBounds(548, 10, 100, 23);

        label21.setText("Depo :");
        label21.setName("label21"); // NOI18N
        label21.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label21);
        label21.setBounds(4, 40, 68, 23);

        kdgudang.setEditable(false);
        kdgudang.setName("kdgudang"); // NOI18N
        kdgudang.setPreferredSize(new java.awt.Dimension(80, 23));
        kdgudang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdgudangKeyPressed(evt);
            }
        });
        FormInput.add(kdgudang);
        kdgudang.setBounds(75, 40, 55, 23);

        nmgudang.setEditable(false);
        nmgudang.setName("nmgudang"); // NOI18N
        nmgudang.setPreferredSize(new java.awt.Dimension(207, 23));
        FormInput.add(nmgudang);
        nmgudang.setBounds(132, 40, 197, 23);

        BtnGudang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnGudang.setMnemonic('2');
        BtnGudang.setToolTipText("Alt+2");
        BtnGudang.setName("BtnGudang"); // NOI18N
        BtnGudang.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnGudang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnGudangActionPerformed(evt);
            }
        });
        FormInput.add(BtnGudang);
        BtnGudang.setBounds(332, 40, 28, 23);

        internalFrame1.add(FormInput, java.awt.BorderLayout.PAGE_START);

        TabRawat.setBackground(new java.awt.Color(255, 255, 253));
        TabRawat.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 246, 236)));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
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

        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setComponentPopupMenu(Popup);
        tbObat.setName("tbObat"); // NOI18N
        tbObat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbObatMouseClicked(evt);
            }
        });
        tbObat.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                tbObatPropertyChange(evt);
            }
        });
        tbObat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbObatKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbObat);

        TabRawat.addTab("Umum", Scroll);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(300, 102));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll2.setName("Scroll2"); // NOI18N
        Scroll2.setOpaque(true);
        Scroll2.setPreferredSize(new java.awt.Dimension(454, 90));

        tbObatRacikan.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObatRacikan.setName("tbObatRacikan"); // NOI18N
        tbObatRacikan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbObatRacikanKeyPressed(evt);
            }
        });
        Scroll2.setViewportView(tbObatRacikan);

        jPanel3.add(Scroll2, java.awt.BorderLayout.PAGE_START);

        Scroll3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll3.setName("Scroll3"); // NOI18N
        Scroll3.setOpaque(true);

        tbDetailObatRacikan.setAutoCreateRowSorter(true);
        tbDetailObatRacikan.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbDetailObatRacikan.setName("tbDetailObatRacikan"); // NOI18N
        tbDetailObatRacikan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbDetailObatRacikanMouseClicked(evt);
            }
        });
        tbDetailObatRacikan.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                tbDetailObatRacikanPropertyChange(evt);
            }
        });
        tbDetailObatRacikan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbDetailObatRacikanKeyPressed(evt);
            }
        });
        Scroll3.setViewportView(tbDetailObatRacikan);

        jPanel3.add(Scroll3, java.awt.BorderLayout.CENTER);

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
            tbObat.requestFocus();
        }
}//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        if(TabRawat.getSelectedIndex()==0){
            tampil();
        }else if(TabRawat.getSelectedIndex()==1){
            hentikanEditRacikanFarmasi();
            if(tbObatRacikan.getRowCount()!=0){
                if(tbObatRacikan.getSelectedRow()!= -1){
                    setNoRacikAktifDariPilihan();
                    if(getNoRacikAktif().equals("")||
                            tbObatRacikan.getValueAt(tbObatRacikan.getSelectedRow(),1).toString().equals("")||
                            tbObatRacikan.getValueAt(tbObatRacikan.getSelectedRow(),2).toString().equals("")||
                            tbObatRacikan.getValueAt(tbObatRacikan.getSelectedRow(),3).toString().equals("")||
                            tbObatRacikan.getValueAt(tbObatRacikan.getSelectedRow(),4).toString().equals("")||
                            tbObatRacikan.getValueAt(tbObatRacikan.getSelectedRow(),5).toString().equals("")){
                        JOptionPane.showMessageDialog(null,"Silahkan lengkapi data racikan..!!");
                    }else{
                        tampildetailracikanobat();
                    }
                }else{
                    JOptionPane.showMessageDialog(null,"Silahkan pilih racikan..!!");
                }
            }else{
                JOptionPane.showMessageDialog(null,"Silahkan masukkan racikan..!!");
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
        BtnCariActionPerformed(evt);
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnAllActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnCari, TCari);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked
        if(tbObat.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
            if(evt.getClickCount()==2){
                if(akses.getform().equals("DlgPemberianObat")){
                    dispose();
                }
            }
        }
}//GEN-LAST:event_tbObatMouseClicked

    private void tbObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyPressed
        if(kdgudang.getText().equals("")){
            Valid.textKosong(TCari,"Lokasi");                              
        }else if(tbObat.getRowCount()!=0){
            if(evt.getKeyCode()==KeyEvent.VK_ENTER){
                try {
                    getData();
                    i=tbObat.getSelectedColumn();
                    if(i==8){
                        try {
                            if(tbObat.getValueAt(tbObat.getSelectedRow(),8).toString().equals("0")||tbObat.getValueAt(tbObat.getSelectedRow(),8).toString().equals("")||tbObat.getValueAt(tbObat.getSelectedRow(),8).toString().equals("0.0")||tbObat.getValueAt(tbObat.getSelectedRow(),8).toString().equals("0,0")) {
                                tbObat.setValueAt(embalase,tbObat.getSelectedRow(),8);
                            }
                        } catch (Exception e) {
                            tbObat.setValueAt(0,tbObat.getSelectedRow(),8);
                        }
                    }else if(i==9){
                        try {
                            if(tbObat.getValueAt(tbObat.getSelectedRow(),9).toString().equals("0")||tbObat.getValueAt(tbObat.getSelectedRow(),9).toString().equals("")||tbObat.getValueAt(tbObat.getSelectedRow(),9).toString().equals("0.0")||tbObat.getValueAt(tbObat.getSelectedRow(),9).toString().equals("0,0")) {
                                tbObat.setValueAt(tuslah,tbObat.getSelectedRow(),9);
                            }
                        } catch (Exception e) {
                            tbObat.setValueAt(0,tbObat.getSelectedRow(),9);
                        }
                            
                        TCari.setText("");
                        TCari.requestFocus();
                    }else if((i==10)||(i==3)){
                        TCari.setText("");
                        TCari.requestFocus();
                    }
                } catch (java.lang.NullPointerException e) {
                }
            }else if((evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }else if(evt.getKeyCode()==KeyEvent.VK_DELETE){
                if(tbObat.getSelectedRow()!= -1){
                    tbObat.setValueAt("", tbObat.getSelectedRow(),1);
                }
            }else if(evt.getKeyCode()==KeyEvent.VK_SHIFT){
                TCari.requestFocus();
            }else if(evt.getKeyCode()==KeyEvent.VK_RIGHT){
                i=tbObat.getSelectedColumn();
                if(i==2){
                    try {      
                        getData();
                        
                        try {
                            if(tbObat.getValueAt(tbObat.getSelectedRow(),8).toString().equals("0")||tbObat.getValueAt(tbObat.getSelectedRow(),8).toString().equals("")||tbObat.getValueAt(tbObat.getSelectedRow(),8).toString().equals("0.0")||tbObat.getValueAt(tbObat.getSelectedRow(),8).toString().equals("0,0")) {
                                tbObat.setValueAt(embalase,tbObat.getSelectedRow(),8);
                            }
                        } catch (Exception e) {
                            tbObat.setValueAt(0,tbObat.getSelectedRow(),8);
                        }

                        try {
                            if(tbObat.getValueAt(tbObat.getSelectedRow(),9).toString().equals("0")||tbObat.getValueAt(tbObat.getSelectedRow(),9).toString().equals("")||tbObat.getValueAt(tbObat.getSelectedRow(),9).toString().equals("0.0")||tbObat.getValueAt(tbObat.getSelectedRow(),9).toString().equals("0,0")) {
                                tbObat.setValueAt(tuslah,tbObat.getSelectedRow(),9);
                            }
                        } catch (Exception e) {
                            tbObat.setValueAt(0,tbObat.getSelectedRow(),9);
                        }   
                    
                    } catch (Exception e) {
                        tbObat.setValueAt(0,tbObat.getSelectedRow(),10);
                    }   
                }else if(i==13){
                    aturanpakai.setSize(internalFrame1.getWidth(),internalFrame1.getHeight());
                    aturanpakai.setLocationRelativeTo(internalFrame1);
                    aturanpakai.setVisible(true);
                }
            }             
        }
}//GEN-LAST:event_tbObatKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTambahActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        DlgBarang barang=new DlgBarang(null,false);
        barang.emptTeks();
        barang.isCek();
        barang.setSize(internalFrame1.getWidth(),internalFrame1.getHeight());
        barang.setLocationRelativeTo(internalFrame1);
        barang.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());           
    }//GEN-LAST:event_BtnTambahActionPerformed

private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        hentikanEditRacikanFarmasi();
        tampilkanSemuaDetailRacikan();
        // Penggunaan kapasitas dinonaktifkan. Jumlah obat, termasuk dari paket
        // resep, selalu diperlakukan sebagai jumlah unit yang diinput.
        for(i=0;i<tbObat.getRowCount();i++){
            tbObat.setValueAt(Boolean.FALSE,i,0);
        }
        if(VALIDASIULANGBERIOBAT.equals("yes")){
            for(i=0;i<tbObat.getRowCount();i++){ 
                if(Valid.SetAngka(tbObat.getValueAt(i,1).toString())>0){
                    getDataobat(i);
                } 
            }   
            for(i=0;i<tbDetailObatRacikan.getRowCount();i++){ 
                if(Valid.SetAngka(tbDetailObatRacikan.getValueAt(i,10).toString())>0){
                    getDatadetailobatracikan(i);
                }
            }
        }
        if(TNoRw.getText().trim().equals("")){
            Valid.textKosong(TCari,"Data");
        }else if(kdgudang.getText().equals("")){
            Valid.textKosong(TCari,"Lokasi");                              
        }else{
            int reply = JOptionPane.showConfirmDialog(rootPane,"Eeiiiiiits, udah bener belum data yang mau disimpan..??","Konfirmasi",JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.YES_OPTION) {
                try {  
                    ChkJln.setSelected(false);
                    Sequel.AutoComitFalse();
                    sukses=true;
                    ttlhpp=0;ttljual=0;
                    // Penggunaan kapasitas dinonaktifkan (sama spt DlgPeresepanDokter.simpandata()) --
                    // jumlah yg diinput disimpan apa adanya ke detail_pemberian_obat, tidak dibagi
                    // databarang.kapasitas. Pembagian itu yg bikin billing tampil pecahan (0 koma
                    // sekian) padahal Copy Resep menampilkan angka bulat yg benar.
                    for(i=0;i<tbObat.getRowCount();i++){
                        tbObat.setValueAt(Boolean.FALSE,i,0);
                    }
                    for(i=0;i<tbObat.getRowCount();i++){
                        if(Valid.SetAngka(tbObat.getValueAt(i,1).toString())>0){
                            if(tbObat.getValueAt(i,0).toString().equals("true")){
                                pscarikapasitas= koneksi.prepareStatement("select IFNULL(databarang.kapasitas,1) from databarang where databarang.kode_brng=?");                                      
                                try {
                                    pscarikapasitas.setString(1,tbObat.getValueAt(i,2).toString());
                                    carikapasitas=pscarikapasitas.executeQuery();
                                    if(carikapasitas.next()){ 
                                        if(Sequel.menyimpantf2("detail_pemberian_obat","?,?,?,?,?,?,?,?,?,?,?,?,?,?","data",14,new String[]{
                                            Valid.SetTgl(DTPTgl.getSelectedItem()+""),cmbJam.getSelectedItem()+":"+cmbMnt.getSelectedItem()+":"+cmbDtk.getSelectedItem(),TNoRw.getText(),tbObat.getValueAt(i,2).toString(),tbObat.getValueAt(i,12).toString(),
                                            tbObat.getValueAt(i,6).toString(),""+(Double.parseDouble(tbObat.getValueAt(i,1).toString())/carikapasitas.getDouble(1)),
                                            tbObat.getValueAt(i,8).toString(),tbObat.getValueAt(i,9).toString(),""+Math.round(Double.parseDouble(tbObat.getValueAt(i,8).toString())+
                                                Double.parseDouble(tbObat.getValueAt(i,9).toString())+(Double.parseDouble(tbObat.getValueAt(i,6).toString())*
                                                (Double.parseDouble(tbObat.getValueAt(i,1).toString())/carikapasitas.getDouble(1)))),
                                            "Ranap",kdgudang.getText(),tbObat.getValueAt(i,16).toString(),tbObat.getValueAt(i,17).toString()                            
                                        })==true){
                                            ttljual=ttljual+Math.round(Double.parseDouble(tbObat.getValueAt(i,8).toString())+
                                                    Double.parseDouble(tbObat.getValueAt(i,9).toString())+(Double.parseDouble(tbObat.getValueAt(i,6).toString())*
                                                            (Double.parseDouble(tbObat.getValueAt(i,1).toString())/carikapasitas.getDouble(1))));
                                            ttlhpp=ttlhpp+Math.round(Double.parseDouble(tbObat.getValueAt(i,12).toString())*
                                                            (Double.parseDouble(tbObat.getValueAt(i,1).toString())/carikapasitas.getDouble(1)));
                                            if(!tbObat.getValueAt(i,13).toString().equals("")){
                                                Sequel.menyimpan("aturan_pakai","?,?,?,?,?",5,new String[]{
                                                    Valid.SetTgl(DTPTgl.getSelectedItem()+""),cmbJam.getSelectedItem()+":"+cmbMnt.getSelectedItem()+":"+cmbDtk.getSelectedItem(),TNoRw.getText(),tbObat.getValueAt(i,2).toString(),tbObat.getValueAt(i,13).toString()
                                                }); 
                                            }                                            
                                            
                                            if(aktifkanbatch.equals("yes")){
                                                Sequel.mengedit("data_batch","no_batch=? and kode_brng=? and no_faktur=?","sisa=sisa-?",4,new String[]{
                                                    ""+(Double.parseDouble(tbObat.getValueAt(i,1).toString())/carikapasitas.getDouble(1)),tbObat.getValueAt(i,16).toString(),tbObat.getValueAt(i,2).toString(),tbObat.getValueAt(i,17).toString()
                                                });
                                                Trackobat.catatRiwayat(tbObat.getValueAt(i,2).toString(),0,(Double.parseDouble(tbObat.getValueAt(i,1).toString())/carikapasitas.getDouble(1)),"Pemberian Obat",akses.getkode(),kdgudang.getText(),"Simpan",tbObat.getValueAt(i,16).toString(),tbObat.getValueAt(i,17).toString(),TNoRw.getText()+" "+TNoRM.getText()+" "+TPasien.getText());
                                                Sequel.menyimpan("gudangbarang","'"+tbObat.getValueAt(i,2).toString()+"','"+kdgudang.getText()+"','-"+(Double.parseDouble(tbObat.getValueAt(i,1).toString())/carikapasitas.getDouble(1))+"','"+tbObat.getValueAt(i,16).toString()+"','"+tbObat.getValueAt(i,17).toString()+"'", 
                                                         "stok=stok-'"+(Double.parseDouble(tbObat.getValueAt(i,1).toString())/carikapasitas.getDouble(1))+"'","kode_brng='"+tbObat.getValueAt(i,2).toString()+"' and kd_bangsal='"+kdgudang.getText()+"' and no_batch='"+tbObat.getValueAt(i,16).toString()+"' and no_faktur='"+tbObat.getValueAt(i,17).toString()+"'");   
                                            }else{
                                                Trackobat.catatRiwayat(tbObat.getValueAt(i,2).toString(),0,(Double.parseDouble(tbObat.getValueAt(i,1).toString())/carikapasitas.getDouble(1)),"Pemberian Obat",akses.getkode(),kdgudang.getText(),"Simpan","","",TNoRw.getText()+" "+TNoRM.getText()+" "+TPasien.getText());
                                                Sequel.menyimpan("gudangbarang","'"+tbObat.getValueAt(i,2).toString()+"','"+kdgudang.getText()+"','-"+(Double.parseDouble(tbObat.getValueAt(i,1).toString())/carikapasitas.getDouble(1))+"','',''", 
                                                         "stok=stok-'"+(Double.parseDouble(tbObat.getValueAt(i,1).toString())/carikapasitas.getDouble(1))+"'","kode_brng='"+tbObat.getValueAt(i,2).toString()+"' and kd_bangsal='"+kdgudang.getText()+"' and no_batch='' and no_faktur=''");  
                                            }
                                            
                                            if(aktifpcare.equals("yes")){
                                                arrSplit = tbObat.getValueAt(i,13).toString().toLowerCase().split("x");
                                                signa1="1";
                                                try {
                                                    if(!arrSplit[0].replaceAll("[^0-9.]+", "").equals("")){
                                                        signa1=arrSplit[0].replaceAll("[^0-9.]+", "");
                                                    }
                                                } catch (Exception e) {
                                                    signa1="1";
                                                }
                                                signa2="1";
                                                try {
                                                    if(!arrSplit[1].replaceAll("[^0-9.]+", "").equals("")){
                                                        signa2=arrSplit[1].replaceAll("[^0-9.]+", "");
                                                    }
                                                } catch (Exception e) {
                                                    signa2="1";
                                                } 
                                                simpanObatPCare(
                                                    nokunjungan,"false",Sequel.cariIsi("select kode_brng_pcare from maping_obat_pcare where kode_brng=?",tbObat.getValueAt(i,2).toString()),
                                                    signa1,signa2,""+(Double.parseDouble(tbObat.getValueAt(i,1).toString())/carikapasitas.getDouble(1)),""+(Double.parseDouble(tbObat.getValueAt(i,1).toString())/carikapasitas.getDouble(1)),"",TNoRw.getText(),
                                                    Valid.SetTgl(DTPTgl.getSelectedItem()+""),cmbJam.getSelectedItem()+":"+cmbMnt.getSelectedItem()+":"+cmbDtk.getSelectedItem(),
                                                    tbObat.getValueAt(i,2).toString(),tbObat.getValueAt(i,16).toString()
                                                );
                                            }
                                        }else{
                                            sukses=false;
                                        }  
                                    }else{
                                        if(Sequel.menyimpantf2("detail_pemberian_obat","?,?,?,?,?,?,?,?,?,?,?,?,?,?","data",14,new String[]{
                                            Valid.SetTgl(DTPTgl.getSelectedItem()+""),cmbJam.getSelectedItem()+":"+cmbMnt.getSelectedItem()+":"+cmbDtk.getSelectedItem(),TNoRw.getText(),tbObat.getValueAt(i,2).toString(),tbObat.getValueAt(i,12).toString(),
                                            tbObat.getValueAt(i,6).toString(),""+Double.parseDouble(tbObat.getValueAt(i,1).toString()),
                                            tbObat.getValueAt(i,8).toString(),tbObat.getValueAt(i,9).toString(),""+Math.round(Double.parseDouble(tbObat.getValueAt(i,8).toString())+
                                                Double.parseDouble(tbObat.getValueAt(i,9).toString())+(Double.parseDouble(tbObat.getValueAt(i,6).toString())*
                                                Double.parseDouble(tbObat.getValueAt(i,1).toString()))),
                                            "Ranap",kdgudang.getText(),tbObat.getValueAt(i,16).toString(),tbObat.getValueAt(i,17).toString() 
                                        })==true){    
                                            ttljual=ttljual+Math.round(Double.parseDouble(tbObat.getValueAt(i,8).toString())+
                                                    Double.parseDouble(tbObat.getValueAt(i,9).toString())+(Double.parseDouble(tbObat.getValueAt(i,6).toString())*
                                                            Double.parseDouble(tbObat.getValueAt(i,1).toString())));
                                            ttlhpp=ttlhpp+Math.round(Double.parseDouble(tbObat.getValueAt(i,12).toString())*
                                                            Double.parseDouble(tbObat.getValueAt(i,1).toString()));
                                            if(!tbObat.getValueAt(i,13).toString().equals("")){
                                                Sequel.menyimpan("aturan_pakai","?,?,?,?,?",5,new String[]{
                                                    Valid.SetTgl(DTPTgl.getSelectedItem()+""),cmbJam.getSelectedItem()+":"+cmbMnt.getSelectedItem()+":"+cmbDtk.getSelectedItem(),TNoRw.getText(),tbObat.getValueAt(i,2).toString(),tbObat.getValueAt(i,13).toString()
                                                });
                                            }                                            
                                                
                                            if(aktifkanbatch.equals("yes")){
                                                Sequel.mengedit("data_batch","no_batch=? and kode_brng=? and no_faktur=?","sisa=sisa-?",4,new String[]{
                                                    ""+(Double.parseDouble(tbObat.getValueAt(i,1).toString())),tbObat.getValueAt(i,16).toString(),tbObat.getValueAt(i,2).toString(),tbObat.getValueAt(i,17).toString()
                                                }); 
                                                Trackobat.catatRiwayat(tbObat.getValueAt(i,2).toString(),0,Double.parseDouble(tbObat.getValueAt(i,1).toString()),"Pemberian Obat",akses.getkode(),kdgudang.getText(),"Simpan",tbObat.getValueAt(i,16).toString(),tbObat.getValueAt(i,17).toString(),TNoRw.getText()+" "+TNoRM.getText()+" "+TPasien.getText());
                                                Sequel.menyimpan("gudangbarang","'"+tbObat.getValueAt(i,2).toString()+"','"+kdgudang.getText()+"','-"+Double.parseDouble(tbObat.getValueAt(i,1).toString())+"','"+tbObat.getValueAt(i,16).toString()+"','"+tbObat.getValueAt(i,17).toString()+"'", 
                                                         "stok=stok-'"+Double.parseDouble(tbObat.getValueAt(i,1).toString())+"'","kode_brng='"+tbObat.getValueAt(i,2).toString()+"' and kd_bangsal='"+kdgudang.getText()+"' and no_batch='"+tbObat.getValueAt(i,16).toString()+"' and no_faktur='"+tbObat.getValueAt(i,17).toString()+"'");  
                                            }else{
                                                Trackobat.catatRiwayat(tbObat.getValueAt(i,2).toString(),0,Double.parseDouble(tbObat.getValueAt(i,1).toString()),"Pemberian Obat",akses.getkode(),kdgudang.getText(),"Simpan","","",TNoRw.getText()+" "+TNoRM.getText()+" "+TPasien.getText());
                                                Sequel.menyimpan("gudangbarang","'"+tbObat.getValueAt(i,2).toString()+"','"+kdgudang.getText()+"','-"+Double.parseDouble(tbObat.getValueAt(i,1).toString())+"','',''", 
                                                         "stok=stok-'"+Double.parseDouble(tbObat.getValueAt(i,1).toString())+"'","kode_brng='"+tbObat.getValueAt(i,2).toString()+"' and kd_bangsal='"+kdgudang.getText()+"' and no_batch='' and no_faktur=''");   
                                            }
                                            
                                            if(aktifpcare.equals("yes")){
                                                arrSplit = tbObat.getValueAt(i,13).toString().toLowerCase().split("x");
                                                signa1="1";
                                                try {
                                                    if(!arrSplit[0].replaceAll("[^0-9.]+", "").equals("")){
                                                        signa1=arrSplit[0].replaceAll("[^0-9.]+", "");
                                                    }
                                                } catch (Exception e) {
                                                    signa1="1";
                                                }
                                                signa2="1";
                                                try {
                                                    if(!arrSplit[1].replaceAll("[^0-9.]+", "").equals("")){
                                                        signa2=arrSplit[1].replaceAll("[^0-9.]+", "");
                                                    }
                                                } catch (Exception e) {
                                                    signa2="1";
                                                } 
                                                simpanObatPCare(
                                                    nokunjungan,"false",Sequel.cariIsi("select kode_brng_pcare from maping_obat_pcare where kode_brng=?",tbObat.getValueAt(i,2).toString()),
                                                    signa1,signa2,""+Double.parseDouble(tbObat.getValueAt(i,1).toString()),""+Double.parseDouble(tbObat.getValueAt(i,1).toString()),"",TNoRw.getText(),
                                                    Valid.SetTgl(DTPTgl.getSelectedItem()+""),cmbJam.getSelectedItem()+":"+cmbMnt.getSelectedItem()+":"+cmbDtk.getSelectedItem(),
                                                    tbObat.getValueAt(i,2).toString(),tbObat.getValueAt(i,16).toString()
                                                );
                                            }
                                        }else{
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
                                if(Sequel.menyimpantf2("detail_pemberian_obat","?,?,?,?,?,?,?,?,?,?,?,?,?,?","data",14,new String[]{
                                    Valid.SetTgl(DTPTgl.getSelectedItem()+""),cmbJam.getSelectedItem()+":"+cmbMnt.getSelectedItem()+":"+cmbDtk.getSelectedItem(),TNoRw.getText(),tbObat.getValueAt(i,2).toString(),tbObat.getValueAt(i,12).toString(),
                                    tbObat.getValueAt(i,6).toString(),""+Double.parseDouble(tbObat.getValueAt(i,1).toString()),
                                    tbObat.getValueAt(i,8).toString(),tbObat.getValueAt(i,9).toString(),""+Math.round(Double.parseDouble(tbObat.getValueAt(i,8).toString())+
                                        Double.parseDouble(tbObat.getValueAt(i,9).toString())+(Double.parseDouble(tbObat.getValueAt(i,6).toString())*
                                        Double.parseDouble(tbObat.getValueAt(i,1).toString()))),
                                    "Ranap",kdgudang.getText(),tbObat.getValueAt(i,16).toString(),tbObat.getValueAt(i,17).toString() 
                                })==true){ 
                                    ttljual=ttljual+Math.round(Double.parseDouble(tbObat.getValueAt(i,8).toString())+
                                            Double.parseDouble(tbObat.getValueAt(i,9).toString())+(Double.parseDouble(tbObat.getValueAt(i,6).toString())*
                                                    Double.parseDouble(tbObat.getValueAt(i,1).toString())));
                                    ttlhpp=ttlhpp+Math.round(Double.parseDouble(tbObat.getValueAt(i,12).toString())*
                                                    Double.parseDouble(tbObat.getValueAt(i,1).toString()));
                                    if(!tbObat.getValueAt(i,13).toString().equals("")){
                                        Sequel.menyimpan("aturan_pakai","?,?,?,?,?",5,new String[]{
                                            Valid.SetTgl(DTPTgl.getSelectedItem()+""),cmbJam.getSelectedItem()+":"+cmbMnt.getSelectedItem()+":"+cmbDtk.getSelectedItem(),TNoRw.getText(),tbObat.getValueAt(i,2).toString(),tbObat.getValueAt(i,13).toString()
                                        });
                                    }                                
                                    
                                    if(aktifkanbatch.equals("yes")){
                                        Sequel.mengedit("data_batch","no_batch=? and kode_brng=? and no_faktur=?","sisa=sisa-?",4,new String[]{
                                            ""+Double.parseDouble(tbObat.getValueAt(i,1).toString()),tbObat.getValueAt(i,16).toString(),tbObat.getValueAt(i,2).toString(),tbObat.getValueAt(i,17).toString()
                                        });
                                        Trackobat.catatRiwayat(tbObat.getValueAt(i,2).toString(),0,Double.parseDouble(tbObat.getValueAt(i,1).toString()),"Pemberian Obat",akses.getkode(),kdgudang.getText(),"Simpan",tbObat.getValueAt(i,16).toString(),tbObat.getValueAt(i,17).toString(),TNoRw.getText()+" "+TNoRM.getText()+" "+TPasien.getText());
                                        Sequel.menyimpan("gudangbarang","'"+tbObat.getValueAt(i,2).toString()+"','"+kdgudang.getText()+"','-"+Double.parseDouble(tbObat.getValueAt(i,1).toString())+"','"+tbObat.getValueAt(i,16).toString()+"','"+tbObat.getValueAt(i,17).toString()+"'", 
                                                 "stok=stok-'"+Double.parseDouble(tbObat.getValueAt(i,1).toString())+"'","kode_brng='"+tbObat.getValueAt(i,2).toString()+"' and kd_bangsal='"+kdgudang.getText()+"' and no_batch='"+tbObat.getValueAt(i,16).toString()+"' and no_faktur='"+tbObat.getValueAt(i,17).toString()+"'");   
                                    }else{ 
                                        Trackobat.catatRiwayat(tbObat.getValueAt(i,2).toString(),0,Double.parseDouble(tbObat.getValueAt(i,1).toString()),"Pemberian Obat",akses.getkode(),kdgudang.getText(),"Simpan","","",TNoRw.getText()+" "+TNoRM.getText()+" "+TPasien.getText());
                                        Sequel.menyimpan("gudangbarang","'"+tbObat.getValueAt(i,2).toString()+"','"+kdgudang.getText()+"','-"+Double.parseDouble(tbObat.getValueAt(i,1).toString())+"','',''", 
                                                 "stok=stok-'"+Double.parseDouble(tbObat.getValueAt(i,1).toString())+"'","kode_brng='"+tbObat.getValueAt(i,2).toString()+"' and kd_bangsal='"+kdgudang.getText()+"' and no_batch='' and no_faktur=''");  
                                    }
                                    
                                    if(aktifpcare.equals("yes")){
                                        arrSplit = tbObat.getValueAt(i,13).toString().toLowerCase().split("x");
                                        signa1="1";
                                        try {
                                            if(!arrSplit[0].replaceAll("[^0-9.]+", "").equals("")){
                                                signa1=arrSplit[0].replaceAll("[^0-9.]+", "");
                                            }
                                        } catch (Exception e) {
                                            signa1="1";
                                        }
                                        signa2="1";
                                        try {
                                            if(!arrSplit[1].replaceAll("[^0-9.]+", "").equals("")){
                                                signa2=arrSplit[1].replaceAll("[^0-9.]+", "");
                                            }
                                        } catch (Exception e) {
                                            signa2="1";
                                        } 
                                        simpanObatPCare(
                                            nokunjungan,"false",Sequel.cariIsi("select kode_brng_pcare from maping_obat_pcare where kode_brng=?",tbObat.getValueAt(i,2).toString()),
                                            signa1,signa2,""+Double.parseDouble(tbObat.getValueAt(i,1).toString()),""+Double.parseDouble(tbObat.getValueAt(i,1).toString()),"",TNoRw.getText(),
                                            Valid.SetTgl(DTPTgl.getSelectedItem()+""),cmbJam.getSelectedItem()+":"+cmbMnt.getSelectedItem()+":"+cmbDtk.getSelectedItem(),
                                            tbObat.getValueAt(i,2).toString(),tbObat.getValueAt(i,16).toString()
                                        );
                                    }
                                }else{
                                    sukses=false;
                                }                                   
                            }                            
                        }
                    }  

                    for(i=0;i<tbObatRacikan.getRowCount();i++){ 
                        if(Valid.SetAngka(tbObatRacikan.getValueAt(i,4).toString())>0){ 
                            if(Sequel.menyimpantf2("obat_racikan","?,?,?,?,?,?,?,?,?","Obat Racikan",9,new String[]{
                               Valid.SetTgl(DTPTgl.getSelectedItem()+""),cmbJam.getSelectedItem()+":"+cmbMnt.getSelectedItem()+":"+cmbDtk.getSelectedItem(),TNoRw.getText(),
                               tbObatRacikan.getValueAt(i,0).toString(),tbObatRacikan.getValueAt(i,1).toString(),
                               tbObatRacikan.getValueAt(i,2).toString(),tbObatRacikan.getValueAt(i,4).toString(),
                               tbObatRacikan.getValueAt(i,5).toString(),tbObatRacikan.getValueAt(i,6).toString()
                            })==false){
                                sukses=false;
                            }
                        }
                    }

                    gabungkanDuplikatDetailRacikan();
                    java.util.HashSet<String> detailRacikanTersimpan=new java.util.HashSet<String>();
                    for(i=0;i<tbDetailObatRacikan.getRowCount();i++){ 
                        if(Valid.SetAngka(tbDetailObatRacikan.getValueAt(i,10).toString())>0){
                            String kunciDetail=tbDetailObatRacikan.getValueAt(i,0).toString()+"|"+tbDetailObatRacikan.getValueAt(i,1).toString();
                            boolean detailRacikanBerhasil=true;
                            if(!detailRacikanTersimpan.contains(kunciDetail)){
                                detailRacikanBerhasil=Sequel.menyimpantf2("detail_obat_racikan","?,?,?,?,?","Data",5,new String[]{
                                   Valid.SetTgl(DTPTgl.getSelectedItem()+""),cmbJam.getSelectedItem()+":"+cmbMnt.getSelectedItem()+":"+cmbDtk.getSelectedItem(),TNoRw.getText(),
                                   tbDetailObatRacikan.getValueAt(i,0).toString(),tbDetailObatRacikan.getValueAt(i,1).toString()
                                });
                                if(detailRacikanBerhasil){
                                    detailRacikanTersimpan.add(kunciDetail);
                                }
                            }
                            if(detailRacikanBerhasil==true){
                                if(Sequel.menyimpantf2("detail_pemberian_obat","?,?,?,?,?,?,?,?,?,?,?,?,?,?","data",14,new String[]{
                                    Valid.SetTgl(DTPTgl.getSelectedItem()+""),cmbJam.getSelectedItem()+":"+cmbMnt.getSelectedItem()+":"+cmbDtk.getSelectedItem(),TNoRw.getText(),
                                    tbDetailObatRacikan.getValueAt(i,1).toString(),tbDetailObatRacikan.getValueAt(i,5).toString(),
                                    tbDetailObatRacikan.getValueAt(i,4).toString(),""+Double.parseDouble(tbDetailObatRacikan.getValueAt(i,10).toString()),
                                    tbDetailObatRacikan.getValueAt(i,11).toString(),tbDetailObatRacikan.getValueAt(i,12).toString(),
                                    ""+Math.round(Double.parseDouble(tbDetailObatRacikan.getValueAt(i,11).toString())+                                        
                                        Double.parseDouble(tbDetailObatRacikan.getValueAt(i,12).toString())+
                                        (Double.parseDouble(tbDetailObatRacikan.getValueAt(i,4).toString())*
                                        Double.parseDouble(tbDetailObatRacikan.getValueAt(i,10).toString()))),
                                    "Ranap",kdgudang.getText(),tbDetailObatRacikan.getValueAt(i,16).toString(),tbDetailObatRacikan.getValueAt(i,17).toString()
                                })==true){
                                    ttljual=ttljual+Math.round(Double.parseDouble(tbDetailObatRacikan.getValueAt(i,11).toString())+
                                            Double.parseDouble(tbDetailObatRacikan.getValueAt(i,12).toString())+(Double.parseDouble(tbDetailObatRacikan.getValueAt(i,4).toString())*
                                                    Double.parseDouble(tbDetailObatRacikan.getValueAt(i,10).toString())));
                                    ttlhpp=ttlhpp+Math.round(Double.parseDouble(tbDetailObatRacikan.getValueAt(i,5).toString())*
                                                    Double.parseDouble(tbDetailObatRacikan.getValueAt(i,10).toString()));
                                    if(aktifkanbatch.equals("yes")){
                                        Sequel.mengedit("data_batch","no_batch=? and kode_brng=? and no_faktur=?","sisa=sisa-?",4,new String[]{
                                            ""+Double.parseDouble(tbDetailObatRacikan.getValueAt(i,10).toString()),tbDetailObatRacikan.getValueAt(i,16).toString(),tbDetailObatRacikan.getValueAt(i,1).toString(),tbDetailObatRacikan.getValueAt(i,17).toString()
                                        }); 
                                        Trackobat.catatRiwayat(tbDetailObatRacikan.getValueAt(i,1).toString(),0,Double.parseDouble(tbDetailObatRacikan.getValueAt(i,10).toString()),"Pemberian Obat",akses.getkode(),kdgudang.getText(),"Simpan",tbDetailObatRacikan.getValueAt(i,16).toString(),tbDetailObatRacikan.getValueAt(i,17).toString(),TNoRw.getText()+" "+TNoRM.getText()+" "+TPasien.getText());
                                        Sequel.menyimpan("gudangbarang","'"+tbDetailObatRacikan.getValueAt(i,1).toString()+"','"+kdgudang.getText()+"','-"+Double.parseDouble(tbDetailObatRacikan.getValueAt(i,10).toString())+"','"+tbDetailObatRacikan.getValueAt(i,16).toString()+"','"+tbDetailObatRacikan.getValueAt(i,17).toString()+"'", 
                                                 "stok=stok-'"+Double.parseDouble(tbDetailObatRacikan.getValueAt(i,10).toString())+"'","kode_brng='"+tbDetailObatRacikan.getValueAt(i,1).toString()+"' and kd_bangsal='"+kdgudang.getText()+"' and no_batch='"+tbDetailObatRacikan.getValueAt(i,16).toString()+"' and no_faktur='"+tbDetailObatRacikan.getValueAt(i,17).toString()+"'");  
                                    }else{
                                        Trackobat.catatRiwayat(tbDetailObatRacikan.getValueAt(i,1).toString(),0,Double.parseDouble(tbDetailObatRacikan.getValueAt(i,10).toString()),"Pemberian Obat",akses.getkode(),kdgudang.getText(),"Simpan","","",TNoRw.getText()+" "+TNoRM.getText()+" "+TPasien.getText());
                                        Sequel.menyimpan("gudangbarang","'"+tbDetailObatRacikan.getValueAt(i,1).toString()+"','"+kdgudang.getText()+"','-"+Double.parseDouble(tbDetailObatRacikan.getValueAt(i,10).toString())+"','',''", 
                                                 "stok=stok-'"+Double.parseDouble(tbDetailObatRacikan.getValueAt(i,10).toString())+"'","kode_brng='"+tbDetailObatRacikan.getValueAt(i,1).toString()+"' and kd_bangsal='"+kdgudang.getText()+"' and no_batch='' and no_faktur=''");   
                                    }
                                    
                                    if(aktifpcare.equals("yes")){
                                        arrSplit = Sequel.cariIsi("select aturan_pakai from obat_racikan where tgl_perawatan='"+Valid.SetTgl(DTPTgl.getSelectedItem()+"")+"' and "+
                                                    "jam='"+cmbJam.getSelectedItem()+":"+cmbMnt.getSelectedItem()+":"+cmbDtk.getSelectedItem()+"' and "+
                                                    "no_rawat='"+TNoRw.getText()+"' and no_racik='"+tbDetailObatRacikan.getValueAt(i,0).toString()+"'").toLowerCase().split("x");
                                        signa1="1";
                                        try {
                                            if(!arrSplit[0].replaceAll("[^0-9.]+", "").equals("")){
                                                signa1=arrSplit[0].replaceAll("[^0-9.]+", "");
                                            }
                                        } catch (Exception e) {
                                            signa1="1";
                                        }
                                        signa2="1";
                                        try {
                                            if(!arrSplit[1].replaceAll("[^0-9.]+", "").equals("")){
                                                signa2=arrSplit[1].replaceAll("[^0-9.]+", "");
                                            }
                                        } catch (Exception e) {
                                            signa2="2";
                                        } 
                                        simpanObatPCare(
                                            nokunjungan,"true",Sequel.cariIsi("select kode_brng_pcare from maping_obat_pcare where kode_brng=?",tbDetailObatRacikan.getValueAt(i,1).toString()),
                                            signa1,signa2,""+Double.parseDouble(tbDetailObatRacikan.getValueAt(i,10).toString()),""+Double.parseDouble(tbDetailObatRacikan.getValueAt(i,10).toString()),"",TNoRw.getText(),
                                            Valid.SetTgl(DTPTgl.getSelectedItem()+""),cmbJam.getSelectedItem()+":"+cmbMnt.getSelectedItem()+":"+cmbDtk.getSelectedItem(),
                                            tbDetailObatRacikan.getValueAt(i,1).toString(),tbDetailObatRacikan.getValueAt(i,16).toString()
                                        );
                                    }
                                }else{
                                    sukses=false;
                                }  
                            }else{
                                sukses=false;
                            }    
                        }
                    }
                    
                    if(!noresep.equals("")){
                        Sequel.mengedit("resep_obat","no_resep='"+noresep+"'","tgl_perawatan='"+Valid.SetTgl(DTPTgl.getSelectedItem()+"")+"',jam='"+cmbJam.getSelectedItem()+":"+cmbMnt.getSelectedItem()+":"+cmbDtk.getSelectedItem()+"'");
                    }
                    
                    if(sukses==true){
                        Sequel.queryu("delete from tampjurnal");    
                        if(ttljual>0){
                            Sequel.menyimpan("tampjurnal","'"+Suspen_Piutang_Obat_Ranap+"','Suspen Piutang Obat Ranap','"+ttljual+"','0'","Rekening");    
                            Sequel.menyimpan("tampjurnal","'"+Obat_Ranap+"','Pendapatan Obat Rawat Inap','0','"+ttljual+"'","Rekening");                              
                        }
                        if(ttlhpp>0){
                            Sequel.menyimpan("tampjurnal","'"+HPP_Obat_Rawat_Inap+"','HPP Persediaan Obat Rawat Inap','"+ttlhpp+"','0'","Rekening");    
                            Sequel.menyimpan("tampjurnal","'"+Persediaan_Obat_Rawat_Inap+"','Persediaan Obat Rawat Inap','0','"+ttlhpp+"'","Rekening");                              
                        }
                        if((ttljual>0)||(ttlhpp>0)){
                            sukses=jur.simpanJurnal(TNoRw.getText(),"U","PEMBERIAN OBAT RAWAT INAP PASIEN "+TNoRw.getText()+" "+TNoRM.getText()+" "+TPasien.getText()+", DIPOSTING OLEH "+akses.getkode());     
                        }
                    }
                    
                    if(sukses==true){
                        Sequel.Commit();
                        for(i=0;i<tbObat.getRowCount();i++){
                            tbObat.setValueAt("",i,1);
                        }
                        Valid.tabelKosong(tabModeObatRacikan);
                        Valid.tabelKosong(tabModeDetailObatRacikan);
                    }else{
                        sukses=false;
                        JOptionPane.showMessageDialog(null,"Terjadi kesalahan saat pemrosesan data, transaksi dibatalkan.\nPeriksa kembali data sebelum melanjutkan menyimpan..!!");
                        Sequel.RollBack();
                    }
                    
                    Sequel.AutoComitTrue();
                    ChkJln.setSelected(true);
                                                                    
                    if(sukses==true){
                        if(ChkNoResep.isSelected()==true){
                            DlgResepObat resep=new DlgResepObat(null,false);
                            resep.setSize(internalFrame1.getWidth(),internalFrame1.getHeight());
                            resep.setLocationRelativeTo(internalFrame1);
                            resep.emptTeks(); 
                            resep.isCek();
                            resep.setNoRm(TNoRw.getText(),DTPTgl.getDate(),DTPTgl.getDate(),cmbJam.getSelectedItem().toString(),cmbMnt.getSelectedItem().toString(),cmbDtk.getSelectedItem().toString(),"ranap");
                            resep.tampil();
                            //resep.setAlwaysOnTop(true);
                            resep.dokter.setAlwaysOnTop(true);
                            resep.setVisible(true);
                        }
                        dispose();  
                    }                       
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }                
        }
}//GEN-LAST:event_BtnSimpanActionPerformed

private void BtnSeek5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek5ActionPerformed
    DlgCariKonversi carikonversi=new DlgCariKonversi(null,false);
    carikonversi.setLocationRelativeTo(internalFrame1);
    carikonversi.setVisible(true);
}//GEN-LAST:event_BtnSeek5ActionPerformed

private void BtnSeek5KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeek5KeyPressed
// TODO add your handling code here:
}//GEN-LAST:event_BtnSeek5KeyPressed

private void ppBersihkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppBersihkanActionPerformed
    for(i=0;i<tbObat.getRowCount();i++){ 
        tbObat.setValueAt("",i,1);
        tbObat.setValueAt(0,i,10);
        tbObat.setValueAt(0,i,9);
        tbObat.setValueAt(0,i,8);
    }
}//GEN-LAST:event_ppBersihkanActionPerformed

private void JeniskelasItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_JeniskelasItemStateChanged
    if(this.isVisible()==true){
        tampil(); 
    }        
}//GEN-LAST:event_JeniskelasItemStateChanged

private void JeniskelasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JeniskelasKeyPressed
        Valid.pindah(evt, cmbDtk,TCari);
}//GEN-LAST:event_JeniskelasKeyPressed

private void DTPTglKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DTPTglKeyPressed
       Valid.pindah(evt,BtnKeluar,cmbJam);
}//GEN-LAST:event_DTPTglKeyPressed

private void cmbJamKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cmbJamKeyPressed
        Valid.pindah(evt,DTPTgl,cmbMnt);
}//GEN-LAST:event_cmbJamKeyPressed

private void cmbMntKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cmbMntKeyPressed
        Valid.pindah(evt,cmbJam,cmbDtk);
}//GEN-LAST:event_cmbMntKeyPressed

private void cmbDtkKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cmbDtkKeyPressed
        Valid.pindah(evt,cmbMnt,Jeniskelas);
}//GEN-LAST:event_cmbDtkKeyPressed

private void ChkJlnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkJlnActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_ChkJlnActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        if(TabRawat.getSelectedIndex()==0){
            BtnTambah1.setVisible(false);
            BtnHapus.setVisible(false);
            label13.setPreferredSize(new Dimension(65, 23));
        }else if(TabRawat.getSelectedIndex()==1){
            BtnTambah1.setVisible(true);
            BtnHapus.setVisible(true);
            label13.setPreferredSize(new Dimension(1, 23));
            setNoRacikAktifDariPilihan();
        }
    }//GEN-LAST:event_formWindowActivated

    private void ChkNoResepItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_ChkNoResepItemStateChanged
        if(ChkNoResep.isSelected()==true){
                    DlgResepObat resep=new DlgResepObat(null,false);
                    resep.setSize(internalFrame1.getWidth(),internalFrame1.getHeight());
                    resep.setLocationRelativeTo(internalFrame1);
                    resep.emptTeks(); 
                    resep.isCek();
                    resep.setNoRm(TNoRw.getText(),DTPTgl.getDate(),DTPTgl.getDate(),cmbJam.getSelectedItem().toString(),cmbMnt.getSelectedItem().toString(),cmbDtk.getSelectedItem().toString(),"ralan");
                    resep.tampil();
                    resep.setVisible(true);
                }
    }//GEN-LAST:event_ChkNoResepItemStateChanged

    private void KdPjKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdPjKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_KdPjKeyPressed

    private void kelasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kelasKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_kelasKeyPressed

    private void ppStokActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppStokActionPerformed
        if(kdgudang.getText().equals("")){
            Valid.textKosong(TCari,"Lokasi");                              
        }else{
            if(TabRawat.getSelectedIndex()==0){
                for(i=0;i<tbObat.getRowCount();i++){ 
                    getDataobat(i); 
                }   
            }else if(TabRawat.getSelectedIndex()==1){
                for(i=0;i<tbDetailObatRacikan.getRowCount();i++){ 
                    getDatadetailobatracikan(i);
                }
            }
        }
    }//GEN-LAST:event_ppStokActionPerformed

    private void kdgudangKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdgudangKeyPressed
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_PAGE_DOWN:
                Sequel.cariIsi("select bangsal.nm_bangsal from bangsal where bangsal.kd_bangsal=?",nmgudang,kdgudang.getText());
                break;
            case KeyEvent.VK_PAGE_UP:
                Sequel.cariIsi("select bangsal.nm_bangsal from bangsal where bangsal.kd_bangsal=?",nmgudang,kdgudang.getText());
                TCari.requestFocus();
                break;
            case KeyEvent.VK_ENTER:
                Sequel.cariIsi("select bangsal.nm_bangsal from bangsal where bangsal.kd_bangsal=?",nmgudang,kdgudang.getText());
                BtnSimpan.requestFocus();
                break;
            case KeyEvent.VK_UP:
                BtnGudangActionPerformed(null);
                break;
            default:
            break;
        }
    }//GEN-LAST:event_kdgudangKeyPressed

    private void BtnGudangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnGudangActionPerformed
        caribangsal.isCek();
        caribangsal.emptTeks();
        caribangsal.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        caribangsal.setLocationRelativeTo(internalFrame1);
        caribangsal.setAlwaysOnTop(false);
        caribangsal.setVisible(true);
    }//GEN-LAST:event_BtnGudangActionPerformed

    private void tbObatRacikanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatRacikanKeyPressed
        if(tbObatRacikan.getRowCount()!=0){
            i=tbObatRacikan.getSelectedColumn();
            if(evt.getKeyCode()==KeyEvent.VK_RIGHT){
                if(i==3 || i==5){
                    bukaDropdownRacikanFarmasi(i);
                }
            }else if(evt.getKeyCode()==KeyEvent.VK_SHIFT){
                if((i==4)){
                    TCari.requestFocus();
                }
            }else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
                if((i==6)){
                    if(tbObatRacikan.getSelectedRow()!= -1){
                        hentikanEditRacikanFarmasi();
                        setNoRacikAktifDariPilihan();
                        if(getNoRacikAktif().equals("")||
                                tbObatRacikan.getValueAt(tbObatRacikan.getSelectedRow(),1).toString().equals("")||
                                tbObatRacikan.getValueAt(tbObatRacikan.getSelectedRow(),2).toString().equals("")||
                                tbObatRacikan.getValueAt(tbObatRacikan.getSelectedRow(),3).toString().equals("")||
                                tbObatRacikan.getValueAt(tbObatRacikan.getSelectedRow(),4).toString().equals("")||
                                tbObatRacikan.getValueAt(tbObatRacikan.getSelectedRow(),5).toString().equals("")){
                            JOptionPane.showMessageDialog(null,"Silahkan lengkapi data racikan..!!");
                        }else{
                            tampildetailracikanobat();
                            TCari.requestFocus();
                        }
                    }else{
                        JOptionPane.showMessageDialog(null,"Silahkan pilih racikan..!!");
                    }
                }
            }
        }
    }//GEN-LAST:event_tbObatRacikanKeyPressed

    private void tbDetailObatRacikanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDetailObatRacikanMouseClicked
        if(tbObat.getRowCount()!=0){
            try {
                getDatadetailobatracikan();
            } catch (Exception e) {
            }
        }
    }//GEN-LAST:event_tbDetailObatRacikanMouseClicked

    private void tbDetailObatRacikanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbDetailObatRacikanKeyPressed
        if(tbObatRacikan.getSelectedRow()!= -1){
            if(tbDetailObatRacikan.getRowCount()!=0){
                i=tbDetailObatRacikan.getSelectedColumn();
                if(evt.getKeyCode()==KeyEvent.VK_ENTER){
                    try {
                        if(i==11){
                            try {
                                if(tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),11).toString().equals("0")||tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),11).toString().equals("")||tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),11).toString().equals("0.0")||tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),11).toString().equals("0,0")) {
                                    tbDetailObatRacikan.setValueAt(embalase,tbDetailObatRacikan.getSelectedRow(),11);
                                }
                            } catch (Exception e) {
                                tbDetailObatRacikan.setValueAt(0,tbDetailObatRacikan.getSelectedRow(),11);
                            }
                        }else if(i==12){
                            try {
                                if(tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),12).toString().equals("0")||tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),12).toString().equals("")||tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),12).toString().equals("0.0")||tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),12).toString().equals("0,0")) {
                                    tbDetailObatRacikan.setValueAt(tuslah,tbDetailObatRacikan.getSelectedRow(),12);
                                }
                            } catch (Exception e) {
                                tbDetailObatRacikan.setValueAt(0,tbDetailObatRacikan.getSelectedRow(),12);
                            }

                            TCari.setText("");
                            TCari.requestFocus();
                        }else if((i==9)||(i==10)){
                            try {
                                if(!tabModeDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),9).toString().equals("")){
                                    tbDetailObatRacikan.setValueAt(
                                        Valid.SetAngka8((Double.parseDouble(tbObatRacikan.getValueAt(tbObatRacikan.getSelectedRow(),4).toString())
                                            *Double.parseDouble(tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),9).toString()))
                                        /Double.parseDouble(tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),8).toString()),1)
                                    ,tbDetailObatRacikan.getSelectedRow(),10);
                                    getDatadetailobatracikan();
                                }
                            } catch (Exception e) {
                                tbDetailObatRacikan.setValueAt(0,tbDetailObatRacikan.getSelectedRow(),10);
                                tbDetailObatRacikan.setValueAt(0,tbDetailObatRacikan.getSelectedRow(),11);
                                tbDetailObatRacikan.setValueAt(0,tbDetailObatRacikan.getSelectedRow(),12);
                            }

                            TCari.setText("");
                            TCari.requestFocus();
                        }else if(i==11){
                            TCari.setText("");
                            TCari.requestFocus();
                        }
                    } catch (java.lang.NullPointerException e) {
                    }
                }else if((evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                    try {
                        if((i==9)||(i==10)){
                            if(!tabModeDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),9).toString().equals("")){
                                tbDetailObatRacikan.setValueAt(
                                    Valid.SetAngka8((Double.parseDouble(tbObatRacikan.getValueAt(tbObatRacikan.getSelectedRow(),4).toString())
                                    *Valid.SetAngka(tbDetailObatRacikan.getValueAt(row,9).toString()))
                                    /Valid.SetAngka(tbDetailObatRacikan.getValueAt(row,8).toString()),1),row,10
                                );
                                getDatadetailobatracikan();
                            }
                        }   
                    } catch (java.lang.NullPointerException e) {
                    }
                }else if(evt.getKeyCode()==KeyEvent.VK_DELETE){
                    if((i==9)||(i==10)){
                        if(tbDetailObatRacikan.getSelectedRow()!= -1){
                            tbDetailObatRacikan.setValueAt("",tbDetailObatRacikan.getSelectedRow(),9);
                            tbDetailObatRacikan.setValueAt(0,tbDetailObatRacikan.getSelectedRow(),10);
                            tbDetailObatRacikan.setValueAt(0,tbDetailObatRacikan.getSelectedRow(),11);
                            tbDetailObatRacikan.setValueAt(0,tbDetailObatRacikan.getSelectedRow(),12);
                        }
                    }

                }else if(evt.getKeyCode()==KeyEvent.VK_SHIFT){
                    if(i!=9){
                        TCari.requestFocus();
                    }
                }else if(evt.getKeyCode()==KeyEvent.VK_RIGHT){
                    if((i==9)||(i==10)){
                        try {
                            if(!tabModeDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),9).toString().equals("")){
                                tbDetailObatRacikan.setValueAt(
                                    Valid.SetAngka8((Double.parseDouble(tbObatRacikan.getValueAt(tbObatRacikan.getSelectedRow(),4).toString())
                                    *Valid.SetAngka(tbDetailObatRacikan.getValueAt(row,9).toString()))
                                    /Valid.SetAngka(tbDetailObatRacikan.getValueAt(row,8).toString()),1),row,10
                                );
                            }
                            
                            try {
                                if(tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),11).toString().equals("0")||tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),11).toString().equals("")||tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),11).toString().equals("0.0")||tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),11).toString().equals("0,0")) {
                                    tbDetailObatRacikan.setValueAt(embalase,tbDetailObatRacikan.getSelectedRow(),11);
                                }
                            } catch (Exception e) {
                                tbDetailObatRacikan.setValueAt(0,tbDetailObatRacikan.getSelectedRow(),11);
                            }

                            try {
                                if(tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),12).toString().equals("0")||tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),12).toString().equals("")||tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),12).toString().equals("0.0")||tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),12).toString().equals("0,0")) {
                                    tbDetailObatRacikan.setValueAt(tuslah,tbDetailObatRacikan.getSelectedRow(),12);
                                }
                            } catch (Exception e) {
                                tbDetailObatRacikan.setValueAt(0,tbDetailObatRacikan.getSelectedRow(),12);
                            }
                            getDatadetailobatracikan();
                        } catch (Exception e) {
                            tbDetailObatRacikan.setValueAt(0,tbDetailObatRacikan.getSelectedRow(),10);
                            tbDetailObatRacikan.setValueAt(0,tbDetailObatRacikan.getSelectedRow(),11);
                            tbDetailObatRacikan.setValueAt(0,tbDetailObatRacikan.getSelectedRow(),12);
                        }
                    }
                }
            }
        }else{
            JOptionPane.showMessageDialog(null,"Silahkan pilih No.Racikan terlebih dahulu");
        }  
    }//GEN-LAST:event_tbDetailObatRacikanKeyPressed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        if(TabRawat.getSelectedIndex()==0){
            BtnTambah1.setVisible(false);
            BtnHapus.setVisible(false);
            label13.setPreferredSize(new Dimension(65, 23));
        }else if(TabRawat.getSelectedIndex()==1){
            BtnTambah1.setVisible(true);
            BtnHapus.setVisible(true);
            label13.setPreferredSize(new Dimension(1, 23));
            setNoRacikAktifDariPilihan();
        }
    }//GEN-LAST:event_TabRawatMouseClicked

    private void BtnTambah1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTambah1ActionPerformed
        i=tabModeObatRacikan.getRowCount()+1;
        if(i==99){
            JOptionPane.showMessageDialog(null,"Maksimal 98 Racikan..!!");
        }else{
            tabModeObatRacikan.addRow(new Object[]{""+i,"Racikan "+i,"","",1,"",""});
            tbObatRacikan.setRowSelectionInterval(tbObatRacikan.getRowCount()-1, tbObatRacikan.getRowCount()-1);
            setNoRacikAktifDariPilihan();
            fokusKolomRacikanFarmasi(tbObatRacikan.getRowCount()-1,3);
        }
    }//GEN-LAST:event_BtnTambah1ActionPerformed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        hapusRacikanFarmasi();
    }//GEN-LAST:event_BtnHapusActionPerformed

    private void hapusRacikanFarmasi() {
        if(tbObatRacikan.getSelectedRow()==-1){
            JOptionPane.showMessageDialog(null,"Silahkan pilih racikan yang mau dihapus..!!");
            return;
        }
        String noRacik=tbObatRacikan.getValueAt(tbObatRacikan.getSelectedRow(),0).toString();
        boolean adaObat=false;
        for(int baris=0;baris<tabModeDetailObatRacikan.getRowCount();baris++){
            if(tabModeDetailObatRacikan.getValueAt(baris,0).toString().equals(noRacik)&&
                    Valid.SetAngka(tabModeDetailObatRacikan.getValueAt(baris,10).toString())>0){
                adaObat=true;
            }
        }
        if(adaObat){
            JOptionPane.showMessageDialog(null,"Maaf racikan sudah memiliki obat, bersihkan jumlah obatnya dulu..!!");
            return;
        }
        tampilkanSemuaDetailRacikan();
        for(int baris=tabModeDetailObatRacikan.getRowCount()-1;baris>=0;baris--){
            if(tabModeDetailObatRacikan.getValueAt(baris,0).toString().equals(noRacik)){
                tabModeDetailObatRacikan.removeRow(baris);
            }
        }
        tabModeObatRacikan.removeRow(tbObatRacikan.getSelectedRow());
        setNoRacikAktifDariPilihan();
    }

    private void tbObatPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_tbObatPropertyChange
        if(this.isVisible()==true){
            getData();
        }
    }//GEN-LAST:event_tbObatPropertyChange

    private void tbDetailObatRacikanPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_tbDetailObatRacikanPropertyChange
        if(this.isVisible()==true){
            getDatadetailobatracikan();
        }
    }//GEN-LAST:event_tbDetailObatRacikanPropertyChange

    private void ppStok1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppStok1ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        DlgCekStok ceksetok=new DlgCekStok(null,false);
        ceksetok.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        ceksetok.setLocationRelativeTo(internalFrame1);
        ceksetok.setAlwaysOnTop(false);
        ceksetok.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_ppStok1ActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgCariObat2 dialog = new DlgCariObat2(new javax.swing.JFrame(), true);
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
    private widget.Button BtnGudang;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnSeek5;
    private widget.Button BtnSimpan;
    private widget.Button BtnTambah;
    private widget.Button BtnTambah1;
    private widget.CekBox ChkJln;
    private widget.CekBox ChkNoResep;
    private widget.Tanggal DTPTgl;
    private widget.PanelBiasa FormInput;
    private widget.ComboBox Jeniskelas;
    private widget.TextBox KdPj;
    private javax.swing.JPopupMenu Popup;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll2;
    private widget.ScrollPane Scroll3;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private javax.swing.JTabbedPane TabRawat;
    private widget.ComboBox cmbDtk;
    private widget.ComboBox cmbJam;
    private widget.ComboBox cmbMnt;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel5;
    private javax.swing.JPanel jPanel3;
    private widget.TextBox kdgudang;
    private widget.TextBox kelas;
    private widget.Label label12;
    private widget.Label label13;
    private widget.Label label21;
    private widget.Label label9;
    private widget.TextBox nmgudang;
    private widget.panelisi panelisi3;
    private javax.swing.JMenuItem ppBersihkan;
    private javax.swing.JMenuItem ppStok;
    private javax.swing.JMenuItem ppStok1;
    private widget.Table tbDetailObatRacikan;
    private widget.Table tbObat;
    private widget.Table tbObatRacikan;
    // End of variables declaration//GEN-END:variables

    public void tampil() {   
        jml=0;
        for(i=0;i<tbObat.getRowCount();i++){
            if(Valid.SetAngka(tbObat.getValueAt(i,1).toString())>0){
                jml++;
            }
        }

        pilih=null;
        pilih=new boolean[jml]; 
        jumlah=null;
        jumlah=new double[jml];
        eb=null;
        eb=new double[jml];
        ts=null;
        ts=new double[jml];
        stok=null;
        stok=new double[jml];
        harga=null;
        harga=new double[jml];
        kodebarang=null;
        kodebarang=new String[jml];
        namabarang=null;
        namabarang=new String[jml];
        kodesatuan=null;
        kodesatuan=new String[jml];
        letakbarang=null;
        letakbarang=new String[jml];
        namajenis=null;                
        namajenis=new String[jml];        
        industri=null;                
        industri=new String[jml];  
        beli=null;
        beli=new double[jml];
        aturan=null;
        aturan=new String[jml];
        kategori=null;
        kategori=new String[jml];
        golongan=null;
        golongan=new String[jml];
        nobatch=new String[jml];
        nofaktur=new String[jml];
        kadaluarsa=new String[jml];
        
        jml=0;        
        for(i=0;i<tbObat.getRowCount();i++){
            if(Valid.SetAngka(tbObat.getValueAt(i,1).toString())>0){
                pilih[jml]=false;
                try {
                    jumlah[jml]=Double.parseDouble(tbObat.getValueAt(i,1).toString());
                } catch (Exception e) {
                    jumlah[jml]=0;
                }
                kodebarang[jml]=tbObat.getValueAt(i,2).toString();
                namabarang[jml]=tbObat.getValueAt(i,3).toString();
                kodesatuan[jml]=tbObat.getValueAt(i,4).toString();
                letakbarang[jml]=tbObat.getValueAt(i,5).toString();
                try {
                    harga[jml]=Double.parseDouble(tbObat.getValueAt(i,6).toString());
                } catch (Exception e) {
                    harga[jml]=0;
                }
                namajenis[jml]=tbObat.getValueAt(i,7).toString();
                try {
                    eb[jml]=Double.parseDouble(tbObat.getValueAt(i,8).toString());
                } catch (Exception e) {
                    eb[jml]=0;
                }                
                try {
                    ts[jml]=Double.parseDouble(tbObat.getValueAt(i,9).toString());
                } catch (Exception e) {
                    ts[jml]=0;
                }                
                try {
                    stok[jml]=Double.parseDouble(tbObat.getValueAt(i,10).toString());
                } catch (Exception e) {
                    stok[jml]=0;
                }
                industri[jml]=tbObat.getValueAt(i,11).toString();
                try {
                    beli[jml]=Double.parseDouble(tbObat.getValueAt(i,12).toString());
                } catch (Exception e) {
                    beli[jml]=0;
                }
                aturan[jml]=tbObat.getValueAt(i,13).toString();
                kategori[jml]=tbObat.getValueAt(i,14).toString();
                golongan[jml]=tbObat.getValueAt(i,15).toString();
                nobatch[jml]=tbObat.getValueAt(i,16).toString();
                nofaktur[jml]=tbObat.getValueAt(i,17).toString();
                try {
                    kadaluarsa[jml]=tbObat.getValueAt(i,18).toString();
                } catch (Exception e) {
                    kadaluarsa[jml]="0000-00-00";
                }
                jml++;
            }
        }
        
        Valid.tabelKosong(tabMode);
        
        for(i=0;i<jml;i++){
            tabMode.addRow(new Object[] {pilih[i],jumlah[i],kodebarang[i],namabarang[i],
               kodesatuan[i],letakbarang[i],harga[i],namajenis[i],eb[i],ts[i],stok[i],industri[i],
               beli[i],aturan[i],kategori[i],golongan[i],nobatch[i],nofaktur[i],kadaluarsa[i]
            });
        }
                
        try{
            if(kenaikan>0){
                if(aktifkanbatch.equals("yes")){
                    if(aktifpcare.equals("yes")){
                        sql="select data_batch.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,(data_batch.h_beli+(data_batch.h_beli*?)) as harga,"+
                            " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,kategori_barang.nama as kategori,golongan_barang.nama as golongan,"+
                            " data_batch.no_batch,data_batch.no_faktur,data_batch.tgl_kadaluarsa,gudangbarang.stok,data_batch."+hppfarmasi+" as dasar "+
                            " from data_batch inner join databarang on data_batch.kode_brng=databarang.kode_brng "+
                            " inner join jenis on databarang.kdjns=jenis.kdjns "+
                            " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                            " inner join golongan_barang on databarang.kode_golongan=golongan_barang.kode "+
                            " inner join kategori_barang on databarang.kode_kategori=kategori_barang.kode "+
                            " inner join maping_obat_pcare on maping_obat_pcare.kode_brng=databarang.kode_brng "+
                            " inner join gudangbarang on gudangbarang.kode_brng=data_batch.kode_brng and gudangbarang.no_batch=data_batch.no_batch and gudangbarang.no_faktur=data_batch.no_faktur ";
                    }else{
                        sql="select data_batch.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,(data_batch.h_beli+(data_batch.h_beli*?)) as harga,"+
                            " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,kategori_barang.nama as kategori,golongan_barang.nama as golongan,"+
                            " data_batch.no_batch,data_batch.no_faktur,data_batch.tgl_kadaluarsa,gudangbarang.stok,data_batch."+hppfarmasi+" as dasar "+
                            " from data_batch inner join databarang on data_batch.kode_brng=databarang.kode_brng "+
                            " inner join jenis on databarang.kdjns=jenis.kdjns "+
                            " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                            " inner join golongan_barang on databarang.kode_golongan=golongan_barang.kode "+
                            " inner join kategori_barang on databarang.kode_kategori=kategori_barang.kode "+
                            " inner join gudangbarang on gudangbarang.kode_brng=data_batch.kode_brng and gudangbarang.no_batch=data_batch.no_batch and gudangbarang.no_faktur=data_batch.no_faktur ";
                    }
                    psobat=koneksi.prepareStatement(
                        sql+" where gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.kode_brng like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.nama_brng like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and kategori_barang.nama like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and golongan_barang.nama like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and data_batch.no_batch like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and data_batch.no_faktur like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and jenis.nama like ? order by data_batch.tgl_kadaluarsa asc");
                    try{
                        psobat.setDouble(1,kenaikan);
                        psobat.setString(2,kdgudang.getText());
                        psobat.setString(3,"%"+TCari.getText().trim()+"%");
                        psobat.setString(4,kdgudang.getText());
                        psobat.setString(5,"%"+TCari.getText().trim()+"%");
                        psobat.setString(6,kdgudang.getText());
                        psobat.setString(7,"%"+TCari.getText().trim()+"%");
                        psobat.setString(8,kdgudang.getText());
                        psobat.setString(9,"%"+TCari.getText().trim()+"%");
                        psobat.setString(10,kdgudang.getText());
                        psobat.setString(11,"%"+TCari.getText().trim()+"%");
                        psobat.setString(12,kdgudang.getText());
                        psobat.setString(13,"%"+TCari.getText().trim()+"%");
                        psobat.setString(14,kdgudang.getText());
                        psobat.setString(15,"%"+TCari.getText().trim()+"%");
                        rsobat=psobat.executeQuery();
                        while(rsobat.next()){
                            tabMode.addRow(new Object[] {false,"",rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("harga"),100),                                   
                                rsobat.getString("nama"),0,0,rsobat.getDouble("stok"),rsobat.getString("nama_industri"),
                                rsobat.getDouble("dasar"),"",rsobat.getString("kategori"),rsobat.getString("golongan"),
                                rsobat.getString("no_batch"),rsobat.getString("no_faktur"),rsobat.getString("tgl_kadaluarsa")
                            });          
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : "+e);
                    } finally{
                        if(rsobat != null){
                            rsobat.close();
                        }
                        if(psobat != null){
                            psobat.close();
                        }
                    }
                }else{
                    if(aktifpcare.equals("yes")){
                        sql="select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,(databarang.h_beli+(databarang.h_beli*?)) as harga,"+
                            " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,kategori_barang.nama as kategori,gudangbarang.stok,golongan_barang.nama as golongan,databarang."+hppfarmasi+" as dasar "+
                            " from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                            " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                            " inner join golongan_barang on databarang.kode_golongan=golongan_barang.kode "+
                            " inner join kategori_barang on databarang.kode_kategori=kategori_barang.kode "+
                            " inner join maping_obat_pcare on maping_obat_pcare.kode_brng=databarang.kode_brng "+
                            " inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng ";
                    }else{
                        sql="select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,(databarang.h_beli+(databarang.h_beli*?)) as harga,"+
                            " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,kategori_barang.nama as kategori,gudangbarang.stok,golongan_barang.nama as golongan,databarang."+hppfarmasi+" as dasar "+
                            " from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                            " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                            " inner join golongan_barang on databarang.kode_golongan=golongan_barang.kode "+
                            " inner join kategori_barang on databarang.kode_kategori=kategori_barang.kode "+
                            " inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng ";
                    }
                    psobat=koneksi.prepareStatement(
                        sql+" where gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.status='1' and databarang.kode_brng like ? or "+
                        " gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.status='1' and databarang.nama_brng like ? or "+
                        " gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.status='1' and kategori_barang.nama like ? or "+
                        " gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.status='1' and golongan_barang.nama like ? or "+
                        " gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.status='1' and jenis.nama like ? order by databarang.nama_brng");
                    try{
                        psobat.setDouble(1,kenaikan);
                        psobat.setString(2,kdgudang.getText());
                        psobat.setString(3,"%"+TCari.getText().trim()+"%");
                        psobat.setString(4,kdgudang.getText());
                        psobat.setString(5,"%"+TCari.getText().trim()+"%");
                        psobat.setString(6,kdgudang.getText());
                        psobat.setString(7,"%"+TCari.getText().trim()+"%");
                        psobat.setString(8,kdgudang.getText());
                        psobat.setString(9,"%"+TCari.getText().trim()+"%");
                        psobat.setString(10,kdgudang.getText());
                        psobat.setString(11,"%"+TCari.getText().trim()+"%");
                        rsobat=psobat.executeQuery();
                        while(rsobat.next()){
                                tabMode.addRow(new Object[] {false,"",rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("harga"),100),                                   
                                   rsobat.getString("nama"),0,0,rsobat.getDouble("stok"),rsobat.getString("nama_industri"),
                                   rsobat.getDouble("dasar"),"",rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                });          
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : "+e);
                    } finally{
                        if(rsobat != null){
                            rsobat.close();
                        }
                        if(psobat != null){
                            psobat.close();
                        }
                    }
                }                                                         
            }else{
                if(aktifkanbatch.equals("yes")){
                    if(aktifpcare.equals("yes")){
                        sql="select data_batch.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,data_batch.kelas1,"+
                            " data_batch.kelas2,data_batch.kelas3,data_batch.utama,data_batch.vip,data_batch.vvip,data_batch.beliluar,data_batch.karyawan,"+
                            " databarang.letak_barang,industrifarmasi.nama_industri,data_batch.h_beli,kategori_barang.nama as kategori,golongan_barang.nama as golongan, "+
                            " data_batch.no_batch,data_batch.no_faktur,data_batch.tgl_kadaluarsa,gudangbarang.stok,data_batch."+hppfarmasi+" as dasar "+
                            " from data_batch inner join databarang on data_batch.kode_brng=databarang.kode_brng "+
                            " inner join jenis on databarang.kdjns=jenis.kdjns "+
                            " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                            " inner join golongan_barang on databarang.kode_golongan=golongan_barang.kode "+
                            " inner join kategori_barang on databarang.kode_kategori=kategori_barang.kode "+
                            " inner join maping_obat_pcare on maping_obat_pcare.kode_brng=databarang.kode_brng "+
                            " inner join gudangbarang on gudangbarang.kode_brng=data_batch.kode_brng and gudangbarang.no_batch=data_batch.no_batch and gudangbarang.no_faktur=data_batch.no_faktur ";
                    }else{
                        sql="select data_batch.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,data_batch.kelas1,"+
                            " data_batch.kelas2,data_batch.kelas3,data_batch.utama,data_batch.vip,data_batch.vvip,data_batch.beliluar,data_batch.karyawan,"+
                            " databarang.letak_barang,industrifarmasi.nama_industri,data_batch.h_beli,kategori_barang.nama as kategori,golongan_barang.nama as golongan, "+
                            " data_batch.no_batch,data_batch.no_faktur,data_batch.tgl_kadaluarsa,gudangbarang.stok,data_batch."+hppfarmasi+" as dasar "+
                            " from data_batch inner join databarang on data_batch.kode_brng=databarang.kode_brng "+
                            " inner join jenis on databarang.kdjns=jenis.kdjns "+
                            " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                            " inner join golongan_barang on databarang.kode_golongan=golongan_barang.kode "+
                            " inner join kategori_barang on databarang.kode_kategori=kategori_barang.kode "+
                            " inner join gudangbarang on gudangbarang.kode_brng=data_batch.kode_brng and gudangbarang.no_batch=data_batch.no_batch and gudangbarang.no_faktur=data_batch.no_faktur ";                        
                    }
                    psobat=koneksi.prepareStatement(
                        sql+" where gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.kode_brng like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.nama_brng like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and kategori_barang.nama like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and golongan_barang.nama like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and data_batch.no_batch like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and data_batch.no_faktur like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and jenis.nama like ? order by data_batch.tgl_kadaluarsa asc");
                    try{
                        psobat.setString(1,kdgudang.getText());
                        psobat.setString(2,"%"+TCari.getText().trim()+"%");
                        psobat.setString(3,kdgudang.getText());
                        psobat.setString(4,"%"+TCari.getText().trim()+"%");
                        psobat.setString(5,kdgudang.getText());
                        psobat.setString(6,"%"+TCari.getText().trim()+"%");
                        psobat.setString(7,kdgudang.getText());
                        psobat.setString(8,"%"+TCari.getText().trim()+"%");
                        psobat.setString(9,kdgudang.getText());
                        psobat.setString(10,"%"+TCari.getText().trim()+"%");
                        psobat.setString(11,kdgudang.getText());
                        psobat.setString(12,"%"+TCari.getText().trim()+"%");
                        psobat.setString(13,kdgudang.getText());
                        psobat.setString(14,"%"+TCari.getText().trim()+"%");
                        rsobat=psobat.executeQuery();
                        if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                            while(rsobat.next()){
                                tabMode.addRow(new Object[] {false,"",rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas1"),100),
                                   rsobat.getString("nama"),0,0,rsobat.getDouble("stok"),rsobat.getString("nama_industri"),
                                   rsobat.getDouble("dasar"),"",rsobat.getString("kategori"),rsobat.getString("golongan"),
                                   rsobat.getString("no_batch"),rsobat.getString("no_faktur"),rsobat.getString("tgl_kadaluarsa")
                                });
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                            while(rsobat.next()){
                                tabMode.addRow(new Object[] {false,"",rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas2"),100),
                                   rsobat.getString("nama"),0,0,rsobat.getDouble("stok"),rsobat.getString("nama_industri"),
                                   rsobat.getDouble("dasar"),"",rsobat.getString("kategori"),rsobat.getString("golongan"),
                                   rsobat.getString("no_batch"),rsobat.getString("no_faktur"),rsobat.getString("tgl_kadaluarsa")
                                });
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                            while(rsobat.next()){
                                tabMode.addRow(new Object[] {false,"",rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas3"),100),
                                   rsobat.getString("nama"),0,0,rsobat.getDouble("stok"),rsobat.getString("nama_industri"),
                                   rsobat.getDouble("dasar"),"",rsobat.getString("kategori"),rsobat.getString("golongan"),
                                   rsobat.getString("no_batch"),rsobat.getString("no_faktur"),rsobat.getString("tgl_kadaluarsa")
                                });
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                            while(rsobat.next()){
                                tabMode.addRow(new Object[] {false,"",rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("utama"),100),
                                   rsobat.getString("nama"),0,0,rsobat.getDouble("stok"),rsobat.getString("nama_industri"),
                                   rsobat.getDouble("dasar"),"",rsobat.getString("kategori"),rsobat.getString("golongan"),
                                   rsobat.getString("no_batch"),rsobat.getString("no_faktur"),rsobat.getString("tgl_kadaluarsa")
                                });
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                            while(rsobat.next()){
                                tabMode.addRow(new Object[] {false,"",rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("vip"),100),
                                   rsobat.getString("nama"),0,0,rsobat.getDouble("stok"),rsobat.getString("nama_industri"),
                                   rsobat.getDouble("dasar"),"",rsobat.getString("kategori"),rsobat.getString("golongan"),
                                   rsobat.getString("no_batch"),rsobat.getString("no_faktur"),rsobat.getString("tgl_kadaluarsa")
                                });
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                            while(rsobat.next()){
                                tabMode.addRow(new Object[] {false,"",rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("vvip"),100),
                                   rsobat.getString("nama"),0,0,rsobat.getDouble("stok"),rsobat.getString("nama_industri"),
                                   rsobat.getDouble("dasar"),"",rsobat.getString("kategori"),rsobat.getString("golongan"),
                                   rsobat.getString("no_batch"),rsobat.getString("no_faktur"),rsobat.getString("tgl_kadaluarsa")
                                });
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                            while(rsobat.next()){
                                tabMode.addRow(new Object[] {false,"",rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                    rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("beliluar"),100),
                                    rsobat.getString("nama"),0,0,rsobat.getDouble("stok"),rsobat.getString("nama_industri"),
                                    rsobat.getDouble("dasar"),"",rsobat.getString("kategori"),rsobat.getString("golongan"),
                                    rsobat.getString("no_batch"),rsobat.getString("no_faktur"),rsobat.getString("tgl_kadaluarsa")
                                });
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                            while(rsobat.next()){
                                tabMode.addRow(new Object[] {false,"",rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                    rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("karyawan"),100),
                                    rsobat.getString("nama"),0,0,rsobat.getDouble("stok"),rsobat.getString("nama_industri"),
                                    rsobat.getDouble("dasar"),"",rsobat.getString("kategori"),rsobat.getString("golongan"),
                                    rsobat.getString("no_batch"),rsobat.getString("no_faktur"),rsobat.getString("tgl_kadaluarsa")
                                });
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : "+e);
                    } finally{
                        if(rsobat != null){
                            rsobat.close();
                        }
                        if(psobat != null){
                            psobat.close();
                        }
                    }
                }else{
                    if(aktifpcare.equals("yes")){
                        sql="select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,databarang.kelas1,"+
                            " databarang.kelas2,databarang.kelas3,databarang.utama,databarang.vip,databarang.vvip,databarang.beliluar,databarang.karyawan,gudangbarang.stok,"+
                            " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,kategori_barang.nama as kategori,golongan_barang.nama as golongan,databarang."+hppfarmasi+" as dasar "+
                            " from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                            " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                            " inner join golongan_barang on databarang.kode_golongan=golongan_barang.kode "+
                            " inner join kategori_barang on databarang.kode_kategori=kategori_barang.kode "+
                            " inner join maping_obat_pcare on maping_obat_pcare.kode_brng=databarang.kode_brng "+
                            " inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng ";
                    }else{
                        sql="select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,databarang.kelas1,"+
                            " databarang.kelas2,databarang.kelas3,databarang.utama,databarang.vip,databarang.vvip,databarang.beliluar,databarang.karyawan,gudangbarang.stok,"+
                            " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,kategori_barang.nama as kategori,golongan_barang.nama as golongan,databarang."+hppfarmasi+" as dasar "+
                            " from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                            " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                            " inner join golongan_barang on databarang.kode_golongan=golongan_barang.kode "+
                            " inner join kategori_barang on databarang.kode_kategori=kategori_barang.kode "+
                            " inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng ";
                    }
                    psobat=koneksi.prepareStatement(
                        sql+" where gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.status='1' and databarang.kode_brng like ? or "+
                        " gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.status='1' and databarang.nama_brng like ? or "+
                        " gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.status='1' and kategori_barang.nama like ? or "+
                        " gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.status='1' and golongan_barang.nama like ? or "+
                        " gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.status='1' and jenis.nama like ? order by databarang.nama_brng");
                    try{
                        psobat.setString(1,kdgudang.getText());
                        psobat.setString(2,"%"+TCari.getText().trim()+"%");
                        psobat.setString(3,kdgudang.getText());
                        psobat.setString(4,"%"+TCari.getText().trim()+"%");
                        psobat.setString(5,kdgudang.getText());
                        psobat.setString(6,"%"+TCari.getText().trim()+"%");
                        psobat.setString(7,kdgudang.getText());
                        psobat.setString(8,"%"+TCari.getText().trim()+"%");
                        psobat.setString(9,kdgudang.getText());
                        psobat.setString(10,"%"+TCari.getText().trim()+"%");
                        rsobat=psobat.executeQuery();
                        if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                            while(rsobat.next()){
                                tabMode.addRow(new Object[] {false,"",rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                           rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas1"),100),
                                           rsobat.getString("nama"),0,0,rsobat.getDouble("stok"),rsobat.getString("nama_industri"),
                                           rsobat.getDouble("dasar"),"",rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                });
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                            while(rsobat.next()){
                                tabMode.addRow(new Object[] {false,"",rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                           rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas2"),100),
                                           rsobat.getString("nama"),0,0,rsobat.getDouble("stok"),rsobat.getString("nama_industri"),
                                           rsobat.getDouble("dasar"),"",rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                });
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                            while(rsobat.next()){
                                tabMode.addRow(new Object[] {false,"",rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                           rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas3"),100),
                                           rsobat.getString("nama"),0,0,rsobat.getDouble("stok"),rsobat.getString("nama_industri"),
                                           rsobat.getDouble("dasar"),"",rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                });
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                            while(rsobat.next()){
                                tabMode.addRow(new Object[] {false,"",rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                           rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("utama"),100),
                                           rsobat.getString("nama"),0,0,rsobat.getDouble("stok"),rsobat.getString("nama_industri"),
                                           rsobat.getDouble("dasar"),"",rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                });
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                            while(rsobat.next()){
                                tabMode.addRow(new Object[] {false,"",rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                           rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("vip"),100),
                                           rsobat.getString("nama"),0,0,rsobat.getDouble("stok"),rsobat.getString("nama_industri"),
                                           rsobat.getDouble("dasar"),"",rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                });
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                            while(rsobat.next()){
                                tabMode.addRow(new Object[] {false,"",rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                           rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("vvip"),100),
                                           rsobat.getString("nama"),0,0,rsobat.getDouble("stok"),rsobat.getString("nama_industri"),
                                           rsobat.getDouble("dasar"),"",rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                });
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                            while(rsobat.next()){
                                tabMode.addRow(new Object[] {false,"",rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                           rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("beliluar"),100),
                                           rsobat.getString("nama"),0,0,rsobat.getDouble("stok"),rsobat.getString("nama_industri"),
                                           rsobat.getDouble("dasar"),"",rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                });
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                            while(rsobat.next()){
                                tabMode.addRow(new Object[] {false,"",rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                           rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("karyawan"),100),
                                           rsobat.getString("nama"),0,0,rsobat.getDouble("stok"),rsobat.getString("nama_industri"),
                                           rsobat.getDouble("dasar"),"",rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                });
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : "+e);
                    } finally{
                        if(rsobat != null){
                            rsobat.close();
                        }
                        if(psobat != null){
                            psobat.close();
                        }
                    }
                }
            }              
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }

    public void emptTeks() {
        TCari.requestFocus();
    }

    private void getData() {
        if(nmgudang.getText().trim().equals("")){
            Valid.textKosong(kdgudang,"Lokasi");
        }else{
            if(tbObat.getSelectedRow()!= -1){
                row=tbObat.getSelectedRow();
                if(!tbObat.getValueAt(row,1).toString().equals("")){
                    try {
                        if(Double.parseDouble(tbObat.getValueAt(row,1).toString())>0){
                            stokbarang=0;  
                            if(aktifkanbatch.equals("yes")){
                                psstok=koneksi.prepareStatement("select ifnull(gudangbarang.stok,'0') from gudangbarang where gudangbarang.kd_bangsal=? and gudangbarang.kode_brng=? and gudangbarang.no_batch=? and gudangbarang.no_faktur=?");
                                try {
                                    psstok.setString(1,kdgudang.getText());
                                    psstok.setString(2,tbObat.getValueAt(row,2).toString());
                                    psstok.setString(3,tbObat.getValueAt(row,16).toString());
                                    psstok.setString(4,tbObat.getValueAt(row,17).toString());
                                    rsstok=psstok.executeQuery();
                                    if(rsstok.next()){
                                        stokbarang=rsstok.getDouble(1);
                                    }                                
                                } catch (Exception e) {
                                    stokbarang=0;
                                    System.out.println("Notifikasi : "+e);
                                }finally{
                                    if(rsstok != null){
                                        rsstok.close();
                                    }
                                    if(psstok != null){
                                        psstok.close();
                                    }
                                }
                            }else{
                                psstok=koneksi.prepareStatement("select ifnull(gudangbarang.stok,'0') from gudangbarang where gudangbarang.kd_bangsal=? and gudangbarang.kode_brng=? and gudangbarang.no_batch='' and gudangbarang.no_faktur=''");
                                try {
                                    psstok.setString(1,kdgudang.getText());
                                    psstok.setString(2,tbObat.getValueAt(row,2).toString());
                                    rsstok=psstok.executeQuery();
                                    if(rsstok.next()){
                                        stokbarang=rsstok.getDouble(1);
                                    }                                
                                } catch (Exception e) {
                                    stokbarang=0;
                                    System.out.println("Notifikasi : "+e);
                                }finally{
                                    if(rsstok != null){
                                        rsstok.close();
                                    }
                                    if(psstok != null){
                                        psstok.close();
                                    }
                                }
                            }

                            tbObat.setValueAt(stokbarang,row,10);

                            y=0;
                            try {
                                if(tbObat.getValueAt(row,0).toString().equals("true")){
                                    pscarikapasitas= koneksi.prepareStatement("select IFNULL(databarang.kapasitas,1) from databarang where databarang.kode_brng=?");                                      
                                    try {
                                        pscarikapasitas.setString(1,tbObat.getValueAt(row,2).toString());
                                        carikapasitas=pscarikapasitas.executeQuery();
                                        if(carikapasitas.next()){ 
                                            y=Double.parseDouble(tbObat.getValueAt(row,1).toString())/carikapasitas.getDouble(1);
                                        }else{
                                            y=Double.parseDouble(tbObat.getValueAt(row,1).toString());
                                        }
                                    } catch (Exception e) {
                                        y=Double.parseDouble(tbObat.getValueAt(row,1).toString());
                                        System.out.println("Kapasitasmu masih kosong broooh : "+e);
                                    } finally{
                                        if(carikapasitas!=null){
                                            carikapasitas.close();
                                        }
                                        if(pscarikapasitas!=null){
                                            pscarikapasitas.close();
                                        }
                                    }
                                }else{
                                    y=Double.parseDouble(tbObat.getValueAt(row,1).toString());
                                }                        
                            } catch (Exception e) {
                                y=0;
                            }

                            stokbarang=0;
                            try {
                                stokbarang=Double.parseDouble(tbObat.getValueAt(row,10).toString());
                            } catch (Exception e) {
                                stokbarang=0;
                            }

                            if(stokbarang<y){
                                JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                tbObat.setValueAt("",row,1);
                            }
                        }
                        if((tbObat.getSelectedColumn()==16)||(tbObat.getSelectedColumn()==17)){
                            cariBatch();   
                            //getData2();
                        }
                    } catch (Exception e) {
                        tbObat.setValueAt("",row,1);
                        tbObat.setValueAt(0,row,10);
                    }
                }else{
                    tbObat.setValueAt(0,row,10);
                }   
            }
        }            
    }
    
    private void getDataobat(int data) {        
        try {              
            stokbarang=0;  
            if(aktifkanbatch.equals("yes")){
                psstok=koneksi.prepareStatement("select ifnull(gudangbarang.stok,'0') from gudangbarang where gudangbarang.kd_bangsal=? and gudangbarang.kode_brng=? and gudangbarang.no_batch=? and gudangbarang.no_faktur=?");
                try {
                    psstok.setString(1,kdgudang.getText());
                    psstok.setString(2,tbObat.getValueAt(data,2).toString());
                    psstok.setString(3,tbObat.getValueAt(data,16).toString());
                    psstok.setString(4,tbObat.getValueAt(data,17).toString());
                    rsstok=psstok.executeQuery();
                    if(rsstok.next()){
                        stokbarang=rsstok.getDouble(1);
                    }                                
                } catch (Exception e) {
                    stokbarang=0;
                    System.out.println("Notifikasi : "+e);
                }finally{
                    if(rsstok != null){
                        rsstok.close();
                    }
                    if(psstok != null){
                        psstok.close();
                    }
                }
            }else{
                psstok=koneksi.prepareStatement("select ifnull(gudangbarang.stok,'0') from gudangbarang where gudangbarang.kd_bangsal=? and gudangbarang.kode_brng=? and gudangbarang.no_batch='' and gudangbarang.no_faktur=''");
                try {
                    psstok.setString(1,kdgudang.getText());
                    psstok.setString(2,tbObat.getValueAt(data,2).toString());
                    rsstok=psstok.executeQuery();
                    if(rsstok.next()){
                        stokbarang=rsstok.getDouble(1);
                    }                                
                } catch (Exception e) {
                    stokbarang=0;
                    System.out.println("Notifikasi : "+e);
                }finally{
                    if(rsstok != null){
                        rsstok.close();
                    }
                    if(psstok != null){
                        psstok.close();
                    }
                }
            }

            tbObat.setValueAt(stokbarang,data,10);

            y=0;
            try {
                if(tbObat.getValueAt(data,0).toString().equals("true")){
                    pscarikapasitas= koneksi.prepareStatement("select IFNULL(databarang.kapasitas,1) from databarang where databarang.kode_brng=?");                                      
                    try {
                        pscarikapasitas.setString(1,tbObat.getValueAt(data,2).toString());
                        carikapasitas=pscarikapasitas.executeQuery();
                        if(carikapasitas.next()){ 
                            y=Double.parseDouble(tbObat.getValueAt(data,1).toString())/carikapasitas.getDouble(1);
                        }else{
                            y=Double.parseDouble(tbObat.getValueAt(data,1).toString());
                        }
                    } catch (Exception e) {
                        y=Double.parseDouble(tbObat.getValueAt(data,1).toString());
                        System.out.println("Kapasitasmu masih kosong broooh : "+e);
                    } finally{
                        if(carikapasitas!=null){
                            carikapasitas.close();
                        }
                        if(pscarikapasitas!=null){
                            pscarikapasitas.close();
                        }
                    }
                }else{
                    y=Double.parseDouble(tbObat.getValueAt(data,1).toString());
                }                        
            } catch (Exception e) {
                y=0;
            }
            if(stokbarang<y){
                JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
            }
        } catch (Exception e) {
            tbObat.setValueAt(0,data,10);
        } 
    }

    public JTable getTable(){
        return tbObat;
    }
    
    public void isCek(){  
        if(!DEPOAKTIFOBAT.equals("")){
            kdgudang.setText(DEPOAKTIFOBAT);
            nmgudang.setText(caribangsal.tampil3(DEPOAKTIFOBAT));
        }else{
            kdgudang.setText(akses.getkdbangsal());
            if(akses.getkdbangsal().equals("")){
                kdgudang.setText(bangsaldefault);
            } 
            nmgudang.setText(caribangsal.tampil3(kdgudang.getText()));
        }
             
        BtnTambah.setEnabled(akses.getobat());
        TCari.requestFocus();
        BtnGudang.setEnabled(akses.getakses_depo_obat());
    }
    
    public void setNoRm(String norwt,String norm,String pasien,Date tanggal,String jam,String menit,String detik,boolean status) {        
        aktifpcare="no";
        TNoRw.setText(norwt);
        TNoRM.setText(norm);
        TPasien.setText(pasien);
        DTPTgl.setDate(tanggal);
        cmbJam.setSelectedItem(jam);
        cmbMnt.setSelectedItem(menit);
        cmbDtk.setSelectedItem(detik);
        ChkJln.setSelected(status);
        this.noresep="";
        KdPj.setText(Sequel.cariIsi("select reg_periksa.kd_pj from reg_periksa where reg_periksa.no_rawat=?",norwt));
        kelas.setText(Sequel.cariIsi(
                "select kamar.kelas from kamar inner join kamar_inap on kamar.kd_kamar=kamar_inap.kd_kamar "+
                "where kamar_inap.no_rawat=? and kamar_inap.stts_pulang='-' order by STR_TO_DATE(concat(kamar_inap.tgl_masuk,' ',kamar_inap.jam_masuk),'%Y-%m-%d %H:%i:%s') desc limit 1",norwt));
        if(kelas.getText().equals("Kelas 1")){
            Jeniskelas.setSelectedItem("Kelas 1");
        }else if(kelas.getText().equals("Kelas 2")){
            Jeniskelas.setSelectedItem("Kelas 2");
        }else if(kelas.getText().equals("Kelas 3")){
            Jeniskelas.setSelectedItem("Kelas 3");
        }else if(kelas.getText().equals("Kelas Utama")){
            Jeniskelas.setSelectedItem("Utama/BPJS");
        }else if(kelas.getText().equals("Kelas VIP")){
            Jeniskelas.setSelectedItem("VIP");
        }else if(kelas.getText().equals("Kelas VVIP")){
            Jeniskelas.setSelectedItem("VVIP");
        }        
        kenaikan=Sequel.cariIsiAngka("select (set_harga_obat_ranap.hargajual/100) from set_harga_obat_ranap where set_harga_obat_ranap.kd_pj='"+KdPj.getText()+"' and set_harga_obat_ranap.kelas='"+kelas.getText()+"'");
        tampilkanPlanSOAPTerakhir(norwt);
        tampilkanTindakanPasienRanap(norwt);
        TCari.requestFocus();
    } 
    
    public void setNoRm(String norwt,String norm,String pasien,Date tanggal) {       
        aktifpcare="no";
        TNoRw.setText(norwt);
        TNoRM.setText(norm);
        TPasien.setText(pasien);
        tampilkanCatatanResepDokter("");
        DTPTgl.setDate(tanggal);
        ChkJln.setSelected(true);
        KdPj.setText(Sequel.cariIsi("select reg_periksa.kd_pj from reg_periksa where reg_periksa.no_rawat=?",norwt));
        kelas.setText(Sequel.cariIsi(
                "select kamar.kelas from kamar inner join kamar_inap on kamar.kd_kamar=kamar_inap.kd_kamar "+
                "where kamar_inap.no_rawat=? and kamar_inap.stts_pulang='-' order by STR_TO_DATE(concat(kamar_inap.tgl_masuk,' ',kamar_inap.jam_masuk),'%Y-%m-%d %H:%i:%s') desc limit 1",norwt));
        if(kelas.getText().equals("Kelas 1")){
            Jeniskelas.setSelectedItem("Kelas 1");
        }else if(kelas.getText().equals("Kelas 2")){
            Jeniskelas.setSelectedItem("Kelas 2");
        }else if(kelas.getText().equals("Kelas 3")){
            Jeniskelas.setSelectedItem("Kelas 3");
        }else if(kelas.getText().equals("Kelas Utama")){
            Jeniskelas.setSelectedItem("Utama/BPJS");
        }else if(kelas.getText().equals("Kelas VIP")){
            Jeniskelas.setSelectedItem("VIP");
        }else if(kelas.getText().equals("Kelas VVIP")){
            Jeniskelas.setSelectedItem("VVIP");
        }        
        kenaikan=Sequel.cariIsiAngka("select (set_harga_obat_ranap.hargajual/100) from set_harga_obat_ranap where set_harga_obat_ranap.kd_pj='"+KdPj.getText()+"' and set_harga_obat_ranap.kelas='"+kelas.getText()+"'");
        tampilkanPlanSOAPTerakhir(norwt);
        tampilkanTindakanPasienRanap(norwt);
        TCari.requestFocus();
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
        aturKolomTindakanPasien(tbTindakanPasien);
        aturKolomTindakanPasien(tbTindakanPasienRacikan);

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

        panelTindakanPasien=new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelTindakanPasien.setName("panelTindakanPasien");
        panelTindakanPasien.setOpaque(false);
        panelTindakanPasien.setPreferredSize(new java.awt.Dimension(460,100));
        panelTindakanPasien.add(labelTindakanPasien, java.awt.BorderLayout.PAGE_START);
        panelTindakanPasien.add(scrollTindakanPasien, java.awt.BorderLayout.CENTER);
        panelTindakanPasienRacikan=new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelTindakanPasienRacikan.setName("panelTindakanPasienRacikan");
        panelTindakanPasienRacikan.setOpaque(false);
        panelTindakanPasienRacikan.setPreferredSize(new java.awt.Dimension(460,100));
        panelTindakanPasienRacikan.add(labelTindakanPasienRacikan, java.awt.BorderLayout.PAGE_START);
        panelTindakanPasienRacikan.add(scrollTindakanPasienRacikan, java.awt.BorderLayout.CENTER);

        panelPlanSOAP = new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelPlanSOAP.setName("panelPlanSOAP");
        panelPlanSOAP.setOpaque(false);
        panelPlanSOAP.setPreferredSize(new java.awt.Dimension(460,100));
        panelPlanSOAPRacikan = new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelPlanSOAPRacikan.setName("panelPlanSOAPRacikan");
        panelPlanSOAPRacikan.setOpaque(false);
        panelPlanSOAPRacikan.setPreferredSize(new java.awt.Dimension(460,100));
        panelCatatanDokter = new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelCatatanDokter.setName("panelCatatanDokter");
        panelCatatanDokter.setOpaque(false);
        panelCatatanDokter.setPreferredSize(new java.awt.Dimension(230,260));
        panelCatatanDokterRacikan = new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelCatatanDokterRacikan.setName("panelCatatanDokterRacikan");
        panelCatatanDokterRacikan.setOpaque(false);
        panelCatatanDokterRacikan.setPreferredSize(new java.awt.Dimension(230,260));

        labelPlanSOAP = new widget.Label();
        labelPlanSOAP.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelPlanSOAP.setText("Plan SOAP Dokter :");
        labelPlanSOAP.setPreferredSize(new java.awt.Dimension(100,23));
        labelPlanSOAPRacikan = new widget.Label();
        labelPlanSOAPRacikan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelPlanSOAPRacikan.setText("Plan SOAP Dokter :");
        labelPlanSOAPRacikan.setPreferredSize(new java.awt.Dimension(100,23));
        labelCatatanDokter = new widget.Label();
        labelCatatanDokter.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelCatatanDokter.setText("Catatan Dokter :");
        labelCatatanDokter.setPreferredSize(new java.awt.Dimension(100,23));
        labelCatatanDokterRacikan = new widget.Label();
        labelCatatanDokterRacikan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelCatatanDokterRacikan.setText("Catatan Dokter :");
        labelCatatanDokterRacikan.setPreferredSize(new java.awt.Dimension(100,23));

        areaPlanSOAP = new javax.swing.JTextArea();
        areaPlanSOAP.setEditable(false);
        areaPlanSOAP.setLineWrap(true);
        areaPlanSOAP.setWrapStyleWord(true);
        areaPlanSOAP.setMargin(new java.awt.Insets(6,6,6,6));
        areaPlanSOAP.setName("areaPlanSOAP");
        areaPlanSOAPRacikan = new javax.swing.JTextArea();
        areaPlanSOAPRacikan.setEditable(false);
        areaPlanSOAPRacikan.setLineWrap(true);
        areaPlanSOAPRacikan.setWrapStyleWord(true);
        areaPlanSOAPRacikan.setMargin(new java.awt.Insets(6,6,6,6));
        areaPlanSOAPRacikan.setName("areaPlanSOAPRacikan");
        areaCatatanDokter = new javax.swing.JTextArea();
        areaCatatanDokter.setEditable(false);
        areaCatatanDokter.setLineWrap(true);
        areaCatatanDokter.setWrapStyleWord(true);
        areaCatatanDokter.setMargin(new java.awt.Insets(6,6,6,6));
        areaCatatanDokter.setName("areaCatatanDokter");
        areaCatatanDokterRacikan = new javax.swing.JTextArea();
        areaCatatanDokterRacikan.setEditable(false);
        areaCatatanDokterRacikan.setLineWrap(true);
        areaCatatanDokterRacikan.setWrapStyleWord(true);
        areaCatatanDokterRacikan.setMargin(new java.awt.Insets(6,6,6,6));
        areaCatatanDokterRacikan.setName("areaCatatanDokterRacikan");

        scrollPlanSOAP = new widget.ScrollPane();
        scrollPlanSOAP.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255,255,255)));
        scrollPlanSOAP.setName("scrollPlanSOAP");
        scrollPlanSOAP.setViewportView(areaPlanSOAP);
        scrollPlanSOAPRacikan = new widget.ScrollPane();
        scrollPlanSOAPRacikan.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255,255,255)));
        scrollPlanSOAPRacikan.setName("scrollPlanSOAPRacikan");
        scrollPlanSOAPRacikan.setViewportView(areaPlanSOAPRacikan);
        scrollCatatanDokter = new widget.ScrollPane();
        scrollCatatanDokter.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255,255,255)));
        scrollCatatanDokter.setName("scrollCatatanDokter");
        scrollCatatanDokter.setPreferredSize(new java.awt.Dimension(230,235));
        scrollCatatanDokter.setViewportView(areaCatatanDokter);
        scrollCatatanDokterRacikan = new widget.ScrollPane();
        scrollCatatanDokterRacikan.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255,255,255)));
        scrollCatatanDokterRacikan.setName("scrollCatatanDokterRacikan");
        scrollCatatanDokterRacikan.setPreferredSize(new java.awt.Dimension(230,235));
        scrollCatatanDokterRacikan.setViewportView(areaCatatanDokterRacikan);

        javax.swing.JPanel panelIsiPlanSOAP=new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelIsiPlanSOAP.setName("panelIsiPlanSOAP");
        panelIsiPlanSOAP.setOpaque(false);
        panelIsiPlanSOAP.add(labelPlanSOAP, java.awt.BorderLayout.PAGE_START);
        panelIsiPlanSOAP.add(scrollPlanSOAP, java.awt.BorderLayout.CENTER);
        panelCatatanDokter.add(labelCatatanDokter, java.awt.BorderLayout.PAGE_START);
        panelCatatanDokter.add(scrollCatatanDokter, java.awt.BorderLayout.CENTER);
        javax.swing.JSplitPane splitInfoSOAP=new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT,panelIsiPlanSOAP,panelTindakanPasien);
        splitInfoSOAP.setName("splitInfoSOAP");
        splitInfoSOAP.setBorder(null);
        splitInfoSOAP.setResizeWeight(0.78);
        splitInfoSOAP.setDividerLocation(260);
        panelPlanSOAP.add(splitInfoSOAP, java.awt.BorderLayout.CENTER);
        panelPlanSOAP.add(panelCatatanDokter, java.awt.BorderLayout.PAGE_END);

        javax.swing.JPanel panelIsiPlanSOAPRacikan=new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelIsiPlanSOAPRacikan.setName("panelIsiPlanSOAPRacikan");
        panelIsiPlanSOAPRacikan.setOpaque(false);
        panelIsiPlanSOAPRacikan.add(labelPlanSOAPRacikan, java.awt.BorderLayout.PAGE_START);
        panelIsiPlanSOAPRacikan.add(scrollPlanSOAPRacikan, java.awt.BorderLayout.CENTER);
        panelCatatanDokterRacikan.add(labelCatatanDokterRacikan, java.awt.BorderLayout.PAGE_START);
        panelCatatanDokterRacikan.add(scrollCatatanDokterRacikan, java.awt.BorderLayout.CENTER);
        javax.swing.JSplitPane splitInfoSOAPRacikan=new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT,panelIsiPlanSOAPRacikan,panelTindakanPasienRacikan);
        splitInfoSOAPRacikan.setName("splitInfoSOAPRacikan");
        splitInfoSOAPRacikan.setBorder(null);
        splitInfoSOAPRacikan.setResizeWeight(0.78);
        splitInfoSOAPRacikan.setDividerLocation(260);
        panelPlanSOAPRacikan.add(splitInfoSOAPRacikan, java.awt.BorderLayout.CENTER);
        panelPlanSOAPRacikan.add(panelCatatanDokterRacikan, java.awt.BorderLayout.PAGE_END);

        panelUmumObat = new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelUmumObat.setName("panelUmumObat");
        panelUmumObat.setOpaque(false);
        int indexUmum = TabRawat.indexOfComponent(Scroll);
        if(indexUmum>-1) {
            TabRawat.remove(indexUmum);
            panelUmumObat.add(Scroll, java.awt.BorderLayout.CENTER);
            panelUmumObat.add(panelPlanSOAP, java.awt.BorderLayout.EAST);
            TabRawat.insertTab("Umum", null, panelUmumObat, null, indexUmum);
        }

        panelRacikanObat = new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelRacikanObat.setName("panelRacikanObat");
        panelRacikanObat.setOpaque(false);
        int indexRacikan = TabRawat.indexOfComponent(jPanel3);
        if(indexRacikan>-1) {
            pasangPanelDetailRacikanFarmasi(Scroll3);
            TabRawat.remove(indexRacikan);
            panelRacikanObat.add(jPanel3, java.awt.BorderLayout.CENTER);
            panelRacikanObat.add(panelPlanSOAPRacikan, java.awt.BorderLayout.EAST);
            TabRawat.insertTab("Racikan / V2", null, panelRacikanObat, null, indexRacikan);
        }
        TabRawat.setSelectedIndex(0);
        refreshPanelPlanSOAP();
        refreshPanelCatatanDokter();
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

    private void pasangPanelDetailRacikanFarmasi(widget.ScrollPane scrollDetail) {
        if(labelDetailRacikanFarmasi!=null){
            return;
        }
        labelDetailRacikanFarmasi = new widget.Label();
        labelDetailRacikanFarmasi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelDetailRacikanFarmasi.setPreferredSize(new java.awt.Dimension(100,23));

        javax.swing.JPanel panelDetail=new javax.swing.JPanel(new java.awt.BorderLayout(1,1));
        panelDetail.setName("panelDetailRacikanFarmasi");
        panelDetail.setOpaque(false);
        panelDetail.add(labelDetailRacikanFarmasi, java.awt.BorderLayout.PAGE_START);
        panelDetail.add(scrollDetail, java.awt.BorderLayout.CENTER);
        jPanel3.remove(scrollDetail);
        jPanel3.add(panelDetail, java.awt.BorderLayout.CENTER);
        tampilkanLabelDetailRacikanFarmasi();
    }

    private void pasangEditorRacikanFarmasi() {
        muatPilihanMetodeRacikFarmasi();
        muatPilihanAturanPakaiFarmasi();

        javax.swing.JComboBox<String> cmbMetode=new javax.swing.JComboBox<String>();
        cmbMetode.addItem("");
        for(String[] metode:pilihanMetodeRacikFarmasi){
            cmbMetode.addItem(metode[1]);
        }
        tbObatRacikan.getColumnModel().getColumn(3).setCellEditor(new javax.swing.DefaultCellEditor(cmbMetode));

        javax.swing.JComboBox<String> cmbAturan=new javax.swing.JComboBox<String>();
        cmbAturan.setEditable(true);
        cmbAturan.addItem("");
        for(String aturan:pilihanAturanPakaiFarmasi){
            cmbAturan.addItem(aturan);
        }
        tbObatRacikan.getColumnModel().getColumn(5).setCellEditor(new javax.swing.DefaultCellEditor(cmbAturan));
    }

    private void muatPilihanMetodeRacikFarmasi() {
        pilihanMetodeRacikFarmasi.clear();
        try(PreparedStatement psMetode=koneksi.prepareStatement("select kd_racik,nm_racik from metode_racik order by nm_racik");
            ResultSet rsMetode=psMetode.executeQuery()){
            while(rsMetode.next()){
                pilihanMetodeRacikFarmasi.add(new String[]{rsMetode.getString("kd_racik"),rsMetode.getString("nm_racik")});
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Metode Racik Farmasi DlgCariObat2 : "+e);
        }
    }

    private void muatPilihanAturanPakaiFarmasi() {
        pilihanAturanPakaiFarmasi.clear();
        try(PreparedStatement psAturan=koneksi.prepareStatement("select aturan from master_aturan_pakai order by aturan");
            ResultSet rsAturan=psAturan.executeQuery()){
            while(rsAturan.next()){
                pilihanAturanPakaiFarmasi.add(rsAturan.getString("aturan"));
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Aturan Pakai Farmasi DlgCariObat2 : "+e);
        }
    }

    private void isiKodeMetodeRacikFarmasi(int row) {
        if(row<0 || row>=tbObatRacikan.getRowCount()){
            return;
        }
        String metode=tbObatRacikan.getValueAt(row,3).toString();
        for(String[] pilihan:pilihanMetodeRacikFarmasi){
            if(pilihan[1].equals(metode)){
                tbObatRacikan.setValueAt(pilihan[0],row,2);
                return;
            }
        }
        tbObatRacikan.setValueAt("",row,2);
    }

    private void hentikanEditRacikanFarmasi() {
        try {
            if(tbObatRacikan.isEditing()){
                tbObatRacikan.getCellEditor().stopCellEditing();
            }
            if(tbDetailObatRacikan.isEditing()){
                tbDetailObatRacikan.getCellEditor().stopCellEditing();
            }
        } catch (Exception e) {
        }
    }

    private void bukaDropdownRacikanFarmasi(int kolom) {
        try {
            int row=tbObatRacikan.getSelectedRow();
            if(row==-1){
                return;
            }
            tbObatRacikan.requestFocus();
            tbObatRacikan.setColumnSelectionInterval(kolom,kolom);
            tbObatRacikan.editCellAt(row,kolom);
            if(tbObatRacikan.getEditorComponent()!=null){
                tbObatRacikan.getEditorComponent().requestFocus();
                if(tbObatRacikan.getEditorComponent() instanceof javax.swing.JComboBox){
                    ((javax.swing.JComboBox)tbObatRacikan.getEditorComponent()).showPopup();
                }
            }
        } catch (Exception e) {
        }
    }

    private void tampilkanTindakanPasienRanap(String noRawat) {
        if(tabModeTindakan==null){
            return;
        }
        Valid.tabelKosong(tabModeTindakan);
        if(noRawat==null || noRawat.trim().equals("")){
            return;
        }

        String sql=
            "select rawat_inap_dr.tgl_perawatan,rawat_inap_dr.jam_rawat,'Dokter' as jenis,rawat_inap_dr.kd_jenis_prw,jns_perawatan_inap.nm_perawatan,dokter.nm_dokter as pelaksana,rawat_inap_dr.biaya_rawat "+
            "from rawat_inap_dr inner join jns_perawatan_inap on rawat_inap_dr.kd_jenis_prw=jns_perawatan_inap.kd_jenis_prw inner join dokter on rawat_inap_dr.kd_dokter=dokter.kd_dokter where rawat_inap_dr.no_rawat=? "+
            "union all "+
            "select rawat_inap_pr.tgl_perawatan,rawat_inap_pr.jam_rawat,'Petugas' as jenis,rawat_inap_pr.kd_jenis_prw,jns_perawatan_inap.nm_perawatan,petugas.nama as pelaksana,rawat_inap_pr.biaya_rawat "+
            "from rawat_inap_pr inner join jns_perawatan_inap on rawat_inap_pr.kd_jenis_prw=jns_perawatan_inap.kd_jenis_prw inner join petugas on rawat_inap_pr.nip=petugas.nip where rawat_inap_pr.no_rawat=? "+
            "union all "+
            "select rawat_inap_drpr.tgl_perawatan,rawat_inap_drpr.jam_rawat,'Dokter & Petugas' as jenis,rawat_inap_drpr.kd_jenis_prw,jns_perawatan_inap.nm_perawatan,concat(dokter.nm_dokter,' / ',petugas.nama) as pelaksana,rawat_inap_drpr.biaya_rawat "+
            "from rawat_inap_drpr inner join jns_perawatan_inap on rawat_inap_drpr.kd_jenis_prw=jns_perawatan_inap.kd_jenis_prw inner join dokter on rawat_inap_drpr.kd_dokter=dokter.kd_dokter inner join petugas on rawat_inap_drpr.nip=petugas.nip where rawat_inap_drpr.no_rawat=? "+
            "order by tgl_perawatan,jam_rawat,jenis,nm_perawatan";

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
            System.out.println("Notifikasi Tindakan Pasien DlgCariObat2 : "+e);
        }
    }

    private void tampilkanPlanSOAPTerakhir(String noRawat) {
        planSOAPTerakhir="";
        try {
            if(noRawat!=null && !noRawat.trim().equals("")) {
                pastikanTabelDraftResepSOAP();
                bersihkanDraftSOAPNonDokter(noRawat);
                planSOAPTerakhir=Sequel.cariIsi(
                    "select ifnull(permintaan_resep_soap.resep_teks,'') from permintaan_resep_soap " +
                    "inner join dokter on permintaan_resep_soap.kd_dokter=dokter.kd_dokter " +
                    "where permintaan_resep_soap.no_rawat=? and permintaan_resep_soap.status='Belum Terlayani' " +
                    "order by permintaan_resep_soap.tgl_permintaan desc,permintaan_resep_soap.jam_permintaan desc limit 1",
                    noRawat
                );
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Plan SOAP DlgCariObat2 : "+e);
        }
        refreshPanelPlanSOAP();
    }

    private void refreshPanelPlanSOAP() {
        String isi = planSOAPTerakhir.trim().equals("") ? "Belum ada Plan SOAP dokter untuk kunjungan ini." : planSOAPTerakhir;
        if(areaPlanSOAP!=null) {
            areaPlanSOAP.setText(isi);
            areaPlanSOAP.setCaretPosition(0);
        }
        if(areaPlanSOAPRacikan!=null) {
            areaPlanSOAPRacikan.setText(isi);
            areaPlanSOAPRacikan.setCaretPosition(0);
        }
    }

    private void tampilkanCatatanResepDokter(String noResep) {
        catatanResepDokter="";
        try {
            if(noResep!=null && !noResep.trim().equals("")) {
                pastikanTabelCatatanResepDokter();
                catatanResepDokter=Sequel.cariIsi("select catatan from catatan_resep_dokter where no_resep=?",noResep);
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Catatan Resep Dokter DlgCariObat2 : "+e);
        }
        refreshPanelCatatanDokter();
    }

    private void refreshPanelCatatanDokter() {
        String isi = catatanResepDokter.trim().equals("") ? "Tidak ada catatan dokter untuk resep ini." : catatanResepDokter;
        if(areaCatatanDokter!=null) {
            areaCatatanDokter.setText(isi);
            areaCatatanDokter.setCaretPosition(0);
        }
        if(areaCatatanDokterRacikan!=null) {
            areaCatatanDokterRacikan.setText(isi);
            areaCatatanDokterRacikan.setCaretPosition(0);
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
            System.out.println("Notifikasi Plan SOAP DlgCariObat2 : "+e);
        }
    }

    private void pastikanTabelCatatanResepDokter() {
        if(catatanResepDokterTableChecked){
            return;
        }
        try {
            Sequel.queryu(
                "create table if not exists catatan_resep_dokter ("+
                "no_resep varchar(14) not null,"+
                "catatan text,"+
                "tanggal_update datetime not null,"+
                "primary key (no_resep)) engine=InnoDB default charset=latin1");
            catatanResepDokterTableChecked=true;
        } catch (Exception e) {
            System.out.println("Notifikasi Catatan Resep Dokter DlgCariObat2 : "+e);
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
            System.out.println("Notifikasi Plan SOAP DlgCariObat2 : "+e);
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
    
    private void getDatadetailobatracikan() {
        if(tbDetailObatRacikan.getSelectedRow()!= -1){
            row=tbDetailObatRacikan.getSelectedRow();
            try {
                if(Valid.SetAngka(tbDetailObatRacikan.getValueAt(row,10).toString())>0){
                    stokbarang=0;  
                    if(aktifkanbatch.equals("yes")){
                        psstok=koneksi.prepareStatement("select ifnull(gudangbarang.stok,'0') from gudangbarang where gudangbarang.kd_bangsal=? and gudangbarang.kode_brng=? and gudangbarang.no_batch=? and gudangbarang.no_faktur=?");
                        try {
                            psstok.setString(1,kdgudang.getText());
                            psstok.setString(2,tbDetailObatRacikan.getValueAt(row,1).toString());
                            psstok.setString(3,tbDetailObatRacikan.getValueAt(row,16).toString());
                            psstok.setString(4,tbDetailObatRacikan.getValueAt(row,17).toString());
                            rsstok=psstok.executeQuery();
                            if(rsstok.next()){
                                stokbarang=rsstok.getDouble(1);
                            }                                
                        } catch (Exception e) {
                            stokbarang=0;
                            System.out.println("Notifikasi : "+e);
                        }finally{
                            if(rsstok != null){
                                rsstok.close();
                            }
                            if(psstok != null){
                                psstok.close();
                            }
                        }
                    }else{
                        psstok=koneksi.prepareStatement("select ifnull(gudangbarang.stok,'0') from gudangbarang where gudangbarang.kd_bangsal=? and gudangbarang.kode_brng=? and gudangbarang.no_batch='' and gudangbarang.no_faktur=''");
                        try {
                            psstok.setString(1,kdgudang.getText());
                            psstok.setString(2,tbDetailObatRacikan.getValueAt(row,1).toString());
                            rsstok=psstok.executeQuery();
                            if(rsstok.next()){
                                stokbarang=rsstok.getDouble(1);
                            }                                
                        } catch (Exception e) {
                            stokbarang=0;
                            System.out.println("Notifikasi : "+e);
                        }finally{
                            if(rsstok != null){
                                rsstok.close();
                            }
                            if(psstok != null){
                                psstok.close();
                            }
                        }
                    }

                    tbDetailObatRacikan.setValueAt(stokbarang,row,7);
                    
                    y=0;
                    try {
                        y=Double.parseDouble(tbDetailObatRacikan.getValueAt(row,10).toString());
                    } catch (Exception e) {
                        y=0;
                    }
                    
                    stokbarang=0;
                    try {
                        stokbarang=Double.parseDouble(tbDetailObatRacikan.getValueAt(row,7).toString());
                    } catch (Exception e) {
                        stokbarang=0;
                    }

                    if(stokbarang<y){
                        JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                        tbDetailObatRacikan.setValueAt("",row,9);
                        tbDetailObatRacikan.setValueAt(0,row,10);
                        tbDetailObatRacikan.setValueAt(0,row,11);
                        tbDetailObatRacikan.setValueAt(0,row,12);
                    }
                }    
                if((tbDetailObatRacikan.getSelectedColumn()==16)||(tbDetailObatRacikan.getSelectedColumn()==17)){
                    cariBatch();   
                    //getData2();
                }
            } catch (Exception e) {
                System.out.println("Notif Racikan : "+e);
                tbDetailObatRacikan.setValueAt(0,row,10);
                tbDetailObatRacikan.setValueAt(0,row,11);
                tbDetailObatRacikan.setValueAt(0,row,12);
            }   
        }
    }

    private void setNoRacikAktifDariPilihan(){
        if(tbObatRacikan.getSelectedRow()!=-1 && tbObatRacikan.getValueAt(tbObatRacikan.getSelectedRow(),0)!=null){
            noRacikAktif=tbObatRacikan.getValueAt(tbObatRacikan.getSelectedRow(),0).toString();
        }else{
            noRacikAktif="";
        }
        tampilkanDetailRacikanAktifFarmasi();
        tampilkanLabelDetailRacikanFarmasi();
    }

    private String getNoRacikAktif(){
        if(noRacikAktif.equals("")){
            setNoRacikAktifDariPilihan();
        }
        return noRacikAktif;
    }

    private void tampilkanLabelDetailRacikanFarmasi() {
        if(labelDetailRacikanFarmasi==null){
            return;
        }
        String label="Informasi obat untuk racikan: pilih/isi racikan terlebih dahulu";
        try {
            if(tbObatRacikan.getSelectedRow()!=-1){
                String no=tbObatRacikan.getValueAt(tbObatRacikan.getSelectedRow(),0).toString();
                String nama=tbObatRacikan.getValueAt(tbObatRacikan.getSelectedRow(),1).toString();
                String metode=tbObatRacikan.getValueAt(tbObatRacikan.getSelectedRow(),3).toString();
                label="Informasi obat untuk racikan: "+no+". "+(nama.equals("") ? "Racikan" : nama);
                if(!metode.equals("")){
                    label=label+" ("+metode+")";
                }
            }
        } catch (Exception e) {
        }
        labelDetailRacikanFarmasi.setText(label);
    }

    private void tampilkanDetailRacikanAktifFarmasi() {
        try {
            final String noRacik=noRacikAktif;
            javax.swing.table.TableRowSorter sorter=(javax.swing.table.TableRowSorter)tbDetailObatRacikan.getRowSorter();
            if(sorter==null){
                sorter=new javax.swing.table.TableRowSorter(tabModeDetailObatRacikan);
                tbDetailObatRacikan.setRowSorter(sorter);
            }
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
                        return entry.getStringValue(0).equals(noRacik);
                    }
                });
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Filter Detail Racikan DlgCariObat2 : "+e);
        }
    }

    private void tampilkanSemuaDetailRacikan() {
        try {
            javax.swing.table.TableRowSorter sorter=(javax.swing.table.TableRowSorter)tbDetailObatRacikan.getRowSorter();
            if(sorter!=null){
                sorter.setRowFilter(null);
            }
        } catch (Exception e) {
        }
    }

    private void fokusKolomRacikanFarmasi(int row,int kolom) {
        try {
            if(row<0 || row>=tbObatRacikan.getRowCount()){
                return;
            }
            tbObatRacikan.requestFocus();
            tbObatRacikan.setRowSelectionInterval(row,row);
            tbObatRacikan.setColumnSelectionInterval(kolom,kolom);
            tbObatRacikan.editCellAt(row,kolom);
            if(tbObatRacikan.getEditorComponent()!=null){
                tbObatRacikan.getEditorComponent().requestFocus();
                if(tbObatRacikan.getEditorComponent() instanceof javax.swing.JComboBox){
                    ((javax.swing.JComboBox)tbObatRacikan.getEditorComponent()).showPopup();
                }
            }
        } catch (Exception e) {
        }
    }

    public void tampildetailracikanobat() {        
        tampilkanSemuaDetailRacikan();
        z=0;
        for(i=0;i<tbDetailObatRacikan.getRowCount();i++){
            if(Valid.SetAngka(tbDetailObatRacikan.getValueAt(i,10).toString())>0){
                z++;
            }
        }    
        
        pilih=null;
        pilih=new boolean[z]; 
        jumlah=null;
        jumlah=new double[z];
        harga=null;
        harga=new double[z];
        eb=null;
        eb=new double[z];
        ts=null;
        ts=new double[z];
        stok=null;
        stok=new double[z];
        kodebarang=null;
        kodebarang=new String[z];
        namabarang=null;
        namabarang=new String[z];
        kodesatuan=null;
        kodesatuan=new String[z];
        letakbarang=null;
        letakbarang=new String[z];
        no=null;
        no=new String[z];
        namajenis=null;
        namajenis=new String[z];        
        industri=null;
        industri=new String[z];         
        beli=null;
        beli=new double[z]; 
        kategori=null;
        kategori=new String[z];
        golongan=null;
        golongan=new String[z];        
        kapasitas=null;
        kapasitas=new double[z];   
        kandungan=null;
        kandungan=new double[z];
        nobatch=new String[z];
        nofaktur=new String[z];
        kadaluarsa=new String[z];
        z=0;        
        for(i=0;i<tbDetailObatRacikan.getRowCount();i++){
            if(Valid.SetAngka(tbDetailObatRacikan.getValueAt(i,10).toString())>0){
                no[z]=tbDetailObatRacikan.getValueAt(i,0).toString();
                kodebarang[z]=tbDetailObatRacikan.getValueAt(i,1).toString();
                namabarang[z]=tbDetailObatRacikan.getValueAt(i,2).toString();
                kodesatuan[z]=tbDetailObatRacikan.getValueAt(i,3).toString();
                try {
                    harga[z]=Double.parseDouble(tbDetailObatRacikan.getValueAt(i,4).toString());
                } catch (Exception e) {
                    harga[z]=0;
                }
                try {
                    beli[z]=Double.parseDouble(tbDetailObatRacikan.getValueAt(i,5).toString());
                } catch (Exception e) {
                    beli[z]=0;
                }
                namajenis[z]=tbDetailObatRacikan.getValueAt(i,6).toString();
                try {
                    stok[z]=Double.parseDouble(tbDetailObatRacikan.getValueAt(i,7).toString());
                } catch (Exception e) {
                    stok[z]=0;
                }                
                try {
                    kapasitas[z]=Double.parseDouble(tbDetailObatRacikan.getValueAt(i,8).toString());
                } catch (Exception e) {
                    kapasitas[z]=0;
                }                
                try {
                    kandungan[z]=Double.parseDouble(tbDetailObatRacikan.getValueAt(i,9).toString());
                } catch (Exception e) {
                    kandungan[z]=0;
                }                
                try {
                    jumlah[z]=Double.parseDouble(tbDetailObatRacikan.getValueAt(i,10).toString());
                } catch (Exception e) {
                    jumlah[z]=0;
                }                 
                try {
                    eb[z]=Double.parseDouble(tbDetailObatRacikan.getValueAt(i,11).toString());
                } catch (Exception e) {
                    eb[z]=0;
                } 
                try {
                    ts[z]=Double.parseDouble(tbDetailObatRacikan.getValueAt(i,12).toString());
                } catch (Exception e) {
                    ts[z]=0;
                }                                 
                industri[z]=tbDetailObatRacikan.getValueAt(i,13).toString();
                kategori[z]=tbDetailObatRacikan.getValueAt(i,14).toString();
                golongan[z]=tbDetailObatRacikan.getValueAt(i,15).toString();
                nobatch[z]=tbDetailObatRacikan.getValueAt(i,16).toString();
                nofaktur[z]=tbDetailObatRacikan.getValueAt(i,17).toString();
                try {
                    kadaluarsa[z]=tbObat.getValueAt(i,18).toString();
                } catch (Exception e) {
                    kadaluarsa[z]="0000-00-00";
                }
                z++;
            }
        }
        
        Valid.tabelKosong(tabModeDetailObatRacikan);             
        
        for(i=0;i<z;i++){
            tabModeDetailObatRacikan.addRow(new Object[] {
                no[i],kodebarang[i],namabarang[i],kodesatuan[i],harga[i],beli[i],
                namajenis[i],stok[i],kapasitas[i],kandungan[i],jumlah[i],eb[i],
                ts[i],industri[i],kategori[i],golongan[i],nobatch[i],nofaktur[i],kadaluarsa[i]
            });
        }
        
        try {
            if(kenaikan>0){
                if(aktifkanbatch.equals("yes")){
                    if(aktifpcare.equals("yes")){
                        sql="select data_batch.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,(data_batch.h_beli+(data_batch.h_beli*?)) as harga,"+
                            " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,kategori_barang.nama as kategori,golongan_barang.nama as golongan,databarang.kapasitas, "+
                            " data_batch.no_batch,data_batch.no_faktur,data_batch.tgl_kadaluarsa,gudangbarang.stok,data_batch."+hppfarmasi+" as dasar "+
                            " from data_batch inner join databarang on data_batch.kode_brng=databarang.kode_brng "+
                            " inner join jenis on databarang.kdjns=jenis.kdjns "+
                            " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                            " inner join golongan_barang on databarang.kode_golongan=golongan_barang.kode "+
                            " inner join kategori_barang on databarang.kode_kategori=kategori_barang.kode "+
                            " inner join maping_obat_pcare on maping_obat_pcare.kode_brng=databarang.kode_brng "+
                            " inner join gudangbarang on gudangbarang.kode_brng=data_batch.kode_brng and gudangbarang.no_batch=data_batch.no_batch and gudangbarang.no_faktur=data_batch.no_faktur ";
                    }else{
                        sql="select data_batch.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,(data_batch.h_beli+(data_batch.h_beli*?)) as harga,"+
                            " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,kategori_barang.nama as kategori,golongan_barang.nama as golongan,databarang.kapasitas, "+
                            " data_batch.no_batch,data_batch.no_faktur,data_batch.tgl_kadaluarsa,gudangbarang.stok,data_batch."+hppfarmasi+" as dasar "+
                            " from data_batch inner join databarang on data_batch.kode_brng=databarang.kode_brng "+
                            " inner join jenis on databarang.kdjns=jenis.kdjns "+
                            " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                            " inner join golongan_barang on databarang.kode_golongan=golongan_barang.kode "+
                            " inner join kategori_barang on databarang.kode_kategori=kategori_barang.kode "+
                            " inner join gudangbarang on gudangbarang.kode_brng=data_batch.kode_brng and gudangbarang.no_batch=data_batch.no_batch and gudangbarang.no_faktur=data_batch.no_faktur ";
                    }
                    psobat=koneksi.prepareStatement(
                        sql+" where gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.kode_brng like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.nama_brng like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and kategori_barang.nama like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and golongan_barang.nama like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and data_batch.no_batch like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and data_batch.no_faktur like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and jenis.nama like ? order by data_batch.tgl_kadaluarsa asc");
                    try {
                        psobat.setDouble(1,kenaikan);
                        psobat.setString(2,kdgudang.getText());
                        psobat.setString(3,"%"+TCari.getText().trim()+"%");
                        psobat.setString(4,kdgudang.getText());
                        psobat.setString(5,"%"+TCari.getText().trim()+"%");
                        psobat.setString(6,kdgudang.getText());
                        psobat.setString(7,"%"+TCari.getText().trim()+"%");
                        psobat.setString(8,kdgudang.getText());
                        psobat.setString(9,"%"+TCari.getText().trim()+"%");
                        psobat.setString(10,kdgudang.getText());
                        psobat.setString(11,"%"+TCari.getText().trim()+"%");
                        psobat.setString(12,kdgudang.getText());
                        psobat.setString(13,"%"+TCari.getText().trim()+"%");
                        psobat.setString(14,kdgudang.getText());
                        psobat.setString(15,"%"+TCari.getText().trim()+"%");
                        rsobat=psobat.executeQuery();
                        while(rsobat.next()){
                            tabModeDetailObatRacikan.addRow(new Object[] {
                                getNoRacikAktif(),
                                rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                rsobat.getString("kode_sat"),Valid.roundUp(rsobat.getDouble("harga"),100),
                                rsobat.getDouble("dasar"),rsobat.getString("nama"),rsobat.getDouble("stok"),
                                rsobat.getDouble("kapasitas"),"",0,0,0,rsobat.getString("nama_industri"),
                                rsobat.getString("kategori"),rsobat.getString("golongan"),
                                rsobat.getString("no_batch"),rsobat.getString("no_faktur"),rsobat.getString("tgl_kadaluarsa")
                            });          
                        } 
                    }catch(Exception e){
                        System.out.println("Notifikasi : "+e);
                    }finally{
                        if(rsobat != null){
                            rsobat.close();
                        }
                        if(psobat != null){
                            psobat.close();
                        }
                    }
                }else{
                    if(aktifpcare.equals("yes")){
                        sql="select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,(databarang.h_beli+(databarang.h_beli*?)) as harga,databarang."+hppfarmasi+" as dasar,gudangbarang.stok,"+
                            " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,kategori_barang.nama as kategori,golongan_barang.nama as golongan,databarang.kapasitas "+
                            " from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                            " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                            " inner join golongan_barang on databarang.kode_golongan=golongan_barang.kode "+
                            " inner join kategori_barang on databarang.kode_kategori=kategori_barang.kode "+
                            " inner join maping_obat_pcare on maping_obat_pcare.kode_brng=databarang.kode_brng "+
                            " inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng ";
                    }else{
                        sql="select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,(databarang.h_beli+(databarang.h_beli*?)) as harga,databarang."+hppfarmasi+" as dasar,gudangbarang.stok,"+
                            " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,kategori_barang.nama as kategori,golongan_barang.nama as golongan,databarang.kapasitas "+
                            " from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                            " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                            " inner join golongan_barang on databarang.kode_golongan=golongan_barang.kode "+
                            " inner join kategori_barang on databarang.kode_kategori=kategori_barang.kode "+
                            " inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng ";
                    }
                    psobat=koneksi.prepareStatement(
                        sql+" where gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.status='1' and databarang.kode_brng like ? or "+
                        " gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.status='1' and databarang.nama_brng like ? or "+
                        " gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.status='1' and kategori_barang.nama like ? or "+
                        " gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.status='1' and golongan_barang.nama like ? or "+
                        " gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.status='1' and jenis.nama like ? order by databarang.nama_brng");
                    try {
                        psobat.setDouble(1,kenaikan);
                        psobat.setString(2,kdgudang.getText());
                        psobat.setString(3,"%"+TCari.getText().trim()+"%");
                        psobat.setString(4,kdgudang.getText());
                        psobat.setString(5,"%"+TCari.getText().trim()+"%");
                        psobat.setString(6,kdgudang.getText());
                        psobat.setString(7,"%"+TCari.getText().trim()+"%");
                        psobat.setString(8,kdgudang.getText());
                        psobat.setString(9,"%"+TCari.getText().trim()+"%");
                        psobat.setString(10,kdgudang.getText());
                        psobat.setString(11,"%"+TCari.getText().trim()+"%");
                        rsobat=psobat.executeQuery();
                        while(rsobat.next()){
                            tabModeDetailObatRacikan.addRow(new Object[] {
                                getNoRacikAktif(),
                                rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                rsobat.getString("kode_sat"),Valid.roundUp(rsobat.getDouble("harga"),100),
                                rsobat.getDouble("dasar"),rsobat.getString("nama"),rsobat.getDouble("stok"),
                                rsobat.getDouble("kapasitas"),"",0,0,0,rsobat.getString("nama_industri"),
                                rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                            });          
                        } 
                    }catch(Exception e){
                        System.out.println("Notifikasi : "+e);
                    }finally{
                        if(rsobat != null){
                            rsobat.close();
                        }
                        if(psobat != null){
                            psobat.close();
                        }
                    }
                }
            }else{
                if(aktifkanbatch.equals("yes")){
                    if(aktifpcare.equals("yes")){
                        sql="select data_batch.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,data_batch.kelas1,data_batch.kelas2,"+
                            " data_batch.kelas3,data_batch.utama,data_batch.vip,data_batch.vvip,data_batch.beliluar,data_batch.karyawan,"+
                            " databarang.letak_barang,industrifarmasi.nama_industri,data_batch.h_beli,kategori_barang.nama as kategori,golongan_barang.nama as golongan,databarang.kapasitas,  "+
                            " data_batch.no_batch,data_batch.no_faktur,data_batch.tgl_kadaluarsa,gudangbarang.stok,data_batch."+hppfarmasi+" as dasar "+
                            " from data_batch inner join databarang on data_batch.kode_brng=databarang.kode_brng "+
                            " inner join jenis on databarang.kdjns=jenis.kdjns "+
                            " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                            " inner join golongan_barang on databarang.kode_golongan=golongan_barang.kode "+
                            " inner join kategori_barang on databarang.kode_kategori=kategori_barang.kode "+
                            " inner join maping_obat_pcare on maping_obat_pcare.kode_brng=databarang.kode_brng "+
                            " inner join gudangbarang on gudangbarang.kode_brng=data_batch.kode_brng and gudangbarang.no_batch=data_batch.no_batch and gudangbarang.no_faktur=data_batch.no_faktur ";
                    }else{
                        sql="select data_batch.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,data_batch.kelas1,data_batch.kelas2,"+
                            " data_batch.kelas3,data_batch.utama,data_batch.vip,data_batch.vvip,data_batch.beliluar,data_batch.karyawan,"+
                            " databarang.letak_barang,industrifarmasi.nama_industri,data_batch.h_beli,kategori_barang.nama as kategori,golongan_barang.nama as golongan,databarang.kapasitas,  "+
                            " data_batch.no_batch,data_batch.no_faktur,data_batch.tgl_kadaluarsa,gudangbarang.stok,data_batch."+hppfarmasi+" as dasar "+
                            " from data_batch inner join databarang on data_batch.kode_brng=databarang.kode_brng "+
                            " inner join jenis on databarang.kdjns=jenis.kdjns "+
                            " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                            " inner join golongan_barang on databarang.kode_golongan=golongan_barang.kode "+
                            " inner join kategori_barang on databarang.kode_kategori=kategori_barang.kode "+
                            " inner join gudangbarang on gudangbarang.kode_brng=data_batch.kode_brng and gudangbarang.no_batch=data_batch.no_batch and gudangbarang.no_faktur=data_batch.no_faktur ";
                    }
                    psobat=koneksi.prepareStatement(
                        sql+" where gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.kode_brng like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.nama_brng like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and kategori_barang.nama like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and golongan_barang.nama like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and data_batch.no_batch like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and data_batch.no_faktur like ? or "+
                        " gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and jenis.nama like ? order by data_batch.tgl_kadaluarsa");
                    try{    
                        psobat.setString(1,kdgudang.getText());
                        psobat.setString(2,"%"+TCari.getText().trim()+"%");
                        psobat.setString(3,kdgudang.getText());
                        psobat.setString(4,"%"+TCari.getText().trim()+"%");
                        psobat.setString(5,kdgudang.getText());
                        psobat.setString(6,"%"+TCari.getText().trim()+"%");
                        psobat.setString(7,kdgudang.getText());
                        psobat.setString(8,"%"+TCari.getText().trim()+"%");
                        psobat.setString(9,kdgudang.getText());
                        psobat.setString(10,"%"+TCari.getText().trim()+"%");
                        psobat.setString(11,kdgudang.getText());
                        psobat.setString(12,"%"+TCari.getText().trim()+"%");
                        psobat.setString(13,kdgudang.getText());
                        psobat.setString(14,"%"+TCari.getText().trim()+"%");
                        rsobat=psobat.executeQuery();
                        if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                            while(rsobat.next()){
                                tabModeDetailObatRacikan.addRow(new Object[] {
                                    getNoRacikAktif(),
                                    rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                    rsobat.getString("kode_sat"),Valid.roundUp(rsobat.getDouble("kelas1"),100),
                                    rsobat.getDouble("dasar"),rsobat.getString("nama"),rsobat.getDouble("stok"),
                                    rsobat.getDouble("kapasitas"),"",0,0,0,rsobat.getString("nama_industri"),
                                    rsobat.getString("kategori"),rsobat.getString("golongan"),
                                    rsobat.getString("no_batch"),rsobat.getString("no_faktur"),rsobat.getString("tgl_kadaluarsa")
                                });           
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                            while(rsobat.next()){
                                tabModeDetailObatRacikan.addRow(new Object[] {
                                    getNoRacikAktif(),
                                    rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                    rsobat.getString("kode_sat"),Valid.roundUp(rsobat.getDouble("kelas2"),100),
                                    rsobat.getDouble("dasar"),rsobat.getString("nama"),rsobat.getDouble("stok"),
                                    rsobat.getDouble("kapasitas"),"",0,0,0,rsobat.getString("nama_industri"),
                                    rsobat.getString("kategori"),rsobat.getString("golongan"),
                                    rsobat.getString("no_batch"),rsobat.getString("no_faktur"),rsobat.getString("tgl_kadaluarsa")
                                });           
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                            while(rsobat.next()){
                                    tabModeDetailObatRacikan.addRow(new Object[] {
                                        getNoRacikAktif(),
                                        rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                        rsobat.getString("kode_sat"),Valid.roundUp(rsobat.getDouble("kelas3"),100),
                                        rsobat.getDouble("dasar"),rsobat.getString("nama"),rsobat.getDouble("stok"),
                                        rsobat.getDouble("kapasitas"),"",0,0,0,rsobat.getString("nama_industri"),
                                        rsobat.getString("kategori"),rsobat.getString("golongan"),
                                        rsobat.getString("no_batch"),rsobat.getString("no_faktur"),rsobat.getString("tgl_kadaluarsa")
                                    });
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                            while(rsobat.next()){
                                tabModeDetailObatRacikan.addRow(new Object[] {
                                    getNoRacikAktif(),
                                    rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                    rsobat.getString("kode_sat"),Valid.roundUp(rsobat.getDouble("utama"),100),
                                    rsobat.getDouble("dasar"),rsobat.getString("nama"),rsobat.getDouble("stok"),
                                    rsobat.getDouble("kapasitas"),"",0,0,0,rsobat.getString("nama_industri"),
                                    rsobat.getString("kategori"),rsobat.getString("golongan"),
                                    rsobat.getString("no_batch"),rsobat.getString("no_faktur"),rsobat.getString("tgl_kadaluarsa")
                                });            
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                            while(rsobat.next()){
                                tabModeDetailObatRacikan.addRow(new Object[] {
                                    getNoRacikAktif(),
                                    rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                    rsobat.getString("kode_sat"),Valid.roundUp(rsobat.getDouble("vip"),100),
                                    rsobat.getDouble("dasar"),rsobat.getString("nama"),rsobat.getDouble("stok"),
                                    rsobat.getDouble("kapasitas"),"",0,0,0,rsobat.getString("nama_industri"),
                                    rsobat.getString("kategori"),rsobat.getString("golongan"),
                                    rsobat.getString("no_batch"),rsobat.getString("no_faktur"),rsobat.getString("tgl_kadaluarsa")
                                });             
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                            while(rsobat.next()){
                                tabModeDetailObatRacikan.addRow(new Object[] {
                                    getNoRacikAktif(),
                                    rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                    rsobat.getString("kode_sat"),Valid.roundUp(rsobat.getDouble("vvip"),100),
                                    rsobat.getDouble("dasar"),rsobat.getString("nama"),rsobat.getDouble("stok"),
                                    rsobat.getDouble("kapasitas"),"",0,0,0,rsobat.getString("nama_industri"),
                                    rsobat.getString("kategori"),rsobat.getString("golongan"),
                                    rsobat.getString("no_batch"),rsobat.getString("no_faktur"),rsobat.getString("tgl_kadaluarsa")
                                });          
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                            while(rsobat.next()){
                                tabModeDetailObatRacikan.addRow(new Object[] {
                                    getNoRacikAktif(),
                                    rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                    rsobat.getString("kode_sat"),Valid.roundUp(rsobat.getDouble("beliluar"),100),
                                    rsobat.getDouble("dasar"),rsobat.getString("nama"),rsobat.getDouble("stok"),
                                    rsobat.getDouble("kapasitas"),"",0,0,0,rsobat.getString("nama_industri"),
                                    rsobat.getString("kategori"),rsobat.getString("golongan"),
                                    rsobat.getString("no_batch"),rsobat.getString("no_faktur"),rsobat.getString("tgl_kadaluarsa")
                                });            
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                            while(rsobat.next()){
                                tabModeDetailObatRacikan.addRow(new Object[] {
                                    getNoRacikAktif(),
                                    rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                    rsobat.getString("kode_sat"),Valid.roundUp(rsobat.getDouble("karyawan"),100),
                                    rsobat.getDouble("dasar"),rsobat.getString("nama"),rsobat.getDouble("stok"),
                                    rsobat.getDouble("kapasitas"),"",0,0,0,rsobat.getString("nama_industri"),
                                    rsobat.getString("kategori"),rsobat.getString("golongan"),
                                    rsobat.getString("no_batch"),rsobat.getString("no_faktur"),rsobat.getString("tgl_kadaluarsa")
                                });        
                            }
                        } 
                    }catch(Exception e){
                        System.out.println("Notifikasi : "+e);
                    }finally{
                        if(rsobat != null){
                            rsobat.close();
                        }
                        if(psobat != null){
                            psobat.close();
                        }
                    }
                }else{
                    if(aktifpcare.equals("yes")){
                        sql="select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,databarang.kelas1,databarang.kelas2,databarang.kelas3,"+
                            " databarang.utama,databarang.vip,databarang.vvip,databarang.beliluar,databarang.karyawan,databarang."+hppfarmasi+" as dasar,gudangbarang.stok,"+
                            " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,kategori_barang.nama as kategori,golongan_barang.nama as golongan,databarang.kapasitas  "+
                            " from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                            " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                            " inner join golongan_barang on databarang.kode_golongan=golongan_barang.kode "+
                            " inner join kategori_barang on databarang.kode_kategori=kategori_barang.kode "+
                            " inner join maping_obat_pcare on maping_obat_pcare.kode_brng=databarang.kode_brng "+
                            " inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng ";
                    }else{
                        sql="select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,databarang.kelas1,databarang.kelas2,databarang.kelas3,"+
                            " databarang.utama,databarang.vip,databarang.vvip,databarang.beliluar,databarang.karyawan,databarang."+hppfarmasi+" as dasar,gudangbarang.stok,"+
                            " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,kategori_barang.nama as kategori,golongan_barang.nama as golongan,databarang.kapasitas  "+
                            " from databarang inner join jenis on databarang.kdjns=jenis.kdjns "+
                            " inner join industrifarmasi on industrifarmasi.kode_industri=databarang.kode_industri "+
                            " inner join golongan_barang on databarang.kode_golongan=golongan_barang.kode "+
                            " inner join kategori_barang on databarang.kode_kategori=kategori_barang.kode "+
                            " inner join gudangbarang on databarang.kode_brng=gudangbarang.kode_brng ";
                    }
                    psobat=koneksi.prepareStatement(
                        sql+" where gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.status='1' and databarang.kode_brng like ? or "+
                        " gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.status='1' and databarang.nama_brng like ? or "+
                        " gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.status='1' and kategori_barang.nama like ? or "+
                        " gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.status='1' and golongan_barang.nama like ? or "+
                        " gudangbarang.no_batch='' and gudangbarang.no_faktur='' and gudangbarang.stok>0 and gudangbarang.kd_bangsal=? and databarang.status='1' and jenis.nama like ? order by databarang.nama_brng");
                    try{    
                        psobat.setString(1,kdgudang.getText());
                        psobat.setString(2,"%"+TCari.getText().trim()+"%");
                        psobat.setString(3,kdgudang.getText());
                        psobat.setString(4,"%"+TCari.getText().trim()+"%");
                        psobat.setString(5,kdgudang.getText());
                        psobat.setString(6,"%"+TCari.getText().trim()+"%");
                        psobat.setString(7,kdgudang.getText());
                        psobat.setString(8,"%"+TCari.getText().trim()+"%");
                        psobat.setString(9,kdgudang.getText());
                        psobat.setString(10,"%"+TCari.getText().trim()+"%");
                        rsobat=psobat.executeQuery();
                        if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                            while(rsobat.next()){
                                tabModeDetailObatRacikan.addRow(new Object[] {
                                    getNoRacikAktif(),
                                    rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                    rsobat.getString("kode_sat"),Valid.roundUp(rsobat.getDouble("kelas1"),100),
                                    rsobat.getDouble("dasar"),rsobat.getString("nama"),rsobat.getDouble("stok"),
                                    rsobat.getDouble("kapasitas"),"",0,0,0,rsobat.getString("nama_industri"),
                                    rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                });             
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                            while(rsobat.next()){
                                tabModeDetailObatRacikan.addRow(new Object[] {
                                    getNoRacikAktif(),
                                    rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                    rsobat.getString("kode_sat"),Valid.roundUp(rsobat.getDouble("kelas2"),100),
                                    rsobat.getDouble("dasar"),rsobat.getString("nama"),rsobat.getDouble("stok"),
                                    rsobat.getDouble("kapasitas"),"",0,0,0,rsobat.getString("nama_industri"),
                                    rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                });             
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                            while(rsobat.next()){
                                tabModeDetailObatRacikan.addRow(new Object[] {
                                    getNoRacikAktif(),
                                    rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                    rsobat.getString("kode_sat"),Valid.roundUp(rsobat.getDouble("kelas3"),100),
                                    rsobat.getDouble("dasar"),rsobat.getString("nama"),rsobat.getDouble("stok"),
                                    rsobat.getDouble("kapasitas"),"",0,0,0,rsobat.getString("nama_industri"),
                                    rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                });            
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                            while(rsobat.next()){
                                tabModeDetailObatRacikan.addRow(new Object[] {
                                    getNoRacikAktif(),
                                    rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                    rsobat.getString("kode_sat"),Valid.roundUp(rsobat.getDouble("utama"),100),
                                    rsobat.getDouble("dasar"),rsobat.getString("nama"),rsobat.getDouble("stok"),
                                    rsobat.getDouble("kapasitas"),"",0,0,0,rsobat.getString("nama_industri"),
                                    rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                });             
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                            while(rsobat.next()){
                                tabModeDetailObatRacikan.addRow(new Object[] {
                                    getNoRacikAktif(),
                                    rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                    rsobat.getString("kode_sat"),Valid.roundUp(rsobat.getDouble("vip"),100),
                                    rsobat.getDouble("dasar"),rsobat.getString("nama"),rsobat.getDouble("stok"),
                                    rsobat.getDouble("kapasitas"),"",0,0,0,rsobat.getString("nama_industri"),
                                    rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                });            
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                            while(rsobat.next()){
                                tabModeDetailObatRacikan.addRow(new Object[] {
                                    getNoRacikAktif(),
                                    rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                    rsobat.getString("kode_sat"),Valid.roundUp(rsobat.getDouble("vvip"),100),
                                    rsobat.getDouble("dasar"),rsobat.getString("nama"),rsobat.getDouble("stok"),
                                    rsobat.getDouble("kapasitas"),"",0,0,0,rsobat.getString("nama_industri"),
                                    rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                });            
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                            while(rsobat.next()){
                                tabModeDetailObatRacikan.addRow(new Object[] {
                                    getNoRacikAktif(),
                                    rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                    rsobat.getString("kode_sat"),Valid.roundUp(rsobat.getDouble("beliluar"),100),
                                    rsobat.getDouble("dasar"),rsobat.getString("nama"),rsobat.getDouble("stok"),
                                    rsobat.getDouble("kapasitas"),"",0,0,0,rsobat.getString("nama_industri"),
                                    rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                });           
                            }
                        }else if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                            while(rsobat.next()){
                                tabModeDetailObatRacikan.addRow(new Object[] {
                                    getNoRacikAktif(),
                                    rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                    rsobat.getString("kode_sat"),Valid.roundUp(rsobat.getDouble("karyawan"),100),
                                    rsobat.getDouble("dasar"),rsobat.getString("nama"),rsobat.getDouble("stok"),
                                    rsobat.getDouble("kapasitas"),"",0,0,0,rsobat.getString("nama_industri"),
                                    rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                });        
                            }
                        } 
                    }catch(Exception e){
                        System.out.println("Notifikasi : "+e);
                    }finally{
                        if(rsobat != null){
                            rsobat.close();
                        }
                        if(psobat != null){
                            psobat.close();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : "+e);
        }
        tampilkanDetailRacikanAktifFarmasi();
    }
    
    public void tampilobat2(String no_resep) { 
        this.noresep=no_resep;
        tampilkanCatatanResepDokter(no_resep);
        try{
            Valid.tabelKosong(tabMode);
            Valid.tabelKosong(tabModeObatRacikan);
            Valid.tabelKosong(tabModeDetailObatRacikan);
            if(kenaikan>0){
                if(aktifkanbatch.equals("yes")){
                    psobat=koneksi.prepareStatement("select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,(databarang.h_beli+(databarang.h_beli*?)) as harga,"+
                        " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,kategori_barang.nama as kategori,golongan_barang.nama as golongan,"+
                        " resep_dokter.jml, resep_dokter.aturan_pakai from databarang inner join jenis inner join industrifarmasi inner join golongan_barang "+
                        " inner join kategori_barang inner join resep_dokter on databarang.kdjns=jenis.kdjns "+
                        " and industrifarmasi.kode_industri=databarang.kode_industri and databarang.kode_golongan=golongan_barang.kode and databarang.kode_kategori=kategori_barang.kode "+
                        " and resep_dokter.kode_brng=databarang.kode_brng where "+
                        " resep_dokter.no_resep=? and databarang.status='1' and databarang.kode_brng like ? or "+
                        " resep_dokter.no_resep=? and databarang.status='1' and databarang.nama_brng like ? or "+
                        " resep_dokter.no_resep=? and databarang.status='1' and kategori_barang.nama like ? or "+
                        " resep_dokter.no_resep=? and databarang.status='1' and golongan_barang.nama like ? or "+
                        " resep_dokter.no_resep=? and databarang.status='1' and jenis.nama like ? order by databarang.nama_brng");
                    try {
                        psobat.setDouble(1,kenaikan);
                        psobat.setString(2,no_resep);
                        psobat.setString(3,"%"+TCari.getText().trim()+"%");
                        psobat.setString(4,no_resep);
                        psobat.setString(5,"%"+TCari.getText().trim()+"%");
                        psobat.setString(6,no_resep);
                        psobat.setString(7,"%"+TCari.getText().trim()+"%");
                        psobat.setString(8,no_resep);
                        psobat.setString(9,"%"+TCari.getText().trim()+"%");
                        psobat.setString(10,no_resep);
                        psobat.setString(11,"%"+TCari.getText().trim()+"%");
                        rsobat=psobat.executeQuery();
                        while(rsobat.next()){
                            no_batchcari="";tgl_kadaluarsacari="";no_fakturcari="";h_belicari=0;hargacari=0;sisacari=0;
                            psbatch=koneksi.prepareStatement(
                                "select data_batch.no_batch, data_batch.kode_brng, data_batch.tgl_beli, data_batch.tgl_kadaluarsa, data_batch.asal, data_batch.no_faktur, "+
                                "data_batch.h_beli,(data_batch.h_beli+(data_batch.h_beli*?)) as harga, gudangbarang.stok,data_batch."+hppfarmasi+" as dasar from data_batch inner join gudangbarang "+
                                "on data_batch.kode_brng=gudangbarang.kode_brng and data_batch.no_batch=gudangbarang.no_batch and data_batch.no_faktur=gudangbarang.no_faktur where "+
                                "gudangbarang.stok>0 and data_batch.kode_brng=? and gudangbarang.kd_bangsal=? order by data_batch.tgl_kadaluarsa desc limit 1");
                            try {
                                psbatch.setDouble(1,kenaikan);
                                psbatch.setString(2,rsobat.getString("kode_brng"));
                                psbatch.setString(3,kdgudang.getText());
                                rsbatch=psbatch.executeQuery();
                                while(rsbatch.next()){
                                    no_batchcari=rsbatch.getString("no_batch");
                                    tgl_kadaluarsacari=rsbatch.getString("tgl_kadaluarsa");
                                    no_fakturcari=rsbatch.getString("no_faktur");
                                    h_belicari=rsbatch.getDouble("dasar");                                    
                                    hargacari=rsbatch.getDouble("harga");                                 
                                    sisacari=rsbatch.getDouble("stok");
                                }                                
                            } catch (Exception e) {
                                System.out.println("Notif : "+e);
                            } finally{
                                if(rsbatch!=null){
                                    rsbatch.close();
                                }
                                if(psbatch!=null){
                                    psbatch.close();
                                }
                            }
                            if(rsobat.getDouble("jml")>sisacari){
                                JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                tabMode.addRow(new Object[] {false,sisacari,rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(hargacari,100),                                   
                                   rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                   h_belicari,rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),
                                   no_batchcari,no_fakturcari,tgl_kadaluarsacari
                                });
                            }else{
                                tabMode.addRow(new Object[] {false,rsobat.getString("jml"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(hargacari,100),                                   
                                   rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                   h_belicari,rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),
                                   no_batchcari,no_fakturcari,tgl_kadaluarsacari
                                });
                            }          
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : "+e);
                    } finally{
                        if(rsobat != null){
                            rsobat.close();
                        }
                        if(psobat != null){
                            psobat.close();
                        }
                    } 
                }else{
                    psobat=koneksi.prepareStatement("select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,(databarang.h_beli+(databarang.h_beli*?)) as harga,"+
                        " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,kategori_barang.nama as kategori,golongan_barang.nama as golongan,"+
                        " resep_dokter.jml, resep_dokter.aturan_pakai,databarang."+hppfarmasi+" as dasar from databarang inner join jenis inner join industrifarmasi inner join golongan_barang "+
                        " inner join kategori_barang inner join resep_dokter on databarang.kdjns=jenis.kdjns "+
                        " and industrifarmasi.kode_industri=databarang.kode_industri and databarang.kode_golongan=golongan_barang.kode and databarang.kode_kategori=kategori_barang.kode "+
                        " and resep_dokter.kode_brng=databarang.kode_brng where "+
                        " resep_dokter.no_resep=? and databarang.status='1' and databarang.kode_brng like ? or "+
                        " resep_dokter.no_resep=? and databarang.status='1' and databarang.nama_brng like ? or "+
                        " resep_dokter.no_resep=? and databarang.status='1' and kategori_barang.nama like ? or "+
                        " resep_dokter.no_resep=? and databarang.status='1' and golongan_barang.nama like ? or "+
                        " resep_dokter.no_resep=? and databarang.status='1' and jenis.nama like ? order by databarang.nama_brng");
                    try {
                        psobat.setDouble(1,kenaikan);
                        psobat.setString(2,no_resep);
                        psobat.setString(3,"%"+TCari.getText().trim()+"%");
                        psobat.setString(4,no_resep);
                        psobat.setString(5,"%"+TCari.getText().trim()+"%");
                        psobat.setString(6,no_resep);
                        psobat.setString(7,"%"+TCari.getText().trim()+"%");
                        psobat.setString(8,no_resep);
                        psobat.setString(9,"%"+TCari.getText().trim()+"%");
                        psobat.setString(10,no_resep);
                        psobat.setString(11,"%"+TCari.getText().trim()+"%");
                        rsobat=psobat.executeQuery();
                        while(rsobat.next()){
                            sisacari=0;
                            psstok=koneksi.prepareStatement("select ifnull(gudangbarang.stok,'0') from gudangbarang where gudangbarang.kd_bangsal=? and gudangbarang.kode_brng=? and gudangbarang.no_batch='' and gudangbarang.no_faktur=''");
                            try {
                                psstok.setString(1,kdgudang.getText());
                                psstok.setString(2,rsobat.getString("kode_brng"));
                                rsstok=psstok.executeQuery();
                                if(rsstok.next()){
                                    sisacari=rsstok.getDouble(1);
                                }                                
                            } catch (Exception e) {
                                sisacari=0;
                                System.out.println("Notifikasi : "+e);
                            }finally{
                                if(rsstok != null){
                                    rsstok.close();
                                }
                                if(psstok != null){
                                    psstok.close();
                                }
                            }
                            if(rsobat.getDouble("jml")>sisacari){
                                JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                tabMode.addRow(new Object[] {false,sisacari,rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("harga"),100),                                   
                                   rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                   rsobat.getDouble("dasar"),rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                }); 
                            }else{
                                tabMode.addRow(new Object[] {false,rsobat.getString("jml"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                   rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("harga"),100),                                   
                                   rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                   rsobat.getDouble("dasar"),rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                }); 
                            }         
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : "+e);
                    } finally{
                        if(rsobat != null){
                            rsobat.close();
                        }
                        if(psobat != null){
                            psobat.close();
                        }
                    } 
                }                                    
            }else{
                if(aktifkanbatch.equals("yes")){
                    psobat=koneksi.prepareStatement("select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,databarang.kelas1,"+
                        " databarang.kelas2,databarang.kelas3,databarang.utama,databarang.vip,databarang.vvip,databarang.beliluar,databarang.karyawan,"+
                        " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,kategori_barang.nama as kategori,"+
                        " golongan_barang.nama as golongan,resep_dokter.jml,resep_dokter.aturan_pakai "+
                        " from databarang inner join jenis inner join industrifarmasi inner join golongan_barang inner join kategori_barang inner join resep_dokter "+
                        " on databarang.kdjns=jenis.kdjns and industrifarmasi.kode_industri=databarang.kode_industri and databarang.kode_golongan=golongan_barang.kode "+
                        " and databarang.kode_kategori=kategori_barang.kode and resep_dokter.kode_brng=databarang.kode_brng "+
                        " where resep_dokter.no_resep=? and databarang.status='1' and databarang.kode_brng like ? or "+
                        " resep_dokter.no_resep=? and databarang.status='1' and databarang.nama_brng like ? or "+
                        " resep_dokter.no_resep=? and databarang.status='1' and kategori_barang.nama like ? or "+
                        " resep_dokter.no_resep=? and databarang.status='1' and golongan_barang.nama like ? or "+
                        " resep_dokter.no_resep=? and databarang.status='1' and jenis.nama like ? order by databarang.nama_brng");
                    try {
                        psobat.setString(1,no_resep);
                        psobat.setString(2,"%"+TCari.getText().trim()+"%");
                        psobat.setString(3,no_resep);
                        psobat.setString(4,"%"+TCari.getText().trim()+"%");
                        psobat.setString(5,no_resep);
                        psobat.setString(6,"%"+TCari.getText().trim()+"%");
                        psobat.setString(7,no_resep);
                        psobat.setString(8,"%"+TCari.getText().trim()+"%");
                        psobat.setString(9,no_resep);
                        psobat.setString(10,"%"+TCari.getText().trim()+"%");
                        rsobat=psobat.executeQuery();
                        while(rsobat.next()){
                            no_batchcari="";tgl_kadaluarsacari="";no_fakturcari="";h_belicari=0;hargacari=0;sisacari=0;
                            psbatch=koneksi.prepareStatement(
                                "select data_batch.no_batch, data_batch.kode_brng, data_batch.tgl_beli, data_batch.tgl_kadaluarsa, data_batch.asal, data_batch.no_faktur, "+
                                "data_batch.h_beli, data_batch.ralan, data_batch.kelas1, data_batch.kelas2, data_batch.kelas3, data_batch.utama, data_batch.vip, data_batch.vvip, data_batch.beliluar, "+
                                "data_batch.jualbebas, data_batch.karyawan, data_batch.jumlahbeli, gudangbarang.stok,data_batch."+hppfarmasi+" as dasar from data_batch inner join gudangbarang where "+
                                "gudangbarang.stok>0 and data_batch.kode_brng=? and gudangbarang.kd_bangsal=? order by data_batch.tgl_kadaluarsa desc limit 1");
                            try {
                                psbatch.setString(1,rsobat.getString("kode_brng"));
                                psbatch.setString(2,kdgudang.getText());
                                rsbatch=psbatch.executeQuery();
                                while(rsbatch.next()){
                                    no_batchcari=rsbatch.getString("no_batch");
                                    tgl_kadaluarsacari=rsbatch.getString("tgl_kadaluarsa");
                                    no_fakturcari=rsbatch.getString("no_faktur");
                                    h_belicari=rsbatch.getDouble("dasar");
                                    if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                                        hargacari=rsbatch.getDouble("kelas1");
                                    }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                                        hargacari=rsbatch.getDouble("kelas2");
                                    }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                                        hargacari=rsbatch.getDouble("kelas3");
                                    }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                                        hargacari=rsbatch.getDouble("utama");
                                    }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                                        hargacari=rsbatch.getDouble("vip");
                                    }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                                        hargacari=rsbatch.getDouble("vvip");
                                    }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                                        hargacari=rsbatch.getDouble("beliluar");
                                    }else if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                                        hargacari=rsbatch.getDouble("karyawan");
                                    }                                 
                                    sisacari=rsbatch.getDouble("stok");
                                }                                
                            } catch (Exception e) {
                                System.out.println("Notif : "+e);
                            } finally{
                                if(rsbatch!=null){
                                    rsbatch.close();
                                }
                                if(psbatch!=null){
                                    psbatch.close();
                                }
                            }
                            if(rsobat.getDouble("jml")>sisacari){
                                JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                tabMode.addRow(new Object[] {
                                    false,sisacari,rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                    rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(hargacari,100),
                                    rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                    h_belicari,rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),
                                    no_batchcari,no_fakturcari,tgl_kadaluarsacari
                                });
                            }else{
                                tabMode.addRow(new Object[] {
                                    false,rsobat.getString("jml"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                    rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(hargacari,100),
                                    rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                    h_belicari,rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),
                                    no_batchcari,no_fakturcari,tgl_kadaluarsacari
                                });
                            }   
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : "+e);
                    } finally{
                        if(rsobat != null){
                            rsobat.close();
                        }
                        if(psobat != null){
                            psobat.close();
                        }
                    }
                }else{
                    psobat=koneksi.prepareStatement("select databarang.kode_brng, databarang.nama_brng,jenis.nama, databarang.kode_sat,databarang.kelas1,"+
                        " databarang.kelas2,databarang.kelas3,databarang.utama,databarang.vip,databarang.vvip,databarang.beliluar,databarang.karyawan,"+
                        " databarang.letak_barang,industrifarmasi.nama_industri,databarang.h_beli,kategori_barang.nama as kategori,"+
                        " golongan_barang.nama as golongan,resep_dokter.jml,resep_dokter.aturan_pakai,databarang."+hppfarmasi+" as dasar "+
                        " from databarang inner join jenis inner join industrifarmasi inner join golongan_barang inner join kategori_barang inner join resep_dokter "+
                        " on databarang.kdjns=jenis.kdjns and industrifarmasi.kode_industri=databarang.kode_industri and databarang.kode_golongan=golongan_barang.kode "+
                        " and databarang.kode_kategori=kategori_barang.kode and resep_dokter.kode_brng=databarang.kode_brng "+
                        " where resep_dokter.no_resep=? and databarang.status='1' and databarang.kode_brng like ? or "+
                        " resep_dokter.no_resep=? and databarang.status='1' and databarang.nama_brng like ? or "+
                        " resep_dokter.no_resep=? and databarang.status='1' and kategori_barang.nama like ? or "+
                        " resep_dokter.no_resep=? and databarang.status='1' and golongan_barang.nama like ? or "+
                        " resep_dokter.no_resep=? and databarang.status='1' and jenis.nama like ? order by databarang.nama_brng");
                    try {
                        psobat.setString(1,no_resep);
                        psobat.setString(2,"%"+TCari.getText().trim()+"%");
                        psobat.setString(3,no_resep);
                        psobat.setString(4,"%"+TCari.getText().trim()+"%");
                        psobat.setString(5,no_resep);
                        psobat.setString(6,"%"+TCari.getText().trim()+"%");
                        psobat.setString(7,no_resep);
                        psobat.setString(8,"%"+TCari.getText().trim()+"%");
                        psobat.setString(9,no_resep);
                        psobat.setString(10,"%"+TCari.getText().trim()+"%");
                        rsobat=psobat.executeQuery();
                        while(rsobat.next()){
                            sisacari=0;
                            psstok=koneksi.prepareStatement("select ifnull(gudangbarang.stok,'0') from gudangbarang where gudangbarang.kd_bangsal=? and gudangbarang.kode_brng=? and gudangbarang.no_batch='' and gudangbarang.no_faktur=''");
                            try {
                                psstok.setString(1,kdgudang.getText());
                                psstok.setString(2,rsobat.getString("kode_brng"));
                                rsstok=psstok.executeQuery();
                                if(rsstok.next()){
                                    sisacari=rsstok.getDouble(1);
                                }                                
                            } catch (Exception e) {
                                sisacari=0;
                                System.out.println("Notifikasi : "+e);
                            }finally{
                                if(rsstok != null){
                                    rsstok.close();
                                }
                                if(psstok != null){
                                    psstok.close();
                                }
                            }
                            if(rsobat.getDouble("jml")>sisacari){
                                JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                                    tabMode.addRow(new Object[] {false,sisacari,rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                               rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas1"),100),
                                               rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                               rsobat.getDouble("dasar"),rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                                    tabMode.addRow(new Object[] {false,sisacari,rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                               rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas2"),100),
                                               rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                               rsobat.getDouble("dasar"),rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                                    tabMode.addRow(new Object[] {false,sisacari,rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                               rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas3"),100),
                                               rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                               rsobat.getDouble("dasar"),rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                                    tabMode.addRow(new Object[] {false,sisacari,rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                               rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("utama"),100),
                                               rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                               rsobat.getDouble("dasar"),rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                                    tabMode.addRow(new Object[] {false,sisacari,rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                               rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("vip"),100),
                                               rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                               rsobat.getDouble("dasar"),rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                                    tabMode.addRow(new Object[] {false,sisacari,rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                               rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("vvip"),100),
                                               rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                               rsobat.getDouble("dasar"),rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                                    tabMode.addRow(new Object[] {false,sisacari,rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                               rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("beliluar"),100),
                                               rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                               rsobat.getDouble("dasar"),rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                                    tabMode.addRow(new Object[] {false,sisacari,rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                               rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("karyawan"),100),
                                               rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                               rsobat.getDouble("dasar"),rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                    });
                                }
                            }else{
                                if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                                    tabMode.addRow(new Object[] {false,rsobat.getString("jml"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                               rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas1"),100),
                                               rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                               rsobat.getDouble("dasar"),rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                                    tabMode.addRow(new Object[] {false,rsobat.getString("jml"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                               rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas2"),100),
                                               rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                               rsobat.getDouble("dasar"),rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                                    tabMode.addRow(new Object[] {false,rsobat.getString("jml"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                               rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("kelas3"),100),
                                               rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                               rsobat.getDouble("dasar"),rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                                    tabMode.addRow(new Object[] {false,rsobat.getString("jml"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                               rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("utama"),100),
                                               rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                               rsobat.getDouble("dasar"),rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                                    tabMode.addRow(new Object[] {false,rsobat.getString("jml"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                               rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("vip"),100),
                                               rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                               rsobat.getDouble("dasar"),rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                                    tabMode.addRow(new Object[] {false,rsobat.getString("jml"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                               rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("vvip"),100),
                                               rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                               rsobat.getDouble("dasar"),rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                                    tabMode.addRow(new Object[] {false,rsobat.getString("jml"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                               rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("beliluar"),100),
                                               rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                               rsobat.getDouble("dasar"),rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                    });
                                }else if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                                    tabMode.addRow(new Object[] {false,rsobat.getString("jml"),rsobat.getString("kode_brng"),rsobat.getString("nama_brng"),
                                               rsobat.getString("kode_sat"),rsobat.getString("letak_barang"),Valid.roundUp(rsobat.getDouble("karyawan"),100),
                                               rsobat.getString("nama"),0,0,sisacari,rsobat.getString("nama_industri"),
                                               rsobat.getDouble("dasar"),rsobat.getString("aturan_pakai"),rsobat.getString("kategori"),rsobat.getString("golongan"),"","",""
                                    });
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : "+e);
                    } finally{
                        if(rsobat != null){
                            rsobat.close();
                        }
                        if(psobat != null){
                            psobat.close();
                        }
                    }
                }                    
            }  
            for(i=0;i<tbObat.getRowCount();i++){
                getDataobat(i);
            }
            psobat=koneksi.prepareStatement(
                    "select resep_dokter_racikan.no_racik,resep_dokter_racikan.nama_racik,"+
                    "resep_dokter_racikan.kd_racik,metode_racik.nm_racik as metode,"+
                    "resep_dokter_racikan.jml_dr,resep_dokter_racikan.aturan_pakai,"+
                    "resep_dokter_racikan.keterangan from resep_dokter_racikan inner join metode_racik "+
                    "on resep_dokter_racikan.kd_racik=metode_racik.kd_racik where "+
                    "resep_dokter_racikan.no_resep=? ");
            try {
                psobat.setString(1,no_resep);
                rsobat=psobat.executeQuery();
                while(rsobat.next()){
                    tabModeObatRacikan.addRow(new String[]{
                        rsobat.getString("no_racik"),rsobat.getString("nama_racik"),rsobat.getString("kd_racik"),
                        rsobat.getString("metode"),rsobat.getString("jml_dr"),rsobat.getString("aturan_pakai"),
                        rsobat.getString("keterangan")
                    });
                    if(kenaikan>0){
                        if(aktifkanbatch.equals("yes")){
                            ps2=koneksi.prepareStatement("select databarang.kode_brng,databarang.nama_brng,resep_dokter_racikan_detail.jml,resep_dokter_racikan_detail.kandungan,"+
                                "databarang.kode_sat,(databarang.h_beli+(databarang.h_beli*?)) as harga,databarang.letak_barang,"+
                                "databarang.h_beli,kategori_barang.nama as kategori,golongan_barang.nama as golongan,"+
                                "industrifarmasi.nama_industri,jenis.nama as jenis,databarang.kapasitas from resep_dokter_racikan_detail inner join databarang inner join jenis "+
                                "inner join golongan_barang inner join kategori_barang inner join industrifarmasi on "+
                                "resep_dokter_racikan_detail.kode_brng=databarang.kode_brng and databarang.kdjns=jenis.kdjns "+
                                "and industrifarmasi.kode_industri=databarang.kode_industri and databarang.kode_golongan=golongan_barang.kode "+
                                "and databarang.kode_kategori=kategori_barang.kode where resep_dokter_racikan_detail.no_resep=? and "+
                                "resep_dokter_racikan_detail.no_racik=? order by databarang.kode_brng");
                            try {
                                ps2.setDouble(1,kenaikan);
                                ps2.setString(2,no_resep);
                                ps2.setString(3,rsobat.getString("no_racik"));
                                rs2=ps2.executeQuery();
                                while(rs2.next()){
                                    no_batchcari="";tgl_kadaluarsacari="";no_fakturcari="";h_belicari=0;hargacari=0;sisacari=0;
                                    psbatch=koneksi.prepareStatement(
                                        "select data_batch.no_batch, data_batch.kode_brng, data_batch.tgl_beli, data_batch.tgl_kadaluarsa, data_batch.asal, data_batch.no_faktur, "+
                                        "data_batch.h_beli,(data_batch.h_beli+(data_batch.h_beli*?)) as harga, gudangbarang.stok,data_batch."+hppfarmasi+" as dasar from data_batch inner join gudangbarang "+
                                        "on data_batch.kode_brng=gudangbarang.kode_brng and data_batch.no_batch=gudangbarang.no_batch and data_batch.no_faktur=gudangbarang.no_faktur where "+
                                        "gudangbarang.stok>0 and data_batch.kode_brng=? and gudangbarang.kd_bangsal=? order by data_batch.tgl_kadaluarsa desc limit 1");
                                    try {
                                        psbatch.setDouble(1,kenaikan);
                                        psbatch.setString(2,rs2.getString("kode_brng"));
                                        psbatch.setString(3,kdgudang.getText());
                                        rsbatch=psbatch.executeQuery();
                                        while(rsbatch.next()){
                                            no_batchcari=rsbatch.getString("no_batch");
                                            tgl_kadaluarsacari=rsbatch.getString("tgl_kadaluarsa");
                                            no_fakturcari=rsbatch.getString("no_faktur");
                                            h_belicari=rsbatch.getDouble("dasar");                                    
                                            hargacari=rsbatch.getDouble("harga");                                 
                                            sisacari=rsbatch.getDouble("stok");
                                        }                                
                                    } catch (Exception e) {
                                        System.out.println("Notif : "+e);
                                    } finally{
                                        if(rsbatch!=null){
                                            rsbatch.close();
                                        }
                                        if(psbatch!=null){
                                            psbatch.close();
                                        }
                                    }
                                    if(rs2.getDouble("jml")>sisacari){
                                        JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                        tabModeDetailObatRacikan.addRow(new Object[]{
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),hargacari,h_belicari,
                                            rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                            sisacari,0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                            rs2.getString("golongan"),no_batchcari,no_fakturcari,tgl_kadaluarsacari                                    
                                        });  
                                    }else{
                                        tabModeDetailObatRacikan.addRow(new Object[]{
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),hargacari,h_belicari,
                                            rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                            rs2.getDouble("jml"),0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                            rs2.getString("golongan"),no_batchcari,no_fakturcari,tgl_kadaluarsacari                                    
                                        });  
                                    }                        
                                }
                            } catch (Exception e) {
                                System.out.println("Notifikasi 3 : "+e);
                            } finally{
                                if(rs2!=null){
                                    rs2.close();
                                }
                                if(ps2!=null){
                                    ps2.close();
                                }
                            }
                        }else{
                            ps2=koneksi.prepareStatement("select databarang.kode_brng,databarang.nama_brng,resep_dokter_racikan_detail.jml,resep_dokter_racikan_detail.kandungan,"+
                                "databarang.kode_sat,(databarang.h_beli+(databarang.h_beli*?)) as harga,databarang.letak_barang,"+
                                "databarang.h_beli,kategori_barang.nama as kategori,golongan_barang.nama as golongan,"+
                                "industrifarmasi.nama_industri,jenis.nama as jenis,databarang.kapasitas,databarang."+hppfarmasi+" as dasar from resep_dokter_racikan_detail inner join databarang inner join jenis "+
                                "inner join golongan_barang inner join kategori_barang inner join industrifarmasi on "+
                                "resep_dokter_racikan_detail.kode_brng=databarang.kode_brng and databarang.kdjns=jenis.kdjns "+
                                "and industrifarmasi.kode_industri=databarang.kode_industri and databarang.kode_golongan=golongan_barang.kode "+
                                "and databarang.kode_kategori=kategori_barang.kode where resep_dokter_racikan_detail.no_resep=? and "+
                                "resep_dokter_racikan_detail.no_racik=? order by databarang.kode_brng");
                            try {
                                ps2.setDouble(1,kenaikan);
                                ps2.setString(2,no_resep);
                                ps2.setString(3,rsobat.getString("no_racik"));
                                rs2=ps2.executeQuery();
                                while(rs2.next()){
                                    sisacari=0;
                                    psstok=koneksi.prepareStatement("select ifnull(gudangbarang.stok,'0') from gudangbarang where gudangbarang.kd_bangsal=? and gudangbarang.kode_brng=? and gudangbarang.no_batch='' and gudangbarang.no_faktur=''");
                                    try {
                                        psstok.setString(1,kdgudang.getText());
                                        psstok.setString(2,rs2.getString("kode_brng"));
                                        rsstok=psstok.executeQuery();
                                        if(rsstok.next()){
                                            sisacari=rsstok.getDouble(1);
                                        }                                
                                    } catch (Exception e) {
                                        sisacari=0;
                                        System.out.println("Notifikasi : "+e);
                                    }finally{
                                        if(rsstok != null){
                                            rsstok.close();
                                        }
                                        if(psstok != null){
                                            psstok.close();
                                        }
                                    }
                                    if(rs2.getDouble("jml")>sisacari){
                                        JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                        tabModeDetailObatRacikan.addRow(new Object[]{
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),rs2.getDouble("harga"),rs2.getDouble("dasar"),
                                            rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                            sisacari,0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                            rs2.getString("golongan"),"","",""                                    
                                        });  
                                    }else{
                                        tabModeDetailObatRacikan.addRow(new Object[]{
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),rs2.getDouble("harga"),rs2.getDouble("dasar"),
                                            rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                            rs2.getDouble("jml"),0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                            rs2.getString("golongan"),"","",""                                    
                                        });  
                                    }                        
                                }
                            } catch (Exception e) {
                                System.out.println("Notifikasi 3 : "+e);
                            } finally{
                                if(rs2!=null){
                                    rs2.close();
                                }
                                if(ps2!=null){
                                    ps2.close();
                                }
                            }
                        }
                    }else{
                        if(aktifkanbatch.equals("yes")){
                            ps2=koneksi.prepareStatement("select databarang.kode_brng,databarang.nama_brng,resep_dokter_racikan_detail.jml,resep_dokter_racikan_detail.kandungan,"+
                                "databarang.kode_sat,databarang.kelas1,databarang.kelas2,databarang.kelas3,databarang.utama,databarang.vip,databarang.vvip,databarang.beliluar,"+
                                "databarang.karyawan,databarang.h_beli,kategori_barang.nama as kategori,golongan_barang.nama as golongan,"+
                                "industrifarmasi.nama_industri,jenis.nama as jenis,databarang.kapasitas,databarang."+hppfarmasi+" as dasar from resep_dokter_racikan_detail inner join databarang inner join jenis "+
                                "inner join golongan_barang inner join kategori_barang inner join industrifarmasi on "+
                                "resep_dokter_racikan_detail.kode_brng=databarang.kode_brng and databarang.kdjns=jenis.kdjns "+
                                "and industrifarmasi.kode_industri=databarang.kode_industri and databarang.kode_golongan=golongan_barang.kode "+
                                "and databarang.kode_kategori=kategori_barang.kode where resep_dokter_racikan_detail.no_resep=? and "+
                                "resep_dokter_racikan_detail.no_racik=? order by databarang.kode_brng");
                            try {
                                ps2.setString(1,no_resep);
                                ps2.setString(2,rsobat.getString("no_racik"));
                                rs2=ps2.executeQuery();
                                while(rs2.next()){
                                    no_batchcari="";tgl_kadaluarsacari="";no_fakturcari="";h_belicari=0;hargacari=0;sisacari=0;
                                    psbatch=koneksi.prepareStatement(
                                        "select data_batch.no_batch, data_batch.kode_brng, data_batch.tgl_beli, data_batch.tgl_kadaluarsa, data_batch.asal, data_batch.no_faktur, "+
                                        "data_batch.h_beli, data_batch.ralan, data_batch.kelas1, data_batch.kelas2, data_batch.kelas3, data_batch.utama, data_batch.vip, data_batch.vvip, data_batch.beliluar, "+
                                        "data_batch.jualbebas, data_batch.karyawan, data_batch.jumlahbeli, gudangbarang.stok,data_batch."+hppfarmasi+" as dasar from data_batch inner join gudangbarang "+
                                        "on data_batch.kode_brng=gudangbarang.kode_brng and data_batch.no_batch=gudangbarang.no_batch and data_batch.no_faktur=gudangbarang.no_faktur where "+
                                        "gudangbarang.stok>0 and data_batch.kode_brng=? and gudangbarang.kd_bangsal=? order by data_batch.tgl_kadaluarsa desc limit 1");
                                    try {
                                        psbatch.setString(1,rs2.getString("kode_brng"));
                                        psbatch.setString(2,kdgudang.getText());
                                        rsbatch=psbatch.executeQuery();
                                        while(rsbatch.next()){
                                            no_batchcari=rsbatch.getString("no_batch");
                                            tgl_kadaluarsacari=rsbatch.getString("tgl_kadaluarsa");
                                            no_fakturcari=rsbatch.getString("no_faktur");
                                            h_belicari=rsbatch.getDouble("dasar");
                                            if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                                                hargacari=rsbatch.getDouble("kelas1");
                                            }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                                                hargacari=rsbatch.getDouble("kelas2");
                                            }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                                                hargacari=rsbatch.getDouble("kelas3");
                                            }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                                                hargacari=rsbatch.getDouble("utama");
                                            }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                                                hargacari=rsbatch.getDouble("vip");
                                            }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                                                hargacari=rsbatch.getDouble("vvip");
                                            }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                                                hargacari=rsbatch.getDouble("beliluar");
                                            }else if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                                                hargacari=rsbatch.getDouble("karyawan");
                                            }                                 
                                            sisacari=rsbatch.getDouble("stok");
                                        }                                
                                    } catch (Exception e) {
                                        System.out.println("Notif : "+e);
                                    } finally{
                                        if(rsbatch!=null){
                                            rsbatch.close();
                                        }
                                        if(psbatch!=null){
                                            psbatch.close();
                                        }
                                    }
                                    if(rs2.getDouble("jml")>sisacari){
                                        JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                        tabModeDetailObatRacikan.addRow(new Object[]{
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),hargacari,h_belicari,
                                            rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                            sisacari,0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                            rs2.getString("golongan"),no_batchcari,no_fakturcari,tgl_kadaluarsacari                                     
                                        });  
                                    }else{
                                        tabModeDetailObatRacikan.addRow(new Object[]{
                                            rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                            rs2.getString("kode_sat"),hargacari,h_belicari,
                                            rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                            rs2.getDouble("jml"),0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                            rs2.getString("golongan"),no_batchcari,no_fakturcari,tgl_kadaluarsacari                                     
                                        });  
                                    }                                                             
                                }
                            } catch (Exception e) {
                                System.out.println("Notifikasi 3 : "+e);
                            } finally{
                                if(rs2!=null){
                                    rs2.close();
                                }
                                if(ps2!=null){
                                    ps2.close();
                                }
                            } 
                        }else{
                            ps2=koneksi.prepareStatement("select databarang.kode_brng,databarang.nama_brng,resep_dokter_racikan_detail.jml,resep_dokter_racikan_detail.kandungan,"+
                                "databarang.kode_sat,databarang.kelas1,databarang.kelas2,databarang.kelas3,databarang.utama,databarang.vip,databarang.vvip,databarang.beliluar,"+
                                "databarang.karyawan,databarang.h_beli,kategori_barang.nama as kategori,golongan_barang.nama as golongan,"+
                                "industrifarmasi.nama_industri,jenis.nama as jenis,databarang.kapasitas,databarang."+hppfarmasi+" as dasar from resep_dokter_racikan_detail inner join databarang inner join jenis "+
                                "inner join golongan_barang inner join kategori_barang inner join industrifarmasi on "+
                                "resep_dokter_racikan_detail.kode_brng=databarang.kode_brng and databarang.kdjns=jenis.kdjns "+
                                "and industrifarmasi.kode_industri=databarang.kode_industri and databarang.kode_golongan=golongan_barang.kode "+
                                "and databarang.kode_kategori=kategori_barang.kode where resep_dokter_racikan_detail.no_resep=? and "+
                                "resep_dokter_racikan_detail.no_racik=? order by databarang.kode_brng");
                            try {
                                ps2.setString(1,no_resep);
                                ps2.setString(2,rsobat.getString("no_racik"));
                                rs2=ps2.executeQuery();
                                while(rs2.next()){
                                    sisacari=0;
                                    psstok=koneksi.prepareStatement("select ifnull(gudangbarang.stok,'0') from gudangbarang where gudangbarang.kd_bangsal=? and gudangbarang.kode_brng=? and gudangbarang.no_batch='' and gudangbarang.no_faktur=''");
                                    try {
                                        psstok.setString(1,kdgudang.getText());
                                        psstok.setString(2,rs2.getString("kode_brng"));
                                        rsstok=psstok.executeQuery();
                                        if(rsstok.next()){
                                            sisacari=rsstok.getDouble(1);
                                        }                                
                                    } catch (Exception e) {
                                        sisacari=0;
                                        System.out.println("Notifikasi : "+e);
                                    }finally{
                                        if(rsstok != null){
                                            rsstok.close();
                                        }
                                        if(psstok != null){
                                            psstok.close();
                                        }
                                    }
                                    if(rs2.getDouble("jml")>sisacari){
					JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                                        if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                                            tabModeDetailObatRacikan.addRow(new Object[]{
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),rs2.getDouble("kelas1"),rs2.getDouble("dasar"),
                                                rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                                sisacari,0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                                rs2.getString("golongan"),"","",""                                    
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                                            tabModeDetailObatRacikan.addRow(new Object[]{
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),rs2.getDouble("kelas2"),rs2.getDouble("dasar"),
                                                rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                                sisacari,0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                                rs2.getString("golongan"),"","",""                                    
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                                            tabModeDetailObatRacikan.addRow(new Object[]{
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),rs2.getDouble("kelas3"),rs2.getDouble("dasar"),
                                                rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                                sisacari,0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                                rs2.getString("golongan"),"","",""                                    
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                                            tabModeDetailObatRacikan.addRow(new Object[]{
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),rs2.getDouble("utama"),rs2.getDouble("dasar"),
                                                rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                                sisacari,0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                                rs2.getString("golongan"),"","",""                                    
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                                            tabModeDetailObatRacikan.addRow(new Object[]{
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),rs2.getDouble("vip"),rs2.getDouble("dasar"),
                                                rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                                sisacari,0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                                rs2.getString("golongan"),"","",""                                    
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                                            tabModeDetailObatRacikan.addRow(new Object[]{
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),rs2.getDouble("vvip"),rs2.getDouble("dasar"),
                                                rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                                sisacari,0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                                rs2.getString("golongan"),"","",""                                    
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                                            tabModeDetailObatRacikan.addRow(new Object[]{
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),rs2.getDouble("beliluar"),rs2.getDouble("dasar"),
                                                rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                                sisacari,0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                                rs2.getString("golongan"),"","",""                                    
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                                            tabModeDetailObatRacikan.addRow(new Object[]{
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),rs2.getDouble("karyawan"),rs2.getDouble("dasar"),
                                                rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                                sisacari,0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                                rs2.getString("golongan"),"","",""                                    
                                            });
                                        } 
                                    }else{
                                        if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                                            tabModeDetailObatRacikan.addRow(new Object[]{
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),rs2.getDouble("kelas1"),rs2.getDouble("dasar"),
                                                rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                                rs2.getDouble("jml"),0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                                rs2.getString("golongan"),"","",""                                    
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                                            tabModeDetailObatRacikan.addRow(new Object[]{
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),rs2.getDouble("kelas2"),rs2.getDouble("dasar"),
                                                rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                                rs2.getDouble("jml"),0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                                rs2.getString("golongan"),"","",""                                    
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                                            tabModeDetailObatRacikan.addRow(new Object[]{
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),rs2.getDouble("kelas3"),rs2.getDouble("dasar"),
                                                rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                                rs2.getDouble("jml"),0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                                rs2.getString("golongan"),"","",""                                    
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                                            tabModeDetailObatRacikan.addRow(new Object[]{
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),rs2.getDouble("utama"),rs2.getDouble("dasar"),
                                                rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                                rs2.getDouble("jml"),0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                                rs2.getString("golongan"),"","",""                                    
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                                            tabModeDetailObatRacikan.addRow(new Object[]{
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),rs2.getDouble("vip"),rs2.getDouble("dasar"),
                                                rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                                rs2.getDouble("jml"),0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                                rs2.getString("golongan"),"","",""                                    
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                                            tabModeDetailObatRacikan.addRow(new Object[]{
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),rs2.getDouble("vvip"),rs2.getDouble("dasar"),
                                                rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                                rs2.getDouble("jml"),0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                                rs2.getString("golongan"),"","",""                                    
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                                            tabModeDetailObatRacikan.addRow(new Object[]{
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),rs2.getDouble("beliluar"),rs2.getDouble("dasar"),
                                                rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                                rs2.getDouble("jml"),0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                                rs2.getString("golongan"),"","",""                                    
                                            });
                                        }else if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                                            tabModeDetailObatRacikan.addRow(new Object[]{
                                                rsobat.getString("no_racik"),rs2.getString("kode_brng"),rs2.getString("nama_brng"),
                                                rs2.getString("kode_sat"),rs2.getDouble("karyawan"),rs2.getDouble("dasar"),
                                                rs2.getString("jenis"),sisacari,rs2.getDouble("kapasitas"),rs2.getString("kandungan"),
                                                rs2.getDouble("jml"),0,0,rs2.getString("nama_industri"),rs2.getString("kategori"),
                                                rs2.getString("golongan"),"","",""                                    
                                            });
                                        } 
                                    }                                                             
                                }
                            } catch (Exception e) {
                                System.out.println("Notifikasi 3 : "+e);
                            } finally{
                                if(rs2!=null){
                                    rs2.close();
                                }
                                if(ps2!=null){
                                    ps2.close();
                                }
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
                if(psobat!=null){
                    psobat.close();
                }
            }
            for(i=0;i<tbDetailObatRacikan.getRowCount();i++){
                getDatadetailobatracikan(i);
            }  
            aturTampilanDetailRacikanFarmasi(true);
            if(tbObatRacikan.getRowCount()>0 && tbObatRacikan.getSelectedRow()==-1){
                tbObatRacikan.setRowSelectionInterval(0,0);
            }
            setNoRacikAktifDariPilihan();
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }

    private void gabungkanDuplikatDetailRacikan() {
        try {
            for(int awal=0;awal<tbDetailObatRacikan.getRowCount();awal++){
                for(int banding=tbDetailObatRacikan.getRowCount()-1;banding>awal;banding--){
                    if(kunciDetailRacikan(awal).equals(kunciDetailRacikan(banding))){
                        double jumlahAwal=Valid.SetAngka(tbDetailObatRacikan.getValueAt(awal,10).toString());
                        double jumlahBanding=Valid.SetAngka(tbDetailObatRacikan.getValueAt(banding,10).toString());
                        tbDetailObatRacikan.setValueAt(""+(jumlahAwal+jumlahBanding),awal,10);
                        tabModeDetailObatRacikan.removeRow(tbDetailObatRacikan.convertRowIndexToModel(banding));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Gabung Detail Racikan DlgCariObat2 : "+e);
        }
    }

    private String kunciDetailRacikan(int baris) {
        return tbDetailObatRacikan.getValueAt(baris,0).toString()+"|"+
               tbDetailObatRacikan.getValueAt(baris,1).toString()+"|"+
               tbDetailObatRacikan.getValueAt(baris,16).toString()+"|"+
               tbDetailObatRacikan.getValueAt(baris,17).toString();
    }

    private boolean resepRacikanV2(String noResep) {
        try {
            int total=Sequel.cariInteger("select count(*) from resep_dokter_racikan_detail where no_resep=?",noResep);
            if(total==0){
                return false;
            }
            int totalV2=Sequel.cariInteger(
                "select count(*) from resep_dokter_racikan_detail where no_resep=? and ifnull(kandungan,'')='' and ifnull(p1,1)=1 and ifnull(p2,1)=1",
                noResep
            );
            return total==totalV2;
        } catch (Exception e) {
            System.out.println("Notifikasi Deteksi Racikan V2 DlgCariObat2 : "+e);
            return false;
        }
    }

    private void aturTampilanDetailRacikanFarmasi(boolean sederhana) {
        aturLebarKolomDetailRacikan(8, sederhana ? 0 : 40);
        aturLebarKolomDetailRacikan(9, sederhana ? 0 : 70);
        aturLebarKolomDetailRacikan(10, sederhana ? 70 : 40);
        aturLebarKolomDetailRacikan(11, sederhana ? 0 : 40);
        aturLebarKolomDetailRacikan(12, 50);
    }

    private void aturLebarKolomDetailRacikan(int kolom, int lebar) {
        try {
            TableColumn column=tbDetailObatRacikan.getColumnModel().getColumn(kolom);
            if(lebar==0){
                column.setMinWidth(0);
                column.setMaxWidth(0);
                column.setPreferredWidth(0);
            }else{
                column.setMinWidth(15);
                column.setMaxWidth(1000);
                column.setPreferredWidth(lebar);
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Kolom Detail Racikan DlgCariObat2 : "+e);
        }
    }
    
    private void getDatadetailobatracikan(int data) {       
        try {
            stokbarang=0;  
            if(aktifkanbatch.equals("yes")){
                psstok=koneksi.prepareStatement("select ifnull(gudangbarang.stok,'0') from gudangbarang where gudangbarang.kd_bangsal=? and gudangbarang.kode_brng=? and gudangbarang.no_batch=? and gudangbarang.no_faktur=?");
                try {
                    psstok.setString(1,kdgudang.getText());
                    psstok.setString(2,tbDetailObatRacikan.getValueAt(data,1).toString());
                    psstok.setString(3,tbDetailObatRacikan.getValueAt(data,16).toString());
                    psstok.setString(4,tbDetailObatRacikan.getValueAt(data,17).toString());
                    rsstok=psstok.executeQuery();
                    if(rsstok.next()){
                        stokbarang=rsstok.getDouble(1);
                    }                                
                } catch (Exception e) {
                    stokbarang=0;
                    System.out.println("Notifikasi : "+e);
                }finally{
                    if(rsstok != null){
                        rsstok.close();
                    }
                    if(psstok != null){
                        psstok.close();
                    }
                }
            }else{
                psstok=koneksi.prepareStatement("select ifnull(gudangbarang.stok,'0') from gudangbarang where gudangbarang.kd_bangsal=? and gudangbarang.kode_brng=? and gudangbarang.no_batch='' and gudangbarang.no_faktur=''");
                try {
                    psstok.setString(1,kdgudang.getText());
                    psstok.setString(2,tbDetailObatRacikan.getValueAt(data,1).toString());
                    rsstok=psstok.executeQuery();
                    if(rsstok.next()){
                        stokbarang=rsstok.getDouble(1);
                    }                                
                } catch (Exception e) {
                    stokbarang=0;
                    System.out.println("Notifikasi : "+e);
                }finally{
                    if(rsstok != null){
                        rsstok.close();
                    }
                    if(psstok != null){
                        psstok.close();
                    }
                }
            }

            tbDetailObatRacikan.setValueAt(stokbarang,data,7);
            y=0;
            try {
                y=Double.parseDouble(tbDetailObatRacikan.getValueAt(data,10).toString());
            } catch (Exception e) {
                y=0;
            }

            if(stokbarang<y){
                JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
            }               
        } catch (Exception e) {
            tbDetailObatRacikan.setValueAt(0,data,10);
            tbDetailObatRacikan.setValueAt(0,data,11);
            tbDetailObatRacikan.setValueAt(0,data,12);
        }           
    }
    
    private void cariBatch() {
        if(TabRawat.getSelectedIndex()==0){
            try {
                if(!tbObat.getValueAt(tbObat.getSelectedRow(),16).toString().equals("")){
                    ps2=koneksi.prepareStatement("select * from data_batch where data_batch.no_batch=? and data_batch.kode_brng=? and data_batch.sisa>0 order by data_batch.tgl_kadaluarsa limit 1");
                    try {
                        ps2.setString(1,tbObat.getValueAt(tbObat.getSelectedRow(),16).toString());
                        ps2.setString(2,tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
                        rs2=ps2.executeQuery();
                        if(rs2.next()){
                            tbObat.setValueAt(rs2.getString("no_faktur"), tbObat.getSelectedRow(),17);
                            tbObat.setValueAt(rs2.getString("tgl_kadaluarsa"), tbObat.getSelectedRow(),18);
                            tbObat.setValueAt(rs2.getDouble(hppfarmasi), tbObat.getSelectedRow(),12);
                            if(aktifkanbatch.equals("yes")){
                                if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                                    tbObat.setValueAt(rs2.getDouble("karyawan"), tbObat.getSelectedRow(),6);
                                }else if(Jeniskelas.getSelectedItem().equals("Rawat Jalan")){
                                    tbObat.setValueAt(rs2.getDouble("ralan"), tbObat.getSelectedRow(),6);
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                                    tbObat.setValueAt(rs2.getDouble("kelas1"), tbObat.getSelectedRow(),6);
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                                    tbObat.setValueAt(rs2.getDouble("kelas2"),tbObat.getSelectedRow(),6);
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                                    tbObat.setValueAt(rs2.getDouble("kelas3"), tbObat.getSelectedRow(),6);
                                }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                                    tbObat.setValueAt(rs2.getDouble("utama"), tbObat.getSelectedRow(),6);
                                }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                                    tbObat.setValueAt(rs2.getDouble("vip"), tbObat.getSelectedRow(),6);
                                }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                                    tbObat.setValueAt(rs2.getDouble("vvip"), tbObat.getSelectedRow(),6);
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(rs2!=null){
                            rs2.close();
                        }
                        if(ps2!=null){
                            ps2.close();
                        }
                    } 
                }
                
                try {
                    stokbarang=0;
                    if(aktifkanbatch.equals("yes")){
                        psstok=koneksi.prepareStatement("select ifnull(gudangbarang.stok,'0') from gudangbarang where gudangbarang.kd_bangsal=? and gudangbarang.kode_brng=? and gudangbarang.no_batch=? and gudangbarang.no_faktur=?");
                        try {
                            psstok.setString(1,kdgudang.getText());
                            psstok.setString(2,tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
                            psstok.setString(3,tbObat.getValueAt(tbObat.getSelectedRow(),16).toString());
                            psstok.setString(4,tbObat.getValueAt(tbObat.getSelectedRow(),17).toString());
                            rsstok=psstok.executeQuery();
                            if(rsstok.next()){
                                stokbarang=rsstok.getDouble(1);
                            }                                
                        } catch (Exception e) {
                            stokbarang=0;
                            System.out.println("Notifikasi : "+e);
                        }finally{
                            if(rsstok != null){
                                rsstok.close();
                            }
                            if(psstok != null){
                                psstok.close();
                            }
                        }
                    }else{
                        psstok=koneksi.prepareStatement("select ifnull(gudangbarang.stok,'0') from gudangbarang where gudangbarang.kd_bangsal=? and gudangbarang.kode_brng=? and gudangbarang.no_batch='' and gudangbarang.no_faktur=''");
                        try {
                            psstok.setString(1,kdgudang.getText());
                            psstok.setString(2,tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
                            rsstok=psstok.executeQuery();
                            if(rsstok.next()){
                                stokbarang=rsstok.getDouble(1);
                            }                                
                        } catch (Exception e) {
                            stokbarang=0;
                            System.out.println("Notifikasi : "+e);
                        }finally{
                            if(rsstok != null){
                                rsstok.close();
                            }
                            if(psstok != null){
                                psstok.close();
                            }
                        }
                    }

                    tbObat.setValueAt(stokbarang,tbObat.getSelectedRow(),10);
                    y=0;
                    try {
                        y=Double.parseDouble(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
                    } catch (Exception e) {
                        y=0;
                    }
                    if(stokbarang<y){
                        JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                        tbObat.setValueAt("",tbObat.getSelectedRow(),1);
                    }
                } catch (Exception e) {
                    tbObat.setValueAt(0,tbObat.getSelectedRow(),10);
                }
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            }
        }else if(TabRawat.getSelectedIndex()==1){
            try {
                if(!tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),16).toString().equals("")){
                    ps2=koneksi.prepareStatement("select * from data_batch where data_batch.no_batch=? and data_batch.kode_brng=? and data_batch.sisa>0 order by data_batch.tgl_kadaluarsa limit 1");
                    try {
                        ps2.setString(1,tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),16).toString());
                        ps2.setString(2,tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),1).toString());
                        rs2=ps2.executeQuery();
                        if(rs2.next()){
                            tbDetailObatRacikan.setValueAt(rs2.getString("no_faktur"), tbDetailObatRacikan.getSelectedRow(),17);
                            tbDetailObatRacikan.setValueAt(rs2.getString("tgl_kadaluarsa"), tbDetailObatRacikan.getSelectedRow(),18);
                            tbDetailObatRacikan.setValueAt(rs2.getDouble(hppfarmasi), tbDetailObatRacikan.getSelectedRow(),5);
                            if(aktifkanbatch.equals("yes")){
                                if(Jeniskelas.getSelectedItem().equals("Karyawan")){
                                    tbDetailObatRacikan.setValueAt(rs2.getDouble("karyawan"), tbDetailObatRacikan.getSelectedRow(),4);
                                }else if(Jeniskelas.getSelectedItem().equals("Beli Luar")){
                                    tbDetailObatRacikan.setValueAt(rs2.getDouble("beliluar"), tbDetailObatRacikan.getSelectedRow(),4);
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 1")){
                                    tbDetailObatRacikan.setValueAt(rs2.getDouble("kelas1"), tbDetailObatRacikan.getSelectedRow(),4);
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 2")){
                                    tbDetailObatRacikan.setValueAt(rs2.getDouble("kelas2"),tbDetailObatRacikan.getSelectedRow(),4);
                                }else if(Jeniskelas.getSelectedItem().equals("Kelas 3")){
                                    tbDetailObatRacikan.setValueAt(rs2.getDouble("kelas3"), tbDetailObatRacikan.getSelectedRow(),4);
                                }else if(Jeniskelas.getSelectedItem().equals("Utama/BPJS")){
                                    tbDetailObatRacikan.setValueAt(rs2.getDouble("utama"), tbDetailObatRacikan.getSelectedRow(),4);
                                }else if(Jeniskelas.getSelectedItem().equals("VIP")){
                                    tbDetailObatRacikan.setValueAt(rs2.getDouble("vip"), tbDetailObatRacikan.getSelectedRow(),4);
                                }else if(Jeniskelas.getSelectedItem().equals("VVIP")){
                                    tbDetailObatRacikan.setValueAt(rs2.getDouble("vvip"), tbDetailObatRacikan.getSelectedRow(),4);
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Notif : "+e);
                    } finally{
                        if(rs2!=null){
                            rs2.close();
                        }
                        if(ps2!=null){
                            ps2.close();
                        }
                    } 
                }
                
                try {
                    stokbarang=0;
                    if(aktifkanbatch.equals("yes")){
                        psstok=koneksi.prepareStatement("select ifnull(gudangbarang.stok,'0') from gudangbarang where gudangbarang.kd_bangsal=? and gudangbarang.kode_brng=? and gudangbarang.no_batch=? and gudangbarang.no_faktur=?");
                        try {
                            psstok.setString(1,kdgudang.getText());
                            psstok.setString(2,tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),1).toString());
                            psstok.setString(3,tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),16).toString());
                            psstok.setString(4,tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),17).toString());
                            rsstok=psstok.executeQuery();
                            if(rsstok.next()){
                                stokbarang=rsstok.getDouble(1);
                            }                                
                        } catch (Exception e) {
                            stokbarang=0;
                            System.out.println("Notifikasi : "+e);
                        }finally{
                            if(rsstok != null){
                                rsstok.close();
                            }
                            if(psstok != null){
                                psstok.close();
                            }
                        }
                    }else{
                        psstok=koneksi.prepareStatement("select ifnull(gudangbarang.stok,'0') from gudangbarang where gudangbarang.kd_bangsal=? and gudangbarang.kode_brng=? and gudangbarang.no_batch='' and gudangbarang.no_faktur=''");
                        try {
                            psstok.setString(1,kdgudang.getText());
                            psstok.setString(2,tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),1).toString());
                            rsstok=psstok.executeQuery();
                            if(rsstok.next()){
                                stokbarang=rsstok.getDouble(1);
                            }                                
                        } catch (Exception e) {
                            stokbarang=0;
                            System.out.println("Notifikasi : "+e);
                        }finally{
                            if(rsstok != null){
                                rsstok.close();
                            }
                            if(psstok != null){
                                psstok.close();
                            }
                        }
                    }

                    tbDetailObatRacikan.setValueAt(stokbarang,tbDetailObatRacikan.getSelectedRow(),7);
                    y=0;
                    try {
                        y=Double.parseDouble(tbDetailObatRacikan.getValueAt(tbDetailObatRacikan.getSelectedRow(),10).toString());
                    } catch (Exception e) {
                        y=0;
                    }
                    if(stokbarang<y){
                        JOptionPane.showMessageDialog(rootPane,"Maaf stok tidak mencukupi..!!");
                        tbDetailObatRacikan.setValueAt(0,tbDetailObatRacikan.getSelectedRow(),10);
                    }
                } catch (Exception e) {
                    tbDetailObatRacikan.setValueAt(0,tbDetailObatRacikan.getSelectedRow(),7);
                }
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            }
        }            
    }
    
    public void setPCare(String aktif,String nokunjung){
        aktifpcare=aktif;
        nokunjungan=nokunjung;
    }
    
    private void simpanObatPCare(String noKunjungan,String racikan,String kdObat,String signa1,String signa2,String jmlObat,String jmlPermintaan,String nmObatNonDPHO,String no_rawat,String tgl_perawatan,String jam,String kode_brng,String no_batch){
        try {
            headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-cons-id",koneksiDB.CONSIDAPIPCARE());
            utc=String.valueOf(api.GetUTCdatetimeAsString());
            headers.add("X-timestamp",utc);            
            headers.add("X-signature",api.getHmac());
            headers.add("X-authorization","Basic "+Base64.encodeBase64String(otorisasi.getBytes()));
            headers.add("user_key",koneksiDB.USERKEYAPIPCARE());
            requestJson ="{" +
                "\"kdObatSK\": 0," +
                "\"noKunjungan\": \""+noKunjungan+"\"," +
                "\"racikan\": "+racikan+"," +
                "\"kdRacikan\": null," +
                "\"obatDPHO\": true," +
                "\"kdObat\": \""+kdObat+"\"," +
                "\"signa1\": "+signa1+"," +
                "\"signa2\": "+signa2+"," +
                "\"jmlObat\": "+jmlObat+"," +
                "\"jmlPermintaan\": "+jmlPermintaan+"," +
                "\"nmObatNonDPHO\": \""+nmObatNonDPHO+"\"" +
             "}";
            System.out.println(requestJson);
            requestEntity = new HttpEntity(requestJson,headers);
            requestJson=api.getRest().exchange(URL+"/obat/kunjungan", HttpMethod.POST, requestEntity, String.class).getBody();
            System.out.println(requestJson);
            root = mapper.readTree(requestJson);
            nameNode = root.path("metaData");
            System.out.println("code : "+nameNode.path("code").asText());
            System.out.println("message : "+nameNode.path("message").asText()); 
            if(nameNode.path("code").asText().equals("201")){
                response = mapper.readTree(api.Decrypt(root.path("response").asText(),utc));
                kdObatSK="";
                if(response.isArray()){
                    for(JsonNode list:response){
                        if(list.path("field").asText().equals("kdObatSK")){
                            kdObatSK=list.path("message").asText();
                        }
                    }
                }
                Sequel.menyimpan2("pcare_obat_diberikan","?,?,?,?,?,?,?",7,new String[]{
                    no_rawat,noKunjungan,kdObatSK,tgl_perawatan,jam,kode_brng,no_batch
                });
            }
        }catch (Exception ex) {
            System.out.println("Notifikasi Bridging : "+ex);
            if(ex.toString().contains("UnknownHostException")){
                JOptionPane.showMessageDialog(null,"Koneksi ke server PCare terputus...!");
            }else if(ex.toString().contains("500")){
                JOptionPane.showMessageDialog(null,"Server PCare baru ngambek broooh...!");
            }else if(ex.toString().contains("401")){
                JOptionPane.showMessageDialog(null,"Username/Password salah. Lupa password? Wani piro...!");
            }else if(ex.toString().contains("408")){
                JOptionPane.showMessageDialog(null,"Time out, hayati lelah baaaang...!");
            }else if(ex.toString().contains("424")){
                JOptionPane.showMessageDialog(null,"Ambil data masternya yang bener dong coy...!");
            }else if(ex.toString().contains("412")){
                JOptionPane.showMessageDialog(null,"Tidak sesuai kondisi. Aku, kamu end...!");
            }else if(ex.toString().contains("204")){
                JOptionPane.showMessageDialog(null,"Data tidak ditemukan...!");
            }
        } 
    }
}
