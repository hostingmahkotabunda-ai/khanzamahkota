-- =====================================================================
-- Tabel Template Catatan ADIME Gizi (RMCatatanADIMEGizi)
-- Satu baris = satu template berisi teks untuk field Asesmen, Monitoring,
-- Evaluasi, dan Instruksi.
--
-- CATATAN PENTING: field Diagnosis dan Intervensi SENGAJA tidak masuk
-- template karena (khususnya Diagnosis) datanya tertarik dari halaman
-- sebelumnya; bila ikut template, data tarikan itu akan tertimpa.
--
-- Dipakai oleh:
--   - MasterCariTemplateADIMEGizi  (dialog pencari/pemilih template)
--   - MasterTemplateADIMEGizi      (master tambah/ubah/hapus template)
--   - tombol "Template" pada toolbar RMCatatanADIMEGizi
--
-- Catatan: aplikasi otomatis membuat tabel ini (CREATE TABLE IF NOT
-- EXISTS) dan mengisi data default saat masih kosong. Migrasi otomatis:
--   * tabel skema lama 6-field (punya kolom diagnosis) -> di-drop & dibuat
--     ulang.
--   * tabel skema 3-field (belum punya kolom instruksi) -> ditambah kolom
--     instruksi tanpa menghapus data.
-- Script ini referensi / untuk dijalankan manual.
-- =====================================================================

CREATE TABLE IF NOT EXISTS `template_adime_gizi` (  
  `no_template` varchar(15) NOT NULL,
  `nama_template` varchar(150) DEFAULT NULL,
  `asesmen` text,
  `monitoring` text,
  `evaluasi` text,
  `instruksi` text,
  PRIMARY KEY (`no_template`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Bila tabel sudah terlanjur dibuat tanpa kolom instruksi, jalankan:
-- ALTER TABLE `template_adime_gizi` ADD COLUMN `instruksi` text;

-- Data default (jalankan hanya bila tabel masih kosong) --------------
INSERT INTO `template_adime_gizi` (`no_template`,`nama_template`,`asesmen`,`monitoring`,`evaluasi`,`instruksi`) VALUES
('G0001','Pasien Umum / KU Baik',
 'Asupan makan baik. Keadaan umum baik, kesadaran composmentis. Antropometri dalam batas normal. Hasil laboratorium dalam batas normal.',
 'Monitoring asupan makan harian, berat badan, dan hasil laboratorium.',
 'Asupan makan tercapai minimal 80% dari kebutuhan. Status gizi dipertahankan baik.',
 'Lanjutkan diet. Kolaborasi dengan DPJP dan perawat untuk pemantauan asupan.'),
('G0002','Asupan Kurang / TKTP',
 'Asupan makan kurang dari kebutuhan (<80%). Keadaan umum lemah. Nafsu makan menurun. Penurunan berat badan.',
 'Monitoring asupan makan setiap hari, berat badan, dan keluhan saluran cerna.',
 'Asupan makan meningkat bertahap menuju target kebutuhan. Berat badan dipertahankan/meningkat.',
 'Lanjutkan diet TKTP, evaluasi asupan tiap hari. Kolaborasi DPJP bila asupan tetap rendah.'),
('G0003','Pasien Diabetes Mellitus',
 'Riwayat Diabetes Mellitus. Kadar gula darah tinggi. Keadaan umum baik, nafsu makan baik.',
 'Monitoring asupan makan, kadar gula darah (GDS), dan berat badan.',
 'Kadar gula darah terkontrol. Asupan sesuai diet DM. Status gizi dipertahankan.',
 'Lanjutkan diet DM. Kolaborasi DPJP untuk pengaturan terapi dan pemantauan gula darah.');
