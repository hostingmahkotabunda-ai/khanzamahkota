package bridging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.*;

/** Payload POST/PUT bersama: satu code, escaping Jackson, dan waktu WITA ke UTC. */
public final class SatuSehatAllergyPayload {
    private SatuSehatAllergyPayload(){}
    public static ObjectNode build(ObjectMapper mapper,String organization,String identifier,String patient,
            String encounter,String recorder,String recordedDate,String originalText,SatuSehatAllergyMapping.Mapping mapping,String id){
        return build(mapper,organization,identifier,patient,encounter,recorder,recordedDate,originalText,mapping,id,"unconfirmed");
    }
    public static ObjectNode build(ObjectMapper mapper,String organization,String identifier,String patient,
            String encounter,String recorder,String recordedDate,String originalText,SatuSehatAllergyMapping.Mapping mapping,String id,String verification){
        mapping.validate();SatuSehatEncounterIdentity.requireIds(patient,recorder);
        if(!"unconfirmed".equals(verification)&&!"confirmed".equals(verification))throw new IllegalArgumentException("Pilih status verifikasi alergi per baris.");
        if("confirmed".equals(verification)&&SatuSehatAllergySearchTerm.suspected(originalText))throw new IllegalArgumentException("Catatan masih menyebut dugaan/curiga. Tinjau catatan klinis; jangan mengirimnya sebagai confirmed.");
        if(organization==null||!organization.matches("[A-Za-z0-9.-]{1,64}"))throw new IllegalArgumentException("ID organisasi lokal belum valid.");
        if(encounter==null||!encounter.matches("[A-Za-z0-9.-]{1,64}"))throw new IllegalArgumentException("ID Encounter belum valid.");
        ObjectNode root=mapper.createObjectNode();root.put("resourceType","AllergyIntolerance");
        if(id!=null&&!id.isEmpty()){
            if(!id.matches("[A-Za-z0-9.-]{1,64}"))throw new IllegalArgumentException("ID AllergyIntolerance tidak valid.");root.put("id",id);
        }
        root.putArray("identifier").addObject().put("system","http://sys-ids.kemkes.go.id/allergy/"+organization).put("value",identifier);
        root.putObject("clinicalStatus").putArray("coding").addObject().put("system","http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical").put("code","active").put("display","Active");
        root.putObject("verificationStatus").putArray("coding").addObject().put("system","http://terminology.hl7.org/CodeSystem/allergyintolerance-verification").put("code",verification).put("display","confirmed".equals(verification)?"Confirmed":"Unconfirmed");
        root.putArray("category").add(mapping.category);
        ObjectNode code=root.putObject("code");code.putArray("coding").addObject().put("system",mapping.system).put("code",mapping.code).put("display",mapping.display);
        code.put("text",mapping.partial?"Alergi "+mapping.display:mapping.text);
        root.putObject("patient").put("reference","Patient/"+patient);
        root.putObject("encounter").put("reference","Encounter/"+encounter);
        Instant instant;
        if(recordedDate==null||recordedDate.trim().isEmpty())throw new IllegalArgumentException("Waktu pencatatan alergi wajib diisi.");
        String time=recordedDate.trim().replace(' ','T');
        try{
            try{instant=OffsetDateTime.parse(time).toInstant();}
            catch(java.time.format.DateTimeParseException e){instant=LocalDateTime.parse(time).atZone(ZoneId.of("Asia/Makassar")).toInstant();}
        }catch(DateTimeException e){throw new IllegalArgumentException("Format waktu pencatatan alergi tidak valid. Perbaiki catatan sumber tanpa mengganti tanggal pelayanan.");}
        root.put("recordedDate",instant.toString());
        root.putObject("recorder").put("reference","Practitioner/"+recorder);
        // Simpan teks sumber utuh termasuk reaksi/karakter khusus.
        root.putArray("note").addObject().put("text",originalText);
        if(mapping.partial)((com.fasterxml.jackson.databind.node.ArrayNode)root.path("note")).addObject().put("text",
                "Mapping sebagian: kode hanya untuk "+mapping.display+". Zat lain yang disebut pada catatan sumber belum terpetakan.");
        return root;
    }
}
