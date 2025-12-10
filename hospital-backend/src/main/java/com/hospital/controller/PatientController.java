package com.hospital.controller;

import com.hospital.annotation.RequireRole;
import com.hospital.common.Result;
import com.hospital.dto.PatientRegisterDTO;
import com.hospital.entity.Patient;
import com.hospital.service.DoctorFollowService;
import com.hospital.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {
    
    private final PatientService patientService;
    private final DoctorFollowService doctorFollowService;
    
    @PostMapping("/register")
    public ResponseEntity<Result<Patient>> register(@RequestBody PatientRegisterDTO dto) {
        Patient patient = patientService.register(dto);
        return ResponseEntity.ok(Result.success("注册成功", patient));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Result<Patient>> getById(@PathVariable Long id) {
        Patient patient = patientService.getById(id);
        return ResponseEntity.ok(Result.success("查询成功", patient));
    }
    
    @PutMapping("/update")
    public ResponseEntity<Result<Object>> update(@RequestBody Patient patient) {
        patientService.updateInfo(patient);
        return ResponseEntity.ok(Result.success("更新成功", null));
    }
    
    @GetMapping("/credit/{patientId}")
    public ResponseEntity<Result<Map<String, Object>>> getCreditInfo(@PathVariable Long patientId) {
        Map<String, Object> creditInfo = patientService.getCreditInfo(patientId);
        return ResponseEntity.ok(Result.success("查询成功", creditInfo));
    }
    
    /**
     * 关注医生
     */
    @PostMapping("/follow-doctor")
    @RequireRole(1) // 仅患者可访问
    public ResponseEntity<Result<Object>> followDoctor(@RequestParam Long patientId, @RequestParam Long doctorId) {
        doctorFollowService.followDoctor(patientId, doctorId);
        return ResponseEntity.ok(Result.success("关注成功", null));
    }
    
    /**
     * 取消关注医生
     */
    @PostMapping("/unfollow-doctor")
    @RequireRole(1) // 仅患者可访问
    public ResponseEntity<Result<Object>> unfollowDoctor(@RequestParam Long patientId, @RequestParam Long doctorId) {
        doctorFollowService.unfollowDoctor(patientId, doctorId);
        return ResponseEntity.ok(Result.success("取消关注成功", null));
    }
    
    /**
     * 获取患者关注的医生ID列表
     */
    @GetMapping("/followed-doctors/{patientId}")
    @RequireRole(1) // 仅患者可访问
    public ResponseEntity<Result<List<Long>>> getFollowedDoctors(@PathVariable Long patientId) {
        List<Long> doctorIds = doctorFollowService.getFollowedDoctorIds(patientId);
        return ResponseEntity.ok(Result.success("查询成功", doctorIds));
    }
    
    /**
     * 检查是否关注了医生
     */
    @GetMapping("/is-following")
    @RequireRole(1) // 仅患者可访问
    public ResponseEntity<Result<Boolean>> isFollowing(@RequestParam Long patientId, @RequestParam Long doctorId) {
        boolean isFollowing = doctorFollowService.isFollowing(patientId, doctorId);
        return ResponseEntity.ok(Result.success("查询成功", isFollowing));
    }
}

