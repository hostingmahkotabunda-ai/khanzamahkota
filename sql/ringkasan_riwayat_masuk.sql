-- Tabel Ringkasan Riwayat Masuk dan Keluar Rumah Sakit (RM 2a)
-- 1 baris per no_rawat (REPLACE INTO dari form RMRingkasanRiwayatMasuk).
-- Catatan: form juga membuat tabel ini otomatis (ensureTable()), jadi
-- menjalankan skrip ini bersifat opsional / untuk membuat manual.

CREATE TABLE IF NOT EXISTS ringkasan_riwayat_masuk (
    no_rawat         VARCHAR(17) NOT NULL PRIMARY KEY,
    no_ktp           VARCHAR(20)  NULL,
    no_asuransi      VARCHAR(25)  NULL,
    perkawinan       VARCHAR(20)  NULL,
    petugas_tpp      VARCHAR(60)  NULL,
    suku_bangsa      VARCHAR(30)  NULL,
    suku_lainnya     VARCHAR(60)  NULL,
    cara_masuk       VARCHAR(30)  NULL,
    agama            VARCHAR(20)  NULL,
    gol_darah        VARCHAR(5)   NULL,
    pendidikan       VARCHAR(30)  NULL,
    alamat_lengkap   TEXT         NULL,
    no_telpon        VARCHAR(40)  NULL,
    pekerjaan        VARCHAR(30)  NULL,
    pekerjaan_asli   VARCHAR(60)  NULL,
    verifikasi_oleh  VARCHAR(60)  NULL,
    riwayat_ke       INT          NULL,
    ruangan_unit     VARCHAR(60)  NULL,
    kelas            VARCHAR(20)  NULL,
    diagnosa_masuk   TEXT         NULL,
    kode_diagnosa    VARCHAR(20)  NULL,
    perawat_ruangan  VARCHAR(60)  NULL,
    dokter_merawat   VARCHAR(60)  NULL,
    created_by       VARCHAR(50)  NULL,
    updated_by       VARCHAR(50)  NULL,
    created_at       DATETIME     NULL,
    updated_at       DATETIME     NULL
);
