package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;

/** Local validation only; never changes clinical values or calls an API. */
public final class SatuSehatImmunizationPayload {
    private SatuSehatImmunizationPayload() {}

    /** Operator confirmed this travel-vaccine service records first doses only. */
    public static String confirmedDoseNumber(String vaccineCode) {
        return "93023057".equals(vaccineCode)?"1":"";
    }

    // MENIVAX ACYW in this flow is used for meningococcal travel immunization.
    // Do not apply the special-program classification to unrelated vaccines.
    public static String reasonCode(String vaccineCode) {
        return "93023057".equals(vaccineCode)?"IM-Khusus":"IM-Program";
    }
    public static String reasonDisplay(String vaccineCode) {
        return "93023057".equals(vaccineCode)?"Imunisasi Program Khusus":"Imunisasi Program";
    }

    /** Only issue codes, field paths and rule numbers; never raw response/identities. */
    public static String errorSummary(String body) {
        StringBuilder summary=new StringBuilder();
        try {
            JsonNode outcome=new ObjectMapper().readTree(body);
            for(JsonNode issue:outcome.path("issue")) {
                String code=issue.path("code").asText();
                if(code.matches("[a-z-]{1,40}"))summary.append("code=").append(code).append(' ');
                for(String key:new String[]{"expression","location"})for(JsonNode field:issue.path(key)) {
                    String path=field.asText();
                    if(path.matches("Immunization(?:[.][A-Za-z][A-Za-z0-9]*|\\[[0-9]+\\]){0,30}"))
                        summary.append("field=").append(path).append(' ');
                }
                java.util.regex.Matcher rule=java.util.regex.Pattern.compile("(?i)RuleNumber\\s*[:=]?\\s*([0-9]{1,8})(?![0-9])").matcher(issue.path("details").path("text").asText()+" "+issue.path("diagnostics").asText());
                while(rule.find())summary.append("rule=").append(rule.group(1)).append(' ');
            }
        }catch(Exception ignored){}
        return summary.length()==0?"Rincian field/rule tidak tersedia pada respons; periksa OperationOutcome di monitoring SATUSEHAT.":summary.toString().trim();
    }

    public static String validateAndSerialize(String json,String doseNumber) throws Exception {
        String value=doseNumber==null?"":doseNumber.trim();
        if(!value.matches("[1-9][0-9]{0,9}"))
            throw new IllegalArgumentException("Isi kolom Dosis ke- dengan urutan dosis yang sudah dikonfirmasi, misalnya 1. Bukan volume atau aturan pakai.");
        long number=Long.parseLong(value);
        if(number>Integer.MAX_VALUE)throw new IllegalArgumentException("Urutan dosis terlalu besar.");
        ObjectMapper mapper=new ObjectMapper();
        JsonNode parsed=mapper.readTree(json);
        if(parsed==null||!parsed.isObject())throw new IllegalArgumentException("Payload imunisasi tidak valid.");
        com.fasterxml.jackson.databind.node.ObjectNode payload=(com.fasterxml.jackson.databind.node.ObjectNode)parsed;
        payload.putArray("protocolApplied").addObject().put("doseNumberPositiveInt",(int)number);
        return validateAndSerialize(mapper.writeValueAsString(payload));
    }

    public static String validateAndSerialize(String json) throws Exception {
        ObjectMapper mapper=new ObjectMapper();
        JsonNode payload=mapper.readTree(json);
        if(payload==null||!payload.isObject()||!"Immunization".equals(payload.path("resourceType").asText()))
            throw new IllegalArgumentException("Payload imunisasi tidak valid.");
        String expiry=payload.path("expirationDate").asText();
        if(!expiry.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))
            throw new IllegalArgumentException("Tanggal kedaluwarsa batch vaksin belum tersedia/valid. Lengkapi data batch sesuai kemasan dan pastikan No.Batch, kode barang, serta No.Faktur cocok. Pengiriman ditahan.");
        try {LocalDate.parse(expiry);} catch(java.time.DateTimeException e) {
            throw new IllegalArgumentException("Tanggal kedaluwarsa batch vaksin tidak valid. Periksa data batch; pengiriman ditahan.");
        }
        if(!payload.has("protocolApplied"))throw new IllegalArgumentException("protocolApplied wajib diisi. Isi kolom Dosis ke- berdasarkan catatan imunisasi.");
        {
            JsonNode protocols=payload.path("protocolApplied");
            if(!protocols.isArray()||protocols.size()==0)throw new IllegalArgumentException("Protokol imunisasi tidak valid.");
            for(JsonNode protocol:protocols){
                JsonNode number=protocol.path("doseNumberPositiveInt");
                boolean integer=number.isIntegralNumber()&&number.canConvertToInt()&&number.asInt()>0;
                JsonNode text=protocol.path("doseNumberString");
                boolean string=text.isTextual()&&!text.asText().trim().isEmpty();
                if(integer==string||protocol.has("doseNumberPositiveInt")&&!integer||protocol.has("doseNumberString")&&!string)
                    throw new IllegalArgumentException("Urutan dosis harus berasal dari catatan imunisasi yang valid, bukan aturan pakai obat.");
            }
        }
        return mapper.writeValueAsString(payload);
    }
}
