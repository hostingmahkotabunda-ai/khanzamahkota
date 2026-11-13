# Form rekam medis yang terbukti memiliki isian

Pemeriksaan baca saja pada 10 November 2026. Nama form dicocokkan dengan kode sumber aplikasi; jumlah berasal dari catatan database yang sudah ada, tanpa menampilkan identitas atau jawaban klinis pasien. Semua riwayat dihitung, tidak hanya November.

**111 entri form/modul utama mempunyai isian**, dari 216 tabel unik yang diperiksa. Tabel detail, lampiran, master pasien, dan transaksi biaya tidak dihitung sebagai form tersendiri. Beberapa entri menggunakan layar yang sama dengan bagian atau versi yang berbeda.

Kriteria digunakan = minimal satu catatan tersimpan. Ini tidak membuktikan form diisi lengkap, diisi setiap pasien, atau semua catatan dibuat oleh petugas tertentu. Aktivitas terbaru memakai jendela 12 Oktober–10 November 2026. Nilai hitungan adalah catatan, bukan jumlah pasien.

Pengelompokan mengikuti nama/tujuan form bila jelas; form tanpa penanda khusus mengikuti status kunjungan di registrasi. Status kunjungan dapat berubah dari rawat jalan menjadi rawat inap, sehingga angka status registrasi bukan selalu unit saat pengisian. Form IGD/bersama/penunjang dipisahkan agar tidak salah dikelompokkan.

## Ringkasan

| Kelompok | Form/modul berisi data | Dengan isian 30 hari terakhir |
|---|---:|---:|
|Rawat Jalan|33|6|
|Rawat Inap|42|17|
|Bersama / IGD / Penunjang|36|15|

## Rawat Jalan

| Nama form | Catatan tersimpan | Isian 30 hari | Tanggal terbesar tercatat | Tabel sumber |
|---|---:|---:|---|---|
|SOAP / Pemeriksaan Rawat Jalan|40353|1178|2026-11-10|`pemeriksaan_ralan`|
|Resume Medis Rawat Jalan V2|1176|289|2026-11-10 02:28:27|`resume_medis_ralan_v2`|
|Profil Ringkas Medis Rawat Jalan|280|280|2026-11-09|`profil_ringkas_medis_ralan`|
|Asesmen Awal Rawat Jalan|254|253|2026-11-09|`asesmen_ralan`|
|Catatan Pasien Rawat Jalan|433|28|2026-11-09 11:15:50|`catatan_pasien_ralan`|
|Penilaian Awal Medis Rawat Jalan Umum|53|9|2026-11-06 15:55:20|`penilaian_medis_ralan`|
|Catatan Cek GDS & Pemberian Insulin / Obat Gula|3|0|2025-08-19|`catatan_cek_gds`|
|Catatan Keperawatan Rawat Jalan|2|0|2025-09-02|`catatan_keperawatan_ralan`|
|Catatan Perawatan Pasien|1|0|2026-06-13|`catatan_perawatan`|
|Follow Up DBD|1|0|2025-06-28|`follow_up_dbd`|
|Pemantauan EWS Pasien Dewasa|1|0|2025-06-30 09:01:55|`pemantauan_pews_dewasa`|
|Pemeriksaan Ginekologi Rawat Jalan|2|0|2026-05-14|`pemeriksaan_ginekologi_ralan`|
|Pemeriksaan Obstetri Rawat Jalan|13|0|2026-05-18|`pemeriksaan_obstetri_ralan`|
|Penilaian Awal Keperawatan Kebidanan Rawat Jalan|1|0|2025-06-23 11:30:34|`penilaian_awal_keperawatan_kebidanan`|
|Penilaian Awal Keperawatan Rawat Jalan Bayi/Anak|10|0|2026-06-10 15:58:08|`penilaian_awal_keperawatan_ralan_bayi`|
|Penilaian Awal Keperawatan Rawat Jalan Umum|48|0|2026-05-24 16:09:28|`penilaian_awal_keperawatan_ralan`|
|Penilaian Awal Medis Rawat Jalan Bayi/Anak|67|0|2026-09-15 16:04:11|`penilaian_medis_ralan_anak`|
|Penilaian Awal Medis Rawat Jalan Kebidanan & Kandungan|3156|0|2026-09-15 21:14:26|`penilaian_medis_ralan_kandungan`|
|Penilaian Awal Medis Rawat Jalan Mata|1|0|2025-09-30 13:52:50|`penilaian_medis_ralan_mata`|
|Penilaian Awal Medis Rawat Jalan Penyakit Dalam|11|0|2026-08-27 21:09:28|`penilaian_medis_ralan_penyakit_dalam`|
|Penilaian Awal Medis Rawat Jalan Psikiatri|1|0|2025-08-17 15:10:49|`penilaian_medis_ralan_psikiatrik`|
|Penilaian Lanjutan Risiko Jatuh Pasien Dewasa|2|0|2025-06-30 08:57:53|`penilaian_lanjutan_resiko_jatuh_dewasa`|
|Penilaian Medical Check Up (MCU)|7|0|2025-10-21 08:17:18|`penilaian_mcu`|
|Penilaian Ulang Nyeri (form lama)|1|0|2025-06-28 09:50:31|`penilaian_ulang_nyeri`|
|Penolakan Anjuran Medis|1|0|2025-07-29 14:24:39|`surat_penolakan_anjuran_medis`|
|Resume Medis Pasien Rawat Jalan|529|Tidak tersedia|Tidak tersedia|`resume_pasien`|
|Skrining Nutrisi Pasien Dewasa|1|0|2025-06-30 08:57:02|`skrining_nutrisi_dewasa`|
|Skrining Rawat Jalan|1|0|2025-08-11|`skrining_rawat_jalan`|
|Surat Keterangan Bebas Narkoba|1|0|2025-06-04|`surat_skbn`|
|Surat Keterangan Hamil/Tidak Hamil|1|0|2025-06-30|`surat_hamil`|
|Surat Keterangan Kontrol|1|0|2026-06-25|`surat_kontrol`|
|Surat Keterangan Sakit Pihak Ke 2|1|0|2025-08-25|`suratsakitpihak2`|
|Surat Keterangan Sehat|1|0|2025-08-25|`surat_keterangan_sehat`|

