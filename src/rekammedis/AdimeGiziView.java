package rekammedis;

import java.awt.*;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

/** Presentation only. Reuses the original components, documents, permissions, and action listeners. */
public final class AdimeGiziView extends JPanel {
    private static final Color BACK=new Color(243,247,250),INK=new Color(24,44,62),TEAL=new Color(12,132,124),MUTED=new Color(100,116,139);
    private final JSplitPane split;
    private final JPanel input;
    private int previousDivider=430;
    public AdimeGiziView(Map<String,JComponent> controls,JScrollPane[] notes,JScrollPane history,JComponent[] filters,JButton[] actions,JCheckBox showInput){
        super(new BorderLayout(0,8));setBackground(BACK);setBorder(BorderFactory.createEmptyBorder(8,14,10,14));
        JPanel header=new Surface(new BorderLayout(14,0),INK);header.setBorder(BorderFactory.createEmptyBorder(6,14,6,14));
        JLabel title=new JLabel("Catatan ADIME Gizi");title.setFont(new Font("Segoe UI",Font.BOLD,18));title.setForeground(Color.WHITE);header.add(title,BorderLayout.CENTER);
        showInput.setText("Tampilkan input");showInput.setIcon(null);showInput.setSelectedIcon(null);showInput.setRolloverIcon(null);showInput.setRolloverSelectedIcon(null);showInput.setOpaque(false);showInput.setBorderPainted(false);showInput.setFont(new Font("Segoe UI",Font.BOLD,12));showInput.setForeground(Color.WHITE);showInput.setPreferredSize(new Dimension(150,24));header.add(showInput,BorderLayout.EAST);add(header,BorderLayout.NORTH);
        input=new JPanel(new BorderLayout(0,8));input.setOpaque(false);
        JPanel identity=new Surface(new GridBagLayout(),Color.WHITE);identity.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
        GridBagConstraints c=new GridBagConstraints();c.fill=GridBagConstraints.HORIZONTAL;c.insets=new Insets(2,4,2,4);
        JPanel patient=new JPanel(new GridBagLayout());patient.setOpaque(false);GridBagConstraints p=new GridBagConstraints();p.fill=GridBagConstraints.HORIZONTAL;p.insets=new Insets(0,0,0,8);
        field(patient,p,0,"No. rawat",controls.get("visit"),190,0);field(patient,p,1,"No. rekam medis",controls.get("rm"),110,0);field(patient,p,2,"Nama pasien",controls.get("patient"),250,1);
        c.gridx=0;c.gridy=0;c.weightx=1;c.gridwidth=2;identity.add(patient,c);
        JPanel timestamp=new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));timestamp.setOpaque(false);timestamp.add(label("Tanggal / waktu"));
        for(String key:new String[]{"date","hour","minute","second"}){JComponent component=controls.get(key);style(component);component.setPreferredSize(new Dimension("date".equals(key)?132:54,27));timestamp.add(component);}
        JCheckBox automatic=(JCheckBox)controls.get("automatic");automatic.setText("Otomatis");automatic.setOpaque(false);automatic.setForeground(MUTED);automatic.setFont(new Font("Segoe UI",Font.BOLD,12));automatic.setHorizontalAlignment(SwingConstants.LEADING);automatic.setHorizontalTextPosition(SwingConstants.TRAILING);automatic.setIconTextGap(8);automatic.setMargin(new Insets(0,4,0,0));automatic.setPreferredSize(new Dimension(112,27));timestamp.add(automatic);
        c.gridy=1;c.gridwidth=1;c.weightx=0;identity.add(timestamp,c);
        JPanel staff=new JPanel(new GridBagLayout());staff.setOpaque(false);GridBagConstraints s=new GridBagConstraints();s.fill=GridBagConstraints.HORIZONTAL;s.insets=new Insets(0,3,0,3);s.gridx=0;staff.add(label("Petugas"),s);
        for(String key:new String[]{"staffCode","staffName","staffPick"}){JComponent component=controls.get(key);style(component);s.gridx++;s.weightx="staffName".equals(key)?1:0;component.setPreferredSize(new Dimension("staffCode".equals(key)?95:"staffPick".equals(key)?36:180,27));staff.add(component,s);}
        c.gridx=1;c.weightx=1;identity.add(staff,c);input.add(identity,BorderLayout.NORTH);
        JPanel sections=new JPanel(new GridLayout(2,3,10,10));sections.setOpaque(false);String[] names={"Asesmen","Diagnosis","Intervensi","Monitoring","Evaluasi","Instruksi"};String[] badges={"A","D","I","M","E","I"};
        for(int i=0;i<6;i++){
            JPanel section=new Surface(new BorderLayout(0,7),Color.WHITE);section.setBorder(BorderFactory.createEmptyBorder(10,12,10,12));JLabel heading=new JLabel(badges[i]+"   "+names[i]);heading.setForeground(TEAL);heading.setFont(new Font("Segoe UI",Font.BOLD,14));section.add(heading,BorderLayout.NORTH);
            JScrollPane note=notes[i];note.setBorder(BorderFactory.createLineBorder(new Color(222,232,238)));note.getViewport().setBackground(Color.WHITE);note.getVerticalScrollBar().setUnitIncrement(16);Component area=note.getViewport().getView();area.setFont(new Font("Segoe UI",Font.BOLD,14));area.setForeground(INK);
            if(area instanceof JTextArea){JTextArea text=(JTextArea)area;text.setLineWrap(true);text.setWrapStyleWord(true);text.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));text.setBackground(Color.WHITE);}
            note.setPreferredSize(new Dimension(220,112));section.add(note,BorderLayout.CENTER);sections.add(section);
        }
        input.add(sections,BorderLayout.CENTER);input.setMinimumSize(new Dimension(400,250));input.setPreferredSize(new Dimension(1000,435));
        JPanel records=new Surface(new BorderLayout(0,8),Color.WHITE);records.setBorder(BorderFactory.createEmptyBorder(10,14,10,14));records.setMinimumSize(new Dimension(400,130));
        JPanel filterBar=new JPanel(new FlowLayout(FlowLayout.LEFT,7,0));filterBar.setOpaque(false);JLabel historyTitle=new JLabel("Riwayat ADIME");historyTitle.setFont(new Font("Segoe UI",Font.BOLD,14));historyTitle.setForeground(INK);filterBar.add(historyTitle);
        String[] labels={"Dari","Sampai","Cari"};for(int i=0;i<filters.length;i++){JComponent component=filters[i];style(component);if(i<3)filterBar.add(label(labels[i]));if(component instanceof JButton){JButton button=(JButton)component;button.setIcon(null);button.setText(i==3?"Cari":"Semua");colorButton(button,i==3?TEAL:new Color(71,85,105));}component.setPreferredSize(new Dimension(i<2?120:i==2?230:i==3?60:74,30));filterBar.add(component);}records.add(filterBar,BorderLayout.NORTH);
        JTable table=(JTable)history.getViewport().getView();table.setFont(new Font("Segoe UI",Font.PLAIN,12));table.setRowHeight(32);table.setShowGrid(false);table.setIntercellSpacing(new Dimension(0,0));table.setFillsViewportHeight(true);table.setBackground(Color.WHITE);table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));table.getTableHeader().setBackground(new Color(234,243,247));table.getTableHeader().setPreferredSize(new Dimension(100,32));
        int[] widths={170,90,210,65,40,175,300,300,300,300,300,300,100,180};for(int i=0;i<table.getColumnCount();i++)table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){@Override public Component getTableCellRendererComponent(JTable t,Object value,boolean selected,boolean focus,int row,int col){super.getTableCellRendererComponent(t,value,selected,focus,row,col);setBorder(BorderFactory.createEmptyBorder(0,8,0,8));setBackground(selected?new Color(214,239,235):row%2==0?Color.WHITE:new Color(247,250,252));setForeground(INK);setToolTipText(java.util.Objects.toString(value,""));return this;}});
        history.setColumnHeaderView(table.getTableHeader());history.setBorder(BorderFactory.createLineBorder(new Color(225,234,239)));records.add(history,BorderLayout.CENTER);
        split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,input,records);split.setOpaque(false);split.setBorder(BorderFactory.createEmptyBorder());split.setDividerSize(8);split.setResizeWeight(.64);split.setContinuousLayout(true);split.setDividerLocation(previousDivider);add(split,BorderLayout.CENTER);
        JPanel toolbar=new Surface(new BorderLayout(0,9),Color.WHITE);toolbar.setBorder(BorderFactory.createEmptyBorder(10,12,10,12));JPanel buttonRow=new JPanel(new GridLayout(1,actions.length,10,0));buttonRow.setOpaque(false);
        for(JButton button:actions){style(button);button.setPreferredSize(new Dimension(100,40));button.setIcon(null);String text=button.getText();Color color=text.equals("Simpan")?new Color(8,121,102):text.equals("Hapus")?new Color(187,43,65):text.equals("Ganti")?new Color(171,86,0):text.equals("Baru")?new Color(37,99,205):text.equals("Template")?new Color(105,78,183):text.equals("Keluar")?INK:new Color(71,85,105);colorButton(button,color);buttonRow.add(button);}
        toolbar.add(buttonRow,BorderLayout.NORTH);JLabel help=new JLabel("Klik kanan catatan tersimpan untuk Instruksi Diet / NutritionOrder.");help.setForeground(MUTED);help.setFont(new Font("Segoe UI",Font.PLAIN,12));toolbar.add(help,BorderLayout.SOUTH);add(toolbar,BorderLayout.SOUTH);
    }
    private static JLabel label(String text){JLabel label=new JLabel(text);label.setFont(new Font("Segoe UI",Font.PLAIN,12));label.setForeground(MUTED);return label;}
    private static void style(JComponent component){component.setFont(new Font("Segoe UI",Font.BOLD,13));component.setForeground(INK);if(component instanceof JTextField){component.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(215,226,233)),BorderFactory.createEmptyBorder(4,8,4,8)));component.setBackground(Color.WHITE);}else if(component instanceof JComboBox)component.setBackground(Color.WHITE);}
    /** Paint over the legacy glass gradient while keeping the original button and its listeners. */
    private static void colorButton(JButton button,final Color color){
        button.setFont(new Font("Segoe UI",Font.BOLD,13));button.setForeground(Color.WHITE);button.setBackground(color);button.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));button.setContentAreaFilled(false);button.setOpaque(false);button.setFocusPainted(true);button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI(){@Override public void update(Graphics graphics,JComponent component){JButton b=(JButton)component;Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(Color.WHITE);g.fillRect(0,0,b.getWidth(),b.getHeight());g.setColor(!b.isEnabled()?new Color(218,226,232):b.getModel().isPressed()?color.darker():b.getModel().isRollover()?color.brighter():color);g.fillRoundRect(0,0,b.getWidth(),b.getHeight(),10,10);if(b.hasFocus()){g.setColor(new Color(166,215,232));g.drawRoundRect(2,2,b.getWidth()-5,b.getHeight()-5,8,8);}g.dispose();paint(graphics,component);}});
    }
    private static void field(JPanel target,GridBagConstraints c,int col,String text,JComponent component,int width,double weight){JPanel p=new JPanel(new BorderLayout(7,0));p.setOpaque(false);p.add(label(text),BorderLayout.WEST);style(component);component.setPreferredSize(new Dimension(width,28));p.add(component,BorderLayout.CENTER);c.gridx=col;c.weightx=weight;target.add(p,c);}
    public void setInputVisible(boolean visible){if(visible==input.isVisible())return;if(!visible){previousDivider=split.getDividerLocation();input.setVisible(false);split.setDividerSize(0);split.setDividerLocation(0);}else{input.setVisible(true);split.setDividerSize(8);split.setDividerLocation(previousDivider>0?previousDivider:430);}revalidate();repaint();}
    private static final class Surface extends JPanel{private final Color fill;Surface(LayoutManager layout,Color fill){super(layout);this.fill=fill;setOpaque(false);}@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D p=(Graphics2D)g.create();p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);p.setColor(fill);p.fillRoundRect(0,0,getWidth(),getHeight(),16,16);p.dispose();}}
}
