import bridging.SatuSehatMedicationStatementService;
import java.lang.reflect.Proxy;
import java.sql.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class SatuSehatMedicationStatementStorageTest {
    private Connection database(int count) {
        ResultSet rs=(ResultSet)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{ResultSet.class},(proxy,method,args)->{
            if(method.getName().equals("next"))return true;
            if(method.getName().equals("getInt"))return count;
            if(method.getName().equals("close"))return null;
            throw new AssertionError(method.getName());
        });
        PreparedStatement statement=(PreparedStatement)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{PreparedStatement.class},(proxy,method,args)->{
            if(method.getName().equals("executeQuery"))return rs;
            if(method.getName().equals("close"))return null;
            throw new AssertionError("Tidak boleh menulis database saat memilih tabel: "+method.getName());
        });
        return (Connection)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Connection.class},(proxy,method,args)->{
            if(method.getName().equals("prepareStatement")){
                assertTrue(args[0].toString().contains("column_name='source_key'"));
                return statement;
            }
            throw new AssertionError(method.getName());
        });
    }
    @Test public void existingPrescriptionSchemaUsesSeparateTable()throws Exception{
        assertEquals("satu_sehat_medicationstatement_rekonsiliasi",SatuSehatMedicationStatementService.resolveStorageTable(database(0)));
    }
    @Test public void existingReconciliationSchemaRetainsSavedIds()throws Exception{
        assertEquals("satu_sehat_medicationstatement",SatuSehatMedicationStatementService.resolveStorageTable(database(1)));
    }
}
