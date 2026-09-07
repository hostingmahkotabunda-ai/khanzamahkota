# Pemuatan awal DlgRawatInap

Klik dua kali nomor rawat dari Kamar Inap sekarang memakai `setNoRmAsync`. Identitas pasien diisi dari tabel, lalu DPJP, SOAP, dan jumlah validasi dibaca melalui SwingWorker dengan koneksi baca tersendiri. Query SOAP (termasuk filter, UNION rawat jalan/rawat inap, dan urutan) tetap sama. Pencarian otomatis saat pengisian nomor rawat awal dicegah agar tidak memuat SOAP dua kali. Komponen Resep baru dibuat saat tab Resep dipilih.

Selama pemuatan, glass pane menahan klik ke tabel/tab dan keadaan enabled setiap komponen disimpan. Setelah berhasil, keadaan tersebut dipulihkan sehingga tombol yang sebelumnya dilarang oleh hak akses tetap dilarang. Jika gagal, halaman tetap terkunci dan menyediakan Coba lagi/Tutup. Menutup dialog membatalkan worker; hasil dari versi konteks lama diabaikan. Koneksi dan statement worker ditutup tanpa menyentuh transaksi koneksi utama.

Konstruktor form dan `isCek()` masih memiliki inisialisasi komponen serta beberapa pembacaan konfigurasi/pegawai. Karena itu perubahan ini menghilangkan penantian data pasien sebelum tampil, tetapi belum menjamin nol jeda saat pembukaan pertama. Kecepatan aktual perlu diuji pada aplikasi yang terhubung ke database pengguna.

## Verifikasi yang sudah dilakukan

- Kompilasi lima source yang berubah dengan JDK 15, target Java 8: berhasil (peringatan kompatibilitas/deprecation/unchecked).
- `test/RanapLoadingReadTest.java`: berhasil tanpa koneksi database. Meliputi pemetaan 24 kolom, status validasi, penanda RALAN, parameter tanggal/RM/kata kunci kedua sumber, pencarian kosong, pemilihan DPJP pertama, hitungan validasi, propagasi error, pembatalan, dan penutupan resource JDBC.
- Literal SQL pembacaan SOAP dibandingkan dengan salinan sebelum perubahan: sama.
- Belum ada pengujian GUI dengan database nyata; tidak ada perubahan data/skema database yang dijalankan selama pengerjaan ini.

## Mencoba di aplikasi

Build/Run ulang proyek melalui NetBeans agar perubahan source dipakai. Output verifikasi berada di `tmp/compile-ranap-loading`; JAR distribusi belum diganti.

1. Klik dua kali nomor rawat. Identitas pasien harus terlihat dengan status Memuat data pasien. Tabel/tab/tombol tindakan belum dapat dipakai; Tutup tetap dapat dipakai.
2. Setelah selesai, cocokkan DPJP, daftar SOAP beserta tanggal/urutan/sumber RALAN, dan notifikasi validasi dengan versi lama. Coba ketiga filter tanggal Kamar Inap dan pasien gabung ibu/bayi melalui menu tindakan.
3. Uji akun dengan hak akses berbeda: tombol terlarang harus tetap terlarang setelah memuat.
4. Tutup sebelum selesai, kemudian buka pasien lain. Hasil pasien sebelumnya tidak boleh masuk ke dialog baru.
5. Pada lingkungan uji, simulasikan kegagalan koneksi. Harus tampil Coba lagi/Tutup, tanpa menganggap kegagalan sebagai daftar SOAP kosong. Coba lagi setelah koneksi pulih.
6. Buka tab Resep, lalu kembali ke Pemeriksaan. Pastikan konteks resep sesuai pasien. Uji simpan/edit hanya dengan pasien/data uji.

## Kembali ke versi awal

Lima file asli disimpan sebelum perubahan di `tmp/backup-ranap-loading-20260907-094833`. Script `restore.ps1` di folder itu memeriksa hash semua target sebelum memulihkan source; jika ada perubahan lain setelah pengerjaan ini, script berhenti agar perubahan tersebut tidak tertimpa. Setelah pemulihan, Build/Run ulang. File uji dan catatan ini tidak memengaruhi perilaku aplikasi.

Jalankan dari root proyek jika ingin membatalkan perubahan:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tmp/backup-ranap-loading-20260907-094833/restore.ps1
```
