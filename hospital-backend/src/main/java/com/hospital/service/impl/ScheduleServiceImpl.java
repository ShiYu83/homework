package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.entity.Schedule;
import com.hospital.repository.ScheduleRepository;
import com.hospital.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    
    private final ScheduleRepository scheduleRepository;
    
    @Override
    public List<Schedule> getSchedules(Long doctorId, Long departmentId, LocalDate startDate, LocalDate endDate) {
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
        wrapper.eq(Schedule::getStatus, 1);
        wrapper.orderByAsc(Schedule::getScheduleDate, Schedule::getTimeSlot);
        return scheduleRepository.selectList(wrapper);
    }
    
    @Override
    public Schedule createSchedule(Schedule schedule) {
        scheduleRepository.insert(schedule);
        return schedule;
    }
    
    @Override
    public Schedule updateSchedule(Long id, Schedule schedule) {
        schedule.setId(id);
        scheduleRepository.updateById(schedule);
        return scheduleRepository.selectById(id);
    }
    
    @Override
    public void deleteSchedule(Long id) {
        Schedule schedule = new Schedule();
        schedule.setId(id);
        schedule.setStatus(0);
        scheduleRepository.updateById(schedule);
    }
    
    @Override
    public void updateSlotCount(Long scheduleId, int reservedCount) {
        Schedule schedule = scheduleRepository.selectById(scheduleId);
        if (schedule != null) {
            schedule.setReservedCount(reservedCount);
            scheduleRepository.updateById(schedule);
        }
    }
}

