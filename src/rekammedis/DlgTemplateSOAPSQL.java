package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.koneksiDB;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
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

/** Template SOAP versi server (MySQL), pengganti versi CSV lokal (lihat DlgTemplateSOAPExcel).
 *  Dibuat supaya template SOAP tersimpan terpusat & sama di semua komputer, bukan per-komputer
 *  lewat file CSV lagi. Migrasi dari CSV ke sini lewat tombol "Kirim ke SQL" di DlgTemplateSOAPExcel. */
public class DlgTemplateSOAPSQL extends JDialog {
    private final Connection koneksi = koneksiDB.condb();
    private final JTextField txtCari = new JTextField(30);
    private final JLabel lblCount = new JLabel("0");
    private final DefaultTableModel tabMode;
    private final JTable tbTemplate;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final List<Baris> daftar = new ArrayList<Baris>();
    private DlgTemplateSOAPExcel.SoapTemplateExcel templateTerpilih;

    private static final class Baris {
        int id;
        DlgTemplateSOAPExcel.SoapTemplateExcel data;
    }

    public DlgTemplateSOAPSQL(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Template SOAP (Server) ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        tabMode = new DefaultTableModel(null, new Object[]{
            "Judul", "Subjek", "Objek", "Asesmen", "Plan", "Instruksi", "Evaluasi", "Dikirim Oleh"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        tbTemplate = new JTable(tabMode);
        sorter = new TableRowSorter<DefaultTableModel>(tabMode);

        pastikanTabel(koneksi);
        initComponents();
        loadTemplates();
    }

    /** Buat tabel template_soap kalau belum ada. Dipanggil juga dari DlgTemplateSOAPExcel
     *  sebelum migrasi, jadi method ini statis & menerima koneksi dari luar. */
    static void pastikanTabel(Connection koneksi) {
        try (Statement st = koneksi.createStatement()) {
            st.executeUpdate(
                "create table if not exists template_soap (" +
                "id int not null auto_increment," +
                "judul varchar(200) not null," +
                "subjek text," +
                "objek text," +
                "asesmen text," +
                "plan text," +
                "instruksi text," +
                "evaluasi text," +
                "dibuat_oleh varchar(100) default ''," +
                "created_at datetime default current_timestamp," +
                "primary key (id)" +
                ") engine=InnoDB default charset=utf8"
            );
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
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

        JLabel lblInfo = new JLabel("Template SOAP tersimpan di server (MySQL) -- sama untuk semua komputer.");
        panelAtas.add(lblInfo, BorderLayout.SOUTH);

        add(panelAtas, BorderLayout.NORTH);

        tbTemplate.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbTemplate.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbTemplate.setRowHeight(24);
        tbTemplate.setDefaultRenderer(Object.class, new WarnaTable());
        tbTemplate.setRowSorter(sorter);

        int[] widths = new int[]{160, 230, 230, 160, 160, 160, 160, 120};
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
        daftar.clear();
        tabMode.setRowCount(0);

        try {
            PreparedStatement ps = koneksi.prepareStatement(
                "select id,judul,subjek,objek,asesmen,plan,instruksi,evaluasi,dibuat_oleh from template_soap order by judul");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Baris baris = new Baris();
                baris.id = rs.getInt("id");
                baris.data = new DlgTemplateSOAPExcel.SoapTemplateExcel(
                    nvl(rs.getString("judul")), nvl(rs.getString("subjek")), nvl(rs.getString("objek")),
                    nvl(rs.getString("asesmen")), nvl(rs.getString("plan")), nvl(rs.getString("instruksi")),
                    nvl(rs.getString("evaluasi")), "", nvl(rs.getString("dibuat_oleh"))
                );
                daftar.add(baris);
                tabMode.addRow(new Object[]{
                    baris.data.getTitle(), baris.data.getSubject(), baris.data.getObjectText(),
                    baris.data.getAssessment(), baris.data.getPlan(), baris.data.getImplementation(),
                    baris.data.getEvaluation(), baris.data.getAsalInput()
                });
            }
            rs.close();
            ps.close();
            if (tbTemplate.getRowCount() > 0) {
                tbTemplate.setRowSelectionInterval(0, 0);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Gagal memuat template SOAP dari server.\n" + e.getMessage(),
                    "Template SOAP (Server)",
                    JOptionPane.ERROR_MESSAGE);
        }
        updateCount();
    }

    private String nvl(String s) {
        return s == null ? "" : s;
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
        DlgTemplateSOAPExcel.SoapTemplateExcel baru = inputTemplate(null);
        if (baru == null) {
            return;
        }
        try {
            PreparedStatement ps = koneksi.prepareStatement(
                "insert into template_soap(judul,subjek,objek,asesmen,plan,instruksi,evaluasi,dibuat_oleh,created_at) " +
                "values(?,?,?,?,?,?,?,?,now())");
            ps.setString(1, baru.getTitle());
            ps.setString(2, baru.getSubject());
            ps.setString(3, baru.getObjectText());
            ps.setString(4, baru.getAssessment());
            ps.setString(5, baru.getPlan());
            ps.setString(6, baru.getImplementation());
            ps.setString(7, baru.getEvaluation());
            ps.setString(8, akses.getnamauser());
            ps.executeUpdate();
            ps.close();
            loadTemplates();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Gagal menyimpan template SOAP ke server.\n" + e.getMessage(),
                    "Template SOAP (Server)",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ubahTemplate() {
        int viewRow = tbTemplate.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Silahkan pilih template yang akan diubah terlebih dahulu.",
                    "Template SOAP (Server)",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tbTemplate.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= daftar.size()) {
            return;
        }
        Baris baris = daftar.get(modelRow);
        DlgTemplateSOAPExcel.SoapTemplateExcel baru = inputTemplate(baris.data);
        if (baru == null) {
            return;
        }
        try {
            PreparedStatement ps = koneksi.prepareStatement(
                "update template_soap set judul=?,subjek=?,objek=?,asesmen=?,plan=?,instruksi=?,evaluasi=? where id=?");
            ps.setString(1, baru.getTitle());
            ps.setString(2, baru.getSubject());
            ps.setString(3, baru.getObjectText());
            ps.setString(4, baru.getAssessment());
            ps.setString(5, baru.getPlan());
            ps.setString(6, baru.getImplementation());
            ps.setString(7, baru.getEvaluation());
            ps.setInt(8, baris.id);
            ps.executeUpdate();
            ps.close();
            loadTemplates();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Gagal mengubah template SOAP di server.\n" + e.getMessage(),
                    "Template SOAP (Server)",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapusTemplate() {
        int viewRow = tbTemplate.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Silahkan pilih template yang akan dihapus terlebih dahulu.",
                    "Template SOAP (Server)",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tbTemplate.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= daftar.size()) {
            return;
        }
        Baris baris = daftar.get(modelRow);
        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Hapus template \"" + baris.data.getTitle() + "\" dari server ?",
                "Template SOAP (Server)",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (konfirmasi != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            PreparedStatement ps = koneksi.prepareStatement("delete from template_soap where id=?");
            ps.setInt(1, baris.id);
            ps.executeUpdate();
            ps.close();
            loadTemplates();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Gagal menghapus template SOAP di server.\n" + e.getMessage(),
                    "Template SOAP (Server)",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private DlgTemplateSOAPExcel.SoapTemplateExcel inputTemplate(DlgTemplateSOAPExcel.SoapTemplateExcel existing) {
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

        String judul = existing == null ? "Tambah Template SOAP (Server)" : "Ubah Template SOAP (Server)";
        int hasil = JOptionPane.showConfirmDialog(this, scrollInput, judul,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (hasil != JOptionPane.OK_OPTION) {
            return null;
        }

        String title = fTitle.getText().trim();
        if (title.equals("")) {
            JOptionPane.showMessageDialog(this,
                    "Judul template tidak boleh kosong.",
                    "Template SOAP (Server)",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }

        return new DlgTemplateSOAPExcel.SoapTemplateExcel(
                title,
                fSubject.getText().trim(),
                fObject.getText().trim(),
                fAssessment.getText().trim(),
                fPlan.getText().trim(),
                fImplementation.getText().trim(),
                fEvaluation.getText().trim(),
                "",
                existing != null ? existing.getAsalInput() : akses.getnamauser()
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

    private void pilihTemplate() {
        int viewRow = tbTemplate.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Silahkan pilih template SOAP terlebih dahulu.",
                    "Template SOAP (Server)",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tbTemplate.convertRowIndexToModel(viewRow);
        if (modelRow >= 0 && modelRow < daftar.size()) {
            templateTerpilih = daftar.get(modelRow).data;
            dispose();
        }
    }

    public DlgTemplateSOAPExcel.SoapTemplateExcel getTemplateTerpilih() {
        return templateTerpilih;
    }

    /** Jumlah template yg sudah ada di server -- dipakai DlgRawatInap utk memutuskan
     *  buka dialog SQL (kalau sudah ada isinya) atau dialog CSV lama (kalau belum dimigrasikan).
     *  PENTING: koneksiDB.condb() adalah koneksi tunggal milik seluruh aplikasi (singleton) --
     *  JANGAN dibungkus try-with-resources / ditutup di sini, nanti koneksi aplikasi ikut tertutup. */
    public static int jumlahTemplate() {
        try {
            Connection k = koneksiDB.condb();
            pastikanTabel(k);
            Statement st = k.createStatement();
            ResultSet rs = st.executeQuery("select count(*) from template_soap");
            int jumlah = rs.next() ? rs.getInt(1) : 0;
            rs.close();
            st.close();
            return jumlah;
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
        return 0;
    }
}
