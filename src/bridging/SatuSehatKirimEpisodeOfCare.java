package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fungsi.koneksiDB;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;

/** Pengiriman EpisodeOfCare PTM yang merujuk Condition dan Encounter SATUSEHAT. */
public final class SatuSehatKirimEpisodeOfCare extends JDialog {
    private final Connection koneksi=koneksiDB.condb();
    private final ApiSatuSehat api=new ApiSatuSehat();
    private final SatuSehatCekNIK cekViaSatuSehat=new SatuSehatCekNIK();
    private final ObjectMapper mapper=new ObjectMapper();
    private final String noRawat;
    private final String noRm;
    private final String namaPasien;
    private final String nik;
    private final String idCondition;
    private final String icd10;
    private final String namaPenyakit;
    private final SatuSehatPtmRecommendation.Result rekomendasi;
    private String diagnosisPenyertaInfo="";
    private String link="";
    private String idPasien="";
    private String idEpisode="";
    private String identifierEpisode="";

    private final JComboBox<String> cmbTipe=new JComboBox<>(new String[]{"", "CAD", "CVD", "CKD", "cancer"});
    private final JComboBox<String> cmbStatus=new JComboBox<>(new String[]{"waitlist", "active", "onhold", "finished", "cancelled", "entered-in-error"});
    private final JTextField txtTanggalMulai=new JTextField(26);
    private final JTextField txtTanggalSelesai=new JTextField(26);
    private final JTextField txtEpisode=new JTextField(36);
    private final JLabel lblKeterangan=new JLabel("Belum terhubung dengan EpisodeOfCare.");
    private final JButton btnKirim=new JButton("Kirim / Hubungkan");
    private final JButton btnUpdate=new JButton("Update Status");
    private final JButton btnAnc=new JButton("Atur ANC");

    public SatuSehatKirimEpisodeOfCare(Dialog parent, boolean modal, String noRawat, String noRm,
            String namaPasien, String nik, String idCondition, String tanggalMulai) {
        this(parent,modal,noRawat,noRm,namaPasien,nik,idCondition,tanggalMulai,"","");
    }

    public SatuSehatKirimEpisodeOfCare(Dialog parent, boolean modal, String noRawat, String noRm,
            String namaPasien, String nik, String idCondition, String tanggalMulai,String icd10,String namaPenyakit) {
        super(parent, modal);
        this.noRawat=noRawat;
        this.noRm=noRm;
        this.namaPasien=namaPasien;
        this.nik=nik;
        this.idCondition=idCondition;
        this.icd10=icd10==null?"":icd10;
        this.namaPenyakit=namaPenyakit==null?"":namaPenyakit;
        SatuSehatPtmRecommendation.Result awal=SatuSehatPtmRecommendation.recommend(this.icd10,this.namaPenyakit);
        this.rekomendasi=awal.maternity?SatuSehatPtmRecommendation.recommend(this.icd10,this.namaPenyakit,cariDiagnosisPenyerta()):awal;
        initForm(tanggalMulai);
        try {
            link=koneksiDB.URLFHIRSATUSEHAT();
        } catch (Exception e) {
            System.out.println("Notifikasi EpisodeOfCare : "+e);
        }
        muatEpisodeLokal();
    }

