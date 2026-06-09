@echo off
echo ========================================
echo SAP JCo 安装脚本
echo ========================================
echo.
echo 请确保你已经下载了 sapjco3.jar 文件
echo 并将其放在当前目录下
echo.
pause

if not exist "sapjco3.jar" (
    echo 错误: 找不到 sapjco3.jar 文件
    pause
    exit /b 1
)

echo 正在安装 SAP JCo 到本地 Maven 仓库...
call mvn install:install-file ^
  -Dfile=sapjco3.jar ^
  -DgroupId=com.sap ^
  -DartifactId=sapjco3 ^
  -Dversion=3.1.2 ^
  -Dpackaging=jar

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo 安装成功！
    echo ========================================
) else (
    echo.
    echo 安装失败，请检查错误信息
)

pause
