package bridging;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.*;

/** Pure R4 serialization and review validation. No database, network, or guessed clinical values. */
public final class SatuSehatNutritionOrderPayload {
    public static final ObjectMapper JSON=new ObjectMapper();
    public static final ZoneId ZONE=ZoneId.of("Asia/Makassar");
    public static final String SCT="http://snomed.info/sct";
    public static final String KEMKES="http://terminology.kemkes.go.id/CodeSystem/clinical-term";
    private static final DateTimeFormatter LOCAL=DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss").withResolverStyle(ResolverStyle.STRICT);
    private SatuSehatNutritionOrderPayload(){}

    public static final class Concept {
        public final String local,label,system,code,display;
        public final boolean direct;
        Concept(String local,String label,String system,String code,String display,boolean direct){
            this.local=local;this.label=label;this.system=system;this.code=code;this.display=display;this.direct=direct;
        }
        public ObjectNode json(){ObjectNode n=JSON.createObjectNode();n.put("local",local);n.put("label",label);n.put("system",system);n.put("code",code);n.put("display",display);n.put("confirmed",direct);return n;}
        @Override public String toString(){return local+" - "+label+(direct?"":" (perlu ditinjau)");}
    }
    private static Concept c(String local,String label,String sys,String code,String display,boolean direct){return new Concept(local,label,sys,code,display,direct);}
    /** Sources: official SATUSEHAT Gizi tables 19/20, checked 2026-11-13. */
    public static final List<Concept> DIETS=Collections.unmodifiableList(Arrays.asList(
        c("TKTP","Tinggi kalori tinggi protein",SCT,"1055200005","Increased protein and/or protein derivative diet",false),
        c("Tprot","Tinggi protein","","","",false),c("RG","Rendah garam",SCT,"386619000","Low salt diet",true),
        c("RS","Rendah serat","","","",false),c("RL","Rendah lemak",SCT,"1055207008","Decreased fat and oil diet",true),
        c("RP","Rendah protein (perlu jumlah gram)","","","",false),
        c("Rpur","Rendah purin",SCT,"22745007","Low purine diet",true),
        c("DM","Diabetes melitus",KEMKES,"OV000370","Diet Diabetes Melitus (DM)",true),
        c("TS","Tinggi serat","","","",false),c("JANTUNG","Diet Jantung",KEMKES,"OV000380","Diet Jantung",true),
        c("HATI","Diet Hati",KEMKES,"OV000372","Diet Hati (DH)",true),
        c("RP20-40","Rendah protein 20-40 g",KEMKES,"OV000374","Diet Rendah Protein (RP/protein 20 - 40 g)",true),
        c("RP50","Rendah protein 50 g",KEMKES,"OV000375","Diet Rendah Protein (RP/protein 50 g)",true),
        c("RP60","Rendah protein 60 g",KEMKES,"OV000376","Diet Rendah Protein (RP/protein 60 g)",true)));
    public static final List<Concept> TEXTURES=Collections.unmodifiableList(Arrays.asList(
        c("NB","Nasi Biasa",SCT,"228060008","Solid food",false),
        c("NL","Nasi Lembek",SCT,"228059003","Soft food",false),
        c("BB","Bubur",SCT,"228059003","Soft food",false),
        c("BS","Bubur Saring",SCT,"441791000124106","Strained food",false),
        c("MCJ","Makanan Cair Jernih","","","",false),c("MCP","Makanan Cair Penuh","","","",false)));

