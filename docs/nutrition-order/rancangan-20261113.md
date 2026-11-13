# Rancangan NutritionOrder Mahkota Bunda

Tanggal: 13 November 2026 (Asia/Makassar).
Status: rancangan untuk ditinjau, belum diimplementasikan atau dimigrasikan.
Titik kembali kode: commit 7deb37d5, branch feature/kamar-inap-tampilan-modern.

## Keputusan berdasarkan keterangan rumah sakit

- Dokter PJ memberikan instruksi diet dengan koordinasi petugas gizi.
- Petugas gizi tetap menjadi pencatat ADIME. Pencatat dan pemberi instruksi adalah peran berbeda.
- Catatan ADIME berisi rencana tindakan berdasarkan asesmen dan diagnosis gizi; tidak setiap ADIME otomatis merupakan instruksi dokter.
- Perubahan dicatat melalui ADIME lanjutan dan CPPT. Kertas tetap digunakan untuk komunikasi ke dapur.
- Keterangan mulai berlaku pada instruksi kertas harus disalin secara benar, bukan diganti waktu komputer.
- Pengiriman Diet/Composition yang sudah ada tetap berjalan dengan ID dan penyimpanannya sendiri.

## Temuan kode yang menentukan desain

`src/rekammedis/RMCatatanADIMEGizi.java` menyimpan sembilan kolom ADIME yang ada. Instruksi dan intervensi berupa teks bebas. Identitas catatan yang digunakan untuk edit/hapus adalah pasangan no_rawat dan tanggal; keduanya dapat berubah pada proses edit. Catatan belum memiliki struktur diet, dokter pemberi instruksi, atau waktu mulai berlaku.

`src/bridging/SatuSehatDietQuery.java` membaca instruksi ADIME untuk Composition dan menggunakan filter kunjungan selesai. Query tersebut tidak dipakai langsung untuk NutritionOrder: permintaan makan dapat berlaku selama perawatan apabila Encounter sudah tersedia dan instruksinya sah. Syarat nota, pembayaran, maupun pasien pulang bukan syarat baru yang akan ditambahkan pada daftar NutritionOrder.

## Tampilan dan alur petugas

Tambahkan akses **Instruksi Diet / NutritionOrder** dari catatan ADIME yang sudah disimpan. Gunakan dialog tambahan yang terkait dengan ADIME, bukan form asesmen baru. Cara ini menjaga form lama tetap dapat digunakan sebelum atau ketika fitur NutritionOrder belum tersedia.

Gambaran isi dialog:

| Bagian | Isian dan perilaku |
|---|---|
| Sumber | Kunjungan, tanggal ADIME, dan petugas pencatat mengikuti catatan yang dipilih |
| Isi ADIME | Intervensi dan instruksi ditampilkan sebagai rujukan; salin sebagai bahan awal tanpa mengubah sumber |
| Tujuan pencatatan | Pilih rekomendasi atau instruksi untuk dilaksanakan; tidak dipilih otomatis dari teks |
| Pemberi instruksi | Pilih dokter yang benar; kandidat dokter PJ kunjungan/DPJP, tanpa menebak bila ada beberapa |
| Waktu instruksi dibuat | Sesuai dokumentasi; menjadi NutritionOrder.dateTime |
| Mulai berlaku | Tanggal dan jam diet mulai dilaksanakan, terpisah dari waktu pembuatan |
| Diet | Dapat memilih lebih dari satu jenis diet bila memang diperintahkan; tidak mengurai singkatan dari teks secara otomatis |
| Bentuk makanan | NB, NL, BB, BS, MCJ, atau MCP, dengan status mapping terlihat |
| Instruksi untuk dilaksanakan | Teks yang ditinjau petugas, dapat berbeda dari rencana awal ADIME; simpan snapshot |
| Status | Pilih kondisi yang sebenarnya; aktif, ditunda, dihentikan, selesai, atau salah entri |
| Perubahan | Pilih instruksi sebelumnya dan alasan perubahan bila memang menggantikan; bukan sekadar karena ada ADIME lebih baru |
| Rincian tambahan | Jadwal, energi, protein, dan zat gizi hanya apabila tersedia sebagai preskripsi terverifikasi |

Dokter pada `orderer` merupakan pemberi instruksi, bukan otomatis penulis ADIME. Untuk tahap pertama, pengiriman instruksi dokter menuntut konfirmasi dokter dan referensi Practitioner yang valid sebagai aturan kelengkapan lokal. Dokumentasi Gizi sendiri tidak menandai orderer sebagai elemen wajib universal.

Rekomendasi ahli gizi yang belum menjadi instruksi dokter menggunakan intent proposal. Jangan mencantumkan dokter seolah-olah sudah memberikan instruksi. Pengiriman rekomendasi tersebut ditahan sampai tanggung jawab penulis rekomendasi dan representasi orderer disepakati; fitur awal dapat difokuskan pada instruksi dokter yang sudah dikonfirmasi.

## Pemetaan FHIR R4

