package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;
import kepegawaian.DlgCariPegawai;

/**
 * Panel input SBAR (Situation, Background, Assessment, Recommendation) yang
 * dipasang sebagai tab di DlgRawatJalan (status "ralan") dan DlgRawatInap
 * (status "ranap"). Data disimpan ke tabel sbar_pasien (1 tabel gabungan).
 */
public final class PanelSBAR extends JPanel {
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();

    private final String status; // "ralan" / "ranap"
    private String noRawat = "";
    private String noRkmMedis = "";
    private String namaPasien = "";
    private boolean ubah = false;
    private String tglLama = "";
    private String jamLama = "";

    private final widget.TextBox NIP = new widget.TextBox();
    private final widget.TextBox NamaPemeriksa = new widget.TextBox();
    private final widget.TextBox Profesi = new widget.TextBox();
    private final widget.TextBox KdDPJP = new widget.TextBox();
    private final widget.TextBox NamaDPJP = new widget.TextBox();
    private final JLabel LStatus = new JLabel("-");
    private final widget.ComboBox CmbBaca = new widget.ComboBox();
    private final widget.ComboBox CmbKonfirmasi = new widget.ComboBox();

    private final JTextArea Situation = new JTextArea();
    private final JTextArea Background = new JTextArea();
    private final JTextArea Assesmen = new JTextArea();
    private final JTextArea Recommendation = new JTextArea();

    private final widget.Button BtnCariPemeriksa = new widget.Button();
    private final widget.Button BtnCariDPJP = new widget.Button();
    private final widget.Button BtnBaru = new widget.Button();
    private final widget.Button BtnSimpan = new widget.Button();
    private final widget.Button BtnHapus = new widget.Button();
    private final widget.Button BtnCetak = new widget.Button();
    private final JLabel LCount = new JLabel("0");

    private final widget.Table tbSBAR = new widget.Table();
    private final DefaultTableModel tabMode;

    private final DlgCariPegawai pegawai = new DlgCariPegawai(null, true);
    private final DlgCariDokter dokter = new DlgCariDokter(null, true);

