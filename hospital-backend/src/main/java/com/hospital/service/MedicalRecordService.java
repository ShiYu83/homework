package com.hospital.service;

import com.hospital.entity.MedicalRecord;

import java.util.List;

public interface MedicalRecordService {
    MedicalRecord createMedicalRecord(MedicalRecord medicalRecord);
    
    MedicalRecord getMedicalRecordByAppointmentId(Long appointmentId);
    
    List<MedicalRecord> getMedicalRecordsByPatientId(Long patientId);
    
    List<MedicalRecord> getMedicalRecordsByDoctorId(Long doctorId);
}

