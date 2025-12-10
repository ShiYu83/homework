package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("prescription_item")
public class PrescriptionItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long prescriptionId;
    
    private Long medicineId;
    
    private Integer quantity;
    
    private BigDecimal price; // 单价快照
    
    private BigDecimal subtotal; // 小计
    
    private LocalDateTime createTime;
}

