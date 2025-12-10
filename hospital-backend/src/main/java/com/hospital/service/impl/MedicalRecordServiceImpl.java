package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.entity.MedicalRecord;
import com.hospital.repository.MedicalRecordRepository;
import com.hospital.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {
    
    private final MedicalRecordRepository medicalRecordRepository;
    
    @Override
    @Transactional
    public MedicalRecord createMedicalRecord(MedicalRecord medicalRecord) {
        medicalRecord.setCreateTime(LocalDateTime.now());
        medicalRecord.setUpdateTime(LocalDateTime.now());
        medicalRecordRepository.insert(medicalRecord);
        return medicalRecord;
    }
    
    @Override
    public MedicalRecord getMedicalRecordByAppointmentId(Long appointmentId) {
        return medicalRecordRepository.selectOne(
            new LambdaQueryWrapper<MedicalRecord>()
                .eq(MedicalRecord::getAppointmentId, appointmentId)
        );
    }
    
    @Override
    public List<MedicalRecord> getMedicalRecordsByPatientId(Long patientId) {
        return medicalRecordRepository.selectList(
            new LambdaQueryWrapper<MedicalRecord>()
                .eq(MedicalRecord::getPatientId, patientId)
                .orderByDesc(MedicalRecord::getCreateTime)
        );
    }
    
    @Override
    public List<MedicalRecord> getMedicalRecordsByDoctorId(Long doctorId) {
        return medicalRecordRepository.selectList(
            new LambdaQueryWrapper<MedicalRecord>()
                .eq(MedicalRecord::getDoctorId, doctorId)
                .orderByDesc(MedicalRecord::getCreateTime)
        );
    }
}

