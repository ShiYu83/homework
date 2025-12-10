package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Doctor;
import com.hospital.repository.DoctorRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {
    
    private final DoctorRepository doctorRepository;
    
    @GetMapping("/list")
    public ResponseEntity<Result<List<Doctor>>> list(@RequestParam(required = false) Long departmentId) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getStatus, 1);
        
        if (departmentId != null) {
            wrapper.eq(Doctor::getDepartmentId, departmentId);
        }
        
        List<Doctor> doctors = doctorRepository.selectList(wrapper);
        return ResponseEntity.ok(Result.success("查询成功", doctors));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Result<Doctor>> getById(@PathVariable Long id) {
        Doctor doctor = doctorRepository.selectById(id);
        return ResponseEntity.ok(Result.success("查询成功", doctor));
    }
}

