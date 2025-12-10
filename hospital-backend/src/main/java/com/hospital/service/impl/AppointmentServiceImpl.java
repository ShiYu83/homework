package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.dto.AppointmentCreateDTO;
import com.hospital.dto.TimeSlotDTO;
import com.hospital.entity.Appointment;
import com.hospital.entity.Schedule;
import com.hospital.entity.SysUser;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.ScheduleRepository;
import com.hospital.repository.SysUserRepository;
import com.hospital.service.AppointmentService;
import com.hospital.util.CurrentUserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final SysUserRepository sysUserRepository;
    
    @Override
    @Transactional
    public Appointment create(AppointmentCreateDTO dto) {
        // 查询排班信息
        Schedule schedule = scheduleRepository.selectById(dto.getScheduleId());
        if (schedule == null) {
            throw new RuntimeException("排班信息不存在");
        }
        
        // 检查号源是否充足
        if (schedule.getReservedCount() >= schedule.getTotalCount()) {
            throw new RuntimeException("号源已满");
        }
        
        // 检查状态
        if (schedule.getStatus() != 1) {
            throw new RuntimeException("该排班已停诊");
        }
        
        // 生成预约号
        String appointmentNo = generateAppointmentNo();
        
        // 创建预约记录
        Appointment appointment = new Appointment();
        appointment.setPatientId(dto.getPatientId());
        appointment.setDoctorId(schedule.getDoctorId());
        appointment.setScheduleId(dto.getScheduleId());
        appointment.setAppointmentNo(appointmentNo);
        appointment.setAppointmentDate(schedule.getScheduleDate());
        appointment.setTimeSlot(schedule.getTimeSlot());
        appointment.setStatus(1); // 待支付
        appointment.setFee(new BigDecimal("25.00")); // 默认挂号费
        appointment.setCreateTime(LocalDateTime.now());
        
        appointmentRepository.insert(appointment);
        
        // 更新排班的已预约数
        schedule.setReservedCount(schedule.getReservedCount() + 1);
        scheduleRepository.updateById(schedule);
        
        return appointment;
    }
    
    @Override
    @Transactional
    public void cancel(Long appointmentId, String reason) {
        // 获取当前登录用户ID
        Long userId = CurrentUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        // 查询当前用户信息
        SysUser currentUser = sysUserRepository.selectById(userId);
        if (currentUser == null || currentUser.getPatientId() == null) {
            throw new RuntimeException("权限不足");
        }
        
        // 查询预约记录
        Appointment appointment = appointmentRepository.selectById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("预约记录不存在");
        }
        
        // 验证当前用户是否有权限取消该预约
        if (!appointment.getPatientId().equals(currentUser.getPatientId())) {
            throw new RuntimeException("权限不足，无法取消他人预约");
        }
        
        if (appointment.getStatus() == 3) {
            throw new RuntimeException("预约已取消");
        }
        
        // 更新预约状态
        appointment.setStatus(3); // 已取消
        appointment.setCancelReason(reason);
        appointment.setCancelTime(LocalDateTime.now());
        appointment.setUpdateTime(LocalDateTime.now());
        appointmentRepository.updateById(appointment);
        
        // 释放号源
        Schedule schedule = scheduleRepository.selectById(appointment.getScheduleId());
        if (schedule != null) {
            schedule.setReservedCount(Math.max(0, schedule.getReservedCount() - 1));
            scheduleRepository.updateById(schedule);
        }
    }
    
    @Override
    @Transactional
    public Appointment modify(Long appointmentId, Long newScheduleId, String newTimeSlot) {
        Appointment appointment = appointmentRepository.selectById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("预约记录不存在");
        }
        
        if (appointment.getStatus() == 3) {
            throw new RuntimeException("已取消的预约不能修改");
        }
        
        if (appointment.getStatus() == 4) {
            throw new RuntimeException("已完成的预约不能修改");
        }
        
        // 释放原号源
        Schedule oldSchedule = scheduleRepository.selectById(appointment.getScheduleId());
        if (oldSchedule != null) {
            oldSchedule.setReservedCount(Math.max(0, oldSchedule.getReservedCount() - 1));
            scheduleRepository.updateById(oldSchedule);
        }
        
        // 查询新排班信息
        Schedule newSchedule = scheduleRepository.selectById(newScheduleId);
        if (newSchedule == null) {
            throw new RuntimeException("新排班信息不存在");
        }
        
        // 检查新号源是否充足
        if (newSchedule.getReservedCount() >= newSchedule.getTotalCount()) {
            throw new RuntimeException("新号源已满");
        }
        
        // 更新预约信息
        appointment.setScheduleId(newScheduleId);
        appointment.setDoctorId(newSchedule.getDoctorId());
        appointment.setAppointmentDate(newSchedule.getScheduleDate());
        appointment.setTimeSlot(newTimeSlot != null ? newTimeSlot : newSchedule.getTimeSlot());
        appointment.setUpdateTime(LocalDateTime.now());
        appointmentRepository.updateById(appointment);
        
        // 占用新号源
        newSchedule.setReservedCount(newSchedule.getReservedCount() + 1);
        scheduleRepository.updateById(newSchedule);
        
        return appointment;
    }
    
    @Override
    public List<Appointment> queryByPatient(Long patientId) {
        return appointmentRepository.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getPatientId, patientId)
                .orderByDesc(Appointment::getCreateTime)
        );
    }
    
    @Override
    public List<TimeSlotDTO> getAvailableSlots(Long departmentId, Long doctorId, String date) {
        LocalDate scheduleDate = LocalDate.parse(date);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Schedule> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getScheduleDate, scheduleDate)
                .eq(Schedule::getStatus, 1)
                .last("LIMIT 1000");
        
        if (departmentId != null) {
            wrapper.eq(Schedule::getDepartmentId, departmentId);
        }
        
        if (doctorId != null) {
            wrapper.eq(Schedule::getDoctorId, doctorId);
        }
        
        List<Schedule> schedules = scheduleRepository.selectList(wrapper);
        
        return schedules.stream()
            .map(schedule -> {
                TimeSlotDTO dto = new TimeSlotDTO();
                dto.setScheduleId(schedule.getId());
                dto.setTimeSlot(schedule.getTimeSlot());
                dto.setTotalCount(schedule.getTotalCount());
                dto.setRemaining(schedule.getTotalCount() - schedule.getReservedCount());
                return dto;
            })
            .filter(dto -> dto.getRemaining() > 0)
            .collect(Collectors.toList());
    }
    
    @Override
    public Appointment getById(Long id) {
        return appointmentRepository.selectById(id);
    }
    
    private String generateAppointmentNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateStr = LocalDate.now().format(formatter);
        String sequence = String.format("%04d", System.currentTimeMillis() % 10000);
        return dateStr + sequence;
    }
}

