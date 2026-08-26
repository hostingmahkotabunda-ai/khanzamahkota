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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.sf.jasperreports.engine.JasperCompileManager;

/**
 * Panel "Surat Keterangan Lahir" yang di-embed sebagai tab di DlgRawatInap (khusus RM bayi).
 * HANYA lapisan data (tidak ada kop/desain/sidik kaki/TTD -- itu semua fisik, sudah ada di
 * kertas blanko yang dicetak duluan; cetak dari sini tinggal menumpuk data di atas kertas itu).
 *
 * Sebagian field auto-tarik dari tabel `pasien_bayi` (data kelahiran bawaan Khanza) + `pasien`
 * (bayi & ibu, ibu dicari lewat `ranap_gabung`) begitu tab ini dibuka pertama kali -- field
 * yang tidak ada sumbernya (Pekerjaan Ayah, Dokter Anak, Keterangan Lain, 2 foto) diisi manual.
 * SEMUA field (termasuk yang auto-tarik) tetap bisa diedit manual oleh petugas. Kalau sudah
 * pernah disimpan sebelumnya, data yang tersimpan itu yang dimuat (bukan tarik ulang).
 */
public final class RMSuratKeteranganLahir extends JPanel {

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();

    private String noRawat = "";
    private byte[] dataFoto1 = null;
    private byte[] dataFoto2 = null;

    private final widget.TextBox TNoSKL = new widget.TextBox();
    private final widget.TextBox TNamaBayi = new widget.TextBox();
    private final widget.Tanggal dtpTglLahir = dt();
    private final widget.TextBox TJamLahir = new widget.TextBox();
    private final widget.TextBox TTempatLahir = new widget.TextBox();
    private final widget.TextBox TNamaAyah = new widget.TextBox();
    private final widget.TextBox TNamaIbu = new widget.TextBox();
    private final widget.TextBox TAlamatOrtu = new widget.TextBox();
    private final widget.TextBox TPekerjaanAyah = new widget.TextBox();
    private final widget.TextBox TPekerjaanIbu = new widget.TextBox();
    private final widget.TextBox TDokterPenolong = new widget.TextBox();
    private final widget.TextBox TDokterAnak = new widget.TextBox();
    private final widget.ComboBox cmbJK = comboJK();
    private final widget.TextBox TBeratBadan = new widget.TextBox();
    private final widget.TextBox TPanjangBadan = new widget.TextBox();
    private final widget.TextBox TLingkarKepala = new widget.TextBox();
    private final widget.TextBox TLingkarDada = new widget.TextBox();
    private final widget.TextBox TLingkarPerut = new widget.TextBox();
    private final widget.TextArea taKeteranganLain = ta();

    private final JLabel lblFoto1 = new JLabel("(belum ada)");
    private final JLabel lblFoto2 = new JLabel("(belum ada)");

    private final widget.Button BtnSimpan = new widget.Button();
    private final widget.Button BtnCetak = new widget.Button();

    public RMSuratKeteranganLahir() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        int row = 0;

