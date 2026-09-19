# Pasien pindah kamar muncul pada daftar Pulang

Kolom `kamar_inap.stts_pulang='Pindah Kamar'` menandai berakhirnya pemakaian kamar lama, bukan pemulangan pasien dari rumah sakit. Filter Pulang pada tampilan dan cetak sebelumnya hanya memeriksa `tgl_keluar`. Akibatnya, riwayat kamar lama masuk daftar Pulang meskipun pasien masih aktif di kamar baru. Log pasien pulang tidak dipanggil oleh proses pindah kamar.

Pasien bisa tidak terlihat pada daftar Belum Pulang karena filter bangsal/lantai masih menunjuk kamar lama. Ada pula celah penyimpanan: proses sebelumnya menutup atau menghapus kamar lama tanpa memastikan kamar baru berhasil disimpan. Kegagalan dapat berasal dari benturan primary key `(no_rawat,tgl_masuk,jam_masuk)`, kesalahan SQL, atau koneksi. Kondisi ini tidak dapat dipastikan untuk pasien tertentu hanya dari tampilan status `Pindah Kamar`.

## Perubahan

- Filter daftar dan cetak Pulang mengecualikan `-`, `Pindah Kamar`, dan nomor rawat yang masih memiliki kamar aktif.
- Empat pilihan pindah memakai transaksi pada koneksi tersendiri. Kamar aktif dan ketersediaan tujuan diperiksa ulang dengan penguncian baris. Perubahan ditolak jika sumber berubah atau lebih dari satu kamar aktif ditemukan.
- SQL penyimpanan berparameter dan jumlah baris hasil perubahan diperiksa. Kegagalan membatalkan perubahan kamar inap serta status tempat tidur. Dialog ditutup setelah commit berhasil.
- Nomor rawat diambil dari form pindah secara konsisten. Pindah dicatat pada `trackersql` bila diaktifkan; tidak ditambahkan sebagai pemulangan pada `log_pasien_pulang`.

Tabel `kamar` dan `kamar_inap` dalam skema repositori memakai InnoDB. Transaksi mengandalkan dukungan tersebut. Perhitungan tarif dan lama inap pada form tetap mengikuti perilaku sebelumnya.

## Memeriksa pasien yang sudah terdampak

Pada daftar Belum Pulang, kosongkan filter bangsal/lantai, lalu cari nomor rawat. Pemeriksaan SQL berikut bersifat baca saja; ganti placeholder dengan nomor rawat terkait:

```sql
SELECT no_rawat, kd_kamar, stts_pulang,
       tgl_masuk, jam_masuk, tgl_keluar, jam_keluar
FROM kamar_inap
WHERE no_rawat = 'NOMOR_RAWAT_YANG_DIPERIKSA'
ORDER BY tgl_masuk, jam_masuk;
```

Jika ada segmen tujuan berstatus `-`, pasien masih aktif. Jika riwayat terakhir `Pindah Kamar` tanpa segmen aktif, periksa kamar tujuan dan waktu pindah sebenarnya sebelum memperbaiki data. Perubahan kode ini tidak otomatis merekonstruksi segmen yang hilang pada kejadian sebelumnya.

## Verifikasi

`RanapPulangFilterTest` menjalankan SQL filter pada HSQLDB in-memory dengan data sintetis. `RanapPindahKamarTest` menguji empat mode dan kegagalan transaksi melalui simulasi JDBC. Keduanya tidak memakai database SIMRS.

Jalankan dari root proyek menggunakan PowerShell:

```powershell
New-Item -ItemType Directory -Force tmp/compile-pindah-kamar-20260919,tmp/empty-sourcepath | Out-Null
javac -encoding UTF-8 -source 8 -target 8 -cp 'build/classes;lib/*' -sourcepath tmp/empty-sourcepath -d tmp/compile-pindah-kamar-20260919 src/fungsi/PindahKamarInap.java src/fungsi/koneksiDB.java src/simrskhanza/DlgKamarInap.java test/RanapPulangFilterTest.java test/RanapPindahKamarTest.java
java '-Djava.awt.headless=true' -cp 'tmp/compile-pindah-kamar-20260919;build/classes;lib/*' RanapPulangFilterTest
java -cp 'tmp/compile-pindah-kamar-20260919;build/classes;lib/*' RanapPindahKamarTest
```

Kompilasi ini memakai kelas proyek yang sudah tersedia pada `build/classes` dan menghasilkan artefak terisolasi di `tmp`. Pengujian antarmuka dan server MySQL operasional belum dilakukan. JAR pada `dist` tidak diperbarui oleh perintah tersebut.
