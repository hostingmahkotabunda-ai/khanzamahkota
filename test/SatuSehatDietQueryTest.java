import bridging.SatuSehatDietQuery;
import java.sql.*;
import java.util.UUID;
import org.junit.*;
import static org.junit.Assert.*;

/** Actual diet query, synthetic in-memory H2; no live database or API. */
public class SatuSehatDietQueryTest {
    private Connection db;
    @Before public void setup()throws Exception{
        db=DriverManager.getConnection("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL");
        exec("create table reg_periksa(no_rawat varchar,no_rkm_medis varchar,tgl_registrasi varchar,jam_reg varchar,stts varchar,status_lanjut varchar,status_bayar varchar)");
        exec("create table pasien(no_rkm_medis varchar,nm_pasien varchar,no_ktp varchar)");
        exec("create table satu_sehat_encounter(no_rawat varchar,id_encounter varchar)");
        exec("create table catatan_adime_gizi(no_rawat varchar,tanggal varchar,nip varchar,instruksi varchar)");
        exec("create table pegawai(nik varchar,nama varchar,no_ktp varchar)");
        exec("create table satu_sehat_diet(no_rawat varchar,tanggal varchar,id_diet varchar)");
        exec("create table kamar_inap(no_rawat varchar,tgl_masuk varchar,jam_masuk varchar,tgl_keluar varchar,stts_pulang varchar)");
        exec("insert into reg_periksa values('visit','patient','2026-10-01','08:00:00','Sudah','Ranap','Belum Bayar')");
        exec("insert into pasien values('patient','Synthetic patient','test-nik')");
        exec("insert into satu_sehat_encounter values('visit','test-encounter')");
        exec("insert into catatan_adime_gizi values('visit','2026-10-02 10:00:00','staff','Instruksi sintetis')");
        exec("insert into pegawai values('staff','Synthetic staff','test-nik')");
        exec("insert into kamar_inap values('visit','2026-10-01','09:00:00','2026-10-03','Sembuh')");
    }
    @After public void close()throws Exception{db.close();}
    private void exec(String sql)throws Exception{try(Statement s=db.createStatement()){s.execute(sql);}}
    private int rows(boolean keyword)throws Exception{
        try(PreparedStatement s=db.prepareStatement(SatuSehatDietQuery.query(keyword))){
            s.setString(1,"2026-10-01");s.setString(2,"2026-11-07");if(keyword)for(int i=3;i<=8;i++)s.setString(i,"%visit%");
            try(ResultSet r=s.executeQuery()){int n=0;while(r.next())n++;return n;}
        }
    }
    @Test public void dischargedInpatientVisibleWithoutAnyNotaTable()throws Exception{assertEquals(1,rows(false));}
    @Test public void activeInpatientHidden()throws Exception{exec("update kamar_inap set stts_pulang='-',tgl_keluar='0000-00-00'");assertEquals(0,rows(false));}
    @Test public void transferIsNotDischarge()throws Exception{exec("update kamar_inap set stts_pulang='Pindah Kamar'");assertEquals(0,rows(false));}
    @Test public void activeSegmentOverridesHistoricalDischarge()throws Exception{exec("insert into kamar_inap values('visit','2026-10-04','09:00:00','0000-00-00','-')");assertEquals(0,rows(false));}
    @Test public void newerTransferDoesNotReuseOlderDischarge()throws Exception{exec("insert into kamar_inap values('visit','2026-10-04','09:00:00','2026-10-05','Pindah Kamar')");assertEquals(0,rows(false));}
    @Test public void blankEncounterIsHeld()throws Exception{exec("update satu_sehat_encounter set id_encounter=' '");assertEquals(0,rows(false));}
    @Test public void emptyInstructionsAreExcluded()throws Exception{exec("update catatan_adime_gizi set instruksi='  '");assertEquals(0,rows(false));}
    @Test public void missingStaffDoesNotInventAuthor()throws Exception{exec("delete from pegawai");assertEquals(0,rows(false));}
    @Test public void paidOutpatientVisibleWithoutNota()throws Exception{exec("delete from kamar_inap");exec("update reg_periksa set status_lanjut='Ralan',status_bayar='Sudah Bayar'");assertEquals(1,rows(false));}
    @Test public void unpaidOutpatientIsExcluded()throws Exception{exec("delete from kamar_inap");exec("update reg_periksa set status_lanjut='Ralan'");assertEquals(0,rows(false));}
    @Test public void registrationFilterAndSearchWork()throws Exception{assertEquals(1,rows(true));exec("update reg_periksa set tgl_registrasi='2026-09-30'");assertEquals(0,rows(false));}
    @Test public void roomHistoryDoesNotDuplicateDiet()throws Exception{exec("insert into kamar_inap values('visit','2026-09-30','09:00:00','2026-10-01','Pindah Kamar')");assertEquals(1,rows(false));}
    @Test public void distinctDietNotesAreRetained()throws Exception{exec("insert into catatan_adime_gizi values('visit','2026-10-03 10:00:00','staff','Instruksi lanjutan')");assertEquals(2,rows(false));}
    @Test public void previouslySentDietRemainsAvailableForUpdate()throws Exception{exec("insert into satu_sehat_diet values('visit','2026-10-02 10:00:00','existing-diet')");assertEquals(1,rows(false));}
}
