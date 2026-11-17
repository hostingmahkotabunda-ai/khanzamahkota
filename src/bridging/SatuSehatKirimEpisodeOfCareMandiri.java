package bridging;

import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;
import java.util.ArrayList;
import java.util.List;

/** Halaman mandiri pengiriman EpisodeOfCare; daftar/aksi dikembangkan dari data ANC/PTM lokal. */
public final class SatuSehatKirimEpisodeOfCareMandiri extends JDialog {
    private final JDateChooser from=SatuSehatKirimNutritionOrder.calendar(LocalDate.now(SatuSehatEpisodePayload.HOSPITAL_ZONE).withDayOfMonth(1));
    private final JDateChooser to=SatuSehatKirimNutritionOrder.calendar(LocalDate.now(SatuSehatEpisodePayload.HOSPITAL_ZONE));
    private final JTextField search=new JTextField(18);
    private final JButton reload=SatuSehatAturNutritionOrder.actionButton("Cari / muat ulang",true);
    private final JButton preview=SatuSehatAturNutritionOrder.actionButton("Pratinjau offline",false);
    private final JButton send=SatuSehatAturNutritionOrder.actionButton("Kirim / Hubungkan ANC",true);
    private final JButton close=SatuSehatAturNutritionOrder.actionButton("Tutup",false);
    private final JLabel info=new JLabel(" ");
    private final DefaultTableModel model=new DefaultTableModel(new String[]{"Jenis","No. Rawat","RM","Pasien","Tanggal kunjungan","HPHT / mulai","Kategori ANC","Status klinis","Kelengkapan","ID Encounter","ID EpisodeOfCare"},0){
        @Override public boolean isCellEditable(int r,int c){return false;}
    };
    private final JTable table=new JTable(model);
    private List<SatuSehatEpisodeList.Row> rows=new ArrayList<SatuSehatEpisodeList.Row>();
    private boolean loading;
    private boolean sending;
    /** Match the NutritionOrder menu lifecycle; repeated clicks reuse the live window. */
    public static SatuSehatKirimEpisodeOfCareMandiri open(Window owner,JDialog home,
            SatuSehatKirimEpisodeOfCareMandiri current,Runnable closeOthers,java.awt.Component relativeTo){
        if(!javax.swing.SwingUtilities.isEventDispatchThread()){
            throw new IllegalStateException("Dialog harus dibuka pada Swing event thread.");
        }
        home.dispose();
        if(current==null||!current.isDisplayable()){
            closeOthers.run();
            current=new SatuSehatKirimEpisodeOfCareMandiri(owner);
            current.setLocationRelativeTo(relativeTo);
        }
        current.setVisible(true);
        current.toFront();
        current.requestFocus();
        return current;
    }
    public SatuSehatKirimEpisodeOfCareMandiri(Window owner){
        super(owner,"Kirim EpisodeOfCare SATUSEHAT",ModalityType.MODELESS);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1320,screen.width-60),Math.min(700,screen.height-90));setLocationRelativeTo(owner);
        JPanel panel=new JPanel(new BorderLayout(0,12));panel.setBackground(new Color(243,247,250));
        panel.setBorder(BorderFactory.createEmptyBorder(16,18,16,18));setContentPane(panel);
        JPanel top=new JPanel(new BorderLayout(0,12));top.setOpaque(false);
        JPanel banner=new SatuSehatAturNutritionOrder.Surface(new GridLayout(2,1,0,5),new Color(24,44,62));
        banner.setBorder(BorderFactory.createEmptyBorder(14,18,14,18));
        JLabel title=new JLabel("Episode Perawatan SATUSEHAT");title.setFont(new Font("Segoe UI",Font.BOLD,22));title.setForeground(Color.WHITE);banner.add(title);
        JLabel subtitle=new JLabel("Catatan ANC dan PTM dari petugas poli");subtitle.setForeground(new Color(194,218,228));banner.add(subtitle);top.add(banner,BorderLayout.NORTH);
        JPanel filters=new SatuSehatAturNutritionOrder.Surface(new FlowLayout(FlowLayout.LEFT,12,10),Color.WHITE);
        filters.add(field("Tanggal kunjungan dari",from));filters.add(field("Sampai",to));filters.add(field("No. Rawat / RM / Nama",search));filters.add(reload);top.add(filters,BorderLayout.SOUTH);panel.add(top,BorderLayout.NORTH);
        table.setRowHeight(34);table.setFont(new Font("Segoe UI",Font.PLAIN,13));table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);table.setFillsViewportHeight(true);table.setBackground(Color.WHITE);table.setSelectionBackground(new Color(214,239,235));table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        int[] widths={70,160,80,210,140,140,100,110,320,250,250};for(int i=0;i<widths.length;i++)table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        panel.add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout(0,8));bottom.setOpaque(false);JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));buttons.setOpaque(false);
        close.setName("closeEpisode");close.addActionListener(e->dispose());buttons.add(preview);buttons.add(send);buttons.add(close);bottom.add(buttons,BorderLayout.NORTH);bottom.add(info,BorderLayout.SOUTH);panel.add(bottom,BorderLayout.SOUTH);
        reload.addActionListener(e->load());search.addActionListener(e->load());preview.addActionListener(e->preview());
        send.setEnabled(false);send.addActionListener(e->send());
        table.getSelectionModel().addListSelectionListener(e->updateSendButton());
    }
    private static JPanel field(String label,JComponent control){
        JPanel p=new JPanel(new BorderLayout(0,5));p.setOpaque(false);p.add(new JLabel(label),BorderLayout.NORTH);control.setPreferredSize(new Dimension(180,36));p.add(control,BorderLayout.CENTER);return p;
    }
    public void load(){
        if(loading||sending)return;
        final String a,b,q=search.getText().trim();
        try{a=SatuSehatKirimNutritionOrder.dateValue(from);b=SatuSehatKirimNutritionOrder.dateValue(to);}
        catch(Exception e){info.setText("Pilih rentang tanggal kunjungan.");return;}
        loading=true;reload.setEnabled(false);preview.setEnabled(false);send.setEnabled(false);info.setText("Memuat catatan lokal...");
        new SwingWorker<SatuSehatEpisodeList.Result,Void>(){
            @Override protected SatuSehatEpisodeList.Result doInBackground()throws Exception{return new SatuSehatEpisodeList().load(a,b,q);}
            @Override protected void done(){
                loading=false;reload.setEnabled(true);preview.setEnabled(true);
                if(!isDisplayable())return;
                try{SatuSehatEpisodeList.Result result=get();rows=result.rows;model.setRowCount(0);
                    for(SatuSehatEpisodeList.Row r:rows)model.addRow(new Object[]{r.kind,r.visit,r.rm,r.name,r.date,r.start,r.category,r.status,r.readiness(),r.encounter,r.remote});
                    info.setText(rows.size()+" catatan. "+String.join(" ",result.notices));
                }catch(Exception e){rows.clear();model.setRowCount(0);info.setText("Catatan belum dapat dimuat. Periksa koneksi/struktur tabel; data tidak diubah.");}
                updateSendButton();
            }
        }.execute();
    }
    private void updateSendButton(){
        int selected=table.getSelectedRow();
        send.setEnabled(!loading&&!sending&&fungsi.akses.getsatu_sehat_kirim_encounter()&&selected>=0&&selected<rows.size()&&"ANC".equals(rows.get(table.convertRowIndexToModel(selected)).kind));
    }
    private void send(){
        if(sending||loading||!fungsi.akses.getsatu_sehat_kirim_encounter())return;
        int index=table.getSelectedRow();if(index<0)return;
        final SatuSehatEpisodeList.Row row=rows.get(table.convertRowIndexToModel(index));
        if(!"ANC".equals(row.kind))return;
        if(JOptionPane.showConfirmDialog(this,"Kirim/hubungkan ANC untuk RM "+row.rm+"\nNo. Rawat: "+row.visit+"\nHPHT: "+row.start+" | Kategori: "+row.category+"\nEpisode yang sudah ada akan digunakan kembali.\nEncounter akan diperbarui dengan tautan episode dan kategori ANC.","Konfirmasi pengiriman SATUSEHAT",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        sending=true;reload.setEnabled(false);send.setEnabled(false);preview.setEnabled(false);close.setEnabled(false);table.setEnabled(false);setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        info.setText("Memeriksa referensi, mengirim episode, dan menghubungkan Encounter...");
        new SwingWorker<String,Void>(){
            @Override protected String doInBackground()throws Exception{return new SatuSehatAncSender().send(row);}
            @Override protected void done(){
                sending=false;reload.setEnabled(true);preview.setEnabled(true);close.setEnabled(true);table.setEnabled(true);setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                String message;boolean ok;
                try{message=get();ok=true;}catch(Exception e){message=SatuSehatAncSender.error(e);ok=false;}
                if(isDisplayable()){
                    info.setText(message);JTextArea text=new JTextArea(message,5,65);text.setLineWrap(true);text.setWrapStyleWord(true);text.setEditable(false);
                    JOptionPane.showMessageDialog(SatuSehatKirimEpisodeOfCareMandiri.this,new JScrollPane(text),"EpisodeOfCare",ok?JOptionPane.INFORMATION_MESSAGE:JOptionPane.WARNING_MESSAGE);
                    load();
                }
            }
        }.execute();
    }
    private void preview(){
        int index=table.getSelectedRow();if(index<0){info.setText("Pilih satu catatan terlebih dahulu.");return;}
        SatuSehatEpisodeList.Row row=rows.get(table.convertRowIndexToModel(index));
        try{
            com.fasterxml.jackson.databind.ObjectMapper mapper=new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.node.ObjectNode payload=SatuSehatEpisodePayload.post(mapper,"contoh-organisasi","contoh-pasien","contoh-episode",row.kind,row.kind.equals("ANC")?"Antenatal Care":row.kind,row.status,row.start,"",row.kind.equals("ANC")?"":"contoh-condition",java.time.Clock.systemUTC());
            JTextArea area=new JTextArea(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload),24,75);area.setEditable(false);area.setCaretPosition(0);
            JOptionPane.showMessageDialog(this,new JScrollPane(area),"Pratinjau offline - ID contoh, tidak dikirim",JOptionPane.INFORMATION_MESSAGE);
        }catch(Exception e){info.setText("Tanggal atau status episode perlu diperiksa sebelum pratinjau.");}
    }
}
