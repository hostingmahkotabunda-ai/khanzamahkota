package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;

abstract class SatuSehatKirimServiceRequestLabBase extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final ApiSatuSehat api = new ApiSatuSehat();
    private final SatuSehatCekNIK cekViaSatuSehat = new SatuSehatCekNIK();
    private final ObjectMapper mapper = new ObjectMapper();
    private final widget.Table tbData = new widget.Table();
    private final widget.ScrollPane scroll = new widget.ScrollPane();
    private final widget.InternalFrame internalFrame = new widget.InternalFrame();
    private final widget.panelisi panelBottom = new widget.panelisi();
    private final widget.panelisi panelTop = new widget.panelisi();
    private final widget.Tanggal DTPCari1 = new widget.Tanggal();
    private final widget.Tanggal DTPCari2 = new widget.Tanggal();
    private final widget.TextBox TCari = new widget.TextBox();
    private final widget.Button BtnCari = new widget.Button();
    private final widget.Button BtnSemua = new widget.Button();
    private final widget.Button BtnPilihSemua = new widget.Button();
    private final widget.Button BtnBersihkan = new widget.Button();
    private final widget.Button BtnKirim = new widget.Button();
    private final widget.Button BtnUpdate = new widget.Button();
    private final widget.Button BtnPrint = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();
    private final widget.Label LCount = new widget.Label();
    private final String judulDialog;
    private final String namaTabelPermintaan;
    private final String namaTabelDetail;
    private final String namaTabelSimpan;
    private final String namaFileHtml;
    private final String link;

    protected SatuSehatKirimServiceRequestLabBase(
            Frame parent,
            boolean modal,
            String judulDialog,
            String namaTabelPermintaan,
            String namaTabelDetail,
            String namaTabelSimpan,
            String namaFileHtml
    ) {
        super(parent, modal);
        this.judulDialog = judulDialog;
        this.namaTabelPermintaan = namaTabelPermintaan;
        this.namaTabelDetail = namaTabelDetail;
        this.namaTabelSimpan = namaTabelSimpan;
        this.namaFileHtml = namaFileHtml;
        this.link = koneksiDB.URLFHIRSATUSEHAT();
        tabMode = new DefaultTableModel(null, new Object[]{
            "P", "No.Rawat", "No.RM", "Nama Pasien", "No.KTP Pasien",
            "Kode Dokter", "Nama Dokter Perujuk", "No.KTP Dokter", "ID Encounter",
            "No.Permintaan", "Tgl & Jam Permintaan", "Diagnosa Klinis",
            "Detail Pemeriksaan", "Lab Code", "Lab System", "Lab Display",
            "ID Service Request", "ID Template", "Kode Pemeriksaan"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex == 0;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
        };
        ensureSupportTables();
        initComponents();
        tampil();
    }

    private void initComponents() {
        setTitle(judulDialog);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(1, 1));
        internalFrame.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)),
                judulDialog,
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Tahoma", 0, 11),
                new java.awt.Color(50, 50, 50)
        ));
        internalFrame.setLayout(new BorderLayout(1, 1));

        TCari.setDocument(new batasInput((byte) 100).getKata(TCari));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setDisplayFormat("dd-MM-yyyy");

        panelTop.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));
        widget.Label lblTanggal = new widget.Label();
        lblTanggal.setText("Tgl.Registrasi :");
        lblTanggal.setPreferredSize(new Dimension(85, 23));
        panelTop.add(lblTanggal);
        DTPCari1.setPreferredSize(new Dimension(95, 23));
        panelTop.add(DTPCari1);
        widget.Label lblSd = new widget.Label();
        lblSd.setText("s.d.");
        lblSd.setPreferredSize(new Dimension(24, 23));
        panelTop.add(lblSd);
        DTPCari2.setPreferredSize(new Dimension(95, 23));
        panelTop.add(DTPCari2);
        widget.Label lblKeyword = new widget.Label();
        lblKeyword.setText("Key Word :");
        lblKeyword.setPreferredSize(new Dimension(70, 23));
        panelTop.add(lblKeyword);
        TCari.setPreferredSize(new Dimension(240, 23));
        panelTop.add(TCari);
        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setPreferredSize(new Dimension(28, 23));
        BtnCari.addActionListener(evt -> tampil());
        panelTop.add(BtnCari);

        tbData.setModel(tabMode);
        tbData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbData.setDefaultRenderer(Object.class, new WarnaTable());
        setColumnWidths();
        scroll.setViewportView(tbData);
        internalFrame.add(scroll, BorderLayout.CENTER);

        panelBottom.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));
        widget.Label lblRecord = new widget.Label();
        lblRecord.setText("Record :");
        lblRecord.setPreferredSize(new Dimension(53, 23));
        panelBottom.add(lblRecord);
        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setPreferredSize(new Dimension(60, 23));
        panelBottom.add(LCount);
        BtnSemua.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnSemua.setText("Semua");
        BtnSemua.setPreferredSize(new Dimension(90, 30));
        BtnSemua.addActionListener(evt -> {
            TCari.setText("");
            tampil();
        });
        panelBottom.add(BtnSemua);
        BtnPilihSemua.setText("Pilih Semua");
        BtnPilihSemua.setPreferredSize(new Dimension(100, 30));
        BtnPilihSemua.addActionListener(evt -> setSemuaPilihan(true));
        panelBottom.add(BtnPilihSemua);
        BtnBersihkan.setText("Bersihkan");
        BtnBersihkan.setPreferredSize(new Dimension(100, 30));
        BtnBersihkan.addActionListener(evt -> setSemuaPilihan(false));
        panelBottom.add(BtnBersihkan);
        BtnKirim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/34.png")));
        BtnKirim.setText("Kirim");
        BtnKirim.setPreferredSize(new Dimension(100, 30));
        BtnKirim.addActionListener(evt -> prosesKirim(false));
        panelBottom.add(BtnKirim);
        BtnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/edit_f2.png")));
        BtnUpdate.setText("Update");
        BtnUpdate.setPreferredSize(new Dimension(100, 30));
        BtnUpdate.addActionListener(evt -> prosesKirim(true));
        panelBottom.add(BtnUpdate);
        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png")));
        BtnPrint.setText("Cetak");
        BtnPrint.setPreferredSize(new Dimension(100, 30));
        BtnPrint.addActionListener(evt -> cetak());
        panelBottom.add(BtnPrint);
        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setText("Keluar");
        BtnKeluar.setPreferredSize(new Dimension(100, 30));
        BtnKeluar.addActionListener(evt -> dispose());
        panelBottom.add(BtnKeluar);

        internalFrame.add(panelTop, BorderLayout.NORTH);
        internalFrame.add(panelBottom, BorderLayout.SOUTH);
        add(internalFrame, BorderLayout.CENTER);

        TCari.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    tampil();
                }
            }
        });

        if (koneksiDB.CARICEPAT().equals("aktif")) {
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { if (TCari.getText().length() > 2) tampil(); }
                @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { if (TCari.getText().length() > 2 || TCari.getText().trim().isEmpty()) tampil(); }
                @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { if (TCari.getText().length() > 2) tampil(); }
            });
        }

        setPreferredSize(new Dimension(1250, 680));
        pack();
    }

    private void setColumnWidths() {
        int[] widths = new int[]{20, 110, 80, 170, 120, 90, 170, 120, 220, 110, 135, 180, 180, 100, 200, 220, 220, 0, 0};
        for (int i = 0; i < widths.length; i++) {
            TableColumn column = tbData.getColumnModel().getColumn(i);
            if (widths[i] == 0) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
                column.setPreferredWidth(0);
            } else {
                column.setPreferredWidth(widths[i]);
            }
        }
    }

    private void ensureSupportTables() {
        PreparedStatement ps = null;
        try {
            ps = koneksi.prepareStatement(
                    "create table if not exists " + namaTabelSimpan + " (" +
                    "noorder varchar(30) not null," +
                    "id_template varchar(20) not null," +
                    "kd_jenis_prw varchar(20) not null," +
                    "id_servicerequest varchar(120) default null," +
                    "primary key (noorder,id_template,kd_jenis_prw)" +
                    ") engine=InnoDB default charset=latin1 collate=latin1_swedish_ci"
            );
            ps.executeUpdate();
            ps.close();
            ps = koneksi.prepareStatement("alter table " + namaTabelSimpan + " modify id_servicerequest varchar(120) null");
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void setSemuaPilihan(boolean selected) {
        for (int i = 0; i < tabMode.getRowCount(); i++) {
            tabMode.setValueAt(selected, i, 0);
        }
    }

    private String selectedDate(widget.Tanggal tanggal) {
        return Valid.SetTgl(String.valueOf(tanggal.getSelectedItem()));
    }

    private void tampil() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Valid.tabelKosong(tabMode);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select reg_periksa.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.no_ktp,")
               .append(namaTabelPermintaan).append(".dokter_perujuk as kd_dokter,pegawai.nama,pegawai.no_ktp as ktpdokter,")
               .append("satu_sehat_encounter.id_encounter,")
               .append(namaTabelPermintaan).append(".noorder,")
               .append(namaTabelPermintaan).append(".tgl_permintaan,")
               .append(namaTabelPermintaan).append(".jam_permintaan,")
               .append(namaTabelPermintaan).append(".diagnosa_klinis,")
               .append("template_laboratorium.Pemeriksaan,satu_sehat_mapping_lab.code,satu_sehat_mapping_lab.system,")
               .append("satu_sehat_mapping_lab.display,ifnull(").append(namaTabelSimpan).append(".id_servicerequest,'') as id_servicerequest,")
               .append(namaTabelDetail).append(".id_template,").append(namaTabelDetail).append(".kd_jenis_prw ")
               .append("from reg_periksa ")
               .append("inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis ")
               .append("inner join ").append(namaTabelPermintaan).append(" on ").append(namaTabelPermintaan).append(".no_rawat=reg_periksa.no_rawat ")
               .append("left join pegawai on pegawai.nik=").append(namaTabelPermintaan).append(".dokter_perujuk ")
               .append("inner join satu_sehat_encounter on satu_sehat_encounter.no_rawat=reg_periksa.no_rawat ")
               .append("inner join ").append(namaTabelDetail).append(" on ").append(namaTabelDetail).append(".noorder=").append(namaTabelPermintaan).append(".noorder ")
               .append("inner join template_laboratorium on template_laboratorium.id_template=").append(namaTabelDetail).append(".id_template ")
               .append("inner join satu_sehat_mapping_lab on satu_sehat_mapping_lab.id_template=template_laboratorium.id_template ")
               .append("left join ").append(namaTabelSimpan).append(" on ").append(namaTabelSimpan).append(".noorder=").append(namaTabelDetail).append(".noorder ")
               .append("and ").append(namaTabelSimpan).append(".id_template=").append(namaTabelDetail).append(".id_template ")
               .append("and ").append(namaTabelSimpan).append(".kd_jenis_prw=").append(namaTabelDetail).append(".kd_jenis_prw ")
               .append("where reg_periksa.tgl_registrasi between ? and ? ");
            if (!TCari.getText().trim().isEmpty()) {
                sql.append("and (reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or pasien.nm_pasien like ? or ")
                   .append("pasien.no_ktp like ? or pegawai.nama like ? or template_laboratorium.Pemeriksaan like ? or ")
                   .append("satu_sehat_mapping_lab.code like ? or ").append(namaTabelPermintaan).append(".noorder like ?) ");
            }
            sql.append("order by ").append(namaTabelPermintaan).append(".tgl_permintaan desc,")
               .append(namaTabelPermintaan).append(".jam_permintaan desc,")
               .append(namaTabelPermintaan).append(".noorder,")
               .append(namaTabelDetail).append(".kd_jenis_prw,")
               .append(namaTabelDetail).append(".id_template");

            ps = koneksi.prepareStatement(sql.toString());
            ps.setString(1, selectedDate(DTPCari1));
            ps.setString(2, selectedDate(DTPCari2));
            if (!TCari.getText().trim().isEmpty()) {
                String keyword = "%" + TCari.getText().trim() + "%";
                for (int i = 3; i <= 10; i++) {
                    ps.setString(i, keyword);
                }
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                tabMode.addRow(new Object[]{
                    false, rs.getString("no_rawat"), rs.getString("no_rkm_medis"), rs.getString("nm_pasien"), rs.getString("no_ktp"),
                    rs.getString("kd_dokter"), rs.getString("nama"), rs.getString("ktpdokter"), rs.getString("id_encounter"),
                    rs.getString("noorder"), rs.getString("tgl_permintaan") + " " + rs.getString("jam_permintaan"),
                    rs.getString("diagnosa_klinis"), rs.getString("Pemeriksaan"), rs.getString("code"), rs.getString("system"),
                    rs.getString("display"), rs.getString("id_servicerequest"), rs.getString("id_template"), rs.getString("kd_jenis_prw")
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
            } catch (Exception ignored) {
            }
            LCount.setText(String.valueOf(tabMode.getRowCount()));
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void prosesKirim(boolean update) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            for (int row = 0; row < tabMode.getRowCount(); row++) {
                if (Boolean.TRUE.equals(tabMode.getValueAt(row, 0))) {
                    prosesBaris(row, update);
                }
            }
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void prosesBaris(int row, boolean update) {
        String noKtpPasien = valueAt(row, 4);
        String noKtpDokter = valueAt(row, 7);
        String idEncounter = valueAt(row, 8);
        String noOrder = valueAt(row, 9);
        String code = valueAt(row, 13);
        String system = valueAt(row, 14);
        String display = valueAt(row, 15);
        String existingId = valueAt(row, 16);
        String idTemplate = valueAt(row, 17);
        String kdJenisPrw = valueAt(row, 18);
        String identifierValue = noOrder + "." + kdJenisPrw;

        if (noKtpPasien.trim().isEmpty() || noKtpDokter.trim().isEmpty() || idEncounter.trim().isEmpty()) {
            logSkip("No.KTP pasien/dokter atau ID Encounter kosong", row);
            return;
        }
        if (code.trim().isEmpty() || system.trim().isEmpty() || display.trim().isEmpty()) {
            logSkip("Mapping pemeriksaan lab belum lengkap", row);
            return;
        }

        String idPasien = cekViaSatuSehat.tampilIDPasien(noKtpPasien);
        String idDokter = cekViaSatuSehat.tampilIDParktisi(noKtpDokter);
        if (idPasien.trim().isEmpty() || idDokter.trim().isEmpty()) {
            logSkip("IHS pasien atau praktisi tidak ditemukan", row);
            return;
        }

        if (update && !isLikelyFhirId(existingId)) {
            String syncedId = cariServiceRequestSudahAda(identifierValue);
            if (!syncedId.isEmpty()) {
                upsertIdServiceRequest(noOrder, idTemplate, kdJenisPrw, syncedId);
                tabMode.setValueAt(syncedId, row, 16);
                existingId = syncedId;
            }
        }
        if (update && existingId.trim().isEmpty()) {
            logSkip("ID ServiceRequest lokal kosong, gunakan Kirim dulu", row);
            return;
        }

        try {
            String payload = buildPayload(row, idPasien, idDokter, update, existingId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + api.TokenSatuSehat());
            HttpEntity<String> requestEntity = new HttpEntity<>(payload, headers);
            String url = update ? link + "/ServiceRequest/" + existingId : link + "/ServiceRequest";
            System.out.println("URL : " + url);
            System.out.println("Request JSON : " + payload);
            String result = api.getRest().exchange(url, update ? HttpMethod.PUT : HttpMethod.POST, requestEntity, String.class).getBody();
            System.out.println("Result JSON : " + result);
            JsonNode root = mapper.readTree(result);
            String resultId = root.path("id").asText();
            if (resultId.trim().isEmpty()) {
                resultId = existingId;
            }
            if (!resultId.trim().isEmpty()) {
                upsertIdServiceRequest(noOrder, idTemplate, kdJenisPrw, resultId);
                tabMode.setValueAt(resultId, row, 16);
                tabMode.setValueAt(false, row, 0);
            }
        } catch (HttpStatusCodeException e) {
            tanganiHttpError(e, row, update, identifierValue, noOrder, idTemplate, kdJenisPrw);
        } catch (Exception e) {
            System.out.println("Notifikasi Bridging (" + (update ? "update" : "create") + " " + getModeLabel() + ") : " + e);
            logBaris(row);
        }
    }

    private void tanganiHttpError(
            HttpStatusCodeException e,
            int row,
            boolean update,
            String identifierValue,
            String noOrder,
            String idTemplate,
            String kdJenisPrw
    ) {
        String body = e.getResponseBodyAsString();
        System.out.println("Notifikasi Bridging (" + (update ? "update" : "create") + " " + getModeLabel() + ") : " + e);
        logBaris(row);
        System.out.println("HTTP Status : " + e.getStatusCode().value());
        System.out.println("Response Body : " + body);
        try {
            JsonNode outcome = mapper.readTree(body);
            JsonNode issue = outcome.path("issue").isArray() && outcome.path("issue").size() > 0 ? outcome.path("issue").get(0) : null;
            if (issue != null) {
                String details = issue.path("details").path("text").asText();
                System.out.println("OperationOutcome : severity=" + issue.path("severity").asText()
                        + ", code=" + issue.path("code").asText()
                        + ", details=" + details
                        + ", diagnostics=" + issue.path("diagnostics").asText()
                        + ", expression=" + issue.path("expression").asText());
                if (!update && details.toLowerCase().contains("duplicate")) {
                    String foundId = cariServiceRequestSudahAda(identifierValue);
                    if (!foundId.isEmpty()) {
                        System.out.println("Duplicate ditemukan di SATUSEHAT : " + foundId);
                        upsertIdServiceRequest(noOrder, idTemplate, kdJenisPrw, foundId);
                        tabMode.setValueAt(foundId, row, 16);
                        tabMode.setValueAt(false, row, 0);
                    }
                } else if (update) {
                    String foundId = cariServiceRequestSudahAda(identifierValue);
                    if (!foundId.isEmpty()) {
                        System.out.println("ID ServiceRequest lokal diperbarui dari duplicate check : " + foundId);
                        upsertIdServiceRequest(noOrder, idTemplate, kdJenisPrw, foundId);
                        tabMode.setValueAt(foundId, row, 16);
                    }
                }
            }
        } catch (Exception parseEx) {
            System.out.println("Notifikasi parsing error body : " + parseEx);
        }
    }

    private void logSkip(String alasan, int row) {
        System.out.println("Lewat baris " + getModeLabel() + " : " + alasan);
        logBaris(row);
    }

    private void logBaris(int row) {
        System.out.println("Baris gagal : no_rawat=" + valueAt(row, 1)
                + ", no_rm=" + valueAt(row, 2)
                + ", no_order=" + valueAt(row, 9)
                + ", id_template=" + valueAt(row, 17)
                + ", kd_jenis_prw=" + valueAt(row, 18)
                + ", lab_code=" + valueAt(row, 13)
                + ", authoredOn=" + toFhirDateTime(valueAt(row, 10)));
    }

    private String buildPayload(int row, String idPasien, String idDokter, boolean update, String existingId) {
        String identifierValue = valueAt(row, 9) + "." + valueAt(row, 18);
        StringBuilder json = new StringBuilder();
        json.append("{")
            .append("\"resourceType\":\"ServiceRequest\",");
        if (update) {
            json.append("\"id\":\"").append(escapeJson(existingId)).append("\",");
        }
        json.append("\"identifier\":[{")
            .append("\"system\":\"http://sys-ids.kemkes.go.id/servicerequest/").append(escapeJson(koneksiDB.IDSATUSEHAT())).append("\",")
            .append("\"value\":\"").append(escapeJson(identifierValue)).append("\"")
            .append("}],")
            .append("\"status\":\"active\",")
            .append("\"intent\":\"order\",")
            .append("\"category\":[{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"108252007\",\"display\":\"Laboratory procedure\"}]}],")
            .append("\"code\":{\"coding\":[{")
            .append("\"system\":\"").append(escapeJson(valueAt(row, 14))).append("\",")
            .append("\"code\":\"").append(escapeJson(valueAt(row, 13))).append("\",")
            .append("\"display\":\"").append(escapeJson(valueAt(row, 15))).append("\"")
            .append("}],\"text\":\"").append(escapeJson(valueAt(row, 12))).append("\"},")
            .append("\"subject\":{\"reference\":\"Patient/").append(escapeJson(idPasien)).append("\"},")
            .append("\"encounter\":{\"reference\":\"Encounter/").append(escapeJson(valueAt(row, 8))).append("\"},")
            .append("\"authoredOn\":\"").append(escapeJson(toFhirDateTime(valueAt(row, 10)))).append("\",")
            .append("\"requester\":{\"reference\":\"Practitioner/").append(escapeJson(idDokter)).append("\",\"display\":\"").append(escapeJson(valueAt(row, 6))).append("\"},")
            .append("\"performer\":[{\"reference\":\"Organization/").append(escapeJson(koneksiDB.IDSATUSEHAT())).append("\",\"display\":\"Ruang Laborat/Petugas Laborat\"}],")
            .append("\"reasonCode\":[{\"text\":\"").append(escapeJson(valueAt(row, 11))).append("\"}]")
            .append("}");
        return json.toString();
    }

    private String valueAt(int row, int col) {
        Object value = tabMode.getValueAt(row, col);
        return value == null ? "" : value.toString();
    }

    private String toFhirDateTime(String tanggalJam) {
        String raw = tanggalJam == null ? "" : tanggalJam.trim();
        if (raw.isEmpty()) {
            return "";
        }
        String[] parts = raw.split("\\s+");
        String tanggal = parts.length > 0 ? parts[0] : "";
        String jam = parts.length > 1 ? parts[1] : "00:00:00";
        if (jam.trim().isEmpty()) {
            jam = "00:00:00";
        }
        if (jam.length() == 5) {
            jam = jam + ":00";
        }
        return tanggal + "T" + jam + "+07:00";
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
    }

    private void upsertIdServiceRequest(String noOrder, String idTemplate, String kdJenisPrw, String idServiceRequest) {
        PreparedStatement ps = null;
        try {
            ps = koneksi.prepareStatement(
                    "insert into " + namaTabelSimpan + " (noorder,id_template,kd_jenis_prw,id_servicerequest) values(?,?,?,?) "
                    + "on duplicate key update id_servicerequest=values(id_servicerequest)"
            );
            ps.setString(1, noOrder);
            ps.setString(2, idTemplate);
            ps.setString(3, kdJenisPrw);
            ps.setString(4, idServiceRequest);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private String cariServiceRequestSudahAda(String identifierValue) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + api.TokenSatuSehat());
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            String system = "http://sys-ids.kemkes.go.id/servicerequest/" + koneksiDB.IDSATUSEHAT();
            String encoded = URLEncoder.encode(system, StandardCharsets.UTF_8.name()).replace("+", "%20")
                    + "%7C"
                    + URLEncoder.encode(identifierValue, StandardCharsets.UTF_8.name()).replace("+", "%20");
            String url = link + "/ServiceRequest?identifier=" + encoded;
            System.out.println("URL Duplicate Check : " + url);
            String json = api.getRest().exchange(url, HttpMethod.GET, requestEntity, String.class).getBody();
            System.out.println("JSON Duplicate Check : " + json);
            JsonNode root = mapper.readTree(json);
            for (JsonNode entry : root.path("entry")) {
                JsonNode resource = entry.path("resource");
                for (JsonNode identifier : resource.path("identifier")) {
                    if (system.equals(identifier.path("system").asText()) && identifierValue.equals(identifier.path("value").asText())) {
                        return resource.path("id").asText();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi duplicate check : " + e);
        }
        return "";
    }

    private boolean isLikelyFhirId(String id) {
        if (id == null) {
            return false;
        }
        String trimmed = id.trim();
        return !trimmed.isEmpty() && trimmed.length() >= 8 && !trimmed.contains(" ");
    }

    private void cetak() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            File file = new File(namaFileHtml);
            StringBuilder html = new StringBuilder();
            html.append("<html><head><title>").append(escapeHtml(judulDialog)).append("</title>")
                .append("<style>table{border-collapse:collapse;font-family:tahoma;font-size:11px;width:100%;}th,td{border:1px solid #d6d6d6;padding:4px;vertical-align:top;}th{background:#f5f5f5;}</style>")
                .append("</head><body><h3>").append(escapeHtml(judulDialog)).append("</h3><table><tr>")
                .append("<th>No.Rawat</th><th>No.RM</th><th>Nama Pasien</th><th>Dokter</th><th>No.Permintaan</th><th>Tgl & Jam Permintaan</th>")
                .append("<th>Diagnosa Klinis</th><th>Detail Pemeriksaan</th><th>Lab Code</th><th>Lab System</th><th>Lab Display</th><th>ID Service Request</th>")
                .append("</tr>");
            for (int i = 0; i < tabMode.getRowCount(); i++) {
                html.append("<tr>")
                    .append("<td>").append(escapeHtml(valueAt(i, 1))).append("</td>")
                    .append("<td>").append(escapeHtml(valueAt(i, 2))).append("</td>")
                    .append("<td>").append(escapeHtml(valueAt(i, 3))).append("</td>")
                    .append("<td>").append(escapeHtml(valueAt(i, 6))).append("</td>")
                    .append("<td>").append(escapeHtml(valueAt(i, 9))).append("</td>")
                    .append("<td>").append(escapeHtml(valueAt(i, 10))).append("</td>")
                    .append("<td>").append(escapeHtml(valueAt(i, 11))).append("</td>")
                    .append("<td>").append(escapeHtml(valueAt(i, 12))).append("</td>")
                    .append("<td>").append(escapeHtml(valueAt(i, 13))).append("</td>")
                    .append("<td>").append(escapeHtml(valueAt(i, 14))).append("</td>")
                    .append("<td>").append(escapeHtml(valueAt(i, 15))).append("</td>")
                    .append("<td>").append(escapeHtml(valueAt(i, 16))).append("</td>")
                    .append("</tr>");
            }
            html.append("</table></body></html>");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(html.toString());
            writer.close();
            Desktop.getDesktop().browse(file.toURI());
        } catch (Exception e) {
            System.out.println("Notifikasi Cetak : " + e);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    protected String getModeLabel() {
        return namaTabelPermintaan.equals("permintaan_lab") ? "lab pk" : "lab mb";
    }

    public void isCek() {
        boolean aktif = akses.getsatu_sehat_kirim_medicationrequest();
        BtnKirim.setEnabled(aktif);
        BtnUpdate.setEnabled(aktif);
        BtnPrint.setEnabled(true);
    }

    public JTable getTable() {
        return tbData;
    }
}
