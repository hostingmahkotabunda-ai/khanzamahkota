package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fungsi.akses;
import fungsi.koneksiDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 *
 * Worker background untuk pengiriman Encounter harian dan Condition susulan.
 */
public class SatuSehatAutoEncounterConditionService {
    private static final int DEFAULT_ENCOUNTER_INTERVAL_MINUTES = 10;
    private static final int DEFAULT_CONDITION_INTERVAL_MINUTES = 20;
    private static final int DEFAULT_ENCOUNTER_LOOKBACK_DAYS = 7;
    private static final int DEFAULT_CONDITION_LOOKBACK_DAYS = 14;
    private static final int LOCK_TTL_MINUTES = 9;
    private static final int MESSAGE_LIMIT = 500;
    private static final SatuSehatAutoEncounterConditionService INSTANCE = new SatuSehatAutoEncounterConditionService();

    private final Connection koneksi = koneksiDB.condb();
    private final ApiSatuSehat api = new ApiSatuSehat();
    private final SatuSehatCekNIK cekViaSatuSehat = new SatuSehatCekNIK();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String link = koneksiDB.URLFHIRSATUSEHAT();
    private final String workerName = buildWorkerName();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean encounterActive = new AtomicBoolean(false);
    private final AtomicBoolean conditionActive = new AtomicBoolean(false);
    private Timer timer;

    public static SatuSehatAutoEncounterConditionService getInstance() {
        return INSTANCE;
    }

    private SatuSehatAutoEncounterConditionService() {
        super();
    }

    public synchronized void start() {
        if (started.getAndSet(true)) {
            return;
        }

        try {
            ensureSupportTables();
            if (!isEnabled()) {
                started.set(false);
                System.out.println("Auto Satu Sehat nonaktif");
                return;
            }
            if (link.equals("")) {
                started.set(false);
                System.out.println("Auto Satu Sehat dilewati, URLFHIRSATUSEHAT belum diatur");
                return;
            }
            timer = new Timer("AutoSatuSehatEncounterCondition", true);
            scheduleEncounter();
            scheduleCondition();
            System.out.println("Auto Satu Sehat aktif di worker " + workerName);
        } catch (Exception e) {
            started.set(false);
            System.out.println("Gagal mengaktifkan Auto Satu Sehat : " + e);
        }
    }

