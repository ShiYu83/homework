# 重启 Docker 服务指南

## 方法一：使用 Docker Compose 命令（推荐）

### 1. 停止所有服务
```bash
docker-compose down
```
这会停止并删除所有容器。

### 2. 启动所有服务
```bash
docker-compose up -d
```
`-d` 参数表示在后台运行（detached mode）。

### 3. 查看服务状态
```bash
docker-compose ps
```

### 完整重启（一步完成）
```bash
docker-compose restart
```
这会重启所有正在运行的服务容器，不会删除数据。

## 方法二：重启单个服务

### 重启MySQL
```bash
docker-compose restart mysql
```

### 重启Redis
```bash
docker-compose restart redis
```

### 重启Elasticsearch
```bash
docker-compose restart elasticsearch
```

### 重启RabbitMQ
```bash
docker-compose restart rabbitmq
```

## 方法三：使用Docker命令

### 查看运行中的容器
```bash
docker ps
```

### 停止容器
```bash
docker stop <容器名称或ID>
```

### 启动容器
```bash
docker start <容器名称或ID>
```

### 重启容器
```bash
docker restart <容器名称或ID>
```

### 停止所有容器
```bash
docker stop $(docker ps -q)
```

## 方法四：完整重置（包括数据）

⚠️ **警告**：这会删除所有容器和数据！

```bash
# 停止并删除所有容器
docker-compose down

# 删除所有卷（包括数据库数据）
docker-compose down -v

# 重新创建并启动
docker-compose up -d
```

## 常用操作

### 查看日志
```bash
# 查看所有服务日志
docker-compose logs

# 查看特定服务日志（实时）
docker-compose logs -f mysql

# 查看最近100行日志
docker-compose logs --tail=100 mysql
```

### 进入容器内部
```bash
# 进入MySQL容器
docker exec -it hospital-mysql bash

# 进入MySQL命令行
docker exec -it hospital-mysql mysql -uhospital_user -phospital123 hospital
```

### 检查服务健康状态
```bash
# 查看所有服务状态
docker-compose ps

# 查看特定服务状态
docker-compose ps mysql
```

## Windows PowerShell 快速命令

### 完整重启
```powershell
cd C:\Users\28953\Desktop\homework4
docker-compose restart
```

### 停止并重新启动
```powershell
cd C:\Users\28953\Desktop\homework4
docker-compose down
docker-compose up -d
```

### 查看日志
```powershell
docker-compose logs -f
```

## 故障排查

### 如果服务无法启动

1. **检查Docker是否运行**
   ```powershell
   docker info
   ```

2. **查看错误日志**
   ```powershell
   docker-compose logs
   ```

3. **检查端口占用**
   - MySQL: 3306
   - Redis: 6379
   - Elasticsearch: 9200
   - RabbitMQ: 5672, 15672

4. **清理并重新创建**
   ```powershell
   docker-compose down -v
   docker-compose up -d
   ```

### 如果数据丢失

如果是使用 `docker-compose down -v`，数据会被删除。要恢复数据：

1. 查看是否有数据备份
2. 重新执行初始化SQL脚本：
   ```powershell
   docker exec -i hospital-mysql mysql -uhospital_user -phospital123 hospital < sql/init.sql
   ```

## 项目特定的Docker服务

根据 `docker-compose.yml`，项目包含以下服务：

- **mysql** (医院MySQL数据库) - 端口 3306
- **redis** (缓存服务) - 端口 6379
- **elasticsearch** (搜索引擎) - 端口 9200
- **rabbitmq** (消息队列) - 端口 5672, 15672

## 推荐的重启步骤

### 日常重启（保留数据）
```powershell
cd C:\Users\28953\Desktop\homework4
docker-compose restart
```

### 完全重启（包括重新创建容器）
```powershell
cd C:\Users\28953\Desktop\homework4
docker-compose down
docker-compose up -d
```

### 验证服务是否正常
```powershell
# 等待10秒让服务启动
Start-Sleep -Seconds 10

# 检查服务状态
docker-compose ps

# 测试MySQL连接
docker exec hospital-mysql mysql -uhospital_user -phospital123 -e "SELECT 1"
```

## 注意事项

1. **重启不会删除数据**：使用 `docker-compose restart` 或 `docker-compose down`（不加 `-v`）不会删除数据库中的数据。

2. **卷数据持久化**：数据库数据存储在Docker卷中，重启服务不会丢失。

3. **服务启动顺序**：Docker Compose会自动处理服务依赖关系。

4. **端口冲突**：如果端口被占用，服务可能无法启动，需要先停止占用端口的进程。






