package com.hospital.service;

import com.hospital.entity.Schedule;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {
    List<Schedule> getSchedules(Long doctorId, Long departmentId, LocalDate startDate, LocalDate endDate);
    Schedule createSchedule(Schedule schedule);
    Schedule updateSchedule(Long id, Schedule schedule);
    void deleteSchedule(Long id);
    void updateSlotCount(Long scheduleId, int reservedCount);
}

