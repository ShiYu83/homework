@echo off
chcp 65001 >nul
echo ========================================
echo    医院挂号系统 - 启动所有服务
echo ========================================
echo.

REM 检查 PowerShell 执行策略并运行脚本
if exist "%~dp0start-all.ps1" (
    powershell.exe -ExecutionPolicy Bypass -File "%~dp0start-all.ps1"
) else (
    echo 错误: 找不到启动脚本 start-all.ps1
    echo 请确保该文件存在于项目根目录
    pause
    exit /b 1
)

pause

