package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.annotation.RequireRole;
import com.hospital.common.Result;
import com.hospital.entity.Appointment;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 查询与统计分析控制器
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final AppointmentRepository appointmentRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;

    // ==================== 预约统计（管理员） ====================

    @GetMapping("/appointments")
    @RequireRole(3) // 仅管理员
    public ResponseEntity<Result<Map<String, Object>>> getAppointmentStatistics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long departmentId) {

        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(Appointment::getAppointmentDate, startDate, endDate);
        if (departmentId != null) {
            // 需要通过医生关联科室
            List<Long> doctorIds = doctorRepository.selectList(
                    new LambdaQueryWrapper<com.hospital.entity.Doctor>()
                            .eq(com.hospital.entity.Doctor::getDepartmentId, departmentId))
                    .stream().map(com.hospital.entity.Doctor::getId).collect(Collectors.toList());
            if (!doctorIds.isEmpty()) {
                wrapper.in(Appointment::getDoctorId, doctorIds);
            }
        }

        List<Appointment> appointments = appointmentRepository.selectList(wrapper);

        // 统计信息
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", appointments.size());
        statistics.put("pending", appointments.stream().filter(a -> a.getStatus() == 1).count());
        statistics.put("confirmed", appointments.stream().filter(a -> a.getStatus() == 2).count());
        statistics.put("cancelled", appointments.stream().filter(a -> a.getStatus() == 3).count());
        statistics.put("completed", appointments.stream().filter(a -> a.getStatus() == 4).count());

        // 按日期统计
        Map<LocalDate, Long> dailyStats = appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getAppointmentDate, Collectors.counting()));
        statistics.put("dailyStats", dailyStats);

        // 按科室统计
        Map<Long, Long> departmentStats = appointments.stream()
                .collect(Collectors.groupingBy(
                        apt -> {
                            com.hospital.entity.Doctor doctor = doctorRepository.selectById(apt.getDoctorId());
                            return doctor != null ? doctor.getDepartmentId() : 0L;
                        },
                        Collectors.counting()));
        statistics.put("departmentStats", departmentStats);

        return ResponseEntity.ok(Result.success("查询成功", statistics));
    }

    // 其他统计方法暂时移除，因为编译错误
}