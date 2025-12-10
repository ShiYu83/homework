package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.entity.Medicine;
import com.hospital.entity.Prescription;
import com.hospital.entity.PrescriptionItem;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.MedicineRepository;
import com.hospital.repository.PrescriptionItemRepository;
import com.hospital.repository.PrescriptionRepository;
import com.hospital.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {
    
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final MedicineRepository medicineRepository;
    private final AppointmentRepository appointmentRepository;
    
    @Override
    @Transactional
    public Prescription createPrescription(Long medicalRecordId, Long appointmentId, List<PrescriptionItem> items) {
        // 计算总金额
        BigDecimal totalAmount = items.stream()
            .map(PrescriptionItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 创建处方
        Prescription prescription = new Prescription();
        prescription.setMedicalRecordId(medicalRecordId);
        prescription.setAppointmentId(appointmentId);
        prescription.setTotalAmount(totalAmount);
        prescription.setStatus(1); // 待支付
        prescription.setCreateTime(LocalDateTime.now());
        prescription.setUpdateTime(LocalDateTime.now());
        prescriptionRepository.insert(prescription);
        
        // 创建处方明细
        for (PrescriptionItem item : items) {
            item.setPrescriptionId(prescription.getId());
            item.setCreateTime(LocalDateTime.now());
            prescriptionItemRepository.insert(item);
        }
        
        return prescription;
    }
    
    @Override
    public Prescription getPrescriptionById(Long id) {
        return prescriptionRepository.selectById(id);
    }
    
    @Override
    public Prescription getPrescriptionByAppointmentId(Long appointmentId) {
        return prescriptionRepository.selectOne(
            new LambdaQueryWrapper<Prescription>()
                .eq(Prescription::getAppointmentId, appointmentId)
        );
    }
    
    @Override
    public List<Prescription> getPrescriptionsByPatientId(Long patientId) {
        // 需要通过appointment关联查询
        // 先查询该患者的所有预约ID
        List<Long> appointmentIds = appointmentRepository.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.hospital.entity.Appointment>()
                .eq(com.hospital.entity.Appointment::getPatientId, patientId)
                .select(com.hospital.entity.Appointment::getId)
        ).stream().map(com.hospital.entity.Appointment::getId).collect(java.util.stream.Collectors.toList());
        
        if (appointmentIds.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        // 根据预约ID查询处方
        return prescriptionRepository.selectList(
            new LambdaQueryWrapper<Prescription>()
                .in(Prescription::getAppointmentId, appointmentIds)
                .orderByDesc(Prescription::getCreateTime)
        );
    }
    
    @Override
    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.selectList(
            new LambdaQueryWrapper<Prescription>()
                .orderByDesc(Prescription::getCreateTime)
        );
    }
    
    @Override
    public Map<String, Object> getPrescriptionDetail(Long prescriptionId) {
        Prescription prescription = prescriptionRepository.selectById(prescriptionId);
        if (prescription == null) {
            return null;
        }
        
        List<PrescriptionItem> items = prescriptionItemRepository.selectList(
            new LambdaQueryWrapper<PrescriptionItem>()
                .eq(PrescriptionItem::getPrescriptionId, prescriptionId)
        );
        
        // 填充药品信息
        List<Map<String, Object>> itemDetails = items.stream().map(item -> {
            Medicine medicine = medicineRepository.selectById(item.getMedicineId());
            Map<String, Object> detail = new HashMap<>();
            detail.put("id", item.getId());
            detail.put("medicineId", item.getMedicineId());
            detail.put("medicineName", medicine != null ? medicine.getName() : "未知药品");
            detail.put("quantity", item.getQuantity());
            detail.put("price", item.getPrice());
            detail.put("subtotal", item.getSubtotal());
            detail.put("unit", medicine != null ? medicine.getUnit() : "");
            detail.put("specification", medicine != null ? medicine.getSpecification() : "");
            return detail;
        }).collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("prescription", prescription);
        result.put("items", itemDetails);
        
        return result;
    }
}

