package fungsi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.sql.*;

/** Menjaga relasi PTM/ANC pada aplikasi service terpisah. Tidak membuat episode atau skema. */
public final class SatuSehatEpisodeEncounterGuard {
    private static final String ANC="http://terminology.kemkes.go.id/CodeSystem/episodeofcare/ANC";
    private SatuSehatEpisodeEncounterGuard(){}
    public static void applyLocal(Connection db,ObjectNode payload,String noRawat)throws Exception{
        String[] sql={
            "select e.id_episode,e.no_rkm_medis,r.no_rkm_medis as rm_kunjungan,'' status_kunjungan from satu_sehat_encounter_episode k join satu_sehat_episode_of_care e on e.id_episode=k.id_episode join reg_periksa r on r.no_rawat=k.no_rawat where k.no_rawat=?",
            "select e.id_episode,e.no_rkm_medis,r.no_rkm_medis as rm_kunjungan,k.status_kunjungan from satu_sehat_kunjungan_anc k join satu_sehat_episode_kehamilan e on e.id_episode_kehamilan=k.id_episode_kehamilan join reg_periksa r on r.no_rawat=k.no_rawat where k.no_rawat=?"
        };
        for(String query:sql)try(PreparedStatement p=db.prepareStatement(query)){
            p.setString(1,noRawat);try(ResultSet r=p.executeQuery()){while(r.next()){
                if(!r.getString("no_rkm_medis").equals(r.getString("rm_kunjungan")))throw new Exception("Pasien episode tidak sesuai kunjungan.");
                String id=r.getString("id_episode"),code=r.getString("status_kunjungan");
                if(id==null||!id.matches("[A-Za-z0-9.-]{1,64}"))throw new Exception("Episode belum siap; gunakan dialog Encounter untuk mengirim episode terlebih dahulu.");
                ArrayNode episodes=payload.withArray("episodeOfCare");boolean exists=false;
                for(JsonNode e:episodes)if(("EpisodeOfCare/"+id).equals(e.path("reference").asText()))exists=true;
                if(!exists)episodes.addObject().put("reference","EpisodeOfCare/"+id);
                if(code!=null&&!code.isEmpty()){
                    if(!java.util.Arrays.asList("K1A","K1M","K2","K3","K4","K5","K6").contains(code))throw new Exception("Kode ANC tidak valid.");
                    ArrayNode ids=payload.withArray("identifier");
                    for(int i=ids.size()-1;i>=0;i--)if(ANC.equals(ids.get(i).path("system").asText()))ids.remove(i);
                    ids.addObject().put("system",ANC).put("value",code);
                }
            }}
        }catch(SQLException e){if(e.getErrorCode()!=1146&&!"42S02".equals(e.getSQLState()))throw e;}
    }
    public static void preserveRemote(ObjectNode payload,JsonNode remote)throws Exception{
        if(!"Encounter".equals(remote.path("resourceType").asText())
                ||!payload.path("subject").path("reference").equals(remote.path("subject").path("reference"))
                ||!payload.path("serviceProvider").path("reference").equals(remote.path("serviceProvider").path("reference")))throw new Exception("Encounter server tidak sesuai pasien/organisasi.");
        ArrayNode ids=payload.withArray("identifier");boolean localAnc=false;
        for(JsonNode id:ids)if(ANC.equals(id.path("system").asText()))localAnc=true;
        for(JsonNode id:remote.path("identifier")){
            if(localAnc&&ANC.equals(id.path("system").asText()))continue;
            boolean exists=false;for(JsonNode current:ids)if(current.path("system").equals(id.path("system"))&&current.path("value").equals(id.path("value")))exists=true;
            if(!exists)ids.add(id);
        }
        ArrayNode episodes=payload.withArray("episodeOfCare");
        for(JsonNode episode:remote.path("episodeOfCare")){
            boolean exists=false;for(JsonNode current:episodes)if(current.path("reference").equals(episode.path("reference")))exists=true;
            if(!exists)episodes.add(episode);
        }
        if(episodes.size()==0)payload.remove("episodeOfCare");
    }
}
