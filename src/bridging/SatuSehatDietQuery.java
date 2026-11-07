package bridging;

/** List discharge-diet narratives independently of printed billing notes. No writes. */
public final class SatuSehatDietQuery {
    private SatuSehatDietQuery(){}
    public static String query(boolean search){
        String sql="select reg_periksa.tgl_registrasi,reg_periksa.jam_reg,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,"
            +"pasien.nm_pasien,pasien.no_ktp,satu_sehat_encounter.id_encounter,catatan_adime_gizi.instruksi,"
            +"pegawai.nama,pegawai.no_ktp as ktppraktisi,catatan_adime_gizi.tanggal,ifnull(satu_sehat_diet.id_diet,'') as satu_sehat_diet "
            +"from reg_periksa inner join pasien on pasien.no_rkm_medis=reg_periksa.no_rkm_medis "
            +"inner join satu_sehat_encounter on satu_sehat_encounter.no_rawat=reg_periksa.no_rawat "
            +"inner join catatan_adime_gizi on catatan_adime_gizi.no_rawat=reg_periksa.no_rawat "
            +"inner join pegawai on pegawai.nik=catatan_adime_gizi.nip "
            +"left join satu_sehat_diet on satu_sehat_diet.no_rawat=catatan_adime_gizi.no_rawat and satu_sehat_diet.tanggal=catatan_adime_gizi.tanggal "
            +"where reg_periksa.tgl_registrasi between ? and ? and reg_periksa.stts<>'Batal' "
            +"and trim(ifnull(catatan_adime_gizi.instruksi,''))<>'' and trim(ifnull(satu_sehat_encounter.id_encounter,''))<>'' "
            +"and not exists(select 1 from kamar_inap active_room where active_room.no_rawat=reg_periksa.no_rawat and trim(ifnull(active_room.stts_pulang,'')) in ('','-')) "
            +"and ((reg_periksa.status_lanjut='Ralan' and reg_periksa.status_bayar='Sudah Bayar') or "
            +"(reg_periksa.status_lanjut='Ranap' and exists(select 1 from kamar_inap k where k.no_rawat=reg_periksa.no_rawat "
            +"and trim(ifnull(k.stts_pulang,'')) not in ('','-','Pindah Kamar') and k.tgl_keluar>'0000-00-00' "
            +"and not exists(select 1 from kamar_inap newer where newer.no_rawat=k.no_rawat and concat(newer.tgl_masuk,' ',newer.jam_masuk)>concat(k.tgl_masuk,' ',k.jam_masuk))))) ";
        if(search)sql+="and (reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or pasien.nm_pasien like ? or pasien.no_ktp like ? or pegawai.no_ktp like ? or pegawai.nama like ?) ";
        return sql+"order by reg_periksa.tgl_registrasi,reg_periksa.jam_reg,reg_periksa.no_rawat,catatan_adime_gizi.tanggal";
    }
}