    public static String hash(String text){try{byte[] bytes=MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));StringBuilder out=new StringBuilder();for(byte b:bytes)out.append(String.format("%02x",b&255));return out.toString();}catch(Exception e){throw new IllegalStateException("Tidak dapat menghitung sidik data.");}}
    public static String sourceKey(String visit,String date){return hash(visit+"\n"+date);}
    public static OffsetDateTime time(String text){
        try{String t=text.trim();return t.contains("T")?OffsetDateTime.parse(t):LocalDateTime.parse(t,LOCAL).atZone(ZONE).toOffsetDateTime();}
        catch(Exception e){throw new IllegalArgumentException("Gunakan tanggal dan jam yyyy-MM-dd HH:mm:ss (WITA), atau datetime dengan zona waktu.");}
    }
    public static String ref(String type,String id){if(id==null||!id.matches("[A-Za-z0-9.-]{1,64}"))throw new IllegalArgumentException("Referensi "+type+" belum valid.");return type+"/"+id;}
    /** OffsetDateTime.toString omits zero seconds; FHIR R4 requires them when a time is present. */
    public static String fhirTime(String value){OffsetDateTime date=time(value);int offset=date.getOffset().getTotalSeconds();if(Math.abs(offset)>14*3600||offset%60!=0)throw new IllegalArgumentException("Zona waktu datetime tidak sesuai format FHIR R4.");return date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);}
    private static String required(JsonNode n,String key,String label){JsonNode field=n.path(key);String v=field.isNull()?"":field.asText().trim();if(v.isEmpty())throw new IllegalArgumentException(label+" belum diisi.");return v;}
    public static void validateReview(JsonNode r,Clock clock){
        String intent=required(r,"intent","Tujuan pencatatan");
        if(!Arrays.asList("proposal","order").contains(intent))throw new IllegalArgumentException("Pilih rekomendasi atau instruksi diet.");
        String status=required(r,"status","Status instruksi");
        if(!Arrays.asList("draft","active","on-hold","revoked","completed","entered-in-error").contains(status))throw new IllegalArgumentException("Status NutritionOrder tidak valid.");
        required(r,"doctor","Dokter pemberi instruksi");required(r,"instruction","Instruksi diet");
        if(!r.path("reviewed").asBoolean(false))throw new IllegalArgumentException("Petugas perlu mengonfirmasi instruksi dan dokter pemberi instruksi.");
        OffsetDateTime issued=time(required(r,"issued","Waktu instruksi dibuat")),start=time(required(r,"start","Waktu mulai berlaku"));
        if(issued.toLocalDate().isBefore(LocalDate.of(2014,6,3))||issued.toInstant().isAfter(clock.instant()))throw new IllegalArgumentException("Waktu instruksi dibuat harus sesuai catatan, sejak 3 Juni 2014 dan tidak di masa depan.");
        if(start.toInstant().isBefore(issued.toInstant()))throw new IllegalArgumentException("Mulai berlaku sebelum instruksi dibuat; periksa waktu pada dokumentasi.");
        String end=r.path("end").isNull()?"":r.path("end").asText().trim();if(!end.isEmpty()&&time(end).toInstant().isBefore(start.toInstant()))throw new IllegalArgumentException("Waktu akhir sebelum mulai berlaku.");
        if((status.equals("revoked")||status.equals("completed"))&&end.isEmpty())throw new IllegalArgumentException("Isi waktu akhir sesuai penghentian/penyelesaian instruksi.");
        if(!end.isEmpty()&&(status.equals("revoked")||status.equals("completed"))&&time(end).toInstant().isAfter(clock.instant()))throw new IllegalArgumentException("Waktu penghentian/selesai belum terjadi.");
        JsonNode diets=r.path("diets");if(!diets.isArray()||diets.size()==0)throw new IllegalArgumentException("Pilih jenis diet sesuai instruksi.");
        for(JsonNode diet:diets)validateConcept(diet);
        if(r.has("texture")&&!r.path("texture").isNull())validateConcept(r.path("texture"));
    }
    public static void validateConcept(JsonNode n){
        String system=required(n,"system","Sistem terminologi"),code=required(n,"code","Kode diet/bentuk makanan");required(n,"display","Display resmi");
        if(!Arrays.asList(SCT,KEMKES).contains(system)||!code.matches("[A-Za-z0-9.-]{1,64}"))throw new IllegalArgumentException("Terminologi diet/bentuk makanan belum valid.");
        if(!n.path("confirmed").asBoolean(false))throw new IllegalArgumentException("Mapping "+n.path("local").asText()+" perlu ditinjau dan dikonfirmasi.");
    }
    private static ObjectNode coding(JsonNode c){ObjectNode concept=JSON.createObjectNode();ObjectNode coding=concept.putArray("coding").addObject();for(String key:new String[]{"system","code","display"})coding.put(key,c.path(key).asText());if(!c.path("label").asText().isEmpty())concept.put("text",c.path("label").asText());return concept;}
    public static ObjectNode build(JsonNode review,String patient,String encounter,String doctor,Clock clock){
        validateReview(review,clock);if("draft".equals(review.path("status").asText()))throw new IllegalArgumentException("Instruksi masih draft; tinjau status sebelum pengiriman.");
        ObjectNode p=JSON.createObjectNode();p.put("resourceType","NutritionOrder");p.put("status",review.path("status").asText());p.put("intent",review.path("intent").asText());
        p.putObject("patient").put("reference",ref("Patient",patient));p.putObject("encounter").put("reference",ref("Encounter",encounter));p.putObject("orderer").put("reference",ref("Practitioner",doctor));
        p.put("dateTime",fhirTime(review.path("issued").asText()));ObjectNode diet=p.putObject("oralDiet");ArrayNode types=diet.putArray("type");for(JsonNode c:review.path("diets"))types.add(coding(c));
        if(review.has("texture")&&!review.path("texture").isNull())diet.putArray("texture").addObject().set("modifier",coding(review.path("texture")));
        ObjectNode period=diet.putArray("schedule").addObject().putObject("repeat").putObject("boundsPeriod");period.put("start",fhirTime(review.path("start").asText()));
        if(!review.path("end").isNull()&&!review.path("end").asText().trim().isEmpty())period.put("end",fhirTime(review.path("end").asText()));
        String text=review.path("instruction").asText();
        if(review.has("texture")&&!review.path("texture").isNull())text+="\nBentuk makanan: "+review.path("texture").path("label").asText();
        diet.put("instruction",text);return p;
    }
    public static void verifyUpdate(JsonNode payload,JsonNode remote){
        if(!"NutritionOrder".equals(remote.path("resourceType").asText()))throw new IllegalArgumentException("Resource server bukan NutritionOrder.");
        for(String key:new String[]{"patient","encounter","orderer"})if(!payload.path(key).path("reference").asText().equals(remote.path(key).path("reference").asText()))throw new IllegalArgumentException("Referensi "+key+" berbeda; perlu ditinjau.");
        if(!payload.path("intent").equals(remote.path("intent")))throw new IllegalArgumentException("Intent berubah; buat instruksi baru, bukan menimpa rekomendasi.");
        if(!time(payload.path("dateTime").asText()).toInstant().equals(time(remote.path("dateTime").asText()).toInstant()))throw new IllegalArgumentException("Waktu instruksi server berbeda.");
        if(!conceptSignatures(payload.path("oralDiet").path("type"),false).equals(conceptSignatures(remote.path("oralDiet").path("type"),false))||!conceptSignatures(payload.path("oralDiet").path("texture"),true).equals(conceptSignatures(remote.path("oralDiet").path("texture"),true)))throw new IllegalArgumentException("Jenis diet/bentuk makanan berubah. Catat ADIME lanjutan dan instruksi baru; jangan menimpa preskripsi lama.");
        JsonNode localStart=payload.path("oralDiet").path("schedule"),remoteStart=remote.path("oralDiet").path("schedule");
        if(localStart.isArray()&&remoteStart.isArray()&&localStart.size()==1&&remoteStart.size()==1){String a=localStart.get(0).path("repeat").path("boundsPeriod").path("start").asText(),b=remoteStart.get(0).path("repeat").path("boundsPeriod").path("start").asText();if(!a.isEmpty()&&!b.isEmpty()&&!time(a).toInstant().equals(time(b).toInstant()))throw new IllegalArgumentException("Waktu mulai instruksi server berbeda; tinjau dokumentasi sebelum koreksi.");}
        String status=remote.path("status").asText();
        if(Arrays.asList("revoked","completed","entered-in-error").contains(status)&&!status.equals(payload.path("status").asText())&&!"entered-in-error".equals(payload.path("status").asText()))throw new IllegalArgumentException("Instruksi server sudah berakhir; tidak boleh diaktifkan kembali melalui Update.");
    }
    private static List<String> conceptSignatures(JsonNode list,boolean texture){List<String> result=new ArrayList<String>();for(JsonNode item:list){JsonNode concept=texture?item.path("modifier"):item;List<String> codes=new ArrayList<String>();for(JsonNode c:concept.path("coding"))codes.add(c.path("system").asText()+"|"+c.path("code").asText());Collections.sort(codes);result.add(codes.toString());}Collections.sort(result);return result;}
    public static ObjectNode mergeUpdate(ObjectNode local,ObjectNode remote){
        verifyUpdate(local,remote);ObjectNode merged=remote.deepCopy();java.util.Iterator<String> keys=local.fieldNames();while(keys.hasNext()){String key=keys.next();if(!"oralDiet".equals(key))merged.set(key,local.get(key));}
        ObjectNode oral=remote.path("oralDiet").isObject()?((ObjectNode)remote.path("oralDiet")).deepCopy():JSON.createObjectNode();
        for(String key:new String[]{"type","texture","schedule","instruction"}){oral.remove(key);if(local.path("oralDiet").has(key))oral.set(key,local.path("oralDiet").get(key).deepCopy());}
        JsonNode localTextures=oral.path("texture"),remoteTextures=remote.path("oralDiet").path("texture");
        if(localTextures.isArray()&&remoteTextures.isArray())for(JsonNode item:localTextures)for(JsonNode old:remoteTextures){ArrayNode a=JSON.createArrayNode().add(item),b=JSON.createArrayNode().add(old);if(conceptSignatures(a,true).equals(conceptSignatures(b,true))){java.util.Iterator<String> fields=old.fieldNames();while(fields.hasNext()){String field=fields.next();if(!"modifier".equals(field))((ObjectNode)item).set(field,old.get(field).deepCopy());}}}
        merged.set("oralDiet",oral);return merged;
    }
    /** Hold POST retry after an uncertain result. No clinical payload or arbitrary error text in logs. */
    public static String safeError(String body){
        try{JsonNode n=JSON.readTree(body);StringBuilder b=new StringBuilder();for(JsonNode issue:n.path("issue")){
            for(JsonNode f:issue.path("expression")){String path=f.asText();if(path.matches("NutritionOrder(?:[.][A-Za-z][A-Za-z0-9]*|\\[[0-9]+\\]){0,30}"))b.append(path).append(' ');}
            java.util.regex.Matcher m=java.util.regex.Pattern.compile("(?i)RuleNumber\\s*[:=]?\\s*([0-9]{1,8})(?![0-9])").matcher(issue.path("details").path("text").asText());while(m.find())b.append("rule=").append(m.group(1)).append(' ');
        }return b.length()==0?"Periksa OperationOutcome pada monitoring SATUSEHAT.":b.toString().trim();}catch(Exception e){return "Periksa respons pada monitoring SATUSEHAT.";}
    }
}