| Sumber lokal | Elemen | Ketentuan |
|---|---|---|
| IHS pasien | patient.reference | Patient/{id}; bukan subject pada endpoint R4 |
| Encounter kunjungan | encounter.reference | Encounter/{id}, pasien harus sama |
| Status instruksi | status | Gunakan kode R4; label dihentikan dipetakan ke revoked, bukan stopped |
| Jenis pencatatan | intent | proposal untuk rekomendasi; order untuk instruksi yang sah |
| Waktu instruksi dibuat | dateTime | Offset sebenarnya, Asia/Makassar; tidak diganti hari ini |
| Dokter pemberi instruksi | orderer.reference | Practitioner/{id} yang sesuai, tanpa Practitioner/ kosong |
| Jenis diet terverifikasi | oralDiet.type[].coding | Sistem, kode, dan display berasal dari referensi terverifikasi |
| Bentuk makanan terverifikasi | oralDiet.texture[].modifier.coding | Terpisah dari jenis diet |
| Waktu berlaku/jadwal | oralDiet.schedule[].repeat.boundsPeriod | Rancangan awal memakai start/end bila relevan; uji terhadap panduan Timing dan validasi SATUSEHAT sebelum implementasi |
| Instruksi diet | oralDiet.instruction | Huruf i kecil sesuai R4 |
| Zat gizi dan jumlah | oralDiet.nutrient[].modifier/amount | Bukan oralDiet.nutrition; satuan UCUM dan besaran tidak boleh ditebak |

Wajib pada use case Gizi: status, intent, patient, encounter, dateTime. Isi oralDiet harus menggambarkan instruksi sebenarnya. Persyaratan klinis lokal dan syarat server harus ditandai terpisah.

Jangan memakai basedOn/subject atau elemen dari R5 tanpa memeriksa struktur R4. Beberapa contoh dokumentasi memuat perbedaan huruf/path; struktur R4 menjadi acuan serialisasi.

Fokus pertama adalah diet oral. Suplemen oral dan formula enteral memerlukan struktur tersendiri ketika memang digunakan. Nutrisi parenteral tidak dimasukkan sebagai oralDiet atau enteralFormula.

## Mapping awal

Lihat `mapping-diet-awal-20261113.csv`. Setiap baris memisahkan keberadaan kode resmi dan kesesuaian istilah lokal.

- Enam jenis diet memiliki pasangan langsung pada lampiran: RG, RL, Rpur, DM, diet jantung, dan diet hati. Ini verifikasi referensi, belum bukti diterima oleh API.
- TKTP perlu dikonfirmasi sebagai padanan TETP pada lampiran, bukan digabung otomatis dengan Tprot.
- RP harus dirinci menurut preskripsi protein; jangan memilih salah satu kategori gram secara otomatis.
- RS rendah serat tidak langsung disamakan dengan rendah sisa.
- Tprot dan TS masih memerlukan verifikasi konsep yang tepat.
- NB/NL/BB/BS memiliki kandidat kategori tekstur umum yang perlu dikonfirmasi gizi.
- MCJ dan MCP tidak boleh dikolaps otomatis menjadi satu instruksi klinis hanya karena lampiran memiliki kategori cair umum. Jika kategori umum dipilih setelah ditinjau, rincian lokal tetap harus dipertahankan dalam instruksi.

## Penyimpanan yang direncanakan

Tidak mengubah struktur tabel ADIME lama pada tahap awal. Tambahkan tabel pendamping setelah migrasi ditinjau:

1. `gizi_instruksi_diet`: UUID stabil, sumber ADIME, no_rawat, waktu pencatatan/pemberlakuan/penghentian, tujuan, status, dokter, petugas pencatat, snapshot instruksi, revisi, dan relasi instruksi yang diganti.
2. `gizi_instruksi_diet_item`: pilihan jenis diet, bentuk makanan, serta snapshot sistem/kode/display dan status verifikasinya. Dukung beberapa jenis diet per instruksi.
3. `satu_sehat_nutrition_order`: UUID lokal, lingkungan endpoint dan organisasi dari konfigurasi, ID server, revisi/hash payload berhasil, status sinkronisasi, serta rincian error yang sudah disamarkan.
4. Riwayat revisi lokal: perubahan beserta penulis dan waktunya; jangan menghapus versi lama ketika diet berubah.

Nama tabel dan kolom di atas adalah usulan desain, bukan DDL yang sudah dijalankan. Pertahankan data klinis asli; snapshot diperlukan karena tanggal/no_rawat ADIME dapat diedit. Setelah instruksi tertaut, perubahan/penghapusan sumber ADIME perlu pemeriksaan dan jejak perubahan agar tidak meninggalkan rujukan yang keliru. Jangan gunakan ON DELETE CASCADE yang menghapus riwayat pengiriman.

Tidak memakai id_diet dari Composition sebagai ID NutritionOrder. Tidak hardcode Organization ID. Identitas organisasi/endpoint berasal dari konfigurasi Mahkota Bunda.

## Pengiriman dan perubahan instruksi

