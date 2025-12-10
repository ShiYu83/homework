package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 诊室实体
 */
@Data
@TableName("consulting_room")
public class ConsultingRoom {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String roomNo; // 诊室编号
    
    private String name; // 诊室名称
    
    private Long departmentId; // 所属科室ID
    
    private Long doctorId; // 所属医生ID
    
    private String location; // 位置
    
    private Integer status; // 状态 0-停用 1-启用
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}

