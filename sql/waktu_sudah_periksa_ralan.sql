CREATE TABLE IF NOT EXISTS `waktu_sudah_periksa_ralan` (
  `no_rawat` varchar(17) NOT NULL,
  `waktu_sudah` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `petugas` varchar(50) NOT NULL DEFAULT '',
  `sumber` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`no_rawat`),
  KEY `waktu_sudah` (`waktu_sudah`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
