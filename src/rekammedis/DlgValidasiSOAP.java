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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
 * Dialog validasi SOAP. SOAP yang diinput SELAIN DPJP (dokter lain maupun
 * non-dokter) divalidasi oleh DPJP; SOAP dari DPJP sendiri tidak perlu.
 * Diparameter status ("ralan"/"ranap") -> tabel pemeriksaan_ralan /
 * pemeriksaan_ranap. Filter per no_rawat (pasien yang sedang dibuka).
 */
public final class DlgValidasiSOAP extends JDialog {
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final String status;
    private final String tabel;

    /** Kondisi SQL (alias pr=pemeriksaan, pg=pegawai): penginput SOAP adalah
     *  DPJP pasien pada no_rawat tsb -> tidak perlu divalidasi. SOAP dari
     *  dokter lain maupun non-dokter (bukan DPJP) tetap butuh validasi DPJP. */
    private static String kondisiPenginputDpjp(String status) {
        if ("ranap".equalsIgnoreCase(status)) {
            return "exists(select 1 from dpjp_ranap dd inner join dokter dk on dd.kd_dokter=dk.kd_dokter "
                    + "where dd.no_rawat=pr.no_rawat and lower(trim(dk.nm_dokter))=lower(trim(ifnull(pg.nama,''))))";
        }
        return "exists(select 1 from reg_periksa rr inner join dokter dk on rr.kd_dokter=dk.kd_dokter "
                + "where rr.no_rawat=pr.no_rawat and lower(trim(dk.nm_dokter))=lower(trim(ifnull(pg.nama,''))))";
    }

    private final widget.TextBox TNoRw = new widget.TextBox();
    private final widget.TextBox TNoRM = new widget.TextBox();
    private final widget.TextBox TPasien = new widget.TextBox();
    private final widget.TextBox KdValidator = new widget.TextBox();
    private final widget.TextBox NmValidator = new widget.TextBox();
    private final widget.TextBox NIP = new widget.TextBox();
    private final widget.TextBox NmPenginput = new widget.TextBox();
    private final widget.TextBox TglSOAP = new widget.TextBox();
    private final widget.TextBox JamSOAP = new widget.TextBox();
    private final widget.TextBox JamValidasi = new widget.TextBox();
    private final JLabel LStatus = new JLabel("-");

    private final JTextArea Subjek = new JTextArea();
    private final JTextArea Objek = new JTextArea();
    private final JTextArea Asesmen = new JTextArea();
    private final JTextArea Plan = new JTextArea();
    private final JTextArea Instruksi = new JTextArea();
    private final JTextArea Evaluasi = new JTextArea();

    private final widget.Button BtnCariValidator = new widget.Button();
    private final widget.Button BtnValidasi = new widget.Button();
    private final widget.Button BtnPilihSemua = new widget.Button();
    private final widget.Button BtnBaru = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();
    private final widget.Button BtnCari = new widget.Button();
    private final widget.TextBox TCari = new widget.TextBox();
    private final JLabel LCount = new JLabel("0");

    private final widget.Table tbSOAP = new widget.Table();
    private final DefaultTableModel tabMode;
    private final DlgCariDokter dokter = new DlgCariDokter(null, true);

    private String tglKey = "";
    private String jamKey = "";
    private String noRawatFilter = "";
    private String noRkmMedis = "";
    private String namaPasien = "";

