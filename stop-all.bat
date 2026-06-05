@echo off
chcp 65001 >nul
echo ========================================
echo    Stop All Services
echo ========================================
echo.

echo [1/2] Stop Backend (Port 3000)...
for /f "tokens=5 delims= " %%a in ('netstat -ano 2^>nul ^| findstr ":3000.*LISTENING"') do (
    taskkill /F /PID %%a >nul 2>&1
    if not errorlevel 1 echo [OK] Backend stopped ^(PID: %%a^)
)

echo.
echo [2/2] Stop Frontend (Port 9000)...
for /f "tokens=5 delims= " %%a in ('netstat -ano 2^>nul ^| findstr ":9000.*LISTENING"') do (
    taskkill /F /PID %%a >nul 2>&1
    if not errorlevel 1 echo [OK] Frontend stopped ^(PID: %%a^)
)

echo.
echo ========================================
echo    All Services Stopped
echo ========================================
echo.
pause
