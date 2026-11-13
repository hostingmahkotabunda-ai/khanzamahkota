import bridging.SatuSehatNutritionOrderService;
import java.io.IOException;
import java.net.*;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.http.*;
import org.springframework.http.client.*;
import org.springframework.web.client.*;

/** Transport interception only: these tests never connect to a server or use real identifiers. */
public class SatuSehatNutritionOrderHttpTest {
    @Test public void encodedIdentifierIsNotEncodedAgain()throws Exception{
        for(String type:new String[]{"Patient","Practitioner"}){
            final URI[] captured=new URI[1];
            RestTemplate rest=new RestTemplate(new ClientHttpRequestFactory(){public ClientHttpRequest createRequest(URI uri,HttpMethod method)throws IOException{captured[0]=uri;throw new IOException("offline transport interception");}});
            String identifier="https://fhir.kemkes.go.id/id/nik|0000000000000000";
            String path="/"+type+"?identifier="+URLEncoder.encode(identifier,"UTF-8");
            try{SatuSehatNutritionOrderService.readResource(rest,"https://example.invalid/fhir-r4/v1",path,new HttpHeaders());fail();}catch(SatuSehatNutritionOrderService.SafeFailure expected){}
            assertNotNull(captured[0]);assertFalse(captured[0].getRawQuery().contains("%25"));assertEquals("identifier="+identifier,captured[0].getQuery());
        }
    }
    @Test public void safePhaseSurvivesWorkerAndSensitiveRootCause(){
        Throwable e=new ExecutionException(new SatuSehatNutritionOrderService.SafeFailure("Pemeriksaan Patient ditolak SATUSEHAT (HTTP 400).",new RuntimeException("private-url private-token private-nik")));
        String message=SatuSehatNutritionOrderService.errorMessage(e);assertTrue(message.contains("Patient"));assertTrue(message.contains("400"));assertFalse(message.contains("private"));
    }
    @Test public void sqlFailureIncludesCodesButNeverStatementOrValues(){String message=SatuSehatNutritionOrderService.errorMessage(new ExecutionException(new SQLException("private-patient private-query","42S22",1054)));assertTrue(message.contains("42S22"));assertTrue(message.contains("1054"));assertFalse(message.contains("private"));}
    @Test public void httpFailureNeverUsesRawStatusText(){String message=SatuSehatNutritionOrderService.errorMessage(new ExecutionException(new HttpClientErrorException(HttpStatus.BAD_REQUEST,"private-patient private-token")));assertTrue(message.contains("400"));assertFalse(message.contains("private"));}
}
