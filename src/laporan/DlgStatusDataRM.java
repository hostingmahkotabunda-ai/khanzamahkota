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
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.WaktuPeriksaRalan;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import simrskhanza.DlgCariCaraBayar;
import simrskhanza.DlgCariPoli;

/**
 *
 * @author perpustakaan
 */
public final class DlgStatusDataRM extends javax.swing.JDialog {
    private static final int KOLOM_KELENGKAPAN_AWAL=7;
    private static final String[] KODE_RALAN={
        "A01","A02","A03","A04","A05","A06",
        "B01","B02","B03","B04","B05","B06","B07","B08","B09",
        "C01","C02","C03","C04",
        "D01","D02","D03","D04","D05","D06",
        "E01","E02","E03","E04","E05","E06"
    };
    private static final String[] KODE_RANAP=new String[61];
    private static final String[] KOLOM_DASAR={"No.Rawat","Tanggal","Dokter Dituju","Nomer RM","Pasien","Poliklinik","Status"};
    private static final String[] KOLOM_WAKTU={"Jam Daftar","Jam Selesai","Lama Tunggu","Dasar Waktu"};
    private static final String[] KOLOM_RALAN={
        "A1 Nama Pasien","A2 Nomor RM","A3 Nomor SEP BPJS","A4 Poli Tujuan","A5 Tanggal Pelayanan","A6 Nama DPJP",
        "B1 Anamnesis","B2 Pemeriksaan Fisik","B3 Tanda Vital","B4 Diagnosa","B5 Diagnosis Sesuai ICD-10","B6 Terapi/Pengobatan","B7 Resep Obat","B8 Edukasi PX","B9 Rencana Kontrol",
        "C1 Permintaan Lab","C2 Hasil Lab","C3 Permintaan Radiologi","C4 Hasil Radiologi",
        "D1 CPPT Dokter","D2 CPPT Perawat","D3 CPPT PPA Lain","D4 Tanggal & Jam","D5 Nama PPA","D6 TTD PPA",
        "E1 SEP Valid","E2 Diagnosa Sesuai Pelayanan","E3 Indikasi Pemeriksaan","E4 Resume/CPPT","E5 Obat Sesuai Indikasi","E6 Kesesuaian Data Administrasi"
    };
    private static final String[] KOLOM_RANAP={
        "I1 Nama","I2 Tanggal Lahir","I3 JK","I4 Alamat","I5 Nomor Telepon","I6 Identitas PJ",
        "I7 Surat Pengantar Ranap","I8 Surat Rujukan","I9 Formulir Persetujuan Umum","I10 Identitas Kepesertaan BPJS/Asuransi","I11 General Consent",
        "I12 Assessment Awal Medis","I13 Assessment Awal Keperawatan","I14 Assessment Gizi","I15 Assessment Risiko Jatuh","I16 Assessment Nyeri",
        "I17 Anamnesis","I18 Pemeriksaan Fisik","I19 Diagnosis Awal","I20 Rencana Pelayanan","I21 Instruksi Dokter","I22 CPPT","I23 Catatan Konsul",
        "I24 Pengkajian","I25 Diagnosis Keperawatan","I26 Intervensi (Tindakan)","I27 Implementasi Tindakan","I28 Evaluasi","I29 Monitoring Tanda Vital",
        "I30 Catatan Pemberian Obat","I31 Instruksi Obat Dokter","I32 Dokumentasi Pemberian Obat",
        "I33 Permintaan Lab","I34 Hasil Lab","I35 Permintaan Radiologi","I36 Hasil Radiologi","I37 Hasil Pemeriksaan Penunjang",
        "I38 Informed Consent Tindakan","I39 Informed Consent Operasi","I40 Checklist Keselamatan Operasi","I41 Laporan Tindakan","I42 Laporan Anestesi","I43 Laporan Hasil Tindakan","I44 Laporan Kondisi Pasien",
        "I45 CPPT Dokter","I46 CPPT Perawat","I47 CPPT PPA Lain","I48 Tanggal & Jam","I49 Nama PPA","I50 TTD PPA",
        "I51 Edukasi Diagnosis","I52 Edukasi Tindakan","I53 Edukasi Penggunaan Obat","I54 Edukasi Keluarga Pasien",
        "I55 Kelengkapan Resume","I56 Diagnosis Akhir","I57 Kondisi Saat Pulang","I58 Terapi/Obat","I59 Instruksi Perawatan","I60 Jadwal Kontrol","I61 Surat Kontrol"
    };
    static{
        for(int x=0;x<KODE_RANAP.length;x++) KODE_RANAP[x]=String.format("I%02d",x+1);
    }
    private boolean sedangMemuat=false;
    private final Map<String,Boolean> perubahanBelumDisimpan=new java.util.LinkedHashMap<>();
    private widget.Button BtnSimpan;
    private final DefaultTableModel tabMode;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private DlgCariPoli poli=new DlgCariPoli(null,false);
    private DlgCariCaraBayar penjab=new DlgCariCaraBayar(null,false);
    private int i=0,adasoapiralan=0,tidakadasoapiralan=0,adasoapiranap=0,tidakadasoapiranap=0,adaresumeralan=0,tidakadaresumeralan=0,
            adaresumeranap=0,tidakadaresumeranap=0,adatriaseigd=0,tidakadatriaseigd=0,adaaskepigd=0,tidakadaaskepigd=0,adaicd10=0,tidakadaicd10=0,
            adaicd9=0,tidakadaicd9=0;  
    private String soapiralan="",soapiranap="",resumeralan="",resumeranap="",pilihan="",triaseigd="",askepigd="",icd10="",icd9="";
    private StringBuilder htmlContent;
    /** Creates new form DlgLhtBiaya
     * @param parent
     * @param modal */
    public DlgStatusDataRM(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        Status.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Ralan","Ranap"}));
        Status.setSelectedItem("Ralan");
        pasangTombolSimpan();
        this.setLocation(8,1);
        setSize(885,674);

        tabMode=new DefaultTableModel(null,kolomModelAktif()){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){
                 return colIndex>=KOLOM_KELENGKAPAN_AWAL && colIndex<KOLOM_KELENGKAPAN_AWAL+jumlahKelengkapanAktif();
             }
             @Override public Class<?> getColumnClass(int colIndex){
                 return colIndex>=KOLOM_KELENGKAPAN_AWAL && colIndex<KOLOM_KELENGKAPAN_AWAL+jumlahKelengkapanAktif() ? Boolean.class : String.class;
             }
        };
        tbBangsal.setModel(tabMode);

        tbBangsal.setPreferredScrollableViewportSize(new Dimension(800,800));
        tbBangsal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbBangsal.setFont(new java.awt.Font("Tahoma",java.awt.Font.PLAIN,12));
        tbBangsal.setRowHeight(Math.max(tbBangsal.getRowHeight(),29));
        tbBangsal.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tbBangsal.setRowSelectionAllowed(true);
        tbBangsal.setColumnSelectionAllowed(false);
        tbBangsal.setIntercellSpacing(new Dimension(1,1));

