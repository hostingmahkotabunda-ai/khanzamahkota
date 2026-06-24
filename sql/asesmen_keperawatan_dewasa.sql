-- =====================================================================
-- Tabel: asesmen_keperawatan_dewasa  (form RMAsesmenKeperawatanDewasa, RM 5d)
-- Dibuka dari tab "Penilaian Awal" -> "Awal Keperawatan Dewasa".
-- Semua field teks = TEXT agar tidak kena "Row size too large (>8126)".
-- REPLACE INTO per no_rawat. Nama tabel BARU, tidak menimpa bawaan.
-- =====================================================================
CREATE TABLE IF NOT EXISTS `asesmen_keperawatan_dewasa` (
  `no_rawat`           VARCHAR(17) NOT NULL,
  `tanggal`            DATE        DEFAULT NULL,
  `jam`                TIME        DEFAULT NULL,
  `ruang`              TEXT, `lantai` TEXT, `kelas` TEXT, `diagnosa_medis` TEXT, `gelang` TEXT,
  `kondisi_masuk`      TEXT, `via` TEXT, `nadi` TEXT, `respirasi` TEXT, `suhu` TEXT, `spo2` TEXT,
  `td`                 TEXT, `td_posisi` TEXT, `tb` TEXT, `bb` TEXT,
  `gcs_e`              TEXT, `gcs_m` TEXT, `gcs_v` TEXT, `diagnosa_masuk` TEXT, `keluhan_utama` TEXT,
  `alergi`             TEXT, `alergi_lateks` TEXT, `alergi_obat` TEXT, `snap_alert` TEXT,
  `barang_jenis`       TEXT, `barang_tindakan` TEXT,
  `riwayat_pasien`     TEXT, `masalah_anastesi` TEXT, `deskripsi_penyakit` TEXT,
  `alkohol`            TEXT, `alkohol_jumlah` TEXT, `alkohol_jenis` TEXT,
  `merokok`            TEXT, `merokok_jumlah` TEXT, `merokok_jenis` TEXT,
  `influenza_12bln`    TEXT, `pneumonia_5thn` TEXT, `vaksinasi_lainnya` TEXT, `riwayat_keluarga` TEXT,
  `status_nikah`       TEXT, `keluarga_tinggal` TEXT, `tempat_tinggal` TEXT, `pekerjaan` TEXT,
  `agama_keyakinan`    TEXT, `aktivitas_psiko` TEXT, `curiga_aniaya` TEXT, `status_emosional` TEXT,
  `keluarga_terdekat`  TEXT, `hubungan_keluarga` TEXT, `telepon` TEXT, `informasi_dari` TEXT,
  `fisik_mata`         TEXT, `fisik_mata_ket` TEXT, `fisik_kardio` TEXT, `fisik_kardio_ket` TEXT,
  `fisik_gastro`       TEXT, `fisik_gastro_ket` TEXT,
  `mst_bb`             TEXT, `mst_asupan` TEXT, `mst_total` TEXT,
  `fisik_genito`       TEXT, `fisik_genito_ket` TEXT, `fisik_neuro` TEXT, `fisik_neuro_ket` TEXT,
  `fisik_musculo`      TEXT, `fisik_musculo_ket` TEXT, `luka_rujuk` TEXT,
  `norton_fisik`       TEXT, `norton_mental` TEXT, `norton_aktivitas` TEXT, `norton_mobilitas` TEXT,
  `norton_inkontinensia` TEXT, `norton_total` TEXT, `norton_catatan` TEXT,
  `adl_makan`          TEXT, `adl_mandi` TEXT, `adl_grooming` TEXT, `adl_berpakaian` TEXT, `adl_bak` TEXT,
  `adl_bab`            TEXT, `adl_toilet` TEXT, `adl_transfer` TEXT, `adl_mobilitas` TEXT, `adl_tangga` TEXT,
  `adl_total`          TEXT, `adl_interpretasi` TEXT,
  `morse_jatuh`        TEXT, `morse_diagnosis` TEXT, `morse_alat` TEXT, `morse_infus` TEXT,
  `morse_jalan`        TEXT, `morse_mental` TEXT, `morse_total` TEXT, `morse_resiko` TEXT,
  `nyeri_skala`        TEXT, `nyeri_lokasi` TEXT, `nyeri_onset` TEXT, `nyeri_variasi` TEXT,
  `nyeri_kualitas`     TEXT, `nyeri_pemberat` TEXT, `nyeri_pencetus` TEXT, `nyeri_obat` TEXT, `nyeri_efek` TEXT,
  `restraint_pernah`   TEXT, `restraint_perlu` TEXT,
  `komunikasi`         TEXT, `bahasa_harian` TEXT, `hambatan_belajar` TEXT, `cara_belajar` TEXT,
  `tingkat_pendidikan` TEXT, `kebutuhan_edukasi` TEXT,
  `kriteria_discharge` TEXT, `resume_umum` TEXT, `resume_rujuk` TEXT, `rencana_keperawatan` TEXT, `masalah_keperawatan` TEXT,
  `tgl_ttd`            DATE        DEFAULT NULL,
  `jam_ttd`            TIME        DEFAULT NULL,
  `nik`                VARCHAR(20) DEFAULT NULL,
  `kd_dokter`          VARCHAR(20) DEFAULT NULL,
  PRIMARY KEY (`no_rawat`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
