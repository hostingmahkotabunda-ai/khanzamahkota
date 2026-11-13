-- Mahkota Bunda: tinjau dan jalankan MANUAL setelah persetujuan.
-- Form CP (Clinical Pathway) Anak - Bronkopneumonia.
-- Header: 1 baris per rawatan (no_rawat). Detail: grid harian dinamis (bisa >3 hari, makanya baris bukan kolom).

CREATE TABLE IF NOT EXISTS cp_bronkopneumonia (
    no_rawat VARCHAR(17) NOT NULL,
    diagnosis_awal VARCHAR(100) DEFAULT '',
    kode_icd10 VARCHAR(20) DEFAULT '',
    rencana_rawat VARCHAR(20) DEFAULT '5 hari',
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

-- Kalau tabel di atas sudah lebih dulu kebuat sebelum kolom kode_* ditambahkan (CREATE TABLE IF NOT
-- EXISTS di atas jadi tidak akan menambah kolom ke tabel yang sudah ada), tambahkan manual via ALTER:
ALTER TABLE cp_bronkopneumonia ADD COLUMN IF NOT EXISTS kode_perawat_ppjp VARCHAR(20) DEFAULT '' AFTER nama_perawat_ppjp;
ALTER TABLE cp_bronkopneumonia ADD COLUMN IF NOT EXISTS kode_dpjp VARCHAR(20) DEFAULT '' AFTER nama_dpjp;
ALTER TABLE cp_bronkopneumonia ADD COLUMN IF NOT EXISTS kode_verifikator VARCHAR(20) DEFAULT '' AFTER nama_verifikator;
ALTER TABLE cp_bronkopneumonia ADD COLUMN IF NOT EXISTS ruang_rawat VARCHAR(150) DEFAULT '' AFTER tinggi_badan;
ALTER TABLE cp_bronkopneumonia ADD COLUMN IF NOT EXISTS tgl_masuk_tampil VARCHAR(20) DEFAULT '' AFTER ruang_rawat;
ALTER TABLE cp_bronkopneumonia ADD COLUMN IF NOT EXISTS hari_rawat_ke VARCHAR(10) DEFAULT '' AFTER tgl_masuk_tampil;

CREATE TABLE IF NOT EXISTS cp_bronkopneumonia_detail (
    no_rawat VARCHAR(17) NOT NULL,
    item_kode VARCHAR(40) NOT NULL,
    hari_ke INT NOT NULL,
    nilai VARCHAR(5) NOT NULL DEFAULT '',
    PRIMARY KEY (no_rawat, item_kode, hari_ke)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC;