    private void initForm(String tanggalMulai) {
        setTitle("Episode Perawatan PTM SATUSEHAT");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(800,650);
        setLocationRelativeTo(getOwner());

        txtTanggalMulai.setText(normalisasiTanggal(tanggalMulai));
        txtTanggalMulai.setToolTipText("yyyy-MM-dd (tanggal tanpa jam), atau datetime ISO berzona; waktu lokal tanpa offset memakai WITA.");
        txtTanggalSelesai.setToolTipText(txtTanggalMulai.getToolTipText());
        txtEpisode.setEditable(false);
        btnUpdate.setEnabled(false);
        cmbTipe.setSelectedItem(rekomendasi.type);
        cmbTipe.setToolTipText("Rekomendasi dapat diubah sesuai penilaian petugas; pilihan kosong wajib dilengkapi sebelum pengiriman.");

        JPanel data=new JPanel(new GridBagLayout());
        data.setBorder(BorderFactory.createTitledBorder("Episode perawatan"));
        GridBagConstraints c=new GridBagConstraints();
        c.insets=new Insets(5,7,5,7);
        c.anchor=GridBagConstraints.WEST;
        c.fill=GridBagConstraints.HORIZONTAL;
        tambahBaris(data,c,0,"No. Rawat",new JLabel(noRawat));
        tambahBaris(data,c,1,"Pasien",new JLabel(noRm+" - "+namaPasien));
        tambahBaris(data,c,2,"Diagnosis dipilih",teksBaca("ICD-10: "+(icd10.isEmpty()?"Belum tersedia":icd10)+"\nPenyakit: "+(namaPenyakit.isEmpty()?"Belum tersedia":namaPenyakit),2));
        tambahBaris(data,c,3,"Condition",new JLabel(idCondition));
        tambahBaris(data,c,4,"Tipe PTM",cmbTipe);
        tambahBaris(data,c,5,"Saran alur",teksBaca(rekomendasi.route,2));
        tambahBaris(data,c,6,"Rekomendasi",teksBaca(rekomendasi.explanation+diagnosisPenyertaInfo,5));
        tambahBaris(data,c,7,"Status",cmbStatus);
        tambahBaris(data,c,8,"Tanggal mulai",txtTanggalMulai);
        tambahBaris(data,c,9,"Tanggal selesai",txtTanggalSelesai);
        tambahBaris(data,c,10,"ID EpisodeOfCare",txtEpisode);

        JPanel bawah=new JPanel(new BorderLayout(5,5));
        bawah.setBorder(BorderFactory.createEmptyBorder(5,8,8,8));
        bawah.add(lblKeterangan,BorderLayout.CENTER);
        JPanel tombol=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnTutup=new JButton("Tutup");
        btnAnc.setVisible(rekomendasi.maternity&&rekomendasi.route.contains("ANC"));
        btnAnc.setEnabled(fungsi.akses.getsatu_sehat_kirim_encounter());
        btnAnc.setToolTipText("Buka pengaturan hanya bila kunjungan ini benar-benar pelayanan ANC. Tidak mengirim otomatis.");
        tombol.add(btnAnc);
        tombol.add(btnKirim);
        tombol.add(btnUpdate);
        tombol.add(btnTutup);
        bawah.add(tombol,BorderLayout.SOUTH);

        add(data,BorderLayout.CENTER);
        add(bawah,BorderLayout.SOUTH);
        btnKirim.addActionListener(e -> kirimAtauHubungkan());
        btnUpdate.addActionListener(e -> updateStatus());
        btnTutup.addActionListener(e -> dispose());
        btnAnc.addActionListener(e -> {
            if(!fungsi.akses.getsatu_sehat_kirim_encounter())return;
            new SatuSehatAturANC(this,true,noRawat,noRm,namaPasien,"").setVisible(true);
            lblKeterangan.setText("Jika pengaturan ANC sudah disimpan, lanjutkan Kirim/Update pada Encounter bila sudah siap.");
        });
        cmbTipe.addActionListener(e -> muatEpisodeLokal());
    }

    private javax.swing.JComponent teksBaca(String text,int rows){
        JTextArea area=new JTextArea(text,rows,42);area.setEditable(false);area.setOpaque(false);
        area.setLineWrap(true);area.setWrapStyleWord(true);area.setFont(new JLabel().getFont());
        javax.swing.JScrollPane scroll=new javax.swing.JScrollPane(area);
        scroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        return scroll;
    }

    private java.util.List<SatuSehatPtmRecommendation.Diagnosis> cariDiagnosisPenyerta(){
        java.util.List<SatuSehatPtmRecommendation.Diagnosis> values=new java.util.ArrayList<>();
        try(PreparedStatement p=koneksi.prepareStatement("select distinct c.kd_penyakit,p.nm_penyakit from satu_sehat_condition c "
                +"left join penyakit p on p.kd_penyakit=c.kd_penyakit where c.no_rawat=? and c.id_condition<>? "
                +"and c.id_condition is not null and c.id_condition<>'' order by c.kd_penyakit")){
            p.setString(1,noRawat);p.setString(2,idCondition);
            try(ResultSet r=p.executeQuery()){while(r.next())values.add(new SatuSehatPtmRecommendation.Diagnosis(r.getString(1),r.getString(2)));}
        }catch(Exception e){
            diagnosisPenyertaInfo=" Data Condition penyerta belum dapat dimuat. Tinjau daftar Condition kunjungan secara manual.";
            return null;
        }
        return values;
    }

