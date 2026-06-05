@echo off
chcp 65001 >nul
echo ========================================
echo    启动前端服务 - Vue 3
echo ========================================
echo.

cd /d "%~dp0frontend"

echo [1/2] 检查依赖是否安装...
if not exist "node_modules" (
    echo [!] node_modules 不存在，正在安装依赖...
    npm install
)

echo [✓] 依赖已安装

echo.
echo [2/2] 启动前端服务...
echo 服务地址: http://localhost:9000
echo.

npm run dev

pause