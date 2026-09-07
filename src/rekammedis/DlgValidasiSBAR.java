package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;

/**
 * Dialog validasi SBAR. Diparameter status ("ralan"/"ranap") sehingga dibuka
 * sebagai "Validasi SBAR Rawat Jalan" atau "Validasi SBAR Rawat Inap".
 * Setiap dokter boleh memvalidasi (sesuai keputusan).
 */
public final class DlgValidasiSBAR extends JDialog {
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final String status;

    private final widget.TextBox TNoRw = new widget.TextBox();
    private final widget.TextBox TNoRM = new widget.TextBox();
    private final widget.TextBox TPasien = new widget.TextBox();
    private final widget.TextBox KdValidator = new widget.TextBox();
    private final widget.TextBox NmValidator = new widget.TextBox();
    private final widget.TextBox NIP = new widget.TextBox();
    private final widget.TextBox NmPemeriksa = new widget.TextBox();
    private final widget.TextBox TglSBAR = new widget.TextBox();
    private final widget.TextBox JamSBAR = new widget.TextBox();
    private final widget.TextBox JamValidasi = new widget.TextBox();
    private final JLabel LStatus = new JLabel("-");

    private final JTextArea Situation = new JTextArea();
    private final JTextArea Background = new JTextArea();
    private final JTextArea Assesmen = new JTextArea();
    private final JTextArea Recommendation = new JTextArea();
    private final widget.ComboBox CmbBaca = new widget.ComboBox();
    private final widget.ComboBox CmbKonfirmasi = new widget.ComboBox();

    private final widget.Button BtnCariValidator = new widget.Button();
    private final widget.Button BtnValidasi = new widget.Button();
    private final widget.Button BtnBaru = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();
    private final widget.Button BtnCari = new widget.Button();
    private final widget.TextBox TCari = new widget.TextBox();
    private final widget.Tanggal Tgl1 = new widget.Tanggal();
    private final widget.Tanggal Tgl2 = new widget.Tanggal();
    private final JLabel LCount = new JLabel("0");

    private final widget.Table tbSBAR = new widget.Table();
    private final DefaultTableModel tabMode;
    private final DlgCariDokter dokter = new DlgCariDokter(null, true);

    private String tglKey = "";
    private String jamKey = "";
    private String noRawatFilter = "";
    private String noRkmMedis = "";
    private String namaPasien = "";

    public DlgValidasiSBAR(Frame parent, boolean modal, String status) {
        super(parent, modal);
        this.status = (status == null || status.trim().equals("")) ? "ralan" : status.trim();
        setTitle(this.status.equals("ranap") ? "::[ Validasi SBAR Rawat Inap ]::" : "::[ Validasi SBAR Rawat Jalan ]::");
        setSize(1200, 720);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        tabMode = new DefaultTableModel(null, new Object[]{
            "Tgl SBAR", "Jam", "Status", "No.Rawat", "No.RM", "Nama Pasien", "NIP", "Nama Pemeriksa",
            "Profesi", "DPJP", "Situation", "Background", "Assesmen", "Recommendation", "Advis",
            "Baca", "Konfirmasi", "Validasi", "Validator", "Tgl Validasi", "Jam Validasi"
        }) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        initComponents();
        baru();
        tampil();
    }

