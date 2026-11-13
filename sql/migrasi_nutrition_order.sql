-- MANUAL ONLY. Review, back up, and obtain approval before executing.
-- Does not alter ADIME, Composition, permissions, inventory, or clinical records.
CREATE TABLE gizi_instruksi_diet (
 id_instruksi char(36) NOT NULL,
 source_key char(64) NOT NULL,
 no_rawat varchar(30) NOT NULL,
 tanggal_adime datetime NOT NULL,
 source_hash char(64) NOT NULL,
 review_json mediumtext NOT NULL,
 revisi int NOT NULL,
 pencatat varchar(40) NOT NULL,
 diubah_pada datetime NOT NULL,
 PRIMARY KEY(id_instruksi), UNIQUE KEY uq_gizi_diet_source(source_key),
 KEY idx_gizi_diet_visit(no_rawat)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE gizi_instruksi_diet_riwayat (
 id_instruksi char(36) NOT NULL, revisi int NOT NULL,
 source_hash char(64) NOT NULL, review_json mediumtext NOT NULL,
 pencatat varchar(40) NOT NULL, dicatat_pada datetime NOT NULL,
 PRIMARY KEY(id_instruksi,revisi),
 FOREIGN KEY(id_instruksi) REFERENCES gizi_instruksi_diet(id_instruksi)
 ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE satu_sehat_nutrition_order (
 id_instruksi char(36) NOT NULL, organisasi varchar(64) NOT NULL,
 endpoint_hash char(64) NOT NULL, endpoint varchar(255) NOT NULL,
 id_nutrition_order varchar(64) NOT NULL DEFAULT '',
 sync_state varchar(24) NOT NULL DEFAULT 'NEW',
 revisi_terkirim int NOT NULL DEFAULT 0,
 revisi_dikirim int NOT NULL DEFAULT 0,
 payload_hash char(64) NOT NULL DEFAULT '',
 payload_dikirim mediumtext,
 pesan varchar(255) NOT NULL DEFAULT '',
 diubah_pada datetime NOT NULL,
 PRIMARY KEY(id_instruksi,organisasi,endpoint_hash),
 FOREIGN KEY(id_instruksi) REFERENCES gizi_instruksi_diet(id_instruksi)
 ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- No automatic downgrade: these tables may contain clinical history and server IDs.
-- To roll application code back, keep these additive tables and their data.
-- Drop only unused tables after verifying zero records and separate approval.
