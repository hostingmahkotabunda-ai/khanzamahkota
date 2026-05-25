<?php
   include '../conf/conf.php';
   include '../phpqrcode/qrlib.php';
?>
<!DOCTYPE html>
<html lang="id">
<head>
<meta charset="utf-8">
<title>Nota Billing Rawat Jalan</title>
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
img.qr { width: 45px; height: 45px; }
</style>
</head>
<body onload="window.print()">
<?php
    reportsqlinjection();
    $usere      = trim(isset($_GET['usere']))?trim($_GET['usere']):NULL;
    $passwordte = trim(isset($_GET['passwordte']))?trim($_GET['passwordte']):NULL;
    if((USERHYBRIDWEB==$usere)&&(PASHYBRIDWEB==$passwordte)){
        $petugas        = validTeks4(str_replace("_"," ",$_GET['petugas']),20);
        $tanggal        = validTeks4(str_replace("_"," ",$_GET['tanggal']),20);
        $nonota         = str_replace(": ","",getOne("select temporary_bayar_ralan.temp2 from temporary_bayar_ralan where temporary_bayar_ralan.temp9='$petugas' and temporary_bayar_ralan.temp1='No.Nota'"));
        $norawat        = getOne("select nota_jalan.no_rawat from nota_jalan where nota_jalan.no_nota='$nonota'");
        $kodecarabayar  = getOne("select reg_periksa.kd_pj from reg_periksa where reg_periksa.no_rawat='$norawat'");
        $carabayar      = getOne("select penjab.png_jawab from penjab where penjab.kd_pj='$kodecarabayar'");
        $PNG_TEMP_DIR   = dirname(__FILE__).DIRECTORY_SEPARATOR.'temp'.DIRECTORY_SEPARATOR;
        $PNG_WEB_DIR    = 'temp/';
        if (!file_exists($PNG_TEMP_DIR)) mkdir($PNG_TEMP_DIR);
        $_sql           = "select temporary_bayar_ralan.temp1,temporary_bayar_ralan.temp2,temporary_bayar_ralan.temp3,temporary_bayar_ralan.temp4,temporary_bayar_ralan.temp5,temporary_bayar_ralan.temp6,temporary_bayar_ralan.temp7,temporary_bayar_ralan.temp8,temporary_bayar_ralan.temp9,temporary_bayar_ralan.temp10,temporary_bayar_ralan.temp11,temporary_bayar_ralan.temp12,temporary_bayar_ralan.temp13,temporary_bayar_ralan.temp14 from temporary_bayar_ralan where temporary_bayar_ralan.temp9='$petugas' order by temporary_bayar_ralan.no asc";
        $hasil          = bukaquery($_sql);

        if(mysqli_num_rows($hasil)!=0) {
            $setting = mysqli_fetch_array(bukaquery("select setting.nama_instansi,setting.alamat_instansi,setting.kabupaten,setting.propinsi,setting.kontak,setting.email,setting.logo from setting"));

            echo "<div class='w'>";

            // ===== HEADER =====
            echo "<div class='c'>";
            echo "<img class='logo' src='data:image/jpeg;base64,". base64_encode($setting['logo']). "'/>";
            echo "<h3>".$setting["nama_instansi"]."</h3>";
            echo "<div class='s'>".$setting["alamat_instansi"]."</div>";
            echo "<div class='s'>".$setting["kabupaten"].", ".$setting["propinsi"]."</div>";
            echo "<div class='s'>".$setting["kontak"]." | ".$setting["email"]."</div>";
            echo "<div class='b' style='margin-top:1mm;'>BILLING - ".$carabayar."</div>";
            echo "</div>";

            echo "<div class='ln'></div>";

            // ===== INFO PASIEN (6 baris pertama, alamat dilewati) =====
            $z=1;
            while($inapdrpasien = mysqli_fetch_array($hasil)) {
                if($z<=6){
                    if(stripos($inapdrpasien[0], 'Alamat') === false){
                        echo "<div class='row s'>".str_replace("  ","&nbsp;&nbsp;",$inapdrpasien[0])." ".$inapdrpasien[1]."</div>";
                    }
                }
                $z++;
            }

            // ===== DOKTER =====
            $_sql = "select temporary_bayar_ralan.temp2 from temporary_bayar_ralan where temporary_bayar_ralan.temp9='$petugas' and temporary_bayar_ralan.temp8='Dokter' group by temporary_bayar_ralan.temp2 order by temporary_bayar_ralan.no asc";
            $hasil = bukaquery($_sql);
            $dokterList = "";
            while($inapdrpasien = mysqli_fetch_array($hasil)) {
                $dokterList .= " ".$inapdrpasien[0];
            }
            if(trim($dokterList) !== ""){
                echo "<div class='row s'>Dokter :".$dokterList."</div>";
            }

            echo "<div class='ln'></div>";

            // ===== ADMINISTRASI RM (Registrasi) =====
            $hasil2 = bukaquery("select temporary_bayar_ralan.temp1,temporary_bayar_ralan.temp2,temporary_bayar_ralan.temp3,temporary_bayar_ralan.temp7 from temporary_bayar_ralan where temporary_bayar_ralan.temp9='$petugas' and temporary_bayar_ralan.temp8='Registrasi' order by temporary_bayar_ralan.no asc");
            if(mysqli_num_rows($hasil2)!=0){
                echo "<div class='section-header'>ADMINISTRASI RM</div>";
                while($inapdrpasien = mysqli_fetch_array($hasil2)) {
                    echo "<div class='row'>".$inapdrpasien[1]."<span class='r'>".$inapdrpasien[3]."</span></div>";
                }
            }

            // ===== TINDAKAN =====
            $hasil3 = bukaquery("select temporary_bayar_ralan.temp1,temporary_bayar_ralan.temp2,temporary_bayar_ralan.temp3,temporary_bayar_ralan.temp7,temporary_bayar_ralan.temp5 from temporary_bayar_ralan where temporary_bayar_ralan.temp9='$petugas' and (temporary_bayar_ralan.temp8='Ralan Dokter' or temporary_bayar_ralan.temp8='Ralan Dokter Paramedis' or temporary_bayar_ralan.temp8='Ralan Paramedis' or temporary_bayar_ralan.temp8='Laborat' or temporary_bayar_ralan.temp8='Radiologi') order by temporary_bayar_ralan.no asc");
            $tindakanHeader = false;
            while($inapdrpasien = mysqli_fetch_array($hasil3)) {
                if(!empty($inapdrpasien[3])){
                    if(!$tindakanHeader){
                        echo "<div class='section-header'>TINDAKAN</div>";
                        $tindakanHeader = true;
                    }
                    $qty = !empty($inapdrpasien[4]) ? " x".$inapdrpasien[4] : "";
                    echo "<div class='row'>".$inapdrpasien[1].$qty."<span class='r'>".$inapdrpasien[3]."</span></div>";
                }
            }

            // ===== OPERASI / VK =====
            $hasil3 = bukaquery("select temporary_bayar_ralan.temp1,temporary_bayar_ralan.temp2,temporary_bayar_ralan.temp3,temporary_bayar_ralan.temp7,temporary_bayar_ralan.temp5 from temporary_bayar_ralan where temporary_bayar_ralan.temp9='$petugas' and temporary_bayar_ralan.temp8='Operasi' order by temporary_bayar_ralan.no asc");
            if(mysqli_num_rows($hasil3)!=0){
                $opHeader = false;
                while($inapdrpasien = mysqli_fetch_array($hasil3)) {
                    if(!empty($inapdrpasien[3])){
                        if(!$opHeader){
                            echo "<div class='section-header'>OPERASI / VK</div>";
                            $opHeader = true;
                        }
                        $qty = !empty($inapdrpasien[4]) ? " x".$inapdrpasien[4] : "";
                        echo "<div class='row'>".$inapdrpasien[1].$qty."<span class='r'>".$inapdrpasien[3]."</span></div>";
                    }
                }
            }

            // ===== OBAT & BHP =====
            $hasil4 = bukaquery("select temporary_bayar_ralan.temp1,temporary_bayar_ralan.temp2,temporary_bayar_ralan.temp3,temporary_bayar_ralan.temp7,temporary_bayar_ralan.temp8,temporary_bayar_ralan.temp5 from temporary_bayar_ralan where temporary_bayar_ralan.temp9='$petugas' and (temporary_bayar_ralan.temp8='Obat' or temporary_bayar_ralan.temp8='TtlObat') group by temporary_bayar_ralan.temp2 order by temporary_bayar_ralan.no asc");
            if(mysqli_num_rows($hasil4)!=0){
                $obatHeader = false;
                while($inapdrpasien = mysqli_fetch_array($hasil4)) {
                    if(!empty($inapdrpasien[3])){
                        if(!$obatHeader){
                            echo "<div class='section-header'>OBAT &amp; BHP</div>";
                            $obatHeader = true;
                        }
                        $qty = !empty($inapdrpasien[5]) ? " x".$inapdrpasien[5] : "";
                        echo "<div class='row'>".$inapdrpasien[1].$qty."<span class='r'>".$inapdrpasien[3]."</span></div>";
                    } else if($inapdrpasien["temp8"]=="TtlObat"){
                        echo "<div class='row b'>Subtotal Obat<span class='r'>".$inapdrpasien["temp2"]."</span></div>";
                    }
                }
            }

            // ===== POTONGAN =====
            $hasil5 = bukaquery("select temporary_bayar_ralan.temp1,temporary_bayar_ralan.temp2,temporary_bayar_ralan.temp3,temporary_bayar_ralan.temp7,temporary_bayar_ralan.temp5 from temporary_bayar_ralan where temporary_bayar_ralan.temp9='$petugas' and temporary_bayar_ralan.temp8='Potongan' order by temporary_bayar_ralan.no asc");
            while($inapdrpasien = mysqli_fetch_array($hasil5)) {
                echo "<div class='row'>".$inapdrpasien[0]." ".$inapdrpasien[1]."<span class='r'>-".$inapdrpasien[3]."</span></div>";
            }

            // ===== TAMBAHAN =====
            $hasil6 = bukaquery("select temporary_bayar_ralan.temp1,temporary_bayar_ralan.temp2,temporary_bayar_ralan.temp3,temporary_bayar_ralan.temp7,temporary_bayar_ralan.temp5 from temporary_bayar_ralan where temporary_bayar_ralan.temp9='$petugas' and temporary_bayar_ralan.temp8='Tambahan' order by temporary_bayar_ralan.no asc");
            while($inapdrpasien = mysqli_fetch_array($hasil6)) {
                echo "<div class='row'>".$inapdrpasien[0]." ".$inapdrpasien[1]."<span class='r'>".$inapdrpasien[3]."</span></div>";
            }

            // ===== ITEM LAIN (temp8 = '-') =====
            $hasil7 = bukaquery("select temporary_bayar_ralan.temp1,temporary_bayar_ralan.temp2,temporary_bayar_ralan.temp3,temporary_bayar_ralan.temp7,temporary_bayar_ralan.temp5 from temporary_bayar_ralan where temporary_bayar_ralan.temp9='$petugas' and temporary_bayar_ralan.temp8='-' and temporary_bayar_ralan.temp7<>'' group by temporary_bayar_ralan.temp2 order by temporary_bayar_ralan.no asc");
            while($inapdrpasien = mysqli_fetch_array($hasil7)) {
                echo "<div class='row'>".$inapdrpasien[0]." ".$inapdrpasien[1]."<span class='r'>".$inapdrpasien[3]."</span></div>";
            }

            echo "<div class='ln'></div>";

            // ===== TOTAL TAGIHAN / PPN / TOTAL BAYAR =====
            $hasil7 = bukaquery("select temporary_bayar_ralan.temp1,temporary_bayar_ralan.temp2,temporary_bayar_ralan.temp3,temporary_bayar_ralan.temp7 from temporary_bayar_ralan where temporary_bayar_ralan.temp9='$petugas' and temporary_bayar_ralan.temp8='Tagihan' and temporary_bayar_ralan.temp7<>'' order by temporary_bayar_ralan.no asc");
            while($inapdrpasien = mysqli_fetch_array($hasil7)) {
                if($inapdrpasien["temp1"]=="TOTAL BAYAR"){
                    echo "<div class='row b' style='font-size:12px;'>".$inapdrpasien[0]."<span class='r'>".$inapdrpasien[3]."</span></div>";
                }else{
                    echo "<div class='row b'>".$inapdrpasien[0]."<span class='r'>".$inapdrpasien[3]."</span></div>";
                }
            }

            echo "<div class='ln'></div>";

            // ===== FOOTER (Tanggal & TTD Petugas + QR) =====
            echo "<div class='c s'>".getOne("select setting.kabupaten from setting").", ".$tanggal."</div>";
            echo "<div class='c s' style='margin-top:1mm;'>Petugas</div>";

            if(getOne("select count(petugas.nama) from petugas where petugas.nip='$petugas'")>=1){
                $filename               = $PNG_TEMP_DIR.$petugas.'.png';
                $errorCorrectionLevel   = 'L';
                $matrixPointSize        = 4;
                QRcode::png("Dikeluarkan di ".$setting["nama_instansi"].", Kabupaten/Kota ".$setting["kabupaten"]."\nDitandatangani secara elektronik oleh ".getOne("select petugas.nama from petugas where petugas.nip='$petugas'")."\nID  ".getOne3("select ifnull(sha1(sidikjari.sidikjari),'".$petugas."') from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik='".$petugas."'",$petugas)."\n".$tanggal, $filename, $errorCorrectionLevel, $matrixPointSize, 2);
                echo "<div class='c'><img class='qr' src='".$PNG_WEB_DIR.basename($filename)."'/></div>";
                echo "<div class='c s b'>( ".getOne("select petugas.nama from petugas where petugas.nip='$petugas'")." )</div>";
            }else{
                $filename               = $PNG_TEMP_DIR.$petugas.'.png';
                $errorCorrectionLevel   = 'L';
                $matrixPointSize        = 4;
                QRcode::png("Dikeluarkan di ".$setting["nama_instansi"].", Kabupaten/Kota ".$setting["kabupaten"]."\nDitandatangani secara elektronik oleh Admin Utama\nID ADMIN\n".$tanggal, $filename, $errorCorrectionLevel, $matrixPointSize, 2);
                echo "<div class='c'><img class='qr' src='".$PNG_WEB_DIR.basename($filename)."'/></div>";
                echo "<div class='c s b'>( Admin Utama )</div>";
            }

            echo "<div class='ln'></div>";
            echo "<div class='c s'>Terima Kasih Atas Kunjungan Anda</div>";

            echo "</div>"; // end .w
        } else {
            echo "<div class='w c b'>Data Billing masih kosong !</div>";
        }
    }else{
        exit(header("Location:../index.php"));
    }
?>
</body>
</html>