    /** Jumlah SBAR yang belum divalidasi untuk no_rawat & status tertentu. */
    public static int countBelum(String noRawat, String status) {
        if (noRawat == null || noRawat.trim().equals("")) {
            return 0;
        }
        String st = (status == null || status.trim().equals("")) ? "ralan" : status.trim();
        sekuel Sequel = new sekuel();
        String n = Sequel.cariIsi(
                "select count(*) from sbar_pasien where no_rawat=? and status='" + st + "' and validasi='Belum'",
                noRawat.trim());
        try {
            return (n == null || n.trim().equals("")) ? 0 : Integer.parseInt(n.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Versi worker: koneksi pembacaan dimiliki dan ditutup oleh pemanggil. */
    public static int countBelum(java.sql.Connection baca, String noRawat, String status) throws java.sql.SQLException {
        if (noRawat == null || noRawat.trim().equals("")) return 0;
        String st = status == null || status.trim().equals("") ? "ralan" : status.trim();
        try (java.sql.PreparedStatement p = baca.prepareStatement(
                "select count(*) from sbar_pasien where no_rawat=? and status=? and validasi='Belum'")) {
            p.setQueryTimeout(60);
            p.setString(1, noRawat.trim());
            p.setString(2, st);
            try (java.sql.ResultSet r = p.executeQuery()) {
                return r.next() ? r.getInt(1) : 0;
            }
        }
    }

    /** Cetak laporan SBAR pasien (rptSBARPerawatanPasien) untuk no_rawat & status tertentu. */
    public static void cetakSBAR(String noRawat, String status) {
        if (noRawat == null || noRawat.trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Pilih pasien terlebih dahulu untuk mencetak SBAR.");
            return;
        }
        String st = (status == null || status.trim().equals("")) ? "ralan" : status.trim();
        sekuel Sequel = new sekuel();
        validasi Valid = new validasi();
        String norm = Sequel.cariIsi("select reg_periksa.no_rkm_medis from reg_periksa where reg_periksa.no_rawat=?", noRawat.trim());
        String nama = Sequel.cariIsi("select pasien.nm_pasien from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where reg_periksa.no_rawat=?", noRawat.trim());
        java.util.Map<String, Object> param = new java.util.HashMap<String, Object>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
        param.put("norawat", noRawat.trim());
        param.put("norm", norm);
        param.put("namapasien", nama);
        param.put("statuspasien", st.equalsIgnoreCase("ranap") ? "Rawat Inap" : "Rawat Jalan");
        String sql = "select date_format(s.tgl_sbar,'%d-%m-%Y') as tgl_sbar,time_format(s.jam_sbar,'%H:%i:%s') as jam_sbar,"
                + "coalesce((select nm_dokter from dokter where kd_dokter=s.nip),(select nama from petugas where nip=s.nip),(select nama from pegawai where nik=s.nip),s.nip) as pemeriksa,"
                + "s.profesi,ifnull((select nm_dokter from dokter where kd_dokter=s.kd_dokter),'') as dpjp,"
                + "s.situation,s.background,s.assesmen,s.recommendation,s.advis,s.baca,s.konfirmasi,s.validasi,"
                + "ifnull((select nm_dokter from dokter where kd_dokter=s.validator),'') as validator,"
                + "if(s.validasi='Sudah',date_format(s.tgl_validasi,'%d-%m-%Y'),'') as tgl_validasi,"
                + "if(s.validasi='Sudah',time_format(s.jam_validasi,'%H:%i:%s'),'') as jam_validasi "
                + "from sbar_pasien s where s.no_rawat='" + noRawat.trim() + "' and s.status='" + st + "' "
                + "order by s.tgl_sbar,s.jam_sbar";
        Valid.MyReportqry("rptSBARPerawatanPasien.jasper", "report", "::[ Laporan SBAR Pasien ]::", sql, param);
    }

    private void initComponents() {
        setLayout(new BorderLayout(4, 4));
        CmbBaca.addItem("Belum");
        CmbBaca.addItem("Sudah");
        CmbKonfirmasi.addItem("Belum");
        CmbKonfirmasi.addItem("Sudah");

        TNoRw.setEditable(false);
        TNoRM.setEditable(false);
        TPasien.setEditable(false);
        KdValidator.setEditable(false);
        NmValidator.setEditable(false);
        NIP.setEditable(false);
        NmPemeriksa.setEditable(false);
        TglSBAR.setEditable(false);
        JamSBAR.setEditable(false);
        LStatus.setOpaque(true);
        LStatus.setHorizontalAlignment(SwingConstants.CENTER);
        LStatus.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));

        Situation.setEditable(false);
        Background.setEditable(false);
        Assesmen.setEditable(false);
        Recommendation.setEditable(false);
        for (JTextArea ta : new JTextArea[]{Situation, Background, Assesmen, Recommendation}) {
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
            ta.setFont(new Font("Tahoma", 0, 12));
        }
        Situation.setBackground(new Color(245, 245, 245));
        Background.setBackground(new Color(245, 245, 245));
        Assesmen.setBackground(new Color(245, 245, 245));
        Recommendation.setBackground(new Color(245, 245, 245));

        BtnCariValidator.setText("...");
        BtnCariValidator.setPreferredSize(new Dimension(34, 23));
        BtnValidasi.setText("Validasi");
        BtnBaru.setText("Baru");
        BtnKeluar.setText("Keluar");
        BtnCari.setText("Cari");

        BtnCariValidator.addActionListener(e -> bukaCariValidator());
        BtnValidasi.addActionListener(e -> validasi());
        BtnBaru.addActionListener(e -> baru());
        BtnKeluar.addActionListener(e -> dispose());
        BtnCari.addActionListener(e -> tampil());

        // ===== Form atas =====
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Detail SBAR"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(2, 3, 2, 3);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        addLabel(form, g, 0, 0, "No.Rawat :");
        form.add(gabung3(TNoRw, TNoRM, TPasien, null), gc(g, 1, 0, 3, 1.0));
        addLabel(form, g, 4, 0, "Status Verifikasi :");
        form.add(LStatus, gc(g, 5, 0, 1, 0.6));

        addLabel(form, g, 0, 1, "Validator :");
        form.add(gabung2(KdValidator, NmValidator, BtnCariValidator), gc(g, 1, 1, 3, 1.0));
        addLabel(form, g, 4, 1, "Pemeriksa :");
        form.add(gabung2(NIP, NmPemeriksa, null), gc(g, 5, 1, 1, 0.6));

        addLabel(form, g, 0, 2, "Tgl SBAR :");
        JPanel tglJam = new JPanel(new GridLayout(1, 2, 4, 0));
        tglJam.add(TglSBAR);
        tglJam.add(JamSBAR);
        form.add(tglJam, gc(g, 1, 2, 3, 1.0));
        addLabel(form, g, 4, 2, "Jam Validasi :");
        JamValidasi.setToolTipText("Format jam: HH:mm:ss (otomatis jam sekarang, bisa disesuaikan)");
        form.add(JamValidasi, gc(g, 5, 2, 1, 0.6));

        form.add(areaBerlabel("S (SITUATION)", Situation), gc(g, 0, 3, 2, 1.0));
        form.add(areaBerlabel("A (ASSESSMENT)", Assesmen), gc(g, 2, 3, 2, 1.0));
        GridBagConstraints gStat = gc(g, 4, 3, 2, 1.0);
        gStat.gridheight = 2;
        form.add(panelStatus(), gStat);

        form.add(areaBerlabel("B (BACKGROUND)", Background), gc(g, 0, 4, 2, 1.0));
        form.add(areaBerlabel("R (RECOMMENDATION)", Recommendation), gc(g, 2, 4, 2, 1.0));

        // ===== Tabel =====
        tbSBAR.setModel(tabMode);
        tbSBAR.setAutoResizeMode(widget.Table.AUTO_RESIZE_OFF);
        tbSBAR.setDefaultRenderer(Object.class, new WarnaTable());
        tbSBAR.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                getData();
            }
        });
        int[] lebar = {80, 60, 60, 120, 80, 160, 90, 150, 130, 120, 180, 180, 180, 180, 150, 55, 70, 65, 130, 90, 70};
        for (int i = 0; i < lebar.length; i++) {
            TableColumn c = tbSBAR.getColumnModel().getColumn(i);
            c.setPreferredWidth(lebar[i]);
        }
        JScrollPane scroll = new JScrollPane(tbSBAR);
        scroll.setBorder(BorderFactory.createTitledBorder(".: Input Data"));

        // ===== Filter + tombol =====
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        filter.add(new JLabel("Cari (No.Rawat / No.RM / Nama) :"));
        TCari.setPreferredSize(new Dimension(280, 23));
        filter.add(TCari);
        filter.add(BtnCari);

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        tombol.add(BtnValidasi);
        tombol.add(BtnBaru);
        tombol.add(new JLabel("  Jumlah :"));
        tombol.add(LCount);
        tombol.add(BtnKeluar);

        JPanel bawah = new JPanel(new BorderLayout());
        bawah.add(filter, BorderLayout.NORTH);
        bawah.add(tombol, BorderLayout.SOUTH);

        add(form, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bawah, BorderLayout.SOUTH);

        dokter.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent evt) {
                if (dokter.getTable().getSelectedRow() != -1) {
                    int r = dokter.getTable().getSelectedRow();
                    KdValidator.setText(dokter.getTable().getValueAt(r, 0).toString());
                    NmValidator.setText(dokter.getTable().getValueAt(r, 1).toString());
                }
            }
        });
    }

    private void addLabel(JPanel p, GridBagConstraints g, int x, int y, String teks) {
        JLabel l = new JLabel(teks);
        GridBagConstraints lc = (GridBagConstraints) g.clone();
        lc.gridx = x;
        lc.gridy = y;
        lc.weightx = 0;
        lc.fill = GridBagConstraints.NONE;
        p.add(l, lc);
    }

    private GridBagConstraints gc(GridBagConstraints base, int x, int y, int w, double wx) {
        GridBagConstraints c = (GridBagConstraints) base.clone();
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = w;
        c.weightx = wx;
        if (y >= 3) {
            c.fill = GridBagConstraints.BOTH;
            c.weighty = 1.0;
        }
        return c;
    }

    private JPanel gabung2(java.awt.Component a, java.awt.Component b, java.awt.Component btn) {
        JPanel p = new JPanel(new BorderLayout(3, 0));
        JPanel kiri = new JPanel(new GridLayout(1, 2, 3, 0));
        a.setPreferredSize(new Dimension(80, 23));
        kiri.add(a);
        kiri.add(b);
        p.add(kiri, BorderLayout.CENTER);
        if (btn != null) {
            p.add(btn, BorderLayout.EAST);
        }
        return p;
    }

    private JPanel gabung3(java.awt.Component a, java.awt.Component b, java.awt.Component c, java.awt.Component btn) {
        JPanel p = new JPanel(new BorderLayout(3, 0));
        JPanel kiri = new JPanel(new GridLayout(1, 3, 3, 0));
        a.setPreferredSize(new Dimension(90, 23));
        b.setPreferredSize(new Dimension(80, 23));
        kiri.add(a);
        kiri.add(b);
        kiri.add(c);
        p.add(kiri, BorderLayout.CENTER);
        if (btn != null) {
            p.add(btn, BorderLayout.EAST);
        }
        return p;
    }

    private JPanel areaBerlabel(String judul, JTextArea ta) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(judul));
        JScrollPane sc = new JScrollPane(ta);
        sc.setPreferredSize(new Dimension(200, 60));
        p.add(sc, BorderLayout.CENTER);
        return p;
    }

    private JPanel panelStatus() {
        JPanel p = new JPanel(new GridLayout(2, 2, 6, 4));
        p.setBorder(BorderFactory.createTitledBorder("Status"));
        p.add(new JLabel("Baca :"));
        p.add(CmbBaca);
        p.add(new JLabel("Konfirmasi :"));
        p.add(CmbKonfirmasi);
        return p;
    }

    private void bukaCariValidator() {
        dokter.emptTeks();
        dokter.isCek();
        dokter.setSize(900, 540);
        dokter.setLocationRelativeTo(this);
        dokter.setVisible(true);
    }

    private void baru() {
        tglKey = "";
        jamKey = "";
        TNoRw.setText(noRawatFilter);
        TNoRM.setText(noRkmMedis);
        TPasien.setText(namaPasien);
        NIP.setText("");
        NmPemeriksa.setText("");
        TglSBAR.setText("");
        JamSBAR.setText("");
        Situation.setText("");
        Background.setText("");
        Assesmen.setText("");
        Recommendation.setText("");
        CmbBaca.setSelectedItem("Sudah");
        CmbKonfirmasi.setSelectedItem("Sudah");
        JamValidasi.setText(jamSekarang());
        // Validator default = user login bila dokter
        String kd = akses.getkode();
        String nm = namaDokter(kd);
        KdValidator.setText(nm.equals("") ? "" : kd);
        NmValidator.setText(nm);
        setStatusLabel("Belum");
    }

    private void setStatusLabel(String v) {
        if (v != null && v.equalsIgnoreCase("Sudah")) {
            LStatus.setText("TERVERIFIKASI");
            LStatus.setBackground(new Color(200, 240, 200));
            LStatus.setForeground(new Color(20, 110, 20));
        } else {
            LStatus.setText("BELUM DIVALIDASI");
            LStatus.setBackground(new Color(250, 230, 200));
            LStatus.setForeground(new Color(150, 90, 0));
        }
    }

    /** Dipanggil host (DlgRawatJalan/DlgRawatInap) agar validasi langsung per pasien. */
    public void setNoRawat(String noRawat) {
        this.noRawatFilter = noRawat == null ? "" : noRawat.trim();
        this.noRkmMedis = "";
        this.namaPasien = "";
        if (!this.noRawatFilter.equals("")) {
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "select p.no_rkm_medis,p.nm_pasien from reg_periksa reg inner join pasien p "
                    + "on reg.no_rkm_medis=p.no_rkm_medis where reg.no_rawat=?")) {
                ps.setString(1, this.noRawatFilter);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        noRkmMedis = rs.getString(1);
                        namaPasien = rs.getString(2);
                    }
                }
            } catch (Exception e) {
                System.out.println("Notif setNoRawat SBAR : " + e);
            }
        }
        TNoRw.setText(noRawatFilter);
        TNoRM.setText(noRkmMedis);
        TPasien.setText(namaPasien);
        tampil();
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        String sql = "select s.tgl_sbar,s.jam_sbar,s.status,s.no_rawat,p.no_rkm_medis,p.nm_pasien,s.nip,s.profesi,"
                + "s.kd_dokter,s.situation,s.background,s.assesmen,s.recommendation,s.advis,s.baca,s.konfirmasi,"
                + "s.validasi,s.validator,s.tgl_validasi,s.jam_validasi "
                + "from sbar_pasien s inner join reg_periksa reg on s.no_rawat=reg.no_rawat "
                + "inner join pasien p on reg.no_rkm_medis=p.no_rkm_medis "
                + "where s.status=? "
                + (noRawatFilter.equals("") ? "" : "and s.no_rawat=? ")
                + "and (s.no_rawat like ? or p.no_rkm_medis like ? or p.nm_pasien like ?) "
                + "order by s.tgl_sbar desc,s.jam_sbar desc";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, status);
            if (!noRawatFilter.equals("")) {
                ps.setString(idx++, noRawatFilter);
            }
            String cari = "%" + TCari.getText().trim() + "%";
            ps.setString(idx++, cari);
            ps.setString(idx++, cari);
            ps.setString(idx++, cari);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tabMode.addRow(new Object[]{
                        rs.getString("tgl_sbar"), rs.getString("jam_sbar"), rs.getString("status"),
                        rs.getString("no_rawat"), rs.getString("no_rkm_medis"), rs.getString("nm_pasien"),
                        rs.getString("nip"), namaPetugas(rs.getString("nip")), rs.getString("profesi"),
                        namaDokter(rs.getString("kd_dokter")), rs.getString("situation"), rs.getString("background"),
                        rs.getString("assesmen"), rs.getString("recommendation"), rs.getString("advis"),
                        rs.getString("baca"), rs.getString("konfirmasi"), rs.getString("validasi"),
                        namaDokter(rs.getString("validator")), tampilTgl(rs.getString("tgl_validasi")),
                        tampilJam(rs.getString("jam_validasi"))
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tampil validasi SBAR : " + e);
        }
        LCount.setText("" + tabMode.getRowCount());
    }

    private void getData() {
        int r = tbSBAR.getSelectedRow();
        if (r == -1) {
            return;
        }
        tglKey = nilai(r, 0);
        jamKey = nilai(r, 1);
        TglSBAR.setText(tglKey);
        JamSBAR.setText(jamKey);
        TNoRw.setText(nilai(r, 3));
        TNoRM.setText(nilai(r, 4));
        TPasien.setText(nilai(r, 5));
        NIP.setText(nilai(r, 6));
        NmPemeriksa.setText(nilai(r, 7));
        Situation.setText(nilai(r, 10));
        Background.setText(nilai(r, 11));
        Assesmen.setText(nilai(r, 12));
        Recommendation.setText(nilai(r, 13));
        CmbBaca.setSelectedItem(nilai(r, 15).equals("") ? "Sudah" : nilai(r, 15));
        CmbKonfirmasi.setSelectedItem(nilai(r, 16).equals("") ? "Sudah" : nilai(r, 16));
        setStatusLabel(nilai(r, 17));
        JamValidasi.setText(jamSekarang());
        // Validator default tetap = user login (yang akan memvalidasi sekarang)
        String kd = akses.getkode();
        String nm = namaDokter(kd);
        KdValidator.setText(nm.equals("") ? "" : kd);
        NmValidator.setText(nm);
    }

    private void validasi() {
        if (tglKey.equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih SBAR yang akan divalidasi terlebih dahulu.");
            return;
        }
        if (KdValidator.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Validator (dokter) belum dipilih.\nUser login bukan dokter, silakan pilih dokter validator.");
            return;
        }
        if (JamValidasi.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Jam validasi belum diisi.");
            return;
        }
        String tglV = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String jamV = JamValidasi.getText().trim();
        try (PreparedStatement ps = koneksi.prepareStatement(
                "update sbar_pasien set validasi='Sudah',validator=?,tgl_validasi=?,jam_validasi=?,"
                + "baca=?,konfirmasi=? where no_rawat=? and tgl_sbar=? and jam_sbar=?")) {
            ps.setString(1, KdValidator.getText());
            ps.setString(2, tglV);
            ps.setString(3, jamV);
            ps.setString(4, CmbBaca.getSelectedItem().toString());
            ps.setString(5, CmbKonfirmasi.getSelectedItem().toString());
            ps.setString(6, TNoRw.getText());
            ps.setString(7, tglKey);
            ps.setString(8, jamKey);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "SBAR berhasil divalidasi.");
            setStatusLabel("Sudah");
            tampil();
            baru();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memvalidasi SBAR.\n" + e.getMessage());
        }
    }

    private String nilai(int r, int c) {
        Object o = tbSBAR.getValueAt(r, c);
        return o == null ? "" : o.toString();
    }

    private String tampilTgl(String t) {
        return (t == null || t.startsWith("0000")) ? "" : t;
    }

    private String tampilJam(String t) {
        return (t == null || t.equals("00:00:00")) ? "" : t;
    }

    private String jamSekarang() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    private String namaDokter(String kd) {
        if (kd == null || kd.trim().equals("")) {
            return "";
        }
        String nm = Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", kd);
        return nm == null ? "" : nm;
    }

    private String namaPetugas(String kode) {
        if (kode == null || kode.trim().equals("")) {
            return "";
        }
        String nm = Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", kode);
        if (nm == null || nm.trim().equals("")) {
            nm = Sequel.cariIsi("select nama from petugas where nip=?", kode);
        }
        if (nm == null || nm.trim().equals("")) {
            nm = Sequel.cariIsi("select nama from pegawai where nik=?", kode);
        }
        return nm == null ? "" : nm;
    }
}
