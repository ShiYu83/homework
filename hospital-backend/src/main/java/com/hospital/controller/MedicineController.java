package com.hospital.controller;

import com.hospital.annotation.RequireRole;
import com.hospital.common.Result;
import com.hospital.entity.Medicine;
import com.hospital.repository.SysUserRepository;
import com.hospital.service.MedicineService;
import com.hospital.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/medicine")
@RequiredArgsConstructor
public class MedicineController {
    
    private final MedicineService medicineService;
    private final JwtUtil jwtUtil;
    private final SysUserRepository sysUserRepository;
    
    /**
     * 获取所有药品（管理员）
     */
    @GetMapping("/all")
    @RequireRole(3)
    public ResponseEntity<Result<List<Medicine>>> getAllMedicines() {
        List<Medicine> medicines = medicineService.getAllMedicines();
        return ResponseEntity.ok(Result.success(medicines));
    }
    
    /**
     * 获取已上架药品（医生可用）
     */
    @GetMapping("/available")
    public ResponseEntity<Result<List<Medicine>>> getAvailableMedicines(
            @RequestParam(required = false) String keyword) {
        List<Medicine> medicines = medicineService.getAvailableMedicines(keyword);
        return ResponseEntity.ok(Result.success(medicines));
    }
    
    /**
     * 根据ID获取药品
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<Medicine>> getMedicineById(@PathVariable Long id) {
        Medicine medicine = medicineService.getMedicineById(id);
        if (medicine == null) {
            return ResponseEntity.ok(Result.error(404, "药品不存在"));
        }
        return ResponseEntity.ok(Result.success(medicine));
    }
    
    /**
     * 创建药品（管理员）
     */
    @PostMapping
    @RequireRole(3)
    public ResponseEntity<Result<Medicine>> createMedicine(@RequestBody Medicine medicine) {
        if (medicine.getName() == null || medicine.getPrice() == null) {
            return ResponseEntity.ok(Result.error(400, "药品名称和价格不能为空"));
        }
        Medicine created = medicineService.createMedicine(medicine);
        return ResponseEntity.ok(Result.success("药品创建成功", created));
    }
    
    /**
     * 更新药品（管理员）
     */
    @PutMapping("/{id}")
    @RequireRole(3)
    public ResponseEntity<Result<Medicine>> updateMedicine(@PathVariable Long id, @RequestBody Medicine medicine) {
        medicine.setId(id);
        Medicine updated = medicineService.updateMedicine(medicine);
        return ResponseEntity.ok(Result.success("药品更新成功", updated));
    }
    
    /**
     * 删除药品（管理员）
     */
    @DeleteMapping("/{id}")
    @RequireRole(3)
    public ResponseEntity<Result<Object>> deleteMedicine(@PathVariable Long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.ok(Result.success("药品删除成功", null));
    }
    
    /**
     * 上架/下架药品（管理员）
     */
    @PutMapping("/{id}/status")
    @RequireRole(3)
    public ResponseEntity<Result<Object>> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status != 0 && status != 1) {
            return ResponseEntity.ok(Result.error(400, "状态值无效，0-下架，1-上架"));
        }
        medicineService.updateStatus(id, status);
        return ResponseEntity.ok(Result.success(status == 1 ? "药品已上架" : "药品已下架", null));
    }
}

