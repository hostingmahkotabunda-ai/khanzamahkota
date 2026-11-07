# Daftar Encounter selesai tanpa syarat cetak nota

Perubahan terbatas pada halaman manual SatuSehatKirimEncounter dan helper baru
SatuSehatEncounterCompletion. Konfigurasi, tabel, data klinis, dan alur auto-service
tidak diubah. Tidak ada migrasi atau POST/PUT yang dijalankan saat pengerjaan.

## Syarat daftar sesuai instruksi pengguna

- Rawat jalan: status_lanjut Ralan, status_bayar **Sudah Bayar**, tidak Batal,
  dan tidak ada kamar_inap aktif pada nomor rawat yang sama. Status Sudah
  diperiksa atau tombol waktu tunggu tidak dipakai sebagai bukti selesai.
- Rawat inap: status_lanjut Ranap, segmen kamar terakhir memiliki status pulang
  nyata dan tanggal keluar, serta tidak ada segmen kamar aktif. Status kosong,
  tanda '-', dan Pindah Kamar tidak dianggap pulang. Rawat inap tidak
  mensyaratkan sudah bayar sesuai instruksi pengguna.
- Filter memakai tanggal registrasi seperti label halaman. Mapping lokasi,
  master pasien/dokter/poli, dan relasi ID Encounter tetap dipertahankan.
- Dua cabang query eksklusif berdasarkan Ralan/Ranap. Waktu kamar dan waktu
  pembayaran diambil sebagai subquery satu nilai agar riwayat kamar/nota tidak
  menduplikasi kunjungan.

## Sumber waktu dan payload

Rawat inap memakai waktu masuk kamar pertama untuk mulai layanan dan waktu
keluar segmen terminal terakhir untuk pulang. Jam 00:00:00 pada tanggal keluar
yang valid tetap boleh dipakai; hanya tanggal nol yang tidak dianggap keluar.

Rawat jalan memakai SOAP pertama sebagai waktu mulai layanan. Waktu akhir
diambil dari tanggal/jam keluar resume medis tersimpan yang lebih akhir dari
registrasi; jika tidak tersedia, gunakan waktu transaksi pembayaran tersimpan
di nota_jalan. Nota bersifat sumber waktu opsional, bukan JOIN wajib atau
syarat tampil. Tidak ada waktu yang diganti menjadi sekarang atau ditambah
durasi perkiraan. Resume V2 dapat tidak tersedia; metadata diperiksa dengan
SELECT, tanpa membuat tabel. Kunjungan sudah bayar tanpa kedua sumber waktu
tetap tampil tetapi pengirimannya ditahan hingga waktu yang benar tersedia.
Waktu pembayaran sebagai akhir Ralan mengikuti penanda selesai yang ditetapkan
pengguna; ini bukan klaim bahwa pencetakan nota adalah kejadian klinis pulang.

Sebelum pencarian identitas, pembuatan episode ANC, dan pengiriman, status
terbaru dibaca ulang dari database. Pembayaran yang dibatalkan atau pasien
yang kembali aktif tidak lolos berdasarkan isi tabel UI yang sudah lama.
Waktu nyata dikonversi dari WITA ke UTC, diperiksa urutan dan batas waktunya,
serta ditahan jika kosong/tidak valid/di masa depan. Parsing tanggal lokal
strict agar tanggal tidak valid tidak dinormalisasi otomatis.

Payload final memiliki status finished, period.start/end, dan riwayat
arrived → in-progress → finished berdasarkan waktu registrasi, mulai layanan,
dan akhir yang tersedia. Tanpa waktu mulai layanan/pulang, tidak mengarang
transisi dan tidak mengirim. Riwayat finished memakai start dan end yang sama
pada waktu pulang, mengikuti koreksi berdasarkan respons validator server.

PUT tetap GET resource terbaru, memeriksa pasien/organisasi, mempertahankan
episode ANC/PTM dan identifier melalui helper lama, serta memakai If-Match.
Merge juga mempertahankan field klinis server lain, termasuk diagnosis dan
riwayat transisi sebelumnya. Status server cancelled/entered-in-error,
perbedaan awal/akhir final, atau riwayat tidak konsisten ditahan untuk ditinjau.
Status histories server yang sudah ada tidak dibuat ulang dengan perkiraan.

## Notifikasi

Popup validasi identitas dan kegagalan Kirim/Update dihapus dari alur pengiriman.
Status bawah halaman menampilkan jumlah berhasil dan belum selesai; tooltip
menampilkan rincian validasi/OperationOutcome. Baris gagal tetap dicentang.
POST baru dianggap selesai di UI sesudah ID tersimpan lokal, PUT setelah
respons berhasil. Dialog tindakan pengguna untuk ANC/PTM tidak dihilangkan.
Logging payload/respons klinis mentah dari handler Encounter dihapus; helper
pencarian identitas lama masih mempunyai logging tersendiri dan tidak diubah.

## Verifikasi

Kompilasi helper dan dialog berhasil, source/target Java 8 pada JDK 15;
peringatan bootstrap/unchecked. Sebanyak **71 tes** lulus: 23 tes baru query
H2/payload selesai/merge dan 48 tes identitas serta integrasi/payload episode
yang sudah ada. Fixture mencakup sudah bayar tanpa nota, belum bayar dengan
nota, pasien aktif, pindah kamar, segmen aktif setelah riwayat pulang,
pemulangan tanpa nota/belum bayar, tengah malam, tanggal keluar nol, filter
tanggal/kata pencarian, waktu kosong/terbalik/future, dan preservasi klinis PUT.

