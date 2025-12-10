package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("medical_record")
public class MedicalRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long appointmentId;
    
    private Long patientId;
    
    private Long doctorId;
    
    private String diagnosis;
    
    private String symptoms;
    
    private String advice;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}

