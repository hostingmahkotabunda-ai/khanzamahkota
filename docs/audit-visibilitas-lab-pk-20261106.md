# Audit visibilitas pengiriman Lab PK

Pemeriksaan 6 November 2026 menggunakan database workspace dengan koneksi
read-only. Tidak ada perubahan data, migrasi, atau pengiriman ke SATUSEHAT.
Rentang pemeriksaan diasumsikan 1 Oktober–6 November 2026; rentang filter
yang sedang dipilih operator pada aplikasi tidak dapat dilihat. Pemeriksaan
ulang dengan akhir 3 November memberikan hasil yang sama.

## Hasil

Query ServiceRequest tanpa kata pencarian menghasilkan 1.037 item order,
66 nomor order, pada 56 nomor rawat. Semua kunjungan tersebut berstatus Ranap.
Penyaringan memakai tanggal registrasi, sesuai implementasi base class.

Pada seluruh item kandidat tersebut:

- Tidak ada baris ServiceRequest lokal yang cocok dengan kombinasi noorder,
  id_template, dan kd_jenis_prw.
- Tidak ada baris Specimen, Observation, atau DiagnosticReport lokal yang cocok.
- Tidak ada nota_jalan maupun nota_inap untuk nomor rawat terkait.

Query daftar asli Specimen, Observation, dan DiagnosticReport diekstrak dari
metode tampil(), tanpa kata pencarian, lalu dihitung dengan SELECT count(*)
dan seluruh JOIN/filter dipertahankan. Kedua cabang nota_jalan/nota_inap
menghasilkan nol pada masing-masing modul.

Jumlah tabel global berbeda dari hasil kecocokan kandidat: ServiceRequest
79 baris, Specimen 16 baris, Observation 0, DiagnosticReport 0. Karena itu
tidak tepat mengatakan seluruh database belum pernah menyimpan ServiceRequest
atau Specimen. Ketiadaan baris cocok juga tidak membuktikan resource tidak
ada di server; ID yang gagal tersimpan atau data dari lingkungan lain belum
diselidiki. Audit ini tidak melakukan pencarian API.

## Penyaring kode

- SatuSehatKirimServiceRequestLabBase.tampil(): tanggal registrasi, Encounter,
  detail order, template, mapping; tidak mensyaratkan nota. ID ServiceRequest
  memakai LEFT JOIN sehingga calon yang belum memiliki ID tetap tampil.
- SatuSehatKirimSpecimenLabPK.tampil(): INNER JOIN ServiceRequest dengan tiga
  kunci item, serta INNER JOIN nota dan filter tanggal nota.
- SatuSehatKirimObservationLabPK.tampil(): INNER JOIN Specimen, Encounter,
  pemeriksaan/hasil dengan kecocokan tanggal/jam dan dokter perujuk, detail
  hasil, pegawai dokter, serta nota dan tanggal nota.
- SatuSehatKirimDiagnosticReportLabPK.tampil(): INNER JOIN ServiceRequest,
  Specimen, Observation, pemeriksaan yang cocok, saran_kesan_lab, pegawai,
  serta nota dan tanggal nota.

## Implikasi

Ada dua hambatan independen pada pintu awal: ID ServiceRequest item belum
tersimpan cocok dan nota tidak tersedia. Pengiriman ServiceRequest yang
berhasil saja belum menghilangkan penyaring nota. Jangan membuat nota atau
mengubah data klinis hanya untuk meloloskan daftar. Jika alur pelayanan
memerlukan pengiriman sebelum nota, query modul lanjutan perlu diadaptasi
dengan sumber tanggal yang disepakati dan tetap mempertahankan referensi
FHIR serta kecocokan hasil yang benar.

Belum diaudit: kelengkapan seluruh nilai hasil, kode sampel, kesan, identitas
FHIR di server, dan semua hambatan tambahan yang mungkin muncul setelah
dua penyaring awal diselesaikan. Tidak ada perubahan kode aplikasi pada audit ini.

## Pembaruan setelah pengiriman ServiceRequest dan izin melepas nota

Pemeriksaan ulang menemukan 919 dari 1.037 item mempunyai baris ServiceRequest
lokal yang cocok. Sisa 118 item belum mempunyai baris yang cocok; audit ini
tidak menyimpulkan seluruhnya gagal kirim. Jumlah tabel global saat pemeriksaan
ulang: ServiceRequest 998, Specimen 16, Observation 0, DiagnosticReport 0.

Atas instruksi pengguna, ketiga metode tampil() Lab PK diubah:

- Lepas INNER JOIN nota dan gunakan tanggal registrasi, sama seperti ServiceRequest.
- Gabungkan dua query Ralan/Ranap menjadi satu agar baris tidak terduplikasi.
- Tetap wajibkan JOIN dan ID upstream tidak kosong. Mapping serta sumber
  waktu pengambilan/hasil, isi payload POST/PUT, dan konfigurasi tidak diubah.
- Observation juga mencocokkan id_template dan kd_jenis_prw hasil dengan item
  order, termasuk jenis pemeriksaan header. DiagnosticReport mencocokkan jenis
  pemeriksaan header. Ini mencegah hasil parameter lain ikut terpasang akibat
  JOIN yang sebelumnya hanya memakai kunjungan dan waktu.

