# Penguncian input saat membuka Rawat Inap

Rekaman JFR pada aplikasi proyek (PID 33864, classpath NetBeans) menunjukkan waktu baca SOAP sekitar 196–200 ms pada worker. Waktu tersebut bukan eksekusi query di thread antarmuka. Pada EDT juga terlihat penantian kunci render sekitar 13–20 ms dan sampel pembaruan kursor Windows melalui `pulihkanKomponenAwal -> setEnabled -> WGlobalCursorManager`. Rekaman tidak membuktikan bahwa seluruh keluhan freeze berasal dari satu jalur tersebut.

Perubahan tahap ini menghilangkan penonaktifan dan pengaktifan ulang seluruh pohon komponen saat pemuatan. Glass pane tetap menahan mouse; `PengunciInputSementara` menahan keyboard pada form dan root jendela tanpa mengubah status enabled/editable. Keyboard jendela lain tetap berjalan. Panel pemuatan menerima navigasi, Space/Enter untuk tombolnya, dan Alt+F4 tetap tersedia. Mnemonic form seperti Alt+S juga diblokir ketika fokus berada pada tombol pemuatan. Pengunci dilepas ketika berhasil, dibatalkan, konteks diganti, atau dialog ditutup; kegagalan tetap mengunci dengan pilihan Coba lagi/Tutup.

Query dan filter SOAP, koneksi baca, data yang ditampilkan, hak akses, serta handler Simpan/Edit/Hapus tidak diubah dalam tahap ini. Data tetap memerlukan waktu pemuatan; perubahan ini menargetkan pekerjaan visual akibat perubahan status banyak komponen, bukan menghilangkan waktu database.

## Verifikasi

- Kompilasi JDK 15 target Java 8 berhasil.
- `RanapInputLockTest`: input selama pemuatan dan mnemonic terblokir, kontrol pemuatan/jendela lain tetap menerima keyboard, status enabled/editable/isi tetap, dan buka/kunci ulang berfungsi. Uji tambahan dengan JFrame asli juga lulus untuk event root/jendela serta isolasi jendela lain; jendela uji tidak ditampilkan.
- `RanapLoadingReadTest`: kontrak hasil/filter SOAP, DPJP, validasi, kegagalan, pembatalan, dan penutupan JDBC lulus.
- `RanapTableUpdateTest`: 1000 baris menjadi satu event tabel, data/penanda RALAN, sorting, kolom, checkbox, pengosongan, EDT, dan pembatalan lulus.
- Handler Simpan/Edit/Hapus identik dengan backup sebelum tahap ini; `git diff --check` lulus.
- Pengguna mencoba aplikasi terbaru (PID 36936) dan mengonfirmasi **"Sudah mulus"** pada 7 September 2026. Proses uji telah berhenti ketika pengambilan snapshot JFR susulan dilakukan, sehingga tidak ada perbandingan angka latensi sesudah perubahan. Hasil visual ini berdasarkan konfirmasi pengguna, bukan klaim pengukuran nol milidetik.

Kelas uji aplikasi berada di `tmp/compile-ranap-input`; peluncuran uji memakai urutan classpath proyek dari `tmp/diagnostics-ranap-ui/run-input.args`. JAR distribusi tidak diganti. Untuk menjalankan source dari NetBeans, hentikan Run yang lama lalu Build/Run ulang.

## Pemulihan tahap ini

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tmp/backup-ranap-input-20260907-110832/restore.ps1
```

Script memeriksa hash agar tidak menimpa perubahan lanjutan dan mengembalikan DlgRawatInap ke sebelum tahap ini. Helper/test yang baru tidak lagi dipakai oleh aplikasi setelah pemulihan dan dapat dibiarkan. Build/Run ulang setelah pemulihan. Untuk kembali lebih jauh, lanjutkan pemulihan tahap paint, lazy, lalu loading sesuai dokumen masing-masing.
