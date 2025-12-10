package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("patient")
public class Patient {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String idCard;
    
    private String phone;
    
    private Integer gender;
    
    private LocalDate birthday;
    
    private String allergyHistory;
    
    private Integer creditScore; // 诚信度分数 0-100，默认100
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}

