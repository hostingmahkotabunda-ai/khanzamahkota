package rekammedis;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Formulir Penandaan Lokasi Operasi -- Wanita (RM 30, blok 10 RM Operasi).
 * Diagram tubuh/kepala/tangan/kaki di {@link DiagramTubuhWanita} adalah hasil
 * crop gambar asli dari kertas RM 30 (gambar/penandaan1.png..6.png), lalu
 * dokter menandai lokasi operasi langsung dengan mouse (gambar bebas/freehand)
 * di atas tiap diagram lewat {@link PanelDiagramTanda}. Tanda disimpan sbg data vektor (teks, bukan
 * bitmap) supaya tetap bisa diedit ulang tiap dibuka lagi.
 */
public final class RMPenandaanLokasiOperasi extends JDialog {

    private static final Font FONT_FORM = new Font("Times New Roman", Font.PLAIN, 13);
    private static final Font FONT_FORM_BOLD = new Font("Times New Roman", Font.BOLD, 13);

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();

    private final widget.TextBox TNoRw = ro();
    private final widget.TextBox TNoRM = ro();
    private final widget.TextBox TPasien = ro();
    private final widget.TextBox TJK = ro();
    private final widget.TextBox TTglLahir = ro();
    private final widget.Tanggal dtpTanggal = dt();

    private final widget.TextBox TProsedur = tf();
    private final widget.Tanggal dtpTanggalProsedur = dt();

    private final PanelDiagramTanda panelBadan = new PanelDiagramTanda(DiagramTubuhWanita.Jenis.BADAN);
    private final PanelDiagramTanda panelKepalaProfil = new PanelDiagramTanda(DiagramTubuhWanita.Jenis.KEPALA_PROFIL);
    private final PanelDiagramTanda panelKepalaDepanBelakang = new PanelDiagramTanda(DiagramTubuhWanita.Jenis.KEPALA_DEPAN_BELAKANG);
    private final PanelDiagramTanda panelTanganPalmar = new PanelDiagramTanda(DiagramTubuhWanita.Jenis.TANGAN_PALMAR);
    private final PanelDiagramTanda panelTanganDorsal = new PanelDiagramTanda(DiagramTubuhWanita.Jenis.TANGAN_DORSAL);
    private final PanelDiagramTanda panelKaki = new PanelDiagramTanda(DiagramTubuhWanita.Jenis.KAKI);

    private final JCheckBox chkPernyataan = new JCheckBox(
            "Saya menyatakan bahwa lokasi yang telah ditetapkan pada diagram di atas adalah benar "
            + "dan telah dikonfirmasi bersama pasien/keluarga.");

    private final widget.ComboBox cmbDokter = new widget.ComboBox();
    private final JLabel lblFotoDokter = new JLabel();
    private final widget.Tanggal dtpTglTtdDokter = dt();

    private final widget.TextBox TNamaKonfirmasi = tf();
    private final widget.Tanggal dtpTglTtdPasien = dt();

    private final widget.Button BtnBaru = new widget.Button();
    private final widget.Button BtnSimpan = new widget.Button();
    private final widget.Button BtnHapus = new widget.Button();
    private final widget.Button BtnCetak = new widget.Button();
    private final widget.Button BtnKeluar = new widget.Button();

    public RMPenandaanLokasiOperasi(Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("::[ Formulir Penandaan Lokasi Operasi - Wanita (RM 30) ]::");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
        ensureTable();
        muatComboDokter();
        setSize(1150, 860);
        setMinimumSize(new Dimension(980, 680));
        setLocationRelativeTo(parent);
    }

