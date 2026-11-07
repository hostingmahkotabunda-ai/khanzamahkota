package bridging;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/** Explicit local identity links; never changes clinical doctor codes or matches names automatically. */
public final class SatuSehatLabDoctorIdentity {
    private static final Object LOCK=new Object();
    private final Path file;
    private final ObjectMapper mapper=new ObjectMapper();
    public SatuSehatLabDoctorIdentity(){this(Paths.get("cache/satusehat-lab-doctor.iyem"));}
    public SatuSehatLabDoctorIdentity(Path file){this.file=file;}
    public static final class Identity {
        public final String name,nik;
        Identity(String name,String nik){this.name=name;this.nik=nik;}
    }
    private ObjectNode read()throws Exception{
        if(!Files.exists(file))return mapper.createObjectNode();
        JsonNode node=mapper.readTree(Files.readAllBytes(file));
        if(node==null||!node.isObject())throw new IllegalStateException("Berkas penghubung dokter tidak valid.");
        return (ObjectNode)node;
    }
    public Identity resolve(Connection db,String doctorCode,String clinicalName)throws Exception{
        if(doctorCode==null||doctorCode.trim().isEmpty())return new Identity(clinicalName,"");
        String employee; synchronized(LOCK){JsonNode n=read().get(doctorCode);employee=n==null?doctorCode:n.asText();}
        try(PreparedStatement s=db.prepareStatement("select nama,no_ktp from pegawai where nik=?")){
            s.setString(1,employee);try(ResultSet r=s.executeQuery()){
                if(!r.next())return new Identity(clinicalName,"");
                String nik=r.getString("no_ktp");
                Identity result=new Identity(clinicalName==null||clinicalName.isEmpty()?r.getString("nama"):clinicalName,nik==null?"":nik.trim());
                if(r.next())throw new IllegalStateException("Kode pegawai tidak unik.");
                return result;
            }
        }
    }
    public void save(Connection db,String doctor,String employee)throws Exception{
        if(doctor==null||doctor.trim().isEmpty()||employee==null||employee.trim().isEmpty())throw new IllegalArgumentException("Pilih dokter dan pegawai yang sesuai.");
        for(String[] check:new String[][]{{"select count(*) from dokter where kd_dokter=?",doctor},{"select count(*) from pegawai where nik=?",employee}}){
            try(PreparedStatement s=db.prepareStatement(check[0])){s.setString(1,check[1]);try(ResultSet r=s.executeQuery()){r.next();if(r.getInt(1)!=1)throw new IllegalArgumentException("Data dokter/pegawai tidak ditemukan atau tidak unik.");}}
        }
        synchronized(LOCK){
            ObjectNode links=read();links.put(doctor,employee);
            Path target=file.toAbsolutePath();Files.createDirectories(target.getParent());
            Path temp=Files.createTempFile(target.getParent(),"lab-doctor-",".tmp");
            try{
                Files.write(temp,mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(links));
                if(Files.exists(target))Files.copy(target,target.resolveSibling(target.getFileName()+"-backup.iyem"),StandardCopyOption.REPLACE_EXISTING);
                try{Files.move(temp,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
                catch(AtomicMoveNotSupportedException e){Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);}
            }finally{Files.deleteIfExists(temp);}
        }
    }
    public boolean choose(java.awt.Component owner,Connection db,String doctor,String name)throws Exception{
        DefaultTableModel model=new DefaultTableModel(new String[]{"Kode Pegawai","Nama Pegawai"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        try(PreparedStatement s=db.prepareStatement("select nik,nama from pegawai order by nama");ResultSet r=s.executeQuery()){
            while(r.next())model.addRow(new Object[]{r.getString(1),r.getString(2)});
        }
        JTable table=new JTable(model);table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableRowSorter<DefaultTableModel> sorter=new TableRowSorter<>(model);table.setRowSorter(sorter);
        JTextField search=new JTextField();search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            private void filter(){sorter.setRowFilter(RowFilter.regexFilter("(?i)"+java.util.regex.Pattern.quote(search.getText()),0,1));}
            public void insertUpdate(javax.swing.event.DocumentEvent e){filter();}public void removeUpdate(javax.swing.event.DocumentEvent e){filter();}public void changedUpdate(javax.swing.event.DocumentEvent e){filter();}
        });
        JPanel panel=new JPanel(new java.awt.BorderLayout(5,5));
        JPanel top=new JPanel(new java.awt.GridLayout(0,1,3,3));
        top.add(new JLabel("Dokter PJ tercatat: "+name));top.add(new JLabel("Pilih pegawai orang yang sama. Cari nama/kode:"));top.add(search);
        panel.add(top,java.awt.BorderLayout.NORTH);panel.add(new JScrollPane(table));panel.setPreferredSize(new java.awt.Dimension(580,380));
        if(JOptionPane.showConfirmDialog(owner,panel,"Hubungkan Dokter PJ Lab",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)!=JOptionPane.OK_OPTION)return false;
        if(table.getSelectedRow()<0)throw new IllegalArgumentException("Belum memilih pegawai. Penghubung tidak disimpan.");
        save(db,doctor,model.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()),0).toString());return true;
    }
    public static boolean validReferences(String patient,String practitioner){
        return patient!=null&&patient.matches("[A-Za-z0-9.-]{1,64}")&&practitioner!=null&&practitioner.matches("[A-Za-z0-9.-]{1,64}");
    }
}
