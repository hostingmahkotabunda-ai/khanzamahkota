# Episode Perawatan PTM dan Atur ANC — Mahkota Bunda

## Status penerapan

Kode dan migrasi disiapkan untuk ditinjau. Migrasi, POST/PUT SATUSEHAT, dan
pengujian data pasien produksi tidak dijalankan dalam pekerjaan adaptasi ini.
Migrasi dan pengiriman ke produksi memerlukan persetujuan pengguna terlebih dahulu.

Endpoint, organisasi, serta token memakai `koneksiDB.URLFHIRSATUSEHAT()`,
`koneksiDB.IDSATUSEHAT()` dan `ApiSatuSehat`, yang membaca konfigurasi
SATUSEHAT Mahkota Bunda di `setting/database.xml`. Tidak ada organisasi,
client ID, atau secret dari faskes lain yang disalin. File konfigurasi yang
sudah ada tidak diubah oleh adaptasi ini.

Pemeriksaan offline menemukan nilai konfigurasi SATUSEHAT pada
`KhanzaHMSServiceSatuSehat/setting/database.xml` berbeda dari aplikasi utama.
Service terpisah **belum siap dijalankan di produksi**: setelah persetujuan
deployment, selaraskan endpoint, organisasi, kredensial dan koneksi database
service dengan konfigurasi Mahkota Bunda yang disetujui. Nilai rahasia tidak
dicantumkan di dokumen ini dan konfigurasi service tidak diganti otomatis.

## Migrasi manual setelah persetujuan

Tinjau definisi tabel yang sudah ada dan cadangkan database sesuai prosedur RS.
Skrip `CREATE TABLE IF NOT EXISTS` tidak memperbaiki skema lama yang berbeda;
jika tabel bernama sama sudah ada, cocokkan kolomnya dahulu.

1. `sql/migrasi_episodeofcare_ptm_satusehat.sql`: episode PTM dan relasi banyak
   episode per nomor rawat.
2. `sql/migrasi_anc_satusehat.sql`: episode kehamilan dan kategori ANC per
   kunjungan. Satu nomor rawat hanya memiliki satu penanda ANC.

Dialog tidak menjalankan DDL otomatis. Jika skema belum tersedia, fitur
terkait memberi informasi migrasi. Jangan menghapus tabel untuk mengulang
migrasi: ID server dan hubungan kunjungan harus dipertahankan.

## Alur PTM

Pada Encounter, pilih baris lalu **Episode PTM**, pilih Condition yang sudah
dikirim untuk kunjungan tersebut. Akses alternatif tetap tersedia melalui
**Episode Perawatan PTM** di menu konteks Condition.

Pilih tipe `CAD`, `CVD`, `CKD`, atau `cancer`, status dan tanggal yang sesuai
catatan klinis. Tipe dan kelayakan episode harus ditentukan menurut use case,
bukan otomatis dari semua diagnosis. **Kirim / Hubungkan** mencari episode
yang sesuai sebelum membuat resource. Pencarian gagal atau hasil ambigu
menghentikan proses; tidak dianggap sebagai alasan membuat episode baru.

Episode yang ditemukan wajib cocok dengan pasien, organisasi dan tipe PTM.
Condition diverifikasi terhadap pasien dan Encounter kunjungan. **Update
Status** menggunakan resource GET terbaru dan mempertahankan field lainnya.
Koneksi episode ditambahkan ke Encounter server tanpa mengganti identifier ANC
atau referensi episode lainnya. Bila pembaruan Encounter gagal sesudah episode
berhasil dibuat, ID lokal tetap tersedia untuk mengulang hubungan.

Dialog menampilkan ICD-10/nama penyakit dari baris Condition yang dipilih dan
rekomendasi beserta alasannya. Akses dari Encounter meneruskan diagnosis yang
dipilih dalam daftar Condition, bukan mengambil sembarang diagnosis pasien.
Rekomendasi awal menggunakan kelompok ICD-10 terbatas:

