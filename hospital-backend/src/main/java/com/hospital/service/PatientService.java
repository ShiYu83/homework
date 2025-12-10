package com.hospital.service;

import com.hospital.dto.PatientRegisterDTO;
import com.hospital.entity.Patient;

import java.util.List;
import java.util.Map;

public interface PatientService {
    Patient register(PatientRegisterDTO dto);
    
    Patient getById(Long id);
    
    void updateInfo(Patient patient);
    
    List<Patient> getFamilyMembers(Long userId);
    
    Map<String, Object> getCreditInfo(Long patientId);
}

