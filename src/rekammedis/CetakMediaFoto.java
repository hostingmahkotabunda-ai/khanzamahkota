package rekammedis;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JasperCompileManager;

/**
 * Cetak/preview Jasper untuk satu foto dokumentasi rawat inap (tabel
 * dokumentasi_foto_ranap), dipanggil dari tab "Media" di RMRiwayatPerawatan.
 * Beda dari preview media di webapp billing-ranap (cuma lihat gambar) --
 * di sini tiap foto bisa langsung dicetak/di-preview sbg dokumen Jasper
 * lengkap kop RS + identitas pasien.
 */
public final class CetakMediaFoto {

    private static final sekuel Sequel = new sekuel();
    private static final validasi Valid = new validasi();

    private CetakMediaFoto() {
    }

    public static void cetak(int id) {
        Connection koneksi = koneksiDB.condb();
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select dokumentasi_foto_ranap.no_rawat,dokumentasi_foto_ranap.keterangan,"
                + "dokumentasi_foto_ranap.nama_file,dokumentasi_foto_ranap.photo,"
                + "date_format(dokumentasi_foto_ranap.tgl_upload,'%d-%m-%Y %H:%i') as tgl_upload,"
                + "ifnull(petugas.nama,dokumentasi_foto_ranap.created_by) as oleh,"
                + "ifnull(pasien.nm_pasien,'') as nm_pasien,ifnull(pasien.no_rkm_medis,'') as no_rkm_medis "
                + "from dokumentasi_foto_ranap "
                + "left join petugas on petugas.nip=dokumentasi_foto_ranap.created_by "
                + "left join reg_periksa on reg_periksa.no_rawat=dokumentasi_foto_ranap.no_rawat "
                + "left join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "
                + "where dokumentasi_foto_ranap.id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    javax.swing.JOptionPane.showMessageDialog(null, "Data foto tidak ditemukan.");
                    return;
                }
                byte[] foto = rs.getBytes("photo");
                if (foto == null) {
                    javax.swing.JOptionPane.showMessageDialog(null, "Foto kosong/rusak, tidak bisa dicetak.");
                    return;
                }
                siapkanReport("rptMediaFoto");
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("namars", akses.getnamars());
                param.put("alamatrs", akses.getalamatrs());
                param.put("kotars", akses.getkabupatenrs());
                param.put("propinsirs", akses.getpropinsirs());
                param.put("kontakrs", akses.getkontakrs());
                param.put("emailrs", akses.getemailrs());
                param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
                param.put("nama_pasien", rs.getString("nm_pasien"));
                param.put("no_rm", rs.getString("no_rkm_medis"));
                param.put("no_rawat", rs.getString("no_rawat"));
                param.put("keterangan", nvl(rs.getString("keterangan")).isEmpty() ? nvl(rs.getString("nama_file")) : rs.getString("keterangan"));
                param.put("tgl_upload", rs.getString("tgl_upload"));
                param.put("oleh", rs.getString("oleh"));
                param.put("foto", new ByteArrayInputStream(foto));
                Valid.MyReport("rptMediaFoto.jasper", "report", "Dokumentasi Foto Rawat Inap", param);
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Gagal cetak foto.\n" + e.getMessage());
        }
    }

    private static String nvl(String v) {
        return v == null ? "" : v;
    }

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
