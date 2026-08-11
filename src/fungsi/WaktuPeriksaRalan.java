package fungsi;

/** Pencatat waktu pertama kali kunjungan rawat jalan ditandai sudah diperiksa. */
public final class WaktuPeriksaRalan {
    private static final sekuel Sequel = new sekuel();
    private static boolean tabelDipastikan = false;

    private WaktuPeriksaRalan() {
    }

    public static synchronized void pastikanTabel() {
        if (tabelDipastikan) {
            return;
        }
        try {
            Sequel.queryu("create table if not exists waktu_sudah_periksa_ralan ("
                    + "no_rawat varchar(17) not null,"
                    + "waktu_sudah datetime not null default current_timestamp,"
                    + "petugas varchar(50) not null default '',"
                    + "sumber varchar(50) not null default '',"
                    + "primary key (no_rawat),"
                    + "key waktu_sudah (waktu_sudah)"
                    + ") engine=InnoDB default charset=latin1");
            tabelDipastikan = true;
        } catch (Exception e) {
            System.out.println("Notifikasi pastikan tabel waktu sudah periksa ralan : " + e);
        }
    }

    /** Simpan hanya klik Sudah yang pertama agar waktu tunggu tidak berubah saat status diulang. */
    public static void catat(String noRawat, String sumber) {
        if (noRawat == null || noRawat.trim().equals("")) {
            return;
        }
        String noRawatAman = noRawat.replace("'", "");
        if (!"Sudah".equals(Sequel.cariIsi(
                "select stts from reg_periksa where no_rawat='" + noRawatAman
                + "' and status_lanjut='Ralan'"))) {
            return;
        }
        pastikanTabel();
        try {
            Sequel.queryu("insert ignore into waktu_sudah_periksa_ralan "
                    + "(no_rawat,waktu_sudah,petugas,sumber) values ('"
                    + noRawatAman + "',now(),'"
                    + akses.getkode().replace("'", "") + "','"
                    + sumber.replace("'", "") + "')");
        } catch (Exception e) {
            System.out.println("Notifikasi catat waktu sudah periksa ralan : " + e);
        }
    }
}
