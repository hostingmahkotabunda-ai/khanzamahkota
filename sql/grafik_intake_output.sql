-- Tabel-tabel pendukung Grafik Nadi/Suhu/TTV (RM 9)
-- Nadi/Suhu/Respirasi/Tensi/BB-TB TIDAK punya tabel sendiri sebagai sumber
-- utama -- ditarik OTOMATIS dari riwayat SOAP/CPPT (tabel pemeriksaan_ranap,
-- sudah ada, diisi tiap kali DlgSOAPPerawatan disimpan), jam aktualnya
-- di-"snap" ke checkpoint terdekat (06/12/18/24) lalu digambar sbg grafik
-- garis grid-per-tanggal (persis pola kertas RM 9) di form RMGrafikTTV.
-- Catatan: form juga membuat kedua tabel di bawah ini otomatis
-- (ensureTable()), jadi menjalankan skrip ini bersifat opsional.

-- 1. Intake/Output harian -- Per Oral, Parenteral, Transfusi, D.L.L, Kemih,
--    Muntah, Defekasi, Berkemih -- tidak ada sumber datanya di sistem manapun,
--    diisi manual, 1 baris per tanggal per no_rawat (snapshot harian,
--    REPLACE INTO kalau tanggal yg sama diisi ulang).
CREATE TABLE IF NOT EXISTS grafik_intake_output (
    no_rawat     VARCHAR(17)  NOT NULL,
    tanggal      DATE         NOT NULL,
    per_oral     VARCHAR(20)  NULL,
    parenteral   VARCHAR(20)  NULL,
    transfusi    VARCHAR(20)  NULL,
    dll          VARCHAR(20)  NULL,
    kemih        VARCHAR(20)  NULL,
    muntah       VARCHAR(20)  NULL,
    defekasi     VARCHAR(20)  NULL,
    berkemih     VARCHAR(20)  NULL,
    catatan      VARCHAR(500) NULL,
    created_by   VARCHAR(50)  NULL,
    created_at   DATETIME     NULL,
    PRIMARY KEY (no_rawat, tanggal)
);

-- 2. Input manual Nadi/Suhu/TTV per checkpoint (06/12/18/24) -- dipakai
--    petugas utk "menggambar" titik sendiri persis seperti di kertas, ATAU
--    melengkapi checkpoint yg tidak tercatat di SOAP/CPPT. Kalau ada baris
--    manual utk tanggal+jam_slot yg sama dgn hasil snap CPPT, nilai manual
--    ini yg dipakai (override), lihat method bangunGrid() di RMGrafikTTV.java.
CREATE TABLE IF NOT EXISTS grafik_ttv_manual (
    no_rawat     VARCHAR(17) NOT NULL,
    tanggal      DATE        NOT NULL,
    jam_slot     VARCHAR(2)  NOT NULL, -- '06','12','18','24'
    nadi         VARCHAR(5)  NULL,
    suhu         VARCHAR(5)  NULL,
    respirasi    VARCHAR(5)  NULL,
    tensi        VARCHAR(10) NULL,
    tinggi       VARCHAR(5)  NULL,
    berat        VARCHAR(5)  NULL,
    created_by   VARCHAR(50) NULL,
    created_at   DATETIME    NULL,
    PRIMARY KEY (no_rawat, tanggal, jam_slot)
);
