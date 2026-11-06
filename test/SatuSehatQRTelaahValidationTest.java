import bridging.SatuSehatQRTelaahValidation;
import org.junit.Test;
import static org.junit.Assert.*;
public class SatuSehatQRTelaahValidationTest {
    private void blocked(String patient,String practitioner,String encounter,String field){
        try{SatuSehatQRTelaahValidation.requireReferences(patient,practitioner,encounter);fail("Invalid reference must be blocked before POST/PUT");}
        catch(IllegalArgumentException expected){assertTrue(expected.getMessage().contains(field));}
    }
    @Test public void practitionerSearchTotalZeroIsBlocked(){blocked("patient-test","","encounter-test","Practitioner");}
    @Test public void nullPractitionerIsBlocked(){blocked("patient-test",null,"encounter-test","Practitioner");}
    @Test public void fullReferenceCannotBeUsedAsId(){blocked("patient-test","Practitioner/","encounter-test","Practitioner");}
    @Test public void emptyPatientIsBlocked(){blocked("","practitioner-test","encounter-test","Patient");}
    @Test public void emptyEncounterIsBlocked(){blocked("patient-test","practitioner-test","","Encounter");}
    @Test public void queryStringCannotBecomeResourceId(){blocked("patient-test","practitioner-test","x?query=1","Encounter");}
    @Test public void resolvedReferencesAreAccepted(){SatuSehatQRTelaahValidation.requireReferences("patient-test","practitioner-test","encounter-test");}
}
