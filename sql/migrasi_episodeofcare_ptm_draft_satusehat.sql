-- Jalankan manual setelah backup dan persetujuan. Menyimpan draf lokal, tidak mengirim API.
CREATE TABLE IF NOT EXISTS satu_sehat_episode_ptm_draft (
  no_rawat VARCHAR(17) NOT NULL,
  no_rkm_medis VARCHAR(15) NOT NULL,
  kd_penyakit VARCHAR(10) NOT NULL,
  id_condition VARCHAR(64) NOT NULL DEFAULT '',
  tipe ENUM('CAD','CVD','CKD','cancer') NOT NULL,
  status ENUM('waitlist','active') NOT NULL,
  tanggal_mulai DATE NOT NULL,
  diubah_pada TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY(no_rawat), KEY idx_episode_ptm_draft(no_rkm_medis,tipe)
) ENGINE=InnoDB;
