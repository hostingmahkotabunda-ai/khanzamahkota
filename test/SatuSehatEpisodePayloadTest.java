import bridging.SatuSehatEpisodePayload;
import bridging.SatuSehatEpisodeSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Seluruh data sintetis; tidak menginisialisasi ApiSatuSehat atau koneksiDB. */
public class SatuSehatEpisodePayloadTest {
    private final ObjectMapper mapper=new ObjectMapper();
    private final Clock clock=Clock.fixed(Instant.parse("2026-11-03T08:00:00Z"),ZoneOffset.UTC);
    private ObjectNode post(String type,String start,String end){
        return SatuSehatEpisodePayload.post(mapper,"test-org","test-patient","test-local",type,"Test type","active",start,end,
                "ANC".equals(type)?"":"test-condition",clock);
    }
    private void fails(String field,Runnable action){
        try{action.run();fail("Expected invalid field "+field);}catch(IllegalArgumentException e){assertTrue(e.getMessage(),e.getMessage().contains(field));}
    }
    @Test public void ancPostRepairsBothProvenErrorFieldsWithoutChangingHpht(){
        ObjectNode p=post("ANC","2026-08-26","");
        assertEquals("2026-08-26T00:00:00Z",p.path("period").path("start").asText());
        assertEquals("2026-08-26T00:00:00Z",p.path("statusHistory").get(0).path("period").path("start").asText());
        assertEquals("http://sys-ids.kemkes.go.id/episode-of-care/test-org",p.path("identifier").get(0).path("system").asText());
        assertFalse(p.has("diagnosis"));
    }
    @Test public void ptmPostRetainsRegistrationTimeFromWita(){
        ObjectNode p=post("CAD","2026-08-26 09:17:25","");
        assertEquals("2026-08-26T01:17:25Z",p.path("period").path("start").asText());
        assertEquals("Condition/test-condition",p.path("diagnosis").get(0).path("condition").path("reference").asText());
    }
    @Test public void allPtmTypesUseSameDateAndIdentifierValidation(){
        for(String type:new String[]{"CAD","CVD","CKD","cancer"}){
            ObjectNode p=post(type,"2026-08-26","");
            assertEquals("2026-08-26T00:00:00Z",p.path("period").path("start").asText());
            assertEquals("http://sys-ids.kemkes.go.id/episode-of-care/test-org",p.path("identifier").get(0).path("system").asText());
        }
    }
    @Test public void futureDateBlockedForPtmAndAnc(){for(String type:new String[]{"CAD","ANC"})fails("period.start",()->post(type,"2026-11-04",""));}
    @Test public void futureTimeOnSameDateBlocked(){fails("period.start",()->post("CAD","2026-11-03T08:00:01Z",""));}
    @Test public void exactNowIsAllowed(){assertEquals(clock.instant().toString(),post("CAD",clock.instant().toString(),"").path("period").path("start").asText());}
    @Test public void juneThirdBoundaryAcceptedForBothFlows(){for(String type:new String[]{"CAD","ANC"})assertEquals("2014-06-03T00:00:00Z",post(type,"2014-06-03","").path("period").path("start").asText());}
    @Test public void beforeJuneThirdBlocked(){for(String type:new String[]{"CAD","ANC"})fails("period.start",()->post(type,"2014-06-02",""));}
    @Test public void explicitZoneRespectsUtcLowerBoundary(){
        assertEquals("2014-06-03T00:00:00Z",SatuSehatEpisodePayload.dateTime("2014-06-03T08:00:00+08:00","boundary",clock));
        fails("boundary",()->SatuSehatEpisodePayload.dateTime("2014-06-03T07:59:59+08:00","boundary",clock));
    }
    @Test public void explicitOffsetConvertedWithoutChangingInstant(){
        assertEquals("2026-08-25T16:30:00Z",SatuSehatEpisodePayload.dateTime("2026-08-26T00:30:00+08:00","test",clock));
        assertEquals("2026-08-26",SatuSehatEpisodePayload.clinicalDate("2026-08-25T16:30:00Z"));
        ObjectNode remote=post("ANC","2026-08-26","");remote.with("period").put("start","2026-08-25T16:30:00Z");
        assertTrue(SatuSehatEpisodeSupport.sameHpht(remote,"2026-08-26"));
    }
    @Test public void offsetOrderingUsesInstantsNotStringOrder(){
        fails("period.end",()->post("CAD","2026-08-26T00:00:00Z","2026-08-26T01:00:00+08:00"));
    }
    @Test public void endBeforeStartRejectedForPtmAndAnc(){for(String type:new String[]{"CAD","ANC"})fails("period.end",()->post(type,"2026-08-26","2026-08-25"));}
    @Test public void futureEndRejected(){fails("period.end",()->post("ANC","2026-08-26","2026-11-04"));}
    @Test public void invalidCalendarDateNotAutoCorrected(){fails("period.start",()->post("ANC","2026-02-30",""));}
    @Test public void missingDateNotReplacedWithToday(){fails("period.start",()->post("CAD","",""));}
    @Test public void identifierUsesSuppliedOrganizationWithoutTrailingSlash(){
        assertEquals("http://sys-ids.kemkes.go.id/episode-of-care/other-test-org",SatuSehatEpisodePayload.identifierSystem("other-test-org"));
        fails("Organization",()->SatuSehatEpisodePayload.identifierSystem("test-org/"));
    }
    @Test public void ptmAndAncPutNormalizeLegacyHistoryAndIdentifierButKeepOtherData(){
        for(String type:new String[]{"CAD","ANC"}){
            ObjectNode p=post(type,"2026-08-26","");
            p.put("id","test-episode");p.put("extensionNote","unchanged-test");
            ((ObjectNode)p.withArray("identifier").get(0)).put("system","http://sys-ids.kemkes.go.id/episodeofcare/test-org/");
            ((ObjectNode)p.withArray("statusHistory").get(0).path("period")).put("start","2026-08-26");
            p.withArray("identifier").addObject().put("system","urn:external-test").put("value","external-test-id");
            SatuSehatEpisodePayload.update(p,"test-org","test-local","finished","2026-08-26","2026-10-30",clock);
            assertEquals("http://sys-ids.kemkes.go.id/episode-of-care/test-org",p.path("identifier").get(0).path("system").asText());
            assertEquals(2,p.path("identifier").size());assertEquals("unchanged-test",p.path("extensionNote").asText());
            assertEquals(2,p.path("statusHistory").size());
            assertEquals("2026-10-30T00:00:00Z",p.path("statusHistory").get(0).path("period").path("end").asText());
            assertEquals(p.path("statusHistory").get(0).path("period").path("end"),p.path("statusHistory").get(1).path("period").path("start"));
        }
    }
    @Test public void putPreservesKnownRemoteTimeWhenLocalDateSchemaHasNoTime(){
        ObjectNode p=post("CAD","2026-08-26T09:15:00+08:00","");
        SatuSehatEpisodePayload.update(p,"test-org","test-local","active","2026-08-26","",clock);
        assertEquals("2026-08-26T01:15:00Z",p.path("period").path("start").asText());
        assertEquals(1,p.path("statusHistory").size());
    }
    @Test public void putRejectsFutureHistoryWithExactPath(){
        ObjectNode p=post("ANC","2026-08-26","");
        ((ObjectNode)p.withArray("statusHistory").get(0).path("period")).put("start","2026-11-04");
        fails("statusHistory[0].period.start",()->SatuSehatEpisodePayload.update(p,"test-org","test-local","active","2026-08-26","",clock));
    }
    @Test public void validateRejectsHistoryEndBeforeStart(){
        ObjectNode p=post("ANC","2026-08-26","");
        ((ObjectNode)p.withArray("statusHistory").get(0).path("period")).put("end","2026-08-25");
        fails("statusHistory[0].period.end",()->SatuSehatEpisodePayload.validate(p,clock));
    }
    @Test public void putRejectsClosingBeforeLastStatusBegan(){
        ObjectNode p=post("CAD","2026-08-26","");
        ((ObjectNode)p.withArray("statusHistory").get(0).path("period")).put("start","2026-10-31T00:00:00Z");
        fails("statusHistory[0].period.end",()->SatuSehatEpisodePayload.update(p,"test-org","test-local","finished","2026-08-26","2026-10-30",clock));
    }
    @Test public void putRejectsFutureEpisodeStartForBothFlows(){
        for(String type:new String[]{"CAD","ANC"}){
            ObjectNode p=post(type,"2026-08-26","");
            fails("period.start",()->SatuSehatEpisodePayload.update(p,"test-org","test-local","active","2026-11-04","",clock));
        }
    }
    @Test public void putRejectsFutureHistoryEnd(){
        ObjectNode p=post("ANC","2026-08-26","");
        ((ObjectNode)p.withArray("statusHistory").get(0).path("period")).put("end","2026-11-04");
        fails("statusHistory[0].period.end",()->SatuSehatEpisodePayload.update(p,"test-org","test-local","active","2026-08-26","",clock));
    }
    @Test public void putRejectsHistoryBeforeLowerBoundary(){
        ObjectNode p=post("CAD","2014-06-03","");
        ((ObjectNode)p.withArray("statusHistory").get(0).path("period")).put("start","2014-06-02");
        fails("statusHistory[0].period.start",()->SatuSehatEpisodePayload.update(p,"test-org","test-local","active","2014-06-03","",clock));
    }
    @Test public void serializationDoesNotDependOnClockDefaultZone(){
        assertEquals(SatuSehatEpisodePayload.dateTime("2026-08-26 07:30:00","test",clock),
                SatuSehatEpisodePayload.dateTime("2026-08-26 07:30:00","test",clock.withZone(ZoneId.of("America/New_York"))));
    }
    @Test public void putPreservesPeriodExtensions(){
        ObjectNode p=post("CAD","2026-08-26","");p.with("period").putArray("extension").addObject().put("url","urn:test").put("valueString","retained");
        SatuSehatEpisodePayload.update(p,"test-org","test-local","active","2026-08-26","",clock);
        assertEquals("retained",p.path("period").path("extension").get(0).path("valueString").asText());
    }
}
