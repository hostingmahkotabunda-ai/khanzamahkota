# NutritionOrder: implementasi lokal dan persiapan uji bersama petugas gizi

Implementasi 13 November 2026. Pengguna telah menyiapkan tabel dan berhasil melakukan percobaan pengiriman NutritionOrder; monitoring yang dibagikan pengguna menunjukkan POST sukses dengan respons 201 Created. Pemeriksaan/pembaruan kode oleh asisten tidak menjalankan migrasi atau POST/PUT klinis secara otomatis.

## Akses yang ditambahkan

- Menu utama: **Kirim NutritionOrder Satu Sehat**. Terpisah dari **Kirim Diet Satu Sehat** yang tetap mengirim Composition.
- Catatan ADIME Gizi: klik kanan catatan yang sudah disimpan, pilih **Instruksi Diet / NutritionOrder SATUSEHAT**.
- Pengguna dengan hak ADIME dapat membaca dan melengkapi tinjauan. Pengguna dengan hak kirim Diet dapat membaca dan melakukan Kirim/Update/Rekonsiliasi. Tidak ada perubahan hak pengguna atau kolom akses database.

## Memakai data sebelumnya bersama petugas gizi

1. Pilih rentang **tanggal ADIME**, cari nomor rawat/RM, lalu pilih satu catatan.
2. Pilih **Lengkapi Instruksi Diet**. Intervensi dan instruksi sumber tetap tersedia untuk ditinjau; ADIME asli tidak ditimpa.
3. Konfirmasi apakah catatan merupakan rekomendasi atau instruksi dokter yang sudah berlaku. DPJP rawat inap diutamakan; bila tidak ada, gunakan kandidat dokter PJ registrasi. Jika kandidat tunggal dan terhubung ke pegawai, dokter terpilih otomatis. Jika ada beberapa DPJP, pilih pemberi instruksi yang benar. Dokter yang sudah tersimpan tetap dipertahankan, dan pilihan otomatis masih perlu dikonfirmasi sesuai dokumentasi.
4. Pada tab **Instruksi & waktu**, gunakan kalender serta pilihan jam dan menit untuk waktu instruksi dibuat, mulai berlaku, dan akhir berlaku. Kontrol langsung aktif tanpa centang; detik otomatis 00 untuk waktu baru atau yang diedit. Waktu lama dipertahankan jika tidak diedit, termasuk detik yang sebelumnya tercatat. Tanggal ADIME hanya petunjuk awal kalender, bukan pengisian waktu klinis otomatis. Gunakan waktu pada dokumentasi asli, bukan hari ini. Tombol Reset mengosongkan jam/menit tanpa mengisi waktu baru secara otomatis.
5. Tambahkan jenis diet dan bentuk makanan yang relevan. Beberapa jenis diet dapat ditambahkan sekaligus. Jangan menambahkan kategori yang tidak diperintahkan.
6. Mapping kandidat membutuhkan peninjauan istilah dan konfirmasi. Kode yang kosong harus diverifikasi lebih dahulu; mencentang Terverifikasi tidak mengisi kode otomatis. RP mempunyai pilihan spesifik 20-40 g, 50 g, dan 60 g apabila memang diresepkan.
7. Tinjau teks instruksi serta status dan konfirmasi petugas. Draft yang belum lengkap dapat disimpan, tetapi tidak dapat dikirim.
8. Simpan tinjauan lokal; lihat alasan kelengkapan pada daftar. **Pratinjau Offline (ID Contoh)** tidak memanggil API, tidak mencari IHS, dan mengganti seluruh ID referensi dengan contoh. Isi instruksi/waktu masih berasal dari tinjauan, sehingga pratinjau bukan dokumen publik.
9. Setelah data klinis disetujui dan percobaan pengiriman diizinkan, pilih **Kirim Baru** dan konfirmasi tujuan sesuai konfigurasi. Uji pertama cukup satu catatan yang telah ditinjau petugas gizi.

Untuk instruksi dokter gunakan order. Proposal hanya dipakai bila benar-benar merupakan rekomendasi dengan penanggung jawab yang sesuai; jangan mengatasnamakan dokter jika belum memberi rekomendasi tersebut.

## Perubahan dan penghentian

