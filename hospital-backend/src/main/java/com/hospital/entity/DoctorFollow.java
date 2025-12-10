package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 医生关注实体类
 */
@Data
@TableName("doctor_follow")
public class DoctorFollow {
  @TableId(type = IdType.AUTO)
  private Long id;

  private Long patientId;

  private Long doctorId;

  private LocalDateTime createTime;
}