        for (i = 0; i < tabMode.getColumnCount(); i++) {
            TableColumn column = tbBangsal.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(105);
            }else if(i==1){
                column.setPreferredWidth(65);
            }else if(i==2){
                column.setPreferredWidth(150);
            }else if(i==3){
                column.setPreferredWidth(65);
            }else if(i==4){
                column.setPreferredWidth(150);   
            }else if(i==5){
                column.setPreferredWidth(130);
            }else if(i==6){
                column.setPreferredWidth(43);
            }else if(i>=KOLOM_KELENGKAPAN_AWAL && i<KOLOM_KELENGKAPAN_AWAL+jumlahKelengkapanAktif()){
                column.setPreferredWidth(82);
            }else if(i==KOLOM_KELENGKAPAN_AWAL+jumlahKelengkapanAktif()){
                column.setPreferredWidth(125);
            }else if(i==KOLOM_KELENGKAPAN_AWAL+jumlahKelengkapanAktif()+1){
                column.setPreferredWidth(125);
            }else if(i==KOLOM_KELENGKAPAN_AWAL+jumlahKelengkapanAktif()+2){
                column.setPreferredWidth(90);
            }else if(i==KOLOM_KELENGKAPAN_AWAL+jumlahKelengkapanAktif()+3){
                column.setPreferredWidth(105);
            }
        }
        tbBangsal.setDefaultRenderer(Object.class, new WarnaTable(){
            private final javax.swing.border.Border borderTerpilih=
                    javax.swing.BorderFactory.createMatteBorder(2,0,2,0,new java.awt.Color(255,193,7));
            private final javax.swing.border.Border borderNormal=
                    javax.swing.BorderFactory.createEmptyBorder(3,5,3,5);

            @Override public java.awt.Component getTableCellRendererComponent(JTable table,Object value,
                    boolean selected,boolean focus,int row,int column){
                java.awt.Component komponen=super.getTableCellRendererComponent(table,value,selected,focus,row,column);
                if(selected){
                    komponen.setBackground(new java.awt.Color(0,82,155));
                    komponen.setForeground(java.awt.Color.WHITE);
                    komponen.setFont(table.getFont().deriveFont(java.awt.Font.BOLD));
                    if(komponen instanceof javax.swing.JComponent){
                        ((javax.swing.JComponent)komponen).setBorder(borderTerpilih);
                    }
                }else{
                    komponen.setBackground(row%2==0 ? java.awt.Color.WHITE : new java.awt.Color(239,246,252));
                    komponen.setForeground(new java.awt.Color(34,52,69));
                    komponen.setFont(table.getFont().deriveFont(java.awt.Font.PLAIN));
                    if(komponen instanceof javax.swing.JComponent){
                        ((javax.swing.JComponent)komponen).setBorder(borderNormal);
                    }
                }
                return komponen;
            }
        });
        tbBangsal.setDefaultRenderer(Boolean.class, new RendererCeklis());
        tbBangsal.getTableHeader().setDefaultRenderer(new RendererHeaderKelompok());
        tbBangsal.getTableHeader().setPreferredSize(new Dimension(0,78));
        tbBangsal.getTableHeader().setReorderingAllowed(false);
        pasangHeaderBerkelompok();
        tabMode.addTableModelListener((TableModelEvent e) -> {
            if(!sedangMemuat && e.getType()==TableModelEvent.UPDATE && e.getFirstRow()>=0 &&
                    e.getColumn()>=KOLOM_KELENGKAPAN_AWAL && e.getColumn()<KOLOM_KELENGKAPAN_AWAL+jumlahKelengkapanAktif()){
                tandaiPerubahan(e.getFirstRow(),e.getColumn());
            }
        });
        Status.addActionListener((java.awt.event.ActionEvent evt) -> {
            if(tabMode!=null){
                konfigurasiKolomAktif();
                tampil();
            }
        });
        WaktuPeriksaRalan.pastikanTabel();
        
        TCari.setDocument(new batasInput((int)90).getKata(TCari));
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        tampil();
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        tampil();
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        tampil();
                    }
                }
            });
        }  
        
        poli.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(poli.getTable().getSelectedRow()!= -1){
                    kdpoli.setText(poli.getTable().getValueAt(poli.getTable().getSelectedRow(),0).toString());
                    nmpoli.setText(poli.getTable().getValueAt(poli.getTable().getSelectedRow(),1).toString());
                }      
                kdpoli.requestFocus();
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {poli.emptTeks();}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });   
        
        penjab.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(penjab.getTable().getSelectedRow()!= -1){
                    kdpenjab.setText(penjab.getTable().getValueAt(penjab.getTable().getSelectedRow(),1).toString());
                    nmpenjab.setText(penjab.getTable().getValueAt(penjab.getTable().getSelectedRow(),2).toString());
                }      
                kdpenjab.requestFocus();
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {penjab.emptTeks();}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });   
        
        penjab.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    penjab.dispose();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        
        ChkInput.setSelected(false);
        isForm();
    }    

    private void pasangTombolSimpan(){
        BtnSimpan=new widget.Button();
        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png")));
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Simpan perubahan checklist (Alt+S)");
        BtnSimpan.setName("BtnSimpan");
        BtnSimpan.setPreferredSize(new Dimension(100,30));
        BtnSimpan.setEnabled(false);
        BtnSimpan.addActionListener((java.awt.event.ActionEvent evt) -> simpanSemuaPerubahan());
        panelGlass5.add(BtnSimpan,Math.max(0,panelGlass5.getComponentCount()-2));
    }

    private boolean modeRanap(){
        return Status!=null && "Ranap".equals(Status.getSelectedItem());
    }

    private String[] kodeKelengkapanAktif(){
        return modeRanap()?KODE_RANAP:KODE_RALAN;
    }

    private String[] namaKelengkapanAktif(){
        return modeRanap()?KOLOM_RANAP:KOLOM_RALAN;
    }

    private int jumlahKelengkapanAktif(){
        return kodeKelengkapanAktif().length;
    }

    private Object[] kolomModelAktif(){
        String[] kelengkapan=namaKelengkapanAktif();
        Object[] kolom=new Object[KOLOM_DASAR.length+kelengkapan.length+KOLOM_WAKTU.length];
        int tujuan=0;
        for(String nama:KOLOM_DASAR) kolom[tujuan++]=nama;
        for(String nama:kelengkapan) kolom[tujuan++]=hapusNomorHeader(nama);
        for(String nama:KOLOM_WAKTU) kolom[tujuan++]=nama;
        return kolom;
    }

    private String hapusNomorHeader(String nama){
        return nama.replaceFirst("^[A-EI]\\d+\\s+","");
    }

    private void konfigurasiKolomAktif(){
        sedangMemuat=true;
        Valid.tabelKosong(tabMode);
        tabMode.setColumnIdentifiers(kolomModelAktif());
        tbBangsal.setModel(tabMode);
        for(int kolom=0;kolom<tabMode.getColumnCount();kolom++){
            TableColumn bagian=tbBangsal.getColumnModel().getColumn(kolom);
            if(kolom==0) bagian.setPreferredWidth(105);
            else if(kolom==1) bagian.setPreferredWidth(65);
            else if(kolom==2) bagian.setPreferredWidth(150);
            else if(kolom==3) bagian.setPreferredWidth(65);
            else if(kolom==4) bagian.setPreferredWidth(150);
            else if(kolom==5) bagian.setPreferredWidth(130);
            else if(kolom==6) bagian.setPreferredWidth(43);
            else if(kolom<KOLOM_KELENGKAPAN_AWAL+jumlahKelengkapanAktif()) bagian.setPreferredWidth(82);
            else if(kolom<KOLOM_KELENGKAPAN_AWAL+jumlahKelengkapanAktif()+2) bagian.setPreferredWidth(125);
            else if(kolom==KOLOM_KELENGKAPAN_AWAL+jumlahKelengkapanAktif()+2) bagian.setPreferredWidth(90);
            else bagian.setPreferredWidth(105);
        }
        tbBangsal.setDefaultRenderer(Boolean.class,new RendererCeklis());
        tbBangsal.getTableHeader().setDefaultRenderer(new RendererHeaderKelompok());
        pasangHeaderBerkelompok();
        sedangMemuat=false;
    }

    private void pasangHeaderBerkelompok(){
        javax.swing.JPanel headerGabungan=new javax.swing.JPanel(new java.awt.BorderLayout());
        headerGabungan.setOpaque(true);
        headerGabungan.add(new KopKelompok(),java.awt.BorderLayout.NORTH);
        headerGabungan.add(tbBangsal.getTableHeader(),java.awt.BorderLayout.CENTER);
        headerGabungan.setPreferredSize(new Dimension(lebarSeluruhKolom(),106));
        Scroll.setColumnHeaderView(headerGabungan);
    }

    private int lebarSeluruhKolom(){
        int lebar=0;
        for(int kolom=0;kolom<tbBangsal.getColumnModel().getColumnCount();kolom++){
            lebar+=tbBangsal.getColumnModel().getColumn(kolom).getPreferredWidth();
        }
        return lebar;
    }

    private class KopKelompok extends javax.swing.JComponent{
        KopKelompok(){
            setPreferredSize(new Dimension(lebarSeluruhKolom(),28));
            setOpaque(true);
        }

        @Override protected void paintComponent(java.awt.Graphics grafis){
            super.paintComponent(grafis);
            gambarKelompok(grafis,0,6,"DATA PASIEN",new java.awt.Color(220,230,237));
            if(modeRanap()){
                gambarKelompok(grafis,7,12,"IDENTITAS PASIEN",new java.awt.Color(255,174,0));
                gambarKelompok(grafis,13,17,"DOKUMENTASI PELAYANAN MEDIS",new java.awt.Color(239,145,145));
                gambarKelompok(grafis,18,22,"BERKAS ASSESSMENT AWAL",new java.awt.Color(255,216,105));
                gambarKelompok(grafis,23,29,"CATATAN DOKTER",new java.awt.Color(155,198,230));
                gambarKelompok(grafis,30,35,"CATATAN KEPERAWATAN",new java.awt.Color(190,220,240));
                gambarKelompok(grafis,36,38,"TERAPI & PENGOBATAN",new java.awt.Color(205,235,205));
                gambarKelompok(grafis,39,43,"PEMERIKSAAN PENUNJANG",new java.awt.Color(255,216,105));
                gambarKelompok(grafis,44,50,"TINDAKAN/OPERASI",new java.awt.Color(225,190,235));
                gambarKelompok(grafis,51,56,"CPPT & MONITORING",new java.awt.Color(155,198,230));
                gambarKelompok(grafis,57,60,"EDUKASI PASIEN",new java.awt.Color(255,225,170));
                gambarKelompok(grafis,61,67,"DISCHARGE/PEMULANGAN",new java.awt.Color(180,225,210));
                gambarKelompok(grafis,68,71,"WAKTU PELAYANAN",new java.awt.Color(220,230,237));
            }else{
                gambarKelompok(grafis,7,12,"A. IDENTITAS DAN ADMINISTRASI",new java.awt.Color(255,174,0));
                gambarKelompok(grafis,13,21,"B. DOKUMENTASI PELAYANAN MEDIS",new java.awt.Color(239,145,145));
                gambarKelompok(grafis,22,25,"C. PEMERIKSAAN PENUNJANG",new java.awt.Color(255,216,105));
                gambarKelompok(grafis,26,31,"D. CPPT & MONITORING",new java.awt.Color(155,198,230));
                gambarKelompok(grafis,32,37,"E. BERKAS KLAIM BPJS (DOUBLE CEK)",new java.awt.Color(244,196,202));
                gambarKelompok(grafis,38,41,"WAKTU PELAYANAN",new java.awt.Color(220,230,237));
            }
        }

        private void gambarKelompok(java.awt.Graphics grafis,int awal,int akhir,String judul,java.awt.Color warna){
            int x=0;
            for(int kolom=0;kolom<awal;kolom++) x+=tbBangsal.getColumnModel().getColumn(kolom).getWidth();
            int lebar=0;
            for(int kolom=awal;kolom<=akhir;kolom++) lebar+=tbBangsal.getColumnModel().getColumn(kolom).getWidth();
            grafis.setColor(warna);
            grafis.fillRect(x,0,lebar,getHeight());
            grafis.setColor(new java.awt.Color(120,120,120));
            grafis.drawRect(x,0,lebar-1,getHeight()-1);
            grafis.setColor(new java.awt.Color(35,35,35));
            grafis.setFont(new java.awt.Font("Tahoma",java.awt.Font.BOLD,10));
            java.awt.FontMetrics ukuran=grafis.getFontMetrics();
            int posisiX=x+Math.max(4,(lebar-ukuran.stringWidth(judul))/2);
            int posisiY=(getHeight()-ukuran.getHeight())/2+ukuran.getAscent();
            java.awt.Shape potonganLama=grafis.getClip();
            grafis.clipRect(x+1,1,Math.max(0,lebar-2),Math.max(0,getHeight()-2));
            grafis.drawString(judul,posisiX,posisiY);
            grafis.setClip(potonganLama);
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

        TKd = new widget.TextBox();
        kdpoli = new widget.TextBox();
        kdpenjab = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbBangsal = new widget.Table();
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
        nmpoli = new widget.TextBox();
        BtnSeek2 = new widget.Button();
        label19 = new widget.Label();
        nmpenjab = new widget.TextBox();
        BtnSeek3 = new widget.Button();
        jLabel18 = new widget.Label();
        Status = new widget.ComboBox();

        TKd.setForeground(new java.awt.Color(255, 255, 255));
        TKd.setName("TKd"); // NOI18N

        kdpoli.setEditable(false);
        kdpoli.setName("kdpoli"); // NOI18N
        kdpoli.setPreferredSize(new java.awt.Dimension(75, 23));
        kdpoli.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdpoliKeyPressed(evt);
            }
        });

        kdpenjab.setEditable(false);
        kdpenjab.setName("kdpenjab"); // NOI18N
        kdpenjab.setPreferredSize(new java.awt.Dimension(75, 23));
        kdpenjab.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdpenjabKeyPressed(evt);
            }
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Status Data Rekam Medis Pasien ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

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

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass5.setName("panelGlass5"); // NOI18N
        panelGlass5.setPreferredSize(new java.awt.Dimension(55, 55));
        panelGlass5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        label11.setText("Tanggal :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(50, 23));
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
        PanelInput.setPreferredSize(new java.awt.Dimension(192, 65));
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
        FormInput.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        label17.setText("Asal Poli :");
        label17.setName("label17"); // NOI18N
        label17.setPreferredSize(new java.awt.Dimension(60, 23));
        FormInput.add(label17);

        nmpoli.setEditable(false);
        nmpoli.setName("nmpoli"); // NOI18N
        nmpoli.setPreferredSize(new java.awt.Dimension(190, 23));
        FormInput.add(nmpoli);

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

        label19.setText("Cara Bayar :");
        label19.setName("label19"); // NOI18N
        label19.setPreferredSize(new java.awt.Dimension(100, 23));
        FormInput.add(label19);

        nmpenjab.setEditable(false);
        nmpenjab.setName("nmpenjab"); // NOI18N
        nmpenjab.setPreferredSize(new java.awt.Dimension(190, 23));
        FormInput.add(nmpenjab);

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

        jLabel18.setText("Status :");
        jLabel18.setName("jLabel18"); // NOI18N
        jLabel18.setPreferredSize(new java.awt.Dimension(75, 23));
        FormInput.add(jLabel18);

        Status.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Ralan", "Ranap" }));
        Status.setLightWeightPopupEnabled(false);
        Status.setName("Status"); // NOI18N
        Status.setPreferredSize(new java.awt.Dimension(92, 23));
        FormInput.add(Status);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        if(cetakKelengkapanDinamis()) return;
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            BtnPrint.requestFocus();
        }else if(tabMode.getRowCount()!=0){
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {            
                File g = new File("file2.css");            
                BufferedWriter bg = new BufferedWriter(new FileWriter(g));
                bg.write(
                        ".isi td{border-right: 1px solid #e2e7dd;font: 11px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                        ".isi2 td{font: 11px tahoma;height:12px;background: #ffffff;color:#323232;}"+                    
                        ".isi3 td{border-right: 1px solid #e2e7dd;font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                        ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
                );
                bg.close();

                File f;            
                BufferedWriter bw; 

                pilihan = (String)JOptionPane.showInputDialog(null,"Silahkan pilih laporan..!","Pilihan Cetak",JOptionPane.QUESTION_MESSAGE,null,new Object[]{"Laporan 1 (HTML)","Laporan 2 (WPS)","Laporan 3 (CSV)"},"Laporan 1 (HTML)");
                switch (pilihan) {
                    case "Laporan 1 (HTML)":
                            htmlContent = new StringBuilder();
                            htmlContent.append(                             
                                "<tr class='isi'>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='105px'>No.Rawat</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='65px'>Tanggal</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='150px'>Dokter Dituju</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='65px'>Nomer RM</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='150px'>Pasien</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='130px'>Poliklinik</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='43px'>Status</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='70px'>SOAPI Ralan</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='70px'>SOAPI Ranap</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='70px'>Resume Ralan</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='70px'>Resume Ranap</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='70px'>Triase IGD</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='70px'>Askep IGD</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='54px'>ICD 10</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='54px'>ICD 9</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center'>Jam Daftar</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center'>Jam Selesai</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center'>Lama Tunggu</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center'>Dasar Waktu</td>"+
                                "</tr>"
                            ); 
                            for(i=0;i<tabMode.getRowCount();i++){  
                                htmlContent.append(                             
                                    "<tr class='isi'>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,0)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,1)+"</td>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,2)+"</td>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,3)+"</td>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,4)+"</td>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,5)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,6)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,7)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,8)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,9)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,10)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,11)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,12)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,13)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,14)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,15)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,16)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,17)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,18)+"</td>"+
                                    "</tr>"
                                ); 
                            }            

                            f = new File("StatusDataRM.html");            
                            bw = new BufferedWriter(new FileWriter(f));            
                            bw.write("<html>"+
                                        "<head><link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" /></head>"+
                                        "<body>"+
                                            "<table width='1408px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                                "<tr class='isi2'>"+
                                                    "<td valign='top' align='center'>"+
                                                        "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
                                                        akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                                        akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                                        "<font size='2' face='Tahoma'>REKAP STATUS DATA RM PERIODE "+Tgl1.getSelectedItem()+" s.d. "+Tgl2.getSelectedItem()+"<br><br></font>"+        
                                                    "</td>"+
                                               "</tr>"+
                                            "</table>"+
                                            "<table width='1408px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                                htmlContent.toString()+
                                            "</table>"+
                                        "</body>"+                   
                                     "</html>"
                            );

                            bw.close();                         
                            Desktop.getDesktop().browse(f.toURI());
                        break;
                    case "Laporan 2 (WPS)":
                            htmlContent = new StringBuilder();
                            htmlContent.append(                             
                                "<tr class='isi'>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='105px'>No.Rawat</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='65px'>Tanggal</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='150px'>Dokter Dituju</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='65px'>Nomer RM</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='150px'>Pasien</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='130px'>Poliklinik</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='43px'>Status</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='70px'>SOAPI Ralan</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='70px'>SOAPI Ranap</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='70px'>Resume Ralan</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='70px'>Resume Ranap</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='70px'>Triase IGD</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='70px'>Askep IGD</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='54px'>ICD 10</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='54px'>ICD 9</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center'>Jam Daftar</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center'>Jam Selesai</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center'>Lama Tunggu</td>"+
                                    "<td valign='middle' bgcolor='#FFFAFA' align='center'>Dasar Waktu</td>"+
                                "</tr>"
                            ); 
                            for(i=0;i<tabMode.getRowCount();i++){  
                                htmlContent.append(                             
                                    "<tr class='isi'>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,0)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,1)+"</td>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,2)+"</td>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,3)+"</td>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,4)+"</td>"+
                                        "<td valign='top'>"+tabMode.getValueAt(i,5)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,6)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,7)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,8)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,9)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,10)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,11)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,12)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,13)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,14)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,15)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,16)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,17)+"</td>"+
                                        "<td valign='top' align='center'>"+tabMode.getValueAt(i,18)+"</td>"+
                                    "</tr>"
                                ); 
                            }            

                            f = new File("StatusDataRM.wps");            
                            bw = new BufferedWriter(new FileWriter(f));            
                            bw.write("<html>"+
                                        "<head><link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" /></head>"+
                                        "<body>"+
                                            "<table width='1408px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                                "<tr class='isi2'>"+
                                                    "<td valign='top' align='center'>"+
                                                        "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
                                                        akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                                        akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                                        "<font size='2' face='Tahoma'>REKAP STATUS DATA RM PERIODE "+Tgl1.getSelectedItem()+" s.d. "+Tgl2.getSelectedItem()+"<br><br></font>"+        
                                                    "</td>"+
                                               "</tr>"+
                                            "</table>"+
                                            "<table width='1408px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                                htmlContent.toString()+
                                            "</table>"+
                                        "</body>"+                   
                                     "</html>"
                            );

                            bw.close();                         
                            Desktop.getDesktop().browse(f.toURI());
                        break;
                    case "Laporan 3 (CSV)":
                            htmlContent = new StringBuilder();
                            htmlContent.append(                             
                                "\"No.Rawat\";\"Tanggal\";\"Dokter Dituju\";\"Nomer RM\";\"Pasien\";\"Poliklinik\";\"Status\";\"SOAPI Ralan\";\"SOAPI Ranap\";\"Resume Ralan\";\"Resume Ranap\";\"Triase IGD\";\"Askep IGD\";\"ICD 10\";\"ICD 9\";\"Jam Daftar\";\"Jam Selesai\";\"Lama Tunggu\";\"Dasar Waktu\"\n"
                            ); 
                            for(i=0;i<tabMode.getRowCount();i++){  
                                htmlContent.append(                             
                                    "\""+tabMode.getValueAt(i,0)+"\";\""+tabMode.getValueAt(i,1)+"\";\""+tabMode.getValueAt(i,2)+"\";\""+tabMode.getValueAt(i,3)+"\";\""+tabMode.getValueAt(i,4)+"\";"+
                                    "\""+tabMode.getValueAt(i,5)+"\";\""+tabMode.getValueAt(i,6)+"\";\""+tabMode.getValueAt(i,7)+"\";\""+tabMode.getValueAt(i,8)+"\";\""+tabMode.getValueAt(i,9)+"\";"+
                                    "\""+tabMode.getValueAt(i,10)+"\";\""+tabMode.getValueAt(i,11)+"\";\""+tabMode.getValueAt(i,12)+"\";\""+tabMode.getValueAt(i,13)+"\";\""+tabMode.getValueAt(i,14)+"\";"+
                                    "\""+tabMode.getValueAt(i,15)+"\";\""+tabMode.getValueAt(i,16)+"\";\""+tabMode.getValueAt(i,17)+"\";\""+tabMode.getValueAt(i,18)+"\"\n"
                                ); 
                            }            

                            f = new File("StatusDataRM.csv");            
                            bw = new BufferedWriter(new FileWriter(f));            
                            bw.write(htmlContent.toString());

                            bw.close();                         
                            Desktop.getDesktop().browse(f.toURI());
                        break; 
                }                 
            } catch (Exception e) {
            }     
            this.setCursor(Cursor.getDefaultCursor());
        }
}//GEN-LAST:event_BtnPrintActionPerformed

    private boolean cetakKelengkapanDinamis(){
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, tidak ada data yang dapat dicetak.");
            return true;
        }
        Object jenis=JOptionPane.showInputDialog(null,"Silakan pilih format laporan.","Pilihan Cetak",
                JOptionPane.QUESTION_MESSAGE,null,new Object[]{"Laporan HTML","Laporan CSV"},"Laporan HTML");
        if(jenis==null) return true;
        try{
            boolean csv=jenis.toString().equals("Laporan CSV");
            File file=new File(csv?"StatusDataRM.csv":"StatusDataRM.html");
            try(BufferedWriter tulis=new BufferedWriter(new FileWriter(file))){
                if(csv){
                    for(int kolom=0;kolom<tabMode.getColumnCount();kolom++){
                        if(kolom>0) tulis.write(";");
                        tulis.write(nilaiCsv(tabMode.getColumnName(kolom)));
                    }
                    tulis.newLine();
                    for(int baris=0;baris<tabMode.getRowCount();baris++){
                        for(int kolom=0;kolom<tabMode.getColumnCount();kolom++){
                            if(kolom>0) tulis.write(";");
                            Object nilai=tabMode.getValueAt(baris,kolom);
                            tulis.write(nilaiCsv(nilai instanceof Boolean ? (Boolean.TRUE.equals(nilai)?"Ya":"Tidak") : String.valueOf(nilai)));
                        }
                        tulis.newLine();
                    }
                }else{
                    tulis.write("<html><head><meta charset='UTF-8'><style>body{font:11px Arial}table{border-collapse:collapse}th,td{border:1px solid #aaa;padding:4px;white-space:nowrap}th{background:#f0f5eb}</style></head><body>");
                    tulis.write("<h3>Status dan Kelengkapan Berkas Rekam Medis</h3><table><tr>");
                    for(int kolom=0;kolom<tabMode.getColumnCount();kolom++) tulis.write("<th>"+nilaiHtml(tabMode.getColumnName(kolom))+"</th>");
                    tulis.write("</tr>");
                    for(int baris=0;baris<tabMode.getRowCount();baris++){
                        tulis.write("<tr>");
                        for(int kolom=0;kolom<tabMode.getColumnCount();kolom++){
                            Object nilai=tabMode.getValueAt(baris,kolom);
                            tulis.write("<td>"+nilaiHtml(nilai instanceof Boolean ? (Boolean.TRUE.equals(nilai)?"&#10003;":"") : String.valueOf(nilai))+"</td>");
                        }
                        tulis.write("</tr>");
                    }
                    tulis.write("</table></body></html>");
                }
            }
            Desktop.getDesktop().browse(file.toURI());
        }catch(Exception e){
            JOptionPane.showMessageDialog(null,"Laporan gagal dibuat: "+e.getMessage());
        }
        return true;
    }

    private String nilaiCsv(String nilai){
        return "\""+(nilai==null?"":nilai.replace("\"","\"\""))+"\"";
    }

    private String nilaiHtml(String nilai){
        if(nilai==null) return "";
        return nilai.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;")
                .replace("&amp;#10003;","&#10003;");
    }

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            //Valid.pindah(evt, BtnHapus, BtnAll);
        }
}//GEN-LAST:event_BtnPrintKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        keluarJikaDiizinkan();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            keluarJikaDiizinkan();
        }else{Valid.pindah(evt,BtnKeluar,TKd);}
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void keluarJikaDiizinkan(){
        if(!perubahanBelumDisimpan.isEmpty()){
            int pilihanKeluar=JOptionPane.showConfirmDialog(null,
                    "Masih ada "+perubahanBelumDisimpan.size()+" perubahan yang belum disimpan. Tetap keluar?",
                    "Konfirmasi",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
            if(pilihanKeluar!=JOptionPane.YES_OPTION) return;
        }
        dispose();
    }

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
       tampil();
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
        kdpoli.setText("");
        nmpoli.setText("");
        kdpenjab.setText("");
        nmpenjab.setText("");
        Status.setSelectedItem("Ralan");
        tampil();
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnAllActionPerformed(null);
        }else{

        }
    }//GEN-LAST:event_BtnAllKeyPressed

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void kdpoliKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdpoliKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            Sequel.cariIsi("select poliklinik.nm_poli from poliklinik where poliklinik.kd_poli=?", nmpoli,kdpoli.getText());
        }else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnAll.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            Tgl2.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            BtnSeek2ActionPerformed(null);
        }
    }//GEN-LAST:event_kdpoliKeyPressed

    private void BtnSeek2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek2ActionPerformed
        poli.isCek();
        poli.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        poli.setLocationRelativeTo(internalFrame1);
        poli.setAlwaysOnTop(false);
        poli.setVisible(true);
    }//GEN-LAST:event_BtnSeek2ActionPerformed

    private void BtnSeek2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeek2KeyPressed
        //Valid.pindah(evt,DTPCari2,TCari);
    }//GEN-LAST:event_BtnSeek2KeyPressed

    private void kdpenjabKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdpenjabKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            Sequel.cariIsi("select penjab.png_jawab from penjab where penjab.kd_pj=?", nmpenjab,kdpenjab.getText());
        }else if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            Sequel.cariIsi("select penjab.png_jawab from penjab where penjab.kd_pj=?", nmpenjab,kdpenjab.getText());
            BtnAll.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            Sequel.cariIsi("select penjab.png_jawab from penjab where penjab.kd_pj=?", nmpenjab,kdpenjab.getText());
            Tgl2.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            BtnSeek2ActionPerformed(null);
        }
    }//GEN-LAST:event_kdpenjabKeyPressed

    private void BtnSeek3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSeek3ActionPerformed
        penjab.isCek();
        penjab.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        penjab.setLocationRelativeTo(internalFrame1);
        penjab.setAlwaysOnTop(false);
        penjab.setVisible(true);
    }//GEN-LAST:event_BtnSeek3ActionPerformed

    private void BtnSeek3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSeek3KeyPressed
        //Valid.pindah(evt,DTPCari2,TCari);
    }//GEN-LAST:event_BtnSeek3KeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgStatusDataRM dialog = new DlgStatusDataRM(new javax.swing.JFrame(), true);
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
    private widget.CekBox ChkInput;
    private widget.panelisi FormInput;
    private javax.swing.JPanel PanelInput;
    private widget.ScrollPane Scroll;
    private widget.ComboBox Status;
    private widget.TextBox TCari;
    private widget.TextBox TKd;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel18;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.TextBox kdpenjab;
    private widget.TextBox kdpoli;
    private widget.Label label11;
    private widget.Label label17;
    private widget.Label label18;
    private widget.Label label19;
    private widget.TextBox nmpenjab;
    private widget.TextBox nmpoli;
    private widget.panelisi panelGlass5;
    private widget.Table tbBangsal;
    // End of variables declaration//GEN-END:variables

    public void tampil(){        
        try{   
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); 
            sedangMemuat=true;
            Valid.tabelKosong(tabMode);   
            ps=koneksi.prepareStatement(
                "select reg_periksa.no_rawat,reg_periksa.tgl_registrasi,reg_periksa.jam_reg,dokter.nm_dokter,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,pasien.jk,pasien.alamat,pasien.no_tlp,reg_periksa.p_jawab,poliklinik.nm_poli,reg_periksa.status_lanjut, "+
                "(select min(timestamp(pr.tgl_perawatan,pr.jam_rawat)) from pemeriksaan_ralan pr where pr.no_rawat=reg_periksa.no_rawat) as waktu_soap, "+
                "ws.waktu_sudah,coalesce((select min(timestamp(pr.tgl_perawatan,pr.jam_rawat)) from pemeriksaan_ralan pr where pr.no_rawat=reg_periksa.no_rawat),ws.waktu_sudah) as waktu_selesai "+
                "from reg_periksa inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "+
                "left join waktu_sudah_periksa_ralan ws on ws.no_rawat=reg_periksa.no_rawat where  "+
                "concat(reg_periksa.kd_poli,poliklinik.nm_poli) like ? and concat(reg_periksa.kd_pj,penjab.png_jawab) like ? "+
                "and reg_periksa.tgl_registrasi between ? and ? and reg_periksa.status_lanjut like ? "+
                (TCari.getText().equals("")?"":"and (reg_periksa.no_rawat like ? or dokter.nm_dokter like ? or reg_periksa.no_rkm_medis like ? or "+
                "pasien.nm_pasien like ? or poliklinik.nm_poli like ? or penjab.png_jawab like ?) ")+"order by reg_periksa.tgl_registrasi");
            try {
                ps.setString(1,"%"+kdpoli.getText()+nmpoli.getText()+"%");
                ps.setString(2,"%"+kdpenjab.getText()+nmpenjab.getText()+"%");
                ps.setString(3,Valid.SetTgl(Tgl1.getSelectedItem()+""));
                ps.setString(4,Valid.SetTgl(Tgl2.getSelectedItem()+""));
                ps.setString(5,"%"+Status.getSelectedItem().toString().replaceAll("Semua","")+"%");
                if(!TCari.getText().trim().equals("")){
                    ps.setString(6,"%"+TCari.getText().trim()+"%");
                    ps.setString(7,"%"+TCari.getText().trim()+"%");
                    ps.setString(8,"%"+TCari.getText().trim()+"%");
                    ps.setString(9,"%"+TCari.getText().trim()+"%");
                    ps.setString(10,"%"+TCari.getText().trim()+"%");
                    ps.setString(11,"%"+TCari.getText().trim()+"%");
                }
                    
                rs=ps.executeQuery();
                adasoapiralan=0;tidakadasoapiralan=0;adasoapiranap=0;tidakadasoapiranap=0;adaresumeralan=0;tidakadaresumeralan=0;adaresumeranap=0;tidakadaresumeranap=0;
                adatriaseigd=0;tidakadatriaseigd=0;adaaskepigd=0;tidakadaaskepigd=0;adaicd10=0;tidakadaicd10=0;adaicd9=0;tidakadaicd9=0; 
                while(rs.next()){
                    String noRawat=rs.getString("no_rawat");
                    Boolean[] ceklis=ambilKelengkapan(noRawat,rs);
                    Object[] baris=new Object[KOLOM_DASAR.length+ceklis.length+KOLOM_WAKTU.length];
                    baris[0]=rs.getString("no_rawat"); baris[1]=rs.getString("tgl_registrasi");
                    baris[2]=rs.getString("nm_dokter"); baris[3]=rs.getString("no_rkm_medis");
                    baris[4]=rs.getString("nm_pasien"); baris[5]=rs.getString("nm_poli"); baris[6]=rs.getString("status_lanjut");
                    for(int x=0;x<ceklis.length;x++) baris[KOLOM_KELENGKAPAN_AWAL+x]=ceklis[x];
                    int waktu=KOLOM_KELENGKAPAN_AWAL+ceklis.length;
                    baris[waktu]=rs.getString("tgl_registrasi")+" "+rs.getString("jam_reg");
                    baris[waktu+1]=rs.getString("waktu_selesai")==null?"-":rs.getString("waktu_selesai");
                    baris[waktu+2]=formatLamaTunggu(rs.getString("tgl_registrasi")+" "+rs.getString("jam_reg"),rs.getString("waktu_selesai"));
                    baris[waktu+3]=rs.getString("waktu_soap")!=null?"SOAP Pertama":(rs.getString("waktu_sudah")!=null?"Klik Status Sudah":"Tidak Tersedia");
                    tabMode.addRow(baris);
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
            sedangMemuat=false;
            this.setCursor(Cursor.getDefaultCursor());
        }catch(Exception e){
            sedangMemuat=false;
            System.out.println("Notifikasi : "+e);
        }
    }

    private Boolean[] ambilKelengkapan(String noRawat, ResultSet dataRegistrasi){
        return modeRanap()?ambilKelengkapanRanap(noRawat,dataRegistrasi):ambilKelengkapanRalan(noRawat);
    }

    private Boolean[] ambilKelengkapanRalan(String noRawat){
        Boolean[] nilai=new Boolean[KODE_RALAN.length];
        for(int x=0;x<nilai.length;x++) nilai[x]=false;

        boolean soap=adaData("select count(*) from pemeriksaan_ralan where no_rawat=?",noRawat);
        boolean anamnesis=adaData("select count(*) from pemeriksaan_ralan where no_rawat=? and trim(keluhan)<>''",noRawat);
        boolean pemeriksaanFisik=adaData("select count(*) from pemeriksaan_ralan where no_rawat=? and trim(pemeriksaan)<>''",noRawat);
        boolean tandaVital=adaData("select count(*) from pemeriksaan_ralan where no_rawat=? and (trim(tensi)<>'' or trim(nadi)<>'' or trim(suhu_tubuh)<>'')",noRawat);
        boolean diagnosisSoap=adaData("select count(*) from pemeriksaan_ralan where no_rawat=? and trim(penilaian)<>''",noRawat);
        boolean terapi=adaData("select count(*) from pemeriksaan_ralan where no_rawat=? and trim(rtl)<>''",noRawat);
        boolean icd10=adaData("select count(*) from diagnosa_pasien where no_rawat=?",noRawat);
        boolean resep=adaData("select count(*) from resep_obat where no_rawat=?",noRawat);
        boolean sep=adaData("select count(*) from bridging_sep where no_rawat=? and trim(no_sep)<>''",noRawat);
        boolean permintaanLab=adaData("select count(*) from permintaan_lab where no_rawat=?",noRawat);
        boolean hasilLab=adaData("select count(*) from periksa_lab where no_rawat=?",noRawat) ||
                adaData("select count(*) from detail_periksa_lab where no_rawat=?",noRawat);
        boolean permintaanRad=adaData("select count(*) from permintaan_radiologi where no_rawat=?",noRawat);
        boolean hasilRad=adaData("select count(*) from periksa_radiologi where no_rawat=?",noRawat);
        boolean resume=adaData("select count(*) from resume_medis_ralan_v2 where no_rawat=?",noRawat);
        boolean rencanaKontrol=adaData("select count(*) from surat_kontrol where no_rawat=?",noRawat);
        boolean petugas=adaData("select count(*) from pemeriksaan_ralan where no_rawat=? and trim(nip)<>''",noRawat);

        // A. Identitas dan administrasi sesuai formulir review rawat jalan.
        nilai[0]=true; nilai[1]=true; nilai[2]=sep; nilai[3]=true; nilai[4]=true; nilai[5]=true;
        // B. Dokumentasi pelayanan medis.
        nilai[6]=anamnesis; nilai[7]=pemeriksaanFisik; nilai[8]=tandaVital; nilai[9]=diagnosisSoap;
        nilai[10]=icd10; nilai[11]=terapi || resep; nilai[12]=resep; nilai[14]=rencanaKontrol;
        // C. Pemeriksaan penunjang.
        nilai[15]=permintaanLab; nilai[16]=hasilLab; nilai[17]=permintaanRad; nilai[18]=hasilRad;
        // D. CPPT & monitoring. Profesi spesifik tetap manual sampai sumber profesinya dipastikan.
        nilai[19]=soap; nilai[22]=soap; nilai[23]=petugas;
        // E. Double check klaim BPJS.
        nilai[25]=sep; nilai[26]=icd10; nilai[27]=permintaanLab || permintaanRad;
        nilai[28]=resume || soap; nilai[29]=resep;

        return terapkanOverride(noRawat,nilai,KODE_RALAN);
    }

    private Boolean[] ambilKelengkapanRanap(String noRawat,ResultSet registrasi){
        Boolean[] nilai=new Boolean[KODE_RANAP.length];
        for(int x=0;x<nilai.length;x++) nilai[x]=false;
        try{
            nilai[0]=terisi(registrasi.getString("nm_pasien"));
            nilai[1]=terisi(registrasi.getString("tgl_lahir"));
            nilai[2]=terisi(registrasi.getString("jk"));
            nilai[3]=terisi(registrasi.getString("alamat"));
            nilai[4]=terisi(registrasi.getString("no_tlp"));
            nilai[5]=terisi(registrasi.getString("p_jawab"));
        }catch(Exception e){}

        boolean sep=adaData("select count(*) from bridging_sep where no_rawat=? and trim(no_sep)<>''",noRawat);
        boolean persetujuanUmum=adaData("select count(*) from surat_persetujuan_umum where no_rawat=?",noRawat);
        boolean asesmenMedis=adaSalahSatu(noRawat,
                "select count(*) from penilaian_medis_ranap where no_rawat=?",
                "select count(*) from penilaian_medis_ranap_kandungan where no_rawat=?",
                "select count(*) from ringkasan_riwayat_masuk where no_rawat=?");
        boolean asesmenKeperawatan=adaSalahSatu(noRawat,
                "select count(*) from penilaian_awal_keperawatan_ranap where no_rawat=?",
                "select count(*) from penilaian_awal_keperawatan_kebidanan_ranap where no_rawat=?",
                "select count(*) from asesmen_keperawatan_bayi where no_rawat=?",
                "select count(*) from asesmen_kebidanan where no_rawat=?",
                "select count(*) from penilaian_awal_keperawatan_ranap_anak where no_rawat=?",
                "select count(*) from asesmen_keperawatan_anak where no_rawat=?");
        boolean asesmenGizi=adaSalahSatu(noRawat,
                "select count(*) from skrining_nutrisi_dewasa where no_rawat=?",
                "select count(*) from skrining_nutrisi_anak where no_rawat=?",
                "select count(*) from skrining_nutrisi_lansia where no_rawat=?",
                "select count(*) from skrining_gizi where no_rawat=?",
                "select count(*) from asuhan_gizi where no_rawat=?",
                "select count(*) from monitoring_asuhan_gizi where no_rawat=?",
                "select count(*) from catatan_adime_gizi where no_rawat=?");
        boolean soap=adaData("select count(*) from pemeriksaan_ranap where no_rawat=?",noRawat);
        boolean catatanKeperawatan=adaData("select count(*) from catatan_keperawatan_ranap where no_rawat=?",noRawat);
        boolean resep=adaData("select count(*) from resep_obat where no_rawat=?",noRawat);
        boolean pemberianObat=adaData("select count(*) from detail_pemberian_obat where no_rawat=?",noRawat);
        boolean permintaanLab=adaData("select count(*) from permintaan_lab where no_rawat=?",noRawat);
        boolean hasilLab=adaData("select count(*) from periksa_lab where no_rawat=?",noRawat) || adaData("select count(*) from detail_periksa_lab where no_rawat=?",noRawat);
        boolean permintaanRad=adaData("select count(*) from permintaan_radiologi where no_rawat=?",noRawat);
        boolean hasilRad=adaData("select count(*) from periksa_radiologi where no_rawat=?",noRawat);
        boolean operasi=adaData("select count(*) from operasi where no_rawat=?",noRawat);
        boolean laporanOperasi=adaData("select count(*) from laporan_operasi where no_rawat=?",noRawat);
        boolean checklistOperasi=adaSalahSatu(noRawat,
                "select count(*) from signin_sebelum_anestesi where no_rawat=?",
                "select count(*) from timeout_sebelum_insisi where no_rawat=?",
                "select count(*) from signout_sebelum_menutup_luka where no_rawat=?");
        boolean resume=adaData("select count(*) from resume_medis_ranap_v2 where no_rawat=?",noRawat);
        boolean diagnosisAkhir=adaData("select count(*) from diagnosa_pasien where no_rawat=? and status='Ranap'",noRawat) ||
                adaData("select count(*) from resume_medis_ranap_v2 where no_rawat=? and trim(diagnosa_keluar)<>''",noRawat);
        boolean suratKontrol=adaData("select count(*) from surat_kontrol where no_rawat=?",noRawat);
        boolean asesmenNyeri=adaData("select count(*) from asesmen_ulang_nyeri where no_rawat=?",noRawat);

        nilai[6]=adaSalahSatu(noRawat,
                "select count(*) from pengantar_pasien_ranap where no_rawat=?",
                "select count(*) from lembar_transfer_pasien_internal where no_rawat=?");
        nilai[7]=adaData("select count(*) from rujuk_masuk where no_rawat=?",noRawat);
        nilai[8]=persetujuanUmum; nilai[9]=sep; nilai[10]=persetujuanUmum;
        nilai[11]=asesmenMedis; nilai[12]=asesmenKeperawatan; nilai[13]=asesmenGizi;
        nilai[15]=asesmenNyeri;
        nilai[16]=adaData("select count(*) from pemeriksaan_ranap where no_rawat=? and trim(keluhan)<>''",noRawat);
        nilai[17]=adaData("select count(*) from pemeriksaan_ranap where no_rawat=? and trim(pemeriksaan)<>''",noRawat);
        nilai[18]=adaData("select count(*) from pemeriksaan_ranap where no_rawat=? and trim(penilaian)<>''",noRawat);
        nilai[19]=adaData("select count(*) from pemeriksaan_ranap where no_rawat=? and trim(rtl)<>''",noRawat);
        nilai[20]=adaData("select count(*) from pemeriksaan_ranap where no_rawat=? and trim(instruksi)<>''",noRawat);
        nilai[21]=soap; nilai[22]=adaData("select count(*) from catatan_perawatan where no_rawat=?",noRawat);
        nilai[23]=asesmenKeperawatan; nilai[27]=catatanKeperawatan;
        nilai[28]=adaData("select count(*) from pemeriksaan_ranap where no_rawat=? and (trim(tensi)<>'' or trim(nadi)<>'' or trim(suhu_tubuh)<>'')",noRawat);
        nilai[29]=pemberianObat; nilai[30]=resep; nilai[31]=pemberianObat;
        nilai[32]=permintaanLab; nilai[33]=hasilLab; nilai[34]=permintaanRad; nilai[35]=hasilRad; nilai[36]=hasilLab||hasilRad;
        nilai[37]=operasi; nilai[38]=operasi; nilai[39]=checklistOperasi; nilai[40]=laporanOperasi;
        nilai[42]=laporanOperasi; nilai[43]=laporanOperasi;
        nilai[44]=soap; nilai[47]=soap;
        nilai[48]=adaData("select count(*) from pemeriksaan_ranap where no_rawat=? and trim(nip)<>''",noRawat);
        nilai[54]=resume; nilai[55]=diagnosisAkhir;
        nilai[56]=adaData("select count(*) from resume_medis_ranap_v2 where no_rawat=? and trim(keadaan_keluar)<>''",noRawat);
        nilai[57]=adaData("select count(*) from resume_medis_ranap_v2 where no_rawat=? and trim(terapi_obat)<>''",noRawat) || resep;
        nilai[58]=adaData("select count(*) from resume_medis_ranap_v2 where no_rawat=? and trim(instruksi_tindak_lanjut)<>''",noRawat);
        nilai[59]=adaData("select count(*) from resume_medis_ranap_v2 where no_rawat=? and trim(tanggal_kontrol)<>''",noRawat) || suratKontrol;
        nilai[60]=suratKontrol;
        return terapkanOverride(noRawat,nilai,KODE_RANAP);
    }

    private Boolean[] terapkanOverride(String noRawat,Boolean[] nilai,String[] kode){
        Map<String,Boolean> manual=ambilOverrideManual(noRawat);
        for(int x=0;x<kode.length;x++){
            if(manual.containsKey(kode[x])) nilai[x]=manual.get(kode[x]);
            String kunci=noRawat+"|"+kode[x];
            if(perubahanBelumDisimpan.containsKey(kunci)) nilai[x]=perubahanBelumDisimpan.get(kunci);
        }
        return nilai;
    }

    private boolean terisi(String nilai){
        return nilai!=null && !nilai.trim().equals("") && !nilai.trim().equals("-");
    }

    private boolean adaSalahSatu(String noRawat,String... daftarSql){
        for(String sql:daftarSql) if(adaData(sql,noRawat)) return true;
        return false;
    }

    private boolean adaData(String sql, String noRawat){
        try(PreparedStatement cek=koneksi.prepareStatement(sql)){
            cek.setString(1,noRawat);
            try(ResultSet hasil=cek.executeQuery()){
                return hasil.next() && hasil.getInt(1)>0;
            }
        }catch(Exception e){
            return false;
        }
    }

    private Map<String,Boolean> ambilOverrideManual(String noRawat){
        Map<String,Boolean> hasil=new HashMap<>();
        try(PreparedStatement cek=koneksi.prepareStatement(
                "select kode_komponen,nilai from kelengkapan_berkas_rm where no_rawat=?")){
            cek.setString(1,noRawat);
            try(ResultSet data=cek.executeQuery()){
                while(data.next()) hasil.put(data.getString(1),data.getString(2).equals("1"));
            }
        }catch(Exception e){
            // Tabel dibuat melalui sql/kelengkapan_berkas_rm.sql sebelum fitur manual digunakan.
        }
        return hasil;
    }

    private void tandaiPerubahan(int baris, int kolom){
        if(baris>=tabMode.getRowCount()) return;
        String noRawat=String.valueOf(tabMode.getValueAt(baris,0));
        int indeks=kolom-KOLOM_KELENGKAPAN_AWAL;
        perubahanBelumDisimpan.put(noRawat+"|"+kodeKelengkapanAktif()[indeks],
                Boolean.TRUE.equals(tabMode.getValueAt(baris,kolom)));
        BtnSimpan.setEnabled(true);
        BtnSimpan.setText("Simpan ("+perubahanBelumDisimpan.size()+")");
    }

    private void simpanSemuaPerubahan(){
        if(perubahanBelumDisimpan.isEmpty()){
            JOptionPane.showMessageDialog(null,"Tidak ada perubahan checklist yang perlu disimpan.");
            return;
        }
        boolean autoCommit=true;
        try(PreparedStatement simpan=koneksi.prepareStatement(
                "insert into kelengkapan_berkas_rm(no_rawat,kode_komponen,nilai,diubah_oleh) values(?,?,?,?) "+
                "on duplicate key update nilai=values(nilai),diubah_oleh=values(diubah_oleh)")){
            autoCommit=koneksi.getAutoCommit();
            koneksi.setAutoCommit(false);
            for(Map.Entry<String,Boolean> perubahan:perubahanBelumDisimpan.entrySet()){
                int pemisah=perubahan.getKey().lastIndexOf('|');
                simpan.setString(1,perubahan.getKey().substring(0,pemisah));
                simpan.setString(2,perubahan.getKey().substring(pemisah+1));
                simpan.setString(3,Boolean.TRUE.equals(perubahan.getValue())?"1":"0");
                simpan.setString(4,akses.getkode());
                simpan.addBatch();
            }
            simpan.executeBatch();
            koneksi.commit();
            int jumlah=perubahanBelumDisimpan.size();
            perubahanBelumDisimpan.clear();
            BtnSimpan.setEnabled(false);
            BtnSimpan.setText("Simpan");
            JOptionPane.showMessageDialog(null,jumlah+" perubahan checklist berhasil disimpan.");
        }catch(Exception e){
            try{koneksi.rollback();}catch(Exception ex){}
            JOptionPane.showMessageDialog(null,
                    "Perubahan belum dapat disimpan. Jalankan sql/kelengkapan_berkas_rm.sql terlebih dahulu.\n"+e.getMessage());
        }finally{
            try{koneksi.setAutoCommit(autoCommit);}catch(Exception e){}
        }
    }

    private class RendererCeklis extends JCheckBox implements javax.swing.table.TableCellRenderer{
        private final javax.swing.border.Border borderTerpilih=
                javax.swing.BorderFactory.createMatteBorder(2,0,2,0,new java.awt.Color(255,193,7));
        private final javax.swing.border.Border borderNormal=
                javax.swing.BorderFactory.createEmptyBorder(3,5,3,5);
        RendererCeklis(){
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
            setFocusPainted(false);
        }
        @Override public java.awt.Component getTableCellRendererComponent(JTable table,Object value,
                boolean selected,boolean focus,int row,int column){
            setSelected(Boolean.TRUE.equals(value));
            setBackground(selected ? new java.awt.Color(0,82,155) :
                    (row%2==0 ? java.awt.Color.WHITE : new java.awt.Color(239,246,252)));
            setForeground(selected ? java.awt.Color.WHITE : new java.awt.Color(34,52,69));
            setBorder(selected ? borderTerpilih : borderNormal);
            return this;
        }
    }

    private class RendererHeaderKelompok extends DefaultTableCellRenderer{
        RendererHeaderKelompok(){
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            setFont(new java.awt.Font("Tahoma",java.awt.Font.PLAIN,9));
            setOpaque(true);
        }
        @Override public java.awt.Component getTableCellRendererComponent(JTable table,Object value,
                boolean selected,boolean focus,int row,int column){
            super.getTableCellRendererComponent(table,value,selected,focus,row,column);
            int model=table.convertColumnIndexToModel(column);
            String judul=String.valueOf(value);
            setText(model>=KOLOM_KELENGKAPAN_AWAL && model<KOLOM_KELENGKAPAN_AWAL+jumlahKelengkapanAktif() ?
                    bungkusJudulHeader(judul) : judul);
            setBackground(warnaHeaderKolom(model));
            setBorder(javax.swing.UIManager.getBorder("TableHeader.cellBorder"));
            return this;
        }
    }

    private java.awt.Color warnaHeaderKolom(int model){
        if(!modeRanap()){
            if(model>=7 && model<=12) return new java.awt.Color(255,174,0);
            if(model>=13 && model<=21) return new java.awt.Color(239,145,145);
            if(model>=22 && model<=25) return new java.awt.Color(255,216,105);
            if(model>=26 && model<=31) return new java.awt.Color(155,198,230);
            if(model>=32 && model<=37) return new java.awt.Color(244,196,202);
        }else{
            if(model>=7 && model<=12) return new java.awt.Color(255,174,0);
            if(model>=13 && model<=17) return new java.awt.Color(239,145,145);
            if(model>=18 && model<=22) return new java.awt.Color(255,216,105);
            if(model>=23 && model<=29) return new java.awt.Color(155,198,230);
            if(model>=30 && model<=35) return new java.awt.Color(190,220,240);
            if(model>=36 && model<=38) return new java.awt.Color(205,235,205);
            if(model>=39 && model<=43) return new java.awt.Color(255,216,105);
            if(model>=44 && model<=50) return new java.awt.Color(225,190,235);
            if(model>=51 && model<=56) return new java.awt.Color(155,198,230);
            if(model>=57 && model<=60) return new java.awt.Color(255,225,170);
            if(model>=61 && model<=67) return new java.awt.Color(180,225,210);
        }
        return new java.awt.Color(240,245,235);
    }

    private String bungkusJudulHeader(String judul){
        String[] kata=judul.split(" ");
        StringBuilder hasil=new StringBuilder("<html><center>");
        int panjangBaris=0;
        for(int x=0;x<kata.length;x++){
            if(panjangBaris>0 && panjangBaris+kata[x].length()+1>13){
                hasil.append("<br>");
                panjangBaris=0;
            }else if(panjangBaris>0){
                hasil.append(" ");
                panjangBaris++;
            }
            hasil.append(kata[x]);
            panjangBaris+=kata[x].length();
        }
        return hasil.append("</center></html>").toString();
    }

    private String formatLamaTunggu(String mulai, String selesai) {
        if(selesai==null || selesai.trim().equals("")){
            return "-";
        }
        try{
            long menit=java.time.Duration.between(
                    java.sql.Timestamp.valueOf(mulai).toLocalDateTime(),
                    java.sql.Timestamp.valueOf(selesai).toLocalDateTime()).toMinutes();
            if(menit<0){
                return "Data waktu tidak valid";
            }
            return (menit/60>0?(menit/60)+" jam ":"")+(menit%60)+" menit";
        }catch(Exception e){
            return "-";
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
            PanelInput.setPreferredSize(new Dimension(WIDTH,65));
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