- Data lama tidak otomatis aktif; status dan waktu ditentukan dari dokumentasi, bukan status pasien pulang.
- ADIME baru tidak menghentikan instruksi lama secara otomatis.
- Penghentian atau penyelesaian instruksi lama ditinjau pada catatan lama, dengan waktu akhir sebenarnya, lalu Update jika sudah terkirim.
- Perubahan substantif diet dicatat melalui ADIME lanjutan dan tinjauan baru. Untuk tahap ini hubungan menggantikan instruksi lama belum dibuat otomatis; operator harus memeriksa dan menyelesaikan status lama secara eksplisit.
- Intent, pasien, Encounter, dokter pemberi instruksi, dan waktu pembuatan tidak boleh berubah saat Update instruksi yang sudah terkirim. Koreksi atau instruksi baru memerlukan tinjauan tersendiri.
- Setelah tertaut dengan tinjauan NutritionOrder, ADIME sumber dilindungi dari edit/hapus pada layar ini agar riwayat tidak terputus. Gunakan ADIME lanjutan. Perubahan dari aplikasi lain tetap terdeteksi melalui sidik sumber sebelum kirim.

## Persiapan database: belum dijalankan

File: `sql/migrasi_nutrition_order.sql`. Berisi tiga tabel tambahan: instruksi, riwayat revisi, dan status pengiriman per organisasi/endpoint. Tidak mengubah tabel ADIME, Composition, data pasien, atau stok.

Tidak ada CREATE/ALTER dari constructor, tombol Cari, atau tombol Simpan. Jika tabel belum tersedia, daftar ADIME tetap dapat dibaca dan dirancang sebagai bahan tinjauan, tetapi Simpan/Kirim ditahan. Persetujuan migrasi dan cadangan database diperlukan sebelum penggunaan penyimpanan baru di produksi.

Rollback aplikasi dilakukan dengan mengembalikan kode ke titik cadangan yang sesuai. Tabel tambahan yang sudah berisi riwayat dan ID server dipertahankan; jangan dihapus otomatis ketika rollback kode. Git tidak mengembalikan isi database.

## Pengamanan pengiriman

- Endpoint dan organisasi memakai konfigurasi lokal Mahkota Bunda; tidak ada Organization ID rumah sakit lain yang di-hardcode.
- IHS pasien/dokter diperiksa pada saat Kirim/Update yang dipicu pengguna. Pasien dan organisasi Encounter harus cocok.
- Payload memakai patient R4, oralDiet.instruction, jenis diet terstruktur, serta jadwal mulai/akhir. Tidak mengarang energi, protein, dosis, atau parameter gizi.
- Setiap instruksi mempunyai UUID lokal dan revisi. Status pengiriman disimpan terpisah dari Composition, dibedakan per endpoint/organisasi.
- Kunci transaksi menahan dua pengiriman untuk instruksi/revisi yang sama. Respons yang terlambat tidak boleh menimpa hasil revisi yang lebih baru.
- Update memeriksa referensi dan intent yang sudah ada, menggunakan versi server/If-Match, serta mempertahankan field klinis server di luar bagian yang dikelola layar ini.
- Bila hasil request tidak pasti, instruksi ditahan dalam SENDING/UNKNOWN. Tidak ada POST ulang otomatis. Gunakan ID dari monitoring untuk **Rekonsiliasi ID**, yang hanya GET dan pemeriksaan kesesuaian isi percobaan.
- Belum ada namespace identifier khusus NutritionOrder yang diasumsikan. Karena contoh resmi dapat tidak menyertakan identifier, tahap awal menggunakan registri pengiriman lokal dan rekonsiliasi manual. Jika hasil tidak pasti dan ID tidak dapat ditemukan, perlu pemeriksaan administrator; tombol reset kirim tidak disediakan agar tidak menciptakan duplikasi.
- Log fitur tidak mencetak token, NIK, payload klinis, atau respons mentah. Ringkasan error hanya HTTP, expression, dan nomor rule yang disaring.

## Batas yang masih perlu diuji

Catatan perkembangan: uji pengguna setelah perbaikan pencarian IHS sudah diterima server. Batas berikut tetap menjadi cakupan pengujian lanjutan; satu POST berhasil bukan bukti seluruh kasus konkurensi, timeout, dan PUT sudah diuji melalui API.

