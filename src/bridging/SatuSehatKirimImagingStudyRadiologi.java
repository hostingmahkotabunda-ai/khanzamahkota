/*
  Custom imaging study bridge for radiology
 */

package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import widget.Tanggal;

public final class SatuSehatKirimImagingStudyRadiologi extends JDialog {
    private static final SimpleDateFormat SQL_DATE = new SimpleDateFormat("yyyy-MM-dd");
    private final DefaultTableModel tabMode;
    private final JTable tbData = new JTable();
    private final Tanggal DTPCari1 = new Tanggal();
    private final Tanggal DTPCari2 = new Tanggal();
    private final JTextField TCari = new JTextField(24);
    private final JButton BtnCari = new JButton("Cari");
    private final JButton BtnSemua = new JButton("Semua");
    private final JButton BtnCariOrthanc = new JButton("Cari DICOM");
    private final JButton BtnKirim = new JButton("Kirim");
    private final JButton BtnUpdate = new JButton("Update");
    private final JButton BtnKeluar = new JButton("Keluar");
    private final JPopupMenu popup = new JPopupMenu();
    private final JButton ppPilihSemua = new JButton("Pilih Semua");
    private final JButton ppBersihkan = new JButton("Bersihkan");
    private final JLabel LCount = new JLabel("0");

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final ApiSatuSehat api = new ApiSatuSehat();
    private final ApiOrthanc orthanc = new ApiOrthanc();
    private final SatuSehatCekNIK cekViaSatuSehat = new SatuSehatCekNIK();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, JsonNode> cacheOrthanc = new HashMap<>();
    private String link = "";

