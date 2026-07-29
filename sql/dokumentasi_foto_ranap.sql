-- Dokumentasi Foto Rawat Inap
-- Log berulang -- BUKAN 1 baris per no_rawat, tapi 1 baris per foto yang
-- diupload (bisa banyak foto per no_rawat). Diisi dari form
-- RMDokumentasiFoto, dipicu klik-kanan pasien di DlgKamarInap.
-- Catatan: form juga membuat tabel ini otomatis (ensureTable()), jadi
-- menjalankan skrip ini bersifat opsional / untuk membuat manual.

CREATE TABLE IF NOT EXISTS dokumentasi_foto_ranap (
    id          INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    no_rawat    VARCHAR(17)  NOT NULL,
    keterangan  VARCHAR(255) NULL,
    nama_file   VARCHAR(150) NULL,
    photo       LONGBLOB     NULL,
    tgl_upload  DATETIME     NULL,
    created_by  VARCHAR(50)  NULL,
    INDEX idx_no_rawat (no_rawat)
);
