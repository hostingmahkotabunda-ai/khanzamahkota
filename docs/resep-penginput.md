# Penginput resep

Jalankan `sql/resep_penginput.sql` di database SIMRS, kemudian build dan pasang aplikasi. SQL ini membuat tabel pendamping; tidak menambah kolom pada `resep_obat`. Samakan charset/collation `no_resep` dengan tabel induk bila database memakai pengaturan berbeda dari dump proyek (latin1). Foreign key menghapus metadata saat resep induk dihapus agar nomor yang digunakan kembali tidak menampilkan penginput lama.

Pencatatan berlaku untuk resep baru yang disimpan lewat `DlgPeresepanDokter`, termasuk tab Resep rawat inap dan rawat jalan yang memakai dialog ini. ID akun diambil dari `akses.getkode()` saat pengguna menyimpan dan tetap disimpan untuk pelacakan, tetapi tidak ditampilkan pada kartu, detail, maupun tooltip. Nama disalin dari master pegawai/petugas/dokter; jika tidak ditemukan, tampil “Nama petugas belum tersedia”. Waktu input menggunakan waktu server database, bukan tanggal resep yang bisa diubah pengguna.

Data penginput ditulis setelah commit resep utama dan setelah autocommit aktif. Kegagalan metadata tidak membatalkan resep; pengguna mendapat pesan agar menghubungi admin dan tidak menyimpan ulang. Ada jeda antara commit utama dan metadata: jika aplikasi/koneksi terputus saat itu, resep dapat tersimpan tanpa penginput. Aplikasi tidak membuat tabel otomatis saat transaksi berlangsung.

Penginput awal tidak diubah ketika resep diedit. Resep lama tidak diisi dengan akun orang yang mengedit atau membuka resep. Resep dari modul lain yang belum memakai pencatatan ini juga ditampilkan sebagai “Penginput belum tercatat”. Perubahan ini belum mencatat riwayat pengeditan maupun mengisi ulang data dari trackersql.

Tab resep menampilkan calon penginput sebelum simpan dan penginput tersimpan saat membuka resep untuk diedit. Tombol Detail Resep menampilkan resep untuk nomor rawat pasien tersebut, termasuk penginput dan waktu input; pilih baris lalu Lihat Detail.

Halaman Copy Resep juga menampilkan penginput dan waktu input pada kepala detail resep yang dipilih, di bawah informasi dokter peresep. Identitas ini berasal dari resep sumber yang sedang dilihat, bukan akun pengguna yang membuka halaman atau menyalin resep.

Setiap kartu Daftar Resep menampilkan baris Penginput di bawah nama dokter. Arahkan kursor ke kartu untuk melihat identitas dan waktu input lengkap jika teks terpotong. Metadata dimuat sekali per resep dan diperbarui saat daftar dimuat ulang.

Pada Data Peresepan Obat Oleh Dokter, tab Resep Rawat Inap memiliki kolom Petugas Penginput setelah Jam Validasi. Kolom ini hanya menampilkan nama dari tabel pendamping, tanpa ID akun. Resep lama tanpa metadata tetap ditampilkan.

## Verifikasi

Kompilasi dialog dan helper dengan dependensi proyek; jalankan `test/ResepPenginputTest.java` sebagai program Java biasa. Tes memakai JDBC proxy tanpa koneksi SIMRS, untuk memeriksa penolakan penulisan saat transaksi utama aktif, identitas/waktu yang dibaca, duplikasi, data lama, nama cadangan, dan kegagalan tabel.

Uji penerimaan di database uji setelah migrasi:

1. Simpan resep umum, racikan, serta catatan tanpa obat dengan akun A; periksa metadata pada daftar/detail.
2. Edit resep tersebut dengan akun B; pastikan penginput tetap A.
3. Buka resep lama tanpa metadata; pastikan muncul “Penginput belum tercatat”.
4. Dengan tabel pendamping tidak tersedia di database uji, simpan resep baru; pastikan resep utama tetap tersimpan dan pesan kegagalan metadata muncul.
5. Gagalkan penyimpanan resep utama; pastikan tidak ada metadata baru.
6. Hapus resep uji; pastikan metadata ikut terhapus.
