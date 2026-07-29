-- Tabel Asesmen / Penilaian Awal Rawat Jalan (form RMAsesmenRalan)
-- 1 baris per no_rawat (REPLACE INTO). Semua kolom isian = TEXT untuk
-- menghindari error #1118 Row size too large di InnoDB.
-- Catatan: form juga membuat tabel ini otomatis (pastikanTabel()), jadi
-- menjalankan skrip ini bersifat opsional / untuk membuat manual.

CREATE TABLE IF NOT EXISTS asesmen_ralan (
    no_rawat         VARCHAR(17) NOT NULL PRIMARY KEY,
    tanggal          DATE,
    jam              TIME,
    rujukan          TEXT,
    cara_datang      TEXT,
    informasi_dari   TEXT,
    penyakit_sekarang TEXT,
    penyakit_dahulu  TEXT,
    td               TEXT,
    suhu             TEXT,
    nadi             TEXT,
    rr               TEXT,
    nyeri            TEXT,
    skala_nyeri      TEXT,
    bb               TEXT,
    tb               TEXT,
    imt              TEXT,
    lingkar_kepala   TEXT,
    alat_bantu       TEXT,
    prothesa         TEXT,
    cacat_tubuh      TEXT,
    adl              TEXT,
    mandiri          TEXT,
    status_nikah     TEXT,
    saudara          TEXT,
    jumlah_saudara   TEXT,
    negara           TEXT,
    wna_asal         TEXT,
    pekerjaan        TEXT,
    tinggal_bersama  TEXT,
    nama_keluarga    TEXT,
    telepon          TEXT,
    agama            TEXT,
    riwayat_alergi   TEXT,
    riwayat_bedah    TEXT,
    tgl_ttd          DATE,
    jam_ttd          TIME,
    nik              VARCHAR(20)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Migrasi bila tabel sudah terlanjur dibuat versi lama (tanpa kolom riwayat):
-- ALTER TABLE asesmen_ralan ADD COLUMN riwayat_alergi TEXT;
-- ALTER TABLE asesmen_ralan ADD COLUMN riwayat_bedah  TEXT;
