<?php
 include '../conf/conf.php';
?>
<!DOCTYPE html>
<html lang="id">
<head>
<meta charset="utf-8">
<title>Resep Dokter - Thermal</title>
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
img.logo { width: 35px; height: 35px; }
</style>
</head>
<body onload="window.print()">
<?php
    function hResep($teks){
        return htmlspecialchars($teks,ENT_QUOTES,'UTF-8');
    }

    reportsqlinjection();
    $usere      = trim(isset($_GET['usere']))?trim($_GET['usere']):NULL;
    $passwordte = trim(isset($_GET['passwordte']))?trim($_GET['passwordte']):NULL;
    if((USERHYBRIDWEB==$usere)&&(PASHYBRIDWEB==$passwordte)){
        $no_resep = validTeks4(isset($_GET['no_resep'])?$_GET['no_resep']:NULL,14);

        $_sql = "select resep_obat.no_resep,resep_obat.tgl_peresepan,resep_obat.jam_peresepan,".
                "resep_obat.tgl_perawatan,resep_obat.jam,resep_obat.no_rawat,pasien.no_rkm_medis,".
                "pasien.nm_pasien,dokter.nm_dokter,poliklinik.nm_poli,penjab.png_jawab ".
                "from resep_obat inner join reg_periksa on resep_obat.no_rawat=reg_periksa.no_rawat ".
                "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis ".
                "inner join dokter on resep_obat.kd_dokter=dokter.kd_dokter ".
                "inner join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli ".
                "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj ".
                "where resep_obat.no_resep='$no_resep'";
        $hasil=bukaquery($_sql);

        if(mysqli_num_rows($hasil)!=0) {
            $setting = mysqli_fetch_array(bukaquery("select setting.nama_instansi,setting.alamat_instansi,setting.kabupaten,setting.propinsi,setting.kontak,setting.email,setting.logo from setting"));
            $resep   = mysqli_fetch_array($hasil);

            echo "<div class='w'>";

            // ===== HEADER =====
            echo "<div class='c'>";
            echo "<img class='logo' src='data:image/jpeg;base64,".base64_encode($setting['logo'])."'/>";
            echo "<h3>".hResep($setting['nama_instansi'])."</h3>";
            echo "<div class='s'>".hResep($setting['alamat_instansi'])."</div>";
            echo "<div class='s'>".hResep($setting['kabupaten']).", ".hResep($setting['propinsi'])."</div>";
            echo "<div class='s'>".hResep($setting['kontak'])."</div>";
            echo "<div class='b' style='margin-top:1mm;'>RESEP DOKTER</div>";
            echo "</div>";

            echo "<div class='ln'></div>";

            // ===== INFO PASIEN & RESEP =====
            echo "<div class='row s'><span class='b'>No.Resep</span><span class='r'>".hResep($resep['no_resep'])."</span></div>";
            echo "<div class='row s'><span class='b'>No.Rawat</span><span class='r'>".hResep($resep['no_rawat'])."</span></div>";
            echo "<div class='row s'><span class='b'>No.RM</span><span class='r'>".hResep($resep['no_rkm_medis'])."</span></div>";
            echo "<div class='row s'>Pasien : ".hResep($resep['nm_pasien'])."</div>";
            echo "<div class='row s'>Dokter : ".hResep($resep['nm_dokter'])."</div>";
            echo "<div class='row s'>Poli : ".hResep($resep['nm_poli'])."</div>";
            echo "<div class='row s'>Jaminan : ".hResep($resep['png_jawab'])."</div>";
            echo "<div class='row s'>Tgl.Resep : ".hResep($resep['tgl_peresepan'])." ".hResep($resep['jam_peresepan'])."</div>";

            echo "<div class='ln'></div>";

            // ===== OBAT =====
            echo "<div class='section-header'>OBAT & DAFTAR</div>";

            $i=1;
            $obat=bukaquery("select resep_dokter.jml,databarang.kode_sat,databarang.nama_brng,resep_dokter.aturan_pakai ".
                            "from resep_dokter inner join databarang on resep_dokter.kode_brng=databarang.kode_brng ".
                            "where resep_dokter.no_resep='$no_resep' order by databarang.nama_brng");
            $adaObat = false;
            while($barisobat=mysqli_fetch_array($obat)){
                $adaObat = true;
                echo "<div class='row s'>";
                echo "<span class='b'>".$i.".</span> ".hResep($barisobat['nama_brng']);
                echo "<br/><span class='s' style='padding-left:3mm;'>".hResep($barisobat['jml'])." ".hResep($barisobat['kode_sat'])." - ".hResep($barisobat['aturan_pakai'])."</span>";
                echo "</div>";
                $i++;
            }

            // ===== RACIKAN =====
            $racikan=bukaquery("select resep_dokter_racikan.no_racik,resep_dokter_racikan.nama_racik,".
                               "resep_dokter_racikan.jml_dr,resep_dokter_racikan.aturan_pakai,".
                               "resep_dokter_racikan.keterangan,metode_racik.nm_racik ".
                               "from resep_dokter_racikan inner join metode_racik on resep_dokter_racikan.kd_racik=metode_racik.kd_racik ".
                               "where resep_dokter_racikan.no_resep='$no_resep' order by resep_dokter_racikan.no_racik");
            while($barisracik=mysqli_fetch_array($racikan)){
                $adaObat = true;
                echo "<div class='row s'>";
                echo "<span class='b'>".$i.".</span> ".hResep($barisracik['nama_racik']);
                if(trim($barisracik['keterangan'])!=""){
                    echo "<br/><span class='s' style='padding-left:3mm;'>".hResep($barisracik['keterangan'])."</span>";
                }
                echo "<br/><span class='s' style='padding-left:3mm;'>".hResep($barisracik['jml_dr'])." ".hResep($barisracik['nm_racik'])." - ".hResep($barisracik['aturan_pakai'])."</span>";
                echo "</div>";
                
                // Detail racikan
                $detail=bukaquery("select resep_dokter_racikan_detail.jml,databarang.kode_sat,databarang.nama_brng ".
                                  "from resep_dokter_racikan_detail inner join databarang on resep_dokter_racikan_detail.kode_brng=databarang.kode_brng ".
                                  "where resep_dokter_racikan_detail.no_resep='$no_resep' and resep_dokter_racikan_detail.no_racik='".validTeks4($barisracik['no_racik'],2)."' ".
                                  "order by databarang.nama_brng");
                while($barisdetail=mysqli_fetch_array($detail)){
                    echo "<div class='row s' style='padding-left:3mm;'>";
                    echo "  ✓ ".hResep($barisdetail['nama_brng'])." (".hResep($barisdetail['jml'])." ".hResep($barisdetail['kode_sat']).")";
                    echo "</div>";
                }
                $i++;
            }

            if(!$adaObat){
                echo "<div class='row s c'>Data obat masih kosong</div>";
            }

            echo "<div class='ln'></div>";

            // ===== FOOTER =====
            echo "<div class='c s'>".getOne("select setting.kabupaten from setting").", ".date('d-m-Y')."</div>";
            echo "<div class='c s' style='margin-top:2mm;'>Dokter</div>";
            echo "<div class='s' style='height:8mm;'></div>";
            echo "<div class='c s b'>".hResep($resep['nm_dokter'])."</div>";

            echo "<div class='ln'></div>";
            echo "<div class='c s'>Dicetak oleh SIMRS Khanza</div>";

            echo "</div>"; // end .w
        }else{
            echo "<div class='w c b'>Data resep tidak ditemukan !</div>";
        }
    }else{
        exit(header("Location:../index.php"));
    }
?>
</body>
</html>