    public DlgValidasiSOAP(Frame parent, boolean modal, String status) {
        super(parent, modal);
        this.status = (status == null || status.trim().equals("")) ? "ralan" : status.trim();
        this.tabel = this.status.equals("ranap") ? "pemeriksaan_ranap" : "pemeriksaan_ralan";
        setTitle(this.status.equals("ranap") ? "::[ Validasi SOAP Rawat Inap ]::" : "::[ Validasi SOAP Rawat Jalan ]::");
        setSize(1200, 720);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        tabMode = new DefaultTableModel(null, new Object[]{
            "Pilih", "Tgl", "Jam", "No.Rawat", "No.RM", "Nama Pasien", "NIP", "Penginput", "Profesi",
            "Subjek", "Objek", "Asesmen", "Plan", "Instruksi", "Evaluasi",
            "Keterangan", "Validator", "Tgl Validasi", "Jam Validasi"
        }) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 0;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return c == 0 ? Boolean.class : Object.class;
            }
        };
        initComponents();
        baru();
        tampil();
    }

    /** Pastikan kolom validasi/validator/tgl_validasi/jam_validasi ada di tabel
     *  pemeriksaan (auto-create bila belum ada), agar fitur validasi SOAP tidak
     *  bergantung pada ALTER TABLE manual dan query tabel utama tidak error. */
    public static void pastikanKolom(String tabel) {
        try {
            sekuel Sequel = new sekuel();
            String ada = Sequel.cariIsi(
                    "select count(*) from information_schema.columns where table_schema=database() "
                    + "and table_name='" + tabel + "' and column_name='validasi'");
            if (ada != null && ada.trim().equals("0")) {
                Sequel.queryu("alter table " + tabel + " "
                        + "add column validasi enum('Belum','Sudah') not null default 'Belum', "
                        + "add column validator varchar(20) not null default '', "
                        + "add column tgl_validasi date null, "
                        + "add column jam_validasi time null");
            }
        } catch (Exception e) {
            System.out.println("Notif pastikanKolom validasi (" + tabel + ") : " + e);
        }
    }

    /** Jumlah SOAP non-dokter yang belum divalidasi untuk no_rawat tertentu. */
    public static int countBelum(String noRawat, String status) {
        if (noRawat == null || noRawat.trim().equals("")) {
            return 0;
        }
        String tbl = ("ranap".equalsIgnoreCase(status)) ? "pemeriksaan_ranap" : "pemeriksaan_ralan";
        sekuel Sequel = new sekuel();
        String n = Sequel.cariIsi(
                "select count(*) from " + tbl + " pr left join pegawai pg on pr.nip=pg.nik "
                + "where pr.no_rawat=? and pr.validasi='Belum' and not " + kondisiPenginputDpjp(status), noRawat.trim());
        try {
            return (n == null || n.trim().equals("")) ? 0 : Integer.parseInt(n.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(4, 4));

        TNoRw.setEditable(false);
        TNoRM.setEditable(false);
        TPasien.setEditable(false);
        KdValidator.setEditable(false);
        NmValidator.setEditable(false);
        NIP.setEditable(false);
        NmPenginput.setEditable(false);
        TglSOAP.setEditable(false);
        JamSOAP.setEditable(false);
        LStatus.setOpaque(true);
        LStatus.setHorizontalAlignment(SwingConstants.CENTER);
        LStatus.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));

        for (JTextArea ta : new JTextArea[]{Subjek, Objek, Asesmen, Plan, Instruksi, Evaluasi}) {
            ta.setEditable(true);
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
            ta.setFont(new Font("Tahoma", 0, 12));
            ta.setBackground(new Color(255, 255, 255));
        }

        BtnCariValidator.setText("...");
        BtnCariValidator.setPreferredSize(new Dimension(34, 23));
        BtnValidasi.setText("Validasi Tercentang");
        BtnPilihSemua.setText("Centang Semua (Belum)");
        BtnBaru.setText("Baru");
        BtnKeluar.setText("Keluar");
        BtnCari.setText("Cari");

        BtnCariValidator.addActionListener(e -> bukaCariValidator());
        BtnValidasi.addActionListener(e -> validasi());
        BtnPilihSemua.addActionListener(e -> centangSemua());
        BtnBaru.addActionListener(e -> baru());
        BtnKeluar.addActionListener(e -> dispose());
        BtnCari.addActionListener(e -> tampil());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Detail SOAP"));
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
        addLabel(form, g, 4, 1, "Penginput :");
        form.add(gabung2(NIP, NmPenginput, null), gc(g, 5, 1, 1, 0.6));

        addLabel(form, g, 0, 2, "Tgl SOAP :");
        JPanel tglJam = new JPanel(new GridLayout(1, 2, 4, 0));
        tglJam.add(TglSOAP);
        tglJam.add(JamSOAP);
        form.add(tglJam, gc(g, 1, 2, 3, 1.0));
        addLabel(form, g, 4, 2, "Jam Validasi :");
        JamValidasi.setToolTipText("Format jam: HH:mm:ss (otomatis jam sekarang, bisa disesuaikan)");
        form.add(JamValidasi, gc(g, 5, 2, 1, 0.6));

        form.add(areaBerlabel("S (SUBJEK)", Subjek), gc(g, 0, 3, 2, 1.0));
        form.add(areaBerlabel("O (OBJEK)", Objek), gc(g, 2, 3, 2, 1.0));
        form.add(areaBerlabel("A (ASESMEN)", Asesmen), gc(g, 4, 3, 2, 1.0));
        form.add(areaBerlabel("P (PLAN)", Plan), gc(g, 0, 4, 2, 1.0));
        form.add(areaBerlabel("INSTRUKSI", Instruksi), gc(g, 2, 4, 2, 1.0));
        form.add(areaBerlabel("EVALUASI", Evaluasi), gc(g, 4, 4, 2, 1.0));

        tbSOAP.setModel(tabMode);
        tbSOAP.setAutoResizeMode(widget.Table.AUTO_RESIZE_OFF);
        tbSOAP.setDefaultRenderer(Object.class, new WarnaTable());
        tbSOAP.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                getData();
            }
        });
        int[] lebar = {45, 80, 60, 120, 80, 160, 90, 150, 130, 180, 180, 180, 180, 160, 160, 130, 130, 90, 70};
        for (int i = 0; i < lebar.length; i++) {
            TableColumn c = tbSOAP.getColumnModel().getColumn(i);
            c.setPreferredWidth(lebar[i]);
        }
        JScrollPane scroll = new JScrollPane(tbSOAP);
        scroll.setBorder(BorderFactory.createTitledBorder(".: SOAP Belum/Sudah Divalidasi (input selain DPJP)"));

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        filter.add(new JLabel("Cari (No.Rawat / No.RM / Nama) :"));
        TCari.setPreferredSize(new Dimension(280, 23));
        filter.add(TCari);
        filter.add(BtnCari);

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        tombol.add(BtnPilihSemua);
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
        NmPenginput.setText("");
        TglSOAP.setText("");
        JamSOAP.setText("");
        Subjek.setText("");
        Objek.setText("");
        Asesmen.setText("");
        Plan.setText("");
        Instruksi.setText("");
        Evaluasi.setText("");
        JamValidasi.setText(jamSekarang());
        setValidatorDpjp(noRawatFilter);
        setStatusLabel("Belum");
    }

    /** Set validator default = DPJP pasien pada no_rawat; fallback dokter login. */
    private void setValidatorDpjp(String noRawat) {
        String kd = "";
        if (noRawat != null && !noRawat.trim().equals("")) {
            if (status.equals("ranap")) {
                kd = Sequel.cariIsi("select kd_dokter from dpjp_ranap where no_rawat=? order by kd_dokter limit 1", noRawat.trim());
            } else {
                kd = Sequel.cariIsi("select kd_dokter from reg_periksa where no_rawat=?", noRawat.trim());
            }
        }
        if (kd == null) {
            kd = "";
        }
        String nm = namaDokter(kd);
        if (nm.equals("")) {
            kd = akses.getkode();
            nm = namaDokter(kd);
        }
        KdValidator.setText(nm.equals("") ? "" : kd);
        NmValidator.setText(nm);
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

    /** Dipanggil host agar validasi langsung per pasien. */
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
                System.out.println("Notif setNoRawat SOAP : " + e);
            }
        }
        TNoRw.setText(noRawatFilter);
        TNoRM.setText(noRkmMedis);
        TPasien.setText(namaPasien);
        tampil();
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        String sql = "select pr.tgl_perawatan,pr.jam_rawat,pr.no_rawat,p.no_rkm_medis,p.nm_pasien,pr.nip,"
                + "ifnull(pg.nama,'') as penginput,ifnull(pg.jbtn,'') as profesi,"
                + "pr.keluhan,pr.pemeriksaan,pr.penilaian,pr.rtl,pr.instruksi,pr.evaluasi,"
                + "pr.validasi,pr.validator,pr.tgl_validasi,pr.jam_validasi "
                + "from " + tabel + " pr "
                + "inner join reg_periksa reg on pr.no_rawat=reg.no_rawat "
                + "inner join pasien p on reg.no_rkm_medis=p.no_rkm_medis "
                + "left join pegawai pg on pr.nip=pg.nik "
                + "where not " + kondisiPenginputDpjp(status) + " "
                + (noRawatFilter.equals("") ? "" : "and pr.no_rawat=? ")
                + "and (pr.no_rawat like ? or p.no_rkm_medis like ? or p.nm_pasien like ?) "
                + "order by pr.tgl_perawatan desc,pr.jam_rawat desc";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            int idx = 1;
            if (!noRawatFilter.equals("")) {
                ps.setString(idx++, noRawatFilter);
            }
            String cari = "%" + TCari.getText().trim() + "%";
            ps.setString(idx++, cari);
            ps.setString(idx++, cari);
            ps.setString(idx++, cari);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String val = rs.getString("validasi");
                    String ket = (val != null && val.equalsIgnoreCase("Sudah")) ? "Sudah Divalidasi" : "Belum Divalidasi";
                    tabMode.addRow(new Object[]{
                        Boolean.FALSE,
                        rs.getString("tgl_perawatan"), rs.getString("jam_rawat"), rs.getString("no_rawat"),
                        rs.getString("no_rkm_medis"), rs.getString("nm_pasien"), rs.getString("nip"),
                        rs.getString("penginput"), rs.getString("profesi"), rs.getString("keluhan"),
                        rs.getString("pemeriksaan"), rs.getString("penilaian"), rs.getString("rtl"),
                        rs.getString("instruksi"), rs.getString("evaluasi"), ket,
                        namaDokter(rs.getString("validator")), tampilTgl(rs.getString("tgl_validasi")),
                        tampilJam(rs.getString("jam_validasi"))
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tampil validasi SOAP : " + e);
        }
        LCount.setText("" + tabMode.getRowCount());
    }

    private void getData() {
        int r = tbSOAP.getSelectedRow();
        if (r == -1) {
            return;
        }
        tglKey = nilai(r, 1);
        jamKey = nilai(r, 2);
        TglSOAP.setText(tglKey);
        JamSOAP.setText(jamKey);
        TNoRw.setText(nilai(r, 3));
        TNoRM.setText(nilai(r, 4));
        TPasien.setText(nilai(r, 5));
        NIP.setText(nilai(r, 6));
        NmPenginput.setText(nilai(r, 7));
        Subjek.setText(nilai(r, 9));
        Objek.setText(nilai(r, 10));
        Asesmen.setText(nilai(r, 11));
        Plan.setText(nilai(r, 12));
        Instruksi.setText(nilai(r, 13));
        Evaluasi.setText(nilai(r, 14));
        setStatusLabel(nilai(r, 15).startsWith("Sudah") ? "Sudah" : "Belum");
        JamValidasi.setText(jamSekarang());
        setValidatorDpjp(nilai(r, 3));
    }

    /** Centang semua baris yang belum divalidasi. */
    private void centangSemua() {
        boolean adaBelum = false;
        for (int i = 0; i < tabMode.getRowCount(); i++) {
            if (nilaiModel(i, 15).startsWith("Belum")) {
                tabMode.setValueAt(Boolean.TRUE, i, 0);
                adaBelum = true;
            }
        }
        if (!adaBelum) {
            JOptionPane.showMessageDialog(this, "Tidak ada SOAP yang belum divalidasi.");
        }
    }

    /** Validasi semua baris tercentang (yang masih 'Belum'); fallback ke baris terpilih. */
    private void validasi() {
        List<Integer> rows = new ArrayList<>();
        for (int i = 0; i < tabMode.getRowCount(); i++) {
            Object cek = tabMode.getValueAt(i, 0);
            if (Boolean.TRUE.equals(cek) && nilaiModel(i, 15).startsWith("Belum")) {
                rows.add(i);
            }
        }
        if (rows.isEmpty()) {
            int r = tbSOAP.getSelectedRow();
            if (r == -1) {
                JOptionPane.showMessageDialog(this, "Centang minimal satu SOAP (atau pilih satu baris) yang akan divalidasi.");
                return;
            }
            int mr = tbSOAP.convertRowIndexToModel(r);
            if (!nilaiModel(mr, 15).startsWith("Belum")) {
                JOptionPane.showMessageDialog(this, "SOAP yang dipilih sudah tervalidasi.");
                return;
            }
            rows.add(mr);
        }
        if (KdValidator.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Validator (dokter) belum dipilih.\nUser login bukan dokter, silakan pilih dokter validator.");
            return;
        }
        if (JamValidasi.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Jam validasi belum diisi.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Validasi " + rows.size() + " data SOAP terpilih?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        String tglV = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String jamV = JamValidasi.getText().trim();
        int sukses = 0;
        try {
            for (int mr : rows) {
                String noRw = nilaiModel(mr, 3);
                String tgl = nilaiModel(mr, 1);
                String jam = nilaiModel(mr, 2);
                boolean barisDibuka = !tglKey.equals("") && noRw.equals(TNoRw.getText())
                        && tgl.equals(tglKey) && jam.equals(jamKey);
                if (barisDibuka) {
                    // Baris yang sedang dibuka di form: simpan juga editan isi SOAP
                    try (PreparedStatement ps = koneksi.prepareStatement(
                            "update " + tabel + " set keluhan=?,pemeriksaan=?,penilaian=?,rtl=?,instruksi=?,evaluasi=?,"
                            + "validasi='Sudah',validator=?,tgl_validasi=?,jam_validasi=? "
                            + "where no_rawat=? and tgl_perawatan=? and jam_rawat=?")) {
                        ps.setString(1, Subjek.getText());
                        ps.setString(2, Objek.getText());
                        ps.setString(3, Asesmen.getText());
                        ps.setString(4, Plan.getText());
                        ps.setString(5, Instruksi.getText());
                        ps.setString(6, Evaluasi.getText());
                        ps.setString(7, KdValidator.getText());
                        ps.setString(8, tglV);
                        ps.setString(9, jamV);
                        ps.setString(10, noRw);
                        ps.setString(11, tgl);
                        ps.setString(12, jam);
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = koneksi.prepareStatement(
                            "update " + tabel + " set validasi='Sudah',validator=?,tgl_validasi=?,jam_validasi=? "
                            + "where no_rawat=? and tgl_perawatan=? and jam_rawat=?")) {
                        ps.setString(1, KdValidator.getText());
                        ps.setString(2, tglV);
                        ps.setString(3, jamV);
                        ps.setString(4, noRw);
                        ps.setString(5, tgl);
                        ps.setString(6, jam);
                        ps.executeUpdate();
                    }
                }
                sukses++;
            }
            JOptionPane.showMessageDialog(this, sukses + " data SOAP berhasil divalidasi.");
            tampil();
            baru();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memvalidasi SOAP.\n" + e.getMessage());
        }
    }

    private String nilai(int r, int c) {
        Object o = tbSOAP.getValueAt(r, c);
        return o == null ? "" : o.toString();
    }

    private String nilaiModel(int r, int c) {
        Object o = tabMode.getValueAt(r, c);
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
}
