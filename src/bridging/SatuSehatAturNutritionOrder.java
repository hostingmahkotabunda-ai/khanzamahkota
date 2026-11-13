package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Review a saved ADIME without modifying it or claiming clinical approval automatically. */
public final class SatuSehatAturNutritionOrder extends JDialog {
    private static final Color INK=new Color(24,44,62),TEAL=new Color(12,132,124),MUTED=new Color(100,116,139),BACK=new Color(243,247,250);
    private final JComboBox<String> intent=new JComboBox<String>(new String[]{"","proposal - Rekomendasi","order - Instruksi dokter"});
    private final JComboBox<String> status=new JComboBox<String>(new String[]{"draft","active","on-hold","revoked","completed","entered-in-error"});
    private final JComboBox<SatuSehatNutritionOrderService.Doctor> doctor=new JComboBox<SatuSehatNutritionOrderService.Doctor>();
    private final SatuSehatNutritionOrderDateTime issued,start,end;
    private final JTextArea instruction=new JTextArea(5,55);
    private final JCheckBox reviewed=new JCheckBox("Saya sudah meninjau instruksi, waktu, dan dokter pemberi instruksi berdasarkan dokumentasi.");
    private final DefaultTableModel diets=model(),textures=model();
    private final JTable dietTable=new JTable(diets),textureTable=new JTable(textures);
    private ObjectNode result;
    private static DefaultTableModel model(){return new DefaultTableModel(new String[]{"Kode lokal","Nama lokal","Sistem","Kode terminologi","Display resmi","Terverifikasi"},0){@Override public Class<?> getColumnClass(int col){return col==5?Boolean.class:String.class;}@Override public boolean isCellEditable(int row,int col){return col>=2;}};}
    public SatuSehatAturNutritionOrder(Window owner,SatuSehatNutritionOrderService.Row row,List<SatuSehatNutritionOrderService.Doctor> doctors,List<String> candidates){
        super(owner,"Instruksi Diet / NutritionOrder",ModalityType.APPLICATION_MODAL);setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        issued=new SatuSehatNutritionOrderDateTime(row.date);start=new SatuSehatNutritionOrderDateTime(row.date);end=new SatuSehatNutritionOrderDateTime(row.date);
        getContentPane().setBackground(BACK);
        JPanel root=new JPanel(new BorderLayout(0,14));root.setBackground(BACK);root.setBorder(BorderFactory.createEmptyBorder(18,22,16,22));setContentPane(root);
        JPanel top=new JPanel();top.setOpaque(false);top.setLayout(new BoxLayout(top,BoxLayout.Y_AXIS));
        JPanel hero=new Surface(new BorderLayout(),INK);hero.setBorder(BorderFactory.createEmptyBorder(16,20,16,20));
        JPanel heading=new JPanel(new GridLayout(2,1,0,4));heading.setOpaque(false);
        JLabel title=new JLabel("Instruksi diet pasien");title.setForeground(Color.WHITE);title.setFont(new Font("Segoe UI",Font.BOLD,23));heading.add(title);
        JLabel subtitle=new JLabel("Lengkapi rencana diet dan waktu pelayanan sesuai dokumentasi.");subtitle.setForeground(new Color(194,218,228));subtitle.setFont(new Font("Segoe UI",Font.PLAIN,13));heading.add(subtitle);hero.add(heading,BorderLayout.CENTER);
        JLabel badge=new JLabel("GIZI  /  SATUSEHAT");badge.setForeground(new Color(111,226,211));badge.setFont(new Font("Segoe UI",Font.BOLD,12));hero.add(badge,BorderLayout.EAST);top.add(hero);top.add(Box.createVerticalStrut(12));
        JPanel sourceBox=new JPanel(new BorderLayout(10,6));sourceBox.setOpaque(false);sourceBox.setBorder(BorderFactory.createEmptyBorder(0,4,0,4));
        JLabel patient=new JLabel(row.rm+"  •  "+row.name);patient.setFont(new Font("Segoe UI",Font.BOLD,14));patient.setForeground(INK);
        JLabel visit=new JLabel("No. rawat "+row.visit+"   |   ADIME "+row.date);visit.setFont(new Font("Segoe UI",Font.PLAIN,12));visit.setForeground(MUTED);
        JPanel sourceTitle=new JPanel(new GridLayout(2,1,0,4));sourceTitle.setOpaque(false);sourceTitle.add(patient);sourceTitle.add(visit);sourceBox.add(sourceTitle,BorderLayout.CENTER);
        int n=0;
        JTextArea source=new JTextArea("Intervensi:\n"+row.intervention+"\n\nInstruksi ADIME:\n"+row.instruction,5,55);source.setEditable(false);source.setLineWrap(true);source.setWrapStyleWord(true);
        styleText(source);source.setBackground(new Color(235,246,245));JScrollPane sourceScroll=new JScrollPane(source);sourceScroll.setVisible(false);sourceScroll.setPreferredSize(new Dimension(600,110));sourceBox.add(sourceScroll,BorderLayout.SOUTH);
        JButton sourceToggle=actionButton("Lihat catatan ADIME",false);sourceToggle.addActionListener(e->{boolean show=!sourceScroll.isVisible();sourceScroll.setVisible(show);sourceToggle.setText(show?"Tutup catatan ADIME":"Lihat catatan ADIME");root.revalidate();root.repaint();});sourceBox.add(sourceToggle,BorderLayout.EAST);top.add(sourceBox);root.add(top,BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();tabs.setFont(new Font("Segoe UI",Font.BOLD,14));tabs.setBackground(BACK);tabs.setForeground(TEAL);
        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI(){@Override protected void installDefaults(){super.installDefaults();tabInsets=new Insets(8,16,8,16);contentBorderInsets=new Insets(0,0,0,0);}@Override protected void paintContentBorder(Graphics g,int placement,int selected){}@Override protected void paintTabBorder(Graphics g,int placement,int index,int x,int y,int w,int h,boolean selected){}@Override protected void paintTabBackground(Graphics g,int placement,int index,int x,int y,int w,int h,boolean selected){g.setColor(selected?new Color(219,240,237):BACK);g.fillRoundRect(x,y,w,h,12,12);}});
        JPanel first=stack(),second=stack();JPanel form=new JPanel(new GridBagLayout());form.setOpaque(false);GridBagConstraints c=new GridBagConstraints();c.insets=new Insets(3,4,3,8);c.fill=GridBagConstraints.HORIZONTAL;c.weightx=1;
        addRow(form,c,n++,"Tujuan",intent);addRow(form,c,n++,"Status (sesuai kondisi)",status);
        doctor.addItem(null);for(SatuSehatNutritionOrderService.Doctor d:doctors)doctor.addItem(d);
        addRow(form,c,n++,"Dokter pemberi instruksi",doctor);
        java.util.List<String> names=new java.util.ArrayList<String>();for(String code:candidates){String name=code;for(SatuSehatNutritionOrderService.Doctor d:doctors)if(d.code.equals(code)){name=d.name;break;}names.add(name);}
        JTextArea pj=new JTextArea(names.isEmpty()?"DPJP/PJ belum tersedia; pilih dokter sesuai dokumentasi.":String.join("; ",names)+(new java.util.HashSet<String>(candidates).size()>1?". Ada beberapa DPJP; pilih pemberi instruksi yang sesuai.":". Dokter tetap dapat disesuaikan dengan instruksi yang tercatat."));pj.setEditable(false);pj.setLineWrap(true);pj.setWrapStyleWord(true);pj.setOpaque(false);pj.setRows(names.size()>1?2:1);
        addRow(form,c,n++,"DPJP/PJ kunjungan",pj);
        first.add(card("Penanggung jawab","Dokter mengikuti DPJP/PJ kunjungan dan tetap dapat dikonfirmasi.",form));first.add(Box.createVerticalStrut(12));
        JPanel timing=new JPanel(new GridBagLayout());timing.setOpaque(false);n=0;addRow(timing,c,n++,"Instruksi dibuat",issued);addRow(timing,c,n++,"Mulai berlaku",start);addRow(timing,c,n++,"Akhir berlaku",end);
        first.add(card("Waktu pelayanan","Pilih tanggal, jam, dan menit sesuai catatan. Detik untuk waktu baru otomatis 00.",timing));first.add(Box.createVerticalGlue());
        second.add(card("Jenis diet","Tambahkan jenis diet yang sesuai dengan instruksi dokter.",conceptPanel(dietTable,diets,SatuSehatNutritionOrderPayload.DIETS,false)));second.add(Box.createVerticalStrut(12));
        second.add(card("Bentuk makanan","Pilih bentuk makanan yang diberikan kepada pasien.",conceptPanel(textureTable,textures,SatuSehatNutritionOrderPayload.TEXTURES,true)));second.add(Box.createVerticalStrut(12));
        instruction.setLineWrap(true);instruction.setWrapStyleWord(true);styleText(instruction);second.add(card("Instruksi yang ditinjau","Teks ADIME menjadi bahan awal; pastikan sesuai instruksi yang berlaku.",new JScrollPane(instruction)));
        tabs.addTab("01   Instruksi & waktu",page(first));tabs.addTab("02   Diet & catatan",page(second));root.add(tabs,BorderLayout.CENTER);
        JPanel footer=new Surface(new BorderLayout(10,8),Color.WHITE);footer.setBorder(BorderFactory.createEmptyBorder(12,16,12,16));reviewed.setOpaque(false);reviewed.setFont(new Font("Segoe UI",Font.PLAIN,12));reviewed.setForeground(INK);footer.add(reviewed,BorderLayout.NORTH);
        JLabel warning=new JLabel("Perubahan diet dicatat melalui ADIME lanjutan; riwayat sebelumnya tetap disimpan.");warning.setFont(new Font("Segoe UI",Font.PLAIN,12));warning.setForeground(MUTED);footer.add(warning,BorderLayout.CENTER);
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));buttons.setOpaque(false);JButton save=actionButton("Simpan tinjauan",true),cancel=actionButton("Batal",false);buttons.add(cancel);buttons.add(save);footer.add(buttons,BorderLayout.EAST);root.add(footer,BorderLayout.SOUTH);
        save.addActionListener(e->{try{if(dietTable.isEditing()&&!dietTable.getCellEditor().stopCellEditing())return;if(textureTable.isEditing()&&!textureTable.getCellEditor().stopCellEditing())return;
            ObjectNode r=SatuSehatNutritionOrderPayload.JSON.createObjectNode();String selected=String.valueOf(intent.getSelectedItem());r.put("intent",selected.isEmpty()?"":selected.split(" ")[0]);r.put("status",String.valueOf(status.getSelectedItem()));
            SatuSehatNutritionOrderService.Doctor d=(SatuSehatNutritionOrderService.Doctor)doctor.getSelectedItem();r.put("doctor",d==null?"":d.code);r.put("issued",issued.value());r.put("start",start.value());r.put("end",end.value());r.put("instruction",instruction.getText().trim());r.put("reviewed",reviewed.isSelected());ArrayNode types=r.putArray("diets");for(int i=0;i<diets.getRowCount();i++)types.add(concept(diets,i));if(textures.getRowCount()>0)r.set("texture",concept(textures,0));
            // Unresolved drafts can be saved; the same payload validator gates every send.
            if(!"draft".equals(r.path("status").asText()))SatuSehatNutritionOrderPayload.validateReview(r,java.time.Clock.systemUTC());
            result=r;dispose();
        }catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Instruksi belum lengkap",JOptionPane.WARNING_MESSAGE);}});
        cancel.addActionListener(e->dispose());
        JsonNode r=row.review;String mode=r.path("intent").asText();intent.setSelectedIndex("proposal".equals(mode)?1:"order".equals(mode)?2:0);status.setSelectedItem(r.has("status")?r.path("status").asText():"draft");
        String saved=r.path("doctor").isNull()?"":r.path("doctor").asText();String selection=suggestedDoctor(saved,candidates,doctors);boolean found=false;
        for(int i=1;i<doctor.getItemCount();i++)if(doctor.getItemAt(i).code.equals(selection)){doctor.setSelectedIndex(i);found=true;break;}
        if(!saved.isEmpty()&&!found){SatuSehatNutritionOrderService.Doctor stored=new SatuSehatNutritionOrderService.Doctor(saved,"Dokter tersimpan; periksa master bila perlu");doctor.addItem(stored);doctor.setSelectedItem(stored);}
        issued.setValue(r.path("issued").asText());start.setValue(r.path("start").asText());end.setValue(r.path("end").asText());instruction.setText(r.has("instruction")?r.path("instruction").asText():row.instruction);reviewed.setSelected(r.path("reviewed").asBoolean());
        for(JsonNode d:r.path("diets"))addConcept(diets,d);if(r.has("texture")&&!r.path("texture").isNull())addConcept(textures,r.path("texture"));
        Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();setSize(Math.min(1080,screen.width-60),Math.min(850,screen.height-90));setLocationRelativeTo(owner);
    }
    private static JPanel stack(){JPanel p=new JPanel();p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.setBackground(BACK);p.setBorder(BorderFactory.createEmptyBorder(14,0,10,0));return p;}
    private static JScrollPane page(JPanel p){JScrollPane s=new JScrollPane(p);s.setBorder(BorderFactory.createEmptyBorder());s.getViewport().setBackground(BACK);s.getVerticalScrollBar().setUnitIncrement(18);return s;}
    private static JPanel card(String title,String detail,Component body){JPanel card=new Surface(new BorderLayout(0,10),Color.WHITE);card.setAlignmentX(Component.LEFT_ALIGNMENT);card.setBorder(BorderFactory.createEmptyBorder(12,18,12,18));JPanel text=new JPanel(new GridLayout(2,1,0,4));text.setOpaque(false);JLabel h=new JLabel(title);h.setFont(new Font("Segoe UI",Font.BOLD,16));h.setForeground(INK);text.add(h);JLabel sub=new JLabel(detail);sub.setFont(new Font("Segoe UI",Font.PLAIN,12));sub.setForeground(MUTED);text.add(sub);card.add(text,BorderLayout.NORTH);card.add(body,BorderLayout.CENTER);card.setMaximumSize(new Dimension(Integer.MAX_VALUE,card.getPreferredSize().height));return card;}
    private static void styleText(JTextArea area){area.setFont(new Font("Segoe UI",Font.PLAIN,13));area.setForeground(INK);area.setBorder(BorderFactory.createEmptyBorder(10,12,10,12));area.setBackground(Color.WHITE);}
    static JButton actionButton(String text,boolean primary){JButton b=new JButton(text){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(!isEnabled()?new Color(229,235,239):primary?(getModel().isRollover()?TEAL.darker():TEAL):new Color(230,241,242));g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);g2.dispose();super.paintComponent(g);}};b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);b.setBorder(BorderFactory.createEmptyBorder(10,16,10,16));b.setForeground(primary?Color.WHITE:TEAL);b.setFont(new Font("Segoe UI",Font.BOLD,13));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;}
    static final class Surface extends JPanel{private final Color fill;Surface(LayoutManager layout,Color fill){super(layout);this.fill=fill;setOpaque(false);}@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D p=(Graphics2D)g.create();p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);p.setColor(fill);p.fillRoundRect(0,0,getWidth(),getHeight(),18,18);p.dispose();}}
    public static String suggestedDoctor(String saved,List<String> candidates,List<SatuSehatNutritionOrderService.Doctor> doctors){
        if(saved!=null&&!saved.trim().isEmpty())return saved;
        java.util.Set<String> unique=new java.util.LinkedHashSet<String>();for(String code:candidates)if(code!=null&&!code.trim().isEmpty())unique.add(code);
        if(unique.size()==1){String candidate=unique.iterator().next();for(SatuSehatNutritionOrderService.Doctor d:doctors)if(d.code.equals(candidate))return candidate;}return "";
    }
    private static JPanel conceptPanel(JTable table,DefaultTableModel model,List<SatuSehatNutritionOrderPayload.Concept> choices,boolean single){
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);for(int i=0;i<6;i++)table.getColumnModel().getColumn(i).setPreferredWidth(i==2||i==4?210:i==1?160:100);
        table.getColumnModel().getColumn(5).setMinWidth(100);
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JComboBox<String>(new String[]{SatuSehatNutritionOrderPayload.SCT,SatuSehatNutritionOrderPayload.KEMKES})));
        JPanel panel=new JPanel(new BorderLayout(0,8));panel.setOpaque(false);JComboBox<SatuSehatNutritionOrderPayload.Concept> select=new JComboBox<SatuSehatNutritionOrderPayload.Concept>();for(SatuSehatNutritionOrderPayload.Concept c:choices)select.addItem(c);select.setFont(new Font("Segoe UI",Font.PLAIN,13));select.setBackground(Color.WHITE);
        JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0));top.setOpaque(false);JButton add=actionButton("+ Tambahkan",true),remove=actionButton("Hapus pilihan",false);top.add(select);top.add(add);top.add(remove);panel.add(top,BorderLayout.NORTH);JScrollPane scroll=new JScrollPane(table);scroll.setPreferredSize(new Dimension(720,single?76:110));scroll.setBorder(BorderFactory.createLineBorder(new Color(224,233,239)));panel.add(scroll,BorderLayout.CENTER);
        table.setRowHeight(30);table.setFont(new Font("Segoe UI",Font.PLAIN,12));table.setForeground(INK);table.setSelectionBackground(new Color(215,241,236));table.setSelectionForeground(INK);table.setShowGrid(false);table.setFillsViewportHeight(true);table.setBackground(Color.WHITE);table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));table.getTableHeader().setBackground(new Color(234,243,247));
        add.addActionListener(e->{SatuSehatNutritionOrderPayload.Concept c=(SatuSehatNutritionOrderPayload.Concept)select.getSelectedItem();if(single)model.setRowCount(0);for(int i=0;i<model.getRowCount();i++)if(c.local.equals(model.getValueAt(i,0)))return;addConcept(model,c.json());});
        remove.addActionListener(e->{int row=table.getSelectedRow();if(row>=0)model.removeRow(row);});
        JLabel note=new JLabel("Periksa kesesuaian kode sebelum menandai Terverifikasi.");note.setFont(new Font("Segoe UI",Font.PLAIN,12));note.setForeground(MUTED);panel.add(note,BorderLayout.SOUTH);return panel;
    }
    private static void addConcept(DefaultTableModel m,JsonNode c){m.addRow(new Object[]{c.path("local").asText(),c.path("label").asText(),c.path("system").asText(),c.path("code").asText(),c.path("display").asText(),c.path("confirmed").asBoolean()});}
    private static ObjectNode concept(DefaultTableModel m,int row){ObjectNode c=SatuSehatNutritionOrderPayload.JSON.createObjectNode();String[] keys={"local","label","system","code","display"};for(int i=0;i<5;i++)c.put(keys[i],java.util.Objects.toString(m.getValueAt(row,i),"").trim());c.put("confirmed",Boolean.TRUE.equals(m.getValueAt(row,5)));return c;}
    private static void addRow(JPanel form,GridBagConstraints c,int row,String label,Component input){c.gridy=row;c.gridx=0;c.weightx=0;JLabel caption=new JLabel(label);caption.setFont(new Font("Segoe UI",Font.PLAIN,13));caption.setForeground(MUTED);form.add(caption,c);c.gridx=1;c.weightx=1;if(input instanceof JComboBox){input.setFont(new Font("Segoe UI",Font.PLAIN,14));input.setBackground(Color.WHITE);input.setPreferredSize(new Dimension(280,32));}form.add(input,c);}
    public ObjectNode result(){return result;}
}
