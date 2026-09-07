# Pembaruan tabel dan layout setelah Rawat Inap terbuka

Keluhan: halaman sudah terbuka lebih cepat, tetapi tampilan sempat tersendat/berubah susunan sebelum utuh. Dua pekerjaan pada EDT (thread antarmuka) diperbaiki di DlgRawatInap.java:

1. Sebelumnya, hasil SOAP dikosongkan/dimasukkan baris demi baris sehingga setiap baris menghasilkan event model yang diterima JTable dan pengurutnya. Sekarang data awal disiapkan menjadi kumpulan baris serta penanda RALAN di worker. EDT mengganti kumpulan baris sekali dan mengirim satu `fireTableDataChanged()`. Struktur/model kolom tidak diganti, sehingga renderer, lebar, posisi kolom, dan sort key dipertahankan. Pencarian sinkron juga memakai pemasangan satu event ini.
2. Sebelumnya, layout SOAP dijalankan dari componentResized/invokeLater, memakai ukuran sementara 1860x505 ketika panel belum siap, dan dijalankan lagi saat notifikasi validasi diperbarui. Sekarang LayoutManager menghitung posisi dengan ukuran nyata pada siklus layout sebelum paint. Perhitungan dilewati jika ukuran belum siap atau belum berubah. Penampilan notifikasi tidak lagi memaksa perhitungan ulang seluruh layout.

Query SOAP, filter pasien, urutan hasil database, kolom/status validasi, penanda sumber RALAN, aturan checkbox RALAN, dan handler Simpan/Edit/Hapus dipertahankan. Glass pane pemuatan serta pemulihan hak akses tetap dipakai.

## Verifikasi

- Kompilasi DlgRawatInap dan kedua uji berhasil menggunakan JDK 15 target Java 8.
- RanapLoadingReadTest berhasil: kontrak query/hasil SOAP, DPJP, validasi, pembatalan, error, dan penutupan JDBC.
- RanapTableUpdateTest berhasil dengan JTable/pengurut asli dalam mode headless tanpa database: 1000 baris menghasilkan satu event model; isi/urutan model, penanda RALAN, sorting, identitas/lebar/renderer kolom, pengosongan, dan pembatasan pembaruan ke EDT tetap bekerja.
- Handler Simpan/Hapus/Edit dibandingkan dengan backup dan identik.
- Belum dilakukan pengukuran frame/latensi di aplikasi dengan database pengguna. Hasil uji satu event bukan klaim bahwa seluruh freeze sudah hilang pada semua kondisi/perangkat.

Build/Run ulang proyek di NetBeans untuk mencoba. Perhatikan pembukaan pertama, penampilan notifikasi validasi, tabel pasien dengan banyak SOAP, perubahan ukuran jendela, penutupan/pembukaan panel input, pengurutan kolom, serta hak akses setelah pemuatan. Tidak mengganti JAR distribusi selama pengerjaan ini.

## Pemulihan

Backup source sebelum tahap ini: `tmp/backup-ranap-paint-20260907-102300/DlgRawatInap.java`. Untuk kembali ke kondisi setelah penundaan dialog (tahap kedua):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tmp/backup-ranap-paint-20260907-102300/restore.ps1
```

Script memeriksa hash terlebih dahulu agar tidak menimpa perubahan lanjutan. Build/Run ulang setelah pemulihan. Jika ingin membatalkan tahap yang lebih awal juga, pulihkan tahap ini dahulu, kemudian ikuti pemulihan pada `ranap-lazy-20260907.md` dan `ranap-loading-20260907.md` secara berurutan.
