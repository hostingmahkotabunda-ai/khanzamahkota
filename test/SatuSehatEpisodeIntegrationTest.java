import bridging.SatuSehatEpisodeSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import static org.junit.Assert.*;

/** Uji payload murni: tidak membuat koneksi database atau request SATUSEHAT. */
public class SatuSehatEpisodeIntegrationTest {
    private final ObjectMapper m=new ObjectMapper();
    private ObjectNode encounter()throws Exception{
        return (ObjectNode)m.readTree("{\"resourceType\":\"Encounter\",\"subject\":{\"reference\":\"Patient/test-patient\"},"
                +"\"serviceProvider\":{\"reference\":\"Organization/test-org\"},\"identifier\":[{\"system\":\"encounter-test\",\"value\":\"visit-1\"}]}");
    }
    @Test public void ancPayloadUsesConfiguredOrganizationAndHpht(){
        ObjectNode e=SatuSehatEpisodeSupport.buildAnc(m,"test-org","test-patient","ANC-test","2026-04-10");
        assertEquals("Organization/test-org",e.path("managingOrganization").path("reference").asText());
        assertEquals("http://sys-ids.kemkes.go.id/episode-of-care/test-org",e.path("identifier").get(0).path("system").asText());
        assertEquals("2026-04-10T00:00:00Z",e.path("period").path("start").asText());
        assertTrue(SatuSehatEpisodeSupport.matchesEpisode(e,"test-patient","test-org","ANC"));
        assertFalse(SatuSehatEpisodeSupport.matchesEpisode(e,"test-patient","test-org","CAD"));
    }
    @Test public void ptmAndAncCoexistWhenEncounterIsUpdated()throws Exception{
        ObjectNode remote=encounter();SatuSehatEpisodeSupport.addEpisode(remote,"ptm-1");
        SatuSehatEpisodeSupport.setAncIdentifier(remote,"K2");
        ObjectNode local=encounter();SatuSehatEpisodeSupport.addEpisode(local,"anc-1");
        SatuSehatEpisodeSupport.setAncIdentifier(local,"K3");
        SatuSehatEpisodeSupport.preserveRemoteLinks(local,remote);
        assertEquals(2,local.path("episodeOfCare").size());
        assertEquals(2,local.path("identifier").size());
        assertEquals("K3",local.path("identifier").get(1).path("value").asText());
        assertEquals("K2",remote.path("identifier").get(1).path("value").asText());
    }
    @Test public void linkingPtmDoesNotChangeAncIdentifierOrOtherFields()throws Exception{
        ObjectNode e=encounter();e.put("status","finished");
        SatuSehatEpisodeSupport.setAncIdentifier(e,"K1M");SatuSehatEpisodeSupport.addEpisode(e,"anc-1");
        SatuSehatEpisodeSupport.addEpisode(e,"ptm-1");
        assertEquals("finished",e.path("status").asText());assertEquals("K1M",e.path("identifier").get(1).path("value").asText());
        assertEquals(2,e.path("episodeOfCare").size());
    }
    @Test public void retryDoesNotDuplicateEpisodesOrAncCodes()throws Exception{
        ObjectNode local=encounter(),remote=encounter();
        for(int n=0;n<3;n++){
            SatuSehatEpisodeSupport.addEpisode(local,"anc-1");SatuSehatEpisodeSupport.addEpisode(remote,"anc-1");
            SatuSehatEpisodeSupport.setAncIdentifier(local,"K4");SatuSehatEpisodeSupport.preserveRemoteLinks(local,remote);
        }
        assertEquals(1,local.path("episodeOfCare").size());assertEquals(2,local.path("identifier").size());
    }
    @Test public void unrelatedIdentifiersSurviveAndNoLocalAncDoesNotEraseRemoteAnc()throws Exception{
        ObjectNode local=encounter(),remote=encounter();SatuSehatEpisodeSupport.setAncIdentifier(remote,"K2");
        remote.withArray("identifier").addObject().put("system","other-service").put("value","other-id");
        SatuSehatEpisodeSupport.preserveRemoteLinks(local,remote);
        assertEquals(3,local.path("identifier").size());assertFalse(local.has("episodeOfCare"));
    }
    @Test public void exactEpisodeIdMatchingDoesNotConfusePrefixes()throws Exception{
        ObjectNode e=encounter();SatuSehatEpisodeSupport.addEpisode(e,"episode-1");SatuSehatEpisodeSupport.addEpisode(e,"episode-10");
        assertEquals(2,e.path("episodeOfCare").size());
    }
    @Test(expected=IllegalArgumentException.class) public void rejectAnotherPatientOnEncounterUpdate()throws Exception{
        ObjectNode local=encounter(),remote=encounter();remote.with("subject").put("reference","Patient/other");
        SatuSehatEpisodeSupport.preserveRemoteLinks(local,remote);
    }
    @Test(expected=IllegalArgumentException.class) public void rejectAnotherOrganizationOnEncounterUpdate()throws Exception{
        ObjectNode local=encounter(),remote=encounter();remote.with("serviceProvider").put("reference","Organization/other");
        SatuSehatEpisodeSupport.preserveRemoteLinks(local,remote);
    }
    @Test public void episodeSearchCannotReuseAnotherPregnancyOrOrganization(){
        ObjectNode e=SatuSehatEpisodeSupport.buildAnc(m,"test-org","test-patient","ANC-test","2026-04-10");
        assertFalse(SatuSehatEpisodeSupport.matchesEpisode(e,"test-patient","other-org","ANC"));
        assertFalse(SatuSehatEpisodeSupport.matchesEpisode(e,"other-patient","test-org","ANC"));
        assertFalse(SatuSehatEpisodeSupport.sameHpht(e,"2025-04-10"));
        assertTrue(SatuSehatEpisodeSupport.sameHpht(e,"2026-04-10"));
    }
    @Test(expected=IllegalArgumentException.class) public void rejectInvalidAncCode(){SatuSehatEpisodeSupport.validateAncCode("K7");}
    @Test(expected=IllegalArgumentException.class) public void rejectUnsafeEpisodeId()throws Exception{SatuSehatEpisodeSupport.addEpisode(encounter(),"bad\"id");}
    @Test public void allSupportedAncCategoriesAreAccepted(){for(String code:new String[]{"K1A","K1M","K2","K3","K4","K5","K6"})SatuSehatEpisodeSupport.validateAncCode(code);}

