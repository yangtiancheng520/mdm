@echo off
chcp 65001 >nul
echo ========================================
echo    一键启动管理系统
echo ========================================
echo.

echo [1/3] 检查数据库连接...
mysql -uroot -padmin -e "use vue_admin;" 2>nul
if errorlevel 1 (
    echo [错误] 数据库连接失败！
    echo 请确保：
    echo   1. MySQL 服务已启动
    echo   2. vue_admin 数据库已创建
    pause
    exit /b 1
)
echo [✓] 数据库连接正常

echo.
echo [2/3] 启动后端服务...
cd /d "%~dp0backend"
start "MDM-Backend" cmd /k "mvnd spring-boot:run"
timeout /t 5 /nobreak >nul

echo.
echo [3/3] 启动前端服务...
cd /d "%~dp0frontend"
start "MDM-Frontend" cmd /k "npm run dev"

echo.
echo ========================================
echo    所有服务启动完成！
echo ========================================
echo.
echo 后端地址: http://localhost:3000
echo 前端地址: http://localhost:9000
echo.
echo 测试账号: admin / 123456
echo.
echo 按任意键打开浏览器...
pause >nul

start http://localhost:9000

exit
