# Sinkronisasi SatuSehat 30 Oktober 2026

Sumber: QuantumTech-IT/KHANZA-MAHKOTA-2025, branch
`feature/paket-obat-operasi`, commit `6869eebab57b68fc5fb39203826dc8a6e8518194`.
Basis lokal sebelum perubahan: `8bf652d52d2334043827eefc0df6f53731e8086f`.

## Cakupan

- Mengambil 27 file baru dan memperbarui 12 file pada project
  `KhanzaHMSServiceSatuSehat` dan `src/bridging/SatuSehat*`.
- Dua file referensi LOINC lokal dipertahankan.
- Menggabungkan perubahan lokal pada MedicationDispense dan Encounter,
  bukan mengganti seluruh file tersebut dengan versi GitHub.
- Menambahkan `DlgCariTemplateLaborat.java` karena form mapping laboratorium
  GitHub merujuk kelas ini tetapi kelasnya tidak tersedia pada commit sumber.
  Pemilih ini membaca template yang ada dan tidak mengubah data laboratorium.
- Menambahkan 13 hak akses SatuSehat yang dibutuhkan pada `akses.java`,
  mengikuti pola login/reset lokal. Kolom hak akses yang belum ada menghasilkan
  `false`, menggunakan helper `getBoolean` lokal.
- Build service menghasilkan aturan ignore `/KhanzaHMSServiceSatuSehat/build/`.

## Perbaikan lokal yang dipertahankan

- Konversi Asia/Makassar ke UTC pada form MedicationDispense.
- Pengolahan signa/dosis, termasuk dosis pecahan.
- Pemeriksaan ID MedicationRequest sebelum pengiriman serta
  `authorizingPrescription`, pada form dan service MedicationDispense.
- Referensi Medication menggunakan ID Medication, bukan ID MedicationRequest.
- Perubahan lokal statusHistory Encounter yang tidak menyertakan akhir periode
  arrived; validasi pasien/praktisi dan query terbaru dari GitHub tetap masuk.

## Verifikasi

- Aplikasi utama: `ant -q compile` berhasil.
- Service: `ant -q -f KhanzaHMSServiceSatuSehat/build.xml -Djavac.classpath=../lib/* compile`
  berhasil. Override classpath diperlukan karena konfigurasi service masih
  merujuk lokasi library `/Users/windiartonugroho/Lib`.
- Pemeriksaan regresi menggunakan metode signa dan UTC yang diekstrak dari
  source hasil gabungan: signa 3 x 1, dosis 2 x 0.5, konversi WITA, serta
  pergantian tanggal UTC berhasil.
- Seluruh 39 path yang diminta tersedia; tidak ada penanda konflik merge.

## Batas verifikasi

Belum menjalankan aplikasi terhadap database atau mengirim data ke API.
Tidak menjalankan migrasi database. Menu baru pada `src/simrskhanza/frmUtama.java`
dan editor hak akses tidak ikut diambil: cakupan penarikan adalah 39 file
SatuSehat beserta dependensi kompilasinya. Jadi kelas modul baru sudah tersedia,
tetapi penambahan pintasan menu dan aktivasi otomatisasinya belum dilakukan.
Belum melakukan commit atau push hasil penggabungan.

Log build dan pemeriksaan regresi tersimpan di `tmp/satusehat-*-build.log`
dan `tmp/SatuSehatMergeRegression.java`. Lokasi cadangan file sebelum
penggabungan dicatat di `tmp/satusehat-sync-backup-path.txt`.

## Lanjutan: menu CarePlan

Menu `Kirim CarePlan Satu Sehat` kini ditambahkan pada menu kategori yang sama
dengan Diet, semua menu, dan pencarian. Menggunakan ikon `/48x48/satusehat.png`
yang sudah tersedia. Tombol dan handler mengikuti hak akses
`satu_sehat_kirim_careplan`; pengguna tanpa hak tersebut tidak mendapat akses.
Admin utama mengikuti hak admin yang sudah ditetapkan di `akses.java`.
Editor hak akses dan struktur database tidak diubah dalam penambahan menu ini.
Modul SatuSehat baru lainnya tetap belum ditambahkan ke menu.

Payload identifier CarePlan sudah diperbaiki menjadi array pada POST/PUT form
dan pengiriman rawat jalan/rawat inap service.

## Lanjutan: menu Mapping Laboratorium

Menu `Mapping Laboratorium Satu Sehat` ditambahkan setelah CarePlan di kategori,
semua menu, dan pencarian, menggunakan ikon SatuSehat yang tersedia.
Handler membuka `SatuSehatMapingLaborat` dan memeriksa hak akses
`satu_sehat_mapping_lab` sebelum membuat form. Menu CarePlan dan menu sebelumnya
dipertahankan. Build aplikasi utama berhasil; log ada di
`tmp/mapping-lab-menu-build.log`. Belum melakukan uji UI/database langsung.
Form dari GitHub memiliki inisialisasi tabel mapping ketika dibuka; proses
build ini tidak membuka form atau menjalankan SQL tersebut.
