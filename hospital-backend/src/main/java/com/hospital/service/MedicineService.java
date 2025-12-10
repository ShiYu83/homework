package com.hospital.service;

import com.hospital.entity.Medicine;

import java.util.List;

public interface MedicineService {
    List<Medicine> getAllMedicines();
    
    List<Medicine> getAvailableMedicines(String keyword);
    
    Medicine getMedicineById(Long id);
    
    Medicine createMedicine(Medicine medicine);
    
    Medicine updateMedicine(Medicine medicine);
    
    void deleteMedicine(Long id);
    
    void updateStatus(Long id, Integer status);
}

