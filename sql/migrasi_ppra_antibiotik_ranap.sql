-- Mahkota Bunda: tinjau dan jalankan MANUAL setelah persetujuan.
-- Form input antibiotik per pasien rawat inap untuk PPRA/DDD (dipanggil dari menu
-- sidebar "FormMenu" di DlgRawatInap -> RMPPRAAntibiotik.java).
-- Beda dengan migrasi_ppra_ddd_antimikroba.sql (itu data HISTORIS hasil migrasi Excel,
-- tidak disentuh form ini). ppra_referensi_ddd (282 baris WHO ATC/DDD) DIPAKAI ULANG
-- sebagai master lookup Nama Antibiotik -> Kode ATC -> Standar DDD.
--
-- Header: 1 baris per antibiotik yang diberikan ke pasien selama 1 rawatan (no_rawat
-- bisa punya banyak baris kalau gonta-ganti/tambah antibiotik). Detail: log tanggal
-- pemberian, 1 baris per HARI (bukan per kali pemberian -- lihat catatan di
-- RMPPRAAntibiotik.java), unique (antibiotik_id, tanggal) supaya tidak bisa dobel.
--
-- Kolom identitas pasien (nama/diagnosis) SENGAJA tidak disimpan di sini -- cukup
-- no_rawat sebagai kunci, identitas ditarik langsung dari reg_periksa/pasien/diagnosa_pasien
-- saat form dibuka (lihat catatan feedback user di memory migrasi-ppra-ddd-antimikroba).

CREATE TABLE IF NOT EXISTS ppra_antibiotik_ranap (
    id INT NOT NULL AUTO_INCREMENT,
    no_rawat VARCHAR(17) NOT NULL,
    nama_antimikroba VARCHAR(150) NOT NULL,
    kode_atc VARCHAR(20) DEFAULT '',
    standar_ddd_gram DECIMAL(10,4) DEFAULT 0,
    dosis_per_pemberian_mg DECIMAL(10,2) DEFAULT 0,
    interval_jam INT DEFAULT 24,
    dosis_per_hari_mg DECIMAL(10,2) DEFAULT 0,
    rute VARCHAR(10) DEFAULT '',
    lama_terapi_hari INT DEFAULT 0,
    total_dosis_gram DECIMAL(12,4) DEFAULT 0,
    ddd_dihitung DECIMAL(12,4) DEFAULT 0,
    created_by VARCHAR(50) DEFAULT '',
    created_at DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_no_rawat (no_rawat)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ppra_antibiotik_ranap_tanggal (
    id INT NOT NULL AUTO_INCREMENT,
    antibiotik_id INT NOT NULL,
    tanggal DATE NOT NULL,
    created_by VARCHAR(50) DEFAULT '',
    created_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_antibiotik_tanggal (antibiotik_id, tanggal),
    KEY idx_antibiotik_id (antibiotik_id)
) ENGINE=InnoDB;
