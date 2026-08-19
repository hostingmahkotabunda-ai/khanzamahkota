package rekammedis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import kepegawaian.DlgCariDokter;
import kepegawaian.DlgCariPetugas;


/**
 *
 * @author perpustakaan
 */
public final class RMPenilaianAwalKeperawatanRanapAnak extends javax.swing.JDialog {
    private final DefaultTableModel tabMode,tabModeMasalah,tabModeDetailMasalah,tabModeRencana,tabModeDetailRencana;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i=0,jml=0,index=0;
    private DlgCariPetugas petugas=new DlgCariPetugas(null,false);
    private DlgCariDokter dokter=new DlgCariDokter(null,false);
    private StringBuilder htmlContent;
    private String pilihan="";
    private boolean[] pilih; 
    private String[] kode,masalah;
    private String masalahkeperawatan="",finger=""; 
    private File file;
    private FileWriter fileWriter;
    private String iyem;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private JsonNode response;
    private FileReader myObj;
    
    /** Creates new form DlgRujuk
     * @param parent
     * @param modal */
    public RMPenilaianAwalKeperawatanRanapAnak(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        tabMode=new DefaultTableModel(null,new Object[]{
            "No.Rawat","No.RM","Nama Pasien","Tgl.Lahir","J.K.","NIP Pengkaji 1","Nama Pengkaji 1","NIP Pengkaji 2","Nama Pengkaji 2","Kode DPJP","Nama DPJP",
            "Tgl.Asuhan","Macam Kasus","Anamnesis","Tiba Di Ruang Rawat","Cara Masuk","Keluhan Utama","Riwayat Penyakit Saat Ini","Riwayat Penyakit Dahulu","Riwayat Penyakit Keluarga",
            "Riwayat Penggunaan Obat","Riwayat Pembedahan","Riwayat Dirawat Di RS","Alat Bantu Yang Dipakai","Riwayat Transfusi Darah","Riwayat Imunisasi","Riwayat Perkembangan Anak Dibanding Saudara",
            "Riwayat Alergi","Kesadaran Mental","Keadaan Umum","GCS(E,V,M)","TD(mmHg)",
            "Nadi(x/menit)","RR(x/menit)","Suhu(°C)","SpO2(%)","BB(Kg)","TB(cm)",
            "Mandi","Makan/Minum","Berpakaian","Eliminasi","Berpindah","Porsi Makan","Frekuensi Makan","Jenis Makanan","Lama Tidur","Gangguan Tidur",
            "a. Aktifitas Sehari-hari","b. Berjalan","c. Aktifitas","d. Alat Ambulasi","e. Ekstremitas Atas","f. Ekstremitas Bawah","g. Kemampuan Menggenggam",
            "h. Kemampuan Koordinasi","i. Kesimpulan Gangguan Fungsi","a. Kondisi Psikologis","b. Adakah Perilaku","c. Gangguan Jiwa di Masa Lalu","d. Hubungan Pasien",
            "e. Agama","f. Tinggal Dengan","g. Pekerjaan","h. Pembayaran","i. Nilai-nilai Kepercayaan","j. Bahasa Sehari-hari","k. Pendidikan Pasien","l. Pendidikan P.J.",
            "m. Edukasi Diberikan Kepada","Skala Wajah","N.S. Wajah","Skala Kaki","N.S. Kaki","Skala Aktifitas","N.S. Aktifitas","Skala Menangis","N.S. Menangis","Skala Bersuara","N.S. Bersuara",
            "Total Skala Nyeri","Skala Humpty Dumpty 1","N.H. 1","Skala Humpty Dumpty 2","N.H. 2","Skala Humpty Dumpty 3","N.H. 3","Skala Humpty Dumpty 4","N.H. 4","Skala Humpty Dumpty 5","N.H. 5","Skala Humpty Dumpty 6","N.H. 6","Skala Humpty Dumpty 7","N.H. 7","Total","1. Apakah ada penurunan BB yang tidak diinginkan selama 6 bulan terakhir ?","Skor 1","2. Apakah asupan makan berkurang karena tidak nafsu makan ?",
            "Skor 2","Total Skor","Pasien dengan diagnosis khusus","Keterangan Diagnosa Khusus","Sudah dibaca dan diketahui oleh Dietisen","Jam Dibaca Dietisen",
            "Rencana Keperawatan Lainnya"
        }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbObat.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 106; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(105);
            }else if(i==1){
                column.setPreferredWidth(65);
            }else if(i==2){
                column.setPreferredWidth(160);
            }else if(i==3){
                column.setPreferredWidth(65);
            }else if(i==4){
                column.setPreferredWidth(25);
            }else if(i==5){
                column.setPreferredWidth(85);
            }else if(i==6){
                column.setPreferredWidth(150);
            }else if(i==7){
                column.setPreferredWidth(85);
            }else if(i==8){
                column.setPreferredWidth(150);
            }else if(i==9){
                column.setPreferredWidth(90);
            }else if(i==10){
                column.setPreferredWidth(150);
            }else if(i==11){
                column.setPreferredWidth(117);
            }else if(i==12){
                column.setPreferredWidth(78);
            }else if(i==13){
                column.setPreferredWidth(150);
            }else if(i==14){
                column.setPreferredWidth(110);
            }else if(i==15){
                column.setPreferredWidth(70);
            }else if(i==16){
                column.setPreferredWidth(350);
            }else if(i==17){
                column.setPreferredWidth(170);
            }else if(i==18){
                column.setPreferredWidth(170);
            }else if(i==19){
                column.setPreferredWidth(170);
            }else if(i==20){
                column.setPreferredWidth(150);
            }else if(i==21){
                column.setPreferredWidth(150);
            }else if(i==22){
                column.setPreferredWidth(125);
            }else if(i==23){
                column.setPreferredWidth(210);
            }else if(i==24){
                column.setPreferredWidth(130);
            }else if(i==25){
                column.setPreferredWidth(130);
            }else if(i==26){
                column.setPreferredWidth(130);
            }else if(i==27){
                column.setPreferredWidth(130);
            }else if(i==28){
                column.setPreferredWidth(44);
            }else if(i==29){
                column.setPreferredWidth(59);
            }else if(i==30){
                column.setPreferredWidth(61);
            }else if(i==31){
                column.setPreferredWidth(59);
            }else if(i==32){
                column.setPreferredWidth(120);
            }else if(i==33){
                column.setPreferredWidth(85);
            }else if(i==34){
                column.setPreferredWidth(64);
            }else if(i==35){
                column.setPreferredWidth(60);
            }else if(i==36){
                column.setPreferredWidth(74);
            }else if(i==37){
                column.setPreferredWidth(67);
            }else if(i==38){
                column.setPreferredWidth(52);
            }else if(i==39){
                column.setPreferredWidth(52);
            }else if(i==40){
                column.setPreferredWidth(44);
            }else if(i==41){
                column.setPreferredWidth(44);
            }else if(i==42){
                column.setPreferredWidth(150);
            }else if(i==43){
                column.setPreferredWidth(150);
            }else if(i==44){
                column.setPreferredWidth(106);
            }else if(i==45){
                column.setPreferredWidth(130);
            }else if(i==46){
                column.setPreferredWidth(65);
            }else if(i==47){
                column.setPreferredWidth(50);
            }else if(i==48){
                column.setPreferredWidth(130);
            }else if(i==49){
                column.setPreferredWidth(72);
            }else if(i==50){
                column.setPreferredWidth(54);
            }else if(i==51){
                column.setPreferredWidth(63);
            }else if(i==52){
                column.setPreferredWidth(69);
            }else if(i==53){
                column.setPreferredWidth(97);
            }else if(i==54){
                column.setPreferredWidth(75);
            }else if(i==55){
                column.setPreferredWidth(170);
            }else if(i==56){
                column.setPreferredWidth(70);
            }else if(i==57){
                column.setPreferredWidth(140);
            }else if(i==58){
                column.setPreferredWidth(140);
            }else if(i==59){
                column.setPreferredWidth(140);
            }else if(i==60){
                column.setPreferredWidth(140);
            }else if(i==61){
                column.setPreferredWidth(140);
            }else if(i==62){
                column.setPreferredWidth(111);
            }else if(i==63){
                column.setPreferredWidth(60);
            }else if(i==64){
                column.setPreferredWidth(60);
            }else if(i==65){
                column.setPreferredWidth(140);
            }else if(i==66){
                column.setPreferredWidth(119);
            }else if(i==67){
                column.setPreferredWidth(65);
            }else if(i==68){
                column.setPreferredWidth(74);
            }else if(i==69){
                column.setPreferredWidth(140);
            }else if(i==70){
                column.setPreferredWidth(180);
            }else if(i==71){
                column.setPreferredWidth(70);
            }else if(i==72){
                column.setPreferredWidth(180);
            }else if(i==73){
                column.setPreferredWidth(70);
            }else if(i==74){
                column.setPreferredWidth(180);
            }else if(i==75){
                column.setPreferredWidth(70);
            }else if(i==76){
                column.setPreferredWidth(180);
            }else if(i==77){
                column.setPreferredWidth(70);
            }else if(i==78){
                column.setPreferredWidth(180);
            }else if(i==79){
                column.setPreferredWidth(70);
            }else if(i==80){
                column.setPreferredWidth(79);
            }else if(i==81){
                column.setPreferredWidth(200);
            }else if(i==82){
                column.setPreferredWidth(82);
            }else if(i==83){
                column.setPreferredWidth(200);
            }else if(i==84){
                column.setPreferredWidth(82);
            }else if(i==85){
                column.setPreferredWidth(200);
            }else if(i==86){
                column.setPreferredWidth(82);
            }else if(i==87){
                column.setPreferredWidth(200);
            }else if(i==88){
                column.setPreferredWidth(82);
            }else if(i==89){
                column.setPreferredWidth(200);
            }else if(i==90){
                column.setPreferredWidth(82);
            }else if(i==91){
                column.setPreferredWidth(200);
            }else if(i==92){
                column.setPreferredWidth(82);
            }else if(i==93){
                column.setPreferredWidth(200);
            }else if(i==94){
                column.setPreferredWidth(82);
            }else if(i==95){
                column.setPreferredWidth(82);
            }else if(i==96){
                column.setPreferredWidth(380);
            }else if(i==97){
                column.setPreferredWidth(40);
            }else if(i==98){
                column.setPreferredWidth(317);
            }else if(i==99){
                column.setPreferredWidth(40);
            }else if(i==100){
                column.setPreferredWidth(58);
            }else if(i==101){
                column.setPreferredWidth(165);
            }else if(i==102){
                column.setPreferredWidth(149);
            }else if(i==103){
                column.setPreferredWidth(209);
            }else if(i==104){
                column.setPreferredWidth(107);
            }else if(i==105){
                column.setPreferredWidth(200);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        
        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        KetAnamnesis.setDocument(new batasInput((byte)30).getKata(KetAnamnesis));
        KelUtama.setDocument(new batasInput((int)300).getKata(KelUtama));
        RPS.setDocument(new batasInput((int)300).getKata(RPS));
        RPD.setDocument(new batasInput((int)100).getKata(RPD));
        RPK.setDocument(new batasInput((int)100).getKata(RPK));
        RPO.setDocument(new batasInput((int)100).getKata(RPO));
        RPembedahan.setDocument(new batasInput((byte)40).getKata(RPembedahan));
        RDirawatRS.setDocument(new batasInput((byte)40).getKata(RDirawatRS));
        RTranfusi.setDocument(new batasInput((byte)40).getKata(RTranfusi));
        Alergi.setDocument(new batasInput((byte)40).getKata(Alergi));
        RiImunisasi.setDocument(new batasInput((int)300).getKata(RiImunisasi));
        KetPerkmAnk.setDocument(new batasInput((byte)30).getKata(KetPerkmAnk));
        KesadaranMental.setDocument(new batasInput((byte)40).getKata(KesadaranMental));
        GCS.setDocument(new batasInput((byte)10).getKata(GCS));
        TD.setDocument(new batasInput((byte)8).getKata(TD));
        Nadi.setDocument(new batasInput((byte)5).getKata(Nadi));
        RR.setDocument(new batasInput((byte)5).getKata(RR));
        Suhu.setDocument(new batasInput((byte)5).getKata(Suhu));
        SpO2.setDocument(new batasInput((byte)5).getKata(SpO2));
        BB.setDocument(new batasInput((byte)5).getKata(BB));
        TB.setDocument(new batasInput((byte)5).getKata(TB));
        PolaNutrisiPorsi.setDocument(new batasInput((byte)3).getKata(PolaNutrisiPorsi));
        PolaNutrisiFrekuensi.setDocument(new batasInput((byte)3).getKata(PolaNutrisiFrekuensi));
        PolaNutrisiJenis.setDocument(new batasInput((byte)20).getKata(PolaNutrisiJenis));
        PolaTidurLama.setDocument(new batasInput((byte)3).getKata(PolaTidurLama));
        KeteranganBerjalan.setDocument(new batasInput((byte)40).getKata(KeteranganBerjalan));
        KeteranganEkstrimitasAtas.setDocument(new batasInput((byte)40).getKata(KeteranganEkstrimitasAtas));
        KeteranganEkstrimitasBawah.setDocument(new batasInput((byte)40).getKata(KeteranganEkstrimitasBawah));
        KeteranganKemampuanMenggenggam.setDocument(new batasInput((byte)40).getKata(KeteranganKemampuanMenggenggam));
        KeteranganKemampuanKoordinasi.setDocument(new batasInput((byte)40).getKata(KeteranganKemampuanKoordinasi));
        KeteranganAdakahPerilaku.setDocument(new batasInput((byte)40).getKata(KeteranganAdakahPerilaku));
        KeteranganTinggalDengan.setDocument(new batasInput((byte)40).getKata(KeteranganTinggalDengan));
        KeteranganNilaiKepercayaan.setDocument(new batasInput((byte)40).getKata(KeteranganNilaiKepercayaan));
        KeteranganEdukasiPsikologis.setDocument(new batasInput((byte)40).getKata(KeteranganEdukasiPsikologis));
        KeteranganDiagnosaKhususGizi.setDocument(new batasInput((byte)50).getKata(KeteranganDiagnosaKhususGizi));
        KeteranganDiketahuiDietisen.setDocument(new batasInput((byte)10).getKata(KeteranganDiketahuiDietisen));
        Rencana.setDocument(new batasInput((int)200).getKata(Rencana));
        
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
            
            TCariMasalah.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCariMasalah.getText().length()>2){
                        tampilMasalah2();
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCariMasalah.getText().length()>2){
                        tampilMasalah2();
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCariMasalah.getText().length()>2){
                        tampilMasalah2();
                    }
                }
            });
        }
        
        petugas.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(petugas.getTable().getSelectedRow()!= -1){ 
                    if(i==1){
                        KdPetugas.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),0).toString());
                        NmPetugas.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),1).toString());  
                    }else{
                        KdPetugas2.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),0).toString());
                        NmPetugas2.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(),1).toString());  
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
        
        dokter.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(dokter.getTable().getSelectedRow()!= -1){ 
                    KdDPJP.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                    NmDPJP.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());  
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
        
        tabModeMasalah=new DefaultTableModel(null,new Object[]{
                "P","KODE","MASALAH KEPERAWATAN"
            }){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if (colIndex==0) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Double.class
             };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbMasalahKeperawatan.setModel(tabModeMasalah);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbMasalahKeperawatan.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbMasalahKeperawatan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        for (i = 0; i < 3; i++) {
            TableColumn column = tbMasalahKeperawatan.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(20);
            }else if(i==1){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==2){
                column.setPreferredWidth(350);
            }
        }
        tbMasalahKeperawatan.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeRencana=new DefaultTableModel(null,new Object[]{
                "P","KODE","RENCANA KEPERAWATAN"
            }){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){
                boolean a = false;
                if (colIndex==0) {
                    a=true;
                }
                return a;
             }
             Class[] types = new Class[] {
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Double.class
             };
             @Override
             public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
             }
        };
        tbRencanaKeperawatan.setModel(tabModeRencana);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbRencanaKeperawatan.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbRencanaKeperawatan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        for (i = 0; i < 3; i++) {
            TableColumn column = tbRencanaKeperawatan.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(20);
            }else if(i==1){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==2){
                column.setPreferredWidth(350);
            }
        }
        tbRencanaKeperawatan.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeDetailMasalah=new DefaultTableModel(null,new Object[]{
                "Kode","Masalah Keperawatan"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbMasalahDetail.setModel(tabModeDetailMasalah);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbMasalahDetail.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbMasalahDetail.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 2; i++) {
            TableColumn column = tbMasalahDetail.getColumnModel().getColumn(i);
            if(i==0){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==1){
                column.setPreferredWidth(420);
            }
        }
        tbMasalahDetail.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeDetailRencana=new DefaultTableModel(null,new Object[]{
                "Kode","Rencana Keperawatan"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbRencanaDetail.setModel(tabModeDetailRencana);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbRencanaDetail.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbRencanaDetail.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 2; i++) {
            TableColumn column = tbRencanaDetail.getColumnModel().getColumn(i);
            if(i==0){
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }else if(i==1){
                column.setPreferredWidth(420);
            }
        }
        tbRencanaDetail.setDefaultRenderer(Object.class, new WarnaTable());
        
        ChkAccor.setSelected(false);
        isMenu();
        modernisasiTampilan();
    }

    /**
     * Lapisan presentasi untuk membuat form asesmen panjang lebih mudah
     * dinavigasi. Seluruh komponen input dan logika penyimpanan tetap memakai
     * komponen lama sehingga tidak mengubah kontrak data form.
     */
    private void modernisasiTampilan() {
        final Color utama = new Color(0, 133, 143);
        final Color utamaMuda = new Color(230, 247, 248);
        final Color latar = new Color(246, 249, 251);
        final Color garis = new Color(214, 224, 230);
        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(garis),
                "  Penilaian Awal Keperawatan Rawat Inap Anak  ",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Tahoma", Font.BOLD, 13), utama));
        internalFrame1.setBackground(latar);
        internalFrame2.setBackground(latar);
        FormInput.setBackground(Color.WHITE);
        scrollInput.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        scrollInput.getVerticalScrollBar().setUnitIncrement(22);

        // Penanda wajib mengikuti validasi pada BtnSimpanActionPerformed.
        tandaiWajib(jLabel10, "No. Rawat");
        tandaiWajib(label14, "Pengkaji 1");
        tandaiWajib(label15, "Pengkaji 2");
        tandaiWajib(label16, "DPJP");
        tandaiWajib(jLabel30, "Riwayat Penyakit Saat Ini");

        JLabel[] judulBagian = {
            jLabel94, jLabel95, jLabel169, jLabel180,
            jLabel189, jLabel203, jLabel59, jLabel271
        };
        for (JLabel judul : judulBagian) {
            judul.setFont(new Font("Tahoma", Font.BOLD, 12));
            judul.setForeground(utama);
        }

        javax.swing.JSeparator[] pemisah = {
            jSeparator2, jSeparator3, jSeparator5, jSeparator4,
            jSeparator6, jSeparator8, jSeparator10, jSeparator11, jSeparator12
        };
        for (javax.swing.JSeparator separator : pemisah) {
            separator.setForeground(garis);
            separator.setBackground(garis);
            separator.setBorder(null);
        }

        /*
         * Pecah kanvas lama menjadi delapan halaman sungguhan. Komponen tidak
         * disalin: komponen asli dipindahkan ke halaman masing-masing agar
         * listener, nilai, validasi, dan proses simpan tetap identik.
         */
        final int[] awalBagian = {0, 538, 630, 800, 950, 1128, 1310, 1560};
        final int[] akhirBagian = {538, 630, 800, 950, 1128, 1310, 1560, 2100};
        final JPanel[] halamanBagian = new JPanel[awalBagian.length];
        for (int nomorBagian = 0; nomorBagian < halamanBagian.length; nomorBagian++) {
            JPanel halaman = new JPanel(null);
            halaman.setName("halamanAsesmen" + (nomorBagian + 1));
            halaman.setBackground(Color.WHITE);
            halaman.setBorder(new EmptyBorder(8, 12, 12, 12));
            halaman.setPreferredSize(new Dimension(
                    900, (akhirBagian[nomorBagian] - awalBagian[nomorBagian]) + 35));
            halamanBagian[nomorBagian] = halaman;
        }

        Component[] komponenForm = FormInput.getComponents();
        for (Component komponen : komponenForm) {
            java.awt.Rectangle batas = komponen.getBounds();
            int nomorBagian = halamanBagian.length - 1;
            for (int kandidat = 0; kandidat < awalBagian.length; kandidat++) {
                if (batas.y >= awalBagian[kandidat] && batas.y < akhirBagian[kandidat]) {
                    nomorBagian = kandidat;
                    break;
                }
            }
            komponen.setBounds(
                    batas.x + 12,
                    batas.y - awalBagian[nomorBagian] + 12,
                    batas.width,
                    batas.height);
            halamanBagian[nomorBagian].add(komponen);
        }
        scrollInput.setViewportView(halamanBagian[0]);

        JPanel navigasi = new JPanel();
        navigasi.setName("navigasiAsesmen");
        navigasi.setBackground(Color.WHITE);
        navigasi.setBorder(new EmptyBorder(12, 10, 12, 10));
        navigasi.setPreferredSize(new Dimension(205, 100));
        navigasi.setLayout(new javax.swing.BoxLayout(navigasi, javax.swing.BoxLayout.Y_AXIS));

        JLabel judulNavigasi = new JLabel("BAGIAN ASESMEN");
        judulNavigasi.setFont(new Font("Tahoma", Font.BOLD, 11));
        judulNavigasi.setForeground(new Color(75, 90, 100));
        judulNavigasi.setBorder(new EmptyBorder(0, 8, 10, 0));
        judulNavigasi.setAlignmentX(Component.LEFT_ALIGNMENT);
        navigasi.add(judulNavigasi);

        String[] namaBagian = {
            "1  Informasi & Riwayat",
            "2  Pemeriksaan Fisik",
            "3  Pola Kehidupan",
            "4  Fungsi & Mobilitas",
            "5  Psikososial",
            "6  Nyeri",
            "7  Risiko Jatuh",
            "8  Gizi & Rencana"
        };
        for (int indexBagian = 0; indexBagian < namaBagian.length; indexBagian++) {
            final int halamanAktif = indexBagian;
            JButton tombolBagian = new JButton(namaBagian[indexBagian]);
            tombolBagian.setName("navigasiBagian" + (indexBagian + 1));
            tombolBagian.setHorizontalAlignment(SwingConstants.LEFT);
            tombolBagian.setFont(new Font("Tahoma", indexBagian == 0 ? Font.BOLD : Font.PLAIN, 11));
            tombolBagian.setForeground(indexBagian == 0 ? utama : new Color(65, 78, 86));
            tombolBagian.setBackground(indexBagian == 0 ? utamaMuda : Color.WHITE);
            tombolBagian.setBorder(new EmptyBorder(9, 10, 9, 8));
            tombolBagian.setMargin(new Insets(0, 0, 0, 0));
            tombolBagian.setFocusPainted(false);
            tombolBagian.setCursor(new Cursor(Cursor.HAND_CURSOR));
            tombolBagian.setMaximumSize(new Dimension(185, 38));
            tombolBagian.setAlignmentX(Component.LEFT_ALIGNMENT);
            tombolBagian.addActionListener(e -> {
                scrollInput.setViewportView(halamanBagian[halamanAktif]);
                scrollInput.getViewport().setViewPosition(new java.awt.Point(0, 0));
                for (Component komponen : navigasi.getComponents()) {
                    if (komponen instanceof JButton) {
                        JButton item = (JButton) komponen;
                        boolean aktif = item == tombolBagian;
                        item.setBackground(aktif ? utamaMuda : Color.WHITE);
                        item.setForeground(aktif ? utama : new Color(65, 78, 86));
                        item.setFont(new Font("Tahoma", aktif ? Font.BOLD : Font.PLAIN, 11));
                    }
                }
            });
            navigasi.add(tombolBagian);
            navigasi.add(javax.swing.Box.createVerticalStrut(3));
        }

        navigasi.add(javax.swing.Box.createVerticalGlue());
        JLabel keterangan = new JLabel("<html><span style='color:#d32f2f'>*</span> Wajib diisi</html>");
        keterangan.setFont(new Font("Tahoma", Font.PLAIN, 11));
        keterangan.setForeground(new Color(85, 95, 102));
        keterangan.setBorder(new EmptyBorder(10, 8, 4, 0));
        keterangan.setAlignmentX(Component.LEFT_ALIGNMENT);
        navigasi.add(keterangan);
        internalFrame2.add(navigasi, java.awt.BorderLayout.LINE_START);

        panelGlass8.setBackground(Color.WHITE);
        panelGlass8.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, garis));
        BtnSimpan.setText("Simpan & Lanjutkan");
        BtnSimpan.setPreferredSize(new Dimension(145, 32));
        BtnBatal.setText("Baru / Bersihkan");
        BtnBatal.setPreferredSize(new Dimension(125, 32));
        TabRawat.setFont(new Font("Tahoma", Font.BOLD, 11));
        TabRawat.setForeground(utama);

        internalFrame2.revalidate();
        internalFrame2.repaint();
    }

    private void tandaiWajib(JLabel label, String teks) {
        label.setText("<html>" + teks + " <font color='#D32F2F'>*</font> :</html>");
        label.setToolTipText("Wajib diisi");
        label.setForeground(new Color(55, 65, 72));
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
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnPrint = new widget.Button();
        BtnAll = new widget.Button();
        BtnKeluar = new widget.Button();
        TabRawat = new javax.swing.JTabbedPane();
        internalFrame2 = new widget.InternalFrame();
        scrollInput = new widget.ScrollPane();
        FormInput = new widget.PanelBiasa();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        TNoRM = new widget.TextBox();
        label14 = new widget.Label();
        KdPetugas = new widget.TextBox();
        NmPetugas = new widget.TextBox();
        BtnPetugas = new widget.Button();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        Jk = new widget.TextBox();
        jLabel10 = new widget.Label();
        label11 = new widget.Label();
        jLabel11 = new widget.Label();
        jLabel36 = new widget.Label();
        Anamnesis = new widget.ComboBox();
        TglAsuhan = new widget.Tanggal();
        NmPetugas2 = new widget.TextBox();
        BtnPetugas2 = new widget.Button();
        KdPetugas2 = new widget.TextBox();
        label15 = new widget.Label();
        label16 = new widget.Label();
        KdDPJP = new widget.TextBox();
        NmDPJP = new widget.TextBox();
        BtnDPJP = new widget.Button();
        TibadiRuang = new widget.ComboBox();
        jLabel37 = new widget.Label();
        CaraMasuk = new widget.ComboBox();
        jLabel38 = new widget.Label();
        jLabel94 = new widget.Label();
        jLabel9 = new widget.Label();
        jLabel39 = new widget.Label();
        Alergi = new widget.TextBox();
        scrollPane1 = new widget.ScrollPane();
        RPS = new widget.TextArea();
        jLabel30 = new widget.Label();
        scrollPane2 = new widget.ScrollPane();
        RPK = new widget.TextArea();
        jLabel31 = new widget.Label();
        scrollPane3 = new widget.ScrollPane();
        RPD = new widget.TextArea();
        jLabel32 = new widget.Label();
        scrollPane4 = new widget.ScrollPane();
        RPO = new widget.TextArea();
        jSeparator2 = new javax.swing.JSeparator();
        MacamKasus = new widget.ComboBox();
        jLabel41 = new widget.Label();
        KetAnamnesis = new widget.TextBox();
        jLabel40 = new widget.Label();
        RDirawatRS = new widget.TextBox();
        RPembedahan = new widget.TextBox();
        jLabel42 = new widget.Label();
        jLabel43 = new widget.Label();
        AlatBantuDipakai = new widget.ComboBox();
        jLabel45 = new widget.Label();
        RTranfusi = new widget.TextBox();
        jLabel46 = new widget.Label();
        jLabel95 = new widget.Label();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel47 = new widget.Label();
        KesadaranMental = new widget.TextBox();
        jLabel130 = new widget.Label();
        KeadaanMentalUmum = new widget.ComboBox();
        jLabel28 = new widget.Label();
        GCS = new widget.TextBox();
        jLabel22 = new widget.Label();
        TD = new widget.TextBox();
        jLabel23 = new widget.Label();
        jLabel17 = new widget.Label();
        Nadi = new widget.TextBox();
        jLabel16 = new widget.Label();
        jLabel26 = new widget.Label();
        RR = new widget.TextBox();
        jLabel25 = new widget.Label();
        jLabel18 = new widget.Label();
        Suhu = new widget.TextBox();
        jLabel20 = new widget.Label();
        jLabel24 = new widget.Label();
        SpO2 = new widget.TextBox();
        jLabel29 = new widget.Label();
        jLabel12 = new widget.Label();
        BB = new widget.TextBox();
        jLabel13 = new widget.Label();
        jLabel15 = new widget.Label();
        TB = new widget.TextBox();
        jLabel48 = new widget.Label();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel169 = new widget.Label();
        jLabel54 = new widget.Label();
        jLabel170 = new widget.Label();
        PolaAktifitasEliminasi = new widget.ComboBox();
        jLabel171 = new widget.Label();
        jLabel172 = new widget.Label();
        jLabel173 = new widget.Label();
        jLabel174 = new widget.Label();
        PolaAktifitasMandi = new widget.ComboBox();
        PolaAktifitasMakan = new widget.ComboBox();
        PolaAktifitasBerpakaian = new widget.ComboBox();
        PolaAktifitasBerpindah = new widget.ComboBox();
        jLabel55 = new widget.Label();
        jLabel121 = new widget.Label();
        PolaNutrisiPorsi = new widget.TextBox();
        jLabel122 = new widget.Label();
        jLabel123 = new widget.Label();
        PolaNutrisiFrekuensi = new widget.TextBox();
        jLabel175 = new widget.Label();
        jLabel177 = new widget.Label();
        PolaNutrisiJenis = new widget.TextBox();
        jLabel56 = new widget.Label();
        jLabel176 = new widget.Label();
        PolaTidurLama = new widget.TextBox();
        jLabel178 = new widget.Label();
        PolaTidurGangguan = new widget.ComboBox();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel180 = new widget.Label();
        jLabel179 = new widget.Label();
        AktifitasSehari2 = new widget.ComboBox();
        jLabel181 = new widget.Label();
        Berjalan = new widget.ComboBox();
        KeteranganBerjalan = new widget.TextBox();
        jLabel182 = new widget.Label();
        Aktifitas = new widget.ComboBox();
        jLabel183 = new widget.Label();
        AlatAmbulasi = new widget.ComboBox();
        jLabel184 = new widget.Label();
        EkstrimitasAtas = new widget.ComboBox();
        KeteranganEkstrimitasAtas = new widget.TextBox();
        jLabel185 = new widget.Label();
        EkstrimitasBawah = new widget.ComboBox();
        KeteranganEkstrimitasBawah = new widget.TextBox();
        jLabel186 = new widget.Label();
        KemampuanMenggenggam = new widget.ComboBox();
        KeteranganKemampuanMenggenggam = new widget.TextBox();
        jLabel187 = new widget.Label();
        KemampuanKoordinasi = new widget.ComboBox();
        KeteranganKemampuanKoordinasi = new widget.TextBox();
        jLabel188 = new widget.Label();
        KesimpulanGangguanFungsi = new widget.ComboBox();
        jSeparator6 = new javax.swing.JSeparator();
        jLabel189 = new widget.Label();
        jLabel190 = new widget.Label();
        KondisiPsikologis = new widget.ComboBox();
        jLabel191 = new widget.Label();
        AdakahPerilaku = new widget.ComboBox();
        KeteranganAdakahPerilaku = new widget.TextBox();
        jLabel192 = new widget.Label();
        GangguanJiwa = new widget.ComboBox();
        jLabel193 = new widget.Label();
        HubunganAnggotaKeluarga = new widget.ComboBox();
        jLabel194 = new widget.Label();
        Agama = new widget.TextBox();
        jLabel195 = new widget.Label();
        TinggalDengan = new widget.ComboBox();
        KeteranganTinggalDengan = new widget.TextBox();
        jLabel196 = new widget.Label();
        PekerjaanPasien = new widget.TextBox();
        jLabel197 = new widget.Label();
        CaraBayar = new widget.TextBox();
        jLabel198 = new widget.Label();
        NilaiKepercayaan = new widget.ComboBox();
        KeteranganNilaiKepercayaan = new widget.TextBox();
        jLabel199 = new widget.Label();
        Bahasa = new widget.TextBox();
        jLabel200 = new widget.Label();
        PendidikanPasien = new widget.TextBox();
        jLabel201 = new widget.Label();
        PendidikanPJ = new widget.ComboBox();
        jLabel202 = new widget.Label();
        EdukasiPsikolgis = new widget.ComboBox();
        KeteranganEdukasiPsikologis = new widget.TextBox();
        jSeparator8 = new javax.swing.JSeparator();
        jLabel203 = new widget.Label();
        jSeparator10 = new javax.swing.JSeparator();
        jSeparator11 = new javax.swing.JSeparator();
        jLabel271 = new widget.Label();
        jLabel272 = new widget.Label();
        SkalaGizi1 = new widget.ComboBox();
        jLabel273 = new widget.Label();
        NilaiGizi1 = new widget.TextBox();
        jLabel274 = new widget.Label();
        SkalaGizi2 = new widget.ComboBox();
        jLabel275 = new widget.Label();
        NilaiGizi2 = new widget.TextBox();
        jLabel276 = new widget.Label();
        NilaiGiziTotal = new widget.TextBox();
        jLabel277 = new widget.Label();
        DiagnosaKhususGizi = new widget.ComboBox();
        KeteranganDiagnosaKhususGizi = new widget.TextBox();
        jLabel278 = new widget.Label();
        DiketahuiDietisen = new widget.ComboBox();
        jLabel279 = new widget.Label();
        KeteranganDiketahuiDietisen = new widget.TextBox();
        jSeparator12 = new javax.swing.JSeparator();
        Scroll6 = new widget.ScrollPane();
        tbMasalahKeperawatan = new widget.Table();
        TabRencanaKeperawatan = new javax.swing.JTabbedPane();
        panelBiasa1 = new widget.PanelBiasa();
        Scroll8 = new widget.ScrollPane();
        tbRencanaKeperawatan = new widget.Table();
        scrollPane5 = new widget.ScrollPane();
        Rencana = new widget.TextArea();
        BtnTambahMasalah = new widget.Button();
        BtnAllMasalah = new widget.Button();
        BtnCariMasalah = new widget.Button();
        TCariMasalah = new widget.TextBox();
        BtnTambahRencana = new widget.Button();
        BtnAllRencana = new widget.Button();
        BtnCariRencana = new widget.Button();
        label13 = new widget.Label();
        TCariRencana = new widget.TextBox();
        label12 = new widget.Label();
        jLabel280 = new widget.Label();
        jLabel281 = new widget.Label();
        jLabel282 = new widget.Label();
        jLabel283 = new widget.Label();
        jLabel284 = new widget.Label();
        jLabel285 = new widget.Label();
        jLabel286 = new widget.Label();
        jLabel287 = new widget.Label();
        jLabel288 = new widget.Label();
        jLabel59 = new widget.Label();
        jLabel289 = new widget.Label();
        SkalaResiko7 = new widget.ComboBox();
        NilaiResiko7 = new widget.TextBox();
        SkalaResiko8 = new widget.ComboBox();
        NilaiResiko8 = new widget.TextBox();
        jLabel290 = new widget.Label();
        jLabel291 = new widget.Label();
        jLabel292 = new widget.Label();
        SkalaResiko9 = new widget.ComboBox();
        SkalaResiko10 = new widget.ComboBox();
        NilaiResiko9 = new widget.TextBox();
        NilaiResiko10 = new widget.TextBox();
        SkalaResiko11 = new widget.ComboBox();
        jLabel293 = new widget.Label();
        jLabel294 = new widget.Label();
        jLabel295 = new widget.Label();
        SkalaResiko12 = new widget.ComboBox();
        SkalaResiko13 = new widget.ComboBox();
        NilaiResiko11 = new widget.TextBox();
        NilaiResiko12 = new widget.TextBox();
        NilaiResikoTotal1 = new widget.TextBox();
        jLabel296 = new widget.Label();
        TingkatResiko1 = new widget.Label();
        NilaiResiko13 = new widget.TextBox();
        jLabel49 = new widget.Label();
        RPerkmAnk = new widget.ComboBox();
        KetPerkmAnk = new widget.TextBox();
        jLabel33 = new widget.Label();
        scrollPane7 = new widget.ScrollPane();
        KelUtama = new widget.TextArea();
        scrollPane8 = new widget.ScrollPane();
        RiImunisasi = new widget.TextArea();
        jLabel164 = new widget.Label();
        jLabel165 = new widget.Label();
        SkalaWajah = new widget.ComboBox();
        SkalaKaki = new widget.ComboBox();
        SkalaAktifitas = new widget.ComboBox();
        NilaiAktifitas = new widget.TextBox();
        NilaiKaki = new widget.TextBox();
        NilaiWajah = new widget.TextBox();
        jLabel168 = new widget.Label();
        jLabel297 = new widget.Label();
        SkalaMenangis = new widget.ComboBox();
        SkalaBersuara = new widget.ComboBox();
        NilaiMenangis = new widget.TextBox();
        NilaiBersuara = new widget.TextBox();
        SkalaNyeri1 = new widget.TextBox();
        jLabel298 = new widget.Label();
        jLabel167 = new widget.Label();
        jLabel166 = new widget.Label();
        totatskalanyeri = new widget.Label();
        internalFrame3 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        panelGlass9 = new widget.panelisi();
        jLabel19 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        PanelAccor = new widget.PanelBiasa();
        ChkAccor = new widget.CekBox();
        FormMenu = new widget.PanelBiasa();
        jLabel34 = new widget.Label();
        TNoRM1 = new widget.TextBox();
        TPasien1 = new widget.TextBox();
        BtnPrint1 = new widget.Button();
        FormMasalahRencana = new widget.PanelBiasa();
        Scroll7 = new widget.ScrollPane();
        tbMasalahDetail = new widget.Table();
        Scroll9 = new widget.ScrollPane();
        tbRencanaDetail = new widget.Table();
        scrollPane6 = new widget.ScrollPane();
        DetailRencana = new widget.TextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Penilaian Awal Keperawatan Rawat Inap Umum ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 54));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setName("BtnSimpan"); // NOI18N
        BtnSimpan.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpanActionPerformed(evt);
            }
        });
        BtnSimpan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSimpanKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnSimpan);

        BtnBatal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png"))); // NOI18N
        BtnBatal.setMnemonic('B');
        BtnBatal.setText("Baru");
        BtnBatal.setToolTipText("Alt+B");
        BtnBatal.setName("BtnBatal"); // NOI18N
        BtnBatal.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnBatalActionPerformed(evt);
            }
        });
        BtnBatal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnBatalKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnBatal);

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
        panelGlass8.add(BtnHapus);

        BtnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        BtnEdit.setMnemonic('G');
        BtnEdit.setText("Ganti");
        BtnEdit.setToolTipText("Alt+G");
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
        panelGlass8.add(BtnEdit);

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
        panelGlass8.add(BtnPrint);

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

        internalFrame1.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        TabRawat.setBackground(new java.awt.Color(254, 255, 254));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setToolTipText("");
        TabRawat.setName("TabRawat"); // NOI18N
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });

        internalFrame2.setBorder(null);
        internalFrame2.setName("internalFrame2"); // NOI18N
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        scrollInput.setName("scrollInput"); // NOI18N
        scrollInput.setPreferredSize(new java.awt.Dimension(102, 557));

        FormInput.setBackground(new java.awt.Color(255, 255, 255));
        FormInput.setBorder(null);
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(870, 2100));
        FormInput.setLayout(null);

        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(74, 10, 131, 23);

        TPasien.setEditable(false);
        TPasien.setName("TPasien"); // NOI18N
        FormInput.add(TPasien);
        TPasien.setBounds(309, 10, 260, 23);

        TNoRM.setEditable(false);
        TNoRM.setName("TNoRM"); // NOI18N
        FormInput.add(TNoRM);
        TNoRM.setBounds(207, 10, 100, 23);

        label14.setText("Pengkaji 1 :");
        label14.setName("label14"); // NOI18N
        label14.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label14);
        label14.setBounds(0, 40, 70, 23);

        KdPetugas.setEditable(false);
        KdPetugas.setName("KdPetugas"); // NOI18N
        KdPetugas.setPreferredSize(new java.awt.Dimension(80, 23));
        KdPetugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdPetugasKeyPressed(evt);
            }
        });
        FormInput.add(KdPetugas);
        KdPetugas.setBounds(74, 40, 100, 23);

        NmPetugas.setEditable(false);
        NmPetugas.setName("NmPetugas"); // NOI18N
        NmPetugas.setPreferredSize(new java.awt.Dimension(207, 23));
        FormInput.add(NmPetugas);
        NmPetugas.setBounds(176, 40, 210, 23);

        BtnPetugas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPetugas.setMnemonic('2');
        BtnPetugas.setToolTipText("Alt+2");
        BtnPetugas.setName("BtnPetugas"); // NOI18N
        BtnPetugas.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnPetugas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPetugasActionPerformed(evt);
            }
        });
        BtnPetugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPetugasKeyPressed(evt);
            }
        });
        FormInput.add(BtnPetugas);
        BtnPetugas.setBounds(388, 40, 28, 23);

        jLabel8.setText("Tgl.Lahir :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(580, 10, 60, 23);

        TglLahir.setEditable(false);
        TglLahir.setName("TglLahir"); // NOI18N
        FormInput.add(TglLahir);
        TglLahir.setBounds(644, 10, 80, 23);

        Jk.setEditable(false);
        Jk.setName("Jk"); // NOI18N
        FormInput.add(Jk);
        Jk.setBounds(774, 10, 80, 23);

        jLabel10.setText("No.Rawat :");
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(0, 10, 70, 23);

        label11.setText("Tanggal :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label11);
        label11.setBounds(438, 70, 70, 23);

        jLabel11.setText("J.K. :");
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(740, 10, 30, 23);

        jLabel36.setText("Anamnesis :");
        jLabel36.setName("jLabel36"); // NOI18N
        FormInput.add(jLabel36);
        jLabel36.setBounds(0, 100, 70, 23);

        Anamnesis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Autoanamnesis", "Alloanamnesis" }));
        Anamnesis.setName("Anamnesis"); // NOI18N
        Anamnesis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AnamnesisKeyPressed(evt);
            }
        });
        FormInput.add(Anamnesis);
        Anamnesis.setBounds(74, 100, 130, 23);

        TglAsuhan.setForeground(new java.awt.Color(50, 70, 50));
        TglAsuhan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "05-05-2024 04:41:35" }));
        TglAsuhan.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        TglAsuhan.setName("TglAsuhan"); // NOI18N
        TglAsuhan.setOpaque(false);
        TglAsuhan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TglAsuhanKeyPressed(evt);
            }
        });
        FormInput.add(TglAsuhan);
        TglAsuhan.setBounds(512, 70, 135, 23);

        NmPetugas2.setEditable(false);
        NmPetugas2.setName("NmPetugas2"); // NOI18N
        NmPetugas2.setPreferredSize(new java.awt.Dimension(207, 23));
        FormInput.add(NmPetugas2);
        NmPetugas2.setBounds(614, 40, 210, 23);

        BtnPetugas2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPetugas2.setMnemonic('2');
        BtnPetugas2.setToolTipText("Alt+2");
        BtnPetugas2.setName("BtnPetugas2"); // NOI18N
        BtnPetugas2.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnPetugas2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPetugas2ActionPerformed(evt);
            }
        });
        BtnPetugas2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPetugas2KeyPressed(evt);
            }
        });
        FormInput.add(BtnPetugas2);
        BtnPetugas2.setBounds(826, 40, 28, 23);

        KdPetugas2.setEditable(false);
        KdPetugas2.setName("KdPetugas2"); // NOI18N
        KdPetugas2.setPreferredSize(new java.awt.Dimension(80, 23));
        KdPetugas2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdPetugas2KeyPressed(evt);
            }
        });
        FormInput.add(KdPetugas2);
        KdPetugas2.setBounds(512, 40, 100, 23);

        label15.setText("Pengkaji 2 :");
        label15.setName("label15"); // NOI18N
        label15.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label15);
        label15.setBounds(438, 40, 70, 23);

        label16.setText("DPJP :");
        label16.setName("label16"); // NOI18N
        label16.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label16);
        label16.setBounds(0, 70, 70, 23);

        KdDPJP.setEditable(false);
        KdDPJP.setName("KdDPJP"); // NOI18N
        KdDPJP.setPreferredSize(new java.awt.Dimension(80, 23));
        KdDPJP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdDPJPKeyPressed(evt);
            }
        });
        FormInput.add(KdDPJP);
        KdDPJP.setBounds(74, 70, 110, 23);

        NmDPJP.setEditable(false);
        NmDPJP.setName("NmDPJP"); // NOI18N
        NmDPJP.setPreferredSize(new java.awt.Dimension(207, 23));
        FormInput.add(NmDPJP);
        NmDPJP.setBounds(186, 70, 230, 23);

        BtnDPJP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnDPJP.setMnemonic('2');
        BtnDPJP.setToolTipText("Alt+2");
        BtnDPJP.setName("BtnDPJP"); // NOI18N
        BtnDPJP.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnDPJP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnDPJPActionPerformed(evt);
            }
        });
        BtnDPJP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnDPJPKeyPressed(evt);
            }
        });
        FormInput.add(BtnDPJP);
        BtnDPJP.setBounds(418, 70, 28, 23);

        TibadiRuang.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Jalan Tanpa Bantuan", "Kursi Roda", "Brankar" }));
        TibadiRuang.setName("TibadiRuang"); // NOI18N
        TibadiRuang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TibadiRuangKeyPressed(evt);
            }
        });
        FormInput.add(TibadiRuang);
        TibadiRuang.setBounds(516, 100, 155, 23);

        jLabel37.setText("Tiba Di Ruang Rawat :");
        jLabel37.setName("jLabel37"); // NOI18N
        FormInput.add(jLabel37);
        jLabel37.setBounds(392, 100, 120, 23);

        CaraMasuk.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Poli", "IGD", "Lain-lain" }));
        CaraMasuk.setSelectedIndex(2);
        CaraMasuk.setName("CaraMasuk"); // NOI18N
        CaraMasuk.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                CaraMasukKeyPressed(evt);
            }
        });
        FormInput.add(CaraMasuk);
        CaraMasuk.setBounds(759, 100, 95, 23);

        jLabel38.setText("Cara Masuk :");
        jLabel38.setName("jLabel38"); // NOI18N
        FormInput.add(jLabel38);
        jLabel38.setBounds(685, 100, 70, 23);

        jLabel94.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel94.setText("I. RIWAYAT KESEHATAN");
        jLabel94.setName("jLabel94"); // NOI18N
        FormInput.add(jLabel94);
        jLabel94.setBounds(10, 250, 180, 23);

        jLabel9.setText("Riwayat Penggunaan Obat :");
        jLabel9.setName("jLabel9"); // NOI18N
        FormInput.add(jLabel9);
        jLabel9.setBounds(450, 320, 150, 23);

        jLabel39.setText("Riwayat Alergi :");
        jLabel39.setName("jLabel39"); // NOI18N
        FormInput.add(jLabel39);
        jLabel39.setBounds(470, 400, 90, 23);

        Alergi.setFocusTraversalPolicyProvider(true);
        Alergi.setName("Alergi"); // NOI18N
        Alergi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AlergiKeyPressed(evt);
            }
        });
        FormInput.add(Alergi);
        Alergi.setBounds(570, 400, 280, 23);

        scrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane1.setName("scrollPane1"); // NOI18N

        RPS.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        RPS.setColumns(20);
        RPS.setRows(5);
        RPS.setName("RPS"); // NOI18N
        RPS.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RPSKeyPressed(evt);
            }
        });
        scrollPane1.setViewportView(RPS);

        FormInput.add(scrollPane1);
        scrollPane1.setBounds(180, 270, 280, 43);

        jLabel30.setText("Riwayat Penyakit Saat Ini :");
        jLabel30.setName("jLabel30"); // NOI18N
        FormInput.add(jLabel30);
        jLabel30.setBounds(0, 270, 175, 23);

        scrollPane2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane2.setName("scrollPane2"); // NOI18N

        RPK.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        RPK.setColumns(20);
        RPK.setRows(5);
        RPK.setName("RPK"); // NOI18N
        RPK.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RPKKeyPressed(evt);
            }
        });
        scrollPane2.setViewportView(RPK);

        FormInput.add(scrollPane2);
        scrollPane2.setBounds(180, 320, 265, 43);

        jLabel31.setText("Riwayat Penyakit Keluarga :");
        jLabel31.setName("jLabel31"); // NOI18N
        FormInput.add(jLabel31);
        jLabel31.setBounds(0, 320, 180, 23);

        scrollPane3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane3.setName("scrollPane3"); // NOI18N

        RPD.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        RPD.setColumns(20);
        RPD.setRows(5);
        RPD.setName("RPD"); // NOI18N
        RPD.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RPDKeyPressed(evt);
            }
        });
        scrollPane3.setViewportView(RPD);

        FormInput.add(scrollPane3);
        scrollPane3.setBounds(600, 270, 250, 43);

        jLabel32.setText("Riwayat Penyakit Dahulu :");
        jLabel32.setName("jLabel32"); // NOI18N
        FormInput.add(jLabel32);
        jLabel32.setBounds(460, 270, 140, 23);

        scrollPane4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane4.setName("scrollPane4"); // NOI18N

        RPO.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        RPO.setColumns(20);
        RPO.setRows(5);
        RPO.setName("RPO"); // NOI18N
        RPO.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RPOKeyPressed(evt);
            }
        });
        scrollPane4.setViewportView(RPO);

        FormInput.add(scrollPane4);
        scrollPane4.setBounds(600, 320, 250, 43);

        jSeparator2.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator2.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator2.setName("jSeparator2"); // NOI18N
        FormInput.add(jSeparator2);
        jSeparator2.setBounds(0, 130, 880, 1);

        MacamKasus.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Trauma", "Non Trauma" }));
        MacamKasus.setSelectedIndex(1);
        MacamKasus.setName("MacamKasus"); // NOI18N
        MacamKasus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MacamKasusKeyPressed(evt);
            }
        });
        FormInput.add(MacamKasus);
        MacamKasus.setBounds(742, 70, 112, 23);

        jLabel41.setText("Macam Kasus :");
        jLabel41.setName("jLabel41"); // NOI18N
        FormInput.add(jLabel41);
        jLabel41.setBounds(658, 70, 80, 23);

        KetAnamnesis.setFocusTraversalPolicyProvider(true);
        KetAnamnesis.setName("KetAnamnesis"); // NOI18N
        KetAnamnesis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KetAnamnesisKeyPressed(evt);
            }
        });
        FormInput.add(KetAnamnesis);
        KetAnamnesis.setBounds(208, 100, 175, 23);

        jLabel40.setText("Riwayat Dirawat Di RS :");
        jLabel40.setName("jLabel40"); // NOI18N
        FormInput.add(jLabel40);
        jLabel40.setBounds(440, 370, 130, 23);

        RDirawatRS.setFocusTraversalPolicyProvider(true);
        RDirawatRS.setName("RDirawatRS"); // NOI18N
        RDirawatRS.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RDirawatRSKeyPressed(evt);
            }
        });
        FormInput.add(RDirawatRS);
        RDirawatRS.setBounds(570, 370, 280, 23);

        RPembedahan.setFocusTraversalPolicyProvider(true);
        RPembedahan.setName("RPembedahan"); // NOI18N
        RPembedahan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RPembedahanKeyPressed(evt);
            }
        });
        FormInput.add(RPembedahan);
        RPembedahan.setBounds(160, 370, 280, 23);

        jLabel42.setText("Riwayat Pembedahan :");
        jLabel42.setName("jLabel42"); // NOI18N
        FormInput.add(jLabel42);
        jLabel42.setBounds(0, 370, 156, 23);

        jLabel43.setText("Alat Bantu Yang Dipakai :");
        jLabel43.setName("jLabel43"); // NOI18N
        FormInput.add(jLabel43);
        jLabel43.setBounds(0, 400, 168, 23);

        AlatBantuDipakai.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Kacamata", "Prothesa", "Alat Bantu Dengar", "Lain-lain" }));
        AlatBantuDipakai.setSelectedIndex(3);
        AlatBantuDipakai.setName("AlatBantuDipakai"); // NOI18N
        AlatBantuDipakai.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AlatBantuDipakaiKeyPressed(evt);
            }
        });
        FormInput.add(AlatBantuDipakai);
        AlatBantuDipakai.setBounds(170, 400, 140, 23);

        jLabel45.setText("Riwayat Transfusi Darah :");
        jLabel45.setName("jLabel45"); // NOI18N
        FormInput.add(jLabel45);
        jLabel45.setBounds(0, 430, 170, 23);

        RTranfusi.setFocusTraversalPolicyProvider(true);
        RTranfusi.setName("RTranfusi"); // NOI18N
        RTranfusi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RTranfusiKeyPressed(evt);
            }
        });
        FormInput.add(RTranfusi);
        RTranfusi.setBounds(170, 430, 300, 23);

        jLabel46.setText("Riwayat Imunisasi :");
        jLabel46.setName("jLabel46"); // NOI18N
        FormInput.add(jLabel46);
        jLabel46.setBounds(30, 460, 101, 23);

        jLabel95.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel95.setText("II. PEMERIKSAAN FISIK");
        jLabel95.setName("jLabel95"); // NOI18N
        FormInput.add(jLabel95);
        jLabel95.setBounds(10, 540, 180, 23);

        jSeparator3.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator3.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator3.setName("jSeparator3"); // NOI18N
        FormInput.add(jSeparator3);
        jSeparator3.setBounds(0, 538, 880, 1);

        jLabel47.setText("Kesadaran :");
        jLabel47.setName("jLabel47"); // NOI18N
        FormInput.add(jLabel47);
        jLabel47.setBounds(0, 560, 138, 23);

        KesadaranMental.setFocusTraversalPolicyProvider(true);
        KesadaranMental.setName("KesadaranMental"); // NOI18N
        KesadaranMental.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KesadaranMentalKeyPressed(evt);
            }
        });
        FormInput.add(KesadaranMental);
        KesadaranMental.setBounds(140, 560, 175, 23);

        jLabel130.setText("Keadaan Umum :");
        jLabel130.setName("jLabel130"); // NOI18N
        FormInput.add(jLabel130);
        jLabel130.setBounds(350, 560, 90, 23);

        KeadaanMentalUmum.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Baik", "Sedang", "Buruk" }));
        KeadaanMentalUmum.setName("KeadaanMentalUmum"); // NOI18N
        KeadaanMentalUmum.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeadaanMentalUmumKeyPressed(evt);
            }
        });
        FormInput.add(KeadaanMentalUmum);
        KeadaanMentalUmum.setBounds(440, 560, 90, 23);

        jLabel28.setText("GCS(E,V,M) :");
        jLabel28.setName("jLabel28"); // NOI18N
        FormInput.add(jLabel28);
        jLabel28.setBounds(550, 560, 70, 23);

        GCS.setFocusTraversalPolicyProvider(true);
        GCS.setName("GCS"); // NOI18N
        GCS.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                GCSKeyPressed(evt);
            }
        });
        FormInput.add(GCS);
        GCS.setBounds(630, 560, 75, 23);

        jLabel22.setText("TD :");
        jLabel22.setName("jLabel22"); // NOI18N
        FormInput.add(jLabel22);
        jLabel22.setBounds(720, 560, 30, 23);

        TD.setFocusTraversalPolicyProvider(true);
        TD.setName("TD"); // NOI18N
        TD.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TDKeyPressed(evt);
            }
        });
        FormInput.add(TD);
        TD.setBounds(760, 560, 65, 23);

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel23.setText("mmHg");
        jLabel23.setName("jLabel23"); // NOI18N
        FormInput.add(jLabel23);
        jLabel23.setBounds(830, 560, 40, 23);

        jLabel17.setText("Nadi :");
        jLabel17.setName("jLabel17"); // NOI18N
        FormInput.add(jLabel17);
        jLabel17.setBounds(0, 590, 73, 23);

        Nadi.setFocusTraversalPolicyProvider(true);
        Nadi.setName("Nadi"); // NOI18N
        Nadi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NadiKeyPressed(evt);
            }
        });
        FormInput.add(Nadi);
        Nadi.setBounds(80, 590, 50, 23);

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel16.setText("x/menit");
        jLabel16.setName("jLabel16"); // NOI18N
        FormInput.add(jLabel16);
        jLabel16.setBounds(130, 590, 50, 23);

        jLabel26.setText("RR :");
        jLabel26.setName("jLabel26"); // NOI18N
        FormInput.add(jLabel26);
        jLabel26.setBounds(180, 590, 50, 23);

        RR.setFocusTraversalPolicyProvider(true);
        RR.setName("RR"); // NOI18N
        RR.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RRKeyPressed(evt);
            }
        });
        FormInput.add(RR);
        RR.setBounds(240, 590, 50, 23);

        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel25.setText("x/menit");
        jLabel25.setName("jLabel25"); // NOI18N
        FormInput.add(jLabel25);
        jLabel25.setBounds(300, 590, 50, 23);

        jLabel18.setText("Suhu :");
        jLabel18.setName("jLabel18"); // NOI18N
        FormInput.add(jLabel18);
        jLabel18.setBounds(360, 590, 40, 23);

        Suhu.setFocusTraversalPolicyProvider(true);
        Suhu.setName("Suhu"); // NOI18N
        Suhu.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SuhuKeyPressed(evt);
            }
        });
        FormInput.add(Suhu);
        Suhu.setBounds(410, 590, 50, 23);

        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel20.setText("°C");
        jLabel20.setName("jLabel20"); // NOI18N
        FormInput.add(jLabel20);
        jLabel20.setBounds(460, 590, 30, 23);

        jLabel24.setText("SpO2 :");
        jLabel24.setName("jLabel24"); // NOI18N
        FormInput.add(jLabel24);
        jLabel24.setBounds(500, 590, 40, 23);

        SpO2.setFocusTraversalPolicyProvider(true);
        SpO2.setName("SpO2"); // NOI18N
        SpO2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SpO2KeyPressed(evt);
            }
        });
        FormInput.add(SpO2);
        SpO2.setBounds(540, 590, 50, 23);

        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel29.setText("%");
        jLabel29.setName("jLabel29"); // NOI18N
        FormInput.add(jLabel29);
        jLabel29.setBounds(600, 590, 30, 23);

        jLabel12.setText("BB :");
        jLabel12.setName("jLabel12"); // NOI18N
        FormInput.add(jLabel12);
        jLabel12.setBounds(630, 590, 30, 23);

        BB.setFocusTraversalPolicyProvider(true);
        BB.setName("BB"); // NOI18N
        BB.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BBKeyPressed(evt);
            }
        });
        FormInput.add(BB);
        BB.setBounds(670, 590, 50, 23);

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel13.setText("Kg");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(725, 590, 30, 23);

        jLabel15.setText("TB :");
        jLabel15.setName("jLabel15"); // NOI18N
        FormInput.add(jLabel15);
        jLabel15.setBounds(750, 590, 30, 23);

        TB.setFocusTraversalPolicyProvider(true);
        TB.setName("TB"); // NOI18N
        TB.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TBKeyPressed(evt);
            }
        });
        FormInput.add(TB);
        TB.setBounds(790, 590, 50, 23);

        jLabel48.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel48.setText("cm");
        jLabel48.setName("jLabel48"); // NOI18N
        FormInput.add(jLabel48);
        jLabel48.setBounds(845, 590, 30, 23);

        jSeparator5.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator5.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator5.setName("jSeparator5"); // NOI18N
        FormInput.add(jSeparator5);
        jSeparator5.setBounds(0, 630, 880, 1);

        jLabel169.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel169.setText("III. POLA KEHIDUPAN SEHARI - HARI ");
        jLabel169.setName("jLabel169"); // NOI18N
        FormInput.add(jLabel169);
        jLabel169.setBounds(10, 630, 490, 23);

        jLabel54.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel54.setText("b. Pola Nutrisi :");
        jLabel54.setName("jLabel54"); // NOI18N
        FormInput.add(jLabel54);
        jLabel54.setBounds(30, 740, 100, 23);

        jLabel170.setText("Mandi :");
        jLabel170.setName("jLabel170"); // NOI18N
        FormInput.add(jLabel170);
        jLabel170.setBounds(10, 670, 109, 23);

        PolaAktifitasEliminasi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Mandiri", "Bantuan Orang Lain" }));
        PolaAktifitasEliminasi.setName("PolaAktifitasEliminasi"); // NOI18N
        PolaAktifitasEliminasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PolaAktifitasEliminasiKeyPressed(evt);
            }
        });
        FormInput.add(PolaAktifitasEliminasi);
        PolaAktifitasEliminasi.setBounds(120, 700, 164, 23);

        jLabel171.setText("Makan/Minum :");
        jLabel171.setName("jLabel171"); // NOI18N
        FormInput.add(jLabel171);
        jLabel171.setBounds(310, 670, 100, 23);

        jLabel172.setText("Berpakaian :");
        jLabel172.setName("jLabel172"); // NOI18N
        FormInput.add(jLabel172);
        jLabel172.setBounds(620, 670, 80, 23);

        jLabel173.setText("Eliminasi :");
        jLabel173.setName("jLabel173"); // NOI18N
        FormInput.add(jLabel173);
        jLabel173.setBounds(10, 700, 109, 23);

        jLabel174.setText("Berpindah :");
        jLabel174.setName("jLabel174"); // NOI18N
        FormInput.add(jLabel174);
        jLabel174.setBounds(310, 700, 100, 23);

        PolaAktifitasMandi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Mandiri", "Bantuan Orang Lain" }));
        PolaAktifitasMandi.setName("PolaAktifitasMandi"); // NOI18N
        PolaAktifitasMandi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PolaAktifitasMandiKeyPressed(evt);
            }
        });
        FormInput.add(PolaAktifitasMandi);
        PolaAktifitasMandi.setBounds(120, 670, 164, 23);

        PolaAktifitasMakan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Mandiri", "Bantuan Orang Lain" }));
        PolaAktifitasMakan.setName("PolaAktifitasMakan"); // NOI18N
        PolaAktifitasMakan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PolaAktifitasMakanKeyPressed(evt);
            }
        });
        FormInput.add(PolaAktifitasMakan);
        PolaAktifitasMakan.setBounds(420, 670, 164, 23);

        PolaAktifitasBerpakaian.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Mandiri", "Bantuan Orang Lain" }));
        PolaAktifitasBerpakaian.setName("PolaAktifitasBerpakaian"); // NOI18N
        PolaAktifitasBerpakaian.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PolaAktifitasBerpakaianKeyPressed(evt);
            }
        });
        FormInput.add(PolaAktifitasBerpakaian);
        PolaAktifitasBerpakaian.setBounds(700, 670, 164, 23);

        PolaAktifitasBerpindah.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Mandiri", "Bantuan Orang Lain" }));
        PolaAktifitasBerpindah.setName("PolaAktifitasBerpindah"); // NOI18N
        PolaAktifitasBerpindah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PolaAktifitasBerpindahKeyPressed(evt);
            }
        });
        FormInput.add(PolaAktifitasBerpindah);
        PolaAktifitasBerpindah.setBounds(420, 700, 164, 23);

        jLabel55.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel55.setText("a. Pola Aktifitas :");
        jLabel55.setName("jLabel55"); // NOI18N
        FormInput.add(jLabel55);
        jLabel55.setBounds(50, 650, 128, 23);

        jLabel121.setText("Porsi Makan");
        jLabel121.setName("jLabel121"); // NOI18N
        FormInput.add(jLabel121);
        jLabel121.setBounds(100, 740, 73, 23);

        PolaNutrisiPorsi.setName("PolaNutrisiPorsi"); // NOI18N
        PolaNutrisiPorsi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PolaNutrisiPorsiKeyPressed(evt);
            }
        });
        FormInput.add(PolaNutrisiPorsi);
        PolaNutrisiPorsi.setBounds(170, 740, 50, 23);

        jLabel122.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel122.setText("porsi,");
        jLabel122.setName("jLabel122"); // NOI18N
        FormInput.add(jLabel122);
        jLabel122.setBounds(230, 740, 31, 23);

        jLabel123.setText("Frekuensi Makan");
        jLabel123.setName("jLabel123"); // NOI18N
        FormInput.add(jLabel123);
        jLabel123.setBounds(250, 740, 94, 23);

        PolaNutrisiFrekuensi.setName("PolaNutrisiFrekuensi"); // NOI18N
        PolaNutrisiFrekuensi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PolaNutrisiFrekuensiKeyPressed(evt);
            }
        });
        FormInput.add(PolaNutrisiFrekuensi);
        PolaNutrisiFrekuensi.setBounds(350, 740, 50, 23);

        jLabel175.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel175.setText("x/hari,");
        jLabel175.setName("jLabel175"); // NOI18N
        FormInput.add(jLabel175);
        jLabel175.setBounds(400, 740, 40, 23);

        jLabel177.setText("Jenis Makanan");
        jLabel177.setName("jLabel177"); // NOI18N
        FormInput.add(jLabel177);
        jLabel177.setBounds(420, 740, 92, 23);

        PolaNutrisiJenis.setName("PolaNutrisiJenis"); // NOI18N
        PolaNutrisiJenis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PolaNutrisiJenisKeyPressed(evt);
            }
        });
        FormInput.add(PolaNutrisiJenis);
        PolaNutrisiJenis.setBounds(510, 740, 328, 23);

        jLabel56.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel56.setText("c. Pola Tidur :");
        jLabel56.setName("jLabel56"); // NOI18N
        FormInput.add(jLabel56);
        jLabel56.setBounds(30, 770, 80, 23);

        jLabel176.setText("Lama Tidur");
        jLabel176.setName("jLabel176"); // NOI18N
        FormInput.add(jLabel176);
        jLabel176.setBounds(100, 770, 60, 23);

        PolaTidurLama.setName("PolaTidurLama"); // NOI18N
        PolaTidurLama.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PolaTidurLamaKeyPressed(evt);
            }
        });
        FormInput.add(PolaTidurLama);
        PolaTidurLama.setBounds(160, 770, 50, 23);

        jLabel178.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel178.setText(" jam/hari,");
        jLabel178.setName("jLabel178"); // NOI18N
        FormInput.add(jLabel178);
        jLabel178.setBounds(210, 770, 51, 23);

        PolaTidurGangguan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada Gangguan", "Insomnia" }));
        PolaTidurGangguan.setName("PolaTidurGangguan"); // NOI18N
        PolaTidurGangguan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PolaTidurGangguanKeyPressed(evt);
            }
        });
        FormInput.add(PolaTidurGangguan);
        PolaTidurGangguan.setBounds(270, 770, 164, 23);

        jSeparator4.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator4.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator4.setName("jSeparator4"); // NOI18N
        FormInput.add(jSeparator4);
        jSeparator4.setBounds(0, 800, 880, 1);

        jLabel180.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel180.setText("IV. PENGKAJIAN FUNGSI");
        jLabel180.setName("jLabel180"); // NOI18N
        FormInput.add(jLabel180);
        jLabel180.setBounds(10, 810, 180, 23);

        jLabel179.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel179.setText("a. Kemampuan Aktifitas Sehari-hari");
        jLabel179.setName("jLabel179"); // NOI18N
        FormInput.add(jLabel179);
        jLabel179.setBounds(30, 830, 180, 23);

        AktifitasSehari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Mandiri", "Bantuan minimal", "Bantuan Sebagian", "Ketergantungan Total" }));
        AktifitasSehari2.setName("AktifitasSehari2"); // NOI18N
        AktifitasSehari2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AktifitasSehari2KeyPressed(evt);
            }
        });
        FormInput.add(AktifitasSehari2);
        AktifitasSehari2.setBounds(220, 830, 158, 23);

        jLabel181.setText("b. Berjalan :");
        jLabel181.setName("jLabel181"); // NOI18N
        FormInput.add(jLabel181);
        jLabel181.setBounds(380, 830, 70, 23);

        Berjalan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TAK", "Penurunan Kekuatan/ROM", "Paralisis", "Sering Jatuh", "Deformitas", "Hilang keseimbangan", "Riwayat Patah Tulang", "Lain-lain" }));
        Berjalan.setName("Berjalan"); // NOI18N
        Berjalan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BerjalanKeyPressed(evt);
            }
        });
        FormInput.add(Berjalan);
        Berjalan.setBounds(450, 830, 178, 23);

        KeteranganBerjalan.setFocusTraversalPolicyProvider(true);
        KeteranganBerjalan.setName("KeteranganBerjalan"); // NOI18N
        KeteranganBerjalan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganBerjalanKeyPressed(evt);
            }
        });
        FormInput.add(KeteranganBerjalan);
        KeteranganBerjalan.setBounds(640, 830, 204, 23);

        jLabel182.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel182.setText("c. Aktifitas");
        jLabel182.setName("jLabel182"); // NOI18N
        FormInput.add(jLabel182);
        jLabel182.setBounds(30, 860, 60, 23);

        Aktifitas.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tirah Baring", "Duduk", "Berjalan" }));
        Aktifitas.setName("Aktifitas"); // NOI18N
        Aktifitas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AktifitasKeyPressed(evt);
            }
        });
        FormInput.add(Aktifitas);
        Aktifitas.setBounds(100, 860, 110, 23);

        jLabel183.setText("d. Alat Ambulasi :");
        jLabel183.setName("jLabel183"); // NOI18N
        FormInput.add(jLabel183);
        jLabel183.setBounds(210, 860, 100, 23);

        AlatAmbulasi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Walker", "Tongkat", "Kursi Roda", "Tidak Menggunakan" }));
        AlatAmbulasi.setName("AlatAmbulasi"); // NOI18N
        AlatAmbulasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AlatAmbulasiKeyPressed(evt);
            }
        });
        FormInput.add(AlatAmbulasi);
        AlatAmbulasi.setBounds(320, 860, 147, 23);

        jLabel184.setText("e. Ekstremitas Atas :");
        jLabel184.setName("jLabel184"); // NOI18N
        FormInput.add(jLabel184);
        jLabel184.setBounds(470, 860, 110, 23);

        EkstrimitasAtas.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TAK", "Lemah", "Oedema", "Tidak Simetris", "Lain-lain" }));
        EkstrimitasAtas.setName("EkstrimitasAtas"); // NOI18N
        EkstrimitasAtas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                EkstrimitasAtasKeyPressed(evt);
            }
        });
        FormInput.add(EkstrimitasAtas);
        EkstrimitasAtas.setBounds(580, 860, 120, 23);

        KeteranganEkstrimitasAtas.setFocusTraversalPolicyProvider(true);
        KeteranganEkstrimitasAtas.setName("KeteranganEkstrimitasAtas"); // NOI18N
        KeteranganEkstrimitasAtas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganEkstrimitasAtasKeyPressed(evt);
            }
        });
        FormInput.add(KeteranganEkstrimitasAtas);
        KeteranganEkstrimitasAtas.setBounds(710, 860, 137, 23);

        jLabel185.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel185.setText("f. Ekstremitas Bawah");
        jLabel185.setName("jLabel185"); // NOI18N
        FormInput.add(jLabel185);
        jLabel185.setBounds(30, 890, 110, 23);

        EkstrimitasBawah.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TAK", "Varises", "Oedema", "Tidak Simetris", "Lain-lain" }));
        EkstrimitasBawah.setName("EkstrimitasBawah"); // NOI18N
        EkstrimitasBawah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                EkstrimitasBawahKeyPressed(evt);
            }
        });
        FormInput.add(EkstrimitasBawah);
        EkstrimitasBawah.setBounds(150, 890, 120, 23);

        KeteranganEkstrimitasBawah.setFocusTraversalPolicyProvider(true);
        KeteranganEkstrimitasBawah.setName("KeteranganEkstrimitasBawah"); // NOI18N
        KeteranganEkstrimitasBawah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganEkstrimitasBawahKeyPressed(evt);
            }
        });
        FormInput.add(KeteranganEkstrimitasBawah);
        KeteranganEkstrimitasBawah.setBounds(280, 890, 137, 23);

        jLabel186.setText("g. Kemampuan Menggenggam :");
        jLabel186.setName("jLabel186"); // NOI18N
        FormInput.add(jLabel186);
        jLabel186.setBounds(410, 890, 160, 23);

        KemampuanMenggenggam.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada Kesulitan", "Terakhir", "Lain-lain" }));
        KemampuanMenggenggam.setName("KemampuanMenggenggam"); // NOI18N
        KemampuanMenggenggam.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KemampuanMenggenggamKeyPressed(evt);
            }
        });
        FormInput.add(KemampuanMenggenggam);
        KemampuanMenggenggam.setBounds(580, 890, 147, 23);

        KeteranganKemampuanMenggenggam.setFocusTraversalPolicyProvider(true);
        KeteranganKemampuanMenggenggam.setName("KeteranganKemampuanMenggenggam"); // NOI18N
        KeteranganKemampuanMenggenggam.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganKemampuanMenggenggamKeyPressed(evt);
            }
        });
        FormInput.add(KeteranganKemampuanMenggenggam);
        KeteranganKemampuanMenggenggam.setBounds(730, 890, 115, 23);

        jLabel187.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel187.setText("h. Kemampuan Koordinasi");
        jLabel187.setName("jLabel187"); // NOI18N
        FormInput.add(jLabel187);
        jLabel187.setBounds(30, 920, 140, 23);

        KemampuanKoordinasi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada Kesulitan", "Ada Masalah" }));
        KemampuanKoordinasi.setName("KemampuanKoordinasi"); // NOI18N
        KemampuanKoordinasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KemampuanKoordinasiKeyPressed(evt);
            }
        });
        FormInput.add(KemampuanKoordinasi);
        KemampuanKoordinasi.setBounds(170, 920, 147, 23);

        KeteranganKemampuanKoordinasi.setFocusTraversalPolicyProvider(true);
        KeteranganKemampuanKoordinasi.setName("KeteranganKemampuanKoordinasi"); // NOI18N
        KeteranganKemampuanKoordinasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganKemampuanKoordinasiKeyPressed(evt);
            }
        });
        FormInput.add(KeteranganKemampuanKoordinasi);
        KeteranganKemampuanKoordinasi.setBounds(320, 920, 147, 23);

        jLabel188.setText("i. Kesimpulan Gangguan Fungsi :");
        jLabel188.setName("jLabel188"); // NOI18N
        FormInput.add(jLabel188);
        jLabel188.setBounds(470, 920, 170, 23);

        KesimpulanGangguanFungsi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak (Tidak Perlu Co DPJP)", "Ya (Co DPJP)" }));
        KesimpulanGangguanFungsi.setName("KesimpulanGangguanFungsi"); // NOI18N
        KesimpulanGangguanFungsi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KesimpulanGangguanFungsiKeyPressed(evt);
            }
        });
        FormInput.add(KesimpulanGangguanFungsi);
        KesimpulanGangguanFungsi.setBounds(650, 920, 195, 23);

        jSeparator6.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator6.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator6.setName("jSeparator6"); // NOI18N
        FormInput.add(jSeparator6);
        jSeparator6.setBounds(0, 950, 880, 1);

        jLabel189.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel189.setText("V. RIWAYAT PSIKOLOGIS – SOSIAL – EKONOMI – BUDAYA – SPIRITUAL");
        jLabel189.setName("jLabel189"); // NOI18N
        FormInput.add(jLabel189);
        jLabel189.setBounds(10, 960, 490, 23);

        jLabel190.setText(":");
        jLabel190.setName("jLabel190"); // NOI18N
        FormInput.add(jLabel190);
        jLabel190.setBounds(130, 980, 10, 23);

        KondisiPsikologis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada Masalah", "Marah", "Takut", "Depresi", "Cepat Lelah", "Cemas", "Gelisah", "Sulit Tidur", "Lain-lain" }));
        KondisiPsikologis.setName("KondisiPsikologis"); // NOI18N
        KondisiPsikologis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KondisiPsikologisKeyPressed(evt);
            }
        });
        FormInput.add(KondisiPsikologis);
        KondisiPsikologis.setBounds(150, 980, 142, 23);

        jLabel191.setText("b. Adakah Perilaku :");
        jLabel191.setName("jLabel191"); // NOI18N
        FormInput.add(jLabel191);
        jLabel191.setBounds(300, 980, 110, 23);

        AdakahPerilaku.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada Masalah", "Perilaku Kekerasan", "Gangguan Efek", "Gangguan Memori", "Halusinasi", "Kecenderungan Percobaan Bunuh Diri", "Lain-lain" }));
        AdakahPerilaku.setName("AdakahPerilaku"); // NOI18N
        AdakahPerilaku.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AdakahPerilakuKeyPressed(evt);
            }
        });
        FormInput.add(AdakahPerilaku);
        AdakahPerilaku.setBounds(410, 980, 235, 23);

        KeteranganAdakahPerilaku.setFocusTraversalPolicyProvider(true);
        KeteranganAdakahPerilaku.setName("KeteranganAdakahPerilaku"); // NOI18N
        KeteranganAdakahPerilaku.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganAdakahPerilakuKeyPressed(evt);
            }
        });
        FormInput.add(KeteranganAdakahPerilaku);
        KeteranganAdakahPerilaku.setBounds(650, 980, 202, 23);

        jLabel192.setText(":");
        jLabel192.setName("jLabel192"); // NOI18N
        FormInput.add(jLabel192);
        jLabel192.setBounds(180, 1010, 10, 23);

        GangguanJiwa.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak", "Ya" }));
        GangguanJiwa.setName("GangguanJiwa"); // NOI18N
        GangguanJiwa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                GangguanJiwaKeyPressed(evt);
            }
        });
        FormInput.add(GangguanJiwa);
        GangguanJiwa.setBounds(200, 1010, 77, 23);

        jLabel193.setText("d. Hubungan Pasien dengan Anggota Keluarga :");
        jLabel193.setName("jLabel193"); // NOI18N
        FormInput.add(jLabel193);
        jLabel193.setBounds(280, 1010, 240, 23);

        HubunganAnggotaKeluarga.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Harmonis", "Kurang Harmonis", "Tidak Harmonis", "Konflik Besar" }));
        HubunganAnggotaKeluarga.setName("HubunganAnggotaKeluarga"); // NOI18N
        HubunganAnggotaKeluarga.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                HubunganAnggotaKeluargaKeyPressed(evt);
            }
        });
        FormInput.add(HubunganAnggotaKeluarga);
        HubunganAnggotaKeluarga.setBounds(530, 1010, 133, 23);

        jLabel194.setText("e. Agama :");
        jLabel194.setName("jLabel194"); // NOI18N
        FormInput.add(jLabel194);
        jLabel194.setBounds(670, 1010, 60, 23);

        Agama.setEditable(false);
        Agama.setFocusTraversalPolicyProvider(true);
        Agama.setName("Agama"); // NOI18N
        FormInput.add(Agama);
        Agama.setBounds(730, 1010, 120, 23);

        jLabel195.setText(":");
        jLabel195.setName("jLabel195"); // NOI18N
        FormInput.add(jLabel195);
        jLabel195.setBounds(120, 1040, 10, 23);

        TinggalDengan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sendiri", "Orang Tua", "Suami/Istri", "Keluarga", "Lain-lain" }));
        TinggalDengan.setName("TinggalDengan"); // NOI18N
        TinggalDengan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TinggalDenganKeyPressed(evt);
            }
        });
        FormInput.add(TinggalDengan);
        TinggalDengan.setBounds(140, 1040, 105, 23);

        KeteranganTinggalDengan.setFocusTraversalPolicyProvider(true);
        KeteranganTinggalDengan.setName("KeteranganTinggalDengan"); // NOI18N
        KeteranganTinggalDengan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganTinggalDenganKeyPressed(evt);
            }
        });
        FormInput.add(KeteranganTinggalDengan);
        KeteranganTinggalDengan.setBounds(250, 1040, 137, 23);

        jLabel196.setText("g. Pekerjaan :");
        jLabel196.setName("jLabel196"); // NOI18N
        FormInput.add(jLabel196);
        jLabel196.setBounds(380, 1040, 83, 23);

        PekerjaanPasien.setEditable(false);
        PekerjaanPasien.setFocusTraversalPolicyProvider(true);
        PekerjaanPasien.setName("PekerjaanPasien"); // NOI18N
        FormInput.add(PekerjaanPasien);
        PekerjaanPasien.setBounds(470, 1040, 140, 23);

        jLabel197.setText("h. Pembayaran :");
        jLabel197.setName("jLabel197"); // NOI18N
        FormInput.add(jLabel197);
        jLabel197.setBounds(610, 1040, 90, 23);

        CaraBayar.setEditable(false);
        CaraBayar.setFocusTraversalPolicyProvider(true);
        CaraBayar.setName("CaraBayar"); // NOI18N
        FormInput.add(CaraBayar);
        CaraBayar.setBounds(710, 1040, 140, 23);

        jLabel198.setText(":");
        jLabel198.setName("jLabel198"); // NOI18N
        FormInput.add(jLabel198);
        jLabel198.setBounds(310, 1070, 10, 23);

        NilaiKepercayaan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada", "Ada" }));
        NilaiKepercayaan.setName("NilaiKepercayaan"); // NOI18N
        NilaiKepercayaan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NilaiKepercayaanKeyPressed(evt);
            }
        });
        FormInput.add(NilaiKepercayaan);
        NilaiKepercayaan.setBounds(340, 1070, 105, 23);

        KeteranganNilaiKepercayaan.setFocusTraversalPolicyProvider(true);
        KeteranganNilaiKepercayaan.setName("KeteranganNilaiKepercayaan"); // NOI18N
        KeteranganNilaiKepercayaan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganNilaiKepercayaanKeyPressed(evt);
            }
        });
        FormInput.add(KeteranganNilaiKepercayaan);
        KeteranganNilaiKepercayaan.setBounds(450, 1070, 160, 23);

        jLabel199.setText("j. Bahasa Sehari-hari :");
        jLabel199.setName("jLabel199"); // NOI18N
        FormInput.add(jLabel199);
        jLabel199.setBounds(610, 1070, 120, 23);

        Bahasa.setEditable(false);
        Bahasa.setFocusTraversalPolicyProvider(true);
        Bahasa.setName("Bahasa"); // NOI18N
        FormInput.add(Bahasa);
        Bahasa.setBounds(730, 1070, 120, 23);

        jLabel200.setText(":");
        jLabel200.setName("jLabel200"); // NOI18N
        FormInput.add(jLabel200);
        jLabel200.setBounds(140, 1100, 10, 23);

        PendidikanPasien.setEditable(false);
        PendidikanPasien.setFocusTraversalPolicyProvider(true);
        PendidikanPasien.setName("PendidikanPasien"); // NOI18N
        FormInput.add(PendidikanPasien);
        PendidikanPasien.setBounds(150, 1100, 100, 23);

        jLabel201.setText("l. Pendidikan P.J. :");
        jLabel201.setName("jLabel201"); // NOI18N
        FormInput.add(jLabel201);
        jLabel201.setBounds(250, 1100, 100, 23);

        PendidikanPJ.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-", "TS", "TK", "SD", "SMP", "SMA", "SLTA/SEDERAJAT", "D1", "D2", "D3", "D4", "S1", "S2", "S3" }));
        PendidikanPJ.setName("PendidikanPJ"); // NOI18N
        PendidikanPJ.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PendidikanPJKeyPressed(evt);
            }
        });
        FormInput.add(PendidikanPJ);
        PendidikanPJ.setBounds(360, 1100, 135, 23);

        jLabel202.setText("m. Edukasi Diberikan Kepada :");
        jLabel202.setName("jLabel202"); // NOI18N
        FormInput.add(jLabel202);
        jLabel202.setBounds(490, 1100, 160, 23);

        EdukasiPsikolgis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Pasien", "Keluarga" }));
        EdukasiPsikolgis.setName("EdukasiPsikolgis"); // NOI18N
        EdukasiPsikolgis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                EdukasiPsikolgisKeyPressed(evt);
            }
        });
        FormInput.add(EdukasiPsikolgis);
        EdukasiPsikolgis.setBounds(650, 1100, 95, 23);

        KeteranganEdukasiPsikologis.setFocusTraversalPolicyProvider(true);
        KeteranganEdukasiPsikologis.setName("KeteranganEdukasiPsikologis"); // NOI18N
        KeteranganEdukasiPsikologis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganEdukasiPsikologisKeyPressed(evt);
            }
        });
        FormInput.add(KeteranganEdukasiPsikologis);
        KeteranganEdukasiPsikologis.setBounds(750, 1100, 99, 23);

        jSeparator8.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator8.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator8.setName("jSeparator8"); // NOI18N
        FormInput.add(jSeparator8);
        jSeparator8.setBounds(0, 1128, 880, 1);

        jLabel203.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel203.setText("VI. PENILAIAN TINGKAT NYERI");
        jLabel203.setName("jLabel203"); // NOI18N
        FormInput.add(jLabel203);
        jLabel203.setBounds(10, 1160, 380, 23);

        jSeparator10.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator10.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator10.setName("jSeparator10"); // NOI18N
        FormInput.add(jSeparator10);
        jSeparator10.setBounds(0, 1310, 880, 1);

        jSeparator11.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator11.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator11.setName("jSeparator11"); // NOI18N
        FormInput.add(jSeparator11);
        jSeparator11.setBounds(20, 1990, 880, 0);

        jLabel271.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel271.setText("VIII. SKRINING GIZI");
        jLabel271.setName("jLabel271"); // NOI18N
        FormInput.add(jLabel271);
        jLabel271.setBounds(30, 1570, 380, 23);

        jLabel272.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel272.setText("1. Apakah ada penurunan BB yang tidak diinginkan selama 6 bulan terakhir ?");
        jLabel272.setName("jLabel272"); // NOI18N
        FormInput.add(jLabel272);
        jLabel272.setBounds(60, 1590, 380, 23);

        SkalaGizi1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak ada penurunan berat badan", "Tidak yakin/ tidak tahu/ terasa baju lebih longgar", "Ya 1-5 kg", "Ya 6-10 kg", "Ya 11-15 kg", "Ya > 15 kg" }));
        SkalaGizi1.setName("SkalaGizi1"); // NOI18N
        SkalaGizi1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                SkalaGizi1ItemStateChanged(evt);
            }
        });
        SkalaGizi1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SkalaGizi1KeyPressed(evt);
            }
        });
        FormInput.add(SkalaGizi1);
        SkalaGizi1.setBounds(440, 1590, 320, 23);

        jLabel273.setText("Skor :");
        jLabel273.setName("jLabel273"); // NOI18N
        FormInput.add(jLabel273);
        jLabel273.setBounds(790, 1590, 40, 23);

        NilaiGizi1.setEditable(false);
        NilaiGizi1.setFocusTraversalPolicyProvider(true);
        NilaiGizi1.setName("NilaiGizi1"); // NOI18N
        FormInput.add(NilaiGizi1);
        NilaiGizi1.setBounds(840, 1590, 60, 23);

        jLabel274.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel274.setText("2. Apakah asupan makan berkurang karena tidak nafsu makan ?");
        jLabel274.setName("jLabel274"); // NOI18N
        FormInput.add(jLabel274);
        jLabel274.setBounds(60, 1620, 380, 23);

        SkalaGizi2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak", "Ya" }));
        SkalaGizi2.setName("SkalaGizi2"); // NOI18N
        SkalaGizi2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                SkalaGizi2ItemStateChanged(evt);
            }
        });
        SkalaGizi2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SkalaGizi2KeyPressed(evt);
            }
        });
        FormInput.add(SkalaGizi2);
        SkalaGizi2.setBounds(440, 1620, 320, 23);

        jLabel275.setText("Skor :");
        jLabel275.setName("jLabel275"); // NOI18N
        FormInput.add(jLabel275);
        jLabel275.setBounds(790, 1620, 40, 23);

        NilaiGizi2.setEditable(false);
        NilaiGizi2.setFocusTraversalPolicyProvider(true);
        NilaiGizi2.setName("NilaiGizi2"); // NOI18N
        FormInput.add(NilaiGizi2);
        NilaiGizi2.setBounds(840, 1620, 60, 23);

        jLabel276.setText("Total Skor :");
        jLabel276.setName("jLabel276"); // NOI18N
        FormInput.add(jLabel276);
        jLabel276.setBounds(720, 1650, 110, 23);

        NilaiGiziTotal.setEditable(false);
        NilaiGiziTotal.setFocusTraversalPolicyProvider(true);
        NilaiGiziTotal.setName("NilaiGiziTotal"); // NOI18N
        FormInput.add(NilaiGiziTotal);
        NilaiGiziTotal.setBounds(840, 1650, 60, 23);

        jLabel277.setText("Pasien dengan diagnosis khusus : ");
        jLabel277.setName("jLabel277"); // NOI18N
        FormInput.add(jLabel277);
        jLabel277.setBounds(20, 1680, 206, 23);

        DiagnosaKhususGizi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak", "Ya" }));
        DiagnosaKhususGizi.setName("DiagnosaKhususGizi"); // NOI18N
        DiagnosaKhususGizi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DiagnosaKhususGiziKeyPressed(evt);
            }
        });
        FormInput.add(DiagnosaKhususGizi);
        DiagnosaKhususGizi.setBounds(230, 1680, 80, 23);

        KeteranganDiagnosaKhususGizi.setFocusTraversalPolicyProvider(true);
        KeteranganDiagnosaKhususGizi.setName("KeteranganDiagnosaKhususGizi"); // NOI18N
        KeteranganDiagnosaKhususGizi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganDiagnosaKhususGiziKeyPressed(evt);
            }
        });
        FormInput.add(KeteranganDiagnosaKhususGizi);
        KeteranganDiagnosaKhususGizi.setBounds(320, 1680, 150, 23);

        jLabel278.setText("Sudah dibaca dan diketahui oleh Dietisen :");
        jLabel278.setName("jLabel278"); // NOI18N
        FormInput.add(jLabel278);
        jLabel278.setBounds(470, 1680, 220, 23);

        DiketahuiDietisen.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak", "Ya" }));
        DiketahuiDietisen.setName("DiketahuiDietisen"); // NOI18N
        DiketahuiDietisen.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DiketahuiDietisenKeyPressed(evt);
            }
        });
        FormInput.add(DiketahuiDietisen);
        DiketahuiDietisen.setBounds(690, 1680, 80, 23);

        jLabel279.setText("Jam  :");
        jLabel279.setName("jLabel279"); // NOI18N
        FormInput.add(jLabel279);
        jLabel279.setBounds(790, 1680, 40, 23);

        KeteranganDiketahuiDietisen.setFocusTraversalPolicyProvider(true);
        KeteranganDiketahuiDietisen.setName("KeteranganDiketahuiDietisen"); // NOI18N
        KeteranganDiketahuiDietisen.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganDiketahuiDietisenKeyPressed(evt);
            }
        });
        FormInput.add(KeteranganDiketahuiDietisen);
        KeteranganDiketahuiDietisen.setBounds(840, 1680, 60, 23);

        jSeparator12.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator12.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator12.setName("jSeparator12"); // NOI18N
        FormInput.add(jSeparator12);
        jSeparator12.setBounds(0, 1569, 880, 2);

        Scroll6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 253)));
        Scroll6.setName("Scroll6"); // NOI18N
        Scroll6.setOpaque(true);

        tbMasalahKeperawatan.setName("tbMasalahKeperawatan"); // NOI18N
        tbMasalahKeperawatan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbMasalahKeperawatanMouseClicked(evt);
            }
        });
        tbMasalahKeperawatan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbMasalahKeperawatanKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbMasalahKeperawatanKeyReleased(evt);
            }
        });
        Scroll6.setViewportView(tbMasalahKeperawatan);

        FormInput.add(Scroll6);
        Scroll6.setBounds(30, 1720, 400, 143);

        TabRencanaKeperawatan.setBackground(new java.awt.Color(255, 255, 254));
        TabRencanaKeperawatan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TabRencanaKeperawatan.setForeground(new java.awt.Color(50, 50, 50));
        TabRencanaKeperawatan.setName("TabRencanaKeperawatan"); // NOI18N

        panelBiasa1.setName("panelBiasa1"); // NOI18N
        panelBiasa1.setLayout(new java.awt.BorderLayout());

        Scroll8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 253)));
        Scroll8.setName("Scroll8"); // NOI18N
        Scroll8.setOpaque(true);

        tbRencanaKeperawatan.setName("tbRencanaKeperawatan"); // NOI18N
        Scroll8.setViewportView(tbRencanaKeperawatan);

        panelBiasa1.add(Scroll8, java.awt.BorderLayout.CENTER);

        TabRencanaKeperawatan.addTab("Rencana Keperawatan", panelBiasa1);

        scrollPane5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane5.setName("scrollPane5"); // NOI18N

        Rencana.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Rencana.setColumns(20);
        Rencana.setRows(5);
        Rencana.setName("Rencana"); // NOI18N
        Rencana.setOpaque(true);
        Rencana.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RencanaKeyPressed(evt);
            }
        });
        scrollPane5.setViewportView(Rencana);

        TabRencanaKeperawatan.addTab("Rencana Keperawatan Lainnya", scrollPane5);

        FormInput.add(TabRencanaKeperawatan);
        TabRencanaKeperawatan.setBounds(450, 1720, 420, 143);

        BtnTambahMasalah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        BtnTambahMasalah.setMnemonic('3');
        BtnTambahMasalah.setToolTipText("Alt+3");
        BtnTambahMasalah.setName("BtnTambahMasalah"); // NOI18N
        BtnTambahMasalah.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnTambahMasalah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTambahMasalahActionPerformed(evt);
            }
        });
        FormInput.add(BtnTambahMasalah);
        BtnTambahMasalah.setBounds(380, 1870, 28, 23);

        BtnAllMasalah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAllMasalah.setMnemonic('2');
        BtnAllMasalah.setToolTipText("2Alt+2");
        BtnAllMasalah.setName("BtnAllMasalah"); // NOI18N
        BtnAllMasalah.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnAllMasalah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllMasalahActionPerformed(evt);
            }
        });
        BtnAllMasalah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnAllMasalahKeyPressed(evt);
            }
        });
        FormInput.add(BtnAllMasalah);
        BtnAllMasalah.setBounds(350, 1870, 28, 23);

        BtnCariMasalah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCariMasalah.setMnemonic('1');
        BtnCariMasalah.setToolTipText("Alt+1");
        BtnCariMasalah.setName("BtnCariMasalah"); // NOI18N
        BtnCariMasalah.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCariMasalah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariMasalahActionPerformed(evt);
            }
        });
        BtnCariMasalah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariMasalahKeyPressed(evt);
            }
        });
        FormInput.add(BtnCariMasalah);
        BtnCariMasalah.setBounds(320, 1870, 28, 23);

        TCariMasalah.setToolTipText("Alt+C");
        TCariMasalah.setName("TCariMasalah"); // NOI18N
        TCariMasalah.setPreferredSize(new java.awt.Dimension(140, 23));
        TCariMasalah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariMasalahKeyPressed(evt);
            }
        });
        FormInput.add(TCariMasalah);
        TCariMasalah.setBounds(100, 1870, 215, 23);

        BtnTambahRencana.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        BtnTambahRencana.setMnemonic('3');
        BtnTambahRencana.setToolTipText("Alt+3");
        BtnTambahRencana.setName("BtnTambahRencana"); // NOI18N
        BtnTambahRencana.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnTambahRencana.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTambahRencanaActionPerformed(evt);
            }
        });
        FormInput.add(BtnTambahRencana);
        BtnTambahRencana.setBounds(830, 1870, 28, 23);

        BtnAllRencana.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAllRencana.setMnemonic('2');
        BtnAllRencana.setToolTipText("2Alt+2");
        BtnAllRencana.setName("BtnAllRencana"); // NOI18N
        BtnAllRencana.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnAllRencana.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllRencanaActionPerformed(evt);
            }
        });
        BtnAllRencana.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnAllRencanaKeyPressed(evt);
            }
        });
        FormInput.add(BtnAllRencana);
        BtnAllRencana.setBounds(790, 1870, 28, 23);

        BtnCariRencana.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCariRencana.setMnemonic('1');
        BtnCariRencana.setToolTipText("Alt+1");
        BtnCariRencana.setName("BtnCariRencana"); // NOI18N
        BtnCariRencana.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCariRencana.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariRencanaActionPerformed(evt);
            }
        });
        BtnCariRencana.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariRencanaKeyPressed(evt);
            }
        });
        FormInput.add(BtnCariRencana);
        BtnCariRencana.setBounds(760, 1870, 28, 23);

        label13.setText("Key Word :");
        label13.setName("label13"); // NOI18N
        label13.setPreferredSize(new java.awt.Dimension(60, 23));
        FormInput.add(label13);
        label13.setBounds(460, 1870, 60, 23);

        TCariRencana.setToolTipText("Alt+C");
        TCariRencana.setName("TCariRencana"); // NOI18N
        TCariRencana.setPreferredSize(new java.awt.Dimension(215, 23));
        TCariRencana.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariRencanaKeyPressed(evt);
            }
        });
        FormInput.add(TCariRencana);
        TCariRencana.setBounds(520, 1870, 235, 23);

        label12.setText("Key Word :");
        label12.setName("label12"); // NOI18N
        label12.setPreferredSize(new java.awt.Dimension(60, 23));
        FormInput.add(label12);
        label12.setBounds(40, 1870, 60, 23);

        jLabel280.setText(":");
        jLabel280.setName("jLabel280"); // NOI18N
        FormInput.add(jLabel280);
        jLabel280.setBounds(160, 920, 10, 23);

        jLabel281.setText(":");
        jLabel281.setName("jLabel281"); // NOI18N
        FormInput.add(jLabel281);
        jLabel281.setBounds(140, 890, 10, 23);

        jLabel282.setText(":");
        jLabel282.setName("jLabel282"); // NOI18N
        FormInput.add(jLabel282);
        jLabel282.setBounds(90, 860, 10, 23);

        jLabel283.setText(":");
        jLabel283.setName("jLabel283"); // NOI18N
        FormInput.add(jLabel283);
        jLabel283.setBounds(210, 830, 10, 23);

        jLabel284.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel284.setText("a. Kondisi Psikologis");
        jLabel284.setName("jLabel284"); // NOI18N
        FormInput.add(jLabel284);
        jLabel284.setBounds(40, 980, 110, 23);

        jLabel285.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel285.setText("c. Gangguan Jiwa di Masa Lalu");
        jLabel285.setName("jLabel285"); // NOI18N
        FormInput.add(jLabel285);
        jLabel285.setBounds(40, 1010, 160, 23);

        jLabel286.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel286.setText("f. Tinggal Dengan");
        jLabel286.setName("jLabel286"); // NOI18N
        FormInput.add(jLabel286);
        jLabel286.setBounds(40, 1040, 100, 23);

        jLabel287.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel287.setText("i. Nilai-nilai Kepercayaan/Budaya Yang Perlu Diperhatikan");
        jLabel287.setName("jLabel287"); // NOI18N
        FormInput.add(jLabel287);
        jLabel287.setBounds(40, 1070, 290, 23);

        jLabel288.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel288.setText("k. Pendidikan Pasien");
        jLabel288.setName("jLabel288"); // NOI18N
        FormInput.add(jLabel288);
        jLabel288.setBounds(40, 1100, 110, 23);

        jLabel59.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel59.setText("Skala Humpty Dumpty :");
        jLabel59.setName("jLabel59"); // NOI18N
        FormInput.add(jLabel59);
        jLabel59.setBounds(30, 1310, 130, 23);

        jLabel289.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel289.setText("1. Umur");
        jLabel289.setName("jLabel289"); // NOI18N
        FormInput.add(jLabel289);
        jLabel289.setBounds(50, 1330, 200, 23);

        SkalaResiko7.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0 - 3 Tahun", "3 - 7 Tahun", "7 - 13 Tahun", "> 13 Tahun" }));
        SkalaResiko7.setName("SkalaResiko7"); // NOI18N
        SkalaResiko7.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                SkalaResiko7ItemStateChanged(evt);
            }
        });
        SkalaResiko7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SkalaResiko7ActionPerformed(evt);
            }
        });
        SkalaResiko7.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SkalaResiko7KeyPressed(evt);
            }
        });
        FormInput.add(SkalaResiko7);
        SkalaResiko7.setBounds(260, 1330, 500, 23);

        NilaiResiko7.setEditable(false);
        NilaiResiko7.setFocusTraversalPolicyProvider(true);
        NilaiResiko7.setName("NilaiResiko7"); // NOI18N
        FormInput.add(NilaiResiko7);
        NilaiResiko7.setBounds(780, 1330, 50, 23);

        SkalaResiko8.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Laki-laki", "Perempuan" }));
        SkalaResiko8.setName("SkalaResiko8"); // NOI18N
        SkalaResiko8.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                SkalaResiko8ItemStateChanged(evt);
            }
        });
        SkalaResiko8.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SkalaResiko8KeyPressed(evt);
            }
        });
        FormInput.add(SkalaResiko8);
        SkalaResiko8.setBounds(260, 1360, 500, 23);

        NilaiResiko8.setEditable(false);
        NilaiResiko8.setFocusTraversalPolicyProvider(true);
        NilaiResiko8.setName("NilaiResiko8"); // NOI18N
        FormInput.add(NilaiResiko8);
        NilaiResiko8.setBounds(780, 1360, 50, 23);

        jLabel290.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel290.setText("2. Jenis Kelamin");
        jLabel290.setName("jLabel290"); // NOI18N
        FormInput.add(jLabel290);
        jLabel290.setBounds(50, 1360, 200, 23);

        jLabel291.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel291.setText("3. Diagnosa");
        jLabel291.setName("jLabel291"); // NOI18N
        FormInput.add(jLabel291);
        jLabel291.setBounds(50, 1390, 200, 23);

        jLabel292.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel292.setText("4. Gangguan Kognitif");
        jLabel292.setName("jLabel292"); // NOI18N
        FormInput.add(jLabel292);
        jLabel292.setBounds(50, 1420, 200, 23);

        SkalaResiko9.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Kelainan Neurologi", "Perubahan Dalam Oksigen(Masalah Saluran Nafas, Dehidrasi, Anemia, Anoreksia / Sakit Kepala, Dll)", "Kelainan Psikis / Perilaku", "Diagnosa Lain" }));
        SkalaResiko9.setName("SkalaResiko9"); // NOI18N
        SkalaResiko9.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                SkalaResiko9ItemStateChanged(evt);
            }
        });
        SkalaResiko9.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SkalaResiko9KeyPressed(evt);
            }
        });
        FormInput.add(SkalaResiko9);
        SkalaResiko9.setBounds(260, 1390, 500, 23);

        SkalaResiko10.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Sadar Terhadap Keterbatasan", "Lupa Keterbatasan", "Mengetahui Kemampuan Diri" }));
        SkalaResiko10.setName("SkalaResiko10"); // NOI18N
        SkalaResiko10.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                SkalaResiko10ItemStateChanged(evt);
            }
        });
        SkalaResiko10.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SkalaResiko10KeyPressed(evt);
            }
        });
        FormInput.add(SkalaResiko10);
        SkalaResiko10.setBounds(260, 1420, 500, 23);

        NilaiResiko9.setEditable(false);
        NilaiResiko9.setFocusTraversalPolicyProvider(true);
        NilaiResiko9.setName("NilaiResiko9"); // NOI18N
        FormInput.add(NilaiResiko9);
        NilaiResiko9.setBounds(780, 1390, 50, 23);

        NilaiResiko10.setEditable(false);
        NilaiResiko10.setFocusTraversalPolicyProvider(true);
        NilaiResiko10.setName("NilaiResiko10"); // NOI18N
        FormInput.add(NilaiResiko10);
        NilaiResiko10.setBounds(780, 1420, 50, 23);

        SkalaResiko11.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Riwayat Jatuh Dari Tempat Tidur Saat Bayi/Anak", "Pasien Menggunakan Alat Bantu/Box/Mebel", "Pasien Berada Di Tempat Tidur", "Di Luar Ruang Rawat" }));
        SkalaResiko11.setName("SkalaResiko11"); // NOI18N
        SkalaResiko11.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                SkalaResiko11ItemStateChanged(evt);
            }
        });
        SkalaResiko11.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SkalaResiko11KeyPressed(evt);
            }
        });
        FormInput.add(SkalaResiko11);
        SkalaResiko11.setBounds(260, 1450, 500, 23);

        jLabel293.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel293.setText("5. Faktor Lingkungan");
        jLabel293.setName("jLabel293"); // NOI18N
        FormInput.add(jLabel293);
        jLabel293.setBounds(50, 1450, 200, 23);

        jLabel294.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel294.setText("6. Efek Obat Penenang/Operasi/Anastesi");
        jLabel294.setName("jLabel294"); // NOI18N
        FormInput.add(jLabel294);
        jLabel294.setBounds(50, 1480, 210, 23);

        jLabel295.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel295.setText("7. Penggunaan Obat");
        jLabel295.setName("jLabel295"); // NOI18N
        FormInput.add(jLabel295);
        jLabel295.setBounds(50, 1510, 210, 23);

        SkalaResiko12.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Dalam 24 Jam", "Dalam 48 Jam", "> 48 Jam" }));
        SkalaResiko12.setName("SkalaResiko12"); // NOI18N
        SkalaResiko12.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                SkalaResiko12ItemStateChanged(evt);
            }
        });
        SkalaResiko12.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SkalaResiko12KeyPressed(evt);
            }
        });
        FormInput.add(SkalaResiko12);
        SkalaResiko12.setBounds(260, 1480, 500, 23);

        SkalaResiko13.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Bermacam-macam Obat Yang Digunakan : Obat Sedative (Kecuali Pasien ICU Yang Menggunakan sedasi dan paralisis), Hipnotik, Barbiturat, Fenoti-Azin, Antidepresan, Laksans/Diuretika,Narkotik", "Salah Satu Dari Pengobatan Di Atas", "Pengobatan Lain" }));
        SkalaResiko13.setName("SkalaResiko13"); // NOI18N
        SkalaResiko13.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                SkalaResiko13ItemStateChanged(evt);
            }
        });
        SkalaResiko13.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SkalaResiko13KeyPressed(evt);
            }
        });
        FormInput.add(SkalaResiko13);
        SkalaResiko13.setBounds(260, 1510, 500, 23);

        NilaiResiko11.setEditable(false);
        NilaiResiko11.setFocusTraversalPolicyProvider(true);
        NilaiResiko11.setName("NilaiResiko11"); // NOI18N
        FormInput.add(NilaiResiko11);
        NilaiResiko11.setBounds(780, 1450, 50, 23);

        NilaiResiko12.setEditable(false);
        NilaiResiko12.setFocusTraversalPolicyProvider(true);
        NilaiResiko12.setName("NilaiResiko12"); // NOI18N
        FormInput.add(NilaiResiko12);
        NilaiResiko12.setBounds(780, 1480, 50, 23);

        NilaiResikoTotal1.setEditable(false);
        NilaiResikoTotal1.setFocusTraversalPolicyProvider(true);
        NilaiResikoTotal1.setName("NilaiResikoTotal1"); // NOI18N
        FormInput.add(NilaiResikoTotal1);
        NilaiResikoTotal1.setBounds(780, 1540, 50, 23);

        jLabel296.setText("Total :");
        jLabel296.setName("jLabel296"); // NOI18N
        FormInput.add(jLabel296);
        jLabel296.setBounds(690, 1540, 70, 23);

        TingkatResiko1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        TingkatResiko1.setText("Tingkat Resiko : Risiko Tinggi (>=12), Tindakan : Intervensi risiko jatuh tinggi(pasang gelang warna kuning)");
        TingkatResiko1.setToolTipText("");
        TingkatResiko1.setName("TingkatResiko1"); // NOI18N
        FormInput.add(TingkatResiko1);
        TingkatResiko1.setBounds(50, 1540, 640, 23);

        NilaiResiko13.setEditable(false);
        NilaiResiko13.setFocusTraversalPolicyProvider(true);
        NilaiResiko13.setName("NilaiResiko13"); // NOI18N
        FormInput.add(NilaiResiko13);
        NilaiResiko13.setBounds(780, 1510, 50, 23);

        jLabel49.setText("Riwayat Perkembangan Anak Dibanding Saudara:");
        jLabel49.setName("jLabel49"); // NOI18N
        FormInput.add(jLabel49);
        jLabel49.setBounds(460, 430, 260, 23);

        RPerkmAnk.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Cepat", "Lambat", "Sama", "Lainnya" }));
        RPerkmAnk.setName("RPerkmAnk"); // NOI18N
        RPerkmAnk.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RPerkmAnkKeyPressed(evt);
            }
        });
        FormInput.add(RPerkmAnk);
        RPerkmAnk.setBounds(490, 460, 80, 23);

        KetPerkmAnk.setFocusTraversalPolicyProvider(true);
        KetPerkmAnk.setName("KetPerkmAnk"); // NOI18N
        KetPerkmAnk.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KetPerkmAnkKeyPressed(evt);
            }
        });
        FormInput.add(KetPerkmAnk);
        KetPerkmAnk.setBounds(580, 460, 270, 23);

        jLabel33.setText("KELUHAN UTAMA");
        jLabel33.setName("jLabel33"); // NOI18N
        FormInput.add(jLabel33);
        jLabel33.setBounds(0, 140, 130, 23);

        scrollPane7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane7.setName("scrollPane7"); // NOI18N

        KelUtama.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        KelUtama.setColumns(20);
        KelUtama.setRows(5);
        KelUtama.setName("KelUtama"); // NOI18N
        KelUtama.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KelUtamaKeyPressed(evt);
            }
        });
        scrollPane7.setViewportView(KelUtama);

        FormInput.add(scrollPane7);
        scrollPane7.setBounds(140, 140, 710, 100);

        scrollPane8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane8.setName("scrollPane8"); // NOI18N

        RiImunisasi.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        RiImunisasi.setColumns(20);
        RiImunisasi.setRows(5);
        RiImunisasi.setName("RiImunisasi"); // NOI18N
        RiImunisasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RiImunisasiKeyPressed(evt);
            }
        });
        scrollPane8.setViewportView(RiImunisasi);

        FormInput.add(scrollPane8);
        scrollPane8.setBounds(140, 460, 330, 70);

        jLabel164.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel164.setText("Skala FLACCS :");
        jLabel164.setName("jLabel164"); // NOI18N
        FormInput.add(jLabel164);
        jLabel164.setBounds(30, 1180, 210, 23);

        jLabel165.setText("Wajah :");
        jLabel165.setName("jLabel165"); // NOI18N
        FormInput.add(jLabel165);
        jLabel165.setBounds(40, 1200, 60, 23);

        SkalaWajah.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tersenyum/tidak ada ekspresi khusus", "Terkadang meringis/menarik diri", "Sering menggetarkan dagu dan mengatupkan rahang" }));
        SkalaWajah.setName("SkalaWajah"); // NOI18N
        SkalaWajah.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                SkalaWajahItemStateChanged(evt);
            }
        });
        SkalaWajah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SkalaWajahKeyPressed(evt);
            }
        });
        FormInput.add(SkalaWajah);
        SkalaWajah.setBounds(100, 1200, 310, 23);

        SkalaKaki.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Gerakan normal/relaksasi", "Tidak tenang/tegang", "Kaki dibuat menendang/menarik" }));
        SkalaKaki.setName("SkalaKaki"); // NOI18N
        SkalaKaki.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                SkalaKakiItemStateChanged(evt);
            }
        });
        SkalaKaki.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SkalaKakiKeyPressed(evt);
            }
        });
        FormInput.add(SkalaKaki);
        SkalaKaki.setBounds(100, 1230, 310, 23);

        SkalaAktifitas.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidur posisi normal, mudah bergerak", "Gerakan menggeliat/berguling, kaku", "Melengkungkan punggung/kaku menghentak" }));
        SkalaAktifitas.setName("SkalaAktifitas"); // NOI18N
        SkalaAktifitas.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                SkalaAktifitasItemStateChanged(evt);
            }
        });
        SkalaAktifitas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SkalaAktifitasKeyPressed(evt);
            }
        });
        FormInput.add(SkalaAktifitas);
        SkalaAktifitas.setBounds(100, 1260, 310, 23);

        NilaiAktifitas.setEditable(false);
        NilaiAktifitas.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        NilaiAktifitas.setText("0");
        NilaiAktifitas.setFocusTraversalPolicyProvider(true);
        NilaiAktifitas.setName("NilaiAktifitas"); // NOI18N
        NilaiAktifitas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NilaiAktifitasKeyPressed(evt);
            }
        });
        FormInput.add(NilaiAktifitas);
        NilaiAktifitas.setBounds(420, 1260, 40, 23);

        NilaiKaki.setEditable(false);
        NilaiKaki.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        NilaiKaki.setText("0");
        NilaiKaki.setFocusTraversalPolicyProvider(true);
        NilaiKaki.setName("NilaiKaki"); // NOI18N
        NilaiKaki.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NilaiKakiKeyPressed(evt);
            }
        });
        FormInput.add(NilaiKaki);
        NilaiKaki.setBounds(420, 1230, 40, 23);

        NilaiWajah.setEditable(false);
        NilaiWajah.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        NilaiWajah.setText("0");
        NilaiWajah.setFocusTraversalPolicyProvider(true);
        NilaiWajah.setName("NilaiWajah"); // NOI18N
        NilaiWajah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NilaiWajahKeyPressed(evt);
            }
        });
        FormInput.add(NilaiWajah);
        NilaiWajah.setBounds(420, 1200, 40, 23);

        jLabel168.setText("Menangis :");
        jLabel168.setName("jLabel168"); // NOI18N
        FormInput.add(jLabel168);
        jLabel168.setBounds(470, 1200, 60, 23);

        jLabel297.setText("Bersuara :");
        jLabel297.setName("jLabel297"); // NOI18N
        FormInput.add(jLabel297);
        jLabel297.setBounds(470, 1230, 60, 23);

        SkalaMenangis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak menangis (mudah bergerak)", "Mengerang/merengek", "Menangis terus menerus, terisak, menjerit" }));
        SkalaMenangis.setName("SkalaMenangis"); // NOI18N
        SkalaMenangis.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                SkalaMenangisItemStateChanged(evt);
            }
        });
        SkalaMenangis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SkalaMenangisKeyPressed(evt);
            }
        });
        FormInput.add(SkalaMenangis);
        SkalaMenangis.setBounds(540, 1200, 266, 23);

        SkalaBersuara.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Bersuara normal/tenang", "Tenang bila dipeluk, digendong/diajak bicara", "Sulit untuk menenangkan" }));
        SkalaBersuara.setName("SkalaBersuara"); // NOI18N
        SkalaBersuara.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                SkalaBersuaraItemStateChanged(evt);
            }
        });
        SkalaBersuara.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SkalaBersuaraKeyPressed(evt);
            }
        });
        FormInput.add(SkalaBersuara);
        SkalaBersuara.setBounds(540, 1230, 266, 23);

        NilaiMenangis.setEditable(false);
        NilaiMenangis.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        NilaiMenangis.setText("0");
        NilaiMenangis.setFocusTraversalPolicyProvider(true);
        NilaiMenangis.setName("NilaiMenangis"); // NOI18N
        NilaiMenangis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NilaiMenangisKeyPressed(evt);
            }
        });
        FormInput.add(NilaiMenangis);
        NilaiMenangis.setBounds(810, 1200, 40, 23);

        NilaiBersuara.setEditable(false);
        NilaiBersuara.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        NilaiBersuara.setText("0");
        NilaiBersuara.setFocusTraversalPolicyProvider(true);
        NilaiBersuara.setName("NilaiBersuara"); // NOI18N
        NilaiBersuara.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NilaiBersuaraKeyPressed(evt);
            }
        });
        FormInput.add(NilaiBersuara);
        NilaiBersuara.setBounds(810, 1230, 40, 23);

        SkalaNyeri1.setEditable(false);
        SkalaNyeri1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        SkalaNyeri1.setText("0");
        SkalaNyeri1.setFocusTraversalPolicyProvider(true);
        SkalaNyeri1.setName("SkalaNyeri1"); // NOI18N
        SkalaNyeri1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SkalaNyeri1KeyPressed(evt);
            }
        });
        FormInput.add(SkalaNyeri1);
        SkalaNyeri1.setBounds(590, 1270, 80, 23);

        jLabel298.setText("Skala nyeri :");
        jLabel298.setName("jLabel298"); // NOI18N
        FormInput.add(jLabel298);
        jLabel298.setBounds(490, 1270, 90, 23);

        jLabel167.setText("Aktifitas :");
        jLabel167.setName("jLabel167"); // NOI18N
        FormInput.add(jLabel167);
        jLabel167.setBounds(40, 1260, 60, 23);

        jLabel166.setText("Kaki :");
        jLabel166.setName("jLabel166"); // NOI18N
        FormInput.add(jLabel166);
        jLabel166.setBounds(40, 1230, 60, 23);

        totatskalanyeri.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        totatskalanyeri.setText("TIDAK NYERI");
        totatskalanyeri.setToolTipText("");
        totatskalanyeri.setFont(new java.awt.Font("Segoe UI Emoji", 1, 14)); // NOI18N
        totatskalanyeri.setName("totatskalanyeri"); // NOI18N
        FormInput.add(totatskalanyeri);
        totatskalanyeri.setBounds(680, 1250, 200, 50);

        scrollInput.setViewportView(FormInput);

        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Input Penilaian", internalFrame2);

        internalFrame3.setBorder(null);
        internalFrame3.setName("internalFrame3"); // NOI18N
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));

        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setName("tbObat"); // NOI18N
        tbObat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbObatMouseClicked(evt);
            }
        });
        tbObat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbObatKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbObat);

        internalFrame3.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel19.setText("Tgl.Asuhan :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "05-05-2024" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari1);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d.");
        jLabel21.setName("jLabel21"); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(23, 23));
        panelGlass9.add(jLabel21);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "05-05-2024" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(195, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('3');
        BtnCari.setToolTipText("Alt+3");
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

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass9.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(LCount);

        internalFrame3.add(panelGlass9, java.awt.BorderLayout.PAGE_END);

        PanelAccor.setBackground(new java.awt.Color(255, 255, 255));
        PanelAccor.setName("PanelAccor"); // NOI18N
        PanelAccor.setPreferredSize(new java.awt.Dimension(470, 43));
        PanelAccor.setLayout(new java.awt.BorderLayout(1, 1));

        ChkAccor.setBackground(new java.awt.Color(255, 250, 250));
        ChkAccor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kiri.png"))); // NOI18N
        ChkAccor.setSelected(true);
        ChkAccor.setFocusable(false);
        ChkAccor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ChkAccor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ChkAccor.setName("ChkAccor"); // NOI18N
        ChkAccor.setPreferredSize(new java.awt.Dimension(15, 20));
        ChkAccor.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kiri.png"))); // NOI18N
        ChkAccor.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kanan.png"))); // NOI18N
        ChkAccor.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kanan.png"))); // NOI18N
        ChkAccor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkAccorActionPerformed(evt);
            }
        });
        PanelAccor.add(ChkAccor, java.awt.BorderLayout.WEST);

        FormMenu.setBackground(new java.awt.Color(255, 255, 255));
        FormMenu.setBorder(null);
        FormMenu.setName("FormMenu"); // NOI18N
        FormMenu.setPreferredSize(new java.awt.Dimension(115, 43));
        FormMenu.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 9));

        jLabel34.setText("Pasien :");
        jLabel34.setName("jLabel34"); // NOI18N
        jLabel34.setPreferredSize(new java.awt.Dimension(55, 23));
        FormMenu.add(jLabel34);

        TNoRM1.setEditable(false);
        TNoRM1.setName("TNoRM1"); // NOI18N
        TNoRM1.setPreferredSize(new java.awt.Dimension(100, 23));
        FormMenu.add(TNoRM1);

        TPasien1.setEditable(false);
        TPasien1.setBackground(new java.awt.Color(245, 250, 240));
        TPasien1.setName("TPasien1"); // NOI18N
        TPasien1.setPreferredSize(new java.awt.Dimension(250, 23));
        FormMenu.add(TPasien1);

        BtnPrint1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item (copy).png"))); // NOI18N
        BtnPrint1.setMnemonic('T');
        BtnPrint1.setToolTipText("Alt+T");
        BtnPrint1.setName("BtnPrint1"); // NOI18N
        BtnPrint1.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnPrint1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrint1ActionPerformed(evt);
            }
        });
        FormMenu.add(BtnPrint1);

        PanelAccor.add(FormMenu, java.awt.BorderLayout.NORTH);

        FormMasalahRencana.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 254)));
        FormMasalahRencana.setName("FormMasalahRencana"); // NOI18N
        FormMasalahRencana.setLayout(new java.awt.GridLayout(3, 0, 1, 1));

        Scroll7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 254)));
        Scroll7.setName("Scroll7"); // NOI18N
        Scroll7.setOpaque(true);

        tbMasalahDetail.setName("tbMasalahDetail"); // NOI18N
        Scroll7.setViewportView(tbMasalahDetail);

        FormMasalahRencana.add(Scroll7);

        Scroll9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 254)));
        Scroll9.setName("Scroll9"); // NOI18N
        Scroll9.setOpaque(true);

        tbRencanaDetail.setName("tbRencanaDetail"); // NOI18N
        Scroll9.setViewportView(tbRencanaDetail);

        FormMasalahRencana.add(Scroll9);

        scrollPane6.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 254)), "Rencana Keperawatan Lainnya :", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        scrollPane6.setName("scrollPane6"); // NOI18N

        DetailRencana.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 1));
        DetailRencana.setColumns(20);
        DetailRencana.setRows(5);
        DetailRencana.setName("DetailRencana"); // NOI18N
        DetailRencana.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DetailRencanaKeyPressed(evt);
            }
        });
        scrollPane6.setViewportView(DetailRencana);

        FormMasalahRencana.add(scrollPane6);

        PanelAccor.add(FormMasalahRencana, java.awt.BorderLayout.CENTER);

        internalFrame3.add(PanelAccor, java.awt.BorderLayout.EAST);

        TabRawat.addTab("Data Penilaian", internalFrame3);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRM.getText().trim().equals("")){
    Valid.textKosong(TNoRw,"Nama Pasien");
} else if(KdPetugas.getText().trim().equals("") || NmPetugas.getText().trim().equals("")){
    Valid.textKosong(BtnPetugas,"Pengkaji 1");
} else if(KdPetugas2.getText().trim().equals("") || NmPetugas2.getText().trim().equals("")){
    Valid.textKosong(BtnPetugas2,"Pegkaji 2");
} else if(KdDPJP.getText().trim().equals("") || NmDPJP.getText().trim().equals("")){
    Valid.textKosong(BtnDPJP,"DPJP");
} else if(RPS.getText().trim().equals("")){
    Valid.textKosong(RPS,"Riwayat Penyakit Sekarang");
} else {
    // Lakukan penyimpanan (upsert -- replace into, supaya bisa DIEDIT ulang tanpa gagal
    // "Duplicate entry", beda dgn versi lama yg cuma insert murni & gagal kalau no_rawat sudah ada)
     if (simpanUpsertAnak(new String[]{
    TNoRw.getText(),
    Valid.SetTgl(TglAsuhan.getSelectedItem() + "") + " " + TglAsuhan.getSelectedItem().toString().substring(11, 19),
    Anamnesis.getSelectedItem().toString(),
    KetAnamnesis.getText(),
    TibadiRuang.getSelectedItem().toString(),
    MacamKasus.getSelectedItem().toString(),
    CaraMasuk.getSelectedItem().toString(),
    KelUtama.getText(),
    RPS.getText(),
    RPD.getText(),
    RPK.getText(),
    RPO.getText(),
    RPembedahan.getText(),
    RDirawatRS.getText(),
    AlatBantuDipakai.getSelectedItem().toString(),
    RTranfusi.getText(),
    RiImunisasi.getText(),
    RPerkmAnk.getSelectedItem().toString(),
    KetPerkmAnk.getText(),
    Alergi.getText(),
    KesadaranMental.getText(),
    KeadaanMentalUmum.getSelectedItem().toString(),
    GCS.getText(),
    TD.getText(),
    Nadi.getText(),
    RR.getText(),
    Suhu.getText(),
    SpO2.getText(),
    BB.getText(),
    TB.getText(),
    PolaAktifitasMakan.getSelectedItem().toString(),
    PolaAktifitasMandi.getSelectedItem().toString(),
    PolaAktifitasEliminasi.getSelectedItem().toString(),
    PolaAktifitasBerpakaian.getSelectedItem().toString(),
    PolaAktifitasBerpindah.getSelectedItem().toString(),
    PolaNutrisiFrekuensi.getText(),
    PolaNutrisiJenis.getText(),
    PolaNutrisiPorsi.getText(),
    PolaTidurLama.getText(),
    PolaTidurGangguan.getSelectedItem().toString(),
    AktifitasSehari2.getSelectedItem().toString(),
    Aktifitas.getSelectedItem().toString(),
    Berjalan.getSelectedItem().toString(),
    KeteranganBerjalan.getText(),
    AlatAmbulasi.getSelectedItem().toString(),
    EkstrimitasAtas.getSelectedItem().toString(),
    KeteranganEkstrimitasAtas.getText(),
    EkstrimitasBawah.getSelectedItem().toString(),
    KeteranganEkstrimitasBawah.getText(),
    KemampuanMenggenggam.getSelectedItem().toString(),
    KeteranganKemampuanMenggenggam.getText(),
    KemampuanKoordinasi.getSelectedItem().toString(),
    KeteranganKemampuanKoordinasi.getText(),
    KesimpulanGangguanFungsi.getSelectedItem().toString(),
    KondisiPsikologis.getSelectedItem().toString(),
    GangguanJiwa.getSelectedItem().toString(),
    AdakahPerilaku.getSelectedItem().toString(),
    KeteranganAdakahPerilaku.getText(),
    HubunganAnggotaKeluarga.getSelectedItem().toString(),
    TinggalDengan.getSelectedItem().toString(),
    KeteranganTinggalDengan.getText(),
    NilaiKepercayaan.getSelectedItem().toString(),
    KeteranganNilaiKepercayaan.getText(),
    PendidikanPJ.getSelectedItem().toString(),
    EdukasiPsikolgis.getSelectedItem().toString(),
    KeteranganEdukasiPsikologis.getText(),
    SkalaWajah.getSelectedItem().toString(),
    NilaiWajah.getText(),
    SkalaKaki.getSelectedItem().toString(),
    NilaiKaki.getText(),
    SkalaAktifitas.getSelectedItem().toString(),
    NilaiAktifitas.getText(),
    SkalaMenangis.getSelectedItem().toString(),
    NilaiMenangis.getText(),
    SkalaBersuara.getSelectedItem().toString(),
    NilaiBersuara.getText(),
    SkalaNyeri1.getText(),
    SkalaResiko7.getSelectedItem().toString(),
    NilaiResiko7.getText(),
    SkalaResiko8.getSelectedItem().toString(),
    NilaiResiko8.getText(),
    SkalaResiko9.getSelectedItem().toString(),
    NilaiResiko9.getText(),
    SkalaResiko10.getSelectedItem().toString(),
    NilaiResiko10.getText(),
    SkalaResiko11.getSelectedItem().toString(),
    NilaiResiko11.getText(),
    SkalaResiko12.getSelectedItem().toString(),
    NilaiResiko12.getText(),
    SkalaResiko13.getSelectedItem().toString(),
    NilaiResiko13.getText(),
    NilaiResikoTotal1.getText(),
    SkalaGizi1.getSelectedItem().toString(),
    NilaiGizi1.getText(),
    SkalaGizi2.getSelectedItem().toString(),
    NilaiGizi2.getText(),
    NilaiGiziTotal.getText(),
    DiagnosaKhususGizi.getSelectedItem().toString(),
    KeteranganDiagnosaKhususGizi.getText(),
    DiketahuiDietisen.getSelectedItem().toString(),
    KeteranganDiketahuiDietisen.getText(),
    Rencana.getText(),
    KdPetugas.getText(),
    KdPetugas2.getText(),
    KdDPJP.getText()
}) == true) {
    for (i = 0; i < tbMasalahKeperawatan.getRowCount(); i++) {
        if (tbMasalahKeperawatan.getValueAt(i, 0).toString().equals("true")) {
            Sequel.menyimpan2("penilaian_awal_keperawatan_ranap_masalah", "?,?", 2, new String[]{TNoRw.getText(), tbMasalahKeperawatan.getValueAt(i, 1).toString()});
        }
    }
    for (i = 0; i < tbRencanaKeperawatan.getRowCount(); i++) {
        if (tbRencanaKeperawatan.getValueAt(i, 0).toString().equals("true")) {
            Sequel.menyimpan2("penilaian_awal_keperawatan_ranap_rencana", "?,?", 2, new String[]{TNoRw.getText(), tbRencanaKeperawatan.getValueAt(i, 1).toString()});
        }
    }


        
//        // Melakukan penyimpanan untuk humty_dumpty_keperawatan
//        if(Sequel.menyimpantf("humty_dumpty_keperawatan", "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?", "Data", 16, new String[]{
//            TNoRw.getText(), 
//            SkalaResiko7.getSelectedItem().toString(), NilaiResiko7.getText(), SkalaResiko8.getSelectedItem().toString(), NilaiResiko8.getText(),
//            SkalaResiko9.getSelectedItem().toString(), NilaiResiko9.getText(), SkalaResiko10.getSelectedItem().toString(), NilaiResiko10.getText(), 
//            SkalaResiko11.getSelectedItem().toString(), NilaiResiko11.getText(), SkalaResiko12.getSelectedItem().toString(), NilaiResiko12.getText(),
//            SkalaResiko13.getSelectedItem().toString(), NilaiResiko13.getText(), NilaiResikoTotal1.getText()
//        })==true)
{
//            tabMode.addRow(new String[]{
//                TNoRw.getText(),
//                SkalaResiko7.getSelectedItem().toString(), NilaiResiko7.getText(), SkalaResiko8.getSelectedItem().toString(), NilaiResiko8.getText(), SkalaResiko9.getSelectedItem().toString(), NilaiResiko9.getText(),
//                SkalaResiko10.getSelectedItem().toString(), NilaiResiko10.getText(), SkalaResiko11.getSelectedItem().toString(), NilaiResiko11.getText(), SkalaResiko12.getSelectedItem().toString(), NilaiResiko12.getText(),
//                SkalaResiko13.getSelectedItem().toString(), NilaiResiko13.getText(), NilaiResikoTotal1.getText()
//        });
        emptTeks();
    }
    }
}
           
        

}//GEN-LAST:event_BtnSimpanActionPerformed

    /** Urutan KOLOM fisik penilaian_awal_keperawatan_ranap_anak (dari information_schema), 105 kolom -- dipakai simpanUpsertAnak() & muat(). */
    private static final String[] KOLOM_ANAK = {
        "no_rawat","tanggal","informasi","ket_informasi","tiba_diruang_rawat","kasus_trauma","cara_masuk","kel_utama",
        "rps","rpd","rpk","rpo","riwayat_pembedahan","riwayat_dirawat_dirs","alat_bantu_dipakai","riwayat_tranfusi",
        "riwayat_imunisasi","riwayat_prkmbngan","riwayat_prkmbngan_lainnya","riwayat_alergi","pemeriksaan_mental",
        "pemeriksaan_keadaan_umum","pemeriksaan_gcs","pemeriksaan_td","pemeriksaan_nadi","pemeriksaan_rr",
        "pemeriksaan_suhu","pemeriksaan_spo2","pemeriksaan_bb","pemeriksaan_tb","pola_aktifitas_makanminum",
        "pola_aktifitas_mandi","pola_aktifitas_eliminasi","pola_aktifitas_berpakaian","pola_aktifitas_berpindah",
        "pola_nutrisi_frekuesi_makan","pola_nutrisi_jenis_makanan","pola_nutrisi_porsi_makan","pola_tidur_lama_tidur",
        "pola_tidur_gangguan","pengkajian_fungsi_kemampuan_sehari","pengkajian_fungsi_aktifitas",
        "pengkajian_fungsi_berjalan","pengkajian_fungsi_berjalan_keterangan","pengkajian_fungsi_ambulasi",
        "pengkajian_fungsi_ekstrimitas_atas","pengkajian_fungsi_ekstrimitas_atas_keterangan",
        "pengkajian_fungsi_ekstrimitas_bawah","pengkajian_fungsi_ekstrimitas_bawah_keterangan",
        "pengkajian_fungsi_menggenggam","pengkajian_fungsi_menggenggam_keterangan","pengkajian_fungsi_koordinasi",
        "pengkajian_fungsi_koordinasi_keterangan","pengkajian_fungsi_kesimpulan","riwayat_psiko_kondisi_psiko",
        "riwayat_psiko_gangguan_jiwa","riwayat_psiko_perilaku","riwayat_psiko_perilaku_keterangan",
        "riwayat_psiko_hubungan_keluarga","riwayat_psiko_tinggal","riwayat_psiko_tinggal_keterangan",
        "riwayat_psiko_nilai_kepercayaan","riwayat_psiko_nilai_kepercayaan_keterangan","riwayat_psiko_pendidikan_pj",
        "riwayat_psiko_edukasi_diberikan","riwayat_psiko_edukasi_diberikan_keterangan","wajah","nilaiwajah","kaki",
        "nilaikaki","aktifitas","nilaiaktifitas","menangis","nilaimenangis","bersuara","nilaibersuara","hasilnyeri",
        "penilaian_humptydumpty_skala1","penilaian_humptydumpty_nilai1","penilaian_humptydumpty_skala2",
        "penilaian_humptydumpty_nilai2","penilaian_humptydumpty_skala3","penilaian_humptydumpty_nilai3",
        "penilaian_humptydumpty_skala4","penilaian_humptydumpty_nilai4","penilaian_humptydumpty_skala5",
        "penilaian_humptydumpty_nilai5","penilaian_humptydumpty_skala6","penilaian_humptydumpty_nilai6",
        "penilaian_humptydumpty_skala7","penilaian_humptydumpty_nilai7","penilaian_humptydumpty_totalnilai",
        "skrining_gizi1","nilai_gizi1","skrining_gizi2","nilai_gizi2","nilai_total_gizi",
        "skrining_gizi_diagnosa_khusus","skrining_gizi_ket_diagnosa_khusus","skrining_gizi_diketahui_dietisen",
        "skrining_gizi_jam_diketahui_dietisen","rencana","nip1","nip2","kd_dokter"
    };

    /**
     * Simpan (upsert) penilaian_awal_keperawatan_ranap_anak -- ganti dari INSERT murni (Sequel.menyimpantf 5-arg,
     * gagal "Duplicate entry" kalau no_rawat sudah ada, TIDAK BISA DIEDIT) ke REPLACE INTO (aman, tidak ada tabel
     * lain yg FK ke tabel ini -- dicek via information_schema.key_column_usage sblm dipakai).
     */
    private boolean simpanUpsertAnak(String[] nilai) {
        if (nilai.length != KOLOM_ANAK.length) {
            JOptionPane.showMessageDialog(this, "Kesalahan internal: jumlah kolom (" + KOLOM_ANAK.length
                    + ") != jumlah nilai (" + nilai.length + ").");
            return false;
        }
        StringBuilder cols = new StringBuilder();
        StringBuilder qm = new StringBuilder();
        for (String k : KOLOM_ANAK) {
            if (cols.length() > 0) { cols.append(","); qm.append(","); }
            cols.append(k);
            qm.append("?");
        }
        try (PreparedStatement psAnak = koneksi.prepareStatement(
                "replace into penilaian_awal_keperawatan_ranap_anak (" + cols + ") values (" + qm + ")")) {
            for (int idx = 0; idx < nilai.length; idx++) {
                psAnak.setString(idx + 1, nilai[idx]);
            }
            psAnak.executeUpdate();
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Maaf, gagal menyimpan data. Ada kesalahan Query...!\n" + e.getMessage());
            return false;
        }
    }

    /** Muat data penilaian_awal_keperawatan_ranap_anak yg sudah tersimpan (kalau ada) -- dipanggil dari setNoRm(). */
    private void muatAnak(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select * from penilaian_awal_keperawatan_ranap_anak where no_rawat=?")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getString("tanggal") != null) {
                        try {
                            TglAsuhan.setDate(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("tanggal")));
                        } catch (Exception ignore) { }
                    }
                    pilihAnak(Anamnesis, rs.getString("informasi"));
                    KetAnamnesis.setText(nvAnak(rs.getString("ket_informasi")));
                    pilihAnak(TibadiRuang, rs.getString("tiba_diruang_rawat"));
                    pilihAnak(MacamKasus, rs.getString("kasus_trauma"));
                    pilihAnak(CaraMasuk, rs.getString("cara_masuk"));
                    KelUtama.setText(nvAnak(rs.getString("kel_utama")));
                    RPS.setText(nvAnak(rs.getString("rps")));
                    RPD.setText(nvAnak(rs.getString("rpd")));
                    RPK.setText(nvAnak(rs.getString("rpk")));
                    RPO.setText(nvAnak(rs.getString("rpo")));
                    RPembedahan.setText(nvAnak(rs.getString("riwayat_pembedahan")));
                    RDirawatRS.setText(nvAnak(rs.getString("riwayat_dirawat_dirs")));
                    pilihAnak(AlatBantuDipakai, rs.getString("alat_bantu_dipakai"));
                    RTranfusi.setText(nvAnak(rs.getString("riwayat_tranfusi")));
                    RiImunisasi.setText(nvAnak(rs.getString("riwayat_imunisasi")));
                    pilihAnak(RPerkmAnk, rs.getString("riwayat_prkmbngan"));
                    KetPerkmAnk.setText(nvAnak(rs.getString("riwayat_prkmbngan_lainnya")));
                    Alergi.setText(nvAnak(rs.getString("riwayat_alergi")));
                    KesadaranMental.setText(nvAnak(rs.getString("pemeriksaan_mental")));
                    pilihAnak(KeadaanMentalUmum, rs.getString("pemeriksaan_keadaan_umum"));
                    GCS.setText(nvAnak(rs.getString("pemeriksaan_gcs")));
                    TD.setText(nvAnak(rs.getString("pemeriksaan_td")));
                    Nadi.setText(nvAnak(rs.getString("pemeriksaan_nadi")));
                    RR.setText(nvAnak(rs.getString("pemeriksaan_rr")));
                    Suhu.setText(nvAnak(rs.getString("pemeriksaan_suhu")));
                    SpO2.setText(nvAnak(rs.getString("pemeriksaan_spo2")));
                    BB.setText(nvAnak(rs.getString("pemeriksaan_bb")));
                    TB.setText(nvAnak(rs.getString("pemeriksaan_tb")));
                    pilihAnak(PolaAktifitasMakan, rs.getString("pola_aktifitas_makanminum"));
                    pilihAnak(PolaAktifitasMandi, rs.getString("pola_aktifitas_mandi"));
                    pilihAnak(PolaAktifitasEliminasi, rs.getString("pola_aktifitas_eliminasi"));
                    pilihAnak(PolaAktifitasBerpakaian, rs.getString("pola_aktifitas_berpakaian"));
                    pilihAnak(PolaAktifitasBerpindah, rs.getString("pola_aktifitas_berpindah"));
                    PolaNutrisiFrekuensi.setText(nvAnak(rs.getString("pola_nutrisi_frekuesi_makan")));
                    PolaNutrisiJenis.setText(nvAnak(rs.getString("pola_nutrisi_jenis_makanan")));
                    PolaNutrisiPorsi.setText(nvAnak(rs.getString("pola_nutrisi_porsi_makan")));
                    PolaTidurLama.setText(nvAnak(rs.getString("pola_tidur_lama_tidur")));
                    pilihAnak(PolaTidurGangguan, rs.getString("pola_tidur_gangguan"));
                    pilihAnak(AktifitasSehari2, rs.getString("pengkajian_fungsi_kemampuan_sehari"));
                    pilihAnak(Aktifitas, rs.getString("pengkajian_fungsi_aktifitas"));
                    pilihAnak(Berjalan, rs.getString("pengkajian_fungsi_berjalan"));
                    KeteranganBerjalan.setText(nvAnak(rs.getString("pengkajian_fungsi_berjalan_keterangan")));
                    pilihAnak(AlatAmbulasi, rs.getString("pengkajian_fungsi_ambulasi"));
                    pilihAnak(EkstrimitasAtas, rs.getString("pengkajian_fungsi_ekstrimitas_atas"));
                    KeteranganEkstrimitasAtas.setText(nvAnak(rs.getString("pengkajian_fungsi_ekstrimitas_atas_keterangan")));
                    pilihAnak(EkstrimitasBawah, rs.getString("pengkajian_fungsi_ekstrimitas_bawah"));
                    KeteranganEkstrimitasBawah.setText(nvAnak(rs.getString("pengkajian_fungsi_ekstrimitas_bawah_keterangan")));
                    pilihAnak(KemampuanMenggenggam, rs.getString("pengkajian_fungsi_menggenggam"));
                    KeteranganKemampuanMenggenggam.setText(nvAnak(rs.getString("pengkajian_fungsi_menggenggam_keterangan")));
                    pilihAnak(KemampuanKoordinasi, rs.getString("pengkajian_fungsi_koordinasi"));
                    KeteranganKemampuanKoordinasi.setText(nvAnak(rs.getString("pengkajian_fungsi_koordinasi_keterangan")));
                    pilihAnak(KesimpulanGangguanFungsi, rs.getString("pengkajian_fungsi_kesimpulan"));
                    pilihAnak(KondisiPsikologis, rs.getString("riwayat_psiko_kondisi_psiko"));
                    pilihAnak(GangguanJiwa, rs.getString("riwayat_psiko_gangguan_jiwa"));
                    pilihAnak(AdakahPerilaku, rs.getString("riwayat_psiko_perilaku"));
                    KeteranganAdakahPerilaku.setText(nvAnak(rs.getString("riwayat_psiko_perilaku_keterangan")));
                    pilihAnak(HubunganAnggotaKeluarga, rs.getString("riwayat_psiko_hubungan_keluarga"));
                    pilihAnak(TinggalDengan, rs.getString("riwayat_psiko_tinggal"));
                    KeteranganTinggalDengan.setText(nvAnak(rs.getString("riwayat_psiko_tinggal_keterangan")));
                    pilihAnak(NilaiKepercayaan, rs.getString("riwayat_psiko_nilai_kepercayaan"));
                    KeteranganNilaiKepercayaan.setText(nvAnak(rs.getString("riwayat_psiko_nilai_kepercayaan_keterangan")));
                    pilihAnak(PendidikanPJ, rs.getString("riwayat_psiko_pendidikan_pj"));
                    pilihAnak(EdukasiPsikolgis, rs.getString("riwayat_psiko_edukasi_diberikan"));
                    KeteranganEdukasiPsikologis.setText(nvAnak(rs.getString("riwayat_psiko_edukasi_diberikan_keterangan")));
                    pilihAnak(SkalaWajah, rs.getString("wajah"));
                    NilaiWajah.setText(nvAnak(rs.getString("nilaiwajah")));
                    pilihAnak(SkalaKaki, rs.getString("kaki"));
                    NilaiKaki.setText(nvAnak(rs.getString("nilaikaki")));
                    pilihAnak(SkalaAktifitas, rs.getString("aktifitas"));
                    NilaiAktifitas.setText(nvAnak(rs.getString("nilaiaktifitas")));
                    pilihAnak(SkalaMenangis, rs.getString("menangis"));
                    NilaiMenangis.setText(nvAnak(rs.getString("nilaimenangis")));
                    pilihAnak(SkalaBersuara, rs.getString("bersuara"));
                    NilaiBersuara.setText(nvAnak(rs.getString("nilaibersuara")));
                    SkalaNyeri1.setText(nvAnak(rs.getString("hasilnyeri")));
                    pilihAnak(SkalaResiko7, rs.getString("penilaian_humptydumpty_skala1"));
                    NilaiResiko7.setText(nvAnak(rs.getString("penilaian_humptydumpty_nilai1")));
                    pilihAnak(SkalaResiko8, rs.getString("penilaian_humptydumpty_skala2"));
                    NilaiResiko8.setText(nvAnak(rs.getString("penilaian_humptydumpty_nilai2")));
                    pilihAnak(SkalaResiko9, rs.getString("penilaian_humptydumpty_skala3"));
                    NilaiResiko9.setText(nvAnak(rs.getString("penilaian_humptydumpty_nilai3")));
                    pilihAnak(SkalaResiko10, rs.getString("penilaian_humptydumpty_skala4"));
                    NilaiResiko10.setText(nvAnak(rs.getString("penilaian_humptydumpty_nilai4")));
                    pilihAnak(SkalaResiko11, rs.getString("penilaian_humptydumpty_skala5"));
                    NilaiResiko11.setText(nvAnak(rs.getString("penilaian_humptydumpty_nilai5")));
                    pilihAnak(SkalaResiko12, rs.getString("penilaian_humptydumpty_skala6"));
                    NilaiResiko12.setText(nvAnak(rs.getString("penilaian_humptydumpty_nilai6")));
                    pilihAnak(SkalaResiko13, rs.getString("penilaian_humptydumpty_skala7"));
                    NilaiResiko13.setText(nvAnak(rs.getString("penilaian_humptydumpty_nilai7")));
                    NilaiResikoTotal1.setText(nvAnak(rs.getString("penilaian_humptydumpty_totalnilai")));
                    pilihAnak(SkalaGizi1, rs.getString("skrining_gizi1"));
                    NilaiGizi1.setText(nvAnak(rs.getString("nilai_gizi1")));
                    pilihAnak(SkalaGizi2, rs.getString("skrining_gizi2"));
                    NilaiGizi2.setText(nvAnak(rs.getString("nilai_gizi2")));
                    NilaiGiziTotal.setText(nvAnak(rs.getString("nilai_total_gizi")));
                    pilihAnak(DiagnosaKhususGizi, rs.getString("skrining_gizi_diagnosa_khusus"));
                    KeteranganDiagnosaKhususGizi.setText(nvAnak(rs.getString("skrining_gizi_ket_diagnosa_khusus")));
                    pilihAnak(DiketahuiDietisen, rs.getString("skrining_gizi_diketahui_dietisen"));
                    KeteranganDiketahuiDietisen.setText(nvAnak(rs.getString("skrining_gizi_jam_diketahui_dietisen")));
                    Rencana.setText(nvAnak(rs.getString("rencana")));
                    String nip1 = nvAnak(rs.getString("nip1"));
                    String nip2 = nvAnak(rs.getString("nip2"));
                    String kdDok = nvAnak(rs.getString("kd_dokter"));
                    if (!nip1.equals("")) {
                        KdPetugas.setText(nip1);
                        NmPetugas.setText(Sequel.cariIsi("select nama from petugas where nip=?", nip1));
                    }
                    if (!nip2.equals("")) {
                        KdPetugas2.setText(nip2);
                        NmPetugas2.setText(Sequel.cariIsi("select nama from petugas where nip=?", nip2));
                    }
                    if (!kdDok.equals("")) {
                        KdDPJP.setText(kdDok);
                        NmDPJP.setText(Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", kdDok));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat penilaian awal anak : " + e);
        }
    }

    private static void pilihAnak(widget.ComboBox combo, String nilai) {
        if (nilai == null || nilai.trim().equals("")) { return; }
        for (int idx = 0; idx < combo.getItemCount(); idx++) {
            Object it = combo.getItemAt(idx);
            if (it != null && it.toString().equalsIgnoreCase(nilai.trim())) {
                combo.setSelectedItem(it);
                return;
            }
        }
    }

    private static String nvAnak(String v) {
        return v == null ? "" : v;
    }

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        if(tbObat.getSelectedRow()>-1){
            if(akses.getkode().equals("Admin Utama")){
                hapus();
            }else{
                if(KdPetugas.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(),5).toString())){
                    hapus();
                }else{
                    JOptionPane.showMessageDialog(null,"Hanya bisa dihapus oleh petugas yang bersangkutan..!!");
                }
            }
        }else{
            JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih data terlebih dahulu..!!");
        }   
        
            
}//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnHapusActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnBatal, BtnEdit);
        }
}//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
        if(TNoRM.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Nama Pasien");
        }else if(KdPetugas.getText().trim().equals("")||NmPetugas.getText().trim().equals("")){
            Valid.textKosong(BtnPetugas,"Pengkaji 1");
        }else if(KdPetugas2.getText().trim().equals("")||NmPetugas2.getText().trim().equals("")){
            Valid.textKosong(BtnPetugas2,"Pegkaji 2");
        }else if(KdDPJP.getText().trim().equals("")||NmDPJP.getText().trim().equals("")){
            Valid.textKosong(BtnDPJP,"DPJP");
        }else if(RPS.getText().trim().equals("")){
            Valid.textKosong(RPS,"Riwayat Penyakit Sekarang");
        }else{
            if(tbObat.getSelectedRow()>-1){
                if(akses.getkode().equals("Admin Utama")){
                    ganti();
                }else{
                    if(KdPetugas.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(),5).toString())){
                        ganti();
                    }else{
                        JOptionPane.showMessageDialog(null,"Hanya bisa diganti oleh petugas yang bersangkutan..!!");
                    }
                }
            }else{
                JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih data terlebih dahulu..!!");
            }  
        }
}//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnEditActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnHapus, BtnPrint);
        }
}//GEN-LAST:event_BtnEditKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnKeluarActionPerformed(null);
        }else{Valid.pindah(evt,BtnEdit,TCari);}
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            BtnBatal.requestFocus();
        }else if(tabMode.getRowCount()!=0){
            try{
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
                
                ps=koneksi.prepareStatement(
                    "select penilaian_awal_keperawatan_ranap_anak.no_rawat,penilaian_awal_keperawatan_ranap_anak.tanggal,penilaian_awal_keperawatan_ranap_anak.informasi,penilaian_awal_keperawatan_ranap_anak.ket_informasi,penilaian_awal_keperawatan_ranap_anak.tiba_diruang_rawat,"+
                    "penilaian_awal_keperawatan_ranap_anak.kasus_trauma,penilaian_awal_keperawatan_ranap_anak.cara_masuk,penilaian_awal_keperawatan_ranap_anak.kel_utama,penilaian_awal_keperawatan_ranap_anak.rps,penilaian_awal_keperawatan_ranap_anak.rpd,penilaian_awal_keperawatan_ranap_anak.rpk,penilaian_awal_keperawatan_ranap_anak.rpo,"+
                    "penilaian_awal_keperawatan_ranap_anak.riwayat_pembedahan,penilaian_awal_keperawatan_ranap_anak.riwayat_dirawat_dirs,penilaian_awal_keperawatan_ranap_anak.alat_bantu_dipakai,"+
                    "penilaian_awal_keperawatan_ranap_anak.riwayat_tranfusi,penilaian_awal_keperawatan_ranap_anak.riwayat_imunisasi,penilaian_awal_keperawatan_ranap_anak.riwayat_prkmbngan,penilaian_awal_keperawatan_ranap_anak.riwayat_prkmbngan_lainnya,penilaian_awal_keperawatan_ranap_anak.riwayat_alergi,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_mental,"+
                    "penilaian_awal_keperawatan_ranap_anak.pemeriksaan_keadaan_umum,"+
                    "penilaian_awal_keperawatan_ranap_anak.pemeriksaan_gcs,"+
                    "penilaian_awal_keperawatan_ranap_anak.pemeriksaan_td,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_nadi,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_rr,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_suhu,"+
                    "penilaian_awal_keperawatan_ranap_anak.pemeriksaan_spo2,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_bb,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_tb,"+
                    "penilaian_awal_keperawatan_ranap_anak.pola_aktifitas_makanminum,penilaian_awal_keperawatan_ranap_anak.pola_aktifitas_mandi,penilaian_awal_keperawatan_ranap_anak.pola_aktifitas_eliminasi,penilaian_awal_keperawatan_ranap_anak.pola_aktifitas_berpakaian,"+
                    "penilaian_awal_keperawatan_ranap_anak.pola_aktifitas_berpindah,penilaian_awal_keperawatan_ranap_anak.pola_nutrisi_frekuesi_makan,penilaian_awal_keperawatan_ranap_anak.pola_nutrisi_jenis_makanan,penilaian_awal_keperawatan_ranap_anak.pola_nutrisi_porsi_makan,"+
                    "penilaian_awal_keperawatan_ranap_anak.pola_tidur_lama_tidur,penilaian_awal_keperawatan_ranap_anak.pola_tidur_gangguan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_kemampuan_sehari,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_aktifitas,"+
                    "penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_berjalan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_berjalan_keterangan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_ambulasi,"+
                    "penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_ekstrimitas_atas,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_ekstrimitas_atas_keterangan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_ekstrimitas_bawah,"+
                    "penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_ekstrimitas_bawah_keterangan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_menggenggam,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_menggenggam_keterangan,"+
                    "penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_koordinasi,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_koordinasi_keterangan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_kesimpulan,"+
                    "penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_kondisi_psiko,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_gangguan_jiwa,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_perilaku,"+
                    "penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_perilaku_keterangan,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_hubungan_keluarga,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_tinggal,"+
                    "penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_tinggal_keterangan,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_nilai_kepercayaan,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_nilai_kepercayaan_keterangan,"+
                    "penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_pendidikan_pj,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_edukasi_diberikan,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_edukasi_diberikan_keterangan,"+
                    "penilaian_awal_keperawatan_ranap_anak.wajah,penilaian_awal_keperawatan_ranap_anak.nilaiwajah,penilaian_awal_keperawatan_ranap_anak.kaki,"+
                    "penilaian_awal_keperawatan_ranap_anak.nilaikaki,penilaian_awal_keperawatan_ranap_anak.aktifitas,penilaian_awal_keperawatan_ranap_anak.nilaiaktifitas,penilaian_awal_keperawatan_ranap_anak.menangis,penilaian_awal_keperawatan_ranap_anak.nilaimenangis,"+
                    "penilaian_awal_keperawatan_ranap_anak.bersuara,penilaian_awal_keperawatan_ranap_anak.nilaibersuara,penilaian_awal_keperawatan_ranap_anak.hasilnyeri,"+
                    "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala1,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai1,"+
                    "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala2,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai2,"+
                    "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala3,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai3,"+
                    "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala4,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai4,"+
                    "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala5,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai5,"+
                    "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala6,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai6,"+
                    "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala7,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai7,"+
                    "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_totalnilai,"+
                    "penilaian_awal_keperawatan_ranap_anak.skrining_gizi1,penilaian_awal_keperawatan_ranap_anak.nilai_gizi1,penilaian_awal_keperawatan_ranap_anak.skrining_gizi2,"+
                    "penilaian_awal_keperawatan_ranap_anak.nilai_gizi2,penilaian_awal_keperawatan_ranap_anak.nilai_total_gizi,penilaian_awal_keperawatan_ranap_anak.skrining_gizi_diagnosa_khusus,penilaian_awal_keperawatan_ranap_anak.skrining_gizi_ket_diagnosa_khusus,"+
                    "penilaian_awal_keperawatan_ranap_anak.skrining_gizi_diketahui_dietisen,penilaian_awal_keperawatan_ranap_anak.skrining_gizi_jam_diketahui_dietisen,penilaian_awal_keperawatan_ranap_anak.rencana,penilaian_awal_keperawatan_ranap_anak.nip1,"+
                    "penilaian_awal_keperawatan_ranap_anak.nip2,penilaian_awal_keperawatan_ranap_anak.kd_dokter,pasien.tgl_lahir,pasien.jk,pengkaji1.nama as pengkaji1,pengkaji2.nama as pengkaji2,dokter.nm_dokter,"+
                    "reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.agama,pasien.pekerjaan,pasien.pnd,penjab.png_jawab,bahasa_pasien.nama_bahasa "+
                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "inner join penilaian_awal_keperawatan_ranap_anak on reg_periksa.no_rawat=penilaian_awal_keperawatan_ranap_anak.no_rawat "+
                    "inner join petugas as pengkaji1 on penilaian_awal_keperawatan_ranap_anak.nip1=pengkaji1.nip "+
                    "inner join petugas as pengkaji2 on penilaian_awal_keperawatan_ranap_anak.nip2=pengkaji2.nip "+
                    "inner join dokter on penilaian_awal_keperawatan_ranap_anak.kd_dokter=dokter.kd_dokter "+
                    "inner join bahasa_pasien on bahasa_pasien.id=pasien.bahasa_pasien "+
                    "inner join penjab on penjab.kd_pj=reg_periksa.kd_pj where "+
                    "penilaian_awal_keperawatan_ranap_anak.tanggal between ? and ? "+
                    (TCari.getText().trim().equals("")?"":"and (reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or penilaian_awal_keperawatan_ranap_anak.nip1 like ? or "+
                    "pengkaji1.nama like ? or penilaian_awal_keperawatan_ranap_anak.kd_dokter like ? or dokter.nm_dokter like ?)")+
                    " order by penilaian_awal_keperawatan_ranap_anak.tanggal");

                try {
                    ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                    ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                    if(!TCari.getText().equals("")){
                        ps.setString(3,"%"+TCari.getText()+"%");
                        ps.setString(4,"%"+TCari.getText()+"%");
                        ps.setString(5,"%"+TCari.getText()+"%");
                        ps.setString(6,"%"+TCari.getText()+"%");
                        ps.setString(7,"%"+TCari.getText()+"%");
                        ps.setString(8,"%"+TCari.getText()+"%");
                        ps.setString(9,"%"+TCari.getText()+"%");
                    }   
                    rs=ps.executeQuery();
                    pilihan = (String)JOptionPane.showInputDialog(null,"Silahkan pilih laporan..!","Pilihan Cetak",JOptionPane.QUESTION_MESSAGE,null,new Object[]{"Laporan 1 (HTML)","Laporan 2 (WPS)","Laporan 3 (CSV)"},"Laporan 1 (HTML)");
                    switch (pilihan) {
                        case "Laporan 1 (HTML)":
                                htmlContent = new StringBuilder();
                                htmlContent.append(                             
                                    "<tr class='isi'>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='105px'>No.Rawat</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>No.RM</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Nama Pasien</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Tgl.Lahir</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='25px'>J.K.</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>NIP Pengkaji 1</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Nama Pengkaji 1</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>NIP Pengkaji 2</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Nama Pengkaji 2</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Kode DPJP</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Nama DPJP</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='117px'>Tgl.Asuhan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='78px'>Macam Kasus</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Anamnesis</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='110px'>Tiba Di Ruang Rawat</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Cara Masuk</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='220px'>Riwayat Penyakit Saat Ini</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Riwayat Penyakit Dahulu</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Riwayat Penyakit Keluarga</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Riwayat Penggunaan Obat</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Riwayat Pembedahan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Riwayat Dirawat Di RS</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Alat Bantu Yang Dipakai</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='210px'>Dalam Keadaan Hamil/Sedang Menyusui</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Riwayat Transfusi Darah</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Riwayat Alergi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='48px'>Merokok</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Batang/Hari</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='44px'>Alkohol</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='59px'>Gelas/Hari</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='61px'>Obat Tidur</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='59px'>Olah Raga</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Kesadaran Mental</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Keadaan Umum</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='64px'>GCS(E,V,M)</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='60px'>TD(mmHg)</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='74px'>Nadi(x/menit)</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='67px'>RR(x/menit)</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='52px'>Suhu(°C)</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='52px'>SpO2(%)</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='44px'>BB(Kg)</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='44px'>TB(cm)</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Kepala</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Wajah</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='106px'>Leher</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Kejang</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Sensorik</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='50px'>Pulsasi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Sirkulasi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='72px'>Denyut Nadi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='54px'>Retraksi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='63px'>Pola Nafas</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='69px'>Suara Nafas</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='97px'>Batuk & Sekresi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='75px'>Volume</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Jenis Pernafasaan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Irama</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Mulut</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Lidah</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Gigi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Tenggorokan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Abdomen</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Peistatik Usus</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Anus</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Sensorik</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Penglihatan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Alat Bantu Penglihatan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Motorik</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Pendengaran</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Bicara</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Otot</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Kulit</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Warna Kulit</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Turgor</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Resiko Decubitas</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Oedema</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Pergerakan Sendi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Kekuatan Otot</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Fraktur</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Nyeri Sendi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Frekuensi BAB</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>x/</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Konsistensi BAB</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Warna BAB</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Frekuensi BAK</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>x/</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Warna BAK</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Lain-lain BAK</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Mandi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Makan/Minum</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Berpakaian</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Eliminasi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Berpindah</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Porsi Makan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Frekuensi Makan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Jenis Makanan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Lama Tidur</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Gangguan Tidur</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>a. Aktifitas Sehari-hari</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>b. Berjalan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>c. Aktifitas</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>d. Alat Ambulasi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>e. Ekstremitas Atas</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>f. Ekstremitas Bawah</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>g. Kemampuan Menggenggam</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>h. Kemampuan Koordinasi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>i. Kesimpulan Gangguan Fungsi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>a. Kondisi Psikologis</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>b. Adakah Perilaku</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>c. Gangguan Jiwa di Masa Lalu</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>d. Hubungan Pasien</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>e. Agama</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>f. Tinggal Dengan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>g. Pekerjaan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>h. Pembayaran</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>i. Nilai-nilai Kepercayaan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>j. Bahasa Sehari-hari</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>k. Pendidikan Pasien</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>l. Pendidikan P.J.</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>m. Edukasi Diberikan Kepada</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Nyeri</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Penyebab Nyeri</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Kualitas Nyeri</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Lokasi Nyeri</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Nyeri Menyebar</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Nyeri</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Waktu / Durasi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Nyeri Hilang Bila</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Diberitahukan Pada Dokter</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Morse 1</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.M. 1</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Morse 2</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.M. 2</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Morse 3</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.M. 3</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Morse 4</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.M. 4</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Morse 5</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.M. 5</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Morse 6</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.M. 6</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>T.M.</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 1</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 1</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 2</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 2</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 3</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 3</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 4</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 4</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 5</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 5</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 6</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 6</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 7</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 7</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 8</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 8</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 9</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 9</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 10</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 10</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 11</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 11</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>T.S.</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>1. Apakah ada penurunan BB yang tidak diinginkan selama 6 bulan terakhir ?</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skor 1</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>2. Apakah asupan makan berkurang karena tidak nafsu makan ?</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skor 2</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Total Skor</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Pasien dengan diagnosis khusus</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Keterangan Diagnosa Khusus</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Sudah dibaca dan diketahui oleh Dietisen</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Jam Dibaca Dietisen</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Rencana Keperawatan Lainnya</td>"+
                                    "</tr>"
                                );
                                while(rs.next()){
                                    htmlContent.append(
                                        "<tr class='isi'>"+
                                            "<td valign='top'>"+rs.getString("no_rawat")+"</td>"+
                                            "<td valign='top'>"+rs.getString("no_rkm_medis")+"</td>"+
                                            "<td valign='top'>"+rs.getString("nm_pasien")+"</td>"+
                                            "<td valign='top'>"+rs.getString("tgl_lahir")+"</td>"+
                                            "<td valign='top'>"+rs.getString("jk")+"</td>"+
                                            "<td valign='top'>"+rs.getString("nip1")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkaji1")+"</td>"+
                                            "<td valign='top'>"+rs.getString("nip2")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkaji2")+"</td>"+
                                            "<td valign='top'>"+rs.getString("kd_dokter")+"</td>"+
                                            "<td valign='top'>"+rs.getString("nm_dokter")+"</td>"+
                                            "<td valign='top'>"+rs.getString("tanggal")+"</td>"+
                                            "<td valign='top'>"+rs.getString("kasus_trauma")+"</td>"+
                                            "<td valign='top'>"+rs.getString("informasi")+", "+rs.getString("ket_informasi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("tiba_diruang_rawat")+"</td>"+
                                            "<td valign='top'>"+rs.getString("cara_masuk")+"</td>"+
                                            "<td valign='top'>"+rs.getString("rps")+"</td>"+
                                            "<td valign='top'>"+rs.getString("rpd")+"</td>"+
                                            "<td valign='top'>"+rs.getString("rpk")+"</td>"+
                                            "<td valign='top'>"+rs.getString("rpo")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_pembedahan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_dirawat_dirs")+"</td>"+
                                            "<td valign='top'>"+rs.getString("alat_bantu_dipakai")+"</td>"+
                                            //"<td valign='top'>"+rs.getString("riwayat_kehamilan")+", "+rs.getString("riwayat_kehamilan_perkiraan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_tranfusi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_alergi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_merokok")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_merokok_jumlah")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_alkohol")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_alkohol_jumlah")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_narkoba")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_olahraga")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_mental")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_keadaan_umum")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_gcs")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_td")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_nadi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_rr")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_suhu")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_spo2")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_bb")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_tb")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_susunan_kepala")+", "+rs.getString("pemeriksaan_susunan_kepala_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_susunan_wajah")+", "+rs.getString("pemeriksaan_susunan_wajah_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_susunan_leher")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_susunan_kejang")+", "+rs.getString("pemeriksaan_susunan_kejang_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_susunan_sensorik")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_kardiovaskuler_pulsasi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_kardiovaskuler_sirkulasi")+", "+rs.getString("pemeriksaan_kardiovaskuler_sirkulasi_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_kardiovaskuler_denyut_nadi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_respirasi_retraksi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_respirasi_pola_nafas")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_respirasi_suara_nafas")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_respirasi_batuk")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_respirasi_volume_pernafasan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_respirasi_jenis_pernafasan")+", "+rs.getString("pemeriksaan_respirasi_jenis_pernafasan_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_respirasi_irama_nafas")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_gastrointestinal_mulut")+", "+rs.getString("pemeriksaan_gastrointestinal_mulut_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_gastrointestinal_lidah")+", "+rs.getString("pemeriksaan_gastrointestinal_lidah_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_gastrointestinal_gigi")+", "+rs.getString("pemeriksaan_gastrointestinal_gigi_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_gastrointestinal_tenggorokan")+", "+rs.getString("pemeriksaan_gastrointestinal_tenggorokan_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_gastrointestinal_abdomen")+", "+rs.getString("pemeriksaan_gastrointestinal_abdomen_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_gastrointestinal_peistatik_usus")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_gastrointestinal_anus")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_neurologi_sensorik")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_neurologi_pengelihatan")+", "+rs.getString("pemeriksaan_neurologi_pengelihatan_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_neurologi_alat_bantu_penglihatan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_neurologi_motorik")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_neurologi_pendengaran")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_neurologi_bicara")+", "+rs.getString("pemeriksaan_neurologi_bicara_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_neurologi_kekuatan_otot")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_integument_kulit")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_integument_warnakulit")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_integument_turgor")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_integument_dekubitas")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_muskuloskletal_oedema")+", "+rs.getString("pemeriksaan_muskuloskletal_oedema_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_muskuloskletal_pergerakan_sendi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_muskuloskletal_kekauatan_otot")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_muskuloskletal_fraktur")+", "+rs.getString("pemeriksaan_muskuloskletal_fraktur_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_muskuloskletal_nyeri_sendi")+", "+rs.getString("pemeriksaan_muskuloskletal_nyeri_sendi_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_eliminasi_bab_frekuensi_jumlah")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_eliminasi_bab_frekuensi_durasi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_eliminasi_bab_konsistensi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_eliminasi_bab_warna")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_eliminasi_bak_frekuensi_jumlah")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_eliminasi_bak_frekuensi_durasi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_eliminasi_bak_warna")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_eliminasi_bak_lainlain")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pola_aktifitas_mandi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pola_aktifitas_makanminum")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pola_aktifitas_berpakaian")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pola_aktifitas_eliminasi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pola_aktifitas_berpindah")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pola_nutrisi_porsi_makan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pola_nutrisi_frekuesi_makan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pola_nutrisi_jenis_makanan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pola_tidur_lama_tidur")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pola_tidur_gangguan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkajian_fungsi_kemampuan_sehari")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkajian_fungsi_berjalan")+", "+rs.getString("pengkajian_fungsi_berjalan_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkajian_fungsi_aktifitas")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkajian_fungsi_ambulasi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkajian_fungsi_ekstrimitas_atas")+", "+rs.getString("pengkajian_fungsi_ekstrimitas_atas_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkajian_fungsi_ekstrimitas_bawah")+", "+rs.getString("pengkajian_fungsi_ekstrimitas_bawah_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkajian_fungsi_menggenggam")+", "+rs.getString("pengkajian_fungsi_menggenggam_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkajian_fungsi_koordinasi")+", "+rs.getString("pengkajian_fungsi_koordinasi_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkajian_fungsi_kesimpulan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_psiko_kondisi_psiko")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_psiko_perilaku")+", "+rs.getString("riwayat_psiko_perilaku_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_psiko_gangguan_jiwa")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_psiko_hubungan_keluarga")+"</td>"+
                                            "<td valign='top'>"+rs.getString("agama")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_psiko_tinggal")+", "+rs.getString("riwayat_psiko_tinggal_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pekerjaan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("png_jawab")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_psiko_nilai_kepercayaan")+", "+rs.getString("riwayat_psiko_nilai_kepercayaan_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("nama_bahasa")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pnd")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_psiko_pendidikan_pj")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_psiko_edukasi_diberikan")+", "+rs.getString("riwayat_psiko_edukasi_diberikan_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_nyeri")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_nyeri_penyebab")+", "+rs.getString("penilaian_nyeri_ket_penyebab")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_nyeri_kualitas")+", "+rs.getString("penilaian_nyeri_ket_kualitas")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_nyeri_lokasi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_nyeri_menyebar")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_nyeri_skala")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_nyeri_waktu")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_nyeri_hilang")+", "+rs.getString("penilaian_nyeri_ket_hilang")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_nyeri_diberitahukan_dokter")+", "+rs.getString("penilaian_nyeri_jam_diberitahukan_dokter")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_skala1")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_nilai1")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_skala2")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_nilai2")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_skala3")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_nilai3")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_skala4")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_nilai4")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_skala5")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_nilai5")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_skala6")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_nilai6")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_totalnilai")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala1")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai1")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala2")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai2")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala3")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai3")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala4")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai4")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala5")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai5")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala6")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai6")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala7")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai7")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala8")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai8")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala9")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai9")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala10")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai10")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala11")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai11")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_totalnilai")+"</td>"+
                                            "<td valign='top'>"+rs.getString("skrining_gizi1")+"</td>"+
                                            "<td valign='top'>"+rs.getString("nilai_gizi1")+"</td>"+
                                            "<td valign='top'>"+rs.getString("skrining_gizi2")+"</td>"+
                                            "<td valign='top'>"+rs.getString("nilai_gizi2")+"</td>"+
                                            "<td valign='top'>"+rs.getString("nilai_total_gizi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("skrining_gizi_diagnosa_khusus")+"</td>"+
                                            "<td valign='top'>"+rs.getString("skrining_gizi_ket_diagnosa_khusus")+"</td>"+
                                            "<td valign='top'>"+rs.getString("skrining_gizi_diketahui_dietisen")+"</td>"+
                                            "<td valign='top'>"+rs.getString("skrining_gizi_jam_diketahui_dietisen")+"</td>"+
                                            "<td valign='top'>"+rs.getString("rencana")+"</td>"+
                                        "</tr>"
                                    );
                                }
                                f = new File("RMPenilaianAwalKeperawatanRanap.html");            
                                bw = new BufferedWriter(new FileWriter(f));            
                                bw.write("<html>"+
                                            "<head><link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" /></head>"+
                                            "<body>"+
                                                "<table width='18500px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
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
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>No.RM</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Nama Pasien</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Tgl.Lahir</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='25px'>J.K.</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>NIP Pengkaji 1</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Nama Pengkaji 1</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>NIP Pengkaji 2</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Nama Pengkaji 2</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Kode DPJP</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Nama DPJP</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='117px'>Tgl.Asuhan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='78px'>Macam Kasus</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Anamnesis</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='110px'>Tiba Di Ruang Rawat</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Cara Masuk</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='220px'>Riwayat Penyakit Saat Ini</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Riwayat Penyakit Dahulu</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Riwayat Penyakit Keluarga</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Riwayat Penggunaan Obat</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Riwayat Pembedahan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Riwayat Dirawat Di RS</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Alat Bantu Yang Dipakai</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='210px'>Dalam Keadaan Hamil/Sedang Menyusui</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Riwayat Transfusi Darah</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Riwayat Alergi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='48px'>Merokok</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Batang/Hari</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='44px'>Alkohol</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='59px'>Gelas/Hari</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='61px'>Obat Tidur</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='59px'>Olah Raga</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Kesadaran Mental</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Keadaan Umum</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='64px'>GCS(E,V,M)</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='60px'>TD(mmHg)</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='74px'>Nadi(x/menit)</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='67px'>RR(x/menit)</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='52px'>Suhu(°C)</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='52px'>SpO2(%)</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='44px'>BB(Kg)</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='44px'>TB(cm)</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Kepala</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Wajah</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='106px'>Leher</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Kejang</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Sensorik</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='50px'>Pulsasi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Sirkulasi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='72px'>Denyut Nadi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='54px'>Retraksi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='63px'>Pola Nafas</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='69px'>Suara Nafas</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='97px'>Batuk & Sekresi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center' width='75px'>Volume</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Jenis Pernafasaan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Irama</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Mulut</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Lidah</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Gigi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Tenggorokan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Abdomen</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Peistatik Usus</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Anus</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Sensorik</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Penglihatan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Alat Bantu Penglihatan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Motorik</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Pendengaran</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Bicara</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Otot</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Kulit</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Warna Kulit</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Turgor</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Resiko Decubitas</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Oedema</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Pergerakan Sendi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Kekuatan Otot</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Fraktur</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Nyeri Sendi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Frekuensi BAB</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>x/</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Konsistensi BAB</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Warna BAB</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Frekuensi BAK</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>x/</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Warna BAK</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Lain-lain BAK</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Mandi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Makan/Minum</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Berpakaian</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Eliminasi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Berpindah</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Porsi Makan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Frekuensi Makan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Jenis Makanan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Lama Tidur</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Gangguan Tidur</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>a. Aktifitas Sehari-hari</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>b. Berjalan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>c. Aktifitas</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>d. Alat Ambulasi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>e. Ekstremitas Atas</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>f. Ekstremitas Bawah</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>g. Kemampuan Menggenggam</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>h. Kemampuan Koordinasi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>i. Kesimpulan Gangguan Fungsi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>a. Kondisi Psikologis</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>b. Adakah Perilaku</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>c. Gangguan Jiwa di Masa Lalu</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>d. Hubungan Pasien</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>e. Agama</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>f. Tinggal Dengan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>g. Pekerjaan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>h. Pembayaran</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>i. Nilai-nilai Kepercayaan</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>j. Bahasa Sehari-hari</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>k. Pendidikan Pasien</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>l. Pendidikan P.J.</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>m. Edukasi Diberikan Kepada</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Nyeri</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Penyebab Nyeri</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Kualitas Nyeri</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Lokasi Nyeri</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Nyeri Menyebar</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Nyeri</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Waktu / Durasi</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Nyeri Hilang Bila</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Diberitahukan Pada Dokter</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Morse 1</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.M. 1</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Morse 2</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.M. 2</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Morse 3</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.M. 3</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Morse 4</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.M. 4</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Morse 5</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.M. 5</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Morse 6</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.M. 6</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>T.M.</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 1</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 1</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 2</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 2</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 3</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 3</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 4</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 4</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 5</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 5</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 6</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 6</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 7</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 7</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 8</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 8</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 9</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 9</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 10</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 10</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skala Sydney 11</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>N.S. 11</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>T.S.</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>1. Apakah ada penurunan BB yang tidak diinginkan selama 6 bulan terakhir ?</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skor 1</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>2. Apakah asupan makan berkurang karena tidak nafsu makan ?</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Skor 2</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Total Skor</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Pasien dengan diagnosis khusus</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Keterangan Diagnosa Khusus</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Sudah dibaca dan diketahui oleh Dietisen</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Jam Dibaca Dietisen</td>"+
                                        "<td valign='middle' bgcolor='#FFFAFA' align='center'>Rencana Keperawatan Lainnya</td>"+
                                    "</tr>"
                                );
                                while(rs.next()){
                                    htmlContent.append(
                                        "<tr class='isi'>"+
                                            "<td valign='top'>"+rs.getString("no_rawat")+"</td>"+
                                            "<td valign='top'>"+rs.getString("no_rkm_medis")+"</td>"+
                                            "<td valign='top'>"+rs.getString("nm_pasien")+"</td>"+
                                            "<td valign='top'>"+rs.getString("tgl_lahir")+"</td>"+
                                            "<td valign='top'>"+rs.getString("jk")+"</td>"+
                                            "<td valign='top'>"+rs.getString("nip1")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkaji1")+"</td>"+
                                            "<td valign='top'>"+rs.getString("nip2")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkaji2")+"</td>"+
                                            "<td valign='top'>"+rs.getString("kd_dokter")+"</td>"+
                                            "<td valign='top'>"+rs.getString("nm_dokter")+"</td>"+
                                            "<td valign='top'>"+rs.getString("tanggal")+"</td>"+
                                            "<td valign='top'>"+rs.getString("kasus_trauma")+"</td>"+
                                            "<td valign='top'>"+rs.getString("informasi")+", "+rs.getString("ket_informasi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("tiba_diruang_rawat")+"</td>"+
                                            "<td valign='top'>"+rs.getString("cara_masuk")+"</td>"+
                                            "<td valign='top'>"+rs.getString("rps")+"</td>"+
                                            "<td valign='top'>"+rs.getString("rpd")+"</td>"+
                                            "<td valign='top'>"+rs.getString("rpk")+"</td>"+
                                            "<td valign='top'>"+rs.getString("rpo")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_pembedahan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_dirawat_dirs")+"</td>"+
                                            "<td valign='top'>"+rs.getString("alat_bantu_dipakai")+"</td>"+
                                            //"<td valign='top'>"+rs.getString("riwayat_kehamilan")+", "+rs.getString("riwayat_kehamilan_perkiraan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_tranfusi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_alergi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_merokok")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_merokok_jumlah")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_alkohol")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_alkohol_jumlah")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_narkoba")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_olahraga")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_mental")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_keadaan_umum")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_gcs")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_td")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_nadi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_rr")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_suhu")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_spo2")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_bb")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_tb")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_susunan_kepala")+", "+rs.getString("pemeriksaan_susunan_kepala_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_susunan_wajah")+", "+rs.getString("pemeriksaan_susunan_wajah_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_susunan_leher")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_susunan_kejang")+", "+rs.getString("pemeriksaan_susunan_kejang_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_susunan_sensorik")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_kardiovaskuler_pulsasi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_kardiovaskuler_sirkulasi")+", "+rs.getString("pemeriksaan_kardiovaskuler_sirkulasi_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_kardiovaskuler_denyut_nadi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_respirasi_retraksi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_respirasi_pola_nafas")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_respirasi_suara_nafas")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_respirasi_batuk")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_respirasi_volume_pernafasan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_respirasi_jenis_pernafasan")+", "+rs.getString("pemeriksaan_respirasi_jenis_pernafasan_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_respirasi_irama_nafas")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_gastrointestinal_mulut")+", "+rs.getString("pemeriksaan_gastrointestinal_mulut_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_gastrointestinal_lidah")+", "+rs.getString("pemeriksaan_gastrointestinal_lidah_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_gastrointestinal_gigi")+", "+rs.getString("pemeriksaan_gastrointestinal_gigi_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_gastrointestinal_tenggorokan")+", "+rs.getString("pemeriksaan_gastrointestinal_tenggorokan_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_gastrointestinal_abdomen")+", "+rs.getString("pemeriksaan_gastrointestinal_abdomen_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_gastrointestinal_peistatik_usus")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_gastrointestinal_anus")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_neurologi_sensorik")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_neurologi_pengelihatan")+", "+rs.getString("pemeriksaan_neurologi_pengelihatan_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_neurologi_alat_bantu_penglihatan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_neurologi_motorik")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_neurologi_pendengaran")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_neurologi_bicara")+", "+rs.getString("pemeriksaan_neurologi_bicara_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_neurologi_kekuatan_otot")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_integument_kulit")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_integument_warnakulit")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_integument_turgor")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_integument_dekubitas")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_muskuloskletal_oedema")+", "+rs.getString("pemeriksaan_muskuloskletal_oedema_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_muskuloskletal_pergerakan_sendi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_muskuloskletal_kekauatan_otot")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_muskuloskletal_fraktur")+", "+rs.getString("pemeriksaan_muskuloskletal_fraktur_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_muskuloskletal_nyeri_sendi")+", "+rs.getString("pemeriksaan_muskuloskletal_nyeri_sendi_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_eliminasi_bab_frekuensi_jumlah")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_eliminasi_bab_frekuensi_durasi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_eliminasi_bab_konsistensi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_eliminasi_bab_warna")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_eliminasi_bak_frekuensi_jumlah")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_eliminasi_bak_frekuensi_durasi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_eliminasi_bak_warna")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pemeriksaan_eliminasi_bak_lainlain")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pola_aktifitas_mandi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pola_aktifitas_makanminum")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pola_aktifitas_berpakaian")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pola_aktifitas_eliminasi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pola_aktifitas_berpindah")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pola_nutrisi_porsi_makan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pola_nutrisi_frekuesi_makan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pola_nutrisi_jenis_makanan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pola_tidur_lama_tidur")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pola_tidur_gangguan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkajian_fungsi_kemampuan_sehari")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkajian_fungsi_berjalan")+", "+rs.getString("pengkajian_fungsi_berjalan_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkajian_fungsi_aktifitas")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkajian_fungsi_ambulasi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkajian_fungsi_ekstrimitas_atas")+", "+rs.getString("pengkajian_fungsi_ekstrimitas_atas_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkajian_fungsi_ekstrimitas_bawah")+", "+rs.getString("pengkajian_fungsi_ekstrimitas_bawah_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkajian_fungsi_menggenggam")+", "+rs.getString("pengkajian_fungsi_menggenggam_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkajian_fungsi_koordinasi")+", "+rs.getString("pengkajian_fungsi_koordinasi_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pengkajian_fungsi_kesimpulan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_psiko_kondisi_psiko")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_psiko_perilaku")+", "+rs.getString("riwayat_psiko_perilaku_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_psiko_gangguan_jiwa")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_psiko_hubungan_keluarga")+"</td>"+
                                            "<td valign='top'>"+rs.getString("agama")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_psiko_tinggal")+", "+rs.getString("riwayat_psiko_tinggal_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pekerjaan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("png_jawab")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_psiko_nilai_kepercayaan")+", "+rs.getString("riwayat_psiko_nilai_kepercayaan_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("nama_bahasa")+"</td>"+
                                            "<td valign='top'>"+rs.getString("pnd")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_psiko_pendidikan_pj")+"</td>"+
                                            "<td valign='top'>"+rs.getString("riwayat_psiko_edukasi_diberikan")+", "+rs.getString("riwayat_psiko_edukasi_diberikan_keterangan")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_nyeri")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_nyeri_penyebab")+", "+rs.getString("penilaian_nyeri_ket_penyebab")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_nyeri_kualitas")+", "+rs.getString("penilaian_nyeri_ket_kualitas")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_nyeri_lokasi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_nyeri_menyebar")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_nyeri_skala")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_nyeri_waktu")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_nyeri_hilang")+", "+rs.getString("penilaian_nyeri_ket_hilang")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_nyeri_diberitahukan_dokter")+", "+rs.getString("penilaian_nyeri_jam_diberitahukan_dokter")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_skala1")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_nilai1")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_skala2")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_nilai2")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_skala3")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_nilai3")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_skala4")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_nilai4")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_skala5")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_nilai5")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_skala6")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_nilai6")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhmorse_totalnilai")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala1")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai1")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala2")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai2")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala3")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai3")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala4")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai4")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala5")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai5")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala6")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai6")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala7")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai7")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala8")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai8")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala9")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai9")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala10")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai10")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_skala11")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_nilai11")+"</td>"+
                                            "<td valign='top'>"+rs.getString("penilaian_jatuhsydney_totalnilai")+"</td>"+
                                            "<td valign='top'>"+rs.getString("skrining_gizi1")+"</td>"+
                                            "<td valign='top'>"+rs.getString("nilai_gizi1")+"</td>"+
                                            "<td valign='top'>"+rs.getString("skrining_gizi2")+"</td>"+
                                            "<td valign='top'>"+rs.getString("nilai_gizi2")+"</td>"+
                                            "<td valign='top'>"+rs.getString("nilai_total_gizi")+"</td>"+
                                            "<td valign='top'>"+rs.getString("skrining_gizi_diagnosa_khusus")+"</td>"+
                                            "<td valign='top'>"+rs.getString("skrining_gizi_ket_diagnosa_khusus")+"</td>"+
                                            "<td valign='top'>"+rs.getString("skrining_gizi_diketahui_dietisen")+"</td>"+
                                            "<td valign='top'>"+rs.getString("skrining_gizi_jam_diketahui_dietisen")+"</td>"+
                                            "<td valign='top'>"+rs.getString("rencana")+"</td>"+
                                        "</tr>"
                                    );
                                }
                                f = new File("RMPenilaianAwalKeperawatanRanap.wps");            
                                bw = new BufferedWriter(new FileWriter(f));            
                                bw.write("<html>"+
                                            "<head><link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" /></head>"+
                                            "<body>"+
                                                "<table width='18500px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
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
                                    "\"No.Rawat\";\"No.RM\";\"Nama Pasien\";\"Tgl.Lahir\";\"J.K.\";\"NIP Pengkaji 1\";\"Nama Pengkaji 1\";\"NIP Pengkaji 2\";\"Nama Pengkaji 2\";\"Kode DPJP\";\"Nama DPJP\";\"Tgl.Asuhan\";\"Macam Kasus\";\"Anamnesis\";\"Tiba Di Ruang Rawat\";\"Cara Masuk\";\"Riwayat Penyakit Saat Ini\";\"Riwayat Penyakit Dahulu\";\"Riwayat Penyakit Keluarga\";\"Riwayat Penggunaan Obat\";\"Riwayat Pembedahan\";\"Riwayat Dirawat Di RS\";\"Alat Bantu Yang Dipakai\";\"Dalam Keadaan Hamil/Sedang Menyusui\";\"Riwayat Transfusi Darah\";\"Riwayat Alergi\";\"Merokok\";\"Batang/Hari\";\"Alkohol\";\"Gelas/Hari\";\"Obat Tidur\";\"Olah Raga\";\"Kesadaran Mental\";\"Keadaan Umum\";\"GCS(E,V,M)\";\"TD(mmHg)\";\"Nadi(x/menit)\";\"RR(x/menit)\";\"Suhu(°C)\";\"SpO2(%)\";\"BB(Kg)\";\"TB(cm)\";\"Kepala\";\"Wajah\";\"Leher\";\"Kejang\";\"Sensorik\";\"Pulsasi\";\"Sirkulasi\";\"Denyut Nadi\";\"Retraksi\";\"Pola Nafas\";\"Suara Nafas\";\"Batuk & Sekresi\";\"Volume\";\"Jenis Pernafasaan\";\"Irama\";\"Mulut\";\"Lidah\";\"Gigi\";\"Tenggorokan\";\"Abdomen\";\"Peistatik Usus\";\"Anus\";\"Sensorik\";\"Penglihatan\";\"Alat Bantu Penglihatan\";\"Motorik\";\"Pendengaran\";\"Bicara\";\"Otot\";\"Kulit\";\"Warna Kulit\";\"Turgor\";\"Resiko Decubitas\";\"Oedema\";\"Pergerakan Sendi\";\"Kekuatan Otot\";\"Fraktur\";\"Nyeri Sendi\";\"Frekuensi BAB\";\"x/\";\"Konsistensi BAB\";\"Warna BAB\";\"Frekuensi BAK\";\"x/\";\"Warna BAK\";\"Lain-lain BAK\";\"Mandi\";\"Makan/Minum\";\"Berpakaian\";\"Eliminasi\";\"Berpindah\";\"Porsi Makan\";\"Frekuensi Makan\";\"Jenis Makanan\";\"Lama Tidur\";\"Gangguan Tidur\";\"a. Aktifitas Sehari-hari\";\"b. Berjalan\";\"c. Aktifitas\";\"d. Alat Ambulasi\";\"e. Ekstremitas Atas\";\"f. Ekstremitas Bawah\";\"g. Kemampuan Menggenggam\";\"h. Kemampuan Koordinasi\";\"i. Kesimpulan Gangguan Fungsi\";\"a. Kondisi Psikologis\";\"b. Adakah Perilaku\";\"c. Gangguan Jiwa di Masa Lalu\";\"d. Hubungan Pasien\";\"e. Agama\";\"f. Tinggal Dengan\";\"g. Pekerjaan\";\"h. Pembayaran\";\"i. Nilai-nilai Kepercayaan\";\"j. Bahasa Sehari-hari\";\"k. Pendidikan Pasien\";\"l. Pendidikan P.J.\";\"m. Edukasi Diberikan Kepada\";\"Nyeri\";\"Penyebab Nyeri\";\"Kualitas Nyeri\";\"Lokasi Nyeri\";\"Nyeri Menyebar\";\"Skala Nyeri\";\"Waktu / Durasi\";\"Nyeri Hilang Bila\";\"Diberitahukan Pada Dokter\";\"Skala Morse 1\";\"N.M. 1\";\"Skala Morse 2\";\"N.M. 2\";\"Skala Morse 3\";\"N.M. 3\";\"Skala Morse 4\";\"N.M. 4\";\"Skala Morse 5\";\"N.M. 5\";\"Skala Morse 6\";\"N.M. 6\";\"T.M.\";\"Skala Sydney 1\";\"N.S. 1\";\"Skala Sydney 2\";\"N.S. 2\";\"Skala Sydney 3\";\"N.S. 3\";\"Skala Sydney 4\";\"N.S. 4\";\"Skala Sydney 5\";\"N.S. 5\";\"Skala Sydney 6\";\"N.S. 6\";\"Skala Sydney 7\";\"N.S. 7\";\"Skala Sydney 8\";\"N.S. 8\";\"Skala Sydney 9\";\"N.S. 9\";\"Skala Sydney 10\";\"N.S. 10\";\"Skala Sydney 11\";\"N.S. 11\";\"T.S.\";\"1. Apakah ada penurunan BB yang tidak diinginkan selama 6 bulan terakhir ?\";\"Skor 1\";\"2. Apakah asupan makan berkurang karena tidak nafsu makan ?\";\"Skor 2\";\"Total Skor\";\"Pasien dengan diagnosis khusus\";\"Keterangan Diagnosa Khusus\";\"Sudah dibaca dan diketahui oleh Dietisen\";\"Jam Dibaca Dietisen\";\"Rencana Keperawatan Lainnya\"\n"
                                ); 
                                while(rs.next()){
                                   // htmlContent.append(
                                       // "\""+rs.getString("no_rawat")+"\";\""+rs.getString("no_rkm_medis")+"\";\""+rs.getString("nm_pasien")+"\";\""+rs.getString("tgl_lahir")+"\";\""+rs.getString("jk")+"\";\""+rs.getString("nip1")+"\";\""+rs.getString("pengkaji1")+"\";\""+rs.getString("nip2")+"\";\""+rs.getString("pengkaji2")+"\";\""+rs.getString("kd_dokter")+"\";\""+rs.getString("nm_dokter")+"\";\""+rs.getString("tanggal")+"\";\""+rs.getString("kasus_trauma")+"\";\""+rs.getString("informasi")+", "+rs.getString("ket_informasi")+"\";\""+rs.getString("tiba_diruang_rawat")+"\";\""+rs.getString("cara_masuk")+"\";\""+rs.getString("rps")+"\";\""+rs.getString("rpd")+"\";\""+rs.getString("rpk")+"\";\""+rs.getString("rpo")+"\";\""+rs.getString("riwayat_pembedahan")+"\";\""+rs.getString("riwayat_dirawat_dirs")+"\";\""+rs.getString("alat_bantu_dipakai")+"\";\""+rs.getString("riwayat_kehamilan")+", "+rs.getString("riwayat_kehamilan_perkiraan")+"\";\""+rs.getString("riwayat_tranfusi")+"\";\""+rs.getString("riwayat_alergi")+"\";\""+rs.getString("riwayat_merokok")+"\";\""+rs.getString("riwayat_merokok_jumlah")+"\";\""+rs.getString("riwayat_alkohol")+"\";\""+rs.getString("riwayat_alkohol_jumlah")+"\";\""+rs.getString("riwayat_narkoba")+"\";\""+rs.getString("riwayat_olahraga")+"\";\""+rs.getString("pemeriksaan_mental")+"\";\""+rs.getString("pemeriksaan_keadaan_umum")+"\";\""+rs.getString("pemeriksaan_gcs")+"\";\""+rs.getString("pemeriksaan_td")+"\";\""+rs.getString("pemeriksaan_nadi")+"\";\""+rs.getString("pemeriksaan_rr")+"\";\""+rs.getString("pemeriksaan_suhu")+"\";\""+rs.getString("pemeriksaan_spo2")+"\";\""+rs.getString("pemeriksaan_bb")+"\";\""+rs.getString("pemeriksaan_tb")+"\";\""+rs.getString("pemeriksaan_susunan_kepala")+", "+rs.getString("pemeriksaan_susunan_kepala_keterangan")+"\";\""+rs.getString("pemeriksaan_susunan_wajah")+", "+rs.getString("pemeriksaan_susunan_wajah_keterangan")+"\";\""+rs.getString("pemeriksaan_susunan_leher")+"\";\""+rs.getString("pemeriksaan_susunan_kejang")+", "+rs.getString("pemeriksaan_susunan_kejang_keterangan")+"\";\""+rs.getString("pemeriksaan_susunan_sensorik")+"\";\""+rs.getString("pemeriksaan_kardiovaskuler_pulsasi")+"\";\""+rs.getString("pemeriksaan_kardiovaskuler_sirkulasi")+", "+rs.getString("pemeriksaan_kardiovaskuler_sirkulasi_keterangan")+"\";\""+rs.getString("pemeriksaan_kardiovaskuler_denyut_nadi")+"\";\""+rs.getString("pemeriksaan_respirasi_retraksi")+"\";\""+rs.getString("pemeriksaan_respirasi_pola_nafas")+"\";\""+rs.getString("pemeriksaan_respirasi_suara_nafas")+"\";\""+rs.getString("pemeriksaan_respirasi_batuk")+"\";\""+rs.getString("pemeriksaan_respirasi_volume_pernafasan")+"\";\""+rs.getString("pemeriksaan_respirasi_jenis_pernafasan")+", "+rs.getString("pemeriksaan_respirasi_jenis_pernafasan_keterangan")+"\";\""+rs.getString("pemeriksaan_respirasi_irama_nafas")+"\";\""+rs.getString("pemeriksaan_gastrointestinal_mulut")+", "+rs.getString("pemeriksaan_gastrointestinal_mulut_keterangan")+"\";\""+rs.getString("pemeriksaan_gastrointestinal_lidah")+", "+rs.getString("pemeriksaan_gastrointestinal_lidah_keterangan")+"\";\""+rs.getString("pemeriksaan_gastrointestinal_gigi")+", "+rs.getString("pemeriksaan_gastrointestinal_gigi_keterangan")+"\";\""+rs.getString("pemeriksaan_gastrointestinal_tenggorokan")+", "+rs.getString("pemeriksaan_gastrointestinal_tenggorokan_keterangan")+"\";\""+rs.getString("pemeriksaan_gastrointestinal_abdomen")+", "+rs.getString("pemeriksaan_gastrointestinal_abdomen_keterangan")+"\";\""+rs.getString("pemeriksaan_gastrointestinal_peistatik_usus")+"\";\""+rs.getString("pemeriksaan_gastrointestinal_anus")+"\";\""+rs.getString("pemeriksaan_neurologi_sensorik")+"\";\""+rs.getString("pemeriksaan_neurologi_pengelihatan")+", "+rs.getString("pemeriksaan_neurologi_pengelihatan_keterangan")+"\";\""+rs.getString("pemeriksaan_neurologi_alat_bantu_penglihatan")+"\";\""+rs.getString("pemeriksaan_neurologi_motorik")+"\";\""+rs.getString("pemeriksaan_neurologi_pendengaran")+"\";\""+rs.getString("pemeriksaan_neurologi_bicara")+", "+rs.getString("pemeriksaan_neurologi_bicara_keterangan")+"\";\""+rs.getString("pemeriksaan_neurologi_kekuatan_otot")+"\";\""+rs.getString("pemeriksaan_integument_kulit")+"\";\""+rs.getString("pemeriksaan_integument_warnakulit")+"\";\""+rs.getString("pemeriksaan_integument_turgor")+"\";\""+rs.getString("pemeriksaan_integument_dekubitas")+"\";\""+rs.getString("pemeriksaan_muskuloskletal_oedema")+", "+rs.getString("pemeriksaan_muskuloskletal_oedema_keterangan")+"\";\""+rs.getString("pemeriksaan_muskuloskletal_pergerakan_sendi")+"\";\""+rs.getString("pemeriksaan_muskuloskletal_kekauatan_otot")+"\";\""+rs.getString("pemeriksaan_muskuloskletal_fraktur")+", "+rs.getString("pemeriksaan_muskuloskletal_fraktur_keterangan")+"\";\""+rs.getString("pemeriksaan_muskuloskletal_nyeri_sendi")+", "+rs.getString("pemeriksaan_muskuloskletal_nyeri_sendi_keterangan")+"\";\""+rs.getString("pemeriksaan_eliminasi_bab_frekuensi_jumlah")+"\";\""+rs.getString("pemeriksaan_eliminasi_bab_frekuensi_durasi")+"\";\""+rs.getString("pemeriksaan_eliminasi_bab_konsistensi")+"\";\""+rs.getString("pemeriksaan_eliminasi_bab_warna")+"\";\""+rs.getString("pemeriksaan_eliminasi_bak_frekuensi_jumlah")+"\";\""+rs.getString("pemeriksaan_eliminasi_bak_frekuensi_durasi")+"\";\""+rs.getString("pemeriksaan_eliminasi_bak_warna")+"\";\""+rs.getString("pemeriksaan_eliminasi_bak_lainlain")+"\";\""+rs.getString("pola_aktifitas_mandi")+"\";\""+rs.getString("pola_aktifitas_makanminum")+"\";\""+rs.getString("pola_aktifitas_berpakaian")+"\";\""+rs.getString("pola_aktifitas_eliminasi")+"\";\""+rs.getString("pola_aktifitas_berpindah")+"\";\""+rs.getString("pola_nutrisi_porsi_makan")+"\";\""+rs.getString("pola_nutrisi_frekuesi_makan")+"\";\""+rs.getString("pola_nutrisi_jenis_makanan")+"\";\""+rs.getString("pola_tidur_lama_tidur")+"\";\""+rs.getString("pola_tidur_gangguan")+"\";\""+rs.getString("pengkajian_fungsi_kemampuan_sehari")+"\";\""+rs.getString("pengkajian_fungsi_berjalan")+", "+rs.getString("pengkajian_fungsi_berjalan_keterangan")+"\";\""+rs.getString("pengkajian_fungsi_aktifitas")+"\";\""+rs.getString("pengkajian_fungsi_ambulasi")+"\";\""+rs.getString("pengkajian_fungsi_ekstrimitas_atas")+", "+rs.getString("pengkajian_fungsi_ekstrimitas_atas_keterangan")+"\";\""+rs.getString("pengkajian_fungsi_ekstrimitas_bawah")+", "+rs.getString("pengkajian_fungsi_ekstrimitas_bawah_keterangan")+"\";\""+rs.getString("pengkajian_fungsi_menggenggam")+", "+rs.getString("pengkajian_fungsi_menggenggam_keterangan")+"\";\""+rs.getString("pengkajian_fungsi_koordinasi")+", "+rs.getString("pengkajian_fungsi_koordinasi_keterangan")+"\";\""+rs.getString("pengkajian_fungsi_kesimpulan")+"\";\""+rs.getString("riwayat_psiko_kondisi_psiko")+"\";\""+rs.getString("riwayat_psiko_perilaku")+", "+rs.getString("riwayat_psiko_perilaku_keterangan")+"\";\""+rs.getString("riwayat_psiko_gangguan_jiwa")+"\";\""+rs.getString("riwayat_psiko_hubungan_keluarga")+"\";\""+rs.getString("agama")+"\";\""+rs.getString("riwayat_psiko_tinggal")+", "+rs.getString("riwayat_psiko_tinggal_keterangan")+"\";\""+rs.getString("pekerjaan")+"\";\""+rs.getString("png_jawab")+"\";\""+rs.getString("riwayat_psiko_nilai_kepercayaan")+", "+rs.getString("riwayat_psiko_nilai_kepercayaan_keterangan")+"\";\""+rs.getString("nama_bahasa")+"\";\""+rs.getString("pnd")+"\";\""+rs.getString("riwayat_psiko_pendidikan_pj")+"\";\""+rs.getString("riwayat_psiko_edukasi_diberikan")+", "+rs.getString("riwayat_psiko_edukasi_diberikan_keterangan")+"\";\""+rs.getString("penilaian_nyeri")+"\";\""+rs.getString("penilaian_nyeri_penyebab")+", "+rs.getString("penilaian_nyeri_ket_penyebab")+"\";\""+rs.getString("penilaian_nyeri_kualitas")+", "+rs.getString("penilaian_nyeri_ket_kualitas")+"\";\""+rs.getString("penilaian_nyeri_lokasi")+"\";\""+rs.getString("penilaian_nyeri_menyebar")+"\";\""+rs.getString("penilaian_nyeri_skala")+"\";\""+rs.getString("penilaian_nyeri_waktu")+"\";\""+rs.getString("penilaian_nyeri_hilang")+", "+rs.getString("penilaian_nyeri_ket_hilang")+"\";\""+rs.getString("penilaian_nyeri_diberitahukan_dokter")+", "+rs.getString("penilaian_nyeri_jam_diberitahukan_dokter")+"\";\""+rs.getString("penilaian_jatuhmorse_skala1")+"\";\""+rs.getString("penilaian_jatuhmorse_nilai1")+"\";\""+rs.getString("penilaian_jatuhmorse_skala2")+"\";\""+rs.getString("penilaian_jatuhmorse_nilai2")+"\";\""+rs.getString("penilaian_jatuhmorse_skala3")+"\";\""+rs.getString("penilaian_jatuhmorse_nilai3")+"\";\""+rs.getString("penilaian_jatuhmorse_skala4")+"\";\""+rs.getString("penilaian_jatuhmorse_nilai4")+"\";\""+rs.getString("penilaian_jatuhmorse_skala5")+"\";\""+rs.getString("penilaian_jatuhmorse_nilai5")+"\";\""+rs.getString("penilaian_jatuhmorse_skala6")+"\";\""+rs.getString("penilaian_jatuhmorse_nilai6")+"\";\""+rs.getString("penilaian_jatuhmorse_totalnilai")+"\";\""+rs.getString("penilaian_jatuhsydney_skala1")+"\";\""+rs.getString("penilaian_jatuhsydney_nilai1")+"\";\""+rs.getString("penilaian_jatuhsydney_skala2")+"\";\""+rs.getString("penilaian_jatuhsydney_nilai2")+"\";\""+rs.getString("penilaian_jatuhsydney_skala3")+"\";\""+rs.getString("penilaian_jatuhsydney_nilai3")+"\";\""+rs.getString("penilaian_jatuhsydney_skala4")+"\";\""+rs.getString("penilaian_jatuhsydney_nilai4")+"\";\""+rs.getString("penilaian_jatuhsydney_skala5")+"\";\""+rs.getString("penilaian_jatuhsydney_nilai5")+"\";\""+rs.getString("penilaian_jatuhsydney_skala6")+"\";\""+rs.getString("penilaian_jatuhsydney_nilai6")+"\";\""+rs.getString("penilaian_jatuhsydney_skala7")+"\";\""+rs.getString("penilaian_jatuhsydney_nilai7")+"\";\""+rs.getString("penilaian_jatuhsydney_skala8")+"\";\""+rs.getString("penilaian_jatuhsydney_nilai8")+"\";\""+rs.getString("penilaian_jatuhsydney_skala9")+"\";\""+rs.getString("penilaian_jatuhsydney_nilai9")+"\";\""+rs.getString("penilaian_jatuhsydney_skala10")+"\";\""+rs.getString("penilaian_jatuhsydney_nilai10")+"\";\""+rs.getString("penilaian_jatuhsydney_skala11")+"\";\""+rs.getString("penilaian_jatuhsydney_nilai11")+"\";\""+rs.getString("penilaian_jatuhsydney_totalnilai")+"\";\""+rs.getString("skrining_gizi1")+"\";\""+rs.getString("nilai_gizi1")+"\";\""+rs.getString("skrining_gizi2")+"\";\""+rs.getString("nilai_gizi2")+"\";\""+rs.getString("nilai_total_gizi")+"\";\""+rs.getString("skrining_gizi_diagnosa_khusus")+"\";\""+rs.getString("skrining_gizi_ket_diagnosa_khusus")+"\";\""+rs.getString("skrining_gizi_diketahui_dietisen")+"\";\""+rs.getString("skrining_gizi_jam_diketahui_dietisen")+"\";\""+rs.getString("rencana")+"\"\n"
                                   // );
                                }
                                f = new File("RMPenilaianAwalKeperawatanRanap.csv");            
                                bw = new BufferedWriter(new FileWriter(f));            
                                bw.write(htmlContent.toString());

                                bw.close();                         
                                Desktop.getDesktop().browse(f.toURI());
                            break; 
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
        }
        this.setCursor(Cursor.getDefaultCursor());
}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnEdit, BtnKeluar);
        }
}//GEN-LAST:event_BtnPrintKeyPressed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }
}//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        tampil();
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
        tampil();
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            TCari.setText("");
            tampil();
        }else{
            Valid.pindah(evt, BtnCari, TPasien);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                ChkAccor.setSelected(true);
                isMenu();
                getMasalah();
            } catch (java.lang.NullPointerException e) {
            }
            if((evt.getClickCount()==2)&&(tbObat.getSelectedColumn()==0)){
                TabRawat.setSelectedIndex(0);
            }
        }
}//GEN-LAST:event_tbObatMouseClicked

    private void tbObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyPressed
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    ChkAccor.setSelected(true);
                    isMenu();
                    getMasalah();
                } catch (java.lang.NullPointerException e) {
                }
            }else if(evt.getKeyCode()==KeyEvent.VK_SPACE){
                try {
                    getData();
                    TabRawat.setSelectedIndex(0);
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
}//GEN-LAST:event_tbObatKeyPressed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        if(TabRawat.getSelectedIndex()==1){
            tampil();
        }
    }//GEN-LAST:event_TabRawatMouseClicked

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        try {
            if(Valid.daysOld("./cache/masalahkeperawatan.iyem")<30){
                tampilMasalah2();
            }else{
                tampilMasalah();
            }
        } catch (Exception e) {
        }
    }//GEN-LAST:event_formWindowOpened

    private void ChkAccorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkAccorActionPerformed
        if(tbObat.getSelectedRow()!= -1){
            isMenu();
        }else{
            ChkAccor.setSelected(false);
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data yang mau ditampilkan...!!!!");
        }
    }//GEN-LAST:event_ChkAccorActionPerformed

    private void BtnPrint1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrint1ActionPerformed
        if(tbObat.getSelectedRow()>-1){
            Map<String, Object> param = new HashMap<>();    
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());          
            param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
            param.put("nyeri",Sequel.cariGambar("select gambar.nyeri from gambar")); 
            finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",tbObat.getValueAt(tbObat.getSelectedRow(),5).toString());
            param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+tbObat.getValueAt(tbObat.getSelectedRow(),6).toString()+"\nID "+(finger.equals("")?tbObat.getValueAt(tbObat.getSelectedRow(),5).toString():finger)+"\n"+Valid.SetTgl3(tbObat.getValueAt(tbObat.getSelectedRow(),11).toString())); 
            
            try {
                masalahkeperawatan="";
                ps=koneksi.prepareStatement(
                    "select master_masalah_keperawatan.kode_masalah,master_masalah_keperawatan.nama_masalah from master_masalah_keperawatan "+
                    "inner join penilaian_awal_keperawatan_ranap_masalah on penilaian_awal_keperawatan_ranap_masalah.kode_masalah=master_masalah_keperawatan.kode_masalah "+
                    "where penilaian_awal_keperawatan_ranap_masalah.no_rawat=? order by penilaian_awal_keperawatan_ranap_masalah.kode_masalah");
                try {
                    ps.setString(1,tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                    rs=ps.executeQuery();
                    while(rs.next()){
                        masalahkeperawatan=rs.getString("nama_masalah")+", "+masalahkeperawatan;
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
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            }
            param.put("masalah",masalahkeperawatan);  
            try {
                masalahkeperawatan="";
                ps=koneksi.prepareStatement(
                    "select master_rencana_keperawatan.kode_rencana,master_rencana_keperawatan.rencana_keperawatan from master_rencana_keperawatan "+
                    "inner join penilaian_awal_keperawatan_ranap_rencana on penilaian_awal_keperawatan_ranap_rencana.kode_rencana=master_rencana_keperawatan.kode_rencana "+
                    "where penilaian_awal_keperawatan_ranap_rencana.no_rawat=? order by penilaian_awal_keperawatan_ranap_rencana.kode_rencana");
                try {
                    ps.setString(1,tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                    rs=ps.executeQuery();
                    while(rs.next()){
                        masalahkeperawatan=rs.getString("rencana_keperawatan")+", "+masalahkeperawatan;
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
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            }
            param.put("rencana",masalahkeperawatan); 
            Valid.MyReportqry("rptCetakPenilaianAwalKeperawatanRanap.jasper","report","::[ Laporan Penilaian Awal Keperawatan Rawat Inap ]::",
                "select penilaian_awal_keperawatan_ranap_anak.no_rawat,penilaian_awal_keperawatan_ranap_anak.tanggal,penilaian_awal_keperawatan_ranap_anak.informasi,penilaian_awal_keperawatan_ranap_anak.ket_informasi,penilaian_awal_keperawatan_ranap_anak.tiba_diruang_rawat,"+
                "penilaian_awal_keperawatan_ranap_anak.kasus_trauma,penilaian_awal_keperawatan_ranap_anak.cara_masuk,penilaian_awal_keperawatan_ranap_anak.kel_utama,penilaian_awal_keperawatan_ranap_anak.rps,penilaian_awal_keperawatan_ranap_anak.rpd,penilaian_awal_keperawatan_ranap_anak.rpk,penilaian_awal_keperawatan_ranap_anak.rpo,"+
                "penilaian_awal_keperawatan_ranap_anak.riwayat_pembedahan,penilaian_awal_keperawatan_ranap_anak.riwayat_dirawat_dirs,penilaian_awal_keperawatan_ranap_anak.alat_bantu_dipakai,"+
                "penilaian_awal_keperawatan_ranap_anak.riwayat_tranfusi,penilaian_awal_keperawatan_ranap_anak.riwayat_imunisasi,penilaian_awal_keperawatan_ranap_anak.riwayat_prkmbngan,penilaian_awal_keperawatan_ranap_anak.riwayat_prkmbngan_lainnya,penilaian_awal_keperawatan_ranap_anak.riwayat_alergi,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_mental,"+
                "penilaian_awal_keperawatan_ranap_anak.pemeriksaan_keadaan_umum,"+
                "penilaian_awal_keperawatan_ranap_anak.pemeriksaan_gcs,"+
                "penilaian_awal_keperawatan_ranap_anak.pemeriksaan_td,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_nadi,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_rr,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_suhu,"+
                "penilaian_awal_keperawatan_ranap_anak.pemeriksaan_spo2,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_bb,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_tb,"+
                "penilaian_awal_keperawatan_ranap_anak.pola_aktifitas_makanminum,penilaian_awal_keperawatan_ranap_anak.pola_aktifitas_mandi,penilaian_awal_keperawatan_ranap_anak.pola_aktifitas_eliminasi,penilaian_awal_keperawatan_ranap_anak.pola_aktifitas_berpakaian,"+
                "penilaian_awal_keperawatan_ranap_anak.pola_aktifitas_berpindah,penilaian_awal_keperawatan_ranap_anak.pola_nutrisi_frekuesi_makan,penilaian_awal_keperawatan_ranap_anak.pola_nutrisi_jenis_makanan,penilaian_awal_keperawatan_ranap_anak.pola_nutrisi_porsi_makan,"+
                "penilaian_awal_keperawatan_ranap_anak.pola_tidur_lama_tidur,penilaian_awal_keperawatan_ranap_anak.pola_tidur_gangguan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_kemampuan_sehari,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_aktifitas,"+
                "penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_berjalan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_berjalan_keterangan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_ambulasi,"+
                "penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_ekstrimitas_atas,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_ekstrimitas_atas_keterangan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_ekstrimitas_bawah,"+
                "penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_ekstrimitas_bawah_keterangan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_menggenggam,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_menggenggam_keterangan,"+
                "penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_koordinasi,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_koordinasi_keterangan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_kesimpulan,"+
                "penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_kondisi_psiko,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_gangguan_jiwa,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_perilaku,"+
                "penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_perilaku_keterangan,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_hubungan_keluarga,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_tinggal,"+
                "penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_tinggal_keterangan,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_nilai_kepercayaan,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_nilai_kepercayaan_keterangan,"+
                "penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_pendidikan_pj,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_edukasi_diberikan,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_edukasi_diberikan_keterangan,"+
                "penilaian_awal_keperawatan_ranap_anak.wajah,penilaian_awal_keperawatan_ranap_anak.nilaiwajah,penilaian_awal_keperawatan_ranap_anak.kaki,"+
                "penilaian_awal_keperawatan_ranap_anak.nilaikaki,penilaian_awal_keperawatan_ranap_anak.aktifitas,penilaian_awal_keperawatan_ranap_anak.nilaiaktifitas,penilaian_awal_keperawatan_ranap_anak.menangis,penilaian_awal_keperawatan_ranap_anak.nilaimenangis,"+
                "penilaian_awal_keperawatan_ranap_anak.bersuara,penilaian_awal_keperawatan_ranap_anak.nilaibersuara,penilaian_awal_keperawatan_ranap_anak.hasilnyeri,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala1,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai1,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala2,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai2,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala3,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai3,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala4,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai4,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala5,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai5,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala6,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai6,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala7,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai7,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_totalnilai,"+
                "penilaian_awal_keperawatan_ranap_anak.skrining_gizi1,penilaian_awal_keperawatan_ranap_anak.nilai_gizi1,penilaian_awal_keperawatan_ranap_anak.skrining_gizi2,"+
                "penilaian_awal_keperawatan_ranap_anak.nilai_gizi2,penilaian_awal_keperawatan_ranap_anak.nilai_total_gizi,penilaian_awal_keperawatan_ranap_anak.skrining_gizi_diagnosa_khusus,penilaian_awal_keperawatan_ranap_anak.skrining_gizi_ket_diagnosa_khusus,"+
                "penilaian_awal_keperawatan_ranap_anak.skrining_gizi_diketahui_dietisen,penilaian_awal_keperawatan_ranap_anak.skrining_gizi_jam_diketahui_dietisen,penilaian_awal_keperawatan_ranap_anak.rencana,penilaian_awal_keperawatan_ranap_anak.nip1,"+
                "penilaian_awal_keperawatan_ranap_anak.nip2,penilaian_awal_keperawatan_ranap_anak.kd_dokter,pasien.tgl_lahir,pasien.jk,pengkaji1.nama as pengkaji1,pengkaji2.nama as pengkaji2,dokter.nm_dokter,"+
                "reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.agama,pasien.pekerjaan,pasien.pnd,penjab.png_jawab,bahasa_pasien.nama_bahasa "+
                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "inner join penilaian_awal_keperawatan_ranap_anak on reg_periksa.no_rawat=penilaian_awal_keperawatan_ranap_anak.no_rawat "+
                "inner join petugas as pengkaji1 on penilaian_awal_keperawatan_ranap_anak.nip1=pengkaji1.nip "+
                "inner join petugas as pengkaji2 on penilaian_awal_keperawatan_ranap_anak.nip2=pengkaji2.nip "+
                "inner join dokter on penilaian_awal_keperawatan_ranap_anak.kd_dokter=dokter.kd_dokter "+
                "inner join bahasa_pasien on bahasa_pasien.id=pasien.bahasa_pasien "+
                "inner join penjab on penjab.kd_pj=reg_periksa.kd_pj where reg_periksa.no_rawat='"+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()+"'",param);
            
            Valid.MyReportqry("rptCetakPenilaianAwalKeperawatanRanap2.jasper","report","::[ Laporan Penilaian Awal Keperawatan Rawat Inap ]::",
                "select penilaian_awal_keperawatan_ranap_anak.no_rawat,penilaian_awal_keperawatan_ranap_anak.tanggal,penilaian_awal_keperawatan_ranap_anak.informasi,penilaian_awal_keperawatan_ranap_anak.ket_informasi,penilaian_awal_keperawatan_ranap_anak.tiba_diruang_rawat,"+
                "penilaian_awal_keperawatan_ranap_anak.kasus_trauma,penilaian_awal_keperawatan_ranap_anak.cara_masuk,penilaian_awal_keperawatan_ranap_anak.kel_utama,penilaian_awal_keperawatan_ranap_anak.rps,penilaian_awal_keperawatan_ranap_anak.rpd,penilaian_awal_keperawatan_ranap_anak.rpk,penilaian_awal_keperawatan_ranap_anak.rpo,"+
                "penilaian_awal_keperawatan_ranap_anak.riwayat_pembedahan,penilaian_awal_keperawatan_ranap_anak.riwayat_dirawat_dirs,penilaian_awal_keperawatan_ranap_anak.alat_bantu_dipakai,"+
                "penilaian_awal_keperawatan_ranap_anak.riwayat_tranfusi,penilaian_awal_keperawatan_ranap_anak.riwayat_imunisasi,penilaian_awal_keperawatan_ranap_anak.riwayat_prkmbngan,penilaian_awal_keperawatan_ranap_anak.riwayat_prkmbngan_lainnya,penilaian_awal_keperawatan_ranap_anak.riwayat_alergi,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_mental,"+
                "penilaian_awal_keperawatan_ranap_anak.pemeriksaan_keadaan_umum,"+
                "penilaian_awal_keperawatan_ranap_anak.pemeriksaan_gcs,"+
                "penilaian_awal_keperawatan_ranap_anak.pemeriksaan_td,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_nadi,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_rr,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_suhu,"+
                "penilaian_awal_keperawatan_ranap_anak.pemeriksaan_spo2,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_bb,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_tb,"+
                "penilaian_awal_keperawatan_ranap_anak.pola_aktifitas_makanminum,penilaian_awal_keperawatan_ranap_anak.pola_aktifitas_mandi,penilaian_awal_keperawatan_ranap_anak.pola_aktifitas_eliminasi,penilaian_awal_keperawatan_ranap_anak.pola_aktifitas_berpakaian,"+
                "penilaian_awal_keperawatan_ranap_anak.pola_aktifitas_berpindah,penilaian_awal_keperawatan_ranap_anak.pola_nutrisi_frekuesi_makan,penilaian_awal_keperawatan_ranap_anak.pola_nutrisi_jenis_makanan,penilaian_awal_keperawatan_ranap_anak.pola_nutrisi_porsi_makan,"+
                "penilaian_awal_keperawatan_ranap_anak.pola_tidur_lama_tidur,penilaian_awal_keperawatan_ranap_anak.pola_tidur_gangguan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_kemampuan_sehari,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_aktifitas,"+
                "penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_berjalan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_berjalan_keterangan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_ambulasi,"+
                "penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_ekstrimitas_atas,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_ekstrimitas_atas_keterangan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_ekstrimitas_bawah,"+
                "penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_ekstrimitas_bawah_keterangan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_menggenggam,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_menggenggam_keterangan,"+
                "penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_koordinasi,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_koordinasi_keterangan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_kesimpulan,"+
                "penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_kondisi_psiko,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_gangguan_jiwa,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_perilaku,"+
                "penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_perilaku_keterangan,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_hubungan_keluarga,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_tinggal,"+
                "penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_tinggal_keterangan,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_nilai_kepercayaan,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_nilai_kepercayaan_keterangan,"+
                "penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_pendidikan_pj,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_edukasi_diberikan,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_edukasi_diberikan_keterangan,"+
                "penilaian_awal_keperawatan_ranap_anak.wajah,penilaian_awal_keperawatan_ranap_anak.nilaiwajah,penilaian_awal_keperawatan_ranap_anak.kaki,"+
                "penilaian_awal_keperawatan_ranap_anak.nilaikaki,penilaian_awal_keperawatan_ranap_anak.aktifitas,penilaian_awal_keperawatan_ranap_anak.nilaiaktifitas,penilaian_awal_keperawatan_ranap_anak.menangis,penilaian_awal_keperawatan_ranap_anak.nilaimenangis,"+
                "penilaian_awal_keperawatan_ranap_anak.bersuara,penilaian_awal_keperawatan_ranap_anak.nilaibersuara,penilaian_awal_keperawatan_ranap_anak.hasilnyeri,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala1,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai1,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala2,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai2,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala3,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai3,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala4,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai4,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala5,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai5,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala6,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai6,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala7,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai7,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_totalnilai,"+
                "penilaian_awal_keperawatan_ranap_anak.skrining_gizi1,penilaian_awal_keperawatan_ranap_anak.nilai_gizi1,penilaian_awal_keperawatan_ranap_anak.skrining_gizi2,"+
                "penilaian_awal_keperawatan_ranap_anak.nilai_gizi2,penilaian_awal_keperawatan_ranap_anak.nilai_total_gizi,penilaian_awal_keperawatan_ranap_anak.skrining_gizi_diagnosa_khusus,penilaian_awal_keperawatan_ranap_anak.skrining_gizi_ket_diagnosa_khusus,"+
                "penilaian_awal_keperawatan_ranap_anak.skrining_gizi_diketahui_dietisen,penilaian_awal_keperawatan_ranap_anak.skrining_gizi_jam_diketahui_dietisen,penilaian_awal_keperawatan_ranap_anak.rencana,penilaian_awal_keperawatan_ranap_anak.nip1,"+
                "penilaian_awal_keperawatan_ranap_anak.nip2,penilaian_awal_keperawatan_ranap_anak.kd_dokter,pasien.tgl_lahir,pasien.jk,pengkaji1.nama as pengkaji1,pengkaji2.nama as pengkaji2,dokter.nm_dokter,"+
                "reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.agama,pasien.pekerjaan,pasien.pnd,penjab.png_jawab,bahasa_pasien.nama_bahasa "+
                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "inner join penilaian_awal_keperawatan_ranap_anak on reg_periksa.no_rawat=penilaian_awal_keperawatan_ranap_anak.no_rawat "+
                "inner join petugas as pengkaji1 on penilaian_awal_keperawatan_ranap_anak.nip1=pengkaji1.nip "+
                "inner join petugas as pengkaji2 on penilaian_awal_keperawatan_ranap_anak.nip2=pengkaji2.nip "+
                "inner join dokter on penilaian_awal_keperawatan_ranap_anak.kd_dokter=dokter.kd_dokter "+
                "inner join bahasa_pasien on bahasa_pasien.id=pasien.bahasa_pasien "+
                "inner join penjab on penjab.kd_pj=reg_periksa.kd_pj where reg_periksa.no_rawat='"+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()+"'",param);
        }else{
            JOptionPane.showMessageDialog(null,"Maaf, silahkan pilih data terlebih dahulu..!!!!");
        }  
    }//GEN-LAST:event_BtnPrint1ActionPerformed

    private void TglAsuhanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TglAsuhanKeyPressed
        Valid.pindah(evt,BtnDPJP,MacamKasus);
    }//GEN-LAST:event_TglAsuhanKeyPressed

    private void AnamnesisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AnamnesisKeyPressed
        Valid.pindah(evt,MacamKasus,KetAnamnesis);
    }//GEN-LAST:event_AnamnesisKeyPressed

    private void BtnPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPetugasKeyPressed
        Valid.pindah(evt,BtnSimpan,BtnPetugas2);
    }//GEN-LAST:event_BtnPetugasKeyPressed

    private void BtnPetugasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPetugasActionPerformed
        i=1;
        petugas.isCek();
        petugas.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        petugas.setLocationRelativeTo(internalFrame1);
        petugas.setAlwaysOnTop(false);
        petugas.setVisible(true);
    }//GEN-LAST:event_BtnPetugasActionPerformed

    private void KdPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdPetugasKeyPressed

    }//GEN-LAST:event_KdPetugasKeyPressed

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            isRawat();
        }else{
            Valid.pindah(evt,TCari,BtnPetugas);
        }
    }//GEN-LAST:event_TNoRwKeyPressed

    private void BtnPetugas2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPetugas2ActionPerformed
        i=2;
        petugas.isCek();
        petugas.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        petugas.setLocationRelativeTo(internalFrame1);
        petugas.setAlwaysOnTop(false);
        petugas.setVisible(true);
    }//GEN-LAST:event_BtnPetugas2ActionPerformed

    private void BtnPetugas2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPetugas2KeyPressed
        Valid.pindah(evt,BtnPetugas,BtnDPJP);
    }//GEN-LAST:event_BtnPetugas2KeyPressed

    private void KdPetugas2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdPetugas2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_KdPetugas2KeyPressed

    private void KdDPJPKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdDPJPKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_KdDPJPKeyPressed

    private void BtnDPJPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDPJPActionPerformed
        dokter.isCek();
        dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setAlwaysOnTop(false);
        dokter.setVisible(true);
    }//GEN-LAST:event_BtnDPJPActionPerformed

    private void BtnDPJPKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnDPJPKeyPressed
        Valid.pindah(evt,BtnPetugas2,MacamKasus);
    }//GEN-LAST:event_BtnDPJPKeyPressed

    private void TibadiRuangKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TibadiRuangKeyPressed
        Valid.pindah(evt,KetAnamnesis,CaraMasuk);
    }//GEN-LAST:event_TibadiRuangKeyPressed

    private void CaraMasukKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_CaraMasukKeyPressed
        Valid.pindah(evt,TibadiRuang,RPS);
    }//GEN-LAST:event_CaraMasukKeyPressed

    private void AlergiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AlergiKeyPressed
        Valid.pindah(evt,RTranfusi,KelUtama);
    }//GEN-LAST:event_AlergiKeyPressed

    private void RPSKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RPSKeyPressed
        Valid.pindah2(evt,CaraMasuk,RPD);
    }//GEN-LAST:event_RPSKeyPressed

    private void RPKKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RPKKeyPressed
        Valid.pindah2(evt,RPD,RPO);
    }//GEN-LAST:event_RPKKeyPressed

    private void RPDKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RPDKeyPressed
        Valid.pindah2(evt,RPS,RPK);
    }//GEN-LAST:event_RPDKeyPressed

    private void RPOKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RPOKeyPressed
        Valid.pindah2(evt,RPK,RPembedahan);
    }//GEN-LAST:event_RPOKeyPressed

    private void DetailRencanaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DetailRencanaKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_DetailRencanaKeyPressed

    private void MacamKasusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MacamKasusKeyPressed
        Valid.pindah(evt,BtnDPJP,Anamnesis);
    }//GEN-LAST:event_MacamKasusKeyPressed

    private void KetAnamnesisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KetAnamnesisKeyPressed
        Valid.pindah(evt,Anamnesis,TibadiRuang);
    }//GEN-LAST:event_KetAnamnesisKeyPressed

    private void RDirawatRSKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RDirawatRSKeyPressed
        Valid.pindah(evt,RPembedahan,AlatBantuDipakai);
    }//GEN-LAST:event_RDirawatRSKeyPressed

    private void RPembedahanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RPembedahanKeyPressed
        Valid.pindah(evt,RPO,RDirawatRS);
    }//GEN-LAST:event_RPembedahanKeyPressed

    private void AlatBantuDipakaiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AlatBantuDipakaiKeyPressed
        //Valid.pindah(evt,RDirawatRS,SedangMenyusui);
    }//GEN-LAST:event_AlatBantuDipakaiKeyPressed

    private void RTranfusiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RTranfusiKeyPressed
   //     Valid.pindah(evt,KetSedangMenyusui,Alergi);
    }//GEN-LAST:event_RTranfusiKeyPressed

    private void KesadaranMentalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KesadaranMentalKeyPressed
   //     Valid.pindah(evt,OlahRaga,KeadaanMentalUmum);
    }//GEN-LAST:event_KesadaranMentalKeyPressed

    private void KeadaanMentalUmumKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeadaanMentalUmumKeyPressed
        Valid.pindah(evt,KesadaranMental,GCS);
    }//GEN-LAST:event_KeadaanMentalUmumKeyPressed

    private void GCSKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_GCSKeyPressed
        Valid.pindah(evt,KeadaanMentalUmum,TD);
    }//GEN-LAST:event_GCSKeyPressed

    private void TDKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TDKeyPressed
        Valid.pindah(evt,GCS,Nadi);
    }//GEN-LAST:event_TDKeyPressed

    private void NadiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NadiKeyPressed
        Valid.pindah(evt,TD,RR);
    }//GEN-LAST:event_NadiKeyPressed

    private void RRKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RRKeyPressed
        Valid.pindah(evt,Nadi,Suhu);
    }//GEN-LAST:event_RRKeyPressed

    private void SuhuKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SuhuKeyPressed
        Valid.pindah(evt,RR,SpO2);
    }//GEN-LAST:event_SuhuKeyPressed

    private void SpO2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SpO2KeyPressed
        Valid.pindah(evt,Suhu,BB);
    }//GEN-LAST:event_SpO2KeyPressed

    private void BBKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BBKeyPressed
        Valid.pindah(evt,SpO2,TB);
    }//GEN-LAST:event_BBKeyPressed

    private void TBKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TBKeyPressed
      //  Valid.pindah(evt,BB,SistemSarafKepala);
    }//GEN-LAST:event_TBKeyPressed

    private void PolaAktifitasEliminasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PolaAktifitasEliminasiKeyPressed
        Valid.pindah(evt,PolaAktifitasBerpakaian,PolaAktifitasBerpindah);
    }//GEN-LAST:event_PolaAktifitasEliminasiKeyPressed

    private void PolaAktifitasMandiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PolaAktifitasMandiKeyPressed
   //     Valid.pindah(evt,LBAK,PolaAktifitasMakan);
    }//GEN-LAST:event_PolaAktifitasMandiKeyPressed

    private void PolaAktifitasMakanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PolaAktifitasMakanKeyPressed
        Valid.pindah(evt,PolaAktifitasMandi,PolaAktifitasBerpakaian);
    }//GEN-LAST:event_PolaAktifitasMakanKeyPressed

    private void PolaAktifitasBerpakaianKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PolaAktifitasBerpakaianKeyPressed
        Valid.pindah(evt,PolaAktifitasMakan,PolaAktifitasEliminasi);
    }//GEN-LAST:event_PolaAktifitasBerpakaianKeyPressed

    private void PolaAktifitasBerpindahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PolaAktifitasBerpindahKeyPressed
        Valid.pindah(evt,PolaAktifitasEliminasi,PolaNutrisiPorsi);
    }//GEN-LAST:event_PolaAktifitasBerpindahKeyPressed

    private void PolaNutrisiPorsiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PolaNutrisiPorsiKeyPressed
        Valid.pindah(evt,PolaAktifitasBerpindah,PolaNutrisiFrekuensi);
    }//GEN-LAST:event_PolaNutrisiPorsiKeyPressed

    private void PolaNutrisiFrekuensiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PolaNutrisiFrekuensiKeyPressed
        Valid.pindah(evt,PolaNutrisiPorsi,PolaNutrisiJenis);
    }//GEN-LAST:event_PolaNutrisiFrekuensiKeyPressed

    private void PolaNutrisiJenisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PolaNutrisiJenisKeyPressed
        Valid.pindah(evt,PolaNutrisiFrekuensi,PolaTidurLama);
    }//GEN-LAST:event_PolaNutrisiJenisKeyPressed

    private void PolaTidurLamaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PolaTidurLamaKeyPressed
        Valid.pindah(evt,PolaNutrisiJenis,PolaTidurGangguan);
    }//GEN-LAST:event_PolaTidurLamaKeyPressed

    private void PolaTidurGangguanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PolaTidurGangguanKeyPressed
        Valid.pindah(evt,PolaTidurLama,AktifitasSehari2);
    }//GEN-LAST:event_PolaTidurGangguanKeyPressed

    private void AktifitasSehari2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AktifitasSehari2KeyPressed
        Valid.pindah(evt,PolaTidurGangguan,Berjalan);
    }//GEN-LAST:event_AktifitasSehari2KeyPressed

    private void BerjalanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BerjalanKeyPressed
        Valid.pindah(evt,AktifitasSehari2,KeteranganBerjalan);
    }//GEN-LAST:event_BerjalanKeyPressed

    private void KeteranganBerjalanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganBerjalanKeyPressed
        Valid.pindah(evt,Berjalan,Aktifitas);
    }//GEN-LAST:event_KeteranganBerjalanKeyPressed

    private void AktifitasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AktifitasKeyPressed
        Valid.pindah(evt,KeteranganBerjalan,AlatAmbulasi);
    }//GEN-LAST:event_AktifitasKeyPressed

    private void AlatAmbulasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AlatAmbulasiKeyPressed
        Valid.pindah(evt,Aktifitas,EkstrimitasAtas);
    }//GEN-LAST:event_AlatAmbulasiKeyPressed

    private void EkstrimitasAtasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_EkstrimitasAtasKeyPressed
        Valid.pindah(evt,AlatAmbulasi,KeteranganEkstrimitasAtas);
    }//GEN-LAST:event_EkstrimitasAtasKeyPressed

    private void KeteranganEkstrimitasAtasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganEkstrimitasAtasKeyPressed
        Valid.pindah(evt,EkstrimitasAtas,EkstrimitasBawah);
    }//GEN-LAST:event_KeteranganEkstrimitasAtasKeyPressed

    private void EkstrimitasBawahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_EkstrimitasBawahKeyPressed
        Valid.pindah(evt,KeteranganEkstrimitasAtas,KeteranganEkstrimitasBawah);
    }//GEN-LAST:event_EkstrimitasBawahKeyPressed

    private void KeteranganEkstrimitasBawahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganEkstrimitasBawahKeyPressed
        Valid.pindah(evt,EkstrimitasBawah,KemampuanMenggenggam);
    }//GEN-LAST:event_KeteranganEkstrimitasBawahKeyPressed

    private void KemampuanMenggenggamKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KemampuanMenggenggamKeyPressed
        Valid.pindah(evt,KeteranganEkstrimitasBawah,KeteranganKemampuanMenggenggam);
    }//GEN-LAST:event_KemampuanMenggenggamKeyPressed

    private void KeteranganKemampuanMenggenggamKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganKemampuanMenggenggamKeyPressed
        Valid.pindah(evt,KemampuanMenggenggam,KemampuanKoordinasi);
    }//GEN-LAST:event_KeteranganKemampuanMenggenggamKeyPressed

    private void KemampuanKoordinasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KemampuanKoordinasiKeyPressed
        Valid.pindah(evt,KeteranganKemampuanMenggenggam,KeteranganKemampuanKoordinasi);
    }//GEN-LAST:event_KemampuanKoordinasiKeyPressed

    private void KeteranganKemampuanKoordinasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganKemampuanKoordinasiKeyPressed
        Valid.pindah(evt,KemampuanKoordinasi,KesimpulanGangguanFungsi);
    }//GEN-LAST:event_KeteranganKemampuanKoordinasiKeyPressed

    private void KesimpulanGangguanFungsiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KesimpulanGangguanFungsiKeyPressed
        Valid.pindah(evt,KeteranganKemampuanKoordinasi,KondisiPsikologis);
    }//GEN-LAST:event_KesimpulanGangguanFungsiKeyPressed

    private void KondisiPsikologisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KondisiPsikologisKeyPressed
        Valid.pindah(evt,KesimpulanGangguanFungsi,AdakahPerilaku);
    }//GEN-LAST:event_KondisiPsikologisKeyPressed

    private void AdakahPerilakuKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AdakahPerilakuKeyPressed
        Valid.pindah(evt,KondisiPsikologis,KeteranganAdakahPerilaku);
    }//GEN-LAST:event_AdakahPerilakuKeyPressed

    private void KeteranganAdakahPerilakuKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganAdakahPerilakuKeyPressed
        Valid.pindah(evt,AdakahPerilaku,GangguanJiwa);
    }//GEN-LAST:event_KeteranganAdakahPerilakuKeyPressed

    private void GangguanJiwaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_GangguanJiwaKeyPressed
        Valid.pindah(evt,KeteranganAdakahPerilaku,HubunganAnggotaKeluarga);
    }//GEN-LAST:event_GangguanJiwaKeyPressed

    private void HubunganAnggotaKeluargaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_HubunganAnggotaKeluargaKeyPressed
        Valid.pindah(evt,GangguanJiwa,TinggalDengan);
    }//GEN-LAST:event_HubunganAnggotaKeluargaKeyPressed

    private void TinggalDenganKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TinggalDenganKeyPressed
        Valid.pindah(evt,HubunganAnggotaKeluarga,KeteranganTinggalDengan);
    }//GEN-LAST:event_TinggalDenganKeyPressed

    private void KeteranganTinggalDenganKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganTinggalDenganKeyPressed
        Valid.pindah(evt,TinggalDengan,NilaiKepercayaan);
    }//GEN-LAST:event_KeteranganTinggalDenganKeyPressed

    private void NilaiKepercayaanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NilaiKepercayaanKeyPressed
        Valid.pindah(evt,KeteranganTinggalDengan,KeteranganNilaiKepercayaan);
    }//GEN-LAST:event_NilaiKepercayaanKeyPressed

    private void KeteranganNilaiKepercayaanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganNilaiKepercayaanKeyPressed
        Valid.pindah(evt,NilaiKepercayaan,PendidikanPJ);
    }//GEN-LAST:event_KeteranganNilaiKepercayaanKeyPressed

    private void PendidikanPJKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PendidikanPJKeyPressed
        Valid.pindah(evt,KeteranganNilaiKepercayaan,EdukasiPsikolgis);
    }//GEN-LAST:event_PendidikanPJKeyPressed

    private void EdukasiPsikolgisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_EdukasiPsikolgisKeyPressed
        Valid.pindah(evt,PendidikanPJ,KeteranganEdukasiPsikologis);
    }//GEN-LAST:event_EdukasiPsikolgisKeyPressed

    private void KeteranganEdukasiPsikologisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganEdukasiPsikologisKeyPressed
    //    Valid.pindah(evt,EdukasiPsikolgis,Nyeri);
    }//GEN-LAST:event_KeteranganEdukasiPsikologisKeyPressed

    private void SkalaGizi1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_SkalaGizi1ItemStateChanged
        if(SkalaGizi1.getSelectedIndex()==0){
            NilaiGizi1.setText("0");
        }else if(SkalaGizi1.getSelectedIndex()==1){
            NilaiGizi1.setText("2");
        }else if(SkalaGizi1.getSelectedIndex()==2){
            NilaiGizi1.setText("1");
        }else if(SkalaGizi1.getSelectedIndex()==3){
            NilaiGizi1.setText("2");
        }else if(SkalaGizi1.getSelectedIndex()==4){
            NilaiGizi1.setText("3");
        }else if(SkalaGizi1.getSelectedIndex()==5){
            NilaiGizi1.setText("4");
        }
        isTotalGizi();
    }//GEN-LAST:event_SkalaGizi1ItemStateChanged

    private void SkalaGizi1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SkalaGizi1KeyPressed
   //     Valid.pindah(evt,SkalaSydney11,SkalaGizi2);
    }//GEN-LAST:event_SkalaGizi1KeyPressed

    private void SkalaGizi2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_SkalaGizi2ItemStateChanged
        if(SkalaGizi2.getSelectedIndex()==0){
            NilaiGizi2.setText("0");
        }else if(SkalaGizi2.getSelectedIndex()==1){
            NilaiGizi2.setText("1");
        }
        isTotalGizi();
    }//GEN-LAST:event_SkalaGizi2ItemStateChanged

    private void SkalaGizi2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SkalaGizi2KeyPressed
        Valid.pindah(evt,SkalaGizi1,DiagnosaKhususGizi);
    }//GEN-LAST:event_SkalaGizi2KeyPressed

    private void DiagnosaKhususGiziKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DiagnosaKhususGiziKeyPressed
        Valid.pindah(evt,SkalaGizi2,KeteranganDiagnosaKhususGizi);
    }//GEN-LAST:event_DiagnosaKhususGiziKeyPressed

    private void KeteranganDiagnosaKhususGiziKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganDiagnosaKhususGiziKeyPressed
        Valid.pindah(evt,DiagnosaKhususGizi,DiketahuiDietisen);
    }//GEN-LAST:event_KeteranganDiagnosaKhususGiziKeyPressed

    private void DiketahuiDietisenKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DiketahuiDietisenKeyPressed
        Valid.pindah(evt,KeteranganDiagnosaKhususGizi,KeteranganDiketahuiDietisen);
    }//GEN-LAST:event_DiketahuiDietisenKeyPressed

    private void KeteranganDiketahuiDietisenKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganDiketahuiDietisenKeyPressed
        Valid.pindah(evt,DiketahuiDietisen,TCariMasalah);
    }//GEN-LAST:event_KeteranganDiketahuiDietisenKeyPressed

    private void tbMasalahKeperawatanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbMasalahKeperawatanMouseClicked
        if(tabModeMasalah.getRowCount()!=0){
            try {
                tampilRencana2();
            } catch (java.lang.NullPointerException e) {
            }
        }
    }//GEN-LAST:event_tbMasalahKeperawatanMouseClicked

    private void tbMasalahKeperawatanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbMasalahKeperawatanKeyPressed
        if(tabModeMasalah.getRowCount()!=0){
            if(evt.getKeyCode()==KeyEvent.VK_SHIFT){
                TCariMasalah.setText("");
                TCariMasalah.requestFocus();
            }
        }
    }//GEN-LAST:event_tbMasalahKeperawatanKeyPressed

    private void tbMasalahKeperawatanKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbMasalahKeperawatanKeyReleased
        if(tabModeMasalah.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    tampilRencana2();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbMasalahKeperawatanKeyReleased

    private void RencanaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RencanaKeyPressed
        Valid.pindah2(evt,TCariMasalah,BtnSimpan);
    }//GEN-LAST:event_RencanaKeyPressed

    private void BtnTambahMasalahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTambahMasalahActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        MasterMasalahKeperawatan form=new MasterMasalahKeperawatan(null,false);
        form.isCek();
        form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        form.setLocationRelativeTo(internalFrame1);
        form.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnTambahMasalahActionPerformed

    private void BtnAllMasalahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllMasalahActionPerformed
        TCari.setText("");
        tampilMasalah();
    }//GEN-LAST:event_BtnAllMasalahActionPerformed

    private void BtnAllMasalahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllMasalahKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnAllMasalahActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnCariMasalah, TCariMasalah);
        }
    }//GEN-LAST:event_BtnAllMasalahKeyPressed

    private void BtnCariMasalahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariMasalahActionPerformed
        tampilMasalah2();
    }//GEN-LAST:event_BtnCariMasalahActionPerformed

    private void BtnCariMasalahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariMasalahKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            tampilMasalah2();
        }else if((evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN)||(evt.getKeyCode()==KeyEvent.VK_TAB)){
            Rencana.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            KeteranganDiketahuiDietisen.requestFocus();
        }
    }//GEN-LAST:event_BtnCariMasalahKeyPressed

    private void TCariMasalahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariMasalahKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            tampilMasalah2();
        }else if((evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN)||(evt.getKeyCode()==KeyEvent.VK_TAB)){
            Rencana.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            KeteranganDiketahuiDietisen.requestFocus();
        }
    }//GEN-LAST:event_TCariMasalahKeyPressed

    private void BtnTambahRencanaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTambahRencanaActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        MasterRencanaKeperawatan form=new MasterRencanaKeperawatan(null,false);
        form.isCek();
        form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        form.setLocationRelativeTo(internalFrame1);
        form.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnTambahRencanaActionPerformed

    private void BtnAllRencanaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllRencanaActionPerformed
        TCariRencana.setText("");
        tampilRencana();
        tampilRencana2();
    }//GEN-LAST:event_BtnAllRencanaActionPerformed

    private void BtnAllRencanaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllRencanaKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnAllRencanaActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnCariRencana, TCariRencana);
        }
    }//GEN-LAST:event_BtnAllRencanaKeyPressed

    private void BtnCariRencanaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariRencanaActionPerformed
        tampilRencana2();
    }//GEN-LAST:event_BtnCariRencanaActionPerformed

    private void BtnCariRencanaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariRencanaKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            tampilRencana2();
        }else if((evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN)||(evt.getKeyCode()==KeyEvent.VK_TAB)){
            BtnSimpan.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            TCariRencana.requestFocus();
        }
    }//GEN-LAST:event_BtnCariRencanaKeyPressed

    private void TCariRencanaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariRencanaKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            tampilRencana2();
        }else if((evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN)||(evt.getKeyCode()==KeyEvent.VK_TAB)){
            BtnCariRencana.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            TCariMasalah.requestFocus();
        }
    }//GEN-LAST:event_TCariRencanaKeyPressed

    private void SkalaResiko7ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_SkalaResiko7ItemStateChanged
        if(SkalaResiko7.getSelectedIndex()==0){
            NilaiResiko7.setText("4");
        }else if(SkalaResiko7.getSelectedIndex()==1){
            NilaiResiko7.setText("3");
        }else if(SkalaResiko7.getSelectedIndex()==2){
            NilaiResiko7.setText("2");
        }else{
            NilaiResiko7.setText("1");
        }
        isTotalResikoJatuh1();
    }//GEN-LAST:event_SkalaResiko7ItemStateChanged

    private void SkalaResiko7KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SkalaResiko7KeyPressed
        Valid.pindah(evt,NilaiResikoTotal1,SkalaResiko8);
    }//GEN-LAST:event_SkalaResiko7KeyPressed

    private void SkalaResiko8ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_SkalaResiko8ItemStateChanged
        if(SkalaResiko8.getSelectedIndex()==0){
            NilaiResiko8.setText("2");
        }else{
            NilaiResiko8.setText("1");
        }
        isTotalResikoJatuh1();
    }//GEN-LAST:event_SkalaResiko8ItemStateChanged

    private void SkalaResiko8KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SkalaResiko8KeyPressed
        Valid.pindah(evt,SkalaResiko7,SkalaResiko9);
    }//GEN-LAST:event_SkalaResiko8KeyPressed

    private void SkalaResiko9ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_SkalaResiko9ItemStateChanged
        if(SkalaResiko9.getSelectedIndex()==0){
            NilaiResiko9.setText("4");
        }else if(SkalaResiko9.getSelectedIndex()==1){
            NilaiResiko9.setText("3");
        }else if(SkalaResiko9.getSelectedIndex()==2){
            NilaiResiko9.setText("2");
        }else{
            NilaiResiko9.setText("1");
        }
        isTotalResikoJatuh1();
    }//GEN-LAST:event_SkalaResiko9ItemStateChanged

    private void SkalaResiko9KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SkalaResiko9KeyPressed
        Valid.pindah(evt,SkalaResiko8,SkalaResiko10);
    }//GEN-LAST:event_SkalaResiko9KeyPressed

    private void SkalaResiko10ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_SkalaResiko10ItemStateChanged
        if(SkalaResiko10.getSelectedIndex()==0){
            NilaiResiko10.setText("3");
        }else if(SkalaResiko10.getSelectedIndex()==1){
            NilaiResiko10.setText("2");
        }else{
            NilaiResiko10.setText("1");
        }
        isTotalResikoJatuh1();
    }//GEN-LAST:event_SkalaResiko10ItemStateChanged

    private void SkalaResiko10KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SkalaResiko10KeyPressed
        Valid.pindah(evt,SkalaResiko9,SkalaResiko11);
    }//GEN-LAST:event_SkalaResiko10KeyPressed

    private void SkalaResiko11ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_SkalaResiko11ItemStateChanged
        if(SkalaResiko11.getSelectedIndex()==0){
            NilaiResiko11.setText("4");
        }else if(SkalaResiko11.getSelectedIndex()==1){
            NilaiResiko11.setText("3");
        }else if(SkalaResiko11.getSelectedIndex()==2){
            NilaiResiko11.setText("2");
        }else{
            NilaiResiko11.setText("1");
        }
        isTotalResikoJatuh1();
    }//GEN-LAST:event_SkalaResiko11ItemStateChanged

    private void SkalaResiko11KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SkalaResiko11KeyPressed
        Valid.pindah(evt,SkalaResiko10,SkalaResiko12);
    }//GEN-LAST:event_SkalaResiko11KeyPressed

    private void SkalaResiko12ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_SkalaResiko12ItemStateChanged
        if(SkalaResiko12.getSelectedIndex()==0){
            NilaiResiko12.setText("3");
        }else if(SkalaResiko12.getSelectedIndex()==1){
            NilaiResiko12.setText("2");
        }else{
            NilaiResiko12.setText("1");
        }
        isTotalResikoJatuh1();
    }//GEN-LAST:event_SkalaResiko12ItemStateChanged

    private void SkalaResiko12KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SkalaResiko12KeyPressed
        Valid.pindah(evt,SkalaResiko11,SkalaResiko13);
    }//GEN-LAST:event_SkalaResiko12KeyPressed

    private void SkalaResiko13ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_SkalaResiko13ItemStateChanged
        if(SkalaResiko13.getSelectedIndex()==0){
            NilaiResiko13.setText("3");
        }else if(SkalaResiko13.getSelectedIndex()==1){
            NilaiResiko13.setText("2");
        }else{
            NilaiResiko13.setText("1");
        }
        isTotalResikoJatuh1();
    }//GEN-LAST:event_SkalaResiko13ItemStateChanged

    private void SkalaResiko13KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SkalaResiko13KeyPressed
       // Valid.pindah(evt,SkalaResiko13,SkalaSydney1);
    }//GEN-LAST:event_SkalaResiko13KeyPressed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,Rencana,BtnBatal);
        }
    }//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            emptTeks();
        }else{
            Valid.pindah(evt, BtnSimpan, BtnHapus);
        }
    }//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        emptTeks();
    }//GEN-LAST:event_BtnBatalActionPerformed

    private void RPerkmAnkKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RPerkmAnkKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_RPerkmAnkKeyPressed

    private void KetPerkmAnkKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KetPerkmAnkKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_KetPerkmAnkKeyPressed

    private void KelUtamaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KelUtamaKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_KelUtamaKeyPressed

    private void RiImunisasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RiImunisasiKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_RiImunisasiKeyPressed

    private void SkalaWajahItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_SkalaWajahItemStateChanged
        NilaiWajah.setText(SkalaWajah.getSelectedIndex()+"");
        SkalaNyeri1.setText(""+(Integer.parseInt(NilaiWajah.getText())+Integer.parseInt(NilaiKaki.getText())+Integer.parseInt(NilaiAktifitas.getText())+Integer.parseInt(NilaiMenangis.getText())+Integer.parseInt(NilaiBersuara.getText())));
        isTotalSkalaNyeri();
    }//GEN-LAST:event_SkalaWajahItemStateChanged

    private void SkalaWajahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SkalaWajahKeyPressed
    //    Valid.pindah(evt,SG4,SkalaKaki);
    }//GEN-LAST:event_SkalaWajahKeyPressed

    private void SkalaKakiItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_SkalaKakiItemStateChanged
        NilaiKaki.setText(SkalaKaki.getSelectedIndex()+"");
        SkalaNyeri1.setText(""+(Integer.parseInt(NilaiWajah.getText())+Integer.parseInt(NilaiKaki.getText())+Integer.parseInt(NilaiAktifitas.getText())+Integer.parseInt(NilaiMenangis.getText())+Integer.parseInt(NilaiBersuara.getText())));
        isTotalSkalaNyeri();
    }//GEN-LAST:event_SkalaKakiItemStateChanged

    private void SkalaKakiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SkalaKakiKeyPressed
        Valid.pindah(evt,SkalaWajah,SkalaAktifitas);
    }//GEN-LAST:event_SkalaKakiKeyPressed

    private void SkalaAktifitasItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_SkalaAktifitasItemStateChanged
        NilaiAktifitas.setText(SkalaAktifitas.getSelectedIndex()+"");
        SkalaNyeri1.setText(""+(Integer.parseInt(NilaiWajah.getText())+Integer.parseInt(NilaiKaki.getText())+Integer.parseInt(NilaiAktifitas.getText())+Integer.parseInt(NilaiMenangis.getText())+Integer.parseInt(NilaiBersuara.getText())));
        isTotalSkalaNyeri();
    }//GEN-LAST:event_SkalaAktifitasItemStateChanged

    private void SkalaAktifitasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SkalaAktifitasKeyPressed
        Valid.pindah(evt,SkalaKaki,SkalaMenangis);
    }//GEN-LAST:event_SkalaAktifitasKeyPressed

    private void NilaiAktifitasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NilaiAktifitasKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_NilaiAktifitasKeyPressed

    private void NilaiKakiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NilaiKakiKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_NilaiKakiKeyPressed

    private void NilaiWajahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NilaiWajahKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_NilaiWajahKeyPressed

    private void SkalaMenangisItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_SkalaMenangisItemStateChanged
        NilaiMenangis.setText(SkalaMenangis.getSelectedIndex()+"");
        SkalaNyeri1.setText(""+(Integer.parseInt(NilaiWajah.getText())+Integer.parseInt(NilaiKaki.getText())+Integer.parseInt(NilaiAktifitas.getText())+Integer.parseInt(NilaiMenangis.getText())+Integer.parseInt(NilaiBersuara.getText())));
        isTotalSkalaNyeri();
    }//GEN-LAST:event_SkalaMenangisItemStateChanged

    private void SkalaMenangisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SkalaMenangisKeyPressed
        Valid.pindah(evt,SkalaAktifitas,SkalaBersuara);
    }//GEN-LAST:event_SkalaMenangisKeyPressed

    private void SkalaBersuaraItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_SkalaBersuaraItemStateChanged
        NilaiBersuara.setText(SkalaBersuara.getSelectedIndex()+"");
        SkalaNyeri1.setText(""+(Integer.parseInt(NilaiWajah.getText())+Integer.parseInt(NilaiKaki.getText())+Integer.parseInt(NilaiAktifitas.getText())+Integer.parseInt(NilaiMenangis.getText())+Integer.parseInt(NilaiBersuara.getText())));
        isTotalSkalaNyeri();
    }//GEN-LAST:event_SkalaBersuaraItemStateChanged

    private void SkalaBersuaraKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SkalaBersuaraKeyPressed
//        Valid.pindah(evt,SkalaMenangis,Nyeri);
    }//GEN-LAST:event_SkalaBersuaraKeyPressed

    private void NilaiMenangisKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NilaiMenangisKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_NilaiMenangisKeyPressed

    private void NilaiBersuaraKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NilaiBersuaraKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_NilaiBersuaraKeyPressed

    private void SkalaNyeri1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SkalaNyeri1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_SkalaNyeri1KeyPressed

    private void SkalaResiko7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SkalaResiko7ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SkalaResiko7ActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            RMPenilaianAwalKeperawatanRanap dialog = new RMPenilaianAwalKeperawatanRanap(new javax.swing.JFrame(), true);
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
    private widget.ComboBox AdakahPerilaku;
    private widget.TextBox Agama;
    private widget.ComboBox Aktifitas;
    private widget.ComboBox AktifitasSehari2;
    private widget.ComboBox AlatAmbulasi;
    private widget.ComboBox AlatBantuDipakai;
    private widget.TextBox Alergi;
    private widget.ComboBox Anamnesis;
    private widget.TextBox BB;
    private widget.TextBox Bahasa;
    private widget.ComboBox Berjalan;
    private widget.Button BtnAll;
    private widget.Button BtnAllMasalah;
    private widget.Button BtnAllRencana;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnCariMasalah;
    private widget.Button BtnCariRencana;
    private widget.Button BtnDPJP;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPetugas;
    private widget.Button BtnPetugas2;
    private widget.Button BtnPrint;
    private widget.Button BtnPrint1;
    private widget.Button BtnSimpan;
    private widget.Button BtnTambahMasalah;
    private widget.Button BtnTambahRencana;
    private widget.TextBox CaraBayar;
    private widget.ComboBox CaraMasuk;
    private widget.CekBox ChkAccor;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.TextArea DetailRencana;
    private widget.ComboBox DiagnosaKhususGizi;
    private widget.ComboBox DiketahuiDietisen;
    private widget.ComboBox EdukasiPsikolgis;
    private widget.ComboBox EkstrimitasAtas;
    private widget.ComboBox EkstrimitasBawah;
    private widget.PanelBiasa FormInput;
    private widget.PanelBiasa FormMasalahRencana;
    private widget.PanelBiasa FormMenu;
    private widget.TextBox GCS;
    private widget.ComboBox GangguanJiwa;
    private widget.ComboBox HubunganAnggotaKeluarga;
    private widget.TextBox Jk;
    private widget.TextBox KdDPJP;
    private widget.TextBox KdPetugas;
    private widget.TextBox KdPetugas2;
    private widget.ComboBox KeadaanMentalUmum;
    private widget.TextArea KelUtama;
    private widget.ComboBox KemampuanKoordinasi;
    private widget.ComboBox KemampuanMenggenggam;
    private widget.TextBox KesadaranMental;
    private widget.ComboBox KesimpulanGangguanFungsi;
    private widget.TextBox KetAnamnesis;
    private widget.TextBox KetPerkmAnk;
    private widget.TextBox KeteranganAdakahPerilaku;
    private widget.TextBox KeteranganBerjalan;
    private widget.TextBox KeteranganDiagnosaKhususGizi;
    private widget.TextBox KeteranganDiketahuiDietisen;
    private widget.TextBox KeteranganEdukasiPsikologis;
    private widget.TextBox KeteranganEkstrimitasAtas;
    private widget.TextBox KeteranganEkstrimitasBawah;
    private widget.TextBox KeteranganKemampuanKoordinasi;
    private widget.TextBox KeteranganKemampuanMenggenggam;
    private widget.TextBox KeteranganNilaiKepercayaan;
    private widget.TextBox KeteranganTinggalDengan;
    private widget.ComboBox KondisiPsikologis;
    private widget.Label LCount;
    private widget.ComboBox MacamKasus;
    private widget.TextBox Nadi;
    private widget.TextBox NilaiAktifitas;
    private widget.TextBox NilaiBersuara;
    private widget.TextBox NilaiGizi1;
    private widget.TextBox NilaiGizi2;
    private widget.TextBox NilaiGiziTotal;
    private widget.TextBox NilaiKaki;
    private widget.ComboBox NilaiKepercayaan;
    private widget.TextBox NilaiMenangis;
    private widget.TextBox NilaiResiko10;
    private widget.TextBox NilaiResiko11;
    private widget.TextBox NilaiResiko12;
    private widget.TextBox NilaiResiko13;
    private widget.TextBox NilaiResiko7;
    private widget.TextBox NilaiResiko8;
    private widget.TextBox NilaiResiko9;
    private widget.TextBox NilaiResikoTotal1;
    private widget.TextBox NilaiWajah;
    private widget.TextBox NmDPJP;
    private widget.TextBox NmPetugas;
    private widget.TextBox NmPetugas2;
    private widget.PanelBiasa PanelAccor;
    private widget.TextBox PekerjaanPasien;
    private widget.ComboBox PendidikanPJ;
    private widget.TextBox PendidikanPasien;
    private widget.ComboBox PolaAktifitasBerpakaian;
    private widget.ComboBox PolaAktifitasBerpindah;
    private widget.ComboBox PolaAktifitasEliminasi;
    private widget.ComboBox PolaAktifitasMakan;
    private widget.ComboBox PolaAktifitasMandi;
    private widget.TextBox PolaNutrisiFrekuensi;
    private widget.TextBox PolaNutrisiJenis;
    private widget.TextBox PolaNutrisiPorsi;
    private widget.ComboBox PolaTidurGangguan;
    private widget.TextBox PolaTidurLama;
    private widget.TextBox RDirawatRS;
    private widget.TextArea RPD;
    private widget.TextArea RPK;
    private widget.TextArea RPO;
    private widget.TextArea RPS;
    private widget.TextBox RPembedahan;
    private widget.ComboBox RPerkmAnk;
    private widget.TextBox RR;
    private widget.TextBox RTranfusi;
    private widget.TextArea Rencana;
    private widget.TextArea RiImunisasi;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll6;
    private widget.ScrollPane Scroll7;
    private widget.ScrollPane Scroll8;
    private widget.ScrollPane Scroll9;
    private widget.ComboBox SkalaAktifitas;
    private widget.ComboBox SkalaBersuara;
    private widget.ComboBox SkalaGizi1;
    private widget.ComboBox SkalaGizi2;
    private widget.ComboBox SkalaKaki;
    private widget.ComboBox SkalaMenangis;
    private widget.TextBox SkalaNyeri1;
    private widget.ComboBox SkalaResiko10;
    private widget.ComboBox SkalaResiko11;
    private widget.ComboBox SkalaResiko12;
    private widget.ComboBox SkalaResiko13;
    private widget.ComboBox SkalaResiko7;
    private widget.ComboBox SkalaResiko8;
    private widget.ComboBox SkalaResiko9;
    private widget.ComboBox SkalaWajah;
    private widget.TextBox SpO2;
    private widget.TextBox Suhu;
    private widget.TextBox TB;
    private widget.TextBox TCari;
    private widget.TextBox TCariMasalah;
    private widget.TextBox TCariRencana;
    private widget.TextBox TD;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRM1;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.TextBox TPasien1;
    private javax.swing.JTabbedPane TabRawat;
    private javax.swing.JTabbedPane TabRencanaKeperawatan;
    private widget.Tanggal TglAsuhan;
    private widget.TextBox TglLahir;
    private widget.ComboBox TibadiRuang;
    private widget.ComboBox TinggalDengan;
    private widget.Label TingkatResiko1;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.Label jLabel10;
    private widget.Label jLabel11;
    private widget.Label jLabel12;
    private widget.Label jLabel121;
    private widget.Label jLabel122;
    private widget.Label jLabel123;
    private widget.Label jLabel13;
    private widget.Label jLabel130;
    private widget.Label jLabel15;
    private widget.Label jLabel16;
    private widget.Label jLabel164;
    private widget.Label jLabel165;
    private widget.Label jLabel166;
    private widget.Label jLabel167;
    private widget.Label jLabel168;
    private widget.Label jLabel169;
    private widget.Label jLabel17;
    private widget.Label jLabel170;
    private widget.Label jLabel171;
    private widget.Label jLabel172;
    private widget.Label jLabel173;
    private widget.Label jLabel174;
    private widget.Label jLabel175;
    private widget.Label jLabel176;
    private widget.Label jLabel177;
    private widget.Label jLabel178;
    private widget.Label jLabel179;
    private widget.Label jLabel18;
    private widget.Label jLabel180;
    private widget.Label jLabel181;
    private widget.Label jLabel182;
    private widget.Label jLabel183;
    private widget.Label jLabel184;
    private widget.Label jLabel185;
    private widget.Label jLabel186;
    private widget.Label jLabel187;
    private widget.Label jLabel188;
    private widget.Label jLabel189;
    private widget.Label jLabel19;
    private widget.Label jLabel190;
    private widget.Label jLabel191;
    private widget.Label jLabel192;
    private widget.Label jLabel193;
    private widget.Label jLabel194;
    private widget.Label jLabel195;
    private widget.Label jLabel196;
    private widget.Label jLabel197;
    private widget.Label jLabel198;
    private widget.Label jLabel199;
    private widget.Label jLabel20;
    private widget.Label jLabel200;
    private widget.Label jLabel201;
    private widget.Label jLabel202;
    private widget.Label jLabel203;
    private widget.Label jLabel21;
    private widget.Label jLabel22;
    private widget.Label jLabel23;
    private widget.Label jLabel24;
    private widget.Label jLabel25;
    private widget.Label jLabel26;
    private widget.Label jLabel271;
    private widget.Label jLabel272;
    private widget.Label jLabel273;
    private widget.Label jLabel274;
    private widget.Label jLabel275;
    private widget.Label jLabel276;
    private widget.Label jLabel277;
    private widget.Label jLabel278;
    private widget.Label jLabel279;
    private widget.Label jLabel28;
    private widget.Label jLabel280;
    private widget.Label jLabel281;
    private widget.Label jLabel282;
    private widget.Label jLabel283;
    private widget.Label jLabel284;
    private widget.Label jLabel285;
    private widget.Label jLabel286;
    private widget.Label jLabel287;
    private widget.Label jLabel288;
    private widget.Label jLabel289;
    private widget.Label jLabel29;
    private widget.Label jLabel290;
    private widget.Label jLabel291;
    private widget.Label jLabel292;
    private widget.Label jLabel293;
    private widget.Label jLabel294;
    private widget.Label jLabel295;
    private widget.Label jLabel296;
    private widget.Label jLabel297;
    private widget.Label jLabel298;
    private widget.Label jLabel30;
    private widget.Label jLabel31;
    private widget.Label jLabel32;
    private widget.Label jLabel33;
    private widget.Label jLabel34;
    private widget.Label jLabel36;
    private widget.Label jLabel37;
    private widget.Label jLabel38;
    private widget.Label jLabel39;
    private widget.Label jLabel40;
    private widget.Label jLabel41;
    private widget.Label jLabel42;
    private widget.Label jLabel43;
    private widget.Label jLabel45;
    private widget.Label jLabel46;
    private widget.Label jLabel47;
    private widget.Label jLabel48;
    private widget.Label jLabel49;
    private widget.Label jLabel54;
    private widget.Label jLabel55;
    private widget.Label jLabel56;
    private widget.Label jLabel59;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private widget.Label jLabel9;
    private widget.Label jLabel94;
    private widget.Label jLabel95;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator8;
    private widget.Label label11;
    private widget.Label label12;
    private widget.Label label13;
    private widget.Label label14;
    private widget.Label label15;
    private widget.Label label16;
    private widget.PanelBiasa panelBiasa1;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane scrollInput;
    private widget.ScrollPane scrollPane1;
    private widget.ScrollPane scrollPane2;
    private widget.ScrollPane scrollPane3;
    private widget.ScrollPane scrollPane4;
    private widget.ScrollPane scrollPane5;
    private widget.ScrollPane scrollPane6;
    private widget.ScrollPane scrollPane7;
    private widget.ScrollPane scrollPane8;
    private widget.Table tbMasalahDetail;
    private widget.Table tbMasalahKeperawatan;
    private widget.Table tbObat;
    private widget.Table tbRencanaDetail;
    private widget.Table tbRencanaKeperawatan;
    private widget.Label totatskalanyeri;
    // End of variables declaration//GEN-END:variables

    private void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            ps=koneksi.prepareStatement(
                "select penilaian_awal_keperawatan_ranap_anak.no_rawat,penilaian_awal_keperawatan_ranap_anak.tanggal,penilaian_awal_keperawatan_ranap_anak.informasi,penilaian_awal_keperawatan_ranap_anak.ket_informasi,penilaian_awal_keperawatan_ranap_anak.tiba_diruang_rawat,"+
                "penilaian_awal_keperawatan_ranap_anak.kasus_trauma,penilaian_awal_keperawatan_ranap_anak.cara_masuk,penilaian_awal_keperawatan_ranap_anak.kel_utama,penilaian_awal_keperawatan_ranap_anak.rps,penilaian_awal_keperawatan_ranap_anak.rpd,penilaian_awal_keperawatan_ranap_anak.rpk,penilaian_awal_keperawatan_ranap_anak.rpo,"+
                "penilaian_awal_keperawatan_ranap_anak.riwayat_pembedahan,penilaian_awal_keperawatan_ranap_anak.riwayat_dirawat_dirs,penilaian_awal_keperawatan_ranap_anak.alat_bantu_dipakai,"+
                "penilaian_awal_keperawatan_ranap_anak.riwayat_tranfusi,penilaian_awal_keperawatan_ranap_anak.riwayat_imunisasi,penilaian_awal_keperawatan_ranap_anak.riwayat_prkmbngan,penilaian_awal_keperawatan_ranap_anak.riwayat_prkmbngan_lainnya,penilaian_awal_keperawatan_ranap_anak.riwayat_alergi,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_mental,"+
                "penilaian_awal_keperawatan_ranap_anak.pemeriksaan_keadaan_umum,"+
                "penilaian_awal_keperawatan_ranap_anak.pemeriksaan_gcs,"+
                "penilaian_awal_keperawatan_ranap_anak.pemeriksaan_td,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_nadi,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_rr,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_suhu,"+
                "penilaian_awal_keperawatan_ranap_anak.pemeriksaan_spo2,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_bb,penilaian_awal_keperawatan_ranap_anak.pemeriksaan_tb,"+
                "penilaian_awal_keperawatan_ranap_anak.pola_aktifitas_makanminum,penilaian_awal_keperawatan_ranap_anak.pola_aktifitas_mandi,penilaian_awal_keperawatan_ranap_anak.pola_aktifitas_eliminasi,penilaian_awal_keperawatan_ranap_anak.pola_aktifitas_berpakaian,"+
                "penilaian_awal_keperawatan_ranap_anak.pola_aktifitas_berpindah,penilaian_awal_keperawatan_ranap_anak.pola_nutrisi_frekuesi_makan,penilaian_awal_keperawatan_ranap_anak.pola_nutrisi_jenis_makanan,penilaian_awal_keperawatan_ranap_anak.pola_nutrisi_porsi_makan,"+
                "penilaian_awal_keperawatan_ranap_anak.pola_tidur_lama_tidur,penilaian_awal_keperawatan_ranap_anak.pola_tidur_gangguan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_kemampuan_sehari,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_aktifitas,"+
                "penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_berjalan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_berjalan_keterangan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_ambulasi,"+
                "penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_ekstrimitas_atas,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_ekstrimitas_atas_keterangan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_ekstrimitas_bawah,"+
                "penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_ekstrimitas_bawah_keterangan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_menggenggam,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_menggenggam_keterangan,"+
                "penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_koordinasi,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_koordinasi_keterangan,penilaian_awal_keperawatan_ranap_anak.pengkajian_fungsi_kesimpulan,"+
                "penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_kondisi_psiko,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_gangguan_jiwa,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_perilaku,"+
                "penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_perilaku_keterangan,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_hubungan_keluarga,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_tinggal,"+
                "penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_tinggal_keterangan,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_nilai_kepercayaan,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_nilai_kepercayaan_keterangan,"+
                "penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_pendidikan_pj,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_edukasi_diberikan,penilaian_awal_keperawatan_ranap_anak.riwayat_psiko_edukasi_diberikan_keterangan,"+
                "penilaian_awal_keperawatan_ranap_anak.wajah,penilaian_awal_keperawatan_ranap_anak.nilaiwajah,penilaian_awal_keperawatan_ranap_anak.kaki,"+
                "penilaian_awal_keperawatan_ranap_anak.nilaikaki,penilaian_awal_keperawatan_ranap_anak.aktifitas,penilaian_awal_keperawatan_ranap_anak.nilaiaktifitas,penilaian_awal_keperawatan_ranap_anak.menangis,penilaian_awal_keperawatan_ranap_anak.nilaimenangis,"+
                "penilaian_awal_keperawatan_ranap_anak.bersuara,penilaian_awal_keperawatan_ranap_anak.nilaibersuara,penilaian_awal_keperawatan_ranap_anak.hasilnyeri,"+
                "penilaian_awal_keperawatan_ranap_anak.skrining_gizi1,penilaian_awal_keperawatan_ranap_anak.nilai_gizi1,penilaian_awal_keperawatan_ranap_anak.skrining_gizi2,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala1,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai1,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala2,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai2,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala3,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai3,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala4,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai4,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala5,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai5,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala6,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai6,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_skala7,penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_nilai7,"+
                "penilaian_awal_keperawatan_ranap_anak.penilaian_humptydumpty_totalnilai,"+       
                "penilaian_awal_keperawatan_ranap_anak.nilai_gizi2,penilaian_awal_keperawatan_ranap_anak.nilai_total_gizi,penilaian_awal_keperawatan_ranap_anak.skrining_gizi_diagnosa_khusus,penilaian_awal_keperawatan_ranap_anak.skrining_gizi_ket_diagnosa_khusus,"+
                "penilaian_awal_keperawatan_ranap_anak.skrining_gizi_diketahui_dietisen,penilaian_awal_keperawatan_ranap_anak.skrining_gizi_jam_diketahui_dietisen,penilaian_awal_keperawatan_ranap_anak.rencana,penilaian_awal_keperawatan_ranap_anak.nip1,"+
                "penilaian_awal_keperawatan_ranap_anak.nip2,penilaian_awal_keperawatan_ranap_anak.kd_dokter,pasien.tgl_lahir,pasien.jk,pengkaji1.nama as pengkaji1,pengkaji2.nama as pengkaji2,dokter.nm_dokter,"+
                "reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.agama,pasien.pekerjaan,pasien.pnd,penjab.png_jawab,bahasa_pasien.nama_bahasa "+
                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                "inner join penilaian_awal_keperawatan_ranap_anak on reg_periksa.no_rawat=penilaian_awal_keperawatan_ranap_anak.no_rawat "+
                "inner join petugas as pengkaji1 on penilaian_awal_keperawatan_ranap_anak.nip1=pengkaji1.nip "+
                "inner join petugas as pengkaji2 on penilaian_awal_keperawatan_ranap_anak.nip2=pengkaji2.nip "+
                "inner join dokter on penilaian_awal_keperawatan_ranap_anak.kd_dokter=dokter.kd_dokter "+
                "inner join bahasa_pasien on bahasa_pasien.id=pasien.bahasa_pasien "+
                "inner join penjab on penjab.kd_pj=reg_periksa.kd_pj where "+
                "penilaian_awal_keperawatan_ranap_anak.tanggal between ? and ? "+
                (TCari.getText().trim().equals("")?"":"and (reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or penilaian_awal_keperawatan_ranap_anak.nip1 like ? or "+
                "pengkaji1.nama like ? or penilaian_awal_keperawatan_ranap_anak.kd_dokter like ? or dokter.nm_dokter like ?)")+
                " order by penilaian_awal_keperawatan_ranap_anak.tanggal");
            
            try {
                ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                if(!TCari.getText().equals("")){
                    ps.setString(3,"%"+TCari.getText()+"%");
                    ps.setString(4,"%"+TCari.getText()+"%");
                    ps.setString(5,"%"+TCari.getText()+"%");
                    ps.setString(6,"%"+TCari.getText()+"%");
                    ps.setString(7,"%"+TCari.getText()+"%");
                    ps.setString(8,"%"+TCari.getText()+"%");
                    ps.setString(9,"%"+TCari.getText()+"%");
                }   
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new String[]{
                        rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),rs.getString("tgl_lahir"),rs.getString("jk"),rs.getString("nip1"),rs.getString("pengkaji1"),rs.getString("nip2"),rs.getString("pengkaji2"),
                        rs.getString("kd_dokter"),rs.getString("nm_dokter"),rs.getString("tanggal"),rs.getString("kasus_trauma"),rs.getString("informasi")+", "+rs.getString("ket_informasi"),rs.getString("tiba_diruang_rawat"),rs.getString("cara_masuk"),
                        rs.getString("kel_utama"),rs.getString("rps"),rs.getString("rpd"),rs.getString("rpk"),rs.getString("rpo"),rs.getString("riwayat_pembedahan"),rs.getString("riwayat_dirawat_dirs"),rs.getString("alat_bantu_dipakai"),
                        rs.getString("riwayat_tranfusi"),rs.getString("riwayat_imunisasi"),rs.getString("riwayat_prkmbngan")+", "+rs.getString("riwayat_prkmbngan_lainnya"),rs.getString("riwayat_alergi"),
                        rs.getString("pemeriksaan_mental"),rs.getString("pemeriksaan_keadaan_umum"),
                        rs.getString("pemeriksaan_gcs"),rs.getString("pemeriksaan_td"),rs.getString("pemeriksaan_nadi"),rs.getString("pemeriksaan_rr"),rs.getString("pemeriksaan_suhu"),rs.getString("pemeriksaan_spo2"),rs.getString("pemeriksaan_bb"),
                        rs.getString("pemeriksaan_tb"),rs.getString("pola_aktifitas_mandi"),
                        rs.getString("pola_aktifitas_makanminum"),rs.getString("pola_aktifitas_berpakaian"),rs.getString("pola_aktifitas_eliminasi"),rs.getString("pola_aktifitas_berpindah"),rs.getString("pola_nutrisi_porsi_makan"),
                        rs.getString("pola_nutrisi_frekuesi_makan"),rs.getString("pola_nutrisi_jenis_makanan"),rs.getString("pola_tidur_lama_tidur"),rs.getString("pola_tidur_gangguan"),rs.getString("pengkajian_fungsi_kemampuan_sehari"),
                        rs.getString("pengkajian_fungsi_berjalan")+", "+rs.getString("pengkajian_fungsi_berjalan_keterangan"),rs.getString("pengkajian_fungsi_aktifitas"),rs.getString("pengkajian_fungsi_ambulasi"),
                        rs.getString("pengkajian_fungsi_ekstrimitas_atas")+", "+rs.getString("pengkajian_fungsi_ekstrimitas_atas_keterangan"),rs.getString("pengkajian_fungsi_ekstrimitas_bawah")+", "+rs.getString("pengkajian_fungsi_ekstrimitas_bawah_keterangan"),
                        rs.getString("pengkajian_fungsi_menggenggam")+", "+rs.getString("pengkajian_fungsi_menggenggam_keterangan"),rs.getString("pengkajian_fungsi_koordinasi")+", "+rs.getString("pengkajian_fungsi_koordinasi_keterangan"),
                        rs.getString("pengkajian_fungsi_kesimpulan"),rs.getString("riwayat_psiko_kondisi_psiko"),rs.getString("riwayat_psiko_perilaku")+", "+rs.getString("riwayat_psiko_perilaku_keterangan"),rs.getString("riwayat_psiko_gangguan_jiwa"),
                        rs.getString("riwayat_psiko_hubungan_keluarga"),rs.getString("agama"),rs.getString("riwayat_psiko_tinggal")+", "+rs.getString("riwayat_psiko_tinggal_keterangan"),rs.getString("pekerjaan"),rs.getString("png_jawab"),
                        rs.getString("riwayat_psiko_nilai_kepercayaan")+", "+rs.getString("riwayat_psiko_nilai_kepercayaan_keterangan"),rs.getString("nama_bahasa"),rs.getString("pnd"),rs.getString("riwayat_psiko_pendidikan_pj"),
                        rs.getString("riwayat_psiko_edukasi_diberikan")+", "+rs.getString("riwayat_psiko_edukasi_diberikan_keterangan"),rs.getString("wajah"),rs.getString("nilaiwajah"),rs.getString("kaki"),rs.getString("nilaikaki"),rs.getString("aktifitas"),rs.getString("nilaiaktifitas"),rs.getString("menangis"),
                        rs.getString("nilaimenangis"),rs.getString("bersuara"),rs.getString("nilaibersuara"),rs.getString("hasilnyeri"),
                        rs.getString("penilaian_humptydumpty_skala1"),rs.getString("penilaian_humptydumpty_nilai1"),rs.getString("penilaian_humptydumpty_skala2"),rs.getString("penilaian_humptydumpty_nilai2"),
                        rs.getString("penilaian_humptydumpty_skala3"),rs.getString("penilaian_humptydumpty_nilai3"),rs.getString("penilaian_humptydumpty_skala4"),rs.getString("penilaian_humptydumpty_nilai4"),rs.getString("penilaian_humptydumpty_skala5"),
                        rs.getString("penilaian_humptydumpty_nilai5"),rs.getString("penilaian_humptydumpty_skala6"),rs.getString("penilaian_humptydumpty_nilai6"),rs.getString("penilaian_humptydumpty_skala7"),rs.getString("penilaian_humptydumpty_nilai7"),
                        rs.getString("penilaian_humptydumpty_totalnilai"),rs.getString("skrining_gizi1"),rs.getString("nilai_gizi1"),rs.getString("skrining_gizi2"),rs.getString("nilai_gizi2"),rs.getString("nilai_total_gizi"),
                        rs.getString("skrining_gizi_diagnosa_khusus"),rs.getString("skrining_gizi_ket_diagnosa_khusus"),rs.getString("skrining_gizi_diketahui_dietisen"),rs.getString("skrining_gizi_jam_diketahui_dietisen"),
                        rs.getString("rencana")
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

    public void emptTeks() {
        TglAsuhan.setDate(new Date());
        Anamnesis.setSelectedIndex(0);
        KetAnamnesis.setText("");
        TibadiRuang.setSelectedIndex(0);
        CaraMasuk.setSelectedIndex(0);
        MacamKasus.setSelectedIndex(0);
        KelUtama.setText("");
        RPS.setText("");
        RPD.setText("");
        RPK.setText("");
        RPO.setText("");
        RPembedahan.setText("");
        RDirawatRS.setText("");
        AlatBantuDipakai.setSelectedIndex(0);
        RPerkmAnk.setSelectedIndex(0);
        KetPerkmAnk.setText("");
        RTranfusi.setText("");
        Alergi.setText("");
        RiImunisasi.setText("");
        KesadaranMental.setText("");
        KeadaanMentalUmum.setSelectedIndex(0);
        GCS.setText("");
        TD.setText("");
        Nadi.setText("");
        RR.setText("");
        Suhu.setText("");
        SpO2.setText("");
        BB.setText("");
        TB.setText("");
        SkalaWajah.setSelectedIndex(0);
        NilaiWajah.setText("0");
        SkalaKaki.setSelectedIndex(0);
        NilaiKaki.setText("0");
        SkalaAktifitas.setSelectedIndex(0);
        NilaiAktifitas.setText("0");
        SkalaMenangis.setSelectedIndex(0);
        NilaiMenangis.setText("0");
        SkalaBersuara.setSelectedIndex(0);
        NilaiBersuara.setText("0");
        SkalaNyeri1.setText("0");
        PolaAktifitasMandi.setSelectedIndex(0);
        PolaAktifitasMakan.setSelectedIndex(0);
        PolaAktifitasBerpakaian.setSelectedIndex(0);
        PolaAktifitasEliminasi.setSelectedIndex(0);
        PolaAktifitasBerpindah.setSelectedIndex(0);
        PolaNutrisiPorsi.setText("");
        PolaNutrisiFrekuensi.setText("");
        PolaNutrisiJenis.setText("");
        PolaTidurLama.setText("");
        PolaTidurGangguan.setSelectedIndex(0);
        AktifitasSehari2.setSelectedIndex(0);
        Berjalan.setSelectedIndex(0);
        KeteranganBerjalan.setText("");
        Aktifitas.setSelectedIndex(0);
        AlatAmbulasi.setSelectedIndex(0);
        EkstrimitasAtas.setSelectedIndex(0);
        KeteranganEkstrimitasAtas.setText("");
        EkstrimitasBawah.setSelectedIndex(0);
        KeteranganEkstrimitasBawah.setText("");
        KemampuanMenggenggam.setSelectedIndex(0);
        KeteranganKemampuanMenggenggam.setText("");
        KemampuanKoordinasi.setSelectedIndex(0);
        KeteranganKemampuanKoordinasi.setText("");
        KesimpulanGangguanFungsi.setSelectedIndex(0);
        KondisiPsikologis.setSelectedIndex(0);
        AdakahPerilaku.setSelectedIndex(0);
        KeteranganAdakahPerilaku.setText("");
        GangguanJiwa.setSelectedIndex(0);
        HubunganAnggotaKeluarga.setSelectedIndex(0);
        TinggalDengan.setSelectedIndex(0);
        KeteranganTinggalDengan.setText("");
        NilaiKepercayaan.setSelectedIndex(0);
        KeteranganNilaiKepercayaan.setText("");
        PendidikanPJ.setSelectedIndex(0);
        EdukasiPsikolgis.setSelectedIndex(0);
        KeteranganEdukasiPsikologis.setText("");
        SkalaResiko7.setSelectedIndex(0);
        NilaiResiko7.setText("0");
        SkalaResiko8.setSelectedIndex(0);
        NilaiResiko8.setText("0");
        SkalaResiko9.setSelectedIndex(0);
        NilaiResiko9.setText("0");
        SkalaResiko10.setSelectedIndex(0);
        NilaiResiko10.setText("0");
        SkalaResiko11.setSelectedIndex(0);
        NilaiResiko11.setText("0");
        SkalaResiko12.setSelectedIndex(0);
        NilaiResiko12.setText("0");
        SkalaResiko13.setSelectedIndex(0);
        NilaiResiko13.setText("0");
        NilaiResikoTotal1.setText("0");
        SkalaGizi1.setSelectedIndex(0);
        NilaiGizi1.setText("0");
        SkalaGizi2.setSelectedIndex(0);
        NilaiGizi2.setText("0");
        NilaiGiziTotal.setText("0");
        DiagnosaKhususGizi.setSelectedIndex(0);
        KeteranganDiagnosaKhususGizi.setText("");
        DiketahuiDietisen.setSelectedIndex(0);
        KeteranganDiketahuiDietisen.setText("");
        Rencana.setText("");
        for (i = 0; i < tabModeMasalah.getRowCount(); i++) {
            tabModeMasalah.setValueAt(false,i,0);
        }
        Valid.tabelKosong(tabModeRencana);
        TabRawat.setSelectedIndex(0);
        MacamKasus.requestFocus();
    } 

    private void getData() {
        if(tbObat.getSelectedRow()!= -1){
            TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()); 
            TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString()); 
            TglLahir.setText(tbObat.getValueAt(tbObat.getSelectedRow(),3).toString()); 
            Jk.setText(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString()); 
            KdPetugas2.setText(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString()); 
            NmPetugas2.setText(tbObat.getValueAt(tbObat.getSelectedRow(),8).toString()); 
            KdDPJP.setText(tbObat.getValueAt(tbObat.getSelectedRow(),9).toString()); 
            NmDPJP.setText(tbObat.getValueAt(tbObat.getSelectedRow(),10).toString()); 
            MacamKasus.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),12).toString()); 
            if(tbObat.getValueAt(tbObat.getSelectedRow(),13).toString().contains("Autoanamnesis")){
                Anamnesis.setSelectedItem("Autoanamnesis");
            }else{
                Anamnesis.setSelectedItem("Alloanamnesis");
            }
            KetAnamnesis.setText(tbObat.getValueAt(tbObat.getSelectedRow(),13).toString().replaceAll(Anamnesis.getSelectedItem().toString()+", ",""));
            TibadiRuang.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),14).toString()); 
            CaraMasuk.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),15).toString()); 
            KelUtama.setText(tbObat.getValueAt(tbObat.getSelectedRow(),16).toString());
            RPS.setText(tbObat.getValueAt(tbObat.getSelectedRow(),17).toString()); 
            RPD.setText(tbObat.getValueAt(tbObat.getSelectedRow(),18).toString()); 
            RPK.setText(tbObat.getValueAt(tbObat.getSelectedRow(),19).toString()); 
            RPO.setText(tbObat.getValueAt(tbObat.getSelectedRow(),20).toString()); 
            RPembedahan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),21).toString()); 
            RDirawatRS.setText(tbObat.getValueAt(tbObat.getSelectedRow(),22).toString()); 
            AlatBantuDipakai.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),23).toString()); 
            
            RTranfusi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),24).toString());
            RiImunisasi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),25).toString());
            if(tbObat.getValueAt(tbObat.getSelectedRow(),26).toString().contains("Ya")){
                RPerkmAnk.setSelectedItem("Cepat");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),26).toString().contains("Lambat")){
                RPerkmAnk.setSelectedItem("Lambat");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),26).toString().contains("Sama")){
                RPerkmAnk.setSelectedItem("Sama");
            }else{
                RPerkmAnk.setSelectedItem("Lainnya");
            }
            KetPerkmAnk.setText(tbObat.getValueAt(tbObat.getSelectedRow(),26).toString().replaceAll(RPerkmAnk.getSelectedItem().toString()+", ",""));
            Alergi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),27).toString()); 
            
            KesadaranMental.setText(tbObat.getValueAt(tbObat.getSelectedRow(),28).toString());  
            KeadaanMentalUmum.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),29).toString());   
            GCS.setText(tbObat.getValueAt(tbObat.getSelectedRow(),30).toString());  
            TD.setText(tbObat.getValueAt(tbObat.getSelectedRow(),31).toString());  
            Nadi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),32).toString());  
            RR.setText(tbObat.getValueAt(tbObat.getSelectedRow(),33).toString());  
            Suhu.setText(tbObat.getValueAt(tbObat.getSelectedRow(),34).toString());  
            SpO2.setText(tbObat.getValueAt(tbObat.getSelectedRow(),35).toString());  
            BB.setText(tbObat.getValueAt(tbObat.getSelectedRow(),36).toString());  
            TB.setText(tbObat.getValueAt(tbObat.getSelectedRow(),37).toString());       
            PolaAktifitasMandi.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),38).toString());
            PolaAktifitasMakan.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),39).toString());
            PolaAktifitasBerpakaian.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),40).toString());
            PolaAktifitasEliminasi.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),41).toString());
            PolaAktifitasBerpindah.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),42).toString());
            PolaNutrisiPorsi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),43).toString());
            PolaNutrisiFrekuensi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),44).toString());
            PolaNutrisiJenis.setText(tbObat.getValueAt(tbObat.getSelectedRow(),45).toString());
            PolaTidurLama.setText(tbObat.getValueAt(tbObat.getSelectedRow(),46).toString());
            PolaTidurGangguan.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),47).toString());
            AktifitasSehari2.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),48).toString());
            if(tbObat.getValueAt(tbObat.getSelectedRow(),49).toString().contains("TAK")){
                Berjalan.setSelectedItem("TAK");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),49).toString().contains("Penurunan Kekuatan/ROM")){
                Berjalan.setSelectedItem("Penurunan Kekuatan/ROM");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),49).toString().contains("Paralisis")){
                Berjalan.setSelectedItem("Paralisis");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),49).toString().contains("Sering Jatuh")){
                Berjalan.setSelectedItem("Sering Jatuh");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),49).toString().contains("Deformitas")){
                Berjalan.setSelectedItem("Deformitas");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),49).toString().contains("Hilang keseimbangan")){
                Berjalan.setSelectedItem("Hilang keseimbangan");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),49).toString().contains("Riwayat Patah Tulang")){
                Berjalan.setSelectedItem("Riwayat Patah Tulang");
            }else{
                Berjalan.setSelectedItem("Lain-lain");
            }
            KeteranganBerjalan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),49).toString().replaceAll(Berjalan.getSelectedItem().toString()+", ",""));
            Aktifitas.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),50).toString());
            AlatAmbulasi.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),51).toString());
            if(tbObat.getValueAt(tbObat.getSelectedRow(),52).toString().contains("TAK")){
                EkstrimitasAtas.setSelectedItem("TAK");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),52).toString().contains("Lemah")){
                EkstrimitasAtas.setSelectedItem("Lemah");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),52).toString().contains("Oedema")){
                EkstrimitasAtas.setSelectedItem("Oedema");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),52).toString().contains("Tidak Simetris")){
                EkstrimitasAtas.setSelectedItem("Tidak Simetris");
            }else{
                EkstrimitasAtas.setSelectedItem("Lain-lain");
            }
            KeteranganEkstrimitasAtas.setText(tbObat.getValueAt(tbObat.getSelectedRow(),52).toString().replaceAll(EkstrimitasAtas.getSelectedItem().toString()+", ",""));
            if(tbObat.getValueAt(tbObat.getSelectedRow(),53).toString().contains("TAK")){
                EkstrimitasBawah.setSelectedItem("TAK");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),53).toString().contains("Varises")){
                EkstrimitasBawah.setSelectedItem("Varises");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),53).toString().contains("Oedema")){
                EkstrimitasBawah.setSelectedItem("Oedema");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),53).toString().contains("Tidak Simetris")){
                EkstrimitasBawah.setSelectedItem("Tidak Simetris");
            }else{
                EkstrimitasBawah.setSelectedItem("Lain-lain");
            }
            KeteranganEkstrimitasBawah.setText(tbObat.getValueAt(tbObat.getSelectedRow(),53).toString().replaceAll(EkstrimitasBawah.getSelectedItem().toString()+", ",""));
            if(tbObat.getValueAt(tbObat.getSelectedRow(),54).toString().contains("Tidak Ada Kesulitan")){
                KemampuanMenggenggam.setSelectedItem("Tidak Ada Kesulitan");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),54).toString().contains("Terakhir")){
                KemampuanMenggenggam.setSelectedItem("Terakhir");
            }else{
                KemampuanMenggenggam.setSelectedItem("Lain-lain");
            }
            KeteranganKemampuanMenggenggam.setText(tbObat.getValueAt(tbObat.getSelectedRow(),54).toString().replaceAll(KemampuanMenggenggam.getSelectedItem().toString()+", ",""));
            if(tbObat.getValueAt(tbObat.getSelectedRow(),55).toString().contains("Tidak Ada Kesulitan")){
                KemampuanKoordinasi.setSelectedItem("Tidak Ada Kesulitan");
            }else{
                KemampuanKoordinasi.setSelectedItem("Ada Masalah");
            }
            KeteranganKemampuanKoordinasi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),55).toString().replaceAll(KemampuanKoordinasi.getSelectedItem().toString()+", ",""));
            KesimpulanGangguanFungsi.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),56).toString());
            KondisiPsikologis.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),57).toString());
            if(tbObat.getValueAt(tbObat.getSelectedRow(),58).toString().contains("Tidak Ada Masalah")){
                AdakahPerilaku.setSelectedItem("Tidak Ada Masalah");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),58).toString().contains("Perilaku Kekerasan")){
                AdakahPerilaku.setSelectedItem("Perilaku Kekerasan");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),58).toString().contains("Gangguan Efek")){
                AdakahPerilaku.setSelectedItem("Gangguan Efek");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),58).toString().contains("Gangguan Memori")){
                AdakahPerilaku.setSelectedItem("Gangguan Memori");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),58).toString().contains("Halusinasi")){
                AdakahPerilaku.setSelectedItem("Halusinasi");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),58).toString().contains("Kecenderungan Percobaan Bunuh Diri")){
                AdakahPerilaku.setSelectedItem("Kecenderungan Percobaan Bunuh Diri");
            }else{
                AdakahPerilaku.setSelectedItem("Lain-lain");
            }
            KeteranganAdakahPerilaku.setText(tbObat.getValueAt(tbObat.getSelectedRow(),58).toString().replaceAll(AdakahPerilaku.getSelectedItem().toString()+", ",""));
            GangguanJiwa.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),59).toString());
            HubunganAnggotaKeluarga.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),60).toString());
            Agama.setText(tbObat.getValueAt(tbObat.getSelectedRow(),61).toString());
            if(tbObat.getValueAt(tbObat.getSelectedRow(),62).toString().contains("Sendiri")){
                TinggalDengan.setSelectedItem("Sendiri");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),62).toString().contains("Orang Tua")){
                TinggalDengan.setSelectedItem("Orang Tua");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),62).toString().contains("Suami/Istri")){
                TinggalDengan.setSelectedItem("Suami/Istri");
            }else if(tbObat.getValueAt(tbObat.getSelectedRow(),62).toString().contains("Keluarga")){
                TinggalDengan.setSelectedItem("Keluarga");
            }else{
                TinggalDengan.setSelectedItem("Lain-lain");
            }
            KeteranganTinggalDengan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),62).toString().replaceAll(TinggalDengan.getSelectedItem().toString()+", ",""));
            PekerjaanPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(),63).toString());
            CaraBayar.setText(tbObat.getValueAt(tbObat.getSelectedRow(),64).toString());
            if(tbObat.getValueAt(tbObat.getSelectedRow(),65).toString().contains("Tidak Ada")){
                NilaiKepercayaan.setSelectedItem("Tidak Ada");
            }else{
                NilaiKepercayaan.setSelectedItem("Ada");
            }
            KeteranganNilaiKepercayaan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),65).toString().replaceAll(NilaiKepercayaan.getSelectedItem().toString()+", ",""));
            Bahasa.setText(tbObat.getValueAt(tbObat.getSelectedRow(),66).toString());
            PendidikanPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(),67).toString());
            PendidikanPJ.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),68).toString());
            if(tbObat.getValueAt(tbObat.getSelectedRow(),69).toString().contains("Pasien")){
                EdukasiPsikolgis.setSelectedItem("Pasien");
            }else{
                EdukasiPsikolgis.setSelectedItem("Keluarga");
            }
            KeteranganEdukasiPsikologis.setText(tbObat.getValueAt(tbObat.getSelectedRow(),69).toString().replaceAll(EdukasiPsikolgis.getSelectedItem().toString()+", ",""));
            SkalaWajah.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),70).toString());
            NilaiWajah.setText(tbObat.getValueAt(tbObat.getSelectedRow(),71).toString());
            SkalaKaki.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),72).toString());
            NilaiKaki.setText(tbObat.getValueAt(tbObat.getSelectedRow(),73).toString());
            SkalaAktifitas.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),74).toString());
            NilaiAktifitas.setText(tbObat.getValueAt(tbObat.getSelectedRow(),75).toString());
            SkalaMenangis.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),76).toString());
            NilaiMenangis.setText(tbObat.getValueAt(tbObat.getSelectedRow(),77).toString());
            SkalaBersuara.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),78).toString());
            NilaiBersuara.setText(tbObat.getValueAt(tbObat.getSelectedRow(),79).toString());
            SkalaNyeri1.setText(tbObat.getValueAt(tbObat.getSelectedRow(),80).toString());
            SkalaResiko7.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),81).toString());
            NilaiResiko7.setText(tbObat.getValueAt(tbObat.getSelectedRow(),82).toString());
            SkalaResiko8.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),83).toString());
            NilaiResiko8.setText(tbObat.getValueAt(tbObat.getSelectedRow(),84).toString());
            SkalaResiko9.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),85).toString());
            NilaiResiko9.setText(tbObat.getValueAt(tbObat.getSelectedRow(),86).toString());
            SkalaResiko10.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),87).toString());
            NilaiResiko10.setText(tbObat.getValueAt(tbObat.getSelectedRow(),88).toString());
            SkalaResiko11.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),89).toString());
            NilaiResiko11.setText(tbObat.getValueAt(tbObat.getSelectedRow(),90).toString());
            SkalaResiko12.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),91).toString());
            NilaiResiko12.setText(tbObat.getValueAt(tbObat.getSelectedRow(),92).toString());
            SkalaResiko13.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),93).toString());
            NilaiResiko13.setText(tbObat.getValueAt(tbObat.getSelectedRow(),94).toString());
            NilaiResikoTotal1.setText(tbObat.getValueAt(tbObat.getSelectedRow(),95).toString());
            isTotalResikoJatuh1();
            SkalaGizi1.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),96).toString());
            NilaiGizi1.setText(tbObat.getValueAt(tbObat.getSelectedRow(),97).toString());
            SkalaGizi2.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),98).toString());
            NilaiGizi2.setText(tbObat.getValueAt(tbObat.getSelectedRow(),99).toString());
            NilaiGiziTotal.setText(tbObat.getValueAt(tbObat.getSelectedRow(),100).toString());
            DiagnosaKhususGizi.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),101).toString());
            KeteranganDiagnosaKhususGizi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),102).toString());
            DiketahuiDietisen.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),103).toString());
            KeteranganDiketahuiDietisen.setText(tbObat.getValueAt(tbObat.getSelectedRow(),104).toString());
            Rencana.setText(tbObat.getValueAt(tbObat.getSelectedRow(),105).toString());
            
            try {
                Valid.tabelKosong(tabModeMasalah);
                
                ps=koneksi.prepareStatement(
                        "select master_masalah_keperawatan.kode_masalah,master_masalah_keperawatan.nama_masalah from master_masalah_keperawatan "+
                        "inner join penilaian_awal_keperawatan_ranap_masalah on penilaian_awal_keperawatan_ranap_masalah.kode_masalah=master_masalah_keperawatan.kode_masalah "+
                        "where penilaian_awal_keperawatan_ranap_masalah.no_rawat=? order by penilaian_awal_keperawatan_ranap_masalah.kode_masalah");
                try {
                    ps.setString(1,tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                    rs=ps.executeQuery();
                    while(rs.next()){
                        tabModeMasalah.addRow(new Object[]{true,rs.getString(1),rs.getString(2)});
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
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            }
            
            try {
                Valid.tabelKosong(tabModeRencana);
                
                ps=koneksi.prepareStatement(
                        "select master_rencana_keperawatan.kode_rencana,master_rencana_keperawatan.rencana_keperawatan from master_rencana_keperawatan "+
                        "inner join penilaian_awal_keperawatan_ranap_rencana on penilaian_awal_keperawatan_ranap_rencana.kode_rencana=master_rencana_keperawatan.kode_rencana "+
                        "where penilaian_awal_keperawatan_ranap_rencana.no_rawat=? order by penilaian_awal_keperawatan_ranap_rencana.kode_rencana");
                try {
                    ps.setString(1,tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                    rs=ps.executeQuery();
                    while(rs.next()){
                        tabModeRencana.addRow(new Object[]{true,rs.getString(1),rs.getString(2)});
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
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            }
            
            Valid.SetTgl2(TglAsuhan,tbObat.getValueAt(tbObat.getSelectedRow(),11).toString());
        }
    }

    private void isRawat() {
        try {
            ps=koneksi.prepareStatement(
                    "select pasien.nm_pasien, if(pasien.jk='L','Laki-Laki','Perempuan') as jk,pasien.tgl_lahir,pasien.agama,"+
                    "bahasa_pasien.nama_bahasa,pasien.pnd,pasien.pekerjaan "+
                    "from pasien inner join bahasa_pasien on bahasa_pasien.id=pasien.bahasa_pasien "+
                    "where pasien.no_rkm_medis=?");
            try {
                ps.setString(1,TNoRM.getText());
                rs=ps.executeQuery();
                if(rs.next()){
                    TPasien.setText(rs.getString("nm_pasien"));
                    Jk.setText(rs.getString("jk"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
                    Agama.setText(rs.getString("agama"));
                    Bahasa.setText(rs.getString("nama_bahasa"));
                    PendidikanPasien.setText(rs.getString("pnd"));
                    PekerjaanPasien.setText(rs.getString("pekerjaan"));
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
        } catch (Exception e) {
            System.out.println("Notif : "+e);
        }
    }
    
    public void setNoRm(String norwt, Date tgl2,String carabayar,String norm) {
        TNoRw.setText(norwt);
        TNoRM.setText(norm);
        TCari.setText(norwt);
        Sequel.cariIsi("select reg_periksa.tgl_registrasi from reg_periksa where reg_periksa.no_rawat='"+norwt+"'", DTPCari1);
        CaraBayar.setText(carabayar);
        DTPCari2.setDate(tgl2);
        isRawat();
        muatAnak(norwt);
    }
    
    
    public void isCek(){
        BtnSimpan.setEnabled(akses.getpenilaian_awal_keperawatan_ranap());
        BtnHapus.setEnabled(akses.getpenilaian_awal_keperawatan_ranap());
        BtnEdit.setEnabled(akses.getpenilaian_awal_keperawatan_ranap());
        BtnEdit.setEnabled(akses.getpenilaian_awal_keperawatan_ranap()); 
        BtnTambahMasalah.setEnabled(akses.getmaster_masalah_keperawatan()); 
        BtnTambahRencana.setEnabled(akses.getmaster_rencana_keperawatan()); 
        if(akses.getjml2()>=1){
            KdPetugas.setEditable(false);
            BtnPetugas.setEnabled(false);
            KdPetugas.setText(akses.getkode());
            NmPetugas.setText(petugas.tampil3(KdPetugas.getText()));
            if(NmPetugas.getText().equals("")){
                KdPetugas.setText("");
                JOptionPane.showMessageDialog(null,"User login bukan petugas...!!");
            }
        }            
    }

    public void setTampil(){
       TabRawat.setSelectedIndex(1);
    }
    
    private void isMenu(){
        if(ChkAccor.isSelected()==true){
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(470,HEIGHT));
            FormMenu.setVisible(true);  
            FormMasalahRencana.setVisible(true);
            ChkAccor.setVisible(true);
        }else if(ChkAccor.isSelected()==false){    
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(15,HEIGHT));
            FormMenu.setVisible(false);  
            FormMasalahRencana.setVisible(false);  
            ChkAccor.setVisible(true);
        }
    }

    private void getMasalah() {
        if(tbObat.getSelectedRow()!= -1){
            TNoRM1.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
            TPasien1.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
            DetailRencana.setText(tbObat.getValueAt(tbObat.getSelectedRow(),105).toString());
            
            try {
                Valid.tabelKosong(tabModeDetailMasalah);
                ps=koneksi.prepareStatement(
                        "select master_masalah_keperawatan.kode_masalah,master_masalah_keperawatan.nama_masalah from master_masalah_keperawatan "+
                        "inner join penilaian_awal_keperawatan_ranap_masalah on penilaian_awal_keperawatan_ranap_masalah.kode_masalah=master_masalah_keperawatan.kode_masalah "+
                        "where penilaian_awal_keperawatan_ranap_masalah.no_rawat=? order by penilaian_awal_keperawatan_ranap_masalah.kode_masalah");
                try {
                    ps.setString(1,tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                    rs=ps.executeQuery();
                    while(rs.next()){
                        tabModeDetailMasalah.addRow(new Object[]{rs.getString(1),rs.getString(2)});
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
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            }
            
            try {
                Valid.tabelKosong(tabModeDetailRencana);
                ps=koneksi.prepareStatement(
                        "select master_rencana_keperawatan.kode_rencana,master_rencana_keperawatan.rencana_keperawatan from master_rencana_keperawatan "+
                        "inner join penilaian_awal_keperawatan_ranap_rencana on penilaian_awal_keperawatan_ranap_rencana.kode_rencana=master_rencana_keperawatan.kode_rencana "+
                        "where penilaian_awal_keperawatan_ranap_rencana.no_rawat=? order by penilaian_awal_keperawatan_ranap_rencana.kode_rencana");
                try {
                    ps.setString(1,tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                    rs=ps.executeQuery();
                    while(rs.next()){
                        tabModeDetailRencana.addRow(new Object[]{rs.getString(1),rs.getString(2)});
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
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            }
        }
    }
   
    

   private void hapus() {
        if(Sequel.queryu2tf("delete from penilaian_awal_keperawatan_ranap_anak where no_rawat=?",1,new String[]{
            tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
        })==true){
            TNoRM1.setText("");
            TPasien1.setText("");
            ChkAccor.setSelected(false);
            isMenu();
            tabMode.removeRow(tbObat.getSelectedRow());
            LCount.setText(""+tabMode.getRowCount());
        }else{
            JOptionPane.showMessageDialog(null,"Gagal menghapus..!!");
        }
    }


    private void ganti() {
//        if(Sequel.mengedittf("humty_dumpty_keperawatan","no_rawat=?","penilaian_humptydumpty_skala1=?,penilaian_humptydumpty_nilai1=?,penilaian_humptydumpty_skala2=?,penilaian_humptydumpty_nilai2=?,penilaian_humptydumpty_skala3=?,penilaian_humptydumpty_nilai3=?,penilaian_humptydumpty_skala4=?,penilaian_humptydumpty_nilai4=?,penilaian_humptydumpty_skala5=?,penilaian_humptydumpty_nilai5=?,penilaian_humptydumpty_skala6=?,penilaian_humptydumpty_nilai6=?,penilaian_humptydumpty_skala7=?,penilaian_humptydumpty_nilai7=?,penilaian_humptydumpty_totalnilai=?",16, new String[]{
//            TNoRw.getText(),SkalaResiko7.getSelectedItem().toString(), NilaiResiko7.getText(), SkalaResiko8.getSelectedItem().toString(), NilaiResiko8.getText(),
//            SkalaResiko9.getSelectedItem().toString(), NilaiResiko9.getText(), SkalaResiko10.getSelectedItem().toString(), NilaiResiko10.getText(), 
//            SkalaResiko11.getSelectedItem().toString(), NilaiResiko11.getText(), SkalaResiko12.getSelectedItem().toString(), NilaiResiko12.getText(),
//            SkalaResiko13.getSelectedItem().toString(), NilaiResiko13.getText(), NilaiResikoTotal1.getText()
//        })==true)
        
        
        
        if(Sequel.mengedittf("penilaian_awal_keperawatan_ranap_anak","no_rawat=?","no_rawat=?,tanggal=?,informasi=?,ket_informasi=?,tiba_diruang_rawat=?,kasus_trauma=?,cara_masuk=?,kel_utama=?,rps=?,rpd=?,rpk=?,rpo=?,riwayat_pembedahan=?,riwayat_dirawat_dirs=?,alat_bantu_dipakai=?,riwayat_tranfusi=?,riwayat_imunisasi=?,riwayat_prkmbngan=?,riwayat_prkmbngan_lainnya=?,riwayat_alergi=?,pemeriksaan_mental=?,pemeriksaan_keadaan_umum=?,pemeriksaan_gcs=?,pemeriksaan_td=?,pemeriksaan_nadi=?,pemeriksaan_rr=?,pemeriksaan_suhu=?,pemeriksaan_spo2=?,pemeriksaan_bb=?,pemeriksaan_tb=?,pola_aktifitas_makanminum=?,pola_aktifitas_mandi=?,pola_aktifitas_eliminasi=?,pola_aktifitas_berpakaian=?,pola_aktifitas_berpindah=?,pola_nutrisi_frekuesi_makan=?,pola_nutrisi_jenis_makanan=?,pola_nutrisi_porsi_makan=?,pola_tidur_lama_tidur=?,pola_tidur_gangguan=?,pengkajian_fungsi_kemampuan_sehari=?,pengkajian_fungsi_aktifitas=?,pengkajian_fungsi_berjalan=?,pengkajian_fungsi_berjalan_keterangan=?,pengkajian_fungsi_ambulasi=?,pengkajian_fungsi_ekstrimitas_atas=?,pengkajian_fungsi_ekstrimitas_atas_keterangan=?,pengkajian_fungsi_ekstrimitas_bawah=?,pengkajian_fungsi_ekstrimitas_bawah_keterangan=?,pengkajian_fungsi_menggenggam=?,pengkajian_fungsi_menggenggam_keterangan=?,pengkajian_fungsi_koordinasi=?,pengkajian_fungsi_koordinasi_keterangan=?,pengkajian_fungsi_kesimpulan=?,riwayat_psiko_kondisi_psiko=?,riwayat_psiko_gangguan_jiwa=?,riwayat_psiko_perilaku=?,riwayat_psiko_perilaku_keterangan=?,riwayat_psiko_hubungan_keluarga=?,riwayat_psiko_tinggal=?,riwayat_psiko_tinggal_keterangan=?,riwayat_psiko_nilai_kepercayaan=?,riwayat_psiko_nilai_kepercayaan_keterangan=?,riwayat_psiko_pendidikan_pj=?,riwayat_psiko_edukasi_diberikan=?,riwayat_psiko_edukasi_diberikan_keterangan=?,wajah=?,nilaiwajah=?,kaki=?,nilaikaki=?,aktifitas=?,nilaiaktifitas=?,menangis=?,nilaimenangis=?,bersuara=?,nilaibersuara=?,hasilnyeri=?,penilaian_humptydumpty_skala1=?,penilaian_humptydumpty_nilai1=?,penilaian_humptydumpty_skala2=?,penilaian_humptydumpty_nilai2=?,penilaian_humptydumpty_skala3=?,penilaian_humptydumpty_nilai3=?,penilaian_humptydumpty_skala4=?,penilaian_humptydumpty_nilai4=?,penilaian_humptydumpty_skala5=?,penilaian_humptydumpty_nilai5=?,penilaian_humptydumpty_skala6=?,penilaian_humptydumpty_nilai6=?,penilaian_humptydumpty_skala7=?,penilaian_humptydumpty_nilai7=?,penilaian_humptydumpty_totalnilai=?,skrining_gizi1=?,nilai_gizi1=?,skrining_gizi2=?,nilai_gizi2=?,nilai_total_gizi=?,skrining_gizi_diagnosa_khusus=?,skrining_gizi_ket_diagnosa_khusus=?,skrining_gizi_diketahui_dietisen=?,skrining_gizi_jam_diketahui_dietisen=?,rencana=?,nip1=?,nip2=?,kd_dokter=?",106,new String[]{
                TNoRw.getText(),Valid.SetTgl(TglAsuhan.getSelectedItem()+"")+" "+TglAsuhan.getSelectedItem().toString().substring(11,19),Anamnesis.getSelectedItem().toString(),KetAnamnesis.getText(),TibadiRuang.getSelectedItem().toString(),MacamKasus.getSelectedItem().toString(), 
                    CaraMasuk.getSelectedItem().toString(),KelUtama.getText(),RPS.getText(),RPD.getText(),RPK.getText(),RPO.getText(),RPembedahan.getText(),RDirawatRS.getText(),AlatBantuDipakai.getSelectedItem().toString(),RTranfusi.getText(),RiImunisasi.getText(),RPerkmAnk.getSelectedItem().toString(),KetPerkmAnk.getText(), 
                    Alergi.getText(),KesadaranMental.getText(), 
                    KeadaanMentalUmum.getSelectedItem().toString(),GCS.getText(),TD.getText(),Nadi.getText(),RR.getText(),Suhu.getText(),SpO2.getText(),BB.getText(),TB.getText(),
                    PolaAktifitasMakan.getSelectedItem().toString(),PolaAktifitasMandi.getSelectedItem().toString(),PolaAktifitasEliminasi.getSelectedItem().toString(), 
                    PolaAktifitasBerpakaian.getSelectedItem().toString(),PolaAktifitasBerpindah.getSelectedItem().toString(),PolaNutrisiFrekuensi.getText(),PolaNutrisiJenis.getText(),PolaNutrisiPorsi.getText(),PolaTidurLama.getText(),PolaTidurGangguan.getSelectedItem().toString(),AktifitasSehari2.getSelectedItem().toString(), 
                    Aktifitas.getSelectedItem().toString(),Berjalan.getSelectedItem().toString(),KeteranganBerjalan.getText(),AlatAmbulasi.getSelectedItem().toString(),EkstrimitasAtas.getSelectedItem().toString(),KeteranganEkstrimitasAtas.getText(),EkstrimitasBawah.getSelectedItem().toString(),
                    KeteranganEkstrimitasBawah.getText(),KemampuanMenggenggam.getSelectedItem().toString(),KeteranganKemampuanMenggenggam.getText(),KemampuanKoordinasi.getSelectedItem().toString(),KeteranganKemampuanKoordinasi.getText(),KesimpulanGangguanFungsi.getSelectedItem().toString(),
                    KondisiPsikologis.getSelectedItem().toString(),GangguanJiwa.getSelectedItem().toString(),AdakahPerilaku.getSelectedItem().toString(),KeteranganAdakahPerilaku.getText(),HubunganAnggotaKeluarga.getSelectedItem().toString(),TinggalDengan.getSelectedItem().toString(),KeteranganTinggalDengan.getText(),
                    NilaiKepercayaan.getSelectedItem().toString(),KeteranganNilaiKepercayaan.getText(),PendidikanPJ.getSelectedItem().toString(),EdukasiPsikolgis.getSelectedItem().toString(),KeteranganEdukasiPsikologis.getText(),SkalaWajah.getSelectedItem().toString(),NilaiWajah.getText(),SkalaKaki.getSelectedItem().toString(),NilaiKaki.getText(),SkalaAktifitas.getSelectedItem().toString(),NilaiAktifitas.getText(),SkalaMenangis.getSelectedItem().toString(),
                    NilaiMenangis.getText(),SkalaBersuara.getSelectedItem().toString(),NilaiBersuara.getText(),SkalaNyeri1.getText(),SkalaResiko7.getSelectedItem().toString(),NilaiResiko7.getText(),SkalaResiko8.getSelectedItem().toString(),NilaiResiko8.getText(),
                    SkalaResiko9.getSelectedItem().toString(),NilaiResiko9.getText(),SkalaResiko10.getSelectedItem().toString(),NilaiResiko10.getText(), SkalaResiko11.getSelectedItem().toString(),NilaiResiko11.getText(),SkalaResiko12.getSelectedItem().toString(),NilaiResiko12.getText(),
                    SkalaResiko13.getSelectedItem().toString(),NilaiResiko13.getText(),NilaiResikoTotal1.getText(),SkalaGizi1.getSelectedItem().toString(),NilaiGizi1.getText(),SkalaGizi2.getSelectedItem().toString(),NilaiGizi2.getText(),NilaiGiziTotal.getText(),DiagnosaKhususGizi.getSelectedItem().toString(),
                    KeteranganDiagnosaKhususGizi.getText(),DiketahuiDietisen.getSelectedItem().toString(),KeteranganDiketahuiDietisen.getText(),Rencana.getText(),KdPetugas.getText(),KdPetugas2.getText(),KdDPJP.getText(),tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
             
        })==true){
                Sequel.meghapus("penilaian_awal_keperawatan_ranap_masalah","no_rawat",tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                for (i = 0; i < tbMasalahKeperawatan.getRowCount(); i++) {
                    if(tbMasalahKeperawatan.getValueAt(i,0).toString().equals("true")){
                        Sequel.menyimpan2("penilaian_awal_keperawatan_ranap_masalah","?,?",2,new String[]{TNoRw.getText(),tbMasalahKeperawatan.getValueAt(i,1).toString()});
                    }
                }
                Sequel.meghapus("penilaian_awal_keperawatan_ranap_rencana","no_rawat",tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                for (i = 0; i < tbRencanaKeperawatan.getRowCount(); i++) {
                    if(tbRencanaKeperawatan.getValueAt(i,0).toString().equals("true")){
                        Sequel.menyimpan2("penilaian_awal_keperawatan_ranap_rencana","?,?",2,new String[]{TNoRw.getText(),tbRencanaKeperawatan.getValueAt(i,1).toString()});
                    }
                }
//                if(Sequel.mengedittf("humty_dumpty_keperawatan","no_rawat=?","penilaian_humptydumpty_skala1=?,penilaian_humptydumpty_nilai1=?,penilaian_humptydumpty_skala2=?,penilaian_humptydumpty_nilai2=?,penilaian_humptydumpty_skala3=?,penilaian_humptydumpty_nilai3=?,penilaian_humptydumpty_skala4=?,penilaian_humptydumpty_nilai4=?,penilaian_humptydumpty_skala5=?,penilaian_humptydumpty_nilai5=?,penilaian_humptydumpty_skala6=?,penilaian_humptydumpty_nilai6=?,penilaian_humptydumpty_skala7=?,penilaian_humptydumpty_nilai7=?,penilaian_humptydumpty_totalnilai=?",16, new String[]{
//            TNoRw.getText(),SkalaResiko7.getSelectedItem().toString(), NilaiResiko7.getText(), SkalaResiko8.getSelectedItem().toString(), NilaiResiko8.getText(),
//            SkalaResiko9.getSelectedItem().toString(), NilaiResiko9.getText(), SkalaResiko10.getSelectedItem().toString(), NilaiResiko10.getText(), 
//            SkalaResiko11.getSelectedItem().toString(), NilaiResiko11.getText(), SkalaResiko12.getSelectedItem().toString(), NilaiResiko12.getText(),
//            SkalaResiko13.getSelectedItem().toString(), NilaiResiko13.getText(), NilaiResikoTotal1.getText()
//        }))
            {
//            tabMode.addRow(new String[]{
//                TNoRw.getText(),
//                SkalaResiko7.getSelectedItem().toString(), NilaiResiko7.getText(), SkalaResiko8.getSelectedItem().toString(), NilaiResiko8.getText(), SkalaResiko9.getSelectedItem().toString(), NilaiResiko9.getText(),
//                SkalaResiko10.getSelectedItem().toString(), NilaiResiko10.getText(), SkalaResiko11.getSelectedItem().toString(), NilaiResiko11.getText(), SkalaResiko12.getSelectedItem().toString(), NilaiResiko12.getText(),
//                SkalaResiko13.getSelectedItem().toString(), NilaiResiko13.getText(), NilaiResikoTotal1.getText()
//        });
//            tbObat.setValueAt(SkalaResiko7.getSelectedItem().toString(),tbObat.getSelectedRow(),142);
//            tbObat.setValueAt(NilaiResiko7.getText(),tbObat.getSelectedRow(),143);
//            tbObat.setValueAt(SkalaResiko8.getSelectedItem().toString(),tbObat.getSelectedRow(),144);
//            tbObat.setValueAt(NilaiResiko8.getText(),tbObat.getSelectedRow(),145);
//            tbObat.setValueAt(SkalaResiko9.getSelectedItem().toString(),tbObat.getSelectedRow(),146);
//            tbObat.setValueAt(NilaiResiko9.getText(),tbObat.getSelectedRow(),147);
//            tbObat.setValueAt(SkalaResiko10.getSelectedItem().toString(),tbObat.getSelectedRow(),148);
//            tbObat.setValueAt(NilaiResiko10.getText(),tbObat.getSelectedRow(),149);
//            tbObat.setValueAt(SkalaResiko11.getSelectedItem().toString(),tbObat.getSelectedRow(),150);
//            tbObat.setValueAt(NilaiResiko11.getText(),tbObat.getSelectedRow(),151);
//            tbObat.setValueAt(SkalaResiko12.getSelectedItem().toString(),tbObat.getSelectedRow(),152);
//            tbObat.setValueAt(NilaiResiko12.getText(),tbObat.getSelectedRow(),153);
//            tbObat.setValueAt(SkalaResiko13.getSelectedItem().toString(),tbObat.getSelectedRow(),154);
//            tbObat.setValueAt(NilaiResiko13.getText(),tbObat.getSelectedRow(),155);
//            tbObat.setValueAt(NilaiResikoTotal1.getText(),tbObat.getSelectedRow(),156);
//            isTotalResikoJatuh1();
                                
                getMasalah();
                tampil();
                DetailRencana.setText(Rencana.getText());
                emptTeks();
                TabRawat.setSelectedIndex(1);
        }
    }
    }
    
    
    
    private void isTotalSkalaNyeri() {
    try {
        int totalSkalaNyeri = Integer.parseInt(NilaiWajah.getText()) +
                              Integer.parseInt(NilaiKaki.getText()) +
                              Integer.parseInt(NilaiAktifitas.getText()) +
                              Integer.parseInt(NilaiMenangis.getText()) +
                              Integer.parseInt(NilaiBersuara.getText());

        SkalaNyeri1.setText(String.valueOf(totalSkalaNyeri));

        if (totalSkalaNyeri == 0) {
            // Emotikon untuk "Tidak Nyeri"
            totatskalanyeri.setText("<html>Tidak Nyeri <span style='font-size:23px;'>&#x1F642;</span></html>");

        } else if (totalSkalaNyeri <= 3) {
            // Emotikon untuk "Nyeri Ringan"
            totatskalanyeri.setText("<html>Nyeri Ringan <span style='font-size:23px;'>&#x1F610;</span></html>");
        } else if (totalSkalaNyeri <= 6) {
            // Emotikon untuk "Nyeri Sedang"
            totatskalanyeri.setText("<html>Nyeri Sedang <span style='font-size:23px;'>&#x1F614;</span></html>");
        } else if (totalSkalaNyeri <= 10) {
            // Emotikon untuk "Nyeri Hebat"
            totatskalanyeri.setText("<html>Nyeri Hebat <span style='font-size:23px;'>&#x1F622;</span></html>");
        } 
    } catch (Exception e) {
        SkalaNyeri1.setText("0");
        totatskalanyeri.setText("TIDAK NYERI \uD83D\uDE02");
    }
}

    
     private void isTotalResikoJatuh1(){
        try {
            NilaiResikoTotal1.setText((Integer.parseInt(NilaiResiko7.getText())+Integer.parseInt(NilaiResiko8.getText())+Integer.parseInt(NilaiResiko9.getText())+Integer.parseInt(NilaiResiko10.getText())+Integer.parseInt(NilaiResiko11.getText())+Integer.parseInt(NilaiResiko12.getText())+Integer.parseInt(NilaiResiko13.getText()))+"");
            if(Integer.parseInt(NilaiResikoTotal1.getText())<12){
                TingkatResiko1.setText("Tingkat Resiko : Risiko Rendah (7-11), Tindakan : Intervensi pencegahan risiko jatuh standar");
            }else if(Integer.parseInt(NilaiResikoTotal1.getText())<7){
                TingkatResiko1.setText("Tingkat Resiko : Risiko Rendah (0-7), Tindakan : Intervensi pencegahan risiko jatuh standar");
            }else if(Integer.parseInt(NilaiResikoTotal1.getText())>=12){
                TingkatResiko1.setText("Tingkat Resiko : Risiko Tinggi (>=12), Tindakan : Intervensi risiko jatuh tinggi(pasang gelang warna kuning)");
            }
        } catch (Exception e) {
            NilaiResikoTotal1.setText("0");
            TingkatResiko1.setText("Tingkat Resiko : Risiko Rendah (0-7), Tindakan : Intervensi pencegahan risiko jatuh standar");
        }
    }
    
    private void isTotalGizi(){
        try {
            NilaiGiziTotal.setText((Integer.parseInt(NilaiGizi1.getText())+Integer.parseInt(NilaiGizi2.getText()))+"");
        } catch (Exception e) {
            NilaiGiziTotal.setText("0");
        }
    }
    
    private void tampilMasalah() {
        try{
            Valid.tabelKosong(tabModeMasalah);
            file=new File("./cache/masalahkeperawatan.iyem");
            file.createNewFile();
            fileWriter = new FileWriter(file);
            iyem="";
            ps=koneksi.prepareStatement("select * from master_masalah_keperawatan order by master_masalah_keperawatan.kode_masalah");
            try {
                rs=ps.executeQuery();
                while(rs.next()){
                    tabModeMasalah.addRow(new Object[]{false,rs.getString(1),rs.getString(2)});
                    iyem=iyem+"{\"KodeMasalah\":\""+rs.getString(1)+"\",\"NamaMasalah\":\""+rs.getString(2)+"\"},";
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
            fileWriter.write("{\"masalahkeperawatan\":["+iyem.substring(0,iyem.length()-1)+"]}");
            fileWriter.flush();
            fileWriter.close();
            iyem=null;
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }
    
    private void tampilMasalah2() {
        try{
            jml=0;
            for(i=0;i<tbMasalahKeperawatan.getRowCount();i++){
                if(tbMasalahKeperawatan.getValueAt(i,0).toString().equals("true")){
                    jml++;
                }
            }

            pilih=null;
            pilih=new boolean[jml]; 
            kode=null;
            kode=new String[jml];
            masalah=null;
            masalah=new String[jml];

            index=0;        
            for(i=0;i<tbMasalahKeperawatan.getRowCount();i++){
                if(tbMasalahKeperawatan.getValueAt(i,0).toString().equals("true")){
                    pilih[index]=true;
                    kode[index]=tbMasalahKeperawatan.getValueAt(i,1).toString();
                    masalah[index]=tbMasalahKeperawatan.getValueAt(i,2).toString();
                    index++;
                }
            } 

            Valid.tabelKosong(tabModeMasalah);

            for(i=0;i<jml;i++){
                tabModeMasalah.addRow(new Object[] {
                    pilih[i],kode[i],masalah[i]
                });
            }
            
            myObj = new FileReader("./cache/masalahkeperawatan.iyem");
            root = mapper.readTree(myObj);
            response = root.path("masalahkeperawatan");
            if(response.isArray()){
                for(JsonNode list:response){
                    if(list.path("KodeMasalah").asText().toLowerCase().contains(TCariMasalah.getText().toLowerCase())||list.path("NamaMasalah").asText().toLowerCase().contains(TCariMasalah.getText().toLowerCase())){
                        tabModeMasalah.addRow(new Object[]{
                            false,list.path("KodeMasalah").asText(),list.path("NamaMasalah").asText()
                        });                    
                    }
                }
            }
            myObj.close();
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }
    
    private void tampilRencana() {
        try{
            file=new File("./cache/rencanakeperawatan.iyem");
            file.createNewFile();
            fileWriter = new FileWriter(file);
            iyem="";
            ps=koneksi.prepareStatement("select * from master_rencana_keperawatan order by master_rencana_keperawatan.kode_rencana");
            try {
                rs=ps.executeQuery();
                while(rs.next()){
                    iyem=iyem+"{\"KodeMasalah\":\""+rs.getString(1)+"\",\"KodeRencana\":\""+rs.getString(2)+"\",\"NamaRencana\":\""+rs.getString(3)+"\"},";
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
            fileWriter.write("{\"rencanakeperawatan\":["+iyem.substring(0,iyem.length()-1)+"]}");
            fileWriter.flush();
            fileWriter.close();
            iyem=null;
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
    }
    
    private void tampilRencana2() {
        try{
            jml=0;
            for(i=0;i<tbRencanaKeperawatan.getRowCount();i++){
                if(tbRencanaKeperawatan.getValueAt(i,0).toString().equals("true")){
                    jml++;
                }
            }

            pilih=null;
            pilih=new boolean[jml]; 
            kode=null;
            kode=new String[jml];
            masalah=null;
            masalah=new String[jml];

            index=0;        
            for(i=0;i<tbRencanaKeperawatan.getRowCount();i++){
                if(tbRencanaKeperawatan.getValueAt(i,0).toString().equals("true")){
                    pilih[index]=true;
                    kode[index]=tbRencanaKeperawatan.getValueAt(i,1).toString();
                    masalah[index]=tbRencanaKeperawatan.getValueAt(i,2).toString();
                    index++;
                }
            } 

            Valid.tabelKosong(tabModeRencana);

            for(i=0;i<jml;i++){
                tabModeRencana.addRow(new Object[] {
                    pilih[i],kode[i],masalah[i]
                });
            }

            myObj = new FileReader("./cache/rencanakeperawatan.iyem");
            root = mapper.readTree(myObj);
            response = root.path("rencanakeperawatan");
            if(response.isArray()){
                for(i=0;i<tbMasalahKeperawatan.getRowCount();i++){
                    if(tbMasalahKeperawatan.getValueAt(i,0).toString().equals("true")){
                        for(JsonNode list:response){
                            if(list.path("KodeMasalah").asText().toLowerCase().equals(tbMasalahKeperawatan.getValueAt(i,1).toString())&&
                                    list.path("NamaRencana").asText().toLowerCase().contains(TCariRencana.getText().toLowerCase())){
                                tabModeRencana.addRow(new Object[]{
                                    false,list.path("KodeRencana").asText(),list.path("NamaRencana").asText()
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

    
    }
