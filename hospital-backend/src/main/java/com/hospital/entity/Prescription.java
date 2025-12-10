package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("prescription")
public class Prescription {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long medicalRecordId;
    
    private Long appointmentId;
    
    private BigDecimal totalAmount;
    
    private Integer status; // 1-待支付 2-已支付
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}

