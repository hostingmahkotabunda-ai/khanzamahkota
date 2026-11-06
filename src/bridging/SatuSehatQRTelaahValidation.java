package bridging;

/** Validasi referensi QR Telaah sebelum autentikasi/pengiriman resource. */
public final class SatuSehatQRTelaahValidation {
    private SatuSehatQRTelaahValidation(){}
    public static void requireReferences(String patient,String practitioner,String encounter){
        if(!valid(patient))throw new IllegalArgumentException("ID Patient belum ditemukan/tidak valid. Periksa identitas pasien; QR Telaah belum dikirim.");
        if(!valid(practitioner))throw new IllegalArgumentException("ID Practitioner petugas telaah belum ditemukan/tidak valid. Periksa NIK pada master pegawai dan hasil pencarian Practitioner SATUSEHAT. QR Telaah belum dikirim; jangan mengganti petugas dengan identitas orang lain.");
        if(!valid(encounter))throw new IllegalArgumentException("ID Encounter belum tersedia/tidak valid. Selesaikan pengiriman Encounter yang sesuai sebelum QR Telaah.");
    }
    private static boolean valid(String id){return id!=null&&id.matches("[A-Za-z0-9.-]{1,64}");}
}
