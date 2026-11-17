package bridging;

import fungsi.koneksiDB;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import javax.swing.*;

/** Form kerja lokal untuk asisten poli. Form ini tidak melakukan HTTP ke SATUSEHAT. */
public final class SatuSehatInputEpisodeOfCare extends JDialog {
    private final Connection db=koneksiDB.condb(); private final String noRawat;
    private String rm="",pasien="",tgl="";
    private final CardLayout cards=new CardLayout(); private final JPanel body=new JPanel(cards);
    private final JRadioButton anc=new JRadioButton("ANC / Kehamilan",true), ptm=new JRadioButton("PTM");
    private final JDateChooser hpht=SatuSehatKirimNutritionOrder.calendar(LocalDate.now());
    private final JTextField mulai=new JTextField(12);
    private final JComboBox<String> kunjungan=new JComboBox<>(new String[]{
        "K1A — Kunjungan pertama akses", "K1M — Kunjungan pertama murni",
        "K2 — Kunjungan ANC ke-2", "K3 — Kunjungan ANC ke-3", "K4 — Kunjungan ANC ke-4",
        "K5 — Kunjungan ANC ke-5", "K6 — Kunjungan ANC ke-6"});
    private final JComboBox<Diagnosis> diagnosis=new JComboBox<>();
    private final JComboBox<String> tipe=new JComboBox<>(new String[]{"CAD","CVD","CKD","cancer"});
    private final JComboBox<String> status=new JComboBox<>(new String[]{"waitlist","active"});
    private final JLabel info=new JLabel(" ");
    public SatuSehatInputEpisodeOfCare(Window owner,String noRawat){super(owner,"Catatan Episode Perawatan SATUSEHAT",ModalityType.APPLICATION_MODAL);this.noRawat=noRawat;muat();ui();}
    private void muat(){
        try (PreparedStatement p=db.prepareStatement("select r.no_rkm_medis,p.nm_pasien,r.tgl_registrasi from reg_periksa r join pasien p on p.no_rkm_medis=r.no_rkm_medis where r.no_rawat=?")) {
            p.setString(1,noRawat);
            try (ResultSet r=p.executeQuery()) { if(r.next()){rm=r.getString(1);pasien=r.getString(2);tgl=r.getString(3);mulai.setText(tgl);} }
        } catch(Exception e){throw new IllegalArgumentException("Kunjungan tidak dapat dimuat.",e);}
        try (PreparedStatement p=db.prepareStatement("select d.kd_penyakit,ifnull(x.nm_penyakit,d.kd_penyakit),ifnull(c.id_condition,'') from diagnosa_pasien d left join penyakit x on x.kd_penyakit=d.kd_penyakit left join satu_sehat_condition c on c.no_rawat=d.no_rawat and c.kd_penyakit=d.kd_penyakit where d.no_rawat=? order by d.prioritas")) {
            p.setString(1,noRawat);
            try (ResultSet r=p.executeQuery()) { while(r.next()) diagnosis.addItem(new Diagnosis(r.getString(1),r.getString(2),r.getString(3))); }
        } catch(Exception ignored){}
    }
    private void ui(){
        setSize(760,500);setLocationRelativeTo(getOwner());setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel root=new JPanel(new BorderLayout(12,12));root.setBorder(BorderFactory.createEmptyBorder(14,16,14,16));root.setBackground(new Color(244,248,252));
        JPanel title=new JPanel(new BorderLayout());title.setBackground(new Color(20,48,70));title.setBorder(BorderFactory.createEmptyBorder(14,16,14,16));
        JLabel h=new JLabel("Catatan Episode Perawatan");h.setForeground(Color.WHITE);h.setFont(h.getFont().deriveFont(Font.BOLD,20f));title.add(h,BorderLayout.NORTH);
        JLabel sub=new JLabel("Simpan data klinis terlebih dahulu. Pengiriman dilakukan dari menu EpisodeOfCare.");sub.setForeground(new Color(220,235,245));title.add(sub,BorderLayout.SOUTH);root.add(title,BorderLayout.NORTH);
        JPanel content=new JPanel(new BorderLayout(8,8));content.setOpaque(false);
        JLabel identity=new JLabel("No. Rawat  "+noRawat+"     RM  "+rm+"     Pasien  "+pasien);identity.setBorder(BorderFactory.createEmptyBorder(2,5,6,5));content.add(identity,BorderLayout.NORTH);
        ButtonGroup group=new ButtonGroup();group.add(anc);group.add(ptm);JPanel tabs=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));tabs.add(anc);tabs.add(ptm);content.add(tabs,BorderLayout.CENTER);
        body.add(ancPanel(),"ANC");body.add(ptmPanel(),"PTM");content.add(body,BorderLayout.SOUTH);root.add(content,BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout());info.setForeground(new Color(80,90,100));bottom.add(info,BorderLayout.CENTER);JButton save=new JButton("Simpan sebagai siap ditinjau");save.setBackground(new Color(0,121,107));save.setForeground(Color.WHITE);save.addActionListener(e->simpan());JButton close=new JButton("Tutup");close.addActionListener(e->dispose());JPanel b=new JPanel();b.add(close);b.add(save);bottom.add(b,BorderLayout.EAST);root.add(bottom,BorderLayout.SOUTH);
        anc.addActionListener(e->cards.show(body,"ANC"));ptm.addActionListener(e->cards.show(body,"PTM"));diagnosis.addActionListener(e->saran());add(root);
    }
    private JPanel ancPanel(){JPanel p=form();add(p,"HPHT",hpht);add(p,"Kategori kunjungan",kunjungan);add(p,"Tanggal kunjungan",new JLabel(tgl));return p;}
    private JPanel ptmPanel(){JPanel p=form();add(p,"Diagnosis dokter",diagnosis);add(p,"Saran tipe PTM",tipe);add(p,"Status klinis",status);add(p,"Tanggal mulai",mulai);return p;}
    private JPanel form(){JPanel p=new JPanel(new GridBagLayout());p.setBackground(Color.WHITE);p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(215,225,235)),BorderFactory.createEmptyBorder(14,14,14,14)));return p;}
    private void add(JPanel p,String label,JComponent c){GridBagConstraints g=new GridBagConstraints();g.gridx=0;g.gridy=p.getComponentCount();g.anchor=GridBagConstraints.WEST;g.insets=new Insets(5,4,5,12);p.add(new JLabel(label),g);g.gridx=1;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;p.add(c,g);}
    private void saran(){Diagnosis d=(Diagnosis)diagnosis.getSelectedItem();if(d==null)return;SatuSehatPtmRecommendation.Result r=SatuSehatPtmRecommendation.recommend(d.code,d.name);if(!r.type.isEmpty())tipe.setSelectedItem(r.type);info.setText(r.explanation);}
    private void simpan(){try{if(anc.isSelected())simpanAnc();else simpanPtm();JOptionPane.showMessageDialog(this,"Catatan tersimpan lokal dan siap ditinjau sebelum pengiriman.");dispose();}catch(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Episode Perawatan",JOptionPane.WARNING_MESSAGE);}}
    private void simpanAnc()throws Exception{LocalDate h=LocalDate.parse(SatuSehatKirimNutritionOrder.dateValue(hpht));String kategori=String.valueOf(kunjungan.getSelectedItem()).split("\\s+")[0];try(PreparedStatement p=db.prepareStatement("insert into satu_sehat_episode_kehamilan(no_rkm_medis,hpht,status,no_rawat_awal,identifier_lokal) values(?,?,'active',?,?) on duplicate key update diubah_pada=current_timestamp")){p.setString(1,rm);p.setString(2,h.toString());p.setString(3,noRawat);p.setString(4,"ANC-"+rm+"-"+h.toString().replace("-", ""));p.executeUpdate();}try(PreparedStatement p=db.prepareStatement("insert into satu_sehat_kunjungan_anc(no_rawat,id_episode_kehamilan,status_kunjungan) select ?,id_episode_kehamilan,? from satu_sehat_episode_kehamilan where no_rkm_medis=? and hpht=? on duplicate key update status_kunjungan=values(status_kunjungan)")){p.setString(1,noRawat);p.setString(2,kategori);p.setString(3,rm);p.setString(4,h.toString());if(p.executeUpdate()==0)throw new Exception("Episode kehamilan tidak dapat disimpan.");}}
    private void simpanPtm()throws Exception{Diagnosis d=(Diagnosis)diagnosis.getSelectedItem();if(d==null)throw new Exception("Diagnosis dokter belum tersedia pada kunjungan ini.");LocalDate.parse(mulai.getText().trim());try(PreparedStatement p=db.prepareStatement("insert into satu_sehat_episode_ptm_draft(no_rawat,no_rkm_medis,kd_penyakit,id_condition,tipe,status,tanggal_mulai) values(?,?,?,?,?,?,?) on duplicate key update kd_penyakit=values(kd_penyakit),id_condition=values(id_condition),tipe=values(tipe),status=values(status),tanggal_mulai=values(tanggal_mulai),diubah_pada=current_timestamp")){p.setString(1,noRawat);p.setString(2,rm);p.setString(3,d.code);p.setString(4,d.condition);p.setString(5,(String)tipe.getSelectedItem());p.setString(6,(String)status.getSelectedItem());p.setString(7,mulai.getText().trim());p.executeUpdate();}}
    private static final class Diagnosis{final String code,name,condition;Diagnosis(String c,String n,String i){code=c;name=n;condition=i;}public String toString(){return code+" - "+name+(condition.isEmpty()?"  (Condition belum terkirim)":"");}}
}
