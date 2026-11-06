# Audit EpisodeOfCare PTM/ANC — 3 November 2026

## Bukti dan batas akses

`AGENTS.md` dicari pada seluruh workspace (termasuk berkas tersembunyi dan
yang diabaikan Git) serta direktori induknya; berkas tersebut tidak ditemukan.
Kode yang dibaca mencakup dialog PTM, Encounter, Atur ANC, helper episode,
pengujian sebelumnya, konfigurasi melalui pembacaan terbatas, dan ApiSatuSehat.

Tidak menjalankan migrasi, mengubah konfigurasi produksi, mengirim POST/PUT,
meminta token, atau membaca resource pasien. Pemeriksaan database hanya
SELECT waktu/zona server pada koneksi read-only. Pemeriksaan endpoint hanya
HEAD tanpa autentikasi dan tanpa isi resource. Contoh di dokumen ini disamarkan.

## Akar masalah yang dibuktikan oleh OperationOutcome

Respons lengkap pengguna berasal dari **POST EpisodeOfCare ANC**:

| Rule | Expression pada respons | Nilai yang dikirim | Temuan |
|---|---|---|---|
| 10404 | `EpisodeOfCare.statusHistory[0].period.start` | `2026-08-26` | Validator menolak tanggal tanpa komponen datetime/zona. |
| 10406 | `EpisodeOfCare.period.start` | `2026-08-26` | Penolakan sama pada awal episode. |
| 10458 | `EpisodeOfCare.identifier[0].system` | `http://sys-ids.kemkes.go.id/episodeofcare/<ORG>/` | Namespace tanpa tanda hubung dan dengan slash akhir ditolak. |

Field `end` tidak ada di payload gagal tersebut; tidak ada bukti rule ini
berasal dari `period.end` atau `statusHistory[].period.end` pada request itu.
Tidak ada respons gagal PTM yang diberikan. Namun, kode PTM memakai pola
tanggal saja dan identifier yang sama sehingga risiko serialisasinya juga ada.

Pesan umum "Future Date or Past Date before 3rd June 2014" tidak membuktikan
HPHT salah. Pada waktu pemeriksaan, Agustus 2026 sudah berlalu dan lebih baru
dari batas 2014. Jam pada saat request gagal dibuat tidak tersedia.

## Perbandingan sumber resmi

Halaman definisi resmi EpisodeOfCare tidak konsisten secara internal: uraian
`identifier.system` memuat `episodeofcare/{org}/`, sedangkan contoh JSON-nya
memakai `episode-of-care/{org}`. Halaman itu juga memberi contoh periode
dengan tanggal saja. Respons validator aktual menolak bentuk pertama.

Contoh JSON resmi dan koleksi Postman SATUSEHAT (Registrasi Jantung dan ANC)
memakai namespace bertanda hubung tanpa slash akhir dan datetime berzona.
Perbaikan mengikuti bentuk contoh tersebut, selaras dengan respons endpoint
yang diberikan pengguna. Ini bukan klaim bahwa seluruh validator FHIR menolak
presisi tanggal; temuan khusus berlaku pada request SATUSEHAT yang diperiksa.

Panduan ANC menyebut awal episode mengikuti HPHT bila tersedia. HPHT tetap
dipakai dan tidak diganti dengan tanggal registrasi/hari ini.

Sumber yang diperiksa:
- https://satusehat.kemkes.go.id/platform/docs/id/fhir/resources/episode-of-care/
- https://satusehat.kemkes.go.id/platform/docs/id/interoperability/anc/
- https://www.postman.com/satusehat/satusehat-public/documentation/maukx58/21-use-case-registrasi-jantung
- https://www.postman.com/satusehat/satusehat-public/request/3wafxmd/get-episodeofcare-dengan-subject-patient-dan-type-anc-dengan-status-active-dan-organization-org-id-faskes-get-eoc-dari-faskes-sendiri

## Asal tanggal dan jalur pengiriman

- PTM POST: nilai awal dialog berasal dari tanggal/jam registrasi yang
  diberikan Encounter atau Condition; operator dapat meninjaunya. Episode
  lokal menyimpan tanggal mulai/selesai sebagai DATE. Sebelumnya input awal
  dipotong ke sepuluh karakter dan memakai hari ini jika input tidak tersedia.
  Pemotongan jam dan fallback hari ini dihapus.
- PTM PUT: GET episode terbaru, pertahankan field lain, perbarui status/periode,
  lalu serialisasikan dan validasi seluruh periode termasuk riwayat status.
  Waktu server yang sudah diketahui dipertahankan bila DATE lokal hanya
  memuat tanggal yang sama. Perubahan status tidak boleh menciptakan akhir
  riwayat sebelum awalnya atau riwayat yang tumpang tindih.
- ANC POST: HPHT dari episode lokal, yang ditinjau di Atur ANC. Usulan awal
  dapat berasal dari asesmen kebidanan. Helper yang dipanggil Encounter
  menyerialisasikan HPHT menjadi datetime dan mengambil organisasi konfigurasi.
- ANC pada tombol Update Encounter: jalurnya tetap memastikan/memakai kembali
  episode ANC lalu melakukan PUT **Encounter**, bukan PUT EpisodeOfCare ANC.
  Tidak ditemukan jalur PUT EpisodeOfCare ANC tersendiri dalam tiga kelas yang
  diperiksa. Uji PUT ANC memakai helper payload generik tanpa menambahkan
  workflow penutupan kehamilan atau pengiriman baru ke aplikasi.

