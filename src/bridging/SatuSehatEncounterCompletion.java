package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import java.time.*;

/** Eligibility and real event times for the manual completed-encounter list. No writes. */
public final class SatuSehatEncounterCompletion {
    private SatuSehatEncounterCompletion(){}
    public static String query(boolean inpatient,boolean search,boolean resumeAvailable){
        String start="concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg)";
        String care=inpatient?"(select min(concat(k.tgl_masuk,' ',k.jam_masuk)) from kamar_inap k where k.no_rawat=reg_periksa.no_rawat and k.tgl_masuk>'0000-00-00')"
            :"(select min(concat(p.tgl_perawatan,' ',p.jam_rawat)) from pemeriksaan_ralan p where p.no_rawat=reg_periksa.no_rawat)";
        String end;
        if(inpatient){
            end="(select max(concat(k.tgl_keluar,' ',k.jam_keluar)) from kamar_inap k where k.no_rawat=reg_periksa.no_rawat "
                +"and trim(ifnull(k.stts_pulang,'')) not in ('','-','Pindah Kamar') and k.tgl_keluar>'0000-00-00' "
                +"and not exists(select 1 from kamar_inap newer where newer.no_rawat=k.no_rawat and concat(newer.tgl_masuk,' ',newer.jam_masuk)>concat(k.tgl_masuk,' ',k.jam_masuk)))";
        }else{
            String resume=resumeAvailable?"(select max(concat(m.tgl_keluar,' ',m.jam_keluar)) from resume_medis_ralan_v2 m where m.no_rawat=reg_periksa.no_rawat and m.tgl_keluar>'0000-00-00' and concat(m.tgl_keluar,' ',m.jam_keluar)>"+start+"),":"";
            // Optional stored payment time; absence of a nota never excludes a paid outpatient.
            end="coalesce("+resume+"(select max(concat(n.tanggal,' ',n.jam)) from nota_jalan n where n.no_rawat=reg_periksa.no_rawat),null)";
        }
        String sql="select reg_periksa.tgl_registrasi,reg_periksa.jam_reg,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.no_ktp,reg_periksa.kd_dokter,"
            +"pegawai.nama,pegawai.no_ktp as ktpdokter,reg_periksa.kd_poli,poliklinik.nm_poli,satu_sehat_mapping_lokasi_ralan.id_lokasi_satusehat,reg_periksa.stts,reg_periksa.status_lanjut,"
            +end+" as pulang,"+care+" as mulai_layanan,ifnull(satu_sehat_encounter.id_encounter,'') as id_encounter "
            +"from reg_periksa inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis inner join pegawai on pegawai.nik=reg_periksa.kd_dokter "
            +"inner join poliklinik on poliklinik.kd_poli=reg_periksa.kd_poli inner join satu_sehat_mapping_lokasi_ralan on satu_sehat_mapping_lokasi_ralan.kd_poli=reg_periksa.kd_poli "
            +"left join satu_sehat_encounter on satu_sehat_encounter.no_rawat=reg_periksa.no_rawat "
            +"where reg_periksa.tgl_registrasi between ? and ? and reg_periksa.stts<>'Batal' "
            +"and reg_periksa.status_lanjut='"+(inpatient?"Ranap":"Ralan")+"' "
            +"and not exists(select 1 from kamar_inap active_room where active_room.no_rawat=reg_periksa.no_rawat and trim(ifnull(active_room.stts_pulang,'')) in ('','-')) "
            +(inpatient?"and "+end+" is not null ":"and reg_periksa.status_bayar='Sudah Bayar' ");
        if(search)sql+="and (reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or pasien.nm_pasien like ? or pasien.no_ktp like ? or pegawai.nama like ? or poliklinik.nm_poli like ? or reg_periksa.stts like ? or reg_periksa.status_lanjut like ?) ";
        return sql+"order by reg_periksa.tgl_registrasi,reg_periksa.jam_reg";
    }
    public static void complete(ObjectNode payload,String start,String care,String end,Clock clock){
        Instant a=instant(start),b=instant(care),c=instant(end);
        if(a.isBefore(Instant.parse("2014-06-03T00:00:00Z"))||b.isBefore(a)||c.isBefore(b)||c.isAfter(clock.instant()))
            throw new IllegalArgumentException("Urutan/waktu selesai kunjungan belum valid. Periksa catatan pelayanan.");
        payload.put("status","finished");ObjectNode period=payload.with("period");period.put("start",a.toString());period.put("end",c.toString());
        ArrayNode history=payload.putArray("statusHistory");
        add(history,"arrived",a,b);add(history,"in-progress",b,c);add(history,"finished",c,c);
    }
    /** Bootstrap the Encounter ID needed by Condition, without claiming final completion. */
    public static void initial(ObjectNode payload){
        JsonNode arrived=payload.path("statusHistory").get(0).deepCopy();
        payload.put("status","arrived");payload.with("period").remove("end");
        payload.remove("diagnosis");payload.putArray("statusHistory").add(arrived);
    }
    private static void add(ArrayNode history,String status,Instant start,Instant end){
        ObjectNode node=history.addObject();node.put("status",status);ObjectNode period=node.putObject("period");period.put("start",start.toString());if(end!=null)period.put("end",end.toString());
    }
    private static Instant instant(String value){
        if(value==null||value.trim().isEmpty())throw new IllegalArgumentException("Waktu mulai/selesai pelayanan belum tercatat. Encounter ditahan.");
        return OffsetDateTime.parse(value).toInstant();
    }
    /** Retain remote clinical fields and historical transitions during PUT. */
    public static ObjectNode mergeCompleted(ObjectNode local,JsonNode remote){
        if(remote.path("status").asText().equals("cancelled")||remote.path("status").asText().equals("entered-in-error"))throw new IllegalArgumentException("Encounter server dibatalkan/salah entri; perlu ditinjau sebelum Update.");
        Instant localStart=instant(local.path("period").path("start").asText()),localEnd=instant(local.path("period").path("end").asText());
        if(remote.path("period").has("start")&&!instant(remote.path("period").path("start").asText()).equals(localStart))throw new IllegalArgumentException("Waktu mulai server berbeda; tinjau sebelum Update.");
        if("finished".equals(remote.path("status").asText())&&remote.path("period").has("end")&&!instant(remote.path("period").path("end").asText()).equals(localEnd))throw new IllegalArgumentException("Waktu selesai server berbeda; tinjau sebelum Update.");
        ObjectNode merged=((ObjectNode)remote).deepCopy();
        java.util.Iterator<String> fields=local.fieldNames();while(fields.hasNext()){String key=fields.next();if(!key.equals("statusHistory"))merged.set(key,local.get(key));}
        if(!remote.path("statusHistory").isArray()||remote.path("statusHistory").size()==0){merged.set("statusHistory",local.path("statusHistory"));return merged;}
        ArrayNode history=((ArrayNode)remote.path("statusHistory")).deepCopy();Instant end=localEnd,previousEnd=localStart;
        for(int i=0;i<history.size();i++){
            JsonNode h=history.get(i);Instant begin=instant(h.path("period").path("start").asText());
            if(begin.isAfter(end)||begin.isBefore(previousEnd))throw new IllegalArgumentException("Urutan riwayat server tidak sesuai periode kunjungan.");
            if(h.path("period").has("end")){
                previousEnd=instant(h.path("period").path("end").asText());
                if(previousEnd.isBefore(begin))throw new IllegalArgumentException("Akhir riwayat server sebelum awalnya.");
            }else previousEnd=begin;
            if(h.path("status").asText().equals("finished")){
                if(!begin.equals(end)||i!=history.size()-1)throw new IllegalArgumentException("Waktu selesai server berbeda; tinjau sebelum Update.");
                ((ObjectNode)h.path("period")).put("end",end.toString());
                merged.set("statusHistory",history);return merged;
            }
            if(!h.path("period").has("end")){
                if(i!=history.size()-1)throw new IllegalArgumentException("Riwayat status server belum konsisten.");
                Instant close="arrived".equals(h.path("status").asText())?instant(local.path("statusHistory").get(1).path("period").path("start").asText()):end;
                if(close.isBefore(begin))throw new IllegalArgumentException("Awal riwayat server melewati batas pelayanan lokal.");
                ((ObjectNode)h.path("period")).put("end",close.toString());previousEnd=close;
            }else if(instant(h.path("period").path("end").asText()).isAfter(end))throw new IllegalArgumentException("Riwayat server melewati waktu pulang lokal.");
        }
        if(previousEnd.isBefore(end)){
            JsonNode care=local.path("statusHistory").get(1);
            if(!instant(care.path("period").path("start").asText()).equals(previousEnd))throw new IllegalArgumentException("Batas riwayat server dan mulai pelayanan berbeda; tinjau sebelum Update.");
            history.add(care.deepCopy());
        }
        add(history,"finished",end,end);merged.set("statusHistory",history);return merged;
    }
}
