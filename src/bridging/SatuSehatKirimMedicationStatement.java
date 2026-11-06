package bridging;

import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.akses;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.springframework.web.client.HttpStatusCodeException;

/** Pengiriman manual riwayat obat yang telah ditinjau dari Rekonsiliasi Obat. */
public final class SatuSehatKirimMedicationStatement extends JDialog {
    private final JTextField start=new JTextField(LocalDate.now(ZoneId.of("Asia/Makassar")).withDayOfMonth(1).toString(),10);
    private final JTextField end=new JTextField(LocalDate.now(ZoneId.of("Asia/Makassar")).toString(),10);
    private final JTextField query=new JTextField(16);
    private final JButton search=new JButton("Cari"), sourceInfo=new JButton("Sumber Informasi"), kfa=new JButton("Pilih KFA"), save=new JButton("Simpan Mapping"), preview=new JButton("Pratinjau JSON"),
            send=new JButton("Kirim Baru"), update=new JButton("Update"), close=new JButton("Keluar");
    private final JLabel info=new JLabel("Pilih satu catatan, lengkapi status dan mapping obat sesuai riwayat yang tercatat.");
    private final DefaultTableModel model=new DefaultTableModel(new String[]{"No. Rekonsiliasi","No. Rawat","Pasien","Waktu Wawancara",
            "Obat Riwayat","Dosis / Aturan","Catatan Sumber","Status Penggunaan","Sumber Obat","Kode KFA / Medication/ID",
            "Nama sesuai KFA","ID Encounter","ID MedicationStatement","Sumber Informasi"},0){
        @Override public boolean isCellEditable(int row,int col){return (col>=7&&col<=10)||col==13;}
    };
    private final JTable table=new JTable(model);
    private List<SatuSehatMedicationStatementService.Row> rows=new ArrayList<SatuSehatMedicationStatementService.Row>();
    private SatuSehatMedicationStatementService service;

    public static boolean allowed(){return akses.getrekonsiliasi_obat() && akses.getsatu_sehat_kirim_medication();}

