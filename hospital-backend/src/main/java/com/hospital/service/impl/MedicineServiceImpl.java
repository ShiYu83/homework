package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.entity.Medicine;
import com.hospital.repository.MedicineRepository;
import com.hospital.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {
    
    private final MedicineRepository medicineRepository;
    
    @Override
    public List<Medicine> getAllMedicines() {
        return medicineRepository.selectList(null);
    }
    
    @Override
    public List<Medicine> getAvailableMedicines(String keyword) {
        LambdaQueryWrapper<Medicine> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Medicine::getStatus, 1); // 只查询已上架的药品
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Medicine::getName, keyword);
        }
        wrapper.orderByDesc(Medicine::getCreateTime);
        return medicineRepository.selectList(wrapper);
    }
    
    @Override
    public Medicine getMedicineById(Long id) {
        return medicineRepository.selectById(id);
    }
    
    @Override
    @Transactional
    public Medicine createMedicine(Medicine medicine) {
        medicine.setCreateTime(LocalDateTime.now());
        medicine.setUpdateTime(LocalDateTime.now());
        medicineRepository.insert(medicine);
        return medicine;
    }
    
    @Override
    @Transactional
    public Medicine updateMedicine(Medicine medicine) {
        medicine.setUpdateTime(LocalDateTime.now());
        medicineRepository.updateById(medicine);
        return medicine;
    }
    
    @Override
    @Transactional
    public void deleteMedicine(Long id) {
        medicineRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        Medicine medicine = medicineRepository.selectById(id);
        if (medicine != null) {
            medicine.setStatus(status);
            medicine.setUpdateTime(LocalDateTime.now());
            medicineRepository.updateById(medicine);
        }
    }
}

