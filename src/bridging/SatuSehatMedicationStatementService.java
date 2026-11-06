package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fungsi.koneksiDB;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

public final class SatuSehatMedicationStatementService {
    private static final Object SEND_LOCK = new Object();
    public static final class Row {
        public String key, reconciliation, encounterNumber, patientName, nik, encounter, asserted,
                medicineName, dosage, note, status="", mode="", medicine="", display="", remoteId="", informationSource="";
    }
    private final Connection db;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ApiSatuSehat api = new ApiSatuSehat();
    private final String base, organization;
    private String storageTable="satu_sehat_medicationstatement_rekonsiliasi";

    public SatuSehatMedicationStatementService() throws Exception {
        db = koneksiDB.condb();
        base = koneksiDB.URLFHIRSATUSEHAT().replaceAll("/+$", "");
        organization = koneksiDB.IDSATUSEHAT();
    }

    public void ensureTable() throws Exception {
        storageTable=resolveStorageTable(db);
        try (Statement s = db.createStatement()) {
            s.executeUpdate("create table if not exists "+storageTable+" ("
                    + "source_key varchar(64) not null primary key,no_rekonsiliasi varchar(20) not null,"
                    + "nama_obat varchar(100) not null,status_obat varchar(20) not null,"
                    + "mode_obat varchar(40) not null,kode_obat varchar(100) not null,display_obat varchar(255) not null,"
                    + "information_source varchar(100) not null default '',"
                    + "id_medicationstatement varchar(64) default null) ENGINE=InnoDB");
        }
        try(PreparedStatement p=db.prepareStatement("select count(*) from information_schema.columns where table_schema=database() and table_name=? and column_name='information_source'")){
            p.setString(1,storageTable);
            boolean missing;
            try(ResultSet rs=p.executeQuery()){missing=!rs.next()||rs.getInt(1)==0;}
            if(missing)try(Statement s=db.createStatement()){
                s.executeUpdate("alter table "+storageTable+" add column information_source varchar(100) not null default ''");
            }
        }
    }

    public static String resolveStorageTable(Connection db) throws Exception {
        // Tabel Khanza lama memakai no_resep/kode_brng. Jangan ubah skema atau ID di sana.
        // Instalasi versi awal modul ini yang sudah mempunyai source_key tetap dibaca.
        try(PreparedStatement p=db.prepareStatement("select count(*) from information_schema.columns "
                +"where table_schema=database() and table_name='satu_sehat_medicationstatement' and column_name='source_key'");
                ResultSet rs=p.executeQuery()){
            if(rs.next()&&rs.getInt(1)>0)return "satu_sehat_medicationstatement";
        }
        return "satu_sehat_medicationstatement_rekonsiliasi";
    }

    private static final String SELECT = "select r.no_rekonsiliasi,r.no_rawat,r.tanggal_wawancara,r.rekonsiliasi_obat_saat,"
            + "p.nm_pasien,p.no_ktp,e.id_encounter,d.nama_obat,d.dosis_obat,d.frekuensi,d.cara_pemberian,"
            + "d.waktu_pemberian_terakhir,d.tindak_lanjut,d.perubahan_aturan_pakai "
            + "from rekonsiliasi_obat r join rekonsiliasi_obat_detail_obat d on d.no_rekonsiliasi=r.no_rekonsiliasi "
            + "join reg_periksa v on v.no_rawat=r.no_rawat join pasien p on p.no_rkm_medis=v.no_rkm_medis "
            + "left join satu_sehat_encounter e on e.no_rawat=r.no_rawat ";

    private String value(ResultSet rs, String key) throws Exception {
        String value = rs.getString(key); return value == null ? "" : value;
    }

    private Row read(ResultSet rs) throws Exception {
        Row row = new Row();
        row.reconciliation=value(rs,"no_rekonsiliasi");row.encounterNumber=value(rs,"no_rawat");
        row.patientName=value(rs,"nm_pasien");row.nik=value(rs,"no_ktp");row.encounter=value(rs,"id_encounter");
        row.asserted=value(rs,"tanggal_wawancara");row.medicineName=value(rs,"nama_obat");
        row.key=SatuSehatMedicationStatementPayload.key(row.reconciliation,row.medicineName);
        row.dosage="Dosis: " + value(rs,"dosis_obat") + "; Frekuensi: " + value(rs,"frekuensi")
                + "; Cara pemberian: " + value(rs,"cara_pemberian");
        row.note="Riwayat obat: " + row.medicineName + ". Rekonsiliasi saat: " + value(rs,"rekonsiliasi_obat_saat")
                + ". Pemberian terakhir (sesuai catatan): " + value(rs,"waktu_pemberian_terakhir")
                + ". Tindak lanjut: " + value(rs,"tindak_lanjut")
                + ". Perubahan aturan pakai: " + value(rs,"perubahan_aturan_pakai");
        return row;
    }

