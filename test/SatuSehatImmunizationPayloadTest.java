import bridging.SatuSehatImmunizationPayload;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Synthetic payloads only; no configured database or API calls. */
public class SatuSehatImmunizationPayloadTest {
    private final ObjectMapper mapper=new ObjectMapper();
    private ObjectNode fixture(){ObjectNode p=mapper.createObjectNode();p.put("resourceType","Immunization");p.put("expirationDate","2028-12-06");p.putObject("doseQuantity").put("value",1).put("unit","mL");p.putArray("protocolApplied").addObject().put("doseNumberPositiveInt",1);return p;}
    private void held(ObjectNode p)throws Exception{try{SatuSehatImmunizationPayload.validateAndSerialize(p.toString());fail("must stop before send");}catch(IllegalArgumentException expected){}}
    @Test public void confirmedUmrohVaccineProducesFirstDoseWithoutChangingQuantity()throws Exception{
        ObjectNode p=fixture();p.remove("protocolApplied");
        JsonNode actual=mapper.readTree(SatuSehatImmunizationPayload.validateAndSerialize(p.toString(),SatuSehatImmunizationPayload.confirmedDoseNumber("93023057")));
        assertEquals(1,actual.path("protocolApplied").get(0).path("doseNumberPositiveInt").asInt());assertEquals(p.path("doseQuantity"),actual.path("doseQuantity"));
    }
    @Test public void otherVaccinesStillRequireClinicalDoseEntry()throws Exception{
        for(String vaccine:new String[]{null,"","other","93023058","2026-11-09"}){
            try{SatuSehatImmunizationPayload.validateAndSerialize(fixture().toString(),SatuSehatImmunizationPayload.confirmedDoseNumber(vaccine));fail("must not assume first dose for other vaccines");}catch(IllegalArgumentException expected){}
        }
    }
    @Test public void missingProtocolIsHeldRatherThanInventingDoseNumber()throws Exception{ObjectNode p=fixture();p.remove("protocolApplied");held(p);}
    @Test public void confirmedFirstDoseCreatesValidProtocol()throws Exception{ObjectNode p=fixture();p.remove("protocolApplied");JsonNode actual=mapper.readTree(SatuSehatImmunizationPayload.validateAndSerialize(p.toString(),"1"));assertEquals(1,actual.path("protocolApplied").get(0).path("doseNumberPositiveInt").asInt());assertEquals(p.path("doseQuantity"),actual.path("doseQuantity"));}
    @Test public void confirmedSecondDoseIsNotChangedToFirst()throws Exception{JsonNode actual=mapper.readTree(SatuSehatImmunizationPayload.validateAndSerialize(fixture().toString(),"2"));assertEquals(2,actual.path("protocolApplied").get(0).path("doseNumberPositiveInt").asInt());}
    @Test public void doseColumnCannotContainUsageRuleOrVolume()throws Exception{for(String value:new String[]{"","0","-1","1.5","0.5 mL","1 kali","2147483648"}){try{SatuSehatImmunizationPayload.validateAndSerialize(fixture().toString(),value);fail("must reject "+value);}catch(IllegalArgumentException expected){}}}
    @Test public void meningococcalTravelVaccineUsesDocumentedSpecialProgram(){assertEquals("IM-Khusus",SatuSehatImmunizationPayload.reasonCode("93023057"));assertEquals("Imunisasi Program Khusus",SatuSehatImmunizationPayload.reasonDisplay("93023057"));}
    @Test public void unrelatedVaccineIsNotAutomaticallyClassifiedAsTravelVaccine(){assertEquals("IM-Program",SatuSehatImmunizationPayload.reasonCode("other"));assertEquals("Imunisasi Program",SatuSehatImmunizationPayload.reasonDisplay("other"));}
    @Test public void errorSummaryReportsRulesAndFieldsWithoutRawClinicalText(){
        String body="{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"code\":\"value\",\"expression\":[\"Immunization.reasonCode[0].coding[0].code\"],\"details\":{\"text\":\"Invalid private-value RuleNumber: 12345\"},\"diagnostics\":\"Patient/private-id Token private-secret\"}]}";
        String summary=SatuSehatImmunizationPayload.errorSummary(body);assertTrue(summary.contains("rule=12345"));assertTrue(summary.contains("field=Immunization.reasonCode[0].coding[0].code"));assertFalse(summary.contains("private"));
    }
    @Test public void errorSummarySupportsLocationAndDiagnosticsRules(){String summary=SatuSehatImmunizationPayload.errorSummary("{\"issue\":[{\"location\":[\"Immunization.vaccineCode\"],\"diagnostics\":\"RuleNumber 12346\"}]}");assertTrue(summary.contains("field=Immunization.vaccineCode"));assertTrue(summary.contains("rule=12346"));}
    @Test public void errorSummaryDoesNotEchoHtmlOrArbitraryFields(){String summary=SatuSehatImmunizationPayload.errorSummary("<html>private-secret</html>");assertFalse(summary.contains("private"));summary=SatuSehatImmunizationPayload.errorSummary("{\"issue\":[{\"expression\":[\"Patient/private-id\"]}]}");assertFalse(summary.contains("private"));}
    @Test public void quantityAndUnitArePreserved()throws Exception{ObjectNode original=fixture();JsonNode p=mapper.readTree(SatuSehatImmunizationPayload.validateAndSerialize(original.toString()));assertEquals(original.path("doseQuantity"),p.path("doseQuantity"));}
    @Test public void emptyExpiryStopsSend()throws Exception{ObjectNode p=fixture();p.put("expirationDate","");held(p);}
    @Test public void missingExpiryStopsSend()throws Exception{ObjectNode p=fixture();p.remove("expirationDate");held(p);}
    @Test public void nullExpiryStopsSend()throws Exception{ObjectNode p=fixture();p.putNull("expirationDate");held(p);}
    @Test public void nonexistentCalendarDateStopsSend()throws Exception{ObjectNode p=fixture();p.put("expirationDate","2027-02-29");held(p);}
    @Test public void zeroMysqlDateStopsSend()throws Exception{ObjectNode p=fixture();p.put("expirationDate","0000-00-00");held(p);}
    @Test public void datetimeIsNotAcceptedForDateField()throws Exception{ObjectNode p=fixture();p.put("expirationDate","2028-12-06T00:00:00Z");held(p);}
    @Test public void validExplicitDoseNumberIsPreserved()throws Exception{ObjectNode p=fixture();p.putArray("protocolApplied").addObject().put("doseNumberPositiveInt",2);assertEquals(p,mapper.readTree(SatuSehatImmunizationPayload.validateAndSerialize(p.toString())));}
    @Test public void validExplicitStringIsPreserved()throws Exception{ObjectNode p=fixture();p.putArray("protocolApplied").addObject().put("doseNumberString","unknown");assertEquals(p,mapper.readTree(SatuSehatImmunizationPayload.validateAndSerialize(p.toString())));}
    @Test public void invalidNumbersAreHeld()throws Exception{for(String value:new String[]{"0","-1","1.5","2147483648","null","\"1\""}){ObjectNode p=fixture();p.putArray("protocolApplied").addObject().set("doseNumberPositiveInt",mapper.readTree(value));held(p);}}
    @Test public void emptyProtocolIsHeld()throws Exception{ObjectNode p=fixture();p.putArray("protocolApplied").addObject();held(p);}
    @Test public void twoDoseNumberChoicesAreHeld()throws Exception{ObjectNode p=fixture();p.putArray("protocolApplied").addObject().put("doseNumberPositiveInt",1).put("doseNumberString","1");held(p);}
    @Test public void malformedOriginalRequestIsRejected()throws Exception{try{SatuSehatImmunizationPayload.validateAndSerialize("{\"resourceType\":\"Immunization\",\"protocolApplied\":[{\"doseNumberPositiveInt\":}]}");fail();}catch(com.fasterxml.jackson.core.JsonProcessingException expected){}}
    @Test public void postAndPutUseSameValidationAndNeverParseUsageRule()throws Exception{
        String source=new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/bridging/SatuSehatKirimVaksin.java")),"UTF-8");
        assertEquals(2,source.split("json=SatuSehatImmunizationPayload.validateAndSerialize\\(json,",-1).length-1);
        assertTrue(source.contains("colIndex==0||colIndex==22"));
        assertFalse(source.contains("rs.getString(\"jml\"),rs.getString(\"aturan\")"));
        assertFalse(source.contains("replaceAll(\"[^0-9.]\""));
    }
}
