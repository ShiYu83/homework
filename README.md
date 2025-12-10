# 智能医院挂号管理系统

## 项目简介

智能医院挂号管理系统是一套集患者管理、医生排班、在线挂号、就诊流程管理于一体的智能化医院挂号系统。

## 技术架构

### 后端
- Spring Boot 3.2.0
- MyBatis-Plus 3.5.5
- MySQL 8.0
- Redis
- RabbitMQ
- Elasticsearch

### 前端
- Vue 3 + TypeScript
- Element Plus
- Vite
- Pinia
- Vue Router

## 快速开始

### 方式一：一键启动（推荐）

**Windows:**
```bash
启动所有服务.bat
```

**Linux/Mac:**
```bash
chmod +x start-all.ps1
./start-all.ps1
```

### 方式二：手动启动

#### 1. 启动基础设施

```bash
# 使用Docker Compose启动MySQL、Redis、Elasticsearch、RabbitMQ
docker-compose up -d

# 等待10-15秒让MySQL初始化完成
```

#### 2. 启动后端服务

```bash
cd hospital-backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 http://localhost:9090 启动

**重要提示：** 所有API接口都需要加上 `/api` 前缀：
- ✅ 正确访问：http://localhost:9090/api/health
- ✅ 根路径访问：http://localhost:9090 （显示API信息和可用端点）
- ❌ 错误访问：http://localhost:9090/health

#### 3. 启动前端服务

```bash
cd hospital-frontend
npm install  # 首次运行需要
npm run dev
```

前端服务将在 http://localhost:3000 启动

**注意**：首次访问系统需要先进行用户注册或登录。

### 测试账号

- **管理员**: `admin` / `123456`
- **医生**: `doctor001` / `123456`
- **患者**: `patient001` / `123456`

## 功能模块

### 1. 用户认证模块 ✅
- ✅ 用户登录（JWT Token认证）
- ✅ 用户注册
- ✅ 用户信息查询
- ✅ Token自动管理

### 2. 患者管理模块 ✅
- ✅ 患者注册
- ✅ 患者信息查询
- ✅ 患者信息管理（前端页面）
- ✅ 家庭成员管理（前端页面）

### 3. 医生排班管理 ✅
- ✅ 排班计划查询
- ✅ 排班计划创建
- ✅ 排班计划更新
- ✅ 排班计划删除
- ✅ 号源管理

### 4. 预约挂号模块 ✅
- ✅ 智能导诊（症状选择、科室推荐、医生推荐）
- ✅ 可预约号源查询
- ✅ 预约创建
- ✅ 预约取消
- ✅ 预约修改
- ✅ 预约记录查询
- ✅ 预约状态管理（待支付、已预约、已取消、已完成）

### 5. 支付结算模块 ✅
- ✅ 支付订单创建（挂号费、药品费、合并订单）
- ✅ 多种支付方式（微信、支付宝、医保）
- ✅ 支付处理
- ✅ 支付后自动更新预约状态为已完成
- ✅ 退款处理

### 6. 病例和处方模块 ✅
- ✅ 医生创建病例（症状、诊断、医嘱）
- ✅ 医生开具处方（选择药品、设置数量）
- ✅ 患者查看病例和处方
- ✅ 医生查看历史病例
- ✅ 处方支付状态跟踪

### 7. 药品管理模块 ✅
- ✅ 药品信息管理（增删改查）
- ✅ 药品上架/下架
- ✅ 药品搜索功能
- ✅ 药品库存管理

### 8. 科室医生管理 ✅
- ✅ 科室列表查询
- ✅ 医生列表查询
- ✅ 按科室筛选医生
- ✅ 医生信息管理

### 9. 管理员功能模块 ✅
- ✅ 医生信息管理
- ✅ 患者信息管理
- ✅ 科室信息管理
- ✅ 诊室信息管理
- ✅ 排班信息管理
- ✅ 药品信息管理
- ✅ 调班申请审核
- ✅ 数据统计分析

### 10. 医生功能模块 ✅
- ✅ 查看个人信息和排班
- ✅ 调班申请
- ✅ 患者队列查询
- ✅ 叫号功能
- ✅ 看病开药
- ✅ 查看历史病例

## 数据库

数据库初始化脚本位于 `sql/init.sql`，包含以下表：
- patient (患者表)
- doctor (医生表)
- department (科室表)
- schedule (排班表)
- appointment (预约记录表)
- sys_user (系统用户表)
- consulting_room (诊室表)
- schedule_adjustment (调班申请表)
- medical_record (病例表)
- prescription (处方表)
- prescription_item (处方明细表)
- payment (支付订单表)
- medicine (药品表)

**数据库连接信息：**
- 主机: localhost
- 端口: 3306
- 数据库: hospital
- 用户名: hospital_user
- 密码: hospital123

**执行数据库更新脚本（可选）：**
```bash
docker exec -i hospital-mysql mysql -uhospital_user -phospital123 hospital < sql/update_schema.sql
```

## API文档

### 认证相关 ✅
- `POST /api/auth/login` - 用户登录（返回JWT Token）
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/current` - 获取当前用户信息

### 患者相关 ✅
- `POST /api/patient/register` - 患者注册
- `GET /api/patient/{id}` - 查询患者信息
- `PUT /api/patient/update` - 更新患者信息
- `GET /api/patient/credit/{patientId}` - 获取患者诚信度信息

