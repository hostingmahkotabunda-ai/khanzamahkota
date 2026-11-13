package bridging;

import com.toedter.calendar.JDateChooser;
import java.awt.FlowLayout;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import javax.swing.*;

/** Calendar and time choices in WITA. Unset clinical times stay unset; never default to now. */
public final class SatuSehatNutritionOrderDateTime extends JPanel {
    private final JDateChooser date=new JDateChooser();
    private final JComboBox<String> hour=choices(24),minute=choices(60);
    private final JLabel seconds=new JLabel("00");
    private boolean loading;
    private String original="";
    private LocalDateTime originalLocal;
    private static JComboBox<String> choices(int count){JComboBox<String> c=new JComboBox<String>();c.addItem("");for(int i=0;i<count;i++)c.addItem(String.format(java.util.Locale.ROOT,"%02d",i));return c;}
    public SatuSehatNutritionOrderDateTime(String sourceDate){
        super(new FlowLayout(FlowLayout.LEFT,5,0));setOpaque(false);
        date.setLocale(new java.util.Locale("id","ID"));date.setDateFormatString("dd MMM yyyy");date.setPreferredSize(new java.awt.Dimension(175,32));
        if(date.getDateEditor().getUiComponent() instanceof JTextField)((JTextField)date.getDateEditor().getUiComponent()).setEditable(false);
        try{setDate(LocalDate.parse(sourceDate.substring(0,10)));}catch(Exception ignored){date.setDate(null);}
        add(date);add(new JLabel("Jam"));add(hour);add(new JLabel(":"));add(minute);add(new JLabel(":"));add(seconds);JLabel zone=new JLabel(" WITA");zone.setForeground(new java.awt.Color(13,128,122));add(zone);
        JButton clear=new JButton("Reset");clear.setBorderPainted(false);clear.setContentAreaFilled(false);clear.setForeground(new java.awt.Color(100,116,139));clear.addActionListener(e->setValue(""));add(clear);
        for(JComboBox<String> choice:new JComboBox[]{hour,minute}){choice.setFont(new java.awt.Font("Segoe UI",java.awt.Font.PLAIN,13));choice.setBackground(java.awt.Color.WHITE);choice.setPreferredSize(new java.awt.Dimension(62,32));choice.addActionListener(e->updateSeconds());}
        seconds.setFont(new java.awt.Font("Segoe UI",java.awt.Font.BOLD,13));seconds.setForeground(new java.awt.Color(100,116,139));seconds.setToolTipText("Detik otomatis 00 untuk waktu baru. Waktu lama dipertahankan jika tidak diubah.");
        date.setFont(new java.awt.Font("Segoe UI",java.awt.Font.PLAIN,13));date.addPropertyChangeListener("date",e->updateSeconds());
        date.setToolTipText("Pilih tanggal dari kalender sesuai dokumentasi. Tanggal ADIME hanya petunjuk awal kalender.");
        hour.setToolTipText("Jam (00-23)");minute.setToolTipText("Menit (00-59)");
    }
    private void setDate(LocalDate value){Calendar c=Calendar.getInstance();c.clear();c.set(value.getYear(),value.getMonthValue()-1,value.getDayOfMonth(),12,0,0);date.setCalendar(c);}
    private LocalDateTime selection(){Calendar c=date.getCalendar();if(c==null||hour.getSelectedIndex()<1||minute.getSelectedIndex()<1)return null;return LocalDateTime.of(c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH),hour.getSelectedIndex()-1,minute.getSelectedIndex()-1,0);}
    private void updateSeconds(){if(loading)return;LocalDateTime selected=selection();seconds.setText(originalLocal!=null&&selected!=null&&selected.equals(originalLocal.withSecond(0).withNano(0))?String.format(java.util.Locale.ROOT,"%02d",originalLocal.getSecond()):"00");}
    public void setValue(String value){
        loading=true;original=value==null?"":value.trim();originalLocal=null;
        try{
            if(original.isEmpty()||"null".equals(original)){original="";hour.setSelectedItem("");minute.setSelectedItem("");return;}
            try{originalLocal=SatuSehatNutritionOrderPayload.time(original).atZoneSameInstant(SatuSehatNutritionOrderPayload.ZONE).toLocalDateTime();setDate(originalLocal.toLocalDate());hour.setSelectedIndex(originalLocal.getHour()+1);minute.setSelectedIndex(originalLocal.getMinute()+1);}
            catch(IllegalArgumentException e){hour.setSelectedItem("");minute.setSelectedItem("");}
        }finally{loading=false;updateSeconds();}
    }
    public String value(){
        if(hour.getSelectedIndex()<1&&minute.getSelectedIndex()<1){if(!original.isEmpty())throw new IllegalArgumentException("Pilih ulang tanggal dan jam yang belum valid.");return "";}
        LocalDateTime local=selection();if(local==null)throw new IllegalArgumentException("Lengkapi tanggal, jam, dan menit.");
        // Preserve a stored UTC/offset/fractional value when the operator has not changed its displayed time.
        if(originalLocal!=null&&local.equals(originalLocal.withSecond(0).withNano(0)))return original;
        return local.format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss"));
    }
}
