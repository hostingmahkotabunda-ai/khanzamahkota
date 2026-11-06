# QR Telaah: referensi penulis kosong

Log yang diberikan menunjukkan pencarian Patient berhasil (`total: 1`) dan pencarian Practitioner petugas tidak menemukan hasil (`total: 0`, `entry: []`). Payload kemudian memuat `author.reference: Practitioner/` tanpa ID. Ini referensi yang tidak valid; nama pada display tidak menggantikan ID Practitioner. Penyebab hasil pencarian nol (NIK master pegawai tidak cocok atau data praktisi belum tersedia) belum dapat dipastikan dari log saja.

Kode sebelum perbaikan hanya memeriksa bahwa kolom NIK pasien/petugas terisi, lalu meneruskan hasil pencarian tanpa memvalidasi ID yang dikembalikan. Perilaku ini ada pada Kirim dan Update. Penanganan error hanya menampilkan nama exception 400, sehingga OperationOutcome tidak tampak dalam log yang diberikan. Rincian field yang ditolak server dan seluruh penyebab HTTP 400 belum dapat dikonfirmasi tanpa OperationOutcome.

Perbaikan terbatas:

- `SatuSehatQRTelaahValidation.requireReferences` memvalidasi ID Patient, Practitioner dan Encounter sebelum pengiriman resource.
- Kedua handler memanggil validasi setelah pencarian identitas dan sebelum membentuk request/authentication pengiriman. Baris yang tidak valid tetap terpilih dan ditahan dengan pesan spesifik.
- Respons HTTP error kini ditampilkan pada dialog: details.text, diagnostics dan expression dari OperationOutcome bila tersedia.
- Payload lengkap dan respons lengkap tidak lagi dicetak dari kedua handler untuk mengurangi pemaparan identitas pada log.

Kompilasi terarah dua kelas berhasil (JDK 15, source/target 8; peringatan bootstrap dan unchecked pada kelas dialog). Tujuh pengujian validasi referensi berhasil tanpa database/jaringan. Tidak ada migrasi, perubahan identitas/master pegawai, perubahan konfigurasi, atau POST/PUT resource yang dilakukan. Dialog aplikasi tidak dijalankan saat pengujian; pembuatan tabel yang sudah ada pada konstruktor tidak dieksekusi oleh kompilasi atau tes helper.

## Temuan payload lain yang belum diperbaiki

Dokumentasi pengkajian resep SATUSEHAT merujuk `https://fhir.kemkes.go.id/Questionnaire/Q0007`, pertanyaan/linkId resmi serta tipe jawaban yang sesuai. Payload yang diberikan belum memiliki questionnaire dan menggunakan grup/linkId lokal identitas/telaah-resep/telaah-obat. Memasukkan URL Q0007 saja tidak menyelaraskan pertanyaan lokal dengan pertanyaan resmi. Delapan pemeriksaan lokal dan telaah obat tidak boleh dianggap otomatis menjawab seluruh pengkajian administrasi/farmasetik/klinis Q0007. Pemetaan perlu ditinjau sesuai data klinis yang benar-benar tersedia.

`authored` saat ini berasal dari tanggal/jam peresepan dan diberi offset tetap +07:00. Kesesuaian dengan waktu pengkajian aktual serta zona WITA masih perlu diperiksa; waktu sumber belum diubah. Temuan ini belum terbukti sebagai penyebab HTTP 400 pada log tersebut.

Referensi: [SATUSEHAT Resume Medis Rawat Jalan, Pengiriman Data Pengkajian Resep](https://satusehat.kemkes.go.id/platform/docs/id/interoperability/rme-rawat-jalan/), [SATUSEHAT Rawat Inap, Pengkajian Resep](https://satusehat.kemkes.go.id/platform/docs/id/interoperability/rawat-inap-new/).

## Perubahan notifikasi atas permintaan pengguna

Popup validasi per baris dan popup HTTP error pengiriman dihilangkan. Validasi ID tetap berjalan dan baris yang gagal/ditahan tetap dicentang. Ringkasan berhasil/belum selesai muncul pada label status di bawah daftar; rincian validasi/OperationOutcome tersedia saat penunjuk diarahkan ke label. Kirim dan Update mereset ringkasan setiap proses. Ini tidak mengubah identitas petugas, payload klinis, atau menjalankan pengiriman ulang. Kompilasi terarah dan tujuh tes validasi referensi berhasil.

## Pemeriksaan ulang pencarian Practitioner

Pemeriksaan baca saja pada sumber resep yang dilaporkan menemukan satu baris petugas. NIK dari `pegawai.no_ktp` berisi 16 digit tanpa spasi di awal/akhir. Pemeriksaan ini tidak memverifikasi kepemilikan/keabsahan NIK terhadap dokumen kependudukan. OAuth dan FHIR menunjuk host produksi resmi yang sama. Pencarian ulang menggunakan identifier NIK yang di-URL-encode secara utuh mengembalikan HTTP 200, `total: 0`, `entry: []`.

Dengan demikian, hasil kosong saat pemeriksaan ulang bukan karena whitespace lokal, lingkungan sandbox yang dipakai tanpa sengaja, atau karakter pemisah identifier yang tidak di-encode. Penyebab operasionalnya adalah ID Practitioner belum ditemukan pada MNI untuk NIK tersebut. Alasan administratif/data pusat yang mendasarinya belum dapat dipastikan dari akses API ini. Perlu pengecekan profil tenaga kesehatan dan, bila data sudah sesuai, aduan IHS/NIK tenaga kesehatan tidak ditemukan melalui kanal resmi Kemenkes. Tidak ada perubahan NIK, pendaftaran otomatis, pengiriman resource klinis, atau perubahan konfigurasi yang dilakukan.
