package com.hospital.controller;

import com.hospital.annotation.RequireRole;
import com.hospital.common.Result;
import com.hospital.entity.MedicalRecord;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.SysUserRepository;
import com.hospital.service.MedicalRecordService;
import com.hospital.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/medical-record")
@RequiredArgsConstructor
public class MedicalRecordController {
    
    private final MedicalRecordService medicalRecordService;
    private final AppointmentRepository appointmentRepository;
    private final JwtUtil jwtUtil;
    private final SysUserRepository sysUserRepository;
    
    /**
     * 创建病例（医生）
     */
    @PostMapping
    @RequireRole(2)
    public ResponseEntity<Result<MedicalRecord>> createMedicalRecord(
            @RequestHeader("Authorization") String token,
            @RequestBody MedicalRecord medicalRecord) {
        String username = jwtUtil.getUsernameFromToken(token.substring(7));
        var user = sysUserRepository.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.hospital.entity.SysUser>()
                .eq(com.hospital.entity.SysUser::getUsername, username)
        );
        
        if (user == null || user.getDoctorId() == null) {
            return ResponseEntity.ok(Result.error(403, "无权限"));
        }
        
        // 验证预约是否存在且属于当前医生
        var appointment = appointmentRepository.selectById(medicalRecord.getAppointmentId());
        if (appointment == null || !appointment.getDoctorId().equals(user.getDoctorId())) {
            return ResponseEntity.ok(Result.error(400, "预约不存在或无权限"));
        }
        
        medicalRecord.setDoctorId(user.getDoctorId());
        medicalRecord.setPatientId(appointment.getPatientId());
        
        MedicalRecord created = medicalRecordService.createMedicalRecord(medicalRecord);
        return ResponseEntity.ok(Result.success("病例创建成功", created));
    }
    
    /**
     * 根据预约ID获取病例
     */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<Result<MedicalRecord>> getMedicalRecordByAppointmentId(
            @PathVariable Long appointmentId) {
        MedicalRecord record = medicalRecordService.getMedicalRecordByAppointmentId(appointmentId);
        if (record == null) {
            return ResponseEntity.ok(Result.error(404, "病例不存在"));
        }
        return ResponseEntity.ok(Result.success(record));
    }
    
    /**
     * 获取患者的病例列表（患者）
     */
    @GetMapping("/patient")
    @RequireRole(1)
    public ResponseEntity<Result<List<MedicalRecord>>> getMedicalRecordsByPatientId(
            @RequestHeader("Authorization") String token) {
        String username = jwtUtil.getUsernameFromToken(token.substring(7));
        var user = sysUserRepository.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.hospital.entity.SysUser>()
                .eq(com.hospital.entity.SysUser::getUsername, username)
        );
        
        if (user == null || user.getPatientId() == null) {
            return ResponseEntity.ok(Result.error(403, "无权限"));
        }
        
        List<MedicalRecord> records = medicalRecordService.getMedicalRecordsByPatientId(user.getPatientId());
        return ResponseEntity.ok(Result.success(records));
    }
    
    /**
     * 获取医生的病例列表（医生）
     */
    @GetMapping("/doctor")
    @RequireRole(2)
    public ResponseEntity<Result<List<MedicalRecord>>> getMedicalRecordsByDoctorId(
            @RequestHeader("Authorization") String token) {
        String username = jwtUtil.getUsernameFromToken(token.substring(7));
        var user = sysUserRepository.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.hospital.entity.SysUser>()
                .eq(com.hospital.entity.SysUser::getUsername, username)
        );
        
        if (user == null || user.getDoctorId() == null) {
            return ResponseEntity.ok(Result.error(403, "无权限"));
        }
        
        List<MedicalRecord> records = medicalRecordService.getMedicalRecordsByDoctorId(user.getDoctorId());
        return ResponseEntity.ok(Result.success(records));
    }
}

