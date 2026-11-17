import bridging.SatuSehatAncSender;
import bridging.SatuSehatEpisodeSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.*;

/** Synthetic fixtures only: no database, credentials, or real HTTP. */
public class SatuSehatAncSenderTest {
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    interface Run {void run()throws Exception;}
    static void fails(Run run)throws Exception{try{run.run();}catch(Exception expected){return;}throw new AssertionError("Expected refusal");}
    static class Fixture implements SatuSehatAncSender.Http,SatuSehatAncSender.Store {
        SatuSehatAncSender.Data d=new SatuSehatAncSender.Data();
        ObjectNode encounter=SatuSehatAncSender.JSON.createObjectNode();
        ObjectNode episode;
        List<JsonNode> matches=new ArrayList<JsonNode>();
        String saved="",version="";boolean pending,postFailure,putFailure;int posts,puts;
        Fixture(){
            d.visit="test-visit";d.rm="test-rm";d.nik="0000000000000000";d.hpht="2020-06-01";
            d.date="2020-07-01";d.status="active";d.category="K1M";d.identifier="test-anc";
            d.remote="";d.encounter="test-encounter";d.org="test-organization";
            encounter.put("resourceType","Encounter");encounter.put("id",d.encounter);encounter.put("status","finished");
            encounter.putObject("meta").put("versionId","v1");
            encounter.putObject("subject").put("reference","Patient/test-patient");
            encounter.putObject("serviceProvider").put("reference","Organization/"+d.org);
            encounter.putObject("period").put("start","2020-07-01T00:00:00Z").put("end","2020-07-01T01:00:00Z");
            encounter.putArray("diagnosis").addObject().putObject("condition").put("reference","Condition/test-condition");
            encounter.putArray("identifier").addObject().put("system","urn:test:visit").put("value","test-visit");
            encounter.putArray("episodeOfCare").addObject().put("reference","EpisodeOfCare/test-ptm");
            episode=SatuSehatEpisodeSupport.buildAnc(SatuSehatAncSender.JSON,d.org,"test-patient",d.identifier,d.hpht);
            episode.put("id","test-anc-remote");
        }
        public JsonNode get(String path){
            if(path.startsWith("/Patient?")){
                ObjectNode bundle=SatuSehatAncSender.JSON.createObjectNode();bundle.put("resourceType","Bundle");bundle.put("total",1);
                bundle.putArray("entry").addObject().putObject("resource").put("resourceType","Patient").put("id","test-patient");return bundle;
            }
            if(path.startsWith("/Encounter/"))return encounter.deepCopy();
            return episode.deepCopy();
        }
        public List<JsonNode> search(String path){return matches;}
        public JsonNode post(ObjectNode payload)throws Exception{
            posts++;check(payload.path("identifier").get(0).path("system").asText().equals("http://sys-ids.kemkes.go.id/episode-of-care/test-organization"),"Configured organization");
            check(payload.path("period").path("start").asText().equals("2020-06-01T00:00:00Z"),"HPHT preserved, full datetime");
            if(postFailure)throw new java.io.IOException("simulated uncertain response");
            return episode.deepCopy();
        }
        public JsonNode put(String path,ObjectNode payload,String v)throws Exception{
            puts++;version=v;check(saved.equals("test-anc-remote")||!d.remote.isEmpty(),"ID must be durable before PUT");
            if(putFailure)throw new java.io.IOException("simulated PUT failure");
            encounter=payload.deepCopy();return encounter.deepCopy();
        }
        public void saveId(String id){saved=id;}
        public boolean pending(){return pending;}
        public void beginPost(){pending=true;}
        public void clearPending(){pending=false;}
        void run()throws Exception{SatuSehatAncSender.sync(d,this,this);}
    }
    public static void main(String[] args)throws Exception{
        Fixture first=new Fixture();ObjectNode original=first.encounter.deepCopy();first.run();
        check(first.posts==1&&first.puts==1&&!first.pending,"POST and link");check(first.version.equals("v1"),"Version passed for conditional PUT");
        check(SatuSehatAncSender.hasLink(first.encounter,"test-anc-remote","K1M"),"ANC link and category");
        check(first.encounter.path("episodeOfCare").get(0).equals(original.path("episodeOfCare").get(0)),"PTM retained");
        for(String field:Arrays.asList("period","diagnosis","status","subject","serviceProvider"))check(first.encounter.path(field).equals(original.path(field)),"Retain "+field);
        check(first.encounter.path("identifier").get(0).equals(original.path("identifier").get(0)),"Other identifiers retained");
        first.d.remote=first.saved;first.run();check(first.posts==1&&first.puts==1,"Repeat no POST/PUT");
        Fixture recovery=new Fixture();recovery.pending=true;recovery.matches.add(recovery.episode);recovery.run();check(recovery.posts==0&&!recovery.pending,"Recover existing ID without POST");
        Fixture wrongPatient=new Fixture();wrongPatient.encounter.with("subject").put("reference","Patient/other");fails(()->wrongPatient.run());check(wrongPatient.posts==0,"Wrong patient blocked");
        Fixture wrongOrg=new Fixture();wrongOrg.encounter.with("serviceProvider").put("reference","Organization/other");fails(()->wrongOrg.run());check(wrongOrg.posts==0,"Wrong org blocked");
        Fixture wrongHpht=new Fixture();wrongHpht.episode.with("period").put("start","2020-05-01T00:00:00Z");wrongHpht.matches.add(wrongHpht.episode);fails(()->wrongHpht.run());check(wrongHpht.posts==0,"Different pregnancy blocked");
        Fixture duplicate=new Fixture();duplicate.matches.add(duplicate.episode);ObjectNode other=duplicate.episode.deepCopy();other.put("id","other");duplicate.matches.add(other);fails(()->duplicate.run());check(duplicate.posts==0,"Ambiguous episodes blocked");
        Fixture pending=new Fixture();pending.pending=true;fails(()->pending.run());check(pending.posts==0,"Uncertain previous POST blocked");
        Fixture timeout=new Fixture();timeout.postFailure=true;fails(()->timeout.run());check(timeout.pending,"Timeout keeps hold");fails(()->timeout.run());check(timeout.posts==1,"Timeout does not repost");
        Fixture partial=new Fixture();partial.putFailure=true;fails(()->partial.run());check(partial.saved.equals("test-anc-remote")&&!partial.pending,"ID saved on partial failure");partial.d.remote=partial.saved;partial.putFailure=false;partial.run();check(partial.posts==1&&partial.puts==2,"Retry only links");
        Fixture invalidDate=new Fixture();invalidDate.d.hpht="2099-01-01";fails(()->invalidDate.run());check(invalidDate.posts==0,"Future date blocked");
        Fixture invalidCategory=new Fixture();invalidCategory.d.category="UNKNOWN";fails(()->invalidCategory.run());check(invalidCategory.posts==0,"Invalid category blocked");
        Fixture mismatchId=new Fixture();mismatchId.d.remote="other-id";fails(()->mismatchId.run());check(mismatchId.puts==0,"Mismatched GET ID blocked");
        System.out.println("PASS: 13 ANC scenarios; synthetic data only, no DB/API.");
    }
}
