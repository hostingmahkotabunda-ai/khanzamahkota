import bridging.*;
import bridging.SatuSehatAllergyReferenceService.*;
import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class SatuSehatAllergyReferenceTest {
    @Rule public TemporaryFolder temp=new TemporaryFolder();
    private final ObjectMapper json=new ObjectMapper();
    private Candidate milk(){return new Candidate("food",SatuSehatAllergyMapping.SNOMED,"3718001","Cow's milk","https://reference.test");}
    private Candidate drug(){return new Candidate("medication",SatuSehatAllergyMapping.KFA,"91000101","Paracetamol","https://reference.test");}
    private SatuSehatAllergyReferenceService service(Candidate... candidates){return new SatuSehatAllergyReferenceService(q -> new Result(Arrays.asList(candidates),true,""));}
    private SatuSehatAllergyMapping store(){return new SatuSehatAllergyMapping(new File(temp.getRoot(),"allergy.iyem"));}
    private void rejected(String text){try{SatuSehatAllergySearchTerm.extract(text);fail("Unsafe/ambiguous search term accepted");}catch(IllegalArgumentException expected){}}
    @Test public void suspectedPhraseIsSeparatedFromCowMilkSearch(){assertEquals("cow's milk",SatuSehatAllergySearchTerm.extract("Curiga alergi susu sapi"));assertTrue(SatuSehatAllergySearchTerm.suspected("Curiga alergi susu sapi"));}
    @Test public void knownReactionsAreNotSentAsSearchTerms(){assertEquals("ampicillin",SatuSehatAllergySearchTerm.extract("Ampisilin (gatal, sesak napas)"));}
    @Test public void dexamethasoneBodyPainUsesOnlyDrugNameForReferenceSearch(){
        assertEquals("dexamethasone",SatuSehatAllergySearchTerm.extract("Dexamethasone (nyeri badan)"));
        assertEquals("dexamethasone",SatuSehatAllergySearchTerm.extract("Dexamethason (nyeri badan)"));
        rejected("Dexamethasone (nyeri badan, amoxicillin)");
    }
    @Test public void bodyPainRemainsInSourceNoteAfterReferenceLookup()throws Exception{
        String source="Dexamethasone (nyeri badan)";
        // Kode sintetis hanya untuk fixture; tes tidak mengakses katalog/API.
        Candidate example=new Candidate("medication",SatuSehatAllergyMapping.KFA,"91000001","Dexamethasone","https://reference.test");
        SatuSehatAllergyReferenceService svc=service(example);SatuSehatAllergyMapping st=store();
        assertTrue(svc.saveAutomatic(svc.search(source),st));
        JsonNode payload=SatuSehatAllergyPayload.build(json,"org-test","local-test","patient-test","encounter-test","practitioner-test",
                "2026-11-04 09:00:00",source,st.resolve(source),null);
        assertEquals(source,payload.path("note").get(0).path("text").asText());
        assertEquals("Alergi Dexamethasone",payload.path("code").path("text").asText());
        assertEquals("unconfirmed",payload.path("verificationStatus").path("coding").get(0).path("code").asText());
    }
    @Test public void moreThanOneAllergenCannotBecomeOneSearch(){for(String s:new String[]{"seafood, ampicillin","ampicillin dan amoxicillin","seafood/udang","asam mefenamat, amoxcillin, ampicilin gatal kebas"})rejected(s);}
    @Test public void anotherAllergenInParenthesesCannotBeDiscarded(){rejected("ampicillin (amoxicillin)");rejected("seafood (gatal, udang)");}
    @Test public void unknownDrugGroupsAreNotGuessed(){for(String s:new String[]{"antibiotik (gatal)","obat batuk pilek","obat dosis tinggi (luka bakar)","-",""})rejected(s);}
    @Test public void patientMarkersAndLongIdentifiersAreRejected(){rejected("pasien Ahmad");rejected("NIK 1234567890123456");rejected("alergi 2026-11-04");}
    @Test public void negationCannotBecomePositiveAllergy(){rejected("tidak alergi susu sapi");rejected("alergi susu sapi disangkal");}
    @Test public void exactSingleCandidateCanBeSavedAutomatically()throws Exception{
        SatuSehatAllergyReferenceService s=service(milk());Lookup l=s.search("curiga alergi susu sapi");
        assertEquals("3718001",l.automatic().code);SatuSehatAllergyMapping store=store();assertTrue(s.saveAutomatic(l,store));
        assertEquals("3718001",store.resolve("curiga alergi susu sapi").code);
        assertEquals("Alergi Cow's milk",store.resolve("curiga alergi susu sapi").text);
    }
    @Test public void fuzzyMatchNeverAutomaticallySaves()throws Exception{
        SatuSehatAllergyReferenceService s=service(new Candidate("food",SatuSehatAllergyMapping.SNOMED,"10827009","Milk protein","https://reference.test"));
        Lookup l=s.search("susu sapi");assertNull(l.automatic());assertFalse(s.saveAutomatic(l,store()));assertEquals(0,temp.getRoot().listFiles().length);
    }
    @Test public void duplicateSameCodeDoesNotCreateFalseAmbiguity()throws Exception{assertNotNull(service(milk(),milk()).search("susu sapi").automatic());}
    @Test public void differentExactCodesRequireReview()throws Exception{
        Candidate other=new Candidate("food",SatuSehatAllergyMapping.SNOMED,"123456","Cow's milk","https://reference.test");
        assertNull(service(milk(),other).search("susu sapi").automatic());
    }
    @Test public void conflictingDisplayForSameCodeIsBlocked()throws Exception{
        Candidate wrong=new Candidate("food",SatuSehatAllergyMapping.SNOMED,"3718001","Fish roe","https://reference.test");
        Lookup l=service(milk(),wrong).search("susu sapi");assertNull(l.automatic());assertFalse(l.result.complete);assertTrue(l.result.candidates.isEmpty());
    }
    @Test public void unknownCategoryRequiresOperatorReview()throws Exception{
        assertNull(service(new Candidate("",SatuSehatAllergyMapping.SNOMED,"111088007","Latex","https://reference.test")).search("lateks").automatic());
    }
    @Test public void partialResultsAreNotAutomaticallySaved()throws Exception{
        SatuSehatAllergyReferenceService s=new SatuSehatAllergyReferenceService(q -> new Result(Arrays.asList(milk()),false,"service unavailable"));
        Lookup l=s.search("susu sapi");assertFalse(s.saveAutomatic(l,store()));assertEquals(0,temp.getRoot().listFiles().length);
    }
    @Test public void successfulQueryIsReusedForEquivalentPhrases()throws Exception{
        AtomicInteger calls=new AtomicInteger();SatuSehatAllergyReferenceService s=new SatuSehatAllergyReferenceService(q -> {calls.incrementAndGet();return new Result(Arrays.asList(milk()),true,"");});
        s.search("susu sapi");s.search("curiga alergi susu sapi");assertEquals(1,calls.get());
    }
    @Test public void failureIsRetriedWithoutWritingDictionary()throws Exception{
        AtomicInteger calls=new AtomicInteger();SatuSehatAllergyReferenceService s=new SatuSehatAllergyReferenceService(q -> {calls.incrementAndGet();return new Result(Collections.emptyList(),false,"offline");});
        s.search("susu sapi");s.search("susu sapi");assertEquals(2,calls.get());assertEquals(0,temp.getRoot().listFiles().length);
    }
    @Test public void automaticLookupCannotOverwriteReviewedMapping()throws Exception{
        SatuSehatAllergyMapping st=store();st.save(new SatuSehatAllergyMapping.Mapping("susu sapi","food",SatuSehatAllergyMapping.SNOMED,"10827009","Milk protein","Alergi Milk protein"));
        try{service(milk()).saveAutomatic(service(milk()).search("susu sapi"),st);fail();}catch(IOException expected){}
        assertEquals("10827009",st.resolve("susu sapi").code);
    }
    @Test public void reviewedSelectionCanRepairOldInvalidPair()throws Exception{
        Path file=new File(temp.getRoot(),"allergy.iyem").toPath();
        Files.write(file,("{\"alergi\":[{\"keyword\":\"susu sapi\",\"category\":\"food\",\"coding_system\":\"http://snomed.info/sct\",\"coding_code\":\"227183005\",\"coding_display\":\"Milk protein\",\"text\":\"Alergi susu sapi\"}]}").getBytes(StandardCharsets.UTF_8));
        SatuSehatAllergyMapping st=store();st.saveReference(milk().mapping("susu sapi","food"),milk().source,true);assertEquals("3718001",st.resolve("susu sapi").code);
    }
    @Test public void lookupDoesNotCreatePlaceholderForUnknownText()throws Exception{assertNull(store().lookup("paracetamol"));assertEquals(0,temp.getRoot().listFiles().length);}
    @Test public void lookupCanRetryIncompletePlaceholder()throws Exception{
        Files.write(new File(temp.getRoot(),"allergy.iyem").toPath(),"{\"alergi\":[{\"keyword\":\"paracetamol\",\"coding_code\":\"unknown\"}]}".getBytes(StandardCharsets.UTF_8));
        assertNull(store().lookup("paracetamol"));
    }
    @Test public void savedReferenceHasProvenanceAndNoClinicalVerification()throws Exception{
        SatuSehatAllergyReferenceService s=service(drug());s.saveAutomatic(s.search("parasetamol"),store());
        JsonNode node=json.readTree(new File(temp.getRoot(),"allergy.iyem")).path("alergi").get(0);
        assertEquals("exact_unique",node.path("reference_selection").asText());assertTrue(node.has("source_checked_at"));assertFalse(node.has("verificationStatus"));
    }
    @Test public void reviewedAliasCorrectionPreservesCanonicalKeyword()throws Exception{
        SatuSehatAllergyMapping st=store();st.save(new SatuSehatAllergyMapping.Mapping("obat internal","medication",SatuSehatAllergyMapping.KFA,"91000524","AMPICILLIN","Alergi AMPICILLIN"));
        File file=new File(temp.getRoot(),"allergy.iyem");JsonNode root=json.readTree(file);
        ((com.fasterxml.jackson.databind.node.ObjectNode)root.path("alergi").get(0)).putArray("aliases").add("obat lokal");
        json.writeValue(file,root);st.saveReference(drug().mapping("obat lokal","medication"),drug().source,true);
        assertEquals("91000101",st.resolve("obat internal").code);assertEquals("91000101",st.resolve("obat lokal").code);
        assertEquals("obat internal",json.readTree(file).path("alergi").get(0).path("keyword").asText());
    }
}
