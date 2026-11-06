package bridging;

import fungsi.koneksiDB;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Menandai kunjungan ANC dan menghubungkannya dengan satu episode kehamilan lokal. */
public final class SatuSehatAturANC extends JDialog {
    private final Connection koneksi=koneksiDB.condb();
    private final String noRawat;
    private final String noRm;
    private final String namaPasien;
    private final String namaPoli;
    private long idEpisodeKehamilan=0;
    private boolean sudahDitandai=false;
    private LocalDate tanggalKunjungan;

    private final JTextField txtHpht=new JTextField(12);
    private final JComboBox<String> cmbKunjungan=new JComboBox<>(new String[]{"K1A","K1M","K2","K3","K4","K5","K6"});
    private final JLabel lblSaran=new JLabel("Belum dapat dihitung.");
    private final JLabel lblEpisode=new JLabel("Belum ada episode kehamilan aktif.");
    private final JLabel lblInfo=new JLabel(" ");
    private final JButton btnSimpan=new JButton("Simpan ANC");
    private final JButton btnHapus=new JButton("Batalkan ANC");

    public SatuSehatAturANC(Dialog parent, boolean modal, String noRawat, String noRm,
            String namaPasien, String namaPoli) {
        super(parent,modal);
        this.noRawat=noRawat;
        this.noRm=noRm;
        this.namaPasien=namaPasien;
        this.namaPoli=namaPoli;
        initForm();
        muatData();
    }

