-- =====================================================================
-- Tabel Template Asuhan Gizi (RMDataAsuhanGizi)
-- Logika mengikuti template_laporan_operasi: satu baris = satu template
-- berisi teks untuk field Fisik/Klinis, Intervensi Gizi, dan
-- Monitoring & Evaluasi sekaligus.
--
-- Dipakai oleh:
--   - MasterCariTemplateAsuhanGizi  (dialog pencari/pemilih template)
--   - MasterTemplateAsuhanGizi      (master tambah/ubah/hapus template)
--   - tombol "Template" pada toolbar RMDataAsuhanGizi
--
-- Catatan: aplikasi otomatis membuat tabel ini (CREATE TABLE IF NOT
-- EXISTS) dan mengisi data default saat masih kosong ketika dialog
-- pencari template pertama kali dibuka. Bila terdeteksi tabel lama
-- dengan skema berbeda (tanpa kolom nama_template), tabel di-drop lalu
-- dibuat ulang. Script ini disertakan sebagai referensi / untuk dijalankan
-- manual.
-- =====================================================================

CREATE TABLE IF NOT EXISTS `template_asuhan_gizi` (
  `no_template` varchar(15) NOT NULL,
  `nama_template` varchar(150) DEFAULT NULL,
  `fisik_klinis` text,
  `intervensi_gizi` text,
  `monitoring_evaluasi` text,
  PRIMARY KEY (`no_template`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data default (jalankan hanya bila tabel masih kosong) --------------
INSERT INTO `template_asuhan_gizi` (`no_template`,`nama_template`,`fisik_klinis`,`intervensi_gizi`,`monitoring_evaluasi`) VALUES
('G0001','Pasien Umum / KU Baik',
 'Keadaan umum baik, kesadaran composmentis. Tanda-tanda vital dalam batas normal. Tidak ada mual, muntah, maupun diare. Nafsu makan baik.',
 'Pemberian diet biasa bentuk makanan biasa melalui rute oral. Energi dan protein sesuai kebutuhan. Edukasi gizi kepada pasien dan keluarga mengenai diet yang dianjurkan.',
 'Monitoring asupan makan harian (target minimal 80% dari kebutuhan), berat badan, dan hasil laboratorium. Evaluasi setiap hari dan sesuaikan intervensi sesuai perkembangan klinis.');

