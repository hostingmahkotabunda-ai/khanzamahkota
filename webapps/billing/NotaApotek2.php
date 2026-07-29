<?php
 include '../conf/conf.php';
?>
<!DOCTYPE html>
<html lang="id">
<head>
<meta charset="utf-8">
<title>Nota Apotek</title>
<style>
@page {
  size: 58mm auto;
  margin: 0;
}
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  width: 58mm;
  font-family: Arial, Helvetica, sans-serif;
  font-size: 11px;
  font-weight: normal;
  line-height: 1.3;
  letter-spacing: 0;
  background: #ffffff;
}
.w { padding: 2mm 1.5mm; }
.c { text-align: center; }
.r { float: right; }
.b { font-weight: bold; }
.ln { border-top: 1px dashed #000; margin: 1.5mm 0; clear: both; }
.row { clear: both; margin-bottom: 1px; overflow: hidden; }
.s { font-size: 10px; line-height: 1.4; }
h3 { font-size: 12px; font-weight: bold; margin-bottom: 2px; line-height: 1.3; }
.section-header {
  font-size: 10px;
  font-weight: bold;
  margin-top: 2mm;
  margin-bottom: 0.5mm;
  text-transform: uppercase;
}
.item-aturan {
  font-size: 10px;
  font-style: italic;
  margin-left: 2mm;
  margin-bottom: 1mm;
}
img.logo { width: 35px; height: 35px; }
</style>
</head>
<body onload="window.print()">
<?php
    reportsqlinjection();
    $usere      = trim(isset($_GET['usere']))?trim($_GET['usere']):NULL;
    $passwordte = trim(isset($_GET['passwordte']))?trim($_GET['passwordte']):NULL;
    if((USERHYBRIDWEB==$usere)&&(PASHYBRIDWEB==$passwordte)){
        $nonota    = validTeks4(str_replace("_"," ",$_GET['nonota']),20);

        $_sql      = "SELECT penjualan.tgl_jual,penjualan.nip,penjualan.no_rkm_medis,penjualan.nm_pasien,penjualan.keterangan,penjualan.ongkir,penjualan.ppn,penjualan.ppn_umum,penjualan.nama_bayar from penjualan where penjualan.nota_jual='$nonota'";
        $hasil     = mysqli_fetch_array(bukaquery($_sql));

        $tanggal   = $hasil["tgl_jual"];
        $catatan   = $hasil["keterangan"];
        $petugas   = getOne("select petugas.nama from petugas where petugas.nip='".$hasil["nip"]."'");
        $norm      = $hasil["no_rkm_medis"];
        $pasien    = $hasil["nm_pasien"];
        $ongkir    = $hasil["ongkir"];
        $ppnobat   = $hasil["ppn"];
        // PPN umum diambil LANGSUNG dari yang tersimpan saat transaksi (field "PPN(%)" di
        // DlgPenjualan) -- BUKAN dihitung ulang dari akun_bayar.ppn (persen tetap per metode
        // bayar), krn itu tidak selalu sama dgn yang dipakai/diketik user saat transaksi.
        $nilaippn  = $hasil["ppn_umum"];

        $_sql = "select detailjual.kode_brng,databarang.nama_brng, detailjual.kode_sat,
                 kodesatuan.satuan,detailjual.h_jual, detailjual.jumlah,
                 detailjual.subtotal, detailjual.dis, detailjual.bsr_dis,detailjual.tambahan,detailjual.total,detailjual.aturan_pakai from
                 detailjual inner join databarang inner join kodesatuan inner join jenis
                 on detailjual.kode_brng=databarang.kode_brng and databarang.kdjns=jenis.kdjns
                 and detailjual.kode_sat=kodesatuan.kode_sat where
                 detailjual.nota_jual='$nonota'";
        $hasil = bukaquery($_sql);

        $setting = mysqli_fetch_array(bukaquery("select setting.nama_instansi,setting.alamat_instansi,setting.kabupaten,setting.propinsi,setting.kontak,setting.email,setting.logo from setting"));

        echo "<div class='w'>";

        // ===== HEADER =====
        echo "<div class='c'>";
        echo "<img class='logo' src='data:image/jpeg;base64,". base64_encode($setting['logo']). "'/>";
        echo "<h3>".$setting["nama_instansi"]."</h3>";
        echo "<div class='s'>".$setting["alamat_instansi"]."</div>";
        echo "<div class='s'>".$setting["kabupaten"].", ".$setting["propinsi"]."</div>";
        echo "<div class='s'>".$setting["kontak"]." | ".$setting["email"]."</div>";
        echo "<div class='b' style='margin-top:1mm;'>NOTA APOTEK</div>";
        echo "</div>";

        echo "<div class='ln'></div>";

        // ===== INFO PASIEN =====
        echo "<div class='row s'>No.RM : $norm</div>";
        echo "<div class='row s'>No.Nota : $nonota</div>";
        echo "<div class='row s'>Pasien : $pasien</div>";
        echo "<div class='row s'>Tanggal : $tanggal</div>";
        echo "<div class='row s'>Petugas : $petugas</div>";
        if(trim($catatan) !== ""){
            echo "<div class='row s'>Catatan : $catatan</div>";
        }

        echo "<div class='ln'></div>";

        // ===== DAFTAR ITEM =====
        echo "<div class='section-header'>DAFTAR OBAT</div>";
        $ttlpesan = 0;
        $i = 1;
        while($barispesan = mysqli_fetch_array($hasil)) {
            $ttlpesan = $ttlpesan + $barispesan["total"];
            $jml = $barispesan["jumlah"]." ".$barispesan["satuan"];
            echo "<div class='row'>".$i.". ".$barispesan["nama_brng"]."</div>";
            echo "<div class='row s'>&nbsp;&nbsp;".$jml." x ".formatDuit2($barispesan["h_jual"])."<span class='r'>".formatDuit2($barispesan["total"])."</span></div>";
            if(trim($barispesan["aturan_pakai"]) !== ""){
                echo "<div class='item-aturan'>".$barispesan["aturan_pakai"]."</div>";
            }
            $i++;
        }

        echo "<div class='ln'></div>";

        // ===== TOTAL =====
        echo "<div class='row'>Tagihan<span class='r'>".formatDuit2($ttlpesan)."</span></div>";
        echo "<div class='row'>PPN<span class='r'>".formatDuit2($nilaippn+$ppnobat)."</span></div>";
        echo "<div class='row'>Ongkos Kirim<span class='r'>".formatDuit2($ongkir)."</span></div>";
        echo "<div class='ln'></div>";
        echo "<div class='row b' style='font-size:12px;'>TOTAL BAYAR<span class='r'>".formatDuit2($ttlpesan+$nilaippn+$ongkir+$ppnobat)."</span></div>";

        echo "<div class='ln'></div>";
        echo "<div class='c s'>Terima Kasih Atas Kunjungan Anda</div>";

        echo "</div>"; // end .w
    }else {
        exit(header("Location:../index.php"));
    }
?>
</body>
</html>
