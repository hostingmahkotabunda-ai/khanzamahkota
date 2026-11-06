# Perbaikan mapping AllergyIntolerance

Pemeriksaan kode dan kamus lokal menemukan:

- Penambahan keyword belum berarti mapping selesai: sembilan entri lokal masih berkode `unknown` saat diperiksa. Entri Seafood berkategori `medication`.
- Pencarian cadangan memilih satu token dari catatan yang bisa berisi beberapa alergen. Sebagian alergi dapat terabaikan.
- Update menulis dua properti `code`, sehingga parser dapat menimpa salah satunya. Kedua jalur merangkai JSON manual.
- Waktu pencatatan sebelumnya diberi `+07:00` meskipun lokasi Mahkota Bunda menggunakan WITA.
- Query pencatatan hasil belum membedakan Ralan/Ranap pada join, sementara primary key tabel lokal mencakup status tersebut.

## Perubahan dan penggunaan

Build ulang aplikasi, buka pengiriman AllergyIntolerance, pilih baris dan gunakan **Mapping Alergi** untuk melengkapi kategori, sistem, kode, display dan keterangan berdasarkan zat penyebab yang terdokumentasi. Hak akses mengikuti pengiriman AllergyIntolerance. Mapping disimpan ke `cache/alergisatusehat.iyem` dengan cadangan `cache/alergisatusehat-backup-*.iyem`. Pembacaan daftar tidak menulis file.

**Status Mapping** membedakan belum dimapping, perlu ditinjau, dan lengkap secara lokal. Lengkap secara lokal berarti validasi kelengkapan/lokal berhasil, bukan konfirmasi server terminologi. Klik **Kirim** untuk baris tanpa ID, **Update** untuk baris dengan ID.

Pencocokan mengabaikan kapitalisasi dan menormalkan spasi tanpa menggabungkan kata. Hanya keyword utuh atau alias utuh yang sengaja tercantum dalam kamus yang diterima. Catatan berisi beberapa alergen/reaksi tidak dipecah menjadi satu token lalu dianggap selesai. Petugas perlu meninjau seluruh informasi klinis sebelum membuat mapping; fitur ini belum memecah banyak alergen menjadi beberapa resource.

Kamus bawaan mendukung istilah utuh Seafood, sea food, makanan laut, alergi seafood dan alergi makanan laut sebagai `food`, SNOMED CT `44027008`, display `Seafood`. Ini tidak menyimpulkan jenis ikan/udang tertentu. Mapping valid yang telah dibuat operator dipertahankan. Entri lokal `seafood` sudah diperbaiki dan dicadangkan. Penambahan referensi lokal berikut melengkapi sebagian placeholder lainnya.

## Penambahan referensi lokal 4 November 2026

Pada tahap awal, katalog publik KFA Browser ditelusuri pada tab bahan zat aktif. Lima pasangan kode/nama berikut diverifikasi dari hasil pencarian resmi lalu disimpan ke `.iyem`. Fitur pencarian daring selanjutnya memakai salinan lokal terlebih dahulu, lalu mencari referensi bila belum ada mapping.

| Keyword | Sistem | Kode | Display resmi |
| --- | --- | --- | --- |
| ibuprofen | KFA BZA | 91000107 | IBUPROFEN |
| ranitidine | KFA BZA | 91000375 | RANITIDINE |
| ampicillin | KFA BZA | 91000524 | AMPICILLIN |
| amoxicillin | KFA BZA | 91000237 | Amoxicillin |
| asam mefenamat | KFA BZA | 91000528 | Mefenamic Acid |
| popcorn | SNOMED CT | 226726003 | Popcorn |

