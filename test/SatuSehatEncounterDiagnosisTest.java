import bridging.SatuSehatEncounterDiagnosis;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.sql.*;
import java.util.UUID;
import org.junit.*;
import static org.junit.Assert.*;

public class SatuSehatEncounterDiagnosisTest {
    private Connection db;
    @Before public void setup()throws Exception{
        db=DriverManager.getConnection("jdbc:h2:mem:"+UUID.randomUUID());
        exec("create table diagnosa_pasien(no_rawat varchar,kd_penyakit varchar,status varchar,prioritas int)");
        exec("create table satu_sehat_condition(no_rawat varchar,kd_penyakit varchar,status varchar,id_condition varchar)");
        exec("insert into diagnosa_pasien values('visit','test-code','Ranap',1)");
        exec("insert into satu_sehat_condition values('visit','test-code','Ranap','test-condition')");
    }
    @After public void close()throws Exception{db.close();}
    private void exec(String sql)throws Exception{try(Statement s=db.createStatement()){s.execute(sql);}}
    private ArrayNode load()throws Exception{return SatuSehatEncounterDiagnosis.load(db,new ObjectMapper(),"visit","Ranap");}
    @Test public void recordedConditionAndPriorityBecomeDischargeDiagnosis()throws Exception{ArrayNode n=load();assertEquals(1,n.size());assertEquals("Condition/test-condition",n.get(0).path("condition").path("reference").asText());assertEquals(1,n.get(0).path("rank").asInt());assertEquals("DD",n.get(0).path("use").path("coding").get(0).path("code").asText());}
    @Test(expected=SatuSehatEncounterDiagnosis.NotReady.class) public void unsentConditionIsHeld()throws Exception{exec("delete from satu_sehat_condition");load();}
    @Test(expected=SatuSehatEncounterDiagnosis.NotReady.class) public void anotherVisitConditionIsNotBorrowed()throws Exception{exec("update satu_sehat_condition set no_rawat='other'");load();}
    @Test(expected=SatuSehatEncounterDiagnosis.NotReady.class) public void outpatientConditionIsNotBorrowedForInpatient()throws Exception{exec("update satu_sehat_condition set status='Ralan'");load();}
    @Test(expected=SatuSehatEncounterDiagnosis.NotReady.class) public void missingPrimaryIsHeld()throws Exception{exec("update diagnosa_pasien set prioritas=2");load();}
    @Test(expected=SatuSehatEncounterDiagnosis.NotReady.class) public void partiallySentDiagnosesAreNotSilentlyDropped()throws Exception{exec("insert into diagnosa_pasien values('visit','secondary','Ranap',2)");load();}
    @Test(expected=SatuSehatEncounterDiagnosis.NotReady.class) public void malformedReferenceIsHeld()throws Exception{exec("update satu_sehat_condition set id_condition='Condition/'");load();}
    @Test public void secondaryDiagnosisKeepsRecordedRank()throws Exception{exec("insert into diagnosa_pasien values('visit','secondary','Ranap',2)");exec("insert into satu_sehat_condition values('visit','secondary','Ranap','test-secondary')");ArrayNode n=load();assertEquals(2,n.size());assertEquals(2,n.get(1).path("rank").asInt());}
    private void listFixture()throws Exception{
        exec("create table reg_periksa(no_rawat varchar,no_rkm_medis varchar,tgl_registrasi varchar,jam_reg varchar,stts varchar,status_lanjut varchar)");
        exec("create table pasien(no_rkm_medis varchar,nm_pasien varchar,no_ktp varchar)");
        exec("create table satu_sehat_encounter(no_rawat varchar,id_encounter varchar)");
        exec("create table penyakit(kd_penyakit varchar,nm_penyakit varchar)");
        exec("insert into reg_periksa values('visit','patient','2026-10-01','08:00:00','Sudah','Ranap')");
        exec("insert into pasien values('patient','Synthetic patient','test-nik')");
        exec("insert into satu_sehat_encounter values('visit','test-encounter')");
        exec("insert into penyakit values('test-code','Synthetic diagnosis')");
    }
    private int listRows(boolean search)throws Exception{
        try(PreparedStatement s=db.prepareStatement(SatuSehatEncounterDiagnosis.conditionListQuery(search))){
            s.setString(1,"2026-10-01");s.setString(2,"2026-11-07");if(search)for(int i=3;i<=10;i++)s.setString(i,"%visit%");
            try(ResultSet r=s.executeQuery()){int n=0;while(r.next())n++;return n;}
        }
    }
    @Test public void conditionListDoesNotRequireNotaAndSupportsKeyword()throws Exception{listFixture();assertEquals(1,listRows(false));assertEquals(1,listRows(true));}
    @Test public void conditionListDoesNotMixOutpatientDiagnosis()throws Exception{listFixture();exec("insert into diagnosa_pasien values('visit','test-code','Ralan',1)");assertEquals(1,listRows(false));}
    @Test public void conditionListRequiresActualEncounterID()throws Exception{listFixture();exec("update satu_sehat_encounter set id_encounter=''");assertEquals(0,listRows(false));}
    @Test public void conditionListUsesRegistrationRange()throws Exception{listFixture();exec("update reg_periksa set tgl_registrasi='2026-09-30'");assertEquals(0,listRows(false));}
}