    @Override public void setVisible(boolean visible){
        if(visible){
            java.awt.GraphicsConfiguration config=getGraphicsConfiguration();
            java.awt.Rectangle area=new java.awt.Rectangle(config.getBounds());
            java.awt.Insets insets=java.awt.Toolkit.getDefaultToolkit().getScreenInsets(config);
            area.x+=insets.left;area.y+=insets.top;area.width-=insets.left+insets.right;area.height-=insets.top+insets.bottom;
            setSize(Math.min(getWidth(),area.width-20),Math.min(getHeight(),area.height-20));
            setLocation(Math.max(area.x,Math.min(getX(),area.x+area.width-getWidth())),Math.max(area.y,Math.min(getY(),area.y+area.height-getHeight())));
        }
        super.setVisible(visible);
    }

    private void tambahBaris(JPanel panel, GridBagConstraints c, int baris, String label, java.awt.Component komponen) {
        c.gridy=baris;
        c.gridx=0;
        c.weightx=0;
        panel.add(new JLabel(label),c);
        c.gridx=1;
        c.weightx=1;
        panel.add(komponen,c);
    }

    private String normalisasiTanggal(String nilai) {
        // Pertahankan jam registrasi/offset yang tersedia; jangan isi tanggal hari ini sebagai fallback.
        return nilai==null?"":nilai.trim();
    }

    private boolean tanggalValid(String nilai, boolean bolehKosong) {
        if(bolehKosong && nilai.trim().equals("")){
            return true;
        }
        try{SatuSehatEpisodePayload.dateTime(nilai,"Tanggal episode",java.time.Clock.systemUTC());return true;}
        catch(IllegalArgumentException e){return false;}
    }

    private void muatEpisodeLokal() {
        btnKirim.setEnabled(fungsi.akses.getsatu_sehat_kirim_condition());
        idEpisode="";
        identifierEpisode="";
        txtEpisode.setText("");
        btnUpdate.setEnabled(false);
        if(cmbTipe.getSelectedItem()==null||cmbTipe.getSelectedItem().toString().isEmpty()){
            btnKirim.setEnabled(false);lblKeterangan.setText("Pilih tipe PTM setelah meninjau diagnosis dan rekomendasi.");return;
        }
        String sql="select id_episode,status,tanggal_mulai,ifnull(tanggal_selesai,'') tanggal_selesai,identifier_lokal " +
                   "from satu_sehat_episode_of_care where no_rkm_medis=? and tipe=? " +
                   "and status in ('waitlist','active','onhold') order by diubah_pada desc limit 2";
        try (PreparedStatement ps=koneksi.prepareStatement(sql)) {
            ps.setString(1,noRm);
            ps.setString(2,cmbTipe.getSelectedItem().toString());
            try (ResultSet rs=ps.executeQuery()) {
                if(rs.next()){
                    idEpisode=rs.getString("id_episode");
                    identifierEpisode=rs.getString("identifier_lokal");
                    txtEpisode.setText(idEpisode);
                    cmbStatus.setSelectedItem(rs.getString("status"));
                    txtTanggalMulai.setText(rs.getString("tanggal_mulai"));
                    txtTanggalSelesai.setText(rs.getString("tanggal_selesai"));
                    lblKeterangan.setText("Episode aktif ditemukan. Encounter ini dapat dihubungkan ke episode tersebut.");
                    btnUpdate.setEnabled(true);
                    if(rs.next())throw new Exception("Lebih dari satu episode PTM terbuka untuk pasien dan tipe yang sama.");
                }else{
                    lblKeterangan.setText("Belum ada episode aktif untuk pasien dan tipe PTM ini.");
                }
            }
        } catch (Exception e) {
            btnKirim.setEnabled(false);
            btnUpdate.setEnabled(false);
            lblKeterangan.setText("Periksa migrasi/data EpisodeOfCare: "+e.getMessage());
            System.out.println("Notifikasi EpisodeOfCare : "+e);
        }
    }

