package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Appointment;
import com.hospital.repository.AppointmentRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {
    
    private final AppointmentRepository appointmentRepository;
    
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<Result<Map<String, Object>>> getDoctorQueue(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        // 查询当天的预约
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getDoctorId, doctorId)
                .eq(Appointment::getAppointmentDate, date)
                .in(Appointment::getStatus, List.of(2)) // 已预约状态
                .orderByAsc(Appointment::getTimeSlot, Appointment::getCreateTime);
        
        List<Appointment> appointments = appointmentRepository.selectList(wrapper);
        
        // 按时间段分组
        Map<String, List<Appointment>> slotsMap = appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getTimeSlot));
        
        Map<String, Object> result = new HashMap<>();
        result.put("doctorId", doctorId);
        result.put("date", date);
        result.put("totalCount", appointments.size());
        result.put("slots", slotsMap);
        result.put("currentTime", LocalDateTime.now());
        
        return ResponseEntity.ok(Result.success("查询成功", result));
    }
    
    @GetMapping("/waiting/{appointmentId}")
    public ResponseEntity<Result<Map<String, Object>>> getWaitingInfo(@PathVariable Long appointmentId) {
        Appointment appointment = appointmentRepository.selectById(appointmentId);
        if (appointment == null) {
            return ResponseEntity.ok(Result.error("预约不存在"));
        }
        
        // 查询前面有多少人
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getDoctorId, appointment.getDoctorId())
                .eq(Appointment::getAppointmentDate, appointment.getAppointmentDate())
                .eq(Appointment::getTimeSlot, appointment.getTimeSlot())
                .eq(Appointment::getStatus, 2)
                .lt(Appointment::getCreateTime, appointment.getCreateTime())
                .orderByAsc(Appointment::getCreateTime);
        
        List<Appointment> beforeMe = appointmentRepository.selectList(wrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("appointmentId", appointmentId);
        result.put("waitingCount", beforeMe.size());
        result.put("estimatedWaitTime", beforeMe.size() * 15); // 假设每人15分钟
        result.put("currentNumber", beforeMe.size() + 1);
        
        return ResponseEntity.ok(Result.success("查询成功", result));
    }
}