    private void initForm() {
        setTitle("Atur Kunjungan ANC SATUSEHAT");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(680,350);
        setLocationRelativeTo(getOwner());

        JPanel data=new JPanel(new GridBagLayout());
        data.setBorder(BorderFactory.createTitledBorder("Episode kehamilan"));
        GridBagConstraints c=new GridBagConstraints();
        c.insets=new Insets(5,7,5,7);
        c.anchor=GridBagConstraints.WEST;
        c.fill=GridBagConstraints.HORIZONTAL;
        tambahBaris(data,c,0,"No. Rawat",new JLabel(noRawat));
        tambahBaris(data,c,1,"Pasien",new JLabel(noRm+" - "+namaPasien));
        tambahBaris(data,c,2,"Poli",new JLabel(namaPoli));
        tambahBaris(data,c,3,"HPHT (yyyy-MM-dd)",txtHpht);
        tambahBaris(data,c,4,"Kunjungan ANC",cmbKunjungan);
        tambahBaris(data,c,5,"Saran otomatis",lblSaran);
        tambahBaris(data,c,6,"Episode",lblEpisode);
        cmbKunjungan.setToolTipText("K1A = K1 akses, K1M = K1 murni, K2-K6 = kunjungan ANC berikutnya");

        JPanel bawah=new JPanel(new BorderLayout(5,5));
        bawah.setBorder(BorderFactory.createEmptyBorder(4,8,8,8));
        bawah.add(lblInfo,BorderLayout.CENTER);
        JPanel tombol=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnTutup=new JButton("Tutup");
        tombol.add(btnSimpan);
        tombol.add(btnHapus);
        tombol.add(btnTutup);
        bawah.add(tombol,BorderLayout.SOUTH);
        add(data,BorderLayout.CENTER);
        add(bawah,BorderLayout.SOUTH);

        btnSimpan.addActionListener(e -> simpan());
        btnHapus.addActionListener(e -> hapusKunjungan());
        btnTutup.addActionListener(e -> dispose());
        txtHpht.addActionListener(e -> pilihSaranKunjungan());
        txtHpht.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                pilihSaranKunjungan();
            }
        });
    }

    private void tambahBaris(JPanel panel, GridBagConstraints c, int baris, String label, java.awt.Component komponen) {
        c.gridy=baris;
        c.gridx=0;
        c.weightx=0;
        panel.add(new JLabel(label),c);
        c.gridx=1;
        c.weightx=1;
        panel.add(komponen,c);
    }

    private void muatData() {
        tanggalKunjungan=cariTanggalKunjungan();
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select k.id_episode_kehamilan,k.status_kunjungan,e.hpht,e.id_episode " +
                "from satu_sehat_kunjungan_anc k inner join satu_sehat_episode_kehamilan e " +
                "on e.id_episode_kehamilan=k.id_episode_kehamilan where k.no_rawat=?")) {
            ps.setString(1,noRawat);
            try (ResultSet rs=ps.executeQuery()) {
                if(rs.next()){
                    sudahDitandai=true;
                    idEpisodeKehamilan=rs.getLong("id_episode_kehamilan");
                    cmbKunjungan.setSelectedItem(rs.getString("status_kunjungan"));
                    txtHpht.setText(rs.getString("hpht"));
                    txtHpht.setEditable(false);
                    lblSaran.setText("Sudah dikonfirmasi: "+rs.getString("status_kunjungan"));
                    tampilkanEpisode(rs.getString("id_episode"));
                }
            }
        } catch (Exception e) {
            lblInfo.setText("Jalankan migrasi_anc_satusehat.sql sebelum memakai menu ini.");
            btnSimpan.setEnabled(false);
            btnHapus.setEnabled(false);
            System.out.println("Notifikasi ANC : "+e);
            return;
        }

        if(!sudahDitandai){
            muatEpisodeAktif();
            if(txtHpht.getText().trim().isEmpty()){
                txtHpht.setText(cariHphtAsesmen());
            }
            pilihSaranKunjungan();
        }
        btnHapus.setEnabled(sudahDitandai);
        String poli=namaPoli.toLowerCase();
        if(!poli.contains("kandungan") && !poli.contains("obstetri") && !poli.contains("kebidanan")){
            lblInfo.setText("Pastikan kunjungan ini benar-benar pelayanan ANC.");
        }else if(!sudahDitandai){
            lblInfo.setText("Konfirmasi HPHT dan kategori kunjungan sebelum menyimpan.");
        }
    }

    private void muatEpisodeAktif() {
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select id_episode_kehamilan,hpht,id_episode from satu_sehat_episode_kehamilan " +
                "where no_rkm_medis=? and status='active' order by diubah_pada desc limit 2")) {
            ps.setString(1,noRm);
            try (ResultSet rs=ps.executeQuery()) {
                if(rs.next()){
                    idEpisodeKehamilan=rs.getLong("id_episode_kehamilan");
                    txtHpht.setText(rs.getString("hpht"));
                    txtHpht.setEditable(false);
                    tampilkanEpisode(rs.getString("id_episode"));
                    if(rs.next()){
                        btnSimpan.setEnabled(false);
                        lblEpisode.setText("Lebih dari satu episode aktif. Tinjau data episode sebelum menyimpan.");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi ANC : "+e);
        }
    }

    private String cariHphtAsesmen() {
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select hpht from penilaian_awal_keperawatan_kebidanan " +
                "where no_rawat=? and hpht is not null order by tanggal desc limit 1")) {
            ps.setString(1,noRawat);
            try (ResultSet rs=ps.executeQuery()) {
                if(rs.next() && rs.getDate("hpht")!=null){
                    return rs.getDate("hpht").toString();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi ANC HPHT rawat jalan : "+e);
        }
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select riwayat_hamil_hpht from penilaian_awal_keperawatan_kebidanan_ranap " +
                "where no_rawat=? order by tanggal desc limit 1")) {
            ps.setString(1,noRawat);
            try (ResultSet rs=ps.executeQuery()) {
                if(rs.next() && rs.getDate("riwayat_hamil_hpht")!=null){
                    return rs.getDate("riwayat_hamil_hpht").toString();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi ANC HPHT rawat inap : "+e);
        }
        return "";
    }

    private void pilihSaranKunjungan() {
        if(sudahDitandai){
            return;
        }
        int ancKe=cariAncKeAsesmen();
        if(ancKe>=2){
            String kode="K"+Math.min(6,ancKe);
            tetapkanSaran(kode,"berdasarkan ANC ke-"+ancKe+" pada asesmen kebidanan");
            return;
        }
        if(ancKe==1){
            pilihSaranKunjunganPertama("berdasarkan ANC ke-1 pada asesmen kebidanan");
            return;
        }

        int urutanTerakhir=idEpisodeKehamilan==0 ? 0 : cariUrutanAncTerakhir();
        if(urutanTerakhir>0){
            String kode="K"+Math.min(6,urutanTerakhir+1);
            tetapkanSaran(kode,"berdasarkan riwayat ANC pada episode kehamilan ini");
            return;
        }
        pilihSaranKunjunganPertama("berdasarkan HPHT dan tanggal kunjungan");
    }

    private LocalDate cariTanggalKunjungan() {
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select tgl_registrasi from reg_periksa where no_rawat=? limit 1")) {
            ps.setString(1,noRawat);
            try (ResultSet rs=ps.executeQuery()) {
                if(rs.next() && rs.getDate("tgl_registrasi")!=null){
                    return rs.getDate("tgl_registrasi").toLocalDate();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi ANC tanggal kunjungan : "+e);
        }
        return null;
    }

    private int cariAncKeAsesmen() {
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select riwayat_hamil_ancke from penilaian_awal_keperawatan_kebidanan_ranap " +
                "where no_rawat=? order by tanggal desc limit 1")) {
            ps.setString(1,noRawat);
            try (ResultSet rs=ps.executeQuery()) {
                if(rs.next()){
                    String nilai=rs.getString("riwayat_hamil_ancke");
                    Matcher angka=Pattern.compile("\\d+").matcher(nilai==null ? "" : nilai);
                    if(angka.find()){
                        int urutan=Integer.parseInt(angka.group());
                        return urutan>=1 && urutan<=6 ? urutan : 0;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi ANC nomor kunjungan asesmen : "+e);
        }
        return 0;
    }

    private int cariUrutanAncTerakhir() {
        int urutan=0;
        try (PreparedStatement ps=koneksi.prepareStatement(
                "select k.status_kunjungan from satu_sehat_kunjungan_anc k join reg_periksa r on r.no_rawat=k.no_rawat where k.id_episode_kehamilan=? and r.tgl_registrasi<=? and k.no_rawat<>?")) {
            ps.setLong(1,idEpisodeKehamilan);
            ps.setString(2,tanggalKunjungan==null?"1900-01-01":tanggalKunjungan.toString());
            ps.setString(3,noRawat);
            try (ResultSet rs=ps.executeQuery()) {
                while(rs.next()){
                    String kode=rs.getString("status_kunjungan");
                    int nilai=(kode.equals("K1A") || kode.equals("K1M")) ? 1 : Integer.parseInt(kode.substring(1));
                    urutan=Math.max(urutan,nilai);
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi ANC : "+e);
        }
        return urutan;
    }

    private void pilihSaranKunjunganPertama(String sumber) {
        try {
            LocalDate hpht=LocalDate.parse(txtHpht.getText().trim(),DateTimeFormatter.ISO_LOCAL_DATE);
            if(tanggalKunjungan==null || hpht.isAfter(tanggalKunjungan)){
                lblSaran.setText("Belum dapat dihitung: tanggal HPHT atau kunjungan tidak valid.");
                return;
            }
            long minggu=ChronoUnit.DAYS.between(hpht,tanggalKunjungan)/7;
            String kode=minggu<=12 ? "K1M" : "K1A";
            tetapkanSaran(kode,sumber+" (usia kehamilan "+minggu+" minggu)");
        } catch (DateTimeParseException e) {
            lblSaran.setText("Isi HPHT untuk menghitung saran K1M atau K1A.");
        }
    }

    private void tetapkanSaran(String kode, String alasan) {
        cmbKunjungan.setSelectedItem(kode);
        lblSaran.setText(kode+" - "+alasan);
    }

    private void tampilkanEpisode(String idEpisode) {
        lblEpisode.setText(idEpisode==null || idEpisode.trim().isEmpty()
                ? "Episode lokal, belum dikirim ke SATUSEHAT"
                : idEpisode);
    }

    private void simpan() {
        LocalDate hpht;
        try {
            hpht=LocalDate.parse(txtHpht.getText().trim(),DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,"HPHT harus berformat yyyy-MM-dd.");
            return;
        }
        if(hpht.isBefore(LocalDate.of(2014,6,3)) || hpht.isAfter(LocalDate.now(java.time.ZoneId.of("Asia/Makassar")))){
            JOptionPane.showMessageDialog(this,"HPHT tidak boleh sebelum 03-06-2014 atau melewati hari ini.");
            return;
        }
        if(tanggalKunjungan!=null && hpht.isAfter(tanggalKunjungan)){
            JOptionPane.showMessageDialog(this,"HPHT tidak boleh melewati tanggal kunjungan.");
            return;
        }
        try{SatuSehatEpisodePayload.dateTime(hpht.toString(),"HPHT / EpisodeOfCare.period.start",java.time.Clock.systemUTC());}
        catch(IllegalArgumentException e){JOptionPane.showMessageDialog(this,e.getMessage());return;}

        if(!fungsi.akses.getsatu_sehat_kirim_encounter())return;
        long committedId=idEpisodeKehamilan;
        try(Connection tx=koneksiDB.bukaKoneksiTransaksi()){
            tx.setAutoCommit(false);
            try {
                // Kunci pasien pada koneksi khusus, bukan koneksi transaksi milik layar lain.
                try(PreparedStatement p=tx.prepareStatement("select no_rkm_medis from pasien where no_rkm_medis=? for update")){
                    p.setString(1,noRm);try(ResultSet r=p.executeQuery()){if(!r.next())throw new Exception("Pasien tidak ditemukan.");}
                }
                try(PreparedStatement p=tx.prepareStatement("select no_rkm_medis from reg_periksa where no_rawat=?")){
                    p.setString(1,noRawat);try(ResultSet r=p.executeQuery()){if(!r.next()||!noRm.equals(r.getString(1)))throw new Exception("Pasien kunjungan berubah.");}
                }
                SatuSehatEpisodeSupport.validateAncCode(cmbKunjungan.getSelectedItem().toString());
                if(committedId==0){
                    try(PreparedStatement p=tx.prepareStatement("select id_episode_kehamilan,hpht from satu_sehat_episode_kehamilan where no_rkm_medis=? and status='active' for update")){
                        p.setString(1,noRm);try(ResultSet r=p.executeQuery()){
                            if(r.next()){
                                if(!hpht.toString().equals(r.getString("hpht")))throw new Exception("Ada episode aktif dengan HPHT berbeda. Tinjau episode sebelumnya.");
                                committedId=r.getLong(1);
                                if(r.next())throw new Exception("Ada lebih dari satu episode kehamilan aktif.");
                            }
                        }
                    }
                    if(committedId==0){
                        String identifier="ANC-"+noRm.replaceAll("[^A-Za-z0-9]","")+"-"+hpht.toString().replace("-","");
                        try(PreparedStatement p=tx.prepareStatement("insert into satu_sehat_episode_kehamilan (no_rkm_medis,hpht,status,no_rawat_awal,identifier_lokal) values (?,?,'active',?,?)",PreparedStatement.RETURN_GENERATED_KEYS)){
                            p.setString(1,noRm);p.setString(2,hpht.toString());p.setString(3,noRawat);p.setString(4,identifier);p.executeUpdate();
                            try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new Exception("ID episode lokal tidak terbentuk.");committedId=r.getLong(1);}
                        }
                    }
                }
                try(PreparedStatement p=tx.prepareStatement("select no_rkm_medis,hpht,status from satu_sehat_episode_kehamilan where id_episode_kehamilan=? for update")){
                    p.setLong(1,committedId);try(ResultSet r=p.executeQuery()){
                        if(!r.next()||!noRm.equals(r.getString(1))||!hpht.toString().equals(r.getString(2)))throw new Exception("Episode/HPHT tidak sesuai. Muat ulang dialog.");
                        if(!sudahDitandai&&!"active".equals(r.getString(3)))throw new Exception("Episode sudah tidak aktif.");
                    }
                }
                try(PreparedStatement p=tx.prepareStatement("select id_episode_kehamilan from satu_sehat_kunjungan_anc where no_rawat=? for update")){
                    p.setString(1,noRawat);try(ResultSet r=p.executeQuery()){if(r.next()&&r.getLong(1)!=committedId)throw new Exception("Kunjungan sudah terhubung dengan episode lain. Muat ulang dialog.");}
                }
                try(PreparedStatement p=tx.prepareStatement("insert into satu_sehat_kunjungan_anc(no_rawat,id_episode_kehamilan,status_kunjungan) values (?,?,?) on duplicate key update status_kunjungan=values(status_kunjungan)")){
                    p.setString(1,noRawat);p.setLong(2,committedId);p.setString(3,cmbKunjungan.getSelectedItem().toString());p.executeUpdate();
                }
                tx.commit();
            }catch(Exception e){tx.rollback();throw e;}
            idEpisodeKehamilan=committedId;sudahDitandai=true;
            btnHapus.setEnabled(true);txtHpht.setEditable(false);
            lblInfo.setText("ANC tersimpan lokal. Kirim/Update Encounter untuk menyinkronkan SATUSEHAT.");
            JOptionPane.showMessageDialog(this,"Kunjungan ANC berhasil disimpan lokal.");
        }catch(Exception e){
            JOptionPane.showMessageDialog(this,"Gagal menyimpan kunjungan ANC: "+e.getMessage());
        }
    }
    private void hapusKunjungan() {
        if(!fungsi.akses.getsatu_sehat_kirim_encounter())return;
        if(JOptionPane.showConfirmDialog(this,"Batalkan penanda ANC untuk kunjungan ini?","Konfirmasi",
                JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION){
            return;
        }
        try (PreparedStatement ps=koneksi.prepareStatement("delete from satu_sehat_kunjungan_anc where no_rawat=?")) {
            ps.setString(1,noRawat);
            ps.executeUpdate();
            btnHapus.setEnabled(false);
            sudahDitandai=false;
            lblInfo.setText("Penanda lokal dibatalkan. Data ANC yang sudah ada di server perlu ditinjau; tidak otomatis dihapus.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,"Gagal membatalkan ANC: "+e.getMessage());
        }
    }
}
