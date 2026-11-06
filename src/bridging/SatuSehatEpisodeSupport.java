package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import org.springframework.http.*;

/** Penggabungan PTM/ANC tanpa menghapus identifier atau episode dari alur lainnya. */
public final class SatuSehatEpisodeSupport {
    public static final String ANC_IDENTIFIER="http://terminology.kemkes.go.id/CodeSystem/episodeofcare/ANC";
    public static final String TYPE_SYSTEM="http://terminology.kemkes.go.id/CodeSystem/episodeofcare-type";
    private SatuSehatEpisodeSupport() {}

    public static void validateAncCode(String code) {
        if(!Arrays.asList("K1A","K1M","K2","K3","K4","K5","K6").contains(code))
            throw new IllegalArgumentException("Kategori kunjungan ANC belum valid.");
    }

    public static boolean matchesEpisode(JsonNode resource,String patient,String organization,String type) {
        if(!"EpisodeOfCare".equals(resource.path("resourceType").asText())
                ||!("Patient/"+patient).equals(resource.path("patient").path("reference").asText())
                ||!("Organization/"+organization).equals(resource.path("managingOrganization").path("reference").asText()))return false;
        for(JsonNode item:resource.path("type"))for(JsonNode coding:item.path("coding"))
            if(TYPE_SYSTEM.equals(coding.path("system").asText())&&type.equals(coding.path("code").asText()))return true;
        return false;
    }

    public static void validateEpisode(JsonNode resource,String patient,String organization,String type) {
        if(!matchesEpisode(resource,patient,organization,type))
            throw new IllegalArgumentException("EpisodeOfCare tidak sesuai pasien, organisasi, atau tipe "+type+".");
    }

    public static boolean sameHpht(JsonNode resource,String hpht) {
        String start=resource.path("period").path("start").asText();
        try{return SatuSehatEpisodePayload.clinicalDate(start).equals(hpht);}
        catch(IllegalArgumentException e){return false;}
    }

    public static ObjectNode buildAnc(ObjectMapper mapper,String organization,String patient,String identifier,String hpht) {
        return SatuSehatEpisodePayload.post(mapper,organization,patient,identifier,"ANC","Antenatal Care","active",hpht,"","",java.time.Clock.systemUTC());
    }

    public static void addEpisode(ObjectNode encounter,String id) {
        if(!id.matches("[A-Za-z0-9.-]{1,64}"))throw new IllegalArgumentException("ID episode tidak valid.");
        ArrayNode values=array(encounter,"episodeOfCare");String ref="EpisodeOfCare/"+id;
        for(JsonNode item:values)if(ref.equals(item.path("reference").asText()))return;
        values.addObject().put("reference",ref);
    }

    private static ArrayNode array(ObjectNode node,String field) {
        if(node.path(field).isMissingNode())return node.putArray(field);
        if(!node.path(field).isArray())throw new IllegalArgumentException("Elemen "+field+" bukan array.");
        return (ArrayNode)node.path(field);
    }

    public static void setAncIdentifier(ObjectNode encounter,String code) {
        validateAncCode(code);ArrayNode values=array(encounter,"identifier");
        for(int n=values.size()-1;n>=0;n--)if(ANC_IDENTIFIER.equals(values.get(n).path("system").asText()))values.remove(n);
        ObjectNode id=values.addObject();id.put("system",ANC_IDENTIFIER);id.put("value",code);
    }

