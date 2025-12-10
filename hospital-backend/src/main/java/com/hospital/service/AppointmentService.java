package com.hospital.service;

import com.hospital.dto.AppointmentCreateDTO;
import com.hospital.dto.TimeSlotDTO;
import com.hospital.entity.Appointment;

import java.util.List;

public interface AppointmentService {
    Appointment create(AppointmentCreateDTO dto);
    
    void cancel(Long appointmentId, String reason);
    
    Appointment modify(Long appointmentId, Long newScheduleId, String newTimeSlot);
    
    List<Appointment> queryByPatient(Long patientId);
    
    List<TimeSlotDTO> getAvailableSlots(Long departmentId, Long doctorId, String date);
    
    Appointment getById(Long id);
}