    public SatuSehatKirimImagingStudyRadiologi(Frame parent, boolean modal) {
        super(parent, modal);
        ensureSupportTable();
        try {
            link = koneksiDB.URLFHIRSATUSEHAT();
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }

        tabMode = new DefaultTableModel(null, new String[]{
            "P", "No.Rawat", "No.RM", "Nama Pasien", "No.KTP Pasien", "Kode Dokter", "Nama Dokter",
            "No.KTP Dokter", "ID Encounter", "No.Permintaan", "Tgl & Jam Hasil", "Nama Pemeriksaan",
            "Kode Pemeriksaan", "ID ServiceRequest", "Code", "System", "Display", "Orthanc Study ID",
            "Orthanc Series ID", "Study UID", "Series UID", "Modality", "Jml Instance", "ID ImagingStudy"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return colIndex == 0;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
        };

        initComponents();
        tampil();
    }

    private void initComponents() {
        setTitle("::[ Kirim ImagingStudy Radiologi Satu Sehat ]::");
        setSize(1100, 700);
        setLocation(10, 2);
        setLayout(new BorderLayout(6, 6));

        TCari.setDocument(new batasInput((byte) 100).getKata(TCari));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        top.add(new JLabel("Mulai"));
        top.add(DTPCari1);
        top.add(new JLabel("Sampai"));
        top.add(DTPCari2);
        top.add(new JLabel("Cari"));
        top.add(TCari);
        top.add(BtnCari);
        top.add(BtnSemua);
        top.add(BtnCariOrthanc);
        top.add(BtnKirim);
        top.add(BtnUpdate);
        top.add(BtnKeluar);
        top.add(new JLabel("Data:"));
        top.add(LCount);
        add(top, BorderLayout.NORTH);

        tbData.setModel(tabMode);
        tbData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbData.setPreferredScrollableViewportSize(new Dimension(1000, 500));
        tbData.setDefaultRenderer(Object.class, new WarnaTable());
        for (int i = 0; i < tabMode.getColumnCount(); i++) {
            TableColumn column = tbData.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(30);
            } else if (i == 1 || i == 9) {
                column.setPreferredWidth(120);
            } else if (i == 2 || i == 5) {
                column.setPreferredWidth(85);
            } else if (i == 3 || i == 6 || i == 11) {
                column.setPreferredWidth(180);
            } else if (i == 4 || i == 7) {
                column.setPreferredWidth(130);
            } else if (i == 8 || i == 13 || i == 17 || i == 18 || i == 19 || i == 20 || i == 23) {
                column.setPreferredWidth(180);
            } else if (i == 10) {
                column.setPreferredWidth(140);
            } else if (i == 12) {
                column.setPreferredWidth(110);
            } else if (i == 14 || i == 15 || i == 16) {
                column.setPreferredWidth(150);
            } else {
                column.setPreferredWidth(100);
            }
        }
        add(new JScrollPane(tbData), BorderLayout.CENTER);

        popup.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
        popup.add(ppPilihSemua);
        popup.add(ppBersihkan);
        tbData.setComponentPopupMenu(popup);

        BtnCari.addActionListener(evt -> tampil());
        BtnSemua.addActionListener(evt -> {
            TCari.setText("");
            tampil();
        });
        BtnCariOrthanc.addActionListener(evt -> cariOrthancTerpilih());
        BtnKirim.addActionListener(evt -> kirim(false));
        BtnUpdate.addActionListener(evt -> kirim(true));
        BtnKeluar.addActionListener(evt -> dispose());
        ppPilihSemua.addActionListener(evt -> pilihSemua(true));
        ppBersihkan.addActionListener(evt -> pilihSemua(false));

        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    tampil();
                }
            }
        });
    }

    private void ensureSupportTable() {
        try {
            Sequel.queryu("create table if not exists satu_sehat_imagingstudy_radiologi(" +
                    "noorder varchar(15) not null," +
                    "kd_jenis_prw varchar(15) not null," +
                    "orthanc_study_id varchar(80) default null," +
                    "orthanc_series_id varchar(80) default null," +
                    "study_uid varchar(120) default null," +
                    "series_uid varchar(120) default null," +
                    "modality varchar(20) default null," +
                    "number_of_instances int default null," +
                    "id_imagingstudy varchar(80) default null," +
                    "primary key(noorder,kd_jenis_prw))");
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement(
                "select reg_periksa.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.no_ktp,periksa_radiologi.kd_dokter,pegawai.nama,pegawai.no_ktp as ktpdokter," +
                "satu_sehat_encounter.id_encounter,permintaan_radiologi.noorder,permintaan_radiologi.tgl_hasil,permintaan_radiologi.jam_hasil," +
                "jns_perawatan_radiologi.nm_perawatan,permintaan_pemeriksaan_radiologi.kd_jenis_prw,satu_sehat_servicerequest_radiologi.id_servicerequest," +
                "ifnull(satu_sehat_mapping_radiologi.code,'') as code,ifnull(satu_sehat_mapping_radiologi.system,'') as system,ifnull(satu_sehat_mapping_radiologi.display,'') as display," +
                "ifnull(satu_sehat_imagingstudy_radiologi.orthanc_study_id,'') as orthanc_study_id,ifnull(satu_sehat_imagingstudy_radiologi.orthanc_series_id,'') as orthanc_series_id," +
                "ifnull(satu_sehat_imagingstudy_radiologi.study_uid,'') as study_uid,ifnull(satu_sehat_imagingstudy_radiologi.series_uid,'') as series_uid," +
                "ifnull(satu_sehat_imagingstudy_radiologi.modality,'') as modality,ifnull(satu_sehat_imagingstudy_radiologi.number_of_instances,'') as number_of_instances," +
                "ifnull(satu_sehat_imagingstudy_radiologi.id_imagingstudy,'') as id_imagingstudy " +
                "from reg_periksa " +
                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                "inner join permintaan_radiologi on permintaan_radiologi.no_rawat=reg_periksa.no_rawat " +
                "inner join permintaan_pemeriksaan_radiologi on permintaan_pemeriksaan_radiologi.noorder=permintaan_radiologi.noorder " +
                "inner join jns_perawatan_radiologi on jns_perawatan_radiologi.kd_jenis_prw=permintaan_pemeriksaan_radiologi.kd_jenis_prw " +
                "left join satu_sehat_mapping_radiologi on satu_sehat_mapping_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw " +
                "inner join satu_sehat_servicerequest_radiologi on satu_sehat_servicerequest_radiologi.noorder=permintaan_pemeriksaan_radiologi.noorder and satu_sehat_servicerequest_radiologi.kd_jenis_prw=permintaan_pemeriksaan_radiologi.kd_jenis_prw " +
                "inner join satu_sehat_encounter on satu_sehat_encounter.no_rawat=reg_periksa.no_rawat " +
                "inner join periksa_radiologi on periksa_radiologi.no_rawat=permintaan_radiologi.no_rawat and periksa_radiologi.tgl_periksa=permintaan_radiologi.tgl_hasil and periksa_radiologi.jam=permintaan_radiologi.jam_hasil and periksa_radiologi.dokter_perujuk=permintaan_radiologi.dokter_perujuk " +
                "inner join pegawai on periksa_radiologi.kd_dokter=pegawai.nik " +
                "left join satu_sehat_imagingstudy_radiologi on satu_sehat_imagingstudy_radiologi.noorder=permintaan_pemeriksaan_radiologi.noorder and satu_sehat_imagingstudy_radiologi.kd_jenis_prw=permintaan_pemeriksaan_radiologi.kd_jenis_prw " +
                "where reg_periksa.tgl_registrasi between ? and ? " +
                (TCari.getText().trim().isEmpty() ? "" :
                    "and (reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or pasien.nm_pasien like ? or pasien.no_ktp like ? or jns_perawatan_radiologi.nm_perawatan like ? or permintaan_radiologi.noorder like ?) ") +
                "order by permintaan_radiologi.tgl_hasil desc,permintaan_radiologi.jam_hasil desc");
            ps.setString(1, getSqlDate(DTPCari1));
            ps.setString(2, getSqlDate(DTPCari2));
            if (!TCari.getText().trim().isEmpty()) {
                for (int i = 3; i <= 8; i++) {
                    ps.setString(i, "%" + TCari.getText().trim() + "%");
                }
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                tabMode.addRow(new Object[]{
                    false,
                    rs.getString("no_rawat"),
                    rs.getString("no_rkm_medis"),
                    rs.getString("nm_pasien"),
                    rs.getString("no_ktp"),
                    rs.getString("kd_dokter"),
                    rs.getString("nama"),
                    rs.getString("ktpdokter"),
                    rs.getString("id_encounter"),
                    rs.getString("noorder"),
                    rs.getString("tgl_hasil") + " " + rs.getString("jam_hasil"),
                    rs.getString("nm_perawatan"),
                    rs.getString("kd_jenis_prw"),
                    rs.getString("id_servicerequest"),
                    rs.getString("code"),
                    rs.getString("system"),
                    rs.getString("display"),
                    rs.getString("orthanc_study_id"),
                    rs.getString("orthanc_series_id"),
                    rs.getString("study_uid"),
                    rs.getString("series_uid"),
                    rs.getString("modality"),
                    rs.getString("number_of_instances"),
                    rs.getString("id_imagingstudy")
                });
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ex) {
                System.out.println("Notifikasi : " + ex);
            }
        }
        LCount.setText(String.valueOf(tabMode.getRowCount()));
    }

    private String getSqlDate(Tanggal tanggal) {
        try {
            if (tanggal.getDate() != null) {
                return SQL_DATE.format(tanggal.getDate());
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
        return Valid.SetTgl(tanggal.getSelectedItem() + "");
    }

    private void pilihSemua(boolean value) {
        for (int i = 0; i < tbData.getRowCount(); i++) {
            tbData.setValueAt(value, i, 0);
        }
    }

    private void cariOrthancTerpilih() {
        int row = tbData.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Silakan pilih data radiologi terlebih dahulu.");
            return;
        }
        try {
            OrthancSeriesInfo info = resolveOrthancData(row);
            if (info == null) {
                JOptionPane.showMessageDialog(this, "Series DICOM Orthanc belum ditemukan untuk data ini.");
                return;
            }
            simpanOrthancInfo(row, info, tbData.getValueAt(row, 23).toString());
            tampil();
            JOptionPane.showMessageDialog(this,
                "Series ditemukan.\nStudy UID: " + info.studyUid + "\nSeries UID: " + info.seriesUid + "\nModality: " + info.modality);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mencari series Orthanc.\n" + e.getMessage());
            System.out.println("Notifikasi : " + e);
        }
    }

    private void kirim(boolean update) {
        for (int i = 0; i < tbData.getRowCount(); i++) {
            if (!"true".equals(String.valueOf(tbData.getValueAt(i, 0)))) {
                continue;
            }
            try {
                if (update && tbData.getValueAt(i, 23).toString().trim().isEmpty()) {
                    continue;
                }
                if (!update && !tbData.getValueAt(i, 23).toString().trim().isEmpty()) {
                    continue;
                }

                String patientId = cekViaSatuSehat.tampilIDPasien(tbData.getValueAt(i, 4).toString());
                String practitionerId = cekViaSatuSehat.tampilIDParktisi(tbData.getValueAt(i, 7).toString());
                if (patientId.isEmpty() || practitionerId.isEmpty()) {
                    System.out.println("Notifikasi : ID Patient/Practitioner belum ditemukan untuk row " + i);
                    continue;
                }

                OrthancSeriesInfo info = resolveOrthancData(i);
                if (info == null || info.seriesUid.isEmpty() || info.modality.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Series DICOM Orthanc tidak ditemukan atau belum lengkap untuk pemeriksaan " + tbData.getValueAt(i, 11).toString());
                    continue;
                }

                String payload = buildPayload(i, patientId, practitionerId, info, update);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("Authorization", "Bearer " + api.TokenSatuSehat());
                HttpEntity<String> requestEntity = new HttpEntity<>(payload, headers);
                String url = link + "/ImagingStudy" + (update ? "/" + tbData.getValueAt(i, 23).toString() : "");
                System.out.println("URL : " + url);
                System.out.println("Request JSON : " + payload);
                String response = api.getRest().exchange(url, update ? HttpMethod.PUT : HttpMethod.POST, requestEntity, String.class).getBody();
                System.out.println("Result JSON : " + response);
                JsonNode root = mapper.readTree(response);
                String idImagingStudy = update ? tbData.getValueAt(i, 23).toString() : root.path("id").asText();
                if (!idImagingStudy.isEmpty()) {
                    simpanOrthancInfo(i, info, idImagingStudy);
                    tbData.setValueAt(idImagingStudy, i, 23);
                    tbData.setValueAt(false, i, 0);
                }
            } catch (HttpClientErrorException e) {
                System.out.println("Notifikasi Bridging : " + e);
                System.out.println("Response Body : " + e.getResponseBodyAsString());
            } catch (Exception e) {
                System.out.println("Notifikasi : " + e);
            }
        }
    }

    private String buildPayload(int row, String patientId, String practitionerId, OrthancSeriesInfo info, boolean update) throws Exception {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("resourceType", "ImagingStudy");
        if (update) {
            payload.put("id", tbData.getValueAt(row, 23).toString());
        }

        ArrayNode identifier = payload.putArray("identifier");
        ObjectNode ident = identifier.addObject();
        ident.put("use", "usual");
        ident.put("system", "http://sys-ids.kemkes.go.id/acsn/" + koneksiDB.IDSATUSEHAT());
        ident.put("value", tbData.getValueAt(row, 9).toString() + "." + tbData.getValueAt(row, 12).toString());

        payload.put("status", "available");

        ArrayNode modality = payload.putArray("modality");
        ObjectNode modalityNode = modality.addObject();
        modalityNode.put("system", "http://dicom.nema.org/resources/ontology/DCM");
        modalityNode.put("code", info.modality);
        modalityNode.put("display", modalityDisplay(info.modality));

        ObjectNode subject = payload.putObject("subject");
        subject.put("reference", "Patient/" + patientId);
        subject.put("display", tbData.getValueAt(row, 3).toString());

        if (!tbData.getValueAt(row, 8).toString().trim().isEmpty()) {
            ObjectNode encounter = payload.putObject("encounter");
            encounter.put("reference", "Encounter/" + tbData.getValueAt(row, 8).toString());
        }

        payload.put("started", tbData.getValueAt(row, 10).toString().replace(" ", "T") + "+07:00");

        ArrayNode basedOn = payload.putArray("basedOn");
        ObjectNode basedOnNode = basedOn.addObject();
        basedOnNode.put("reference", "ServiceRequest/" + tbData.getValueAt(row, 13).toString());

        ArrayNode interpreter = payload.putArray("interpreter");
        ObjectNode interpreterNode = interpreter.addObject();
        interpreterNode.put("reference", "Practitioner/" + practitionerId);
        interpreterNode.put("display", tbData.getValueAt(row, 6).toString());

        payload.put("numberOfSeries", 1);
        payload.put("numberOfInstances", info.numberOfInstances);
        payload.put("description", "Imaging study radiologi " + tbData.getValueAt(row, 11).toString() + " No.Rawat " + tbData.getValueAt(row, 1).toString());

        if (isValidProcedureCode(row)) {
            ArrayNode procedureCode = payload.putArray("procedureCode");
            ObjectNode procedureConcept = procedureCode.addObject();
            ArrayNode coding = procedureConcept.putArray("coding");
            ObjectNode proc = coding.addObject();
            proc.put("system", tbData.getValueAt(row, 15).toString());
            proc.put("code", tbData.getValueAt(row, 14).toString());
            proc.put("display", tbData.getValueAt(row, 16).toString());
        }

        ArrayNode series = payload.putArray("series");
        ObjectNode seriesNode = series.addObject();
        seriesNode.put("uid", info.seriesUid);
        seriesNode.put("number", 1);
        ObjectNode seriesModality = seriesNode.putObject("modality");
        seriesModality.put("system", "http://dicom.nema.org/resources/ontology/DCM");
        seriesModality.put("code", info.modality);
        seriesModality.put("display", modalityDisplay(info.modality));
        if (!info.seriesDescription.isEmpty()) {
            seriesNode.put("description", info.seriesDescription);
        } else {
            seriesNode.put("description", tbData.getValueAt(row, 11).toString());
        }
        if (info.numberOfInstances > 0) {
            seriesNode.put("numberOfInstances", info.numberOfInstances);
        }

        return mapper.writeValueAsString(payload);
    }

    private boolean isValidProcedureCode(int row) {
        String system = tbData.getValueAt(row, 15).toString().trim().toLowerCase(Locale.ENGLISH);
        String code = tbData.getValueAt(row, 14).toString().trim();
        String display = tbData.getValueAt(row, 16).toString().trim();
        return !code.isEmpty() && !display.isEmpty() &&
               (system.contains("loinc.org") || system.contains("terminology.kemkes.go.id/codesystem/kptl"));
    }

    private void simpanOrthancInfo(int row, OrthancSeriesInfo info, String idImagingStudy) throws Exception {
        PreparedStatement ps = null;
        try {
            ps = koneksi.prepareStatement(
                "replace into satu_sehat_imagingstudy_radiologi(noorder,kd_jenis_prw,orthanc_study_id,orthanc_series_id,study_uid,series_uid,modality,number_of_instances,id_imagingstudy) values(?,?,?,?,?,?,?,?,?)");
            ps.setString(1, tbData.getValueAt(row, 9).toString());
            ps.setString(2, tbData.getValueAt(row, 12).toString());
            ps.setString(3, info.studyId);
            ps.setString(4, info.seriesId);
            ps.setString(5, info.studyUid);
            ps.setString(6, info.seriesUid);
            ps.setString(7, info.modality);
            ps.setInt(8, info.numberOfInstances);
            ps.setString(9, idImagingStudy);
            ps.executeUpdate();
            tbData.setValueAt(info.studyId, row, 17);
            tbData.setValueAt(info.seriesId, row, 18);
            tbData.setValueAt(info.studyUid, row, 19);
            tbData.setValueAt(info.seriesUid, row, 20);
            tbData.setValueAt(info.modality, row, 21);
            tbData.setValueAt(String.valueOf(info.numberOfInstances), row, 22);
            tbData.setValueAt(idImagingStudy, row, 23);
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    private OrthancSeriesInfo resolveOrthancData(int row) throws Exception {
        if (!tbData.getValueAt(row, 20).toString().trim().isEmpty() && !tbData.getValueAt(row, 21).toString().trim().isEmpty()) {
            return new OrthancSeriesInfo(
                    tbData.getValueAt(row, 17).toString(),
                    tbData.getValueAt(row, 18).toString(),
                    tbData.getValueAt(row, 19).toString(),
                    tbData.getValueAt(row, 20).toString(),
                    tbData.getValueAt(row, 21).toString(),
                    parseInt(tbData.getValueAt(row, 22).toString()),
                    tbData.getValueAt(row, 11).toString()
            );
        }

        String noRm = tbData.getValueAt(row, 2).toString();
        String noOrder = tbData.getValueAt(row, 9).toString();
        String namaPemeriksaan = normalize(tbData.getValueAt(row, 11).toString());
        String tanggal = tbData.getValueAt(row, 10).toString().split(" ")[0].replace("-", "");
        JsonNode studies = orthanc.AmbilSeries(noRm, tanggal, tanggal);
        if (studies == null || !studies.isArray() || studies.size() == 0) {
            return null;
        }

        OrthancSeriesInfo best = null;
        int bestScore = Integer.MIN_VALUE;
        for (JsonNode study : studies) {
            String studyId = study.path("ID").asText();
            JsonNode studyTags = study.path("MainDicomTags");
            String studyDesc = normalize(
                    studyTags.path("StudyDescription").asText() + " " +
                    studyTags.path("RequestedProcedureDescription").asText() + " " +
                    studyTags.path("AccessionNumber").asText()
            );
            int studyBase = 0;
            if (!noOrder.isEmpty() && textOr(studyTags.path("AccessionNumber"), "").contains(noOrder)) {
                studyBase += 250;
            }
            if (!namaPemeriksaan.isEmpty()) {
                if (studyDesc.contains(namaPemeriksaan) || namaPemeriksaan.contains(studyDesc)) {
                    studyBase += 120;
                }
                studyBase += skorToken(namaPemeriksaan, studyDesc) * 15;
            }

            for (JsonNode seriesIdNode : study.path("Series")) {
                String seriesId = seriesIdNode.asText();
                JsonNode series = getOrthancJson("/series/" + seriesId);
                JsonNode seriesTags = series.path("MainDicomTags");
                String seriesUid = seriesTags.path("SeriesInstanceUID").asText();
                String modality = seriesTags.path("Modality").asText();
                String seriesDesc = seriesTags.path("SeriesDescription").asText();
                if (seriesUid.isEmpty() || modality.isEmpty()) {
                    continue;
                }

                String parentStudy = textOr(series.path("ParentStudy"), studyId);
                JsonNode studyDetail = getOrthancJson("/studies/" + parentStudy);
                String studyUid = studyDetail.path("MainDicomTags").path("StudyInstanceUID").asText();
                int instances = series.path("Instances").isArray() ? series.path("Instances").size() : 0;
                int score = studyBase;
                String normalizedSeries = normalize(seriesDesc);
                if (!namaPemeriksaan.isEmpty()) {
                    if (normalizedSeries.contains(namaPemeriksaan) || namaPemeriksaan.contains(normalizedSeries)) {
                        score += 150;
                    }
                    score += skorToken(namaPemeriksaan, normalizedSeries) * 20;
                }
                if (instances > 0) {
                    score += 20;
                }
                if (!studyUid.isEmpty()) {
                    score += 25;
                }

                if (best == null || score > bestScore) {
                    best = new OrthancSeriesInfo(parentStudy, seriesId, studyUid, seriesUid, modality, instances, seriesDesc);
                    bestScore = score;
                }
            }
        }
        return best;
    }

    private JsonNode getOrthancJson(String path) throws Exception {
        if (cacheOrthanc.containsKey(path)) {
            return cacheOrthanc.get(path);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + orthanc.Auth());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + path;
        String response = orthanc.getRest().exchange(url, HttpMethod.GET, entity, String.class).getBody();
        JsonNode root = mapper.readTree(response);
        cacheOrthanc.put(path, root);
        return root;
    }

    private String textOr(JsonNode node, String fallback) {
        String value = node == null ? "" : node.asText();
        return value.isEmpty() ? fallback : value;
    }

    private int skorToken(String left, String right) {
        if (left.isEmpty() || right.isEmpty()) {
            return 0;
        }
        String[] parts = left.split(" ");
        int score = 0;
        for (String part : parts) {
            if (part.length() < 3) {
                continue;
            }
            if (right.contains(part)) {
                score++;
            }
        }
        return score;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private String modalityDisplay(String code) {
        String upper = code == null ? "" : code.trim().toUpperCase(Locale.ENGLISH);
        switch (upper) {
            case "CR":
                return "Computed Radiography";
            case "DX":
                return "Digital Radiography";
            case "CT":
                return "Computed Tomography";
            case "MR":
                return "Magnetic Resonance";
            case "US":
                return "Ultrasound";
            case "MG":
                return "Mammography";
            case "RF":
                return "Radiofluoroscopy";
            case "XA":
                return "X-Ray Angiography";
            case "OT":
                return "Other";
            default:
                return upper;
        }
    }

    public void isCek() {
        boolean aktif = akses.getsatu_sehat_kirim_medicationrequest();
        BtnCariOrthanc.setEnabled(aktif);
        BtnKirim.setEnabled(aktif);
        BtnUpdate.setEnabled(aktif);
    }

    public JTable getTable() {
        return tbData;
    }

    private static final class OrthancSeriesInfo {
        private final String studyId;
        private final String seriesId;
        private final String studyUid;
        private final String seriesUid;
        private final String modality;
        private final int numberOfInstances;
        private final String seriesDescription;

        private OrthancSeriesInfo(String studyId, String seriesId, String studyUid, String seriesUid,
                                  String modality, int numberOfInstances, String seriesDescription) {
            this.studyId = studyId == null ? "" : studyId;
            this.seriesId = seriesId == null ? "" : seriesId;
            this.studyUid = studyUid == null ? "" : studyUid;
            this.seriesUid = seriesUid == null ? "" : seriesUid;
            this.modality = modality == null ? "" : modality;
            this.numberOfInstances = numberOfInstances;
            this.seriesDescription = seriesDescription == null ? "" : seriesDescription;
        }
    }
}