## Rawat Inap

| Nama form | Catatan tersimpan | Isian 30 hari | Tanggal terbesar tercatat | Tabel sumber |
|---|---:|---:|---|---|
|SOAP / Pemeriksaan Rawat Inap|43058|2327|2027-08-01 (melewati tanggal audit)|`pemeriksaan_ranap`|
|Asesmen Ulang Nyeri (RM 7.1)|765|193|2026-11-10 09:24:33|`asesmen_ulang_nyeri`|
|Resume Medis Rawat Inap V2|1270|168|2026-11-10 10:45:56|`resume_medis_ranap_v2`|
|Dokumentasi Foto Rawat Inap|593|126|2026-11-09 21:34:26|`dokumentasi_foto_ranap`|
|Asuhan Gizi Pasien|601|77|2026-11-09|`asuhan_gizi`|
|Asesmen Kebidanan|208|65|2026-11-09|`asesmen_kebidanan`|
|Asesmen Risiko Jatuh Pasien Dewasa (Morse Fall Scale)|190|64|2026-11-09 19:24:35|`risiko_jatuh_morse`|
|Persetujuan Rawat Inap|382|53|2026-11-09|`surat_persetujuan_rawat_inap`|
|Asesmen Keperawatan Bayi|266|49|2026-11-09|`asesmen_keperawatan_bayi`|
|Early Warning Scoring System (Bayi) - RM 9.1|82|46|2026-11-10 00:01:48|`ews_bayi`|
|Skrining Gizi Lanjut|370|44|2026-11-07 12:19:23|`skrining_gizi`|
|Penilaian Awal Keperawatan Kebidanan Rawat Inap|134|40|2026-11-09 02:25:38|`penilaian_awal_keperawatan_kebidanan_ranap`|
|Penilaian Awal Keperawatan Rawat Inap Anak|236|40|2026-11-10 07:37:56|`penilaian_awal_keperawatan_ranap_anak`|
|Skrining Nutrisi Pasien Anak|243|37|2026-11-10 13:03:17|`skrining_nutrisi_anak`|
|Early Warning Scoring System (Dewasa) - RM 9.1|52|35|2026-11-09 19:23:22|`ews_dewasa`|
|Penilaian Awal Keperawatan Rawat Inap Umum|32|1|2026-11-05 06:15:25|`penilaian_awal_keperawatan_ranap`|
|Penilaian Awal Medis Rawat Inap Umum|11|1|2026-10-20 02:00:07|`penilaian_medis_ranap`|
|Asesmen Awal Medis Bedah / Pra Bedah (RM 22)|1|0|2026-10-03|`asesmen_awal_medis_bedah`|
|Askep Perioperatif (RM 28)|1|0|2026-10-03|`askep_perioperatif`|
|Catatan Keperawatan Rawat Inap|1|0|2025-06-28|`catatan_keperawatan_ranap`|
|Catatan Observasi Rawat Inap Kebidanan|2|0|2026-08-05|`catatan_observasi_ranap_kebidanan`|
|Catatan Observasi Rawat Inap Post Partum/Post SC|3|0|2026-05-09|`catatan_observasi_ranap_postpartum`|
|Ceklis Keselamatan Pembedahan (RM 29)|1|0|2026-10-03|`ceklis_keselamatan_pembedahan`|
|Check List Post Operasi|1|0|2026-04-23 10:30:48|`checklist_post_operasi`|
|Check List Pre Operasi|1|0|2026-04-27 12:24:49|`checklist_pre_operasi`|
|Form A - Evaluasi Awal Manajer Pelayanan Pasien|2|0|2025-08-26 13:06:30|`mpp_evaluasi`|
|Form B - Catatan Implementasi Manajer Pelayanan Pasien|2|0|2025-08-26 13:07:05|`mpp_evaluasi_catatan`|
|Formulir Hasil Pemeriksaan ECHOCARDIOGRAFI|2|0|2025-10-15 09:34:00|`hasil_pemeriksaan_echo`|
|Formulir Penandaan Lokasi Operasi - Wanita (RM 30)|1|0|2026-10-03|`penandaan_lokasi_operasi`|
|Monitoring & Evaluasi Asuhan Gizi|2|0|2026-04-28 14:54:34|`monitoring_asuhan_gizi`|
|Pemantauan PEWS Pasien Anak|3|0|2025-08-11 16:24:17|`pemantauan_pews_anak`|
|Pemeriksaan Obstetri Rawat Inap|14|0|2026-06-15|`pemeriksaan_obstetri_ranap`|
|Penilaian Awal Medis Rawat Inap Kebidanan & Kandungan|96|0|2026-10-09 20:33:11|`penilaian_medis_ranap_kandungan`|
|Penilaian Pre Anestesi|1|0|2025-08-05 13:48:39|`penilaian_pre_anestesi`|
|Penilaian Tambahan Pasien Geriatri|1|0|2025-06-30 11:45:26|`penilaian_tambahan_geriatri`|
|Persetujuan Penundaan Pelayanan|1|0|2025-07-29 14:25:47|`persetujuan_penundaan_pelayanan`|
|Resume Medis Pasien Rawat Inap|1480|0|2026-05-22 15:12:14|`resume_pasien_ranap`|
|Risiko Jatuh - Get Up and Go Test|23|0|2026-10-07|`risiko_jatuh_getupandgo`|
|Skrining Manager Pelayanan Pasien|3|0|2025-08-26|`mpp_skrining`|
|Surat Keterangan Lahir|8|0|2026-09-16 08:33:07|`surat_keterangan_lahir`|
|Surat Keterangan Rawat Inap|1|0|2025-08-19|`surat_keterangan_rawat_inap`|
|Transfer Pasien Antar Ruang|3|0|2026-03-11 10:44:18|`transfer_pasien_antar_ruang`|