        row = baris1(form, row, "No. SKL", TNoSKL);
        row = baris1(form, row, "Nama Bayi", TNamaBayi);
        row = barisTglJam(form, row);
        row = baris1(form, row, "Tempat Lahir", TTempatLahir);
        row = baris2(form, row, "Nama Ayah", TNamaAyah, "Nama Ibu", TNamaIbu);
        row = baris1(form, row, "Alamat Orang Tua", TAlamatOrtu);
        row = baris2(form, row, "Pekerjaan Ayah", TPekerjaanAyah, "Pekerjaan Ibu", TPekerjaanIbu);
        row = barisDokter(form, row, "Dokter Penolong", TDokterPenolong, "Dokter Anak", TDokterAnak);
        row = judul(form, row, "Identitas Bayi");
        row = barisKombo(form, row, "Jenis Kelamin", cmbJK);
        row = baris2(form, row, "Berat Badan (gram)", TBeratBadan, "Panjang Badan (cm)", TPanjangBadan);
        row = baris3(form, row, "Lingkar Kepala (cm)", TLingkarKepala, "Lingkar Dada (cm)", TLingkarDada, "Lingkar Perut (cm)", TLingkarPerut);
        row = area(form, row, "Keterangan Lain", taKeteranganLain);
        row = barisFoto(form, row);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        BtnSimpan.setText("Simpan");
        BtnSimpan.addActionListener(e -> simpan());
        BtnCetak.setText("Cetak");
        BtnCetak.setToolTipText("Cetak lapisan data di atas kertas blanko Surat Keterangan Lahir (kop/label/TTD/sidik kaki sudah fisik di kertas)");
        BtnCetak.addActionListener(e -> cetak());
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        bawah.setOpaque(false);
        bawah.add(BtnCetak);
        bawah.add(BtnSimpan);
        add(bawah, BorderLayout.SOUTH);
    }

    /**
     * Dipanggil setiap kali tab ini jadi aktif/dipilih. Terima no_rawat APA PUN yang lagi
     * dibuka di DlgRawatInap (bisa punya bayi sendiri, ATAU punya ibu yang RM-nya sudah
     * digabung ke bayi lewat ranap_gabung) -- di-resolve dulu ke no_rawat BAYI yang
     * sebenarnya sebelum tarik/simpan data, supaya SKL selalu tentang bayi, konsisten
     * dibuka dari halaman ibu maupun langsung dari halaman bayi.
     */
    public void setKonteks(String norwtDibuka) {
        if (norwtDibuka == null) { norwtDibuka = ""; }
        String norwtBayi = norwtDibuka.trim().isEmpty() ? "" : resolveNoRawatBayi(norwtDibuka.trim());
        if (norwtBayi.equals(noRawat)) { return; }
        noRawat = norwtBayi;
        kosongkan();
        if (noRawat.trim().isEmpty()) { return; }
        ensureTable();
        if (!muatTersimpan(noRawat)) {
            tarikOtomatis(noRawat);
        }
    }

    /** Kalau norwtDibuka itu no_rawat IBU yg sudah digabung, balikin no_rawat BAYI-nya lewat ranap_gabung. */
    private String resolveNoRawatBayi(String norwtDibuka) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select no_rawat2 from ranap_gabung where no_rawat=?")) {
            ps.setString(1, norwtDibuka);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("no_rawat2");
                }
            }
        } catch (Exception e) {
            System.out.println("Notif resolve no_rawat bayi SKL : " + e);
        }
        return norwtDibuka;
    }

    private void kosongkan() {
        TNoSKL.setText(""); TNamaBayi.setText(""); TJamLahir.setText(""); TTempatLahir.setText("");
        TNamaAyah.setText(""); TNamaIbu.setText(""); TAlamatOrtu.setText("");
        TPekerjaanAyah.setText(""); TPekerjaanIbu.setText(""); TDokterPenolong.setText(""); TDokterAnak.setText("");
        cmbJK.setSelectedIndex(0);
        TBeratBadan.setText(""); TPanjangBadan.setText("");
        TLingkarKepala.setText(""); TLingkarDada.setText(""); TLingkarPerut.setText("");
        taKeteranganLain.setText("");
        dtpTglLahir.setDate(new Date());
        dataFoto1 = null; dataFoto2 = null;
        lblFoto1.setText("(belum ada)"); lblFoto2.setText("(belum ada)");
    }

    /** true kalau ada data tersimpan sebelumnya (dimuat), false kalau belum pernah disimpan. */
    private boolean muatTersimpan(String norwt) {
        try (PreparedStatement ps = koneksi.prepareStatement("select * from surat_keterangan_lahir where no_rawat=?")) {
            ps.setString(1, norwt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TNoSKL.setText(g(rs, "no_skl"));
                    TNamaBayi.setText(g(rs, "nama_bayi"));
                    setTgl(dtpTglLahir, rs.getDate("tgl_lahir"));
                    TJamLahir.setText(g(rs, "jam_lahir"));
                    TTempatLahir.setText(g(rs, "tempat_lahir"));
                    TNamaAyah.setText(g(rs, "nama_ayah"));
                    TNamaIbu.setText(g(rs, "nama_ibu"));
                    TAlamatOrtu.setText(g(rs, "alamat_orang_tua"));
                    TPekerjaanAyah.setText(g(rs, "pekerjaan_ayah"));
                    TPekerjaanIbu.setText(g(rs, "pekerjaan_ibu"));
                    TDokterPenolong.setText(g(rs, "dokter_penolong"));
                    TDokterAnak.setText(g(rs, "dokter_anak"));
                    String jk = g(rs, "jk");
                    cmbJK.setSelectedItem(jk.isEmpty() ? "" : jk);
                    TBeratBadan.setText(g(rs, "berat_badan"));
                    TPanjangBadan.setText(g(rs, "panjang_badan"));
                    TLingkarKepala.setText(g(rs, "lingkar_kepala"));
                    TLingkarDada.setText(g(rs, "lingkar_dada"));
                    TLingkarPerut.setText(g(rs, "lingkar_perut"));
                    taKeteranganLain.setText(g(rs, "keterangan_lain"));
                    dataFoto1 = rs.getBytes("foto1");
                    dataFoto2 = rs.getBytes("foto2");
                    lblFoto1.setText(dataFoto1 == null ? "(belum ada)" : "(tersimpan)");
                    lblFoto2.setText(dataFoto2 == null ? "(belum ada)" : "(tersimpan)");
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat surat keterangan lahir : " + e);
        }
        return false;
    }

    /** Tarik data dari pasien_bayi + pasien (bayi & ibu lewat ranap_gabung) -- HANYA saat belum pernah disimpan. */
    private void tarikOtomatis(String norwt) {
        String noRkmBayi = "";
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select pasien.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.tgl_lahir "
                + "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                + "where reg_periksa.no_rawat=?")) {
            ps.setString(1, norwt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    noRkmBayi = nvl(rs.getString("no_rkm_medis"));
                    TNamaBayi.setText(nvl(rs.getString("nm_pasien")));
                    String jk = nvl(rs.getString("jk"));
                    cmbJK.setSelectedItem(jk.equalsIgnoreCase("L") ? "LAKI-LAKI" : jk.equalsIgnoreCase("P") ? "PEREMPUAN" : "");
                    setTgl(dtpTglLahir, rs.getDate("tgl_lahir"));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik identitas bayi SKL : " + e);
        }

        TTempatLahir.setText(Sequel.cariIsi("select nama_instansi from setting"));

        if (!noRkmBayi.isEmpty()) {
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "select pasien_bayi.no_skl,pasien_bayi.berat_badan,pasien_bayi.panjang_badan,"
                    + "pasien_bayi.lingkar_kepala,pasien_bayi.lingkar_dada,pasien_bayi.lingkar_perut,"
                    + "pasien_bayi.jam_lahir,pasien_bayi.nama_ayah,ifnull(pegawai.nama,'') as nm_penolong "
                    + "from pasien_bayi left join pegawai on pasien_bayi.penolong=pegawai.nik "
                    + "where pasien_bayi.no_rkm_medis=?")) {
                ps.setString(1, noRkmBayi);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        TNoSKL.setText(nvl(rs.getString("no_skl")));
                        TBeratBadan.setText(nvl(rs.getString("berat_badan")));
                        TPanjangBadan.setText(nvl(rs.getString("panjang_badan")));
                        TLingkarKepala.setText(nvl(rs.getString("lingkar_kepala")));
                        TLingkarDada.setText(nvl(rs.getString("lingkar_dada")));
                        TLingkarPerut.setText(nvl(rs.getString("lingkar_perut")));
                        TJamLahir.setText(nvl(rs.getString("jam_lahir")));
                        TNamaAyah.setText(nvl(rs.getString("nama_ayah")));
                        TDokterPenolong.setText(nvl(rs.getString("nm_penolong")));
                    }
                }
            } catch (Exception e) {
                System.out.println("Notif tarik pasien_bayi SKL : " + e);
            }
        }

        // Ibu dicari lewat ranap_gabung: no_rawat (ibu) -> no_rawat2 (bayi, = norwt di sini).
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select pasien.nm_pasien,pasien.alamat,pasien.pekerjaan "
                + "from ranap_gabung inner join reg_periksa on reg_periksa.no_rawat=ranap_gabung.no_rawat "
                + "inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "
                + "where ranap_gabung.no_rawat2=?")) {
            ps.setString(1, norwt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TNamaIbu.setText(nvl(rs.getString("nm_pasien")));
                    TAlamatOrtu.setText(nvl(rs.getString("alamat")));
                    TPekerjaanIbu.setText(nvl(rs.getString("pekerjaan")));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik ibu (ranap_gabung) SKL : " + e);
        }
    }

    private void simpan() {
        if (noRawat.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "insert into surat_keterangan_lahir "
                + "(no_rawat,no_rkm_medis,no_skl,nama_bayi,tgl_lahir,jam_lahir,tempat_lahir,nama_ayah,nama_ibu,"
                + "alamat_orang_tua,pekerjaan_ayah,pekerjaan_ibu,dokter_penolong,dokter_anak,jk,berat_badan,"
                + "panjang_badan,lingkar_kepala,lingkar_dada,lingkar_perut,keterangan_lain,foto1,foto2,"
                + "created_by,created_at,updated_at) values "
                + "(?,(select no_rkm_medis from reg_periksa where no_rawat=?),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
                + "?,now(),now()) "
                + "on duplicate key update no_rkm_medis=values(no_rkm_medis),no_skl=values(no_skl),"
                + "nama_bayi=values(nama_bayi),tgl_lahir=values(tgl_lahir),jam_lahir=values(jam_lahir),"
                + "tempat_lahir=values(tempat_lahir),nama_ayah=values(nama_ayah),nama_ibu=values(nama_ibu),"
                + "alamat_orang_tua=values(alamat_orang_tua),pekerjaan_ayah=values(pekerjaan_ayah),"
                + "pekerjaan_ibu=values(pekerjaan_ibu),dokter_penolong=values(dokter_penolong),"
                + "dokter_anak=values(dokter_anak),jk=values(jk),berat_badan=values(berat_badan),"
                + "panjang_badan=values(panjang_badan),lingkar_kepala=values(lingkar_kepala),"
                + "lingkar_dada=values(lingkar_dada),lingkar_perut=values(lingkar_perut),"
                + "keterangan_lain=values(keterangan_lain),foto1=values(foto1),foto2=values(foto2),"
                + "created_by=values(created_by),updated_at=now()")) {
            ps.setString(1, noRawat);
            ps.setString(2, noRawat);
            ps.setString(3, TNoSKL.getText());
            ps.setString(4, TNamaBayi.getText());
            ps.setString(5, ambilTgl(dtpTglLahir));
            ps.setString(6, TJamLahir.getText());
            ps.setString(7, TTempatLahir.getText());
            ps.setString(8, TNamaAyah.getText());
            ps.setString(9, TNamaIbu.getText());
            ps.setString(10, TAlamatOrtu.getText());
            ps.setString(11, TPekerjaanAyah.getText());
            ps.setString(12, TPekerjaanIbu.getText());
            ps.setString(13, TDokterPenolong.getText());
            ps.setString(14, TDokterAnak.getText());
            ps.setString(15, s(cmbJK));
            ps.setString(16, TBeratBadan.getText());
            ps.setString(17, TPanjangBadan.getText());
            ps.setString(18, TLingkarKepala.getText());
            ps.setString(19, TLingkarDada.getText());
            ps.setString(20, TLingkarPerut.getText());
            ps.setString(21, taKeteranganLain.getText());
            ps.setBytes(22, dataFoto1);
            ps.setBytes(23, dataFoto2);
            ps.setString(24, akses.getkode());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Surat Keterangan Lahir tersimpan.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan.\n" + e.getMessage());
        }
    }

    /**
     * Cetak label + nilai (lihat catatan di rptSuratKeteranganLahir.jrxml) --
     * kop/TTD/sidik kaki tetap fisik di kertas blanko F4, tapi semua label
     * ("Tempat :", "Nama Orang Tua", dst.) dan nilainya dicetak dari sini,
     * mengikuti referensi format.pdf dari user. Posisi tiap field masih
     * ESTIMASI, BELUM dikalibrasi ke kertas blanko fisik asli. Jangan
     * janjikan hasil pas 100% sebelum ada cetak-coba nyata di kertas tersebut.
     */
    private void cetak() {
        if (noRawat.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        try {
            siapkanReport("rptSuratKeteranganLahir");
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("no_skl", bersihkanNoSkl(TNoSKL.getText()));
            param.put("nama_bayi", TNamaBayi.getText());
            Date tglLahir = dtpTglLahir.getDate();
            String jam = TJamLahir.getText();
            String tglJam = formatTanggalHari(tglLahir);
            if (jam != null && !jam.trim().isEmpty()) {
                tglJam = tglJam + " Jam " + jam.trim() + " Wita";
            }
            param.put("tgl_jam_teks", tglJam);
            param.put("tempat_lahir", TTempatLahir.getText());
            param.put("nama_ayah", TNamaAyah.getText());
            param.put("nama_ibu", TNamaIbu.getText());
            param.put("alamat_orang_tua", TAlamatOrtu.getText());
            param.put("pekerjaan_ayah", TPekerjaanAyah.getText());
            param.put("pekerjaan_ibu", TPekerjaanIbu.getText());
            param.put("dokter_penolong", TDokterPenolong.getText());
            param.put("dokter_anak", TDokterAnak.getText());
            param.put("jk", s(cmbJK));
            param.put("berat_badan", satuan(TBeratBadan.getText(), "Gram"));
            param.put("panjang_badan", satuan(TPanjangBadan.getText(), "Cm"));
            param.put("lingkar_kepala", satuan(TLingkarKepala.getText(), "Cm"));
            param.put("lingkar_dada", satuan(TLingkarDada.getText(), "Cm"));
            param.put("lingkar_perut", satuan(TLingkarPerut.getText(), "Cm"));
            param.put("keterangan_lain", taKeteranganLain.getText());
            param.put("tanggal_surat", akses.getkabupatenrs() + ", " + formatTanggalAngka(tglLahir));
            // Normalisasi juga saat cetak agar foto lama yang sudah tersimpan
            // dengan rasio mendatar tetap dicetak dalam kotak potret.
            param.put("foto1", dataFoto1 == null ? null : new ByteArrayInputStream(seragamkanUkuranFoto(dataFoto1)));
            param.put("foto2", dataFoto2 == null ? null : new ByteArrayInputStream(seragamkanUkuranFoto(dataFoto2)));
            siapkanReport("rptSuratKeteranganLahir");
            Valid.MyReport("rptSuratKeteranganLahir.jasper", "report", "Surat Keterangan Lahir", param);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal cetak.\n" + e.getMessage());
        }
    }

    /**
     * Cetak selalu nambahin prefix "NO : " sendiri di jrxml -- kalau petugas
     * kebetulan ikut ngetik "NO :" di field No. SKL, hasilnya dobel ("NO : NO :
     * ..."). Buang prefix "no"/"no." (dgn optional titik dua) di depan biar aman.
     */
    private static String bersihkanNoSkl(String v) {
        String s = v == null ? "" : v.trim();
        return s.replaceFirst("(?i)^no\\.?\\s*:?\\s*", "");
    }

    private static String satuan(String angka, String satuan) {
        String a = angka == null ? "" : angka.trim();
        return a.isEmpty() ? "" : a + " " + satuan;
    }

    private static final String[] HARI_ID = {"Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"};
    private static final String[] BULAN_ID = {"", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"};

    /** "Selasa, 20 Januari 2026" */
    private static String formatTanggalHari(Date d) {
        if (d == null) { return ""; }
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return HARI_ID[c.get(Calendar.DAY_OF_WEEK) - 1] + ", " + formatTanggalAngka(d);
    }

    /** "20 Januari 2026" */
    private static String formatTanggalAngka(Date d) {
        if (d == null) { return ""; }
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c.get(Calendar.DAY_OF_MONTH) + " " + BULAN_ID[c.get(Calendar.MONTH) + 1] + " " + c.get(Calendar.YEAR);
    }

    /** Kompilasi jrxml->jasper bila perlu (pola sama seperti CetakAsesmen.siapkanReport). */
    private static String siapkanReport(String baseName) throws Exception {
        File jrxml = new File("./report/" + baseName + ".jrxml");
        File jasper = new File("./report/" + baseName + ".jasper");
        if (!jrxml.exists()) {
            throw new Exception("File report " + baseName + ".jrxml tidak ditemukan.");
        }
        if (!jasper.exists() || jrxml.lastModified() > jasper.lastModified()) {
            JasperCompileManager.compileReportToFile(jrxml.getPath(), jasper.getPath());
        }
        return jasper.getPath();
    }

    private void ensureTable() {
        Sequel.queryu(
                "create table if not exists surat_keterangan_lahir ("
                + "no_rawat varchar(17) not null primary key,"
                + "no_rkm_medis varchar(15) null,"
                + "no_skl varchar(30) null,"
                + "nama_bayi varchar(50) null,"
                + "tgl_lahir date null,"
                + "jam_lahir varchar(8) null,"
                + "tempat_lahir varchar(150) null,"
                + "nama_ayah varchar(50) null,"
                + "nama_ibu varchar(50) null,"
                + "alamat_orang_tua varchar(200) null,"
                + "pekerjaan_ayah varchar(60) null,"
                + "pekerjaan_ibu varchar(60) null,"
                + "dokter_penolong varchar(100) null,"
                + "dokter_anak varchar(100) null,"
                + "jk varchar(20) null,"
                + "berat_badan varchar(10) null,"
                + "panjang_badan varchar(10) null,"
                + "lingkar_kepala varchar(10) null,"
                + "lingkar_dada varchar(10) null,"
                + "lingkar_perut varchar(10) null,"
                + "keterangan_lain varchar(200) null,"
                + "foto1 longblob null,"
                + "foto2 longblob null,"
                + "created_by varchar(50) null,"
                + "created_at datetime null,"
                + "updated_at datetime null"
                + ")");
    }

    // ====================== Foto ======================
    private int barisFoto(JPanel p, int row) {
        p.add(lbl("Foto 1"), gc(0, row, 1, 0.0));
        JPanel pnl1 = panelFotoTombol(1);
        p.add(pnl1, gc(1, row, 1, 1.0));
        p.add(lbl("Foto 2"), gc(2, row, 1, 0.0));
        JPanel pnl2 = panelFotoTombol(2);
        p.add(pnl2, gc(3, row, 1, 1.0));
        return row + 1;
    }

    private JPanel panelFotoTombol(int nomor) {
        widget.Button btn = new widget.Button();
        btn.setText("Upload Foto " + nomor);
        JLabel lbl = nomor == 1 ? lblFoto1 : lblFoto2;
        btn.addActionListener(e -> {
            byte[] data = pilihGambar();
            if (data == null) { return; }
            if (nomor == 1) { dataFoto1 = data; } else { dataFoto2 = data; }
            lbl.setText("(terisi " + data.length + " byte)");
        });
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnl.setOpaque(false);
        pnl.add(btn);
        pnl.add(lbl);
        return pnl;
    }

    private byte[] pilihGambar() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Gambar (jpg, jpeg, png, bmp)", "jpg", "jpeg", "png", "bmp"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                byte[] mentah = Files.readAllBytes(fc.getSelectedFile().toPath());
                return seragamkanUkuranFoto(mentah);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal membaca gambar.\n" + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Rasio kotak foto di jrxml (FOTO_BOX_W / FOTO_BOX_H, lihat report/rptSuratKeteranganLahir.jrxml
     * elemen &lt;image&gt; foto1/foto2, width="100" height="120"). Dipakai supaya Foto 1 & Foto 2
     * SELALU seragam bentuk/ukurannya di hasil cetak, tidak peduli foto asal yg diupload petugas
     * potret/lanskap/persegi apapun -- sebelumnya scaleImage="RetainShape" bikin 2 foto kelihatan
     * beda ukuran satu sama lain kalau rasio aslinya beda.
     */
    private static final int FOTO_BOX_W = 100;
    private static final int FOTO_BOX_H = 120;
    private static final int FOTO_TARGET_W = FOTO_BOX_W * 4;
    private static final int FOTO_TARGET_H = FOTO_BOX_H * 4;

    /**
     * Crop-tengah foto ke rasio kotak cetak lalu resize ke ukuran piksel TETAP
     * (400x480) -- jadi Foto 1 & Foto 2 selalu persis sama ukuran & rasionya
     * di hasil cetak, walau foto asal yg diupload petugas ukurannya beda-beda.
     */
    private static byte[] seragamkanUkuranFoto(byte[] asli) {
        try {
            Image gambar = ImageIO.read(new ByteArrayInputStream(asli));
            if (gambar == null) { return asli; }
            int w = gambar.getWidth(null);
            int h = gambar.getHeight(null);
            if (w <= 0 || h <= 0) { return asli; }

            double rasioTarget = FOTO_TARGET_W / (double) FOTO_TARGET_H;
            double rasioAsli = w / (double) h;
            int cropW = w;
            int cropH = h;
            if (rasioAsli > rasioTarget) {
                cropW = (int) Math.round(h * rasioTarget);
            } else if (rasioAsli < rasioTarget) {
                cropH = (int) Math.round(w / rasioTarget);
            }
            int cropX = (w - cropW) / 2;
            int cropY = (h - cropH) / 2;

            java.awt.image.BufferedImage hasil = new java.awt.image.BufferedImage(
                    FOTO_TARGET_W, FOTO_TARGET_H, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2 = hasil.createGraphics();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, FOTO_TARGET_W, FOTO_TARGET_H);
            g2.drawImage(gambar, 0, 0, FOTO_TARGET_W, FOTO_TARGET_H,
                    cropX, cropY, cropX + cropW, cropY + cropH, null);
            g2.dispose();

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            ImageIO.write(hasil, "jpg", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            return asli;
        }
    }

    // ====================== Helpers UI ======================
    private int baris1(JPanel p, int row, String label, widget.TextBox field) {
        p.add(lbl(label), gc(0, row, 1, 0.0));
        siz(field);
        p.add(field, gc(1, row, 3, 1.0));
        return row + 1;
    }

    private int baris2(JPanel p, int row, String label1, widget.TextBox f1, String label2, widget.TextBox f2) {
        p.add(lbl(label1), gc(0, row, 1, 0.0));
        siz(f1);
        p.add(f1, gc(1, row, 1, 1.0));
        p.add(lbl(label2), gc(2, row, 1, 0.0));
        siz(f2);
        p.add(f2, gc(3, row, 1, 1.0));
        return row + 1;
    }

    /**
     * Sama seperti baris2, tapi tiap field dibungkus tombol "..." untuk memilih dokter
     * dari master data (kepegawaian.DlgCariDokter) -- field tetap bisa diketik manual.
     */
    private int barisDokter(JPanel p, int row, String label1, widget.TextBox f1, String label2, widget.TextBox f2) {
        p.add(lbl(label1), gc(0, row, 1, 0.0));
        p.add(fieldDenganPickerDokter(f1), gc(1, row, 1, 1.0));
        p.add(lbl(label2), gc(2, row, 1, 0.0));
        p.add(fieldDenganPickerDokter(f2), gc(3, row, 1, 1.0));
        return row + 1;
    }

    private JPanel fieldDenganPickerDokter(widget.TextBox field) {
        siz(field);
        widget.Button btnPilih = new widget.Button();
        btnPilih.setText("...");
        btnPilih.setToolTipText("Pilih dari data dokter");
        btnPilih.setPreferredSize(new Dimension(28, 23));
        btnPilih.addActionListener(e -> pilihDokter(field));
        JPanel wrap = new JPanel(new BorderLayout(3, 0));
        wrap.setOpaque(false);
        wrap.add(field, BorderLayout.CENTER);
        wrap.add(btnPilih, BorderLayout.EAST);
        return wrap;
    }

    private void pilihDokter(widget.TextBox target) {
        kepegawaian.DlgCariDokter dlgDokter = new kepegawaian.DlgCariDokter(null, false);
        dlgDokter.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                try {
                    int r = dlgDokter.getTable().getSelectedRow();
                    if (r >= 0) {
                        target.setText(String.valueOf(dlgDokter.getTable().getValueAt(r, 1)));
                    }
                } catch (Exception ex) {
                    // batal pilih / tabel kosong, biarkan field apa adanya
                }
            }
        });
        dlgDokter.setLocationRelativeTo(this);
        dlgDokter.setVisible(true);
    }

    private int baris3(JPanel p, int row, String l1, widget.TextBox f1, String l2, widget.TextBox f2, String l3, widget.TextBox f3) {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 3, 3, 3);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridy = 0;
        g.gridx = 0; g.weightx = 0; wrap.add(lbl(l1), g);
        g.gridx = 1; g.weightx = 1; siz(f1); wrap.add(f1, g);
        g.gridx = 2; g.weightx = 0; wrap.add(lbl(l2), g);
        g.gridx = 3; g.weightx = 1; siz(f2); wrap.add(f2, g);
        g.gridx = 4; g.weightx = 0; wrap.add(lbl(l3), g);
        g.gridx = 5; g.weightx = 1; siz(f3); wrap.add(f3, g);
        p.add(wrap, gc(0, row, 4, 1.0));
        return row + 1;
    }

    private int barisTglJam(JPanel p, int row) {
        p.add(lbl("Tanggal Lahir"), gc(0, row, 1, 0.0));
        dtpTglLahir.setPreferredSize(new Dimension(150, 25));
        p.add(dtpTglLahir, gc(1, row, 1, 1.0));
        p.add(lbl("Jam Lahir"), gc(2, row, 1, 0.0));
        siz(TJamLahir);
        TJamLahir.setToolTipText("HH:mm, contoh 02:45");
        p.add(TJamLahir, gc(3, row, 1, 1.0));
        return row + 1;
    }

    private int barisKombo(JPanel p, int row, String label, widget.ComboBox combo) {
        p.add(lbl(label), gc(0, row, 1, 0.0));
        combo.setPreferredSize(new Dimension(220, 25));
        p.add(combo, gc(1, row, 3, 1.0));
        return row + 1;
    }

    private int area(JPanel p, int row, String label, widget.TextArea a) {
        p.add(lbl(label), gc(0, row, 1, 0.0));
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        JScrollPane sc = new JScrollPane(a);
        sc.setPreferredSize(new Dimension(400, 60));
        sc.setWheelScrollingEnabled(false);
        p.add(sc, gc(1, row, 3, 1.0));
        return row + 1;
    }

    private int judul(JPanel p, int row, String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(new Font("Tahoma", Font.BOLD, 12));
        l.setForeground(new Color(32, 49, 66));
        p.add(l, gc(0, row, 4, 1.0));
        return row + 1;
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t + " :");
        l.setFont(new Font("Tahoma", Font.PLAIN, 11));
        return l;
    }

    private void siz(Component c) {
        c.setPreferredSize(new Dimension(220, 23));
    }

    private GridBagConstraints gc(int x, int y, int w, double weightx) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x; g.gridy = y; g.gridwidth = w;
        g.weightx = weightx;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(3, 3, 3, 3);
        g.anchor = GridBagConstraints.WEST;
        return g;
    }

    private static widget.ComboBox comboJK() {
        widget.ComboBox c = new widget.ComboBox();
        c.addItem("");
        c.addItem("LAKI-LAKI");
        c.addItem("PEREMPUAN");
        return c;
    }

    private static widget.Tanggal dt() {
        widget.Tanggal d = new widget.Tanggal();
        d.setDisplayFormat("dd-MM-yyyy");
        return d;
    }

    private static widget.TextArea ta() {
        return new widget.TextArea();
    }

    private static void setTgl(widget.Tanggal d, java.sql.Date sqlDate) {
        d.setDate(sqlDate == null ? new Date() : new Date(sqlDate.getTime()));
    }

    /**
     * "yyyy-MM-dd" siap simpan ke kolom DATE -- pakai getDate() langsung (bukan
     * getSelectedItem().toString()), karena toString()-nya ikut format tampilan
     * "dd-MM-yyyy" (lihat dt()/setDisplayFormat) dan bikin nilai yg disimpan ke
     * MySQL jadi salah format (baru ketahuan lewat error "Incorrect date value").
     */
    private static String ambilTgl(widget.Tanggal d) {
        Date x = d.getDate();
        return x == null ? null : new java.text.SimpleDateFormat("yyyy-MM-dd").format(x);
    }

    private static String s(widget.ComboBox c) {
        Object v = c.getSelectedItem();
        return v == null ? "" : v.toString();
    }

    private static String g(ResultSet rs, String kolom) {
        try {
            String v = rs.getString(kolom);
            return v == null ? "" : v;
        } catch (Exception e) {
            return "";
        }
    }

    private static String nvl(String v) {
        return v == null ? "" : v;
    }
}
