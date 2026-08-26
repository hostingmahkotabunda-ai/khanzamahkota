package rekammedis;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import kepegawaian.DlgCariDokter;

/**
 * Asesmen Awal Medis Bedah / Pra Bedah (RM 22). Diisi dokter bedah sebelum
 * operasi. Identitas pasien &amp; vital sign SEBELUM tindakan ditarik
 * otomatis dari data yg sudah ada (pola sama {@link RMTransferPasienInternal});
 * 9 bagian isian (data objektif s/d instruksi) diisi manual saat itu juga.
 * TTD dokter ditarik dari foto pegawai (dicocokkan lewat NAMA, krn dokter
 * tidak punya nip/nik langsung -- pola sama {@link CetakCPPT#ambilGambarServer}).
 */
public final class RMAsesmenAwalMedisBedah extends JDialog {

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final Map<String, ImageIcon> cacheFotoTtd = new java.util.HashMap<>();
    // Form ini dibuka sebagai dialog modal dari Dokumentasi RM Operasi.
    // Picker juga harus modal agar tidak diblokir dan jatuh di belakang form induk.
    private final DlgCariDokter pickerDokter = new DlgCariDokter(null, true);

    // Header identitas (readonly)
    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TJK = ro();
    private final widget.TextBox TTglLahir = ro();
    private final widget.Tanggal dtpTanggal = dt();

    // Vital sign sebelum tindakan
    private final widget.TextBox tTensi = tf();
    private final widget.TextBox tNadi = tf();
    private final widget.TextBox tRespirasi = tf();
    private final widget.TextBox tSuhu = tf();
    private final widget.TextBox tTinggiBadan = tf();
    private final widget.TextBox tBeratBadan = tf();

    // 9 bagian ceklis (form kertas asli cuma minta tanda centang, bukan isian teks -- lihat PETUNJUK "BERI TANDA (V) PADA KOLOM CHECKLIST")
    private final JCheckBox ckDataObjektif = new JCheckBox("Data Objektif");
    private final JCheckBox ckDataSubjektif = new JCheckBox("Data Subjektif");
    private final JCheckBox ckDiagnosaBedah = new JCheckBox("Diagnosa Bedah");
    private final JCheckBox ckRencanaTindakan = new JCheckBox("Rencana Tindakan");
    private final JCheckBox ckRencanaTindakanAlternatif = new JCheckBox("Rencana Tindakan Alternatif");
    private final JCheckBox ckPerkiraanKehilanganDarah = new JCheckBox("Perkiraan Kehilangan Darah");
    private final JCheckBox ckKomplikasiPenyulit = new JCheckBox("Komplikasi / Penyulit Saat Pembedahan");
    private final JCheckBox ckPrognosis = new JCheckBox("Prognosis");
    private final JCheckBox ckInstruksi = new JCheckBox("Instruksi");

    // Dokter bedah (penandatangan)
    private final widget.TextBox tDokter = ro();
    private final widget.Button btnPilihDokter = new widget.Button();
    private final JLabel lblFotoDokter = new JLabel();
    private String kdDokter = "";

    private final widget.Button BtnBaru = new widget.Button();
    private final widget.Button BtnSimpan = new widget.Button();
    private final widget.Button BtnHapus = new widget.Button();
    private final widget.Button BtnCetak = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();