    public PanelSBAR(String status) {
        this.status = (status == null || status.trim().equals("")) ? "ralan" : status.trim();
        tabMode = new DefaultTableModel(null, new Object[]{
            "Tgl SBAR", "Jam", "NIP", "Nama Pemeriksa", "Profesi", "DPJP",
            "Situation", "Background", "Assesmen", "Recommendation", "Advis",
            "Baca", "Konfirmasi", "Validasi", "Validator", "Tgl Validasi", "Jam Validasi"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        initComponents();
        baru();
    }

    private void initComponents() {
        setLayout(new BorderLayout(4, 4));

        CmbBaca.addItem("Belum");
        CmbBaca.addItem("Sudah");
        CmbKonfirmasi.addItem("Belum");
        CmbKonfirmasi.addItem("Sudah");

        NIP.setEditable(false);
        NamaPemeriksa.setEditable(false);
        KdDPJP.setEditable(false);
        NamaDPJP.setEditable(false);
        Profesi.setDocument(new batasInput((byte) 100).getKata(Profesi));
        LStatus.setOpaque(true);
        LStatus.setHorizontalAlignment(SwingConstants.CENTER);
        LStatus.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));

        for (JTextArea ta : new JTextArea[]{Situation, Background, Assesmen, Recommendation}) {
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
            ta.setFont(new java.awt.Font("Tahoma", 0, 12));
        }

        BtnCariPemeriksa.setText("...");
        BtnCariPemeriksa.setPreferredSize(new Dimension(34, 23));
        BtnCariDPJP.setText("...");
        BtnCariDPJP.setPreferredSize(new Dimension(34, 23));
        BtnBaru.setText("Baru");
        BtnSimpan.setText("Simpan SBAR");
        BtnHapus.setText("Hapus SBAR");
        BtnCetak.setText("Status Verifikasi SBAR");
        java.awt.Font fontAksi = new java.awt.Font("Tahoma", 1, 12);
        BtnBaru.setFont(fontAksi);
        BtnSimpan.setFont(fontAksi);
        BtnHapus.setFont(fontAksi);
        BtnCetak.setFont(fontAksi);
        BtnBaru.setPreferredSize(new Dimension(90, 30));
        BtnSimpan.setPreferredSize(new Dimension(140, 30));
        BtnHapus.setPreferredSize(new Dimension(135, 30));
        BtnCetak.setPreferredSize(new Dimension(205, 30));

        BtnCariPemeriksa.addActionListener(e -> bukaCariPemeriksa());
        BtnCariDPJP.addActionListener(e -> bukaCariDPJP());
        BtnBaru.addActionListener(e -> baru());
        BtnSimpan.addActionListener(e -> simpan());
        BtnHapus.addActionListener(e -> hapus());
        BtnCetak.addActionListener(e -> {
            DlgSBARPerawatanPasien d = new DlgSBARPerawatanPasien(null, true, status);
            d.setNoRawat(noRawat);
            d.setVisible(true);
        });

        // Tab SBAR sekarang HANYA untuk verifikasi -- SBAR-nya sendiri sudah otomatis terisi
        // dari SOAP (lihat DlgRawatInap/DlgRawatJalan, checkbox "SBAR Otomatis"), jadi input
        // manual (Baru/Simpan/Hapus + pilih ulang petugas/DPJP) tidak diperlukan lagi di sini.
        // Method baru()/simpan()/hapus() TETAP ada (dipakai internal, mis. baru() dari
        // setKonteks()) -- yg diubah cuma UI-nya, supaya tidak mengubah alur data yg sudah ada.
        BtnBaru.setVisible(false);
        BtnSimpan.setVisible(false);
        BtnHapus.setVisible(false);
        BtnCariPemeriksa.setVisible(false);
        BtnCariDPJP.setVisible(false);
        Profesi.setEditable(false);
        CmbBaca.setEnabled(false);
        CmbKonfirmasi.setEnabled(false);
        for (JTextArea ta : new JTextArea[]{Situation, Background, Assesmen, Recommendation}) {
            ta.setEditable(false);
        }

        // ===== Form atas =====
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Detail SBAR (Verifikasi)"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(2, 3, 2, 3);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        // baris 0: Dilakukan + Status verifikasi
        addLabel(form, g, 0, 0, "Dilakukan :");
        form.add(gabungPemeriksa(), gc(g, 1, 0, 3, 1.0));
        addLabel(form, g, 4, 0, "Status Verifikasi :");
        form.add(LStatus, gc(g, 5, 0, 1, 0.6));

        // baris 1: Profesi
        addLabel(form, g, 0, 1, "Profesi/Jabatan :");
        form.add(Profesi, gc(g, 1, 1, 3, 1.0));
        addLabel(form, g, 4, 1, "Dokter DPJP :");
        form.add(gabungDPJP(), gc(g, 5, 1, 1, 0.6));

        // baris 2: S | A | Status (Baca/Konfirmasi membentang 2 baris)
        form.add(areaBerlabel("S (SITUATION)", Situation), gc(g, 0, 2, 2, 1.0));
        form.add(areaBerlabel("A (ASSESSMENT)", Assesmen), gc(g, 2, 2, 2, 1.0));
        GridBagConstraints gStat = gc(g, 4, 2, 2, 1.0);
        gStat.gridheight = 2;
        form.add(panelBacaKonfirmasi(), gStat);

        // baris 3: B | R
        form.add(areaBerlabel("B (BACKGROUND)", Background), gc(g, 0, 3, 2, 1.0));
        form.add(areaBerlabel("R (RECOMMENDATION)", Recommendation), gc(g, 2, 3, 2, 1.0));

        // ===== Tabel input =====
        tbSBAR.setModel(tabMode);
        tbSBAR.setAutoResizeMode(widget.Table.AUTO_RESIZE_OFF);
        tbSBAR.setDefaultRenderer(Object.class, new WarnaTable());
        tbSBAR.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                getData();
            }
        });
        int[] lebar = {80, 60, 90, 170, 150, 120, 200, 200, 200, 200, 160, 60, 70, 70, 120, 80, 70};
        for (int i = 0; i < lebar.length; i++) {
            TableColumn c = tbSBAR.getColumnModel().getColumn(i);
            c.setPreferredWidth(lebar[i]);
        }
        JScrollPane scroll = new JScrollPane(tbSBAR);
        scroll.setBorder(BorderFactory.createTitledBorder(".: Daftar SBAR"));

        // ===== Tombol bawah =====
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bawah.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 200), 2));
        bawah.add(BtnBaru);
        bawah.add(BtnSimpan);
        bawah.add(BtnHapus);
        bawah.add(BtnCetak);
        bawah.add(new JLabel("     Jumlah :"));
        bawah.add(LCount);

        JPanel atas = new JPanel(new BorderLayout());
        atas.add(form, BorderLayout.CENTER);

        add(atas, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bawah, BorderLayout.SOUTH);

        pasangListenerPemeriksa();
        pasangListenerDPJP();
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
        if (y >= 2) {
            c.fill = GridBagConstraints.BOTH;
            c.weighty = 1.0;
        }
        return c;
    }

    private JPanel gabungPemeriksa() {
        JPanel p = new JPanel(new BorderLayout(3, 0));
        JPanel kiri = new JPanel(new GridLayout(1, 2, 3, 0));
        NIP.setPreferredSize(new Dimension(90, 23));
        kiri.add(NIP);
        kiri.add(NamaPemeriksa);
        p.add(kiri, BorderLayout.CENTER);
        p.add(BtnCariPemeriksa, BorderLayout.EAST);
        return p;
    }

    private JPanel gabungDPJP() {
        JPanel p = new JPanel(new BorderLayout(3, 0));
        JPanel kiri = new JPanel(new GridLayout(1, 2, 3, 0));
        KdDPJP.setPreferredSize(new Dimension(70, 23));
        kiri.add(KdDPJP);
        kiri.add(NamaDPJP);
        p.add(kiri, BorderLayout.CENTER);
        p.add(BtnCariDPJP, BorderLayout.EAST);
        return p;
    }

    private JPanel areaBerlabel(String judul, JTextArea ta) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(judul));
        JScrollPane sc = new JScrollPane(ta);
        sc.setPreferredSize(new Dimension(200, 70));
        p.add(sc, BorderLayout.CENTER);
        return p;
    }

    private JPanel panelBacaKonfirmasi() {
        JPanel p = new JPanel(new GridLayout(2, 2, 6, 4));
        p.setBorder(BorderFactory.createTitledBorder("Status"));
        p.add(new JLabel("Baca :"));
        p.add(CmbBaca);
        p.add(new JLabel("Konfirmasi :"));
        p.add(CmbKonfirmasi);
        return p;
    }

    private void pasangListenerPemeriksa() {
        pegawai.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent evt) {
                if (pegawai.getTable().getSelectedRow() != -1) {
                    int r = pegawai.getTable().getSelectedRow();
                    NIP.setText(pegawai.getTable().getValueAt(r, 0).toString());
                    NamaPemeriksa.setText(pegawai.getTable().getValueAt(r, 1).toString());
                    String jab = pegawai.getTable().getValueAt(r, 3) == null ? "" : pegawai.getTable().getValueAt(r, 3).toString();
                    String dep = pegawai.getTable().getValueAt(r, 5) == null ? "" : pegawai.getTable().getValueAt(r, 5).toString();
                    Profesi.setText((jab + (dep.equals("") ? "" : " / " + dep)).trim());
                }
            }
        });
    }

    private void pasangListenerDPJP() {
        dokter.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent evt) {
                if (dokter.getTable().getSelectedRow() != -1) {
                    int r = dokter.getTable().getSelectedRow();
                    KdDPJP.setText(dokter.getTable().getValueAt(r, 0).toString());
                    NamaDPJP.setText(dokter.getTable().getValueAt(r, 1).toString());
                }
            }
        });
    }

    private void bukaCariPemeriksa() {
        pegawai.emptTeks();
        pegawai.setSize(900, 540);
        pegawai.setLocationRelativeTo(this);
        pegawai.setVisible(true);
    }

    private void bukaCariDPJP() {
        dokter.emptTeks();
        dokter.isCek();
        dokter.setSize(900, 540);
        dokter.setLocationRelativeTo(this);
        dokter.setVisible(true);
    }

    /** Dipanggil host (DlgRawatJalan/DlgRawatInap) saat tab SBAR dibuka untuk pasien tertentu. */
    public void setKonteks(String noRawat) {
        this.noRawat = noRawat == null ? "" : noRawat.trim();
        this.noRkmMedis = "";
        this.namaPasien = "";
        if (!this.noRawat.equals("")) {
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis,pasien.nm_pasien from reg_periksa "
                    + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where reg_periksa.no_rawat=?")) {
                ps.setString(1, this.noRawat);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        noRkmMedis = rs.getString(1);
                        namaPasien = rs.getString(2);
                    }
                }
            } catch (Exception e) {
                System.out.println("Notif konteks SBAR : " + e);
            }
        }
        baru();
        tampil();
    }

    private void baru() {
        ubah = false;
        tglLama = "";
        jamLama = "";
        NIP.setText(akses.getkode());
        NamaPemeriksa.setText(namaPemeriksa(akses.getkode()));
        Profesi.setText(resolveProfesi(akses.getkode()));
        // DPJP otomatis = dokter penanggung jawab pasien (dpjp_ranap -> reg_periksa.kd_dokter)
        KdDPJP.setText("");
        NamaDPJP.setText("");
        if (!noRawat.equals("")) {
            String kd = Sequel.cariIsi("select dpjp_ranap.kd_dokter from dpjp_ranap where dpjp_ranap.no_rawat=?", noRawat);
            if (kd == null || kd.trim().equals("")) {
                kd = Sequel.cariIsi("select reg_periksa.kd_dokter from reg_periksa where reg_periksa.no_rawat=?", noRawat);
            }
            if (kd != null && !kd.trim().equals("")) {
                KdDPJP.setText(kd);
                NamaDPJP.setText(namaDokter(kd));
            }
        }
        Situation.setText("");
        Background.setText("");
        Assesmen.setText("");
        Recommendation.setText("");
        CmbBaca.setSelectedItem("Sudah");
        CmbKonfirmasi.setSelectedItem("Sudah");
        setStatusLabel("Belum");
    }

    private void setStatusLabel(String validasi) {
        if (validasi != null && validasi.equalsIgnoreCase("Sudah")) {
            LStatus.setText("TERVERIFIKASI");
            LStatus.setBackground(new Color(200, 240, 200));
            LStatus.setForeground(new Color(20, 110, 20));
        } else {
            LStatus.setText("BELUM DIVALIDASI");
            LStatus.setBackground(new Color(250, 230, 200));
            LStatus.setForeground(new Color(150, 90, 0));
        }
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        if (noRawat.equals("")) {
            LCount.setText("0");
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select tgl_sbar,jam_sbar,nip,profesi,kd_dokter,situation,background,assesmen,recommendation,advis,"
                + "baca,konfirmasi,validasi,validator,tgl_validasi,jam_validasi from sbar_pasien "
                + "where no_rawat=? and status=? order by tgl_sbar,jam_sbar")) {
            ps.setString(1, noRawat);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tabMode.addRow(new Object[]{
                        rs.getString("tgl_sbar"), rs.getString("jam_sbar"), rs.getString("nip"),
                        namaPemeriksa(rs.getString("nip")), rs.getString("profesi"), namaDokter(rs.getString("kd_dokter")),
                        rs.getString("situation"), rs.getString("background"), rs.getString("assesmen"),
                        rs.getString("recommendation"), rs.getString("advis"),
                        rs.getString("baca"), rs.getString("konfirmasi"), rs.getString("validasi"),
                        rs.getString("validator"), tampilTgl(rs.getString("tgl_validasi")), tampilJam(rs.getString("jam_validasi"))
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tampil SBAR : " + e);
        }
        LCount.setText("" + tabMode.getRowCount());
    }

    private String tampilTgl(String t) {
        return (t == null || t.startsWith("0000")) ? "" : t;
    }

    private String tampilJam(String t) {
        return (t == null || t.equals("00:00:00")) ? "" : t;
    }

    // Resolusi nama pembuat/pemeriksa dari kode login (bisa dokter, petugas, atau pegawai).
    private String namaPemeriksa(String kode) {
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

    private String namaDokter(String kd) {
        if (kd == null || kd.trim().equals("")) {
            return "";
        }
        return Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", kd);
    }

    // Profesi/jabatan otomatis dari kode pembuat (dokter=spesialis, petugas/pegawai=jabatan).
    private String resolveProfesi(String kode) {
        if (kode == null || kode.trim().equals("")) {
            return "";
        }
        String p = Sequel.cariIsi("select trim(concat('Dokter ', ifnull((select nm_sps from spesialis where spesialis.kd_sps=dokter.kd_sps),''))) from dokter where dokter.kd_dokter=?", kode);
        if (p != null && !p.trim().equals("")) {
            return p.trim();
        }
        p = Sequel.cariIsi("select jabatan.nm_jbtn from petugas inner join jabatan on jabatan.kd_jbtn=petugas.kd_jbtn where petugas.nip=?", kode);
        if (p != null && !p.trim().equals("")) {
            return p.trim();
        }
        p = Sequel.cariIsi("select trim(concat(ifnull(jbtn,''), case when ifnull(departemen,'')='' then '' else concat(' / ', departemen) end)) from pegawai where nik=?", kode);
        return p == null ? "" : p.trim();
    }

    private void getData() {
        int r = tbSBAR.getSelectedRow();
        if (r == -1) {
            return;
        }
        ubah = true;
        tglLama = tbSBAR.getValueAt(r, 0).toString();
        jamLama = tbSBAR.getValueAt(r, 1).toString();
        NIP.setText(nilai(r, 2));
        NamaPemeriksa.setText(nilai(r, 3));
        Profesi.setText(nilai(r, 4));
        // DPJP: ambil kode dari DB karena kolom tabel menyimpan nama (bukan kode)
        KdDPJP.setText(Sequel.cariIsi("select kd_dokter from sbar_pasien where no_rawat='" + noRawat
                + "' and tgl_sbar='" + tglLama + "' and jam_sbar='" + jamLama + "'"));
        NamaDPJP.setText(nilai(r, 5));
        Situation.setText(nilai(r, 6));
        Background.setText(nilai(r, 7));
        Assesmen.setText(nilai(r, 8));
        Recommendation.setText(nilai(r, 9));
        CmbBaca.setSelectedItem(nilai(r, 11).equals("") ? "Belum" : nilai(r, 11));
        CmbKonfirmasi.setSelectedItem(nilai(r, 12).equals("") ? "Belum" : nilai(r, 12));
        setStatusLabel(nilai(r, 13));
    }

    private String nilai(int r, int c) {
        Object o = tbSBAR.getValueAt(r, c);
        return o == null ? "" : o.toString();
    }

    private void simpan() {
        if (noRawat.equals("")) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pasien belum dipilih.");
            return;
        }
        if (Situation.getText().trim().equals("") && Background.getText().trim().equals("")
                && Assesmen.getText().trim().equals("") && Recommendation.getText().trim().equals("")) {
            javax.swing.JOptionPane.showMessageDialog(this, "Minimal isi salah satu dari S/B/A/R.");
            return;
        }
        try {
            if (ubah) {
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "update sbar_pasien set nip=?,profesi=?,kd_dokter=?,situation=?,background=?,assesmen=?,"
                        + "recommendation=?,baca=?,konfirmasi=? where no_rawat=? and tgl_sbar=? and jam_sbar=?")) {
                    ps.setString(1, NIP.getText());
                    ps.setString(2, Profesi.getText());
                    ps.setString(3, KdDPJP.getText());
                    ps.setString(4, Situation.getText());
                    ps.setString(5, Background.getText());
                    ps.setString(6, Assesmen.getText());
                    ps.setString(7, Recommendation.getText());
                    ps.setString(8, CmbBaca.getSelectedItem().toString());
                    ps.setString(9, CmbKonfirmasi.getSelectedItem().toString());
                    ps.setString(10, noRawat);
                    ps.setString(11, tglLama);
                    ps.setString(12, jamLama);
                    ps.executeUpdate();
                }
            } else {
                Date now = new Date();
                String tgl = new SimpleDateFormat("yyyy-MM-dd").format(now);
                String jam = new SimpleDateFormat("HH:mm:ss").format(now);
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "insert into sbar_pasien(no_rawat,tgl_sbar,jam_sbar,nip,profesi,kd_dokter,situation,background,"
                        + "assesmen,recommendation,baca,konfirmasi,validasi,status) "
                        + "values(?,?,?,?,?,?,?,?,?,?,?,?, 'Belum', ?)")) {
                    ps.setString(1, noRawat);
                    ps.setString(2, tgl);
                    ps.setString(3, jam);
                    ps.setString(4, NIP.getText());
                    ps.setString(5, Profesi.getText());
                    ps.setString(6, KdDPJP.getText());
                    ps.setString(7, Situation.getText());
                    ps.setString(8, Background.getText());
                    ps.setString(9, Assesmen.getText());
                    ps.setString(10, Recommendation.getText());
                    ps.setString(11, CmbBaca.getSelectedItem().toString());
                    ps.setString(12, CmbKonfirmasi.getSelectedItem().toString());
                    ps.setString(13, status);
                    ps.executeUpdate();
                }
            }
            baru();
            tampil();
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal menyimpan SBAR.\n" + e.getMessage());
        }
    }

    private void hapus() {
        int r = tbSBAR.getSelectedRow();
        if (r == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pilih baris SBAR yang akan dihapus.");
            return;
        }
        if ("Sudah".equalsIgnoreCase(nilai(r, 13))) {
            javax.swing.JOptionPane.showMessageDialog(this, "SBAR sudah TERVERIFIKASI, tidak bisa dihapus.");
            return;
        }
        if (javax.swing.JOptionPane.showConfirmDialog(this, "Hapus SBAR ini?", "Konfirmasi",
                javax.swing.JOptionPane.YES_NO_OPTION) != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement(
                "delete from sbar_pasien where no_rawat=? and tgl_sbar=? and jam_sbar=?")) {
            ps.setString(1, noRawat);
            ps.setString(2, tbSBAR.getValueAt(r, 0).toString());
            ps.setString(3, tbSBAR.getValueAt(r, 1).toString());
            ps.executeUpdate();
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal menghapus.\n" + e.getMessage());
        }
        baru();
        tampil();
    }
}
