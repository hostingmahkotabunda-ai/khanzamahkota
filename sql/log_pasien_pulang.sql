-- Log siapa yg memulangkan pasien (klik tombol "Pulang" di DlgKamarInap) -- sebelumnya tidak
-- tercatat sama sekali, jadi kalau ada masalah pasien dipulangkan tidak jelas oleh siapa,
-- tidak bisa di-track. 1 baris per aksi pulang (bukan snapshot -- kalau pasien yg sama pernah
-- "batal pulang" lalu dipulangkan ulang, itu jadi baris baru lagi).
-- Catatan: form (DlgKamarInap) juga membuat tabel ini otomatis (ensureTable()), jadi
-- menjalankan skrip ini bersifat opsional / untuk membuat manual.

CREATE TABLE IF NOT EXISTS log_pasien_pulang (
    id             INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    no_rawat       VARCHAR(17)  NOT NULL,
    no_rkm_medis   VARCHAR(15)  NULL,
    nm_pasien      VARCHAR(100) NULL,
    status_pulang  VARCHAR(30)  NULL,
    tgl_pulang     DATE         NULL,
    jam_pulang     VARCHAR(8)   NULL,
    petugas_nip    VARCHAR(20)  NULL,
    petugas_nama   VARCHAR(60)  NULL,
    dicatat_pada   DATETIME     NULL,
    INDEX idx_no_rawat (no_rawat)
);
