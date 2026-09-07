# Diagnosis masuk otomatis pada resume V2

Resume Ranap V2 sebelumnya membaca diagnosis awal melalui `isRawat()`, kemudian mengosongkannya saat `muatDataJikaAda()` menemukan bahwa resume belum tersimpan. Pengisian kini dilakukan setelah pemuatan/pengosongan resume, melalui helper bersama `DiagnosaMasukResume`.

Resume Rawat Jalan yang dibuka dari DlgRawatJalan adalah `RMResumeMedisRalanV2`. Kolom Diagnosa Masuk beserta field ICD 10 yang sebelumnya disembunyikan ditampilkan kembali dan memakai sumber diagnosis yang sama.

- Sumber: `kamar_inap.diagnosa_awal`, yaitu kolom Diagnosa Awal Masuk pada DlgKamarInap.
- Pencocokan menggunakan `no_rawat` yang sama melalui parameter JDBC. Tidak mengambil diagnosis kunjungan lain berdasarkan nomor RM.
- Jika ada beberapa riwayat pindah kamar, ambil diagnosis pertama yang tidak NULL/kosong menurut tanggal masuk, jam masuk, lalu kode kamar.
- Resume baru, resume lama dengan diagnosis kosong, dan tombol Baru mengisi diagnosis dari sumber tersebut. Diagnosis resume yang sudah terisi tidak ditimpa pada pemuatan.
- Jika nomor rawat belum memiliki data Kamar Inap (termasuk rawat jalan murni), diagnosis tetap kosong. Kolom tetap dapat diedit.
- Kode ICD 10 tidak ditebak dari teks diagnosis awal. Nilai ICD yang telah tersimpan tetap dimuat seperti sebelumnya.
- Pengisian hanya pada form. Penyimpanan tetap melalui Simpan/Ubah; tabel sumber dan data resume tidak diperbarui otomatis. Hak akses, validasi, serta handler penyimpanan tetap.

## Verifikasi

Kompilasi helper dan kedua dialog berhasil dengan JDK 15 target Java 8. `ResumeDiagnosaMasukTest` lulus memakai data buatan dalam HSQLDB memori: pemilihan diagnosis pertama, pindah kamar, isolasi nomor rawat, data NULL/spasi/tidak ditemukan, parameter SQL, serta perlindungan diagnosis yang sudah terisi. Sintaks LIMIT dan TRIM disesuaikan hanya pada adapter pengujian agar cocok dengan HSQLDB 1.8; kode aplikasi memakai sintaks MySQL. Database pasien tidak digunakan pada pengujian ini.

Build/Run ulang proyek dari NetBeans untuk mencoba tampilan. Uji visual kedua halaman dengan data pasien belum dilakukan dalam tahap ini.

## Pemulihan

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tmp/backup-resume-diagnosa-20260907-112845/restore.ps1
```

Script memeriksa hash dan mengembalikan kedua source resume. Helper serta test yang baru boleh dibiarkan karena tidak dirujuk oleh dialog setelah pemulihan. Build/Run ulang setelah pemulihan. Perbaikan DlgRawatInap dari tahap sebelumnya tidak ikut dibatalkan.
