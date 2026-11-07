import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Runs the actual list queries on synthetic H2 data, never on the configured database.
 * Test runtime: H2 2.2.224 (MySQL mode); no application dialog is instantiated. */
public class SatuSehatLabPKVisibilityTest {
    private Connection db;
    private final Map<String,String> queries=new LinkedHashMap<>();
    @Before public void setup() throws Exception {
        db=DriverManager.getConnection("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;NON_KEYWORDS=SYSTEM");
        Map<String,Set<String>> schema=new LinkedHashMap<>();
        for(String module:Arrays.asList("Specimen","Observation","DiagnosticReport")){
            String src=new String(Files.readAllBytes(Paths.get("src/bridging/SatuSehatKirim"+module+"LabPK.java")),StandardCharsets.UTF_8);
            src=src.substring(src.indexOf("private void tampil()"));
            Matcher chunk=Pattern.compile("ps=koneksi\\.prepareStatement\\((.*?)\\(TCari\\.getText",Pattern.DOTALL).matcher(src);
            assertTrue(chunk.find());
            Matcher literals=Pattern.compile("\"([^\"]*)\"").matcher(chunk.group(1));
            StringBuilder sql=new StringBuilder();while(literals.find())sql.append(literals.group(1));
            queries.put(module,sql.toString());
            assertFalse("A second query would duplicate Ralan/Ranap",chunk.find());
            Matcher columns=Pattern.compile("([a-z_]+)\\.([a-zA-Z_]+)").matcher(sql);
            while(columns.find())schema.computeIfAbsent(columns.group(1),k->new LinkedHashSet<>()).add(columns.group(2));
        }
        for(Map.Entry<String,Set<String>> table:schema.entrySet()){
            List<String> columns=new ArrayList<>();for(String col:table.getValue())columns.add(col+" varchar(255)");
            try(Statement s=db.createStatement()){s.execute("create table "+table.getKey()+" ("+String.join(",",columns)+")");}
            String marks=String.join(",",Collections.nCopies(columns.size(),"?"));
            try(PreparedStatement s=db.prepareStatement("insert into "+table.getKey()+" values("+marks+")")){
                int i=0;for(String col:table.getValue()){
                    String value="fixture";
                    if(col.equals("tgl_registrasi")||col.equals("tgl_hasil")||col.equals("tgl_periksa"))value="2026-10-15";
                    if(col.equals("jam")||col.equals("jam_hasil"))value="10:00:00";
                    if(col.equals("kd_dokter")||col.equals("dokter_perujuk")||col.equals("nik"))value="staff";
                    s.setString(++i,value);
                }s.executeUpdate();
            }
        }
    }
    @After public void close() throws Exception {if(db!=null)db.close();}
    private int rows(String module)throws Exception{
        try(PreparedStatement s=db.prepareStatement(queries.get(module))){
            s.setString(1,"2026-10-01");s.setString(2,"2026-11-06");
            try(ResultSet r=s.executeQuery()){int n=0;while(r.next())n++;return n;}
        }
    }
    private void execute(String sql)throws Exception{try(Statement s=db.createStatement()){s.execute(sql);}}
    private void allRows(int n)throws Exception{for(String module:queries.keySet())assertEquals(module,n,rows(module));}
    @Test public void completeChainIsVisibleWithoutAnyNotaTables()throws Exception{allRows(1);}
    @Test public void missingServiceRequestPreventsSpecimenAndReport()throws Exception{
        execute("delete from satu_sehat_servicerequest_lab");assertEquals(0,rows("Specimen"));assertEquals(0,rows("DiagnosticReport"));
    }
    @Test public void blankServiceRequestIsNotSelectable()throws Exception{
        execute("update satu_sehat_servicerequest_lab set id_servicerequest=' '");assertEquals(0,rows("Specimen"));assertEquals(0,rows("DiagnosticReport"));
    }
    @Test public void missingSpecimenPreventsObservationAndReport()throws Exception{
        execute("delete from satu_sehat_specimen_lab");assertEquals(0,rows("Observation"));assertEquals(0,rows("DiagnosticReport"));
    }
    @Test public void blankSpecimenPreventsObservationAndReport()throws Exception{
        execute("update satu_sehat_specimen_lab set id_specimen=''");assertEquals(0,rows("Observation"));assertEquals(0,rows("DiagnosticReport"));
    }
    @Test public void missingObservationPreventsReport()throws Exception{
        execute("delete from satu_sehat_observation_lab");assertEquals(0,rows("DiagnosticReport"));assertEquals(1,rows("Observation"));
    }
    @Test public void blankEncounterPreventsObservationAndReport()throws Exception{
        execute("update satu_sehat_encounter set id_encounter=''");assertEquals(0,rows("Observation"));assertEquals(0,rows("DiagnosticReport"));
    }
    @Test public void registrationOutsideRangeIsExcluded()throws Exception{
        execute("update reg_periksa set tgl_registrasi='2026-09-30'");allRows(0);
    }
    @Test public void unrelatedTemplateResultIsNeverPaired()throws Exception{
        execute("update detail_periksa_lab set id_template='other-template'");assertEquals(0,rows("Observation"));
    }
    @Test public void unrelatedExaminationResultIsNeverPaired()throws Exception{
        execute("update detail_periksa_lab set kd_jenis_prw='other-test'");assertEquals(0,rows("Observation"));
    }
    @Test public void examinationHeaderMustMatchOrderItem()throws Exception{
        execute("update periksa_lab set kd_jenis_prw='other-test'");assertEquals(0,rows("Observation"));assertEquals(0,rows("DiagnosticReport"));
    }
    @Test public void mismatchedResultTimeIsExcluded()throws Exception{
        execute("update periksa_lab set jam='11:00:00'");assertEquals(0,rows("Observation"));assertEquals(0,rows("DiagnosticReport"));
    }
    @Test public void absentKesanPreventsReport()throws Exception{
        execute("delete from saran_kesan_lab");assertEquals(0,rows("DiagnosticReport"));
    }
    @Test public void missingEmployeeDoesNotHideClinicalResults()throws Exception{
        execute("delete from pegawai");assertEquals(1,rows("Observation"));assertEquals(1,rows("DiagnosticReport"));
    }
}
