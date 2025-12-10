# 启动医院挂号系统 - PowerShell脚本
# 用于启动所有服务（Docker、后端、前端）

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   医院挂号系统 - 启动所有服务" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查Docker是否运行
Write-Host "步骤1: 检查Docker服务..." -ForegroundColor Yellow
try {
    docker info | Out-Null
    Write-Host "Docker服务正常运行" -ForegroundColor Green
} catch {
    Write-Host "Docker未运行，请先启动Docker Desktop" -ForegroundColor Red
    Read-Host "按Enter键退出"
    exit 1
}

# 启动Docker服务
Write-Host ""
Write-Host "步骤2: 启动Docker基础设施服务..." -ForegroundColor Yellow
Write-Host "  启动MySQL、Redis、Elasticsearch、RabbitMQ..." -ForegroundColor Gray

docker-compose up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker服务启动失败" -ForegroundColor Red
    Read-Host "按Enter键退出"
    exit 1
}

Write-Host "Docker服务启动成功" -ForegroundColor Green
Write-Host "  等待服务初始化（15秒）..." -ForegroundColor Gray
Start-Sleep -Seconds 15

# 检查服务状态
Write-Host ""
Write-Host "步骤3: 检查服务状态..." -ForegroundColor Yellow
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# 启动后端服务
Write-Host ""
Write-Host "步骤4: 启动后端服务..." -ForegroundColor Yellow
$backendPath = Join-Path $PSScriptRoot "hospital-backend"

if (Test-Path $backendPath) {
    Write-Host "  后端路径: $backendPath" -ForegroundColor Gray
    
    # 检查Maven是否可用
    $mavenAvailable = $false
    try {
        $null = mvn --version 2>&1
        $mavenAvailable = $true
    } catch {
        Write-Host "  警告: Maven未找到，将尝试使用IDE运行" -ForegroundColor Yellow
    }
    
    if ($mavenAvailable) {
        Write-Host "  使用Maven启动后端服务..." -ForegroundColor Gray
        $backendCmd = "cd '$backendPath'; mvn spring-boot:run"
        Start-Process powershell -ArgumentList "-NoExit", "-Command", $backendCmd -WindowStyle Normal
        Write-Host "后端服务启动命令已执行" -ForegroundColor Green
    } else {
        Write-Host "  请手动在IDE中启动后端服务" -ForegroundColor Yellow
        Write-Host "  或安装Maven后重新运行此脚本" -ForegroundColor Yellow
    }
} else {
    Write-Host "找不到后端目录: $backendPath" -ForegroundColor Red
}

# 启动前端服务
Write-Host ""
Write-Host "步骤5: 启动前端服务..." -ForegroundColor Yellow
$frontendPath = Join-Path $PSScriptRoot "hospital-frontend"

if (Test-Path $frontendPath) {
    Write-Host "  前端路径: $frontendPath" -ForegroundColor Gray
    
    # 检查Node.js是否可用
    $nodeAvailable = $false
    try {
        $null = node --version 2>&1
        $nodeAvailable = $true
    } catch {
        Write-Host "Node.js未找到，请先安装Node.js" -ForegroundColor Red
    }
    
    if ($nodeAvailable) {
        # 检查node_modules是否存在
        $nodeModulesPath = Join-Path $frontendPath "node_modules"
        if (-not (Test-Path $nodeModulesPath)) {
            Write-Host "  首次运行，正在安装依赖..." -ForegroundColor Yellow
            Push-Location $frontendPath
            npm install
            Pop-Location
        }
        
        Write-Host "  启动前端开发服务器..." -ForegroundColor Gray
        $frontendCmd = "cd '$frontendPath'; npm run dev"
        Start-Process powershell -ArgumentList "-NoExit", "-Command", $frontendCmd -WindowStyle Normal
        Write-Host "前端服务启动命令已执行" -ForegroundColor Green
    }
} else {
    Write-Host "找不到前端目录: $frontendPath" -ForegroundColor Red
}

# 显示服务信息
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   服务启动完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "服务访问地址:" -ForegroundColor Yellow
Write-Host "  前端:      http://localhost:3000" -ForegroundColor White
Write-Host "  后端API:   http://localhost:9090/api" -ForegroundColor White
Write-Host "  健康检查:  http://localhost:9090/api/health" -ForegroundColor White
Write-Host ""
Write-Host "Docker服务:" -ForegroundColor Yellow
Write-Host "  MySQL:        localhost:3306" -ForegroundColor White
Write-Host "  Redis:        localhost:6379" -ForegroundColor White
Write-Host "  Elasticsearch: http://localhost:9200" -ForegroundColor White
Write-Host "  RabbitMQ管理:  http://localhost:15672 (admin/admin123)" -ForegroundColor White
Write-Host ""
Write-Host "测试账号:" -ForegroundColor Yellow
Write-Host "  管理员: admin / 123456" -ForegroundColor White
Write-Host "  医生:   doctor001 / 123456" -ForegroundColor White
Write-Host "  患者:   patient001 / 123456" -ForegroundColor White
Write-Host ""
Write-Host "注意: 后端和前端服务已在新的PowerShell窗口中启动" -ForegroundColor Cyan
Write-Host "      请等待服务完全启动后再访问" -ForegroundColor Cyan
Write-Host ""

Read-Host "按Enter键退出"