    public List<Row> load(String start, String end, String query) throws Exception {
        java.time.LocalDate.parse(start);java.time.LocalDate.parse(end);
        if(start.compareTo(end)>0)throw new IllegalArgumentException("Tanggal awal melebihi tanggal akhir.");
        List<Row> rows=new ArrayList<Row>();
        try(PreparedStatement p=db.prepareStatement(SELECT+"where r.tanggal_wawancara >= ? and r.tanggal_wawancara < ? "
                + "and (r.no_rawat like ? or r.no_rekonsiliasi like ? or p.nm_pasien like ? or p.no_rkm_medis like ?) order by r.tanggal_wawancara,d.nama_obat")) {
            p.setString(1,start+" 00:00:00");p.setString(2,java.time.LocalDate.parse(end).plusDays(1)+" 00:00:00");
            for(int i=3;i<=6;i++)p.setString(i,"%"+query+"%");
            try(ResultSet rs=p.executeQuery()){while(rs.next())rows.add(read(rs));}
        }
        for(Row row:rows)loadReview(row);
        return rows;
    }

    private void loadReview(Row row) throws Exception {
        try(PreparedStatement p=db.prepareStatement("select * from "+storageTable+" where source_key=?")) {
            p.setString(1,row.key);
            try(ResultSet rs=p.executeQuery()){if(rs.next()){
                row.status=value(rs,"status_obat");row.mode=value(rs,"mode_obat");row.medicine=value(rs,"kode_obat");
                row.display=value(rs,"display_obat");row.remoteId=value(rs,"id_medicationstatement");
                row.informationSource=value(rs,"information_source");
            }}
        }
    }

    private Row fresh(Row original) throws Exception {
        Row row;
        try(PreparedStatement p=db.prepareStatement(SELECT+"where r.no_rekonsiliasi=? and d.nama_obat=?")) {
            p.setString(1,original.reconciliation);p.setString(2,original.medicineName);
            try(ResultSet rs=p.executeQuery()){
                if(!rs.next())throw new IllegalStateException("Catatan sumber sudah dihapus. Muat ulang data.");
                row=read(rs);
                if(rs.next())throw new IllegalStateException("Ada lebih dari satu catatan dengan nama obat yang sama. Periksa rekonsiliasi.");
            }
        }
        if(!row.key.equals(original.key)||!row.encounterNumber.equals(original.encounterNumber)
                ||!row.nik.equals(original.nik)||!row.encounter.equals(original.encounter)
                ||!row.asserted.equals(original.asserted)||!row.dosage.equals(original.dosage)||!row.note.equals(original.note))
            throw new IllegalStateException("Data sumber berubah. Muat ulang dan tinjau kembali.");
        row.status=original.status;row.mode=original.mode;row.medicine=original.medicine;row.display=original.display;
        row.informationSource=original.informationSource;
        return row;
    }

    public void saveReview(Row row) throws Exception {
        fresh(row);
        SatuSehatMedicationStatementPayload.validate(row.status,row.mode,row.medicine,row.display);
        SatuSehatMedicationStatementPayload.validateInformationSource(row.informationSource);
        try(PreparedStatement p=db.prepareStatement("insert into "+storageTable+" "
                + "(source_key,no_rekonsiliasi,nama_obat,status_obat,mode_obat,kode_obat,display_obat,information_source) values (?,?,?,?,?,?,?,?) "
                + "on duplicate key update status_obat=values(status_obat),mode_obat=values(mode_obat),kode_obat=values(kode_obat),display_obat=values(display_obat),information_source=values(information_source)")) {
            p.setString(1,row.key);p.setString(2,row.reconciliation);p.setString(3,row.medicineName);
            p.setString(4,row.status);p.setString(5,row.mode);p.setString(6,row.medicine);p.setString(7,row.display);p.setString(8,row.informationSource);p.executeUpdate();
        }
    }

    private JsonNode request(String path, HttpMethod method, JsonNode body, String version) throws Exception {
        HttpHeaders headers=new HttpHeaders();headers.setContentType(MediaType.APPLICATION_JSON);
        String token=api.TokenSatuSehat();
        if(token==null||token.trim().isEmpty())throw new IllegalStateException("Token SATUSEHAT tidak tersedia.");
        headers.add("Authorization","Bearer "+token);
        if(version!=null&&!version.isEmpty())headers.add("If-Match","W/\""+version+"\"");
        String response=api.getRest().exchange(URI.create(base+path),method,
                new HttpEntity<String>(body==null?null:mapper.writeValueAsString(body),headers),String.class).getBody();
        return mapper.readTree(response);
    }

