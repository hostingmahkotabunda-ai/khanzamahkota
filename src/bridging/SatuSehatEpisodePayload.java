package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Serialisasi/validasi EpisodeOfCare murni, tanpa koneksi database atau API. */
public final class SatuSehatEpisodePayload {
    public static final ZoneId HOSPITAL_ZONE=ZoneId.of("Asia/Makassar");
    public static final Instant MINIMUM=Instant.parse("2014-06-03T00:00:00Z");
    private SatuSehatEpisodePayload() {}

    public static String identifierSystem(String organization) {
        if(organization==null||!organization.matches("[A-Za-z0-9.-]{1,64}"))
            throw new IllegalArgumentException("Organization ID konfigurasi tidak valid.");
        return "http://sys-ids.kemkes.go.id/episode-of-care/"+organization;
    }

    public static String dateTime(String input,String field,Clock clock) {
        Instant instant=parse(input,field);
        if(instant.isBefore(MINIMUM))throw new IllegalArgumentException(field+": sebelum batas 03-06-2014 UTC.");
        if(instant.isAfter(clock.instant()))throw new IllegalArgumentException(field+": melewati waktu komputer; periksa tanggal dan sinkronisasi jam server.");
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }

    private static Instant parse(String input,String field) {
        if(input==null||input.trim().isEmpty())throw new IllegalArgumentException(field+": tanggal/waktu wajib diisi.");
        String value=input.trim().replace(' ','T');
        try {
            // Untuk data klinis DATE tanpa jam, midnight UTC adalah konvensi serialisasi
            // tanggal (sesuai contoh SATUSEHAT), bukan klaim jam kejadian yang diketahui.
            if(value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))return LocalDate.parse(value).atStartOfDay(ZoneOffset.UTC).toInstant();
            try{return OffsetDateTime.parse(value).toInstant();}
            catch(DateTimeParseException noOffset){return LocalDateTime.parse(value).atZone(HOSPITAL_ZONE).toInstant();}
        } catch(DateTimeException e){throw new IllegalArgumentException(field+": format tanggal/waktu tidak valid; gunakan yyyy-MM-dd atau datetime ISO berzona.",e);}
    }

    public static String clinicalDate(String value) {
        if(value!=null&&value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))return LocalDate.parse(value).toString();
        return parse(value,"tanggal klinis").atZone(HOSPITAL_ZONE).toLocalDate().toString();
    }

    private static void period(ObjectNode root,String start,String end,Clock clock) {
        ObjectNode p=root.with("period");
        p.put("start",dateTime(start,"EpisodeOfCare.period.start",clock));
        if(end!=null&&!end.trim().isEmpty())p.put("end",dateTime(end,"EpisodeOfCare.period.end",clock));
        else p.remove("end");
    }

    public static ObjectNode post(ObjectMapper mapper,String organization,String patient,String identifier,
            String type,String display,String status,String start,String end,String condition,Clock clock) {
        ObjectNode root=mapper.createObjectNode();root.put("resourceType","EpisodeOfCare");root.put("status",status);
        setIdentifier(root,organization,identifier);
        root.putArray("type").addObject().putArray("coding").addObject()
                .put("system",SatuSehatEpisodeSupport.TYPE_SYSTEM).put("code",type).put("display",display);
        root.putObject("patient").put("reference","Patient/"+patient);
        root.putObject("managingOrganization").put("reference","Organization/"+organization);
        if(condition!=null&&!condition.isEmpty()){
            ObjectNode diagnosis=root.putArray("diagnosis").addObject();
            diagnosis.putObject("condition").put("reference","Condition/"+condition);
            diagnosis.putObject("role").putArray("coding").addObject().put("system","http://terminology.hl7.org/CodeSystem/diagnosis-role")
                    .put("code","AD").put("display","Admission diagnosis");diagnosis.put("rank",1);
        }
        period(root,start,end,clock);
        ObjectNode history=root.putArray("statusHistory").addObject();history.put("status",status);
        ObjectNode hp=history.putObject("period");hp.put("start",root.path("period").path("start").asText());
        if(root.path("period").has("end"))hp.put("end",root.path("period").path("end").asText());
        validate(root,clock);return root;
    }

    public static void setIdentifier(ObjectNode root,String organization,String value) {
        if(value==null||value.trim().isEmpty())throw new IllegalArgumentException("Identifier lokal episode belum tersedia.");
        String system=identifierSystem(organization);
        ArrayNode ids=root.withArray("identifier");boolean found=false;
        for(JsonNode id:ids){
            String old=id.path("system").asText();
            if(old.equals(system)||old.equals(system+"/")||old.equals("http://sys-ids.kemkes.go.id/episodeofcare/"+organization)
                    ||old.equals("http://sys-ids.kemkes.go.id/episodeofcare/"+organization+"/")){
                ((ObjectNode)id).put("system",system);found=true;
            }
        }
        if(!found)ids.addObject().put("system",system).put("use","official").put("value",value);
    }

    public static void update(ObjectNode root,String organization,String identifier,String status,String start,String end,Clock clock) {
        String previous=root.path("status").asText();
        // Skema lokal DATE tidak menyimpan jam. Bila tanggal operator tetap sama,
        // pertahankan waktu yang sudah diketahui dari resource GET, jangan timpa menjadi midnight.
        start=preserveKnownTime(start,root.path("period").path("start").asText());
        end=preserveKnownTime(end,root.path("period").path("end").asText());
        setIdentifier(root,organization,identifier);period(root,start,end,clock);
        ArrayNode history=root.withArray("statusHistory");
        // Normalisasi presisi tanggal lama tanpa mengubah instant datetime yang sudah ada.
        for(int i=0;i<history.size();i++){
            JsonNode p=history.get(i).path("period");
            if(!p.isObject())throw new IllegalArgumentException("EpisodeOfCare.statusHistory["+i+"].period tidak tersedia.");
            ObjectNode hp=(ObjectNode)p;
            hp.put("start",dateTime(hp.path("start").asText(),"EpisodeOfCare.statusHistory["+i+"].period.start",clock));
            if(hp.has("end"))hp.put("end",dateTime(hp.path("end").asText(),"EpisodeOfCare.statusHistory["+i+"].period.end",clock));
        }
        if(history.size()==0){
            ObjectNode h=history.addObject();h.put("status",status);h.put("period",root.path("period").deepCopy());
        }else if(!status.equals(previous)){
            String transition=root.path("period").has("end")?root.path("period").path("end").asText():clock.instant().toString();
            ObjectNode last=(ObjectNode)history.get(history.size()-1).path("period");
            if(!last.has("end"))last.put("end",transition);
            ObjectNode h=history.addObject();h.put("status",status);ObjectNode hp=h.putObject("period");hp.put("start",transition);
            if(root.path("period").has("end"))hp.put("end",transition);
        }
        root.put("status",status);validate(root,clock);
    }

    private static String preserveKnownTime(String input,String existing) {
        if(input!=null&&input.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")&&existing!=null&&existing.contains("T")
                &&input.equals(clinicalDate(existing)))return existing;
        return input;
    }

    public static void validate(JsonNode root,Clock clock) {
        Instant episodeStart=validatePeriod(root.path("period"),"EpisodeOfCare.period",clock);
        Instant episodeEnd=root.path("period").has("end")?parse(root.path("period").path("end").asText(),"EpisodeOfCare.period.end"):null;
        Instant previousEnd=null;
        for(int i=0;i<root.path("statusHistory").size();i++){
            JsonNode p=root.path("statusHistory").get(i).path("period");String field="EpisodeOfCare.statusHistory["+i+"].period";
            Instant begin=validatePeriod(p,field,clock);
            if(begin.isBefore(episodeStart)||(episodeEnd!=null&&begin.isAfter(episodeEnd)))throw new IllegalArgumentException(field+".start di luar periode episode.");
            if(i>0&&(previousEnd==null||begin.isBefore(previousEnd)))throw new IllegalArgumentException(field+": urutan status tumpang tindih atau status sebelumnya belum ditutup.");
            previousEnd=p.has("end")?parse(p.path("end").asText(),field+".end"):null;
            if(episodeEnd!=null&&previousEnd!=null&&previousEnd.isAfter(episodeEnd))throw new IllegalArgumentException(field+".end melewati akhir episode.");
        }
    }

    private static Instant validatePeriod(JsonNode p,String field,Clock clock) {
        String start=dateTime(p.path("start").asText(),field+".start",clock);Instant begin=Instant.parse(start);
        if(p.has("end")&&Instant.parse(dateTime(p.path("end").asText(),field+".end",clock)).isBefore(begin))
            throw new IllegalArgumentException(field+".end sebelum start; periksa tanggal klinis.");
        return begin;
    }
}
