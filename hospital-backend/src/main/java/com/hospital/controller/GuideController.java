package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.DoctorRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/guide")
@RequiredArgsConstructor
public class GuideController {
    
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    
    @GetMapping("/symptoms")
    public ResponseEntity<Result<Map<String, List<String>>>> getSymptoms() {
        Map<String, List<String>> symptoms = new HashMap<>();
        symptoms.put("内科", List.of("头痛", "发热", "咳嗽", "胸闷", "腹痛", "消化不良"));
        symptoms.put("外科", List.of("外伤", "骨折", "关节疼痛", "腰痛", "皮肤问题"));
        symptoms.put("儿科", List.of("儿童发热", "儿童咳嗽", "儿童腹泻", "儿童皮疹"));
        symptoms.put("妇产科", List.of("月经不调", "妇科炎症", "孕期检查", "产后复查"));
        return ResponseEntity.ok(Result.success("查询成功", symptoms));
    }
    
    @GetMapping("/recommend")
    public ResponseEntity<Result<List<Department>>> recommendBySymptom(@RequestParam String symptom) {
        // 简单的症状匹配逻辑
        List<Department> departments = departmentRepository.selectList(
            new LambdaQueryWrapper<Department>()
                .eq(Department::getStatus, 1)
        );
        
        // 根据症状推荐科室（简化实现）
        // 实际应该使用更复杂的算法
        return ResponseEntity.ok(Result.success("推荐成功", departments));
    }
    
    @GetMapping("/doctors")
    public ResponseEntity<Result<List<Doctor>>> recommendDoctors(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String specialty) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<Doctor>()
            .eq(Doctor::getStatus, 1);
        
        if (departmentId != null) {
            wrapper.eq(Doctor::getDepartmentId, departmentId);
        }
        if (specialty != null && !specialty.isEmpty()) {
            wrapper.like(Doctor::getSpecialty, specialty);
        }
        
        List<Doctor> doctors = doctorRepository.selectList(wrapper);
        return ResponseEntity.ok(Result.success("查询成功", doctors));
    }
}

