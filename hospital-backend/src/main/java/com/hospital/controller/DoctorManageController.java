package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.annotation.RequireRole;
import com.hospital.common.Result;
import com.hospital.entity.*;
import com.hospital.repository.*;
import com.hospital.util.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 医生管理控制器
 * 实现医生的管理功能
 */
@RestController
@RequestMapping("/api/doctor-manage")
@RequireRole(2) // 仅医生可访问
@RequiredArgsConstructor
@Slf4j
public class DoctorManageController {

    private final DoctorRepository doctorRepository;
    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final ScheduleAdjustmentRepository scheduleAdjustmentRepository;
    private final PatientRepository patientRepository;
    private final SysUserRepository sysUserRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // ==================== 基本信息查询 ====================

    @GetMapping("/info")
    public ResponseEntity<Result<Map<String, Object>>> getDoctorInfo(
            @RequestHeader("Authorization") String token) {
        String username = jwtUtil.getUsernameFromToken(token.substring(7));
        SysUser user = sysUserRepository.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));

        Doctor doctor = doctorRepository.selectById(user.getDoctorId());

        // 查询号源信息
        LambdaQueryWrapper<Schedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(Schedule::getDoctorId, doctor.getId())
                .ge(Schedule::getScheduleDate, LocalDate.now())
                .orderByAsc(Schedule::getScheduleDate);
        List<Schedule> schedules = scheduleRepository.selectList(scheduleWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("doctor", doctor);
        result.put("schedules", schedules);
        result.put("totalSchedules", schedules.size());
        result.put("availableCount", schedules.stream()
                .mapToInt(s -> s.getTotalCount() - s.getReservedCount())
                .sum());

        return ResponseEntity.ok(Result.success("查询成功", result));
    }

    // ==================== 调班管理 ====================

    @PostMapping("/schedule-adjustment")
    public ResponseEntity<Result<ScheduleAdjustment>> createScheduleAdjustment(
            @RequestHeader("Authorization") String token,
            @RequestBody ScheduleAdjustmentDTO dto) {
        String username = jwtUtil.getUsernameFromToken(token.substring(7));
        SysUser user = sysUserRepository.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));

        // 查询原排班信息
        Schedule originalSchedule = scheduleRepository.selectById(dto.getScheduleId());
        if (originalSchedule == null || !originalSchedule.getDoctorId().equals(user.getDoctorId())) {
            return ResponseEntity.ok(Result.error(400, "排班不存在或无权限"));
        }

        ScheduleAdjustment adjustment = new ScheduleAdjustment();
        adjustment.setDoctorId(user.getDoctorId());
        adjustment.setScheduleId(dto.getScheduleId());
        adjustment.setOriginalDate(originalSchedule.getScheduleDate());
        adjustment.setOriginalTimeSlot(originalSchedule.getTimeSlot());
        adjustment.setNewDate(dto.getNewDate());
        adjustment.setNewTimeSlot(dto.getNewTimeSlot());
        adjustment.setReason(dto.getReason());
        adjustment.setStatus(0); // 待审核

        scheduleAdjustmentRepository.insert(adjustment);

        // 如果是停诊（新日期为空），直接停诊
        if (dto.getNewDate() == null) {
            originalSchedule.setStatus(0); // 停诊
            scheduleRepository.updateById(originalSchedule);
            adjustment.setStatus(1); // 停诊直接通过
            scheduleAdjustmentRepository.updateById(adjustment);
        }

        return ResponseEntity.ok(Result.success("调班申请提交成功", adjustment));
    }

    @GetMapping("/schedule-adjustments")
    public ResponseEntity<Result<List<ScheduleAdjustment>>> getScheduleAdjustments(
            @RequestHeader("Authorization") String token) {
        String username = jwtUtil.getUsernameFromToken(token.substring(7));
        SysUser user = sysUserRepository.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));

        List<ScheduleAdjustment> adjustments = scheduleAdjustmentRepository.selectList(
                new LambdaQueryWrapper<ScheduleAdjustment>()
                        .eq(ScheduleAdjustment::getDoctorId, user.getDoctorId())
                        .orderByDesc(ScheduleAdjustment::getCreateTime));

        return ResponseEntity.ok(Result.success("查询成功", adjustments));
    }

    // ==================== 患者队列查询 ====================

    @GetMapping("/patient-queue")
    public ResponseEntity<Result<List<Map<String, Object>>>> getPatientQueue(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) LocalDate date) {
        String username = jwtUtil.getUsernameFromToken(token.substring(7));
        SysUser user = sysUserRepository.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));

        if (date == null) {
            date = LocalDate.now();
        }

        // 查询当天的预约（包括待支付、已预约、已完成状态的预约）
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getDoctorId, user.getDoctorId())
                .eq(Appointment::getAppointmentDate, date)
                .in(Appointment::getStatus, 1, 2, 4) // 待支付、已预约或已完成
                .orderByAsc(Appointment::getTimeSlot);

        List<Appointment> appointments = appointmentRepository.selectList(wrapper);
        
        log.info("医生 {} (用户ID: {}) 在日期 {} 查询预约记录，状态条件: [1,2,4]，查询到 {} 条记录", 
                user.getDoctorId(), user.getId(), date, appointments.size());
        
        // 如果没有记录，查询所有状态的预约用于调试
        if (appointments.isEmpty()) {
            LambdaQueryWrapper<Appointment> allWrapper = new LambdaQueryWrapper<>();
            allWrapper.eq(Appointment::getDoctorId, user.getDoctorId())
                    .eq(Appointment::getAppointmentDate, date);
            List<Appointment> allAppointments = appointmentRepository.selectList(allWrapper);
            log.warn("该日期无有效预约，但查询到 {} 条所有状态的预约记录，状态分布: {}", 
                    allAppointments.size(),
                    allAppointments.stream()
                            .map(a -> "ID:" + a.getId() + ",状态:" + a.getStatus())
                            .collect(java.util.stream.Collectors.joining(", ")));
        }

        // 组装返回数据，包含患者详细信息
        List<Map<String, Object>> queue = appointments.stream()
                .map(apt -> {
            Map<String, Object> item = new HashMap<>();
            item.put("appointmentId", apt.getId());
            item.put("appointmentNo", apt.getAppointmentNo());
            item.put("timeSlot", apt.getTimeSlot());
            item.put("status", apt.getStatus());
            item.put("createTime", apt.getCreateTime());
            item.put("appointmentDate", apt.getAppointmentDate());
            item.put("completeTime", apt.getCompleteTime()); // 添加完成时间，用于判断是否已叫号

            // 查询患者信息（确保只显示预约了当前医生的患者）
            Patient patient = patientRepository.selectById(apt.getPatientId());
            if (patient != null) {
                Map<String, Object> patientInfo = new HashMap<>();
                patientInfo.put("id", patient.getId());
                patientInfo.put("name", patient.getName());
                patientInfo.put("phone", patient.getPhone());
                patientInfo.put("gender", patient.getGender());
                patientInfo.put("birthday", patient.getBirthday());
                patientInfo.put("allergyHistory", patient.getAllergyHistory());
                patientInfo.put("creditScore", patient.getCreditScore());
                item.put("patient", patientInfo);
            } else {
                // 如果患者不存在，记录警告但不返回该记录
                log.warn("预约 {} 关联的患者 {} 不存在", apt.getId(), apt.getPatientId());
                return null; // 返回null，后续过滤掉
            }

            return item;
        })
        .filter(item -> item != null && item.get("patient") != null) // 过滤掉没有患者信息的记录
        .toList();

        log.info("医生 {} 在日期 {} 返回 {} 条有效患者队列记录", user.getDoctorId(), date, queue.size());
        return ResponseEntity.ok(Result.success("查询成功", queue));
    }

    /**
     * 叫号功能
     */
    @PostMapping("/call-patient/{appointmentId}")
    public ResponseEntity<Result<Object>> callPatient(
            @RequestHeader("Authorization") String token,
            @PathVariable Long appointmentId) {
        String username = jwtUtil.getUsernameFromToken(token.substring(7));
        SysUser user = sysUserRepository.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));

        // 验证预约是否属于当前医生
        Appointment appointment = appointmentRepository.selectById(appointmentId);
        if (appointment == null || !appointment.getDoctorId().equals(user.getDoctorId())) {
            return ResponseEntity.ok(Result.error(400, "预约不存在或无权限"));
        }

        // 只要医生点击叫号就会成功，无论患者状态如何
        // 更新预约状态，记录完成时间表示已叫号（如果还没有记录）
        if (appointment.getCompleteTime() == null) {
            appointment.setCompleteTime(LocalDateTime.now());
            appointmentRepository.updateById(appointment);
        }

        log.info("医生 {} 叫号患者成功，预约号: {}, 患者状态: {}", 
                user.getDoctorId(), appointment.getAppointmentNo(), appointment.getStatus());
        return ResponseEntity.ok(Result.success("叫号成功", null));
    }

    // ==================== 修改密码 ====================

    @PostMapping("/change-password")
    public ResponseEntity<Result<Object>> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody ChangePasswordDTO dto) {
        String username = jwtUtil.getUsernameFromToken(token.substring(7));
        SysUser user = sysUserRepository.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));

        if (user == null) {
            return ResponseEntity.ok(Result.error(404, "用户不存在"));
        }

        // 验证原密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return ResponseEntity.ok(Result.error(400, "原密码错误"));
        }

        // 验证新密码格式
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6) {
            return ResponseEntity.ok(Result.error(400, "新密码长度至少6位"));
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        sysUserRepository.updateById(user);

        return ResponseEntity.ok(Result.success("密码修改成功", null));
    }

    @Data
    static class ScheduleAdjustmentDTO {
        private Long scheduleId;
        private LocalDate newDate;
        private String newTimeSlot;
        private String reason;
    }

    @Data
    static class ChangePasswordDTO {
        private String oldPassword;
        private String newPassword;
    }
}
