import bridging.*;
import bridging.SatuSehatAllergyReferenceService.*;
import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class SatuSehatAllergyOnlineClientTest {
    private final ObjectMapper json=new ObjectMapper();
    private String foodHtml()throws Exception{
        String coding="{\"system\":\"http://snomed.info/sct\",\"code\":\"3718001\",\"display\":\"Cow's milk\"}";
        return "<html>762766007<button data-clipboard-text=\""+org.apache.commons.lang3.StringEscapeUtils.escapeHtml4(coding)+"\"></button></html>";
    }
    private String csv(){return "\"Code\",\"Display\",\"Codesystem\"\r\n\"3718001\",\"Cow's milk \",\"http://snomed.info/sct\"\r\n\"111088007\",\"Latex\",\"http://snomed.info/sct\"\r\n\"123456\",\"Substance, example\",\"http://snomed.info/sct\"\r\n";}
    private String publicKfa(){return "{\"status\":200,\"error\":false,\"data\":[{\"kfaCode\":\"91000101\",\"name\":\"Paracetamol\"}],\"meta\":{\"totalPage\":1}}";}
    private String products(){return "{\"total\":1,\"items\":{\"data\":[{\"active_ingredients\":[{\"active\":true,\"state\":\"valid\",\"kfa_code\":\"91000101\",\"zat_aktif\":\"Paracetamol\"}]}]}}";}
    private SatuSehatAllergyOnlineClient client(SatuSehatAllergyOnlineClient.Transport t){return new SatuSehatAllergyOnlineClient("https://api-satusehat.kemkes.go.id/kfa-v2",() -> "synthetic-access",t);}
    private String reference(String url)throws IOException{
        try{if(url.equals(SatuSehatAllergyOnlineClient.FOODS))return foodHtml();if(url.equals(SatuSehatAllergyOnlineClient.SNOMED_SHEET))return csv();}
        catch(Exception e){throw new IOException(e);}throw new IOException("Unexpected request");
    }
    @Test public void exactKfaBzaIsReturnedFromPublicReference()throws Exception{
        Result r=client((url,body,bearer)->url.equals(SatuSehatAllergyOnlineClient.KFA_BROWSER)?publicKfa():reference(url)).search("paracetamol");
        assertTrue(r.complete);assertEquals(1,r.candidates.size());assertEquals("91000101",r.candidates.get(0).code);
    }
    @Test public void onlineQueryContainsOnlyExtractedTerm()throws Exception{
        List<String> bodies=new ArrayList<>();
        SatuSehatAllergyReferenceService svc=new SatuSehatAllergyReferenceService(client((url,body,bearer)->{
            if(url.equals(SatuSehatAllergyOnlineClient.KFA_BROWSER)){bodies.add(body);return publicKfa();}return reference(url);
        }));
        svc.search("curiga alergi susu sapi");assertEquals("cow's milk",json.readTree(bodies.get(0)).path("search").asText());assertFalse(bodies.get(0).contains("curiga"));
    }
    @Test public void foodClassificationUsesPublishedExpansion()throws Exception{
        Result r=client((url,body,bearer)->url.equals(SatuSehatAllergyOnlineClient.KFA_BROWSER)?publicKfa():reference(url)).search("cow's milk");
        Candidate c=r.candidates.stream().filter(x -> x.code.equals("3718001")).findFirst().get();assertEquals("food",c.category);
    }
    @Test public void verifiedEnvironmentConceptGetsEnvironmentCategory()throws Exception{
        Result r=client((url,body,bearer)->url.equals(SatuSehatAllergyOnlineClient.KFA_BROWSER)?publicKfa():reference(url)).search("latex");
        assertEquals("environment",r.candidates.stream().filter(x -> x.code.equals("111088007")).findFirst().get().category);
    }
    @Test public void unclassifiedSnomedSubstanceRequiresCategoryReview()throws Exception{
        Result r=client((url,body,bearer)->url.equals(SatuSehatAllergyOnlineClient.KFA_BROWSER)?publicKfa():reference(url)).search("substance, example");
        assertEquals("",r.candidates.stream().filter(x -> x.code.equals("123456")).findFirst().get().category);
    }
    @Test public void fallbackUsesConfiguredKfaGetOnly()throws Exception{
        List<String> calls=new ArrayList<>();
        Result r=client((url,body,bearer)->{
            calls.add(url);
            if(url.equals(SatuSehatAllergyOnlineClient.KFA_BROWSER))throw new IOException("unavailable");
            if(url.startsWith("https://api-satusehat.kemkes.go.id/kfa-v2/products/all?")){assertNull(body);assertNotNull(bearer);return products();}
            assertNull(bearer);return reference(url);
        }).search("paracetamol");
        assertTrue(r.complete);assertTrue(calls.stream().noneMatch(x -> x.contains("/fhir-r4/")));assertEquals("91000101",r.candidates.get(0).code);
    }
    @Test public void fallbackNeverSendsCredentialsToUnexpectedHost()throws Exception{
        AtomicInteger auth=new AtomicInteger();
        SatuSehatAllergyOnlineClient c=new SatuSehatAllergyOnlineClient("https://untrusted.invalid/kfa-v2",()->{auth.incrementAndGet();return "synthetic";},
                (url,body,bearer)->{if(url.equals(SatuSehatAllergyOnlineClient.KFA_BROWSER))throw new IOException();return reference(url);});
        assertFalse(c.search("latex").complete);assertEquals(0,auth.get());
    }
    @Test public void failedProviderKeepsOtherCandidatesButPreventsAutoSelection()throws Exception{
        SatuSehatAllergyReferenceService svc=new SatuSehatAllergyReferenceService(client((url,body,bearer)->{
            if(url.equals(SatuSehatAllergyOnlineClient.KFA_BROWSER)||url.contains("/products/all?"))throw new IOException("offline");return reference(url);
        }));
        Lookup l=svc.search("susu sapi");assertFalse(l.result.candidates.isEmpty());assertNull(l.automatic());assertFalse(l.result.warning.isEmpty());
    }
    @Test public void malformedFoodCatalogueFailsClosed()throws Exception{
        Result r=client((url,body,bearer)->url.equals(SatuSehatAllergyOnlineClient.KFA_BROWSER)?publicKfa():url.equals(SatuSehatAllergyOnlineClient.FOODS)?"<html>changed</html>":csv()).search("paracetamol");
        assertFalse(r.complete);assertFalse(r.candidates.isEmpty());
    }
    @Test public void malformedCsvHeaderFailsClosed()throws Exception{
        Result r=client((url,body,bearer)->url.equals(SatuSehatAllergyOnlineClient.KFA_BROWSER)?publicKfa():url.equals(SatuSehatAllergyOnlineClient.SNOMED_SHEET)?"unavailable":reference(url)).search("paracetamol");
        assertFalse(r.complete);
    }
    @Test public void referenceIndexIsReusedWithinOneHour()throws Exception{
        AtomicInteger fetches=new AtomicInteger();
        SatuSehatAllergyOnlineClient c=client((url,body,bearer)->{if(url.equals(SatuSehatAllergyOnlineClient.KFA_BROWSER))return publicKfa();fetches.incrementAndGet();return reference(url);});
        c.search("paracetamol");c.search("latex");assertEquals(2,fetches.get());
    }
    @Test public void paginatedPublicSearchIsMarkedPartial()throws Exception{
        Result r=client((url,body,bearer)->url.equals(SatuSehatAllergyOnlineClient.KFA_BROWSER)?publicKfa().replace("totalPage\":1","totalPage\":2"):reference(url)).search("paracetamol");
        assertFalse(r.complete);
    }
    @Test public void truncatedFallbackIsMarkedPartial()throws Exception{
        Result r=client((url,body,bearer)->{if(url.equals(SatuSehatAllergyOnlineClient.KFA_BROWSER))throw new IOException();if(url.contains("/products/all?"))return products().replace("total\":1","total\":101");return reference(url);}).search("paracetamol");
        assertFalse(r.complete);
    }
    @Test public void edibleChemicalDoesNotAutomaticallyBecomeFoodAllergen()throws Exception{
        String chemical="{\"system\":\"http://snomed.info/sct\",\"code\":\"123456\",\"display\":\"Chemical example\"}";
        String html=foodHtml()+"<button data-clipboard-text=\""+org.apache.commons.lang3.StringEscapeUtils.escapeHtml4(chemical)+"\"></button>";
        SatuSehatAllergyReferenceService svc=new SatuSehatAllergyReferenceService(client((url,body,bearer)->url.equals(SatuSehatAllergyOnlineClient.KFA_BROWSER)?publicKfa():url.equals(SatuSehatAllergyOnlineClient.FOODS)?html:csv()));
        Lookup l=svc.search("chemical example");assertNull(l.automatic());
        assertEquals("",l.result.candidates.stream().filter(c -> c.code.equals("123456")).findFirst().get().category);
    }
}
