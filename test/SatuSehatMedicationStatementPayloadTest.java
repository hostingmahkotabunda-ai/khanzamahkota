import bridging.SatuSehatMedicationStatementPayload;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import static org.junit.Assert.*;

public class SatuSehatMedicationStatementPayloadTest {
    private final ObjectMapper mapper=new ObjectMapper();

    private ObjectNode payload(String status,String mode,String code,String name,String note) {
        return SatuSehatMedicationStatementPayload.build(mapper,"TEST-ORG","test-record", "Patient/test-patient",
                "Encounter/test-encounter","2026-11-03 00:30:00",status,mode,code,name,"1 tablet\nSesuai catatan",note,"Pasien sendiri");
    }

    @Test public void narrativeSurvivesJsonEscaping() throws Exception {
        String note="Pasien menyatakan: \"obat rutin\"\r\nCatatan\\arsip\tΩ";
        JsonNode parsed=mapper.readTree(mapper.writeValueAsString(payload("active","KFA (obat luar faskes)","92000001","Obat contoh uji",note)));
        assertEquals(note,parsed.path("note").get(0).path("text").asText());
        assertEquals("1 tablet\nSesuai catatan",parsed.path("dosage").get(0).path("text").asText());
    }

    @Test public void interviewTimeConvertsFromWitaAcrossDateBoundary() {
        assertEquals("2026-11-02T16:30:00Z",SatuSehatMedicationStatementPayload.utc("2026-11-03 00:30:00"));
    }

    @Test public void usesSelectedPatientSourceWithoutInventingUsagePeriod() {
        ObjectNode node=payload("unknown","KFA (obat luar faskes)","93000001","Obat contoh uji","");
        assertFalse(node.has("effectiveDateTime"));assertFalse(node.has("effectivePeriod"));
        assertEquals("Patient/test-patient",node.path("informationSource").path("reference").asText());assertFalse(node.has("category"));
        assertEquals("unknown",node.path("status").asText());assertFalse(node.has("note"));
    }

    @Test public void medicationChoiceIsExclusive() {
        ObjectNode node=payload("stopped","Medication (faskes ini)","Medication/test-medication","","");
        assertFalse(node.has("medicationCodeableConcept"));assertTrue(node.has("medicationReference"));
        node=payload("completed","KFA (obat luar faskes)","92000001","Obat contoh uji","");
        assertFalse(node.has("medicationReference"));assertTrue(node.has("medicationCodeableConcept"));
    }

    @Test(expected=IllegalArgumentException.class) public void missingInformationSourceBlocksPayload() {
        SatuSehatMedicationStatementPayload.build(mapper,"TEST-ORG","test-record","Patient/test-patient",
                "Encounter/test-encounter","2026-11-03 00:30:00","active","KFA (obat luar faskes)","92000001","Obat uji","","","");
    }

    @Test public void preservesExplicitPractitionerSource() {
        ObjectNode node=SatuSehatMedicationStatementPayload.build(mapper,"TEST-ORG","test-record","Patient/test-patient",
                "Encounter/test-encounter","2026-11-03 00:30:00","active","KFA (obat luar faskes)","92000001","Obat uji","","","Practitioner/test-practitioner");
        assertEquals("Practitioner/test-practitioner",node.path("informationSource").path("reference").asText());
    }

    @Test(expected=IllegalArgumentException.class) public void sourceMustBeAllowedResource() {
        SatuSehatMedicationStatementPayload.validateInformationSource("Encounter/test-encounter");
    }

    @Test(expected=IllegalArgumentException.class) public void sourceCannotContainQuery() {
        SatuSehatMedicationStatementPayload.validateInformationSource("Practitioner/test?other=1");
    }

    @Test(expected=IllegalArgumentException.class) public void requiresReviewedStatus() {
        payload("","KFA (obat luar faskes)","92000001","Obat contoh uji","");
    }

    @Test(expected=IllegalArgumentException.class) public void doesNotTreatFollowupAsUsageStatus() {
        payload("Lanjut","KFA (obat luar faskes)","92000001","Obat contoh uji","");
    }

    @Test(expected=IllegalArgumentException.class) public void rejectsIngredientKfaCode() {
        payload("active","KFA (obat luar faskes)","91000001","Obat contoh uji","");
    }

    @Test(expected=IllegalArgumentException.class) public void rejectsPlaceholderCode() {
        payload("active","KFA (obat luar faskes)","unknown","Obat contoh uji","");
    }

    @Test(expected=IllegalArgumentException.class) public void rejectsReferenceQueryInjection() {
        payload("active","Medication (faskes ini)","Medication/test?subject=other","","");
    }

    @Test(expected=java.time.format.DateTimeParseException.class) public void rejectsInvalidDateWithoutFallback() {
        SatuSehatMedicationStatementPayload.utc("tanggal tidak diketahui");
    }

    @Test public void identifiersAreStableAndSeparateDrugsAndEncounters() throws Exception {
        String first=SatuSehatMedicationStatementPayload.key("R1","Obat A");
        assertEquals(first,SatuSehatMedicationStatementPayload.key("R1","Obat A"));
        assertNotEquals(first,SatuSehatMedicationStatementPayload.key("R1","Obat B"));
        assertNotEquals(first,SatuSehatMedicationStatementPayload.key("R2","Obat A"));
        assertTrue(first.matches("[0-9a-f]{64}"));
    }
}
