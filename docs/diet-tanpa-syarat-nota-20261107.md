# Daftar pengiriman Diet tanpa syarat nota

Atas instruksi pengguna, filter nota pada halaman SatuSehatKirimDiet dilepas.
Tidak ada perubahan data pasien, isi instruksi gizi, konfigurasi, migrasi,
atau pengiriman ke SATUSEHAT selama pengerjaan.

Sebelumnya dua query memakai INNER JOIN nota_jalan/nota_inap dan filter tanggal
nota, walaupun label UI tertulis Tgl.Registrasi. Query sekarang satu untuk
Ralan/Ranap, memakai tanggal registrasi, tanpa membaca tabel nota.

Sumber narasi tetap catatan_adime_gizi.instruksi, bukan menggabungkan isian
skrining/asuhan menjadi instruksi baru. ID Encounter tidak boleh kosong dan
pegawai pembuat catatan tetap harus tersedia. Catatan diet yang sudah mempunyai
ID lokal tetap tampil untuk alur Update; tiap tanggal catatan tetap terpisah.

Karena payload lama berupa Composition dengan bagian Discharge diet (narrative),
daftar mempertahankan batas kunjungan selesai sesuai alur Encounter yang telah
disepakati: Ralan Sudah Bayar; Ranap segmen terakhir sudah berstatus pulang
dengan tanggal keluar valid, tanpa kamar aktif. Pindah Kamar, status kosong,
tanda '-', dan kunjungan Batal tidak dianggap selesai. Riwayat kamar tidak
menggandakan catatan diet.

Sebelum POST/PUT, prasyarat dibaca ulang berdasarkan nomor rawat dan waktu
catatan. Perubahan status pelayanan, ID Encounter/diet, instruksi, atau NIK
pasien/petugas membuat baris ditahan agar operator memuat ulang daftar.
Pencarian Patient/Practitioner yang menghasilkan ID kosong/tidak valid juga
ditahan sebelum mengirim Composition. Ini tidak mengarang identitas atau
mengganti penulis instruksi.

Kompilasi helper query dan dialog berhasil (JDK 15, source/target 8,
peringatan bootstrap/unchecked). Empat belas tes H2 sintetis lulus: tanpa
tabel nota, pasien aktif, pindah kamar, kamar aktif setelah riwayat pulang,
ID Encounter/instruksi kosong, petugas hilang, Ralan sudah/belum bayar,
filter tanggal/pencarian, beberapa segmen kamar, beberapa catatan ADIME,
dan ID diet yang telah tersimpan.

```powershell
java '-Djava.awt.headless=true' -cp 'tmp/compile-diet-visibility;tmp/h2-2.2.224.jar;lib/*;lib/hamcrest/*' org.junit.runner.JUnitCore SatuSehatDietQueryTest
```

SELECT baca saja pada database workspace untuk registrasi 1 Oktober–7 November
2026 menghasilkan **48 catatan dari 48 nomor rawat**, seluruhnya tanpa nota,
tanpa kamar aktif, dan belum mempunyai ID Diet lokal. Angka ini menunjukkan
hasil query daftar, bukan bukti penerimaan payload server. Identitas pasien
tidak dicantumkan.

Build JAR, uji UI manual, dan POST/PUT belum dilakukan. Bentuk payload Composition
lama serta aturan tanggalnya tidak diubah pada tugas filter ini. Menghapus
syarat administratif nota tidak menjamin seluruh payload lama diterima;
validitas klinis instruksi dan profil resource tetap perlu diperiksa saat
pengiriman. Panduan terkini membahas diet terstruktur melalui NutritionOrder;
pekerjaan ini tidak memigrasikan menu Composition lama menjadi NutritionOrder.

Referensi resmi yang diperiksa:
- https://satusehat.kemkes.go.id/platform/docs/id/interoperability/rme-rawat-jalan/
- https://satusehat.kemkes.go.id/platform/docs/id/interoperability/rawat-inap-new/
