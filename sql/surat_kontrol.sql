-- =====================================================================
-- Tabel: surat_kontrol  (form SuratKontrolV2)
-- Dibuka dari tombol "Surat Kontrol" di Daftar Pasien Rawat Jalan.
-- 1 baris per no_rawat (REPLACE INTO).
-- =====================================================================
CREATE TABLE IF NOT EXISTS `surat_kontrol` (
  `no_rawat`              VARCHAR(17) NOT NULL,
  `tanggal_surat`         DATE        DEFAULT NULL,
  `diagnosa`              TEXT,
  `terapi`                TEXT,
  `jadwal_kontrol`        DATE        DEFAULT NULL,
  `rencana_tindak_lanjut` TEXT,
  `kd_dokter`             VARCHAR(20) DEFAULT NULL,
  `nik`                   VARCHAR(20) DEFAULT NULL,
  PRIMARY KEY (`no_rawat`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
