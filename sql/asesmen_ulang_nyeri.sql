-- Tabel Asesmen Ulang Nyeri (RM 7.1)
-- BEDA dari resume/ringkasan lain: BUKAN 1 baris per no_rawat, tapi catatan
-- berulang -- setiap kali kaji ulang nyeri dilakukan, disimpan sebagai baris
-- baru (id auto_increment). Diisi dari form RMAsesmenUlangNyeri.
-- Catatan: form juga membuat tabel ini otomatis (ensureTable()), jadi
-- menjalankan skrip ini bersifat opsional / untuk membuat manual.
--
-- perawat_vital_paraf / perawat_intervensi_paraf: kolom lama (paraf manual),
-- sudah tidak dipakai form (dibiarkan ada, tidak di-drop). Diganti dengan
-- perawat_vital_nip / perawat_intervensi_nip -- form sekarang memilih
-- petugas via DlgCariPetugas (default: user login) dan menyimpan NIP-nya,
-- lalu tabel riwayat menarik foto TTD dari pegawai.photo berdasarkan NIP itu.

CREATE TABLE IF NOT EXISTS asesmen_ulang_nyeri (
    id                          INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    no_rawat                    VARCHAR(17)  NOT NULL,
    tgl_jam_vital               DATETIME     NULL,
    skor_nyeri                  VARCHAR(5)   NULL,
    skor_sedasi                 VARCHAR(5)   NULL,
    tekanan_darah                VARCHAR(20)  NULL,
    nadi                        VARCHAR(20)  NULL,
    suhu                        VARCHAR(10)  NULL,
    pernafasan                  VARCHAR(20)  NULL,
    perawat_vital_nama          VARCHAR(60)  NULL,
    perawat_vital_paraf         VARCHAR(30)  NULL,
    perawat_vital_nip           VARCHAR(20)  NULL,
    tgl_jam_intervensi          DATETIME     NULL,
    nama_obat                   VARCHAR(100) NULL,
    dosis_frekuensi             VARCHAR(60)  NULL,
    rute                        VARCHAR(40)  NULL,
    intervensi_non_farmakologi  VARCHAR(150) NULL,
    perawat_intervensi_nama     VARCHAR(60)  NULL,
    perawat_intervensi_paraf    VARCHAR(30)  NULL,
    perawat_intervensi_nip      VARCHAR(20)  NULL,
    waktu_kaji_ulang            VARCHAR(60)  NULL,
    created_by                  VARCHAR(50)  NULL,
    created_at                  DATETIME     NULL,
    INDEX idx_no_rawat (no_rawat)
);
