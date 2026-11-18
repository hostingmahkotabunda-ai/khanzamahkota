# =====================================================================
#  SIMRS Khanza - launcher update otomatis (dipanggil jalankan-client.bat v2)
#
#  - Bandingkan version.txt server vs lokal; kalau beda, salin file yang berubah
#    dengan indikator progress (jendela grafis + bar berwarna di konsol).
#  - Aturan sama dengan launcher lama: folder "setting" & "cache" serta file
#    *.html *.csv *.bat version.txt TIDAK pernah ditimpa.
#  - version.txt lokal ditulis PALING AKHIR, jadi kalau update terputus,
#    pembukaan berikutnya otomatis mengulang.
#  - Apa pun yang gagal -> keluar dengan tenang; .bat tetap menjalankan
#    versi lokal yang ada (aplikasi tidak pernah terblokir oleh launcher).
#
#  File ini ikut tersalin ke client lewat update (bukan *.bat), jadi perubahan
#  di sini menyebar otomatis. Simpan sebagai ASCII (hindari karakter non-ASCII
#  literal: PowerShell 5.1 membaca file tanpa BOM sebagai ANSI).
#
#  -Perintah "<baris java>" (launcher v3): setelah update, launcher yang menjalankan
#    java dengan KONSOL TERSEMBUNYI (selama loading hanya splash yang tampil; Khanza
#    memunculkan konsolnya sendiri setelah aplikasi terbuka), lalu exit 0 dan .bat
#    langsung tutup. Exit 1 = gagal menjalankan -> .bat menjalankan java biasa.
#    Tanpa -Perintah (launcher v2): hanya update, .bat yang menjalankan java.
#
#  Uji tanpa jendela:  -TanpaJendela     Simpan tangkapan jendela: -Tangkap <file.png>
# =====================================================================
param(
    [Parameter(Mandatory = $true)][string]$Server,
    [Parameter(Mandatory = $true)][string]$Local,
    [string]$Perintah = '',
    [switch]$TanpaJendela,
    [string]$Tangkap = ''
)

