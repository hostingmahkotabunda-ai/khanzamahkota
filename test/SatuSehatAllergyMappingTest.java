import bridging.SatuSehatAllergyMapping;
import bridging.SatuSehatAllergyMapping.Mapping;
import com.fasterxml.jackson.databind.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class SatuSehatAllergyMappingTest {
    @Rule public TemporaryFolder temp=new TemporaryFolder();
    private final ObjectMapper json=new ObjectMapper();
    private Path file;private SatuSehatAllergyMapping store;
    @Before public void setup()throws Exception{
        file=temp.getRoot().toPath().resolve("allergy.iyem");store=new SatuSehatAllergyMapping(file.toFile());
    }
    private void write(String text)throws Exception{Files.write(file,text.getBytes(StandardCharsets.UTF_8));}
    private void blocked(String text)throws Exception{
        try{store.resolve(text);fail("Must be blocked");}catch(IllegalArgumentException expected){}
    }
    private Mapping seafood(){return new Mapping("seafood","food",SatuSehatAllergyMapping.SNOMED,"44027008","Seafood","Alergi seafood");}
    @Test public void normalizeKeepsWordBoundaries(){assertEquals("sea food",SatuSehatAllergyMapping.normalize(" SEA\n\tFOOD\u00a0"));}
    @Test public void seafoodWorksWithoutCache()throws Exception{
        Mapping m=store.resolve(" Seafood ");assertEquals("food",m.category);assertEquals("44027008",m.code);assertFalse(Files.exists(file));
    }
    @Test public void seafoodPlaceholderIsCorrected()throws Exception{
        write("{\"alergi\":[{\"keyword\":\"seafood\",\"category\":\"medication\",\"coding_code\":\"unknown\"}]}");
        assertEquals("food",store.resolve("SEAFOOD").category);assertEquals("44027008",store.resolve("seafood").code);
    }
    @Test public void wholeSeafoodSynonymsWork()throws Exception{
        for(String key:new String[]{"makanan laut","alergi seafood","sea food"})assertEquals("44027008",store.resolve(key).code);
    }
    @Test public void multipleAllergensCannotMatchSingleWord()throws Exception{
        store.save(seafood());blocked("seafood, ampisilin");blocked("seafood dan udang");
    }
    @Test public void reactionTextCannotMatchSingleWord()throws Exception{store.save(seafood());blocked("seafood (gatal)");}
    @Test public void blankDoesNotMeanNoKnownAllergy()throws Exception{blocked("");blocked(" ");blocked("-");assertFalse(Files.exists(file));}
    @Test public void explicitNegativeStatementIsAccepted()throws Exception{assertEquals("716186003",store.resolve("Tidak ada alergi").code);}
    @Test public void unknownIsPendingAndCannotBeSent()throws Exception{
        blocked("obat belum diketahui");JsonNode root=json.readTree(file.toFile());
        assertEquals(1,root.path("alergi").size());assertEquals("pending",root.path("alergi").get(0).path("review_status").asText());
        blocked("OBAT BELUM DIKETAHUI");assertEquals(1,json.readTree(file.toFile()).path("alergi").size());
    }
    @Test public void statusAndEditorDoNotWrite()throws Exception{
        assertEquals("Belum dimapping",store.status("obat tertentu"));store.forEditing("obat tertentu");assertFalse(Files.exists(file));
    }
    @Test public void savePreservesOtherEntriesMetadataAndBackup()throws Exception{
        String original="{\"version\":3,\"alergi\":[{\"keyword\":\"seafood\",\"coding_code\":\"unknown\",\"custom\":true},{\"keyword\":\"lain\",\"coding_code\":\"unknown\"}]}";
        write(original);store.save(seafood());JsonNode result=json.readTree(file.toFile());
        assertEquals(2,result.path("alergi").size());assertEquals(3,result.path("version").asInt());assertTrue(result.path("alergi").get(0).path("custom").asBoolean());
        assertEquals("unknown",result.path("alergi").get(1).path("coding_code").asText());
        try(DirectoryStream<Path> backups=Files.newDirectoryStream(file.getParent(),"alergisatusehat-backup-*.iyem")){
            Path backup=backups.iterator().next();assertEquals(original,new String(Files.readAllBytes(backup),StandardCharsets.UTF_8));
        }
    }
    @Test public void malformedCacheCannotBeOverwritten()throws Exception{
        write("{broken");byte[] before=Files.readAllBytes(file);
        try{store.save(seafood());fail();}catch(java.io.IOException expected){}assertArrayEquals(before,Files.readAllBytes(file));
    }
    @Test public void wrongRootCannotBeOverwritten()throws Exception{
        write("{\"alergi\":{}}");try{store.save(seafood());fail();}catch(java.io.IOException expected){}
        assertEquals("{\"alergi\":{}}",new String(Files.readAllBytes(file),StandardCharsets.UTF_8));
    }
    @Test public void duplicateKeysAreBlocked()throws Exception{
        write("{\"alergi\":[{\"keyword\":\"seafood\"},{\"keyword\":\"Seafood\"}]}");
        try{store.resolve("seafood");fail();}catch(java.io.IOException expected){}
    }
    @Test public void explicitAliasMatchesWholeTextOnly()throws Exception{
        store.save(seafood());JsonNode root=json.readTree(file.toFile());
        ((com.fasterxml.jackson.databind.node.ObjectNode)root.path("alergi").get(0)).putArray("aliases").add("seafood (gatal)");
        write(json.writeValueAsString(root));assertEquals("44027008",store.resolve("seafood (gatal)").code);blocked("seafood (gatal), obat");
    }
    @Test public void positiveAliasCannotBecomeNoKnownAllergy()throws Exception{
        store.save(new Mapping("tidak ada alergi","environment",SatuSehatAllergyMapping.SNOMED,"716186003","No known allergy","No known allergy"));
        JsonNode root=json.readTree(file.toFile());((com.fasterxml.jackson.databind.node.ObjectNode)root.path("alergi").get(0)).putArray("aliases").add("seafood");
        write(json.writeValueAsString(root));blocked("seafood");
    }
    @Test public void invalidCategoryIsRejected(){
        try{new Mapping("seafood","medication",SatuSehatAllergyMapping.SNOMED,"44027008","Seafood","Seafood").validate();fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void placeholderAndBadSystemsAreRejected(){
        for(String code:new String[]{"unknown","","abc"})try{new Mapping("a","food",SatuSehatAllergyMapping.SNOMED,code,"A","A").validate();fail();}catch(IllegalArgumentException expected){}
        try{new Mapping("a","food","http://incorrect","123456","A","A").validate();fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void positiveInputCannotUseNoKnownAllergy(){
        try{new Mapping("seafood","food",SatuSehatAllergyMapping.SNOMED,"716186003","No known allergy","No known allergy").validate();fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void missingDisplayIsRejected(){
        try{new Mapping("a","food",SatuSehatAllergyMapping.SNOMED,"123456","","A").validate();fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void validExistingMappingIsNotAutomaticallyReplaced()throws Exception{
        store.save(new Mapping("seafood","food",SatuSehatAllergyMapping.SNOMED,"123456","Reviewed example","Reviewed example"));
        assertEquals("123456",store.resolve("seafood").code);
    }
    @Test public void officialKfaActiveIngredientsAreAccepted()throws Exception{
        String[][] references={{"ibuprofen","91000107","IBUPROFEN"},{"ranitidine","91000375","RANITIDINE"},
            {"ampicillin","91000524","AMPICILLIN"},{"amoxicillin","91000237","Amoxicillin"},{"asam mefenamat","91000528","Mefenamic Acid"}};
        for(String[] ref:references){
            store.save(new Mapping(ref[0],"medication",SatuSehatAllergyMapping.KFA,ref[1],ref[2],"Alergi "+ref[2]));
            Mapping m=store.resolve(ref[0].toUpperCase(java.util.Locale.ROOT));assertEquals(ref[1],m.code);assertEquals(SatuSehatAllergyMapping.KFA,m.system);
        }
    }
    @Test public void verifiedPopcornIsFood()throws Exception{
        store.save(new Mapping("popcorn","food",SatuSehatAllergyMapping.SNOMED,"226726003","Popcorn","Alergi popcorn"));
        assertEquals("226726003",store.resolve("POPCORN").code);assertEquals("food",store.resolve("popcorn").category);
    }
    @Test public void explicitLocalDrugAliasesDoNotRequireManualMapping()throws Exception{
        store.save(new Mapping("ampicillin","medication",SatuSehatAllergyMapping.KFA,"91000524","AMPICILLIN","Alergi AMPICILLIN"));
        JsonNode root=json.readTree(file.toFile());
        ((com.fasterxml.jackson.databind.node.ObjectNode)root.path("alergi").get(0)).putArray("aliases").add("ampicillin (gatal, sesak napas)").add("ampisilin");
        write(json.writeValueAsString(root));
        assertEquals("91000524",store.resolve("AMPICILLIN (gatal, sesak napas)").code);
        assertEquals("Alergi AMPICILLIN",store.resolve("ampisilin").text);
        blocked("ampicillin, amoxicillin");blocked("antibiotik (gatal)");
    }
    @Test public void pendingReasonExplainsClinicalAmbiguity()throws Exception{
        write("{\"alergi\":[{\"keyword\":\"antibiotik (gatal)\",\"coding_code\":\"unknown\",\"review_reason\":\"Jenis antibiotik belum disebutkan\"}]}");
        assertTrue(store.status("antibiotik (gatal)").contains("Jenis antibiotik belum disebutkan"));
        try{store.resolve("antibiotik (gatal)");fail();}catch(IllegalArgumentException expected){assertEquals("Jenis antibiotik belum disebutkan",expected.getMessage());}
    }
    @Test public void medicationCannotUseFoodOrSnomedDrugCodes(){
        try{new Mapping("ibuprofen","food",SatuSehatAllergyMapping.KFA,"91000107","IBUPROFEN","Alergi IBUPROFEN").validate();fail();}catch(IllegalArgumentException expected){}
        try{new Mapping("ibuprofen","medication",SatuSehatAllergyMapping.SNOMED,"387207008","Ibuprofen","Alergi Ibuprofen").validate();fail();}catch(IllegalArgumentException expected){}
        try{new Mapping("obat","medication",SatuSehatAllergyMapping.KFA,"94000001","Kemasan","Obat").validate();fail();}catch(IllegalArgumentException expected){}
    }
}
