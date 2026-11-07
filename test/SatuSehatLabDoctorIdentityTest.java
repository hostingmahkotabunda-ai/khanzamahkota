import bridging.SatuSehatLabDoctorIdentity;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Synthetic identity mapping, no live database or API. H2 2.2.224 at test runtime. */
public class SatuSehatLabDoctorIdentityTest {
    private Connection db;
    private Path dir,file;
    private SatuSehatLabDoctorIdentity links;
    @Before public void setup()throws Exception{
        db=DriverManager.getConnection("jdbc:h2:mem:"+UUID.randomUUID());
        try(Statement s=db.createStatement()){
            s.execute("create table dokter(kd_dokter varchar,nm_dokter varchar)");
            s.execute("create table pegawai(nik varchar,nama varchar,no_ktp varchar)");
            s.execute("insert into dokter values('doctor-a','Clinical Doctor')");
            s.execute("insert into pegawai values('employee-a','Clinical Doctor','1111111111111111')");
        }
        dir=Files.createTempDirectory("lab-doctor-test-");file=dir.resolve("links.iyem");links=new SatuSehatLabDoctorIdentity(file);
    }
    @After public void close()throws Exception{
        db.close();try(java.util.stream.Stream<Path> paths=Files.list(dir)){for(Path p:(Iterable<Path>)paths::iterator)Files.delete(p);}Files.delete(dir);
    }
    @Test public void sameNameDoesNotAutomaticallyLinkDifferentCodes()throws Exception{
        assertEquals("",links.resolve(db,"doctor-a","Clinical Doctor").nik);assertFalse(Files.exists(file));
    }
    @Test public void existingDirectCodeRemainsSupported()throws Exception{
        assertEquals("1111111111111111",links.resolve(db,"employee-a","Clinical Doctor").nik);
    }
    @Test public void explicitLinkPersistsWithoutChangingClinicalDoctor()throws Exception{
        links.save(db,"doctor-a","employee-a");
        SatuSehatLabDoctorIdentity.Identity id=new SatuSehatLabDoctorIdentity(file).resolve(db,"doctor-a","Clinical Doctor");
        assertEquals("Clinical Doctor",id.name);assertEquals("1111111111111111",id.nik);
        assertFalse(new String(Files.readAllBytes(file),"UTF-8").contains("1111111111111111"));
        try(Statement s=db.createStatement();ResultSet r=s.executeQuery("select kd_dokter from dokter")){assertTrue(r.next());assertEquals("doctor-a",r.getString(1));}
    }
    @Test public void unknownEmployeeCannotBeSaved()throws Exception{
        try{links.save(db,"doctor-a","missing");fail();}catch(IllegalArgumentException expected){}assertFalse(Files.exists(file));
    }
    @Test public void unknownDoctorCannotBeSaved()throws Exception{
        try{links.save(db,"missing","employee-a");fail();}catch(IllegalArgumentException expected){}assertFalse(Files.exists(file));
    }
    @Test public void deletedEmployeeDoesNotTriggerNameFallback()throws Exception{
        links.save(db,"doctor-a","employee-a");try(Statement s=db.createStatement()){s.execute("delete from pegawai");}
        assertEquals("",links.resolve(db,"doctor-a","Clinical Doctor").nik);
    }
    @Test public void malformedFileIsNotOverwritten()throws Exception{
        Files.write(file,"[]".getBytes("UTF-8"));
        try{links.save(db,"doctor-a","employee-a");fail();}catch(IllegalStateException expected){}
        assertEquals("[]",new String(Files.readAllBytes(file),"UTF-8"));
    }
    @Test public void unknownFHIRIdentitiesAreHeld(){
        assertFalse(SatuSehatLabDoctorIdentity.validReferences("test-patient",""));
        assertFalse(SatuSehatLabDoctorIdentity.validReferences("","test-practitioner"));
        assertFalse(SatuSehatLabDoctorIdentity.validReferences("test-patient","Practitioner/"));
        assertTrue(SatuSehatLabDoctorIdentity.validReferences("test-patient","test-practitioner"));
    }
}