    private List<JsonNode> search(String path) throws Exception {
        List<JsonNode> result=new ArrayList<JsonNode>();
        java.util.Set<String> visited=new java.util.HashSet<String>();
        while(path!=null){
            if(!visited.add(path)||visited.size()>100)throw new IllegalStateException("Pagination pencarian tidak selesai.");
            JsonNode bundle=request(path,HttpMethod.GET,null,null);
            if(!"Bundle".equals(bundle.path("resourceType").asText()))throw new IllegalStateException("Respons pencarian bukan Bundle.");
            for(JsonNode entry:bundle.path("entry")){
                JsonNode resource=entry.path("resource");
                if("OperationOutcome".equals(resource.path("resourceType").asText()))throw new IllegalStateException(resource.toString());
                result.add(resource);
            }
            path=null;
            for(JsonNode link:bundle.path("link"))if("next".equals(link.path("relation").asText())){
                String next=link.path("url").asText();
                if(!next.startsWith(base+"/"))throw new IllegalStateException("Tujuan pagination tidak sesuai konfigurasi SATUSEHAT.");
                path=next.substring(base.length());
            }
        }
        return result;
    }

    public ObjectNode prepare(Row original) throws Exception {
        Row row=fresh(original);
        SatuSehatMedicationStatementPayload.validate(row.status,row.mode,row.medicine,row.display);
        SatuSehatMedicationStatementPayload.validateInformationSource(row.informationSource);
        if(!row.encounter.matches("[A-Za-z0-9.-]{1,64}"))throw new IllegalStateException("Encounter belum dikirim.");
        String nik=row.nik.replaceAll("[^0-9]","");
        if(nik.length()!=16)throw new IllegalStateException("NIK pasien belum valid.");
        JsonNode encounter=request("/Encounter/"+row.encounter,HttpMethod.GET,null,null);
        List<JsonNode> patients=search("/Patient?identifier="+URLEncoder.encode("https://fhir.kemkes.go.id/id/nik|"+nik,"UTF-8"));
        if(patients.size()!=1)throw new IllegalStateException("Patient berdasarkan NIK tidak ditemukan atau tidak unik.");
        String patient="Patient/"+patients.get(0).path("id").asText();
        String source=SatuSehatMedicationStatementPayload.PATIENT_SOURCE.equals(row.informationSource)?patient:row.informationSource;
        if(!source.equals(patient)){
            JsonNode sourceResource=request("/"+source,HttpMethod.GET,null,null);
            String[] parts=source.split("/",2);
            if(!parts[0].equals(sourceResource.path("resourceType").asText())||!parts[1].equals(sourceResource.path("id").asText()))
                throw new IllegalStateException("Referensi sumber informasi tidak ditemukan.");
            if("RelatedPerson".equals(parts[0])&&!patient.equals(sourceResource.path("patient").path("reference").asText()))
                throw new IllegalStateException("RelatedPerson sumber informasi tidak terkait dengan pasien ini.");
        }
        if(!patient.equals(encounter.path("subject").path("reference").asText())
                ||!("Organization/"+organization).equals(encounter.path("serviceProvider").path("reference").asText()))
            throw new IllegalStateException("Pasien/organisasi Encounter tidak sesuai.");
        if("Medication (faskes ini)".equals(row.mode)){
            JsonNode medication=request("/"+row.medicine,HttpMethod.GET,null,null);
            if(!"Medication".equals(medication.path("resourceType").asText()))throw new IllegalStateException("Referensi Medication tidak ditemukan.");
            List<JsonNode> dispenses=search("/MedicationDispense?subject="+URLEncoder.encode(patient,"UTF-8")
                    +"&medication="+URLEncoder.encode(row.medicine,"UTF-8"));
            boolean found=false;
            for(JsonNode dispense:dispenses)if(patient.equals(dispense.path("subject").path("reference").asText())
                    &&row.medicine.equals(dispense.path("medicationReference").path("reference").asText())){
                String context=dispense.path("context").path("reference").asText();
                if(context.matches("Encounter/[A-Za-z0-9.-]{1,64}")){
                    JsonNode previousEncounter=request("/"+context,HttpMethod.GET,null,null);
                    if(("Organization/"+organization).equals(previousEncounter.path("serviceProvider").path("reference").asText())){found=true;break;}
                }
            }
            if(!found)throw new IllegalStateException("Medication belum ditemukan pada riwayat MedicationDispense pasien dari faskes ini.");
        }
        return SatuSehatMedicationStatementPayload.build(mapper,organization,row.key,patient,"Encounter/"+row.encounter,
                row.asserted,row.status,row.mode,row.medicine,row.display,row.dosage,row.note,source);
    }

