package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fungsi.koneksiDB;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;

/** Explicit ANC send followed by a version-checked Encounter link. No HTTP on construction. */
public final class SatuSehatAncSender {
    public static final ObjectMapper JSON=new ObjectMapper();
    public static final class Data {
        public String visit,rm,nik,hpht,category,date,status,identifier,remote,encounter,org,endpoint;
        public long local;
    }
    public interface Http {
        JsonNode get(String path)throws Exception;
        List<JsonNode> search(String path)throws Exception;
        JsonNode post(ObjectNode payload)throws Exception;
        JsonNode put(String path,ObjectNode payload,String version)throws Exception;
    }
    public interface Store {
        void saveId(String id)throws Exception;
        boolean pending()throws Exception;
        void beginPost()throws Exception;
        void clearPending()throws Exception;
    }
    public static String sync(Data d,Http http,Store store)throws Exception{
        require(d.nik!=null&&d.nik.matches("[0-9]{16}"),"NIK pasien belum valid untuk pencarian IHS.");
        require("active".equals(d.status),"Episode ANC sudah tidak aktif; tinjau episode kehamilan.");
        SatuSehatEpisodeSupport.validateAncCode(d.category);
        SatuSehatEpisodePayload.dateTime(d.hpht,"HPHT",Clock.systemUTC());
        require(!LocalDate.parse(d.hpht).isAfter(LocalDate.parse(d.date)),"HPHT melewati tanggal kunjungan.");
        validId(d.encounter);SatuSehatEpisodePayload.identifierSystem(d.org);
        JsonNode bundle=http.get("/Patient?identifier="+enc("https://fhir.kemkes.go.id/id/nik|"+d.nik));
        require("Bundle".equals(bundle.path("resourceType").asText())&&bundle.path("entry").size()==1&&(!bundle.has("total")||bundle.path("total").asInt()==1),"IHS pasien tidak ditemukan atau ambigu.");
        JsonNode patient=bundle.path("entry").get(0).path("resource");
        require("Patient".equals(patient.path("resourceType").asText()),"Hasil pencarian bukan Patient.");
        String patientId=patient.path("id").asText();validId(patientId);
        verifyEncounter(http.get("/Encounter/"+d.encounter),d,patientId);
        String remote=d.remote==null?"":d.remote;
        if(!remote.isEmpty()){
            validId(remote);JsonNode existing=http.get("/EpisodeOfCare/"+remote);verifyEpisode(existing,d,patientId);
            require(remote.equals(existing.path("id").asText()),"ID episode pada respons berbeda; pengiriman dihentikan.");
        }else{
            List<JsonNode> matches=http.search("/EpisodeOfCare?patient="+enc(patientId)+"&type=ANC&status=active&organization="+enc(d.org));
            for(JsonNode item:matches){
                verifyEpisode(item,d,patientId);
                String found=item.path("id").asText();validId(found);
                require(remote.isEmpty()||remote.equals(found),"Lebih dari satu episode ANC aktif ditemukan. Tinjau duplikasi.");remote=found;
            }
            if(remote.isEmpty()){
                require(!store.pending(),"Hasil kirim sebelumnya belum pasti. Periksa log SATUSEHAT; POST ulang ditahan sampai ID dapat direkonsiliasi.");
                ObjectNode payload=SatuSehatEpisodeSupport.buildAnc(JSON,d.org,patientId,d.identifier,d.hpht);
                store.beginPost();
                JsonNode created;
                try{created=http.post(payload);}catch(HttpStatusCodeException e){
                    int code=e.getStatusCode().value();
                    if(code>=400&&code<500&&code!=408&&code!=409&&code!=429)store.clearPending();
                    throw e;
                }
                verifyEpisode(created,d,patientId);remote=created.path("id").asText();validId(remote);
            }
            // Persist before Encounter PUT: a partial failure must not create another episode.
            store.saveId(remote);store.clearPending();
        }
        JsonNode latest=http.get("/Encounter/"+d.encounter);verifyEncounter(latest,d,patientId);
        ObjectNode updated=((ObjectNode)latest).deepCopy();
        SatuSehatEpisodeSupport.addEpisode(updated,remote);
        SatuSehatEpisodeSupport.setAncIdentifier(updated,d.category);
        if(!updated.equals(latest)){
            String version=latest.path("meta").path("versionId").asText();
            require(version.matches("[A-Za-z0-9.-]{1,128}"),"ID episode tersimpan. Versi Encounter tidak tersedia; pembaruan ditahan.");
            JsonNode result=http.put("/Encounter/"+d.encounter,updated,version);
            verifyEncounter(result,d,patientId);
            require(hasLink(result,remote,d.category),"ID episode tersimpan; respons tautan Encounter belum sesuai. Muat ulang dan coba hubungkan kembali.");
        }
        return "Episode ANC berhasil tersimpan dan terhubung ke Encounter.";
    }
    public static boolean hasLink(JsonNode encounter,String id,String category){
        boolean episode=false,anc=false;
        for(JsonNode link:encounter.path("episodeOfCare"))if(("EpisodeOfCare/"+id).equals(link.path("reference").asText()))episode=true;
        for(JsonNode item:encounter.path("identifier"))if(SatuSehatEpisodeSupport.ANC_IDENTIFIER.equals(item.path("system").asText())&&category.equals(item.path("value").asText()))anc=true;
        return episode&&anc;
    }
    private static void verifyEncounter(JsonNode resource,Data d,String patient){
        require(resource.isObject()&&"Encounter".equals(resource.path("resourceType").asText())&&d.encounter.equals(resource.path("id").asText()),"Resource Encounter tidak sesuai.");
        require(("Patient/"+patient).equals(resource.path("subject").path("reference").asText()),"Pasien pada Encounter berbeda; pengiriman dihentikan.");
        require(("Organization/"+d.org).equals(resource.path("serviceProvider").path("reference").asText()),"Organisasi Encounter berbeda dari konfigurasi.");
        require(!Arrays.asList("cancelled","entered-in-error").contains(resource.path("status").asText()),"Encounter telah dibatalkan/salah entri.");
    }
    private static void verifyEpisode(JsonNode resource,Data d,String patient){
        SatuSehatEpisodeSupport.validateEpisode(resource,patient,d.org,"ANC");
        require("active".equals(resource.path("status").asText()),"Episode ANC di server tidak aktif.");
        require(SatuSehatEpisodeSupport.sameHpht(resource,d.hpht),"HPHT episode server berbeda; tinjau sebelum membuat episode baru.");
    }
    private static void require(boolean condition,String message){if(!condition)throw new IllegalArgumentException(message);}
    private static void validId(String id){require(id!=null&&id.matches("[A-Za-z0-9.-]{1,64}"),"ID referensi belum tersedia atau tidak valid.");}
    private static String enc(String value)throws Exception{return URLEncoder.encode(value,"UTF-8");}