| Kelompok ICD-10 | Saran awal |
| --- | --- |
| I20–I25 | CAD |
| I60–I69 | CVD |
| N18 | CKD |
| C00–C97 | cancer |

Ini adalah bantuan pemilihan berdasarkan kelompok diagnosis, bukan crosswalk
resmi lengkap atau penetapan kelayakan EpisodeOfCare. Status episode tetap
ditinjau petugas. Nama penyakit dipakai sebagai pemeriksaan konteks/konflik;
jika hanya nama yang cocok tanpa kode pendukung, sistem menampilkan arahan
tetapi tidak mengisi tipe otomatis.

Hipertensi/diabetes memerlukan penilaian kriteria layanan tambahan dan tidak
langsung dipilihkan satu tipe. Diagnosis obstetri ditinjau menurut fase
kehamilan/persalinan/nifas; ANC hanya ditandai jika pelayanan aktualnya ANC.
Diagnosis yang belum tercakup, kode ganda,
indikasi suspek/negasi, atau konflik nama/kode memakai pilihan manual.
Pilihan awal kosong untuk kasus tersebut, sehingga tidak otomatis menjadi CAD.
Petugas tetap dapat mengubah rekomendasi sebelum mengirim.

Untuk lingkup ibu hamil, **Saran alur** dan penjelasan spesifik tetap tampil
meskipun tidak ada tipe PTM yang layak dipilih otomatis:

| ICD-10 | Penjelasan rekomendasi |
| --- | --- |
| Z34/Z35/Z36 | Pemantauan/skrin­ing antenatal: tinjau ANC. |
| O10.2/O10.3 | Penyakit hipertensif ginjal/jantung-ginjal sebelumnya: tinjau penyakit dasar dan kelayakan PTM penyerta. |
| O13/O14/O15 | Hipertensi gestasional/preeklamsia/eklamsia: tidak otomatis CAD, CVD atau CKD. |
| O24.0–O24.3 | Diabetes sebelumnya: tinjau konteks/penyakit dasar, bukan mengasumsikan CKD. |
| O24.4 | Diabetes gestasional: bukan bukti CKD. |
| O99.4 | Kelompok penyakit sirkulasi: perlu penyakit dasar untuk membedakan CAD/CVD atau kondisi lain. |
| O99.0/O23 | Anemia/infeksi genitourinaria pada kehamilan: tidak otomatis empat tipe PTM. |
| O80–O84 | Persalinan: tinjau INC. |
| Z39/O15.2 | Nifas/pascapersalinan: tinjau pelayanan nifas; tidak otomatis ANC. |

Dialog membaca Condition penyerta yang sudah terkirim **pada nomor rawat yang
sama**, tanpa mengganti Condition obstetri yang dipilih. Contohnya, O99.4
disertai I25.1 menampilkan petunjuk CAD; O14.1 disertai N18.5 menampilkan
petunjuk CKD. Untuk membuat episode PTM tersebut, pilih Condition penyakit
dasar yang sesuai. Ketiadaan Condition pendukung juga dijelaskan, bukan
diisi dengan diagnosis atau tipe episode baru. Tombol Atur ANC disediakan
untuk membuka pengaturan bila pelayanan antenatal telah dipastikan; tidak
menyimpan penanda atau mengirim API secara otomatis.

Rekomendasi obstetri ini adalah bantuan pemilihan, bukan crosswalk resmi
SATUSEHAT dari seluruh kode O ke empat tipe PTM. Dasar rujukan:
- https://satusehat.kemkes.go.id/platform/docs/id/interoperability/anc/
- https://satusehat.kemkes.go.id/platform/docs/id/interoperability/skrining-ptm/
- https://classbrowser.nhs.uk/ICD-10-5TH-Edition/vol1/block-o10-o16.htm
- https://classbrowser.nhs.uk/ICD-10-5TH-Edition/vol1/block-o20-o29.htm
- https://classbrowser.nhs.uk/ICD-10-5TH-Edition/vol1/block-o94-o99.htm

