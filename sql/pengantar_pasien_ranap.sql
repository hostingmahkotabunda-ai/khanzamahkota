-- Tabel Pengantar Pasien Rawat Inap (RM 3a)
-- 1 baris per no_rawat (REPLACE INTO dari form RMPengantarPasienRanap).
-- Catatan: form juga membuat tabel ini otomatis (ensureTable()), jadi
-- menjalankan skrip ini bersifat opsional / untuk membuat manual.

CREATE TABLE IF NOT EXISTS pengantar_pasien_ranap (
    no_rawat             VARCHAR(17) NOT NULL PRIMARY KEY,
    ruangan_tujuan       VARCHAR(60)  NULL,
    tanggal              DATE         NULL,
    jam                  VARCHAR(8)   NULL,
    wali                 VARCHAR(60)  NULL,
    td                   VARCHAR(20)  NULL,
    nadi                 VARCHAR(20)  NULL,
    suhu                 VARCHAR(10)  NULL,
    frekuensi_nafas      VARCHAR(20)  NULL,
    skor_nyeri_ada       VARCHAR(10)  NULL,
    skala_nyeri          VARCHAR(5)   NULL,
    bb                   VARCHAR(20)  NULL,
    tb                   VARCHAR(20)  NULL,
    lingkar_kepala       VARCHAR(20)  NULL,
    alat_bantu           VARCHAR(100) NULL,
    prothesa             VARCHAR(100) NULL,
    cacat_tubuh          VARCHAR(100) NULL,
    adl                  VARCHAR(20)  NULL,
    resiko_jatuh         VARCHAR(20)  NULL,
    score_jatuh          VARCHAR(20)  NULL,
    perawat_penulis      VARCHAR(60)  NULL,
    riwayat_penyakit     TEXT         NULL,
    pemeriksaan_jasmani  TEXT         NULL,
    laboratorium         TEXT         NULL,
    diagnosa             TEXT         NULL,
    usul_pengobatan      TEXT         NULL,
    asal_pengobatan      VARCHAR(20)  NULL,
    pengobatan_diberikan TEXT         NULL,
    dokter_penulis       VARCHAR(60)  NULL,
    created_by           VARCHAR(50)  NULL,
    updated_by           VARCHAR(50)  NULL,
    created_at           DATETIME     NULL,
    updated_at           DATETIME     NULL
);