$ErrorActionPreference = 'Stop'
$Local = $Local.TrimEnd('\')
$Server = $Server.TrimEnd('\')

$EX_DIR = @('setting', 'cache')
$EX_FILE = @('*.html', '*.csv', '*.bat', 'version.txt')

# ---------------------------------------------------------------- konsol
try { [Console]::OutputEncoding = [Text.Encoding]::UTF8 } catch {}
$VT = $false
try { $VT = [bool]$Host.UI.SupportsVirtualTerminal } catch {}
$WT = [bool]$env:WT_SESSION       # Windows Terminal: font lengkap (spinner braille, centang)
$E = [char]27
$R = if ($VT) { "$E[0m" } else { '' }
function Warna([int]$r, [int]$g, [int]$b) { if ($VT) { "$E[38;2;$r;$g;${b}m" } else { '' } }
$HIJAU = Warna 124 193 66
$PINK = Warna 233 30 120
$ABU = Warna 143 163 173
$PUTIH = Warna 230 236 239
$MERAH = Warna 240 84 84
$KUNING = Warna 242 179 61
$TEBAL = if ($VT) { "$E[1m" } else { '' }
$HAPUS = if ($VT) { "$E[K" } else { "" }
$CENTANG = if ($WT) { [string][char]0x2713 } else { [string][char]0x221A }
$SILANG = if ($WT) { [string][char]0x2717 } else { 'x' }
$BLOK = [string][char]0x2588
$KOSONG = [string][char]0x2591
$SPIN = if ($WT) { @(0x280B, 0x2819, 0x2839, 0x2838, 0x283C, 0x2834, 0x2826, 0x2827, 0x2807, 0x280F) | ForEach-Object { [string][char]$_ } } else { @('|', '/', '-', '\') }

function Tulis([string]$s) { [Console]::Out.WriteLine($s) }

function Gradasi([string]$s) {
    if (-not $VT) { return $s }
    $n = [Math]::Max(1, $s.Length - 1); $sb = New-Object Text.StringBuilder
    for ($i = 0; $i -lt $s.Length; $i++) {
        $t = $i / $n
        [void]$sb.Append((Warna ([int](124 + (233 - 124) * $t)) ([int](193 + (30 - 193) * $t)) ([int](66 + (120 - 66) * $t))))
        [void]$sb.Append($s[$i])
    }
    [void]$sb.Append($R); $sb.ToString()
}

function Ukuran([double]$b) {
    if ($b -ge 1GB) { return ('{0:N1} GB' -f ($b / 1GB)) }
    if ($b -ge 1MB) { return ('{0:N1} MB' -f ($b / 1MB)) }
    return ('{0:N0} KB' -f ($b / 1KB))
}

function Durasi([double]$detik) {
    if ($detik -lt 1) { return '< 1 dtk' }
    if ($detik -lt 60) { return ('{0:N0} dtk' -f $detik) }
    return ('{0}m {1:00}d' -f [int][Math]::Floor($detik / 60), [int]($detik % 60))
}

# ---------------------------------------------------------------- state progress
$S = @{
    Fase = 'periksa'          # periksa | unduh | rapikan | selesai | gagal
    Status = 'Memeriksa file yang berubah'
    FileNama = ''; FileKe = 0; FileJumlah = 0
    Total = [double]0; Selesai = [double]0
    Kecepatan = [double]0
    VersiLama = ''; VersiBaru = ''
    Pesan = ''
}
$jam = [Diagnostics.Stopwatch]::StartNew()
$jamUI = [Diagnostics.Stopwatch]::StartNew()
$spinKe = 0

# ---------------------------------------------------------------- jendela grafis
$form = $null; $bg = $null; $k = 1.0
$CARD_W = 612; $CARD_H = 352; $PAD = 44; $BAR_Y = 280; $BAR_H = 6; $TEXT_X = 270

function Buat-Jendela {
    Add-Type -AssemblyName System.Windows.Forms, System.Drawing
    Add-Type -Namespace KhanzaLauncher -Name Win -MemberDefinition @'
[DllImport("user32.dll")] public static extern bool SetProcessDPIAware();
[DllImport("dwmapi.dll")] public static extern int DwmSetWindowAttribute(IntPtr h, int attr, ref int val, int size);
[DllImport("user32.dll")] public static extern bool ReleaseCapture();
[DllImport("user32.dll")] public static extern IntPtr SendMessage(IntPtr h, int msg, int wp, int lp);
'@
    [void][KhanzaLauncher.Win]::SetProcessDPIAware()
    [Windows.Forms.Application]::EnableVisualStyles()

    $gDpi = [Drawing.Graphics]::FromHwnd([IntPtr]::Zero)
    $script:k = [Math]::Max(1.0, $gDpi.DpiX / 96.0); $gDpi.Dispose()

    $dir = Join-Path $Local 'launcher'
    $file = if ($script:k -gt 1.2) { Join-Path $dir 'update-bg@2x.png' } else { Join-Path $dir 'update-bg.png' }
    if (-not (Test-Path $file)) { $file = Join-Path $dir 'update-bg.png' }
    if (-not (Test-Path $file)) { throw 'gambar latar tidak ada' }
    # baca lewat memori supaya file tidak terkunci (file ini sendiri bisa ikut terupdate)
    $ms = New-Object IO.MemoryStream (, [IO.File]::ReadAllBytes($file))
    $src = [Drawing.Image]::FromStream($ms)

    $w = [int][Math]::Round($CARD_W * $script:k); $h = [int][Math]::Round($CARD_H * $script:k)
    $script:bg = New-Object Drawing.Bitmap $w, $h
    $g = [Drawing.Graphics]::FromImage($script:bg)
    $g.InterpolationMode = 'HighQualityBicubic'; $g.PixelOffsetMode = 'HighQuality'
    $g.DrawImage($src, 0, 0, $w, $h); $g.Dispose(); $src.Dispose()

    $f = New-Object Windows.Forms.Form
    $f.FormBorderStyle = 'None'; $f.StartPosition = 'CenterScreen'
    $f.ClientSize = New-Object Drawing.Size $w, $h
    $f.Text = 'Pembaruan SIMRS Khanza'; $f.TopMost = $true; $f.ShowInTaskbar = $true
    $f.BackColor = [Drawing.Color]::FromArgb(10, 18, 22)
    $ico = Join-Path $Local 'logo-rs-transparent.ico'
    if (Test-Path $ico) { try { $f.Icon = New-Object Drawing.Icon $ico } catch {} }
    $f.GetType().GetProperty('DoubleBuffered', [Reflection.BindingFlags]'NonPublic,Instance').SetValue($f, $true, $null)
    # catatan: jangan pakai nama param $s - PowerShell tidak peka huruf, $s menimpa $S (state)
    $f.Add_Paint({ param($pengirim, $ev) Gambar-Jendela $ev.Graphics })
    # jendela bisa digeser
    $f.Add_MouseDown({ param($pengirim, $ev)
            if ($ev.Button -eq 'Left') { [void][KhanzaLauncher.Win]::ReleaseCapture(); [void][KhanzaLauncher.Win]::SendMessage($pengirim.Handle, 0xA1, 2, 0) } })
    $f.Add_FormClosed({ $script:form = $null })
    $f.Show()
    # Windows 11: sudut membulat + bayangan bawaan sistem
    try { $v = 2; [void][KhanzaLauncher.Win]::DwmSetWindowAttribute($f.Handle, 33, [ref]$v, 4) } catch {}
    $script:form = $f
    [Windows.Forms.Application]::DoEvents()
}

function Font-Px([string]$nama, [double]$px, [string]$gaya = 'Regular') {
    New-Object Drawing.Font $nama, ([single]($px * $script:k)), ([Drawing.FontStyle]$gaya), ([Drawing.GraphicsUnit]::Pixel)
}

$TF = $null
function Teks($g, [string]$s, $font, $warna, [double]$x, [double]$baseline, [switch]$Kanan) {
    if (-not $script:TF) { $script:TF = [Drawing.StringFormat]::GenericTypographic; $script:TF.FormatFlags = $script:TF.FormatFlags -bor [Drawing.StringFormatFlags]::MeasureTrailingSpaces }
    $ff = $font.FontFamily
    $asc = $font.Size * $ff.GetCellAscent($font.Style) / $ff.GetEmHeight($font.Style)
    $px = $x * $script:k
    if ($Kanan) { $px -= $g.MeasureString($s, $font, 10000, $script:TF).Width }
    $br = New-Object Drawing.SolidBrush $warna
    $g.DrawString($s, $font, $br, [single]$px, [single]($baseline * $script:k - $asc), $script:TF)
    $br.Dispose()
}

function Potong($g, [string]$s, $font, [double]$maks) {
    if ($g.MeasureString($s, $font, 10000, $script:TF).Width -le $maks * $script:k) { return $s }
    while ($s.Length -gt 4 -and $g.MeasureString('...' + $s, $font, 10000, $script:TF).Width -gt $maks * $script:k) { $s = $s.Substring(1) }
    '...' + $s
}

function RR([double]$x, [double]$y, [double]$w, [double]$h, [double]$r) {
    $p = New-Object Drawing.Drawing2D.GraphicsPath
    $d = [single]([Math]::Min($r * 2, [Math]::Min($w, $h)))
    if ($d -le 0) { $p.AddRectangle((New-Object Drawing.RectangleF ([single]$x), ([single]$y), ([single]$w), ([single]$h))); return $p }
    $p.AddArc([single]$x, [single]$y, $d, $d, 180, 90)
    $p.AddArc([single]($x + $w - $d), [single]$y, $d, $d, 270, 90)
    $p.AddArc([single]($x + $w - $d), [single]($y + $h - $d), $d, $d, 0, 90)
    $p.AddArc([single]$x, [single]($y + $h - $d), $d, $d, 90, 90)
    $p.CloseFigure(); $p
}

function Gambar-Jendela($g) {
    $g.DrawImageUnscaled($script:bg, 0, 0)
    $g.SmoothingMode = 'AntiAlias'; $g.TextRenderingHint = 'ClearTypeGridFit'; $g.PixelOffsetMode = 'HighQuality'
    $k = $script:k
    $cHijau = [Drawing.Color]::FromArgb(124, 193, 66); $cPink = [Drawing.Color]::FromArgb(233, 30, 120)
    $cMerah = [Drawing.Color]::FromArgb(240, 84, 84)
    $barX = $PAD; $barW = $CARD_W - $PAD * 2
    $t = $jam.Elapsed.TotalSeconds

    # versi lama -> baru
    $f15 = Font-Px 'Segoe UI' 15
    $lama = if ($S.VersiLama) { $S.VersiLama } else { 'pemasangan baru' }
    Teks $g ("Versi $lama  " + [char]0x2192 + "  $($S.VersiBaru)") $f15 ([Drawing.Color]::FromArgb(185, 198, 205)) $TEXT_X 226
    $f15.Dispose()

    # spinner / ikon status
    $sx = $barX * $k; $sy = 256 * $k; $sd = 13 * $k
    $pen = New-Object Drawing.Pen ([Drawing.Color]::FromArgb(30, 255, 255, 255)), ([single](2.2 * $k))
    $g.DrawEllipse($pen, [single]$sx, [single]$sy, [single]$sd, [single]$sd); $pen.Dispose()
    if ($S.Fase -eq 'selesai' -or $S.Fase -eq 'gagal') {
        $c = if ($S.Fase -eq 'selesai') { $cHijau } else { $cMerah }
        $br = New-Object Drawing.SolidBrush $c; $g.FillEllipse($br, [single]$sx, [single]$sy, [single]$sd, [single]$sd); $br.Dispose()
        $pen = New-Object Drawing.Pen ([Drawing.Color]::White), ([single](1.8 * $k)); $pen.StartCap = 'Round'; $pen.EndCap = 'Round'
        if ($S.Fase -eq 'selesai') {
            $g.DrawLines($pen, [Drawing.PointF[]]@((New-Object Drawing.PointF ([single]($sx + 3 * $k)), ([single]($sy + 7 * $k))), (New-Object Drawing.PointF ([single]($sx + 6 * $k)), ([single]($sy + 10 * $k))), (New-Object Drawing.PointF ([single]($sx + 10 * $k)), ([single]($sy + 4 * $k)))))
        } else {
            $g.DrawLine($pen, [single]($sx + 4 * $k), [single]($sy + 4 * $k), [single]($sx + 9 * $k), [single]($sy + 9 * $k))
            $g.DrawLine($pen, [single]($sx + 9 * $k), [single]($sy + 4 * $k), [single]($sx + 4 * $k), [single]($sy + 9 * $k))
        }
        $pen.Dispose()
    } else {
        $rect = New-Object Drawing.RectangleF ([single]$sx), ([single]$sy), ([single]$sd), ([single]$sd)
        $lg = New-Object Drawing.Drawing2D.LinearGradientBrush $rect, $cHijau, $cPink, 45.0
        $pen = New-Object Drawing.Pen $lg, ([single](2.2 * $k)); $pen.StartCap = 'Round'; $pen.EndCap = 'Round'
        $g.DrawArc($pen, $rect, [single](($t * 400) % 360), 110); $pen.Dispose(); $lg.Dispose()
    }

    # status + persen
    $f13 = Font-Px 'Segoe UI' 13
    $f14 = Font-Px 'Segoe UI Semibold' 14
    $p = if ($S.Total -gt 0) { [Math]::Min(100, 100.0 * $S.Selesai / $S.Total) } elseif ($S.Fase -eq 'selesai') { 100 } else { 0 }
    $status = $S.Status
    if ($S.Fase -eq 'unduh' -and $S.FileNama) { $status = "Mengunduh  $($S.FileNama)" }
    $warnaStatus = if ($S.Fase -eq 'gagal') { [Drawing.Color]::FromArgb(255, 150, 150) } else { [Drawing.Color]::FromArgb(211, 221, 226) }
    $status = Potong $g $status $f13 ($barW - 90)
    Teks $g $status $f13 $warnaStatus ($barX + 23) 267
    if ($S.Fase -ne 'periksa' -and $S.Fase -ne 'gagal') { Teks $g ('{0:N0}%' -f [Math]::Floor($p)) $f14 ([Drawing.Color]::White) ($barX + $barW) 267 -Kanan }

    # bar
    $fw = [Math]::Max($BAR_H, $barW * $p / 100.0)
    if ($S.Fase -eq 'periksa') { $fw = $barW }     # fase memeriksa: bar penuh tipis bergerak (indeterminate)
    $isi = RR ($barX * $k) ($BAR_Y * $k) ($fw * $k) ($BAR_H * $k) ($BAR_H / 2 * $k)
    $rBar = New-Object Drawing.RectangleF ([single]($barX * $k)), 0, ([single]($barW * $k)), 1
    if ($S.Fase -eq 'gagal') {
        $br = New-Object Drawing.SolidBrush $cMerah
    } elseif ($S.Fase -eq 'periksa') {
        $br = New-Object Drawing.SolidBrush ([Drawing.Color]::FromArgb(40, 255, 255, 255))
    } else {
        $br = New-Object Drawing.Drawing2D.LinearGradientBrush $rBar, $cHijau, $cPink, 0.0
    }
    $g.FillPath($br, $isi); $br.Dispose()
    # kilau berjalan
    $oldClip = $g.Clip
    $g.SetClip($isi)
    $kx = ($barX - 120 + (($t * 260) % ($barW + 240))) * $k
    $rk = New-Object Drawing.RectangleF ([single]$kx), ([single]($BAR_Y * $k)), ([single](120 * $k)), ([single]($BAR_H * $k))
    $lk = New-Object Drawing.Drawing2D.LinearGradientBrush $rk, ([Drawing.Color]::FromArgb(0, 255, 255, 255)), ([Drawing.Color]::FromArgb(0, 255, 255, 255)), 0.0
    $blend = New-Object Drawing.Drawing2D.ColorBlend 3
    $blend.Colors = @([Drawing.Color]::FromArgb(0, 255, 255, 255), [Drawing.Color]::FromArgb(150, 255, 255, 255), [Drawing.Color]::FromArgb(0, 255, 255, 255))
    $blend.Positions = @([single]0, [single]0.5, [single]1)
    $lk.InterpolationColors = $blend
    $g.FillRectangle($lk, $rk); $lk.Dispose()
    $g.Clip = $oldClip; $isi.Dispose()

    # detail di bawah bar
    $f11 = Font-Px 'Segoe UI' 11
    $abu = [Drawing.Color]::FromArgb(143, 163, 173)
    if ($S.Fase -eq 'unduh' -or $S.Fase -eq 'rapikan') {
        $kiri = "$(Ukuran $S.Selesai) / $(Ukuran $S.Total)"
        if ($S.Kecepatan -gt 0) { $kiri += "   " + [char]0x00B7 + "   $(Ukuran $S.Kecepatan)/s" }
        Teks $g $kiri $f11 $abu $barX 304
        $kanan = "file $($S.FileKe) dari $($S.FileJumlah)"
        if ($S.Kecepatan -gt 0 -and $S.Selesai -lt $S.Total) { $kanan = "sisa " + (Durasi (($S.Total - $S.Selesai) / $S.Kecepatan)) + "   " + [char]0x00B7 + "   " + $kanan }
        Teks $g $kanan $f11 $abu ($barX + $barW) 304 -Kanan
    } elseif ($S.Pesan) {
        Teks $g (Potong $g $S.Pesan $f11 $barW) $f11 $abu $barX 304
    }
    $pesanBawah = if ($S.Fase -eq 'gagal') { 'Aplikasi tetap dibuka dengan versi yang ada' } elseif ($S.Fase -eq 'selesai') { 'Membuka aplikasi...' } else { 'Mohon tunggu, jangan tutup jendela ini' }
    Teks $g $pesanBawah $f11 ([Drawing.Color]::FromArgb(126, 142, 151)) $barX ($CARD_H - 20)
    $f11.Dispose(); $f13.Dispose(); $f14.Dispose()
}

# ---------------------------------------------------------------- render ke konsol + jendela
$terakhirKonsol = ''
function Segarkan([switch]$Paksa) {
    if (-not $Paksa -and $jamUI.ElapsedMilliseconds -lt 50) { return }
    $jamUI.Restart()
    if ($script:form) {
        try { $script:form.Invalidate(); [Windows.Forms.Application]::DoEvents() } catch { $script:form = $null }
    }
    $script:spinKe++
    if ($S.Fase -eq 'unduh') {
        $p = if ($S.Total -gt 0) { [Math]::Min(1.0, $S.Selesai / $S.Total) } else { 1.0 }
        $lebar = 28; $isi = [int][Math]::Round($p * $lebar)
        $bar = (Gradasi ($BLOK * $isi)) + $ABU + ($KOSONG * ($lebar - $isi)) + $R
        $sp = $SPIN[$script:spinKe % $SPIN.Count]
        $info = "{0,4:N0}%  {1} / {2}" -f ($p * 100), (Ukuran $S.Selesai), (Ukuran $S.Total)
        if ($S.Kecepatan -gt 0) { $info += "  $(Ukuran $S.Kecepatan)/s" }
        if ($S.Kecepatan -gt 0 -and $S.Selesai -lt $S.Total) { $info += "  sisa $(Durasi (($S.Total - $S.Selesai) / $S.Kecepatan))" }
        $baris = "  $PINK$sp$R $bar $PUTIH$info$R"
        if ($baris -ne $script:terakhirKonsol) {
            [Console]::Out.Write("`r" + $baris + $HAPUS)
            $script:terakhirKonsol = $baris
        }
    }
}

function Tangkap-Jendela([string]$ke) {
    if (-not $script:form) { return }
    $bmp = New-Object Drawing.Bitmap $script:form.ClientSize.Width, $script:form.ClientSize.Height
    $script:form.DrawToBitmap($bmp, (New-Object Drawing.Rectangle 0, 0, $bmp.Width, $bmp.Height))
    $bmp.Save($ke, [Drawing.Imaging.ImageFormat]::Png); $bmp.Dispose()
}

function Akhiri-Baris { if ($script:terakhirKonsol) { [Console]::Out.WriteLine(''); $script:terakhirKonsol = '' } }

# ---------------------------------------------------------------- daftar file
function Cocok-Pola([string]$nama, [string[]]$pola) {
    foreach ($p in $pola) { if ($nama -like $p) { return $true } }
    $false
}

function Kumpulkan([IO.DirectoryInfo]$dir, [string]$rel, [Collections.Generic.List[object]]$hasil) {
    foreach ($f in $dir.GetFiles()) {
        if (Cocok-Pola $f.Name $EX_FILE) { continue }
        $hasil.Add([pscustomobject]@{ Rel = $(if ($rel) { $rel + '\' + $f.Name } else { $f.Name }); Ukuran = $f.Length; Waktu = $f.LastWriteTimeUtc })
    }
    foreach ($d in $dir.GetDirectories()) {
        if ($EX_DIR -contains $d.Name.ToLowerInvariant()) { continue }
        Segarkan
        Kumpulkan $d $(if ($rel) { $rel + '\' + $d.Name } else { $d.Name }) $hasil
    }
}

function Salin-Dengan-Progress([string]$src, [string]$dst, [datetime]$waktu) {
    $d = Split-Path $dst -Parent
    if (-not (Test-Path -LiteralPath $d)) { [void](New-Item -ItemType Directory -Path $d -Force) }
    $tmp = "$dst.__upd"
    $buf = New-Object byte[] (1MB)
    $in = [IO.File]::Open($src, 'Open', 'Read', 'Read')
    try {
        $out = [IO.File]::Create($tmp)
        try {
            while (($n = $in.Read($buf, 0, $buf.Length)) -gt 0) {
                $out.Write($buf, 0, $n)
                $S.Selesai += $n
                $det = $jam.Elapsed.TotalSeconds - $script:mulaiUnduh
                if ($det -gt 0.5) { $S.Kecepatan = ($S.Selesai - $script:selesaiAwal) / $det }
                Segarkan
                if ($Tangkap -and -not $script:sudahTangkapTengah -and $S.Selesai -ge $S.Total / 2) {
                    $script:sudahTangkapTengah = $true; Tangkap-Jendela ($Tangkap -replace '\.png$', '-tengah.png')
                }
            }
        } finally { $out.Dispose() }
    } finally { $in.Dispose() }
    try {
        if (Test-Path -LiteralPath $dst) {
            [IO.File]::SetAttributes($dst, 'Normal')
            [IO.File]::Replace($tmp, $dst, [NullString]::Value)   # $null biasa diubah PS jadi "" -> error
        } else {
            [IO.File]::Move($tmp, $dst)
        }
    } catch {
        try { [IO.File]::Delete($tmp) } catch {}
        $inti = $_.Exception; while ($inti.InnerException) { $inti = $inti.InnerException }
        if ($inti -is [IO.IOException] -and (($inti.HResult -band 0xFFFF) -in 32, 33)) {
            throw "File $(Split-Path $dst -Leaf) sedang dipakai. Tutup SIMRS Khanza yang masih terbuka di PC ini, lalu buka ulang."
        }
        throw $inti.Message
    }
    [IO.File]::SetLastWriteTimeUtc($dst, $waktu)
}

function Jalankan-Robocopy {
    $argumen = @("`"$Server`"", "`"$Local`"", '/MIR', '/XD') + $EX_DIR + @('/XF') + $EX_FILE + @('/R:2', '/W:5', '/NJH', '/NJS', '/NDL', '/NFL', '/NP')
    $log = Join-Path $env:TEMP 'khanza-launcher-robocopy.log'
    $p = Start-Process -FilePath robocopy.exe -ArgumentList $argumen -NoNewWindow -PassThru -RedirectStandardOutput $log
    while (-not $p.WaitForExit(50)) { Segarkan }
    $p.WaitForExit()
    $p.ExitCode
}

# ---------------------------------------------------------------- jalankan aplikasi
function Atur-Shortcut {
    # Shortcut desktop -> "Run: Minimized" supaya terminal .bat tidak berkedip saat dibuka.
    try {
        $target = Join-Path $Local 'jalankan-client.bat'
        $shell = New-Object -ComObject WScript.Shell
        foreach ($dir in @([Environment]::GetFolderPath('Desktop'), [Environment]::GetFolderPath('CommonDesktopDirectory'))) {
            if (-not $dir -or -not (Test-Path -LiteralPath $dir)) { continue }
            foreach ($f in Get-ChildItem -LiteralPath $dir -Filter *.lnk -File -ErrorAction SilentlyContinue) {
                try {
                    $lnk = $shell.CreateShortcut($f.FullName)
                    if ($lnk.TargetPath -ieq $target -and $lnk.WindowStyle -ne 7) { $lnk.WindowStyle = 7; $lnk.Save() }
                } catch {}   # shortcut Public Desktop butuh admin -> lewati
            }
        }
        [void][Runtime.InteropServices.Marshal]::ReleaseComObject($shell)
    } catch {}
}

function Jalankan-Aplikasi {
    if (-not $Perintah) { exit 0 }    # launcher v2: .bat yang menjalankan java
    try {
        if ($Perintah -notmatch '^\s*(?:"([^"]+)"|(\S+))\s+(.+)$') { throw "baris java tidak dikenali: $Perintah" }
        $exe = if ($Matches[1]) { $Matches[1] } else { $Matches[2] }
        $argumen = $Matches[3]
        # java berjalan di konsol klasik tersembunyi, bukan Windows Terminal -> pakai simbol aman
        Remove-Item Env:WT_SESSION, Env:WT_PROFILE_ID -ErrorAction SilentlyContinue
        $env:KHANZA_LAUNCHER = '3'
        $proses = Start-Process -FilePath $exe -ArgumentList $argumen -WorkingDirectory $Local -WindowStyle Hidden -PassThru
        # java yang langsung mati (jar rusak, java tidak ada) jangan sampai tak terlihat:
        # serahkan ke .bat supaya dijalankan ulang dengan konsol terlihat
        if ($proses.WaitForExit(1200) -and $proses.ExitCode -ne 0) { throw "java berhenti dengan kode $($proses.ExitCode)" }
    } catch {
        Tulis "  $KUNING!$R ${ABU}Gagal menjalankan aplikasi tersembunyi ($($_.Exception.Message)), dijalankan biasa.$R"
        # munculkan lagi terminal .bat supaya error java berikutnya terbaca
        try { $h = [KhanzaLauncher.Konsol]::GetConsoleWindow(); if ($h -ne [IntPtr]::Zero) { [void][KhanzaLauncher.Konsol]::ShowWindow($h, 5) } } catch {}
        exit 1
    }
    Atur-Shortcut
    exit 0
}

function Sembunyikan-Terminal {
    # Mode v3: terminal .bat disembunyikan sejak awal; yang tampil hanya jendela update
    # (bila ada) lalu splash. Log tetap ada: Khanza memunculkan konsolnya setelah terbuka.
    if (-not $Perintah -or $Tangkap) { return }
    try {
        Add-Type -Namespace KhanzaLauncher -Name Konsol -MemberDefinition @'
[DllImport("kernel32.dll")] public static extern IntPtr GetConsoleWindow();
[DllImport("user32.dll")] public static extern bool ShowWindow(IntPtr h, int cmd);
[DllImport("user32.dll", CharSet = CharSet.Unicode)] public static extern int GetClassName(IntPtr h, System.Text.StringBuilder s, int n);
'@
        $h = [KhanzaLauncher.Konsol]::GetConsoleWindow()
        if ($h -eq [IntPtr]::Zero) { return }
        $kelas = New-Object Text.StringBuilder 64
        [void][KhanzaLauncher.Konsol]::GetClassName($h, $kelas, 64)
        # Konsol klasik bisa disembunyikan penuh (SW_HIDE). Windows Terminal (PseudoConsoleWindow)
        # mengabaikan SW_HIDE tapi mendukung minimize (SW_MINIMIZE).
        $mode = if ($kelas.ToString() -eq 'PseudoConsoleWindow') { 6 } else { 0 }
        [void][KhanzaLauncher.Konsol]::ShowWindow($h, $mode)
    } catch {}
}

# =====================================================================
#  Alur utama
# =====================================================================
Sembunyikan-Terminal
try { $Host.UI.RawUI.WindowTitle = 'SIMRS Khanza' } catch {}
Tulis ''
Tulis ("  " + (Gradasi 'SIMRS Khanza') + "$ABU  " + [char]0x00B7 + "  launcher$R")

if (-not (Test-Path -LiteralPath (Join-Path $Server 'version.txt'))) {
    Tulis "  $KUNING!$R ${ABU}Sumber update tidak terjangkau, memakai versi lokal.$R"
    Jalankan-Aplikasi
}

$S.VersiBaru = ([IO.File]::ReadAllLines((Join-Path $Server 'version.txt')) | Select-Object -First 1).Trim()
$fileLokal = Join-Path $Local 'version.txt'
if (Test-Path -LiteralPath $fileLokal) { $S.VersiLama = ([IO.File]::ReadAllLines($fileLokal) | Select-Object -First 1); if ($S.VersiLama) { $S.VersiLama = $S.VersiLama.Trim() } }

if ($S.VersiBaru -eq $S.VersiLama) {
    Tulis "  $HIJAU$CENTANG$R ${PUTIH}Versi terbaru$R $ABU($($S.VersiLama))$R"
    Jalankan-Aplikasi
}

Tulis ''
Tulis ("  " + $TEBAL + $PUTIH + "Pembaruan tersedia" + $R + "   $ABU$(if ($S.VersiLama) { $S.VersiLama } else { '(baru)' })$R  $PINK" + [char]0x2192 + "$R  $HIJAU$TEBAL$($S.VersiBaru)$R")

if (-not $TanpaJendela) {
    try { Buat-Jendela } catch { Tulis "  ${ABU}(jendela grafis tidak tersedia: $($_.Exception.Message))$R"; $form = $null }
}

$hasil = 'gagal'
try {
    # 1. cari file yang berubah
    $S.Fase = 'periksa'; $S.Status = 'Memeriksa file yang berubah'
    Segarkan -Paksa
    $semua = New-Object 'Collections.Generic.List[object]'
    Kumpulkan (New-Object IO.DirectoryInfo $Server) '' $semua
    $perlu = New-Object 'Collections.Generic.List[object]'
    foreach ($f in $semua) {
        $l = New-Object IO.FileInfo (Join-Path $Local $f.Rel)
        if (-not $l.Exists -or $l.Length -ne $f.Ukuran -or [Math]::Abs(($l.LastWriteTimeUtc - $f.Waktu).TotalSeconds) -gt 2) { $perlu.Add($f) }
    }
    # file besar (jar utama) dulu supaya persen terasa jujur sejak awal
    $perlu = @($perlu | Sort-Object -Property Ukuran -Descending)
    $S.FileJumlah = $perlu.Count
    $S.Total = [double](($perlu | Measure-Object -Property Ukuran -Sum).Sum)
    Tulis "  ${ABU}$($perlu.Count) file berubah, total $(Ukuran $S.Total)$R"

    # 2. unduh dengan progress
    $S.Fase = 'unduh'; $S.Status = 'Mengunduh pembaruan'
    $script:mulaiUnduh = $jam.Elapsed.TotalSeconds; $script:selesaiAwal = 0
    $i = 0
    foreach ($f in $perlu) {
        $i++; $S.FileKe = $i; $S.FileNama = $f.Rel
        Segarkan -Paksa
        Salin-Dengan-Progress (Join-Path $Server $f.Rel) (Join-Path $Local $f.Rel) $f.Waktu
    }
    Segarkan -Paksa
    Akhiri-Baris

    # 3. rapikan: hapus file yang sudah tidak ada di server + jaring pengaman (sama persis launcher lama)
    $S.Fase = 'rapikan'; $S.Status = 'Merapikan file lama'; $S.FileNama = ''
    Segarkan -Paksa
    $rc = Jalankan-Robocopy
    if ($rc -ge 8) { throw "robocopy gagal (kode $rc)" }

    # 4. tandai versi PALING AKHIR
    Copy-Item -LiteralPath (Join-Path $Server 'version.txt') -Destination $fileLokal -Force
    $S.Fase = 'selesai'; $S.Status = 'Pembaruan selesai'
    if ($S.Total -le 0) { $S.Total = 1; $S.Selesai = 1 }
    $hasil = 'ok'
    $dtk = $jam.Elapsed.TotalSeconds
    Tulis "  $HIJAU$CENTANG$R ${PUTIH}Pembaruan selesai$R ${ABU}ke versi $($S.VersiBaru) dalam $(Durasi $dtk)$R"
} catch {
    Akhiri-Baris
    $S.Fase = 'gagal'; $S.Status = 'Pembaruan gagal'
    $inti = $_.Exception; while ($inti.InnerException) { $inti = $inti.InnerException }
    $S.Pesan = $inti.Message
    Tulis "  $MERAH$SILANG Pembaruan gagal:$R $($inti.Message)"
    Tulis "  ${ABU}Aplikasi tetap dibuka dengan versi lokal yang ada.$R"
}

# tahan sebentar supaya user sempat melihat hasilnya
$tahan = if ($hasil -eq 'ok') { 900 } else { 3500 }
$akhir = [Diagnostics.Stopwatch]::StartNew()
while ($akhir.ElapsedMilliseconds -lt $tahan -and $form) {
    Segarkan -Paksa
    Start-Sleep -Milliseconds 30
}
if ($Tangkap) { Tangkap-Jendela $Tangkap }
if ($form) { try { $form.Close(); $form.Dispose() } catch {} }
Tulis ''
Jalankan-Aplikasi