Dengan query baru, SELECT baca saja menghasilkan Specimen 919 item,
Observation 0, DiagnosticReport 0 untuk rentang registrasi 1 Oktober–6 November.
Dua tahap terakhir belum mempunyai rantai resource lokal yang cocok.
Tidak ada query yang menulis database atau pengiriman resource dijalankan.

Kompilasi tiga kelas dialog berhasil dengan source/target Java 8 pada JDK 15
(peringatan bootstrap/unchecked). Tiga belas tes query SQL sebenarnya dengan
fixture H2 sintetis lulus: tanpa tabel nota, resource upstream hilang/kosong,
tanggal registrasi di luar rentang, template/jenis/waktu hasil berbeda, dan
kesan tidak tersedia. H2 hanya diperlukan untuk pengujian, bukan runtime aplikasi.

Contoh menjalankan setelah kompilasi test ke tmp/compile-lab-pk-visibility,
dengan artefak H2 2.2.224 dari Maven Central di tmp/h2-2.2.224.jar:

```powershell
java '-Djava.awt.headless=true' -cp 'tmp/compile-lab-pk-visibility;tmp/h2-2.2.224.jar;lib/*;lib/hamcrest/*' org.junit.runner.JUnitCore SatuSehatLabPKVisibilityTest
```

Dasar referensi: [Specimen](https://satusehat.kemkes.go.id/platform/docs/id/fhir/resources/specimen/),
[DiagnosticReport](https://satusehat.kemkes.go.id/platform/docs/id/fhir/resources/diagnostic-report/),
dan [alur Rawat Inap](https://satusehat.kemkes.go.id/platform/docs/id/interoperability/rawat-inap-new/).
Dokumentasi mengaitkan resource dengan permintaan, spesimen, hasil, dan encounter;
nota lokal tidak disebut sebagai prasyarat pada alur tersebut. Ini mendasari
pelepasan penyaring administratif, bukan jaminan bahwa semua payload lama akan
diterima validator. Format/kelengkapan payload dan penerimaan server tetap belum
diverifikasi dengan POST/PUT. Build JAR dan uji manual aplikasi belum dilakukan.

## Hubungan identitas dokter PJ

Setelah pengiriman Specimen, 866 item mempunyai Specimen dan Encounter lokal.
Header hasil cocok pada seluruh item tersebut, tetapi hanya 344 yang mempunyai
detail hasil sesuai template, jenis, dan waktu order. Seluruh item tersaring
oleh INNER JOIN periksa_lab.kd_dokter=pegawai.nik. Dokter PJ tercatat di master
dokter, tetapi kode dokter tidak ditemukan sebagai kode pegawai. Kandidat
pegawai dengan nama yang mirip tidak boleh dijadikan penghubung otomatis.

Observation dan DiagnosticReport kini memakai LEFT JOIN untuk menampilkan
nama dokter PJ yang benar dari master dokter meskipun pegawainya belum
terhubung. Identitas untuk kirim diambil melalui SatuSehatLabDoctorIdentity:

- Hubungan kode yang sama tetap didukung.
- Bila kode berbeda, operator memilih baris pemeriksaan, menekan
  **Hubungkan Dokter PJ**, mencari dan memilih pegawai orang yang sama,
  lalu menyimpan. Dialog tidak memilih kandidat otomatis.
- Penghubung berisi kode dokter dan kode pegawai saja, tanpa NIK, pada
  cache/satusehat-lab-doctor.iyem. File dipakai bersama kedua modul, ditulis
  atomik dengan backup, dan tidak mengubah catatan pemeriksaan/master database.
- Nama/kode dokter klinis tetap dipertahankan. NIK diambil dari pegawai
  yang dipilih. NIK kosong ditampilkan sebagai belum lengkap pada footer;
  ID Patient/Practitioner kosong atau format tidak valid ditahan sebelum
  POST/PUT, dengan status footer tanpa popup per item.
- Berkas penghubung rusak tidak ditimpa. Pegawai yang sudah dihapus tidak
  diganti otomatis dengan kandidat berdasarkan nama.

Query baca saja setelah perubahan menghasilkan Observation 344 item dan
DiagnosticReport 0 (Observation belum tersimpan). Ini adalah hasil query,
bukan bukti uji UI aplikasi atau penerimaan server. Sebanyak 522 item lainnya
belum mempunyai detail hasil yang cocok menurut tiga kunci tersebut; belum
disimpulkan bahwa hasil klinisnya sama sekali tidak ada.

Kompilasi helper dan ketiga dialog berhasil. Total 22 tes H2 sintetis lulus:
14 tes query dan 8 tes identitas/persistensi/referensi. Untuk menjalankan:

```powershell
java '-Djava.awt.headless=true' -cp 'tmp/compile-lab-pk-visibility;tmp/h2-2.2.224.jar;lib/*;lib/hamcrest/*' org.junit.runner.JUnitCore SatuSehatLabPKVisibilityTest SatuSehatLabDoctorIdentityTest
```

Belum ada penghubung pegawai produksi dipilih/disimpan, perubahan data klinis,
migrasi, atau pengiriman resource API. Operator tetap perlu memastikan pegawai
yang dipilih adalah dokter PJ pada catatan pemeriksaan yang sama. JAR aplikasi
belum dibangun dan tampilan belum diuji manual.