- Menu **Kirim NutritionOrder SATUSEHAT** terpisah dari menu Diet; filter utama menggunakan waktu instruksi dan menyediakan pencarian no_rawat.
- Catatan lama ditampilkan sebagai kandidat yang belum ditinjau, bukan otomatis dinyatakan aktif.
- Tampilkan alasan belum siap: Encounter, identitas, dokter, status/tujuan, tanggal, atau mapping. Teks ADIME tetap dapat dibaca oleh petugas yang berhak.
- Tombol Kirim hanya untuk instruksi terverifikasi yang belum mempunyai ID server. Update untuk ID yang sudah ada.
- Periksa pasien/Encounter serta lingkungan sebelum POST/PUT. Nama petugas/NIK tidak dijadikan pengganti referensi IHS.
- Gunakan UUID lokal dan revisi; cegah klik ganda dan dua proses mengirim instruksi yang sama.
- Sebelum POST ulang akibat timeout, rekonsiliasi hasil kiriman. Sistem identifier/search NutritionOrder masih harus diverifikasi dari referensi resmi sebelum ditetapkan; jangan menebak namespace Kemenkes.
- Setelah hasil POST/PUT terkonfirmasi, simpan ID dan revisi berhasil. Jika penyimpanan lokal atau koneksi gagal, tampilkan perlu rekonsiliasi, bukan menganggap belum pernah dikirim.
- Rencana/rekomendasi tidak diubah begitu saja menjadi order dengan menimpa intent; buat catatan instruksi yang jelas dan pertahankan riwayatnya.
- Perubahan diet substantif membuat instruksi baru dan mencatat instruksi sebelumnya yang diganti. Penghentian diet lama hanya atas konfirmasi, dengan waktu yang sebenarnya.
- Kegagalan satu langkah sinkronisasi harus terlihat. Jangan menyatakan diet lama telah berhenti di server atau instruksi baru telah terkirim bila permintaannya gagal.
- Pulang tidak otomatis menyelesaikan seluruh instruksi diet: sebagian diet merupakan anjuran lanjutan di rumah.
- Rencana hak akses: pengisian mengikuti akses ADIME; pengiriman dapat memakai akses kirim diet yang sudah ada pada tahap awal, dengan pemisahan peran pengisi/pengirim tetap terlihat. Jangan mengubah hak pengguna tanpa ditinjau.

## Urutan implementasi dan pengujian yang akan dilakukan

1. Selesaikan review mapping yang masih kandidat dan verifikasi teknis identifier/Timing.
2. Siapkan DDL dan prosedur rollback sebagai file yang ditinjau; jangan jalankan pada produksi tanpa persetujuan.
3. Implementasikan validasi/pembentuk payload dan tes sintetis tanpa koneksi database/API.
4. Implementasikan dialog yang ditautkan dari ADIME dan penyimpanan pendamping; form ADIME lama harus tetap dapat dipakai bila tabel tambahan belum ada.
5. Implementasikan daftar pengiriman dengan alasan kelengkapan, pencegahan duplikasi, dan rekonsiliasi.
6. Uji pada lingkungan terpisah, lalu tinjau contoh payload tersamarkan dan tampilan bersama petugas gizi.

Skenario tes penerimaan:

- Instruksi sah menghasilkan patient/encounter/orderer R4 yang benar.
- Rekomendasi tidak otomatis dipromosikan menjadi instruksi dokter.
- Jenis diet/bentuk makanan yang belum diverifikasi ditahan sesuai kebutuhan data yang benar-benar diisi.
- TKTP, Tprot, RP tanpa rincian, serta MCJ/MCP tidak disamakan otomatis.
- Beberapa jenis diet tidak saling menimpa.
- dateTime dan mulai berlaku terpisah; konversi WITA ke UTC mempertahankan instant.
- Urutan akhir sebelum awal ditolak, tetapi jadwal yang memang akan datang tidak dipotong menjadi hari ini.
- Catatan ADIME baru tidak otomatis menghentikan instruksi lama.
- Penghentian/selesai/salah entri sesuai R4; tidak disimpulkan dari status pulang saja.
- Identitas dokter yang kosong atau salah pasien/Encounter menghentikan pengiriman.
- Klik ganda, timeout, dan gagal menyimpan ID tidak menghasilkan POST ulang tanpa rekonsiliasi.
- ID Composition tidak pernah dipakai untuk NutritionOrder.
- Perubahan sumber ADIME terlacak; riwayat klinis/pengiriman tidak terhapus.
- Log/error tidak menampilkan token atau identitas pasien.

Pengujian di atas adalah rencana tes, belum dinyatakan lulus. Tidak ada klaim bahwa penambahan satu NutritionOrder pasti langsung mengubah dashboard menjadi 100%; cakupan/filter dan jadwal pembaruan dashboard tetap perlu diverifikasi.

## Referensi yang dibaca ulang 13 November 2026

- https://satusehat.kemkes.go.id/platform/docs/id/interoperability/gizi/
- https://satusehat.kemkes.go.id/platform/docs/id/terminology/lampiran-terminologi/gizi/
- https://hl7.org/fhir/R4/nutritionorder.html
- https://hl7.org/fhir/R4/nutritionorder-definitions.html

Kode referensi bukan hasil pencarian otomatis atas catatan pasien. Dokumen ini tidak memuat data pasien, kredensial, atau payload produksi.
