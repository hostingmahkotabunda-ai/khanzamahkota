# Perbaikan payload Observation Lab PK

Log pengguna menunjukkan pencarian Patient dan Practitioner masing-masing
mengembalikan satu resource, serta referensi keduanya terisi dalam payload.
HTTP 400 yang diberikan belum disertai OperationOutcome. Karena itu field
yang ditolak server belum dapat dipastikan dan tidak disimpulkan dari nomor
HTTP saja. Tidak ada NIK/identitas pasien disalin ke dokumen ini.

## Kesalahan yang terbukti pada kode/payload

- Regex `(\r|\r||\r)` mengandung alternatif kosong sehingga menyisipkan
  `<br>` di setiap batas karakter pada POST dan PUT. Ini merusak teks hasil.
- Payload belum mempunyai issued. Panduan Rawat Inap mencantumkan issued
  dalam pemetaan Observation; contoh resmi menyertakan field tersebut.
- Identifier memakai namespace servicerequest, sedangkan contoh Observation
  resmi menggunakan observation/{Organization_ID}.
- Datetime lokal diberi +07:00 tetap, sedangkan proyek memakai WITA.
- JSON dirangkai manual sehingga kutip, baris baru, dan backslash dari
  hasil atau display dapat menghasilkan JSON tidak valid.

## Perubahan

SatuSehatLabObservationPayload menjadi builder bersama POST/PUT dengan Jackson.
Teks asli tidak diproses sebagai HTML, tidak dibuang tanda abnormal, dan tidak
dikonversi otomatis menjadi angka. Identifier menggunakan organisasi konfigurasi
dan gabungan order/jenis/template dengan prefiks panjang agar tiap item berbeda.
PUT tetap menyertakan resource ID lama. Referensi kosong/tidak valid ditahan.

Waktu hasil lokal tanpa offset ditafsirkan sebagai Asia/Makassar dan dikonversi
ke UTC. Waktu dengan offset mempertahankan instant. effectiveDateTime dan issued
menggunakan waktu hasil yang tersedia di daftar, bukan waktu komputer/hari ini.
Sumber waktu verifikasi/rilis terpisah belum dibuktikan tersedia; kesesuaian
waktu hasil dengan waktu rilis versi observasi perlu dikonfirmasi pada alur lab.

Contoh sintetis:

```json
{
  "identifier": [{
    "system": "http://sys-ids.kemkes.go.id/observation/test-org",
    "value": "10:order-test9:kind-test10:template-a"
  }],
  "effectiveDateTime": "2026-10-15T08:44:51Z",
  "issued": "2026-10-15T08:44:51Z",
  "valueString": "Hasil uji: teks asli\nbaris berikutnya"
}
```

Blok di atas hanya cuplikan, bukan resource lengkap untuk dikirim. Waktu asal
contoh adalah 2026-10-15 16:44:51 WITA. Identitas contoh seluruhnya sintetis.

Handler Observation menghapus logging payload/response klinis mentah. Error
HTTP dibaca sebagai OperationOutcome; expression/details/diagnostics muncul
sebagai tooltip status, tanpa popup per item. Pencarian identitas melalui
SatuSehatCekNIK masih memiliki logging tersendiri dari kode lama dan tidak
diubah dalam perbaikan terbatas ini.

## Verifikasi dan batasnya

Kompilasi helper dan tiga dialog Lab PK berhasil (JDK 15, target/source 8;
peringatan bootstrap/unchecked). Delapan tes payload baru lulus: POST/PUT,
issued, organisasi konfigurasi, teks dengan karakter khusus JSON, perbedaan
identifier antarparameter, offset, tanggal tidak valid, dan referensi kosong.
Ditambah tes query/identitas sebelumnya, total 30 tes lulus.

```powershell
java '-Djava.awt.headless=true' -cp 'tmp/compile-lab-pk-visibility;tmp/h2-2.2.224.jar;lib/*;lib/hamcrest/*' org.junit.runner.JUnitCore SatuSehatLabObservationPayloadTest SatuSehatLabPKVisibilityTest SatuSehatLabDoctorIdentityTest
```

Tidak ada migrasi, perubahan data klinis/konfigurasi produksi, atau POST/PUT
yang dijalankan. JAR aplikasi belum dibangun dan penerimaan endpoint belum
diverifikasi. Tipe valueString tetap merupakan alur naratif lama; penyesuaian
ke valueQuantity untuk hasil kuantitatif memerlukan pemetaan tipe hasil dan
unit UCUM yang benar, bukan pengambilan angka dari teks gabungan secara otomatis.
Perubahan ini tidak menjamin seluruh kode/tipe hasil diterima validator.

Sumber resmi:
- https://satusehat.kemkes.go.id/platform/docs/id/fhir/resources/observation/
- https://satusehat.kemkes.go.id/platform/docs/id/interoperability/rawat-inap-new/
- https://www.postman.com/satusehat/satusehat-public/documentation/42907689-421ae3ec-01ae-440d-873c-2d425f07ee4f?entity=request-42907689-0c825542-f9f8-4ab3-bcdf-f880c256b620
