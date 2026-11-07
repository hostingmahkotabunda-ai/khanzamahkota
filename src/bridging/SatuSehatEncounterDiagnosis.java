package bridging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import java.sql.*;
import java.util.*;

/** Resolve recorded visit diagnoses to sent Conditions; never invent a diagnosis. */
public final class SatuSehatEncounterDiagnosis {
    private SatuSehatEncounterDiagnosis(){}
    public static final class NotReady extends IllegalArgumentException {NotReady(String text){super(text);}}
    public static String conditionListQuery(boolean search){
        String sql="select reg_periksa.tgl_registrasi,reg_periksa.jam_reg,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.no_ktp,"
            +"reg_periksa.stts,reg_periksa.status_lanjut,'' as pulang,satu_sehat_encounter.id_encounter,diagnosa_pasien.kd_penyakit,penyakit.nm_penyakit,ifnull(satu_sehat_condition.id_condition,'') as id_condition "
            +"from reg_periksa inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "
            +"inner join satu_sehat_encounter on satu_sehat_encounter.no_rawat=reg_periksa.no_rawat "
            +"inner join diagnosa_pasien on diagnosa_pasien.no_rawat=reg_periksa.no_rawat and diagnosa_pasien.status=reg_periksa.status_lanjut "
            +"inner join penyakit on penyakit.kd_penyakit=diagnosa_pasien.kd_penyakit "
            +"left join satu_sehat_condition on satu_sehat_condition.no_rawat=diagnosa_pasien.no_rawat and satu_sehat_condition.kd_penyakit=diagnosa_pasien.kd_penyakit and satu_sehat_condition.status=diagnosa_pasien.status "
            +"where reg_periksa.tgl_registrasi between ? and ? and trim(ifnull(satu_sehat_encounter.id_encounter,''))<>'' and reg_periksa.stts<>'Batal' ";
        if(search)sql+="and (reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or pasien.nm_pasien like ? or pasien.no_ktp like ? or diagnosa_pasien.kd_penyakit like ? or penyakit.nm_penyakit like ? or reg_periksa.stts like ? or reg_periksa.status_lanjut like ?) ";
        return sql+"order by reg_periksa.tgl_registrasi,reg_periksa.jam_reg,reg_periksa.no_rawat,diagnosa_pasien.prioritas";
    }
    public static ArrayNode load(Connection db,ObjectMapper mapper,String visit,String status)throws SQLException{
        ArrayNode items=mapper.createArrayNode();Set<Integer> ranks=new HashSet<>();Set<String> references=new HashSet<>();
        try(PreparedStatement s=db.prepareStatement("select d.prioritas,c.id_condition from diagnosa_pasien d left join satu_sehat_condition c on c.no_rawat=d.no_rawat and c.kd_penyakit=d.kd_penyakit and c.status=d.status where d.no_rawat=? and d.status=? order by d.prioritas")){
            s.setString(1,visit);s.setString(2,status);
            try(ResultSet r=s.executeQuery()){while(r.next()){
                int rank=r.getInt("prioritas");String id=r.getString("id_condition");
                if(id==null||!id.matches("[A-Za-z0-9.-]{1,64}"))throw new NotReady("Kirim seluruh Condition untuk kunjungan dan jenis pelayanan yang sama, lalu Update Encounter.");
                if(rank<1||!ranks.add(rank)||!references.add(id))throw new NotReady("Prioritas/referensi diagnosis tidak unik atau tidak valid; tinjau diagnosis kunjungan.");
                ObjectNode item=items.addObject();item.putObject("condition").put("reference","Condition/"+id);
                ObjectNode use=item.putObject("use").putArray("coding").addObject();use.put("system","http://terminology.hl7.org/CodeSystem/diagnosis-role");use.put("code","DD");use.put("display","Discharge diagnosis");
                item.put("rank",rank);
            }}
        }
        if(items.size()==0||!ranks.contains(1))throw new NotReady("Diagnosis utama/Condition belum tersedia. Lengkapi diagnosis, kirim Condition, lalu Update Encounter.");
        return items;
    }
}
