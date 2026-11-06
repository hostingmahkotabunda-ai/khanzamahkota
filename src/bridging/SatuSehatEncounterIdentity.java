package bridging;

/** Memblokir referensi FHIR kosong/tidak valid sebelum POST/PUT Encounter. */
public final class SatuSehatEncounterIdentity {
    private SatuSehatEncounterIdentity(){}
    public static void requireIds(String patientId,String practitionerId){
        if(!valid(patientId))throw new IllegalArgumentException(
                "ID Patient SATUSEHAT belum ditemukan atau tidak valid. Periksa NIK pada master pasien dan pencarian Patient; Encounter belum dikirim.");
        if(!valid(practitionerId))throw new IllegalArgumentException(
                "ID Practitioner SATUSEHAT belum ditemukan atau tidak valid. Periksa identitas dokter dan pencarian Practitioner; Encounter belum dikirim.");
    }
    private static boolean valid(String value){return value!=null&&value.matches("[A-Za-z0-9.-]{1,64}");}
}
