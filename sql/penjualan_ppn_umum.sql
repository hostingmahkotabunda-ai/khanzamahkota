-- Kolom baru utk simpan JUMLAH PPN umum (field "PPN(%)" / besarppn di DlgPenjualan) per nota.
-- Sebelumnya kolom `penjualan.ppn` yg sudah ada cuma nyimpen PPN OBAT (besarppnobat) --
-- PPN umum sama sekali TIDAK pernah disimpan, jadi saat nota di-cetak ulang lewat
-- "Cari Penjualan" (NotaApotek2.php), PPN umum-nya DIHITUNG ULANG dari tabel
-- akun_bayar.ppn (persen tetap per metode bayar) -- BUKAN dari yang diketik user
-- di layar DlgPenjualan. Itu penyebab PPN di nota cetak beda dari yang di layar.
-- Ditambah di AKHIR tabel (bukan disisipkan di tengah) supaya INSERT lama yg pakai
-- urutan kolom positional (`insert into penjualan values(...)`) tidak perlu diubah urutannya.

ALTER TABLE penjualan ADD COLUMN ppn_umum DOUBLE NULL DEFAULT 0;
