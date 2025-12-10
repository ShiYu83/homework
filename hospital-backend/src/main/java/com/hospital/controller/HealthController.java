package com.hospital.controller;


import com.hospital.common.Result;
import com.hospital.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {
    
    @Resource
    private DataSource dataSource;
    private final DepartmentRepository departmentRepository;
    
    @GetMapping
    public ResponseEntity<Result<Map<String, Object>>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", "Hospital Registration System");
        data.put("version", "1.0.0");
        
        // 测试数据库连接
        Map<String, Object> dbStatus = new HashMap<>();
        try {
            try (Connection conn = dataSource.getConnection()) {
                dbStatus.put("connected", !conn.isClosed());
                dbStatus.put("catalog", conn.getCatalog());
            }
        } catch (Exception e) {
            dbStatus.put("connected", false);
            dbStatus.put("error", e.getMessage());
        }
        
        // 测试数据库查询
        try {
            long count = departmentRepository.selectCount(null);
            dbStatus.put("queryTest", "success");
            dbStatus.put("departmentCount", count);
        } catch (Exception e) {
            dbStatus.put("queryTest", "failed");
            dbStatus.put("queryError", e.getMessage());
        }
        
        data.put("database", dbStatus);
        return ResponseEntity.ok(Result.success(data));
    }
}

