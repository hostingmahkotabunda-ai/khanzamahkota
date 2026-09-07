# Penundaan dialog pencarian dan panel Rawat Inap

Perubahan tahap kedua ini menerapkan dua langkah yang disetujui: dialog pencarian dibuat saat diperlukan dan panel SBAR/Surat Keterangan Lahir dibuat ketika tab dipilih. Perubahan pemuatan awal dengan SwingWorker dari tahap sebelumnya tetap dipertahankan.

## Perilaku

- Lima dialog pencarian (tindakan biasa, tindakan beberapa waktu, pasien, pegawai, dan riwayat lima SOAP) tidak lagi dibuat oleh konstruktor DlgRawatInap. Getter membuat setiap dialog sekali dan memasang callback pilihan yang sama seperti sebelumnya. Membuka ulang memakai instance yang sama dalam dialog Rawat Inap tersebut.
- Registrasi listener dari Billing dan Kamar Inap ditampung sampai dialog pencarian pertama kali dibuat. Pendaftaran listener tidak lagi memicu pembuatan dialog. Callback pilihan pasien bayi, filter dokter, dan refresh billing dipertahankan.
- Nama dokter, nama pegawai, dan jabatan pada pengisian awal dibaca langsung dengan kode yang sama dari tabel dokter/pegawai; tidak perlu membuat form pencarian atau memuat ulang seluruh cache untuk memperoleh satu nama. Nilai kini berasal langsung dari database, bukan kemungkinan salinan cache lama.
- Tab SBAR dan Surat Keterangan Lahir mula-mula berisi wadah kosong. Panel lengkap dibuat sekali pada pemilihan pertama. Setiap kali tab dikunjungi, `setKonteks(TNoRw.getText())` tetap dipanggil agar konteks sesuai pasien aktif.
- Query/filter/urutan SOAP, gabungan rawat jalan dan inap, aturan simpan/edit/hapus, hak akses, serta perilaku membuat jendela Rawat Inap baru tetap dipertahankan. Tidak menerapkan penyempitan filter RM atau penggunaan ulang jendela utama.

Masih ada inisialisasi komponen utama dan pembacaan konfigurasi akun pada konstruktor. Hasil ini mengurangi beban sebelum tampil, bukan menjamin nol jeda. Pertama kali membuka pencarian atau tab tambahan masih membutuhkan waktu persiapan.

## Verifikasi

Kompilasi source yang terpengaruh serta pemanggil Billing/Kamar Inap berhasil. Uji `RanapLoadingReadTest` tanpa database berhasil. Kelima kelompok callback pilihan dibandingkan dengan backup dan identik selain indentasi. Handler Simpan/Hapus/Edit identik dengan backup tahap sebelumnya. Tidak ditemukan akses field pencarian lama dari pemanggil lain di source. Pengujian GUI/database nyata masih perlu dilakukan; JAR distribusi tidak diganti selama pengerjaan ini.

Build/Run ulang keseluruhan proyek di NetBeans (Rawat Inap, Billing, dan Kamar Inap harus dibangun bersama karena akses dialog pencarian sekarang melalui getter/metode registrasi listener).

Uji pada data uji:

1. Buka Rawat Inap dari nomor rawat: identitas dan pemuatan awal tetap sesuai pasien.
2. Buka pencarian pasien, pegawai, dokter, petugas, tindakan biasa/beberapa waktu, serta riwayat SOAP. Pilih dan tutup, lalu ulangi. Pastikan hasil masuk ke kolom yang sama dan tidak ada callback berulang akibat pembukaan kedua.
3. Dari Kamar Inap, uji pemilihan pasien bayi dan filter dokter; dari Billing, buka tindakan dan pastikan daftar billing diperbarui seperti sebelumnya setelah dialog ditutup.
4. Buka SBAR dan Surat Keterangan Lahir, pindah tab, lalu buka lagi. Periksa konteks pasien, data tersimpan, dan hak akses. Ulangi pada pasien lain, termasuk kasus gabung ibu/bayi.
5. Uji menggunakan akun dengan hak akses berbeda; uji simpan/edit hanya pada data uji.

## Pemulihan

Backup sebelum tahap kedua: `tmp/backup-ranap-lazy-20260907-101013` (DlgRawatInap.java, DlgKamarInap.java, DlgBilingRanap.java). `restore.ps1` memulihkan kondisi sebelum penundaan dialog ini, termasuk tetap mempertahankan perubahan SwingWorker tahap pertama. Script memeriksa hash file sebelum menyalin agar perubahan lanjutan tidak tertimpa.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tmp/backup-ranap-lazy-20260907-101013/restore.ps1
```

Setelah pemulihan, Build/Run ulang. Jika ingin membatalkan juga tahap pertama, setelah tahap kedua dipulihkan ikuti script pada `docs/ranap-loading-20260907.md`.

Versi GitHub pembanding berbeda dari modifikasi khusus proyek ini. Backup lokal memberikan pemulihan persis ke kondisi sebelumnya; mengganti seluruh file dengan versi GitHub bukan pemulihan yang setara.
