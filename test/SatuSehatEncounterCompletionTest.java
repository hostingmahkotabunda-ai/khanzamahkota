import bridging.SatuSehatEncounterCompletion;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.sql.*;
import java.time.*;
import java.util.UUID;
import org.junit.*;
import static org.junit.Assert.*;

/** Actual query execution on synthetic H2 only; no clinical API or configured database. */
public class SatuSehatEncounterCompletionTest {
    private Connection db;
    private final ObjectMapper mapper=new ObjectMapper();
    private final Clock clock=Clock.fixed(Instant.parse("2026-11-07T12:00:00Z"),ZoneOffset.UTC);
    @Before public void setup()throws Exception{
        db=DriverManager.getConnection("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL");
        exec("create table reg_periksa(no_rawat varchar,no_rkm_medis varchar,tgl_registrasi varchar,jam_reg varchar,kd_dokter varchar,kd_poli varchar,stts varchar,status_lanjut varchar,status_bayar varchar)");
        exec("create table pasien(no_rkm_medis varchar,nm_pasien varchar,no_ktp varchar)");
        exec("create table pegawai(nik varchar,nama varchar,no_ktp varchar)");
        exec("create table poliklinik(kd_poli varchar,nm_poli varchar)");
        exec("create table satu_sehat_mapping_lokasi_ralan(kd_poli varchar,id_lokasi_satusehat varchar)");
        exec("create table satu_sehat_encounter(no_rawat varchar,id_encounter varchar)");
        exec("create table kamar_inap(no_rawat varchar,tgl_masuk varchar,jam_masuk varchar,tgl_keluar varchar,jam_keluar varchar,stts_pulang varchar)");
        exec("create table pemeriksaan_ralan(no_rawat varchar,tgl_perawatan varchar,jam_rawat varchar)");
        exec("create table nota_jalan(no_rawat varchar,tanggal varchar,jam varchar)");
        exec("create table resume_medis_ralan_v2(no_rawat varchar,tgl_keluar varchar,jam_keluar varchar)");
        exec("insert into reg_periksa values('visit','patient','2026-10-01','08:00:00','doctor','clinic','Sudah','Ralan','Sudah Bayar')");
        exec("insert into pasien values('patient','Synthetic patient','test-nik')");
        exec("insert into pegawai values('doctor','Synthetic doctor','test-nik')");
        exec("insert into poliklinik values('clinic','Synthetic clinic')");
        exec("insert into satu_sehat_mapping_lokasi_ralan values('clinic','test-location')");
        exec("insert into pemeriksaan_ralan values('visit','2026-10-01','09:00:00')");
    }
    @After public void close()throws Exception{db.close();}
    private void exec(String sql)throws Exception{try(Statement s=db.createStatement()){s.execute(sql);}}
    private int rows(boolean inpatient,boolean resume,boolean search)throws Exception{
        try(PreparedStatement s=db.prepareStatement(SatuSehatEncounterCompletion.query(inpatient,search,resume))){
            s.setString(1,"2026-10-01");s.setString(2,"2026-11-07");if(search)for(int n=3;n<=10;n++)s.setString(n,"%visit%");
            try(ResultSet r=s.executeQuery()){int count=0;while(r.next())count++;return count;}
        }
    }
    private void inpatient()throws Exception{exec("update reg_periksa set status_lanjut='Ranap',status_bayar='Belum Bayar'");}
    private void segment(String date,String exit,String status)throws Exception{
        exec("insert into kamar_inap values('visit','"+date+"','10:00:00','"+exit+"','00:00:00','"+status+"')");
    }
    @Test public void paidOutpatientVisibleWithoutNota()throws Exception{assertEquals(1,rows(false,true,false));}
    @Test public void unpaidOutpatientHiddenEvenWithNota()throws Exception{
        exec("update reg_periksa set status_bayar='Belum Bayar'");exec("insert into nota_jalan values('visit','2026-10-01','11:00:00')");assertEquals(0,rows(false,true,false));
    }
    @Test public void cancelledPaidVisitHidden()throws Exception{exec("update reg_periksa set stts='Batal'");assertEquals(0,rows(false,true,false));}
    @Test public void dischargedInpatientWithoutNotaAndWithoutPaymentVisible()throws Exception{inpatient();segment("2026-10-01","2026-10-03","Sembuh");assertEquals(1,rows(true,true,false));}
    @Test public void activeInpatientHiddenEvenIfPaid()throws Exception{inpatient();exec("update reg_periksa set status_bayar='Sudah Bayar'");segment("2026-10-01","0000-00-00","-");assertEquals(0,rows(true,true,false));}
    @Test public void transferIsNotDischarge()throws Exception{inpatient();segment("2026-10-01","2026-10-03","Pindah Kamar");assertEquals(0,rows(true,true,false));}
    @Test public void anyActiveSegmentBlocksHistoricalDischarge()throws Exception{inpatient();segment("2026-10-01","2026-10-03","Sembuh");segment("2026-10-04","0000-00-00","-");assertEquals(0,rows(true,true,false));}
    @Test public void latestTransferDoesNotReuseEarlierDischarge()throws Exception{inpatient();segment("2026-10-01","2026-10-03","Sembuh");segment("2026-10-04","2026-10-05","Pindah Kamar");assertEquals(0,rows(true,true,false));}
    @Test public void multipleSegmentsReturnOneVisit()throws Exception{inpatient();segment("2026-10-01","2026-10-03","Pindah Kamar");segment("2026-10-03","2026-10-05","Sembuh");assertEquals(1,rows(true,true,false));}
    @Test public void zeroDischargeDateHidden()throws Exception{inpatient();segment("2026-10-01","0000-00-00","Sembuh");assertEquals(0,rows(true,true,false));}
    @Test public void missingResumeTableDoesNotBreakPaidOutpatientList()throws Exception{exec("drop table resume_medis_ralan_v2");assertEquals(1,rows(false,false,false));}
    @Test public void optionalKeywordWorks()throws Exception{assertEquals(1,rows(false,true,true));}
    @Test public void registrationFilterAppliesWithoutNota()throws Exception{exec("update reg_periksa set tgl_registrasi='2026-09-30'");assertEquals(0,rows(false,true,false));}
    @Test public void activeRoomAlsoBlocksPaidOutpatient()throws Exception{segment("2026-10-01","0000-00-00","-");assertEquals(0,rows(false,true,false));}
    private ObjectNode completed(){ObjectNode n=mapper.createObjectNode();SatuSehatEncounterCompletion.complete(n,"2026-10-01T00:00:00Z","2026-10-01T01:00:00Z","2026-10-03T00:00:00Z",clock);return n;}
    @Test public void finishedPayloadUsesActualTimesAndThreeTransitions(){ObjectNode n=completed();assertEquals("finished",n.path("status").asText());assertEquals("2026-10-03T00:00:00Z",n.path("period").path("end").asText());assertEquals(3,n.path("statusHistory").size());assertEquals("in-progress",n.path("statusHistory").get(1).path("status").asText());}
    @Test(expected=IllegalArgumentException.class) public void unknownTimeIsHeld(){SatuSehatEncounterCompletion.complete(mapper.createObjectNode(),"2026-10-01T00:00:00Z","","",clock);}
    @Test(expected=IllegalArgumentException.class) public void futureEndIsHeld(){SatuSehatEncounterCompletion.complete(mapper.createObjectNode(),"2026-10-01T00:00:00Z","2026-10-01T01:00:00Z","2026-11-08T00:00:00Z",clock);}
    @Test(expected=IllegalArgumentException.class) public void endBeforeCareIsHeld(){SatuSehatEncounterCompletion.complete(mapper.createObjectNode(),"2026-10-01T00:00:00Z","2026-10-03T01:00:00Z","2026-10-03T00:00:00Z",clock);}
    @Test public void putRetainsClinicalFieldsAndAncPtm()throws Exception{
        ObjectNode remote=(ObjectNode)mapper.readTree("{\"diagnosis\":[{\"condition\":{\"reference\":\"Condition/test\"}}],\"episodeOfCare\":[{\"reference\":\"EpisodeOfCare/anc\"},{\"reference\":\"EpisodeOfCare/ptm\"}],\"identifier\":[{\"system\":\"anc-test\",\"value\":\"K1\"}]}");
        ObjectNode merged=SatuSehatEncounterCompletion.mergeCompleted(completed(),remote);assertEquals(remote.path("diagnosis"),merged.path("diagnosis"));assertEquals(2,merged.path("episodeOfCare").size());assertEquals(remote.path("identifier"),merged.path("identifier"));
    }
    @Test public void putKeepsExistingHistoryAndClosesOpenState()throws Exception{
        ObjectNode remote=(ObjectNode)mapper.readTree("{\"statusHistory\":[{\"status\":\"in-progress\",\"period\":{\"start\":\"2026-10-01T01:00:00Z\"}}]}");
        ObjectNode merged=SatuSehatEncounterCompletion.mergeCompleted(completed(),remote);assertEquals(2,merged.path("statusHistory").size());assertEquals("2026-10-03T00:00:00Z",merged.path("statusHistory").get(0).path("period").path("end").asText());assertFalse(remote.path("statusHistory").get(0).path("period").has("end"));
    }
    @Test(expected=IllegalArgumentException.class) public void inconsistentServerFinishIsHeld()throws Exception{
        ObjectNode remote=(ObjectNode)mapper.readTree("{\"statusHistory\":[{\"status\":\"finished\",\"period\":{\"start\":\"2026-10-04T01:00:00Z\"}}]}");SatuSehatEncounterCompletion.mergeCompleted(completed(),remote);
    }
    @Test(expected=IllegalArgumentException.class) public void cancelledServerEncounterIsNotRevived()throws Exception{
        ObjectNode remote=mapper.createObjectNode();remote.put("status","cancelled");SatuSehatEncounterCompletion.mergeCompleted(completed(),remote);
    }
    @Test(expected=IllegalArgumentException.class) public void differentServerStartIsNotOverwritten()throws Exception{
        ObjectNode remote=mapper.createObjectNode();remote.putObject("period").put("start","2026-10-01T02:00:00Z");SatuSehatEncounterCompletion.mergeCompleted(completed(),remote);
    }
    @Test public void everyFinishedHistoryPeriodHasStartAndEnd(){
        for(JsonNode h:completed().path("statusHistory")){assertTrue(h.path("period").hasNonNull("start"));assertTrue(h.path("period").hasNonNull("end"));}
    }
    @Test public void bootstrapDoesNotClaimFinishedWithoutDiagnosis(){
        ObjectNode n=completed();SatuSehatEncounterCompletion.initial(n);assertEquals("arrived",n.path("status").asText());assertFalse(n.path("period").has("end"));assertEquals(1,n.path("statusHistory").size());assertTrue(n.path("statusHistory").get(0).path("period").has("end"));
    }
    @Test public void putAfterBootstrapAppendsCareAndClosedFinished()throws Exception{
        ObjectNode initial=completed();SatuSehatEncounterCompletion.initial(initial);
        ObjectNode n=SatuSehatEncounterCompletion.mergeCompleted(completed(),initial);assertEquals(3,n.path("statusHistory").size());
        for(JsonNode h:n.path("statusHistory"))assertTrue(h.path("period").hasNonNull("end"));
    }
    @Test public void previouslyFinishedOpenHistoryIsClosedAtRecordedDischarge()throws Exception{
        ObjectNode remote=completed();((ObjectNode)remote.path("statusHistory").get(2).path("period")).remove("end");
        ObjectNode n=SatuSehatEncounterCompletion.mergeCompleted(completed(),remote);assertEquals(3,n.path("statusHistory").size());assertEquals(n.path("period").path("end"),n.path("statusHistory").get(2).path("period").path("end"));
    }
}
