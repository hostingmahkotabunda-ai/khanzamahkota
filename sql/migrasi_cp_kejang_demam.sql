-- Mahkota Bunda: tinjau dan jalankan MANUAL setelah persetujuan.
-- Form CP (Clinical Pathway) Anak - Kejang Demam.
-- Header: 1 baris per rawatan (no_rawat). Detail: grid harian dinamis (bisa >3 hari, makanya baris bukan kolom).
-- Struktur sama persis dgn migrasi_cp_bronkopneumonia.sql (CP pertama/pola acuan), bedanya kolom
-- "nilai" di tabel detail dilebarkan ke VARCHAR(20) krn baris Nutrisi butuh isian angka bebas,
-- bukan cuma tanda +/- (1-2 karakter).

CREATE TABLE IF NOT EXISTS cp_kejang_demam (
    no_rawat VARCHAR(17) NOT NULL,
    diagnosis_awal VARCHAR(100) DEFAULT '',
    kode_icd10 VARCHAR(20) DEFAULT '',
    rencana_rawat VARCHAR(20) DEFAULT '3 hari',
    berat_badan VARCHAR(10) DEFAULT '',
    tinggi_badan VARCHAR(10) DEFAULT '',
    ruang_rawat VARCHAR(150) DEFAULT '',
    tgl_masuk_tampil VARCHAR(20) DEFAULT '',
    hari_rawat_ke VARCHAR(10) DEFAULT '',
    jumlah_hari_tampil INT NOT NULL DEFAULT 3,
    varians TEXT,
    jumlah_biaya VARCHAR(30) DEFAULT '',
    diag_akhir_utama VARCHAR(100) DEFAULT '',
    icd10_akhir_utama VARCHAR(20) DEFAULT '',
    tindakan_utama VARCHAR(100) DEFAULT '',
    icd9_utama VARCHAR(20) DEFAULT '',
    diag_akhir_penyerta VARCHAR(100) DEFAULT '',
    icd10_akhir_penyerta VARCHAR(20) DEFAULT '',
    tindakan_penyerta VARCHAR(100) DEFAULT '',
    icd9_penyerta VARCHAR(20) DEFAULT '',
    diag_akhir_komplikasi VARCHAR(100) DEFAULT '',
    icd10_akhir_komplikasi VARCHAR(20) DEFAULT '',
    tindakan_komplikasi VARCHAR(100) DEFAULT '',
    icd9_komplikasi VARCHAR(20) DEFAULT '',
    nama_perawat_ppjp VARCHAR(100) DEFAULT '',
    kode_perawat_ppjp VARCHAR(20) DEFAULT '',
    nama_dpjp VARCHAR(100) DEFAULT '',
    kode_dpjp VARCHAR(20) DEFAULT '',
    nama_verifikator VARCHAR(100) DEFAULT '',
    kode_verifikator VARCHAR(20) DEFAULT '',
    created_by VARCHAR(50) DEFAULT '',
    created_at DATETIME NULL,
    updated_by VARCHAR(50) DEFAULT '',
    updated_at DATETIME NULL,
    PRIMARY KEY (no_rawat)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cp_kejang_demam_detail (
    no_rawat VARCHAR(17) NOT NULL,
    item_kode VARCHAR(40) NOT NULL,
    hari_ke INT NOT NULL,
    nilai VARCHAR(20) NOT NULL DEFAULT '',
    PRIMARY KEY (no_rawat, item_kode, hari_ke)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC;
