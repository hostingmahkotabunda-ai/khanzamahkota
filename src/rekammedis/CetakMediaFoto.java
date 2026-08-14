package rekammedis;

import fungsi.koneksiDB;
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
 * Hasil cetaknya HANYA gambar aslinya saja (tanpa kop/identitas/keterangan
 * -- per permintaan user 2026-08-14), beda dari preview di webapp
 * billing-ranap yang cuma bisa lihat gambar, tidak bisa dicetak.
 */
public final class CetakMediaFoto {

    private static final validasi Valid = new validasi();

    private CetakMediaFoto() {
    }

    public static void cetak(int id) {
        Connection koneksi = koneksiDB.condb();
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select photo from dokumentasi_foto_ranap where id=?")) {
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
                param.put("foto", new ByteArrayInputStream(foto));
                Valid.MyReport("rptMediaFoto.jasper", "report", "Dokumentasi Foto Rawat Inap", param);
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Gagal cetak foto.\n" + e.getMessage());
        }
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
