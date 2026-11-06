# Audit hasil pengiriman catatan dugaan alergi susu sapi

Pemeriksaan dilakukan baca saja: SELECT satu baris pencatatan lokal yang cocok dengan screenshot dan GET resource AllergyIntolerance melalui endpoint dari konfigurasi lokal. Resource ditemukan di SATUSEHAT. Tidak ada identitas pasien atau kredensial disalin ke laporan ini.

Bagian data yang sudah tersimpan:

```json
{
  "clinicalStatus": {"coding": [{"code": "active"}]},
  "verificationStatus": {"coding": [{"code": "confirmed"}]},
  "category": ["food"],
  "code": {"coding": [{
    "system": "http://snomed.info/sct",
    "code": "227183005",
    "display": "Milk protein"
  }]}
}
```

Temuan yang terbukti:

1. Pasangan kode/display tidak sesuai: referensi HL7 mencantumkan `227183005` sebagai **Fish roe**, sedangkan **Milk protein** adalah `10827009` dan **Cow's milk** adalah `3718001`. Pemilihan zat yang tepat tetap mengikuti catatan dan penilaian klinis; jangan otomatis mengganti satu konsep dengan konsep lainnya.
2. Resource tersimpan sebagai `confirmed`, sementara catatan sumber lokal menyebut dugaan/curiga. Dugaan alergi dapat direpresentasikan sebagai `unconfirmed` bila belum terverifikasi klinis. Status final memerlukan peninjauan petugas klinis.
3. Pembentuk payload saat audit (`SatuSehatAllergyPayload.build`) masih menetapkan `confirmed` secara tetap. Perbaikan mapping sebelumnya mempertahankan perilaku ini; kelengkapan mapping tidak membuktikan alergi telah dikonfirmasi.
4. Frasa sumber tidak ditemukan di kamus `.iyem` yang sedang diperiksa. Tidak dapat dipastikan versi aplikasi atau kamus mana yang dahulu menghasilkan kode tersebut hanya dari resource yang tersimpan. Mekanisme lama mempunyai pencarian token, tetapi belum ada bukti bahwa mekanisme itulah yang menghasilkan resource ini.

Keberadaan resource dan ID membuktikan pengiriman diterima; tidak membuktikan bahwa pasangan kode/display atau kepastian klinis sudah benar. Jangan mengandalkan penerimaan API untuk menilai kebenaran mapping.

Tidak ada PUT/POST resource FHIR, perubahan database, perubahan catatan klinis, atau perbaikan resource produksi yang dijalankan pada audit ini. Autentikasi dipakai hanya untuk membaca resource. Alur status verifikasi serta data historis ini masih perlu diperbaiki/ditinjau; audit ini belum menerapkan perubahan tersebut.

## Tindak lanjut implementasi

Pembentuk payload kini memakai pilihan verifikasi per baris, dengan default `unconfirmed` untuk data baru. Update data ber-ID memerlukan pilihan status eksplisit. Catatan yang masih menyebut dugaan tidak boleh dikirim sebagai `confirmed`. Pasangan `227183005 — Milk protein` ditolak oleh validasi lokal, dan pencarian referensi dapat menemukan `3718001 — Cow's milk` untuk istilah susu sapi. Tidak ada resource historis yang diperbaiki atau dikirim ulang otomatis. Status resource yang diaudit di atas tetap merupakan hasil pemeriksaan saat audit, bukan hasil PUT dari perubahan kode ini.

Referensi:

- [SATUSEHAT AllergyIntolerance, verificationStatus](https://satusehat.kemkes.go.id/platform/docs/id/fhir/resources/allergy-intolerance/)
- [HL7 Types of Edible Substances, kode dan display SNOMED](https://terminology.hl7.org/en/ValueSet-edible-substance-type.html)
- [HL7 definisi status verifikasi, termasuk unconfirmed/confirmed](https://terminology.hl7.org/4.0.0/CodeSystem-allergyintolerance-verification.xml.html)
