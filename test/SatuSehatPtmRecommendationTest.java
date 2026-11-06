import bridging.SatuSehatPtmRecommendation;
import org.junit.Test;
import static org.junit.Assert.*;

public class SatuSehatPtmRecommendationTest {
    @Test public void coronaryCodeAndNameSuggestCad(){assertEquals("CAD",SatuSehatPtmRecommendation.recommend("I25.1","Atherosclerotic heart disease").type);}
    @Test public void cerebrovascularCodeAndNameSuggestCvd(){assertEquals("CVD",SatuSehatPtmRecommendation.recommend("I63.9","Cerebral infarction, unspecified").type);}
    @Test public void chronicKidneyCodeSuggestsCkd(){assertEquals("CKD",SatuSehatPtmRecommendation.recommend("N18.5","Chronic kidney disease stage 5").type);}
    @Test public void malignantNeoplasmCodeSuggestsCancer(){assertEquals("cancer",SatuSehatPtmRecommendation.recommend("C50.9","Malignant neoplasm of breast").type);}
    @Test public void normalizationAcceptsLowercaseAndCompactCode(){assertEquals("CAD",SatuSehatPtmRecommendation.recommend(" i251 ","Coronary artery disease").type);}
    @Test public void hypertensionDoesNotChooseBetweenCadAndCkd(){
        SatuSehatPtmRecommendation.Result r=SatuSehatPtmRecommendation.recommend("I10","Essential hypertension");
        assertEquals("",r.type);assertTrue(r.explanation.contains("CAD/CKD"));assertTrue(r.explanation.contains("risiko"));
    }
    @Test public void diabetesDoesNotAssertKidneyDisease(){
        SatuSehatPtmRecommendation.Result r=SatuSehatPtmRecommendation.recommend("E11.9","Type 2 diabetes mellitus");
        assertEquals("",r.type);assertTrue(r.explanation.contains("tidak menetapkan penyakit ginjal kronis"));
    }
    @Test public void conflictingCodeAndNameRequireReview(){
        assertEquals("",SatuSehatPtmRecommendation.recommend("I25.1","Chronic kidney disease").type);
    }
    @Test public void multipleDiseaseNamesRequireReview(){
        assertEquals("",SatuSehatPtmRecommendation.recommend("I25.1","Coronary artery disease and chronic kidney disease").type);
    }
    @Test public void nameOnlyIsSuggestionWithoutPrefill(){
        SatuSehatPtmRecommendation.Result r=SatuSehatPtmRecommendation.recommend("","Penyakit jantung koroner");
        assertEquals("",r.type);assertTrue(r.explanation.contains("CAD"));
    }
    @Test public void unsupportedCodeDoesNotGetOverriddenByName(){assertEquals("",SatuSehatPtmRecommendation.recommend("R07.4","Suspected coronary artery disease").type);}
    @Test public void multipleCodesNotSilentlyReducedToFirstCode(){assertEquals("",SatuSehatPtmRecommendation.recommend("I25.1, N18.5","Coronary artery disease").type);}
    @Test public void benignUterineTumorDoesNotBecomeCancer(){assertEquals("",SatuSehatPtmRecommendation.recommend("D25.9","Leiomyoma of uterus").type);}
    @Test public void acuteKidneyFailureDoesNotBecomeChronicKidneyDisease(){assertEquals("",SatuSehatPtmRecommendation.recommend("N17.9","Acute renal failure").type);}
    @Test public void heartFailureDoesNotAutomaticallyBecomeCoronaryDisease(){assertEquals("",SatuSehatPtmRecommendation.recommend("I50.0","Congestive heart failure").type);}
    @Test public void pregnancyDirectsOperatorToAnc(){
        SatuSehatPtmRecommendation.Result r=SatuSehatPtmRecommendation.recommend("Z34.9","Supervision of normal pregnancy");
        assertEquals("",r.type);assertTrue(r.explanation.contains("Atur ANC"));
    }
    @Test public void uncertainAndNegatedNamesDoNotPrefill(){
        assertEquals("",SatuSehatPtmRecommendation.recommend("C50.9","Suspected breast cancer").type);
        assertEquals("",SatuSehatPtmRecommendation.recommend("I63.9","No evidence of stroke").type);
    }
    @Test public void missingDiagnosisNeverDefaultsToCad(){assertEquals("",SatuSehatPtmRecommendation.recommend(null,null).type);}
    @Test public void outOfRangeCancerCodeNotRecognized(){assertEquals("",SatuSehatPtmRecommendation.recommend("C98","Other disease").type);}
    @Test public void inSituCodeRequiresManualReview(){assertEquals("",SatuSehatPtmRecommendation.recommend("D05.9","Carcinoma in situ of breast").type);}
    @Test public void normalAndHighRiskPregnancyRecommendAnc(){
        for(String code:new String[]{"Z34.9","Z35.9","Z36.9"}){
            SatuSehatPtmRecommendation.Result r=SatuSehatPtmRecommendation.recommend(code,"Antenatal supervision");
            assertTrue(r.maternity);assertEquals("",r.type);assertTrue(r.route.contains("ANC"));assertTrue(r.explanation.contains("Atur ANC"));
        }
    }
    @Test public void preeclampsiaDoesNotBecomeCadOrCkd(){
        SatuSehatPtmRecommendation.Result r=SatuSehatPtmRecommendation.recommend("O14.1","Severe pre-eclampsia");
        assertEquals("",r.type);assertTrue(r.explanation.contains("Preeklamsia"));assertTrue(r.maternity);
    }
    @Test public void eclampsiaDoesNotBecomeStroke(){
        SatuSehatPtmRecommendation.Result r=SatuSehatPtmRecommendation.recommend("O15.0","Eclampsia in pregnancy");
        assertEquals("",r.type);assertTrue(r.explanation.contains("tidak otomatis merupakan stroke"));
    }
    @Test public void gestationalDiabetesIsDistinguishedFromPreexistingDiabetes(){
        SatuSehatPtmRecommendation.Result gestational=SatuSehatPtmRecommendation.recommend("O24.4","Diabetes mellitus arising in pregnancy");
        SatuSehatPtmRecommendation.Result preexisting=SatuSehatPtmRecommendation.recommend("O24.1","Pre-existing diabetes mellitus");
        assertEquals("",gestational.type);assertTrue(gestational.explanation.contains("gestasional"));
        assertEquals("",preexisting.type);assertTrue(preexisting.explanation.contains("sebelum kehamilan"));
    }
    @Test public void gestationalHypertensionDoesNotBecomeCoronaryDisease(){
        SatuSehatPtmRecommendation.Result r=SatuSehatPtmRecommendation.recommend("O13","Gestational hypertension");
        assertEquals("",r.type);assertTrue(r.explanation.contains("Hipertensi gestasional"));
    }
    @Test public void hypertensiveRenalDiseaseGetsSpecificConditionalAdvice(){
        SatuSehatPtmRecommendation.Result r=SatuSehatPtmRecommendation.recommend("O10.2","Pre-existing hypertensive renal disease complicating pregnancy");
        assertEquals("",r.type);assertTrue(r.explanation.contains("CKD"));assertTrue(r.explanation.contains("N18"));
    }
    @Test public void circulatoryPregnancyCodeDoesNotAlwaysMeanCad(){
        SatuSehatPtmRecommendation.Result r=SatuSehatPtmRecommendation.recommend("O99.4","Diseases of circulatory system complicating pregnancy");
        assertEquals("",r.type);assertTrue(r.explanation.contains("I20–I25"));assertTrue(r.explanation.contains("I60–I69"));
    }
    @Test public void namesCanSuggestUnderlyingDiseaseWithoutChangingSelectedObstetricCondition(){
        SatuSehatPtmRecommendation.Result r=SatuSehatPtmRecommendation.recommend("O99.4","Pregnancy complicated by coronary artery disease");
        assertEquals("",r.type);assertTrue(r.explanation.contains("petunjuk CAD"));
    }
    @Test public void publishedUnderlyingConditionProvidesActionableHint(){
        SatuSehatPtmRecommendation.Result r=SatuSehatPtmRecommendation.recommend("O99.4","Circulatory disease complicating pregnancy",
                java.util.Arrays.asList(new SatuSehatPtmRecommendation.Diagnosis("I25.1","Coronary artery disease")));
        assertEquals("",r.type);assertTrue(r.explanation.contains("I25.1 → CAD"));assertTrue(r.explanation.contains("Condition penyerta tersebut"));
    }
    @Test public void preeclampsiaAndDocumentedKidneyDiseaseRemainSeparate(){
        SatuSehatPtmRecommendation.Result r=SatuSehatPtmRecommendation.recommend("O14.1","Severe pre-eclampsia",
                java.util.Arrays.asList(new SatuSehatPtmRecommendation.Diagnosis("N18.5","Chronic kidney disease")));
        assertEquals("",r.type);assertTrue(r.explanation.contains("N18.5 → CKD"));assertTrue(r.route.contains("PTM penyerta"));
    }
    @Test public void missingUnderlyingConditionIsExplainedInsteadOfFabricated(){
        SatuSehatPtmRecommendation.Result r=SatuSehatPtmRecommendation.recommend("O99.4","Circulatory disease complicating pregnancy",java.util.Collections.emptyList());
        assertEquals("",r.type);assertTrue(r.explanation.contains("Belum ditemukan Condition penyerta"));
    }
    @Test public void deliveryAndPostnatalCareDoNotSuggestAncAsPrimaryRoute(){
        assertFalse(SatuSehatPtmRecommendation.recommend("O82","Delivery by caesarean section").route.contains("ANC"));
        assertTrue(SatuSehatPtmRecommendation.recommend("O82","Delivery by caesarean section").route.contains("INC"));
        assertTrue(SatuSehatPtmRecommendation.recommend("Z39.2","Routine postpartum follow-up").route.contains("PNC"));
        assertFalse(SatuSehatPtmRecommendation.recommend("O15.2","Eclampsia in puerperium").route.contains("ANC"));
    }
    @Test public void pregnancyLossDoesNotSuggestRoutineAnc(){
        SatuSehatPtmRecommendation.Result r=SatuSehatPtmRecommendation.recommend("O03.9","Spontaneous abortion");
        assertEquals("",r.type);assertTrue(r.route.contains("kehilangan kehamilan"));
    }
    @Test public void urinaryInfectionAndAnemiaAreNotTurnedIntoPtm(){
        assertEquals("",SatuSehatPtmRecommendation.recommend("O23.0","Infection of kidney in pregnancy").type);
        assertEquals("",SatuSehatPtmRecommendation.recommend("O99.0","Anaemia complicating pregnancy").type);
    }
    @Test public void nonMaternityConditionRemainsSelectedDespiteOtherDiagnoses(){
        SatuSehatPtmRecommendation.Result r=SatuSehatPtmRecommendation.recommend("I25.1","Coronary artery disease",
                java.util.Arrays.asList(new SatuSehatPtmRecommendation.Diagnosis("N18.5","Chronic kidney disease")));
        assertEquals("CAD",r.type);assertFalse(r.maternity);
    }
}
