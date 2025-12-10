@echo off
chcp 65001 >nul
echo ========================================
echo 重启后端服务
echo ========================================
echo.

echo 步骤1: 停止现有服务...
echo 查找占用9090端口的进程...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :9090 ^| findstr LISTENING') do (
    echo 找到进程ID: %%a
    echo 正在停止进程...
    taskkill /F /PID %%a >nul 2>&1
    if errorlevel 1 (
        echo 停止进程失败，可能需要管理员权限
    ) else (
        echo 进程已停止
    )
)

echo.
echo 等待3秒让端口释放...
timeout /t 3 /nobreak >nul

echo.
echo 步骤2: 启动后端服务...
cd /d "%~dp0hospital-backend"
if exist "pom.xml" (
    echo 正在启动Spring Boot应用...
    start "Hospital Backend" cmd /k "mvn spring-boot:run"
    echo.
    echo ========================================
    echo 后端服务正在启动中...
    echo ========================================
    echo.
    echo 请等待服务启动完成（约10-20秒）
    echo 启动后可以访问: http://localhost:9090/api/health
    echo.
) else (
    echo 错误: 找不到pom.xml文件
    echo 请确保在正确的目录下运行此脚本
)

pause





