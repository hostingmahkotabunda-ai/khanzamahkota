package bridging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.*;
import java.time.format.DateTimeFormatter;

/** Shared POST/PUT laboratory narrative payload; no database or network access. */
public final class SatuSehatLabObservationPayload {
    private SatuSehatLabObservationPayload(){}
    public static ObjectNode build(ObjectMapper mapper,String organization,String order,String kind,String template,
            String system,String code,String display,String patient,String practitioner,String encounter,
            String specimen,String resultTime,String result,String resourceId){
        requireId(organization);requireId(patient);requireId(practitioner);requireId(encounter);requireId(specimen);
        if(order==null||order.trim().isEmpty()||kind==null||kind.trim().isEmpty()||template==null||template.trim().isEmpty())throw new IllegalArgumentException("Identitas item hasil lab belum lengkap.");
        if(system==null||system.trim().isEmpty()||code==null||code.trim().isEmpty())throw new IllegalArgumentException("Mapping kode hasil lab belum lengkap.");
        if(result==null||result.trim().isEmpty())throw new IllegalArgumentException("Hasil lab belum tersedia.");
        String time=utc(resultTime);
        ObjectNode node=mapper.createObjectNode();node.put("resourceType","Observation");
        if(resourceId!=null&&!resourceId.isEmpty()){requireId(resourceId);node.put("id",resourceId);}
        ObjectNode identifier=node.putArray("identifier").addObject();
        identifier.put("system","http://sys-ids.kemkes.go.id/observation/"+organization);
        // Length prefixes keep different item keys distinct, even when keys contain separators.
        identifier.put("value",part(order)+part(kind)+part(template));
        node.put("status","final");
        ObjectNode category=node.putArray("category").addObject().putArray("coding").addObject();
        category.put("system","http://terminology.hl7.org/CodeSystem/observation-category");category.put("code","laboratory");category.put("display","Laboratory");
        ObjectNode coding=node.putObject("code").putArray("coding").addObject();coding.put("system",system);coding.put("code",code);coding.put("display",display==null?"":display);
        node.putObject("subject").put("reference","Patient/"+patient);
        node.putArray("performer").addObject().put("reference","Practitioner/"+practitioner);
        node.putObject("encounter").put("reference","Encounter/"+encounter);
        node.putObject("specimen").put("reference","Specimen/"+specimen);
        node.put("effectiveDateTime",time);
        // Current source is the recorded result time, not the computer clock.
        node.put("issued",time);
        node.put("valueString",result);
        return node;
    }
    private static String part(String value){return value.length()+":"+value;}
    private static void requireId(String value){if(value==null||!value.matches("[A-Za-z0-9.-]{1,64}"))throw new IllegalArgumentException("Referensi/organisasi hasil lab belum valid.");}
    public static String utc(String value){
        if(value==null||value.trim().isEmpty())throw new IllegalArgumentException("Waktu hasil lab belum tersedia.");
        String normalized=value.trim().replace(' ','T');Instant instant;
        try{instant=OffsetDateTime.parse(normalized).toInstant();}
        catch(java.time.format.DateTimeParseException e){instant=LocalDateTime.parse(normalized).atZone(ZoneId.of("Asia/Makassar")).toInstant();}
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }
}
