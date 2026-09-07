# Perubahan Rawat Inap dan Resume, 7 September 2026

## Hasil akhir

- Pembukaan Rawat Inap dari Kamar Inap memuat data pasien lewat worker dengan koneksi baca tersendiri. Dialog pendukung, Resep, SBAR, dan Surat Keterangan Lahir dibuat saat diperlukan. Data tabel dipasang sekaligus dan layout SOAP dihitung sebelum paint.
- Glass pane dan pengunci keyboard melindungi form selama data belum siap tanpa menonaktifkan/mengaktifkan semua komponen. Pengguna telah mengonfirmasi tampilan pembukaan terbaru mulus. Query/filter SOAP, hak akses, serta fungsi Simpan/Edit/Hapus dipertahankan.
- Pilihan Risiko Jatuh Bayi & Anak dan Risiko Jatuh Dewasa dipindahkan ke menu Penilaian Awal. Tab Risiko Jatuh yang terpisah dihapus; pembukaan kedua form tetap memakai handler semula.
- Diagnosa Masuk pada Resume Ranap V2 dan Resume Rawat Jalan diisi dari `kamar_inap.diagnosa_awal` untuk nomor rawat yang sama jika kolom resume kosong. Diagnosis tersimpan tetap dipertahankan. Kolom tersebut ditampilkan kembali pada Resume Rawat Jalan. Tidak ada penyimpanan otomatis ke database.

## Validasi

Seluruh source yang berubah dikompilasi bersama memakai JDK 15 target Java 8. Empat pengujian berikut lulus pada hasil kompilasi gabungan:

- `RanapLoadingReadTest`: kontrak pembacaan SOAP/DPJP/validasi, pembatalan, kegagalan, dan penutupan JDBC.
- `RanapTableUpdateTest`: 1000 baris dalam satu event, sorting, kolom, penanda RALAN, checkbox, dan EDT.
- `RanapInputLockTest`: keyboard, mnemonic, kontrol pemuatan, dan status enabled/editable.
- `rekammedis.ResumeDiagnosaMasukTest`: diagnosis per nomor rawat, pindah kamar, resume terisi, data kosong, dan parameter SQL memakai database memori.

Jalankan Build/Run ulang melalui NetBeans. Uji visual resume dengan data pasien belum dilakukan; konfirmasi visual pengguna berlaku untuk pembukaan Rawat Inap.

## Catatan tahap pengerjaan dan pemulihan

[Loading](ranap-loading-20260907.md), [dialog bertahap](ranap-lazy-20260907.md), [tabel/layout](ranap-paint-20260907.md), dan [penguncian input](ranap-input-20260907.md) mencatat kondisi serta verifikasi pada masing-masing tahap. Perilaku penguncian final dijelaskan dalam catatan penguncian input. [Pengisian diagnosis resume](resume-diagnosa-masuk-20260907.md) menjelaskan aturan sumber diagnosis.

Folder `tmp/` berisi backup, hasil kompilasi, dan diagnostik lokal; folder tersebut tidak termasuk commit. Script pemulihan bertahap yang disebut dalam catatan memerlukan backup lokal itu. Riwayat commit Git tersedia untuk pemulihan pada checkout lain. JAR distribusi tidak diperbarui oleh commit source ini.
