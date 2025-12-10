# 医院挂号系统后端

## 技术栈
- Spring Boot 3.2.0
- MyBatis-Plus 3.5.5
- MySQL 8.0
- Redis
- RabbitMQ
- Elasticsearch

## 开发

```bash
# 使用Maven构建
mvn clean install

# 运行应用
mvn spring-boot:run
```

## 项目结构
```
src/main/java/com/hospital/
├── config/       # 配置类
├── controller/   # 控制器
├── service/      # 业务层
├── repository/    # 数据访问层
├── entity/       # 实体类
└── dto/          # 数据传输对象
```

## API接口

- GET /api/department/list - 获取科室列表
- GET /api/doctor/list - 获取医生列表
- GET /api/appointment/available-slots - 查询可预约号源
- POST /api/appointment/create - 创建预约
- POST /api/appointment/cancel/{id} - 取消预约
- GET /api/appointment/patient/{patientId} - 查询患者预约记录
- POST /api/patient/register - 患者注册

