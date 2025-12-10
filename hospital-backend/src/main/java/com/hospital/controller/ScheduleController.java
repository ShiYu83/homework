package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Schedule;
import com.hospital.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {
    
    private final ScheduleService scheduleService;
    
    @GetMapping("/list")
    public ResponseEntity<Result<List<Schedule>>> list(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Schedule> schedules = scheduleService.getSchedules(doctorId, departmentId, startDate, endDate);
        return ResponseEntity.ok(Result.success("查询成功", schedules));
    }
    
    @PostMapping("/create")
    public ResponseEntity<Result<Schedule>> create(@RequestBody Schedule schedule) {
        Schedule created = scheduleService.createSchedule(schedule);
        return ResponseEntity.ok(Result.success("创建成功", created));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Result<Schedule>> update(@PathVariable Long id, @RequestBody Schedule schedule) {
        Schedule updated = scheduleService.updateSchedule(id, schedule);
        return ResponseEntity.ok(Result.success("更新成功", updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Object>> delete(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.ok(Result.success("删除成功", null));
    }
}

