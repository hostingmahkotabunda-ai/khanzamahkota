# Audit daftar pengiriman radiologi, 1 Oktober–3 November 2026

Pemeriksaan dilakukan memakai konfigurasi database workspace pada koneksi
read-only. Tidak ada perubahan data, migrasi, atau pengiriman API.
Tidak ada identitas pasien dicantumkan dalam laporan ini.

## Rekonsiliasi jumlah

- Data pemeriksaan: **30 transaksi**, **32 item**, pada **27 nomor rawat**.
- Rawat jalan: 20 transaksi / 22 item.
- Rawat inap: 10 transaksi / 10 item.
- Permintaan radiologi dengan tanggal permintaan dalam rentang: 32 header.
- Seluruh 32 item mempunyai permintaan dengan jenis pemeriksaan yang sama
  pada nomor rawat terkait. Hasil radiologi juga tersedia untuk semua item.

Query `tampil()` asli, tanpa kata pencarian, dijalankan untuk kedua cabang
Ralan/Ranap pada setiap modul. Hasil aktual:

| Halaman | Jumlah baris |
| --- | ---: |
| ServiceRequest | 1 |
| Specimen | 1 |
| Observation | 1 |
| DiagnosticReport | 1 |

## Penyaring utama

| ID Encounter lokal | Mapping jenis pemeriksaan | Item |
| --- | --- | ---: |
| Belum tersedia | Belum tersedia | 17 |
| Belum tersedia | Tersedia | 12 |
| Tersedia | Belum tersedia | 2 |
| Tersedia | Tersedia | 1 |

Sebanyak **27 dari 30 transaksi**, atau 29 dari 32 item, belum mempunyai ID
Encounter tersimpan lokal. Ini melibatkan 24 nomor rawat. Tidak adanya ID
lokal tidak membuktikan bahwa resource pasti belum ada di server; bila pernah
terkirim, perlu pemeriksaan/pemulihan ID sebelum membuat duplikat.

Sebanyak **19 item** pada **17 transaksi** kekurangan mapping radiologi,
mencakup 9 jenis pemeriksaan. Kelompok ini bertumpang tindih dengan kelompok
yang belum mempunyai ID Encounter; jumlahnya tidak boleh dijumlahkan sebagai
transaksi terpisah.

| Kode lokal | Nama master pemeriksaan | Item belum mempunyai mapping |
| --- | --- | ---: |
| J000004 | USG Scrotum | 1 |
| R003 | Waters (1) | 2 |
| R004B | Skull / Cranium (2) | 1 |
| R013B | Thorax AP/PA LAT | 9 |
| R014B | Abdomen | 2 |
| R022 | Genu | 1 |
| R023 | Crurist | 1 |
| R028 | USG Head/MSK | 1 |
| R030 | HSG | 1 |

Mapping yang ada pada cohort ini hanya mencakup 13 item Thorax AP (R013).
Keberadaan baris mapping diperiksa, bukan penetapan validitas klinis setiap
kode terminologi. Mapping jenis lain harus ditinjau sesuai pemeriksaan
sebenarnya; tidak boleh disamakan otomatis dengan Thorax AP.

## Syarat per halaman

Daftar Data Pemeriksaan bersumber dari `periksa_radiologi`. Daftar pengiriman
memerlukan rantai sumber tambahan melalui INNER JOIN:

1. ServiceRequest: permintaan + detail + mapping + Encounter lokal.
2. Specimen: permintaan + detail + mapping + ID ServiceRequest lokal.
3. Observation: mapping + Specimen + Encounter + pemeriksaan + hasil pada
   tanggal/jam yang cocok, serta kecocokan dokter perujuk.
4. DiagnosticReport: ServiceRequest + Specimen + Observation + mapping +
   Encounter + hasil yang cocok.

Filter waktu juga berbeda: ServiceRequest/Specimen memakai tanggal permintaan,
Observation/DiagnosticReport memakai tanggal hasil. Karena itu jumlah pada
halaman Data Pemeriksaan tidak otomatis sama dengan halaman pengiriman.

Ada **3 item** yang mempunyai hasil pemeriksaan tetapi tidak mempunyai
permintaan dengan jenis pemeriksaan dan tanggal/jam hasil yang cocok persis.
Perlu peninjauan keterkaitan order/hasil sebelum Observation/DiagnosticReport
dikirim. Audit tidak mengubah tanggal/jam atau dokter untuk memaksakan JOIN.

Master dokter perujuk dan pegawai dokter pelapor tersedia pada seluruh item
yang diaudit; keduanya bukan penyaring utama pada kasus ini.

## Tindak lanjut

1. Lengkapi atau pulihkan ID Encounter untuk nomor rawat terkait.
2. Lengkapi mapping 9 jenis pemeriksaan berdasarkan terminologi yang sesuai.
3. Tinjau 3 item dengan keterkaitan tanggal/jam order–hasil yang berbeda.
4. Setelah data prasyarat valid, muat ulang dan kirim berurutan:
   ServiceRequest → Specimen → Observation → DiagnosticReport.

Query belum diubah untuk melewati prasyarat tersebut. Penyelidikan selanjutnya
atau perbaikan tampilan sebaiknya memperlihatkan alasan belum siap kirim,
tanpa membentuk referensi FHIR kosong atau mengarang keterkaitan pemeriksaan.
