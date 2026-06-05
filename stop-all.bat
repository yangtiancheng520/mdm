@echo off
chcp 65001 >nul
echo ========================================
echo    停止所有服务
echo ========================================
echo.

echo [1/2] 停止后端服务 (端口 3000)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :3000 ^| findstr LISTENING') do (
    taskkill /F /PID %%a 2>nul
    if not errorlevel 1 (
        echo [✓] 已停止后端服务 ^(PID: %%a^)
    )
)

echo.
echo [2/2] 停止前端服务 (端口 9000)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :9000 ^| findstr LISTENING') do (
    taskkill /F /PID %%a 2>nul
    if not errorlevel 1 (
        echo [✓] 已停止前端服务 ^(PID: %%a^)
    )
)

echo.
echo ========================================
echo    所有服务已停止
echo ========================================
pause
