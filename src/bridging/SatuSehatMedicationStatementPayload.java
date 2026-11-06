package bridging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;

/** Riwayat pemakaian obat; tidak menyimpulkan pemakaian dari resep. */
public final class SatuSehatMedicationStatementPayload {
    public static final String PATIENT_SOURCE = "Pasien sendiri";
    private SatuSehatMedicationStatementPayload() {}

    public static String key(String reconciliation, String medicine) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(
                (reconciliation + "\u0000" + medicine).getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : hash) result.append(String.format(Locale.ROOT, "%02x", b & 255));
        return result.toString();
    }

    public static String utc(String value) {
        return LocalDateTime.parse(value.replace(' ', 'T'), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .atZone(ZoneId.of("Asia/Makassar")).toInstant().toString();
    }

    public static void validate(String status, String mode, String medicine, String display) {
        if (!Arrays.asList("active", "completed", "stopped", "on-hold", "not-taken", "unknown").contains(status))
            throw new IllegalArgumentException("Pilih status penggunaan berdasarkan catatan klinis.");
        if ("KFA (obat luar faskes)".equals(mode)) {
            if (!medicine.matches("9[23][0-9]{6}") || display.trim().isEmpty())
                throw new IllegalArgumentException("Isi kode KFA 92/93 (8 digit) dan nama obat sesuai KFA.");
        } else if ("Medication (faskes ini)".equals(mode)) {
            if (!medicine.matches("Medication/[A-Za-z0-9.-]{1,64}"))
                throw new IllegalArgumentException("Isi referensi Medication/ID dari riwayat obat faskes ini.");
        } else throw new IllegalArgumentException("Pilih sumber obat yang sesuai.");
    }

    public static void validateInformationSource(String source) {
        if(source==null || !(PATIENT_SOURCE.equals(source)
                || source.matches("(Patient|Practitioner|PractitionerRole|RelatedPerson|Organization)/[A-Za-z0-9.-]{1,64}")))
            throw new IllegalArgumentException("Isi Sumber Informasi: pilih Pasien sendiri jika pasien yang memberikan riwayat, atau isi referensi pemberi informasi (mis. Practitioner/ID atau RelatedPerson/ID).");
    }

    public static ObjectNode build(ObjectMapper mapper, String organization, String identifier,
            String patient, String encounter, String asserted, String status, String mode,
            String medicine, String display, String dosage, String note, String informationSource) {
        validate(status, mode, medicine, display);
        validateInformationSource(informationSource);
        if (!patient.matches("Patient/[A-Za-z0-9.-]{1,64}") || !encounter.matches("Encounter/[A-Za-z0-9.-]{1,64}"))
            throw new IllegalArgumentException("Referensi pasien/encounter belum valid.");
        ObjectNode root = mapper.createObjectNode();
        root.put("resourceType", "MedicationStatement");
        ObjectNode id = root.putArray("identifier").addObject();
        id.put("system", "http://sys-ids.kemkes.go.id/medicationstatement/" + organization);
        id.put("value", identifier);
        root.put("status", status);
        if ("Medication (faskes ini)".equals(mode)) {
            root.putObject("medicationReference").put("reference", medicine);
        } else {
            ObjectNode coding = root.putObject("medicationCodeableConcept").putArray("coding").addObject();
            coding.put("system", "http://sys-ids.kemkes.go.id/kfa");
            coding.put("code", medicine);
            coding.put("display", display);
        }
        root.putObject("subject").put("reference", patient);
        root.putObject("context").put("reference", encounter);
        root.putObject("informationSource").put("reference", PATIENT_SOURCE.equals(informationSource) ? patient : informationSource);
        // Waktu wawancara adalah waktu pernyataan, bukan perkiraan awal konsumsi obat.
        root.put("dateAsserted", utc(asserted));
        if (!dosage.trim().isEmpty()) root.putArray("dosage").addObject().put("text", dosage);
        if (!note.trim().isEmpty()) root.putArray("note").addObject().put("text", note);
        return root;
    }
}