- Kompilasi dan tes payload sintetis sudah dilakukan; hasil akhir dicatat pada laporan pengerjaan.
- Tampilan editor diperiksa menggunakan data sintetis tanpa koneksi database/API.
- DDL tidak dijalankan oleh asisten. Tiga tabel sudah ditemukan pada instalasi pengguna; pengujian migrasi ulang pada salinan database terpisah belum dilakukan.
- Transaksi penyimpanan, locking antarkoneksi, penerimaan schedule.boundsPeriod, ETag/If-Match, dan respons POST/PUT perlu uji integrasi setelah lingkungan uji disiapkan.
- Mapping yang belum pasti masih membutuhkan peninjauan; verifikasi lokal tidak menjamin penerimaan terminologi oleh server.
- Fitur awal mencakup instruksi diet oral. Jadwal terperinci, kebutuhan zat gizi terstruktur, suplemen, dan formula enteral belum memiliki editor khusus; jangan menyatakan informasi tersebut sudah dikirim terstruktur hanya karena muncul dalam teks.
- Dashboard 100% belum dapat dijanjikan tanpa pengiriman yang sah dan verifikasi hasil pengolahan dashboard.

## Hasil verifikasi lokal

- Kompilasi enam kelas aplikasi terkait beserta tes berhasil dengan target Java 8. Ada peringatan bootstrap classpath pada JDK yang lebih baru dan penggunaan API lama; tidak ada error kompilasi.
- **49 tes** dalam `test/SatuSehatNutritionOrderPayloadTest.java` lulus: referensi R4, waktu/zona/batas tanggal, pemisahan waktu instruksi dan berlaku, status, mapping ambigu, beberapa jenis diet, POST/PUT, perlindungan referensi/intent/jenis diet, dan pemeliharaan field server. Tambahan pemeriksaan memastikan detik tetap diserialisasi ketika bernilai nol, pecahan detik dipertahankan, dan offset sesuai FHIR R4.
- `git diff --check` lulus.
- Pratinjau editor memakai catatan dan dokter sintetis, tanpa membuka koneksi database atau API.
- `contoh-payload-sintetis.json` hanya contoh keluaran pembentuk payload, bukan data pasien dan bukan bukti penerimaan server.
- `pratinjau-editor.png` memperlihatkan editor dengan data sintetis; tersedia scroll vertikal pada layar kecil, sementara tombol Simpan/Batal berada di luar area scroll.

### Perbaikan pencarian IHS

Pencarian identifier sebelumnya memakai query yang sudah di-encode pada overload String RestTemplate, sehingga karakter persen di-encode kembali dan GET pasien/praktisi ditolak HTTP 400. Pemanggilan GET sekarang memakai overload URI untuk menjaga query tetap di-encode satu kali. Pengujian GET pada kasus uji pengguna berhasil menemukan masing-masing satu pasien dan praktisi; pasien serta organisasi Encounter sesuai. Tidak ada POST/PUT NutritionOrder dilakukan dalam pemeriksaan tersebut.

Pesan kegagalan sekarang mempertahankan tahap pemeriksaan, HTTP status atau SQLState/kode database yang aman, tanpa URL berisi NIK, token, query SQL, atau respons mentah. Empat tes transport/pesan error tambahan lulus; total **53 tes** bersama 49 tes payload.

### Penyempurnaan UI DPJP dan waktu

Dokter tunggal dari DPJP/PJ dipilih otomatis untuk tinjauan yang belum memiliki dokter. DPJP yang lebih dari satu tidak dipilih sembarang; dokter yang sudah disimpan tidak ditimpa. Nama dokter kini terlihat pada informasi PJ, bukan kode saja. Tanggal menggunakan kalender, waktu memakai pilihan jam/menit/detik dalam WITA. Nilai lama, termasuk UTC atau pecahan detik, dipertahankan saat tampilan dibuka tanpa perubahan.

Empat belas tes editor tambahan lulus dengan kontrol Swing dan data sintetis, tanpa database/API. Total saat ini **67 tes lulus**. Pemeriksaan tampilan sintetis diperbarui pada `pratinjau-editor.png`.

### Tampilan terbaru