    // ====================== UI ======================
    private void initComponents() {
        final Color latar = new Color(246, 249, 251);
        final Color garis = new Color(215, 224, 230);
        final Color teks = new Color(32, 49, 66);

        getContentPane().setBackground(latar);
        getContentPane().setLayout(new BorderLayout());

        JPanel atas = new JPanel(new BorderLayout(12, 10));
        atas.setBackground(latar);
        atas.setBorder(BorderFactory.createEmptyBorder(14, 18, 10, 18));
        JPanel blokJudul = new JPanel();
        blokJudul.setOpaque(false);
        blokJudul.setLayout(new BoxLayout(blokJudul, BoxLayout.Y_AXIS));
        JLabel judulUtama = new JLabel("Formulir Penandaan Lokasi Operasi");
        judulUtama.setFont(new Font("Times New Roman", Font.BOLD, 21));
        judulUtama.setForeground(teks);
        JLabel subjudul = new JLabel("Form RM 30  •  Wanita  •  klik + seret mouse di atas diagram untuk menandai lokasi");
        subjudul.setFont(FONT_FORM);
        subjudul.setForeground(new Color(92, 107, 119));
        blokJudul.add(judulUtama);
        blokJudul.add(Box.createVerticalStrut(3));
        blokJudul.add(subjudul);
        atas.add(blokJudul, BorderLayout.NORTH);

        JPanel ringkasanPasien = new JPanel(new GridLayout(1, 5, 0, 0));
        ringkasanPasien.setBackground(Color.WHITE);
        ringkasanPasien.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis), BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        ringkasanPasien.add(fieldRingkasan("No. Rawat *", TNoRw, true));
        ringkasanPasien.add(fieldRingkasan("No. RM", TNoRM, true));
        ringkasanPasien.add(fieldRingkasan("Nama Pasien", TPasien, true));
        ringkasanPasien.add(fieldRingkasan("Jenis Kelamin", TJK, true));
        ringkasanPasien.add(fieldRingkasan("Tanggal Lahir", TTglLahir, true));
        atas.add(ringkasanPasien, BorderLayout.CENTER);
        getContentPane().add(atas, BorderLayout.NORTH);

