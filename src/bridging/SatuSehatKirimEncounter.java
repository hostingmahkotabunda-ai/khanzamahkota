/*
  by Mas Elkhanza
 */

package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import java.awt.Dimension;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;

/**
 *
 * @author dosen
 */
public final class SatuSehatKirimEncounter extends javax.swing.JDialog {
    private final javax.swing.JLabel statusEncounter=new javax.swing.JLabel("Hanya Ralan sudah bayar / Ranap sudah dipulangkan.");
    private final StringBuilder rincianEncounter=new StringBuilder();
    private final java.util.Map<String,String[]> waktuSelesaiEncounter=new java.util.HashMap<>();
    private int pilihanEncounterAwal;
    private int jumlahPilihanEncounter(){int total=0;for(int row=0;row<tbObat.getRowCount();row++)if(Boolean.TRUE.equals(tbObat.getValueAt(row,0)))total++;return total;}
    private void mulaiPengirimanEncounter(){pilihanEncounterAwal=jumlahPilihanEncounter();rincianEncounter.setLength(0);statusEncounter.setText("Memproses "+pilihanEncounterAwal+" Encounter...");statusEncounter.setToolTipText(null);}
    private void catatStatusEncounter(String message){
        if(rincianEncounter.length()<7000)rincianEncounter.append(message).append('\n');
        statusEncounter.setText("Encounter belum selesai. Rincian: arahkan penunjuk.");
        statusEncounter.setToolTipText("<html>"+rincianEncounter.toString().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\n","<br>")+"</html>");
    }
    private void selesaiPengirimanEncounter(){
        int pending=jumlahPilihanEncounter();
        if(pending>0&&rincianEncounter.length()==0)catatStatusEncounter("Ada baris yang belum memenuhi syarat Kirim/Update. Periksa NIK pasien/dokter dan ID Encounter; Kirim untuk ID kosong, Update untuk ID yang sudah ada.");
        statusEncounter.setText("Encounter: "+(pilihanEncounterAwal-pending)+" berhasil, "+pending+" belum selesai. Rincian: arahkan penunjuk.");
    }
    private boolean resumeRalanTersedia()throws Exception{
        try(PreparedStatement p=koneksi.prepareStatement("select count(*) from information_schema.columns where table_schema=database() and table_name='resume_medis_ralan_v2' and column_name in ('no_rawat','tgl_keluar','jam_keluar')");ResultSet r=p.executeQuery()){return r.next()&&r.getInt(1)==3;}
    }
    private boolean validasiKunjunganSelesai(int row){
        String noRawat=tbObat.getValueAt(row,2).toString();
        waktuSelesaiEncounter.remove(noRawat);
        try{
            String sql=SatuSehatEncounterCompletion.query("Ranap".equals(tbObat.getValueAt(row,13).toString()),false,resumeRalanTersedia());
            sql=sql.replace("order by reg_periksa.tgl_registrasi", "and reg_periksa.no_rawat=? order by reg_periksa.tgl_registrasi");
            try(PreparedStatement p=koneksi.prepareStatement(sql)){
                p.setString(1,"2014-06-03");p.setString(2,"9999-12-31");p.setString(3,noRawat);
                try(ResultSet r=p.executeQuery()){
                    if(!r.next())throw new IllegalArgumentException("Status terbaru belum memenuhi syarat selesai/pulang. Muat ulang daftar.");
                    String[] times={tanggalFhirUtc(r.getString("tgl_registrasi")+" "+r.getString("jam_reg")),tanggalFhirUtc(r.getString("mulai_layanan")),tanggalFhirUtc(r.getString("pulang"))};
                    SatuSehatEncounterCompletion.complete(mapper.createObjectNode(),times[0],times[1],times[2],java.time.Clock.systemUTC());
                    waktuSelesaiEncounter.put(noRawat,times);
                    return true;
                }
            }
        }catch(Exception e){catatStatusEncounter("Baris "+(row+1)+": "+e.getMessage());return false;}
    }
    private String lengkapiEncounterSelesai(String payload,int row)throws Exception{
        String[] times=waktuSelesaiEncounter.get(tbObat.getValueAt(row,2).toString());
        if(times==null)throw new IllegalArgumentException("Kunjungan belum diverifikasi selesai.");
        ObjectNode resource=(ObjectNode)mapper.readTree(payload);
        SatuSehatEncounterCompletion.complete(resource,times[0],times[1],times[2],java.time.Clock.systemUTC());
        try{
            resource.set("diagnosis",SatuSehatEncounterDiagnosis.load(koneksi,mapper,tbObat.getValueAt(row,2).toString(),tbObat.getValueAt(row,13).toString()));
        }catch(SatuSehatEncounterDiagnosis.NotReady e){
            if(!tbObat.getValueAt(row,15).toString().isEmpty())throw e;
            SatuSehatEncounterCompletion.initial(resource);
            catatStatusEncounter("Baris "+(row+1)+": Payload Encounter awal disiapkan untuk memperoleh ID. Setelah berhasil, kirim Condition lalu Update Encounter sampai finished. "+e.getMessage());
        }
        return mapper.writeValueAsString(resource);
    }

    private final DefaultTableModel tabMode;
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private Connection koneksi=koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;   
    private int i=0;
    private String link="",json="",iddokter="",idpasien="";
    private ApiSatuSehat api=new ApiSatuSehat();
    private HttpHeaders headers ;
    private HttpEntity requestEntity;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private JsonNode response;
    private SatuSehatCekNIK cekViaSatuSehat=new SatuSehatCekNIK();  
    private StringBuilder htmlContent;    
    private final JButton BtnANC=new JButton("Atur ANC");
    private final JButton BtnEpisodePTM=new JButton("Episode PTM");
    private static final Object ANC_EPISODE_LOCK=new Object();
    
    /** Creates new form DlgKamar
     * @param parent
     * @param modal */
    public SatuSehatKirimEncounter(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        statusEncounter.setPreferredSize(new Dimension(44,24));
        jPanel3.add(statusEncounter,java.awt.BorderLayout.SOUTH);
        jPanel3.setPreferredSize(new Dimension(44,158));
        BtnANC.setPreferredSize(new Dimension(100,30));
        BtnANC.setToolTipText("Tandai dan klasifikasikan kunjungan ANC");
        BtnANC.addActionListener(evt -> bukaPengaturanAnc());
        panelGlass8.add(BtnANC,3);
        BtnEpisodePTM.setPreferredSize(new Dimension(110,30));
        BtnEpisodePTM.addActionListener(evt -> bukaEpisodePtm());
        panelGlass8.add(BtnEpisodePTM,3);
        // Dua baris tetap agar tombol tambahan tidak terpotong pada jendela sempit.
        panelGlass8.setPreferredSize(new Dimension(44,84));
        panelGlass8.setLayout(new java.awt.GridLayout(2,4,5,5));

        this.setLocation(10,2);
        setSize(628,674);

        tabMode=new DefaultTableModel(null,new String[]{
                "P","Tanggal Registrasi","No.Rawat","No.RM","Nama Pasien","No.KTP Pasien","Kode Dokter","Nama Dokter",
                "No.KTP Dokter","Kode Poli","Nama Poli/Unit","ID Lokasi Unit","Stts Rawat","Stts Lanjut",
                "Tanggal Pulang","ID Encounter"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if (colIndex==0) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                 java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, 
                 java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, 
                 java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, 
                 java.lang.String.class
             };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbObat.setModel(tabMode);

        //tbKamar.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbKamar.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 16; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(20);
            }else if(i==1){
                column.setPreferredWidth(150);
            }else if(i==2){
                column.setPreferredWidth(105);
            }else if(i==3){
                column.setPreferredWidth(70);
            }else if(i==4){
                column.setPreferredWidth(150);
            }else if(i==5){
                column.setPreferredWidth(110);
            }else if(i==6){
                column.setPreferredWidth(80);
            }else if(i==7){
                column.setPreferredWidth(150);
            }else if(i==8){
                column.setPreferredWidth(110);
            }else if(i==9){
                column.setPreferredWidth(80);
            }else if(i==10){
                column.setPreferredWidth(140);
            }else if(i==11){
                column.setPreferredWidth(210);
            }else if(i==12){
                column.setPreferredWidth(63);
            }else if(i==13){
                column.setPreferredWidth(63);
            }else if(i==14){
                column.setPreferredWidth(150);
            }else if(i==15){
                column.setPreferredWidth(215);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));
        
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
        
        try {
            link=koneksiDB.URLFHIRSATUSEHAT();
        } catch (Exception e) {
            System.out.println("Notif : "+e);
        }  
        
        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTML.setEditable(true);
        LoadHTML.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(
                ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi2 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#323232;}"+
                ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi5 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#AA0000;}"+
                ".isi6 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#FF0000;}"+
                ".isi7 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#C8C800;}"+
                ".isi8 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#00AA00;}"+
                ".isi9 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#969696;}"
        );
        Document doc = kit.createDefaultDocument();
        LoadHTML.setDocument(doc);
    }
    
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        ppPilihSemua = new javax.swing.JMenuItem();
        ppBersihkan = new javax.swing.JMenuItem();
        LoadHTML = new widget.editorpane();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        BtnAll = new widget.Button();
        BtnKirim = new widget.Button();
        BtnUpdate = new widget.Button();
        BtnPrint = new widget.Button();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel15 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel17 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel16 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        ppPilihSemua.setBackground(new java.awt.Color(255, 255, 254));
        ppPilihSemua.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppPilihSemua.setForeground(new java.awt.Color(50, 50, 50));
        ppPilihSemua.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppPilihSemua.setText("Pilih Semua");
        ppPilihSemua.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppPilihSemua.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppPilihSemua.setName("ppPilihSemua"); // NOI18N
        ppPilihSemua.setPreferredSize(new java.awt.Dimension(150, 26));
        ppPilihSemua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppPilihSemuaActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppPilihSemua);

        ppBersihkan.setBackground(new java.awt.Color(255, 255, 254));
        ppBersihkan.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppBersihkan.setForeground(new java.awt.Color(50, 50, 50));
        ppBersihkan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppBersihkan.setText("Hilangkan Pilihan");
        ppBersihkan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppBersihkan.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppBersihkan.setName("ppBersihkan"); // NOI18N
        ppBersihkan.setPreferredSize(new java.awt.Dimension(150, 26));
        ppBersihkan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppBersihkanActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppBersihkan);

        LoadHTML.setBorder(null);
        LoadHTML.setName("LoadHTML"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(null);
        setIconImages(null);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Pengiriman Data Encounter Satu Sehat ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setComponentPopupMenu(jPopupMenu1);
        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbObat.setComponentPopupMenu(jPopupMenu1);
        tbObat.setName("tbObat"); // NOI18N
        Scroll.setViewportView(tbObat);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(44, 100));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(53, 23));
        panelGlass8.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass8.add(LCount);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('M');
        BtnAll.setText("Semua");
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(100, 30));
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
        panelGlass8.add(BtnAll);

        BtnKirim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/34.png"))); // NOI18N
        BtnKirim.setMnemonic('K');
        BtnKirim.setText("Kirim");
        BtnKirim.setToolTipText("Alt+K");
        BtnKirim.setName("BtnKirim"); // NOI18N
        BtnKirim.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKirim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKirimActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnKirim);

        BtnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/edit_f2.png"))); // NOI18N
        BtnUpdate.setMnemonic('U');
        BtnUpdate.setText("Update");
        BtnUpdate.setToolTipText("Alt+U");
        BtnUpdate.setName("BtnUpdate"); // NOI18N
        BtnUpdate.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnUpdateActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnUpdate);

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
        panelGlass8.add(BtnPrint);

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
        panelGlass8.add(BtnKeluar);

        jPanel3.add(panelGlass8, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel15.setText("Tgl.Registrasi :");
        jLabel15.setName("jLabel15"); // NOI18N
        jLabel15.setPreferredSize(new java.awt.Dimension(85, 23));
        panelGlass9.add(jLabel15);

        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "14-06-2023" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari1);

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("s.d.");
        jLabel17.setName("jLabel17"); // NOI18N
        jLabel17.setPreferredSize(new java.awt.Dimension(24, 23));
        panelGlass9.add(jLabel17);

        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "14-06-2023" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari2);

        jLabel16.setText("Key Word :");
        jLabel16.setName("jLabel16"); // NOI18N
        jLabel16.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(jLabel16);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(210, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('6');
        BtnCari.setToolTipText("Alt+6");
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
        panelGlass9.add(BtnCari);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnPrint,BtnKeluar);}
    }//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try{
            htmlContent = new StringBuilder();
            htmlContent.append(                             
                "<tr class='isi'>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Tanggal Registrasi</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.Rawat</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.RM</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nama Pasien</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.KTP Pasien</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kode Dokter</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nama Dokter</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.KTP Dokter</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kode Poli</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nama Poli/Unit</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>ID Lokasi Unit</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Stts Rawat</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Stts Lanjut</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Tanggal Pulang</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>ID Encounter</b></td>"+
                "</tr>"
            );
            for (i = 0; i < tabMode.getRowCount(); i++) {
                htmlContent.append(
                    "<tr class='isi'>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,1).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,2).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,3).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,4).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,5).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,6).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,7).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,8).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,9).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,10).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,11).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,12).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,13).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,14).toString()+"</td>"+
                        "<td valign='top'>"+tbObat.getValueAt(i,15).toString()+"</td>"+
                    "</tr>");
            }
            LoadHTML.setText(
                "<html>"+
                  "<table width='100%' border='0' align='center' cellpadding='1px' cellspacing='0' class='tbl_form'>"+
                   htmlContent.toString()+
                  "</table>"+
                "</html>"
            );

            File g = new File("file2.css");            
            BufferedWriter bg = new BufferedWriter(new FileWriter(g));
            bg.write(
                ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi2 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#323232;}"+
                ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi5 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#AA0000;}"+
                ".isi6 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#FF0000;}"+
                ".isi7 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#C8C800;}"+
                ".isi8 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#00AA00;}"+
                ".isi9 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#969696;}"
            );
            bg.close();

            File f = new File("DataSatuSehatEncounter.html");            
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));            
            bw.write(LoadHTML.getText().replaceAll("<head>","<head>"+
                        "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />"+
                        "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                            "<tr class='isi2'>"+
                                "<td valign='top' align='center'>"+
                                    "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
                                    akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                    akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                    "<font size='2' face='Tahoma'>DATA PENGIRIMAN SATU SEHAT ENCOUNTER<br><br></font>"+        
                                "</td>"+
                           "</tr>"+
                        "</table>")
            );
            bw.close();                         
            Desktop.getDesktop().browse(f.toURI());
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        this.setCursor(Cursor.getDefaultCursor());       
    }//GEN-LAST:event_BtnPrintActionPerformed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_UP){
            tbObat.requestFocus();
        }
    }//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        tampil();
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnCariActionPerformed(null);
        }else{
            Valid.pindah(evt,TCari,BtnPrint);
        }
    }//GEN-LAST:event_BtnCariKeyPressed

    private void BtnKirimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKirimActionPerformed
        mulaiPengirimanEncounter();
        for(i=0;i<tbObat.getRowCount();i++){
            if(tbObat.getValueAt(i,0).toString().equals("true")&&(!tbObat.getValueAt(i,5).toString().equals(""))&&(!tbObat.getValueAt(i,8).toString().equals(""))&&tbObat.getValueAt(i,15).toString().equals("")){
                try {
                    if(!validasiKunjunganSelesai(i))continue;
                    iddokter=cekViaSatuSehat.tampilIDParktisi(tbObat.getValueAt(i,8).toString());
                    idpasien=cekViaSatuSehat.tampilIDPasien(tbObat.getValueAt(i,5).toString());
                    try{
                        if(!validasiIdentitasEncounter(i))continue;
                        pastikanEpisodeAnc(tbObat.getValueAt(i,2).toString());
                        headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.add("Authorization", "Bearer "+api.TokenSatuSehat());
                        json = "{" +
                                    "\"resourceType\": \"Encounter\"," +
                                    "\"status\": \"arrived\"," +
                                    "\"class\": {" +
                                        "\"system\": \"http://terminology.hl7.org/CodeSystem/v3-ActCode\"," +
                                        "\"code\": \""+(tbObat.getValueAt(i,13).toString().equals("Ralan")?"AMB":"IMP")+"\"," +
                                        "\"display\": \""+(tbObat.getValueAt(i,13).toString().equals("Ralan")?"ambulatory":"inpatient encounter")+"\"" +
                                    "}," +
                                    "\"subject\": {" +
                                        "\"reference\": \"Patient/"+idpasien+"\"," +
                                        "\"display\": \""+tbObat.getValueAt(i,4).toString()+"\"" +
                                    "}" + episodeOfCareJson(tbObat.getValueAt(i,2).toString()) + "," +
                                    "\"participant\": [" +
                                        "{" +
                                            "\"type\": [" +
                                                "{" +
                                                    "\"coding\": [" +
                                                        "{" +
                                                            "\"system\": \"http://terminology.hl7.org/CodeSystem/v3-ParticipationType\"," +
                                                            "\"code\": \"ATND\"," +
                                                            "\"display\": \"attender\"" +
                                                        "}" +
                                                    "]" +
                                                "}" +
                                            "]," +
                                            "\"individual\": {" +
                                                "\"reference\": \"Practitioner/"+iddokter+"\"," +
                                                "\"display\": \""+tbObat.getValueAt(i,7).toString()+"\"" +
                                            "}" +
                                        "}" +
                                    "]," +
                                    "\"period\": {" +
                                        "\"start\": \""+tbObat.getValueAt(i,1).toString()+"\"" +
                                    "}," +
                                    "\"location\": [" +
                                        "{" +
                                            "\"location\": {" +
                                                "\"reference\": \"Location/"+tbObat.getValueAt(i,11).toString()+"\"," +
                                                "\"display\": \""+tbObat.getValueAt(i,10).toString()+"\"" +
                                            "}" +
                                        "}" +
                                    "]," +
                                    "\"statusHistory\": [" +
                                        "{" +
                                            "\"status\": \"arrived\"," +
                                            "\"period\": {" +
                                                "\"start\": \""+tbObat.getValueAt(i,1).toString()+"\"," +
                                                "\"end\": \""+tbObat.getValueAt(i,14).toString()+"\"" +
                                            "}" +
                                        "}" +
                                    "]," +
                                    "\"serviceProvider\": {" +
                                        "\"reference\": \"Organization/"+koneksiDB.IDSATUSEHAT()+"\"" +
                                    "}," +
                                    "\"identifier\": [" +
                                        identifierAncJson(tbObat.getValueAt(i,2).toString()) +
                                        "{" +
                                            "\"system\": \"http://sys-ids.kemkes.go.id/encounter/"+koneksiDB.IDSATUSEHAT()+"\"," +
                                            "\"value\": \""+tbObat.getValueAt(i,2).toString()+"\"" +
                                        "}" +
                                    "]" +
                                "}";
                        json=lengkapiEncounterSelesai(json,i);
                        requestEntity = new HttpEntity(json,headers);
                        json=api.getRest().exchange(link+"/Encounter", HttpMethod.POST, requestEntity, String.class).getBody();
                        root = mapper.readTree(json);
                        response = root.path("id");
                        if(!response.asText().equals("")){
                            if(Sequel.menyimpantf2("satu_sehat_encounter","?,?","No.Rawat",2,new String[]{
                                tbObat.getValueAt(i,2).toString(),response.asText()
                            })==true){
                                tbObat.setValueAt(response.asText(),i,15);
                                tbObat.setValueAt(false,i,0);
                            }
                        }
                    }catch(Exception e){
                        tampilkanKesalahanBridging(e);
                    }
                } catch (Exception e) {
                    tampilkanKesalahanBridging(e);
                }
            }
        }
        selesaiPengirimanEncounter();
    }//GEN-LAST:event_BtnKirimActionPerformed

    private void ppPilihSemuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppPilihSemuaActionPerformed
        for(i=0;i<tbObat.getRowCount();i++){
            tbObat.setValueAt(true,i,0);
        }
    }//GEN-LAST:event_ppPilihSemuaActionPerformed

    private void ppBersihkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppBersihkanActionPerformed
        for(i=0;i<tbObat.getRowCount();i++){
            tbObat.setValueAt(false,i,0);
        }
    }//GEN-LAST:event_ppBersihkanActionPerformed

    private void BtnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUpdateActionPerformed
        mulaiPengirimanEncounter();
        for(i=0;i<tbObat.getRowCount();i++){
            if(tbObat.getValueAt(i,0).toString().equals("true")&&(!tbObat.getValueAt(i,5).toString().equals(""))&&(!tbObat.getValueAt(i,8).toString().equals(""))&&(!tbObat.getValueAt(i,15).toString().equals(""))){
                try {
                    if(!validasiKunjunganSelesai(i))continue;
                    iddokter=cekViaSatuSehat.tampilIDParktisi(tbObat.getValueAt(i,8).toString());
                    idpasien=cekViaSatuSehat.tampilIDPasien(tbObat.getValueAt(i,5).toString());
                    try{
                        if(!validasiIdentitasEncounter(i))continue;
                        pastikanEpisodeAnc(tbObat.getValueAt(i,2).toString());
                        headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.add("Authorization", "Bearer "+api.TokenSatuSehat());
                        json = "{" +
                                    "\"resourceType\": \"Encounter\"," +
                                    "\"id\": \""+tbObat.getValueAt(i,15).toString()+"\"," +
                                    "\"identifier\": [" +
                                        identifierAncJson(tbObat.getValueAt(i,2).toString()) +
                                        "{" +
                                            "\"system\": \"http://sys-ids.kemkes.go.id/encounter/"+koneksiDB.IDSATUSEHAT()+"\"," +
                                            "\"value\": \""+tbObat.getValueAt(i,2).toString()+"\"" +
                                        "}" +
                                    "]," +
                                    "\"status\": \"arrived\"," +
                                    "\"class\": {" +
                                        "\"system\": \"http://terminology.hl7.org/CodeSystem/v3-ActCode\"," +
                                        "\"code\": \""+(tbObat.getValueAt(i,13).toString().equals("Ralan")?"AMB":"IMP")+"\"," +
                                        "\"display\": \""+(tbObat.getValueAt(i,13).toString().equals("Ralan")?"ambulatory":"inpatient encounter")+"\"" +
                                    "}," +
                                    "\"subject\": {" +
                                        "\"reference\": \"Patient/"+idpasien+"\"," +
                                        "\"display\": \""+tbObat.getValueAt(i,4).toString()+"\"" +
                                    "}" + episodeOfCareJson(tbObat.getValueAt(i,2).toString()) + "," +
                                    "\"participant\": [" +
                                        "{" +
                                            "\"type\": [" +
                                                "{" +
                                                    "\"coding\": [" +
                                                        "{" +
                                                            "\"system\": \"http://terminology.hl7.org/CodeSystem/v3-ParticipationType\"," +
                                                            "\"code\": \"ATND\"," +
                                                            "\"display\": \"attender\"" +
                                                        "}" +
                                                    "]" +
                                                "}" +
                                            "]," +
                                            "\"individual\": {" +
                                                "\"reference\": \"Practitioner/"+iddokter+"\"," +
                                                "\"display\": \""+tbObat.getValueAt(i,7).toString()+"\"" +
                                            "}" +
                                        "}" +
                                    "]," +
                                    "\"period\": {" +
                                        "\"start\": \""+tbObat.getValueAt(i,1).toString()+"\"" +
                                    "}," +
                                    "\"location\": [" +
                                        "{" +
                                            "\"location\": {" +
                                                "\"reference\": \"Location/"+tbObat.getValueAt(i,11).toString()+"\"," +
                                                "\"display\": \""+tbObat.getValueAt(i,10).toString()+"\"" +
                                            "}" +
                                        "}" +
                                    "]," +
                                    "\"statusHistory\": [" +
                                        "{" +
                                            "\"status\": \"arrived\"," +
                                            "\"period\": {" +
                                                "\"start\": \""+tbObat.getValueAt(i,1).toString()+"\"," +
                                                "\"end\": \""+tbObat.getValueAt(i,14).toString()+"\"" +
                                            "}" +
                                        "}" +
                                    "]," +
                                    "\"serviceProvider\": {" +
                                        "\"reference\": \"Organization/"+koneksiDB.IDSATUSEHAT()+"\"" +
                                    "}" +
                                "}";
                        json=lengkapiEncounterSelesai(json,i);
                        json=pertahankanEpisodeDanIdentifier(json,tbObat.getValueAt(i,15).toString());
                        requestEntity = new HttpEntity(json,headers);
                        json=api.getRest().exchange(link+"/Encounter/"+tbObat.getValueAt(i,15).toString(), HttpMethod.PUT, requestEntity, String.class).getBody();
                        tbObat.setValueAt(false,i,0);
                    }catch(Exception e){
                        tampilkanKesalahanBridging(e);
                    }
                } catch (Exception e) {
                    tampilkanKesalahanBridging(e);
                }
            }
        }
        selesaiPengirimanEncounter();
    }//GEN-LAST:event_BtnUpdateActionPerformed

    private boolean validasiIdentitasEncounter(int row){
        try{
            SatuSehatEncounterIdentity.requireIds(idpasien,iddokter);
            return true;
        }catch(IllegalArgumentException e){
            catatStatusEncounter("Baris "+(row+1)+": "+e.getMessage());
            return false;
        }
    }

    private void bukaPengaturanAnc() {
        if(!akses.getsatu_sehat_kirim_encounter())return;
        int baris=tbObat.getSelectedRow();
        if(baris<0){
            JOptionPane.showMessageDialog(this,"Pilih satu baris kunjungan terlebih dahulu.");
            return;
        }
        SatuSehatAturANC dialog=new SatuSehatAturANC(
                this,true,
                tbObat.getValueAt(baris,2).toString(),
                tbObat.getValueAt(baris,3).toString(),
                tbObat.getValueAt(baris,4).toString(),
                tbObat.getValueAt(baris,10).toString()
        );
        dialog.setVisible(true);
    }

    private void bukaEpisodePtm() {
        if(!akses.getsatu_sehat_kirim_encounter()||!akses.getsatu_sehat_kirim_condition())return;
        int row=tbObat.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Pilih satu kunjungan terlebih dahulu.");return;}
        java.util.List<String> ids=new java.util.ArrayList<>();
        java.util.List<String> labels=new java.util.ArrayList<>();
        java.util.List<String> codes=new java.util.ArrayList<>();
        java.util.List<String> diseases=new java.util.ArrayList<>();
        try(PreparedStatement p=koneksi.prepareStatement("select c.id_condition,c.kd_penyakit,p.nm_penyakit "
                +"from satu_sehat_condition c left join penyakit p on p.kd_penyakit=c.kd_penyakit "
                +"where c.no_rawat=? and c.id_condition is not null and c.id_condition<>'' order by c.kd_penyakit")){
            p.setString(1,tbObat.getValueAt(row,2).toString());
            try(ResultSet r=p.executeQuery()){while(r.next()){
                ids.add(r.getString(1));codes.add(r.getString(2));diseases.add(r.getString(3));
                labels.add(r.getString(2)+" - "+r.getString(3)+" ["+r.getString(1)+"]");
            }}
            if(ids.isEmpty()){JOptionPane.showMessageDialog(this,"Kirim Condition yang terkait terlebih dahulu melalui menu Condition.");return;}
            javax.swing.JComboBox<String> choice=new javax.swing.JComboBox<>(labels.toArray(new String[0]));
            if(JOptionPane.showConfirmDialog(this,choice,"Pilih Condition untuk episode PTM",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
            new SatuSehatKirimEpisodeOfCare(this,true,tbObat.getValueAt(row,2).toString(),tbObat.getValueAt(row,3).toString(),
                    tbObat.getValueAt(row,4).toString(),tbObat.getValueAt(row,5).toString(),ids.get(choice.getSelectedIndex()),
                    tbObat.getValueAt(row,1).toString(),codes.get(choice.getSelectedIndex()),diseases.get(choice.getSelectedIndex())).setVisible(true);
        }catch(Exception e){JOptionPane.showMessageDialog(this,"Gagal membuka episode PTM: "+e.getMessage());}
    }

    private void pastikanEpisodeAnc(String noRawat) throws Exception {
        synchronized(ANC_EPISODE_LOCK){pastikanEpisodeAncTerkunci(noRawat);}
    }

    private void pastikanEpisodeAncTerkunci(String noRawat) throws Exception {
        String idEpisode="";
        String identifier="";
        String hpht="";
        long idLokal=0;
        try (PreparedStatement psAnc=koneksi.prepareStatement(
                "select e.id_episode_kehamilan,e.identifier_lokal,e.hpht,e.status,e.no_rkm_medis,ifnull(e.id_episode,'') id_episode " +
                "from satu_sehat_kunjungan_anc k inner join satu_sehat_episode_kehamilan e " +
                "on e.id_episode_kehamilan=k.id_episode_kehamilan " +
                "where k.no_rawat=? limit 1")) {
            psAnc.setString(1,noRawat);
            try (ResultSet rsAnc=psAnc.executeQuery()) {
                if(!rsAnc.next()) return;
                idLokal=rsAnc.getLong("id_episode_kehamilan");
                identifier=rsAnc.getString("identifier_lokal");
                hpht=rsAnc.getString("hpht");
                idEpisode=rsAnc.getString("id_episode");
                String rm=Sequel.cariIsi("select no_rkm_medis from reg_periksa where no_rawat=?",noRawat);
                if(!rm.equals(rsAnc.getString("no_rkm_medis")))throw new Exception("Pasien episode ANC tidak sesuai kunjungan.");
                if(idEpisode.isEmpty()&&!"active".equals(rsAnc.getString("status")))throw new Exception("Episode ANC belum dikirim dan sudah tidak aktif.");
            }
        } catch (java.sql.SQLException e) {
            if(e.getErrorCode()==1146 || "42S02".equals(e.getSQLState())){
                System.out.println("Notifikasi ANC : tabel ANC belum tersedia, Encounter dikirim tanpa data ANC.");
                return;
            }
            throw e;
        }
        if(idpasien==null || idpasien.trim().isEmpty()){
            throw new Exception("ID IHS pasien belum tersedia untuk membuat EpisodeOfCare ANC.");
        }
        if(!idEpisode.isEmpty()){
            if(!idEpisode.matches("[A-Za-z0-9.-]{1,64}"))throw new Exception("ID episode ANC lokal tidak valid.");
            HttpHeaders h=new HttpHeaders();h.add("Authorization","Bearer "+api.TokenSatuSehat());
            JsonNode remote=mapper.readTree(api.getRest().exchange(link+"/EpisodeOfCare/"+idEpisode,HttpMethod.GET,new HttpEntity(h),String.class).getBody());
            SatuSehatEpisodeSupport.validateEpisode(remote,idpasien,koneksiDB.IDSATUSEHAT(),"ANC");
            if(!SatuSehatEpisodeSupport.sameHpht(remote,hpht))throw new Exception("HPHT episode ANC server tidak sesuai lokal.");
            return;
        }

        idEpisode=cariEpisodeAncAktif(idpasien,hpht);
        if(!idEpisode.isEmpty()){
            simpanIdEpisodeAnc(idLokal,idEpisode);
            return;
        }

        ObjectNode episode=SatuSehatEpisodeSupport.buildAnc(mapper,koneksiDB.IDSATUSEHAT(),idpasien,identifier,hpht);

        HttpHeaders headerAnc=new HttpHeaders();
        headerAnc.setContentType(MediaType.APPLICATION_JSON);
        headerAnc.add("Authorization","Bearer "+api.TokenSatuSehat());
        String payload=mapper.writeValueAsString(episode);
        System.out.println("URL EpisodeOfCare ANC : "+link+"/EpisodeOfCare");
        System.out.println("EpisodeOfCare ANC: payload tervalidasi, period/statusHistory berformat datetime UTC.");
        String hasil=api.getRest().exchange(link+"/EpisodeOfCare",HttpMethod.POST,
                new HttpEntity(payload,headerAnc),String.class).getBody();
        idEpisode=mapper.readTree(hasil).path("id").asText();
        if(idEpisode.isEmpty()){
            throw new Exception("SATUSEHAT tidak mengembalikan ID EpisodeOfCare ANC.");
        }
        simpanIdEpisodeAnc(idLokal,idEpisode);
    }

    private String cariEpisodeAncAktif(String idPasien,String hpht) throws Exception {
        String result="";
        java.util.List<JsonNode> entries=SatuSehatEpisodeSupport.search(api,link,
                "/EpisodeOfCare?patient="+URLEncoder.encode(idPasien,"UTF-8")+
                "&type=ANC&status=active&organization="+URLEncoder.encode(koneksiDB.IDSATUSEHAT(),"UTF-8"),mapper);
        for(JsonNode resource:entries){
            if(!SatuSehatEpisodeSupport.matchesEpisode(resource,idPasien,koneksiDB.IDSATUSEHAT(),"ANC"))continue;
            if(!"active".equals(resource.path("status").asText()))continue;
            if(!SatuSehatEpisodeSupport.sameHpht(resource,hpht))
                throw new Exception("Ada episode ANC aktif dengan HPHT berbeda. Tinjau episode sebelum mengirim.");
            String id=resource.path("id").asText();
            if(!result.isEmpty()&&!result.equals(id))throw new Exception("Lebih dari satu episode ANC sesuai. Tinjau duplikasi di server.");
            result=id;
        }
        return result;
    }
    private void simpanIdEpisodeAnc(long idLokal, String idEpisode) throws Exception {
        try (PreparedStatement psAnc=koneksi.prepareStatement(
                "update satu_sehat_episode_kehamilan set id_episode=? where id_episode_kehamilan=? and (id_episode is null or id_episode='' or id_episode=?)")) {
            psAnc.setString(1,idEpisode);
            psAnc.setLong(2,idLokal);
            psAnc.setString(3,idEpisode);
            if(psAnc.executeUpdate()!=1)throw new Exception("ID episode ANC berubah atau gagal disimpan. Tinjau lokal sebelum mengulang.");
        }
    }

    private void tampilkanKesalahanBridging(Exception e) {
        String message=e.getMessage();
        if(e instanceof org.springframework.web.client.HttpStatusCodeException){
            org.springframework.web.client.HttpStatusCodeException http=(org.springframework.web.client.HttpStatusCodeException)e;
            message="HTTP "+http.getStatusCode().value();
            try{
                JsonNode outcome=mapper.readTree(http.getResponseBodyAsString());
                for(JsonNode issue:outcome.path("issue"))message+=" "+issue.path("expression").toString()+" "+issue.path("details").path("text").asText()+" "+issue.path("diagnostics").asText();
            }catch(Exception ignored){}
        }
        catatStatusEncounter(message==null?e.getClass().getSimpleName():message);
    }
    private String identifierAncJson(String noRawat) throws Exception {
        try (PreparedStatement psAnc=koneksi.prepareStatement(
                "select status_kunjungan from satu_sehat_kunjungan_anc where no_rawat=?")) {
            psAnc.setString(1,noRawat);
            try (ResultSet rsAnc=psAnc.executeQuery()) {
                if(rsAnc.next()){
                    SatuSehatEpisodeSupport.validateAncCode(rsAnc.getString("status_kunjungan"));
                    return "{\"system\":\"http://terminology.kemkes.go.id/CodeSystem/episodeofcare/ANC\","+
                           "\"value\":\""+rsAnc.getString("status_kunjungan")+"\"},";
                }
            }
        } catch (java.sql.SQLException e) {
            if(e.getErrorCode()!=1146&&!"42S02".equals(e.getSQLState()))throw e;
        }
        return "";
    }

    private String episodeOfCareJson(String noRawat) throws Exception {
        ObjectNode links=mapper.createObjectNode();
        String[] queries={
            "select p.id_episode from satu_sehat_encounter_episode p join satu_sehat_episode_of_care e on e.id_episode=p.id_episode join reg_periksa r on r.no_rawat=p.no_rawat and r.no_rkm_medis=e.no_rkm_medis where p.no_rawat=? order by p.id_episode",
            "select e.id_episode from satu_sehat_kunjungan_anc k join satu_sehat_episode_kehamilan e on e.id_episode_kehamilan=k.id_episode_kehamilan join reg_periksa r on r.no_rawat=k.no_rawat and r.no_rkm_medis=e.no_rkm_medis where k.no_rawat=? and e.id_episode is not null and e.id_episode<>''"
        };
        for(String query:queries){
            try(PreparedStatement p=koneksi.prepareStatement(query)){
                p.setString(1,noRawat);
                try(ResultSet r=p.executeQuery()){while(r.next())SatuSehatEpisodeSupport.addEpisode(links,r.getString(1));}
            }catch(java.sql.SQLException e){
                if(e.getErrorCode()!=1146&&!"42S02".equals(e.getSQLState()))throw e;
            }
        }
        return links.has("episodeOfCare") ? ",\"episodeOfCare\":"+mapper.writeValueAsString(links.path("episodeOfCare")) : "";
    }

    private String pertahankanEpisodeDanIdentifier(String payload,String idEncounter) throws Exception {
        if(!idEncounter.matches("[A-Za-z0-9.-]{1,64}"))throw new Exception("ID Encounter tidak valid.");
        JsonNode remote=mapper.readTree(api.getRest().exchange(link+"/Encounter/"+idEncounter,HttpMethod.GET,new HttpEntity(headers),String.class).getBody());
        ObjectNode local=(ObjectNode)mapper.readTree(payload);
        SatuSehatEpisodeSupport.preserveRemoteLinks(local,remote);
        String version=remote.path("meta").path("versionId").asText();
        if(!version.isEmpty())headers.add("If-Match","W/\""+version+"\"");
        return mapper.writeValueAsString(SatuSehatEncounterCompletion.mergeCompleted(local,remote));
    }
    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        tampil();
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            TCari.setText("");
            tampil();
        }else{
            Valid.pindah(evt, BtnPrint, BtnKeluar);
        }
    }//GEN-LAST:event_BtnAllKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            SatuSehatKirimEncounter dialog = new SatuSehatKirimEncounter(new javax.swing.JFrame(), true);
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
    private widget.Button BtnKirim;
    private widget.Button BtnPrint;
    private widget.Button BtnUpdate;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.Label LCount;
    private widget.editorpane LoadHTML;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel15;
    private widget.Label jLabel16;
    private widget.Label jLabel17;
    private widget.Label jLabel7;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private javax.swing.JMenuItem ppBersihkan;
    private javax.swing.JMenuItem ppPilihSemua;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables
    private String tanggalFhirUtc(String nilai) {
        if(nilai==null || nilai.trim().isEmpty()) return "";
        LocalDateTime waktuLokal=LocalDateTime.parse(nilai.trim(),DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss").withResolverStyle(java.time.format.ResolverStyle.STRICT));
        return waktuLokal.atZone(ZoneId.of("Asia/Makassar"))
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx"));
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        waktuSelesaiEncounter.clear();
        try {
            boolean resume=resumeRalanTersedia();
            for(boolean inpatient:new boolean[]{false,true}){
                try(PreparedStatement p=koneksi.prepareStatement(SatuSehatEncounterCompletion.query(inpatient,!TCari.getText().isEmpty(),resume))){
                    p.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+""));p.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
                    if(!TCari.getText().isEmpty())for(int n=3;n<=10;n++)p.setString(n,"%"+TCari.getText()+"%");
                    try(ResultSet r=p.executeQuery()){
                        while(r.next()){
                            tabMode.addRow(new Object[]{false,tanggalFhirUtc(r.getString("tgl_registrasi")+" "+r.getString("jam_reg")),r.getString("no_rawat"),r.getString("no_rkm_medis"),r.getString("nm_pasien"),
                                r.getString("no_ktp"),r.getString("kd_dokter"),r.getString("nama"),r.getString("ktpdokter"),r.getString("kd_poli"),r.getString("nm_poli"),r.getString("id_lokasi_satusehat"),r.getString("stts"),r.getString("status_lanjut"),tanggalFhirUtc(r.getString("pulang")),r.getString("id_encounter")});
                        }
                    }
                }
            }
            statusEncounter.setText("Ralan sudah bayar / Ranap sudah pulang. Waktu yang belum lengkap ditahan saat kirim.");
        }catch(Exception e){catatStatusEncounter("Daftar belum lengkap: "+e.getMessage());}
        LCount.setText(""+tabMode.getRowCount());
    }
    public void isCek(){
        BtnKirim.setEnabled(akses.getsatu_sehat_kirim_encounter());
        BtnPrint.setEnabled(akses.getsatu_sehat_kirim_encounter());
        BtnANC.setEnabled(akses.getsatu_sehat_kirim_encounter());
        BtnEpisodePTM.setEnabled(akses.getsatu_sehat_kirim_encounter()&&akses.getsatu_sehat_kirim_condition());
    }
    
    public JTable getTable(){
        return tbObat;
    }
}
