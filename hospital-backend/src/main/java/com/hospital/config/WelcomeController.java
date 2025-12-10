package com.hospital.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 根路径控制器
 * 处理对 http://localhost:9090 的访问，显示API信息
 */
@RestController
public class WelcomeController {
    
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "欢迎使用医院挂号管理系统 API");
        response.put("service", "Hospital Registration System");
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("apiBaseUrl", "http://localhost:9090/api");
        response.put("frontendUrl", "http://localhost:3000");
        response.put("healthCheck", "http://localhost:9090/api/health");
        response.put("note", "所有API接口都需要加上 /api 前缀");
        response.put("frontendNote", "前端应用请访问: http://localhost:3000");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("健康检查", "GET /api/health");
        endpoints.put("用户登录", "POST /api/auth/login");
        endpoints.put("用户注册", "POST /api/patient/register");
        endpoints.put("科室列表", "GET /api/department/list");
        endpoints.put("医生列表", "GET /api/doctor/list");
        endpoints.put("预约接口", "GET /api/appointment/available-slots");
        endpoints.put("智能导诊", "GET /api/guide/symptoms");
        endpoints.put("排班管理", "GET /api/schedule/list");
        endpoints.put("候诊队列", "GET /api/queue/doctor/{doctorId}");
        response.put("endpoints", endpoints);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/api")
    public ResponseEntity<Map<String, Object>> apiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "这是后端API服务器");
        response.put("service", "Hospital Registration System Backend");
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("frontendUrl", "http://localhost:3000");
        response.put("note", "请访问前端应用: http://localhost:3000");
        response.put("apiEndpoints", "所有API接口路径如下:");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("健康检查", "GET /api/health");
        endpoints.put("用户登录", "POST /api/auth/login");
        endpoints.put("用户注册", "POST /api/patient/register");
        endpoints.put("科室列表", "GET /api/department/list");
        endpoints.put("医生列表", "GET /api/doctor/list");
        endpoints.put("预约接口", "GET /api/appointment/available-slots");
        endpoints.put("智能导诊", "GET /api/guide/symptoms");
        endpoints.put("排班管理", "GET /api/schedule/list");
        endpoints.put("候诊队列", "GET /api/queue/doctor/{doctorId}");
        endpoints.put("上传头像", "POST /api/upload/avatar");
        response.put("endpoints", endpoints);
        
        Map<String, Object> uploadInfo = new HashMap<>();
        uploadInfo.put("uploadEndpoint", "/api/upload/avatar");
        uploadInfo.put("method", "POST");
        uploadInfo.put("note", "需要管理员权限，使用multipart/form-data格式上传文件");
        response.put("upload", uploadInfo);
        
        return ResponseEntity.ok(response);
    }
}

