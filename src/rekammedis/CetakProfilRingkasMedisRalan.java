package rekammedis;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JasperCompileManager;

/** Cetak Profil Ringkas Medis Rawat Jalan (RM.01) untuk seluruh riwayat pasien. */
public final class CetakProfilRingkasMedisRalan {
    private static final Connection koneksi = koneksiDB.condb();
    private static final sekuel Sequel = new sekuel();
    private static final validasi Valid = new validasi();

    private CetakProfilRingkasMedisRalan() {}

    public static void cetak(String noRM) {
        if (noRM == null || noRM.trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Pilih pasien terlebih dahulu.");
            return;
        }
        try {
            Map<String,Object> param = identitas(noRM.trim());
            File jrxml = new File("./report/rptProfilRingkasMedisRalan.jrxml");
            File jasper = new File("./report/rptProfilRingkasMedisRalan.jasper");
            if (!jrxml.exists()) { throw new Exception("Template rptProfilRingkasMedisRalan.jrxml tidak ditemukan."); }
            if (!jasper.exists() || jrxml.lastModified() > jasper.lastModified()) {
                // Proyek membawa beberapa versi Eclipse JDT; gunakan javac agar
                // kompilasi laporan tidak berhenti karena konflik versi JDT.
                System.setProperty("net.sf.jasperreports.compiler.java",
                        "net.sf.jasperreports.engine.design.JRJavacCompiler");
                DefaultJasperReportsContext.getInstance().setProperty(
                        "net.sf.jasperreports.compiler.java",
                        "net.sf.jasperreports.engine.design.JRJavacCompiler");
                JasperCompileManager.compileReportToFile(jrxml.getPath(), jasper.getPath());
            }
            Valid.MyReport("rptProfilRingkasMedisRalan.jasper", "report",
                    "::[ Profil Ringkas Medis Rawat Jalan ]::", param);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal mencetak Profil Ringkas Medis Rawat Jalan:\n" + e.getMessage());
            System.out.println("Notif cetak profil ringkas medis ralan: " + e);
        }
    }

    private static Map<String,Object> identitas(String noRM) throws Exception {
        Map<String,Object> p = new HashMap<String,Object>();
        p.put("namars", akses.getnamars());
        p.put("logo", Sequel.cariGambar("select setting.logo from setting"));
        p.put("nama", ""); p.put("norm", noRM); p.put("tgllahir", ""); p.put("jk", "");
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select nm_pasien,date_format(tgl_lahir,'%d-%m-%Y') tgl_lahir,jk from pasien where no_rkm_medis=?")) {
            ps.setString(1, noRM);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p.put("nama", nilai(rs.getString("nm_pasien")));
                    p.put("tgllahir", nilai(rs.getString("tgl_lahir")));
                    p.put("jk", nilai(rs.getString("jk")));
                }
            }
        }
        return p;
    }

    private static String nilai(String s) { return s == null ? "" : s; }
}
