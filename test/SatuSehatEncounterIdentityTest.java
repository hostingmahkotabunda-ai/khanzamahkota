import bridging.SatuSehatEncounterIdentity;
import org.junit.Test;
import static org.junit.Assert.*;
public class SatuSehatEncounterIdentityTest {
    private void blocked(String patient,String practitioner,String field){
        try{SatuSehatEncounterIdentity.requireIds(patient,practitioner);fail("Empty/malformed reference must be blocked");}
        catch(IllegalArgumentException e){assertTrue(e.getMessage().contains(field));}
    }
    @Test public void emptyPatientFromSearchTotalZeroIsBlocked(){blocked("","test-practitioner","Patient");}
    @Test public void nullPatientIsBlocked(){blocked(null,"test-practitioner","Patient");}
    @Test public void whitespacePatientIsBlocked(){blocked(" ","test-practitioner","Patient");}
    @Test public void emptyPractitionerIsBlocked(){blocked("test-patient","","Practitioner");}
    @Test public void fullReferenceCannotBeUsedAsResourceId(){blocked("Patient/test-patient","test-practitioner","Patient");}
    @Test public void malformedPatientIdIsBlocked(){blocked("test-patient?other=1","test-practitioner","Patient");}
    @Test public void resolvedIdsAreAccepted(){SatuSehatEncounterIdentity.requireIds("test-patient","test-practitioner");}
}
