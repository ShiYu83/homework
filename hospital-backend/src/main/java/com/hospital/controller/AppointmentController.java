package com.hospital.controller;

import com.hospital.annotation.RequireRole;
import com.hospital.common.Result;
import com.hospital.dto.AppointmentCreateDTO;
import com.hospital.dto.TimeSlotDTO;
import com.hospital.entity.Appointment;
import com.hospital.service.AppointmentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping("/available-slots")
    public ResponseEntity<Result<List<TimeSlotDTO>>> getAvailableSlots(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam String date) {
        List<TimeSlotDTO> slots = appointmentService.getAvailableSlots(departmentId, doctorId, date);
        return ResponseEntity.ok(Result.success("查询成功", slots));
    }

    @PostMapping("/create")
    @RequireRole(1) // 仅患者
    public ResponseEntity<Result<Appointment>> create(@RequestBody AppointmentCreateDTO dto) {
        Appointment appointment = appointmentService.create(dto);
        return ResponseEntity.ok(Result.success("预约成功", appointment));
    }

    @PutMapping("/modify/{id}")
    @RequireRole(1) // 仅患者
    public ResponseEntity<Result<Appointment>> modify(
            @PathVariable Long id,
            @RequestBody ModifyAppointmentDTO dto) {
        Appointment appointment = appointmentService.modify(id, dto.getScheduleId(), dto.getTimeSlot());
        return ResponseEntity.ok(Result.success("修改成功", appointment));
    }

    @GetMapping("/patient/{patientId}")
    @RequireRole(1) // 仅患者
    public ResponseEntity<Result<List<Appointment>>> queryByPatient(@PathVariable Long patientId) {
        List<Appointment> appointments = appointmentService.queryByPatient(patientId);
        return ResponseEntity.ok(Result.success("查询成功", appointments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<Appointment>> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            return ResponseEntity.ok(Result.error(404, "预约不存在"));
        }
        return ResponseEntity.ok(Result.success(appointment));
    }

    @PutMapping("/cancel/{id}")
    @RequireRole(1) // 仅患者
    public ResponseEntity<Result<Void>> cancel(@PathVariable Long id, @RequestParam(required = false) String reason) {
        appointmentService.cancel(id, reason);
        return ResponseEntity.ok(Result.success("取消成功", null));
    }

    @Data
    static class ModifyAppointmentDTO {
        private Long scheduleId;
        private String timeSlot;
    }
}
