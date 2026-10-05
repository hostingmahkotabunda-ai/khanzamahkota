-- Jalankan pada database SIMRS sebelum memakai aplikasi versi ini.
-- Tidak mengubah kolom atau INSERT pada resep_obat.
-- Charset/collation no_resep harus sama dengan resep_obat.no_resep.
CREATE TABLE IF NOT EXISTS resep_penginput (
    no_resep varchar(14) NOT NULL,
    id_penginput varchar(20) NOT NULL,
    nama_penginput varchar(150) NOT NULL,
    waktu_input datetime NOT NULL,
    PRIMARY KEY (no_resep),
    CONSTRAINT resep_penginput_resep_fk FOREIGN KEY (no_resep)
        REFERENCES resep_obat (no_resep) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
