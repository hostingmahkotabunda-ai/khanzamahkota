package rekammedis;

import fungsi.akses;
import fungsi.sekuel;
import fungsi.validasi;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JasperCompileManager;

/**
 * Util cetak preview Jasper untuk form-form asesmen keperawatan
 * (Bayi/Anak/Dewasa/Kebidanan). Memakai satu template bersama
 * report/rptAsesmenKeperawatan.jrxml: kop RS + isi (HTML) + tanda tangan QR.
 * Isi laporan dibangun di masing-masing form sebagai HTML lalu dikirim
 * lewat parameter "isi".
 */
public final class CetakAsesmen {

    private static final sekuel Sequel = new sekuel();
    private static final validasi Valid = new validasi();

    private CetakAsesmen() {
    }

    /** Header section tebal. */
    public static void h(StringBuilder sb, String judul) {
        sb.append("<b>").append(esc(judul)).append("</b><br/>");
    }

    /** Baris "Label : nilai" (nilai kosong -> "-"). */
    public static void r(StringBuilder sb, String label, String nilai) {
        String v = (nilai == null || nilai.trim().isEmpty()) ? "-" : esc(nilai).replace("\n", "<br/>");
        sb.append(esc(label)).append(" : ").append(v).append("<br/>");
    }

    /** Spasi antar section. */
    public static void sp(StringBuilder sb) {
        sb.append("<br/>");
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
     * @param judul     judul form (mis. "ASSESMENT KEPERAWATAN ANAK")
     * @param koderm    kode form RM (mis. "RM 5d")
     * @param isiHtml   isi laporan dalam HTML
     * @param tglTtd    teks tanggal/jam TTD
     * @param ttdLabel  label penandatangan (mis. "Perawat Pengkaji")
     * @param nip       NIP penandatangan (untuk QR)
     * @param namaTtd   nama penandatangan
     */
    public static void cetak(String judul, String koderm, String isiHtml,
            String tglTtd, String ttdLabel, String nip, String namaTtd) {
        try {
            siapkanReport("rptAsesmenKeperawatan");
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            param.put("judul", judul == null ? "" : judul);
            param.put("koderm", koderm == null ? "" : koderm);
            param.put("isi", isiHtml == null ? "" : isiHtml);
            param.put("ttd_label", ttdLabel == null ? "" : ttdLabel);
            param.put("ttd_nama", namaTtd == null ? "" : namaTtd);
            param.put("kota_ttd", akses.getkabupatenrs());
            param.put("tgl_ttd", tglTtd == null ? "" : tglTtd);
            param.put("finger", buatQr(nip, namaTtd, tglTtd));
            Valid.MyReport("rptAsesmenKeperawatan.jasper", "report", judul, param);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Gagal cetak asesmen : " + e.getMessage());
        }
    }

    private static String buatQr(String nip, String nama, String tglTtd) {
        String finger = Sequel.cariIsi(
                "select sha1(sidikjari.sidikjari) from sidikjari "
                + "inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?", nip);
        finger = finger == null ? "" : finger;
        return "Ditandatangani secara elektronik oleh " + (nama == null ? "" : nama)
                + "\nID " + (finger.isEmpty() ? (nip == null ? "" : nip) : finger)
                + "\n" + (tglTtd == null ? "" : tglTtd)
                + "\n" + akses.getnamars();
    }

    private static void siapkanReport(String baseName) throws Exception {
        File jrxml = new File("./report/" + baseName + ".jrxml");
        File jasper = new File("./report/" + baseName + ".jasper");
        if (!jrxml.exists()) {
            throw new Exception("File report " + baseName + ".jrxml tidak ditemukan.");
        }
        if (!jasper.exists() || jrxml.lastModified() > jasper.lastModified()) {
            JasperCompileManager.compileReportToFile(jrxml.getPath(), jasper.getPath());
        }
    }
}
