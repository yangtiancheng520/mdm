@echo off
chcp 65001 >nul
echo ========================================
echo    Start Frontend - Vue 3
echo ========================================
echo.

cd /d "%~dp0frontend"

echo [1/2] Check dependencies...
if not exist "node_modules" (
    echo [!] node_modules not found, installing dependencies...
    npm install
)

echo [OK] Dependencies ready

echo.
echo [2/2] Start frontend service...
echo Service URL: http://localhost:9000
echo.

npm run dev

pause
