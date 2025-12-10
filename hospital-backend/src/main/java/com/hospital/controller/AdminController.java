package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.annotation.RequireRole;
import com.hospital.common.Result;
import com.hospital.entity.*;
import com.hospital.repository.*;
import com.hospital.service.AuthService;
import com.hospital.util.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 * 实现管理员的所有管理功能
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequireRole(3) // 仅管理员可访问
@RequiredArgsConstructor
public class AdminController {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DepartmentRepository departmentRepository;
    private final ConsultingRoomRepository consultingRoomRepository;
    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final ScheduleAdjustmentRepository scheduleAdjustmentRepository;
    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ==================== 医生信息管理 ====================

    @GetMapping("/doctors")
    public ResponseEntity<Result<List<Map<String, Object>>>> getDoctors(
            @RequestParam(required = false) Long departmentId) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        if (departmentId != null) {
            wrapper.eq(Doctor::getDepartmentId, departmentId);
        }
        List<Doctor> doctors = doctorRepository.selectList(wrapper);

        // 组装返回数据，包含账户信息
        List<Map<String, Object>> result = doctors.stream().map(doctor -> {
            Map<String, Object> doctorInfo = new HashMap<>();
            doctorInfo.put("id", doctor.getId());
            doctorInfo.put("name", doctor.getName());
            doctorInfo.put("departmentId", doctor.getDepartmentId());
            doctorInfo.put("title", doctor.getTitle());
            doctorInfo.put("specialty", doctor.getSpecialty());
            doctorInfo.put("introduction", doctor.getIntroduction());
            doctorInfo.put("status", doctor.getStatus());
            doctorInfo.put("entryDate", doctor.getEntryDate());
            doctorInfo.put("consultingRoomId", doctor.getConsultingRoomId());
            doctorInfo.put("createTime", doctor.getCreateTime());
            doctorInfo.put("avatar", doctor.getAvatar());

            // 查询对应的系统用户账户信息
            SysUser sysUser = sysUserRepository.selectOne(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getDoctorId, doctor.getId())
                            .eq(SysUser::getUserType, 2));
            if (sysUser != null) {
                doctorInfo.put("username", sysUser.getUsername());
                // 注意：这里不返回密码，密码需要通过单独的接口查询
            }

            return doctorInfo;
        }).toList();

        return ResponseEntity.ok(Result.success("查询成功", result));
    }

    /**
     * 查询医生账户和密码
     */
    @GetMapping("/doctors/{id}/account")
    public ResponseEntity<Result<Map<String, String>>> getDoctorAccount(@PathVariable Long id) {
        Doctor doctor = doctorRepository.selectById(id);
        if (doctor == null) {
            return ResponseEntity.ok(Result.error(404, "医生不存在"));
        }

        SysUser sysUser = sysUserRepository.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDoctorId, id)
                        .eq(SysUser::getUserType, 2));

        if (sysUser == null) {
            return ResponseEntity.ok(Result.error(404, "该医生尚未创建账户"));
        }

        Map<String, String> accountInfo = new HashMap<>();
        accountInfo.put("username", sysUser.getUsername());
        accountInfo.put("password", "123456"); // 返回默认密码，实际系统中密码已加密存储

        return ResponseEntity.ok(Result.success("查询成功", accountInfo));
    }

    @PostMapping("/doctors")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Result<Map<String, Object>>> createDoctor(@RequestBody Doctor doctor) {
        try {
            // 增强参数验证
            if (doctor == null) {
                return ResponseEntity.ok(Result.error(400, "医生信息不能为空"));
            }
            if (doctor.getName() == null || doctor.getName().trim().isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "医生姓名不能为空"));
            }
            if (doctor.getDepartmentId() == null) {
                return ResponseEntity.ok(Result.error(400, "科室不能为空"));
            }
            if (doctor.getTitle() == null || doctor.getTitle().trim().isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "职称不能为空"));
            }
            if (doctor.getEntryDate() == null) {
                return ResponseEntity.ok(Result.error(400, "入职日期不能为空"));
            }
            if (doctor.getConsultingRoomId() == null) {
                return ResponseEntity.ok(Result.error(400, "诊室编号不能为空"));
            }

            // 检查科室是否存在
            Department department = departmentRepository.selectById(doctor.getDepartmentId());
            if (department == null) {
                return ResponseEntity.ok(Result.error(400, "指定的科室不存在"));
            }

            // 检查诊室是否存在
            ConsultingRoom consultingRoom = consultingRoomRepository.selectById(doctor.getConsultingRoomId());
            if (consultingRoom == null) {
                return ResponseEntity.ok(Result.error(400, "指定的诊室不存在"));
            }

            // 设置创建时间
            if (doctor.getCreateTime() == null) {
                doctor.setCreateTime(LocalDateTime.now());
            }

            // 1. 创建医生记录
            doctorRepository.insert(doctor);

            // 2. 生成医生账户和密码
            // 入职时间年份（4位）
            String year = String.valueOf(doctor.getEntryDate().getYear());

            // 获取科室代码（2位，不足补0）
            String deptCode = department.getCode();
            // 从科室代码中提取数字部分，例如从"DEPT001"提取"001"
            String numericDeptCode = deptCode.replaceAll("\\D+", "");
            if (numericDeptCode.isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "科室代码格式错误，无法提取数字部分"));
            }
            // 格式化为2位，如果不足则补0
            numericDeptCode = String.format("%02d", Integer.parseInt(numericDeptCode));

            // 诊室编号，格式化为4位，不足则补0
            String roomNo = consultingRoom.getRoomNo();
            String numericRoomNo;
            try {
                numericRoomNo = String.format("%04d", Integer.parseInt(roomNo));
            } catch (NumberFormatException ex) {
                // 如果不是数字，取前4位或补0
                if (roomNo.length() >= 4) {
                    numericRoomNo = roomNo.substring(0, 4);
                } else {
                    numericRoomNo = String.format("%-4s", roomNo).replace(' ', '0');
                }
            }

            // 拼接用户名：入职年 + 科室代码 + 诊室编号
            String username = year + numericDeptCode + numericRoomNo;

            // 检查用户名是否已存在，如果存在则添加后缀1-9
            String finalUsername = username;
            for (int i = 1; i <= 9; i++) {
                SysUser existingUser = sysUserRepository.selectOne(
                        new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, finalUsername));
                if (existingUser == null) {
                    break;
                }
                finalUsername = username + i;
            }

            // 默认密码
            String password = "123456";

            // 3. 创建系统用户记录
            SysUser sysUser = new SysUser();
            sysUser.setUsername(finalUsername);
            sysUser.setPassword(passwordEncoder.encode(password));
            sysUser.setPhone(""); // 设置默认空字符串，避免数据库字段为空
            sysUser.setUserType(2); // 医生角色
            sysUser.setDoctorId(doctor.getId());
            sysUser.setStatus(1); // 启用状态
            sysUser.setCreateTime(LocalDateTime.now());
            sysUserRepository.insert(sysUser);

            // 5. 返回医生信息和账户信息
            Map<String, Object> result = new HashMap<>();
            result.put("doctor", doctor);
            result.put("username", finalUsername);
            result.put("password", password); // 返回明文密码供管理员查看

            return ResponseEntity.ok(Result.success("创建成功，账户已生成", result));
        } catch (Exception e) {
            log.error("创建医生失败", e);
            return ResponseEntity.ok(Result.error(500, "创建医生失败：" + e.getMessage()));
        }
    }

    @PutMapping("/doctors/{id}")
    public ResponseEntity<Result<Object>> updateDoctor(@PathVariable Long id, @RequestBody Doctor doctor) {
        doctor.setId(id);
        doctorRepository.updateById(doctor);
        return ResponseEntity.ok(Result.success("更新成功", null));
    }

    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<Result<Object>> deleteDoctor(@PathVariable Long id) {
        // 删除关联的系统用户记录
        sysUserRepository.delete(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDoctorId, id)
                .eq(SysUser::getUserType, 2));
        // 删除医生记录
        doctorRepository.deleteById(id);
        return ResponseEntity.ok(Result.success("删除成功", null));
    }

    // ==================== 患者信息管理 ====================

    @GetMapping("/patients")
    public ResponseEntity<Result<List<Patient>>> getPatients(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Patient::getName, keyword)
                    .or().like(Patient::getPhone, keyword)
                    .or().like(Patient::getIdCard, keyword));
        }
        List<Patient> patients = patientRepository.selectList(wrapper);
        return ResponseEntity.ok(Result.success("查询成功", patients));
    }

    @PostMapping("/patients")
    public ResponseEntity<Result<Patient>> createPatient(@RequestBody Patient patient) {
        patientRepository.insert(patient);
        return ResponseEntity.ok(Result.success("创建成功", patient));
    }

    @PutMapping("/patients/{id}")
    public ResponseEntity<Result<Object>> updatePatient(@PathVariable Long id, @RequestBody Patient patient) {
        patient.setId(id);
        patientRepository.updateById(patient);
        return ResponseEntity.ok(Result.success("更新成功", null));
    }

    @DeleteMapping("/patients/{id}")
    public ResponseEntity<Result<Object>> deletePatient(@PathVariable Long id) {
        patientRepository.deleteById(id);
        return ResponseEntity.ok(Result.success("删除成功", null));
    }

    // ==================== 科室信息管理 ====================

    @GetMapping("/departments")
    public ResponseEntity<Result<List<Department>>> getDepartments() {
        List<Department> departments = departmentRepository.selectList(null);
        return ResponseEntity.ok(Result.success("查询成功", departments));
    }

    @PostMapping("/departments")
    public ResponseEntity<Result<Department>> createDepartment(@RequestBody Department department) {
        departmentRepository.insert(department);
        return ResponseEntity.ok(Result.success("创建成功", department));
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<Result<Object>> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        department.setId(id);
        departmentRepository.updateById(department);
        return ResponseEntity.ok(Result.success("更新成功", null));
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<Result<Object>> deleteDepartment(@PathVariable Long id) {
        departmentRepository.deleteById(id);
        return ResponseEntity.ok(Result.success("删除成功", null));
    }

    // ==================== 诊室信息管理 ====================

    @GetMapping("/consulting-rooms")
    public ResponseEntity<Result<List<ConsultingRoom>>> getConsultingRooms(
            @RequestParam(required = false) Long departmentId) {
        LambdaQueryWrapper<ConsultingRoom> wrapper = new LambdaQueryWrapper<>();
        if (departmentId != null) {
            wrapper.eq(ConsultingRoom::getDepartmentId, departmentId);
        }
        List<ConsultingRoom> rooms = consultingRoomRepository.selectList(wrapper);
        return ResponseEntity.ok(Result.success("查询成功", rooms));
    }

    @PostMapping("/consulting-rooms")
    public ResponseEntity<Result<ConsultingRoom>> createConsultingRoom(@RequestBody ConsultingRoom room) {
        consultingRoomRepository.insert(room);
        return ResponseEntity.ok(Result.success("创建成功", room));
    }

    @PutMapping("/consulting-rooms/{id}")
    public ResponseEntity<Result<Object>> updateConsultingRoom(@PathVariable Long id,
            @RequestBody ConsultingRoom room) {
        room.setId(id);
        consultingRoomRepository.updateById(room);
        return ResponseEntity.ok(Result.success("更新成功", null));
    }

    @DeleteMapping("/consulting-rooms/{id}")
    public ResponseEntity<Result<Object>> deleteConsultingRoom(@PathVariable Long id) {
        consultingRoomRepository.deleteById(id);
        return ResponseEntity.ok(Result.success("删除成功", null));
    }

    // ==================== 号源池管理 ====================

    @GetMapping("/schedules")
    public ResponseEntity<Result<List<Schedule>>> getSchedules(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        if (doctorId != null) {
            wrapper.eq(Schedule::getDoctorId, doctorId);
        }
        if (departmentId != null) {
            wrapper.eq(Schedule::getDepartmentId, departmentId);
        }
        if (startDate != null) {
            wrapper.ge(Schedule::getScheduleDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Schedule::getScheduleDate, endDate);
        }
        List<Schedule> schedules = scheduleRepository.selectList(wrapper);
        return ResponseEntity.ok(Result.success("查询成功", schedules));
    }

    @PostMapping("/schedules")
    public ResponseEntity<Result<Schedule>> createSchedule(@RequestBody Schedule schedule) {
        scheduleRepository.insert(schedule);
        return ResponseEntity.ok(Result.success("创建成功", schedule));
    }

    @PutMapping("/schedules/{id}")
    public ResponseEntity<Result<Object>> updateSchedule(@PathVariable Long id, @RequestBody Schedule schedule) {
        schedule.setId(id);
        scheduleRepository.updateById(schedule);
        return ResponseEntity.ok(Result.success("更新成功", null));
    }

    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<Result<Object>> deleteSchedule(@PathVariable Long id) {
        scheduleRepository.deleteById(id);
        return ResponseEntity.ok(Result.success("删除成功", null));
    }

    // ==================== 预约时段管理 ====================

    @GetMapping("/time-slots")
    public ResponseEntity<Result<List<String>>> getTimeSlots() {
        // 预定义的时段列表
        List<String> timeSlots = List.of(
                "08:00-09:00", "09:00-10:00", "10:00-11:00", "11:00-12:00",
                "14:00-15:00", "15:00-16:00", "16:00-17:00", "17:00-18:00");
        return ResponseEntity.ok(Result.success("查询成功", timeSlots));
    }

    // ==================== 修改个人密码 ====================

    @PostMapping("/change-password")
    public ResponseEntity<Result<Object>> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody ChangePasswordDTO dto) {
        String username = jwtUtil.getUsernameFromToken(token.substring(7));
        SysUser user = sysUserRepository.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return ResponseEntity.ok(Result.error(400, "原密码错误"));
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        sysUserRepository.updateById(user);

        return ResponseEntity.ok(Result.success("密码修改成功", null));
    }

    /**
     * 为已有医生创建系统用户账户
     * 用于修复医生记录存在但系统用户记录缺失的情况
     */
    @PostMapping("/doctors/{id}/create-account")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Result<Map<String, Object>>> createAccountForDoctor(@PathVariable Long id) {
        try {
            // 查询医生记录
            Doctor doctor = doctorRepository.selectById(id);
            if (doctor == null) {
                return ResponseEntity.ok(Result.error(404, "医生不存在"));
            }

            // 检查是否已经存在关联的系统用户
            SysUser existingUser = sysUserRepository.selectOne(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getDoctorId, id)
                            .eq(SysUser::getUserType, 2));
            if (existingUser != null) {
                return ResponseEntity.ok(Result.error(400, "该医生已经有系统用户账户"));
            }

            // 检查科室是否存在
            Department department = departmentRepository.selectById(doctor.getDepartmentId());
            if (department == null) {
                return ResponseEntity.ok(Result.error(400, "医生所属科室不存在"));
            }

            // 检查诊室是否存在
            ConsultingRoom consultingRoom = consultingRoomRepository.selectById(doctor.getConsultingRoomId());
            if (consultingRoom == null) {
                return ResponseEntity.ok(Result.error(400, "医生所属诊室不存在"));
            }

            // 生成医生账户和密码
            // 入职时间年份（4位）
            String year = String.valueOf(doctor.getEntryDate().getYear());

            // 获取科室代码（2位，不足补0）
            String deptCode = department.getCode();
            // 从科室代码中提取数字部分，例如从"DEPT001"提取"001"
            String numericDeptCode = deptCode.replaceAll("\\D+", "");
            if (numericDeptCode.isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "科室代码格式错误，无法提取数字部分"));
            }
            // 格式化为2位，如果不足则补0
            numericDeptCode = String.format("%02d", Integer.parseInt(numericDeptCode));

            // 诊室编号，格式化为4位，不足则补0
            String roomNo = consultingRoom.getRoomNo();
            String numericRoomNo;
            try {
                numericRoomNo = String.format("%04d", Integer.parseInt(roomNo));
            } catch (NumberFormatException ex) {
                // 如果不是数字，取前4位或补0
                if (roomNo.length() >= 4) {
                    numericRoomNo = roomNo.substring(0, 4);
                } else {
                    numericRoomNo = String.format("%-4s", roomNo).replace(' ', '0');
                }
            }

            // 拼接用户名：入职年 + 科室代码 + 诊室编号
            String username = year + numericDeptCode + numericRoomNo;

            // 检查用户名是否已存在，如果存在则添加后缀1-9
            String finalUsername = username;
            for (int i = 1; i <= 9; i++) {
                SysUser userCheck = sysUserRepository.selectOne(
                        new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, finalUsername));
                if (userCheck == null) {
                    break;
                }
                finalUsername = username + i;
            }

            // 默认密码
            String password = "123456";

            // 创建系统用户记录
            SysUser sysUser = new SysUser();
            sysUser.setUsername(finalUsername);
            sysUser.setPassword(passwordEncoder.encode(password));
            sysUser.setPhone(""); // 设置默认空字符串，避免数据库字段为空
            sysUser.setUserType(2); // 医生角色
            sysUser.setDoctorId(doctor.getId());
            sysUser.setStatus(1); // 启用状态
            sysUser.setCreateTime(LocalDateTime.now());
            sysUserRepository.insert(sysUser);

            // 返回医生信息和账户信息
            Map<String, Object> result = new HashMap<>();
            result.put("doctor", doctor);
            result.put("username", finalUsername);
            result.put("password", password); // 返回明文密码供管理员查看

            return ResponseEntity.ok(Result.success("账户创建成功", result));
        } catch (Exception e) {
            log.error("为医生创建账户失败", e);
            return ResponseEntity.ok(Result.error(500, "创建账户失败：" + e.getMessage()));
        }
    }

    // ==================== 调班审核管理 ====================
    
    /**
     * 获取所有待审核的调班申请
     */
    @GetMapping("/schedule-adjustments")
    public ResponseEntity<Result<List<Map<String, Object>>>> getScheduleAdjustments() {
        LambdaQueryWrapper<ScheduleAdjustment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScheduleAdjustment::getStatus, 0) // 待审核
                .orderByDesc(ScheduleAdjustment::getCreateTime);
        
        List<ScheduleAdjustment> adjustments = scheduleAdjustmentRepository.selectList(wrapper);
        
        // 组装返回数据，包含医生信息
        List<Map<String, Object>> result = adjustments.stream().map(adj -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", adj.getId());
            item.put("doctorId", adj.getDoctorId());
            item.put("scheduleId", adj.getScheduleId());
            item.put("originalDate", adj.getOriginalDate());
            item.put("originalTimeSlot", adj.getOriginalTimeSlot());
            item.put("newDate", adj.getNewDate());
            item.put("newTimeSlot", adj.getNewTimeSlot());
            item.put("reason", adj.getReason());
            item.put("status", adj.getStatus());
            item.put("createTime", adj.getCreateTime());
            
            // 查询医生信息
            Doctor doctor = doctorRepository.selectById(adj.getDoctorId());
            if (doctor != null) {
                Map<String, Object> doctorInfo = new HashMap<>();
                doctorInfo.put("id", doctor.getId());
                doctorInfo.put("name", doctor.getName());
                doctorInfo.put("title", doctor.getTitle());
                doctorInfo.put("departmentId", doctor.getDepartmentId());
                item.put("doctor", doctorInfo);
            }
            
            // 查询受影响的预约数量
            LambdaQueryWrapper<Appointment> aptWrapper = new LambdaQueryWrapper<>();
            aptWrapper.eq(Appointment::getScheduleId, adj.getScheduleId())
                    .eq(Appointment::getStatus, 2); // 已预约
            long affectedCount = appointmentRepository.selectCount(aptWrapper);
            item.put("affectedAppointments", affectedCount);
            
            return item;
        }).toList();
        
        return ResponseEntity.ok(Result.success("查询成功", result));
    }
    
    /**
     * 审核调班申请
     */
    @PostMapping("/schedule-adjustments/{id}/review")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Result<Object>> reviewScheduleAdjustment(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token,
            @RequestBody ReviewScheduleAdjustmentDTO dto) {
        try {
            // 获取管理员ID
            String username = jwtUtil.getUsernameFromToken(token.substring(7));
            SysUser admin = sysUserRepository.selectOne(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
            
            // 查询调班申请
            ScheduleAdjustment adjustment = scheduleAdjustmentRepository.selectById(id);
            if (adjustment == null) {
                return ResponseEntity.ok(Result.error(404, "调班申请不存在"));
            }
            
            if (adjustment.getStatus() != 0) {
                return ResponseEntity.ok(Result.error(400, "该调班申请已审核"));
            }
            
            // 更新审核信息
            adjustment.setStatus(dto.getApproved() ? 1 : 2); // 1-已通过 2-已拒绝
            adjustment.setReviewerId(admin.getId());
            adjustment.setReviewComment(dto.getComment());
            adjustment.setUpdateTime(LocalDateTime.now());
            scheduleAdjustmentRepository.updateById(adjustment);
            
            // 如果同意，需要调整患者预约时间
            if (dto.getApproved()) {
                // 查询该排班下的所有已预约记录
                LambdaQueryWrapper<Appointment> aptWrapper = new LambdaQueryWrapper<>();
                aptWrapper.eq(Appointment::getScheduleId, adjustment.getScheduleId())
                        .eq(Appointment::getStatus, 2); // 已预约状态
                
                List<Appointment> appointments = appointmentRepository.selectList(aptWrapper);
                
                // 更新每个预约的时间和排班
                for (Appointment apt : appointments) {
                    // 如果指定了新日期和时间段，更新预约
                    if (adjustment.getNewDate() != null) {
                        apt.setAppointmentDate(adjustment.getNewDate());
                    }
                    if (adjustment.getNewTimeSlot() != null && !adjustment.getNewTimeSlot().isEmpty()) {
                        apt.setTimeSlot(adjustment.getNewTimeSlot());
                    }
                    
                    // 如果指定了新的排班，需要查找或创建新的排班记录
                    if (adjustment.getNewDate() != null && adjustment.getNewTimeSlot() != null) {
                        // 查找新的排班记录
                        LambdaQueryWrapper<Schedule> scheduleWrapper = new LambdaQueryWrapper<>();
                        scheduleWrapper.eq(Schedule::getDoctorId, adjustment.getDoctorId())
                                .eq(Schedule::getScheduleDate, adjustment.getNewDate())
                                .eq(Schedule::getTimeSlot, adjustment.getNewTimeSlot());
                        
                        Schedule newSchedule = scheduleRepository.selectOne(scheduleWrapper);
                        if (newSchedule != null) {
                            apt.setScheduleId(newSchedule.getId());
                        }
                    }
                    
                    apt.setUpdateTime(LocalDateTime.now());
                    appointmentRepository.updateById(apt);
                }
                
                // 更新原排班状态（可选：标记为已调班）
                Schedule originalSchedule = scheduleRepository.selectById(adjustment.getScheduleId());
                if (originalSchedule != null) {
                    // 可以减少已预约数，因为已经转移到新排班
                    // 这里可以根据业务需求调整
                }
                
                log.info("管理员 {} 同意调班申请 {}，已调整 {} 个预约", admin.getId(), id, appointments.size());
            } else {
                log.info("管理员 {} 拒绝调班申请 {}", admin.getId(), id);
            }
            
            return ResponseEntity.ok(Result.success(
                    dto.getApproved() ? "调班申请已通过，患者预约时间已调整" : "调班申请已拒绝", null));
        } catch (Exception e) {
            log.error("审核调班申请失败", e);
            return ResponseEntity.ok(Result.error(500, "审核失败：" + e.getMessage()));
        }
    }
    
    @Data
    static class ReviewScheduleAdjustmentDTO {
        private Boolean approved; // true-同意 false-拒绝
        private String comment; // 审核意见
    }
    
    @Data
    static class ChangePasswordDTO {
        private String oldPassword;
        private String newPassword;
    }
}
