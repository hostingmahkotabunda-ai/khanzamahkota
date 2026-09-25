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

    public static final String SUMBER_TOMBOL = "Tombol Waktu Tunggu";

    /**
     * Ekspresi SQL "waktu selesai menunggu" utk 1 baris reg_periksa (alias regAlias).
     * Kunjungan yg didaftarkan pada/sesudah hari KLIK TOMBOL pertama di DlgRawatJalan hanya
     * memakai waktu tombol itu (belum diklik = NULL -> tampil "-"). Kunjungan sebelumnya tetap
     * memakai aturan lama: SOAP pertama, lalu klik status Sudah pertama.
     */
    public static String sqlWaktuSelesai(String regAlias) {
        String r = regAlias;
        return "coalesce((select w1.waktu_sudah from waktu_sudah_periksa_ralan w1 where w1.no_rawat=" + r
                + ".no_rawat and w1.sumber='" + SUMBER_TOMBOL + "'),"
                + "case when " + r + ".tgl_registrasi<ifnull((select date(min(w2.waktu_sudah)) from waktu_sudah_periksa_ralan w2 "
                + "where w2.sumber='" + SUMBER_TOMBOL + "'),'9999-12-31') then "
                + "coalesce((select min(timestamp(pr.tgl_perawatan,pr.jam_rawat)) from pemeriksaan_ralan pr where pr.no_rawat=" + r + ".no_rawat),"
                + "(select w3.waktu_sudah from waktu_sudah_periksa_ralan w3 where w3.no_rawat=" + r + ".no_rawat)) end)";
    }

    /** Waktu selesai menunggu utk 1 no_rawat (aturan sama dgn sqlWaktuSelesai), "" kalau belum ada. */
    public static String waktuSelesai(String noRawat) {
        String v = Sequel.cariIsi("select " + sqlWaktuSelesai("reg_periksa")
                + " from reg_periksa where reg_periksa.no_rawat=?", noRawat);
        return v == null ? "" : v;
    }

    /** Waktu yg tercatat lewat tombol utk no_rawat ini, "" kalau tombol belum pernah diklik. */
    public static String waktuTombol(String noRawat) {
        pastikanTabel();
        String v = Sequel.cariIsi("select waktu_sudah from waktu_sudah_periksa_ralan where no_rawat=? and sumber='"
                + SUMBER_TOMBOL + "'", noRawat);
        return v == null ? "" : v;
    }

    /**
     * Catat waktu SEKARANG sbg akhir waktu tunggu. Hanya klik pertama yg berlaku (return false
     * kalau sudah pernah dicatat lewat tombol). Baris lama utk no_rawat yg sama (dari klik status
     * Sudah, dsb) ditimpa krn primary key-nya no_rawat.
     */
    public static boolean catatDariTombol(String noRawat) {
        if (noRawat == null || noRawat.trim().equals("")) {
            return false;
        }
        if (!waktuTombol(noRawat).equals("")) {
            return false;
        }
        return Sequel.queryu2tf("insert into waktu_sudah_periksa_ralan (no_rawat,waktu_sudah,petugas,sumber) values (?,now(),?,?) "
                + "on duplicate key update waktu_sudah=if(sumber=values(sumber),waktu_sudah,values(waktu_sudah)),"
                + "petugas=if(sumber=values(sumber),petugas,values(petugas)),sumber=values(sumber)",
                3, new String[]{noRawat, akses.getkode(), SUMBER_TOMBOL});
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
