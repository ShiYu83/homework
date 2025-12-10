@echo off
chcp 65001 >nul
echo ========================================
echo    医院挂号系统 - 端口状态检查
echo ========================================
echo.

powershell -ExecutionPolicy Bypass -File "%~dp0test-ports.ps1"

pause

