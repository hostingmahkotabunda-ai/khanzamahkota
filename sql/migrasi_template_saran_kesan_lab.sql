-- Mahkota Bunda: tinjau dan jalankan MANUAL setelah persetujuan.
-- Tabel master template Kesan & Saran untuk Data Pemeriksaan Laboratorium Patologi Klinis.
CREATE TABLE IF NOT EXISTS template_saran_kesan_lab (
    no_template VARCHAR(10) NOT NULL,
    nama_template VARCHAR(100) NOT NULL,
    kesan TEXT,
    saran TEXT,
    PRIMARY KEY (no_template)
) ENGINE=InnoDB;
