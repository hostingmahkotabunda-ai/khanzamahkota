import bridging.SatuSehatLabObservationPayload;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import static org.junit.Assert.*;

public class SatuSehatLabObservationPayloadTest {
    private final ObjectMapper mapper=new ObjectMapper();
    private ObjectNode build(String result,String id,String template){
        return SatuSehatLabObservationPayload.build(mapper,"test-org","order-test","kind-test",template,"http://loinc.org","test-code","Test display","test-patient","test-practitioner","test-encounter","test-specimen","2026-10-15 16:44:51",result,id);
    }
    @Test public void postIncludesIssuedAndConfiguredOrganization(){
        ObjectNode n=build("Hasil uji","","template-a");
        assertEquals("http://sys-ids.kemkes.go.id/observation/test-org",n.path("identifier").get(0).path("system").asText());
        assertEquals("2026-10-15T08:44:51Z",n.path("issued").asText());assertEquals(n.path("issued"),n.path("effectiveDateTime"));assertFalse(n.has("id"));
    }
    @Test public void textAndJsonSpecialCharactersArePreserved()throws Exception{
        String text="Hasil uji: \"contoh\" \\ path\nbaris dua\r\ntab\takhir";
        JsonNode n=mapper.readTree(mapper.writeValueAsString(build(text,"","template-a")));
        assertEquals(text,n.path("valueString").asText());assertFalse(n.path("valueString").asText().contains("<br>"));
    }
    @Test public void putUsesSamePayloadAndExistingResourceId(){
        ObjectNode post=build("Hasil uji","","template-a"),put=build("Hasil uji","existing-observation","template-a");
        assertEquals("existing-observation",put.remove("id").asText());assertEquals(post,put);
    }
    @Test public void parametersWithinOrderHaveDifferentIdentifiers(){
        assertNotEquals(build("hasil","","template-a").path("identifier"),build("hasil","","template-b").path("identifier"));
    }
    @Test public void suppliedOffsetKeepsItsInstant(){assertEquals("2026-10-15T09:44:51Z",SatuSehatLabObservationPayload.utc("2026-10-15T16:44:51+07:00"));}
    @Test(expected=java.time.format.DateTimeParseException.class) public void invalidDateIsHeld(){SatuSehatLabObservationPayload.utc("2026-02-30 16:00:00");}
    @Test(expected=IllegalArgumentException.class) public void incompleteReferenceIsHeld(){
        SatuSehatLabObservationPayload.build(mapper,"test-org","o","k","t","http://loinc.org","code","display","test-patient","","test-encounter","test-specimen","2026-10-15 12:00:00","hasil","");
    }
    @Test public void numericalLookingTextIsNotSilentlyConverted(){
        ObjectNode n=build("Hasil uji: 1.30* mg/dl","","template-a");assertEquals("Hasil uji: 1.30* mg/dl",n.path("valueString").asText());assertFalse(n.has("valueQuantity"));
    }
}
