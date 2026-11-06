# MedicationStatement SATUSEHAT

Menu **Kirim MedicationStatement Satu Sehat** mengambil riwayat obat dari
`rekonsiliasi_obat` dan `rekonsiliasi_obat_detail_obat`. Setiap obat pada satu
rekonsiliasi merupakan satu MedicationStatement. Data resep tidak otomatis
dianggap sebagai bukti penggunaan obat.

## Penggunaan

1. Build aplikasi, lalu buka menu dengan akun yang mempunyai kedua hak akses:
   **Rekonsiliasi Obat** dan **Kirim Medication SATUSEHAT**.
2. Pilih rentang tanggal wawancara dan tekan **Cari**.
3. Pilih satu baris. Isi **Status Penggunaan** sesuai informasi klinis yang
   terdokumentasi. `Stop`/`Lanjut` pada sumber adalah tindak lanjut rekonsiliasi
   dan tidak otomatis diubah menjadi status penggunaan.
   Klik **Sumber Informasi**: pilih **Pasien sendiri** hanya jika pasien
   memberikan riwayat tersebut; untuk pihak lain isi referensi SATUSEHAT
   `Practitioner/{id}`, `PractitionerRole/{id}`, `RelatedPerson/{id}`,
   `Organization/{id}`, atau `Patient/{id}` yang sesuai. Sumber wajib diisi
   berdasarkan respons validasi SATUSEHAT (RuleNumber 10439), dan tidak
   otomatis diambil dari petugas pencatat. Referensi pihak lain diperiksa
   di server; RelatedPerson harus terkait dengan pasien kunjungan.
4. Untuk obat luar faskes, pilih sumber **KFA (obat luar faskes)**, kemudian
   **Pilih KFA**, atau isi kode produk 92/93 beserta nama resminya. Cocokkan
   obat, kekuatan dan sediaan dengan catatan sumber.
5. Untuk obat dari faskes ini, pilih **Medication (faskes ini)** dan isi
   `Medication/{id}` dari riwayat MedicationDispense pasien. Sistem memeriksa
   referensi serta organisasi pada encounter penyerahan obat tersebut.
6. **Simpan Mapping** menyimpan hasil tinjauan lokal. **Pratinjau JSON**
   memeriksa referensi di server dan menampilkan payload tanpa POST/PUT.
7. **Kirim Baru** mengirim satu catatan yang dipilih. Jika identifier sudah
   ada, ID disinkronkan tanpa membuat data baru. **Update** memperbarui
   catatan yang sudah ada dan menggunakan versi server jika tersedia.

Nama/aturan obat dari rekonsiliasi dipertahankan dalam `dosage.text` dan
`note.text`. Waktu wawancara menjadi `dateAsserted` dengan konversi dari
Asia/Makassar ke UTC. Awal/akhir konsumsi dan sumber informasi tidak ditebak
dari waktu wawancara atau identitas petugas pencatat.

## Penyimpanan dan pemulihan

Tabel tambahan `satu_sehat_medicationstatement_rekonsiliasi` dibuat dengan
`CREATE TABLE IF NOT EXISTS` ketika menu pertama kali memuat data. Akun database
aplikasi memerlukan hak membuat tabel tersebut. Tabel menyimpan mapping/status
yang ditinjau dan ID resource server; tabel rekonsiliasi tidak diubah.
Tabel lama `satu_sehat_medicationstatement` berbasis `no_resep/kode_brng` tidak
diubah. Jika instalasi versi awal modul ini sudah membuat tabel tersebut
dengan kolom `source_key`, modul tetap menggunakannya agar mapping dan ID
yang pernah disimpan tidak hilang.
Kolom `information_source` ditambahkan otomatis jika belum tersedia. Akun
database memerlukan hak ALTER untuk pembaruan ini. Mapping lama tetap ada;
sumber informasinya perlu dilengkapi sebelum pengiriman berikutnya.

Identifier stabil dibentuk dari nomor rekonsiliasi dan nama obat. Duplikasi
nama obat dalam rekonsiliasi yang sama ditolak untuk menghindari pemilihan
catatan yang ambigu. Perubahan sumber sejak daftar dimuat mengharuskan
pemuatan dan peninjauan ulang.

Jika pengiriman terputus setelah server membuat resource, gunakan **Kirim
Baru** pada catatan yang sama untuk pencarian identifier dan pemulihan ID.
Jangan mengganti nama obat/nomor rekonsiliasi sebagai cara mengulang pengiriman.
Pengiriman dari beberapa instalasi aplikasi secara bersamaan tetap perlu
dihindari untuk catatan yang sama; kunci pengiriman hanya berlaku dalam satu
proses aplikasi.

## Verifikasi pengembangan

Kelas baru dan integrasi `frmUtama` dikompilasi dengan target Java 8.
Pengujian `test/SatuSehatMedicationStatementPayloadTest.java` mencakup
escaping JSON, waktu, pilihan medication yang eksklusif, status/mapping
tidak valid, dan kestabilan identifier. Pengujian ini tidak mengirim data
ke SATUSEHAT dan tidak menggantikan uji integrasi pada lingkungan uji.

Rujukan:
- https://satusehat.kemkes.go.id/platform/docs/id/fhir/resources/medication-statement/
- https://satusehat.kemkes.go.id/platform/docs/id/interoperability/rawat-inap-new/
