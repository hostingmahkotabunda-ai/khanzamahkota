-- Penyimpanan override ceklis manual pada menu Status Data RM.
-- Satu baris per komponen membuat penambahan komponen baru tidak memerlukan ALTER TABLE.
CREATE TABLE IF NOT EXISTS `kelengkapan_berkas_rm` (
  `no_rawat` varchar(17) NOT NULL,
  `kode_komponen` varchar(3) NOT NULL,
  `nilai` enum('0','1') NOT NULL DEFAULT '0',
  `diubah_oleh` varchar(20) NOT NULL DEFAULT '',
  `diubah_pada` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`no_rawat`,`kode_komponen`),
  KEY `kode_komponen` (`kode_komponen`),
  CONSTRAINT `kelengkapan_berkas_rm_ibfk_1` FOREIGN KEY (`no_rawat`)
    REFERENCES `reg_periksa` (`no_rawat`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
