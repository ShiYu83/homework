package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 调班申请表
 */
@Data
@TableName("schedule_adjustment")
public class ScheduleAdjustment {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long doctorId; // 医生ID
    
    private Long scheduleId; // 排班ID
    
    private LocalDate originalDate; // 原排班日期
    
    private String originalTimeSlot; // 原时间段
    
    private LocalDate newDate; // 新排班日期
    
    private String newTimeSlot; // 新时间段
    
    private String reason; // 调班原因
    
    private Integer status; // 状态 0-待审核 1-已通过 2-已拒绝
    
    private Long reviewerId; // 审核人ID（管理员）
    
    private String reviewComment; // 审核意见
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}

