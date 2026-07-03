package rekammedis;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Image;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JasperCompileManager;

public final class CetakCPPT {
    private static final sekuel Sequel = new sekuel();
    private static final validasi Valid = new validasi();

    private CetakCPPT() {
    }

    public static void cetak(String noRawat, String jenis) {
        String nomorRawat = noRawat == null ? "" : noRawat.trim();
        String mode = jenis == null ? "" : jenis.trim().toLowerCase();
        String tabel = mode.equals("ranap") ? "pemeriksaan_ranap" : "pemeriksaan_ralan";
        String judul = mode.equals("ranap") ? "::[ Cetak CPPT Rawat Inap ]::" :
                (mode.equals("igd") ? "::[ Cetak CPPT IGD ]::" : "::[ Cetak CPPT Rawat Jalan ]::");

        if (nomorRawat.equals("")) {
            JOptionPane.showMessageDialog(null, "Pilih pasien terlebih dahulu.");
            return;
        }

        DlgValidasiSOAP.pastikanKolom(tabel);
        if (Sequel.cariInteger("select count(*) from " + tabel + " where no_rawat='" + amanSql(nomorRawat) + "'") == 0) {
            JOptionPane.showMessageDialog(null, "Belum ada data SOAP/CPPT untuk No.Rawat " + nomorRawat + ".");
            return;
        }

        try {
            siapkanReportCPPT("rptCPPTRanap");
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            param.put("url_penggajian", "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" +
                    koneksiDB.HYBRIDWEB() + "/penggajian/");
            Valid.MyReportqry("rptCPPTRanap.jasper", "report", judul, queryCetak(nomorRawat, mode, tabel), param);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal cetak CPPT : " + e.getMessage());
        }
    }

    private static String queryCetak(String noRawat, String jenis, String tabel) {
        String kamar = jenis.equals("ranap") ?
                "ifnull((select concat(bangsal.nm_bangsal,' / ',kamar.kd_kamar) from kamar_inap " +
                "inner join kamar on kamar_inap.kd_kamar=kamar.kd_kamar " +
                "inner join bangsal on kamar.kd_bangsal=bangsal.kd_bangsal " +
                "where kamar_inap.no_rawat=reg_periksa.no_rawat order by kamar_inap.tgl_masuk desc,kamar_inap.jam_masuk desc limit 1),'') as kamar," :
                "'' as kamar,";
        String dpjp = jenis.equals("ranap") ?
                "ifnull((select dokter_dpjp.nm_dokter from dpjp_ranap " +
                "inner join dokter dokter_dpjp on dpjp_ranap.kd_dokter=dokter_dpjp.kd_dokter " +
                "where dpjp_ranap.no_rawat=reg_periksa.no_rawat limit 1),ifnull(dokter.nm_dokter,'')) as dpjp," :
                "ifnull(dokter.nm_dokter,'') as dpjp,";
        String validatorPhoto = "if(coalesce(nullif(vpegawai.photo,''),nullif((select p2.photo from pegawai p2 where lower(trim(p2.nama))=lower(trim(vdokter.nm_dokter)) limit 1),''),'')='' " +
                "or coalesce(nullif(vpegawai.photo,''),nullif((select p2.photo from pegawai p2 where lower(trim(p2.nama))=lower(trim(vdokter.nm_dokter)) limit 1),''),'')='-' " +
                "or coalesce(nullif(vpegawai.photo,''),nullif((select p2.photo from pegawai p2 where lower(trim(p2.nama))=lower(trim(vdokter.nm_dokter)) limit 1),''),'')='pages/pegawai/photo/'," +
                "'',replace(coalesce(nullif(vpegawai.photo,''),nullif((select p2.photo from pegawai p2 where lower(trim(p2.nama))=lower(trim(vdokter.nm_dokter)) limit 1),''),''),'\\\\','/')) as validator_photo,";

        return "select reg_periksa.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien," +
                "if(pasien.jk='L','Laki-laki','Perempuan') as jk," +
                "ifnull(date_format(pasien.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir," +
                "concat(ifnull(pasien.alamat,'')," +
                "if(kelurahan.nm_kel is null or kelurahan.nm_kel='', '', concat(', ',kelurahan.nm_kel))," +
                "if(kecamatan.nm_kec is null or kecamatan.nm_kec='', '', concat(', ',kecamatan.nm_kec))," +
                "if(kabupaten.nm_kab is null or kabupaten.nm_kab='', '', concat(', ',kabupaten.nm_kab))) as alamat," +
                "ifnull(date_format(reg_periksa.tgl_registrasi,'%d-%m-%Y'),'') as tgl_masuk," +
                "ifnull(date_format(reg_periksa.jam_reg,'%H:%i:%s'),'') as jam_masuk," +
                kamar + dpjp +
                "date_format(" + tabel + ".tgl_perawatan,'%d-%m-%Y') as tgl_perawatan," +
                "ifnull(date_format(" + tabel + ".jam_rawat,'%H:%i:%s'),'') as jam_rawat," +
                "ifnull(" + tabel + ".suhu_tubuh,'') as suhu_tubuh,ifnull(" + tabel + ".tensi,'') as tensi," +
                "ifnull(" + tabel + ".nadi,'') as nadi,ifnull(" + tabel + ".respirasi,'') as respirasi,ifnull(" + tabel + ".tinggi,'') as tinggi," +
                "ifnull(" + tabel + ".berat,'') as berat,ifnull(" + tabel + ".spo2,'') as spo2,ifnull(" + tabel + ".gcs,'') as gcs,ifnull(" + tabel + ".kesadaran,'') as kesadaran," +
                "ifnull(" + tabel + ".keluhan,'') as keluhan,ifnull(" + tabel + ".pemeriksaan,'') as pemeriksaan," +
                "ifnull(" + tabel + ".alergi,'') as alergi,ifnull(" + tabel + ".penilaian,'') as penilaian," +
                "ifnull(" + tabel + ".rtl,'') as rtl,ifnull(" + tabel + ".instruksi,'') as instruksi," +
                "ifnull(" + tabel + ".evaluasi,'') as evaluasi," + tabel + ".nip,pegawai.nama,pegawai.jbtn," +
                "ifnull(" + tabel + ".validasi,'Belum') as validasi," +
                "ifnull(" + tabel + ".validator,'') as validator," +
                "ifnull(vdokter.nm_dokter,ifnull(vpegawai.nama,'')) as nama_validator," +
                validatorPhoto +
                "ifnull(date_format(" + tabel + ".tgl_validasi,'%d-%m-%Y'),'') as tgl_validasi," +
                "ifnull(date_format(" + tabel + ".jam_validasi,'%H:%i:%s'),'') as jam_validasi " +
                "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                "inner join " + tabel + " on " + tabel + ".no_rawat=reg_periksa.no_rawat " +
                "inner join pegawai on " + tabel + ".nip=pegawai.nik " +
                "left join dokter on reg_periksa.kd_dokter=dokter.kd_dokter " +
                "left join dokter vdokter on " + tabel + ".validator=vdokter.kd_dokter " +
                "left join pegawai vpegawai on " + tabel + ".validator=vpegawai.nik " +
                "left join kelurahan on pasien.kd_kel=kelurahan.kd_kel " +
                "left join kecamatan on pasien.kd_kec=kecamatan.kd_kec " +
                "left join kabupaten on pasien.kd_kab=kabupaten.kd_kab " +
                "where reg_periksa.no_rawat='" + amanSql(noRawat) + "' " +
                "order by " + tabel + ".tgl_perawatan," + tabel + ".jam_rawat";
    }