Sumber kelima obat adalah [KFA Browser resmi](https://satusehat.kemkes.go.id/kfa-browser/farmasi). Metadata setiap entri KFA mencantumkan URL detail referensi. Alias utuh mencakup ejaan lokal `ibuporfen`, `ranitidin`, `ampisilin`, `ampicilin`, `amoksisilin`, `amoxcillin` serta catatan lama `ranitidin (gatal)` dan `ampicillin (gatal, sesak napas)`. Operator tidak perlu mengisi ulang kode untuk input yang cocok dengan keyword/alias tersebut. Teks bersama `code.text` hanya menyebut nama zat aktif; reaksi dari teks klinis sumber tetap berada pada `note`, sehingga alias yang dipakai pasien lain tidak mewarisi reaksi pasien sebelumnya.

`Popcorn` baru ditemukan dalam kamus selama pengerjaan dan dilengkapi sebagai zat makanan, tanpa menyimpulkan alergi jagung/mentega atau bahan tambahan tertentu. Arti kode diverifikasi melalui [HL7 Types of Edible Substances](https://terminology.hl7.org/en/ValueSet-edible-substance-type.html). Kode Seafood juga ditemukan dalam lampiran referensi alergi yang ditautkan dokumentasi SATUSEHAT.

Panduan [AllergyIntolerance SATUSEHAT](https://dokumentasisatusehat-stg.dto.kemkes.go.id/resource-and-terminologi/daftar-resources-and-terminologi/allergyintollerance) menetapkan KFA BZA/POV/POA untuk obat/vaksin, ATC level 3/4 untuk golongan obat, serta SNOMED untuk makanan/lingkungan. Validator lokal kini membedakan KFA obat (kategori medication, prefiks 91/92/93) dan SNOMED. Dialog saat ini menyediakan SNOMED/KFA; belum menyediakan pilihan ATC.

Lima catatan tetap pending: satu catatan gabungan tiga obat, dua catatan antibiotik tanpa jenis/golongan spesifik, obat dosis tinggi, dan obat batuk pilek. Ditambahkan `review_reason` yang ditampilkan pada status/alasan gagal. Catatan gabungan tidak dipetakan ke satu zat atau dianggap selesai walaupun referensi masing-masing obat sudah tersedia. Jangan mengganti catatan tidak lengkap dengan No known allergy atau menebak komposisi obat.

Seluruh isi sebelum perubahan dicadangkan. Tidak ada perubahan catatan klinis/database, konfigurasi endpoint, atau pengiriman resource pasien. Hanya pencarian katalog referensi publik dilakukan. Penerimaan terminologi oleh endpoint FHIR belum diuji.

## Pencarian daring dan status verifikasi per baris

Setelah build ulang, tersedia tombol **Cari Referensi Online** serta kolom **Verifikasi Alergi** (di sisi kanan tabel). Alurnya:

1. Pilih satu baris dan klik **Cari Referensi Online**, atau centang baris lalu klik **Kirim/Update**.
2. Kirim/Update memakai mapping lokal terlebih dahulu. Mapping yang belum tersedia dicari daring melalui worker agar antarmuka tetap responsif. Maksimal sepuluh catatan baru yang berbeda per proses.
3. Pencarian hanya memakai nama alergen yang diekstrak. Misalnya `curiga alergi susu sapi` menjadi `cow's milk`; reaksi dalam kurung yang dikenali tidak dikirim sebagai kata pencarian. Catatan kosong, `-`, negasi, penanda identitas, obat tidak spesifik, serta beberapa zat dalam satu catatan tidak dipilih otomatis.
4. Hasil tepat dan tunggal dengan kategori yang telah diketahui disimpan ke `.iyem` beserta sumber/waktu pemeriksaan, tanpa mengubah mapping valid yang sudah ada. Penulisan membuat cadangan. Kegagalan/hasil parsial tidak dijadikan mapping otomatis.
5. Hasil ambigu, hasil mirip, atau kategori belum ditentukan memerlukan pilihan petugas melalui tombol pencarian. Pilihan yang ditinjau dapat memperbaiki mapping lokal lama, tetapi tidak otomatis memperbarui resource pasien yang sudah dikirim.
6. Klik **Kirim** untuk baris baru atau **Update** untuk baris dengan ID. Pencarian dari tombol referensi hanya menyimpan mapping. Pencarian yang dipicu tombol Kirim/Update meneruskan tindakan yang dipilih untuk baris yang lolos validasi. Perubahan centang/status selama pencarian membatalkan kelanjutan pengiriman dan meminta pengguna memeriksa pilihan lagi.

**Verifikasi Alergi** merupakan informasi per catatan pasien, bukan properti kamus. Baris baru dimulai dengan `unconfirmed`. Baris yang sudah mempunyai ID dimuat dengan pilihan kosong agar Update memerlukan pilihan eksplisit; aplikasi tidak menebak status historis atau menurunkannya diam-diam. `confirmed` hanya dipakai jika dipilih petugas dan catatan sumber tidak masih menyebut `curiga/dugaan/suspek`. Kode yang ditemukan otomatis tidak mengonfirmasi alergi. Kedua jalur POST/PUT memakai validasi status yang sama.

Pasangan kode/display yang diketahui tidak sesuai kini diblokir sebelum pengiriman, termasuk `227183005` dengan display `Milk protein`. Referensi sebenarnya adalah `227183005 — Fish roe`, `10827009 — Milk protein`, dan `3718001 — Cow's milk`.

Sumber dan keterbatasan:

- KFA BZA dicari melalui katalog publik resmi KFA Browser yang sudah diuji. Jika layanan tersebut berubah/tidak tersedia, adapter mencoba **GET** `products/all` pada `URLKFAV2SATUSEHAT` dari konfigurasi Mahkota Bunda memakai akses aplikasi yang sudah ada. Kandidat diambil dari bahan zat aktif berstatus aktif/valid. Hasil paginasi yang belum lengkap memerlukan peninjauan. Tidak ada Organization ID rumah sakit lain yang ditanam dalam kode.
- Referensi SNOMED dibaca dari lampiran alergi yang ditautkan dokumentasi SATUSEHAT dan ekspansi publik **Types of Edible Substances** milik HL7. Ini pencarian pada daftar terbitan yang tersedia, bukan layanan pencarian seluruh SNOMED CT milik SATUSEHAT. Halaman HTML dipakai karena resource JSON publik hanya berisi definisi ECL tanpa ekspansi kode. Perubahan format sumber menyebabkan hasil ditahan untuk peninjauan.
- Kategori otomatis SNOMED dibatasi pada konsep yang sudah diperiksa: Seafood, Popcorn, Cow's milk, Milk protein, Latex dan beberapa konsep pollen. Konsep lain ditampilkan dengan kategori yang harus ditinjau. Keanggotaan daftar edible substance saja tidak membuktikan konsep lolos pengecualian obat pada ECL SATUSEHAT; bahan kimia tidak otomatis dianggap alergen makanan.
- Padanan bahasa Indonesia terbatas pada istilah utuh yang didefinisikan dalam `SatuSehatAllergySearchTerm`; tidak ada terjemahan bebas, pemilihan token pertama, atau tebakan golongan obat. Kandidat baru/istilah yang tidak dikenal tetap memerlukan peninjauan.
- Katalog SNOMED disimpan di memori selama satu jam. Hasil pencarian sukses dipakai ulang selama dialog aktif; gangguan jaringan tidak dicache. Seluruh request dibatasi timeout/ukuran, tanpa mencatat kredensial atau payload pasien. Redirect otomatis dinonaktifkan; autentikasi cadangan hanya boleh menuju host KFA produksi/sandbox resmi.

File implementasi: `SatuSehatAllergySearchTerm`, `SatuSehatAllergyReferenceService`, `SatuSehatAllergyOnlineClient`, `SatuSehatAllergyMapping`, `SatuSehatAllergyPayload`, dan integrasi pada `SatuSehatKirimAllergyIntolerance`.

Contoh bagian mapping sebelum:

```json
{"keyword":"seafood","category":"medication","coding_system":"http://snomed.info/sct","coding_code":"unknown","coding_display":"seafood"}
```

Sesudah:

```json
{"keyword":"seafood","category":"food","coding_system":"http://snomed.info/sct","coding_code":"44027008","coding_display":"Seafood","text":"Alergi seafood","review_status":"ready"}
```

POST dan PUT memakai pembentuk payload yang sama, satu properti `code`, Jackson untuk escaping, dan teks sumber pada `note`. Waktu tanpa offset dibaca sebagai WITA (`Asia/Makassar`) lalu dikonversi UTC; waktu dengan offset eksplisit mengikuti offset tersebut. Organization ID berasal dari konfigurasi lokal. Tidak ada penggantian tanggal pelayanan menjadi tanggal saat ini. Catatan kosong/`-` tidak dianggap No known allergy. Kode No known allergy hanya untuk pernyataan negatif eksplisit, termasuk saat menggunakan alias.

Penyimpanan hasil memakai nama kolom eksplisit dan join membedakan Ralan/Ranap. Pembuatan tabel otomatis saat membuka dialog dihapus. Skema tabel yang sudah ada diperiksa baca saja dan mencakup `no_rawat`, `tgl_perawatan`, `jam_rawat`, `status`, `id_allergy_intolerance`. Tidak ada migrasi atau perubahan konfigurasi produksi.

## Tombol pencarian di dalam Mapping Alergi

Dialog Mapping Alergi kini memakai `SatuSehatAllergyMappingEditor`. Dua tombol **Cari Online** berada di samping **Kode zat penyebab** dan **Display resmi**. Keduanya memakai pencarian yang sama dan mengisi kategori, sistem terminologi, kode, display serta keterangan secara konsisten dari satu pilihan referensi.

Isi **Nama zat untuk dicari**, klik salah satu tombol, lalu pilih hasil yang sesuai. Untuk satu zat, nama pencarian diisi dari catatan bila dapat diekstrak. Ejaan `ibu profen`, `ibuporfen`, `cafein`, dan `kafein` dikenali; keterangan `berdebar` dipertahankan pada catatan sumber. Teks sumber tidak diganti dengan nama pencarian.

Pencarian berjalan di latar belakang; tombol simpan dinonaktifkan selama pencarian. Pembatalan pilihan mempertahankan isian sebelumnya. Menutup dialog membatalkan worker dan mengabaikan hasil yang terlambat. Pilihan hanya mengisi form; file `.iyem` baru berubah setelah **OK**, disertai cadangan dan metadata sumber jika pasangan referensi masih sesuai.

Sesuai pilihan pengguna, catatan seperti `ibu profen dan asam mefenamat (bengkak)` kini boleh disimpan melalui **OK** setelah satu obat dipilih dan field wajib valid. Mapping ditandai `partial_mapping: true` dan `review_status: partial`, dengan status tabel **Sebagian terpetakan**. Pilihan ini eksplisit oleh operator; pencarian otomatis tidak mengambil satu token/obat dari catatan gabungan.

Payload hanya berisi kode zat yang dipilih, keterangan kode hanya menyebut zat tersebut, dan `note` mempertahankan teks sumber lengkap beserta penjelasan bahwa zat lain belum terpetakan. Status verifikasi tetap merupakan pilihan per baris, bukan kesimpulan dari keberhasilan mapping. Catatan gabungan/alias lama yang belum disimpan ulang sebagai pilihan sebagian tetap ditahan. Alias gabungan yang ditinjau ulang disimpan sebagai entri tersendiri agar mapping satu zat yang sudah ada tetap utuh.

Fitur ini belum membuat beberapa resource dari satu catatan. Zat lainnya belum dikirim sebagai kode terstruktur. Memilih kode lain lalu **Update** pada ID yang sama mengganti resource tersebut, bukan menambah resource untuk obat kedua. Daftar reaksi dalam kurung, misalnya `ibuprofen (gatal, bengkak dan sesak napas)`, tidak dianggap sebagai beberapa alergen.

Pratinjau Swing dirender dengan data contoh tanpa database/jaringan, lalu diperiksa secara visual. Uji regresi editor memeriksa pengisian pasangan referensi, pelestarian catatan/status klinis, pembatasan catatan gabungan, dan penolakan alias gabungan lama. Tampilan dialog dalam aplikasi rumah sakit yang berjalan tetap belum diuji manual.

## Verifikasi

Kompilasi terarah delapan kelas terkait berhasil memakai JDK 15, source/target 8. Terdapat peringatan bootstrap class path karena bukan JDK 8. Sebanyak **90 pengujian JUnit** mapping/payload/pencarian berhasil tanpa database/API: seluruh pengujian sebelumnya serta pencarian tepat/ambigu/parsial, pemisahan kata pencarian, duplikat/konflik referensi, fallback KFA, pembatasan host autentikasi, parsing lampiran, cache, pelestarian mapping lama/keyword kanonis/alias, pemilihan operator, status verifikasi POST/PUT, dan regresi kode susu/telur ikan.

Uji daring terpisah menggunakan istilah contoh (tanpa catatan pasien dan tanpa menyimpan mapping uji) berhasil menemukan Paracetamol `91000101`, dugaan alergi susu sapi `3718001`, dan Lateks `111088007`. Pencarian katalog publik dan pembacaan ekspansi berhasil. Struktur GET KFA cadangan juga diperiksa baca saja menggunakan produk contoh; bahan zat aktif berada pada `active_ingredients[].kfa_code/zat_aktif`.

Class hasil kompilasi/pengujian berada di `tmp/compile-allergy`; belum dilakukan build/distribusi JAR penuh. Tampilan dialog belum diuji manual di aplikasi berjalan. Penerimaan kode oleh profil/terminologi SATUSEHAT pada endpoint produksi belum diverifikasi. Tidak ada POST/PUT resource FHIR, migrasi, atau perubahan konfigurasi yang dijalankan.

## Referensi resmi

- [SATUSEHAT AllergyIntolerance](https://satusehat.kemkes.go.id/platform/docs/id/fhir/resources/allergy-intolerance/): kategori, kode zat penyebab, konversi waktu ke UTC. Panduan resource menggunakan contoh identifier allergy organisasi; pola existing dipertahankan.
- [HL7 SNOMED CT food concepts](https://www.hl7.org/fhir/valueset-modified-foodtype.html): `44027008` adalah Seafood. Daftar ini memverifikasi arti kode; penerimaan pada profil AllergyIntolerance SATUSEHAT tetap bergantung aturan terminologi use case.

Catatan tambahan: Dexamethasone (nyeri badan) dan ejaan Dexamethason (nyeri badan) dicari sebagai nama obat Dexamethasone. Keterangan nyeri badan tetap berada pada note sumber, tidak mengonfirmasi alergi. Nama obat lain di dalam kurung tetap memerlukan peninjauan.
