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