    private static void siapkanReportCPPT(String baseName) throws Exception {
        File jrxml = new File("./report/" + baseName + ".jrxml");
        File jasper = new File("./report/" + baseName + ".jasper");
        if (!jrxml.exists()) {
            throw new Exception("File report " + baseName + " tidak ditemukan.");
        }
        if (!jasper.exists() || jrxml.lastModified() > jasper.lastModified()) {
            JasperCompileManager.compileReportToFile(jrxml.getPath(), jasper.getPath());
        }
    }

    private static String amanSql(String nilai) {
        return nilai == null ? "" : nilai.replace("'", "''");
    }

    public static Image ambilGambarServer(String alamat) {
        String url = alamat == null ? "" : alamat.trim();
        if (url.equals("")) {
            return null;
        }
        Image gambar = bacaGambarServer(url);
        if (gambar != null) {
            return gambar;
        }
        int posisi = url.lastIndexOf('/');
        String namaFile = posisi >= 0 ? url.substring(posisi + 1) : url;
        String base = url;
        int idxPenggajian = url.indexOf("/penggajian/");
        if (idxPenggajian >= 0) {
            base = url.substring(0, idxPenggajian + "/penggajian/".length());
            gambar = bacaGambarServer(base + "pages/pegawai/photo/" + namaFile);
            if (gambar != null) {
                return gambar;
            }
            gambar = bacaGambarServer(base + "photo/" + namaFile);
            if (gambar != null) {
                return gambar;
            }
        }
        return null;
    }

    private static Image bacaGambarServer(String url) {
        try {
            URLConnection koneksiGambar = new URL(encodeUrlGambar(url)).openConnection();
            koneksiGambar.setConnectTimeout(3000);
            koneksiGambar.setReadTimeout(3000);
            return ImageIO.read(koneksiGambar.getInputStream());
        } catch (Exception e) {
            return null;
        }
    }

    private static String encodeUrlGambar(String alamat) {
        try {
            URL url = new URL(alamat);
            String[] potongan = url.getPath().split("/", -1);
            StringBuilder path = new StringBuilder();
            for (int i = 0; i < potongan.length; i++) {
                if (i > 0) {
                    path.append("/");
                }
                if (!potongan[i].equals("")) {
                    path.append(URLEncoder.encode(potongan[i], "UTF-8").replace("+", "%20"));
                }
            }
            String port = url.getPort() == -1 ? "" : ":" + url.getPort();
            String query = url.getQuery() == null ? "" : "?" + url.getQuery();
            return url.getProtocol() + "://" + url.getHost() + port + path.toString() + query;
        } catch (Exception e) {
            return alamat.replace(" ", "%20");
        }
    }
}
