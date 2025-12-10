package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Department;
import com.hospital.repository.DepartmentRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController {
    
    private final DepartmentRepository departmentRepository;
    
    @GetMapping("/list")
    public ResponseEntity<Result<List<Department>>> list() {
        try {
            List<Department> departments = departmentRepository.selectList(
                new LambdaQueryWrapper<Department>()
                    .eq(Department::getStatus, 1)
            );
            return ResponseEntity.ok(Result.success("查询成功", departments));
        } catch (Exception e) {
            // 记录详细错误信息
            System.err.println("查询科室列表失败：" + e.getClass().getName());
            System.err.println("错误信息：" + e.getMessage());
            e.printStackTrace();
            throw e; // 重新抛出异常，让全局异常处理器处理
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Result<Department>> getById(@PathVariable Long id) {
        Department department = departmentRepository.selectById(id);
        return ResponseEntity.ok(Result.success("查询成功", department));
    }
}