Aturan kode: https://icd.who.int/browse10/2019/en
Ketentuan episode: https://satusehat.kemkes.go.id/platform/docs/id/interoperability/skrining-ptm/

## Alur ANC

Pada Encounter, pilih kunjungan lalu **Atur ANC**. Konfirmasi HPHT dan kategori
`K1A`, `K1M`, `K2` sampai `K6`. Saran dari asesmen/riwayat hanya membantu
pengisian dan wajib ditinjau petugas. Saran tidak menentukan pelayanan ANC
berdasarkan nama poli saja.

**Simpan ANC** menyimpan lokal pada transaksi khusus; tidak mengirim API.
Episode kehamilan dipakai kembali untuk kunjungan berikutnya pada kehamilan
yang sama. HPHT episode yang sudah dipilih tidak diedit melalui kunjungan.
Lebih dari satu episode aktif atau HPHT berbeda memerlukan peninjauan data,
termasuk penutupan episode sebelumnya bila memang sudah berakhir.

Setelah persetujuan pengiriman produksi, **Kirim/Update Encounter** memastikan
episode ANC, menyimpan ID server, dan memasukkan:

- `Encounter.episodeOfCare`: referensi episode ANC bersama episode PTM terkait.
- `Encounter.identifier`: kategori ANC dengan system
  `http://terminology.kemkes.go.id/CodeSystem/episodeofcare/ANC`, bersama
  identifier Encounter biasa dan identifier lain yang sudah ada.

Episode ANC server diverifikasi terhadap pasien, organisasi dan HPHT. Status
aktif dari kehamilan berbeda tidak otomatis diambil sebagai pengganti.
Update memakai versi server (`If-Match`) bila tersedia. Jika ada konflik versi,
muat ulang dan tinjau sebelum mengulang; tidak melakukan overwrite paksa.

**Batalkan ANC** membatalkan penanda lokal kunjungan saja. Identifier atau
episode yang sudah dikirim tidak dihapus otomatis dari server. Koreksi data
produksi harus ditinjau khusus, agar tidak menghapus data alur lain.

## Batasan dan pemeriksaan

Fitur memakai hak akses Kirim Encounter dan/atau Kirim Condition yang ada.
Sinkronisasi kehamilan selesai/persalinan dan seluruh data klinis modul ANC
(Observation, tindakan, dan lain-lain) berada di luar adaptasi episode ini.
Jangan menutup episode kehamilan dengan menghapus relasi kunjungan.

Auto Encounter dalam aplikasi utama serta aplikasi service terpisah membaca
relasi PTM/ANC yang sudah siap dari tabel lokal. Bila kunjungan ditandai ANC
tetapi ID episode belum tersedia, pengiriman otomatis dihentikan untuk
kunjungan tersebut dan diarahkan ke dialog Encounter. Service terpisah
mempertahankan relasi/identifier dari GET server saat PUT. Build service
terpisah perlu disertakan dalam deployment; menjalankan biner service lama
masih dapat menghapus relasi dari resource Encounter saat PUT.

Kunci pengiriman ANC dalam aplikasi mencegah pengiriman ganda dalam satu
proses. Hindari pengiriman episode baru yang sama dari beberapa instalasi
secara bersamaan; pencarian server bukan kunci transaksi antar-aplikasi.

Uji `test/SatuSehatEpisodeIntegrationTest.java` hanya memakai JSON sintetis,
tanpa database/API. Cakupan: PTM+ANC bersamaan, update kategori ANC, identifier
lain tetap ada, pengulangan tanpa duplikat, serta penolakan pasien, organisasi,
HPHT dan kode yang tidak sesuai. Pengujian integrasi endpoint dilakukan
terpisah setelah persetujuan dengan konfigurasi lingkungan uji yang sesuai.

Rujukan resmi:
- https://satusehat.kemkes.go.id/platform/docs/id/interoperability/anc/
- https://satusehat.kemkes.go.id/platform/docs/id/interoperability/skrining-ptm/
- https://satusehat.kemkes.go.id/platform/docs/id/fhir/resources/encounter/
