package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("medicine")
public class Medicine {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private BigDecimal price;
    
    private String unit;
    
    private String specification;
    
    private Integer stock;
    
    private Integer status; // 0-下架 1-上架
    
    private String category;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}