    @Test public void separateServicePreservesSamePtmAncLinksAsMainApplication()throws Exception{
        ObjectNode remote=encounter();SatuSehatEpisodeSupport.addEpisode(remote,"ptm-1");SatuSehatEpisodeSupport.setAncIdentifier(remote,"K2");
        ObjectNode main=encounter();SatuSehatEpisodeSupport.addEpisode(main,"anc-1");SatuSehatEpisodeSupport.setAncIdentifier(main,"K3");
        ObjectNode service=(ObjectNode)m.readTree(main.toString());
        SatuSehatEpisodeSupport.preserveRemoteLinks(main,remote);
        fungsi.SatuSehatEpisodeEncounterGuard.preserveRemote(service,remote);
        assertEquals(main,service);
    }

    @Test public void separateServiceRetainsRemoteAncWhenNoLocalAncMapping()throws Exception{
        ObjectNode remote=encounter();SatuSehatEpisodeSupport.addEpisode(remote,"anc-1");SatuSehatEpisodeSupport.setAncIdentifier(remote,"K6");
        ObjectNode service=encounter();fungsi.SatuSehatEpisodeEncounterGuard.preserveRemote(service,remote);
        assertEquals(remote,service);
    }

    @Test(expected=Exception.class) public void separateServiceRejectsAnotherPatient()throws Exception{
        ObjectNode remote=encounter();remote.with("subject").put("reference","Patient/other");
        fungsi.SatuSehatEpisodeEncounterGuard.preserveRemote(encounter(),remote);
    }
}
