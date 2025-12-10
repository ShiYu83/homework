@echo off
chcp 65001 >nul
echo 正在检查端口9090...

REM 检查端口是否被占用
netstat -ano | findstr :9090 >nul
if %errorlevel% == 0 (
    echo 警告: 端口9090已被占用！
    echo 正在查找占用端口的进程...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :9090') do (
        echo 发现进程ID: %%a
        echo 是否要终止该进程? (Y/N)
        set /p choice=
        if /i "!choice!"=="Y" (
            taskkill /F /PID %%a
            echo 进程已终止
        ) else (
            echo 请手动终止占用端口的进程，或修改application.yml中的端口配置
            pause
            exit /b 1
        )
    )
)

echo 正在启动后端服务...
cd /d %~dp0
call mvn spring-boot:run

pause

