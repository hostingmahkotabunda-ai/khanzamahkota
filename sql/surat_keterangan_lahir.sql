-- Data isian utk cetak Surat Keterangan Lahir (SKL) -- HANYA lapisan data, tidak ada
-- kop/desain/sidik kaki/TTD di aplikasi ini (itu semua fisik, sudah ada di kertas blanko
-- yg dicetak duluan). 1 baris per no_rawat (bayi). Sebagian field auto-tarik dari
-- pasien_bayi + pasien (bayi & ibu via ranap_gabung) saat pertama dibuka, sisanya
-- (Pekerjaan Ayah, Dokter Anak, Keterangan Lain, 2 foto) input manual -- SEMUA field
-- (termasuk yg auto-tarik) tetap bisa diedit manual oleh petugas.
-- Catatan: form juga membuat tabel ini otomatis (ensureTable()), jadi menjalankan
-- skrip ini bersifat opsional / untuk membuat manual.

CREATE TABLE IF NOT EXISTS surat_keterangan_lahir (
    no_rawat          VARCHAR(17)  NOT NULL PRIMARY KEY,
    no_rkm_medis      VARCHAR(15)  NULL,
    no_skl            VARCHAR(30)  NULL,
    nama_bayi         VARCHAR(50)  NULL,
    tgl_lahir         DATE         NULL,
    jam_lahir         VARCHAR(8)   NULL,
    tempat_lahir      VARCHAR(150) NULL,
    nama_ayah         VARCHAR(50)  NULL,
    nama_ibu          VARCHAR(50)  NULL,
    alamat_orang_tua  VARCHAR(200) NULL,
    pekerjaan_ayah    VARCHAR(60)  NULL,
    pekerjaan_ibu     VARCHAR(60)  NULL,
    dokter_penolong   VARCHAR(100) NULL,
    dokter_anak       VARCHAR(100) NULL,
    jk                VARCHAR(20)  NULL,
    berat_badan       VARCHAR(10)  NULL,
    panjang_badan     VARCHAR(10)  NULL,
    lingkar_kepala    VARCHAR(10)  NULL,
    lingkar_dada      VARCHAR(10)  NULL,
    lingkar_perut     VARCHAR(10)  NULL,
    keterangan_lain   VARCHAR(200) NULL,
    foto1             LONGBLOB     NULL,
    foto2             LONGBLOB     NULL,
    created_by        VARCHAR(50)  NULL,
    created_at        DATETIME     NULL,
    updated_at        DATETIME     NULL
);
