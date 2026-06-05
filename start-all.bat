@echo off
chcp 65001 >nul
echo ========================================
echo    MDM Management System
echo ========================================
echo.

REM Set Maven path
set "MAVEN_HOME=D:\maven\mvn"
set "PATH=%MAVEN_HOME%\bin;%PATH%"

echo [1/3] Check database connection...
mysql -uroot -padmin -e "use mdm;" 2>nul
if errorlevel 1 (
    echo [Error] Database connection failed!
    echo Please ensure:
    echo   1. MySQL service is running
    echo   2. mdm database exists
    pause
    exit /b 1
)
echo [OK] Database connected

echo.
echo [2/3] Start backend service...
cd /d "%~dp0backend"
start "MDM-Backend" cmd /c "set MAVEN_HOME=D:\maven\mvn && set PATH=D:\maven\mvn\bin;%PATH% && mvn spring-boot:run"

echo Waiting for backend to start...
ping -n 8 127.0.0.1 >nul 2>&1

echo.
echo [3/3] Start frontend service...
cd /d "%~dp0frontend"
start "MDM-Frontend" cmd /c "npm run dev"

echo.
echo ========================================
echo    All Services Started!
echo ========================================
echo.
echo Backend:  http://localhost:3000
echo Frontend: http://localhost:9000
echo.
echo Account: admin / 123456
echo.
echo Press any key to open browser...
pause >nul

start "" http://localhost:9000

exit
