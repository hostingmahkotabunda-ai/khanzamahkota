import bridging.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class SatuSehatAllergyPayloadTest {
    private final ObjectMapper json=new ObjectMapper();
    private final SatuSehatAllergyMapping.Mapping mapping=new SatuSehatAllergyMapping.Mapping("seafood","food",SatuSehatAllergyMapping.SNOMED,"44027008","Seafood","Alergi seafood");
    private ObjectNode payload(String date,String note,String id){return SatuSehatAllergyPayload.build(json,"org-test","local-test","patient-test","encounter-test","practitioner-test",date,note,mapping,id);}
    @Test public void postHasFoodCodeAndLocalOrganization(){
        ObjectNode p=payload("2026-11-04 09:00:00","Seafood",null);
        assertFalse(p.has("id"));assertEquals("food",p.path("category").get(0).asText());
        assertEquals("44027008",p.path("code").path("coding").get(0).path("code").asText());
        assertEquals("http://sys-ids.kemkes.go.id/allergy/org-test",p.path("identifier").get(0).path("system").asText());
    }
    @Test public void postAndPutAreIdenticalExceptId(){
        ObjectNode post=payload("2026-11-04 09:00:00","Seafood",null),put=payload("2026-11-04 09:00:00","Seafood","allergy-test");
        assertEquals("allergy-test",put.remove("id").asText());assertEquals(post,put);
    }
    @Test public void singleCodeContainsTextAndCoding()throws Exception{
        String body=json.writeValueAsString(payload("2026-11-04 09:00:00","Seafood","allergy-test"));
        com.fasterxml.jackson.core.JsonParser parser=json.getFactory().createParser(body);int codes=0;
        while(parser.nextToken()!=null)if(parser.getCurrentToken()==com.fasterxml.jackson.core.JsonToken.FIELD_NAME&&parser.getParsingContext().getParent().inRoot()&&"code".equals(parser.getCurrentName()))codes++;
        parser.close();assertEquals(1,codes);
        JsonNode p=json.readTree(body);assertEquals("Alergi seafood",p.path("code").path("text").asText());assertTrue(p.path("code").path("coding").isArray());
    }
    @Test public void originalNotesWithQuotesNewlinesAndBackslashArePreserved()throws Exception{
        String note="Seafood \"gatal\"\ncatatan\\lain\t";
        JsonNode p=json.readTree(json.writeValueAsString(payload("2026-11-04 09:00:00",note,null)));
        assertEquals(note,p.path("note").get(0).path("text").asText());
    }
    @Test public void witaTimeIsConvertedToUtc(){assertEquals("2026-11-04T01:00:00Z",payload("2026-11-04 09:00:00","Seafood",null).path("recordedDate").asText());}
    @Test public void explicitOffsetIsHonored(){assertEquals("2026-11-04T02:00:00Z",payload("2026-11-04T09:00:00+07:00","Seafood",null).path("recordedDate").asText());}
    @Test public void utcIsNotShiftedAgain(){assertEquals("2026-11-04T01:00:00Z",payload("2026-11-04T01:00:00Z","Seafood",null).path("recordedDate").asText());}
    @Test public void malformedDateIsBlocked(){try{payload("not-a-date","Seafood",null);fail();}catch(IllegalArgumentException expected){}}
    @Test public void malformedPutIdIsBlocked(){try{payload("2026-11-04 09:00:00","Seafood","/bad");fail();}catch(IllegalArgumentException expected){}}
    @Test public void emptyReferenceIsBlocked(){
        try{SatuSehatAllergyPayload.build(json,"org-test","local-test","","encounter-test","practitioner-test","2026-11-04 09:00:00","Seafood",mapping,null);fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void emptyOrganizationIsBlocked(){
        try{SatuSehatAllergyPayload.build(json,"","local-test","patient-test","encounter-test","practitioner-test","2026-11-04 09:00:00","Seafood",mapping,null);fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void kfaDrugPayloadPreservesSourceReactionOnlyInNote(){
        SatuSehatAllergyMapping.Mapping m=new SatuSehatAllergyMapping.Mapping("ampicillin","medication",SatuSehatAllergyMapping.KFA,"91000524","AMPICILLIN","Alergi AMPICILLIN");
        for(String id:new String[]{null,"allergy-test"}){
            ObjectNode p=SatuSehatAllergyPayload.build(json,"org-test","local-test","patient-test","encounter-test","practitioner-test","2026-11-04 09:00:00","ampicillin (gatal, sesak napas)",m,id);
            assertEquals("http://sys-ids.kemkes.go.id/kfa",p.path("code").path("coding").get(0).path("system").asText());
            assertEquals("91000524",p.path("code").path("coding").get(0).path("code").asText());
            assertEquals("Alergi AMPICILLIN",p.path("code").path("text").asText());
            assertEquals("ampicillin (gatal, sesak napas)",p.path("note").get(0).path("text").asText());
        }
    }
    @Test public void defaultVerificationIsUnconfirmedForPostAndPut(){
        for(String id:new String[]{null,"allergy-test"})assertEquals("unconfirmed",payload("2026-11-04 09:00:00","Seafood",id).path("verificationStatus").path("coding").get(0).path("code").asText());
    }
    @Test public void suspectedAllergyCannotBeSentAsConfirmed(){
        try{SatuSehatAllergyPayload.build(json,"org-test","local-test","patient-test","encounter-test","practitioner-test","2026-11-04 09:00:00","curiga alergi susu sapi",mapping,null,"confirmed");fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void confirmedRequiresExplicitPerRowSelection(){
        ObjectNode p=SatuSehatAllergyPayload.build(json,"org-test","local-test","patient-test","encounter-test","practitioner-test","2026-11-04 09:00:00","Seafood",mapping,null,"confirmed");
        assertEquals("confirmed",p.path("verificationStatus").path("coding").get(0).path("code").asText());
    }
    @Test public void invalidOrBlankVerificationIsBlocked(){
        for(String state:new String[]{"","anything",null})try{SatuSehatAllergyPayload.build(json,"org-test","local-test","patient-test","encounter-test","practitioner-test","2026-11-04 09:00:00","Seafood",mapping,"allergy-test",state);fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void knownMilkFishRoeMismatchIsRejected(){
        SatuSehatAllergyMapping.Mapping wrong=new SatuSehatAllergyMapping.Mapping("susu sapi","food",SatuSehatAllergyMapping.SNOMED,"227183005","Milk protein","Alergi susu sapi");
        try{SatuSehatAllergyPayload.build(json,"org-test","local-test","patient-test","encounter-test","practitioner-test","2026-11-04 09:00:00","curiga alergi susu sapi",wrong,null);fail();}catch(IllegalArgumentException expected){}
    }
}
