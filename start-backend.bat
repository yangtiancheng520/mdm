@echo off
chcp 65001 >nul
echo ========================================
echo    Start Backend - Spring Boot
echo ========================================
echo.

cd /d "%~dp0backend"

REM Set Maven path
set MAVEN_HOME=D:\maven\mvn
set PATH=%MAVEN_HOME%\bin;%PATH%

echo [1/2] Check database connection...
mysql -uroot -padmin -e "use mdm;" 2>nul
if errorlevel 1 (
    echo [Error] Database connection failed. Please ensure MySQL is running and mdm database exists.
    pause
    exit /b 1
)
echo [OK] Database connected

echo.
echo [2/2] Start backend service...
echo Service URL: http://localhost:3000
echo.

mvn spring-boot:run

pause