    public SatuSehatKirimMedicationStatement(Frame owner,boolean modal){
        super(owner,modal);
        setTitle("::[ Kirim MedicationStatement - Riwayat Obat SATUSEHAT ]::");
        setSize(1200,650);setLocationRelativeTo(owner);setLayout(new BorderLayout(6,6));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Tanggal wawancara (yyyy-MM-dd)"));top.add(start);top.add(new JLabel("s.d."));top.add(end);
        top.add(new JLabel("Cari"));top.add(query);top.add(search);add(top,BorderLayout.NORTH);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        for(int i=0;i<model.getColumnCount();i++)table.getColumnModel().getColumn(i).setPreferredWidth(i==6?400:i==5?260:180);
        table.getColumnModel().getColumn(7).setCellEditor(new DefaultCellEditor(new JComboBox<String>(
                new String[]{"","active","completed","stopped","on-hold","not-taken","unknown"})));
        table.getColumnModel().getColumn(8).setCellEditor(new DefaultCellEditor(new JComboBox<String>(
                new String[]{"","KFA (obat luar faskes)","Medication (faskes ini)"})));
        add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout());JPanel buttons=new JPanel(new FlowLayout(FlowLayout.LEFT));
        for(JButton button:new JButton[]{sourceInfo,kfa,save,preview,send,update,close})buttons.add(button);
        bottom.add(buttons,BorderLayout.NORTH);bottom.add(info,BorderLayout.SOUTH);add(bottom,BorderLayout.SOUTH);
        search.addActionListener(e->load());query.addActionListener(e->load());
        save.addActionListener(e->act("save"));preview.addActionListener(e->act("preview"));
        kfa.addActionListener(e->chooseKfa());
        sourceInfo.addActionListener(e->chooseInformationSource());
        send.addActionListener(e->act("send"));update.addActionListener(e->act("update"));close.addActionListener(e->dispose());
        isCek();if(allowed())load();
    }

    public void isCek(){
        for(JButton b:new JButton[]{search,sourceInfo,kfa,save,preview,send,update})b.setEnabled(allowed());
        table.setEnabled(allowed());
    }

    @Override public void setVisible(boolean visible){
        if(visible){
            java.awt.GraphicsConfiguration config=getGraphicsConfiguration();
            java.awt.Rectangle area=new java.awt.Rectangle(config.getBounds());
            java.awt.Insets insets=java.awt.Toolkit.getDefaultToolkit().getScreenInsets(config);
            area.x+=insets.left;area.y+=insets.top;
            area.width-=insets.left+insets.right;area.height-=insets.top+insets.bottom;
            setSize(Math.min(getWidth(),area.width-20),Math.min(getHeight(),area.height-20));
            setLocation(Math.max(area.x,Math.min(getX(),area.x+area.width-getWidth())),
                    Math.max(area.y,Math.min(getY(),area.y+area.height-getHeight())));
        }
        super.setVisible(visible);
    }

    private void busy(boolean value){
        for(JButton b:new JButton[]{search,sourceInfo,kfa,save,preview,send,update})b.setEnabled(!value&&allowed());
        start.setEnabled(!value);end.setEnabled(!value);query.setEnabled(!value);table.setEnabled(!value&&allowed());
        close.setEnabled(!value);setDefaultCloseOperation(value?DO_NOTHING_ON_CLOSE:DISPOSE_ON_CLOSE);
    }

    private <T> void work(Callable<T> task,java.util.function.Consumer<T> done){
        if(!allowed()){JOptionPane.showMessageDialog(this,"Memerlukan hak akses Rekonsiliasi Obat dan Kirim Medication SATUSEHAT.");return;}
        busy(true);info.setText("Memproses...");
        new SwingWorker<T,Void>(){
            @Override protected T doInBackground()throws Exception{
                if(service==null){
                    SatuSehatMedicationStatementService candidate=new SatuSehatMedicationStatementService();
                    candidate.ensureTable();service=candidate;
                }
                return task.call();
            }
            @Override protected void done(){
                busy(false);
                try{done.accept(get());}catch(Exception ex){
                    Throwable cause=ex;while(cause.getCause()!=null)cause=cause.getCause();
                    String message=cause.getMessage();
                    if(cause instanceof HttpStatusCodeException)message+="\n"+((HttpStatusCodeException)cause).getResponseBodyAsString();
                    info.setText("Gagal. Periksa detail pesan dan data sumber.");
                    JTextArea details=new JTextArea(message,12,80);details.setEditable(false);details.setLineWrap(true);details.setWrapStyleWord(true);
                    JOptionPane.showMessageDialog(SatuSehatKirimMedicationStatement.this,new JScrollPane(details),"MedicationStatement",JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void load(){
        if(table.isEditing())table.getCellEditor().stopCellEditing();
        String from=start.getText().trim(),to=end.getText().trim(),filter=query.getText().trim();
        work(()->service.load(from,to,filter),result->{
            rows=result;model.setRowCount(0);
            for(SatuSehatMedicationStatementService.Row r:rows)model.addRow(new Object[]{r.reconciliation,r.encounterNumber,r.patientName,
                    r.asserted,r.medicineName,r.dosage,r.note,r.status,r.mode,r.medicine,r.display,r.encounter,r.remoteId,r.informationSource});
            info.setText(rows.isEmpty()
                    ? "Tidak ada rekonsiliasi tersimpan beserta detail obat yang cocok. Periksa tanggal wawancara dan kata pencarian."
                    : rows.size()+" catatan. Lengkapi status dan mapping; pilih Pratinjau JSON sebelum mengirim.");
        });
    }

    private void act(String action){
        if(table.isEditing()&&!table.getCellEditor().stopCellEditing())return;
        int selected=table.getSelectedRow();
        if(selected<0){JOptionPane.showMessageDialog(this,"Pilih satu catatan riwayat obat.");return;}
        SatuSehatMedicationStatementService.Row r=rows.get(selected);
        r.status=String.valueOf(model.getValueAt(selected,7)).trim();r.mode=String.valueOf(model.getValueAt(selected,8)).trim();
        r.medicine=String.valueOf(model.getValueAt(selected,9)).trim();r.display=String.valueOf(model.getValueAt(selected,10)).trim();
        r.informationSource=String.valueOf(model.getValueAt(selected,13)).trim();
        work(()->{
            if("preview".equals(action))return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(service.prepare(r));
            if("save".equals(action)){service.saveReview(r);return "Mapping tersimpan. Belum dikirim ke SATUSEHAT.";}
            return service.send(r,"update".equals(action));
        },result->{
            model.setValueAt(r.remoteId,selected,12);
            if("preview".equals(action)){
                JTextArea text=new JTextArea(result,28,90);text.setEditable(false);
                JOptionPane.showMessageDialog(this,new JScrollPane(text),"Pratinjau MedicationStatement",JOptionPane.INFORMATION_MESSAGE);
                info.setText("Pratinjau selesai. Belum dikirim ke SATUSEHAT.");
            }else info.setText(result);
        });
    }

    private void chooseInformationSource(){
        if(!allowed())return;
        if(table.isEditing()&&!table.getCellEditor().stopCellEditing())return;
        int selected=table.getSelectedRow();
        if(selected<0){JOptionPane.showMessageDialog(this,"Pilih satu catatan riwayat obat.");return;}
        JComboBox<String> source=new JComboBox<String>(new String[]{"",SatuSehatMedicationStatementPayload.PATIENT_SOURCE});
        source.setEditable(true);source.setSelectedItem(model.getValueAt(selected,13));
        Object[] fields={"Siapa yang memberikan informasi riwayat obat ini?",source,
                "Pilih Pasien sendiri hanya jika pasien memberikan informasi.",
                "Untuk pihak lain, masukkan referensi SATUSEHAT, misalnya Practitioner/ID atau RelatedPerson/ID."};
        if(JOptionPane.showConfirmDialog(this,fields,"Sumber Informasi",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        String value=String.valueOf(source.getEditor().getItem()).trim();
        try{
            SatuSehatMedicationStatementPayload.validateInformationSource(value);
            model.setValueAt(value,selected,13);info.setText("Sumber informasi dipilih: "+value+". Klik Simpan Mapping untuk menyimpan.");
        }catch(IllegalArgumentException e){JOptionPane.showMessageDialog(this,e.getMessage());}
    }

    private void chooseKfa(){
        if(!allowed())return;
        if(table.isEditing()&&!table.getCellEditor().stopCellEditing())return;
        int selected=table.getSelectedRow();
        if(selected<0){JOptionPane.showMessageDialog(this,"Pilih satu catatan riwayat obat.");return;}
        SatuSehatReferensiObatKFA dialog=new SatuSehatReferensiObatKFA((Frame)getOwner(),true);
        dialog.setLocationRelativeTo(this);dialog.setVisible(true);
        JTable choices=dialog.getTable();int choice=choices.getSelectedRow();
        if(choice>=0){
            String code=String.valueOf(choices.getValueAt(choice,0));
            if(!code.matches("9[23][0-9]{6}")){JOptionPane.showMessageDialog(this,"Pilih KFA produk obat 92/93.");return;}
            model.setValueAt("KFA (obat luar faskes)",selected,8);
            model.setValueAt(code,selected,9);model.setValueAt(choices.getValueAt(choice,1),selected,10);
            info.setText("KFA dipilih. Pastikan sesuai nama, kekuatan, dan sediaan obat pada riwayat.");
        }
    }
}
