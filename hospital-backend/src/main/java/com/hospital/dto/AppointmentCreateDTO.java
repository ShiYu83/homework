package com.hospital.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AppointmentCreateDTO {
    @NotNull(message = "患者ID不能为空")
    private Long patientId;
    
    @NotNull(message = "排班ID不能为空")
    private Long scheduleId;
    
    private Long doctorId;
    
    private LocalDate appointmentDate;
    
    private String timeSlot;
}