        JPanel isi = new JPanel();
        isi.setOpaque(false);
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));
        isi.setBorder(BorderFactory.createEmptyBorder(0, 18, 14, 18));

        isi.add(panelProsedur(garis, teks));
        isi.add(Box.createVerticalStrut(10));
        isi.add(panelDiagram(garis, teks));
        isi.add(Box.createVerticalStrut(10));
        isi.add(panelPernyataanTtd(garis, teks));

        JScrollPane scroll = new JScrollPane(isi);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getViewport().setBackground(latar);
        getContentPane().add(scroll, BorderLayout.CENTER);

        BtnBaru.setText("Baru");
        BtnSimpan.setText("Simpan Data");
        BtnHapus.setText("Hapus Data");
        BtnCetak.setText("Cetak");
        BtnKeluar.setText("Keluar");
        BtnBaru.addActionListener(e -> setNoRm(ambil(TNoRw)));
        BtnSimpan.addActionListener(e -> simpan());
        BtnHapus.addActionListener(e -> hapus());
        BtnCetak.addActionListener(e -> cetak());
        BtnKeluar.addActionListener(e -> dispose());
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 9));
        bawah.setBackground(Color.WHITE);
        bawah.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, garis));
        bawah.add(BtnHapus);
        bawah.add(BtnBaru);
        bawah.add(BtnCetak);
        bawah.add(BtnKeluar);
        bawah.add(BtnSimpan);
        getContentPane().add(bawah, BorderLayout.SOUTH);
    }

    private JPanel panelProsedur(Color garis, Color teks) {
        JPanel kartu = kartu(garis);
        kartu.setLayout(new GridBagLayout());
        int row = 0;
        GridBagConstraints g = gc(0, row, 1, 0.0);
        kartu.add(lbl("Tanggal Pengisian"), g);
        dtpTanggal.setPreferredSize(new Dimension(150, 25));
        kartu.add(dtpTanggal, gc(1, row, 1, 1.0));
        kartu.add(lbl("Prosedur/Tindakan"), gc(2, row, 1, 0.0));
        siz(TProsedur);
        kartu.add(TProsedur, gc(3, row, 1, 1.0));
        row++;
        kartu.add(lbl("Tanggal Prosedur"), gc(0, row, 1, 0.0));
        dtpTanggalProsedur.setPreferredSize(new Dimension(150, 25));
        kartu.add(dtpTanggalProsedur, gc(1, row, 1, 1.0));
        return kartu;
    }

    private JPanel panelDiagram(Color garis, Color teks) {
        JPanel kartu = kartu(garis);
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));
        JLabel judul = new JLabel("Diagram Penandaan Lokasi");
        judul.setFont(new Font("Times New Roman", Font.BOLD, 15));
        judul.setForeground(teks);
        judul.setAlignmentX(Component.LEFT_ALIGNMENT);
        kartu.add(judul);
        kartu.add(Box.createVerticalStrut(4));
        JLabel ket = new JLabel("Klik kiri lalu seret mouse di atas gambar untuk menggambar tanda (merah).");
        ket.setFont(new Font("Tahoma", Font.ITALIC, 11));
        ket.setForeground(new Color(120, 133, 143));
        ket.setAlignmentX(Component.LEFT_ALIGNMENT);
        kartu.add(ket);
        kartu.add(Box.createVerticalStrut(8));

        JPanel grid = new JPanel(new GridLayout(0, 3, 12, 12));
        grid.setOpaque(false);
        grid.add(diagramDenganTombol("Tubuh (Depan/Belakang)", panelBadan));
        grid.add(diagramDenganTombol("Kepala (Profil Kiri/Kanan)", panelKepalaProfil));
        grid.add(diagramDenganTombol("Kepala (Depan/Belakang)", panelKepalaDepanBelakang));
        grid.add(diagramDenganTombol("Tangan (Palmar)", panelTanganPalmar));
        grid.add(diagramDenganTombol("Tangan (Dorsal)", panelTanganDorsal));
        grid.add(diagramDenganTombol("Kaki", panelKaki));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        kartu.add(grid);
        return kartu;
    }

    private JPanel diagramDenganTombol(String judul, PanelDiagramTanda panel) {
        JPanel bungkus = new JPanel(new BorderLayout(0, 4));
        bungkus.setOpaque(false);
        JLabel lbl = new JLabel(judul);
        lbl.setFont(new Font("Tahoma", Font.BOLD, 11));
        bungkus.add(lbl, BorderLayout.NORTH);
        bungkus.add(panel, BorderLayout.CENTER);
        widget.Button btnUndo = new widget.Button();
        btnUndo.setText("Hapus Tanda Terakhir");
        btnUndo.addActionListener(e -> panel.hapusTerakhir());
        widget.Button btnClear = new widget.Button();
        btnClear.setText("Bersihkan");
        btnClear.addActionListener(e -> panel.bersihkan());
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        tombol.setOpaque(false);
        tombol.add(btnUndo);
        tombol.add(btnClear);
        bungkus.add(tombol, BorderLayout.SOUTH);
        return bungkus;
    }

    private JPanel panelPernyataanTtd(Color garis, Color teks) {
        JPanel kartu = kartu(garis);
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));
        chkPernyataan.setOpaque(false);
        chkPernyataan.setFont(FONT_FORM);
        chkPernyataan.setAlignmentX(Component.LEFT_ALIGNMENT);
        kartu.add(chkPernyataan);
        kartu.add(Box.createVerticalStrut(10));

        JPanel ttd = new JPanel(new GridBagLayout());
        ttd.setOpaque(false);
        ttd.setAlignmentX(Component.LEFT_ALIGNMENT);
        int row = 0;
        ttd.add(lbl("Ditandatangani Dokter"), gc(0, row, 1, 0.0));
        cmbDokter.setPreferredSize(new Dimension(220, 25));
        ttd.add(cmbDokter, gc(1, row, 1, 1.0));
        ttd.add(lblFotoDokter, gc(2, row, 1, 0.0));
        ttd.add(lbl("Tanggal"), gc(3, row, 1, 0.0));
        dtpTglTtdDokter.setPreferredSize(new Dimension(140, 25));
        ttd.add(dtpTglTtdDokter, gc(4, row, 1, 1.0));
        row++;
        ttd.add(lbl("Dikonfirmasi Pasien/Keluarga"), gc(0, row, 1, 0.0));
        siz(TNamaKonfirmasi);
        ttd.add(TNamaKonfirmasi, gc(1, row, 1, 1.0));
        ttd.add(lbl("Tanggal"), gc(3, row, 1, 0.0));
        dtpTglTtdPasien.setPreferredSize(new Dimension(140, 25));
        ttd.add(dtpTglTtdPasien, gc(4, row, 1, 1.0));
        kartu.add(ttd);
        return kartu;
    }

    /** Item combo "Ditandatangani Dokter" -- toString() dipakai spy ComboBox nampilin nama dokter, tapi kode aslinya tetap dipegang terpisah biar aman kalau ada nama kembar. */
    private static final class ItemDokter {
        final String kode;
        final String nama;

        ItemDokter(String kode, String nama) {
            this.kode = kode;
            this.nama = nama;
        }

        @Override
        public String toString() {
            return nama;
        }
    }

    /** Dropdown dokter (ganti dialog picker DlgCariDokter yg dinilai kebruatan/terlalu berat utk kebutuhan sekadar pilih 1 dokter). */
    private void muatComboDokter() {
        cmbDokter.addItem(new ItemDokter("", ""));
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select kd_dokter, nm_dokter from dokter where status='1' order by nm_dokter")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cmbDokter.addItem(new ItemDokter(nvl(rs.getString("kd_dokter")), nvl(rs.getString("nm_dokter"))));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat combo dokter RM30 : " + e);
        }
        cmbDokter.addActionListener(e -> {
            Object dipilih = cmbDokter.getSelectedItem();
            String nama = dipilih instanceof ItemDokter ? ((ItemDokter) dipilih).nama : "";
            lblFotoDokter.setIcon(nama.isEmpty() ? null : ambilFotoTtd(nama));
        });
    }

    /** Pilih item combo dgn kode dokter tersimpan (dipanggil saat memuat data yg sudah ada). */
    private void pilihDokterDenganKode(String kode) {
        for (int i = 0; i < cmbDokter.getItemCount(); i++) {
            ItemDokter item = (ItemDokter) cmbDokter.getItemAt(i);
            if (item.kode.equals(kode)) {
                cmbDokter.setSelectedIndex(i);
                return;
            }
        }
    }

    private ItemDokter dokterTerpilih() {
        Object v = cmbDokter.getSelectedItem();
        return v instanceof ItemDokter ? (ItemDokter) v : new ItemDokter("", "");
    }

    public void isCek() {
        boolean bisa = akses.getpenilaian_awal_keperawatan_ranap();
        BtnSimpan.setEnabled(bisa);
        BtnHapus.setEnabled(bisa);
    }

    // ====================== Muat / Simpan ======================
    public void setNoRm(String norawat) {
        kosongkan();
        if (norawat == null || norawat.trim().isEmpty()) {
            return;
        }
        TNoRw.setText(norawat);
        tarikDataPasien(norawat);
        muatDataJikaAda(norawat);
    }

    private void kosongkan() {
        TNoRw.setText(""); TNoRM.setText(""); TPasien.setText(""); TJK.setText(""); TTglLahir.setText("");
        dtpTanggal.setDate(new Date());
        TProsedur.setText("");
        dtpTanggalProsedur.setDate(new Date());
        panelBadan.bersihkan(); panelKepalaProfil.bersihkan(); panelKepalaDepanBelakang.bersihkan();
        panelTanganPalmar.bersihkan(); panelTanganDorsal.bersihkan(); panelKaki.bersihkan();
        chkPernyataan.setSelected(false);
        cmbDokter.setSelectedIndex(0); lblFotoDokter.setIcon(null);
        dtpTglTtdDokter.setDate(new Date());
        TNamaKonfirmasi.setText("");
        dtpTglTtdPasien.setDate(new Date());
    }

    private void tarikDataPasien(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "select p.no_rkm_medis,p.nm_pasien,p.jk,ifnull(date_format(p.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir "
                + "from reg_periksa rp inner join pasien p on rp.no_rkm_medis=p.no_rkm_medis where rp.no_rawat=?")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    TJK.setText("L".equalsIgnoreCase(rs.getString("jk")) ? "Laki-Laki" : "Perempuan");
                    TTglLahir.setText(rs.getString("tgl_lahir"));
                    TNamaKonfirmasi.setText(nvl(rs.getString("nm_pasien")));
                }
            }
        } catch (Exception e) {
            System.out.println("Notif tarik data pasien RM30 : " + e);
        }
    }

    private void muatDataJikaAda(String norawat) {
        try (PreparedStatement ps = koneksi.prepareStatement("select * from penandaan_lokasi_operasi where no_rawat=?")) {
            ps.setString(1, norawat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getDate("tanggal") != null) {
                        dtpTanggal.setDate(new Date(rs.getDate("tanggal").getTime()));
                    }
                    TProsedur.setText(nvl(rs.getString("prosedur")));
                    if (rs.getDate("tanggal_prosedur") != null) {
                        dtpTanggalProsedur.setDate(new Date(rs.getDate("tanggal_prosedur").getTime()));
                    }
                    panelBadan.muatDariTeks(nvl(rs.getString("diagram_badan")));
                    panelKepalaProfil.muatDariTeks(nvl(rs.getString("diagram_kepala_profil")));
                    panelKepalaDepanBelakang.muatDariTeks(nvl(rs.getString("diagram_kepala_depan_belakang")));
                    panelTanganPalmar.muatDariTeks(nvl(rs.getString("diagram_tangan_palmar")));
                    panelTanganDorsal.muatDariTeks(nvl(rs.getString("diagram_tangan_dorsal")));
                    panelKaki.muatDariTeks(nvl(rs.getString("diagram_kaki")));
                    chkPernyataan.setSelected("1".equals(rs.getString("pernyataan_benar")));
                    String kode = nvl(rs.getString("kd_dokter"));
                    if (!kode.isEmpty()) {
                        pilihDokterDenganKode(kode);
                        lblFotoDokter.setIcon(ambilFotoTtd(nvl(rs.getString("nama_dokter"))));
                    }
                    if (rs.getDate("tanggal_ttd_dokter") != null) {
                        dtpTglTtdDokter.setDate(new Date(rs.getDate("tanggal_ttd_dokter").getTime()));
                    }
                    String namaKonfirmasi = nvl(rs.getString("nama_konfirmasi"));
                    if (!namaKonfirmasi.isEmpty()) {
                        TNamaKonfirmasi.setText(namaKonfirmasi);
                    }
                    if (rs.getDate("tanggal_ttd_pasien") != null) {
                        dtpTglTtdPasien.setDate(new Date(rs.getDate("tanggal_ttd_pasien").getTime()));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notif muat RM30 : " + e);
        }
    }

    private void simpan() {
        if (ambil(TNoRw).equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        String sql = "insert into penandaan_lokasi_operasi "
                + "(no_rawat,tanggal,prosedur,tanggal_prosedur,diagram_badan,diagram_kepala_profil,"
                + "diagram_kepala_depan_belakang,diagram_tangan_palmar,diagram_tangan_dorsal,diagram_kaki,"
                + "pernyataan_benar,kd_dokter,nama_dokter,tanggal_ttd_dokter,nama_konfirmasi,tanggal_ttd_pasien,"
                + "created_by,created_at) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,now()) "
                + "on duplicate key update tanggal=values(tanggal),prosedur=values(prosedur),"
                + "tanggal_prosedur=values(tanggal_prosedur),diagram_badan=values(diagram_badan),"
                + "diagram_kepala_profil=values(diagram_kepala_profil),"
                + "diagram_kepala_depan_belakang=values(diagram_kepala_depan_belakang),"
                + "diagram_tangan_palmar=values(diagram_tangan_palmar),diagram_tangan_dorsal=values(diagram_tangan_dorsal),"
                + "diagram_kaki=values(diagram_kaki),pernyataan_benar=values(pernyataan_benar),"
                + "kd_dokter=values(kd_dokter),nama_dokter=values(nama_dokter),"
                + "tanggal_ttd_dokter=values(tanggal_ttd_dokter),nama_konfirmasi=values(nama_konfirmasi),"
                + "tanggal_ttd_pasien=values(tanggal_ttd_pasien),updated_by=values(created_by),updated_at=now()";
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, ambil(TNoRw));
            ps.setString(idx++, ambilTgl(dtpTanggal));
            ps.setString(idx++, ambil(TProsedur));
            ps.setString(idx++, ambilTgl(dtpTanggalProsedur));
            ps.setString(idx++, panelBadan.simpanKeTeks());
            ps.setString(idx++, panelKepalaProfil.simpanKeTeks());
            ps.setString(idx++, panelKepalaDepanBelakang.simpanKeTeks());
            ps.setString(idx++, panelTanganPalmar.simpanKeTeks());
            ps.setString(idx++, panelTanganDorsal.simpanKeTeks());
            ps.setString(idx++, panelKaki.simpanKeTeks());
            ps.setString(idx++, chkPernyataan.isSelected() ? "1" : "0");
            ItemDokter dokter = dokterTerpilih();
            ps.setString(idx++, dokter.kode);
            ps.setString(idx++, dokter.nama);
            ps.setString(idx++, ambilTgl(dtpTglTtdDokter));
            ps.setString(idx++, ambil(TNamaKonfirmasi));
            ps.setString(idx++, ambilTgl(dtpTglTtdPasien));
            ps.setString(idx++, akses.getkode());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Penandaan Lokasi Operasi tersimpan.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan.\n" + e.getMessage());
        }
    }

    private void hapus() {
        if (ambil(TNoRw).equals("")) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus penandaan lokasi operasi untuk No.Rawat " + ambil(TNoRw) + " ?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try (PreparedStatement ps = koneksi.prepareStatement("delete from penandaan_lokasi_operasi where no_rawat=?")) {
            ps.setString(1, ambil(TNoRw));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data dihapus.");
            setNoRm(ambil(TNoRw));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus.\n" + e.getMessage());
        }
    }

    public void cetak() {
        if (ambil(TNoRw).equals("")) {
            JOptionPane.showMessageDialog(this, "Pilih pasien terlebih dahulu.");
            return;
        }
        if (Sequel.cariInteger("select count(*) from penandaan_lokasi_operasi where no_rawat=?", ambil(TNoRw)) == 0) {
            JOptionPane.showMessageDialog(this, "Simpan data terlebih dahulu sebelum mencetak.");
            return;
        }
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            param.put("url_penggajian", "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/"
                    + koneksiDB.HYBRIDWEB() + "/penggajian/");
            String sql = "select a.*,pasien.no_rkm_medis,pasien.nm_pasien,"
                    + "if(pasien.jk='L','Laki-laki','Perempuan') as jk,"
                    + "ifnull(date_format(pasien.tgl_lahir,'%d-%m-%Y'),'') as tgl_lahir,"
                    + "ifnull(date_format(a.tanggal,'%d-%m-%Y'),'') as tanggal_cetak,"
                    + "ifnull(date_format(a.tanggal_prosedur,'%d-%m-%Y'),'') as tanggal_prosedur_cetak,"
                    + "ifnull(date_format(a.tanggal_ttd_dokter,'%d-%m-%Y'),'') as tanggal_ttd_dokter_cetak,"
                    + "ifnull(date_format(a.tanggal_ttd_pasien,'%d-%m-%Y'),'') as tanggal_ttd_pasien_cetak,"
                    + fotoSql("a.nama_dokter", "dokter_photo") + " "
                    + "from penandaan_lokasi_operasi a "
                    + "inner join reg_periksa on a.no_rawat=reg_periksa.no_rawat "
                    + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "where a.no_rawat='" + ambil(TNoRw) + "'";
            Valid.MyReportqry("rptPenandaanLokasiOperasiWanita.jasper", "report",
                    "::[ Formulir Penandaan Lokasi Operasi - Wanita ]::", sql, param);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak.\n" + e.getMessage());
        }
    }

    private String fotoSql(String kolomNama, String alias) {
        String sub = "(select p2.photo from pegawai p2 where lower(trim(p2.nama))=lower(trim(" + kolomNama + ")) limit 1)";
        return "if(coalesce(nullif(" + sub + ",''),'')='' or coalesce(nullif(" + sub + ",''),'')='-' "
                + "or coalesce(nullif(" + sub + ",''),'')='pages/pegawai/photo/','',"
                + "replace(coalesce(" + sub + ",''),'\\\\\\\\','/')) as " + alias;
    }

    private void ensureTable() {
        Sequel.queryu2(
                "create table if not exists penandaan_lokasi_operasi ("
                + "no_rawat varchar(17) not null primary key,"
                + "tanggal date null,"
                + "prosedur varchar(200) null,"
                + "tanggal_prosedur date null,"
                + "diagram_badan text null,"
                + "diagram_kepala_profil text null,"
                + "diagram_kepala_depan_belakang text null,"
                + "diagram_tangan_palmar text null,"
                + "diagram_tangan_dorsal text null,"
                + "diagram_kaki text null,"
                + "pernyataan_benar varchar(1) null,"
                + "kd_dokter varchar(20) null,"
                + "nama_dokter varchar(60) null,"
                + "tanggal_ttd_dokter date null,"
                + "nama_konfirmasi varchar(60) null,"
                + "tanggal_ttd_pasien date null,"
                + "created_by varchar(50) null,"
                + "updated_by varchar(50) null,"
                + "created_at datetime null,"
                + "updated_at datetime null"
                + ") ROW_FORMAT=DYNAMIC");
    }

    private ImageIcon ambilFotoTtd(String nama) {
        if (nama == null || nama.trim().isEmpty()) {
            return null;
        }
        try {
            String photo = bersihkanPathFotoTtd(Sequel.cariIsi(
                    "select photo from pegawai where lower(trim(nama))=lower(trim(?)) limit 1", nama));
            if (!photo.isEmpty()) {
                String urlPenggajian = "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/"
                        + koneksiDB.HYBRIDWEB() + "/penggajian/";
                Image gambar = CetakCPPT.ambilGambarServer(urlPenggajian + photo);
                if (gambar != null) {
                    return new ImageIcon(gambar.getScaledInstance(-1, 28, Image.SCALE_SMOOTH));
                }
            }
        } catch (Exception ignore) { }
        return null;
    }

    private static String bersihkanPathFotoTtd(String photo) {
        if (photo == null) {
            return "";
        }
        String p = photo.trim();
        if (p.equals("") || p.equals("-") || p.equals("pages/pegawai/photo/")) {
            return "";
        }
        return p.replace("\\", "/");
    }

    // ====================== Helpers UI ======================
    private JPanel kartu(Color garis) {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(garis), BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private JPanel fieldRingkasan(String label, widget.TextBox field, boolean readOnly) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Tahoma", Font.PLAIN, 10));
        l.setForeground(new Color(120, 133, 143));
        p.add(l, BorderLayout.NORTH);
        field.setFont(FONT_FORM_BOLD);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t + " :");
        l.setFont(new Font("Tahoma", Font.PLAIN, 11));
        return l;
    }

    private void siz(Component c) {
        c.setPreferredSize(new Dimension(220, 23));
    }

    private GridBagConstraints gc(int x, int y, int w, double weightx) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x; g.gridy = y; g.gridwidth = w;
        g.weightx = weightx;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(3, 3, 3, 3);
        g.anchor = GridBagConstraints.WEST;
        return g;
    }

    private static widget.TextBox tf() {
        widget.TextBox t = new widget.TextBox();
        t.setFont(FONT_FORM);
        return t;
    }

    private static widget.TextBox ro() {
        widget.TextBox t = new widget.TextBox();
        t.setFont(FONT_FORM);
        t.setEditable(false);
        return t;
    }

    private static widget.Tanggal dt() {
        widget.Tanggal d = new widget.Tanggal();
        d.setDisplayFormat("dd-MM-yyyy");
        return d;
    }

    /** "yyyy-MM-dd" siap simpan ke kolom DATE (lihat catatan sama di RMSuratKeteranganLahir.ambilTgl). */
    private static String ambilTgl(widget.Tanggal d) {
        Date x = d.getDate();
        return x == null ? null : new java.text.SimpleDateFormat("yyyy-MM-dd").format(x);
    }

    private static String ambil(widget.TextBox t) {
        String s = t.getText();
        return s == null ? "" : s.trim();
    }

    private static String nvl(String v) {
        return v == null ? "" : v;
    }
}
