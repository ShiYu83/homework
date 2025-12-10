package com.hospital.controller;

import com.hospital.annotation.RequireRole;
import com.hospital.common.Result;
import com.hospital.entity.Prescription;
import com.hospital.entity.PrescriptionItem;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.SysUserRepository;
import com.hospital.service.PrescriptionService;
import com.hospital.util.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/prescription")
@RequiredArgsConstructor
public class PrescriptionController {
    
    private final PrescriptionService prescriptionService;
    private final AppointmentRepository appointmentRepository;
    private final JwtUtil jwtUtil;
    private final SysUserRepository sysUserRepository;
    
    /**
     * 创建处方（医生）
     */
    @PostMapping
    @RequireRole(2)
    public ResponseEntity<Result<Prescription>> createPrescription(
            @RequestHeader("Authorization") String token,
            @RequestBody CreatePrescriptionDTO dto) {
        String username = jwtUtil.getUsernameFromToken(token.substring(7));
        var user = sysUserRepository.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.hospital.entity.SysUser>()
                .eq(com.hospital.entity.SysUser::getUsername, username)
        );
        
        if (user == null || user.getDoctorId() == null) {
            return ResponseEntity.ok(Result.error(403, "无权限"));
        }
        
        // 验证预约是否存在且属于当前医生
        var appointment = appointmentRepository.selectById(dto.getAppointmentId());
        if (appointment == null || !appointment.getDoctorId().equals(user.getDoctorId())) {
            return ResponseEntity.ok(Result.error(400, "预约不存在或无权限"));
        }
        
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            return ResponseEntity.ok(Result.error(400, "处方明细不能为空"));
        }
        
        Prescription prescription = prescriptionService.createPrescription(
            dto.getMedicalRecordId(),
            dto.getAppointmentId(),
            dto.getItems()
        );
        
        return ResponseEntity.ok(Result.success("处方创建成功", prescription));
    }
    
    /**
     * 获取处方详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<Map<String, Object>>> getPrescriptionDetail(@PathVariable Long id) {
        Map<String, Object> detail = prescriptionService.getPrescriptionDetail(id);
        if (detail == null) {
            return ResponseEntity.ok(Result.error(404, "处方不存在"));
        }
        return ResponseEntity.ok(Result.success(detail));
    }
    
    /**
     * 根据预约ID获取处方
     */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<Result<Prescription>> getPrescriptionByAppointmentId(
            @PathVariable Long appointmentId) {
        Prescription prescription = prescriptionService.getPrescriptionByAppointmentId(appointmentId);
        if (prescription == null) {
            return ResponseEntity.ok(Result.error(404, "处方不存在"));
        }
        return ResponseEntity.ok(Result.success(prescription));
    }
    
    /**
     * 获取患者的所有处方（患者）
     */
    @GetMapping("/patient")
    @RequireRole(1)
    public ResponseEntity<Result<List<Prescription>>> getPrescriptionsByPatientId(
            @RequestHeader("Authorization") String token) {
        String username = jwtUtil.getUsernameFromToken(token.substring(7));
        var user = sysUserRepository.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.hospital.entity.SysUser>()
                .eq(com.hospital.entity.SysUser::getUsername, username)
        );
        
        if (user == null || user.getPatientId() == null) {
            return ResponseEntity.ok(Result.error(403, "无权限"));
        }
        
        List<Prescription> prescriptions = prescriptionService.getPrescriptionsByPatientId(user.getPatientId());
        return ResponseEntity.ok(Result.success(prescriptions));
    }
    
    /**
     * 获取所有处方（管理员）
     */
    @GetMapping("/all")
    @RequireRole(3)
    public ResponseEntity<Result<List<Prescription>>> getAllPrescriptions() {
        List<Prescription> prescriptions = prescriptionService.getAllPrescriptions();
        return ResponseEntity.ok(Result.success(prescriptions));
    }
    
    @Data
    public static class CreatePrescriptionDTO {
        private Long medicalRecordId;
        private Long appointmentId;
        private List<PrescriptionItem> items;
    }
}