Editor memakai dua tab: **Instruksi & waktu** dan **Diet & catatan**. Informasi pasien tetap terlihat di atas, catatan sumber ADIME dapat dibuka melalui tombol, dan konfirmasi serta Simpan tetap terlihat di bawah. Label Akhir berlaku dan Bentuk makanan tidak lagi memuat tulisan dalam kurung “bila ada”. Validasi klinis tetap berlaku: waktu akhir wajib untuk status selesai/dihentikan; waktu akhir tidak dibuat otomatis untuk diet yang masih aktif.

Pengujian editor disesuaikan dengan kontrol tanpa checkbox dan detik otomatis. Total tetap **67 tes lulus**, kompilasi berhasil, dan kedua tab diperiksa dengan data sintetis pada `pratinjau-editor.png` serta `pratinjau-diet.png`. Tidak ada migrasi, perubahan catatan klinis, atau pengiriman API dalam penyempurnaan UI ini.

### Halaman pengiriman terbaru

Filter tanggal awal dan akhir menggunakan kalender dengan format tampilan tanggal Indonesia; tanggal tidak diketik melalui teks bebas. Nilai pencarian tetap dikirim ke query sebagai yyyy-MM-dd dan tetap memakai tanggal ADIME. Kolom pencarian nomor rawat/RM/nama tetap berupa teks.

Header, kartu filter, tabel, serta tombol tindakan memakai gaya visual yang sama dengan editor. Status kelengkapan diletakkan lebih awal di tabel, baris lebih tinggi, warna selang-seling, dan tersedia penanda jumlah catatan serta baris yang sedang dipilih. Pratinjau tetap offline dan tombol Kirim/Update tetap meminta konfirmasi seperti sebelumnya.

Tiga tes kalender filter tambahan lulus: tanggal dipertahankan dengan benar, editor tanggal tidak menerima teks bebas, dan tanggal kosong ditolak sebelum pemuatan. Total **70 tes lulus**, kompilasi dan git diff --check berhasil. Tampilan daftar diperiksa memakai baris sintetis (`pratinjau-pengiriman.png`); tidak ada perubahan data atau pengiriman API.

### Halaman input ADIME

Layout lama dengan posisi tetap diganti dengan tampilan yang menyesuaikan lebar layar: informasi pasien/petugas di bagian atas, enam kartu isian dalam dua baris, serta riwayat di bawah. Pemisah area input dan riwayat dapat digeser. Tampilkan input dapat digunakan untuk memperluas riwayat tanpa mengosongkan isian.

Komponen, dokumen teks, batas panjang, tombol beserta listener, template, timer waktu, hak akses, dan popup NutritionOrder tetap memakai komponen asli. SQL penyimpanan tidak diubah. Pembacaan menit ketika memilih catatan lama diperbaiki dari satu menjadi dua digit agar waktu ditampilkan dengan benar; tidak ada pembaruan data tersimpan yang dijalankan.

Kelas presentasi `rekammedis.AdimeGiziView` tidak memiliki akses database/API. Kompilasi berhasil, 70 tes regresi NutritionOrder tetap lulus, dan pemeriksaan tampilan sintetis memastikan komponen serta isi enam catatan tidak berubah setelah area input disembunyikan dan ditampilkan kembali. Contoh tampilan: `pratinjau-adime.png`.

Penyempurnaan berikutnya menebalkan teks enam isian ADIME dan identitas/waktu/petugas. Tombol memakai latar solid dengan teks putih: Template ungu, Simpan hijau, Baru biru, Ganti jingga, Hapus merah, Cetak abu gelap, dan Keluar biru gelap. Komponen tombol asli beserta hak akses, model, mnemonic, dan listener tetap digunakan. Tombol ditempatkan dalam satu grid; petunjuk dipisahkan ke baris bawah sehingga tidak menyebabkan pembungkusan tombol. Teks checkbox Otomatis diposisikan di sebelah kanan indikator, menggantikan pengaturan CENTER lama yang menumpuk teks dengan indikator. Pemeriksaan geometri tombol pada lebar 1000, 1320, dan 1850 piksel lulus tanpa tumpang tindih atau tombol terpotong. Kompilasi, git diff --check, dan 70 tes regresi tetap lulus.

Header ADIME kemudian diringkas menjadi satu baris; label identitas dipindahkan ke samping input dan bagian pasien/petugas dirapatkan agar ruang isian lebih luas. Tampilan sintetis dan kompilasi diperiksa kembali.
