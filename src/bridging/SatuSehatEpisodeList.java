package bridging;

import fungsi.koneksiDB;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Read-only local episode queue. Missing prerequisites remain visible. */
public final class SatuSehatEpisodeList {
    public static final class Row {
        public String kind,visit,rm,name,date,start,category,status,remote,encounter,condition,diagnosis,identifier;
        public String readiness(){
            if(!remote.isEmpty())return "ID episode tersimpan; tautan Encounter perlu ditinjau";
            if(encounter.isEmpty())return "Menunggu Encounter";
            if(!"ANC".equals(kind)&&condition.isEmpty())return "Menunggu Condition";
            try{
                SatuSehatEpisodePayload.dateTime(start,"Tanggal mulai",java.time.Clock.systemUTC());
                if("ANC".equals(kind)){
                    SatuSehatEpisodeSupport.validateAncCode(category);
                    if(LocalDate.parse(start).isAfter(LocalDate.parse(date)))return "HPHT melewati tanggal kunjungan";
                }
            }catch(IllegalArgumentException e){return "Periksa tanggal/kategori episode";}
            return "Siap ditinjau";
        }
    }
    public static final class Result {
        public final List<Row> rows=new ArrayList<Row>();
        public final List<String> notices=new ArrayList<String>();
    }
    public Result load(String from,String to,String search)throws SQLException{
        if(LocalDate.parse(from).isAfter(LocalDate.parse(to)))throw new IllegalArgumentException("Tanggal awal melewati tanggal akhir.");
        Result result=new Result();
        try(Connection c=koneksiDB.bukaKoneksiBaca()){
            if(exists(c,"satu_sehat_kunjungan_anc")&&exists(c,"satu_sehat_episode_kehamilan")){
                query(c,result,from,to,search,"select 'ANC' kind,k.no_rawat,r.no_rkm_medis,p.nm_pasien,r.tgl_registrasi,e.hpht mulai,k.status_kunjungan kategori,e.status,ifnull(e.id_episode,'') remote,ifnull(s.id_encounter,'') encounter,'' cond,'' diagnosis,e.identifier_lokal identifier "
                    +"from satu_sehat_kunjungan_anc k join reg_periksa r on r.no_rawat=k.no_rawat join satu_sehat_episode_kehamilan e on e.id_episode_kehamilan=k.id_episode_kehamilan and e.no_rkm_medis=r.no_rkm_medis join pasien p on p.no_rkm_medis=r.no_rkm_medis left join satu_sehat_encounter s on s.no_rawat=r.no_rawat ");
            }else result.notices.add("Tabel ANC belum tersedia.");
            if(exists(c,"satu_sehat_episode_ptm_draft")){
                query(c,result,from,to,search,"select d.tipe kind,d.no_rawat,r.no_rkm_medis,p.nm_pasien,r.tgl_registrasi,d.tanggal_mulai mulai,'' kategori,d.status,ifnull((select min(l.id_episode) from satu_sehat_encounter_episode l join satu_sehat_episode_of_care e on e.id_episode=l.id_episode where l.no_rawat=d.no_rawat and e.no_rkm_medis=d.no_rkm_medis and e.tipe=d.tipe),'') remote,ifnull(s.id_encounter,'') encounter,ifnull((select min(c.id_condition) from satu_sehat_condition c where c.no_rawat=d.no_rawat and c.kd_penyakit=d.kd_penyakit and c.id_condition<>''),'') cond,d.kd_penyakit diagnosis,'' identifier "
                    +"from satu_sehat_episode_ptm_draft d join reg_periksa r on r.no_rawat=d.no_rawat and r.no_rkm_medis=d.no_rkm_medis join pasien p on p.no_rkm_medis=r.no_rkm_medis left join satu_sehat_encounter s on s.no_rawat=r.no_rawat ");
            }else result.notices.add("Draf PTM belum tersedia; daftar ANC tetap ditampilkan.");
        }
        return result;
    }
    private boolean exists(Connection c,String table)throws SQLException{
        try(PreparedStatement p=c.prepareStatement("select 1 from information_schema.tables where table_schema=database() and table_name=?")){
            p.setString(1,table);try(ResultSet r=p.executeQuery()){return r.next();}
        }
    }
    private void query(Connection c,Result result,String from,String to,String search,String sql)throws SQLException{
        sql+="where r.tgl_registrasi between ? and ? and (r.no_rawat like ? or r.no_rkm_medis like ? or p.nm_pasien like ?) order by r.tgl_registrasi desc,r.no_rawat desc";
        try(PreparedStatement p=c.prepareStatement(sql)){
            p.setQueryTimeout(30);p.setString(1,from);p.setString(2,to);
            for(int i=3;i<=5;i++)p.setString(i,"%"+search.trim()+"%");
            try(ResultSet r=p.executeQuery()){
                while(r.next()){
                    Row row=new Row();row.kind=r.getString("kind");row.visit=r.getString("no_rawat");row.rm=r.getString("no_rkm_medis");row.name=r.getString("nm_pasien");row.date=r.getString("tgl_registrasi");row.start=r.getString("mulai");row.category=r.getString("kategori");row.status=r.getString("status");row.remote=r.getString("remote");row.encounter=r.getString("encounter");row.condition=r.getString("cond");row.diagnosis=r.getString("diagnosis");row.identifier=r.getString("identifier");result.rows.add(row);
                }
            }
        }
    }
}