### 预约相关 ✅
- `GET /api/appointment/available-slots` - 查询可预约号源
- `POST /api/appointment/create` - 创建预约
- `PUT /api/appointment/modify/{id}` - 修改预约
- `PUT /api/appointment/cancel/{id}` - 取消预约
- `GET /api/appointment/patient/{patientId}` - 查询患者预约记录
- `GET /api/appointment/{id}` - 获取预约详情

### 排班管理 ✅
- `GET /api/schedule/list` - 查询排班列表
- `POST /api/schedule/create` - 创建排班
- `PUT /api/schedule/{id}` - 更新排班
- `DELETE /api/schedule/{id}` - 删除排班

### 智能导诊 ✅
- `GET /api/guide/symptoms` - 获取症状列表
- `GET /api/guide/recommend` - 根据症状推荐科室
- `GET /api/guide/doctors` - 推荐医生

### 支付相关 ✅
- `POST /api/payment/create` - 创建支付订单
- `POST /api/payment/pay` - 执行支付
- `POST /api/payment/refund` - 退款处理

### 科室医生相关 ✅
- `GET /api/department/list` - 获取科室列表
- `GET /api/department/{id}` - 获取科室详情
- `GET /api/doctor/list` - 获取医生列表
- `GET /api/doctor/{id}` - 获取医生详情

### 管理员相关 ✅
- `GET /api/admin/doctors` - 查询医生列表
- `POST /api/admin/doctors` - 创建医生
- `PUT /api/admin/doctors/{id}` - 更新医生信息
- `DELETE /api/admin/doctors/{id}` - 删除医生
- `GET /api/admin/patients` - 查询患者列表
- `POST /api/admin/patients` - 创建患者
- `PUT /api/admin/patients/{id}` - 更新患者信息
- `DELETE /api/admin/patients/{id}` - 删除患者
- `GET /api/admin/departments` - 查询科室列表
- `POST /api/admin/departments` - 创建科室
- `PUT /api/admin/departments/{id}` - 更新科室信息
- `DELETE /api/admin/departments/{id}` - 删除科室
- `GET /api/admin/rooms` - 查询诊室列表
- `POST /api/admin/rooms` - 创建诊室
- `PUT /api/admin/rooms/{id}` - 更新诊室信息
- `DELETE /api/admin/rooms/{id}` - 删除诊室
- `GET /api/admin/schedules` - 查询排班列表
- `POST /api/admin/schedules` - 创建排班
- `PUT /api/admin/schedules/{id}` - 更新排班信息
- `DELETE /api/admin/schedules/{id}` - 删除排班
- `GET /api/admin/schedule-adjustments` - 查询调班申请列表
- `PUT /api/admin/schedule-adjustments/{id}/approve` - 审核通过调班申请
- `PUT /api/admin/schedule-adjustments/{id}/reject` - 审核拒绝调班申请
- `GET /api/admin/statistics` - 查看统计数据
- `POST /api/admin/change-password` - 修改密码

### 医生管理相关 ✅
- `GET /api/doctor-manage/info` - 查看医生信息
- `POST /api/doctor-manage/schedule-adjustment` - 申请调班
- `GET /api/doctor-manage/schedule-adjustments` - 查看调班申请列表
- `GET /api/doctor-manage/patient-queue` - 查看患者队列
- `POST /api/doctor-manage/call-patient/{appointmentId}` - 叫号
- `POST /api/doctor-manage/change-password` - 修改密码

### 病例相关 ✅
- `POST /api/medical-record` - 创建病例（医生）
- `GET /api/medical-record/appointment/{appointmentId}` - 根据预约ID获取病例
- `GET /api/medical-record/patient` - 获取患者病例列表
- `GET /api/medical-record/doctor` - 获取医生病例列表

### 处方相关 ✅
- `POST /api/prescription` - 创建处方（医生）
- `GET /api/prescription/{id}` - 获取处方详情
- `GET /api/prescription/appointment/{appointmentId}` - 根据预约ID获取处方
- `GET /api/prescription/patient` - 获取患者处方列表
- `GET /api/prescription/all` - 获取所有处方（管理员）

### 药品相关 ✅
- `GET /api/medicine/available` - 获取可用药品（支持关键词搜索）
- `GET /api/medicine/{id}` - 获取药品详情
- `GET /api/medicine/all` - 获取所有药品（管理员）
- `POST /api/medicine` - 创建药品（管理员）
- `PUT /api/medicine/{id}` - 更新药品（管理员）
- `DELETE /api/medicine/{id}` - 删除药品（管理员）
- `PUT /api/medicine/{id}/status` - 上架/下架药品（管理员）

### 支付相关 ✅
- `POST /api/payment/create` - 创建支付订单（挂号费）
- `POST /api/payment/create-combined` - 创建合并订单（挂号费+药品费）
- `POST /api/payment/pay` - 执行支付
- `POST /api/payment/refund` - 退款处理

### 候诊队列相关 ✅
- `GET /api/queue/doctor/{doctorId}` - 获取医生候诊队列
- `GET /api/queue/patient/{patientId}` - 获取患者排队信息

## 开发环境

详细的环境配置请参考 `环境.md` 文件。

## 项目文档

- 软件需求规格说明书
- 软件概要设计文档
- 软件详细设计文档
- 数据库详细设计文档
- 测试文档
- 用户手册

## 许可证

MIT License