    /** Auto Encounter tidak membuat episode ANC; episode yang belum siap harus dikirim lewat dialog Encounter. */
    public static void applyLocalLinks(Connection db,ObjectNode encounter,String noRawat) throws Exception {
        try(PreparedStatement p=db.prepareStatement("select e.id_episode,e.no_rkm_medis,r.no_rkm_medis as rm_kunjungan from satu_sehat_encounter_episode k join satu_sehat_episode_of_care e on e.id_episode=k.id_episode join reg_periksa r on r.no_rawat=k.no_rawat where k.no_rawat=?")){
            p.setString(1,noRawat);try(ResultSet r=p.executeQuery()){while(r.next()){
                if(!r.getString("no_rkm_medis").equals(r.getString("rm_kunjungan")))throw new Exception("Episode PTM tidak sesuai pasien kunjungan.");
                addEpisode(encounter,r.getString("id_episode"));
            }}
        }catch(SQLException e){if(e.getErrorCode()!=1146&&!"42S02".equals(e.getSQLState()))throw e;}
        try(PreparedStatement p=db.prepareStatement("select e.id_episode,e.no_rkm_medis,r.no_rkm_medis as rm_kunjungan,k.status_kunjungan from satu_sehat_kunjungan_anc k join satu_sehat_episode_kehamilan e on e.id_episode_kehamilan=k.id_episode_kehamilan join reg_periksa r on r.no_rawat=k.no_rawat where k.no_rawat=?")){
            p.setString(1,noRawat);try(ResultSet r=p.executeQuery()){if(r.next()){
                if(!r.getString("no_rkm_medis").equals(r.getString("rm_kunjungan")))throw new Exception("Episode ANC tidak sesuai pasien kunjungan.");
                String id=r.getString("id_episode");
                if(id==null||id.isEmpty())throw new Exception("Episode ANC belum dikirim. Gunakan Kirim/Update pada dialog Encounter terlebih dahulu.");
                addEpisode(encounter,id);setAncIdentifier(encounter,r.getString("status_kunjungan"));
            }}
        }catch(SQLException e){if(e.getErrorCode()!=1146&&!"42S02".equals(e.getSQLState()))throw e;}
    }

    /** Lakukan pada payload PUT yang dibangun aplikasi, memakai GET server terbaru. */
    public static void preserveRemoteLinks(ObjectNode local,JsonNode remote) {
        if(!"Encounter".equals(remote.path("resourceType").asText())
                ||!local.path("subject").path("reference").asText().equals(remote.path("subject").path("reference").asText())
                ||!local.path("serviceProvider").path("reference").asText().equals(remote.path("serviceProvider").path("reference").asText()))
            throw new IllegalArgumentException("Encounter server tidak sesuai pasien atau organisasi.");
        ArrayNode identifiers=array(local,"identifier");
        boolean localAnc=false;for(JsonNode id:identifiers)if(ANC_IDENTIFIER.equals(id.path("system").asText()))localAnc=true;
        for(JsonNode id:remote.path("identifier")){
            if(localAnc&&ANC_IDENTIFIER.equals(id.path("system").asText()))continue;
            boolean found=false;
            for(JsonNode existing:identifiers)if(existing.path("system").equals(id.path("system"))&&existing.path("value").equals(id.path("value")))found=true;
            if(!found)identifiers.add(id);
        }
        ArrayNode episodes=array(local,"episodeOfCare");
        for(JsonNode item:remote.path("episodeOfCare")){
            boolean found=false;for(JsonNode existing:episodes)if(existing.path("reference").equals(item.path("reference")))found=true;
            if(!found)episodes.add(item);
        }
        if(episodes.size()==0)local.remove("episodeOfCare");
    }

    /** GET semua halaman, hanya ke endpoint konfigurasi; gagal mencari tidak berarti boleh POST baru. */
    public static List<JsonNode> search(ApiSatuSehat api,String base,String path,ObjectMapper mapper) throws Exception {
        List<JsonNode> values=new ArrayList<>();Set<String> visited=new HashSet<>();
        String url=base+path;
        while(url!=null){
            if(!url.startsWith(base+"/")||!visited.add(url)||visited.size()>100)throw new Exception("Pagination EpisodeOfCare tidak valid.");
            HttpHeaders h=new HttpHeaders();h.add("Authorization","Bearer "+api.TokenSatuSehat());
            JsonNode bundle=mapper.readTree(api.getRest().exchange(URI.create(url),HttpMethod.GET,new HttpEntity<>(h),String.class).getBody());
            if(!"Bundle".equals(bundle.path("resourceType").asText()))throw new Exception("Pencarian tidak mengembalikan Bundle.");
            for(JsonNode entry:bundle.path("entry")){
                JsonNode item=entry.path("resource");
                if("OperationOutcome".equals(item.path("resourceType").asText()))throw new Exception(item.toString());
                values.add(item);
            }
            url=null;for(JsonNode l:bundle.path("link"))if("next".equals(l.path("relation").asText()))url=l.path("url").asText();
        }
        return values;
    }
}
