@echo off
chcp 65001 >nul
echo ========================================
echo 停止后端服务
echo ========================================
echo.

echo 查找占用9090端口的进程...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :9090 ^| findstr LISTENING') do (
    set PID=%%a
    echo 找到进程ID: %%a
    echo 正在停止进程...
    taskkill /F /PID %%a
    echo 进程已停止
)

echo.
echo ========================================
echo 完成！
echo ========================================
echo.
echo 现在可以重新启动后端服务了：
echo   cd hospital-backend
echo   mvn spring-boot:run
echo.
pause





