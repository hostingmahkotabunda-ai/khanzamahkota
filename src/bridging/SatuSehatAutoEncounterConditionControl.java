package bridging;

import fungsi.koneksiDB;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class SatuSehatAutoEncounterConditionControl extends JDialog {
    private final Connection koneksi = koneksiDB.condb();
    private final SatuSehatAutoEncounterConditionService service = SatuSehatAutoEncounterConditionService.getInstance();
    private final JLabel lblEnabled = new JLabel();
    private final JLabel lblRunning = new JLabel();
    private final JLabel lblEncounterQueue = new JLabel();
    private final JLabel lblConditionQueue = new JLabel();
    private final JLabel lblEncounterLock = new JLabel();
    private final JLabel lblConditionLock = new JLabel();
    private final JComboBox cmbFilter = new JComboBox(new String[]{"Semua","Gagal","Sukses"});
    private final DefaultTableModel tabMode = new DefaultTableModel(null,new String[]{
        "Waktu","Service","Resource","No.Rawat","ICD10","Status","Keterangan"
    }){
        @Override public boolean isCellEditable(int row, int column){
            return false;
        }
    };
    private final JTable tbLog = new JTable(tabMode);

    public SatuSehatAutoEncounterConditionControl(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        refreshData();
    }

    private void initComponents() {
        setTitle("Kontrol Auto Satu Sehat Encounter & Condition");
        setSize(980, 620);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(8, 8));

        JPanel pnlStatus = new JPanel(new GridLayout(3, 2, 8, 8));
        pnlStatus.setBorder(BorderFactory.createTitledBorder("Status Service"));
        pnlStatus.add(lblEnabled);
        pnlStatus.add(lblRunning);
        pnlStatus.add(lblEncounterQueue);
        pnlStatus.add(lblConditionQueue);
        pnlStatus.add(lblEncounterLock);
        pnlStatus.add(lblConditionLock);

        JPanel pnlControl = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        pnlControl.setBorder(BorderFactory.createTitledBorder("Kontrol"));

        JButton btnEnable = new JButton("Aktifkan");
        btnEnable.addActionListener((evt) -> {
            service.setEnabled(true);
            refreshData();
        });
        pnlControl.add(btnEnable);

        JButton btnDisable = new JButton("Nonaktifkan");
        btnDisable.addActionListener((evt) -> {
            service.setEnabled(false);
            refreshData();
        });
        pnlControl.add(btnDisable);

        JButton btnRunEncounter = new JButton("Jalankan Encounter");
        btnRunEncounter.addActionListener((evt) -> {
            service.runEncounterNow();
            refreshData();
        });
        pnlControl.add(btnRunEncounter);

        JButton btnRunCondition = new JButton("Jalankan Condition");
        btnRunCondition.addActionListener((evt) -> {
            service.runConditionNow();
            refreshData();
        });
        pnlControl.add(btnRunCondition);

        JButton btnRunAll = new JButton("Jalankan Semua");
        btnRunAll.addActionListener((evt) -> {
            service.runAllNow();
            refreshData();
        });
        pnlControl.add(btnRunAll);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener((evt) -> refreshData());
        pnlControl.add(btnRefresh);

        pnlControl.add(new JLabel("Filter Log"));
        cmbFilter.addActionListener((evt) -> loadLog());
        pnlControl.add(cmbFilter);

        JPanel pnlNorth = new JPanel(new BorderLayout(8, 8));
        pnlNorth.add(pnlStatus, BorderLayout.CENTER);
        pnlNorth.add(pnlControl, BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(tbLog);
        scroll.setBorder(BorderFactory.createTitledBorder("Log Auto Kirim"));
        add(pnlNorth, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void refreshData() {
        lblEnabled.setText("Aktif: " + (service.isEnabled() ? "Ya" : "Tidak"));
        lblRunning.setText("Scheduler Berjalan: " + (service.isRunning() ? "Ya" : "Tidak"));
        lblEncounterQueue.setText("Antrean Encounter: " + hitungAntreanEncounter());
        lblConditionQueue.setText("Antrean Condition: " + hitungAntreanCondition());
        lblEncounterLock.setText("Status Encounter: " + bacaLock("encounter"));
        lblConditionLock.setText("Status Condition: " + bacaLock("condition"));
        loadLog();
    }

    private String bacaLock(String serviceName) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement("select ifnull(last_status,'-') as last_status, ifnull(last_message,'') as last_message from satu_sehat_auto_service_lock where service_name=?");
            ps.setString(1, serviceName);
            rs = ps.executeQuery();
            if (rs.next()) {
                String pesan = rs.getString("last_message");
                if (pesan.length() > 70) {
                    pesan = pesan.substring(0, 70);
                }
                return rs.getString("last_status") + (pesan.equals("") ? "" : " | " + pesan);
            }
        } catch (Exception e) {
            return "Gagal baca status: " + e;
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
            try {
                if (ps != null) ps.close();
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
        }
        return "-";
    }

    private int hitungAntreanEncounter() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement(
                    "select count(*) as total from (" +
                    "select reg_periksa.no_rawat from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                    "inner join pegawai on pegawai.nik=reg_periksa.kd_dokter inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli " +
                    "inner join satu_sehat_mapping_lokasi_ralan on satu_sehat_mapping_lokasi_ralan.kd_poli=poliklinik.kd_poli " +
                    "left join nota_jalan on nota_jalan.no_rawat=reg_periksa.no_rawat left join satu_sehat_encounter on satu_sehat_encounter.no_rawat=reg_periksa.no_rawat " +
                    "where reg_periksa.status_lanjut='Ralan' and reg_periksa.tgl_registrasi between date_sub(curdate(), interval ? day) and curdate() " +
                    "and ifnull(satu_sehat_encounter.id_encounter,'')='' and ifnull(pasien.no_ktp,'')<>'' and ifnull(pegawai.no_ktp,'')<>'' " +
                    "union all " +
                    "select reg_periksa.no_rawat from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                    "inner join pegawai on pegawai.nik=reg_periksa.kd_dokter " +
                    "inner join (" +
                    "select ki1.no_rawat,ki1.kd_kamar,mapping_ranap.id_lokasi_satusehat,ki1.tgl_keluar,ki1.jam_keluar " +
                    "from kamar_inap ki1 inner join satu_sehat_mapping_lokasi_ranap mapping_ranap on mapping_ranap.kd_kamar=ki1.kd_kamar " +
                    "inner join (" +
                    "select ki2.no_rawat,max(timestamp(ki2.tgl_masuk,ki2.jam_masuk)) as max_masuk " +
                    "from kamar_inap ki2 inner join satu_sehat_mapping_lokasi_ranap mapping_ranap2 on mapping_ranap2.kd_kamar=ki2.kd_kamar " +
                    "group by ki2.no_rawat" +
                    ") pilihan_kamar on pilihan_kamar.no_rawat=ki1.no_rawat and timestamp(ki1.tgl_masuk,ki1.jam_masuk)=pilihan_kamar.max_masuk " +
                    ") kamar_ranap on kamar_ranap.no_rawat=reg_periksa.no_rawat " +
                    "left join nota_inap on nota_inap.no_rawat=reg_periksa.no_rawat left join satu_sehat_encounter on satu_sehat_encounter.no_rawat=reg_periksa.no_rawat " +
                    "where reg_periksa.status_lanjut='Ranap' and reg_periksa.tgl_registrasi between date_sub(curdate(), interval ? day) and curdate() " +
                    "and (ifnull(nota_inap.no_rawat,'')<>'' or (kamar_ranap.tgl_keluar<>'0000-00-00' and kamar_ranap.jam_keluar<>'00:00:00')) " +
                    "and ifnull(satu_sehat_encounter.id_encounter,'')='' and ifnull(pasien.no_ktp,'')<>'' and ifnull(pegawai.no_ktp,'')<>''" +
                    ") z");
            ps.setInt(1, service.getEncounterLookbackDays());
            ps.setInt(2, service.getEncounterLookbackDays());
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (Exception e) {
            System.out.println("Notif antrean encounter : " + e);
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
            try {
                if (ps != null) ps.close();
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
        }
        return 0;
    }

    private int hitungAntreanCondition() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement(
                    "select count(*) as total from (" +
                    "select diagnosa_pasien.no_rawat from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                    "inner join nota_jalan on nota_jalan.no_rawat=reg_periksa.no_rawat inner join satu_sehat_encounter on satu_sehat_encounter.no_rawat=reg_periksa.no_rawat " +
                    "inner join diagnosa_pasien on diagnosa_pasien.no_rawat=reg_periksa.no_rawat left join satu_sehat_condition on satu_sehat_condition.no_rawat=diagnosa_pasien.no_rawat " +
                    "and satu_sehat_condition.kd_penyakit=diagnosa_pasien.kd_penyakit and satu_sehat_condition.status=diagnosa_pasien.status " +
                    "where nota_jalan.tanggal between date_sub(curdate(), interval ? day) and curdate() " +
                    "and ifnull(satu_sehat_encounter.id_encounter,'')<>'' and ifnull(satu_sehat_condition.id_condition,'')='' and ifnull(pasien.no_ktp,'')<>'' " +
                    "union all " +
                    "select diagnosa_pasien.no_rawat from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                    "inner join nota_inap on nota_inap.no_rawat=reg_periksa.no_rawat inner join satu_sehat_encounter on satu_sehat_encounter.no_rawat=reg_periksa.no_rawat " +
                    "inner join diagnosa_pasien on diagnosa_pasien.no_rawat=reg_periksa.no_rawat left join satu_sehat_condition on satu_sehat_condition.no_rawat=diagnosa_pasien.no_rawat " +
                    "and satu_sehat_condition.kd_penyakit=diagnosa_pasien.kd_penyakit and satu_sehat_condition.status=diagnosa_pasien.status " +
                    "where nota_inap.tanggal between date_sub(curdate(), interval ? day) and curdate() " +
                    "and ifnull(satu_sehat_encounter.id_encounter,'')<>'' and ifnull(satu_sehat_condition.id_condition,'')='' and ifnull(pasien.no_ktp,'')<>''" +
                    ") z");
            ps.setInt(1, service.getConditionLookbackDays());
            ps.setInt(2, service.getConditionLookbackDays());
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (Exception e) {
            System.out.println("Notif antrean condition : " + e);
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
            try {
                if (ps != null) ps.close();
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
        }
        return 0;
    }

    private void loadLog() {
        while (tabMode.getRowCount() > 0) {
            tabMode.removeRow(0);
        }

        String tambahan = "";
        if (cmbFilter.getSelectedItem().toString().equals("Gagal")) {
            tambahan = " where status_kirim='GAGAL' ";
        } else if (cmbFilter.getSelectedItem().toString().equals("Sukses")) {
            tambahan = " where status_kirim='SUKSES' ";
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement(
                    "select date_format(dibuat_pada,'%d-%m-%Y %H:%i:%s') as waktu,service_name,resource_type,no_rawat,ifnull(kd_penyakit,'') as kd_penyakit,status_kirim,keterangan " +
                    "from satu_sehat_auto_service_log " + tambahan +
                    " order by dibuat_pada desc limit 200");
            rs = ps.executeQuery();
            while (rs.next()) {
                tabMode.addRow(new Object[]{
                    rs.getString("waktu"),
                    rs.getString("service_name"),
                    rs.getString("resource_type"),
                    rs.getString("no_rawat"),
                    rs.getString("kd_penyakit"),
                    rs.getString("status_kirim"),
                    rs.getString("keterangan")
                });
            }
        } catch (Exception e) {
            System.out.println("Notif load log auto satu sehat : " + e);
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
            try {
                if (ps != null) ps.close();
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
        }
    }
}
