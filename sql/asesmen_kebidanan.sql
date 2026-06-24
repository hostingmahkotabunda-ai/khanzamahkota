-- =====================================================================
-- Tabel: asesmen_kebidanan (+ asesmen_kebidanan_persalinan)
-- form RMAsesmenKebidanan (RM 5a). Dibuka dari tab "Penilaian Awal"
-- -> "Awal Kebidanan". Semua field teks = TEXT agar tidak kena
-- "Row size too large (>8126)". REPLACE INTO per no_rawat.
-- =====================================================================
CREATE TABLE IF NOT EXISTS `asesmen_kebidanan` (
  `no_rawat`           VARCHAR(17) NOT NULL,
  `tanggal`            DATE        DEFAULT NULL,
  `jam`                TIME        DEFAULT NULL,
  `ruang`              TEXT, `lantai` TEXT, `kelas` TEXT, `diagnosa_medis` TEXT, `gelang` TEXT,
  `kondisi_masuk`      TEXT, `via` TEXT, `nadi` TEXT, `respirasi` TEXT, `suhu` TEXT, `spo2` TEXT,
  `td`                 TEXT, `td_posisi` TEXT, `tb` TEXT, `bb` TEXT,
  `gcs_e`              TEXT, `gcs_m` TEXT, `gcs_v` TEXT, `keluhan_utama` TEXT,
  `alergi_lateks`      TEXT, `alergi_makanan_obat` TEXT, `alergi_reaksi` TEXT, `snap_alert` TEXT,
  `nyeri_skala`        TEXT, `nyeri_lokasi` TEXT, `nyeri_onset` TEXT, `nyeri_variasi` TEXT,
  `nyeri_kualitas`     TEXT, `nyeri_pemberat` TEXT, `nyeri_pencetus` TEXT, `nyeri_obat` TEXT, `nyeri_efek` TEXT,
  `status_nikah`       TEXT, `keluarga_tinggal` TEXT, `tempat_tinggal` TEXT, `pekerjaan` TEXT, `agama_keyakinan` TEXT,
  `menarche`           TEXT, `lama_haid` TEXT, `jumlah_darah` TEXT, `haid_terakhir` TEXT,
  `tafsiran_persalinan` TEXT, `gangguan_haid` TEXT,
  `kawin_ke`           TEXT, `gpa_g` TEXT, `gpa_p` TEXT, `gpa_a` TEXT, `gpa_hidup` TEXT,
  `hamil_muda`         TEXT, `hamil_tua` TEXT, `riwayat_operasi` TEXT,
  `riwayat_penyakit_keluarga` TEXT, `riwayat_gynekologi` TEXT, `kb_metode` TEXT, `kb_lama` TEXT, `kb_komplikasi` TEXT,
  `pola_makan`         TEXT, `pola_minum` TEXT, `pola_konsumsi` TEXT,
  `gizi_asupan`        TEXT, `gizi_bb` TEXT, `gizi_hb` TEXT, `gizi_metabolisme` TEXT, `gizi_total` TEXT,
  `bak_jumlah`         TEXT, `bak_warna` TEXT, `bak_terakhir` TEXT, `bab_jumlah` TEXT, `bab_karakteristik` TEXT,
  `bab_terakhir`       TEXT, `tidur_jam` TEXT, `tidur_terakhir` TEXT,
  `nilai_keyakinan`    TEXT, `penerimaan_kehamilan` TEXT, `sosial_support` TEXT,
  `fisik_mata`         TEXT, `fisik_dada` TEXT, `fisik_ekstrimitas` TEXT, `fisik_kardio` TEXT,
  `morse_jatuh`        TEXT, `morse_diagnosis` TEXT, `morse_alat` TEXT, `morse_infus` TEXT,
  `morse_jalan`        TEXT, `morse_mental` TEXT, `morse_total` TEXT, `morse_resiko` TEXT,
  `obs_inspeksi`       TEXT, `obs_tfu` TEXT, `obs_letak_punggung` TEXT, `obs_presentasi` TEXT, `obs_palpasi` TEXT,
  `obs_taksiran_bb`    TEXT, `obs_djj` TEXT, `obs_djj_irama` TEXT, `obs_bagian_terendah` TEXT, `obs_his` TEXT, `obs_his_irama` TEXT,
  `gyn_inspeksi`       TEXT, `gyn_vagina` TEXT, `gyn_portio` TEXT, `gyn_vt` TEXT, `gyn_kesan_panggul` TEXT, `gyn_imbang` TEXT,
  `nifas_tfu`          TEXT, `nifas_kontraksi` TEXT, `nifas_lochea` TEXT, `nifas_luka` TEXT,
  `kriteria_discharge` TEXT, `perencanaan_pulang` TEXT,
  `penunjang_hb`       TEXT, `penunjang_ht` TEXT, `penunjang_urine` TEXT, `penunjang_ctg` TEXT, `penunjang_usg` TEXT,
  `diagnosa_kebidanan` TEXT, `rencana_kebidanan` TEXT,
  `tgl_ttd`            DATE        DEFAULT NULL,
  `jam_ttd`            TIME        DEFAULT NULL,
  `nik`                VARCHAR(20) DEFAULT NULL,
  `kd_dokter`          VARCHAR(20) DEFAULT NULL,
  PRIMARY KEY (`no_rawat`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Riwayat persalinan & nifas yang lalu (beberapa baris per no_rawat)
CREATE TABLE IF NOT EXISTS `asesmen_kebidanan_persalinan` (
  `no_rawat`          VARCHAR(17) NOT NULL,
  `urut`              INT(3)      NOT NULL DEFAULT 0,
  `tgl_partus`        VARCHAR(50)  DEFAULT NULL,
  `tempat_partus`     VARCHAR(100) DEFAULT NULL,
  `umur_hamil`        VARCHAR(50)  DEFAULT NULL,
  `jenis_persalinan`  VARCHAR(100) DEFAULT NULL,
  `penolong`          VARCHAR(100) DEFAULT NULL,
  `penyulit`          VARCHAR(150) DEFAULT NULL,
  `anak`              VARCHAR(100) DEFAULT NULL,
  `keadaan_anak`      VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (`no_rawat`,`urut`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
