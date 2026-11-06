/*
  By Mas Elkhanza
 */

package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 *
 * @author dosen
 */
public final class SatuSehatKirimAllergyIntolerance extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private Connection koneksi=koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;   
    private int i=0;
    private String link="",idpasien="",idpraktisi="";
    private ApiSatuSehat api=new ApiSatuSehat();
    private ObjectMapper mapper = new ObjectMapper();
    private SatuSehatCekNIK cekViaSatuSehat=new SatuSehatCekNIK();  
    private StringBuilder htmlContent;   
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false; 

    private final SatuSehatAllergyMapping allergyMappings =
            new SatuSehatAllergyMapping(new File("./cache/alergisatusehat.iyem"));
    private final javax.swing.JButton BtnMappingAlergi = new javax.swing.JButton("Mapping Alergi");
    private final javax.swing.JButton BtnReferensiAlergi = new javax.swing.JButton("Cari Referensi Online");
    private final SatuSehatAllergyReferenceService allergyReferences=new SatuSehatAllergyReferenceService(
            new SatuSehatAllergyOnlineClient(koneksiDB.URLKFAV2SATUSEHAT(),() -> api.TokenSatuSehat()));

    private String normalisasiNIK(String nik){
        return nik==null ? "" : nik.trim();
    }
    private boolean validasiIdentitasSatuSehat(int baris){
        String nikPasien=normalisasiNIK(nilai(baris,5)), nikPraktisi=normalisasiNIK(nilai(baris,9));
        if(!nikPasien.matches("[0-9]{16}")||!nikPraktisi.matches("[0-9]{16}"))return false;
        idpasien=cekViaSatuSehat.tampilIDPasien(nikPasien);
        idpraktisi=cekViaSatuSehat.tampilIDParktisi(nikPraktisi);
        try { SatuSehatEncounterIdentity.requireIds(idpasien,idpraktisi); return true; }
        catch(IllegalArgumentException e){ return false; }
    }
    /** Creates new form DlgKamar
     * @param parent
     * @param modal */
    public SatuSehatKirimAllergyIntolerance(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        initComponents();

        this.setLocation(10,2);
        setSize(628,674);

        tabMode=new DefaultTableModel(null,new String[]{
                "P","Tanggal Registrasi","No.Rawat","No.RM","Nama Pasien","No.KTP Pasien","ID Encounter",
                "Alergi","Petugas/Praktisi","No.KTP Praktisi","Tanggal","ID Allergy Intolerance",
                "Status", "Status Mapping", "Verifikasi Alergi"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if (colIndex==0 || colIndex==14) {
                    a=true;
                }
                return a;
             }
             @Override
             public Class getColumnClass(int columnIndex) {
                return columnIndex==0 ? Boolean.class : String.class;
             }
        };
        tbObat.setModel(tabMode);
        BtnMappingAlergi.addActionListener(evt -> editMappingAlergi());
        panelGlass8.setLayout(new java.awt.GridLayout(3,3,5,5));
        panelGlass8.setPreferredSize(new Dimension(44,120));
        panelGlass8.add(BtnMappingAlergi);
                BtnReferensiAlergi.addActionListener(evt -> cariReferensiBaris());
        panelGlass8.add(BtnReferensiAlergi);
        tbObat.getColumnModel().getColumn(14).setCellEditor(new javax.swing.DefaultCellEditor(
                new javax.swing.JComboBox<String>(new String[]{"","unconfirmed","confirmed"})));
        jPanel3.setPreferredSize(new Dimension(44,180));

        //tbKamar.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbKamar.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 15; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(20);
            }else if(i==1){
                column.setPreferredWidth(110);
            }else if(i==2){
                column.setPreferredWidth(105);
            }else if(i==3){
                column.setPreferredWidth(70);
            }else if(i==4){
                column.setPreferredWidth(150);
            }else if(i==5){
                column.setPreferredWidth(110);
            }else if(i==6){
                column.setPreferredWidth(215);
            }else if(i==7){
                column.setPreferredWidth(250);
            }else if(i==8){
                column.setPreferredWidth(150);
            }else if(i==9){
                column.setPreferredWidth(120);
            }else if(i==10){
                column.setPreferredWidth(110);
            }else if(i==11){
                column.setPreferredWidth(220);
            }else if(i==12){
                column.setPreferredWidth(60);
            }else if(i==13){
                column.setPreferredWidth(360);
            }else if(i==14){
                column.setPreferredWidth(130);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));
        
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
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Pengiriman Allergy Intolerance Satu Sehat ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
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

        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "02-04-2026" }));
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

        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "02-04-2026" }));
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
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>ID Encounter</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Alergi</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Petugas/Praktisi</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.KTP Praktisi</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Tanggal</b></td>"+
                    "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>ID Allergy Intolerance</b></td>"+
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
                    "</tr>");
            }
            LoadHTML.setText(
                "<html>"+
                  "<table width='1500px' border='0' align='center' cellpadding='1px' cellspacing='0' class='tbl_form'>"+
                   htmlContent.toString()+
                  "</table>"+
                "</html>"
            );
            htmlContent=null;

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

            File f = new File("DataSatuSehatAllergyIntolerance.html");            
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));            
            bw.write(LoadHTML.getText().replaceAll("<head>","<head>"+
                        "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />"+
                        "<table width='1700px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                            "<tr class='isi2'>"+
                                "<td valign='top' align='center'>"+
                                    "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
                                    akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                    akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                    "<font size='2' face='Tahoma'>DATA PENGIRIMAN SATU SEHAT ALLERGY INTOLERANCE<br><br></font>"+        
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
        runBackground(() ->tampil());
    }//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnCariActionPerformed(null);
        }else{
            Valid.pindah(evt,TCari,BtnPrint);
        }
    }//GEN-LAST:event_BtnCariKeyPressed
    private void BtnKirimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKirimActionPerformed
        kirimAlergi(false);
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
        kirimAlergi(true);
    }//GEN-LAST:event_BtnUpdateActionPerformed
    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        runBackground(() ->tampil());
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            TCari.setText("");
            runBackground(() ->tampil());
        }else{
            Valid.pindah(evt, BtnPrint, BtnKeluar);
        }
    }//GEN-LAST:event_BtnAllKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
            });
        } 
    }//GEN-LAST:event_formWindowOpened

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            SatuSehatKirimAllergyIntolerance dialog = new SatuSehatKirimAllergyIntolerance(new javax.swing.JFrame(), true);
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
    
    private void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            ps=koneksi.prepareStatement(
                   "select reg_periksa.tgl_registrasi,reg_periksa.jam_reg,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,"+
                   "pasien.nm_pasien,pasien.no_ktp,satu_sehat_encounter.id_encounter,pemeriksaan_ralan.alergi,"+
                   "pegawai.nama,pegawai.no_ktp as ktppraktisi,pemeriksaan_ralan.tgl_perawatan,pemeriksaan_ralan.jam_rawat,"+
                   "ifnull(satu_sehat_allergy_intolerance.id_allergy_intolerance,'') as satu_sehat_allergy_intolerance "+
                   "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                   "inner join satu_sehat_encounter on satu_sehat_encounter.no_rawat=reg_periksa.no_rawat "+
                   "inner join pemeriksaan_ralan on pemeriksaan_ralan.no_rawat=reg_periksa.no_rawat "+
                   "inner join pegawai on pemeriksaan_ralan.nip=pegawai.nik "+
                   "left join satu_sehat_allergy_intolerance on satu_sehat_allergy_intolerance.no_rawat=pemeriksaan_ralan.no_rawat "+
                   "and satu_sehat_allergy_intolerance.tgl_perawatan=pemeriksaan_ralan.tgl_perawatan and satu_sehat_allergy_intolerance.jam_rawat=pemeriksaan_ralan.jam_rawat and satu_sehat_allergy_intolerance.status='Ralan' "+
                   "where pemeriksaan_ralan.alergi<>'' and reg_periksa.tgl_registrasi between ? and ? "+
                   (TCari.getText().equals("")?"":"and (reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or "+
                   "pasien.nm_pasien like ? or pasien.no_ktp like ? or pegawai.no_ktp like ? or pegawai.nama like ?) "));
            try {
                ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
                ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
                if(!TCari.getText().equals("")){
                    ps.setString(3,"%"+TCari.getText()+"%");
                    ps.setString(4,"%"+TCari.getText()+"%");
                    ps.setString(5,"%"+TCari.getText()+"%");
                    ps.setString(6,"%"+TCari.getText()+"%");
                    ps.setString(7,"%"+TCari.getText()+"%");
                    ps.setString(8,"%"+TCari.getText()+"%");
                }
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new Object[]{
                        false,rs.getString("tgl_registrasi")+" "+rs.getString("jam_reg"),rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),
                        rs.getString("no_ktp"),rs.getString("id_encounter"),rs.getString("alergi"),rs.getString("nama"),rs.getString("ktppraktisi"),
                        rs.getString("tgl_perawatan")+" "+rs.getString("jam_rawat"),rs.getString("satu_sehat_allergy_intolerance"),"Ralan",allergyMappings.status(rs.getString("alergi")),rs.getString("satu_sehat_allergy_intolerance").isEmpty()?"unconfirmed":""
                    });
                }
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
            
            ps=koneksi.prepareStatement(
                   "select reg_periksa.tgl_registrasi,reg_periksa.jam_reg,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,"+
                   "pasien.nm_pasien,pasien.no_ktp,satu_sehat_encounter.id_encounter,pemeriksaan_ranap.alergi,"+
                   "pegawai.nama,pegawai.no_ktp as ktppraktisi,pemeriksaan_ranap.tgl_perawatan,pemeriksaan_ranap.jam_rawat,"+
                   "ifnull(satu_sehat_allergy_intolerance.id_allergy_intolerance,'') as satu_sehat_allergy_intolerance "+
                   "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                   "inner join satu_sehat_encounter on satu_sehat_encounter.no_rawat=reg_periksa.no_rawat "+
                   "inner join pemeriksaan_ranap on pemeriksaan_ranap.no_rawat=reg_periksa.no_rawat "+
                   "inner join pegawai on pemeriksaan_ranap.nip=pegawai.nik "+
                   "left join satu_sehat_allergy_intolerance on satu_sehat_allergy_intolerance.no_rawat=pemeriksaan_ranap.no_rawat "+
                   "and satu_sehat_allergy_intolerance.tgl_perawatan=pemeriksaan_ranap.tgl_perawatan and satu_sehat_allergy_intolerance.jam_rawat=pemeriksaan_ranap.jam_rawat and satu_sehat_allergy_intolerance.status='Ranap' "+
                   "where pemeriksaan_ranap.alergi<>'' and reg_periksa.tgl_registrasi between ? and ? "+
                   (TCari.getText().equals("")?"":"and (reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or "+
                   "pasien.nm_pasien like ? or pasien.no_ktp like ? or pegawai.no_ktp like ? or pegawai.nama like ?) "));
            try {
                ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+""));
                ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+""));
                if(!TCari.getText().equals("")){
                    ps.setString(3,"%"+TCari.getText()+"%");
                    ps.setString(4,"%"+TCari.getText()+"%");
                    ps.setString(5,"%"+TCari.getText()+"%");
                    ps.setString(6,"%"+TCari.getText()+"%");
                    ps.setString(7,"%"+TCari.getText()+"%");
                    ps.setString(8,"%"+TCari.getText()+"%");
                }
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new Object[]{
                        false,rs.getString("tgl_registrasi")+" "+rs.getString("jam_reg"),rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),
                        rs.getString("no_ktp"),rs.getString("id_encounter"),rs.getString("alergi"),rs.getString("nama"),rs.getString("ktppraktisi"),
                        rs.getString("tgl_perawatan")+" "+rs.getString("jam_rawat"),rs.getString("satu_sehat_allergy_intolerance"),"Ranap",allergyMappings.status(rs.getString("alergi")),rs.getString("satu_sehat_allergy_intolerance").isEmpty()?"unconfirmed":""
                    });
                }
            } catch (Exception e) {
                System.out.println("Notif : "+e);
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
        LCount.setText(""+tabMode.getRowCount());
    }

    private String nilai(int row,int column){
        Object value=tbObat.getValueAt(row,column);
        return value==null?"":value.toString().trim();
    }

    private void refreshMappingStatus(){
        for(int row=0;row<tbObat.getRowCount();row++)
            tbObat.setValueAt(allergyMappings.status(nilai(row,7)),row,13);
    }

    private void editMappingAlergi(){
        if(ceksukses){javax.swing.JOptionPane.showMessageDialog(this,"Tunggu pemuatan daftar selesai.");return;}
        int row=tbObat.getSelectedRow();
        if(row<0){javax.swing.JOptionPane.showMessageDialog(this,"Pilih baris alergi terlebih dahulu.");return;}
        try {
            String original=nilai(row,7);
            SatuSehatAllergyMappingEditor editor=new SatuSehatAllergyMappingEditor(original,allergyMappings.forEditing(original),allergyReferences);
            javax.swing.JDialog dialog=new javax.swing.JDialog(this,"Mapping Alergi",true);
            javax.swing.JButton save=new javax.swing.JButton("OK"),cancel=new javax.swing.JButton("Cancel");
            editor.addPropertyChangeListener("searching",event -> save.setEnabled(!editor.isSearching()));
            save.addActionListener(event -> {
                try{
                    SatuSehatAllergyMapping.Mapping mapping=editor.mapping();String source=editor.referenceSource();
                    if(source==null)allergyMappings.save(mapping);else allergyMappings.saveReference(mapping,source,true);
                    refreshMappingStatus();
                    javax.swing.JOptionPane.showMessageDialog(dialog,mapping.partial?"Mapping sebagian disimpan: "+mapping.display+". Zat lain pada catatan belum terpetakan; catatan asli tetap utuh.":"Mapping disimpan dengan cadangan. Silakan gunakan Kirim/Update sesuai status data.");
                    dialog.dispose();
                }catch(Exception e){javax.swing.JOptionPane.showMessageDialog(dialog,e.getMessage(),"Mapping belum valid",javax.swing.JOptionPane.WARNING_MESSAGE);}
            });
            cancel.addActionListener(event -> dialog.dispose());
            javax.swing.JPanel buttons=new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));buttons.add(save);buttons.add(cancel);
            dialog.setLayout(new java.awt.BorderLayout());dialog.add(editor,java.awt.BorderLayout.CENTER);dialog.add(buttons,java.awt.BorderLayout.SOUTH);
            dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            dialog.addWindowListener(new java.awt.event.WindowAdapter(){@Override public void windowClosed(java.awt.event.WindowEvent event){editor.cancelSearch();}});
            dialog.getRootPane().setDefaultButton(save);
            dialog.getRootPane().registerKeyboardAction(event -> dialog.dispose(),javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
            dialog.setMinimumSize(new Dimension(780,460));dialog.pack();dialog.setLocationRelativeTo(this);dialog.setVisible(true);
        }catch(Exception e){javax.swing.JOptionPane.showMessageDialog(this,"Mapping tidak dapat dibaca. Periksa format kamus/cadangan sebelum melanjutkan.");}
    }
    private void kirimAlergi(boolean update){
        if(!akses.getsatu_sehat_kirim_allergy_intolerance()||ceksukses)return;
        if(tbObat.isEditing()&&!tbObat.getCellEditor().stopCellEditing())return;
        java.util.Set<String> missing=new java.util.LinkedHashSet<>();
        for(int row=0;row<tbObat.getRowCount();row++){
            if(!Boolean.TRUE.equals(tbObat.getValueAt(row,0))||update==nilai(row,11).isEmpty())continue;
            try{if(allergyMappings.lookup(nilai(row,7))==null){SatuSehatAllergySearchTerm.extract(nilai(row,7));missing.add(nilai(row,7));}}
            catch(Exception e){/* Alasan spesifik tetap ditampilkan oleh validasi pengiriman. */}
        }
        if(missing.isEmpty()){prosesKirimAlergi(update);return;}
        if(missing.size()>10){javax.swing.JOptionPane.showMessageDialog(this,"Pilih maksimal 10 catatan alergi baru per pencarian daring.");return;}
        cariReferensiOnline(new java.util.ArrayList<>(missing),true,update);
    }

    private void cariReferensiBaris(){
        if(!akses.getsatu_sehat_kirim_allergy_intolerance()||ceksukses)return;
        int row=tbObat.getSelectedRow();
        if(row<0){javax.swing.JOptionPane.showMessageDialog(this,"Pilih satu baris alergi terlebih dahulu.");return;}
        try{
            String text=nilai(row,7);SatuSehatAllergySearchTerm.extract(text);
            cariReferensiOnline(java.util.Collections.singletonList(text),false,false);
        }catch(IllegalArgumentException e){javax.swing.JOptionPane.showMessageDialog(this,e.getMessage());}
    }

    private void cariReferensiOnline(final java.util.List<String> texts,final boolean untukKirim,final boolean update){
        final String pilihanAwal=untukKirim?pilihanPengiriman():"";
        ceksukses=true;setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        BtnReferensiAlergi.setEnabled(false);BtnMappingAlergi.setEnabled(false);BtnKirim.setEnabled(false);BtnUpdate.setEnabled(false);
        new javax.swing.SwingWorker<java.util.List<SatuSehatAllergyReferenceService.Lookup>,Void>(){
            private final StringBuilder errors=new StringBuilder();
            @Override protected java.util.List<SatuSehatAllergyReferenceService.Lookup> doInBackground(){
                java.util.List<SatuSehatAllergyReferenceService.Lookup> results=new java.util.ArrayList<>();
                for(String text:texts){
                    if(isCancelled())break;
                    try{results.add(allergyReferences.search(text));}
                    catch(Exception e){errors.append("Referensi belum dapat dicari; periksa satu nama alergen dan koneksi.\n");}
                }
                return results;
            }
            @Override protected void done(){
                ceksukses=false;
                if(!isDisplayable())return;
                setCursor(Cursor.getDefaultCursor());isCek();
                try{
                    java.util.List<SatuSehatAllergyReferenceService.Lookup> results=get();
                    if(untukKirim){
                        if(tbObat.isEditing())tbObat.getCellEditor().stopCellEditing();
                        if(!pilihanAwal.equals(pilihanPengiriman())){
                            javax.swing.JOptionPane.showMessageDialog(SatuSehatKirimAllergyIntolerance.this,"Pilihan baris/status berubah selama pencarian. Periksa kembali lalu klik Kirim/Update; belum ada pengiriman.");return;
                        }
                        int held=0;
                        for(SatuSehatAllergyReferenceService.Lookup lookup:results){
                            try{if(!allergyReferences.saveAutomatic(lookup,allergyMappings))held++;}
                            catch(Exception e){held++;}
                        }
                        refreshMappingStatus();
                        if(held>0||errors.length()>0)javax.swing.JOptionPane.showMessageDialog(SatuSehatKirimAllergyIntolerance.this,
                                "Sebagian referensi belum cocok tepat/tunggal atau layanan tidak lengkap. Baris tersebut tetap ditahan. Gunakan Cari Referensi Online untuk meninjau pilihan.");
                        prosesKirimAlergi(update);
                    }else if(!results.isEmpty()){
                        SatuSehatAllergyReferenceService.Lookup lookup=results.get(0);
                        if(allergyReferences.saveAutomatic(lookup,allergyMappings))
                            javax.swing.JOptionPane.showMessageDialog(SatuSehatKirimAllergyIntolerance.this,"Referensi cocok tepat dan tunggal; mapping disimpan dengan cadangan. Status klinis tetap mengikuti pilihan per baris.");
                        else pilihReferensi(lookup);
                        refreshMappingStatus();
                    }else javax.swing.JOptionPane.showMessageDialog(SatuSehatKirimAllergyIntolerance.this,"Referensi belum dapat ditemukan. Periksa nama alergen dan koneksi; mapping lama dipertahankan.");
                }catch(Exception e){javax.swing.JOptionPane.showMessageDialog(SatuSehatKirimAllergyIntolerance.this,"Referensi belum dapat disimpan. Muat ulang dan tinjau kamus; tidak ada pengiriman otomatis dari tombol pencarian.");}
            }
        }.execute();
    }

    private String pilihanPengiriman(){
        StringBuilder selected=new StringBuilder();
        for(int row=0;row<tbObat.getRowCount();row++)if(Boolean.TRUE.equals(tbObat.getValueAt(row,0)))
            for(int col:new int[]{2,6,7,10,11,12,14})selected.append(nilai(row,col)).append('\u0000');
        return selected.toString();
    }

    private void pilihReferensi(SatuSehatAllergyReferenceService.Lookup lookup)throws java.io.IOException{
        if(lookup.result.candidates.isEmpty()){
            javax.swing.JOptionPane.showMessageDialog(this,"Tidak ditemukan padanan dalam referensi yang tersedia. "+lookup.result.warning);return;
        }
        javax.swing.JList<SatuSehatAllergyReferenceService.Candidate> list=new javax.swing.JList<>(
                lookup.result.candidates.toArray(new SatuSehatAllergyReferenceService.Candidate[0]));
        list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        javax.swing.JComboBox<String> category=new javax.swing.JComboBox<>(new String[]{"","food","environment","biologic","medication"});
        list.addListSelectionListener(e -> {
            SatuSehatAllergyReferenceService.Candidate c=list.getSelectedValue();
            if(c!=null){category.setSelectedItem(c.category);category.setEnabled(c.category.isEmpty());}
        });
        list.setSelectedIndex(0);
        javax.swing.JScrollPane scroll=new javax.swing.JScrollPane(list);scroll.setPreferredSize(new Dimension(700,240));
        Object[] fields={"Nama alergen yang dicari: "+lookup.query,lookup.result.warning,scroll,"Kategori (tinjau jika belum ditentukan)",category,
            "Pilih hanya zat yang sesuai catatan klinis. Penemuan kode tidak mengonfirmasi alergi pasien."};
        if(javax.swing.JOptionPane.showConfirmDialog(this,fields,"Referensi Alergi",javax.swing.JOptionPane.OK_CANCEL_OPTION,javax.swing.JOptionPane.PLAIN_MESSAGE)==javax.swing.JOptionPane.OK_OPTION){
            SatuSehatAllergyReferenceService.Candidate c=list.getSelectedValue();
            if(c!=null)allergyMappings.saveReference(c.mapping(lookup.original,(String)category.getSelectedItem()),c.source,true);
        }
    }

    /** POST dan PUT memakai validasi serta pembentuk payload yang sama. Tidak menulis DDL. */
    private void prosesKirimAlergi(boolean update){
        if(!akses.getsatu_sehat_kirim_allergy_intolerance())return;
        if(ceksukses){javax.swing.JOptionPane.showMessageDialog(this,"Tunggu pemuatan daftar selesai.");return;}
        int sukses=0,terpilih=0;
        StringBuilder masalah=new StringBuilder();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            for(int row=0;row<tbObat.getRowCount();row++){
                if(!Boolean.TRUE.equals(tbObat.getValueAt(row,0)))continue;
                terpilih++;
                try {
                    String oldId=nilai(row,11);
                    if(update&&oldId.isEmpty())throw new IllegalArgumentException("Belum dikirim; gunakan Kirim terlebih dahulu.");
                    if(!update&&!oldId.isEmpty())throw new IllegalArgumentException("Sudah memiliki ID; gunakan Update.");
                    SatuSehatAllergyMapping.Mapping mapping=allergyMappings.resolve(nilai(row,7));
                    tbObat.setValueAt(allergyMappings.status(nilai(row,7)),row,13);
                    if(!validasiIdentitasSatuSehat(row))throw new IllegalArgumentException("NIK atau ID SATUSEHAT pasien/praktisi belum valid. Lengkapi mapping identitas terlebih dahulu.");
                    ObjectNode payload=SatuSehatAllergyPayload.build(mapper,koneksiDB.IDSATUSEHAT(),nilai(row,2),
                            idpasien,nilai(row,6),idpraktisi,nilai(row,10),nilai(row,7),mapping,update?oldId:null,nilai(row,14));
                    HttpHeaders requestHeaders=new HttpHeaders();
                    requestHeaders.setContentType(MediaType.APPLICATION_JSON);
                    requestHeaders.add("Authorization","Bearer "+api.TokenSatuSehat());
                    String body=api.getRest().exchange(link+"/AllergyIntolerance"+(update?"/"+oldId:""),
                            update?HttpMethod.PUT:HttpMethod.POST,new HttpEntity<String>(mapper.writeValueAsString(payload),requestHeaders),String.class).getBody();
                    JsonNode result=mapper.readTree(body);
                    String id=result==null?"":result.path("id").asText();
                    if(!id.matches("[A-Za-z0-9.-]{1,64}")||result==null||!"AllergyIntolerance".equals(result.path("resourceType").asText()))
                        throw new IllegalStateException("Respons tidak memuat resource/ID AllergyIntolerance yang valid. Periksa hasil di SATUSEHAT sebelum mengulang Kirim.");
                    if(update&&!id.equals(oldId))throw new IllegalStateException("ID respons Update berbeda. Periksa hasil sebelum mengulang.");
                    if(!update){
                        // Tampilkan ID lebih dahulu agar kegagalan simpan lokal tidak memicu POST ulang di sesi ini.
                        tbObat.setValueAt(id,row,11);
                        try(PreparedStatement save=koneksi.prepareStatement("insert into satu_sehat_allergy_intolerance "
                                +"(no_rawat,tgl_perawatan,jam_rawat,status,id_allergy_intolerance) values (?,?,?,?,?)")){
                            save.setString(1,nilai(row,2));save.setString(2,nilai(row,10).substring(0,10));
                            save.setString(3,nilai(row,10).substring(11,19));save.setString(4,nilai(row,12));save.setString(5,id);
                            save.executeUpdate();
                        }catch(java.sql.SQLException e){
                            throw new IllegalStateException("API berhasil, tetapi ID gagal disimpan lokal. Catat ID pada tabel dan perbaiki pencatatan lokal sebelum memuat ulang/kirim lagi.");
                        }
                    }
                    sukses++;tbObat.setValueAt(false,row,0);
                }catch(org.springframework.web.client.HttpStatusCodeException e){
                    masalah.append("Baris ").append(row+1).append(": HTTP ").append(e.getStatusCode().value());
                    // Hanya tampilkan pesan validasi di dialog, tanpa menulis payload/kredensial ke log.
                    try {
                        JsonNode outcome=mapper.readTree(e.getResponseBodyAsString());
                        for(JsonNode issue:outcome.path("issue"))masalah.append("; ").append(issue.path("details").path("text").asText());
                    }catch(Exception ignored){}
                    masalah.append('\n');
                }catch(IllegalArgumentException|IllegalStateException e){
                    masalah.append("Baris ").append(row+1).append(": ").append(e.getMessage()).append('\n');
                }catch(Exception e){
                    masalah.append("Baris ").append(row+1).append(": Proses gagal. Periksa koneksi, kamus, dan respons sebelum mengulang.\n");
                }
            }
        }finally{setCursor(Cursor.getDefaultCursor());refreshMappingStatus();}
        javax.swing.JTextArea hasil=new javax.swing.JTextArea("Dipilih: "+terpilih+"; berhasil: "+sukses+".\n"+masalah,12,70);
        hasil.setEditable(false);hasil.setLineWrap(true);hasil.setWrapStyleWord(true);
        javax.swing.JOptionPane.showMessageDialog(this,new javax.swing.JScrollPane(hasil),"AllergyIntolerance",javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    public void isCek(){
        BtnKirim.setEnabled(akses.getsatu_sehat_kirim_allergy_intolerance());
        BtnUpdate.setEnabled(akses.getsatu_sehat_kirim_allergy_intolerance());
        BtnPrint.setEnabled(akses.getsatu_sehat_kirim_allergy_intolerance());
        BtnMappingAlergi.setEnabled(akses.getsatu_sehat_kirim_allergy_intolerance());
        BtnReferensiAlergi.setEnabled(akses.getsatu_sehat_kirim_allergy_intolerance());
    }
    
    public JTable getTable(){
        return tbObat;
    }
    
    private void runBackground(Runnable task) {
        if (ceksukses) return;
        if (executor.isShutdown() || executor.isTerminated()) return;
        if (!isDisplayable()) return;

        ceksukses = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            executor.submit(() -> {
                try {
                    task.run();
                } finally {
                    ceksukses = false;
                    SwingUtilities.invokeLater(() -> {
                        if (isDisplayable()) {
                            setCursor(Cursor.getDefaultCursor());
                        }
                    });
                }
            });
        } catch (RejectedExecutionException ex) {
            ceksukses = false;
        }
    }
    
    @Override
    public void dispose() {
        executor.shutdownNow();
        super.dispose();
    }
}
