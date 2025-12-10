# Port Test Script
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Hospital System - Port Status Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$ports = @(
    @{Name="Backend Service (Spring Boot)"; Port=9090; URL="http://localhost:9090/api/health"},
    @{Name="Frontend Service (Vite)"; Port=3000; URL="http://localhost:3000"},
    @{Name="MySQL Database"; Port=3306; URL=""},
    @{Name="Redis Cache"; Port=6379; URL=""},
    @{Name="Elasticsearch"; Port=9200; URL="http://localhost:9200"},
    @{Name="RabbitMQ (AMQP)"; Port=5672; URL=""},
    @{Name="RabbitMQ (Management)"; Port=15672; URL="http://localhost:15672"}
)

$results = @()

foreach ($item in $ports) {
    Write-Host "Checking port $($item.Port) - $($item.Name)..." -NoNewline
    
    try {
        $connection = Test-NetConnection -ComputerName localhost -Port $item.Port -WarningAction SilentlyContinue -ErrorAction SilentlyContinue
        
        if ($connection.TcpTestSucceeded) {
            Write-Host " [OK]" -ForegroundColor Green
            
            # If HTTP service, try to access URL
            if ($item.URL -ne "") {
                try {
                    $response = Invoke-WebRequest -Uri $item.URL -Method GET -TimeoutSec 3 -ErrorAction SilentlyContinue
                    if ($response.StatusCode -eq 200) {
                        Write-Host "  HTTP Response: 200 OK" -ForegroundColor Green
                    }
                } catch {
                    Write-Host "  HTTP access failed, but port is open" -ForegroundColor Yellow
                }
            }
            
            $results += @{Name=$item.Name; Port=$item.Port; Status="OK"; Color="Green"}
        } else {
            Write-Host " [FAILED]" -ForegroundColor Red
            $results += @{Name=$item.Name; Port=$item.Port; Status="FAILED"; Color="Red"}
        }
    } catch {
        Write-Host " [ERROR]" -ForegroundColor Red
        $results += @{Name=$item.Name; Port=$item.Port; Status="ERROR"; Color="Red"}
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Test Results Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

foreach ($result in $results) {
    $color = if ($result.Status -eq "OK") { "Green" } else { "Red" }
    Write-Host "$($result.Name) (Port $($result.Port)): $($result.Status)" -ForegroundColor $color
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Service URLs" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Frontend:      http://localhost:3000" -ForegroundColor Yellow
Write-Host "Backend API:   http://localhost:9090/api" -ForegroundColor Yellow
Write-Host "Health Check:  http://localhost:9090/api/health" -ForegroundColor Yellow
Write-Host "RabbitMQ:      http://localhost:15672 (admin/admin123)" -ForegroundColor Yellow
Write-Host "Elasticsearch: http://localhost:9200" -ForegroundColor Yellow
Write-Host ""

# Check Docker container status
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Docker Container Status" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

try {
    $containers = docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" 2>$null
    if ($containers) {
        Write-Host $containers
    } else {
        Write-Host "No running Docker containers detected" -ForegroundColor Yellow
        Write-Host "Tip: Run 'docker-compose up -d' to start infrastructure services" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Unable to check Docker container status" -ForegroundColor Red
}

Write-Host ""
