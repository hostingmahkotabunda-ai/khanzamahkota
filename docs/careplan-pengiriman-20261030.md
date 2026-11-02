# Hasil pengiriman CarePlan 1–30 Oktober 2026

Pengiriman langsung ke API produksi SATUSEHAT menggunakan konfigurasi lokal
`setting/database.xml`, atas instruksi pengguna. Kredensial tidak ditampilkan
atau disalin ke laporan. Waktu pemeriksaan menggunakan WIB (`+07:00`), sesuai
konfirmasi pengguna.

## Cakupan dan hasil

Rentang mengikuti filter menu CarePlan: **tanggal registrasi** 1–30 Oktober
2026, untuk pemeriksaan dengan RTL tidak kosong. Ini bukan filter tanggal
pembuatan CarePlan. Hasil merupakan snapshot database saat pemeriksaan.

| Kelompok | Jumlah catatan |
|---|---:|
| RTL rawat jalan dalam rentang registrasi | 1.161 |
| RTL rawat inap dalam rentang registrasi | 2.504 |
| Catatan dengan ID Encounter lokal, diperiksa ke API | 604 |
| CarePlan baru berhasil dibuat dan ID lokal terverifikasi | **10** |
| Tidak diproses karena pencarian dibatasi consent/privasi | 530 |
| Tidak diproses karena NIK petugas tidak valid | 45 |
| Tidak diproses karena pencarian Practitioner tidak menghasilkan satu identitas pasti | 19 |
| Catatan tanpa ID Encounter lokal, tidak dikirim | 3.061 |

Catatan tanpa ID Encounter terdiri dari 557 rawat jalan dan 2.504 rawat inap.
Tidak ada CarePlan lama yang diperbarui atau dipulihkan ID-nya dalam eksekusi
ini. Sepuluh keberhasilan semuanya merupakan pengiriman baru.

## Pemeriksaan sebelum pengiriman

- Memastikan Encounter milik organisasi pada konfigurasi.
- Mencocokkan hasil pencarian Patient berdasarkan NIK dengan subjek Encounter.
- Memastikan NIK petugas menghasilkan satu Practitioner.
- Mencari CarePlan berdasarkan subjek dan Encounter, termasuk pagination.
- Melewati hasil yang menyertakan OperationOutcome `suppressed` dengan pesan
  `No consent available` atau hasil yang tidak dapat dicocokkan dengan pasti.
- Mengirim hanya calon yang tidak memiliki CarePlan cocok dalam hasil pencarian
  lengkap yang dapat diakses, setelah pemeriksaan ulang sebelum POST.
- Membentuk JSON dengan serializer, termasuk `identifier` sebagai array.
- Mencatat keberhasilan respons server dan menyimpan ID ke
  `satu_sehat_careplan`; verifikasi baca ulang memastikan 10 ID sesuai.

Satu respons 429 muncul pada audit awal; proses dihentikan, frekuensi dikurangi,
dan catatan yang terkena 429 diperiksa kembali. Tidak ada error 429 tersisa pada
rekap akhir. Tidak dilakukan pembuatan Encounter, perubahan data klinis sumber,
perubahan NIK, atau perubahan consent.

## Yang masih diperlukan

- Untuk 530 catatan, selesaikan pembatasan akses/consent melalui pengelola
  SATUSEHAT yang berwenang, atau gunakan cadangan mapping lokal yang tepat
  sebagai bahan pemulihan ID. Adanya resource tersembunyi pada suatu kunjungan
  tidak cukup untuk memastikan ID CarePlan yang cocok bagi setiap catatan.
- Periksa NIK/registrasi Practitioner bagi 64 catatan yang bermasalah.
- Lengkapi atau pulihkan mapping Encounter sebelum memproses 3.061 catatan
  lainnya. Tidak otomatis membuat Encounter dalam pekerjaan ini.

## Jejak lokal

Log detail berisi data pasien dan disimpan lokal, serta dikecualikan dari Git
melalui `.git/info/exclude`:

- `tmp/careplan-october-audit-1793345346376.jsonl`
- `tmp/careplan-october-audit-1793345556700.jsonl`
- `tmp/careplan-october-apply-1793345526435.jsonl` — 2 berhasil
- `tmp/careplan-october-apply-1793346638786.jsonl` — 8 berhasil
- `tmp/careplan-october-journal.jsonl` — rencana dan hasil respons sebelum
  pencatatan ID lokal

Utilitas pemeriksaan/pengiriman berada di `tmp/CarePlanOctoberSync.java`;
verifikasi ID lokal di `tmp/CarePlanOctoberVerify.java`. Tidak ada proses
pengiriman yang masih berjalan setelah penyelesaian ini.
