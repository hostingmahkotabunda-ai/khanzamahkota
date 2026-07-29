package rekammedis;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

/**
 * Util cetak preview Jasper untuk form-form asesmen keperawatan
 * (Ralan/Bayi/Anak/Dewasa/Kebidanan). Memakai template bersama
 * report/rptAsesmenKeperawatan.jrxml (kop RS + tabel identitas pasien + TTD
 * ganda + QR) dan subreport report/rptAsesmenBaris.jrxml untuk badan laporan
 * (section/field/field2, dirender sebagai kotak bergaris JasperReports asli
 * -- BUKAN html di dalam text field, karena markup="html" JasperReports
 * terbukti tidak mendukung tabel/border/background-color).
 *
 * Isi laporan dibangun sebagai List&lt;Map&lt;String,Object&gt;&gt; "baris"
 * lewat helper h()/r()/r2(), lalu dikirim sebagai datasource subreport.
 */
public final class CetakAsesmen {

    private static final sekuel Sequel = new sekuel();
    private static final validasi Valid = new validasi();

    private CetakAsesmen() {
    }

    /** Kontrak untuk field checkbox-group (Grup) di tiap form asesmen. */
    public interface OpsiCheckbox {
        List<String> semuaOpsi();
        String get();
    }

    /** Identitas pasien untuk tabel kop laporan. Semua field String, boleh kosong. */
    public static final class Identitas {
        public String nama = "";
        public String noRawat = "";
        public String kelas = "";
        public String nik = "";
        public String tglMasuk = "";
        public String pembayaran = "";
        public String jk = "";
        public String noRM = "";
        public String unit = "";
        public String tglLahir = "";
        public String alamat = "";
    }

    public static List<Map<String, ?>> mulai() {
        return new ArrayList<Map<String, ?>>();
    }

    /**
     * Penanda internal: string yang SUDAH berupa HTML aman (dibangun oleh
     * formatCheckboxHtml/fmt) dan tidak boleh di-escape ulang. Dipakai supaya
     * label/field teks bebas dari form (yang mungkin mengandung "&lt;"/"&amp;")
     * tetap otomatis di-escape, sementara markup checkbox tebal/warna tidak
     * ikut ter-escape saat lewat r()/r2() yang sama.
     */
    private static final String RAW_PREFIX = "RAWHTML";

    /** Escape teks bebas jadi aman utk markup HTML subreport; baris baru jadi &lt;br/&gt;. Lewati string yang sudah RAW_PREFIX. */
    private static String amankan(String s) {
        if (s == null) { return ""; }
        if (s.startsWith(RAW_PREFIX)) {
            return s.substring(RAW_PREFIX.length());
        }
        return esc(s).replace("\n", "<br/>");
    }

    /** Section header: bar abu-abu, judul tebal. */
    public static void h(List<Map<String, ?>> rows, String judul) {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("tipe", "section");
        row.put("label", amankan(judul));
        row.put("nilai", "");
        row.put("label2", "");
        row.put("nilai2", "");
        rows.add(row);
    }

    /** Baris field satu kolom penuh ("LABEL : nilai", kotak bergaris; kosong jika nilai kosong). */
    public static void r(List<Map<String, ?>> rows, String label, String nilai) {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("tipe", "field");
        row.put("label", amankan(label));
        row.put("nilai", amankan(nilai));
        row.put("label2", "");
        row.put("nilai2", "");
        rows.add(row);
    }

    /** Baris field checkbox-group: semua opsi ditampilkan, yang tercentang tebal+hijau, sisanya abu-abu. */
    public static void r(List<Map<String, ?>> rows, String label, OpsiCheckbox grup) {
        r(rows, label, fmt(grup));
    }

    /** Baris dua kolom sejajar (mis. "NAMA : .." | "UMUR : .."). */
    public static void r2(List<Map<String, ?>> rows, String label1, String nilai1, String label2, String nilai2) {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("tipe", "field2");
        row.put("label", amankan(label1));
        row.put("nilai", amankan(nilai1));
        row.put("label2", amankan(label2));
        row.put("nilai2", amankan(nilai2));
        rows.add(row);
    }

    /** Dipertahankan untuk kompatibilitas pemanggil lama; spasi kini bawaan dari kotak bergaris. */
    public static void sp(List<Map<String, ?>> rows) {
        // no-op
    }

    /**
     * Format checkbox-group jadi markup HTML siap-cetak. Kalau TIDAK ADA opsi
     * yang dicentang sama sekali -> kembalikan kosong (field tampil blank,
     * sama seperti field teks yang belum diisi -- supaya tidak terlihat
     * seperti terisi padahal cuma daftar opsi abu-abu semua). Kalau ADA yang
     * dicentang -> semua opsi tetap ditampilkan, yang tercentang <b>tebal</b>,
     * sisanya abu-abu (simbol centang unicode ✓/☐ terbukti TIDAK muncul di
     * render PDF pipeline ini, makanya pakai tebal/warna bukan simbol).
     * Hasilnya ditandai RAW_PREFIX supaya r()/r2() tidak meng-escape ulang
     * markup-nya. Dipakai juga saat checkbox perlu digabung r2() dengan field
     * teks lain.
     */
    public static String fmt(OpsiCheckbox grup) {
        return RAW_PREFIX + formatCheckboxHtml(grup);
    }