    private void kirimAtauHubungkan() {
        if(!fungsi.akses.getsatu_sehat_kirim_condition())return;
        if(!validasiInput()){
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            idPasien=cekViaSatuSehat.tampilIDPasien(nik);
            if(idPasien.equals("")){
                throw new Exception("ID IHS pasien tidak ditemukan.");
            }
            validasiConditionEncounter();
            if(idEpisode.equals("")){
                JsonNode episodeRemote=cariEpisodeRemote();
                if(episodeRemote!=null){
                    idEpisode=teks(episodeRemote,"id");
                    identifierEpisode=identifierRemote(episodeRemote);
                    String statusRemote=teks(episodeRemote,"status");
                    if(!statusRemote.equals("")){
                        cmbStatus.setSelectedItem(statusRemote);
                    }
                    txtTanggalMulai.setText(teks(episodeRemote.path("period"),"start"));
                    String end=teks(episodeRemote.path("period"),"end");
                    txtTanggalSelesai.setText(end);
                }else{
                    idEpisode=postEpisode();
                }
                if(idEpisode.equals("")){
                    throw new Exception("SATUSEHAT tidak mengembalikan ID EpisodeOfCare.");
                }
                simpanEpisodeLokal();
            }
            ambilEpisodeRemote(idEpisode);
            hubungkanEncounter();
            hubungkanEncounterSatusehat();
            txtEpisode.setText(idEpisode);
            btnUpdate.setEnabled(true);
            lblKeterangan.setText("EpisodeOfCare tersimpan dan sudah direferensikan oleh Encounter.");
            JOptionPane.showMessageDialog(this,"EpisodeOfCare berhasil disimpan dan Encounter berhasil diperbarui.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,"Gagal memproses EpisodeOfCare: "+pesanError(e));
            System.out.println("Notifikasi EpisodeOfCare : "+pesanError(e));
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void updateStatus() {
        if(!fungsi.akses.getsatu_sehat_kirim_condition())return;
        if(idEpisode.equals("") || !validasiInput()){
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            if(idPasien.equals("")){
                idPasien=cekViaSatuSehat.tampilIDPasien(nik);
            }
            HttpHeaders headers=headerJson();
            validasiConditionEncounter();
            ObjectNode resource=ambilEpisodeRemote(idEpisode);
            String version=resource.path("meta").path("versionId").asText();
            if(!version.isEmpty())headers.add("If-Match","W/\""+version+"\"");
            perbaruiStatusResource(resource);
            String payload=mapper.writeValueAsString(resource);
            HttpEntity<String> request=new HttpEntity<>(payload,headers);
            String hasil=api.getRest().exchange(link+"/EpisodeOfCare/"+idEpisode,HttpMethod.PUT,request,String.class).getBody();
            JsonNode root=mapper.readTree(hasil);
            if(teks(root,"id").equals("")){
                throw new Exception("Respons update tidak memiliki ID EpisodeOfCare.");
            }
            simpanEpisodeLokal();
            hubungkanEncounter();
            hubungkanEncounterSatusehat();
            lblKeterangan.setText("Status EpisodeOfCare berhasil diperbarui.");
            JOptionPane.showMessageDialog(this,"Status EpisodeOfCare berhasil diperbarui.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,"Gagal memperbarui EpisodeOfCare: "+pesanError(e));
            System.out.println("Notifikasi EpisodeOfCare : "+pesanError(e));
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private boolean validasiInput() {
        if(cmbTipe.getSelectedItem()==null||cmbTipe.getSelectedItem().toString().isEmpty()){
            JOptionPane.showMessageDialog(this,"Pilih tipe PTM sesuai diagnosis dan pelayanan terlebih dahulu.");return false;
        }
        if(nik.trim().equals("") || idCondition.trim().equals("")){
            JOptionPane.showMessageDialog(this,"NIK pasien atau ID Condition belum tersedia.");
            return false;
        }
        if(!tanggalValid(txtTanggalMulai.getText(),false) || !tanggalValid(txtTanggalSelesai.getText(),true)){
            JOptionPane.showMessageDialog(this,"Periksa format tanggal/datetime, batas 03-06-2014, dan waktu komputer. Tanggal mendatang tidak dikirim otomatis.");
            return false;
        }
        if((cmbStatus.getSelectedItem().toString().equals("finished") || cmbStatus.getSelectedItem().toString().equals("cancelled")) && txtTanggalSelesai.getText().trim().equals("")){
            JOptionPane.showMessageDialog(this,"Tanggal selesai wajib diisi untuk status selesai atau dibatalkan.");
            return false;
        }
        if(!txtTanggalSelesai.getText().trim().isEmpty()&&java.time.Instant.parse(SatuSehatEpisodePayload.dateTime(txtTanggalSelesai.getText(),"period.end",java.time.Clock.systemUTC()))
                .isBefore(java.time.Instant.parse(SatuSehatEpisodePayload.dateTime(txtTanggalMulai.getText(),"period.start",java.time.Clock.systemUTC())))){
            JOptionPane.showMessageDialog(this,"Tanggal selesai tidak boleh sebelum tanggal mulai.");return false;
        }
        return true;
    }

    private JsonNode cariEpisodeRemote() throws Exception {
        String type=cmbTipe.getSelectedItem().toString();
        java.util.List<JsonNode> resources=SatuSehatEpisodeSupport.search(api,link,
                "/EpisodeOfCare?patient="+encode(idPasien)+"&type="+encode(type)+"&organization="+encode(koneksiDB.IDSATUSEHAT()),mapper);
        JsonNode found=null;
        for(JsonNode resource:resources){
            if(!(episodeSesuai(resource,"waitlist")||episodeSesuai(resource,"active")||episodeSesuai(resource,"onhold")))continue;
            if(found!=null&&!teks(found,"id").equals(teks(resource,"id")))throw new Exception("Lebih dari satu episode PTM terbuka. Tinjau sebelum membuat atau menghubungkan episode.");
            found=resource;
        }
        return found;
    }
    private boolean episodeSesuai(JsonNode resource, String status) {
        if(!status.equals(teks(resource,"status"))){
            return false;
        }
        return SatuSehatEpisodeSupport.matchesEpisode(resource,idPasien,koneksiDB.IDSATUSEHAT(),cmbTipe.getSelectedItem().toString());
    }

    private String postEpisode() throws Exception {
        identifierEpisode=identifierLokal();
        HttpHeaders headers=headerJson();
        String payload=mapper.writeValueAsString(buatResource(""));
        HttpEntity<String> request=new HttpEntity<>(payload,headers);
        String hasil=api.getRest().exchange(link+"/EpisodeOfCare",HttpMethod.POST,request,String.class).getBody();
        return teks(mapper.readTree(hasil),"id");
    }

    private ObjectNode buatResource(String id) {
        String tipe=cmbTipe.getSelectedItem().toString();
        ObjectNode resource=SatuSehatEpisodePayload.post(mapper,koneksiDB.IDSATUSEHAT(),idPasien,
                identifierEpisode.isEmpty()?identifierLokal():identifierEpisode,tipe,displayTipe(tipe),
                cmbStatus.getSelectedItem().toString(),txtTanggalMulai.getText().trim(),txtTanggalSelesai.getText().trim(),
                idCondition,java.time.Clock.systemUTC());
        if(!id.isEmpty())resource.put("id",id);
        return resource;
    }
    private void simpanEpisodeLokal() throws Exception {
        String sql="insert into satu_sehat_episode_of_care " +
                   "(id_episode,no_rkm_medis,tipe,status,tanggal_mulai,tanggal_selesai,no_rawat_awal,id_condition,identifier_lokal) " +
                   "values (?,?,?,?,?,nullif(?,''),?,?,?) on duplicate key update status=values(status)," +
                   "tanggal_mulai=values(tanggal_mulai),tanggal_selesai=values(tanggal_selesai)," +
                   "id_condition=values(id_condition),diubah_pada=current_timestamp";
        try (PreparedStatement ps=koneksi.prepareStatement(sql)) {
            ps.setString(1,idEpisode);
            ps.setString(2,noRm);
            ps.setString(3,cmbTipe.getSelectedItem().toString());
            ps.setString(4,cmbStatus.getSelectedItem().toString());
            ps.setString(5,SatuSehatEpisodePayload.clinicalDate(txtTanggalMulai.getText().trim()));
            ps.setString(6,txtTanggalSelesai.getText().trim().isEmpty()?"":SatuSehatEpisodePayload.clinicalDate(txtTanggalSelesai.getText().trim()));
            ps.setString(7,noRawat);
            ps.setString(8,idCondition);
            ps.setString(9,identifierEpisode.equals("") ? identifierLokal() : identifierEpisode);
            ps.executeUpdate();
        }
    }

    private ObjectNode ambilEpisodeRemote(String id) throws Exception {
        HttpEntity<String> request=new HttpEntity<>(headerJson());
        String hasil=api.getRest().exchange(link+"/EpisodeOfCare/"+id,HttpMethod.GET,request,String.class).getBody();
        JsonNode resource=mapper.readTree(hasil);
        if(!resource.isObject()||!id.equals(teks(resource,"id"))){
            throw new Exception("Resource EpisodeOfCare tidak valid.");
        }
        SatuSehatEpisodeSupport.validateEpisode(resource,idPasien,koneksiDB.IDSATUSEHAT(),cmbTipe.getSelectedItem().toString());
        return (ObjectNode)resource;
    }

    private void perbaruiStatusResource(ObjectNode resource) {
        SatuSehatEpisodePayload.update(resource,koneksiDB.IDSATUSEHAT(),
                identifierEpisode.isEmpty()?identifierLokal():identifierEpisode,cmbStatus.getSelectedItem().toString(),
                txtTanggalMulai.getText().trim(),txtTanggalSelesai.getText().trim(),java.time.Clock.systemUTC());
    }
    private void hubungkanEncounter() throws Exception {
        String sql="insert ignore into satu_sehat_encounter_episode(no_rawat,id_episode) values (?,?)";
        try (PreparedStatement ps=koneksi.prepareStatement(sql)) {
            ps.setString(1,noRawat);
            ps.setString(2,idEpisode);
            ps.executeUpdate();
        }
    }

    private void hubungkanEncounterSatusehat() throws Exception {
        String idEncounter="";
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select id_encounter from satu_sehat_encounter where no_rawat=? limit 1")) {
            ps.setString(1,noRawat);
            try (ResultSet rs=ps.executeQuery()) {
                if(rs.next()){
                    idEncounter=rs.getString("id_encounter");
                }
            }
        }
        if(idEncounter==null || idEncounter.trim().equals("")){
            throw new Exception("ID Encounter SATUSEHAT belum tersedia untuk nomor rawat ini.");
        }

        String url=link+"/Encounter/"+idEncounter;
        System.out.println("URL Encounter GET untuk EpisodeOfCare : "+url);
        HttpEntity<String> getRequest=new HttpEntity<>(headerJson());
        String hasilGet=api.getRest().exchange(url,HttpMethod.GET,getRequest,String.class).getBody();
        JsonNode nodeEncounter=mapper.readTree(hasilGet);
        if(!nodeEncounter.isObject()){
            throw new Exception("Resource Encounter SATUSEHAT tidak valid.");
        }
        ObjectNode encounter=(ObjectNode)nodeEncounter;
        if(!("Patient/"+idPasien).equals(encounter.path("subject").path("reference").asText())
                ||!("Organization/"+koneksiDB.IDSATUSEHAT()).equals(encounter.path("serviceProvider").path("reference").asText()))
            throw new Exception("Encounter tidak sesuai pasien atau organisasi Mahkota Bunda.");
        ArrayNode daftarEpisode;
        JsonNode episodeNode=encounter.path("episodeOfCare");
        if(episodeNode.isArray()){
            daftarEpisode=(ArrayNode)episodeNode;
        }else{
            daftarEpisode=encounter.putArray("episodeOfCare");
        }
        String referensi="EpisodeOfCare/"+idEpisode;
        boolean sudahAda=false;
        for(JsonNode episode:daftarEpisode){
            if(referensi.equals(teks(episode,"reference"))){
                sudahAda=true;
                break;
            }
        }
        if(!sudahAda){
            daftarEpisode.addObject().put("reference",referensi);
        }

        System.out.println("URL Encounter PUT untuk EpisodeOfCare : "+url);
        String payload=mapper.writeValueAsString(encounter);
        HttpHeaders putHeaders=headerJson();
        String version=encounter.path("meta").path("versionId").asText();
        if(!version.isEmpty())putHeaders.add("If-Match","W/\""+version+"\"");
        HttpEntity<String> putRequest=new HttpEntity<>(payload,putHeaders);
        String hasilPut=api.getRest().exchange(url,HttpMethod.PUT,putRequest,String.class).getBody();
        JsonNode responsePut=mapper.readTree(hasilPut);
        if(!idEncounter.equals(teks(responsePut,"id"))){
            throw new Exception("SATUSEHAT tidak mengembalikan ID Encounter setelah pembaruan.");
        }
    }

    private HttpHeaders headerJson() throws Exception {
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization","Bearer "+api.TokenSatuSehat());
        return headers;
    }

    private void validasiConditionEncounter() throws Exception {
        if(!idCondition.matches("[A-Za-z0-9.-]{1,64}"))throw new Exception("ID Condition tidak valid.");
        JsonNode condition=mapper.readTree(api.getRest().exchange(link+"/Condition/"+idCondition,HttpMethod.GET,new HttpEntity<>(headerJson()),String.class).getBody());
        if(!"Condition".equals(teks(condition,"resourceType"))||!("Patient/"+idPasien).equals(condition.path("subject").path("reference").asText()))
            throw new Exception("Condition tidak sesuai pasien.");
        try(PreparedStatement p=koneksi.prepareStatement("select id_encounter from satu_sehat_encounter where no_rawat=?")){
            p.setString(1,noRawat);try(ResultSet r=p.executeQuery()){
                if(!r.next()||!("Encounter/"+r.getString(1)).equals(condition.path("encounter").path("reference").asText()))
                    throw new Exception("Condition tidak sesuai Encounter kunjungan.");
            }
        }
    }

    private String identifierLokal() {
        return noRawat.replace('/','-')+"-"+cmbTipe.getSelectedItem().toString();
    }

    private String identifierRemote(JsonNode resource) {
        JsonNode identifiers=resource.path("identifier");
        if(identifiers.isArray() && identifiers.size()>0){
            String nilai=teks(identifiers.get(0),"value");
            if(!nilai.equals("")){
                return nilai;
            }
        }
        return identifierLokal();
    }

    private String displayTipe(String tipe) {
        if(tipe.equals("CAD")) return "Coronary Arterial Disease Management Care";
        if(tipe.equals("CVD")) return "Cerebrovascular Disease";
        if(tipe.equals("CKD")) return "Chronic Kidney Disease";
        return "Cancer Management Care";
    }

    private String encode(String nilai) throws Exception {
        return URLEncoder.encode(nilai,"UTF-8");
    }

    private String teks(JsonNode node, String nama) {
        JsonNode value=node.path(nama);
        return value.isMissingNode() || value.isNull() ? "" : value.asText();
    }

    private String pesanError(Exception e) {
        if(e instanceof HttpStatusCodeException){
            String body=((HttpStatusCodeException)e).getResponseBodyAsString();
            try {
                JsonNode issues=mapper.readTree(body).path("issue");
                if(issues.isArray() && issues.size()>0){
                    String detail=teks(issues.get(0).path("details"),"text");
                    if(!detail.equals("")){
                        if(detail.toLowerCase().contains("consent or privacy rules")){
                            return "Akses EpisodeOfCare ditolak oleh kebijakan SATUSEHAT. " +
                                   "Pastikan use case EpisodeOfCare PTM sudah diaktifkan untuk kredensial Production organisasi ini. " +
                                   "Pesan server: "+detail;
                        }
                        return detail;
                    }
                }
            } catch (Exception tidakDipakai) {
                // Gunakan pesan HTTP standar bila OperationOutcome tidak dapat dibaca.
            }
        }
        String pesan=e.getMessage();
        if(pesan==null || pesan.trim().equals("")){
            return e.toString();
        }
        return pesan;
    }
}