    public synchronized void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        started.set(false);
    }

    public synchronized void setEnabled(boolean aktif) {
        try {
            ensureSupportTables();
            setConfigValue("aktif", aktif ? "yes" : "no");
            if (aktif) {
                stop();
                start();
            } else {
                stop();
            }
        } catch (Exception e) {
            System.out.println("Gagal mengubah status Auto Satu Sehat : " + e);
        }
    }

    public boolean isEnabled() {
        return getConfigValue("aktif", "yes").equalsIgnoreCase("yes");
    }

    public boolean isRunning() {
        return started.get();
    }

    public void runEncounterNow() {
        if (encounterActive.compareAndSet(false, true)) {
            Thread worker = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        runEncounterJob();
                    } finally {
                        encounterActive.set(false);
                    }
                }
            }, "AutoSatuSehatEncounterManual");
            worker.setDaemon(true);
            worker.start();
        }
    }

    public void runConditionNow() {
        if (conditionActive.compareAndSet(false, true)) {
            Thread worker = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        runConditionJob();
                    } finally {
                        conditionActive.set(false);
                    }
                }
            }, "AutoSatuSehatConditionManual");
            worker.setDaemon(true);
            worker.start();
        }
    }

    public void runAllNow() {
        runEncounterNow();
        runConditionNow();
    }

    private void scheduleEncounter() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (encounterActive.compareAndSet(false, true)) {
                    Thread worker = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                runEncounterJob();
                            } finally {
                                encounterActive.set(false);
                            }
                        }
                    }, "AutoSatuSehatEncounterJob");
                    worker.setDaemon(true);
                    worker.start();
                }
            }
        }, 2L * 60L * 1000L, getEncounterIntervalMinutes() * 60L * 1000L);
    }

    private void scheduleCondition() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (conditionActive.compareAndSet(false, true)) {
                    Thread worker = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                runConditionJob();
                            } finally {
                                conditionActive.set(false);
                            }
                        }
                    }, "AutoSatuSehatConditionJob");
                    worker.setDaemon(true);
                    worker.start();
                }
            }
        }, 5L * 60L * 1000L, getConditionIntervalMinutes() * 60L * 1000L);
    }

    private void runEncounterJob() {
        final String serviceName = "encounter";
        if (!ambilLock(serviceName)) {
            return;
        }

        int sukses = 0;
        int gagal = 0;
        try {
            String token = api.TokenSatuSehat();
            if (token.equals("")) {
                throw new Exception("Token Satu Sehat kosong");
            }

            PreparedStatement ps = koneksi.prepareStatement(
                    "select data_kunjungan.mulai,data_kunjungan.no_rawat,data_kunjungan.no_rkm_medis,data_kunjungan.nm_pasien," +
                    "data_kunjungan.ktp_pasien,data_kunjungan.nm_dokter,data_kunjungan.ktp_dokter,data_kunjungan.nm_poli," +
                    "data_kunjungan.id_lokasi_satusehat,data_kunjungan.status_lanjut,data_kunjungan.pulang " +
                    "from (" +
                    "select concat(reg_periksa.tgl_registrasi,'T',reg_periksa.jam_reg,'+07:00') as mulai,reg_periksa.no_rawat,reg_periksa.no_rkm_medis," +
                    "pasien.nm_pasien,pasien.no_ktp as ktp_pasien,pegawai.nama as nm_dokter,pegawai.no_ktp as ktp_dokter,poliklinik.nm_poli," +
                    "satu_sehat_mapping_lokasi_ralan.id_lokasi_satusehat,reg_periksa.status_lanjut,ifnull(concat(nota_jalan.tanggal,'T',nota_jalan.jam,'+07:00'),concat(date_format(date_add(timestamp(reg_periksa.tgl_registrasi,reg_periksa.jam_reg), interval 4 hour),'%Y-%m-%dT%H:%i:%s'),'+07:00')) as pulang " +
                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                    "inner join pegawai on pegawai.nik=reg_periksa.kd_dokter inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli " +
                    "inner join satu_sehat_mapping_lokasi_ralan on satu_sehat_mapping_lokasi_ralan.kd_poli=poliklinik.kd_poli " +
                    "left join nota_jalan on nota_jalan.no_rawat=reg_periksa.no_rawat " +
                    "left join satu_sehat_encounter on satu_sehat_encounter.no_rawat=reg_periksa.no_rawat " +
                    "where reg_periksa.status_lanjut='Ralan' and reg_periksa.tgl_registrasi between date_sub(curdate(), interval " + getEncounterLookbackDays() + " day) and curdate() " +
                    "and ifnull(satu_sehat_encounter.id_encounter,'')='' and ifnull(pasien.no_ktp,'')<>'' and ifnull(pegawai.no_ktp,'')<>'' " +
                    "union all " +
                    "select concat(reg_periksa.tgl_registrasi,'T',reg_periksa.jam_reg,'+07:00') as mulai,reg_periksa.no_rawat,reg_periksa.no_rkm_medis," +
                    "pasien.nm_pasien,pasien.no_ktp as ktp_pasien,pegawai.nama as nm_dokter,pegawai.no_ktp as ktp_dokter,kamar_ranap.nm_bangsal as nm_poli," +
                    "kamar_ranap.id_lokasi_satusehat,reg_periksa.status_lanjut,ifnull(concat(nota_inap.tanggal,'T',nota_inap.jam,'+07:00'),concat(kamar_ranap.tgl_keluar,'T',kamar_ranap.jam_keluar,'+07:00')) as pulang " +
                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                    "inner join pegawai on pegawai.nik=reg_periksa.kd_dokter " +
                    "inner join (" +
                    "select ki1.no_rawat,ki1.kd_kamar,bangsal.nm_bangsal,mapping_ranap.id_lokasi_satusehat,ki1.tgl_keluar,ki1.jam_keluar " +
                    "from kamar_inap ki1 inner join satu_sehat_mapping_lokasi_ranap mapping_ranap on mapping_ranap.kd_kamar=ki1.kd_kamar " +
                    "left join kamar on kamar.kd_kamar=ki1.kd_kamar left join bangsal on bangsal.kd_bangsal=kamar.kd_bangsal " +
                    "inner join (" +
                    "select ki2.no_rawat,max(timestamp(ki2.tgl_masuk,ki2.jam_masuk)) as max_masuk " +
                    "from kamar_inap ki2 inner join satu_sehat_mapping_lokasi_ranap mapping_ranap2 on mapping_ranap2.kd_kamar=ki2.kd_kamar " +
                    "group by ki2.no_rawat" +
                    ") pilihan_kamar on pilihan_kamar.no_rawat=ki1.no_rawat and timestamp(ki1.tgl_masuk,ki1.jam_masuk)=pilihan_kamar.max_masuk " +
                    ") kamar_ranap on kamar_ranap.no_rawat=reg_periksa.no_rawat " +
                    "left join nota_inap on nota_inap.no_rawat=reg_periksa.no_rawat " +
                    "left join satu_sehat_encounter on satu_sehat_encounter.no_rawat=reg_periksa.no_rawat " +
                    "where reg_periksa.status_lanjut='Ranap' and reg_periksa.tgl_registrasi between date_sub(curdate(), interval " + getEncounterLookbackDays() + " day) and curdate() " +
                    "and (ifnull(nota_inap.no_rawat,'')<>'' or (kamar_ranap.tgl_keluar<>'0000-00-00' and kamar_ranap.jam_keluar<>'00:00:00')) " +
                    "and ifnull(satu_sehat_encounter.id_encounter,'')='' and ifnull(pasien.no_ktp,'')<>'' and ifnull(pegawai.no_ktp,'')<>'' " +
                    ") as data_kunjungan order by data_kunjungan.mulai,data_kunjungan.no_rawat");

            ResultSet rs = null;
            try {
                rs = ps.executeQuery();
                while (rs.next()) {
                    EncounterData data = new EncounterData();
                    data.mulai = rs.getString("mulai");
                    data.noRawat = rs.getString("no_rawat");
                    data.noRkmMedis = rs.getString("no_rkm_medis");
                    data.namaPasien = rs.getString("nm_pasien");
                    data.ktpPasien = rs.getString("ktp_pasien");
                    data.namaDokter = rs.getString("nm_dokter");
                    data.ktpDokter = rs.getString("ktp_dokter");
                    data.namaPoli = rs.getString("nm_poli");
                    data.idLokasiSatusehat = rs.getString("id_lokasi_satusehat");
                    data.statusLanjut = rs.getString("status_lanjut");
                    data.pulang = rs.getString("pulang");
                    if (kirimEncounter(data, token)) {
                        sukses++;
                    } else {
                        gagal++;
                    }
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }

            lepasLock(serviceName, "SELESAI", "Encounter otomatis selesai. Sukses " + sukses + ", gagal " + gagal);
        } catch (Exception e) {
            lepasLock(serviceName, "GAGAL", "Encounter otomatis gagal. " + potongPesan(e.toString()));
            System.out.println("Notifikasi Auto Encounter : " + e);
        }
    }

    private void runConditionJob() {
        final String serviceName = "condition";
        if (!ambilLock(serviceName)) {
            return;
        }

        int sukses = 0;
        int gagal = 0;
        try {
            String token = api.TokenSatuSehat();
            if (token.equals("")) {
                throw new Exception("Token Satu Sehat kosong");
            }

            PreparedStatement ps = koneksi.prepareStatement(
                    "select data_kondisi.registrasi,data_kondisi.no_rawat,data_kondisi.no_rkm_medis,data_kondisi.nm_pasien,data_kondisi.ktp_pasien," +
                    "data_kondisi.status_lanjut,data_kondisi.pulang,data_kondisi.id_encounter,data_kondisi.kd_penyakit,data_kondisi.nm_penyakit,data_kondisi.status_diagnosa " +
                    "from (" +
                    "select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) as registrasi,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien," +
                    "pasien.no_ktp as ktp_pasien,reg_periksa.status_lanjut,concat(nota_jalan.tanggal,' ',nota_jalan.jam) as pulang,satu_sehat_encounter.id_encounter," +
                    "diagnosa_pasien.kd_penyakit,penyakit.nm_penyakit,diagnosa_pasien.status as status_diagnosa " +
                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis inner join nota_jalan on nota_jalan.no_rawat=reg_periksa.no_rawat " +
                    "inner join satu_sehat_encounter on satu_sehat_encounter.no_rawat=reg_periksa.no_rawat inner join diagnosa_pasien on diagnosa_pasien.no_rawat=reg_periksa.no_rawat " +
                    "inner join penyakit on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit left join satu_sehat_condition on satu_sehat_condition.no_rawat=diagnosa_pasien.no_rawat " +
                    "and satu_sehat_condition.kd_penyakit=diagnosa_pasien.kd_penyakit and satu_sehat_condition.status=diagnosa_pasien.status " +
                    "where nota_jalan.tanggal between date_sub(curdate(), interval " + getConditionLookbackDays() + " day) and curdate() " +
                    "and ifnull(satu_sehat_encounter.id_encounter,'')<>'' and ifnull(satu_sehat_condition.id_condition,'')='' and ifnull(pasien.no_ktp,'')<>'' " +
                    "union all " +
                    "select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) as registrasi,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien," +
                    "pasien.no_ktp as ktp_pasien,reg_periksa.status_lanjut,concat(nota_inap.tanggal,' ',nota_inap.jam) as pulang,satu_sehat_encounter.id_encounter," +
                    "diagnosa_pasien.kd_penyakit,penyakit.nm_penyakit,diagnosa_pasien.status as status_diagnosa " +
                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis inner join nota_inap on nota_inap.no_rawat=reg_periksa.no_rawat " +
                    "inner join satu_sehat_encounter on satu_sehat_encounter.no_rawat=reg_periksa.no_rawat inner join diagnosa_pasien on diagnosa_pasien.no_rawat=reg_periksa.no_rawat " +
                    "inner join penyakit on diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit left join satu_sehat_condition on satu_sehat_condition.no_rawat=diagnosa_pasien.no_rawat " +
                    "and satu_sehat_condition.kd_penyakit=diagnosa_pasien.kd_penyakit and satu_sehat_condition.status=diagnosa_pasien.status " +
                    "where nota_inap.tanggal between date_sub(curdate(), interval " + getConditionLookbackDays() + " day) and curdate() " +
                    "and ifnull(satu_sehat_encounter.id_encounter,'')<>'' and ifnull(satu_sehat_condition.id_condition,'')='' and ifnull(pasien.no_ktp,'')<>'' " +
                    ") as data_kondisi order by data_kondisi.registrasi,data_kondisi.no_rawat,data_kondisi.kd_penyakit");

            ResultSet rs = null;
            try {
                rs = ps.executeQuery();
                while (rs.next()) {
                    ConditionData data = new ConditionData();
                    data.registrasi = rs.getString("registrasi");
                    data.noRawat = rs.getString("no_rawat");
                    data.noRkmMedis = rs.getString("no_rkm_medis");
                    data.namaPasien = rs.getString("nm_pasien");
                    data.ktpPasien = rs.getString("ktp_pasien");
                    data.statusLanjut = rs.getString("status_lanjut");
                    data.pulang = rs.getString("pulang");
                    data.idEncounter = rs.getString("id_encounter");
                    data.kdPenyakit = rs.getString("kd_penyakit");
                    data.namaPenyakit = rs.getString("nm_penyakit");
                    data.statusDiagnosa = rs.getString("status_diagnosa");
                    if (kirimCondition(data, token)) {
                        sukses++;
                    } else {
                        gagal++;
                    }
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }

            lepasLock(serviceName, "SELESAI", "Condition otomatis selesai. Sukses " + sukses + ", gagal " + gagal);
        } catch (Exception e) {
            lepasLock(serviceName, "GAGAL", "Condition otomatis gagal. " + potongPesan(e.toString()));
            System.out.println("Notifikasi Auto Condition : " + e);
        }
    }

    private boolean kirimEncounter(EncounterData data, String token) {
        try {
            String idDokter = cekViaSatuSehat.tampilIDParktisi(data.ktpDokter);
            if (idDokter.equals("")) {
                catatLog("encounter", "Encounter", data.noRawat, "", data.statusLanjut, "GAGAL", "ID praktisi tidak ditemukan untuk KTP " + data.ktpDokter);
                return false;
            }

            String idPasien = cekViaSatuSehat.tampilIDPasien(data.ktpPasien);
            if (idPasien.equals("")) {
                catatLog("encounter", "Encounter", data.noRawat, "", data.statusLanjut, "GAGAL", "ID pasien tidak ditemukan untuk KTP " + data.ktpPasien);
                return false;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            String requestJson = mapper.writeValueAsString(buatPayloadEncounter(data, idPasien, idDokter));
            System.out.println("URL : " + link + "/Encounter");
            System.out.println("Request JSON Auto Encounter : " + requestJson);
            String result = api.getRest().exchange(link + "/Encounter", HttpMethod.POST, new HttpEntity(requestJson, headers), String.class).getBody();
            System.out.println("Result JSON Auto Encounter : " + result);
            JsonNode root = mapper.readTree(result);
            String idEncounter = root.path("id").asText();
            if (idEncounter.equals("")) {
                catatLog("encounter", "Encounter", data.noRawat, "", data.statusLanjut, "GAGAL", ambilPesanHasil(root, result));
                return false;
            }

            simpanEncounter(data.noRawat, idEncounter);
            catatLog("encounter", "Encounter", data.noRawat, "", data.statusLanjut, "SUKSES", "Encounter terkirim dengan ID " + idEncounter);
            return true;
        } catch (Exception e) {
            catatLog("encounter", "Encounter", data.noRawat, "", data.statusLanjut, "GAGAL", e.toString());
            System.out.println("Notifikasi Kirim Auto Encounter : " + e);
            return false;
        }
    }

    private boolean kirimCondition(ConditionData data, String token) {
        try {
            String idPasien = cekViaSatuSehat.tampilIDPasien(data.ktpPasien);
            if (idPasien.equals("")) {
                catatLog("condition", "Condition", data.noRawat, data.kdPenyakit, data.statusDiagnosa, "GAGAL", "ID pasien tidak ditemukan untuk KTP " + data.ktpPasien);
                return false;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            String requestJson = mapper.writeValueAsString(buatPayloadCondition(data, idPasien));
            System.out.println("URL : " + link + "/Condition");
            System.out.println("Request JSON Auto Condition : " + requestJson);
            String result = api.getRest().exchange(link + "/Condition", HttpMethod.POST, new HttpEntity(requestJson, headers), String.class).getBody();
            System.out.println("Result JSON Auto Condition : " + result);
            JsonNode root = mapper.readTree(result);
            String idCondition = root.path("id").asText();
            if (idCondition.equals("")) {
                catatLog("condition", "Condition", data.noRawat, data.kdPenyakit, data.statusDiagnosa, "GAGAL", ambilPesanHasil(root, result));
                return false;
            }

            simpanCondition(data.noRawat, data.kdPenyakit, data.statusDiagnosa, idCondition);
            catatLog("condition", "Condition", data.noRawat, data.kdPenyakit, data.statusDiagnosa, "SUKSES", "Condition terkirim dengan ID " + idCondition);
            return true;
        } catch (Exception e) {
            catatLog("condition", "Condition", data.noRawat, data.kdPenyakit, data.statusDiagnosa, "GAGAL", e.toString());
            System.out.println("Notifikasi Kirim Auto Condition : " + e);
            return false;
        }
    }

    private ObjectNode buatPayloadEncounter(EncounterData data, String idPasien, String idDokter) {
        ObjectNode payload = mapper.createObjectNode();
        ObjectNode kelas = payload.put("resourceType", "Encounter").put("status", "arrived").putObject("class");
        kelas.put("system", "http://terminology.hl7.org/CodeSystem/v3-ActCode");
        kelas.put("code", data.statusLanjut.equals("Ralan") ? "AMB" : "IMP");
        kelas.put("display", data.statusLanjut.equals("Ralan") ? "ambulatory" : "inpatient encounter");

        ObjectNode subject = payload.putObject("subject");
        subject.put("reference", "Patient/" + idPasien);
        subject.put("display", data.namaPasien);

        ArrayNode participant = payload.putArray("participant");
        ObjectNode participantItem = participant.addObject();
        ArrayNode participantType = participantItem.putArray("type");
        ObjectNode participantCodingWrapper = participantType.addObject();
        ArrayNode participantCoding = participantCodingWrapper.putArray("coding");
        ObjectNode participantCodingItem = participantCoding.addObject();
        participantCodingItem.put("system", "http://terminology.hl7.org/CodeSystem/v3-ParticipationType");
        participantCodingItem.put("code", "ATND");
        participantCodingItem.put("display", "attender");
        ObjectNode individual = participantItem.putObject("individual");
        individual.put("reference", "Practitioner/" + idDokter);
        individual.put("display", data.namaDokter);

        payload.putObject("period").put("start", data.mulai);

        ArrayNode location = payload.putArray("location");
        ObjectNode locationItem = location.addObject();
        ObjectNode locationObject = locationItem.putObject("location");
        locationObject.put("reference", "Location/" + data.idLokasiSatusehat);
        locationObject.put("display", data.namaPoli);

        ArrayNode statusHistory = payload.putArray("statusHistory");
        ObjectNode statusHistoryItem = statusHistory.addObject();
        statusHistoryItem.put("status", "arrived");
        ObjectNode period = statusHistoryItem.putObject("period");
        period.put("start", data.mulai);
        period.put("end", data.pulang);

        payload.putObject("serviceProvider").put("reference", "Organization/" + koneksiDB.IDSATUSEHAT());

        ArrayNode identifier = payload.putArray("identifier");
        ObjectNode identifierItem = identifier.addObject();
        identifierItem.put("system", "http://sys-ids.kemkes.go.id/encounter/" + koneksiDB.IDSATUSEHAT());
        identifierItem.put("value", data.noRawat);
        return payload;
    }

    private ObjectNode buatPayloadCondition(ConditionData data, String idPasien) {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("resourceType", "Condition");

        ObjectNode clinicalStatus = payload.putObject("clinicalStatus");
        ArrayNode clinicalStatusCoding = clinicalStatus.putArray("coding");
        ObjectNode clinicalStatusItem = clinicalStatusCoding.addObject();
        clinicalStatusItem.put("system", "http://terminology.hl7.org/CodeSystem/condition-clinical");
        clinicalStatusItem.put("code", "active");
        clinicalStatusItem.put("display", "Active");

        ArrayNode category = payload.putArray("category");
        ObjectNode categoryItem = category.addObject();
        ArrayNode categoryCoding = categoryItem.putArray("coding");
        ObjectNode categoryCodingItem = categoryCoding.addObject();
        categoryCodingItem.put("system", "http://terminology.hl7.org/CodeSystem/condition-category");
        categoryCodingItem.put("code", "encounter-diagnosis");
        categoryCodingItem.put("display", "Encounter Diagnosis");

        ObjectNode code = payload.putObject("code");
        ArrayNode codeCoding = code.putArray("coding");
        ObjectNode codeCodingItem = codeCoding.addObject();
        codeCodingItem.put("system", "http://hl7.org/fhir/sid/icd-10");
        codeCodingItem.put("code", data.kdPenyakit);
        codeCodingItem.put("display", data.namaPenyakit);

        ObjectNode subject = payload.putObject("subject");
        subject.put("reference", "Patient/" + idPasien);
        subject.put("display", data.namaPasien);

        ObjectNode encounter = payload.putObject("encounter");
        encounter.put("reference", "Encounter/" + data.idEncounter);
        encounter.put("display", "Diagnosa " + data.namaPasien + " selama kunjungan/dirawat dari tanggal " + data.registrasi + " sampai " + data.pulang);
        return payload;
    }

    private void simpanEncounter(String noRawat, String idEncounter) throws Exception {
        PreparedStatement ps = koneksi.prepareStatement(
                "insert into satu_sehat_encounter(no_rawat,id_encounter) values(?,?) " +
                "on duplicate key update id_encounter=values(id_encounter)");
        try {
            ps.setString(1, noRawat);
            ps.setString(2, idEncounter);
            ps.executeUpdate();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    private void simpanCondition(String noRawat, String kdPenyakit, String statusDiagnosa, String idCondition) throws Exception {
        PreparedStatement ps = koneksi.prepareStatement(
                "insert into satu_sehat_condition(no_rawat,kd_penyakit,status,id_condition) values(?,?,?,?) " +
                "on duplicate key update id_condition=values(id_condition)");
        try {
            ps.setString(1, noRawat);
            ps.setString(2, kdPenyakit);
            ps.setString(3, statusDiagnosa);
            ps.setString(4, idCondition);
            ps.executeUpdate();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    private boolean ambilLock(String serviceName) {
        PreparedStatement insert = null;
        PreparedStatement update = null;
        try {
            insert = koneksi.prepareStatement(
                    "insert ignore into satu_sehat_auto_service_lock(service_name,locked_until,locked_by,last_started_at,last_finished_at,last_status,last_message) " +
                    "values(?,null,null,null,null,null,null)");
            insert.setString(1, serviceName);
            insert.executeUpdate();

            update = koneksi.prepareStatement(
                    "update satu_sehat_auto_service_lock set locked_until=date_add(now(), interval ? minute),locked_by=?,last_started_at=now()," +
                    "last_status='BERJALAN',last_message=? where service_name=? and (locked_until is null or locked_until < now() or locked_by=?)");
            update.setInt(1, LOCK_TTL_MINUTES);
            update.setString(2, workerName);
            update.setString(3, potongPesan("Worker " + workerName + " mulai memproses " + serviceName));
            update.setString(4, serviceName);
            update.setString(5, workerName);
            return update.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Gagal ambil lock " + serviceName + " : " + e);
            return false;
        } finally {
            try {
                if (insert != null) {
                    insert.close();
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
            try {
                if (update != null) {
                    update.close();
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
        }
    }

    private void lepasLock(String serviceName, String status, String message) {
        PreparedStatement ps = null;
        try {
            ps = koneksi.prepareStatement(
                    "update satu_sehat_auto_service_lock set locked_until=now(),locked_by=null,last_finished_at=now(),last_status=?,last_message=? where service_name=?");
            ps.setString(1, status);
            ps.setString(2, potongPesan(message));
            ps.setString(3, serviceName);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Gagal melepas lock " + serviceName + " : " + e);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
        }
    }

    private void catatLog(String serviceName, String resourceType, String noRawat, String kdPenyakit, String statusRawat, String statusKirim, String keterangan) {
        PreparedStatement ps = null;
        try {
            ps = koneksi.prepareStatement(
                    "insert into satu_sehat_auto_service_log(service_name,resource_type,no_rawat,kd_penyakit,status_rawat,status_kirim,keterangan,dibuat_pada) " +
                    "values(?,?,?,?,?,?,?,now())");
            ps.setString(1, serviceName);
            ps.setString(2, resourceType);
            ps.setString(3, noRawat);
            ps.setString(4, kdPenyakit);
            ps.setString(5, statusRawat);
            ps.setString(6, statusKirim);
            ps.setString(7, potongPesan(keterangan));
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Gagal mencatat log auto Satu Sehat : " + e);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
        }
    }

    private void ensureSupportTables() throws Exception {
        PreparedStatement psConfig = null;
        PreparedStatement psLock = null;
        PreparedStatement psLog = null;
        try {
            psConfig = koneksi.prepareStatement(
                    "create table if not exists satu_sehat_auto_service_config (" +
                    "nama_config varchar(60) not null," +
                    "nilai_config varchar(200) default null," +
                    "primary key(nama_config)) engine=InnoDB default charset=latin1");
            psConfig.executeUpdate();

            psLock = koneksi.prepareStatement(
                    "create table if not exists satu_sehat_auto_service_lock (" +
                    "service_name varchar(40) not null," +
                    "locked_until datetime default null," +
                    "locked_by varchar(120) default null," +
                    "last_started_at datetime default null," +
                    "last_finished_at datetime default null," +
                    "last_status varchar(20) default null," +
                    "last_message varchar(500) default null," +
                    "primary key(service_name)) engine=InnoDB default charset=latin1");
            psLock.executeUpdate();

            psLog = koneksi.prepareStatement(
                    "create table if not exists satu_sehat_auto_service_log (" +
                    "id bigint not null auto_increment," +
                    "service_name varchar(40) not null," +
                    "resource_type varchar(30) not null," +
                    "no_rawat varchar(17) default null," +
                    "kd_penyakit varchar(10) default null," +
                    "status_rawat varchar(10) default null," +
                    "status_kirim varchar(20) not null," +
                    "keterangan varchar(500) default null," +
                    "dibuat_pada datetime not null," +
                    "primary key(id)," +
                    "key service_date(service_name,dibuat_pada)," +
                    "key rawat_idx(no_rawat,kd_penyakit,status_rawat)) engine=InnoDB default charset=latin1");
            psLog.executeUpdate();

            setConfigDefault("aktif", "yes");
            setConfigDefault("encounter_interval_minutes", String.valueOf(DEFAULT_ENCOUNTER_INTERVAL_MINUTES));
            setConfigDefault("condition_interval_minutes", String.valueOf(DEFAULT_CONDITION_INTERVAL_MINUTES));
            setConfigDefault("encounter_lookback_days", String.valueOf(DEFAULT_ENCOUNTER_LOOKBACK_DAYS));
            setConfigDefault("condition_lookback_days", String.valueOf(DEFAULT_CONDITION_LOOKBACK_DAYS));
        } finally {
            if (psConfig != null) {
                psConfig.close();
            }
            if (psLock != null) {
                psLock.close();
            }
            if (psLog != null) {
                psLog.close();
            }
        }
    }

    private void setConfigDefault(String namaConfig, String nilaiDefault) throws Exception {
        PreparedStatement ps = koneksi.prepareStatement(
                "insert ignore into satu_sehat_auto_service_config(nama_config,nilai_config) values(?,?)");
        try {
            ps.setString(1, namaConfig);
            ps.setString(2, nilaiDefault);
            ps.executeUpdate();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    private void setConfigValue(String namaConfig, String nilaiConfig) throws Exception {
        PreparedStatement ps = koneksi.prepareStatement(
                "insert into satu_sehat_auto_service_config(nama_config,nilai_config) values(?,?) " +
                "on duplicate key update nilai_config=values(nilai_config)");
        try {
            ps.setString(1, namaConfig);
            ps.setString(2, nilaiConfig);
            ps.executeUpdate();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    private String getConfigValue(String namaConfig, String nilaiDefault) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ensureSupportTables();
            ps = koneksi.prepareStatement("select ifnull(nilai_config,?) as nilai_config from satu_sehat_auto_service_config where nama_config=?");
            ps.setString(1, nilaiDefault);
            ps.setString(2, namaConfig);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("nilai_config");
            }
        } catch (Exception e) {
            System.out.println("Gagal membaca config Auto Satu Sehat : " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            }
        }
        return nilaiDefault;
    }

    private int getConfigInt(String namaConfig, int nilaiDefault) {
        try {
            return Integer.parseInt(getConfigValue(namaConfig, String.valueOf(nilaiDefault)));
        } catch (Exception e) {
            return nilaiDefault;
        }
    }

    public int getEncounterIntervalMinutes() {
        return getConfigInt("encounter_interval_minutes", DEFAULT_ENCOUNTER_INTERVAL_MINUTES);
    }

    public int getConditionIntervalMinutes() {
        return getConfigInt("condition_interval_minutes", DEFAULT_CONDITION_INTERVAL_MINUTES);
    }

    public int getEncounterLookbackDays() {
        return getConfigInt("encounter_lookback_days", DEFAULT_ENCOUNTER_LOOKBACK_DAYS);
    }

    public int getConditionLookbackDays() {
        return getConfigInt("condition_lookback_days", DEFAULT_CONDITION_LOOKBACK_DAYS);
    }

    private String buildWorkerName() {
        String komputer = System.getenv("COMPUTERNAME");
        if (komputer == null || komputer.equals("")) {
            komputer = "UNKNOWN-PC";
        }

        String user = akses.getkode();
        if (user == null || user.equals("")) {
            user = "UNKNOWN-USER";
        }

        return komputer + "-" + user.replaceAll("\\s+", "_");
    }

    private String ambilPesanHasil(JsonNode root, String rawResult) {
        String pesan = root.path("issue").isArray() && root.path("issue").size() > 0
                ? root.path("issue").get(0).path("details").path("text").asText()
                : "";
        if (pesan.equals("")) {
            pesan = root.path("message").asText();
        }
        if (pesan.equals("")) {
            pesan = rawResult;
        }
        return potongPesan(pesan);
    }

    private String potongPesan(String pesan) {
        if (pesan == null) {
            return "";
        }
        pesan = pesan.replace('\n', ' ').replace('\r', ' ').trim();
        if (pesan.length() > MESSAGE_LIMIT) {
            return pesan.substring(0, MESSAGE_LIMIT);
        }
        return pesan;
    }

    private static class EncounterData {
        private String mulai = "";
        private String noRawat = "";
        private String noRkmMedis = "";
        private String namaPasien = "";
        private String ktpPasien = "";
        private String namaDokter = "";
        private String ktpDokter = "";
        private String namaPoli = "";
        private String idLokasiSatusehat = "";
        private String statusLanjut = "";
        private String pulang = "";
    }

    private static class ConditionData {
        private String registrasi = "";
        private String noRawat = "";
        private String noRkmMedis = "";
        private String namaPasien = "";
        private String ktpPasien = "";
        private String statusLanjut = "";
        private String pulang = "";
        private String idEncounter = "";
        private String kdPenyakit = "";
        private String namaPenyakit = "";
        private String statusDiagnosa = "";
    }
}
