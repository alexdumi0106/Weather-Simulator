@echo off
echo ====== Killing old Android processes ======
taskkill /F /IM adb.exe >nul 2>&1
taskkill /F /IM emulator.exe >nul 2>&1
taskkill /F /IM qemu-system-x86_64.exe >nul 2>&1

echo ====== Restarting ADB ======
cd /d "D:\AndroidStudioApp\Sdk\platform-tools"
adb kill-server
adb start-server

echo ====== Starting Emulator ======
cd /d "D:\AndroidStudioApp\Sdk\emulator"
emulator.exe -avd Medium_Phone_API_34 -wipe-data

pause