    public String send(SatuSehatEpisodeList.Row selected)throws Exception{
        require(fungsi.akses.getsatu_sehat_kirim_encounter(),"Akses pengiriman Encounter diperlukan.");
        require("ANC".equals(selected.kind),"Pilih catatan ANC untuk pengiriman ini.");
        try(Connection c=koneksiDB.bukaKoneksiTransaksi()){
            c.setAutoCommit(false);
            try{
                Data d=new Data();
                try(PreparedStatement p=c.prepareStatement("select e.id_episode_kehamilan,e.no_rkm_medis,e.hpht,e.status,e.identifier_lokal,ifnull(e.id_episode,'') remote,k.status_kunjungan,r.tgl_registrasi,p.no_ktp,ifnull(s.id_encounter,'') encounter from satu_sehat_kunjungan_anc k join satu_sehat_episode_kehamilan e on e.id_episode_kehamilan=k.id_episode_kehamilan join reg_periksa r on r.no_rawat=k.no_rawat and r.no_rkm_medis=e.no_rkm_medis join pasien p on p.no_rkm_medis=r.no_rkm_medis left join satu_sehat_encounter s on s.no_rawat=r.no_rawat where k.no_rawat=? for update")){
                    p.setString(1,selected.visit);try(ResultSet r=p.executeQuery()){
                        require(r.next(),"Catatan ANC tidak ditemukan; muat ulang.");
                        d.visit=selected.visit;d.local=r.getLong(1);d.rm=r.getString(2);d.hpht=r.getString(3);d.status=r.getString(4);d.identifier=r.getString(5);d.remote=r.getString(6);d.category=r.getString(7);d.date=r.getString(8);d.nik=r.getString(9);d.encounter=r.getString(10);
                    }
                }
                require(d.rm.equals(selected.rm)&&d.hpht.equals(selected.start)&&d.category.equals(selected.category)&&d.encounter.equals(selected.encounter)&&d.status.equals(selected.status),"Catatan berubah sejak ditampilkan; muat ulang sebelum mengirim.");
                d.org=koneksiDB.IDSATUSEHAT();d.endpoint=koneksiDB.URLFHIRSATUSEHAT().replaceAll("/+$","");
                require(d.endpoint.startsWith("https://"),"Endpoint SATUSEHAT harus HTTPS.");
                final Path marker=Paths.get("cache","satusehat-episode-pending",SatuSehatNutritionOrderPayload.hash(d.endpoint+"|"+d.org+"|"+d.identifier)+".pending");
                Store store=new Store(){
                    public boolean pending(){return Files.exists(marker);}
                    public void beginPost()throws Exception{Files.createDirectories(marker.getParent());Files.write(marker,"UNKNOWN".getBytes(StandardCharsets.UTF_8),StandardOpenOption.CREATE_NEW);}
                    public void clearPending()throws Exception{Files.deleteIfExists(marker);}
                    public void saveId(String id)throws Exception{
                        try(PreparedStatement p=c.prepareStatement("update satu_sehat_episode_kehamilan set id_episode=? where id_episode_kehamilan=? and (id_episode is null or id_episode='' or id_episode=?)")){
                            p.setString(1,id);p.setLong(2,d.local);p.setString(3,id);require(p.executeUpdate()==1,"ID episode diterima tetapi penyimpanan lokal belum berhasil. Jangan POST ulang.");
                        }
                        c.commit();
                    }
                };
                Http http=new Http(){
                    final ApiSatuSehat api=new ApiSatuSehat();
                    HttpHeaders headers()throws Exception{HttpHeaders h=new HttpHeaders();h.setContentType(MediaType.APPLICATION_JSON);h.add("Authorization","Bearer "+api.TokenSatuSehat());return h;}
                    public JsonNode get(String path)throws Exception{return JSON.readTree(api.getRest().exchange(URI.create(d.endpoint+path),HttpMethod.GET,new HttpEntity<String>(headers()),String.class).getBody());}
                    public List<JsonNode> search(String path)throws Exception{return SatuSehatEpisodeSupport.search(api,d.endpoint,path,JSON);}
                    public JsonNode post(ObjectNode payload)throws Exception{return JSON.readTree(api.getRest().exchange(d.endpoint+"/EpisodeOfCare",HttpMethod.POST,new HttpEntity<String>(payload.toString(),headers()),String.class).getBody());}
                    public JsonNode put(String path,ObjectNode payload,String version)throws Exception{HttpHeaders h=headers();h.add("If-Match","W/\""+version+"\"");return JSON.readTree(api.getRest().exchange(d.endpoint+path,HttpMethod.PUT,new HttpEntity<String>(payload.toString(),h),String.class).getBody());}
                };
                String message=sync(d,http,store);c.commit();return message;
            }catch(Exception e){c.rollback();throw e;}
        }
    }
    public static String error(Throwable error){
        while(error instanceof java.util.concurrent.ExecutionException&&error.getCause()!=null)error=error.getCause();
        if(error instanceof HttpStatusCodeException){
            HttpStatusCodeException e=(HttpStatusCodeException)error;StringBuilder out=new StringBuilder("SATUSEHAT HTTP "+e.getStatusCode().value()+". ");
            try{for(JsonNode issue:JSON.readTree(e.getResponseBodyAsString()).path("issue")){
                for(JsonNode field:issue.path("expression"))if(field.asText().matches("(?:EpisodeOfCare|Encounter|Patient)(?:[.][A-Za-z][A-Za-z0-9]*|\\[[0-9]+\\]){0,30}"))out.append(field.asText()).append(' ');
                java.util.regex.Matcher m=java.util.regex.Pattern.compile("RuleNumber\\s*:?\\s*([0-9]{1,8})").matcher(issue.path("details").path("text").asText());while(m.find())out.append("Rule ").append(m.group(1)).append(' ');
            }}catch(Exception ignored){}
            return out.append("Periksa rincian pada monitoring SATUSEHAT; muat ulang status sebelum mencoba kembali.").toString();
        }
        if(error instanceof IllegalArgumentException)return error.getMessage();
        return "Proses belum selesai. Periksa koneksi/penyimpanan lalu muat ulang. Hasil POST yang belum pasti ditahan agar tidak terkirim ganda.";
    }
}