    private void verifyIdentity(JsonNode remote, JsonNode payload) {
        if(!"MedicationStatement".equals(remote.path("resourceType").asText())
                ||!remote.path("subject").path("reference").asText().equals(payload.path("subject").path("reference").asText())
                ||!remote.path("context").path("reference").asText().equals(payload.path("context").path("reference").asText()))
            throw new IllegalStateException("Identitas MedicationStatement server tidak sesuai.");
        boolean found=false;
        for(JsonNode id:remote.path("identifier"))if(id.path("system").asText().equals(payload.path("identifier").get(0).path("system").asText())
                &&id.path("value").asText().equals(payload.path("identifier").get(0).path("value").asText()))found=true;
        if(!found)throw new IllegalStateException("Identifier MedicationStatement server tidak sesuai.");
    }

    public String send(Row row, boolean update) throws Exception {
        synchronized(SEND_LOCK){return sendLocked(row,update);}
    }

    private String sendLocked(Row row, boolean update) throws Exception {
        ObjectNode payload=prepare(row);
        saveReview(row);
        Row saved=new Row();saved.key=row.key;loadReview(saved);
        String identifier=payload.path("identifier").get(0).path("system").asText()+"|"+row.key;
        List<JsonNode> matches=search("/MedicationStatement?identifier="+URLEncoder.encode(identifier,"UTF-8"));
        if(matches.size()>1)throw new IllegalStateException("Identifier mempunyai lebih dari satu MedicationStatement.");
        JsonNode remote=null;
        if(!saved.remoteId.isEmpty()){
            if(!saved.remoteId.matches("[A-Za-z0-9.-]{1,64}"))throw new IllegalStateException("ID lokal tidak valid.");
            remote=request("/MedicationStatement/"+saved.remoteId,HttpMethod.GET,null,null);
            if(!matches.isEmpty()&&!saved.remoteId.equals(matches.get(0).path("id").asText()))
                throw new IllegalStateException("ID lokal berbeda dengan hasil pencarian server.");
        }else if(!matches.isEmpty())remote=matches.get(0);
        if(remote!=null){
            verifyIdentity(remote,payload);
            String id=remote.path("id").asText();persistId(row,id);
            if(!update)return "Sudah ada di server; ID disinkronkan. Gunakan Update untuk mengubah isi.";
            payload.put("id",id);
            request("/MedicationStatement/"+id,HttpMethod.PUT,payload,remote.path("meta").path("versionId").asText());
        }else{
            if(update)throw new IllegalStateException("Data belum dikirim. Gunakan Kirim Baru.");
            remote=request("/MedicationStatement",HttpMethod.POST,payload,null);
            String id=remote.path("id").asText();
            // Simpan ID segera; pencarian identifier memulihkan ID bila penyimpanan lokal gagal.
            persistId(row,id);
        }
        JsonNode verified=request("/MedicationStatement/"+row.remoteId,HttpMethod.GET,null,null);
        verifyIdentity(verified,payload);
        for(String field:new String[]{"status","medicationReference","medicationCodeableConcept","dosage","note","informationSource"})
            if(!verified.path(field).equals(payload.path(field)))throw new IllegalStateException("ID tersimpan, tetapi verifikasi isi server berbeda pada "+field+". Periksa sebelum Update.");
        return "Berhasil: MedicationStatement/"+row.remoteId;
    }

    private void persistId(Row row,String id) throws Exception {
        if(!id.matches("[A-Za-z0-9.-]{1,64}"))throw new IllegalStateException("Respons server tidak berisi ID MedicationStatement valid.");
        try(PreparedStatement p=db.prepareStatement("update "+storageTable+" set id_medicationstatement=? "
                + "where source_key=? and (id_medicationstatement is null or id_medicationstatement='' or id_medicationstatement=?)")){
            p.setString(1,id);p.setString(2,row.key);p.setString(3,id);
            if(p.executeUpdate()!=1)throw new IllegalStateException("ID server "+id+" belum tersimpan lokal. Muat ulang dan lakukan Kirim Baru untuk pemulihan ID.");
        }
        row.remoteId=id;
    }
}
