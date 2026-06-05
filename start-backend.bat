@echo off
chcp 65001 >nul
echo ========================================
echo    启动后端服务 - Spring Boot
echo ========================================
echo.

cd /d "%~dp0backend"

echo [1/2] 检查数据库连接...
mysql -uroot -padmin -e "use vue_admin;" 2>nul
if errorlevel 1 (
    echo [错误] 数据库连接失败，请确保 MySQL 已启动且 vue_admin 数据库存在
    pause
    exit /b 1
)
echo [✓] 数据库连接正常

echo.
echo [2/2] 启动后端服务...
echo 服务地址: http://localhost:3000
echo.

mvnd spring-boot:run

pause