## Bersama / IGD / Penunjang

| Nama form | Catatan tersimpan | Isian 30 hari | Tanggal terbesar tercatat | Tabel sumber |
|---|---:|---:|---|---|
|Hasil Pemeriksaan Laboratorium Patologi Klinis|2229|377|2026-11-10|`periksa_lab`|
|Permintaan Laboratorium Patologi Klinis|1379|184|2026-11-10|`permintaan_lab`|
|Triase IGD|772|118|2026-11-10 02:02:38|`data_triase_igd`|
|Asesmen Risiko Jatuh Pasien Bayi dan Anak (Humpty Dumpty Scale)|318|99|2026-11-10 00:01:24|`risiko_jatuh_bayi_anak`|
|Catatan ADIME Gizi|545|77|2026-11-09 11:51:09|`catatan_adime_gizi`|
|Lembar Transfer Pasien Internal (RM 38)|250|72|2026-11-09 22:31:42|`lembar_transfer_pasien_internal`|
|Early Warning Scoring System (Anak) - RM 9.1|134|55|2026-11-09 22:30:36|`ews_anak`|
|Ringkasan Riwayat Masuk dan Keluar Rumah Sakit (RM 2a)|167|46|2026-11-09 22:21:47|`ringkasan_riwayat_masuk`|
|Laporan Operasi|301|41|2026-11-10 00:40:41|`laporan_operasi`|
|Laporan Operasi (draft tersimpan)|268|40|2026-11-10 00:41:50|`draft_laporan_operasi`|
|SBAR Perawatan Pasien|102|28|2026-11-09|`sbar_pasien`|
|Hasil Pemeriksaan Radiologi|156|26|2026-11-09|`hasil_radiologi`|
|Pemeriksaan Radiologi|170|26|2026-11-09|`periksa_radiologi`|
|Pengkajian Awal Medis IGD|89|6|2026-11-06 20:52:10|`penilaian_medis_igd`|
|Rekonsiliasi Obat|3|2|2026-11-03 06:18:11|`rekonsiliasi_obat`|
|Asesmen Pasien UGD V2|1|0|2026-04-26 01:49:53|`asesmen_pasien_ugd_v2`|
|Catatan Observasi IGD|4|0|2025-09-02|`catatan_observasi_igd`|
|Check List Kriteria Masuk HCU|3|0|2025-10-14 09:24:19|`checklist_kriteria_masuk_hcu`|
|Diagnosis Pasien (ICD-10)|29547|Tidak tersedia|Tidak tersedia|`diagnosa_pasien`|
|Hasil Pemeriksaan EKG|2|0|2025-08-11 15:04:08|`hasil_pemeriksaan_ekg`|
|Hasil Pemeriksaan USG Kebidanan|2|0|2025-08-19 10:36:43|`hasil_pemeriksaan_usg`|
|Konseling Farmasi|9|0|2025-09-16 10:53:28|`konseling_farmasi`|
|Laporan Hasil Kritis Laboratorium|5|0|2026-09-26|`lapor_kritis_lab`|
|Monitoring Reaksi Tranfusi|2|0|2025-06-30|`monitoring_reaksi_tranfusi`|
|Penilaian Awal Keperawatan IGD|5|0|2026-04-29 07:28:30|`penilaian_awal_keperawatan_igd`|
|Penilaian Lanjutan Risiko Jatuh Pasien Geriatri|1|0|2023-07-12 10:04:46|`penilaian_lanjutan_resiko_jatuh_geriatri`|
|Penilaian Lanjutan Risiko Jatuh Pasien Lansia|2|0|2023-08-18 21:34:34|`penilaian_lanjutan_resiko_jatuh_lansia`|
|Perencanaan Pemulangan Pasien|3|0|2025-09-02|`perencanaan_pemulangan`|
|Pernyataan Persetujuan/Penolakan Tindakan Kedokteran|20|0|2025-10-21|`persetujuan_penolakan_tindakan`|
|Persetujuan Umum|10|0|2025-10-14|`surat_persetujuan_umum`|
|Prosedur Pasien (ICD-9)|3121|Tidak tersedia|Tidak tersedia|`prosedur_pasien`|
|Sign-In Sebelum Anestesi|2|0|2026-04-22 15:28:31|`signin_sebelum_anestesi`|
|Surat Keterangan Bebas Tato|2|0|2025-06-30|`surat_bebas_tato`|
|Surat Keterangan Buta Warna|2|0|2025-06-30|`surat_buta_warna`|
|Surat Keterangan Sakit|5|0|2025-08-26|`suratsakit`|
|Telaah Resep dan Telaah Obat|494|Tidak tersedia|Tidak tersedia|`telaah_farmasi`|

## Catatan verifikasi

Audit menghitung keberadaan catatan, tanpa menilai kelengkapan jawaban klinis. Form yang hanya satu atau dua kali diisi tetap disertakan sesuai kriteria pengguna. Form tanpa catatan tidak masuk daftar utama. Data bertanggal setelah hari audit tidak dihitung sebagai aktivitas 30 hari terakhir. Contohnya tabel SOAP rawat inap memiliki tanggal maksimum 2027-08-01; tanggal tersebut ditampilkan sebagai nilai database dan tidak dianggap aktivitas pada hari audit. Tidak ada migrasi, perubahan kode aplikasi, perubahan data, atau pengiriman SATUSEHAT pada audit ini.
