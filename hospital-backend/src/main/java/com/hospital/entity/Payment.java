package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long appointmentId;
    
    private String orderNo;
    
    private BigDecimal amount;
    
    private Integer paymentMethod; // 1-微信 2-支付宝 3-医保 4-现金
    
    private Integer status; // 1-待支付 2-已支付 3-已退款
    
    private LocalDateTime payTime;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private Integer orderType; // 1-挂号费 2-药品费 3-合并订单
    
    private Long prescriptionId; // 处方ID（如果是药品订单）
}

