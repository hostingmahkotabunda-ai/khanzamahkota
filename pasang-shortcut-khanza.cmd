@echo off
setlocal

set "APPDIR=%~dp0"
set "LAUNCHER=%APPDIR%jalankan-client.bat"
set "ICON=%APPDIR%logo-rs-transparent.ico"

if not exist "%LAUNCHER%" (
    echo [GAGAL] jalankan-client.bat tidak ditemukan di folder ini.
    pause
    exit /b 1
)

if not exist "%ICON%" (
    echo [GAGAL] logo-rs-transparent.ico tidak ditemukan di folder ini.
    pause
    exit /b 1
)

powershell.exe -NoProfile -ExecutionPolicy Bypass -Command ^
  "$desktop=[Environment]::GetFolderPath('Desktop');" ^
  "$shortcut=Join-Path $desktop 'Khanza RS.lnk';" ^
  "$shell=New-Object -ComObject WScript.Shell;" ^
  "$link=$shell.CreateShortcut($shortcut);" ^
  "$link.TargetPath='%LAUNCHER%';" ^
  "$link.WorkingDirectory='%APPDIR%';" ^
  "$link.IconLocation='%ICON%,0';" ^
  "$link.Description='Jalankan SIMRS Khanza';" ^
  "$link.Save();" ^
  "[void][Runtime.InteropServices.Marshal]::ReleaseComObject($shell)"

if errorlevel 1 (
    echo [GAGAL] Shortcut tidak berhasil dibuat.
    pause
    exit /b 1
)

echo [BERHASIL] Shortcut Khanza RS sudah dibuat di Desktop.
pause
