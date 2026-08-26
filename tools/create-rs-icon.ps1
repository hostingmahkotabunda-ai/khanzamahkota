$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -ReferencedAssemblies System.Drawing @'
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;

public static class LogoBackgroundRemoval {
    public static Bitmap Extract(Bitmap source) {
        int width = source.Width, height = source.Height;
        int total = width * height;
        int[] distance = new int[total];
        Queue<int> queue = new Queue<int>();
        for (int i = 0; i < total; i++) distance[i] = Int32.MaxValue;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color c = source.GetPixel(x, y);
                int high = Math.Max(c.R, Math.Max(c.G, c.B));
                int low = Math.Min(c.R, Math.Min(c.G, c.B));
                if (low < 238 || high - low > 14) {
                    int index = y * width + x;
                    distance[index] = 0;
                    queue.Enqueue(index);
                }
            }
        }

        while (queue.Count > 0) {
            int index = queue.Dequeue();
            int nextDistance = distance[index] + 1;
            if (nextDistance > 24) continue;
            int x = index % width, y = index / width;
            if (x > 0) Visit(index - 1, nextDistance, distance, queue);
            if (x + 1 < width) Visit(index + 1, nextDistance, distance, queue);
            if (y > 0) Visit(index - width, nextDistance, distance, queue);
            if (y + 1 < height) Visit(index + width, nextDistance, distance, queue);
        }

        Bitmap result = new Bitmap(width, height, PixelFormat.Format32bppArgb);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int d = distance[y * width + x];
                Color c = source.GetPixel(x, y);
                int alpha = d <= 18 ? 255 : (d <= 24 ? (24 - d) * 42 : 0);
                result.SetPixel(x, y, Color.FromArgb(alpha, c.R, c.G, c.B));
            }
        }
        return result;
    }

    private static void Visit(int index, int candidate, int[] distance, Queue<int> queue) {
        if (candidate < distance[index]) {
            distance[index] = candidate;
            queue.Enqueue(index);
        }
    }
}
'@

$source = 'C:\Users\User\Downloads\logo rs.png'
$iconPath = 'C:\Users\User\Videos\KHANZA-DEV\logo-rs-transparent.ico'
$shortcutPath = 'C:\Users\User\Desktop\Khanza RS.lnk'
$sizes = @(16, 24, 32, 48, 64, 128, 256)

$src = [System.Drawing.Bitmap]::new($source)
try {
    $cutout = [LogoBackgroundRemoval]::Extract($src)
    # Detect the coloured logo while ignoring the near-white background.
    $minX = $src.Width; $minY = $src.Height; $maxX = -1; $maxY = -1
    for ($y = 0; $y -lt $src.Height; $y += 2) {
        for ($x = 0; $x -lt $src.Width; $x += 2) {
            $c = $src.GetPixel($x, $y)
            $high = [Math]::Max($c.R, [Math]::Max($c.G, $c.B))
            $low = [Math]::Min($c.R, [Math]::Min($c.G, $c.B))
            if ($low -lt 238 -or ($high - $low) -gt 14) {
                $minX = [Math]::Min($minX, $x); $maxX = [Math]::Max($maxX, $x)
                $minY = [Math]::Min($minY, $y); $maxY = [Math]::Max($maxY, $y)
            }
        }
    }
    if ($maxX -lt 0) { throw 'Isi logo tidak terdeteksi.' }

    $contentW = $maxX - $minX + 1
    $contentH = $maxY - $minY + 1
    $pad = [Math]::Ceiling([Math]::Max($contentW, $contentH) * 0.02)
    $left = [Math]::Max(0, $minX - $pad)
    $top = [Math]::Max(0, $minY - $pad)
    $right = [Math]::Min($src.Width - 1, $maxX + $pad)
    $bottom = [Math]::Min($src.Height - 1, $maxY + $pad)
    $cropW = $right - $left + 1
    $cropH = $bottom - $top + 1
    $sourceRect = [System.Drawing.Rectangle]::new($left, $top, $cropW, $cropH)

    $frames = [System.Collections.Generic.List[byte[]]]::new()
    foreach ($size in $sizes) {
        $bitmap = [System.Drawing.Bitmap]::new($size, $size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
        try {
            $graphics.Clear([System.Drawing.Color]::Transparent)
            $graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceCopy
            $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
            $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
            $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
            $scale = [Math]::Min($size / $cropW, $size / $cropH)
            $drawW = [Math]::Max(1, [Math]::Round($cropW * $scale))
            $drawH = [Math]::Max(1, [Math]::Round($cropH * $scale))
            $drawX = [Math]::Floor(($size - $drawW) / 2)
            $drawY = [Math]::Floor(($size - $drawH) / 2)
            $destRect = [System.Drawing.Rectangle]::new($drawX, $drawY, $drawW, $drawH)
            $graphics.DrawImage($cutout, $destRect, $sourceRect, [System.Drawing.GraphicsUnit]::Pixel)
            $memory = [System.IO.MemoryStream]::new()
            try {
                $bitmap.Save($memory, [System.Drawing.Imaging.ImageFormat]::Png)
                $frames.Add($memory.ToArray())
            } finally { $memory.Dispose() }
        } finally { $graphics.Dispose(); $bitmap.Dispose() }
    }
} finally { if ($null -ne $cutout) { $cutout.Dispose() }; $src.Dispose() }

# ICO header + directory entries + PNG image frames.
$stream = [System.IO.File]::Open($iconPath, [System.IO.FileMode]::Create)
$writer = [System.IO.BinaryWriter]::new($stream)
try {
    $writer.Write([uint16]0); $writer.Write([uint16]1); $writer.Write([uint16]$sizes.Count)
    $offset = 6 + (16 * $sizes.Count)
    for ($i = 0; $i -lt $sizes.Count; $i++) {
        $dimension = if ($sizes[$i] -eq 256) { 0 } else { $sizes[$i] }
        $writer.Write([byte]$dimension); $writer.Write([byte]$dimension)
        $writer.Write([byte]0); $writer.Write([byte]0)
        $writer.Write([uint16]1); $writer.Write([uint16]32)
        $writer.Write([uint32]$frames[$i].Length); $writer.Write([uint32]$offset)
        $offset += $frames[$i].Length
    }
    foreach ($frame in $frames) { $writer.Write($frame) }
} finally { $writer.Dispose(); $stream.Dispose() }

$shell = New-Object -ComObject WScript.Shell
try {
    $shortcut = $shell.CreateShortcut($shortcutPath)
    $shortcut.IconLocation = "$iconPath,0"
    $shortcut.Save()
} finally { [void][Runtime.InteropServices.Marshal]::ReleaseComObject($shell) }

Get-Item -LiteralPath $iconPath, $shortcutPath | Select-Object FullName, Length, LastWriteTime
