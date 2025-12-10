package com.hospital.service;

import com.hospital.entity.Prescription;
import com.hospital.entity.PrescriptionItem;

import java.util.List;
import java.util.Map;

public interface PrescriptionService {
    Prescription createPrescription(Long medicalRecordId, Long appointmentId, List<PrescriptionItem> items);
    
    Prescription getPrescriptionById(Long id);
    
    Prescription getPrescriptionByAppointmentId(Long appointmentId);
    
    List<Prescription> getPrescriptionsByPatientId(Long patientId);
    
    List<Prescription> getAllPrescriptions();
    
    Map<String, Object> getPrescriptionDetail(Long prescriptionId);
}