## Aturan serialisasi dan validasi setelah perbaikan

Semua jalur yang membentuk EpisodeOfCare menggunakan helper murni
`SatuSehatEpisodePayload`, dengan Clock yang dapat dipatok pada pengujian.

1. Nilai DATE tanpa jam tetap mempertahankan tanggal klinis. Konvensi
   serialisasinya `yyyy-MM-ddT00:00:00Z`, mengikuti contoh tanggal tanpa jam
   yang diberi datetime pada koleksi SATUSEHAT. Midnight ini adalah penanda
   presisi tanggal, bukan klaim jam kejadian yang tersedia dari asesmen.
2. Datetime lokal tanpa offset diperlakukan sebagai WITA (`Asia/Makassar`),
   mengikuti proyek. Datetime dengan offset mempertahankan instant aslinya.
   Keduanya dikonversi ke UTC, bukan sekadar ditempeli `Z`.
3. Awal/akhir episode dan setiap awal/akhir riwayat status divalidasi terhadap
   interval `[2014-06-03T00:00:00Z, Clock.instant()]`. Batas tersebut dievaluasi
   sebagai instant UTC; contohnya `2014-06-03T08:00:00+08:00` tepat pada batas,
   sedangkan satu detik sebelumnya ditolak.
4. Akhir sebelum awal, riwayat di luar periode episode, riwayat tumpang tindih,
   dan tanggal/jam masa depan ditolak dengan nama field. Tidak dipotong,
   digeser, atau diganti hari ini secara otomatis.
5. PUT memperbaiki identifier namespace lama milik organisasi konfigurasi,
   mempertahankan nilai identifier dan identifier eksternal lainnya. PTM/ANC
   tidak hardcode Organization ID rumah sakit di kode payload.

## Pemeriksaan waktu/lingkungan

Sampel pengukuran 3 November 2026 WITA:

- Komputer/JVM: `2026-11-02T23:17:46.516Z`, JVM `Asia/Makassar`.
- Database: UTC `2026-11-02 23:13:59`, waktu lokal `2026-11-03 07:13:59`,
  zona sesi/global `SYSTEM`.
- HEAD tanpa autentikasi ke base FHIR konfigurasi: HTTP 401; header Date
  gateway `Mon, 02 Nov 2026 23:13:59 GMT`.
- Endpoint FHIR terklasifikasi production HTTPS. Organization ID konfigurasi
  sama dengan Organization ID pada identifier error. Tidak ada ID faskes sumber
  yang dimasukkan ke perbaikan.

Komputer sekitar 227 detik lebih maju daripada database/header gateway pada
sampel ini. Header gateway bukan bukti langsung jam internal validator.
Selisih ini tidak menjelaskan HPHT Agustus sebagai future date, tetapi dapat
mempengaruhi transisi status yang menggunakan waktu komputer saat ini.
Sinkronisasi jam perlu diperiksa administrator sebelum uji pengiriman ulang.
Pekerjaan ini tidak mengubah jam, NTP, zona waktu, atau konfigurasi endpoint.

## Contoh sebelum/sesudah (fragmen tersamarkan)

Sebelum:
```json
{
  "identifier": [{"system": "http://sys-ids.kemkes.go.id/episodeofcare/<ORG>/", "value": "<EPISODE-LOKAL>"}],
  "statusHistory": [{"status": "active", "period": {"start": "2026-08-26"}}],
  "period": {"start": "2026-08-26"}
}
```

Sesudah:
```json
{
  "identifier": [{"system": "http://sys-ids.kemkes.go.id/episode-of-care/<ORG>", "use": "official", "value": "<EPISODE-LOKAL>"}],
  "statusHistory": [{"status": "active", "period": {"start": "2026-08-26T00:00:00Z"}}],
  "period": {"start": "2026-08-26T00:00:00Z"}
}
```

Untuk timestamp yang diketahui, contoh `2026-08-26T09:17:25+08:00` menjadi
`2026-08-26T01:17:25Z`; instant dan jam klinisnya sama.

## Validasi dan hal yang belum diverifikasi

Kompilasi sumber terkait dengan target Java 8 berhasil (peringatan bootstrap
classpath dan unchecked yang ada pada proyek). `git diff --check` bersih.
JUnit: **41 pengujian lulus**, terdiri dari 26 uji tanggal/identifier/payload
dan 15 regresi penggabungan PTM/ANC.

Pengujian menggunakan data sintetis dan Clock tetap: tanggal valid,
masa depan (termasuk jam pada tanggal yang sama), batas 03-06-2014,
akhir sebelum awal, offset lintas hari, urutan riwayat, organisasi dinamis,
perbaikan namespace pada PUT, pelestarian waktu yang sudah diketahui,
serta simulasi payload POST/PUT PTM/ANC. Tidak ada API atau database pada tes.

Penerimaan payload hasil perbaikan oleh validator produksi belum diverifikasi
karena tidak ada POST/PUT yang dijalankan. Kebenaran klinis HPHT, tanggal
diagnosis/registrasi, dan pilihan operator tidak disimpulkan dari log ini.
Jam validator pada saat error awal juga belum diketahui. Uji pengiriman ulang,
perubahan konfigurasi, migrasi dan koreksi data produksi tetap memerlukan
persetujuan tersendiri.
