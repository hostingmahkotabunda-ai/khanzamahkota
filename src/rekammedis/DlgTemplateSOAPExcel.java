package rekammedis;

import fungsi.WarnaTable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

public class DlgTemplateSOAPExcel extends JDialog {
    private final String csvPath;
    private final JTextField txtCari = new JTextField(30);
    private final JLabel lblCount = new JLabel("0");
    private final JLabel lblPath = new JLabel();
    private final DefaultTableModel tabMode;
    private final JTable tbTemplate;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final List<SoapTemplateExcel> templates = new ArrayList<SoapTemplateExcel>();
    private SoapTemplateExcel templateTerpilih;

    public DlgTemplateSOAPExcel(Frame parent, boolean modal, String csvPath) {
        super(parent, modal);
        this.csvPath = csvPath;
        setTitle("::[ Template SOAP CSV ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        tabMode = new DefaultTableModel(null, new Object[]{
            "Judul", "Subjek", "Objek", "Asesmen", "Plan", "Instruksi", "Evaluasi"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        tbTemplate = new JTable(tabMode);
        sorter = new TableRowSorter<DefaultTableModel>(tabMode);

        initComponents();
        loadTemplates();
    }

    private void initComponents() {
        JPanel panelAtas = new JPanel(new BorderLayout(8, 8));
        panelAtas.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

        JPanel panelCari = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelCari.add(new JLabel("Cari Template :"));
        panelCari.add(txtCari);

        JButton btnMuatUlang = new JButton("Muat Ulang");
        btnMuatUlang.addActionListener(evt -> loadTemplates());
        panelCari.add(btnMuatUlang);

        JButton btnTambah = new JButton("Tambah");
        JButton btnUbah = new JButton("Ubah");
        JButton btnHapus = new JButton("Hapus");
        btnTambah.addActionListener(evt -> tambahTemplate());
        btnUbah.addActionListener(evt -> ubahTemplate());
        btnHapus.addActionListener(evt -> hapusTemplate());
        panelCari.add(btnTambah);
        panelCari.add(btnUbah);
        panelCari.add(btnHapus);

        panelAtas.add(panelCari, BorderLayout.NORTH);

        lblPath.setText(csvPath);
        panelAtas.add(lblPath, BorderLayout.SOUTH);

        add(panelAtas, BorderLayout.NORTH);

        tbTemplate.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbTemplate.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbTemplate.setRowHeight(24);
        tbTemplate.setDefaultRenderer(Object.class, new WarnaTable());
        tbTemplate.setRowSorter(sorter);

        int[] widths = new int[]{180, 250, 250, 180, 180, 180, 180};
        for (int i = 0; i < widths.length; i++) {
            TableColumn column = tbTemplate.getColumnModel().getColumn(i);
            column.setPreferredWidth(widths[i]);
        }

        tbTemplate.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    pilihTemplate();
                }
            }
        });
        tbTemplate.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    evt.consume();
                    pilihTemplate();
                }
            }
        });

        txtCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTemplates();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTemplates();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTemplates();
            }
        });

        add(new JScrollPane(tbTemplate), BorderLayout.CENTER);

        JPanel panelBawah = new JPanel(new BorderLayout());
        panelBawah.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelInfo.add(new JLabel("Jumlah :"));
        panelInfo.add(lblCount);
        panelBawah.add(panelInfo, BorderLayout.WEST);

        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton btnPilih = new JButton("Pilih");
        JButton btnBatal = new JButton("Batal");
        btnPilih.addActionListener(evt -> pilihTemplate());
        btnBatal.addActionListener(evt -> dispose());
        panelTombol.add(btnPilih);
        panelTombol.add(btnBatal);
        panelBawah.add(panelTombol, BorderLayout.EAST);

        add(panelBawah, BorderLayout.SOUTH);

        setSize(new Dimension(1180, 650));
        setLocationRelativeTo(getParent());
    }

    private void loadTemplates() {
        templates.clear();
        tabMode.setRowCount(0);
        lblCount.setText("0");

        File file = new File(csvPath);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this,
                    "File template SOAP CSV tidak ditemukan :\n" + csvPath,
                    "Template SOAP CSV",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<List<String>> records = parseCsv(file);
            if (records.isEmpty()) {
                return;
            }

            List<String> headerRow = records.get(0);
            if (headerRow.isEmpty()) {
                return;
            }

            Map<String, Integer> headers = readHeaders(headerRow);
            for (int rowIndex = 1; rowIndex < records.size(); rowIndex++) {
                List<String> row = records.get(rowIndex);
                if (row == null || row.isEmpty()) {
                    continue;
                }

                SoapTemplateExcel template = new SoapTemplateExcel(
                        value(row, headers, "soaptemplate_title"),
                        value(row, headers, "soaptemplate_subject"),
                        value(row, headers, "soaptemplate_object"),
                        value(row, headers, "soaptemplate_asessment", "soaptemplate_assessment"),
                        value(row, headers, "soaptemplate_plan"),
                        value(row, headers, "soaptemplate_implementation"),
                        value(row, headers, "soaptemplate_evaluation"),
                        value(row, headers, "soaptemplate_revision"),
                        value(row, headers, "soaptemplate_asal_input")
                );

                if (template.isEmpty()) {
                    continue;
                }

                templates.add(template);
                tabMode.addRow(new Object[]{
                    template.getTitle(),
                    template.getSubject(),
                    template.getObjectText(),
                    template.getAssessment(),
                    template.getPlan(),
                    template.getImplementation(),
                    template.getEvaluation()
                });
            }

            if (tbTemplate.getRowCount() > 0) {
                tbTemplate.setRowSelectionInterval(0, 0);
            }
            updateCount();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Gagal membaca file template SOAP CSV.\n" + e.getMessage(),
                    "Template SOAP CSV",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Map<String, Integer> readHeaders(List<String> headerRow) {
        Map<String, Integer> headers = new HashMap<String, Integer>();
        for (int col = 0; col < headerRow.size(); col++) {
            String header = normalizeHeader(headerRow.get(col));
            if (!header.equals("")) {
                headers.put(header, Integer.valueOf(col));
            }
        }
        return headers;
    }

    private String value(List<String> row, Map<String, Integer> headers, String... keys) {
        for (String key : keys) {
            Integer columnIndex = headers.get(key.toLowerCase());
            if (columnIndex != null && columnIndex.intValue() < row.size()) {
                return row.get(columnIndex.intValue()).trim();
            }
        }
        return "";
    }

    private String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\uFEFF", "").trim().toLowerCase();
    }

    private List<List<String>> parseCsv(File file) throws Exception {
        List<List<String>> records = new ArrayList<List<String>>();
        try (Reader input = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            List<String> row = new ArrayList<String>();
            StringBuilder field = new StringBuilder();
            boolean inQuotes = false;
            int data;

            while ((data = input.read()) != -1) {
                char current = (char) data;

                if (inQuotes) {
                    if (current == '"') {
                        input.mark(1);
                        int next = input.read();
                        if (next == '"') {
                            field.append('"');
                        } else {
                            inQuotes = false;
                            if (next != -1) {
                                input.reset();
                            }
                        }
                    } else {
                        field.append(current);
                    }
                } else {
                    if (current == '"') {
                        inQuotes = true;
                    } else if (current == ',') {
                        row.add(field.toString());
                        field.setLength(0);
                    } else if (current == '\r') {
                        row.add(field.toString());
                        field.setLength(0);
                        records.add(row);
                        row = new ArrayList<String>();
                        input.mark(1);
                        int next = input.read();
                        if (next != '\n' && next != -1) {
                            input.reset();
                        }
                    } else if (current == '\n') {
                        row.add(field.toString());
                        field.setLength(0);
                        records.add(row);
                        row = new ArrayList<String>();
                    } else {
                        field.append(current);
                    }
                }
            }

            if (field.length() > 0 || !row.isEmpty()) {
                row.add(field.toString());
                records.add(row);
            }
        }
        return records;
    }

    private void filterTemplates() {
        String keyword = txtCari.getText().trim();
        if (keyword.equals("")) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(keyword)));
        }
        updateCount();
    }

    private void updateCount() {
        lblCount.setText(String.valueOf(tbTemplate.getRowCount()));
    }

    private void tambahTemplate() {
        SoapTemplateExcel baru = inputTemplate(null);
        if (baru == null) {
            return;
        }
        templates.add(baru);
        simpanDanMuatUlang();
    }

    private void ubahTemplate() {
        int viewRow = tbTemplate.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Silahkan pilih template yang akan diubah terlebih dahulu.",
                    "Template SOAP CSV",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tbTemplate.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= templates.size()) {
            return;
        }
        SoapTemplateExcel baru = inputTemplate(templates.get(modelRow));
        if (baru == null) {
            return;
        }
        templates.set(modelRow, baru);
        simpanDanMuatUlang();
    }

    private void hapusTemplate() {
        int viewRow = tbTemplate.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Silahkan pilih template yang akan dihapus terlebih dahulu.",
                    "Template SOAP CSV",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tbTemplate.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= templates.size()) {
            return;
        }
        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Hapus template \"" + templates.get(modelRow).getTitle() + "\" ?",
                "Template SOAP CSV",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (konfirmasi != JOptionPane.YES_OPTION) {
            return;
        }
        templates.remove(modelRow);
        simpanDanMuatUlang();
    }

    private void simpanDanMuatUlang() {
        try {
            writeCsv(new File(csvPath), templates);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Gagal menyimpan file template SOAP CSV.\n" + e.getMessage(),
                    "Template SOAP CSV",
                    JOptionPane.ERROR_MESSAGE);
        }
        // Selalu muat ulang dari file agar tabel mencerminkan isi file sebenarnya
        // (sekaligus rollback tampilan bila penyimpanan gagal).
        loadTemplates();
    }

    private SoapTemplateExcel inputTemplate(SoapTemplateExcel existing) {
        JTextField fTitle = new JTextField(existing != null ? existing.getTitle() : "", 40);
        JTextArea fSubject = new JTextArea(existing != null ? existing.getSubject() : "", 3, 40);
        JTextArea fObject = new JTextArea(existing != null ? existing.getObjectText() : "", 3, 40);
        JTextArea fAssessment = new JTextArea(existing != null ? existing.getAssessment() : "", 3, 40);
        JTextArea fPlan = new JTextArea(existing != null ? existing.getPlan() : "", 3, 40);
        JTextArea fImplementation = new JTextArea(existing != null ? existing.getImplementation() : "", 3, 40);
        JTextArea fEvaluation = new JTextArea(existing != null ? existing.getEvaluation() : "", 3, 40);

        JPanel panel = new JPanel(new GridBagLayout());
        addBaris(panel, 0, "Judul :", fTitle);
        addBaris(panel, 1, "Subjek :", new JScrollPane(fSubject));
        addBaris(panel, 2, "Objek :", new JScrollPane(fObject));
        addBaris(panel, 3, "Asesmen :", new JScrollPane(fAssessment));
        addBaris(panel, 4, "Plan :", new JScrollPane(fPlan));
        addBaris(panel, 5, "Instruksi :", new JScrollPane(fImplementation));
        addBaris(panel, 6, "Evaluasi :", new JScrollPane(fEvaluation));

        JScrollPane scrollInput = new JScrollPane(panel);
        scrollInput.setPreferredSize(new Dimension(720, 520));
        scrollInput.getVerticalScrollBar().setUnitIncrement(16);

        String judul = existing == null ? "Tambah Template SOAP" : "Ubah Template SOAP";
        int hasil = JOptionPane.showConfirmDialog(this, scrollInput, judul,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (hasil != JOptionPane.OK_OPTION) {
            return null;
        }

        String title = fTitle.getText().trim();
        if (title.equals("")) {
            JOptionPane.showMessageDialog(this,
                    "Judul template tidak boleh kosong.",
                    "Template SOAP CSV",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }

        // Kolom Revisi & Asal sengaja tidak ditampilkan, selalu disimpan kosong.
        return new SoapTemplateExcel(
                title,
                fSubject.getText().trim(),
                fObject.getText().trim(),
                fAssessment.getText().trim(),
                fPlan.getText().trim(),
                fImplementation.getText().trim(),
                fEvaluation.getText().trim(),
                "",
                ""
        );
    }

    private void addBaris(JPanel panel, int baris, String label, java.awt.Component field) {
        GridBagConstraints labelC = new GridBagConstraints();
        labelC.gridx = 0;
        labelC.gridy = baris;
        labelC.anchor = GridBagConstraints.NORTHWEST;
        labelC.insets = new Insets(4, 4, 4, 8);
        panel.add(new JLabel(label), labelC);

        GridBagConstraints fieldC = new GridBagConstraints();
        fieldC.gridx = 1;
        fieldC.gridy = baris;
        fieldC.fill = GridBagConstraints.HORIZONTAL;
        fieldC.weightx = 1.0;
        fieldC.insets = new Insets(4, 0, 4, 4);
        panel.add(field, fieldC);
    }

    private void writeCsv(File file, List<SoapTemplateExcel> data) throws Exception {
        String[] header = new String[]{
            "soaptemplate_title", "soaptemplate_subject", "soaptemplate_object",
            "soaptemplate_asessment", "soaptemplate_plan", "soaptemplate_implementation",
            "soaptemplate_evaluation", "soaptemplate_revision", "soaptemplate_asal_input"
        };
        try (Writer output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            output.write(barisCsv(header));
            for (SoapTemplateExcel t : data) {
                output.write(barisCsv(new String[]{
                    t.getTitle(), t.getSubject(), t.getObjectText(), t.getAssessment(),
                    t.getPlan(), t.getImplementation(), t.getEvaluation(),
                    t.getRevision(), t.getAsalInput()
                }));
            }
        }
    }

    private String barisCsv(String[] fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escapeCsv(fields[i]));
        }
        sb.append("\r\n");
        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            value = "";
        }
        boolean perluKutip = value.indexOf(',') >= 0
                || value.indexOf('"') >= 0
                || value.indexOf('\r') >= 0
                || value.indexOf('\n') >= 0;
        if (perluKutip) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void pilihTemplate() {
        int viewRow = tbTemplate.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Silahkan pilih template SOAP terlebih dahulu.",
                    "Template SOAP CSV",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tbTemplate.convertRowIndexToModel(viewRow);
        if (modelRow >= 0 && modelRow < templates.size()) {
            templateTerpilih = templates.get(modelRow);
            dispose();
        }
    }

    public SoapTemplateExcel getTemplateTerpilih() {
        return templateTerpilih;
    }

    public static class SoapTemplateExcel {
        private final String title;
        private final String subject;
        private final String objectText;
        private final String assessment;
        private final String plan;
        private final String implementation;
        private final String evaluation;
        private final String revision;
        private final String asalInput;

        public SoapTemplateExcel(String title, String subject, String objectText, String assessment,
                String plan, String implementation, String evaluation, String revision, String asalInput) {
            this.title = title;
            this.subject = subject;
            this.objectText = objectText;
            this.assessment = assessment;
            this.plan = plan;
            this.implementation = implementation;
            this.evaluation = evaluation;
            this.revision = revision;
            this.asalInput = asalInput;
        }

        public boolean isEmpty() {
            return title.equals("")
                    && subject.equals("")
                    && objectText.equals("")
                    && assessment.equals("")
                    && plan.equals("")
                    && implementation.equals("")
                    && evaluation.equals("")
                    && revision.equals("");
        }

        public String getTitle() {
            return title;
        }

        public String getSubject() {
            return subject;
        }

        public String getObjectText() {
            return objectText;
        }

        public String getAssessment() {
            return assessment;
        }

        public String getPlan() {
            return plan;
        }

        public String getImplementation() {
            return implementation;
        }

        public String getEvaluation() {
            return evaluation;
        }

        public String getRevision() {
            return revision;
        }

        public String getAsalInput() {
            return asalInput;
        }

        public String getAppliedEvaluation() {
            return !evaluation.equals("") ? evaluation : revision;
        }
    }
}
