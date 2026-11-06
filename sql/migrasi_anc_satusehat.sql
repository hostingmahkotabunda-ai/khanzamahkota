-- Mahkota Bunda: tinjau dan jalankan MANUAL setelah persetujuan.
-- Terpisah dari episode PTM. Tidak memindahkan/menghapus data lama.
CREATE TABLE IF NOT EXISTS satu_sehat_episode_kehamilan (
    id_episode_kehamilan BIGINT NOT NULL AUTO_INCREMENT,
    no_rkm_medis VARCHAR(15) NOT NULL,
    hpht DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    no_rawat_awal VARCHAR(17) NOT NULL,
    identifier_lokal VARCHAR(100) NOT NULL,
    id_episode VARCHAR(64) DEFAULT NULL,
    diubah_pada TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id_episode_kehamilan),
    UNIQUE KEY uq_episode_kehamilan_hpht (no_rkm_medis, hpht),
    UNIQUE KEY uq_episode_kehamilan_identifier (identifier_lokal),
    UNIQUE KEY uq_episode_kehamilan_remote (id_episode),
    KEY idx_episode_kehamilan_pasien (no_rkm_medis, status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS satu_sehat_kunjungan_anc (
    no_rawat VARCHAR(17) NOT NULL,
    id_episode_kehamilan BIGINT NOT NULL,
    status_kunjungan ENUM('K1A','K1M','K2','K3','K4','K5','K6') NOT NULL,
    PRIMARY KEY (no_rawat),
    KEY idx_kunjungan_anc_episode (id_episode_kehamilan),
    CONSTRAINT fk_kunjungan_anc_kehamilan FOREIGN KEY (id_episode_kehamilan)
        REFERENCES satu_sehat_episode_kehamilan(id_episode_kehamilan)
) ENGINE=InnoDB;
