package bridging;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import fungsi.akses;
import fungsi.koneksiDB;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.*;
import java.time.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/** Dedicated connections, explicit reviewed instructions, no implicit DDL or sends on opening a screen. */
public final class SatuSehatNutritionOrderService {
    private static final ObjectMapper M=SatuSehatNutritionOrderPayload.JSON;
    public static final class Row {
        public String visit,rm,name,date,author,intervention,instruction,sourceHash,encounter="",id="",remote="",state="NEW";
        public ObjectNode review=M.createObjectNode();
        public int revision,sentRevision;
    }
    public static final class Doctor {
        public final String code,name;
        public Doctor(String code,String name){this.code=code;this.name=name;}
        @Override public String toString(){return code+" - "+name;}
    }
    private final String endpoint=koneksiDB.URLFHIRSATUSEHAT().replaceAll("/+$",""),org=koneksiDB.IDSATUSEHAT();
    private final String endpointHash=SatuSehatNutritionOrderPayload.hash(endpoint);
    public static boolean canRead(){return akses.getcatatan_adime_gizi()||akses.getsatu_sehat_kirim_diet();}
    public static boolean canEdit(){return akses.getcatatan_adime_gizi();}
    public static boolean canSend(){return akses.getsatu_sehat_kirim_diet();}
    private static void require(boolean ok,String msg){if(!ok)throw new IllegalArgumentException(msg);}
    private static String value(ResultSet r,String field)throws Exception{String v=r.getString(field);return v==null?"":v;}
    private static String actor(){String who=akses.getkode();require(who!=null&&!who.trim().isEmpty(),"Pengguna pencatat belum tersedia.");return who;}
    public static boolean migrated(Connection c)throws Exception{
        try(PreparedStatement s=c.prepareStatement("select count(*) from information_schema.tables where table_schema=database() and table_name in ('gizi_instruksi_diet','gizi_instruksi_diet_riwayat','satu_sehat_nutrition_order')");ResultSet r=s.executeQuery()){r.next();return r.getInt(1)==3;}
    }
    private static void requireMigration(Connection c)throws Exception{require(migrated(c),"Penyimpanan NutritionOrder belum tersedia. Migrasi harus ditinjau dan dijalankan terpisah dengan persetujuan. ADIME tetap dapat digunakan.");}
    private static String sourceHash(ResultSet r)throws Exception{StringBuilder b=new StringBuilder();for(String key:new String[]{"no_rawat","tanggal","no_rkm_medis","nip","asesmen","diagnosis","intervensi","monitoring","evaluasi","instruksi"}){String v=value(r,key);b.append(v.length()).append(':').append(v);}return SatuSehatNutritionOrderPayload.hash(b.toString());}
    private static final String SOURCE="select a.*,r.no_rkm_medis,p.nm_pasien,ifnull(e.id_encounter,'') as id_encounter from catatan_adime_gizi a join reg_periksa r on r.no_rawat=a.no_rawat join pasien p on p.no_rkm_medis=r.no_rkm_medis left join satu_sehat_encounter e on e.no_rawat=r.no_rawat ";
    public List<Row> load(String from,String to,String filter)throws Exception{
        require(canRead(),"Tidak memiliki akses ADIME atau pengiriman Diet.");LocalDate a=LocalDate.parse(from),b=LocalDate.parse(to);require(!b.isBefore(a),"Rentang tanggal tidak valid.");
        List<Row> out=new ArrayList<Row>();try(Connection c=koneksiDB.bukaKoneksiBaca()){
            boolean storage=migrated(c);
            try(PreparedStatement s=c.prepareStatement(SOURCE+"where a.tanggal>=? and a.tanggal<? and (a.no_rawat like ? or r.no_rkm_medis like ? or p.nm_pasien like ?) order by a.tanggal desc")){
                s.setString(1,a+" 00:00:00");s.setString(2,b.plusDays(1)+" 00:00:00");for(int i=3;i<=5;i++)s.setString(i,"%"+filter+"%");
                try(ResultSet r=s.executeQuery()){while(r.next()){
                    Row row=new Row();row.visit=value(r,"no_rawat");row.rm=value(r,"no_rkm_medis");row.name=value(r,"nm_pasien");row.date=value(r,"tanggal");row.author=value(r,"nip");row.intervention=value(r,"intervensi");row.instruction=value(r,"instruksi");row.sourceHash=sourceHash(r);row.encounter=value(r,"id_encounter");out.add(row);
                }}
            }
            if(storage)for(Row row:out)loadReview(c,row);
        }return out;
    }
    private void loadReview(Connection c,Row row)throws Exception{
        try(PreparedStatement s=c.prepareStatement("select i.*,ifnull(n.id_nutrition_order,'') as remote_id,ifnull(n.sync_state,'NEW') as current_state,ifnull(n.revisi_terkirim,0) as sent_revision from gizi_instruksi_diet i left join satu_sehat_nutrition_order n on n.id_instruksi=i.id_instruksi and n.organisasi=? and n.endpoint_hash=? where i.source_key=?")){
            s.setString(1,org);s.setString(2,endpointHash);s.setString(3,SatuSehatNutritionOrderPayload.sourceKey(row.visit,row.date));try(ResultSet r=s.executeQuery()){if(r.next()){row.id=value(r,"id_instruksi");row.review=(ObjectNode)M.readTree(value(r,"review_json"));row.revision=r.getInt("revisi");row.remote=value(r,"remote_id");row.state=value(r,"current_state");row.sentRevision=r.getInt("sent_revision");if(!row.sourceHash.equals(value(r,"source_hash")))row.state="SOURCE_CHANGED";}}
        }
    }
    public List<Doctor> doctors()throws Exception{
        require(canRead(),"Akses tidak tersedia.");List<Doctor> out=new ArrayList<Doctor>();try(Connection c=koneksiDB.bukaKoneksiBaca();PreparedStatement s=c.prepareStatement("select d.kd_dokter,d.nm_dokter from dokter d join pegawai g on g.nik=d.kd_dokter where d.status='1' and g.stts_aktif='AKTIF' order by d.nm_dokter");ResultSet r=s.executeQuery()){while(r.next())out.add(new Doctor(r.getString(1),r.getString(2)));}return out;
    }
    public List<String> doctorCandidates(String visit)throws Exception{
        require(canRead(),"Akses tidak tersedia.");List<String> out=new ArrayList<String>();try(Connection c=koneksiDB.bukaKoneksiBaca()){
            // A registered inpatient DPJP takes precedence over the registration/referring doctor.
            try(PreparedStatement s=c.prepareStatement("select distinct kd_dokter from dpjp_ranap where no_rawat=? and trim(ifnull(kd_dokter,''))<>'' order by kd_dokter")){s.setString(1,visit);try(ResultSet r=s.executeQuery()){while(r.next())out.add(r.getString(1));}}
            if(out.isEmpty())try(PreparedStatement s=c.prepareStatement("select kd_dokter from reg_periksa where no_rawat=? and trim(ifnull(kd_dokter,''))<>''")){s.setString(1,visit);try(ResultSet r=s.executeQuery()){while(r.next())out.add(r.getString(1));}}
        }return out;
    }
    private void checkSource(Connection c,Row row,boolean lock)throws Exception{
        try(PreparedStatement s=c.prepareStatement(SOURCE+"where a.no_rawat=? and a.tanggal=?"+(lock?" for update":""))){s.setString(1,row.visit);s.setString(2,row.date);try(ResultSet r=s.executeQuery()){require(r.next(),"ADIME sumber tidak ditemukan; tinjau ulang.");require(row.sourceHash.equals(sourceHash(r)),"ADIME berubah sejak daftar dimuat; muat ulang dan tinjau instruksi.");row.encounter=value(r,"id_encounter");require(!r.next(),"Sumber/Encounter ganda; perlu diperiksa.");}}
    }
    public void save(Row row,ObjectNode review)throws Exception{
        require(canEdit(),"Memerlukan akses Catatan ADIME Gizi.");
        // Draft can retain unresolved terminology, but never bypass send validation.
        require(review.path("instruction").asText().length()<=20000,"Instruksi terlalu panjang.");
        try(Connection c=koneksiDB.bukaKoneksiTransaksi()){
            requireMigration(c);c.setAutoCommit(false);try{
                checkSource(c,row,true);String id=row.id;int rev=0;
                try(PreparedStatement s=c.prepareStatement("select id_instruksi,revisi from gizi_instruksi_diet where source_key=? for update")){s.setString(1,SatuSehatNutritionOrderPayload.sourceKey(row.visit,row.date));try(ResultSet r=s.executeQuery()){if(r.next()){id=r.getString(1);rev=r.getInt(2);}}}
                require(rev==row.revision,"Instruksi telah diubah pengguna lain. Muat ulang.");
                if(!id.isEmpty())try(PreparedStatement s=c.prepareStatement("select sync_state from satu_sehat_nutrition_order where id_instruksi=? for update")){s.setString(1,id);try(ResultSet r=s.executeQuery()){while(r.next())require(!Arrays.asList("SENDING","UNKNOWN").contains(r.getString(1)),"Pengiriman belum pasti; rekonsiliasi ID sebelum mengubah instruksi.");}}
                String json=M.writeValueAsString(review),who=actor();int next=rev+1;
                if(id.isEmpty()){
                    id=UUID.randomUUID().toString();try(PreparedStatement s=c.prepareStatement("insert into gizi_instruksi_diet(id_instruksi,source_key,no_rawat,tanggal_adime,source_hash,review_json,revisi,pencatat,diubah_pada) values(?,?,?,?,?,?,?,?,now())")){s.setString(1,id);s.setString(2,SatuSehatNutritionOrderPayload.sourceKey(row.visit,row.date));s.setString(3,row.visit);s.setString(4,row.date);s.setString(5,row.sourceHash);s.setString(6,json);s.setInt(7,next);s.setString(8,who);s.executeUpdate();}
                }else try(PreparedStatement s=c.prepareStatement("update gizi_instruksi_diet set review_json=?,source_hash=?,revisi=?,pencatat=?,diubah_pada=now() where id_instruksi=? and revisi=?")){s.setString(1,json);s.setString(2,row.sourceHash);s.setInt(3,next);s.setString(4,who);s.setString(5,id);s.setInt(6,rev);require(s.executeUpdate()==1,"Revisi berubah; muat ulang.");}
                try(PreparedStatement s=c.prepareStatement("insert into gizi_instruksi_diet_riwayat(id_instruksi,revisi,source_hash,review_json,pencatat,dicatat_pada) values(?,?,?,?,?,now())")){s.setString(1,id);s.setInt(2,next);s.setString(3,row.sourceHash);s.setString(4,json);s.setString(5,who);s.executeUpdate();}
                c.commit();row.id=id;row.revision=next;row.review=review.deepCopy();if("SOURCE_CHANGED".equals(row.state))row.state=row.remote.isEmpty()?"NEW":"SENT";
            }catch(Exception e){c.rollback();throw e;}
        }
    }
    public String readiness(Row row){
        if(row.id.isEmpty())return "Belum ditinjau/disimpan";
        if(Arrays.asList("SOURCE_CHANGED","SENDING","UNKNOWN").contains(row.state))return row.state+" - perlu pemeriksaan";
        if(row.encounter.trim().isEmpty())return "Belum ada ID Encounter";
        try{SatuSehatNutritionOrderPayload.build(row.review,"contoh-patient",row.encounter,"contoh-practitioner",Clock.systemUTC());}catch(Exception e){return e.getMessage();}
        return row.remote.isEmpty()?"Siap ditinjau untuk Kirim":row.sentRevision==row.revision?"Sudah terkirim":"Revisi lokal - perlu Update";
    }
    public ObjectNode preview(Row row)throws Exception{
        require(canRead(),"Akses tidak tersedia.");return SatuSehatNutritionOrderPayload.build(row.review,"contoh-patient","contoh-encounter","contoh-practitioner",Clock.systemUTC());
    }
    private void config(){URI u=URI.create(endpoint);require("https".equals(u.getScheme())&&u.getHost()!=null&&u.getUserInfo()==null&&u.getQuery()==null&&u.getFragment()==null&&u.getPath().endsWith("/fhir-r4/v1"),"Endpoint FHIR R4 pada konfigurasi belum valid.");require(org!=null&&org.matches("[A-Za-z0-9.-]{1,64}"),"Organisasi pada konfigurasi belum valid.");}
    private static HttpHeaders headers(ApiSatuSehat api){String token=api.TokenSatuSehat();require(token!=null&&!token.isEmpty(),"Token SATUSEHAT tidak tersedia.");HttpHeaders h=new HttpHeaders();h.setContentType(MediaType.APPLICATION_JSON);h.set("Authorization","Bearer "+token);return h;}
    /** Sanitized stage information survives SwingWorker/transport exception wrappers. */
    public static final class SafeFailure extends IllegalStateException {
        public SafeFailure(String message,Throwable cause){super(message,cause);}
    }
    /** The query is already encoded. The URI overload prevents RestTemplate encoding '%' a second time. */
    public static String readResource(RestTemplate rest,String base,String path,HttpHeaders h){
        String resource="referensi";for(String type:new String[]{"Patient","Practitioner","Encounter","NutritionOrder"})if(path.startsWith("/"+type+"?")||path.startsWith("/"+type+"/"))resource=type;
        try{return rest.exchange(URI.create(base+path),HttpMethod.GET,new HttpEntity<String>(h),String.class).getBody();}
        catch(HttpStatusCodeException e){throw new SafeFailure("Pemeriksaan "+resource+" ditolak SATUSEHAT (HTTP "+e.getStatusCode().value()+"). "+SatuSehatNutritionOrderPayload.safeError(e.getResponseBodyAsString()),e);}
        catch(Exception e){throw new SafeFailure("Pemeriksaan "+resource+" belum selesai. Periksa koneksi dan konfigurasi endpoint SATUSEHAT.",e);}
    }
    public static String errorMessage(Throwable error){
        Throwable current=error;String validation=null;
        for(int depth=0;current!=null&&depth<20;depth++,current=current.getCause()){
            if(current instanceof SafeFailure)return current.getMessage();
            if(current instanceof HttpStatusCodeException){HttpStatusCodeException http=(HttpStatusCodeException)current;return "SATUSEHAT menolak permintaan (HTTP "+http.getStatusCode().value()+"). "+SatuSehatNutritionOrderPayload.safeError(http.getResponseBodyAsString());}
            if(current instanceof SQLException){SQLException sql=(SQLException)current;String state=sql.getSQLState();return "Penyimpanan NutritionOrder belum dapat diakses (SQLState "+(state!=null&&state.matches("[A-Z0-9]{5}")?state:"tidak tersedia")+", kode "+sql.getErrorCode()+"). Periksa struktur/koneksi database; muat ulang status sebelum mencoba kembali.";}
            if(validation==null&&(current instanceof IllegalArgumentException||current instanceof IllegalStateException))validation=current.getMessage();
        }
        return validation==null?"Operasi belum selesai. Muat ulang status; periksa koneksi atau struktur penyimpanan.":validation;
    }
    private JsonNode get(ApiSatuSehat api,HttpHeaders h,String path)throws Exception{
        String body=readResource(api.getRest(),endpoint,path,h);
        try{return M.readTree(body);}catch(Exception e){throw new SafeFailure("Respons pemeriksaan referensi SATUSEHAT belum dapat dibaca. Pengiriman ditahan.",e);}
    }
    private String identity(ApiSatuSehat api,HttpHeaders h,String type,String nik)throws Exception{
        require(nik!=null&&nik.matches("[0-9]{16}"),"NIK "+type+" belum lengkap; pengiriman ditahan.");
        JsonNode bundle=get(api,h,"/"+type+"?identifier="+URLEncoder.encode("https://fhir.kemkes.go.id/id/nik|"+nik,"UTF-8"));
        require(bundle.path("entry").isArray()&&bundle.path("entry").size()==1,"IHS "+type+" tidak ditemukan atau ambigu.");JsonNode resource=bundle.path("entry").get(0).path("resource");require(type.equals(resource.path("resourceType").asText()),"Jenis referensi IHS tidak sesuai.");String id=resource.path("id").asText();SatuSehatNutritionOrderPayload.ref(type,id);return id;
    }
    private ObjectNode prepare(Row row,ApiSatuSehat api,HttpHeaders h)throws Exception{
        require(!row.id.isEmpty(),"Simpan instruksi terlebih dahulu.");
        try(Connection c=koneksiDB.bukaKoneksiBaca()){
            requireMigration(c);checkSource(c,row,false);
            try(PreparedStatement s=c.prepareStatement("select p.no_ktp as patient_nik,g.no_ktp as doctor_nik from reg_periksa r join pasien p on p.no_rkm_medis=r.no_rkm_medis join dokter d on d.kd_dokter=? join pegawai g on g.nik=d.kd_dokter where r.no_rawat=?")){s.setString(1,row.review.path("doctor").asText());s.setString(2,row.visit);try(ResultSet r=s.executeQuery()){require(r.next(),"Dokter pemberi instruksi belum terhubung ke pegawai.");String patient=identity(api,h,"Patient",r.getString("patient_nik")),doctor=identity(api,h,"Practitioner",r.getString("doctor_nik"));JsonNode encounter=get(api,h,"/"+SatuSehatNutritionOrderPayload.ref("Encounter",row.encounter));require(SatuSehatNutritionOrderPayload.ref("Patient",patient).equals(encounter.path("subject").path("reference").asText()),"Pasien pada Encounter tidak sesuai.");require(!Arrays.asList("cancelled","entered-in-error").contains(encounter.path("status").asText()),"Encounter server tidak valid untuk instruksi ini.");
                require(SatuSehatNutritionOrderPayload.ref("Organization",org).equals(encounter.path("serviceProvider").path("reference").asText()),"Organisasi Encounter berbeda dari konfigurasi.");
                return SatuSehatNutritionOrderPayload.build(row.review,patient,row.encounter,doctor,Clock.systemUTC());}}
        }
    }
    private void reserve(Row row,boolean update,ObjectNode payload)throws Exception{
        try(Connection c=koneksiDB.bukaKoneksiTransaksi()){
            c.setAutoCommit(false);try{
                requireMigration(c);checkSource(c,row,true);
                try(PreparedStatement s=c.prepareStatement("select revisi,review_json from gizi_instruksi_diet where id_instruksi=? for update")){s.setString(1,row.id);try(ResultSet r=s.executeQuery()){require(r.next()&&r.getInt(1)==row.revision&&M.readTree(r.getString(2)).equals(row.review),"Instruksi berubah; muat ulang sebelum kirim.");}}
                try(PreparedStatement s=c.prepareStatement("insert ignore into satu_sehat_nutrition_order(id_instruksi,organisasi,endpoint_hash,endpoint,diubah_pada) values(?,?,?,?,now())")){s.setString(1,row.id);s.setString(2,org);s.setString(3,endpointHash);s.setString(4,endpoint);s.executeUpdate();}
                try(PreparedStatement s=c.prepareStatement("select * from satu_sehat_nutrition_order where id_instruksi=? and organisasi=? and endpoint_hash=? for update")){bind(s,row);try(ResultSet r=s.executeQuery()){require(r.next(),"Status sinkronisasi tidak tersedia.");require(!Arrays.asList("SENDING","UNKNOWN").contains(r.getString("sync_state")),"Hasil pengiriman belum pasti. Rekonsiliasi sebelum mengirim ulang.");String remote=value(r,"id_nutrition_order");require(update?!remote.isEmpty():remote.isEmpty(),update?"Belum ada ID NutritionOrder; gunakan Kirim.":"ID NutritionOrder sudah ada; gunakan Update.");require(!update||remote.equals(row.remote),"ID server berubah; muat ulang.");}}
                try(PreparedStatement s=c.prepareStatement("update satu_sehat_nutrition_order set sync_state='SENDING',revisi_dikirim=?,payload_hash=?,payload_dikirim=?,pesan='',diubah_pada=now() where id_instruksi=? and organisasi=? and endpoint_hash=?")){s.setInt(1,row.revision);s.setString(2,SatuSehatNutritionOrderPayload.hash(payload.toString()));s.setString(3,payload.toString());s.setString(4,row.id);s.setString(5,org);s.setString(6,endpointHash);require(s.executeUpdate()==1,"Gagal mengunci pengiriman.");}
                c.commit();
            }catch(Exception e){c.rollback();throw e;}
        }
    }
    private void bind(PreparedStatement s,Row row)throws Exception{s.setString(1,row.id);s.setString(2,org);s.setString(3,endpointHash);}
    private void outcome(Row row,String state,String remote,String message)throws Exception{
        try(Connection c=koneksiDB.bukaKoneksiTransaksi();PreparedStatement s=c.prepareStatement("update satu_sehat_nutrition_order set sync_state=?,id_nutrition_order=?,revisi_terkirim=case when ?='SENT' then revisi_dikirim else revisi_terkirim end,pesan=?,diubah_pada=now() where id_instruksi=? and organisasi=? and endpoint_hash=? and sync_state='SENDING' and revisi_dikirim=?")){s.setString(1,state);s.setString(2,remote);s.setString(3,state);s.setString(4,message.substring(0,Math.min(255,message.length())));s.setString(5,row.id);s.setString(6,org);s.setString(7,endpointHash);s.setInt(8,row.revision);require(s.executeUpdate()==1,"Status hasil belum tersimpan atau revisi berubah; muat ulang untuk rekonsiliasi.");row.state=state;row.remote=remote;if("SENT".equals(state))row.sentRevision=row.revision;}
    }
    private void verifyRemote(ObjectNode payload,JsonNode remote){
        SatuSehatNutritionOrderPayload.verifyUpdate(payload,remote);
    }
    public String send(Row row,boolean update)throws Exception{
        require(canSend(),"Memerlukan akses pengiriman Diet SATUSEHAT.");config();
        require(!Arrays.asList("SENDING","UNKNOWN","SOURCE_CHANGED").contains(row.state),"Perlu rekonsiliasi atau tinjau ulang sumber sebelum pengiriman.");
        SatuSehatNutritionOrderPayload.build(row.review,"contoh-patient",row.encounter,"contoh-practitioner",Clock.systemUTC());
        ApiSatuSehat api=new ApiSatuSehat();HttpHeaders h=headers(api);ObjectNode local=prepare(row,api,h),payload=local;
        if(update){JsonNode remote=get(api,h,"/"+SatuSehatNutritionOrderPayload.ref("NutritionOrder",row.remote));verifyRemote(local,remote);require(!"entered-in-error".equals(remote.path("status").asText()),"Instruksi server salah entri; perlu ditinjau.");String version=remote.path("meta").path("versionId").asText();require(version.matches("[A-Za-z0-9.-]{1,128}"),"Versi server tidak tersedia; Update ditahan.");h.set("If-Match","W/\""+version+"\"");payload=SatuSehatNutritionOrderPayload.mergeUpdate(local,(ObjectNode)remote);payload.put("id",row.remote);}
        reserve(row,update,local);
        try{
            String response=api.getRest().exchange(endpoint+"/NutritionOrder"+(update?"/"+row.remote:""),update?HttpMethod.PUT:HttpMethod.POST,new HttpEntity<String>(payload.toString(),h),String.class).getBody();
            JsonNode result=M.readTree(response);String id=result.path("id").asText();SatuSehatNutritionOrderPayload.ref("NutritionOrder",id);require(!update||id.equals(row.remote),"ID hasil Update tidak sesuai.");verifyRemote(local,result);outcome(row,"SENT",id,"Berhasil");return "NutritionOrder berhasil disimpan di SATUSEHAT.";
        }catch(HttpStatusCodeException e){
            int code=e.getStatusCode().value();String state=code>=400&&code<500&&code!=408&&code!=409?"FAILED":"UNKNOWN";String message="HTTP "+code+" "+SatuSehatNutritionOrderPayload.safeError(e.getResponseBodyAsString());
            try{outcome(row,state,row.remote,message);}catch(Exception ignored){}throw new IllegalStateException(message+". Muat ulang status sebelum tindakan berikutnya.");
        }catch(Exception e){try{outcome(row,"UNKNOWN",row.remote,"Hasil belum pasti; perlu rekonsiliasi.");}catch(Exception ignored){}throw new IllegalStateException("Hasil pengiriman belum pasti. Jangan kirim ulang; rekonsiliasi ID dari monitoring SATUSEHAT.");}
    }
    /** Manual recovery of an uncertain send; GET only and exact submitted clinical content check. */
    public String reconcile(Row row,String remoteId)throws Exception{
        require(canSend(),"Memerlukan akses pengiriman Diet.");config();SatuSehatNutritionOrderPayload.ref("NutritionOrder",remoteId);
        ApiSatuSehat api=new ApiSatuSehat();HttpHeaders h=headers(api);JsonNode remote=get(api,h,"/NutritionOrder/"+remoteId);
        try(Connection c=koneksiDB.bukaKoneksiTransaksi()){
            c.setAutoCommit(false);try{
                requireMigration(c);try(PreparedStatement s=c.prepareStatement("select * from satu_sehat_nutrition_order where id_instruksi=? and organisasi=? and endpoint_hash=? for update")){bind(s,row);try(ResultSet r=s.executeQuery()){require(r.next(),"Belum ada percobaan kirim yang dapat direkonsiliasi.");require(Arrays.asList("SENDING","UNKNOWN").contains(r.getString("sync_state")),"Rekonsiliasi hanya untuk hasil kirim yang belum pasti.");JsonNode sent=M.readTree(value(r,"payload_dikirim"));verifyRemote((ObjectNode)sent,remote);for(String key:new String[]{"status","oralDiet"})require(sent.path(key).equals(remote.path(key)),"Isi server berbeda dari payload percobaan; perlu tinjauan manual.");require(r.getInt("revisi_dikirim")==row.revision,"Revisi berbeda; muat ulang.");}}
                try(PreparedStatement s=c.prepareStatement("update satu_sehat_nutrition_order set sync_state='SENT',id_nutrition_order=?,revisi_terkirim=revisi_dikirim,pesan='ID direkonsiliasi',diubah_pada=now() where id_instruksi=? and organisasi=? and endpoint_hash=?")){s.setString(1,remoteId);s.setString(2,row.id);s.setString(3,org);s.setString(4,endpointHash);s.executeUpdate();}c.commit();row.remote=remoteId;row.state="SENT";row.sentRevision=row.revision;
            }catch(Exception e){c.rollback();throw e;}
        }return "ID NutritionOrder berhasil direkonsiliasi tanpa POST ulang.";
    }
}