    public RMAsesmenAwalMedisBedah(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Asesmen Awal Medis Bedah / Pra Bedah (RM 22) ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        ensureTable();
        initComponents();
        siapkanPickerDokter();
        setSize(1080, 800);
        setMinimumSize(new Dimension(900, 620));
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        final Color latar = new Color(246, 249, 251);
        final Color garis = new Color(215, 224, 230);
        final Color teks = new Color(32, 49, 66);

        getContentPane().setBackground(latar);
        getContentPane().setLayout(new BorderLayout());

        JPanel atas = new JPanel(new BorderLayout(12, 10));
        atas.setBackground(latar);
        atas.setBorder(new EmptyBorder(14, 18, 10, 18));

        JPanel barisJudul = new JPanel(new BorderLayout());
        barisJudul.setOpaque(false);
        JPanel blokJudul = new JPanel();
        blokJudul.setOpaque(false);
        blokJudul.setLayout(new BoxLayout(blokJudul, BoxLayout.Y_AXIS));
        JLabel judulUtama = new JLabel("Asesmen Awal Medis Bedah / Pra Bedah");
        judulUtama.setFont(new Font("Tahoma", Font.BOLD, 20));
        judulUtama.setForeground(teks);
        JLabel subjudul = new JLabel("Form RM 22  •  Diisi dokter bedah sebelum tindakan operasi");
        subjudul.setFont(new Font("Tahoma", Font.PLAIN, 12));
        subjudul.setForeground(new Color(92, 107, 119));
        blokJudul.add(judulUtama);
        blokJudul.add(Box.createVerticalStrut(3));
        blokJudul.add(subjudul);
        barisJudul.add(blokJudul, BorderLayout.WEST);
        atas.add(barisJudul, BorderLayout.NORTH);

        JPanel ringkasanPasien = new JPanel(new GridLayout(1, 5, 0, 0));
        ringkasanPasien.setBackground(Color.WHITE);
        ringkasanPasien.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis),
                new EmptyBorder(10, 12, 10, 12)));
        ringkasanPasien.add(fieldRingkasan("No. Rawat *", TNoRw, true));
        ringkasanPasien.add(fieldRingkasan("No. RM", TNoRM, true));
        ringkasanPasien.add(fieldRingkasan("Nama Pasien", TPasien, true));
        ringkasanPasien.add(fieldRingkasan("Jenis Kelamin", TJK, true));
        ringkasanPasien.add(fieldRingkasan("Tanggal Lahir", TTglLahir, true));
        atas.add(ringkasanPasien, BorderLayout.CENTER);

        JLabel wajib = new JLabel("* Pasien wajib dipilih");
        wajib.setForeground(new Color(198, 40, 40));
        wajib.setFont(new Font("Tahoma", Font.PLAIN, 10));
        atas.add(wajib, BorderLayout.SOUTH);
        getContentPane().add(atas, BorderLayout.NORTH);

        JPanel isi = new JPanel();
        isi.setBackground(latar);
        isi.setBorder(new EmptyBorder(4, 18, 18, 18));
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));

        JPanel kartuVital = kartu("Tanggal Pemeriksaan & Vital Sign", teks, garis);
        int row = 0;
        row = pasanganVertikal(kartuVital, row, "Tanggal / Jam", dtpTanggal, "Tensi (mmHg)", tTensi);
        row = pasanganVertikal(kartuVital, row, "Nadi (x/mnt)", tNadi, "Respirasi (x/mnt)", tRespirasi);
        row = pasanganVertikal(kartuVital, row, "Suhu Badan (°C)", tSuhu, "Tinggi Badan (cm)", tTinggiBadan);
        row = tunggalVertikal(kartuVital, row, "Berat Badan (kg)", tBeratBadan);
        isi.add(kartuVital);
        isi.add(Box.createVerticalStrut(10));

        JPanel kartuIsian = kartu("Asesmen (Beri Tanda Centang)", teks, garis);
        JPanel panelChecklist = new JPanel(new GridLayout(9, 1, 0, 4));
        panelChecklist.setOpaque(false);
        for (JCheckBox ck : new JCheckBox[]{ckDataObjektif, ckDataSubjektif, ckDiagnosaBedah, ckRencanaTindakan,
            ckRencanaTindakanAlternatif, ckPerkiraanKehilanganDarah, ckKomplikasiPenyulit, ckPrognosis, ckInstruksi}) {
            ck.setOpaque(false);
            ck.setFont(new Font("Tahoma", Font.PLAIN, 12));
            panelChecklist.add(ck);
        }
        GridBagConstraints gChk = gc(0, 1, 4, 1.0);
        gChk.insets = new Insets(1, 4, 8, 4);
        kartuIsian.add(panelChecklist, gChk);
        isi.add(kartuIsian);
        isi.add(Box.createVerticalStrut(10));

        JPanel kartuTtd = kartu("Dokter Bedah (Penandatangan)", teks, garis);
        row = 0;
        row = tunggalVertikal(kartuTtd, row, "Dokter Bedah", bungkusPicker(bungkusFotoTtd(tDokter, lblFotoDokter), btnPilihDokter));
        isi.add(kartuTtd);
        isi.add(Box.createVerticalGlue());

        JScrollPane scroll = bungkusScroll(isi);
        getContentPane().add(scroll, BorderLayout.CENTER);

        BtnBaru.setText("Baru");
        BtnSimpan.setText("Simpan Data");
        BtnHapus.setText("Hapus Data");
        BtnCetak.setText("Cetak");
        BtnKeluar.setText("Keluar");
        BtnBaru.addActionListener(e -> setNoRm(ambil(TNoRw)));
        BtnSimpan.addActionListener(e -> simpan());
        BtnHapus.addActionListener(e -> hapus());
        BtnCetak.addActionListener(e -> cetak());
        BtnKeluar.addActionListener(e -> dispose());
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 9));
        bawah.setBackground(Color.WHITE);
        bawah.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, garis));
        bawah.add(BtnHapus);
        bawah.add(BtnBaru);
        bawah.add(BtnCetak);
        bawah.add(BtnKeluar);
        bawah.add(BtnSimpan);
        getContentPane().add(bawah, BorderLayout.SOUTH);
    }

    private void siapkanPickerDokter() {
        btnPilihDokter.setText("...");
        btnPilihDokter.setPreferredSize(new Dimension(32, 25));
        btnPilihDokter.addActionListener(e -> {
            pickerDokter.isCek();
            pickerDokter.setSize(650, 400);
            pickerDokter.setLocationRelativeTo(this);
            pickerDokter.setVisible(true);
        });
        pickerDokter.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (pickerDokter.getTable().getSelectedRow() != -1) {
                    kdDokter = pickerDokter.getTable().getValueAt(pickerDokter.getTable().getSelectedRow(), 0).toString();
                    String namaDokter = pickerDokter.getTable().getValueAt(pickerDokter.getTable().getSelectedRow(), 1).toString();
                    tDokter.setText(kdDokter + " - " + namaDokter);
                    lblFotoDokter.setIcon(ambilFotoTtdDokter(namaDokter));
                }
            }
        });
    }

    public void isCek() {
        boolean bisa = akses.getpenilaian_awal_keperawatan_ranap();
        BtnSimpan.setEnabled(bisa);
        BtnHapus.setEnabled(bisa);
    }

    public void emptTeks() {
        for (widget.TextBox t : new widget.TextBox[]{TNoRw, TNoRM, TPasien, TJK, TTglLahir,
            tTensi, tNadi, tRespirasi, tSuhu, tTinggiBadan, tBeratBadan, tDokter}) {
            t.setText("");
        }
        for (JCheckBox ck : new JCheckBox[]{ckDataObjektif, ckDataSubjektif, ckDiagnosaBedah, ckRencanaTindakan,
            ckRencanaTindakanAlternatif, ckPerkiraanKehilanganDarah, ckKomplikasiPenyulit, ckPrognosis, ckInstruksi}) {
            ck.setSelected(false);
        }
        dtpTanggal.setDate(new Date());
        lblFotoDokter.setIcon(null);
        kdDokter = "";
    }

    /** Dipanggil dari menu "Penilaian Awal". Tarik data yang sudah ada lalu timpa dengan data tersimpan bila ada. */
    public void setNoRm(String norawat) {
        emptTeks();
        if (norawat == null || norawat.trim().equals("")) {
            return;
        }
        TNoRw.setText(norawat);
        tarikDataPasien(norawat);
        muatDataJikaAda(norawat);
    }

    private void tarikDataPasien(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select p.no_rkm_medis,p.nm_pasien,p.jk,ifnull(date_format(p.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir "
                + "from reg_periksa rp inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis "
                + "where rp.no_rawat=?")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    TJK.setText("L".equalsIgnoreCase(rs.getString("jk")) ? "Laki-Laki" : "Perempuan");
                    TTglLahir.setText(rs.getString("tgl_lahir"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik data pasien asesmen bedah : " + e);
        }

        try (PreparedStatement ps = koneksi.prepareStatement(
                "select tensi,nadi,respirasi,suhu_tubuh,tinggi,berat "
                + "from pemeriksaan_ranap where no_rawat=? order by tgl_perawatan desc,jam_rawat desc limit 1")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tTensi.setText(nvl(rs.getString("tensi")));
                    tNadi.setText(nvl(rs.getString("nadi")));
                    tRespirasi.setText(nvl(rs.getString("respirasi")));
                    tSuhu.setText(nvl(rs.getString("suhu_tubuh")));
                    tTinggiBadan.setText(nvl(rs.getString("tinggi")));
                    tBeratBadan.setText(nvl(rs.getString("berat")));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik vital asesmen bedah : " + e);
        }

        dtpTanggal.setDate(new Date());
    }

    private void muatDataJikaAda(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select * from asesmen_awal_medis_bedah where no_rawat=?")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getDate("tanggal") != null) {
                        isiTanggalJam(dtpTanggal, rs.getDate("tanggal"), rs.getString("jam"));
                    }
                    if (!nvl(rs.getString("tensi")).equals("")) { tTensi.setText(rs.getString("tensi")); }
                    if (!nvl(rs.getString("nadi")).equals("")) { tNadi.setText(rs.getString("nadi")); }
                    if (!nvl(rs.getString("respirasi")).equals("")) { tRespirasi.setText(rs.getString("respirasi")); }
                    if (!nvl(rs.getString("suhu")).equals("")) { tSuhu.setText(rs.getString("suhu")); }
                    if (!nvl(rs.getString("tinggi_badan")).equals("")) { tTinggiBadan.setText(rs.getString("tinggi_badan")); }
                    if (!nvl(rs.getString("berat_badan")).equals("")) { tBeratBadan.setText(rs.getString("berat_badan")); }
                    ckDataObjektif.setSelected("1".equals(rs.getString("data_objektif")));
                    ckDataSubjektif.setSelected("1".equals(rs.getString("data_subjektif")));
                    ckDiagnosaBedah.setSelected("1".equals(rs.getString("diagnosa_bedah")));
                    ckRencanaTindakan.setSelected("1".equals(rs.getString("rencana_tindakan")));
                    ckRencanaTindakanAlternatif.setSelected("1".equals(rs.getString("rencana_tindakan_alternatif")));
                    ckPerkiraanKehilanganDarah.setSelected("1".equals(rs.getString("perkiraan_kehilangan_darah")));
                    ckKomplikasiPenyulit.setSelected("1".equals(rs.getString("komplikasi_penyulit")));
                    ckPrognosis.setSelected("1".equals(rs.getString("prognosis")));
                    ckInstruksi.setSelected("1".equals(rs.getString("instruksi")));
                    if (!nvl(rs.getString("kd_dokter")).equals("")) {
                        kdDokter = rs.getString("kd_dokter");
                        String namaDokter = nvl(rs.getString("nama_dokter"));
                        tDokter.setText(kdDokter + " - " + namaDokter);
                        lblFotoDokter.setIcon(ambilFotoTtdDokter(namaDokter));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat asesmen bedah : " + e);
        }
    }

    private void isiTanggalJam(widget.Tanggal komponen, java.sql.Date tgl, String jam) {
        if (tgl == null) { return; }
        String j = nvl(jam);
        try {
            komponen.setDate(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .parse(tgl + " " + (j.equals("") ? "00:00:00" : j)));
        } catch (Exception ignore) { }
    }

    private void simpan() {
        if (ambil(TNoRw).equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into asesmen_awal_medis_bedah (no_rawat,tanggal,jam,tensi,nadi,respirasi,suhu,tinggi_badan,berat_badan,"
                + "data_objektif,data_subjektif,diagnosa_bedah,rencana_tindakan,rencana_tindakan_alternatif,"
                + "perkiraan_kehilangan_darah,komplikasi_penyulit,prognosis,instruksi,kd_dokter,nama_dokter,"
                + "updated_by,updated_at,created_by,created_at) "
                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,now(),?,now()) "
                + "on duplicate key update tanggal=values(tanggal),jam=values(jam),tensi=values(tensi),nadi=values(nadi),"
                + "respirasi=values(respirasi),suhu=values(suhu),tinggi_badan=values(tinggi_badan),berat_badan=values(berat_badan),"
                + "data_objektif=values(data_objektif),data_subjektif=values(data_subjektif),diagnosa_bedah=values(diagnosa_bedah),"
                + "rencana_tindakan=values(rencana_tindakan),rencana_tindakan_alternatif=values(rencana_tindakan_alternatif),"
                + "perkiraan_kehilangan_darah=values(perkiraan_kehilangan_darah),komplikasi_penyulit=values(komplikasi_penyulit),"
                + "prognosis=values(prognosis),instruksi=values(instruksi),kd_dokter=values(kd_dokter),nama_dokter=values(nama_dokter),"
                + "updated_by=values(updated_by),updated_at=now()")) {
            int i = 1;
            ps.setString(i++, ambil(TNoRw));
            setTglJam(ps, i, dtpTanggal); i += 2;
            ps.setString(i++, ambil(tTensi));
            ps.setString(i++, ambil(tNadi));
            ps.setString(i++, ambil(tRespirasi));
            ps.setString(i++, ambil(tSuhu));
            ps.setString(i++, ambil(tTinggiBadan));
            ps.setString(i++, ambil(tBeratBadan));
            ps.setString(i++, ckDataObjektif.isSelected() ? "1" : "0");
            ps.setString(i++, ckDataSubjektif.isSelected() ? "1" : "0");
            ps.setString(i++, ckDiagnosaBedah.isSelected() ? "1" : "0");
            ps.setString(i++, ckRencanaTindakan.isSelected() ? "1" : "0");
            ps.setString(i++, ckRencanaTindakanAlternatif.isSelected() ? "1" : "0");
            ps.setString(i++, ckPerkiraanKehilanganDarah.isSelected() ? "1" : "0");
            ps.setString(i++, ckKomplikasiPenyulit.isSelected() ? "1" : "0");
            ps.setString(i++, ckPrognosis.isSelected() ? "1" : "0");
            ps.setString(i++, ckInstruksi.isSelected() ? "1" : "0");
            ps.setString(i++, kdDokter);
            ps.setString(i++, ambilNamaDokterDariTeks());
            ps.setString(i++, akses.getkode());
            ps.setString(i++, akses.getkode());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Asesmen awal medis bedah tersimpan.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan.\n" + e.getMessage());
        }
    }

    private String ambilNamaDokterDariTeks() {
        String t = ambil(tDokter);
        int idx = t.indexOf(" - ");
        return idx < 0 ? t : t.substring(idx + 3).trim();
    }

    private void setTglJam(PreparedStatement ps, int idx, widget.Tanggal komponen) throws Exception {
        Date d = komponen.getDate();
        if (d == null) {
            ps.setNull(idx, java.sql.Types.DATE);
            ps.setString(idx + 1, "");
        } else {
            ps.setString(idx, new java.text.SimpleDateFormat("yyyy-MM-dd").format(d));
            ps.setString(idx + 1, new java.text.SimpleDateFormat("HH:mm:ss").format(d));
        }
    }

    private void hapus() {
        if (ambil(TNoRw).equals("")) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus asesmen awal medis bedah untuk No.Rawat " + ambil(TNoRw) + " ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement("delete from asesmen_awal_medis_bedah where no_rawat=?")) {
            ps.setString(1, ambil(TNoRw));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data dihapus.");
            String norw = ambil(TNoRw);
            setNoRm(norw);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus.\n" + e.getMessage());
        }
    }

    /** Cetak Jasper RM 22 -- foto TTD dokter dicocokkan by nama di dalam query cetak (pola sama CetakCPPT). */
    public void cetak() {
        if (ambil(TNoRw).equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        if (Sequel.cariInteger("select count(*) from asesmen_awal_medis_bedah where no_rawat=?", ambil(TNoRw)) == 0) {
            JOptionPane.showMessageDialog(this, "Simpan data terlebih dahulu sebelum mencetak.");
            return;
        }
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            param.put("url_penggajian", "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/"
                    + koneksiDB.HYBRIDWEB() + "/penggajian/");
            String sql = "select pasien.no_rkm_medis,pasien.nm_pasien,"
                    + "if(pasien.jk='L','Laki-laki','Perempuan') as jk,"
                    + "ifnull(date_format(pasien.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir,"
                    + "ifnull(date_format(a.tanggal,'%d-%m-%Y'),'') as tanggal,ifnull(a.jam,'') as jam,"
                    + "a.tensi,a.nadi,a.respirasi,a.suhu,a.tinggi_badan,a.berat_badan,"
                    + "a.data_objektif,a.data_subjektif,a.diagnosa_bedah,a.rencana_tindakan,"
                    + "a.rencana_tindakan_alternatif,a.perkiraan_kehilangan_darah,a.komplikasi_penyulit,"
                    + "a.prognosis,a.instruksi,ifnull(a.nama_dokter,'') as nama_dokter,"
                    + "if(coalesce(nullif((select p2.photo from pegawai p2 where lower(trim(p2.nama))=lower(trim(a.nama_dokter)) limit 1),''),'')='' "
                    + "or coalesce(nullif((select p2.photo from pegawai p2 where lower(trim(p2.nama))=lower(trim(a.nama_dokter)) limit 1),''),'')='-' "
                    + "or coalesce(nullif((select p2.photo from pegawai p2 where lower(trim(p2.nama))=lower(trim(a.nama_dokter)) limit 1),''),'')='pages/pegawai/photo/',"
                    + "'',replace(coalesce((select p2.photo from pegawai p2 where lower(trim(p2.nama))=lower(trim(a.nama_dokter)) limit 1),''),'\\\\\\\\','/')) as dokter_photo "
                    + "from asesmen_awal_medis_bedah a "
                    + "inner join reg_periksa on a.no_rawat=reg_periksa.no_rawat "
                    + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "where a.no_rawat='" + ambil(TNoRw) + "'";
            Valid.MyReportqry("rptAsesmenAwalMedisBedah.jasper", "report", "::[ Asesmen Awal Medis Bedah / Pra Bedah ]::", sql, param);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak.\n" + e.getMessage());
        }
    }

    private void ensureTable() {
        Sequel.queryu2(
                "create table if not exists asesmen_awal_medis_bedah ("
                + "no_rawat varchar(17) not null primary key,"
                + "tanggal date null,"
                + "jam varchar(8) null,"
                + "tensi varchar(20) null,"
                + "nadi varchar(10) null,"
                + "respirasi varchar(10) null,"
                + "suhu varchar(10) null,"
                + "tinggi_badan varchar(10) null,"
                + "berat_badan varchar(10) null,"
                + "data_objektif varchar(1) null,"
                + "data_subjektif varchar(1) null,"
                + "diagnosa_bedah varchar(1) null,"
                + "rencana_tindakan varchar(1) null,"
                + "rencana_tindakan_alternatif varchar(1) null,"
                + "perkiraan_kehilangan_darah varchar(1) null,"
                + "komplikasi_penyulit varchar(1) null,"
                + "prognosis varchar(1) null,"
                + "instruksi varchar(1) null,"
                + "kd_dokter varchar(20) null,"
                + "nama_dokter varchar(60) null,"
                + "created_by varchar(50) null,"
                + "updated_by varchar(50) null,"
                + "created_at datetime null,"
                + "updated_at datetime null"
                + ")");
    }

    /** Bungkus field readonly + label foto TTD kecil di sebelah kanan. */
    private JPanel bungkusFotoTtd(Component field, JLabel lblFoto) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        p.add(field, BorderLayout.CENTER);
        lblFoto.setPreferredSize(new Dimension(60, 28));
        p.add(lblFoto, BorderLayout.EAST);
        return p;
    }

    /** Bungkus field teks + tombol picker "..." di sebelah kanan. */
    private JPanel bungkusPicker(Component field, Component tombol) {
        JPanel p = new JPanel(new BorderLayout(4, 0));
        p.setOpaque(false);
        p.add(field, BorderLayout.CENTER);
        p.add(tombol, BorderLayout.EAST);
        return p;
    }

    /** Foto TTD dokter -- dokter TIDAK punya nip/nik langsung, dicocokkan lewat NAMA ke pegawai.nama (pola sama CetakCPPT.queryCetak). */
    private ImageIcon ambilFotoTtdDokter(String namaDokter) {
        if (namaDokter == null || namaDokter.trim().isEmpty()) { return null; }
        String key = namaDokter.trim().toLowerCase();
        if (cacheFotoTtd.containsKey(key)) { return cacheFotoTtd.get(key); }
        ImageIcon ic = null;
        try {
            String photo = bersihkanPathFotoTtd(Sequel.cariIsi(
                    "select photo from pegawai where lower(trim(nama))=lower(trim(?)) limit 1", namaDokter));
            if (!photo.isEmpty()) {
                String urlPenggajian = "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/"
                        + koneksiDB.HYBRIDWEB() + "/penggajian/";
                Image gambar = CetakCPPT.ambilGambarServer(urlPenggajian + photo);
                if (gambar != null) {
                    ic = new ImageIcon(gambar.getScaledInstance(-1, 28, Image.SCALE_SMOOTH));
                }
            }
        } catch (Exception ignore) { }
        cacheFotoTtd.put(key, ic);
        return ic;
    }

    private static String bersihkanPathFotoTtd(String photo) {
        if (photo == null) { return ""; }
        String p = photo.trim();
        if (p.equals("") || p.equals("-") || p.equals("pages/pegawai/photo/")) { return ""; }
        return p.replace("\\", "/");
    }

    // ====================== Helpers UI (pola sama dengan RMTransferPasienInternal) ======================
    private static widget.TextBox tf() { return new widget.TextBox(); }

    private static widget.TextBox ro() {
        widget.TextBox t = new widget.TextBox();
        t.setEditable(false);
        return t;
    }

    private static widget.Tanggal dt() {
        widget.Tanggal d = new widget.Tanggal();
        d.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        return d;
    }

    private JPanel fieldRingkasan(String label, Component komponen, boolean bacaSaja) {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(0, 10, 0, 10));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Tahoma", Font.PLAIN, 10));
        l.setForeground(label.contains("*") ? new Color(190, 35, 35) : new Color(86, 101, 112));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        komponen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        komponen.setPreferredSize(new Dimension(150, 25));
        if (bacaSaja) {
            komponen.setBackground(new Color(248, 250, 251));
        }
        p.add(l);
        p.add(Box.createVerticalStrut(3));
        p.add(komponen);
        return p;
    }

    private JPanel kartu(String judul, Color teks, Color garis) {
        JPanel luar = new JPanel(new GridBagLayout());
        luar.setBackground(Color.WHITE);
        luar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis),
                new EmptyBorder(8, 12, 12, 12)));
        luar.setAlignmentX(Component.LEFT_ALIGNMENT);
        luar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 3000));
        JLabel l = new JLabel(judul);
        l.setFont(new Font("Tahoma", Font.BOLD, 13));
        l.setForeground(teks);
        GridBagConstraints g = gc(0, 0, 4, 1.0);
        g.insets = new Insets(2, 4, 10, 4);
        luar.add(l, g);
        return luar;
    }

    private int pasanganVertikal(JPanel p, int row, String label1, Component komponen1,
            String label2, Component komponen2) {
        int barisLabel = (row * 2) + 1;
        int barisInput = barisLabel + 1;
        p.add(labelAtas(label1), gc(0, barisLabel, 2, 0.5));
        p.add(labelAtas(label2), gc(2, barisLabel, 2, 0.5));
        siapkanInput(komponen1);
        siapkanInput(komponen2);
        GridBagConstraints kiri = gc(0, barisInput, 2, 0.5);
        kiri.insets = new Insets(1, 4, 8, 10);
        GridBagConstraints kanan = gc(2, barisInput, 2, 0.5);
        kanan.insets = new Insets(1, 10, 8, 4);
        p.add(komponen1, kiri);
        p.add(komponen2, kanan);
        return row + 1;
    }

    private int tunggalVertikal(JPanel p, int row, String label, Component komponen) {
        int barisLabel = (row * 2) + 1;
        int barisInput = barisLabel + 1;
        p.add(labelAtas(label), gc(0, barisLabel, 4, 1.0));
        siapkanInput(komponen);
        GridBagConstraints g = gc(0, barisInput, 4, 1.0);
        g.insets = new Insets(1, 4, 8, 4);
        p.add(komponen, g);
        return row + 1;
    }

    private JLabel labelAtas(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font("Tahoma", Font.PLAIN, 11));
        l.setForeground(new Color(49, 64, 75));
        return l;
    }

    private void siapkanInput(Component komponen) {
        komponen.setPreferredSize(new Dimension(320, 30));
        if (komponen instanceof widget.TextBox && !((widget.TextBox) komponen).isEditable()) {
            komponen.setBackground(new Color(245, 248, 249));
        }
    }

    private JScrollPane bungkusScroll(JPanel halaman) {
        JScrollPane scroll = new JScrollPane(halaman);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.getViewport().setBackground(new Color(246, 249, 251));
        return scroll;
    }

    private GridBagConstraints gc(int x, int y, int w, double wx) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x; g.gridy = y; g.gridwidth = w; g.weightx = wx;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(2, 4, 2, 4);
        return g;
    }

    private static String ambil(widget.TextBox t) {
        return t.getText() == null ? "" : t.getText().trim();
    }

    private static String nvl(String v) {
        return v == null ? "" : v;
    }
}
