package bridging;

import java.awt.*;
import javax.swing.*;
import bridging.SatuSehatAllergyReferenceService.*;

/** Form mapping; pencarian hanya mengisi form, tanpa menyimpan atau mengirim data. */
public final class SatuSehatAllergyMappingEditor extends JPanel {
    private final String keyword;
    private final boolean multiple;
    private final SatuSehatAllergyReferenceService references;
    private final JTextField query=new JTextField(),code=new JTextField(),display=new JTextField(),text=new JTextField();
    private final JComboBox<String> category=new JComboBox<>(new String[]{"","food","medication","environment","biologic"});
    private final JComboBox<String> system=new JComboBox<>(new String[]{SatuSehatAllergyMapping.SNOMED,SatuSehatAllergyMapping.KFA});
    private final JButton findCode=new JButton("Cari Online"),findDisplay=new JButton("Cari Online");
    private final JTextArea status=new JTextArea(3,45);
    private javax.swing.SwingWorker<Lookup,Void> worker;
    private boolean searching;
    private Candidate chosen;
    private String chosenCategory;

    public SatuSehatAllergyMappingEditor(String original,SatuSehatAllergyMapping.Mapping mapping,SatuSehatAllergyReferenceService references){
        this.references=references;
        this.multiple=SatuSehatAllergySearchTerm.multiple(original);
        this.keyword=multiple?SatuSehatAllergyMapping.normalize(original):mapping.keyword;
        setLayout(new GridBagLayout());setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        JTextArea source=new JTextArea(original,2,42);source.setEditable(false);source.setLineWrap(true);source.setWrapStyleWord(true);
        try{query.setText(SatuSehatAllergySearchTerm.extract(original));}catch(IllegalArgumentException ignored){query.setText("");}
        category.setSelectedItem(mapping.category);system.setSelectedItem(mapping.system);
        code.setText(mapping.code);display.setText(mapping.display);text.setText(mapping.text);
        addField(0,"Catatan/keyword",new JScrollPane(source),null);
        addField(1,"Nama zat untuk dicari",query,null);
        addField(2,"Kategori",category,null);
        addField(3,"Sistem terminologi",system,null);
        addField(4,"Kode zat penyebab",code,findCode);
        addField(5,"Display resmi",display,findDisplay);
        addField(6,"Keterangan",text,null);
        status.setEditable(false);status.setLineWrap(true);status.setWrapStyleWord(true);status.setOpaque(false);
        GridBagConstraints info=new GridBagConstraints();info.gridx=0;info.gridy=7;info.gridwidth=3;info.weightx=1;info.weighty=1;info.anchor=GridBagConstraints.NORTHWEST;info.fill=GridBagConstraints.HORIZONTAL;info.insets=new Insets(10,0,0,0);add(status,info);
        status.setText(multiple?"Catatan memuat beberapa zat. Pilih satu zat lalu tekan OK untuk menyimpan mapping sebagian. Kode yang dikirim hanya mewakili zat pilihan; catatan asli tetap utuh dan zat lain belum terpetakan.":
                "Isi satu nama zat lalu klik Cari Online di samping kode atau display. Pilihan referensi mengisi kategori, sistem, kode, dan display sekaligus. Perubahan baru disimpan setelah menekan OK.");
        if(multiple)status.setForeground(new Color(160,50,30));
        findCode.addActionListener(e -> search());findDisplay.addActionListener(e -> search());query.addActionListener(e -> search());
    }
    private void addField(int row,String title,JComponent field,JButton button){
        GridBagConstraints c=new GridBagConstraints();c.gridy=row;c.gridx=0;c.anchor=GridBagConstraints.WEST;c.insets=new Insets(5,0,5,10);
        add(new JLabel(title),c);c.gridx=1;c.weightx=1;c.fill=GridBagConstraints.HORIZONTAL;c.gridwidth=button==null?2:1;c.insets=new Insets(5,0,5,0);add(field,c);
        if(button!=null){c.gridx=2;c.gridwidth=1;c.weightx=0;c.insets=new Insets(5,8,5,0);add(button,c);}
    }
    public boolean isSearching(){return searching;}
    public boolean isMultiple(){return multiple;}
    private void busy(boolean value){
        boolean old=searching;searching=value;
        findCode.setEnabled(!value);findDisplay.setEnabled(!value);query.setEnabled(!value);
        category.setEnabled(!value);system.setEnabled(!value);code.setEnabled(!value);display.setEnabled(!value);text.setEnabled(!value);
        firePropertyChange("searching",old,value);
    }
    private void search(){
        if(searching)return;
        final String term=query.getText();
        try{SatuSehatAllergySearchTerm.extract(term);}catch(IllegalArgumentException e){status.setText(e.getMessage()+" Isi satu nama zat pada kolom pencarian.");return;}
        busy(true);status.setText("Sedang mencari referensi daring...");
        worker=new SwingWorker<Lookup,Void>(){
            @Override protected Lookup doInBackground()throws Exception{return references.search(term);}
            @Override protected void done(){
                busy(false);
                if(isCancelled()||!isDisplayable())return;
                try{
                    Lookup lookup=get();
                    if(lookup.result.candidates.isEmpty()){status.setText("Referensi belum ditemukan. "+lookup.result.warning);return;}
                    choose(lookup);
                }catch(Exception e){status.setText("Pencarian belum berhasil. Periksa koneksi/nama zat dan coba lagi; isian sebelumnya dipertahankan.");}
            }
        };
        worker.execute();
    }
    private void choose(Lookup lookup){
        JList<Candidate> list=new JList<>(lookup.result.candidates.toArray(new Candidate[0]));list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JComboBox<String> selectedCategory=new JComboBox<>(new String[]{"","food","medication","environment","biologic"});
        list.addListSelectionListener(e -> {Candidate c=list.getSelectedValue();if(c!=null){selectedCategory.setSelectedItem(c.category);selectedCategory.setEnabled(c.category.isEmpty());}});
        list.setSelectedIndex(0);
        JScrollPane scroll=new JScrollPane(list);scroll.setPreferredSize(new Dimension(650,240));
        Object[] options={"Hasil untuk: "+lookup.query,lookup.result.warning,scroll,"Kategori (tinjau bila belum diketahui)",selectedCategory,
            "Pilih zat yang sesuai catatan klinis. Pilihan mengisi form dan tidak langsung mengirim data."};
        if(JOptionPane.showConfirmDialog(this,options,"Pilih Referensi Alergi",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION&&list.getSelectedValue()!=null)
            applyReference(list.getSelectedValue(),(String)selectedCategory.getSelectedItem());
        else status.setText("Pemilihan referensi dibatalkan. Isian sebelumnya dipertahankan.");
    }
    public void applyReference(Candidate candidate,String selectedCategory){
        chosen=candidate;chosenCategory=selectedCategory;
        category.setSelectedItem(selectedCategory);system.setSelectedItem(candidate.system);code.setText(candidate.code);display.setText(candidate.display);
        text.setText("Alergi "+candidate.display);
        status.setText(multiple?"Referensi "+candidate.display+" dipilih. Tekan OK untuk menyimpan mapping sebagian. Hanya kode zat ini yang dipakai; catatan asli tetap utuh dan zat lain belum terpetakan.":
                "Referensi "+candidate.display+" dipilih. Periksa isian, lalu tekan OK untuk menyimpan mapping. Catatan sumber dan status verifikasi pasien tetap dipertahankan.");
    }
    public SatuSehatAllergyMapping.Mapping mapping(){
        if(searching)throw new IllegalArgumentException("Tunggu pencarian referensi selesai.");
        SatuSehatAllergyMapping.Mapping m=new SatuSehatAllergyMapping.Mapping(keyword,(String)category.getSelectedItem(),(String)system.getSelectedItem(),code.getText(),display.getText(),multiple?"Alergi "+display.getText().trim():text.getText(),multiple);
        m.validate();return m;
    }
    public String referenceSource(){
        if(chosen!=null&&chosen.system.equals(system.getSelectedItem())&&chosen.code.equals(code.getText().trim())&&chosen.display.equals(display.getText().trim())&&java.util.Objects.equals(chosenCategory,category.getSelectedItem()))return chosen.source;
        return null;
    }
    public void cancelSearch(){if(worker!=null)worker.cancel(true);}
}
