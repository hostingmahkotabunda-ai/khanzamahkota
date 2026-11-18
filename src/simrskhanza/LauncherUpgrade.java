package simrskhanza;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mengganti jalankan-client.bat versi lama di PC client dengan launcher terkini (yang memanggil
 * launcher\launcher.ps1 untuk update dengan indikator progress), tanpa perlu disalin manual.
 *
 * Kenapa perlu: update otomatis sengaja tidak menyalin *.bat (robocopy /XF *.bat), jadi .bat
 * di client tidak pernah ikut berubah. Kelas ini jalan sekali saat Khanza dibuka.
 *
 * Keamanan mengganti .bat yang SEDANG berjalan: cmd membaca .bat sedikit demi sedikit dan
 * setelah java selesai, cmd melanjutkan membaca dari posisi byte setelah baris java. Karena itu
 * file baru WAJIB lebih pendek dari posisi itu, sehingga cmd langsung bertemu akhir file dan
 * berhenti. Launcher v2 menaruh "& exit" di baris java yang sama, jadi penggantian berikutnya
 * tidak terikat batas ini lagi.
 *
 * Alamat SERVER, akun share, dan opsi memori java diambil dari .bat lama (per PC bisa beda).
 */
final class LauncherUpgrade {

    /** Versi launcher terkini. Naikkan angkanya bila template launcher-client.bat.txt diubah. */
    static final String PENANDA = "KHANZA-LAUNCHER v3";
    /** Launcher v2 ke atas selalu menaruh "exit" di baris yang sama dengan baris yang sedang berjalan. */
    static final String PENANDA_UMUM = "KHANZA-LAUNCHER v";

    private LauncherUpgrade() {
    }

    static void periksa() {
        try {
            File dir = new File(System.getProperty("user.dir"));
            File bat = new File(dir, "jalankan-client.bat");
            File ps1 = new File(dir, "launcher" + File.separator + "launcher.ps1");
            if (!bat.isFile() || !ps1.isFile()) {
                return;   // bukan PC client ber-launcher, atau launcher.ps1 belum tiba
            }
            byte[] lama = Files.readAllBytes(bat.toPath());
            String s = new String(lama, StandardCharsets.ISO_8859_1);
            if (s.contains(PENANDA)) {
                return;   // sudah versi terkini
            }
            String baru = susun(s);
            if (baru == null) {
                System.out.println("  [Launcher] format jalankan-client.bat tidak dikenali, dilewati.");
                return;
            }
            byte[] isi = baru.getBytes(StandardCharsets.ISO_8859_1);
            // .bat v1 (lama): cmd melanjutkan membaca setelah baris java -> file baru wajib lebih pendek.
            // v2+: baris yang sedang berjalan sudah membawa "exit" sendiri -> bebas panjang.
            int batasAman = s.contains(PENANDA_UMUM) ? Integer.MAX_VALUE : posisiSetelahBarisJava(s);
            if (batasAman < 0 || isi.length >= batasAman) {
                System.out.println("  [Launcher] launcher baru terlalu panjang untuk diganti aman ("
                        + isi.length + " >= " + batasAman + " byte), dilewati.");
                return;
            }

            File cache = new File(dir, "cache");
            if (!cache.isDirectory()) {
                cache.mkdirs();
            }
            File cadangan = new File(cache, "jalankan-client.bat.v1");
            if (!cadangan.exists()) {
                Files.write(cadangan.toPath(), lama);
            }
            File tmp = new File(dir, "jalankan-client.bat.baru");
            Files.write(tmp.toPath(), isi);
            try {
                Files.move(tmp.toPath(), bat.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception e) {
                Files.move(tmp.toPath(), bat.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("  [Launcher] jalankan-client.bat diperbarui ke launcher terbaru.");
        } catch (Throwable e) {
            System.out.println("  [Launcher] gagal memperbarui launcher: " + e);
        }
    }

    /** Susun isi launcher terkini dari template + nilai yang diambil dari .bat lama. null bila tidak dikenali. */
    static String susun(String lama) throws Exception {
        Matcher server = Pattern.compile("(?im)^\\s*set\\s+\"SERVER=([^\"\\r\\n]+)\"").matcher(lama);
        if (!server.find()) {
            return null;
        }
        Matcher akun = Pattern.compile("(?im)^\\s*net use \"%SERVER%\" /user:(.+?) /persistent:no").matcher(lama);
        if (!akun.find()) {
            return null;
        }
        String java;
        Matcher jc = Pattern.compile("(?im)^\\s*set\\s+\"JAVA_CMD=([^\"\\r\\n]+)\"").matcher(lama);
        if (jc.find()) {
            java = jc.group(1);                         // v3+
        } else {
            java = barisJava(lama);                     // v1/v2: baris "java ... SIMRSKhanza.jar [& exit]"
            if (java == null) {
                return null;
            }
            java = java.replaceAll("(?i)\\s*&\\s*exit\\s*$", "");
        }
        java = java.replaceAll("\\s+-XX:(Max)?PermSize=\\S+", "").trim();
        if (java.contains("\"")) {
            return null;   // baris java dengan tanda kutip tidak bisa dimasukkan ke set "JAVA_CMD=..."; biarkan
        }

        String t = bacaTemplate();
        t = t.replace("@@SERVER@@", server.group(1).trim())
                .replace("@@AKUN@@", akun.group(1).trim())
                .replace("@@JAVA@@", java);
        return t.replace("\r\n", "\n").replace("\n", "\r\n");
    }

    private static String barisJava(String s) {
        String hasil = null;
        for (String b : s.split("\r?\n")) {
            String t = b.trim();
            if (t.toLowerCase().startsWith("java") && t.contains("SIMRSKhanza.jar")) {
                hasil = t;
            }
        }
        return hasil;
    }

    /** Offset byte tepat setelah baris java terakhir (tempat cmd melanjutkan membaca). */
    static int posisiSetelahBarisJava(String s) {
        int i = s.lastIndexOf("SIMRSKhanza.jar");
        if (i < 0) {
            return -1;
        }
        int nl = s.indexOf('\n', i);
        return nl < 0 ? s.length() : nl + 1;
    }

    private static String bacaTemplate() throws Exception {
        try (InputStream in = LauncherUpgrade.class.getResourceAsStream("launcher-client.bat.txt")) {
            if (in == null) {
                throw new IllegalStateException("template launcher-client.bat.txt tidak ada di jar");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.ISO_8859_1);
        }
    }
}