    private static String formatCheckboxHtml(OpsiCheckbox grup) {
        if (grup == null) { return ""; }
        String v = grup.get();
        if (v == null || v.trim().isEmpty()) { return ""; }
        java.util.Set<String> terpilih = new java.util.HashSet<String>();
        for (String x : v.split(",")) { terpilih.add(x.trim()); }
        List<String> opsi = grup.semuaOpsi();
        StringBuilder sb = new StringBuilder();
        for (String o : opsi) {
            if (sb.length() > 0) { sb.append("   "); }
            String teks = esc(o);
            if (terpilih.contains(o)) {
                sb.append("<b>").append(teks).append("</b>");
            } else {
                sb.append("<font color='#AAAAAA'>").append(teks).append("</font>");
            }
        }
        return sb.toString();
    }

    public static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * Tampilkan preview Jasper.
     *
     * @param judul     judul form (mis. "ASESMEN KEPERAWATAN DEWASA")
     * @param koderm    kode form RM (mis. "RM 5d")
     * @param rows      baris isi laporan (dari h()/r()/r2())
     * @param id        identitas pasien untuk tabel kop
     * @param tglTtd    teks tanggal/jam TTD
     * @param ttdLabel  label penandatangan kiri (mis. "Perawat Pengkaji")
     * @param nip       NIP penandatangan kiri (untuk tarik foto TTD dari data pegawai)
     * @param namaTtd   nama penandatangan kiri
     * @param dpjpLabel label kotak kanan (mis. "Dokter Penanggung Jawab"), boleh null/kosong untuk sembunyikan
     * @param dpjpNama  nama dokter penanggung jawab, boleh null/kosong untuk sembunyikan
     */
    public static void cetak(String judul, String koderm, List<Map<String, ?>> rows, Identitas id,
            String tglTtd, String ttdLabel, String nip, String namaTtd, String dpjpLabel, String dpjpNama) {
        try {
            siapkanReport("rptAsesmenKeperawatan");
            String subBarisPath = siapkanReport("rptAsesmenBaris");
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            param.put("judul", judul == null ? "" : judul);
            param.put("koderm", koderm == null ? "" : koderm);
            param.put("baris", new JRMapCollectionDataSource(rows));
            param.put("subBarisPath", subBarisPath);
            Identitas i = id == null ? new Identitas() : id;
            param.put("id_nama", i.nama);
            param.put("id_norawat", i.noRawat);
            param.put("id_kelas", i.kelas);
            param.put("id_nik", i.nik);
            param.put("id_tglmrs", i.tglMasuk);
            param.put("id_pembayaran", i.pembayaran);
            param.put("id_jk", i.jk);
            param.put("id_norm", i.noRM);
            param.put("id_unit", i.unit);
            param.put("id_tgllahir", i.tglLahir);
            param.put("id_alamat", i.alamat);
            param.put("ttd_label", ttdLabel == null ? "" : ttdLabel);
            param.put("ttd_nama", namaTtd == null ? "" : namaTtd);
            param.put("kota_ttd", akses.getkabupatenrs());
            param.put("tgl_ttd", tglTtd == null ? "" : tglTtd);
            param.put("url_penggajian", "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/"
                    + koneksiDB.HYBRIDWEB() + "/penggajian/");
            param.put("ttd_petugas_photo", ambilFotoPegawai(nip));
            param.put("dpjp_label", dpjpLabel == null ? "" : dpjpLabel);
            param.put("dpjp_nama", dpjpNama == null ? "" : dpjpNama);
            param.put("ttd_dokter_photo", ambilFotoDokter(dpjpNama));
            Valid.MyReport("rptAsesmenKeperawatan.jasper", "report", judul, param);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Gagal cetak asesmen : " + e.getMessage());
        }
    }

    /** Foto TTD petugas (perawat/bidan pengkaji) berdasarkan NIP -- pola sama seperti ppa_photo di CetakCPPT. */
    private static String ambilFotoPegawai(String nip) {
        if (nip == null || nip.trim().isEmpty()) { return ""; }
        return bersihkanPathFoto(Sequel.cariIsi("select photo from pegawai where nik=?", nip.trim()));
    }

    /** Foto TTD dokter -- dokter tidak punya kolom photo sendiri, dicocokkan lewat nama ke tabel pegawai (pola sama seperti validator_photo di CetakCPPT). */
    private static String ambilFotoDokter(String namaDokter) {
        if (namaDokter == null || namaDokter.trim().isEmpty()) { return ""; }
        return bersihkanPathFoto(Sequel.cariIsi(
                "select photo from pegawai where lower(trim(nama))=lower(trim(?)) limit 1", namaDokter.trim()));
    }

    private static String bersihkanPathFoto(String photo) {
        if (photo == null) { return ""; }
        String p = photo.trim();
        if (p.equals("") || p.equals("-") || p.equals("pages/pegawai/photo/")) { return ""; }
        return p.replace("\\", "/");
    }

    /** Kompilasi jrxml->jasper bila perlu; mengembalikan path file .jasper. */
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
}
