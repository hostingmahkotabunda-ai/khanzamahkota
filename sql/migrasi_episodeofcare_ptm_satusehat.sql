-- Mahkota Bunda: tinjau dan jalankan MANUAL setelah persetujuan.
-- Tidak memuat kredensial, tidak mengubah tabel Encounter/Condition yang sudah ada.
CREATE TABLE IF NOT EXISTS satu_sehat_episode_of_care (
    id_episode VARCHAR(64) NOT NULL,
    no_rkm_medis VARCHAR(15) NOT NULL,
    tipe VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    tanggal_mulai DATE NOT NULL,
    tanggal_selesai DATE DEFAULT NULL,
    no_rawat_awal VARCHAR(17) NOT NULL,
    id_condition VARCHAR(64) NOT NULL,
    identifier_lokal VARCHAR(100) NOT NULL,
    diubah_pada TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id_episode),
    KEY idx_episode_ptm_pasien (no_rkm_medis, tipe, status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS satu_sehat_encounter_episode (
    no_rawat VARCHAR(17) NOT NULL,
    id_episode VARCHAR(64) NOT NULL,
    PRIMARY KEY (no_rawat, id_episode),
    CONSTRAINT fk_encounter_episode_ptm FOREIGN KEY (id_episode)
        REFERENCES satu_sehat_episode_of_care(id_episode)
) ENGINE=InnoDB;
