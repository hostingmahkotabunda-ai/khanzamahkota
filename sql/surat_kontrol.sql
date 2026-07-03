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
false	2026-05-25T11:35:33+07:00	2026/05/25/000003	000001	PASIEN PERCOBAAN	-	dr.sarah	dr. Sarah Novita Rahayu	6202064211970001	IGDK	IGD Rawat INAP	66689542-4f55-4b32-adc4-2605e62d3ab8	Sudah	Ralan	2026-05-25T13:24:11+07:00	