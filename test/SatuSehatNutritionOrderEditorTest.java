import bridging.*;
import com.toedter.calendar.JDateChooser;
import java.awt.Component;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Real picker controls with synthetic values on the EDT, without database or network. */
public class SatuSehatNutritionOrderEditorTest {
    private <T> T edt(final Callable<T> work)throws Exception{final AtomicReference<T> out=new AtomicReference<T>();SwingUtilities.invokeAndWait(()->{try{out.set(work.call());}catch(Exception e){throw new RuntimeException(e);}});return out.get();}
    private SatuSehatNutritionOrderDateTime picker(){return new SatuSehatNutritionOrderDateTime("2026-11-06 12:06:55.0");}
    private List<JComboBox> times(SatuSehatNutritionOrderDateTime p){List<JComboBox> out=new ArrayList<JComboBox>();for(Component c:p.getComponents())if(c instanceof JComboBox)out.add((JComboBox)c);return out;}
    private JDateChooser date(SatuSehatNutritionOrderDateTime p){for(Component c:p.getComponents())if(c instanceof JDateChooser)return (JDateChooser)c;throw new AssertionError();}
    private void incomplete(SatuSehatNutritionOrderDateTime p){try{p.value();fail("Must not invent a clinical time");}catch(IllegalArgumentException expected){}}
    @Test public void emptyClinicalTimeNeverDefaultsToNow()throws Exception{edt(()->{SatuSehatNutritionOrderDateTime p=picker();assertEquals("",p.value());assertTrue(date(p).isEnabled());for(Component c:p.getComponents())assertFalse(c instanceof JCheckBox);assertEquals(2,times(p).size());return null;});}
    @Test public void storedLocalTimeRoundTrips()throws Exception{edt(()->{SatuSehatNutritionOrderDateTime p=picker();p.setValue("2026-10-17 10:00:17");assertEquals("2026-10-17 10:00:17",p.value());return null;});}
    @Test public void utcStoredTimeDisplaysInWitaWithoutChangingInstant()throws Exception{edt(()->{SatuSehatNutritionOrderDateTime p=picker();p.setValue("2026-10-17T02:00:17Z");assertEquals("10",times(p).get(0).getSelectedItem());assertEquals("2026-10-17T02:00:17Z",p.value());return null;});}
    @Test public void fractionalSecondsNotLostWhenReopening()throws Exception{edt(()->{SatuSehatNutritionOrderDateTime p=picker();p.setValue("2026-10-17T10:00:17.125+08:00");assertEquals("2026-10-17T10:00:17.125+08:00",p.value());return null;});}
    @Test public void optionalEndCanRemainUnset()throws Exception{edt(()->{SatuSehatNutritionOrderDateTime p=picker();p.setValue("2026-10-17 10:00:00");p.setValue("");assertEquals("",p.value());assertTrue(date(p).isEnabled());return null;});}
    @Test public void pickerUsesSourceDayAndZeroSecondsWithoutCheckbox()throws Exception{edt(()->{SatuSehatNutritionOrderDateTime p=picker();times(p).get(0).setSelectedItem("11");incomplete(p);times(p).get(1).setSelectedItem("30");assertEquals("2026-11-06 11:30:00",p.value());return null;});}
    @Test public void invalidOldDraftCannotSilentlyBecomeCurrentTime()throws Exception{edt(()->{SatuSehatNutritionOrderDateTime p=picker();p.setValue("tanggal belum diketahui");incomplete(p);return null;});}
    @Test public void dateUsesCalendarInsteadOfFreeText()throws Exception{edt(()->{SatuSehatNutritionOrderDateTime p=picker();assertFalse(((JTextField)date(p).getDateEditor().getUiComponent()).isEditable());return null;});}
    @Test public void changingDisplayedMinuteProducesExplicitNewLocalTimeWithZeroSeconds()throws Exception{edt(()->{SatuSehatNutritionOrderDateTime p=picker();p.setValue("2026-10-17T02:00:17Z");times(p).get(1).setSelectedItem("30");assertEquals("2026-10-17 10:30:00",p.value());return null;});}
    private List<SatuSehatNutritionOrderService.Doctor> doctors(){return Arrays.asList(new SatuSehatNutritionOrderService.Doctor("test-A","Dokter A"),new SatuSehatNutritionOrderService.Doctor("test-B","Dokter B"));}
    @Test public void soleDpjpSelectedAutomatically(){assertEquals("test-B",SatuSehatAturNutritionOrder.suggestedDoctor("",Arrays.asList("test-B"),doctors()));}
    @Test public void multipleDpjpRequireSelection(){assertEquals("",SatuSehatAturNutritionOrder.suggestedDoctor("",Arrays.asList("test-A","test-B"),doctors()));}
    @Test public void savedDoctorNeverReplacedByNewDpjp(){assertEquals("test-A",SatuSehatAturNutritionOrder.suggestedDoctor("test-A",Arrays.asList("test-B"),doctors()));assertEquals("saved-old",SatuSehatAturNutritionOrder.suggestedDoctor("saved-old",Arrays.asList("test-B"),doctors()));}
    @Test public void duplicateDpjpEntriesAreNotAmbiguous(){assertEquals("test-B",SatuSehatAturNutritionOrder.suggestedDoctor("",Arrays.asList("test-B","test-B"),doctors()));}
    @Test public void missingEmployeeLinkDoesNotChooseAnotherDoctor(){assertEquals("",SatuSehatAturNutritionOrder.suggestedDoctor("",Arrays.asList("missing"),doctors()));assertEquals("",SatuSehatAturNutritionOrder.suggestedDoctor("",Arrays.asList("missing","test-A"),doctors()));}
    @Test public void dateFilterRoundTripsSourceDay()throws Exception{edt(()->{JDateChooser picker=SatuSehatKirimNutritionOrder.calendar(java.time.LocalDate.of(2026,10,17));assertEquals("2026-10-17",SatuSehatKirimNutritionOrder.dateValue(picker));SatuSehatKirimNutritionOrder.setDateValue(picker,java.time.LocalDate.of(2026,11,1));assertEquals("2026-11-01",SatuSehatKirimNutritionOrder.dateValue(picker));return null;});}
    @Test public void dateFilterCannotBeEnteredAsFreeText()throws Exception{edt(()->{JDateChooser picker=SatuSehatKirimNutritionOrder.calendar(java.time.LocalDate.of(2026,11,13));picker.setEnabled(false);picker.setEnabled(true);assertFalse(((JTextField)picker.getDateEditor().getUiComponent()).isEditable());return null;});}
    @Test public void clearedFilterDateIsRejectedBeforeLoading()throws Exception{edt(()->{JDateChooser picker=SatuSehatKirimNutritionOrder.calendar(java.time.LocalDate.of(2026,11,13));picker.setDate(null);try{SatuSehatKirimNutritionOrder.dateValue(picker);fail("Must request a date");}catch(IllegalArgumentException expected){}return null;});}
}
