import bridging.*;
import bridging.SatuSehatAllergyReferenceService.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import javax.swing.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class SatuSehatAllergyMappingEditorTest {
    private interface UiCheck {void run()throws Exception;}
    private void ui(UiCheck test)throws Exception{
        AtomicReference<Throwable> failure=new AtomicReference<>();
        SwingUtilities.invokeAndWait(()->{try{test.run();}catch(Throwable e){failure.set(e);}});
        if(failure.get()!=null)throw new AssertionError(failure.get());
    }
    private SatuSehatAllergyMapping.Mapping pending(String keyword){return new SatuSehatAllergyMapping.Mapping(keyword,"",SatuSehatAllergyMapping.SNOMED,"","","Alergi "+keyword);}
    private Candidate ibuprofen(){return new Candidate("medication",SatuSehatAllergyMapping.KFA,"91000107","IBUPROFEN","https://reference.test");}
    private SatuSehatAllergyReferenceService references(AtomicInteger calls){return new SatuSehatAllergyReferenceService(q->{calls.incrementAndGet();return new Result(Arrays.asList(ibuprofen()),true,"");});}
    @Test public void selectedReferenceFillsCompleteMappingWithoutNetworkOrPersistence()throws Exception{
        ui(()->{
            AtomicInteger calls=new AtomicInteger();String source="ibu profen (bengkak)";
            SatuSehatAllergyMappingEditor form=new SatuSehatAllergyMappingEditor(source,pending(source),references(calls));
            form.applyReference(ibuprofen(),"medication");SatuSehatAllergyMapping.Mapping m=form.mapping();
            assertEquals("medication",m.category);assertEquals(SatuSehatAllergyMapping.KFA,m.system);assertEquals("91000107",m.code);
            assertEquals("IBUPROFEN",m.display);assertEquals("Alergi IBUPROFEN",m.text);assertEquals(source,m.keyword);
            assertEquals("https://reference.test",form.referenceSource());assertEquals(0,calls.get());
        });
    }
    @Test public void compoundSourceCanBeSavedAsExplicitPartialMapping()throws Exception{
        ui(()->{
            String source="ibu profen dan asam mefenamat (bengkak)";
            SatuSehatAllergyMappingEditor form=new SatuSehatAllergyMappingEditor(source,pending(source),references(new AtomicInteger()));
            form.applyReference(ibuprofen(),"medication");assertTrue(form.isMultiple());
            SatuSehatAllergyMapping.Mapping m=form.mapping();assertTrue(m.partial);assertEquals("91000107",m.code);assertEquals("Alergi IBUPROFEN",m.text);
        });
    }
    @Test public void reactionListDoesNotCountAsMultipleAllergens()throws Exception{
        ui(()->{
            String source="Ibuprofen (gatal, bengkak dan sesak napas)";
            SatuSehatAllergyMappingEditor form=new SatuSehatAllergyMappingEditor(source,pending(source),references(new AtomicInteger()));
            form.applyReference(ibuprofen(),"medication");assertFalse(form.isMultiple());assertEquals("91000107",form.mapping().code);
        });
    }
    @Test public void selectedMedicineDoesNotAlterOriginalClinicalNoteOrVerification()throws Exception{
        ui(()->{
            String source="curiga alergi ibu profen (bengkak)";
            SatuSehatAllergyMappingEditor form=new SatuSehatAllergyMappingEditor(source,pending(source),references(new AtomicInteger()));form.applyReference(ibuprofen(),"medication");
            com.fasterxml.jackson.databind.JsonNode p=SatuSehatAllergyPayload.build(new com.fasterxml.jackson.databind.ObjectMapper(),"org-test","local-test","patient-test","encounter-test","practitioner-test","2026-11-04 09:00:00",source,form.mapping(),null);
            assertEquals(source,p.path("note").get(0).path("text").asText());assertEquals("unconfirmed",p.path("verificationStatus").path("coding").get(0).path("code").asText());
        });
    }
    @Test public void localSpellingsAndReactionCanBeUsedInSearch(){
        assertEquals("ibuprofen",SatuSehatAllergySearchTerm.extract("ibu profen"));
        assertEquals("caffeine",SatuSehatAllergySearchTerm.extract("cafein (berdebar)"));
        assertEquals("caffeine",SatuSehatAllergySearchTerm.extract("kafein"));
    }
    @Test public void legacyCompoundAliasCannotResolveAsOneMedicine()throws Exception{
        org.junit.rules.TemporaryFolder folder=new org.junit.rules.TemporaryFolder();folder.create();
        try{
            java.io.File file=new java.io.File(folder.getRoot(),"allergy.iyem");com.fasterxml.jackson.databind.ObjectMapper json=new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.node.ObjectNode root=json.createObjectNode(),entry=root.putArray("alergi").addObject();
            entry.put("keyword","ibuprofen");entry.put("category","medication");entry.put("coding_system",SatuSehatAllergyMapping.KFA);entry.put("coding_code","91000107");entry.put("coding_display","IBUPROFEN");entry.put("text","Alergi IBUPROFEN");entry.putArray("aliases").add("ibu profen dan asam mefenamat (bengkak)");json.writeValue(file,root);
            try{new SatuSehatAllergyMapping(file).resolve("ibu profen dan asam mefenamat (bengkak)");fail();}catch(IllegalArgumentException expected){assertTrue(expected.getMessage().contains("beberapa zat"));}
        }finally{folder.delete();}
    }
    @Test public void partialMappingRoundTripKeepsCoverageAndOriginalNote()throws Exception{
        org.junit.rules.TemporaryFolder folder=new org.junit.rules.TemporaryFolder();folder.create();
        try{
            String source="Phinicillin, Ampicillin dan sejenisnya (ruam)";
            SatuSehatAllergyMapping.Mapping selected=new SatuSehatAllergyMapping.Mapping(source,"medication",SatuSehatAllergyMapping.KFA,"91000524","AMPICILLIN","Alergi AMPICILLIN",true);
            SatuSehatAllergyMapping st=new SatuSehatAllergyMapping(new java.io.File(folder.getRoot(),"allergy.iyem"));st.saveReference(selected,"https://reference.test",true);
            assertTrue(st.resolve(source).partial);assertTrue(st.status(source).contains("Sebagian terpetakan"));
            com.fasterxml.jackson.databind.ObjectMapper json=new com.fasterxml.jackson.databind.ObjectMapper();
            assertEquals("partial",json.readTree(new java.io.File(folder.getRoot(),"allergy.iyem")).path("alergi").get(0).path("review_status").asText());
            for(String id:new String[]{null,"allergy-test"}){
                com.fasterxml.jackson.databind.JsonNode p=SatuSehatAllergyPayload.build(json,"org-test","local-test","patient-test","encounter-test","practitioner-test","2026-11-04 09:00:00",source,st.resolve(source),id);
                assertEquals(1,p.path("code").path("coding").size());assertEquals("91000524",p.path("code").path("coding").get(0).path("code").asText());
                assertEquals(source,p.path("note").get(0).path("text").asText());assertTrue(p.path("note").get(1).path("text").asText().contains("Mapping sebagian"));
                assertEquals("unconfirmed",p.path("verificationStatus").path("coding").get(0).path("code").asText());
            }
        }finally{folder.delete();}
    }
    @Test public void compoundMappingRequiresExplicitPartialFlag(){
        try{new SatuSehatAllergyMapping.Mapping("ibuprofen dan asam mefenamat","medication",SatuSehatAllergyMapping.KFA,"91000107","IBUPROFEN","Alergi IBUPROFEN").validate();fail();}
        catch(IllegalArgumentException expected){assertTrue(expected.getMessage().contains("mapping sebagian"));}
    }
    @Test public void explicitPartialChoiceForLegacyAliasPreservesSingleDrugEntry()throws Exception{
        org.junit.rules.TemporaryFolder folder=new org.junit.rules.TemporaryFolder();folder.create();
        try{
            String group="ibuprofen dan asam mefenamat (bengkak)";java.io.File file=new java.io.File(folder.getRoot(),"allergy.iyem");
            com.fasterxml.jackson.databind.ObjectMapper json=new com.fasterxml.jackson.databind.ObjectMapper();
            SatuSehatAllergyMapping st=new SatuSehatAllergyMapping(file);st.save(new SatuSehatAllergyMapping.Mapping("ibuprofen","medication",SatuSehatAllergyMapping.KFA,"91000107","IBUPROFEN","Alergi IBUPROFEN"));
            com.fasterxml.jackson.databind.node.ObjectNode root=(com.fasterxml.jackson.databind.node.ObjectNode)json.readTree(file);
            ((com.fasterxml.jackson.databind.node.ObjectNode)root.path("alergi").get(0)).putArray("aliases").add(group);json.writeValue(file,root);
            st.saveReference(new SatuSehatAllergyMapping.Mapping(group,"medication",SatuSehatAllergyMapping.KFA,"91000107","IBUPROFEN","Alergi IBUPROFEN",true),"https://reference.test",true);
            assertFalse(st.resolve("ibuprofen").partial);assertTrue(st.resolve(group).partial);assertEquals(2,json.readTree(file).path("alergi").size());
        }finally{folder.delete();}
    }
}