```powershell
java '-Djava.awt.headless=true' -cp 'tmp/compile-episode;tmp/h2-2.2.224.jar;tmp/compile-prepush-satusehat;lib/*;lib/hamcrest/*' org.junit.runner.JUnitCore SatuSehatEncounterCompletionTest SatuSehatEpisodeIntegrationTest SatuSehatEpisodePayloadTest SatuSehatEncounterIdentityTest
```

SELECT baca saja pada database workspace, rentang registrasi 1 Oktober–7
November 2026, menghasilkan Ralan **903**, Ranap **129**, termasuk **125 Ranap
tanpa nota**. Jumlah kamar aktif pada hasil kedua cabang **0**. Ini jumlah
yang lolos query daftar dengan master/mapping tersedia, bukan jumlah resource
yang siap atau sudah diterima SATUSEHAT. ID, urutan waktu, dan kelengkapan
klinis diperiksa lagi saat kirim. Identitas pasien tidak dicantumkan.

JAR aplikasi dan uji manual UI belum dilakukan. Tidak ada pengiriman ulang
produksi. Status selesai/riwayat payload harus tetap mengikuti hasil validasi
profil use case server; penerimaan endpoint belum diuji.

Referensi yang diperiksa:
- https://satusehat.kemkes.go.id/platform/docs/id/fhir/resources/encounter/
- https://satusehat.kemkes.go.id/platform/docs/id/interoperability/rawat-inap-new/

## Koreksi berdasarkan respons validator 10122 dan 10457

Pengguna memberikan respons server: setiap statusHistory.period harus memiliki
start dan end (10122), serta Encounter.diagnosis tidak ditemukan (10457).
Lookup Patient dan Practitioner masing-masing berhasil. Penyebab ini berbeda
dari lookup identitas kosong dan tidak terkait dengan pelepasan syarat nota.

Implementasi awal di dokumen ini masih menghilangkan end pada status finished
dan langsung mengirim finished tanpa diagnosis. Itu sudah dikoreksi. Tes awal
berfokus pada urutan waktu/preservasi dan belum menangkap dua kewajiban tersebut.

- finished mempunyai period.start=end=waktu pulang tercatat, tanpa menambah
  detik/perkiraan baru. PUT menutup finished lama yang end-nya belum ada.
- SatuSehatEncounterDiagnosis mengambil seluruh diagnosis pelayanan yang sama
  dari diagnosa_pasien dan referensi satu_sehat_condition, memakai rank dari
  prioritas, serta peran DD/Discharge diagnosis. Tidak memilih kode klinis baru.
  Diagnosis utama, rank/ref yang valid/unik, dan seluruh Condition terkait
  harus tersedia sebelum finalisasi. Kondisi dari nomor rawat atau status
  Ralan/Ranap lain tidak dipinjam untuk meloloskan validasi.
- Karena Condition memerlukan ID Encounter, POST tanpa rangkaian Condition
  lengkap disiapkan sebagai arrived, dengan riwayat arrived start/end yang
  diketahui, tanpa period.end final atau diagnosis palsu. Setelah ID awal
  tersimpan: kirim Condition, lalu Update Encounter untuk finalisasi finished.
  Jika Condition lengkap sudah tersedia, POST dapat membentuk payload final.
- PUT tetap ditahan bila diagnosis belum siap. Riwayat arrived dari tahap
  awal dipertahankan; in-progress dari waktu pelayanan yang tercatat dan
  finished ditambahkan tanpa menggandakan riwayat lama. Konflik waktu server
  ditahan. Episode/identifier ANC/PTM dan field klinis lain tetap dipertahankan.
- Query daftar Condition juga dilepas dari nota, memakai tanggal registrasi,
  ID Encounter tidak kosong, dan diagnosis.status sesuai status_lanjut. Ini
  memungkinkan tindak lanjut bagi kunjungan tanpa nota. Kolom tanggal pulang
  yang sebelumnya mengambil nota tidak diisi dengan waktu perkiraan. Payload
  Condition dan kode diagnosis klinis tidak diubah.

Kompilasi helper, dialog Encounter, dan Condition berhasil. **87 tes** lulus:
tes sebelumnya beserta regresi end seluruh riwayat, tahap bootstrap→Condition→
finalisasi, diagnosis hilang/parsial, status/kunjungan berbeda, prioritas dan
referensi tidak valid, serta daftar Condition tanpa nota. Hanya fixture H2
sintetis; tidak ada POST/PUT, migrasi, atau perubahan data klinis dijalankan.
JAR dan penerimaan endpoint setelah koreksi belum diverifikasi.

```powershell
java '-Djava.awt.headless=true' -cp 'tmp/compile-episode;tmp/h2-2.2.224.jar;tmp/compile-prepush-satusehat;lib/*;lib/hamcrest/*' org.junit.runner.JUnitCore SatuSehatEncounterCompletionTest SatuSehatEncounterDiagnosisTest SatuSehatEpisodeIntegrationTest SatuSehatEpisodePayloadTest SatuSehatEncounterIdentityTest
```
